package org.tcoffee.client;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.tcoffee.client.util.IO;

public class TClientTest {

	
	@Test
	public void testNormalizeFile() throws IOException { 
		
		assertEquals( null, TClient.normalizeFiles(null) );
		assertEquals( "", TClient.normalizeFiles("") );
		assertEquals( new Long(1), TClient.normalizeFiles(new Long(1)) );
		assertEquals( "hola", TClient.normalizeFiles("hola") );

		File file = new File("./testNormalizeFile");
		if( file.exists() ) file.delete();
		IO.writeContent("Hola", file);
		
		/* read the content of the specified file */
		assertEquals( "Hola", TClient.normalizeFiles("file:./testNormalizeFile"));
		
		file.delete();
	}
	
}
