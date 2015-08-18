/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.lib.solr.FacetResult;
import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.DecisivePurchaseDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseLostProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.UsedProductDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.AlsoBuyProduct;
import com.kickmogu.yodobashi.community.resource.domain.constants.CancelPointGrantType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ProductSatisfaction;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewSortType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewType;
import com.kickmogu.yodobashi.community.resource.domain.constants.Verification;


/**
 * レビュー DAO です。
 * @author kamiike
 *
 */
public interface ReviewDao {

	/**
	 * 指定した条件のレビュー情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @return レビュー情報リスト
	 */
	public List<ReviewDO> findReviewByCommunityUserIdAndSKU(
			String communityUserId, String sku);

	/**
	 * 指定した条件のレビュー情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @param condition 条件
	 * @return レビュー情報リスト
	 */
	public List<ReviewDO> findReviewByCommunityUserIdAndSKU(
			String communityUserId, String sku, Condition condition);
	
	/**
	 * 指定した条件のレビュー情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @param condition 条件
	 * @return レビュー情報リスト
	 */
	public List<ReviewDO> findReviewByCommunityUserIdAndSKU(
			String communityUserId, String sku, ReviewType reviewType, ContentsStatus status);
	/**
	 * 指定した条件のレビュー情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @param condition 条件
	 * @return レビュー情報リスト
	 */
	public List<ReviewDO> findReviewByCommunityUserIdAndSKU(
			String communityUserId, String sku, ReviewType reviewType, ContentsStatus status, Condition condition);

	/**
	 * 指定したレビュー情報を返します。
	 * @param reviewId レビューID
	 * @return レビュー情報
	 */
	public ReviewDO loadReview(String reviewId);

	/**
	 * 指定したレビュー情報をインデックス情報から返します。
	 * @param reviewId レビューID
	 * @return レビュー情報
	 */
	public ReviewDO loadReviewFromIndex(String reviewId);
	public ReviewDO loadReviewFromIndex(String reviewId, boolean fill);
	public ReviewDO loadReviewFromIndex(String reviewId, boolean fill, boolean includeDeleteContents);
	/**
	 * 指定したレビュー情報を返します。
	 * @param reviewId レビューID
	 * @param condition 条件
	 * @param withLock ロックを取得するかどうか
	 * @return レビュー情報
	 */
	public ReviewDO loadReview(String reviewId, Condition condition, boolean withLock);

	/**
	 * レビュー情報を保存します。
	 * @param review レビュー
	 * @return レビューID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.MEDIUM, frequencyComment="レビューの登録の頻度は中くらい")
	public String saveReview(ReviewDO review);

	/**
	 * 指定した条件のレビューにポイント付与可能か厳格にチェックします。
	 * @param communityUserId コミュニティユーザーID
	 * @param product 商品情報
	 * @param reviewTerm レビュー期間
	 * @return 有効な場合、true
	 */
	public boolean isStrictPointGrantReview(String communityUserId, ProductDO product, int reviewTerm);

	/**
	 * 指定した条件のレビューにポイント付与可能か緩くチェックします。
	 * @param communityUserId コミュニティユーザーID
	 * @param product 商品情報
	 * @param reviewTerm レビュー期間
	 * @return 有効な場合、true
	 */
	public boolean isLeniencePointGrantReview(String communityUserId, ProductDO product, int reviewTerm);
	public Map<String, Boolean> isLeniencePointGrantReviews(List<Map<String, Object>> leniencePointGrantReviewInputList);

	/**
	 * レビュー情報のインデックスを更新します。
	 * @param reviewId レビューID
	 * @return レビュー情報
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.MEDIUM, frequencyComment="レビューの登録の頻度は中くらい")
	public ReviewDO updateReviewInIndex(String reviewId);	
	public ReviewDO updateReviewInIndex(String reviewId, boolean mngToolOperation);
	
	/**
	 * レビュー情報のインデックスを更新します。カタログ通信なし
	 * @param reviewId レビューID
	 * @return レビュー情報
	 */
	public ReviewDO updateReviewInIndexForMR(String reviewId);
	
