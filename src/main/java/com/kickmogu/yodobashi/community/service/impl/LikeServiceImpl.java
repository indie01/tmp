/**
 *
 */
package com.kickmogu.yodobashi.community.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.common.exception.UnActiveException;
import com.kickmogu.yodobashi.community.common.exception.UnMatchTargetException;
import com.kickmogu.yodobashi.community.resource.dao.ActionHistoryDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.ImageDao;
import com.kickmogu.yodobashi.community.resource.dao.InformationDao;
import com.kickmogu.yodobashi.community.resource.dao.LikeDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionAnswerDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.ReviewDao;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;
import com.kickmogu.yodobashi.community.resource.domain.constants.LikeTargetType;
import com.kickmogu.yodobashi.community.service.IndexService;
import com.kickmogu.yodobashi.community.service.LikeService;
import com.kickmogu.yodobashi.community.service.UserService;
import com.kickmogu.yodobashi.community.service.config.ServiceConfig;
import com.kickmogu.yodobashi.community.service.vo.CommunityUserSetVO;

/**
 * いいねサービスの実装です。
 * @author kamiike
 */
@Service
public class LikeServiceImpl extends AbstractServiceImpl implements LikeService {

	/**
	 * アクション履歴 DAO です。
	 */
	@Autowired
	private ActionHistoryDao actionHistoryDao;

	/**
	 * コミュニティユーザー DAO です。
	 */
	@Autowired
	private CommunityUserDao communityUserDao;

	/**
	 * お知らせ情報 DAO です。
	 */
	@Autowired
	private InformationDao informationDao;

	/**
	 * コミュニティユーザーフォロー DAO です。
	 */
	@Autowired
	private CommunityUserFollowDao communityUserFollowDao;

	/**
	 * いいね DAO です。
	 */
	@Autowired
	private LikeDao likeDao;

	/**
	 * イメージDAO です。
	 */
	@Autowired
	private ImageDao imageDao;

	/**
	 * イメージDAO です。
	 */
	@Autowired
	private QuestionAnswerDao questionAnswerDao;

	/**
	 * イメージDAO です。
	 */
	@Autowired
	private QuestionDao questionDao;

	/**
	 * イメージDAO です。
	 */
	@Autowired
	private ReviewDao reviewDao;
	
	/**
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	private RequestScopeDao requestScopeDao;

	/**
	 * インデックスサービスです。
	 */
	@Autowired
	private IndexService indexService;

	/**
	 * コミュニティユーザーサービスです。
	 */
	@Autowired
	private UserService userService;
	
	@Autowired
	private ServiceConfig serviceConfig;

	/**
	 * レビューに対してのいいね情報を更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param reviewId レビューID
	 * @param release 削除するかどうか
	 * @return 成功可否  0:かわらない,1:いいね,2:いいね取り消し
	 */
	@Override
	@ArroundHBase
	@ArroundSolr
	public int updateLikeReview(
			String communityUserId,
			String reviewId,
			boolean release) {
		return updateLike(
				LikeTargetType.REVIEW,
				communityUserId,
				reviewId,
				release);
	}

	/**
	 * 質問回答に対してのいいね情報を更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param questionAnswerId 質問回答ID
	 * @param release 削除するかどうか
	 * @return 成功可否  0:かわらない,1:いいね,2:いいね取り消し
	 */
	@Override
	@ArroundHBase
	@ArroundSolr
	public int updateLikeQuestionAnswer(
			String communityUserId,
			String questionAnswerId,
			boolean release) {
		return updateLike(
				LikeTargetType.QUESTION_ANSWER,
				communityUserId,
				questionAnswerId,
				release);
	}

	/**
	 * 画像に対してのいいね情報を更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param imageId 画像ID
	 * @param release 削除するかどうか
	 * @return 成功可否  0:かわらない,1:いいね,2:いいね取り消し
	 */
	@Override
	@ArroundHBase
	@ArroundSolr
	public int updateLikeImage(
			String communityUserId,
			String imageId,
			boolean release) {
		return updateLike(
				LikeTargetType.IMAGE,
				communityUserId,
				imageId,
				release);
	}

