package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * 商品引渡方法です。

1 店舗渡し
2 配送／回収
3 センタ渡し
4 代済店舗渡し
5 本部一括
 */
public enum DeliverType implements LabeledEnum<DeliverType, String>{

	SHOP("1", "店舗渡し"),
	DELIVER_CALL_IN("2", "配送／回収"),
	CENTER("3", "センタ渡し"),
	AGENT("4", "代済店舗渡し"),
	FRONT_OFFICE("5", "本部一括"),

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
	private DeliverType(String code, String label) {
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
	public static DeliverType codeOf(String code) {
		for (DeliverType element : values()) {
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