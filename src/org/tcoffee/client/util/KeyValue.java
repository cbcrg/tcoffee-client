package org.tcoffee.client.util;

import java.io.File;

public class KeyValue {

	public String key;
	public Object value;
	
	public KeyValue() { 
		
	}
	
	
	public KeyValue( KeyValue that ) { 
		this.key = that.key;
		this.value = that.value;
	}
	
	public KeyValue( String key, Object value ) { 
		this.key = key;
		this.value = value;
	}
	
	public String toString() { 
		return key + "=" + value;
	}

	public String getValueAsString() { 
		return value != null ? value.toString() : null;
	}
	
	
	public boolean isFile() { 
		return value instanceof File;
	}
}
