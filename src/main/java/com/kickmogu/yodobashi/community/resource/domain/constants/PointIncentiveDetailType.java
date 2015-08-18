package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * ポイントインセンティブ詳細タイプです。
 */
public enum PointIncentiveDetailType implements LabeledEnum<PointIncentiveDetailType, String>{

	CAMPAIGN ("0001", "キャンペーン"),
	FIRST_COME_BASIS ("0002", "先着順"),
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
	private PointIncentiveDetailType(String code, String label) {
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
	public static PointIncentiveDetailType codeOf(String code) {
		for (PointIncentiveDetailType element : values()) {
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