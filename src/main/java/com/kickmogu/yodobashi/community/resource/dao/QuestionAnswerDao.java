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
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.Verification;


/**
 * 質問 DAO です。
 * @author kamiike
 *
 */
public interface QuestionAnswerDao {

	/**
	 * 指定した条件の質問情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param questionId 質問ID
	 * @param condition 条件
	 * @return 質問情報リスト
	 */
	public List<QuestionAnswerDO> findQuestionAnswerByCommunityUserIdAndQuestionId(
			String communityUserId, String questionId, Condition condition);
	
	public List<QuestionAnswerDO> findQuestionAnswerByCommunityUserIdAndQuestionIds(
			String communityUserId, List<String> questionIds, Condition condition);
	
	public QuestionAnswerDO getNewSaveQuestionAnswerByCommunityUserId(
			String communityUserId, String sku);

	/**
	 * 質問回答情報を保存します。
	 * @param questionAnswer 質問回答
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.MEDIUM, frequencyComment="質問回答の登録の頻度は中くらい")
	public void saveQuestionAnswer(
			QuestionAnswerDO questionAnswer);

	/**
	 * 指定した質問回答を削除します。
	 * @param questionAnswerId 質問回答ID
	 * @param logical 論理削除かどうか
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="質問回答の削除の頻度は稀")
	public void deleteQuestionAnswer(
			String questionAnswerId,
			boolean logical,
			boolean mngToolOperation);

	/**
	 * 質問回答のインデックスを更新します。
	 * @param questionAnswerId 質問回答ID
	 * @return 質問回答情報
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.MEDIUM, frequencyComment="質問回答の登録の頻度は中くらい")
	public QuestionAnswerDO updateQuestionAnswerInIndex(String questionAnswerId);

	public QuestionAnswerDO updateQuestionAnswerInIndex(String questionAnswerId, boolean mngToolOperation);
	/**
	 * 指定した質問回答情報を返します。
	 * @param questionAnswerId 質問回答ID
	 * @param condition 条件
	 * @param withLock ロックを取得するかどうか
	 * @return 質問回答情報
	 */
	public QuestionAnswerDO loadQuestionAnswer(
			String questionAnswerId, Condition condition, boolean withLock);

	/**
	 * 指定した商品の質問に回答を書いたユーザーを重複を除いて返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findDistinctAnswererBySKU(
			String sku, int limit, int offset);
	public SearchResult<CommunityUserDO> findDistinctAnswererBySKU(
			String sku, int limit, int offset, boolean excludeProduct);

	/**
	 * 指定した商品の質問に回答を書いたユーザーを重複を除いて返します。
	 * @param skus SKUリスト
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findDistinctAnswererBySKU(
			List<String> skus, int limit, int offset);

	/**
	 * フォローした商品の質問に回答を書いたユーザーを重複を除いて返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findDistinctAnswererByFollowProduct(
			String communityUserId, int limit, int offset);

	/**
	 * 投稿した質問に回答を書いたユーザーを重複を除いて返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findDistinctAnswererByPostQuestion(
			String communityUserId, int limit, int offset);

	/**
	 * フォローした質問に回答を書いたユーザーを重複を除いて返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findDistinctAnswererByFollowQuestion(
			String communityUserId, int limit, int offset);

	/**
	 * 購入した商品の質問に回答を書いたユーザーを重複を除いて返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param publicSetting 非公開情報を表示する場合はtrue
	 * @return コミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findDistinctAnswererByQuestionForPurchaseProduct(
			String communityUserId, int limit, int offset, boolean publicSetting);

	/**
	 * 指定した質問に回答を書いている人を返します。
	 * @param questionId 質問ID
	 * @param withoutAnswerId 対象から外す質問回答ID
	 * @param asc 昇順ソート
	 * @return 質問回答を書いているコミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findAnswerCommunityUserByQuestionId(
			String questionId, ContentsStatus[] statuses, String withoutAnswerId, boolean asc);

	/**
	 * 質問の回答者数情報を返します。
	 * @param questionIds 質問IDリスト
	 * @return 質問の回答者数情報
	 */
	public Map<String, Long> loadQuestionAnswerCountMapByQuestionId(List<String> questionIds);

	/**
	 * 質問回答数情報を返します。
	 * @param skus SKUリスト
	 * @return 質問回答数情報
	 */
	public Map<String, Long> loadQuestionAnswerCountMapBySKU(List<String> skus);

	/**
	 * 質問回答数情報を返します。
	 * @param communityUserIds コミュニティユーザーIDのリスト
	 * @return 質問回答数情報
	 */
	public Map<String, Long> loadQuestionAnswerCountMapByCommunityUserId(
			List<String> communityUserIds);

	/**
	 * 質問回答のスコア情報をインデックスも合わせて更新します。
	 * @param questionAnswer 質問回答
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="バッチ処理なのでテスト対象外")
	public void updateQuestionAnswerScoreWithIndex(QuestionAnswerDO questionAnswer);
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="バッチ処理なのでテスト対象外")
	public void updateQuestionAnswerScoreWithIndexForBatch(QuestionAnswerDO questionAnswer);
	public void updateQuestionAnswerScoreWithIndexForBatchBegin(int bulkSize);
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="バッチ処理なのでテスト対象外")
	public void updateQuestionAnswerScoreWithIndexForBatchEnd();

	/**
	 * 指定した質問中で一番スコアの高い回答を返します。
	 * @param questionId 質問ID
	 * @return 一番スコアの高い回答
	 */
	public QuestionAnswerDO loadHighScoreQuestionAnswerByQuestionId(String questionId);

