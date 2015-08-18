/**
 *
 */
package com.kickmogu.yodobashi.community.service;

import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.dao.ProductMasterDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.impl.dummy.DummySendMailDaoImpl;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.EventHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.MailInfoDO;
import com.kickmogu.yodobashi.community.resource.domain.MailSettingDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.VersionDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.AlsoBuyProduct;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommentTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSendTiming;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSettingType;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ProductSatisfaction;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewType;

/**
 * メール投稿のテストです。
 * @author kamiike
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/jcclientContext.xml")
public class MailTest extends BaseTest {

	/**
	 * 商品マスター DAO です。
	 */
	@Autowired
	protected ProductMasterDao productMasterDao;

	/**
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	protected RequestScopeDao requestScopeDao;

	/**
	 * 商品サービスです。
	 */
	@Autowired
	protected ProductService productService;

	/**
	 * 質問サービスです。
	 */
	@Autowired
	protected QuestionService questionService;

	/**
	 * レビューサービスです。
	 */
	@Autowired
	protected ReviewService reviewService;

	/**
	 * コメントサービスです。
	 */
	@Autowired
	protected CommentService commentService;


	/**
	 * フォローサービスです。
	 */
	@Autowired
	protected FollowService followService;

	/**
	 * バッチメールサービスです。
	 */
	@Autowired
	protected BatchMailService batchMailService;

	/**
	 * ランダムインスタンスです。
	 */
	private static Random random;

	static {
	}

	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() {
		System.setProperty("user.name", "sugizon");
		initialize();
		hBaseOperations.deleteAll(EventHistoryDO.class);
		hBaseOperations.deleteAll(VersionDO.class);
		hBaseOperations.deleteAll(ProductMasterDO.class);
		solrOperations.deleteAll(ProductMasterDO.class);
		DummySendMailDaoImpl.popMailInfoAndDelete();

		try {
			random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		} catch (Exception e) {
			random = new Random();
		}

	}

	private static final String exportPath="C:\\Users\\sugimoto\\Desktop\\htdocs\\mail";

	private void exportMail(MailInfoDO mailInfo){
		long currentTime = System.currentTimeMillis();
		
	    try{
	    	File file = new File(exportPath + "\\" + mailInfo.getMailType().getTemplateName() + "_" + currentTime +"_pc.html");
	        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
	        pw.println(mailInfo.getHtmlBody().replaceAll("@@BR@@", "\n"));
	        pw.close();
	    }catch(IOException e){
	    }

	    try{
	    	File file = new File(exportPath + "\\" + mailInfo.getMailType().getTemplateName()  + "_" + currentTime + "_mb.txt");
	        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
	        pw.println(mailInfo.getTitle());
	        pw.println("-----------------------------------------------------------------");
	        pw.println(mailInfo.getTextBody().replaceAll("@@BR@@", "\n"));
	        pw.close();
	    }catch(IOException e){
	    }
	}



	/**
	 * ユーザー登録のメールテストです。
	 */
	@Test
	public void testUserRegister() {
		//CommunityUserDO communityUser = createCommunityUser("テストさん", false);

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
//		assertEquals(1, mails.size());
		MailInfoDO mailInfo = mails.get(0);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("ようこそ") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("テストさん") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("テストさん") != -1);

		exportMail(mailInfo);
	}

	/**
	 * レビュー期限 5 日前のメールテストです。
	 */
	@Test
	public void testReviewLimitNotifyForFiveDaysAgo() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);

		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.REVIEW_LIMIT);
		mailSetting.setMailSettingValue(MailSendTiming.FIVE_DAYS_AGO);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		ProductDO product1 = productService.getProductBySku("100000001001390279").getProduct();
		ProductDO product2 = productService.getProductBySku("100000001000410414").getProduct();
		ProductDO product3 = productService.getProductBySku("100000001000971125").getProduct();

		Date salesDate = DateUtils.addDays(new Date(), 0 - (product1.getRvwInitPostTerm() - 5 - 1));
		// 注文履歴を登録します。
		createReceipt(communityUser, product1.getJan(), 
				salesDate);
		salesDate = DateUtils.addDays(new Date(), 0 - (product2.getRvwInitPostTerm() - 5 - 1));
		// 注文履歴を登録します。
		createReceipt(communityUser, product2.getJan(), 
				salesDate);
		salesDate = DateUtils.addDays(new Date(), 0 - (product3.getRvwInitPostTerm() - 5 - 1));
		// 注文履歴を登録します。
		createReceipt(communityUser, product3.getJan(), 
				salesDate);

//		assertTrue(reviewService.canPostReview(communityUser.getCommunityUserId(
//				), product1.getSku(), ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE));
//		assertTrue(reviewService.canPostReview(communityUser.getCommunityUserId(
//				), product2.getSku(), ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE));
//		assertTrue(reviewService.canPostReview(communityUser.getCommunityUserId(
//				), product3.getSku(), ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE));

		DummySendMailDaoImpl.popMailInfoAndDelete();
		batchMailService.sendMail(communityUser, new Date());
		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
		boolean sendMail = false;
		for (MailInfoDO target : mails) {
			if (target.getMailType().equals(MailType.REVIEW_LIMIT_NOTIFY_FOR_FIVE_DAYS_AGO)) {
				exportMail(target);
				if( !sendMail )
					sendMail = true;
			}
		}
		assertTrue(sendMail);

	}

	/**
	 * レビュー期限 10 日前のメールテストです。
	 */
	@Test
	public void testReviewLimitNotifyForTenDaysAgo() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);

		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.REVIEW_LIMIT);
		mailSetting.setMailSettingValue(MailSendTiming.TEN_DAYS_AGO);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		ProductDO product1 = productService.getProductBySku("100000001001390279").getProduct();
		ProductDO product2 = productService.getProductBySku("100000001000410414").getProduct();
		ProductDO product3 = productService.getProductBySku("100000001000971125").getProduct();

		Date salesDate = DateUtils.addDays(new Date(), 0 - (product1.getRvwInitPostTerm() + (product1.getRvwCntnPostTerm() * 1) - 10 - 1));
		// 注文履歴を登録します。
		createReceipt(communityUser, product1.getJan(), 
				salesDate);
		salesDate = DateUtils.addDays(new Date(), 0 - (product2.getRvwInitPostTerm() + (product2.getRvwCntnPostTerm() * 1) - 10 - 1));
		// 注文履歴を登録します。
		createReceipt(communityUser, product2.getJan(), 
				salesDate);
		salesDate = DateUtils.addDays(new Date(), 0 - (product3.getRvwInitPostTerm() + (product3.getRvwCntnPostTerm() * 1) - 10 - 1));
		// 注文履歴を登録します。
		createReceipt(communityUser, product3.getJan(), 
				salesDate);

//		assertTrue(reviewService.canPostReview(communityUser.getCommunityUserId(
//				), product1.getSku(), ReviewType.REVIEW_AFTER_FEW_DAYS));
//		assertTrue(reviewService.canPostReview(communityUser.getCommunityUserId(
//				), product2.getSku(), ReviewType.REVIEW_AFTER_FEW_DAYS));
//		assertTrue(reviewService.canPostReview(communityUser.getCommunityUserId(
//				), product3.getSku(), ReviewType.REVIEW_AFTER_FEW_DAYS));

		DummySendMailDaoImpl.popMailInfoAndDelete();
		batchMailService.sendMail(communityUser, new Date());
		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
		boolean sendMail = false;
		for (MailInfoDO target : mails) {
			if (target.getMailType().equals(MailType.REVIEW_LIMIT_NOTIFY_FOR_TEN_DAYS_AGO)) {
				exportMail(target);
				if( !sendMail )
					sendMail = true;
			}
		}
		assertTrue(sendMail);
	}

	/**
	 * 購入商品の新着QA質問のメールテストです。
	 */
	@Test
	public void testNewQuestionForPurchaseProduct() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO questionOwner = createCommunityUser("質問者", false);

		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.PURCHASE_PRODUCT_QUESTION);
		mailSetting.setMailSettingValue(MailSendTiming.EVERYTIME_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);


		product = productService.getProductBySku("200000002000012355").getProduct();
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser, receiptJanCode, 
				salesDate);

		QuestionDO question = new QuestionDO();
		question.setCommunityUser(questionOwner);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文");

		DummySendMailDaoImpl.popMailInfoAndDelete();
		//質問を投稿で登録します。
		questionService.saveQuestion(question);

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
//		assertEquals(1, mails.size());
		MailInfoDO mailInfo = mails.get(0);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("購入した商品に質問") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("テストさんが購入した") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(questionOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("質問本文") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("テストさんが購入した") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(questionOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("質問本文") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(product.getProductName()) != -1);

		exportMail(mailInfo);
	}

	/**
	 * 購入商品の新着QA質問のメールテストです。
	 */
	@Test
	public void testNewQuestionForPurchaseProductSummary() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO questionOwner = createCommunityUser("質問者", false);

		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.PURCHASE_PRODUCT_QUESTION);
		mailSetting.setMailSettingValue(MailSendTiming.DAILY_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);


		product = productService.getProductBySku("200000002000012355").getProduct();
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser, receiptJanCode, 
				salesDate);

		QuestionDO question = new QuestionDO();
		question.setCommunityUser(questionOwner);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文");

		//質問を投稿で登録します。
		questionService.saveQuestion(question);

		question = new QuestionDO();
		question.setCommunityUser(questionOwner);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文２");

		//質問を投稿で登録します。
		questionService.saveQuestion(question);


		question = new QuestionDO();
		question.setCommunityUser(questionOwner);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３質問本文３");

		//質問を投稿で登録します。
		questionService.saveQuestion(question);

		DummySendMailDaoImpl.popMailInfoAndDelete();
		batchMailService.sendMail(communityUser, new Date());

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
		MailInfoDO mailInfo = null;
		for (MailInfoDO target : mails) {
			if (target.getMailType().equals(MailType.NEW_QUESTION_FOR_PURCHASE_PRODUCT_SUMMARY)) {
				mailInfo = target;
			}
		}
