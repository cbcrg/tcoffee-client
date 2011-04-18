package org.tcoffee.client.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("err")
public class ErrorData {

	public String code;
	
	public String message;
	
	public String type;
	
}
