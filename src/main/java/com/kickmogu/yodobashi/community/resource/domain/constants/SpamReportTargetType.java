package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * 違反報告の対象タイプです。
 */
public enum SpamReportTargetType implements LabeledEnum<SpamReportTargetType, String>{

	REVIEW ("1", "レビュー"),
	QUESTION ("2", "質問"),
	QUESTION_ANSWER ("3", "質問回答"),
	IMAGE ("4", "画像単体"),
	COMMENT ("5", "コメント"),
	IMAGESET("6", "画像投稿"),
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
	private SpamReportTargetType(String code, String label) {
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
	public static SpamReportTargetType codeOf(String code) {
		for (SpamReportTargetType element : values()) {
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