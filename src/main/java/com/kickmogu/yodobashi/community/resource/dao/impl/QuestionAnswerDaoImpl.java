/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FilterList.Operator;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.UpdateColumns;
import com.kickmogu.lib.solr.FacetResult;
import com.kickmogu.lib.solr.SolrConstants;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.resource.cache.CacheStrategyType;
import com.kickmogu.yodobashi.community.resource.cache.MethodCache;
import com.kickmogu.yodobashi.community.resource.cache.TargetSystemType;
import com.kickmogu.yodobashi.community.resource.dao.ImageDao;
import com.kickmogu.yodobashi.community.resource.dao.InformationDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionAnswerDao;
import com.kickmogu.yodobashi.community.resource.dao.RemoveContentsDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.util.AdultHelper;
import com.kickmogu.yodobashi.community.resource.dao.util.ProductUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.SolrUtil;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
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
public class QuestionAnswerDaoImpl implements QuestionAnswerDao {

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

	@Autowired
	protected ImageDao imageDao;
	
	@Autowired
	protected RemoveContentsDao removeContentsDao;


	/**
	 * 商品 DAO です。
	 */
	@Autowired @Qualifier("catalog")
	protected ProductDao productDao;
	
	/**
	 * 指定した条件の質問情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param questionId 質問ID
	 * @param condition 条件
	 * @return 質問情報リスト
	 */
	public List<QuestionAnswerDO> findQuestionAnswerByCommunityUserIdAndQuestionId(
			String communityUserId, String questionId, Condition condition) {
		List<QuestionAnswerDO> questionAnswers = hBaseOperations.scanWithIndex(
				QuestionAnswerDO.class, "communityUserId", communityUserId,
				hBaseOperations.createFilterBuilder(QuestionAnswerDO.class).appendSingleColumnValueFilter("questionId", CompareOp.EQUAL, questionId).toFilter(),
				condition);
		
		return questionAnswers;
	}
	
	/**
	 * 指定した条件の質問情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param questionIds 質問IDs
	 * @param condition 条件
	 * @return 質問情報リスト
	 */
	public List<QuestionAnswerDO> findQuestionAnswerByCommunityUserIdAndQuestionIds(
			String communityUserId,
			List<String> questionIds,
			Condition condition){
		if( StringUtils.isEmpty(communityUserId) || questionIds == null || questionIds.isEmpty() )
			return null;
		
		List<QuestionAnswerDO> questionAnswers = hBaseOperations.scanWithIndex(
				QuestionAnswerDO.class, "communityUserId", communityUserId,
				hBaseOperations.createFilterBuilder(QuestionAnswerDO.class, Operator.MUST_PASS_ONE).includeColumnValues("questionId", questionIds.toArray()).toFilter(),
				condition);
		
		return questionAnswers;
	}

	@Override
	public QuestionAnswerDO getNewSaveQuestionAnswerByCommunityUserId(String communityUserId, String sku) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND status_s:");
		buffer.append(SolrUtil.escape(ContentsStatus.SAVE.getCode()));
		buffer.append(" AND communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND productId_s:");
		buffer.append(SolrUtil.escape(sku));
		
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(1);
		query.addSortField("saveDate_d", ORDER.desc);
		
		SearchResult<QuestionAnswerDO> searchResult = new SearchResult<QuestionAnswerDO>(
				solrOperations.findByQuery(query,QuestionAnswerDO.class, Path.includePath("questionAnswerId")));
		ProductUtil.filterInvalidProduct(searchResult);
		
		if( searchResult.getDocuments().isEmpty() )
			return null;
		
