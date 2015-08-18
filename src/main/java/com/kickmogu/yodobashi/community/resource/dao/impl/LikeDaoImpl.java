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
import com.kickmogu.yodobashi.community.resource.dao.LikeDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.util.AdultHelper;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.ProductUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.SolrUtil;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.LikeTargetType;


/**
 * いいね DAO の実装です。
 * @author kamiike
 *
 */
@Service
public class LikeDaoImpl implements LikeDao {

	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(LikeDaoImpl.class);

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
	public Map<String, Long> loadReviewLikeCountMap(List<String> reviewIds) {
		return loadContentsLikeCountMap(reviewIds, LikeTargetType.REVIEW, "reviewId_s");
	}

	/**
	 * 質問に紐づく回答に対するいいね数情報を返します。
	 * @param questionId 質問ID
	 * @return 質問に紐づく回答に対するいいね数
	 */
	@Override
	public long loadQuestionLikeCount(String questionId) {
		StringBuilder query = new StringBuilder();
		query.append("targetType_s:");
		query.append(SolrUtil.escape(LikeTargetType.QUESTION_ANSWER.getCode()));
		query.append(" AND ");
		query.append("questionId_s:");
		query.append(SolrUtil.escape(questionId));
		return solrOperations.count(new SolrQuery(query.toString()), LikeDO.class);
	}

	/**
	 * 質問に紐付く回答に対するいいね数情報を返します。
	 * @param questionIds 質問回答IDリスト
	 * @return 質問に紐付く回答に対するするいいね数情報
	 */
	@Override
	public Map<String, Long> loadQuestionLikeCountMap(List<String> questionIds) {
		return loadContentsLikeCountMap(questionIds, LikeTargetType.QUESTION_ANSWER, "questionId_s");
	}

	/**
	 * 質問回答に対するするいいね数情報を返します。
	 * @param questionAnswerIds 質問回答IDリスト
	 * @return 質問回答に対するするいいね数情報
	 */
	@Override
	public Map<String, Long> loadQuestionAnswerLikeCountMap(List<String> questionAnswerIds) {
		return loadContentsLikeCountMap(questionAnswerIds, LikeTargetType.QUESTION_ANSWER, "questionAnswerId_s");
	}

	/**
	 * 画像に対するいいね数情報を返します。
	 * @param imageIds 画像IDリスト
	 * @return 画像に対するするいいね数情報
	 */
	@Override
	public Map<String, Long> loadImageLikeCountMap(List<String> imageIds) {
		return loadContentsLikeCountMap(imageIds, LikeTargetType.IMAGE, "imageHeaderId_s");
	}

	/**
	 * 画像セットに対するいいね数情報を返します。
	 * @param imageSetIds 画像IDリスト
	 * @return 画像セットに対するするいいね数情報
	 */
	@Override
	public Map<String, Long> loadImageSetLikeCountMap(List<String> imageSetIds) {
		return loadContentsLikeCountMap(imageSetIds, LikeTargetType.IMAGE, "imageSetId_s");
	}


	private String createCountMapFacetQuery(LikeTargetType targetType, String contentField, String contentId){
		StringBuilder query = new StringBuilder();
		query.append("targetType_s:");
		query.append(SolrUtil.escape(targetType.getCode()));
		query.append(" AND ");
		query.append(contentField + ":");
		query.append(SolrUtil.escape(contentId));
		return query.toString();
	}

