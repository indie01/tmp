/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.common.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.utils.Asserts;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.solr.FacetResult;
import com.kickmogu.lib.solr.SolrConstants;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.util.AdultHelper;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.ProductUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.SolrUtil;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;


/**
 * フォロー DAO です。
 * @author kamiike
 *
 */
@Service
public class ProductFollowDaoImpl implements ProductFollowDao {

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

	/**
	 * SKUから商品のフォロワーのコミュニティユーザーを検索して返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return フォロワーのコミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findFollowerCommunityUserBySKUForIndex(
			String sku, int limit, int offset) {
		return findFollowerCommunityUserBySKU(sku, limit, offset, true);
	}

	/**
	 * SKUから商品のフォロワーのコミュニティユーザーを検索して返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param asc 昇順ソート
	 * @return フォロワーのコミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findFollowerCommunityUserBySKU(
			String sku, int limit, int offset, boolean asc) {
		SolrQuery query = new SolrQuery("followProductId_s:" + SolrUtil.escape(sku));
		ORDER order = null;
		if (asc) {
			order = ORDER.asc;
		} else {
			order = ORDER.desc;
		}
		query.addSortField("followDate_dt", order);
		query.setRows(limit);
		query.setStart(offset);
		SearchResult<ProductFollowDO> searchResult = new SearchResult<ProductFollowDO>(
				solrOperations.findByQuery(
				query, ProductFollowDO.class,
				Path.includeProp("*").includePath(
				"communityUser.communityUserId").depth(1)));

		List<CommunityUserDO> followerCommunityUsers = new ArrayList<CommunityUserDO>();
		for (ProductFollowDO productFollow : searchResult.getDocuments()) {
			followerCommunityUsers.add(
					productFollow.getCommunityUser());
		}

		SearchResult<CommunityUserDO> result = new SearchResult<CommunityUserDO>(
				searchResult.getNumFound(), followerCommunityUsers);
		return result;
	}

	/**
	 * 指定したフォロワー、商品の商品フォロー情報
	 * が存在するか判定します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followProductId フォローする商品ID
	 * @return フォロー済みの場合、true
	 */
	@Override
	public boolean existsProductFollow(
			String communityUserId, String followProductId) {
		String productFollowId = createProductFollowId(communityUserId, followProductId);
		return hBaseOperations.load(ProductFollowDO.class,
				productFollowId, Path.includeProp("productFollowId")) != null;
	}

	/**
	 * 商品フォロー情報を新規に作成します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followProductId フォローする商品ID
	 * @param adult アダルト商品かどうか
	 * @return 商品フォロー情報
	 */
	@Override
	public ProductFollowDO createProductFollow(
			String communityUserId,
			String followProductId,
			boolean adult) {
		String productFollowId = createProductFollowId(communityUserId, followProductId);
		ProductFollowDO productFollow = new ProductFollowDO();
		productFollow.setProductFollowId(productFollowId);
		productFollow.setCommunityUser(new CommunityUserDO());
		productFollow.getCommunityUser().setCommunityUserId(communityUserId);
		productFollow.setFollowProduct(new ProductDO());
		productFollow.getFollowProduct().setSku(followProductId);
		productFollow.setFollowDate(timestampHolder.getTimestamp());
		productFollow.setRegisterDateTime(timestampHolder.getTimestamp());
		productFollow.setModifyDateTime(timestampHolder.getTimestamp());
		productFollow.setAdult(adult);
		hBaseOperations.save(productFollow);
		return productFollow;
	}

	/**
	 * 商品フォロー情報を削除します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followProductId フォローする商品ID
	 */
	@Override
	public void deleteFollowProduct(
			String communityUserId,
			String followProductId) {
		String productFollowId = createProductFollowId(communityUserId, followProductId);
		hBaseOperations.scanDeleteWithIndex(
				ActionHistoryDO.class, "communityUserId", communityUserId,
				hBaseOperations.createFilterBuilder(ActionHistoryDO.class
				).appendSingleColumnValueFilter(
						"productId", CompareOp.EQUAL,
						followProductId).appendSingleColumnValueFilter(
								"actionHistoryType", CompareOp.EQUAL,
								ActionHistoryType.USER_FOLLOW_PRODUCT).toFilter());
		hBaseOperations.deleteByKey(ProductFollowDO.class, productFollowId);
	}

	/**
	 * 商品のフォロワーのコミュニティユーザー数を返します。
	 * @param sku SKU
	 * @return 商品のフォロワーのコミュニティユーザー数
	 */
	@Override
	public long countFollowerCommunityUser(
			String sku) {
		return solrOperations.findByQuery(
				new SolrQuery("followProductId_s:" + SolrUtil.escape(sku)).setRows(0),
				ProductFollowDO.class).getNumFound();
	}

