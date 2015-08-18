package com.kickmogu.yodobashi.community.resource.cache;

import org.springframework.stereotype.Service;

@Service
public class MethodArgumentCacheKeyGenerator implements CacheKeyGenerator {


	@Override
	public Object generateCacheKey(MethodArgument methodArgument) {
		return methodArgument;
	}
	
}
