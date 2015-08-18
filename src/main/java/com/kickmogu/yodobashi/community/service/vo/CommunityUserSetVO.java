/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;

/**
 * コミュニティユーザー関連の表示情報を集めたビューオブジェクトです。
 * @author kamiike
 *
 */
public class CommunityUserSetVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = 4617721780419213772L;

	/**
	 * コミュニティユーザー
	 */
	private CommunityUserDO communityUser;

	/** 商品マスター */
	private ProductMasterDO productMaster;

	/** 購入商品 */
	private PurchaseProductDO purchaseProduct;

	/**
	 * レビュー数です。
	 */
	private long postReviewCount;

	/**
	 * 質問数です。
	 */
	private long postQuestionCount;

	/**
	 * 質問回答数です。
	 */
	private long postQuestionAnswerCount;

	/**
	 * 画像投稿数です。
	 */
	private long postImageCount;

	/**
	 * 商品マスター数です。
	 */
	private long productMasterCount;

	/**
	 * ユーザーをフォロー済みかどうかです。
	 */
	private boolean followingUser;

	/**
	 * 検索時のマッチ度で使用するスコアです。
	 */
	private long matchScore;

	/**
	 * 共通の関心事の数です。
	 */
	private long commonInterestCount;

	/**
	 * 共通の購入商品数です。
	 */
	private long commonPurchaseProductCount;

	/**
	 * @return the communityUser
	 */
	public CommunityUserDO getCommunityUser() {
		return communityUser;
	}

	/**
	 * @param communityUser the communityUser to set
	 */
	public void setCommunityUser(CommunityUserDO communityUser) {
		this.communityUser = communityUser;
	}

	/**
	 * @return the productMaster
	 */
	public ProductMasterDO getProductMaster() {
		return productMaster;
	}

	/**
	 * @param productMaster the productMaster to set
	 */
	public void setProductMaster(ProductMasterDO productMaster) {
		this.productMaster = productMaster;
	}

	/**
	 * @return the purchaseProduct
	 */
	public PurchaseProductDO getPurchaseProduct() {
		return purchaseProduct;
	}

	/**
	 * @param purchaseProduct the purchaseProduct to set
	 */
	public void setPurchaseProduct(PurchaseProductDO purchaseProduct) {
		this.purchaseProduct = purchaseProduct;
	}

	/**
	 * @return the postReviewCount
	 */
	public long getPostReviewCount() {
		return postReviewCount;
	}

	/**
	 * @param postReviewCount the postReviewCount to set
	 */
	public void setPostReviewCount(long postReviewCount) {
		this.postReviewCount = postReviewCount;
	}

	/**
	 * @return the postQuestionCount
	 */
	public long getPostQuestionCount() {
		return postQuestionCount;
	}

	/**
	 * @param postQuestionCount the postQuestionCount to set
	 */
	public void setPostQuestionCount(long postQuestionCount) {
		this.postQuestionCount = postQuestionCount;
	}

	/**
	 * @return the postquestionAnswerCount
	 */
	public long getPostQuestionAnswerCount() {
		return postQuestionAnswerCount;
	}

	/**
	 * @param postQuestionAnswerCount the postquestionAnswerCount to set
	 */
	public void setPostQuestionAnswerCount(long postQuestionAnswerCount) {
		this.postQuestionAnswerCount = postQuestionAnswerCount;
	}

	/**
	 * @return the postImageCount
	 */
	public long getPostImageCount() {
		return postImageCount;
	}

	/**
	 * @param postImageCount the postImageCount to set
	 */
	public void setPostImageCount(long postImageCount) {
		this.postImageCount = postImageCount;
	}

	/**
	 * @return the productMasterCount
	 */
	public long getProductMasterCount() {
		return productMasterCount;
	}

	/**
	 * @param productMasterCount the productMasterCount to set
	 */
	public void setProductMasterCount(long productMasterCount) {
		this.productMasterCount = productMasterCount;
	}

	/**
	 * @return the followingUser
	 */
	public boolean isFollowingUser() {
		return followingUser;
	}

	/**
	 * @param followingUser the followingUser to set
	 */
	public void setFollowingUser(boolean followingUser) {
		this.followingUser = followingUser;
	}

	/**
	 * @return commonInterestCount
	 */
	public long getCommonInterestCount() {
		return commonInterestCount;
	}

	/**
	 * @param commonInterestCount セットする commonInterestCount
	 */
	public void setCommonInterestCount(long commonInterestCount) {
		this.commonInterestCount = commonInterestCount;
	}

	/**
	 * @return commonPurchaseProductCount
	 */
	public long getCommonPurchaseProductCount() {
		return commonPurchaseProductCount;
	}

	/**
	 * @param commonPurchaseProductCount セットする commonPurchaseProductCount
	 */
	public void setCommonPurchaseProductCount(long commonPurchaseProductCount) {
		this.commonPurchaseProductCount = commonPurchaseProductCount;
	}

	/**
	 * @return matchScore
	 */
	public long getMatchScore() {
		return matchScore;
	}

	/**
	 * @param matchScore セットする matchScore
	 */
	public void setMatchScore(long matchScore) {
		this.matchScore = matchScore;
	}

}
