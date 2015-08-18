package com.kickmogu.yodobashi.community.common.exception;



public class PmsAccessException extends YcComException {

	/**
	 *
	 */
	private static final long serialVersionUID = -251207800038600997L;

	public PmsAccessException(String message) {
		super(message);
	}

    public PmsAccessException(String message, Throwable cause){
        super(message, cause);
    }

    public PmsAccessException(Throwable cause){
        super(cause);
    }

}
