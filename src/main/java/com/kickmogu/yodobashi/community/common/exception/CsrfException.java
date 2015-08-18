package com.kickmogu.yodobashi.community.common.exception;



public class CsrfException extends YcComException {

    /**
	 *
	 */
	private static final long serialVersionUID = -2624607070351414476L;

	public CsrfException() {
		super();
	}

	public CsrfException(String message){
        super(message);
    }

    public CsrfException(String message, Throwable cause){
        super(message);
        this.initCause(cause);
    }

    public CsrfException(Throwable cause){
        super(cause != null ? cause.toString() : null);
        this.initCause(cause);
    }
}
