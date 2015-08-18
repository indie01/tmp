/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;

/**
 * コミュニティユーザーフォローのビューオブジェクトです。
 * @author kamiike
 *
 */
public class CommunityUserFollowVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = 3827300696965366972L;

	/**
	 * コミュニティユーザーです。
	 */
	private CommunityUserDO communityUser;

	/**
	 * フォローフラグ です。
	 */
	private boolean followingFlg;

	/**
	 * 投稿レビュー数です。
	 */
	private long postReviewCount;

	/**
	 * 投稿質問数です。
	 */
	private long postQuestionCount;

	/**
	 * 投稿回答数です。
	 */
	private long postAnswerCount;

	/**
	 * 投稿画像数です。
	 */
	private long postImageCount;

	/**
	 * 商品マスター数です。
	 */
	private long productMasterCount;

	/**
	 * フォローしているユーザー数です。
	 */
	private long followUserCount;

	/**
	 * フォローされているユーザー数です。
	 */
	private long followerUserCount;

	/**
	 * フォロー日時です。
	 */
	private Date followDate;

	/**
	 * 最新のフォローユーザーのリストです。
	 */
	private List<CommunityUserDO> latestFollowUsers = Lists.newArrayList();

	/**
	 * 最新のフォロワーユーザーのリストです。
	 */
	private List<CommunityUserDO> latestFollowerUsers = Lists.newArrayList();

	/**
	 * 商品マスターのリストです。
	 * メール用フィールドです。
	 */
	private List<ProductMasterDO> productMasters = Lists.newArrayList();

	/**
	 * @return communityUser
	 */
	public CommunityUserDO getCommunityUser() {
		return communityUser;
	}

	/**
	 * @param communityUser セットする communityUser
	 */
	public void setCommunityUser(CommunityUserDO communityUser) {
		this.communityUser = communityUser;
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
	 * @return postAnswerCount
	 */
	public long getPostAnswerCount() {
		return postAnswerCount;
	}

	/**
	 * @param postAnswerCount セットする postAnswerCount
	 */
	public void setPostAnswerCount(long postAnswerCount) {
		this.postAnswerCount = postAnswerCount;
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
	 * @return followUserCount
	 */
	public long getFollowUserCount() {
		return followUserCount;
	}

	/**
	 * @param followUserCount セットする followUserCount
	 */
	public void setFollowUserCount(long followUserCount) {
		this.followUserCount = followUserCount;
	}

	/**
	 * @return followerUserCount
	 */
	public long getFollowerUserCount() {
		return followerUserCount;
	}

	/**
	 * @param followerUserCount セットする followerUserCount
	 */
	public void setFollowerUserCount(long followerUserCount) {
		this.followerUserCount = followerUserCount;
	}

	/**
	 * @return latestFollowUsers
	 */
	public List<CommunityUserDO> getLatestFollowUsers() {
		return latestFollowUsers;
	}

	/**
	 * @param latestFollowUsers セットする latestFollowUsers
	 */
	public void setLatestFollowUsers(List<CommunityUserDO> latestFollowUsers) {
		this.latestFollowUsers = latestFollowUsers;
	}

	/**
	 * @return latestFollowerUsers
	 */
	public List<CommunityUserDO> getLatestFollowerUsers() {
		return latestFollowerUsers;
	}

	/**
	 * @param latestFollowerUsers セットする latestFollowerUsers
	 */
	public void setLatestFollowerUsers(List<CommunityUserDO> latestFollowerUsers) {
		this.latestFollowerUsers = latestFollowerUsers;
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

}
