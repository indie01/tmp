package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.LabeledEnum;

/**
 * ポイントインセンティブタイプです。
 */
public enum PointIncentiveType implements LabeledEnum<PointIncentiveType, String>{

	IMMEDIATELY_AFTER_DECISIVE_PURCHASE (
			"A001", "購入の決め手",
			ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE, false),
	IMMEDIATELY_AFTER_PURCHASE_LOST_PRODUCT (
			"A002", "この商品と購入を迷った商品",
			ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE, false),
	IMMEDIATELY_AFTER_USED_PRODUCT (
			"A003", "過去に使っていた似た商品",
			ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE, false),
	IMMEDIATELY_AFTER_REVIEW (
			"A004", "商品レビュー（本文）",
			ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE, false),
	IMMEDIATELY_SATISFACTION (
			"A005", "この商品の満足度",
			ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE, false),
	AFTER_FEW_DAYS_SATISFACTION (
			"B001", "この商品の満足度",
			ReviewType.REVIEW_AFTER_FEW_DAYS, false),
	AFTER_FEW_DAYS_ALSO_BUY (
			"B002", "この商品を次も買いますか",
			ReviewType.REVIEW_AFTER_FEW_DAYS, false),
	AFTER_FEW_DAYS_REVIEW (
			"B003", "商品レビュー（本文）",
			ReviewType.REVIEW_AFTER_FEW_DAYS, false),
	SPECIAL_COND("_00", "特別条件", null, false),
	OLD_REVIEW("_01", "レビューポイント", null, true),
	OLD_BONUS("_02", "ボーナスポイント", null, true),
	OLD_FIRST("_03", "先着順ポイント", null, true),
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
	 * レビュータイプです。
	 */
	private ReviewType reviewType;

	/**
	 * 移行前のタイプかどうかです。
	 */
	private boolean old;

	/**
	 * コンストラクタです。
	 * @param code コード
	 * @param label ラベル
	 * @param reviewType レビュータイプ
	 * @param old 移行前のタイプかどうか
	 */
	private PointIncentiveType(
			String code,
			String label,
			ReviewType reviewType,
			boolean old) {
		this.code = code;
		this.label = label;
		this.reviewType = reviewType;
		this.old = old;
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
	public static PointIncentiveType codeOf(String code) {
		for (PointIncentiveType element : values()) {
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
	 * レビュータイプを返します。
	 * @return reviewType レビュータイプ
	 */
	public ReviewType getReviewType() {
		return reviewType;
	}

	/**
	 * 移行前のタイプかどうか
	 * @return 移行前のタイプかどうか
	 */
	public boolean isOld() {
		return old;
	}

}