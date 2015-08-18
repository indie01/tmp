package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * ポイント付与ステータスです。
 */
public enum MigrationAddPointStatus implements LabeledEnum<MigrationAddPointStatus, String>{

	WAIT ("  ", "未処理"),
	COMPLETE ("01", " 処理済み"),
	NG_NO_USER ("02", "処理対象外（有効会員なし）"),
	NG_NO_SKU ("03", "処理対象外（有効品目なし）"),
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
	private MigrationAddPointStatus(String code, String label) {
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
	public static MigrationAddPointStatus codeOf(String code) {
		for (MigrationAddPointStatus element : values()) {
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