	/**
	 * 指定したコミュニティユーザーが投稿した質問回答を回答投稿日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param status ステータス
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 質問回答一覧
	 */
	public SearchResult<QuestionAnswerDO> findQuestionAnswerByCommunityUserId(
			String communityUserId,
			String excludeAnswerId,
			int limit,
			Date offsetTime,
			boolean previous);
	
	public SearchResult<QuestionAnswerDO> findTemporaryQuestionAnswerByCommunityUserId(
			String communityUserId,
			String excludeAnswerId,
			int limit,
			Date offsetTime,
			boolean previous);

	/**
	 * 指定した質問に対して投稿した質問回答を回答投稿日時順（降順）に返します。
	 * @param questionId 質問ID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 質問回答一覧
	 */
	public SearchResult<QuestionAnswerDO> findNewQuestionAnswerByQuestionId(
			String questionId, String excludeAnswerId, int limit, Date offsetTime, boolean previous);

	/**
	 * 指定した質問に対して投稿した質問回答を適合度順（降順）に返します。
	 * @param questionId 質問ID
	 * @param limit 最大取得件数
	 * @param offsetMatchScore 検索開始適合度
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 質問回答一覧
	 */
	public SearchResult<QuestionAnswerDO> findMatchQuestionAnswerByQuestionId(
			String questionId, String excludeAnswerId, int limit, Double offsetMatchScore,
			Date offsetTime, boolean previous);

	/**
	 * 投稿質問回答数を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @return 投稿質問回答数
	 */
	public long countPostQuestionAnswerCount(
			String communityUserId, String sku);

	/**
	 * 投稿質問回答数を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param status ステータス
	 * @param アダルト確認フラグ
	 * @return 投稿質問回答数
	 */
	public long countPostQuestionAnswerCount(
			String communityUserId,
			ContentsStatus status,
			Verification adultVerification);

	/**
	 * 投稿質問回答数を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param status ステータス
	 * @param アダルト確認フラグ
	 * @return 投稿質問回答数
	 */
	public long countPostQuestionAnswerCountForMypage(
			String communityUserId,
			Verification adultVerification);

	/**
	 * 指定した日付に指定したコミュニティユーザーの投稿質問に
	 * 回答がついたものを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param targetDate 対象日
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return 検索結果
	 */
	public SearchResult<QuestionAnswerDO> findQuestionAnswerByCommunityUserQuestion(
			String communityUserId,
			Date targetDate, int limit, int offset);

	public SearchResult<QuestionAnswerDO> findQuestionAnswerByCommunityUserQuestionForMR(
			String communityUserId,
			Date targetDate, int limit, int offset);

	/**
	 * 指定したコミュニティユーザーが回答した質問の別の回答を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param publicDate 公開された日付
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return 質問回答リスト
	 */
	public SearchResult<QuestionAnswerDO> findAnotherQuestionAnswerByCommunityUserAnswer(
			String communityUserId, Date publicDate, int limit, int offset);
	
	public SearchResult<QuestionAnswerDO> findAnotherQuestionAnswerByCommunityUserAnswerForMR(
			String communityUserId, Date publicDate, int limit, int offset);

	/**
	 * 指定したコミュニティユーザーが指定した日付に投稿した質問回答を返します。
	 * @param communityUserIds コミュニティユーザーIDリスト
	 * @param publicDate 公開された日付
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return 質問回答リスト
	 */
	public SearchResult<QuestionAnswerDO> findQuestionAnswerByCommunityUserIds(
			List<String> communityUserIds, Date publicDate, int limit, int offset);
	public SearchResult<QuestionAnswerDO> findQuestionAnswerByCommunityUserIdsForMR(
			List<String> communityUserIds, Date publicDate, int limit, int offset);

	/**
	 * 指定した質問、日付に回答した質問回答を返します。
	 * @param questionIds 質問IDリスト
	 * @param publicDate 公開された日付
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return 質問回答リスト
	 */
	public SearchResult<QuestionAnswerDO> findQuestionAnswerByQuestionIds(
			List<String> questionIds, Date publicDate, String excludeCommunityId, int limit, int offset);
	public SearchResult<QuestionAnswerDO> findQuestionAnswerByQuestionIdsForMR(
			List<String> questionIds, Date publicDate, String excludeCommunityId, int limit, int offset);

	/**
	 * 指定した条件の質問情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param questionId 質問ID
	 * @return 質問情報リスト
	 */
	public List<QuestionAnswerDO> findQuestionAnswerByCommunityUserIdAndQuestionId(
			String communityUserId, String questionId);

	/**
	 * 指定した質問回答情報を返します。
	 * @param questionAnswerId 質問回答ID
	 * @return 質問回答情報
	 */
	public QuestionAnswerDO loadQuestionAnswer(String questionAnswerId);

	/**
	 * 指定した質問回答情報をインデックス情報から返します。
	 * @param questionAnswerId 質問回答ID
	 * @return 質問回答情報
	 */
	public QuestionAnswerDO loadQuestionAnswerFromIndex(String questionAnswerId);
	public QuestionAnswerDO loadQuestionAnswerFromIndex(String questionAnswerId, boolean includeDeleteContents);
	
	/**
	 * 指定した日付以前に保存した保存質問を返します。
	 * @param intervalDate 公開された日付
	 * @return 質問リスト
	 */
	public SearchResult<QuestionAnswerDO> findTemporaryQuestionAnswerByBeforeInterval(Date intervalDate);

	
	public void removeQuestionAnswers(List<String> questionAnswerIds);

	public void removeTemporaryQuestionAnswer(String communityUserId);
	
	public String findProductSku(String questionAnswerId);
	
}
