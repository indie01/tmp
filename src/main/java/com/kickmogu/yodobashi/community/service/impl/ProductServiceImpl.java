/**
 *
 */
package com.kickmogu.yodobashi.community.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.resource.cache.CacheStrategyType;
import com.kickmogu.yodobashi.community.resource.cache.MethodCache;
import com.kickmogu.yodobashi.community.resource.cache.TargetSystemType;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.dao.ActionHistoryDao;
import com.kickmogu.yodobashi.community.resource.dao.CommentDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.EventHistoryDao;
import com.kickmogu.yodobashi.community.resource.dao.ImageDao;
import com.kickmogu.yodobashi.community.resource.dao.InformationDao;
import com.kickmogu.yodobashi.community.resource.dao.LikeDao;
import com.kickmogu.yodobashi.community.resource.dao.OrderDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductMasterDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionAnswerDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.ReviewDao;
import com.kickmogu.yodobashi.community.resource.dao.VotingDao;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.VersionDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityOperationStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.EventHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.FillType;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;
import com.kickmogu.yodobashi.community.resource.domain.constants.LikeMessageType;
import com.kickmogu.yodobashi.community.resource.domain.constants.LikePrefixType;
import com.kickmogu.yodobashi.community.resource.domain.constants.MaintenanceStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.PurchaseProductSearchCondition;
import com.kickmogu.yodobashi.community.resource.domain.constants.VersionType;
import com.kickmogu.yodobashi.community.service.IndexService;
import com.kickmogu.yodobashi.community.service.ProductService;
import com.kickmogu.yodobashi.community.service.SocialMediaService;
import com.kickmogu.yodobashi.community.service.SystemMaintenanceService;
import com.kickmogu.yodobashi.community.service.vo.CommentSetVO;
import com.kickmogu.yodobashi.community.service.vo.NewsFeedVO;
import com.kickmogu.yodobashi.community.service.vo.ProductMasterSetVO;
import com.kickmogu.yodobashi.community.service.vo.ProductSetVO;
import com.kickmogu.yodobashi.community.service.vo.PurchaseProductSetVO;

/**
 * 商品サービスの実装です。
 * @author kamiike
 *
 */
@Service("productService")
public class ProductServiceImpl extends AbstractServiceImpl implements ProductService {
	
	private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

	/**
	 * コミュニティユーザー DAO です。
	 */
	@Autowired
	private CommunityUserDao communityUserDao;

	/**
	 * アクション履歴 DAO です。
	 */
	@Autowired
	private ActionHistoryDao actionHistoryDao;

	/**
	 * コメント DAO です。
	 */
	@Autowired
	private CommentDao commentDao;

	/**
	 * コミュニティユーザーフォロー DAO です。
	 */
	@Autowired
	private CommunityUserFollowDao communityUserFollowDao;

	/**
	 * 画像 DAO です。
	 */
	@Autowired
	private ImageDao imageDao;

	/**
	 * お知らせ情報 DAO です。
	 */
	@Autowired
	private InformationDao informationDao;

	/**
	 * イベント履歴 DAO です。
	 */
	@Autowired
	private EventHistoryDao eventHistoryDao;

	/**
	 * いいね DAO です。
	 */
	@Autowired
	private LikeDao likeDao;
	
	/**
	 * 参考になった DAO です。
	 */
	@Autowired
	private VotingDao votingDao;

	/**
	 * 注文 DAO です。
	 */
	@Autowired
	private OrderDao orderDao;

	/**
	 * レビュー DAO です。
	 */
	@Autowired
	private ReviewDao reviewDao;

	/**
	 * 商品フォロー DAO です。
	 */
	@Autowired
	private ProductFollowDao productFollowDao;

	/**
	 * 商品マスター DAO です。
	 */
	@Autowired
	private ProductMasterDao productMasterDao;

	/**
	 * 商品 DAO です。
	 */
	@Autowired @Qualifier("catalog")
	private ProductDao productDao;

	/**
	 * 質問 DAO です。
	 */
	@Autowired
	private QuestionDao questionDao;
	
	/**
	 * 質問回答 DAO です。
	 */
	@Autowired
	protected QuestionAnswerDao questionAnswerDao;

	/**
	 * 質問フォロー DAO です。
	 */
	@Autowired
	private QuestionFollowDao questionFollowDao;

	/**
	 * インデックスサービスです。
	 */
	@Autowired
	private IndexService indexService;

	/**
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	private RequestScopeDao requestScopeDao;

	/**
	 * socialメディア連携サービスです。
	 */
	@Autowired
	private SocialMediaService socialMediaService;

	@Autowired
	private SystemMaintenanceService systemMaintenanceService;
	
	@Autowired
	private ResourceConfig resourceConfig;
	
	/**
	 * 指定した商品のニュースフィードをアクション日時順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return ニュースフィード
	 */
	@Override
	public SearchResult<NewsFeedVO> findNewsFeedBySku(
			String sku, int limit, Date offsetTime, boolean previous) {
		return findNewsFeedBySku(sku, limit, offsetTime, previous, false);
	}
	@Override
	public SearchResult<NewsFeedVO> findNewsFeedBySku(
			String sku, int limit, Date offsetTime, boolean previous, boolean excludeProduct) {
		SearchResult<NewsFeedVO> result = new SearchResult<NewsFeedVO>();
		result.setSku(sku);
		SearchResult<ActionHistoryDO> searchResult
				= actionHistoryDao.findNewsFeedBySku(
						sku, limit, offsetTime, previous, excludeProduct);
		result.setHasAdult(searchResult.isHasAdult());
		result.setNumFound(searchResult.getNumFound());
		
		List<String> communityUserIds = new ArrayList<String>();
		List<String> reviewIds = new ArrayList<String>();
		List<String> questionIds = new ArrayList<String>();
		List<String> skus = new ArrayList<String>();
		List<String> questionAnswerIds = new ArrayList<String>();
		List<String> imageSetIds = new ArrayList<String>();
		List<String> imageIds = new ArrayList<String>();
		List<ReviewDO> reviewDOs = new ArrayList<ReviewDO>();
		List<String> likeCommunityUserIds = new ArrayList<String>();	// いいね50回されたユーザーリスト
		
		
		for (ActionHistoryDO actionHistory : searchResult.getDocuments()) {
			if (actionHistory.getReview() != null && actionHistory.getReview().getCommunityUser() != null) {
				reviewIds.add(actionHistory.getReview().getReviewId());
				reviewDOs.add(actionHistory.getReview());
				likeCommunityUserIds.add(actionHistory.getReview().getCommunityUser().getCommunityUserId());
			}
			if (actionHistory.getQuestion() != null && actionHistory.getQuestion().getCommunityUser() != null) {
				questionIds.add(actionHistory.getQuestion().getQuestionId());
				if( actionHistory.getQuestion().getProduct() != null ){
					skus.add(actionHistory.getQuestion().getProduct().getSku());
				}
			}
			if (actionHistory.getQuestionAnswer() != null && actionHistory.getQuestionAnswer().getCommunityUser() != null) {
				questionAnswerIds.add(actionHistory.getQuestionAnswer().getQuestionAnswerId());
				questionIds.add(actionHistory.getQuestionAnswer().getQuestion().getQuestionId());
			}
			if (actionHistory.getImageSetId() != null) {
				imageSetIds.add(actionHistory.getImageSetId());
			}
			if (actionHistory.getImageHeader() != null &&
					actionHistory.getImageHeader().getImageId() != null) {
				imageIds.add(actionHistory.getImageHeader().getImageId());
			}
			if (actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_PRODUCT_MASTER_RANK_CHANGE) &&
					actionHistory.getCommunityUser() != null) {
				communityUserIds.add(actionHistory.getCommunityUser().getCommunityUserId());
			}
			if (actionHistory.getActionHistoryType() == ActionHistoryType.LIKE_IMAGE_50) {
				likeCommunityUserIds.add(actionHistory.getImageHeader().getOwnerCommunityUserId());
				// 画像にいいね50回の場合はImageSetIdがセットされていないのでここで設定する
				actionHistory.setImageSetId(actionHistory.getImageHeader().getImageId());
			}
		}
		
		// レビューの場合ユーザー名も必要なのでここでCommunityUserDOを取得しなおす
		Map<String,CommunityUserDO> likeCommunityUserMap = communityUserDao.loadCommunityUserMap(likeCommunityUserIds);
		if (likeCommunityUserMap == null) {
			likeCommunityUserMap = new HashMap<String,CommunityUserDO>();
		}
		
		Map<String, Long> reviewCommentCountMap = new HashMap<String, Long>();
		Map<String, Long> questionCommentCountMap= new HashMap<String, Long>();
		Map<String, Long> questionAnswerCommentCountMap= new HashMap<String, Long>();
		Map<String, Long> imageSetCommentCountMap = new HashMap<String, Long>();

		Map<String, List<ImageHeaderDO>> reviewAllImageMap = new HashMap<String, List<ImageHeaderDO>>();
		Map<String, List<ImageHeaderDO>> questionAllImageMap = new HashMap<String, List<ImageHeaderDO>>();
		Map<String, List<ImageHeaderDO>> questionAnswerAllImageMap = new HashMap<String, List<ImageHeaderDO>>();
		Map<String, List<ImageHeaderDO>> imageSetAllImageMap = new HashMap<String, List<ImageHeaderDO>>();

		Map<String, Long> reviewLikeCountMap = new HashMap<String, Long>();
		Map<String, Long> questionAnswerLikeCountMap = new HashMap<String, Long>();
		Map<String, Long> imageSetLikeCountMap = new HashMap<String, Long>();
		Map<String, Long> imageLikeCountMap = new HashMap<String, Long>();
		
		Map<String, Long[]> reviewVotingCountMap = new HashMap<String, Long[]>();
		Map<String, Long[]> questionAnswerVotingCountMap = new HashMap<String, Long[]>();
		Map<String, Long[]> imageSetVotingCountMap = new HashMap<String, Long[]>();
		Map<String, Long[]> imageCountVotingMap = new HashMap<String, Long[]>();
		
