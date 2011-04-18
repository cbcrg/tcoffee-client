package org.tcoffee.client.util;

import java.io.Serializable;

/**
 * Handle application version number and make it possible to make comparation between numbers 
 * 
 * 
 * @author Paolo Di Tommaso
 * 
 *
 */
public class Version implements Serializable, Comparable<Version> {

	private static final long serialVersionUID = 1L;

	int[] ver;
	
	Long val;
	
	/**
	 * @param version a version number string e.g. <code>1.0.5</code>, <code>2.1</code>
	 */
	public Version(String version) { 
		String[] parts = version.split("\\.");
		
		int len = (parts == null || parts.length==0) ? 0 : parts.length;
		ver = new int[len];
		for( int i=0; i<len; i++ ) { 
			ver[i] = Integer.parseInt(parts[i]);
		}
	}
	

	/**
	 * Compare recursively the version number 
	 * 
	 * @param thatVer
	 * @param index
	 * @return
	 */
	int compare( Version that, int index ) { 

		int len = Math.max(this.ver.length, that.ver.length);
		if( index < this.ver.length && index < that.ver.length ) { 
			int cmp = this.ver[index] - that.ver[index];
			if( cmp == 0 ) { 
				return compare(that, index+1);
			}
			
			return cmp > 0 ? 1 : ( cmp < 0 ) ? -1 : 0  ;
		}
		
		if( this.ver.length == that.ver.length ) { 
			int cmp = that.ver[len-1] - this.ver[len-1];
			return cmp > 0 ? 1 : ( cmp < 0 ) ? -1 : 0  ;
		}
		else if( this.ver.length > that.ver.length ) { 
			return 1;
		}
		else { 
			return -1;
		}
		
	}

	@Override
	public int compareTo(Version that) {
		return compare(that,0);
	}
	
	public boolean lessThen( Version that ) { 
		return compareTo(that)<0;
	}

	public boolean lessThenEquals( Version that ) { 
		return compareTo(that)<=0;
	}
	
	public boolean greaterThan( Version that ) { 
		return compareTo(that)>0;
	}
	
	public boolean greaterThanEquals( Version that ) { 
		return compareTo(that)>=0;
	}

	
	public boolean equals( Object obj ) { 
		if( !(obj instanceof Version) ) return false;
		Version that = (Version)obj;
		return this.compareTo(that)==0;
					
	}
	
	public int hashCode() { 
		return ver != null ? toString().hashCode() : 0;
	}
	
	public String toString() { 
		StringBuilder result = new StringBuilder();
		for( int i=0; ver != null && i < ver.length; i++ ) { 
			if( i>0 ) result.append(".");
			result.append( ver[i] );
		}
		
		return result.toString();
	}
	
}
