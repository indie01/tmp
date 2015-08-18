package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * アクション履歴タイプです。
 */
public enum ActionHistoryType implements LabeledEnum<ActionHistoryType, String>{

	USER_REVIEW("01", "レビュー投稿", ActionHistoryGroup.USER),
	USER_QUESTION("02", "質問を投稿", ActionHistoryGroup.USER),
	USER_ANSWER("03", "質問に回答", ActionHistoryGroup.USER),
	USER_IMAGE("04", "画像を投稿", ActionHistoryGroup.USER),
	USER_REVIEW_COMMENT("05", "レビューにコメント", ActionHistoryGroup.USER),
	USER_ANSWER_COMMENT("06", "質問回答にコメント", ActionHistoryGroup.USER),
	USER_IMAGE_COMMENT("07", "画像にコメント", ActionHistoryGroup.USER),
	USER_FOLLOW_USER("08", "ユーザーをフォロー", ActionHistoryGroup.USER),
	USER_FOLLOW_PRODUCT("09", "商品をフォロー", ActionHistoryGroup.USER),
	USER_FOLLOW_QUESTION("10", "質問をフォロー", ActionHistoryGroup.USER),
	USER_PRODUCT_MASTER_RANK_CHANGE("11", "商品マスターランクイン", ActionHistoryGroup.USER),
	PRODUCT_REVIEW("01", "商品の新着レビュー", ActionHistoryGroup.PRODUCT),
	PRODUCT_QUESTION("02", "商品の新着質問", ActionHistoryGroup.PRODUCT),
	PRODUCT_ANSWER("03", "商品の新着回答", ActionHistoryGroup.PRODUCT),
	PRODUCT_IMAGE("04", "商品の新着画像", ActionHistoryGroup.PRODUCT),
	QUESTION_ANSWER("01", "質問の新着回答", ActionHistoryGroup.QUESTION),
	LIKE_REVIEW_50("01", "レビューにいいねが50回", ActionHistoryGroup.LIKE),
	LIKE_ANSWER_50("02", "質問回答にいいねが50回", ActionHistoryGroup.LIKE),
	LIKE_IMAGE_50("03", "画像にいいねが50回", ActionHistoryGroup.LIKE)
	// TODO 参考になったを入れるかどうか検討
	;

	public enum ActionHistoryGroup {
		USER("1"),
		PRODUCT("2"),
		QUESTION("3"),
		LIKE("4")
		;

		/**
		 * コードです。
		 */
		private String groupCode;

		/**
		 * 最小コードです。
		 */
		private String minCode;

		/**
		 * 最大コードです。
		 */
		private String maxCode;

		/**
		 * コンストラクタです。
		 * @param code コード
		 */
		private ActionHistoryGroup(String code) {
			this.groupCode = code;
			this.minCode = code + "00";
			this.maxCode = code + "99";
		}

		/**
		 * コードを返します。
		 * @return コード
		 */
		public String getCode() {
			return this.groupCode;
		}

		/**
		 * 最小コードを返します。
		 * @return 最小コード
		 */
		public String getMinCode() {
			return this.minCode;
		}

		/**
		 * 最大コードを返します。
		 * @return 最大コード
		 */
		public String getMaxCode() {
			return this.maxCode;
		}
	}

	/**
	 * コードです。
	 */
	private String code;

	/**
	 * ラベルです。
	 */
	private String label;

	/**
	 * アクション履歴グループです。
	 */
	private ActionHistoryGroup group;

	/**
	 * コンストラクタです。
	 * @param code コード
	 * @param label ラベル
	 * @param group グループ
	 */
	private ActionHistoryType(String code, String label, ActionHistoryGroup group) {
		this.code = group.getCode() + code;
		this.label = label;
		this.group = group;
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
	public static ActionHistoryType codeOf(String code) {
		for (ActionHistoryType element : values()) {
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

	/**
	 * ラベルを返します。
	 * @return ラベル
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * アクション履歴グループを返します。
	 * @return アクション履歴グループ
	 */
	public ActionHistoryGroup getGroup() {
		return group;
	}

}