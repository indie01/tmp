package com.kickmogu.yodobashi.community.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.lib.core.time.SystemTime;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.dao.ActionHistoryDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.domain.AbstractReviewDO.PointGrantRequestDetail;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.DecisivePurchaseDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewPointSpecial;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewQuestPoint;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseLostProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDecisivePurchaseDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.SlipDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.UsedProductDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.AlsoBuyProduct;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommentTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.DeliverType;
import com.kickmogu.yodobashi.community.resource.domain.constants.EffectiveSlipType;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;
import com.kickmogu.yodobashi.community.resource.domain.constants.LikeTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.OrderEntryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointIncentiveType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointQuestType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ProductSatisfaction;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SalesRegistDetailType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SlipDetailCategory;
import com.kickmogu.yodobashi.community.service.config.ServiceConfig;

/**
 * レビューサービスのテストクラスです。
 *
 * @author hirabayashi
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/jcclientContext.xml")
public class ReviewServiceTest extends BaseTest {

	/**
	 * レビューサービスです。
	 */
	@Autowired
	private ReviewService reviewService;

	@Autowired
	private SystemTime SystemTime;

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
	private CommunityUserDO commentUser;
	private CommunityUserDO likeUser;
	private CommunityUserDO followerUser;
	private CommunityUserDO productFollowUser;

	private ReviewDecisivePurchaseDO reviewDecisivePurchase1;
	private ReviewDecisivePurchaseDO reviewDecisivePurchase2;
	private ReviewDecisivePurchaseDO reviewDecisivePurchase3;
	private PurchaseLostProductDO purchaseLostProduct1;
	private PurchaseLostProductDO purchaseLostProduct2;
	private PurchaseLostProductDO purchaseLostProduct3;
	private UsedProductDO usedProduct1;
	private UsedProductDO usedProduct2;
	private UsedProductDO usedProduct3;

	@Autowired  @Qualifier("default")

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
		createCommunityUserSet();
	}

	@After
	public void teardown() {
		SystemTime.adjustTime(0, TimeUnit.DAYS);
	}

	@Test
	public void testGetReviewQstPoint() {
		ProductDO product1 = productDao.loadProduct("100000001000624829");
		PurchaseProductDO purchaseProduct = new PurchaseProductDO();
		purchaseProduct.setOrderDate(getDate("2012/01/25"));
		purchaseProduct.setPurchaseDate(getDate("2012/02/10"));
		Date pointBaseDate = getDate("2012/02/21");
		ReviewQuestPoint questPoint1 = ReviewDO.getReviewQstPoint(
				PointQuestType.IMMEDIATELY_AFTER_DECISIVE_PURCHASE,
				product1, purchaseProduct, pointBaseDate);
		assertNotNull(questPoint1);
		assertEquals(
				PointIncentiveType.IMMEDIATELY_AFTER_DECISIVE_PURCHASE.getCode(),
				questPoint1.getRvwQstCd());
		assertEquals(10L, questPoint1.getRvwQstBasePoint().longValue());
		assertEquals(ReviewPointSpecial.POINT_TYPE_ADD,
				questPoint1.getReviewQuestPointSpecial().getPtTyp().intValue());
		assertEquals(ReviewPointSpecial.SPECIAL_COND_TYPE_TERM,
				questPoint1.getReviewQuestPointSpecial().getRvwSpTyp().intValue());
		assertEquals(80L,
				questPoint1.getReviewQuestPointSpecial().getRvwSpPoint().longValue());

		ReviewQuestPoint questPoint2 = ReviewDO.getReviewQstPoint(
				PointQuestType.IMMEDIATELY_AFTER_PURCHASE_LOST_PRODUCT,
				product1, purchaseProduct, pointBaseDate);
		assertNotNull(questPoint2);
		assertEquals(
				PointIncentiveType.IMMEDIATELY_AFTER_PURCHASE_LOST_PRODUCT.getCode(),
				questPoint2.getRvwQstCd());
		assertEquals(9L, questPoint2.getRvwQstBasePoint().longValue());
		assertEquals(ReviewPointSpecial.POINT_TYPE_ADD,
				questPoint2.getReviewQuestPointSpecial().getPtTyp().intValue());
		assertEquals(ReviewPointSpecial.SPECIAL_COND_TYPE_FIRST,
				questPoint2.getReviewQuestPointSpecial().getRvwSpTyp().intValue());
		assertEquals(60L,
				questPoint2.getReviewQuestPointSpecial().getRvwSpPoint().longValue());

		ReviewQuestPoint questPoint3 = ReviewDO.getReviewQstPoint(
				PointQuestType.IMMEDIATELY_AFTER_USED_PRODUCT,
				product1, purchaseProduct, pointBaseDate);
		assertNotNull(questPoint3);
		assertEquals(
				PointIncentiveType.IMMEDIATELY_AFTER_USED_PRODUCT.getCode(),
				questPoint3.getRvwQstCd());
		assertEquals(8L, questPoint3.getRvwQstBasePoint().longValue());
		assertEquals(ReviewPointSpecial.POINT_TYPE_ADD,
				questPoint3.getReviewQuestPointSpecial().getPtTyp().intValue());
		assertEquals(ReviewPointSpecial.SPECIAL_COND_TYPE_TERM,
				questPoint3.getReviewQuestPointSpecial().getRvwSpTyp().intValue());
		assertEquals(10L,
				questPoint3.getReviewQuestPointSpecial().getRvwSpPoint().longValue());

		ReviewQuestPoint questPoint4 = ReviewDO.getReviewQstPoint(
				PointQuestType.IMMEDIATELY_AFTER_REVIEW,
				product1, purchaseProduct, pointBaseDate);
		assertNotNull(questPoint4);
		assertEquals(
				PointIncentiveType.IMMEDIATELY_AFTER_REVIEW.getCode(),
				questPoint4.getRvwQstCd());
		assertEquals(7L, questPoint4.getRvwQstBasePoint().longValue());
		assertNull(questPoint4.getReviewQuestPointSpecial());

		pointBaseDate = getDate("2012/02/27");
		ReviewQuestPoint questPoint5 = ReviewDO.getReviewQstPoint(
				PointQuestType.AFTER_FEW_DAYS_ALSO_BUY,
				product1, purchaseProduct, pointBaseDate);
		assertNotNull(questPoint5);
		assertEquals(
				PointIncentiveType.AFTER_FEW_DAYS_ALSO_BUY.getCode(),
				questPoint5.getRvwQstCd());
		assertEquals(6L, questPoint5.getRvwQstBasePoint().longValue());
		assertNull(questPoint5.getReviewQuestPointSpecial());

		pointBaseDate = getDate("2012/03/27");
		ReviewQuestPoint questPoint6 = ReviewDO.getReviewQstPoint(
				PointQuestType.AFTER_FEW_DAYS_REVIEW,
				product1, purchaseProduct, pointBaseDate);
		assertNotNull(questPoint6);
		assertEquals(
				PointIncentiveType.AFTER_FEW_DAYS_REVIEW.getCode(),
				questPoint6.getRvwQstCd());
		assertEquals(5L, questPoint6.getRvwQstBasePoint().longValue());
		assertNull(questPoint6.getReviewQuestPointSpecial());

		pointBaseDate = getDate("2012/04/27");
		ReviewQuestPoint questPoint7 = ReviewDO.getReviewQstPoint(
				PointQuestType.AFTER_FEW_DAYS_SATISFACTION,
				product1, purchaseProduct, pointBaseDate);
		assertNotNull(questPoint7);
		assertEquals(
				PointIncentiveType.AFTER_FEW_DAYS_SATISFACTION.getCode(),
				questPoint7.getRvwQstCd());
		assertEquals(4L, questPoint7.getRvwQstBasePoint().longValue());
		assertNull(questPoint7.getReviewQuestPointSpecial());

		ProductDO product2 = productDao.loadProduct("200000002000012355");

		ReviewQuestPoint questPoint8 = ReviewDO.getReviewQstPoint(
				PointQuestType.PRODUCT_POINT,
				product2, purchaseProduct, pointBaseDate);
		assertNotNull(questPoint8);
		assertNull(questPoint8.getRvwQstCd());
		assertNull(questPoint8.getRvwQstBasePoint());
		assertEquals(ReviewPointSpecial.POINT_TYPE_REPLACE,
				questPoint8.getReviewQuestPointSpecial().getPtTyp().intValue());
		assertEquals(ReviewPointSpecial.SPECIAL_COND_TYPE_TERM,
				questPoint8.getReviewQuestPointSpecial().getRvwSpTyp().intValue());
		assertEquals(800L,
				questPoint8.getReviewQuestPointSpecial().getRvwSpPoint().longValue());

	}

	
	@Test
	public void testGrantReviewQstPoint() {
		ProductDO product = null;
		CommunityUserDO communityUser = null;
/*
		try{
			product = productDao.loadProduct("100000001000827415");
			communityUser = createCommunityUser("ポイント付与フラグOFF", false);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -5);
			// 注文履歴を登録します。
			createReviewSlip(communityUser, product.getJan(), OrderEntryType.EC, SlipDetailCategory.NORMAL, cal.getTime(), cal.getTime());
			// 登録した注文履歴を検証します。
			PurchaseProductDO purchaseProduct = getPurchaseProductReceipt(communityUser, cal.getTime());
			// 経過レビューを登録・検証します。
			submitReview(initReview(purchaseProduct, ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE, ContentsStatus.SUBMITTED));
			
			assertTrue(false);
		}catch (IllegalArgumentException e) {
			assertEquals("This product can not review. sku=" + product.getSku(), e.getMessage());
		}
		
		try{
			product = productDao.loadProduct("100000001000937211");
			communityUser = createCommunityUser("ポイント付与フラグON マスタ不整合", false);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -65);
			// 注文履歴を登録します。
			createReviewSlip(communityUser, product.getJan(), OrderEntryType.EC, SlipDetailCategory.NORMAL, cal.getTime(), cal.getTime());
			// 登録した注文履歴を検証します。
			PurchaseProductDO purchaseProduct = getPurchaseProductReceipt(communityUser, cal.getTime());
			// 経過レビューを登録・検証します。
			submitReview(initReview(purchaseProduct, ReviewType.REVIEW_AFTER_FEW_DAYS, ContentsStatus.SUBMITTED));
			
			assertTrue(false);
		}catch (IllegalArgumentException e) {
			assertEquals("This product can not review. sku=" + product.getSku(), e.getMessage());
		}

		try{
			product = productDao.loadProduct("100000001000965304");
			communityUser = createCommunityUser("ポイント付与フラグON 期間切れ", false);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -65);
			// 注文履歴を登録します。
			createReviewSlip(communityUser, product.getJan(), OrderEntryType.EC, SlipDetailCategory.NORMAL, cal.getTime(), cal.getTime());
			// 登録した注文履歴を検証します。
			PurchaseProductDO purchaseProduct = getPurchaseProductReceipt(communityUser, cal.getTime());
			// 経過レビューを登録・検証します。
			submitReview(initReview(purchaseProduct, ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE, ContentsStatus.SUBMITTED));
			
			assertTrue(false);
		}catch (IllegalArgumentException e) {
			assertEquals("This product can not review. sku=" + product.getSku(), e.getMessage());
		}

		{
			product = productDao.loadProduct("100000001001033690");
			communityUser = createCommunityUser("設問のみ設定 購入直後", false);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -5);
			
			
			// 注文履歴を登録します。
			createReviewSlip(communityUser, product.getJan(), OrderEntryType.EC, SlipDetailCategory.NORMAL, cal.getTime(), cal.getTime());

			// 登録した注文履歴を検証します。
			PurchaseProductDO purchaseProduct = getPurchaseProductReceipt(communityUser, cal.getTime());
			// 経過レビューを登録・検証します。
			ReviewDO review = submitReview(initReview(purchaseProduct, ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE, ContentsStatus.SUBMITTED));
			
			assertNotNull(review.getPointGrantRequestId());
			assertNotNull(review.getPointGrantRequestDetails());
			
			long pointSummary = 0;
			for(PointGrantRequestDetail pointGrantRequestDetail :review.getPointGrantRequestDetails()){
				System.out.println(pointGrantRequestDetail.getType().getCode() + ":" + pointGrantRequestDetail.getPoint());
				pointSummary += pointGrantRequestDetail.getPoint();
			}
			
			assertEquals(163L, pointSummary);
		}
		{
			product = productDao.loadProduct("100000001001033690");
			communityUser = createCommunityUser("設問のみ設定 X日経過後", false);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -65);
			// 注文履歴を登録します。
			createReviewSlip(communityUser, product.getJan(), OrderEntryType.EC, SlipDetailCategory.NORMAL, cal.getTime(), cal.getTime());
			// 登録した注文履歴を検証します。
			PurchaseProductDO purchaseProduct = getPurchaseProductReceipt(communityUser, cal.getTime());
			// 経過レビューを登録・検証します。
			ReviewDO review = submitReview(initReview(purchaseProduct, ReviewType.REVIEW_AFTER_FEW_DAYS, ContentsStatus.SUBMITTED));
			
			assertNotNull(review.getPointGrantRequestId());
			assertNotNull(review.getPointGrantRequestDetails());

			long pointSummary = 0;
			for(PointGrantRequestDetail pointGrantRequestDetail :review.getPointGrantRequestDetails()){
				System.out.println(pointGrantRequestDetail.getType().getCode() + ":" + pointGrantRequestDetail.getPoint());
				pointSummary += pointGrantRequestDetail.getPoint();
			}
			
			assertEquals(263168L, pointSummary);

		}

		{
			product = productDao.loadProduct("100000001001074944");
			communityUser = createCommunityUser("設問 特別設定ヘッダー情報　 購入直後", false);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -5);
			// 注文履歴を登録します。
			createReviewSlip(communityUser, product.getJan(), OrderEntryType.EC, SlipDetailCategory.NORMAL, cal.getTime(), cal.getTime()); 
			// 登録した注文履歴を検証します。
			PurchaseProductDO purchaseProduct = getPurchaseProductReceipt(communityUser, cal.getTime());
			// 経過レビューを登録・検証します。
			ReviewDO review = submitReview(initReview(purchaseProduct, ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE, ContentsStatus.SUBMITTED));
			
			assertNotNull(review.getPointGrantRequestId());
			assertNotNull(review.getPointGrantRequestDetails());
			
			long pointSummary = 0;
			for(PointGrantRequestDetail pointGrantRequestDetail :review.getPointGrantRequestDetails()){
				System.out.println(pointGrantRequestDetail.getType().getCode() + ":" + pointGrantRequestDetail.getPoint());
				pointSummary += pointGrantRequestDetail.getPoint();
			}
			
			assertEquals(150995010L, pointSummary);
		}
		{
			product = productDao.loadProduct("100000001001074944");
			communityUser = createCommunityUser("設問 特別設定ヘッダー情報　  X日経過後", false);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -65);
			// 注文履歴を登録します。
			createReviewSlip(communityUser, product.getJan(), OrderEntryType.EC, SlipDetailCategory.NORMAL, cal.getTime(), cal.getTime());
			// 登録した注文履歴を検証します。
			PurchaseProductDO purchaseProduct = getPurchaseProductReceipt(communityUser, cal.getTime());
			// 経過レビューを登録・検証します。
			ReviewDO review = submitReview(initReview(purchaseProduct, ReviewType.REVIEW_AFTER_FEW_DAYS, ContentsStatus.SUBMITTED));
			
			assertNotNull(review.getPointGrantRequestId());
			assertNotNull(review.getPointGrantRequestDetails());

			long pointSummary = 0;
			for(PointGrantRequestDetail pointGrantRequestDetail :review.getPointGrantRequestDetails()){
				System.out.println(pointGrantRequestDetail.getType().getCode() + ":" + pointGrantRequestDetail.getPoint());
				pointSummary += pointGrantRequestDetail.getPoint();
			}
			assertEquals(526336L, pointSummary);
		}

		
		{
			product = productDao.loadProduct("100000001001079012");
			communityUser = createCommunityUser("設問 特別条件設定 詳細条件　 購入直後", false);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -5);
			// 注文履歴を登録します。
			createReviewSlip(communityUser, product.getJan(), OrderEntryType.EC, SlipDetailCategory.NORMAL, cal.getTime(), cal.getTime()); 
			// 登録した注文履歴を検証します。
			PurchaseProductDO purchaseProduct = getPurchaseProductReceipt(communityUser, cal.getTime());
			// 経過レビューを登録・検証します。
			ReviewDO review = submitReview(initReview(purchaseProduct, ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE, ContentsStatus.SUBMITTED));
			
			assertNotNull(review.getPointGrantRequestId());
			assertNotNull(review.getPointGrantRequestDetails());
			
			long pointSummary = 0;
			for(PointGrantRequestDetail pointGrantRequestDetail :review.getPointGrantRequestDetails()){
				System.out.println(pointGrantRequestDetail.getType().getCode() + ":" + pointGrantRequestDetail.getPoint());
				pointSummary += pointGrantRequestDetail.getPoint();
			}
			
			assertEquals(150995010L, pointSummary);
		}

		
		{
			product = productDao.loadProduct("100000001001079012");
			communityUser = createCommunityUser("設問 特別条件設定 詳細条件　 X日経過後", false);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -65);
			// 注文履歴を登録します。
			createReviewSlip(communityUser, product.getJan(), OrderEntryType.EC, SlipDetailCategory.NORMAL, cal.getTime(), cal.getTime()); 
			// 登録した注文履歴を検証します。
			PurchaseProductDO purchaseProduct = getPurchaseProductReceipt(communityUser, cal.getTime());
			// 経過レビューを登録・検証します。
			ReviewDO review = submitReview(initReview(purchaseProduct, ReviewType.REVIEW_AFTER_FEW_DAYS, ContentsStatus.SUBMITTED));
			
			assertNotNull(review.getPointGrantRequestId());
			assertNotNull(review.getPointGrantRequestDetails());
			
			long pointSummary = 0;
			for(PointGrantRequestDetail pointGrantRequestDetail :review.getPointGrantRequestDetails()){
				System.out.println(pointGrantRequestDetail.getType().getCode() + ":" + pointGrantRequestDetail.getPoint());
				pointSummary += pointGrantRequestDetail.getPoint();
			}
			assertEquals(262145L, pointSummary);
		}
		{
			product = productDao.loadProduct("100000001001188153");
			communityUser = createCommunityUser("設問 特別条件設定 詳細条件　 購入直後2", false);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -5);
			// 注文履歴を登録します。
			createReviewSlip(communityUser, product.getJan(), OrderEntryType.EC, SlipDetailCategory.NORMAL, cal.getTime(), cal.getTime()); 
			// 登録した注文履歴を検証します。
			PurchaseProductDO purchaseProduct = getPurchaseProductReceipt(communityUser, cal.getTime());
			// 経過レビューを登録・検証します。
			ReviewDO review = submitReview(initReview(purchaseProduct, ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE, ContentsStatus.SUBMITTED));
			
			assertNotNull(review.getPointGrantRequestId());
			assertNotNull(review.getPointGrantRequestDetails());
			
			long pointSummary = 0;
			for(PointGrantRequestDetail pointGrantRequestDetail :review.getPointGrantRequestDetails()){
				System.out.println(pointGrantRequestDetail.getType().getCode() + ":" + pointGrantRequestDetail.getPoint());
				pointSummary += pointGrantRequestDetail.getPoint();
			}
			
			assertEquals(7L, pointSummary);
		}


		{
			product = productDao.loadProduct("100000001001220335");
			communityUser = createCommunityUser("品目特別設定　 単一設定", false);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -5);
			// 注文履歴を登録します。
			createReviewSlip(communityUser, product.getJan(), OrderEntryType.EC, SlipDetailCategory.NORMAL, cal.getTime(), cal.getTime()); 
			// 登録した注文履歴を検証します。
			PurchaseProductDO purchaseProduct = getPurchaseProductReceipt(communityUser, cal.getTime());
			// 経過レビューを登録・検証します。
			ReviewDO review = submitReview(initReview(purchaseProduct, ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE, ContentsStatus.SUBMITTED));
			
			assertNotNull(review.getPointGrantRequestId());
			assertNotNull(review.getPointGrantRequestDetails());
			
			long pointSummary = 0;
			for(PointGrantRequestDetail pointGrantRequestDetail :review.getPointGrantRequestDetails()){
				System.out.println(pointGrantRequestDetail.getType().getCode() + ":" + pointGrantRequestDetail.getPoint());
				pointSummary += pointGrantRequestDetail.getPoint();
			}
			
			assertEquals(164L, pointSummary);
		}
		
		{
			product = productDao.loadProduct("100000001001325327");
			communityUser = createCommunityUser("品目特別設定　 複数設定", false);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -5);
			// 注文履歴を登録します。
			createReviewSlip(communityUser, product.getJan(), OrderEntryType.EC, SlipDetailCategory.NORMAL, cal.getTime(), cal.getTime()); 
			// 登録した注文履歴を検証します。
			PurchaseProductDO purchaseProduct = getPurchaseProductReceipt(communityUser, cal.getTime());
			// 経過レビューを登録・検証します。
			ReviewDO review = submitReview(initReview(purchaseProduct, ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE, ContentsStatus.SUBMITTED));
			
			assertNotNull(review.getPointGrantRequestId());
			assertNotNull(review.getPointGrantRequestDetails());
			
			long pointSummary = 0;
			for(PointGrantRequestDetail pointGrantRequestDetail :review.getPointGrantRequestDetails()){
				System.out.println(pointGrantRequestDetail.getType().getCode() + ":" + pointGrantRequestDetail.getPoint());
				pointSummary += pointGrantRequestDetail.getPoint();
			}
			
			assertEquals(1L, pointSummary);
		}

		{
			product = productDao.loadProduct("100000001001363481");
			communityUser = createCommunityUser("品目特別設定　 複数設定2", false);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -5);
			// 注文履歴を登録します。
			createReviewSlip(communityUser, product.getJan(), OrderEntryType.EC, SlipDetailCategory.NORMAL, cal.getTime(), cal.getTime()); 
			// 登録した注文履歴を検証します。
			PurchaseProductDO purchaseProduct = getPurchaseProductReceipt(communityUser, cal.getTime());
			// 経過レビューを登録・検証します。
			ReviewDO review = submitReview(initReview(purchaseProduct, ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE, ContentsStatus.SUBMITTED));
			
			assertNotNull(review.getPointGrantRequestId());
			assertNotNull(review.getPointGrantRequestDetails());
			
			long pointSummary = 0;
			for(PointGrantRequestDetail pointGrantRequestDetail :review.getPointGrantRequestDetails()){
				System.out.println(pointGrantRequestDetail.getType().getCode() + ":" + pointGrantRequestDetail.getPoint());
				pointSummary += pointGrantRequestDetail.getPoint();
			}
			
			assertEquals(2L, pointSummary);
		}
		{
			product = productDao.loadProduct("100000001001237299");
			communityUser = createCommunityUser("品目特別設定　 複数設定3", false);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -5);
			// 注文履歴を登録します。
			createReviewSlip(communityUser, product.getJan(), OrderEntryType.EC, SlipDetailCategory.NORMAL, cal.getTime(), cal.getTime()); 
			// 登録した注文履歴を検証します。
			PurchaseProductDO purchaseProduct = getPurchaseProductReceipt(communityUser, cal.getTime());
			// 経過レビューを登録・検証します。
			ReviewDO review = submitReview(initReview(purchaseProduct, ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE, ContentsStatus.SUBMITTED));
			
			assertNotNull(review.getPointGrantRequestId());
			assertNotNull(review.getPointGrantRequestDetails());
			
			long pointSummary = 0;
			for(PointGrantRequestDetail pointGrantRequestDetail :review.getPointGrantRequestDetails()){
				System.out.println(pointGrantRequestDetail.getType().getCode() + ":" + pointGrantRequestDetail.getPoint());
				pointSummary += pointGrantRequestDetail.getPoint();
			}
			
			assertEquals(163L, pointSummary);
		}

		{
			product = productDao.loadProduct("100000001001377916");
			communityUser = createCommunityUser("品目特別設定　 複数設定4", false);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -5);
			// 注文履歴を登録します。
			createReviewSlip(communityUser, product.getJan(), OrderEntryType.EC, SlipDetailCategory.NORMAL, cal.getTime(), cal.getTime()); 
			// 登録した注文履歴を検証します。
			PurchaseProductDO purchaseProduct = getPurchaseProductReceipt(communityUser, cal.getTime());
			// 経過レビューを登録・検証します。
			ReviewDO review = submitReview(initReview(purchaseProduct, ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE, ContentsStatus.SUBMITTED));
			
			assertNotNull(review.getPointGrantRequestId());
			assertNotNull(review.getPointGrantRequestDetails());
			
			long pointSummary = 0;
			for(PointGrantRequestDetail pointGrantRequestDetail :review.getPointGrantRequestDetails()){
				System.out.println(pointGrantRequestDetail.getType().getCode() + ":" + pointGrantRequestDetail.getPoint());
				pointSummary += pointGrantRequestDetail.getPoint();
			}
			
			assertEquals(163L, pointSummary);
		}

		{
			product = productDao.loadProduct("100000001001377918");
			communityUser = createCommunityUser("品目特別設定　 複数設定5", false);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -5);
			// 注文履歴を登録します。
			createReviewSlip(communityUser, product.getJan(), OrderEntryType.EC, SlipDetailCategory.NORMAL, cal.getTime(), cal.getTime()); 
			// 登録した注文履歴を検証します。
			PurchaseProductDO purchaseProduct = getPurchaseProductReceipt(communityUser, cal.getTime());
			// 経過レビューを登録・検証します。
			ReviewDO review = submitReview(initReview(purchaseProduct, ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE, ContentsStatus.SUBMITTED));
			
			assertNotNull(review.getPointGrantRequestId());
			assertNotNull(review.getPointGrantRequestDetails());
			
			long pointSummary = 0;
			for(PointGrantRequestDetail pointGrantRequestDetail :review.getPointGrantRequestDetails()){
				System.out.println(pointGrantRequestDetail.getType().getCode() + ":" + pointGrantRequestDetail.getPoint());
				pointSummary += pointGrantRequestDetail.getPoint();
			}
			
			assertEquals(4L, pointSummary);
		}
*/
		{
			product = productDao.loadProduct("100000001001274334");
			communityUser = createCommunityUser("品目特別設定　 複数設定6", false);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -5);
			// 注文履歴を登録します。
			createReviewSlip(communityUser, product.getJan(), OrderEntryType.EC, SlipDetailCategory.NORMAL, cal.getTime(), cal.getTime()); 
			// 登録した注文履歴を検証します。
			PurchaseProductDO purchaseProduct = getPurchaseProductReceipt(communityUser, cal.getTime());
			// 経過レビューを登録・検証します。
			ReviewDO review = submitReview(initReview(purchaseProduct, ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE, ContentsStatus.SUBMITTED));
			
			assertNotNull(review.getPointGrantRequestId());
			assertNotNull(review.getPointGrantRequestDetails());
			
			long pointSummary = 0;
			for(PointGrantRequestDetail pointGrantRequestDetail :review.getPointGrantRequestDetails()){
				System.out.println(pointGrantRequestDetail.getType().getCode() + ":" + pointGrantRequestDetail.getPoint());
				pointSummary += pointGrantRequestDetail.getPoint();
			}
			
			assertEquals(167L, pointSummary);
		}
		
	}
	
	public static long slipNo=0; 
	private void createReviewSlip(CommunityUserDO communityUser, String janCode, OrderEntryType orderEntryType, SlipDetailCategory slipDetailCategory, Date entryDate, Date billingDate) {
		SlipHeaderDO slipHeader = new SlipHeaderDO();
		slipHeader.setDeliverType(DeliverType.SHOP);
		slipHeader.setEffectiveSlipType(EffectiveSlipType.EFFECTIVE);
		slipHeader.setOrderEntryDate(entryDate);
		slipHeader.setOrderEntryType(orderEntryType);
		slipHeader.setOuterCustomerId(communityUser.getCommunityId());
		slipHeader.setSlipNo("" + Long.valueOf(slipNo++));
		hBaseOperations.save(slipHeader);
		solrOperations.save(slipHeader);

		SlipDetailDO slipDetail = new SlipDetailDO();
		slipDetail.setOldestBillingDate(billingDate);
		slipDetail.setEffectiveNum(1);
		slipDetail.setHeader(slipHeader);
		slipDetail.setJanCode(janCode);
		slipDetail.setOuterCustomerId(communityUser.getCommunityId());
		slipDetail.setSalesRegistDetailType(SalesRegistDetailType.EFFECTIVE);
		slipDetail.setSlipDetailCategory(slipDetailCategory);
		slipDetail.setSlipDetailNo(10);
		slipDetail.setSlipNo(slipHeader.getSlipNo());
		hBaseOperations.save(slipDetail);
		solrOperations.save(slipDetail);

		aggregateReviewOrder(communityUser.getCommunityId());
	}

	private void aggregateReviewOrder(String outerCustomerId) {
		List<String> outerCustomerIds = new ArrayList<String>();
		outerCustomerIds.add(outerCustomerId);
		aggregateOrderService.aggregateOrder(outerCustomerIds);
	}

	
	
	
	/**
	 * 登録した注文履歴を取得します。
	 *
	 * @param communityUser
	 * @param salesDate
	 * @return
	 */
	private PurchaseProductDO getPurchaseProductReceipt(
			CommunityUserDO communityUser, Date salesDate) {
		List<PurchaseProductDO> hBasePurchaseProductResults = getHBasePurchaseProducts(communityUser);
		
		System.out.println(">>>>>>>>>>>>>>>>>>>" + hBasePurchaseProductResults.size());
		
		assertEquals(1, hBasePurchaseProductResults.size());
		PurchaseProductDO hBasePurchaseProductResult = hBasePurchaseProductResults
				.get(0);
		return hBasePurchaseProductResult;
	}
	
	
	private ReviewDO initReview(
			PurchaseProductDO purchaseProduct, 
			ReviewType reviewType,
			ContentsStatus contentsStatus){

		ReviewDO review = new ReviewDO();
		review.setReviewType(reviewType);
		review.setStatus(contentsStatus);
		review.setCommunityUser(purchaseProduct.getCommunityUser());
		review.setProduct(purchaseProduct.getProduct());
		review.setReviewBody("review");

		List<ReviewDecisivePurchaseDO> l = new ArrayList<ReviewDecisivePurchaseDO>();
		for(int i=0;i<3;i++){
			ReviewDecisivePurchaseDO r1 =new ReviewDecisivePurchaseDO();
			r1.setDecisivePurchase(new DecisivePurchaseDO());
			r1.getDecisivePurchase().setDecisivePurchaseName("購入の決め手");
			l.add(r1);
		}
		review.setReviewDecisivePurchases(l);
		review.setNoUsedProductFlag(false);
		List<UsedProductDO> usedProducts = new ArrayList<UsedProductDO>();
		for(int s=0;s<3;s++){
			UsedProductDO usedProduct = new UsedProductDO();
			usedProduct.setProductName("昔使っていた商品");
			usedProducts.add(usedProduct);
		}
		review.setUsedProducts(usedProducts);
		review.setNoLostProductFlag(false);
		List<PurchaseLostProductDO> lostProducts = new ArrayList<PurchaseLostProductDO>();
		for(int s=0;s<3;s++){
			PurchaseLostProductDO lostProduct = new PurchaseLostProductDO();
			lostProduct.setProductName("購入を迷った商品");
			lostProducts.add(lostProduct);
		}
		review.setPurchaseLostProducts(lostProducts);

		review.setAlsoBuyProduct(AlsoBuyProduct.MAYNOTBUY);
		review.setProductSatisfaction(ProductSatisfaction.FOUR);
		return review;
	}
	
	/**
	 * 経過後レビューの登録・検証を行います。
	 *
	 * @param purchaseProduct
	 */
	private ReviewDO submitReview(ReviewDO review) {
		ReviewDO saveReview = reviewService.saveReview(review);
		return saveReview;
	}

	
	
	
	
	/**
	 * レビュー投稿を検証します。 Slip購入 - 経過レビュー
	 */
	@Test
	public void testSaveAfterReviewForSubmitBySlip() {
		Date entryDate = getDate("2012/02/22");
		Date billingDate = getDate("2012/02/22");
		// 注文履歴を登録します。
		createSlip(communityUser, slipJanCode, entryDate, billingDate);
		// 登録した注文履歴を検証します。
		PurchaseProductDO purchaseProduct = checkPurchaseProductSlip(
				communityUser, entryDate, billingDate);
		// 経過レビュー(一時保存)を登録・検証します。
		ReviewDO review = null;
		ReviewDO saveReview = testSaveReviewForSubmitAfter(purchaseProduct, ContentsStatus.SAVE, "経過レビュー投稿一時保存", review);
		// レビューヒストリーが追加されていないことを確認します。
		testReviewHistory(saveReview, 0);
		// 経過レビュー(一時保存)を編集します。
		saveReview = testSaveReviewForSubmitAfter(purchaseProduct, ContentsStatus.SAVE, "経過レビュー投稿一時保存編集", review);
		// レビューヒストリーが追加されていないことを確認します。
		testReviewHistory(saveReview, 0);
		// 経過レビュー(一時保存)を削除します。
		testDeleteReview(saveReview);
		// 経過レビュー(一時保存)を再登録・検証します。
		testSaveReviewForSubmitAfter(purchaseProduct, ContentsStatus.SAVE, "経過レビュー再投稿一時保存", review);
		// 経過レビューを登録・検証します。
		saveReview = testSaveReviewForSubmitAfter(purchaseProduct, ContentsStatus.SUBMITTED, "経過レビュー投稿", review);
		// レビューヒストリーが追加されていることを確認します。
		testReviewHistory(saveReview, 1);
		// アクションヒストリーを検証します。
		checkCreateReviewActionHistory(saveReview);
		// コメントを登録・検証します。
		CommentDO comment = null;
		CommentDO saveComment = createComment(saveReview, comment, "コメント登録");
		// コメントのアクションヒストリーを検証します。
		checkCreateReviewCommentActionHistory(saveComment);
		// いいねを登録・検証します。
		createLike(saveReview, false);
		// いいねを解除・検証します。
		createLike(saveReview, true);
		// いいね50件以上を登録・検証します。
		createLike50(saveReview);
		// 経過レビューを編集します。
		saveReview = testSaveReviewForSubmitAfter(purchaseProduct, ContentsStatus.SUBMITTED, "経過レビュー編集", saveReview);
		// レビューヒストリーが増えていることを確認します。
		testReviewHistory(saveReview, 2);
		// レビューを削除します。
		testDeleteReview(saveReview);
		// レビューヒストリーが削除されていないことを確認します。
		testReviewHistory(saveReview, 2);
		// 経過レビューを新規に登録・検証します。
		saveReview = testSaveReviewForSubmitAfter(purchaseProduct, ContentsStatus.SUBMITTED, "経過レビュー別投稿", review);
		// 経過レビューを新規に登録・検証します。
		testSaveReviewForSubmitAfter(purchaseProduct, ContentsStatus.SUBMITTED, "経過レビュー別投稿(二件目)", review);
		// コメントを登録・検証します。
		saveComment = createComment(saveReview, comment, "コメント登録2");
		// コメントを登録・検証します。
		saveComment = createComment(saveReview, saveComment, "コメント編集");
		// コメントの削除を検証します。
		deleteComment(saveComment);
		// 日付を変更して登録・検証します。
		SystemTime.adjustTime(10, TimeUnit.DAYS);
		testSaveReviewForSubmitAfter(purchaseProduct, ContentsStatus.SUBMITTED, "経過レビュー日付変更", review);
	}

	/**
	 * レビュー投稿を検証します。 Receipt購入 - 経過レビュー
	 */
	@Test
	public void testSaveAfterReviewForSubmitByReceipt() {
		Date salesDate = getDate("2011/10/01");
		// 注文履歴を登録します。
		createReceipt(communityUser, receiptJanCode, salesDate);
		// 登録した注文履歴を検証します。
		PurchaseProductDO purchaseProduct = checkPurchaseProductReceipt(
				communityUser, salesDate);
		ReviewDO review = null;
		// 経過レビュー(一時保存)を登録・検証します。
		ReviewDO saveReview = testSaveReviewForSubmitAfter(purchaseProduct, ContentsStatus.SAVE, "経過レビュー投稿一時保存", review);
		// レビューヒストリーが追加されていないことを確認します。
		testReviewHistory(saveReview, 0);
		// 経過レビュー(一時保存)を編集します。
		saveReview = testSaveReviewForSubmitAfter(purchaseProduct, ContentsStatus.SAVE, "経過レビュー投稿一時保存編集", review);
		// レビューヒストリーが追加されていないことを確認します。
		testReviewHistory(saveReview, 0);
		// 経過レビュー(一時保存)を削除します。
		testDeleteReview(saveReview);
		// 経過レビュー(一時保存)を再登録・検証します。
		testSaveReviewForSubmitAfter(purchaseProduct, ContentsStatus.SAVE, "経過レビュー再投稿一時保存", review);
		// 経過レビューを登録・検証します。
		saveReview = testSaveReviewForSubmitAfter(purchaseProduct, ContentsStatus.SUBMITTED, "経過レビュー投稿", review);
		// レビューヒストリーが追加されていることを確認します。
		testReviewHistory(saveReview, 1);
		// アクションヒストリーを検証します。
		checkCreateReviewActionHistory(saveReview);
		// コメントを登録・検証します。
		CommentDO comment = null;
		CommentDO saveComment = createComment(saveReview, comment, "コメント登録");
		// コメントのアクションヒストリーを検証します。
		checkCreateReviewCommentActionHistory(saveComment);
		// いいねを登録・検証します。
		createLike(saveReview, false);
		// いいねを解除・検証します。
		createLike(saveReview, true);
		// いいね50件以上を登録・検証します。
		createLike50(saveReview);
		// 経過レビューを編集します。
		testSaveReviewForSubmitAfter(purchaseProduct, ContentsStatus.SUBMITTED, "経過レビュー編集", saveReview);
		// レビューヒストリーが追加いることを確認します。
		testReviewHistory(saveReview, 2);
		// レビューを削除します。
		testDeleteReview(saveReview);
		// レビューヒストリーが削除されていないことを確認します。
		testReviewHistory(saveReview, 2);
		// 経過レビューを新規に登録・検証します。
		saveReview = testSaveReviewForSubmitAfter(purchaseProduct, ContentsStatus.SUBMITTED, "経過レビュー別投稿", review);
		// 経過レビューを新規に登録・検証します。
		testSaveReviewForSubmitAfter(purchaseProduct, ContentsStatus.SUBMITTED, "経過レビュー別投稿(二件目)", review);
		// コメントを登録・検証します。
		saveComment = createComment(saveReview, comment, "コメント登録");
		// コメントを編集・検証します。
		saveComment = createComment(saveReview, saveComment, "コメント編集");
		// コメントの削除を検証します。
		deleteComment(saveComment);
		// 日付を変更して登録・検証します。
		SystemTime.adjustTime(10, TimeUnit.DAYS);
		testSaveReviewForSubmitAfter(purchaseProduct, ContentsStatus.SUBMITTED, "経過レビュー日付変更", review);
	}

	/**
	 * レビュー投稿を検証します。 Slip購入 - 購入直後レビュー
	 */
	@Test
	public void testSaveImmediatelyReviewForSubmitBySlip() {
		Date entryDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(entryDate);
		calendar.add(Calendar.DATE, -1);
		Date billingDate = calendar.getTime();
		// 注文履歴を登録します。
		createSlip(communityUser, slipJanCode, entryDate, billingDate);
		// 登録した注文履歴を検証します。
		PurchaseProductDO purchaseProduct = checkPurchaseProductSlip(
				communityUser, entryDate, billingDate);
		// 購入直後レビュー(一時保存)を登録・検証します。
		ReviewDO review = null;
		ReviewDO saveReview = testSaveReviewForSubmitImmediately(purchaseProduct, ContentsStatus.SAVE, "購入直後レビュー投稿一時保存", review);
		// レビューヒストリーが追加されていないことを確認します。
		testReviewHistory(saveReview, 0);
		// 購入直後レビュー(一時保存)を編集します。
		saveReview = testSaveReviewForSubmitImmediately(purchaseProduct, ContentsStatus.SAVE, "購入直後レビュー投稿一時保存編集", review);
		// レビューヒストリーが追加されていないことを確認します。
		testReviewHistory(saveReview, 0);
		// 購入直後レビュー(一時保存)を削除します。
		testDeleteReview(saveReview);
		// 購入直後レビュー(一時保存)を再登録・検証します。
		testSaveReviewForSubmitImmediately(purchaseProduct, ContentsStatus.SAVE, "購入直後レビュー投稿一時保存再登録", review);
		// 購入直後レビューを登録・検証します。
		saveReview = testSaveReviewForSubmitImmediately(purchaseProduct, ContentsStatus.SUBMITTED, "購入直後レビュー投稿", review);
		// レビューヒストリーが追加されていることを確認します。
		testReviewHistory(saveReview, 1);
		// アクションヒストリーを検証します。
		checkCreateReviewActionHistory(saveReview);
		// コメントを登録・検証します。
		CommentDO comment = null;
		CommentDO saveComment = createComment(saveReview, comment, "コメント登録");
		// コメントのアクションヒストリーを検証します。
		checkCreateReviewCommentActionHistory(saveComment);
		// いいねを登録・検証します。
		createLike(saveReview, false);
		// いいねを解除・検証します。
		createLike(saveReview, true);
		// いいね50件以上を登録・検証します。
		createLike50(saveReview);
		// 購入直後レビューを編集・検証します。
		testSaveReviewForSubmitImmediately(purchaseProduct, ContentsStatus.SUBMITTED, "購入直後レビュー編集", saveReview);
		// レビューヒストリーが追加されていることを確認します。
		testReviewHistory(saveReview, 2);
		// 購入の決め手情報/購入を迷った商品情報/過去に使っていた商品に追加します。
		saveReview = testUpdateReviewProduct(saveReview, false);
		// レビューヒストリーが追加されていることを確認します。
		testReviewHistory(saveReview, 3);
		// 購入の決め手情報/購入を迷った商品情報/過去に使っていた商品を削除して追加します。
		saveReview = testUpdateReviewProduct(saveReview, true);
		// レビューヒストリーが追加されていることを確認します。
		testReviewHistory(saveReview, 4);
		// レビュー投稿不可能か検証します。
		assertEquals(false, reviewService.canPostReview(purchaseProduct
				.getCommunityUser().getCommunityUserId(), purchaseProduct
				.getProduct().getSku(),
				ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE));
		// レビューを削除します。
		testDeleteReview(saveReview);
		// レビューヒストリーが削除されていないことを確認します。
		testReviewHistory(saveReview, 4);
		// レビュー投稿不可能か検証します。
		assertEquals(false, reviewService.canPostReview(purchaseProduct
				.getCommunityUser().getCommunityUserId(), purchaseProduct
				.getProduct().getSku(),
				ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE));
		// 経過レビューを新規に登録・検証します。
		saveReview = testSaveReviewForSubmitAfter(purchaseProduct, ContentsStatus.SUBMITTED, "経過レビュー投稿", review);
		// コメントを登録・検証します。
		saveComment = createComment(saveReview, comment, "コメント登録");
		// コメントを編集・検証します。
		saveComment = createComment(saveReview, saveComment, "コメント編集");
		// コメントの削除を検証します。
		deleteComment(saveComment);
		// 日付を変更して登録・検証します。
		SystemTime.adjustTime(10, TimeUnit.DAYS);
		testSaveReviewForSubmitAfter(purchaseProduct, ContentsStatus.SUBMITTED, "経過レビュー日付変更", review);
	}

	/**
	 * レビュー投稿を検証します。 Receipt購入 - 購入直後レビュー
	 */
	@Test
	public void testSaveImmediatelyReviewForSubmitByReceipt() {
		Date salesDate = new Date();
		// 注文履歴を登録します。
		createReceipt(communityUser, receiptJanCode, salesDate);
		// 登録した注文履歴を検証します。
		PurchaseProductDO purchaseProduct = checkPurchaseProductReceipt(
				communityUser, salesDate);
		// 購入直後レビュー(一時保存)を登録・検証します。
		ReviewDO review = null;
		ReviewDO saveReview = testSaveReviewForSubmitImmediately(purchaseProduct, ContentsStatus.SAVE, "購入直後レビュー投稿一時保存", review);
		// レビューヒストリーが追加されていないことを確認します。
		testReviewHistory(saveReview, 0);
		// 購入直後レビュー(一時保存)を編集します。
		saveReview = testSaveReviewForSubmitImmediately(purchaseProduct, ContentsStatus.SAVE, "購入直後レビュー投稿一時保存編集", review);
		// レビューヒストリーが追加されていないことを確認します。
		testReviewHistory(saveReview, 0);
		// 購入直後レビューを登録・検証します。
		saveReview = testSaveReviewForSubmitImmediately(purchaseProduct, ContentsStatus.SUBMITTED, "購入直後レビュー投稿", review);
		// レビューヒストリーが追加されていることを確認します。
		testReviewHistory(saveReview, 1);
		// アクションヒストリーを検証します。
		checkCreateReviewActionHistory(saveReview);
		// コメントを登録・検証します。
		CommentDO comment = null;
		CommentDO saveComment = createComment(saveReview, comment, "コメント登録");
		// コメントのアクションヒストリーを検証します。
		checkCreateReviewCommentActionHistory(saveComment);
		// いいねを登録・検証します。
		createLike(saveReview, false);
		// いいねを解除・検証します。
		createLike(saveReview, true);
		// いいね50件以上を登録・検証します。
		createLike50(saveReview);
		// 購入直後レビューを編集します。
		testSaveReviewForSubmitImmediately(purchaseProduct, ContentsStatus.SUBMITTED, "購入直後レビュー編集", saveReview);
		// レビューヒストリーが追加されていることを確認します。
		testReviewHistory(saveReview, 2);
		// 購入の決め手情報/購入を迷った商品情報/過去に使っていた商品に追加します。
		saveReview = testUpdateReviewProduct(saveReview, false);
		// レビューヒストリーが追加されていることを確認します。
		testReviewHistory(saveReview, 3);
		// 購入の決め手情報/購入を迷った商品情報/過去に使っていた商品を削除して追加します。
		saveReview = testUpdateReviewProduct(saveReview, true);
		// レビューヒストリーが追加されていることを確認します。
		testReviewHistory(saveReview, 4);
		// レビュー投稿不可能か検証します。
		assertEquals(false, reviewService.canPostReview(purchaseProduct
				.getCommunityUser().getCommunityUserId(), purchaseProduct
				.getProduct().getSku(),
				ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE));
		// レビューを削除します。
		testDeleteReview(saveReview);
		// レビューヒストリーが削除されていないことを確認します。
		testReviewHistory(saveReview, 4);
		// レビュー投稿不可能か検証します。
		assertEquals(false, reviewService.canPostReview(purchaseProduct
				.getCommunityUser().getCommunityUserId(), purchaseProduct
				.getProduct().getSku(),
				ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE));
		// 経過レビューを新規に登録・検証します。
		saveReview = testSaveReviewForSubmitAfter(purchaseProduct, ContentsStatus.SUBMITTED, "経過レビュー投稿", review);
		// コメントを登録・検証します。
		saveComment = createComment(saveReview, comment, "コメント登録");
		// コメントを編集・検証します。
		saveComment = createComment(saveReview, saveComment, "コメント編集");
		// コメントの削除を検証します。
		deleteComment(saveComment);
		// 日付を変更して登録・検証します。
		SystemTime.adjustTime(10, TimeUnit.DAYS);
		testSaveReviewForSubmitAfter(purchaseProduct, ContentsStatus.SUBMITTED, "経過レビュー日付変更", review);
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
		// レビュー投稿するコミュニティユーザー、コメントユーザーをフォローします。
		assertTrue(followService.followCommunityUser(
				followerUser.getCommunityUserId(),
				communityUser.getCommunityUserId(), false));
		assertTrue(followService.followCommunityUser(
				followerUser.getCommunityUserId(),
				commentUser.getCommunityUserId(), false));
		// レビューされる商品をフォローします。
		assertTrue(followService.followProduct(
				productFollowUser.getCommunityUserId(), "100000001000624829",
				false));
		assertTrue(followService.followProduct(
				productFollowUser.getCommunityUserId(), "200000002000012355",
				false));
	}

	/**
	 * コメントをSolrから取得します。
	 * @param review
	 * @return
	 */
	private SearchResult<CommentDO> getSolrCommentsByReviewId(ReviewDO review) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("reviewId_s:");
		buffer.append(review.getReviewId());
		SolrQuery query = new SolrQuery(buffer.toString());
		SearchResult<CommentDO> comments = new SearchResult<CommentDO>(
				solrOperations.findByQuery(query, CommentDO.class, commentPath));
		return comments;
	}

	/**
	 * 登録した注文履歴を検証します。
	 *
	 * @param communityUser
	 * @param entryDate
	 * @param billingDate
	 * @return
	 */
	private PurchaseProductDO checkPurchaseProductSlip(
			CommunityUserDO communityUser, Date entryDate, Date billingDate) {
		SearchResult<PurchaseProductDO> solrPurchaseProductResults = getSolrPurchaseProductsByCommunityUser(communityUser);
		List<PurchaseProductDO> hBasePurchaseProductResults = getHBasePurchaseProducts(communityUser);

		assertEquals(1, hBasePurchaseProductResults.size());
		assertEquals(1, solrPurchaseProductResults.getDocuments().size());

		PurchaseProductDO solrPurchaseProductResult = solrPurchaseProductResults.getDocuments().get(0);
		PurchaseProductDO hBasePurchaseProductResult = hBasePurchaseProductResults.get(0);

		assertEquals(slipJanCode, hBasePurchaseProductResult.getJanCode());
		assertEquals(slipJanCode, solrPurchaseProductResult.getJanCode());

		assertEquals(entryDate, solrPurchaseProductResult.getOrderDate());
		assertEquals(billingDate, solrPurchaseProductResult.getPurchaseDate());
		assertEquals(billingDate, solrPurchaseProductResult.getBillingDate());

		assertEquals(entryDate, hBasePurchaseProductResult.getOrderDate());
		assertEquals(billingDate, hBasePurchaseProductResult.getPurchaseDate());
		assertEquals(billingDate, hBasePurchaseProductResult.getBillingDate());

		return solrPurchaseProductResult;
	}

	/**
	 * 登録した注文履歴を検証します。
	 *
	 * @param communityUser
	 * @param salesDate
	 * @return
	 */
	private PurchaseProductDO checkPurchaseProductReceipt(
			CommunityUserDO communityUser, Date salesDate) {
		SearchResult<PurchaseProductDO> solrPurchaseProductResults = getSolrPurchaseProductsByCommunityUser(communityUser);
		List<PurchaseProductDO> hBasePurchaseProductResults = getHBasePurchaseProducts(communityUser);

		assertEquals(1, hBasePurchaseProductResults.size());
		assertEquals(1, solrPurchaseProductResults.getDocuments().size());

		PurchaseProductDO solrPurchaseProductResult = solrPurchaseProductResults
				.getDocuments().get(0);
		PurchaseProductDO hBasePurchaseProductResult = hBasePurchaseProductResults
				.get(0);

		assertEquals(receiptJanCode, hBasePurchaseProductResult.getJanCode());
		assertEquals(receiptJanCode, solrPurchaseProductResult.getJanCode());

		assertEquals(salesDate, solrPurchaseProductResult.getOrderDate());
		assertEquals(salesDate, solrPurchaseProductResult.getPurchaseDate());
		assertEquals(salesDate, solrPurchaseProductResult.getBillingDate());

		assertEquals(salesDate, hBasePurchaseProductResult.getOrderDate());
		assertEquals(salesDate, hBasePurchaseProductResult.getPurchaseDate());
		assertEquals(salesDate, hBasePurchaseProductResult.getBillingDate());

		return hBasePurchaseProductResult;
	}

	/**
	 * 経過後レビューの登録・検証を行います。
	 *
	 * @param purchaseProduct
	 */
	private ReviewDO testSaveReviewForSubmitAfter(
			PurchaseProductDO purchaseProduct, ContentsStatus contentsStatus, String reviewText, ReviewDO review) {
		if(review==null){
			// レビューを登録します。
			review = reviewService.getTemporaryReview(purchaseProduct.getCommunityUser().getCommunityUserId(),
					purchaseProduct.getProduct().getSku(),
					ReviewType.REVIEW_AFTER_FEW_DAYS);
			if(review==null) {
				review = new ReviewDO();
			}
			review.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
			review.setStatus(contentsStatus);
			review.setCommunityUser(purchaseProduct.getCommunityUser());
			review.setProduct(purchaseProduct.getProduct());
		}

		createImage(communityUser);
		createImageTwo(communityUser);
		String tempImageUrl = resourceConfig.temporaryImageUrl + image.getImageId();
		String tempImageUrl2 = resourceConfig.temporaryImageUrl + image2.getImageId();
		StringBuilder html = new StringBuilder();
		html.append("<img src=\"" + tempImageUrl + "\">");
		html.append("<script type=\"text/javascript\">alert('" + reviewText + "本文" + "')</script>");
		html.append("<img src=\"" + tempImageUrl2 + "\">");
		review.setReviewBody(html.toString());
		review.setAlsoBuyProduct(AlsoBuyProduct.MAYNOTBUY);
		review.setProductSatisfaction(ProductSatisfaction.FOUR);
		ReviewDO saveReview = reviewService.saveReview(review);
		// 登録したレビューを取得します。
		ReviewDO reviewByHBase = hBaseOperations.load(ReviewDO.class,
				saveReview.getReviewId(), reviewPath);
		ReviewDO reviewBySolr = solrOperations.load(ReviewDO.class,
				saveReview.getReviewId(), reviewPath);
		// HBaseから取得したレビューを検証します。
		testCheckReviewForSubmitAfter(review, reviewByHBase, purchaseProduct);
		// Solrから取得したレビューを検証します。
		testCheckReviewForSubmitAfter(review, reviewBySolr, purchaseProduct);
		return saveReview;
	}

	/**
	 * 経過後レビューの検証を行います。
	 *
	 * @param review
	 * @param saveReview
	 * @param purchaseProduct
	 */
	private void testCheckReviewForSubmitAfter(ReviewDO review,
			ReviewDO saveReview, PurchaseProductDO purchaseProduct) {
		assertEquals(review.getReviewType(), saveReview.getReviewType());
		assertEquals(review.getStatus(), saveReview.getStatus());
		assertEquals(review.getCommunityUser().getCommunityUserId(), saveReview
				.getCommunityUser().getCommunityUserId());
		assertEquals(review.getProduct().getSku(), saveReview.getProduct()
				.getSku());
		assertEquals(review.getReviewBody(), saveReview.getReviewBody());
		assertEquals(review.getAlsoBuyProduct(), saveReview.getAlsoBuyProduct());
		assertEquals(review.getProductSatisfaction(),
				saveReview.getProductSatisfaction());
		assertEquals(purchaseProduct.getProduct().isAdult(), saveReview.isAdult());
		assertEquals(purchaseProduct.getPurchaseDate(), review.getPurchaseDate()); //購入日付
		if(ContentsStatus.SUBMITTED == review.getStatus()) {
			assertNotNull(review.getPostDate()); //投稿日
		}
		if(ContentsStatus.DELETE == review.getStatus()) {
			assertNotNull(review.getDeleteDate()); //削除日
		} else {
			assertEquals(null, review.getDeleteDate());
		}
		assertNotNull(review.getPointBaseDate()); //ポイント計算基準用の日付
		assertNotNull(review.getSaveDate()); //保存日時(アプリケーション日付)
		assertNotNull(review.getModifyDateTime()); //更新日時(システム日付)
		assertEquals(review.getSaveDate(), review.getModifyDateTime()); //本テストでは同一のものが入っているケースしかないため一致

		ImageDO hBaseImage = hBaseOperations.load(ImageDO.class, image.getImageId());
		ImageHeaderDO hBaseImageHeader = hBaseOperations.load(ImageHeaderDO.class, image.getImageId());
		ImageHeaderDO solrImageHeader = solrOperations.load(ImageHeaderDO.class, image.getImageId());
		ImageDO hBaseImage2 = hBaseOperations.load(ImageDO.class, image2.getImageId());
		ImageHeaderDO hBaseImageHeader2 = hBaseOperations.load(ImageHeaderDO.class, image2.getImageId());
		ImageHeaderDO solrImageHeader2 = solrOperations.load(ImageHeaderDO.class, image2.getImageId());
		assertEquals(image.getImageId(), hBaseImage.getImageId());
		assertEquals(image2.getImageId(), hBaseImage2.getImageId());
		if(ContentsStatus.SAVE == review.getStatus()) {
			assertTrue(hBaseImageHeader == null);
			assertTrue(hBaseImageHeader2 == null);
			assertTrue(solrImageHeader == null);
			assertTrue(solrImageHeader2 == null);
		} else if(ContentsStatus.SUBMITTED == review.getStatus()) {
			checkImageHeader(hBaseImageHeader, review, true);
			checkImageHeader(hBaseImageHeader2, review, false);
			checkImageHeader(solrImageHeader, review, true);
			checkImageHeader(solrImageHeader2, review, false);
		}
	}

	/**
	 * ImageHeaderのデータを検証します。
	 * @param imageHeader
	 * @param review
	 * @param listViewFlag
	 */
	private void checkImageHeader(ImageHeaderDO imageHeader, ReviewDO review, boolean listViewFlag) {
		assertTrue(imageHeader != null);
		assertEquals(communityUser.getCommunityUserId(), imageHeader.getOwnerCommunityUserId());
		assertEquals(PostContentType.REVIEW, imageHeader.getPostContentType());
		assertEquals(review.getReviewId(), imageHeader.getReview().getReviewId());
		assertEquals(listViewFlag, imageHeader.isListViewFlag());
	}

	/**
	 * 購入直後レビューの登録・検証を行います。
	 *
	 * @param purchaseProduct
	 */
	private ReviewDO testSaveReviewForSubmitImmediately(
			PurchaseProductDO purchaseProduct, ContentsStatus contentsStatus, String reviewText, ReviewDO review) {
		if(review==null) {
			// レビュー投稿可能か検証します。
			assertEquals(true, reviewService.canPostReview(purchaseProduct
					.getCommunityUser().getCommunityUserId(), purchaseProduct
					.getProduct().getSku(),
					ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE));
			// レビューを登録します。
			review = reviewService.getTemporaryReview(purchaseProduct.getCommunityUser().getCommunityUserId(),
					purchaseProduct.getProduct().getSku(),
					ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE);
			if(review==null) {
				review = new ReviewDO();
				review.setReviewType(ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE);
				review.setCommunityUser(purchaseProduct.getCommunityUser());
				review.setProduct(purchaseProduct.getProduct());
				// レビューの購入の決め手情報
				reviewDecisivePurchase1 = new ReviewDecisivePurchaseDO();
				reviewDecisivePurchase1.setDecisivePurchase(new DecisivePurchaseDO());
				reviewDecisivePurchase1.getDecisivePurchase().setDecisivePurchaseName(
						"購入の決め手-デザイン");
				reviewDecisivePurchase2 = new ReviewDecisivePurchaseDO();
				reviewDecisivePurchase2.setDecisivePurchase(new DecisivePurchaseDO());
				reviewDecisivePurchase2.getDecisivePurchase().setDecisivePurchaseName(
						"購入の決め手-使い勝手");
				review.getReviewDecisivePurchases().add(reviewDecisivePurchase1);
				review.getReviewDecisivePurchases().add(reviewDecisivePurchase2);
				// 購入を迷った商品情報
				// 過去に使っていた商品
				purchaseLostProduct1 = new PurchaseLostProductDO();
				purchaseLostProduct2 = new PurchaseLostProductDO();
				usedProduct1 = new UsedProductDO();
				usedProduct2 = new UsedProductDO();
				if(product.getSku().equals(purchaseProduct.getProduct().getSku())) {
					purchaseLostProduct1.setProduct(product2);
					usedProduct1.setProduct(product2);
				} else {
					purchaseLostProduct1.setProduct(product);
					usedProduct1.setProduct(product);
				}
				purchaseLostProduct2.setProductName("テスト商品");
				usedProduct2.setProductName("テスト商品");
				review.getPurchaseLostProducts().add(purchaseLostProduct1);
				review.getPurchaseLostProducts().add(purchaseLostProduct2);
				review.getUsedProducts().add(usedProduct1);
				review.getUsedProducts().add(usedProduct2);
			}
		}
		review.setStatus(contentsStatus);
		createImage(communityUser);
		createImageTwo(communityUser);
		String tempImageUrl = resourceConfig.temporaryImageUrl + image.getImageId();
		String tempImageUrl2 = resourceConfig.temporaryImageUrl + image2.getImageId();
		StringBuilder html = new StringBuilder();
		html.append("<img src=\"" + tempImageUrl + "\">");
		html.append("<script type=\"text/javascript\">alert('" + reviewText + "本文" + "')</script>");
		html.append("<img src=\"" + tempImageUrl2 + "\">");
		review.setReviewBody(html.toString());
		ReviewDO saveReview = reviewService.saveReview(review);

		// レビュー投稿不可能か検証します。
		if(ContentsStatus.SAVE != contentsStatus) {
			assertEquals(false, reviewService.canPostReview(purchaseProduct
					.getCommunityUser().getCommunityUserId(), purchaseProduct
					.getProduct().getSku(),
					ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE));
		}
		// 登録したレビューを取得します。
		ReviewDO reviewBySolr = solrOperations.load(ReviewDO.class,
				saveReview.getReviewId(), reviewPath);
		ReviewDO reviewByHBase = hBaseOperations.load(ReviewDO.class,
				saveReview.getReviewId(), reviewPath);
		// HBaseから取得したレビューを検証します。
		testReviewForSubmitImmediately(review, reviewByHBase, purchaseProduct);
		// Solrから取得したレビューを検証します。
		testReviewForSubmitImmediately(review, reviewBySolr, purchaseProduct);
		return saveReview;
	}

	/**
	 * 購入の決め手情報
	 * 購入を迷った商品情報
	 * 過去に使っていた商品
	 *  の更新を行います。
	 */
	private ReviewDO testUpdateReviewProduct(ReviewDO review, boolean dalateContents) {
		review = hBaseOperations.load(ReviewDO.class,
				review.getReviewId(), reviewPath);
		List<ReviewDecisivePurchaseDO> saveReviewDecisivePurchase =
				review.getReviewDecisivePurchases();
		List<PurchaseLostProductDO> savePurchaseLostProduct = review.getPurchaseLostProducts();
		List<UsedProductDO> saveUsedProduct = review.getUsedProducts();
		if(dalateContents) {
			review.getReviewDecisivePurchases().clear();
			review.getPurchaseLostProducts().clear();
			review.getUsedProducts().clear();
		}
		// レビューの購入の決め手情報
		reviewDecisivePurchase3 = new ReviewDecisivePurchaseDO();
		reviewDecisivePurchase3.setDecisivePurchase(new DecisivePurchaseDO());
		reviewDecisivePurchase3.getDecisivePurchase().setDecisivePurchaseName("購入の決め手-色");
		// 購入を迷った商品情報
		// 過去に使っていた商品
		purchaseLostProduct3 = new PurchaseLostProductDO();
		usedProduct3 = new UsedProductDO();
		purchaseLostProduct3.setProduct(product4);
		usedProduct3.setProduct(product4);
		review.getReviewDecisivePurchases().add(reviewDecisivePurchase3);
		review.getPurchaseLostProducts().add(purchaseLostProduct3);
		review.getUsedProducts().add(usedProduct3);
		ReviewDO saveReview = reviewService.saveReview(review);
		checkUpdateReviewProduct(saveReview, saveReviewDecisivePurchase,
				savePurchaseLostProduct, saveUsedProduct, dalateContents);
		review = hBaseOperations.load(ReviewDO.class,
				review.getReviewId(), reviewPath);
		return review;
	}

	private void checkUpdateReviewProduct(
			ReviewDO saveReview,
			List<ReviewDecisivePurchaseDO> saveReviewDecisivePurchase,
			List<PurchaseLostProductDO> savePurchaseLostProduct,
			List<UsedProductDO> saveUsedProduct,
			boolean dalateContents) {
		if(dalateContents) {
			assertEquals(1, saveReview.getReviewDecisivePurchases().size());
			assertEquals(1, saveReview.getPurchaseLostProducts().size());
			assertEquals(1, saveReview.getUsedProducts().size());
			// 購入の決め手
			assertEquals(reviewDecisivePurchase3.getTemporaryDecisivePurchaseName(),
					saveReview.getReviewDecisivePurchases().get(0).getTemporaryDecisivePurchaseName());
			// 購入を迷った商品情報
			assertEquals(purchaseLostProduct3.getProductName(),
					saveReview.getPurchaseLostProducts().get(0).getProductName());
			// 過去に使っていた商品
			assertEquals(usedProduct3.getProductName(),
					saveReview.getUsedProducts().get(0).getProductName());
		} else {
			assertEquals(3, saveReview.getReviewDecisivePurchases().size());
			assertEquals(3, saveReview.getPurchaseLostProducts().size());
			assertEquals(3, saveReview.getUsedProducts().size());
			// 購入の決め手
			assertEquals(reviewDecisivePurchase1.getTemporaryDecisivePurchaseName(),
					saveReview.getReviewDecisivePurchases().get(0).getTemporaryDecisivePurchaseName());
			assertEquals(reviewDecisivePurchase2.getTemporaryDecisivePurchaseName(),
					saveReview.getReviewDecisivePurchases().get(1).getTemporaryDecisivePurchaseName());
			assertEquals(reviewDecisivePurchase3.getTemporaryDecisivePurchaseName(),
					saveReview.getReviewDecisivePurchases().get(2).getTemporaryDecisivePurchaseName());
			// 購入を迷った商品情報
			assertEquals(purchaseLostProduct1.getProduct().getSku(),
					saveReview.getPurchaseLostProducts().get(0).getProduct().getSku());
			assertEquals(purchaseLostProduct2.getProductName(),
					saveReview.getPurchaseLostProducts().get(1).getProductName());
			assertEquals(purchaseLostProduct3.getProductName(),
					saveReview.getPurchaseLostProducts().get(2).getProductName());
			// 過去に使っていた商品
			assertEquals(usedProduct1.getProduct().getSku(),
					saveReview.getUsedProducts().get(0).getProduct().getSku());
			assertEquals(usedProduct2.getProductName(),
					saveReview.getUsedProducts().get(1).getProductName());
			assertEquals(usedProduct3.getProductName(),
					saveReview.getUsedProducts().get(2).getProductName());
		}


	}

	/**
	 * 購入直後レビューの検証を行います。
	 *
	 * @param review
	 * @param saveReview
	 * @param purchaseProduct
	 */
	private void testReviewForSubmitImmediately(ReviewDO review,
			ReviewDO saveReview, PurchaseProductDO purchaseProduct) {
		assertEquals(review.getReviewType(), saveReview.getReviewType());
		assertEquals(review.getStatus(), saveReview.getStatus());
		assertEquals(review.getCommunityUser().getCommunityUserId(), saveReview
				.getCommunityUser().getCommunityUserId());
		assertEquals(review.getProduct().getSku(), saveReview.getProduct()
				.getSku());
		assertEquals(review.getReviewBody(), saveReview.getReviewBody());
		assertEquals(purchaseProduct.getProduct().isAdult(), saveReview.isAdult());
		assertEquals(true, saveReview.isEffective());//有効データか検証します。
		//TODO
//		assertEquals(ElapsedMonth.PURCHASE, review.getElapsedMonth()); //経過月を検証します。
		assertEquals(purchaseProduct.getPurchaseDate(), review.getPurchaseDate()); //購入日付
		if(ContentsStatus.SUBMITTED == review.getStatus()) {
			assertNotNull(review.getPostDate()); //投稿日
		}
		if(ContentsStatus.DELETE == review.getStatus()) {
			assertNotNull(review.getDeleteDate()); //削除日
		} else {
			assertEquals(null, review.getDeleteDate());
		}
		assertNotNull(review.getPointBaseDate()); //ポイント計算基準用の日付
		assertNotNull(review.getSaveDate()); //保存日時(アプリケーション日付)
		assertNotNull(review.getModifyDateTime()); //更新日時(システム日付)
		assertEquals(review.getSaveDate(), review.getModifyDateTime()); //本テストでは同一のものが入っているケースしかないため一致

		// 購入の決め手
		assertEquals(2, saveReview.getReviewDecisivePurchases().size());
		assertEquals(review.getReviewDecisivePurchases().get(0)
				.getTemporaryDecisivePurchaseName(), saveReview
				.getReviewDecisivePurchases().get(0).getTemporaryDecisivePurchaseName());
		assertEquals(review.getReviewDecisivePurchases().get(1).getTemporaryDecisivePurchaseName(),
				saveReview.getReviewDecisivePurchases().get(1).getTemporaryDecisivePurchaseName());
		// 購入を迷った商品情報
		assertEquals(2, saveReview.getPurchaseLostProducts().size());
		assertEquals(purchaseLostProduct1.getProduct().getSku(),
				saveReview.getPurchaseLostProducts().get(0).getProduct().getSku());
		assertEquals(purchaseLostProduct2.getProductName(),
				saveReview.getPurchaseLostProducts().get(1).getProductName());
		// 過去に使っていた商品
		assertEquals(2, saveReview.getUsedProducts().size());
		assertEquals(usedProduct1.getProduct().getSku(),
				saveReview.getUsedProducts().get(0).getProduct().getSku());
		assertEquals(usedProduct2.getProductName(),
				saveReview.getUsedProducts().get(1).getProductName());
		// 購入の決め手・購入を迷った商品情報・過去に使っていた商品
		assertEquals(communityUser.getCommunityUserId(),
				review.getReviewDecisivePurchases().get(0).getCommunityUser().getCommunityUserId());
		assertEquals(communityUser.getCommunityUserId(),
				review.getReviewDecisivePurchases().get(1).getCommunityUser().getCommunityUserId());
		assertEquals(communityUser.getCommunityUserId(),
				review.getPurchaseLostProducts().get(0).getCommunityUser().getCommunityUserId());
		assertEquals(communityUser.getCommunityUserId(),
				review.getPurchaseLostProducts().get(1).getCommunityUser().getCommunityUserId());
		assertEquals(communityUser.getCommunityUserId(),
				review.getUsedProducts().get(0).getCommunityUser().getCommunityUserId());
		assertEquals(communityUser.getCommunityUserId(),
				review.getUsedProducts().get(1).getCommunityUser().getCommunityUserId());
		if(ContentsStatus.SAVE == saveReview.getStatus()) {
			assertTrue(review.getReviewDecisivePurchases().get(0).isTemporary());
			assertTrue(review.getReviewDecisivePurchases().get(1).isTemporary());
			assertTrue(review.getPurchaseLostProducts().get(0).isTemporary());
			assertTrue(review.getPurchaseLostProducts().get(1).isTemporary());
			assertTrue(review.getUsedProducts().get(0).isTemporary());
			assertTrue(review.getUsedProducts().get(1).isTemporary());

		}
		if(ContentsStatus.SUBMITTED == saveReview.getStatus()) {
			assertEquals(false, review.getReviewDecisivePurchases().get(0).isTemporary());
			assertEquals(false, review.getReviewDecisivePurchases().get(1).isTemporary());
			assertEquals(false, review.getPurchaseLostProducts().get(0).isTemporary());
			assertEquals(false, review.getPurchaseLostProducts().get(1).isTemporary());
			assertEquals(false, review.getUsedProducts().get(0).isTemporary());
			assertEquals(false, review.getUsedProducts().get(1).isTemporary());
		}
		assertTrue(review.getReviewDecisivePurchases().get(0).isEffective());
		assertTrue(review.getReviewDecisivePurchases().get(1).isEffective());
		assertTrue(review.getPurchaseLostProducts().get(0).isEffective());
		assertTrue(review.getPurchaseLostProducts().get(1).isEffective());
		assertTrue(review.getUsedProducts().get(0).isEffective());
		assertTrue(review.getUsedProducts().get(1).isEffective());
		assertEquals(false, review.getReviewDecisivePurchases().get(0).isDeleted());
		assertEquals(false, review.getReviewDecisivePurchases().get(1).isDeleted());
		assertEquals(false, review.getPurchaseLostProducts().get(0).isDeleted());
		assertEquals(false, review.getPurchaseLostProducts().get(1).isDeleted());
		assertEquals(false, review.getUsedProducts().get(0).isDeleted());
		assertEquals(false, review.getUsedProducts().get(1).isDeleted());
		//
		ImageHeaderDO hBaseImageHeader = hBaseOperations.load(ImageHeaderDO.class, image.getImageId());
		ImageHeaderDO solrImageHeader = solrOperations.load(ImageHeaderDO.class, image.getImageId());
		ImageDO hBaseImage = hBaseOperations.load(ImageDO.class, image.getImageId());
		ImageDO hBaseImage2 = hBaseOperations.load(ImageDO.class, image2.getImageId());
		ImageHeaderDO hBaseImageHeader2 = hBaseOperations.load(ImageHeaderDO.class, image2.getImageId());
		ImageHeaderDO solrImageHeader2 = solrOperations.load(ImageHeaderDO.class, image2.getImageId());
		assertEquals(image.getImageId(), hBaseImage.getImageId());
		assertEquals(image2.getImageId(), hBaseImage2.getImageId());
		if(ContentsStatus.SAVE == review.getStatus()) {
			assertTrue(hBaseImageHeader == null);
			assertTrue(hBaseImageHeader2 == null);
			assertTrue(solrImageHeader == null);
			assertTrue(solrImageHeader2 == null);
		} else if(ContentsStatus.SUBMITTED == review.getStatus()) {
			checkImageHeader(hBaseImageHeader, review, true);
			checkImageHeader(hBaseImageHeader2, review, false);
			checkImageHeader(solrImageHeader, review, true);
			checkImageHeader(solrImageHeader2, review, false);
		}
	}

	/**
	 * レビュー投稿時のアクションヒストリーを検証します。
	 *
	 * @param review
	 */
	private void checkCreateReviewActionHistory(ReviewDO review) {
		checkActionHistory(review.getCommunityUser(), review, ActionHistoryType.USER_REVIEW);
		checkActionHistory(review.getCommunityUser(), review, ActionHistoryType.PRODUCT_REVIEW);
	}

	/**
	 * レビューコメント投稿時のアクションヒストリーを検証します。
	 *
	 * @param review
	 */
	private void checkCreateReviewCommentActionHistory(CommentDO comment) {
		checkActionHistory(comment.getCommunityUser(), comment,
				ActionHistoryType.USER_REVIEW_COMMENT); // フォローしているユーザーがレビューにコメントをした
	}

	/**
	 * レビュー投稿時のアクションヒストリーを検証します。
	 *
	 * @param communityUser
	 * @param review
	 * @param actionHistoryType
	 */
	private void checkActionHistory(CommunityUserDO communityUser,
			ReviewDO review, ActionHistoryType actionHistoryType) {
		// アクションヒストリーをSolrから取得します。
		SearchResult<ActionHistoryDO> actionHistorisBySolr =
				getSolrActionHistorisByCommunityUserAndActionHistoryType(communityUser, actionHistoryType);
		ActionHistoryDO userActionHistoryBySolr = actionHistorisBySolr.getDocuments().get(0);
		// アクションヒストリーをHBaseから取得します。
		List<ActionHistoryDO> actionHistorisByHBase =
				getHBaseActionHistorisByCommunityUserAndActionHistoryType(communityUser, actionHistoryType);
		ActionHistoryDO userActionHistoryByHBase = actionHistorisByHBase.get(0);
		// アクションヒストリーを検証します。
		checkActionHistory(communityUser, review, userActionHistoryByHBase);
		checkActionHistory(communityUser, review, userActionHistoryBySolr);
	}

	/**
	 * アクションヒストリーを検証します。
	 *
	 * @param communityUser
	 * @param review
	 * @param actionHistory
	 */
	private void checkActionHistory(CommunityUserDO communityUser,
			ReviewDO review, ActionHistoryDO actionHistory) {
		assertEquals(communityUser.getCommunityUserId(), actionHistory
				.getCommunityUser().getCommunityUserId());
		assertEquals(review.getCommunityUser().getCommunityUserId(),
				actionHistory.getCommunityUser().getCommunityUserId());
		assertEquals(review.getReviewId(), actionHistory.getReview()
				.getReviewId());
		assertEquals(review.getProduct().isAdult(), actionHistory.isAdult());
		assertNotNull(actionHistory.getActionTime());
		assertNotNull(actionHistory.getRegisterDateTime());
		assertNotNull(actionHistory.getModifyDateTime());
	}

	/**
	 * コメントの登録・検証を行います。
	 *
	 * @param review
	 */
	private CommentDO createComment(ReviewDO review, CommentDO comment, String commentText) {
		if(comment==null) {
			comment = new CommentDO();
		}
		comment.setCommunityUser(commentUser);
		comment.setTargetType(CommentTargetType.REVIEW);
		comment.setReview(review);
		comment.setCommentBody(commentText);
		commentService.saveComment(comment);

		// コメントをSolrから取得します。
		SearchResult<CommentDO> commentsBySolr = getSolrCommentsByReviewId(review);
		assertEquals(1, commentsBySolr.getDocuments().size());
		CommentDO commentBySolr = commentsBySolr.getDocuments().get(0);
		// コメントをHBaseから取得します。
		Condition path = Path.includeProp("*").includePath(
				"communityUser.communityUserId_s,review.reviewId_s").depth(1);
		CommentDO commentByHBase = hBaseOperations.load(CommentDO.class,
				commentsBySolr.getDocuments().get(0).getCommentId(), path);
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
		assertEquals(comment.getReview().getReviewId(), checkComment
				.getReview().getReviewId());
		assertEquals(comment.getCommentBody(), checkComment.getCommentBody());
		assertEquals(comment.getReview().getCommunityUser()
				.getCommunityUserId(), checkComment.getRelationReviewOwnerId());
		if(checkComment.isWithdraw()) {
			assertNotNull(checkComment.getDeleteDate());
		} else {
			assertNotNull(checkComment.getPostDate());
			assertEquals(null, checkComment.getDeleteDate());
		}
	}

	/**
	 * いいねの登録・検証を行います。
	 *
	 * @param review
	 */
	private void createLike(ReviewDO review, boolean release) {
		// レビューに対していいねを登録・解除します。
		likeService.updateLikeReview(likeUser.getCommunityUserId(), review.getReviewId(), release);
		Condition path = Path.includeProp("*")
				.includePath("communityUser.communityUserId,review.reviewId").depth(1);
		// いいねをHBaseから取得します。
		List<LikeDO> likesByHBase = hBaseOperations
				.scanWithIndex(LikeDO.class, "communityUserId", likeUser.getCommunityUserId(),
						hBaseOperations.createFilterBuilder(LikeDO.class)
								.appendSingleColumnValueFilter("reviewId", CompareOp.EQUAL,
										review.getReviewId()).toFilter(), path);
		// いいねをSolrから取得します。
		StringBuilder buffer = new StringBuilder();
		buffer.append("reviewId_s:");
		buffer.append(review.getReviewId());
		buffer.append(" AND communityUserId_s:");
		buffer.append(likeUser.getCommunityUserId());
		SolrQuery query = new SolrQuery(buffer.toString());
		SearchResult<LikeDO> likesBySolr = new SearchResult<LikeDO>(
				solrOperations.findByQuery(query, LikeDO.class, path));

		path = Path.includeProp("*")
				.includePath("communityUser.communityUserId").depth(1);
		// お知らせをHBaseから取得します。
		List<InformationDO> informationsByHBase = hBaseOperations.scanWithIndex(InformationDO.class, "relationLikeOwnerId", likeUser.getCommunityUserId(),
				hBaseOperations.createFilterBuilder(InformationDO.class).appendSingleColumnValueFilter("communityUserId", CompareOp.EQUAL, review.getCommunityUser().getCommunityUserId()).toFilter(), path);
		// お知らせをSolrから取得します。
		buffer = new StringBuilder();
		buffer.append("relationLikeOwnerId_s:");
		buffer.append(likeUser.getCommunityUserId());
		buffer.append(" AND communityUserId_s:");
		buffer.append(review.getCommunityUser().getCommunityUserId());
		query = new SolrQuery(buffer.toString());
		SearchResult<InformationDO> informationsBySolr = new SearchResult<InformationDO>(
				solrOperations.findByQuery(query, InformationDO.class, path));
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
			checkLike(likeByHBase, review);
			checkLike(likeBySolr, review);
			assertEquals(1, likesBySolr.getDocuments().size());
			// お知らせの検証を行います。
			checkInformation(informationsByHBase.get(0), review, likeByHBase);
			checkInformation(informationsBySolr.getDocuments().get(0), review, likeBySolr);
			// 50件以上のアクションヒストリーが不要なことを確認。
			assertEquals(false, actionHistoryDao.requiredSaveLikeMilstone(likeBySolr, serviceConfig.likeThresholdLimit));
		}
	}

	/**
	 * いいねの検証を行います。
	 * @param like
	 */
	private void checkLike(LikeDO like, ReviewDO review) {
		assertEquals(likeUser.getCommunityUserId(), like.getCommunityUser().getCommunityUserId());
		assertEquals(review.getReviewId(), like.getReview().getReviewId());
		assertEquals(review.getCommunityUser().getCommunityUserId(), like.getRelationReviewOwnerId());
		assertEquals(review.getProduct().getSku(), like.getSku());
		assertEquals(like.getPostDate(), like.getModifyDateTime());
		assertEquals(LikeTargetType.REVIEW, like.getTargetType());
	}

	/**
	 * お知らせの検証を行います。
	 * @param information
	 * @param review
	 */
	private void checkInformation(InformationDO information, ReviewDO review, LikeDO like) {
		assertEquals(review.getReviewId(), information.getReview().getReviewId());
		assertEquals(review.getCommunityUser().getCommunityUserId(),
				information.getCommunityUser().getCommunityUserId());
		assertEquals(review.isAdult(), information.isAdult());
		assertEquals(like.getLikeId(), information.getLike().getLikeId());
		assertEquals(likeUser.getCommunityUserId(), information.getRelationLikeOwnerId());
	}

	/**
	 * いいね50件以上を登録・検証します。
	 *
	 * @param review
	 */
	private void createLike50(ReviewDO review) {
		CommunityUserDO testLikeUser50 = new CommunityUserDO();
		for(int i=1; i<=serviceConfig.likeThresholdLimit; i++) {
			CommunityUserDO testLikeUser = createCommunityUser("testLikeUser" + i, false);
			assertTrue(likeService.updateLikeReview(testLikeUser.getCommunityUserId(), review.getReviewId(), false) == 1);
			if(i==serviceConfig.likeThresholdLimit){
				testLikeUser50 = testLikeUser;
			}
		}
		// アクションヒストリーをSolrから取得します。
		StringBuilder buffer = new StringBuilder();
		buffer.append("reviewId_s:");
		buffer.append(review.getReviewId());
		buffer.append(" AND actionHistoryType_s:");
		buffer.append(ActionHistoryType.LIKE_REVIEW_50.getCode());
		SolrQuery query = new SolrQuery(buffer.toString());
		Condition path = Path.includeProp("*").includePath(
						"communityUser.communityUserId,product.sku,review.reviewId").depth(2);
		SearchResult<ActionHistoryDO> actionHistorisBySolr = new SearchResult<ActionHistoryDO>(
				solrOperations.findByQuery(query, ActionHistoryDO.class, path));
		ActionHistoryDO actionHistoryBySolr = actionHistorisBySolr.getDocuments().get(0);
		// アクションヒストリーをHBaseから取得します。
		ActionHistoryDO actionHistoryByHBase =
				hBaseOperations.load(ActionHistoryDO.class, actionHistoryBySolr.getActionHistoryId(), path);
		checkLike50(actionHistoryByHBase, review, testLikeUser50);
		checkLike50(actionHistoryBySolr, review, testLikeUser50);
	}

	/**
	 * いいね50件以上を検証します。
	 * @param actionHistory
	 * @param review
	 */
	private void checkLike50(ActionHistoryDO actionHistory, ReviewDO review, CommunityUserDO testLikeUser50) {
		assertEquals(review.getReviewId(), actionHistory.getReview().getReviewId());
		assertEquals(review.getProduct().getSku(), actionHistory.getProduct().getSku());
		assertEquals(review.isAdult(), actionHistory.isAdult());
		assertEquals(testLikeUser50.getCommunityUserId(), actionHistory.getCommunityUser().getCommunityUserId());
		assertEquals(testLikeUser50.getCommunityUserId(), actionHistory.getCommunityUser().getCommunityUserId());
		assertEquals(communityUser.getCommunityUserId(), actionHistory.getRelationReviewOwnerId());
	}

	/**
	 * レビュー削除を検証します。
	 */
	private void testDeleteReview(ReviewDO review) {
		requestScopeDao.initialize(review.getCommunityUser(), null);
		ReviewDO oldReview = hBaseOperations.load(ReviewDO.class, review.getReviewId(), reviewPath);
		// レビューを削除します。
		reviewService.deleteReview(review.getReviewId());
		// レビューを取得します。
		ReviewDO reviewByHBase = hBaseOperations.load(ReviewDO.class, review.getReviewId(), reviewPath);
		ReviewDO reviewBySolr = solrOperations.load(ReviewDO.class, review.getReviewId(), reviewPath);

		// 画像を取得します。
		ImageDO hBaseImage = hBaseOperations.load(ImageDO.class, image.getImageId());
		ImageHeaderDO hBaseImageHeader = hBaseOperations.load(ImageHeaderDO.class, image.getImageId());
		ImageHeaderDO solrImageHeader = solrOperations.load(ImageHeaderDO.class, image.getImageId());
		ImageDO hBaseImage2 = hBaseOperations.load(ImageDO.class, image2.getImageId());
		ImageHeaderDO hBaseImageHeader2 = hBaseOperations.load(ImageHeaderDO.class, image2.getImageId());
		ImageHeaderDO solrImageHeader2 = solrOperations.load(ImageHeaderDO.class, image2.getImageId());
		if(ContentsStatus.SAVE.equals(review.getStatus())) {
			// 画像を検証します。
			assertEquals(null, hBaseImage);
			assertEquals(null, hBaseImage2);
			assertEquals(null, hBaseImageHeader);
			assertEquals(null, hBaseImageHeader2);
			assertEquals(null, solrImageHeader);
			assertEquals(null, solrImageHeader2);
			// レビューを検証します。
			assertEquals(null, reviewByHBase);
			assertEquals(null, reviewBySolr);
		} else if(ContentsStatus.SUBMITTED.equals(review.getStatus())) {
			// 画像を検証します。
			assertNotNull(hBaseImage);
			assertNotNull(hBaseImage2);
			assertNotNull(hBaseImageHeader);
			assertNotNull(hBaseImageHeader2);
			assertNotNull(solrImageHeader);
			assertNotNull(solrImageHeader2);
			checkImageHeader(hBaseImageHeader, review, false);
			checkImageHeader(hBaseImageHeader2, review, false);
			checkImageHeader(solrImageHeader, review, false);
			checkImageHeader(solrImageHeader2, review, false);
			// レビューを検証します。
			assertNotNull(reviewByHBase);
			assertEquals(true, reviewByHBase.isDeleted());
			assertEquals(true, reviewBySolr.isDeleted());
			if(ReviewType.REVIEW_AFTER_FEW_DAYS == review.getReviewType()) {

			} else {
				// 購入の決め手を検証します。
				assertEquals(review.getReviewDecisivePurchases().size(), reviewByHBase.getReviewDecisivePurchases().size());
				for(ReviewDecisivePurchaseDO hBaseReviewDecisivePurchase : reviewByHBase.getReviewDecisivePurchases()) {
					ReviewDecisivePurchaseDO solrReviewDecisivePurchase =
							solrOperations.load(ReviewDecisivePurchaseDO.class,
									hBaseReviewDecisivePurchase.getReviewDecisivePurchaseId());
					assertEquals(true, solrReviewDecisivePurchase.isDeleteFlag());
				}
			}
			// アクションヒストリーを検証します。
			checkDeleteActionHistory(ActionHistoryType.USER_REVIEW, communityUser);
			checkDeleteActionHistory(ActionHistoryType.PRODUCT_REVIEW, commentUser);
			checkDeleteActionHistory(ActionHistoryType.USER_REVIEW_COMMENT, commentUser);
			if(50 <= oldReview.getLikes().size()) {
				checkDeleteActionHistory(ActionHistoryType.LIKE_REVIEW_50, commentUser);
			}
			// いいねを検証します。
			if(0 < oldReview.getLikes().size()) {
				// いいねをSolrから取得します。
				StringBuilder buffer = new StringBuilder();
				buffer.append("reviewId_s:");
				buffer.append(review.getReviewId());
				SolrQuery query = new SolrQuery(buffer.toString());
				Condition likePath = Path.includeProp("*")
						.includePath("communityUser.communityUserId,review.reviewId").depth(1);
				SearchResult<LikeDO> likesBySolr = new SearchResult<LikeDO>(
						solrOperations.findByQuery(query, LikeDO.class, likePath));
				// いいねを検証します。
				assertEquals(0, reviewByHBase.getLikes().size());
				assertEquals(0, likesBySolr.getDocuments().size());
				// お知らせを検証します。
				checkDeleteInformation(review, InformationType.REVIEW_LIKE_ADD);
			}
			// コメントを検証します。
			if(0 < oldReview.getComments().size()) {
				checkDeleteComment(reviewByHBase);
			}
			requestScopeDao.destroy();
		}
	}

	/**
	 * コメントの削除を実行・検証します。
	 * @param comment
	 */
	private void deleteComment(CommentDO comment) {
		requestScopeDao.initialize(comment.getCommunityUser(), null);
		commentService.deleteComment(comment.getCommentId());
		checkDeleteComment(comment.getReview());
		requestScopeDao.destroy();
	}

	/**
	 * コメントの削除を検証します。
	 * @param review
	 */
	private void checkDeleteComment(ReviewDO review) {
		// コメントをSolrから取得します。
		SearchResult<CommentDO> commentsBySolr = getSolrCommentsByReviewId(review);
		// コメントを検証します。
		assertEquals(0, review.getComments().size());
		assertEquals(true, commentsBySolr.getDocuments().get(0).isDeleted());
		// コメントのアクションヒストリーを検証します。
		checkDeleteActionHistory(ActionHistoryType.USER_REVIEW_COMMENT, communityUser);
		// お知らせを検証します。
		checkDeleteInformation(review, InformationType.REVIEW_COMMENT_ADD);
	}

	/**
	 * レビューヒストリーを検証します。
	 * @param review
	 */
	private void testReviewHistory(ReviewDO review, int historySize) {
		Condition path = Path
				.includeProp("*").includePath(
						"communityUser.communityUserId,product.sku,review.reviewId").depth(1);
		// レビューヒストリーSolrから取得します。
		StringBuilder buffer = new StringBuilder();
		buffer.append("reviewId_s:");
		buffer.append(review.getReviewId());
		SolrQuery query = new SolrQuery(buffer.toString());
		SearchResult<ReviewHistoryDO> reviewHistorisBySolr = new SearchResult<ReviewHistoryDO>(
				solrOperations.findByQuery(query, ReviewHistoryDO.class, path));
		if(ContentsStatus.SAVE.equals(review.getStatus())) {
			assertEquals(historySize, reviewHistorisBySolr.getDocuments().size());
		} else {
			assertEquals(historySize, reviewHistorisBySolr.getDocuments().size());
			// レビューヒストリーhBaseから取得します。
			ReviewHistoryDO reviewByHBase = hBaseOperations.load(ReviewHistoryDO.class,
					reviewHistorisBySolr.getDocuments().get(historySize-1).getReviewHistoryId(), path);
			checkReviewHistory(review, reviewByHBase);
			checkReviewHistory(review, reviewHistorisBySolr.getDocuments().get(historySize-1));
		}
	}

	/**
	 * レビューヒストリーを検証します。
	 * @param review
	 */
	private void checkReviewHistory(ReviewDO review, ReviewHistoryDO reviewHistory) {
		assertTrue(review.getReviewBody().equals(reviewHistory.getReviewBody()));
		assertTrue(review.getCommunityUser().getCommunityUserId().equals(reviewHistory.getCommunityUser().getCommunityUserId()));
		assertTrue(review.getPointBaseDate().equals(reviewHistory.getPointBaseDate()));
		//TODO
//		assertTrue(review.getElapsedMonth().equals(reviewHistory.getElapsedMonth()));
		assertTrue(review.getPurchaseDate().equals(reviewHistory.getPurchaseDate()));
	}

}