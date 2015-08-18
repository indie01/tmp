/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.common.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.lib.core.utils.Asserts;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.FacetResult;
import com.kickmogu.lib.solr.SolrConstants;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.resource.cache.CacheStrategyType;
import com.kickmogu.yodobashi.community.resource.cache.MethodCache;
import com.kickmogu.yodobashi.community.resource.cache.TargetSystemType;
import com.kickmogu.yodobashi.community.resource.dao.OrderDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.util.AdultHelper;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.ProductUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.SolrUtil;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.ReceiptDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.ReceiptHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.SlipDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.EffectiveSlipType;
import com.kickmogu.yodobashi.community.resource.domain.constants.OrderEntryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReceiptType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SlipDetailCategory;

/**
 * 注文 DAO です。
 * @author kamiike
 *
 */
@Service
public class OrderDaoImpl implements OrderDao {

	/**
	 * HBaseアクセサです。
	 */
	@Autowired  @Qualifier("default")
	private HBaseOperations hBaseOperations;

	/**
	 * Solrアクセサです。
	 */
	@Autowired @Qualifier("default")
	private SolrOperations solrOperations;

	/**
	 * Solrアクセサです。
	 */
	@Autowired @Qualifier("default-RefMaster")
	private SolrOperations refMasterSolrOperations;

	/**
	 * タイムスタンプホルダーです。
	 */
	@Autowired
	private TimestampHolder timestampHolder;

	/**
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	private RequestScopeDao requestScopeDao;

	/**
	 * 商品 DAO です。
	 */
	@Autowired @Qualifier("catalog")
	private ProductDao productDao;

	/**
	 * 指定したコミュニティユーザーID、SKU の購入履歴を取得します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @param condition 条件
	 * @param withLock ロック有りかどうか
	 * @return 購入履歴
	 */
	@Override
	public PurchaseProductDO loadPurchaseProductBySku(
			String communityUserId,
			String sku,
			Condition condition
			, boolean withLock) {
		PurchaseProductDO purchaseProduct = null;
		
		if (withLock) {
			purchaseProduct = hBaseOperations.loadWithLock(PurchaseProductDO.class,
					createPurchaseProductId(communityUserId, sku),
					condition);
		} else {
			purchaseProduct = hBaseOperations.load(PurchaseProductDO.class,
					createPurchaseProductId(communityUserId, sku),
					condition);
		}
		if (ProductUtil.invalid(purchaseProduct)) {
			return null;
		}
		return purchaseProduct;
	}
	
	/**
	 * 指定したコミュニティユーザーID、SKU一覧で購入履歴を取得します。
	 * @param communityUserId
	 * @param skus SKU一覧
	 * @param condition 条件
	 * @return 購入履歴一覧
	 */
	@Override
	public Map<String, PurchaseProductDO> findPurchaseProductBySkusAndByCommunityUserId(
			String communityUserId, List<String> skus, Condition condition) {
		if( StringUtils.isEmpty(communityUserId) || skus == null || skus.isEmpty() )
			return null;
		
		Map<String, PurchaseProductDO> result = new HashMap<String, PurchaseProductDO>();
		
		List<String> skuIds = new ArrayList<String>();
		for (String sku : skus) {
			skuIds.add(createPurchaseProductId(communityUserId, sku));
		}
		
		Map<String, PurchaseProductDO> map = hBaseOperations.find(
				PurchaseProductDO.class, String.class, skuIds,
				condition);
		for (String sku : skus) {
			PurchaseProductDO purchaseProductDO = map.get(createPurchaseProductId(communityUserId, sku));
			if(purchaseProductDO == null)
				continue;
			result.put(sku, purchaseProductDO);
		}
		
		return result;
	}


	@Override
	public void unlockPurchaseProductBySku(String communityUserId, String sku) {
		hBaseOperations.unlockRow(PurchaseProductDO.class, createPurchaseProductId(communityUserId, sku));
	}

