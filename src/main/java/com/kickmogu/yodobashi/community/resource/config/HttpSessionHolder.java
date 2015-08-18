package com.kickmogu.yodobashi.community.resource.config;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

@Service
public class HttpSessionHolder {

	ThreadLocal<HttpSession> httpSessionThreadHolder = new ThreadLocal<HttpSession>();
	
	public void initialize(HttpSession session) {
		httpSessionThreadHolder.set(session);
	}
	
	public HttpSession getHttpSession() {
		return httpSessionThreadHolder.get();
	}
	
	public void destroy(){
		httpSessionThreadHolder.remove();
	}
	
}
