package com.kickmogu.yodobashi.community.resource.cache;

import java.util.LinkedHashMap;
import java.util.Map;

public class FIFOMap<K,V> extends LinkedHashMap<K,V>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -262007954964817882L;
	
	private int capacity;
	
	public FIFOMap(int capacity) {
		this.capacity = capacity;
	}
	
	@Override
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest)  {
        return size() > this.capacity;
    }
	
}
