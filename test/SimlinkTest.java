import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;


public class SimlinkTest {

	@Test 
	public void testExists() throws IOException { 
		File file = new File( "/Users/ptommaso/d", "cloud-coffee.jar" );
		assertTrue( file.exists() );

		File link = new File( "/Users/ptommaso/../ptommaso/d", "cc.jar" );
		assertTrue( link.exists() );
		
		
		System.out.println(link.getAbsolutePath());
		System.out.println(link.getCanonicalPath());
	
		System.out.println(FilenameUtils.normalize(link.getAbsolutePath()));
	}
}
