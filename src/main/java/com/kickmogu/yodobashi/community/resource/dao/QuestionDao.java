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
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.Verification;


/**
 * 質問 DAO です。
 * @author kamiike
 *
 */
public interface QuestionDao {

	/**
	 * 指定した条件の質問情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @return 質問情報リスト
	 */
	public List<QuestionDO> findQuestionByCommunityUserIdAndSKU(
			String communityUserId, String sku);

	/**
	 * 指定した条件の質問情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @param condition 条件
	 * @return 質問情報リスト
	 */
	public List<QuestionDO> findQuestionByCommunityUserIdAndSKU(
			String communityUserId, String sku, Condition condition);

	/**
	 * 指定した質問情報を返します。
	 * @param questionId 質問ID
	 * @param condition 条件
	 * @param withLock ロックを取得するかどうか
	 * @return 質問情報
	 */
	public QuestionDO loadQuestion(String questionId, Condition condition, boolean withLock);

	/**
	 * 質問情報を保存します。
	 * @param question 質問
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.MEDIUM, frequencyComment="質問の登録の頻度は中くらい")
	public void saveQuestion(QuestionDO question);

	/**
	 * 質問情報の最終回答日時を更新します。
	 * @param questionId 質問ID
	 * @param newLastAnswerDate 新しい最終回答日時
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.MEDIUM, frequencyComment="質問回答の登録の頻度は中くらい")
	public void updateQuestionLastAnswerDate(
			String questionId,
			Date newLastAnswerDate);

	/**
	 * 指定した質問を削除します。
	 * @param questionId 質問ID
	 * @param logical 論理削除かどうか
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="質問の削除の頻度は稀")
	public void deleteQuestion(
			String questionId,
			boolean logical,
			boolean mngToolOperation);

	/**
	 * 質問情報のインデックスを更新します。
	 * @param questionId 質問ID
	 * @return 質問情報
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.MEDIUM, frequencyComment="質問の登録の頻度は中くらい")
	public QuestionDO updateQuestionInIndex(String questionId);
	public QuestionDO updateQuestionInIndex(String questionId, boolean mngToolOperation);
	public QuestionDO updateQuestionInIndex(QuestionDO question);
	public QuestionDO updateQuestionInIndex(QuestionDO question, boolean mngToolOperation);

	/**
	 * 質問のスコア情報と閲覧数をインデックスも合わせて更新します。
	 * @param question 質問
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="バッチ処理なのでテスト対象外")
	public void updateQuestionScoreAndViewCountWithIndex(
			QuestionDO question);
	
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="バッチ処理なのでテスト対象外")
	public void updateQuestionScoreAndViewCountWithIndexForBatch(QuestionDO question);
	public void updateQuestionScoreAndViewCountWithIndexForBatchBegin(int bulkSize);
	public void updateQuestionScoreAndViewCountWithIndexForBatchEnd();
	
	/**
	 * 指定した商品に対するQA情報件数リストを返します。
	 * @param sku
	 * @return QA情報件数
	 */
	public long countQuestionBySku(String sku);
	
	/**
	 * 指定した商品に対するQA情報件数リストを返します。
	 * @param sku
	 * @return QA情報件数
	 */
	public long countQuestionBySkus(List<String> skus);
	/**
	 * 指定した商品に対するQA情報件数リストを返します。
	 * @param skus SKUリスト
	 * @return QA情報件数リスト
	 */
	public Map<String, Long> countQuestionBySku(String[] skus);

