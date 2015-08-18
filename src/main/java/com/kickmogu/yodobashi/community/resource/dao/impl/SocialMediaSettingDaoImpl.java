/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FilterList.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.hadoop.hbase.HBaseFilterBuilder;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.yodobashi.community.resource.dao.SocialMediaSettingDao;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.domain.PublicSetting;
import com.kickmogu.yodobashi.community.resource.domain.SocialMediaSettingDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.SocialMediaPublicSettingType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SocialMediaType;

/**
 * ソーシャルメディア連携設定 DAO です。
 * @author kamiike
 *
 */
@Service
public class SocialMediaSettingDaoImpl implements SocialMediaSettingDao {

	/**
	 * HBaseアクセサです。
	 */
	@Autowired  @Qualifier("default")
	private HBaseOperations hBaseOperations;

	/**
	 * タイムスタンプホルダーです。
	 */
	@Autowired
	private TimestampHolder timestampHolder;

	/**
	 * ソーシャルメディア連携設定を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param type タイプ
	 * @return ソーシャルメディア連携設定
	 */
	@Override
	public SocialMediaSettingDO loadSocialMediaSetting(
			String communityUserId, SocialMediaType type) {
		return hBaseOperations.load(SocialMediaSettingDO.class,
				createSocialMediaSettingId(communityUserId, type));
	}

	@Override
	public List<SocialMediaSettingDO> findBySocialSettingsByProviderIdAndProviderUserIds(String providerId, Set<String> providerUserIds){
		HBaseFilterBuilder hBaseFilterBuilder = hBaseOperations.createFilterBuilder(SocialMediaSettingDO.class,Operator.MUST_PASS_ONE);
		hBaseFilterBuilder.includeColumnValues("socialMediaAccountCode", providerUserIds.toArray());

		List<SocialMediaSettingDO> settings = hBaseOperations.scanWithIndex(
				SocialMediaSettingDO.class,
				"socialMediaType",
				SocialMediaType.providerIdOf(providerId),
				hBaseFilterBuilder.toFilter()
				);

		return settings;
	}

	/**
	 * 指定したコミュニティユーザーのソーシャルメディア連携設定を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @return ソーシャルメディア連携設定リスト
	 */
	@Override
	public List<SocialMediaSettingDO> findBySocialMediaSettingByCommunityUserId(String communityUserId) {
		List<String> socialMediaSettingIds = new ArrayList<String>();
		for (SocialMediaType type : SocialMediaType.values()) {
			socialMediaSettingIds.add(
					createSocialMediaSettingId(communityUserId, type));
		}
		List<SocialMediaSettingDO> result = new ArrayList<SocialMediaSettingDO>();
		Map<String, SocialMediaSettingDO> resultMap
				= hBaseOperations.find(SocialMediaSettingDO.class,
						String.class, socialMediaSettingIds);
		for (int i = 0; i < socialMediaSettingIds.size(); i++) {
			String socialMediaSettingId = socialMediaSettingIds.get(i);
			SocialMediaSettingDO setting = null;
			if (resultMap.containsKey(socialMediaSettingId)) {
				setting = resultMap.get(socialMediaSettingId);
			} else {
				setting = new SocialMediaSettingDO();
				setting.setSocialMediaType(SocialMediaType.values()[i]);
				setting.setCommunityUserId(communityUserId);
			}
			mapPublicSettings(setting);
			result.add(setting);
		}
		return result;
	}

	/**
	 * ソーシャルメディア連携設定情報を保存します。
	 * @param socialMediaSetting ソーシャルメディア連携設定情報
	 */
	@Override
	public void saveSocialMediaSetting(SocialMediaSettingDO socialMediaSetting) {
		List<SocialMediaSettingDO> settings = new ArrayList<SocialMediaSettingDO>();
		settings.add(socialMediaSetting);
		saveSocialMediaSettings(settings);
	}

	/**
	 * ソーシャルメディア連携設定情報リストを保存します。
	 * @param socialMediaSettings ソーシャルメディア連携設定情報リスト
	 */
	@Override
	public void saveSocialMediaSettings(List<SocialMediaSettingDO> socialMediaSettings) {
		List<SocialMediaSettingDO> updateSocialMediaSettings = new ArrayList<SocialMediaSettingDO>();

		for (SocialMediaSettingDO socialMediaSetting : socialMediaSettings) {
			if( StringUtils.isEmpty(socialMediaSetting.getSocialMediaAccountCode()))
				continue;

			if (StringUtils.isEmpty(socialMediaSetting.getSocialMediaSettingId())) {
				socialMediaSetting.setSocialMediaSettingId(
						createSocialMediaSettingId(
								socialMediaSetting.getCommunityUserId(),
								socialMediaSetting.getSocialMediaType()));
				socialMediaSetting.setRegisterDateTime(timestampHolder.getTimestamp());
			}
			socialMediaSetting.setModifyDateTime(timestampHolder.getTimestamp());
			updateSocialMediaSettings.add(socialMediaSetting);
		}

		if( !updateSocialMediaSettings.isEmpty())
			hBaseOperations.save(SocialMediaSettingDO.class, updateSocialMediaSettings);
	}

	@Override
	public List<SocialMediaSettingDO> findBySocialMediaSettingByCommunityUserIdAndProviderId(String communityUserId, String providerId){
		return findBySocialMediaSettingByCommunityUserIdAndProviderIdAndProviderUserId(communityUserId, providerId, null);
	}

	@Override
	public List<SocialMediaSettingDO> findBySocialMediaSettingByCommunityUserIdAndProviderIdAndProviderUserId(String communityUserId, String providerId, String providerUserId) {
		List<SocialMediaSettingDO> result = findSocialMediaSettingsByFilter(communityUserId, providerId, providerUserId);
		mapPublicSettings(result);
		return result;
	}

