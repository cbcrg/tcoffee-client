package org.tcoffee.client.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tcoffee.client.data.ErrorData;
import org.tcoffee.client.data.ResponseData;
import org.tcoffee.client.data.ResultData;
import org.tcoffee.client.data.ResultItemData;
import org.tcoffee.client.data.ServiceData;
import org.tcoffee.client.data.SubmitData;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * XML helper class 
 * 
 * @author Paolo Di Tommaso
 *
 */
public class XML {

	private static final Logger log = LoggerFactory.getLogger(XML.class);
	

	private static final XStream xstream;
	
	static {
		/* create an xstream instance */
		xstream = new XStream(new DomDriver());
		xstreamRegisterAliases();	
	}
	
	static void xstreamRegisterConverters() { 
		/* register converters */
		log.debug("XStream registering converters on package: 'convertes'");
		Class[] classes = getClasses("converters");
		for( Class clazz : classes ) {
			if( Modifier.isAbstract(clazz.getModifiers()) ) { continue; }
			if( Modifier.isInterface(clazz.getModifiers()) ) { continue; }
			
			try {
				if( Converter.class.isAssignableFrom(clazz) ) { 
					log.debug(String.format("Registering XStream converter: %s", clazz)); 
					xstream.registerConverter((Converter)clazz.newInstance());
				}
				else if( SingleValueConverter.class.isAssignableFrom(clazz) ) {
					log.debug(String.format("Registering XStream converter: %s", clazz)); 
					xstream.registerConverter((SingleValueConverter)clazz.newInstance());
				}
				else {
					log.warn(String.format("Unknown converter class: %s", clazz));
				}
				
			} catch (Exception e) {
				throw new RuntimeException(String.format("Unable to instantiate converter: %s", clazz), e); 
			}
		}
		
	}
	
	static void xstreamRegisterAliases() { 
		/* process models for annotation */
		xstream.processAnnotations(ErrorData.class);
		xstream.processAnnotations(ResponseData.class);
		xstream.processAnnotations(ResultData.class);
		xstream.processAnnotations(ResultItemData.class);
		xstream.processAnnotations(ServiceData.class);
		xstream.processAnnotations(SubmitData.class);

	}
	
	
    public static Class[] getClasses(String packageName) {
    	
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources;
		try {
			resources = XML.class.getClassLoader().getResources(path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
		    URL resource = resources.nextElement();
			log.debug(String.format("url: %s", resource));
		    dirs.add(new File(resource.getFile()));
		}
		
		ArrayList<Class> classes = new ArrayList<Class>();
		for (File directory : dirs) {
		    try {
				classes.addAll(findClasses(directory, packageName));
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return classes.toArray(new Class[classes.size()]);
	}
    
    /* 
     * be aware this method will not work if the application is package inside a jar file 
     */
    @Deprecated
    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        
        File[] files = directory.listFiles();
        final String SUFFIX = ".class";
        
        for (File file : files) {
            if (file.isDirectory()) {
            	if( !file.getName().contains(".") ) {
            		classes.addAll(findClasses(file, packageName + "." + file.getName()));
            	}
            } 
            else if (file.getName().endsWith(SUFFIX)) {
            	String clazzName = packageName + '.' + file.getName().substring(0, file.getName().length() - SUFFIX.length());
                classes.add(Class.forName(clazzName));
            }
        }
        return classes;
    }   
	
    
	@SuppressWarnings("unchecked")
	public static <T> T fromXML(File file) {

		try {
			return (T) xstream.fromXML( new FileInputStream(file) );
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T fromXML(String xml) {
		return (T) xstream.fromXML(xml);
	}
	
	public static String toXML(Object obj) {
		return xstream.toXML(obj);
	}
	
	public static void toXML(Object obj, File file) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			xstream.toXML(obj,writer);
		} 
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		try {
			if( writer != null ) writer.close();
		} 
		catch (IOException e) {
			log.warn(String.format("Error on closing file: '%s'", file));
		}
	}   
}