	/**
	 * レビュー履歴情報のインデックスを更新します。
	 * @param reviewHistoryId レビュー履歴ID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.MEDIUM, frequencyComment="レビューの登録の頻度は中くらい")
	public void updateReviewHistoryInIndex(String reviewHistoryId);

	/**
	 * 指定したレビューを削除します。
	 * @param reviewId レビューID
	 * @param logical 論理削除かどうか
	 * @param cancelPointGrantType ポイント申請キャンセル理由
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="レビューの登録の頻度は稀")
	public void deleteReview(
			String reviewId,
			boolean effective,
			boolean logical,
			CancelPointGrantType cancelPointGrantType,
			boolean mngToolOperation);

	/**
	 * 指定した商品にレビューを書いている人を返します。
	 * @param sku SKU
	 * @param excludeReviewId 対象から外すレビューID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return レビューを書いているコミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findReviwerExcludeReviewIdBySKU(
			String sku, String excludeReviewId, ContentsStatus[] statuses, int limit, int offset);

	/**
	 * 指定した商品にレビューを書いている人を重複を除いて返します。
	 * @param sku SKU
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return レビューを書いているコミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findDistinctReviwerExcludeCommunityUserIdBySKU(
			String sku, String excludeCommunityUserId, int limit, int offset);

	public SearchResult<CommunityUserDO> findDistinctReviwerExcludeCommunityUserIdBySKU(
			String sku, String excludeCommunityUserId, int limit, int offset, boolean excludeProduct);

	/**
	 * フォローした商品にレビューを書いている人を重複を除いて返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return レビューを書いているコミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findDistinctReviwerByFollowProduct(
			String communityUserId, int limit, int offset);

	/**
	 * 購入した商品にレビューを書いている人を重複を除いて返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param publicSetting 非公開情報を表示する場合はtrue
	 * @return レビューを書いているコミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findDistinctReviwerByPurchaseProduct(
			String communityUserId, int limit, int offset, boolean publicSetting);

	/**
	 * 指定した商品にレビューを書いている人を重複を除いて返します。
	 * @param skus SKUリスト
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return レビューを書いているコミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findDistinctReviwerBySKU(
			List<String> skus, int limit, int offset);

	/**
	 * レビューのスコア情報と閲覧数をインデックスも合わせて更新します。
	 * @param reviewId レビューID
	 * @param score スコア
	 * @param viewCount UU閲覧数
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="バッチ処理なのでテスト対象外")
	public void updateReviewScoreAndViewCountWithIndexForBatch(ReviewDO review);
	public void updateReviewScoreAndViewCountWithIndexForBatchBegin(int bulkSize);
	public void updateReviewScoreAndViewCountWithIndexForBatchEnd();

	/**
	 * 投稿済みレビュー投稿者リストを返します。
	 * @param sku SKU
	 * @return 投稿済みレビュー投稿者リスト
	 */
	public Set<String> loadPostReviewerListBySku(String sku);

	/**
	 * 指定した商品、ユーザーが保持する有効なレビューリストを返します。
	 * @param sku SKU
	 * @param communityUserId コミュニティユーザーID
	 * @return 有効なレビューリスト
	 */
	public List<ReviewDO> findEffectiveReviewList(
			String sku, String communityUserId);

	/**
	 * 有効なレビューをキャンセル更新します。
	 * @param reviews レビューリスト
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="更新通知で商品購入キャンセル時のみ呼ばれるので頻度は稀")
	public void updateCancelEffectiveWithIndex(
			List<ReviewDO> reviews);

	/**
	 * 指定された商品情報のレビュー集計情報をレビュータイプごとに返します。
	 * @param product 商品
	 * @return レビュー集計情報
	 */
	public Map<String, Long> loadReviewSummaryByReviewType(List<String> skus);

	/**
	 * 指定された商品情報のレビュー集計情報を返します。
	 * @param product 商品
	 * @return レビュー集計情報
	 */
	public Map<Integer, Long> loadReviewSummary(ProductDO product);

