package com.kickmogu.yodobashi.community.common.exception;

import java.util.Map;

import com.google.common.collect.Maps;


public class YcComUnvalilableException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3725829311458055867L;
	
	private Map<String, Object> attributes = Maps.newHashMap();

	public YcComUnvalilableException() {
		super();
	}
	
	public YcComUnvalilableException(String message){
        super(message);
    }

    public YcComUnvalilableException(String message, Throwable cause){
        super(message);
        this.initCause(cause);
    }

    public YcComUnvalilableException(Throwable cause){
        super(cause != null ? cause.toString() : null);
        this.initCause(cause);
    }
    
    public void setAttribute(String name, Object value) {
    	attributes.put(name, value);
    }

    @SuppressWarnings("unchecked")
	public <T> T getAttribute(Class<T> clazz, String name) {
    	return (T)attributes.get(name);
    }
}
