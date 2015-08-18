/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.solr.FacetResult;
import com.kickmogu.lib.solr.SolrConstants;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.VotingDao;
import com.kickmogu.yodobashi.community.resource.dao.util.AdultHelper;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.ProductUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.SolrUtil;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.VotingDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.VotingTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.VotingType;


/**
 * いいね DAO の実装です。
 * @author kamiike
 *
 */
@Service
public class VotingDaoImpl implements VotingDao {

	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(VotingDaoImpl.class);

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
	 * コミュニティユーザーDAOです。
	 */
	@Autowired
	private CommunityUserDao communityUserDao;

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

	/**
	 * レビューに対するいいね数情報を返します。
	 * @param reviewIds レビューIDリスト
	 * @return レビューに対するするいいね数情報
	 */
	@Override
	public Map<String, Long[]> loadReviewVotingCountMap(List<String> reviewIds) {
		return loadContentsVotingCountMap(VotingTargetType.REVIEW, reviewIds, null);
	}

	/**
	 * 質問に紐づく回答に対するいいね数情報を返します。
	 * @param questionId 質問ID
	 * @return 質問に紐づく回答に対するいいね数
	 */
	@Override
	public Long[] loadQuestionVotingCount(String questionId) {
		Long[] values = new Long[2];
		StringBuilder query = new StringBuilder();
		query.append("targetType_s:");
		query.append(SolrUtil.escape(VotingTargetType.QUESTION_ANSWER.getCode()));
		query.append(" AND ");
		query.append("votingType_s:");
		query.append(SolrUtil.escape(VotingType.YES.getCode()));
		query.append(" AND ");
		query.append("questionId_s:");
		query.append(SolrUtil.escape(questionId));
		values[0] = solrOperations.count(new SolrQuery(query.toString()), VotingDO.class);
		
		query = new StringBuilder();
		query.append("targetType_s:");
		query.append(SolrUtil.escape(VotingTargetType.QUESTION_ANSWER.getCode()));
		query.append(" AND ");
		query.append("votingType_s:");
		query.append(SolrUtil.escape(VotingType.NO.getCode()));
		query.append(" AND ");
		query.append("questionId_s:");
		query.append(SolrUtil.escape(questionId));
		values[1] = solrOperations.count(new SolrQuery(query.toString()), VotingDO.class);
		
		return values;
	}

	/**
	 * 質問に紐付く回答に対するいいね数情報を返します。
	 * @param questionIds 質問回答IDリスト
	 * @return 質問に紐付く回答に対するするいいね数情報
	 */
	@Override
	public Map<String, Long[]> loadQuestionVotingCountMap(List<String> questionIds) {
		return loadContentsVotingCountMap(VotingTargetType.QUESTION_ANSWER, questionIds, "questionId_s");
	}

	/**
	 * 質問回答に対するするいいね数情報を返します。
	 * @param questionAnswerIds 質問回答IDリスト
	 * @return 質問回答に対するするいいね数情報
	 */
	@Override
	public Map<String, Long[]> loadQuestionAnswerVotingCountMap(List<String> questionAnswerIds) {
		return loadContentsVotingCountMap(VotingTargetType.QUESTION_ANSWER, questionAnswerIds, null);
	}

	/**
	 * 画像に対するいいね数情報を返します。
	 * @param imageIds 画像IDリスト
	 * @return 画像に対するするいいね数情報
	 */
	@Override
	public Map<String, Long[]> loadImageVotingCountMap(List<String> imageIds) {
		return loadContentsVotingCountMap(VotingTargetType.IMAGE, imageIds, null);
	}

	/**
	 * 画像セットに対するいいね数情報を返します。
	 * @param imageSetIds 画像IDリスト
	 * @return 画像セットに対するするいいね数情報
	 */
	@Override
	public Map<String, Long[]> loadImageSetVotingCountMap(List<String> imageSetIds) {
		return loadContentsVotingCountMap(VotingTargetType.IMAGE, imageSetIds, "imageSetId_s");
	}


	private String createCountMapFacetQuery(VotingTargetType targetType, VotingType votingType, String contentField, String contentId){
		StringBuilder query = new StringBuilder();
		query.append("targetType_s:");
		query.append(SolrUtil.escape(targetType.getCode()));
		query.append(" AND ");
		query.append("votingType_s:");
		query.append(SolrUtil.escape(votingType.getCode()));
		query.append(" AND ");
		query.append(contentField + ":");
		query.append(SolrUtil.escape(contentId));
		return query.toString();
	}

