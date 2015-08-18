/**
 *
 */
package com.kickmogu.yodobashi.community.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.AbstractReviewDO.PointGrantRequestDetail;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseLostProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.ScoreFactorDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.UsedProductDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ProductSatisfaction;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewSortType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewType;
import com.kickmogu.yodobashi.community.service.vo.AlsoBuyProductSummaryVO;
import com.kickmogu.yodobashi.community.service.vo.DecisivePurchaseSetVO;
import com.kickmogu.yodobashi.community.service.vo.DecisivePurchaseSummaryVO;
import com.kickmogu.yodobashi.community.service.vo.ProductSatisfactionSetVO;
import com.kickmogu.yodobashi.community.service.vo.ProductSatisfactionSummaryVO;
import com.kickmogu.yodobashi.community.service.vo.PurchaseLostProductSetVO;
import com.kickmogu.yodobashi.community.service.vo.ReviewSetVO;
import com.kickmogu.yodobashi.community.service.vo.ReviewStatisticsVO;
import com.kickmogu.yodobashi.community.service.vo.ReviewSummaryVO;
import com.kickmogu.yodobashi.community.service.vo.UsedProductSetVO;

/**
 * レビューサービスです。
 * @author kamiike
 *
 */
public interface ReviewService extends CommonService{

	/**
	 * レビューサマリー情報（レビュータイプごとのレビュー件数）を返します。
	 * @param product 商品
	 * @return レビューサマリー
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品レビュー一覧で呼ばれるので頻度は高",
		refClassNames={
			"ProductReviewListController"
			}
	)
	public ReviewSummaryVO getReviewSummaryByReviewType(List<String> skus);
	
	/**
	 * レビュー統計情報（商品満足度・購入の決め手・次も買いますか）を返します。
	 * @param product 商品
	 * @return レビュー統計情報
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品レビュー一覧の右サイド表示Ajax(商品満足度,次も買いますか,購入の決め手)で呼ばれるので頻度は高",
		refClassNames={
			"AjaxJsonComponentController"
			}
	)
	public ReviewStatisticsVO getReviewStatistics(
			String sku,
			boolean isReviewDecisivePurchase, 
			boolean isAlsoBuyProductSummary,
			boolean isSatisfactionAvarage);
	
	/**
	 * 購入の決め手情報を返します。
	 * @param product 商品
	 * @param decisivePurchaseIds 必ず含める購入の決め手IDリスト
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 購入の決め手情報
	 */
	@PerformanceTest(type=Type.SELECT,
			frequency=Frequency.LOW,
			frequencyComment="レビュー一覧のメタタグ表示で呼ばれるので頻度は高",
			refClassNames={
				"ProductReviewListController",
				"ProductFeedsController"
				}
		)
	public DecisivePurchaseSummaryVO getReviewDecisivePurchaseByProduct(
			ProductDO product,
			List<String> decisivePurchaseIds,
			int limit, int offset);
	
	/**
	 * 指定の「購入の決め手」を取得します
	 * @param decisivePurchaseIds
	 * @return
	 */
	public SearchResult<DecisivePurchaseSetVO> findDecisivePurchaseFromIndexByIds(List<String> decisivePurchaseIds);

	
	/**
	 * 次も買いますか評価情報を返します。
	 * @param sku　商品SKU
	 * @return 次も買いますか情報一覧サマリー
	 */
	@PerformanceTest(type=Type.SELECT,
			frequency=Frequency.HIGH,
			frequencyComment="商品レビュー一覧の右サイド表示Ajax(商品満足度,次も買いますか,購入の決め手)で呼ばれるので頻度は高",
			refClassNames={
				"AjaxJsonComponentController"
				}
		)
	public AlsoBuyProductSummaryVO getAlsoBuyProductBySku(String sku);
	
