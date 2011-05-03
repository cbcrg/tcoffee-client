package org.tcoffee.client.util;

import java.io.File;

import org.apache.commons.lang.StringUtils;

/**
 * Handy class to handle a key-value pair 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class KeyValue {
	
	/** the key */
	public String key;
	
	/** the value */
	public Object value;
	
	/** Default constructor thgat initialize to null */
	public KeyValue() { 
		
	}
	
	/**
	 * Parse the specified string like a pair using the following syntax i.e. <code>&lt;key&gt;=&lt;value&gt;<code>
	 * 
	 * @param pair
	 */
	public static KeyValue parse ( String pair ) { 
		if( StringUtils.isEmpty(pair) ) { 
			return null;
		}
		
		KeyValue result = new KeyValue();
		int p = pair.indexOf("=");
		if( p == -1 ) { 
			result.key = pair;
		}
		else { 
			String v;
			result.key = pair.substring(0,p).trim();
			result.value = (v=pair.substring(p+1).trim()).length()>0 ? v : null;
		}
		
		return result;
	}
	
	/**
	 * The copy constructor 
	 * 
	 * @param that
	 */
	public KeyValue( KeyValue that ) { 
		this.key = that.key;
		this.value = that.value;
	}
	
	/** 
	 * Constructor to initilize the object with the specified values 
	 * 
	 * @param key
	 * @param value
	 */
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
