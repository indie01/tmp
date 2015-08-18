package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * 注文作成処理タイプです。<br />
 * 開発・検証用の xi インターフェースの処理区分です。
 */
public enum CreateOrderProcCode implements LabeledEnum<CreateOrderProcCode, String>{

	ORDER ("01", "入金処理"),
	CLAIM ("02", "請求処理"),
	COMPLETE ("03", "販売完了処理"),
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
	private CreateOrderProcCode(String code, String label) {
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
	public static CreateOrderProcCode codeOf(String code) {
		for (CreateOrderProcCode element : values()) {
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