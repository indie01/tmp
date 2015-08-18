package com.kickmogu.yodobashi.community.resource.cache;

public enum CacheKeyType {
	MethodArgument(MethodArgumentCacheKeyGenerator.class),
	;
	Class<? extends CacheKeyGenerator> type;
	CacheKeyType(Class<? extends CacheKeyGenerator> type) {
		this.type = type;
	}
	public Class<? extends CacheKeyGenerator> getType() {
		return type;
	}
}
