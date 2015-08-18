package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

public enum ReviewMetaType implements LabeledEnum<ReviewMetaType, String> {
	A001_TITLE("A00101", "購入の決め手 タイトル"),
	A001_DESCRIPTION("A00102", "購入の決め手 説明"),
	A002_TITLE("A00201", "この商品と購入を迷った製品 タイトル"),
	A002_DESCRIPTION("A00202", "この商品と購入を迷った製品手 説明"),
	A003_TITLE("A00301", "過去に使っていた似た製品 タイトル"),
	A003_DESCRIPTION("A00302", "過去に使っていた似た製品手 説明"),
	A004_TITLE("A00401", "第一印象レビューのレビュー本文 タイトル"),
	A004_DESCRIPTION("A00402", "第一印象レビューのレビュー本文 説明"),
	A005_TITLE("A00501", "第一印象レビューの満足度 タイトル"),
	A005_DESCRIPTION("A00502", "第一印象レビューの満足度 説明"),
	B001_TITLE("B00101", "満足度レビューの満足度 タイトル"),
	B001_DESCRIPTION("B00102", "満足度レビューの満足度 説明"),
	B002_TITLE("B00201", "この製品を次を買いますか タイトル"),
	B002_DESCRIPTION("B00202", "この製品を次を買いますか 説明"),
	B003_TITLE("B00301", "満足度レビューのレビュー本文 タイトル"),
	B003_DESCRIPTION("B00302", "満足度レビューのレビュー本文 説明")
	;
	
	/**
	 * コードです。
	 */
	private String code;

	/**
	 * ラベルです。
	 */
	private String label;
	
	private ReviewMetaType(String code, String label) {
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
	public static ReviewMetaType codeOf(String code) {
		for (ReviewMetaType element : values()) {
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

}
