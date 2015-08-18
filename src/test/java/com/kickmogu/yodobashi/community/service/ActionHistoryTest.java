package com.kickmogu.yodobashi.community.service;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.dao.ActionHistoryDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductMasterDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.VersionDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.AlsoBuyProduct;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ProductSatisfaction;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewType;

/**
 * ユーザーサービスのテストクラスです。
 *
 * @author hirabayashi
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/jcclientContext.xml")
public class ActionHistoryTest extends BaseTest {

	/**
	 * データを初期化します。
	 */
	protected void initialize() {
		super.initialize();
	}
	
	@Autowired private ProductService productService;
	@Autowired private ReviewService reviewService;
	@Autowired private QuestionService questionService;
	
	@Autowired private ActionHistoryDao actionHistoryDao;
	
	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() {
		initialize();
	}
	
	@Test
	public void actionHistoryReviewTest(){
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		product = productService.getProductBySku("200000002000012355").getProduct();
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser, receiptJanCode, salesDate);

		ReviewDO review = new ReviewDO();
		review.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
		review.setStatus(ContentsStatus.SUBMITTED);
		review.setCommunityUser(communityUser);
		review.setProduct(product);
		review.setReviewBody("レビュー本文");
		review.setAlsoBuyProduct(AlsoBuyProduct.MAYNOTBUY);
		review.setProductSatisfaction(ProductSatisfaction.FOUR);
		review.setPurchaseDate(salesDate);
		reviewService.saveReview(review);

		SearchResult<ActionHistoryDO> history = actionHistoryDao.findNewsFeedBySku("200000002000012355", 100, null, false);
		
		assertTrue(history.getDocuments().get(0).getReview() != null);
		
	}

	@Test
	public void actionHistoryQuestionTest(){
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		product = productService.getProductBySku("200000002000012355").getProduct();
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser, receiptJanCode, salesDate);

		QuestionDO question = new QuestionDO();
		question.setCommunityUser(communityUser);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文");

		//質問を投稿で登録します。
		questionService.saveQuestion(question);

		SearchResult<ActionHistoryDO> history = actionHistoryDao.findNewsFeedBySku("200000002000012355", 100, null, false);
		
		assertTrue(history.getDocuments().get(0).getQuestion() != null);
		
	}
	
	
	@Test
	public void actionHistoryQuestionAnswerTest(){
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO communityUser2 = createCommunityUser("質問者", false);
		product = productService.getProductBySku("200000002000012355").getProduct();
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser, receiptJanCode, salesDate);

		QuestionDO question = new QuestionDO();
		question.setCommunityUser(communityUser2);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文");

		//質問を投稿で登録します。
		questionService.saveQuestion(question);

		QuestionAnswerDO questionAnswer = new QuestionAnswerDO();
		questionAnswer.setAnswerBody("質問回答");
		questionAnswer.setCommunityUser(communityUser);
		questionAnswer.setQuestion(question);
		questionAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionAnswer.setPurchaseDate(salesDate);
		questionService.saveQuestionAnswer(questionAnswer);

		SearchResult<ActionHistoryDO> history = actionHistoryDao.findNewsFeedBySku("200000002000012355", 100, null, false);
		
		assertTrue(history.getDocuments().get(0).getQuestionAnswer() != null);
		
	}
	
	@Autowired RequestScopeDao requestScopeDao;
	@Autowired LikeService likeService;
	@Autowired CommentService commentService;
	

	@Autowired ProductMasterDao productMasterDao;
	@Test
	public void actionHistoryProductMasterTest(){
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		product = productService.getProductBySku("200000002000012355").getProduct();

		/** 商品マスターランクイン */
		VersionDO version = productService.getNextProductMasterVersion();

		
		List<ProductMasterDO> productMasters = new ArrayList<ProductMasterDO>();
		ProductMasterDO productMaster = new ProductMasterDO();
		productMaster.setRank(1);
		productMaster.setVersion(version.getVersion());
		productMaster.setCommunityUser(communityUser);
		productMaster.setAdult(product.isAdult());
		productMaster.setProduct(product);

		productMasters.add(productMaster);
		productMaster.setPurchaseDate(new Date());
		productMasterDao.createProductMastersWithIndex(productMasters);
		productMasterDao.updateProductMasterVersion(version);
		productMaster.setRequiredNotify(true);
		productService.changeProductMasterRanking(productMaster);

		solrOperations.deleteByKey(ProductMasterDO.class, productMaster.getProductMasterId());

		SearchResult<ActionHistoryDO> history = actionHistoryDao.findNewsFeedBySku("200000002000012355", 100, null, false);
		
		assertTrue(history.getDocuments().get(0).getProductMaster() != null);
		
	}
}
