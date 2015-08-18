package com.kickmogu.yodobashi.community.service;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.dao.InformationDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductMasterDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.VersionDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.AlsoBuyProduct;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommentTargetType;
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
public class InformationTest extends BaseTest {

	/**
	 * データを初期化します。
	 */
	protected void initialize() {
		super.initialize();
	}
	
	@Autowired private ProductService productService;
	@Autowired private ReviewService reviewService;
	@Autowired private QuestionService questionService;
	
	@Autowired private InformationDao informationDao ;
	
	@Autowired RequestScopeDao requestScopeDao;
	@Autowired LikeService likeService;
	@Autowired CommentService commentService;
	@Autowired ProductMasterDao productMasterDao;

	
	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() {
		initialize();
	}
	
	@Test
	public void infomationReviewTest(){
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO communityUser2 = createCommunityUser("テスト2", false);
		product = productService.getProductBySku("200000002000012355").getProduct();
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser, receiptJanCode, 
				salesDate);

		ReviewDO review = new ReviewDO();
		review.setPurchaseDate(salesDate);
		review.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
		review.setStatus(ContentsStatus.SUBMITTED);
		review.setCommunityUser(communityUser);
		review.setProduct(product);
		review.setReviewBody("レビュー本文");
		review.setAlsoBuyProduct(AlsoBuyProduct.MAYNOTBUY);
		review.setProductSatisfaction(ProductSatisfaction.FOUR);
		reviewService.saveReview(review);

		CommentDO comment = new CommentDO();
		comment.setTargetType(CommentTargetType.REVIEW);
		comment.setCommentBody("テストコメントテストコメントテストコ");
		comment.setReview(review);
		comment.setCommunityUser(communityUser2);
		commentService.saveComment(comment);
		
		solrOperations.deleteByKey(ReviewDO.class, review.getReviewId());

		SearchResult<InformationDO> history = informationDao.findNoReadInformationByCommunityUserId(communityUser.getCommunityUserId(), 100, 0);
		
