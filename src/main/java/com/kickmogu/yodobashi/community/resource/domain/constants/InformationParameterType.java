package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * お知らせパラメータータイプです。
 */
public enum InformationParameterType implements LabeledEnum<InformationParameterType, String>{

	FOLLOWER("follower", "フォロワー"),
	REVIEW("review", "レビュー"),
	REVIEW_LIKE("reviewLike", "レビューいいね"),
	QUESTION("question", "質問"),
	QUESTION_ANSWER("questionAnswer", "質問回答"),
	QUESTION_ANSWER_LIKE("questionAnswerLike", "質問回答いいね"),
	PRODUCT_MASTER("productMaster", "商品マスター"),
	COMMENT("comment", "コメント"),
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
	private InformationParameterType(String code, String label) {
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
	public static InformationParameterType codeOf(String code) {
		for (InformationParameterType element : values()) {
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