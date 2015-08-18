package com.kickmogu.yodobashi.community.resource.cache;


public interface CacheKeyGenerator  {
	Object generateCacheKey(MethodArgument methodArgument);
}