	@Override
	public void loadContentsVotingCountMap(
			List<String> reviewIds,
			List<String> questionIds,
			List<String> questionAnswerIds,
			List<String> imageSetIds,
			List<String> imageIds,
			Map<String, Long[]> reviewCountMap,
			Map<String, Long[]> questionCountMap,
			Map<String, Long[]> questionAnswerCountMap,
			Map<String, Long[]> imageSetCountMap,
			Map<String, Long[]> imageCountMap) {

		Map<String, String> reviewQueryMap = new HashMap<String,String>();
		Map<String, String> questionQueryMap = new HashMap<String,String>();
		Map<String, String> questionAnswerQueryMap = new HashMap<String,String>();
		Map<String, String> imageSetQueryMap = new HashMap<String,String>();
		Map<String, String> imageQueryMap = new HashMap<String,String>();

		SolrQuery solrQuery = new SolrQuery("*:*");

		if(reviewIds != null && !reviewIds.isEmpty()){
			for(String reviewId:reviewIds){
				String query = createCountMapFacetQuery(VotingTargetType.REVIEW, VotingType.YES, "reviewId_s", reviewId);
				reviewQueryMap.put(query, reviewId);
				solrQuery.addFacetQuery(query);
			}
		}
		if(questionIds != null && !questionIds.isEmpty()){
			for(String questionId:questionIds){
				String query = createCountMapFacetQuery(VotingTargetType.QUESTION_ANSWER, VotingType.YES, "questionId_s", questionId);
				questionQueryMap.put(query, questionId);
				solrQuery.addFacetQuery(query);
			}
		}
		if(questionAnswerIds != null && !questionAnswerIds.isEmpty()){
			for(String questionAnswerId:questionAnswerIds){
				String query = createCountMapFacetQuery(VotingTargetType.QUESTION_ANSWER, VotingType.YES, "questionAnswerId_s", questionAnswerId);
				questionAnswerQueryMap.put(query, questionAnswerId);
				solrQuery.addFacetQuery(query);
			}
		}
		if(imageSetIds != null && !imageSetIds.isEmpty()){
			for(String imageSetId:imageSetIds){
				String query = createCountMapFacetQuery(VotingTargetType.IMAGE, VotingType.YES, "imageSetId_s", imageSetId);
				imageSetQueryMap.put(query, imageSetId);
				solrQuery.addFacetQuery(query);
			}
		}
		if(imageIds != null && !imageIds.isEmpty()){
			for(String imageId:imageIds){
				String query = createCountMapFacetQuery(VotingTargetType.IMAGE, VotingType.YES, "imageHeaderId_s", imageId);
				imageQueryMap.put(query, imageId);
				solrQuery.addFacetQuery(query);
			}
		}

		if(solrQuery.getFacetQuery() != null && solrQuery.getFacetQuery().length > 0){
			solrQuery.setFacetLimit(solrQuery.getFacetQuery().length);
			solrQuery.setFacetMinCount(0);
			for (FacetResult<String> facetResult : solrOperations.facet(VotingDO.class, String.class, solrQuery)) {
				if(reviewQueryMap.containsKey(facetResult.getFacetQuery())){
					reviewCountMap.put(reviewQueryMap.get(facetResult.getFacetQuery()), new Long[] {facetResult.getCount(), 0L});
				}
				if(questionQueryMap.containsKey(facetResult.getFacetQuery())){
					questionCountMap.put(questionQueryMap.get(facetResult.getFacetQuery()), new Long[] {facetResult.getCount(), 0L});
				}
				if(questionAnswerQueryMap.containsKey(facetResult.getFacetQuery())){
					questionAnswerCountMap.put(questionAnswerQueryMap.get(facetResult.getFacetQuery()), new Long[] {facetResult.getCount(), 0L});
				}
				if(imageSetQueryMap.containsKey(facetResult.getFacetQuery())){
					imageSetCountMap.put(imageSetQueryMap.get(facetResult.getFacetQuery()), new Long[] {facetResult.getCount(), 0L});
				}
				if(imageQueryMap.containsKey(facetResult.getFacetQuery())){
					imageCountMap.put(imageQueryMap.get(facetResult.getFacetQuery()), new Long[] {facetResult.getCount(), 0L});
				}
			}
		}
		
		reviewQueryMap.clear();
		questionQueryMap.clear();
		questionAnswerQueryMap.clear();
		imageSetQueryMap.clear();
		imageQueryMap.clear();
		
		solrQuery = new SolrQuery("*:*");
		
		if(reviewIds != null && !reviewIds.isEmpty()){
			for(String reviewId:reviewIds){
				String query = createCountMapFacetQuery(VotingTargetType.REVIEW, VotingType.NO, "reviewId_s", reviewId);
				reviewQueryMap.put(query, reviewId);
				solrQuery.addFacetQuery(query);
			}
		}
		if(questionIds != null && !questionIds.isEmpty()){
			for(String questionId:questionIds){
				String query = createCountMapFacetQuery(VotingTargetType.QUESTION_ANSWER, VotingType.NO, "questionId_s", questionId);
				questionQueryMap.put(query, questionId);
				solrQuery.addFacetQuery(query);
			}
		}
		if(questionAnswerIds != null && !questionAnswerIds.isEmpty()){
			for(String questionAnswerId:questionAnswerIds){
				String query = createCountMapFacetQuery(VotingTargetType.QUESTION_ANSWER, VotingType.NO, "questionAnswerId_s", questionAnswerId);
				questionAnswerQueryMap.put(query, questionAnswerId);
				solrQuery.addFacetQuery(query);
			}
		}
		if(imageSetIds != null && !imageSetIds.isEmpty()){
			for(String imageSetId:imageSetIds){
				String query = createCountMapFacetQuery(VotingTargetType.IMAGE, VotingType.NO, "imageSetId_s", imageSetId);
				imageSetQueryMap.put(query, imageSetId);
				solrQuery.addFacetQuery(query);
			}
		}
		if(imageIds != null && !imageIds.isEmpty()){
			for(String imageId:imageIds){
				String query = createCountMapFacetQuery(VotingTargetType.IMAGE, VotingType.NO, "imageHeaderId_s", imageId);
				imageQueryMap.put(query, imageId);
				solrQuery.addFacetQuery(query);
			}
		}

		if(solrQuery.getFacetQuery() != null && solrQuery.getFacetQuery().length > 0){
			solrQuery.setFacetLimit(solrQuery.getFacetQuery().length);
			solrQuery.setFacetMinCount(0);
			for (FacetResult<String> facetResult : solrOperations.facet(VotingDO.class, String.class, solrQuery)) {
				if(reviewQueryMap.containsKey(facetResult.getFacetQuery())){
					String contentIdString = reviewQueryMap.get(facetResult.getFacetQuery());
					Long[] values = reviewCountMap.get(contentIdString);
					if( values != null )
						values[1] = facetResult.getCount();
					else
						values = new Long[] {0L, facetResult.getCount()};
					reviewCountMap.put(contentIdString, values);
				}
				if(questionQueryMap.containsKey(facetResult.getFacetQuery())){
					String contentIdString = questionQueryMap.get(facetResult.getFacetQuery());
					Long[] values = questionCountMap.get(contentIdString);
					if( values != null )
						values[1] = facetResult.getCount();
					else
						values = new Long[] {0L, facetResult.getCount()};
					questionCountMap.put(contentIdString, values);
				}
				if(questionAnswerQueryMap.containsKey(facetResult.getFacetQuery())){
					String contentIdString = questionAnswerQueryMap.get(facetResult.getFacetQuery());
					Long[] values = questionAnswerCountMap.get(contentIdString);
					if( values != null )
						values[1] = facetResult.getCount();
					else
						values = new Long[] {0L, facetResult.getCount()};
					questionAnswerCountMap.put(contentIdString, values);
				}
				if(imageSetQueryMap.containsKey(facetResult.getFacetQuery())){
					String contentIdString = imageSetQueryMap.get(facetResult.getFacetQuery());
					Long[] values = imageSetCountMap.get(contentIdString);
					if( values != null )
						values[1] = facetResult.getCount();
					else
						values = new Long[] {0L, facetResult.getCount()};
					imageSetCountMap.put(contentIdString, values);
				}
				if(imageQueryMap.containsKey(facetResult.getFacetQuery())){
					String contentIdString = imageQueryMap.get(facetResult.getFacetQuery());
					Long[] values = imageCountMap.get(contentIdString);
					if( values != null )
						values[1] = facetResult.getCount();
					else
						values = new Long[] {0L, facetResult.getCount()};
					imageCountMap.put(contentIdString, values);
				}
			}
		}
	}

