/**
 *
 */
package com.kickmogu.yodobashi.community.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.common.utils.DateUtil;
import com.kickmogu.yodobashi.community.resource.dao.CommentDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.EventHistoryDao;
import com.kickmogu.yodobashi.community.resource.dao.ImageDao;
import com.kickmogu.yodobashi.community.resource.dao.MailSettingDao;
import com.kickmogu.yodobashi.community.resource.dao.OrderDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductMasterDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionAnswerDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.ReviewDao;
import com.kickmogu.yodobashi.community.resource.dao.SendMailDao;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
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
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.EventHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSendTiming;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSettingType;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailType;
import com.kickmogu.yodobashi.community.resource.domain.constants.MaintenanceStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.PurchaseHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewType;
import com.kickmogu.yodobashi.community.resource.domain.constants.Verification;
import com.kickmogu.yodobashi.community.service.BatchMailService;
import com.kickmogu.yodobashi.community.service.ReviewService;
import com.kickmogu.yodobashi.community.service.SystemMaintenanceService;
import com.kickmogu.yodobashi.community.service.config.ServiceConfig;
import com.kickmogu.yodobashi.community.service.vo.CommunityUserFollowVO;
import com.kickmogu.yodobashi.community.service.vo.MailImageSetVO;

/**
 * バッチメールサービスです。
 * @author kamiike
 */
@Service
public class BatchMailServiceImpl implements BatchMailService {

	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger LOG = LoggerFactory.getLogger(BatchMailServiceImpl.class);

	/**
	 * コミュニティユーザー DAO です。
	 */
	@Autowired
	private CommunityUserDao communityUserDao;

	/**
	 * レビュー DAO です。
	 */
	@Autowired
	private ReviewDao reviewDao;

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
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	private RequestScopeDao requestScopeDao;

	/**
	 * 質問回答 DAO です。
	 */
	@Autowired
	private QuestionAnswerDao questionAnswerDao;

	/**
	 * 画像 DAO です。
	 */
	@Autowired
	private ImageDao imageDao;

	/**
	 * コミュニティユーザーフォロー DAO です。
	 */
	@Autowired
	private CommunityUserFollowDao communityUserFollowDao;

	/**
	 * コメント DAO です。
	 */
	@Autowired
	private CommentDao commentDao;

	/**
	 * 商品フォロー DAO です。
	 */
	@Autowired
	private ProductFollowDao productFollowDao;

	/**
	 * 質問フォロー DAO です。
	 */
	@Autowired
	private QuestionFollowDao questionFollowDao;

	/**
	 * 注文 DAO です。
	 */
	@Autowired
	private OrderDao orderDao;

	/**
	 * メール設定 DAO です。
	 */
	@Autowired
	private MailSettingDao mailSettingDao;

	/**
	 * メール送信 DAO です。
	 */
	@Autowired @Qualifier("xi")
	private SendMailDao sendMailDao;

	/**
	 * イベント履歴 DAO です。
	 */
	@Autowired
	private EventHistoryDao eventHistoryDao;

	/**
	 * サービスコンフィグです。
	 */
	@Autowired
	private ServiceConfig serviceConfig;
	
	@Autowired
	private ReviewService reviewService;

	@Autowired private SystemMaintenanceService systemMaintenanceService;

	
	/**
	 * 指定したコミュニティユーザーに対してメールを送信します。
	 * @param communityUser コミュニティユーザー
	 * @param targetDate 対象日付
	 */
	@Override
	@ArroundSolr
	@ArroundHBase
	public void sendMail(CommunityUserDO communityUser, Date targetDate) {
		int limit = serviceConfig.readLimit;
		int mailListLimit = serviceConfig.mailListLimit;
		int offset = 0;

		if(!systemMaintenanceService.getWebBatchMailStatusWithCache().equals(MaintenanceStatus.IN_OPERATION)) {
			return;
		}

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
					Date pointBaseTime = new Date();
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
							communityUser.getCommunityUserId(), 
							targetDate,
							mailListLimit,
							0);
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
		String uniqueId = IdUtil.createIdByConcatIds(IdUtil.formatDate(batchDate),
				mailInfo.getMailType().getCode(), contentsId);
		if (!eventHistoryDao.existsLog(
				uniqueId,
				EventHistoryType.MAIL)) {
			sendMailDao.sendMail(mailInfo);
			eventHistoryDao.saveLog(uniqueId,
					EventHistoryType.MAIL);
		} else {
			LOG.warn("duplicate batch mail. ignore this mail. "
					+ "uniqueId=" + uniqueId
					+ ", communityUserId=" + mailInfo.getCommunityUser().getCommunityUserId()
					+ ", communityId=" +  mailInfo.getCommunityUser().getCommunityId()
					+ ", mailType=" + mailInfo.getMailType());
		}
	}

	private void print(String message){
		LOG.info(message);
		System.out.println(message);
		System.out.flush();
	}

}