	/**
	 * いいね情報の更新をします。
	 * @param targetType コンテンツ体法
	 * @param communityUserId コミュニティユーザーID
	 * @param contentId コンテンツID
	 * @param release 削除するかどうか
	 * @return 成功可否  0:かわらない,1:いいね,2:いいね取り消し
	 */
	@Override
	@ArroundHBase
	@ArroundSolr
	public int updateLike(
			LikeTargetType likeTargetType,
			String communityUserId,
			String contentId,
			boolean release) {
		
		if( StringUtils.isEmpty(communityUserId) || StringUtils.isEmpty(contentId) || likeTargetType == null )
			throw new IllegalArgumentException("LikeUser and QuestionAnswerUser are the same users.");
		
		// コンテンツ投稿可能チェック
		if(!userService.validateUserStatusForPostContents(communityUserId))
			throw new UnActiveException("can not post contens because user status is failure " + " communityUserId:" + communityUserId);
		
		String likeId = null;
		String actionHistoryId = null;
		String informationId = null;
		
		System.out.println("#13521 communityUserId : " + communityUserId);
		System.out.println("#13521 contentId : " + contentId);
		System.out.println("#13521 likeTargetType : " + likeTargetType.getCode());
		boolean existsLike =likeDao.existsLike(communityUserId, contentId, likeTargetType);
		System.out.println("#13521 existsLike : " + existsLike);
		System.out.println("#13521 release : " + release);
		if (release) {
			if (!existsLike) {
				return 0;
			}
			likeId = likeDao.deleteLike(communityUserId, contentId, likeTargetType);
		}else{
			if (existsLike) {
				return 0;
			} else {
				LikeDO like = new LikeDO();
				like.setTargetType(likeTargetType);
				like.setCommunityUser(new CommunityUserDO());
				like.getCommunityUser().setCommunityUserId(communityUserId);
				
				if( LikeTargetType.REVIEW.equals(likeTargetType) ) {
					ReviewDO review = reviewDao.loadReview(contentId);
					if (review.getCommunityUser() == null
							|| (!CommunityUserStatus.ACTIVE.equals(review.getCommunityUser().getStatus()) && !review.getCommunityUser().isKeepReviewContents())) {
						throw new UnActiveException("can not Voting review because deleted communityUser questionAnswerId:" + contentId);
					}
					
					if( review.getCommunityUser().getCommunityUserId().equals(communityUserId))
						throw new IllegalArgumentException("LikeUser and QuestionAnswerUser are the same users.");
					
					like.setReview(new ReviewDO());
					like.getReview().setReviewId(contentId);
				}else if( LikeTargetType.QUESTION_ANSWER.equals(likeTargetType) ) {
					QuestionAnswerDO questionAnswer = questionAnswerDao.loadQuestionAnswer(contentId);
					if (questionAnswer.getCommunityUser() == null ||
							( CommunityUserStatus.ACTIVE.equals(questionAnswer.getCommunityUser().getStatus()) && !questionAnswer.getCommunityUser().isKeepQuestionContents())) {
						throw new UnActiveException("can not Like questionAnswer because deleted communityUser questionAnswerId:" + contentId);
					}
					
					if( questionAnswer.getCommunityUser().getCommunityUserId().equals(communityUserId))
						throw new IllegalArgumentException("LikeUser and QuestionAnswerUser are the same users.");
					
					QuestionDO question = questionDao.loadQuestion(questionAnswer.getQuestion().getQuestionId());
					if (question.getCommunityUser() == null || 
							( CommunityUserStatus.ACTIVE.equals(question.getCommunityUser().getStatus()) && !question.getCommunityUser().isKeepQuestionContents())) {
						throw new UnActiveException("can not Like questionAnswer because deleted communityUser questionId:" + questionAnswer.getQuestion().getQuestionId());
					}
					
					
					like.setQuestionAnswer(new QuestionAnswerDO());
					like.getQuestionAnswer().setQuestionAnswerId(contentId);
				}else if( LikeTargetType.IMAGE.equals(likeTargetType) ) {
					ImageHeaderDO imageHeader = imageDao.loadImageHeader(contentId);
					if (imageHeader == null || 
							( CommunityUserStatus.ACTIVE.equals(imageHeader.getOwnerCommunityUser().getStatus()) && !imageHeader.getOwnerCommunityUser().isKeepImageContents())) {
						throw new UnActiveException("can not Like image because deleted communityUser imageId:" + contentId);
					}
					
					if( imageHeader.getOwnerCommunityUser().getCommunityUserId().equals(communityUserId))
						throw new IllegalArgumentException("LikeUser and QuestionAnswerUser are the same users.");
					
					like.setImageHeader(new ImageHeaderDO());
					like.getImageHeader().setImageId(contentId);
				}else{
					throw new UnMatchTargetException("LikeTargetType un match targetType:" + likeTargetType.name());
				}
				
				likeDao.createLike(like);
				likeId = like.getLikeId();
				
				InformationDO information = null;
				CommunityUserDO cummunityUserDO = null;
				if( LikeTargetType.REVIEW.equals(likeTargetType) ) {
					cummunityUserDO = like.getReview().getCommunityUser();
					if( cummunityUserDO != null && cummunityUserDO.getCommunityUserId() != null && !cummunityUserDO.getCommunityUserId().equals(communityUserId)){
						information = new InformationDO();
						information.setInformationType(InformationType.REVIEW_LIKE_ADD);
						information.setReview(like.getReview());
						information.setAdult(information.getReview().isAdult());
					}
				}else if( LikeTargetType.QUESTION_ANSWER.equals(likeTargetType) ) {
					cummunityUserDO = like.getQuestionAnswer().getCommunityUser();
					if( cummunityUserDO != null && cummunityUserDO.getCommunityUserId() != null && !cummunityUserDO.getCommunityUserId().equals(communityUserId)){
						information = new InformationDO();
						information.setInformationType(InformationType.QUESTION_ANSWER_LIKE_ADD);
						information.setQuestionAnswer(like.getQuestionAnswer());
						information.setAdult(information.getQuestionAnswer().isAdult());
						information.setQuestion(like.getQuestionAnswer().getQuestion());
						information.setRelationQuestionOwnerId(information.getQuestion().getCommunityUser().getCommunityUserId());
					}
				}else if( LikeTargetType.IMAGE.equals(likeTargetType) ) {
					cummunityUserDO = like.getImageHeader().getOwnerCommunityUser();
					if( cummunityUserDO != null && cummunityUserDO.getCommunityUserId() != null && !cummunityUserDO.getCommunityUserId().equals(communityUserId)){
						information = new InformationDO();
						information.setInformationType(InformationType.IMAGE_LIKE_ADD);
						information.setImageHeader(like.getImageHeader());
						information.setAdult(information.getImageHeader().isAdult());
					}
				}
				// いいねユーザーと投稿ユーザーが同じ場合はお知らせ表示しない
				// お知らせ登録処理
				if(information != null && cummunityUserDO != null){
					information.setLike(like);
					information.setCommunityUser(cummunityUserDO);
					information.setRelationLikeOwnerId(communityUserId);
					information.setRelationCommunityUserId(communityUserId);
					informationDao.createInformation(information);
					informationId = information.getInformationId();
				}
				
				if (actionHistoryDao.requiredSaveLikeMilstone(like, serviceConfig.likeThresholdLimit)) {
					ActionHistoryDO likeActionHistory = null;
					if( LikeTargetType.REVIEW.equals(likeTargetType) ) {
						likeActionHistory = new ActionHistoryDO();
						likeActionHistory.setActionHistoryType(ActionHistoryType.LIKE_REVIEW_50);
						likeActionHistory.setReview(like.getReview());
						likeActionHistory.setAdult(like.getReview().isAdult());
						likeActionHistory.setProduct(like.getReview().getProduct());
						likeActionHistory.setRelationReviewOwnerId(like.getReview().getCommunityUser().getCommunityUserId());
					}else if( LikeTargetType.QUESTION_ANSWER.equals(likeTargetType) ) {
						likeActionHistory = new ActionHistoryDO();
						likeActionHistory.setActionHistoryType(ActionHistoryType.LIKE_ANSWER_50);
						likeActionHistory.setQuestionAnswer(like.getQuestionAnswer());
						likeActionHistory.setQuestion(like.getQuestionAnswer().getQuestion());
						likeActionHistory.setAdult(like.getQuestionAnswer().isAdult());
						likeActionHistory.setProduct(like.getQuestionAnswer().getProduct());
						likeActionHistory.setRelationQuestionAnswerOwnerId(like.getQuestionAnswer().getCommunityUser().getCommunityUserId());
						likeActionHistory.setRelationQuestionOwnerId(like.getQuestionAnswer().getQuestion().getCommunityUser().getCommunityUserId());
					}else if( LikeTargetType.IMAGE.equals(likeTargetType) ) {
						likeActionHistory = new ActionHistoryDO();
						likeActionHistory.setActionHistoryType(ActionHistoryType.LIKE_IMAGE_50);
						likeActionHistory.setImageHeader(like.getImageHeader());
						likeActionHistory.setImageSetId(like.getImageHeader().getImageSetId());
						likeActionHistory.setAdult(like.getImageHeader().isAdult());
						likeActionHistory.setProduct(new ProductDO());
						likeActionHistory.getProduct().setSku(like.getImageHeader().getSku());
						likeActionHistory.setRelationImageOwnerId(like.getImageHeader().getOwnerCommunityUserId());
					}
					
					if( likeActionHistory != null){
						likeActionHistory.setActionHistoryId(actionHistoryDao.createLikeActionHistoryId(like, serviceConfig.likeThresholdLimit));
						likeActionHistory.setCommunityUser(like.getCommunityUser());
						actionHistoryDao.create(likeActionHistory);
						actionHistoryId = likeActionHistory.getActionHistoryId();
					}
				}
			}
		}
		
		indexService.updateIndexForSaveLike(
				likeId,
				actionHistoryId,
				informationId);
		
		return release ? 2 : 1;
	}

