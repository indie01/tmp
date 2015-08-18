package com.kickmogu.yodobashi.community.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.dao.ActionHistoryDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.SpamReportDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommentTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;
import com.kickmogu.yodobashi.community.resource.domain.constants.LikeTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportTargetType;
import com.kickmogu.yodobashi.community.service.config.ServiceConfig;

/**
 * ユーザーサービスのテストクラスです。
 *
 * @author hirabayashi
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/jcclientContext.xml")
public class QuestionServiceTest extends BaseTest {

	/**
	 * フォローサービスです。
	 */
	@Autowired
	protected FollowService followService;

	/**
	 * 質問サービスです。
	 */
	@Autowired
	private QuestionService questionService;

	/**
	 * いいねサービスです。
	 */
	@Autowired
	protected LikeService likeService;

	/**
	 * コメントサービスです。
	 */
	@Autowired
	protected CommentService commentService;

	/**
	 * 違反報告サービスです。
	 */
	@Autowired
	protected SpamReportService spamReportService;

	/**
	 * アクション履歴 DAO です。
	 */
	@Autowired
	private ActionHistoryDao actionHistoryDao;

	/**
	 * リクエストスコープで管理するオブジェクトを扱う DAO です。
	 */
	@Autowired
	private RequestScopeDao requestScopeDao;
	
	@Autowired
	private ServiceConfig serviceConfig;

	/**
	 * コミュニティユーザーです。
	 */
	private CommunityUserDO communityUser;
	private CommunityUserDO answerUser;
	private CommunityUserDO commentUser;
	private CommunityUserDO likeUser;
	private CommunityUserDO followerUser;
	private CommunityUserDO productFollowUser;
	private CommunityUserDO questionFollowUser;
	private CommunityUserDO spamReportUser;

	private Condition spamReportPath = Path
			.includeProp("*").includePath(
					"communityUser.communityUserId,review.reviewId,question.questionId," +
					"questionAnswer.questionAnswerId,comment.commentId").depth(1);

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
		createCommunityUserSet();
	}

	/**
	 * テストに関連するコミュニティユーザー情報を登録します。
	 */
	private void createCommunityUserSet() {
		communityUser = createCommunityUser("actionUser", false);
		commentUser = createCommunityUser("commentUser", false);
		likeUser = createCommunityUser("likeUser", false);
		followerUser = createCommunityUser("followerUser", false);
		productFollowUser = createCommunityUser("productFollowUser", false);
		questionFollowUser = createCommunityUser("questionFollowUser", false);
		answerUser =  createCommunityUser("answerUser", false);
		spamReportUser =   createCommunityUser("spamReportUser", false);
		// 質問投稿するコミュニティユーザー、コメントユーザーをフォローします。
		assertTrue(followService.followCommunityUser(
				followerUser.getCommunityUserId(),
				communityUser.getCommunityUserId(), false));
		assertTrue(followService.followCommunityUser(
				followerUser.getCommunityUserId(),
				answerUser.getCommunityUserId(), false));
		assertTrue(followService.followCommunityUser(
				followerUser.getCommunityUserId(),
				commentUser.getCommunityUserId(), false));
		// 質問される商品をフォローします。
		assertTrue(followService.followProduct(
				productFollowUser.getCommunityUserId(), "100000001000624829",
				false));
		assertTrue(followService.followProduct(
				productFollowUser.getCommunityUserId(), "200000002000012355",
				false));
		// 質問される商品を購入します。
		Date salesDate = new Date();
		createReceipt(answerUser, "4905524312737", salesDate);
	}

	/**
	 * 質問を検証します。
	 */
	@Test
	public void testSaveQuestion() {
		QuestionDO question = null;
		// 質問の登録・検証(一時保存)します。
		QuestionDO saveQuestion = testSaveQuestion(question, ContentsStatus.SAVE, "質問の登録(一時保存)", product);
		// 質問の編集(一時保存)・検証します。
		saveQuestion = testSaveQuestion(saveQuestion, ContentsStatus.SAVE, "質問の編集(一時保存)", product);
		// 質問の登録・検証します。
		saveQuestion = testSaveQuestion(saveQuestion, ContentsStatus.SUBMITTED, "質問の登録", product);
		// アクションヒストリーを検証します。
		checkActionHistory(saveQuestion.getCommunityUser(), saveQuestion, ActionHistoryType.USER_QUESTION);
		checkActionHistory(saveQuestion.getCommunityUser(), saveQuestion, ActionHistoryType.PRODUCT_QUESTION);
		// 質問の編集・検証します。
		saveQuestion = testSaveQuestion(saveQuestion, ContentsStatus.SUBMITTED, "質問の編集", product);
		// 質問をフォローします。
		assertEquals(true,
				followService.followQuestion(questionFollowUser.getCommunityUserId(), saveQuestion.getQuestionId(), false));
		// 回答を登録・検証(一時保存)します。
		QuestionAnswerDO answer = null;
		QuestionAnswerDO saveAanswer = testSaveQuestionAnswer(saveQuestion, answer, ContentsStatus.SAVE, "質問回答(一時保存)");
		// 回答を編集・検証(一時保存)します。
		saveAanswer = testSaveQuestionAnswer(saveQuestion, saveAanswer, ContentsStatus.SAVE, "質問回答編集(一時保存)");
		// 回答を登録・検証します。
		saveAanswer = testSaveQuestionAnswer(saveQuestion, saveAanswer, ContentsStatus.SUBMITTED, "質問回答");
		// コメントのアクションヒストリーを検証します。
		checkActionHistory(saveAanswer.getCommunityUser(), saveAanswer,
				ActionHistoryType.USER_ANSWER); // フォローしているユーザーがレビューに回答をした
		// 回答を編集・検証します。
		saveAanswer = testSaveQuestionAnswer(saveQuestion, saveAanswer, ContentsStatus.SUBMITTED, "質問回答編集");
		// いいねを登録・検証します。
		createLike(saveAanswer, false);
		// いいねを解除・検証します。
		createLike(saveAanswer, true);
		// いいね50件以上を検証します。
		createLike50(saveAanswer);
		// コメントを登録・検証します。
		CommentDO comment = null;
		CommentDO saveComment = createComment(saveAanswer, comment, "コメント投稿");
		// コメントを修正・検証します。
		saveComment = createComment(saveAanswer, saveComment, "コメント修正");
		// 違反報告を登録・検証します。
		createSpamReport(saveQuestion, "質問違反報告登録");
		createSpamReport(saveAanswer, "質問回答違反報告登録");
		// コメントを削除・検証します。
		testDeleteComment(saveComment);
		// コメントを登録・検証します。
		saveComment = createComment(saveAanswer, comment, "コメント削除後・コメント再投稿");
		// 回答を削除・検証します。
		testDeleteQuestionAnswer(saveAanswer, saveComment);
		// 回答を登録・検証します。
		saveAanswer = testSaveQuestionAnswer(saveQuestion, answer, ContentsStatus.SUBMITTED, "回答削除後・質問回答再投稿");
		// コメントを登録・検証します。
		saveComment = createComment(saveAanswer, comment, "回答削除後・コメント再投稿");
		// 質問を削除・検証します。
		testDeleteQuestion(saveQuestion, saveAanswer, saveComment);
	}

	/**
	 * 質問の登録・検証をします。
	 * @param product
	 */
	private QuestionDO testSaveQuestion(QuestionDO question, ContentsStatus status, String questionText, ProductDO product) {
		if(question==null) {
			question = new QuestionDO();
			question.setProduct(product);
		}
		question.setCommunityUser(communityUser);
		question.setStatus(status);
		createImage(communityUser);
		createImageTwo(communityUser);
		String tempImageUrl = resourceConfig.temporaryImageUrl + image.getImageId();
		String tempImageUrl2 = resourceConfig.temporaryImageUrl + image2.getImageId();
		StringBuilder html = new StringBuilder();
		html.append("<img src=\"" + tempImageUrl + "\">");
		html.append("<script type=\"text/javascript\">alert('" + questionText + "本文" + "')</script>");
		html.append("<img src=\"" + tempImageUrl2 + "\">");
		question.setQuestionBody(html.toString());
		QuestionDO saveQuestion = questionService.saveQuestion(question);
		// 登録した質問を取得します。
		QuestionDO questionByHBase = hBaseOperations.load(QuestionDO.class,
				saveQuestion.getQuestionId(), questionPath);
		QuestionDO questionBySolr = solrOperations.load(QuestionDO.class,
				saveQuestion.getQuestionId(), questionPath);
		// 質問の検証をします。
		checkQuestion(questionByHBase, question);
		checkQuestion(questionBySolr, question);
		return saveQuestion;
	}

	/**
	 * 質問の検証をします。
	 * @param saveQuestion
	 * @param question
	 */
	private void checkQuestion(QuestionDO saveQuestion, QuestionDO question) {
		assertNotNull(saveQuestion);
		assertEquals(question.getQuestionId(), saveQuestion.getQuestionId());
		assertEquals(question.getCommunityUser().getCommunityUserId(),
				saveQuestion.getCommunityUser().getCommunityUserId());
		assertEquals(question.getProduct().getSku(), saveQuestion.getProduct().getSku());
		assertEquals(question.getQuestionBody(), saveQuestion.getQuestionBody());
		assertEquals(question.getStatus(), saveQuestion.getStatus());
		if(ContentsStatus.SAVE == saveQuestion.getStatus()) {
			assertEquals(null, saveQuestion.getPostDate()); //投稿日
			assertEquals(null, saveQuestion.getDeleteDate()); //削除日
		} else if(ContentsStatus.SUBMITTED == saveQuestion.getStatus()) {
			assertNotNull(saveQuestion.getPostDate()); //投稿日
			assertEquals(null, saveQuestion.getDeleteDate()); //削除日
		} else if(ContentsStatus.DELETE == saveQuestion.getStatus()) {
			assertNotNull(saveQuestion.getPostDate()); //投稿日
			assertNotNull(saveQuestion.getDeleteDate()); //削除日
		}
		assertNotNull(saveQuestion.getSaveDate()); //保存日時(アプリケーション日付)
		assertNotNull(saveQuestion.getModifyDateTime()); //更新日時(システム日付)
		assertEquals(saveQuestion.getSaveDate(), saveQuestion.getModifyDateTime()); //本テストでは同一のものが入っているケースしかないため一致
	}

	/**
	 * 質問回答の登録・検証をします。
	 */
	private QuestionAnswerDO testSaveQuestionAnswer(QuestionDO question, QuestionAnswerDO answer, ContentsStatus status, String answerText) {
		if(answer==null) {
			answer = new QuestionAnswerDO();
			answer.setQuestion(question);
			answer.setCommunityUser(answerUser);
		}
		createImage(answerUser);
		createImageTwo(answerUser);
		String tempImageUrl = resourceConfig.temporaryImageUrl + image.getImageId();
		String tempImageUrl2 = resourceConfig.temporaryImageUrl + image2.getImageId();
		StringBuilder html = new StringBuilder();
		html.append("<img src=\"" + tempImageUrl + "\">");
		html.append("<script type=\"text/javascript\">alert('" + answerText + "本文" + "')</script>");
		html.append("<img src=\"" + tempImageUrl2 + "\">");
		answer.setAnswerBody(html.toString());
		answer.setStatus(status);
		QuestionAnswerDO saveAnswer = questionService.saveQuestionAnswer(answer);
		// 登録した質問回答を取得します。
		QuestionAnswerDO questionAnswerByHBase = hBaseOperations.load(QuestionAnswerDO.class,
				saveAnswer.getQuestionAnswerId(), questionAnswerPath);
		QuestionAnswerDO questionAnswerBySolr = solrOperations.load(QuestionAnswerDO.class,
				saveAnswer.getQuestionAnswerId(), questionAnswerPath);
		// 登録した質問回答を検証します。
		checkQuestionAnswer(questionAnswerByHBase, answer);
		checkQuestionAnswer(questionAnswerBySolr, answer);
		return saveAnswer;
	}

	/**
	 * 質問回答の検証をします。
	 * @param saveAnswer
	 * @param answer
	 */
	private void checkQuestionAnswer(QuestionAnswerDO saveAnswer, QuestionAnswerDO answer) {
		assertNotNull(saveAnswer);
		assertEquals(answer.getQuestion().getQuestionId(), saveAnswer.getQuestion().getQuestionId());
		assertEquals(answer.getQuestionAnswerId(), saveAnswer.getQuestionAnswerId());
		assertEquals(answer.getAnswerBody(), saveAnswer.getAnswerBody());
		assertEquals(answer.getCommunityUser().getCommunityUserId(), saveAnswer.getCommunityUser().getCommunityUserId());
		assertEquals(answer.getStatus(), saveAnswer.getStatus());
		if(ContentsStatus.SAVE == saveAnswer.getStatus()) {
			assertEquals(null, saveAnswer.getPostDate()); //投稿日
			assertEquals(null, saveAnswer.getDeleteDate()); //削除日
		} else if(ContentsStatus.SUBMITTED == saveAnswer.getStatus()) {
			assertNotNull(saveAnswer.getPostDate()); //投稿日
			assertEquals(null, saveAnswer.getDeleteDate()); //削除日
		} else if(ContentsStatus.DELETE == saveAnswer.getStatus()) {
			assertNotNull(saveAnswer.getPostDate()); //投稿日
			assertNotNull(saveAnswer.getDeleteDate()); //削除日
		}
		assertNotNull(saveAnswer.getSaveDate()); //保存日時(アプリケーション日付)
		assertNotNull(saveAnswer.getModifyDateTime()); //更新日時(システム日付)
		assertEquals(saveAnswer.getSaveDate(), saveAnswer.getModifyDateTime()); //本テストでは同一のものが入っているケースしかないため一致
	}

	/**
	 * いいねの登録・検証を行います。
	 * @param question
	 * @param release
	 */
	private void createLike(QuestionAnswerDO answer, boolean release) {
		// 質問回答に対していいねを登録・解除します。
		likeService.updateLikeQuestionAnswer(likeUser.getCommunityUserId(), answer.getQuestionAnswerId(), release);
		Condition path = Path.includeProp("*")
				.includePath("communityUser.communityUserId,questionAnswer.questionAnswerId").depth(1);
		// いいねをHBaseから取得します。
		List<LikeDO> likesByHBase = hBaseOperations
				.scanWithIndex(LikeDO.class, "communityUserId", likeUser.getCommunityUserId(),
						hBaseOperations.createFilterBuilder(LikeDO.class)
								.appendSingleColumnValueFilter("questionAnswerId", CompareOp.EQUAL,
										answer.getQuestionAnswerId()).toFilter(), path);
		// いいねをSolrから取得します。
		StringBuilder buffer = new StringBuilder();
		buffer.append("questionAnswerId_s:");
		buffer.append(answer.getQuestionAnswerId());
		buffer.append(" AND communityUserId_s:");
		buffer.append(likeUser.getCommunityUserId());
		SolrQuery query = new SolrQuery(buffer.toString());
		SearchResult<LikeDO> likesBySolr = new SearchResult<LikeDO>(
				solrOperations.findByQuery(query, LikeDO.class, path));
		// お知らせを取得します。
		List<InformationDO> informationsByHBase =
				getHBaseInformation(answer.getCommunityUser(), InformationType.QUESTION_ANSWER_LIKE_ADD);
		SearchResult<InformationDO> informationsBySolr =
				getSolrInformation(answer.getCommunityUser(), InformationType.QUESTION_ANSWER_LIKE_ADD);
		// いいねの検証を行います。
		if(release) {
			// いいねを外した場合。
			assertEquals(0, likesByHBase.size());
			assertEquals(0, likesBySolr.getDocuments().size());
			assertEquals(0, informationsByHBase.size());
			assertEquals(0, informationsBySolr.getDocuments().size());
			// お知らせの検証を行います。
		} else {
			// いいねをした場合。
			LikeDO likeByHBase = likesByHBase.get(0);
			LikeDO likeBySolr = likesBySolr.getDocuments().get(0);
			checkLike(likeByHBase, answer);
			checkLike(likeBySolr, answer);
			assertEquals(1, likesBySolr.getDocuments().size());
			// お知らせの検証を行います。
			checkInformation(informationsByHBase.get(0), answer, likeByHBase);
			checkInformation(informationsBySolr.getDocuments().get(0), answer, likeBySolr);
			// 50件以上のアクションヒストリーが不要なことを確認。
			assertEquals(false, actionHistoryDao.requiredSaveLikeMilstone(likeBySolr, serviceConfig.likeThresholdLimit));
		}
	}

	/**
	 * いいねの検証を行います。
	 * @param like
	 */
	private void checkLike(LikeDO like, QuestionAnswerDO answer) {
		assertEquals(likeUser.getCommunityUserId(), like.getCommunityUser().getCommunityUserId());
		assertEquals(answer.getQuestionAnswerId(), like.getQuestionAnswer().getQuestionAnswerId());
		assertEquals(answer.getCommunityUser().getCommunityUserId(), like.getRelationQuestionAnswerOwnerId());
		assertEquals(answer.getProduct().getSku(), like.getSku());
		assertEquals(like.getPostDate(), like.getModifyDateTime());
		assertEquals(LikeTargetType.QUESTION_ANSWER, like.getTargetType());
	}

	/**
	 * お知らせの検証を行います。
	 * @param information
	 * @param review
	 */
	private void checkInformation(InformationDO information, QuestionAnswerDO answer, LikeDO like) {
		assertEquals(answer.getQuestionAnswerId(), information.getQuestionAnswer().getQuestionAnswerId());
		assertEquals(answer.getCommunityUser().getCommunityUserId(),
				information.getCommunityUser().getCommunityUserId());
		assertEquals(answer.isAdult(), information.isAdult());
		assertEquals(like.getLikeId(), information.getLike().getLikeId());
		assertEquals(likeUser.getCommunityUserId(), information.getRelationLikeOwnerId());
	}

	/**
	 * いいね50件以上を登録・検証します。
	 *
	 * @param review
	 */
	private void createLike50(QuestionAnswerDO answer) {
		CommunityUserDO testLikeUser50 = new CommunityUserDO();
		for(int i=1; i<=serviceConfig.likeThresholdLimit; i++) {
			CommunityUserDO testLikeUser = createCommunityUser("testQuestionAnswerLikeUser" + i, false);
			assertTrue(likeService.updateLikeQuestionAnswer(testLikeUser.getCommunityUserId(), answer.getQuestionAnswerId(), false) == 1);
			if(i==serviceConfig.likeThresholdLimit){
				testLikeUser50 = testLikeUser;
			}
		}
		// アクションヒストリーをSolrから取得します。
		StringBuilder buffer = new StringBuilder();
		buffer.append("questionAnswerId_s:");
		buffer.append(answer.getQuestionAnswerId());
		buffer.append(" AND actionHistoryType_s:");
		buffer.append(ActionHistoryType.LIKE_ANSWER_50.getCode());
		SolrQuery query = new SolrQuery(buffer.toString());
		Condition path = Path.includeProp("*").includePath(
						"communityUser.communityUserId,product.sku,questionAnswer.questionAnswerId").depth(2);
		SearchResult<ActionHistoryDO> actionHistorisBySolr = new SearchResult<ActionHistoryDO>(
				solrOperations.findByQuery(query, ActionHistoryDO.class, path));
		ActionHistoryDO actionHistoryBySolr = actionHistorisBySolr.getDocuments().get(0);
		// アクションヒストリーをHBaseから取得します。
		ActionHistoryDO actionHistoryByHBase =
				hBaseOperations.load(ActionHistoryDO.class, actionHistoryBySolr.getActionHistoryId(), path);
		checkLike50(actionHistoryByHBase, answer, testLikeUser50);
		checkLike50(actionHistoryBySolr, answer, testLikeUser50);
	}

	/**
	 * いいね50件以上を検証します。
	 * @param actionHistory
	 * @param review
	 */
	private void checkLike50(ActionHistoryDO actionHistory, QuestionAnswerDO answer, CommunityUserDO testLikeUser50) {
		assertEquals(answer.getQuestionAnswerId(), actionHistory.getQuestionAnswer().getQuestionAnswerId());
		assertEquals(answer.getProduct().getSku(), actionHistory.getProduct().getSku());
		assertEquals(answer.isAdult(), actionHistory.isAdult());
		assertEquals(testLikeUser50.getCommunityUserId(), actionHistory.getCommunityUser().getCommunityUserId());
		assertEquals(answerUser.getCommunityUserId(), actionHistory.getRelationQuestionAnswerOwnerId());
		assertEquals(communityUser.getCommunityUserId(), actionHistory.getRelationQuestionOwnerId());
	}

	/**
	 * コメントの登録・検証を行います。
	 *
	 * @param review
	 */
	private CommentDO createComment(QuestionAnswerDO questionAnswer, CommentDO comment, String commentText) {
		if(comment==null) {
			comment = new CommentDO();
		}
		comment.setCommunityUser(commentUser);
		comment.setTargetType(CommentTargetType.QUESTION_ANSWER);
		comment.setQuestionAnswer(questionAnswer);
		comment.setCommentBody(commentText);
		CommentDO saveComment = commentService.saveComment(comment);

		// コメントを取得します。
		Condition path = Path.includeProp("*").includePath(
				"communityUser.communityUserId,questionAnswer.questionAnswerId").depth(1);
		CommentDO commentByHBase = hBaseOperations.load(CommentDO.class,
				saveComment.getCommentId(), path);
		CommentDO commentBySolr = solrOperations.load(CommentDO.class,
				saveComment.getCommentId(), path);
		// コメントを検証します。
		checkComment(comment, commentByHBase);
		checkComment(comment, commentBySolr);
		assertEquals(commentBySolr.getCommentId(), commentByHBase.getCommentId());
		return commentBySolr;
	}

	/**
	 * コメントの検証を行います。
	 *
	 * @param comment
	 */
	private void checkComment(CommentDO comment, CommentDO checkComment) {
		assertEquals(comment.getCommunityUser().getCommunityUserId(),
				checkComment.getCommunityUser().getCommunityUserId());
		assertEquals(comment.getTargetType(), checkComment.getTargetType());
		assertEquals(comment.getQuestionAnswer().getQuestionAnswerId(), checkComment
				.getQuestionAnswer().getQuestionAnswerId());
		assertEquals(comment.getCommentBody(), checkComment.getCommentBody());
		assertEquals(comment.getQuestionAnswer().getCommunityUser().getCommunityUserId(),
				checkComment.getRelationQuestionAnswerOwnerId());
		assertEquals(comment.getQuestionAnswer().getQuestion().getCommunityUser().getCommunityUserId(),
				checkComment.getRelationQuestionOwnerId());
		if(checkComment.isWithdraw()) {
			assertNotNull(checkComment.getDeleteDate());
		} else {
			assertNotNull(checkComment.getPostDate());
			assertEquals(null, checkComment.getDeleteDate());
		}
	}

	/**
	 * 違反報告を登録・検証します。(質問)
	 * @param question
	 * @param status
	 * @param spamReportBody
	 * @return
	 */
	private SpamReportDO createSpamReport(QuestionDO question, String spamReportBody) {
		SpamReportDO spamReport = new SpamReportDO();
		spamReport.setCommunityUser(spamReportUser);
		spamReport.setQuestion(question);
		spamReport.setStatus(SpamReportStatus.NEW);
		spamReport.setSpamReportBody(spamReportBody);
		spamReport.setTargetType(SpamReportTargetType.QUESTION);
		SpamReportDO saveSpamReport = spamReportService.createSpamReport(spamReport);
		// 登録した違反報告を取得します。
		SpamReportDO spamReportByHBase = hBaseOperations.load(SpamReportDO.class,
				saveSpamReport.getSpamReportId(), spamReportPath);
		SpamReportDO spamReportBySolr = solrOperations.load(SpamReportDO.class,
				saveSpamReport.getSpamReportId(), spamReportPath);
		checkSpamReport(spamReport, spamReportByHBase);
		checkSpamReport(spamReport, spamReportBySolr);
		return saveSpamReport;
	}

	/**
	 * 違反報告を登録・検証します。(質問回答)
	 * @param question
	 * @param status
	 * @param spamReportBody
	 * @return
	 */
	private SpamReportDO createSpamReport(QuestionAnswerDO questionAnswer, String spamReportBody) {
		SpamReportDO spamReport = new SpamReportDO();
		spamReport.setCommunityUser(spamReportUser);
		spamReport.setQuestionAnswer(questionAnswer);
		spamReport.setStatus(SpamReportStatus.NEW);
		spamReport.setSpamReportBody(spamReportBody);
		spamReport.setTargetType(SpamReportTargetType.QUESTION_ANSWER);
		SpamReportDO saveSpamReport = spamReportService.createSpamReport(spamReport);
		// 登録した違反報告を取得します。
		SpamReportDO spamReportByHBase = hBaseOperations.load(SpamReportDO.class,
				saveSpamReport.getSpamReportId(), spamReportPath);
		SpamReportDO spamReportBySolr = solrOperations.load(SpamReportDO.class,
				saveSpamReport.getSpamReportId(), spamReportPath);
		checkSpamReport(spamReport, spamReportByHBase);
		checkSpamReport(spamReport, spamReportBySolr);
		return saveSpamReport;
	}

	/**
	 * 違反報告を検証します。
	 * @param spamReport
	 * @param saveSpamReport
	 */
	private void checkSpamReport(SpamReportDO spamReport, SpamReportDO saveSpamReport) {
		assertNotNull(saveSpamReport);
		assertEquals(spamReport.getCommunityUser().getCommunityUserId(),
				saveSpamReport.getCommunityUser().getCommunityUserId());
		assertEquals(spamReport.getQuestion().getQuestionId(),
				saveSpamReport.getQuestion().getQuestionId());
		assertEquals(spamReport.getStatus(), saveSpamReport.getStatus());
		assertEquals(spamReport.getSpamReportBody(), saveSpamReport.getSpamReportBody());
		assertEquals(spamReport.getTargetType(), saveSpamReport.getTargetType());
	}

	/**
	 * コメントの削除・検証をします。
	 * @param comment
	 */
	private void testDeleteComment(CommentDO comment) {
		Condition path = Path.includeProp("*").includePath("communityUser.communityUserId").depth(1);
		requestScopeDao.initialize(comment.getCommunityUser(), null);
		commentService.deleteComment(comment.getCommentId());
		requestScopeDao.destroy();
		CommentDO commentByHBase = hBaseOperations.load(CommentDO.class,
				comment.getCommentId(), path);
		CommentDO commentBySolr = solrOperations.load(CommentDO.class,
				comment.getCommentId(), path);
		// コメントの削除を検証します。
		assertNotNull(commentByHBase);
		assertTrue(commentByHBase.isDeleted());
		assertTrue(commentBySolr.isDeleted());
	}

	/**
	 * 質問回答の削除・検証をします。
	 * @param questionAnswer
	 * @param comment
	 */
	private void testDeleteQuestionAnswer(QuestionAnswerDO questionAnswer, CommentDO comment) {
		requestScopeDao.initialize(questionAnswer.getCommunityUser(), null);
		questionService.deleteQuestionAnswer(questionAnswer.getQuestionAnswerId());
		requestScopeDao.destroy();
		// 質問回答を取得します。
		QuestionAnswerDO questionAnswerByHBase = hBaseOperations.load(QuestionAnswerDO.class,
				questionAnswer.getQuestionAnswerId(), questionAnswerPath);
		QuestionAnswerDO questionAnswerBySolr = solrOperations.load(QuestionAnswerDO.class,
				questionAnswer.getQuestionAnswerId(), questionAnswerPath);
		// 質問回答の削除を検証します。
		assertNotNull(questionAnswerByHBase);
		assertTrue(questionAnswerByHBase.isDeleted());
		assertTrue(questionAnswerBySolr.isDeleted());
		// 回答に紐づくコメントの削除を検証します。
		if(comment!=null) {
			testDeleteComment(comment);
		}
	}

	private void testDeleteQuestion(QuestionDO question, QuestionAnswerDO questionAnswer, CommentDO comment) {
		requestScopeDao.initialize(question.getCommunityUser(), null);
		questionService.deleteQuestion(question.getQuestionId());
		requestScopeDao.destroy();
		// 質問を取得します。
		QuestionDO questionByHBase = hBaseOperations.load(QuestionDO.class,
				question.getQuestionId(), questionAnswerPath);
		QuestionDO questionBySolr = solrOperations.load(QuestionDO.class,
				question.getQuestionId(), questionAnswerPath);
		// 質問の削除を検証します。
		assertNotNull(questionByHBase);
		assertTrue(questionByHBase.isDeleted());
		assertTrue(questionBySolr.isDeleted());
		// 質問に紐づく回答・回答コメントの削除を検証します。
		if(questionAnswer!=null) {
			testDeleteQuestionAnswer(questionAnswer, comment);
		}
	}

	/**
	 * 質問投稿時のアクションヒストリーを検証します。
	 */
	private void checkActionHistory(CommunityUserDO communityUser,
			QuestionDO question, ActionHistoryType actionHistoryType) {
		// アクションヒストリーをHBaseから取得します。
		List<ActionHistoryDO> actionHistorisByHBase =
				getHBaseActionHistorisByCommunityUserAndActionHistoryType(communityUser, actionHistoryType);
		ActionHistoryDO userActionHistoryByHBase = actionHistorisByHBase.get(0);
		// アクションヒストリーをSolrから取得します。
		SearchResult<ActionHistoryDO> actionHistorisBySolr =
				getSolrActionHistorisByCommunityUserAndActionHistoryType(communityUser, actionHistoryType);
		ActionHistoryDO userActionHistoryBySolr = actionHistorisBySolr.getDocuments().get(0);
		// アクションヒストリーを検証します。
		checkActionHistory(communityUser, question, userActionHistoryByHBase);
		checkActionHistory(communityUser, question, userActionHistoryBySolr);
	}

	/**
	 * アクションヒストリーを検証します。
	 */
	private void checkActionHistory(CommunityUserDO communityUser,
			QuestionDO question, ActionHistoryDO actionHistory) {
		assertEquals(communityUser.getCommunityUserId(), actionHistory
				.getCommunityUser().getCommunityUserId());
		assertEquals(question.getQuestionId(), actionHistory.getQuestion().getQuestionId());
		assertEquals(question.getProduct().isAdult(), actionHistory.isAdult());
		assertNotNull(actionHistory.getActionTime());
		assertNotNull(actionHistory.getRegisterDateTime());
		assertNotNull(actionHistory.getModifyDateTime());
	}

	/**
	 * 質問回答投稿時のアクションヒストリーを検証します。
	 */
	private void checkActionHistory(CommunityUserDO communityUser,
			QuestionAnswerDO questionAnswer, ActionHistoryType actionHistoryType) {
		// アクションヒストリーをHBaseから取得します。
		List<ActionHistoryDO> actionHistorisByHBase =
				getHBaseActionHistorisByCommunityUserAndActionHistoryType(communityUser, actionHistoryType);
		ActionHistoryDO userActionHistoryByHBase = actionHistorisByHBase.get(0);
		// アクションヒストリーをSolrから取得します。
		SearchResult<ActionHistoryDO> actionHistorisBySolr =
				getSolrActionHistorisByCommunityUserAndActionHistoryType(communityUser, actionHistoryType);
		ActionHistoryDO userActionHistoryBySolr = actionHistorisBySolr.getDocuments().get(0);
		// アクションヒストリーを検証します。
		checkActionHistory(communityUser, questionAnswer, userActionHistoryByHBase);
		checkActionHistory(communityUser, questionAnswer, userActionHistoryBySolr);
	}

	/**
	 * アクションヒストリーを検証します。
	 */
	private void checkActionHistory(CommunityUserDO communityUser,
			QuestionAnswerDO questionAnswer, ActionHistoryDO actionHistory) {
		assertEquals(communityUser.getCommunityUserId(), actionHistory
				.getCommunityUser().getCommunityUserId());
		assertEquals(questionAnswer.getCommunityUser().getCommunityUserId(),
				actionHistory.getCommunityUser().getCommunityUserId());
		assertEquals(questionAnswer.getQuestionAnswerId(), actionHistory.getQuestionAnswer().getQuestionAnswerId());
		assertEquals(questionAnswer.getProduct().isAdult(), actionHistory.isAdult());
		assertNotNull(actionHistory.getActionTime());
		assertNotNull(actionHistory.getRegisterDateTime());
		assertNotNull(actionHistory.getModifyDateTime());
	}


}
