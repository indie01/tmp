package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

public enum PurchaseProductSearchCondition implements LabeledEnum<PurchaseProductSearchCondition, String> {
	ALL("0", "すべて"),
	NO_MY_REVIEW ("1", "レビュー未投稿"),
	NO_REVIEW ("2", "誰もレビューを書いていない"),
	HAS_WAIT_ANSWER ("3","回答待ちQ&A");
	
	private String code;
	
	private String label;
	
	private PurchaseProductSearchCondition(String code, String label) {
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
	public static PurchaseProductSearchCondition codeOf(String code) {
		for (PurchaseProductSearchCondition element : values()) {
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
