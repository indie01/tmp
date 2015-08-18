package com.kickmogu.yodobashi.community.common.exception;



public class UnMatchStatusException extends YcComException {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1312277588574449297L;

	public UnMatchStatusException() {
		super();
	}

	public UnMatchStatusException(String message){
        super(message);
    }

    public UnMatchStatusException(String message, Throwable cause){
        super(message);
        this.initCause(cause);
    }

    public UnMatchStatusException(Throwable cause){
        super(cause != null ? cause.toString() : null);
        this.initCause(cause);
    }
}
