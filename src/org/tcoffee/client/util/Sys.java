package org.tcoffee.client.util;



/**
 * General settings and utilities 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class Sys {
		

	/**
	 * The application name to be printed 
	 */
	static public final String appname = "Cloud-Coffee Client";

	/**
	 * The application binary name to be printed on the command line 
	 */
	static public final String appbin = "c-coffee";
	
	/** 
	 * The application version 
	 */
	static public final Version appver = new Version("1.0.2");
	
	static public boolean debug;
	
	static public boolean quiet = false;
	
	static public void print( String message, Object... args ) { 

		if( quiet ) { return; }
		
		if( args == null || args.length == 0 ) { 
			System.out.print(message);
		}
		else { 
			System.out.printf(message, args);
		}
	}
	
	static public void println( String message, Object... args ) { 

		if( quiet ) { return; }
		
		if( args == null || args.length == 0 ) { 
			System.out.println(message);
		}
		else { 
			System.out.printf(message, args);
			System.out.println();
		}
	}	
	
	public static void error() { 
		System.exit(1);
	}
	
	public static void error(String message, Object ... args ) { 
		print(message,args);
		System.out.println();
		System.exit(1);
	}
	
	public static void error(Throwable e ) { 
		if( debug ) { 
			e.printStackTrace();
		}
		else { 
			System.out.println(e.getMessage());
		}
		System.exit(1);
	}
	

	public static void debug(String message, Object ... args) {
		if( debug ) { 
			print(message, args);
			System.out.println();
		}
	}
	
	public static void debug(Throwable e) { 
		if( debug && e != null ) { 
			e.printStackTrace(System.out);
		}
	}
	

	
	
}
