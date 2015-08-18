/**
 *
 */
package com.kickmogu.yodobashi.community.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.common.exception.DataNotFoundException;
import com.kickmogu.yodobashi.community.common.exception.UnActiveException;
import com.kickmogu.yodobashi.community.resource.dao.ActionHistoryDao;
import com.kickmogu.yodobashi.community.resource.dao.CommentDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.ImageDao;
import com.kickmogu.yodobashi.community.resource.dao.InformationDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionAnswerDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.ReviewDao;
import com.kickmogu.yodobashi.community.resource.dao.util.ProductUtil;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommentTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;
import com.kickmogu.yodobashi.community.service.CommentService;
import com.kickmogu.yodobashi.community.service.IndexService;
import com.kickmogu.yodobashi.community.service.MailService;
import com.kickmogu.yodobashi.community.service.UserService;
import com.kickmogu.yodobashi.community.service.vo.CommentSetVO;

/**
 * コメントサービスの実装です。
 * @author kamiike
 */
@Service
public class CommentServiceImpl extends AbstractServiceImpl implements CommentService {

	/**
	 * アクション履歴 DAO です。
	 */
	@Autowired
	protected ActionHistoryDao actionHistoryDao;

	/**
	 * コミュニティユーザー DAO です。
	 */
	@Autowired
	protected CommunityUserDao communityUserDao;

	/**
	 * コメント DAO です。
	 */
	@Autowired
	protected CommentDao commentDao;

	/**
	 * お知らせ情報 DAO です。
	 */
	@Autowired
	protected InformationDao informationDao;

	/**
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	protected RequestScopeDao requestScopeDao;

	/**
	 * インデックスサービスです。
	 */
	@Autowired
	protected IndexService indexService;

	/**
	 * コミュニティユーザーサービスです。
	 */
	@Autowired
	protected UserService userService;
	/**
	 * メールサービスです。
	 */
	@Autowired
	protected MailService mailService;

	@Override
	@ArroundHBase
	public void deleteComment(String commentId) {
		deleteComment(commentId, false);
	}

	/**
	 * 指定したコメントを削除します。
	 * @param commentId コメントID
	 */
	@Override
	@ArroundHBase
	public void deleteComment(String commentId, boolean mngToolOperation) {
		CommentDO comment = commentDao.loadComment(commentId);
		if (comment == null) return;
		if(!mngToolOperation){
			String accessUserId = requestScopeDao.loadCommunityUserId();
			if (!comment.getCommunityUser().getCommunityUserId(
					).equals(accessUserId)) {
				throw new SecurityException(
						"This comment is different owner. ownerId = " +
						comment.getCommunityUser().getCommunityUserId() +
						" input = " + accessUserId);
			}
		}
		if (comment.isDeleteFlag()) {
			return;
		}

		if(!mngToolOperation){
			// コンテンツ投稿可能チェック
			if(!userService.validateUserStatusForPostContents(comment.getCommunityUser().getCommunityUserId()))
				throw new UnActiveException("can not post contens because user status is failure " + " communityUserId:" + comment.getCommunityUser().getCommunityUserId());
		}

		comment.setDeleteFlag(true);
		commentDao.deleteComment(commentId, mngToolOperation);
		indexService.updateIndexForSaveComment(commentId, null, null);
	}


	@Autowired
	private ReviewDao reviewDao;

	@Autowired
	private QuestionDao questionDao;

	@Autowired
	private QuestionAnswerDao questionAnswerDao;

