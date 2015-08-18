package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

public enum ValidateAuthSessionV2UpdateSessionStatus implements LabeledEnum<ValidateAuthSessionV2UpdateSessionStatus, String>{
	
	DO_NOT_UPDATE("001", "更新しない"),
	FULLTIME_UPDATE("002","常に更新する")
	;
	/**
	 * コードです。
	 */
	private String code;

	/**
	 * ラベルです。
	 */
	private String label;
	
	private ValidateAuthSessionV2UpdateSessionStatus(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public String getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}
	
	/**
	 * 指定したコードの値を返します。
	 * @param code コード
	 * @return
	 */
	public static ValidateAuthSessionV2UpdateSessionStatus codeOf(String code) {
		for (ValidateAuthSessionV2UpdateSessionStatus element : values()) {
			if (element.code.equals(code)) {
				return element;
			}
		}
		return null;
	}
	
}
