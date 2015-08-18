package com.kickmogu.yodobashi.community.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.DecisivePurchaseDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDecisivePurchaseDO;
import com.kickmogu.yodobashi.community.resource.domain.SpamReportDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommentTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportTargetType;

/**
 * 初期データを生成します。
 * @author hirabayashi
 *
 */
public class DataSetTest extends BaseTest {

	/**
	 * レビューサービスです。
	 */
	@Autowired
	protected ReviewService reviewService;

	/**
	 * 質問サービスです。
	 */
	@Autowired
	protected QuestionService questionService;

	/**
	 * フォローサービスです。
	 */
	@Autowired
	protected FollowService followService;

	/**
	 * コメントサービスです。
	 */
	@Autowired
	protected CommentService commentService;

	/**
	 * いいねサービスです。
	 */
	@Autowired
	protected LikeService likeService;

	/**
	 * 違反報告サービスです。
	 */
	@Autowired
	protected SpamReportService spamReportService;

	/**
	 * リクエストスコープで管理するオブジェクトを扱う DAO です。
	 */
	@Autowired
	protected RequestScopeDao requestScopeDao;

	/**
	 * コミュニティユーザーです。
	 */
	protected CommunityUserDO communityUser;
	protected CommunityUserDO commentUser;
	protected CommunityUserDO answerUser;
	protected CommunityUserDO likeUser;
	protected CommunityUserDO followerUser;
	protected CommunityUserDO productFollowUser;
	protected CommunityUserDO reportUser;

	/**
	 * レビューです。
	 */
	protected ReviewDO review;
	protected CommentDO reviewComment;

	/**
	 * 質問です。
	 */
	protected QuestionDO question;
	protected QuestionAnswerDO questionAnswer;
	protected CommentDO questionAnswerComment;

	/**
	 * 違反報告です。
	 */
	protected SpamReportDO questionSpamReport;
	protected SpamReportDO questionAnswerSpamReport;
	protected SpamReportDO questionCommentSpamReport;
	protected SpamReportDO reviewSpamReport;
	protected SpamReportDO reviewCommentSpamReport;

	/**
	 * データを初期化します。
	 */
	protected void initialize() {
		super.initialize();
		createCommunityUserSet();
		createReviewSet();
		createQuestionSet();
		createImage();
	}

	/**
	 * テストに使用するコミュニティユーザーを初期生成します。
	 */
	protected void createCommunityUserSet() {
		communityUser = createCommunityUser("communityUser", true);
		commentUser = createCommunityUser("commentUser", false);
		answerUser = createCommunityUser("answerUser", false);
		likeUser = createCommunityUser("likeUser", false);
		followerUser = createCommunityUser("followerUser", false);
		productFollowUser = createCommunityUser("productFollowUser", false);
		reportUser = createCommunityUser("reportUser", false);
		followService.followCommunityUser(
				followerUser.getCommunityUserId(),
				communityUser.getCommunityUserId(), false);
		followService.followCommunityUser(
				followerUser.getCommunityUserId(),
				commentUser.getCommunityUserId(), false);
		followService.followProduct(
				productFollowUser.getCommunityUserId(), "100000001000624829", false);
		// 商品を購入します。
		Date salesDate = new Date();
		createReceipt(communityUser, "4905524312737", salesDate);
		createReceipt(communityUser, "4905524372564", salesDate);
		createReceipt(communityUser, "4562215332070", salesDate);
		createReceipt(communityUser, "4988601007122", salesDate);
		createReceipt(commentUser, "4905524312737", salesDate);
		createReceipt(answerUser, "4905524312737", salesDate);
	}

	/**
	 * テストに使用するレビューを初期生成します。
	 */
	private void createReviewSet() {
		review = createReviewSet(communityUser);
		likeService.updateLikeReview(likeUser.getCommunityUserId(), review.getReviewId(), false);
		reviewComment = saveComment(review, commentUser);
		createSpamReportByReview(review);
		createSpamReportByReviewComment(review);
	}

	/**
	 * テストに使用する質問を初期生成します。
	 */
	private void createQuestionSet() {
		question = createQuestionSet(communityUser);
		questionAnswer = saveQuestionAnswer(question, answerUser);
		likeService.updateLikeQuestionAnswer(
				likeUser.getCommunityUserId(), questionAnswer.getQuestionAnswerId(), false);
		questionAnswerComment = saveCommentByQuestionAnswer(questionAnswer, commentUser);
	}