//		assertNotNull(mailInfo);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("購入した商品に質問") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(communityUser.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(questionOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("質問本文") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(communityUser.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(questionOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("質問本文") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(product.getProductName()) != -1);

		exportMail(mailInfo);
	}

	/**
	 * レビューへのコメントのメールテストです。
	 */
	@Test
	public void testReviewComment() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO commentOwner = createCommunityUser("コメント投稿者", false);

		product = productService.getProductBySku("200000002000012355").getProduct();
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser, receiptJanCode, 
				salesDate);

		ReviewDO review = new ReviewDO();
		review.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
		review.setStatus(ContentsStatus.SUBMITTED);
		review.setCommunityUser(communityUser);
		review.setProduct(product);
		review.setReviewBody("レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文");
		review.setAlsoBuyProduct(AlsoBuyProduct.MAYNOTBUY);
		review.setProductSatisfaction(ProductSatisfaction.FOUR);
		reviewService.saveReview(review);

		DummySendMailDaoImpl.popMailInfoAndDelete();

		CommentDO comment = new CommentDO();
		comment.setTargetType(CommentTargetType.REVIEW);
		comment.setCommentBody("テストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメント");
		comment.setReview(review);
		comment.setCommunityUser(commentOwner);
		commentService.saveComment(comment);

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
//		assertEquals(1, mails.size());
		MailInfoDO mailInfo = mails.get(0);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("レビューにコメント") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("テストさんが投稿した") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(commentOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("テストさんが投稿した") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(commentOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(product.getProductName()) != -1);

		exportMail(mailInfo);
	}

	/**
	 * レビューへのコメントのメールテストです。
	 */
	@Test
	public void testReviewCommentSummary() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO commentOwner = createCommunityUser("コメント投稿者", false);

		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.REVIEW_COMMENT);
		mailSetting.setMailSettingValue(MailSendTiming.DAILY_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);


		product = productService.getProductBySku("200000002000012355").getProduct();
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser, receiptJanCode, 
				salesDate);

		ReviewDO review = new ReviewDO();
		review.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
		review.setStatus(ContentsStatus.SUBMITTED);
		review.setCommunityUser(communityUser);
		review.setProduct(product);
		review.setReviewBody("レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文");
		review.setAlsoBuyProduct(AlsoBuyProduct.MAYNOTBUY);
		review.setProductSatisfaction(ProductSatisfaction.FOUR);
		reviewService.saveReview(review);

		CommentDO comment = new CommentDO();
		comment.setTargetType(CommentTargetType.REVIEW);
		comment.setCommentBody("テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1テストコメント1");
		comment.setReview(review);
		comment.setCommunityUser(commentOwner);
		commentService.saveComment(comment);

		comment = new CommentDO();
		comment.setTargetType(CommentTargetType.REVIEW);
		comment.setCommentBody("テストコメント2");
		comment.setReview(review);
		comment.setCommunityUser(commentOwner);
		commentService.saveComment(comment);

		comment = new CommentDO();
		comment.setTargetType(CommentTargetType.REVIEW);
		comment.setCommentBody("テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3");
		comment.setReview(review);
		comment.setCommunityUser(commentOwner);
		commentService.saveComment(comment);

		DummySendMailDaoImpl.popMailInfoAndDelete();
		batchMailService.sendMail(communityUser, new Date());

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
		MailInfoDO mailInfo = null;
		for (MailInfoDO target : mails) {
			if (target.getMailType().equals(MailType.REVIEW_COMMENT_SUMMARY)) {
				mailInfo = target;
			}
		}
//		assertNotNull(mailInfo);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("レビューにコメント") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("テストさんのレビューにコメント") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("テストコメント") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("テストさんのレビューにコメント") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("テストコメント") != -1);
		exportMail(mailInfo);
	}

	/**
	 * 質問回答へのコメントのメールテストです。
	 */
	@Test
	public void testQuestionAnswerComment() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO questionOwner = createCommunityUser("質問者", false);

		product = productService.getProductBySku("200000002000012355").getProduct();
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser, receiptJanCode, 
				salesDate);

		QuestionDO question = new QuestionDO();
		question.setCommunityUser(questionOwner);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文<br/><div>質問本文</div>質問本文<br/><div>質問本文</div>質問本文<br/><div>質問本文</div>質問本文<br/><div>質問本文</div>質問本文<br/><div>質問本文</div>質問本文<br/><div>質問本文</div>質問本文<br/><div>質問本文</div>質問本文<br/><div>質問本文</div>質問本文<br/><div>質問本文</div>質問本文<br/><div>質問本文</div>質問本文<br/><div>質問本文</div>質問本文<br/><div>質問本文</div>質問本文<br/><div>質問本文</div>質問本文<br/><div>質問本文</div>");

		//質問を投稿で登録します。
		questionService.saveQuestion(question);

		QuestionAnswerDO questionAnswer = new QuestionAnswerDO();
		questionAnswer.setAnswerBody("質問回答<br/><div>質問回答</div>質問回答<br/><div>質問回答</div>質問回答<br/><div>質問回答</div>質問回答<br/><div>質問回答</div>質問回答<br/><div>質問回答</div>質問回答<br/><div>質問回答</div>質問回答<br/><div>質問回答</div>質問回答<br/><div>質問回答</div>質問回答<br/><div>質問回答</div>質問回答<br/><div>質問回答</div>質問回答<br/><div>質問回答</div>質問回答<br/><div>質問回答</div>質問回答<br/><div>質問回答</div>質問回答<br/><div>質問回答</div>質問回答<br/><div>質問回答</div>質問回答<br/><div>質問回答</div>質問回答<br/><div>質問回答</div>質問回答<br/><div>質問回答</div>質問回答<br/><div>質問回答</div>質問回答<br/><div>質問回答</div>質問回答<br/><div>質問回答</div>質問回答<br/><div>質問回答</div>質問回答<br/><div>質問回答</div>質問回答<br/><div>質問回答</div>質問回答<br/><div>質問回答</div>");
		questionAnswer.setCommunityUser(communityUser);
		questionAnswer.setQuestion(question);
		questionAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionService.saveQuestionAnswer(questionAnswer);

		DummySendMailDaoImpl.popMailInfoAndDelete();

		CommentDO comment = new CommentDO();
		comment.setTargetType(CommentTargetType.QUESTION_ANSWER);
		comment.setCommentBody("テストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメント");
		comment.setQuestionAnswer(questionAnswer);
		comment.setCommunityUser(questionOwner);
		commentService.saveComment(comment);

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
//		assertEquals(1, mails.size());
		MailInfoDO mailInfo = mails.get(0);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("回答にコメント") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("テストさんが投稿した") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(questionOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("質問本文") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("テストさんが投稿した") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(questionOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("質問本文") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(product.getProductName()) != -1);

		exportMail(mailInfo);
	}

	/**
	 * 質問回答へのコメントのメールテストです。
	 */
	@Test
	public void testQuestionAnswerCommentSummary() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO questionOwner = createCommunityUser("質問者", false);

		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.ANSWER_COMMENT);
		mailSetting.setMailSettingValue(MailSendTiming.DAILY_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		product = productService.getProductBySku("200000002000012355").getProduct();
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser, receiptJanCode, 
				salesDate);

		QuestionDO question = new QuestionDO();
		question.setCommunityUser(questionOwner);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文");

		//質問を投稿で登録します。
		questionService.saveQuestion(question);

		QuestionAnswerDO questionAnswer = new QuestionAnswerDO();
		questionAnswer.setAnswerBody("質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答");
		questionAnswer.setCommunityUser(communityUser);
		questionAnswer.setQuestion(question);
		questionAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionService.saveQuestionAnswer(questionAnswer);

		CommentDO comment = new CommentDO();
		comment.setTargetType(CommentTargetType.QUESTION_ANSWER);
		comment.setCommentBody("テストコメント");
		comment.setQuestionAnswer(questionAnswer);
		comment.setCommunityUser(questionOwner);
		commentService.saveComment(comment);

		comment = new CommentDO();
		comment.setTargetType(CommentTargetType.QUESTION_ANSWER);
		comment.setCommentBody("テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2");
		comment.setQuestionAnswer(questionAnswer);
		comment.setCommunityUser(questionOwner);
		commentService.saveComment(comment);


		comment = new CommentDO();
		comment.setTargetType(CommentTargetType.QUESTION_ANSWER);
		comment.setCommentBody("テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3");
		comment.setQuestionAnswer(questionAnswer);
		comment.setCommunityUser(questionOwner);
		commentService.saveComment(comment);

		DummySendMailDaoImpl.popMailInfoAndDelete();
		batchMailService.sendMail(communityUser, new Date());

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
		MailInfoDO mailInfo = null;
		for (MailInfoDO target : mails) {
			if (target.getMailType().equals(MailType.QUESTION_ANSWER_COMMENT_SUMMARY)) {
				mailInfo = target;
			}
		}
//		assertNotNull(mailInfo);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("回答にコメント") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("さんの回答にコメント") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(questionOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("質問回答") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("さんの回答にコメント") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(questionOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("質問回答") != -1);

		exportMail(mailInfo);
	}

	/**
	 * 画像へのコメントのメールテストです。
	 */
	@Test
	public void testImageComment() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO commentOwner = createCommunityUser("コメント投稿者", false);

		product = productService.getProductBySku("200000002000012355").getProduct();
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser, receiptJanCode, 
				salesDate);

		List<ImageHeaderDO> imageHeaders = new ArrayList<ImageHeaderDO>();
		for (int i = 0; i < 4; i++) {
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
		}

		requestScopeDao.initialize(communityUser, null);
		imageService.saveImageSet(product.getSku(), imageHeaders, null);

		DummySendMailDaoImpl.popMailInfoAndDelete();

		CommentDO comment = new CommentDO();
		comment.setTargetType(CommentTargetType.IMAGE);
		comment.setCommentBody(" テストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメントテストコメント");
		comment.setImageHeader(imageHeaders.get(0));
		comment.setCommunityUser(commentOwner);
		commentService.saveComment(comment);

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
//		assertEquals(1, mails.size());
		MailInfoDO mailInfo = mails.get(0);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("画像にコメント") != -1);
