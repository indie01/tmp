package com.kickmogu.yodobashi.community.common.exception;



public class UnActiveException extends YcComException {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1312277588574449297L;

	public UnActiveException() {
		super();
	}

	public UnActiveException(String message){
        super(message);
    }

    public UnActiveException(String message, Throwable cause){
        super(message);
        this.initCause(cause);
    }

    public UnActiveException(Throwable cause){
        super(cause != null ? cause.toString() : null);
        this.initCause(cause);
    }
}