		return loadQuestionAnswer(searchResult.getDocuments().get(0).getQuestionAnswerId());
	}

	/**
	 * 質問回答情報を保存します。
	 * @param questionAnswer 質問回答
	 */
	@Override
	public void saveQuestionAnswer(QuestionAnswerDO questionAnswer) {
		questionAnswer.setSaveDate(timestampHolder.getTimestamp());
		questionAnswer.setModifyDateTime(timestampHolder.getTimestamp());
		// エディターのバージョン設定
		questionAnswer.setEditorVersion(EditorVersions.TEXT_EDITOR);
		hBaseOperations.save(questionAnswer);
	}

	/**
	 * 指定した質問回答を削除します。
	 * @param questionAnswerId 質問回答ID
	 * @param logical 論理削除かどうか
	 */
	@Override
	public void deleteQuestionAnswer(
			String questionAnswerId,
			boolean logical,
			boolean mngToolOperation) {

		if (logical) {
			QuestionAnswerDO questionAnswer = new QuestionAnswerDO();
			questionAnswer.setQuestionAnswerId(questionAnswerId);
			questionAnswer.setStatus(ContentsStatus.DELETE);
			questionAnswer.setMngToolOperation(mngToolOperation);
			questionAnswer.setDeleteDate(timestampHolder.getTimestamp());
			questionAnswer.setModifyDateTime(timestampHolder.getTimestamp());

			//関連情報を削除
			//ActionHistoryDO
			hBaseOperations.scanUpdateWithIndex(
					ActionHistoryDO.class, "questionAnswerId", questionAnswerId,
					UpdateColumns.set("deleteFlag", true
							).andSet("deleteDate", timestampHolder.getTimestamp())
							.andSet("modifyDateTime", timestampHolder.getTimestamp()));
			//InformationDO
			hBaseOperations.scanUpdateWithIndex(
					InformationDO.class, "questionAnswerId", questionAnswerId,
					UpdateColumns.set("deleteFlag", true
							).andSet("deleteDate", timestampHolder.getTimestamp())
							.andSet("modifyDateTime", timestampHolder.getTimestamp()));
			//CommentDO
			hBaseOperations.scanUpdateWithIndex(
					CommentDO.class, "questionAnswerId", questionAnswerId,
					UpdateColumns.set("deleteFlag", true
							).andSet("deleteDate", timestampHolder.getTimestamp())
							.andSet("modifyDateTime", timestampHolder.getTimestamp()));
			//SpamReportDO
			hBaseOperations.scanUpdateWithIndex(
					SpamReportDO.class, "questionAnswerId", questionAnswerId,
					UpdateColumns.set("status", SpamReportStatus.DELETE
							).andSet("deleteDate", timestampHolder.getTimestamp())
							.andSet("modifyDateTime", timestampHolder.getTimestamp()));

			//LikeDO
			hBaseOperations.scanDeleteWithIndex(
					LikeDO.class, "questionAnswerId", questionAnswerId);
			// VotingDO
			hBaseOperations.scanDeleteWithIndex(
					VotingDO.class, "questionAnswerId", questionAnswerId);

			hBaseOperations.save(questionAnswer, Path.includeProp("status,deleteDate,modifyDateTime,mngToolOperation"));

		} else {
			hBaseOperations.deleteByKey(QuestionAnswerDO.class, questionAnswerId);
		}
	}

	/**
	 * 質問回答のインデックスを更新します。
	 * @param questionAnswerId 質問回答ID
	 * @return 質問回答情報
	 */
	@Override
	public QuestionAnswerDO updateQuestionAnswerInIndex(String questionAnswerId) {
		return updateQuestionAnswerInIndex(questionAnswerId, false);
	}

	@Override
	public QuestionAnswerDO updateQuestionAnswerInIndex(String questionAnswerId, boolean mngToolOperation) {
		QuestionAnswerDO questionAnswer = loadQuestionAnswer(
				questionAnswerId, Path.DEFAULT, false);
		return updateQuestionAnswerInIndex(questionAnswerId, questionAnswer, mngToolOperation);
	}

	/**
	 * 指定した質問回答情報を返します。
	 * @param questionAnswerId 質問回答ID
	 * @param condition 条件
	 * @param withLock ロックを取得するかどうか
	 * @return 質問回答情報
	 */
	@Override
	public QuestionAnswerDO loadQuestionAnswer(
			String questionAnswerId, Condition condition, boolean withLock) {
		QuestionAnswerDO questionAnswer = null;
		if (withLock) {
			questionAnswer = hBaseOperations.loadWithLock(QuestionAnswerDO.class, questionAnswerId,
					condition);
		} else {
			questionAnswer = hBaseOperations.load(QuestionAnswerDO.class, questionAnswerId,
					condition);
		}
		return questionAnswer;
	}

	/**
	 * 質問回答のインデックスを更新します。
	 * @param questionAnswerId 質問回答ID
	 * @param questionAnswer 質問回答
	 * @return 質問回答情報
	 */
	private QuestionAnswerDO updateQuestionAnswerInIndex(
			String questionAnswerId,
			QuestionAnswerDO questionAnswer) {
		return updateQuestionAnswerInIndex( questionAnswerId, questionAnswer, false);
	}


	private QuestionAnswerDO updateQuestionAnswerInIndex(
			String questionAnswerId,
			QuestionAnswerDO questionAnswer, boolean mngToolOperation) {

		if (questionAnswer == null || questionAnswer.isDeleted()) {
			solrOperations.deleteByQuery(new SolrQuery(
					"questionAnswerId_s:" + questionAnswerId), ActionHistoryDO.class);
			solrOperations.deleteByQuery(new SolrQuery(
					"questionAnswerId_s:" + questionAnswerId), InformationDO.class);
			if(!mngToolOperation){
				solrOperations.deleteByQuery(new SolrQuery(
						"questionAnswerId_s:" + questionAnswerId), LikeDO.class);
				solrOperations.deleteByQuery(new SolrQuery(
						"questionAnswerId_s:" + questionAnswerId), VotingDO.class);
			}
			if (questionAnswer == null) {
				solrOperations.deleteByQuery(new SolrQuery(
						"questionAnswerId_s:" + questionAnswerId), ImageHeaderDO.class);
				solrOperations.deleteByQuery(new SolrQuery(
						"questionAnswerId_s:" + questionAnswerId), CommentDO.class);
				solrOperations.deleteByQuery(new SolrQuery(
						"questionAnswerId_s:" + questionAnswerId), SpamReportDO.class);

				solrOperations.deleteByKey(QuestionAnswerDO.class, questionAnswerId);
			} else {
				solrOperations.save(ImageHeaderDO.class,
						hBaseOperations.scanWithIndex(
								ImageHeaderDO.class, "questionAnswerId", questionAnswerId));
				solrOperations.save(CommentDO.class,
						hBaseOperations.scanWithIndex(
								CommentDO.class, "questionAnswerId", questionAnswerId));
				solrOperations.save(SpamReportDO.class,
						hBaseOperations.scanWithIndex(
								SpamReportDO.class, "questionAnswerId", questionAnswerId));
				solrOperations.save(questionAnswer);
			}

			return null;
		}
		solrOperations.save(questionAnswer);
		return questionAnswer;
	}

	/**
	 * 指定した商品の質問に回答を書いたユーザーを重複を除いて返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findDistinctAnswererBySKU(
			String sku, int limit, int offset) {
		return findDistinctAnswererBySKU(sku, limit, offset, false);
	}
	@Override
	public SearchResult<CommunityUserDO> findDistinctAnswererBySKU(
			String sku, int limit, int offset, boolean excludeProduct) {
		//重複除去と投稿順の並び替えを同時に満たすため、solr ではサブクエリ
		//とdistinctをサポートしていないので、上限数を絞り、java 側で処理します。
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND productId_s:");
		buffer.append(sku);
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		return createDistinctCommunityUsers(buffer.toString(), limit, offset, excludeProduct);
	}

	/**
	 * 指定した商品の質問に回答を書いたユーザーを重複を除いて返します。
	 * @param skus SKUリスト
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findDistinctAnswererBySKU(
			List<String> skus, int limit, int offset) {
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
			buffer.append("questionId_s:");
			buffer.append(skus.get(i));
		}
		buffer.append(")");
		return createDistinctCommunityUsers(buffer.toString(), limit, offset);
	}

	/**
	 * フォローした商品の質問に回答を書いたユーザーを重複を除いて返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findDistinctAnswererByFollowProduct(
			String communityUserId, int limit, int offset) {
		//重複除去と投稿順の並び替えを同時に満たすため、solr ではサブクエリ
		//とdistinctをサポートしていないので、上限数を絞り、java 側で処理します。
		SearchResult<ProductFollowDO> follows
				= new SearchResult<ProductFollowDO>(solrOperations.findByQuery(
				new SolrQuery("communityUserId_s:" + communityUserId
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
			buffer.append(follows.getDocuments().get(i).getFollowProduct().getSku());
		}
		buffer.append(")");
		return createDistinctCommunityUsers(buffer.toString(), limit, offset);
	}

	/**
	 * 投稿した質問に回答を書いたユーザーを重複を除いて返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findDistinctAnswererByPostQuestion(
			String communityUserId, int limit, int offset) {
		//重複除去と投稿順の並び替えを同時に満たすため、solr ではサブクエリ
		//とdistinctをサポートしていないので、上限数を絞り、java 側で処理します。
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND relationQuestionOwnerId_s:");
		buffer.append(communityUserId);
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		return createDistinctCommunityUsers(buffer.toString(), limit, offset);
	}

	/**
	 * フォローした質問に回答を書いたユーザーを重複を除いて返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findDistinctAnswererByFollowQuestion(
			String communityUserId, int limit, int offset) {
		//重複除去と投稿順の並び替えを同時に満たすため、solr ではサブクエリ
		//とdistinctをサポートしていないので、上限数を絞り、java 側で処理します。
		SearchResult<QuestionFollowDO> follows
				= new SearchResult<QuestionFollowDO>(solrOperations.findByQuery(
				new SolrQuery("communityUserId_s:" + communityUserId + " AND deleteFlag_b:false"
						).setRows(SolrConstants.QUERY_ROW_LIMIT),
						QuestionFollowDO.class, Path.includeProp("followQuestionId")));
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
			buffer.append("questionId_s:");
			buffer.append(follows.getDocuments().get(i).getFollowQuestion().getQuestionId());
		}
		buffer.append(")");
		return createDistinctCommunityUsers(buffer.toString(), limit, offset);
	}

	/**
	 * 購入した商品の質問に回答を書いたユーザーを重複を除いて返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param publicSetting 非公開情報を表示する場合はtrue
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findDistinctAnswererByQuestionForPurchaseProduct(
			String communityUserId, int limit, int offset, boolean publicSetting) {
		//重複除去と投稿順の並び替えを同時に満たすため、solr ではサブクエリ
		//とdistinctをサポートしていないので、上限数を絞り、java 側で処理します。
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND communityUserId_s:");
		buffer.append(communityUserId);
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
			buffer.append(purchaseProducts.getDocuments().get(i).getProduct().getSku());
		}
		buffer.append(")");
		return createDistinctCommunityUsers(buffer.toString(), limit, offset);
	}

	/**
	 * 重複しないコミュニティユーザーのリストを返します。
	 * @param query 質問回答検索クエリ
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
		
		SearchResult<QuestionAnswerDO> questionAnswers = new SearchResult<QuestionAnswerDO>(
				solrOperations.findByQuery(
						new SolrQuery(adultHelper.toFilterQuery(query)).setRows(
										SolrConstants.QUERY_ROW_LIMIT).setStart(
												0).addSortField("postDate_dt", ORDER.desc),
										QuestionAnswerDO.class, Path.includeProp(
												"status").includePath(includePath).depth(1)));
		if (adultHelper.isRequireCheckAdult()) {
			result.setHasAdult(
					adultHelper.hasAdult(
							query, ReviewDO.class, solrOperations));
		}
		if(!excludeProduct)
			ProductUtil.filterInvalidProduct(questionAnswers);

		if (questionAnswers.getNumFound() == 0) {
			return result;
		}
		List<String> communityUserIds = new ArrayList<String>();
		List<String> communityUserIdAll = new ArrayList<String>();
		String communityUserId = requestScopeDao.loadCommunityUserId();
		for (QuestionAnswerDO questionAnswer : questionAnswers.getDocuments()) {
			// コンテンツの一時停止対応
			if(!questionAnswer.getCommunityUser().getCommunityUserId().equals(communityUserId) && questionAnswer.getStatus().equals(ContentsStatus.CONTENTS_STOP)) {
				continue;
			}
			if (!communityUserIdAll.contains(
					questionAnswer.getCommunityUser().getCommunityUserId())) {
				communityUserIdAll.add(
						questionAnswer.getCommunityUser().getCommunityUserId());
				if (communityUserIdAll.size() > offset
						&& communityUserIdAll.size() <= (offset + limit)) {
					communityUserIds.add(
							questionAnswer.getCommunityUser().getCommunityUserId());
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
	 * 指定した質問に回答を書いている人を返します。
	 * @param questionId 質問ID
	 * @param withoutAnswerId 対象から外す質問回答ID
	 * @param asc 昇順ソート
	 * @return 質問回答を書いているコミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findAnswerCommunityUserByQuestionId(
			String questionId,
			ContentsStatus[] statuses,
			String withoutAnswerId, boolean asc) {
		ORDER order = null;
		if (asc) {
			order = ORDER.asc;
		} else {
			order = ORDER.desc;
		}
		
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND questionId_s:");
		buffer.append(questionId);
		
		buffer.append(" AND (");
		boolean isFirst = true;
		for(ContentsStatus status:statuses){
			if(!isFirst) buffer.append(" OR ");
			buffer.append(" status_s:");
			buffer.append(status.getCode());
			isFirst = false;
		}
		buffer.append(" ) ");
		
		SearchResult<QuestionAnswerDO> searchResult = new SearchResult<QuestionAnswerDO>(
				solrOperations.findByQuery(
						new SolrQuery(buffer.toString()).setRows(
								SolrConstants.QUERY_ROW_LIMIT).addSortField(
								"postDate_dt", order),
				QuestionAnswerDO.class, Path.includeProp("*").includePath(
						"questionAnswerId,communityUser.communityUserId," +
						"communityUser.communityName").depth(1)));
		ProductUtil.filterInvalidProduct(searchResult);
		SearchResult<CommunityUserDO> result = new SearchResult<CommunityUserDO>(
				searchResult.getNumFound(), new ArrayList<CommunityUserDO>());
		for (QuestionAnswerDO answer : searchResult.getDocuments()) {
			if (answer.getQuestionAnswerId().equals(withoutAnswerId)) {
				continue;
			}
			result.getDocuments().add(answer.getCommunityUser());
		}
		return result;
	}

	/**
	 * 質問の回答者数情報を返します。
	 * @param questionIds 質問IDリスト
	 * @return 質問の回答者数情報
	 */
	@Override
	public Map<String, Long> loadQuestionAnswerCountMapByQuestionId(List<String> questionIds) {
		Map<String, Long> resultMap = new HashMap<String, Long>();
		if (questionIds == null || questionIds.size() == 0) {
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
		for (int i = 0; i < questionIds.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("questionId_s:");
			buffer.append(questionIds.get(i));
		}
		buffer.append(")");
		SolrQuery query = new SolrQuery(buffer.toString());
		query.addFacetField("questionId_s");
		query.setFacetSort(FacetParams.FACET_SORT_COUNT);
		query.setFacetLimit(SolrConstants.QUERY_ROW_LIMIT);
		List<FacetResult<String>> searchResult = solrOperations.facet(
				QuestionAnswerDO.class, String.class, query);
		for (FacetResult<String> facetResult : searchResult) {
			resultMap.put(facetResult.getValue(), facetResult.getCount());
		}

		return resultMap;
	}

	/**
	 * 質問回答数情報を返します。
	 * @param skus SKUリスト
	 * @return 質問回答数情報
	 */
	@Override
	public Map<String, Long> loadQuestionAnswerCountMapBySKU(List<String> skus) {
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
			buffer.append(skus.get(i));
		}
		buffer.append(")");
		SolrQuery query = new SolrQuery(
				buffer.toString());
		query.addFacetField("productId_s");
		query.setFacetSort(FacetParams.FACET_SORT_COUNT);
		query.setFacetLimit(SolrConstants.QUERY_ROW_LIMIT);
		List<FacetResult<String>> searchResult = solrOperations.facet(
				QuestionAnswerDO.class, String.class, query);
		for (FacetResult<String> facetResult : searchResult) {
			resultMap.put(facetResult.getValue(), facetResult.getCount());
		}

		return resultMap;
	}

	/**
	 * 質問回答数情報を返します。
	 * @param communityUserIds コミュニティユーザーIDのリスト
	 * @return 質問回答数情報
	 */
	@Override
	public Map<String, Long> loadQuestionAnswerCountMapByCommunityUserId(
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
		SolrQuery query = new SolrQuery(
				new AdultHelper(requestScopeDao.loadAdultVerification(
										)).toFilterQuery(buffer.toString()));
		query.addFacetField("communityUserId_s");
		query.setFacetSort(FacetParams.FACET_SORT_COUNT);
		query.setFacetLimit(SolrConstants.QUERY_ROW_LIMIT);
		List<FacetResult<String>> searchResult = solrOperations.facet(
				QuestionAnswerDO.class, String.class, query);
		for (FacetResult<String> facetResult : searchResult) {
			resultMap.put(facetResult.getValue(), facetResult.getCount());
		}

		return resultMap;
	}

	/**
	 * 質問回答のスコア情報をインデックスも合わせて更新します。
	 * @param questionAnswer 質問回答
	 */
	@Override
	public void updateQuestionAnswerScoreWithIndex(
			QuestionAnswerDO questionAnswer) {
		hBaseOperations.save(questionAnswer,
				Path.includeProp("questionAnswerScore,modifyDateTime"));
		updateQuestionAnswerInIndex(
				questionAnswer.getQuestionAnswerId(), questionAnswer);
	}
	
	private BulkUpdate<QuestionAnswerDO> bulkUpdate = null;
	@Override
	public void updateQuestionAnswerScoreWithIndexForBatch(QuestionAnswerDO questionAnswer) {
		// modifyDateTime更新がいるのでは
		questionAnswer.setModifyDateTime(timestampHolder.getTimestamp());
		bulkUpdate.write(questionAnswer);
	}
	@Override
	public void updateQuestionAnswerScoreWithIndexForBatchBegin(int bulkSize) {
		bulkUpdate = new BulkUpdate<QuestionAnswerDO>(QuestionAnswerDO.class, hBaseOperations, solrOperations,
				Path.includeProp("questionAnswerScore,modifyDateTime"), bulkSize);
	}
	@Override
	public void updateQuestionAnswerScoreWithIndexForBatchEnd() {
		bulkUpdate.end();
	}
	
	/**
	 * 指定した質問中で一番スコアの高い回答を返します。
	 * @param questionId 質問ID
	 * @return 一番スコアの高い回答
	 */
	@Override
	public QuestionAnswerDO loadHighScoreQuestionAnswerByQuestionId(
			String questionId) {
		SolrQuery query = new SolrQuery(
				"withdraw_b:false AND questionId_s:" + questionId
				+ " AND ( "
				+ "status_s:"
				+ ContentsStatus.SUBMITTED.getCode()
				+ " OR status_s:"
				+ ContentsStatus.CONTENTS_STOP.getCode()
				+ " )");
		query.addSortField("questionAnswerScore_d", ORDER.desc);
		query.setRows(1);
		SearchResult<QuestionAnswerDO> searchResult = new SearchResult<QuestionAnswerDO>(
				solrOperations.findByQuery(
				query, QuestionAnswerDO.class, Path.includeProp("*").includePath(
				"communityUser.communityUserId").depth(1)));
		if (searchResult.getDocuments().size() > 0) {
			return searchResult.getDocuments().get(0);
		} else {
			return null;
		}
	}

	/**
	 * 指定したコミュニティユーザーが投稿した質問回答を回答投稿日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param status ステータス
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 質問回答一覧
	 */
	@Override
	public SearchResult<QuestionAnswerDO> findQuestionAnswerByCommunityUserId(
			String communityUserId,
			String excludeAnswerId,
			int limit,
			Date offsetTime,
			boolean previous) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND communityUserId_s:" + SolrUtil.escape(communityUserId));
		
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

		if(!StringUtils.isEmpty(excludeAnswerId))
			buffer.append(" AND !questionAnswerId:" + excludeAnswerId);
		
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
		SearchResult<QuestionAnswerDO> searchResult = new SearchResult<QuestionAnswerDO>(
				solrOperations.findByQuery(query, QuestionAnswerDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId," +
						"question.communityUser.communityUserId," +
						"product.sku").depth(2)));
		
		// hBaseから取得しなおす
		List<String> answerIds = new ArrayList<String>();
		for (QuestionAnswerDO answerDO : searchResult.getDocuments()) {
			answerIds.add(answerDO.getQuestionAnswerId());
		}
		Map<String, QuestionAnswerDO> answerMap = hBaseOperations.find(QuestionAnswerDO.class, String.class, answerIds, Path.includeProp("*").includePath(
				"communityUser.communityUserId," +
				"question.communityUser.communityUserId," +
				"product.sku").depth(2));
		
