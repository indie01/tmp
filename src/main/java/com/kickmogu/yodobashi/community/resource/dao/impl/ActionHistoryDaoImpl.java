/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.common.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.id.IDGenerator;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.solr.SolrConstants;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.resource.dao.ActionHistoryDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.util.AdultHelper;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.ProductUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.SolrUtil;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.VotingDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.AsyncMessageType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.LikeTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;
import com.kickmogu.yodobashi.community.resource.domain.constants.VotingTargetType;
import com.kickmogu.yodobashi.community.service.annotation.SendMessage;
import com.kickmogu.yodobashi.community.service.annotation.SendMessage.Timing;

@Service
public class ActionHistoryDaoImpl implements ActionHistoryDao {

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(ActionHistoryDaoImpl.class);
	/**
	 * HBaseアクセサです。
	 */
	@Autowired  @Qualifier("default")
	protected HBaseOperations hBaseOperations;

	/**
	 * Solrアクセサです。
	 */
	@Autowired @Qualifier("default")
	private SolrOperations solrOperations;

	/**
	 * タイムスタンプホルダーです。
	 */
	@Autowired
	protected TimestampHolder timestampHolder;

	/**
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	private RequestScopeDao requestScopeDao;

	@Autowired @Qualifier("default")
	private IDGenerator<String> idGenerator;

	/**
	 * アクション履歴を登録します。
	 * @param actionHistory アクション履歴
	 */
	@Override
	public void create(ActionHistoryDO actionHistory) {
		actionHistory.setActionHistoryId(IdUtil.generateActionHistoryId(actionHistory, idGenerator));
		actionHistory.setActionTime(timestampHolder.getTimestamp());
		actionHistory.setRegisterDateTime(timestampHolder.getTimestamp());
		actionHistory.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(actionHistory);
	}

	/**
	 * アクション履歴を登録します。
	 * @param actionHistories アクション履歴
	 */
	@Override
	public void create(List<ActionHistoryDO> actionHistories) {
		List<ActionHistoryDO> saveActionHistories = new ArrayList<ActionHistoryDO>();
		for(ActionHistoryDO actionHistory : actionHistories) {
			actionHistory.setActionHistoryId(IdUtil.generateActionHistoryId(actionHistory, idGenerator));
			actionHistory.setActionTime(timestampHolder.getTimestamp());
			actionHistory.setRegisterDateTime(timestampHolder.getTimestamp());
			actionHistory.setModifyDateTime(timestampHolder.getTimestamp());
			saveActionHistories.add(actionHistory);
		}
		hBaseOperations.save(ActionHistoryDO.class, saveActionHistories);
	}

	/**
	 * アクション履歴のインデックスを更新します。
	 * @param actionHistoryId アクション履歴ID
	 * @return アクション履歴
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS, asyncMessageType=AsyncMessageType.ACTIONHISTORY)
	public ActionHistoryDO updateActionHistoryInIndex(
			String actionHistoryId) {
		ActionHistoryDO actionHistory = hBaseOperations.load(
				ActionHistoryDO.class, actionHistoryId);
		
		if (actionHistory == null || actionHistory.isDeleted()) {
			solrOperations.deleteByKey(ActionHistoryDO.class, actionHistoryId);
			return null;
		} else {
			solrOperations.save(actionHistory);
			return actionHistory;
		}
	}

	/**
	 * アクション履歴のインデックスを更新します。
	 * @param actionHistory アクション履歴
	 * @return アクション履歴
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS, asyncMessageType=AsyncMessageType.ACTIONHISTORY)
	public ActionHistoryDO updateActionHistoryInIndex(ActionHistoryDO actionHistory) {
		if (actionHistory == null )
			throw new IllegalArgumentException("actionHistory argument is null");
		
		if (actionHistory.isDeleted()) {
			solrOperations.deleteByKey(ActionHistoryDO.class, actionHistory.getActionHistoryId());
			return null;
		} else {
			solrOperations.save(actionHistory);
			return actionHistory;
		}
	}
	
	/**
	 * マイルストーンごとのいいね記録を登録すべきか判定します。
	 * @param like いいね
	 * @param threshold 閾値
	 * @return 登録すべきである場合、true
	 */
	@Override
	public boolean requiredSaveLikeMilstone(LikeDO like, int threshold) {
		//インデックスには Like 自体がカウントされていない事を考慮して 1 引きます。
		int indexThreshold = threshold - 1;
		if (like.getTargetType().equals(LikeTargetType.REVIEW)) {
			return solrOperations.findByQuery(new SolrQuery(
					"reviewId_s:" + SolrUtil.escape(like.getReview().getReviewId())
					).setRows(0), LikeDO.class).getNumFound() >= indexThreshold &&
					hBaseOperations.load(ActionHistoryDO.class,
							createLikeActionHistoryId(like, threshold)) == null;
		} else if (like.getTargetType().equals(LikeTargetType.QUESTION_ANSWER)) {
			return solrOperations.findByQuery(new SolrQuery(
					"questionAnswerId_s:" + SolrUtil.escape(like.getQuestionAnswer().getQuestionAnswerId())
					).setRows(0), LikeDO.class).getNumFound() >= indexThreshold &&
					hBaseOperations.load(ActionHistoryDO.class,
							createLikeActionHistoryId(like, threshold)) == null;
		} else {
			return solrOperations.findByQuery(new SolrQuery(
					"imageHeaderId_s:" + SolrUtil.escape(like.getImageHeader().getImageId())
					).setRows(0), LikeDO.class).getNumFound() >= indexThreshold &&
					hBaseOperations.load(ActionHistoryDO.class,
							createLikeActionHistoryId(like, threshold)) == null;
		}
	}

