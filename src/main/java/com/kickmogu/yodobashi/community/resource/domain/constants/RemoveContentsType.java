package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * コンテンツタイプです。
 */
public enum RemoveContentsType implements LabeledEnum<RemoveContentsType, String>{

	REVIEW ("001", "レビュー"),
	QUESTION ("002", "質問"),
	QUESTION_ANSWER ("003", "質問回答"),
	IMAGE ("004", "画像"),
	COMMENT ("005", "コメント"),
	ACTIONHISTORY ("006", "アクションヒストリー"),
	INFORMATION ("007", "お知らせ"),
	SPAMREPORT ("008", "違反報告"),

	REVIEWDECISIVEPURCHASE("009", "購入の決め手情報"),
	PURCHASELOSTPRODUCT("010", "購入を迷った商品情報"),
	USEDPRODUCT("011", "過去に使用した商品情報"),
	
	QUESTIONFOLLOW ("012", "質問フォロー"),
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
	private RemoveContentsType(String code, String label) {
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
	public static RemoveContentsType codeOf(String code) {
		for (RemoveContentsType element : values()) {
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