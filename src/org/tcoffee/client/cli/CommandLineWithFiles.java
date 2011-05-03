package org.tcoffee.client.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.tcoffee.client.exception.ClientException;

/**
 * Special version that will fetch all argument that use a 'file:' prefix 
 * <p>
 * For example: 
 * <pre>
 * command -input=file:/some/path/file.txt 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class CommandLineWithFiles extends CommandLine {

	List<File> argFiles = new ArrayList<File>();
	

	@Override
	protected void parseArgument(String arg) {
		Object[] entry = normalizeFileArgument(arg); 
		arguments.add((String)entry[0]);
		if( entry[1] instanceof File  ) { 
			argFiles.add( (File)entry[1] );
		}

	}
	
	
	
	public List<File> getArgumentsFiles() { 
		checkFilesExist();
		return argFiles;
	}
	
	public void checkFilesExist() { 
		for( File file : argFiles ) { 
			if( !file.exists() ) { 
				throw new ClientException("Specified file as command argument does not exist: %s", file);
			}
		}
	}
	
	
	/**
	 * Given an argument containing a file specification, remove the file prefix from the argument 
	 * and return a file object 
	 * @param arg
	 * @return
	 */
	static Object[] normalizeFileArgument( String arg ) { 
		Object[] defResult = new Object[] { arg, null };
		if( arg == null ) { 
			return defResult;
		}
		
		int p = arg.indexOf("file:");
		if( p == -1 ) { 
			return defResult; 
		}
		
		/* 
		 * Check for the following format 
		 * file:/some/file.txt 
		 */
		if( p == 0 ) { 
			String sFileName = arg.substring(5);
			File file = new File(sFileName);
			return new Object[] { file.getName(), file };
		}	
		
		/*
		 * Check for the following format 
		 * -param=file:/some/file.txt
		 */
		if( p > 0 &&  isSeparator(arg.charAt(p-1))  ) { 
			String sFileName = arg.substring(p+5);
			File file = new File(sFileName);
			arg = arg.substring(0,p) + file.getName();
			return new Object[] { arg, file };   
		}
		
		return defResult;   
	}	
	
	
	static boolean isSeparator( char ch ) { 
		String reserved = "/\\?%*|\"<>.:=+-[] ";
		return reserved.indexOf(ch) != -1;
	}
	
	
	
}