	/**
	 * 商品のフォロワーのコミュニティユーザー数を返します。
	 * @param skus SKUリスト
	 * @return 商品のフォロワーのコミュニティユーザー数リスト
	 */
	@Override
	public Map<String, Long> countFollowerCommunityUserBySku(
			String[] skus) {
		
		Asserts.isTrue(skus.length > 0);
		
		Map<String, String> followerCommunityUserQueryMap = new HashMap<String,String>();
		SolrQuery solrQuery = new SolrQuery("*:*");
		Map<String, Long> followerCommunityUserCountMap = new HashMap<String, Long>();
		
		for(String sku:skus){
			StringBuilder buffer = new StringBuilder();
			buffer.append("followProductId_s:" + SolrUtil.escape(sku));
			String query = buffer.toString();
			followerCommunityUserQueryMap.put(query, sku);
			solrQuery.addFacetQuery(query);
		}
		solrQuery.setFacetLimit(solrQuery.getFacetQuery().length);
		solrQuery.setFacetMinCount(0);
		
		for (FacetResult<String> facetResult : solrOperations.facet(ProductFollowDO.class, String.class, solrQuery)) {
			if(followerCommunityUserQueryMap.containsKey(facetResult.getFacetQuery())){
				followerCommunityUserCountMap.put(followerCommunityUserQueryMap.get(facetResult.getFacetQuery()), facetResult.getCount());
			}
		}
		return followerCommunityUserCountMap;
	}