	/**
	 * 指定した商品に対するQA情報を更新日時（質問・回答の最終更新日時）順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	public SearchResult<QuestionDO> findUpdateQuestionBySku(
			String sku,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous);
	
	public SearchResult<QuestionDO> findUpdateQuestionBySkus(
			List<String> skus,
			String excludeOwnerCommunityUserId, 
			String excludeQuestionId,
			int limit, 
			Date offsetTime, 
			boolean previous);
	
	/**
	 * 指定した商品に対するQA情報を質問投稿日時順（降順）に返します。
	 * @param sku SKU
	 * @param excludeQuestionId 除外する質問ID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	public SearchResult<QuestionDO> findNewQuestionBySku(
			String sku,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous);
	
	/**
	 * 指定した商品に対するQA情報を質問投稿日時順（降順）に返します。
	 * @param sku SKU
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param excludeQuestionId 除外する質問ID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	public SearchResult<QuestionDO> findNewQuestionBySku(
			String sku,
			String excludeCommunityUserId,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous);

	/**
	 * 指定した商品に対するQA情報を回答なし・質問投稿日時順（降順）に返します。
	 * @param sku SKU
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param excludeQuestionId 除外する質問ID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	public SearchResult<QuestionDO> findNewQuestionWithNotAnswerPriorityBySku(
			String sku,
			String excludeCommunityUserId,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous);

	/**
	 * 指定した商品に対するQA情報を盛り上がり順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetQuestionScore 検索開始スコア
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	public SearchResult<QuestionDO> findPopularQuestionBySku(
			String sku,
			String excludeQuestionId,
			int limit,
			Double offsetQuestionScore,
			Date offsetTime,
			boolean previous);
	
	/**
	 * 指定した商品に対するQA情報を更新日時（質問・回答の最終更新日時）順（降順）に返します。
	 * @param skus SKU一覧
	 * @param excludeQuestionId 除外する質問ID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	public SearchResult<QuestionDO> findUpdateQuestionBySkus(
			List<String> skus,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous);
	
	/**
	 * 指定した商品に対するQA情報を質問投稿日時順（降順）に返します。
	 * @param skus SKU一覧
	 * @param excludeQuestionId 除外する質問ID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	public SearchResult<QuestionDO> findNewQuestionBySkus(
			List<String> skus,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous);
	
	/**
	 * 指定した商品に対するQA情報を質問投稿日時順（降順）に返します。
	 * @param skus SKU一覧
	 * @param excludeOwnerCommunityUserId 除外するコミュニティユーザーID
	 * @param excludeQuestionId 除外する質問ID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	public SearchResult<QuestionDO> findNewQuestionBySkus(
			List<String> skus,
			String excludeOwnerCommunityUserId,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous);

	/**
	 * 指定した商品に対するQA情報を盛り上がり順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetQuestionScore 検索開始スコア
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	public SearchResult<QuestionDO> findPopularQuestionBySkus(
			List<String> skus,
			String excludeQuestionId,
			int limit,
			Double offsetQuestionScore,
			Date offsetTime,
			boolean previous);

	/**
	 * 指定した質問情報を返します。
	 * @param questionId 質問ID
	 * @return 質問情報
	 */
	public QuestionDO loadQuestion(String questionId);

	/**
	 * 指定した質問情報をインデックス情報から返します。
	 * @param questionId 質問ID
	 * @return 質問情報
	 */
	public QuestionDO loadQuestionFromIndex(String questionId);
	public QuestionDO loadQuestionFromIndex(String questionId, boolean includeDeleteContents);

	/**
	 * 指定した質問以外で、指定した商品の盛り上がっている質問を盛り上がり順
	 * （降順）に返します。
	 * @param sku SKU
	 * @param excudeQuestionId 除外する質問ID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 盛り上がっている質問一覧
	 */
	public SearchResult<QuestionDO> findPopularQuestionExcudeQuestionId(
			String sku,
			String excudeQuestionId,
			int limit,
			int offset);

	/**
	 * 指定した質問以外で、指定した商品の質問を質問投稿日時順（降順）に返します。
	 * @param sku SKU
	 * @param excudeQuestionId 除外する質問ID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 新着質問一覧
	 */
	public SearchResult<QuestionDO> findNewQuestionExcudeQuestionId(
			String sku,
			String excudeQuestionId,
			int limit,
			int offset);