	/**
	 * 指定した画像に対するいいねユーザーを返します。
	 * @param imageId 画像ID
	 * @param limit 最大取得件数
	 * @return いいねユーザーリスト
	 */
	@Override
	@ArroundSolr
	public SearchResult<CommunityUserSetVO> findLikeCommunityUserByImageId(
			String imageId, int limit) {
		return createCommunityUserSets(LikeTargetType.IMAGE, imageId, limit);
	}

	/**
	 * 指定したレビューに対するいいねユーザーを返します。
	 * @param reviewId レビューID
	 * @param limit 最大取得件数
	 * @return いいねユーザーリスト
	 */
	@Override
	@ArroundSolr
	public SearchResult<CommunityUserSetVO> findLikeCommunityUserByReviewId(
			String reviewId, int limit) {
		return createCommunityUserSets(LikeTargetType.REVIEW, reviewId, limit);
	}

	/**
	 * 指定した質問回答に対するいいねユーザーを返します。
	 * @param questionAnswerId 質問回答ID
	 * @param limit 最大取得件数
	 * @return いいねユーザーリスト
	 */
	@Override
	@ArroundSolr
	public SearchResult<CommunityUserSetVO> findLikeCommunityUserByQuestionAnswerId(
			String questionAnswerId, int limit) {
		return createCommunityUserSets(LikeTargetType.QUESTION_ANSWER,
				questionAnswerId, limit);
	}

