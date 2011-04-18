package org.tcoffee.client.data;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;

@XStreamAlias("response")
public class ResponseData {

	public String status;
	
	@XStreamConverter(ReflectionConverter.class)
	public SubmitData submit;
	
	public ResultData result;
	
	@XStreamImplicit(itemFieldName="service")
	public List<ServiceData> services;
	
	public ErrorData err;
	
}
