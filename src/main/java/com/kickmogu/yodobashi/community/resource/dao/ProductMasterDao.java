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
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.VersionDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.Verification;


/**
 * 商品マスター DAO です。
 * @author kamiike
 *
 */
public interface ProductMasterDao {

	/**
	 * 商品マスターのバージョンを返します。
	 * @param withLock ロックを取得するかどうかです。
	 * @return 商品マスターのバージョン
	 */
	public VersionDO loadProductMasterVersion(boolean withLock);

	/**
	 * 商品マスターを返します。
	 * @param productMasterId 商品マスターID
	 * @param condition 取得条件
	 * @return 商品マスター
	 */
	public ProductMasterDO loadProductMaster(
			String productMasterId,
			Condition condition);

	/**
	 * 商品マスターのバージョンを更新します。
	 * @param version 商品マスターのバージョン
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="バッチ処理なのでテスト対象外")
	public void updateProductMasterVersion(VersionDO version);

	/**
	 * 商品マスターのインデックスを作成します。
	 * @param productMasterId 商品マスターID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="バッチ処理なのでテスト対象外")
	public void updateProductMasterInIndex(String productMasterId);

	/**
	 * 指定したレビュー情報に紐づく商品マスター情報を返します。
	 * @param productMasters SKUとコミュニティユーザーIDのセットリスト
	 * @return 商品マスター情報リスト
	 */
	public List<ProductMasterDO> findProductMasterInRank(List<ProductMasterDO> productMasters);

	/**
	 * 商品マスター数情報を返します。
	 * @param communityUserIds コミュニティユーザーIDのリスト
	 * @return 商品マスター数情報
	 */
	public Map<String, Long> loadProductMasterCountMapByCommunityUserId(
			List<String> communityUserIds);

	/**
	 * 商品マスター数情報を返します。
	 * @param skus 商品idのリスト
	 * @return 商品マスター数情報
	 */
	public Map<String, Long> loadProductMasterCountMapBySKU(
			List<String> skus);

	/**
	 * 指定した商品に紐づく商品マスター情報を返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param adultVerification アダルト表示確認ステータス
	 * @return 商品マスター情報リスト
	 */
	public SearchResult<ProductMasterDO> findProductMasterInRankBySKU(
			String sku, int limit, int offset);
	public SearchResult<ProductMasterDO> findProductMasterInRankBySKU(
			String sku, int limit, int offset, boolean excludeProduct);

	/**
	 * 指定したコミュニティユーザーの商品マスター情報を
	 * 購入日の新しい順に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param offsetSku 検索開始SKU
	 * @param previous より前を取得する場合、true
	 * @return 商品マスター一覧
	 */
	public SearchResult<ProductMasterDO> findNewPurchaseDateProductMasterByCommunityUserId(
			String communityUserId, int limit, Date offsetTime, String offsetSku, boolean previous);

	/**
	 * 指定したコミュニティユーザーの商品マスター情報を
	 * 順位の高い順に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetRank 検索開始ランク
	 * @param offsetSku 検索開始SKU
	 * @param previous より前を取得する場合、true
	 * @param asc 昇順ソートの場合、true
	 * @param adultVerification アダルト表示確認ステータス
	 * @return 商品マスター一覧
	 */
	public SearchResult<ProductMasterDO> findRankProductMasterByCommunityUserId(
			String communityUserId,
			int limit,
			Integer offsetRank,
			String offsetSku,
			boolean previous,
			boolean asc,
			Verification adultVerification);

	public SearchResult<ProductMasterDO> findRankProductMasterByCommunityUserIdForMR(
			String communityUserId,
			int limit,
			Integer offsetRank,
			String offsetSku,
			boolean previous,
			boolean asc,
			Verification adultVerification);
	/**
	 * 指定したコミュニティユーザーの商品マスター数返します。
	 * @param communityUserId コミュニティユーザーID
	 * @return 商品マスター数
	 */
	public long countRankProductMasterByCommunityUserId(
			String communityUserId);

	/**
	 * 指定したコミュニティユーザーがフォローした商品の商品マスターである
	 * コミュニティユーザーを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findDistinctProductMasterByFollowProduct(
			String communityUserId, int limit, int offset);

	/**
	 * 指定したコミュニティユーザーに紐づく新しくランクインした商品マスター情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 商品マスター情報リスト
	 */
	public SearchResult<ProductMasterDO> findProductMasterInNewRankByCommunityUserId(
			String communityUserId, int limit, int offset,
			Verification adultVerification);

	public SearchResult<ProductMasterDO> findProductMasterInNewRankByCommunityUserIdForMR(
			String communityUserId, int limit, int offset,
			Verification adultVerification);

	/**
	 * 商品マスターを登録します。
	 * @param productMasters 商品マスターリスト
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="バッチ処理なのでテスト対象外")
	public void createProductMastersWithIndex(List<ProductMasterDO> productMasters);
}