	/**
	 * 指定したコンテンツに対するいいねユーザーを返します。
	 * @param type タイプ
	 * @param contentsId コンテンツID
	 * @param limit 最大取得件数
	 * @return いいねユーザーリスト
	 */
	private SearchResult<CommunityUserSetVO> createCommunityUserSets(
			LikeTargetType type,
			String contentsId,
			int limit) {
		String communityUserId = requestScopeDao.loadCommunityUserId();
		SearchResult<LikeDO> searchResult = likeDao.findLikeByContentsId(
				type, communityUserId, contentsId, limit);
		SearchResult<CommunityUserSetVO> result = new SearchResult<CommunityUserSetVO>();
		result.setHasAdult(searchResult.isHasAdult());
		result.setNumFound(searchResult.getNumFound());
		List<String> communityUserIds = new ArrayList<String>();
		for (LikeDO like : searchResult.getDocuments()) {
			communityUserIds.add(like.getCommunityUser().getCommunityUserId());
		}
		Map<String, Boolean> userFollowMap = null;
		if (communityUserId != null) {
			userFollowMap = communityUserFollowDao.loadCommunityUserFollowMap(
					communityUserId, communityUserIds);
		}
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(searchResult.getDocuments());
		for (LikeDO like : searchResult.getDocuments()) {
			CommunityUserSetVO vo = new CommunityUserSetVO();
			vo.setCommunityUser(like.getCommunityUser());
			if (userFollowMap != null
					&& userFollowMap.size() > 0
					&& userFollowMap.containsKey(
							like.getCommunityUser().getCommunityUserId())) {
				vo.setFollowingUser(userFollowMap.get(
						like.getCommunityUser().getCommunityUserId()));
			}

			result.updateFirstAndLast(vo);
			if (like.isStop(communityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				continue;
			}
			result.getDocuments().add(vo);
		}
		return result;
	}

}
