/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.solr.FacetResult;
import com.kickmogu.lib.solr.SolrConstants;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.resource.cache.MethodCache;
import com.kickmogu.yodobashi.community.resource.cache.CacheStrategyType;
import com.kickmogu.yodobashi.community.resource.cache.TargetSystemType;
import com.kickmogu.yodobashi.community.resource.config.AppConfigurationDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductMasterDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.util.AdultHelper;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.ProductUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.SolrUtil;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.VersionDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.MaintenanceStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.Verification;
import com.kickmogu.yodobashi.community.resource.domain.constants.VersionType;
import com.kickmogu.yodobashi.community.service.impl.SystemMaintenanceServiceImpl;


/**
 * 商品マスター DAO の実装です。
 * @author kamiike
 *
 */
@Service
public class ProductMasterDaoImpl implements ProductMasterDao {

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

	@Autowired
	private CommunityUserDao communityUserDao;

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

	@Autowired
	private AppConfigurationDao appConfigurationDao;

	private boolean validProductMaster(){
		String productMasterOperationStatus =  appConfigurationDao.getWithCache(SystemMaintenanceServiceImpl.productMasterOperationStatusKey);
		if(StringUtils.isEmpty(productMasterOperationStatus) || !productMasterOperationStatus.equals(MaintenanceStatus.IN_OPERATION.getCode())) {
			return false;
		}
		return true;
	}
	

	/**
	 * 商品 DAO です。
	 */
	@Autowired @Qualifier("catalog")
	private ProductDao productDao;

	/**
	 * 商品マスターのバージョンを返します。
	 * @param withLock ロックを取得するかどうかです。
	 * @return 商品マスターのバージョン
	 */
	@Override
	public VersionDO loadProductMasterVersion(boolean withLock) {
		if (withLock) {
			return hBaseOperations.loadWithLock(VersionDO.class, VersionType.PRODUCT_MASTER);
		} else {
			return hBaseOperations.load(VersionDO.class, VersionType.PRODUCT_MASTER);
		}
	}

	/**
	 * 商品マスターを返します。
	 * @param productMasterId 商品マスターID
	 * @param condition 取得条件
	 * @return 商品マスター
	 */
	@Override
	public ProductMasterDO loadProductMaster(
			String productMasterId,
			Condition condition) {
		
		if(!validProductMaster()) {
			return null;
		}

		ProductMasterDO productMaster = hBaseOperations.load(ProductMasterDO.class, productMasterId, condition);
		if (ProductUtil.invalid(productMaster)) {
			return null;
		} else {
			return productMaster;
		}
	}

	/**
	 * 商品マスターのバージョンを更新します。
	 * @param version 商品マスターのバージョン
	 */
	@Override
	public void updateProductMasterVersion(VersionDO version) {
		VersionDO currentVersion = loadProductMasterVersion(false);
		if (currentVersion == null) {
			version.setRegisterDateTime(timestampHolder.getTimestamp());
		}
		version.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(version);
	}

	/**
	 * 商品マスターのインデックスを更新します。
	 * @param productMasterId 商品マスターID
	 */
	@Override
	public void updateProductMasterInIndex(String productMasterId) {
		ProductMasterDO productMaster = hBaseOperations.load(ProductMasterDO.class, productMasterId);
		if (productMaster != null && !productMaster.isDeleted()) {
			solrOperations.save(productMaster);
		} else {
			solrOperations.deleteByKey(ProductMasterDO.class, productMasterId);
		}
	}

