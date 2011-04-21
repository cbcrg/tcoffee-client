package org.tcoffee.client;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.tcoffee.client.data.ErrorData;
import org.tcoffee.client.data.ResultData;
import org.tcoffee.client.data.ResultItemData;
import org.tcoffee.client.data.ServiceData;
import org.tcoffee.client.data.SubmitData;
import org.tcoffee.client.exception.ServerResponseException;
import org.tcoffee.client.util.Http;
import org.tcoffee.client.util.IO;
import org.tcoffee.client.util.KeyValue;

public class TCoffeeClientTest {

	private TCoffeeClient client;

	@Before
	public void before() { 
		client = new TCoffeeClient("localhost", "bundle");
	}
	
	@Test
	public void testGetUrl() { 
		
		assertEquals( "http://localhost/api/bundle/", client.urlFor(null) );

		assertEquals( "http://localhost/api/bundle/hola", client.urlFor("hola") );
		assertEquals( "http://localhost/api/bundle/more/level", client.urlFor("more/level") );
	
		assertEquals( "http://localhost/api/bundle/verb?x=1", client.urlFor("verb", new KeyValue("x",1)) );
		assertEquals( "http://localhost/api/bundle/verb?x=1&y=a+b", client.urlFor("verb", new KeyValue("x",1), new KeyValue("y", "a b")) );

	}
	
	@Test
	public void testSubmitRequest() throws Exception { 
		/* 
		 * mock the http server Response
		 */
		final String xml = 
			"<response>" +
			"<submit>" +
			"<request-id>999</request-id>" +
			"<status>Running</status>" +
			"<url><![CDATA[/apps/tcoffee/999.html]]></url>" + 
			"</submit>" +
			"</response>";
		
		
		client.http = new Http() {
			@Override
			protected HttpResponse post(String uri, List<KeyValue> pairs) {
				ProtocolVersion ver = new ProtocolVersion("HTTP", 1, 1);
				StatusLine status = new BasicStatusLine( ver, 200, "OK");
				HttpResponse response = new BasicHttpResponse(status);

				BasicHttpEntity entity = new BasicHttpEntity();
				entity.setContent( new ByteArrayInputStream(xml.getBytes()) );
				response.setEntity(entity);
				return response;
			};

		}; 
		
		/* 
		 * submit the request 
		 */
		SubmitData submit = client.submitAlignment("somewhere", new ArrayList<KeyValue>());
		
		assertNotNull(submit);
		assertEquals("999", submit.requestId);
		assertEquals("Running", submit.status );
		assertEquals("/apps/tcoffee/999.html", submit.url);
	}
	
	@Test 
	public void testSubmitWithError() { 
		
		/* 
		 * mock the http server Response
		 */
		final String xml = 
			"<response>" +
			"<err>" +
			"<code>400</code>" +
			"<type>Bad request</type>" +
			"<message>You cannot do that!</message>" +
			"</err>" +
			"</response>";
		
		
		client.http = new Http() {
			@Override
			protected HttpResponse post(String uri, List<KeyValue> pairs) {
				ProtocolVersion ver = new ProtocolVersion("HTTP", 1, 1);
				StatusLine status = new BasicStatusLine( ver, 400, "BAD_REQUEST");
				HttpResponse response = new BasicHttpResponse(status);

				BasicHttpEntity entity = new BasicHttpEntity();
				entity.setContent( new ByteArrayInputStream(xml.getBytes()) );
				response.setEntity(entity);
				return response;
			};

		}; 
		
		/* 
		 * submit the request 
		 */
		try { 
			client.submitAlignment("somewhere", new ArrayList<KeyValue>());
			fail();
		}
		catch( ServerResponseException e ) { 
			ErrorData error = e.error;
			assertNotNull(error);
			assertEquals("400", error.code);
			assertEquals("Bad request", error.type);		
			assertEquals("You cannot do that!", error.message);		
		}
		
	}
	
	
	
