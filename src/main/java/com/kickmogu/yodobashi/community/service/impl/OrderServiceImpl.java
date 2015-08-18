/**
 *
 */
package com.kickmogu.yodobashi.community.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanPredicate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.functors.EqualPredicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.yodobashi.community.resource.dao.OrderDao;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.service.IndexService;
import com.kickmogu.yodobashi.community.service.OrderService;
import com.kickmogu.yodobashi.community.service.vo.PurchaseProductSetVO;

/**
 * 注文サービスの実装です。
 * @author kamiike
 */
@Service
public class OrderServiceImpl extends AbstractServiceImpl implements OrderService {

	/**
	 * 注文 DAO です。
	 */
	@Autowired
	private OrderDao orderDao;

	/**
	 * インデックスサービスです。
	 */
	@Autowired
	private IndexService indexService;

	/**
	 * 指定した商品の購入商品情報があるかどうか返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @return 指定した商品の購入商品情報があるかどうか
	 */
	@Override
	public boolean existsOrder(String communityUserId, String sku) {
		PurchaseProductDO purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUserId,
				sku,
				Path.DEFAULT,
				false);
		return purchaseProduct != null && !purchaseProduct.isDeleted();
	}

	/**
	 * 指定した商品の購入商品情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @return 指定した商品の購入商品情報
	 */
	@Override
	public PurchaseProductDO getOrder(String communityUserId, String sku) {
		PurchaseProductDO purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUserId, sku, Path.DEFAULT, false);
		if (purchaseProduct != null && purchaseProduct.isDeleted()) {
			return null;
		} else {
			return purchaseProduct;
		}
	}

	/**
	 * 購入商品情報の公開設定を更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku 更新する購入商品情報のSKU
	 * @param publicSetting 公開する場合、true
	 */
	@Override
	@ArroundHBase
	public void updatePublicSettingForPurchaseProduct(String communityUserId,
			String sku, boolean publicSetting) {
		PurchaseProductDO purchaseProduct = getOrder(communityUserId, sku);
		if (purchaseProduct != null && !purchaseProduct.isPublicSetting() == publicSetting) {
			purchaseProduct.setPublicSetting(publicSetting);
			orderDao.updatePurchaseProduct(purchaseProduct,
					Path.includeProp("publicSetting,modifyDateTime"));
			indexService.updateIndexForCreatePurchaseProduct(
					purchaseProduct.getPurchaseProductId());
		}
	}


	@Override
	public SearchResult<PurchaseProductDO> getPurchaseProductWithSkus(
			String communityUserId,
			List<String> exclusionSkus,
			int limit,
			int offset) {
		return orderDao.findPurchaseProductByCommunityUserId(
				communityUserId, 
				exclusionSkus, 
				limit, 
				offset);
	}

	@Override
	public Map<String, PurchaseProductDO> findPurchaseProductBySkusAndCommunityUserId(
			String communityUserId,
			List<String> skus) {
		Map<String, PurchaseProductDO> purchaseProducts = orderDao.findPurchaseProductBySkusAndByCommunityUserId(
				communityUserId, 
				skus, 
				Path.DEFAULT);
		// 削除フラグが立っているものは削除する。
		for( Iterator<Map.Entry<String, PurchaseProductDO>> it = purchaseProducts.entrySet().iterator(); it.hasNext();){
			Map.Entry<String, PurchaseProductDO> entry = it.next();
			if( entry.getValue().isDeleted() ){
				it.remove();
			}
		}
		
		return purchaseProducts;
	}

	@Override
	public Map<String, PurchaseProductSetVO> findPurchaseProductSetBySkusAndCommunityUserId(
			String communityUserId, List<ProductDO> products) {
		List<String> skus = Lists.newArrayList();
		for( ProductDO product : products){
			skus.add(product.getSku());
		}
		
		Map<String, PurchaseProductDO> purchaseProducts = findPurchaseProductBySkusAndCommunityUserId(communityUserId, skus);
		
		List<PurchaseProductSetVO> purchaseProductSets = Lists.newArrayList();
		
		for( ProductDO product : products){
			if( purchaseProducts.containsKey(product.getSku())){
				PurchaseProductDO purchaseProduct = purchaseProducts.get(product.getSku());
				purchaseProduct.setProduct(product);
				purchaseProduct.setAdult(product.isAdult());
				PurchaseProductSetVO purchaseProductSet = new PurchaseProductSetVO();
				purchaseProductSet.setPurchaseProduct(purchaseProduct);
				purchaseProductSets.add(purchaseProductSet);
			}
		}
		
		Map<String, Long> myReviewCountMap = new HashMap<String, Long>();
		Map<String, Long> reviewCountMap = new HashMap<String, Long>();
		
		myReviewCountMap.putAll(
				reviewDao.loadReviewCountMapByCommunityUserIdAndSKU(
				communityUserId, skus));
		reviewCountMap.putAll(
				reviewDao.loadReviewCountMap(skus));
		
		settingPointInformation(communityUserId, myReviewCountMap, myReviewCountMap, null, purchaseProductSets);
		
		Map<String, PurchaseProductSetVO> purchaseProductSetMap = Maps.newHashMap();
		for( PurchaseProductSetVO purchaseProductSet : purchaseProductSets){
			purchaseProductSetMap.put(purchaseProductSet.getPurchaseProduct().getProduct().getSku(), purchaseProductSet);
		}
		
		return purchaseProductSetMap;
	}

	@Override
	public PurchaseProductSetVO settingPointInformationToPurchaseProductSet(
			String communityUserId,
			PurchaseProductSetVO purchaseProductSet) {
		List<String> skus = Lists.newArrayList(purchaseProductSet.getPurchaseProduct().getProduct().getSku());
		List<PurchaseProductSetVO> purchaseProductSets = Lists.newArrayList(purchaseProductSet);
		
		Map<String, Long> myReviewCountMap = new HashMap<String, Long>();
		Map<String, Long> reviewCountMap = new HashMap<String, Long>();
		
		myReviewCountMap.putAll(
				reviewDao.loadReviewCountMapByCommunityUserIdAndSKU(
				communityUserId, skus));
		reviewCountMap.putAll(
				reviewDao.loadReviewCountMap(skus));
		
		settingPointInformation(communityUserId, myReviewCountMap, myReviewCountMap, null, purchaseProductSets);
		
		return purchaseProductSets.get(0);
	}

	/**
	 * レビュー可能商品のSKUリストを返す
	 * @param purchaseProductList
	 * @return
	 */
	@Override
	@ArroundHBase
	public List<String> getPostReviewEnableSkus(List<PurchaseProductDO> purchaseProductList) {

		List<String> resultSkuList = new ArrayList<String>();
		
		// 有効な購入商品からSKUを取り出す
		List<String> searchSkuList = new ArrayList<String>();
		for (PurchaseProductDO purchaseProductDO : purchaseProductList) {
			if (!purchaseProductDO.isDeleted()
					&& purchaseProductDO.getPurchaseDate() != null
					&& purchaseProductDO.getProduct() != null) {
				searchSkuList.add(purchaseProductDO.getProduct().getSku());
			}
		}

		// 最新のProductDOを取得しなおす
		List<ProductDO> productList = productDao.loadProducts(searchSkuList);
		if (productList == null) {
			return resultSkuList;
		}
		
		// purchaseProductの並び順通りにレビュー可能商品のSKUを返す
		for (String sku : searchSkuList) {
			ProductDO productDO = (ProductDO) CollectionUtils.find(productList, new BeanPredicate("sku", new EqualPredicate(sku)));
			if (productDO != null && productDO.isCanReview()) {
				resultSkuList.add(sku);
			}
		}
		return resultSkuList;
	}
	
}
