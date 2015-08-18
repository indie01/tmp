package com.kickmogu.yodobashi.community.jcclient.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.kickmogu.lib.core.cofig.BaseConfig;

@Configuration
public class JcClientConfig extends BaseConfig {

	public static JcClientConfig INSTANCE;

	/**
	 * 最大接続数です。
	 */
	@Value("${mq.maxConnections}")
	public Integer maxConnections;

	/**
	 * コネクション辺りの最大セッション数です。
	 */
	@Value("${mq.maximumActive}")
	public Integer maximumActive;

	/**
	 * MQ の接続先です。
	 */
	@Value("${producer.brokerUrl.site1}")
	public String site1ProducerBrokerUrl = null;

	/**
	 * MQ の接続先です。
	 */
	@Value("${producer.brokerUrl.site2}")
	public String site2ProducerBrokerUrl = null;

	/**
	 * MQ に接続するかどうかです。
	 */
	@Value("${mq.connect}")
	public boolean mqConnect;
}
