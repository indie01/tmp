package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * レシート明細区分です。

0 商品
1 保証
2 DPE
3 未登録品
4 中古品
5 委託品"

 */
public enum ReceiptDetailType implements LabeledEnum<ReceiptDetailType, String>{

	PRODUCT("0", "商品"),
	GUARANTEE("1", "保証"),
	DPE("2", "DPE"),
	NOT_REGIST("3", "未登録品"),
	USED("4", "中古品"),
	CHARGE("5", "委託品"),

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
	private ReceiptDetailType(String code, String label) {
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
	public static ReceiptDetailType codeOf(String code) {
		for (ReceiptDetailType element : values()) {
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