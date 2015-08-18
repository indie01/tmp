package com.kickmogu.yodobashi.community.common.exception;



public class AkamaiAccessException extends YcComException {

    /**
	 *
	 */
	private static final long serialVersionUID = 3522957509836547222L;

	public AkamaiAccessException() {
		super();
	}

	public AkamaiAccessException(String message){
        super(message);
    }

    public AkamaiAccessException(String message, Throwable cause){
        super(message);
        this.initCause(cause);
    }

    public AkamaiAccessException(Throwable cause){
        super(cause != null ? cause.toString() : null);
        this.initCause(cause);
    }

}