		assertTrue(history.getDocuments().get(0).getReview() != null);
		
	}

	@Test
	public void infomationQuestionTest(){
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO communityUser2 = createCommunityUser("テスト2", false);
		product = productService.getProductBySku("200000002000012355").getProduct();
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser, receiptJanCode, 
				salesDate);
		createReceipt(communityUser2, receiptJanCode, 
				salesDate);

		QuestionDO question = new QuestionDO();
		question.setCommunityUser(communityUser);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文");

		//質問を投稿で登録します。
		questionService.saveQuestion(question);

		QuestionAnswerDO questionAnswer = new QuestionAnswerDO();
		questionAnswer.setAnswerBody("質問回答");
		questionAnswer.setCommunityUser(communityUser2);
		questionAnswer.setQuestion(question);
		questionAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionAnswer.setPurchaseDate(salesDate);
		questionService.saveQuestionAnswer(questionAnswer);
		
		solrOperations.deleteByKey(QuestionDO.class, question.getQuestionId());

		SearchResult<InformationDO> history = informationDao.findNoReadInformationByCommunityUserId(communityUser.getCommunityUserId(), 100, 0);
		
		assertTrue(history.getDocuments().get(0).getQuestion() != null);
		
	}
	
	@Test
	public void infomationQuestionAnswerTest(){
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO communityUser2 = createCommunityUser("テスト2", false);
		product = productService.getProductBySku("200000002000012355").getProduct();
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser, receiptJanCode, 
				salesDate);
		createReceipt(communityUser2, receiptJanCode, 
				salesDate);

		QuestionDO question = new QuestionDO();
		question.setCommunityUser(communityUser);
		question.setProduct(product);
		question.setStatus(ContentsStatus.SUBMITTED);
		question.setQuestionBody("質問本文");

		//質問を投稿で登録します。
		questionService.saveQuestion(question);

		QuestionAnswerDO questionAnswer = new QuestionAnswerDO();
		questionAnswer.setAnswerBody("質問回答");
		questionAnswer.setCommunityUser(communityUser2);
		questionAnswer.setQuestion(question);
		questionAnswer.setStatus(ContentsStatus.SUBMITTED);
		questionAnswer.setPurchaseDate(salesDate);
		questionService.saveQuestionAnswer(questionAnswer);
		
		CommentDO comment = new CommentDO();
		comment.setTargetType(CommentTargetType.QUESTION_ANSWER);
		comment.setCommentBody("テストコメントテストコメントテストコ");
		comment.setQuestionAnswer(questionAnswer);
		comment.setCommunityUser(communityUser2);
		commentService.saveComment(comment);
		
		solrOperations.deleteByKey(QuestionDO.class, question.getQuestionId());

		SearchResult<InformationDO> history = informationDao.findNoReadInformationByCommunityUserId(communityUser.getCommunityUserId(), 100, 0);
		
		assertTrue(history.getDocuments().get(0).getQuestionAnswer() != null);
		
	}
	@Test
	public void infomationImageHeaderTest(){
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO communityUser2 = createCommunityUser("テスト2", false);
		product = productService.getProductBySku("200000002000012355").getProduct();
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser, receiptJanCode, 
				salesDate);

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
		imageService.saveImageSet(product.getSku(), imageHeaders, salesDate);

		likeService.updateLikeImage(communityUser2.getCommunityUserId(), imageHeaders.get(0).getImageId(), false);

		CommentDO comment = new CommentDO();
		comment.setTargetType(CommentTargetType.IMAGE);
		comment.setCommentBody("テストコメントテストコメントテストコ");
		comment.setImageHeader(imageHeaders.get(0));
		comment.setCommunityUser(communityUser2);
		commentService.saveComment(comment);

		solrOperations.deleteByKey(ImageHeaderDO.class, imageHeaders.get(0).getImageId());

		SearchResult<InformationDO> history = informationDao.findNoReadInformationByCommunityUserId(communityUser.getCommunityUserId(), 100, 0);
		
		assertTrue(history.getDocuments().get(0).getImageHeader() != null);

	}

	@Test
	public void infomationProductMasterTest(){
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

		SearchResult<InformationDO> history = informationDao.findNoReadInformationByCommunityUserId(communityUser.getCommunityUserId(), 100, 0);
		
		assertTrue(history.getDocuments().get(0).getProductMaster() != null);

		
	}
	
	@Test
	public void infomationLikeTest(){
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO communityUser2 = createCommunityUser("テスト2", false);
		product = productService.getProductBySku("200000002000012355").getProduct();
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser, receiptJanCode, 
				salesDate);

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
		imageHeader.setPurchaseDate(salesDate);

		requestScopeDao.initialize(communityUser, null);
		imageService.saveImageSet(product.getSku(), imageHeaders, salesDate);

		likeService.updateLikeImage(communityUser2.getCommunityUserId(), imageHeaders.get(0).getImageId(), false);

		for(LikeDO like:solrOperations.findByQuery(new SolrQuery("*:*"), LikeDO.class).getDocuments()){
			solrOperations.deleteByKey(LikeDO.class, like.getLikeId());	
		}
		solrOperations.deleteByKey(ImageHeaderDO.class, imageHeaders.get(0).getImageId());

		SearchResult<InformationDO> history = informationDao.findNoReadInformationByCommunityUserId(communityUser.getCommunityUserId(), 100, 0);
		
		assertTrue(history.getDocuments().get(0).getLike() != null);


		
	}

	@Test
	public void infomationCommentTest(){
		CommunityUserDO communityUser = createCommunityUser("テスト", false);
		CommunityUserDO communityUser2 = createCommunityUser("テスト2", false);
		product = productService.getProductBySku("100000001000624829").getProduct();
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser, receiptJanCode, salesDate);

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
		imageHeader.setPurchaseDate(salesDate);

		requestScopeDao.initialize(communityUser, null);
		imageService.saveImageSet(product.getSku(), imageHeaders, salesDate);

		likeService.updateLikeImage(communityUser2.getCommunityUserId(), imageHeaders.get(0).getImageId(), false);

		CommentDO comment = new CommentDO();
		comment.setTargetType(CommentTargetType.IMAGE);
		comment.setCommentBody("テストコメントテストコメントテストコ");
		comment.setImageHeader(imageHeaders.get(0));
		comment.setCommunityUser(communityUser2);
		commentService.saveComment(comment);

		solrOperations.deleteByKey(ImageHeaderDO.class, imageHeaders.get(0).getImageId());

		SearchResult<InformationDO> history = informationDao.findNoReadInformationByCommunityUserId(communityUser.getCommunityUserId(), 100, 0);
		
		assertTrue(history.getDocuments().get(0).getComment() != null);

	}
}
