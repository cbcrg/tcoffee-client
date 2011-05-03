package org.tcoffee.client.cli;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class CommandLineWithFilesTest {

	
	@Test 
	public void testParse() { 
		
		String[] args = new String[] { "--alpha=1", "--beta", "2", "--delta", "-arg=value", "something", "-in=file:/some/sequence.fa" };  
		CommandLineWithFiles cmd = new CommandLineWithFiles();
		cmd.parse(args);
		
		
		assertEquals( 3, cmd.options.size() );
		assertEquals( "1", cmd.options.get("alpha") );
		assertEquals( "2", cmd.options.get("beta") );
		assertTrue( cmd.options.containsKey("delta") );
		assertEquals( null, cmd.options.get("delta") );
		assertFalse( cmd.options.containsKey("arg") );
		
		
		assertEquals( 3, cmd.arguments.size() );
		assertEquals( "-arg=value", cmd.arguments.get(0) );
		assertEquals( "something", cmd.arguments.get(1) );
		assertEquals( "-in=sequence.fa", cmd.arguments.get(2) );
		
		assertEquals( 1, cmd.argFiles.size() );
		assertEquals( new File("/some/sequence.fa"), cmd.argFiles.get(0));
		
		assertEquals( "-arg=value something -in=sequence.fa", cmd.getArgumentsString() );
		
	}
	
	@Test 
	public void testNormalizeFileArg() { 
		Object[] result = CommandLineWithFiles.normalizeFileArgument( "hola" );
		assertEquals( "hola", result[0] );
		assertNull( result[1] );

		result = CommandLineWithFiles.normalizeFileArgument( "-param" );
		assertEquals( "-param", result[0] );
		assertNull( result[1] );
		
		result = CommandLineWithFiles.normalizeFileArgument( null );
		assertNull( result[0] );
		assertNull( result[1] );
		
		result = CommandLineWithFiles.normalizeFileArgument( "file:/some/file.txt" );
		assertEquals( "file.txt", result[0] );
		assertEquals( new File("/some/file.txt"),  result[1] );

		result = CommandLineWithFiles.normalizeFileArgument( "-input=file:/some/file.txt" );
		assertEquals( "-input=file.txt", result[0] );
		assertEquals( new File("/some/file.txt"),  result[1] );
		
		result = CommandLineWithFiles.normalizeFileArgument( "-input file:/some/file.txt" );
		assertEquals( "-input file.txt", result[0] );
		assertEquals( new File("/some/file.txt"),  result[1] );
	
		result = CommandLineWithFiles.normalizeFileArgument( "filefile:/some/file.txt" );
		assertEquals( "filefile:/some/file.txt", result[0] );
		assertNull( result[1] );

	
	}
}
