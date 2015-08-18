/**
 *
 */
package com.kickmogu.yodobashi.community.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.yodobashi.community.common.exception.UnActiveException;
import com.kickmogu.yodobashi.community.resource.dao.CommentDao;
import com.kickmogu.yodobashi.community.resource.dao.ImageDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionAnswerDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionDao;
import com.kickmogu.yodobashi.community.resource.dao.ReviewDao;
import com.kickmogu.yodobashi.community.resource.dao.SpamReportDao;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.SpamReportDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommentTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportTargetType;
import com.kickmogu.yodobashi.community.service.IndexService;
import com.kickmogu.yodobashi.community.service.SpamReportService;
import com.kickmogu.yodobashi.community.service.UserService;

/**
 * 違反報告サービスの実装です。
 * @author kamiike
 *
 */
@Service
public class SpamReportServiceImpl implements SpamReportService {

	/**
	 * 違反報告 DAO です。
	 */
	@Autowired
	private SpamReportDao spamReportDao;

	/**
	 * インデックスサービスです。
	 */
	@Autowired
	private IndexService indexService;


	@Autowired
	private UserService userService;

	@Autowired private ReviewDao reviewDao;

	@Autowired private QuestionDao questionDao;

	@Autowired private QuestionAnswerDao questionAnswerDao;

	@Autowired private ImageDao imageDao;

	@Autowired private CommentDao commentDao;

