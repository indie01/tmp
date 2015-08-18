package com.kickmogu.yodobashi.community.resource.domain.constants;

import com.kickmogu.lib.core.constants.EnumLabel;

/**
 * 設問ポイントタイプです。
 */
public enum PointQuestType implements EnumLabel {

	IMMEDIATELY_AFTER_DECISIVE_PURCHASE (PointIncentiveType.IMMEDIATELY_AFTER_DECISIVE_PURCHASE, false, "購入の決め手"),
	IMMEDIATELY_AFTER_PURCHASE_LOST_PRODUCT (PointIncentiveType.IMMEDIATELY_AFTER_PURCHASE_LOST_PRODUCT, false, "この商品と購入を迷った商品"),
	IMMEDIATELY_AFTER_USED_PRODUCT (PointIncentiveType.IMMEDIATELY_AFTER_USED_PRODUCT, false, "過去に使っていた似た商品"),
	IMMEDIATELY_AFTER_REVIEW (PointIncentiveType.IMMEDIATELY_AFTER_REVIEW, false, "商品レビュー（本文）"),
	IMMEDIATELY_SATISFACTION  (PointIncentiveType.IMMEDIATELY_SATISFACTION, false, "この商品の満足度"),
	AFTER_FEW_DAYS_SATISFACTION (PointIncentiveType.AFTER_FEW_DAYS_SATISFACTION, false, "この商品の満足度"),
	AFTER_FEW_DAYS_ALSO_BUY (PointIncentiveType.AFTER_FEW_DAYS_ALSO_BUY, false, "この商品を次も買いますか"),
	AFTER_FEW_DAYS_REVIEW (PointIncentiveType.AFTER_FEW_DAYS_REVIEW, false, "商品レビュー（本文）"),
	PRODUCT_POINT(null, true, "品目ポイント"),
	;

	/**
	 * ポイントインセンティブタイプです。
	 */
	private PointIncentiveType type;

	/**
	 * 商品単位です。
	 */
	private boolean productScope;

	/**
	 * ラベルです。
	 */
	private String label;

	/**
	 * コンストラクタです。
	 * @param code コード
	 * @param label ラベル
	 */
	private PointQuestType(
			PointIncentiveType type,
			boolean productScope,
			String label) {
		this.type = type;
		this.productScope = productScope;
		this.label = label;
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
	 * @return type
	 */
	public PointIncentiveType getType() {
		return type;
	}

	/**
	 * @return productScope
	 */
	public boolean isProductScope() {
		return productScope;
	}

	public static PointQuestType valueOf(PointIncentiveType incentiveType) {
		for (PointQuestType target : values()) {
			if (target.getType() != null && target.getType().equals(incentiveType)) {
				return target;
			}
		}
		return null;
	}

}