	/**
	 * いいねアクション履歴IDを生成します。
	 * @param like いいね
	 * @param threshold 閾値
	 * @return いいねアクション履歴ID
	 */
	@Override
	public String createLikeActionHistoryId(LikeDO like, int threshold) {
		if (like.getTargetType().equals(LikeTargetType.REVIEW)) {
			return IdUtil.withHashPrefix(IdUtil.createIdByConcatIds(
					like.getTargetType().getCode(),
					like.getReview().getReviewId(),
					String.valueOf(threshold)));
		} else if (like.getTargetType().equals(LikeTargetType.QUESTION_ANSWER)) {
			return IdUtil.withHashPrefix(IdUtil.createIdByConcatIds(
					like.getTargetType().getCode(),
					like.getQuestionAnswer().getQuestionAnswerId(),
					String.valueOf(threshold)));
		} else {
			return IdUtil.withHashPrefix(IdUtil.createIdByConcatIds(
					like.getTargetType().getCode(),
					like.getImageHeader().getImageId(),
					String.valueOf(threshold)));
		}
	}
	
	/**
	 * マイルストーンごとの参考になった記録を登録すべきか判定します。
	 * @param voting 参考になった
	 * @param threshold 閾値
	 * @return 登録すべきである場合、true
	 */
	@Override
	public boolean requiredSaveVotingMilstone(VotingDO voting, int threshold) {
		//インデックスには Voting 自体がカウントされていない事を考慮して 1 引きます。
		int indexThreshold = threshold - 1;
		if (voting.getTargetType().equals(VotingTargetType.REVIEW)) {
			return solrOperations.findByQuery(new SolrQuery(
					"reviewId_s:" + SolrUtil.escape(voting.getReview().getReviewId())
					).setRows(0), VotingDO.class).getNumFound() >= indexThreshold &&
					hBaseOperations.load(ActionHistoryDO.class,
							createVotingActionHistoryId(voting, threshold)) == null;
		} else if (voting.getTargetType().equals(VotingTargetType.QUESTION_ANSWER)) {
			return solrOperations.findByQuery(new SolrQuery(
					"questionAnswerId_s:" + SolrUtil.escape(voting.getQuestionAnswer().getQuestionAnswerId())
					).setRows(0), VotingDO.class).getNumFound() >= indexThreshold &&
					hBaseOperations.load(ActionHistoryDO.class,
							createVotingActionHistoryId(voting, threshold)) == null;
		} else {
			return solrOperations.findByQuery(new SolrQuery(
					"imageHeaderId_s:" + SolrUtil.escape(voting.getImageHeader().getImageId())
					).setRows(0), VotingDO.class).getNumFound() >= indexThreshold &&
					hBaseOperations.load(ActionHistoryDO.class,
							createVotingActionHistoryId(voting, threshold)) == null;
		}
	}

	/**
	 * 参考になったアクション履歴IDを生成します。
	 * @param voting 参考になった
	 * @param threshold 閾値
	 * @return 参考になったアクション履歴ID
	 */
	@Override
	public String createVotingActionHistoryId(VotingDO voting, int threshold) {
		if (voting.getTargetType().equals(VotingTargetType.REVIEW)) {
			return IdUtil.withHashPrefix(IdUtil.createIdByConcatIds(
					voting.getTargetType().getCode(),
					voting.getReview().getReviewId(),
					String.valueOf(threshold)));
		} else if (voting.getTargetType().equals(VotingTargetType.QUESTION_ANSWER)) {
			return IdUtil.withHashPrefix(IdUtil.createIdByConcatIds(
					voting.getTargetType().getCode(),
					voting.getQuestionAnswer().getQuestionAnswerId(),
					String.valueOf(threshold)));
		} else {
			return IdUtil.withHashPrefix(IdUtil.createIdByConcatIds(
					voting.getTargetType().getCode(),
					voting.getImageHeader().getImageId(),
					String.valueOf(threshold)));
		}
	}

	/**
	 * 指定した商品のニュースフィード用アクション履歴をアクション日時順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return ニュースフィード用アクション履歴リスト
	 */
	@Override
	public SearchResult<ActionHistoryDO> findNewsFeedBySku(
			String sku, int limit, Date offsetTime, boolean previous) {
		return findNewsFeedBySku(sku, limit, offsetTime, previous, false);
	}
	
	@Override
	public SearchResult<ActionHistoryDO> findNewsFeedBySku(
			String sku, int limit, Date offsetTime, boolean previous, boolean excludeProduct) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("productId_s:" + sku);

