package org.tcoffee.client;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class UpdaterTest {

	@Test 
	public void testUpdater() { 
		ClientUpdater updater = new ClientUpdater();
		updater.remotePath = "file:./test-updater/source";
		updater.targetPath = "./test-updater/target";
		updater.targetFile = "client-lib.txt";
		
		File target = new File(updater.targetPath, updater.targetFile);
		if( target.exists() ) target.delete();
		
		updater.update();
		assertTrue( target.exists() );
		
		
	}
}