//		if( !answerMap.isEmpty() ){
//			List<QuestionAnswerDO> hBaseResultList = new ArrayList<QuestionAnswerDO>();
//			for (String answerId : answerIds) {
//				if (answerMap.containsKey(answerId)) {
//					hBaseResultList.add(answerMap.get(answerId));
//				}
//			}
//			searchResult.setDocuments(hBaseResultList);	// hBaseから取得し格納しなおす
//		}
		
		if( !answerMap.isEmpty() ){
			Iterator<Entry<String, QuestionAnswerDO>> answerInterator = answerMap.entrySet().iterator();
			Entry<String, QuestionAnswerDO> entry = null;
			List<QuestionAnswerDO> answers = Lists.newArrayList();
			while( answerInterator.hasNext() ){
				entry = answerInterator.next();
				answers.add(entry.getValue());
				
			}
			Collections.sort(answers, new Comparator<QuestionAnswerDO>() {
				@Override
				public int compare(QuestionAnswerDO o1, QuestionAnswerDO o2) {
					return o2.getPostDate().compareTo(o1.getPostDate());
				}
				
			});
			searchResult.setDocuments(answers);
		}

		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(
							buffer.toString(),
							QuestionAnswerDO.class, solrOperations));
		}
		ProductUtil.filterInvalidProduct(searchResult);
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}
	
	@Override
	public SearchResult<QuestionAnswerDO> findTemporaryQuestionAnswerByCommunityUserId(
			String communityUserId,
			String excludeAnswerId,
			int limit,
			Date offsetTime,
			boolean previous) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND communityUserId_s:" + communityUserId);

		buffer.append(" AND status_s:");
		buffer.append(ContentsStatus.SAVE.getCode());

		if(!StringUtils.isEmpty(excludeAnswerId))
			buffer.append(" AND !questionAnswerId:" + excludeAnswerId);
		
		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND saveDate_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND saveDate_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}
		AdultHelper adultHelper = new AdultHelper(requestScopeDao.loadAdultVerification());
		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(buffer.toString()));
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("saveDate_dt", ORDER.desc);
		} else {
			query.setSortField("saveDate_dt", ORDER.asc);
		}
		SearchResult<QuestionAnswerDO> searchResult = new SearchResult<QuestionAnswerDO>(
				solrOperations.findByQuery(query, QuestionAnswerDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId," +
						"question.communityUser.communityUserId," +
						"product.sku").depth(2)));
		
		// hBaseから取得しなおす
		List<String> answerIds = new ArrayList<String>();
		for (QuestionAnswerDO answerDO : searchResult.getDocuments()) {
			answerIds.add(answerDO.getQuestionAnswerId());
		}
		Map<String, QuestionAnswerDO> answerMap = hBaseOperations.find(QuestionAnswerDO.class, String.class, answerIds, Path.includeProp("*").includePath(
				"communityUser.communityUserId," +
				"question.communityUser.communityUserId," +
				"product.sku").depth(2));
		
