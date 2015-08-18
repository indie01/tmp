/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FilterList.Operator;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.lib.core.utils.Asserts;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.UpdateColumns;
import com.kickmogu.lib.solr.FacetResult;
import com.kickmogu.lib.solr.SolrConstants;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.common.utils.BeanUtil;
import com.kickmogu.yodobashi.community.resource.cache.CacheStrategyType;
import com.kickmogu.yodobashi.community.resource.cache.MethodCache;
import com.kickmogu.yodobashi.community.resource.cache.TargetSystemType;
import com.kickmogu.yodobashi.community.resource.dao.InformationDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.ReviewDao;
import com.kickmogu.yodobashi.community.resource.dao.SimplePmsDao;
import com.kickmogu.yodobashi.community.resource.dao.util.AdultHelper;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.ProductUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.SolrUtil;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.DecisivePurchaseDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseLostProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDecisivePurchaseDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.SpamReportDO;
import com.kickmogu.yodobashi.community.resource.domain.UsedProductDO;
import com.kickmogu.yodobashi.community.resource.domain.VotingDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.AlsoBuyProduct;
import com.kickmogu.yodobashi.community.resource.domain.constants.CancelPointGrantType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.EditorVersions;
import com.kickmogu.yodobashi.community.resource.domain.constants.ProductSatisfaction;
import com.kickmogu.yodobashi.community.resource.domain.constants.PurchaseHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewSortType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.Verification;


/**
 * レビュー DAO の実装です。
 * @author kamiike
 *
 */
