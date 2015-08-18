/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;

/**
 * 商品フォローのビューオブジェクトです。
 * @author kamiike
 */
public class ProductFollowVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = -5084941009989450074L;

	/**
	 * 商品です。
	 */
	private ProductDO product;

	/**
	 * 投稿質問数です。
	 */
	private long postQuestionCount;

	/**
	 * 投稿レビュー数です。
	 */
	private long postReviewCount;

	/**
	 * 投稿画像数です。
	 */
	private long postImageCount;

	/**
	 * 満足度評価です。
	 */
	private ProductSatisfactionSummaryVO productSatisfactionSummary;

	/**
	 * フォロー人数です。
	 */
	private long followerCount;

	/**
	 * フォロー日時です。
	 */
	private Date followDate;

	/**
	 * 最新のフォロワーのリストです。
	 */
	private List<CommunityUserDO> latestFollowers = Lists.newArrayList();
	
	/**
	 * 商品マスターの有無です。
	 */
	private boolean hasProductMaster;

	public boolean isHasProductMaster() {
		return hasProductMaster;
	}

	public void setHasProductMaster(boolean hasProductMaster) {
		this.hasProductMaster = hasProductMaster;
	}

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
	 * @return postQuestionCount
	 */
	public long getPostQuestionCount() {
		return postQuestionCount;
	}

	/**
	 * @param postQuestionCount セットする postQuestionCount
	 */
	public void setPostQuestionCount(long postQuestionCount) {
		this.postQuestionCount = postQuestionCount;
	}

	/**
	 * @return postReviewCount
	 */
	public long getPostReviewCount() {
		return postReviewCount;
	}

	/**
	 * @param postReviewCount セットする postReviewCount
	 */
	public void setPostReviewCount(long postReviewCount) {
		this.postReviewCount = postReviewCount;
	}

	/**
	 * @return postImageCount
	 */
	public long getPostImageCount() {
		return postImageCount;
	}

	/**
	 * @param postImageCount セットする postImageCount
	 */
	public void setPostImageCount(long postImageCount) {
		this.postImageCount = postImageCount;
	}

	/**
	 * @return followerCount
	 */
	public long getFollowerCount() {
		return followerCount;
	}

	/**
	 * @param followerCount セットするfollowerCount
	 */
	public void setFollowerCount(long followerCount) {
		this.followerCount = followerCount;
	}

	/**
	 * @return latestFollowers
	 */
	public List<CommunityUserDO> getLatestFollowers() {
		return latestFollowers;
	}

	/**
	 * @param latestFollowers セットするlatestFollowers
	 */
	public void setLatestFollowers(List<CommunityUserDO> latestFollowers) {
		this.latestFollowers = latestFollowers;
	}

	/**
	 * @return followDate
	 */
	public Date getFollowDate() {
		return followDate;
	}

	/**
	 * @param followDate セットする followDate
	 */
	public void setFollowDate(Date followDate) {
		this.followDate = followDate;
	}

	public ProductSatisfactionSummaryVO getProductSatisfactionSummary() {
		return productSatisfactionSummary;
	}

	public void setProductSatisfactionSummary(
			ProductSatisfactionSummaryVO productSatisfactionSummary) {
		this.productSatisfactionSummary = productSatisfactionSummary;
	}


}