	@Autowired
	private ImageDao imageDao;
	/**
	 * コメントを登録/更新します。
	 * @param comment コメント
	 * @return 登録したコメント
	 */
	@Override
	@ArroundHBase
	@ArroundSolr
	public CommentDO saveComment(CommentDO comment) {
		CommentDO oldComment = null;
		if (StringUtils.isEmpty(comment.getCommentId())) {
			comment.setCommentId(null);
		} else {
			oldComment = commentDao.loadComment(comment.getCommentId(),Path.DEFAULT);
			if (oldComment == null) {
				throw new DataNotFoundException("This comment was deleted. commentId = " + comment.getCommentId());
			}
			if (!comment.getCommunityUser().getCommunityUserId().equals(oldComment.getCommunityUser().getCommunityUserId())) {
				throw new SecurityException(
						"This comment is different owner. ownerId = " +
						oldComment.getCommunityUser().getCommunityUserId() +
						" input = " + comment.getCommunityUser().getCommunityUserId());
			}
			if (oldComment.isDeleted()) {
				throw new DataNotFoundException("This comment was deleted. commentId = " + comment.getCommentId());
			}
		}

		// コンテンツ投稿可能チェック
		if(!userService.validateUserStatusForPostContents(comment.getCommunityUser().getCommunityUserId()))
			throw new UnActiveException("can not post contens because user status is failure " + " communityUserId:" + comment.getCommunityUser().getCommunityUserId());
		
		ReviewDO review = null;
		QuestionDO question = null;
		QuestionAnswerDO questionAnswer = null;
		
		if(CommentTargetType.REVIEW.equals(comment.getTargetType())){
			review = reviewDao.loadReview(comment.getReview().getReviewId());
			if (review == null
					|| review.getCommunityUser() == null
					|| (review.getCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
						&& !review.getCommunityUser().isKeepReviewContents())) {
				throw new UnActiveException("can not submit comment review because deleted communityUser reviewId:" + comment.getReview().getReviewId());
			}
			comment.setReview(review);
		}else if(CommentTargetType.QUESTION_ANSWER.equals(comment.getTargetType())){
			questionAnswer = questionAnswerDao.loadQuestionAnswer(comment.getQuestionAnswer().getQuestionAnswerId());
			if (questionAnswer == null
					|| questionAnswer.getCommunityUser() == null
					|| (questionAnswer.getCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
						&& !questionAnswer.getCommunityUser().isKeepQuestionContents())
					) {
				throw new UnActiveException("can not submit comment questionAnswer because deleted communityUser questionAnswerId:" + comment.getQuestionAnswer().getQuestionAnswerId());
			}
			question = questionDao.loadQuestion(questionAnswer.getQuestion().getQuestionId());
			if (question == null
					|| question.getCommunityUser() == null
					|| (question.getCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
						&& !question.getCommunityUser().isKeepQuestionContents())
					) {
				throw new UnActiveException("can not submit comment questionAnswer(question) because deleted communityUser questionId:" + questionAnswer.getQuestion().getQuestionId());
			}
			comment.setQuestionAnswer(questionAnswer);
		}else if(CommentTargetType.IMAGE.equals(comment.getTargetType())){
			ImageHeaderDO imageHeader = imageDao.loadImageHeader(comment.getImageHeader().getImageId());
			if (imageHeader == null
					|| (imageHeader.getImageSetId() == null && imageHeader.getReview() == null && imageHeader.getQuestion() == null && imageHeader.getQuestionAnswer() == null)
					|| imageHeader.getOwnerCommunityUser() == null
					|| (imageHeader.getOwnerCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
						&& !imageHeader.getOwnerCommunityUser().isKeepImageContents())
					) {
				throw new UnActiveException("can not submit comment imageSet because deleted communityUser imageId:" + comment.getImageHeader().getImageId());
			}

			comment.setImageHeader(imageHeader);
			
			if( imageHeader.getReview() != null ){
				review = reviewDao.loadReview(imageHeader.getReview().getReviewId());
				if (review == null
						|| review.getCommunityUser() == null
						|| (review.getCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
							&& !review.getCommunityUser().isKeepReviewContents())) {
					throw new UnActiveException("can not submit image comment by review image because deleted communityUser reviewId:" + imageHeader.getReview().getReviewId());
				}
			}else if(imageHeader.getQuestion() != null || imageHeader.getQuestionAnswer() != null ){
				if( imageHeader.getQuestionAnswer() != null ){
					questionAnswer = questionAnswerDao.loadQuestionAnswer(imageHeader.getQuestionAnswer().getQuestionAnswerId());
					if (questionAnswer == null
							|| questionAnswer.getCommunityUser() == null
							|| (questionAnswer.getCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
								&& !questionAnswer.getCommunityUser().isKeepQuestionContents())
							) {
						throw new UnActiveException("can not submit image comment answer image because deleted communityUser questionAnswerId:" + imageHeader.getQuestionAnswer().getQuestionAnswerId());
					}
				}
				if( imageHeader.getQuestion() != null ){
					question = questionDao.loadQuestion(imageHeader.getQuestion().getQuestionId());
					if (question == null
							|| question.getCommunityUser() == null
							|| (question.getCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
								&& !question.getCommunityUser().isKeepQuestionContents())
							) {
						throw new UnActiveException("can not submit image comment by question image because deleted communityUser questionId:" + imageHeader.getQuestion().getQuestionId());
					}
				}
			}else{
				if( StringUtils.isBlank(imageHeader.getImageSetId()) )
					throw new UnActiveException("can not get content");
			}
		}
		
		commentDao.saveComment(comment);
		boolean sameSubmitUser = false;

		if (oldComment == null) {
			ActionHistoryDO actionHistory = new ActionHistoryDO();
			actionHistory.setComment(comment);
			actionHistory.setCommunityUser(comment.getCommunityUser());

			InformationDO information = new InformationDO();
			information.setComment(comment);
			information.setRelationCommentOwnerId(comment.getCommunityUser().getCommunityUserId());
			information.setRelationCommunityUserId(comment.getCommunityUser().getCommunityUserId());

			if (comment.getTargetType().equals(CommentTargetType.REVIEW)) {
				actionHistory.setActionHistoryType(ActionHistoryType.USER_REVIEW_COMMENT);
				actionHistory.setReview(comment.getReview());
				actionHistory.setAdult(actionHistory.getReview().isAdult());
				actionHistory.setRelationReviewOwnerId(comment.getReview().getCommunityUser().getCommunityUserId());
				actionHistory.setProduct(comment.getReview().getProduct());

				information.setInformationType(InformationType.REVIEW_COMMENT_ADD);
				information.setCommunityUser(comment.getReview().getCommunityUser());
				information.setReview(comment.getReview());
				information.setAdult(information.getReview().isAdult());
				sameSubmitUser = (comment.getCommunityUser().getCommunityUserId().equals(comment.getReview().getCommunityUser().getCommunityUserId()));
			} else if (comment.getTargetType().equals(CommentTargetType.QUESTION_ANSWER)) {
				actionHistory.setActionHistoryType(ActionHistoryType.USER_ANSWER_COMMENT);
				actionHistory.setQuestion(comment.getQuestionAnswer().getQuestion());
				actionHistory.setQuestionAnswer(comment.getQuestionAnswer());
				actionHistory.setAdult(actionHistory.getQuestionAnswer().isAdult());
				actionHistory.setRelationQuestionAnswerOwnerId(comment.getQuestionAnswer().getCommunityUser().getCommunityUserId());
				actionHistory.setRelationQuestionOwnerId(comment.getQuestionAnswer().getQuestion().getCommunityUser().getCommunityUserId());
				actionHistory.setProduct(comment.getQuestionAnswer().getProduct());

				information.setInformationType(InformationType.QUESTION_ANSWER_COMMENT_ADD);
				information.setCommunityUser(comment.getQuestionAnswer().getCommunityUser());
				information.setQuestion(comment.getQuestionAnswer().getQuestion());
				information.setQuestionAnswer(comment.getQuestionAnswer());
				information.setAdult(actionHistory.getQuestionAnswer().isAdult());
				information.setRelationQuestionOwnerId(comment.getQuestionAnswer().getQuestion().getCommunityUser().getCommunityUserId());

				sameSubmitUser = (comment.getCommunityUser().getCommunityUserId().equals(comment.getQuestionAnswer().getCommunityUser().getCommunityUserId()));
			} else if (CommentTargetType.IMAGE.equals(comment.getTargetType())) {
				actionHistory.setActionHistoryType(ActionHistoryType.USER_IMAGE_COMMENT);
				actionHistory.setImageHeader(comment.getImageHeader());
				if(comment.getImageHeader().getReview() != null){
					actionHistory.setReview(review);
				}else if(comment.getImageHeader().getQuestion() != null || comment.getImageHeader().getQuestionAnswer() != null){
					actionHistory.setQuestion(question);
					actionHistory.setQuestionAnswer(questionAnswer);
				}else if( comment.getImageHeader().getImageSetId() != null ){
					actionHistory.setImageSetId(comment.getImageHeader().getImageSetId());
				}
				
				actionHistory.setAdult(actionHistory.getImageHeader().isAdult());
				actionHistory.setRelationImageOwnerId(comment.getImageHeader().getOwnerCommunityUserId());
				actionHistory.setProduct(new ProductDO());
				actionHistory.getProduct().setSku(comment.getImageHeader().getSku());

				information.setInformationType(InformationType.IMAGE_COMMENT_ADD);
				information.setCommunityUser(comment.getImageHeader().getOwnerCommunityUser());
				information.setImageHeader(comment.getImageHeader());
				information.setAdult(information.getImageHeader().isAdult());
				sameSubmitUser = (comment.getCommunityUser().getCommunityUserId().equals(comment.getImageHeader().getOwnerCommunityUser().getCommunityUserId()));
				
			}
			if(!sameSubmitUser){
				if( information != null )
					informationDao.createInformation(information);
			}
			if( actionHistory != null )
				actionHistoryDao.create(actionHistory);

			indexService.updateIndexForSaveComment(
					comment.getCommentId(),
					(actionHistory!=null?actionHistory.getActionHistoryId():null),
					(information!=null?information.getInformationId():null));
			mailService.sendNotifyMailForJustAfterCommentSubmit(comment.getCommentId());
		} else {
			indexService.updateIndexForSaveComment(
					comment.getCommentId(),
					null,
					null);
		}
		return comment;
	}

