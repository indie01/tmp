package com.kickmogu.yodobashi.community.resource.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.SocialMediaType;
import com.kickmogu.yodobashi.community.resource.domain.constants.Verification;

/**
 * リクエストスコープで管理するオブジェクトを扱う DAO の実装です。
 * @author kamiike
 *
 */
@Service
public class RequestScopeDaoImpl implements RequestScopeDao {

	/**
	 * コミュニティユーザーを管理します。
	 */
	private ThreadLocal<CommunityUserDO> communityUserHolder = new ThreadLocal<CommunityUserDO>();

	/**
	 * Catalog Cookieを管理します。
	 */
	private ThreadLocal<String[]> catalogCookiesHolder = new ThreadLocal<String[]>();

	/**
	 * アダルト商品表示確認ステータスを管理します。
	 */
	private ThreadLocal<Verification> adultHolder = new ThreadLocal<Verification>();

	/**
	 * CERO商品表示確認ステータスを管理します。
	 */
	private ThreadLocal<Verification> ceroHolder = new ThreadLocal<Verification>();

	/**
	 * ユーザーコネクションリポジトリです。
	 */
	@Autowired
	private UsersConnectionRepository usersConnectionRepository;

	/**
	 * 初期化します。
	 * @param communityUser コミュニティユーザー
	 * @param catalogCookies catalogCookies[0]=autoId、catalogCookies[1]=cartId、catalogCookies[2]=yatpz
	 */
	@Override
	public void initialize(
			CommunityUserDO communityUser,
			String[] catalogCookies) {
		if( communityUser != null )
			communityUserHolder.set(communityUser);
		catalogCookiesHolder.set(catalogCookies);
	}

	/**
	 * 初期化します。
	 * @param adultVerification アダルト商品表示確認ステータス
	 * @param ceroVerification CERO商品表示確認ステータス
	 */
	@Override
	public void initialize(
			Verification adultVerification,
			Verification ceroVerification) {
		adultHolder.set(adultVerification);
		ceroHolder.set(ceroVerification);
	}

	/**
	 * 保持しているコミュニティユーザー情報を返します。
	 * @return コミュニティユーザー情報
	 */
	@Override
	public CommunityUserDO loadCommunityUser() {
		return communityUserHolder.get();
	}

	/**
	 * アダルト商品表示確認ステータスを返します。
	 * @return アダルト商品表示確認ステータス
	 */
	@Override
	public Verification loadAdultVerification() {
		CommunityUserDO communityUser = loadCommunityUser();
		if (communityUser != null) {
			Verification verification = communityUser.getAdultVerification();
			if (verification == Verification.ATANYTIME && communityUser.isTemporaryAuthorized()) {
				// 認証設定が「随時」で一時的にアダルト認証されている場合、AUTHORIZEDに置き換えて返す
				return Verification.AUTHORIZED;
			}
			return verification;
		} else {
			return adultHolder.get();
		}
	}

	/**
	 * CERO商品表示確認ステータスを返します。
	 * @return CERO商品表示確認ステータス
	 */
	@Override
	public Verification loadCeroVerification() {
		CommunityUserDO communityUser = loadCommunityUser();
		if (communityUser != null) {
			Verification verification = communityUser.getCeroVerificationWithAdultState();
			if (verification == Verification.ATANYTIME && communityUser.isTemporaryAuthorized()) {
				// 認証設定が「随時」で一時的にアダルト認証されている場合、AUTHORIZEDに置き換えて返す
				return Verification.AUTHORIZED;
			}
			return verification;
		} else {
			return ceroHolder.get();
		}
	}

	/**
	 * 保持しているコミュニティユーザーIDを返します。
	 * @return コミュニティユーザーID
	 */
	@Override
	public String loadCommunityUserId() {
		CommunityUserDO communityUser = loadCommunityUser();
		if (communityUser != null) {
			return communityUser.getCommunityUserId();
		} else {
			return null;
		}
	}

	/**
	 * コネクションリポジトリを返します。
	 * @return コネクションリポジトリ
	 */
	@Override
	public ConnectionRepository loadConnectionRepository() {
		CommunityUserDO communityUser = communityUserHolder.get();
		if (communityUser == null) {
			throw new IllegalStateException(
					"Unable to get a ConnectionRepository: no user signed in");
		}

		return usersConnectionRepository.createConnectionRepository(
				communityUser.getCommunityUserId());
	}

	/**
	 * Facebookインスタンスを返します。
	 * @return Facebookインスタンス
	 */
	@Override
	@ArroundHBase
	public Facebook loadFacebookClient() {
		ConnectionRepository connectionRepository = loadConnectionRepository();
		Connection<Facebook> connection = connectionRepository.findPrimaryConnection(Facebook.class);
		if (connection == null) {
			return null;
		}
		
		try{
			connection.getApi().userOperations().getUserProfile();
		}catch (Exception e) {
			connectionRepository.removeConnection(connection.getKey());
			return null;
		}
		
		return connection.getApi();
	}

	/**
	 * Twitterインスタンスを返します。
	 * @return Twitterインスタンス
	 */
	@Override
	public Twitter loadTwitterClient() {
		ConnectionRepository connectionRepository = loadConnectionRepository();
		Connection<Twitter> connection = connectionRepository.findPrimaryConnection(
				Twitter.class);
		if (connection == null) {
			return null;
		}
		if (connection.hasExpired()) {
			connectionRepository.removeConnections(SocialMediaType.TWITTER.getProviderId());
			return null;
		}else{
			try{
				connection.getApi().userOperations().getUserProfile();
			}catch (Exception e) {
				connectionRepository.removeConnection(connection.getKey());
				return null;
			}
		}
		return connection.getApi();
	}

	/**
	 * カタログクッキーを返します。
	 * @return カタログクッキー
	 */
	@Override
	public String[] getCatalogCookies() {
		String[] catalogCookies = catalogCookiesHolder.get();
		if (catalogCookies == null) {
			catalogCookies = new String[]{null, null, null};
		}
		return catalogCookies;
	}

	/**
	 * 廃棄します。
	 */
	@Override
	public void destroy() {
		communityUserHolder.remove();
		catalogCookiesHolder.remove();
		adultHolder.remove();
		ceroHolder.remove();
	}
}