@Service
public class ReviewDaoImpl implements ReviewDao {
	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ReviewDaoImpl.class);

	/**
	 * HBaseアクセサです。
	 */
	@Autowired  @Qualifier("default")
	protected HBaseOperations hBaseOperations;

	/**
	 * Solrアクセサです。
	 */
	@Autowired @Qualifier("default")
	protected SolrOperations solrOperations;

	/**
	 * タイムスタンプホルダーです。
	 */
	@Autowired
	protected TimestampHolder timestampHolder;

	/**
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	protected RequestScopeDao requestScopeDao;

	/**
	 * 商品 DAO です。
	 */
	@Autowired @Qualifier("catalog")
	protected ProductDao productDao;

	@Autowired
	protected InformationDao informationDao;
	
	@Autowired @Qualifier("pms")
	protected SimplePmsDao simplePmsDao;
	
	/**
	 * 指定した条件のレビュー情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @return レビュー情報リスト
	 */
	@Override
	public List<ReviewDO> findReviewByCommunityUserIdAndSKU(
			String communityUserId, String sku) {
		return findReviewByCommunityUserIdAndSKU(communityUserId, sku, getDefaultLoadReviewCondition());
	}

	/**
	 * 指定した条件のレビュー情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @param condition 条件
	 * @return レビュー情報リスト
	 */
	@Override
	public List<ReviewDO> findReviewByCommunityUserIdAndSKU(
			String communityUserId, String sku, Condition condition) {
		List<ReviewDO> result = new ArrayList<ReviewDO>();
		// SolrからReviewIdを元にデータを取得する。
		// Solrにデータがなお場合はSkipする（不整合を起こさないため）。
		StringBuilder buffer = new StringBuilder();
		buffer.append("communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND productId_s:");
		buffer.append(SolrUtil.escape(sku));
		
		SearchResult<ReviewDO> findSolrReviews = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(
						new SolrQuery(buffer.toString()), 
						ReviewDO.class)
				);
		
		if( findSolrReviews.getDocuments().isEmpty() )
			return result;
		
		Set<String> reviewIds = new HashSet<String>();
		
		for(ReviewDO checkReview : findSolrReviews.getDocuments()){
			reviewIds.add(checkReview.getReviewId());
		}
		
		// HBaseからデータを取得する。
		Map<String, ReviewDO> reviewMap = hBaseOperations.find(ReviewDO.class, String.class, reviewIds, condition);
		
		if( !reviewMap.isEmpty() ){
			Iterator<Entry<String, ReviewDO>> reviewInterator = reviewMap.entrySet().iterator();
			Entry<String, ReviewDO> entry = null;
			ReviewDO review = null;
			while( reviewInterator.hasNext() ){
				entry = reviewInterator.next();
				review = entry.getValue();
				fillRelationInfo(review);
				result.add(review);
			}
		}
		
		ProductUtil.filterInvalidProduct(result);
		return result;
	}
	
	@Override
	public List<ReviewDO> findReviewByCommunityUserIdAndSKU(
			String communityUserId,
			String sku,
			ReviewType reviewType,
			ContentsStatus status) {
		return findReviewByCommunityUserIdAndSKU(
				communityUserId,
				sku,
				reviewType,
				status,
				getDefaultLoadReviewCondition());
	}
	
	@Override
	public List<ReviewDO> findReviewByCommunityUserIdAndSKU(
			String communityUserId,
			String sku,
			ReviewType reviewType,
			ContentsStatus status,
			Condition condition) {
		List<ReviewDO> result = new ArrayList<ReviewDO>();
		// SolrからReviewIdを元にデータを取得する。
		// Solrにデータがなお場合はSkipする（不整合を起こさないため）。
		StringBuilder buffer = new StringBuilder();
		buffer.append("communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND productId_s:");
		buffer.append(SolrUtil.escape(sku));
		buffer.append(" AND reviewType_s:");
		buffer.append(SolrUtil.escape(reviewType.getCode()));
		buffer.append(" AND status_s:");
		buffer.append(SolrUtil.escape(status.getCode()));
		
		SearchResult<ReviewDO> findSolrReviews = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(
						new SolrQuery(buffer.toString()), 
						ReviewDO.class)
				);
		
		if( findSolrReviews.getDocuments().isEmpty() )
			return result;
		
		Set<String> reviewIds = new HashSet<String>();
		
		for(ReviewDO checkReview : findSolrReviews.getDocuments()){
			reviewIds.add(checkReview.getReviewId());
		}
		
		// HBaseからデータを取得する。
		Map<String, ReviewDO> reviewMap = hBaseOperations.find(ReviewDO.class, String.class, reviewIds, condition);
		
		if( !reviewMap.isEmpty() ){
			Iterator<Entry<String, ReviewDO>> reviewInterator = reviewMap.entrySet().iterator();
			Entry<String, ReviewDO> entry = null;
			ReviewDO review = null;
			while( reviewInterator.hasNext() ){
				entry = reviewInterator.next();
				review = entry.getValue();
				fillRelationInfo(review);
				result.add(review);
			}
		}
		
		ProductUtil.filterInvalidProduct(result);
		return result;
	}

	/**
	 * 指定したレビュー情報を返します。
	 * @param reviewId レビューID
	 * @return レビュー情報
	 */
	@Override
	public ReviewDO loadReview(String reviewId) {
		return loadReview(reviewId, getDefaultLoadReviewCondition(), false);
	}

	/**
	 * 指定したレビュー情報をインデックス情報から返します。
	 * @param reviewId レビューID
	 * @return レビュー情報
	 */
	@Override
	public ReviewDO loadReviewFromIndex(String reviewId) {
		return loadReviewFromIndex(reviewId, true);
	}

	/**
	 * 指定したレビュー情報をインデックス情報から返します。
	 * @param reviewId レビューID
	 * @return レビュー情報
	 */
	@Override
	public ReviewDO loadReviewFromIndex(String reviewId, boolean fill) {
		return loadReviewFromIndex(reviewId, fill, true);
	}

	@Override
	public ReviewDO loadReviewFromIndex(String reviewId, boolean fill,boolean includeDeleteContents) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("reviewId:");
		buffer.append(reviewId);
		if(!includeDeleteContents){
			buffer.append(" AND !status_s:");
			buffer.append(ContentsStatus.DELETE.getCode());
			buffer.append(" AND withdraw_b:false");
		}
		SearchResult<ReviewDO> results = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(
						new SolrQuery(buffer.toString()),
						ReviewDO.class,
						Path.includeProp("*").includePath(
								"communityUser.communityUserId,product.sku," +
								"reviewDecisivePurchases.decisivePurchase.decisivePurchaseId").depth(2)));
		ProductUtil.filterInvalidProduct(results);
		if(results == null || results.getDocuments().isEmpty() || results.getDocuments().size() > 1)
			return null;

		ReviewDO review = results.getDocuments().get(0);

		if(fill)
			fillRelationInfo(review);
		return review;

	}



	/**
	 * 指定したレビュー情報を返します。
	 * @param reviewId レビューID
	 * @param condition 条件
	 * @param withLock ロックを取得するかどうか
	 * @return レビュー情報
	 */
	@Override
	public ReviewDO loadReview(String reviewId, Condition condition, boolean withLock) {
		ReviewDO review = null;
		if (withLock) {
			review = hBaseOperations.loadWithLock(ReviewDO.class, reviewId, condition);
		} else {
			review = hBaseOperations.load(ReviewDO.class, reviewId, condition);
		}
		fillRelationInfo(review);
		return review;
	}

	/**
	 * 指定した条件のレビューにポイント付与可能か厳格にチェックします。
	 * @param communityUserId コミュニティユーザーID
	 * @param product 商品情報
	 * @param reviewTerm レビュー期間
	 * @return 有効な場合、true
	 */
	@Override
	public boolean isStrictPointGrantReview(
			String communityUserId, ProductDO product, int reviewTerm) {
		for (ReviewDO review : findReviewByCommunityUserIdAndSKU(
				communityUserId,
				product.getSku(),
				Path.includeProp(
				"reviewType,productId,pointBaseDate,purchaseDate,effective,status,purchaseHistoryType").includePath("product.sku").depth(1))) {
			if (review.isEffective()) {
				if (reviewTerm == product.getGrantPointReviewTerm(
						review.getPurchaseDate(),
						review.getPointBaseDate())) {
					return false;
				}
			}
			// 他社購入からヨドバシ購入になった場合、レビュー期間で、レビューがあっても他者購入でポイントが付いていなければ、ポイント付与を許可する。
			if (reviewTerm == 0 && 
					ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE.equals(review.getReviewType()) && 
					!ContentsStatus.DELETE.equals(review.getStatus()) &&
					PurchaseHistoryType.YODOBASHI.equals(review.getPurchaseHistoryType())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 指定した条件のレビューにポイント付与可能か緩くチェックします。
	 * @param communityUserId コミュニティユーザーID
	 * @param product 商品情報
	 * @param reviewTerm レビュー期間
	 * @return 有効な場合、true
	 */
	@Override
	public boolean isLeniencePointGrantReview(
			String communityUserId, ProductDO product, int reviewTerm) {
		//削除済みデータも対象に検索する
		if (reviewTerm == 0) {
			//購入直後に関しては、2 件目であれば、期間内かつ有効レビューが無くてもポイントは付かない。
			//自己申請の購入直後レビュー投稿後、ヨドバシで購入し、購入直後期間に経過レビューを書いた場合、
			//ポイントは付かない。そのため有効フラグの有無を購入直後期間の場合条件から外す
			return solrOperations.count(new SolrQuery(
					"communityUserId_s:" + SolrUtil.escape(communityUserId) + " AND productId_s:" + SolrUtil.escape(product.getSku())
					+ " AND !(status_s:" + ContentsStatus.SAVE.getCode() + " OR status_s:" + ContentsStatus.DELETE.getCode() + ") AND  "
					+ ReviewDO.getReviewTermQuery(product).get(reviewTerm)),
					ReviewDO.class) == 0;
		} else {
			return solrOperations.count(new SolrQuery(
					"communityUserId_s:" + SolrUtil.escape(communityUserId) + " AND productId_s:" + SolrUtil.escape(product.getSku())
					+ " AND !(status_s:" + ContentsStatus.SAVE.getCode()  + " OR status_s:" + ContentsStatus.DELETE.getCode() + ") AND effective_b:true AND "
					+ ReviewDO.getReviewTermQuery(product).get(reviewTerm)),
					ReviewDO.class) == 0;
		}
	}

	/**
	 * 指定した条件のレビューにポイント付与可能か緩くチェックします。
	 * @param communityUserId コミュニティユーザーID
	 * @param product 商品情報
	 * @param reviewTerm レビュー期間
	 * @return 有効な場合、true
	 */
	@Override
	public Map<String, Boolean> isLeniencePointGrantReviews(List<Map<String, Object>> leniencePointGrantReviewInputList) {
		Map<String, Boolean> resultMap = new HashMap<String, Boolean>(); 
		for(Map<String, Object> params:leniencePointGrantReviewInputList) {
			String communityUserId = (String)params.get("communityUserId");
			ProductDO product = (ProductDO)params.get("product");
			int reviewTerm = Integer.parseInt(params.get("reviewTerm").toString());
			resultMap.put(IdUtil.createIdByConcatIds(
					communityUserId, product.getSku(), String.valueOf(reviewTerm)), isStrictPointGrantReview(communityUserId, product, reviewTerm));
		}
		return resultMap;
	}
	
	
	/**
	 * レビュー情報を保存します。
	 * @param review レビュー
	 * @return レビューID
	 */
	@Override
	public String saveReview(ReviewDO review) {
		Condition updateCondition = null;
		if (StringUtils.isNotEmpty(review.getReviewId())) {
			updateCondition = Path.includeProp("*").excludeProp("pointBaseDate");
		} else {
			updateCondition = Path.DEFAULT;
		}
		// 満足度の初期値の設定
		if (review.getProductSatisfaction() == null) {
			review.setProductSatisfaction(ProductSatisfaction.NONE);
		}
		// 「次も買いますか」初期値の設定
		if (ReviewType.REVIEW_AFTER_FEW_DAYS.equals(review.getReviewType())) {
			if (review.getAlsoBuyProduct() == null) {
				review.setAlsoBuyProduct(AlsoBuyProduct.NONE);
			}
		}
		// エディターバージョンの設定
		review.setEditorVersion(EditorVersions.TEXT_EDITOR);
		review.setSaveDate(timestampHolder.getTimestamp());
		review.setModifyDateTime(timestampHolder.getTimestamp());

		hBaseOperations.save(review, updateCondition);

		Map<String, ProductDO> productMap = new HashMap<String, ProductDO>();
		updateReviewDecisivePurchase(review);
		updatePurchaseLostProduct(review, productMap);
		updateUsedProduct(review, productMap);
		
		// レビュー履歴保存処理
		if (ContentsStatus.SUBMITTED.equals(review.getStatus())) {
			ReviewHistoryDO history = new ReviewHistoryDO();
			BeanUtil.copyProperties(review, history, new String[]{"reviewDecisivePurchases", "purchaseLostProducts", "usedProducts"});
			hBaseOperations.save(history);

			for (int i = 0; i < review.getReviewDecisivePurchases().size(); i++) {
				ReviewDecisivePurchaseDO src = review.getReviewDecisivePurchases().get(i);
				ReviewDecisivePurchaseDO dest = new ReviewDecisivePurchaseDO();
				dest.setDecisivePurchase(src.getDecisivePurchase());
				dest.setSku(src.getSku());
				dest.setReviewHistory(history);
				dest.setCommunityUser(src.getCommunityUser());
				dest.setEffective(src.isEffective());
				dest.setPurchaseDate(src.getPurchaseDate());
				dest.setReviewDecisivePurchaseId(
						IdUtil.createIdByBranchNo(history.getReviewHistoryId(), i));
				dest.setRegisterDateTime(timestampHolder.getTimestamp());
				dest.setModifyDateTime(timestampHolder.getTimestamp());
				history.getReviewDecisivePurchases().add(dest);
			}
			hBaseOperations.save(ReviewDecisivePurchaseDO.class, history.getReviewDecisivePurchases());

			for (int i = 0; i < review.getPurchaseLostProducts().size(); i++) {
				PurchaseLostProductDO src = review.getPurchaseLostProducts().get(i);
				PurchaseLostProductDO dest = new PurchaseLostProductDO();
				dest.setCommunityUser(src.getCommunityUser());
				dest.setProduct(src.getProduct());
				dest.setProductName(src.getProductName());
				dest.setReviewHistory(history);
				dest.setCommunityUser(src.getCommunityUser());
				dest.setEffective(src.isEffective());
				dest.setReviewProductId(src.getReviewProductId());
				dest.setAdult(src.isAdult());
				dest.setPurchaseLostProductId(
						IdUtil.createIdByBranchNo(history.getReviewHistoryId(), i));
				dest.setRegisterDateTime(timestampHolder.getTimestamp());
				dest.setModifyDateTime(timestampHolder.getTimestamp());
				history.getPurchaseLostProducts().add(dest);
			}
			hBaseOperations.save(PurchaseLostProductDO.class, history.getPurchaseLostProducts());

			for (int i = 0; i < review.getUsedProducts().size(); i++) {
				UsedProductDO src = review.getUsedProducts().get(i);
				UsedProductDO dest = new UsedProductDO();
				dest.setCommunityUser(src.getCommunityUser());
				dest.setProduct(src.getProduct());
				dest.setProductName(src.getProductName());
				dest.setReviewHistory(history);
				dest.setCommunityUser(src.getCommunityUser());
				dest.setEffective(src.isEffective());
				dest.setReviewProductId(src.getReviewProductId());
				dest.setAdult(src.isAdult());
				dest.setUsedProductId(
						IdUtil.createIdByBranchNo(history.getReviewHistoryId(), i));
				dest.setRegisterDateTime(timestampHolder.getTimestamp());
				dest.setModifyDateTime(timestampHolder.getTimestamp());
				history.getUsedProducts().add(dest);
			}
			hBaseOperations.save(UsedProductDO.class, history.getUsedProducts());
			return history.getReviewHistoryId();
		}
		
		return null;
	}
	
	void debugPrint(String msg) {
		LOG.debug(msg);
	}

	/**
	 * レビュー情報のインデックスを更新します。
	 * @param reviewId レビューID
	 * @return レビュー情報
	 */
	@Override
	public ReviewDO updateReviewInIndex(String reviewId) {
		return updateReviewInIndex(reviewId, false);
	}
	
	@Override
	public ReviewDO updateReviewInIndexForMR(String reviewId) {
		// loadReviewでproductとの関連取得をはずす
		// PurchaseLostProductDO,UsedProductDO
		// が関連しているProductDOが取得されないが、updateReviewInIndex（）では使用していないため、取得する必要なし
		// ※updateReviewInIndex()ではsave()しているが、ProductDOはHBaseが持つ情報ではない（CatalogもしくはDBProeuctDetailDO）
		
		Condition path = Path.includeProp("*").includePath(
				"communityUser.communityUserId,product.sku," +
				"reviewDecisivePurchases.decisivePurchase.decisivePurchaseId," +
				"purchaseLostProducts.product.sku," +
				"usedProducts.product.sku,mngToolOperation").excludeRelation(ProductDO.class).depth(2);
		ReviewDO review = loadReview(reviewId, path, false);
		
		return updateReviewInIndex(reviewId, review, false);
	}

	protected String debugPrintReview(String msg, ReviewDO review) {
		{
			debugPrint(msg);
			StringBuffer sb = new StringBuffer();
			
			sb.append("review {communityUserId:" + review.getCommunityUser().getCommunityUserId() + 
					" sku:" + review.getProduct().getSku() + 
					" mngToolOperation:" + review.isMngToolOperation() + "}");
			sb.append("\r\n");
			
			sb.append("       getReviewDecisivePurchases[");
			for (ReviewDecisivePurchaseDO child:review.getReviewDecisivePurchases()) {
				sb.append("{id:" + child.getReviewDecisivePurchaseId() + 
						" sku:" + child.getSku() + "} ");
			}
			sb.append("]");
			sb.append("\r\n");
			//debugPrint(sb.toString());
			
			//sb = new StringBuffer();
			sb.append("       getPurchaseLostProducts[");
			for (PurchaseLostProductDO child:review.getPurchaseLostProducts()) {
				sb.append("{id:" + child.getPurchaseLostProductId() + 
				//		" product:" + child.getProduct() +
						" productName:" + child.getProductName() + "} ");
			}
			sb.append("]");
			sb.append("\r\n");
			//debugPrint(sb.toString());
			
			//sb = new StringBuffer();
			sb.append("       getUsedProducts[");
			for (UsedProductDO child:review.getUsedProducts()) {
				sb.append("{id:" + child.getUsedProductId() + 
				//		" product:" + child.getProduct() +
						" productName:" + child.getProductName() + "} ");
			}
			sb.append("]");
			debugPrint(sb.toString());
			return sb.toString();
		}
	}
	@Override
	public ReviewDO updateReviewInIndex(String reviewId, boolean mngToolOperation) {
		ReviewDO review = loadReview(reviewId);
		return updateReviewInIndex(reviewId, review, mngToolOperation);
	}
	
	/**
	 * レビュー履歴情報のインデックスを更新します。
	 * @param reviewHistoryId レビュー履歴ID
	 */
	@Override
	public void updateReviewHistoryInIndex(String reviewHistoryId) {
		Condition condition = Path.includeProp("*").includeRelation(
				ReviewDecisivePurchaseDO.class,
				PurchaseLostProductDO.class,
				UsedProductDO.class).depth(1);
		if( StringUtils.isBlank(reviewHistoryId))
			return;
		
		ReviewHistoryDO history = hBaseOperations.load(ReviewHistoryDO.class,
				reviewHistoryId, condition);
		if (history != null) {
			 solrOperations.save(history, condition);
		}
	}

	/**
	 * 指定したレビューを削除します。
	 * @param reviewId レビューID
	 * @param logical 論理削除かどうか
	 * @param cancelPointGrantType ポイント申請キャンセル理由
	 */
	@Override
	public void deleteReview(
			String reviewId,
			boolean effective,
			boolean logical,
			CancelPointGrantType cancelPointGrantType,
			boolean mngToolOperation) {
		if (logical) {
			ReviewDO review = new ReviewDO();
			review.setReviewId(reviewId);
			review.setEffective(effective);
			review.setStatus(ContentsStatus.DELETE);
			review.setDeleteDate(timestampHolder.getTimestamp());
			review.setModifyDateTime(timestampHolder.getTimestamp());
			review.setMngToolOperation(mngToolOperation);
			review.setLatestReview(false);
			StringBuilder update = new StringBuilder();
			update.append("effective,status,deleteDate,modifyDateTime,mngToolOperation,latestReview");
			if (cancelPointGrantType != null) {
				review.setCancelPointGrantType(cancelPointGrantType);
				update.append(",cancelPointGrantType");
			}

			//関連情報を削除
			//ActionHistoryDO
			hBaseOperations.scanUpdateWithIndex(
					ActionHistoryDO.class, "reviewId", reviewId,
					UpdateColumns.set("deleteFlag", true
							).andSet("deleteDate", timestampHolder.getTimestamp())
							.andSet("modifyDateTime", timestampHolder.getTimestamp()));
			//InformationDO
			hBaseOperations.scanUpdateWithIndex(
					InformationDO.class, "reviewId", reviewId,
					UpdateColumns.set("deleteFlag", true
							).andSet("deleteDate", timestampHolder.getTimestamp())
							.andSet("modifyDateTime", timestampHolder.getTimestamp()));

			//CommentDO
			hBaseOperations.scanUpdateWithIndex(
					CommentDO.class, "reviewId", reviewId,
					UpdateColumns.set("deleteFlag", true
							).andSet("deleteDate", timestampHolder.getTimestamp())
							.andSet("modifyDateTime", timestampHolder.getTimestamp()));

			//SpamReportDO
			hBaseOperations.scanUpdateWithIndex(
					SpamReportDO.class, "reviewId", reviewId,
					UpdateColumns.set("status", SpamReportStatus.DELETE
							).andSet("deleteDate", timestampHolder.getTimestamp())
							.andSet("modifyDateTime", timestampHolder.getTimestamp()));

			//LikeDO
			hBaseOperations.scanDeleteWithIndex(
					LikeDO.class, "reviewId", reviewId);
			//VotingDO
			hBaseOperations.scanDeleteWithIndex(
					VotingDO.class, "reviewId", reviewId);
			//ReviewDecisivePurchaseDO
			hBaseOperations.scanUpdateWithIndex(
					ReviewDecisivePurchaseDO.class, "reviewId", reviewId,
					UpdateColumns.set("deleteFlag", true));

			//PurchaseLostProductDO
			hBaseOperations.scanUpdateWithIndex(
					PurchaseLostProductDO.class, "reviewId", reviewId,
					UpdateColumns.set("deleteFlag", true));

			//UsedProductDO
			hBaseOperations.scanUpdateWithIndex(
					UsedProductDO.class, "reviewId", reviewId,
					UpdateColumns.set("deleteFlag", true));

			hBaseOperations.save(review, Path.includeProp(update.toString()));
		} else {
			hBaseOperations.deleteByKey(ReviewDO.class, reviewId,
					Path.includeRelation(ReviewDecisivePurchaseDO.class,
							PurchaseLostProductDO.class, UsedProductDO.class).depth(1));
		}
	}

	/**
	 * 指定した商品にレビューを書いている人を返します。
	 * @param sku SKU
	 * @param excludeReviewId 対象から外すレビューID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return レビューを書いているコミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findReviwerExcludeReviewIdBySKU(
			String sku, String excludeReviewId,
			ContentsStatus[] statuses,
			int limit, int offset) {
		StringBuilder sb = new StringBuilder();

		sb.append("productId_s:");
		sb.append(SolrUtil.escape(sku));
		sb.append(" AND withdraw_b:false");

		sb.append(" AND (");
		boolean isFirst = true;
		for(ContentsStatus status:statuses){
			if(!isFirst) sb.append(" OR ");
			sb.append(" status_s:");
			sb.append(status.getCode());
			isFirst = false;
		}
		sb.append(" ) ");
		if(StringUtils.isNotEmpty(excludeReviewId)){
			sb.append(" AND !reviewId:");
			sb.append(SolrUtil.escape(excludeReviewId));
		}
		//メール送信対象者取得のためのインターフェースなので、一時停止チェックは
		//適用しない
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(
				new SolrQuery(sb.toString()).setRows(
								limit).setStart(offset).addSortField("postDate_dt", ORDER.asc),
								ReviewDO.class, Path.includeProp("*").includePath(
										"communityUser.communityUserId").depth(1)));
		SearchResult<CommunityUserDO> result = new SearchResult<CommunityUserDO>(
				searchResult.getNumFound(), new ArrayList<CommunityUserDO>());
		for (ReviewDO review : searchResult.getDocuments()) {
			result.getDocuments().add(review.getCommunityUser());
		}
		return result;
	}

	/**
	 * 指定した商品にレビューを書いている人を重複を除いて返します。
	 * @param sku SKU
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return レビューを書いているコミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findDistinctReviwerExcludeCommunityUserIdBySKU(
			String sku, String excludeCommunityUserId, int limit, int offset) {
		return findDistinctReviwerExcludeCommunityUserIdBySKU(
				sku, excludeCommunityUserId, limit, offset, false);
	}
	@Override
	public SearchResult<CommunityUserDO> findDistinctReviwerExcludeCommunityUserIdBySKU(
			String sku, String excludeCommunityUserId, int limit, int offset, boolean excludeProduct) {
		//重複除去と投稿順の並び替えを同時に満たすため、solr ではサブクエリ
		//とdistinctをサポートしていないので、上限数を絞り、java 側で処理します。
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND productId_s:");
		buffer.append(SolrUtil.escape(sku));
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		if (excludeCommunityUserId != null) {
			buffer.append(" AND !communityUserId_s:");
			buffer.append(SolrUtil.escape(excludeCommunityUserId));
		}
		return createDistinctCommunityUsers(buffer.toString(), limit, offset, excludeProduct);
	}

	/**
	 * 指定した商品にレビューを書いている人を重複を除いて返します。
	 * @param skus SKUリスト
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return レビューを書いているコミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findDistinctReviwerBySKU(
			List<String> skus, int limit, int offset) {
		//重複除去と投稿順の並び替えを同時に満たすため、solr ではサブクエリ
		//とdistinctをサポートしていないので、上限数を絞り、java 側で処理します。
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		buffer.append(" AND (");
		for (int i = 0; i < skus.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("productId_s:");
			buffer.append(SolrUtil.escape(skus.get(i)));
		}
		buffer.append(")");
		return createDistinctCommunityUsers(buffer.toString(), limit, offset);
	}

	/**
	 * フォローした商品にレビューを書いている人を重複を除いて返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return レビューを書いているコミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findDistinctReviwerByFollowProduct(
			String communityUserId, int limit, int offset) {
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
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
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
	 * 購入した商品にレビューを書いている人を重複を除いて返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param publicSetting 非公開情報を表示する場合はtrue
	 * @return レビューを書いているコミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findDistinctReviwerByPurchaseProduct(
			String communityUserId, int limit, int offset, boolean publicSetting) {
		//重複除去と投稿順の並び替えを同時に満たすため、solr ではサブクエリ
		//とdistinctをサポートしていないので、上限数を絞り、java 側で処理します。
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND purchaseDate_dt:" + SolrConstants.QUERY_DATE_TO_NOW);
		if (false == publicSetting) {
			buffer.append(" AND publicSetting_b:" + true);
		}

		SearchResult<PurchaseProductDO> purchaseProducts
				= new SearchResult<PurchaseProductDO>(solrOperations.findByQuery(
				new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT),
						PurchaseProductDO.class, Path.includeProp("productId")));
		ProductUtil.filterInvalidProduct(purchaseProducts);
		if (purchaseProducts.getNumFound() == 0) {
			return new SearchResult<CommunityUserDO>();
		}
		buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		buffer.append(" AND (");
		for (int i = 0; i < purchaseProducts.getDocuments().size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("productId_s:");
			buffer.append(SolrUtil.escape(purchaseProducts.getDocuments().get(i).getProduct().getSku()));
		}
		buffer.append(")");
		return createDistinctCommunityUsers(buffer.toString(), limit, offset);
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
		return createDistinctCommunityUsers(query, limit, offset, false);
	}
	private SearchResult<CommunityUserDO> createDistinctCommunityUsers(
			String query,
			int limit,
			int offset,
			boolean excludeProduct) {

		SearchResult<CommunityUserDO> result = new SearchResult<CommunityUserDO>();
		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());
		
		
		String includePath = "product.sku,communityUser.communityUserId";
		if(excludeProduct) includePath = "communityUser.communityUserId";
		SearchResult<ReviewDO> reviews = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(
				new SolrQuery(adultHelper.toFilterQuery(query)).setRows(
								SolrConstants.QUERY_ROW_LIMIT).setStart(
										0).addSortField("postDate_dt", ORDER.desc),
								ReviewDO.class, Path.includeProp("status").includePath(includePath).depth(1)));
		if (adultHelper.isRequireCheckAdult()) {
			result.setHasAdult(
					adultHelper.hasAdult(query, ReviewDO.class, solrOperations));
		}
		
		if(!excludeProduct)
			ProductUtil.filterInvalidProduct(reviews);

		if (reviews.getNumFound() == 0) {
			return result;
		}
		List<String> communityUserIds = new ArrayList<String>();
		List<String> communityUserIdAll = new ArrayList<String>();
		String communityUserId = requestScopeDao.loadCommunityUserId();
		for (ReviewDO review : reviews.getDocuments()) {
			// コンテンツの一時停止対応
			if(!review.getCommunityUser().getCommunityUserId().equals(communityUserId) && review.getStatus().equals(ContentsStatus.CONTENTS_STOP)) {
				continue;
			}
			if (!communityUserIdAll.contains(
					review.getCommunityUser().getCommunityUserId())) {
				communityUserIdAll.add(
						review.getCommunityUser().getCommunityUserId());
				if (communityUserIdAll.size() > offset
						&& communityUserIdAll.size() <= (offset + limit)) {
					communityUserIds.add(
							review.getCommunityUser().getCommunityUserId());
				}
			}
		}
		result.setNumFound(communityUserIdAll.size());
		if(! communityUserIds.isEmpty()){
			Map<String, CommunityUserDO> resultMap = solrOperations.find(CommunityUserDO.class, String.class, communityUserIds);
			for (String target : communityUserIds) {
				if (resultMap.containsKey(target)) {
					result.getDocuments().add(resultMap.get(target));
				}
			}
		}
		return result;
	}
	
	/**
	 * レビューのスコア情報と閲覧数をインデックスも合わせて更新します。
	 * <p>Localの商品テーブル(DBProductDetail等）を参照するため、夜間バッチ(MapReduce)専用のメソッドです。</p>
	 * @param reviewId レビューID
	 * @param score スコア
	 * @param viewCount UU閲覧数
	 */
	private BulkUpdate<ReviewDO> bulkUpdate = null;
	@Override
	public void updateReviewScoreAndViewCountWithIndexForBatch(ReviewDO review) {
// 呼び出し側に移動
//		ReviewDO review = new ReviewDO();
//		review.setReviewId(reviewId);
//		review.setReviewScore(score);
//		review.setViewCount(viewCount);
//		review.setModifyDateTime(timestampHolder.getTimestamp());
		
		bulkUpdate.write(review);
	}
	@Override
	public void updateReviewScoreAndViewCountWithIndexForBatchBegin(int bulkSize) {
		bulkUpdate = new BulkUpdate<ReviewDO>(ReviewDO.class, 
				hBaseOperations, solrOperations,
				Path.includeProp("reviewScore,viewCount,modifyDateTime"), bulkSize);
	}
	@Override
	public void updateReviewScoreAndViewCountWithIndexForBatchEnd() {
		bulkUpdate.end();
	}

	/**
	 * 投稿済みレビュー投稿者リストを返します。
	 * @param sku SKU
	 * @return 投稿済みレビュー投稿者リスト
	 */
	@Override
	public Set<String> loadPostReviewerListBySku(String sku) {

		List<ReviewDO> reviews = hBaseOperations.scanWithIndex(
				ReviewDO.class, "productId", sku,
				hBaseOperations.createFilterBuilder(ReviewDO.class,Operator.MUST_PASS_ONE
				).includeColumnValues("status", ContentsStatus.SUBMITTED, ContentsStatus.CONTENTS_STOP).toFilter());
		Set<String> communityUserIds = new HashSet<String>();
		for (ReviewDO review : reviews) {
			communityUserIds.add(review.getCommunityUser().getCommunityUserId());
		}
		return communityUserIds;
	}

	/**
	 * 指定した商品、ユーザーが保持する有効なレビューリストを返します。
	 * @param sku SKU
	 * @param communityUserId コミュニティユーザーID
	 * @return 有効なレビューリスト
	 */
	@Override
	public List<ReviewDO> findEffectiveReviewList(
			String sku, String communityUserId) {
		return hBaseOperations.scanWithIndex(
				ReviewDO.class, "communityUserId", communityUserId,
				hBaseOperations.createFilterBuilder(ReviewDO.class
				).appendSingleColumnValueFilter("productId",
						CompareOp.EQUAL, sku).appendSingleColumnValueFilter(
								"effective",
								CompareOp.EQUAL, true).toFilter(),
								Path.includeProp("status,pointGrantRequestId"));
	}

	/**
	 * 有効なレビューをキャンセル更新します。
	 * @param reviews レビューリスト
	 */
	@Override
	public void updateCancelEffectiveWithIndex(List<ReviewDO> reviews) {
		for (ReviewDO review : reviews) {
			ReviewDO dbReview = loadReview(review.getReviewId());
			dbReview.setEffective(false);
			if (review.getCancelPointGrantType() != null) {
				dbReview.setCancelPointGrantType(
						review.getCancelPointGrantType());
			}
			saveReview(dbReview);
			updateReviewInIndex(dbReview.getReviewId(), dbReview);
		}
	}

	/**
	 * 指定された商品情報のレビュー集計情報をレビュータイプごとに返します。
	 * @param product 商品
	 * @return レビュー集計情報
	 */
	@Override
	public Map<String, Long> loadReviewSummaryByReviewType(List<String> skus) {
		Map<String, Long> result = new HashMap<String, Long>();
		Map<String, String> queryMap = ReviewDO.getReviewTypeQuery();
		if (queryMap.isEmpty()) {
			return result;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false");
		if( skus.size() == 1){
			buffer.append(" AND productId_s:");
			buffer.append(SolrUtil.escape(skus.get(0)));
		}else{
			buffer.append(" AND (");
			for(int i=0; i<skus.size(); i++){
				buffer.append("productId_s:");
				buffer.append(SolrUtil.escape(skus.get(i)));
				if( i != skus.size() - 1 )
					buffer.append(" OR ");
			}
			buffer.append(")");
		}
		
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");

		SolrQuery solrQuery = new SolrQuery(buffer.toString());
		solrQuery.setFacetLimit(queryMap.size());
		for (String query : queryMap.values()) {
			solrQuery.addFacetQuery(query);
		}
		
		Map<String, Long> subResult = new HashMap<String, Long>();
		for (FacetResult<String> facetResult : solrOperations.facet(ReviewDO.class, String.class, solrQuery)) {
			subResult.put(facetResult.getFacetQuery(), facetResult.getCount());
		}
		for (Entry<String, String> entry : queryMap.entrySet()) {
			result.put(entry.getKey(), subResult.get(entry.getValue()));
		}
		return result;
	}
	
	/**
	 * 指定された商品情報のレビュー集計情報を返します。
	 * @param product 商品
	 * @return レビュー集計情報
	 */
	@Override
	public Map<Integer, Long> loadReviewSummary(ProductDO product) {
		Map<Integer, Long> result = new HashMap<Integer, Long>();
		Map<Integer, String> queryMap = ReviewDO.getReviewTermQuery(product);
		if (queryMap.isEmpty()) {
			return result;
		}
		SolrQuery solrQuery = new SolrQuery(
				"withdraw_b:false AND productId_s:" + SolrUtil.escape(product.getSku()) + 
				" AND ( "
				+ "status_s:"
				+ ContentsStatus.SUBMITTED.getCode()
				+ " OR status_s:"
				+ ContentsStatus.CONTENTS_STOP.getCode()
				+ " ) ");
		solrQuery.setFacetLimit(queryMap.size());
		for (String query : queryMap.values()) {
			solrQuery.addFacetQuery(query);
		}

		Map<String, Long> subResult = new HashMap<String, Long>();
		for (FacetResult<String> facetResult : solrOperations.facet(
				ReviewDO.class, String.class, solrQuery)) {
			subResult.put(facetResult.getFacetQuery(), facetResult.getCount());
		}
		for (Entry<Integer, String> entry : queryMap.entrySet()) {
			result.put(entry.getKey(), subResult.get(entry.getValue()));
		}
		return result;
	}

	/**
	 * 商品に紐づく購入の決め手を評価の高い順に返します。<br />
	 * 購入の決め手IDの指定がある場合は、それらを含めて評価順にマージして
	 * 返します。<br />
	 * limit が 0 の場合、購入の決め手IDのリスト分だけ取得します。<br />
	 * 購入の決め手の評価数が 0 件のデータは返しません。
	 * @param product 商品
	 * @param decisivePurchaseIds 購入の決め手IDのリスト
	 * @param reviewTerm レビュー区間
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param statistics 統計情報かどうか
	 * @return 検索結果
	 */
	@Override
	public SearchResult<DecisivePurchaseDO> findDecisivePurchaseFromIndexBySKU(
			ProductDO product, List<String> decisivePurchaseIds, Integer reviewTerm,
			int limit, int offset) {
		
		CollectionUtils.filter(decisivePurchaseIds, new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				return object != null;
			}
		});
		
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND deleteFlag_b:false AND ");
		buffer.append("temporary_b:false");
		buffer.append("AND stopFlg_b:false");
		buffer.append(" AND reviewId_s:*");
		buffer.append(" AND sku_s:");
		buffer.append(SolrUtil.escape(product.getSku()));
		if (reviewTerm != null) {
			String condition = ReviewDO.getReviewTermQuery(product).get(reviewTerm);
			if (condition != null) {
				buffer.append(" AND ");
				buffer.append(condition);
			}
		}
		if (decisivePurchaseIds != null) {
			for (String decisivePurchaseId : decisivePurchaseIds) {
				buffer.append(" AND !decisivePurchaseId_s:" + SolrUtil.escape(decisivePurchaseId));
			}
		}
		SolrQuery query = new SolrQuery(buffer.toString());
		query.addFacetField("decisivePurchaseId_s");
		query.setFacetSort(FacetParams.FACET_SORT_COUNT);
		query.setFacetMinCount(1);
		query.setFacetLimit(SolrConstants.QUERY_ROW_LIMIT);
		List<FacetResult<String>> searchResult = solrOperations.facet(
					ReviewDecisivePurchaseDO.class, String.class, query);

		int totalCount = searchResult.size();
		List<FacetResult<String>> tmpSearchResult = new ArrayList<FacetResult<String>>();
		for (int i = offset; i < totalCount && i < (offset + limit); i++) {
			tmpSearchResult.add(searchResult.get(i));
		}

		searchResult = tmpSearchResult;
		List<String> reloadDecisivePurchaseIds = new ArrayList<String>();
		for (FacetResult<String> facetResult : searchResult) {
			reloadDecisivePurchaseIds.add(facetResult.getValue());
		}
		
		List<DecisivePurchaseDO> decisivePurchases = Lists.newArrayList();
		if (reloadDecisivePurchaseIds.size() > 0) {
			Map<String, DecisivePurchaseDO> resultMap = solrOperations.find(DecisivePurchaseDO.class,
					String.class, reloadDecisivePurchaseIds);
			for (FacetResult<String> facetResult : searchResult) {
				DecisivePurchaseDO decisivePurchase = resultMap.get(facetResult.getValue());
				if(decisivePurchase == null || decisivePurchase.isDeleteFlg()) continue;
				decisivePurchase.setRatings(facetResult.getCount());
				decisivePurchases.add(decisivePurchase);
			}
		}
		// 指定された購入の決め手ID一覧がSolrから検索できない場合に、HBaseから取得する。
		if (decisivePurchaseIds != null && !decisivePurchaseIds.isEmpty()) {
			reloadDecisivePurchaseIds = Lists.newArrayList();
			// HBaseから取り直し
			Map<String, DecisivePurchaseDO> resultMap = hBaseOperations.find(DecisivePurchaseDO.class, String.class, decisivePurchaseIds);
			Iterator<Entry<String, DecisivePurchaseDO>> entries = resultMap.entrySet().iterator();
			Entry<String, DecisivePurchaseDO> entry = null;
			while( entries.hasNext() ){
				entry = entries.next();
				decisivePurchases.add(0, entry.getValue());
				totalCount ++;
			}
		}
		if (decisivePurchases.size() > limit) {
			// Hbaseから取得した分でlimitを超過している場合は切り詰める
			decisivePurchases = decisivePurchases.subList(0, limit);
		}
		return new SearchResult<DecisivePurchaseDO>(totalCount, decisivePurchases);
	}

	/**
	 * 商品に紐づく購入の決め手を取得します。
	 * @param product　商品
	 * @param excludeDecisivePurchaseIdList　除外する「購入の決め手」のIDリスト
	 * @param limit　最大取得件数
	 * @param offset 検索開始位置
	 * @return
	 */
	@Override
	public SearchResult<DecisivePurchaseDO> findDecisivePurchaseFromIndexBySKU(
			ProductDO product, List<String> excludeDecisivePurchaseIdList, int limit, int offset) {
		
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND deleteFlag_b:false AND ");
		buffer.append("temporary_b:false");
		buffer.append("AND stopFlg_b:false");
		buffer.append(" AND reviewId_s:*");
		buffer.append(" AND sku_s:");
		buffer.append(SolrUtil.escape(product.getSku()));
		for(String decisivePurchaseId: excludeDecisivePurchaseIdList) {
			buffer.append(" AND !decisivePurchaseId_s:" + decisivePurchaseId);
		}
		
		SolrQuery query = new SolrQuery(buffer.toString());
		query.addFacetField("decisivePurchaseId_s");
		query.setFacetSort(FacetParams.FACET_SORT_COUNT);
		query.setFacetMinCount(1);
		query.setFacetLimit(SolrConstants.QUERY_ROW_LIMIT);
		List<FacetResult<String>> searchResult = solrOperations.facet(
					ReviewDecisivePurchaseDO.class, String.class, query);

		int totalCount = searchResult.size();
		List<FacetResult<String>> tmpSearchResult = new ArrayList<FacetResult<String>>();
		for (int i = offset; i < totalCount && i < (offset + limit); i++) {
			tmpSearchResult.add(searchResult.get(i));
		}

		searchResult = tmpSearchResult;
		List<String> reloadDecisivePurchaseIds = new ArrayList<String>();
		for (FacetResult<String> facetResult : searchResult) {
			reloadDecisivePurchaseIds.add(facetResult.getValue());
		}
		
		List<DecisivePurchaseDO> decisivePurchases =new ArrayList<DecisivePurchaseDO>();
		if (reloadDecisivePurchaseIds.size() > 0) {
			Map<String, DecisivePurchaseDO> resultMap = solrOperations.find(DecisivePurchaseDO.class, String.class, reloadDecisivePurchaseIds);
			for (FacetResult<String> facetResult : searchResult) {
				DecisivePurchaseDO decisivePurchase = resultMap.get(facetResult.getValue());
				if(decisivePurchase == null || decisivePurchase.isDeleteFlg()) continue;
				decisivePurchase.setRatings(facetResult.getCount());
				decisivePurchases.add(decisivePurchase);
			}
		}
		return new SearchResult<DecisivePurchaseDO>(totalCount, decisivePurchases);
	}
	/**
	 * 商品に紐づく購入の決め手を返します。
	 */
	@Override
	public SearchResult<DecisivePurchaseDO> findDecisivePurchaseFromIndexByIds(List<String> decisivePurchaseIds) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("deleteFlg_b:false");
		if (decisivePurchaseIds != null ) {
			if (decisivePurchaseIds.size() == 1) {
				buffer.append(" AND decisivePurchaseId:");
				buffer.append(SolrUtil.escape(decisivePurchaseIds.get(0)));
			} else {
				buffer.append(" AND (");
				for(int i=0; i<decisivePurchaseIds.size(); i++){
					buffer.append("decisivePurchaseId:");
					buffer.append(SolrUtil.escape(decisivePurchaseIds.get(i)));
					if( i != decisivePurchaseIds.size() - 1 ) {
						buffer.append(" OR ");
					}
				}
				buffer.append(")");
			}
		}
		SearchResult<DecisivePurchaseDO> result = new SearchResult<DecisivePurchaseDO>(
				solrOperations.findByQuery(new SolrQuery(buffer.toString()), DecisivePurchaseDO.class));
		return result;
	}

	@Override
	public Map<String, Long> decisivePurchaseCountMap(List<String> decisivePurchaseIds, String excludeCommunityUserId) {
		Map<String, Long> result = new HashMap<String, Long>();
		if( decisivePurchaseIds == null || decisivePurchaseIds.isEmpty())
			return result;
		
		StringBuilder query = new StringBuilder();
		query.append("withdraw_b:false AND deleteFlag_b:false AND (");
		for (int i = 0; i < decisivePurchaseIds.size(); i++) {
			if (i > 0) {
				query.append(" OR ");
			}
			query.append("decisivePurchaseId_s:");
			query.append(SolrUtil.escape(decisivePurchaseIds.get(i)));
		}
		query.append(")");
		query.append(" AND temporary_b:false");
		query.append(" AND stopFlg_b:false");
		query.append(" AND reviewId_s:*");
		if( StringUtils.isNotEmpty(excludeCommunityUserId) ){
			query.append(" AND !communityUserId_s:");
			query.append(SolrUtil.escape(excludeCommunityUserId));			
		}
		
		List<FacetResult<String>> facetResults
		= solrOperations.facet(
				ReviewDecisivePurchaseDO.class,
				String.class,
				new SolrQuery(query.toString())
				.addFacetField("decisivePurchaseId_s")
				.setFacetLimit(SolrConstants.QUERY_ROW_LIMIT));
		
		for (FacetResult<String> facetResult : facetResults ){
			result.put(facetResult.getValue(), facetResult.getCount());
		}
		return result;
	}

	/**
	 * 指定した購入の決め手を選択したコミュニティユーザーを返します。
	 * @param decisivePurchaseId 購入の決め手ID
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param limit 最大取得件数
	 * @return 指定した購入の決め手を選択したコミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findCommunityUserExcludeCommunityUserIdByDecisivePurchaseId(
			String decisivePurchaseId,
			String excludeCommunityUserId,
			int limit) {
		int tmpLimit = limit;
		StringBuilder template = new StringBuilder();
		template.append("withdraw_b:false AND deleteFlag_b:false AND ");
		template.append("decisivePurchaseId_s:");
		template.append(SolrUtil.escape(decisivePurchaseId));
		template.append(" AND temporary_b:false");
		template.append(" AND stopFlg_b:false");
		template.append(" AND reviewId_s:*");
		if( StringUtils.isNotEmpty(excludeCommunityUserId) ){
			template.append(" AND !communityUserId_s:");
			template.append(SolrUtil.escape(excludeCommunityUserId));			
		}	
		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();

		//10件以上がわかれば良いので、上限値は大きくなくていい。
		List<FacetResult<String>> facetResult
			= solrOperations.facet(
					ReviewDecisivePurchaseDO.class,
					String.class,
					new SolrQuery(template.toString())
					.addFacetField("communityUserId_s")
					.setFacetLimit(SolrConstants.QUERY_ROW_LIMIT)
					.setFacetMinCount(1));
		SearchResult<CommunityUserDO> result
				= new SearchResult<CommunityUserDO>(
						facetResult.size(), new ArrayList<CommunityUserDO>());
		if (result.getNumFound() == 0) {
			return result;
		}

		Set<String> searchedCommunityUserIds = new HashSet<String>();
		Set<String> findCommunityUserIds = new LinkedHashSet<String>();
		
		if( null != loginCommunityUserId ){
			SearchResult<CommunityUserFollowDO> followCommunityUsers
					= new SearchResult<CommunityUserFollowDO>(
							solrOperations.findByQuery(
					new SolrQuery("communityUserId_s:"
							+ loginCommunityUserId).setRows(SolrConstants.QUERY_ROW_LIMIT),
					CommunityUserFollowDO.class));
	
			if (followCommunityUsers.getNumFound() > 0) {
				StringBuilder buffer = new StringBuilder();
				buffer.append(template.toString());
				buffer.append(" AND (");
				for (CommunityUserFollowDO follow : followCommunityUsers.getDocuments()) {
					if (searchedCommunityUserIds.size() > 0) {
						buffer.append(" OR ");
					}
					buffer.append("communityUserId_s:");
					buffer.append(SolrUtil.escape(follow.getFollowCommunityUser().getCommunityUserId()));
					searchedCommunityUserIds.add(
							follow.getFollowCommunityUser().getCommunityUserId());
				}
				buffer.append(")");
	
				for (FacetResult<String> target : solrOperations.facet(
						ReviewDecisivePurchaseDO.class,
						String.class,
						new SolrQuery(buffer.toString()).addFacetField(
										"communityUserId_s").setFacetMinCount(1).setFacetLimit(tmpLimit))) {
					findCommunityUserIds.add(target.getValue());
				}
			}
	
			tmpLimit = limit - findCommunityUserIds.size();
	
			if (tmpLimit == 0 || result.getNumFound() == findCommunityUserIds.size()) {
				Map<String, CommunityUserDO> resultMap = solrOperations.find(
						CommunityUserDO.class,
						String.class, findCommunityUserIds);
				for (String communityUserId : findCommunityUserIds) {
					result.getDocuments().add(resultMap.get(communityUserId));
				}
				return result;
			}

			SearchResult<CommunityUserFollowDO> followerCommunityUsers
					= new SearchResult<CommunityUserFollowDO>(
							solrOperations.findByQuery(
					new SolrQuery("followCommunityUserId_s:"
							+ SolrUtil.escape(loginCommunityUserId)
							).setRows(SolrConstants.QUERY_ROW_LIMIT
											).setSortField("followDate_dt", ORDER.desc),
					CommunityUserFollowDO.class));
	
			if (followerCommunityUsers.getNumFound() > 0) {
				StringBuilder buffer = new StringBuilder();
				buffer.append(template.toString());
				boolean init = false;
				for (CommunityUserFollowDO follower : followerCommunityUsers.getDocuments()) {
					if (searchedCommunityUserIds.contains(
							follower.getCommunityUser().getCommunityUserId())) {
						continue;
					}
					if (init) {
						buffer.append(" OR ");
					} else {
						buffer.append(" AND (");
						init = true;
					}
					buffer.append("communityUserId_s:");
					buffer.append(SolrUtil.escape(follower.getCommunityUser().getCommunityUserId()));
					searchedCommunityUserIds.add(
							follower.getCommunityUser().getCommunityUserId());
				}
				if (init) {
					buffer.append(")");
				}
	
				for (FacetResult<String> target : solrOperations.facet(
						ReviewDecisivePurchaseDO.class,
						String.class,
						new SolrQuery(buffer.toString()).addFacetField(
										"communityUserId_s").setFacetMinCount(1).setFacetLimit(tmpLimit))) {
					findCommunityUserIds.add(target.getValue());
				}
			}
	
			tmpLimit = limit - findCommunityUserIds.size();
	
			if (tmpLimit == 0 || result.getNumFound() == findCommunityUserIds.size()) {
				Map<String, CommunityUserDO> resultMap = solrOperations.find(
						CommunityUserDO.class,
						String.class, findCommunityUserIds);
				for (String communityUserId : findCommunityUserIds) {
					result.getDocuments().add(resultMap.get(communityUserId));
				}
				return result;
			}

		}
		
		StringBuilder buffer = new StringBuilder();
		buffer.append(template.toString());
		if (findCommunityUserIds.size() > 0) {
			buffer.append(" AND !(");
			boolean init = false;
			for (String communityUserId : findCommunityUserIds) {
				if (init) {
					buffer.append(" OR ");
				} else {
					init = true;
				}
				buffer.append("communityUserId_s:");
				buffer.append(SolrUtil.escape(communityUserId));
			}
			buffer.append(")");
		}

		for (FacetResult<String> target : solrOperations.facet(
				ReviewDecisivePurchaseDO.class,
				String.class,
				new SolrQuery(
					buffer.toString()).addFacetField("communityUserId_s").setFacetLimit(
							tmpLimit).setFacetMinCount(1))) {
			findCommunityUserIds.add(target.getValue());
		}

		Map<String, CommunityUserDO> resultMap = solrOperations.find(
				CommunityUserDO.class,
				String.class, findCommunityUserIds);
		for (String communityUserId : findCommunityUserIds) {
			result.getDocuments().add(resultMap.get(communityUserId));
		}

		return result;
	}

	/**
	 * 指定したレビュー情報において、購入に迷った商品に選ばれた商品を返します。
	 * @param reviewId レビューID
	 * @return 購入に迷った商品のリスト
	 */
	@Override
	public SearchResult<PurchaseLostProductDO> findPurchaseLostProductByReviewId(
			String reviewId) {
		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());
		String baseQuery = "withdraw_b:false AND stopFlg_b:false AND deleteFlag_b:false AND reviewId_s:" + SolrUtil.escape(reviewId);
		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(baseQuery));
		SearchResult<PurchaseLostProductDO> searchResult
				= new SearchResult<PurchaseLostProductDO>(
						solrOperations.findByQuery(
				query, PurchaseLostProductDO.class,
				Path.includeProp("*").includePath(
						"product.sku").depth(2)));
		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(
							baseQuery, PurchaseLostProductDO.class, solrOperations));
		}
		ProductUtil.filterInvalidProduct(searchResult);

		return searchResult;
	}

	/**
	 * 指定したレビュー情報において、過去に使用した商品に選ばれた商品を返します。
	 * @param reviewId レビューID
	 * @return 過去に使用した商品のリスト
	 */
	@Override
	public SearchResult<UsedProductDO> findUsedProductByReviewId(String reviewId) {
		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());
		String baseQuery = "withdraw_b:false AND deleteFlag_b:false AND stopFlg_b:false AND reviewId_s:" + SolrUtil.escape(reviewId);
		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(baseQuery));
		SearchResult<UsedProductDO> searchResult
				= new SearchResult<UsedProductDO>(
						solrOperations.findByQuery(
				query, UsedProductDO.class,
				Path.includeProp("*").includePath(
						"product.sku").depth(2)));
		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(
							baseQuery, UsedProductDO.class, solrOperations));
		}
		ProductUtil.filterInvalidProduct(searchResult);

		return searchResult;

	}

	/**
	 * 指定した商品のレビュー情報において、購入に迷った商品に選ばれた商品カウントを返します。
	 * @param sku SKU SKU
	 * @param limit 最大取得件数
	 * @return 購入に迷った商品カウントのリスト
	 */
	@Override
	public SearchResult<FacetResult<String>> findPurchaseLostProductCountBySku(
			String sku, int limit) {
		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());
		SearchResult<FacetResult<String>> searchResult = new SearchResult<FacetResult<String>>();
		String baseQuery = "withdraw_b:false AND deleteFlag_b:false AND reviewId_s:* AND temporary_b:false AND stopFlg_b:false AND productId_s:* AND reviewProductId_s:" + SolrUtil.escape(sku);
		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(baseQuery));
		query.addFacetField("productId_s");
		query.setFacetLimit(limit);
		query.setFacetSort(FacetParams.FACET_SORT_COUNT);
		List<FacetResult<String>> facetResult = solrOperations.facet(
				PurchaseLostProductDO.class, String.class, query);
		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(
							baseQuery, PurchaseLostProductDO.class, solrOperations));
		}
		searchResult.setDocuments(facetResult);
		searchResult.setNumFound(facetResult.size());

		return searchResult;
	}

	/**
	 * 指定した商品のレビュー情報において、過去に使用した商品に選ばれた商品カウントを返します。
	 * @param sku SKU SKU
	 * @param limit 最大取得件数
	 * @return 過去に使用した商品カウントのリスト
	 */
	@Override
	public SearchResult<FacetResult<String>> findUsedProductCountBySku(
			String sku, int limit) {
		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());
		SearchResult<FacetResult<String>> searchResult = new SearchResult<FacetResult<String>>();
		String baseQuery = "withdraw_b:false AND deleteFlag_b:false AND stopFlg_b:false AND reviewId_s:* AND temporary_b:false AND productId_s:* AND reviewProductId_s:" + SolrUtil.escape(sku);
		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(baseQuery));
		query.addFacetField("productId_s");
		query.setFacetLimit(limit);
		query.setFacetSort(FacetParams.FACET_SORT_COUNT);
		List<FacetResult<String>> facetResult = solrOperations.facet(
				UsedProductDO.class, String.class, query);
		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(
							baseQuery, UsedProductDO.class, solrOperations));
		}
		searchResult.setDocuments(facetResult);
		searchResult.setNumFound(facetResult.size());

		return searchResult;
	}

	/**
	 * 指定した期間に更新のあったレビューを返します。
	 * @param fromDate 検索開始時間
	 * @param toDate 検索終了時間
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return
	 */
	@Override
	public SearchResult<ReviewDO> findUpdatedReviewByOffsetTime(
			Date fromDate, Date toDate, int limit, int offset) {
		StringBuilder buffer = new StringBuilder();
		// 有効
		buffer.append(" ( ");
		buffer.append("postDate_dt:{" +
				DateUtil.getThreadLocalDateFormat().format(fromDate) + " TO " + DateUtil.getThreadLocalDateFormat().format(toDate) + "}");
		buffer.append(" AND status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" ) ");
		// 削除
		buffer.append(" OR ( ");
		buffer.append("deleteDate_dt:{" +
				DateUtil.getThreadLocalDateFormat().format(fromDate) + " TO " + DateUtil.getThreadLocalDateFormat().format(toDate) + "}");
		buffer.append(" AND status_s:");
		buffer.append(ContentsStatus.DELETE.getCode());
		buffer.append(" ) ");
		// 一時停止
		buffer.append(" OR ( ");
		buffer.append("modifyDateTime_dt:{" +
				DateUtil.getThreadLocalDateFormat().format(fromDate) + " TO " + DateUtil.getThreadLocalDateFormat().format(toDate) + "}");
		buffer.append(" AND status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		SolrQuery query = new SolrQuery(buffer.toString());
		if (limit > 0)
			query.setRows(limit);
		query.setStart(offset);
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(query, ReviewDO.class));
		return searchResult;
	}
	
	
	/**
	 * 指定したユーザーの全ての有効レビュー、一時停止レビューを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return
	 */
	@Override
	public SearchResult<ReviewDO> findReviewByCommunityUserId(
			String communityUserId, int limit, int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		SolrQuery query = new SolrQuery(buffer.toString());
		if (limit > 0)
			query.setRows(limit);
		query.setStart(offset);
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(query, ReviewDO.class));
		return searchResult;
	}
	
	/**
	 * 指定した商品、レビュー区間に対するレビューを投稿日時順（降順）に返します。
	 * @param product 商品
	 * @param reviewType レビュータイプ
	 * @param reviewTerm レビュー区間
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@Override
	public SearchResult<ReviewDO> findNewReviewBySku(
			ProductDO product,
			ReviewType reviewType,
			Integer reviewTerm,
			String excludeReviewId,
			int limit, Date offsetTime, boolean previous) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND productId_s:" + SolrUtil.escape(product.getSku()));
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		if(!StringUtils.isEmpty(excludeReviewId))
			buffer.append(" AND !reviewId:" + SolrUtil.escape(excludeReviewId));
		if (reviewType != null) {
			String condition = ReviewDO.getReviewTypeQuery(product).get(reviewType.getCode());
			if (condition != null) {
				buffer.append(" AND ").append(condition);
			}
		}
		if (reviewTerm != null) {
			String condition = ReviewDO.getReviewTermQuery(product).get(reviewTerm);
			if (condition != null) {
				buffer.append(" AND ");
				buffer.append(condition);
			}
		}
		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND postDate_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND postDate_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("postDate_dt", ORDER.desc);
		} else {
			query.setSortField("postDate_dt", ORDER.asc);
		}
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(query, ReviewDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId," +
						"reviewDecisivePurchases.decisivePurchase.decisivePurchaseId").depth(2)));
		ProductUtil.filterInvalidProduct(searchResult);
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}
	
	@Override
	public SearchResult<ReviewDO> findNewReviewBySkus(
			List<String> skus,
			ReviewType reviewType,
			String excludeReviewId,
			int limit, Date offsetTime, boolean previous){
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false");
		if( skus.size() == 1){
			buffer.append(" AND productId_s:");
			buffer.append(SolrUtil.escape(skus.get(0)));
		}else{
			buffer.append(" AND (");
			for(int i=0; i<skus.size(); i++){
				buffer.append("productId_s:");
				buffer.append(SolrUtil.escape(skus.get(i)));
				if( i != skus.size() - 1 )
					buffer.append(" OR ");
			}
			buffer.append(")"); 
		}
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		if(!StringUtils.isEmpty(excludeReviewId))
			buffer.append(" AND !reviewId:" + SolrUtil.escape(excludeReviewId));
		if (reviewType != null) {
			buffer.append(" AND reviewType_s:").append(SolrUtil.escape(reviewType.getCode()));
		}
		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND postDate_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND postDate_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("postDate_dt", ORDER.desc);
		} else {
			query.setSortField("postDate_dt", ORDER.asc);
		}
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(query, ReviewDO.class,
				Path.includeProp("*").includePath(
						"product.sku," + 
						"communityUser.communityUserId," +
						"reviewDecisivePurchases.decisivePurchase.decisivePurchaseId").depth(2)));
		ProductUtil.filterInvalidProduct(searchResult);
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}
	
	/**
	 * 指定した商品、評価に対するレビューを投稿日時順（降順）に返します。
	 * @param product 商品
	 * @param productSatisfaction 指定の評価
	 * @param sortType ソート順（01:最新順,02:適合度順)
	 * @param excludeReviewId 除外するレビューID
	 * @param reviewTerm レビュー区間
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@Override
	public SearchResult<ReviewDO> findReviewBySkuAndRatingStar(
			ProductDO product,
			ProductSatisfaction productSatisfaction,
			ReviewSortType sortType,
			Integer reviewTerm,
			String excludeReviewId,
			int limit,
			Date offsetTime,
			Double offsetScore,
			boolean previous) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND productId_s:" + SolrUtil.escape(product.getSku()));
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		if(!StringUtils.isEmpty(excludeReviewId))
			buffer.append(" AND !reviewId:" + SolrUtil.escape(excludeReviewId));
		
		if (productSatisfaction != null)
			buffer.append(" AND productSatisfaction_s:" + productSatisfaction.getCode());
		
		if (reviewTerm != null) {
			String condition = ReviewDO.getReviewTermQuery(product).get(reviewTerm);
			if (condition != null) {
				buffer.append(" AND ");
				buffer.append(condition);
			}
		}
		if(ReviewSortType.MATCH_SORT.equals(sortType)){
			if (offsetScore != null) {
				if (previous) {
					buffer.append(" AND ((");
					buffer.append("reviewScore_d:[" + offsetScore + " TO *]");
					buffer.append(" AND postDate_dt:{" + DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
					buffer.append(") OR ");
					buffer.append("reviewScore_d:{" + offsetScore + " TO *}");
					buffer.append(")");
				} else {
					buffer.append(" AND ((");
					buffer.append("reviewScore_d:[* TO " + offsetScore + "]");
					buffer.append(" AND postDate_dt:{* TO " + DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
					buffer.append(") OR ");
					buffer.append("reviewScore_d:{* TO " + offsetScore + "}");
					buffer.append(")");
				}
			}
		}else{
			if (offsetTime != null) {
				if (previous) {
					buffer.append(" AND postDate_dt:{" +
							DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
				} else {
					buffer.append(" AND postDate_dt:{* TO " +
							DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
				}
			}
		}
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(limit);
		
		if(ReviewSortType.MATCH_SORT.equals(sortType)){
			if (offsetTime == null || !previous) {
				query.setSortField("reviewScore_d", ORDER.desc);
				query.addSortField("postDate_dt", ORDER.desc);
			} else {
				query.setSortField("reviewScore_d", ORDER.asc);
				query.addSortField("postDate_dt", ORDER.asc);
			}
		}else{
			if (offsetTime == null || !previous) {
				query.setSortField("postDate_dt", ORDER.desc);
			} else {
				query.setSortField("postDate_dt", ORDER.asc);
			}
		}
		
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(query, ReviewDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId," +
						"reviewDecisivePurchases.decisivePurchase.decisivePurchaseId").depth(2)));
		ProductUtil.filterInvalidProduct(searchResult);
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}
	
	/**
	 * 指定した商品、評価に対するレビューを投稿日時順（降順）に返します。
	 * @param skus 商品SKU一覧
	 * @param productSatisfaction 指定の評価
	 * @param sortType ソート順（01:最新順,02:適合度順)
	 * @param excludeReviewId 除外するレビューID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@Override
	public SearchResult<ReviewDO> findNewReviewBySkusAndRatingStar(
			List<String> skus,
			ProductSatisfaction productSatisfaction,
			ReviewSortType sortType,
			String excludeReviewId,
			int limit,
			Date offsetTime,
			Double offsetScore,
			boolean previous) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false");
		if( skus.size() == 1){
			buffer.append(" AND productId_s:");
			buffer.append(SolrUtil.escape(skus.get(0)));
		}else{
			buffer.append(" AND (");
			for(int i=0; i<skus.size(); i++){
				buffer.append("productId_s:");
				buffer.append(SolrUtil.escape(skus.get(i)));
				if( i != skus.size() - 1 )
					buffer.append(" OR ");
			}
			buffer.append(")"); 
		}
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		if(!StringUtils.isEmpty(excludeReviewId))
			buffer.append(" AND !reviewId:" + SolrUtil.escape(excludeReviewId));
		
		if (productSatisfaction != null)
			buffer.append(" AND productSatisfaction_s:" + productSatisfaction.getCode());
		
		if(ReviewSortType.MATCH_SORT.equals(sortType)){
			if (offsetScore != null) {
				if (previous) {
					buffer.append(" AND ((");
					buffer.append("reviewScore_d:[" + offsetScore + " TO *]");
					buffer.append(" AND postDate_dt:{" + DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
					buffer.append(") OR ");
					buffer.append("reviewScore_d:{" + offsetScore + " TO *}");
					buffer.append(")");
				} else {
					buffer.append(" AND ((");
					buffer.append("reviewScore_d:[* TO " + offsetScore + "]");
					buffer.append(" AND postDate_dt:{* TO " + DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
					buffer.append(") OR ");
					buffer.append("reviewScore_d:{* TO " + offsetScore + "}");
					buffer.append(")");
				}
			}
		}else{
			if (offsetTime != null) {
				if (previous) {
					buffer.append(" AND postDate_dt:{" +
							DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
				} else {
					buffer.append(" AND postDate_dt:{* TO " +
							DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
				}
			}
		}
		
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(limit);
		
		if(ReviewSortType.MATCH_SORT.equals(sortType)){
			if (offsetTime == null || !previous) {
				query.setSortField("reviewScore_d", ORDER.desc);
				query.addSortField("postDate_dt", ORDER.desc);
			} else {
				query.setSortField("reviewScore_d", ORDER.asc);
				query.addSortField("postDate_dt", ORDER.asc);
			}
		}else{
			if (offsetTime == null || !previous) {
				query.setSortField("postDate_dt", ORDER.desc);
			} else {
				query.setSortField("postDate_dt", ORDER.asc);
			}
		}
		
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(query, ReviewDO.class,
				Path.includeProp("*").includePath(
						"product.sku," + 
						"communityUser.communityUserId," +
						"reviewDecisivePurchases.decisivePurchase.decisivePurchaseId").depth(2)));
		ProductUtil.filterInvalidProduct(searchResult);
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}

	/**
	 * 指定した商品とコミュニティユーザーIDに対する、レビュー区間に対するレビューを投稿日時順（降順）に返します。
	 * @param communityUserId コミュニティーユーザーID
	 * @param product 商品
	 * @param reviewTerm レビュー区間
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	public SearchResult<ReviewDO> findNewReviewBySkuAndCommunityUserId(
			String communityUserId,
			String sku,
			int limit,
			Date offsetTime,
			boolean previous){
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false");
		buffer.append(" AND productId_s:");
		buffer.append(SolrUtil.escape(sku));
		buffer.append(" AND communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		
		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND postDate_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND postDate_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("postDate_dt", ORDER.desc);
		} else {
			query.setSortField("postDate_dt", ORDER.asc);
		}
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(query, ReviewDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId," +
						"reviewDecisivePurchases.decisivePurchase.decisivePurchaseId").depth(2)));
		ProductUtil.filterInvalidProduct(searchResult);
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}
	
	public long countReviewBySku(String sku){
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND productId_s:" + SolrUtil.escape(sku));
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(0);
		
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(query, ReviewDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId," +
						"reviewDecisivePurchases.decisivePurchase.decisivePurchaseId").depth(2)));
		ProductUtil.filterInvalidProduct(searchResult);
		
		return searchResult.getNumFound();
	}
	
	public long countReviewBySkus(List<String> skus){
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false");
		if( skus.size() == 1){
			buffer.append(" AND productId_s:");
			buffer.append(SolrUtil.escape(skus.get(0)));
		}else{
			buffer.append(" AND (");
			for(int i=0; i<skus.size(); i++){
				buffer.append("productId_s:");
				buffer.append(SolrUtil.escape(skus.get(i)));
				if( i != skus.size() - 1 )
					buffer.append(" OR ");
			}
			buffer.append(")"); 
		}
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(0);
		
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(query, ReviewDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId," +
						"reviewDecisivePurchases.decisivePurchase.decisivePurchaseId").depth(2)));
		ProductUtil.filterInvalidProduct(searchResult);
		
		return searchResult.getNumFound();
	}
	
	/**
	 * WSせんよう
	 * 指定した商品、レビュー区間に対するレビューを投稿日時順（降順）に返します。
	 * @param product 商品
	 * @param reviewType レビュータイプ
	 * @param reviewTerm レビュー区間
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@Override
	public SearchResult<ReviewDO> findNewReviewBySku(String sku, int limit) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND productId_s:" + SolrUtil.escape(sku));
		buffer.append(" AND ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(limit);
		query.setSortField("postDate_dt", ORDER.desc);
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(query, ReviewDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId," +
						"reviewDecisivePurchases.decisivePurchase.decisivePurchaseId").depth(2)));
		return searchResult;
	}
	
	
	/**
	 * 指定のカテゴリコードの最新のレビューを取得する（カテゴリ指定なしは、すべての商品が対象）
	 * @param categoryCode カテゴリコード
	 * @param offsetTime 検索キー（対象は、投稿日時）
	 * @param limit 取得上限
	 * @param previous (true：古い順,false:新しい順）
	 * @return レビュー一覧を取得
	 */
	@Override
	public SearchResult<ReviewDO> findReviewByCategoryCode(
			String categoryCode,
			Date offsetTime,
			int limit,
			boolean previous) {
		// TODO カテゴリが指定できるようになったら利用する。
		
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false");
		buffer.append(" AND ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND postDate_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND postDate_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(limit);
		if( previous ){
			query.setSortField("postDate_dt", ORDER.asc);
		}else{
			query.setSortField("postDate_dt", ORDER.desc);
		}
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(query, ReviewDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId,product.sku").depth(1)));
		ProductUtil.filterInvalidProduct(searchResult);
		
		// SolrにUserがIndexされていない場合の対処
		Iterator<ReviewDO> it = searchResult.getDocuments().iterator();
		while (it.hasNext()) {
			ReviewDO reviewDO = it.next();
			if (reviewDO == null || reviewDO.getCommunityUser() == null || reviewDO.getCommunityUser().getCommunityName() == null) {
				it.remove();
			}
		}
		return searchResult;
	}

	/**
	 * 指定した商品、経過月に対するレビューを適合順（降順）に返します。
	 * @param product 商品
	 * @param reviewType レビュータイプ
	 * @param reviewTerm レビュー区間
	 * @param limit 最大取得件数
	 * @param offsetMatchScore 検索開始適合度
	 * @param offsetTime 検索開始時間（２次ソート条件）
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@Override
	public SearchResult<ReviewDO> findMatchReviewBySku(
			ProductDO product,
			ReviewType reviewType,
			Integer reviewTerm,
			String excludeReviewId,
			int limit,
			Double offsetMatchScore,
			Date offsetTime,
			boolean previous) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND productId_s:" + SolrUtil.escape(product.getSku()));
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		if(!StringUtils.isEmpty(excludeReviewId))
			buffer.append(" AND !reviewId:" + SolrUtil.escape(excludeReviewId));
		if (reviewType != null) {
			buffer.append(" AND reviewType_s:").append(SolrUtil.escape(reviewType.getCode()));
		}
		if (reviewTerm != null) {
			String condition = ReviewDO.getReviewTermQuery(product).get(reviewTerm);
			if (condition != null) {
				buffer.append(" AND ");
				buffer.append(condition);
			}
		}
		if (offsetMatchScore != null) {
			if (previous) {
				buffer.append(" AND ((");
				buffer.append("reviewScore_d:[" +
						offsetMatchScore + " TO *]");
				buffer.append(" AND postDate_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
				buffer.append(") OR ");
				buffer.append("reviewScore_d:{" +
						offsetMatchScore + " TO *}");
				buffer.append(")");
			} else {
				buffer.append(" AND ((");
				buffer.append("reviewScore_d:[* TO " +
						offsetMatchScore + "]");
				buffer.append(" AND postDate_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
				buffer.append(") OR ");
				buffer.append("reviewScore_d:{* TO " +
						offsetMatchScore + "}");
				buffer.append(")");
			}
		}
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("reviewScore_d", ORDER.desc);
			query.addSortField("postDate_dt", ORDER.desc);
		} else {
			query.setSortField("reviewScore_d", ORDER.asc);
			query.addSortField("postDate_dt", ORDER.asc);
		}
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(query, ReviewDO.class,
						Path.includeProp("*").includePath(
								"communityUser.communityUserId," +
								"reviewDecisivePurchases.decisivePurchase.decisivePurchaseId").depth(2)));
		ProductUtil.filterInvalidProduct(searchResult);
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}
	
	/**
	 * 指定した商品、経過月に対するレビューを適合順（降順）に返します。
	 * @param product 商品
	 * @param reviewType レビュータイプ
	 * @param reviewTerm レビュー区間
	 * @param limit 最大取得件数
	 * @param offsetMatchScore 検索開始適合度
	 * @param offsetTime 検索開始時間（２次ソート条件）
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@Override
	public SearchResult<ReviewDO> findMatchReviewBySkus(
			List<String> skus,
			ReviewType reviewType,
			String excludeReviewId,
			int limit,
			Double offsetMatchScore,
			Date offsetTime,
			boolean previous) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false");
		if( skus.size() == 1){
			buffer.append(" AND productId_s:");
			buffer.append(SolrUtil.escape(skus.get(0)));
		}else{
			buffer.append(" AND (");
			for(int i=0; i<skus.size(); i++){
				buffer.append("productId_s:");
				buffer.append(SolrUtil.escape(skus.get(i)));
				if( i != skus.size() - 1 )
					buffer.append(" OR ");
			}
			buffer.append(")"); 
		}
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		if(!StringUtils.isEmpty(excludeReviewId))
			buffer.append(" AND !reviewId:" + SolrUtil.escape(excludeReviewId));
		if (reviewType != null) {
			buffer.append(" AND reviewType_s:").append(SolrUtil.escape(reviewType.getCode()));
		}
		if (offsetMatchScore != null) {
			if (previous) {
				buffer.append(" AND ((");
				buffer.append("reviewScore_d:[" +
						offsetMatchScore + " TO *]");
				buffer.append(" AND postDate_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
				buffer.append(") OR ");
				buffer.append("reviewScore_d:{" +
						offsetMatchScore + " TO *}");
				buffer.append(")");
			} else {
				buffer.append(" AND ((");
				buffer.append("reviewScore_d:[* TO " +
						offsetMatchScore + "]");
				buffer.append(" AND postDate_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
				buffer.append(") OR ");
				buffer.append("reviewScore_d:{* TO " +
						offsetMatchScore + "}");
				buffer.append(")");
			}
		}
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("reviewScore_d", ORDER.desc);
			query.addSortField("postDate_dt", ORDER.desc);
		} else {
			query.setSortField("reviewScore_d", ORDER.asc);
			query.addSortField("postDate_dt", ORDER.asc);
		}
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(query, ReviewDO.class,
						Path.includeProp("*").includePath(
								"product.sku," +
								"communityUser.communityUserId," +
								"reviewDecisivePurchases.decisivePurchase.decisivePurchaseId").depth(2)));
		ProductUtil.filterInvalidProduct(searchResult);
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}
	
	/**
	 * WSせんよう
	 * 指定した商品、経過月に対するレビューを適合順（降順）に返します。
	 * @param product 商品
	 * @param reviewType レビュータイプ
	 * @param reviewTerm レビュー区間
	 * @param limit 最大取得件数
	 * @param offsetMatchScore 検索開始適合度
	 * @param offsetTime 検索開始時間（２次ソート条件）
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@Override
	public SearchResult<ReviewDO> findMatchReviewBySku(String sku, int limit) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND productId_s:" + SolrUtil.escape(sku));
		buffer.append(" AND ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(limit);
		query.setSortField("reviewScore_d", ORDER.desc);
		query.addSortField("postDate_dt", ORDER.desc);
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(query, ReviewDO.class,
						Path.includeProp("*").includePath(
								"communityUser.communityUserId," +
								"reviewDecisivePurchases.decisivePurchase.decisivePurchaseId").depth(2)));
		return searchResult;
	}
	

	/**
	 * 指定したレビューを除外した、商品・レビュワーに紐づくレビュー情報を返します。
	 * @param sku SKU
	 * @param communityUserId コミュニティユーザーID
	 * @param excludeReviewId 除外するレビューID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 検索結果
	 */
	@Override
	public SearchResult<ReviewDO> findReviewExcludeReviewIdByCommuntyUserIdAndSKU(
			String sku,
			String communityUserId,
			String excludeReviewId,
			int limit,
			int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND productId_s:");
		buffer.append(SolrUtil.escape(sku));
		buffer.append(" AND communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		//IDなのでsuffix無し
		buffer.append(" AND !reviewId:");
		buffer.append(SolrUtil.escape(excludeReviewId));
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(
				new SolrQuery(buffer.toString()).setRows(limit).setStart(
						offset).addSortField("postDate_dt", ORDER.desc),
				ReviewDO.class, Path.includeProp("*").includePath(
						"product.sku,communityUser.communityUserId").depth(1)));
		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}

	/**
	 * 指定した商品を除外した、レビュワーに紐づくレビュー情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param excludeSKU 除外するSKU
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 検索結果
	 */
	@Override
	public SearchResult<ReviewDO> findReviewExcludeSkuByCommunityUserId(
			String communityUserId,
			String excludeSKU,
			int limit,
			int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		buffer.append(" AND !productId_s:");
		buffer.append(SolrUtil.escape(excludeSKU));

		//hasAdult対応対象です。

		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(
				new SolrQuery(adultHelper.toFilterQuery(buffer.toString())).setRows(limit
						).setStart(offset).addSortField("postDate_dt", ORDER.desc),
				ReviewDO.class, Path.includeProp("*").includePath("product.sku").depth(1)));
		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(searchResult.getDocuments()));
			if (!searchResult.isHasAdult()) {
				searchResult.setHasAdult(
						adultHelper.hasAdult(
								buffer.toString(), ReviewDO.class, solrOperations));
			}
		}
		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}

	@Override
	public Map<ProductSatisfaction, Long> loadProductSatisfactionSummaryMapWithAll(String sku) {
		return loadProductSatisfactionSummaryMap(sku, true);
	}
	
	@Override
	public Map<String, Map<ProductSatisfaction, Long>> loadProductSatisfactionSummaryMapsWithAll(String[] skus) {
		return loadProductSatisfactionSummaryMaps(skus, true);
	}
	
	private Map<ProductSatisfaction, Long> loadProductSatisfactionSummaryMap(String sku, boolean isAll) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND productId_s:");
		buffer.append(SolrUtil.escape(sku));
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" ) ");
		if( !isAll )
			buffer.append(" AND latestReview_b:true");
		SolrQuery solrQuery = new SolrQuery(buffer.toString());
		solrQuery.setFacetLimit(SolrConstants.QUERY_ROW_LIMIT);
		solrQuery.addFacetField("productSatisfaction_s");

		Map<ProductSatisfaction, Long> result = new HashMap<ProductSatisfaction, Long>();
		for (FacetResult<String> facetResult : solrOperations.facet(ReviewDO.class, String.class,solrQuery)) {
			result.put(ProductSatisfaction.codeOf(facetResult.getValue()), facetResult.getCount());
		}
		return result;
	}
	
	private Map<String, Map<ProductSatisfaction, Long>> loadProductSatisfactionSummaryMaps(String[] skus, boolean isAll) {
		Asserts.isTrue(skus.length > 0);
		
		Map<String, String[]> queryMap = new HashMap<String,String[]>();
		SolrQuery solrQuery = new SolrQuery("*:*");
		Map<String, Map<ProductSatisfaction, Long>> productSatisfactionMap = new HashMap<String, Map<ProductSatisfaction, Long>>();
		
		for(String sku:skus){
			for(ProductSatisfaction productSatisfaction : ProductSatisfaction.values()){
				StringBuilder buffer = new StringBuilder();
				buffer.append("withdraw_b:false AND productId_s:");
				buffer.append(SolrUtil.escape(sku));
				buffer.append(" AND ( ");
				buffer.append("status_s:");
				buffer.append(ContentsStatus.SUBMITTED.getCode());
				buffer.append(" ) ");
				if( !isAll )
					buffer.append(" AND latestReview_b:true");
				buffer.append(" AND productSatisfaction_s:" + productSatisfaction.getCode());
				String query = buffer.toString();
				String[] datas = new String[]{sku, productSatisfaction.getCode()};
				queryMap.put(query, datas);
				solrQuery.addFacetQuery(query);
			}
		}
		solrQuery.setFacetLimit(solrQuery.getFacetQuery().length);
		solrQuery.setFacetMinCount(0);
		
		for(FacetResult<String> result:solrOperations.facet(ReviewDO.class, String.class, solrQuery)) {
				String[] datas = queryMap.get(result.getFacetQuery());
				Map<ProductSatisfaction, Long> satisfactionCountMap = null;
				if(productSatisfactionMap.containsKey(datas[0])) {
					satisfactionCountMap = productSatisfactionMap.get(datas[0]);
				} else {
					satisfactionCountMap  = new HashMap<ProductSatisfaction, Long>();
				}
				satisfactionCountMap.put(ProductSatisfaction.codeOf(datas[1]), result.getCount());
				productSatisfactionMap.put(datas[0], satisfactionCountMap);
		}
		return productSatisfactionMap;
	}
	

	/**
	 * 指定した商品の満足度に関する選択者リストを返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @return 次も買いたいに関する集計情報
	 */
	@Override
	public Map<ProductSatisfaction, SearchResult<CommunityUserDO>> loadProductSatisfactionUserMap(
			String sku, int limit) {
		Map<ProductSatisfaction, SearchResult<CommunityUserDO>> result
				= new HashMap<ProductSatisfaction, SearchResult<CommunityUserDO>>();
		StringBuilder template = new StringBuilder();
		template.append("withdraw_b:false AND productId_s:");
		template.append(SolrUtil.escape(sku));
		template.append(" AND ( ");
		template.append("status_s:");
		template.append(ContentsStatus.SUBMITTED.getCode());
		template.append(" OR status_s:");
		template.append(ContentsStatus.CONTENTS_STOP.getCode());
		template.append(" ) ");
		template.append(" AND reviewType_s:");
		template.append(SolrUtil.escape(ReviewType.REVIEW_AFTER_FEW_DAYS.getCode()));
		if (requestScopeDao.loadCommunityUserId() != null) {
			template.append(" AND !communityUserId_s:");
			template.append(SolrUtil.escape(requestScopeDao.loadCommunityUserId()));
		}

		SearchResult<CommunityUserFollowDO> followCommunityUsers = null;
		SearchResult<CommunityUserFollowDO> followerCommunityUsers = null;

		for (ProductSatisfaction satisfaction : ProductSatisfaction.values()) {
			if (satisfaction.equals(ProductSatisfaction.NONE)) {
				continue;
			}
			int tmpLimit = limit;
			StringBuilder subTemplate = new StringBuilder();
			subTemplate.append(template.toString());
			subTemplate.append(" AND productSatisfaction_s:");
			subTemplate.append(SolrUtil.escape(satisfaction.getCode()));

			//10件以上がわかれば良いので、上限値は大きくなくていい。
			List<FacetResult<String>> facetResult
				= solrOperations.facet(
								ReviewDO.class,
								String.class,
								new SolrQuery(
							subTemplate.toString()).addFacetField(
									"communityUserId_s").setFacetLimit(
											SolrConstants.QUERY_ROW_LIMIT).setFacetMinCount(1));
			SearchResult<CommunityUserDO> subResult
					= new SearchResult<CommunityUserDO>(
							facetResult.size(), new ArrayList<CommunityUserDO>());
			result.put(satisfaction, subResult);
			if (subResult.getNumFound() == 0) {
				continue;
			}

			if (followCommunityUsers == null) {
				followCommunityUsers
						= new SearchResult<CommunityUserFollowDO>(
								solrOperations.findByQuery(
						new SolrQuery("communityUserId_s:"
								+ SolrUtil.escape(requestScopeDao.loadCommunityUserId(
										))).setRows(SolrConstants.QUERY_ROW_LIMIT),
						CommunityUserFollowDO.class));
			}

			Set<String> searchedCommunityUserIds = new HashSet<String>();
			Set<String> findCommunityUserIds = new LinkedHashSet<String>();
			if (followCommunityUsers.getNumFound() > 0) {
				StringBuilder buffer = new StringBuilder();
				buffer.append(subTemplate.toString());
				buffer.append(" AND (");
				for (CommunityUserFollowDO follow : followCommunityUsers.getDocuments()) {
					if (searchedCommunityUserIds.size() > 0) {
						buffer.append(" OR ");
					}
					buffer.append("communityUserId_s:");
					buffer.append(SolrUtil.escape(follow.getFollowCommunityUser().getCommunityUserId()));
					searchedCommunityUserIds.add(
							follow.getFollowCommunityUser().getCommunityUserId());
				}
				buffer.append(")");

				for (FacetResult<String> target : solrOperations.facet(
						ReviewDO.class,
						String.class,
						new SolrQuery(
					buffer.toString()).addFacetField(
							"communityUserId_s").setFacetLimit(
									tmpLimit).setFacetMinCount(1))) {
					findCommunityUserIds.add(target.getValue());
				}
			}

			tmpLimit = limit - findCommunityUserIds.size();

			if (tmpLimit == 0 || subResult.getNumFound() == findCommunityUserIds.size()) {
				Map<String, CommunityUserDO> resultMap = solrOperations.find(
						CommunityUserDO.class,
						String.class, findCommunityUserIds);
				for(String communityUserId : findCommunityUserIds) {
					subResult.getDocuments().add(resultMap.get(communityUserId));
				}
				continue;
			}

			if (followerCommunityUsers == null) {
				followerCommunityUsers
						= new SearchResult<CommunityUserFollowDO>(
								solrOperations.findByQuery(
						new SolrQuery("followCommunityUserId_s:"
								+ SolrUtil.escape(requestScopeDao.loadCommunityUserId(
										))).setRows(SolrConstants.QUERY_ROW_LIMIT
												).setSortField("followDate_dt", ORDER.desc),
												CommunityUserFollowDO.class));
			}

			if (followerCommunityUsers.getNumFound() > 0) {
				StringBuilder buffer = new StringBuilder();
				buffer.append(subTemplate.toString());
				boolean init = false;
				for (CommunityUserFollowDO follower : followerCommunityUsers.getDocuments()) {
					if (searchedCommunityUserIds.contains(
							follower.getCommunityUser().getCommunityUserId())) {
						continue;
					}
					if (init) {
						buffer.append(" OR ");
					} else {
						buffer.append(" AND (");
						init = true;
					}
					buffer.append("communityUserId_s:");
					buffer.append(SolrUtil.escape(follower.getCommunityUser().getCommunityUserId()));
					searchedCommunityUserIds.add(
							follower.getCommunityUser().getCommunityUserId());
				}
				if (init) {
					buffer.append(")");
				}

				for (FacetResult<String> target : solrOperations.facet(
						ReviewDO.class,
						String.class,
						new SolrQuery(
					buffer.toString()).addFacetField(
							"communityUserId_s").setFacetLimit(
									tmpLimit).setFacetMinCount(1))) {
					findCommunityUserIds.add(target.getValue());
				}
			}

			tmpLimit = limit - findCommunityUserIds.size();

			if (tmpLimit == 0 || subResult.getNumFound() == findCommunityUserIds.size()) {
				Map<String, CommunityUserDO> resultMap = solrOperations.find(
						CommunityUserDO.class,
						String.class, findCommunityUserIds);
				for(String communityUserId : findCommunityUserIds) {
					subResult.getDocuments().add(resultMap.get(communityUserId));
				}
				continue;
			}

			StringBuilder buffer = new StringBuilder();
			buffer.append(subTemplate.toString());
			if (findCommunityUserIds.size() > 0) {
				buffer.append(" AND !(");
				boolean init = false;
				for (String communityUserId : findCommunityUserIds) {
					if (init) {
						buffer.append(" OR ");
					} else {
						init = true;
					}
					buffer.append("communityUserId_s:");
					buffer.append(SolrUtil.escape(communityUserId));
				}
				buffer.append(")");
			}

			for (FacetResult<String> target : solrOperations.facet(
					ReviewDO.class,
					String.class,
					new SolrQuery(
				buffer.toString()).addFacetField(
						"communityUserId_s").setFacetLimit(
								tmpLimit).setFacetMinCount(1))) {
				findCommunityUserIds.add(target.getValue());
			}

			Map<String, CommunityUserDO> resultMap = solrOperations.find(
					CommunityUserDO.class,
					String.class, findCommunityUserIds);
			for(String communityUserId : findCommunityUserIds) {
				subResult.getDocuments().add(resultMap.get(communityUserId));
			}
		}

		return result;
	}

	/**
	 * 指定した商品の次も買いたいに関する集計情報を返します。
	 * @param product 商品
	 * @return 次も買いたいに関する集計情報
	 */
	@Override
	public Map<AlsoBuyProduct, Long> loadAlsoBuyProductSummaryMap(List<String> skus) {
		Map<AlsoBuyProduct, Long> result = new HashMap<AlsoBuyProduct, Long>();
		for (AlsoBuyProduct alsoBuyProduct : AlsoBuyProduct.values()) {
			result.put(alsoBuyProduct, new Long(0L));
		}
		if( skus == null || skus.isEmpty()){
			return result;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false");
		if( skus.size() == 1 ){
			buffer.append(" AND productId_s:");
			buffer.append(SolrUtil.escape(skus.get(0)));
		}else{
			buffer.append(" AND ( ");
			for(int i=0; i<skus.size(); i++){
				if( i > 0)
					buffer.append(" OR ");
				buffer.append(SolrUtil.escape(skus.get(i)));
			}
			buffer.append(" ) ");
		}
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		buffer.append(" AND reviewType_s:");
		buffer.append(SolrUtil.escape(ReviewType.REVIEW_AFTER_FEW_DAYS.getCode()));
		buffer.append(" AND latestReview_b:true");
		
		for (FacetResult<AlsoBuyProduct> facetResult : solrOperations.facet(ReviewDO.class, AlsoBuyProduct.class,
				new SolrQuery(buffer.toString()).setRows(
						SolrConstants.QUERY_ROW_LIMIT).addFacetField("alsoBuyProduct_s"))) {
			result.put(facetResult.getValue(), facetResult.getCount());
		}
		return result;
	}

	/**
	 * 購入の決め手の評価数の合計値をカウントします。
	 * @param product 商品
	 * @return 購入の決め手の評価数の合計値
	 */
	@Override
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=10000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public Long countTotalDecisivePurchaseRatings(
			String sku) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND deleteFlag_b:false AND ");
		buffer.append("sku_s:");
		buffer.append(sku);
		buffer.append(" AND reviewId_s:*");
		buffer.append(" AND temporary_b:false");
		buffer.append(" AND stopFlg_b:false");
		return solrOperations.findByQuery(
				new SolrQuery(buffer.toString()).setRows(0),
				ReviewDecisivePurchaseDO.class).getNumFound();
	}

	/**
	 * 指定したステータスのコミュニティユーザーに紐づくレビュー情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param status ステータス
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @param adultVerification アダルト表示確認ステータス
	 * @return 検索結果
	 */
	@Override
	public SearchResult<ReviewDO> findReviewByCommunityUserId(
			String communityUserId,
			String excludeReviewId,
			int limit,
			Date offsetTime,
			boolean previous,
			Verification adultVerification) {

		//hasAdult対応対象です。
		AdultHelper adultHelper = new AdultHelper(adultVerification);
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();
		// コンテンツの一時停止対応
		if(communityUserId.equals(loginCommunityUserId)) {
			buffer.append(" AND (");
			buffer.append(" status_s:");
			buffer.append(ContentsStatus.SUBMITTED.getCode());
			buffer.append(" OR ");
			buffer.append(" status_s:");
			buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
			buffer.append(" ) ");
		}else{
			buffer.append(" AND status_s:");
			buffer.append(ContentsStatus.SUBMITTED.getCode());
		}

		if(!StringUtils.isEmpty(excludeReviewId))
			buffer.append(" AND !reviewId:" + SolrUtil.escape(excludeReviewId));
		
		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND postDate_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND postDate_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}

		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(buffer.toString()));
		if (offsetTime == null || !previous) {
			query.setSortField("postDate_dt", ORDER.desc);
		} else {
			query.setSortField("postDate_dt", ORDER.asc);
		}
		query.setRows(limit);
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(
				query, ReviewDO.class, getDefaultLoadReviewCondition()));
		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(adultHelper.hasAdult(searchResult.getDocuments()));
			if (!searchResult.isHasAdult()) {
				searchResult.setHasAdult(
						adultHelper.hasAdult(
								buffer.toString(), ReviewDO.class, solrOperations));
			}
		}
		
		List<String> reviewIds = new ArrayList<String>();
		for(ReviewDO checkReview : searchResult.getDocuments()){
			reviewIds.add(checkReview.getReviewId());
		}
		// HBaseからデータを取得する。
		Map<String, ReviewDO> reviewMap = hBaseOperations.find(ReviewDO.class, String.class, reviewIds, getDefaultLoadReviewCondition());
		// Solorで取得した順番どおりに詰めなおす
		if( !reviewMap.isEmpty() ){
			Iterator<Entry<String, ReviewDO>> reviewInterator = reviewMap.entrySet().iterator();
			Entry<String, ReviewDO> entry = null;
			List<ReviewDO> reviews = Lists.newArrayList();
			while( reviewInterator.hasNext() ){
				entry = reviewInterator.next();
				ReviewDO review = entry.getValue();
				fillRelationInfo(review);
				reviews.add(review);
				
			}
			Collections.sort(reviews, new Comparator<ReviewDO>() {
				@Override
				public int compare(ReviewDO o1, ReviewDO o2) {
					return o2.getPostDate().compareTo(o1.getPostDate());
				}
				
			});
			searchResult.setDocuments(reviews);
		}
		
