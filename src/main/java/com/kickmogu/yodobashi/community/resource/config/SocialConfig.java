package com.kickmogu.yodobashi.community.resource.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.kickmogu.lib.core.cofig.BaseConfig;

@Configuration
public class SocialConfig extends BaseConfig {

	public static SocialConfig INSTANCE;
	
	@Value("${facebook.clientId}")
	public String facebookClientId;
	
	@Value("${facebook.clientSecret}")
	public String facebookClientSecret;
	
	@Value("${facebook.admins}")
	public String facebookAdmins;
	
	@Value("${twitter.consumerKey}")
	public String twitterConsumerKey;
	
	@Value("${twitter.consumerSecret}")
	public String twitterConsumerSecret;
	
	@Value("${social.applicationUrl}")
	public String socialApplicationUrl;
	
	/**
	 * ソーシャル用ソケットタイムアウト値です。
	 */
	@Value("${social.socket.timeout}")
	public Integer socialSocketTimeout;

	/**
	 * ソーシャル用コネクションタイムアウト値です。
	 */
	@Value("${social.connection.timeout}")
	public Integer socialConnectionTimeout;
	
	/**
	 * bit.lyのアカウント名です。
	 */
	@Value("${shortUrl.bitlyUserName}")
	public String bitlyUserName;
	
	/**
	 * bit.lyのアカウント名です。
	 */
	@Value("${shortUrl.bitlyApiKey}")
	public String bitlyApiKey;
	
	/**
	 * bit.lyのURLです。
	 */
	@Value("${shortUrl.bitlyUrl}")
	public String bitlyUrl;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
		super.afterPropertiesSet();
	}
	
	
	
}