	/*
	 * improve this test 
	 */
	@Test 
	public void testWaitForResult() { 
	
		/*
		 * Mock the response result
		 */
		final String resultXml = 
			"<response>" +
			"<result>" +
			"<elapsed-time>1145637</elapsed-time>" + 
			"<status>DONE</status>" + 
			"<bundle>tcoffee</bundle>" + 
			"<service>psicoffee</service>" + 
			"<title>PSI-Coffee</title>" + 
			"<cmdline>t_coffee -in=file.fasta -mode=mcoffee</cmdline>" + 

			"<item>"  +
				"<webpath>/data/c9127281/1vie.prf</webpath>" + 
				"<label>prf file</label>" + 
				"<type>Template Profile</type>" + 
				"<name>1vie.prf</name>" + 
				"<format>fasta_aln</format>" + 
			"</item>" + 
			"<item>" + 
				"<webpath>/data/c9127281/1ycsB.prf</webpath>" + 
				"<label>cmd file</label>" + 
				"<type>File Profile</type>" + 
				"<name>1ycsB.prf</name>" + 
				"<format>clustal_aln</format>" + 
			"</item>" + 			
			
			"</result>" +
			"</response>";		
		
		client.http = new Http() {
			@Override
			public String getXml(String path) {
				return resultXml;
			} };
		
		ResultData result = client.waitForResult("xxx");
		assertNotNull(result);
		assertEquals( "DONE", result.status );
		assertEquals( 1145637, result.elapsedTime );
		assertEquals( "tcoffee", result.bundle );
		assertEquals( "psicoffee", result.service );
		assertEquals( "PSI-Coffee", result.title );
		assertEquals( "t_coffee -in=file.fasta -mode=mcoffee", result.cmdline );

		List<ResultItemData> items = result.items;
		assertEquals( 2, items.size() );

		assertEquals( "/data/c9127281/1vie.prf", 	items.get(0).webpath );
		assertEquals( "prf file", 					items.get(0).label );
		assertEquals( "Template Profile", 			items.get(0).type );
		assertEquals( "1vie.prf", 					items.get(0).name );
		assertEquals( "fasta_aln", 					items.get(0).format );

		assertEquals( "/data/c9127281/1ycsB.prf", 	items.get(1).webpath );
		assertEquals( "cmd file", 					items.get(1).label );
		assertEquals( "File Profile", 				items.get(1).type );
		assertEquals( "1ycsB.prf", 					items.get(1).name );
		assertEquals( "clustal_aln", 				items.get(1).format );
		
	}
	
	@Test 
	public void testDownloadResultItems() throws IOException { 
		File FILE1 = new File("file1.txt");
		File FILE2 = new File("file2.txt");

		FILE1.delete();
		FILE2.delete();

		/*
		 * mock the client to test it 
		 */
		client.http = new Http() {
			
			@Override
			public File getFile(String uri, File target) {
				
				String content = "xxx";
				if( uri.endsWith("/path/to/file1.txt")) {
					content = "alpha";
				}
				else if( uri.endsWith("/path/to/file2.txt")) {
					content = "beta";
				}
				
				try {
					IO.writeContent(content, target);
					return target;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				
			} }; 
		
		ResultItemData item1 = new ResultItemData();
		item1.webpath = "/path/to/file1.txt";
		item1.name = "file1.txt";
		
		ResultItemData item2 = new ResultItemData();
		item2.webpath = "/path/to/file2.txt";
		item2.name = "file2.txt";
		
		ResultData result = new ResultData();
		result.items = new ArrayList<ResultItemData>();
		result.items.add(item1);
		result.items.add(item2);
		
		client.downloadResultItems(result);
		
		assertTrue( FILE1.exists() );	
		assertEquals( "alpha", IO.readContentAsString(FILE1).trim() );
		
		assertTrue( FILE2.exists() );	
		assertEquals( "beta", IO.readContentAsString(FILE2).trim() );
		
		FILE1.delete();
		FILE2.delete();
	}
	

	
	@Test
	public void testGetService() { 
		final String xmlResponse = 
			"<response>" + 
				"<service >" + 
				"<name>regular</name>" +
				"<group>The group</group> " + 
				"<title>Short name</title> " + 
				"<description>The Description</description> " + 
				"</service>" + 
				
				"<service>" + 
				"<name>expresso</name>" +
				"<group>Expresso group</group> " + 
				"<title>Expresso title</title> " + 
				"<description>Expresso Description</description> " + 
				"</service>" + 

			"</response>";
		
		
		client.http = new Http() {
			public String getXml(String uri) {
				return xmlResponse;
			} };
		
		

		List<ServiceData> items = client.getServices();
		assertNotNull( items );
		assertEquals( 2, items.size() );
		
		assertEquals( "regular", items.get(0).name );
		assertEquals( "Short name", items.get(0).title );
		assertEquals( "The Description", items.get(0).description );
		assertEquals( "The group", items.get(0).group );

		assertEquals( "expresso", items.get(1).name );
		assertEquals( "Expresso title", items.get(1).title );
		assertEquals( "Expresso Description", items.get(1).description );
		assertEquals( "Expresso group", items.get(1).group );
		
	}
	
	@Test 
	public void testPing() { 
		final String xml = 
			"<response>" +
			"<status>OK</status>" +
			"</response>";
		
		client.http = new Http() {
			@Override
			public String getXml(String uri) {
				return xml;
			} };
			
		client.ping();
		
	}
}
