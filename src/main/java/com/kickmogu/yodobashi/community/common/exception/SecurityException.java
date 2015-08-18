package com.kickmogu.yodobashi.community.common.exception;



public class SecurityException extends YcComException {

    /**
	 *
	 */
	private static final long serialVersionUID = -2624607070351414476L;

	public SecurityException() {
		super();
	}

	public SecurityException(String message){
        super(message);
    }

    public SecurityException(String message, Throwable cause){
        super(message);
        this.initCause(cause);
    }

    public SecurityException(Throwable cause){
        super(cause != null ? cause.toString() : null);
        this.initCause(cause);
    }
}
