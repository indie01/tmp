/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import java.util.List;
import java.util.Set;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.SocialMediaSettingDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.SocialMediaType;

/**
 * ソーシャルメディア連携設定 DAO です。
 * @author kamiike
 *
 */
public interface SocialMediaSettingDao {

	/**
	 * ソーシャルメディア連携設定を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param type タイプ
	 * @return ソーシャルメディア連携設定
	 */
	public SocialMediaSettingDO loadSocialMediaSetting(
			String communityUserId, SocialMediaType type);

	/**
	 * 指定したプロバイダーIDとプロバイダーユーザーID一覧からコミュニティユーザーID一覧を取得する
	 * @param providerId プロバイダーID
	 * @param providerUserIds　 プロバイダーユーザーID一覧
	 * @return
	 */
	public List<SocialMediaSettingDO> findBySocialSettingsByProviderIdAndProviderUserIds(
			String providerId, Set<String> providerUserIds);

	/**
	 * 指定したコミュニティユーザーのソーシャルメディア連携設定を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @return ソーシャルメディア連携設定リスト
	 */
	public List<SocialMediaSettingDO> findBySocialMediaSettingByCommunityUserId(String communityUserId);

	/**
	 * ソーシャルメディア連携設定情報を保存します。
	 * @param socialMediaSetting ソーシャルメディア連携設定情報
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="コミュニティユーザ更新系なので頻度は稀")
	public void saveSocialMediaSetting(SocialMediaSettingDO socialMediaSetting);

	/**
	 * ソーシャルメディア連携設定情報リストを保存します。
	 * @param socialMediaSettings ソーシャルメディア連携設定情報リスト
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="コミュニティユーザ更新系なので頻度は稀")
	public void saveSocialMediaSettings(List<SocialMediaSettingDO> socialMediaSettings);











































	/**
	 * 指定したコミュニティユーザーのソーシャルメディア連携設定を返します。
	 * @param communityUserId  コミュニティユーザーID
	 * @param providerId プロバイダーID
	 * @return ソーシャルメディア連携設定リスト
	 */
	public List<SocialMediaSettingDO> findBySocialMediaSettingByCommunityUserIdAndProviderId(String communityUserId, String providerId);
	/**
	 * 指定したコミュニティユーザーのソーシャルメディア連携設定を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param providerId プロバイダーID
	 * @param providerUserId プロバイダーユーザーID
	 * @return ソーシャルメディア連携設定リスト
	 */
	public List<SocialMediaSettingDO> findBySocialMediaSettingByCommunityUserIdAndProviderIdAndProviderUserId(String communityUserId, String providerId, String providerUserId);

	/**
	 * 指定したコミュニティユーザーのソーシャルメディア連携設定を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param providerId プロバイダーID
	 * @param providerUserIds プロバイダーユーザーID一覧
	 * @return ソーシャルメディア連携設定リスト
	 */
	public List<SocialMediaSettingDO> findBySocialMediaSettingByCommunityUserIdAndProvierIdAndProviderUserIds(String communityUserId, String providerId, List<String> providerUserIds);

	/**
	 * 指定したプロバイダーIDでソーシャルメディア連携設定リストを返します。
	 * @param providerId プロバイダーID
	 * @return ソーシャルメディア連携設定リスト
	 */
	public List<SocialMediaSettingDO> findBySocialMediaSettingByProviderId(String communityUserId, String providerId);

	/**
	 * 指定したプロバイダーIDとプロバイダーユーザーIDでソーシャルメディア連携設定を返します。
	 * @param providerId プロバイダーID
	 * @param providerUserId プロバイダーユーザーID
	 * @return ソーシャルメディア連携設定リスト
	 */
	public List<SocialMediaSettingDO> findBySocialMediaSettingByProviderIdAndProviderUserId(String providerId, Set<String> providerUserIds);

	/**
	 * 指定のソーシャルメディア連携設定を削除する。
	 * @param communityUserId コミュニティユーザーID
	 * @param providerId プロバイダーID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="コミュニティユーザ更新系なので頻度は稀")
	public void removeSocialMediaSettings(String communityUserId, String providerId);

	/**
	 * 指定のソーシャルメディア連携設定を削除する。
	 * @param communityUserId コミュニティユーザーID
	 * @param providerId プロバイダーID
	 * @param providerUserId プロバイダーユーザーID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="コミュニティユーザ更新系なので頻度は稀")
	public void removeSocialMediaSettings(String communityUserId, String providerId, String providerUserId);

	/**
	 * 古い設定を全て破棄します。
	 * @param communityUserId コミュニティユーザーID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="コミュニティユーザ更新系なので頻度は稀")
	public void destroyOldSettings(String communityUserId);
}