		buffer.append(" AND (actionHistoryType_s:");
		buffer.append(ActionHistoryType.USER_REVIEW.getCode());
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(ActionHistoryType.USER_QUESTION.getCode());
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(ActionHistoryType.USER_ANSWER.getCode());
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(ActionHistoryType.USER_IMAGE.getCode());
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(ActionHistoryType.USER_FOLLOW_PRODUCT.getCode());
		buffer.append(" OR (actionHistoryType_s:");
		buffer.append(ActionHistoryType.USER_PRODUCT_MASTER_RANK_CHANGE.getCode());
		buffer.append(" AND productMasterRank_i:1)");
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(ActionHistoryType.LIKE_REVIEW_50.getCode());
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(ActionHistoryType.LIKE_ANSWER_50.getCode());
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(ActionHistoryType.LIKE_IMAGE_50.getCode());
		buffer.append(")");
		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND actionTime_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND actionTime_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("actionTime_dt", ORDER.desc);
		} else {
			query.setSortField("actionTime_dt", ORDER.asc);
		}
		
		List<ActionHistoryDO> actionHistories = new ArrayList<ActionHistoryDO>();

		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();
		
		Condition codition = null;
		if(excludeProduct){
			codition = Path.includeProp("*").includePath(
					"communityUser.communityUserId," +
					"question.questionId," +
					"questionAnswer.questionAnswerId," +
					"review.reviewId," +
					"review.reviewDecisivePurchases.decisivePurchase.decisivePurchaseId," +
					"imageHeader.imageId," +
					"productMaster.productMasterId").depth(3);
		}else{
			codition = Path.includeProp("*").includePath(
					"communityUser.communityUserId," +
					"product.sku," +
					"question.questionId," +
					"questionAnswer.questionAnswerId," +
					"review.reviewId," +
					"review.reviewDecisivePurchases.decisivePurchase.decisivePurchaseId," +
					"imageHeader.imageId," +
					"productMaster.productMasterId").depth(3);
		}
		
		SearchResult<ActionHistoryDO> results = new SearchResult<ActionHistoryDO>(
				solrOperations.findByQuery(query, ActionHistoryDO.class,codition));
		
		for(ActionHistoryDO action:results.getDocuments()){
			// コンテンツの一時停止対応
			if(ActionHistoryType.USER_REVIEW.equals(action.getActionHistoryType())){
				if(action.getReview() == null || 
					action.getReview().getCommunityUser() == null || 
					(!action.getReview().getCommunityUser().getCommunityUserId().equals(loginCommunityUserId)
					&& ContentsStatus.CONTENTS_STOP.equals(action.getReview().getStatus()))){
						continue;
				}
			}else if(ActionHistoryType.USER_QUESTION.equals(action.getActionHistoryType())){
				if(action.getQuestion() == null ||
					action.getQuestion().getCommunityUser() == null || 
					(!action.getQuestion().getCommunityUser().getCommunityUserId().equals(loginCommunityUserId)
					&& ContentsStatus.CONTENTS_STOP.equals(action.getQuestion().getStatus()))){
						continue;
				}
			}else if(ActionHistoryType.USER_ANSWER.equals(action.getActionHistoryType())){
				if(action.getQuestionAnswer() == null || 
					action.getQuestionAnswer().getCommunityUser() == null || 
					(!action.getQuestionAnswer().getCommunityUser().getCommunityUserId().equals(loginCommunityUserId)
					&& ContentsStatus.CONTENTS_STOP.equals(action.getQuestionAnswer().getStatus()))){
						continue;
				}
			}
			
			actionHistories.add(action);
		}
		SearchResult<ActionHistoryDO> searchResult = new SearchResult<ActionHistoryDO>(results.getNumFound(), actionHistories);
		if(!excludeProduct)
			ProductUtil.filterInvalidProduct(searchResult);
		// SolrにIndexされていない場合にHbaseからLoadする。
		loadHbaseContents(searchResult);
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}
	
	/**
	 * 指定した商品のニュースフィード用アクション履歴をアクション日時順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return ニュースフィード用アクション履歴リスト
	 */
	@Override
	public SearchResult<ActionHistoryDO> findNewsFeedBySkus(
			List<String> skus, int limit, Date offsetTime, boolean previous) {
		return findNewsFeedBySkus(skus, limit, offsetTime, previous, false);
	}
	
	@Override
	public SearchResult<ActionHistoryDO> findNewsFeedBySkus(
			List<String> skus, int limit, Date offsetTime, boolean previous, boolean excludeProduct) {
		StringBuilder buffer = new StringBuilder();
		if( skus.size() == 1){
			buffer.append("productId_s:" + skus.get(0));
		}else{
			buffer.append("(");
			for(int i=0; i<skus.size(); i++){
				buffer.append("productId_s:" + skus.get(i));
				if( i != skus.size()-1)
					buffer.append(" OR ");
			}
			buffer.append(")");
		}
		
		buffer.append(" AND (actionHistoryType_s:");
		buffer.append(ActionHistoryType.USER_REVIEW.getCode());
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(ActionHistoryType.USER_QUESTION.getCode());
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(ActionHistoryType.USER_ANSWER.getCode());
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(ActionHistoryType.USER_IMAGE.getCode());
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(ActionHistoryType.USER_FOLLOW_PRODUCT.getCode());
		buffer.append(" OR (actionHistoryType_s:");
		buffer.append(ActionHistoryType.USER_PRODUCT_MASTER_RANK_CHANGE.getCode());
		buffer.append(" AND productMasterRank_i:1)");
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(ActionHistoryType.LIKE_REVIEW_50.getCode());
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(ActionHistoryType.LIKE_ANSWER_50.getCode());
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(ActionHistoryType.LIKE_IMAGE_50.getCode());
		buffer.append(")");
		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND actionTime_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND actionTime_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("actionTime_dt", ORDER.desc);
		} else {
			query.setSortField("actionTime_dt", ORDER.asc);
		}
		
		List<ActionHistoryDO> actionHistories = new ArrayList<ActionHistoryDO>();

		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();
		
		Condition codition = null;
		if(excludeProduct){
			codition = Path.includeProp("*").includePath(
					"communityUser.communityUserId," +
					"question.questionId," +
					"questionAnswer.questionAnswerId," +
					"review.reviewId," +
					"review.reviewDecisivePurchases.decisivePurchase.decisivePurchaseId," +
					"imageHeader.imageId," +
					"productMaster.productMasterId").depth(3);
		}else{
			codition = Path.includeProp("*").includePath(
					"communityUser.communityUserId," +
					"product.sku," +
					"question.questionId," +
					"questionAnswer.questionAnswerId," +
					"review.reviewId," +
					"review.reviewDecisivePurchases.decisivePurchase.decisivePurchaseId," +
					"imageHeader.imageId," +
					"productMaster.productMasterId").depth(3);
		}
		
		SearchResult<ActionHistoryDO> results = new SearchResult<ActionHistoryDO>(
				solrOperations.findByQuery(query, ActionHistoryDO.class,codition));
		
		for(ActionHistoryDO action:results.getDocuments()){
			// コンテンツの一時停止対応
			if(ActionHistoryType.USER_REVIEW.equals(action.getActionHistoryType())){
				if(action.getReview() == null || 
					action.getReview().getCommunityUser() == null || 
					(!action.getReview().getCommunityUser().getCommunityUserId().equals(loginCommunityUserId)
					&& ContentsStatus.CONTENTS_STOP.equals(action.getReview().getStatus()))){
						continue;
				}
			}else if(ActionHistoryType.USER_QUESTION.equals(action.getActionHistoryType())){
				if(action.getQuestion() == null ||
					action.getQuestion().getCommunityUser() == null || 
					(!action.getQuestion().getCommunityUser().getCommunityUserId().equals(loginCommunityUserId)
					&& ContentsStatus.CONTENTS_STOP.equals(action.getQuestion().getStatus()))){
						continue;
				}
			}else if(ActionHistoryType.USER_ANSWER.equals(action.getActionHistoryType())){
				if(action.getQuestionAnswer() == null || 
					action.getQuestionAnswer().getCommunityUser() == null || 
					(!action.getQuestionAnswer().getCommunityUser().getCommunityUserId().equals(loginCommunityUserId)
					&& ContentsStatus.CONTENTS_STOP.equals(action.getQuestionAnswer().getStatus()))){
						continue;
				}
			}
			
			actionHistories.add(action);
		}
		SearchResult<ActionHistoryDO> searchResult = new SearchResult<ActionHistoryDO>(results.getNumFound(), actionHistories);
		if(!excludeProduct)
			ProductUtil.filterInvalidProduct(searchResult);
		// SolrにIndexされていない場合にHbaseからLoadする。
		loadHbaseContents(searchResult);
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}

	/**
	 * 指定したコミュニティユーザーのニュースフィード用アクション履歴をアクション日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return ニュースフィード用アクション履歴リスト
	 */
	@Override
	public SearchResult<ActionHistoryDO> findNewsFeedByCommunityUserId(
			String communityUserId, int limit, Date offsetTime, boolean previous) {
		
		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();
		StringBuilder buffer = new StringBuilder();
		buffer.append("(");
		//自身のアクティビティ
		buffer.append("(");
		buffer.append("communityUserId_s:" + SolrUtil.escape(communityUserId));
		buffer.append(" AND (actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_REVIEW.getCode()));
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_QUESTION.getCode()));
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_ANSWER.getCode()));
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_IMAGE.getCode()));
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_FOLLOW_USER.getCode()));
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_FOLLOW_PRODUCT.getCode()));
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_FOLLOW_QUESTION.getCode()));
		buffer.append("))");
		//フォローユーザー
		SearchResult<CommunityUserFollowDO> followUsers
				= new SearchResult<CommunityUserFollowDO>(
						solrOperations.findByQuery(new SolrQuery(
								"communityUserId_s:" + SolrUtil.escape(communityUserId)
								).setRows(SolrConstants.QUERY_ROW_LIMIT),
								CommunityUserFollowDO.class,
								Path.includePath("followCommunityUserId")));
		if (followUsers.getDocuments().size() > 0) {
			buffer.append(" OR (");
			buffer.append("(");
			for (int i = 0; i < followUsers.getDocuments().size(); i++) {
				if (i > 0) {
					buffer.append(" OR ");
				}
				buffer.append("communityUserId_s:");
				buffer.append(SolrUtil.escape(followUsers.getDocuments(
						).get(i).getFollowCommunityUser().getCommunityUserId()));
			}
			buffer.append(")");
			buffer.append(" AND (actionHistoryType_s:");
			buffer.append(SolrUtil.escape(ActionHistoryType.USER_REVIEW.getCode()));
			buffer.append(" OR actionHistoryType_s:");
			buffer.append(SolrUtil.escape(ActionHistoryType.USER_QUESTION.getCode()));
			buffer.append(" OR actionHistoryType_s:");
			buffer.append(SolrUtil.escape(ActionHistoryType.USER_ANSWER.getCode()));
			buffer.append(" OR actionHistoryType_s:");
			buffer.append(SolrUtil.escape(ActionHistoryType.USER_IMAGE.getCode()));
			buffer.append(" OR actionHistoryType_s:");
			buffer.append(SolrUtil.escape(ActionHistoryType.USER_FOLLOW_USER.getCode()));
			buffer.append(" OR actionHistoryType_s:");
			buffer.append(SolrUtil.escape(ActionHistoryType.USER_FOLLOW_PRODUCT.getCode()));
			buffer.append(" OR actionHistoryType_s:");
			buffer.append(SolrUtil.escape(ActionHistoryType.USER_FOLLOW_QUESTION.getCode()));
			buffer.append("))");
		}
		//フォロー商品
		SearchResult<ProductFollowDO> followProducts
		= new SearchResult<ProductFollowDO>(
				solrOperations.findByQuery(new SolrQuery(
						"communityUserId_s:" + SolrUtil.escape(communityUserId)
						).setRows(SolrConstants.QUERY_ROW_LIMIT),
						ProductFollowDO.class,
						Path.includePath("followProductId")));
		if (followProducts.getDocuments().size() > 0) {
			buffer.append(" OR (");
			buffer.append("(");
			for (int i = 0; i < followProducts.getDocuments().size(); i++) {
				if (i > 0) {
					buffer.append(" OR ");
				}
				buffer.append("productId_s:");
				buffer.append(SolrUtil.escape(followProducts.getDocuments(
						).get(i).getFollowProduct().getSku()));
			}
			buffer.append(")");
			buffer.append(" AND (actionHistoryType_s:");
			buffer.append(SolrUtil.escape(ActionHistoryType.PRODUCT_REVIEW.getCode()));
			buffer.append(" OR actionHistoryType_s:");
			buffer.append(SolrUtil.escape(ActionHistoryType.PRODUCT_QUESTION.getCode()));
			buffer.append(" OR actionHistoryType_s:");
			buffer.append(SolrUtil.escape(ActionHistoryType.PRODUCT_ANSWER.getCode()));
			buffer.append(" OR actionHistoryType_s:");
			buffer.append(SolrUtil.escape(ActionHistoryType.PRODUCT_IMAGE.getCode()));
			buffer.append("))");
		}
		//フォロー質問
		SearchResult<QuestionFollowDO> followQuestions
		= new SearchResult<QuestionFollowDO>(
				solrOperations.findByQuery(new SolrQuery(
						"communityUserId_s:" + SolrUtil.escape(communityUserId) +
						" AND deleteFlag_b:false"
						).setRows(SolrConstants.QUERY_ROW_LIMIT),
						QuestionFollowDO.class,
						Path.includePath("followQuestionId")));
		if (followQuestions.getDocuments().size() > 0) {
			buffer.append(" OR (");
			buffer.append("(");
			for (int i = 0; i < followQuestions.getDocuments().size(); i++) {
				if (i > 0) {
					buffer.append(" OR ");
				}
				buffer.append("questionId_s:");
				buffer.append(SolrUtil.escape(followQuestions.getDocuments(
						).get(i).getFollowQuestion().getQuestionId()));
			}
			buffer.append(")");
			buffer.append(" AND actionHistoryType_s:");
			buffer.append(SolrUtil.escape(ActionHistoryType.QUESTION_ANSWER.getCode()));
			buffer.append(")");
		}
		//購入商品
		SearchResult<PurchaseProductDO> purchaseProducts
		= new SearchResult<PurchaseProductDO>(
				solrOperations.findByQuery(new SolrQuery(
						"communityUserId_s:" + SolrUtil.escape(communityUserId)
						).setRows(SolrConstants.QUERY_ROW_LIMIT),
						PurchaseProductDO.class,
						Path.includePath("productId")));
		if (purchaseProducts.getDocuments().size() > 0) {
			buffer.append(" OR (");
			buffer.append("(");
			for (int i = 0; i < purchaseProducts.getDocuments().size(); i++) {
				if (i > 0) {
					buffer.append(" OR ");
				}
				buffer.append("productId_s:");
				buffer.append(SolrUtil.escape(purchaseProducts.getDocuments().get(i).getProduct().getSku()));
			}
			buffer.append(")");
			buffer.append(" AND (actionHistoryType_s:");
			buffer.append(SolrUtil.escape(ActionHistoryType.PRODUCT_QUESTION.getCode()));
			buffer.append(" OR actionHistoryType_s:");
			buffer.append(SolrUtil.escape(ActionHistoryType.PRODUCT_ANSWER.getCode()));
			buffer.append("))");
		}
		buffer.append(")");
		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND actionTime_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND actionTime_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}
		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());
		SolrQuery query = new SolrQuery(
				adultHelper.toFilterQuery(buffer.toString()));
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("actionTime_dt", ORDER.desc);
		} else {
			query.setSortField("actionTime_dt", ORDER.asc);
		}
		SearchResult<ActionHistoryDO> result = new SearchResult<ActionHistoryDO>(
				solrOperations.findByQuery(query, ActionHistoryDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId," +
						"product.sku," +
						"question.communityUser.communityUserId," +
						"questionAnswer.questionAnswerId," +
						"review.reviewDecisivePurchases.decisivePurchase.decisivePurchaseId," +
						"imageHeader.imageId," +
						"followCommunityUser.communityUserId").depth(3)));


		List<ActionHistoryDO> actionHistories = new ArrayList<ActionHistoryDO>();
		for(ActionHistoryDO action:result.getDocuments()){

			buffer.append(SolrUtil.escape(ActionHistoryType.PRODUCT_ANSWER.getCode()));
			buffer.append(SolrUtil.escape(ActionHistoryType.PRODUCT_IMAGE.getCode()));
			buffer.append(SolrUtil.escape(ActionHistoryType.USER_ANSWER.getCode()));
			buffer.append(SolrUtil.escape(ActionHistoryType.USER_IMAGE.getCode()));
			
			// コンテンツの一時停止対応
			if(action.getActionHistoryType().equals(ActionHistoryType.USER_REVIEW) ||
					action.getActionHistoryType().equals(ActionHistoryType.PRODUCT_REVIEW)){
				if(action.getReview() == null || action.getReview().getCommunityUser() == null || (
						!action.getReview().getCommunityUser().getCommunityUserId().equals(loginCommunityUserId)
						&& action.getReview().getStatus().equals(ContentsStatus.CONTENTS_STOP)
					)){
						continue;
				}
			}else if(action.getActionHistoryType().equals(ActionHistoryType.USER_QUESTION) ||
					action.getActionHistoryType().equals(ActionHistoryType.PRODUCT_QUESTION)){
				if(action.getQuestion() == null || action.getQuestion().getCommunityUser() == null || (
						!action.getQuestion().getCommunityUser().getCommunityUserId().equals(loginCommunityUserId)
						&& action.getQuestion().getStatus().equals(ContentsStatus.CONTENTS_STOP)
					)){
						continue;
				}
			}else if(action.getActionHistoryType().equals(ActionHistoryType.USER_ANSWER) ||
					action.getActionHistoryType().equals(ActionHistoryType.PRODUCT_ANSWER)){
				if(action.getQuestionAnswer() == null || action.getQuestionAnswer().getCommunityUser() == null || (
						!action.getQuestionAnswer().getCommunityUser().getCommunityUserId().equals(loginCommunityUserId)
						&& action.getQuestionAnswer().getStatus().equals(ContentsStatus.CONTENTS_STOP)
					)){
						continue;
				}
			}			
			actionHistories.add(action);			
		}
		SearchResult<ActionHistoryDO> searchResult = new SearchResult<ActionHistoryDO>(result.getNumFound(), actionHistories);
		ProductUtil.filterInvalidProduct(searchResult);
		// SolrにIndexされていない場合にHbaseからLoadする。
		loadHbaseContents(searchResult);

		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(
							buffer.toString(),
							ActionHistoryDO.class, solrOperations));
		}
		List<ActionHistoryDO> details = new ArrayList<ActionHistoryDO>();

		//重複チェック処理開始
		//ユーザー関連の重複オブジェクト
		Set<String> userCheck = new HashSet<String>();
		//投稿関連の重複オブジェクト
		Set<String> contentsCheck = new HashSet<String>();
		for (ActionHistoryDO actionHistory : searchResult.getDocuments()) {
			if (actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_QUESTION)) {
				userCheck.add(IdUtil.createIdByConcatIds(PostContentType.QUESTION.getCode(),actionHistory.getQuestion().getQuestionId()));
			} else if (actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_ANSWER)) {
				userCheck.add(IdUtil.createIdByConcatIds(PostContentType.ANSWER.getCode(),actionHistory.getQuestionAnswer().getQuestionAnswerId()));
			}else if (actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_REVIEW)) {
				userCheck.add(IdUtil.createIdByConcatIds(PostContentType.REVIEW.getCode(),actionHistory.getReview().getReviewId()));
			}else if (actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_IMAGE)) {
				userCheck.add(IdUtil.createIdByConcatIds(PostContentType.IMAGE_SET.getCode(),actionHistory.getImageSetId()));
			} else if (actionHistory.getActionHistoryType().equals(ActionHistoryType.QUESTION_ANSWER)) {
				contentsCheck.add(IdUtil.createIdByConcatIds(PostContentType.ANSWER.getCode(),actionHistory.getQuestionAnswer().getQuestionAnswerId()));
			}
		}

		// 重複削除処理
		for (ActionHistoryDO actionHistory : searchResult.getDocuments()) {
			if (actionHistory.getActionHistoryType().equals(ActionHistoryType.PRODUCT_QUESTION)) {
				String key = IdUtil.createIdByConcatIds(PostContentType.QUESTION.getCode(),actionHistory.getQuestion().getQuestionId());
				if (!userCheck.contains(key)) details.add(actionHistory);
			} else if (actionHistory.getActionHistoryType().equals(ActionHistoryType.PRODUCT_ANSWER)) {
				String key = IdUtil.createIdByConcatIds(PostContentType.ANSWER.getCode(),actionHistory.getQuestionAnswer().getQuestionAnswerId());
				if (!userCheck.contains(key) && !contentsCheck.contains(key)) details.add(actionHistory);
			} else if (actionHistory.getActionHistoryType().equals(ActionHistoryType.QUESTION_ANSWER)) {
				String key = IdUtil.createIdByConcatIds(PostContentType.ANSWER.getCode(),actionHistory.getQuestionAnswer().getQuestionAnswerId());
				if (!userCheck.contains(key)) details.add(actionHistory);
			} else if (actionHistory.getActionHistoryType().equals(ActionHistoryType.PRODUCT_REVIEW)) {
				String key = IdUtil.createIdByConcatIds(PostContentType.REVIEW.getCode(),actionHistory.getReview().getReviewId());
				if (!userCheck.contains(key)) details.add(actionHistory);
			} else if (actionHistory.getActionHistoryType().equals(ActionHistoryType.PRODUCT_IMAGE)) {
				String key = IdUtil.createIdByConcatIds(PostContentType.IMAGE_SET.getCode(),actionHistory.getImageSetId());
				if (!userCheck.contains(key)) details.add(actionHistory);
			} else {
				details.add(actionHistory);
			}
		}
		searchResult.setDocuments(details);
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}

	/**
	 * 指定したコミュニティユーザーの投稿系のアクティビティを投稿日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return アクティビティ一覧
	 */
	@Override
	public SearchResult<ActionHistoryDO> findPrimaryActivityByCommunityUserId(
			String communityUserId,
			int limit,
			Date offsetTime,
			boolean previous) {
		StringBuilder buffer = new StringBuilder();

		//自身のアクティビティ
		buffer.append("communityUserId_s:" + communityUserId);
		buffer.append(" AND (actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_REVIEW.getCode()));
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_QUESTION.getCode()));
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_ANSWER.getCode()));
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_IMAGE.getCode()));
		buffer.append(")");
		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND actionTime_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND actionTime_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}
		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());
		SolrQuery query = new SolrQuery(
				adultHelper.toFilterQuery(buffer.toString()));
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("actionTime_dt", ORDER.desc);
		} else {
			query.setSortField("actionTime_dt", ORDER.asc);
		}
		
		List<ActionHistoryDO> actionHistories = new ArrayList<ActionHistoryDO>();

		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();

		SearchResult<ActionHistoryDO> results = new SearchResult<ActionHistoryDO>(
				solrOperations.findByQuery(query, ActionHistoryDO.class,
						Path.includeProp("*").includePath(
								"communityUser.communityUserId," +
								"product.sku," +
								"review.reviewId," +
								"question.questionId," +
								"questionAnswer.questionAnswerId," +
								"imageHeader.imageId" + 
								"question.communityUser.communityUserId," +
								"review.communityUser.communityUser,Id," +
								"review.reviewDecisivePurchases.decisivePurchase.decisivePurchaseId").depth(3)));
		
		for(ActionHistoryDO action:results.getDocuments()){

			// コンテンツの一時停止対応
			if(action.getActionHistoryType().equals(ActionHistoryType.USER_REVIEW)){
				if(action.getReview() == null || (
						action.getReview().getCommunityUser() == null || !action.getReview().getCommunityUser().getCommunityUserId().equals(loginCommunityUserId)
						&& action.getReview().getStatus().equals(ContentsStatus.CONTENTS_STOP)
					)){
						continue;
				}
			}else if(action.getActionHistoryType().equals(ActionHistoryType.USER_QUESTION)){
				if(action.getQuestion() == null || (
						action.getQuestion().getCommunityUser() == null || !action.getQuestion().getCommunityUser().getCommunityUserId().equals(loginCommunityUserId)
						&& action.getQuestion().getStatus().equals(ContentsStatus.CONTENTS_STOP)
					)){
						continue;
				}
			}else if(action.getActionHistoryType().equals(ActionHistoryType.USER_ANSWER)){
				if(action.getQuestionAnswer() == null || (
						action.getQuestionAnswer().getCommunityUser() == null || !action.getQuestionAnswer().getCommunityUser().getCommunityUserId().equals(loginCommunityUserId)
						&& action.getQuestionAnswer().getStatus().equals(ContentsStatus.CONTENTS_STOP)
					)){
						continue;
				}
			}			
			actionHistories.add(action);			
		}
		SearchResult<ActionHistoryDO> searchResult = new SearchResult<ActionHistoryDO>(results.getNumFound(), actionHistories);
		ProductUtil.filterInvalidProduct(searchResult);
		// SolrにIndexされていない場合にHbaseからLoadする。
		loadHbaseContents(searchResult);
		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(buffer.toString(),
							ActionHistoryDO.class, solrOperations));
		}
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}

	/**
	 * 指定したコミュニティユーザーのその他のアクティビティを投稿日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return アクティビティ一覧
	 */
	@Override
	public SearchResult<ActionHistoryDO> findSecondaryActivityByCommunityUserId(
			String communityUserId,
			int limit,
			Date offsetTime,
			boolean previous) {
		StringBuilder buffer = new StringBuilder();

		//自身のアクティビティ
		buffer.append("communityUserId_s:" + communityUserId);
		buffer.append(" AND (actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_REVIEW_COMMENT.getCode()));
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_ANSWER_COMMENT.getCode()));
		// TODO あとで修正する。
