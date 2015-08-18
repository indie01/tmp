package com.kickmogu.yodobashi.community.resource.config;

import java.io.IOException;
import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.velocity.VelocityEngineFactory;

import com.kickmogu.yodobashi.community.resource.domain.constants.MailType;

@Configuration
public class VelocityInitializeContext implements InitializingBean {

	/**
	 * VelocityEngine を返します。
	 * @return VelocityEngine
	 * @throws IOException 入出力例外が発生した場合
	 * @throws VelocityException Velocityの初期化で失敗した場合
	 */
	@Bean
	public VelocityEngine velocityEngine() throws VelocityException, IOException {
		VelocityEngineFactory factory = new VelocityEngineFactory();
		factory.setOverrideLogging(true);
		factory.setResourceLoaderPath("classpath:mail");
		Properties properties = new Properties();
		properties.setProperty("input.encoding", "UTF-8");
		factory.setVelocityProperties(properties);
		return factory.createVelocityEngine();
	}
	

	@Override
	public void afterPropertiesSet() throws Exception {
		MailType.initialize(velocityEngine());
	}
}
