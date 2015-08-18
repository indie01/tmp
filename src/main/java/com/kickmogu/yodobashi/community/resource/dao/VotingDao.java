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
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.VotingDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.VotingTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.VotingType;


/**
 * 参考になった DAO です。
 * @author sugimoto
 *
 */
public interface VotingDao {

	/**
	 * 指定のユーザーおよびコンテンツの参考になった情報を取得する。
	 * @param communityUserId コミュニティユーザーID
	 * @param contentsId コンテンツID
	 * @param targetType コンテンツタイプ
	 * @return 参考になった情報
	 */
	public VotingDO loadVoting(String communityUserId, String contentsId, VotingTargetType targetType);
	/**
	 * レビューに対する参考になった数情報を返します。
	 * @param reviewIds レビューIDリスト
	 * @return レビューに対するする参考になった数情報
	 */
	public Map<String, Long[]> loadReviewVotingCountMap(List<String> reviewIds);

	/**
	 * 質問に紐づく回答に対する参考になった数情報を返します。
	 * @param questionId 質問ID
	 * @return 質問に紐づく回答に対する参考になった数
	 */
	public Long[] loadQuestionVotingCount(String questionId);

	/**
	 * 質問に紐付く回答に対する参考になった数情報を返します。
	 * @param questionIds 質問回答IDリスト
	 * @return 質問に紐付く回答に対するする参考になった数情報
	 */
	public Map<String, Long[]> loadQuestionVotingCountMap(List<String> questionIds);

	/**
	 * 質問回答に対する参考になった数情報を返します。
	 * @param questionAnswerIds 質問回答IDリスト
	 * @return 質問回答に対するする参考になった数情報
	 */
	public Map<String, Long[]> loadQuestionAnswerVotingCountMap(List<String> questionAnswerIds);

	/**
	 * 画像に対する参考になった数情報を返します。
	 * @param imageIds 画像IDリスト
	 * @return 画像に対するする参考になった数情報
	 */
	public Map<String, Long[]> loadImageVotingCountMap(List<String> imageIds);

	/**
	 * 画像に対する参考になった数情報を返します。
	 * @param imageIds 画像IDリスト
	 * @return 画像に対するする参考になった数情報
	 */
	public Map<String, Long[]> loadImageSetVotingCountMap(List<String> imageSetIds);
	
	public Map<String, Long[]> loadContentsVotingCountMap(
			VotingTargetType targetType,
			List<String> contentsIds,
			String otherFacetField);
	
	/**
	 * 指定したコミュニティユーザー、コンテンツID、参考になったタイプの参考になった
	 * が存在するか判定します。
	 * @param communityUserId コミュニティユーザーID
	 * @param contentsId 対象となるコンテンツID
	 * @param type コンテンツタイプ
	 * @return 参考になった済みの場合、true
	 */
	public boolean existsVoting(
			String communityUserId,
			String contentsId,
			VotingTargetType type);

	/**
	 * 指定したコミュニティユーザー、コンテンツID、参考になったタイプの参考になった
	 * を削除します。
	 * @param communityUserId コミュニティユーザーID
	 * @param contentsId 対象となるコンテンツID
	 * @param type コンテンツタイプ
	 * @return 参考になったID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.MEDIUM, frequencyComment="参考になった削除の頻度はそんなに高くない")
	public String deleteVoting(
			String communityUserId,
			String contentsId,
			VotingTargetType type);

	/**
	 * 参考になった情報を新規に作成します。
	 * @param like 参考になった
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.HIGH, frequencyComment="参考になった登録の頻度は高い")
	public void createVoting(VotingDO Voting);
	
	/**
	 * 参考になった情報を更新します。
	 * @param voting 参考になった情報
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.HIGH, frequencyComment="参考になった登録の頻度は高い")
	public void updateVoting(VotingDO voting);
	/**
	 * 参考になった情報のインデックスを更新します。
	 * @param likeId 参考になったID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.HIGH, frequencyComment="参考になった登録の頻度は高い")
	public void updateVotingInIndex(String votingId);


	/**
	 * レビューの参考になった情報マップを返します。
	 * @param communityUserId 対象となるコミュニティユーザーID
	 * @param reviewIds レビューIDリスト
	 * @return レビューの参考になった情報マップ
	 */
	public Map<String, VotingType> loadReviewVotingMap(
			String communityUserId,
			List<String> reviewIds);

	/**
	 * 質問回答の参考になった情報マップを返します。
	 * @param communityUserId 対象となるコミュニティユーザーID
	 * @param questionAnswerIds 質問回答IDリスト
	 * @return 質問回答の参考になった情報マップ
	 */
	public Map<String, VotingType> loadQuestionAnswerVotingMap(
			String communityUserId,
			List<String> questionAnswerIds);

	/**
	 * 画像の参考になった情報マップを返します。
	 * @param communityUserId 対象となるコミュニティユーザーID
	 * @param imageIds 画像IDリスト
	 * @return 画像の参考になった情報マップ
	 */
	public Map<String, VotingType> loadImageVotingMap(
			String communityUserId,
			List<String> imageIds);

	/**
	 * 投稿したレビューに参考になったをしたユーザーを重複を除いて返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findDistinctVotingUserByReview(
			String communityUserId,
			int limit,
			int offset);

	/**
	 * 投稿した質問回答に参考になったをしたユーザーを重複を除いて返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findDistinctVotingUserByQuestionAnswer(
			String communityUserId,
			int limit,
			int offset);

	/**
	 * 投稿した画像に参考になったをしたユーザーを重複を除いて返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findDistinctVotingUserByImage(
			String communityUserId,
			int limit,
			int offset);

	/**
	 * 指定したコンテンツに対する参考になったを返します。
	 * @param type タイプ
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param contentsId コンテンツID
	 * @param limit 最大取得件数
	 * @return コメントリスト
	 */
	public SearchResult<VotingDO> findVotingByContentsId(
			VotingTargetType type,
			String excludeCommunityUserId,
			String contentsId,
			int limit);

	public void loadContentsVotingCountMap(
			List<String> reviewIds, 
			List<String> questionIds, 
			List<String> questionAnswerIds, 
			List<String> imageSetIds, 
			List<String> imageIds,
			Map<String, Long[]> reviewCountMap, 
			Map<String, Long[]> questionCountMap, 
			Map<String, Long[]> questionAnswerCountMap, 
			Map<String, Long[]> imageSetCountMap, 
			Map<String, Long[]> imageCountMap);

}
