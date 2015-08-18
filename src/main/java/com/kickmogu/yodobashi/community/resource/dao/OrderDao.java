/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.ReceiptDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.ReceiptHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.SlipDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipHeaderDO;

/**
 * 注文 DAO です。
 * @author kamiike
 *
 */
public interface OrderDao {

	/**
	 * 指定したコミュニティユーザーID、SKU の購入履歴を取得します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @param condition 条件
	 * @param withLock ロック有りかどうか
	 * @return 購入履歴
	 */
	public PurchaseProductDO loadPurchaseProductBySku(
			String communityUserId, String sku, Condition condition, boolean withLock);
	/**
	 * 指定したコミュニティユーザーID、SKU一覧で購入履歴を取得します。
	 * @param communityUserId
	 * @param skus SKU一覧
	 * @param condition 条件
	 * @return 購入履歴一覧
	 */
	public Map<String, PurchaseProductDO> findPurchaseProductBySkusAndByCommunityUserId(
			String communityUserId, List<String> skus, Condition condition);

	
	public void unlockPurchaseProductBySku(String communityUserId, String sku);

	
	/**
	 * 指定した商品を購入したコミュニティユーザーを検索して返します。<br />
	 * 一時停止フラグ、アダルトフラグを無視し、フォロー日時の昇順で返します。
	 * @param sku SKU
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 購入者のコミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findOrderCommunityUserBySKUForIndex(
			String sku, int limit, int offset);

	/**
	 * 指定した商品を購入したコミュニティユーザーを検索して返します。
	 * @param sku SKU
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param asc 昇順ソート
	 * @return 購入者のコミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findOrderCommunityUserBySKU(
			String sku, String excludeCommunityUserId, int limit, int offset, boolean asc, boolean onlyPublish);
	
	public Map<String, List<CommunityUserDO>> findOrderCommunityUserBySKUs(
			List<String> skus, String excludeCommunityUserId, int limit, boolean asc, boolean onlyPublish);

	/**
	 * 購入履歴情報を新規に登録します。
	 * @param purchaseProduct 購入履歴
	 * @param updateIndex インデックスを更新するかどうか
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.MEDIUM, frequencyComment="レビュー、質問、画像投稿なので未登録の場合に登録されるので頻度は中くらい")
	public void createPurchaseProduct(
			PurchaseProductDO purchaseProduct,
			boolean updateIndex);

	/**
	 * 指定した外部顧客IDに紐づく受注情報ヘッダーを返します。
	 * @param outerCustomerId 外部顧客ID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 受注情報ヘッダーリスト
	 */
	public SearchResult<SlipHeaderDO> findSlipHeaderByOuterCustomerId(
			String outerCustomerId,
			int limit,
			int offset);

	/**
	 * 指定した受注伝票番号に紐づく受注情報詳細を返します。
	 * @param slipNos 受注伝票番号リスト
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 受注情報詳細リスト
	 */
	public SearchResult<SlipDetailDO> findSlipDetailBySlipNo(
			List<String> slipNos,
			int limit,
			int offset);

	/**
	 * 指定した外部顧客IDに紐づく売上ログ情報ヘッダーを返します。
	 * @param outerCustomerId 外部顧客ID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 売上ログ情報ヘッダーリスト
	 */
	public SearchResult<ReceiptHeaderDO> findReceiptHeaderByOuterCustomerId(
			String outerCustomerId,
			int limit,
			int offset);

	/**
	 * 指定したPOSレシート番号に紐づく売上ログ情報詳細を返します。
	 * @param receiptNo POSレシート番号リスト
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 売上ログ情報詳細リスト
	 */
	public SearchResult<ReceiptDetailDO> findReceiptDetailByReceiptNo(
			List<String> receiptNos,
			int limit,
			int offset);

	/**
	 * 参照伝票番号と明細番号から売上ログ明細を取得して返します。
	 * @param refSlipNo 参照伝票番号
	 * @param receiptDetailNo 明細番号
	 * @return 売上ログ明細
	 */
	public ReceiptDetailDO loadReceiptDetailByRefSlipNoAndReceiptDetailNo(
			String refSlipNo, int receiptDetailNo);

	/**
	 * 受注伝票番号と明細番号から受注明細を取得して返します。
	 * @param slipNo 受注伝票番号
	 * @param slipDetailNo 明細番号
	 * @return 受注明細
	 */
	public SlipDetailDO loadSlipDetailBySlipNoAndSlipDetailNo(
			String slipNo, int slipDetailNo);