	/**
	 * コンテンツに対するするいいね数情報を返します。
	 * @param contentsIds コンテンツIDリスト
	 * @param targetType 対象タイプ
	 * @param facetField ファセットフィールド
	 * @return コンテンツに対するするいいね数情報
	 */
	public Map<String, Long[]> loadContentsVotingCountMap(
			VotingTargetType targetType,
			List<String> contentsIds,
			String otherFacetField) {
		Map<String, Long[]> resultMap = new HashMap<String, Long[]>();
		if (contentsIds == null || contentsIds.size() == 0) {
			return resultMap;
		}
		String facetField = null;
		if( otherFacetField != null){
			facetField = otherFacetField;
		}else{
			if( VotingTargetType.REVIEW.equals(targetType)){
				facetField = "reviewId_s";
			}else if( VotingTargetType.QUESTION_ANSWER.equals(targetType)){
				facetField = "questionAnswerId_s";
			}else if( VotingTargetType.IMAGE.equals(targetType)){
				facetField = "imageHeaderId_s";
			}else{
				return resultMap;
			}
		}
		
		Long[] values = null;
		StringBuilder query = new StringBuilder();
		query.append("targetType_s:");
		query.append(SolrUtil.escape(targetType.getCode()));
		query.append(" AND ");
		query.append("votingType_s:");
		query.append(SolrUtil.escape(VotingType.YES.getCode()));
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
		for (FacetResult<String> facetResult : solrOperations.facet(
				VotingDO.class, String.class, new SolrQuery(
						query.toString()).setFacetLimit(
								SolrConstants.QUERY_ROW_LIMIT).addFacetField(facetField))) {
			values = new Long[]{facetResult.getCount(), 0L};
			resultMap.put(facetResult.getValue(), values);
		}
		
		query = new StringBuilder();
		query.append("targetType_s:");
		query.append(SolrUtil.escape(targetType.getCode()));
		query.append(" AND ");
		query.append("votingType_s:");
		query.append(SolrUtil.escape(VotingType.NO.getCode()));
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
		for (FacetResult<String> facetResult : solrOperations.facet(
				VotingDO.class, String.class, new SolrQuery(
						query.toString()).setFacetLimit(
								SolrConstants.QUERY_ROW_LIMIT).addFacetField(facetField))) {
			values = resultMap.get(facetResult.getValue());
			if( values != null )
				values[1] = facetResult.getCount();
			else {
				values = new Long[]{0L, facetResult.getCount()};
			}
			resultMap.put(facetResult.getValue(), values);
		}
		
		return resultMap;
	}