	/**
	 * 画像を登録します。
	 */
	protected ImageHeaderDO createImage() {
		List<ImageHeaderDO> imageHeaders = new ArrayList<ImageHeaderDO>();

		ImageHeaderDO imageHeader = new ImageHeaderDO();
		imageHeaders.add(imageHeader);
		ImageDO image = new ImageDO();
		image.setData(testImageData);
		image.setMimeType("images/jpeg");
		image.setTemporaryKey("test");
		image.setWidth(400);
		image.setHeigth(500);
		image.setCommunityUserId(communityUser.getCommunityUserId());
		imageService.createTemporaryImage(image);
		imageHeader.setImageId(image.getImageId());

		requestScopeDao.initialize(communityUser, null);
		imageService.saveImageSet(product.getSku(), imageHeaders, null);
		requestScopeDao.destroy();

		return imageHeader;
	}

	/**
	 * レビューを作成します。
	 * @param communityUser
	 */
	protected ReviewDO createReviewSet(CommunityUserDO communityUser) {
		ReviewDO review = new ReviewDO();
		review.setReviewType(ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE);
		review.setCommunityUser(communityUser);
		review.setProduct(product);
		ReviewDecisivePurchaseDO reviewDecisivePurchase1 = new ReviewDecisivePurchaseDO();
		reviewDecisivePurchase1.setDecisivePurchase(new DecisivePurchaseDO());
		reviewDecisivePurchase1.getDecisivePurchase().setDecisivePurchaseName("購入の決め手-デザイン");
		review.getReviewDecisivePurchases().add(reviewDecisivePurchase1);
		review.setStatus(ContentsStatus.SUBMITTED);
		StringBuilder html = new StringBuilder();
		createImage(communityUser);
		String tempImageUrl = resourceConfig.temporaryImageUrl + image.getImageId();
		html.append("<img src=\"" + tempImageUrl + "\" />");
		html.append("<script type=\"text/javascript\">alert('" + "レビュー本文" + "')</script>");
		review.setReviewBody(html.toString());
		review = reviewService.saveReview(review);
		return review;
	}
	
	/**
	 * レビューを作成します。
	 * @param communityUser
	 */
	protected ReviewDO createReviewSetByAdult(CommunityUserDO communityUser) {
		ReviewDO review = new ReviewDO();
		review.setReviewType(ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE);
		review.setCommunityUser(communityUser);
		review.setProduct(product3);
		ReviewDecisivePurchaseDO reviewDecisivePurchase1 = new ReviewDecisivePurchaseDO();
		reviewDecisivePurchase1.setDecisivePurchase(new DecisivePurchaseDO());
		reviewDecisivePurchase1.getDecisivePurchase().setDecisivePurchaseName("購入の決め手-デザイン");
		review.getReviewDecisivePurchases().add(reviewDecisivePurchase1);
		review.setStatus(ContentsStatus.SUBMITTED);
		StringBuilder html = new StringBuilder();
		createImage(communityUser);
		String tempImageUrl = resourceConfig.temporaryImageUrl + image.getImageId();
		html.append("<img src=\"" + tempImageUrl + "\" />");
		html.append("<script type=\"text/javascript\">alert('" + "レビュー本文" + "')</script>");
		review.setReviewBody(html.toString());
		review = reviewService.saveReview(review);
		return review;
	}

	/**
	 * レビューのコメントを登録します。
	 * @param review
	 * @return
	 */
	protected CommentDO saveComment(ReviewDO review, CommunityUserDO communityUser) {
		CommentDO reviewComment = new CommentDO();
		reviewComment.setCommunityUser(communityUser);
		reviewComment.setTargetType(CommentTargetType.REVIEW);
		reviewComment.setReview(review);
		reviewComment.setCommentBody("レビューコメント");
		reviewComment = commentService.saveComment(reviewComment);
		return reviewComment;
	}

	/**
	 * レビューの違反報告をします。
	 * @param review
	 */
	protected void createSpamReportByReview(ReviewDO review) {
		SpamReportDO spamReport = new SpamReportDO();
		spamReport.setCommunityUser(reportUser);
		spamReport.setReview(review);
		spamReport.setStatus(SpamReportStatus.NEW);
		spamReport.setSpamReportBody("レビュー違反報告");
		spamReport.setTargetType(SpamReportTargetType.REVIEW);
		reviewSpamReport = spamReportService.createSpamReport(spamReport);
	}

	/**
	 * レビューコメントの違反報告をします。
	 * @param review
	 */
	protected void createSpamReportByReviewComment(ReviewDO review) {
		SpamReportDO spamReport = new SpamReportDO();
		spamReport.setCommunityUser(reportUser);
		spamReport.setComment(reviewComment);
		spamReport.setStatus(SpamReportStatus.NEW);
		spamReport.setSpamReportBody("レビューコメント違反報告");
		spamReport.setTargetType(SpamReportTargetType.COMMENT);
		reviewCommentSpamReport = spamReportService.createSpamReport(spamReport);
	}

