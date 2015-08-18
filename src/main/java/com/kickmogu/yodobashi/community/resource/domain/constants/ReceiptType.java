package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * レシート種別です。

A 通常
B 個別
C 修理
D 中古買取
F 返品伝票
K 受注受付
E 支払方法
L EC受注
 */
public enum ReceiptType implements LabeledEnum<ReceiptType, String>{

	NORMAL("A", "通常"),
	INDIVIDUAL("B", "個別"),
	FIXED("C", "修理"),
	BUY_BACK_USED("D", "中古買取"),
	RETURNING_RECEIPT("F", "返品伝票"),
	ORDER_ENTRY("K", "受注受付"),
	PAYMENT_METHOD("E", "支払方法"),
	EC_ORDER_ENTRY("L", "EC受注"),

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
	private ReceiptType(String code, String label) {
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
	public static ReceiptType codeOf(String code) {
		for (ReceiptType element : values()) {
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