	/**
	 * 指定した商品を購入したコミュニティユーザーを検索して返します。<br />
	 * 一時停止フラグ、アダルトフラグを無視し、フォロー日時の昇順で返します。
	 * @param sku SKU
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 購入者のコミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findOrderCommunityUserBySKUForIndex(
			String sku, int limit, int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND ");
		buffer.append("productId_s:");
		buffer.append(SolrUtil.escape(sku));
		buffer.append(" AND purchaseDate_dt:" + SolrConstants.QUERY_DATE_TO_NOW);
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(limit);
		query.setStart(offset);
		query.addSortField("registerDateTime_dt", ORDER.asc);

		SearchResult<PurchaseProductDO> searchResult = new SearchResult<PurchaseProductDO>(
				solrOperations.findByQuery(
				query, PurchaseProductDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId").depth(1)));

		List<CommunityUserDO> orderCommunityUsers = new ArrayList<CommunityUserDO>();
		for (PurchaseProductDO purchaseProduct : searchResult.getDocuments()) {
			orderCommunityUsers.add(
					purchaseProduct.getCommunityUser());
		}

		SearchResult<CommunityUserDO> result = new SearchResult<CommunityUserDO>(
				searchResult.getNumFound(), orderCommunityUsers);
		return result;
	}
	
	

	/**
	 * 指定した商品を購入したコミュニティユーザーを検索して返します。
	 * @param sku SKU
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param asc 昇順ソート
	 * @return 購入者のコミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findOrderCommunityUserBySKU(
			String sku, String excludeCommunityUserId, int limit, int offset, boolean asc, boolean onlyPublish) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append("productId_s:");
		buffer.append(SolrUtil.escape(sku));
		buffer.append(" AND purchaseDate_dt:" + SolrConstants.QUERY_DATE_TO_NOW);
		if (excludeCommunityUserId != null) {
			buffer.append(" AND !communityUserId_s:");
			buffer.append(SolrUtil.escape(excludeCommunityUserId));
		}
		if(onlyPublish) {
			buffer.append(" AND publicSetting_b:" + true);;
		}
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(limit);
		query.setStart(offset);
		ORDER order = null;
		if (asc) {
			order = ORDER.asc;
		} else {
			order = ORDER.desc;
		}
		query.addSortField("registerDateTime_dt", order);

		SearchResult<PurchaseProductDO> searchResult = new SearchResult<PurchaseProductDO>(
				solrOperations.findByQuery(
				query, PurchaseProductDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId").depth(1)));

		List<CommunityUserDO> orderCommunityUsers = new ArrayList<CommunityUserDO>();
		for (PurchaseProductDO purchaseProduct : searchResult.getDocuments()) {
			orderCommunityUsers.add(
					purchaseProduct.getCommunityUser());
		}

		SearchResult<CommunityUserDO> result = new SearchResult<CommunityUserDO>(
				searchResult.getNumFound(), orderCommunityUsers);
		return result;
	}
	
	@Override
	public Map<String, List<CommunityUserDO>> findOrderCommunityUserBySKUs(
			List<String> skus,
			String excludeCommunityUserId,
			int limit,
			boolean asc,
			boolean onlyPublish) {
		Map<String, List<CommunityUserDO>> result = Maps.newHashMap();
		if (skus.isEmpty()) {
			return result;
		}
		for (String sku:skus) {
			result.put(sku, new ArrayList<CommunityUserDO>());
		}
		
		List<String> currentSkus = Lists.newArrayList(skus.iterator());
		int currentLimit = limit * currentSkus.size();
		
		while (true) {
			StringBuilder buffer = new StringBuilder();
			List<String> skuQueries = Lists.newArrayList();
			for (String sku:currentSkus) {
				skuQueries.add("productId_s:" + SolrUtil.escape(sku));
			}
			buffer.append("( " + StringUtils.join(skuQueries, " OR ") + " )");
			buffer.append(" AND withdraw_b:false ");
			buffer.append(" AND purchaseDate_dt:" + SolrConstants.QUERY_DATE_TO_NOW);
			if (excludeCommunityUserId != null) {
				buffer.append(" AND !communityUserId_s:");
				buffer.append(SolrUtil.escape(excludeCommunityUserId));
			}
			if(onlyPublish) {
				buffer.append(" AND publicSetting_b:" + true);;
			}
			
			SolrQuery query = new SolrQuery(buffer.toString());
			query.setRows(currentLimit);
			query.setStart(0);
			ORDER order = null;
			if (asc) {
				order = ORDER.asc;
			} else {
				order = ORDER.desc;
			}
			query.addSortField("registerDateTime_dt", order);

			SearchResult<PurchaseProductDO> searchResult = new SearchResult<PurchaseProductDO>(
					solrOperations.findByQuery(
					query,
					PurchaseProductDO.class,
					Path.includeProp("*").includePath("communityUser.communityUserId").depth(1)));

			List<String> removeSkus = Lists.newArrayList();
			
			for (String sku:currentSkus) {
				for (PurchaseProductDO purchaseProduct : searchResult.getDocuments()) {
					String purchaseProductSku = purchaseProduct.getProduct().getSku();
					if (purchaseProductSku.equals(sku) && result.get(sku).size() < limit) {
						boolean contains = false;
						for (CommunityUserDO communityUser:result.get(sku)) {
							if (communityUser.getCommunityUserId().equals(purchaseProduct.getCommunityUser().getCommunityUserId())) {
								contains = true;
							}
						}
						if (!contains) {
							result.get(sku).add(purchaseProduct.getCommunityUser());
							if (result.get(sku).size() == limit) {
								removeSkus.add(sku);
							}
						}
					}
				}
			}
			if (currentSkus.size() == 1) break;
			if (searchResult.getNumFound() < currentLimit) break;
			if (removeSkus.size() == currentSkus.size()) break;
			
			// 念のため無限ループ対策
			Asserts.isFalse(removeSkus.isEmpty()); 
			
			currentSkus.removeAll(removeSkus);
			currentLimit = limit * currentSkus.size();
		}
		
		return result;
	}


	/**
	 * 購入履歴情報を新規に登録します。
	 * @param purchaseProduct 購入履歴
	 * @param updateIndex インデックスを更新するかどうか
	 */
	@Override
	public void createPurchaseProduct(
			PurchaseProductDO purchaseProduct,
			boolean updateIndex) {
		purchaseProduct.setPurchaseProductId(createPurchaseProductId(
				purchaseProduct.getCommunityUser().getCommunityUserId(),
				purchaseProduct.getProduct().getSku()));
		purchaseProduct.setRegisterDateTime(timestampHolder.getTimestamp());
		purchaseProduct.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(purchaseProduct);
		if (updateIndex) {
			solrOperations.save(purchaseProduct);
		}
	}

