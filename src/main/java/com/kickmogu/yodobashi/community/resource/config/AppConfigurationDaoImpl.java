package com.kickmogu.yodobashi.community.resource.config;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.cofig.BaseConfig;
import com.kickmogu.lib.core.exception.CommonSystemException;
import com.kickmogu.lib.core.utils.Reflections;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.yodobashi.community.resource.annotation.DynamicConfig;
import com.kickmogu.yodobashi.community.resource.cache.MethodCache;
import com.kickmogu.yodobashi.community.resource.cache.CacheStrategyType;
import com.kickmogu.yodobashi.community.resource.domain.AppConfigurationDO;

@Service
public class AppConfigurationDaoImpl implements AppConfigurationDao {
	
	private static final Logger LOG = LoggerFactory.getLogger(AppConfigurationDaoImpl.class);
	
	@Autowired  @Qualifier("default")
	private HBaseOperations hBaseOperations;

	@Override
	public void set(String key, String value) {
		hBaseOperations.save(new AppConfigurationDO(key, value));
	}

	@Override
	public void set(String key, Integer value) {
		hBaseOperations.save(new AppConfigurationDO(key, value != null ? value.toString() : null));
	}

	@Override
	public void set(String key, Long value) {
		hBaseOperations.save(new AppConfigurationDO(key, value != null ? value.toString() : null));
	}

	@Override
	public void set(String key, Boolean value) {
		hBaseOperations.save(new AppConfigurationDO(key, value != null ? value.toString() : null));
	}

	@Override
	public void updateConfigObject(BaseConfig config) {
		
		List<AppConfigurationDO> appConfigurations = getAll();
		
		for (Field field : Reflections.getAllDeclaredFields(AopUtils.getTargetClass(config.getClass()))) {
			Value valueAnnotation = field.getAnnotation(Value.class);
			if (valueAnnotation == null) {
				continue;
			}
			if (field.getAnnotation(DynamicConfig.class) == null) {
				continue;
			}
			
			for (AppConfigurationDO appConfiguration:appConfigurations) {
				if (valueAnnotation.value().equals(appConfiguration.getKey())) {
					try {
						if (field.getType().equals(String.class)) {
							field.set(config, appConfiguration.getValue());
						} else if (ClassUtils.primitiveToWrapper(field.getType()).equals(Integer.class)) {
							field.set(config, Integer.valueOf(appConfiguration.getValue()));
						} else if (ClassUtils.primitiveToWrapper(field.getType()).equals(Long.class)) {
							field.set(config, Long.valueOf(appConfiguration.getValue()));
						} else if (ClassUtils.primitiveToWrapper(field.getType()).equals(Boolean.class)) {
							field.set(config, Boolean.valueOf(appConfiguration.getValue()));
						}
						LOG.info("config updated." + config.getClass().getName()+":" + valueAnnotation.value() + " " + appConfiguration.getValue());
					} catch (Throwable th) {
						throw new CommonSystemException(th);
					}
				}
			}
		}
	}


	@Override
	public String get(String key) {
		AppConfigurationDO configuration = hBaseOperations.load(AppConfigurationDO.class, key);
		return configuration != null ? configuration.getValue() : null;
	}
	
	@Override @MethodCache(cacheStrategy=CacheStrategyType.JavaVMGlobal,limitTime=30,limitTimeUnit=TimeUnit.SECONDS)
	public String getWithCache(String key) {
		return get(key);
	}


	@Override
	public Integer getAsInt(String key) {
		String strVal = get(key);
		return strVal != null ? Integer.valueOf(strVal) : null;
	}

	@Override
	public Long getAsLong(String key) {
		String strVal = get(key);
		return strVal != null ? Long.valueOf(strVal) : null;

	}

	@Override
	public Boolean getAsBoolean(String key) {
		String strVal = get(key);
		return strVal != null ? Boolean.valueOf(strVal) : null;
	}
	

	@Override
	@MethodCache(cacheStrategy=CacheStrategyType.JavaVMGlobal,limitTime=30,limitTimeUnit=TimeUnit.SECONDS)
	public Integer getAsIntWithCache(String key) {
		return getAsInt(key);
	}

	@Override
	@MethodCache(cacheStrategy=CacheStrategyType.JavaVMGlobal,limitTime=30,limitTimeUnit=TimeUnit.SECONDS)
	public Long getAsLongWithCache(String key) {
		return getAsLong(key);
	}

	@Override
		@MethodCache(cacheStrategy=CacheStrategyType.JavaVMGlobal,limitTime=30,limitTimeUnit=TimeUnit.SECONDS)
	public Boolean getAsBooleanWithCache(String key) {
		return getAsBoolean(key);
	}

	@Override
	public List<AppConfigurationDO> getAll() {
		return hBaseOperations.scanAll(AppConfigurationDO.class);
	}

	@Override
	public void remove(String key) {
		hBaseOperations.deleteByKey(AppConfigurationDO.class, key);
	}

}
