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

import org.apache.commons.lang.StringUtils;
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
import com.kickmogu.lib.hadoop.hbase.UpdateColumns;
import com.kickmogu.lib.solr.FacetResult;
import com.kickmogu.lib.solr.SolrConstants;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.common.exception.YcComException;
import com.kickmogu.yodobashi.community.resource.dao.CommentDao;
import com.kickmogu.yodobashi.community.resource.dao.ImageDao;
import com.kickmogu.yodobashi.community.resource.dao.InformationDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionAnswerDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.ReviewDao;
import com.kickmogu.yodobashi.community.resource.dao.util.AdultHelper;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.ProductUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.SolrUtil;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.SpamReportDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommentTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportStatus;


/**
 * コメント DAO です。
 * @author kamiike
 *
 */
@Service
public class CommentDaoImpl implements CommentDao {

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

	@Autowired @Qualifier("default")
	protected IDGenerator<String> idGenerator;

	@Autowired 
	protected ReviewDao reviewDao;

	@Autowired 
	protected QuestionAnswerDao questionAnswerDao;
	
	@Autowired 
	protected QuestionDao questionDao;
	
	@Autowired 
	protected ImageDao imageDao;
	
	@Autowired 
	protected InformationDao informationDao;
	

	/**
	 * 商品 DAO です。
	 */
	@Autowired @Qualifier("catalog")
	protected ProductDao productDao;