	/**
	 * 商品に紐づく購入の決め手を評価の高い順に返します。<br />
	 * 購入の決め手IDの指定がある場合は、それらを含めて評価順にマージして
	 * 返します。
	 * @param product 商品
	 * @param decisivePurchaseIds 購入の決め手IDのリスト
	 * @param reviewTerm レビュー区間
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 検索結果
	 */
	public SearchResult<DecisivePurchaseDO> findDecisivePurchaseFromIndexBySKU(
			ProductDO product, List<String> decisivePurchaseIds, Integer reviewTerm,
			int limit, int offset);
	
	/**
	 * 商品に紐づく購入の決め手を取得します。
	 * @param product　商品
	 * @param excludeDecisivePurchaseIdList　除外する「購入の決め手」のIDリスト
	 * @param limit　最大取得件数
	 * @param offset 検索開始位置
	 * @return
	 */
	public SearchResult<DecisivePurchaseDO> findDecisivePurchaseFromIndexBySKU(
			ProductDO product, List<String> excludeDecisivePurchaseIdList, int limit, int offset);
	/**
	 * 商品に紐づく購入の決め手を取得します。
	 * @param decisivePurchaseIds
	 * @return
	 */
	public SearchResult<DecisivePurchaseDO> findDecisivePurchaseFromIndexByIds(List<String> decisivePurchaseIds);

	/**
	 * 指定した購入の決め手を選択したコミュニティユーザーを返します。
	 * @param decisivePurchaseId 購入の決め手ID
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param limit 最大取得件数
	 * @return 指定した購入の決め手を選択したコミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findCommunityUserExcludeCommunityUserIdByDecisivePurchaseId(
			String decisivePurchaseId,
			String excludeCommunityUserId,
			int limit);

	/**
	 * 指定したレビュー情報において、購入に迷った商品に選ばれた商品を返します。
	 * @param reviewId レビューID
	 * @return 購入に迷った商品のリスト
	 */
	public SearchResult<PurchaseLostProductDO> findPurchaseLostProductByReviewId(
			String reviewId);

	/**
	 * 指定したレビュー情報において、過去に使用した商品に選ばれた商品を返します。
	 * @param reviewId レビューID
	 * @return 過去に使用した商品のリスト
	 */
	public SearchResult<UsedProductDO> findUsedProductByReviewId(String reviewId);

	/**
	 * 指定した商品のレビュー情報において、購入に迷った商品に選ばれた商品カウントを返します。
	 * @param sku SKU SKU
	 * @param limit 最大取得件数
	 * @return 購入に迷った商品カウントのリスト
	 */
	public SearchResult<FacetResult<String>> findPurchaseLostProductCountBySku(
			String sku, int limit);

	/**
	 * 指定した商品のレビュー情報において、過去に使用した商品に選ばれた商品カウントを返します。
	 * @param sku SKU SKU
	 * @param limit 最大取得件数
	 * @return 過去に使用した商品カウントのリスト
	 */
	public SearchResult<FacetResult<String>> findUsedProductCountBySku(
			String sku, int limit);

	/**
	 * 指定した期間に更新のあったレビューを返します。
	 * @param fromDate 検索開始時間
	 * @param toDate 検索終了時間
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return
	 */
	public SearchResult<ReviewDO> findUpdatedReviewByOffsetTime(
			Date fromDate, Date toDate, int limit, int offset);
	
	/**
	 * 指定したユーザーの全ての有効レビュー、一時停止レビューを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return
	 */
	public SearchResult<ReviewDO> findReviewByCommunityUserId(
			String communityUserId, int limit, int offset);
	
	
			
