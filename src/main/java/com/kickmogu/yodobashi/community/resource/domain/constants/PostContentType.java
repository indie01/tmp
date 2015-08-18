package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * 投稿タイプ
 */
public enum PostContentType implements LabeledEnum<PostContentType, String>{

	REVIEW ("1", "レビュー投稿"),
	QUESTION ("2", "Q&A質問"),
	ANSWER ("3", "Q&A回答"),
	PROFILE ("4", "コミュニティプロフィール"),
	PROFILE_THUMBNAIL ("5", "コミュニティプロフィールサムネイル"),
	IMAGE_SET ("6", "商品画像セット"),
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
	private PostContentType(String code, String label) {
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
	public static PostContentType codeOf(String code) {
		for (PostContentType element : values()) {
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