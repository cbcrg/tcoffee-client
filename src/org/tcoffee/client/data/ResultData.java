package org.tcoffee.client.data;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("result")
public class ResultData {

	public String bundle;
	
	public String service;
	
	public String title;
	
	public String status;
	
	public String cmdline;
	
	@XStreamAlias("elapsed-time")
	public long elapsedTime;
	
	@XStreamImplicit(itemFieldName="item")
	public List<ResultItemData> items;
	
	
	public boolean isStatusRUNNING() { 
		return "Running".equals(status);
	}
	
	public boolean isStatusDONE() {
		return "Done".equals(status);
	} 
	
	public boolean isStatusFAILED() {
		return "Failed".equals(status);
	} 
	
}