//		buffer.append(" OR actionHistoryType_s:");
//		buffer.append(SolrUtil.escape(ActionHistoryType.USER_IMAGE_COMMENT.getCode()));
//		buffer.append(" OR actionHistoryType_s:");
		buffer.append(" OR ((actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_IMAGE_COMMENT.getCode()));
		buffer.append(" AND imageSetId_s:[* TO *])");
		buffer.append(" OR (actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_IMAGE_COMMENT.getCode()));
		buffer.append(" AND reviewId_s:[* TO *])");
		buffer.append(" OR (actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_IMAGE_COMMENT.getCode()));
		buffer.append(" AND questionId_s:[* TO *])");
		buffer.append(" OR (actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_IMAGE_COMMENT.getCode()));
		buffer.append(" AND questionAnswerId_s:[* TO *]))");
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_FOLLOW_USER.getCode()));
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_FOLLOW_PRODUCT.getCode()));
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_FOLLOW_QUESTION.getCode()));
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_PRODUCT_MASTER_RANK_CHANGE.getCode()));
		buffer.append(")");
		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND actionTime_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND actionTime_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}
		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());
		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(buffer.toString()));
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("actionTime_dt", ORDER.desc);
		} else {
			query.setSortField("actionTime_dt", ORDER.asc);
		}
		SearchResult<ActionHistoryDO> searchResult = new SearchResult<ActionHistoryDO>(
				solrOperations.findByQuery(query, ActionHistoryDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId," +
						"product.sku," +
						"question.questionId," +
						"question.communityUser.communityUserId," +
						"questionAnswer.questionAnswerId," +
						"questionAnswer.communityUser.communityUserId," +
						"followCommunityUser.communityUserId," +
						"review.reviewId," +
						"review.communityUser.communityUserId," +
						"imageHeader.imageId," +
						"imageHeader.ownerCommunityUser.communityUserId," +
						"productMaster.productMasterId").depth(3)));

		ProductUtil.filterInvalidProduct(searchResult);
		// SolrにIndexされていない場合にHbaseからLoadする。
		loadHbaseContents(searchResult);

		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(
							buffer.toString(),
							ActionHistoryDO.class, solrOperations));
		}
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}
	
	
	


	@Override
	public SearchResult<ActionHistoryDO> findTimelineActivityByCommunityUserId(
			String communityUserId, int limit, Date offsetTime, boolean previous) {
		StringBuilder buffer = new StringBuilder();

		//自身のアクティビティ
		buffer.append("communityUserId_s:" + communityUserId);
		buffer.append(" AND (actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_REVIEW.getCode()));
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_QUESTION.getCode()));
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_ANSWER.getCode()));
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_IMAGE.getCode()));
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_REVIEW_COMMENT.getCode()));
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_ANSWER_COMMENT.getCode()));
		// TODO あとで修正する。