//		assertEquals(true, mailInfo.getTitle().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("テストさんの投稿画像に") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(commentOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("テストコメント") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("テストさんの投稿画像に") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(commentOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("テストコメント") != -1);

		exportMail(mailInfo);

	}

	/**
	 * 画像へのコメントのメールテストです。
	 */
	@Test
	public void testImageCommentSummary() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO commentOwner = createCommunityUser("コメント投稿者", false);

		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.IMAGE_COMMENT);
		mailSetting.setMailSettingValue(MailSendTiming.DAILY_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		product = productService.getProductBySku("200000002000012355").getProduct();
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser, receiptJanCode, 
				salesDate);

		List<ImageHeaderDO> imageHeaders = new ArrayList<ImageHeaderDO>();
		for (int i = 0; i < 4; i++) {
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
		}

		requestScopeDao.initialize(communityUser, null);
		imageService.saveImageSet(product.getSku(), imageHeaders, null);

		CommentDO comment = new CommentDO();
		comment.setTargetType(CommentTargetType.IMAGE);
		comment.setCommentBody("テストコメント");
		comment.setImageHeader(imageHeaders.get(0));
		comment.setCommunityUser(commentOwner);
		commentService.saveComment(comment);

		comment = new CommentDO();
		comment.setTargetType(CommentTargetType.IMAGE);
		comment.setCommentBody("テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2テストコメント2");
		comment.setImageHeader(imageHeaders.get(0));
		comment.setCommunityUser(commentOwner);
		commentService.saveComment(comment);

		comment = new CommentDO();
		comment.setTargetType(CommentTargetType.IMAGE);
		comment.setCommentBody("テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3テストコメント3");
		comment.setImageHeader(imageHeaders.get(0));
		comment.setCommunityUser(commentOwner);
		commentService.saveComment(comment);

		DummySendMailDaoImpl.popMailInfoAndDelete();
		batchMailService.sendMail(communityUser, new Date());

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
		MailInfoDO mailInfo = null;
		for (MailInfoDO target : mails) {
			if (target.getMailType().equals(MailType.IMAGE_COMMENT_SUMMARY)) {
				mailInfo = target;
			}
		}
//		assertNotNull(mailInfo);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("画像にコメント") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("テストさんの投稿画像に") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(commentOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("テストコメント") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("テストさんの投稿画像に") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(commentOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("テストコメント") != -1);

		exportMail(mailInfo);
	}

	/**
	 * 質問への回答のメールテストです。
	 */
	@Test
	public void testQuestionAnswer() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO questionOwner = createCommunityUser("質問者", false);

		product = productService.getProductBySku("100000001001390279").getProduct();
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser, product.getJan(), 
				salesDate);

		QuestionDO question = new QuestionDO();
		question.setCommunityUser(questionOwner);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("&nbsp;d d d d d&nbsp;");

		//質問を投稿で登録します。
		questionService.saveQuestion(question);

		DummySendMailDaoImpl.popMailInfoAndDelete();

		QuestionAnswerDO questionAnswer = new QuestionAnswerDO();
		questionAnswer.setAnswerBody("&nbsp;d d d d d&nbsp;");
		questionAnswer.setCommunityUser(communityUser);
		questionAnswer.setQuestion(question);
		questionAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionService.saveQuestionAnswer(questionAnswer);

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
//		assertEquals(1, mails.size());
//		MailInfoDO mailInfo = mails.get(0);
//		assertEquals(questionOwner.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("質問に回答") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("質問者さんが投稿した") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(communityUser.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(questionOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("質問本文") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("質問者さんが投稿した") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(communityUser.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(questionOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("質問本文") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(product.getProductName()) != -1);

		for (MailInfoDO target : mails) {
			if (target.getMailType().equals(MailType.QUESTION_ANSWER)) {
				exportMail(target);
			}
		}
		
		
	}

	/**
	 * 質問への回答のメールテストです。
	 */
	@Test
	public void testQuestionAnswerSummary() {
		CommunityUserDO communityUser1 = createCommunityUser("テスト1", false);
		CommunityUserDO communityUser2 = createCommunityUser("テスト2", false);
		CommunityUserDO communityUser3 = createCommunityUser("テスト3", false);
		CommunityUserDO questionOwner = createCommunityUser("質問者", false);

		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(questionOwner.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.QUESTION_ANSWER);
		mailSetting.setMailSettingValue(MailSendTiming.DAILY_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		product = productService.getProductBySku("200000002000012355").getProduct();
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser1, receiptJanCode, 
				salesDate);
		createReceipt(communityUser2, receiptJanCode, 
				salesDate);
		createReceipt(communityUser3, receiptJanCode, 
				salesDate);
		QuestionDO question = new QuestionDO();
		question.setCommunityUser(questionOwner);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文");

		//質問を投稿で登録します。
		questionService.saveQuestion(question);

		QuestionAnswerDO questionAnswer = new QuestionAnswerDO();
		questionAnswer.setAnswerBody("質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答");
		questionAnswer.setCommunityUser(communityUser1);
		questionAnswer.setQuestion(question);
		questionAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionService.saveQuestionAnswer(questionAnswer);

		questionAnswer = new QuestionAnswerDO();
		questionAnswer.setAnswerBody("質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答");
		questionAnswer.setCommunityUser(communityUser2);
		questionAnswer.setQuestion(question);
		questionAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionService.saveQuestionAnswer(questionAnswer);

		questionAnswer = new QuestionAnswerDO();
		questionAnswer.setAnswerBody("質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答");
		questionAnswer.setCommunityUser(communityUser3);
		questionAnswer.setQuestion(question);
		questionAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionService.saveQuestionAnswer(questionAnswer);

		DummySendMailDaoImpl.popMailInfoAndDelete();
		batchMailService.sendMail(questionOwner, new Date());

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
		MailInfoDO mailInfo = null;
		for (MailInfoDO target : mails) {
			if (target.getMailType().equals(MailType.QUESTION_ANSWER_SUMMARY)) {
				mailInfo = target;
			}
		}
//		assertNotNull(mailInfo);
//		assertEquals(questionOwner.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("質問に回答") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("質問者さんの質問に回答") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(questionOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("質問本文") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("質問回答") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("質問者さんの質問に回答") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(questionOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("質問本文") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("質問回答") != -1);

		exportMail(mailInfo);
	}

	/**
	 * 他ユーザからのフォローのメールテストです。
	 */
	@Test
	public void testUserFollow() {
		CommunityUserDO follower = createCommunityUser("フォロワー", false);
		CommunityUserDO followUser = createCommunityUser("フォローユーザー", false);

		createProductMasters(follower);

		DummySendMailDaoImpl.popMailInfoAndDelete();
		followService.followCommunityUser(
				follower.getCommunityUserId(), followUser.getCommunityUserId(), false);

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
//		assertEquals(1, mails.size());
		MailInfoDO mailInfo = mails.get(0);
//		assertEquals(followUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("フォローされました") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(
//				"フォロワーさんがフォローユーザーさんをフォローしました") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(follower.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(followUser.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(
//		"フォロワーさんがフォローユーザーさんをフォローしました") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(follower.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(followUser.getCommunityName()) != -1);

		exportMail(mailInfo);
	}

	/**
	 * 他ユーザからのフォローのメールテストです。
	 */
	@Test
	public void testUserFollowSummary() {
		CommunityUserDO follower = createCommunityUser("フォロワー1", false);
		CommunityUserDO follower2 = createCommunityUser("フォロワー2", false);
		CommunityUserDO follower3 = createCommunityUser("フォロワー3", false);
		CommunityUserDO followUser = createCommunityUser("フォローユーザー", false);

		createProductMasters(follower);
		createProductMasters(follower2);
		createProductMasters(follower3);
		
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(follower, product.getJan(), 
				salesDate);
		
		ReviewDO review = new ReviewDO();
		review.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
		review.setStatus(ContentsStatus.SUBMITTED);
		review.setCommunityUser(follower);
		review.setProduct(product);
		review.setReviewBody(" レビュー本文レビュー本文");
		review.setAlsoBuyProduct(AlsoBuyProduct.MAYNOTBUY);
		review.setProductSatisfaction(ProductSatisfaction.FOUR);
		reviewService.saveReview(review);

		QuestionDO question = new QuestionDO();
		question.setCommunityUser(follower);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody(" 質問本文質問");
		questionService.saveQuestion(question);

		
		question = new QuestionDO();
		question.setCommunityUser(follower2);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody(" 質問本文質問本");
		questionService.saveQuestion(question);
		
		QuestionAnswerDO questionAnswer = new QuestionAnswerDO();
		questionAnswer.setAnswerBody(" 質問回答<br/>");
		questionAnswer.setCommunityUser(follower);
		questionAnswer.setQuestion(question);
		questionAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionService.saveQuestionAnswer(questionAnswer);

		
		
		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(followUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.USER_FOLLOW);
		mailSetting.setMailSettingValue(MailSendTiming.DAILY_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		followService.followCommunityUser(
				follower.getCommunityUserId(), followUser.getCommunityUserId(), false);

		followService.followCommunityUser(
				follower.getCommunityUserId(), follower2.getCommunityUserId(), false);
		followService.followCommunityUser(
				follower.getCommunityUserId(), follower3.getCommunityUserId(), false);
		followService.followCommunityUser(
				follower2.getCommunityUserId(), followUser.getCommunityUserId(), false);
		followService.followCommunityUser(
				follower3.getCommunityUserId(), followUser.getCommunityUserId(), false);

		DummySendMailDaoImpl.popMailInfoAndDelete();
		batchMailService.sendMail(followUser, new Date());

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
		MailInfoDO mailInfo = null;
		for (MailInfoDO target : mails) {
			if (target.getMailType().equals(MailType.USER_FOLLOW_SUMMARY)) {
				mailInfo = target;
			}
		}
//		assertNotNull(mailInfo);
//		assertEquals(followUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("フォローされました") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(
//				"フォローされました") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(follower.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(
//				"フォローされました") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(follower.getCommunityName()) != -1);

		exportMail(mailInfo);
	}

	/**
	 * 回答QAへの別回答のメールテストです。
	 */
	@Test
	public void testAnotherQuestionAnswer() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.ANSWER_QUESTION_ANOTHER_ANSWER);
		mailSetting.setMailSettingValue(MailSendTiming.EVERYTIME_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);
		CommunityUserDO questionOwner = createCommunityUser("質問者", false);
		CommunityUserDO anotherAnswerer = createCommunityUser("別の回答者", false);

		product = productService.getProductBySku("200000002000012355").getProduct();
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser, receiptJanCode, 
				salesDate);
		createReceipt(anotherAnswerer, receiptJanCode, 
				salesDate);

		QuestionDO question = new QuestionDO();
		question.setCommunityUser(questionOwner);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文");

		//質問を投稿で登録します。
		questionService.saveQuestion(question);

		QuestionAnswerDO questionAnswer = new QuestionAnswerDO();
		questionAnswer.setAnswerBody("質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答");
		questionAnswer.setCommunityUser(communityUser);
		questionAnswer.setQuestion(question);
		questionAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionService.saveQuestionAnswer(questionAnswer);

		DummySendMailDaoImpl.popMailInfoAndDelete();

		QuestionAnswerDO anotherAnswer = new QuestionAnswerDO();
		anotherAnswer.setAnswerBody("別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答");
		anotherAnswer.setCommunityUser(anotherAnswerer);
		anotherAnswer.setQuestion(question);
		anotherAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionService.saveQuestionAnswer(anotherAnswer);

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
//		assertEquals(2, mails.size());
		MailInfoDO mailInfo = null;
		for (MailInfoDO target : mails) {
			if (target.getCommunityUser().getCommunityUserId(
					).equals(communityUser.getCommunityUserId())) {
				mailInfo = target;
			}
		}
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("別の回答が投稿されました") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("テストさんが回答した") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(communityUser.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(anotherAnswerer.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(questionOwner.getCommunityName()) == -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("質問本文") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("テストさんが回答した") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(communityUser.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(anotherAnswerer.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(questionOwner.getCommunityName()) == -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("質問本文") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(product.getProductName()) != -1);

		exportMail(mailInfo);
	}

	/**
	 * 回答QAへの別回答のメールテストです。
	 */
	@Test
	public void testAnotherQuestionAnswerSummary() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.ANSWER_QUESTION_ANOTHER_ANSWER);
		mailSetting.setMailSettingValue(MailSendTiming.DAILY_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);
		CommunityUserDO questionOwner = createCommunityUser("質問者", false);
		CommunityUserDO anotherAnswerer1 = createCommunityUser("別の回答者1", false);
		CommunityUserDO anotherAnswerer2 = createCommunityUser("別の回答者2", false);
		CommunityUserDO anotherAnswerer3 = createCommunityUser("別の回答者3", false);

		product = productService.getProductBySku("200000002000012355").getProduct();
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser, receiptJanCode, 
				salesDate);
		createReceipt(anotherAnswerer1, receiptJanCode, 
				salesDate);
		createReceipt(anotherAnswerer2, receiptJanCode, 
				salesDate);
		createReceipt(anotherAnswerer3, receiptJanCode, 
				salesDate);

		QuestionDO question = new QuestionDO();
		question.setCommunityUser(questionOwner);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文");

		//質問を投稿で登録します。
		questionService.saveQuestion(question);

		QuestionAnswerDO questionAnswer = new QuestionAnswerDO();
		questionAnswer.setAnswerBody("質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答");
		questionAnswer.setCommunityUser(communityUser);
		questionAnswer.setQuestion(question);
		questionAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionService.saveQuestionAnswer(questionAnswer);

		QuestionAnswerDO anotherAnswer = new QuestionAnswerDO();
		anotherAnswer.setAnswerBody("別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答");
		anotherAnswer.setCommunityUser(anotherAnswerer1);
		anotherAnswer.setQuestion(question);
		anotherAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionService.saveQuestionAnswer(anotherAnswer);

		anotherAnswer = new QuestionAnswerDO();
		anotherAnswer.setAnswerBody("別の回答2別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答");
		anotherAnswer.setCommunityUser(anotherAnswerer2);
		anotherAnswer.setQuestion(question);
		anotherAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionService.saveQuestionAnswer(anotherAnswer);

		anotherAnswer = new QuestionAnswerDO();
		anotherAnswer.setAnswerBody("別の回答3別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答別の回答");
		anotherAnswer.setCommunityUser(anotherAnswerer3);
		anotherAnswer.setQuestion(question);
		anotherAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionService.saveQuestionAnswer(anotherAnswer);

		DummySendMailDaoImpl.popMailInfoAndDelete();
		batchMailService.sendMail(communityUser, new Date());

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
		MailInfoDO mailInfo = null;
		for (MailInfoDO target : mails) {
			if (target.getMailType().equals(MailType.ANOTHER_QUESTION_ANSWER_SUMMARY)) {
				mailInfo = target;
			}
		}
