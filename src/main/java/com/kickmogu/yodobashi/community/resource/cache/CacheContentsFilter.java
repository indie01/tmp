package com.kickmogu.yodobashi.community.resource.cache;

import java.io.Serializable;

public interface CacheContentsFilter {
	Serializable toCacheContents(Object org) throws NoCacheException;
	Object fromCacheContents(Serializable cacheContents);
}