	/**
	 * 指定したレビュー情報に紐づく商品マスター情報を返します。
	 * @param productMasters SKUとコミュニティユーザーIDのセットリスト
	 * @return 商品マスター情報リスト
	 */
	@Override
	public List<ProductMasterDO> findProductMasterInRank(
			List<ProductMasterDO> productMasters) {
		
		if(!validProductMaster()) {
			return new ArrayList<ProductMasterDO>();
		}
		
		VersionDO version = loadProductMasterVersion(false);
		if (version == null || productMasters == null || productMasters.size() == 0) {
			return new ArrayList<ProductMasterDO>();
		} else {
			StringBuilder query = new StringBuilder();
			for (ProductMasterDO productMaster : productMasters) {
				if (query.length() > 0) {
					query.append(" OR ");
				}
				query.append("(");
				query.append("productId_s:");
				query.append(SolrUtil.escape(productMaster.getProduct().getSku()));
				query.append(" AND communityUserId_s:");
				query.append(SolrUtil.escape(productMaster.getCommunityUser().getCommunityUserId()));
				query.append(" AND rank_i:[1 TO ");
				query.append(ProductMasterDO.RANK_RANGE);
				query.append("]");
				query.append(")");
			}
			query.append(") AND version_i:");
			query.append(version.getVersion());

			SearchResult<ProductMasterDO> searchResult
			= new SearchResult<ProductMasterDO>(solrOperations.findByQuery(
					new SolrQuery("(" + query.toString()).setRows(
							SolrConstants.QUERY_ROW_LIMIT), ProductMasterDO.class));
			ProductUtil.filterInvalidProduct(searchResult);
			return searchResult.getDocuments();
		}
	}

