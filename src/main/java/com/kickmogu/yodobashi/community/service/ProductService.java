/**
 *
 */
package com.kickmogu.yodobashi.community.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.VersionDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.FillType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PurchaseProductSearchCondition;
import com.kickmogu.yodobashi.community.service.vo.NewsFeedVO;
import com.kickmogu.yodobashi.community.service.vo.ProductMasterSetVO;
import com.kickmogu.yodobashi.community.service.vo.ProductSetVO;
import com.kickmogu.yodobashi.community.service.vo.PurchaseProductSetVO;

/**
 * 商品サービスです。
 * @author kamiike
 *
 */
public interface ProductService {

	/**
	 * 指定した商品のニュースフィードをアクション日時順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return ニュースフィード
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.SUPER_HIGH,
		frequencyComment="Feed系なので頻度は極高",
		refClassNames={
			"AjaxHtmlProductFeedController",
			"ProductFeedsController"}
	)
	public SearchResult<NewsFeedVO> findNewsFeedBySku(
			String sku, int limit, Date offsetTime, boolean previous);
	
	@PerformanceTest(type=Type.SELECT,
			frequency=Frequency.SUPER_HIGH,
			frequencyComment="Feed系なので頻度は極高",
			refClassNames={
				"AjaxHtmlProductFeedController",
				"ProductFeedsController"}
		)
	public SearchResult<NewsFeedVO> findNewsFeedBySku(
			String sku, int limit, Date offsetTime, boolean previous, boolean excludeProduct);
	
	@PerformanceTest(type=Type.SELECT,
			frequency=Frequency.SUPER_HIGH,
			frequencyComment="Feed系なので頻度は極高",
			refClassNames={
				"AjaxHtmlProductFeedController",
				"ProductFeedsController"}
		)
	public SearchResult<NewsFeedVO> findNewsFeedBySkuForWs(
			String sku, int limit, Date offsetTime, boolean previous, boolean excludeProduct);
	
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.SUPER_HIGH,
		frequencyComment="Feed系なので頻度は極高",
		refClassNames={
			"AjaxHtmlProductFeedController",
			"ProductFeedsController"}
	)
	public SearchResult<NewsFeedVO> findNewsFeedBySkus(
			String sku, List<String> skus, int limit, Date offsetTime, boolean previous);
	@PerformanceTest(type=Type.SELECT,
			frequency=Frequency.SUPER_HIGH,
			frequencyComment="Feed系なので頻度は極高",
			refClassNames={
				"AjaxHtmlProductFeedController",
				"ProductFeedsController"}
		)
	public SearchResult<NewsFeedVO> findNewsFeedBySkus(
			String sku, List<String> skus, int limit, Date offsetTime, boolean previous, boolean excludeProduct);

	/**
	 * 指定したコミュニティユーザーの購入商品情報を購入日順（降順）に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param condition 絞込み条件
	 * @param mypage マイページ情報かどうか
	 * @param limit 最大取得件数
	 * @param userLimit この商品を購入した外のユーザーの最大取得数
	 * @param offsetTime 検索開始時間
	 * @param offsetSku 検索開始SKU
	 * @param previous より前を取得する場合、true
	 * @return 購入商品情報
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.LOW,
		frequencyComment="ユーザの購入商品一覧で呼ばれるので頻度は低",
		refClassNames={
			"AjaxJsonMypagePurchaseProductController",
			"AjaxJsonUserController",
			"MypagePurchaseProductListController",
			"UserPurchaseProductListController"
			}
	)
	public SearchResult<PurchaseProductSetVO> findPurchaseProductByCommunityUserIdForMyPage(
			String communityUserId,
			PurchaseProductSearchCondition condition,
			int limit, int userLimit, Date offsetTime, String offsetSku, boolean previous);

	public SearchResult<PurchaseProductSetVO> findPurchaseProductByCommunityUserIdForUserPage(
			String communityUserId,
			PurchaseProductSearchCondition condition,
			int limit, int userLimit, Date offsetTime, String offsetSku, boolean previous);
	/**
	 * 指定した商品の商品マスターをランク順（昇順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @return 商品マスター一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="マイページのニュースフィード初期化、商品の商品マスター一覧で呼ばれるので頻度は高",
		refClassNames={
			"MypageFeedsController",
			"ProductMasterListController"
			}
	)
	public SearchResult<ProductMasterSetVO> findProductMasterBySku(
			String sku, int limit);

	public SearchResult<ProductMasterSetVO> findProductMasterBySku(
			String sku, int limit, boolean excludeProduct);
	/**
	 * 指定したコミュニティユーザーの商品マスター情報を
	 * 購入日の新しい順に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param userLimit この商品を購入した外のユーザーの最大取得数
	 * @param offsetTime 検索開始時間
	 * @param offsetSku 検索開始SKU
	 * @param previous より前を取得する場合、true
	 * @return 商品マスター一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="マイページ,ユーザの商品マスター一覧で呼ばれるので頻度は中",
		refClassNames={
			"AjaxJsonMypageMasterController",
			"AjaxJsonUserMasterController",
			"MypageProductMasterListController",
			"UserProductMasterListController"
			}
	)
	public SearchResult<ProductMasterSetVO> findNewPurchaseDateProductMasterByCommunityUserId(
			String communityUserId, int limit,
			int userLimit, Date offsetTime, String offsetSku, boolean previous);

	/**
	 * 指定したコミュニティユーザーの商品マスター情報を
	 * 順位の高い順に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param userLimit この商品を購入した外のユーザーの最大取得数
	 * @param offsetRank 検索開始ランク
	 * @param offsetSku 検索開始SKU
	 * @param previous より前を取得する場合、true
	 * @param asc 昇順ソートの場合、true
	 * @return 商品マスター一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="マイページ,ユーザの商品マスター一覧で呼ばれるので頻度は中",
		refClassNames={
			"AjaxJsonMypageMasterController",
			"AjaxJsonUserMasterController",
			"MypageProductMasterListController",
			"UserProductMasterListController"
			}
	)
	public SearchResult<ProductMasterSetVO> findRankProductMasterByCommunityUserId(
			String communityUserId,
			int limit,
			int userLimit,
			Integer offsetRank,
			String offsetSku,
			boolean previous,
			boolean asc);

	/**
	 * 指定した商品情報を返します。
	 * @param sku SKU
	 * @return 商品情報（最小セット）
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.NONE,
		frequencyComment="カタログWebサービスなのでテスト対象外"
	)
	public ProductSetVO getProductBySku(String sku);
	/**
	 * 指定した商品情報を返します。
	 * @param sku SKU
	 * @param withCart カート情報を取得するかどうか
	 * @return 商品情報（最小セット）
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.NONE,
		frequencyComment="カタログWebサービスなのでテスト対象外"
	)
	public ProductSetVO getProductBySku(String sku, boolean withCart);
	
	/**
	 * 指定した商品情報を返します。
	 * @param sku SKU
	 * @param withCart カート情報を取得するかどうか
	 * @return 商品情報（最小セット）
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.NONE,
		frequencyComment="カタログWebサービスなのでテスト対象外"
	)
	public List<ProductSetVO> getProductBySkus(List<String> skus);
	/**
	 * 指定した商品情報を返します。
	 * @param sku SKU
	 * @return 商品情報（最小セット）
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.NONE,
		frequencyComment="カタログWebサービスなのでテスト対象外"
	)
	public List<ProductSetVO> getProductBySkus(List<String> skus, boolean withCart);

	/**
	 * 指定した商品情報を返します。
	 * @param sku SKU
	 * @return 商品情報（最小セット）
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.NONE,
		frequencyComment="カタログWebサービスなのでテスト対象外"
	)
	public ProductDO getSimpleProductBySku(String sku);
	
	/**
	 * 指定した商品情報を返します。
	 * @param skus SKU一覧
	 * @return 商品情報（最小セット）の一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.NONE,
		frequencyComment="カタログWebサービスなのでテスト対象外"
	)
	public List<ProductDO> getSimpleProductBySkus(List<String> skus);
	
	public List<ProductSetVO> findVariationProduct(String sku);

	/**
	 * 指定した商品情報を返します。
	 * @param sku SKU
	 * @param fillType フィルタイプ
	 * @param params 取得パラメーター
	 * @return 商品情報
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.NONE,
		frequencyComment="カタログWebサービスなのでテスト対象外"
	)
	public ProductSetVO getProductBySku(
			String sku,
			FillType fillType,
			Map<String,Object> params);
	
	/**
	 * 指定した商品情報を返します。
	 * @param sku SKU
	 * @param fillType フィルタイプ
	 * @param params 取得パラメーター
	 * @return 商品情報
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.NONE,
		frequencyComment="カタログWebサービスなのでテスト対象外"
	)
	public ProductSetVO getProductBySku(
			String sku,
			FillType fillType,
			Map<String,Object> params, 
			boolean withCart);
	
	/**
	 * 指定した商品情報を返します。
	 * @param sku SKU
	 * @param fillType フィルタイプ
	 * @param params 取得パラメーター
	 * @return 商品情報
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.NONE,
		frequencyComment="カタログWebサービスなのでテスト対象外"
	)
	public List<ProductSetVO> getProductBySkus(
			List<String> skus,
			FillType fillType,
			Map<String,Object> params);
	
	/**
	 * 指定した商品情報を返します。
	 * @param sku SKU
	 * @param fillType フィルタイプ
	 * @param params 取得パラメーター
	 * @return 商品情報
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.NONE,
		frequencyComment="カタログWebサービスなのでテスト対象外"
	)
	public List<ProductSetVO> getProductBySkus(
			List<String> skus,
			FillType fillType,
			Map<String,Object> params,
			boolean withCart);

	/**
	 * 指定したキーワードで商品を検索して、返します。
	 * @param keyword キーワード
	 * @param excludeSkus 除外する商品リスト
	 * @param includeCero CERO商品を含める場合、true
	 * @param includeAdult アダルト商品を含める場合、true
	 * @return 商品リスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.NONE,
		frequencyComment="カタログWebサービスなのでテスト対象外"
	)
	public List<ProductDO> findProductByKeyword(
			String keyword,
			List<String> excludeSkus,
			boolean includeCero,
			boolean includeAdult);

	/**
	 * 商品マスターのバージョン情報の次バージョンを取得します。
	 * @return 次のバージョン
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.NONE,
		frequencyComment="バッチから呼ばれるのでテスト対象外"
	)
	public VersionDO getNextProductMasterVersion();

	/**
	 * 商品マスターのバージョン情報をアップグレードします。
	 */
	public void upgradeProductMasterVersion();

	/**
	 * 商品マスターのランキング変動をチェックし、変動していた場合、
	 * 適切な処理を実施します。
	 * @param productMaster 新しい商品マスター情報
	 */
	public void changeProductMasterRanking(ProductMasterDO productMaster);
}