	/**
	 * 指定した外部顧客IDに紐づく受注情報ヘッダーを返します。
	 * @param outerCustomerId 外部顧客ID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 受注情報ヘッダーリスト
	 */
	@Override
	public SearchResult<SlipHeaderDO> findSlipHeaderByOuterCustomerId(
			String outerCustomerId,
			int limit,
			int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("outerCustomerId_s:");
		buffer.append(SolrUtil.escape(outerCustomerId));
		buffer.append(" AND (");
		buffer.append("orderEntryType_s:");
		buffer.append(SolrUtil.escape(OrderEntryType.SHOP_PREPAYED.getCode()));
		buffer.append(" OR orderEntryType_s:");
		buffer.append(SolrUtil.escape(OrderEntryType.DELIVER.getCode()));
		buffer.append(" OR orderEntryType_s:");
		buffer.append(SolrUtil.escape(OrderEntryType.EC.getCode()));
		buffer.append(")");
		return new SearchResult<SlipHeaderDO>(
				refMasterSolrOperations.findByQuery(new SolrQuery(buffer.toString()
				).setRows(limit).setStart(offset).setSortField(
						"id", ORDER.asc), SlipHeaderDO.class));
	}

	/**
	 * 指定した受注伝票番号に紐づく受注情報詳細を返します。
	 * @param slipNos 受注伝票番号リスト
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 受注情報詳細リスト
	 */
	@Override
	public SearchResult<SlipDetailDO> findSlipDetailBySlipNo(
			List<String> slipNos,
			int limit,
			int offset) {
		if (slipNos == null || slipNos.size() == 0) {
			return new SearchResult<SlipDetailDO>(0, new ArrayList<SlipDetailDO>());
		}
		StringBuilder buffer = new StringBuilder();
		for (String slipNo : slipNos) {
			if (buffer.length() > 0) {
				buffer.append(" OR ");
			} else {
				buffer.append("(");
			}
			buffer.append("slipNo_s:");
			buffer.append(SolrUtil.escape(slipNo));
		}
		buffer.append(") AND (");
		buffer.append("slipDetailCategory_s:");
		buffer.append(SolrUtil.escape(SlipDetailCategory.NORMAL.getCode()));
		buffer.append(" OR slipDetailCategory_s:");
		buffer.append(SolrUtil.escape(SlipDetailCategory.MAKER_DIRECT.getCode()));
		buffer.append(" OR slipDetailCategory_s:");
		buffer.append(SolrUtil.escape(SlipDetailCategory.OTHER_COMPANY_SERVICE.getCode()));
		buffer.append(")");
		return new SearchResult<SlipDetailDO>(
				refMasterSolrOperations.findByQuery(new SolrQuery(buffer.toString()).
						setRows(limit).
						setStart(offset).
						setSortField("id", ORDER.asc), 
						SlipDetailDO.class));
	}

	/**
	 * 指定した外部顧客IDに紐づく売上ログ情報ヘッダーを返します。
	 * @param outerCustomerId 外部顧客ID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 売上ログ情報ヘッダーリスト
	 */
	@Override
	public SearchResult<ReceiptHeaderDO> findReceiptHeaderByOuterCustomerId(
			String outerCustomerId,
			int limit,
			int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("outerCustomerId_s:");
		buffer.append(SolrUtil.escape(outerCustomerId));
		return new SearchResult<ReceiptHeaderDO>(
				refMasterSolrOperations.findByQuery(new SolrQuery(buffer.toString()
				).setRows(limit).setStart(offset).setSortField(
						"id", ORDER.asc), ReceiptHeaderDO.class));
	}

