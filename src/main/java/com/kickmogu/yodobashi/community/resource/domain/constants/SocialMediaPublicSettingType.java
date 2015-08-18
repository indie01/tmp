package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * ソーシャルメディア公開設定タイプです。
 */
public enum SocialMediaPublicSettingType implements LabeledEnum<SocialMediaPublicSettingType, String>{
	REVIEW("1", "レビューを{0}に自動的に公開"),
	QUESTION("2", "Q&Aの質問を{0}に自動的に公開"),
	ANSWER("3", "Q&Aの回答を{0}に自動的に公開"),
	IMAGE("4", "投稿画像を{0}に自動的に公開"),
	RANKING("5", "Top50製品マスターにランクインしたことを{0}に自動的に公開"),
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
	 * コンストラクタです。
	 * @param code コード
	 * @param label ラベル
	 */
	private SocialMediaPublicSettingType(String code, String label) {
		this.code = code;
		this.label = label;
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
	public static SocialMediaPublicSettingType codeOf(String code) {
		for (SocialMediaPublicSettingType element : values()) {
			if (code.equals(element.code)) {
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

}