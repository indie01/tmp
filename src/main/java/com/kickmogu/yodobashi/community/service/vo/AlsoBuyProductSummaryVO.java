/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import java.util.List;

/**
 * 次も買いますかの集約ビューオブジェクトです。
 * @author kamiike
 *
 */
public class AlsoBuyProductSummaryVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = -4477287057817735327L;

	/**
	 * 総評価数です。
	 */
	private long totalCount;

	/**
	 * 次も買いますかです。
	 */
	private List<AlsoBuyProductVO> alsoBuyProducts;

	/**
	 * @return totalCount
	 */
	public long getTotalCount() {
		return totalCount;
	}

	/**
	 * @param totalCount セットする totalCount
	 */
	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

	/**
	 * @return alsoBuyProducts
	 */
	public List<AlsoBuyProductVO> getAlsoBuyProducts() {
		return alsoBuyProducts;
	}

	/**
	 * @param alsoBuyProducts セットする alsoBuyProducts
	 */
	public void setAlsoBuyProducts(List<AlsoBuyProductVO> alsoBuyProducts) {
		this.alsoBuyProducts = alsoBuyProducts;
	}

}
