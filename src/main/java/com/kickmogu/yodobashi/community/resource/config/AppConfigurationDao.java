package com.kickmogu.yodobashi.community.resource.config;

import java.util.List;

import com.kickmogu.lib.core.cofig.BaseConfig;
import com.kickmogu.yodobashi.community.resource.domain.AppConfigurationDO;


public interface AppConfigurationDao {
	void set(String key, String value);
	void set(String key, Integer value);
	void set(String key, Long value);
	void set(String key, Boolean value);
	String get(String key);
	Integer getAsInt(String key);
	Long getAsLong(String key);
	Boolean getAsBoolean(String key);
	String getWithCache(String key);
	Integer getAsIntWithCache(String key);
	Long getAsLongWithCache(String key);
	Boolean getAsBooleanWithCache(String key);
	void remove(String key);
	List<AppConfigurationDO> getAll();
	void updateConfigObject(BaseConfig config);
}