	/**
	 * 商品マスター数情報を返します。
	 * @param communityUserIds コミュニティユーザーIDのリスト
	 * @return 商品マスター数情報
	 */
	@Override
	public Map<String, Long> loadProductMasterCountMapByCommunityUserId(
			List<String> communityUserIds) {

		Map<String, Long> resultMap = new HashMap<String, Long>();

		if(!validProductMaster()) {
			return resultMap;
		}

		if (communityUserIds == null || communityUserIds.size() == 0) {
			return resultMap;
		}
		VersionDO version = loadProductMasterVersion(false);
		if (version == null) {
			return resultMap;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("rank_i:[1 TO ");
		buffer.append(ProductMasterDO.RANK_RANGE);
		buffer.append("]");
		buffer.append(" AND version_i:");
		buffer.append(version.getVersion());
		buffer.append(" AND (");
		for (int i = 0; i < communityUserIds.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("communityUserId_s:");
			buffer.append(SolrUtil.escape(communityUserIds.get(i)));
		}
		buffer.append(")");
		SolrQuery query = new SolrQuery(
				new AdultHelper(requestScopeDao.loadAdultVerification(
										)).toFilterQuery(buffer.toString()));
		query.addFacetField("communityUserId_s");
		query.setFacetSort(FacetParams.FACET_SORT_COUNT);
		query.setFacetLimit(SolrConstants.QUERY_ROW_LIMIT);
		List<FacetResult<String>> searchResult = solrOperations.facet(
				ProductMasterDO.class, String.class, query);
		for (FacetResult<String> facetResult : searchResult) {
			resultMap.put(facetResult.getValue(), facetResult.getCount());
		}

		return resultMap;
	}

	/**
	 * 商品マスター数情報を返します。
	 * @param skus 商品IDのリスト
	 * @return 商品マスター数情報
	 */
	@Override
	public Map<String, Long> loadProductMasterCountMapBySKU(
			List<String> skus) {
		
		Map<String, Long> resultMap = new HashMap<String, Long>();

		if(!validProductMaster()) {
			return resultMap;
		}
		if (skus == null || skus.size() == 0) {
			return resultMap;
		}
		VersionDO version = loadProductMasterVersion(false);
		if (version == null) {
			return resultMap;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("rank_i:[1 TO ");
		buffer.append(ProductMasterDO.RANK_RANGE);
		buffer.append("]");
		buffer.append(" AND version_i:");
		buffer.append(version.getVersion());
		buffer.append(" AND (");
		for (int i = 0; i < skus.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("productId_s:");
			buffer.append(SolrUtil.escape(skus.get(i)));
		}
		buffer.append(")");
		SolrQuery query = new SolrQuery(
				new AdultHelper(requestScopeDao.loadAdultVerification(
										)).toFilterQuery(buffer.toString()));
		query.addFacetField("productId_s");
		query.setFacetSort(FacetParams.FACET_SORT_COUNT);
		query.setFacetLimit(SolrConstants.QUERY_ROW_LIMIT);
		List<FacetResult<String>> searchResult = solrOperations.facet(
				ProductMasterDO.class, String.class, query);
		for (FacetResult<String> facetResult : searchResult) {
			resultMap.put(facetResult.getValue(), facetResult.getCount());
		}

		return resultMap;
	}

	/**
	 * 指定した商品に紐づく商品マスター情報を返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 商品マスター情報リスト
	 */
	@Override
	public SearchResult<ProductMasterDO> findProductMasterInRankBySKU(
			String sku, int limit, int offset) {
		return findProductMasterInRankBySKU(sku, limit, offset, false);
	}
	
	@Override
	public SearchResult<ProductMasterDO> findProductMasterInRankBySKU(
			String sku, int limit, int offset, boolean excludeProduct) {
		
		if(!validProductMaster()) {
			return new SearchResult<ProductMasterDO>(
					0, new ArrayList<ProductMasterDO>());
		}
		
		
		VersionDO version = loadProductMasterVersion(false);
		if (version == null) {
			return new SearchResult<ProductMasterDO>(
					0, new ArrayList<ProductMasterDO>());
		} else {
			StringBuilder query = new StringBuilder();
			query.append("productId_s:");
			query.append(SolrUtil.escape(sku));
			query.append(" AND rank_i:[1 TO ");
			query.append(ProductMasterDO.RANK_RANGE);
			query.append("]");
			query.append(" AND version_i:");
			query.append(version.getVersion());

			Condition condition = Path.includeProp("*").includePath("product.sku,communityUser.communityUserId").depth(1);
			if(excludeProduct){
				condition = Path.includeProp("*").includePath("communityUser.communityUserId").depth(1);
			}
			
			
			SearchResult<ProductMasterDO> searchResult
					= new SearchResult<ProductMasterDO>(solrOperations.findByQuery(
					new SolrQuery(query.toString()).setRows(limit).setStart(
							offset).setSortField("rank_i", ORDER.asc),
					ProductMasterDO.class, condition));
			if(!excludeProduct)
				ProductUtil.filterInvalidProduct(searchResult);
			return searchResult;
		}
	}

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
	@Override
	public SearchResult<ProductMasterDO> findNewPurchaseDateProductMasterByCommunityUserId(
			String communityUserId, int limit, Date offsetTime, String offsetSku, boolean previous) {
		
		if(!validProductMaster()) {
			return new SearchResult<ProductMasterDO>();
		}

		VersionDO version = loadProductMasterVersion(false);
		if (version == null) {
			return new SearchResult<ProductMasterDO>();
		}

		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());

		StringBuilder buffer = new StringBuilder();
		buffer.append("communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND rank_i:[1 TO ");
		buffer.append(ProductMasterDO.RANK_RANGE);
		buffer.append("]");
		buffer.append(" AND version_i:");
		buffer.append(version.getVersion());
		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND ((");
				buffer.append("purchaseDate_dt:[" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *]");
				if(StringUtils.isNotEmpty(offsetSku))
					buffer.append(" AND productId_s:{" +
							SolrUtil.escape(offsetSku) + " TO *}");
				buffer.append(") OR ");
				buffer.append("purchaseDate_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
				buffer.append(")");
			} else {
				buffer.append(" AND ((");
				buffer.append("purchaseDate_dt:[* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "]");
				if(StringUtils.isNotEmpty(offsetSku))
					buffer.append(" AND productId_s:{* TO " +
							SolrUtil.escape(offsetSku) + "}");
				buffer.append(") OR ");
				buffer.append("purchaseDate_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
				buffer.append(")");
			}
		}
		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(
				buffer.toString()));
		if (offsetTime == null || !previous) {
			query.setSortField("purchaseDate_dt", ORDER.desc);
			query.addSortField("productId_s", ORDER.desc);
		} else {
			query.setSortField("purchaseDate_dt", ORDER.asc);
			query.addSortField("productId_s", ORDER.asc);
		}
		query.setRows(limit);
		SearchResult<ProductMasterDO> searchResult = new SearchResult<ProductMasterDO>(
				solrOperations.findByQuery(
				query, ProductMasterDO.class, Path.includeProp(
				"*").includePath("product.sku,communityUser.communityUserId").depth(1)));
		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(
							buffer.toString(), ProductMasterDO.class, solrOperations));
		}
		ProductUtil.filterInvalidProduct(searchResult);
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}

