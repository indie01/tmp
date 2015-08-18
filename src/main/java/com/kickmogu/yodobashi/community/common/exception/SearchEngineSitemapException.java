package com.kickmogu.yodobashi.community.common.exception;



public class SearchEngineSitemapException extends YcComException {

	/**
	 *
	 */
	private static final long serialVersionUID = -2624607070351414476L;

	public SearchEngineSitemapException() {
		super();
	}

	public SearchEngineSitemapException(String message){
		super(message);
	}

	public SearchEngineSitemapException(String message, Throwable cause){
		super(message);
		this.initCause(cause);
	}

	public SearchEngineSitemapException(Throwable cause){
		super(cause != null ? cause.toString() : null);
		this.initCause(cause);
	}
}
