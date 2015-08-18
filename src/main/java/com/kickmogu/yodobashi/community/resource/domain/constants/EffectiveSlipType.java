package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * 有効伝票区分です。
１：有効、２：無効
有効：購入したGPC/IC/ECが退会していない状態です。
無効：購入したGPC/IC/ECが退会した状態です。"

 */
public enum EffectiveSlipType implements LabeledEnum<EffectiveSlipType, String>{

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
	private EffectiveSlipType(String code, String label) {
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
	public static EffectiveSlipType codeOf(String code) {
		for (EffectiveSlipType element : values()) {
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