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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.common.util.DateUtil;
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
import com.kickmogu.yodobashi.community.resource.cache.CacheStrategyType;
import com.kickmogu.yodobashi.community.resource.cache.MethodCache;
import com.kickmogu.yodobashi.community.resource.cache.TargetSystemType;
import com.kickmogu.yodobashi.community.resource.dao.InformationDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.util.AdultHelper;
import com.kickmogu.yodobashi.community.resource.dao.util.ProductUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.SolrUtil;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.SpamReportDO;
import com.kickmogu.yodobashi.community.resource.domain.VotingDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.EditorVersions;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.Verification;


/**
 * 質問 DAO の実装です。
 * @author kamiike
 *
 */
@Service
public class QuestionDaoImpl implements QuestionDao {

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

	@Autowired
	protected InformationDao informationDao;
	
	/**
	 * 商品 DAO です。
	 */
	@Autowired @Qualifier("catalog")
	protected ProductDao productDao;
	
	/**
	 * 指定した条件の質問情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @return 質問情報リスト
	 */
	@Override
	public List<QuestionDO> findQuestionByCommunityUserIdAndSKU(
			String communityUserId, String sku) {
		return findQuestionByCommunityUserIdAndSKU(
				communityUserId,
				sku,
				getDefaultLoadQuestionCondition());
	}

	/**
	 * 指定した条件の質問情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @param condition 条件
	 * @return 質問情報リスト
	 */
	@Override
	public List<QuestionDO> findQuestionByCommunityUserIdAndSKU(
			String communityUserId,
			String sku,
			Condition condition) {
		List<QuestionDO> searchResult = hBaseOperations.scanWithIndex(
				QuestionDO.class, "communityUserId", communityUserId,
				hBaseOperations.createFilterBuilder(QuestionDO.class).appendSingleColumnValueFilter("productId", CompareOp.EQUAL, sku).toFilter(),
				condition);
		ProductUtil.filterInvalidProduct(searchResult);
		
		return searchResult;
	}

	/**
	 * 指定した質問情報を返します。
	 * @param questionId 質問ID
	 * @param condition 条件
	 * @param withLock ロックを取得するかどうか
	 * @return 質問情報
	 */
	@Override
	public QuestionDO loadQuestion(String questionId, Condition condition, boolean withLock) {
		QuestionDO question = null;
		if (withLock) {
			question = hBaseOperations.loadWithLock(QuestionDO.class, questionId,
					condition);
		} else {
			question = hBaseOperations.load(QuestionDO.class, questionId,
					condition);
		}
		return question;
	}

	/**
	 * 質問情報を保存します。
	 * @param question 質問
	 */
	@Override
	public void saveQuestion(QuestionDO question) {
		question.setSaveDate(timestampHolder.getTimestamp());
		question.setModifyDateTime(timestampHolder.getTimestamp());
		question.setLastAnswerDate(loadLastAnswerDateFromIndex(question.getQuestionId()));
		// エディターのバージョン設定
		question.setEditorVersion(EditorVersions.TEXT_EDITOR);
		hBaseOperations.save(question);
	}

	/**
	 * 質問情報の最終回答日時を更新します。
	 * @param questionId 質問ID
	 * @param newLastAnswerDate 新しい最終回答日時
	 */
	@Override
	public void updateQuestionLastAnswerDate(
			String questionId,
			Date newLastAnswerDate) {
		QuestionDO question = new QuestionDO();
		Date lastAnswerDate = loadLastAnswerDateFromIndex(questionId);
		if (lastAnswerDate == null
				|| (newLastAnswerDate != null && lastAnswerDate.before(
				newLastAnswerDate))) {
			lastAnswerDate = newLastAnswerDate;
		}
		question.setQuestionId(questionId);
		question.setModifyDateTime(timestampHolder.getTimestamp());
		question.setSaveDate(timestampHolder.getTimestamp());
		question.setLastAnswerDate(lastAnswerDate);
		hBaseOperations.save(question, Path.includeProp(
				"lastAnswerDate,modifyDateTime"));
	}

	/**
	 * 指定した質問を削除します。
	 * @param questionId 質問ID
	 * @param logical 論理削除かどうか
	 */
	@Override
	public void deleteQuestion(
			String questionId,
			boolean logical,
			 boolean mngToolOperation) {
		if (logical) {
			QuestionDO question = new QuestionDO();
			question.setQuestionId(questionId);
			question.setStatus(ContentsStatus.DELETE);
			question.setMngToolOperation(mngToolOperation);
			question.setDeleteDate(timestampHolder.getTimestamp());
			question.setModifyDateTime(timestampHolder.getTimestamp());

			//質問フォロー、違反報告、回答、いいね、コメントを削除
			//ActionHistoryDO
			hBaseOperations.scanUpdateWithIndex(
					ActionHistoryDO.class, "questionId", questionId,
					UpdateColumns.set("deleteFlag", true
							).andSet("deleteDate", timestampHolder.getTimestamp())
							.andSet("modifyDateTime", timestampHolder.getTimestamp()));
			//InformationDO
			hBaseOperations.scanUpdateWithIndex(
					InformationDO.class, "questionId", questionId,
					UpdateColumns.set("deleteFlag", true
							).andSet("deleteDate", timestampHolder.getTimestamp())
							.andSet("modifyDateTime", timestampHolder.getTimestamp()));
			//QuestionFollowDO
			hBaseOperations.scanDeleteWithIndex(
					QuestionFollowDO.class, "followQuestionId", questionId);
			//CommentDO
			hBaseOperations.scanUpdateWithIndex(
					CommentDO.class, "questionId", questionId,
					UpdateColumns.set("deleteFlag", true
							).andSet("deleteDate", timestampHolder.getTimestamp())
							.andSet("modifyDateTime", timestampHolder.getTimestamp()));
			//LikeDO
			hBaseOperations.scanDeleteWithIndex(
					LikeDO.class, "questionId", questionId);
			//VotingDO
			hBaseOperations.scanDeleteWithIndex(
					VotingDO.class, "questionId", questionId);
			//SpamReportDO
			hBaseOperations.scanUpdateWithIndex(
					SpamReportDO.class, "questionId", questionId,
					UpdateColumns.set("status", SpamReportStatus.DELETE
							).andSet("deleteDate", timestampHolder.getTimestamp())
							.andSet("modifyDateTime", timestampHolder.getTimestamp()));

			//QuestionAnswerDO
			hBaseOperations.scanUpdateWithIndex(
					QuestionAnswerDO.class, "questionId", questionId,
					UpdateColumns.set("status", ContentsStatus.DELETE
							).andSet("deleteDate", timestampHolder.getTimestamp())
							.andSet("modifyDateTime", timestampHolder.getTimestamp()));

			hBaseOperations.save(question, Path.includeProp("status,deleteDate,modifyDateTime,mngToolOperation"));
		} else {
			hBaseOperations.deleteByKey(QuestionDO.class, questionId);
		}
	}

