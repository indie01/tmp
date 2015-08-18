package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;
import com.kickmogu.web.common.domain.Selectable;


/**
 * レビューソートタイプです。
 */
public enum ReviewSortType implements LabeledEnum<ReviewSortType, String>, Selectable{

	POST_DATE_SORT ("01", "最新順"),
	MATCH_SORT ("02", "適合度順"),
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
	private ReviewSortType(String code, String label) {
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
	public static ReviewSortType codeOf(String code) {
		for (ReviewSortType element : values()) {
			if (element.code.equals(code)) {
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