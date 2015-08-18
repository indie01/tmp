package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * 受注明細カテゴリです。

01 通常明細
02 メーカ直送
03 サービス
04 セット明細(親)
05 メーカ直送サービス
06 他社サービス品
07 配送料
 */
public enum SlipDetailCategory implements LabeledEnum<SlipDetailCategory, String>{

	NORMAL("01", "通常明細"),
	MAKER_DIRECT("02", "メーカ直送"),
	SERVICE("03", "サービス"),
	SET_PARENT("04", "セット明細(親)"),
	MAKER_DIRECT_SERVICE("05", "メーカ直送サービス"),
	OTHER_COMPANY_SERVICE("06", "他社サービス品"),
	CARRIAGE("07", "配送料"),
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
	private SlipDetailCategory(String code, String label) {
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
	public static SlipDetailCategory codeOf(String code) {
		for (SlipDetailCategory element : values()) {
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