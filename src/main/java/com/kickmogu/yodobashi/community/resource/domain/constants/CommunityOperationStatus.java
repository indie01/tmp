package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * アナウンスタイプです。
 */
public enum CommunityOperationStatus implements LabeledEnum<CommunityOperationStatus, String>{

	IN_OPERATION("001", "FullMode"),
	READONLY_OPERATION("002", "ReadOnlyMode"),
	STOP_OPERATION("999", "MaintenanceMode"),
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
	private CommunityOperationStatus(String code, String label) {
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
	public static CommunityOperationStatus codeOf(String code) {
		for (CommunityOperationStatus element : values()) {
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