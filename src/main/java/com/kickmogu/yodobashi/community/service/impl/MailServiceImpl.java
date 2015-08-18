/**
 *
 */
package com.kickmogu.yodobashi.community.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.resource.dao.CommentDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.ImageDao;
import com.kickmogu.yodobashi.community.resource.dao.MailSettingDao;
import com.kickmogu.yodobashi.community.resource.dao.OrderDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductMasterDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionAnswerDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.ReviewDao;
import com.kickmogu.yodobashi.community.resource.dao.SendMailDao;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.MailInfoDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.AsyncMessageType;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommentTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSendTiming;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSettingType;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailType;
import com.kickmogu.yodobashi.community.resource.domain.constants.Verification;
import com.kickmogu.yodobashi.community.service.MailService;
import com.kickmogu.yodobashi.community.service.annotation.SendMessage;
import com.kickmogu.yodobashi.community.service.annotation.SendMessage.Timing;
import com.kickmogu.yodobashi.community.service.config.ServiceConfig;

/**
 * メール送信サービスの実装です。
 * @author kamiike
 */
@Service
public class MailServiceImpl implements MailService {

	/**
	 * コメント DAO です。
	 */
	@Autowired
	private CommentDao commentDao;

	/**
	 * コミュニティユーザー DAO です。
	 */
	@Autowired
	private CommunityUserDao communityUserDao;

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
	 * メール設定 DAO です。
	 */
	@Autowired
	private MailSettingDao mailSettingDao;

	/**
	 * 注文 DAO です。
	 */
	@Autowired
	private OrderDao orderDao;

	/**
	 * 商品 DAO です。
	 */
	@Autowired @Qualifier("catalog")
	private ProductDao productDao;

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
	 * 質問 DAO です。
	 */
	@Autowired
	private QuestionDao questionDao;

	/**
	 * 質問回答 DAO です。
	 */
	@Autowired
	private QuestionAnswerDao questionAnswerDao;

	/**
	 * 質問フォロー DAO です。
	 */
	@Autowired
	private QuestionFollowDao questionFollowDao;

	/**
	 * レビュー DAO です。
	 */
	@Autowired
	private ReviewDao reviewDao;

	/**
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	private RequestScopeDao requestScopeDao;

	/**
	 * メール送信 DAO です。
	 */
	@Autowired @Qualifier("xi")
	private SendMailDao sendMailDao;

	/**
	 * サービスコンフィグです。
	 */
	@Autowired
	private ServiceConfig serviceConfig;

