/**
 *
 */
package com.kickmogu.yodobashi.community.service;

import java.util.List;
import java.util.Map;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.service.vo.PurchaseProductSetVO;

/**
 * 注文サービスです。
 * @author kamiike
 */
public interface OrderService extends CommonService{

	/**
	 * 指定した商品の購入商品情報があるかどうか返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @return 指定した商品の購入商品情報があるかどうか
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.RARE,
		frequencyComment="商品画像、レビュー画像投稿時に使われるので頻度は稀",
		refClassNames={"AjaxJsonProductImageSubmitController","AjaxJsonProductReviewSubmitController"}
	)
	public boolean existsOrder(String communityUserId, String sku);

	/**
	 * 指定した商品の購入商品情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @return 指定した商品の購入商品情報
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.RARE,
		frequencyComment="質問回答投稿、商品画像投稿、質問投稿、レビュー投稿時に呼ばれるので頻度は稀",
		refClassNames={
			"AjaxJsonProductAnswerSubmitController",
			"AjaxJsonProductImageSubmitController",
			"AjaxJsonProductQuestionDetailController",
			"AjaxJsonProductQuestionSubmitController",
			"AjaxJsonProductReviewSubmitController"}
	)
	public PurchaseProductDO getOrder(String communityUserId, String sku);


	/**
	 * 購入商品情報の公開設定を更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku 更新する購入商品情報のSKU
	 * @param publicSetting 公開する場合、true
	 */
	public void updatePublicSettingForPurchaseProduct(
			String communityUserId,
			String sku,
			boolean publicSetting);
	
	public SearchResult<PurchaseProductDO> getPurchaseProductWithSkus(
			String communityUserId, 
			List<String> exclusionSkus,
			int limit,
			int offset);
	
	public Map<String, PurchaseProductDO> findPurchaseProductBySkusAndCommunityUserId(
			String communityUserId,
			List<String> skus);
	
	public Map<String, PurchaseProductSetVO> findPurchaseProductSetBySkusAndCommunityUserId(
			String communityUserId,
			List<ProductDO> products);
	
	public PurchaseProductSetVO settingPointInformationToPurchaseProductSet(
			String communityUserId,
			PurchaseProductSetVO purchaseProductSet);
	
	/**
	 * レビュー可能商品のSKUリストを返す
	 */
	public List<String> getPostReviewEnableSkus(List<PurchaseProductDO> purchaseProductList);
}
