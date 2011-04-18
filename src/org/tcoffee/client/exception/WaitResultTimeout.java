package org.tcoffee.client.exception;

/**
 * This exception is raised when waiting the result time expires 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class WaitResultTimeout extends ClientException {

	private static final long serialVersionUID = 1L;

	public WaitResultTimeout( String message, Object ... args ) {
		super(String.format(message, args));
	}
	
	public WaitResultTimeout( Throwable t, String message, Object... args ) { 
		super(String.format(message,args), t);
	}
	
	public WaitResultTimeout( Throwable t ){ 
		super(t);
	}	
	
}
