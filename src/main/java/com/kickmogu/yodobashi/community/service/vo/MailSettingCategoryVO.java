/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import java.util.List;

import com.google.common.collect.Lists;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSettingType.MailSettingCategory;

/**
 * メール配信設定をカテゴリ毎にまとめたビューオブジェクトです。
 * @author kamiike
 */
public class MailSettingCategoryVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = 9066317656624068862L;

	/**
	 * メール設定カテゴリです。
	 */
	private MailSettingCategory category;

	/**
	 * メール設定リストです。
	 */
	private List<MailSettingVO> mailSettings = Lists.newArrayList();

	/**
	 * @return mailSettings
	 */
	public List<MailSettingVO> getMailSettings() {
		return mailSettings;
	}

	/**
	 * @param mailSettings セットする mailSettings
	 */
	public void setMailSettings(List<MailSettingVO> mailSettings) {
		this.mailSettings = mailSettings;
	}

	/**
	 * @return category
	 */
	public MailSettingCategory getCategory() {
		return category;
	}

	/**
	 * @param category セットする category
	 */
	public void setCategory(MailSettingCategory category) {
		this.category = category;
	}
}