	/**
	 * 商品フォロー情報のインデックスを作成します。
	 * @param productFollowId 商品フォローID
	 * @return 作成した場合、true
	 */
	@Override
	public boolean createProductFollowInIndex(
			String productFollowId) {
		ProductFollowDO productFollow = hBaseOperations.load(
				ProductFollowDO.class, productFollowId);
		if (productFollow != null) {
			solrOperations.save(productFollow);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 商品フォロー情報のインデックスを更新します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followProductId フォローする商品ID
	 */
	@Override
	public void updateProductFollowInIndex(
			String communityUserId, String followProductId) {
		String productFollowId = createProductFollowId(communityUserId, followProductId);
		if (!createProductFollowInIndex(productFollowId)) {
			solrOperations.deleteByQuery(new SolrQuery(
					"communityUserId_s:"
					+ SolrUtil.escape(communityUserId)
					+ " AND productId_s:"
					+ SolrUtil.escape(followProductId)
					+ " AND actionHistoryType_s:"
					+ SolrUtil.escape(ActionHistoryType.USER_FOLLOW_PRODUCT.getCode())),
					ActionHistoryDO.class);
			solrOperations.deleteByKey(ProductFollowDO.class, productFollowId);
		}
	}

	/**
	 * フォローしている商品情報をフォロー日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 検索結果
	 */
	@Override
	public SearchResult<ProductFollowDO> findFollowProduct(
			String communityUserId, int limit, Date offsetTime, boolean previous) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("communityUserId_s:" + SolrUtil.escape(communityUserId));
		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND followDate_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND followDate_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}
		AdultHelper adultHelper = new AdultHelper(requestScopeDao.loadAdultVerification());
		SolrQuery query = new SolrQuery(
				adultHelper.toFilterQuery(buffer.toString()));
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("followDate_dt", ORDER.desc);
		} else {
			query.setSortField("followDate_dt", ORDER.asc);
		}
		SearchResult<ProductFollowDO> searchResult = new SearchResult<ProductFollowDO>(
				solrOperations.findByQuery(query, ProductFollowDO.class,
				Path.includeProp("*").includePath(
						"followProduct.sku").depth(1)));

		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(
							buffer.toString(), ProductFollowDO.class, solrOperations));
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
	 * 指定したコミュニティユーザーが購入した商品をフォローしている
	 * ユーザーを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param publicSetting 非公開情報を表示する場合はtrue
	 * @return レビューを書いているコミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findFollowerByPurchaseProduct(
			String communityUserId, int limit, int offset, boolean publicSetting) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("communityUserId_s:" + SolrUtil.escape(communityUserId));
		buffer.append(" AND purchaseDate_dt:" + SolrConstants.QUERY_DATE_TO_NOW);
		if (false == publicSetting) {
			buffer.append(" AND publicSetting_b:" + true);
		}

		SearchResult<PurchaseProductDO> purchaseProducts
				= new SearchResult<PurchaseProductDO>(solrOperations.findByQuery(
				new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT),
						PurchaseProductDO.class, Path.includeProp("productId")));
		if (purchaseProducts.getNumFound() == 0) {
			return new SearchResult<CommunityUserDO>();
		}
		buffer = new StringBuilder();
		for (int i = 0; i < purchaseProducts.getDocuments().size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("followProductId_s:");
			buffer.append(SolrUtil.escape(purchaseProducts.getDocuments(
					).get(i).getProduct().getSku()));
		}
		return createDistinctCommunityUsers(buffer.toString(), limit, offset);
	}

	/**
	 * コミュニティユーザーIDからフォロー商品を検索して返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return フォロー商品のリスト
	 */
	@Override
	public SearchResult<ProductDO> findFollowProductByCommunityUserId(
			String communityUserId, int limit, int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("communityUserId_s:" + SolrUtil.escape(communityUserId));
		AdultHelper adultHelper = new AdultHelper(requestScopeDao.loadAdultVerification());
		SolrQuery query = new SolrQuery(
					adultHelper.toFilterQuery(buffer.toString()));
		query.setRows(limit);
		query.setStart(offset);
		query.setSortField("followDate_dt", ORDER.asc);
		SearchResult<ProductFollowDO> searchResult = new SearchResult<ProductFollowDO>(
				solrOperations.findByQuery(query, ProductFollowDO.class));

		SearchResult<ProductDO> result = new SearchResult<ProductDO>();
		result.setNumFound(searchResult.getNumFound());
		for (ProductFollowDO follow : searchResult.getDocuments()) {
			result.getDocuments().add(follow.getFollowProduct());
		}
		return result;
	}

	/**
	 * 商品のフォロー情報マップを返します。
	 * @param communityUserId 対象となるコミュニティユーザーID
	 * @param skus SKUリスト
	 * @return 商品のフォロー情報マップ
	 */
	@Override
	public Map<String, Boolean> loadProductFollowMap(
			String communityUserId, List<String> skus) {
		Map<String, Boolean> resultMap = new HashMap<String, Boolean>();
		if (StringUtils.isEmpty(communityUserId) || skus == null || skus.size() == 0) {
			return resultMap;
		}
		Set<String> productFolowIds = new HashSet<String>();
		for (String sku : skus) {
			productFolowIds.add(IdUtil.createIdByConcatIds(communityUserId, sku));
		}
		Map<String, ProductFollowDO> map = hBaseOperations.find(
				ProductFollowDO.class, String.class, productFolowIds,
				Path.includeProp("productFollowId"));
		for (String sku : skus) {
			resultMap.put(sku, map.containsKey(
					IdUtil.createIdByConcatIds(communityUserId, sku)));
		}
		return resultMap;
	}

	/**
	 * 重複しないコミュニティユーザーのリストを返します。
	 * @param query レビュー検索クエリ
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
		SearchResult<ProductFollowDO> follows = new SearchResult<ProductFollowDO>(
				solrOperations.findByQuery(
				new SolrQuery(adultHelper.toFilterQuery(query)).setRows(
						SolrConstants.QUERY_ROW_LIMIT).setStart(
										0).addSortField("followDate_dt", ORDER.desc),
										ProductFollowDO.class, Path.includeProp(
												"*").includePath("communityUser.communityUserId").depth(1)));
		if (adultHelper.isRequireCheckAdult()) {
			result.setHasAdult(
					adultHelper.hasAdult(
							query, ProductFollowDO.class, solrOperations));
		}
		if (follows.getNumFound() == 0) {
			return result;
		}

		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds( follows.getDocuments());
		List<String> communityUserIds = new ArrayList<String>();
		List<String> communityUserIdAll = new ArrayList<String>();
		String communityUserId = requestScopeDao.loadCommunityUserId();
		for (ProductFollowDO follow : follows.getDocuments()) {
			// 一時停止対応
			if (follow.isStop(communityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				continue;
			}

			if (!communityUserIdAll.contains(
					follow.getCommunityUser().getCommunityUserId())) {
				communityUserIdAll.add(
						follow.getCommunityUser().getCommunityUserId());
				if (communityUserIdAll.size() > offset
						&& communityUserIdAll.size() <= (offset + limit)) {
					communityUserIds.add(
							follow.getCommunityUser().getCommunityUserId());
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
	 * 商品フォロー情報IDを生成して返します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followProductId フォローする商品ID
	 * @return 商品フォロー情報ID
	 */
	private String createProductFollowId(String communityUserId, String followProductId) {
		return IdUtil.createIdByConcatIds(communityUserId, followProductId);
	}

	@Override
	public long countFollowProduct(String communityUserId) {
		StringBuilder sb = new StringBuilder();

		sb.append("communityUserId_s:");
		sb.append(SolrUtil.escape(communityUserId));
		sb.append(" AND ownerStop_b:false");

		SearchResult<ProductFollowDO> searchResult = new SearchResult<ProductFollowDO>(
				solrOperations.findByQuery(new SolrQuery(sb.toString()), ProductFollowDO.class));

		return searchResult.getNumFound();

	}

}
