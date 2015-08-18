package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * 売上登録明細区分です。
１：対象、２：非対象
前受金ありの伝票：
明細が売上（請求）登録されたかの判定・・・その後の返品は考慮していません。
前受金なしの伝票：
販売済み数量が設定されているかの判定

 */
public enum SalesRegistDetailType implements LabeledEnum<SalesRegistDetailType, String>{

	EFFECTIVE("1", "有効"),
	INEFFECTIVE("2", "無効"),
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
	private SalesRegistDetailType(String code, String label) {
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
	public static SalesRegistDetailType codeOf(String code) {
		for (SalesRegistDetailType element : values()) {
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