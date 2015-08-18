package com.kickmogu.yodobashi.community.resource.aop;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.Chore;
import org.apache.hadoop.hbase.Stoppable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.kickmogu.core.utils.ThreadLocalUtils;
import com.kickmogu.lib.core.utils.Reflections;
import com.kickmogu.yodobashi.community.resource.cache.CacheContents;
import com.kickmogu.yodobashi.community.resource.cache.CacheKeyGenerator;
import com.kickmogu.yodobashi.community.resource.cache.CacheStrategyType;
import com.kickmogu.yodobashi.community.resource.cache.MethodArgument;
import com.kickmogu.yodobashi.community.resource.cache.MethodCache;
import com.kickmogu.yodobashi.community.resource.cache.MethodCacheManager;
import com.kickmogu.yodobashi.community.resource.cache.MethodCacheStrategy;
import com.kickmogu.yodobashi.community.resource.cache.NoCacheException;
import com.kickmogu.yodobashi.community.resource.cache.TargetSystemType;
import com.kickmogu.yodobashi.community.resource.config.AppConfigurationDao;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;

@Service @Aspect
public class AopMethodCacheHandlerImpl implements Stoppable, InitializingBean, DisposableBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(AopMethodCacheHandlerImpl.class);
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private ResourceConfig resourceConfig;
	
	@Autowired
	private AppConfigurationDao appConfigurationDao;
	
	private Map<Method, MethodCacheManager> cacheManagerMap = Maps.newHashMap();
	private boolean stopped;
	
	private TimeOverCacheClearChore timeOverCacheClearChore;
	private ReportCacheChore reportCacheChore;
	
	public static class MethodCacheContext {
		int depth = 0;
		int skipInnerCacheNum = 0;
	}
	
	//private static ThreadLocal<MethodCacheContext> threadLocal = new ThreadLocal<MethodCacheContext>();
	
	private AtomicBoolean methodCacheForceOff = new AtomicBoolean();
	private long methodCacheForceOffLastLoaded;
	
	@Around("@annotation(com.kickmogu.yodobashi.community.resource.cache.MethodCache)")
	public Object handle(ProceedingJoinPoint jointPoint) throws Throwable {
		
		if (!resourceConfig.enableMethodCache) {
			return jointPoint.proceed();
		}
		
		if (System.currentTimeMillis() - methodCacheForceOffLastLoaded > 30*1000L) {
			methodCacheForceOff.set(getMethodCacheForceOffFromConfig());
			methodCacheForceOffLastLoaded = System.currentTimeMillis();
		}
		
		if (methodCacheForceOff.get()) {
			return jointPoint.proceed();
		}
		
		MethodCacheContext methodCacheContext = ThreadLocalUtils.getThreadVariable(
				MethodCacheContext.class.getCanonicalName(), 
				MethodCacheContext.class);
		
//		MethodCacheContext methodCacheContext = threadLocal.get();
//		if (methodCacheContext == null) {
//			methodCacheContext = new MethodCacheContext();
//			threadLocal.set(methodCacheContext);
//		}

		methodCacheContext.depth++;

		try {
			return handleBody(methodCacheContext, jointPoint);			
		} finally {
			methodCacheContext.depth--;
			if (methodCacheContext.depth == 0){
//				threadLocal.remove();
			}
		}
	}

	protected Object handleBody(MethodCacheContext methodCacheContext, ProceedingJoinPoint jointPoint) throws Throwable {
		
		Method method = Reflections.getMethodFromJointPoint(jointPoint);
		MethodCache cacheAnnotation = method.getAnnotation(MethodCache.class);
		LOG.info(method.toString());
		
		if( cacheAnnotation == null || !isTargetSystem(cacheAnnotation) ){
			return jointPoint.proceed();
		}
		
		if (methodCacheContext.skipInnerCacheNum > 0) {
			return jointPoint.proceed();
		}

		MethodCacheManager cacheManager = null;

		synchronized (method) {
			if (!cacheManagerMap.containsKey(method)) {
				CacheKeyGenerator cacheKeyGenerator = applicationContext.getBean(cacheAnnotation.cacheKey().getType());
				MethodCacheStrategy cacheStrategy = applicationContext.getBean(cacheAnnotation.cacheStrategy().getType());
				cacheStrategy.setMethod(method);
				if (!cacheStrategy.enable()) {
					LOG.debug("DISABLE" + method.getDeclaringClass().getSimpleName()+"."+method.getName());
					return jointPoint.proceed();
				}
				cacheManagerMap.put(method, new MethodCacheManager(method, cacheAnnotation, cacheKeyGenerator, cacheStrategy));	
			}
			cacheManager = cacheManagerMap.get(method);
		}
		
		if (!cacheManager.enable()) {
			return jointPoint.proceed();
		}
		
		if (!cacheManager.filter(jointPoint.getArgs())) {
			return jointPoint.proceed();
		}
		
		try {
			if (cacheAnnotation.skipInnterCache()) {
				methodCacheContext.skipInnerCacheNum++;
			}

			MethodArgument methodArgument = new MethodArgument(method, jointPoint.getArgs());
			Object key = cacheManager.generateKey(methodArgument);
			
			CacheContents cacheContents = cacheManager.getCacheContents(key);
			if (cacheContents == null || cacheManager.timeOvered(cacheContents)) {
				Object orgResult = jointPoint.proceed();
				Serializable result = null;
				try {
					result = cacheManager.getCacheContentsFilter().toCacheContents(orgResult); 
				} catch (NoCacheException e) {
					return result;
				}
				if (cacheContents != null) {
					LOG.info("CacheTimeOrvered " + cacheManager.getMethodSignature() + " cacheKey="+ key + " " + cacheContents.toReportString());
				}
				cacheManager.incrementNohitCount();
				cacheContents = new CacheContents((Serializable)key, result);
				cacheManager.putCacheContents(key, cacheContents);
				return orgResult;				
			} else {
				cacheManager.incrementHitCount();
				cacheContents.incrementHitCount();
				LOG.info("HitCache:" + cacheManager.getMethodSignature()+":"+cacheContents.toReportString());
				return cacheManager.getCacheContentsFilter().fromCacheContents((Serializable)cacheContents.getContents());				
			}			
		} finally {
			if (cacheAnnotation.skipInnterCache()) {
				methodCacheContext.skipInnerCacheNum--;
			}
		}
		
	}
	
	public void clearCache(Method method) {
		cacheManagerMap.get(method).clearAllCache();
	}
	
	public void beforeRequest() {
		Collection<MethodCacheManager> methodCacheManagers = null;
		synchronized (cacheManagerMap) {
			methodCacheManagers = cacheManagerMap.values();
		}
		for (MethodCacheManager methodCacheManager:methodCacheManagers) {
			methodCacheManager.beforeRequest();
		}
	}
	
	private class TimeOverCacheClearChore extends Chore {
		
		private static final int CHECK_INTERVAL = 10*1000;

		public TimeOverCacheClearChore(Stoppable stopper) {
			super("TimeOverCacheClear", CHECK_INTERVAL, stopper);
		}

		@Override
		protected void chore() {
			Collection<MethodCacheManager> methodCacheManagers = null;
			synchronized (cacheManagerMap) {
				methodCacheManagers = cacheManagerMap.values();
			}
			for (MethodCacheManager methodCacheManager:methodCacheManagers) {
				try {
					methodCacheManager.clearTimeOveredCaches();
				} catch (Throwable th){
					LOG.warn("Cache Clear failed."+ th.getClass().getName()+":"+th.getMessage());
				}
			}
			
		}
	}
	
	private class ReportCacheChore extends Chore {
		
		private static final int CHECK_INTERVAL = 60*1000;

		public ReportCacheChore(Stoppable stopper) {
			super("ReportCache", CHECK_INTERVAL, stopper);
		}

		@Override
		protected void chore() {
			Collection<MethodCacheManager> methodCacheManagers = null;
			synchronized (cacheManagerMap) {
				methodCacheManagers = cacheManagerMap.values();
			}
			for (MethodCacheManager methodCacheManager:methodCacheManagers) {
				if (!methodCacheManager.enable()) continue;
				LOG.info(methodCacheManager.toReportString());
			}
			
		}
	}
	
	private boolean getMethodCacheForceOffFromConfig() {
		Boolean result = appConfigurationDao.getAsBoolean("method.cache.force.off");
		return result != null ? result : false;
	}
	
	private boolean isTargetSystem(MethodCache cacheAnnotation){
		// 互換性を保つため指定がなければキャッシュする
		if( cacheAnnotation.targetSystems().length == 0  )
			return true;
		// システム
		String startUpSystemName = System.getProperty("community.system.name");
		if( StringUtils.isEmpty(startUpSystemName))
			return false;
		
		TargetSystemType ownTargetSystemType = TargetSystemType.valueOf(startUpSystemName);
		
		for(TargetSystemType targetSystem : cacheAnnotation.targetSystems() ){
			if( !targetSystem.equals(ownTargetSystemType))
				continue;
			
			if( CacheStrategyType.HttpSession.equals(cacheAnnotation.cacheStrategy()) 
					|| CacheStrategyType.Ajax.equals(cacheAnnotation.cacheStrategy())){
				if( TargetSystemType.CommunityWeb.equals(ownTargetSystemType) 
						|| TargetSystemType.CommunityWs.equals(ownTargetSystemType) 
						|| TargetSystemType.CommunityDataSyncWeb.equals(ownTargetSystemType)){
					return true;
				}else{
					return false;
				}
			}else{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		String methodCacheChoreStartFlag = System.getProperty("community.methodcache.chore.start");
		if( StringUtils.isEmpty(methodCacheChoreStartFlag) || !"true".equals(methodCacheChoreStartFlag) ){
			LOG.info("Skip MethodCache Service TimeOverCache and ReportCache...");
			return;
		}
		
		methodCacheForceOff.set(getMethodCacheForceOffFromConfig());
		methodCacheForceOffLastLoaded = System.currentTimeMillis();
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){

			@Override
			public void run() {
				stop("shutdown");
			}
			
		}));
		this.timeOverCacheClearChore = new TimeOverCacheClearChore(this);
		this.timeOverCacheClearChore.start();
		this.reportCacheChore = new ReportCacheChore(this);
		this.reportCacheChore.start();
		
		LOG.info("Start MethodCache Service TimeOverCache and ReportCache...");
	}

	@Override
	public void stop(String why) {
		this.stopped = true;
		LOG.info("shutdown required.");
	}

	@Override
	public boolean isStopped() {
		return stopped;
	}

	@Override
	public void destroy() throws Exception {
		 stop("shutdown");
		 for (MethodCacheManager methodCacheManager:cacheManagerMap.values()) {
			 methodCacheManager.destory();
		 }
	}

	
}