	@Override
	@ArroundHBase
	public CommentDO saveComment(
			CommentTargetType targetType,
			CommunityUserDO communityUser,
			String contentId,
			String commentText) {
		// コンテンツ投稿可能チェック
		if(!userService.validateUserStatusForPostContents(communityUser.getCommunityUserId()))
			throw new UnActiveException("can not post contens because user status is failure " + " communityUserId:" + communityUser.getCommunityUserId());
		
		ReviewDO review = null;
		QuestionDO question = null;
		QuestionAnswerDO questionAnswer = null;
		
		CommentDO comment = new CommentDO();
		comment.setTargetType(targetType);
		comment.setCommentBody(commentText);
		comment.setCommunityUser(communityUser);
		
		if(CommentTargetType.REVIEW.equals(comment.getTargetType())){
			review = reviewDao.loadReview(contentId);
			if (review == null
					|| review.getCommunityUser() == null
					|| (review.getCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
						&& !review.getCommunityUser().isKeepReviewContents())) {
				throw new UnActiveException("can not submit comment review because deleted communityUser reviewId:" + contentId);
			}
			comment.setReview(review);
		}else if(CommentTargetType.QUESTION_ANSWER.equals(comment.getTargetType())){
			questionAnswer = questionAnswerDao.loadQuestionAnswer(contentId);
			if (questionAnswer == null
					|| questionAnswer.getCommunityUser() == null
					|| (questionAnswer.getCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
						&& !questionAnswer.getCommunityUser().isKeepQuestionContents())
					) {
				throw new UnActiveException("can not submit comment questionAnswer because deleted communityUser questionAnswerId:" + contentId);
			}
			question = questionDao.loadQuestion(questionAnswer.getQuestion().getQuestionId());
			if (question == null
					|| question.getCommunityUser() == null
					|| (question.getCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
						&& !question.getCommunityUser().isKeepQuestionContents())
					) {
				throw new UnActiveException("can not submit comment questionAnswer(question) because deleted communityUser questionId:" + questionAnswer.getQuestion().getQuestionId());
			}
			comment.setQuestionAnswer(questionAnswer);
		}else if(CommentTargetType.IMAGE.equals(comment.getTargetType())){
			ImageHeaderDO imageHeader = imageDao.loadImageHeader(contentId);
			if (imageHeader == null
					|| (imageHeader.getImageSetId() == null && imageHeader.getReview() == null && imageHeader.getQuestion() == null && imageHeader.getQuestionAnswer() == null)
					|| imageHeader.getOwnerCommunityUser() == null
					|| (imageHeader.getOwnerCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
						&& !imageHeader.getOwnerCommunityUser().isKeepImageContents())
					) {
				throw new UnActiveException("can not submit comment imageSet because deleted communityUser imageId:" + contentId);
			}

			comment.setImageHeader(imageHeader);
			
			if( imageHeader.getReview() != null ){
				review = reviewDao.loadReview(imageHeader.getReview().getReviewId());
				if (review == null
						|| review.getCommunityUser() == null
						|| (review.getCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
							&& !review.getCommunityUser().isKeepReviewContents())) {
					throw new UnActiveException("can not submit image comment by review image because deleted communityUser reviewId:" + imageHeader.getReview().getReviewId());
				}
			}else if(imageHeader.getQuestion() != null || imageHeader.getQuestionAnswer() != null ){
				if( imageHeader.getQuestionAnswer() != null ){
					questionAnswer = questionAnswerDao.loadQuestionAnswer(imageHeader.getQuestionAnswer().getQuestionAnswerId());
					if (questionAnswer == null
							|| questionAnswer.getCommunityUser() == null
							|| (questionAnswer.getCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
								&& !questionAnswer.getCommunityUser().isKeepQuestionContents())
							) {
						throw new UnActiveException("can not submit image comment answer image because deleted communityUser questionAnswerId:" + imageHeader.getQuestionAnswer().getQuestionAnswerId());
					}
				}
				if( imageHeader.getQuestion() != null ){
					question = questionDao.loadQuestion(imageHeader.getQuestion().getQuestionId());
					if (question == null
							|| question.getCommunityUser() == null
							|| (question.getCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
								&& !question.getCommunityUser().isKeepQuestionContents())
							) {
						throw new UnActiveException("can not submit image comment by question image because deleted communityUser questionId:" + imageHeader.getQuestion().getQuestionId());
					}
				}
			}else{
				if( StringUtils.isBlank(imageHeader.getImageSetId()) )
					throw new UnActiveException("can not get content");
			}
		}
		
		commentDao.saveComment(comment);
		boolean sameSubmitUser = false;

		ActionHistoryDO actionHistory = new ActionHistoryDO();
		actionHistory.setComment(comment);
		actionHistory.setCommunityUser(comment.getCommunityUser());
	
		InformationDO information = new InformationDO();
		information.setComment(comment);
		information.setRelationCommentOwnerId(comment.getCommunityUser().getCommunityUserId());
		information.setRelationCommunityUserId(comment.getCommunityUser().getCommunityUserId());
	
		if (comment.getTargetType().equals(CommentTargetType.REVIEW)) {
			actionHistory.setActionHistoryType(ActionHistoryType.USER_REVIEW_COMMENT);
			actionHistory.setReview(comment.getReview());
			actionHistory.setAdult(actionHistory.getReview().isAdult());
			actionHistory.setRelationReviewOwnerId(comment.getReview().getCommunityUser().getCommunityUserId());
			actionHistory.setProduct(comment.getReview().getProduct());
	
			information.setInformationType(InformationType.REVIEW_COMMENT_ADD);
			information.setCommunityUser(comment.getReview().getCommunityUser());
			information.setReview(comment.getReview());
			information.setAdult(information.getReview().isAdult());
			sameSubmitUser = (comment.getCommunityUser().getCommunityUserId().equals(comment.getReview().getCommunityUser().getCommunityUserId()));
		} else if (comment.getTargetType().equals(CommentTargetType.QUESTION_ANSWER)) {
			actionHistory.setActionHistoryType(ActionHistoryType.USER_ANSWER_COMMENT);
			actionHistory.setQuestion(comment.getQuestionAnswer().getQuestion());
			actionHistory.setQuestionAnswer(comment.getQuestionAnswer());
			actionHistory.setAdult(actionHistory.getQuestionAnswer().isAdult());
			actionHistory.setRelationQuestionAnswerOwnerId(comment.getQuestionAnswer().getCommunityUser().getCommunityUserId());
			actionHistory.setRelationQuestionOwnerId(comment.getQuestionAnswer().getQuestion().getCommunityUser().getCommunityUserId());
			actionHistory.setProduct(comment.getQuestionAnswer().getProduct());
	
			information.setInformationType(InformationType.QUESTION_ANSWER_COMMENT_ADD);
			information.setCommunityUser(comment.getQuestionAnswer().getCommunityUser());
			information.setQuestion(comment.getQuestionAnswer().getQuestion());
			information.setQuestionAnswer(comment.getQuestionAnswer());
			information.setAdult(actionHistory.getQuestionAnswer().isAdult());
			information.setRelationQuestionOwnerId(comment.getQuestionAnswer().getQuestion().getCommunityUser().getCommunityUserId());
	
			sameSubmitUser = (comment.getCommunityUser().getCommunityUserId().equals(comment.getQuestionAnswer().getCommunityUser().getCommunityUserId()));
		} else if (CommentTargetType.IMAGE.equals(comment.getTargetType())) {
			actionHistory.setActionHistoryType(ActionHistoryType.USER_IMAGE_COMMENT);
			actionHistory.setImageHeader(comment.getImageHeader());
			if(comment.getImageHeader().getReview() != null){
				actionHistory.setReview(review);
			}else if(comment.getImageHeader().getQuestion() != null || comment.getImageHeader().getQuestionAnswer() != null){
				actionHistory.setQuestion(question);
				actionHistory.setQuestionAnswer(questionAnswer);
			}else if( comment.getImageHeader().getImageSetId() != null ){
				actionHistory.setImageSetId(comment.getImageHeader().getImageSetId());
			}
			
			actionHistory.setAdult(actionHistory.getImageHeader().isAdult());
			actionHistory.setRelationImageOwnerId(comment.getImageHeader().getOwnerCommunityUserId());
			actionHistory.setProduct(new ProductDO());
			actionHistory.getProduct().setSku(comment.getImageHeader().getSku());
	
			information.setInformationType(InformationType.IMAGE_COMMENT_ADD);
			information.setCommunityUser(comment.getImageHeader().getOwnerCommunityUser());
			information.setImageHeader(comment.getImageHeader());
			information.setAdult(information.getImageHeader().isAdult());
			sameSubmitUser = (comment.getCommunityUser().getCommunityUserId().equals(comment.getImageHeader().getOwnerCommunityUser().getCommunityUserId()));
			
		}
		if(!sameSubmitUser){
			if( information != null )
				informationDao.createInformation(information);
		}
		if( actionHistory != null )
			actionHistoryDao.create(actionHistory);
	
		indexService.updateIndexForSaveComment(
				comment.getCommentId(),
				(actionHistory!=null?actionHistory.getActionHistoryId():null),
				(information!=null?information.getInformationId():null));
		mailService.sendNotifyMailForJustAfterCommentSubmit(comment.getCommentId());
		
		return comment;
	}

