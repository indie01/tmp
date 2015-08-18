/**
 *
 */
package com.kickmogu.yodobashi.community.resource.domain;

import java.util.List;

import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseIndex;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.yodobashi.community.resource.domain.constants.SocialMediaPublicSettingType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SocialMediaType;


/**
 * ソーシャルメディア連携設定情報です。
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.SMALL,
	excludeBackup=true)
public class SocialMediaSettingDO extends BaseWithTimestampDO {

	/**
	 *
	 */
	private static final long serialVersionUID = 4379921393978445667L;

	/**
	 * ソーシャルメディア連携設定IDです。
	 */
	@HBaseKey
	private String socialMediaSettingId;

	/**
	 * ソーシャルメディアタイプです。
	 */
	@HBaseColumn
	@HBaseIndex(additionalColumns={"socialMediaAccountCode"})
	private SocialMediaType socialMediaType;

	/**
	 * ソーシャルメディアアカウントコードです。
	 */
	@HBaseColumn
	@HBaseIndex(additionalColumns={"socialMediaType"})
	private String socialMediaAccountCode;

	/**
	 * ソーシャルメディアアカウント名です。
	 */
	@HBaseColumn
	private String socialMediaAccountName;

	/**
	 * ソーシャルメディアアカウントURLです。
	 */
	@HBaseColumn
	private String socialMediaAccountUrl;

	/**
	 * ソーシャルメディアアカウントイメージURL
	 */
	@HBaseColumn
	private String socialMediaAccountImageUrl;

	/**
	 * ソーシャルメディアのアクセストークン
	 */
	@HBaseColumn
	private String accessToken;

	/**
	 * ソーシャルメディアのシークレット
	 */
	@HBaseColumn
	private String secret;

	/**
	 * ソーシャルメディアのリフレッシュトークン
	 */
	@HBaseColumn
	private String refreshToken;

	/**
	 * ソーシャルメディアのアクセストークンの有効期限
	 */
	@HBaseColumn
	private Long expireTime;

	/**
	 * 退会データかどうかです。
	 */
	@HBaseColumn
	private boolean withdraw;

	/**
	 * 退会キーです。
	 */
	@HBaseColumn
	@HBaseIndex
	private String withdrawKey;

	/**
	 * コミュニティユーザーIDです。
	 */
	@HBaseColumn
	@HBaseIndex(additionalColumns={"socialMediaType", "socialMediaAccountCode"})
	private String communityUserId;

	/**
	 * 公開設定リストです。
	 */
	@HBaseColumn
	private List<PublicSetting> publicSettings;

	public SocialMediaSettingDO() {
		super();
	}

	public SocialMediaSettingDO(String communityUserId, SocialMediaType socialMediaType) {
		super();
		this.communityUserId = communityUserId;
		this.socialMediaType = socialMediaType;
	}

	/**
	 * @return withdraw
	 */
	public boolean isWithdraw() {
		return withdraw;
	}

	/**
	 * @param withdraw セットする withdraw
	 */
	public void setWithdraw(boolean withdraw) {
		this.withdraw = withdraw;
	}

	/**
	 * 削除済かどうか返します。
	 * @return 削除済の場合、true
	 */
	public boolean isDeleted() {
		return withdraw;
	}

	/**
	 * @return socialMediaType
	 */
	public SocialMediaType getSocialMediaType() {
		return socialMediaType;
	}

	/**
	 * @param socialMediaType セットする socialMediaType
	 */
	public void setSocialMediaType(SocialMediaType socialMediaType) {
		this.socialMediaType = socialMediaType;
	}

	/**
	 * @return socialMediaAccountCode
	 */
	public String getSocialMediaAccountCode() {
		return socialMediaAccountCode;
	}

	/**
	 * @param socialMediaAccountCode セットする socialMediaAccountCode
	 */
	public void setSocialMediaAccountCode(String socialMediaAccountCode) {
		this.socialMediaAccountCode = socialMediaAccountCode;
	}

	/**
	 * @return socialMediaAccountName
	 */
	public String getSocialMediaAccountName() {
		return socialMediaAccountName;
	}

	/**
	 * @param socialMediaAccountName セットする socialMediaAccountName
	 */
	public void setSocialMediaAccountName(String socialMediaAccountName) {
		this.socialMediaAccountName = socialMediaAccountName;
	}

	/**
	 * @return socialMediaAccountUrl
	 */
	public String getSocialMediaAccountUrl() {
		return socialMediaAccountUrl;
	}

	/**
	 * @param socialMediaAccountUrl セットする socialMediaAccountUrl
	 */
	public void setSocialMediaAccountUrl(String socialMediaAccountUrl) {
		this.socialMediaAccountUrl = socialMediaAccountUrl;
	}

	/**
	 * @return socialMediaAccountImageUrl
	 */
	public String getSocialMediaAccountImageUrl() {
		return socialMediaAccountImageUrl;
	}

	/**
	 * @param socialMediaAccountImageUrl
	 */
	public void setSocialMediaAccountImageUrl(String socialMediaAccountImageUrl) {
		this.socialMediaAccountImageUrl = socialMediaAccountImageUrl;
	}

	/**
	 * @return accessToken
	 */
	public String getAccessToken() {
		return accessToken;
	}

	/**
	 * @param accessToken
	 */
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	/**
	 * @return secret
	 */
	public String getSecret() {
		return secret;
	}

	/**
	 * @param secret
	 */
	public void setSecret(String secret) {
		this.secret = secret;
	}

	/**
	 * @return refreshToken
	 */
	public String getRefreshToken() {
		return refreshToken;
	}

	/**
	 * @param refreshToken
	 */
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	/**
	 * @return expireTime
	 */
	public Long getExpireTime() {
		return expireTime;
	}

	/**
	 * @param expireTime
	 */
	public void setExpireTime(Long expireTime) {
		this.expireTime = expireTime;
	}

	/**
	 * @return publicSettings
	 */
	public List<PublicSetting> getPublicSettings() {
		return publicSettings;
	}

	/**
	 * @param publicSettings セットする publicSettings
	 */
	public void setPublicSettings(List<PublicSetting> publicSettings) {
		this.publicSettings = publicSettings;
	}

	/**
	 * 指定した公開設定タイプが公開かどうかです。
	 * @param type 公開設定タイプ
	 * @return 公開の場合、true
	 */
	public boolean isPublic(SocialMediaPublicSettingType type) {
		if (!isLinkFlag()) {
			return false;
		}
		for (PublicSetting publicSetting : getPublicSettings()) {
			if (publicSetting.getType().equals(type)) {
				return publicSetting.isValue();
			}
		}
		return false;
	}

	/**
	 * @return socialMediaSettingId
	 */
	public String getSocialMediaSettingId() {
		return socialMediaSettingId;
	}

	/**
	 * @param socialMediaSettingId セットする socialMediaSettingId
	 */
	public void setSocialMediaSettingId(String socialMediaSettingId) {
		this.socialMediaSettingId = socialMediaSettingId;
	}

	/**
	 * @return communityUserId
	 */
	public String getCommunityUserId() {
		return communityUserId;
	}

	/**
	 * @param communityUserId セットする communityUserId
	 */
	public void setCommunityUserId(String communityUserId) {
		this.communityUserId = communityUserId;
	}

	/**
	 * @return linkFlag
	 */
	public boolean isLinkFlag() {
		return socialMediaAccountCode != null && socialMediaAccountCode.length() > 0;
	}

	public String getWithdrawKey() {
		return withdrawKey;
	}

	public void setWithdrawKey(String withdrawKey) {
		this.withdrawKey = withdrawKey;
	}

}
