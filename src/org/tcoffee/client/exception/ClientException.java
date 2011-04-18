package org.tcoffee.client.exception;

/**
 * Base class for client exception 
 * @author ptommaso
 *
 */
public class ClientException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ClientException( String message, Object ... args ) {
		super(String.format(message, args));
	}
	
	public ClientException( Throwable t, String message, Object... args ) { 
		super(String.format(message,args), t);
	}
	
	public ClientException( Throwable t ){ 
		super(t);
	}

}
