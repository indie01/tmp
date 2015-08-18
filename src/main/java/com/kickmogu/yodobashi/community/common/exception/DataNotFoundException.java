package com.kickmogu.yodobashi.community.common.exception;



public class DataNotFoundException extends YcComException {

    /**
	 *
	 */
	private static final long serialVersionUID = -2624607070351414476L;

	public DataNotFoundException() {
		super();
	}

	public DataNotFoundException(String message){
        super(message);
    }

    public DataNotFoundException(String message, Throwable cause){
        super(message);
        this.initCause(cause);
    }

    public DataNotFoundException(Throwable cause){
        super(cause != null ? cause.toString() : null);
        this.initCause(cause);
    }
}
