package org.tcoffee.client.exception;

public class XmlResponseException extends ClientException {

	public XmlResponseException( String message, Object ... args ) {
		super(message, args);
	}
	
	public XmlResponseException( Throwable t, String message, Object ... args ) { 
		super(t, message, args);
	}
	
}
