package com.kickmogu.yodobashi.community.resource.cache;

import java.io.Serializable;

public class NullCacheContentsFilter implements CacheContentsFilter {

	@Override
	public Serializable toCacheContents(Object org) throws NoCacheException {
		return (Serializable)org;
	}

	@Override
	public Object fromCacheContents(Serializable cacheContents) {
		return cacheContents;
	}


}
