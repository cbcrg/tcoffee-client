package org.tcoffee.client.exception;

import org.tcoffee.client.data.ErrorData;

/**
 * Raised when the server return a error message 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class ServerResponseException extends ClientException {

	private static final long serialVersionUID = 1L;

	
	public ErrorData error;
	
	public ServerResponseException( String message, Object ... args ) {
		super(String.format(message, args));
	}
	
	public ServerResponseException( Throwable t, String message, Object... args ) { 
		super(String.format(message,args), t);
	}
	
	public ServerResponseException( Throwable t ){ 
		super(t);
	}	
	
	public ServerResponseException( ErrorData error ) { 
		super(error.message);
		this.error = error;
	}
	

}