//		List<ReviewDO> reviews = Lists.newArrayList();
//		for (String reviewId : reviewIds) {
//			ReviewDO review = reviewMap.get(reviewId);
//			fillRelationInfo(review);
//			reviews.add(review);
//		}
//		searchResult.setDocuments(reviews);
		
		ProductUtil.filterInvalidProduct(searchResult);
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}
	
	@Override
	public SearchResult<ReviewDO> findTemporaryReviewByCommunityUserId(
			String communityUserId,
			String excludeReviewId,
			int limit,
			Date offsetTime,
			boolean previous,
			Verification adultVerification) {

		//hasAdult対応対象です。
		AdultHelper adultHelper = new AdultHelper(adultVerification);
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND status_s:");
		buffer.append(ContentsStatus.SAVE.getCode());

		if(!StringUtils.isEmpty(excludeReviewId))
			buffer.append(" AND !reviewId:" + SolrUtil.escape(excludeReviewId));
	
		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND saveDate_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND saveDate_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}

		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(buffer.toString()));
		if (offsetTime == null || !previous) {
			query.setSortField("saveDate_dt", ORDER.desc);
		} else {
			query.setSortField("saveDate_dt", ORDER.asc);
		}
		query.setRows(limit);
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(
				query, ReviewDO.class, getDefaultLoadReviewCondition()));
		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(adultHelper.hasAdult(searchResult.getDocuments()));
			if (!searchResult.isHasAdult()) {
				searchResult.setHasAdult(
						adultHelper.hasAdult(
								buffer.toString(), ReviewDO.class, solrOperations));
			}
		}
		
		List<String> reviewIds = new ArrayList<String>();
		for(ReviewDO checkReview : searchResult.getDocuments()){
			reviewIds.add(checkReview.getReviewId());
		}
		// HBaseからデータを取得する。
		Map<String, ReviewDO> reviewMap = hBaseOperations.find(ReviewDO.class, String.class, reviewIds, getDefaultLoadReviewCondition());
		// Solorで取得した順番どおりに詰めなおす