	/**
	 * コメント投稿直後に関係する通知メールを送信します。
	 * @param commentId コメントID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.DELAYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void sendNotifyMailForJustAfterCommentSubmit(
			String commentId) {
		CommentDO comment = commentDao.loadComment(commentId,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId," +
						"review.communityUser.communityUserId," +
						"review.product.sku," +
						"questionAnswer.question.communityUser.communityUserId," +
						"questionAnswer.communityUser.communityUserId," +
						"questionAnswer.product.sku," +
						"imageHeader.ownerCommunityUser.communityUserId," +
						"imageHeader.product.sku," +
						"imageHeader.review.reviewId," +
						"imageHeader.question.questionId," +
						"imageHeader.questionAnswer.questionAnswerId").depth(3));
		if (comment == null || comment.isDeleted() || comment.getCommunityUser().isStop()) {
			return;
		}
		//hasAdult対応対象です。

		if (comment.getTargetType().equals(CommentTargetType.REVIEW)) {
			String targetCommunityUserId = comment.getReview().getCommunityUser().getCommunityUserId();
			if (!targetCommunityUserId.equals(comment.getCommunityUser().getCommunityUserId()) &&
					comment.getReview().getCommunityUser().isActive() &&
					mailSettingDao.loadMailSettingValueWithDefault(
					targetCommunityUserId, MailSettingType.REVIEW_COMMENT
					).equals(MailSendTiming.EVERYTIME_NOTIFY)) {
				if (!(comment.getReview().isAdult() &&
						!comment.getReview().getCommunityUser().getAdultVerification().equals(Verification.AUTHORIZED))) {
					Map<String, Object> dataMap = new HashMap<String, Object>();
					dataMap.put("toUser", comment.getReview().getCommunityUser());
					dataMap.put("review", comment.getReview());
					dataMap.put("comment", comment);
					dataMap.put("serviceConfig", serviceConfig);
					MailInfoDO mailInfo = MailType.REVIEW_COMMENT.createMailInfo(
							comment.getReview().getCommunityUser(), dataMap);
					sendMailDao.sendMail(mailInfo);
				}
			}

		} else if (comment.getTargetType().equals(CommentTargetType.QUESTION_ANSWER)) {
			String targetCommunityUserId = comment.getQuestionAnswer(
					).getCommunityUser().getCommunityUserId();
			if (!targetCommunityUserId.equals(comment.getCommunityUser(
					).getCommunityUserId()) &&
					comment.getQuestionAnswer().getCommunityUser().isActive() &&
					mailSettingDao.loadMailSettingValueWithDefault(
					targetCommunityUserId, MailSettingType.ANSWER_COMMENT
					).equals(MailSendTiming.EVERYTIME_NOTIFY)) {
				if (!(comment.getQuestionAnswer().isAdult() &&
						!comment.getQuestionAnswer().getCommunityUser().getAdultVerification().equals(Verification.AUTHORIZED))) {
					Map<String, Object> dataMap = new HashMap<String, Object>();
					dataMap.put("toUser", comment.getQuestionAnswer().getCommunityUser());
					dataMap.put("questionAnswer", comment.getQuestionAnswer());
					dataMap.put("comment", comment);
					dataMap.put("serviceConfig", serviceConfig);
					MailInfoDO mailInfo = MailType.QUESTION_ANSWER_COMMENT.createMailInfo(
							comment.getQuestionAnswer().getCommunityUser(), dataMap);
					sendMailDao.sendMail(mailInfo);
				}
			}

		} else if (comment.getTargetType().equals(CommentTargetType.IMAGE)) {
			String targetCommunityUserId = comment.getImageHeader().getOwnerCommunityUser().getCommunityUserId();
			if (!targetCommunityUserId.equals(comment.getCommunityUser(
					).getCommunityUserId()) &&
					comment.getImageHeader().getOwnerCommunityUser().isActive() &&
					mailSettingDao.loadMailSettingValueWithDefault(
					targetCommunityUserId, MailSettingType.IMAGE_COMMENT
					).equals(MailSendTiming.EVERYTIME_NOTIFY)) {
				if (!(comment.getImageHeader().isAdult() &&
						!comment.getImageHeader().getOwnerCommunityUser().getAdultVerification().equals(Verification.AUTHORIZED))) {
					ProductDO product = productDao.loadProduct(comment.getImageHeader().getSku());
					Map<String, Object> dataMap = new HashMap<String, Object>();
					dataMap.put("toUser", comment.getImageHeader().getOwnerCommunityUser());
					dataMap.put("image", comment.getImageHeader());
					comment.setProduct(product);
					dataMap.put("comment", comment);
					dataMap.put("serviceConfig", serviceConfig);
					MailInfoDO mailInfo = MailType.IMAGE_COMMENT.createMailInfo(
							comment.getImageHeader().getOwnerCommunityUser(), dataMap);
					sendMailDao.sendMail(mailInfo);
				}
			}
		}
	}
	/**
	 * コミュニティユーザーのフォロー通知メールを送信します。
	 * @param communityUserId コミュニティユーザーID
	 * @param followCommunityUserId フォローするコミュニティユーザーID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.DELAYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void sendFollowCommunityUserNotifyMail(
			String communityUserId,
			String followCommunityUserId) {
		CommunityUserDO communityUser = communityUserDao.load(communityUserId, Path.DEFAULT);
		if (communityUser.isStop()) {
			return;
		}
		CommunityUserDO followCommunityUser = communityUserDao.load(followCommunityUserId, Path.DEFAULT);
		if (followCommunityUser == null || !followCommunityUser.isActive()) {
			return;
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", followCommunityUser);
		dataMap.put("followerUser", communityUser);
		dataMap.put("serviceConfig", serviceConfig);

		requestScopeDao.loadAdultVerification();

		dataMap.put("reviewCount",
				reviewDao.countReviewByCommunityUserId(
						communityUserId,
						null,
						new ContentsStatus[]{ContentsStatus.SUBMITTED},
						communityUser.getAdultVerification()));
		dataMap.put("questionCount",
				questionDao.countQuestionByCommunityUserId(
						communityUserId,
						null,
						new ContentsStatus[]{ContentsStatus.SUBMITTED},
						communityUser.getAdultVerification()));
		dataMap.put("answerCount",
				questionAnswerDao.countPostQuestionAnswerCount(
						communityUserId, ContentsStatus.SUBMITTED,
						communityUser.getAdultVerification()));
		dataMap.put("imageCount",
				imageDao.countImageSetByCommunityUserId(
						communityUserId,
						null,
						new ContentsStatus[]{ContentsStatus.SUBMITTED},
						communityUser.getAdultVerification()));
		dataMap.put("followCount",
				communityUserFollowDao.findFollowCommunityUserByCommunityUserId(
						communityUserId, followCommunityUserId, 0, 0).getNumFound() + 1);

		dataMap.put("productMasters",
				productMasterDao.findRankProductMasterByCommunityUserId(
						communityUserId, 5,
						null, null, false, true,
						communityUser.getAdultVerification()));
		MailInfoDO mailInfo = MailType.USER_FOLLOW.createMailInfo(
				followCommunityUser, dataMap);
		sendMailDao.sendMail(mailInfo);
	}

	/**
	 * 質問投稿直後に関係する通知メールを送信します。
	 * @param questionId 質問
	 * @param sku SKU
	 * @param communityUserId コミュニティユーザーID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.DELAYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void sendNotifyMailForJustAfterQuestionSubmit(
			String questionId, String sku, String communityUserId) {
		int limit = serviceConfig.readLimit;
		int offset = 0;

		QuestionDO question = questionDao.loadQuestion(
				questionId, Path.includeProp("*").includePath("product.sku,communityUser.communityUserId").depth(1), false);
		if (question == null || question.isDeleted()
				|| question.getCommunityUser().isStop()) {
			return;
		}
		//hasAdult対応対象です。

		//購入商品の新着QA質問
		while (true) {
			SearchResult<CommunityUserDO> searchResult = orderDao.findOrderCommunityUserBySKU(
					sku, null, limit, offset, true, false);
			for (CommunityUserDO targetCommunityUser : searchResult.getDocuments()) {
				offset++;
				if (question.isAdult() &&
						!targetCommunityUser.getAdultVerification().equals(Verification.AUTHORIZED)) {
					continue;
				}
				if (!targetCommunityUser.getCommunityUserId().equals(communityUserId)
						&& targetCommunityUser.isActive()
						&& mailSettingDao.loadMailSettingValueWithDefault(
						targetCommunityUser.getCommunityUserId(),
						MailSettingType.PURCHASE_PRODUCT_QUESTION
						).equals(MailSendTiming.EVERYTIME_NOTIFY)) {
					sendNewQuestionMail(targetCommunityUser, question,
							MailType.NEW_QUESTION_FOR_PURCHASE_PRODUCT, true);
				}
			}
			if (searchResult.getDocuments().size() < limit || searchResult.getNumFound() <= offset) {
				break;
			}
		}

		//フォローした商品の新着QA
		offset = 0;
		while (true) {
			SearchResult<CommunityUserDO> searchResult = productFollowDao.findFollowerCommunityUserBySKU(
					sku, limit, offset, true);
			for (CommunityUserDO targetCommunityUser : searchResult.getDocuments()) {
				offset++;
				if (question.isAdult() &&
						!targetCommunityUser.getAdultVerification().equals(Verification.AUTHORIZED)) {
					continue;
				}
				if (!targetCommunityUser.getCommunityUserId().equals(communityUserId)
						&& targetCommunityUser.isActive()
						&& mailSettingDao.loadMailSettingValueWithDefault(
						targetCommunityUser.getCommunityUserId(),
						MailSettingType.FOLLOW_PRODUCT_QUESTION
						).equals(MailSendTiming.EVERYTIME_NOTIFY)) {
					sendNewQuestionMail(targetCommunityUser, question,
							MailType.FOLLOW_PRODUCT_NEW_QUESTION, false);
				}
			}
			if (searchResult.getDocuments().size() < limit || searchResult.getNumFound() <= offset) {
				break;
			}
		}

		//フォローしたユーザのQA質問
		offset = 0;
		while (true) {
			SearchResult<CommunityUserDO> searchResult = communityUserFollowDao.findFollowerCommunityUserByCommunityUserId(
					communityUserId, null, limit, offset);
			for (CommunityUserDO targetCommunityUser : searchResult.getDocuments()) {
				offset++;
				if (question.isAdult() &&
						!targetCommunityUser.getAdultVerification().equals(Verification.AUTHORIZED)) {
					continue;
				}
				if (targetCommunityUser.isActive()
						&& mailSettingDao.loadMailSettingValueWithDefault(
						targetCommunityUser.getCommunityUserId(),
						MailSettingType.FOLLOW_USER_QUESTION
						).equals(MailSendTiming.EVERYTIME_NOTIFY)) {
					sendNewQuestionMail(targetCommunityUser, question,
							MailType.FOLLOW_USER_NEW_QUESTION, false);
				}
			}
			if (searchResult.getDocuments().size() < limit || searchResult.getNumFound() <= offset) {
				break;
			}
		}
	}

	/**
	 * 質問回答投稿直後に関係する通知メールを送信します。
	 * @param questionAnswerId 質問回答ID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.DELAYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void sendNotifyMailForJustAfterQuestionAnswerSubmit(
			String questionAnswerId) {
		int limit = serviceConfig.readLimit;
		int offset = 0;

		//hasAdult対応対象です。

		QuestionAnswerDO questionAnswer = questionAnswerDao.loadQuestionAnswer(
				questionAnswerId,
				Path.includeProp("*").includePath(
						"question.communityUser.communityUserId," +
						"product.sku," +
						"communityUser.communityUserId," +
						"status,withdraw").depth(2), false);
		if (questionAnswer == null || questionAnswer.isDeleted() || questionAnswer.getStatus().equals(ContentsStatus.CONTENTS_STOP)
				|| questionAnswer.getCommunityUser().isStop()) {
			return;
		}

		//質問に対して回答がついたとき
		if (!questionAnswer.getCommunityUser().getCommunityUserId().equals(
				questionAnswer.getQuestion().getCommunityUser().getCommunityUserId())
				&& questionAnswer.getQuestion().getCommunityUser().isActive()
				&& mailSettingDao.loadMailSettingValueWithDefault(
				questionAnswer.getQuestion(
						).getCommunityUser().getCommunityUserId(),
						MailSettingType.QUESTION_ANSWER
				).equals(MailSendTiming.EVERYTIME_NOTIFY)) {
			if (!(questionAnswer.isAdult() &&
					!questionAnswer.getQuestion(
					).getCommunityUser().getAdultVerification().equals(Verification.AUTHORIZED))) {
				sendNewQuestionAnswerMail(
						questionAnswer.getQuestion().getCommunityUser(),
						questionAnswer,
						MailType.QUESTION_ANSWER);
			}
		}

		//回答したQAに別の回答がついたとき
		while (true) {
			SearchResult<CommunityUserDO> searchResult
					= questionAnswerDao.findAnswerCommunityUserByQuestionId(
							questionAnswer.getQuestion().getQuestionId(),
							new ContentsStatus[]{ContentsStatus.SUBMITTED},
							questionAnswerId, true);
			for (CommunityUserDO targetCommunityUser : searchResult.getDocuments()) {
				offset++;
				if (questionAnswer.isAdult() &&
						!targetCommunityUser.getAdultVerification().equals(Verification.AUTHORIZED)) {
					continue;
				}
				if (!targetCommunityUser.getCommunityUserId().equals(
						questionAnswer.getCommunityUser().getCommunityUserId())
						&& targetCommunityUser.isActive()
						&& mailSettingDao.loadMailSettingValueWithDefault(
						targetCommunityUser.getCommunityUserId(),
						MailSettingType.ANSWER_QUESTION_ANOTHER_ANSWER
						).equals(MailSendTiming.EVERYTIME_NOTIFY)) {
					sendNewQuestionAnswerMail(
							targetCommunityUser,
							questionAnswer,
							MailType.ANOTHER_QUESTION_ANSWER);
				}
			}
			if (searchResult.getDocuments().size() < limit || searchResult.getNumFound() <= offset) {
				break;
			}
		}

		//フォローしたQAの新着回答
		offset = 0;
		while (true) {
			SearchResult<CommunityUserDO> searchResult = questionFollowDao.findFollowerCommunityUserByQuestionId(
					questionAnswer.getQuestion().getQuestionId(), limit, offset, true);
			for (CommunityUserDO targetCommunityUser : searchResult.getDocuments()) {
				offset++;
				if (questionAnswer.isAdult() &&
						!targetCommunityUser.getAdultVerification().equals(Verification.AUTHORIZED)) {
					continue;
				}
				if (!targetCommunityUser.getCommunityUserId().equals(
						questionAnswer.getCommunityUser().getCommunityUserId())
						&& targetCommunityUser.isActive()
						&& mailSettingDao.loadMailSettingValueWithDefault(
						targetCommunityUser.getCommunityUserId(),
						MailSettingType.FOLLOW_QUESTION_ANSWER
						).equals(MailSendTiming.EVERYTIME_NOTIFY)) {
					sendNewQuestionAnswerMail(
							targetCommunityUser,
							questionAnswer,
							MailType.FOLLOW_QUESTION_NEW_ANSWER);
				}
			}
			if (searchResult.getDocuments().size() < limit || searchResult.getNumFound() <= offset) {
				break;
			}
		}

		//フォローしたユーザのQAの回答
		offset = 0;
		while (true) {
			SearchResult<CommunityUserDO> searchResult = communityUserFollowDao.findFollowerCommunityUserByCommunityUserId(
					questionAnswer.getCommunityUser().getCommunityUserId(), null, limit, offset);
			for (CommunityUserDO targetCommunityUser : searchResult.getDocuments()) {
				offset++;
				if (questionAnswer.isAdult() &&
						!targetCommunityUser.getAdultVerification().equals(Verification.AUTHORIZED)) {
					continue;
				}
				if (!targetCommunityUser.getCommunityUserId().equals(
						questionAnswer.getCommunityUser().getCommunityUserId())
						&& targetCommunityUser.isActive()
						&& mailSettingDao.loadMailSettingValueWithDefault(
						targetCommunityUser.getCommunityUserId(),
						MailSettingType.FOLLOW_USER_QUESTION_ANSWER
						).equals(MailSendTiming.EVERYTIME_NOTIFY)) {
					sendNewQuestionAnswerMail(
							targetCommunityUser,
							questionAnswer,
							MailType.FOLLOW_USER_NEW_ANSWER);
				}
			}
			if (searchResult.getDocuments().size() < limit || searchResult.getNumFound() <= offset) {
				break;
			}
		}
	}

	/**
	 * レビュー投稿直後に関係する通知メールを送信します。
	 * @param reviewId レビュー
	 * @param sku SKU
	 * @param communityUserId コミュニティユーザーID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.DELAYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void sendNotifyMailForJustAfterReviewSubmit(
			String reviewId, String sku, String communityUserId) {
		int limit = serviceConfig.readLimit;
		int offset = 0;

		ReviewDO review = reviewDao.loadReview(
				reviewId,
				Path.includeProp("*").includePath(
						"product.sku," +
						"communityUser.communityUserId").depth(1), false);
		if (review == null 
				|| review.isDeleted() 
				|| ContentsStatus.CONTENTS_STOP.equals(review.getStatus())
				|| review.getCommunityUser() == null
				|| review.getCommunityUser().isStop()  ) {
			return;
		}
		//hasAdult対応対象です。
		
		Set<String> duplicateCommunityUserId = new HashSet<String>();
		
		//レビューを書いた商品に別のレビューが投稿されたとき
		while (true) {
			SearchResult<CommunityUserDO> searchResult =reviewDao.findReviwerExcludeReviewIdBySKU(
					sku, reviewId, new ContentsStatus[]{ContentsStatus.SUBMITTED}, limit, offset);
			for (CommunityUserDO targetCommunityUser : searchResult.getDocuments()) {
				offset++;
				if(duplicateCommunityUserId.contains(targetCommunityUser.getCommunityUserId())){
					continue;
				}
				if (review.isAdult() &&
						!targetCommunityUser.getAdultVerification().equals(Verification.AUTHORIZED)) {
					continue;
				}
				if (!targetCommunityUser.getCommunityUserId().equals(
						review.getCommunityUser().getCommunityUserId())
						&& targetCommunityUser.isActive()
						&& mailSettingDao.loadMailSettingValueWithDefault(
						targetCommunityUser.getCommunityUserId(),
						MailSettingType.REVIEW_PRODUCT_ANOTHER_REVIEW
						).equals(MailSendTiming.EVERYTIME_NOTIFY)) {
					sendNewReviewMail(targetCommunityUser, review, MailType.ANOTHER_REVIEW);
					duplicateCommunityUserId.add(targetCommunityUser.getCommunityUserId());
				}
			}
			if (searchResult.getDocuments().size() < limit || searchResult.getNumFound() <= offset) {
				break;
			}
		}

		//フォローした商品の新着レビュー
		offset = 0;
		while (true) {
			SearchResult<CommunityUserDO> searchResult = productFollowDao.findFollowerCommunityUserBySKU(
					sku, limit, offset, true);
			for (CommunityUserDO targetCommunityUser : searchResult.getDocuments()) {
				offset++;
				if (review.isAdult() &&
						!targetCommunityUser.getAdultVerification().equals(Verification.AUTHORIZED)) {
					continue;
				}
				if (!targetCommunityUser.getCommunityUserId().equals(
						review.getCommunityUser().getCommunityUserId())
						&& targetCommunityUser.isActive()
						&& mailSettingDao.loadMailSettingValueWithDefault(
						targetCommunityUser.getCommunityUserId(),
						MailSettingType.FOLLOW_PRODUCT_REVIEW
						).equals(MailSendTiming.EVERYTIME_NOTIFY)) {
					sendNewReviewMail(targetCommunityUser, review,
							MailType.FOLLOW_PRODUCT_NEW_REVIEW);
				}
			}
			if (searchResult.getDocuments().size() < limit || searchResult.getNumFound() <= offset) {
				break;
			}
		}

		//フォローしたユーザのレビュー
		offset = 0;
		while (true) {
			SearchResult<CommunityUserDO> searchResult = communityUserFollowDao.findFollowerCommunityUserByCommunityUserId(
					communityUserId, null, limit, offset);
			for (CommunityUserDO targetCommunityUser : searchResult.getDocuments()) {
				offset++;
				if (review.isAdult() &&
						!targetCommunityUser.getAdultVerification().equals(Verification.AUTHORIZED)) {
					continue;
				}
				if (targetCommunityUser.isActive()
						&& mailSettingDao.loadMailSettingValueWithDefault(
						targetCommunityUser.getCommunityUserId(),
						MailSettingType.FOLLOW_USER_REVIEW
						).equals(MailSendTiming.EVERYTIME_NOTIFY)) {
					sendNewReviewMail(targetCommunityUser, review,
							MailType.FOLLOW_USER_NEW_REVIEW);
				}
			}
			if (searchResult.getDocuments().size() < limit || searchResult.getNumFound() <= offset) {
				break;
			}
		}
	}

	/**
	 * 画像投稿直後に関係する通知メールを送信します。
	 * @param imageSetId 画像セットID
	 * @param sku SKU
	 * @param communityUserId コミュニティユーザーID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.DELAYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void sendNotifyMailForJustAfterImageSubmit(
			String imageSetId, String sku, String communityUserId) {
		int limit = serviceConfig.readLimit;
		int offset = 0;
		List<ImageHeaderDO> imageHeaders = new ArrayList<ImageHeaderDO>();
		List<ImageHeaderDO> loadImageHeaders = imageDao.loadImages(
				imageSetId, 
				Path.includeProp("*").
				includePath(
						"ownerCommunityUser.communityName," +
						"review.reviewId," +
						"question.questionId," +
						"questionAnswer.questionAnswerId"
				).depth(1));
		if (loadImageHeaders == null || loadImageHeaders.size() == 0) {
			return;
		}
		for(ImageHeaderDO image:loadImageHeaders) {
			if(image.getStatus().equals(ContentsStatus.SUBMITTED))
				imageHeaders.add(image);
		}
		
		ImageHeaderDO topImage = imageHeaders.get(0);
		if (topImage.getOwnerCommunityUser().isStop()) {
			return;
		}
		ProductDO product = productDao.loadProduct(sku);

		//フォローした商品の新着画像
		while (true) {
			SearchResult<CommunityUserDO> searchResult = productFollowDao.findFollowerCommunityUserBySKU(
					sku, limit, offset, true);
			for (CommunityUserDO targetCommunityUser : searchResult.getDocuments()) {
				offset++;
				if (topImage.isAdult() &&
						!targetCommunityUser.getAdultVerification().equals(Verification.AUTHORIZED)) {
					continue;
				}
				if (!targetCommunityUser.getCommunityUserId().equals(
						topImage.getOwnerCommunityUser().getCommunityUserId())
						&& targetCommunityUser.isActive()
						&& mailSettingDao.loadMailSettingValueWithDefault(
						targetCommunityUser.getCommunityUserId(),
						MailSettingType.FOLLOW_PRODUCT_IMAGE
						).equals(MailSendTiming.EVERYTIME_NOTIFY)) {
					sendNewImageMail(targetCommunityUser, product, topImage, imageHeaders,
							MailType.FOLLOW_PRODUCT_NEW_IMAGE);
				}
			}
			if (searchResult.getDocuments().size() < limit || searchResult.getNumFound() <= offset) {
				break;
			}
		}

		//フォローしたユーザの画像
		offset = 0;
		while (true) {
			SearchResult<CommunityUserDO> searchResult = communityUserFollowDao.findFollowerCommunityUserByCommunityUserId(
					communityUserId, null, limit, offset);
			for (CommunityUserDO targetCommunityUser : searchResult.getDocuments()) {
				offset++;
				if (topImage.isAdult() &&
						!targetCommunityUser.getAdultVerification().equals(Verification.AUTHORIZED)) {
					continue;
				}
				if (targetCommunityUser.isActive()
						&& mailSettingDao.loadMailSettingValueWithDefault(
						targetCommunityUser.getCommunityUserId(),
						MailSettingType.FOLLOW_USER_IMAGE
						).equals(MailSendTiming.EVERYTIME_NOTIFY)) {
					sendNewImageMail(targetCommunityUser, product, topImage, imageHeaders,
							MailType.FOLLOW_USER_NEW_IMAGE);
				}
			}
			if (searchResult.getDocuments().size() < limit || searchResult.getNumFound() <= offset) {
				break;
			}
		}
	}

	/**
	 * 登録完了メールを送信します。
	 * @param communityUserId コミュニティユーザーID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.DELAYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void sendRegistrationCompleteMail(String communityUserId) {
		CommunityUserDO communityUser = communityUserDao.load(communityUserId, Path.DEFAULT);
		if (communityUser == null || !communityUser.isActive()) {
			return;
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", communityUser);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = MailType.USER_REGISTER.createMailInfo(
				communityUser, dataMap);
		sendMailDao.sendMail(mailInfo);
	}

	/**
	 * 一時停止通知メールを送信します。
	 * @param communityUserId コミュニティユーザーID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.DELAYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void sendStopNotifyMail(String communityUserId) {
		CommunityUserDO communityUser = communityUserDao.load(communityUserId, Path.DEFAULT);
		if (communityUser == null || !communityUser.isActive()) {
			return;
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", communityUser);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = MailType.STOP_COMMUNITY_USER.createMailInfo(
				communityUser, dataMap);
		sendMailDao.sendMail(mailInfo);
	}

	/**
	 * 新着レビューのメールを送信します。
	 * @param targetCommunityUser 宛先ユーザー
	 * @param review レビュー
	 * @param mailType メールタイプ
	 */
	private void sendNewReviewMail(CommunityUserDO targetCommunityUser,
			ReviewDO review,
			MailType mailType) {
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", targetCommunityUser);
		dataMap.put("review", review);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = mailType.createMailInfo(
				targetCommunityUser, dataMap);
		sendMailDao.sendMail(mailInfo);
	}