	/**
	 * 指定したコミュニティユーザー、コンテンツID、いいねタイプのいいね
	 * が存在するか判定します。
	 * @param communityUserId コミュニティユーザーID
	 * @param contentsId 対象となるコンテンツID
	 * @param type コンテンツタイプ
	 * @return いいね済みの場合、true
	 */
	@Override
	public boolean existsVoting(
			String communityUserId, String contentsId, VotingTargetType type) {
		return hBaseOperations.load(VotingDO.class, IdUtil.createIdByConcatIds(
				communityUserId, contentsId, type.getCode()), Path.includeProp("votingId")) != null;
	}

	/**
	 * 指定したコミュニティユーザー、コンテンツID、いいねタイプのいいね
	 * を削除します。
	 * @param communityUserId コミュニティユーザーID
	 * @param contentsId 対象となるコンテンツID
	 * @param type コンテンツタイプ
	 * @return いいねID
	 */
	@Override
	public String deleteVoting(
			String communityUserId,
			String contentsId,
			VotingTargetType type) {
		String votingId = IdUtil.createIdByConcatIds(
				communityUserId, contentsId, type.getCode());
		//ActionHistoryDO は、いいねを削除しても削除しない。
		hBaseOperations.scanDeleteWithIndex(InformationDO.class, "votingId", votingId);
		hBaseOperations.deleteByKey(VotingDO.class, votingId);
		return votingId;
	}

