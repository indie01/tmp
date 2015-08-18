package com.kickmogu.yodobashi.community.resource.cache;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.commons.collections.SetUtils;
import org.apache.commons.collections.map.LRUMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.kickmogu.yodobashi.community.resource.cache.PagingAlgorithm;
import com.kickmogu.yodobashi.community.resource.config.HttpSessionHolder;

@Service @Scope("prototype")
public class HttpSessionMethodCacheStrategyImpl implements MethodCacheStrategy {


	@Autowired
	private HttpSessionHolder httpSessionHolder;
	
	private MethodCache methodCacheAnnotation;
	
	private String signature;

	@Override
	public void setMethod(Method method) {
		this.methodCacheAnnotation = method.getAnnotation(MethodCache.class);
		signature = createMethodSignature(method);
	}
	
	private String createMethodSignature(Method method) {
		StringBuilder sb = new StringBuilder();
		sb.append("httpSession.method.cache.");
		sb.append(method.getDeclaringClass().getSimpleName() + "." + method.getName()+"(");
		for (Class<?>paramType:method.getParameterTypes()) {
			sb.append(paramType.getName() + ",");
		}
		String str =  sb.toString();
		return str.substring(0, str.length()-1)+")";
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void put(Object key, CacheContents cacheContents) {
		HttpSession httpSession = httpSessionHolder.getHttpSession();
		Map cache = (Map)httpSession.getAttribute(signature);
		if (cache == null) {
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
		cache.put(key, cacheContents);
		httpSession.setAttribute(signature, cache);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public CacheContents get(Object key) {
		HttpSession httpSession = httpSessionHolder.getHttpSession();
		
		Map cache = (Map)httpSession.getAttribute(signature);
		if (cache == null) return null;
		return (CacheContents)cache.get(key);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public CacheContents remove(Object key) {
		HttpSession httpSession = httpSessionHolder.getHttpSession();
		Map cache = (Map)httpSession.getAttribute(signature);
		if (cache == null) return null;
		return (CacheContents)cache.remove(key);
	}

	@Override
	public boolean enable() {
		HttpSession httpSession = httpSessionHolder.getHttpSession();
		if ( httpSession == null) return false;
		try {
			httpSession.getAttribute("dummy");
			return true;
		} catch (IllegalStateException e) {
			return false;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Set<Object> keySet() {
		HttpSession httpSession = httpSessionHolder.getHttpSession();
		Map cache = (Map)httpSession.getAttribute(signature);
		if (cache == null) return SetUtils.EMPTY_SET;
		return cache.keySet();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public int size() {
		HttpSession httpSession = httpSessionHolder.getHttpSession();
		Map cache = (Map)httpSession.getAttribute(signature);
		if (cache == null) return 0;
		return cache.size();
	}

	@Override
	public void beforeRequest() {
	}

	@Override
	public void destroy() throws Exception {
	}

}
