/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import com.kickmogu.yodobashi.community.resource.domain.constants.AlsoBuyProduct;

/**
 * 次も買いますかのビューオブジェクトです。
 * @author kamiike
 *
 */
public class AlsoBuyProductVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = 6442208257896363191L;

	/**
	 * 次も買いますかコードです。
	 */
	private AlsoBuyProduct alsoBuyProduct;

	/**
	 * 評価数です。
	 */
	private long ratings;
	
	/**
	 * パーセンテージです
	 */
	private long percentage;

	/**
	 * @return alsoBuyProduct
	 */
	public AlsoBuyProduct getAlsoBuyProduct() {
		return alsoBuyProduct;
	}

	/**
	 * @param alsoBuyProduct セットする alsoBuyProduct
	 */
	public void setAlsoBuyProduct(AlsoBuyProduct alsoBuyProduct) {
		this.alsoBuyProduct = alsoBuyProduct;
	}

	/**
	 * @return ratings
	 */
	public long getRatings() {
		return ratings;
	}

	/**
	 * @param ratings セットする ratings
	 */
	public void setRatings(long ratings) {
		this.ratings = ratings;
	}

	public long getPercentage() {
		return percentage;
	}

	public void setPercentage(long percentage) {
		this.percentage = percentage;
	}

}