	/**
	 * 参考になった情報を新規に作成します。
	 * @param Voting いいね
	 */
	@Override
	public void createVoting(VotingDO voting) {
		
		String contentsId = null;
		
		if (voting.getTargetType().equals(VotingTargetType.REVIEW)) {
			contentsId = voting.getReview().getReviewId();
			voting.setReview(hBaseOperations.load(
					ReviewDO.class,
					contentsId, 
					Path.includeProp("reviewId,productId,communityUserId,adult")));
			voting.setRelationReviewOwnerId(voting.getReview().getCommunityUser().getCommunityUserId());
			voting.setSku(voting.getReview().getProduct().getSku());
			voting.setAdult(voting.getReview().isAdult());
		} else if (voting.getTargetType().equals(VotingTargetType.QUESTION_ANSWER)) {
			contentsId = voting.getQuestionAnswer().getQuestionAnswerId();
			voting.setQuestionAnswer(hBaseOperations.load(
					QuestionAnswerDO.class,
					contentsId,
					Path.includeProp("*").includePath("question.questionId").depth(1)));
			voting.setQuestionId(voting.getQuestionAnswer().getQuestion().getQuestionId());
			voting.setRelationQuestionAnswerOwnerId(voting.getQuestionAnswer().getCommunityUser().getCommunityUserId());
			voting.setRelationQuestionOwnerId(voting.getQuestionAnswer().getQuestion().getCommunityUser().getCommunityUserId());
			voting.setSku(voting.getQuestionAnswer().getProduct().getSku());
			voting.setAdult(voting.getQuestionAnswer().isAdult());
		} else {
			contentsId = voting.getImageHeader().getImageId();
			voting.setImageHeader(hBaseOperations.load(
					ImageHeaderDO.class,
					contentsId,
					Path.includeProp("imageId,imageSetId,productId,ownerCommunityUserId,adult")));
			voting.setRelationImageOwnerId(voting.getImageHeader().getOwnerCommunityUserId());
			voting.setSku(voting.getImageHeader().getSku());
			voting.setImageSetId(voting.getImageHeader().getImageSetId());
			voting.setAdult(voting.getImageHeader().isAdult());
		}
		
		voting.setVotingId(
				createVotingId(
						voting.getCommunityUser().getCommunityUserId(),
						contentsId, voting.getTargetType()));
		
		voting.setPostDate(timestampHolder.getTimestamp());
		voting.setRegisterDateTime(timestampHolder.getTimestamp());
		voting.setModifyDateTime(timestampHolder.getTimestamp());
		
		hBaseOperations.save(voting);
	}
	/**
	 * 参考になった情報を更新します。
	 * @param voting 参考になった情報
	 */
	@Override
	public void updateVoting(VotingDO voting) {
		voting.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(voting);
	}

	/**
	 * 参考になった情報のインデックスを更新します。
	 * @param VotingId いいねID
	 */
	@Override
	public void updateVotingInIndex(String votingId) {

		Log.info(">!>!>!>!> Voting load Hbase Start load votingId:" + votingId);
		VotingDO voting = hBaseOperations.load(VotingDO.class, votingId);
		Log.info(">!>!>!>!> Voting load Hbase End");
		
		Log.info((voting == null ? ">!>!>!>!> Voting is null": ">!>!>!>!> Voting is not null " + voting.toString()));

		if (voting != null) {
			Log.info(">!>!>!>!> Index Append or Modify");

			solrOperations.save(voting);
		} else {
			Log.info(">!>!>!>!> Index Remove");
			
			solrOperations.deleteByQuery(new SolrQuery("votingId_s:" + SolrUtil.escape(votingId)), InformationDO.class);
			solrOperations.deleteByKey(VotingDO.class, votingId);
			Log.info(">!>!>!>!> Index Remove votingId:" + SolrUtil.escape(votingId));
		}
	}
	
	/**
	 * 指定のユーザーおよびコンテンツの参考になった情報を取得する。
	 * @param communityUserId コミュニティユーザーID
	 * @param contentsId コンテンツID
	 * @param targetType コンテンツタイプ
	 * @return 参考になった情報
	 */
	@Override
	public VotingDO loadVoting(String communityUserId, String contentsId, VotingTargetType targetType) {
		if (StringUtils.isEmpty(communityUserId) || StringUtils.isEmpty(contentsId) || targetType == null)
			return null;
		
		return hBaseOperations.load(VotingDO.class, createVotingId(communityUserId, contentsId, targetType));
	}
	
