/**
 *
 */
package com.kickmogu.yodobashi.community.service;

import java.util.List;
import java.util.Map;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.VotingDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.VotingTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.VotingType;
import com.kickmogu.yodobashi.community.service.vo.CommunityUserSetVO;

/**
 * いいねサービスです。
 * @author kamiike
 */
public interface VotingService {
	
	/**
	 * レビューに対する参考になった数情報を返します。
	 * @param reviewIds レビューIDリスト
	 * @return レビューに対するする参考になった数情報
	 */
	public Map<String, Long[]> loadReviewVotingCountMap(List<String> reviewIds);
	
	
	public Map<String, Long[]> loadContentsVotingCountMap(VotingTargetType targetType, List<String> contentIds);
	/**
	 * レビューに対してのいいね情報を更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param reviewId レビューID
	 * @param release 削除するかどうか
	 * @return 成功可否
	 */
	public boolean updateVotingReview(
			String communityUserId,
			String reviewId,
			VotingType type);

	/**
	 * 質問回答に対してのいいね情報を更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param questionAnswerId 質問回答ID
	 * @param release 削除するかどうか
	 * @return 成功可否
	 */
	public boolean updateVotingQuestionAnswer(
			String communityUserId,
			String questionAnswerId,
			VotingType type);

	/**
	 * 画像に対してのいいね情報を更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param imageId 画像ID
	 * @param release 削除するかどうか
	 * @return 成功可否
	 */
	public boolean updateVotingImage(
			String communityUserId,
			String imageId,
			VotingType type);
	
	/**
	 * 
	 * @param communityUserId コミュニティユーザーID
	 * @param contentsId コンテンツID
	 * @param votingType 参考になった「はい：いいえ」
	 * @param votingTargetType コンテンツタイプ
	 * @return
	 */
	public boolean updateVoting(
			String communityUserId, 
			String contentsId,
			VotingType votingType,
			VotingTargetType votingTargetType);
	/**
	 * 指定した画像に対するいいねを返します。
	 * @param imageId 画像ID
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param limit 最大取得件数
	 * @return いいねリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.LOW,
		frequencyComment="商品画像詳細画面で3人以上いいねがいたら呼ばれるので頻度は低",
		refClassNames={"AjaxJsonProductImageDetailController"}
	)
	public SearchResult<VotingDO> findVotingByImageId(
			String imageId, String excludeCommunityUserId, int limit);

	/**
	 * 指定したレビューに対するいいねを返します。
	 * @param reviewId レビューID
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param limit 最大取得件数
	 * @return いいねリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.LOW,
		frequencyComment="レビュー詳細画面で3人以上いいねがいたら呼ばれるので頻度は低",
		refClassNames={"AjaxJsonProductReviewDetailController"}
	)
	public SearchResult<VotingDO> findVotingByReviewId(
			String reviewId, String excludeCommunityUserId, int limit);

	/**
	 * 指定した質問回答に対するいいねを返します。
	 * @param questionAnswerId 質問回答ID
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param limit 最大取得件数
	 * @return いいねリスト
	 */
	@PerformanceTest(type=Type.SELECT,
			frequency=Frequency.LOW,
			frequencyComment="質問詳細画面で回答に3人以上いいねがいたら呼ばれるので頻度は低",
			refClassNames={"AjaxJsonProductQuestionDetailController"}
		)	
	public SearchResult<VotingDO> findVotingByQuestionAnswerId(
			String questionAnswerId, String excludeCommunityUserId, int limit);

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
	public SearchResult<CommunityUserSetVO> findVotingCommunityUserByImageId(
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
	public SearchResult<CommunityUserSetVO> findVotingCommunityUserByReviewId(
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
	public SearchResult<CommunityUserSetVO> findVotingCommunityUserByQuestionAnswerId(
			String questionAnswerId, int limit);

}
