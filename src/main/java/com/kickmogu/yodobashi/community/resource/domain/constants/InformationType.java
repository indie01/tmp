package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * お知らせタイプです。
 */
public enum InformationType implements LabeledEnum<InformationType, String>{

	PRODUCT_MASTER_RANK_CHANGE("1", "商品マスタ－ランクイン・順位変動"),
	REVIEW_COMMENT_ADD("2", "レビューにコメント追加"),
	REVIEW_LIKE_ADD("3", "レビューにいいね評価追加"),
	QUESTION_ANSWER_COMMENT_ADD("4", "QA回答にコメント追加"),
	QUESTION_ANSWER_LIKE_ADD("5", "QA回答にいいね評価追加"),
	IMAGE_COMMENT_ADD("6", "画像にコメント追加"),
	IMAGE_LIKE_ADD("7", "画像にいいね評価追加"),
	QUESTION_ANSWER_ADD("8", "QA質問に回答追加"),
	FOLLOW("9", "フォローされた場合"),
	POINT_REVIEW("10", "レビューポイントを獲得"),
	POINT_COMMUNITY("11", "コミュニティ貢献ポイントを獲得"),
	ACCOUNT_STOP("12", "アカウントを強制一時停止された"),
	WELCOME("13", "会員登録時"),
	DEPRIVE_POINT("14", "ポイント剥奪"),
	CONTENTS_STOP("15", "コンテンツの一時停止"),
	REVIEW_VOTING_ADD("16", "レビューに参考になった評価追加"),
	QUESTION_ANSWER_VOTING_ADD("17", "QA回答に参考になった評価追加"),
	IMAGE_VOTING_ADD("18", "画像に参考になった評価追加"),
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
	private InformationType(String code, String label) {
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
	public static InformationType codeOf(String code) {
		for (InformationType element : values()) {
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