package com.kickmogu.yodobashi.community.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.common.utils.DateUtil;
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
import com.kickmogu.yodobashi.community.resource.domain.AbstractReviewDO.PointGrantRequestDetail;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.MailInfoDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommentTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSendTiming;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSettingType;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PurchaseHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewType;
import com.kickmogu.yodobashi.community.resource.domain.constants.Verification;
import com.kickmogu.yodobashi.community.service.config.ServiceConfig;
import com.kickmogu.yodobashi.community.service.vo.CommunityUserFollowVO;
import com.kickmogu.yodobashi.community.service.vo.MailImageSetVO;

@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/jcclientContext.xml")
public class MailLocalTest extends BaseTest {
	
	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger LOG = LoggerFactory.getLogger(MailLocalTest.class);
	
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
	 * サービスコンフィグです。
	 */
	@Autowired
	private ServiceConfig serviceConfig;
	
	@Autowired
	private ReviewService reviewService;
	
	private String communityUserId;
	private String followCommunityUserId;
	private String[] commentIds;
	
	@Override
	protected void initialize() {
		//communityUserId = "400000kC6Ed0600Ht6Ed0";
		communityUserId = "200000wq17f0300Gm7Ed0";
		followCommunityUserId = "200000wq17f0300Gm7Ed0";
		
		commentIds = new String[]{
				"3000008i4Md0a00mz6Ed0-3-09223370655544117157-200000d5yMd0200xz6Ed0",
				"3000008i4Md0a00mz6Ed0-3-09223370655544109450-300000d5yMd0200xz6Ed0",
				"3000008i4Md0a00mz6Ed0-3-09223370655544106670-400000d5yMd0200xz6Ed0",
				"3000008i4Md0a00mz6Ed0-3-09223370655544103773-500000d5yMd0200xz6Ed0",
				"3000008i4Md0a00mz6Ed0-3-09223370655543705746-200000hdyMd0200yz6Ed0"
		};
	}

	@Before
	public void setup() {
		initialize();
	}
	
	@Test
	public void testBatchMailService(){
		CommunityUserDO communityUser = communityUserDao.load(communityUserId, Path.DEFAULT);
		
		Calendar cal = Calendar.getInstance();
		cal.set(2014, 1, 27, 0, 0, 0);
		
		sendMail(communityUser, cal.getTime());
	}
	
