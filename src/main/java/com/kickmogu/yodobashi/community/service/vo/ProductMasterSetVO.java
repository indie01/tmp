package com.kickmogu.yodobashi.community.service.vo;

import java.util.List;

import com.google.common.collect.Lists;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;

/**
 * 商品マスターの表示情報を集めたビューオブジェクトです。
 *
 * @author taniguchi
 *
 */
public class ProductMasterSetVO extends BaseVO {

	/**
	 *
	 */
	private static final long serialVersionUID = 1097513837376778761L;

	/** 商品マスター */
	private ProductMasterDO productMaster;

	/**
	 * 商品をフォロー済みかどうかです。
	 */
	private boolean followingProduct;

	/**
	 * ユーザーをフォロー済みかどうかです。
	 */
	private boolean followingUser;

	/**
	 * この商品の商品マスターであるコミュニティユーザーのリストです。
	 */
	private List<CommunityUserDO> topMasters = Lists.newArrayList();

	/**
	 * @return productMaster
	 */
	public ProductMasterDO getProductMaster() {
		return productMaster;
	}

	/**
	 * @param productMaster セットする productMaster
	 */
	public void setProductMaster(ProductMasterDO productMaster) {
		this.productMaster = productMaster;
	}

	/**
	 * @return followingProduct
	 */
	public boolean isFollowingProduct() {
		return followingProduct;
	}

	/**
	 * @param followingProduct セットする followingProduct
	 */
	public void setFollowingProduct(boolean followingProduct) {
		this.followingProduct = followingProduct;
	}

	/**
	 * @return followingUser
	 */
	public boolean isFollowingUser() {
		return followingUser;
	}

	/**
	 * @param followingUser セットする followingUser
	 */
	public void setFollowingUser(boolean followingUser) {
		this.followingUser = followingUser;
	}

	/**
	 * @return the topMasters
	 */
	public List<CommunityUserDO> getTopMasters() {
		return topMasters;
	}

	/**
	 * @param topMasters the topMasters to set
	 */
	public void setTopMasters(List<CommunityUserDO> topMasters) {
		this.topMasters = topMasters;
	}

}