	/**
	 * レビューのコメント情報マップを返します。
	 * @param communityUserId 対象となるコミュニティユーザーID
	 * @param reviewIds レビューIDリスト
	 * @return レビューのコメント情報マップ
	 */
	public Map<String, Boolean> loadReviewCommentMap(
			String communityUserId, List<String> reviewIds){
		Map<String, Boolean> resultMap = new HashMap<String, Boolean>();
		if (StringUtils.isEmpty(communityUserId) || reviewIds == null || reviewIds.size() == 0) {
			return resultMap;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND deleteFlag_b:false AND ");
		buffer.append("targetType_s:");
		buffer.append(SolrUtil.escape(CommentTargetType.REVIEW.getCode()));
		buffer.append(" AND communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND (");
		for (int i = 0; i < reviewIds.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("reviewId_s:");
			buffer.append(SolrUtil.escape(reviewIds.get(i)));
		}
		buffer.append(")");

		for (CommentDO comment : solrOperations.findByQuery(
				new SolrQuery(buffer.toString()).setRows(
						SolrConstants.QUERY_ROW_LIMIT), CommentDO.class,
						Path.includeProp("reviewId")).getDocuments()) {
			resultMap.put(comment.getReview().getReviewId(), true);
		}
		return resultMap;
	}

	/**
	 * 質問回答のコメント情報マップを返します。
	 * @param communityUserId 対象となるコミュニティユーザーID
	 * @param questionAnswerIds 質問回答IDリスト
	 * @return 質問回答のコメント情報マップ
	 */
	public Map<String, Boolean> loadQuestionAnswerCommentMap(
			String communityUserId, List<String> questionAnswerIds){
		Map<String, Boolean> resultMap = new HashMap<String, Boolean>();
		if (StringUtils.isEmpty(communityUserId) || questionAnswerIds == null || questionAnswerIds.size() == 0) {
			return resultMap;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND deleteFlag_b:false AND ");
		buffer.append("targetType_s:");
		buffer.append(SolrUtil.escape(CommentTargetType.QUESTION_ANSWER.getCode()));
		buffer.append(" AND communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND (");
		for (int i = 0; i < questionAnswerIds.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append(SolrUtil.escape("questionAnswerId_s:"));
			buffer.append(questionAnswerIds.get(i));
		}
		buffer.append(")");

		for (CommentDO comment : solrOperations.findByQuery(
				new SolrQuery(buffer.toString()).setRows(
						SolrConstants.QUERY_ROW_LIMIT), CommentDO.class,
						Path.includeProp("questionAnswerId")).getDocuments()) {
			resultMap.put(comment.getQuestionAnswer().getQuestionAnswerId(), true);
		}
		return resultMap;
	}

	/**
	 * 画像のコメント情報マップを返します。
	 * @param communityUserId 対象となるコミュニティユーザーID
	 * @param imageIds 画像IDリスト
	 * @return 画像のコメント情報マップ
	 */
	public Map<String, Boolean> loadImageCommentMap(
			String communityUserId, List<String> imageIds){
		Map<String, Boolean> resultMap = new HashMap<String, Boolean>();
		if (StringUtils.isEmpty(communityUserId) || imageIds == null || imageIds.size() == 0) {
			return resultMap;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND deleteFlag_b:false AND ");
		buffer.append("targetType_s:");
		buffer.append(SolrUtil.escape(CommentTargetType.IMAGE.getCode()));
		buffer.append(" AND communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND (");
		for (int i = 0; i < imageIds.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("imageHeaderId_s:");
			buffer.append(SolrUtil.escape(imageIds.get(i)));
		}
		buffer.append(")");

		for (CommentDO comment : solrOperations.findByQuery(
				new SolrQuery(buffer.toString()).setRows(
						SolrConstants.QUERY_ROW_LIMIT), CommentDO.class,
						Path.includeProp("imageHeaderId")).getDocuments()) {
			resultMap.put(comment.getImageHeader().getImageId(), true);
		}
		return resultMap;
	}

	/**
	 * レビューに対するコメント数情報を返します。
	 * @param reviewIds レビューIDリスト
	 * @return レビューに対するコメント数情報
	 */
	@Override
	public Map<String, Long> loadReviewCommentCountMap(List<String> reviewIds) {
		return loadContentsCommentCountMap(
				reviewIds, CommentTargetType.REVIEW,
				"reviewId_s", null);
	}

	/**
	 * レビューに対するコメント数情報を返します。
	 * @param reviewIds レビューIDリスト
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @return レビューに対するコメント数情報
	 */
	@Override
	public Map<String, Long> loadReviewCommentCountMap(List<String> reviewIds,
			String excludeCommunityUserId) {
		return loadContentsCommentCountMap(
				reviewIds, CommentTargetType.REVIEW,
				"reviewId_s", excludeCommunityUserId);
	}

	/**
	 * 質問に紐付く回答に対するコメント数情報を返します。
	 * @param questionIds 質問IDリスト
	 * @return 質問に紐付く回答に対するコメント数情報
	 */
	@Override
	public Map<String, Long> loadQuestionCommentCountMap(
			List<String> questionIds) {
		return loadContentsCommentCountMap(
				questionIds, CommentTargetType.QUESTION_ANSWER,
				"questionId_s", null);
	}

	/**
	 * 質問回答に対するコメント数情報を返します。
	 * @param questionAnswerIds 質問回答IDリスト
	 * @return 質問回答に対するコメント数情報
	 */
	@Override
	public Map<String, Long> loadQuestionAnswerCommentCountMap(List<String> questionAnswerIds) {
		return loadContentsCommentCountMap(
				questionAnswerIds, CommentTargetType.QUESTION_ANSWER,
				"questionAnswerId_s", null);
	}

	/**
	 * 質問回答に対するコメント数情報を返します。
	 * @param questionAnswerIds 質問回答IDリスト
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @return 質問回答に対するコメント数情報
	 */
	@Override
	public Map<String, Long> loadQuestionAnswerCommentCountMap(List<String> questionAnswerIds,
			String excludeCommunityUserId) {
		return loadContentsCommentCountMap(
				questionAnswerIds, CommentTargetType.QUESTION_ANSWER,
				"questionAnswerId_s", excludeCommunityUserId);
	}

	/**
	 * 画像に対するコメント数情報を返します。
	 * @param imageIds 画像IDリスト
	 * @return 画像に対するコメント数情報
	 */
	@Override
	public Map<String, Long> loadImageCommentCountMap(List<String> imageIds) {
		return loadContentsCommentCountMap(
				imageIds, CommentTargetType.IMAGE, "imageHeaderId_s",
				null);
	}

	/**
	 * 画像に対するコメント数情報を返します。
	 * @param imageIds 画像IDリスト
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @return 画像に対するコメント数情報
	 */
	@Override
	public Map<String, Long> loadImageCommentCountMap(List<String> imageIds,
			String excludeCommunityUserId) {
		return loadContentsCommentCountMap(
				imageIds, CommentTargetType.IMAGE, "imageHeaderId_s",
				excludeCommunityUserId);
	}

	/**
	 * 画像セットに対するコメント数情報を返します。
	 * @param imageSetIds 画像セットIDリスト
	 * @return 画像セットに対するコメント数情報
	 */
	@Override
	public Map<String, Long> loadImageSetCommentCountMap(List<String> imageSetIds) {
		return loadContentsCommentCountMap(
				imageSetIds, CommentTargetType.IMAGE, "imageSetId_s", null);
	}


	private String createCountMapFacetQuery(CommentTargetType targetType, String contentField, String contentId){
		StringBuilder query = new StringBuilder();
		query.append("withdraw_b:false AND deleteFlag_b:false AND ");
		query.append("targetType_s:");
		query.append(SolrUtil.escape(targetType.getCode()));
		query.append(" AND ");
		query.append(contentField + ":");
		query.append(SolrUtil.escape(contentId));
		return query.toString();
	}


	@Override
	public void loadContentsCommentCountMap(List<String> reviewIds, List<String> questionIds, List<String> questionAnswerIds, List<String> imageSetIds,
			Map<String, Long> reviewCountMap, Map<String, Long> questionCountMap, Map<String, Long> questionAnswerCountMap, Map<String, Long> imageSetCountMap) {

		Map<String, String> reviewQueryMap = new HashMap<String,String>();
		Map<String, String> questionQueryMap = new HashMap<String,String>();
		Map<String, String> questionAnswerQueryMap = new HashMap<String,String>();
		Map<String, String> imageSetQueryMap = new HashMap<String,String>();

		SolrQuery solrQuery = new SolrQuery("withdraw_b:false AND deleteFlag_b:false");

		if(reviewIds != null && !reviewIds.isEmpty()){
			for(String reviewId:reviewIds){
				String query = createCountMapFacetQuery(CommentTargetType.REVIEW, "reviewId_s", reviewId);
				reviewQueryMap.put(query, reviewId);
				solrQuery.addFacetQuery(query);
			}
		}
		if(questionIds != null && !questionIds.isEmpty()){
			for(String questionId:questionIds){
				String query = createCountMapFacetQuery(CommentTargetType.QUESTION_ANSWER, "questionId_s", questionId);
				questionQueryMap.put(query, questionId);
				solrQuery.addFacetQuery(query);
			}
		}
		if(questionAnswerIds != null && !questionAnswerIds.isEmpty()){
			for(String questionAnswerId:questionAnswerIds){
				String query = createCountMapFacetQuery(CommentTargetType.QUESTION_ANSWER, "questionAnswerId_s", questionAnswerId);
				questionAnswerQueryMap.put(query, questionAnswerId);
				solrQuery.addFacetQuery(query);
			}
		}
		if(imageSetIds != null && !imageSetIds.isEmpty()){
			for(String imageSetId:imageSetIds){
				String query = createCountMapFacetQuery(CommentTargetType.IMAGE, "imageSetId_s", imageSetId);
				imageSetQueryMap.put(query, imageSetId);
				solrQuery.addFacetQuery(query);
			}
		}

		if(solrQuery.getFacetQuery() != null && solrQuery.getFacetQuery().length > 0){
			solrQuery.setFacetLimit(solrQuery.getFacetQuery().length);
			solrQuery.setFacetMinCount(0);
			for (FacetResult<String> facetResult : solrOperations.facet(CommentDO.class, String.class, solrQuery)) {
				if(reviewQueryMap.containsKey(facetResult.getFacetQuery())){
					reviewCountMap.put(reviewQueryMap.get(facetResult.getFacetQuery()), facetResult.getCount());
				}
				if(questionQueryMap.containsKey(facetResult.getFacetQuery())){
					questionCountMap.put(questionQueryMap.get(facetResult.getFacetQuery()), facetResult.getCount());
				}
				if(questionAnswerQueryMap.containsKey(facetResult.getFacetQuery())){
					questionAnswerCountMap.put(questionAnswerQueryMap.get(facetResult.getFacetQuery()), facetResult.getCount());
				}
				if(imageSetQueryMap.containsKey(facetResult.getFacetQuery())){
					imageSetCountMap.put(imageSetQueryMap.get(facetResult.getFacetQuery()), facetResult.getCount());
				}
			}
		}
	}

	/**
	 * コンテンツに対するコメント数情報を返します。
	 * @param contentsIds コンテンツIDリスト
	 * @param targetType 対象タイプ
	 * @param facetField ファセットフィールド
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @return コンテンツに対するコメント数情報
	 */
	private Map<String, Long> loadContentsCommentCountMap(
			List<String> contentsIds,
			CommentTargetType targetType,
			String facetField,
			String excludeCommunityUserId) {
		Map<String, Long> resultMap = new HashMap<String, Long>();
		if (contentsIds == null || contentsIds.size() == 0) {
			return resultMap;
		}
		StringBuilder query = new StringBuilder();
		query.append("withdraw_b:false AND deleteFlag_b:false AND ");
		query.append("targetType_s:");
		query.append(SolrUtil.escape(targetType.getCode()));
		query.append(" AND (");
		for (int i = 0; i < contentsIds.size(); i++) {
			if (i > 0) {
				query.append(" OR ");
			}
			query.append(facetField);
			query.append(":");
			query.append(SolrUtil.escape(contentsIds.get(i)));
		}
		query.append(")");
		if (excludeCommunityUserId != null) {
			query.append(" AND !communityUserId_s:");
			query.append(SolrUtil.escape(excludeCommunityUserId));
		}
		for (FacetResult<String> facetResult : solrOperations.facet(
				CommentDO.class, String.class, new SolrQuery(
						query.toString()).setFacetLimit(
								SolrConstants.QUERY_ROW_LIMIT).addFacetField(facetField))) {
			resultMap.put(facetResult.getValue(), facetResult.getCount());
		}
		return resultMap;
	}

	/**
	 * コメント情報を削除します。
	 * @param commentId コメントID
	 */
	@Override
	public void deleteComment(String commentId, boolean mngToolOperation) {
		CommentDO comment = new CommentDO();
		comment.setCommentId(commentId);
		comment.setMngToolOperation(mngToolOperation);
		comment.setModifyDateTime(timestampHolder.getTimestamp());
		comment.setDeleteFlag(true);
		comment.setDeleteDate(timestampHolder.getTimestamp());

		hBaseOperations.scanUpdateWithIndex(
				ActionHistoryDO.class, "commentId", comment.getCommentId(),
				UpdateColumns.set("deleteFlag", true
						).andSet("deleteDate", timestampHolder.getTimestamp())
						.andSet("modifyDateTime", timestampHolder.getTimestamp()));
		hBaseOperations.scanUpdateWithIndex(
				InformationDO.class, "commentId", comment.getCommentId(),
				UpdateColumns.set("deleteFlag", true
						).andSet("deleteDate", timestampHolder.getTimestamp())
						.andSet("modifyDateTime", timestampHolder.getTimestamp()));
		//SpamReportDO
		hBaseOperations.scanUpdateWithIndex(
				SpamReportDO.class, "commentId", comment.getCommentId(),
				UpdateColumns.set("status", SpamReportStatus.DELETE
						).andSet("deleteDate", timestampHolder.getTimestamp())
						.andSet("modifyDateTime", timestampHolder.getTimestamp()));

		hBaseOperations.save(comment, Path.includeProp(
				"deleteFlag,deleteDate,modifyDateTime,mngToolOperation"));
	}

	/**
	 * コメント情報を保存します。
	 * @param comment コメント
	 */
	@Override
	public void saveComment(CommentDO comment) {
		Condition updateCondition = null;
		if (StringUtils.isEmpty(comment.getCommentId())) {
			comment.setRegisterDateTime(timestampHolder.getTimestamp());
			comment.setPostDate(timestampHolder.getTimestamp());
			String commentId = createCommentId(comment);
			if(StringUtils.isEmpty(commentId))
				throw new YcComException("can not generate commentId");

			comment.setCommentId(commentId);
			updateCondition = Path.DEFAULT;
		} else {
			updateCondition = Path.includeProp("*").excludeProp("registerDateTime,postDate");
		}
		comment.setModifyDateTime(timestampHolder.getTimestamp());
		comment.setSaveDate(timestampHolder.getTimestamp());
		if (comment.getTargetType().equals(CommentTargetType.QUESTION_ANSWER)) {
			comment.setQuestionAnswer(hBaseOperations.load(QuestionAnswerDO.class,
					comment.getQuestionAnswer().getQuestionAnswerId(),
					Path.includeProp(
							"*").includePath("question.questionId").depth(1)));
			comment.setQuestionId(comment.getQuestionAnswer().getQuestion(
							).getQuestionId());
			comment.setRelationQuestionAnswerOwnerId(comment.getQuestionAnswer(
					).getCommunityUser().getCommunityUserId());
			comment.setRelationQuestionOwnerId(comment.getQuestionAnswer(
					).getQuestion().getCommunityUser().getCommunityUserId());
			comment.setAdult(comment.getQuestionAnswer().isAdult());
		} else if (comment.getTargetType().equals(CommentTargetType.REVIEW)) {
			comment.setReview(hBaseOperations.load(
					ReviewDO.class, comment.getReview().getReviewId()));
			comment.setRelationReviewOwnerId(
					comment.getReview().getCommunityUser().getCommunityUserId());
			comment.setAdult(comment.getReview().isAdult());
		} else if (comment.getTargetType().equals(CommentTargetType.IMAGE)) {
			comment.setImageHeader(hBaseOperations.load(
					ImageHeaderDO.class, comment.getImageHeader().getImageId()));
			comment.setRelationImageOwnerId(
					comment.getImageHeader().getOwnerCommunityUserId());
			comment.setImageSetId(comment.getImageHeader().getImageSetId());
			comment.setAdult(comment.getImageHeader().isAdult());
		}

		hBaseOperations.save(comment, updateCondition);
	}

	/**
	 * コメント情報のインデックスを更新します。
	 * @param commentId コメントID
	 */
	@Override
	public void updateCommentInIndex(String commentId) {
		CommentDO comment = hBaseOperations.load(CommentDO.class, commentId);
		if (comment == null || comment.isDeleted()) {
			solrOperations.deleteByQuery(new SolrQuery(
					"commentId_s:" + commentId), ActionHistoryDO.class);
			solrOperations.deleteByQuery(new SolrQuery(
					"commentId_s:" + commentId), InformationDO.class);
			if (comment == null) {
				solrOperations.deleteByKey(CommentDO.class, commentId);
				solrOperations.deleteByKey(SpamReportDO.class, commentId);
			} else {
				solrOperations.save(SpamReportDO.class,
						hBaseOperations.scanWithIndex(
								SpamReportDO.class, "commentId", commentId));
				solrOperations.save(comment);
			}
		} else {
			solrOperations.save(comment);
		}
	}

	/**
	 * 指定したコンテンツに対するコメントを返します。
	 * @param type タイプ
	 * @param contentsId コンテンツID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return コメントリスト
	 */
	@Override
	public SearchResult<CommentDO> findCommentByContentsId(
			CommentTargetType type,
			String contentsId,
			List<String> excludeCommentIds,
			int limit,
			Date offsetTime,
			boolean previous) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND deleteFlag_b:false AND ");
		buffer.append("targetType_s:");
		buffer.append(SolrUtil.escape(type.getCode()));

		if (null != excludeCommentIds) {
			for (String excludeCommentId : excludeCommentIds) {
				if(!StringUtils.isEmpty(excludeCommentId))
					buffer.append(" AND !commentId:" + SolrUtil.escape(excludeCommentId));
			}
		}

		if (type.equals(CommentTargetType.REVIEW)) {
			buffer.append(" AND reviewId_s:");
		} else if (type.equals(CommentTargetType.QUESTION_ANSWER)) {
			buffer.append(" AND questionAnswerId_s:");
		} else if (type.equals(CommentTargetType.IMAGE)) {
			buffer.append(" AND imageHeaderId_s:");
		}
		buffer.append(SolrUtil.escape(contentsId));
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
		SearchResult<CommentDO> searchResult = new SearchResult<CommentDO>(
				solrOperations.findByQuery(
						query,
						CommentDO.class,
						Path.includeProp("*").includePath("communityUser.communityUserId").depth(1)));
		
		ProductUtil.filterInvalidProduct(searchResult);
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}

	@Override
	public long moreCountByContentsId(
			CommentTargetType type,
			String contentsId,
			List<String> excludeCommentIds,
			Date offsetTime,
			boolean previous) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND deleteFlag_b:false AND ");
		buffer.append("targetType_s:");
		buffer.append(SolrUtil.escape(type.getCode()));

		if (null != excludeCommentIds) {
			for (String excludeCommentId : excludeCommentIds) {
				if(!StringUtils.isEmpty(excludeCommentId))
					buffer.append(" AND !commentId:" + SolrUtil.escape(excludeCommentId));
			}
		}

		if (type.equals(CommentTargetType.REVIEW)) {
			buffer.append(" AND reviewId_s:");
		} else if (type.equals(CommentTargetType.QUESTION_ANSWER)) {
			buffer.append(" AND questionAnswerId_s:");
		} else if (type.equals(CommentTargetType.IMAGE)) {
			buffer.append(" AND imageHeaderId_s:");
		}
		buffer.append(SolrUtil.escape(contentsId));
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
		if (offsetTime == null || !previous) {
			query.setSortField("postDate_dt", ORDER.desc);
		} else {
			query.setSortField("postDate_dt", ORDER.asc);
		}
		
		return solrOperations.count(query, CommentDO.class);
	}

	/**
	 * 指定したコメント情報を返します。
	 * @param commentId コメントID
	 * @return コメント情報
	 */
	@Override
	public CommentDO loadComment(String commentId) {
		return loadComment(commentId, getDefaultLoadCommentCondition());
	}

	/**
	 * 指定したコメント情報を返します。
	 * @param commentId コメントID
	 * @param condition 条件
	 * @return コメント情報
	 */
	@Override
	public CommentDO loadComment(String commentId, Condition condition) {
		return hBaseOperations.load(CommentDO.class, commentId, condition);
	}

	/**
	 * 指定したコメント情報をインデックス情報から返します。
	 * @param commentId コメントID
	 * @return コメント情報
	 */
	@Override
	public CommentDO loadCommentFromIndex(String commentId) {
		return loadCommentFromIndex(commentId, true);
	}


	@Override
	public CommentDO loadCommentFromIndex(String commentId,
			boolean includeDeleteContents) {

		StringBuilder buffer = new StringBuilder();
		buffer.append("commentId:");
		buffer.append(commentId);
		if(!includeDeleteContents){
			buffer.append(" AND deleteFlag_b:false");
		}

		SearchResult<CommentDO> results = new SearchResult<CommentDO>(solrOperations.findByQuery(
				new SolrQuery(buffer.toString()), CommentDO.class, getDefaultLoadCommentCondition()));
		ProductUtil.filterInvalidProduct(results);
		if(results == null || results.getDocuments().isEmpty() || results.getDocuments().size() > 1)
			return null;

		return results.getDocuments().get(0);
	}


	/**
	 * 指定した日付に指定したコミュニティユーザーの投稿レビューに
	 * コメントがついたものを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param targetDate 対象日
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return レビューリスト
	 */
	@Override
	public SearchResult<CommentDO> findCommentReviewByCommunityUserId(
			String communityUserId, Date targetDate, int limit, int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND deleteFlag_b:false AND stopFlg_b:false AND ");
		buffer.append("relationReviewOwnerId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND targetType_s:");
		buffer.append(SolrUtil.escape(CommentTargetType.REVIEW.getCode()));
		buffer.append(" AND !communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND ");
		buffer.append(SolrUtil.getSolrDateRangeQuery("postDate_dt", targetDate));
		SearchResult<CommentDO> searchResult = new SearchResult<CommentDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
						).toFilterQuery(buffer.toString())).setRows(limit).setStart(offset
								).setSortField("postDate_dt", ORDER.asc),
						CommentDO.class, Path.includeProp("*").includePath(
						"review.reviewId,communityUser.communityUserId").depth(1)));
		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}

	/**
	 * 指定した日付に指定したコミュニティユーザーの投稿回答に
	 * コメントがついたものを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param targetDate 対象日
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return 投稿回答リスト
	 */
	@Override
	public SearchResult<CommentDO> findCommentQuestionAnswerByCommunityUserId(
			String communityUserId, Date targetDate, int limit, int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND deleteFlag_b:false AND stopFlg_b:false AND ");
		buffer.append("relationQuestionAnswerOwnerId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND targetType_s:");
		buffer.append(SolrUtil.escape(CommentTargetType.QUESTION_ANSWER.getCode()));
		buffer.append(" AND !communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND ");
		buffer.append(SolrUtil.getSolrDateRangeQuery("postDate_dt", targetDate));
		SearchResult<CommentDO> searchResult = new SearchResult<CommentDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
						).toFilterQuery(buffer.toString())).setRows(limit).setStart(offset
								).setSortField("postDate_dt", ORDER.asc),
						CommentDO.class, Path.includeProp("*").includePath(
						"questionAnswer.question.questionId,questionAnswer.communityUser.communityUserId," +
						"communityUser.communityUserId").depth(2)));
		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}

	/**
	 * 指定した日付に指定したコミュニティユーザーの投稿画像に
	 * コメントがついたものを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param targetDate 対象日
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return 投稿画像リスト
	 */
	@Override
	public SearchResult<CommentDO> findCommentImageByCommunityUserId(
			String communityUserId, Date targetDate, int limit, int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND deleteFlag_b:false AND stopFlg_b:false AND ");
		buffer.append("relationImageOwnerId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND targetType_s:");
		buffer.append(SolrUtil.escape(CommentTargetType.IMAGE.getCode()));
		buffer.append(" AND !communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND ");
		buffer.append(SolrUtil.getSolrDateRangeQuery("postDate_dt", targetDate));
		SearchResult<CommentDO> searchResult = new SearchResult<CommentDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
						).toFilterQuery(buffer.toString())).setRows(limit).setStart(offset
								).setSortField("postDate_dt", ORDER.asc),
						CommentDO.class, Path.includeProp("*").includePath(
						"imageHeader.product.sku,communityUser.communityUserId").depth(2)));
		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}
	
	@Override
	public SearchResult<CommentDO> findCommentImageByCommunityUserIdForMR(
			String communityUserId, Date targetDate, int limit, int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND deleteFlag_b:false AND stopFlg_b:false AND ");
		buffer.append("relationImageOwnerId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND targetType_s:");
		buffer.append(SolrUtil.escape(CommentTargetType.IMAGE.getCode()));
		buffer.append(" AND !communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND ");
		buffer.append(SolrUtil.getSolrDateRangeQuery("postDate_dt", targetDate));
		SearchResult<CommentDO> searchResult = new SearchResult<CommentDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification())
				.toFilterQuery(buffer.toString()))
				.setRows(limit)
				.setStart(offset)
				.setSortField("postDate_dt", ORDER.asc),
				CommentDO.class, 
				Path.includeProp("*").includePath("imageHeader,communityUser.communityUserId").depth(2)));
		List<String> skus = new ArrayList<String>();
		for(CommentDO comment:searchResult.getDocuments()) {
			skus.add(comment.getImageHeader().getProduct().getSku());
		}
		Map<String, ProductDO> productMap = productDao.findBySkuForMR(skus);
		for(CommentDO comment:searchResult.getDocuments()) {
			comment.getImageHeader().setProduct(productMap.get(comment.getImageHeader().getProduct().getSku()));
		}		
		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}

	/**
	 * コメント情報を読み出すデフォルト条件を返します。
	 * @return コメント情報の読み出し条件
	 */
	private Condition getDefaultLoadCommentCondition() {
		return Path.includeProp("*").includePath(
				"communityUser.communityUserId," + 
				"review.reviewId," + 
				"questionAnswer.questionAnswerId," + 
				"imageHeader.imageId").depth(1);
	}

	private String createCommentId(CommentDO comment){
		String reverseTime = org.apache.commons.lang.StringUtils.leftPad(
				String.valueOf(Long.MAX_VALUE- timestampHolder.getTimestamp().getTime()), 20, '0');
		String generateId = idGenerator.generateId();
		if(comment.getTargetType().equals(CommentTargetType.REVIEW)){
			return IdUtil.createIdByConcatIds(comment.getReview().getReviewId(), comment.getTargetType().getCode(), reverseTime, generateId);
		}else if(comment.getTargetType().equals(CommentTargetType.IMAGE)){
			return IdUtil.createIdByConcatIds(comment.getImageHeader().getImageId(), comment.getTargetType().getCode(), reverseTime, generateId);
		}else if(comment.getTargetType().equals(CommentTargetType.QUESTION_ANSWER)){
			return IdUtil.createIdByConcatIds(comment.getQuestionAnswer().getQuestionAnswerId(), comment.getTargetType().getCode(), reverseTime, generateId);
		}
		return null;
	}
	
}
