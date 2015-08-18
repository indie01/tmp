package com.kickmogu.yodobashi.community.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kickmogu.lib.core.exception.CommonSystemException;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.resource.config.DomainConfig;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.MigrationDao;
import com.kickmogu.yodobashi.community.resource.dao.OrderDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductDetailDao;
import com.kickmogu.yodobashi.community.resource.dao.ReviewDao;
import com.kickmogu.yodobashi.community.resource.dao.SimplePmsDao;
import com.kickmogu.yodobashi.community.resource.dao.SkuCodeNotFoundDao;
import com.kickmogu.yodobashi.community.resource.domain.AccountSharingDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.ReceiptDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.ReceiptHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.SkuCodeNotFoundDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.CancelPointGrantType;
import com.kickmogu.yodobashi.community.resource.domain.constants.EffectiveSlipType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ProductType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PurchaseHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SalesRegistDetailType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SlipDetailCategory;
import com.kickmogu.yodobashi.community.resource.domain.constants.SlipType;
import com.kickmogu.yodobashi.community.service.AggregateOrderService;
import com.kickmogu.yodobashi.community.service.config.ServiceConfig;

@Service @Lazy
public class AggregateOrderServiceImpl implements AggregateOrderService {

	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AggregateOrderServiceImpl.class);

	// PurchaseProductDOの一括書き込みサイズ　※ロック時間節約のため、１Row単位で更新、ロック開放を行う
	protected int PURCHASEPRODUCT_BULK_SIZE = 1;
	
	// HBaseの最大一括書き込みサイズ
	protected int HBASE_MAX_BULK_SIZE = 200;
	
	// Solrの最大一括書き込みサイズ
	protected int SOLR_MAX_BULK_SIZE = 200;
	
	/**
	 * 移行用 DAO です。
	 */
	@Autowired
	private MigrationDao migrationDao;

	/**
	 * レビュー DAO です。
	 */
	@Autowired
	private ReviewDao reviewDao;

	/**
	 * 注文 DAO です。
	 */
	@Autowired
	private OrderDao orderDao;

	/**
	 * 商品詳細 DAO です。
	 */
	@Autowired @Qualifier("default")
	private ProductDetailDao productDetailDao;

	/**
	 * コミュニティユーザー DAO です。
	 */
	@Autowired
	private CommunityUserDao communityUserDao;

	/**
	 * SKU変換エラー DAO です。
	 */
	@Autowired
	private SkuCodeNotFoundDao skuCodeNotFoundDao;

	/**
	 * ポイント管理 DAO です。
	 */
	@Autowired @Qualifier("pms")
	private SimplePmsDao simplePmsDao;

	/**
	 * ドメインコンフィグです。
	 */
	@Autowired
	private DomainConfig domainConfig;

	/**
	 * サービスコンフィグです。
	 */
	@Autowired
	private ServiceConfig serviceConfig;

	/**
	 * 更新通知から呼び出される注文情報の集約処理です。
	 * @param outerCustomerIds 外部顧客IDのリスト
	 */
	@Override
	@ArroundSolr
	@ArroundHBase
	public void aggregateOrder(List<String> outerCustomerIds) {
		if (LOG.isInfoEnabled()) {
			LOG.info("start aggregateOrder. outerCustomerIds=" + outerCustomerIds);
		}
		Map<String, ProductDO> productMap = new HashMap<String, ProductDO>();
		Map<String, Set<String>> skuSets = new HashMap<String, Set<String>>();
		List<SkuCodeNotFoundDO> skuCodeNotFounds = Lists.newArrayList();
		Map<String, Boolean> isEnableMap = Maps.newHashMap();
		
		for (int i = 0; i < outerCustomerIds.size(); i++) {
			List<SkuCodeNotFoundDO> currentSkuCodeNotFounds = Lists.newArrayList();
			String outerCustomerId = outerCustomerIds.get(i);
			int limit = serviceConfig.readLimit;
			int offset = 0;
			Map<String, PurchaseProductDetailDO> purchaseProductDetailMap = new HashMap<String, PurchaseProductDetailDO>();
			//1.外部顧客IDで受注情報を取得します。
			//抽出条件：
			//SlipHeaderDO#orderEntryType = OrderEntryType.SHOP_PREPAYED or OrderEntryType.DELIVER or OrderEntryType.EC
			//SlipDetailDO#slipDetailCategory = SlipDetailCategory.NORMAL or SlipDetailCategory.MAKER_DIRECT
			//or SlipDetailCategory.OTHER_COMPANY_SERVICE（productDetail の JANCode、ProductType = 03、04、05 のもののみ。）
			//JanCodeからSKUを取得。取得できない場合、警告テーブル（JanCode、テーブルタイプ、外部顧客ID、データID）に登録

			// GPC/IC/EC が有効(退会していない)かどうか
			Boolean isEnable = null;
			Date newestModifyDateTime = null;
			
			
			while (true) {
				//キャンセルされた場合、商品購入情報を更新しなくてはいけないため、
				//キャンセルに関わらない項目で検索し、プログラムでチェックします。
				SearchResult<SlipHeaderDO> searchResult = orderDao.findSlipHeaderByOuterCustomerId(
						outerCustomerId, limit, offset);
				List<String> slipNos = new ArrayList<String>();
				Map<String, SlipHeaderDO> headerMap = new HashMap<String, SlipHeaderDO>();
				// TODO ここのチェックは意味が分からない。ループしているので、isEnableフラグがtrueからfalseに変わるケースがあるがいいのか？
				// 受注ヘッダーをチェックしている
				for (SlipHeaderDO header : searchResult.getDocuments()) {
					slipNos.add(header.getSlipNo());
					headerMap.put(header.getSlipNo(), header);
					// GPC/IC/EC が有効(退会していない)かどうかの設定
					if (newestModifyDateTime == null || header.getModifyDateTime().after(newestModifyDateTime)) {
						isEnable = EffectiveSlipType.EFFECTIVE.equals(header.getEffectiveSlipType());
						newestModifyDateTime = header.getModifyDateTime();
					}
				}
				// 受注明細から商品情報を取得している。
				int childOffset = 0;
				while (slipNos.size() > 0) {
					SearchResult<SlipDetailDO> childSearchResult = orderDao.findSlipDetailBySlipNo(
							slipNos, limit, childOffset);
					Set<String> janCodes = new HashSet<String>();
					for (SlipDetailDO detail : childSearchResult.getDocuments()) {
						janCodes.add(detail.getJanCode());
						childOffset++;
					}
					List<String> janCodeList = new ArrayList<String>();
					janCodeList.addAll(janCodes);
					Map<String, String[]> janCodeMap = productDetailDao.loadSkuMap(janCodeList);
					for (SlipDetailDO detail : childSearchResult.getDocuments()) {
						SlipHeaderDO header = headerMap.get(detail.getSlipNo());
						String[] values = janCodeMap.get(detail.getJanCode());
						//JANコードからSKUが検索できない場合、検知できるようにログに残してスキップします。
						if (values == null) {
							SkuCodeNotFoundDO skuCodeNotFound = new SkuCodeNotFoundDO();
							skuCodeNotFound.setJanCode(detail.getJanCode());
							skuCodeNotFound.setDataId(detail.getSlipNo());
							skuCodeNotFound.setDetailNo(detail.getSlipDetailNo());
							skuCodeNotFound.setType(SlipType.SLIP);
							skuCodeNotFound.setOuterCustomerId(outerCustomerId);
							currentSkuCodeNotFounds.add(skuCodeNotFound);
							continue;
						}
						String sku = values[0];
						String productType = values[1];
						String adultKind = values[2];
						if (SlipDetailCategory.OTHER_COMPANY_SERVICE.equals(detail.getSlipDetailCategory()) &&
								!productType.equals(ProductType.DOWNLOAD_WAU.getCode()) && 
								!productType.equals(ProductType.DOWNLOAD_WITHOUT_WAU.getCode()) && 
								!productType.equals(ProductType.DVD_WAU.getCode()) && 
								!productType.equals(ProductType.DOWNLOAD_NUMBER.getCode())) {
							continue;
						}
						
						if (!productMap.containsKey(sku)) {
							ProductDO product = new ProductDO();
							product.setSku(sku);
							product.setAdultKind(adultKind);
							product.setJan(detail.getJanCode());
							productMap.put(sku, product);
						}
						PurchaseProductDetailDO purchaseProductDetail = new PurchaseProductDetailDO();
						purchaseProductDetail.setOuterCustomerId(outerCustomerId);
						purchaseProductDetail.setSku(sku);
						purchaseProductDetail.setJanCode(detail.getJanCode());
						
						if (detail.getEffectiveNum() > 0 && detail.getOldestBillingDate() != null){
							purchaseProductDetail.setOrderDate(header.getOrderEntryDate());
							purchaseProductDetail.setOrderDateRefDataType(SlipType.SLIP);
							purchaseProductDetail.setOrderDateRefId(detail.getSlipNo());
							purchaseProductDetail.setBillingDate(detail.getOldestBillingDate());
							purchaseProductDetail.setBillingDateRefDataType(SlipType.SLIP);
							purchaseProductDetail.setBillingDateRefId(detail.getSlipNo());
						}
						// 同一商品がある場合に、上書きする。
						if (purchaseProductDetailMap.containsKey(sku)) {
							PurchaseProductDetailDO preInfo = purchaseProductDetailMap.get(sku);
							overridePurchaseProductDetailDO(purchaseProductDetail, preInfo);
						}
						
						if (LOG.isDebugEnabled()) {
							LOG.debug("SLIP (outerCustomerId=" + outerCustomerId + ") = " + ToStringBuilder.reflectionToString(purchaseProductDetail,ToStringStyle.SHORT_PREFIX_STYLE));
						}
						// 商品情報をMapに登録する。
						purchaseProductDetailMap.put(sku, purchaseProductDetail);
					}

					if (childSearchResult.getDocuments().size() < limit || childSearchResult.getNumFound() <= childOffset) {
						break;
					}
				}

				offset += searchResult.getDocuments().size();

				if (searchResult.getDocuments().size() < limit || searchResult.getNumFound() <= offset) {
					break;
				}
			}
			//2.外部顧客IDで売り上げログ情報を取得します。
			//抽出条件：
			//ReceiptHeaderDO#effectiveSlipType = EffectiveSlipType.EFFECTIVE
			//ReceiptDetailDO#receiptType = ReceiptType.NORMAL or ReceiptType.INDIVIDUAL
			//JanCodeからSKUを取得。取得できない場合、警告テーブル（JanCode、テーブルタイプ、外部顧客ID、データID）に登録

			offset = 0;
			while (true) {
				//キャンセルされた場合、商品購入情報を更新しなくてはいけないため、
				//キャンセルに関わらない項目で検索し、プログラムでチェックします。
				SearchResult<ReceiptHeaderDO> searchResult = orderDao.findReceiptHeaderByOuterCustomerId(
						outerCustomerId, limit, offset);
				List<String> receiptNos = new ArrayList<String>();
				Map<String, ReceiptHeaderDO> headerMap = new HashMap<String, ReceiptHeaderDO>();
				for (ReceiptHeaderDO header : searchResult.getDocuments()) {
					receiptNos.add(header.getReceiptNo());
					headerMap.put(header.getReceiptNo(), header);
					// GPC/IC/EC が有効(退会していない)かどうかの設定
					if (newestModifyDateTime == null || header.getModifyDateTime().after(newestModifyDateTime)) {
						isEnable = EffectiveSlipType.EFFECTIVE.equals(header.getEffectiveSlipType());
						newestModifyDateTime = header.getModifyDateTime();
					}
				}
				int childOffset = 0;
				while (receiptNos.size() > 0) {
					SearchResult<ReceiptDetailDO> childSearchResult = orderDao.findReceiptDetailByReceiptNo(
							receiptNos, limit, childOffset);
					Set<String> janCodes = new HashSet<String>();
					for (ReceiptDetailDO detail : childSearchResult.getDocuments()) {
						janCodes.add(detail.getJanCode());
						childOffset++;
					}
					List<String> janCodeList = new ArrayList<String>();
					janCodeList.addAll(janCodes);
					Map<String, String[]> janCodeMap = productDetailDao.loadSkuMap(janCodeList);
					for (ReceiptDetailDO detail : childSearchResult.getDocuments()) {
						ReceiptHeaderDO header = headerMap.get(detail.getReceiptNo());
						String[] values = janCodeMap.get(detail.getJanCode());
						//JANコードからSKUが検索できない場合、検知できるようにログに残してスキップします。
						if (values == null) {
							SkuCodeNotFoundDO skuCodeNotFound = new SkuCodeNotFoundDO();
							skuCodeNotFound.setJanCode(detail.getJanCode());
							skuCodeNotFound.setDataId(detail.getReceiptNo());
							skuCodeNotFound.setDetailNo(detail.getReceiptDetailNo());
							skuCodeNotFound.setType(SlipType.RECEIPT);
							skuCodeNotFound.setOuterCustomerId(outerCustomerId);
							currentSkuCodeNotFounds.add(skuCodeNotFound);
							continue;
						}
						String sku = values[0];
						String adultKind = values[2];
						if (!productMap.containsKey(sku)) {
							ProductDO product = new ProductDO();
							product.setSku(sku);
							product.setAdultKind(adultKind);
							product.setJan(detail.getJanCode());
							productMap.put(sku, product);
						}

						PurchaseProductDetailDO purchaseProductDetail = new PurchaseProductDetailDO();
						purchaseProductDetail.setOuterCustomerId(outerCustomerId);
						purchaseProductDetail.setSku(sku);
						purchaseProductDetail.setJanCode(detail.getJanCode());
						if (detail.getNetNum() > 0 && 
								SalesRegistDetailType.EFFECTIVE.equals(detail.getSalesRegistDetailType())) {
							purchaseProductDetail.setOrderDate(header.getSalesDate());
							purchaseProductDetail.setOrderDateRefDataType(SlipType.RECEIPT);
							purchaseProductDetail.setOrderDateRefId(detail.getReceiptNo());
							purchaseProductDetail.setBillingDate(header.getSalesDate());
							purchaseProductDetail.setBillingDateRefDataType(SlipType.RECEIPT);
							purchaseProductDetail.setBillingDateRefId(detail.getReceiptNo());
						}
						// 同一商品がある場合に、上書きする。
						if (purchaseProductDetailMap.containsKey(sku)) {
							PurchaseProductDetailDO preInfo = purchaseProductDetailMap.get(sku);
							overridePurchaseProductDetailDO(purchaseProductDetail, preInfo);
						}
						
						if (LOG.isDebugEnabled()) {
							LOG.debug("RECEIPT (outerCustomerId=" + outerCustomerId + ") = " + ToStringBuilder.reflectionToString(purchaseProductDetail,ToStringStyle.SHORT_PREFIX_STYLE));
						}
						// 商品情報をMapに登録する。
						purchaseProductDetailMap.put(sku, purchaseProductDetail);
					}

					if (childSearchResult.getDocuments().size() < limit || childSearchResult.getNumFound() <= childOffset) {
						break;
					}
				}

				offset += searchResult.getDocuments().size();

				if (searchResult.getDocuments().size() < limit || searchResult.getNumFound() <= offset) {
					break;
				}
			}
			if (isEnable != null) isEnableMap.put(outerCustomerId, isEnable);
			LOG.info("IS_ENABLE outerCustomerId=" + outerCustomerId + " isEnable=" + isEnable);
			
			// GPC/IC/EC退会した場合は、購入商品詳細情報は全て削除
			if (isEnable != null && !isEnable) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Delete PurchaseProductDetail(Slip,Receipt)(outerCustomerId=" + outerCustomerId + ") Size = " + purchaseProductDetailMap.size());
					for (Map.Entry<String, PurchaseProductDetailDO> entry:purchaseProductDetailMap.entrySet()) {
						LOG.debug("DeletePurchaseProductDetail sku = " + entry.getKey() + ", value = " + ToStringBuilder.reflectionToString(entry.getValue(),ToStringStyle.SHORT_PREFIX_STYLE));
					}
				}
				orderDao.deletePurchaseProductDetailsByOuterCustomerIdWithIndex(outerCustomerId);
				if (purchaseProductDetailMap.size() > 0){
					skuSets.put(outerCustomerId, purchaseProductDetailMap.keySet());
				}
				continue;
			}
			skuCodeNotFounds.addAll(currentSkuCodeNotFounds);
			
			if (purchaseProductDetailMap.size() > 0) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Save PurchaseProductDetail(Slip,Receipt)(outerCustomerId=" + outerCustomerId + ") Size = " + purchaseProductDetailMap.size());
					Set<Entry<String, PurchaseProductDetailDO>> purchaseProductDetailMapEntrySet = purchaseProductDetailMap.entrySet();
					Iterator<Entry<String, PurchaseProductDetailDO>> purchaseProductDetailMapIterator = purchaseProductDetailMapEntrySet.iterator();
					while(purchaseProductDetailMapIterator.hasNext()){
						Entry<String, PurchaseProductDetailDO> entry = purchaseProductDetailMapIterator.next();
						LOG.debug("SavePurchaseProductDetail(Slip,Receipt) sku = " + entry.getKey() + ", value = " + ToStringBuilder.reflectionToString(entry.getValue(),ToStringStyle.SHORT_PREFIX_STYLE));
					}
				}
				orderDao.savePurchaseProductDetailsWithIndex(purchaseProductDetailMap.values());
				skuSets.put(outerCustomerId, purchaseProductDetailMap.keySet());
			}

		}
		
		for (List<SkuCodeNotFoundDO> saveSkuCodeNotFounds: Lists.partition(skuCodeNotFounds, 100)) {
			skuCodeNotFoundDao.createListWithIndex(saveSkuCodeNotFounds);
		}

		//外部顧客IDのリストからコミュニティユーザーのリストに集約します。
		List<CommunityUserDO> communityUsers = findCommunityUserByOuterCustomerIds(outerCustomerIds);
		List<String> communityIds = Lists.newArrayList();
		for (CommunityUserDO communityUser:communityUsers) {
			communityIds.add(communityUser.getCommunityId());
		}
		LOG.info("COMMUNITY_USERS outerCustomerIds=" + StringUtils.join(outerCustomerIds,",") + " shareOuterCustomerIds=" + StringUtils.join(communityIds,","));
		
		// 共有化ユーザの有効フラグ(EC/GPC/IC退会していない)は、受注、売上情報より判別
		List<String> checkOuterCustomerIds = Lists.newArrayList();
		for (CommunityUserDO communityUser:communityUsers) {
			if (!isEnableMap.containsKey(communityUser.getCommunityId())) {
				checkOuterCustomerIds.add(communityUser.getCommunityId());
			}
		}
		if (!checkOuterCustomerIds.isEmpty()) {
			Map<String, Boolean> checkIsEnableMap = orderDao.checkCommunityUserIsEnableFromSlipAndReceipt(checkOuterCustomerIds);
			for (Map.Entry<String, Boolean> entry:checkIsEnableMap.entrySet()) {
				LOG.info("IS_ENABLE(ShareOuterCustomer) outerCustomerId=" +entry.getKey() + " isEnable=" + entry.getValue());
			}
			isEnableMap.putAll(checkIsEnableMap);
		}
		
		//購入商品詳細を購入商品に集約します。
		aggregatePurchaseProductAll(
				outerCustomerIds, 
				communityUsers, 
				skuSets,
				productMap, 
				isEnableMap);
		
		LOG.info("end aggregateOrder. outerCustomerIds=" + StringUtils.join(outerCustomerIds,","));
	}

	
	@Override
	public void reCreateAggregateOrder(String outerCustomerId) {
		// TODO Auto-generated method stub
		
	}


	/**
	 * 外部顧客IDのリストからコミュニティユーザーのリストに変換して返します。
	 * @param outerCustomerIds 外部顧客IDのリスト
	 * @return コミュニティユーザーのリスト
	 */
	private List<CommunityUserDO> findCommunityUserByOuterCustomerIds(
			Collection<String> outerCustomerIds) {
		List<CommunityUserDO> communityUsers = new ArrayList<CommunityUserDO>();
		for (String outerCustomerId : outerCustomerIds) {
			boolean exists = false;
			for (CommunityUserDO communityUser : communityUsers) {
				for (AccountSharingDO accountSharing : communityUser.getAccountSharings()) {
					if (accountSharing.getOuterCustomerId().equals(outerCustomerId)) {
						exists = true;
						break;
					}
				}
				if (exists) {
					break;
				}
			}
			if (!exists) {
				List<CommunityUserDO> ecUsers = communityUserDao.findCommunityUserWithAccountSharingByOuterCustomerId(
						outerCustomerId);
				if (ecUsers == null) {
					if (migrationDao.loadMigrationCommunityUserByOuterCustomerId(
							outerCustomerId) != null) {
						LOG.warn("The order from migration pending user to ignore. outerCustomerId=" + outerCustomerId);
					} else {
						LOG.warn("CommunityUser is not found. outerCustomerId = " + outerCustomerId);
					}
					continue;
				}
				communityUsers.addAll(ecUsers);
			}
		}
		return communityUsers;
	}

	/**
	 * 購入商品詳細を購入商品に集約します。
	 * @param outerCustomerIds 処理対象となる外部顧客IDのリスト
	 * @param communityUsers 処理対象となるコミュニティユーザーのリスト
	 * @param skuSets 外部顧客IDをキーに、更新のあったSKUリストが値のマップ
	 * @param productMap SKUと商品情報のマップ
	 */
	private void aggregatePurchaseProductAll(
			Collection<String> outerCustomerIds,
			List<CommunityUserDO> communityUsers,
			Map<String, Set<String>> skuSets,
			Map<String, ProductDO> productMap, Map<String, Boolean> isEnableMap) {
		for (CommunityUserDO communityUser : communityUsers) {
			Set<String> skus = new HashSet<String>();
			List<String> shareOuterCustomerIds = new ArrayList<String>();
			for (AccountSharingDO accountSharing : communityUser.getAccountSharings()) {
				shareOuterCustomerIds.add(accountSharing.getOuterCustomerId());
				if (skuSets.containsKey(accountSharing.getOuterCustomerId())) {
					skus.addAll(skuSets.get(accountSharing.getOuterCustomerId()));
				}
			}
			if (skus.size() == 0) {
				if (LOG.isInfoEnabled()) {
					LOG.info("valid data not found. communityUserId="
							+ communityUser.getCommunityUserId()
							+ ", outerCustomerIds=" + shareOuterCustomerIds);
				}
				continue;
			}
			List<PurchaseProductDO> purchaseProducts = Lists.newArrayList();
			for (String sku : skus) {
				PurchaseProductDO purchaseProduct = aggregatePurchaseProduct(communityUser,
						shareOuterCustomerIds, sku, productMap, isEnableMap);
				if (purchaseProduct != null) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("sku = " + sku + ", purchaseProduct = " + purchaseProduct.toString());
						LOG.debug("-----------------------------------------------------------------------------------------------------------------------");
					}
					
					purchaseProducts.add(purchaseProduct);
					if (purchaseProducts.size() >= PURCHASEPRODUCT_BULK_SIZE) {
						updatePurchaseProductWithIndex(purchaseProducts);
						purchaseProducts.clear();
					}
					
				} else {
					orderDao.unlockPurchaseProductBySku(communityUser.getCommunityUserId(), sku);
				}
			}
			
			if (purchaseProducts.size() > 0) {
				updatePurchaseProductWithIndex(purchaseProducts);
			}
		}
	}
	
	/**
	 * 購入商品の集約処理を行います。
	 * @param communityUser コミュニティユーザー
	 * @param shareOuterCustomerIds 外部顧客IDリスト
	 * @param sku sku
	 * @param productMap SKUと商品情報のマップ
	 */
	private PurchaseProductDO aggregatePurchaseProduct(
			CommunityUserDO communityUser,
			List<String> shareOuterCustomerIds,
			String sku,
			Map<String, ProductDO> productMap,
			Map<String, Boolean> isEnableMap) {
		
		boolean isEnable = true;
		if (isEnableMap.containsKey(communityUser.getCommunityId())) {
			isEnable = isEnableMap.get(communityUser.getCommunityId());
		}
		String argString = " communityUserId=" + communityUser.getCommunityUserId()+ " communityId=" + communityUser.getCommunityId() + " shareOuterCustomerId=" + StringUtils.join(shareOuterCustomerIds,",") + " sku=" + sku + " isEnable=" + isEnable;
		LOG.info("StartAggregatePurchaseProduct " + argString);

		PurchaseProductDO purchaseProduct = new PurchaseProductDO();
		PurchaseProductDO result = purchaseProduct;
		purchaseProduct.setCommunityUser(communityUser);
		purchaseProduct.setProduct(productMap.get(sku));
		purchaseProduct.setAdult(purchaseProduct.getProduct().isAdult());
		purchaseProduct.setPurchaseHistoryType(PurchaseHistoryType.YODOBASHI);
//		int detailCount = 0;
		boolean hasPurchaseProductDetail = false;
		
		for (PurchaseProductDetailDO purchaseProductDetail : orderDao.findPurchaseProductDetailBySkuAndOuterCustomerId(sku, shareOuterCustomerIds)) {
			hasPurchaseProductDetail = true;
			purchaseProduct.setJanCode(purchaseProductDetail.getJanCode());
			
			LOG.debug("MargeStart purchaseProductDetail=" + ToStringBuilder.reflectionToString(purchaseProductDetail, ToStringStyle.SHORT_PREFIX_STYLE));
			
			//注文情報が古いものがあれば、入れ替えします。
			if ((purchaseProduct.getOrderDate() == null && purchaseProductDetail.getOrderDate() != null) || 
					(purchaseProduct.getOrderDate() != null && purchaseProductDetail.getOrderDate() != null && 
					purchaseProduct.getOrderDate().after(purchaseProductDetail.getOrderDate()))) {
				purchaseProduct.setOrderDate(purchaseProductDetail.getOrderDate());
				purchaseProduct.setOrderDateRefDataType(purchaseProductDetail.getOrderDateRefDataType());
				purchaseProduct.setOrderDateRefId(purchaseProductDetail.getOrderDateRefId());
				LOG.debug("OderInfoChanged purchaseProductDetail=" + ToStringBuilder.reflectionToString(purchaseProductDetail, ToStringStyle.SHORT_PREFIX_STYLE));
				LOG.debug("OderInfoChanged purchaseProduct=" + purchaseProduct.toShortString());
			}
			//請求日（購入日付）が古いものがあれば、入れ替えします。
			if ((purchaseProduct.getBillingDate() == null && purchaseProductDetail.getBillingDate() != null) || 
					(purchaseProduct.getBillingDate() != null && purchaseProductDetail.getBillingDate() != null && 
					purchaseProduct.getBillingDate().after(purchaseProductDetail.getBillingDate()))) {
				purchaseProduct.setPurchaseDate(purchaseProductDetail.getBillingDate());
				purchaseProduct.setPurchaseDateRefDataType(purchaseProductDetail.getBillingDateRefDataType());
				purchaseProduct.setPurchaseDateRefId(purchaseProductDetail.getBillingDateRefId());
				purchaseProduct.setBillingDate(purchaseProductDetail.getBillingDate());
				purchaseProduct.setBillingDateRefDataType(purchaseProductDetail.getBillingDateRefDataType());
				purchaseProduct.setBillingDateRefId(purchaseProductDetail.getBillingDateRefId());
				LOG.debug("BillingInfoChanged purchaseProductDetail=" + ToStringBuilder.reflectionToString(purchaseProductDetail, ToStringStyle.SHORT_PREFIX_STYLE));
				LOG.debug("OderInfoChanged purchaseProduct=" + purchaseProduct.toShortString());
			}
//			detailCount++;
		}
		
		LOG.info("PurchaseProductMarged purchaseProduct=" + purchaseProduct.toShortString() + " hasPurchaseProductDetail=" + hasPurchaseProductDetail);
		
		PurchaseProductDO old = orderDao.loadPurchaseProductBySku(
				purchaseProduct.getCommunityUser().getCommunityUserId(),
				purchaseProduct.getProduct().getSku(),
				Path.DEFAULT, true);
		
		LOG.info("OldPurchaseProduct=" +(old != null ? old.toShortString() : "null"));

		// EC/IC/GPC退会しており、購入日付がfixされてない場合は削除する
		if (old != null && !isEnable && !old.isPurchaseDateFix()) {
			orderDao.deletePurchaseProductWithIndex(old.getPurchaseProductId());
			LOG.info("RemovePurchaseProduct because IC/GPC/EC withdrawed. " + argString);
			return null;
		}

		// 既存商品購入情報も購入情報詳細もない場合はなにも行わない
		if (old == null && !hasPurchaseProductDetail) {
			LOG.info("OldPurchaseProductAndPurchaseProductDetailIsNotExist" + argString);
			return null;
		}
		
		// 購入商品情報が既に存在し、購入日付がfixされておらず、入力を受け取った
		// 注文情報の購入日付、注文日付が無い場合、キャンセルとみなし、削除します。
		if (old != null && 
				!old.isPurchaseDateFix() && 
				purchaseProduct.getPurchaseDate() == null) {
			orderDao.deletePurchaseProductWithIndex(old.getPurchaseProductId());
			LOG.info("CancelOrderData PurchaseProductId=" + old.getPurchaseProductId() + argString);
			return null;
		}
		
		if (!hasPurchaseProductDetail) {
			purchaseProduct.setJanCode(old.getJanCode());
		}
		boolean doCheckCancel = false;
		
		// 既存の購入商品情報がなく、購入商品詳細に購入日が存在しない場合は
		// 請求日が確定していないので、購入商品の登録は行わない
		if ( old == null && 
				purchaseProduct.getPurchaseDate() == null &&
				purchaseProduct.getOrderDate() == null) {
			LOG.info("NotSaveBecausePurchaseDateIsNull " + purchaseProduct.getPurchaseProductId() + argString);
			return null;
		}
		
		//既に存在する購入商品情報とマージ処理を行います。
		if (old != null) {
			purchaseProduct.setPurchaseProductId(old.getPurchaseProductId());
			purchaseProduct.setRegisterDateTime(old.getRegisterDateTime());
			purchaseProduct.setPurchaseDateFix(old.isPurchaseDateFix());
			purchaseProduct.setPublicSetting(old.isPublicSetting());
			purchaseProduct.setUserInputPurchaseDate(old.getUserInputPurchaseDate());
			
			boolean update = false;

			// ヨドバシ購入で一度レビューを書いてからキャンセルされた場合に、ヨドバシ外購入となる。
			// キャンセル状態のまま更新された場合は、処理をしない
			if( PurchaseHistoryType.OTHER.equals(old.getPurchaseHistoryType()) && 
					purchaseProduct.getPurchaseDate() == null && 
					purchaseProduct.getOrderDate() == null){
				LOG.info("SlipMergeBecauseCanceled PurchaseProductId=" + old.getPurchaseProductId() + argString);
				return null;
			}
			
			// 他店購入、ヨドバシ購入の切り替え
			// 
			if (!hasPurchaseProductDetail && old.isPurchaseDateFix() && 
					!PurchaseHistoryType.OTHER.equals(old.getPurchaseHistoryType())) {
				update = true;
				doCheckCancel = true;
				purchaseProduct.setPurchaseHistoryType(PurchaseHistoryType.OTHER);
				if (purchaseProduct.getUserInputPurchaseDate() == null) {
					purchaseProduct.setUserInputPurchaseDate(
							purchaseProduct.getPurchaseDate());
				}
				LOG.info("ChangeOtherPurchaseBecauseShareUserIsDisabled PurchaseProduct=" + purchaseProduct.toShortString());
			} else if (hasPurchaseProductDetail && !PurchaseHistoryType.YODOBASHI.equals(old.getPurchaseHistoryType())) {
				update = true;
				purchaseProduct.setPurchaseHistoryType(PurchaseHistoryType.YODOBASHI);
				LOG.info("ChangeYodobashiPurchaseBecauseShareUserIsEnabled " + purchaseProduct.toShortString());
			}
			
			//注文日付は、新旧同じ場合は、念のため旧を設定します。(購入商品詳細が存在しない場合も旧を設定)
			if (matchDate(old.getOrderDate(), purchaseProduct.getOrderDate()) || !hasPurchaseProductDetail) {
				purchaseProduct.setOrderDate(old.getOrderDate());
				purchaseProduct.setOrderDateRefDataType(old.getOrderDateRefDataType());
				purchaseProduct.setOrderDateRefId(old.getOrderDateRefId());
				LOG.debug("OrderDateMergeToOld " + purchaseProduct.toShortString());
			} else {
				update = true;
				LOG.debug("OrderDateMergeToNew " + purchaseProduct.toShortString());
			}
			//購入日付は、fix状態である場合、新旧同じ場合は、念のため旧を設定します。(購入商品詳細が存在しない場合も旧を設定)
			if (old.isPurchaseDateFix() ||
					matchDate(old.getPurchaseDate(), purchaseProduct.getPurchaseDate()) ||
					!hasPurchaseProductDetail) {
				purchaseProduct.setPurchaseDate(old.getPurchaseDate());
				purchaseProduct.setPurchaseDateRefDataType(old.getPurchaseDateRefDataType());
				purchaseProduct.setPurchaseDateRefId(old.getPurchaseDateRefId());
				LOG.debug("PurchaseInfoMergeToOld " + purchaseProduct.toShortString());
			} else {
				update = true;
				LOG.debug("PurchaseInfoMergeToNew " + purchaseProduct.toShortString());
			}
			//請求日付は、新旧同じ場合は、念のため旧を設定します。(購入商品詳細が存在しない場合も旧を設定)
			if (matchDate(old.getBillingDate(), purchaseProduct.getBillingDate()) || !hasPurchaseProductDetail) {
				purchaseProduct.setBillingDate(old.getBillingDate());
				purchaseProduct.setBillingDateRefDataType(old.getBillingDateRefDataType());
				purchaseProduct.setBillingDateRefId(old.getBillingDateRefId());
				LOG.debug("BillingInfoMergeToOld " + purchaseProduct.toShortString());
			//レビュー起算日が決まっている場合は、キャンセルか新しく請求日が決定する場合以外の場合、変更しません。
			} else if (old.isPurchaseDateFix() && 
					old.getBillingDate() != null &&
					purchaseProduct.getBillingDate() != null) {
				purchaseProduct.setBillingDate(old.getBillingDate());
				purchaseProduct.setBillingDateRefDataType(old.getBillingDateRefDataType());
				purchaseProduct.setBillingDateRefId(old.getBillingDateRefId());
				LOG.debug("BillingInfoMergeToOldBecausePurchaseDateFixed " + purchaseProduct.toShortString());
			} else {
				update = true;
				//購入日が固定化されている（=レビューが登録されている）
				//請求日が無くなった場合、キャンセルとなり、キャンセル処理が必要。
				if (old.isPurchaseDateFix() &&
						old.getBillingDate() != null &&
						purchaseProduct.getBillingDate() == null) {
					doCheckCancel = true;
					purchaseProduct.setPurchaseHistoryType(
							PurchaseHistoryType.OTHER);
					if (purchaseProduct.getUserInputPurchaseDate() == null) {
						purchaseProduct.setUserInputPurchaseDate(
								purchaseProduct.getPurchaseDate());
					}
					LOG.debug("CancelAndChanceOtherPurchaseBecauseBillingDateInExpired " + purchaseProduct.toShortString());
				}else{
					LOG.debug("NoMatch " + purchaseProduct.toShortString());
				}
			}

			if (PurchaseHistoryType.YODOBASHI.equals(old.getPurchaseHistoryType()) && !update) {
				LOG.debug("NoMergeBecauseNotChanged " + argString);
				return null;
			}
		}

		LOG.info("UpdatePurchaseProduct "  +purchaseProduct.toShortString() + argString);
		
		//ゆるい共通化でEC会員が複数紐付いている場合、それぞれの購入商品情報を
		//マージする処理を行う。このマージ処理はレビュー登録により fix されている
		//場合も、強制的に一番古い日付に同期を取るものとします。
		List<String> shareCommunityIds = new ArrayList<String>();
		for (AccountSharingDO accountSharing : communityUser.getAccountSharings()) {
			
			// EC退会していないかどうか
			boolean shareIsEnable = true;
			if (isEnableMap.containsKey(accountSharing.getOuterCustomerId())) {
				shareIsEnable = isEnableMap.get(accountSharing.getOuterCustomerId());
			}
			
			// EC会員かつEC退会していない場合
			if (accountSharing.isEc() && shareIsEnable) {
				shareCommunityIds.add(accountSharing.getOuterCustomerId());
			}
		}
		
		 // コミュニティユーザーではないEC会員であれば処理対象外？
		if (shareCommunityIds.size() >= 2) {
			for (Iterator<String> it = shareCommunityIds.iterator(); it.hasNext(); ) {
				String id = it.next();
				if (communityUserDao.loadByHashCommunityId(domainConfig.createHashCommunityId(id),Path.includeProp("communityUserId"), false, false) == null) {
					it.remove();
				}
			}
		}
		
		// ゆるい共通化用マージ処理？ Lock中にゆるい共有先のCommuntyUserIDに関連するPurchaseProductもLockするため、デッドロックとなる危険性あり！
		// TODO 全体処理中で行うイレギュラー処理であり、行ロック中なので別スレッドで処理したい
		LOG.info("ShareCommunityIds=" + StringUtils.join(shareCommunityIds,",") + argString);
		if (shareCommunityIds.size() >= 2) {
			result = null;
			LOG.info("SavePurchaseProductForHavingShareUsers " + purchaseProduct.toShortString());
			orderDao.updatePurchaseProductWithIndex(purchaseProduct, old == null);
			List<PurchaseProductDO> purchaseProducts = new ArrayList<PurchaseProductDO>();
			Collections.sort(shareCommunityIds);
			for (String shareCommunityId : shareCommunityIds) {
				if (communityUser.getCommunityId().equals(shareCommunityId)) {
					purchaseProducts.add(purchaseProduct);
				} else {
					String communityUserId = communityUserDao.loadByHashCommunityId(
							domainConfig.createHashCommunityId(
									shareCommunityId),
									Path.includeProp("communityUserId"),
									false, false).getCommunityUserId();
					PurchaseProductDO target = orderDao.loadPurchaseProductBySku(
							communityUserId,
							purchaseProduct.getProduct().getSku(),
							Path.DEFAULT, true);
					if (target != null) {
						purchaseProducts.add(target);
					} else {
						orderDao.unlockPurchaseProductBySku(communityUserId, purchaseProduct.getProduct().getSku());
					}
				}
			}
			
			PurchaseProductDO model = new PurchaseProductDO();
			model.setAdult(purchaseProduct.getProduct().isAdult());
			model.setPurchaseHistoryType(PurchaseHistoryType.YODOBASHI);
			for (PurchaseProductDO target : purchaseProducts) {
				if (model.getOrderDate() == null ||
						(target.getOrderDate() != null && model.getOrderDate().after(target.getOrderDate()))) {
					model.setOrderDate(target.getOrderDate());
					model.setOrderDateRefDataType(target.getOrderDateRefDataType());
					model.setOrderDateRefId(target.getOrderDateRefId());
				}
				if (model.getPurchaseDate() == null ||
						(target.getPurchaseDate() != null && model.getPurchaseDate().after(target.getPurchaseDate()))) {
					model.setPurchaseDate(target.getPurchaseDate());
					model.setPurchaseDateRefDataType(target.getPurchaseDateRefDataType());
					model.setPurchaseDateRefId(target.getPurchaseDateRefId());
				}
				if (model.getBillingDate() == null || 
						(target.getBillingDate() != null && model.getBillingDate().after(target.getBillingDate()))) {
					model.setBillingDate(target.getBillingDate());
					model.setBillingDateRefDataType(target.getBillingDateRefDataType());
					model.setBillingDateRefId(target.getBillingDateRefId());
				}
				if (target.isPurchaseDateFix()) {
					model.setPurchaseDateFix(true);
				}
			}
			for (PurchaseProductDO target : purchaseProducts) {
				if (!matchDate(model.getOrderDate(), target.getOrderDate())) {
					target.setOrderDate(model.getOrderDate());
					target.setOrderDateRefDataType(model.getOrderDateRefDataType());
					target.setOrderDateRefId(model.getOrderDateRefId());
				}
				if (!matchDate(model.getPurchaseDate(), target.getPurchaseDate())) {
					target.setPurchaseDate(model.getPurchaseDate());
					target.setPurchaseDateRefDataType(model.getPurchaseDateRefDataType());
					target.setPurchaseDateRefId(model.getPurchaseDateRefId());
				}
				if (!matchDate(model.getBillingDate(), target.getBillingDate())) {
					target.setBillingDate(model.getBillingDate());
					target.setBillingDateRefDataType(model.getBillingDateRefDataType());
					target.setBillingDateRefId(model.getBillingDateRefId());
				}
				if (model.isPurchaseDateFix() != target.isPurchaseDateFix()) {
					target.setPurchaseDateFix(model.isPurchaseDateFix());
				}
				
				target.setShare(true);

				LOG.info("MergeShareUser " + target.toShortString());
			}
			updatePurchaseProductWithIndex(purchaseProducts);
		}
		LOG.info("doCheckCancel=" + doCheckCancel + argString);
		if (doCheckCancel) {
			List<ReviewDO> reviews = reviewDao.findEffectiveReviewList(
					sku, communityUser.getCommunityUserId());
			for (ReviewDO review : reviews) {
				if (!review.isDeleted() && review.getPointGrantRequestId() != null) {
					simplePmsDao.cancelPointGrant(
							review.getPointGrantRequestId(),
							CancelPointGrantType.DELETE_REVIEW_ITEM);
					review.setCancelPointGrantType(CancelPointGrantType.DELETE_REVIEW_ITEM);
				}
			}
			reviewDao.updateCancelEffectiveWithIndex(reviews);
		}
		LOG.info("EndAggregatePurchaseProduct result=" + (result != null ? result.toShortString() : "null") + argString);
		return result;
	}

	private void logUpdateError(String msg, List<PurchaseProductDO> purchaseProducts) {
		for (PurchaseProductDO product:purchaseProducts) {
			LOG.error(msg + "\tPurchaseProductDO\t" + product.getPurchaseProductId());
		}
	}
	
	protected void updatePurchaseProductForIndexOnly(List<PurchaseProductDO> purchaseProducts) {
		List<List<PurchaseProductDO>> list = Lists.partition(purchaseProducts, SOLR_MAX_BULK_SIZE);
		for (int i=0; i<list.size(); i++) {			
			try {
				List<PurchaseProductDO> savePurchaseProducts = list.get(i);
				// Solr更新
				orderDao.updatePurchaseProductsInIndex(savePurchaseProducts);
			}catch (Throwable e) {
				LOG.error("AggregateOrder(Solr) error.", e);
				List<PurchaseProductDO> noUpdates = purchaseProducts.subList(i*SOLR_MAX_BULK_SIZE, purchaseProducts.size()); // subList (fromIndex <= index < toIndex)
				logUpdateError("solr unknown update(save) error.", noUpdates); // 以降Update失敗
				if (e instanceof CommonSystemException) {
					throw (CommonSystemException) e;
				}
				throw new CommonSystemException(e);
			}
		}
	}
	
	protected void updatePurchaseProductWithIndex(List<PurchaseProductDO> purchaseProducts) {
		LOG.info("updatePurchaseProductWithIndex size:" + purchaseProducts.size());
		List<PurchaseProductDO> updateds = Lists.newArrayList();
		try {
			List<List<PurchaseProductDO>> list = Lists.partition(purchaseProducts, HBASE_MAX_BULK_SIZE);
			for (int i=0; i<list.size(); i++) {			
				List<PurchaseProductDO> savePurchaseProducts = list.get(i);
				try {
					// HBase更新
					orderDao.updatePurchaseProducts(savePurchaseProducts);
					// 更新成功したものはSolr更新へ
					updateds.addAll(savePurchaseProducts);
				}catch (Throwable e) {
					// updates error.
					LOG.error("AggregateOrder(HBase) error.", e);
					List<PurchaseProductDO> noUpdates = purchaseProducts.subList(i*HBASE_MAX_BULK_SIZE, purchaseProducts.size()); // subList (fromIndex <= index < toIndex)
					logUpdateError("hbase unknown update(put) error.", noUpdates);
					if (e instanceof CommonSystemException) {
						throw (CommonSystemException) e;
					}
					throw new CommonSystemException(e);
				}
				finally {
					// アンロック （ロック時間短縮のためここで開放しておく）
					int cnt=0;
					for (PurchaseProductDO unlockPurchaseProduct:savePurchaseProducts) {
						try {
							orderDao.unlockPurchaseProductBySku(unlockPurchaseProduct.getCommunityUser().getCommunityUserId(), unlockPurchaseProduct.getProduct().getSku());
							cnt++;
						}catch (Throwable t) {
							// unlock error.
							LOG.error("HBase unlock error. \tPurchaseProductDO\t" + unlockPurchaseProduct.getPurchaseProductId(), t);
							
							// Unlockエラー発生ならば以降は全てUnlockエラーとなるはずなので、HBaseも更新失敗として終了する。 エラー後はリカバリ対応へ
							List<PurchaseProductDO> noUpdates = purchaseProducts.subList(i*HBASE_MAX_BULK_SIZE + cnt + 1, purchaseProducts.size()); // subList (fromIndex <= index < toIndex)
							logUpdateError("hbase unknown update(unlock) error.", noUpdates);						
							
							if (t instanceof CommonSystemException) {
								throw (CommonSystemException) t;
							}
							throw new CommonSystemException(t);
						}
					}
				}
			}
		}
		finally {
			// Solr更新
			updatePurchaseProductForIndexOnly(updateds);
		}
	}
		
	/**
	 * 日付を比較し、適切なデータで上書きします。
	 * @param base 基本となる情報
	 * @param old 一つ前の情報
	 */
	private void overridePurchaseProductDetailDO(
			PurchaseProductDetailDO base,
			PurchaseProductDetailDO old) {
		Date preOrderDate = old.getOrderDate();
		Date preBillingDate = old.getBillingDate();
		if (base.getOrderDate() == null && preOrderDate != null) {
			base.setOrderDate(preOrderDate);
			base.setOrderDateRefDataType(old.getOrderDateRefDataType());
			base.setOrderDateRefId(old.getOrderDateRefId());
		} else if (base.getOrderDate() != null &&
				preOrderDate != null &&
				base.getOrderDate().compareTo(preOrderDate) > 0) {
			base.setOrderDate(preOrderDate);
			base.setOrderDateRefDataType(old.getOrderDateRefDataType());
			base.setOrderDateRefId(old.getOrderDateRefId());
		}
		if (base.getBillingDate() == null && preBillingDate != null) {
			base.setBillingDate(preBillingDate);
			base.setBillingDateRefDataType(old.getBillingDateRefDataType());
			base.setBillingDateRefId(old.getBillingDateRefId());
		} else if (base.getBillingDate() != null && 
				preBillingDate != null &&
				base.getBillingDate().compareTo(preBillingDate) > 0) {
			base.setBillingDate(preBillingDate);
			base.setBillingDateRefDataType(old.getBillingDateRefDataType());
			base.setBillingDateRefId(old.getBillingDateRefId());
		}
	}

	/**
	 * 二つの日付が一致しているかどうか判定します。
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean matchDate(Date x, Date y) {
		if (x == null && y == null) {
			return true;
		} else if (x == null && y != null) {
			return false;
		} else if (x != null && y == null) {
			return false;
		} else if (x != null && y != null) {
			return x.compareTo(y) == 0;
		}

		throw new IllegalArgumentException("Date x And y Argument None");
	}
}
