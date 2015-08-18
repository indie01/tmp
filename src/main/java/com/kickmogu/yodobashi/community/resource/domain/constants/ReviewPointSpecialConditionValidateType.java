package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

public enum ReviewPointSpecialConditionValidateType implements LabeledEnum<ReviewPointSpecialConditionValidateType, String>{
	YET_REGISTRED("01","未登録"),
	ALREADY_REGISTRED("02","既に登録があります"),
	DEADLINE("03","登録締め切り");

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
	private ReviewPointSpecialConditionValidateType(String code, String label) {
		this.code = code;
		this.label = label;
	}
	
	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getCode() {
		return code;
	}
	
	/**
	 * 指定したコードの値を返します。
	 * @param code コード
	 * @return
	 */
	public static ReviewPointSpecialConditionValidateType codeOf(String code) {
		for (ReviewPointSpecialConditionValidateType element : values()) {
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

}
