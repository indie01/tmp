package com.kickmogu.yodobashi.community.resource.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.twitter.connect.TwitterConnectionFactory;

import com.kickmogu.yodobashi.community.resource.dao.SocialMediaSettingDao;
import com.kickmogu.yodobashi.community.social.connect.HBaseUsersConnectionRepository;

@Configuration
public class SocialContext implements InitializingBean {

	@Autowired
	private SocialConfig socialConfig;

	@Autowired
	private SocialMediaSettingDao socialMediaSettingDao;

	@Bean
	@Scope(value="singleton", proxyMode=ScopedProxyMode.INTERFACES)
	public ConnectionFactoryLocator connectionFactoryLocator() {
		ConnectionFactoryRegistry registry = new ConnectionFactoryRegistry();
		registry.addConnectionFactory(new TwitterConnectionFactory(socialConfig.twitterConsumerKey, socialConfig.twitterConsumerSecret));
		registry.addConnectionFactory(new FacebookConnectionFactory(socialConfig.facebookClientId, socialConfig.facebookClientSecret));
		return registry;
	}

	@Bean
	@Scope(value="singleton", proxyMode=ScopedProxyMode.INTERFACES)
	public UsersConnectionRepository usersConnectionRepository() {
		return new HBaseUsersConnectionRepository(socialMediaSettingDao, connectionFactoryLocator(), Encryptors.noOpText());
	}

	@Override
	public void afterPropertiesSet() throws Exception {

	}
}