	/**
	 * 指定したコメントをインデックス情報から返します。
	 * @param commentId コメントID
	 * @return コメント
	 */
	@Override
	public CommentDO getCommentFromIndex(String commentId, boolean includeDeleteContents) {
		return commentDao.loadCommentFromIndex(commentId, includeDeleteContents);
	}
	
	@Override
	public long moreCountImageCommentByImageId(
			String contentsId,
			List<String> 
			excludeCommentIds, 
			Date offsetTime, 
			boolean previous) {
		return moreCountCommentByContentsId(
				CommentTargetType.IMAGE, 
				contentsId, 
				excludeCommentIds, 
				offsetTime, 
				previous);
	}

	@Override
	public long moreCountReviewCommentByReviewId(
			String contentsId,
			List<String> excludeCommentIds, 
			Date offsetTime, 
			boolean previous) {
		return moreCountCommentByContentsId(
				CommentTargetType.REVIEW, 
				contentsId, 
				excludeCommentIds, 
				offsetTime, 
				previous);
	}

	@Override
	public long moreCountQuestionAnswerCommentByQuestionAnswerId(
			String contentsId,
			List<String> excludeCommentIds,
			Date offsetTime,
			boolean previous) {
		return moreCountCommentByContentsId(
				CommentTargetType.QUESTION_ANSWER, 
				contentsId, 
				excludeCommentIds, 
				offsetTime, 
				previous);
	}

	
	