	/**
	 * 質問を登録します。
	 * @param communityUser
	 * @return
	 */
	protected QuestionDO createQuestionSet(CommunityUserDO communityUser) {
		QuestionDO question = new QuestionDO();
		questionSpamReport = new SpamReportDO();
		question.setProduct(product);
		question.setCommunityUser(communityUser);
		question.setStatus(ContentsStatus.SUBMITTED);
		StringBuilder html = new StringBuilder();
		createImage(commentUser);
		String tempImageUrl = resourceConfig.temporaryImageUrl + image.getImageId();
		html.append("<img src=\"" + tempImageUrl + "\" />");
		html.append("<script type=\"text/javascript\">alert('" + "質問本文" + "')</script>");
		question.setQuestionBody(html.toString());
		question = questionService.saveQuestion(question);
		return question;
	}

	/**
	 * 質問回答を登録します。
	 * @param question
	 * @param communityUser
	 * @return
	 */
	protected QuestionAnswerDO saveQuestionAnswer(QuestionDO question, CommunityUserDO communityUser) {
		QuestionAnswerDO questionAnswer = new QuestionAnswerDO();
		questionAnswer.setQuestion(question);
		questionAnswer.setCommunityUser(communityUser);
		createImage(answerUser);
		String tempImageUrl = resourceConfig.temporaryImageUrl + image.getImageId();
		StringBuilder commentHtml = new StringBuilder();
		commentHtml.append("<img src=\"" + tempImageUrl + "\" />");
		commentHtml.append("<script type=\"text/javascript\">alert('" + "コメント本文" + "')</script>");
		questionAnswer.setAnswerBody(commentHtml.toString());
		questionAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionAnswer = questionService.saveQuestionAnswer(questionAnswer);
		return questionAnswer;
	}

	/**
	 * 質問回答コメントを登録します。
	 * @param questionAnswer
	 * @param communityUser
	 * @return
	 */
	protected CommentDO saveCommentByQuestionAnswer(QuestionAnswerDO questionAnswer, CommunityUserDO communityUser) {
		CommentDO questionAnswerComment = new CommentDO();
		questionAnswerComment.setCommunityUser(communityUser);
		questionAnswerComment.setTargetType(CommentTargetType.QUESTION_ANSWER);
		questionAnswerComment.setQuestionAnswer(questionAnswer);
		questionAnswerComment.setCommentBody("質問回答コメント");
		questionAnswerComment = commentService.saveComment(questionAnswerComment);
		followService.followQuestion(
				followerUser.getCommunityUserId(), question.getQuestionId(), false);
		return questionAnswerComment;
	}

	/**
	 * 質問の違反報告をします。
	 * @param question
	 */
	protected void createSpamReport(QuestionDO question) {
		SpamReportDO spamReport = new SpamReportDO();
		spamReport.setCommunityUser(reportUser);
		spamReport.setQuestion(question);
		spamReport.setStatus(SpamReportStatus.NEW);
		spamReport.setSpamReportBody("質問違反報告");
		spamReport.setTargetType(SpamReportTargetType.QUESTION);
		questionSpamReport = spamReportService.createSpamReport(spamReport);
	}

	/**
	 * 質問回答の違反報告をします。
	 * @param questionAnswer
	 */
	protected void createSpamReport(QuestionAnswerDO questionAnswer) {
		SpamReportDO spamReport = new SpamReportDO();
		spamReport.setCommunityUser(reportUser);
		spamReport.setQuestionAnswer(questionAnswer);
		spamReport.setStatus(SpamReportStatus.NEW);
		spamReport.setSpamReportBody("質問回答違反報告");
		spamReport.setTargetType(SpamReportTargetType.QUESTION_ANSWER);
		questionAnswerSpamReport = spamReportService.createSpamReport(spamReport);
	}

	/**
	 * 質問回答コメントの違反報告をします。
	 * @param comment
	 */
	protected void createSpamReport(CommentDO comment) {
		SpamReportDO spamReport = new SpamReportDO();
		spamReport.setCommunityUser(reportUser);
		spamReport.setComment(questionAnswerComment);
		spamReport.setStatus(SpamReportStatus.NEW);
		spamReport.setSpamReportBody("質問回答コメント違反報告");
		spamReport.setTargetType(SpamReportTargetType.COMMENT);
		questionCommentSpamReport = spamReportService.createSpamReport(spamReport);
	}
}