//		if( !answerMap.isEmpty() ){
//			List<QuestionAnswerDO> hBaseResultList = new ArrayList<QuestionAnswerDO>();
//			for (String answerId : answerIds) {
//				if (answerMap.containsKey(answerId)) {
//					hBaseResultList.add(answerMap.get(answerId));
//				}
//			}
//			searchResult.setDocuments(hBaseResultList);	// hBaseから取得し格納しなおす
//		}
		
		if( !answerMap.isEmpty() ){
			Iterator<Entry<String, QuestionAnswerDO>> answerInterator = answerMap.entrySet().iterator();
			Entry<String, QuestionAnswerDO> entry = null;
			List<QuestionAnswerDO> answers = Lists.newArrayList();
			while( answerInterator.hasNext() ){
				entry = answerInterator.next();
				answers.add(entry.getValue());
				
			}
			Collections.sort(answers, new Comparator<QuestionAnswerDO>() {
				@Override
				public int compare(QuestionAnswerDO o1, QuestionAnswerDO o2) {
					return o2.getSaveDate().compareTo(o1.getSaveDate());
				}
				
			});
			searchResult.setDocuments(answers);
		}

		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(
							buffer.toString(),
							QuestionAnswerDO.class, solrOperations));
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
	 * 指定した質問に対して投稿した質問回答を回答投稿日時順（降順）に返します。
	 * @param questionId 質問ID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 質問回答一覧
	 */
	@Override
	public SearchResult<QuestionAnswerDO> findNewQuestionAnswerByQuestionId(
			String questionId, String excludeAnswerId, int limit, Date offsetTime, boolean previous) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND questionId_s:" + questionId);
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		if(!StringUtils.isEmpty(excludeAnswerId))
			buffer.append(" AND !questionAnswerId:" + excludeAnswerId);

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
		SearchResult<QuestionAnswerDO> searchResult = new SearchResult<QuestionAnswerDO>(
				solrOperations.findByQuery(query, QuestionAnswerDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId").depth(1)));
		ProductUtil.filterInvalidProduct(searchResult);
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}

	/**
	 * 指定した質問に対して投稿した質問回答を適合度順（降順）に返します。
	 * @param questionId 質問ID
	 * @param limit 最大取得件数
	 * @param offsetMatchScore 検索開始適合度
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 質問回答一覧
	 */
	@Override
	public SearchResult<QuestionAnswerDO> findMatchQuestionAnswerByQuestionId(
			String questionId, String excludeAnswerId, int limit, Double offsetMatchScore,
			Date offsetTime, boolean previous) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND questionId_s:" + questionId);
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		if(!StringUtils.isEmpty(excludeAnswerId))
			buffer.append(" AND !questionAnswerId:" + excludeAnswerId);

		if (offsetMatchScore != null) {
			if (previous) {
				buffer.append(" AND ((");
				buffer.append("questionAnswerScore_d:[" +
						offsetMatchScore + " TO *]");
				buffer.append(" AND postDate_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
				buffer.append(") OR ");
				buffer.append("questionAnswerScore_d:{" +
						offsetMatchScore + " TO *}");
				buffer.append(")");
			} else {
				buffer.append(" AND ((");
				buffer.append("questionAnswerScore_d:[* TO " +
						offsetMatchScore + "]");
				buffer.append(" AND postDate_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
				buffer.append(") OR ");
				buffer.append("questionAnswerScore_d:{* TO " +
						offsetMatchScore + "}");
				buffer.append(")");
			}
		}
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(limit);
		if (offsetMatchScore == null || !previous) {
			query.setSortField("questionAnswerScore_d", ORDER.desc);
			query.addSortField("postDate_dt", ORDER.desc);
		} else {
			query.setSortField("questionAnswerScore_d", ORDER.asc);
			query.addSortField("postDate_dt", ORDER.asc);
		}
		SearchResult<QuestionAnswerDO> searchResult = new SearchResult<QuestionAnswerDO>(
				solrOperations.findByQuery(query, QuestionAnswerDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId").depth(1)));
		ProductUtil.filterInvalidProduct(searchResult);
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}

	/**
	 * 投稿質問回答数を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @return 投稿質問回答数
	 */
	@Override
	public long countPostQuestionAnswerCount(String communityUserId, String sku) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND  ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" AND communityUserId_s:");
		buffer.append(communityUserId);
		if (!StringUtils.isEmpty(sku)) {
			buffer.append(" AND productId_s:");
			buffer.append(sku);
		}

		SolrQuery solrQuery = new SolrQuery(new AdultHelper(
				requestScopeDao.loadAdultVerification()).toFilterQuery(buffer.toString()));
		return solrOperations.count(solrQuery, QuestionAnswerDO.class);
	}

	/**
	 * 投稿質問回答数を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param status ステータス
	 * @param アダルト確認フラグ
	 * @return 投稿質問回答数
	 */
	@Override
	@MethodCache(
			cacheStrategy=CacheStrategyType.HttpSession,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityDataSyncWeb}
			)
	public long countPostQuestionAnswerCount(
			String communityUserId,
			ContentsStatus status,
			Verification adultVerification) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND status_s:");
		buffer.append(status.getCode());
		buffer.append(" AND communityUserId_s:");
		buffer.append(communityUserId);

		SolrQuery solrQuery = new SolrQuery(new AdultHelper(adultVerification).toFilterQuery(buffer.toString()));
		return solrOperations.count(solrQuery, QuestionAnswerDO.class);
	}

	/**
	 * 投稿質問回答数を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param status ステータス
	 * @param アダルト確認フラグ
	 * @return 投稿質問回答数
	 */
	@Override
	@MethodCache(
			cacheStrategy=CacheStrategyType.HttpSession,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityDataSyncWeb}
			)
	public long countPostQuestionAnswerCountForMypage(
			String communityUserId,
			Verification adultVerification) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		buffer.append(" AND communityUserId_s:");
		buffer.append(communityUserId);

		SolrQuery solrQuery = new SolrQuery(new AdultHelper(
						adultVerification).toFilterQuery(buffer.toString()));
		return solrOperations.count(solrQuery, QuestionAnswerDO.class);
	}
	
	/**
	 * 指定した日付に指定したコミュニティユーザーの投稿質問に
	 * 回答がついたものを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param targetDate 対象日
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return 検索結果
	 */
	@Override
	public SearchResult<QuestionAnswerDO> findQuestionAnswerByCommunityUserQuestion(
			String communityUserId,
			Date targetDate, int limit, int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" AND relationQuestionOwnerId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND ");
		buffer.append(SolrUtil.getSolrDateRangeQuery("postDate_dt", targetDate));
		SearchResult<QuestionAnswerDO> searchResult = new SearchResult<QuestionAnswerDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
						).toFilterQuery(buffer.toString())).setRows(limit).setStart(offset
								).setSortField("postDate_dt", ORDER.asc),
						QuestionAnswerDO.class, Path.includeProp("*").includePath(
						"product.sku,question.questionId,communityUser.communityUserId").depth(1)));
		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}

	@Override
	public SearchResult<QuestionAnswerDO> findQuestionAnswerByCommunityUserQuestionForMR(
			String communityUserId,
			Date targetDate, int limit, int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" AND relationQuestionOwnerId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND ");
		buffer.append(SolrUtil.getSolrDateRangeQuery("postDate_dt", targetDate));
		SearchResult<QuestionAnswerDO> searchResult = new SearchResult<QuestionAnswerDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
						).toFilterQuery(buffer.toString())).setRows(limit).setStart(offset
								).setSortField("postDate_dt", ORDER.asc),
						QuestionAnswerDO.class, Path.includeProp("*").includePath(
						"question.questionId,communityUser.communityUserId").depth(1)));

		List<String> skus = new ArrayList<String>();
		for(QuestionAnswerDO answer:searchResult.getDocuments()) {
			skus.add(answer.getProduct().getSku());
		}
		Map<String, ProductDO> productMap = productDao.findBySkuForMR(skus);
		for(QuestionAnswerDO answer:searchResult.getDocuments()) {
			answer.setProduct(productMap.get(answer.getProduct().getSku()));
		}		
		
		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}

	
	/**
	 * 指定したコミュニティユーザーが回答した質問の別の回答を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param publicDate 公開された日付
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return 質問回答リスト
	 */
	@Override
	public SearchResult<QuestionAnswerDO> findAnotherQuestionAnswerByCommunityUserAnswer(
			String communityUserId, Date publicDate, int limit, int offset) {
		SolrQuery query = new SolrQuery(
				"withdraw_b:false AND communityUserId_s:" + SolrUtil.escape(communityUserId)
				+ " AND "
				+ "status_s:"
				+ ContentsStatus.SUBMITTED.getCode());
		query.addFacetField("questionId_s");
		query.setFacetLimit(SolrConstants.QUERY_ROW_LIMIT);
		List<FacetResult<String>> questions
				= solrOperations.facet(QuestionAnswerDO.class, String.class, query);
		if (questions.size() == 0) {
			return new SearchResult<QuestionAnswerDO>();
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		buffer.append(" AND !communityUserId_s:" + SolrUtil.escape(communityUserId));
		buffer.append(" AND (");
		for (int i = 0; i < questions.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("questionId_s:");
			buffer.append(SolrUtil.escape(questions.get(i).getValue()));
		}
		buffer.append(") AND ");
		buffer.append(SolrUtil.getSolrDateRangeQuery("postDate_dt", publicDate));
		SearchResult<QuestionAnswerDO> searchResult = new SearchResult<QuestionAnswerDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
						).toFilterQuery(buffer.toString())).setRows(limit).setStart(offset
								).setSortField("postDate_dt", ORDER.asc),
						QuestionAnswerDO.class, Path.includeProp("*").includePath(
						"product.sku,question.questionId,communityUser.communityUserId").depth(1)));
		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}
	
	@Override
	public SearchResult<QuestionAnswerDO> findAnotherQuestionAnswerByCommunityUserAnswerForMR(
			String communityUserId, Date publicDate, int limit, int offset) {
		SolrQuery query = new SolrQuery(
				"withdraw_b:false AND communityUserId_s:" + SolrUtil.escape(communityUserId)
				+ " AND "
				+ "status_s:"
				+ ContentsStatus.SUBMITTED.getCode());
		query.addFacetField("questionId_s");
		query.setFacetLimit(SolrConstants.QUERY_ROW_LIMIT);
		List<FacetResult<String>> questions
				= solrOperations.facet(QuestionAnswerDO.class, String.class, query);
		if (questions.size() == 0) {
			return new SearchResult<QuestionAnswerDO>();
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		buffer.append(" AND !communityUserId_s:" + SolrUtil.escape(communityUserId));
		buffer.append(" AND (");
		for (int i = 0; i < questions.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("questionId_s:");
			buffer.append(SolrUtil.escape(questions.get(i).getValue()));
		}
		buffer.append(") AND ");
		buffer.append(SolrUtil.getSolrDateRangeQuery("postDate_dt", publicDate));
		SearchResult<QuestionAnswerDO> searchResult = new SearchResult<QuestionAnswerDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
						).toFilterQuery(buffer.toString())).setRows(limit).setStart(offset
								).setSortField("postDate_dt", ORDER.asc),
						QuestionAnswerDO.class, Path.includeProp("*").includePath(
						"question.questionId,communityUser.communityUserId").depth(1)));
		
		List<String> skus = new ArrayList<String>();
		for(QuestionAnswerDO answer:searchResult.getDocuments()) {
			skus.add(answer.getProduct().getSku());
		}
		Map<String, ProductDO> productMap = productDao.findBySkuForMR(skus);
		for(QuestionAnswerDO answer:searchResult.getDocuments()) {
			answer.setProduct(productMap.get(answer.getProduct().getSku()));
		}		
		
		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}

	/**
	 * 指定したコミュニティユーザーが指定した日付に投稿した質問回答を返します。
	 * @param communityUserIds コミュニティユーザーIDリスト
	 * @param publicDate 公開された日付
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return 質問回答リスト
	 */
	@Override
	public SearchResult<QuestionAnswerDO> findQuestionAnswerByCommunityUserIds(
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
		SearchResult<QuestionAnswerDO> searchResult = new SearchResult<QuestionAnswerDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
						).toFilterQuery(buffer.toString())).setRows(limit).setStart(offset
								).setSortField("postDate_dt", ORDER.asc),
						QuestionAnswerDO.class, Path.includeProp("*").includePath(
						"product.sku,question.questionId,communityUser.communityUserId").depth(1)));
		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}

	@Override
	public SearchResult<QuestionAnswerDO> findQuestionAnswerByCommunityUserIdsForMR(
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
		SearchResult<QuestionAnswerDO> searchResult = new SearchResult<QuestionAnswerDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
						).toFilterQuery(buffer.toString())).setRows(limit).setStart(offset
								).setSortField("postDate_dt", ORDER.asc),
						QuestionAnswerDO.class, Path.includeProp("*").includePath(
						"question.questionId,communityUser.communityUserId").depth(1)));
		
		List<String> skus = new ArrayList<String>();
		for(QuestionAnswerDO answer:searchResult.getDocuments()) {
			skus.add(answer.getProduct().getSku());
		}
		Map<String, ProductDO> productMap = productDao.findBySkuForMR(skus);
		for(QuestionAnswerDO answer:searchResult.getDocuments()) {
			answer.setProduct(productMap.get(answer.getProduct().getSku()));
		}		
		
		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}
	
	/**
	 * 指定した質問、日付に回答した質問回答を返します。
	 * @param questionIds 質問IDリスト
	 * @param publicDate 公開された日付
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return 質問回答リスト
	 */
	@Override
	public SearchResult<QuestionAnswerDO> findQuestionAnswerByQuestionIds(
			List<String> questionIds, Date publicDate, String excludeCommunityId, int limit, int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		if(!StringUtils.isEmpty(excludeCommunityId))
			buffer.append(" AND !communityUserId_s:" + SolrUtil.escape(excludeCommunityId));

		buffer.append(" AND (");
		for (int i = 0; i < questionIds.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("questionId_s:");
			buffer.append(SolrUtil.escape(questionIds.get(i)));
		}
		buffer.append(") AND ");
		buffer.append(SolrUtil.getSolrDateRangeQuery("postDate_dt", publicDate));
		SearchResult<QuestionAnswerDO> searchResult
		= new SearchResult<QuestionAnswerDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
						).toFilterQuery(buffer.toString())).setRows(limit).setStart(offset
								).setSortField("postDate_dt", ORDER.asc),
						QuestionAnswerDO.class, Path.includeProp("*").includePath(
						"product.sku,question.questionId,communityUser.communityUserId").depth(1)));
		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}

	@Override
	public SearchResult<QuestionAnswerDO> findQuestionAnswerByQuestionIdsForMR(
			List<String> questionIds, Date publicDate, String excludeCommunityId, int limit, int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		if(!StringUtils.isEmpty(excludeCommunityId))
			buffer.append(" AND !communityUserId_s:" + SolrUtil.escape(excludeCommunityId));

		buffer.append(" AND (");
		for (int i = 0; i < questionIds.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("questionId_s:");
			buffer.append(SolrUtil.escape(questionIds.get(i)));
		}
		buffer.append(") AND ");
		buffer.append(SolrUtil.getSolrDateRangeQuery("postDate_dt", publicDate));
		SearchResult<QuestionAnswerDO> searchResult
		= new SearchResult<QuestionAnswerDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
						).toFilterQuery(buffer.toString())).setRows(limit).setStart(offset
								).setSortField("postDate_dt", ORDER.asc),
						QuestionAnswerDO.class, Path.includeProp("*").includePath(
						"question.questionId,communityUser.communityUserId").depth(1)));

		List<String> skus = new ArrayList<String>();
		for(QuestionAnswerDO answer:searchResult.getDocuments()) {
			skus.add(answer.getProduct().getSku());
		}
		Map<String, ProductDO> productMap = productDao.findBySkuForMR(skus);
		for(QuestionAnswerDO answer:searchResult.getDocuments()) {
			answer.setProduct(productMap.get(answer.getProduct().getSku()));
		}		
		
		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}
	
	
	/**
	 * 指定した条件の質問情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param questionId 質問ID
	 * @return 質問情報リスト
	 */
	@Override
	public List<QuestionAnswerDO> findQuestionAnswerByCommunityUserIdAndQuestionId(
			String communityUserId, String questionId) {
		return findQuestionAnswerByCommunityUserIdAndQuestionId(
				communityUserId, questionId, getDefaultLoadQuestionAnswerCondition());
	}

	/**
	 * 指定した質問回答情報を返します。
	 * @param questionAnswerId 質問回答ID
	 * @return 質問回答情報
	 */
	@Override
	public QuestionAnswerDO loadQuestionAnswer(String questionAnswerId) {
		return loadQuestionAnswer(questionAnswerId,
				getDefaultLoadQuestionAnswerCondition(), false);
	}

	/**
	 * 指定した質問回答情報をインデックス情報から返します。
	 * @param questionAnswerId 質問回答ID
	 * @return 質問回答情報
	 */
	@Override
	public QuestionAnswerDO loadQuestionAnswerFromIndex(String questionAnswerId) {
		return loadQuestionAnswerFromIndex(questionAnswerId, true);
	}

	@Override
	public QuestionAnswerDO loadQuestionAnswerFromIndex(
			String questionAnswerId, boolean includeDeleteContents) {

		StringBuilder buffer = new StringBuilder();
		buffer.append("questionAnswerId:");
		buffer.append(questionAnswerId);
		if(!includeDeleteContents){
			buffer.append(" AND !status_s:");
			buffer.append(ContentsStatus.DELETE.getCode());
		}

		SearchResult<QuestionAnswerDO> results = new SearchResult<QuestionAnswerDO>(
				solrOperations.findByQuery(
						new SolrQuery(buffer.toString()),
						QuestionAnswerDO.class,
						getDefaultLoadQuestionAnswerCondition()));
		ProductUtil.filterInvalidProduct(results);

		if(results == null || results.getDocuments().isEmpty() || results.getDocuments().size() > 1)
			return null;

		return results.getDocuments().get(0);
	}


	/**
	 * 質問回答情報を読み出すデフォルト条件を返します。
	 * @return 質問情報の読み出し条件
	 */
	private Condition getDefaultLoadQuestionAnswerCondition() {
		return Path.includeProp("*").includePath(
				"product.sku,communityUser.communityUserId," +
				"question.product.sku,question.communityUser.communityUserId").depth(2);
	}

	@Override
	public SearchResult<QuestionAnswerDO> findTemporaryQuestionAnswerByBeforeInterval(Date intervalDate) {

		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND status_s:");
		buffer.append(SolrUtil.escape(ContentsStatus.SAVE.getCode()));
		buffer.append(" AND registerDateTime_dt:{" +
				"* TO " + DateUtil.getThreadLocalDateFormat().format(intervalDate) + "}");
		SearchResult<QuestionAnswerDO> searchResult = new SearchResult<QuestionAnswerDO>(
				solrOperations.findByQuery(new SolrQuery(buffer.toString()),QuestionAnswerDO.class));
		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}

	@Override
	public void removeQuestionAnswers(List<String> questionAnswerIds) {
		hBaseOperations.deleteByKeys(QuestionAnswerDO.class, String.class, questionAnswerIds);
		solrOperations.deleteByKeys(QuestionAnswerDO.class, String.class, questionAnswerIds);
	}

	/**
	 * 指定したコミュニティユーザーの保存回答を削除します。
	 * @param reviewId 回答ID
	 */
	@Override
	public void removeTemporaryQuestionAnswer(String communityUserId) {
		List<QuestionAnswerDO> questionAnswers = hBaseOperations.findWithIndex(QuestionAnswerDO.class, "communityUserId",Path.includeProp("questionAnswerId,status"), communityUserId);
		if(questionAnswers == null || questionAnswers.isEmpty()) return;
		List<String> temporaryAnswerIds = new ArrayList<String>();
		for(QuestionAnswerDO questionAnswer:questionAnswers ){
			if(questionAnswer.getStatus().equals(ContentsStatus.SAVE))
				temporaryAnswerIds.add(questionAnswer.getQuestionAnswerId());
		}
		if(!temporaryAnswerIds.isEmpty()){
			hBaseOperations.deleteByKeys(QuestionAnswerDO.class, String.class, temporaryAnswerIds);
			solrOperations.deleteByKeys(QuestionAnswerDO.class, String.class, temporaryAnswerIds);
		}
	}

	@Override
	public String findProductSku(String questionAnswerId) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("questionAnswerId:");
		buffer.append(questionAnswerId);
		
		SearchResult<QuestionAnswerDO> results = new SearchResult<QuestionAnswerDO>(
				solrOperations.findByQuery(
						new SolrQuery(buffer.toString()),
						QuestionAnswerDO.class,
						Path.includeProp("*").includePath("product.sku").depth(1)));
		
		if(results == null || results.getDocuments().isEmpty() || results.getDocuments().size() > 1 || results.getDocuments().get(0).getProduct() == null)
			return null;
		return results.getDocuments().get(0).getProduct().getSku();
	}


}
