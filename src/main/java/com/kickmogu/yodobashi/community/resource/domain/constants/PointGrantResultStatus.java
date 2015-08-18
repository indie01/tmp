package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * PMSのPointGrantRequestExecMainTypeより、抜粋
 * @author imaizumi
 *
 */
public enum PointGrantResultStatus implements LabeledEnum<PointGrantResultStatus, String>{
	POINT_GRANT_NOTSET("10", "ポイント付与許可対象外（既定値）"),
	POINT_GRANT_TARGET("11","ポイント付与実行対象"),
	POINT_GRANT_PERMIT("12", "ポイント付与実行許可"),
	POINT_GRANT_HOLD("13", "ポイント付与実行保留"),
	POINT_GRANT_EXEC_CREATE_FILE("15", "ポイント付与実行ファイル生成"),
	POINT_GRANT_EXEC_SENDING_FILE("16", "ポイント付与実行ファイル送信中"),
	POINT_GRANT_EXEC_SENDED_FILE("17", "ポイント付与実行ファイル送信済"),
	POINT_DEPRIVATION_NOTSET("20", "ポイント剥奪許可対象外"),
	POINT_DEPRIVATION_TARGET("21","ポイント剥奪実行対象"),
	POINT_DEPRIVATION_PERMIT("22","ポイント剥奪実行許可"),
	POINT_DEPRIVATION_HOLD("23","ポイント剥奪実行保留"),
	POINT_DEPRIVATION_EXEC_CREATE_FILE("24", "ポイント剥奪実行ファイル生成"),
	POINT_DEPRIVATION_EXEC_SENDING_FILE("25", "ポイント剥奪実行ファイル送信中"),
	POINT_DEPRIVATION_EXEC_SENDED_FILE("26", "ポイント剥奪実行ファイル送信済"),
	FINISH("90", "終了（正常）"),
	FINISH_FOR_POINT_GRANT_NOT_PERMIT("91", "終了（ポイント付与許可非承認による終了）"),
	FINISH_FOR_POINT_DEPRIVATION_NOT_PERMIT("92", "終了（ポイント剥奪許可非承認による終了）"),
	FINISH_FOR_EC_WITHDRAWAL("93", "終了（EC会員退会による終了）"),
	FINISH_ABNORMAL_FOR_POINT_GRANT("98","異常終了（ポイント額が一致していない）"),
	FINISH_ABNORMAL_FOR_POINT_DEPRIVATION("99","異常終了（ポイント額が一致していない）")
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
	private PointGrantResultStatus(String code, String label) {
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
	public static PointGrantResultStatus codeOf(String code) {
		for (PointGrantResultStatus element : values()) {
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