	/**
	 * 質問のスコア情報と閲覧数をインデックスも合わせて更新します。
	 * @param question 質問
	 */
	private BulkUpdate<QuestionDO> bulkUpdate = null;
	@Override
	public void updateQuestionScoreAndViewCountWithIndexForBatch(
			QuestionDO question) {
		question.setModifyDateTime(timestampHolder.getTimestamp());
		bulkUpdate.write(question);
	}
	@Override
	public void updateQuestionScoreAndViewCountWithIndexForBatchBegin(int bulkSize) {
		bulkUpdate = new BulkUpdate<QuestionDO>(QuestionDO.class, 
				hBaseOperations, solrOperations,
				Path.includeProp("questionScore,viewCount,modifyDateTime"), bulkSize);
	}
	@Override
	public void updateQuestionScoreAndViewCountWithIndexForBatchEnd() {
		bulkUpdate.end();
	}
	
	/**
	 * 質問のスコア情報と閲覧数をインデックスも合わせて更新します。
	 * @param question 質問
	 */
	@Override
	public void updateQuestionScoreAndViewCountWithIndex(
			QuestionDO question) {
		question.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(question,
				Path.includeProp("questionScore,viewCount,modifyDateTime"));
		updateQuestionInIndex(question.getQuestionId(), question, false);
	}
	