//		List<ReviewDO> reviews = Lists.newArrayList();
//		for (String reviewId : reviewIds) {
//			ReviewDO review = reviewMap.get(reviewId);
//			fillRelationInfo(review);
//			reviews.add(review);
//		}
//		searchResult.setDocuments(reviews);
		
		if( !reviewMap.isEmpty() ){
			Iterator<Entry<String, ReviewDO>> reviewInterator = reviewMap.entrySet().iterator();
			Entry<String, ReviewDO> entry = null;
			List<ReviewDO> reviews = Lists.newArrayList();
			while( reviewInterator.hasNext() ){
				entry = reviewInterator.next();
				ReviewDO review = entry.getValue();
				fillRelationInfo(review);
				reviews.add(review);
				
			}
			Collections.sort(reviews, new Comparator<ReviewDO>() {
				@Override
				public int compare(ReviewDO o1, ReviewDO o2) {
					return o2.getSaveDate().compareTo(o1.getSaveDate());
				}
				
			});
			searchResult.setDocuments(reviews);
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
	 * レビュー数情報を返します。
	 * @param skus SKUリスト
	 * @return レビュー数情報
	 */
	@Override
	public Map<String, Long> loadReviewCountMapBySKU(List<String> skus) {
		Map<String, Long> resultMap = new HashMap<String, Long>();
		if (skus == null || skus.size() == 0) {
			return resultMap;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		buffer.append(" AND (");
		for (int i = 0; i < skus.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("productId_s:");
			buffer.append(SolrUtil.escape(skus.get(i)));
		}
		buffer.append(")");

		SolrQuery solrQuery = new SolrQuery(
				buffer.toString());
		solrQuery.addFacetField("productId_s");
		solrQuery.setFacetLimit(SolrConstants.QUERY_ROW_LIMIT);
		for (FacetResult<String> facetResult : solrOperations.facet(
				ReviewDO.class, String.class, solrQuery)) {
			resultMap.put(facetResult.getValue(), facetResult.getCount());
		}

		return resultMap;
	}

	/**
	 * レビュー数情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param skus SKUリスト
	 * @return レビュー数情報
	 */
	@Override
	public Map<String, Long> loadReviewCountMapByCommunityUserIdAndSKU(
			String communityUserId,
			List<String> skus) {
		Map<String, Long> resultMap = new HashMap<String, Long>();
		if (skus == null || skus.size() == 0) {
			return resultMap;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		buffer.append(" AND communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND (");
		for (int i = 0; i < skus.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("productId_s:");
			buffer.append(SolrUtil.escape(skus.get(i)));
		}
		buffer.append(")");

		SolrQuery solrQuery = new SolrQuery(
				buffer.toString());
		solrQuery.addFacetField("productId_s");
		solrQuery.setFacetLimit(SolrConstants.QUERY_ROW_LIMIT);
		for (FacetResult<String> facetResult : solrOperations.facet(
				ReviewDO.class, String.class, solrQuery)) {
			resultMap.put(facetResult.getValue(), facetResult.getCount());
		}

		return resultMap;
	}

	/**
	 * レビュー数情報を返します。
	 * @param communityUserIds コミュニティユーザーIDのリスト
	 * @return レビュー数情報
	 */
	@Override
	public Map<String, Long> loadReviewCountMapByCommunityUserId(
			List<String> communityUserIds) {
		Map<String, Long> resultMap = new HashMap<String, Long>();
		if (communityUserIds == null || communityUserIds.size() == 0) {
			return resultMap;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" AND (");
		for (int i = 0; i < communityUserIds.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("communityUserId_s:");
			buffer.append(SolrUtil.escape(communityUserIds.get(i)));
		}
		buffer.append(")");

		SolrQuery solrQuery = new SolrQuery(
				new AdultHelper(requestScopeDao.loadAdultVerification(
										)).toFilterQuery(buffer.toString()));
		solrQuery.addFacetField("communityUserId_s");
		solrQuery.setFacetLimit(SolrConstants.QUERY_ROW_LIMIT);
		for (FacetResult<String> facetResult : solrOperations.facet(
				ReviewDO.class, String.class, solrQuery)) {
			resultMap.put(facetResult.getValue(), facetResult.getCount());
		}

		return resultMap;
	}

	/**
	 * 投稿レビュー数を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @return 投稿レビュー数
	 */
	@Override
	public long countPostReviewCount(String communityUserId, String sku) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		if(communityUserId != null){
			buffer.append(" AND communityUserId_s:");
			buffer.append(SolrUtil.escape(communityUserId));
		}
		if (!StringUtils.isEmpty(sku)) {
			buffer.append(" AND productId_s:");
			buffer.append(SolrUtil.escape(sku));
		}

		SolrQuery solrQuery = new SolrQuery(new AdultHelper(
				requestScopeDao.loadAdultVerification(
						)).toFilterQuery(buffer.toString()));
		return solrOperations.count(solrQuery, ReviewDO.class);
	}

	/**
	 * 投稿レビュー数リストを返します。
	 * @param skus SKUリスト
	 * @return 投稿レビュー数リスト
	 */
	@Override
	public Map<String, Long> countPostReviewBySku(String[] skus) {
		
		Asserts.isTrue(skus.length > 0);
		
		Map<String, String> postReviewQueryMap = new HashMap<String,String>();
		SolrQuery solrQuery = new SolrQuery("*:*");
		Map<String, Long> postReviewCountMap = new HashMap<String, Long>();
		for(String sku:skus){
			StringBuilder buffer = new StringBuilder();
			buffer.append("withdraw_b:false AND status_s:");
			buffer.append(SolrUtil.escape(ContentsStatus.SUBMITTED.getCode()));
			if (!StringUtils.isEmpty(sku)) {
				buffer.append(" AND productId_s:");
				buffer.append(SolrUtil.escape(sku));
			}
			String query = buffer.toString();
			postReviewQueryMap.put(query, sku);
			solrQuery.addFacetQuery(query);
		}
		solrQuery.setFacetLimit(solrQuery.getFacetQuery().length);
		solrQuery.setFacetMinCount(0);
		
		for (FacetResult<String> facetResult : solrOperations.facet(ReviewDO.class, String.class, solrQuery)) {
			if(postReviewQueryMap.containsKey(facetResult.getFacetQuery())){
				postReviewCountMap.put(postReviewQueryMap.get(facetResult.getFacetQuery()), facetResult.getCount());
			}
		}
		return postReviewCountMap;
	}
	
	@Override
	public Map<String, Long> loadSameProductReviewCountMap(List<ReviewDO> reviews) {
		Map<String, Long> resultMap = new HashMap<String, Long>();
		if(reviews.isEmpty())
			return resultMap;
		Map<String, String> queryMap = new HashMap<String,String>();
		SolrQuery solrQuery = new SolrQuery("*:*");
		
		for(ReviewDO review : reviews){
			StringBuilder buffer = new StringBuilder();
			buffer.append("withdraw_b:false ");
			buffer.append(" AND ");
			buffer.append("status_s:");
			buffer.append(ContentsStatus.SUBMITTED.getCode());
			if(review.getCommunityUser().getCommunityUserId() != null){
				buffer.append(" AND communityUserId_s:");
				buffer.append(SolrUtil.escape(review.getCommunityUser().getCommunityUserId()));
			}
			if (!StringUtils.isEmpty(review.getProduct().getSku())) {
				buffer.append(" AND productId_s:");
				buffer.append(SolrUtil.escape(review.getProduct().getSku()));
			}
			
			buffer.append(" AND !reviewId:");
			buffer.append(SolrUtil.escape(review.getReviewId()));
			
			String query = buffer.toString();
			queryMap.put(query, review.getReviewId());
			solrQuery.addFacetQuery(query);
		}
		
		solrQuery.setFacetLimit(solrQuery.getFacetQuery().length);
		solrQuery.setFacetMinCount(0);
		
		for (FacetResult<String> facetResult : solrOperations.facet(ReviewDO.class, String.class, solrQuery)) {
			if(queryMap.containsKey(facetResult.getFacetQuery())){
				resultMap.put(queryMap.get(facetResult.getFacetQuery()), facetResult.getCount());
			}
		}
		
		return resultMap;
	}

	/**
	 * 指定したコミュニティユーザーがレビューをした商品の別のレビューを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param publicDate 公開された日付
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return レビューリスト
	 */
	@Override
	public SearchResult<ReviewDO> findAnotherReviewByCommunityUserRreview(
			String communityUserId, Date publicDate, int limit, int offset) {
		SolrQuery query = new SolrQuery(
				"withdraw_b:false AND communityUserId_s:" + SolrUtil.escape(communityUserId)
				+ " AND "
				+ "status_s:"
				+ ContentsStatus.SUBMITTED.getCode());
		query.addFacetField("productId_s");
		query.setFacetLimit(SolrConstants.QUERY_ROW_LIMIT);
		List<FacetResult<String>> products
				= solrOperations.facet(ReviewDO.class, String.class, query);
		if (products.size() == 0) {
			return new SearchResult<ReviewDO>();
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("!communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		buffer.append(" AND (");
		for (int i = 0; i < products.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("productId_s:");
			buffer.append(SolrUtil.escape(products.get(i).getValue()));
		}
		buffer.append(") AND ");
		buffer.append(SolrUtil.getSolrDateRangeQuery("postDate_dt", publicDate));
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
								).toFilterQuery(buffer.toString())).setRows(limit).setStart(
								offset).setSortField("postDate_dt", ORDER.asc),
				ReviewDO.class, Path.includeProp("*").includePath(
						"product.sku,communityUser.communityUserId").depth(1)));
		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}

	@Override
	public SearchResult<ReviewDO> findAnotherReviewByCommunityUserRreviewForMR(
			String communityUserId, Date publicDate, int limit, int offset) {
		SolrQuery query = new SolrQuery(
				"withdraw_b:false AND communityUserId_s:" + SolrUtil.escape(communityUserId)
				+ " AND "
				+ "status_s:"
				+ ContentsStatus.SUBMITTED.getCode());
		query.addFacetField("productId_s");
		query.setFacetLimit(SolrConstants.QUERY_ROW_LIMIT);
		List<FacetResult<String>> products
				= solrOperations.facet(ReviewDO.class, String.class, query);
		if (products.size() == 0) {
			return new SearchResult<ReviewDO>();
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("!communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		buffer.append(" AND (");
		for (int i = 0; i < products.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("productId_s:");
			buffer.append(SolrUtil.escape(products.get(i).getValue()));
		}
		buffer.append(") AND ");
		buffer.append(SolrUtil.getSolrDateRangeQuery("postDate_dt", publicDate));
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
								).toFilterQuery(buffer.toString())).setRows(limit).setStart(
								offset).setSortField("postDate_dt", ORDER.asc),
				ReviewDO.class, Path.includeProp("*").includePath(
						"communityUser.communityUserId").depth(1)));

		List<String> skus = new ArrayList<String>();
		for(ReviewDO review:searchResult.getDocuments()) {
			skus.add(review.getProduct().getSku());
		}
		Map<String, ProductDO> productMap = productDao.findBySkuForMR(skus);
		for(ReviewDO review:searchResult.getDocuments()) {
			review.setProduct(productMap.get(review.getProduct().getSku()));
		}		

		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}
	
	/**
	 * 指定したコミュニティユーザーが指定した日付に投稿したレビューを返します。
	 * @param communityUserIds コミュニティユーザーIDリスト
	 * @param publicDate 公開された日付
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return レビューリスト
	 */
	@Override
	@ArroundSolr
	public SearchResult<ReviewDO> findReviewByCommunityUserIds(
			List<String> communityUserIds, Date publicDate, int limit, int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" AND (");
		for (int i = 0; i < communityUserIds.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("communityUserId_s:");
			buffer.append(SolrUtil.escape(communityUserIds.get(i)));
		}
		buffer.append(") AND ");
		buffer.append(SolrUtil.getSolrDateRangeQuery("postDate_dt", publicDate));
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
						).toFilterQuery(buffer.toString())).setRows(limit
								).setStart(offset).setSortField("postDate_dt", ORDER.asc),
				ReviewDO.class, Path.includeProp("*").includePath(
						"product.sku,communityUser.communityUserId").depth(1)));
		return searchResult;
	}
	
	@Override
	@ArroundSolr
	public SearchResult<ReviewDO> findReviewByCommunityUserIdsForMR(
			List<String> communityUserIds, Date publicDate, int limit, int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" AND (");
		for (int i = 0; i < communityUserIds.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("communityUserId_s:");
			buffer.append(SolrUtil.escape(communityUserIds.get(i)));
		}
		buffer.append(") AND ");
		buffer.append(SolrUtil.getSolrDateRangeQuery("postDate_dt", publicDate));
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
						).toFilterQuery(buffer.toString())).setRows(limit
								).setStart(offset).setSortField("postDate_dt", ORDER.asc),
				ReviewDO.class, Path.includeProp("*").includePath(
						"communityUser.communityUserId").depth(1)));

		List<String> skus = new ArrayList<String>();
		for(ReviewDO review:searchResult.getDocuments()) {
			skus.add(review.getProduct().getSku());
		}
		Map<String, ProductDO> productMap = productDao.findBySkuForMR(skus);
		for(ReviewDO review:searchResult.getDocuments()) {
			review.setProduct(productMap.get(review.getProduct().getSku()));
		}		
		return searchResult;
	}

	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.HttpSession,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityDataSyncWeb}
			)
	public long countReviewByCommunityUserId(
			String communityUserId,
			ContentsStatus status) {
		return countReviewByCommunityUserId(
				communityUserId,
				null,
				new ContentsStatus[]{status},
				requestScopeDao.loadAdultVerification());
	}
	
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.HttpSession,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityDataSyncWeb}
			)
	public long countReviewByCommunityUserIdForMypage(String communityUserId) {
		return countReviewByCommunityUserId(
				communityUserId,
				null,
				new ContentsStatus[]{ContentsStatus.SUBMITTED, ContentsStatus.CONTENTS_STOP},
				requestScopeDao.loadAdultVerification());
	}
	
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.HttpSession,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityDataSyncWeb}
			)
	public long countReviewByCommunityUserId(
			String communityUserId,
			String excludeReviewId,
			ContentsStatus[] statuses,
			Verification adultVerification){
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false");
		buffer.append(" AND communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		if( statuses != null && statuses.length >0 ){
			buffer.append(" AND (");
			for( int i=0; i<statuses.length; i++){
				if( i > 0)
					buffer.append(" OR ");
				buffer.append(" status_s:");
				buffer.append(statuses[i].getCode());
			}
			buffer.append(" ) ");
		}
		if(!StringUtils.isEmpty(excludeReviewId))
			buffer.append(" AND !reviewId:" + excludeReviewId);
		
		SolrQuery solrQuery = new SolrQuery(new AdultHelper(adultVerification).toFilterQuery(buffer.toString()));
		return solrOperations.count(solrQuery, ReviewDO.class);
	}
	/**
	 * 指定した商品、日付に投稿したレビューを返します。
	 * @param skus SKUリスト
	 * @param publicDate 公開された日付
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return レビューリスト
	 */
	@Override
	public SearchResult<ReviewDO> findReviewBySKUs(
			List<String> skus, Date publicDate, String excludeCommunityId, int limit, int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		if(!StringUtils.isEmpty(excludeCommunityId)){
			buffer.append(" AND !communityUserId_s:");
			buffer.append(SolrUtil.escape(excludeCommunityId));
		}
		buffer.append(" AND (");
		for (int i = 0; i < skus.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("productId_s:");
			buffer.append(SolrUtil.escape(skus.get(i)));
		}
		buffer.append(") AND ");
		buffer.append(SolrUtil.getSolrDateRangeQuery("postDate_dt", publicDate));
		SearchResult<ReviewDO> searchResult =  new SearchResult<ReviewDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
						).toFilterQuery(buffer.toString())).setRows(limit).setStart(
								offset).setSortField("postDate_dt", ORDER.asc),
				ReviewDO.class, Path.includeProp("*").includePath(
						"product.sku,communityUser.communityUserId").depth(1)));
		return searchResult;
	}

	@Override
	public SearchResult<ReviewDO> findReviewBySKUsForMR(
			List<String> skus, Date publicDate, String excludeCommunityId, int limit, int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		if(!StringUtils.isEmpty(excludeCommunityId)){
			buffer.append(" AND !communityUserId_s:");
			buffer.append(SolrUtil.escape(excludeCommunityId));
		}
		buffer.append(" AND (");
		for (int i = 0; i < skus.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("productId_s:");
			buffer.append(SolrUtil.escape(skus.get(i)));
		}
		buffer.append(") AND ");
		buffer.append(SolrUtil.getSolrDateRangeQuery("postDate_dt", publicDate));
		SearchResult<ReviewDO> searchResult =  new SearchResult<ReviewDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
						).toFilterQuery(buffer.toString())).setRows(limit).setStart(
								offset).setSortField("postDate_dt", ORDER.asc),
				ReviewDO.class, Path.includeProp("*").includePath(
						"communityUser.communityUserId").depth(1)));
		
		for(ReviewDO review:searchResult.getDocuments()) {
			skus.add(review.getProduct().getSku());
		}
		Map<String, ProductDO> productMap = productDao.findBySkuForMR(skus);
		for(ReviewDO review:searchResult.getDocuments()) {
			review.setProduct(productMap.get(review.getProduct().getSku()));
		}		
		
		return searchResult;
	}
	
	/**
	 * レビュー数情報を返します。
	 * @param skus SKUリスト
	 * @return レビュー数情報
	 */
	@Override
	public Map<String, Long> loadReviewCountMap(List<String> skus) {
		Map<String, Long> resultMap = new HashMap<String, Long>();
		if (skus == null || skus.size() == 0) {
			return resultMap;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		buffer.append(" AND (");
		for (int i = 0; i < skus.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("productId_s:");
			buffer.append(SolrUtil.escape(skus.get(i)));
		}
		buffer.append(")");

		SolrQuery solrQuery = new SolrQuery(buffer.toString());
		solrQuery.addFacetField("productId_s");
		solrQuery.setFacetLimit(SolrConstants.QUERY_ROW_LIMIT);
		for (FacetResult<String> facetResult : solrOperations.facet(
				ReviewDO.class, String.class, solrQuery)) {
			resultMap.put(facetResult.getValue(), facetResult.getCount());
		}

		return resultMap;
	}

	/**
	 * ポイント付与リクエストIDに紐付くレビュー情報を返します。
	 * @param pointGrantRequestId ポイント付与リクエストID
	 * @return レビュー情報
	 */
	@Override
	public ReviewDO loadReviewByPointGrantRequestId(String pointGrantRequestId) {
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(new SolrQuery(
				"pointGrantRequestId_s:" + SolrUtil.escape(pointGrantRequestId)), ReviewDO.class));
		if (searchResult.getNumFound() > 0) {
			return searchResult.getDocuments().get(0);
		} else {
			return null;
		}
	}

	/**
	 * レビュー情報をポイント付与フィードバックのために更新します。
	 */
	@Override
	public void updateReviewForPointGrantFeedback(ReviewDO review) {
		review.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(review,
				Path.includeProp("modifyDateTime,pointGrantStatus,grantPoint"));
	}
	
	/**
	 *  最新レビューフラグの設定を更新します。
	 */
	@Override
	public void updateReviewForLatestReview(ReviewDO review) {
		review.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(review,
				Path.includeProp("modifyDateTime,latestReview"));
	}

	/**
	 * 商品に紐づく購入の決め手を全て返します。
	 * @param sku SKU
	 * @return 検索結果
	 */
	private List<DecisivePurchaseDO> findDecisivePurchaseAllBySKU(
			String sku) {
		return hBaseOperations.scan(
				DecisivePurchaseDO.class,
				IdUtil.getMinBranchId(sku), IdUtil.getMaxBranchId(sku));
	}

	/**
	 * レビュー情報のインデックスを更新します。
	 * @param reviewId レビューID
	 * @param review レビュー
	 * @return レビュー情報
	 */
	private ReviewDO updateReviewInIndex(String reviewId, ReviewDO review) {
		return updateReviewInIndex(reviewId, review, false);
	}

	private ReviewDO updateReviewInIndex(String reviewId, ReviewDO review, boolean mngToolOperation) {
		if (review == null || review.isDeleted()) {
			solrOperations.deleteByQuery(new SolrQuery(
					"reviewId_s:" + SolrUtil.escape(reviewId)), ActionHistoryDO.class);
			solrOperations.deleteByQuery(new SolrQuery(
					"reviewId_s:" + SolrUtil.escape(reviewId)), InformationDO.class);

			if(!mngToolOperation){
				solrOperations.deleteByQuery(new SolrQuery(
						"reviewId_s:" + SolrUtil.escape(reviewId)), LikeDO.class);
				solrOperations.deleteByQuery(new SolrQuery(
						"reviewId_s:" + SolrUtil.escape(reviewId)), VotingDO.class);
			}

			if (review == null) {
				solrOperations.deleteByQuery(new SolrQuery(
						"reviewId_s:" + SolrUtil.escape(reviewId)), CommentDO.class);
				solrOperations.deleteByQuery(new SolrQuery(
						"reviewId_s:" + SolrUtil.escape(reviewId)), SpamReportDO.class);
				solrOperations.deleteByKey(ReviewDO.class, reviewId);
				solrOperations.deleteByQuery(new SolrQuery(
						"reviewId_s:" + SolrUtil.escape(reviewId)), ReviewDecisivePurchaseDO.class);
				solrOperations.deleteByQuery(new SolrQuery(
						"reviewId_s:" + SolrUtil.escape(reviewId)), PurchaseLostProductDO.class);
				solrOperations.deleteByQuery(new SolrQuery(
						"reviewId_s:" + SolrUtil.escape(reviewId)), UsedProductDO.class);
			} else {
				solrOperations.save(CommentDO.class,
						hBaseOperations.scanWithIndex(
								CommentDO.class, "reviewId", reviewId));
				solrOperations.save(review);
				solrOperations.save(ReviewDecisivePurchaseDO.class,
						hBaseOperations.scanWithIndex(
								ReviewDecisivePurchaseDO.class, "reviewId", reviewId));
				solrOperations.save(PurchaseLostProductDO.class,
						hBaseOperations.scanWithIndex(
								PurchaseLostProductDO.class, "reviewId", reviewId));
				solrOperations.save(UsedProductDO.class,
						hBaseOperations.scanWithIndex(
								UsedProductDO.class, "reviewId", reviewId));
				solrOperations.save(SpamReportDO.class,
						hBaseOperations.scanWithIndex(
								SpamReportDO.class, "reviewId", reviewId));
			}
			return null;
		}
		
		List<String> deleteReviewDecisivePurchaseIds = Lists.newArrayList();
		for (ReviewDecisivePurchaseDO oldData : solrOperations.findByQuery(
				new SolrQuery("reviewId_s:" + SolrUtil.escape(reviewId)).setRows(SolrConstants.QUERY_ROW_LIMIT),
				ReviewDecisivePurchaseDO.class).getDocuments()) {
			boolean doDelete = true;
			for (ReviewDecisivePurchaseDO newData : review.getReviewDecisivePurchases()) {
				if (newData.getReviewDecisivePurchaseId().equals(oldData.getReviewDecisivePurchaseId())) {
					doDelete = false;
					break;
				}
			}
			if (doDelete) {
				deleteReviewDecisivePurchaseIds.add(oldData.getReviewDecisivePurchaseId());
			}
		}
		List<String> deletePurchaseLostProductIds = new ArrayList<String>();
		for (PurchaseLostProductDO oldData : solrOperations.findByQuery(
				new SolrQuery("reviewId_s:" + SolrUtil.escape(reviewId)).setRows(SolrConstants.QUERY_ROW_LIMIT),
				PurchaseLostProductDO.class).getDocuments()) {
			boolean doDelete = true;
			for (PurchaseLostProductDO newData : review.getPurchaseLostProducts()) {
				if (newData.getPurchaseLostProductId().equals(oldData.getPurchaseLostProductId())) {
					doDelete = false;
					break;
				}
			}
			if (doDelete) {
				deletePurchaseLostProductIds.add(oldData.getPurchaseLostProductId());
			}
		}
		List<String> deleteUsedProductIds = new ArrayList<String>();
		for (UsedProductDO oldData : solrOperations.findByQuery(
				new SolrQuery("reviewId_s:" + SolrUtil.escape(reviewId)).setRows(SolrConstants.QUERY_ROW_LIMIT),
				UsedProductDO.class).getDocuments()) {
			boolean doDelete = true;
			for (UsedProductDO newData : review.getUsedProducts()) {
				if (newData.getUsedProductId().equals(oldData.getUsedProductId())) {
					doDelete = false;
					break;
				}
			}
			if (doDelete) {
				deleteUsedProductIds.add(oldData.getUsedProductId());
			}
		}

		if (!deleteReviewDecisivePurchaseIds.isEmpty()) {
			solrOperations.deleteByKeys(ReviewDecisivePurchaseDO.class,
					String.class, deleteReviewDecisivePurchaseIds);
		}
		if (!deletePurchaseLostProductIds.isEmpty()) {
			solrOperations.deleteByKeys(PurchaseLostProductDO.class,
					String.class, deletePurchaseLostProductIds);
		}
		if (!deleteUsedProductIds.isEmpty()) {
			solrOperations.deleteByKeys(UsedProductDO.class,
					String.class, deleteUsedProductIds);
		}

		List<DecisivePurchaseDO> decisivePurchaseList = new ArrayList<DecisivePurchaseDO>();

		//購入の決め手は未登録の分だけ登録します。
		List<DecisivePurchaseDO> newDecisivePurchases = new ArrayList<DecisivePurchaseDO>();
		if (!ContentsStatus.SAVE.equals(review.getStatus())) {
			decisivePurchaseList.addAll(solrOperations.findByQuery(
					new SolrQuery("sku_s:" + SolrUtil.escape(review.getProduct().getSku())).setRows(SolrConstants.QUERY_ROW_LIMIT),
					DecisivePurchaseDO.class).getDocuments());
			for (ReviewDecisivePurchaseDO newData : review.getReviewDecisivePurchases()) {
				boolean doAdd = true;
				for (DecisivePurchaseDO oldData : decisivePurchaseList) {
					if (newData.getDecisivePurchase().getDecisivePurchaseId().equals(oldData.getDecisivePurchaseId())
							&&
							!newData.isDeleteFlag()
							&&
							!oldData.isDeleteFlg()
							) {
						doAdd = false;
						break;
					}
				}
				if (doAdd) {
					newDecisivePurchases.add(newData.getDecisivePurchase());
				}
			}
		}
		if (newDecisivePurchases.size() > 0) {
			solrOperations.save(DecisivePurchaseDO.class, newDecisivePurchases);
		}
		solrOperations.save(review, Path.includeProp("*").includeRelation(
				ReviewDecisivePurchaseDO.class,
				PurchaseLostProductDO.class,
				UsedProductDO.class).depth(1));

		return review;
	}

	/**
	 * レビューの購入の決め手情報を更新します。
	 * @param review レビュー
	 */
	private void updateReviewDecisivePurchase(ReviewDO review) {
		List<ReviewDecisivePurchaseDO> oldList
				= hBaseOperations.scan(ReviewDecisivePurchaseDO.class,
				IdUtil.getMinBranchId(review.getReviewId()),
				IdUtil.getMaxBranchId(review.getReviewId()),
				Path.includeProp("*").includeRelation(DecisivePurchaseDO.class));
		// 購入の決め手が未登録の場合、決め手情報を以下のルールで寄せる。
		for (ReviewDecisivePurchaseDO reviewDecisivePurchase : review.getReviewDecisivePurchases()) {
			if (StringUtils.isEmpty(reviewDecisivePurchase.getDecisivePurchase().getDecisivePurchaseId())) {
				reviewDecisivePurchase.getDecisivePurchase().normalize();
			}
		}
		List<ReviewDecisivePurchaseDO> _newList
				= review.getReviewDecisivePurchases();
		List<String> removeIdList = new ArrayList<String>();
		List<String> oldIdList = new ArrayList<String>();
		// 自由入力かつ同一文言の値を集約
		Set<String> newDataKeySet = new HashSet<String>();
		List<ReviewDecisivePurchaseDO> newList = new ArrayList<ReviewDecisivePurchaseDO>();
		for (ReviewDecisivePurchaseDO newData : _newList) {
			String decisivePurchaseName = null;
			if (null != newData.getDecisivePurchase()) {
				decisivePurchaseName = newData.getDecisivePurchase().getDecisivePurchaseName();
			}
			if (null == decisivePurchaseName) {
				continue;
			}
			if (!newDataKeySet.contains(decisivePurchaseName)) {
				newDataKeySet.add(decisivePurchaseName);
				newList.add(newData);
			}
		}
		
		for (ReviewDecisivePurchaseDO oldData : oldList) {
			oldIdList.add(oldData.getReviewDecisivePurchaseId());
			boolean doDelete = true;
			for (ReviewDecisivePurchaseDO newData : newList) {
				boolean freeInput = StringUtils.isEmpty(newData.getDecisivePurchase().getDecisivePurchaseId());
				//購入の決め手の自由入力情報が保存済みの購入の決め手の自由入力情報と一致した場合、
				//削除対象から外します。
				if (oldData.getDecisivePurchase() == null && freeInput &&
						oldData.getTemporaryDecisivePurchaseName().equals(newData.getDecisivePurchase().getDecisivePurchaseName())) {
					// 登録済みの購入の決め手と取替え
					newData.setReviewDecisivePurchaseId(oldData.getReviewDecisivePurchaseId());
					doDelete = false;
					break;
				}
				//保存済みの自由入力情報に一致しない自由入力情報は、新規登録なのでスキップします。
				if (freeInput) {
					continue;
				}
				//購入の決め手が一致するものは、更新なので削除対象から外します。
				if (oldData.getDecisivePurchase() != null &&
						oldData.getDecisivePurchase().getDecisivePurchaseId().equals(newData.getDecisivePurchase().getDecisivePurchaseId())) {
					doDelete = false;
					newData.setReviewDecisivePurchaseId(oldData.getReviewDecisivePurchaseId());
					break;
				}
			}
			if (doDelete) {
				removeIdList.add(oldData.getReviewDecisivePurchaseId());
			}
		}
		
		int nextBranchNo = IdUtil.getMaxBranchNo(review.getReviewId(), oldIdList) + 1;

		List<DecisivePurchaseDO> decisivePurchaseAll = null;
		int decisivePurchaseNextId = 0;
		for (ReviewDecisivePurchaseDO newData : newList) {
			newData.setReview(review);
			newData.setSku(review.getProduct().getSku());
			newData.setCommunityUser(review.getCommunityUser());
			newData.setTemporary(review.getStatus().equals(ContentsStatus.SAVE));
			newData.setEffective(review.isEffective());
			newData.setPurchaseDate(review.getPurchaseDate());
			newData.setRegisterDateTime(timestampHolder.getTimestamp());
			newData.setModifyDateTime(timestampHolder.getTimestamp());
			if (StringUtils.isEmpty(newData.getReviewDecisivePurchaseId())) {
				newData.setReviewDecisivePurchaseId(IdUtil.createIdByBranchNo(review.getReviewId(), nextBranchNo));
				nextBranchNo++;
			}
			//自由入力かつ既に存在する購入の決め手の場合は、差し替えます。
			if (StringUtils.isEmpty(newData.getDecisivePurchase().getDecisivePurchaseId())) {
				if (decisivePurchaseAll == null) {
					decisivePurchaseAll = findDecisivePurchaseAllBySKU(review.getProduct().getSku());
					decisivePurchaseNextId = decisivePurchaseAll.size();
				}
				DecisivePurchaseDO decisivePurchase = null;
				for (DecisivePurchaseDO oldData : decisivePurchaseAll) {
					if (oldData.getDecisivePurchaseName().equals(newData.getDecisivePurchase().getDecisivePurchaseName())) {
						decisivePurchase = oldData;
						break;
					}
				}
				if (decisivePurchase != null) {
					// 既に削除済みとなっている購入の決め手と同一の文言の場合復帰する
					if( decisivePurchase.isDeleteFlg() ){
						decisivePurchase.setDeleteFlg(false);
						decisivePurchase.setModifyDateTime(timestampHolder.getTimestamp());
						hBaseOperations.save(decisivePurchase);
					}
					newData.setDecisivePurchase(decisivePurchase);
				} else {
					//一時保存かつまだ存在しない購入の決め手の場合は、テンポラリ領域に保存します。
					if (review.getStatus().equals(ContentsStatus.SAVE)) {
						newData.setTemporaryDecisivePurchaseName(newData.getDecisivePurchase().getDecisivePurchaseName());
						newData.setDecisivePurchase(null);
					} else {
						//投稿かつまだ存在しない購入の決め手の場合は、登録します。
						decisivePurchase = newData.getDecisivePurchase();
						decisivePurchase.setDecisivePurchaseId(
								IdUtil.createIdByBranchNo(review.getProduct().getSku(), decisivePurchaseNextId));
						decisivePurchaseNextId++;
						decisivePurchase.setRegisterDateTime(timestampHolder.getTimestamp());
						decisivePurchase.setModifyDateTime(timestampHolder.getTimestamp());
						decisivePurchase.setSku(review.getProduct().getSku());
						hBaseOperations.save(decisivePurchase);
					}
					newData.setDecisivePurchase(decisivePurchase);
				}
			}
		}
		hBaseOperations.deleteByKeys(ReviewDecisivePurchaseDO.class, String.class, removeIdList);
		hBaseOperations.save(ReviewDecisivePurchaseDO.class, newList);
		review.setReviewDecisivePurchases(newList);
	}

	/**
	 * レビューの購入に迷った商品情報を更新します。
	 * @param review レビュー
	 * @param productMap 商品マップ
	 */
	private void updatePurchaseLostProduct(
			ReviewDO review,
			Map<String, ProductDO> productMap) {
		List<PurchaseLostProductDO> oldList= hBaseOperations.scan(
				PurchaseLostProductDO.class,
				IdUtil.getMinBranchId(review.getReviewId()),
				IdUtil.getMaxBranchId(review.getReviewId()));
		List<PurchaseLostProductDO> newList
				= review.getPurchaseLostProducts();
		List<String> removeIdList = new ArrayList<String>();
		List<String> oldIdList = new ArrayList<String>();
		for (PurchaseLostProductDO oldData : oldList) {
			oldIdList.add(oldData.getPurchaseLostProductId());
			boolean doDelete = true;
			for (PurchaseLostProductDO newData : newList) {
				boolean freeInput = newData.getProduct() == null;
				//商品名入力が保存済みの商品名入力情報と一致した場合、
				//削除対象から外します。
				if (oldData.getProduct() == null &&
						freeInput &&
						oldData.getProductName().equals(newData.getProductName())) {
					newData.setPurchaseLostProductId(oldData.getPurchaseLostProductId());
					doDelete = false;
					break;
				}
				//保存済みの商品名入力に一致しない商品名入力は、新規登録なのでスキップします。
				if (freeInput) {
					continue;
				}
				//SKUが一致するものは、更新なので削除対象から外します。
				if (oldData.getProduct() != null &&
						oldData.getProduct().getSku().equals(newData.getProduct().getSku())) {
					doDelete = false;
					newData.setPurchaseLostProductId(oldData.getPurchaseLostProductId());
					break;
				}
			}
			if (doDelete) {
				removeIdList.add(oldData.getPurchaseLostProductId());
			}
		}
		int nextBranchNo = IdUtil.getMaxBranchNo(review.getReviewId(), oldIdList) + 1;
		List<String> skus = Lists.newArrayList();
		for (PurchaseLostProductDO newData : newList) {
			newData.setReview(review);
			newData.setCommunityUser(review.getCommunityUser());
			newData.setTemporary(ContentsStatus.SAVE.equals(review.getStatus()));
			newData.setEffective(review.isEffective());
			newData.setReviewProductId(review.getProduct().getSku());
			newData.setRegisterDateTime(timestampHolder.getTimestamp());
			newData.setModifyDateTime(timestampHolder.getTimestamp());
			if (newData.getPurchaseLostProductId() == null) {
				newData.setPurchaseLostProductId(
						IdUtil.createIdByBranchNo(review.getReviewId(), nextBranchNo));
				nextBranchNo++;
			}
			if (newData.getProduct() != null) {
				skus.add(newData.getProduct().getSku());
			}
		}
		if (!skus.isEmpty()) {
			productMap.putAll(productDao.findBySku(skus));
			for (PurchaseLostProductDO newData : newList) {
				if (newData.getProduct() != null) {
					ProductDO product = productMap.get(newData.getProduct().getSku());
					newData.setAdult(product.isAdult());
					newData.setProduct(product);
				}
			}
		}
		hBaseOperations.deleteByKeys(PurchaseLostProductDO.class, String.class, removeIdList);
		hBaseOperations.save(PurchaseLostProductDO.class, newList);
	}

	/**
	 * レビューの過去に使用した商品情報を更新します。
	 * @param review レビュー
	 * @param productMap 商品マップ
	 */
	private void updateUsedProduct(
			ReviewDO review,
			Map<String, ProductDO> productMap) {
		review.getUsedProducts();
		List<UsedProductDO> oldList = hBaseOperations.scan(
				UsedProductDO.class,
				IdUtil.getMinBranchId(review.getReviewId()),
				IdUtil.getMaxBranchId(review.getReviewId()));
		List<UsedProductDO> newList = review.getUsedProducts();
		List<String> removeIdList = Lists.newArrayList();
		List<String> oldIdList = Lists.newArrayList();
		for (UsedProductDO oldData : oldList) {
			oldIdList.add(oldData.getUsedProductId());
			boolean doDelete = true;
			for (UsedProductDO newData : newList) {
				boolean freeInput = newData.getProduct() == null;
				//商品名入力が保存済みの商品名入力情報と一致した場合、
				//削除対象から外します。
				if (oldData.getProduct() == null && 
						freeInput &&
						oldData.getProductName().equals(newData.getProductName())) {
					newData.setUsedProductId(oldData.getUsedProductId());
					doDelete = false;
					break;
				}
				//保存済みの商品名入力に一致しない商品名入力は、新規登録なのでスキップします。
				if (freeInput) {
					continue;
				}
				//SKUが一致するものは、更新なので削除対象から外します。
				if (oldData.getProduct() != null &&
						oldData.getProduct().getSku().equals(newData.getProduct().getSku())) {
					doDelete = false;
					newData.setUsedProductId(oldData.getUsedProductId());
					break;
				}
			}
			if (doDelete) {
				removeIdList.add(oldData.getUsedProductId());
			}
		}
		int nextBranchNo = IdUtil.getMaxBranchNo(review.getReviewId(), oldIdList) + 1;
		List<String> skus = Lists.newArrayList();
		for (UsedProductDO newData : newList) {
			newData.setReview(review);
			newData.setCommunityUser(review.getCommunityUser());
			newData.setTemporary(ContentsStatus.SAVE.equals(review.getStatus()));
			newData.setEffective(review.isEffective());
			newData.setReviewProductId(review.getProduct().getSku());
			newData.setRegisterDateTime(timestampHolder.getTimestamp());
			newData.setModifyDateTime(timestampHolder.getTimestamp());
			if (newData.getUsedProductId() == null) {
				newData.setUsedProductId(
						IdUtil.createIdByBranchNo(review.getReviewId(), nextBranchNo));
				nextBranchNo++;
			}
			if (newData.getProduct() != null) {
				if (productMap.containsKey(newData.getProduct().getSku())) {
					ProductDO product = productMap.get(newData.getProduct().getSku());
					newData.setAdult(product.isAdult());
					newData.setProduct(product);
				} else {
					skus.add(newData.getProduct().getSku());
				}
			}
		}
		if (!skus.isEmpty()) {
			productMap.putAll(productDao.findBySku(skus));
			for (UsedProductDO newData : newList) {
				if (newData.getProduct() != null) {
					ProductDO product = productMap.get(newData.getProduct().getSku());
					newData.setAdult(product.isAdult());
					newData.setProduct(product);
				}
			}
		}
		hBaseOperations.deleteByKeys(UsedProductDO.class, String.class, removeIdList);
		hBaseOperations.save(UsedProductDO.class, newList);
	}

	/**
	 * 関連データを埋めます。
	 * @param review レビュー
	 */
	private void fillRelationInfo(ReviewDO review) {
		if (review != null) {
			//購入の決め手の中に一時保存のデータがあれば、それを復元します。
			
			if (review.getReviewDecisivePurchases() != null) {
				List<ReviewDecisivePurchaseDO> reviewDecisivePurchases = new ArrayList<ReviewDecisivePurchaseDO>();
				for (ReviewDecisivePurchaseDO target : review.getReviewDecisivePurchases()) {
					target.setCommunityUser(review.getCommunityUser());
					if (target.getDecisivePurchase() == null) {
						DecisivePurchaseDO decisivePurchase = new DecisivePurchaseDO();
						decisivePurchase.setDecisivePurchaseName(target.getTemporaryDecisivePurchaseName());
						decisivePurchase.setSku(review.getProduct().getSku());
						target.setDecisivePurchase(decisivePurchase);
					}
					if(target.isStopFlg()) continue;
					reviewDecisivePurchases.add(target);
				}
				review.setReviewDecisivePurchases(reviewDecisivePurchases);
			}
			if (review.getPurchaseLostProducts() != null) {
				List<PurchaseLostProductDO> purchaseLostProducts = new ArrayList<PurchaseLostProductDO>();
				for (PurchaseLostProductDO target : review.getPurchaseLostProducts()) {
					target.setCommunityUser(review.getCommunityUser());
					if(target.isStopFlg()) continue;
					purchaseLostProducts.add(target);
				}
				review.setPurchaseLostProducts(purchaseLostProducts);
			}
			if (review.getUsedProducts() != null) {
				List<UsedProductDO> usedProducts = new ArrayList<UsedProductDO>();
				for (UsedProductDO target : review.getUsedProducts()) {
					target.setCommunityUser(review.getCommunityUser());
					if(target.isStopFlg()) continue;
					usedProducts.add(target);
				}
				review.setUsedProducts(usedProducts);
			}
		}
	}

	/**
	 * レビュー情報を読み出すデフォルト条件を返します。
	 * @return レビュー情報の読み出し条件
	 */
	private Condition getDefaultLoadReviewCondition() {
		return Path.includeProp("*").includePath(
				"communityUser.communityUserId,product.sku," +
				"reviewDecisivePurchases.decisivePurchase.decisivePurchaseId," +
				"purchaseLostProducts.product.sku," +
				"usedProducts.product.sku,mngToolOperation").depth(2);
	}

	@Override
	public SearchResult<ReviewDO> findTemporaryReviewByBeforeInterval(
			Date intervalDate) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND status_s:");
		buffer.append(SolrUtil.escape(ContentsStatus.SAVE.getCode()));
		buffer.append(" AND pointBaseDate_dt:{" +
				"* TO " + DateUtil.getThreadLocalDateFormat().format(intervalDate) + "}");
		return new SearchResult<ReviewDO>(
				solrOperations.findByQuery(new SolrQuery(buffer.toString()),ReviewDO.class));
	}

	public void removeReviews(List<String> reviewIds){
		hBaseOperations.deleteByKeys(ReviewDO.class, String.class, reviewIds);
		solrOperations.deleteByKeys(ReviewDO.class, String.class, reviewIds);
	}

	/**
	 * 指定したコミュニティユーザーの保存レビューを削除します。
	 * @param reviewId レビューID
	 * @param logical 論理削除かどうか
	 */
	@Override
	public void removeTemporaryReview(String communityUserId) {
		List<ReviewDO> reviews = hBaseOperations.findWithIndex(ReviewDO.class, "communityUserId",Path.includeProp("reviewId,status"), communityUserId);
		if(reviews == null || reviews.isEmpty()) return;
		List<String> temporaryReviewIds = new ArrayList<String>();
		for(ReviewDO review:reviews ){
			if(review.getStatus().equals(ContentsStatus.SAVE))
				temporaryReviewIds.add(review.getReviewId());
		}
		if(!temporaryReviewIds.isEmpty()){
			hBaseOperations.deleteByKeys(ReviewDO.class, String.class, temporaryReviewIds);
			solrOperations.deleteByKeys(ReviewDO.class, String.class, temporaryReviewIds);
		}
	}

	@Override
	public String findProductSku(String reviewId) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("reviewId:");
		buffer.append(reviewId);
		
		SearchResult<ReviewDO> results = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(
						new SolrQuery(buffer.toString()),
						ReviewDO.class,
						Path.includeProp("*").includePath("product.sku").depth(1)));
		
		ProductUtil.filterInvalidProduct(results);
		if(results == null || results.getDocuments().isEmpty() || results.getDocuments().size() > 1 || results.getDocuments().get(0).getProduct() == null)
			return null;
		
		return results.getDocuments().get(0).getProduct().getSku();
	}


}
