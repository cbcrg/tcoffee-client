package org.tcoffee.client.cli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.tcoffee.client.util.KeyValue;
import org.tcoffee.client.util.Sys;

/**
 * <p>
 * Command line parser. It distinguish between <code>--options</code> (with two hyphens) 
 * and <code>-argument</code> (with ONE hyphen). 
 * </p>
 * 
 * <p>
 * The first define the client configuration propeties, while the latter are considered the remote T-Coffee 
 * application arguments 
 * </p>
 * 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class CommandLine {

	static public class Option { 
		public String name;
		public String description;
		public boolean hasValue;
		public String defValue;
		public String sample;
		public boolean required;

		@Override
		public String toString() {
			return "Option [name=" + name + ", description=" + description + ", required=" + required + "]";
		}

		@Override
		public int hashCode() {
			return name != null ? name.hashCode() : 0;
		}

		@Override
		public boolean equals(Object obj) {
			Option that = (Option) obj;
			return this.name == that.name || (this.name != null && this.name.equals(that.name));
		}
		
		
	}
	
	Map<String,Option> declaredOptions = new TreeMap<String, Option>();
	
	/**
	 * Options to the client application 
	 */
	Map<String,String> options = new HashMap<String, String>();
	
	/** 
	 * The list of argument for the program to be executed remotly 
	 */
	List<String> arguments = new ArrayList<String>();
	
	private String[] args;

	private int index;
	private int length;
	
	private int maxOptionLength = 0;
	
	public CommandLine() { 
	}
	
	public CommandLine addOption( String name, String description ) { 
		addOption(name,description,null,null,false);
		return this;
	}
	
	public CommandLine addOption( String name, String description, String sample, String defValue, boolean required ) { 
		if( StringUtils.isEmpty(name) ) { 
			return this;
		}
		
		Option opt = new Option();
		opt.name = name;
		opt.description = description;
		opt.defValue = defValue;
		opt.hasValue = StringUtils.isNotEmpty(defValue);
		opt.sample = sample;
		opt.required = required;
		declaredOptions.put(name, opt);
		if( name.length()>maxOptionLength ) { 
			maxOptionLength = name.length();
		}
		
		return this;
		
	}
	
	
	public boolean isEmpty() { 
		return options.size() == 0 && arguments.size() == 0;
	}
	
	public boolean hasOption(String name) { 
		if( options == null ) { 
			return false;
		}
		
		if( !options.containsKey(name)) { 
			return false;
		}
		
		Option opt = declaredOptions.get(name);

		return opt != null && ( !opt.hasValue || (opt.hasValue && StringUtils.isNotEmpty(options.get(name))) );
	}
	
	public String getOption( String name ) { 
		return options != null ? options.get(name) : null;
	}
	
	public String getDefault( String name ) { 
		Option opt = declaredOptions != null ? declaredOptions.get(name) : null;
		return opt != null ? opt.defValue : null;
	}
	
	
	KeyValue parsePair( final String arg ) { 

		KeyValue result = new KeyValue();
		
		// check if contains a '=' char
		int p = arg.indexOf("=");
		if( p!=-1) { 
			// split the argument in name/value pair
			result.key = arg.substring(0,p);
			result.value = arg.substring(p+1);
		}
		else { 
			result.key = arg;
			// try to check if the following one is the value for this options
			if( index+1 < length && !args[index+1].startsWith("-")) { 
				result.value = args[index+1];
				index++;
			}

		}

		return result;
	}
	
	/**
	 * Parse the command line arguments 
	 * 
	 * @param args
	 */
	public void parse(String[] args) { 
		if( args == null ) return;
		
		this.args = args;
		this.index = 0;
		this.length = args.length;
	
		
		while( index < length ) { 
			String arg = args[index];
			/*
			 * Check if it is an option (two hyphen)
			 */
			if( arg.startsWith("--")) { 
				// remove the double hyphens
				KeyValue pair = parsePair( arg.substring(2) );
				options.put(pair.key, (String)pair.value);
 			}

			/*
			 * everything else will be a argument for the remote command 
			 */
			else {
				parseArgument(arg);
			}

			index++;
		}
	}

	protected void parseArgument(String arg) {
		arguments.add(arg);
	}

	public String usage() {
		StringBuilder result = new StringBuilder();
			
		result.append("Usage: ").append(Sys.appbin) .append (" [options] <arguments> \n");
			
		if( declaredOptions.size() > 0 ) { 
			result.append("Available options: \n");
			for( String name: declaredOptions.keySet() ) { 
				Option opt = declaredOptions.get(name);

				String fmt = "%-" + maxOptionLength + "s";
				result.append( "--" ) .append( String.format(fmt, name) );
				
				if( opt.hasValue == true ) { 
					result.append("=<");
					result.append( StringUtils.isNotEmpty(opt.sample) ? opt.sample : "value");
					result.append(">");

				}
				
				if( opt.description != null ) { 
					result.append(": ") .append(opt.description);
					if( StringUtils.isNotEmpty(opt.defValue) ) { 
						result.append(" [") .append(opt.defValue) . append("]");
					}
				}
				
				result.append("\n");
			}
			
		}
		
		result.append("Arguments:\n" +
				"  You can (almost) any T-Coffee argument. \n" +
				"  Note: file names must be prepended by the 'file:' prefix (w/o quote)\n");

		result 
			.append("Example: \n" +
					"  c-coffee --out-path=/some/path -in=file:sequences.fa -mode=expresso");
		
		return result.toString();
	}

	/**
	 * 
	 * @return all the command arguments as a string 
	 */
	public String getArgumentsString() { 
		StringBuilder result = new StringBuilder();
		int c=0; 
		for( String arg : arguments ) { 
			if( c++ > 0 ) result.append(" ");
			result.append( arg );
		}

		return result.toString();
	}
	
	public List<String> getArguments() { 
		return new ArrayList<String>(this.arguments);
	}
	
	public Map<String,Option> getDeclaredOptions() { 
		return declaredOptions;
	}
	

}
