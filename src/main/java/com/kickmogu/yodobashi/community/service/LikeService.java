/**
 *
 */
package com.kickmogu.yodobashi.community.service;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.LikeTargetType;
import com.kickmogu.yodobashi.community.service.vo.CommunityUserSetVO;

/**
 * いいねサービスです。
 * @author kamiike
 */
public interface LikeService extends CommonService{

	/**
	 * レビューに対してのいいね情報を更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param reviewId レビューID
	 * @param release 削除するかどうか
	 * @return 成功可否  0:かわらない,1:いいね,2:いいね取り消し
	 */
	public int updateLikeReview(
			String communityUserId, String reviewId, boolean release);

	/**
	 * 質問回答に対してのいいね情報を更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param questionAnswerId 質問回答ID
	 * @param release 削除するかどうか
	 * @return 成功可否  0:かわらない,1:いいね,2:いいね取り消し
	 */
	public int updateLikeQuestionAnswer(
			String communityUserId, String questionAnswerId, boolean release);

	/**
	 * 画像に対してのいいね情報を更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param imageId 画像ID
	 * @param release 削除するかどうか
	 * @return 成功可否  0:かわらない,1:いいね,2:いいね取り消し
	 */
	public int updateLikeImage(
			String communityUserId, String imageId, boolean release);
	
	/**
	 * いいね情報の更新をします。
	 * @param targetType コンテンツ体法
	 * @param communityUserId コミュニティユーザーID
	 * @param contentId コンテンツID
	 * @param release 削除するかどうか
	 * @return 成功可否  0:かわらない,1:いいね,2:いいね取り消し
	 */
	public int updateLike(
			LikeTargetType targetType,
			String communityUserId,
			String contentId,
			boolean release);

	/**
	 * 指定した画像に対するいいねユーザーを返します。
	 * @param imageId 画像ID
	 * @param limit 最大取得件数
	 * @return いいねユーザーリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.RARE,
		frequencyComment="画像詳細画面でいいねリンクをクリックしたらAjaxで呼ばれるので頻度は稀",
		refClassNames={"AjaxHtmlLikeUsersController"}
	)
	public SearchResult<CommunityUserSetVO> findLikeCommunityUserByImageId(
			String imageId, int limit);

	/**
	 * 指定したレビューに対するいいねユーザーを返します。
	 * @param reviewId レビューID
	 * @param limit 最大取得件数
	 * @return いいねユーザーリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.RARE,
		frequencyComment="レビュー詳細画面でいいねリンクをクリックしたらAjaxで呼ばれるので頻度は稀",
		refClassNames={"AjaxHtmlLikeUsersController"}
	)
	public SearchResult<CommunityUserSetVO> findLikeCommunityUserByReviewId(
			String reviewId, int limit);

	/**
	 * 指定した質問回答に対するいいねユーザーを返します。
	 * @param questionAnswerId 質問回答ID
	 * @param limit 最大取得件数
	 * @return いいねユーザーリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.RARE,
		frequencyComment="質問詳細画面でいいねリンクをクリックしたらAjaxで呼ばれるので頻度は稀",
		refClassNames={"AjaxHtmlLikeUsersController"}
	)
	public SearchResult<CommunityUserSetVO> findLikeCommunityUserByQuestionAnswerId(
			String questionAnswerId, int limit);
	
}
