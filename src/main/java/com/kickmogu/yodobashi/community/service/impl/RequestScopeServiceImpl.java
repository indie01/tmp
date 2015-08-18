package com.kickmogu.yodobashi.community.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.stereotype.Service;

import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.Verification;
import com.kickmogu.yodobashi.community.service.RequestScopeService;

/**
 * リクエストスコープで管理するオブジェクトを扱うサービスの実装です。
 * @author kamiike
 *
 */
@Service
public class RequestScopeServiceImpl implements RequestScopeService {

	/**
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	private RequestScopeDao requestScopeDao;

	/**
	 * 初期化します。
	 * @param communityUser コミュニティユーザー
	 * @param catalogCookies catalogCookies[0]=autoId、catalogCookies[1]=cartId、catalogCookies[2]=yatpz
	 */
	@Override
	public void initialize(
			CommunityUserDO communityUser,
			String[] catalogCookies) {
		requestScopeDao.initialize(communityUser, catalogCookies);
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
		requestScopeDao.initialize(adultVerification, ceroVerification);
	}

	/**
	 * コネクションリポジトリを返します。
	 * @return コネクションリポジトリ
	 */
	@Override
	public ConnectionRepository getConnectionRepository() {
		return requestScopeDao.loadConnectionRepository();
	}

	/**
	 * アダルト商品表示確認ステータスを返します。
	 * @return アダルト商品表示確認ステータス
	 */
	@Override
	public Verification getAdultVerification() {
		return requestScopeDao.loadAdultVerification();
	}

	/**
	 * CERO商品表示確認ステータスを返します。
	 * @return CERO商品表示確認ステータス
	 */
	@Override
	public Verification getCeroVerification() {
		return requestScopeDao.loadCeroVerification();
	}

	/**
	 * Facebookインスタンスを返します。
	 * @return Facebookインスタンス
	 */
	@Override
	public Facebook getFacebook() {
		return requestScopeDao.loadFacebookClient();
	}

	/**
	 * Twitterインスタンスを返します。
	 * @return Twitterインスタンス
	 */
	@Override
	public Twitter getTwitter() {
		return requestScopeDao.loadTwitterClient();
	}

	/**
	 * 廃棄します。
	 */
	@Override
	public void destroy() {
		requestScopeDao.destroy();
	}
}
