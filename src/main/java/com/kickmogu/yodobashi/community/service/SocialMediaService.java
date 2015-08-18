/**
 *
 */
package com.kickmogu.yodobashi.community.service;

import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;


/**
 * ソーシャルメディア連携サービスです。
 * @author kamiike
 *
 */
public interface SocialMediaService {

	/**
	 * レビュー投稿直後にソーシャルメディアに通知します。
	 * @param reviewId レビューID
	 * @param communityUserId コミュニティユーザーID
	 */
	public void notifySocialMediaForReviewSubmit(String reviewId, String communityUserId);

	/**
	 * 質問投稿直後にソーシャルメディアに通知します。
	 * @param questionId 質問ID
	 * @param communityUserId コミュニティユーザーID
	 */
	public void notifySocialMediaForQuestionSubmit(String questionId, String communityUserId);

	/**
	 * 質問回答投稿直後にソーシャルメディアに通知します。
	 * @param questionAnswerId 質問回答ID
	 * @param communityUserId コミュニティユーザーID
	 */
	public void notifySocialMediaForQuestionAnswerSubmit(String questionAnswerId, String communityUserId);

	/**
	 * 画像回答投稿直後にソーシャルメディアに通知します。
	 * @param imageId 画像ID
	 * @param imageSetId 画像セットID
	 * @param communityUserId コミュニティユーザーID
	 */
	public void notifySocialMediaForImageSubmit(String imageId,
			String imageSetId, String communityUserId);

	/**
	 * 商品マスターランクイン直後にソーシャルメディアに通知します。
	 * @param productMaster 商品マスター
	 * @param communityUserId コミュニティユーザーID
	 */
	public void notifySocialMediaForProductMasterRankIn(
			ProductMasterDO productMaster,
			String communityUserId);
}