	/**
	 * レビューのいいね情報マップを返します。
	 * @param communityUserId 対象となるコミュニティユーザーID
	 * @param reviewIds レビューIDリスト
	 * @return レビューのいいね情報マップ
	 */
	@Override
	public Map<String, VotingType> loadReviewVotingMap(
			String communityUserId,
			List<String> reviewIds) {
		Map<String, VotingType> resultMap = new HashMap<String, VotingType>();
		if (StringUtils.isEmpty(communityUserId) || reviewIds == null || reviewIds.size() == 0) {
			return resultMap;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("targetType_s:");
		buffer.append(SolrUtil.escape(VotingTargetType.REVIEW.getCode()));
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

		for (VotingDO voting : solrOperations.findByQuery(
				new SolrQuery(buffer.toString()).setRows(
						SolrConstants.QUERY_ROW_LIMIT), VotingDO.class,
						Path.includeProp("reviewId")).getDocuments()) {
			resultMap.put(voting.getReview().getReviewId(), voting.getVotingType());
		}
		return resultMap;
	}

	/**
	 * 質問回答のいいね情報マップを返します。
	 * @param communityUserId 対象となるコミュニティユーザーID
	 * @param questionAnswerIds 質問回答IDリスト
	 * @return 質問回答のいいね情報マップ
	 */
	@Override
	public Map<String, VotingType> loadQuestionAnswerVotingMap(
			String communityUserId, List<String> questionAnswerIds) {
		Map<String, VotingType> resultMap = new HashMap<String, VotingType>();
		if (StringUtils.isEmpty(communityUserId) || questionAnswerIds == null || questionAnswerIds.size() == 0) {
			return resultMap;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("targetType_s:");
		buffer.append(SolrUtil.escape(VotingTargetType.QUESTION_ANSWER.getCode()));
		buffer.append(" AND communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND (");
		for (int i = 0; i < questionAnswerIds.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("questionAnswerId_s:");
			buffer.append(SolrUtil.escape(questionAnswerIds.get(i)));
		}
		buffer.append(")");

		for (VotingDO voting : solrOperations.findByQuery(
				new SolrQuery(buffer.toString()).setRows(
						SolrConstants.QUERY_ROW_LIMIT), VotingDO.class,
						Path.includeProp("questionAnswerId")).getDocuments()) {
			resultMap.put(voting.getQuestionAnswer().getQuestionAnswerId(), voting.getVotingType());
		}
		return resultMap;
	}

	/**
	 * 画像のいいね情報マップを返します。
	 * @param communityUserId 対象となるコミュニティユーザーID
	 * @param imageIds 画像IDリスト
	 * @return 画像のいいね情報マップ
	 */
	@Override
	public Map<String, VotingType> loadImageVotingMap(
			String communityUserId, List<String> imageIds) {
		Map<String, VotingType> resultMap = new HashMap<String, VotingType>();
		if (StringUtils.isEmpty(communityUserId) || imageIds == null || imageIds.size() == 0) {
			return resultMap;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("targetType_s:");
		buffer.append(SolrUtil.escape(VotingTargetType.IMAGE.getCode()));
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

		for (VotingDO voting : solrOperations.findByQuery(
				new SolrQuery(buffer.toString()).setRows(
						SolrConstants.QUERY_ROW_LIMIT), VotingDO.class,
						Path.includeProp("imageHeaderId")).getDocuments()) {
			resultMap.put(voting.getImageHeader().getImageId(), voting.getVotingType());
		}
		return resultMap;
	}

	/**
	 * 投稿したレビューにいいねをしたユーザーを重複を除いて返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findDistinctVotingUserByReview(
			String communityUserId, int limit, int offset) {
		//重複除去と投稿順の並び替えを同時に満たすため、solr ではサブクエリ
		//とdistinctをサポートしていないので、上限数を絞り、java 側で処理します。
		StringBuilder buffer = new StringBuilder();
		buffer.append("relationReviewOwnerId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND targetType_s:");
		buffer.append(SolrUtil.escape(VotingTargetType.REVIEW.getCode()));
		return createDistinctCommunityUsers(buffer.toString(), limit, offset);
	}

	/**
	 * 投稿した質問回答にいいねをしたユーザーを重複を除いて返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findDistinctVotingUserByQuestionAnswer(
			String communityUserId, int limit, int offset) {
		//重複除去と投稿順の並び替えを同時に満たすため、solr ではサブクエリ
		//とdistinctをサポートしていないので、上限数を絞り、java 側で処理します。
		StringBuilder buffer = new StringBuilder();
		buffer.append("relationQuestionAnswerOwnerId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND targetType_s:");
		buffer.append(SolrUtil.escape(VotingTargetType.QUESTION_ANSWER.getCode()));
		return createDistinctCommunityUsers(buffer.toString(), limit, offset);
	}

	/**
	 * 投稿した画像にいいねをしたユーザーを重複を除いて返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findDistinctVotingUserByImage(
			String communityUserId, int limit, int offset) {
		//重複除去と投稿順の並び替えを同時に満たすため、solr ではサブクエリ
		//とdistinctをサポートしていないので、上限数を絞り、java 側で処理します。
		StringBuilder buffer = new StringBuilder();
		buffer.append("relationImageOwnerId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND targetType_s:");
		buffer.append(SolrUtil.escape(VotingTargetType.IMAGE.getCode()));
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
		SearchResult<CommunityUserDO> result = new SearchResult<CommunityUserDO>();
		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());
		SearchResult<VotingDO> votings = new SearchResult<VotingDO>(
				solrOperations.findByQuery(
				new SolrQuery(adultHelper.toFilterQuery(query)).setRows(
								SolrConstants.QUERY_ROW_LIMIT).setStart(
										0).addSortField("postDate_dt", ORDER.desc),
										VotingDO.class, Path.includeProp("communityUserId")));
		if (adultHelper.isRequireCheckAdult()) {
			result.setHasAdult(
					adultHelper.hasAdult(
							query, ReviewDO.class, solrOperations));
		}
		if (votings.getNumFound() == 0) {
			return result;
		}
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(votings.getDocuments());
		List<String> communityUserIds = new ArrayList<String>();
		List<String> communityUserIdAll = new ArrayList<String>();
		String communityUserId = requestScopeDao.loadCommunityUserId();
		for (VotingDO review : votings.getDocuments()) {
			// 一時停止対応
			if (review.isStop(communityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
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
		if(!communityUserIds.isEmpty()){
			Map<String, CommunityUserDO> resultMap = solrOperations.find(
					CommunityUserDO.class, String.class, communityUserIds);
			for (String target : communityUserIds) {
				result.getDocuments().add(resultMap.get(target));
			}
		}
		return result;
	}

	/**
	 * 指定したコンテンツに対するいいねを返します。
	 * @param type タイプ
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param contentsId コンテンツID
	 * @param limit 最大取得件数
	 * @return コメントリスト
	 */
	@Override
	public SearchResult<VotingDO> findVotingByContentsId(
			VotingTargetType type,
			String excludeCommunityUserId,
			String contentsId,
			int limit) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("targetType_s:");
		buffer.append(SolrUtil.escape(type.getCode()));
		if (excludeCommunityUserId != null) {
			buffer.append(" AND !communityUserId_s:");
			buffer.append(SolrUtil.escape(excludeCommunityUserId));
		}
		if (type.equals(VotingTargetType.REVIEW)) {
			buffer.append(" AND reviewId_s:");
		} else if (type.equals(VotingTargetType.QUESTION_ANSWER)) {
			buffer.append(" AND questionAnswerId_s:");
		} else if (type.equals(VotingTargetType.IMAGE)) {
			buffer.append(" AND imageHeaderId_s:");
		}
		buffer.append(SolrUtil.escape(contentsId));
		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());
		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(buffer.toString()));
		query.setRows(limit);
		query.setSortField("postDate_dt", ORDER.desc);
		SearchResult<VotingDO> searchResult = new SearchResult<VotingDO>(
				solrOperations.findByQuery(query, VotingDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId").depth(1)));
		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(
							buffer.toString(), VotingDO.class, solrOperations));
		}
		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}

	/**
	 * いいね情報IDを生成して返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param contentsId 対象となるコンテンツID
	 * @param type コンテンツタイプ
	 * @return いいね情報ID
	 */
	protected String createVotingId(String communityUserId, String contentsId, VotingTargetType type) {
		return IdUtil.createIdByConcatIds(communityUserId, contentsId, type.getCode());
	}
}
