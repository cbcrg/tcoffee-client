package org.tcoffee.client;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.tcoffee.client.cli.CommandLine;
import org.tcoffee.client.cli.CommandLine.Option;
import org.tcoffee.client.exception.ClientException;
import org.tcoffee.client.util.Sys;
import org.tcoffee.client.util.Time;

abstract class AbstractClient {

	protected CommandLine cmd;
	
	protected Properties props;

	protected File propsFile;

	private String fHost;

	private String fBundle;

	private Integer fPollTimeout;

	private Integer fPollSleep;

	private String fOutPath;

	private Boolean fFlatPath;
	
	
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
		
		Option opt = cmd.getDeclaredOptions() != null ? cmd.getDeclaredOptions().get(name) : null;
		
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
	
	Boolean paramAsBool( String name ) { 
		
		String val = param(name);
		if( val == null ) { 
			return null;
		}
		
		return Boolean.parseBoolean(val);
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

	
	protected void preliminaryCheck() {
		/* 
		 * print the current version and exit 
		 */
		if( cmd.hasOption("ver") ) { 
			Sys.println(Sys.appver.toString());
			System.exit(0);
		}
		
		/* 
		 * print the help and exit 
		 */
		Sys.print("%s - rel %s\n", Sys.appname, Sys.appver);
		if( cmd.isEmpty() || cmd.hasOption("help")  ) { 
			Sys.println(cmd.usage());
			System.exit(0);
		}		
		
	}

	/**
	 * Configure the client properties before the create an instance of it.
	 * <p> 
	 * Invoked by the main execute method do not call it directly 
	 */
	protected void configureClient() {
		fHost = param("host");
		fBundle = param("bundle");
		fPollTimeout = paramAsDuration("poll-timeout");
		fPollSleep = paramAsDuration("poll-sleep");
		fOutPath = param("out-path");
		fFlatPath = paramAsBool("flat-path");

	}

	
	/** 
	 * create and configure the client 
	 */
	protected TCoffeeClient createClient() {

		TCoffeeClient client = new TCoffeeClient(fHost, fBundle);

		if( fPollTimeout != null ) { 
			client.setPollTimeoutSecs(fPollTimeout);
		}
		if( fPollSleep != null ) { 
	 		client.setPollSleepSecs(fPollSleep);
		}
		if( StringUtils.isNotEmpty(fOutPath)) { 
			client.setOutputPath(new File(fOutPath));
		}

		if( fFlatPath != null ) { 
			client.setUseFlatPath(fFlatPath);
		}
		return client;
	}	

	/** 
	 * Client exection method 
	 */
	public final void execute() { 

		preliminaryCheck();
		
		configureClient();
		
		run();
		
	}

	public abstract void run();
	
}