//		buffer.append(" OR actionHistoryType_s:");
//		buffer.append(SolrUtil.escape(ActionHistoryType.USER_IMAGE_COMMENT.getCode()));
//		buffer.append(" OR actionHistoryType_s:");
		buffer.append(" OR ((actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_IMAGE_COMMENT.getCode()));
		buffer.append(" AND imageSetId_s:[* TO *])");
		buffer.append(" OR (actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_IMAGE_COMMENT.getCode()));
		buffer.append(" AND reviewId_s:[* TO *])");
		buffer.append(" OR (actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_IMAGE_COMMENT.getCode()));
		buffer.append(" AND questionId_s:[* TO *])");
		buffer.append(" OR (actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_IMAGE_COMMENT.getCode()));
		buffer.append(" AND questionAnswerId_s:[* TO *]))");
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_FOLLOW_USER.getCode()));
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_FOLLOW_PRODUCT.getCode()));
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_FOLLOW_QUESTION.getCode()));
		buffer.append(" OR actionHistoryType_s:");
		buffer.append(SolrUtil.escape(ActionHistoryType.USER_PRODUCT_MASTER_RANK_CHANGE.getCode()));
		buffer.append(")");
		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND actionTime_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND actionTime_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}
		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());
		SolrQuery query = new SolrQuery(
				adultHelper.toFilterQuery(buffer.toString()));
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("actionTime_dt", ORDER.desc);
		} else {
			query.setSortField("actionTime_dt", ORDER.asc);
		}
		
		List<ActionHistoryDO> actionHistories = new ArrayList<ActionHistoryDO>();

		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();

		SearchResult<ActionHistoryDO> results = new SearchResult<ActionHistoryDO>(
				solrOperations.findByQuery(query, ActionHistoryDO.class,
						Path.includeProp("*").includePath(
								"communityUser.communityUserId," +
								"product.sku," +
								"question.questionId," +
								"question.communityUser.communityUserId," +
								"questionAnswer.questionAnswerId," +
								"questionAnswer.communityUser.communityUserId," +
								"review.reviewId," +
								"review.communityUser.communityUserId," +
								"review.reviewDecisivePurchases.decisivePurchase.decisivePurchaseId," +
								"followCommunityUser.communityUserId," +
								"imageHeader.imageId" + 
								"imageHeader.ownerCommunityUser.communityUserId," +
								"productMaster.productMasterId").depth(3)));
		
		for(ActionHistoryDO action:results.getDocuments()){
			// コンテンツの一時停止対応
			if(action.getActionHistoryType().equals(ActionHistoryType.USER_REVIEW)){
				if(action.getReview() == null || (
						action.getReview().getCommunityUser() == null || !action.getReview().getCommunityUser().getCommunityUserId().equals(loginCommunityUserId)
						&& action.getReview().getStatus().equals(ContentsStatus.CONTENTS_STOP)
					)){
						continue;
				}
			}else if(action.getActionHistoryType().equals(ActionHistoryType.USER_QUESTION)){
				if(action.getQuestion() == null || (
						action.getQuestion().getCommunityUser() == null || !action.getQuestion().getCommunityUser().getCommunityUserId().equals(loginCommunityUserId)
						&& action.getQuestion().getStatus().equals(ContentsStatus.CONTENTS_STOP)
					)){
						continue;
				}
			}else if(action.getActionHistoryType().equals(ActionHistoryType.USER_ANSWER)){
				if(action.getQuestionAnswer() == null || (
						action.getQuestionAnswer().getCommunityUser() == null || !action.getQuestionAnswer().getCommunityUser().getCommunityUserId().equals(loginCommunityUserId)
						&& action.getQuestionAnswer().getStatus().equals(ContentsStatus.CONTENTS_STOP)
					)){
						continue;
				}
			}			
			actionHistories.add(action);			
		}
		SearchResult<ActionHistoryDO> searchResult = new SearchResult<ActionHistoryDO>(results.getNumFound(), actionHistories);
		ProductUtil.filterInvalidProduct(searchResult);
		// SolrにIndexされていない場合にHbaseからLoadする。
		loadHbaseContents(searchResult);
		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(buffer.toString(),
							ActionHistoryDO.class, solrOperations));
		}
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}

	private void loadHbaseContents(SearchResult<ActionHistoryDO> searchResult){
		if(searchResult == null || searchResult.getDocuments() == null || searchResult.getDocuments().isEmpty()) return;
		Set<String> reviewIds = new HashSet<String>();
		Set<String> questionIds = new HashSet<String>();
		Set<String> questionAnswerIds = new HashSet<String>();
		Set<String> imageHeaderIds = new HashSet<String>();
		Set<String> productMasterIds = new HashSet<String>();

		for(ActionHistoryDO actionHistory: searchResult.getDocuments()){
			if(actionHistory.getReview() != null && StringUtils.isNotEmpty(actionHistory.getReview().getReviewId()) && actionHistory.getReview().getRegisterDateTime() == null){
				reviewIds.add(actionHistory.getReview().getReviewId());
			}
			if(actionHistory.getQuestion() != null && StringUtils.isNotEmpty(actionHistory.getQuestion().getQuestionId()) && actionHistory.getQuestion().getRegisterDateTime() == null){
				questionIds.add(actionHistory.getQuestion().getQuestionId());
			}
			if(actionHistory.getQuestionAnswer() != null && StringUtils.isNotEmpty(actionHistory.getQuestionAnswer().getQuestionAnswerId()) && actionHistory.getQuestionAnswer().getRegisterDateTime() == null){
				questionAnswerIds.add(actionHistory.getQuestionAnswer().getQuestionAnswerId());
			}
			if(actionHistory.getImageHeader() != null && StringUtils.isNotEmpty(actionHistory.getImageHeader().getImageId()) && actionHistory.getImageHeader().getRegisterDateTime() == null){
				imageHeaderIds.add(actionHistory.getImageHeader().getImageId());
			}
			if(actionHistory.getProductMaster() != null && StringUtils.isNotEmpty(actionHistory.getProductMaster().getProductMasterId()) && actionHistory.getProductMaster().getRegisterDateTime() == null){
				productMasterIds.add(actionHistory.getProductMaster().getProductMasterId());
			}
		}

		Map<String, ReviewDO> reviewsMap = null;
		Map<String, QuestionDO> questionsMap = null;
		Map<String, QuestionAnswerDO> questionAnswersMap = null;
		Map<String, ImageHeaderDO> imageHeadersMap = null;
		Map<String, ProductMasterDO> productMatersMap = null;

		if(!reviewIds.isEmpty())
			reviewsMap = hBaseOperations.find(ReviewDO.class, String.class, reviewIds);
		if(!questionIds.isEmpty())
			questionsMap = hBaseOperations.find(QuestionDO.class, String.class, questionIds);
		if(!questionAnswerIds.isEmpty())
			questionAnswersMap = hBaseOperations.find(QuestionAnswerDO.class, String.class, questionAnswerIds);
		if(!imageHeaderIds.isEmpty())
			imageHeadersMap = hBaseOperations.find(ImageHeaderDO.class, String.class, imageHeaderIds);
		if(!productMasterIds.isEmpty())
			productMatersMap = hBaseOperations.find(ProductMasterDO.class, String.class, productMasterIds);

		for(ActionHistoryDO actionHistory: searchResult.getDocuments()){
			if(actionHistory.getReview() != null && StringUtils.isNotEmpty(actionHistory.getReview().getReviewId()) && actionHistory.getReview().getRegisterDateTime() == null){
				actionHistory.setReview(reviewsMap.get(actionHistory.getReview().getReviewId()));
			}
			if(actionHistory.getQuestion() != null && StringUtils.isNotEmpty(actionHistory.getQuestion().getQuestionId()) && actionHistory.getQuestion().getRegisterDateTime() == null){
				actionHistory.setQuestion(questionsMap.get(actionHistory.getQuestion().getQuestionId()));
			}
			if(actionHistory.getQuestionAnswer() != null && StringUtils.isNotEmpty(actionHistory.getQuestionAnswer().getQuestionAnswerId()) && actionHistory.getQuestionAnswer().getRegisterDateTime() == null){
				actionHistory.setQuestionAnswer(questionAnswersMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId()));
			}
			if(actionHistory.getImageHeader() != null && StringUtils.isNotEmpty(actionHistory.getImageHeader().getImageId()) && actionHistory.getImageHeader().getRegisterDateTime() == null){
				actionHistory.setImageHeader(imageHeadersMap.get(actionHistory.getImageHeader().getImageId()));
			}
			if(actionHistory.getProductMaster() != null && StringUtils.isNotEmpty(actionHistory.getProductMaster().getProductMasterId()) && actionHistory.getProductMaster().getRegisterDateTime() == null){
				actionHistory.setProductMaster(productMatersMap.get(actionHistory.getProductMaster().getProductMasterId()));
			}
		}
	}



}