	/**
	 * 違反報告を登録します。
	 * @param spamReport 違反報告
	 * @return 違反報告
	 */
	@Override
	@ArroundHBase
	public SpamReportDO createSpamReport(SpamReportDO spamReport) {
		// コンテンツ投稿可能チェック
		if(!userService.validateUserStatusForPostContents(spamReport.getCommunityUser().getCommunityUserId()))
			throw new UnActiveException("can not post contens because user status is failure " + " communityUserId:" + spamReport.getCommunityUser().getCommunityUserId());

		if(spamReport.getTargetType().equals(SpamReportTargetType.REVIEW)){
			ReviewDO review = reviewDao.loadReview(spamReport.getReview().getReviewId());
			if (review == null
					|| review.getCommunityUser() == null
					|| (review.getCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
						&& !review.getCommunityUser().isKeepReviewContents())) {
				throw new UnActiveException("can not submit comment review because deleted communityUser reviewId:" + spamReport.getReview().getReviewId());
			}
			spamReport.setReview(review);
		}else if(spamReport.getTargetType().equals(SpamReportTargetType.QUESTION)){
			QuestionDO question = questionDao.loadQuestion(spamReport.getQuestion().getQuestionId());
			if (question == null
					|| question.getCommunityUser() == null
					|| (question.getCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
						&& !question.getCommunityUser().isKeepReviewContents())) {
				throw new UnActiveException("can not submit comment review because deleted communityUser reviewId:" + spamReport.getReview().getReviewId());
			}
			spamReport.setQuestion(question);
		}else if(spamReport.getTargetType().equals(SpamReportTargetType.QUESTION_ANSWER)){
			QuestionAnswerDO questionAnswer = questionAnswerDao.loadQuestionAnswer(spamReport.getQuestionAnswer().getQuestionAnswerId());
			if (questionAnswer == null
					|| questionAnswer.getCommunityUser() == null
					|| (questionAnswer.getCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
						&& !questionAnswer.getCommunityUser().isKeepQuestionContents())
					) {
				throw new UnActiveException("can not submit comment review because deleted communityUser questionAnswerId:" + spamReport.getQuestionAnswer().getQuestionAnswerId());
			}
			QuestionDO question = questionDao.loadQuestion(questionAnswer.getQuestion().getQuestionId());
			if (question == null
					|| question.getCommunityUser() == null
					|| (question.getCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
						&& !question.getCommunityUser().isKeepQuestionContents())
					) {
				throw new UnActiveException("can not submit comment question because deleted communityUser questionId:" + questionAnswer.getQuestion().getQuestionId());
			}
			spamReport.setQuestion(question);
			spamReport.setQuestionAnswer(questionAnswer);
		}else if(spamReport.getTargetType().equals(SpamReportTargetType.IMAGE)){
			ImageHeaderDO imageHeader = imageDao.loadImageHeader(spamReport.getImageHeader().getImageId());
			if (imageHeader == null
					|| imageHeader.getOwnerCommunityUser() == null
					|| (imageHeader.getOwnerCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
						&& !imageHeader.getOwnerCommunityUser().isKeepImageContents())
					) {
				throw new UnActiveException("can not submit comment review because deleted communityUser imageId:" + spamReport.getImageHeader().getImageId());
			}
			spamReport.setImageHeader(imageHeader);
		}else if(spamReport.getTargetType().equals(SpamReportTargetType.COMMENT)){
			CommentDO comment = commentDao.loadComment(spamReport.getComment().getCommentId());
			if (comment == null
					|| comment.getCommunityUser() == null
					|| (comment.getCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
						&& !comment.getCommunityUser().isKeepImageContents())
					) {
				throw new UnActiveException("can not submit comment review because deleted communityUser imageId:" + spamReport.getImageHeader().getImageId());
			}

			if(comment.getTargetType().equals(CommentTargetType.REVIEW)){
				ReviewDO review = reviewDao.loadReview(comment.getReview().getReviewId());
				if (review == null
						|| review.getCommunityUser() == null
						|| (review.getCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
							&& !review.getCommunityUser().isKeepReviewContents())) {
					throw new UnActiveException("can not submit comment review because deleted communityUser reviewId:" + spamReport.getReview().getReviewId());
				}
				spamReport.setReview(review);
			}else if(comment.getTargetType().equals(CommentTargetType.QUESTION_ANSWER)){
				QuestionAnswerDO questionAnswer = questionAnswerDao.loadQuestionAnswer(comment.getQuestionAnswer().getQuestionAnswerId());
				if (questionAnswer == null
						|| questionAnswer.getCommunityUser() == null
						|| (questionAnswer.getCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
							&& !questionAnswer.getCommunityUser().isKeepQuestionContents())
						) {
					throw new UnActiveException("can not submit comment review because deleted communityUser questionAnswerId:" + spamReport.getQuestionAnswer().getQuestionAnswerId());
				}
				QuestionDO question = questionDao.loadQuestion(questionAnswer.getQuestion().getQuestionId());
				if (question == null
						|| question.getCommunityUser() == null
						|| (question.getCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
							&& !question.getCommunityUser().isKeepQuestionContents())
						) {
					throw new UnActiveException("can not submit comment question because deleted communityUser questionId:" + questionAnswer.getQuestion().getQuestionId());
				}
				spamReport.setQuestion(question);
				spamReport.setQuestionAnswer(questionAnswer);
			}else if(comment.getTargetType().equals(CommentTargetType.IMAGE)){
				ImageHeaderDO imageHeader = imageDao.loadImageHeader(comment.getImageHeader().getImageId());
				if (imageHeader == null
						|| imageHeader.getOwnerCommunityUser() == null
						|| (imageHeader.getOwnerCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
							&& !imageHeader.getOwnerCommunityUser().isKeepImageContents())
						) {
					throw new UnActiveException("can not submit comment review because deleted communityUser imageId:" + spamReport.getImageHeader().getImageId());
				}
				spamReport.setImageHeader(imageHeader);
				spamReport.setRelationImageOwnerId(imageHeader.getOwnerCommunityUserId());
			}
			spamReport.setComment(comment);
		}
		spamReportDao.create(spamReport);
		indexService.updateIndexForSaveSpamReport(spamReport.getSpamReportId());
		return spamReport;
	}


	@Override
	@ArroundHBase
	public SpamReportDO updateCheckInitialDate(String spamReportId) {
		return spamReportDao.updateCheckInitialDate(spamReportId);
	}

	@Override
	public SearchResult<SpamReportDO> findSpamReports(Date fromSpamReportDate,
			Date toSpamReportDate, SpamReportStatus status, int limit,
			int offset) {
		return spamReportDao.findSpamReports(fromSpamReportDate, toSpamReportDate, status, limit, offset);
	}


	@Override
	public SpamReportDO getSpamReportById(String spamReportId) {
		return spamReportDao.getSpamReportById(spamReportId);
	}
}
