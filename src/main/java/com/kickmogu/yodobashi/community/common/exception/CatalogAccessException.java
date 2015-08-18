package com.kickmogu.yodobashi.community.common.exception;



public class CatalogAccessException extends YcComException {

	/**
	 *
	 */
	private static final long serialVersionUID = -251207800038600997L;

	public CatalogAccessException(String message) {
		super(message);
	}

    public CatalogAccessException(String message, Throwable cause){
        super(message, cause);
    }

    public CatalogAccessException(Throwable cause){
        super(cause);
    }

}
