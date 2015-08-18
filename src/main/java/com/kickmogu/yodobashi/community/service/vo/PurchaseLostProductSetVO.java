/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import com.kickmogu.yodobashi.community.resource.domain.ProductDO;

/**
 * 購入に迷った商品関連の表示情報を集めたビューオブジェクトです。
 * @author kamiike
 *
 */
public class PurchaseLostProductSetVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = 8896038311090614344L;

	/**
	 * 購入に迷った商品です。
	 */
	private ProductDO purchaseLostProduct;

	/**
	 * 選択者数です。
	 */
	private long count;

	/**
	 * @return purchaseLostProduct
	 */
	public ProductDO getPurchaseLostProduct() {
		return purchaseLostProduct;
	}

	/**
	 * @param purchaseLostProduct セットする purchaseLostProduct
	 */
	public void setPurchaseLostProduct(ProductDO purchaseLostProduct) {
		this.purchaseLostProduct = purchaseLostProduct;
	}

	/**
	 * @return count
	 */
	public long getCount() {
		return count;
	}

	/**
	 * @param count セットする count
	 */
	public void setCount(long count) {
		this.count = count;
	}
}