	@Test
	public void testRegistrationCompleteMail(){
		CommunityUserDO communityUser = communityUserDao.load(communityUserId, Path.DEFAULT);
		if (communityUser == null || !communityUser.isActive()) {
			return;
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", communityUser);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = MailType.USER_REGISTER.createMailInfo(communityUser, dataMap);
		
		sendMail(mailInfo);
	}
	
	@Test
	public void testStopCommunityUser(){
		CommunityUserDO communityUser = communityUserDao.load(communityUserId, Path.DEFAULT);
		if (communityUser == null || !communityUser.isActive()) {
			return;
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", communityUser);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = MailType.STOP_COMMUNITY_USER.createMailInfo(communityUser, dataMap);
		
		sendMail(mailInfo);
	}
	
	@Test
	public void testFollowCommunityUserNotifyMail(){
		CommunityUserDO communityUser = communityUserDao.load(communityUserId, Path.DEFAULT);
		if (communityUser.isStop()) {
			return;
		}
		CommunityUserDO followCommunityUser = communityUserDao.load(
				followCommunityUserId, Path.DEFAULT);
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
						communityUserId, 
						ContentsStatus.SUBMITTED,
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
		
		MailInfoDO mailInfo = MailType.USER_FOLLOW.createMailInfo(followCommunityUser, dataMap);
		
		sendMail(mailInfo);
	}
	
	@Test
	public void testNotifyMailForJustAfterCommentSubmit() {
		for(String commentId : commentIds){
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
				String targetCommunityUserId = comment.getReview(
						).getCommunityUser().getCommunityUserId();
				if (!targetCommunityUserId.equals(comment.getCommunityUser(
						).getCommunityUserId()) &&
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
								comment.getReview(
								).getCommunityUser(), dataMap);
						sendMail(mailInfo);
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
								comment.getQuestionAnswer(
								).getCommunityUser(), dataMap);
						sendMail(mailInfo);
					}
				}
	
			} else if (comment.getTargetType().equals(CommentTargetType.IMAGE)) {
				String targetCommunityUserId = comment.getImageHeader(
						).getOwnerCommunityUser().getCommunityUserId();
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
						sendMail(mailInfo);
					}
				}
			}
		}
	}
	
	@Test
	public void testNotifyMailForJustAfterImageSubmit() {
		String imageSetId = "200000eJlyf0500mD7Ed0";
		String sku = "100000009000871689";
		
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
					followCommunityUserId, null, limit, offset);
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
	
	@Test
	public void testNotifyMailForJustAfterQuestionAnswerSubmit() {
		String questionAnswerId = "200000LLcya0600fgCea0";
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
	
	@Test
	public void testNotifyMailForJustAfterQuestionSubmit() {
		String questionId = "200000MIcya0400fgCea0";
		String sku = "100000009000877733";
		
		int limit = serviceConfig.readLimit;
		int offset = 0;

		QuestionDO question = questionDao.loadQuestion(
				questionId, Path.includeProp(
						"*").includePath("product.sku,communityUser.communityUserId").depth(1), false);
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
				if (!targetCommunityUser.getCommunityUserId().equals(
						communityUserId)
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
				if (!targetCommunityUser.getCommunityUserId().equals(
						communityUserId)
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
	
	@Test
	public void testNotifyMailForJustAfterReviewSubmit() {
		String reviewId = "2000002olBa0200NgCea0";
		String sku = "100000009000046682";
		
		
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
				|| review.getCommunityUser().isStop() ) {
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
	
	private void sendMail(CommunityUserDO communityUser, Date targetDate) {
		int limit = serviceConfig.readLimit;
		int mailListLimit = serviceConfig.mailListLimit;
		int offset = 0;

		//購入商品
		CommunityUserDO oldCommunityUser = requestScopeDao.loadCommunityUser();
		requestScopeDao.initialize(communityUser, null);

		String message = "Send Mail Target CommunityUser: " + (communityUser == null?"null":communityUser.getCommunityUserId() + " " + communityUser.getCommunityName());
		print(message);

		MailSendTiming reviewLimitSendTiming
				= mailSettingDao.loadMailSettingValueWithDefault(
				communityUser.getCommunityUserId(),
				MailSettingType.REVIEW_LIMIT);
		boolean sendReviewLimit = false;
		if (reviewLimitSendTiming.equals(MailSendTiming.FIVE_DAYS_AGO) ||
				reviewLimitSendTiming.equals(MailSendTiming.TEN_DAYS_AGO)) {
			sendReviewLimit = true;
		}

		boolean sendPurchaseProductQuestion
				= mailSettingDao.loadMailSettingValueWithDefault(
				communityUser.getCommunityUserId(),
				MailSettingType.PURCHASE_PRODUCT_QUESTION
				).equals(MailSendTiming.DAILY_NOTIFY);

		Set<String> stopCommunityUserIds = new HashSet<String>();

		List<PurchaseProductDO> reviewLimitTargets = new ArrayList<PurchaseProductDO>();
		List<QuestionDO> purchaseProductQuestions = new ArrayList<QuestionDO>();
		offset = 0;
		while (sendReviewLimit || sendPurchaseProductQuestion) {
			SearchResult<PurchaseProductDO> searchResult
					= orderDao.findPurchaseProductByCommunityUserIdForMR(
					communityUser.getCommunityUserId(), false, limit, offset);
			List<String> skus = new ArrayList<String>();
			for (PurchaseProductDO purchaseProduct : searchResult.getDocuments()) {
				offset++;
				if (purchaseProduct.isAdult() &&
						!communityUser.getAdultVerification().equals(Verification.AUTHORIZED)) {
					continue;
				}
				//期限切れメール送信判断処理
				if (purchaseProduct.getProduct().isCanReview()
						&& sendReviewLimit && reviewLimitTargets.size() < mailListLimit
						&& purchaseProduct.getPurchaseHistoryType().equals(PurchaseHistoryType.YODOBASHI)) {
					int judge = ServiceConfig.INSTANCE.mailReviewLimitFive;
					if (reviewLimitSendTiming.equals(MailSendTiming.TEN_DAYS_AGO)) {
						judge = ServiceConfig.INSTANCE.mailReviewLimitTen;
					}
					Date pointBaseTime = targetDate;
					if (purchaseProduct.getProduct().getNextGrantPointReviewLimit(
							purchaseProduct.getPurchaseDate(),
							pointBaseTime) == judge) {
						int reviewTerm = purchaseProduct.getProduct().getGrantPointReviewTerm(
								purchaseProduct.getPurchaseDate(),
								pointBaseTime);
						
						ReviewType reviewType = ReviewType.REVIEW_AFTER_FEW_DAYS;
						if (reviewTerm == 0) {
							long cnt = reviewDao.countPostReviewCount(communityUser.getCommunityUserId(), purchaseProduct.getProduct().getSku());
							if (cnt <= 0) {
								// 1件もレビューが無い場合
								reviewType = ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE;
							}
						}
						
						Integer elapsedDays = DateUtil.getElapsedDays(purchaseProduct.getPurchaseDate(), pointBaseTime);
						List<PointGrantRequestDetail> pointGrantRequestDetailList =
								reviewService.getPointGrantRequestDetailsWithoutSp(reviewType, purchaseProduct.getProduct(), elapsedDays);
						
						long point = 0;
						for (PointGrantRequestDetail pgrd : pointGrantRequestDetailList) {
							point += pgrd.getPoint();
						}
						
						// pointが0ポイントより大きい場合にメール送信対象とする
						if (point > 0) {
							if (reviewDao.isLeniencePointGrantReview(
									communityUser.getCommunityUserId(),
									purchaseProduct.getProduct(),
									reviewTerm)) {
								reviewLimitTargets.add(purchaseProduct);
							}
						}
					}
				}
				if (sendPurchaseProductQuestion) {
					skus.add(purchaseProduct.getProduct().getSku());
				}
			}
			if (sendPurchaseProductQuestion && purchaseProductQuestions.size() < mailListLimit) {
				SearchResult<QuestionDO> questions = questionDao.findQuestionBySKUsForMR(
						skus, targetDate, communityUser.getCommunityUserId(), limit, 0);
				stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(
						searchResult.getDocuments(), stopCommunityUserIds);
				for (QuestionDO question : questions.getDocuments()) {
					if (question.isStop(communityUser.getCommunityUserId(), stopCommunityUserIds)) {
						continue;
					}
					purchaseProductQuestions.add(question);
				}
			}

			if (searchResult.getDocuments().size() < limit
					|| searchResult.getNumFound() <= offset
					|| (reviewLimitTargets.size() >= mailListLimit
							&& purchaseProductQuestions.size() >= mailListLimit)) {
				break;
			}
		}

		print("Send Mail POINT 2");

		offset = 0;
		//REVIEW_LIMIT ("1", "レビュー期限", MailSettingCategory.POST),
		if (sendReviewLimit && reviewLimitTargets.size() > 0) {
			MailType mailType = MailType.REVIEW_LIMIT_NOTIFY_FOR_FIVE_DAYS_AGO;
			if (reviewLimitSendTiming.equals(MailSendTiming.TEN_DAYS_AGO)) {
				mailType = MailType.REVIEW_LIMIT_NOTIFY_FOR_TEN_DAYS_AGO;
			}
			sendReviewLimitMail(communityUser, reviewLimitTargets, mailType, targetDate);
		}
		//PURCHASE_PRODUCT_QUESTION ("2", "購入商品の新着QA質問", MailSettingCategory.POST),
		if (sendPurchaseProductQuestion && purchaseProductQuestions.size() > 0) {
			sendPurchaseProductQuestionMail(communityUser, purchaseProductQuestions, targetDate);
		}

		boolean sendRankInProductMaster
				= mailSettingDao.loadMailSettingValueWithDefault(
				communityUser.getCommunityUserId(),
				MailSettingType.RANK_IN_PRODUCT_MASTER
				).equals(MailSendTiming.DAILY_NOTIFY);

		//RANK_IN_PRODUCT_MASTER ("18", "Top50の商品マスターにランクインしたとき", MailSettingCategory.PRODUCT_MASTER),
		if (sendRankInProductMaster) {
			SearchResult<ProductMasterDO> searchResult
					= productMasterDao.findProductMasterInNewRankByCommunityUserIdForMR(
							communityUser.getCommunityUserId(), mailListLimit, 0,
							communityUser.getAdultVerification());
			if (searchResult.getDocuments().size() > 0) {
				sendRankInProductMasterMail(communityUser, searchResult.getDocuments(), targetDate);
			}
		}

		boolean sendReviewComment
				= mailSettingDao.loadMailSettingValueWithDefault(
				communityUser.getCommunityUserId(),
				MailSettingType.REVIEW_COMMENT
				).equals(MailSendTiming.DAILY_NOTIFY);

		//REVIEW_COMMENT ("3", "レビューに対してコメントがついたとき", MailSettingCategory.POST),
		if (sendReviewComment) {
			SearchResult<CommentDO> comments
					= commentDao.findCommentReviewByCommunityUserId(
							communityUser.getCommunityUserId(),
							targetDate, mailListLimit, 0);
			stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(
					comments.getDocuments(), stopCommunityUserIds);
			for (Iterator<CommentDO> it = comments.getDocuments().iterator(); it.hasNext(); ) {
				CommentDO comment = it.next();
				if (comment.isStop(communityUser.getCommunityUserId(), stopCommunityUserIds)) {
					it.remove();
				}
			}
			if (comments.getDocuments().size() > 0) {
				sendReviewCommentMail(communityUser, comments, targetDate);
			}
		}

		boolean sendAnswerComment
				= mailSettingDao.loadMailSettingValueWithDefault(
				communityUser.getCommunityUserId(),
				MailSettingType.ANSWER_COMMENT
				).equals(MailSendTiming.DAILY_NOTIFY);
		//ANSWER_COMMENT ("4", "回答に対してコメントがついたとき", MailSettingCategory.POST),
		if (sendAnswerComment) {
			SearchResult<CommentDO> comments
					= commentDao.findCommentQuestionAnswerByCommunityUserId(
							communityUser.getCommunityUserId(), targetDate,
							mailListLimit, 0);
			stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(
					comments.getDocuments(), stopCommunityUserIds);
			for (Iterator<CommentDO> it = comments.getDocuments().iterator(); it.hasNext(); ) {
				CommentDO comment = it.next();
				if (comment.isStop(communityUser.getCommunityUserId(), stopCommunityUserIds)) {
					it.remove();
				}
			}
			if (comments.getDocuments().size() > 0) {
				sendAnswerCommentMail(communityUser, comments, targetDate);
			}
		}

		boolean sendImageComment
				= mailSettingDao.loadMailSettingValueWithDefault(
				communityUser.getCommunityUserId(),
				MailSettingType.IMAGE_COMMENT
				).equals(MailSendTiming.DAILY_NOTIFY);
		//IMAGE_COMMENT ("7", "画像に対してコメントがついたとき", MailSettingCategory.POST),
		if (sendImageComment) {
			SearchResult<CommentDO> comments
					= commentDao.findCommentImageByCommunityUserIdForMR(
							communityUser.getCommunityUserId(), targetDate,
							mailListLimit, 0);
			stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(
					comments.getDocuments(), stopCommunityUserIds);
			for (Iterator<CommentDO> it = comments.getDocuments().iterator(); it.hasNext(); ) {
				CommentDO comment = it.next();
				if (comment.isStop(communityUser.getCommunityUserId(), stopCommunityUserIds)) {
					it.remove();
				}
			}
			if (comments.getDocuments().size() > 0) {
				sendImageCommentMail(communityUser, comments, targetDate);
			}
		}

		boolean sendQuestionAnswer
				= mailSettingDao.loadMailSettingValueWithDefault(
				communityUser.getCommunityUserId(),
				MailSettingType.QUESTION_ANSWER
				).equals(MailSendTiming.DAILY_NOTIFY);
		//QUESTION_ANSWER ("5", "質問に対して回答がついたとき", MailSettingCategory.POST),
		if (sendQuestionAnswer) {
			SearchResult<QuestionAnswerDO> questionAnswers
					= questionAnswerDao.findQuestionAnswerByCommunityUserQuestionForMR(
							communityUser.getCommunityUserId(), targetDate,
							mailListLimit, 0);
			stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(
					questionAnswers.getDocuments(), stopCommunityUserIds);
			for (Iterator<QuestionAnswerDO> it = questionAnswers.getDocuments().iterator(); it.hasNext(); ) {
				QuestionAnswerDO questionAnswer = it.next();
				if (questionAnswer.isStop(communityUser.getCommunityUserId(), stopCommunityUserIds)) {
					it.remove();
				}
			}
			if (questionAnswers.getDocuments().size() > 0) {
				sendQuestionAnswerMail(communityUser, questionAnswers, targetDate);
			}
		}

		boolean sendAnswerQuestionAnotherAnswer
				= mailSettingDao.loadMailSettingValueWithDefault(
				communityUser.getCommunityUserId(),
				MailSettingType.ANSWER_QUESTION_ANOTHER_ANSWER
				).equals(MailSendTiming.DAILY_NOTIFY);
		//ANSWER_QUESTION_ANOTHER_ANSWER ("6", "回答したQAに別の回答がついたとき", MailSettingCategory.POST),
		if (sendAnswerQuestionAnotherAnswer) {
			SearchResult<QuestionAnswerDO> questionAnswers
					= questionAnswerDao.findAnotherQuestionAnswerByCommunityUserAnswerForMR(
					communityUser.getCommunityUserId(),
					targetDate, mailListLimit, 0);
			stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(
					questionAnswers.getDocuments(), stopCommunityUserIds);
			for (Iterator<QuestionAnswerDO> it = questionAnswers.getDocuments().iterator(); it.hasNext(); ) {
				QuestionAnswerDO questionAnswer = it.next();
				if (questionAnswer.isStop(communityUser.getCommunityUserId(), stopCommunityUserIds)) {
					it.remove();
				}
			}
			if (questionAnswers.getDocuments().size() > 0) {
				sendAnswerQuestionAnotherAnswerMail(communityUser, questionAnswers, targetDate);
			}
		}

		boolean sendReviewProductAnotherReview
				= mailSettingDao.loadMailSettingValueWithDefault(
				communityUser.getCommunityUserId(),
				MailSettingType.REVIEW_PRODUCT_ANOTHER_REVIEW
				).equals(MailSendTiming.DAILY_NOTIFY);
		//REVIEW_PRODUCT_ANOTHER_REVIEW ("9", "レビューを書いた商品に別のレビューが投稿されたとき", MailSettingCategory.FOLLOW),
		if (sendReviewProductAnotherReview) {
			SearchResult<ReviewDO> reviews = reviewDao.findAnotherReviewByCommunityUserRreviewForMR(
					communityUser.getCommunityUserId(),
					targetDate, mailListLimit, 0);
			stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(
					reviews.getDocuments(), stopCommunityUserIds);
			for (Iterator<ReviewDO> it = reviews.getDocuments().iterator(); it.hasNext(); ) {
				ReviewDO review = it.next();
				if (review.isStop(communityUser.getCommunityUserId(), stopCommunityUserIds)) {
					it.remove();
				}
			}
			if (reviews.getDocuments().size() > 0) {
				sendReviewProductAnotherReviewMail(communityUser, reviews, targetDate);
			}
		}

		print("Send Mail POINT 3");
		boolean sendUserFollow
				= mailSettingDao.loadMailSettingValueWithDefault(
				communityUser.getCommunityUserId(),
				MailSettingType.USER_FOLLOW
				).equals(MailSendTiming.DAILY_NOTIFY);
		//USER_FOLLOW ("8", "ほかのユーザーにフォローされたとき", MailSettingCategory.FOLLOW),
		if (sendUserFollow) {
			SearchResult<CommunityUserFollowDO> follows = communityUserFollowDao.findNewUserFollow(
					communityUser.getCommunityUserId(),
					targetDate, mailListLimit, 0);
			SearchResult<CommunityUserFollowVO> result
					= new SearchResult<CommunityUserFollowVO>();
			result.setHasAdult(follows.isHasAdult());
			result.setNumFound(follows.getNumFound());
			List<String> communityUserIds = new ArrayList<String>();
			stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(
					follows.getDocuments(), stopCommunityUserIds);
			for (CommunityUserFollowDO follow : follows.getDocuments()) {
				if (follow.isStop(communityUser.getCommunityUserId(), stopCommunityUserIds)) {
					continue;
				}
				communityUserIds.add(
						follow.getCommunityUser().getCommunityUserId());
			}

			if (communityUserIds.size() > 0) {
				Map<String, Long> reviewCountMap
						= reviewDao.loadReviewCountMapByCommunityUserId(
								communityUserIds);
				Map<String, Long> questionCountMap
						= questionDao.loadQuestionCountMapByCommunityUserId(
								communityUserIds);
				Map<String, Long> questionAnswerCountMap
						= questionAnswerDao.loadQuestionAnswerCountMapByCommunityUserId(
						communityUserIds);
				Map<String, Long> imageCountMap
						= imageDao.loadImageCountMapByCommunityUserId(communityUserIds);

				Map<String, Long> followCountMap
						= communityUserFollowDao.loadFollowCountMap(communityUserIds);

				for (CommunityUserFollowDO follow : follows.getDocuments()) {
					if (follow.isStop(communityUser.getCommunityUserId(), stopCommunityUserIds)) {
						continue;
					}
					CommunityUserFollowVO vo = new CommunityUserFollowVO();
					vo.setCommunityUser(follow.getCommunityUser());
					vo.setFollowDate(follow.getFollowDate());
					if (reviewCountMap.containsKey(
							follow.getCommunityUser().getCommunityUserId())) {
						vo.setPostReviewCount(
								reviewCountMap.get(
								follow.getCommunityUser().getCommunityUserId()));
					}
					if (questionCountMap.containsKey(
							follow.getCommunityUser().getCommunityUserId())) {
						vo.setPostQuestionCount(
								questionCountMap.get(
								follow.getCommunityUser().getCommunityUserId()));
					}
					if (questionAnswerCountMap.containsKey(
							follow.getCommunityUser().getCommunityUserId())) {
						vo.setPostAnswerCount(
								questionAnswerCountMap.get(
								follow.getCommunityUser().getCommunityUserId()));
					}
					if (imageCountMap.containsKey(
							follow.getCommunityUser().getCommunityUserId())) {
						vo.setPostImageCount(
								imageCountMap.get(
								follow.getCommunityUser().getCommunityUserId()));
					}
					if (followCountMap.containsKey(
							follow.getCommunityUser().getCommunityUserId())) {
						vo.setFollowUserCount(
								followCountMap.get(
								follow.getCommunityUser().getCommunityUserId()));
					}

					SearchResult<ProductMasterDO> productMasters
							= productMasterDao.findRankProductMasterByCommunityUserIdForMR(
							follow.getCommunityUser().getCommunityUserId(),
							mailListLimit, null, null, false, true,
							communityUser.getAdultVerification());
					vo.setProductMasterCount(productMasters.getNumFound());
					vo.setProductMasters(productMasters.getDocuments());

					result.getDocuments().add(vo);
				}
				if (result.getDocuments().size() > 0) {
					sendUserFollowMail(communityUser, result, targetDate);
				}
			}
		}

		SearchResult<CommunityUserDO> followUsers = null;
		List<String> followUserIds = null;

		boolean sendFollowUserNewQuestion
				= mailSettingDao.loadMailSettingValueWithDefault(
				communityUser.getCommunityUserId(),
				MailSettingType.FOLLOW_USER_QUESTION
				).equals(MailSendTiming.DAILY_NOTIFY);
		boolean sendFollowUserNewReview
				= mailSettingDao.loadMailSettingValueWithDefault(
				communityUser.getCommunityUserId(),
				MailSettingType.FOLLOW_USER_REVIEW
				).equals(MailSendTiming.DAILY_NOTIFY);
		boolean sendFollowUserNewQuestionAnswer
				= mailSettingDao.loadMailSettingValueWithDefault(
				communityUser.getCommunityUserId(),
				MailSettingType.FOLLOW_USER_QUESTION_ANSWER
				).equals(MailSendTiming.DAILY_NOTIFY);
		boolean sendFollowUserNewImage
				= mailSettingDao.loadMailSettingValueWithDefault(
				communityUser.getCommunityUserId(),
				MailSettingType.FOLLOW_USER_IMAGE
				).equals(MailSendTiming.DAILY_NOTIFY);
		if (sendFollowUserNewQuestion || sendFollowUserNewReview
				|| sendFollowUserNewQuestionAnswer || sendFollowUserNewImage) {
			followUsers
					= communityUserFollowDao.findFollowCommunityUserByCommunityUserId(
					communityUser.getCommunityUserId(), null, limit, 0);
			followUserIds = new ArrayList<String>();
			for (CommunityUserDO followUser : followUsers.getDocuments()) {
				if (followUser.isStop()) {
					continue;
				}
				followUserIds.add(followUser.getCommunityUserId());
			}
		}
		//FOLLOW_USER_QUESTION ("14", "フォローしたユーザのQA質問", MailSettingCategory.FOLLOW),
		if (sendFollowUserNewQuestion && followUserIds.size() > 0) {
			SearchResult<QuestionDO> questions = questionDao.findQuestionByCommunityUserIdsForMR(
					followUserIds, targetDate, mailListLimit, 0);
			if (questions.getDocuments().size() > 0) {
				sendFollowUserNewQuestionMail(communityUser, questions, targetDate);
			}
		}
		//FOLLOW_USER_REVIEW ("15", "フォローしたユーザのレビュー", MailSettingCategory.FOLLOW),
		if (sendFollowUserNewReview && followUserIds.size() > 0) {
			SearchResult<ReviewDO> reviews = reviewDao.findReviewByCommunityUserIdsForMR(
					followUserIds, targetDate, mailListLimit, 0);
			if (reviews.getDocuments().size() > 0) {
				sendFollowUserNewReviewMail(communityUser, reviews, targetDate);
			}
		}
		//FOLLOW_USER_QUESTION_ANSWER ("16", "フォローしたユーザのQAの回答", MailSettingCategory.FOLLOW),
		if (sendFollowUserNewQuestionAnswer && followUserIds.size() > 0) {
			SearchResult<QuestionAnswerDO> questionAnswers = questionAnswerDao.findQuestionAnswerByCommunityUserIdsForMR(
					followUserIds, targetDate, mailListLimit, 0);
			if (questionAnswers.getDocuments().size() > 0) {
				sendFollowUserNewQuestionAnswerMail(communityUser, questionAnswers, targetDate);
			}
		}

		//FOLLOW_USER_IMAGE ("17", "フォローしたユーザの画像投稿", MailSettingCategory.FOLLOW),
		if (sendFollowUserNewImage && followUserIds.size() > 0) {
			SearchResult<ImageHeaderDO> images = imageDao.findImageSetByCommunityUserIdsForMR(
					followUserIds, targetDate, mailListLimit, 0);
			SearchResult<MailImageSetVO> imageSets = new SearchResult<MailImageSetVO>();
			for (ImageHeaderDO topImage : images.getDocuments()) {
				MailImageSetVO vo = new MailImageSetVO();
				vo.setTopImage(topImage);
				vo.setImageHeaders(imageDao.findImageByImageSetId(topImage.getImageSetId(), null, new ContentsStatus[]{ContentsStatus.SUBMITTED}));
				imageSets.getDocuments().add(vo);
			}
			if (images.getDocuments().size() > 0) {
				sendFollowUserNewImageMail(communityUser, imageSets, targetDate);
			}
		}

		print("Send Mail POINT 4");
		SearchResult<ProductDO> followProducts = null;
		List<String> followProductIds = null;

		boolean sendFollowProductNewReview
				= mailSettingDao.loadMailSettingValueWithDefault(
				communityUser.getCommunityUserId(),
				MailSettingType.FOLLOW_PRODUCT_REVIEW
				).equals(MailSendTiming.DAILY_NOTIFY);
		boolean sendFollowProductNewQuestion
				= mailSettingDao.loadMailSettingValueWithDefault(
				communityUser.getCommunityUserId(),
				MailSettingType.FOLLOW_PRODUCT_QUESTION
				).equals(MailSendTiming.DAILY_NOTIFY);
		boolean sendFollowProductNewImage
				= mailSettingDao.loadMailSettingValueWithDefault(
				communityUser.getCommunityUserId(),
				MailSettingType.FOLLOW_PRODUCT_IMAGE
				).equals(MailSendTiming.DAILY_NOTIFY);
		if (sendFollowProductNewReview || sendFollowProductNewQuestion
				|| sendFollowProductNewImage) {
			followProducts
					= productFollowDao.findFollowProductByCommunityUserId(
					communityUser.getCommunityUserId(), limit, 0);
			followProductIds = new ArrayList<String>();
			for (ProductDO followProduct : followProducts.getDocuments()) {
				followProductIds.add(followProduct.getSku());
			}
		}
		//FOLLOW_PRODUCT_REVIEW ("10", "フォローした商品の新着レビュー", MailSettingCategory.FOLLOW),
		if (sendFollowProductNewReview && followProductIds.size() > 0) {
			SearchResult<ReviewDO> reviews = reviewDao.findReviewBySKUsForMR(
					followProductIds, targetDate, communityUser.getCommunityUserId(), mailListLimit, 0);
			stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(
					reviews.getDocuments(), stopCommunityUserIds);
			for (Iterator<ReviewDO> it = reviews.getDocuments().iterator(); it.hasNext(); ) {
				ReviewDO review = it.next();
				if (review.isStop(communityUser.getCommunityUserId(), stopCommunityUserIds)) {
					it.remove();
				}
			}
			if (reviews.getDocuments().size() > 0) {
				sendFollowProductNewReviewMail(communityUser, reviews, targetDate);
			}
		}
		//FOLLOW_PRODUCT_QUESTION ("11", "フォローした商品の新着QA", MailSettingCategory.FOLLOW),
		if (sendFollowProductNewQuestion && followProductIds.size() > 0) {
			SearchResult<QuestionDO> questions = questionDao.findQuestionBySKUsForMR(
					followProductIds, targetDate, communityUser.getCommunityUserId(), mailListLimit, 0);
			stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(
					questions.getDocuments(), stopCommunityUserIds);
			for (Iterator<QuestionDO> it = questions.getDocuments().iterator(); it.hasNext(); ) {
				QuestionDO question = it.next();
				if (question.isStop(communityUser.getCommunityUserId(), stopCommunityUserIds)) {
					it.remove();
				}
			}
			if (questions.getDocuments().size() > 0) {
				sendFollowProductNewQuestionMail(communityUser, questions, targetDate);
			}
		}
		//FOLLOW_PRODUCT_IMAGE ("12", "フォローした商品の新着画像", MailSettingCategory.FOLLOW),
		if (sendFollowProductNewImage && followProductIds.size() > 0) {
			SearchResult<ImageHeaderDO> images = imageDao.findImageSetBySKUsForMR(
					followProductIds, targetDate,communityUser.getCommunityUserId(), mailListLimit, 0);
			SearchResult<MailImageSetVO> imageSets = new SearchResult<MailImageSetVO>();
			stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(
					images.getDocuments(), stopCommunityUserIds);
			for (Iterator<ImageHeaderDO> it = images.getDocuments().iterator(); it.hasNext(); ) {
				ImageHeaderDO image = it.next();
				if (image.isStop(communityUser.getCommunityUserId(), stopCommunityUserIds)) {
					it.remove();
				}
			}
			for (ImageHeaderDO topImage : images.getDocuments()) {
				MailImageSetVO vo = new MailImageSetVO();
				vo.setTopImage(topImage);
				vo.setImageHeaders(imageDao.findImageByImageSetId(topImage.getImageSetId(), null, new ContentsStatus[]{ContentsStatus.SUBMITTED}));
				imageSets.getDocuments().add(vo);
			}
			if (images.getDocuments().size() > 0) {
				sendFollowProductNewImageMail(communityUser, imageSets, targetDate);
			}
		}

		boolean sendFollowQuestionAnswer
				= mailSettingDao.loadMailSettingValueWithDefault(
				communityUser.getCommunityUserId(),
				MailSettingType.FOLLOW_QUESTION_ANSWER
				).equals(MailSendTiming.DAILY_NOTIFY);
		//FOLLOW_QUESTION_ANSWER ("13", "フォローしたQAの新着回答", MailSettingCategory.FOLLOW),
		if (sendFollowQuestionAnswer) {
			SearchResult<QuestionDO> followQuestions
					= questionFollowDao.findFollowQuestionByCommunityUserId(
					communityUser.getCommunityUserId(), limit, 0);
			stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(
					followQuestions.getDocuments(), stopCommunityUserIds);
			for (Iterator<QuestionDO> it = followQuestions.getDocuments().iterator(); it.hasNext(); ) {
				QuestionDO question = it.next();
				if (question.isStop(communityUser.getCommunityUserId(), stopCommunityUserIds)) {
					it.remove();
				}
			}
			if (followQuestions.getDocuments().size() > 0) {
				List<String> followQuestionIds = new ArrayList<String>();
				for (QuestionDO followQuestion : followQuestions.getDocuments()) {
					followQuestionIds.add(followQuestion.getQuestionId());
				}
				SearchResult<QuestionAnswerDO> questionAnswers = questionAnswerDao.findQuestionAnswerByQuestionIdsForMR(
						followQuestionIds, targetDate, communityUser.getCommunityUserId(), mailListLimit, 0);
				stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(
						questionAnswers.getDocuments(), stopCommunityUserIds);
				for (Iterator<QuestionAnswerDO> it = questionAnswers.getDocuments().iterator(); it.hasNext(); ) {
					QuestionAnswerDO questionAnswer = it.next();
					if (questionAnswer.isStop(communityUser.getCommunityUserId(), stopCommunityUserIds)) {
						it.remove();
					}
				}
				if (questionAnswers.getDocuments().size() > 0) {
					sendFollowQuestionNewAnswerMail(communityUser, questionAnswers, targetDate);
				}
			}
		}

		requestScopeDao.initialize(oldCommunityUser, null);

		print("End Send Mail");

	}
	
	/**
	 * レビュー期限切れお知らせのメールを送信します。
	 * @param targetCommunityUser 宛先ユーザー
	 * @param purchaseProducts 購入商品情報リスト
	 * @param mailType メールタイプ
	 * @param batchDate バッチ日付
	 */
	private void sendReviewLimitMail(CommunityUserDO targetCommunityUser,
			List<PurchaseProductDO> purchaseProducts,
			MailType mailType,
			Date batchDate) {
		if (targetCommunityUser == null || !targetCommunityUser.isActive()) {
			return;
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", targetCommunityUser);
		dataMap.put("purchaseProducts", purchaseProducts);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = mailType.createMailInfo(
				targetCommunityUser, dataMap);
		sendMail(mailInfo, batchDate, targetCommunityUser.getCommunityUserId());
	}

	/**
	 * 購入商品の新着QA質問のメールを送信します。
	 * @param targetCommunityUser 宛先ユーザー
	 * @param questions 質問情報リスト
	 * @param batchDate バッチ日付
	 */
	private void sendPurchaseProductQuestionMail(CommunityUserDO targetCommunityUser,
			List<QuestionDO> questions,
			Date batchDate) {
		if (targetCommunityUser == null || !targetCommunityUser.isActive()) {
			return;
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", targetCommunityUser);
		dataMap.put("questions", questions);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = MailType.NEW_QUESTION_FOR_PURCHASE_PRODUCT_SUMMARY.createMailInfo(
				targetCommunityUser, dataMap);
		sendMail(mailInfo, batchDate, targetCommunityUser.getCommunityUserId());
	}

	/**
	 * 商品マスターのランクアップのメールを送信します。
	 * @param targetCommunityUser 宛先ユーザー
	 * @param productMasters 商品マスターリスト
	 * @param batchDate バッチ日付
	 */
	private void sendRankInProductMasterMail(CommunityUserDO targetCommunityUser,
			List<ProductMasterDO> productMasters,
			Date batchDate) {
		if (targetCommunityUser == null || !targetCommunityUser.isActive()) {
			return;
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", targetCommunityUser);
		dataMap.put("productMasters", productMasters);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = MailType.PRODUCT_MASTER_RANK_IN_SUMMARY.createMailInfo(
				targetCommunityUser, dataMap);
		sendMail(mailInfo, batchDate, targetCommunityUser.getCommunityUserId());
	}

	/**
	 * 新着コメント付きレビューのメールを送信します。
	 * @param targetCommunityUser 宛先ユーザー
	 * @param comments コメントリスト
	 * @param batchDate バッチ日付
	 */
	private void sendReviewCommentMail(CommunityUserDO targetCommunityUser,
			SearchResult<CommentDO> comments,
			Date batchDate) {
		if (targetCommunityUser == null || !targetCommunityUser.isActive()) {
			return;
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", targetCommunityUser);
		dataMap.put("comments", comments);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = MailType.REVIEW_COMMENT_SUMMARY.createMailInfo(
				targetCommunityUser, dataMap);
		sendMail(mailInfo, batchDate, targetCommunityUser.getCommunityUserId());
	}

	/**
	 * 新着コメント付き質問回答のメールを送信します。
	 * @param targetCommunityUser 宛先ユーザー
	 * @param comments コメントリスト
	 * @param batchDate バッチ日付
	 */
	private void sendAnswerCommentMail(CommunityUserDO targetCommunityUser,
			SearchResult<CommentDO> comments,
			Date batchDate) {
		if (targetCommunityUser == null || !targetCommunityUser.isActive()) {
			return;
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", targetCommunityUser);
		dataMap.put("comments", comments);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = MailType.QUESTION_ANSWER_COMMENT_SUMMARY.createMailInfo(
				targetCommunityUser, dataMap);
		sendMail(mailInfo, batchDate, targetCommunityUser.getCommunityUserId());
	}

	/**
	 * 新着コメント付き画像のメールを送信します。
	 * @param targetCommunityUser 宛先ユーザー
	 * @param comments コメントリスト
	 * @param batchDate バッチ日付
	 */
	private void sendImageCommentMail(CommunityUserDO targetCommunityUser,
			SearchResult<CommentDO> comments,
			Date batchDate) {
		if (targetCommunityUser == null || !targetCommunityUser.isActive()) {
			return;
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", targetCommunityUser);
		dataMap.put("comments", comments);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = MailType.IMAGE_COMMENT_SUMMARY.createMailInfo(
				targetCommunityUser, dataMap);
		sendMail(mailInfo, batchDate, targetCommunityUser.getCommunityUserId());
	}

	/**
	 * 新着回答付き質問のメールを送信します。
	 * @param targetCommunityUser 宛先ユーザー
	 * @param questionAnswers 質問回答リスト
	 * @param batchDate バッチ日付
	 */
	private void sendQuestionAnswerMail(CommunityUserDO targetCommunityUser,
			SearchResult<QuestionAnswerDO> questionAnswers,
			Date batchDate) {
		if (targetCommunityUser == null || !targetCommunityUser.isActive()) {
			return;
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", targetCommunityUser);
		dataMap.put("questionAnswers", questionAnswers);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = MailType.QUESTION_ANSWER_SUMMARY.createMailInfo(
				targetCommunityUser, dataMap);
		sendMail(mailInfo, batchDate, targetCommunityUser.getCommunityUserId());
	}

	/**
	 * 回答質問への別の回答のメールを送信します。
	 * @param targetCommunityUser 宛先ユーザー
	 * @param questionAnswers 質問回答リスト
	 * @param batchDate バッチ日付
	 */
	private void sendAnswerQuestionAnotherAnswerMail(CommunityUserDO targetCommunityUser,
			SearchResult<QuestionAnswerDO> questionAnswers,
			Date batchDate) {
		if (targetCommunityUser == null || !targetCommunityUser.isActive()) {
			return;
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", targetCommunityUser);
		dataMap.put("questionAnswers", questionAnswers);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = MailType.ANOTHER_QUESTION_ANSWER_SUMMARY.createMailInfo(
				targetCommunityUser, dataMap);
		sendMail(mailInfo, batchDate, targetCommunityUser.getCommunityUserId());
	}

	/**
	 * レビューを書いた商品に別のレビューのメールを送信します。
	 * @param targetCommunityUser 宛先ユーザー
	 * @param reviews レビューリスト
	 * @param batchDate バッチ日付
	 */
	private void sendReviewProductAnotherReviewMail(CommunityUserDO targetCommunityUser,
			SearchResult<ReviewDO> reviews,
			Date batchDate) {
		if (targetCommunityUser == null || !targetCommunityUser.isActive()) {
			return;
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", targetCommunityUser);
		dataMap.put("reviews", reviews);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = MailType.ANOTHER_REVIEW_SUMMARY.createMailInfo(
				targetCommunityUser, dataMap);
		sendMail(mailInfo, batchDate, targetCommunityUser.getCommunityUserId());
	}

	/**
	 * 新規フォローのメールを送信します。
	 * @param targetCommunityUser 宛先ユーザー
	 * @param followers フォロワーリスト
	 * @param batchDate バッチ日付
	 */
	private void sendUserFollowMail(CommunityUserDO targetCommunityUser,
			SearchResult<CommunityUserFollowVO> followers,
			Date batchDate) {
		if (targetCommunityUser == null || !targetCommunityUser.isActive()) {
			return;
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", targetCommunityUser);
		dataMap.put("topFollower", followers.getDocuments().get(0));
		dataMap.put("followers", followers);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = MailType.USER_FOLLOW_SUMMARY.createMailInfo(
				targetCommunityUser, dataMap);
		sendMail(mailInfo, batchDate, targetCommunityUser.getCommunityUserId());
	}

	/**
	 * フォローしたユーザのレビューのメールを送信します。
	 * @param targetCommunityUser 宛先ユーザー
	 * @param reviews レビューリスト
	 * @param batchDate バッチ日付
	 */
	private void sendFollowUserNewReviewMail(CommunityUserDO targetCommunityUser,
			SearchResult<ReviewDO> reviews,
			Date batchDate) {
		if (targetCommunityUser == null || !targetCommunityUser.isActive()) {
			return;
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", targetCommunityUser);
		dataMap.put("reviews", reviews);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = MailType.FOLLOW_USER_NEW_REVIEW_SUMMARY.createMailInfo(
				targetCommunityUser, dataMap);
		sendMail(mailInfo, batchDate, targetCommunityUser.getCommunityUserId());
	}

	/**
	 * フォローしたユーザの質問のメールを送信します。
	 * @param targetCommunityUser 宛先ユーザー
	 * @param questions 質問リスト
	 * @param batchDate バッチ日付
	 */
	private void sendFollowUserNewQuestionMail(CommunityUserDO targetCommunityUser,
			SearchResult<QuestionDO> questions,
			Date batchDate) {
		if (targetCommunityUser == null || !targetCommunityUser.isActive()) {
			return;
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", targetCommunityUser);
		dataMap.put("questions", questions);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = MailType.FOLLOW_USER_NEW_QUESTION_SUMMARY.createMailInfo(
				targetCommunityUser, dataMap);
		sendMail(mailInfo, batchDate, targetCommunityUser.getCommunityUserId());
	}

	/**
	 * フォローしたユーザの質問回答のメールを送信します。
	 * @param targetCommunityUser 宛先ユーザー
	 * @param questionAnswers 質問回答リスト
	 * @param batchDate バッチ日付
	 */
	private void sendFollowUserNewQuestionAnswerMail(CommunityUserDO targetCommunityUser,
			SearchResult<QuestionAnswerDO> questionAnswers,
			Date batchDate) {
		if (targetCommunityUser == null || !targetCommunityUser.isActive()) {
			return;
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", targetCommunityUser);
		dataMap.put("questionAnswers", questionAnswers);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = MailType.FOLLOW_USER_NEW_ANSWER_SUMMARY.createMailInfo(
				targetCommunityUser, dataMap);
		sendMail(mailInfo, batchDate, targetCommunityUser.getCommunityUserId());
	}

	/**
	 * フォローしたユーザの画像のメールを送信します。
	 * @param targetCommunityUser 宛先ユーザー
	 * @param imageSets 画像セットリスト
	 * @param batchDate バッチ日付
	 */
	private void sendFollowUserNewImageMail(CommunityUserDO targetCommunityUser,
			SearchResult<MailImageSetVO> imageSets,
			Date batchDate) {
		if (targetCommunityUser == null || !targetCommunityUser.isActive()) {
			return;
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", targetCommunityUser);
		dataMap.put("imageSets", imageSets);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = MailType.FOLLOW_USER_NEW_IMAGE_SUMMARY.createMailInfo(
				targetCommunityUser, dataMap);
		sendMail(mailInfo, batchDate, targetCommunityUser.getCommunityUserId());
	}

	/**
	 * フォローした商品のレビューのメールを送信します。
	 * @param targetCommunityUser 宛先ユーザー
	 * @param reviews レビューリスト
	 * @param batchDate バッチ日付
	 */
	private void sendFollowProductNewReviewMail(CommunityUserDO targetCommunityUser,
			SearchResult<ReviewDO> reviews,
			Date batchDate) {
		if (targetCommunityUser == null || !targetCommunityUser.isActive()) {
			return;
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", targetCommunityUser);
		dataMap.put("reviews", reviews);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = MailType.FOLLOW_PRODUCT_NEW_REVIEW_SUMMARY.createMailInfo(
				targetCommunityUser, dataMap);
		sendMail(mailInfo, batchDate, targetCommunityUser.getCommunityUserId());
	}

	/**
	 * フォローした商品の質問のメールを送信します。
	 * @param targetCommunityUser 宛先ユーザー
	 * @param questions 質問リスト
	 * @param batchDate バッチ日付
	 */
	private void sendFollowProductNewQuestionMail(CommunityUserDO targetCommunityUser,
			SearchResult<QuestionDO> questions,
			Date batchDate) {
		if (targetCommunityUser == null || !targetCommunityUser.isActive()) {
			return;
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", targetCommunityUser);
		dataMap.put("questions", questions);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = MailType.FOLLOW_PRODUCT_NEW_QUESTION_SUMMARY.createMailInfo(
				targetCommunityUser, dataMap);
		sendMail(mailInfo, batchDate, targetCommunityUser.getCommunityUserId());
	}

	/**
	 * フォローした商品の画像のメールを送信します。
	 * @param targetCommunityUser 宛先ユーザー
	 * @param imageSets 画像セットリスト
	 * @param batchDate バッチ日付
	 */
	private void sendFollowProductNewImageMail(CommunityUserDO targetCommunityUser,
			SearchResult<MailImageSetVO> imageSets,
			Date batchDate) {
		if (targetCommunityUser == null || !targetCommunityUser.isActive()) {
			return;
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", targetCommunityUser);
		dataMap.put("imageSets", imageSets);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = MailType.FOLLOW_PRODUCT_NEW_IMAGE_SUMMARY.createMailInfo(
				targetCommunityUser, dataMap);
		sendMail(mailInfo, batchDate, targetCommunityUser.getCommunityUserId());
	}

	/**
	 * フォローした質問の回答のメールを送信します。
	 * @param targetCommunityUser 宛先ユーザー
	 * @param questionAnswers 質問回答リスト
	 * @param batchDate バッチ日付
	 */
	private void sendFollowQuestionNewAnswerMail(CommunityUserDO targetCommunityUser,
			SearchResult<QuestionAnswerDO> questionAnswers,
			Date batchDate) {
		if (targetCommunityUser == null || !targetCommunityUser.isActive()) {
			return;
		}
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("toUser", targetCommunityUser);
		dataMap.put("questionAnswers", questionAnswers);
		dataMap.put("serviceConfig", serviceConfig);
		MailInfoDO mailInfo = MailType.FOLLOW_QUESTION_NEW_ANSWER_SUMMARY.createMailInfo(
				targetCommunityUser, dataMap);
		sendMail(mailInfo, batchDate, targetCommunityUser.getCommunityUserId());
	}

	/**
	 * メールを送信します。
	 * @param mailInfo メール情報
	 * @param batchDate バッチ日付
	 * @param contentsId コンテンツID
	 */
	private void sendMail(MailInfoDO mailInfo, Date batchDate, String contentsId) {
		sendMail(mailInfo);
	}

	private void print(String message){
		LOG.info(message);
		System.out.println(message);
		System.out.flush();
	}
	
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
		sendMail(mailInfo);
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
		sendMail(mailInfo);
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
		sendMail(mailInfo);
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
		sendMail(mailInfo);
	}
	
	private void sendMail(MailInfoDO mailInfo){
		Properties props = new Properties();
		props.put("mail.smtp.host", "192.168.80.73");

		Session session = Session.getInstance(props, null);
		
		try{
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("community@kickmogu.com"));
			InternetAddress[] address = {new InternetAddress("sugimoto@kickmogu.com")};
			message.setRecipients(Message.RecipientType.TO, address);
			message.setSubject(mailInfo.getTitle());
			message.setSentDate(new Date());
			
			Multipart multipart = new MimeMultipart("alternative");
			
			MimeBodyPart textPart = new MimeBodyPart();
			textPart.setText(mailInfo.getTextBody().replaceAll("@@BR@@", "\n"), "iso-2022-jp");
			
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(mailInfo.getHtmlBody().replaceAll("@@BR@@", "\n"), "text/html; charset=iso-2022-jp");
			
			multipart.addBodyPart(textPart);
			multipart.addBodyPart(htmlPart);
			
			message.setContent(multipart);
			
			Transport.send(message);
		}catch(MessagingException mex){
			System.out.println("¥n--Exception handling in msgsendsample.java");
			mex.printStackTrace();
		}
	}
}