		Map<String, Boolean> likeReviewMap = new HashMap<String, Boolean>();
		Map<String, Boolean> likeAnswerMap = new HashMap<String, Boolean>();
		Map<String, Boolean> userFollowMap = new HashMap<String, Boolean>();
		Map<String, Boolean> questionFollowMap = new HashMap<String, Boolean>();
		Map<String, Boolean> hasAnswerMap = new HashMap<String, Boolean>();
		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();
		if( StringUtils.isNotEmpty(loginCommunityUserId)){
			// いいね済みかどうか
			likeReviewMap = likeDao.loadReviewLikeMap(loginCommunityUserId, reviewIds);
			// いいね済みかどうか
			likeAnswerMap = likeDao.loadQuestionAnswerLikeMap(loginCommunityUserId, questionAnswerIds);
			// ユーザーフォロー済みかどうか
			userFollowMap = communityUserFollowDao.loadCommunityUserFollowMap(loginCommunityUserId, communityUserIds);
			// 質問フォロー済みかどうか
			questionFollowMap = questionFollowDao.loadQuestionFollowMap(loginCommunityUserId, questionIds);
			//質問回答済みかどうか
			hasAnswerMap = hasQuestionAnswer(loginCommunityUserId, questionIds);
		}
		
		commentDao.loadContentsCommentCountMap(
				reviewIds, 
				questionIds, 
				questionAnswerIds, 
				imageSetIds,
				reviewCommentCountMap, 
				questionCommentCountMap, 
				questionAnswerCommentCountMap, 
				imageSetCommentCountMap);

		
		likeDao.loadContentsLikeCountMap(
				reviewIds, 
				null, 
				questionAnswerIds,
				imageSetIds, 
				imageIds, 
				reviewLikeCountMap, 
				null, 
				questionAnswerLikeCountMap, 
				imageSetLikeCountMap, 
				imageLikeCountMap);
		
		votingDao.loadContentsVotingCountMap(
				reviewIds, 
				null, 
				questionAnswerIds,
				imageSetIds, 
				imageIds,
				reviewVotingCountMap, 
				null, 
				questionAnswerVotingCountMap, 
				imageSetVotingCountMap, 
				imageCountVotingMap);
		
		imageDao.loadAllImageMapByContentsIds(
				reviewIds,
				questionIds,
				questionAnswerIds,
				imageSetIds,
				reviewAllImageMap,
				questionAllImageMap,
				questionAnswerAllImageMap,
				imageSetAllImageMap);
		
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(searchResult.getDocuments());
		// 同じ商品・ユーザーで、他のレビュー数
		Map<String, Long> sameProductReviewCountMap = reviewDao.loadSameProductReviewCountMap(reviewDOs);
		// 質問回答者数
		Map<String, Long> answerCountMap = questionAnswerDao.loadQuestionAnswerCountMapByQuestionId(questionIds);
		
