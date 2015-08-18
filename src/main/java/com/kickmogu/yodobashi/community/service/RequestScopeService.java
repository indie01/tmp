package com.kickmogu.yodobashi.community.service;

import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.twitter.api.Twitter;

import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.Verification;

/**
 * リクエストスコープで管理するオブジェクトを扱うサービスです。
 * @author kamiike
 *
 */
public interface RequestScopeService {

	/**
	 * 初期化します。
	 * @param communityUser コミュニティユーザー
	 * @param catalogCookies catalogCookies[0]=autoId、catalogCookies[1]=cartId、catalogCookies[2]=yatpz
	 */
	public void initialize(
			CommunityUserDO communityUser,
			String[] catalogCookies);

	/**
	 * 初期化します。
	 * @param adultVerification アダルト商品表示確認ステータス
	 * @param ceroVerification CERO商品表示確認ステータス
	 */
	public void initialize(
			Verification adultVerification,
			Verification ceroVerification);

	/**
	 * コネクションリポジトリを返します。
	 * @return コネクションリポジトリ
	 */
	public ConnectionRepository getConnectionRepository();

	/**
	 * アダルト商品表示確認ステータスを返します。
	 * @return アダルト商品表示確認ステータス
	 */
	public Verification getAdultVerification();

	/**
	 * CERO商品表示確認ステータスを返します。
	 * @return CERO商品表示確認ステータス
	 */
	public Verification getCeroVerification();

	/**
	 * Facebookインスタンスを返します。
	 * @return Facebookインスタンス
	 */
	public Facebook getFacebook();

	/**
	 * Twitterインスタンスを返します。
	 * @return Twitterインスタンス
	 */
	public Twitter getTwitter();

	/**
	 * 廃棄します。
	 */
	public void destroy();
}
