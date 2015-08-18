/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import com.kickmogu.yodobashi.community.resource.domain.constants.ProductSatisfaction;

/**
 * 商品満足度のビューオブジェクトです。
 * @author kamiike
 *
 */
public class ProductSatisfactionVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = 8558467587724825329L;

	/**
	 * 満足度コードです。
	 */
	private ProductSatisfaction productSatisfaction;

	/**
	 * 評価数です。
	 */
	private long satisfactionCount;

	/**
	 * @return productSatisfaction
	 */
	public ProductSatisfaction getProductSatisfaction() {
		return productSatisfaction;
	}

	/**
	 * @param productSatisfaction セットする productSatisfaction
	 */
	public void setProductSatisfaction(ProductSatisfaction productSatisfaction) {
		this.productSatisfaction = productSatisfaction;
	}

	/**
	 * @return satisfactionCount
	 */
	public long getSatisfactionCount() {
		return satisfactionCount;
	}

	/**
	 * @param satisfactionCount セットする satisfactionCount
	 */
	public void setSatisfactionCount(long satisfactionCount) {
		this.satisfactionCount = satisfactionCount;
	}

}
