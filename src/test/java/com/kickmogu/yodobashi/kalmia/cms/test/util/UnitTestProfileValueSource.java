package com.kickmogu.yodobashi.kalmia.cms.test.util;

import java.io.IOException;
import java.util.Properties;

import org.springframework.test.annotation.ProfileValueSource;
import org.springframework.test.annotation.SystemProfileValueSource;

import com.kickmogu.yodobashi.community.common.exception.YcComException;


public class UnitTestProfileValueSource implements ProfileValueSource {

	private Properties properties;

	public UnitTestProfileValueSource() {
		properties = new Properties();
		try {
			properties.load(ClassLoader.getSystemResourceAsStream("unit-test.properties"));
		} catch (IOException e) {
			throw new YcComException(e);
		}
		throw new RuntimeException();
	}

	@Override
	public String get(String key) {
		if (properties.containsKey(key)) {
			return properties.getProperty(key);
		}
		String forOnlyUserKey = System.getProperty("user.name") + "." + key;
		if (properties.containsKey(forOnlyUserKey)) {
			return properties.getProperty(forOnlyUserKey);
		}
		return SystemProfileValueSource.getInstance().get(key);
	}

	public static void main(String[] args) {
		for (Object key:System.getProperties().keySet()) {
			System.out.println(key+":" + System.getProperty((String)key));
		}
	}

}
