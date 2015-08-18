package com.kickmogu.yodobashi.community.common.exception;



public class SpoofingNameException extends YcComException {

	/**
	 *
	 */
	private static final long serialVersionUID = -251207800038600997L;

	public SpoofingNameException(String message) {
		super(message);
	}

    public SpoofingNameException(String message, Throwable cause){
        super(message, cause);
    }

    public SpoofingNameException(Throwable cause){
        super(cause);
    }

}