	/**
	 * 指定した商品、レビュー区間に対するレビューを投稿日時順（降順）に返します。
	 * @param product 商品
	 * @param reviewType レビュータイプ
	 * @param reviewTerm レビュー区間
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	public SearchResult<ReviewDO> findNewReviewBySku(
			ProductDO product,
			ReviewType reviewType,
			Integer reviewTerm,
			String excludeReviewId,
			int limit, Date offsetTime, boolean previous);
	
	/**
	 * 指定した商品、レビュー区間に対するレビューを投稿日時順（降順）に返します。
	 * @param skus 商品SKU一覧
	 * @param reviewType レビュータイプ
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	public SearchResult<ReviewDO> findNewReviewBySkus(
			List<String> skus,
			ReviewType reviewType,
			String excludeReviewId,
			int limit, Date offsetTime, boolean previous);
	
	/**
	 * 指定した商品、評価に対するレビューを投稿日時順（降順）に返します。
	 * @param product 商品
	 * @param productSatisfaction 指定の評価
	 * @param sortType ソート順（01:最新順,02:適合度順)
	 * @param excludeReviewId 除外するレビューID
	 * @param reviewTerm レビュー区間
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	public SearchResult<ReviewDO> findReviewBySkuAndRatingStar(
			ProductDO product,
			ProductSatisfaction productSatisfaction,
			ReviewSortType sortType,
			Integer reviewTerm,
			String excludeReviewId,
			int limit,
			Date offsetTime,
			Double offsetScore,
			boolean previous);
	
	/**
	 * 指定した商品、評価に対するレビューを投稿日時順（降順）に返します。
	 * @param skus 商品SKU一覧
	 * @param productSatisfaction 指定の評価
	 * @param sortType ソート順（01:最新順,02:適合度順)
	 * @param excludeReviewId 除外するレビューID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	public SearchResult<ReviewDO> findNewReviewBySkusAndRatingStar(
			List<String> skus,
			ProductSatisfaction productSatisfaction,
			ReviewSortType sortType,
			String excludeReviewId,
			int limit,
			Date offsetTime,
			Double offsetScore,
			boolean previous);
	
	/**
	 * 指定した商品とコミュニティユーザーIDに対する、レビュー区間に対するレビューを投稿日時順（降順）に返します。
	 * @param communityUserId コミュニティーユーザーID
	 * @param product 商品
	 * @param reviewTerm レビュー区間
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	public SearchResult<ReviewDO> findNewReviewBySkuAndCommunityUserId(
			String communityUserId,
			String sku,
			int limit,
			Date offsetTime,
			boolean previous);
	
	/**
	 * 指定した商品、レビュー区間に対するレビューを投稿日時順（降順）に返します。
	 * @param sku 商品SKU
	 * @return レビュー件数
	 */
	public long countReviewBySku(String sku);
	
	/**
	 * 指定した商品、レビュー区間に対するレビューを投稿日時順（降順）に返します。
	 * @param skus 商品SKU一覧
	 * @return レビュー件数
	 */
	public long countReviewBySkus(List<String> skus);
	
	/**
	 * WSせんよう
	 * @param sku
	 * @param limit
	 * @return
	 */
	public SearchResult<ReviewDO> findNewReviewBySku(String sku, int limit);
	
	/**
	 * 指定のカテゴリコードの最新のレビューを取得する（カテゴリ指定なしは、すべての商品が対象）
	 * @param categoryCode カテゴリコード
	 * @param offsetTime 検索キー（対象は、投稿日時）
	 * @param limit 取得上限
	 * @param previous (true：古い順,false:新しい順）
	 * @return レビュー一覧を取得
	 */
	public SearchResult<ReviewDO> findReviewByCategoryCode(
			String categoryCode, 
			Date offsetTime, 
			int limit, 
			boolean previous);
	/**
	 * 指定した商品、レビュー区間に対するレビューを適合順（降順）に返します。
	 * @param product 商品
	 * @param reviewType レビュータイプ
	 * @param reviewTerm レビュー区間
	 * @param limit 最大取得件数
	 * @param offsetMatchScore 検索開始適合度
	 * @param offsetTime 検索開始時間（２次ソート条件）
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	public SearchResult<ReviewDO> findMatchReviewBySku(
			ProductDO product,
			ReviewType reviewType,
			Integer reviewTerm,
			String excludeReviewId,
			int limit,
			Double offsetMatchScore,
			Date offsetTime,
			boolean previous);
	
	/**
	 * 指定した商品、レビュー区間に対するレビューを適合順（降順）に返します。
	 * @param skus 商品SKU一覧
	 * @param reviewType レビュータイプ
	 * @param limit 最大取得件数
	 * @param offsetMatchScore 検索開始適合度
	 * @param offsetTime 検索開始時間（２次ソート条件）
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	public SearchResult<ReviewDO> findMatchReviewBySkus(
			List<String> skus,
			ReviewType reviewType,
			String excludeReviewId,
			int limit,
			Double offsetMatchScore,
			Date offsetTime,
			boolean previous);
	
	/**
	 * WSせんよう
	 * @param sku
	 * @param limit
	 * @return
	 */
	public SearchResult<ReviewDO> findMatchReviewBySku(String sku, int limit);

