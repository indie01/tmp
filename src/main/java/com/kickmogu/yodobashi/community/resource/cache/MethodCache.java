package com.kickmogu.yodobashi.community.resource.cache;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * キャッシュアノテーション
 *
 *
 * @author nomura
 *
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface MethodCache {
	
	CacheKeyType cacheKey() default CacheKeyType.MethodArgument;
	CacheStrategyType cacheStrategy();
	Class<? extends CacheContentsFilter> cacheContentsFilter() default NullCacheContentsFilter.class;
	int size() default SIZE_DEFAULT;
	PagingAlgorithm pagingAlgorithm() default PagingAlgorithm.LRU;
	long limitTime() default TIME_UNLIMITED;
	TimeUnit limitTimeUnit() default TimeUnit.SECONDS;
	Class<? extends MethodCacheFilter>[] filters() default {};
	boolean skipInnterCache() default true;
	TargetSystemType[] targetSystems() default{};
	
	public static final int SIZE_DEFAULT = 1000;
	
	public static final int SIZE_UNLIMITED = -1;
	public static final long TIME_UNLIMITED = -1L;
}
