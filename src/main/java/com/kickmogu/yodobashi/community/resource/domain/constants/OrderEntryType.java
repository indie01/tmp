package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * 受注区分です。

A 修理
B 未登録　（店舗渡／配送）
C 登録品店舗渡（前受金無）
D 登録品店舗渡（前受金有）
E 登録品配送
F ＥＣ
G 特販
H 特販返品
I ＥＣ返品
J 返品回収
K 返品（自動）
L 中古品
M ＥＣ店舗渡（前受金無）
 */
public enum OrderEntryType implements LabeledEnum<OrderEntryType, String>{

	FIX("A", "修理"),
	NOT_REGIST ("B", "未登録（店舗渡／配送）"),
	SHOP("C", "登録品店舗渡（前受金無）"),
	SHOP_PREPAYED("D", "登録品店舗渡（前受金有）"),
	DELIVER("E", "登録品配送"),
	EC("F", "ＥＣ"),
	SPECIAL("G", "特販"),
	RETURNED_SPECIAL_GOODS("H", "特販返品"),
	RETURNED_EC_GOODS("I", "ＥＣ返品"),
	CALL_IN_RETURNED_GOODS("J", "返品回収"),
	AUTO_RETURNED("K", "返品（自動）"),
	USED("L", "中古品"),
	EC_PASSING_IN_SHOP("M", "ＥＣ店舗渡（前受金無）"),

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
	private OrderEntryType(String code, String label) {
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
	public static OrderEntryType codeOf(String code) {
		for (OrderEntryType element : values()) {
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