	/**
	 * 新着QA質問のメールを送信します。
	 * @param targetCommunityUser 宛先ユーザー
	 * @param question 質問
	 * @param mailType メールタイプ
	 * @param requiredProductFollowingFlg 商品フォローフラグが必要かどうか
	 */
	private void sendNewQuestionMail(CommunityUserDO targetCommunityUser,
			QuestionDO question,
			MailType mailType,
			boolean requiredProductFollowingFlg) {
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", targetCommunityUser);
		dataMap.put("question", question);
		dataMap.put("serviceConfig", serviceConfig);
		if(requiredProductFollowingFlg){
			// メール受信者が質問の商品をフォローしているかどうか
			dataMap.put("followingFlg", productFollowDao.existsProductFollow(
					targetCommunityUser.getCommunityUserId(), question.getProduct().getSku()));
		}

		MailInfoDO mailInfo = mailType.createMailInfo(
				targetCommunityUser, dataMap);
		sendMailDao.sendMail(mailInfo);
	}

	/**
	 * 新着QA質問回答のメールを送信します。
	 * @param targetCommunityUser 宛先ユーザー
	 * @param questionAnswer 質問回答
	 * @param mailType メールタイプ
	 */
	private void sendNewQuestionAnswerMail(CommunityUserDO targetCommunityUser,
			QuestionAnswerDO questionAnswer,
			MailType mailType) {
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", targetCommunityUser);
		dataMap.put("questionAnswer", questionAnswer);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = mailType.createMailInfo(
				targetCommunityUser, dataMap);
		sendMailDao.sendMail(mailInfo);
	}

	/**
	 * 新着画像のメールを送信します。
	 * @param targetCommunityUser 宛先ユーザー
	 * @param product 商品
	 * @param topImage トップ画像
	 * @param imageHeaders 画像リスト
	 * @param mailType メールタイプ
	 */
	private void sendNewImageMail(CommunityUserDO targetCommunityUser,
			ProductDO product,
			ImageHeaderDO topImage,
			List<ImageHeaderDO> imageHeaders,
			MailType mailType) {
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", targetCommunityUser);
		dataMap.put("product", product);
		dataMap.put("topImage", topImage);
		dataMap.put("imageHeaders", imageHeaders);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = mailType.createMailInfo(
				targetCommunityUser, dataMap);
		sendMailDao.sendMail(mailInfo);
	}

}
