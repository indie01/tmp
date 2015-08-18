package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

public enum CancelPointGrantType implements LabeledEnum<CancelPointGrantType, String>{
	DELETE_REVIEW("01","レビュー削除による終了"),
	DELETE_REVIEW_ITEM("02","レビュー商品のキャンセルによる終了"),
	COMMUNITY_FORCED_WITHDRAWAL("03","コミュニティ会員強制退会による終了"),
	COMMUNITY_WITHDRAWAL("04","コミュニティ会員退会による終了"),
	EC_WITHDRAWAL("05","EC会員退会による終了"),
	CONTENTS_STOP("06","コンテンツの一時停止による終了"),
	MODIFY_REVIEW("12", "レビュー更新による終了");

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
	private CancelPointGrantType(String code, String label) {
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
	public static CancelPointGrantType codeOf(String code) {
		for (CancelPointGrantType element : values()) {
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
