/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.ProductSatisfaction;

/**
 * 商品満足度関連の表示情報を集めたビューオブジェクトです。
 * @author kamiike
 *
 */
public class ProductSatisfactionSetVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = -297253914840329073L;

	/**
	 * 商品満足度です。
	 */
	private ProductSatisfaction productSatisfaction;

	/**
	 * 他に選択しているコミュニティユーザーのリストです。
	 */
	private SearchResult<CommunityUserDO> otherSelectedCommunityUsers;

	/**
	 * @return otherSelectedCommunityUsers
	 */
	public SearchResult<CommunityUserDO> getOtherSelectedCommunityUsers() {
		return otherSelectedCommunityUsers;
	}

	/**
	 * @param otherSelectedCommunityUsers セットする otherSelectedCommunityUsers
	 */
	public void setOtherSelectedCommunityUsers(
			SearchResult<CommunityUserDO> otherSelectedCommunityUsers) {
		this.otherSelectedCommunityUsers = otherSelectedCommunityUsers;
	}

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
}