	/**
	 * 指定したPOSレシート番号に紐づく売上ログ情報詳細を返します。
	 * @param receiptNo POSレシート番号リスト
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 売上ログ情報詳細リスト
	 */
	@Override
	public SearchResult<ReceiptDetailDO> findReceiptDetailByReceiptNo(
			List<String> receiptNos,
			int limit,
			int offset) {
		if (receiptNos == null || receiptNos.size() == 0) {
			return new SearchResult<ReceiptDetailDO>(0, new ArrayList<ReceiptDetailDO>());
		}
		StringBuilder buffer = new StringBuilder();
		for (String receiptNo : receiptNos) {
			if (buffer.length() > 0) {
				buffer.append(" OR ");
			} else {
				buffer.append("(");
			}
			buffer.append("receiptNo_s:");
			buffer.append(SolrUtil.escape(receiptNo));
		}
		buffer.append(") AND (");
		buffer.append("receiptType_s:");
		buffer.append(SolrUtil.escape(ReceiptType.NORMAL.getCode()));
		buffer.append(" OR receiptType_s:");
		buffer.append(SolrUtil.escape(ReceiptType.INDIVIDUAL.getCode()));
		buffer.append(" OR receiptType_s:");
		buffer.append(SolrUtil.escape(ReceiptType.EC_ORDER_ENTRY.getCode()));
		buffer.append(")");
		return new SearchResult<ReceiptDetailDO>(
				refMasterSolrOperations.findByQuery(new SolrQuery(buffer.toString()
				).setRows(limit).setStart(offset).setSortField(
						"id", ORDER.asc), ReceiptDetailDO.class));
	}

	/**
	 * 参照伝票番号と明細番号から売上ログ明細を取得して返します。
	 * @param refSlipNo 参照伝票番号
	 * @param receiptDetailNo 明細番号
	 * @return 売上ログ明細
	 */
	@Override
	public ReceiptDetailDO loadReceiptDetailByRefSlipNoAndReceiptDetailNo(
			String refSlipNo, int receiptDetailNo) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("refSlipNo_s:");
		buffer.append(SolrUtil.escape(refSlipNo));
		buffer.append(" AND receiptDetailNo_s:");
		buffer.append(receiptDetailNo);
		List<ReceiptDetailDO> list = refMasterSolrOperations.findByQuery(new SolrQuery(buffer.toString()
				).setRows(1), ReceiptDetailDO.class, Path.includeProp("*").includeRelation(ReceiptHeaderDO.class).depth(1)).getDocuments();
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

	/**
	 * 受注伝票番号と明細番号から受注明細を取得して返します。
	 * @param slipNo 受注伝票番号
	 * @param slipDetailNo 明細番号
	 * @return 受注明細
	 */
	@Override
	public SlipDetailDO loadSlipDetailBySlipNoAndSlipDetailNo(
			String slipNo, int slipDetailNo) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("slipNo_s:");
		buffer.append(SolrUtil.escape(slipNo));
		buffer.append(" AND slipDetailNo_s:");
		buffer.append(slipDetailNo);
		List<SlipDetailDO> list = refMasterSolrOperations.findByQuery(new SolrQuery(buffer.toString()
				).setRows(1), SlipDetailDO.class, Path.includeProp("*").includeRelation(SlipHeaderDO.class).depth(1)).getDocuments();
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

	/**
	 * 購入商品詳細を更新します。
	 * @param purchaseProductDetails 購入商品詳細
	 */
	@Override
	public void savePurchaseProductDetailsWithIndex(Collection<PurchaseProductDetailDO> purchaseProductDetails) {
		for (PurchaseProductDetailDO purchaseProductDetail : purchaseProductDetails) {
			purchaseProductDetail.setModifyDateTime(timestampHolder.getTimestamp());
			purchaseProductDetail.setPurchaseProductDetailId(
					createPurchaseProductDetailId(
							purchaseProductDetail.getOuterCustomerId(),
							purchaseProductDetail.getSku()));
		}
		hBaseOperations.save(PurchaseProductDetailDO.class, purchaseProductDetails);
		// FIXME HBaseのscanだけで取得できるならSolrへのインデックスはいらない
		//solrOperations.save(PurchaseProductDetailDO.class, purchaseProductDetails);
	}
	

