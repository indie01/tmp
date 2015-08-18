/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import java.util.List;

import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;

/**
 * ユーザーページの情報エリアのビューオブジェクトです。
 * @author kamiike
 */
public class UserPageInfoAreaVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = -6546449581356217942L;

	/**
	 * ユーザーページのユーザー情報
	 */
	private CommunityUserDO profileCommunityUser;

	/**
	 * Twitterアカウント連携フラグです。
	 */
	private boolean linkTwitter;

	/**
	 * Mixiアカウント連携フラグです。
	 */
	private boolean linkMixi;

	/**
	 * FaceBookアカウント連携フラグです。
	 */
	private boolean linkFacebook;

	/**
	 * 投稿すべての数です
	 */
	private long postAllCount;
	/**
	 * 投稿レビュー数です。
	 */
	private long postReviewCount;

	/**
	 * 投稿質問数です。
	 */
	private long postQuestionCount;

	/**
	 * 投稿質問回答数です。
	 */
	private long postQuestionAnswerCount;

	/**
	 * 投稿画像数です。
	 */
	private long postImageCount;

	/**
	 * 購入商品数です。
	 */
	private long purchaseProductCount;

	/**
	 * 商品マスター数です。
	 */
	private long productMasterCount;
	
	/**
	 * 画像の投稿枚数です。
	 */
	private long userPostedImageCount;
	/**
	 * 商品マスターのリストです。
	 */
	private List<ProductMasterDO> productMasters;

	/**
	 * 購入商品のリストです。
	 */
	private List<PurchaseProductDO> purchaseProducts;

	/**
	 * アダルトコンテンツを保持しているかどうかです。
	 */
	private boolean hasAdult;

	/**
	 * ユーザーをフォローしているかどうかです。
	 */
	private boolean followingFlg;
	
	/**
	 * 画像のリストです。
	 */
	private List<ImageHeaderDO> images;

	/**
	 * @return profileCommunityUser
	 */
	public CommunityUserDO getProfileCommunityUser() {
		return profileCommunityUser;
	}

	/**
	 * @param profileCommunityUser セットする profileCommunityUser
	 */
	public void setProfileCommunityUser(CommunityUserDO profileCommunityUser) {
		this.profileCommunityUser = profileCommunityUser;
	}

	/**
	 * @return linkTwitter
	 */
	public boolean isLinkTwitter() {
		return linkTwitter;
	}

	/**
	 * @param linkTwitter セットする linkTwitter
	 */
	public void setLinkTwitter(boolean linkTwitter) {
		this.linkTwitter = linkTwitter;
	}

	/**
	 * @return linkMixi
	 */
	public boolean isLinkMixi() {
		return linkMixi;
	}

	/**
	 * @param linkMixi セットする linkMixi
	 */
	public void setLinkMixi(boolean linkMixi) {
		this.linkMixi = linkMixi;
	}

	/**
	 * @return linkFacebook
	 */
	public boolean isLinkFacebook() {
		return linkFacebook;
	}

	/**
	 * @param linkFacebook セットする linkFacebook
	 */
	public void setLinkFacebook(boolean linkFacebook) {
		this.linkFacebook = linkFacebook;
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
	 * @return postQuestionAnswerCount
	 */
	public long getPostQuestionAnswerCount() {
		return postQuestionAnswerCount;
	}

	/**
	 * @param postQuestionAnswerCount セットする postQuestionAnswerCount
	 */
	public void setPostQuestionAnswerCount(long postQuestionAnswerCount) {
		this.postQuestionAnswerCount = postQuestionAnswerCount;
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
	 * @return purchaseProductCount
	 */
	public long getPurchaseProductCount() {
		return purchaseProductCount;
	}

	/**
	 * @param purchaseProductCount セットする purchaseProductCount
	 */
	public void setPurchaseProductCount(long purchaseProductCount) {
		this.purchaseProductCount = purchaseProductCount;
	}

	/**
	 * @return productMasterCount
	 */
	public long getProductMasterCount() {
		return productMasterCount;
	}

	/**
	 * @param productMasterCount セットする productMasterCount
	 */
	public void setProductMasterCount(long productMasterCount) {
		this.productMasterCount = productMasterCount;
	}

	/**
	 * @return productMasters
	 */
	public List<ProductMasterDO> getProductMasters() {
		return productMasters;
	}

	/**
	 * @param productMasters セットする productMasters
	 */
	public void setProductMasters(List<ProductMasterDO> productMasters) {
		this.productMasters = productMasters;
	}

	/**
	 * @return purchaseProducts
	 */
	public List<PurchaseProductDO> getPurchaseProducts() {
		return purchaseProducts;
	}

	/**
	 * @param purchaseProducts セットする purchaseProducts
	 */
	public void setPurchaseProducts(List<PurchaseProductDO> purchaseProducts) {
		this.purchaseProducts = purchaseProducts;
	}

	/**
	 * @return hasAdult
	 */
	public boolean isHasAdult() {
		return hasAdult;
	}

	/**
	 * @param hasAdult セットする hasAdult
	 */
	public void setHasAdult(boolean hasAdult) {
		this.hasAdult = hasAdult;
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

	public long getPostAllCount() {
		return postAllCount;
	}

	public void setPostAllCount(long postAllCount) {
		this.postAllCount = postAllCount;
	}

	public List<ImageHeaderDO> getImages() {
		return images;
	}

	public void setImages(List<ImageHeaderDO> images) {
		this.images = images;
	}

	public long getUserPostedImageCount() {
		return userPostedImageCount;
	}

	public void setUserPostedImageCount(long userPostedImageCount) {
		this.userPostedImageCount = userPostedImageCount;
	}

	
}
