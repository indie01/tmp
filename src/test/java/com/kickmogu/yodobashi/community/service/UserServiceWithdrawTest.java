package com.kickmogu.yodobashi.community.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.lib.solr.SolrConstants;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.AnnounceDO;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.MailSettingDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseLostProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDecisivePurchaseDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.SocialMediaSettingDO;
import com.kickmogu.yodobashi.community.resource.domain.SpamReportDO;
import com.kickmogu.yodobashi.community.resource.domain.UsedProductDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommentTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;

/**
 * ユーザーサービスのテストクラスです。
 *
 * @author hirabayashi
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/jcclientContext.xml")
public class UserServiceWithdrawTest extends DataSetTest {

	/**
	 * データを初期化します。
	 */
	protected void initialize() {
		// 親クラスのinitializeを呼び出す
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
	 * 退会の実行・検証を行います。(自主退会・全てデータを残さない)
	 */
	@Test
	public void testWithdrawWithDelete() {
		testWithdrawCommunityUserSet();
		boolean withImage = false;
		boolean reviewDelete = true;
		boolean qaDelete = true;
		boolean imageDelete = true;
		boolean commentDelete = true;
		// 退会の実行・検証を行います。
		testWithdraw(
				communityUser.getCommunityUserId(),
				CommunityUserStatus.INVALID,
				reviewDelete,
				qaDelete,
				imageDelete,
				commentDelete
				);
		// 再入会を実行・検証します。
		testReCreateCommunityUser(
				communityUser.getCommunityUserId(),
				"reCreateCommunityUser",
				withImage,
				reviewDelete,
				qaDelete,
				imageDelete,
				commentDelete
				);
	}

	/**
	 * 退会の実行・検証を行います。(自主退会・全てデータを残こす)
	 */
	@Test
	public void testWithdraw() {
		testWithdrawCommunityUserSet();
		boolean withImage = false;
		boolean reviewDelete = false;
		boolean qaDelete = false;
		boolean imageDelete = false;
		boolean commentDelete = false;
		// 退会の実行・検証を行います。
		testWithdraw(
				communityUser.getCommunityUserId(),
				CommunityUserStatus.INVALID,
				reviewDelete,
				qaDelete,
				imageDelete,
				commentDelete
				);
		// 再入会を実行・検証します。
		testReCreateCommunityUser(
				communityUser.getCommunityUserId(),
				"reCreateCommunityUser",
				withImage,
				reviewDelete,
				qaDelete,
				imageDelete,
				commentDelete
				);
	}

	/**
	 * 退会の実行・検証を行います。
	 * 自主退会
	 * データを残さない…レビュー、画像
	 * データを残こす…質問、コメント
	 */
	@Test
	public void testWithdrawMix() {
		testWithdrawCommunityUserSet();
		boolean withImage = false;
		boolean reviewDelete = true;
		boolean qaDelete = false;
		boolean imageDelete = true;
		boolean commentDelete = false;
		// 退会の実行・検証を行います。
		testWithdraw(
				communityUser.getCommunityUserId(),
				CommunityUserStatus.INVALID,
				reviewDelete,
				qaDelete,
				imageDelete,
				commentDelete
				);
		// 再入会を実行・検証します。
		testReCreateCommunityUser(
				communityUser.getCommunityUserId(),
				"reCreateCommunityUser",
				withImage,
				reviewDelete,
				qaDelete,
				imageDelete,
				commentDelete
				);
	}

	/**
	 * 退会の実行・検証を行います。
	 * 自主退会
	 * データを残さない…質問、コメント
	 * データを残こす…レビュー、画像
	 */
	@Test
	public void testWithdrawMix2() {
		testWithdrawCommunityUserSet();
		boolean withImage = false;
		boolean reviewDelete = false;
		boolean qaDelete = true;
		boolean imageDelete = false;
		boolean commentDelete = true;
		// 退会の実行・検証を行います。
		testWithdraw(
				communityUser.getCommunityUserId(),
				CommunityUserStatus.INVALID,
				reviewDelete,
				qaDelete,
				imageDelete,
				commentDelete
				);
		// 再入会を実行・検証します。
		testReCreateCommunityUser(
				communityUser.getCommunityUserId(),
				"reCreateCommunityUser",
				withImage,
				reviewDelete,
				qaDelete,
				imageDelete,
				commentDelete
				);
	}

	/**
	 * 退会の実行・検証を行います。(強制退会)
	 */
	@Test
	public void testWithdrawByForce() {
		testWithdrawCommunityUserSet();
		// 退会の実行・検証を行います。
		testWithdraw(communityUser.getCommunityUserId(), CommunityUserStatus.FORCE_LEAVE, true, false, true, false);
		// 退会キャンセルを実行・検証します。
		testCancelWithdraw(communityUser.getCommunityUserId());
	}

	/**
	 * 退会の実行・検証を行います。(強制退会)
	 */
	@Test
	public void testWithdrawByForceTwo() {
		testWithdrawCommunityUserSet();
		checkCommunityUser(communityUser.getCommunityUserId(), CommunityUserStatus.ACTIVE);
		// 退会の実行・検証を行います。
		testWithdraw(communityUser.getCommunityUserId(), CommunityUserStatus.FORCE_LEAVE, false, true, false, true);
		// 退会キャンセルを実行・検証します。
		testCancelWithdraw(communityUser.getCommunityUserId());
	}

	/**
	 * 停止状態の更新を検証します。
	 */
	@Test
	public void testUpdateStop() {
		testWithdrawCommunityUserSet();
		// アカウントを停止します。
		testUpdateStop(communityUser.getCommunityUserId(), true);
		// アカウントを復帰します。
		testUpdateStop(communityUser.getCommunityUserId(), false);
	}

	/**
	 * 外部退会の実行・検証を行います。
	 */
	@Test
	public void testSyncCommunityUserStatusForWithdraw() {
		testWithdrawCommunityUserSet();
		testSyncCommunityUserStatusForWithdraw(communityUser);
	}

	/**
	 * 退会テスト用の会員データを作成します。
	 */
	private void testWithdrawCommunityUserSet() {
		CommunityUserDO contentsCommunityUser = createCommunityUser("contentsCommunityUser", false);
		Date salesDate = new Date();
		createReceipt(contentsCommunityUser, "4905524312737", salesDate);
		// 質問1 質問者が退会者
		QuestionDO question = createQuestionSet(communityUser);
		QuestionAnswerDO questionAnswer = saveQuestionAnswer(question, answerUser);
		saveCommentByQuestionAnswer(questionAnswer, communityUser);
		// 質問2 解答者が退会者
		question = createQuestionSet(contentsCommunityUser);
		questionAnswer = saveQuestionAnswer(question, communityUser);
		saveCommentByQuestionAnswer(questionAnswer, contentsCommunityUser);
		// レビュー1
		ReviewDO review = createReviewSet(contentsCommunityUser);
		saveComment(review, communityUser);
		likeService.updateLikeReview(communityUser.getCommunityUserId(), review.getReviewId(), false);
		// 画像1
		List<ImageHeaderDO> imageHeaders = testUploadImageSet(communityUser, 10, product);
		CommentDO comment = new CommentDO();
		comment.setCommunityUser(contentsCommunityUser);
		comment.setTargetType(CommentTargetType.IMAGE);
		comment.setImageHeader(imageHeaders.get(0));
		comment.setCommentBody("画像コメント");
		commentService.saveComment(comment);
		// 画像2
		imageHeaders = testUploadImageSet(contentsCommunityUser, 10, product);
		comment = new CommentDO();
		comment.setCommunityUser(communityUser);
		comment.setTargetType(CommentTargetType.IMAGE);
		comment.setImageHeader(imageHeaders.get(0));
		comment.setCommentBody("画像コメント");
		commentService.saveComment(comment);

		followService.followQuestion(
				contentsCommunityUser.getCommunityUserId(), question.getQuestionId(), false);
		followService.followCommunityUser(
				communityUser.getCommunityUserId(),
				contentsCommunityUser.getCommunityUserId(), false);
		followService.followProduct(
				communityUser.getCommunityUserId(), "100000001000624829", false);
	}

	/**
	 * 退会の実行・検証を行います。
	 * @param communityUserId
	 * @param force
	 * @param reviewDelete
	 * @param qaDelete
	 * @param imageDelete
	 * @param commentDelete
	 */
	private void testWithdraw(
			String communityUserId,
			CommunityUserStatus communityUserStatus,
			boolean reviewDelete,
			boolean qaDelete,
			boolean imageDelete,
			boolean commentDelete) {
		Map<String, Integer> sizeMap = getSizeMapByHBase(communityUserId);
		// 退会します。
		boolean force = false;
		if(CommunityUserStatus.FORCE_LEAVE.equals(communityUserStatus)) {
			force = true;
		} else if(CommunityUserStatus.INVALID.equals(communityUserStatus)) {
			force = false;
		}
		userService.withdraw(communityUserId, force, reviewDelete, qaDelete, imageDelete, commentDelete, false);
		// コミュニティユーザーを取得します。
		CommunityUserDO communityUserByHBase = getCommunityUserByHbase(communityUserId);
		CommunityUserDO communityUserBySolr = getCommunityUserBySolr(communityUserId);
		// 退会を検証します。
		checkWithdrawCommunityUser(communityUserByHBase, communityUserStatus, reviewDelete, qaDelete, imageDelete, commentDelete, sizeMap);
		assertNotNull(communityUserByHBase.getWithdrawKey());
		checkWithdrawCommunityUser(communityUserBySolr, communityUserStatus, reviewDelete, qaDelete, imageDelete, commentDelete, sizeMap);
	}

	/**
	 * 外部退会の実行・確認を行います。
	 * @param communityUser
	 */
	private void testSyncCommunityUserStatusForWithdraw(CommunityUserDO communityUser) {
		Map<String, Integer> sizeMap = getSizeMapByHBase(communityUser.getCommunityUserId());
		// 退会します。
		userService.syncCommunityUserStatusForWithdraw(communityUser.getCommunityId());
		// コミュニティユーザーを取得します。
		CommunityUserDO communityUserByHBase = getCommunityUserByHbase(communityUser.getCommunityUserId());
		CommunityUserDO communityUserBySolr = getCommunityUserBySolr(communityUser.getCommunityUserId());
		// 退会を検証します。
		checkWithdrawCommunityUser(communityUserByHBase, CommunityUserStatus.INVALID, false, false, false, false, sizeMap);
		assertNotNull(communityUserByHBase.getWithdrawKey());
		checkWithdrawCommunityUser(communityUserBySolr, CommunityUserStatus.INVALID, false, false, false, false, sizeMap);
	}

	private void checkWithdrawCommunityUser(
			CommunityUserDO communityUser,
			CommunityUserStatus communityUserStatus,
			boolean reviewDelete,
			boolean qaDelete,
			boolean imageDelete,
			boolean commentDelete,
			Map<String, Integer> sizeMap) {
		assertNotNull(communityUser);
		GetCommunityUserData getCommunityUserData = new GetCommunityUserData(communityUser.getCommunityUserId());
		List<ReviewDO> reviewsByHBase = getCommunityUserData.getReviewsByHBase();
		List<ReviewDO> reviewsBySolr = getCommunityUserData.getReviewsBySolr();
		List<QuestionDO> questionsByHBase = getCommunityUserData.getQuestionsByHBase();
		List<QuestionDO> questionsBySolr = getCommunityUserData.getQuestionsBySolr();
		List<QuestionAnswerDO> questionAnswersByHBase = getCommunityUserData.getQuestionAnswersByHBase();
		List<QuestionAnswerDO> questionAnswersBySolr = getCommunityUserData.getQuestionAnswersBySolr();
		List<ImageHeaderDO> imageHeaderByHBase = getCommunityUserData.getImageHeaderByHBase();
		List<ImageHeaderDO> imageHeaderBySolr = getCommunityUserData.getImageHeaderBySolr();
		List<CommentDO> relationReviewOwnerCommentsByHBase = getCommunityUserData.getRelationReviewOwnerCommentsByHBase();
		List<CommentDO> relationReviewOwnerCommentsBySolr = getCommunityUserData.getRelationReviewOwnerCommentsBySolr();
		List<CommentDO> relationQuestionOwnerCommentsByHBase = getCommunityUserData.getRelationQuestionOwnerCommentsByHBase();
		List<CommentDO> relationQuestionOwnerCommentsBySolr = getCommunityUserData.getRelationQuestionOwnerCommentsBySolr();
		List<CommentDO> relationQuestionAnswerOwnerCommentsByHBase = getCommunityUserData.getRelationQuestionAnswerOwnerCommentsByHBase();
		List<CommentDO> relationQuestionAnswerOwnerCommentsBySolr = getCommunityUserData.getRelationQuestionAnswerOwnerCommentsBySolr();
		List<CommentDO> relationImageOwnerCommentsByHBase = getCommunityUserData.getRelationImageOwnerCommentsByHBase();
		List<CommentDO> relationImageOwnerCommentsBySolr = getCommunityUserData.getRelationImageOwnerCommentsBySolr();
		List<ActionHistoryDO> actionUserWithdrawActionHistorysByHBase = getCommunityUserData.getActionUserWithdrawActionHistorysByHBase();
		List<ActionHistoryDO> actionUserWithdrawActionHistorysBySolr = getCommunityUserData.getActionUserWithdrawActionHistorysBySolr();
		List<CommunityUserFollowDO> communityUserFollowsByHBase = getCommunityUserData.getCommunityUserFollowsByHBase();
		List<CommunityUserFollowDO> communityUserFollowsBySolr = getCommunityUserData.getCommunityUserFollowsBySolr();
		List<CommunityUserFollowDO> followCommunityUserFollowsByHBase = getCommunityUserData.getFollowCommunityUserFollowsByHBase();
		List<CommunityUserFollowDO> followCommunityUserFollowsBySolr = getCommunityUserData.getFollowCommunityUserFollowsBySolr();
		List<InformationDO> informationsByHBase = getCommunityUserData.getInformationsByHBase();
		List<InformationDO> informationsBySolr = getCommunityUserData.getInformationsBySolr();
		List<InformationDO> relationQuestionOwnerInformationsByHBase = getCommunityUserData.getRelationQuestionOwnerInformationsByHBase();
		List<InformationDO> relationQuestionOwnerInformationsBySolr = getCommunityUserData.getRelationQuestionOwnerInformationsBySolr();
		List<InformationDO> relationQuestionAnswerOwnerInformationsByHBase = getCommunityUserData.getRelationQuestionAnswerOwnerInformationsByHBase();
		List<InformationDO> relationQuestionAnswerOwnerInformationsBySolr = getCommunityUserData.getRelationQuestionAnswerOwnerInformationsBySolr();
		List<InformationDO> relationCommentOwnerInformationsByHBase = getCommunityUserData.getRelationCommentOwnerInformationsByHBase();
		List<InformationDO> relationCommentOwnerInformationsBySolr = getCommunityUserData.getRelationCommentOwnerInformationsBySolr();
		List<InformationDO> relationLikeOwnerInformationsByHBase = getCommunityUserData.getRelationLikeOwnerInformationsByHBase();
		List<InformationDO> relationLikeOwnerInformationsBySolr = getCommunityUserData.getRelationLikeOwnerInformationsBySolr();
		List<LikeDO> likesByHBase =  getCommunityUserData.getLikesByHBase();
		List<LikeDO> likesBySolr = getCommunityUserData.getLikesBySolr();
		List<LikeDO> relationReviewOwnerLikesByHBase =  getCommunityUserData.getRelationReviewOwnerLikesByHBase();
		List<LikeDO> relationReviewOwnerLikesBySolr = getCommunityUserData.getRelationReviewOwnerLikesBySolr();
		List<LikeDO> relationQuestionOwnerLikesByHBase =  getCommunityUserData.getRelationQuestionOwnerLikesByHBase();
		List<LikeDO> relationQuestionOwnerLikesBySolr = getCommunityUserData.getRelationQuestionOwnerLikesBySolr();
		List<LikeDO> relationQuestionAnswerOwnerLikesByHBase =  getCommunityUserData.getRelationQuestionAnswerOwnerLikesByHBase();
		List<LikeDO> relationQuestionAnswerOwnerLikesBySolr = getCommunityUserData.getRelationQuestionAnswerOwnerLikesBySolr();
		List<LikeDO> relationImageOwnerLikesByHBase =  getCommunityUserData.getRelationImageOwnerLikesByHBase();
		List<LikeDO> relationImageOwnerLikesBySolr = getCommunityUserData.getRelationImageOwnerLikesBySolr();
		List<ProductFollowDO> productFollowsByHBase = getCommunityUserData.getProductFollowsByHBase();
		List<ProductFollowDO> productFollowsBySolr = getCommunityUserData.getProductFollowsBySolr();
		List<PurchaseLostProductDO> purchaseLostProductsByHBase = getCommunityUserData.getPurchaseLostProductsByHBase();
		List<PurchaseLostProductDO> purchaseLostProductsBySolr = getCommunityUserData.getPurchaseLostProductsBySolr();
		List<PurchaseProductDO> purchaseProductsByHBase = getCommunityUserData.getPurchaseProductsByHBase();
		List<PurchaseProductDO> purchaseProductsBySolr = getCommunityUserData.getPurchaseProductsBySolr();
		List<QuestionFollowDO> questionFollowsByHBase = getCommunityUserData.getQuestionFollowsByHBase();
		List<QuestionFollowDO> questionFollowsBySolr = getCommunityUserData.getQuestionFollowsBySolr();
		List<QuestionFollowDO> relationQuestionOwnerQuestionFollowsByHBase = getCommunityUserData.getRelationQuestionOwnerQuestionFollowsByHBase();
		List<QuestionFollowDO> relationQuestionOwnerQuestionFollowsBySolr = getCommunityUserData.getRelationQuestionOwnerQuestionFollowsBySolr();
		List<ReviewDecisivePurchaseDO> reviewDecisivePurchasesByHBase = getCommunityUserData.getReviewDecisivePurchasesByHBase();
		List<ReviewDecisivePurchaseDO> reviewDecisivePurchasesBySolr = getCommunityUserData.getReviewDecisivePurchasesBySolr();
		List<UsedProductDO> usedProductsByHBase = getCommunityUserData.getUsedProductsByHBase();
		List<UsedProductDO> usedProductsBySolr = getCommunityUserData.getUsedProductsBySolr();
		List<SpamReportDO> spamReportsByHBase = getCommunityUserData.getSpamReportsByHBase();
		List<SpamReportDO> spamReportsBySolr = getCommunityUserData.getSpamReportsBySolr();
		List<AnnounceDO> announces = getCommunityUserData.getAnnounces();
		List<MailSettingDO> mailSettings = getCommunityUserData.getMailSettings();
		List<SocialMediaSettingDO> socialMediaSettings = getCommunityUserData.getSocialMediaSettings();
		Map<String, List<CommentDO>> commentMapByHBase  = getCommunityUserData.getCommentMapByHBase();
		Map<String, List<CommentDO>> commentMapBySolr = getCommunityUserData.getCommentMapBySolr();
		Map<String, List<ActionHistoryDO>> actionUserActionHistorisByHBase =
				getActionHistoryDivideCategory(actionUserWithdrawActionHistorysByHBase);
		Map<String, List<ActionHistoryDO>> actionUserActionHistorisBySolr =
				getActionHistoryDivideCategory(actionUserWithdrawActionHistorysBySolr);

		assertEquals(communityUserStatus, communityUser.getStatus());
		// レビュー関連の削除を検証します。
		if(reviewDelete || CommunityUserStatus.FORCE_LEAVE.equals(communityUser.getStatus())) {
			for(ReviewDO review : reviewsByHBase) {
				assertTrue(review.isWithdraw());
			}
			for(ReviewDO review : reviewsBySolr) {
				assertTrue(review.isWithdraw());
			}
			for(PurchaseLostProductDO purchaseLostProduct : purchaseLostProductsByHBase) {
				assertTrue(purchaseLostProduct.isWithdraw());
			}
			for(PurchaseLostProductDO purchaseLostProduct : purchaseLostProductsBySolr) {
				assertTrue(purchaseLostProduct.isWithdraw());
			}
			for(UsedProductDO usedProduct : usedProductsByHBase) {
				assertTrue(usedProduct.isWithdraw());
			}
			for(ReviewDecisivePurchaseDO reviewDecisivePurchase : reviewDecisivePurchasesByHBase) {
				assertTrue(reviewDecisivePurchase.isWithdraw());
			}
			for(ReviewDecisivePurchaseDO reviewDecisivePurchase : reviewDecisivePurchasesBySolr) {
				assertTrue(reviewDecisivePurchase.isWithdraw());
			}
			for(CommentDO comment : relationReviewOwnerCommentsByHBase) {
				assertTrue(comment.isWithdraw());
			}
			for(CommentDO comment : relationReviewOwnerCommentsBySolr) {
				assertTrue(comment.isWithdraw());
			}
			for(ActionHistoryDO actionHistory : actionUserActionHistorisByHBase.get("review")) {
				assertTrue(actionHistory.isWithdraw());
			}
			for(ActionHistoryDO actionHistory : actionUserActionHistorisBySolr.get("review")) {
				assertTrue(actionHistory.isWithdraw());
			}
			for(LikeDO like : relationReviewOwnerLikesByHBase) {
				assertTrue(like.isWithdraw());
			}
			for(LikeDO like : relationReviewOwnerLikesBySolr) {
				assertTrue(like.isWithdraw());
			}
		} else {
			for(ReviewDO review : reviewsByHBase) {
				assertTrue(!review.isWithdraw());
			}
			for(ReviewDO review : reviewsBySolr) {
				assertTrue(!review.isWithdraw());
			}
			for(PurchaseLostProductDO purchaseLostProduct : purchaseLostProductsByHBase) {
				assertTrue(!purchaseLostProduct.isWithdraw());
			}
			for(PurchaseLostProductDO purchaseLostProduct : purchaseLostProductsBySolr) {
				assertTrue(!purchaseLostProduct.isWithdraw());
			}
			for(UsedProductDO usedProduct : usedProductsByHBase) {
				assertTrue(!usedProduct.isWithdraw());
			}
			for(UsedProductDO usedProduct : usedProductsBySolr) {
				assertTrue(!usedProduct.isWithdraw());
			}
			for(ReviewDecisivePurchaseDO reviewDecisivePurchase : reviewDecisivePurchasesByHBase) {
				assertTrue(!reviewDecisivePurchase.isWithdraw());
			}
			for(ReviewDecisivePurchaseDO reviewDecisivePurchase : reviewDecisivePurchasesBySolr) {
				assertTrue(!reviewDecisivePurchase.isWithdraw());
			}
			for(CommentDO comment : relationReviewOwnerCommentsByHBase) {
				assertTrue(!comment.isWithdraw());
			}
			for(ActionHistoryDO actionHistory : actionUserActionHistorisByHBase.get("review")) {
				assertTrue(!actionHistory.isWithdraw());
			}
			for(LikeDO like : relationReviewOwnerLikesByHBase) {
				assertTrue(!like.isWithdraw());
			}
			assertEquals(relationReviewOwnerLikesByHBase.size(), relationReviewOwnerLikesBySolr.size());
			int reviewSize = sizeMap.get("review");
			assertEquals(reviewSize, reviewsByHBase.size());
			assertEquals(reviewSize, reviewsBySolr.size());
		}
		// 質問関連の削除を検証します。
		if(qaDelete || CommunityUserStatus.FORCE_LEAVE.equals(communityUser.getStatus())) {
			for(QuestionDO question : questionsByHBase) {
				assertTrue(question.isWithdraw());
			}
			for(QuestionDO question : questionsBySolr) {
				assertTrue(question.isWithdraw());
			}
			for(QuestionAnswerDO questionAnswer : questionAnswersByHBase) {
				assertTrue(questionAnswer.isWithdraw());
			}
			for(QuestionAnswerDO questionAnswer : questionAnswersBySolr) {
				assertTrue(questionAnswer.isWithdraw());
			}
			for(CommentDO comment : relationQuestionOwnerCommentsByHBase) {
				assertTrue(comment.isWithdraw());
			}
			for(CommentDO comment : relationQuestionOwnerCommentsBySolr) {
				assertTrue(comment.isWithdraw());
			}
			for(CommentDO comment : relationQuestionAnswerOwnerCommentsByHBase) {
				assertTrue(comment.isWithdraw());
			}
			for(CommentDO comment : relationQuestionAnswerOwnerCommentsBySolr) {
				assertTrue(comment.isWithdraw());
			}
			for(ActionHistoryDO actionHistory : actionUserActionHistorisByHBase.get("question")) {
				assertTrue(actionHistory.isWithdraw());
			}
			for(ActionHistoryDO actionHistory : actionUserActionHistorisBySolr.get("question")) {
				assertTrue(actionHistory.isWithdraw());
			}
			// 退会ユーザーの質問をフォロー
			for(QuestionFollowDO questionFollow : relationQuestionOwnerQuestionFollowsByHBase) {
				assertTrue(questionFollow.isWithdraw());
			}
			assertEquals(0, relationQuestionOwnerQuestionFollowsBySolr.size());
			// 退会ユーザーの質問に対するお知らせ
			for(InformationDO information : relationQuestionOwnerInformationsByHBase) {
				assertTrue(information.isWithdraw());
			}
			assertEquals(0, relationQuestionOwnerInformationsBySolr.size());
			for(InformationDO information : relationQuestionAnswerOwnerInformationsByHBase) {
				assertTrue(information.isWithdraw());
			}
			assertEquals(0, relationQuestionAnswerOwnerInformationsBySolr.size());
			for(InformationDO information : relationLikeOwnerInformationsBySolr) {
				assertTrue(!InformationType.QUESTION_ANSWER_LIKE_ADD.equals(information.getInformationType()));
			}
			for(LikeDO like : relationQuestionOwnerLikesByHBase) {
				assertTrue(like.isWithdraw());
			}
			assertEquals(0, relationQuestionOwnerLikesBySolr.size());
			for(LikeDO like : relationQuestionAnswerOwnerLikesByHBase) {
				assertTrue(like.isWithdraw());
			}
			assertEquals(0, relationQuestionAnswerOwnerLikesBySolr.size());
		} else {
			for(QuestionDO question : questionsByHBase) {
				assertTrue(!question.isWithdraw());
			}
			for(QuestionAnswerDO questionAnswer : questionAnswersByHBase) {
				assertTrue(!questionAnswer.isWithdraw());
			}
			for(CommentDO comment : relationQuestionOwnerCommentsByHBase) {
				assertTrue(!comment.isWithdraw());
			}
			for(CommentDO comment : relationQuestionAnswerOwnerCommentsByHBase) {
				assertTrue(!comment.isWithdraw());
			}
			for(ActionHistoryDO actionHistory : actionUserActionHistorisByHBase.get("question")) {
				assertTrue(!actionHistory.isWithdraw());
			}
			// 退会ユーザーの質問をフォロー
			for(QuestionFollowDO questionFollow : relationQuestionOwnerQuestionFollowsByHBase) {
				assertTrue(!questionFollow.isWithdraw());
			}
			// 退会ユーザーの質問に対するお知らせ
			for(InformationDO information : relationQuestionOwnerInformationsByHBase) {
				assertTrue(!information.isWithdraw());
			}
			for(InformationDO information : relationQuestionAnswerOwnerInformationsByHBase) {
				assertTrue(!information.isWithdraw());
			}
			for(InformationDO information : relationLikeOwnerInformationsBySolr) {
				if(InformationType.QUESTION_ANSWER_LIKE_ADD.equals(information.getInformationType())) {
					assertTrue(!information.isWithdraw());
				}
			}
			for(LikeDO like : relationQuestionOwnerLikesByHBase) {
				assertTrue(!like.isWithdraw());
			}
			assertEquals(relationQuestionOwnerLikesByHBase.size(), relationQuestionOwnerLikesBySolr.size());
			for(LikeDO like : relationQuestionAnswerOwnerLikesByHBase) {
				assertTrue(!like.isWithdraw());
			}
			assertEquals(relationQuestionAnswerOwnerLikesByHBase.size(), relationQuestionAnswerOwnerLikesBySolr.size());
			int questionSize = sizeMap.get("question");
			int questionAnswerSize = sizeMap.get("questionAnswer");
			assertEquals(questionSize, questionsByHBase.size());
			assertEquals(questionAnswerSize, questionAnswersByHBase.size());
			assertEquals(questionSize, questionsBySolr.size());
			assertEquals(questionAnswerSize, questionAnswersBySolr.size());
		}
		// 画像関係の削除を検証します。
		if(imageDelete || CommunityUserStatus.FORCE_LEAVE.equals(communityUser.getStatus())) {
			for(ImageHeaderDO imageHeader : imageHeaderByHBase) {
				assertTrue(imageHeader.isWithdraw());
			}
			for(ImageHeaderDO imageHeader : imageHeaderBySolr) {
				assertTrue(imageHeader.isWithdraw());
			}
			for(CommentDO comment : relationImageOwnerCommentsByHBase) {
				assertTrue(comment.isWithdraw());
			}
			for(CommentDO comment : relationImageOwnerCommentsBySolr) {
				assertTrue(comment.isWithdraw());
			}
			for(ActionHistoryDO actionHistory : actionUserActionHistorisByHBase.get("image")) {
				assertTrue(actionHistory.isWithdraw());
			}
			for(ActionHistoryDO actionHistory : actionUserActionHistorisBySolr.get("image")) {
				assertTrue(actionHistory.isWithdraw());
			}
			for(LikeDO like : relationImageOwnerLikesByHBase) {
				assertTrue(like.isWithdraw());
			}
			for(LikeDO like : relationImageOwnerLikesBySolr) {
				assertTrue(like.isWithdraw());
			}
		} else {
			for(ImageHeaderDO imageHeader : imageHeaderByHBase) {
				if(PostContentType.PROFILE.equals(imageHeader.getPostContentType()) ||
						PostContentType.PROFILE_THUMBNAIL.equals(imageHeader.getPostContentType())) {
					assertTrue(imageHeader.isWithdraw());
				} else {
					assertTrue(!imageHeader.isWithdraw());
				}
			}
			for(CommentDO comment : relationImageOwnerCommentsByHBase) {
				assertTrue(!comment.isWithdraw());
			}
			for(ActionHistoryDO actionHistory : actionUserActionHistorisByHBase.get("image")) {
				assertTrue(!actionHistory.isWithdraw());
			}
			for(LikeDO like : relationImageOwnerLikesByHBase) {
				assertTrue(!like.isWithdraw());
			}
			assertEquals(relationImageOwnerLikesByHBase.size(), relationImageOwnerLikesBySolr.size());
			int imageHeaderSize = sizeMap.get("imageHeader");
			assertEquals(imageHeaderSize, imageHeaderByHBase.size());
			assertEquals(imageHeaderSize, imageHeaderBySolr.size());
		}
		// コメント関係の削除を検証します。
		if(commentDelete || CommunityUserStatus.FORCE_LEAVE.equals(communityUser.getStatus())) {
			for(CommentDO comment : commentMapByHBase.get("review")) {
				assertTrue(comment.isWithdraw());
			}
			for(CommentDO comment : commentMapBySolr.get("review")) {
				assertTrue(comment.isWithdraw());
			}
			for(CommentDO comment : commentMapByHBase.get("question")) {
				assertTrue(comment.isWithdraw());
			}
			for(CommentDO comment : commentMapBySolr.get("question")) {
				assertTrue(comment.isWithdraw());
			}
			for(CommentDO comment : commentMapByHBase.get("image")) {
				assertTrue(comment.isWithdraw());
			}
			for(CommentDO comment : commentMapBySolr.get("image")) {
				assertTrue(comment.isWithdraw());
			}
			for(ActionHistoryDO actionHistory : actionUserActionHistorisByHBase.get("comment")) {
				assertTrue(actionHistory.isWithdraw());
			}
			for(InformationDO information : relationCommentOwnerInformationsByHBase) {
				assertTrue(information.isWithdraw());
			}
			assertEquals(0, relationCommentOwnerInformationsBySolr.size());
		} else {
			for(CommentDO comment : commentMapByHBase.get("review")) {
				assertTrue(!comment.isWithdraw());
			}
			for(CommentDO comment : commentMapByHBase.get("question")) {
				assertTrue(!comment.isWithdraw());
			}
			for(CommentDO comment : commentMapByHBase.get("image")) {
				assertTrue(!comment.isWithdraw());
			}
			for(ActionHistoryDO actionHistory : actionUserActionHistorisByHBase.get("comment")) {
				assertTrue(!actionHistory.isWithdraw());
			}
			for(InformationDO information : relationCommentOwnerInformationsByHBase) {
				assertTrue(!information.isWithdraw());
			}
		}
		// 退会ユーザーがアクションユーザーのアクションヒストリーを検証します。
		for(ActionHistoryDO actionHistory : actionUserActionHistorisByHBase.get("follow")) {
			assertTrue(actionHistory.isWithdraw());
		}
		// コミュニティユーザーがフォロー
		for(CommunityUserFollowDO communityUserFollow : communityUserFollowsByHBase) {
			assertTrue(communityUserFollow.isWithdraw());
		}
		assertEquals(0, communityUserFollowsBySolr.size());
		// コミュニティユーザーをフォロー
		for(CommunityUserFollowDO communityUserFollow : followCommunityUserFollowsByHBase) {
			assertTrue(communityUserFollow.isWithdraw());
		}
		assertEquals(0, followCommunityUserFollowsBySolr.size());
		// 商品をフォロー
		for(ProductFollowDO productFollow : productFollowsByHBase) {
			assertTrue(productFollow.isWithdraw());
		}
		assertEquals(0, productFollowsBySolr.size());
		// 退会ユーザーが質問をフォロー
		for(QuestionFollowDO questionFollow : questionFollowsByHBase) {
			assertTrue(questionFollow.isWithdraw());
		}
		assertEquals(0, questionFollowsBySolr.size());
		// 退会ユーザーが受け取ったお知らせを検証
		if(CommunityUserStatus.ACTIVE.equals(communityUserStatus)) {
			int count = 0;
			for(InformationDO information : informationsByHBase) {
				if(InformationType.WELCOME.equals(information.getInformationType())) {
					if(!information.isWithdraw()) {
						count++;
					}
				} else {
					assertTrue(information.isWithdraw());
				}
			}
			assertEquals(1, count);
			assertEquals(1, informationsBySolr.size());
		} else {
			for(InformationDO information : informationsByHBase) {
				assertTrue(information.isWithdraw());
			}
			assertEquals(0, informationsBySolr.size());
		}
		// 退会ユーザーが操作したいいね
		for(InformationDO information : relationLikeOwnerInformationsByHBase) {
			assertTrue(information.isWithdraw());
		}
		for(LikeDO like : likesByHBase) {
			assertTrue(like.isWithdraw());
		}
		assertEquals(0, likesBySolr.size());
		// 購入者情報
		for(PurchaseProductDO purchaseProduct : purchaseProductsByHBase) {
			if(CommunityUserStatus.FORCE_LEAVE.equals(communityUserStatus)) {
				assertTrue(purchaseProduct.isWithdraw());
			} else {
				assertTrue(!purchaseProduct.isWithdraw());
			}
		}
		for(PurchaseProductDO purchaseProduct : purchaseProductsBySolr) {
			if(CommunityUserStatus.FORCE_LEAVE.equals(communityUserStatus)) {
				assertTrue(purchaseProduct.isWithdraw());
			} else {
				assertTrue(!purchaseProduct.isWithdraw());
			}
		}
		// スパムレポート
		for(SpamReportDO spamReport : spamReportsByHBase) {
			assertTrue(spamReport.isWithdraw());
		}
		assertEquals(0, spamReportsBySolr.size());
		for(AnnounceDO announce : announces) {
			assertTrue(announce.isWithdraw());
		}
		for(MailSettingDO mailSetting : mailSettings) {
			assertTrue(mailSetting.isWithdraw());
		}
		for(SocialMediaSettingDO socialMediaSetting : socialMediaSettings) {
			assertTrue(socialMediaSetting.isWithdraw());
		}
	}

	/**
	 * solrの各件数を返します。
	 */
	private Map<String, Integer> getSizeMapByHBase(String communityUserId) {
		Map<String, Integer> sizeMap = new HashMap<String, Integer>();
		// レビューを取得します。
		List<ReviewDO> reviews = hBaseOperations
				.scanWithIndex(ReviewDO.class, "communityUserId", communityUserId,
						hBaseOperations.createFilterBuilder(ReviewDO.class).toFilter(), reviewPath);
		sizeMap.put("review", reviews.size());
		// 質問を取得します。
		List<QuestionDO> questions = hBaseOperations
				.scanWithIndex(QuestionDO.class, "communityUserId", communityUserId,
						hBaseOperations.createFilterBuilder(QuestionDO.class).toFilter(), questionPath);
		sizeMap.put("question", questions.size());
		// 質問回答を取得します。
		List<QuestionAnswerDO> questionAnswers = hBaseOperations
				.scanWithIndex(QuestionAnswerDO.class, "communityUserId", communityUserId,
						hBaseOperations.createFilterBuilder(QuestionAnswerDO.class).toFilter(), questionPath);
		sizeMap.put("questionAnswer", questionAnswers.size());
		// 画像を取得します。
		List<ImageHeaderDO> imageHeaders = hBaseOperations
				.scanWithIndex(ImageHeaderDO.class, "ownerCommunityUserId", communityUserId,
						hBaseOperations.createFilterBuilder(ImageHeaderDO.class).toFilter(), imageHeaderPath);
		sizeMap.put("imageHeader", imageHeaders.size());
		// コメントを取得します。
		List<CommentDO> comments = hBaseOperations
				.scanWithIndex(CommentDO.class, "communityUserId", communityUserId,
						hBaseOperations.createFilterBuilder(CommentDO.class).toFilter(), commentPath);
		sizeMap.put("comment", comments.size());

		return sizeMap;
	}

	/**
	 * 退会キャンセルを実行・確認します。
	 */
	private void testCancelWithdraw(String communityUserId) {
		userService.cancelWithdraw(communityUserId);
		checkCommunityUser(communityUserId, CommunityUserStatus.ACTIVE);
	}

	private void checkCommunityUser(String communityUserId, CommunityUserStatus communityUserStatus) {
		// コミュニティユーザーを取得します。
		CommunityUserDO communityUserByHBase = getCommunityUserByHbase(communityUserId);
		CommunityUserDO communityUserBySolr = getCommunityUserBySolr(communityUserId);
		checkCommunityUser(communityUserByHBase);
		checkCommunityUser(communityUserBySolr);
	}

	/**
	 * 退会キャンセルを確認します。
	 * @param communityUser
	 */
	private void checkCommunityUser(CommunityUserDO communityUser) {
		GetCommunityUserData getCommunityUserData = new GetCommunityUserData(communityUser.getCommunityUserId());
		List<ReviewDO> reviewsByHBase = getCommunityUserData.getReviewsByHBase();
		List<ReviewDO> reviewsBySolr = getCommunityUserData.getReviewsBySolr();
		List<QuestionDO> questionsByHBase = getCommunityUserData.getQuestionsByHBase();
		List<QuestionDO> questionsBySolr = getCommunityUserData.getQuestionsBySolr();
		List<QuestionAnswerDO> questionAnswersByHBase = getCommunityUserData.getQuestionAnswersByHBase();
		List<QuestionAnswerDO> questionAnswersBySolr = getCommunityUserData.getQuestionAnswersBySolr();
		List<ImageHeaderDO> imageHeaderByHBase = getCommunityUserData.getImageHeaderByHBase();
		List<ImageHeaderDO> imageHeaderBySolr = getCommunityUserData.getImageHeaderBySolr();
		List<CommentDO> relationReviewOwnerCommentsByHBase = getCommunityUserData.getRelationReviewOwnerCommentsByHBase();
		List<CommentDO> relationReviewOwnerCommentsBySolr = getCommunityUserData.getRelationReviewOwnerCommentsBySolr();
		List<CommentDO> relationQuestionOwnerCommentsByHBase = getCommunityUserData.getRelationQuestionOwnerCommentsByHBase();
		List<CommentDO> relationQuestionOwnerCommentsBySolr = getCommunityUserData.getRelationQuestionOwnerCommentsBySolr();
		List<CommentDO> relationQuestionAnswerOwnerCommentsByHBase = getCommunityUserData.getRelationQuestionAnswerOwnerCommentsByHBase();
		List<CommentDO> relationQuestionAnswerOwnerCommentsBySolr = getCommunityUserData.getRelationQuestionAnswerOwnerCommentsBySolr();
		List<CommentDO> relationImageOwnerCommentsByHBase = getCommunityUserData.getRelationImageOwnerCommentsByHBase();
		List<CommentDO> relationImageOwnerCommentsBySolr = getCommunityUserData.getRelationImageOwnerCommentsBySolr();
		List<ActionHistoryDO> actionHistorisByHBase = getCommunityUserData.getActionHistorisByHBase();
		List<ActionHistoryDO> actionHistorisBySolr = getCommunityUserData.getActionHistorisBySolr();
		List<ActionHistoryDO> actionUserWithdrawActionHistorysByHBase = getCommunityUserData.getActionUserWithdrawActionHistorysByHBase();
		List<ActionHistoryDO> actionUserWithdrawActionHistorysBySolr = getCommunityUserData.getActionUserWithdrawActionHistorysBySolr();
		List<CommunityUserFollowDO> communityUserFollowsByHBase = getCommunityUserData.getCommunityUserFollowsByHBase();
		List<CommunityUserFollowDO> communityUserFollowsBySolr = getCommunityUserData.getCommunityUserFollowsBySolr();
		List<CommunityUserFollowDO> followCommunityUserFollowsByHBase = getCommunityUserData.getFollowCommunityUserFollowsByHBase();
		List<CommunityUserFollowDO> followCommunityUserFollowsBySolr = getCommunityUserData.getFollowCommunityUserFollowsBySolr();
		List<InformationDO> informationsByHBase = getCommunityUserData.getInformationsByHBase();
		List<InformationDO> informationsBySolr = getCommunityUserData.getInformationsBySolr();
		List<InformationDO> relationQuestionOwnerInformationsByHBase = getCommunityUserData.getRelationQuestionOwnerInformationsByHBase();
		List<InformationDO> relationQuestionOwnerInformationsBySolr = getCommunityUserData.getRelationQuestionOwnerInformationsBySolr();
		List<InformationDO> relationQuestionAnswerOwnerInformationsByHBase = getCommunityUserData.getRelationQuestionAnswerOwnerInformationsByHBase();
		List<InformationDO> relationQuestionAnswerOwnerInformationsBySolr = getCommunityUserData.getRelationQuestionAnswerOwnerInformationsBySolr();
		List<InformationDO> relationCommentOwnerInformationsByHBase = getCommunityUserData.getRelationCommentOwnerInformationsByHBase();
		List<InformationDO> relationCommentOwnerInformationsBySolr = getCommunityUserData.getRelationCommentOwnerInformationsBySolr();
		List<InformationDO> relationLikeOwnerInformationsByHBase = getCommunityUserData.getRelationLikeOwnerInformationsByHBase();
		List<InformationDO> relationLikeOwnerInformationsBySolr = getCommunityUserData.getRelationLikeOwnerInformationsBySolr();
		List<LikeDO> likesByHBase =  getCommunityUserData.getLikesByHBase();
		List<LikeDO> likesBySolr = getCommunityUserData.getLikesBySolr();
		List<ProductFollowDO> productFollowsByHBase = getCommunityUserData.getProductFollowsByHBase();
		List<ProductFollowDO> productFollowsBySolr = getCommunityUserData.getProductFollowsBySolr();
		List<PurchaseLostProductDO> purchaseLostProductsByHBase = getCommunityUserData.getPurchaseLostProductsByHBase();
		List<PurchaseLostProductDO> purchaseLostProductsBySolr = getCommunityUserData.getPurchaseLostProductsBySolr();
		List<PurchaseProductDO> purchaseProductsByHBase = getCommunityUserData.getPurchaseProductsByHBase();
		List<PurchaseProductDO> purchaseProductsBySolr = getCommunityUserData.getPurchaseProductsBySolr();
		List<QuestionFollowDO> questionFollowsByHBase = getCommunityUserData.getQuestionFollowsByHBase();
		List<QuestionFollowDO> questionFollowsBySolr = getCommunityUserData.getQuestionFollowsBySolr();
		List<QuestionFollowDO> relationQuestionOwnerQuestionFollowsByHBase = getCommunityUserData.getRelationQuestionOwnerQuestionFollowsByHBase();
		List<QuestionFollowDO> relationQuestionOwnerQuestionFollowsBySolr = getCommunityUserData.getRelationQuestionOwnerQuestionFollowsBySolr();
		List<ReviewDecisivePurchaseDO> reviewDecisivePurchasesByHBase = getCommunityUserData.getReviewDecisivePurchasesByHBase();
		List<ReviewDecisivePurchaseDO> reviewDecisivePurchasesBySolr = getCommunityUserData.getReviewDecisivePurchasesBySolr();
		List<UsedProductDO> usedProductsByHBase = getCommunityUserData.getUsedProductsByHBase();
		List<UsedProductDO> usedProductsBySolr = getCommunityUserData.getUsedProductsBySolr();
		List<SpamReportDO> spamReportsByHBase = getCommunityUserData.getSpamReportsByHBase();
		List<SpamReportDO> spamReportsBySolr = getCommunityUserData.getSpamReportsBySolr();
		List<AnnounceDO> announces = getCommunityUserData.getAnnounces();
		List<MailSettingDO> mailSettings = getCommunityUserData.getMailSettings();
		List<SocialMediaSettingDO> socialMediaSettings = getCommunityUserData.getSocialMediaSettings();
		Map<String, List<CommentDO>> commentMapByHBase  = getCommunityUserData.getCommentMapByHBase();
		Map<String, List<CommentDO>> commentMapBySolr = getCommunityUserData.getCommentMapBySolr();
		Map<String, List<ActionHistoryDO>> actionUserActionHistorisByHBase =
				getActionHistoryDivideCategory(actionUserWithdrawActionHistorysByHBase);
		Map<String, List<ActionHistoryDO>> actionUserActionHistorisBySolr =
				getActionHistoryDivideCategory(actionUserWithdrawActionHistorysBySolr);
		for(ReviewDO review : reviewsByHBase) {
			assertTrue(!review.isDeleted());
			assertEquals(null, review.getDeleteDate());
		}
		assertEquals(reviewsByHBase.size(), reviewsBySolr.size());
		for(QuestionDO question : questionsByHBase) {
			assertTrue(!question.isDeleted());
			assertEquals(null, question.getDeleteDate());
		}
		assertEquals(questionsByHBase.size(), questionsBySolr.size());
		for(QuestionAnswerDO questionAnswer : questionAnswersByHBase) {
			assertTrue(!questionAnswer.isDeleted());
			assertEquals(null, questionAnswer.getDeleteDate());
		}
		assertEquals(questionAnswersByHBase.size(), questionAnswersBySolr.size());
		for(ImageHeaderDO imageHeader : imageHeaderByHBase) {
			assertTrue(!imageHeader.isDeleted());
			assertEquals(null, imageHeader.getDeleteDate());
		}
		assertEquals(imageHeaderByHBase.size(), imageHeaderBySolr.size());
		for(CommentDO comment : relationReviewOwnerCommentsByHBase) {
			assertTrue(!comment.isDeleted());
			assertEquals(null, comment.getDeleteDate());
		}
		assertEquals(relationReviewOwnerCommentsByHBase.size(), relationReviewOwnerCommentsBySolr.size());
		for(CommentDO comment : relationQuestionOwnerCommentsByHBase) {
			assertTrue(!comment.isDeleted());
			assertEquals(null, comment.getDeleteDate());
		}
		assertEquals(relationQuestionOwnerCommentsByHBase.size(), relationQuestionOwnerCommentsBySolr.size());
		for(CommentDO comment : relationQuestionAnswerOwnerCommentsByHBase) {
			assertTrue(!comment.isDeleted());
			assertEquals(null, comment.getDeleteDate());
		}
		assertEquals(relationQuestionAnswerOwnerCommentsByHBase.size(), relationQuestionAnswerOwnerCommentsBySolr.size());
		for(CommentDO comment : relationImageOwnerCommentsByHBase) {
			assertTrue(!comment.isDeleted());
			assertEquals(null, comment.getDeleteDate());
		}
		assertEquals(relationImageOwnerCommentsByHBase.size(), relationImageOwnerCommentsBySolr.size());
		for(ActionHistoryDO actionHistory : actionHistorisByHBase) {
			assertTrue(!actionHistory.isDeleted());
			assertEquals(null, actionHistory.getDeleteDate());
		}
		assertEquals(actionHistorisByHBase.size(), actionHistorisBySolr.size());
		for(ActionHistoryDO actionHistory : actionUserWithdrawActionHistorysByHBase) {
			assertTrue(!actionHistory.isDeleted());
			assertEquals(null, actionHistory.getDeleteDate());
		}
		assertEquals(actionUserWithdrawActionHistorysByHBase.size(), actionUserWithdrawActionHistorysBySolr.size());
		for(CommunityUserFollowDO communityUserFollow : communityUserFollowsByHBase) {
			assertTrue(!communityUserFollow.isDeleted());
		}
		assertEquals(communityUserFollowsByHBase.size(), communityUserFollowsBySolr.size());
		for(CommunityUserFollowDO communityUserFollow : followCommunityUserFollowsByHBase) {
			assertTrue(!communityUserFollow.isDeleted());
		}
		assertEquals(followCommunityUserFollowsByHBase.size(), followCommunityUserFollowsBySolr.size());
		for(InformationDO information : informationsByHBase) {
			assertTrue(!information.isDeleted());
		}
		assertEquals(informationsByHBase.size(), informationsBySolr.size());
		for(InformationDO information : relationQuestionOwnerInformationsByHBase) {
			assertTrue(!information.isDeleted());
		}
		assertEquals(relationQuestionOwnerInformationsByHBase.size(), relationQuestionOwnerInformationsBySolr.size());
		for(InformationDO information : relationQuestionAnswerOwnerInformationsByHBase) {
			assertTrue(!information.isDeleted());
		}
		assertEquals(relationQuestionAnswerOwnerInformationsByHBase.size(), relationQuestionAnswerOwnerInformationsBySolr.size());
		for(InformationDO information : informationsByHBase) {
			assertTrue(!information.isDeleted());
		}
		assertEquals(informationsByHBase.size(), informationsBySolr.size());
		for(InformationDO information : relationCommentOwnerInformationsByHBase) {
			assertTrue(!information.isDeleted());
		}
		assertEquals(relationCommentOwnerInformationsByHBase.size(), relationCommentOwnerInformationsBySolr.size());
		for(InformationDO information : relationLikeOwnerInformationsByHBase) {
			assertTrue(!information.isDeleted());
		}
		assertEquals(relationLikeOwnerInformationsByHBase.size(), relationLikeOwnerInformationsBySolr.size());
		for(LikeDO like : likesByHBase) {
			assertTrue(!like.isDeleted());
		}
		assertEquals(likesByHBase.size(), likesBySolr.size());
		for(ProductFollowDO productFollow : productFollowsByHBase) {
			assertTrue(!productFollow.isDeleted());
		}
		assertEquals(productFollowsByHBase.size(), productFollowsBySolr.size());
		for(PurchaseLostProductDO purchaseLostProduct : purchaseLostProductsByHBase) {
			assertTrue(!purchaseLostProduct.isDeleted());
		}
		assertEquals(purchaseLostProductsByHBase.size(), purchaseLostProductsBySolr.size());
		for(PurchaseProductDO purchaseProduct : purchaseProductsByHBase) {
			assertTrue(!purchaseProduct.isDeleted());
		}
		assertEquals(purchaseProductsByHBase.size(), purchaseProductsBySolr.size());
		for(QuestionFollowDO questionFollow : questionFollowsByHBase) {
			assertTrue(!questionFollow.isDeleted());
		}
		assertEquals(questionFollowsByHBase.size(), questionFollowsBySolr.size());
		for(QuestionFollowDO questionFollow : relationQuestionOwnerQuestionFollowsByHBase) {
			assertTrue(!questionFollow.isDeleted());
		}
		assertEquals(relationQuestionOwnerQuestionFollowsByHBase.size(), relationQuestionOwnerQuestionFollowsBySolr.size());
		for(ReviewDecisivePurchaseDO reviewDecisivePurchase : reviewDecisivePurchasesByHBase) {
			assertTrue(!reviewDecisivePurchase.isDeleted());
		}
		assertEquals(reviewDecisivePurchasesByHBase.size(), reviewDecisivePurchasesBySolr.size());
		for(UsedProductDO usedProduct : usedProductsByHBase) {
			assertTrue(!usedProduct.isDeleted());
		}
		assertEquals(usedProductsByHBase.size(), usedProductsBySolr.size());
		for(SpamReportDO spamReport : spamReportsByHBase) {
			assertTrue(!spamReport.isDeleted());
		}
		assertEquals(spamReportsByHBase.size(), spamReportsBySolr.size());
		for(AnnounceDO announce : announces) {
			assertTrue(!announce.isDeleted());
		}
		for(MailSettingDO mailSetting : mailSettings) {
			assertTrue(!mailSetting.isDeleted());
		}
		for(SocialMediaSettingDO socialMediaSetting : socialMediaSettings) {
			assertTrue(!socialMediaSetting.isDeleted());
		}
		for(CommentDO comment : commentMapByHBase.get("review")) {
			assertTrue(!comment.isDeleted());
		}
		assertEquals(commentMapByHBase.get("review").size(), commentMapBySolr.get("review").size());
		for(CommentDO comment : commentMapByHBase.get("question")) {
			assertTrue(!comment.isDeleted());
		}
		assertEquals(commentMapByHBase.get("question").size(), commentMapBySolr.get("question").size());
		for(CommentDO comment : commentMapByHBase.get("image")) {
			assertTrue(!comment.isDeleted());
		}
		assertEquals(commentMapByHBase.get("image").size(), commentMapBySolr.get("image").size());
		for(ActionHistoryDO actionHistory : actionUserActionHistorisByHBase.get("review")) {
			assertTrue(!actionHistory.isDeleted());
		}
		assertEquals(actionUserActionHistorisByHBase.get("review").size(),
				actionUserActionHistorisBySolr.get("review").size());
		for(ActionHistoryDO actionHistory : actionUserActionHistorisByHBase.get("question")) {
			assertTrue(!actionHistory.isDeleted());
		}
		assertEquals(actionUserActionHistorisByHBase.get("question").size(), actionUserActionHistorisBySolr.get("question").size());
		for(ActionHistoryDO actionHistory : actionUserActionHistorisByHBase.get("image")) {
			assertTrue(!actionHistory.isDeleted());
		}
		assertEquals(actionUserActionHistorisByHBase.get("image").size(), actionUserActionHistorisBySolr.get("image").size());
		for(ActionHistoryDO actionHistory : actionUserActionHistorisByHBase.get("follow")) {
			assertTrue(!actionHistory.isDeleted());
		}
		assertEquals(actionUserActionHistorisByHBase.get("follow").size(), actionUserActionHistorisBySolr.get("follow").size());
	}

	/**
	 * コミュニティユーザーを再登録・検証します。
	 */
	private void testReCreateCommunityUser(
			String communityUserId,
			String communityName,
			boolean withImage,
			boolean reviewDelete,
			boolean qaDelete,
			boolean imageDelete,
			boolean commentDelete
			) {
		CommunityUserDO communityUser = new CommunityUserDO();
		communityUser.setCommunityUserId(communityUserId);
		communityUser.setCommunityName(communityName);
		if(withImage) {
			image = new ImageDO();
			image.setData(testImageData);
			image.setMimeType("images/jpeg");
			image.setTemporaryKey("test");
			image.setWidth(400);
			image.setHeigth(500);
			ImageDO saveImage = imageService.createTemporaryImage(image);

			ImageDO thumbnailImage = new ImageDO();
			thumbnailImage.setData(testImageData);
			thumbnailImage.setMimeType("images/jpeg");
			thumbnailImage.setTemporaryKey("test");
			ImageDO saveThumbnailImage = imageService.createTemporaryImage(thumbnailImage);

			ImageHeaderDO imageHeader = new ImageHeaderDO();
			imageHeader.setImageId(saveImage.getImageId());
			communityUser.setImageHeader(imageHeader);

			ImageHeaderDO thumbnailImageHeader = new ImageHeaderDO();
			thumbnailImageHeader.setImageId(saveThumbnailImage.getImageId());
			communityUser.setThumbnail(thumbnailImageHeader);
		}
		userService.reCreateCommunityUser(communityUser, null, true);
		Map<String, Integer> sizeMap = getSizeMapByHBase(communityUserId);
		CommunityUserDO communityUserByHBase = getCommunityUserByHbase(communityUserId);
		CommunityUserDO communityUserBySolr = getCommunityUserBySolr(communityUserId);
		checkWithdrawCommunityUser(communityUserByHBase, CommunityUserStatus.ACTIVE, reviewDelete, qaDelete, imageDelete, commentDelete, sizeMap);
		checkWithdrawCommunityUser(communityUserBySolr, CommunityUserStatus.ACTIVE, reviewDelete, qaDelete, imageDelete, commentDelete, sizeMap);
		checkReCreateCommunityUser(
				communityUser,
				communityUserByHBase,
				withImage
				);
		checkReCreateCommunityUser(
				communityUser,
				communityUserBySolr,
				withImage
				);
	}

	/**
	 * コミュニティユーザー再登録を検証します。
	 * @param communityUser
	 * @param saveCommunityUser
	 */
	private void checkReCreateCommunityUser(
			CommunityUserDO communityUser,
			CommunityUserDO saveCommunityUser,
			boolean withImage
			) {
		assertEquals(communityUser.getCommunityUserId(), saveCommunityUser.getCommunityUserId());
		assertEquals(communityUser.getCommunityName(), saveCommunityUser.getCommunityName());
		if(withImage) {
			assertEquals(image.getImageId(), saveCommunityUser.getImageHeader().getImageId());
		} else {
			assertEquals(null, saveCommunityUser.getImageHeader());
		}
	}

	/**
	 * アクションヒストリーを種類別に分類して返します。
	 * @param actionHistoris
	 * @return
	 */
	private Map<String, List<ActionHistoryDO>> getActionHistoryDivideCategory(List<ActionHistoryDO> actionHistoris) {
		List<ActionHistoryDO> reviewActionHistory = new ArrayList<ActionHistoryDO>();
		List<ActionHistoryDO> questionActionHistory = new ArrayList<ActionHistoryDO>();
		List<ActionHistoryDO> imageHistory = new ArrayList<ActionHistoryDO>();
		List<ActionHistoryDO> followHistory = new ArrayList<ActionHistoryDO>();
		List<ActionHistoryDO> commentHistory = new ArrayList<ActionHistoryDO>();
		Map<String, List<ActionHistoryDO>> map = new HashMap<String, List<ActionHistoryDO>>();
		for(ActionHistoryDO actionHistory : actionHistoris) {
			if(ActionHistoryType.USER_REVIEW == actionHistory.getActionHistoryType() ||
					ActionHistoryType.PRODUCT_REVIEW == actionHistory.getActionHistoryType() ||
					ActionHistoryType.LIKE_REVIEW_50 == actionHistory.getActionHistoryType()
					) {
				// レビュー関連
				reviewActionHistory.add(actionHistory);
			} else if(ActionHistoryType.USER_QUESTION == actionHistory.getActionHistoryType() ||
					ActionHistoryType.USER_ANSWER == actionHistory.getActionHistoryType() ||
					ActionHistoryType.PRODUCT_QUESTION == actionHistory.getActionHistoryType() ||
					ActionHistoryType.PRODUCT_ANSWER == actionHistory.getActionHistoryType() ||
					ActionHistoryType.QUESTION_ANSWER == actionHistory.getActionHistoryType() ||
					ActionHistoryType.LIKE_ANSWER_50 == actionHistory.getActionHistoryType()
					) {
				// 質問関連
				questionActionHistory.add(actionHistory);
			} else if(ActionHistoryType.USER_IMAGE == actionHistory.getActionHistoryType() ||
					ActionHistoryType.LIKE_IMAGE_50 == actionHistory.getActionHistoryType()
					) {
				// 画像関連
				imageHistory.add(actionHistory);
			} else if(ActionHistoryType.USER_FOLLOW_USER == actionHistory.getActionHistoryType() ||
					ActionHistoryType.USER_FOLLOW_PRODUCT == actionHistory.getActionHistoryType() ||
					ActionHistoryType.USER_FOLLOW_QUESTION == actionHistory.getActionHistoryType()
					) {
				// フォロー関連
				followHistory.add(actionHistory);
			} else if(ActionHistoryType.USER_REVIEW_COMMENT == actionHistory.getActionHistoryType() ||
					ActionHistoryType.USER_ANSWER_COMMENT == actionHistory.getActionHistoryType() ||
					ActionHistoryType.USER_IMAGE_COMMENT == actionHistory.getActionHistoryType()
					) {
				// コメント関連
				commentHistory.add(actionHistory);
			}
		}
		map.put("review", reviewActionHistory);
		map.put("question", questionActionHistory);
		map.put("image", imageHistory);
		map.put("follow", followHistory);
		map.put("comment", commentHistory);
		return map;
	}

	/**
	 * コメントを種類別に分類して返します。
	 * @param comments
	 * @return
	 */
	private Map<String, List<CommentDO>> getCommentDivideCategory(List<CommentDO> comments) {
		List<CommentDO> reviewComment = new ArrayList<CommentDO>();
		List<CommentDO> questionComment = new ArrayList<CommentDO>();
		List<CommentDO> imageComment = new ArrayList<CommentDO>();
		Map<String, List<CommentDO>> map = new HashMap<String, List<CommentDO>>();
		for(CommentDO comment : comments) {
			if(CommentTargetType.REVIEW.equals(comment.getTargetType())) {
				reviewComment.add(comment);
			} else if(CommentTargetType.QUESTION_ANSWER.equals(comment.getTargetType())) {
				questionComment.add(comment);
			} else if(CommentTargetType.IMAGE.equals(comment.getTargetType())) {
				imageComment.add(comment);
			}
		}
		map.put("review", reviewComment);
		map.put("question", questionComment);
		map.put("image", imageComment);
		return map;
	}

	/**
	 * @author hirabayashi
	 *
	 */
	public class GetCommunityUserData {
		GetCommunityUserData(String CommunityUserId) {
			Condition path = Path.includeProp("*")
					.includePath("communityUser.communityUserId_s").depth(1);
			// レビューを取得します。
			List<ReviewDO> reviewsByHBase = hBaseOperations
					.scanWithIndex(ReviewDO.class, "communityUserId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(ReviewDO.class).toFilter(), reviewPath);
			StringBuilder buffer = new StringBuilder();
			buffer.append("communityUserId_s:");
			buffer.append(communityUser.getCommunityUserId());
			SolrQuery query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<ReviewDO> reviewsBySolr = new SearchResult<ReviewDO>(
					solrOperations.findByQuery(query, ReviewDO.class, reviewPath));
			// 質問を取得します。
			List<QuestionDO> questionsByHBase = hBaseOperations
					.scanWithIndex(QuestionDO.class, "communityUserId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(QuestionDO.class).toFilter(), questionPath);
			buffer = new StringBuilder();
			buffer.append("communityUserId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<QuestionDO> questionsBySolr = new SearchResult<QuestionDO>(
					solrOperations.findByQuery(query, QuestionDO.class, questionPath));
			// 質問回答を取得します。
			List<QuestionAnswerDO> questionAnswersByHBase = hBaseOperations
					.scanWithIndex(QuestionAnswerDO.class, "communityUserId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(QuestionAnswerDO.class).toFilter(), questionPath);
			buffer = new StringBuilder();
			buffer.append("communityUserId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<QuestionAnswerDO> questionAnswersBySolr = new SearchResult<QuestionAnswerDO>(
					solrOperations.findByQuery(query, QuestionAnswerDO.class, questionPath));
			// 画像を取得します。
			List<ImageHeaderDO> imageHeaderByHBase = hBaseOperations
					.scanWithIndex(ImageHeaderDO.class, "ownerCommunityUserId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(ImageHeaderDO.class).toFilter(), imageHeaderPath);
			buffer = new StringBuilder();
			buffer.append("ownerCommunityUserId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<ImageHeaderDO> imageHeaderBySolr = new SearchResult<ImageHeaderDO>(
					solrOperations.findByQuery(query, ImageHeaderDO.class, imageHeaderPath));
			// 退会ユーザーが書き込んだコメントを取得します。
			List<CommentDO> commentsByHBase = hBaseOperations
					.scanWithIndex(CommentDO.class, "communityUserId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(CommentDO.class).toFilter(), commentPath);
			buffer = new StringBuilder();
			buffer.append("communityUserId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<CommentDO> commentsBySolr = new SearchResult<CommentDO>(
					solrOperations.findByQuery(query, CommentDO.class, commentPath));
			Map<String, List<CommentDO>> commentMapByHBase = getCommentDivideCategory(commentsByHBase);
			Map<String, List<CommentDO>> commentMapBySolr = getCommentDivideCategory(commentsBySolr.getDocuments());
			// 退会ユーザーの投稿したレビューに紐づくコメントを取得します。
			List<CommentDO> relationReviewOwnerCommentsByHBase = hBaseOperations
					.scanWithIndex(CommentDO.class, "relationReviewOwnerId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(CommentDO.class).toFilter(), commentPath);
			buffer = new StringBuilder();
			buffer.append("relationReviewOwnerId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<CommentDO> relationReviewOwnerCommentsBySolr = new SearchResult<CommentDO>(
					solrOperations.findByQuery(query, CommentDO.class, commentPath));
			// 退会ユーザーの投稿した質問に紐づくコメントを取得します。
			List<CommentDO> relationQuestionOwnerCommentsByHBase = hBaseOperations
					.scanWithIndex(CommentDO.class, "relationQuestionOwnerId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(CommentDO.class).toFilter(), commentPath);
			buffer = new StringBuilder();
			buffer.append("relationQuestionOwnerId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<CommentDO> relationQuestionOwnerCommentsBySolr = new SearchResult<CommentDO>(
					solrOperations.findByQuery(query, CommentDO.class, commentPath));
			// 退会ユーザーの投稿した質問回答に紐づくコメントを取得します。
			List<CommentDO> relationQuestionAnswerOwnerCommentsByHBase = hBaseOperations
					.scanWithIndex(CommentDO.class, "relationQuestionAnswerOwnerId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(CommentDO.class).toFilter(), commentPath);
			buffer = new StringBuilder();
			buffer.append("relationQuestionAnswerOwnerId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<CommentDO> relationQuestionAnswerOwnerCommentsBySolr = new SearchResult<CommentDO>(
					solrOperations.findByQuery(query, CommentDO.class, commentPath));
			// 退会ユーザーの投稿した画像に紐づくコメントを取得します。
			List<CommentDO> relationImageOwnerCommentsByHBase = hBaseOperations
					.scanWithIndex(CommentDO.class, "relationImageOwnerId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(CommentDO.class).toFilter(), commentPath);
			buffer = new StringBuilder();
			buffer.append("relationImageOwnerId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<CommentDO> relationImageOwnerCommentsBySolr = new SearchResult<CommentDO>(
					solrOperations.findByQuery(query, CommentDO.class, commentPath));
			// 退会ユーザーがオーナーのアクションヒストリーを取得します。
			List<ActionHistoryDO> actionHistorisByHBase = hBaseOperations
					.scanWithIndex(ActionHistoryDO.class, "communityUserId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(ActionHistoryDO.class).toFilter(), actionHistoryPath);
			buffer = new StringBuilder();
			buffer.append("communityUserId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<ActionHistoryDO> actionHistorisBySolr = new SearchResult<ActionHistoryDO>(
					solrOperations.findByQuery(query, ActionHistoryDO.class, actionHistoryPath));
			// 退会ユーザーに関係するアクションヒストリーを取得します。
			List<ActionHistoryDO> actionUserWithdrawActionHistorysByHBase = hBaseOperations
					.scanWithIndex(ActionHistoryDO.class, "communityUserId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(ActionHistoryDO.class).toFilter(), actionHistoryPath);
			buffer = new StringBuilder();
			buffer.append("communityUserId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<ActionHistoryDO> actionUserWithdrawActionHistorysBySolr = new SearchResult<ActionHistoryDO>(
					solrOperations.findByQuery(query, ActionHistoryDO.class, actionHistoryPath));
			// 退会ユーザーがフォローしているユーザーを取得します。 CommunityUserFollowDO
			List<CommunityUserFollowDO> communityUserFollowsByHBase = hBaseOperations
					.scanWithIndex(CommunityUserFollowDO.class, "communityUserId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(CommunityUserFollowDO.class).toFilter(), path);
			buffer = new StringBuilder();
			buffer.append("communityUserId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<CommunityUserFollowDO> communityUserFollowsBySolr = new SearchResult<CommunityUserFollowDO>(
					solrOperations.findByQuery(query, CommunityUserFollowDO.class, path));
			// 退会ユーザーがフォローされている情報を取得します。
			List<CommunityUserFollowDO> followCommunityUserFollowsByHBase = hBaseOperations
					.scanWithIndex(CommunityUserFollowDO.class, "followCommunityUserId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(CommunityUserFollowDO.class).toFilter(), path);
			buffer = new StringBuilder();
			buffer.append("followCommunityUserId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<CommunityUserFollowDO> followCommunityUserFollowsBySolr = new SearchResult<CommunityUserFollowDO>(
					solrOperations.findByQuery(query, CommunityUserFollowDO.class, path));
			// 退会ユーザーが受け取ったお知らせを取得します。 InformationDO
			List<InformationDO> informationsByHBase = hBaseOperations
					.scanWithIndex(InformationDO.class, "communityUserId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(InformationDO.class).toFilter(), informationPath);
			buffer = new StringBuilder();
			buffer.append("communityUserId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<InformationDO> informationsBySolr = new SearchResult<InformationDO>(
					solrOperations.findByQuery(query, InformationDO.class, informationPath));
			// 退会ユーザーの投稿した質問に紐づくお知らせを取得します。
			List<InformationDO> relationQuestionOwnerInformationsByHBase = hBaseOperations
					.scanWithIndex(InformationDO.class, "relationQuestionOwnerId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(InformationDO.class).toFilter(), informationPath);
			buffer = new StringBuilder();
			buffer.append("relationQuestionOwnerId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<InformationDO> relationQuestionOwnerInformationsBySolr = new SearchResult<InformationDO>(
					solrOperations.findByQuery(query, InformationDO.class, informationPath));
			// 退会ユーザーの投稿した質問回答に紐づくお知らせを取得します。
			List<InformationDO> relationQuestionAnswerOwnerInformationsByHBase = hBaseOperations
					.scanWithIndex(InformationDO.class, "relationQuestionAnswerOwnerId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(InformationDO.class).toFilter(), informationPath);
			buffer = new StringBuilder();
			buffer.append("relationQuestionAnswerOwnerId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<InformationDO> relationQuestionAnswerOwnerInformationsBySolr = new SearchResult<InformationDO>(
					solrOperations.findByQuery(query, InformationDO.class, informationPath));
			// 退会ユーザーの投稿したコメントに紐づくお知らせを取得します。
			List<InformationDO> relationCommentOwnerInformationsByHBase = hBaseOperations
					.scanWithIndex(InformationDO.class, "relationCommentOwnerId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(InformationDO.class).toFilter(), informationPath);
			buffer = new StringBuilder();
			buffer.append("relationCommentOwnerId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<InformationDO> relationCommentOwnerInformationsBySolr = new SearchResult<InformationDO>(
					solrOperations.findByQuery(query, InformationDO.class, informationPath));
			// 退会ユーザーの投稿したいいねに紐づくお知らせを取得します。
			List<InformationDO> relationLikeOwnerInformationsByHBase = hBaseOperations
					.scanWithIndex(InformationDO.class, "relationLikeOwnerId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(InformationDO.class).toFilter(), informationPath);
			buffer = new StringBuilder();
			buffer.append("relationLikeOwnerId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<InformationDO> relationLikeOwnerInformationsBySolr = new SearchResult<InformationDO>(
					solrOperations.findByQuery(query, InformationDO.class, informationPath));
			// いいねを取得します。 LikeDO
			List<LikeDO> likesByHBase = hBaseOperations
					.scanWithIndex(LikeDO.class, "communityUserId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(LikeDO.class).toFilter(), path);
			buffer = new StringBuilder();
			buffer.append("communityUserId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<LikeDO> likesBySolr = new SearchResult<LikeDO>(
					solrOperations.findByQuery(query, LikeDO.class, path));
			// 退会ユーザーのレビューに対するいいねを取得します。
			List<LikeDO> relationReviewOwnerLikesByHBase = hBaseOperations
					.scanWithIndex(LikeDO.class, "relationReviewOwnerId", communityUser.getCommunityUserId());
			buffer = new StringBuilder();
			buffer.append("relationReviewOwnerId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<LikeDO> relationReviewOwnerLikesBySolr = new SearchResult<LikeDO>(
					solrOperations.findByQuery(query, LikeDO.class, path));
			// 退会ユーザーの質問に対する質問回答についたいいねを取得します。
			List<LikeDO> relationQuestionOwnerLikesByHBase = hBaseOperations
					.scanWithIndex(LikeDO.class, "relationQuestionOwnerId", communityUser.getCommunityUserId(), Path.DEFAULT);

			// IDで取得したデータが、nullのものも混ざっている
			buffer = new StringBuilder();
			buffer.append("relationQuestionOwnerId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<LikeDO> relationQuestionOwnerBySolr = new SearchResult<LikeDO>(
					solrOperations.findByQuery(query, LikeDO.class, path));
			// 退会ユーザーの質問回答に対するいいねを取得します。
			List<LikeDO> relationQuestionAnswerOwnerLikesByHBase = hBaseOperations
					.scanWithIndex(LikeDO.class, "relationQuestionAnswerOwnerId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(LikeDO.class).toFilter(), path);
			buffer = new StringBuilder();
			buffer.append("relationQuestionAnswerOwnerId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<LikeDO> relationQuestionAnswerLikesOwnerBySolr = new SearchResult<LikeDO>(
					solrOperations.findByQuery(query, LikeDO.class, path));
			// 退会ユーザーの画像に対するいいねを取得します。
			List<LikeDO> relationImageOwnerLikesByHBase = hBaseOperations
					.scanWithIndex(LikeDO.class, "relationImageOwnerId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(LikeDO.class).toFilter(), path);
			buffer = new StringBuilder();
			buffer.append("relationImageOwnerId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<LikeDO> relationImageOwnerLikesBySolr = new SearchResult<LikeDO>(
					solrOperations.findByQuery(query, LikeDO.class, path));
			// 商品フォローを取得します。 ProductFollowDO
			List<ProductFollowDO> productFollowsByHBase = hBaseOperations
					.scanWithIndex(ProductFollowDO.class, "communityUserId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(ProductFollowDO.class).toFilter(), path);
			buffer = new StringBuilder();
			buffer.append("communityUserId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<ProductFollowDO> productFollowsBySolr = new SearchResult<ProductFollowDO>(
					solrOperations.findByQuery(query, ProductFollowDO.class, path));
			// 購入を迷った商品情報を取得します。 PurchaseLostProductDO
			List<PurchaseLostProductDO> purchaseLostProductsByHBase = hBaseOperations
					.scanWithIndex(PurchaseLostProductDO.class, "communityUserId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(PurchaseLostProductDO.class).toFilter(), path);
			buffer = new StringBuilder();
			buffer.append("communityUserId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<PurchaseLostProductDO> purchaseLostProductsBySolr = new SearchResult<PurchaseLostProductDO>(
					solrOperations.findByQuery(query, PurchaseLostProductDO.class, path));
			// 購入商品情報を取得します。 PurchaseProductDO
			List<PurchaseProductDO> purchaseProductsByHBase = hBaseOperations
					.scanWithIndex(PurchaseProductDO.class, "communityUserId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(PurchaseProductDO.class).toFilter(), path);
			buffer = new StringBuilder();
			buffer.append("communityUserId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<PurchaseProductDO> purchaseProductsBySolr = new SearchResult<PurchaseProductDO>(
					solrOperations.findByQuery(query, PurchaseProductDO.class, path));
			// 質問フォローを取得します。 QuestionFollowDO
			List<QuestionFollowDO> questionFollowsByHBase = hBaseOperations
					.scanWithIndex(QuestionFollowDO.class, "communityUserId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(QuestionFollowDO.class).toFilter(), path);
			buffer = new StringBuilder();
			buffer.append("communityUserId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<QuestionFollowDO> questionFollowsBySolr = new SearchResult<QuestionFollowDO>(
					solrOperations.findByQuery(query, QuestionFollowDO.class, path));
			// 退会ユーザーの質問のフォローを習得します。
			List<QuestionFollowDO> relationQuestionOwnerQuestionFollowsByHBase = hBaseOperations
					.scanWithIndex(QuestionFollowDO.class, "relationQuestionOwnerId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(QuestionFollowDO.class).toFilter(), path);
			buffer = new StringBuilder();
			buffer.append("relationQuestionOwnerId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<QuestionFollowDO> relationQuestionOwnerQuestionFollowsBySolr = new SearchResult<QuestionFollowDO>(
					solrOperations.findByQuery(query, QuestionFollowDO.class, path));
			// レビューの購入の決め手情報を取得します。 ReviewDecisivePurchaseDO
			List<ReviewDecisivePurchaseDO> reviewDecisivePurchasesByHBase = hBaseOperations
					.scanWithIndex(ReviewDecisivePurchaseDO.class, "communityUserId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(ReviewDecisivePurchaseDO.class).toFilter(), path);
			buffer = new StringBuilder();
			buffer.append("communityUserId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<ReviewDecisivePurchaseDO> reviewDecisivePurchasesBySolr = new SearchResult<ReviewDecisivePurchaseDO>(
					solrOperations.findByQuery(query, ReviewDecisivePurchaseDO.class, path));
			// 過去に使用した商品情報を取得します。 UsedProductDO
			List<UsedProductDO> usedProductsByHBase = hBaseOperations
					.scanWithIndex(UsedProductDO.class, "communityUserId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(UsedProductDO.class).toFilter(), path);
			buffer = new StringBuilder();
			buffer.append("communityUserId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<UsedProductDO> usedProductsBySolr = new SearchResult<UsedProductDO>(
					solrOperations.findByQuery(query, UsedProductDO.class, path));
			// スパムレポートを取得します。
			List<SpamReportDO> spamReportsByHBase = hBaseOperations
					.scanWithIndex(SpamReportDO.class, "communityUserId", communityUser.getCommunityUserId(),
							hBaseOperations.createFilterBuilder(SpamReportDO.class).toFilter(), path);
			buffer = new StringBuilder();
			buffer.append("communityUserId_s:");
			buffer.append(communityUser.getCommunityUserId());
			query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT);
			SearchResult<SpamReportDO> spamReportsBySolr = new SearchResult<SpamReportDO>(
					solrOperations.findByQuery(query, SpamReportDO.class, path));
			// アナウンスを取得します。(HBaseのみ)
			List<AnnounceDO> announces = hBaseOperations
			.scanWithIndex(AnnounceDO.class, "communityUserId", communityUser.getCommunityUserId(),
					hBaseOperations.createFilterBuilder(AnnounceDO.class).toFilter(), path);
			// メール配信設定を取得します。(HBaseのみ)
			List<MailSettingDO> mailSettings = hBaseOperations
			.scanWithIndex(MailSettingDO.class, "communityUserId", communityUser.getCommunityUserId(),
					hBaseOperations.createFilterBuilder(MailSettingDO.class).toFilter(), path);
			// ソーシャル連携を取得します。(HBaseのみ)
			List<SocialMediaSettingDO> socialMediaSettings = hBaseOperations
			.scanWithIndex(SocialMediaSettingDO.class, "communityUserId", communityUser.getCommunityUserId(),
					hBaseOperations.createFilterBuilder(SocialMediaSettingDO.class).toFilter(), path);

			setReviewsByHBase(reviewsByHBase);
			setReviewsBySolr(reviewsBySolr.getDocuments());
			setQuestionsByHBase(questionsByHBase);
			setQuestionsBySolr(questionsBySolr.getDocuments());
			setQuestionAnswersByHBase(questionAnswersByHBase);
			setQuestionAnswersBySolr(questionAnswersBySolr.getDocuments());
			setImageHeaderByHBase(imageHeaderByHBase);
			setImageHeaderBySolr(imageHeaderBySolr.getDocuments());
			setCommentsByHBase(commentsByHBase);
			setCommentsBySolr(commentsBySolr.getDocuments());
			setRelationReviewOwnerCommentsByHBase(relationReviewOwnerCommentsByHBase);
			setRelationReviewOwnerCommentsBySolr(relationReviewOwnerCommentsBySolr.getDocuments());
			setRelationQuestionOwnerCommentsByHBase(relationQuestionOwnerCommentsByHBase);
			setRelationQuestionOwnerCommentsBySolr(relationQuestionOwnerCommentsBySolr.getDocuments());
			setRelationQuestionAnswerOwnerCommentsByHBase(relationQuestionAnswerOwnerCommentsByHBase);
			setRelationQuestionAnswerOwnerCommentsBySolr(relationQuestionAnswerOwnerCommentsBySolr.getDocuments());
			setRelationImageOwnerCommentsByHBase(relationImageOwnerCommentsByHBase);
			setRelationImageOwnerCommentsBySolr(relationImageOwnerCommentsBySolr.getDocuments());
			setActionHistorisByHBase(actionHistorisByHBase);
			setActionHistorisBySolr(actionHistorisBySolr.getDocuments());
			setActionUserWithdrawActionHistorysByHBase(actionUserWithdrawActionHistorysByHBase);
			setActionUserWithdrawActionHistorysBySolr(actionUserWithdrawActionHistorysBySolr.getDocuments());
			setCommunityUserFollowsByHBase(communityUserFollowsByHBase);
			setCommunityUserFollowsBySolr(communityUserFollowsBySolr.getDocuments());
			setFollowCommunityUserFollowsByHBase(followCommunityUserFollowsByHBase);
			setFollowCommunityUserFollowsBySolr(followCommunityUserFollowsBySolr.getDocuments());
			setInformationsByHBase(informationsByHBase);
			setInformationsBySolr(informationsBySolr.getDocuments());
			setRelationQuestionOwnerInformationsByHBase(relationQuestionOwnerInformationsByHBase);
			setRelationQuestionOwnerInformationsBySolr(relationQuestionOwnerInformationsBySolr.getDocuments());
			setRelationQuestionAnswerOwnerInformationsByHBase(relationQuestionAnswerOwnerInformationsByHBase);
			setRelationQuestionAnswerOwnerInformationsBySolr(relationQuestionAnswerOwnerInformationsBySolr.getDocuments());
			setRelationCommentOwnerInformationsByHBase(relationCommentOwnerInformationsByHBase);
			setRelationCommentOwnerInformationsBySolr(relationCommentOwnerInformationsBySolr.getDocuments());
			setRelationLikeOwnerInformationsByHBase(relationLikeOwnerInformationsByHBase);
			setRelationLikeOwnerInformationsBySolr(relationLikeOwnerInformationsBySolr.getDocuments());
			setLikesByHBase(likesByHBase);
			setLikesBySolr(likesBySolr.getDocuments());
			setRelationReviewOwnerLikesByHBase(relationReviewOwnerLikesByHBase);
			setRelationReviewOwnerLikesBySolr(relationReviewOwnerLikesBySolr.getDocuments());
			setRelationQuestionOwnerLikesByHBase(relationQuestionOwnerLikesByHBase);
			setRelationQuestionOwnerLikesBySolr(relationQuestionOwnerBySolr.getDocuments());
			setRelationQuestionAnswerOwnerLikesByHBase(relationQuestionAnswerOwnerLikesByHBase);
			setRelationQuestionAnswerOwnerLikesBySolr(relationQuestionAnswerLikesOwnerBySolr.getDocuments());
			setRelationImageOwnerLikesByHBase(relationImageOwnerLikesByHBase);
			setRelationImageOwnerLikesBySolr(relationImageOwnerLikesBySolr.getDocuments());
			setProductFollowsByHBase(productFollowsByHBase);
			setProductFollowsBySolr(productFollowsBySolr.getDocuments());
			setPurchaseLostProductsByHBase(purchaseLostProductsByHBase);
			setPurchaseLostProductsBySolr(purchaseLostProductsBySolr.getDocuments());
			setPurchaseProductsByHBase(purchaseProductsByHBase);
			setPurchaseProductsBySolr(purchaseProductsBySolr.getDocuments());
			setQuestionFollowsByHBase(questionFollowsByHBase);
			setQuestionFollowsBySolr(questionFollowsBySolr.getDocuments());
			setRelationQuestionOwnerQuestionFollowsByHBase(relationQuestionOwnerQuestionFollowsByHBase);
			setRelationQuestionOwnerQuestionFollowsBySolr(relationQuestionOwnerQuestionFollowsBySolr.getDocuments());
			setReviewDecisivePurchasesByHBase(reviewDecisivePurchasesByHBase);
			setReviewDecisivePurchasesBySolr(reviewDecisivePurchasesBySolr.getDocuments());
			setUsedProductsByHBase(usedProductsByHBase);
			setUsedProductsBySolr(usedProductsBySolr.getDocuments());
			setSpamReportsByHBase(spamReportsByHBase);
			setSpamReportsBySolr(spamReportsBySolr.getDocuments());
			setAnnounces(announces);
			setMailSettings(mailSettings);
			setSocialMediaSettings(socialMediaSettings);
			setCommentMapByHBase(commentMapByHBase);
			setCommentMapBySolr(commentMapBySolr);
		}

		public List<ReviewDO> getReviewsByHBase() {
			return reviewsByHBase;
		}

		public void setReviewsByHBase(List<ReviewDO> reviewsByHBase) {
			this.reviewsByHBase = reviewsByHBase;
		}

		public List<ReviewDO> getReviewsBySolr() {
			return reviewsBySolr;
		}

		public void setReviewsBySolr(List<ReviewDO> reviewsBySolr) {
			this.reviewsBySolr = reviewsBySolr;
		}

		public List<QuestionDO> getQuestionsByHBase() {
			return questionsByHBase;
		}

		public void setQuestionsByHBase(List<QuestionDO> questionsByHBase) {
			this.questionsByHBase = questionsByHBase;
		}

		public List<QuestionDO> getQuestionsBySolr() {
			return questionsBySolr;
		}

		public void setQuestionsBySolr(List<QuestionDO> questionsBySolr) {
			this.questionsBySolr = questionsBySolr;
		}


		public List<QuestionAnswerDO> getQuestionAnswersByHBase() {
			return questionAnswersByHBase;
		}

		public void setQuestionAnswersByHBase(
				List<QuestionAnswerDO> questionAnswersByHBase) {
			this.questionAnswersByHBase = questionAnswersByHBase;
		}

		public List<QuestionAnswerDO> getQuestionAnswersBySolr() {
			return questionAnswersBySolr;
		}

		public void setQuestionAnswersBySolr(
				List<QuestionAnswerDO> questionAnswersBySolr) {
			this.questionAnswersBySolr = questionAnswersBySolr;
		}

		public List<ImageHeaderDO> getImageHeaderByHBase() {
			return imageHeaderByHBase;
		}

		public void setImageHeaderByHBase(List<ImageHeaderDO> imageHeaderByHBase) {
			this.imageHeaderByHBase = imageHeaderByHBase;
		}

		public List<ImageHeaderDO> getImageHeaderBySolr() {
			return imageHeaderBySolr;
		}

		public void setImageHeaderBySolr(List<ImageHeaderDO> imageHeaderBySolr) {
			this.imageHeaderBySolr = imageHeaderBySolr;
		}

		public List<CommentDO> getCommentsByHBase() {
			return commentsByHBase;
		}

		public void setCommentsByHBase(List<CommentDO> commentsByHBase) {
			this.commentsByHBase = commentsByHBase;
		}

		public List<CommentDO> getCommentsBySolr() {
			return commentsBySolr;
		}

		public void setCommentsBySolr(List<CommentDO> commentsBySolr) {
			this.commentsBySolr = commentsBySolr;
		}

		public List<CommentDO> getRelationReviewOwnerCommentsByHBase() {
			return relationReviewOwnerCommentsByHBase;
		}

		public void setRelationReviewOwnerCommentsByHBase(
				List<CommentDO> relationReviewOwnerCommentsByHBase) {
			this.relationReviewOwnerCommentsByHBase = relationReviewOwnerCommentsByHBase;
		}

		public List<CommentDO> getRelationReviewOwnerCommentsBySolr() {
			return relationReviewOwnerCommentsBySolr;
		}

		public void setRelationReviewOwnerCommentsBySolr(
				List<CommentDO> relationReviewOwnerCommentsBySolr) {
			this.relationReviewOwnerCommentsBySolr = relationReviewOwnerCommentsBySolr;
		}

		public List<CommentDO> getRelationQuestionOwnerCommentsByHBase() {
			return relationQuestionOwnerCommentsByHBase;
		}

		public void setRelationQuestionOwnerCommentsByHBase(
				List<CommentDO> relationQuestionOwnerCommentsByHBase) {
			this.relationQuestionOwnerCommentsByHBase = relationQuestionOwnerCommentsByHBase;
		}

		public List<CommentDO> getRelationQuestionOwnerCommentsBySolr() {
			return relationQuestionOwnerCommentsBySolr;
		}

		public void setRelationQuestionOwnerCommentsBySolr(
				List<CommentDO> relationQuestionOwnerCommentsBySolr) {
			this.relationQuestionOwnerCommentsBySolr = relationQuestionOwnerCommentsBySolr;
		}

		public List<CommentDO> getRelationQuestionAnswerOwnerCommentsByHBase() {
			return relationQuestionAnswerOwnerCommentsByHBase;
		}

		public void setRelationQuestionAnswerOwnerCommentsByHBase(
				List<CommentDO> relationQuestionAnswerOwnerCommentsByHBase) {
			this.relationQuestionAnswerOwnerCommentsByHBase = relationQuestionAnswerOwnerCommentsByHBase;
		}

		public List<CommentDO> getRelationQuestionAnswerOwnerCommentsBySolr() {
			return relationQuestionAnswerOwnerCommentsBySolr;
		}

		public void setRelationQuestionAnswerOwnerCommentsBySolr(
				List<CommentDO> relationQuestionAnswerOwnerCommentsBySolr) {
			this.relationQuestionAnswerOwnerCommentsBySolr = relationQuestionAnswerOwnerCommentsBySolr;
		}

		public List<CommentDO> getRelationImageOwnerCommentsByHBase() {
			return relationImageOwnerCommentsByHBase;
		}

		public void setRelationImageOwnerCommentsByHBase(
				List<CommentDO> relationImageOwnerCommentsByHBase) {
			this.relationImageOwnerCommentsByHBase = relationImageOwnerCommentsByHBase;
		}

		public List<CommentDO> getRelationImageOwnerCommentsBySolr() {
			return relationImageOwnerCommentsBySolr;
		}

		public void setRelationImageOwnerCommentsBySolr(
				List<CommentDO> relationImageOwnerCommentsBySolr) {
			this.relationImageOwnerCommentsBySolr = relationImageOwnerCommentsBySolr;
		}

		public List<ActionHistoryDO> getActionHistorisByHBase() {
			return actionHistorisByHBase;
		}

		public void setActionHistorisByHBase(List<ActionHistoryDO> actionHistorisByHBase) {
			this.actionHistorisByHBase = actionHistorisByHBase;
		}

		public List<ActionHistoryDO> getActionHistorisBySolr() {
			return actionHistorisBySolr;
		}

		public void setActionHistorisBySolr(List<ActionHistoryDO> actionHistorisBySolr) {
			this.actionHistorisBySolr = actionHistorisBySolr;
		}

		public List<ActionHistoryDO> getActionUserWithdrawActionHistorysByHBase() {
			return actionUserWithdrawActionHistorysByHBase;
		}

		public void setActionUserWithdrawActionHistorysByHBase(
				List<ActionHistoryDO> actionUserWithdrawActionHistorysByHBase) {
			this.actionUserWithdrawActionHistorysByHBase = actionUserWithdrawActionHistorysByHBase;
		}

		public List<ActionHistoryDO> getActionUserWithdrawActionHistorysBySolr() {
			return actionUserWithdrawActionHistorysBySolr;
		}

		public void setActionUserWithdrawActionHistorysBySolr(
				List<ActionHistoryDO> actionUserWithdrawActionHistorysBySolr) {
			this.actionUserWithdrawActionHistorysBySolr = actionUserWithdrawActionHistorysBySolr;
		}

		public List<CommunityUserFollowDO> getCommunityUserFollowsByHBase() {
			return communityUserFollowsByHBase;
		}

		public void setCommunityUserFollowsByHBase(
				List<CommunityUserFollowDO> communityUserFollowsByHBase) {
			this.communityUserFollowsByHBase = communityUserFollowsByHBase;
		}

		public List<CommunityUserFollowDO> getCommunityUserFollowsBySolr() {
			return communityUserFollowsBySolr;
		}

		public void setCommunityUserFollowsBySolr(
				List<CommunityUserFollowDO> communityUserFollowsBySolr) {
			this.communityUserFollowsBySolr = communityUserFollowsBySolr;
		}

		public List<CommunityUserFollowDO> getFollowCommunityUserFollowsByHBase() {
			return followCommunityUserFollowsByHBase;
		}

		public void setFollowCommunityUserFollowsByHBase(
				List<CommunityUserFollowDO> followCommunityUserFollowsByHBase) {
			this.followCommunityUserFollowsByHBase = followCommunityUserFollowsByHBase;
		}

		public List<CommunityUserFollowDO> getFollowCommunityUserFollowsBySolr() {
			return followCommunityUserFollowsBySolr;
		}

		public void setFollowCommunityUserFollowsBySolr(
				List<CommunityUserFollowDO> followCommunityUserFollowsBySolr) {
			this.followCommunityUserFollowsBySolr = followCommunityUserFollowsBySolr;
		}

		public List<InformationDO> getInformationsByHBase() {
			return informationsByHBase;
		}

		public void setInformationsByHBase(List<InformationDO> informationsByHBase) {
			this.informationsByHBase = informationsByHBase;
		}

		public List<InformationDO> getInformationsBySolr() {
			return informationsBySolr;
		}

		public void setInformationsBySolr(List<InformationDO> informationsBySolr) {
			this.informationsBySolr = informationsBySolr;
		}

		public List<InformationDO> getRelationQuestionOwnerInformationsByHBase() {
			return relationQuestionOwnerInformationsByHBase;
		}

		public void setRelationQuestionOwnerInformationsByHBase(
				List<InformationDO> relationQuestionOwnerInformationsByHBase) {
			this.relationQuestionOwnerInformationsByHBase = relationQuestionOwnerInformationsByHBase;
		}

		public List<InformationDO> getRelationQuestionOwnerInformationsBySolr() {
			return relationQuestionOwnerInformationsBySolr;
		}

		public void setRelationQuestionOwnerInformationsBySolr(
				List<InformationDO> relationQuestionOwnerInformationsBySolr) {
			this.relationQuestionOwnerInformationsBySolr = relationQuestionOwnerInformationsBySolr;
		}

		public List<InformationDO> getRelationQuestionAnswerOwnerInformationsByHBase() {
			return relationQuestionAnswerOwnerInformationsByHBase;
		}

		public void setRelationQuestionAnswerOwnerInformationsByHBase(
				List<InformationDO> relationQuestionAnswerOwnerInformationsByHBase) {
			this.relationQuestionAnswerOwnerInformationsByHBase = relationQuestionAnswerOwnerInformationsByHBase;
		}

		public List<InformationDO> getRelationQuestionAnswerOwnerInformationsBySolr() {
			return relationQuestionAnswerOwnerInformationsBySolr;
		}

		public void setRelationQuestionAnswerOwnerInformationsBySolr(
				List<InformationDO> relationQuestionAnswerOwnerInformationsBySolr) {
			this.relationQuestionAnswerOwnerInformationsBySolr = relationQuestionAnswerOwnerInformationsBySolr;
		}

		public List<InformationDO> getRelationCommentOwnerInformationsByHBase() {
			return relationCommentOwnerInformationsByHBase;
		}

		public void setRelationCommentOwnerInformationsByHBase(
				List<InformationDO> relationCommentOwnerInformationsByHBase) {
			this.relationCommentOwnerInformationsByHBase = relationCommentOwnerInformationsByHBase;
		}

		public List<InformationDO> getRelationCommentOwnerInformationsBySolr() {
			return relationCommentOwnerInformationsBySolr;
		}

		public void setRelationCommentOwnerInformationsBySolr(
				List<InformationDO> relationCommentOwnerInformationsBySolr) {
			this.relationCommentOwnerInformationsBySolr = relationCommentOwnerInformationsBySolr;
		}

		public List<InformationDO> getRelationLikeOwnerInformationsByHBase() {
			return relationLikeOwnerInformationsByHBase;
		}

		public void setRelationLikeOwnerInformationsByHBase(
				List<InformationDO> relationLikeOwnerInformationsByHBase) {
			this.relationLikeOwnerInformationsByHBase = relationLikeOwnerInformationsByHBase;
		}

		public List<InformationDO> getRelationLikeOwnerInformationsBySolr() {
			return relationLikeOwnerInformationsBySolr;
		}

		public void setRelationLikeOwnerInformationsBySolr(
				List<InformationDO> relationLikeOwnerInformationsBySolr) {
			this.relationLikeOwnerInformationsBySolr = relationLikeOwnerInformationsBySolr;
		}

		public List<LikeDO> getLikesByHBase() {
			return likesByHBase;
		}

		public void setLikesByHBase(List<LikeDO> likesByHBase) {
			this.likesByHBase = likesByHBase;
		}

		public List<LikeDO> getLikesBySolr() {
			return likesBySolr;
		}

		public List<LikeDO> getRelationReviewOwnerLikesByHBase() {
			return relationReviewOwnerLikesByHBase;
		}

		public void setRelationReviewOwnerLikesByHBase(
				List<LikeDO> relationReviewOwnerLikesByHBase) {
			this.relationReviewOwnerLikesByHBase = relationReviewOwnerLikesByHBase;
		}

		public List<LikeDO> getRelationReviewOwnerLikesBySolr() {
			return relationReviewOwnerLikesBySolr;
		}

		public void setRelationReviewOwnerLikesBySolr(
				List<LikeDO> relationReviewOwnerLikesBySolr) {
			this.relationReviewOwnerLikesBySolr = relationReviewOwnerLikesBySolr;
		}

		public List<LikeDO> getRelationQuestionOwnerLikesByHBase() {
			return relationQuestionOwnerLikesByHBase;
		}

		public void setRelationQuestionOwnerLikesByHBase(
				List<LikeDO> relationQuestionOwnerLikesByHBase) {
			this.relationQuestionOwnerLikesByHBase = relationQuestionOwnerLikesByHBase;
		}

		public List<LikeDO> getRelationQuestionOwnerLikesBySolr() {
			return relationQuestionOwnerLikesBySolr;
		}

		public void setRelationQuestionOwnerLikesBySolr(
				List<LikeDO> relationQuestionOwnerLikesBySolr) {
			this.relationQuestionOwnerLikesBySolr = relationQuestionOwnerLikesBySolr;
		}

		public List<LikeDO> getRelationQuestionAnswerOwnerLikesByHBase() {
			return relationQuestionAnswerOwnerLikesByHBase;
		}

		public void setRelationQuestionAnswerOwnerLikesByHBase(
				List<LikeDO> relationQuestionAnswerOwnerLikesByHBase) {
			this.relationQuestionAnswerOwnerLikesByHBase = relationQuestionAnswerOwnerLikesByHBase;
		}

		public List<LikeDO> getRelationQuestionAnswerOwnerLikesBySolr() {
			return relationQuestionAnswerOwnerLikesBySolr;
		}

		public void setRelationQuestionAnswerOwnerLikesBySolr(
				List<LikeDO> relationQuestionAnswerOwnerLikesBySolr) {
			this.relationQuestionAnswerOwnerLikesBySolr = relationQuestionAnswerOwnerLikesBySolr;
		}

		public List<LikeDO> getRelationImageOwnerLikesByHBase() {
			return relationImageOwnerLikesByHBase;
		}

		public void setRelationImageOwnerLikesByHBase(
				List<LikeDO> relationImageOwnerLikesByHBase) {
			this.relationImageOwnerLikesByHBase = relationImageOwnerLikesByHBase;
		}

		public List<LikeDO> getRelationImageOwnerLikesBySolr() {
			return relationImageOwnerLikesBySolr;
		}

		public void setRelationImageOwnerLikesBySolr(
				List<LikeDO> relationImageOwnerLikesBySolr) {
			this.relationImageOwnerLikesBySolr = relationImageOwnerLikesBySolr;
		}

		public void setLikesBySolr(List<LikeDO> likesBySolr) {
			this.likesBySolr = likesBySolr;
		}

		public List<ProductFollowDO> getProductFollowsByHBase() {
			return productFollowsByHBase;
		}

		public void setProductFollowsByHBase(List<ProductFollowDO> productFollowsByHBase) {
			this.productFollowsByHBase = productFollowsByHBase;
		}

		public List<ProductFollowDO> getProductFollowsBySolr() {
			return productFollowsBySolr;
		}

		public void setProductFollowsBySolr(List<ProductFollowDO> productFollowsBySolr) {
			this.productFollowsBySolr = productFollowsBySolr;
		}

		public List<PurchaseLostProductDO> getPurchaseLostProductsByHBase() {
			return purchaseLostProductsByHBase;
		}

		public void setPurchaseLostProductsByHBase(
				List<PurchaseLostProductDO> purchaseLostProductsByHBase) {
			this.purchaseLostProductsByHBase = purchaseLostProductsByHBase;
		}

		public List<PurchaseLostProductDO> getPurchaseLostProductsBySolr() {
			return purchaseLostProductsBySolr;
		}

		public void setPurchaseLostProductsBySolr(
				List<PurchaseLostProductDO> purchaseLostProductsBySolr) {
			this.purchaseLostProductsBySolr = purchaseLostProductsBySolr;
		}

		public List<PurchaseProductDO> getPurchaseProductsByHBase() {
			return purchaseProductsByHBase;
		}

		public void setPurchaseProductsByHBase(
				List<PurchaseProductDO> purchaseProductsByHBase) {
			this.purchaseProductsByHBase = purchaseProductsByHBase;
		}

		public List<PurchaseProductDO> getPurchaseProductsBySolr() {
			return purchaseProductsBySolr;
		}

		public void setPurchaseProductsBySolr(
				List<PurchaseProductDO> purchaseProductsBySolr) {
			this.purchaseProductsBySolr = purchaseProductsBySolr;
		}

		public List<QuestionFollowDO> getQuestionFollowsByHBase() {
			return questionFollowsByHBase;
		}

		public void setQuestionFollowsByHBase(
				List<QuestionFollowDO> questionFollowsByHBase) {
			this.questionFollowsByHBase = questionFollowsByHBase;
		}

		public List<QuestionFollowDO> getQuestionFollowsBySolr() {
			return questionFollowsBySolr;
		}

		public void setQuestionFollowsBySolr(
				List<QuestionFollowDO> questionFollowsBySolr) {
			this.questionFollowsBySolr = questionFollowsBySolr;
		}

		public List<QuestionFollowDO> getRelationQuestionOwnerQuestionFollowsByHBase() {
			return relationQuestionOwnerQuestionFollowsByHBase;
		}

		public void setRelationQuestionOwnerQuestionFollowsByHBase(
				List<QuestionFollowDO> relationQuestionOwnerQuestionFollowsByHBase) {
			this.relationQuestionOwnerQuestionFollowsByHBase = relationQuestionOwnerQuestionFollowsByHBase;
		}

		public List<QuestionFollowDO> getRelationQuestionOwnerQuestionFollowsBySolr() {
			return relationQuestionOwnerQuestionFollowsBySolr;
		}

		public void setRelationQuestionOwnerQuestionFollowsBySolr(
				List<QuestionFollowDO> relationQuestionOwnerQuestionFollowsBySolr) {
			this.relationQuestionOwnerQuestionFollowsBySolr = relationQuestionOwnerQuestionFollowsBySolr;
		}

		public List<ReviewDecisivePurchaseDO> getReviewDecisivePurchasesByHBase() {
			return reviewDecisivePurchasesByHBase;
		}

		public void setReviewDecisivePurchasesByHBase(
				List<ReviewDecisivePurchaseDO> reviewDecisivePurchasesByHBase) {
			this.reviewDecisivePurchasesByHBase = reviewDecisivePurchasesByHBase;
		}

		public List<ReviewDecisivePurchaseDO> getReviewDecisivePurchasesBySolr() {
			return reviewDecisivePurchasesBySolr;
		}

		public void setReviewDecisivePurchasesBySolr(
				List<ReviewDecisivePurchaseDO> reviewDecisivePurchasesBySolr) {
			this.reviewDecisivePurchasesBySolr = reviewDecisivePurchasesBySolr;
		}

		public List<UsedProductDO> getUsedProductsByHBase() {
			return usedProductsByHBase;
		}

		public void setUsedProductsByHBase(List<UsedProductDO> usedProductsByHBase) {
			this.usedProductsByHBase = usedProductsByHBase;
		}

		public List<UsedProductDO> getUsedProductsBySolr() {
			return usedProductsBySolr;
		}

		public void setUsedProductsBySolr(List<UsedProductDO> usedProductsBySolr) {
			this.usedProductsBySolr = usedProductsBySolr;
		}

		public List<SpamReportDO> getSpamReportsByHBase() {
			return spamReportsByHBase;
		}

		public void setSpamReportsByHBase(List<SpamReportDO> spamReportsByHBase) {
			this.spamReportsByHBase = spamReportsByHBase;
		}

		public List<SpamReportDO> getSpamReportsBySolr() {
			return spamReportsBySolr;
		}

		public void setSpamReportsBySolr(List<SpamReportDO> spamReportsBySolr) {
			this.spamReportsBySolr = spamReportsBySolr;
		}

		public List<AnnounceDO> getAnnounces() {
			return announces;
		}

		public void setAnnounces(List<AnnounceDO> announces) {
			this.announces = announces;
		}

		public List<MailSettingDO> getMailSettings() {
			return mailSettings;
		}

		public void setMailSettings(List<MailSettingDO> mailSettings) {
			this.mailSettings = mailSettings;
		}

		public List<SocialMediaSettingDO> getSocialMediaSettings() {
			return socialMediaSettings;
		}

		public void setSocialMediaSettings(
				List<SocialMediaSettingDO> socialMediaSettings) {
			this.socialMediaSettings = socialMediaSettings;
		}

		public Map<String, List<CommentDO>> getCommentMapByHBase() {
			return commentMapByHBase;
		}

		public void setCommentMapByHBase(Map<String, List<CommentDO>> commentMapByHBase) {
			this.commentMapByHBase = commentMapByHBase;
		}

		public Map<String, List<CommentDO>> getCommentMapBySolr() {
			return commentMapBySolr;
		}

		public void setCommentMapBySolr(Map<String, List<CommentDO>> commentMapBySolr) {
			this.commentMapBySolr = commentMapBySolr;
		}

		private List<ReviewDO> reviewsByHBase; // 退会ユーザーのレビュー
		private List<ReviewDO> reviewsBySolr;
		private List<QuestionDO> questionsByHBase; // 退会ユーザーの質問
		private List<QuestionDO> questionsBySolr;
		private List<QuestionAnswerDO> questionAnswersByHBase; // 退会ユーザーの質問回答
		private List<QuestionAnswerDO> questionAnswersBySolr;
		private List<ImageHeaderDO> imageHeaderByHBase; // 退会ユーザーの画像
		private List<ImageHeaderDO> imageHeaderBySolr;
		private List<CommentDO> commentsByHBase; // 退会ユーザーのコメント
		private List<CommentDO> commentsBySolr;
		private List<CommentDO> relationReviewOwnerCommentsByHBase; // 退会ユーザーのレビューについたコメント
		private List<CommentDO> relationReviewOwnerCommentsBySolr;
		private List<CommentDO> relationQuestionOwnerCommentsByHBase; // 退会ユーザーの質問についたコメント
		private List<CommentDO> relationQuestionOwnerCommentsBySolr;
		private List<CommentDO> relationQuestionAnswerOwnerCommentsByHBase; // 退会ユーザーの質問回答についたコメント
		private List<CommentDO> relationQuestionAnswerOwnerCommentsBySolr;
		private List<CommentDO> relationImageOwnerCommentsByHBase; // 退会ユーザーの画像についたコメント
		private List<CommentDO> relationImageOwnerCommentsBySolr;
		private List<ActionHistoryDO> actionHistorisByHBase; // 退会ユーザーがオーナーのアクションヒストリー
		private List<ActionHistoryDO> actionHistorisBySolr;
		private List<ActionHistoryDO> actionUserWithdrawActionHistorysByHBase; // 退会ユーザーに関係するアクションヒストリー
		private List<ActionHistoryDO> actionUserWithdrawActionHistorysBySolr;
		private List<CommunityUserFollowDO> communityUserFollowsByHBase; //
		private List<CommunityUserFollowDO> communityUserFollowsBySolr;
		private List<CommunityUserFollowDO> followCommunityUserFollowsByHBase; //
		private List<CommunityUserFollowDO> followCommunityUserFollowsBySolr;
		private List<InformationDO> informationsByHBase; //
		private List<InformationDO> informationsBySolr;
		private List<InformationDO> relationQuestionOwnerInformationsByHBase; //
		private List<InformationDO> relationQuestionOwnerInformationsBySolr;
		private List<InformationDO> relationQuestionAnswerOwnerInformationsByHBase; //
		private List<InformationDO> relationQuestionAnswerOwnerInformationsBySolr;
		private List<InformationDO> relationCommentOwnerInformationsByHBase; // 退会ユーザーの投稿したコメントに紐づくお知らせ
		private List<InformationDO> relationCommentOwnerInformationsBySolr;
		private List<InformationDO> relationLikeOwnerInformationsByHBase; //
		private List<InformationDO> relationLikeOwnerInformationsBySolr;
		private List<LikeDO> likesByHBase; //
		private List<LikeDO> likesBySolr;
		private List<LikeDO> relationReviewOwnerLikesByHBase; //
		private List<LikeDO> relationReviewOwnerLikesBySolr;
		private List<LikeDO> relationQuestionOwnerLikesByHBase; //
		private List<LikeDO> relationQuestionOwnerLikesBySolr;
		private List<LikeDO> relationQuestionAnswerOwnerLikesByHBase; //
		private List<LikeDO> relationQuestionAnswerOwnerLikesBySolr;
		private List<LikeDO> relationImageOwnerLikesByHBase; //
		private List<LikeDO> relationImageOwnerLikesBySolr;
		private List<ProductFollowDO> productFollowsByHBase; //
		private List<ProductFollowDO> productFollowsBySolr;
		private List<PurchaseLostProductDO> purchaseLostProductsByHBase; //
		private List<PurchaseLostProductDO> purchaseLostProductsBySolr;
		private List<PurchaseProductDO> purchaseProductsByHBase; //
		private List<PurchaseProductDO> purchaseProductsBySolr;
		private List<QuestionFollowDO> questionFollowsByHBase; //
		private List<QuestionFollowDO> questionFollowsBySolr;
		private List<QuestionFollowDO> relationQuestionOwnerQuestionFollowsByHBase; //
		private List<QuestionFollowDO> relationQuestionOwnerQuestionFollowsBySolr;
		private List<ReviewDecisivePurchaseDO> reviewDecisivePurchasesByHBase; // レビューの購入の決め手情報
		private List<ReviewDecisivePurchaseDO> reviewDecisivePurchasesBySolr;
		private List<UsedProductDO> usedProductsByHBase; //
		private List<UsedProductDO> usedProductsBySolr;
		private List<SpamReportDO> spamReportsByHBase; //
		private List<SpamReportDO> spamReportsBySolr;
		private List<AnnounceDO> announces; //
		private List<MailSettingDO> mailSettings; //
		private List<SocialMediaSettingDO> socialMediaSettings; //
		private Map<String, List<CommentDO>> commentMapByHBase ; //
		private Map<String, List<CommentDO>> commentMapBySolr;
	}

	/**
	 * 停止状態の更新を検証します。
	 * @param communityUserId
	 */
	private void testUpdateStop(String communityUserId, boolean stop) {
		// 停止します。
		userService.updateStop(communityUserId, stop);
		if(stop) {
			checkCommunityUser(communityUserId, CommunityUserStatus.STOP);
		} else {
			checkCommunityUser(communityUserId, CommunityUserStatus.ACTIVE);
		}
	}

}
