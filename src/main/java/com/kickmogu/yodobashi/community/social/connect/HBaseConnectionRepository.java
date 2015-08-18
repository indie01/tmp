package com.kickmogu.yodobashi.community.social.connect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.NoSuchConnectionException;
import org.springframework.social.connect.NotConnectedException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.kickmogu.yodobashi.community.resource.dao.SocialMediaSettingDao;
import com.kickmogu.yodobashi.community.resource.domain.SocialMediaSettingDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.SocialMediaType;

class HBaseConnectionRepository implements ConnectionRepository {
	
	private final String userId;
	
	private final SocialMediaSettingDao socialMediaSettingDao;
	
	private final ConnectionFactoryLocator connectionFactoryLocator;

	private final TextEncryptor textEncryptor;
	
	public HBaseConnectionRepository(String userId, SocialMediaSettingDao socialMediaSettingDao, ConnectionFactoryLocator connectionFactoryLocator, TextEncryptor textEncryptor) {
		this.userId = userId;
		this.socialMediaSettingDao = socialMediaSettingDao;
		this.connectionFactoryLocator = connectionFactoryLocator;
		this.textEncryptor = textEncryptor;
	}

	@Override
	public MultiValueMap<String, Connection<?>> findAllConnections() {
		List<SocialMediaSettingDO> settings = socialMediaSettingDao.findBySocialMediaSettingByCommunityUserId(userId);
		List<Connection<?>> resultList = mapConnections(settings);
		
		MultiValueMap<String, Connection<?>> connections = new LinkedMultiValueMap<String, Connection<?>>();
		Set<String> registeredProviderIds = connectionFactoryLocator.registeredProviderIds();
		for (String registeredProviderId : registeredProviderIds) {
			connections.put(registeredProviderId, Collections.<Connection<?>>emptyList());
		}
		for (Connection<?> connection : resultList) {
			String providerId = connection.getKey().getProviderId();
			if (connections.get(providerId).size() == 0) {
				connections.put(providerId, new LinkedList<Connection<?>>());
			}
			connections.add(providerId, connection);
		}
		return connections;
	}

