package org.tcoffee.client;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tcoffee.client.data.ResponseData;
import org.tcoffee.client.data.ResultData;
import org.tcoffee.client.data.ResultItemData;
import org.tcoffee.client.data.ServiceData;
import org.tcoffee.client.data.SubmitData;
import org.tcoffee.client.exception.ClientException;
import org.tcoffee.client.exception.ServerResponseException;
import org.tcoffee.client.exception.WaitResultTimeout;
import org.tcoffee.client.exception.XmlResponseException;
import org.tcoffee.client.util.Http;
import org.tcoffee.client.util.IO;
import org.tcoffee.client.util.KeyValue;
import org.tcoffee.client.util.Sys;
import org.tcoffee.client.util.XML;


/**
 * T-Coffee remote client. Connect to a T-Coffee remote server, upload data, execute the alignment and returns 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class TCoffeeClient {
	
	private static Logger log = LoggerFactory.getLogger(TCoffeeClient.class);
	
	private int pollSleepSecs = 5;
	
	private int pollTimeoutSecs = 60;

	/* the downloaded result file */
	private File resultLogFile;
	
	/* wrap the http connection client */
	Http http;
	
	/** 
	 * The remote host name, it could include the port number using the classic ':' notation e.g. <code>localhost:9000 </code>
	 */
	private String host;
	
	private File outpath;
	
	/**
	 * The remote bundle providing the services 
	 */
	private String bundle;

	private SubmitData submit;

	private ResultData result;
	
	
	/**
	 * The client constructor, requires the remote host and bundle information 
	 * 
	 * @param host
	 * @param bundle
	 */
	public TCoffeeClient(String host, String bundle) { 
		this.host = host;
		this.bundle = bundle;
		this.http = new Http();
	}
	
	String urlFor( String verb, KeyValue... params ) { 
		StringBuilder url = new StringBuilder();
		url.append("http://") .append(host) .append("/api/") .append(bundle) .append("/");
		
		if( verb != null ) { 
			url.append(verb);
		}
		
		if( params != null && params.length>0 ) { 
			url.append("?");
			
			for( int i=0; i<params.length; i++ ) { 
				KeyValue entry = params[i];
				
				if( i>0 ) { 
					url.append("&");
				}
				
				url.append(entry.key);
				url.append("=");
				if( entry.value != null ) { 
					try {
						url.append( URLEncoder.encode(entry.value.toString(), "utf-8") );
					} catch (UnsupportedEncodingException e) {
						throw new ClientException("Unable to encode parameter %s", entry );
					} 
				}
			}
		}

		return url.toString();
	}
	
	/**
	 * Execute the alignment. In details:
	 *  
	 * 1) Connect to remote host 
	 * 2) Upload data and submit alingment 
	 * 4) Wait for a response 
	 * 5) Download result alignment files 
	 * 
	 */
	public void run( String cmdline, List<File> files ) { 

		try { 
			String uri = urlFor("run");

			/*
			 * 1.
			 * the requests is composed by a parameter named 'args'
			 * containing the target command command line, plus 
			 * all the files entered named in the form 'file:n' 
			 * where n is the file index 
			 */
			List<KeyValue> params = new ArrayList<KeyValue>();
			params.add( new KeyValue("args", cmdline) ); 
			
			int i=0;
			if( files != null ) for( File file : files ) { 
				params.add( new KeyValue("file:" + i++, file ) );
			}

			
			/*
			 * 2. submit the request and get the result status 
			 */
			Sys.print("Sending request...");
			this.submit = submitAlignment(uri, params);
			Sys.print("\rRequest acquired with ID: %s\n", submit.requestId);
			
			/* 
			 * 3. wait for the result
			 */
			Sys.print("Waiting result...");
			this.result = waitForResult(submit.requestId);
			Sys.print("\r");
			
			/*
			 * 4. download result
			 */
			Sys.print("Downloading result...");
			downloadResultItems(result);
			Sys.print("\r");
			
		}
		catch( ClientException e ) { 
			throw e;
		}
		catch( Exception e ) { 
			throw new ClientException(e, "Error on submitting the following command: '%s'", cmdline);
		}
	}

	/**
	 * @return the command result log text content 
	 */
	public String getResultLog() { 
		if( resultLogFile != null && resultLogFile.exists() ) { 
			try {
				return IO.readContentAsString(resultLogFile);
			} catch (IOException e) {
				throw new ClientException(e, "Unable to read result file: %s", resultLogFile);
			}
		}
		
		return "";
	}
	
	
	/**
	 * Submit the alignment request to the server and returns the {@link SubmitData} job information
	 * 
	 * @param uri
	 * @param pairs
	 * @return
	 */
	SubmitData submitAlignment(String uri, List<KeyValue> pairs) {

		try { 
			String xml = http.postWithXmlResponse(uri,pairs);
			ResponseData result = XML.fromXML(xml);
			checkResponse(result);
			return result.submit;
		}
		catch( ClientException e ) { 
			throw e;
		}
		catch( Exception e ) { 
			throw new ClientException(e, "Unable to submit request [%s]", uri); 
		}
	}
	
	
	/**
	 * Query the remote host for the XML result status of the specified request 
	 * 
	 * @param requestId the alignment request unique identifier 
	 * @return the alignment result status XML string
	 */
	ResultData getResultFor( String requestId ) { 

		String url = urlFor( "result", new KeyValue("rid", requestId));
		ResultData result = getData(url).result;
		return result;
		
	}

	/**
	 * Poll the remote server until the submitted request has been processed 
	 * 
	 * @param requestId the request unique indentifier 
	 * @return
	 */
	ResultData waitForResult( String requestId ) {

		ResultData result = null;
		
		long now, begin = System.currentTimeMillis();
		do { 
			
			result = getResultFor(requestId);
			if( !result.isStatusRUNNING() ) { 
				break;
			}
			
			/* 
			 * check for global timeout 
			 */
			now = System.currentTimeMillis();
			if( (now-begin) > pollTimeoutSecs*1000 ) { 
				throw new WaitResultTimeout("The submitted request does not complete in the expected time");
			}

			/* 
			 * wait before another interation 
			 */
			try {
				Thread.sleep( pollSleepSecs * 1000 );
			} catch (InterruptedException e) {
				log.warn("Result polling interruped");
			}

			
		}
		while( true );
		
		return result; 
	}


	
	void downloadResultItems( ResultData result ) { 
		if( result == null || result.items == null ) { 
			return;
		}

		String base = host;
		if( base.indexOf("/") != -1 ) { 
			base = base.substring( 0, base.indexOf("/") );
		}
		
		base = "http://" + base;
		
		for( ResultItemData item : result.items ) { 
			if( "input_file".equals(item.type) ) { 
				// do not download the input file(s)
				continue;
			}
			
			String path = item.webpath;
			String uri = base + path;
			File target = outpath != null
						? new File(outpath, item.name) 
						: new File(item.name);
			
			http.getFile( uri, target );
			
			/* detect the T-coffee log file */
			if( "system_file".equals(item.type) && "log".equals(item.format) && target.exists()) { 
				resultLogFile = target;
			}
		}
	}
	

	/**
	 * Return the list of available services 
	 * @return
	 */
	public List<ServiceData> getServices() {
		return getData( urlFor("services") ).services;
	}
	
	public void ping() { 
		long begin = System.currentTimeMillis(); 
		String status = getData(urlFor("ping")).status;
		long end = System.currentTimeMillis(); ;
		
		Sys.print(">ping: %s - elapsed time: %s ms", status, (end-begin) );
		
	}
	
	ResponseData getData( String url ) { 
		String xml="(none)";
		try {
			xml = http.getXml(url);
			ResponseData response = XML.fromXML(xml);
			checkResponse(response);
			return response;
		}
		catch( ClientException e ) { 
			throw e;
		}
		catch( Exception e ) { 
			throw new XmlResponseException(e,"Invalid XML server response:\n%s", xml);
		}
		
	}
	
	/**
	 * Check the response object, if contains an error object will raise an exception 
	 * 
	 * @param reponse
	 */
	void checkResponse( ResponseData response ) { 
		if( response == null ) { 
			throw new ClientException("Missing response");
		}
		
		if( response.err != null ) { 
			throw new ServerResponseException(response.err);
		}
		
	}
	
	/**
	 * Setter for polling sleep time 
	 * 
	 * @param pollDelaySecs
	 */
	public void setPollSleepSecs(int pollDelaySecs) {
		this.pollSleepSecs = pollDelaySecs;
	}

	/**
	 * Setter for polling timeout 
	 * 
	 * @param pollTimeoutSecs
	 */
	public void setPollTimeoutSecs(int pollTimeoutSecs) {
		this.pollTimeoutSecs = pollTimeoutSecs;
	}
	
	/**
	 * Defines the path where save the files returned by the server
	 * 
	 * @param file a valid file system directory, if not exist it will be created 
	 */
	public void setOutputPath( File file ) { 
		if( file != null ) { 
			if( !file.exists() && !file.mkdirs()) { 
				throw new ClientException("The provided output path cannot be created: %s", file);
			}
			if( !file.isDirectory() ) { 
				throw new ClientException("The provided output path is not a directory: %s", file);
			}
 			
		}
		this.outpath = file;
	}

	/**
	 * The URL on the T-coffee web site showing the information about the submitted job 
	 */
	public String getRequestUrl() {
		return submit != null ? submit.url : null;
	}

	/**
	 * The request unique identifier 
	 */
	public String getRequestId() { 
		return submit != null ? submit.requestId : null;
	}

	public ResultData getResult() { 
		return this.result;
	}
}
