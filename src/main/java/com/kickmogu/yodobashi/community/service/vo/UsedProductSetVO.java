/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import com.kickmogu.yodobashi.community.resource.domain.ProductDO;

/**
 * 過去の使用した商品関連の表示情報を集めたビューオブジェクトです。
 * @author kamiike
 *
 */
public class UsedProductSetVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = -2321506980836119196L;

	/**
	 * 過去の使用した商品です。
	 */
	private ProductDO usedProduct;

	/**
	 * 選択者数です。
	 */
	private long count;

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

	/**
	 * @return usedProduct
	 */
	public ProductDO getUsedProduct() {
		return usedProduct;
	}

	/**
	 * @param usedProduct セットする usedProduct
	 */
	public void setUsedProduct(ProductDO usedProduct) {
		this.usedProduct = usedProduct;
	}
}
