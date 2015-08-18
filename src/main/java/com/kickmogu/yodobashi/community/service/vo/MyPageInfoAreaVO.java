/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import java.util.List;

import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;

/**
 * マイページの情報エリアのビューオブジェクトです。
 * @author kamiike
 */
public class MyPageInfoAreaVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = -3533373973872859243L;

	/**
	 * Twitterアカウント連携フラグです。
	 */
	private boolean linkTwitter;

	/**
	 * FaceBookアカウント連携フラグです。
	 */
	private boolean linkFacebook;

	/**
	 * 未読のお知らせカウントです。
	 */
	private long noReadInformationCount;
	
	/**
	 * お知らせの数です。
	 */
	private long informationCount;
	
	/**
	 * すべての投稿数です。
	 */
	private long postAllCount;
	/**
	 * 投稿レビュー数です。
	 */
	private long postReviewCount;

	/**
	 * 一時保存レビュー数です。
	 */
	private long temporaryReviewCount;

	/**
	 * 投稿質問数です。
	 */
	private long postQuestionCount;

	/**
	 * 一時保存質問数です。
	 */
	private long temporaryQuestionCount;

	/**
	 * 投稿質問回答数です。
	 */
	private long postQuestionAnswerCount;

	/**
	 * 一時保存質問回答数です。
	 */
	private long temporaryQuestionAnswerCount;

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
	 * 未読お知らせ情報一覧です。
	 */
	private List<InformationDO> noReadInformations;

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
	 * @return noReadInformationCount
	 */
	public long getNoReadInformationCount() {
		return noReadInformationCount;
	}

	/**
	 * @param noReadInformationCount セットする noReadInformationCount
	 */
	public void setNoReadInformationCount(long noReadInformationCount) {
		this.noReadInformationCount = noReadInformationCount;
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
	 * @return temporaryReviewCount
	 */
	public long getTemporaryReviewCount() {
		return temporaryReviewCount;
	}

	/**
	 * @param temporaryReviewCount セットする temporaryReviewCount
	 */
	public void setTemporaryReviewCount(long temporaryReviewCount) {
		this.temporaryReviewCount = temporaryReviewCount;
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
	 * @return temporaryQuestionCount
	 */
	public long getTemporaryQuestionCount() {
		return temporaryQuestionCount;
	}

	/**
	 * @param temporaryQuestionCount セットする temporaryQuestionCount
	 */
	public void setTemporaryQuestionCount(long temporaryQuestionCount) {
		this.temporaryQuestionCount = temporaryQuestionCount;
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
	 * @return temporaryQuestionAnswerCount
	 */
	public long getTemporaryQuestionAnswerCount() {
		return temporaryQuestionAnswerCount;
	}

	/**
	 * @param temporaryQuestionAnswerCount セットする temporaryQuestionAnswerCount
	 */
	public void setTemporaryQuestionAnswerCount(long temporaryQuestionAnswerCount) {
		this.temporaryQuestionAnswerCount = temporaryQuestionAnswerCount;
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

	public long getPostAllCount() {
		return postAllCount;
	}

	public void setPostAllCount(long postAllCount) {
		this.postAllCount = postAllCount;
	}

	public long getInformationCount() {
		return informationCount;
	}

	public void setInformationCount(long informationCount) {
		this.informationCount = informationCount;
	}
	
	public long getTemporaryAllCount(){
		return temporaryReviewCount + temporaryQuestionCount + temporaryQuestionAnswerCount;
	}

	public List<InformationDO> getNoReadInformations() {
		return noReadInformations;
	}

	public void setNoReadInformations(List<InformationDO> noReadInformations) {
		this.noReadInformations = noReadInformations;
	}

}
