/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import java.util.List;

/**
 * 商品満足度のサマリービューオブジェクトです。
 * @author kamiike
 *
 */
public class ProductSatisfactionSummaryVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = 7357941186723404455L;

	/**
	 * 満足度平均評価です。
	 */
	private Double satisfactionAvarage;

	/**
	 * 回答者数です。
	 */
	private long answerCount;
	
	/**
	 * 総レビュー数
	 */
	private long reviewTotalCount;

	/**
	 * 満足度情報リストです。
	 */
	private List<ProductSatisfactionVO> productSatisfactions;

	/**
	 * 商品満足度平均評価数
	 */
	
	private String satisfactionAvaragePoint;
	
	/**
	 * 商品満足度スターCSS
	 */
	private String satisfactionAvarageCss;
	
	/**
	 * 商品満足度平均評価数（バリエーション商品時）
	 */
	private String variationSatisfactionAveragePoint;
	
	/**
	 * 商品満足度スターCSS（バリエーション商品時）
	 */
	private String variationSatisfactionAvarageCss;


	/**
	 * @return satisfactionAvarage
	 */
	public Double getSatisfactionAvarage() {
		return satisfactionAvarage;
	}

	/**
	 * @param satisfactionAvarage セットする satisfactionAvarage
	 */
	public void setSatisfactionAvarage(Double satisfactionAvarage) {
		this.satisfactionAvarage = satisfactionAvarage;
	}

	/**
	 * @return answerCount
	 */
	public long getAnswerCount() {
		return answerCount;
	}

	/**
	 * @param answerCount セットする answerCount
	 */
	public void setAnswerCount(long answerCount) {
		this.answerCount = answerCount;
	}

	public long getReviewTotalCount() {
		return reviewTotalCount;
	}

	public void setReviewTotalCount(long reviewTotalCount) {
		this.reviewTotalCount = reviewTotalCount;
	}

	/**
	 * @return productSatisfactions
	 */
	public List<ProductSatisfactionVO> getProductSatisfactions() {
		return productSatisfactions;
	}

	/**
	 * @param productSatisfactions セットする productSatisfactions
	 */
	public void setProductSatisfactions(
			List<ProductSatisfactionVO> productSatisfactions) {
		this.productSatisfactions = productSatisfactions;
	}

	public String getSatisfactionAvaragePoint() {
		return satisfactionAvaragePoint;
	}

	public void setSatisfactionAvaragePoint(String satisfactionAvaragePoint) {
		this.satisfactionAvaragePoint = satisfactionAvaragePoint;
	}

	public String getSatisfactionAvarageCss() {
		return satisfactionAvarageCss;
	}

	public void setSatisfactionAvarageCss(String satisfactionAvarageCss) {
		this.satisfactionAvarageCss = satisfactionAvarageCss;
	}

	public String getVariationSatisfactionAveragePoint() {
		return variationSatisfactionAveragePoint;
	}

	public void setVariationSatisfactionAveragePoint(
			String variationSatisfactionAveragePoint) {
		this.variationSatisfactionAveragePoint = variationSatisfactionAveragePoint;
	}

	public String getVariationSatisfactionAvarageCss() {
		return variationSatisfactionAvarageCss;
	}

	public void setVariationSatisfactionAvarageCss(
			String variationSatisfactionAvarageCss) {
		this.variationSatisfactionAvarageCss = variationSatisfactionAvarageCss;
	}
}
