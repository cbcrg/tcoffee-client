package org.tcoffee.client;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.tcoffee.client.CommandLine.Option;
import org.tcoffee.client.data.ResultData;
import org.tcoffee.client.exception.ClientException;
import org.tcoffee.client.exception.WaitResultTimeout;
import org.tcoffee.client.util.Sys;
import org.tcoffee.client.util.Time;

/**
 * Execute a generic bundle service remotly 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class CloudCoffee {
	
	private CommandLine cmd;
	
	private Properties props;

	private File propsFile;

	public CloudCoffee(String[] args) { 

		/* 
		 * read application properties 
		 */
		props = readProperties();
		
		/* 
		 * configure the command line options
		 */
		cmd = new CommandLine();
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

	/*
	 * try to fetch the configuration value following the order 
	 * 1) command line argument, if not found -> 2
	 * 2) properties file, if not found -> 3 
	 * 3) command line default value, if empty -> null
	 */
	
	String param( String name  ) { 
		String result = null ;
		if( cmd.hasOption(name) ) { 
			result = cmd.getOption(name);
		}

		if( StringUtils.isEmpty(result) ) { 
			result = props.getProperty(name);
		}
		
		Option opt = cmd.declaredOptions != null ? cmd.declaredOptions.get(name) : null;
		
		if( StringUtils.isEmpty(result) && opt != null) { 
			result = opt.defValue;
		}
		
		if( opt.required && StringUtils.isEmpty(result) ) { 
			throw new ClientException("Configuration property '%s' is missing. Try to enter a value on the command line.", name);
		}
		
		return result;
	}
	
	/*
	 * parse a client parameter as a duration string and returns the number of seconds 
	 * that it specify 
	 */
	Integer paramAsDuration( String name ) { 
		String val = param(name);
		if( val == null ) { 
			return null;
		}
		
		try { 
			return Time.parseDuration(val);
		}
		catch(IllegalArgumentException e) { 
			Sys.debug("Invalid duration value: %s", val);
			return null;
		}
	}
	
	/*
	 * parse a application parameter as a integer value, returns null if does not exist
	 */
	Integer paramAsInt( String name ) { 
		
		String val = param(name);
		if( val == null ) { 
			return null;
		}
		
		try { 
			return Integer.parseInt(val);
		}
		catch(NumberFormatException e) { 
			Sys.debug("Invalid integer value: '%s'", val);
			return null;
		}
	}

	/*
	 * parse a application parameter as a integer value, returns null if does not exist
	 */
	Long paramAsLong( String name ) { 
		
		String val = param(name);
		if( val == null ) { 
			return null;
		}
		
		try { 
			return Long.parseLong(val);
		}
		catch(NumberFormatException e) { 
			Sys.debug("Invalid long value: '%s'", val);
			return null;
		}
	}
	
	
	/*
	 * Read the application properties file. The file is supposed to be in the user home path, named 
	 * like the application binary name prefixed with a '.' char 
	 * 
	 * @return the application properties, or an empty properties object if the file does not exist
	 */
	Properties readProperties() {
		if( propsFile == null ) { 
			String name = "." + Sys.appbin;
			String home = System.getProperty("user.home");
			propsFile = home != null ? new File(home,name) : new File(name);
			Sys.debug("Application properties file name: %s", propsFile);
		}
		
		Properties result = new Properties();
		if( propsFile.exists() ) { 
			try {
				result.load( new FileReader(propsFile) );
			} catch (IOException e) {
				throw new ClientException(e, "Unable to read properties file: %s", propsFile);
			}
		}

		return result;
	}
	
	/**
	 * Save the current properties 
	 */
	void saveProperties() { 
		try {
			FileWriter out = new FileWriter(propsFile);
			props.store( out, null);
		} 
		catch (IOException e) {
			Sys.debug(e);
		}
	}



	/**
	 * The main client applicaton run method
	 */
	public void run() { 
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
		if( cmd.isEmpty() || cmd.hasOption("help") || cmd.arguments.isEmpty() ) { 
			Sys.println(cmd.usage());
			System.exit(0);
		}
		

		/* 
		 * some client parameters 
		 */
		String host = param("host");
		String bundle = param("bundle");
		Integer pollTimeout = paramAsDuration("poll-timeout");
		Integer pollSleep = paramAsDuration("poll-sleep");
		
		String sOutPath = param("out-path");
		
		
		/* 
		 * create and configure the client 
		 */

		TCoffeeClient client = new TCoffeeClient(host, bundle);
		try { 
			/* some configuration */
			if( pollTimeout != null ) { 
				client.setPollTimeoutSecs(pollTimeout);
			}
			if( pollSleep != null ) { 
		 		client.setPollSleepSecs(pollSleep);
			}
			if( StringUtils.isNotEmpty(sOutPath)) { 
				client.setOutputPath(new File(sOutPath));
			}

		
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
			client.run(cmd.getArgumentsString(), cmd.getArgumentsFiles());
			
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
		main.run();
	}


		
}