//		assertNotNull(mailInfo);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("別の回答が投稿されました") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("テストさんが回答した") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(communityUser.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(anotherAnswerer1.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(questionOwner.getCommunityName()) == -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("質問本文") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("テストさんが回答した") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(communityUser.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(anotherAnswerer1.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(questionOwner.getCommunityName()) == -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("質問本文") != -1);

		exportMail(mailInfo);
	}

	/**
	 * レビュー商品への別レビューのメールテストです。
	 */
	@Test
	public void testAnotherReview() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO otherReviewr = createCommunityUser("別の投稿者", false);
		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.REVIEW_PRODUCT_ANOTHER_REVIEW);
		mailSetting.setMailSettingValue(MailSendTiming.EVERYTIME_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		product = productService.getProductBySku("200000002000012355").getProduct();
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser, receiptJanCode, 
				salesDate);
		createReceipt(otherReviewr, receiptJanCode, 
				salesDate);

		ReviewDO review = new ReviewDO();
		review.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
		review.setStatus(ContentsStatus.SUBMITTED);
		review.setCommunityUser(communityUser);
		review.setProduct(product);
		review.setReviewBody("レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文");
		review.setAlsoBuyProduct(AlsoBuyProduct.MAYNOTBUY);
		review.setProductSatisfaction(ProductSatisfaction.FOUR);
		reviewService.saveReview(review);

		DummySendMailDaoImpl.popMailInfoAndDelete();

		ReviewDO otherReview = new ReviewDO();
		otherReview.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
		otherReview.setStatus(ContentsStatus.SUBMITTED);
		otherReview.setCommunityUser(otherReviewr);
		otherReview.setProduct(product);
		otherReview.setReviewBody("別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文");
		otherReview.setAlsoBuyProduct(AlsoBuyProduct.MAYNOTBUY);
		otherReview.setProductSatisfaction(ProductSatisfaction.FOUR);
		reviewService.saveReview(otherReview);

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
//		assertEquals(1, mails.size());
		MailInfoDO mailInfo = mails.get(0);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("別のレビューが投稿されました") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("テストさんがレビューした") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(communityUser.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(otherReviewr.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("別レビュー本文") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("テストさんがレビューした") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(communityUser.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(otherReviewr.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("別レビュー本文") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(product.getProductName()) != -1);


		exportMail(mailInfo);
	}

	/**
	 * レビュー商品への別レビューのメールテストです。
	 */
	@Test
	public void testAnotherReviewSummary() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO otherReviewr1 = createCommunityUser("別の投稿者1", false);
		CommunityUserDO otherReviewr2 = createCommunityUser("別の投稿者2", false);
		CommunityUserDO otherReviewr3 = createCommunityUser("別の投稿者3", false);
		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.REVIEW_PRODUCT_ANOTHER_REVIEW);
		mailSetting.setMailSettingValue(MailSendTiming.DAILY_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		product = productService.getProductBySku("200000002000012355").getProduct();
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser, receiptJanCode, 
				salesDate);
		createReceipt(otherReviewr1, receiptJanCode, 
				salesDate);
		createReceipt(otherReviewr2, receiptJanCode, 
				salesDate);
		createReceipt(otherReviewr3, receiptJanCode, 
				salesDate);

		ReviewDO review = new ReviewDO();
		review.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
		review.setStatus(ContentsStatus.SUBMITTED);
		review.setCommunityUser(communityUser);
		review.setProduct(product);
		review.setReviewBody("レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文");
		review.setAlsoBuyProduct(AlsoBuyProduct.MAYNOTBUY);
		review.setProductSatisfaction(ProductSatisfaction.FOUR);
		reviewService.saveReview(review);

		ReviewDO otherReview = new ReviewDO();
		otherReview.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
		otherReview.setStatus(ContentsStatus.SUBMITTED);
		otherReview.setCommunityUser(otherReviewr1);
		otherReview.setProduct(product);
		otherReview.setReviewBody("別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文");
		otherReview.setAlsoBuyProduct(AlsoBuyProduct.MAYNOTBUY);
		otherReview.setProductSatisfaction(ProductSatisfaction.FOUR);
		reviewService.saveReview(otherReview);

		otherReview = new ReviewDO();
		otherReview.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
		otherReview.setStatus(ContentsStatus.SUBMITTED);
		otherReview.setCommunityUser(otherReviewr2);
		otherReview.setProduct(product);
		otherReview.setReviewBody("別レビュー本文2別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文");
		otherReview.setAlsoBuyProduct(AlsoBuyProduct.MAYNOTBUY);
		otherReview.setProductSatisfaction(ProductSatisfaction.FOUR);
		reviewService.saveReview(otherReview);

		otherReview = new ReviewDO();
		otherReview.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
		otherReview.setStatus(ContentsStatus.SUBMITTED);
		otherReview.setCommunityUser(otherReviewr3);
		otherReview.setProduct(product);
		otherReview.setReviewBody("別レビュー本文3別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文別レビュー本文");
		otherReview.setAlsoBuyProduct(AlsoBuyProduct.MAYNOTBUY);
		otherReview.setProductSatisfaction(ProductSatisfaction.FOUR);
		reviewService.saveReview(otherReview);

		DummySendMailDaoImpl.popMailInfoAndDelete();
		batchMailService.sendMail(communityUser, new Date());

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
		MailInfoDO mailInfo = null;
		for (MailInfoDO target : mails) {
			if (target.getMailType().equals(MailType.ANOTHER_REVIEW_SUMMARY)) {
				mailInfo = target;
			}
		}
//		assertNotNull(mailInfo);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("別のレビューが投稿されました") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("テストさんがレビューした") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(communityUser.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(otherReviewr1.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("別レビュー本文") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("テストさんがレビューした") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(communityUser.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(otherReviewr1.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("別レビュー本文") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(product.getProductName()) != -1);

		exportMail(mailInfo);
	}

	/**
	 * フォロー商品への新着レビューのメールテストです。
	 */
	@Test
	public void testFollowProductNewReview() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO reviwer = createCommunityUser("レビュワー", false);
		product = productService.getProductBySku("200000002000012355").getProduct();
		followService.followProduct(
				communityUser.getCommunityUserId(), product.getSku(), false);
		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.FOLLOW_PRODUCT_REVIEW);
		mailSetting.setMailSettingValue(MailSendTiming.EVERYTIME_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(reviwer, receiptJanCode, 
				salesDate);

		DummySendMailDaoImpl.popMailInfoAndDelete();

		ReviewDO review = new ReviewDO();
		review.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
		review.setStatus(ContentsStatus.SUBMITTED);
		review.setCommunityUser(reviwer);
		review.setProduct(product);
		review.setReviewBody("レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文");
		review.setAlsoBuyProduct(AlsoBuyProduct.MAYNOTBUY);
		review.setProductSatisfaction(ProductSatisfaction.FOUR);
		reviewService.saveReview(review);

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
//		assertEquals(1, mails.size());
		MailInfoDO mailInfo = mails.get(0);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("フォローしている商品にレビュー") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("レビュワーさんがレビュー") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(reviwer.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("レビュー本文") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("レビュワーさんがレビュー") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(reviwer.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("レビュー本文") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(product.getProductName()) != -1);


		exportMail(mailInfo);
	}

	/**
	 * フォロー商品への新着レビューのメールテストです。
	 */
	@Test
	public void testFollowProductNewReviewSummary() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO reviwer1 = createCommunityUser("レビュワー1", false);
		CommunityUserDO reviwer2 = createCommunityUser("レビュワー2", false);
		CommunityUserDO reviwer3 = createCommunityUser("レビュワー3", false);
		product = productService.getProductBySku("200000002000012355").getProduct();
		followService.followProduct(
				communityUser.getCommunityUserId(), product.getSku(), false);
		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.FOLLOW_PRODUCT_REVIEW);
		mailSetting.setMailSettingValue(MailSendTiming.DAILY_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(reviwer1, receiptJanCode, 
				salesDate);
		createReceipt(reviwer2, receiptJanCode, 
				salesDate);
		createReceipt(reviwer3, receiptJanCode, 
				salesDate);

		ReviewDO review = new ReviewDO();
		review.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
		review.setStatus(ContentsStatus.SUBMITTED);
		review.setCommunityUser(reviwer1);
		review.setProduct(product);
		review.setReviewBody("レビュー本文1レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文");
		review.setAlsoBuyProduct(AlsoBuyProduct.MAYNOTBUY);
		review.setProductSatisfaction(ProductSatisfaction.FOUR);
		reviewService.saveReview(review);

		review = new ReviewDO();
		review.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
		review.setStatus(ContentsStatus.SUBMITTED);
		review.setCommunityUser(reviwer2);
		review.setProduct(product);
		review.setReviewBody("レビュー本文2レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文");
		review.setAlsoBuyProduct(AlsoBuyProduct.MAYNOTBUY);
		review.setProductSatisfaction(ProductSatisfaction.FOUR);
		reviewService.saveReview(review);

		review = new ReviewDO();
		review.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
		review.setStatus(ContentsStatus.SUBMITTED);
		review.setCommunityUser(reviwer3);
		review.setProduct(product);
		review.setReviewBody("レビュー本文3レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文");
		review.setAlsoBuyProduct(AlsoBuyProduct.MAYNOTBUY);
		review.setProductSatisfaction(ProductSatisfaction.FOUR);
		reviewService.saveReview(review);

		DummySendMailDaoImpl.popMailInfoAndDelete();
		batchMailService.sendMail(communityUser, new Date());

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
		MailInfoDO mailInfo = null;
		for (MailInfoDO target : mails) {
			if (target.getMailType().equals(MailType.FOLLOW_PRODUCT_NEW_REVIEW_SUMMARY)) {
				mailInfo = target;
			}
		}
//		assertNotNull(mailInfo);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("フォローしている商品にレビュー") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("フォローしている商品にレビューが投稿されました") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("レビュー本文") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("フォローしている商品にレビューが投稿されました") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("レビュー本文") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(product.getProductName()) != -1);

		exportMail(mailInfo);
	}

	/**
	 * フォロー商品への新着質問のメールテストです。
	 */
	@Test
	public void testFollowProductNewQuestion() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO questionOwner = createCommunityUser("質問投稿者", false);
		product = productService.getProductBySku("200000002000012355").getProduct();
		followService.followProduct(
				communityUser.getCommunityUserId(), product.getSku(), false);
		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.FOLLOW_PRODUCT_QUESTION);
		mailSetting.setMailSettingValue(MailSendTiming.EVERYTIME_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		QuestionDO question = new QuestionDO();
		question.setCommunityUser(questionOwner);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文");

		DummySendMailDaoImpl.popMailInfoAndDelete();
		//質問を投稿で登録します。
		questionService.saveQuestion(question);

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
//		assertEquals(1, mails.size());
		MailInfoDO mailInfo = mails.get(0);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("フォローしている商品に質問") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("質問投稿者さんから質問が") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(questionOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("質問本文") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("質問投稿者さんから質問が") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(questionOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("質問本文") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(product.getProductName()) != -1);

		exportMail(mailInfo);
	}

	/**
	 * フォロー商品への新着質問のメールテストです。
	 */
	@Test
	public void testFollowProductNewQuestionSummary() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO questionOwner1 = createCommunityUser("質問投稿者1", false);
		CommunityUserDO questionOwner2 = createCommunityUser("質問投稿者2", false);
		CommunityUserDO questionOwner3 = createCommunityUser("質問投稿者3", false);

		product = productService.getProductBySku("200000002000012355").getProduct();
		followService.followProduct(
				communityUser.getCommunityUserId(), product.getSku(), false);
		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.FOLLOW_PRODUCT_QUESTION);
		mailSetting.setMailSettingValue(MailSendTiming.DAILY_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		QuestionDO question = new QuestionDO();
		question.setCommunityUser(questionOwner1);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文1質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文");

		//質問を投稿で登録します。
		questionService.saveQuestion(question);

		question = new QuestionDO();
		question.setCommunityUser(questionOwner2);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文2質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文");

		//質問を投稿で登録します。
		questionService.saveQuestion(question);

		question = new QuestionDO();
		question.setCommunityUser(questionOwner3);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文3質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文");

		//質問を投稿で登録します。
		questionService.saveQuestion(question);

		DummySendMailDaoImpl.popMailInfoAndDelete();
		batchMailService.sendMail(communityUser, new Date());

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
		MailInfoDO mailInfo = null;
		for (MailInfoDO target : mails) {
			if (target.getMailType().equals(MailType.FOLLOW_PRODUCT_NEW_QUESTION_SUMMARY)) {
				mailInfo = target;
			}
		}
//		assertNotNull(mailInfo);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("フォローしている商品に質問") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("フォローしている商品に質問が投稿されました") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(questionOwner1.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("質問本文") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("フォローしている商品に質問が投稿されました") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(questionOwner1.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("質問本文") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(product.getProductName()) != -1);

		exportMail(mailInfo);
	}

	/**
	 * フォロー商品への新着画像のメールテストです。
	 */
	@Test
	public void testFollowProductNewImage() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO imageOwner = createCommunityUser("画像投稿者", false);
		product = productService.getProductBySku("200000002000012355").getProduct();
		followService.followProduct(
				communityUser.getCommunityUserId(), product.getSku(), false);
		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.FOLLOW_PRODUCT_IMAGE);
		mailSetting.setMailSettingValue(MailSendTiming.EVERYTIME_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(imageOwner, receiptJanCode, 
				salesDate);

		List<ImageHeaderDO> imageHeaders = new ArrayList<ImageHeaderDO>();
		for (int i = 0; i < 4; i++) {
			ImageHeaderDO imageHeader = new ImageHeaderDO();
			imageHeaders.add(imageHeader);
			ImageDO image = new ImageDO();
			image.setData(testImageData);
			image.setMimeType("images/jpeg");
			image.setTemporaryKey("test");
			image.setWidth(400);
			image.setHeigth(500);
			image.setCommunityUserId(imageOwner.getCommunityUserId());
			imageService.createTemporaryImage(image);
			imageHeader.setImageId(image.getImageId());
		}

		DummySendMailDaoImpl.popMailInfoAndDelete();

		requestScopeDao.initialize(imageOwner, null);
		imageService.saveImageSet(product.getSku(), imageHeaders, null);

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
//		assertEquals(1, mails.size());
		MailInfoDO mailInfo = mails.get(0);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("フォローしている商品に画像") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("画像投稿者さんが画像を") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(imageOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("画像が投稿") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(imageOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(product.getProductName()) != -1);

		exportMail(mailInfo);
	}



	/**
	 * フォロー商品への新着画像のメールテストです。
	 */
	@Test
	public void testFollowProductNewImageSummary() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO imageOwner1 = createCommunityUser("画像投稿者1", false);
		CommunityUserDO imageOwner2 = createCommunityUser("画像投稿者2", false);
		CommunityUserDO imageOwner3 = createCommunityUser("画像投稿者3", false);

		product = productService.getProductBySku("200000002000012355").getProduct();
		followService.followProduct(
				communityUser.getCommunityUserId(), product.getSku(), false);
		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.FOLLOW_PRODUCT_IMAGE);
		mailSetting.setMailSettingValue(MailSendTiming.DAILY_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(imageOwner1, receiptJanCode, 
				salesDate);
		createReceipt(imageOwner2, receiptJanCode, 
				salesDate);
		createReceipt(imageOwner3, receiptJanCode, 
				salesDate);

		List<ImageHeaderDO> imageHeaders = new ArrayList<ImageHeaderDO>();
		for (int i = 0; i < 10; i++) {
			ImageHeaderDO imageHeader = new ImageHeaderDO();
			imageHeaders.add(imageHeader);
			ImageDO image = new ImageDO();
			image.setData(testImageData);
			image.setMimeType("images/jpeg");
			image.setTemporaryKey("test");
			image.setWidth(400);
			image.setHeigth(500);
			image.setCommunityUserId(imageOwner1.getCommunityUserId());
			imageService.createTemporaryImage(image);
			imageHeader.setImageId(image.getImageId());
		}

		requestScopeDao.initialize(imageOwner1, null);
		imageService.saveImageSet(product.getSku(), imageHeaders, null);


		imageHeaders = new ArrayList<ImageHeaderDO>();
		for (int i = 0; i < 10; i++) {
			ImageHeaderDO imageHeader = new ImageHeaderDO();
			imageHeaders.add(imageHeader);
			ImageDO image = new ImageDO();
			image.setData(testImageData);
			image.setMimeType("images/jpeg");
			image.setTemporaryKey("test");
			image.setWidth(400);
			image.setHeigth(500);
			image.setCommunityUserId(imageOwner2.getCommunityUserId());
			imageService.createTemporaryImage(image);
			imageHeader.setImageId(image.getImageId());
		}

		requestScopeDao.initialize(imageOwner2, null);
		imageService.saveImageSet(product.getSku(), imageHeaders, null);

		imageHeaders = new ArrayList<ImageHeaderDO>();
		for (int i = 0; i < 10; i++) {
			ImageHeaderDO imageHeader = new ImageHeaderDO();
			imageHeaders.add(imageHeader);
			ImageDO image = new ImageDO();
			image.setData(testImageData);
			image.setMimeType("images/jpeg");
			image.setTemporaryKey("test");
			image.setWidth(400);
			image.setHeigth(500);
			image.setCommunityUserId(imageOwner3.getCommunityUserId());
			imageService.createTemporaryImage(image);
			imageHeader.setImageId(image.getImageId());
		}

		requestScopeDao.initialize(imageOwner3, null);
		imageService.saveImageSet(product.getSku(), imageHeaders, null);

		DummySendMailDaoImpl.popMailInfoAndDelete();
		batchMailService.sendMail(communityUser, new Date());

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
		MailInfoDO mailInfo = null;
		for (MailInfoDO target : mails) {
			if (target.getMailType().equals(MailType.FOLLOW_PRODUCT_NEW_IMAGE_SUMMARY)) {
				mailInfo = target;
			}
		}

//		assertNotNull(mailInfo);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("フォローしている商品に画像") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("フォローしている商品に画像が投稿されました") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(imageOwner1.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("フォローしている商品に画像が投稿されました。") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(imageOwner1.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(product.getProductName()) != -1);

		exportMail(mailInfo);
	}

	/**
	 * フォロー質問への新着回答のメールテストです。
	 */
	@Test
	public void testFollowQuestionNewAnswer() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO questionOwner = createCommunityUser("質問投稿者", false);
		CommunityUserDO answerOwner = createCommunityUser("回答投稿者", false);
		product = productService.getProductBySku("200000002000012355").getProduct();

		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(answerOwner, receiptJanCode, 
				salesDate);

		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.FOLLOW_QUESTION_ANSWER);
		mailSetting.setMailSettingValue(MailSendTiming.EVERYTIME_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		QuestionDO question = new QuestionDO();
		question.setCommunityUser(questionOwner);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文");

		//質問を投稿で登録します。
		questionService.saveQuestion(question);

		followService.followQuestion(communityUser.getCommunityUserId(),
				question.getQuestionId(), false);

		DummySendMailDaoImpl.popMailInfoAndDelete();

		QuestionAnswerDO questionAnswer = new QuestionAnswerDO();
		questionAnswer.setAnswerBody("質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答");
		questionAnswer.setCommunityUser(answerOwner);
		questionAnswer.setQuestion(question);
		questionAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionService.saveQuestionAnswer(questionAnswer);

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
//		assertEquals(2, mails.size());
		MailInfoDO mailInfo = null;
		for (MailInfoDO target : mails) {
			if (target.getCommunityUser().getCommunityUserId(
					).equals(communityUser.getCommunityUserId())) {
				mailInfo = target;
			}
		}
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("回答が投稿されました") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(answerOwner.getCommunityName() + "さんが回答しました") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("質問本文") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("質問回答") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(answerOwner.getCommunityName() + "さんが回答しました") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("質問回答") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("質問本文") != -1);

		exportMail(mailInfo);
	}

	/**
	 * フォロー質問への新着回答のメールテストです。
	 */
	@Test
	public void testFollowQuestionNewAnswerSummary() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO questionOwner = createCommunityUser("質問投稿者", false);
		CommunityUserDO answerOwner1 = createCommunityUser("回答投稿者1", false);
		CommunityUserDO answerOwner2 = createCommunityUser("回答投稿者2", false);
		CommunityUserDO answerOwner3 = createCommunityUser("回答投稿者3", false);

		product = productService.getProductBySku("200000002000012355").getProduct();

		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(answerOwner1, receiptJanCode, 
				salesDate);
		createReceipt(answerOwner2, receiptJanCode, 
				salesDate);
		createReceipt(answerOwner3, receiptJanCode, 
				salesDate);

		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.FOLLOW_QUESTION_ANSWER);
		mailSetting.setMailSettingValue(MailSendTiming.DAILY_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		QuestionDO question = new QuestionDO();
		question.setCommunityUser(questionOwner);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文");

		//質問を投稿で登録します。
		questionService.saveQuestion(question);

		followService.followQuestion(communityUser.getCommunityUserId(),
				question.getQuestionId(), false);

		QuestionAnswerDO questionAnswer = new QuestionAnswerDO();
		questionAnswer.setAnswerBody("質問回答1質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答");
		questionAnswer.setCommunityUser(answerOwner1);
		questionAnswer.setQuestion(question);
		questionAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionService.saveQuestionAnswer(questionAnswer);

		questionAnswer = new QuestionAnswerDO();
		questionAnswer.setAnswerBody("質問回答2質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答");
		questionAnswer.setCommunityUser(answerOwner2);
		questionAnswer.setQuestion(question);
		questionAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionService.saveQuestionAnswer(questionAnswer);

		questionAnswer = new QuestionAnswerDO();
		questionAnswer.setAnswerBody("質問回答3質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答");
		questionAnswer.setCommunityUser(answerOwner3);
		questionAnswer.setQuestion(question);
		questionAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionService.saveQuestionAnswer(questionAnswer);

		DummySendMailDaoImpl.popMailInfoAndDelete();
		batchMailService.sendMail(communityUser, new Date());

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
		MailInfoDO mailInfo = null;
		for (MailInfoDO target : mails) {
			if (target.getMailType().equals(MailType.FOLLOW_QUESTION_NEW_ANSWER_SUMMARY)) {
				mailInfo = target;
			}
		}


//		assertNotNull(mailInfo);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("回答が投稿されました") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("回答が投稿されました。") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(answerOwner1.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("質問回答") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("回答が投稿されました") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("質問回答") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("質問本文") != -1);

		exportMail(mailInfo);
	}

	/**
	 * フォローユーザーの新着レビューのメールテストです。
	 */
	@Test
	public void testFollowUserNewReview() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO reviwer = createCommunityUser("レビュワー", false);
		product = productService.getProductBySku("200000002000012355").getProduct();
		followService.followCommunityUser(communityUser.getCommunityUserId(),
				reviwer.getCommunityUserId(), false);
		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.FOLLOW_USER_REVIEW);
		mailSetting.setMailSettingValue(MailSendTiming.EVERYTIME_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(reviwer, receiptJanCode, 
				salesDate);

		DummySendMailDaoImpl.popMailInfoAndDelete();

		ReviewDO review = new ReviewDO();
		review.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
		review.setStatus(ContentsStatus.SUBMITTED);
		review.setCommunityUser(reviwer);
		review.setProduct(product);
		review.setReviewBody("レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文");
		review.setAlsoBuyProduct(AlsoBuyProduct.MAYNOTBUY);
		review.setProductSatisfaction(ProductSatisfaction.FOUR);
		reviewService.saveReview(review);

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
//		assertEquals(1, mails.size());
		MailInfoDO mailInfo = mails.get(0);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("フォローしている") != -1);
