/**
 *
 */
package com.kickmogu.yodobashi.community.service;

/**
 * メール送信サービスです。
 * @author kamiike
 */
public interface MailService {

	/**
	 * コメント投稿直後に関係する通知メールを送信します。
	 * @param commentId コメントID
	 */
	public void sendNotifyMailForJustAfterCommentSubmit(
			String commentId);

	/**
	 * コミュニティユーザーのフォロー通知メールを送信します。
	 * @param communityUserId コミュニティユーザーID
	 * @param followCommunityUserId フォローするコミュニティユーザーID
	 */
	public void sendFollowCommunityUserNotifyMail(
			String communityUserId,
			String followCommunityUserId);

	/**
	 * 質問投稿直後に関係する通知メールを送信します。
	 * @param questionId 質問
	 * @param sku SKU
	 * @param communityUserId コミュニティユーザーID
	 */
	public void sendNotifyMailForJustAfterQuestionSubmit(
			String questionId, String sku, String communityUserId);

	/**
	 * 質問回答投稿直後に関係する通知メールを送信します。
	 * @param questionAnswerId 質問回答ID
	 */
	public void sendNotifyMailForJustAfterQuestionAnswerSubmit(
			String questionAnswerId);

	/**
	 * レビュー投稿直後に関係する通知メールを送信します。
	 * @param reviewId レビュー
	 * @param sku SKU
	 * @param communityUserId コミュニティユーザーID
	 */
	public void sendNotifyMailForJustAfterReviewSubmit(
			String reviewId, String sku, String communityUserId);

	/**
	 * 画像投稿直後に関係する通知メールを送信します。
	 * @param imageSetId 画像セットID
	 * @param sku SKU
	 * @param communityUserId コミュニティユーザーID
	 */
	public void sendNotifyMailForJustAfterImageSubmit(
			String imageSetId, String sku, String communityUserId);

	/**
	 * 登録完了メールを送信します。
	 * @param communityUserId コミュニティユーザーID
	 */
	public void sendRegistrationCompleteMail(String communityUserId);

	/**
	 * 一時停止通知メールを送信します。
	 * @param communityUserId コミュニティユーザーID
	 */
	public void sendStopNotifyMail(String communityUserId);

}
