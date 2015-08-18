package com.kickmogu.yodobashi.community.resource.cache;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.LRUMap;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.kickmogu.yodobashi.community.resource.cache.PagingAlgorithm;

@Service @Scope("prototype")
public class JavaVMGlobalCacheStrategy implements MethodCacheStrategy {

	private MethodCache methodCacheAnnotation;
	
	@SuppressWarnings("rawtypes")
	private Map cache;

	@SuppressWarnings("rawtypes")
	@Override
	public void setMethod(Method method) {
		this.methodCacheAnnotation = method.getAnnotation(MethodCache.class);
		if (methodCacheAnnotation.size() == MethodCache.SIZE_UNLIMITED) {
			cache = Maps.newHashMap();
		} else {
			if (methodCacheAnnotation.pagingAlgorithm().equals(PagingAlgorithm.FIFO)) {
				cache = new FIFOMap(methodCacheAnnotation.size());
			} else {
				cache = new LRUMap(methodCacheAnnotation.size());
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void put(Object key, CacheContents contents) {
		cache.put(key, contents);
	}

	@Override
	public CacheContents get(Object key) {
		return (CacheContents)cache.get(key);
	}

	@Override
	public CacheContents remove(Object key) {
		return (CacheContents)cache.remove(key);
	}

	@Override
	public boolean enable() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<Object> keySet() {
		return cache.keySet();
	}

	@Override
	public int size() {
		return cache.size();
	}

	@Override
	public void beforeRequest() {

	}

	@Override
	public void destroy() throws Exception {
	}

}