//		assertEquals(true, mailInfo.getTitle().indexOf(reviwer.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTitle().indexOf("さんがレビュー") != -1);
//		assertEquals(true, mailInfo.getTitle().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("フォローしている") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(reviwer.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("レビュー本文") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("フォローしている") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(reviwer.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("レビュー本文") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(product.getProductName()) != -1);
		
		exportMail(mailInfo);
	}

	/**
	 * フォローユーザーの新着レビューのメールテストです。
	 */
	@Test
	public void testFollowUserNewReviewSummary() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO reviwer1 = createCommunityUser("レビュワー1", false);
		CommunityUserDO reviwer2 = createCommunityUser("レビュワー2", false);
		CommunityUserDO reviwer3 = createCommunityUser("レビュワー3", false);
		product = productService.getProductBySku("200000002000012355").getProduct();
		followService.followCommunityUser(communityUser.getCommunityUserId(),
				reviwer1.getCommunityUserId(), false);
		followService.followCommunityUser(communityUser.getCommunityUserId(),
				reviwer2.getCommunityUserId(), false);
		followService.followCommunityUser(communityUser.getCommunityUserId(),
				reviwer3.getCommunityUserId(), false);
		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.FOLLOW_USER_REVIEW);
		mailSetting.setMailSettingValue(MailSendTiming.DAILY_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(reviwer1, receiptJanCode, 
				salesDate);
		createReceipt(reviwer2, receiptJanCode, 
				salesDate);
		createReceipt(reviwer3, receiptJanCode, 
				salesDate);

		ReviewDO review = new ReviewDO();
		review.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
		review.setStatus(ContentsStatus.SUBMITTED);
		review.setCommunityUser(reviwer1);
		review.setProduct(product);
		review.setReviewBody("レビュー本文1レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文");
		review.setAlsoBuyProduct(AlsoBuyProduct.MAYNOTBUY);
		review.setProductSatisfaction(ProductSatisfaction.FOUR);
		reviewService.saveReview(review);

		review = new ReviewDO();
		review.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
		review.setStatus(ContentsStatus.SUBMITTED);
		review.setCommunityUser(reviwer2);
		review.setProduct(product);
		review.setReviewBody("レビュー本文2レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文");
		review.setAlsoBuyProduct(AlsoBuyProduct.MAYNOTBUY);
		review.setProductSatisfaction(ProductSatisfaction.FOUR);
		reviewService.saveReview(review);

		review = new ReviewDO();
		review.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
		review.setStatus(ContentsStatus.SUBMITTED);
		review.setCommunityUser(reviwer3);
		review.setProduct(product);
		review.setReviewBody("レビュー本文3レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文レビュー本文");
		review.setAlsoBuyProduct(AlsoBuyProduct.MAYNOTBUY);
		review.setProductSatisfaction(ProductSatisfaction.FOUR);
		reviewService.saveReview(review);

		DummySendMailDaoImpl.popMailInfoAndDelete();
		batchMailService.sendMail(communityUser, new Date());

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
		MailInfoDO mailInfo = null;
		for (MailInfoDO target : mails) {
			if (target.getMailType().equals(MailType.FOLLOW_USER_NEW_REVIEW_SUMMARY)) {
				mailInfo = target;
			}
		}
//		assertNotNull(mailInfo);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("フォローしている") != -1);
//		assertEquals(true, mailInfo.getTitle().indexOf("ユーザーがレビューを投稿しました") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("フォローしている") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("レビュー本文") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("フォローしている") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(reviwer1.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("レビュー本文") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(product.getProductName()) != -1);

		exportMail(mailInfo);
	}

	/**
	 * フォローユーザーの新着質問のメールテストです。
	 */
	@Test
	public void testFollowUserNewQuestion() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO questionOwner = createCommunityUser("質問投稿者", false);
		product = productService.getProductBySku("200000002000012355").getProduct();
		followService.followCommunityUser(communityUser.getCommunityUserId(),
				questionOwner.getCommunityUserId(), false);
		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.FOLLOW_USER_QUESTION);
		mailSetting.setMailSettingValue(MailSendTiming.EVERYTIME_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		QuestionDO question = new QuestionDO();
		question.setCommunityUser(questionOwner);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文");

		DummySendMailDaoImpl.popMailInfoAndDelete();
		//質問を投稿で登録します。
		questionService.saveQuestion(question);

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
//		assertEquals(1, mails.size());
		MailInfoDO mailInfo = mails.get(0);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("フォローしている") != -1);
