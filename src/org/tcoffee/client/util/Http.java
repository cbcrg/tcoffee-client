package org.tcoffee.client.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.tcoffee.client.exception.ClientException;
import org.tcoffee.client.exception.HttpResponseException;
import org.tcoffee.client.exception.XmlResponseException;


/**
 * Wrap the HttpClient providing handy methods
 * 
 * @author Paolo Di Tommaso
 *
 */
public class Http {

	private HttpClient client;
	
	public Http() { 
		client = new DefaultHttpClient();
	}


	/**
	 * Submit a GET request to the specified URL 
	 * 
	 * @param uri the remote resource to which connect 
	 * @return an instance of {@link HttpResponse} 
	 */
	protected HttpResponse get( String uri ) { 
		try { 
			HttpGet get = new HttpGet(uri);
			HttpResponse response = client.execute(get);
			checkNotNull(response, uri);
			return response;
		}
		catch( ClientException e ) { 
			throw e;
		}
		catch( Exception e ) { 
			throw new HttpResponseException(e, "The server returned with an unexpected condition [%s]", uri);
		}
	}
	
	/**
	 * Connect to the specified URL and parse the response as a XML document 
	 * 
	 * @param uri the remote resource to which connect 
	 * @return the returned response as an XML formatted string 
	 */
	public String getXml( String uri ) { 
		try { 
			HttpResponse response = get(uri);
			String xml = parseXML(response);
			return xml;
		}
		catch( ClientException e ) { 
			throw e;
		}
		catch( Exception e ) { 
			throw new XmlResponseException(e,"Unable to fetch XML response [%s]", uri);
		}
	}
	
	/**
	 * Connect to teh specified URI and save the returned content to the specified file 
	 * 
	 * @param uri the remote resource to which connect
	 * @param target the file to which save the returned content 
	 * @return the target file instance 
	 */
	public File getFile( String uri, File target ) { 
		try { 
			HttpResponse response = get(uri);
			checkValid(response, uri);
			
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(target));
			response.getEntity().writeTo(out);
			
			out.close();
			return target;

		}
		catch( ClientException e ) { 
			throw e;
		}
		catch( Exception e ) { 
			throw new ClientException(e, "Unable to download file resource [%s] to '%s'", uri, target);
		}
	}

	
	protected HttpResponse post( String uri, List<KeyValue> pairs ) { 
		try { 
			HttpPost post = new HttpPost(uri);

			/* add all parameters */
			MultipartEntity entity = new MultipartEntity();
			if( pairs != null ) for( KeyValue entry : pairs ) { 
				ContentBody part;
				if( entry.isFile() ) { 
					part = new FileBody( (File)entry.value );
				}
				else { 
					part = new StringBody(entry.getValueAsString());
				}
				entity.addPart(entry.key, part);
				
			}
			post.setEntity(entity);
			/* submit the requets */
			HttpResponse response = client.execute(post);
			checkNotNull(response, uri);
			return response;
		}
		catch( ClientException e ) { 
			throw e;
		}
		catch( Exception e ) { 
			throw new HttpResponseException(e, "Error posting data [%s]", uri);
		}
	}
	
	private void checkNotNull(HttpResponse response, String uri) {
		StatusLine status = response != null ? response.getStatusLine() : null;
		
		if( status == null ) { 
			throw new HttpResponseException();
		}
	}

	/**
	 * Check the HTTP request returned with a non-error code.
	 * <p> 
	 * If the server return an error code the exception {@link HttpResponseException} is raised 
	 * 
	 * @param response a {@link HttpResponse} instance 
	 * @param uri the remote resource returning the reponse 
	 */
	private void checkValid(HttpResponse response, String uri) {
		StatusLine status = response != null ? response.getStatusLine() : null;
		
		if( status == null ) { 
			throw new HttpResponseException();
		}
		else if( status != null && status.getStatusCode() >= 400 ) { 
			throw new HttpResponseException("The server returned an error: %s - %s [%s]", 
					status.getStatusCode(),
					status.getReasonPhrase(),
					uri);
		}
	}
	
	

	/**
	 * Post the list of pair to the specified URI and return the server response as 
	 * an XML fromatted string 
	 * 
	 * @param uri
	 * @param pairs
	 * @return
	 */
	public String postWithXmlResponse( String uri,  List<KeyValue> pairs  ) { 
		try {
			HttpResponse response = post(uri, pairs);
			String xml = parseXML(response);
			return xml;
		} 
		catch( ClientException e ) { 
			throw e;
		}
		catch (Exception e) {
			throw new XmlResponseException(e, "The server respond with an error condition [%s]", uri);
		}
	}
	
	String parseXML( HttpResponse response ) throws IllegalStateException, IOException { 
		String result = IO.readContentAsString(response.getEntity().getContent());
		
		if( result==null || !result.startsWith("")) { 
			throw new XmlResponseException("Response does not seem to be in XML format (it must begins with the '<?xml version=\"1.0\" encoding=\"UTF-8\"?>' declaration): \n%s", result);
		}
		
		return result;
	}


	
}