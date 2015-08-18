package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * フォロー情報パラメータータイプです。
 */
public enum FollowInfoParameterType implements LabeledEnum<FollowInfoParameterType, String>{

	FOLLOW_USER("followUser", "フォローユーザー"),
	FOLLOWER("follower", "フォロワー"),
	REVIEW("review", "レビュー"),
	QUESTION("question", "質問"),
	QUESTION_ANSWER("questionAnswer", "質問回答"),
	PRODUCT("product", "商品"),
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
	private FollowInfoParameterType(String code, String label) {
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
	public static FollowInfoParameterType codeOf(String code) {
		for (FollowInfoParameterType element : values()) {
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