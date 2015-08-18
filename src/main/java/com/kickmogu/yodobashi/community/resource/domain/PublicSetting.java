package com.kickmogu.yodobashi.community.resource.domain;

import org.msgpack.annotation.Message;

import com.kickmogu.lib.core.resource.domain.BaseDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.SocialMediaPublicSettingType;

/**
 * 公開設定です。
 * @author kamiike
 */
@Message
public class PublicSetting extends BaseDO {
	/**
	 *
	 */
	private static final long serialVersionUID = -8498534592396203183L;

	/**
	 * 公開設定タイプです。
	 */
	protected SocialMediaPublicSettingType type;

	/**
	 * 値（true：公開する、false：非公開）
	 */
	protected boolean value;

	/**
	 * @return type
	 */
	public SocialMediaPublicSettingType getType() {
		return type;
	}

	/**
	 * @param type セットする type
	 */
	public void setType(SocialMediaPublicSettingType type) {
		this.type = type;
	}

	/**
	 * @return value
	 */
	public boolean isValue() {
		return value;
	}

	/**
	 * @param value セットする value
	 */
	public void setValue(boolean value) {
		this.value = value;
	}

}
