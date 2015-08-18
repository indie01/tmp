package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * ソーシャルメディアタイプです。
 */
public enum SocialMediaType implements LabeledEnum<SocialMediaType, String>{

	TWITTER ("1", "Twitter", "twitter"),
	FACEBOOK ("3", "Facebook", "facebook"),
	;

	/**
	 * コードです。
	 */
	private String code;

	/**
	 * ラベルです。
	 */
	private String label;

	/**
	 * ProviderIdです
	 */
	private String providerId;

	/**
	 * コンストラクタです。
	 * @param code コード
	 * @param label ラベル
	 */
	private SocialMediaType(String code, String label, String providerId) {
		this.code = code;
		this.label = label;
		this.providerId = providerId;
	}

	/**
	 * コードを返します。
	 * @return コード
	 */
	public String getCode() {
		return code;
	}

	/**
	 * 指定したコードの値を返します。
	 * @param code コード
	 * @return
	 */
	public static SocialMediaType codeOf(String code) {
		for (SocialMediaType element : values()) {
			if (code.equals(element.code)) {
				return element;
			}
		}
		return null;
	}

	public static SocialMediaType providerIdOf(String providerId) {
		for (SocialMediaType element : values()) {
			if (providerId.equals(element.providerId)) {
				return element;
			}
		}
		return null;
	}


	/**
	 * 文字列表現を返します。
	 * @return 文字列表現
	 */
	@Override
	public String toString() {
		return name();
	}

	/**
	 * ラベルを返します。
	 * @return ラベル
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * ProviderIdを返します。
	 * @return ProviderId
	 */
	public String getProviderId() {
		return providerId;
	}
}