	@Override
	public long countQuestionBySku(String sku){
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND productId_s:" + sku);
		buffer.append(" AND saveDate_dt:" + SolrConstants.QUERY_DATE_TO_NOW);
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");

		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(0);

		SearchResult<QuestionDO> searchResult = new SearchResult<QuestionDO>(
				solrOperations.findByQuery(query, QuestionDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId").depth(1)));
		
		ProductUtil.filterInvalidProduct(searchResult);
		
		return searchResult.getNumFound();
	}
	
	@Override
	public long countQuestionBySkus(List<String> skus){
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
		buffer.append(" AND saveDate_dt:" + SolrConstants.QUERY_DATE_TO_NOW);
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");

		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(0);

		SearchResult<QuestionDO> searchResult = new SearchResult<QuestionDO>(
				solrOperations.findByQuery(query, QuestionDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId").depth(1)));
		
		ProductUtil.filterInvalidProduct(searchResult);
		
		return searchResult.getNumFound();
	}

	/**
	 * 指定した商品に対するQA情報件数リストを返します。
	 * @param skus SKUリスト
	 * @return QA情報件数リスト
	 */
	@Override
	public Map<String, Long> countQuestionBySku(
			String[] skus) {
		
		Asserts.isTrue(skus.length > 0);
		
		Map<String, String> questionQueryMap = new HashMap<String,String>();
		SolrQuery solrQuery = new SolrQuery("*:*");
		Map<String, Long> questionCountMap = new HashMap<String, Long>();
		
		for(String sku:skus){
			StringBuilder buffer = new StringBuilder();
			buffer.append("withdraw_b:false AND productId_s:" + sku);
			buffer.append(" AND saveDate_dt:" + SolrConstants.QUERY_DATE_TO_NOW);
			buffer.append(" AND status_s:");
			buffer.append(ContentsStatus.SUBMITTED.getCode());
			String query = buffer.toString();
			questionQueryMap.put(query, sku);
			solrQuery.addFacetQuery(query);
		}
		solrQuery.setFacetLimit(solrQuery.getFacetQuery().length);
		solrQuery.setFacetMinCount(0);
		
		for (FacetResult<String> facetResult : solrOperations.facet(QuestionDO.class, String.class, solrQuery)) {
			if(questionQueryMap.containsKey(facetResult.getFacetQuery())){
				questionCountMap.put(questionQueryMap.get(facetResult.getFacetQuery()), facetResult.getCount());
			}
		}
		return questionCountMap;
	}
	
	/**
	 * 指定した商品に対するQA情報を更新日時（質問・回答の最終更新日時）順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	@Override
	public SearchResult<QuestionDO> findUpdateQuestionBySku(
			String sku,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND productId_s:" + sku);
		// TODO 他のコンテンツは,saveDate_dtを見ていない。
		//buffer.append(" AND saveDate_dt:" + SolrConstants.QUERY_DATE_TO_NOW);
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");

		if(!StringUtils.isEmpty(excludeQuestionId)) {
			buffer.append(" AND !questionId:" + excludeQuestionId);
		}

		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND modifyDateTime_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND modifyDateTime_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("modifyDateTime_dt", ORDER.desc);
		} else {
			query.setSortField("modifyDateTime_dt", ORDER.asc);
		}
		SearchResult<QuestionDO> searchResult = new SearchResult<QuestionDO>(
				solrOperations.findByQuery(query, QuestionDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId").depth(1)));
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}
	
	/**
	 * 指定した商品に対するQA情報を質問投稿日時順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	@Override
	public SearchResult<QuestionDO> findNewQuestionBySku(
			String sku,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous) {
		return findNewQuestionBySku(
				sku,
				null,
				excludeQuestionId,
				limit,
				offsetTime,
				previous);
	}
	
	@Override
	public SearchResult<QuestionDO> findNewQuestionBySku(
			String sku,
			String excludeCommunityUserId,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND productId_s:" + sku);
		if( !StringUtils.isBlank(excludeCommunityUserId) )
			buffer.append(" AND !communityUserId_s:" + SolrUtil.escape(excludeCommunityUserId));
		if(!StringUtils.isBlank(excludeQuestionId))
			buffer.append(" AND !questionId:" + excludeQuestionId);
		buffer.append(" AND !lastAnswerDate_dt:" + SolrConstants.QUERY_DATE_TO_NOW);
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
		SearchResult<QuestionDO> searchResult = new SearchResult<QuestionDO>(
				solrOperations.findByQuery(query, QuestionDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId").depth(1)));
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}

	/**
	 * 指定した商品に対するQA情報を回答なし・質問投稿日時順（降順）に返します。
	 * @param sku SKU
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param excludeQuestionId 除外する質問ID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	@Override
	public SearchResult<QuestionDO> findNewQuestionWithNotAnswerPriorityBySku(
			String sku,
			String excludeCommunityUserId,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND productId_s:" + sku);
		if( !StringUtils.isBlank(excludeCommunityUserId) ) {
			buffer.append(" AND !communityUserId_s:" + SolrUtil.escape(excludeCommunityUserId));
		}
		if(!StringUtils.isBlank(excludeQuestionId)) {
			buffer.append(" AND !questionId:" + excludeQuestionId);
		}
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
			query.setSortField("lastAnswerDate_dt", ORDER.asc);
			query.setSortField("postDate_dt", ORDER.desc);
		} else {
			query.setSortField("lastAnswerDate_dt", ORDER.desc);
			query.setSortField("postDate_dt", ORDER.asc);
		}
		SearchResult<QuestionDO> searchResult = new SearchResult<QuestionDO>(
				solrOperations.findByQuery(query, QuestionDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId").depth(1)));
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}
	/**
	 * 指定した商品に対するQA情報を盛り上がり順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetQuestionScore 検索開始スコア
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	@Override
	public SearchResult<QuestionDO> findPopularQuestionBySku(
			String sku,
			String excludeQuestionId,
			int limit,
			Double offsetQuestionScore,
			Date offsetTime,
			boolean previous) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND productId_s:" + sku);
		buffer.append(" AND lastAnswerDate_dt:" +SolrConstants.QUERY_DATE_TO_NOW);
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");

		if(!StringUtils.isEmpty(excludeQuestionId)) {
			buffer.append(" AND !questionId:" + excludeQuestionId);
		}

		if (offsetQuestionScore != null) {
			if (previous) {
				buffer.append(" AND ((");
				buffer.append("questionScore_d:[" +
						offsetQuestionScore + " TO *]");
				buffer.append(" AND modifyDateTime_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
				buffer.append(") OR ");
				buffer.append("questionScore_d:{" +
						offsetQuestionScore + " TO *}");
				buffer.append(")");
			} else {
				buffer.append(" AND ((");
				buffer.append("questionScore_d:[* TO " +
						offsetQuestionScore + "]");
				buffer.append(" AND modifyDateTime_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
				buffer.append(") OR ");
				buffer.append("questionScore_d:{* TO " +
						offsetQuestionScore + "}");
				buffer.append(")");
			}
		}
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(limit);
		if (offsetQuestionScore == null || !previous) {
			query.setSortField("questionScore_d", ORDER.desc);
			query.addSortField("modifyDateTime_dt", ORDER.desc);
		} else {
			query.setSortField("questionScore_d", ORDER.asc);
			query.addSortField("modifyDateTime_dt", ORDER.asc);
		}
		SearchResult<QuestionDO> searchResult = new SearchResult<QuestionDO>(
				solrOperations.findByQuery(query, QuestionDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId").depth(1)));
		if (offsetQuestionScore == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}
	
	/**
	 * 指定した商品に対するQA情報を更新日時（質問・回答の最終更新日時）順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	@Override
	public SearchResult<QuestionDO> findUpdateQuestionBySkus(
			List<String> skus,
			String excludeQuestionId,
			int limit,
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

		if(!StringUtils.isEmpty(excludeQuestionId))
			buffer.append(" AND !questionId:" + excludeQuestionId);

		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND modifyDateTime_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND modifyDateTime_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("modifyDateTime_dt", ORDER.desc);
		} else {
			query.setSortField("modifyDateTime_dt", ORDER.asc);
		}
		SearchResult<QuestionDO> searchResult = new SearchResult<QuestionDO>(
				solrOperations.findByQuery(query, QuestionDO.class,
				Path.includeProp("*").includePath(
						"product.sku," +
						"communityUser.communityUserId").depth(1)));
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}
	
	/**
	 * 指定した商品に対するQA情報を質問投稿日時順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	@Override
	public SearchResult<QuestionDO> findNewQuestionBySkus(
			List<String> skus,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous) {
		return findNewQuestionBySkus(
				skus,
				null,
				excludeQuestionId,
				limit,
				offsetTime,
				previous);
	}

	@Override
	public SearchResult<QuestionDO> findNewQuestionBySkus(
			List<String> skus,
			String excludeOwnerCommunityUserId, 
			String excludeQuestionId,
			int limit, 
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
		if (!StringUtils.isBlank(excludeOwnerCommunityUserId)) {
			buffer.append(" AND !communityUserId_s:");
			buffer.append(SolrUtil.escape(excludeOwnerCommunityUserId));
		}
		buffer.append(" AND !lastAnswerDate_dt:" + SolrConstants.QUERY_DATE_TO_NOW);
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");

		if(!StringUtils.isEmpty(excludeQuestionId))
			buffer.append(" AND !questionId:" + excludeQuestionId);

		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND modifyDateTime_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND modifyDateTime_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}
		AdultHelper adultHelper = new AdultHelper(requestScopeDao.loadAdultVerification());
		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(buffer.toString()));
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("modifyDateTime_dt", ORDER.desc);
		} else {
			query.setSortField("modifyDateTime_dt", ORDER.asc);
		}
		SearchResult<QuestionDO> searchResult = new SearchResult<QuestionDO>(
				solrOperations.findByQuery(query, QuestionDO.class,
				Path.includeProp("*").includePath(
						"product.sku," +
						"communityUser.communityUserId").depth(1)));
		
		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(
							buffer.toString(), PurchaseProductDO.class, solrOperations));
		}
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}
	
	@Override
	public SearchResult<QuestionDO> findUpdateQuestionBySkus(
			List<String> skus,
			String excludeOwnerCommunityUserId, 
			String excludeQuestionId,
			int limit, 
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
		if (!StringUtils.isBlank(excludeOwnerCommunityUserId)) {
			buffer.append(" AND !communityUserId_s:");
			buffer.append(SolrUtil.escape(excludeOwnerCommunityUserId));
		}
		
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");

		if(!StringUtils.isEmpty(excludeQuestionId))
			buffer.append(" AND !questionId:" + excludeQuestionId);

		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND postDate_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND postDate_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}
		AdultHelper adultHelper = new AdultHelper(requestScopeDao.loadAdultVerification());
		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(buffer.toString()));
		
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("postDate_dt", ORDER.desc);
		} else {
			query.setSortField("postDate_dt", ORDER.asc);
		}
		SearchResult<QuestionDO> searchResult = new SearchResult<QuestionDO>(
				solrOperations.findByQuery(query, QuestionDO.class,
				Path.includeProp("*").includePath(
						"product.sku," +
						"communityUser.communityUserId").depth(1)));
		
		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(
							buffer.toString(), QuestionDO.class, solrOperations));
		}
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}

	/**
	 * 指定した商品に対するQA情報を盛り上がり順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetQuestionScore 検索開始スコア
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	@Override
	public SearchResult<QuestionDO> findPopularQuestionBySkus(
			List<String> skus,
			String excludeQuestionId,
			int limit,
			Double offsetQuestionScore,
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
		buffer.append(" AND lastAnswerDate_dt:" +SolrConstants.QUERY_DATE_TO_NOW);
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");

		if(!StringUtils.isEmpty(excludeQuestionId))
			buffer.append(" AND !questionId:" + excludeQuestionId);

		if (offsetQuestionScore != null) {
			if (previous) {
				buffer.append(" AND ((");
				buffer.append("questionScore_d:[" +
						offsetQuestionScore + " TO *]");
				buffer.append(" AND modifyDateTime_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
				buffer.append(") OR ");
				buffer.append("questionScore_d:{" +
						offsetQuestionScore + " TO *}");
				buffer.append(")");
			} else {
				buffer.append(" AND ((");
				buffer.append("questionScore_d:[* TO " +
						offsetQuestionScore + "]");
				buffer.append(" AND modifyDateTime_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
				buffer.append(") OR ");
				buffer.append("questionScore_d:{* TO " +
						offsetQuestionScore + "}");
				buffer.append(")");
			}
		}
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(limit);
		if (offsetQuestionScore == null || !previous) {
			query.setSortField("questionScore_d", ORDER.desc);
			query.addSortField("modifyDateTime_dt", ORDER.desc);
		} else {
			query.setSortField("questionScore_d", ORDER.asc);
			query.addSortField("modifyDateTime_dt", ORDER.asc);
		}
		SearchResult<QuestionDO> searchResult = new SearchResult<QuestionDO>(
				solrOperations.findByQuery(query, QuestionDO.class,
				Path.includeProp("*").includePath(
						"product.sku," +
						"communityUser.communityUserId").depth(1)));
		if (offsetQuestionScore == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}

	/**
	 * 質問情報のインデックスを更新します。
	 * @param questionId 質問ID
	 * @return 質問情報
	 */
	@Override
	public QuestionDO updateQuestionInIndex(String questionId) {
		return updateQuestionInIndex(questionId, false);
	}

	@Override
	public QuestionDO updateQuestionInIndex(String questionId, boolean mngToolOperation) {
		QuestionDO question = loadQuestion(questionId);
		return updateQuestionInIndex(questionId, question, mngToolOperation);
	}

	@Override
	public QuestionDO updateQuestionInIndex(QuestionDO question) {
		return updateQuestionInIndex(question, false);
	}

	@Override
	public QuestionDO updateQuestionInIndex(QuestionDO question, boolean mngToolOperation) {
		return updateQuestionInIndex(question.getQuestionId(), question, mngToolOperation);
	}

	/**
	 * 指定した質問情報を返します。
	 * @param questionId 質問ID
	 * @return 質問情報
	 */
	@Override
	public QuestionDO loadQuestion(String questionId) {
		return loadQuestion(questionId, getDefaultLoadQuestionCondition(), false);
	}

	/**
	 * 指定した質問情報をインデックス情報から返します。
	 * @param questionId 質問ID
	 * @return 質問情報
	 */
	@Override
	public QuestionDO loadQuestionFromIndex(String questionId) {
		QuestionDO question = solrOperations.load(QuestionDO.class, questionId,
				getDefaultLoadQuestionCondition());
		return question;
	}

	@Override
	public QuestionDO loadQuestionFromIndex(String questionId, boolean includeDeleteContents) {

		StringBuilder buffer = new StringBuilder();
		buffer.append("questionId:");
		buffer.append(questionId);
		if(!includeDeleteContents){
			buffer.append(" AND !status_s:");
			buffer.append(ContentsStatus.DELETE.getCode());
			buffer.append(" AND withdraw_b:false");
		}
		SearchResult<QuestionDO> results = new SearchResult<QuestionDO>(
				solrOperations.findByQuery(
						new SolrQuery(buffer.toString()),
						QuestionDO.class,
						getDefaultLoadQuestionCondition()));
		if(results == null || results.getDocuments().isEmpty() || results.getDocuments().size() > 1)
			return null;
		return results.getDocuments().get(0);
	}

	/**
	 * 指定した質問以外で、指定した商品の盛り上がっている質問を盛り上がり順
	 * （降順）に返します。
	 * @param sku SKU
	 * @param excudeQuestionId 除外する質問ID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 盛り上がっている質問一覧
	 */
	@Override
	public SearchResult<QuestionDO> findPopularQuestionExcudeQuestionId(
			String sku,
			String excudeQuestionId,
			int limit,
			int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND productId_s:" + sku);
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		buffer.append(" AND !questionId:");
		buffer.append(excudeQuestionId);
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(limit);
		query.setStart(offset);
		query.setSortField("questionScore_d", ORDER.desc);
		SearchResult<QuestionDO> searchResult = new SearchResult<QuestionDO>(
				solrOperations.findByQuery(query, QuestionDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId").depth(1)));
		return searchResult;
	}

	/**
	 * 指定した質問以外で、指定した商品の質問を質問投稿日時順（降順）に返します。
	 * @param sku SKU
	 * @param excudeQuestionId 除外する質問ID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 新着質問一覧
	 */
	@Override
	public SearchResult<QuestionDO> findNewQuestionExcudeQuestionId(
			String sku,
			String excudeQuestionId,
			int limit,
			int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND productId_s:" + sku);
		buffer.append(" AND !lastAnswerDate_dt:" + SolrConstants.QUERY_DATE_TO_NOW);
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		buffer.append(" AND !questionId:");
		buffer.append(excudeQuestionId);
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(limit);
		query.setStart(offset);
		query.setSortField("postDate_dt", ORDER.desc);
		SearchResult<QuestionDO> searchResult = new SearchResult<QuestionDO>(
				solrOperations.findByQuery(query, QuestionDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId").depth(1)));
		return searchResult;
	}

	/**
	 * 指定したコミュニティユーザーが投稿した質問を質問投稿日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param status ステータス
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @param アダルト確認フラグ
	 * @return 質問一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<QuestionDO> findQuestionByCommunityUserId(
			String communityUserId,
			String excludeQuestionId,
			int limit, 
			Date offsetTime,
			boolean previous,
			Verification adultVerification) {
		StringBuilder buffer = new StringBuilder();
		
		buffer.append("withdraw_b:false AND communityUserId_s:" + communityUserId);
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
		if(!StringUtils.isEmpty(excludeQuestionId))
			buffer.append(" AND !questionId:" + excludeQuestionId);

		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND postDate_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND postDate_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}
		AdultHelper adultHelper = new AdultHelper(adultVerification);
		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(buffer.toString()));
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("postDate_dt", ORDER.desc);
		} else {
			query.setSortField("postDate_dt", ORDER.asc);
		}
		SearchResult<QuestionDO> searchResult = new SearchResult<QuestionDO>(
				solrOperations.findByQuery(query, QuestionDO.class,getDefaultLoadQuestionCondition()));

		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(buffer.toString(),
							QuestionDO.class, solrOperations));
		}
		Set<String> questionIds = new HashSet<String>();
		for(QuestionDO questionDO : searchResult.getDocuments()){
			questionIds.add(questionDO.getQuestionId());
		}
		
		Map<String, QuestionDO> questionMap = hBaseOperations.find(QuestionDO.class, String.class, questionIds, getDefaultLoadQuestionCondition());
		
		if( !questionMap.isEmpty() ){
			Iterator<Entry<String, QuestionDO>> questionInterator = questionMap.entrySet().iterator();
			Entry<String, QuestionDO> entry = null;
			List<QuestionDO> questions = Lists.newArrayList();
			while( questionInterator.hasNext() ){
				entry = questionInterator.next();
				questions.add(entry.getValue());
				
			}
			Collections.sort(questions, new Comparator<QuestionDO>() {
				@Override
				public int compare(QuestionDO o1, QuestionDO o2) {
					return o2.getPostDate().compareTo(o1.getPostDate());
				}
				
			});
			searchResult.setDocuments(questions);
		}
		
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}
	
	@Override
	@ArroundSolr
	public SearchResult<QuestionDO> findTemporaryQuestionByCommunityUserId(
			String communityUserId,
			String excludeQuestionId,
			int limit, 
			Date offsetTime,
			boolean previous,
			Verification adultVerification){
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND communityUserId_s:" + communityUserId);
		buffer.append(" AND status_s:");
		buffer.append(ContentsStatus.SAVE.getCode());
		if(!StringUtils.isEmpty(excludeQuestionId))
			buffer.append(" AND !questionId:" + excludeQuestionId);

		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND saveDate_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND saveDate_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}
		AdultHelper adultHelper = new AdultHelper(adultVerification);
		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(buffer.toString()));
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("saveDate_dt", ORDER.desc);
		} else {
			query.setSortField("saveDate_dt", ORDER.asc);
		}
		SearchResult<QuestionDO> searchResult = new SearchResult<QuestionDO>(
				solrOperations.findByQuery(query, QuestionDO.class,getDefaultLoadQuestionCondition()));

		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(buffer.toString(),
							QuestionDO.class, solrOperations));
		}
		Set<String> questionIds = new HashSet<String>();
		for(QuestionDO questionDO : searchResult.getDocuments()){
			questionIds.add(questionDO.getQuestionId());
		}
		
		Map<String, QuestionDO> questionMap = hBaseOperations.find(QuestionDO.class, String.class, questionIds, getDefaultLoadQuestionCondition());
		
		if( !questionMap.isEmpty() ){
			Iterator<Entry<String, QuestionDO>> reviewInterator = questionMap.entrySet().iterator();
			Entry<String, QuestionDO> entry = null;
			List<QuestionDO> questions = Lists.newArrayList();
			while( reviewInterator.hasNext() ){
				entry = reviewInterator.next();
				questions.add(entry.getValue());
				
			}
			Collections.sort(questions, new Comparator<QuestionDO>() {
				@Override
				public int compare(QuestionDO o1, QuestionDO o2) {
					return o2.getSaveDate().compareTo(o1.getSaveDate());
				}
				
			});
			searchResult.setDocuments(questions);
		}
		
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}

	/**
	 * 指定した期間に更新のあった質問を返します。
	 * @param fromDate 検索開始時間
	 * @param toDate 検索終了時間
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return
	 */
	@Override
	public SearchResult<QuestionDO> findUpdatedQuestionByOffsetTime(
			Date fromDate, Date toDate, int limit, int offset){
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
		SearchResult<QuestionDO> searchResult = new SearchResult<QuestionDO>(
				solrOperations.findByQuery(query, QuestionDO.class));
		return searchResult;
	}
	
	/**
	 * 指定したユーザーの全ての有効、一時停止質問を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return
	 */
	@Override
	public SearchResult<QuestionDO> findQuestionByCommunityUserId(
			String communityUserId, int limit, int offset){
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
		SearchResult<QuestionDO> searchResult = new SearchResult<QuestionDO>(
				solrOperations.findByQuery(query, QuestionDO.class));
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
	public long countQuestionByCommunityUserId(String communityUserId,ContentsStatus status) {
		return countQuestionByCommunityUserId(
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
	public long countQuestionByCommunityUserIdForMypage(String communityUserId) {
		return countQuestionByCommunityUserId(
				communityUserId,
				null,
				new ContentsStatus[]{ContentsStatus.SUBMITTED,ContentsStatus.CONTENTS_STOP},
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
	public long countQuestionByCommunityUserId(
			String communityUserId,
			String excludeQuestionId,
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
		if(!StringUtils.isEmpty(excludeQuestionId))
			buffer.append(" AND !questionId:" + excludeQuestionId);
		
		SolrQuery solrQuery = new SolrQuery(new AdultHelper(adultVerification).toFilterQuery(buffer.toString()));
		return solrOperations.count(solrQuery, QuestionDO.class);
	}
	
	/**
	 * 指定したユーザーの購入商品の新着Q&A情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 購入商品の新着Q&A情報
	 */
	@Override
	public SearchResult<QuestionDO> findNewQuestionByPurchaseProduct(
			String communityUserId, int limit, Date offsetTime, boolean previous) {
		SolrQuery productQuery = new SolrQuery("withdraw_b:false AND communityUserId_s:" + communityUserId);
		productQuery.setRows(SolrConstants.QUERY_ROW_LIMIT);
		productQuery.setStart(0);
		productQuery.addSortField("purchaseDate_dt", ORDER.desc);

		SearchResult<PurchaseProductDO> skus = new SearchResult<PurchaseProductDO>(
				solrOperations.findByQuery(
						productQuery, PurchaseProductDO.class, Path.includeProp("productId")));

		if (skus.getDocuments().size() == 0) {
			return new SearchResult<QuestionDO>(0, new ArrayList<QuestionDO>());
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
		for (int i = 0; i < skus.getDocuments().size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("productId_s:");
			buffer.append(skus.getDocuments().get(i).getProduct().getSku());
		}
		buffer.append(")");
		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND modifyDateTime_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND modifyDateTime_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}
		buffer.append(" AND !communityUserId_s:");
		buffer.append(communityUserId);
		
		AdultHelper adultHelper = new AdultHelper(requestScopeDao.loadAdultVerification());
		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(buffer.toString()));
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("modifyDateTime_dt", ORDER.desc);
		} else {
			query.setSortField("modifyDateTime_dt", ORDER.asc);
		}
		SearchResult<QuestionDO> searchResult = new SearchResult<QuestionDO>(
				solrOperations.findByQuery(query, QuestionDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId," +
						"product.sku").depth(1)));

		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(
							buffer.toString(), QuestionDO.class, solrOperations));
		}

		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}

	/**
	 * 質問数情報を返します。
	 * @param skus SKUリスト
	 * @param waitAnswerOnly 回答待ち（回答無し）のみ
	 * @param excludeQuestionOwnerId 除外したい質問者のコミュニティユーザーID
	 * @return 質問数情報
	 */
	@Override
	public Map<String, Long> loadQuestionCountMapBySKU(
			List<String> skus,
			boolean waitAnswerOnly,
			String excludeQuestionOwnerId) {
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
		if (waitAnswerOnly) {
			buffer.append(" AND !lastAnswerDate_dt:" + SolrConstants.QUERY_DATE_TO_NOW);
		}
		if (excludeQuestionOwnerId != null) {
			buffer.append(" AND !communityUserId_s:");
			buffer.append(excludeQuestionOwnerId);
		}
		buffer.append(" AND (");
		for (int i = 0; i < skus.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("productId_s:");
			buffer.append(skus.get(i));
		}
		buffer.append(")");

		SolrQuery solrQuery = new SolrQuery(
				buffer.toString());
		solrQuery.addFacetField("productId_s");
		solrQuery.setFacetLimit(SolrConstants.QUERY_ROW_LIMIT);
		solrQuery.setFacetMinCount(0);
		for (FacetResult<String> facetResult : solrOperations.facet(
				QuestionDO.class, String.class, solrQuery)) {
			resultMap.put(facetResult.getValue(), facetResult.getCount());
		}
		return resultMap;
	}

	/**
	 * 質問数情報を返します。
	 * @param communityUserIds コミュニティユーザーIDのリスト
	 * @return 質問数情報
	 */
	@Override
	public Map<String, Long> loadQuestionCountMapByCommunityUserId(
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
			buffer.append(communityUserIds.get(i));
		}
		buffer.append(")");

		SolrQuery solrQuery = new SolrQuery(
				new AdultHelper(requestScopeDao.loadAdultVerification(
										)).toFilterQuery(buffer.toString()));
		solrQuery.addFacetField("communityUserId_s");
		solrQuery.setFacetLimit(SolrConstants.QUERY_ROW_LIMIT);
		for (FacetResult<String> facetResult : solrOperations.facet(
				QuestionDO.class, String.class, solrQuery)) {
			resultMap.put(facetResult.getValue(), facetResult.getCount());
		}
		return resultMap;
	}

	/**
	 * 投稿質問数を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @return 投稿質問数
	 */
	@Override
	public long countPostQuestionCount(String communityUserId, String sku) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" AND communityUserId_s:");
		buffer.append(communityUserId);
		if (!StringUtils.isEmpty(sku)) {
			buffer.append(" AND productId_s:");
			buffer.append(sku);
		}

		SolrQuery solrQuery = new SolrQuery(new AdultHelper(
				requestScopeDao.loadAdultVerification(
						)).toFilterQuery(buffer.toString()));
		return solrOperations.count(solrQuery, QuestionDO.class);
	}

	/**
	 * 指定したコミュニティユーザーが指定した日付に投稿した質問を返します。
	 * @param communityUserIds コミュニティユーザーIDリスト
	 * @param publicDate 公開された日付
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return 質問リスト
	 */
	@Override
	public SearchResult<QuestionDO> findQuestionByCommunityUserIds(
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
		return new SearchResult<QuestionDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
						).toFilterQuery(buffer.toString())).setRows(limit).setStart(offset
								).setSortField("postDate_dt", ORDER.asc),
						QuestionDO.class, Path.includeProp("*").includePath(
						"product.sku,communityUser.communityUserId").depth(1)));
	}
	@Override
	public SearchResult<QuestionDO> findQuestionByCommunityUserIdsForMR(
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
		SearchResult<QuestionDO> searchResult = new SearchResult<QuestionDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
						).toFilterQuery(buffer.toString())).setRows(limit).setStart(offset
								).setSortField("postDate_dt", ORDER.asc),
						QuestionDO.class, Path.includeProp("*").includePath(
						"communityUser.communityUserId").depth(1)));

		List<String> skus = new ArrayList<String>();
		for(QuestionDO question:searchResult.getDocuments()) {
			skus.add(question.getProduct().getSku());
		}
		Map<String, ProductDO> productMap = productDao.findBySkuForMR(skus);
		for(QuestionDO question:searchResult.getDocuments()) {
			question.setProduct(productMap.get(question.getProduct().getSku()));
		}		
		return searchResult;
	}


	/**
	 * 指定した商品、日付に投稿した質問を返します。
	 * @param skus SKUリスト
	 * @param publicDate 公開された日付
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return 質問リスト
	 */
	@Override
	public SearchResult<QuestionDO> findQuestionBySKUs(
			List<String> skus, Date publicDate,
			String excludeCommunityUserId, int limit, int offset) {
		if (skus == null || skus.size() == 0) {
			return new SearchResult<QuestionDO>();
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ");
		buffer.append(" status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		if(StringUtils.isNotEmpty(excludeCommunityUserId)){
			buffer.append(" AND ");
			buffer.append("!communityUserId_s:");
			buffer.append(SolrUtil.escape(excludeCommunityUserId));
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
		return new SearchResult<QuestionDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
						).toFilterQuery(buffer.toString())).setRows(limit).setStart(offset
								).setSortField("postDate_dt", ORDER.asc),
						QuestionDO.class, Path.includeProp("*").includePath(
						"product.sku,communityUser.communityUserId").depth(1)));
	}

	/**
	 * 指定した商品、日付に投稿した質問を返します。
	 * @param skus SKUリスト
	 * @param publicDate 公開された日付
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return 質問リスト
	 */
	@Override
	public SearchResult<QuestionDO> findQuestionBySKUsForMR(
			List<String> skus, Date publicDate,
			String excludeCommunityUserId, int limit, int offset) {
		if (skus == null || skus.size() == 0) {
			return new SearchResult<QuestionDO>();
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ");
		buffer.append(" status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		if(StringUtils.isNotEmpty(excludeCommunityUserId)){
			buffer.append(" AND ");
			buffer.append("!communityUserId_s:");
			buffer.append(SolrUtil.escape(excludeCommunityUserId));
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
		SearchResult<QuestionDO> results = new SearchResult<QuestionDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
						).toFilterQuery(buffer.toString())).setRows(limit).setStart(offset
								).setSortField("postDate_dt", ORDER.asc),
						QuestionDO.class, Path.includeProp("*").includePath(
						"communityUser.communityUserId").depth(1)));
		
		Map<String, ProductDO> productMap = productDao.findBySkuForMR(skus);
		for(QuestionDO question:results.getDocuments()) {
			question.setProduct(productMap.get(question.getProduct().getSku()));
		}		
		return results;
	}
	
	/**
	 * 質問情報のインデックスを更新します。
	 * @param questionId 質問ID
	 * @param question 質問
	 * @return 質問情報
	 */
	private QuestionDO updateQuestionInIndex(
			String questionId,
			QuestionDO question,
			boolean mngToolOperation) {
		if (question == null || question.isDeleted()) {
			solrOperations.deleteByQuery(new SolrQuery(
					"questionId_s:" + SolrUtil.escape(questionId)), ActionHistoryDO.class);
			solrOperations.deleteByQuery(new SolrQuery(
					"questionId_s:" + SolrUtil.escape(questionId)), InformationDO.class);
			solrOperations.deleteByQuery(new SolrQuery(
					"followQuestionId_s:" + SolrUtil.escape(questionId)), QuestionFollowDO.class);
			if(!mngToolOperation) {
				solrOperations.deleteByQuery(new SolrQuery(
						"questionId_s:" + SolrUtil.escape(questionId)), LikeDO.class);
				solrOperations.deleteByQuery(new SolrQuery(
						"questionId_s:" + SolrUtil.escape(questionId)), VotingDO.class);
			}

			if (question == null) {
				solrOperations.deleteByQuery(new SolrQuery(
						"questionId_s:" + SolrUtil.escape(questionId)), ImageHeaderDO.class);
				solrOperations.deleteByQuery(new SolrQuery(
						"questionId_s:" + SolrUtil.escape(questionId)), CommentDO.class);
				solrOperations.deleteByQuery(new SolrQuery(
						"questionId_s:" + SolrUtil.escape(questionId)), QuestionAnswerDO.class);
				solrOperations.deleteByKey(QuestionDO.class, questionId);
					solrOperations.deleteByQuery(new SolrQuery(
							"questionId_s:" + SolrUtil.escape(questionId)), SpamReportDO.class);
			} else {
				solrOperations.save(ImageHeaderDO.class,
						hBaseOperations.scanWithIndex(
								ImageHeaderDO.class, "questionId", questionId));
				solrOperations.save(CommentDO.class,
						hBaseOperations.scanWithIndex(
								CommentDO.class, "questionId", questionId));
				solrOperations.save(QuestionAnswerDO.class,
						hBaseOperations.scanWithIndex(
								QuestionAnswerDO.class, "questionId", questionId));
				solrOperations.save(SpamReportDO.class,
						hBaseOperations.scanWithIndex(
								SpamReportDO.class, "questionId", questionId));
				solrOperations.save(question);
			}
			return null;
		}
		solrOperations.save(question);
		return question;
	}

	/**
	 * 指定した質問に対する最終回答日を返します。
	 * @param questionId 質問ID
	 * @return 最終回答日
	 */
	private Date loadLastAnswerDateFromIndex(String questionId) {

		if(StringUtils.isEmpty(questionId)) return null;

		SearchResult<QuestionAnswerDO> searchResult = new SearchResult<QuestionAnswerDO>(
				solrOperations.findByQuery(new SolrQuery(
				"withdraw_b:false AND questionId_s:" + SolrUtil.escape(questionId) 
				+ " AND ( "
				+ "status_s:"
				+ ContentsStatus.SUBMITTED.getCode()
				+ " OR status_s:"
				+ ContentsStatus.CONTENTS_STOP.getCode()
				+ " ) ").setSortField("postDate_dt",
						ORDER.desc).setRows(1), QuestionAnswerDO.class,
						Path.includeProp("postDate_dt")));
		if (searchResult.getDocuments().size() > 0) {
			return searchResult.getDocuments().get(0).getPostDate();
		} else {
			return null;
		}
	}

	/**
	 * 質問情報を読み出すデフォルト条件を返します。
	 * @return 質問情報の読み出し条件
	 */
	private Condition getDefaultLoadQuestionCondition() {
		return Path.includeProp("*").includePath(
				"product.sku,communityUser.communityUserId").depth(1);
	}

	@Override
	public SearchResult<QuestionDO> findTemporaryQuestionByBeforeInterval(
			Date intervalDate) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND status_s:");
		buffer.append(SolrUtil.escape(ContentsStatus.SAVE.getCode()));
		buffer.append(" AND registerDateTime_dt:{" +
				"* TO " + DateUtil.getThreadLocalDateFormat().format(intervalDate) + "}");
		return new SearchResult<QuestionDO>(
				solrOperations.findByQuery(new SolrQuery(buffer.toString()),QuestionDO.class));
	}

	@Override
	public void removeQuestions(List<String> questionIds) {
		hBaseOperations.deleteByKeys(QuestionDO.class, String.class, questionIds);
		solrOperations.deleteByKeys(QuestionDO.class, String.class, questionIds);
	}

	/**
	 * 指定したコミュニティユーザーの保存質問を削除します。
	 * @param reviewId レビューID
	 * @param logical 論理削除かどうか
	 */
	@Override
	public void removeTemporaryQuestion(String communityUserId) {
		List<QuestionDO> questions = hBaseOperations.findWithIndex(QuestionDO.class, "communityUserId",Path.includeProp("questionId,status"), communityUserId);
		if(questions == null || questions.isEmpty()) return;
		List<String> temporaryQuestionIds = new ArrayList<String>();
		for(QuestionDO question:questions ){
			if(question.getStatus().equals(ContentsStatus.SAVE))
				temporaryQuestionIds.add(question.getQuestionId());
		}
		if(!temporaryQuestionIds.isEmpty()){
			hBaseOperations.deleteByKeys(QuestionDO.class, String.class, temporaryQuestionIds);
			solrOperations.deleteByKeys(QuestionDO.class, String.class, temporaryQuestionIds);
		}
	}

	@Override
	public String findProductSku(String questionId) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("questionId:");
		buffer.append(questionId);
		
		SearchResult<QuestionDO> results = new SearchResult<QuestionDO>(
				solrOperations.findByQuery(
						new SolrQuery(buffer.toString()),
						QuestionDO.class,
						Path.includeProp("*").includePath("product.sku").depth(1)));
		
		if(results == null || results.getDocuments().isEmpty() || results.getDocuments().size() > 1 || results.getDocuments().get(0).getProduct() == null)
			return null;
		return results.getDocuments().get(0).getProduct().getSku();
	}
	
	
}
