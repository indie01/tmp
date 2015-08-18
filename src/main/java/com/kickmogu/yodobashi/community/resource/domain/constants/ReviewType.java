package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;
import com.kickmogu.web.common.domain.Selectable;

/**
 * レビュータイプです。
 */
public enum ReviewType implements LabeledEnum<ReviewType, String>, Selectable{

	//10/3売上の場合、10/16までが購入直後で、10/17の0:00は購入直後ではない。
	REVIEW_IMMEDIATELY_AFTER_PURCHASE ("1", "購入直後レビュー（14日以内）"),
	REVIEW_AFTER_FEW_DAYS ("2", "経過後X日レビュー（15日以降）"),
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
	private ReviewType(String code, String label) {
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
	public static ReviewType codeOf(String code) {
		for (ReviewType element : values()) {
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