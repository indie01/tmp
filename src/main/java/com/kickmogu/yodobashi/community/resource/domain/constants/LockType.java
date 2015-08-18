package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * ロックタイプです。
 */
public enum LockType implements LabeledEnum<LockType, String>{

	SAVE_REVIEW ("1", "レビュー登録用"),
	SAVE_QUESTION ("2", "質問登録用"),
	SAVE_QUESTION_ANSWER ("3", "質問回答登録用"),
	SOLR_CONTROL("4", "Solr制御用"),
	DELETE_IMAGE_IN_SET("5", "画像セットの画像削除用"),
	SAVE_PURCHASE_PRODUCT("6", "購入商品更新処理")
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
	private LockType(String code, String label) {
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
	public static LockType codeOf(String code) {
		for (LockType element : values()) {
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