package com.kickmogu.yodobashi.community.service.vo;

import java.util.Date;

/**
 * 商品サマリーのビューオブジェクトです。
 * @author mori
 */
public class ReviewPointSummaryVO extends BaseVO {
	
	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = -1198921212006305240L;
	
	/**
	 * skuです。
	 */
	private String sku;
	
	/**
	 * 投稿質問数です。
	 */
	private long qaTotalCount = 0L;

	/**
	 * 投稿レビュー数です。
	 */
	private long reviewTotalCount = 0L;

	/**
	 * 投稿画像数です。
	 */
	private long postImageCount = 0L;

	/**
	 * 満足度平均評価です。
	 */
	private Double averageRating = 0D;

	/**
	 * フォロー人数です。
	 */
	private long productFollowerCount = 0L;
	
	private Date lastUpdate = null;
	
	/**
	 * @return the sku
	 */
	public String getSku() {
		return sku;
	}

	/**
	 * @param sku the sku to set
	 */
	public void setSku(String sku) {
		this.sku = sku;
	}
	
	/**
	 * @return qaTotalCount
	 */
	public long getQaTotalCount() {
		return qaTotalCount;
	}

	/**
	 * @param qaTotalCount セットする qaTotalCount
	 */
	public void setQaTotalCount(long qaTotalCount) {
		this.qaTotalCount = qaTotalCount;
	}

	/**
	 * @return postReviewCount
	 */
	public long getReviewTotalCount() {
		return reviewTotalCount;
	}

	/**
	 * @param postReviewCount セットする postReviewCount
	 */
	public void setReviewTotalCount(long reviewTotalCount) {
		this.reviewTotalCount = reviewTotalCount;
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
	 * @return satisfactionAvarage
	 */
	public Double getAverageRating() {
		return averageRating;
	}

	/**
	 * @param satisfactionAvarage セットするsatisfactionAvarage
	 */
	public void setAverageRating(Double averageRating) {
		this.averageRating = averageRating;
	}

	/**
	 * @return followerCount
	 */
	public long getProductFollowerCount() {
		return productFollowerCount;
	}

	/**
	 * @param followerCount セットするfollowerCount
	 */
	public void setProductFollowerCount(long productFollowerCount) {
		this.productFollowerCount = productFollowerCount;
	}
	
	public Date getLastUpdate() {
		return this.lastUpdate;
	}
	
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
}
