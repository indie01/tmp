package com.kickmogu.yodobashi.community.resource.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RemoteCamelMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6544863331523745450L;
	
	private String url;
	
	private Map<String,Object> headers = new HashMap<String, Object>();
	
	private Object body;
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Map<String, Object> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, Object> headers) {
		this.headers = headers;
	}
	public Object getBody() {
		return body;
	}
	public void setBody(Object body) {
		this.body = body;
	}

}
