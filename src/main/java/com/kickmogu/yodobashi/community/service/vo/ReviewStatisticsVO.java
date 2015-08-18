/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;


/**
 * レビュー統計情報のビューオブジェクトです。
 * @author kamiike
 *
 */
public class ReviewStatisticsVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = -4644918780935387610L;


	/**
	 * 購入の決め手情報サマリーです。
	 */
	private DecisivePurchaseSummaryVO decisivePurchaseSummary;


	/**
	 * 次も買いますか情報サマリーです。
	 */
	private AlsoBuyProductSummaryVO alsoBuyProductSummary;


	/**
	 * 商品満足度サマリーです。
	 */
	private ProductSatisfactionSummaryVO productSatisfactionSummary;


	/**
	 * @return decisivePurchaseSummary
	 */
	public DecisivePurchaseSummaryVO getDecisivePurchaseSummary() {
		return decisivePurchaseSummary;
	}


	/**
	 * @param decisivePurchaseSummary セットする decisivePurchaseSummary
	 */
	public void setDecisivePurchaseSummary(
			DecisivePurchaseSummaryVO decisivePurchaseSummary) {
		this.decisivePurchaseSummary = decisivePurchaseSummary;
	}


	/**
	 * @return alsoBuyProductSummary
	 */
	public AlsoBuyProductSummaryVO getAlsoBuyProductSummary() {
		return alsoBuyProductSummary;
	}


	/**
	 * @param alsoBuyProductSummary セットする alsoBuyProductSummary
	 */
	public void setAlsoBuyProductSummary(
			AlsoBuyProductSummaryVO alsoBuyProductSummary) {
		this.alsoBuyProductSummary = alsoBuyProductSummary;
	}


	/**
	 * @return productSatisfactionSummary
	 */
	public ProductSatisfactionSummaryVO getProductSatisfactionSummary() {
		return productSatisfactionSummary;
	}


	/**
	 * @param productSatisfactionSummary セットする productSatisfactionSummary
	 */
	public void setProductSatisfactionSummary(
			ProductSatisfactionSummaryVO productSatisfactionSummary) {
		this.productSatisfactionSummary = productSatisfactionSummary;
	}

}
