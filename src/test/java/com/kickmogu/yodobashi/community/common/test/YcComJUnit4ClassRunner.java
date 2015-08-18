package com.kickmogu.yodobashi.community.common.test;

import java.io.IOException;
import java.util.Properties;

import org.junit.runners.model.InitializationError;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.kickmogu.lib.core.exception.CommonSystemException;

public class YcComJUnit4ClassRunner extends SpringJUnit4ClassRunner {

	public YcComJUnit4ClassRunner(Class<?> clazz) throws InitializationError {
		super(clazz);
		Properties properties = new Properties();
		try {
			properties.load(ClassLoader.getSystemResourceAsStream("unit-test.properties"));
		} catch (IOException e) {
			throw new CommonSystemException(e);
		}

		String forOnlyUserPreuffix =  System.getProperty("user.name")  + ".";
		for (String key:properties.stringPropertyNames()) {
			if (key.startsWith(forOnlyUserPreuffix)) {
				System.setProperty(key.replaceAll("^" + System.getProperty("user.name")+"\\.", ""), properties.getProperty(key));
			} else {
				System.setProperty(key, properties.getProperty(key));
			}
		}
	}

}