	/**
	 * 指定したコミュニティユーザーが投稿した質問を質問投稿日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param status ステータス
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @param アダルト確認フラグ
	 * @return 質問一覧
	 */
	public SearchResult<QuestionDO> findQuestionByCommunityUserId(
			String communityUserId, 
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous,
			Verification adultVerification);
	
	public SearchResult<QuestionDO> findTemporaryQuestionByCommunityUserId(
			String communityUserId,
			String excludeQuestionId,
			int limit, 
			Date offsetTime,
			boolean previous,
			Verification adultVerification);
	
	/**
	 * 指定した期間に更新のあった質問を返します。
	 * @param fromDate 検索開始時間
	 * @param toDate 検索終了時間
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return
	 */
	public SearchResult<QuestionDO> findUpdatedQuestionByOffsetTime(
			Date fromDate, Date toDate, int limit, int offset);
	
	/**
	 * 指定したユーザーの全ての有効、一時停止質問を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return
	 */
	public SearchResult<QuestionDO> findQuestionByCommunityUserId(
			String communityUserId, int limit, int offset);

	
	public long countQuestionByCommunityUserId(
			String communityUserId,
			ContentsStatus status);
	
	public long countQuestionByCommunityUserIdForMypage(
			String communityUserId);
	
	public long countQuestionByCommunityUserId(
			String communityUserId,
			String excludeQuestionId,
			ContentsStatus[] statuses,
			Verification adultVerification);
	/**
	 * 指定したユーザーの購入商品の新着Q&A情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 購入商品の新着Q&A情報
	 */
	public SearchResult<QuestionDO> findNewQuestionByPurchaseProduct(
			String communityUserId, int limit, Date offsetTime, boolean previous);

	/**
	 * 質問数情報を返します。
	 * @param skus SKUリスト
	 * @param waitAnswerOnly 回答待ち（回答無し）のみ
	 * @param excludeQuestionOwnerId 除外したい質問者のコミュニティユーザーID
	 * @return 質問数情報
	 */
	public Map<String, Long> loadQuestionCountMapBySKU(
			List<String> skus,
			boolean waitAnswerOnly,
			String excludeQuestionOwnerId);

	/**
	 * 質問数情報を返します。
	 * @param communityUserIds コミュニティユーザーIDのリスト
	 * @return 質問数情報
	 */
	public Map<String, Long> loadQuestionCountMapByCommunityUserId(
			List<String> communityUserIds);

	/**
	 * 投稿質問数を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @return 投稿質問数
	 */
	public long countPostQuestionCount(String communityUserId, String sku);

	/**
	 * 指定したコミュニティユーザーが指定した日付に投稿した質問を返します。
	 * @param communityUserIds コミュニティユーザーIDリスト
	 * @param publicDate 公開された日付
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return 質問リスト
	 */
	public SearchResult<QuestionDO> findQuestionByCommunityUserIds(
			List<String> communityUserIds, Date publicDate, int limit, int offset);
	public SearchResult<QuestionDO> findQuestionByCommunityUserIdsForMR(
			List<String> communityUserIds, Date publicDate, int limit, int offset);

	/**
	 * 指定した商品、日付に投稿した質問を返します。
	 * @param skus SKUリスト
	 * @param publicDate 公開された日付
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return 質問リスト
	 */
	public SearchResult<QuestionDO> findQuestionBySKUs(
			List<String> skus, Date publicDate, 
			String excludeCommunityUserId, int limit, int offset);

	public SearchResult<QuestionDO> findQuestionBySKUsForMR(
			List<String> skus, Date publicDate, 
			String excludeCommunityUserId, int limit, int offset);

	/**
	 * 指定した日付以前に保存した保存質問を返します。
	 * @param intervalDate 公開された日付
	 * @return 質問リスト
	 */
	public SearchResult<QuestionDO> findTemporaryQuestionByBeforeInterval(Date intervalDate);

	public void removeQuestions(List<String> questionIds);

	public void removeTemporaryQuestion(String communityUserId);
	
	public String findProductSku(String questionId);

}