//		assertEquals(true, mailInfo.getTitle().indexOf(questionOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTitle().indexOf("さんが質問") != -1);
//		assertEquals(true, mailInfo.getTitle().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("フォローしている") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(questionOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("質問本文") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("フォローしている") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(questionOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("質問本文") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(product.getProductName()) != -1);

		exportMail(mailInfo);
	}

	/**
	 * フォローユーザーの新着質問のメールテストです。
	 */
	@Test
	public void testFollowUserNewQuestionSummary() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO questionOwner1 = createCommunityUser("質問投稿者1", false);
		CommunityUserDO questionOwner2 = createCommunityUser("質問投稿者2", false);
		CommunityUserDO questionOwner3 = createCommunityUser("質問投稿者3", false);
		product = productService.getProductBySku("200000002000012355").getProduct();
		followService.followCommunityUser(communityUser.getCommunityUserId(),
				questionOwner1.getCommunityUserId(), false);
		followService.followCommunityUser(communityUser.getCommunityUserId(),
				questionOwner2.getCommunityUserId(), false);
		followService.followCommunityUser(communityUser.getCommunityUserId(),
				questionOwner3.getCommunityUserId(), false);
		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.FOLLOW_USER_QUESTION);
		mailSetting.setMailSettingValue(MailSendTiming.DAILY_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		QuestionDO question = new QuestionDO();
		question.setCommunityUser(questionOwner1);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文1質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文");

		//質問を投稿で登録します。
		questionService.saveQuestion(question);


		question = new QuestionDO();
		question.setCommunityUser(questionOwner2);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文2質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文");

		//質問を投稿で登録します。
		questionService.saveQuestion(question);

		question = new QuestionDO();
		question.setCommunityUser(questionOwner3);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文3質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文");

		//質問を投稿で登録します。
		questionService.saveQuestion(question);

		DummySendMailDaoImpl.popMailInfoAndDelete();
		batchMailService.sendMail(communityUser, new Date());

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
		MailInfoDO mailInfo = null;
		for (MailInfoDO target : mails) {
			if (target.getMailType().equals(MailType.FOLLOW_USER_NEW_QUESTION_SUMMARY)) {
				mailInfo = target;
			}
		}
//		assertNotNull(mailInfo);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("フォローしている") != -1);
//		assertEquals(true, mailInfo.getTitle().indexOf("が質問") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("フォローしている") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(questionOwner1.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("質問本文") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("フォローしている") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(questionOwner1.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("質問本文") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(product.getProductName()) != -1);


		exportMail(mailInfo);
	}

	/**
	 * フォローユーザーの新着質問回答のメールテストです。
	 */
	@Test
	public void testFollowUserNewAnswer() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO questionOwner = createCommunityUser("質問投稿者", false);
		CommunityUserDO answerOwner = createCommunityUser("回答投稿者", false);
		product = productService.getProductBySku("200000002000012355").getProduct();

		followService.followCommunityUser(communityUser.getCommunityUserId(),
				answerOwner.getCommunityUserId(), false);

		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(answerOwner, receiptJanCode, 
				salesDate);

		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.FOLLOW_USER_QUESTION_ANSWER);
		mailSetting.setMailSettingValue(MailSendTiming.EVERYTIME_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		QuestionDO question = new QuestionDO();
		question.setCommunityUser(questionOwner);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文");

		//質問を投稿で登録します。
		questionService.saveQuestion(question);

		DummySendMailDaoImpl.popMailInfoAndDelete();

		QuestionAnswerDO questionAnswer = new QuestionAnswerDO();
		questionAnswer.setAnswerBody("質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答");
		questionAnswer.setCommunityUser(answerOwner);
		questionAnswer.setQuestion(question);
		questionAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionService.saveQuestionAnswer(questionAnswer);

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
//		assertEquals(2, mails.size());
		MailInfoDO mailInfo = null;
		for (MailInfoDO target : mails) {
			if (target.getCommunityUser().getCommunityUserId(
					).equals(communityUser.getCommunityUserId())) {
				mailInfo = target;
			}
		}
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("フォローしている") != -1);
//		assertEquals(true, mailInfo.getTitle().indexOf(answerOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTitle().indexOf("回答しました") != -1);
//		assertEquals(true, mailInfo.getTitle().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("フォローしている") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(answerOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("質問回答") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("フォローしている") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(answerOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("質問回答") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(product.getProductName()) != -1);

		exportMail(mailInfo);
	}

	/**
	 * フォローユーザーの新着質問回答のメールテストです。
	 */
	@Test
	public void testFollowUserNewAnswerSummary() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO questionOwner = createCommunityUser("質問投稿者", false);
		CommunityUserDO answerOwner1 = createCommunityUser("回答投稿者1", false);
		CommunityUserDO answerOwner2 = createCommunityUser("回答投稿者2", false);
		CommunityUserDO answerOwner3 = createCommunityUser("回答投稿者3", false);
		product = productService.getProductBySku("200000002000012355").getProduct();

		followService.followCommunityUser(communityUser.getCommunityUserId(),
				answerOwner1.getCommunityUserId(), false);
		followService.followCommunityUser(communityUser.getCommunityUserId(),
				answerOwner2.getCommunityUserId(), false);
		followService.followCommunityUser(communityUser.getCommunityUserId(),
				answerOwner3.getCommunityUserId(), false);

		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(answerOwner1, receiptJanCode, 
				salesDate);
		createReceipt(answerOwner2, receiptJanCode, 
				salesDate);
		createReceipt(answerOwner3, receiptJanCode, 
				salesDate);

		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.FOLLOW_USER_QUESTION_ANSWER);
		mailSetting.setMailSettingValue(MailSendTiming.DAILY_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		QuestionDO question = new QuestionDO();
		question.setCommunityUser(questionOwner);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文質問本文");

		//質問を投稿で登録します。
		questionService.saveQuestion(question);

		QuestionAnswerDO questionAnswer = new QuestionAnswerDO();
		questionAnswer.setAnswerBody("質問回答1質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答");
		questionAnswer.setCommunityUser(answerOwner1);
		questionAnswer.setQuestion(question);
		questionAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionService.saveQuestionAnswer(questionAnswer);

		questionAnswer = new QuestionAnswerDO();
		questionAnswer.setAnswerBody("質問回答2質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答");
		questionAnswer.setCommunityUser(answerOwner2);
		questionAnswer.setQuestion(question);
		questionAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionService.saveQuestionAnswer(questionAnswer);

		questionAnswer = new QuestionAnswerDO();
		questionAnswer.setAnswerBody("質問回答3質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答質問回答");
		questionAnswer.setCommunityUser(answerOwner3);
		questionAnswer.setQuestion(question);
		questionAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionService.saveQuestionAnswer(questionAnswer);


		DummySendMailDaoImpl.popMailInfoAndDelete();
		batchMailService.sendMail(communityUser, new Date());

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
		MailInfoDO mailInfo = null;
		for (MailInfoDO target : mails) {
			if (target.getMailType().equals(MailType.FOLLOW_USER_NEW_ANSWER_SUMMARY)) {
				mailInfo = target;
			}
		}
//		assertNotNull(mailInfo);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("フォローしている") != -1);
////		assertEquals(true, mailInfo.getTitle().indexOf(answerOwner1.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTitle().indexOf("回答しました") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("フォローしている") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(answerOwner1.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("質問回答") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("フォローしている") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(answerOwner1.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("質問回答") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(product.getProductName()) != -1);

		exportMail(mailInfo);
	}

	/**
	 * フォローユーザーの新着画像のメールテストです。
	 */
	@Test
	public void testFollowUserNewImage() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO imageOwner = createCommunityUser("画像投稿者", false);
		product = productService.getProductBySku("200000002000012355").getProduct();
		followService.followCommunityUser(communityUser.getCommunityUserId(),
				imageOwner.getCommunityUserId(), false);
		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.FOLLOW_USER_IMAGE);
		mailSetting.setMailSettingValue(MailSendTiming.EVERYTIME_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(imageOwner, receiptJanCode, 
				salesDate);

		List<ImageHeaderDO> imageHeaders = new ArrayList<ImageHeaderDO>();
		for (int i = 0; i < 4; i++) {
			ImageHeaderDO imageHeader = new ImageHeaderDO();
			imageHeaders.add(imageHeader);
			ImageDO image = new ImageDO();
			image.setData(testImageData);
			image.setMimeType("images/jpeg");
			image.setTemporaryKey("test");
			image.setWidth(400);
			image.setHeigth(500);
			image.setCommunityUserId(imageOwner.getCommunityUserId());
			imageService.createTemporaryImage(image);
			imageHeader.setImageId(image.getImageId());
		}

		DummySendMailDaoImpl.popMailInfoAndDelete();

		requestScopeDao.initialize(imageOwner, null);
		imageService.saveImageSet(product.getSku(), imageHeaders, null);

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
//		assertEquals(1, mails.size());
		MailInfoDO mailInfo = mails.get(0);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("フォローしている") != -1);