	@Override
	@ArroundHBase @ArroundSolr
	public void deletePurchaseProductDetailsByOuterCustomerIdWithIndex(
			String outerCustomerId) {
		Asserts.isTrue(outerCustomerId.length() == 10);
		List<PurchaseProductDetailDO> deletePurchaseProductDetails = hBaseOperations.scan(PurchaseProductDetailDO.class, outerCustomerId, Path.includeProp("purchaseProductDetailId,outerCustomerId"));
		System.out.println(deletePurchaseProductDetails.size());
		List<String> deleteKeys = Lists.newArrayList();
		for (PurchaseProductDetailDO purchaseProductDetail:deletePurchaseProductDetails) {
			deleteKeys.add(purchaseProductDetail.getPurchaseProductDetailId());
		}
		for (List<String> doDeleteKeys:Lists.partition(deleteKeys, 100)) {
			System.out.println(doDeleteKeys.get(0));
			hBaseOperations.deleteByKeys(PurchaseProductDetailDO.class, String.class, doDeleteKeys);
		}
		// FIXME Solrはいらないかも
		//solrOperations.deleteByQuery(new SolrQuery("outerCustomerId_s:" + outerCustomerId), PurchaseProductDetailDO.class);
	}

	/**
	 * 指定したSKU、外部顧客IDに紐づく購入商品詳細を返します。
	 * @param sku SKU
	 * @param outerCustomerIds 外部顧客IDのリスト
	 * @return 購入商品詳細リスト
	 */
	@Override
	public Collection<PurchaseProductDetailDO> findPurchaseProductDetailBySkuAndOuterCustomerId(
			String sku, List<String> outerCustomerIds) {
		List<String> ids = new ArrayList<String>();
		for (String outerCustomerId : outerCustomerIds) {
			ids.add(createPurchaseProductDetailId(outerCustomerId, sku));
		}
		return hBaseOperations.find(PurchaseProductDetailDO.class, String.class, ids).values();
	}

	/**
	 * 指定した購入履歴情報の購入日を固定化します。
	 * @param purchaseProductId 購入履歴ID
	 */
	@Override
	public void fixPurchaseDate(String purchaseProductId) {
		PurchaseProductDO purchaseProduct = new PurchaseProductDO();
		purchaseProduct.setPurchaseProductId(purchaseProductId);
		purchaseProduct.setPurchaseDateFix(true);
		purchaseProduct.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(purchaseProduct, Path.includeProp(
				"purchaseDateFix,modifyDateTime"));
	}

	/**
	 * 購入履歴情報をインデックスごと更新登録します。
	 * @param purchaseProduct 購入履歴
	 * @param create 新規作成の場合、true
	 */
	@Override
	public void updatePurchaseProductWithIndex(
			PurchaseProductDO purchaseProduct,
			boolean create) {
		if (create) {
			createPurchaseProduct(purchaseProduct, false);
		} else {
			purchaseProduct.setModifyDateTime(timestampHolder.getTimestamp());
			hBaseOperations.save(purchaseProduct);
		}
		solrOperations.save(purchaseProduct);
	}
	
	

	@Override
	public void updatePurchaseProductsWithIndex(
			List<PurchaseProductDO> purchaseProducts, boolean create) {
		
		for (PurchaseProductDO purchaseProduct:purchaseProducts) {
			if (purchaseProduct.getPurchaseProductId() == null) {
				purchaseProduct.setPurchaseProductId(createPurchaseProductId(
						purchaseProduct.getCommunityUser().getCommunityUserId(),
						purchaseProduct.getProduct().getSku()));				
			}
			if(purchaseProduct.getRegisterDateTime() ==  null) {
				purchaseProduct.setRegisterDateTime(timestampHolder.getTimestamp());
			}
			purchaseProduct.setModifyDateTime(timestampHolder.getTimestamp());				
		}

		hBaseOperations.save(PurchaseProductDO.class, purchaseProducts);
		solrOperations.save(PurchaseProductDO.class, purchaseProducts);
	}

	@Override
	public void updatePurchaseProducts(
			List<PurchaseProductDO> purchaseProducts) {
		
		for (PurchaseProductDO purchaseProduct:purchaseProducts) {
			if (purchaseProduct.getPurchaseProductId() == null) {
				purchaseProduct.setPurchaseProductId(createPurchaseProductId(
						purchaseProduct.getCommunityUser().getCommunityUserId(),
						purchaseProduct.getProduct().getSku()));				
			}
			if(purchaseProduct.getRegisterDateTime() ==  null) {
				purchaseProduct.setRegisterDateTime(timestampHolder.getTimestamp());
			}
			purchaseProduct.setModifyDateTime(timestampHolder.getTimestamp());				
		}

		hBaseOperations.save(PurchaseProductDO.class, purchaseProducts);
	}

	@Override
	public void updatePurchaseProductsInIndex(
			List<PurchaseProductDO> purchaseProducts) {
		Collections.sort(purchaseProducts, new Comparator<PurchaseProductDO>() {
			@Override
			public int compare(PurchaseProductDO arg0, PurchaseProductDO arg1) {
				return arg0.getPurchaseProductId().compareTo(arg1.getPurchaseProductId());
			}
		});
		
		List<PurchaseProductDO> updates = Lists.newArrayList();
		for (PurchaseProductDO product:purchaseProducts) {
			if (product.getPurchaseProductId() == null) {
				product.setPurchaseProductId(createPurchaseProductId(
						product.getCommunityUser().getCommunityUserId(),
						product.getProduct().getSku()));				
			}
			solrOperations.load(PurchaseProductDO.class, product.getPurchaseProductId()); // read lock
			PurchaseProductDO hbaseData = hBaseOperations.load(PurchaseProductDO.class, product.getPurchaseProductId());
			if (hbaseData != null)
				updates.add(hbaseData);
		}
		
		solrOperations.save(PurchaseProductDO.class, updates);
	}

