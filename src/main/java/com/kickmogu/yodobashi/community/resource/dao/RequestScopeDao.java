package com.kickmogu.yodobashi.community.resource.dao;

import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.twitter.api.Twitter;

import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.Verification;

/**
 * リクエストスコープで管理するオブジェクトを扱う DAO です。
 * @author kamiike
 *
 */
public interface RequestScopeDao {

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
	 * 保持しているコミュニティユーザー情報を返します。
	 * @return コミュニティユーザー情報
	 */
	public CommunityUserDO loadCommunityUser();

	/**
	 * 保持しているコミュニティユーザーIDを返します。
	 * @return コミュニティユーザーID
	 */
	public String loadCommunityUserId();

	/**
	 * アダルト商品表示確認ステータスを返します。
	 * @return アダルト商品表示確認ステータス
	 */
	public Verification loadAdultVerification();

	/**
	 * CERO商品表示確認ステータスを返します。
	 * @return CERO商品表示確認ステータス
	 */
	public Verification loadCeroVerification();

	/**
	 * コネクションリポジトリを返します。
	 * @return コネクションリポジトリ
	 */
	public ConnectionRepository loadConnectionRepository();

	/**
	 * Facebookインスタンスを返します。
	 * @return Facebookインスタンス
	 */
	public Facebook loadFacebookClient();

	/**
	 * Twitterインスタンスを返します。
	 * @return Twitterインスタンス
	 */
	public Twitter loadTwitterClient();

	/**
	 * カタログクッキーを返します。
	 * @return カタログクッキー
	 */
	public String[] getCatalogCookies();

	/**
	 * 廃棄します。
	 */
	public void destroy();
}