	/**
	 * 指定したコミュニティユーザーの商品マスター情報を
	 * 順位の高い順に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetRank 検索開始ランク
	 * @param offsetSku 検索開始SKU
	 * @param previous より前を取得する場合、true
	 * @param asc 昇順ソートの場合、true
	 * @param アダルト確認フラグ
	 * @return 商品マスター一覧
	 */
	@Override
	@MethodCache(
			cacheStrategy=CacheStrategyType.HttpSession,
			size=10,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityDataSyncWeb}
			)
	public SearchResult<ProductMasterDO> findRankProductMasterByCommunityUserId(
			String communityUserId,
			int limit,
			Integer offsetRank,
			String offsetSku,
			boolean previous,
			boolean asc,
			Verification adultVerification) {

		if(!validProductMaster()) {
			return new SearchResult<ProductMasterDO>();
		}
		
		VersionDO version = loadProductMasterVersion(false);
		if (version == null) {
			return new SearchResult<ProductMasterDO>();
		}

		StringBuilder buffer = new StringBuilder();
		buffer.append("communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND rank_i:[1 TO ");
		buffer.append(ProductMasterDO.RANK_RANGE);
		buffer.append("]");
		buffer.append(" AND version_i:");
		buffer.append(version.getVersion());
		if (offsetRank != null) {
			if ((previous && !asc) || (!previous && asc)) {
				buffer.append(" AND ((");
				buffer.append("rank_i:[" +
						offsetRank + " TO *]");
				buffer.append(" AND productId_s:{" +
						offsetSku + " TO *}");
				buffer.append(") OR ");
				buffer.append("rank_i:{" +
						offsetRank + " TO *}");
				buffer.append(")");
			} else {
				buffer.append(" AND ((");
				buffer.append("rank_i:[* TO " +
						offsetRank + "]");
				buffer.append(" AND productId_s:{* TO " +
						offsetSku + "}");
				buffer.append(") OR ");
				buffer.append("rank_i:{* TO " +
						offsetRank + "}");
				buffer.append(")");
			}
		}
		AdultHelper adultHelper = new AdultHelper(adultVerification);

		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(buffer.toString()));
		if (offsetRank == null || !previous) {
			if (asc) {
				query.setSortField("rank_i", ORDER.asc);
				query.addSortField("productId_s", ORDER.asc);
			} else {
				query.setSortField("rank_i", ORDER.desc);
				query.addSortField("productId_s", ORDER.desc);
			}
		} else {
			if (asc) {
				query.setSortField("rank_i", ORDER.desc);
				query.addSortField("productId_s", ORDER.desc);
			} else {
				query.setSortField("rank_i", ORDER.asc);
				query.addSortField("productId_s", ORDER.asc);
			}
		}
		query.setRows(limit);
		SearchResult<ProductMasterDO> searchResult = new SearchResult<ProductMasterDO>(
				solrOperations.findByQuery(
				query, ProductMasterDO.class, Path.includeProp(
				"*").includePath("product.sku,communityUser.communityUserId").depth(1)));
		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(
							buffer.toString(), ProductMasterDO.class, solrOperations));
		}
		ProductUtil.filterInvalidProduct(searchResult);
		if (offsetRank == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}

	
	/**
	 * 指定したコミュニティユーザーの商品マスター情報を
	 * 順位の高い順に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetRank 検索開始ランク
	 * @param offsetSku 検索開始SKU
	 * @param previous より前を取得する場合、true
	 * @param asc 昇順ソートの場合、true
	 * @param アダルト確認フラグ
	 * @return 商品マスター一覧
	 */
	@Override
	public SearchResult<ProductMasterDO> findRankProductMasterByCommunityUserIdForMR(
			String communityUserId,
			int limit,
			Integer offsetRank,
			String offsetSku,
			boolean previous,
			boolean asc,
			Verification adultVerification) {

		if(!validProductMaster()) {
			return new SearchResult<ProductMasterDO>();
		}

		VersionDO version = loadProductMasterVersion(false);
		if (version == null) {
			return new SearchResult<ProductMasterDO>();
		}

		StringBuilder buffer = new StringBuilder();
		buffer.append("communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND rank_i:[1 TO ");
		buffer.append(ProductMasterDO.RANK_RANGE);
		buffer.append("]");
		buffer.append(" AND version_i:");
		buffer.append(version.getVersion());
		if (offsetRank != null) {
			if ((previous && !asc) || (!previous && asc)) {
				buffer.append(" AND ((");
				buffer.append("rank_i:[" +
						offsetRank + " TO *]");
				buffer.append(" AND productId_s:{" +
						offsetSku + " TO *}");
				buffer.append(") OR ");
				buffer.append("rank_i:{" +
						offsetRank + " TO *}");
				buffer.append(")");
			} else {
				buffer.append(" AND ((");
				buffer.append("rank_i:[* TO " +
						offsetRank + "]");
				buffer.append(" AND productId_s:{* TO " +
						offsetSku + "}");
				buffer.append(") OR ");
				buffer.append("rank_i:{* TO " +
						offsetRank + "}");
				buffer.append(")");
			}
		}
		AdultHelper adultHelper = new AdultHelper(adultVerification);

		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(buffer.toString()));
		if (offsetRank == null || !previous) {
			if (asc) {
				query.setSortField("rank_i", ORDER.asc);
				query.addSortField("productId_s", ORDER.asc);
			} else {
				query.setSortField("rank_i", ORDER.desc);
				query.addSortField("productId_s", ORDER.desc);
			}
		} else {
			if (asc) {
				query.setSortField("rank_i", ORDER.desc);
				query.addSortField("productId_s", ORDER.desc);
			} else {
				query.setSortField("rank_i", ORDER.asc);
				query.addSortField("productId_s", ORDER.asc);
			}
		}
		query.setRows(limit);
		SearchResult<ProductMasterDO> searchResult = new SearchResult<ProductMasterDO>(
				solrOperations.findByQuery(
				query, ProductMasterDO.class, Path.includeProp(
				"*").includePath("communityUser.communityUserId").depth(1)));
		
		List<String> skus = new ArrayList<String>();
		for(ProductMasterDO productMaster:searchResult.getDocuments()) {
			skus.add(productMaster.getProduct().getSku());
		}
		Map<String, ProductDO> productMap = productDao.findBySkuForMR(skus);
		for(ProductMasterDO productMaster:searchResult.getDocuments()) {
			productMaster.setProduct(productMap.get(productMaster.getProduct().getSku()));
		}		
		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(
							buffer.toString(), ProductMasterDO.class, solrOperations));
		}
		ProductUtil.filterInvalidProduct(searchResult);
		if (offsetRank == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}

	
	/**
	 * 指定したコミュニティユーザーの商品マスター数返します。
	 * @param communityUserId コミュニティユーザーID
	 * @return 商品マスター数
	 */
	@Override
	public long countRankProductMasterByCommunityUserId(
			String communityUserId) {

		if(!validProductMaster()) {
			return 0;
		}

		VersionDO version = loadProductMasterVersion(false);
		if (version == null) {
			return 0;
		}

		StringBuilder buffer = new StringBuilder();
		buffer.append("communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND rank_i:[1 TO ");
		buffer.append(ProductMasterDO.RANK_RANGE);
		buffer.append("]");
		buffer.append(" AND version_i:");
		buffer.append(version.getVersion());
		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());

		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(buffer.toString()));
		query.setRows(0);
		return solrOperations.count(
				query, ProductMasterDO.class);
	}

	/**
	 * 指定したコミュニティユーザーがフォローした商品の商品マスターである
	 * コミュニティユーザーを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findDistinctProductMasterByFollowProduct(
			String communityUserId, int limit, int offset) {
		
		if(!validProductMaster()) {
			return new SearchResult<CommunityUserDO>();
		}
		
		VersionDO version = loadProductMasterVersion(false);
		if (version == null) {
			return new SearchResult<CommunityUserDO>();
		}
		//重複除去と投稿順の並び替えを同時に満たすため、solr ではサブクエリ
		//とdistinctをサポートしていないので、上限数を絞り、java 側で処理します。
		SearchResult<ProductFollowDO> follows
				= new SearchResult<ProductFollowDO>(solrOperations.findByQuery(
				new SolrQuery("communityUserId_s:" + SolrUtil.escape(communityUserId)
						).setRows(SolrConstants.QUERY_ROW_LIMIT),
				ProductFollowDO.class, Path.includeProp("followProductId")));
		ProductUtil.filterInvalidProduct(follows);
		if (follows.getNumFound() == 0) {
			return new SearchResult<CommunityUserDO>();
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("rank_i:1");
		buffer.append(" AND version_i:");
		buffer.append(version.getVersion());
		buffer.append(" AND (");
		for (int i = 0; i < follows.getDocuments().size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("productId_s:");
			buffer.append(SolrUtil.escape(follows.getDocuments().get(i).getFollowProduct().getSku()));
		}
		buffer.append(")");
		return createDistinctCommunityUsers(buffer.toString(), limit, offset);
	}

	/**
	 * 指定したコミュニティユーザーに紐づく新しくランクインした商品マスター情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 商品マスター情報リスト
	 */
	@Override
	public SearchResult<ProductMasterDO> findProductMasterInNewRankByCommunityUserId(
			String communityUserId, int limit, int offset,
			Verification adultVerification) {

		if(!validProductMaster()) {
			return new SearchResult<ProductMasterDO>(
					0, new ArrayList<ProductMasterDO>());
		}
		
		VersionDO version = loadProductMasterVersion(false);
		if (version == null) {
			return new SearchResult<ProductMasterDO>(
					0, new ArrayList<ProductMasterDO>());
		} else {
			StringBuilder query = new StringBuilder();
			query.append("(communityUserId_s:");
			query.append(SolrUtil.escape(communityUserId));
			query.append(" AND rank_i:[1 TO ");
			query.append(ProductMasterDO.RANK_RANGE);
			query.append("]");
			query.append(") AND version_i:");
			query.append(version.getVersion());
			query.append(" AND requiredNotify_b:true");

			AdultHelper adultHelper = new AdultHelper(
					adultVerification);

			SearchResult<ProductMasterDO> searchResult = new SearchResult<ProductMasterDO>(
					solrOperations.findByQuery(
					new SolrQuery(adultHelper.toFilterQuery(
							query.toString())).setRows(limit).setStart(
							offset).setSortField("rankInVersion_i", ORDER.desc
									).addSortField("rank_i", ORDER.asc),
					ProductMasterDO.class, Path.includeProp(
							"*").includePath("product.sku").depth(1)));
			ProductUtil.filterInvalidProduct(searchResult);
			return searchResult;
		}
	}

	public SearchResult<ProductMasterDO> findProductMasterInNewRankByCommunityUserIdForMR(
			String communityUserId, int limit, int offset,
			Verification adultVerification) {

		if(!validProductMaster()) {
			return new SearchResult<ProductMasterDO>(
					0, new ArrayList<ProductMasterDO>());
		}
		
		VersionDO version = loadProductMasterVersion(false);
		if (version == null) {
			return new SearchResult<ProductMasterDO>(
					0, new ArrayList<ProductMasterDO>());
		} else {
			StringBuilder query = new StringBuilder();
			query.append("(communityUserId_s:");
			query.append(SolrUtil.escape(communityUserId));
			query.append(" AND rank_i:[1 TO ");
			query.append(ProductMasterDO.RANK_RANGE);
			query.append("]");
			query.append(") AND version_i:");
			query.append(version.getVersion());
			query.append(" AND requiredNotify_b:true");

			AdultHelper adultHelper = new AdultHelper(
					adultVerification);

			SearchResult<ProductMasterDO> searchResult = new SearchResult<ProductMasterDO>(
					solrOperations.findByQuery(
					new SolrQuery(adultHelper.toFilterQuery(
							query.toString())).setRows(limit).setStart(
							offset).setSortField("rankInVersion_i", ORDER.desc
									).addSortField("rank_i", ORDER.asc),
					ProductMasterDO.class, Path.includeProp("*")));

			List<String> skus = new ArrayList<String>();
			for(ProductMasterDO productMaster:searchResult.getDocuments()) {
				skus.add(productMaster.getProduct().getSku());
			}
			Map<String, ProductDO> productMap = productDao.findBySkuForMR(skus);
			for(ProductMasterDO productMaster:searchResult.getDocuments()) {
				productMaster.setProduct(productMap.get(productMaster.getProduct().getSku()));
			}		
			ProductUtil.filterInvalidProduct(searchResult);
			return searchResult;
		}
	}
	/**
	 * 重複しないコミュニティユーザーのリストを返します。
	 * @param query 商品マスタ検索クエリ
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	private SearchResult<CommunityUserDO> createDistinctCommunityUsers(
			String query,
			int limit,
			int offset) {

		SearchResult<CommunityUserDO> result = new SearchResult<CommunityUserDO>();
		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());
		SearchResult<ProductMasterDO> productMasters
				= new SearchResult<ProductMasterDO>(
				solrOperations.findByQuery(
						new SolrQuery(adultHelper.toFilterQuery(query)).setRows(
										SolrConstants.QUERY_ROW_LIMIT).setStart(
												0).addSortField("rank_i", ORDER.asc),
												ProductMasterDO.class, Path.includeProp("communityUserId")));
		if (adultHelper.isRequireCheckAdult()) {
			result.setHasAdult(
					adultHelper.hasAdult(query, ReviewDO.class, solrOperations));
		}
		if (productMasters.getNumFound() == 0) {
			return new SearchResult<CommunityUserDO>();
		}
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(productMasters.getDocuments());
		List<String> communityUserIds = new ArrayList<String>();
		List<String> communityUserIdAll = new ArrayList<String>();
		String communityUserId = requestScopeDao.loadCommunityUserId();
		for (ProductMasterDO productMaster : productMasters.getDocuments()) {
			// 一時停止対応
			if (productMaster.isStop(communityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				continue;
			}

			if (!communityUserIdAll.contains(
					productMaster.getCommunityUser().getCommunityUserId())) {
				communityUserIdAll.add(
						productMaster.getCommunityUser().getCommunityUserId());
				if (communityUserIdAll.size() > offset
						&& communityUserIdAll.size() <= (offset + limit)) {
					communityUserIds.add(
							productMaster.getCommunityUser().getCommunityUserId());
				}
			}
		}
		result.setNumFound(communityUserIdAll.size());
		if(! communityUserIds.isEmpty()){
			Map<String, CommunityUserDO> resultMap = solrOperations.find(
					CommunityUserDO.class, String.class, communityUserIds);
			for (String target : communityUserIds) {
				result.getDocuments().add(resultMap.get(target));
			}
		}
		return result;
	}

	/**
	 * 商品マスターを登録します。
	 * @param productMasters 商品マスターリスト
	 */
	@Override
	public void createProductMastersWithIndex(List<ProductMasterDO> productMasters) {
		for (ProductMasterDO productMaster : productMasters) {
			productMaster.setProductMasterId(IdUtil.createIdByConcatIds(
					IdUtil.formatVersion(productMaster.getVersion()),
					productMaster.getProduct().getSku(),
					productMaster.getCommunityUser().getCommunityUserId()));
			productMaster.setModifyDateTime(timestampHolder.getTimestamp());
		}
		hBaseOperations.save(ProductMasterDO.class, productMasters);
		solrOperations.save(ProductMasterDO.class, productMasters);
	}
}