	private long moreCountCommentByContentsId(
			CommentTargetType type,
			String contentsId,
			List<String> excludeCommentIds,
			Date offsetTime,
			boolean previous){
		return commentDao.moreCountByContentsId(
				type, 
				contentsId, 
				excludeCommentIds, 
				offsetTime, 
				previous);
	}

	@Override
	public SearchResult<CommentSetVO> loadCommentSet(String commentId) {
		CommentDO loadComment = commentDao.loadComment(commentId);
		if (ProductUtil.invalid(loadComment)) {
			return new SearchResult<CommentSetVO>();
		}
		List<CommentDO> documents = new ArrayList<CommentDO>();
		documents.add(loadComment);
		SearchResult<CommentDO> searchResult = new SearchResult<CommentDO>(documents.size(), documents);
		SearchResult<CommentSetVO> result = new SearchResult<CommentSetVO>();
		result.setHasAdult(searchResult.isHasAdult());
		result.setNumFound(searchResult.getNumFound());
		String communityUserId = requestScopeDao.loadCommunityUserId();
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(searchResult.getDocuments());
		for (CommentDO comment : searchResult.getDocuments()) {
			CommentSetVO vo = new CommentSetVO();
			vo.setComment(comment);
			if (communityUserId != null) {
				vo.setCommentFlg(communityUserId.equals(comment.getCommunityUser().getCommunityUserId()));
			}
			result.updateFirstAndLast(vo);
			if (comment.isStop(communityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				continue;
			}
			result.getDocuments().add(vo);
		}
		return result;
	}

	@Override
	public CommentDO loadComment(String commentId) {
		CommentDO comment = commentDao.loadComment(commentId);
		if (ProductUtil.invalid(comment)) {
			return null;
		} else {
			return comment;
		}
	}

	@Override
	public long loadImageCommentCount(String imageId) {
		List<String> imageIds = Lists.newArrayList();
		if( StringUtils.isBlank(imageId))
			return 0;
		
		imageIds.add(imageId);
		
		Map<String, Long> imageCommentCountMap = commentDao.loadImageCommentCountMap(imageIds);
		if( imageCommentCountMap.containsKey(imageId) ){
			return imageCommentCountMap.get(imageId);
		}
		
		return 0;
	}
	
}
