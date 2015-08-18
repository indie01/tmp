package com.kickmogu.yodobashi.community.resource.cache;

import java.lang.reflect.Method;
import java.util.Set;

public interface MethodCacheStrategy {
	boolean enable();
	void setMethod(Method method);

	void put(Object key, CacheContents cache);
	CacheContents get(Object key);
	CacheContents remove(Object key);
	Set<Object> keySet();
	int size();
	void beforeRequest();
	void destroy() throws Exception;
}
