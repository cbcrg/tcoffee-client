package org.tcoffee.client;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.tcoffee.client.cli.CommandLineWithFiles;
import org.tcoffee.client.data.ResultData;
import org.tcoffee.client.exception.WaitResultTimeout;
import org.tcoffee.client.util.Sys;

/**
 * Execute a generic bundle service remotly 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class CloudCoffee extends AbstractClient {
	

	public CloudCoffee(String[] args) { 

		/* 
		 * read application properties 
		 */
		props = readProperties();
		
		/* 
		 * configure the command line options
		 */
		cmd = new CommandLineWithFiles();
		cmd.addOption("host", "The remote host to which connect", null, "tcoffee.crg.cat", true);
		cmd.addOption("bundle", "The remote bundle serving the request", null, "tcoffee", true);
		cmd.addOption("quiet", "Run quietly without any console output");
		cmd.addOption("debug", "Run showing debug informations");
		cmd.addOption("ping", "Ping the server to verify service integrity");
		cmd.addOption("out-path", "The path where to save the output files");
		cmd.addOption("poll-timeout", "Max time to wait for the request completion (seconds)", "n", "1h", false);
		cmd.addOption("poll-sleep", "Time to sleep between each poll requests", "n", "5s", false);
		cmd.addOption("help", "Print this help");
		cmd.addOption("update-client", "Update the client and exit");
		cmd.addOption("ver", "Print the client version number");

		/*
		 * parse the command line 
		 */
		cmd.parse(args);
		
		/*
		 * check for debug mode
		 */
		if( cmd.hasOption("debug")) { 
			Sys.debug = true;
			Sys.quiet = false;
			org.apache.log4j.Logger.getRootLogger().setLevel( Level.DEBUG );
		}
		
		if( cmd.hasOption("quiet" )) { 
			Sys.quiet = true;
			Sys.debug = false;
		}
		

	}


	@Override
	protected void preliminaryCheck() {
		/* 
		 * print the current version and exit 
		 */
		if( cmd.hasOption("ver") ) { 
			Sys.println(Sys.appver.toString());
			System.exit(0);
		}
		
		if( cmd.hasOption("update-client") ) { 
			update(cmd.getOption("update-client"));
			System.exit(0);
		}
		
		/* 
		 * print the help and exit 
		 */
		Sys.print("%s - rel %s\n", Sys.appname, Sys.appver);
		if( cmd.isEmpty() || cmd.hasOption("help") || cmd.getArguments().isEmpty() ) { 
			Sys.println(cmd.usage());
			System.exit(0);
		}
	}

	/**
	 * The main client applicaton run method
	 */
	public void run() { 
		
		TCoffeeClient client = null;

		try { 
			client = createClient();
		
			/* 
			 * just a ping and exit 
			 */
			if( cmd.hasOption("ping")) { 
				client.ping();
				System.exit(0);
			}
			
			
			/*
			 * submit a generic request 
			 */
			client.run(cmd.getArgumentsString(), ((CommandLineWithFiles)cmd).getArgumentsFiles());
			
			/* 
			 * display the result as returned by T-Coffee
			 */
			ResultData result = client.getResult();
			if( result != null && result.isStatusDONE() ) { 
				Sys.print("\r%s", client.getResultLog());
			}
			else { 
				Sys.error("Your request terminated with errors. For more information check the file '_tcoffee.err.log'.");
			}
			
			/* 
			 * show the link to the result page
			 */
			if( client.getRequestUrl() != null ) { 
				Sys.print("\nYou can share this result using the following link %s\n", client.getRequestUrl());
			}
			
		}
		catch( WaitResultTimeout e ) { 
			String message = 
					"The submitted request does not complete in the expected time\n" +
					"You can continue to check for it at the following link %s\n";
			Sys.error(message, client.getRequestUrl());
		}
		catch( Exception e) { 
			Sys.error(e);			
		}
 	}
	



	/**
	 * Update the application library downloading from the remote repositority 
	 */
	private void update(String mode) {
		if( mode.equalsIgnoreCase("no") || mode.equalsIgnoreCase("false")) { 
			return;
		}
		
		/* 
		 * get the last update time 
		 */
		String sLastUpdate = props.getProperty("client.last.update");
		Long lastUpdate = null;
		try { 
			lastUpdate = Long.parseLong(sLastUpdate);
		} catch( NumberFormatException e ) { 
			Sys.debug("Not a long value: '%s'", sLastUpdate);
		}

		/* 
		 * if the last update was less than 1 day skip it 
		 */
		if( !"force".equals(mode) && lastUpdate != null && (System.currentTimeMillis()-lastUpdate < 1000 * 60 * 60 * 24) ) { 
			return;
		}
		
		try { 
			/* configure the updater */
			String sLibrary = System.getenv("CLOUDCOFFEE_LIB");
			if( StringUtils.isEmpty(sLibrary)) { 
				Sys.debug("Missing CLOUDCOFFEE_LIB in your environment cannot update the client");
				return;
			}

			File library = new File(sLibrary);
			ClientUpdater updater = new ClientUpdater();
			updater.targetPath = library.getParent();
			updater.targetFile = library.getName();
			
			/* 
			 * download the updated client 
			 */
			updater.update();
		}
		
		/* 
		 * save the last update time in any case 
		 */
		finally { 
			props.setProperty("client.last.update", String.valueOf(new Date().getTime()));
			saveProperties();
		}
	}

	/**
	 * T-Coffee remote client entry point
	 * @throws URISyntaxException 
	 */
	public static void main(String[] args) throws URISyntaxException {

		CloudCoffee main = new CloudCoffee (args);
		main.execute();
	}


		
}