	/**
	 * 次も買いますか評価情報を返します。
	 * @param skus 商品SKU一覧
	 * @return 次も買いますか情報一覧サマリー
	 */
	@PerformanceTest(type=Type.SELECT,
			frequency=Frequency.HIGH,
			frequencyComment="商品レビュー一覧の右サイド表示Ajax(商品満足度,次も買いますか,購入の決め手)で呼ばれるので頻度は高",
			refClassNames={
				"AjaxJsonComponentController"
				}
		)
	public AlsoBuyProductSummaryVO getAlsoBuyProductBySkus(List<String> skus);
	
	/**
	 * 指定したレビュー情報において、購入に迷った商品に選ばれた商品を返します。
	 * @param reviewId レビューID
	 * @return 購入に迷った商品のリスト
	 */
	//
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品レビュー一覧の右サイド表示Ajax(この商品の購入前に使っていた商品)で呼ばれるので頻度は高",
		refClassNames={
			"AjaxJsonComponentController"
			}
	)
	public SearchResult<PurchaseLostProductDO> findPurchaseLostProductByReviewId(String reviewId);

	/**
	 * 指定したレビュー情報において、過去に使用した商品に選ばれた商品を返します。
	 * @param reviewId レビューID
	 * @return 過去に使用した商品のリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品レビューの右サイド表示Ajax(過去に使用した商品)で呼ばれるので頻度は高",
		refClassNames={
			"AjaxJsonComponentController"
			}
	)
	public SearchResult<UsedProductDO> findUsedProductByReviewId(String reviewId);

	/**
	 * 指定した商品のレビュー情報において、購入に迷った商品に選ばれた商品を返します。
	 * @param sku SKU SKU
	 * @param limit 最大取得件数
	 * @return 購入に迷った商品のリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品レビューの右サイド表示Ajax(購入に迷った商品)で呼ばれるので頻度は高",
		refClassNames={
			"AjaxJsonComponentController"
			}
	)
	public SearchResult<PurchaseLostProductSetVO> findPurchaseLostProductBySku(String sku, int limit);

	/**
	 * 指定した商品のレビュー情報において、過去に使用した商品に選ばれた商品を返します。
	 * @param sku SKU SKU
	 * @param limit 最大取得件数
	 * @return 過去に使用した商品のリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品レビューの右サイド表示Ajax(過去に使用した商品)で呼ばれるので頻度は高",
		refClassNames={
			"AjaxJsonComponentController"
			}
	)
	/**
	 * WSせんよう
	 * @param sku
	 * @param limit
	 * @return
	 */
	public SearchResult<UsedProductSetVO> findUsedProductBySku(String sku, int limit);

	/**
	 * 指定した商品に対するレビュー件数を返します。
	 * @param product 商品
	 * @return レビュー件数
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.SUPER_HIGH,
		frequencyComment="商品系の各画面で共通的に呼ばれるので頻度は極高",
		refClassNames={"AbstractProductBaseController"}
	)
	public long countReviewBySku(String sku);
	
	/**
	 * 指定した商品に対するレビュー件数を返します。
	 * @param product 商品
	 * @return レビュー件数
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.SUPER_HIGH,
		frequencyComment="商品系の各画面で共通的に呼ばれるので頻度は極高",
		refClassNames={"AbstractProductBaseController"}
	)
	public long countReviewBySkus(List<String> skus);

	/**
	 * 指定した商品、経過月に対するレビューを投稿日時順（降順）に返します。
	 * @param product 商品
	 * @param reviewType レビュータイプ
	 * @param excludeReviewId 除外するレビューID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="レビュー一覧の初期表示ともっと見るAjaxで呼ばれるので頻度は高",
		refClassNames={"AjaxJsonProductReviewController","ProductReviewListController"}
	)
	public SearchResult<ReviewSetVO> findNewReviewBySkuAndReviewType(
			ProductDO product,
			ReviewType reviewType,
			String excludeReviewId,
			int limit, 
			Date offsetTime, 
			boolean previous);
	
	/**
	 * 指定した商品、経過月に対するレビューを投稿日時順（降順）に返します。
	 * @param sku 商品SKU
	 * @param skus バリエーション商品一覧 
	 * @param reviewType レビュータイプ
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="レビュー一覧の初期表示ともっと見るAjaxで呼ばれるので頻度は高",
		refClassNames={"AjaxJsonProductReviewController","ProductReviewListController"}
	)
	public SearchResult<ReviewSetVO> findNewReviewBySkusAndReviewType(
			String sku,
			List<String> skus,
			ReviewType reviewType,
			String excludeReviewId,
			int limit,
			Date offsetTime,
			boolean previous);
	
	/**
	 * 指定した商品、評価に対するレビューを投稿日時順（降順）に返します。
	 * @param sku 商品SKU
	 * @param skus バリエーション商品一覧 
	 * @param productSatisfaction 指定の評価
	 * @param excludeReviewId 除外するレビューID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="レビュー一覧の初期表示ともっと見るAjaxで呼ばれるので頻度は高",
		refClassNames={"AjaxJsonProductReviewController","ProductReviewListController"}
	)
	public SearchResult<ReviewSetVO> findReviewBySkusAndRatingStar(
			String sku,
			List<String> skus,
			String excludeReviewId,
			ProductSatisfaction productSatisfaction,
			ReviewSortType sortType,
			int limit,
			Date offsetTime,
			Double offsetScore,
			boolean previous);
	
	/**
	 * 指定した商品、評価に対するレビューを投稿日時順（降順）に返します。
	 * @param product 商品
	 * @param excludeReviewId 除外するレビューID
	 * @param productSatisfaction 指定の評価
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="レビュー一覧の初期表示ともっと見るAjaxで呼ばれるので頻度は高",
		refClassNames={"AjaxJsonProductReviewController","ProductReviewListController"}
	)
	public SearchResult<ReviewSetVO> findReviewBySkuAndRatingStar(
			ProductDO product,
			String excludeReviewId,
			ProductSatisfaction productSatisfaction,
			ReviewSortType sortType,
			int limit,
			Date offsetTime,
			Double offsetScore,
			boolean previous);
	
	/**
	 * 指定した商品とコミュニティーユーザーIDで、レビューを投稿日時順（降順）に返します。
	 * @param communityUserId コミュニティーユーザーID
	 * @param sku 商品SKU
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@PerformanceTest(type=Type.SELECT,
			frequency=Frequency.HIGH,
			frequencyComment="レビュー一覧の初期表示ともっと見るAjaxで呼ばれるので頻度は高",
			refClassNames={"AjaxJsonProductReviewController","ProductReviewListController"}
		)
	public SearchResult<ReviewSetVO> findNewReviewBySkuAndCommunityUserId(
			String communityUserId,
			String sku,
			int limit, 
			Date offsetTime, 
			boolean previous);
	
	/**
	 * WSせんよう
	 * @param sku
	 * @param limit
	 * @return
	 */
	public SearchResult<ReviewSetVO> findNewReviewBySku(String sku, int limit);
	
	/**
	 * 
	 * @param categoryCode
	 * @param offsetTime
	 * @param limit
	 * @param previous
	 * @return
	 */
	public SearchResult<ReviewSetVO> findReviewByCategoryCode(
			String categoryCode,
			Date offsetTime,
			int limit,
			boolean previous);
	
	/**
	 * 指定した商品、経過月に対するレビューを投稿日時順（降順）に返します。
	 * @param product 商品
	 * @param reviewTerm レビュー区間
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="レビュー一覧の初期表示ともっと見るAjaxで呼ばれるので頻度は高",
		refClassNames={"AjaxJsonProductReviewController","ProductReviewListController"}
	)
	public SearchResult<ReviewSetVO> findNewReviewBySku(
			ProductDO product,
			Integer reviewTerm,
			String excludeReviewId,
			int limit, Date offsetTime, boolean previous);

	/**
	 * 指定した商品、経過月に対するレビューを適合順（降順）に返します。
	 * @param product 商品情報
	 * @param reviewType レビュータイプ
	 * @param excludeReviewId 対象外のレビューID
	 * @param limit 最大取得件数
	 * @param offsetMatchScore 検索開始適合度
	 * @param offsetTime 検索開始時間（２次ソート条件）
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="レビュー一覧の初期表示ともっと見るAjaxで呼ばれるので頻度は高",
		refClassNames={"AjaxJsonProductReviewController","ProductReviewListController"}
	)
	public SearchResult<ReviewSetVO> findMatchReviewBySkuAndReviewType(
			ProductDO product,
			ReviewType reviewType,
			String excludeReviewId,
			int limit,
			Double offsetMatchScore,
			Date offsetTime, boolean previous);
	
	/**
	 * 指定した商品、経過月に対するレビューを適合順（降順）に返します。
	 * @param product 商品情報
	 * @param reviewType レビュータイプ
	 * @param excludeReviewId 対象外のレビューID
	 * @param limit 最大取得件数
	 * @param offsetMatchScore 検索開始適合度
	 * @param offsetTime 検索開始時間（２次ソート条件）
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="レビュー一覧の初期表示ともっと見るAjaxで呼ばれるので頻度は高",
		refClassNames={"AjaxJsonProductReviewController","ProductReviewListController"}
	)
	public SearchResult<ReviewSetVO> findMatchReviewBySkusAndReviewType(
			String sku,
			List<String> skus,
			ReviewType reviewType,
			String excludeReviewId,
			int limit,
			Double offsetMatchScore,
			Date offsetTime, boolean previous);

	public SearchResult<ReviewSetVO> findMatchReviewBySku(String sku, int limit);

	/**
	 * 指定した商品、経過月に対するレビューを適合順（降順）に返します。
	 * @param product 商品
	 * @param reviewTerm レビュー区間
	 * @param limit 最大取得件数
	 * @param offsetMatchScore 検索開始適合度
	 * @param offsetTime 検索開始時間（２次ソート条件）
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="レビュー一覧の初期表示ともっと見るAjaxで呼ばれるので頻度は高",
		refClassNames={"AjaxJsonProductReviewController","ProductReviewListController"}
	)
	public SearchResult<ReviewSetVO> findMatchReviewBySku(
			ProductDO product,
			Integer reviewTerm,
			String excludeReviewId,
			int limit,
			Double offsetMatchScore,
			Date offsetTime, boolean previous);

	/**
	 * 指定した商品を除いた、指定したレビュアーによるレビューを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param excludeSKU 除くSKU
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return レビュー一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品レビュー詳細の左サイドAjax表示(ユーザーAさんの他の商品レビュー)で呼ばれるので頻度は高",
		refClassNames={
			"AjaxJsonComponentController"
			}
	)
	public SearchResult<ReviewDO> findReviewExcludeSKUByCommunityUserId(
			String communityUserId, String excludeSKU, int limit, int offset);

	/**
	 * 指定したレビューを除いた、指定したレビュアーによるレビューを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @param excludeReviewId 除くレビューID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return レビュー一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品レビュー詳細の初期表示で呼ばれるので頻度は高",
		refClassNames={
			"ProductReviewDetailControllers"
			}
	)
	public SearchResult<ReviewDO> findReviewExcludeReviewIdByCommunityUserId(
			String communityUserId, String sku, String excludeReviewId,
			int limit, int offset);

	/**
	 * 指定した商品に対する購入の決め手を返します。
	 * @param product 商品
	 * @param decisivePurchaseIds 先頭に含める購入の決め手IDリスト
	 * @param withSelectedUsers 評価したユーザー情報を含めるかどうか
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param selectedUserLimit 選択済みユーザーの最大取得件数
	 * @return 購入の決め手一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品レビュー一覧の右側の購入の決め手の処理と購入時レビュー投稿で呼ばれるので頻度は高",
		refClassNames={
			"AjaxJsonProductReviewSubmitController",
			"AjaxHtmlDecisiveController"
			}
	)
	public SearchResult<DecisivePurchaseSetVO> findDecisivePurchaseBySKU(
			ProductDO product,
			List<String> decisivePurchaseIds,
			boolean withSelectedUsers,
			int limit,
			int offset,
			int selectedUserLimit);

	/**
	 * 指定した商品に対する購入の決め手を返します。
	 * @param product 商品
	 * @param excludeDecisivePurchaseIds 除外する「購入の決め手」のIDリスト
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return
	 */
	public SearchResult<DecisivePurchaseSetVO> findDecisivePurchaseBySKU(
			ProductDO product,
			List<String> excludeDecisivePurchaseIds,
			int limit,
			int offset
			);

	/**
	 * 指定した商品に対する商品満足度セットを返します。
	 * @param sku SKU
	 * @param selectedUserLimit 選択済みユーザーの最大取得件数
	 * @return 商品満足度セットリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.RARE,
		frequencyComment="商品レビュー投稿で呼ばれるので頻度は稀",
		refClassNames={
			"AjaxJsonProductReviewSubmitController"
			}
	)
	public List<ProductSatisfactionSetVO> findProductSatisfactionBySKU(
			String sku, int selectedUserLimit);

	/**
	 * 指定したコミュニティユーザーの投稿したレビューを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="マイページ,ユーザのアクティビティのレビュー一覧の初期表示、もっと見るAjaxで呼ばれるので頻度は中",
		refClassNames={
			"AjaxJsonMypageActivityController",
			"AjaxJsonUserActivityController",
			"MypageActivityReviewListController",
			"UserActivityReviewListController"
			}
	)
	public SearchResult<ReviewSetVO> findReviewByCommunityUserId(
			String communityUserId, int limit,
			Date offsetTime,
			boolean previous);

	/**
	 * 指定したコミュニティユーザーの投稿した一時保存レビューを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 一時保存レビュー一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.LOW,
		frequencyComment="マイページの保存レビューの初期表示、もっと見るAjaxで呼ばれるので頻度は稀",
		refClassNames={
			"AjaxJsonMypageSaveController",
			"MypageSaveReviewListController"
			}
	)
	public SearchResult<ReviewSetVO> findTemporaryReviewByCommunityUserId(
			String communityUserId, String excludeReviewId, int limit,
			Date offsetTime, boolean previous);

	/**
	 * 指定した条件でポイント付与可能かどうか返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param product 商品
	 * @param purchaseProduct 購入商品
	 * @return ポイント付与できる場合、true
	 */
	public boolean canGrantPointReview(
			String communityUserId,
			ProductDO product,
			PurchaseProductDO purchaseProduct);

	/**
	 * 指定した形式の一時保存レビューを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @param reviewType レビュータイプ
	 * @return 一時保存レビュー
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.RARE,
		frequencyComment="レビュー投稿時に呼ばれるので頻度は稀",
		refClassNames={
			"AjaxJsonProductReviewSubmitController"
			}
	)
	public ReviewDO getTemporaryReview(
			String communityUserId, String sku, ReviewType reviewType);


	/**
	 * 指定したレビューを返します。
	 * @param reviewId レビューID
	 * @return レビュー
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.RARE,
		frequencyComment="レビュー投稿時に呼ばれるので頻度は稀",
		refClassNames={
			"AjaxJsonProductReviewSubmitController"
			}
	)
	public ReviewDO getReview(String reviewId);
	
	/**
	 * 指定したレビューをインデックス情報から返します。
	 * @param reviewId レビューID
	 * @return レビュー情報
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.SUPER_HIGH,
		frequencyComment="レビュー詳細(初期表示,右側の購入の決め手,コメントリスト表示,画像コメント投稿,コメント-いいね件数表示部表示,コメント編集,いいね登録参照),違反登録,年齢認証などで使用するので頻度は高",
		refClassNames={
			"AjaxJsonComponentController",
			"AjaxJsonProductReviewDetailController",
			"SpamReportController",
			"AgeVerifyIntercepter"
			}
	)
	public ReviewSetVO getReviewFromIndex(
			String reviewId, boolean includeDeleteContents);

	/**
	 * 指定したレビューをインデックス情報から返します。退会削除レビューは取得しない。
	 * @param reviewId レビューID
	 * @return レビュー情報
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.SUPER_HIGH,
		frequencyComment="レビュー詳細(初期表示,右側の購入の決め手,コメントリスト表示,画像コメント投稿,コメント-いいね件数表示部表示,コメント編集,いいね登録参照),違反登録,年齢認証などで使用するので頻度は高",
		refClassNames={
			"ProductReviewDetailController"
			}
	)
	public ReviewSetVO getReviewFromIndexExcludeWithdraw(String reviewId, boolean includeDeleteContents);
	
	/**
	 * レビュー情報を登録します。
	 * @param review レビュー情報
	 * @param uploadImageIds 画像一覧
	 * @return レビュー情報
	 */
	public ReviewDO addReview(ReviewDO review);
	/**
	 * レビュー情報を更新します。
	 * @param review レビュー情報
	 * @param uploadImageIds 画像一覧
	 * @return レビュー情報
	 */
	public ReviewDO modifyReview(ReviewDO review);
	
	/**
	 * レビュー情報を一時保存します。
	 * @param review レビュー情報
	 * @param uploadImageIds 画像一覧
	 * @return レビュー情報
	 */
	public ReviewDO saveReview(ReviewDO review);
	/**
	 * 指定したレビュー情報を削除します。
	 * @param reviewId レビューID
	 */
	public void deleteReview(String reviewId);
	
	public void deleteReview(String reviewId, boolean mngToolOperation);

	/**
	 * レビューのスコア情報と閲覧数を更新します。
	 * @param targetDate 対象日付
	 * @param review レビュー情報
	 * @param scoreFactor スコア係数
	 */
	public void updateReviewScoreAndViewCountForBatch(
			Date targetDate,
			ReviewDO review,
			ScoreFactorDO scoreFactor);
	public void updateReviewScoreAndViewCountForBatchBegin(int bulkSize);
	public void updateReviewScoreAndViewCountForBatchEnd();

	/**
	 * ポイント付与結果をフィードバックします。
	 * @param pointGrantRequestId ポイント付与ID
	 * @param point ポイント
	 * @param status 結果ステータス
	 */
	public void feedbackPointGrant(
			String pointGrantRequestId,
			Long point,
			String status,
			Date feedbackDate);


	/**
	 * レビュー情報をHbaseから取得し、周辺情報を付与して返します。
	 * SearchResult形式ですが、1件のみ返します
	 * hasAdultはfalse
	 * @param reviewId
	 * @return
	 */
	public SearchResult<ReviewSetVO> loadReviewSet(String reviewId);

	public boolean isShowReview(String reviewId);
	
	public ReviewDO loadReview(String reviewId);
	
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

	
	
	public Set<String> getAlreadyGrantSpCode(String sku, String communityUserId);
	public Set<String> getAlreadyGrantSpCode(ProductDO product, String communityUserId);
	
	@PerformanceTest(type=Type.SELECT,
			frequency=Frequency.SUPER_HIGH,
			frequencyComment="レビュー一覧のMetaタグで使用するので頻度は高",
			refClassNames={
				"ProductReviewListController",
				"ProductFeedsController"
			}
	)
	public ProductSatisfactionSummaryVO getProductSatisfactionSummary(String sku);
	
	@PerformanceTest(type=Type.SELECT,
			frequency=Frequency.SUPER_HIGH,
			frequencyComment="レビュー一覧のMetaタグで使用するので頻度は高",
			refClassNames={
				"ProductReviewListController",
				"ProductFeedsController"
			}
	)
	public ProductSatisfactionSummaryVO getProductSatisfactionSummary(String[] skus);
	
	public Map<String, ProductSatisfactionSummaryVO> getSatisfactionAvarageMap(String[] skus);

	/**
	 * レビュー投稿に際して付与されるポイントを計算します。(SP除外)
	 * @param reviewType レビュータイプ
	 * @param product 商品情報
	 * @param elapsedDays 購入日から投稿日までの経過日数
	 * @return 付与されるポイント詳細リスト
	 */
	public List<PointGrantRequestDetail> getPointGrantRequestDetailsWithoutSp(
			ReviewType reviewType, ProductDO product, Integer elapsedDays);
	
	public String findProductSku(String reviewId);
}
