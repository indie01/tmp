package com.kickmogu.yodobashi.community.common.exception;



public class FollowLimitException extends YcComException {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1312277588574449297L;

	public FollowLimitException() {
		super();
	}

	public FollowLimitException(String message){
        super(message);
    }

    public FollowLimitException(String message, Throwable cause){
        super(message);
        this.initCause(cause);
    }

    public FollowLimitException(Throwable cause){
        super(cause != null ? cause.toString() : null);
        this.initCause(cause);
    }
}
