/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import com.kickmogu.yodobashi.community.resource.domain.ProductDO;

/**
 * 商品関連の表示情報を集めたビューオブジェクトです。
 * @author kamiike
 *
 */
public class ProductSetVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = -1198921212006305249L;

	/**
	 * 商品
	 */
	private ProductDO product;

	/**
	 * フォロー済みかどうかです。
	 */
	private boolean followingFlg;

	/**
	 * フォロワー数です。
	 */
	private long followerCount;

	/**
	 * @return product
	 */
	public ProductDO getProduct() {
		return product;
	}

	/**
	 * @param product セットする product
	 */
	public void setProduct(ProductDO product) {
		this.product = product;
	}

	/**
	 * @return followingFlg
	 */
	public boolean isFollowingFlg() {
		return followingFlg;
	}

	/**
	 * @param followingFlg セットする followingFlg
	 */
	public void setFollowingFlg(boolean followingFlg) {
		this.followingFlg = followingFlg;
	}

	/**
	 * @return followerCount
	 */
	public long getFollowerCount() {
		return followerCount;
	}

	/**
	 * @param followerCount セットする followerCount
	 */
	public void setFollowerCount(long followerCount) {
		this.followerCount = followerCount;
	}

}
