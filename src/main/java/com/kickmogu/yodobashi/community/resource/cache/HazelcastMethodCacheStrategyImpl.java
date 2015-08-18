package com.kickmogu.yodobashi.community.resource.cache;

import java.lang.reflect.Method;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.hazelcast.config.Config;
import com.hazelcast.config.Join;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;

@Service @Scope("prototype")
public class HazelcastMethodCacheStrategyImpl implements MethodCacheStrategy {
	
	private static boolean initialized;
	
	private static final String INSTANCE_NAME = "methodcache";
	private String mapName;

	@Autowired
	private ResourceConfig resourceConfig;
	
	@Override
	public boolean enable() {
		return resourceConfig.enableHazelcastMethodCache;
	}

	@Override
	public void setMethod(Method method) {
		
		if (!resourceConfig.enableHazelcastMethodCache) {
			return;
		}
		
		synchronized (HazelcastMethodCacheStrategyImpl.class) {
			if (!initialized) {
				Config config = new Config();
				config.setInstanceName(INSTANCE_NAME);
				
				NetworkConfig networkConfig = config.getNetworkConfig();
				networkConfig.getInterfaces().clear();
				networkConfig.setPort(resourceConfig.hazelcastMethodCachePort);
				networkConfig.setPortAutoIncrement(true);
				Join join = networkConfig.getJoin();
				join.getAwsConfig().setEnabled(false);
				join.getTcpIpConfig().setEnabled(false);
				join.getMulticastConfig().setEnabled(true)
				.setMulticastGroup(resourceConfig.hazelcastMethodCacheMulticastGroup)
				.setMulticastPort(resourceConfig.hazelcastMethodCacheMulticastPort);
				Hazelcast.newHazelcastInstance(config);
				initialized = true;				
			}
		}
		
		MethodCache methodCacheAnnotation = method.getAnnotation(MethodCache.class);
		Config config = Hazelcast.getHazelcastInstanceByName(INSTANCE_NAME).getConfig();
		MapConfig mapCfg = new MapConfig();
		NearCacheConfig nearCacheConfig = new NearCacheConfig();
		
		this.mapName = createMapName(method);

		mapCfg.setName(this.mapName);
		if (methodCacheAnnotation.size() != MethodCache.SIZE_UNLIMITED) {
			mapCfg.setEvictionPolicy(methodCacheAnnotation.pagingAlgorithm().name());
			nearCacheConfig.setEvictionPolicy(methodCacheAnnotation.pagingAlgorithm().name());
			mapCfg.getMaxSizeConfig().setSize(methodCacheAnnotation.size());
			nearCacheConfig.setMaxSize(methodCacheAnnotation.size());
		}
		if (methodCacheAnnotation.limitTime() != MethodCache.TIME_UNLIMITED) {
			int time = (int)methodCacheAnnotation.limitTimeUnit().toSeconds(methodCacheAnnotation.limitTime());
			mapCfg.setTimeToLiveSeconds(time);
			nearCacheConfig.setTimeToLiveSeconds(time);
		}
		
		mapCfg.setNearCacheConfig(nearCacheConfig);
		config.addMapConfig(mapCfg);
	}
	
	private String createMapName(Method method) {
		StringBuilder sb = new StringBuilder();
		sb.append("hazelcast.method.cache.");
		sb.append(method.getDeclaringClass().getSimpleName() + "." + method.getName()+"(");
		for (Class<?>paramType:method.getParameterTypes()) {
			sb.append(paramType.getName() + ",");
		}
		String str =  sb.toString();
		return str.substring(0, str.length()-1)+")";
	}

	@Override
	public void put(Object key, CacheContents cache) {
		Hazelcast.getHazelcastInstanceByName(INSTANCE_NAME).getMap(mapName).put(key, cache);
	}

	@Override
	public CacheContents get(Object key) {
		return (CacheContents)Hazelcast.getHazelcastInstanceByName(INSTANCE_NAME).getMap(mapName).get(key);
	}

	@Override
	public CacheContents remove(Object key) {
		return (CacheContents)Hazelcast.getHazelcastInstanceByName(INSTANCE_NAME).getMap(mapName).remove(key);
	}

	@Override
	public Set<Object> keySet() {
		return Hazelcast.getHazelcastInstanceByName(INSTANCE_NAME).getMap(mapName).keySet();
	}

	@Override
	public int size() {
		return Hazelcast.getHazelcastInstanceByName(INSTANCE_NAME).getMap(mapName).size();
	}

	@Override
	public void beforeRequest() {
	}

	@Override
	public void destroy() throws Exception {
		if (resourceConfig.enableHazelcastMethodCache) {
			try {
				Hazelcast.getHazelcastInstanceByName(INSTANCE_NAME).getLifecycleService().shutdown();
			} catch (Throwable th){}
		}
	}

}
