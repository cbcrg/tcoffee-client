package org.tcoffee.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.tcoffee.client.util.IO;
import org.tcoffee.client.util.Sys;
import org.tcoffee.client.util.Version;

/**
 * Check if a newer version is available and download it 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class ClientUpdater {

	public String remotePath;
	
	public Version currentVersion;
	
	public String targetPath;
	
	public String targetFile;
	
	
	/*
	 * common initialization 
	 */
	{
		remotePath = "http://tcoffee.org/Packages/Client";
		currentVersion = Sys.appver;
		targetPath = new File(".").getAbsolutePath();
		targetFile = "c-coffee.jar";
		
	}

	/**
	 * 1. Connect to the remote path and check the content of file name '.version'
	 * 2. if the current version is different from the remote one (2) otherwise exit
	 * 3. download the new client library 
	 * 4. 
	 */
	public void update()  { 
		Sys.println("Check if exists a never version exists");
		
		if( !remotePath.endsWith("/") ) { 
			remotePath += "/";
		}
 		
		try {
			/* 
			 * check the remote version
			 */
			URL versionFile = new URL( remotePath + ".version");
			String sVersion = IO.readContentAsString(versionFile.openStream()).trim();
			if( StringUtils.isEmpty(sVersion) ) { 
				Sys.debug("Version file not available");
				return;
			}
			Version remoteVersion = new Version(sVersion);
			
			if( !remoteVersion.greaterThan(currentVersion)) { 
				Sys.debug("Current version: '%s' is up-to-date. Remote version: '%s'", currentVersion, remoteVersion);
				return;
			}
			Sys.println("Version '%s' available for donwload", remoteVersion);
			
			/* 
			 * try to download the updated version 
			 */
			File download = File.createTempFile("updater-", ".tmp");
			
			FileOutputStream save = new FileOutputStream (download);
			URL clientFile = new URL( remotePath + targetFile );
			Sys.println("Downloading update [%s]", clientFile);
			IO.write(clientFile.openStream(), save);
			save.close();
			
			/* 
			 * replace current lib with the downloaded one
			 */
			File target = new File(targetPath, targetFile + ".new");
			if( target.exists() ) target.delete();
			FileUtils.moveFile(download, target);
			Sys.println("Your client has been update to version: %s.\nIt will take effect on next time you will invoke it.", remoteVersion);
		}
		catch( IOException e ) { 
			Sys.println("Cannot complete client update. Reported error: " + e.getMessage());
			Sys.debug(e);
		}
	}


}
