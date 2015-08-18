package com.kickmogu.yodobashi.community.resource.cache;

public enum CacheStrategyType {
	JavaVMGlobal(JavaVMGlobalCacheStrategy.class),
	Ajax(AJaxMethodCacheStrategyImpl.class),
	HttpSession(HttpSessionMethodCacheStrategyImpl.class),
	Hazelcast(HazelcastMethodCacheStrategyImpl.class)
	;
	Class<? extends MethodCacheStrategy> type;
	private CacheStrategyType(Class<? extends MethodCacheStrategy> type) {
		this.type = type;
	}
	public Class<? extends MethodCacheStrategy> getType() {
		return type;
	}
}