	@Override
	public void loadContentsLikeCountMap(List<String> reviewIds, List<String> questionIds, List<String> questionAnswerIds, List<String> imageSetIds, List<String> imageIds,
			Map<String, Long> reviewCountMap, Map<String, Long> questionCountMap, Map<String, Long> questionAnswerCountMap, Map<String, Long> imageSetCountMap, Map<String, Long> imageCountMap) {

		Map<String, String> reviewQueryMap = new HashMap<String,String>();
		Map<String, String> questionQueryMap = new HashMap<String,String>();
		Map<String, String> questionAnswerQueryMap = new HashMap<String,String>();
		Map<String, String> imageSetQueryMap = new HashMap<String,String>();
		Map<String, String> imageQueryMap = new HashMap<String,String>();

		SolrQuery solrQuery = new SolrQuery("*:*");

		if(reviewIds != null && !reviewIds.isEmpty()){
			for(String reviewId:reviewIds){
				String query = createCountMapFacetQuery(LikeTargetType.REVIEW, "reviewId_s", reviewId);
				reviewQueryMap.put(query, reviewId);
				solrQuery.addFacetQuery(query);
			}
		}
		if(questionIds != null && !questionIds.isEmpty()){
			for(String questionId:questionIds){
				String query = createCountMapFacetQuery(LikeTargetType.QUESTION_ANSWER, "questionId_s", questionId);
				questionQueryMap.put(query, questionId);
				solrQuery.addFacetQuery(query);
			}
		}
		if(questionAnswerIds != null && !questionAnswerIds.isEmpty()){
			for(String questionAnswerId:questionAnswerIds){
				String query = createCountMapFacetQuery(LikeTargetType.QUESTION_ANSWER, "questionAnswerId_s", questionAnswerId);
				questionAnswerQueryMap.put(query, questionAnswerId);
				solrQuery.addFacetQuery(query);
			}
		}
		if(imageSetIds != null && !imageSetIds.isEmpty()){
			for(String imageSetId:imageSetIds){
				String query = createCountMapFacetQuery(LikeTargetType.IMAGE, "imageSetId_s", imageSetId);
				imageSetQueryMap.put(query, imageSetId);
				solrQuery.addFacetQuery(query);
			}
		}
		if(imageIds != null && !imageIds.isEmpty()){
			for(String imageId:imageIds){
				String query = createCountMapFacetQuery(LikeTargetType.IMAGE, "imageHeaderId_s", imageId);
				imageQueryMap.put(query, imageId);
				solrQuery.addFacetQuery(query);
			}
		}

		if(solrQuery.getFacetQuery() != null && solrQuery.getFacetQuery().length > 0){
			solrQuery.setFacetLimit(solrQuery.getFacetQuery().length);
			solrQuery.setFacetMinCount(0);
			for (FacetResult<String> facetResult : solrOperations.facet(LikeDO.class, String.class, solrQuery)) {
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
				if(imageQueryMap.containsKey(facetResult.getFacetQuery())){
					imageCountMap.put(imageQueryMap.get(facetResult.getFacetQuery()), facetResult.getCount());
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
	private Map<String, Long> loadContentsLikeCountMap(
			List<String> contentsIds,
			LikeTargetType targetType,
			String facetField) {
		Map<String, Long> resultMap = new HashMap<String, Long>();
		if (contentsIds == null || contentsIds.size() == 0) {
			return resultMap;
		}
		StringBuilder query = new StringBuilder();
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
		for (FacetResult<String> facetResult : solrOperations.facet(
				LikeDO.class, String.class, new SolrQuery(
						query.toString()).setFacetLimit(
								SolrConstants.QUERY_ROW_LIMIT).addFacetField(facetField))) {
			resultMap.put(facetResult.getValue(), facetResult.getCount());
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
	public boolean existsLike(
			String communityUserId, String contentsId, LikeTargetType type) {
		return hBaseOperations.load(LikeDO.class, IdUtil.createIdByConcatIds(
				communityUserId, contentsId, type.getCode()), Path.includeProp("likeId")) != null;
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
	public String deleteLike(
			String communityUserId,
			String contentsId,
			LikeTargetType type) {
		String likeId = IdUtil.createIdByConcatIds(
				communityUserId, contentsId, type.getCode());
		//ActionHistoryDO は、いいねを削除しても削除しない。
		hBaseOperations.scanDeleteWithIndex(
				InformationDO.class, "likeId", likeId);
		hBaseOperations.deleteByKey(LikeDO.class, likeId);
		return likeId;
	}

	/**
	 * いいね情報を新規に作成します。
	 * @param like いいね
	 */
	@Override
	public void createLike(
			LikeDO like) {
		like.setRegisterDateTime(timestampHolder.getTimestamp());
		like.setModifyDateTime(timestampHolder.getTimestamp());
		String contentsId = null;
		if (like.getTargetType().equals(LikeTargetType.REVIEW)) {
			contentsId = like.getReview().getReviewId();
			like.setReview(hBaseOperations.load(ReviewDO.class,
					contentsId, Path.includeProp("reviewId,productId,communityUserId,adult")));
			like.setRelationReviewOwnerId(
					like.getReview().getCommunityUser().getCommunityUserId());
			like.setSku(like.getReview().getProduct().getSku());
			like.setAdult(like.getReview().isAdult());
		} else if (like.getTargetType().equals(LikeTargetType.QUESTION_ANSWER)) {
			contentsId = like.getQuestionAnswer().getQuestionAnswerId();
			like.setQuestionAnswer(hBaseOperations.load(QuestionAnswerDO.class,
					contentsId, Path.includeProp(
							"*").includePath("question.questionId").depth(1)));
			like.setQuestionId(like.getQuestionAnswer().getQuestion(
							).getQuestionId());
			like.setRelationQuestionAnswerOwnerId(like.getQuestionAnswer(
					).getCommunityUser().getCommunityUserId());
			like.setRelationQuestionOwnerId(like.getQuestionAnswer(
					).getQuestion().getCommunityUser().getCommunityUserId());
			like.setSku(like.getQuestionAnswer().getProduct().getSku());
			like.setAdult(like.getQuestionAnswer().isAdult());
		} else {
			contentsId = like.getImageHeader().getImageId();
			like.setImageHeader(hBaseOperations.load(ImageHeaderDO.class,
					contentsId, Path.includeProp("imageId,imageSetId,productId,ownerCommunityUserId,adult")));
			like.setRelationImageOwnerId(
					like.getImageHeader().getOwnerCommunityUserId());
			like.setSku(like.getImageHeader().getSku());
			like.setImageSetId(like.getImageHeader().getImageSetId());
			like.setAdult(like.getImageHeader().isAdult());
		}
		like.setLikeId(createLikeId(like.getCommunityUser().getCommunityUserId(),
				contentsId, like.getTargetType()));
		like.setPostDate(timestampHolder.getTimestamp());
		hBaseOperations.save(like);
	}

	/**
	 * いいね情報のインデックスを更新します。
	 * @param likeId いいねID
	 */
	@Override
	public void updateLikeInIndex(String likeId) {

		Log.info(">!>!>!>!> Like load Hbase Start load LikeId:" + likeId);
		LikeDO like = hBaseOperations.load(LikeDO.class, likeId);
		Log.info(">!>!>!>!> Like load Hbase End");
		if(like == null){
			Log.info(">!>!>!>!> Like is null");
		}else{
			Log.info(">!>!>!>!> Like is not null " + like.toString());
		}

		if (like != null) {

			Log.info(">!>!>!>!> Index Append or Modify");

			solrOperations.save(like);
		} else {

			Log.info(">!>!>!>!> Index Remove");
			solrOperations.deleteByQuery(new SolrQuery(
					"likeId_s:" + SolrUtil.escape(likeId)), InformationDO.class);
			solrOperations.deleteByKey(LikeDO.class, likeId);
			Log.info(">!>!>!>!> Index Remove likeId:" + SolrUtil.escape(likeId));

		}
	}

	/**
	 * レビューのいいね情報マップを返します。
	 * @param communityUserId 対象となるコミュニティユーザーID
	 * @param reviewIds レビューIDリスト
	 * @return レビューのいいね情報マップ
	 */
	@Override
	public Map<String, Boolean> loadReviewLikeMap(
			String communityUserId, List<String> reviewIds) {
		Map<String, Boolean> resultMap = new HashMap<String, Boolean>();
		if (StringUtils.isEmpty(communityUserId) || reviewIds == null || reviewIds.size() == 0) {
			return resultMap;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("targetType_s:");
		buffer.append(SolrUtil.escape(LikeTargetType.REVIEW.getCode()));
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

		for (LikeDO like : solrOperations.findByQuery(
				new SolrQuery(buffer.toString()).setRows(
						SolrConstants.QUERY_ROW_LIMIT), LikeDO.class,
						Path.includeProp("reviewId")).getDocuments()) {
			resultMap.put(like.getReview().getReviewId(), true);
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
	public Map<String, Boolean> loadQuestionAnswerLikeMap(
			String communityUserId, List<String> questionAnswerIds) {
		Map<String, Boolean> resultMap = new HashMap<String, Boolean>();
		if (StringUtils.isEmpty(communityUserId) || questionAnswerIds == null || questionAnswerIds.size() == 0) {
			return resultMap;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("targetType_s:");
		buffer.append(SolrUtil.escape(LikeTargetType.QUESTION_ANSWER.getCode()));
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

		for (LikeDO like : solrOperations.findByQuery(
				new SolrQuery(buffer.toString()).setRows(
						SolrConstants.QUERY_ROW_LIMIT), LikeDO.class,
						Path.includeProp("questionAnswerId")).getDocuments()) {
			resultMap.put(like.getQuestionAnswer().getQuestionAnswerId(), true);
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
	public Map<String, Boolean> loadImageLikeMap(
			String communityUserId, List<String> imageIds) {
		Map<String, Boolean> resultMap = new HashMap<String, Boolean>();
		if (StringUtils.isEmpty(communityUserId) || imageIds == null || imageIds.size() == 0) {
			return resultMap;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("targetType_s:");
		buffer.append(SolrUtil.escape(LikeTargetType.IMAGE.getCode()));
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

		for (LikeDO like : solrOperations.findByQuery(
				new SolrQuery(buffer.toString()).setRows(
						SolrConstants.QUERY_ROW_LIMIT), LikeDO.class,
						Path.includeProp("imageHeaderId")).getDocuments()) {
			resultMap.put(like.getImageHeader().getImageId(), true);
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
	public SearchResult<CommunityUserDO> findDistinctLikeUserByReview(
			String communityUserId, int limit, int offset) {
		//重複除去と投稿順の並び替えを同時に満たすため、solr ではサブクエリ
		//とdistinctをサポートしていないので、上限数を絞り、java 側で処理します。
		StringBuilder buffer = new StringBuilder();
		buffer.append("relationReviewOwnerId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND targetType_s:");
		buffer.append(SolrUtil.escape(LikeTargetType.REVIEW.getCode()));
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
	public SearchResult<CommunityUserDO> findDistinctLikeUserByQuestionAnswer(
			String communityUserId, int limit, int offset) {
		//重複除去と投稿順の並び替えを同時に満たすため、solr ではサブクエリ
		//とdistinctをサポートしていないので、上限数を絞り、java 側で処理します。
		StringBuilder buffer = new StringBuilder();
		buffer.append("relationQuestionAnswerOwnerId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND targetType_s:");
		buffer.append(SolrUtil.escape(LikeTargetType.QUESTION_ANSWER.getCode()));
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
	public SearchResult<CommunityUserDO> findDistinctLikeUserByImage(
			String communityUserId, int limit, int offset) {
		//重複除去と投稿順の並び替えを同時に満たすため、solr ではサブクエリ
		//とdistinctをサポートしていないので、上限数を絞り、java 側で処理します。
		StringBuilder buffer = new StringBuilder();
		buffer.append("relationImageOwnerId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND targetType_s:");
		buffer.append(SolrUtil.escape(LikeTargetType.IMAGE.getCode()));
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
		SearchResult<LikeDO> likes = new SearchResult<LikeDO>(
				solrOperations.findByQuery(
				new SolrQuery(adultHelper.toFilterQuery(query)).setRows(
								SolrConstants.QUERY_ROW_LIMIT).setStart(
										0).addSortField("postDate_dt", ORDER.desc),
										LikeDO.class, Path.includeProp("communityUserId")));
		if (adultHelper.isRequireCheckAdult()) {
			result.setHasAdult(
					adultHelper.hasAdult(
							query, ReviewDO.class, solrOperations));
		}
		if (likes.getNumFound() == 0) {
			return result;
		}
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(likes.getDocuments());
		List<String> communityUserIds = new ArrayList<String>();
		List<String> communityUserIdAll = new ArrayList<String>();
		String communityUserId = requestScopeDao.loadCommunityUserId();
		for (LikeDO review : likes.getDocuments()) {
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
	public SearchResult<LikeDO> findLikeByContentsId(
			LikeTargetType type,
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
		if (type.equals(LikeTargetType.REVIEW)) {
			buffer.append(" AND reviewId_s:");
		} else if (type.equals(LikeTargetType.QUESTION_ANSWER)) {
			buffer.append(" AND questionAnswerId_s:");
		} else if (type.equals(LikeTargetType.IMAGE)) {
			buffer.append(" AND imageHeaderId_s:");
		}
		buffer.append(SolrUtil.escape(contentsId));
		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());
		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(buffer.toString()));
		query.setRows(limit);
		query.setSortField("postDate_dt", ORDER.desc);
		SearchResult<LikeDO> searchResult = new SearchResult<LikeDO>(
				solrOperations.findByQuery(query, LikeDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId").depth(1)));
		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(
							buffer.toString(), LikeDO.class, solrOperations));
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
	protected String createLikeId(String communityUserId, String contentsId, LikeTargetType type) {
		return IdUtil.createIdByConcatIds(communityUserId, contentsId, type.getCode());
	}
}
