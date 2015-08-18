package com.kickmogu.yodobashi.community.resource.cache;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.exception.CommonSystemException;

public class MethodCacheManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(MethodCacheManager.class);
	
	protected Method method;
	protected MethodCache cacheAnnotation;
	protected CacheKeyGenerator cacheKeyGenerator;
	protected MethodCacheStrategy cacheStrategy;
	protected AtomicInteger hitCount = new AtomicInteger(0);
	protected AtomicInteger nohitCount = new AtomicInteger(0);
	protected List<MethodCacheFilter> filters = Lists.newArrayList();
	protected CacheContentsFilter cacheContentsFilter;
	
	public MethodCacheManager(Method method, MethodCache cacheAnnotation, CacheKeyGenerator cacheKeyGenerator, MethodCacheStrategy cacheStrategy) {
		this.method = method;
		this.cacheAnnotation = cacheAnnotation;
		this.cacheKeyGenerator = cacheKeyGenerator;
		this.cacheStrategy = cacheStrategy;
		try {
			this.cacheContentsFilter = cacheAnnotation.cacheContentsFilter().newInstance();
		} catch (Throwable e) {
			throw new CommonSystemException(e);
		}
		for (Class<? extends MethodCacheFilter> filterClass:cacheAnnotation.filters()) {
			try {
				filters.add(filterClass.newInstance());
			} catch (Throwable e) {
				throw new CommonSystemException(e);
			}
		}
	}

	public CacheContentsFilter getCacheContentsFilter() {
		return cacheContentsFilter;
	}

	public boolean filter(Object[] args) {
		for (MethodCacheFilter filter:filters) {
			if (!filter.filter(args)) return false;
		}
		return true;
	}
	
	public boolean enable() {
		return cacheStrategy.enable();
	}

	public Object generateKey(MethodArgument methodArgument) {
		return cacheKeyGenerator.generateCacheKey(methodArgument);
	}

	public synchronized void putCacheContents(Object key,
			CacheContents cacheContents) {
		cacheStrategy.put(key, cacheContents);
	}

	public synchronized CacheContents getCacheContents(Object key) {
		CacheContents cacheContents = cacheStrategy.get(key);
		if (cacheContents == null) return null;
		return cacheContents;
	}
	
	public long getLimitTimeAsMillSeconds() {
		return cacheAnnotation.limitTimeUnit().toMillis(cacheAnnotation.limitTime());
	}

	public boolean timeOvered(CacheContents cacheContents) {
		if (cacheAnnotation.limitTime() == MethodCache.TIME_UNLIMITED) return false;
		return System.currentTimeMillis() - cacheContents.getStartTime() > cacheAnnotation.limitTimeUnit().toMillis(cacheAnnotation.limitTime());
	}
	
	public String getMethodSignature() {
		StringBuilder sb = new StringBuilder();
		sb.append(method.getDeclaringClass().getSimpleName() + "." + method.getName()+"(");
		for (Class<?>paramType:method.getParameterTypes()) {
			sb.append(paramType.getName() + ",");
		}
		String str =  sb.toString();
		return str.substring(0, str.length()-1)+")";
	}
	
	public int getHitCount() {
		return hitCount.intValue();
	}
	
	public int getNohitCount() {
		return nohitCount.intValue();
	}
	
	public void incrementHitCount() {
		 hitCount.incrementAndGet();
	}

	public void incrementNohitCount() {
		 nohitCount.incrementAndGet();
	}
	
	public double getHitRatio() {
		if (getHitCount() == 0 && getNohitCount() == 0) return 0;
		return (double)getHitCount()/(getHitCount()+getNohitCount());
	}

	public  void clearTimeOveredCaches() {
		if (!cacheStrategy.enable()) return;
		// Hazelcastタイプのある場合は、Hazelcastに管理を全て任せる。
		if (cacheAnnotation.cacheStrategy() == CacheStrategyType.Hazelcast) return;
		if (cacheAnnotation.limitTime() == MethodCache.TIME_UNLIMITED) return;
		
		synchronized (this) {
			List<Object> keys = Lists.newArrayList(cacheStrategy.keySet().iterator());
			for (Object key:keys) {
				CacheContents cacheContents = cacheStrategy.get(key);
				if (timeOvered(cacheContents)) {
					cacheStrategy.remove(key);
					LOG.info("CacheTimeOrvered(Chore) " + getMethodSignature() + " cacheKey="+ key + " " + cacheContents.toReportString());
				}
			}
		}
	}
	
	public void clearAllCache(){
		if (!cacheStrategy.enable()) return;
		synchronized (this) {
			List<Object> keys = Lists.newArrayList(cacheStrategy.keySet().iterator());
			for (Object key:keys) {
				CacheContents cacheContents = cacheStrategy.get(key);
				cacheStrategy.remove(key);
				LOG.info("removeCache(By clearAllCache) " + getMethodSignature() + " cacheKey="+ key + " " + cacheContents.toReportString());
			}
		}
	}

	public String toReportString() {
		return "MethodCacheReport:" + getMethodSignature() + " currentSize=" + cacheStrategy.size() + " hitCount=" + getHitCount() + " nohitCount=" + getNohitCount() + " hitRatio="+new DecimalFormat("###.###").format(getHitRatio());
	}

	public void beforeRequest() {
		this.cacheStrategy.beforeRequest();
	}

	public void destory() {
		try {
			cacheStrategy.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
