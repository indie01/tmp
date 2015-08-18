/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import java.util.List;
import java.util.Map;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.LikeTargetType;


/**
 * いいね DAO です。
 * @author kamiike
 *
 */
public interface LikeDao {

	/**
	 * レビューに対するいいね数情報を返します。
	 * @param reviewIds レビューIDリスト
	 * @return レビューに対するするいいね数情報
	 */
	public Map<String, Long> loadReviewLikeCountMap(List<String> reviewIds);

	/**
	 * 質問に紐づく回答に対するいいね数情報を返します。
	 * @param questionId 質問ID
	 * @return 質問に紐づく回答に対するいいね数
	 */
	public long loadQuestionLikeCount(String questionId);

	/**
	 * 質問に紐付く回答に対するいいね数情報を返します。
	 * @param questionIds 質問回答IDリスト
	 * @return 質問に紐付く回答に対するするいいね数情報
	 */
	public Map<String, Long> loadQuestionLikeCountMap(List<String> questionIds);

	/**
	 * 質問回答に対するいいね数情報を返します。
	 * @param questionAnswerIds 質問回答IDリスト
	 * @return 質問回答に対するするいいね数情報
	 */
	public Map<String, Long> loadQuestionAnswerLikeCountMap(List<String> questionAnswerIds);

	/**
	 * 画像に対するいいね数情報を返します。
	 * @param imageIds 画像IDリスト
	 * @return 画像に対するするいいね数情報
	 */
	public Map<String, Long> loadImageLikeCountMap(List<String> imageIds);

	/**
	 * 画像に対するいいね数情報を返します。
	 * @param imageIds 画像IDリスト
	 * @return 画像に対するするいいね数情報
	 */
	public Map<String, Long> loadImageSetLikeCountMap(List<String> imageSetIds);
	
	/**
	 * 指定したコミュニティユーザー、コンテンツID、いいねタイプのいいね
	 * が存在するか判定します。
	 * @param communityUserId コミュニティユーザーID
	 * @param contentsId 対象となるコンテンツID
	 * @param type コンテンツタイプ
	 * @return いいね済みの場合、true
	 */
	public boolean existsLike(
			String communityUserId, String contentsId, LikeTargetType type);

	/**
	 * 指定したコミュニティユーザー、コンテンツID、いいねタイプのいいね
	 * を削除します。
	 * @param communityUserId コミュニティユーザーID
	 * @param contentsId 対象となるコンテンツID
	 * @param type コンテンツタイプ
	 * @return いいねID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.MEDIUM, frequencyComment="いいね削除の頻度はそんなに高くない")
	public String deleteLike(
			String communityUserId,
			String contentsId,
			LikeTargetType type);

	/**
	 * いいね情報を新規に作成します。
	 * @param like いいね
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.HIGH, frequencyComment="いいね登録の頻度は高い")
	public void createLike(
			LikeDO like);

	/**
	 * いいね情報のインデックスを更新します。
	 * @param likeId いいねID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.HIGH, frequencyComment="いいね登録の頻度は高い")
	public void updateLikeInIndex(
			String likeId);


	/**
	 * レビューのいいね情報マップを返します。
	 * @param communityUserId 対象となるコミュニティユーザーID
	 * @param reviewIds レビューIDリスト
	 * @return レビューのいいね情報マップ
	 */
	public Map<String, Boolean> loadReviewLikeMap(
			String communityUserId, List<String> reviewIds);

	/**
	 * 質問回答のいいね情報マップを返します。
	 * @param communityUserId 対象となるコミュニティユーザーID
	 * @param questionAnswerIds 質問回答IDリスト
	 * @return 質問回答のいいね情報マップ
	 */
	public Map<String, Boolean> loadQuestionAnswerLikeMap(
			String communityUserId, List<String> questionAnswerIds);

	/**
	 * 画像のいいね情報マップを返します。
	 * @param communityUserId 対象となるコミュニティユーザーID
	 * @param imageIds 画像IDリスト
	 * @return 画像のいいね情報マップ
	 */
	public Map<String, Boolean> loadImageLikeMap(
			String communityUserId, List<String> imageIds);

	/**
	 * 投稿したレビューにいいねをしたユーザーを重複を除いて返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findDistinctLikeUserByReview(
			String communityUserId, int limit, int offset);

	/**
	 * 投稿した質問回答にいいねをしたユーザーを重複を除いて返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findDistinctLikeUserByQuestionAnswer(
			String communityUserId, int limit, int offset);

	/**
	 * 投稿した画像にいいねをしたユーザーを重複を除いて返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findDistinctLikeUserByImage(
			String communityUserId, int limit, int offset);

	/**
	 * 指定したコンテンツに対するいいねを返します。
	 * @param type タイプ
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param contentsId コンテンツID
	 * @param limit 最大取得件数
	 * @return コメントリスト
	 */
	public SearchResult<LikeDO> findLikeByContentsId(
			LikeTargetType type,
			String excludeCommunityUserId,
			String contentsId,
			int limit);

	public void loadContentsLikeCountMap(
			List<String> reviewIds, 
			List<String> questionIds, 
			List<String> questionAnswerIds, 
			List<String> imageSetIds, 
			List<String> imageIds,
			Map<String, Long> reviewCountMap, 
			Map<String, Long> questionCountMap, 
			Map<String, Long> questionAnswerCountMap, 
			Map<String, Long> imageSetCountMap, 
			Map<String, Long> imageCountMap);

}