	@Override
	public List<Connection<?>> findConnections(String providerId) {
		List<SocialMediaSettingDO> settings = socialMediaSettingDao.findBySocialMediaSettingByProviderId(userId, providerId);
		List<Connection<?>> resultList = mapConnections(settings);
		return resultList;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <A> List<Connection<A>> findConnections(Class<A> apiType) {
		List<?> connections = findConnections(getProviderId(apiType));
		return (List<Connection<A>>) connections;
	}

	@Override
	public MultiValueMap<String, Connection<?>> findConnectionsToUsers(MultiValueMap<String, String> providerUsers) {
		if (providerUsers.isEmpty()) {
			throw new IllegalArgumentException("Unable to execute find: no providerUsers provided");
		}
		List<Connection<?>> resultList = new ArrayList<Connection<?>>();
		
		for (Iterator<Entry<String, List<String>>> it = providerUsers.entrySet().iterator(); it.hasNext();) {
			Entry<String, List<String>> entry = it.next();
			List<SocialMediaSettingDO> settings = socialMediaSettingDao.findBySocialMediaSettingByCommunityUserIdAndProvierIdAndProviderUserIds(userId, entry.getKey(), entry.getValue());
			if( settings.isEmpty() )
				continue;
			resultList.addAll(mapConnections(settings));
		}
		
		MultiValueMap<String, Connection<?>> connectionsForUsers = new LinkedMultiValueMap<String, Connection<?>>();
		for (Connection<?> connection : resultList) {
			String providerId = connection.getKey().getProviderId();
			List<String> userIds = providerUsers.get(providerId);
			List<Connection<?>> connections = connectionsForUsers.get(providerId);
			if (connections == null) {
				connections = new ArrayList<Connection<?>>(userIds.size());
				for (int i = 0; i < userIds.size(); i++) {
					connections.add(null);
				}
				connectionsForUsers.put(providerId, connections);
			}
			String providerUserId = connection.getKey().getProviderUserId();
			int connectionIndex = userIds.indexOf(providerUserId);
			connections.set(connectionIndex, connection);
		}
		return connectionsForUsers;
	}

	@Override
	public Connection<?> getConnection(ConnectionKey connectionKey) {
		List<SocialMediaSettingDO> settings = socialMediaSettingDao.findBySocialMediaSettingByCommunityUserIdAndProviderIdAndProviderUserId(userId, connectionKey.getProviderId(), connectionKey.getProviderUserId());
		if( settings.isEmpty())
			throw new NoSuchConnectionException(connectionKey);
		
		return mapConnection(settings.get(0));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <A> Connection<A> getConnection(Class<A> apiType,String providerUserId) {
		String providerId = getProviderId(apiType);
		return (Connection<A>) getConnection(new ConnectionKey(providerId, providerUserId));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <A> Connection<A> getPrimaryConnection(Class<A> apiType) {
		String providerId = getProviderId(apiType);
		Connection<A> connection = (Connection<A>) findPrimaryConnection(providerId);
		if (connection == null) {
			throw new NotConnectedException(providerId);
		}
		return connection;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <A> Connection<A> findPrimaryConnection(Class<A> apiType) {
		String providerId = getProviderId(apiType);
		return (Connection<A>) findPrimaryConnection(providerId);
	}

	@Override
	public void addConnection(Connection<?> connection) {
		ConnectionData data = connection.createData();
		SocialMediaSettingDO setting = mapSocialMediaSetting(userId, data);
		socialMediaSettingDao.saveSocialMediaSetting(setting);
	}

	@Override
	public void updateConnection(Connection<?> connection) {
		ConnectionData data = connection.createData();
		SocialMediaSettingDO setting = mapSocialMediaSetting(userId, data);
		socialMediaSettingDao.saveSocialMediaSetting(setting);
	}

	@Override
	public void removeConnections(String providerId) {
		socialMediaSettingDao.removeSocialMediaSettings(userId, providerId);
	}

	@Override
	public void removeConnection(ConnectionKey connectionKey) {
		socialMediaSettingDao.removeSocialMediaSettings(userId, connectionKey.getProviderId(), connectionKey.getProviderUserId());
	}
	
	// internal helpers
	
	private Connection<?> findPrimaryConnection(String providerId) {
		List<SocialMediaSettingDO> settings = socialMediaSettingDao.findBySocialMediaSettingByCommunityUserIdAndProviderId(userId, providerId);
		if( settings == null || settings.isEmpty() )
			return null;
		
		return mapConnection(settings.get(0));
	}
	
	private List<Connection<?>> mapConnections(List<SocialMediaSettingDO> settings){
		List<Connection<?>> connections = new ArrayList<Connection<?>>();
		
		if( settings == null || settings.isEmpty())
			return connections;
		
		for(SocialMediaSettingDO setting : settings)
			connections.add(mapConnection(setting));
				
		return connections;
	}
	
	private Connection<?> mapConnection(SocialMediaSettingDO setting) {
		ConnectionData connectionData = mapConnectionData(setting);
		ConnectionFactory<?> connectionFactory = connectionFactoryLocator.getConnectionFactory(connectionData.getProviderId());
		return connectionFactory.createConnection(connectionData);
	}
	
	private ConnectionData mapConnectionData(SocialMediaSettingDO setting) {
		return new ConnectionData(
				setting.getSocialMediaType().getProviderId(), 
				setting.getSocialMediaAccountCode(),
				setting.getSocialMediaAccountName(),
				setting.getSocialMediaAccountUrl(),
				setting.getSocialMediaAccountImageUrl(),
				decrypt(setting.getAccessToken()), 
				decrypt(setting.getSecret()), 
				decrypt(setting.getRefreshToken()), 
				expireTime(setting.getExpireTime()));
	}
	
	private SocialMediaSettingDO mapSocialMediaSetting(String userId, ConnectionData connectionData){
		SocialMediaSettingDO result = new SocialMediaSettingDO();
		
		result.setCommunityUserId(userId);
		result.setSocialMediaType(SocialMediaType.providerIdOf(connectionData.getProviderId()));
		result.setSocialMediaAccountCode(connectionData.getProviderUserId());
		result.setSocialMediaAccountName(connectionData.getDisplayName());
		result.setSocialMediaAccountUrl(connectionData.getProfileUrl());
		result.setSocialMediaAccountImageUrl(connectionData.getImageUrl());
		result.setAccessToken(encrypt(connectionData.getAccessToken()));
		result.setSecret(encrypt(connectionData.getSecret()));
		result.setRefreshToken(encrypt(connectionData.getRefreshToken()));
		result.setExpireTime(connectionData.getExpireTime());
		
		return result;
	}
	
	private String decrypt(String encryptedText) {
		return encryptedText != null ? textEncryptor.decrypt(encryptedText) : encryptedText;
	}
	
	private Long expireTime(Long expireTime) {
		return expireTime != null ? (expireTime == 0 ? null : expireTime) : null;
	}
	
	private <A> String getProviderId(Class<A> apiType) {
		return connectionFactoryLocator.getConnectionFactory(apiType).getProviderId();
	}
	
	private String encrypt(String text) {
		return text != null ? textEncryptor.encrypt(text) : text;
	}

}
