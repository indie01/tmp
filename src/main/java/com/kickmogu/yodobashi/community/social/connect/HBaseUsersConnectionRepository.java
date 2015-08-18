package com.kickmogu.yodobashi.community.social.connect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.ConnectionSignUp;
import org.springframework.social.connect.UsersConnectionRepository;

import com.kickmogu.yodobashi.community.resource.dao.SocialMediaSettingDao;
import com.kickmogu.yodobashi.community.resource.domain.SocialMediaSettingDO;

public class HBaseUsersConnectionRepository implements UsersConnectionRepository {

	private final SocialMediaSettingDao socialMediaSettingDao;
	
	private final ConnectionFactoryLocator connectionFactoryLocator;

	private final TextEncryptor textEncryptor;

	private ConnectionSignUp connectionSignUp;
	public HBaseUsersConnectionRepository(SocialMediaSettingDao socialMediaSettingDao, ConnectionFactoryLocator connectionFactoryLocator, TextEncryptor textEncryptor) {
		this.socialMediaSettingDao = socialMediaSettingDao;
		this.connectionFactoryLocator = connectionFactoryLocator;
		this.textEncryptor = textEncryptor;
	}

	public void setConnectionSignUp(ConnectionSignUp connectionSignUp) {
		this.connectionSignUp = connectionSignUp;
	}

	@Override
	public List<String> findUserIdsWithConnection(Connection<?> connection) {
		ConnectionKey key = connection.getKey();
		Set<String> providerUserIds = new HashSet<String>();
		providerUserIds.add(key.getProviderUserId());
		
		List<SocialMediaSettingDO> settings = socialMediaSettingDao.findBySocialMediaSettingByProviderIdAndProviderUserId(key.getProviderId(), providerUserIds);
		
		if (settings.size() == 0) {
			if (connectionSignUp != null) {
				String newUserId = connectionSignUp.execute(connection);
				createConnectionRepository(newUserId).addConnection(connection);
				return Arrays.asList(newUserId);
			}
		}
		
		List<String> localUserIds = new ArrayList<String>();
		for(SocialMediaSettingDO setting : settings)
			localUserIds.add(setting.getCommunityUserId());
		
		return localUserIds;
	}

	@Override
	public Set<String> findUserIdsConnectedTo(String providerId, Set<String> providerUserIds) {
		List<SocialMediaSettingDO> settings = socialMediaSettingDao.findBySocialMediaSettingByProviderIdAndProviderUserId(providerId, providerUserIds);
		
		final Set<String> localUserIds = new HashSet<String>();
		
		if( !settings.isEmpty() ){
			for(SocialMediaSettingDO setting : settings)
				localUserIds.add(setting.getCommunityUserId());
		}

		return localUserIds;
	}

	@Override
	public ConnectionRepository createConnectionRepository(String userId) {
		if (userId == null) {
			throw new IllegalArgumentException("userId cannot be null");
		}
		return new HBaseConnectionRepository(userId, socialMediaSettingDao, connectionFactoryLocator, textEncryptor);
	}

}