		for (ActionHistoryDO actionHistory : searchResult.getDocuments()) {
			NewsFeedVO vo = new NewsFeedVO();
			vo.setActionHistory(actionHistory);
			if (ActionHistoryType.USER_REVIEW.equals(actionHistory.getActionHistoryType())
					|| ActionHistoryType.LIKE_REVIEW_50.equals(actionHistory.getActionHistoryType())) {
				
				// ユーザー名も必要になるのでここで設定
				String reivewCommunityUserId = actionHistory.getReview().getCommunityUser().getCommunityUserId();
				if (likeCommunityUserMap.containsKey(reivewCommunityUserId)) {
					actionHistory.getReview().setCommunityUser(likeCommunityUserMap.get(reivewCommunityUserId));
				}
				
				Long reviewCommentCount = reviewCommentCountMap.get(actionHistory.getReview().getReviewId());
				Long reviewLikeCount = reviewLikeCountMap.get(actionHistory.getReview().getReviewId());
				Long[] reviewVotingCount = reviewVotingCountMap.get(actionHistory.getReview().getReviewId());
				if (reviewCommentCount != null) {
					vo.setCommentCount(reviewCommentCount);
				}
				if (reviewLikeCount != null) {
					vo.setLikeCount(reviewLikeCount);
				}
				if( reviewVotingCount != null ) {
					vo.setVotingCountYes(reviewVotingCount[0]);
					vo.setVotingCountNo(reviewVotingCount[1]);
				}
				if (reviewAllImageMap.containsKey(actionHistory.getReview().getReviewId())) {
					vo.setImages(reviewAllImageMap.get(actionHistory.getReview().getReviewId()));
				}
				if( sameProductReviewCountMap.containsKey(actionHistory.getReview().getReviewId()))
					vo.setOtherReviewCount(sameProductReviewCountMap.get(actionHistory.getReview().getReviewId()));
				if (likeReviewMap.containsKey(actionHistory.getReview().getReviewId()))
					vo.setLikeFlg(likeReviewMap.get(actionHistory.getReview().getReviewId()));
				// TODO あとで効率よく取れるように変更する。
				SearchResult<LikeDO> likes = findLikeByReviewId(
						actionHistory.getReview().getReviewId(),
						loginCommunityUserId,
						resourceConfig.evaluationAreaLikeReadLimit);
				
				long like_count = vo.getLikeCount() - (vo.isLikeFlg()? 1 : 0);
				if( vo.isLikeFlg() ){
					// getLikeCount()は、自分を含んだ数字であることが前提。
					if( 0 == likes.getDocuments().size() ){
						vo.setLikePrefixType(LikePrefixType.ONLYONE.getCode());
					}else{
						vo.setLikePrefixType(LikePrefixType.MULTIPLE.getCode());
					}
				}else{
					vo.setLikePrefixType(LikePrefixType.NONE.getCode());
				}
				
				List<String> communityUserNames = new ArrayList<String>();
				if( like_count == 0 ){
					vo.setLikeMessageType(LikeMessageType.NONE.getCode());
				}else{
					if( like_count <= 3 ){
						vo.setLikeMessageType(LikeMessageType.UPTO3.getCode());
						for( LikeDO like : likes.getDocuments()){
							communityUserNames.add(like.getCommunityUser().getCommunityName());
						}
					}else{
						vo.setLikeMessageType(LikeMessageType.MULTIPLE.getCode());
					}
				}
				vo.setLikeUserNames(communityUserNames);
				
				// TODO あとで効率よく取れるように変更する。
				SearchResult<CommentSetVO> resultComment = findReviewCommentByReviewId(
						actionHistory.getReview().getReviewId(),
						null,
						resourceConfig.commentInitReadLimit,
						null,
						false);
				if( !resultComment.getDocuments().isEmpty() )
					Collections.reverse(resultComment.getDocuments());
				vo.setCommentViewRemainingCount(vo.getCommentCount() - resultComment.getDocuments().size());
				vo.setComments(resultComment);
			} else if (ActionHistoryType.USER_QUESTION.equals(actionHistory.getActionHistoryType())) {
				Long questionCommentCount = questionCommentCountMap.get(actionHistory.getQuestion().getQuestionId());
				if (questionCommentCount != null) {
					vo.setCommentCount(questionCommentCount);
				}
				if (questionAllImageMap.containsKey(actionHistory.getQuestion().getQuestionId())) {
					vo.setImages(questionAllImageMap.get(actionHistory.getQuestion().getQuestionId()));
				}
				if( hasAnswerMap.containsKey(actionHistory.getQuestion().getQuestionId())){
					vo.setAnswerFlg(hasAnswerMap.get(actionHistory.getQuestion().getQuestionId()));
				}
			} else if (ActionHistoryType.USER_ANSWER.equals(actionHistory.getActionHistoryType())
					|| ActionHistoryType.LIKE_ANSWER_50.equals(actionHistory.getActionHistoryType())) {
				Long questionAnswerCommentCount = questionAnswerCommentCountMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId());
				Long questionAnswerLikeCount = questionAnswerLikeCountMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId());
				Long[] questionAnswerVotingCount = questionAnswerVotingCountMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId());
				
				if (questionAnswerCommentCount != null) {
					vo.setCommentCount(questionAnswerCommentCount);
				}
				if (questionAnswerLikeCount != null) {
					vo.setLikeCount(questionAnswerLikeCount);
				}
				if( questionAnswerVotingCount != null ) {
					vo.setVotingCountYes(questionAnswerVotingCount[0]);
					vo.setVotingCountNo(questionAnswerVotingCount[1]);
				}
				if (questionAnswerAllImageMap.containsKey(actionHistory.getQuestionAnswer().getQuestionAnswerId())) {
					vo.setImages(questionAnswerAllImageMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId()));
				}
				if (questionAllImageMap.containsKey(actionHistory.getQuestionAnswer().getQuestion().getQuestionId())) {
					vo.setSubContentImages(questionAllImageMap.get(actionHistory.getQuestionAnswer().getQuestion().getQuestionId()));
				}
				
				///// TODO リファクタしたほうが良い。デザインのための処理　start/////
				String questionId = actionHistory.getQuestion().getQuestionId();
				
				if (answerCountMap.containsKey(questionId)) {
					vo.setAnswerCount(answerCountMap.get(questionId));
				}
				
				// 質問
				QuestionDO question = questionDao.loadQuestionFromIndex(questionId, true);
				actionHistory.setQuestion(question);
				
				// 質問&回答
				QuestionAnswerDO questionAnswerDO = questionAnswerDao.loadQuestionAnswerFromIndex(actionHistory.getQuestionAnswer().getQuestionAnswerId());
				actionHistory.setQuestionAnswer(questionAnswerDO);
				
				if (likeAnswerMap.containsKey(actionHistory.getQuestionAnswer().getQuestionAnswerId()))
					vo.setLikeFlg(likeAnswerMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId()));
				
				// TODO あとで効率よく取れるように変更する。
				SearchResult<LikeDO> likes = findLikeByQuestionAnswerId(
						actionHistory.getQuestionAnswer().getQuestionAnswerId(),
						loginCommunityUserId,
						resourceConfig.evaluationAreaLikeReadLimit);
				
				long like_count = vo.getLikeCount() - (vo.isLikeFlg()? 1 : 0);
				if( vo.isLikeFlg() ){
					// getLikeCount()は、自分を含んだ数字であることが前提。
					if( 0 == likes.getDocuments().size() ){
						vo.setLikePrefixType(LikePrefixType.ONLYONE.getCode());
					}else{
						vo.setLikePrefixType(LikePrefixType.MULTIPLE.getCode());
					}
				}else{
					vo.setLikePrefixType(LikePrefixType.NONE.getCode());
				}
				
				List<String> communityUserNames = new ArrayList<String>();
				if( like_count == 0 ){
					vo.setLikeMessageType(LikeMessageType.NONE.getCode());
				}else{
					if( like_count <= 3 ){
						vo.setLikeMessageType(LikeMessageType.UPTO3.getCode());
						for( LikeDO like : likes.getDocuments()){
							communityUserNames.add(like.getCommunityUser().getCommunityName());
						}
					}else{
						vo.setLikeMessageType(LikeMessageType.MULTIPLE.getCode());
					}
				}
				vo.setLikeUserNames(communityUserNames);
				
				// TODO あとで効率よく取れるように変更する。
				SearchResult<CommentSetVO> resultComment = findQuestionAnswerCommentByQuestionAnswerId(
						actionHistory.getQuestionAnswer().getQuestionAnswerId(),
						null,
						resourceConfig.commentInitReadLimit,
						null,
						false);
				if( !resultComment.getDocuments().isEmpty() )
					Collections.reverse(resultComment.getDocuments());
				vo.setCommentViewRemainingCount(vo.getCommentCount() - resultComment.getDocuments().size());
				vo.setComments(resultComment);
			} else if (ActionHistoryType.USER_IMAGE.equals(actionHistory.getActionHistoryType())
					|| ActionHistoryType.LIKE_IMAGE_50.equals(actionHistory.getActionHistoryType())) {
				
				Long imageSetCommentCount = imageSetCommentCountMap.get(actionHistory.getImageSetId());
				if (imageSetCommentCount != null) {
					vo.setCommentCount(imageSetCommentCount);
				}
				if (ActionHistoryType.USER_IMAGE.equals(actionHistory.getActionHistoryType())) {
					Long imageSetLikeCount = imageSetLikeCountMap.get(actionHistory.getImageSetId());
					Long[] imageSetVotingCount = imageSetVotingCountMap.get(actionHistory.getImageSetId());
					
					if (imageSetLikeCount != null) {
						vo.setLikeCount(imageSetLikeCount);
					}
					if( imageSetVotingCount != null ) {
						vo.setVotingCountYes(imageSetVotingCount[0]);
						vo.setVotingCountNo(imageSetVotingCount[1]);
					}
				} else {
					// 画像にいいねが50回
					
					// もうちょっと細かい情報が必要になるのでここで取得しなおす
					ImageHeaderDO imageHeaderDO = imageDao.loadImageHeader(actionHistory.getImageHeader().getImageId());
					// ユーザー名も必要になるのでここで設定
					String imageCommunityUserId = actionHistory.getImageHeader().getOwnerCommunityUser().getCommunityUserId();
					if (likeCommunityUserMap.containsKey(imageCommunityUserId)) {
						imageHeaderDO.setOwnerCommunityUser(likeCommunityUserMap.get(imageCommunityUserId));
					}
					actionHistory.setImageHeader(imageHeaderDO);
					
					Long imageLikeCount = imageLikeCountMap.get(actionHistory.getImageHeader().getImageId());
					Long[] imageVotingCount = imageCountVotingMap.get(actionHistory.getImageSetId());
					
					if (imageLikeCount != null) {
						vo.setLikeCount(imageLikeCount);
					}
					if( imageVotingCount != null ) {
						vo.setVotingCountYes(imageVotingCount[0]);
						vo.setVotingCountNo(imageVotingCount[1]);
					}
					vo.setImages(Arrays.asList(new ImageHeaderDO[]{actionHistory.getImageHeader()}));
				}
				if (imageSetAllImageMap.containsKey(actionHistory.getImageSetId())) {
					vo.setImageHeaders(imageSetAllImageMap.get(actionHistory.getImageSetId()));
				}
			} else if (actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_PRODUCT_MASTER_RANK_CHANGE)) {
				if (userFollowMap.containsKey(actionHistory.getCommunityUser().getCommunityUserId())) {
					vo.setFollowingFlg(userFollowMap.get(actionHistory.getCommunityUser().getCommunityUserId()));
				}
			}
			if (questionFollowMap != null
					&& questionFollowMap.size() > 0
					&& actionHistory.getQuestion() != null
					&& questionFollowMap.containsKey(actionHistory.getQuestion().getQuestionId())) {
				vo.setFollowingFlg(questionFollowMap.get(actionHistory.getQuestion().getQuestionId()));
			}
			
			result.updateFirstAndLast(vo);
			if (actionHistory.isStop(loginCommunityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				continue;
			}
			
			if (actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_REVIEW) ||
					actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_IMAGE) ||
					actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_QUESTION) ||
					actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_ANSWER)) {
				// コンテンツの購入情報を取得する。
				PurchaseProductDO purchaseProductDO = orderDao.loadPurchaseProductBySku(
						actionHistory.getCommunityUser().getCommunityUserId(),
						sku,
						Path.DEFAULT,
						false);
				if( purchaseProductDO != null && !purchaseProductDO.isDeleted() ){
					vo.setPurchaseProduct( purchaseProductDO );
				}
				// ログインユーザーの購入情報を取得する。
				if( StringUtils.isNotEmpty(loginCommunityUserId)){
					purchaseProductDO = orderDao.loadPurchaseProductBySku(
							loginCommunityUserId,
							sku,
							Path.DEFAULT,
							false);
					if( purchaseProductDO != null && !purchaseProductDO.isDeleted() ){
						vo.setLoginUserPurchaseProduct(purchaseProductDO);
					}
				}
			}
			
			result.getDocuments().add(vo);
		}
		
		return result;
	}
	
	@Override
	public SearchResult<NewsFeedVO> findNewsFeedBySkuForWs(
			String sku, int limit, Date offsetTime, boolean previous, boolean excludeProduct) {
		SearchResult<NewsFeedVO> result = new SearchResult<NewsFeedVO>();
		SearchResult<ActionHistoryDO> searchResult
				= actionHistoryDao.findNewsFeedBySku(
						sku, limit, offsetTime, previous, excludeProduct);
		result.setHasAdult(searchResult.isHasAdult());
		result.setNumFound(searchResult.getNumFound());
		String communityUserId = requestScopeDao.loadCommunityUserId();
		List<String> communityUserIds = new ArrayList<String>();
		List<String> reviewIds = new ArrayList<String>();
		List<String> questionIds = new ArrayList<String>();
		List<String> questionAnswerIds = new ArrayList<String>();
		List<String> imageSetIds = new ArrayList<String>();
		List<String> imageIds = new ArrayList<String>();

		for (ActionHistoryDO actionHistory : searchResult.getDocuments()) {
			if (actionHistory.getReview() != null && actionHistory.getReview().getCommunityUser() != null) {
				reviewIds.add(actionHistory.getReview().getReviewId());
			}
			if (actionHistory.getQuestion() != null && actionHistory.getQuestion().getCommunityUser() != null) {
				questionIds.add(actionHistory.getQuestion().getQuestionId());
			}
			if (actionHistory.getQuestionAnswer() != null && actionHistory.getQuestionAnswer().getCommunityUser() != null) {
				questionAnswerIds.add(actionHistory.getQuestionAnswer().getQuestionAnswerId());
			}
			if (actionHistory.getImageSetId() != null) {
				imageSetIds.add(actionHistory.getImageSetId());
			}
			if (actionHistory.getImageHeader() != null &&
					actionHistory.getImageHeader().getImageId() != null) {
				imageIds.add(actionHistory.getImageHeader().getImageId());
			}
			if (actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_PRODUCT_MASTER_RANK_CHANGE) && 
					actionHistory.getCommunityUser() != null) {
				communityUserIds.add(actionHistory.getCommunityUser().getCommunityUserId());
			}
		}
		
		Map<String, Long> reviewCommentCountMap = new HashMap<String, Long>();
		Map<String, Long> questionCommentCountMap= new HashMap<String, Long>();
		Map<String, Long> questionAnswerCommentCountMap= new HashMap<String, Long>();
		Map<String, Long> imageSetCommentCountMap = new HashMap<String, Long>();

		Map<String, List<ImageHeaderDO>> reviewAllImageMap = new HashMap<String, List<ImageHeaderDO>>();
		Map<String, List<ImageHeaderDO>> questionAllImageMap = new HashMap<String, List<ImageHeaderDO>>();
		Map<String, List<ImageHeaderDO>> questionAnswerAllImageMap = new HashMap<String, List<ImageHeaderDO>>();
		Map<String, List<ImageHeaderDO>> imageSetAllImageMap = new HashMap<String, List<ImageHeaderDO>>();

		Map<String, Long> reviewLikeCountMap = new HashMap<String, Long>();
		Map<String, Long> questionAnswerLikeCountMap = new HashMap<String, Long>();
		Map<String, Long> imageSetLikeCountMap = new HashMap<String, Long>();
		Map<String, Long> imageLikeCountMap = new HashMap<String, Long>();
		
		Map<String, Long[]> reviewVotingCountMap = new HashMap<String, Long[]>();
		Map<String, Long[]> questionAnswerVotingCountMap = new HashMap<String, Long[]>();
		Map<String, Long[]> imageSetVotingCountMap = new HashMap<String, Long[]>();
		Map<String, Long[]> imageCountVotingMap = new HashMap<String, Long[]>();
		
		commentDao.loadContentsCommentCountMap(
				reviewIds, 
				questionIds, 
				questionAnswerIds, 
				imageSetIds,
				reviewCommentCountMap, 
				questionCommentCountMap, 
				questionAnswerCommentCountMap, 
				imageSetCommentCountMap);

		
		likeDao.loadContentsLikeCountMap(
				reviewIds, 
				null, 
				questionAnswerIds,
				imageSetIds, 
				imageIds, 
				reviewLikeCountMap, 
				null, 
				questionAnswerLikeCountMap, 
				imageSetLikeCountMap, 
				imageLikeCountMap);
		
		votingDao.loadContentsVotingCountMap(
				reviewIds, 
				null, 
				questionAnswerIds,
				imageSetIds, 
				imageIds,
				reviewVotingCountMap, 
				null, 
				questionAnswerVotingCountMap, 
				imageSetVotingCountMap, 
				imageCountVotingMap);
		
		imageDao.loadAllImageMapByContentsIds(
				reviewIds,
				questionIds,
				questionAnswerIds,
				imageSetIds,
				reviewAllImageMap,
				questionAllImageMap,
				questionAnswerAllImageMap,
				imageSetAllImageMap);

		Map<String, Boolean> userFollowMap = null;
		Map<String, Boolean> questionFollowMap = null;
		if (communityUserId != null) {
			userFollowMap = communityUserFollowDao.loadCommunityUserFollowMap(communityUserId, communityUserIds);
			questionFollowMap = questionFollowDao.loadQuestionFollowMap(communityUserId, questionIds);
		}
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(searchResult.getDocuments());
		for (ActionHistoryDO actionHistory : searchResult.getDocuments()) {
			NewsFeedVO vo = new NewsFeedVO();
			vo.setActionHistory(actionHistory);
			if (ActionHistoryType.USER_REVIEW.equals(actionHistory.getActionHistoryType())
					|| ActionHistoryType.LIKE_REVIEW_50.equals(actionHistory.getActionHistoryType())) {
				Long reviewCommentCount = reviewCommentCountMap.get(actionHistory.getReview().getReviewId());
				Long reviewLikeCount = reviewLikeCountMap.get(actionHistory.getReview().getReviewId());
				Long[] reviewVotingCount = reviewVotingCountMap.get(actionHistory.getReview().getReviewId());
				if (reviewCommentCount != null) {
					vo.setCommentCount(reviewCommentCount);
				}
				if (reviewLikeCount != null) {
					vo.setLikeCount(reviewLikeCount);
				}
				if( reviewVotingCount != null ) {
					vo.setVotingCountYes(reviewVotingCount[0]);
					vo.setVotingCountNo(reviewVotingCount[1]);
				}
				if (reviewAllImageMap.containsKey(actionHistory.getReview().getReviewId())) {
					vo.setImages(reviewAllImageMap.get(actionHistory.getReview().getReviewId()));
				}
			} else if (ActionHistoryType.USER_QUESTION.equals(actionHistory.getActionHistoryType())) {
				Long questionCommentCount = questionCommentCountMap.get(actionHistory.getQuestion().getQuestionId());
				if (questionCommentCount != null) {
					vo.setCommentCount(questionCommentCount);
				}
				if (questionAllImageMap.containsKey(actionHistory.getQuestion().getQuestionId())) {
					vo.setImages(questionAllImageMap.get(actionHistory.getQuestion().getQuestionId()));
				}
			} else if (ActionHistoryType.USER_ANSWER.equals(actionHistory.getActionHistoryType())
					|| ActionHistoryType.LIKE_ANSWER_50.equals(actionHistory.getActionHistoryType())) {
				Long questionAnswerCommentCount = questionAnswerCommentCountMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId());
				Long questionAnswerLikeCount = questionAnswerLikeCountMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId());
				Long[] questionAnswerVotingCount = questionAnswerVotingCountMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId());
				
				if (questionAnswerCommentCount != null) {
					vo.setCommentCount(questionAnswerCommentCount);
				}
				if (questionAnswerLikeCount != null) {
					vo.setLikeCount(questionAnswerLikeCount);
				}
				if( questionAnswerVotingCount != null ) {
					vo.setVotingCountYes(questionAnswerVotingCount[0]);
					vo.setVotingCountNo(questionAnswerVotingCount[1]);
				}
				if (questionAnswerAllImageMap.containsKey(actionHistory.getQuestionAnswer().getQuestionAnswerId())) {
					vo.setImages(questionAnswerAllImageMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId()));
				}
			} else if (ActionHistoryType.USER_IMAGE.equals(actionHistory.getActionHistoryType())
					|| ActionHistoryType.LIKE_IMAGE_50.equals(actionHistory.getActionHistoryType())) {
				Long imageSetCommentCount = imageSetCommentCountMap.get(actionHistory.getImageSetId());
				if (imageSetCommentCount != null) {
					vo.setCommentCount(imageSetCommentCount);
				}
				if (ActionHistoryType.USER_IMAGE.equals(actionHistory.getActionHistoryType())) {
					Long imageSetLikeCount = imageSetLikeCountMap.get(actionHistory.getImageSetId());
					Long[] imageSetVotingCount = imageSetVotingCountMap.get(actionHistory.getImageSetId());
					
					if (imageSetLikeCount != null) {
						vo.setLikeCount(imageSetLikeCount);
					}
					if( imageSetVotingCount != null ) {
						vo.setVotingCountYes(imageSetVotingCount[0]);
						vo.setVotingCountNo(imageSetVotingCount[1]);
					}
				} else {
					Long imageLikeCount = imageLikeCountMap.get(actionHistory.getImageHeader().getImageId());
					Long[] imageVotingCount = imageCountVotingMap.get(actionHistory.getImageSetId());
					
					if (imageLikeCount != null) {
						vo.setLikeCount(imageLikeCount);
					}
					if( imageVotingCount != null ) {
						vo.setVotingCountYes(imageVotingCount[0]);
						vo.setVotingCountNo(imageVotingCount[1]);
					}
				}
				if (imageSetAllImageMap.containsKey(actionHistory.getImageSetId())) {
					vo.setImageHeaders(imageSetAllImageMap.get(actionHistory.getImageSetId()));
				}
			} else if (actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_PRODUCT_MASTER_RANK_CHANGE)) {
				if (userFollowMap != null
						&& userFollowMap.size() > 0
						&& userFollowMap.containsKey(
						actionHistory.getCommunityUser().getCommunityUserId())) {
					vo.setFollowingFlg(userFollowMap.get(
							actionHistory.getCommunityUser().getCommunityUserId()));
				}
			}
			if (questionFollowMap != null
					&& questionFollowMap.size() > 0
					&& actionHistory.getQuestion() != null
					&& questionFollowMap.containsKey(
					actionHistory.getQuestion().getQuestionId())) {
				vo.setFollowingFlg(questionFollowMap.get(
						actionHistory.getQuestion().getQuestionId()));
			}

			result.updateFirstAndLast(vo);
			if (actionHistory.isStop(communityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				continue;
			}
			
			if (actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_REVIEW) ||
					actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_IMAGE) ||
					actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_QUESTION) ||
					actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_ANSWER)) {
				// コンテンツの購入情報を取得する。
				PurchaseProductDO purchaseProductDO = orderDao.loadPurchaseProductBySku(
						actionHistory.getCommunityUser().getCommunityUserId(),
						sku,
						Path.DEFAULT,
						false);
				if( purchaseProductDO != null && !purchaseProductDO.isDeleted() ){
					vo.setPurchaseProduct( purchaseProductDO );
				}
			}
			
			result.getDocuments().add(vo);
		}
		
		return result;
	}
	
	@Override
	public SearchResult<NewsFeedVO> findNewsFeedBySkus(String sku,
			List<String> skus, int limit, Date offsetTime, boolean previous) {
		return findNewsFeedBySkus(sku, skus, limit, offsetTime, previous, false);
	}
	@Override
	public SearchResult<NewsFeedVO> findNewsFeedBySkus(String sku,
			List<String> skus, int limit, Date offsetTime, boolean previous, boolean excludeProduct) {
		SearchResult<NewsFeedVO> result = new SearchResult<NewsFeedVO>();
		result.setSku(sku);
		List<String> inputSkus = new ArrayList<String>();
		inputSkus.add(sku);
		inputSkus.addAll(skus);
		SearchResult<ActionHistoryDO> searchResult
				= actionHistoryDao.findNewsFeedBySkus(
						inputSkus, limit, offsetTime, previous, excludeProduct);
		result.setHasAdult(searchResult.isHasAdult());
		result.setNumFound(searchResult.getNumFound());
		
		List<String> communityUserIds = new ArrayList<String>();
		List<String> reviewIds = new ArrayList<String>();
		List<String> questionIds = new ArrayList<String>();
		List<String> skuList = new ArrayList<String>();
		List<String> questionAnswerIds = new ArrayList<String>();
		List<String> imageSetIds = new ArrayList<String>();
		List<String> imageIds = new ArrayList<String>();
		List<ReviewDO> reviewDOs = new ArrayList<ReviewDO>();
		List<String> likeCommunityUserIds = new ArrayList<String>();	// いいね50回されたユーザーリスト
		
		for (ActionHistoryDO actionHistory : searchResult.getDocuments()) {
			if (actionHistory.getReview() != null && actionHistory.getReview().getCommunityUser() != null) {
				reviewIds.add(actionHistory.getReview().getReviewId());
				reviewDOs.add(actionHistory.getReview());
				likeCommunityUserIds.add(actionHistory.getReview().getCommunityUser().getCommunityUserId());
			}
			if (actionHistory.getQuestion() != null && actionHistory.getQuestion().getCommunityUser() != null) {
				questionIds.add(actionHistory.getQuestion().getQuestionId());
				if( actionHistory.getQuestion().getProduct() != null ){
					skuList.add(actionHistory.getQuestion().getProduct().getSku());
				}
			}
			if (actionHistory.getQuestionAnswer() != null && actionHistory.getQuestionAnswer().getCommunityUser() != null) {
				questionAnswerIds.add(actionHistory.getQuestionAnswer().getQuestionAnswerId());
			}
			if (actionHistory.getImageSetId() != null) {
				imageSetIds.add(actionHistory.getImageSetId());
			}
			if (actionHistory.getImageHeader() != null &&
					actionHistory.getImageHeader().getImageId() != null) {
				imageIds.add(actionHistory.getImageHeader().getImageId());
			}
			if (actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_PRODUCT_MASTER_RANK_CHANGE) &&
					actionHistory.getCommunityUser() != null) {
				communityUserIds.add(actionHistory.getCommunityUser().getCommunityUserId());
			}
			if (actionHistory.getActionHistoryType() == ActionHistoryType.LIKE_IMAGE_50) {
				likeCommunityUserIds.add(actionHistory.getImageHeader().getOwnerCommunityUserId());
				// 画像にいいね50回の場合はImageSetIdがセットされていないのでここで設定する
				actionHistory.setImageSetId(actionHistory.getImageHeader().getImageId());
			}
		}
		// レビューの場合ユーザー名も必要なのでここでCommunityUserDOを取得しなおす
		Map<String,CommunityUserDO> likeCommunityUserMap = communityUserDao.loadCommunityUserMap(likeCommunityUserIds);
		if (likeCommunityUserMap == null) {
			likeCommunityUserMap = new HashMap<String,CommunityUserDO>();
		}
		
		Map<String, Long> reviewCommentCountMap = new HashMap<String, Long>();
		Map<String, Long> questionCommentCountMap= new HashMap<String, Long>();
		Map<String, Long> questionAnswerCommentCountMap= new HashMap<String, Long>();
		Map<String, Long> imageSetCommentCountMap = new HashMap<String, Long>();
		
		Map<String, List<ImageHeaderDO>> reviewAllImageMap = new HashMap<String, List<ImageHeaderDO>>();
		Map<String, List<ImageHeaderDO>> questionAllImageMap = new HashMap<String, List<ImageHeaderDO>>();
		Map<String, List<ImageHeaderDO>> questionAnswerAllImageMap = new HashMap<String, List<ImageHeaderDO>>();
		Map<String, List<ImageHeaderDO>> imageSetAllImageMap = new HashMap<String, List<ImageHeaderDO>>();

		Map<String, Long> reviewLikeCountMap = new HashMap<String, Long>();
		Map<String, Long> questionAnswerLikeCountMap = new HashMap<String, Long>();
		Map<String, Long> imageSetLikeCountMap = new HashMap<String, Long>();
		Map<String, Long> imageLikeCountMap = new HashMap<String, Long>();
		
		Map<String, Long[]> reviewVotingCountMap = new HashMap<String, Long[]>();
		Map<String, Long[]> questionAnswerVotingCountMap = new HashMap<String, Long[]>();
		Map<String, Long[]> imageSetVotingCountMap = new HashMap<String, Long[]>();
		Map<String, Long[]> imageCountVotingMap = new HashMap<String, Long[]>();
		
		Map<String, Boolean> likeReviewMap = new HashMap<String, Boolean>();
		Map<String, Boolean> likeAnswerMap = new HashMap<String, Boolean>();
		Map<String, Boolean> userFollowMap = new HashMap<String, Boolean>();
		Map<String, Boolean> questionFollowMap = new HashMap<String, Boolean>();
		Map<String, Boolean> hasAnswerMap = new HashMap<String, Boolean>();
		
		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();
		if( StringUtils.isNotEmpty(loginCommunityUserId)){
			// いいね済みかどうか
			likeReviewMap = likeDao.loadReviewLikeMap(loginCommunityUserId, reviewIds);
			// いいね済みかどうか
			likeAnswerMap = likeDao.loadQuestionAnswerLikeMap(loginCommunityUserId, questionAnswerIds);
			// ユーザーフォロー済みかどうか
			userFollowMap = communityUserFollowDao.loadCommunityUserFollowMap(loginCommunityUserId, communityUserIds);
			// 質問フォロー済みかどうか
			questionFollowMap = questionFollowDao.loadQuestionFollowMap(loginCommunityUserId, questionIds);
			//質問回答済みかどうか
			hasAnswerMap = hasQuestionAnswer(loginCommunityUserId, questionIds);
		}
		
		commentDao.loadContentsCommentCountMap(
				reviewIds, 
				questionIds, 
				questionAnswerIds, 
				imageSetIds,
				reviewCommentCountMap, 
				questionCommentCountMap, 
				questionAnswerCommentCountMap, 
				imageSetCommentCountMap);
		
		likeDao.loadContentsLikeCountMap(
				reviewIds, 
				null, 
				questionAnswerIds,
				imageSetIds, 
				imageIds, 
				reviewLikeCountMap, 
				null, 
				questionAnswerLikeCountMap, 
				imageSetLikeCountMap, 
				imageLikeCountMap);
		
		votingDao.loadContentsVotingCountMap(reviewIds, null, questionAnswerIds,imageSetIds, imageIds,
				reviewVotingCountMap, null, questionAnswerVotingCountMap, imageSetVotingCountMap, imageCountVotingMap);
		
		imageDao.loadAllImageMapByContentsIds(
				reviewIds, 
				questionIds, 
				questionAnswerIds,
				imageSetIds,
				reviewAllImageMap, 
				questionAllImageMap, 
				questionAnswerAllImageMap,
				imageSetAllImageMap);
		
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(searchResult.getDocuments());
		// 同じ商品・ユーザーで、他のレビュー数
		Map<String, Long> sameProductReviewCountMap = reviewDao.loadSameProductReviewCountMap(reviewDOs);
		// 質問回答者数
		Map<String, Long> answerCountMap = questionAnswerDao.loadQuestionAnswerCountMapByQuestionId(questionIds);
		
		for (ActionHistoryDO actionHistory : searchResult.getDocuments()) {
			NewsFeedVO vo = new NewsFeedVO();
			vo.setActionHistory(actionHistory);
			if (ActionHistoryType.USER_REVIEW.equals(actionHistory.getActionHistoryType())
					|| ActionHistoryType.LIKE_REVIEW_50.equals(actionHistory.getActionHistoryType())) {
				
				// ユーザー名も必要になるのでここで設定
				String reivewCommunityUserId = actionHistory.getReview().getCommunityUser().getCommunityUserId();
				if (likeCommunityUserMap.containsKey(reivewCommunityUserId)) {
					actionHistory.getReview().setCommunityUser(likeCommunityUserMap.get(reivewCommunityUserId));
				}
				
				Long reviewCommentCount = reviewCommentCountMap.get(actionHistory.getReview().getReviewId());
				Long reviewLikeCount = reviewLikeCountMap.get(actionHistory.getReview().getReviewId());
				Long[] reviewVotingCount = reviewVotingCountMap.get(actionHistory.getReview().getReviewId());
				if (reviewCommentCount != null) {
					vo.setCommentCount(reviewCommentCount);
				}
				if (reviewLikeCount != null) {
					vo.setLikeCount(reviewLikeCount);
				}
				if( reviewVotingCount != null ) {
					vo.setVotingCountYes(reviewVotingCount[0]);
					vo.setVotingCountNo(reviewVotingCount[1]);
				}
				if (reviewAllImageMap.containsKey(actionHistory.getReview().getReviewId())) {
					vo.setImages(reviewAllImageMap.get(actionHistory.getReview().getReviewId()));
				}
				if( sameProductReviewCountMap.containsKey(actionHistory.getReview().getReviewId()))
					vo.setOtherReviewCount(sameProductReviewCountMap.get(actionHistory.getReview().getReviewId()));
				if (likeReviewMap.containsKey(actionHistory.getReview().getReviewId()))
					vo.setLikeFlg(likeReviewMap.get(actionHistory.getReview().getReviewId()));
				// TODO あとで効率よく取れるように変更する。
				SearchResult<LikeDO> likes = findLikeByReviewId(
						actionHistory.getReview().getReviewId(),
						loginCommunityUserId,
						resourceConfig.evaluationAreaLikeReadLimit);
				
				long like_count = vo.getLikeCount() - (vo.isLikeFlg()? 1 : 0);
				if( vo.isLikeFlg() ){
					// getLikeCount()は、自分を含んだ数字であることが前提。
					if( 0 == likes.getDocuments().size() ){
						vo.setLikePrefixType(LikePrefixType.ONLYONE.getCode());
					}else{
						vo.setLikePrefixType(LikePrefixType.MULTIPLE.getCode());
					}
				}else{
					vo.setLikePrefixType(LikePrefixType.NONE.getCode());
				}
				
				List<String> communityUserNames = new ArrayList<String>();
				if( like_count == 0 ){
					vo.setLikeMessageType(LikeMessageType.NONE.getCode());
				}else{
					if( like_count <= 3 ){
						vo.setLikeMessageType(LikeMessageType.UPTO3.getCode());
						for( LikeDO like : likes.getDocuments()){
							communityUserNames.add(like.getCommunityUser().getCommunityName());
						}
					}else{
						vo.setLikeMessageType(LikeMessageType.MULTIPLE.getCode());
					}
				}
				vo.setLikeUserNames(communityUserNames);
				
				// TODO あとで効率よく取れるように変更する。
				SearchResult<CommentSetVO> resultComment = findReviewCommentByReviewId(
						actionHistory.getReview().getReviewId(),
						null,
						resourceConfig.commentInitReadLimit,
						null,
						false);
				if( !resultComment.getDocuments().isEmpty() )
					Collections.reverse(resultComment.getDocuments());
				vo.setCommentViewRemainingCount(vo.getCommentCount() - resultComment.getDocuments().size());
				vo.setComments(resultComment);
			} else if (ActionHistoryType.USER_QUESTION.equals(actionHistory.getActionHistoryType())) {
				Long questionCommentCount = questionCommentCountMap.get(actionHistory.getQuestion().getQuestionId());
				if (questionCommentCount != null) {
					vo.setCommentCount(questionCommentCount);
				}
				if (questionAllImageMap.containsKey(actionHistory.getQuestion().getQuestionId())) {
					vo.setImages(questionAllImageMap.get(actionHistory.getQuestion().getQuestionId()));
				}
				if( hasAnswerMap.containsKey(actionHistory.getQuestion().getQuestionId())){
					vo.setAnswerFlg(hasAnswerMap.get(actionHistory.getQuestion().getQuestionId()));
				}
			} else if (ActionHistoryType.USER_ANSWER.equals(actionHistory.getActionHistoryType())
					|| ActionHistoryType.LIKE_ANSWER_50.equals(actionHistory.getActionHistoryType())) {
				Long questionAnswerCommentCount = questionAnswerCommentCountMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId());
				Long questionAnswerLikeCount = questionAnswerLikeCountMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId());
				Long[] questionAnswerVotingCount = questionAnswerVotingCountMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId());
				
				if (questionAnswerCommentCount != null) {
					vo.setCommentCount(questionAnswerCommentCount);
				}
				if (questionAnswerLikeCount != null) {
					vo.setLikeCount(questionAnswerLikeCount);
				}
				if( questionAnswerVotingCount != null ) {
					vo.setVotingCountYes(questionAnswerVotingCount[0]);
					vo.setVotingCountNo(questionAnswerVotingCount[1]);
				}
				if (questionAnswerAllImageMap.containsKey(actionHistory.getQuestionAnswer().getQuestionAnswerId())) {
					vo.setImages(questionAnswerAllImageMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId()));
				}
				
				///// TODO リファクタしたほうが良い。デザインのための処理　start/////
				String questionId = actionHistory.getQuestion().getQuestionId();
				
				if (answerCountMap.containsKey(questionId)) {
					vo.setAnswerCount(answerCountMap.get(questionId));
				}
				
				// 質問
				QuestionDO question = questionDao.loadQuestionFromIndex(questionId, true);
				actionHistory.setQuestion(question);
				
				// 質問&回答
				QuestionAnswerDO questionAnswerDO = questionAnswerDao.loadQuestionAnswerFromIndex(actionHistory.getQuestionAnswer().getQuestionAnswerId());
				actionHistory.setQuestionAnswer(questionAnswerDO);
				
				if (likeAnswerMap.containsKey(actionHistory.getQuestionAnswer().getQuestionAnswerId()))
					vo.setLikeFlg(likeAnswerMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId()));
				
				// TODO あとで効率よく取れるように変更する。
				SearchResult<LikeDO> likes = findLikeByQuestionAnswerId(
						actionHistory.getQuestionAnswer().getQuestionAnswerId(),
						loginCommunityUserId,
						resourceConfig.evaluationAreaLikeReadLimit);
				
				long like_count = vo.getLikeCount() - (vo.isLikeFlg()? 1 : 0);
				if( vo.isLikeFlg() ){
					// getLikeCount()は、自分を含んだ数字であることが前提。
					if( 0 == likes.getDocuments().size() ){
						vo.setLikePrefixType(LikePrefixType.ONLYONE.getCode());
					}else{
						vo.setLikePrefixType(LikePrefixType.MULTIPLE.getCode());
					}
				}else{
					vo.setLikePrefixType(LikePrefixType.NONE.getCode());
				}
				
				List<String> communityUserNames = new ArrayList<String>();
				if( like_count == 0 ){
					vo.setLikeMessageType(LikeMessageType.NONE.getCode());
				}else{
					if( like_count <= 3 ){
						vo.setLikeMessageType(LikeMessageType.UPTO3.getCode());
						for( LikeDO like : likes.getDocuments()){
							communityUserNames.add(like.getCommunityUser().getCommunityName());
						}
					}else{
						vo.setLikeMessageType(LikeMessageType.MULTIPLE.getCode());
					}
				}
				vo.setLikeUserNames(communityUserNames);
				
				// TODO あとで効率よく取れるように変更する。
				SearchResult<CommentSetVO> resultComment = findQuestionAnswerCommentByQuestionAnswerId(
						actionHistory.getQuestionAnswer().getQuestionAnswerId(),
						null,
						resourceConfig.commentInitReadLimit,
						null,
						false);
				if( !resultComment.getDocuments().isEmpty() )
					Collections.reverse(resultComment.getDocuments());
				vo.setCommentViewRemainingCount(vo.getCommentCount() - resultComment.getDocuments().size());
				vo.setComments(resultComment);
			} else if (ActionHistoryType.USER_IMAGE.equals(actionHistory.getActionHistoryType())
					|| ActionHistoryType.LIKE_IMAGE_50.equals(actionHistory.getActionHistoryType())) {
				Long imageSetCommentCount = imageSetCommentCountMap.get(actionHistory.getImageSetId());
				if (imageSetCommentCount != null) {
					vo.setCommentCount(imageSetCommentCount);
				}
				if (ActionHistoryType.USER_IMAGE.equals(actionHistory.getActionHistoryType())) {
					Long imageSetLikeCount = imageSetLikeCountMap.get(actionHistory.getImageSetId());
					Long[] imageSetVotingCount = imageSetVotingCountMap.get(actionHistory.getImageSetId());
					
					if (imageSetLikeCount != null) {
						vo.setLikeCount(imageSetLikeCount);
					}
					if( imageSetVotingCount != null ) {
						vo.setVotingCountYes(imageSetVotingCount[0]);
						vo.setVotingCountNo(imageSetVotingCount[1]);
					}
				} else {
					// 画像にいいねが50回
					
					// もうちょっと細かい情報が必要になるのでここで取得しなおす
					ImageHeaderDO imageHeaderDO = imageDao.loadImageHeader(actionHistory.getImageHeader().getImageId());
					// ユーザー名も必要になるのでここで設定
					String imageCommunityUserId = actionHistory.getImageHeader().getOwnerCommunityUser().getCommunityUserId();
					if (likeCommunityUserMap.containsKey(imageCommunityUserId)) {
						imageHeaderDO.setOwnerCommunityUser(likeCommunityUserMap.get(imageCommunityUserId));
					}
					actionHistory.setImageHeader(imageHeaderDO);
					
					Long imageLikeCount = imageLikeCountMap.get(actionHistory.getImageHeader().getImageId());
					Long[] imageVotingCount = imageCountVotingMap.get(actionHistory.getImageSetId());
					
					if (imageLikeCount != null) {
						vo.setLikeCount(imageLikeCount);
					}
					if( imageVotingCount != null ) {
						vo.setVotingCountYes(imageVotingCount[0]);
						vo.setVotingCountNo(imageVotingCount[1]);
					}
					vo.setImages(Arrays.asList(new ImageHeaderDO[]{actionHistory.getImageHeader()}));
				}
				if (imageSetAllImageMap.containsKey(actionHistory.getImageSetId())) {
					vo.setImageHeaders(imageSetAllImageMap.get(actionHistory.getImageSetId()));
				}
			} else if (actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_PRODUCT_MASTER_RANK_CHANGE)) {
				if (userFollowMap.containsKey(actionHistory.getCommunityUser().getCommunityUserId())) {
					vo.setFollowingFlg(userFollowMap.get(actionHistory.getCommunityUser().getCommunityUserId()));
				}
			}
			if (questionFollowMap != null
					&& questionFollowMap.size() > 0
					&& actionHistory.getQuestion() != null
					&& questionFollowMap.containsKey(actionHistory.getQuestion().getQuestionId())) {
				vo.setFollowingFlg(questionFollowMap.get(actionHistory.getQuestion().getQuestionId()));
			}
			
			result.updateFirstAndLast(vo);
			if (actionHistory.isStop(loginCommunityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				continue;
			}
			
			if (actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_REVIEW) ||
					actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_IMAGE) ||
					actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_QUESTION) ||
					actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_ANSWER)) {
				// コンテンツの購入情報を取得する。
				PurchaseProductDO purchaseProductDO = orderDao.loadPurchaseProductBySku(
						actionHistory.getCommunityUser().getCommunityUserId(),
						actionHistory.getProduct().getSku(),
						Path.DEFAULT,
						false);
				if( purchaseProductDO != null && !purchaseProductDO.isDeleted() ){
					vo.setPurchaseProduct( purchaseProductDO );
				}
				// ログインユーザーの購入情報を取得する。
				if( StringUtils.isNotEmpty(loginCommunityUserId)){
					purchaseProductDO = orderDao.loadPurchaseProductBySku(
							loginCommunityUserId,
							actionHistory.getProduct().getSku(),
							Path.DEFAULT,
							false);
					if( purchaseProductDO != null && !purchaseProductDO.isDeleted() ){
						vo.setLoginUserPurchaseProduct(purchaseProductDO);
					}
				}
			}
			
			result.getDocuments().add(vo);
		}
		
		return result;
	}
	
	@Override
	public SearchResult<PurchaseProductSetVO> findPurchaseProductByCommunityUserIdForMyPage(
			String communityUserId,
			PurchaseProductSearchCondition condition,
			int limit,
			int userLimit,
			Date offsetTime,
			String offsetSku,
			boolean previous) {
		return findPurchaseProductByCommunityUserId(
				communityUserId, 
				condition, 
				true, 
				limit, 
				userLimit, 
				offsetTime, 
				offsetSku, 
				previous);
	}
	
	@Override
	@MethodCache(
			cacheStrategy=CacheStrategyType.Hazelcast,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			size=1000,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public SearchResult<PurchaseProductSetVO> findPurchaseProductByCommunityUserIdForUserPage(
			String communityUserId,
			PurchaseProductSearchCondition condition,
			int limit,
			int userLimit, 
			Date offsetTime, 
			String offsetSku, 
			boolean previous) {
		return findPurchaseProductByCommunityUserId(
				communityUserId, 
				condition, 
				false, 
				limit, 
				userLimit, 
				offsetTime, 
				offsetSku, 
				previous);
	}

	/**
	 * 指定したコミュニティユーザーの購入商品情報を購入日順（降順）に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param condition 絞込み条件
	 * @param mypage マイページ情報かどうか
	 * @param limit 最大取得件数
	 * @param userLimit この商品を購入した外のユーザーの最大取得数
	 * @param offsetTime 検索開始時間
	 * @param offsetSku 検索開始SKU
	 * @param previous より前を取得する場合、true
	 * @return 購入商品情報
	 */
	private SearchResult<PurchaseProductSetVO> findPurchaseProductByCommunityUserId(
			String communityUserId,
			PurchaseProductSearchCondition condition, 
			boolean mypage, 
			int limit,
			int userLimit, 
			Date offsetTime, 
			String offsetSku,
			boolean previous){
		
		// FIX ME Solr検索のリミットを300くらいにあげるための暫定対応
		//int tmpLimit = limit;
		int tmpLimit = resourceConfig.mypagePurchaseProductSearchSolrLimit;
		Date tmpOffsetTime = offsetTime;
		String tmpOffsetSku = offsetSku;
		SearchResult<PurchaseProductSetVO> result = new SearchResult<PurchaseProductSetVO>(0, new ArrayList<PurchaseProductSetVO>());
		Map<String, Long> myReviewCountMap = new HashMap<String, Long>();
		Map<String, Long> reviewCountMap = new HashMap<String, Long>();
		Map<String, Long> questionCountMap = new HashMap<String, Long>();
		
		int totalCount = 0;
		int targetCount = 0;
		
		while (true) {
			if( offsetTime != null && !StringUtils.isEmpty(offsetSku) && result.getDocuments().size() == limit ){
				// FIXME　もっと見る、前に戻るの場合は、処理をすべてさせずに中断させる。　そのときnumFoundにlimit+1する。実装としてはよくないので今後改修する。
				result.setNumFound(limit + 1);
				break;
			}
			
			SearchResult<PurchaseProductDO> searchResult = orderDao.findPurchaseProductByCommunityUserId(
					communityUserId, 
					!mypage,
					tmpLimit,
					tmpOffsetTime,
					tmpOffsetSku,
					previous);
			if (searchResult.isHasAdult()) {
				result.setHasAdult(searchResult.isHasAdult());
			}
			
			if (searchResult.getDocuments().isEmpty()) {
				if (PurchaseProductSearchCondition.NO_MY_REVIEW.equals(condition) || PurchaseProductSearchCondition.NO_REVIEW.equals(condition)) {
					result.setNumFound(totalCount - targetCount );
				} else if (PurchaseProductSearchCondition.HAS_WAIT_ANSWER.equals(condition)) {
					result.setNumFound(targetCount);
				} else {
					result.setNumFound(totalCount);
				}
				
				break;
			}
			
			List<String> skus = Lists.newArrayList();
			for (PurchaseProductDO purchaseProduct : searchResult.getDocuments()) {
				skus.add(purchaseProduct.getProduct().getSku());
			}
			
			totalCount += searchResult.getDocuments().size();
			//totalCount += searchResult.getNumFound();
			if (PurchaseProductSearchCondition.NO_MY_REVIEW.equals(condition)) {
				myReviewCountMap.putAll(
						reviewDao.loadReviewCountMapByCommunityUserIdAndSKU(
						communityUserId, skus));
				targetCount += myReviewCountMap.size();
			} else if (PurchaseProductSearchCondition.NO_REVIEW.equals(condition)) {
				reviewCountMap.putAll(
						reviewDao.loadReviewCountMap(skus));
				targetCount += reviewCountMap.size();
			} else if (PurchaseProductSearchCondition.HAS_WAIT_ANSWER.equals(condition)) {
				questionCountMap.putAll(
						questionDao.loadQuestionCountMapBySKU(skus, true, communityUserId));
				targetCount += questionCountMap.size();
			}
			
			for (PurchaseProductDO purchaseProduct : searchResult.getDocuments()) {
				// FIX ME Solr検索のリミットを300くらいにあげるための暫定対応
				if( result.getDocuments().size() == limit )
					break;
				
				if (PurchaseProductSearchCondition.NO_MY_REVIEW.equals(condition)) {
					Long count = myReviewCountMap.get(purchaseProduct.getProduct().getSku());
					if (count == null || count == 0) {
						PurchaseProductSetVO vo = new PurchaseProductSetVO();
						vo.setPurchaseProduct(purchaseProduct);
						result.getDocuments().add(vo);
					}
				} else if (PurchaseProductSearchCondition.NO_REVIEW.equals(condition)) {
					Long count = reviewCountMap.get(purchaseProduct.getProduct().getSku());
					if (count == null || count == 0) {
						PurchaseProductSetVO vo = new PurchaseProductSetVO();
						vo.setPurchaseProduct(purchaseProduct);
						result.getDocuments().add(vo);
					}
				} else if (PurchaseProductSearchCondition.HAS_WAIT_ANSWER.equals(condition)) {
					Long count = questionCountMap.get(purchaseProduct.getProduct().getSku());
					if (count != null && count > 0) {
						PurchaseProductSetVO vo = new PurchaseProductSetVO();
						vo.setPurchaseProduct(purchaseProduct);
						result.getDocuments().add(vo);
					}
				} else {
					PurchaseProductSetVO vo = new PurchaseProductSetVO();
					vo.setPurchaseProduct(purchaseProduct);
					result.getDocuments().add(vo);
				}
			}
			
			PurchaseProductDO tmpPurchaseProductDO = searchResult.getDocuments().get(searchResult.getDocuments().size() - 1);
			tmpOffsetTime = tmpPurchaseProductDO.getPurchaseDate();
			tmpOffsetSku = tmpPurchaseProductDO.getProduct().getSku();
		}
		
		if( previous ) {
			Collections.reverse(result.getDocuments());
		}
		
		List<PurchaseProductSetVO> resultDocuments = Lists.newArrayList();
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(result.getDocuments());
		for (PurchaseProductSetVO vo : result.getDocuments()) {
			result.updateFirstAndLast(vo);
			if (vo.isStop(communityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				continue;
			}
			resultDocuments.add(vo);
		}
		
		result.setDocuments(resultDocuments);
		
		if (result.getDocuments().isEmpty()) {
			return result;
		}
		
		List<String> skus = Lists.newArrayList();
		for (PurchaseProductSetVO vo : result.getDocuments()) {
			skus.add(vo.getPurchaseProduct().getProduct().getSku());
		}
		if (mypage) {
			// 各コンテンツの投稿数を設定する
			if(!PurchaseProductSearchCondition.NO_MY_REVIEW.equals(condition)) {
				myReviewCountMap.putAll(
						reviewDao.loadReviewCountMapByCommunityUserIdAndSKU(
						communityUserId, skus));
			}
			if(!PurchaseProductSearchCondition.NO_REVIEW.equals(condition)) {
				reviewCountMap.putAll(
						reviewDao.loadReviewCountMap(skus));
			}
			if(!PurchaseProductSearchCondition.HAS_WAIT_ANSWER.equals(condition)) {
				questionCountMap.putAll(
						questionDao.loadQuestionCountMapBySKU(
								skus, true, communityUserId));
			}
			// ポイント付与判断およびポイント情報
			settingPointInformation(
					communityUserId,
					myReviewCountMap,
					reviewCountMap,
					questionCountMap,
					result.getDocuments());

		}
		// 商品を購入した他のユーザー一覧設定
		Map<String, List<CommunityUserDO>> orderCommunityUserListMap = orderDao.findOrderCommunityUserBySKUs(
				skus, 
				communityUserId, 
				userLimit, 
				false, 
				true);

		for (PurchaseProductSetVO vo : result.getDocuments()) {
			String sku = vo.getPurchaseProduct().getProduct().getSku();
			vo.setOtherPurchaseCommunityUsers(orderCommunityUserListMap.get(sku));
			// 停止ユーザーを省く処理
			for (Iterator<CommunityUserDO> it = vo.getOtherPurchaseCommunityUsers().iterator(); it.hasNext();) {
				CommunityUserDO target = it.next();
				if (target.isStop(requestScopeDao)) {
					it.remove();
				}
			}
		}
		
		// 商品ごとのレビュー件数、Q&A件数、画像件数を設定する
		Map<String, Long> productReviewCountMap = reviewDao.loadReviewCountMap(skus);
		Map<String, Long> productQuestionCountMap = questionDao.countQuestionBySku(skus.toArray(new String[0]));
		Map<String, Long> productImageCountMap = imageDao.countImageBySku(skus.toArray(new String[0]));
		for (PurchaseProductSetVO vo : result.getDocuments()) {
			String sku = vo.getPurchaseProduct().getProduct().getSku();
			if (productReviewCountMap.containsKey(sku)) {
				vo.setReviewCount(productReviewCountMap.get(sku));
			}
			if (productQuestionCountMap.containsKey(sku)) {
				vo.setQuestionCount(productQuestionCountMap.get(sku));
			}
			if (productImageCountMap.containsKey(sku)) {
				vo.setImageCount(productImageCountMap.get(sku));
			}
		}
		
		return result;
	}
	/**
	 * 指定した商品の商品マスターをランク順（昇順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @return 商品マスター一覧
	 */
	@Override
	public SearchResult<ProductMasterSetVO> findProductMasterBySku(
			String sku, int limit, boolean excludeProduct) {
		return createProductMasterSets(
				productMasterDao.findProductMasterInRankBySKU(sku, limit, 0, excludeProduct),
				true, 0);
	}
	
	@Override
	public SearchResult<ProductMasterSetVO> findProductMasterBySku(
			String sku, int limit) {
		return findProductMasterBySku(sku ,limit, false);
	}

	/**
	 * 指定したコミュニティユーザーの商品マスター情報を
	 * 購入日の新しい順に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param userLimit この商品を購入した外のユーザーの最大取得数
	 * @param offsetTime 検索開始時間
	 * @param offsetSku 検索開始SKU
	 * @param previous より前を取得する場合、true
	 * @return 商品マスター一覧
	 */
	@Override
	public SearchResult<ProductMasterSetVO> findNewPurchaseDateProductMasterByCommunityUserId(
			String communityUserId, int limit,
			int userLimit, Date offsetTime, String offsetSku, boolean previous) {
		return createProductMasterSets(
				productMasterDao.findNewPurchaseDateProductMasterByCommunityUserId(
						communityUserId, limit, offsetTime, offsetSku, previous),
						false, userLimit);
	}

	/**
	 * 指定したコミュニティユーザーの商品マスター情報を
	 * 順位の高い順に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param userLimit この商品を購入した外のユーザーの最大取得数
	 * @param offsetRank 検索開始ランク
	 * @param offsetSku 検索開始SKU
	 * @param previous より前を取得する場合、true
	 * @param asc 昇順ソートの場合、true
	 * @return 商品マスター一覧
	 */
	@Override
	public SearchResult<ProductMasterSetVO> findRankProductMasterByCommunityUserId(
			String communityUserId,
			int limit,
			int userLimit,
			Integer offsetRank,
			String offsetSku,
			boolean previous,
			boolean asc) {
		return createProductMasterSets(
				productMasterDao.findRankProductMasterByCommunityUserId(
						communityUserId, limit, offsetRank,
						offsetSku, previous, asc,
						requestScopeDao.loadAdultVerification()),
						false, userLimit);
	}

	/**
	 * 商品マスターセットを生成して返します。
	 * @param searchResult 検索結果
	 * @param bySku 商品ベースの検索の場合
	 * @param userLimit この商品を購入した外のユーザーの最大取得数
	 * @return 商品マスターセット
	 */
	private SearchResult<ProductMasterSetVO> createProductMasterSets(
			SearchResult<ProductMasterDO> searchResult,
			boolean bySku,
			int userLimit) {
		SearchResult<ProductMasterSetVO> result = new SearchResult<ProductMasterSetVO>();
		result.setHasAdult(searchResult.isHasAdult());
		result.setNumFound(searchResult.getNumFound());
		if (searchResult.getDocuments().size() == 0) {
			return result;
		}
		List<String> communityUserIds = new ArrayList<String>();
		List<String> skus = new ArrayList<String>();
		for (ProductMasterDO productMaster : searchResult.getDocuments()) {
			communityUserIds.add(
					productMaster.getCommunityUser().getCommunityUserId());
			skus.add(productMaster.getProduct().getSku());
		}
		Map<String, Boolean> userFollowMap = null;
		String communityUserId = requestScopeDao.loadCommunityUserId();
		if (communityUserId != null && bySku) {
			userFollowMap = communityUserFollowDao.loadCommunityUserFollowMap(
					communityUserId, communityUserIds);
		}
		Map<String, Boolean> productFollowMap = null;
		if (communityUserId != null && !bySku) {
			productFollowMap = productFollowDao.loadProductFollowMap(communityUserId, skus);
		}
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(searchResult.getDocuments());
		for (ProductMasterDO productMaster : searchResult.getDocuments()) {
			ProductMasterSetVO vo = new ProductMasterSetVO();
			vo.setProductMaster(productMaster);
			if (userFollowMap != null && userFollowMap.containsKey(
					productMaster.getCommunityUser().getCommunityUserId())) {
				vo.setFollowingUser(userFollowMap.get(
						productMaster.getCommunityUser().getCommunityUserId()));
			}
			if (productFollowMap != null && productFollowMap.containsKey(
					productMaster.getProduct().getSku())) {
				vo.setFollowingProduct(
						productFollowMap.get(
								productMaster.getProduct().getSku()));
			}
			if (!bySku) {
				for (ProductMasterDO target : productMasterDao.findProductMasterInRankBySKU(
						productMaster.getProduct(
						).getSku(), 10, 0).getDocuments()) {
					vo.getTopMasters().add(target.getCommunityUser());
				}
			}
			result.updateFirstAndLast(vo);
			if (productMaster.isStop(communityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				continue;
			}
			result.getDocuments().add(vo);
		}
		return result;
	}

	/**
	 * 指定した商品情報を返します。
	 * @param sku SKU
	 * @return 商品情報（最小セット）
	 */
	@Override
	@MethodCache(
			cacheStrategy=CacheStrategyType.HttpSession,
			size=50,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityDataSyncWeb}
			)
	public ProductSetVO getProductBySku(String sku) {
		return getProductBySku(sku, FillType.SMALL, null);
	}
	
	@Override
	@MethodCache(
			cacheStrategy=CacheStrategyType.HttpSession,
			size=50,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityDataSyncWeb}
			)
	public ProductSetVO getProductBySku(String sku, boolean withCart) {
		return getProductBySku(sku, FillType.SMALL, null, withCart);
	}
	
	@Override
	public List<ProductSetVO> getProductBySkus(List<String> skus) {
		return getProductBySkus(skus, FillType.SMALL, null, false);
	}
	
	@Override
	public List<ProductSetVO> getProductBySkus(List<String> skus, boolean withCart) {
		return getProductBySkus(skus, FillType.SMALL, null, withCart);
	}
	/**
	 * 指定した商品情報を返します。
	 * @param sku SKU
	 * @return 商品情報（最小セット）
	 */
	@Override
	@MethodCache(
			cacheStrategy=CacheStrategyType.HttpSession,
			size=50,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityDataSyncWeb}
			)
	public ProductDO getSimpleProductBySku(String sku) {
		return productDao.loadProduct(sku);
	}

	@Override
	@MethodCache(
			cacheStrategy=CacheStrategyType.HttpSession,
			size=50,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityDataSyncWeb}
			)
	public List<ProductDO> getSimpleProductBySkus(List<String> skus) {
		return productDao.loadProducts(skus);
	}
	
	
	@Override
	@MethodCache(
			cacheStrategy=CacheStrategyType.HttpSession,
			size=50,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityDataSyncWeb}
			)
	public List<ProductSetVO> findVariationProduct(String sku) {
		List<ProductDO> variationProducts = productDao.findVariationProductBySku(sku);
		if( variationProducts == null || variationProducts.isEmpty())
			return null;
		
		List<ProductSetVO> results = new ArrayList<ProductSetVO>();
		ProductSetVO productSet = null;
		for(ProductDO product : variationProducts){
			productSet = new ProductSetVO();
			productSet.setProduct(product);
			results.add(productSet);
		}
		return results;
	}
	/**
	 * 指定した商品情報を返します。
	 * @param sku SKU
	 * @param fillType フィルタイプ
	 * @param params 取得パラメーター
	 * @return 商品情報
	 */
	@Override
	public ProductSetVO getProductBySku(
			String sku,
			FillType fillType,
			Map<String,Object> params) {
		return getProductBySku(sku, fillType, params, false);
	}
	/**
	 * 指定した商品情報を返します。
	 * @param sku SKU
	 * @param fillType フィルタイプ
	 * @param params 取得パラメーター
	 * @return 商品情報
	 */
	@Override
	public ProductSetVO getProductBySku(
			String sku,
			FillType fillType,
			Map<String,Object> params,
			boolean withCart) {
		String[] catalogCookies = requestScopeDao.getCatalogCookies();
		ProductDO product = productDao.loadProduct(
				sku,
				fillType,
				catalogCookies[0],
				catalogCookies[1],
				catalogCookies[2],
				withCart,
				params);
		if (product == null) {
			return null;
		} else {
			ProductSetVO productSet = new ProductSetVO();
			productSet.setProduct(product);
			productSet.setFollowerCount(productFollowDao.countFollowerCommunityUser(sku));
			String accessUserId = requestScopeDao.loadCommunityUserId();
			if (accessUserId != null) {
				productSet.setFollowingFlg(
						productFollowDao.existsProductFollow(
								accessUserId, sku));
			}
			return productSet;
		}
	}
	@Override
	public List<ProductSetVO> getProductBySkus(
			List<String> skus,
			FillType fillType, 
			Map<String, Object> params) {
		return getProductBySkus(skus, fillType, params, false);
	}
	@Override
	public List<ProductSetVO> getProductBySkus(
			List<String> skus,
			FillType fillType, 
			Map<String, Object> params,
			boolean withCart) {
		String[] catalogCookies = requestScopeDao.getCatalogCookies();
		List<ProductDO> products = productDao.loadProducts(skus,
				fillType,
				catalogCookies[0],
				catalogCookies[1],
				catalogCookies[2],
				withCart,
				params);
		
		if (products == null || products.isEmpty()) {
			return null;
		}
		
		List<ProductSetVO> result = new ArrayList<ProductSetVO>();
		Map<String, Long> productFollowerMap = productFollowDao.countFollowerCommunityUserBySku(skus.toArray(new String[skus.size()]));
		
		// カート情報を取得する。
//		List<ShoppingCartDO> shoppingCarts = null;
//		if( withCart ){
//			shoppingCarts = productDao.loadShoppingCarts(skus);
//		}
		ProductSetVO productSet = null;
		for( ProductDO product : products ){
			productSet = new ProductSetVO();
//			if( shoppingCarts != null && !shoppingCarts.isEmpty()){
//				for( ShoppingCartDO shoppingCart : shoppingCarts){
//					if( shoppingCart.getSku().equals(product.getSku())){
//						product.setCartTag(shoppingCart.getCartTag());
//						break;
//					}
//				}
//			}
			productSet.setProduct(product);
			if( productFollowerMap.containsKey(product.getSku())){
				productSet.setFollowerCount(productFollowerMap.get(product.getSku()));
			}
			String accessUserId = requestScopeDao.loadCommunityUserId();
			if (accessUserId != null) {
				productSet.setFollowingFlg(
						productFollowDao.existsProductFollow(
								accessUserId, product.getSku()));
			}
			result.add(productSet);
		}

		return result;
	}
	/**
	 * 指定したキーワードで商品を検索して、返します。
	 * @param keyword キーワード
	 * @param excludeSkus 除外する商品リスト
	 * @param includeCero CERO商品を含める場合、true
	 * @param includeAdult アダルト商品を含める場合、true
	 * @return 商品リスト
	 */
	@Override
	public List<ProductDO> findProductByKeyword(
			String keyword,
			List<String> excludeSkus,
			boolean includeCero,
			boolean includeAdult) {
		return productDao.findByKeyword(keyword, excludeSkus,
				includeCero, includeAdult);
	}
	
	/**
	 * 商品マスターのバージョン情報の次バージョンを取得します。
	 * @return 次のバージョン
	 */
	@Override
	@ArroundHBase
	public VersionDO getNextProductMasterVersion() {
		VersionDO version = productMasterDao.loadProductMasterVersion(false);
		if (version == null) {
			version = new VersionDO();
			version.setVersion(1);
			version.setVersionType(VersionType.PRODUCT_MASTER);
		} else {
			version.setVersion(version.getVersion() + 1);
		}
		return version;
	}

	/**
	 * 商品マスターのバージョン情報をアップグレードします。
	 */
	@Override
	@ArroundHBase
	public void upgradeProductMasterVersion() {
		
		// ReadOnlyモードチェック
		if (systemMaintenanceService.getCommunityOperationStatus().equals(CommunityOperationStatus.READONLY_OPERATION)) {
			log.warn("Not updateProductMasterVersion for " + CommunityOperationStatus.READONLY_OPERATION.getLabel());
			return;
		}
		
		VersionDO version = getNextProductMasterVersion();
		productMasterDao.updateProductMasterVersion(version);
	}

	/**
	 * 商品マスターのランキング変動をチェックし、変動していた場合、
	 * 適切な処理を実施します。
	 * @param productMaster 新しい商品マスター情報
	 */
	@Override
	@ArroundSolr
	@ArroundHBase
	public void changeProductMasterRanking(ProductMasterDO productMaster) {
		if (!productMaster.isRequiredNotify()) {
			return;
		}
		if(!systemMaintenanceService.getWebProductMasterStatusWithCache().equals(MaintenanceStatus.IN_OPERATION)) {
			return;
		}
		
		if (!eventHistoryDao.existsLog(productMaster.getProductMasterId(),
				EventHistoryType.PRODUCT_MASTER_RANKIN_INFORMATION)) {
			InformationDO information = new InformationDO();
			information.setAdult(productMaster.isAdult());
			information.setCommunityUser(productMaster.getCommunityUser());
			information.setInformationType(InformationType.PRODUCT_MASTER_RANK_CHANGE);
			information.setProductMaster(productMaster);
			information.setProduct(productMaster.getProduct());
			information.setAdult(productMaster.getProduct().isAdult()); // ??この行いらないのでは
			informationDao.createInformation(information);
			
			// 2012/07/04 saka informationDao.updateInformationInIndex() のhbase.loadなしVerをよぶ
			informationDao.updateInformationInIndex(information);

			//アクションを記録します。
			ActionHistoryDO rankInActionHistory = new ActionHistoryDO();
			rankInActionHistory.setActionHistoryType(ActionHistoryType.USER_PRODUCT_MASTER_RANK_CHANGE);
			rankInActionHistory.setCommunityUser(productMaster.getCommunityUser());
			rankInActionHistory.setProduct(productMaster.getProduct());
			rankInActionHistory.setAdult(productMaster.isAdult());
			rankInActionHistory.setProductMaster(productMaster);
			rankInActionHistory.setProductMasterRank(productMaster.getRank());
			actionHistoryDao.create(rankInActionHistory);

			// 2012/07/04 saka actionHistoryDao.updateActionHistoryInIndex() のhbase.loadなしVerをよぶ
			indexService.updateActionHistoryIndexWithCreateLink(rankInActionHistory);

			// 2012/07/05 saka  notifySocialMediaForProductMasterRankIn() のhbase.loadなしVerをよぶ
			socialMediaService.notifySocialMediaForProductMasterRankIn(
					productMaster,
					productMaster.getCommunityUser().getCommunityUserId());
			
			eventHistoryDao.saveLog(productMaster.getProductMasterId(),
					EventHistoryType.PRODUCT_MASTER_RANKIN_INFORMATION);
		}
	}
}
