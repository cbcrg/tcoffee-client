package org.tcoffee.client.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("submit")
public class SubmitData {

	/**
	 * The request unique identifier assigned by the server
	 */
	@XStreamAlias("request-id")
	public String requestId;
	
	/**
	 * The request status 
	 */
	public String status;

	
	/**
	 * The job reference url 
	 */
	public String url;
}
