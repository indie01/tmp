package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * お客様キーID種別です。
 */
public enum LikeMessageType implements LabeledEnum<LikeMessageType, Integer>{

	NONE (0, "なし"),
	UPTO3(1, "3人まで"),
	MULTIPLE(2, "4人以上"),
	;

	/**
	 * コードです。
	 */
	private Integer code;

	/**
	 * ラベルです。
	 */
	private String label;

	/**
	 * コンストラクタです。
	 * @param code コード
	 * @param label ラベル
	 */
	private LikeMessageType(Integer code, String label) {
		this.code = code;
		this.label = label;
	}

	/**
	 * コードを返します。
	 * @return コード
	 */
	public Integer getCode() {
		return code;
	}

	/**
	 * 指定したコードの値を返します。
	 * @param code コード
	 * @return
	 */
	public static LikeMessageType codeOf(String code) {
		for (LikeMessageType element : values()) {
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