package org.tcoffee.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.tcoffee.client.cli.CommandLine;
import org.tcoffee.client.exception.ClientException;
import org.tcoffee.client.util.IO;
import org.tcoffee.client.util.KeyValue;
import org.tcoffee.client.util.Sys;
import org.tcoffee.client.util.Version;

/**
 * Generic client for server interaction 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class TClient extends AbstractClient {

	
	/**
	 * Client entry point 
	 * 
	 * @param args the CLI arguments
	 */
	public static void main(String[] args) { 
	
		new TClient(args).execute();
	}
	
	
	/**
	 * Client initialization 
	 * 
	 * @param args the CL arguments arrays
	 */
	public TClient(String[] args) { 
		/*
		 * preliminary initialization
		 */
		Sys.appname = "T-Server Remoting Client";
		Sys.appbin = "t-client";
		Sys.appver = new Version("1.0");

		/*
		 * read properties 
		 */
		props = readProperties();

		/*
		 * prepare the command line parser 
		 */
		cmd = new CommandLine();
		cmd.addOption("host", "The remote host to which connect", null, "tcoffee.crg.cat", true);
		cmd.addOption("bundle", "The remote bundle serving the request", null, "tcoffee", true);
		cmd.addOption("quiet", "Run quietly without any console output");
		cmd.addOption("debug", "Run showing debug informations");
		cmd.addOption("ping", "Ping the server to verify service integrity");
		cmd.addOption("out-path", "The path where to save the output files");
		cmd.addOption("flat-path", "Do not create subfolder for the server returned files","true|false","true",true);
		cmd.addOption("poll-timeout", "Max time to wait for the request completion (seconds)", "n", "1h", false);
		cmd.addOption("poll-sleep", "Time to sleep between each poll requests", "n", "5s", false);
		cmd.addOption("help", "Print this help");
		cmd.addOption("update-client", "Update the client and exit");
		cmd.addOption("ver", "Print the client version number");
		cmd.addOption("list", "Print the list of programs exposed by the server");
		cmd.addOption("program", "Invoke the execution of a program exposed by the server", "name", null, true);
		cmd.addOption("download", "Download result fiels for teh specified request", "512bc2d0", null, true);
		
	
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
	public void run() {

		TCoffeeClient client = createClient();
		
		/* 
		 * just a ping and exit 
		 */
		if( cmd.hasOption("ping")) { 
			client.ping();
			System.exit(0);
		}
		
		/*
		 * print the list of available services remotely  
		 */
		if( cmd.hasOption("list")) { 
			client.printServices();
			System.exit(0);
		}
		
		/*
		 * invoke the program selected
		 */
		if( cmd.hasOption("program") ) { 
			
			// check all argument to resolve the specied files as string values
			//TODO this handle only text files
			// future release should be able to manage binary files 
			List<KeyValue> params = new ArrayList<KeyValue>();
			for( String arg : cmd.getArguments() ) { 
				KeyValue pair = KeyValue.parse(arg);
				if( pair == null ) continue;
				
				pair.value = normalizeFiles(pair.value);
				params.add( pair );
			}
			
			/* 
			 * invoke the program requested
			 */
			client.runProgram( cmd.getOption("program"), params );

			System.exit(0);
		}
		
		if( cmd.hasOption("download") ) { 
			client.downloadFiles( cmd.getOption("download") );
		}
		
		
	}


	static Object normalizeFiles(Object eventuallyFileArgument)  {
		if( !(eventuallyFileArgument instanceof String)) { 
			return eventuallyFileArgument;
		}
		
		String str = (String) eventuallyFileArgument;
		if( str.startsWith("file:") ) { 
			File file = new File(str.substring(5).trim());
			try {
				return IO.readContentAsString(file).trim();
			} catch (IOException e) {
				throw new ClientException(e, "The entered file does not exist: '%s'", file);
			}
		}
		
		return str;
	}


	
}