	/**
	 * 購入履歴情報を更新します。
	 * @param purchaseProduct 購入履歴
	 */
	@Override
	public void updatePurchaseProduct(
			PurchaseProductDO purchaseProduct,
			Condition condition) {
		purchaseProduct.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(purchaseProduct, condition);
	}

	/**
	 * 購入履歴情報をインデックスごと削除します。
	 * @param purchaseProductId 購入履歴ID
	 */
	@Override
	public void deletePurchaseProductWithIndex(
			String purchaseProductId) {
		hBaseOperations.deleteByKey(PurchaseProductDO.class, purchaseProductId);
		solrOperations.deleteByKey(PurchaseProductDO.class, purchaseProductId);
	}

	/**
	 * 購入履歴情報のインデックス情報を更新します。
	 * @param purchaseProductId 購入履歴ID
	 */
	@Override
	public void updatePurchaseProductInIndex(String purchaseProductId) {
		solrOperations.load(PurchaseProductDO.class, purchaseProductId); // read lock
		
		PurchaseProductDO purchaseProduct = hBaseOperations.load(
				PurchaseProductDO.class, purchaseProductId);
		solrOperations.save(purchaseProduct);
	}

	/**
	 * 指定したコミュニティユーザーの購入商品を検索して返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param publicOnly 公開設定のもののみ取得する場合、true
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 購入商品情報のリスト
	 */
	@MethodCache(
			cacheStrategy=CacheStrategyType.HttpSession,
			size=10, limitTime=5, limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityDataSyncWeb}
			)
	public SearchResult<PurchaseProductDO> findPurchaseProductByCommunityUserId(
			String communityUserId, boolean publicOnly, int limit, int offset) {
		//hasAdult対応対象です。

		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND ");
		buffer.append("communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND purchaseDate_dt:" + SolrConstants.QUERY_DATE_TO_NOW);
		if (publicOnly) {
			buffer.append(" AND publicSetting_b:true");
		}

		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());
		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(buffer.toString()));
		query.setRows(limit);
		query.setStart(offset);
		query.addSortField("purchaseDate_dt", ORDER.desc);
		query.addSortField("productId_s", ORDER.desc);

		SearchResult<PurchaseProductDO> searchResult
		= new SearchResult<PurchaseProductDO>(solrOperations.findByQuery(
				query, PurchaseProductDO.class, Path.includeProp("*").includePath("product.sku").depth(1)));
		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(
							buffer.toString(), PurchaseProductDO.class, solrOperations));
		}
		ProductUtil.filterInvalidProduct(searchResult);

		return searchResult;
	}
	
	/**
	 * 指定したコミュニティユーザーの購入商品を検索して返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param publicOnly 公開設定のもののみ取得する場合、true
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 購入商品情報のリスト
	 */
	public SearchResult<PurchaseProductDO> findPurchaseProductByCommunityUserIdForMR(
			String communityUserId, boolean publicOnly, int limit, int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND ");
		buffer.append("communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND purchaseDate_dt:" + SolrConstants.QUERY_DATE_TO_NOW);
		if (publicOnly) {
			buffer.append(" AND publicSetting_b:true");
		}

		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());
		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(buffer.toString()));
		query.setRows(limit);
		query.setStart(offset);
		query.addSortField("purchaseDate_dt", ORDER.desc);

		SearchResult<PurchaseProductDO> searchResult
		= new SearchResult<PurchaseProductDO>(solrOperations.findByQuery(
				query, PurchaseProductDO.class, Path.includeProp("*")));
		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(
							buffer.toString(), PurchaseProductDO.class, solrOperations));
		}

		List<String> skus = new ArrayList<String>();
		for(PurchaseProductDO purchaseProduct:searchResult.getDocuments()) {
			skus.add(purchaseProduct.getProduct().getSku());
		}
		Map<String, ProductDO> productMap = productDao.findBySkuForMR(skus);
		for(PurchaseProductDO purchaseProduct:searchResult.getDocuments()) {
			purchaseProduct.setProduct(productMap.get(purchaseProduct.getProduct().getSku()));
		}		
		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}
	
	
	
	/**
	 * 購入履歴IDを生成して返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @return 購入履歴ID
	 */
	@Override
	public String createPurchaseProductId(String communityUserId, String sku) {
		return IdUtil.createIdByConcatIds(communityUserId, sku);
	}

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
	@Override
	public SearchResult<PurchaseProductDO> findPurchaseProductByCommunityUserId(
			String communityUserId,
			boolean publicOnly,
			int limit,
			Date offsetTime,
			String offsetSku,
			boolean previous) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND ");
		buffer.append("communityUserId_s:" + SolrUtil.escape(communityUserId));
		buffer.append(" AND purchaseDate_dt:" + SolrConstants.QUERY_DATE_TO_NOW);
		if (publicOnly) {
			buffer.append(" AND publicSetting_b:true");
		}
		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND ((");
				buffer.append("purchaseDate_dt:[" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *]");
				if(StringUtils.isNotEmpty(offsetSku)){
					buffer.append(" AND productId_s:{" +
							SolrUtil.escape(offsetSku) + " TO *}");
				}
				buffer.append(") OR ");
				buffer.append("purchaseDate_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
				buffer.append(")");
			} else {
				buffer.append(" AND ((");
				buffer.append("purchaseDate_dt:[* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "]");
				if(StringUtils.isNotEmpty(offsetSku)){
					buffer.append(" AND productId_s:{* TO " +
							SolrUtil.escape(offsetSku) + "}");
				}
				buffer.append(") OR ");
				buffer.append("purchaseDate_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
				buffer.append(")");
			}
		}
		AdultHelper adultHelper = new AdultHelper(requestScopeDao.loadAdultVerification());
		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(buffer.toString()));
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("purchaseDate_dt", ORDER.desc);
			query.addSortField("productId_s", ORDER.desc);
		} else {
			query.setSortField("purchaseDate_dt", ORDER.asc);
			query.addSortField("productId_s", ORDER.asc);
		}
		SearchResult<PurchaseProductDO> searchResult = new SearchResult<PurchaseProductDO>(
				solrOperations.findByQuery(query, PurchaseProductDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId," +
						"product.sku").depth(1)));
		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(
							buffer.toString(), PurchaseProductDO.class, solrOperations));
		}
		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}

	@Override
	public SearchResult<PurchaseProductDO> findPurchaseProductByCommunityUserId(
			String communityUserId,
			List<String> exclusionSkus,
			int limit,
			int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND ");
		buffer.append("communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND purchaseDate_dt:" + SolrConstants.QUERY_DATE_TO_NOW);
		if( exclusionSkus != null && !exclusionSkus.isEmpty()){
			buffer.append(" AND !(");
			for(int i = 0; i < exclusionSkus.size(); i++){
				if( i >= 1 ){
					buffer.append(" OR ");
				}
				buffer.append("productId_s:");
				buffer.append(SolrUtil.escape(exclusionSkus.get(i)));
			}
			buffer.append(")");
		}

		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());
		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(buffer.toString()));
		query.setRows(limit);
		query.setStart(offset);
		query.addSortField("purchaseDate_dt", ORDER.desc);
		query.addSortField("registerDateTime_dt", ORDER.desc);

		SearchResult<PurchaseProductDO> searchResult
		= new SearchResult<PurchaseProductDO>(solrOperations.findByQuery(
				query, PurchaseProductDO.class, Path.includeProp("*").includePath("product.sku").depth(1)));
		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(
							buffer.toString(), PurchaseProductDO.class, solrOperations));
		}
		ProductUtil.filterInvalidProduct(searchResult);
		
		// offsetを指定されたときはnumFoundが従来の挙動と異なるので修正
		long newNumFound = searchResult.getNumFound() - offset;
		searchResult.setNumFound(newNumFound < 0 ? 0 : newNumFound);

		return searchResult;
	}

	/**
	 * 購入商品詳細IDを生成して返します。
	 * @param outerCustomerId 外部顧客ID
	 * @param sku SKU
	 * @return 購入商品詳細ID
	 */
	private String createPurchaseProductDetailId(String outerCustomerId, String sku) {
		return IdUtil.createIdByConcatIds(outerCustomerId, sku);
	}

	public Map<String, PurchaseProductDO> findPurchaseProductBySku(List<Map<String, String>> params){
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		
		if(params != null && !params.isEmpty()) {
			buffer.append(" AND ( ");
			boolean isFirst = true;
			for(Map<String, String> param: params){
				if(!isFirst) buffer.append(" OR ");
				buffer.append(" purchaseProductId: ");
				buffer.append(createPurchaseProductDetailId(param.get("communityUserId"), param.get("sku")));
				isFirst = false;
			}
			buffer.append(" ) ");
		}
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(999);
		
		Map<String, PurchaseProductDO> purchaseProductMap = new HashMap<String, PurchaseProductDO>();
		for(PurchaseProductDO purchaseProduct:solrOperations.findByQuery(query, PurchaseProductDO.class,
				Path.includeProp("*").includePath("communityUser.communityUserId").depth(1)).getDocuments()) {
			purchaseProductMap.put(purchaseProduct.getPurchaseProductId(), purchaseProduct);
		}
		return purchaseProductMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Boolean> checkCommunityUserIsEnableFromSlipAndReceipt(
			List<String> checkOuterCustomerIds) {
		
		Map<String, Boolean> result = Maps.newHashMap();
		
		// EffectiveSlipTypeの値が混在している外部顧客IDのセット
		Set<String> mixedOuterCustomerIds = Sets.newHashSet();
		
		for (List<String> outerCustomerIds:Lists.partition(checkOuterCustomerIds, 100)) {
			
			SolrQuery query = new SolrQuery("*:*");
			Map<String,Map<EffectiveSlipType,String>> facetQueryMap = Maps.newHashMap();
			for (String outerCustomerId:outerCustomerIds) {
				facetQueryMap.put(outerCustomerId, new HashMap<EffectiveSlipType, String>());
				for (EffectiveSlipType effectiveSlipType:EffectiveSlipType.values()) {
					String facetQuery = "outerCustomerId_s:"+outerCustomerId+" AND effectiveSlipType_s:" + effectiveSlipType.getCode();
					query.addFacetQuery(facetQuery);
					facetQueryMap.get(outerCustomerId).put(effectiveSlipType, facetQuery);
				}
			}
			
			query.setFacetLimit(query.getFacetQuery().length);
			query.setFacetMinCount(1);
			
			for (Class<?> type:Lists.newArrayList(SlipHeaderDO.class, ReceiptHeaderDO.class)) {
				
				List<FacetResult<String>> facetResults = solrOperations.facet(type, String.class, query);
				Map<String, FacetResult<String>> facetResultMap = Maps.newHashMap();
				for (FacetResult<String> facetResult:facetResults) {
					facetResultMap.put(facetResult.getFacetQuery(), facetResult);
				}
				
				for (Map.Entry<String,Map<EffectiveSlipType,String>> outerCustomerIdAndMapEntry:facetQueryMap.entrySet()) {
					String outerCustomerId = outerCustomerIdAndMapEntry.getKey();
					for (Map.Entry<EffectiveSlipType,String> effectiveTypeAndFacetQuery:outerCustomerIdAndMapEntry.getValue().entrySet()) {
						EffectiveSlipType effectiveSlipType = effectiveTypeAndFacetQuery.getKey();
						String facetQuery = effectiveTypeAndFacetQuery.getValue();
						FacetResult<String> facetResult = facetResultMap.get(facetQuery);
						if (facetResult == null || facetResult.getCount() < 1) continue;
						if (mixedOuterCustomerIds.contains(outerCustomerId)) continue;
						if (result.containsKey(outerCustomerId)) {
							if (result.get(outerCustomerId) != (effectiveSlipType.equals(EffectiveSlipType.EFFECTIVE))) {
								mixedOuterCustomerIds.add(outerCustomerId);
							}
						} else {
							result.put(outerCustomerId, effectiveSlipType.equals(EffectiveSlipType.EFFECTIVE));
						}
					}
				}
			}
		}
		
		for (String mixedOuterCustomerId:mixedOuterCustomerIds) {
			Boolean isEnable = null;
			Date modifyDateTile = null;
			SolrQuery query = new SolrQuery("outerCustomerId_s:"+mixedOuterCustomerId);
			query.setRows(1);
			query.addSortField("modifyDateTime_dt", ORDER.desc);
			com.kickmogu.lib.core.domain.SearchResult<SlipHeaderDO> slipResult = solrOperations.findByQuery(query, SlipHeaderDO.class);
			if (slipResult.getNumFound() > 0) {
				modifyDateTile = slipResult.getDocuments().get(0).getModifyDateTime();
				isEnable = slipResult.getDocuments().get(0).getEffectiveSlipType().equals(EffectiveSlipType.EFFECTIVE);
			}
			com.kickmogu.lib.core.domain.SearchResult<ReceiptHeaderDO> receiptResult = solrOperations.findByQuery(query, ReceiptHeaderDO.class);
			if (receiptResult.getNumFound() > 0) {
				if (isEnable == null ) {
					isEnable = receiptResult.getDocuments().get(0).getEffectiveSlipType().equals(EffectiveSlipType.EFFECTIVE);
				} else if (receiptResult.getDocuments().get(0).getModifyDateTime().after(modifyDateTile)) {
					isEnable = receiptResult.getDocuments().get(0).getEffectiveSlipType().equals(EffectiveSlipType.EFFECTIVE);
				}
			}
			if (isEnable != null) {
				result.put(mixedOuterCustomerId, isEnable);
			}
		}
		
		return result;
	}

}
