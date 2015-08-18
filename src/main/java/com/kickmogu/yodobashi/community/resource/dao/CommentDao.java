/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommentTargetType;


/**
 * コメント DAO です。
 * @author kamiike
 *
 */
public interface CommentDao {

	/**
	 * レビューのコメント情報マップを返します。
	 * @param communityUserId 対象となるコミュニティユーザーID
	 * @param reviewIds レビューIDリスト
	 * @return レビューのコメント情報マップ
	 */
	public Map<String, Boolean> loadReviewCommentMap(
			String communityUserId, List<String> reviewIds);

	/**
	 * 質問回答のコメント情報マップを返します。
	 * @param communityUserId 対象となるコミュニティユーザーID
	 * @param questionAnswerIds 質問回答IDリスト
	 * @return 質問回答のコメント情報マップ
	 */
	public Map<String, Boolean> loadQuestionAnswerCommentMap(
			String communityUserId, List<String> questionAnswerIds);

	/**
	 * 画像のコメント情報マップを返します。
	 * @param communityUserId 対象となるコミュニティユーザーID
	 * @param imageIds 画像IDリスト
	 * @return 画像のコメント情報マップ
	 */
	public Map<String, Boolean> loadImageCommentMap(
			String communityUserId, List<String> imageIds);

	/**
	 * レビューに対するコメント数情報を返します。
	 * @param reviewIds レビューIDリスト
	 * @return レビューに対するコメント数情報
	 */
	public Map<String, Long> loadReviewCommentCountMap(List<String> reviewIds);

	/**
	 * レビューに対するコメント数情報を返します。
	 * @param reviewIds レビューIDリスト
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @return レビューに対するコメント数情報
	 */
	public Map<String, Long> loadReviewCommentCountMap(List<String> reviewIds,
			String excludeCommunityUserId);

	/**
	 * 質問に紐付く回答に対するコメント数情報を返します。
	 * @param questionIds 質問IDリスト
	 * @return 質問に紐付く回答に対するコメント数情報
	 */
	public Map<String, Long> loadQuestionCommentCountMap(List<String> questionIds);

	/**
	 * 質問回答に対するコメント数情報を返します。
	 * @param questionAnswerIds 質問回答IDリスト
	 * @return 質問回答に対するコメント数情報
	 */
	public Map<String, Long> loadQuestionAnswerCommentCountMap(List<String> questionAnswerIds);

	/**
	 * 質問回答に対するコメント数情報を返します。
	 * @param questionAnswerIds 質問回答IDリスト
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @return 質問回答に対するコメント数情報
	 */
	public Map<String, Long> loadQuestionAnswerCommentCountMap(List<String> questionAnswerIds,
			String excludeCommunityUserId);

	/**
	 * 画像に対するコメント数情報を返します。
	 * @param imageIds 画像IDリスト
	 * @return 画像に対するコメント数情報
	 */
	public Map<String, Long> loadImageCommentCountMap(List<String> imageIds);

	/**
	 * 画像に対するコメント数情報を返します。
	 * @param imageIds 画像IDリスト
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @return 画像に対するコメント数情報
	 */
	public Map<String, Long> loadImageCommentCountMap(List<String> imageIds,
			String excludeCommunityUserId);

	/**
	 * 画像セットに対するコメント数情報を返します。
	 * @param imageSetIds 画像セットIDリスト
	 * @return 画像セットに対するコメント数情報
	 */
	public Map<String, Long> loadImageSetCommentCountMap(List<String> imageSetIds);

	/**
	 * コメント情報を削除します。
	 * @param commentId コメントID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.MEDIUM, frequencyComment="コメントの削除はそんなに頻度は高くないと仮定")
	public void deleteComment(String commentId, boolean mngToolOperation);

	/**
	 * コメント情報を保存します。
	 * @param comment コメント
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.HIGH, frequencyComment="コメント投入の頻度は高い")
	public void saveComment(CommentDO comment);

	/**
	 * コメント情報のインデックスを更新します。
	 * @param commentId コメントID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.HIGH, frequencyComment="コメント投入の頻度は高い")
	public void updateCommentInIndex(
			String commentId);

	/**
	 * 指定したコンテンツに対するコメントを返します。
	 * @param type タイプ
	 * @param contentsId コンテンツID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return コメントリスト
	 */
	public SearchResult<CommentDO> findCommentByContentsId(
			CommentTargetType type,
			String contentsId,
			List<String> excludeCommentIds,
			int limit,
			Date offsetTime,
			boolean previous);

	public long moreCountByContentsId(CommentTargetType type,
			String contentsId,
			List<String> excludeCommentIds,
			Date offsetTime,
			boolean previous);
	
	/**
	 * 指定したコメント情報を返します。
	 * @param commentId コメントID
	 * @return コメント情報
	 */
	public CommentDO loadComment(String commentId);

	/**
	 * 指定したコメント情報を返します。
	 * @param commentId コメントID
	 * @param condition 条件
	 * @return コメント情報
	 */
	public CommentDO loadComment(String commentId, Condition condition);

	/**
	 * 指定したコメント情報をインデックス情報から返します。
	 * @param commentId コメントID
	 * @return コメント情報
	 */
	public CommentDO loadCommentFromIndex(String commentId);

	public CommentDO loadCommentFromIndex(String commentId, boolean includeDeleteContents);

	/**
	 * 指定した日付に指定したコミュニティユーザーの投稿レビューに
	 * コメントがついたものを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param targetDate 対象日
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return レビューリスト
	 */
	public SearchResult<CommentDO> findCommentReviewByCommunityUserId(
			String communityUserId, Date targetDate, int limit, int offset);

	/**
	 * 指定した日付に指定したコミュニティユーザーの投稿回答に
	 * コメントがついたものを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param targetDate 対象日
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return 投稿回答リスト
	 */
	public SearchResult<CommentDO> findCommentQuestionAnswerByCommunityUserId(
			String communityUserId, Date targetDate, int limit, int offset);

	/**
	 * 指定した日付に指定したコミュニティユーザーの投稿画像に
	 * コメントがついたものを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param targetDate 対象日
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return 投稿画像リスト
	 */
	public SearchResult<CommentDO> findCommentImageByCommunityUserId(
			String communityUserId, Date targetDate, int limit, int offset);

	public SearchResult<CommentDO> findCommentImageByCommunityUserIdForMR(
			String communityUserId, Date targetDate, int limit, int offset);


	public void loadContentsCommentCountMap(List<String> reviewIds, List<String> questionIds, List<String> questionanswerIds, List<String> imageSetIds,
			Map<String, Long> reviewCountMap, Map<String, Long> questionCountMap, Map<String, Long> questionAnswerCountMap, Map<String, Long> imageSetCountMap);

}
