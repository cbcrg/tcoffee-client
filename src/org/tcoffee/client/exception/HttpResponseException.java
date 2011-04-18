package org.tcoffee.client.exception;

import org.apache.http.StatusLine;

public class HttpResponseException extends ClientException {

	private static final long serialVersionUID = 1L;

	
	public HttpResponseException() { 
		super("The server return with an unknown error");
	}
	
	public HttpResponseException( String message, Object ... args ) { 
		super(message, args);
	}
	
	public HttpResponseException( Throwable e, String message, Object... args ) { 
		super(e, message, args);
	}
	
	public HttpResponseException( StatusLine status ) { 
		super( "The server return an error: %s - %s", status.getStatusCode(), status.getReasonPhrase() ); 
	}
	
}