	/**
	 * 指定したレビューを除外した、商品・レビュワーに紐づくレビュー情報を返します。
	 * @param sku SKU
	 * @param communityUserId コミュニティユーザーID
	 * @param excludeReviewId 除外するレビューID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 検索結果
	 */
	public SearchResult<ReviewDO> findReviewExcludeReviewIdByCommuntyUserIdAndSKU(
			String sku,
			String communityUserId,
			String excludeReviewId,
			int limit,
			int offset);

	/**
	 * 指定した商品を除外した、レビュワーに紐づくレビュー情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param excludeSKU 除外するSKU
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 検索結果
	 */
	public SearchResult<ReviewDO> findReviewExcludeSkuByCommunityUserId(
			String communityUserId,
			String excludeSKU,
			int limit,
			int offset);

	
	/**
	 * 指定した商品の満足度に関する集計情報を返します(すべて)。
	 * @param product 商品
	 * @return 商品の満足度に関する集計情報
	 */
	public Map<ProductSatisfaction, Long> loadProductSatisfactionSummaryMapWithAll(String sku);

	/**
	 * 指定した商品の満足度に関する集計情報を返します（すべて）。
	 * @param skus 商品リスト
	 * @return 商品の満足度に関する集計情報リスト
	 */
	public Map<String, Map<ProductSatisfaction, Long>> loadProductSatisfactionSummaryMapsWithAll(String[] skus);

	/**
	 * 指定した商品の満足度に関する選択者リストを返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @return 次も買いたいに関する集計情報
	 */
	public Map<ProductSatisfaction, SearchResult<CommunityUserDO>> loadProductSatisfactionUserMap(
			String sku, int limit);

	/**
	 * 指定した商品の次も買いたいに関する集計情報を返します。
	 * @param product 商品
	 * @return 次も買いたいに関する集計情報
	 */
	public Map<AlsoBuyProduct, Long> loadAlsoBuyProductSummaryMap(List<String> skus);

	/**
	 * 購入の決め手の評価数の合計値をカウントします。
	 * @param product 商品
	 * @return 購入の決め手の評価数の合計値
	 */
	public Long countTotalDecisivePurchaseRatings(
			String sku);

	/**
	 * 指定したステータスのコミュニティユーザーに紐づくレビュー情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param status ステータス
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @param adultVerification アダルト表示確認ステータス
	 * @return 検索結果
	 */
	public SearchResult<ReviewDO> findReviewByCommunityUserId(
			String communityUserId,
			String excludeReviewId,
			int limit,
			Date offsetTime,
			boolean previous,
			Verification adultVerification);
	
	public SearchResult<ReviewDO> findTemporaryReviewByCommunityUserId(
			String communityUserId,
			String excludeReviewId,
			int limit,
			Date offsetTime,
			boolean previous,
			Verification adultVerification);

	public long countReviewByCommunityUserId(String communityUserId, ContentsStatus status);

	public long countReviewByCommunityUserIdForMypage(String communityUserId);

	public long countReviewByCommunityUserId(
			String communityUserId,
			String excludeReviewId,
			ContentsStatus[] statuses,
			Verification adultVerification);
	/**
	 * レビュー数情報を返します。
	 * @param skus SKUリスト
	 * @return レビュー数情報
	 */
	public Map<String, Long> loadReviewCountMapBySKU(List<String> skus);

