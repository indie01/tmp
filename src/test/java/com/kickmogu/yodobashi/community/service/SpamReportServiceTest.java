package com.kickmogu.yodobashi.community.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.domain.SpamReportDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportGroupType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportTargetType;

/**
 * 違反報告サービスのテストクラスです。
 *
 * @author hirabayashi
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/jcclientContext.xml")
public class SpamReportServiceTest extends DataSetTest {

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
	 * お知らせ用のパスです。
	 */
	private Condition spamReportPath = 
			Path.includeProp("*").includePath(
					"communityUser.communityUserId,review.reviewId,question.questionId," +
					"questionAnswer.questionAnswerId,comment.commentId,imageHeader.imageId").depth(1);

	/**
	 * 違反報告を登録・検証します。(レビュー)
	 */
	@Test
	public void testCreateSpamReportByReview() {
		SpamReportDO spamReport = new SpamReportDO();
		spamReport.setCommunityUser(reportUser);
		spamReport.setReview(review);
		spamReport.setStatus(SpamReportStatus.NEW);
		spamReport.setSpamReportBody("レビュー違反報告です。");
		spamReport.setTargetType(SpamReportTargetType.REVIEW);
		SpamReportDO saveSpamReport = spamReportService.createSpamReport(spamReport);
		// 登録した違反報告を取得します。
		SpamReportDO spamReportByHBase = hBaseOperations.load(SpamReportDO.class,
				saveSpamReport.getSpamReportId(), spamReportPath);
		SpamReportDO spamReportBySolr = solrOperations.load(SpamReportDO.class,
				saveSpamReport.getSpamReportId(), spamReportPath);
		// 違反報告を検証します。
		checkSpamReport(spamReport, saveSpamReport);
		checkSpamReport(spamReport, spamReportByHBase);
		checkSpamReport(spamReport, spamReportBySolr);
	}
	
	/**
	 * 違反報告を登録・検証します。(質問)
	 */
	@Test
	public void testCreateSpamReportByQuestion() {
		SpamReportDO spamReport = new SpamReportDO();
		spamReport.setCommunityUser(reportUser);
		spamReport.setQuestion(question);
		spamReport.setStatus(SpamReportStatus.NEW);
		spamReport.setSpamReportBody("質問違反報告です。");
		spamReport.setTargetType(SpamReportTargetType.QUESTION);
		SpamReportDO saveSpamReport = spamReportService.createSpamReport(spamReport);
		// 登録した違反報告を取得します。
		SpamReportDO spamReportByHBase = hBaseOperations.load(SpamReportDO.class,
				saveSpamReport.getSpamReportId(), spamReportPath);
		SpamReportDO spamReportBySolr = solrOperations.load(SpamReportDO.class,
				saveSpamReport.getSpamReportId(), spamReportPath);
		// 違反報告を検証します。
		checkSpamReport(spamReport, saveSpamReport);
		checkSpamReport(spamReport, spamReportByHBase);
		checkSpamReport(spamReport, spamReportBySolr);
	}
	
	/**
	 * 違反報告を登録・検証します。(質問回答)
	 */
	@Test
	public void testCreateSpamReportByQuestionAnswer() {
		SpamReportDO spamReport = new SpamReportDO();
		spamReport.setCommunityUser(reportUser);
		spamReport.setQuestionAnswer(questionAnswer);
		spamReport.setStatus(SpamReportStatus.NEW);
		spamReport.setSpamReportBody("質問回答違反報告です。");
		spamReport.setTargetType(SpamReportTargetType.QUESTION_ANSWER);
		SpamReportDO saveSpamReport = spamReportService.createSpamReport(spamReport);
		// 登録した違反報告を取得します。
		SpamReportDO spamReportByHBase = hBaseOperations.load(SpamReportDO.class,
				saveSpamReport.getSpamReportId(), spamReportPath);
		SpamReportDO spamReportBySolr = solrOperations.load(SpamReportDO.class,
				saveSpamReport.getSpamReportId(), spamReportPath);
		// 違反報告を検証します。
		checkSpamReport(spamReport, saveSpamReport);
		checkSpamReport(spamReport, spamReportByHBase);
		checkSpamReport(spamReport, spamReportBySolr);
	}
	
	/**
	 * 違反報告を登録・検証します。(画像)
	 */
	@Test
	public void testCreateSpamReportByImage() {
		SpamReportDO spamReport = new SpamReportDO();
		spamReport.setCommunityUser(reportUser);
		spamReport.setImageHeader(communityUser.getImageHeader());
		spamReport.setStatus(SpamReportStatus.NEW);
		spamReport.setSpamReportBody("画像違反報告です。");
		spamReport.setTargetType(SpamReportTargetType.IMAGE);
		SpamReportDO saveSpamReport = spamReportService.createSpamReport(spamReport);
		// 登録した違反報告を取得します。
		SpamReportDO spamReportByHBase = hBaseOperations.load(SpamReportDO.class,
				saveSpamReport.getSpamReportId(), spamReportPath);
		SpamReportDO spamReportBySolr = solrOperations.load(SpamReportDO.class,
				saveSpamReport.getSpamReportId(), spamReportPath);
		// 違反報告を検証します。
		checkSpamReport(spamReport, saveSpamReport);
		checkSpamReport(spamReport, spamReportByHBase);
		checkSpamReport(spamReport, spamReportBySolr);
	}
	
	/**
	 * 違反報告を登録・検証します。(レビューコメント)
	 */
	@Test
	public void testCreateSpamReportByReviewComment() {
		SpamReportDO spamReport = new SpamReportDO();
		spamReport.setCommunityUser(reportUser);
		spamReport.setComment(reviewComment);
		spamReport.setStatus(SpamReportStatus.NEW);
		spamReport.setSpamReportBody("レビューコメント違反報告です。");
		spamReport.setTargetType(SpamReportTargetType.COMMENT);
		SpamReportDO saveSpamReport = spamReportService.createSpamReport(spamReport);
		// 登録した違反報告を取得します。
		SpamReportDO spamReportByHBase = hBaseOperations.load(SpamReportDO.class,
				saveSpamReport.getSpamReportId(), spamReportPath);
		SpamReportDO spamReportBySolr = solrOperations.load(SpamReportDO.class,
				saveSpamReport.getSpamReportId(), spamReportPath);
		// 違反報告を検証します。
		checkSpamReport(spamReport, saveSpamReport);
		checkSpamReport(spamReport, spamReportByHBase);
		checkSpamReport(spamReport, spamReportBySolr);
	}
	
	/**
	 * 違反報告を登録・検証します。(質問回答コメント)
	 */
	@Test
	public void testCreateSpamReportByQuestionAnswerComment() {
		SpamReportDO spamReport = new SpamReportDO();
		spamReport.setCommunityUser(reportUser);
		spamReport.setComment(questionAnswerComment);
		spamReport.setStatus(SpamReportStatus.NEW);
		spamReport.setSpamReportBody("質問回答コメント違反報告です。");
		spamReport.setTargetType(SpamReportTargetType.COMMENT);
		SpamReportDO saveSpamReport = spamReportService.createSpamReport(spamReport);
		// 登録した違反報告を取得します。
		SpamReportDO spamReportByHBase = hBaseOperations.load(SpamReportDO.class,
				saveSpamReport.getSpamReportId(), spamReportPath);
		SpamReportDO spamReportBySolr = solrOperations.load(SpamReportDO.class,
				saveSpamReport.getSpamReportId(), spamReportPath);
		// 違反報告を検証します。
		checkSpamReport(spamReport, saveSpamReport);
		checkSpamReport(spamReport, spamReportByHBase);
		checkSpamReport(spamReport, spamReportBySolr);
	}
	
	/**
	 * 違反報告を検証します。
	 */
	private void checkSpamReport(SpamReportDO spamReport, SpamReportDO saveSpamReport) {
		assertNotNull(saveSpamReport);
		assertNotNull(saveSpamReport.getSpamReportId());
		assertEquals(spamReport.getSpamReportBody(), saveSpamReport.getSpamReportBody());
		assertEquals(spamReport.getTargetType(), saveSpamReport.getTargetType());
		assertEquals(spamReport.getStatus(), saveSpamReport.getStatus());
		assertEquals(spamReport.getCommunityUser().getCommunityUserId(), 
				saveSpamReport.getCommunityUser().getCommunityUserId());
		if(SpamReportTargetType.REVIEW == saveSpamReport.getTargetType()) {
			assertEquals(SpamReportGroupType.REVIEW, saveSpamReport.getGroupType());
			assertEquals(spamReport.getReview().getReviewId(), 
					saveSpamReport.getReview().getReviewId());
			assertEquals(review.getCommunityUser().getCommunityUserId(), 
					saveSpamReport.getRelationReviewOwnerId());
		} else if(SpamReportTargetType.QUESTION == saveSpamReport.getTargetType()) {
			assertEquals(SpamReportGroupType.QUESTION, saveSpamReport.getGroupType());
			assertEquals(spamReport.getQuestion().getQuestionId(), 
					saveSpamReport.getQuestion().getQuestionId());
			assertEquals(question.getCommunityUser().getCommunityUserId(), 
					saveSpamReport.getRelationQuestionOwnerId());
		} else if(SpamReportTargetType.QUESTION_ANSWER == saveSpamReport.getTargetType()) {
			assertEquals(SpamReportGroupType.QUESTION, saveSpamReport.getGroupType());
			assertEquals(spamReport.getQuestion().getQuestionId(), 
					saveSpamReport.getQuestion().getQuestionId());
			assertEquals(spamReport.getQuestionAnswer().getQuestionAnswerId(), 
					saveSpamReport.getQuestionAnswer().getQuestionAnswerId());
			assertEquals(question.getCommunityUser().getCommunityUserId(), 
					saveSpamReport.getRelationQuestionOwnerId());
			assertEquals(questionAnswer.getCommunityUser().getCommunityUserId(), 
					saveSpamReport.getRelationQuestionAnswerOwnerId());
		} else if(SpamReportTargetType.IMAGE == saveSpamReport.getTargetType()) {
			assertEquals(SpamReportGroupType.IMAGE, saveSpamReport.getGroupType());
			assertNotNull(saveSpamReport.getImageHeader().getImageId());
			assertEquals(spamReport.getImageHeader().getImageId(), 
					saveSpamReport.getImageHeader().getImageId());
			assertEquals(communityUser.getCommunityUserId(), 
					saveSpamReport.getRelationImageOwnerId());
		} else if(SpamReportTargetType.COMMENT == saveSpamReport.getTargetType()) {
			assertNotNull(saveSpamReport.getGroupType());
			assertEquals(spamReport.getComment().getCommentId(), 
					saveSpamReport.getComment().getCommentId());
			assertEquals(commentUser.getCommunityUserId(), 
					saveSpamReport.getRelationCommentOwnerId());
			if(SpamReportGroupType.REVIEW == saveSpamReport.getGroupType()) {
				assertEquals(review.getCommunityUser().getCommunityUserId(), 
						saveSpamReport.getRelationReviewOwnerId());
			} else if(SpamReportGroupType.QUESTION == saveSpamReport.getGroupType()) {
				assertEquals(question.getCommunityUser().getCommunityUserId(), 
						saveSpamReport.getRelationQuestionOwnerId());
				assertEquals(questionAnswer.getCommunityUser().getCommunityUserId(), 
						saveSpamReport.getRelationQuestionAnswerOwnerId());
			}
		}
		assertNotNull(saveSpamReport.getReportDate());
		if(saveSpamReport.getStatus() == SpamReportStatus.RESOLVED) {
			assertNotNull(saveSpamReport.getResolvedDate());
			assertEquals(null, saveSpamReport.getDeleteDate());
		} else if(saveSpamReport.getStatus() == SpamReportStatus.DELETE) {
			assertNotNull(saveSpamReport.getDeleteDate());
		} else {
			assertEquals(null, saveSpamReport.getResolvedDate());
			assertEquals(null, saveSpamReport.getDeleteDate());
		}
		if(CommunityUserStatus.ACTIVE == reportUser.getStatus()) {
			assertEquals(false, saveSpamReport.isWithdraw());
			assertEquals(null, saveSpamReport.getWithdrawKey());
		} else {
			assertTrue(saveSpamReport.isWithdraw());
			assertEquals(reportUser.getWithdrawKey(), saveSpamReport.getWithdrawKey());
		}
	}
}