	@Override
	public List<SocialMediaSettingDO> findBySocialMediaSettingByCommunityUserIdAndProvierIdAndProviderUserIds(String communityUserId, String providerId, List<String> providerUserIds) {
		List<SocialMediaSettingDO> result = new ArrayList<SocialMediaSettingDO>();
		List<SocialMediaSettingDO> settings = null;
		for(String providerUserId : providerUserIds){
			settings = findSocialMediaSettingsByFilter(communityUserId, providerId, providerUserId);
			if( !settings.isEmpty() )
				result.addAll(settings);
		}
		// 公開設定をセットする
		mapPublicSettings(result);
		return result;
	}

	@Override
	public List<SocialMediaSettingDO> findBySocialMediaSettingByProviderId(String communityUserId, String providerId) {
		// 対象のソーシャルメディア連携設定リストを取得する
		List<SocialMediaSettingDO> result =findSocialMediaSettingsByFilter(communityUserId, providerId, null);
		// 公開設定をセットする
		mapPublicSettings(result);
		return result;
	}

	@Override
	public List<SocialMediaSettingDO> findBySocialMediaSettingByProviderIdAndProviderUserId(String providerId, Set<String> providerUserIds) {
		HBaseFilterBuilder hBaseFilterBuilder = hBaseOperations.createFilterBuilder(SocialMediaSettingDO.class,Operator.MUST_PASS_ONE);
		hBaseFilterBuilder.includeColumnValues("socialMediaAccountCode", providerUserIds.toArray());
		List<SocialMediaSettingDO> result = hBaseOperations.scanWithIndex(
				SocialMediaSettingDO.class,
				"socialMediaType",
				SocialMediaType.providerIdOf(providerId),
				hBaseFilterBuilder.toFilter());

		// 公開設定をセットする
		mapPublicSettings(result);

		return result;
	}

	@Override
	public void removeSocialMediaSettings(String communityUserId,String providerId) {
		removeSocialMediaSettings(communityUserId, providerId, null);
	}

	@Override
	public void removeSocialMediaSettings(String communityUserId,String providerId, String providerUserId) {
		// 削除対象のソーシャルメディア連携設定リストを取得する
		List<SocialMediaSettingDO> settings = findSocialMediaSettingsByFilter(communityUserId, providerId, providerUserId);
		// 削除処理
		for( SocialMediaSettingDO setting : settings)
			hBaseOperations.deleteByKey(SocialMediaSettingDO.class, setting.getSocialMediaSettingId());
	}

	/**
	 * 古い設定を全て破棄します。
	 * @param communityUserId コミュニティユーザーID
	 */
	@Override
	public void destroyOldSettings(String communityUserId) {
		hBaseOperations.scanDeleteWithIndex(
				SocialMediaSettingDO.class, "communityUserId", communityUserId);
	}

	/**
	 * ソーシャルメディア連携設定IDを新規に生成します。
	 * @param communityUserId コミュニティユーザーID
	 * @param socialMediaType ソーシャルメディアタイプ
	 * @return ソーシャルメディア連携設定ID
	 */
	private String createSocialMediaSettingId(String communityUserId, SocialMediaType socialMediaType) {
		return IdUtil.createIdByConcatIds(communityUserId, socialMediaType.getCode());
	}

	private List<SocialMediaSettingDO> findSocialMediaSettingsByFilter(String communityUserId, String providerId, String providerUserId){
		// 検索フィルターを設定する
		HBaseFilterBuilder hBaseFilterBuilder = hBaseOperations.createFilterBuilder(SocialMediaSettingDO.class);
		if( providerId != null )
			hBaseFilterBuilder.appendSingleColumnValueFilter("socialMediaType", CompareOp.EQUAL, SocialMediaType.providerIdOf(providerId));
		if( providerUserId != null )
			hBaseFilterBuilder.appendSingleColumnValueFilter("socialMediaAccountCode", CompareOp.EQUAL, providerUserId);
		// 対象のソーシャルメディア連携設定リストを取得する
		return hBaseOperations.scanWithIndex(
				SocialMediaSettingDO.class,
				"communityUserId",
				communityUserId,
				hBaseFilterBuilder.toFilter());
	}
	/**
	 * 公開設定を設定する
	 * @param settings　ソーシャルメディア連携設定リスト
	 */
	private void mapPublicSettings(List<SocialMediaSettingDO> settings){
		for(SocialMediaSettingDO setting : settings)
			mapPublicSettings(setting);
	}
	/**
	 * 公開設定を設定する
	 * @param setting ソーシャルメディア連携設定
	 */
	private void mapPublicSettings(SocialMediaSettingDO setting){
		Map<SocialMediaPublicSettingType, PublicSetting> map
		= new HashMap<SocialMediaPublicSettingType, PublicSetting>();
		if (setting.getPublicSettings() != null) {
			for (PublicSetting publicSetting : setting.getPublicSettings()) {
				map.put(publicSetting.getType(), publicSetting);
			}
		}
		List<PublicSetting> publicSettings = new ArrayList<PublicSetting>();
		for (SocialMediaPublicSettingType type : SocialMediaPublicSettingType.values()) {
			PublicSetting publicSetting = null;
			if (map.containsKey(type)) {
				publicSetting = map.get(type);
			} else {
				publicSetting = new PublicSetting();
				publicSetting.setType(type);
			}
			publicSettings.add(publicSetting);
		}
		setting.setPublicSettings(publicSettings);
	}
}