	/**
	 * レビュー数情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param skus SKUリスト
	 * @return レビュー数情報
	 */
	public Map<String, Long> loadReviewCountMapByCommunityUserIdAndSKU(
			String communityUserId,
			List<String> skus);

	/**
	 * レビュー数情報を返します。
	 * @param communityUserIds コミュニティユーザーIDのリスト
	 * @return レビュー数情報
	 */
	public Map<String, Long> loadReviewCountMapByCommunityUserId(
			List<String> communityUserIds);

	/**
	 * 投稿レビュー数を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @return 投稿レビュー数
	 */
	public long countPostReviewCount(String communityUserId, String sku);
	
	/**
	 * 投稿レビュー数リストを返します。
	 * @param skus SKUリスト
	 * @return 投稿レビュー数リスト
	 */
	public Map<String, Long> countPostReviewBySku(String[] skus);

	/**
	 * 指定したコミュニティユーザーがレビューをした商品の別のレビューを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param publicDate 公開された日付
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return レビューリスト
	 */
	public SearchResult<ReviewDO> findAnotherReviewByCommunityUserRreview(
			String communityUserId, Date publicDate, int limit, int offset);

	public SearchResult<ReviewDO> findAnotherReviewByCommunityUserRreviewForMR(
			String communityUserId, Date publicDate, int limit, int offset);
	
	/**
	 * 指定したコミュニティユーザーが指定した日付に投稿したレビューを返します。
	 * @param communityUserIds コミュニティユーザーIDリスト
	 * @param publicDate 公開された日付
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return レビューリスト
	 */
	public SearchResult<ReviewDO> findReviewByCommunityUserIds(
			List<String> communityUserIds, Date publicDate, int limit, int offset);
	public SearchResult<ReviewDO> findReviewByCommunityUserIdsForMR(
			List<String> communityUserIds, Date publicDate, int limit, int offset);

	/**
	 * 指定した商品、日付に投稿したレビューを返します。
	 * @param skus SKUリスト
	 * @param publicDate 公開された日付
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return レビューリスト
	 */
	public SearchResult<ReviewDO> findReviewBySKUs(
			List<String> skus, Date publicDate, String excludeCommunityId, int limit, int offset);
	public SearchResult<ReviewDO> findReviewBySKUsForMR(
			List<String> skus, Date publicDate, String excludeCommunityId, int limit, int offset);

	/**
	 * レビュー数情報を返します。
	 * @param skus SKUリスト
	 * @return レビュー数情報
	 */
	public Map<String, Long> loadReviewCountMap(List<String> skus);
	
	/**
	 * 同一商品で同一ユーザーがレビューをしている数（指定のレビュー以外の数）
	 * @param reviews
	 * @return
	 */
	public Map<String, Long> loadSameProductReviewCountMap(List<ReviewDO> reviews);

	/**
	 * ポイント付与リクエストIDに紐付くレビュー情報を返します。
	 * @param pointGrantRequestId ポイント付与リクエストID
	 * @return レビュー情報
	 */
	public ReviewDO loadReviewByPointGrantRequestId(String pointGrantRequestId);

	/**
	 * レビュー情報をポイント付与フィードバックのために更新します。
	 */
	public void updateReviewForPointGrantFeedback(ReviewDO review);
	
	/**
	 * 最新レビューフラグの設定を更新します。
	 * @param review
	 */
	public void updateReviewForLatestReview(ReviewDO review);

	/**
	 * 指定した日付以前に保存した保存レビューを返します。
	 * @param intervalDate 公開された日付
	 * @return レビューリスト
	 */
	public SearchResult<ReviewDO> findTemporaryReviewByBeforeInterval(Date intervalDate);


	public void removeReviews(List<String> reviewIds);

	public void removeTemporaryReview(String communityUserId);
	
	public String findProductSku(String reviewId);
	
	public Map<String, Long> decisivePurchaseCountMap(List<String> decisivePurchaseIds, String excludeCommunityUserId);

}
