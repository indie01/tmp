package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.EnumLabel;
import com.kickmogu.lib.core.constants.LabeledEnum;
import com.kickmogu.web.common.domain.Selectable;

/**
 * メール設定タイプです。
 */
public enum MailSettingType implements LabeledEnum<MailSettingType, String>, Selectable{

	REVIEW_LIMIT ("01", "レビュー期限", MailSettingCategory.POST),
	PURCHASE_PRODUCT_QUESTION ("02", "購入商品の新着QA質問", MailSettingCategory.POST),
	REVIEW_COMMENT ("03", "レビューに対してコメントがついたとき", MailSettingCategory.POST),
	ANSWER_COMMENT ("04", "回答に対してコメントがついたとき", MailSettingCategory.POST),
	QUESTION_ANSWER ("05", "質問に対して回答がついたとき", MailSettingCategory.POST),
	ANSWER_QUESTION_ANOTHER_ANSWER ("06", "回答したQAに別の回答がついたとき", MailSettingCategory.POST),
	IMAGE_COMMENT ("07", "画像に対してコメントがついたとき", MailSettingCategory.POST),
	USER_FOLLOW ("08", "ほかのユーザーにフォローされたとき", MailSettingCategory.FOLLOW),
	REVIEW_PRODUCT_ANOTHER_REVIEW ("09", "レビューを書いた商品に別のレビューが投稿されたとき", MailSettingCategory.POST),
	FOLLOW_PRODUCT_REVIEW ("10", "フォローした商品の新着レビュー", MailSettingCategory.FOLLOW),
	FOLLOW_PRODUCT_QUESTION ("11", "フォローした商品の新着QA", MailSettingCategory.FOLLOW),
	FOLLOW_PRODUCT_IMAGE ("12", "フォローした商品の新着画像", MailSettingCategory.FOLLOW),
	FOLLOW_QUESTION_ANSWER ("13", "フォローしたQAの新着回答", MailSettingCategory.FOLLOW),
	FOLLOW_USER_QUESTION ("14", "フォローしたユーザのQA質問", MailSettingCategory.FOLLOW),
	FOLLOW_USER_REVIEW ("15", "フォローしたユーザのレビュー", MailSettingCategory.FOLLOW),
	FOLLOW_USER_QUESTION_ANSWER ("16", "フォローしたユーザのQAの回答", MailSettingCategory.FOLLOW),
	FOLLOW_USER_IMAGE ("17", "フォローしたユーザの画像投稿", MailSettingCategory.FOLLOW),
	RANK_IN_PRODUCT_MASTER ("18", "Top50の商品マスターにランクインしたとき", MailSettingCategory.PRODUCT_MASTER),
	;

	/**
	 * メール設定カテゴリです。
	 * @author kamiike
	 */
	public enum MailSettingCategory implements EnumLabel {
		POST("投稿に関するメール"),
		FOLLOW("フォローに関するメール"),
		PRODUCT_MASTER("商品マスターに関するメール");

		/**
		 * カテゴリラベルです。
		 */
		private String categoryLabel;

		/**
		 * コンストラクタです。
		 * @param categoryLabel カテゴリラベル
		 */
		private MailSettingCategory(String categoryLabel) {
			this.categoryLabel = categoryLabel;
		}

		/**
		 * カテゴリラベルを返します。
		 * @return カテゴリラベル
		 */
		public String getLabel() {
			return categoryLabel;
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
	 * メール設定カテゴリです。
	 */
	private MailSettingCategory category;

	/**
	 * コンストラクタです。
	 * @param code コード
	 * @param label ラベル
	 * @param category メール設定カテゴリ
	 */
	private MailSettingType(
			String code,
			String label,
			MailSettingCategory category) {
		this.code = code;
		this.label = label;
		this.category = category;
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
	public static MailSettingType codeOf(String code) {
		for (MailSettingType element : values()) {
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

	/**
	 * メール設定カテゴリを返します。
	 * @return category メール設定カテゴリ
	 */
	public MailSettingCategory getCategory() {
		return category;
	}

}