	/**
	 * 購入商品詳細を更新します。
	 * @param purchaseProductDetails 購入商品詳細
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.MEDIUM, frequencyComment="更新通知のAggregateOrderServiceImplで呼ばれるので頻度は稀")
	public void savePurchaseProductDetailsWithIndex(Collection<PurchaseProductDetailDO> purchaseProductDetails);

	/**
	 * 指定したSKU、外部顧客IDに紐づく購入商品詳細を返します。
	 * @param sku SKU
	 * @param outerCustomerIds 外部顧客IDのリスト
	 * @return 購入商品詳細リスト
	 */
	public Collection<PurchaseProductDetailDO> findPurchaseProductDetailBySkuAndOuterCustomerId(
			String sku, List<String> outerCustomerIds);

	/**
	 * 指定した購入履歴情報の購入日を固定化します。
	 * @param purchaseProductId 購入履歴ID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="レビュー登録時、購入履歴情報の購入日を固定していない場合呼ばれるので頻度は稀")
	public void fixPurchaseDate(String purchaseProductId);

	/**
	 * 購入履歴情報をインデックスごと更新登録します。
	 * @param purchaseProduct 購入履歴
	 * @param create 新規作成の場合、true
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.MEDIUM, frequencyComment="更新通知のAggregateOrderServiceImplで呼ばれるので頻度は稀")
	public void updatePurchaseProductWithIndex(
			PurchaseProductDO purchaseProduct,
			boolean create);

	public void updatePurchaseProductsWithIndex(
			List<PurchaseProductDO> purchaseProduct,
			boolean create);
	
	/**
	 * 購入履歴情報を更新します。
	 * @param purchaseProduct 購入履歴
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.MEDIUM, frequencyComment="マイページの購入商品情報の公開設定を更新時のみ呼ばれるので頻度は稀")
	public void updatePurchaseProduct(
			PurchaseProductDO purchaseProduct,
			Condition condition);

	/**
	 * 購入履歴情報をインデックスごと削除します。
	 * @param purchaseProductId 購入履歴ID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="更新通知のAggregateOrderServiceImplで呼ばれ、消される条件も厳しいので頻度は超稀なのでテスト対象外")
	public void deletePurchaseProductWithIndex(
			String purchaseProductId);

	/**
	 * 購入履歴情報のインデックス情報を更新します。
	 * @param purchaseProductId 購入履歴ID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.MEDIUM, frequencyComment="レビュー、質問、画像投稿なので未登録の場合に登録されるので頻度は中くらい")
	public void updatePurchaseProductInIndex(String purchaseProductId);

	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.MEDIUM, frequencyComment="レビュー、質問、画像投稿なので未登録の場合に登録されるので頻度は中くらい（HBase更新のみ）")
	public void updatePurchaseProducts(List<PurchaseProductDO> purchaseProducts);

	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.MEDIUM, frequencyComment="レビュー、質問、画像投稿なので未登録の場合に登録されるので頻度は中くらい（HBase取得&Solr更新）")
	public void updatePurchaseProductsInIndex(List<PurchaseProductDO> purchaseProducts);
	
	/**
	 * 指定したコミュニティユーザーの購入商品を検索して返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param publicOnly 公開設定のもののみ取得する場合、true
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 購入商品情報のリスト
	 */
	public SearchResult<PurchaseProductDO> findPurchaseProductByCommunityUserId(
			String communityUserId, boolean publicOnly, int limit, int offset);

	public SearchResult<PurchaseProductDO> findPurchaseProductByCommunityUserIdForMR(
			String communityUserId, boolean publicOnly, int limit, int offset);
	
	public SearchResult<PurchaseProductDO> findPurchaseProductByCommunityUserId(
			String communityUserId, List<String> exclusionSkus, int limit, int offset);
	/**
	 * 購入履歴IDを生成して返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @return 購入履歴ID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="ID生成のみなのでテスト対象外")
	public String createPurchaseProductId(String communityUserId, String sku);

	/**
	 * 指定したコミュニティユーザーの購入商品情報を購入日順（降順）に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param publicOnly 公開データのみ取得するかどうか
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param offsetSku 検索開始SKU
	 * @param previous より前を取得する場合、true
	 * @return 購入商品情報
	 */
	public SearchResult<PurchaseProductDO> findPurchaseProductByCommunityUserId(
			String communityUserId,
			boolean publicOnly,
			int limit,
			Date offsetTime, String offsetSku, boolean previous);
	
	public Map<String, PurchaseProductDO> findPurchaseProductBySku(List<Map<String, String>> params);

	public void deletePurchaseProductDetailsByOuterCustomerIdWithIndex(
			String outerCustomerId);

	public Map<String, Boolean> checkCommunityUserIsEnableFromSlipAndReceipt(List<String> checkOuterCustomerIds);
}
