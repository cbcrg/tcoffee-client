package org.tcoffee.client.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("service")
public class ServiceData {

	public String name;
	
	public String group;
	
	public String title;
	
	public String description;
	
	public String cite;


	@Override
	public String toString() {
		return "Service [name=" + name + ", title=" + title + "]";
	}
	
	
}