//		assertEquals(true, mailInfo.getTitle().indexOf(imageOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTitle().indexOf("さんが画像") != -1);
//		assertEquals(true, mailInfo.getTitle().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("フォローしている") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(imageOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("フォローしている") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(imageOwner.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(product.getProductName()) != -1);

		exportMail(mailInfo);
	}

	/**
	 * フォローユーザーの新着画像のメールテストです。
	 */
	@Test
	public void testFollowUserNewImageSummary() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO imageOwner1 = createCommunityUser("画像投稿者1", false);
		CommunityUserDO imageOwner2 = createCommunityUser("画像投稿者2", false);
		CommunityUserDO imageOwner3 = createCommunityUser("画像投稿者3", false);

		product = productService.getProductBySku("200000002000012355").getProduct();
		followService.followCommunityUser(communityUser.getCommunityUserId(),
				imageOwner1.getCommunityUserId(), false);
		followService.followCommunityUser(communityUser.getCommunityUserId(),
				imageOwner2.getCommunityUserId(), false);
		followService.followCommunityUser(communityUser.getCommunityUserId(),
				imageOwner3.getCommunityUserId(), false);
		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.FOLLOW_USER_IMAGE);
		mailSetting.setMailSettingValue(MailSendTiming.DAILY_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(imageOwner1, receiptJanCode, 
				salesDate);
		createReceipt(imageOwner2, receiptJanCode, 
				salesDate);
		createReceipt(imageOwner3, receiptJanCode, 
				salesDate);

		List<ImageHeaderDO> imageHeaders = new ArrayList<ImageHeaderDO>();
		for (int i = 0; i < 10; i++) {
			ImageHeaderDO imageHeader = new ImageHeaderDO();
			imageHeaders.add(imageHeader);
			ImageDO image = new ImageDO();
			image.setData(testImageData);
			image.setMimeType("images/jpeg");
			image.setTemporaryKey("test");
			image.setWidth(400);
			image.setHeigth(500);
			image.setCommunityUserId(imageOwner1.getCommunityUserId());
			imageService.createTemporaryImage(image);
			imageHeader.setImageId(image.getImageId());
		}


		requestScopeDao.initialize(imageOwner1, null);
		imageService.saveImageSet(product.getSku(), imageHeaders, null);


		imageHeaders = new ArrayList<ImageHeaderDO>();
		for (int i = 0; i < 10; i++) {
			ImageHeaderDO imageHeader = new ImageHeaderDO();
			imageHeaders.add(imageHeader);
			ImageDO image = new ImageDO();
			image.setData(testImageData);
			image.setMimeType("images/jpeg");
			image.setTemporaryKey("test");
			image.setWidth(400);
			image.setHeigth(500);
			image.setCommunityUserId(imageOwner2.getCommunityUserId());
			imageService.createTemporaryImage(image);
			imageHeader.setImageId(image.getImageId());
		}

		requestScopeDao.initialize(imageOwner2, null);
		imageService.saveImageSet(product.getSku(), imageHeaders, null);


		imageHeaders = new ArrayList<ImageHeaderDO>();
		for (int i = 0; i < 10; i++) {
			ImageHeaderDO imageHeader = new ImageHeaderDO();
			imageHeaders.add(imageHeader);
			ImageDO image = new ImageDO();
			image.setData(testImageData);
			image.setMimeType("images/jpeg");
			image.setTemporaryKey("test");
			image.setWidth(400);
			image.setHeigth(500);
			image.setCommunityUserId(imageOwner3.getCommunityUserId());
			imageService.createTemporaryImage(image);
			imageHeader.setImageId(image.getImageId());
		}

		requestScopeDao.initialize(imageOwner3, null);
		imageService.saveImageSet(product.getSku(), imageHeaders, null);

		DummySendMailDaoImpl.popMailInfoAndDelete();
		batchMailService.sendMail(communityUser, new Date());

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
		MailInfoDO mailInfo = null;
		for (MailInfoDO target : mails) {
			if (target.getMailType().equals(MailType.FOLLOW_USER_NEW_IMAGE_SUMMARY)) {
				mailInfo = target;
			}
		}
//		assertNotNull(mailInfo);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("フォローしている") != -1);
////		assertEquals(true, mailInfo.getTitle().indexOf(imageOwner1.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTitle().indexOf("が画像") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("フォローしている") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(imageOwner1.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("フォローしている") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(imageOwner1.getCommunityName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(product.getProductName()) != -1);


		exportMail(mailInfo);
	}

	/**
	 * 会員の一時停止のメールテストです。
	 */
	@Test
	public void testStopCommunityUser() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);

		DummySendMailDaoImpl.popMailInfoAndDelete();

		userService.updateStop(communityUser.getCommunityUserId(), true);

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
//		assertEquals(1, mails.size());
		MailInfoDO mailInfo = mails.get(0);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("一時停止のご連絡") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("テスト") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("テスト") != -1);

		exportMail(mailInfo);
	}

	/**
	 * 商品マスターランクインのメールテストです。
	 */
	@Test
	public void testProductMasterRankInSummary() {
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		MailSettingDO mailSetting = new MailSettingDO();
		mailSetting.setCommunityUserId(communityUser.getCommunityUserId());
		mailSetting.setMailSettingType(MailSettingType.RANK_IN_PRODUCT_MASTER);
		mailSetting.setMailSettingValue(MailSendTiming.DAILY_NOTIFY);
		List<MailSettingDO> mailSettings = new ArrayList<MailSettingDO>();
		mailSettings.add(mailSetting);
		userService.saveMailSettings(mailSettings);

		product = productService.getProductBySku("200000002000012355").getProduct();
		VersionDO version = productService.getNextProductMasterVersion();
		ProductMasterDO productMaster = new ProductMasterDO();
		productMaster.setVersion(version.getVersion());
		productMaster.setRankInVersion(version.getVersion());
		productMaster.setRank(1);
		productMaster.setRequiredNotify(true);
		productMaster.setCommunityUser(communityUser);
		productMaster.setProduct(product);
		List<ProductMasterDO> productMasters = new ArrayList<ProductMasterDO>();
		productMasters.add(productMaster);
		productMasterDao.createProductMastersWithIndex(productMasters);
		productService.upgradeProductMasterVersion();

		productService.changeProductMasterRanking(productMaster);

		DummySendMailDaoImpl.popMailInfoAndDelete();
		batchMailService.sendMail(communityUser, new Date());

		List<MailInfoDO> mails = DummySendMailDaoImpl.popMailInfoAndDelete();
		MailInfoDO mailInfo = null;
		for (MailInfoDO target : mails) {
			if (target.getMailType().equals(MailType.PRODUCT_MASTER_RANK_IN_SUMMARY)) {
				mailInfo = target;
			}
		}


//		assertNotNull(mailInfo);
//		assertEquals(communityUser.getCommunityUserId(),
//				mailInfo.getCommunityUser().getCommunityUserId());
//		assertEquals(true, mailInfo.getTitle().indexOf("商品マスターにランクインしました") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf("1にランクイン") != -1);
//		assertEquals(true, mailInfo.getTextBody().indexOf(product.getProductName()) != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf("1にランクイン") != -1);
//		assertEquals(true, mailInfo.getHtmlBody().indexOf(product.getProductName()) != -1);

		exportMail(mailInfo);
	}


	private void createProductMasters(CommunityUserDO communityUser){

		String[] skus ={
				"100000001001033690",
				"100000001000965304",
				"100000001000937211",
				"100000001000827415",
				"100000001001391026",
				"100000009000738581",
				"200000002000012355"};

		/** 商品マスターランクイン */
		VersionDO version = productService.getNextProductMasterVersion();

		int i = 1;
		for(String sku:skus){
			ProductDO product = productService.getProductBySku(sku).getProduct();
			List<ProductMasterDO> productMasters = new ArrayList<ProductMasterDO>();
			ProductMasterDO productMaster = new ProductMasterDO();
			productMaster.setRank(i);
			productMaster.setVersion(version.getVersion());
			productMaster.setCommunityUser(communityUser);
			productMaster.setAdult(product.isAdult());
			productMaster.setProduct(product);

			productMaster.setImageLikeCount(random.nextInt(100));
			productMaster.setImagePostCount(random.nextInt(100));
			productMaster.setReviewLikeCount(random.nextInt(100));
			productMaster.setReviewPostCount(random.nextInt(100));
			productMaster.setReviewShowCount(random.nextInt(100));
			productMaster.setAnswerLikeCount(random.nextInt(100));
			productMaster.setAnswerPostCount(random.nextInt(100));

			productMasters.add(productMaster);
			productMaster.setPurchaseDate(new Date());
			productMasterDao.createProductMastersWithIndex(productMasters);
			productMasterDao.updateProductMasterVersion(version);
			productMaster.setRequiredNotify(true);
			productService.changeProductMasterRanking(productMaster);
			i++;
		}
	}

}
