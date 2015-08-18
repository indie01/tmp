package com.kickmogu.yodobashi.community.service;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.FillType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PurchaseProductSearchCondition;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewType;

/**
 * 閲覧系のテストクラスです。
 *
 * @author hirabayashi
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/jcclientContext.xml")
public class ViewServiceTest extends DataSetTest {

	/**
	 * マイページサービスです。
	 */
	@Autowired
	protected MyPageService myPageService;

	/**
	 * オーダーサービスです。
	 */
	@Autowired
	protected OrderService orderService;

	/**
	 * 商品サービスです。
	 */
	@Autowired
	protected ProductService productService;

	/**
	 * ソーシャルメディア連携サービスです。
	 */
	@Autowired
	protected SocialMediaService socialMediaService;

	/**
	 * 関係するユーザーを検索するサービスです。
	 */
	@Autowired
	protected SocialUserFindService socialUserFindService;

	/**
	 * 移行ユーザーサービスです。
	 */
	@Autowired
	protected MigrationUserService migrationUserService;

	/**
	 * リクエストスコープで管理するオブジェクトを扱うサービスです。
	 */
	@Autowired
	protected RequestScopeService requestScopeService;

	/**
	 * データを初期化します。
	 */
	protected void initialize() {
		super.initialize();
	}

	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() {
		initialize();
	}

	/**
	 * 参照系のテストを実行します。
	 */
	@Test
	public void testView() {
		Date offsetTime = null;
		int limit = 10;
		int offset = 0;
		// マイページサービスを参照します。
		myPageService.getMyPageInfoAreaByCommunityUserId(communityUser.getCommunityUserId(), limit, limit, limit, false);
		myPageService.findNoReadInformationByCommunityUserId(communityUser.getCommunityUserId(), limit, offset);
		myPageService.findInformationByCommunityUserId(communityUser.getCommunityUserId(), 5, offsetTime, false);
		myPageService.countNoReadInformation(communityUser.getCommunityUserId());
		// ユーザーサービスを参照します。
		userService.getUserPageInfoAreaByCommunityUserId(communityUser.getCommunityUserId(), limit, limit, limit);
		userService.findNewsFeedByCommunityUserId(communityUser.getCommunityUserId(), limit, offsetTime, false);
		userService.getCommunityUserIdByCommunityName(communityUser.getCommunityName());
		userService.getCommunityUserByHashCommunityId(commentUser.getHashCommunityId());
		userService.getCommunityUserSetByCommunityName(communityUser.getCommunityName());
		userService.getCommunityUserByCommunityName(communityUser.getCommunityName(), product.getSku());
		Set<String> providerUserIds = new HashSet<String>();
		providerUserIds.add("123");
		userService.findCommunityUserBySocialProviderUserIds("twitter", providerUserIds);
		List<String> communityUserIds = new ArrayList<String>();
		communityUserIds.add(commentUser.getCommunityUserId());
		userService.duplicateCommunityName(communityUser.getCommunityId(), "communityUser");
		userService.findMailSettingList(commentUser.getCommunityUserId());
		userService.isShowWelcomeHint(communityUser.getCommunityUserId());
		userService.findSocialMediaSettingList(communityUser.getCommunityUserId());
		//TODO
//		reviewService.getReviewStatistics(ElapsedMonth.AFTERFOURMONTH, product.getSku());
		reviewService.findPurchaseLostProductBySku(product.getSku(), limit);
		reviewService.findUsedProductBySku(product.getSku(), limit);
		reviewService.countReviewBySku(product.getSku());
		//TODO
//		reviewService.findNewReviewBySku(product.getSku(), ElapsedMonth.AFTERFIVEMONTH, limit, offsetTime, false);
//		reviewService.findMatchReviewBySku(product.getSku(), ElapsedMonth.AFTERFIVEMONTH, limit,
//				null, offsetTime, false);
		reviewService.findReviewExcludeSKUByCommunityUserId(communityUser.getCommunityUserId(), product2.getSku(), limit, offset);
		reviewService.findReviewExcludeReviewIdByCommunityUserId(
				communityUser.getCommunityUserId(), product.getSku(), review.getReviewId(), limit, offset);
		List<String> decisivePurchaseIds = new ArrayList<String>();
		decisivePurchaseIds.add(review.getReviewDecisivePurchases().get(0).getReviewDecisivePurchaseId());
		//TODO
//		reviewService.findDecisivePurchaseBySKU(product.getSku(), decisivePurchaseIds, true, limit, offset, 7);
		reviewService.findProductSatisfactionBySKU(product.getSku(), 7);
		reviewService.findReviewByCommunityUserId(communityUser.getCommunityUserId(), limit, offsetTime, false);
		reviewService.findTemporaryReviewByCommunityUserId(communityUser.getCommunityUserId(), null, limit, offsetTime, false);
		reviewService.canPostReview(communityUser.getCommunityUserId(), product.getSku(), ReviewType.REVIEW_AFTER_FEW_DAYS);
		reviewService.getTemporaryReview(communityUser.getCommunityUserId(), product.getSku(), ReviewType.REVIEW_AFTER_FEW_DAYS);
//		reviewService.hasTemporaryReview(communityUser.getCommunityUserId(), product.getSku(), ReviewType.REVIEW_AFTER_FEW_DAYS);
		ReviewDO getReview = reviewService.getReview(review.getReviewId());
		assertNotNull(getReview.getCommunityUser().getCommunityName());
		reviewService.getReviewFromIndex(review.getReviewId(),false);
		// 質問サービスを参照します。
		questionService.countQuestionBySku(product.getSku());
		questionService.findUpdateQuestionBySku(product.getSku(),null, limit, offsetTime, false);
		questionService.findNewQuestionBySku(product.getSku(),null, limit, offsetTime, false);
		questionService.findPopularQuestionBySku(product.getSku(),null, limit, null, offsetTime, false);
		questionService.getQuestionFromIndex(question.getQuestionId(),false);
		questionService.findPopularQuestionExcudeQuestionId(product.getSku(), question.getQuestionId(), limit, offset);
		questionService.findNewQuestionExcudeQuestionId(product.getSku(), question.getQuestionId(), limit, offset);
		questionService.findQuestionByCommunityUserId(communityUser.getCommunityUserId(), limit, offsetTime, false);
		questionService.findTemporaryQuestionByCommunityUserId(communityUser.getCommunityUserId(),null, limit, offsetTime, false);
		questionService.findQuestionAnswerByCommunityUserId(communityUser.getCommunityUserId(), limit, offsetTime, false);
		questionService.findTemporaryQuestionAnswerByCommunityUserId(communityUser.getCommunityUserId(),null, limit, offsetTime, false);
		questionService.findNewQuestionByPurchaseProduct(communityUser.getCommunityUserId(), limit, offsetTime, false);
		questionService.getTemporaryQuestion(communityUser.getCommunityUserId(), product.getSku());
//		questionService.hasTemporaryQuestion(communityUser.getCommunityUserId(), product.getSku());
		questionService.getQuestion(question.getQuestionId());
		questionService.getQuestionAnswerFromIndex(questionAnswer.getQuestionAnswerId(),false);
		questionService.hasQuestionAnswer(communityUser.getCommunityUserId(), question.getQuestionId());
		questionService.getTemporaryQuestionAnswer(communityUser.getCommunityUserId(), question.getQuestionId());
//		questionService.hasTemporaryQuestionAnswer(communityUser.getCommunityUserId(), question.getQuestionId());
		questionService.findNewQuestionAnswerByQuestionId(question.getQuestionId(), null, limit, offsetTime, false);
		questionService.findMatchQuestionAnswerByQuestionId(question.getQuestionId(), null, limit, null, offsetTime, false);
		// いいねサービスを参照します。
		likeService.findLikeByImageId(image.getImageId(), null, limit);
		likeService.findLikeByReviewId(review.getReviewId(), null, limit);
		likeService.findLikeByQuestionAnswerId(questionAnswer.getQuestionAnswerId(), null, limit);
		likeService.findLikeCommunityUserByImageId(image.getImageId(), limit);
		likeService.findLikeCommunityUserByReviewId(review.getReviewId(), limit);
		likeService.findLikeCommunityUserByQuestionAnswerId(questionAnswer.getQuestionAnswerId(), limit);
		// コメントサービスを参照します。
		commentService.getCommentFromIndex(reviewComment.getCommentId(),false);
		commentService.findImageCommentByImageId(image.getImageId(), null, limit, offsetTime, false);
		commentService.findReviewCommentByReviewId(review.getReviewId(), null, limit, offsetTime, false);
		commentService.findQuestionAnswerCommentByQuestionAnswerId(
				questionAnswer.getQuestionAnswerId(), null, limit, offsetTime, false);
		// フォローサービスを参照します。
		followService.findFollowCommunityUser(communityUser.getCommunityUserId(), limit, 7, offsetTime, false);
		followService.findFollowerCommunityUser(communityUser.getCommunityUserId(), limit, 7, offsetTime, false);
		followService.findFollowProduct(followerUser.getCommunityUserId(), limit, 7, offsetTime, false);
		followService.findFollowQuestion(communityUser.getCommunityUserId(), limit, 7, offsetTime, false);
		// オーダーサービスを参照します。
		orderService.existsOrder(communityUser.getCommunityUserId(), product.getSku());
		orderService.getOrder(communityUser.getCommunityUserId(), product.getSku());
		// 商品サービスを参照します。
		productService.findNewsFeedBySku(product.getSku(), limit, offsetTime, true);
		productService.findPurchaseProductByCommunityUserIdForMyPage(communityUser.getCommunityUserId(), PurchaseProductSearchCondition.NO_MY_REVIEW, limit, 7, offsetTime, null, false);
		productService.findPurchaseProductByCommunityUserIdForMyPage(communityUser.getCommunityUserId(), PurchaseProductSearchCondition.NO_REVIEW, limit, 7, offsetTime, null, false);
		productService.findPurchaseProductByCommunityUserIdForMyPage(communityUser.getCommunityUserId(), PurchaseProductSearchCondition.HAS_WAIT_ANSWER, limit, 7, offsetTime, null, false);
		productService.findPurchaseProductByCommunityUserIdForMyPage(communityUser.getCommunityUserId(), null,  limit, 7, offsetTime, null, false);
		productService.findPurchaseProductByCommunityUserIdForUserPage(communityUser.getCommunityUserId(), null, limit, 7, offsetTime, null, false);
		productService.findProductMasterBySku(product.getSku(), limit);
		productService.findNewPurchaseDateProductMasterByCommunityUserId(communityUser.getCommunityUserId(), limit, 7, offsetTime, product.getSku(), false);
		productService.findRankProductMasterByCommunityUserId(communityUser.getCommunityUserId(), limit, 7, null, null, false, false);
		productService.getProductBySku(product.getSku());
		productService.getProductBySku(product.getSku(), FillType.LARGE, null);
		productService.findProductByKeyword("本文",null, false, false);
		productService.getNextProductMasterVersion();
		// ソーシャルメディア連携サービスを参照します。
//		socialMediaService.getUserProfileImage(SocialMediaType.TWITTER, communityUser.getCommunityUserId());
		// 画像サービスを参照します。
		//imageService.findImageBySku(product.getSku(), limit, offsetTime,null, 1, true, true);
		imageService.findImageByImageSetId(image.getImageId(), null);
		imageService.findImageByCommunityUserId(communityUser.getCommunityUserId(), limit, offsetTime, false);
		imageService.getImageHeaderFromIndex(image.getImageId(),false);
		imageService.getTemporaryImage(image.getImageId());

		// 関係するユーザーを検索するサービスを参照します。
		socialUserFindService.findReviewerBySKU(product.getSku(), communityUser.getCommunityUserId(), limit, offset);
		socialUserFindService.findAnswererBySKU(product.getSku(), limit, offset);
		socialUserFindService.findProductFollowerBySKU(product.getSku(), limit, offset, true);
		socialUserFindService.findAllTypeImagePostCommunityUserBySKU(product.getSku(), limit, offset);
		socialUserFindService.findImagePostCommunityUserBySKU(product.getSku(), communityUser.getCommunityUserId(), limit, offset);
		socialUserFindService.findReviewerByFollowProduct(communityUser.getCommunityUserId(), limit, offset);
		socialUserFindService.findAnswererByFollowProduct(communityUser.getCommunityUserId(), limit, offset);
		socialUserFindService.findAnswererByPostQuestion(communityUser.getCommunityUserId(), limit, offset);
		socialUserFindService.findAnswererByFollowQuestion(communityUser.getCommunityUserId(), limit, offset);
		socialUserFindService.findProductMasterByFollowProduct(communityUser.getCommunityUserId(), limit, offset);
		socialUserFindService.findReviewerByPurchaseProduct(communityUser.getCommunityUserId(), limit, offset, false);
		socialUserFindService.findAnswererByQuestionForPurchaseProduct(communityUser.getCommunityUserId(), limit, offset, false);
		socialUserFindService.findLikeCommunityUserByReview(communityUser.getCommunityUserId(), limit, offset);
		socialUserFindService.findLikeCommunityUserByQuestionAnswer(communityUser.getCommunityUserId(), limit, offset);
		socialUserFindService.findLikeCommunityUserByImage(communityUser.getCommunityUserId(), limit, offset);
		socialUserFindService.findFollowerByPurchaseProduct(communityUser.getCommunityUserId(), limit, offset, false);
		List<String> skus = new ArrayList<String>();
		skus.add(product.getSku());
		socialUserFindService.findReviewerByViewProducts(skus, limit, offset);
		socialUserFindService.findAnswererByViewProducts(skus, limit, offset);
		socialUserFindService.findCommunityUserByPartialMatch(communityUser.getCommunityUserId(), "", "", limit);
		// 違反報告サービスを参照します。
		// リクエストスコープで管理するオブジェクトを扱うサービスを参照します。
		requestScopeService.initialize(communityUser, null);
		requestScopeService.getConnectionRepository();
		requestScopeService.getAdultVerification();
		requestScopeService.getCeroVerification();
		requestScopeService.getFacebook();
		requestScopeService.getTwitter();
		requestScopeService.destroy();
	}
}
