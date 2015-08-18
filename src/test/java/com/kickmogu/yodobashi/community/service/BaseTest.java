package com.kickmogu.yodobashi.community.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.resource.config.DomainConfig;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.dao.OrderDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityNameDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.HashCommunityIdDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.NormalizeCharDO;
import com.kickmogu.yodobashi.community.resource.domain.NormalizeCharGroupDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.ReceiptDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.ReceiptHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDecisivePurchaseDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.SkuCodeNotFoundDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipBillingDateDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.SocialMediaSettingDO;
import com.kickmogu.yodobashi.community.resource.domain.SpamReportDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.DeliverType;
import com.kickmogu.yodobashi.community.resource.domain.constants.EffectiveSlipType;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;
import com.kickmogu.yodobashi.community.resource.domain.constants.OrderEntryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReceiptDetailType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReceiptRegistType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReceiptType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SalesRegistDetailType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SlipDetailCategory;

public class BaseTest {

	/**
	 * ユーザーサービスです。
	 */
	@Autowired
	protected UserService userService;

	/**
	 * 画像サービスです。
	 */
	@Autowired
	protected ImageService imageService;

	/**
	 * 更新通知から呼び出される注文情報の集約処理サービスです。
	 */
	@Autowired
	protected AggregateOrderService aggregateOrderService;

	/**
	 * ドメインコンフィグです。
	 */
	@Autowired
	protected DomainConfig domainConfig;

	/**
	 * リソースコンフィグです。
	 */
	@Autowired
	protected ResourceConfig resourceConfig;

	/**
	 * HBaseアクセサです。
	 */
	@Autowired  @Qualifier("default")
	protected HBaseOperations hBaseOperations;

	/**
	 * Solrアクセサです。
	 */
	@Autowired @Qualifier("default")
	protected SolrOperations solrOperations;

	/**
	 * リクエストスコープで管理するオブジェクトを扱う DAO です。
	 */
	@Autowired
	private RequestScopeDao requestScopeDao;

	/**
	 * 商品 DAO です。
	 */
	@Autowired
	protected ProductDao productDao;

	/**
	 * オーダー DAO です。
	 */
	@Autowired
	protected OrderDao orderDao;

	/**
	 * 通常商品です。
	 */
	protected ProductDO product;

	/**
	 * セット商品です。
	 */
	protected ProductDO product2;

	/**
	 * アダルト商品です。
	 */
	protected ProductDO product3;

	/**
	 * CERO商品です。
	 */
	protected ProductDO product4;

	protected ImageDO image;
	protected ImageDO image2;

	/**
	 * Slipテスト用JANコードです。<br />
	 * 対応する SKU = 100000001000624829
	 */
	protected String slipJanCode = "4905524312737";

	/**
	 * Receiptテスト用JANコードです。<br />
	 * 対応する SKU = 100000001000624829
	 */
	protected String receiptJanCode = "4905524312737";

	/**
	 * コミュニティユーザー用のパスです。
	 */
	protected Condition communityUserPath = Path.includeProp("*").includePath(
			"imageHeader.imageId,thumbnail.imageId").depth(1);

	/**
	 * アクションヒストリー用のパスです。
	 */
	protected Condition actionHistoryPath = Path
			.includeProp("*")
			.includePath(
					"communityUser.communityUserId,product.sku," +
					"review.reviewId,comment.commentId,imageHeader.imageId").depth(2);

	/**
	 * お知らせ用のパスです。
	 */
	protected Condition informationPath = Path.includeProp("*")
			.includePath("communityUser.communityUserId,review.reviewId,product.sku").depth(1);

	/**
	 * レビュー情報の取得パスです。
	 */
	protected Condition reviewPath = Path
			.includeProp("*").includePath(
					"communityUser.communityUserId,product.sku," +
					"reviewDecisivePurchases.decisivePurchase.decisivePurchaseId," +
					"purchaseLostProducts.product.sku," +
					"usedProducts.product.sku,likes.likeId").depth(2);

	/**
	 * 質問情報の取得パスです。
	 */
	protected Condition questionPath = Path
			.includeProp("*").includePath(
					"communityUser.communityUserId,product.sku,likes.likeId").depth(1);
	protected Condition questionAnswerPath = Path
			.includeProp("*").includePath(
					"communityUser.communityUserId,product.sku," +
					"question.questionId").depth(1);

	/**
	 * 購入商品用のパスです。
	 */
	protected Condition purchaseProductPath = Path.includeProp("*")
			.includePath("communityUserId.communityId").depth(1);

	/**
	 * コメント用のパスです。
	 */
	protected Condition commentPath = Path.includeProp("*")
			.includePath("communityUser.communityUserId_s,review.reviewId_s").depth(1);

	/**
	 * イメージヘッダーのパスです。
	 */
	protected Condition imageHeaderPath = Path.includeProp("*")
			.includePath("ownerCommunityUser.communityUserId,image.imageId").depth(1);

	/**
	 * テスト用画像ファイルです。
	 */
	protected byte[] testImageData;

	/**
	 * データを初期化します。
	 */
	protected void initialize() {
		InputStream inputStream = null;
		try {
			try {
				inputStream = getClass().getClassLoader().getResourceAsStream("test.jpg");
				testImageData = new byte[inputStream.available()];
				inputStream.read(testImageData);
			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		hBaseOperations.physicalDeleteAll(HashCommunityIdDO.class);
		hBaseOperations.physicalDeleteAll(CommunityNameDO.class);
		hBaseOperations.physicalDeleteAll(CommunityUserDO.class);
		hBaseOperations.physicalDeleteAll(ImageHeaderDO.class);
		hBaseOperations.physicalDeleteAll(ImageDO.class);
//		hBaseOperations.physicalDeleteAll(InformationDO.class);

		hBaseOperations.physicalDeleteAll(PurchaseProductDO.class);
		hBaseOperations.physicalDeleteAll(PurchaseProductDetailDO.class);
		hBaseOperations.physicalDeleteAll(SkuCodeNotFoundDO.class);
		hBaseOperations.physicalDeleteAll(SlipHeaderDO.class);
		hBaseOperations.physicalDeleteAll(SlipDetailDO.class);
		hBaseOperations.physicalDeleteAll(ReceiptHeaderDO.class);
		hBaseOperations.physicalDeleteAll(ReceiptDetailDO.class);

		hBaseOperations.physicalDeleteAll(ActionHistoryDO.class);
		hBaseOperations.physicalDeleteAll(NormalizeCharDO.class);
		hBaseOperations.physicalDeleteAll(NormalizeCharGroupDO.class);

		hBaseOperations.physicalDeleteAll(ReviewDO.class);
		hBaseOperations.physicalDeleteAll(ReviewDecisivePurchaseDO.class);
		hBaseOperations.physicalDeleteAll(ReviewHistoryDO.class);
		hBaseOperations.physicalDeleteAll(QuestionDO.class);
		hBaseOperations.physicalDeleteAll(QuestionAnswerDO.class);

		hBaseOperations.physicalDeleteAll(CommentDO.class);
		hBaseOperations.physicalDeleteAll(LikeDO.class);

		hBaseOperations.physicalDeleteAll(CommunityUserFollowDO.class);
		hBaseOperations.physicalDeleteAll(ProductFollowDO.class);
		hBaseOperations.physicalDeleteAll(QuestionFollowDO.class);

		hBaseOperations.physicalDeleteAll(SpamReportDO.class);
		hBaseOperations.physicalDeleteAll(SocialMediaSettingDO.class);


		// ******************************************************************* //

		solrOperations.deleteAll(ImageHeaderDO.class);
		solrOperations.deleteAll(CommunityUserDO.class);
		solrOperations.deleteAll(InformationDO.class);
		solrOperations.deleteAll(ActionHistoryDO.class);

		solrOperations.deleteAll(PurchaseProductDO.class);
		solrOperations.deleteAll(PurchaseProductDetailDO.class);
		solrOperations.deleteAll(SkuCodeNotFoundDO.class);
		solrOperations.deleteAll(SlipHeaderDO.class);
		solrOperations.deleteAll(SlipDetailDO.class);
		solrOperations.deleteAll(ReceiptHeaderDO.class);
		solrOperations.deleteAll(ReceiptDetailDO.class);

		solrOperations.deleteAll(ReviewDO.class);
		solrOperations.deleteAll(ReviewDecisivePurchaseDO.class);
		solrOperations.deleteAll(ReviewHistoryDO.class);
		solrOperations.deleteAll(QuestionDO.class);
		solrOperations.deleteAll(QuestionAnswerDO.class);

		solrOperations.deleteAll(CommentDO.class);
		solrOperations.deleteAll(LikeDO.class);

		solrOperations.deleteAll(CommunityUserFollowDO.class);
		solrOperations.deleteAll(ProductFollowDO.class);
		solrOperations.deleteAll(QuestionFollowDO.class);

		solrOperations.deleteAll(SpamReportDO.class);

		createProduct();
		requestScopeDao.destroy();
	}


	/**
	 * コミュニティユーザーを生成します。
	 * @param communityName ニックネーム
	 * @param withImage 画像有りで登録するかどうか
	 * @return コミュニティユーザー
	 */
	protected CommunityUserDO createCommunityUser(String communityName, boolean withImage) {
		CommunityUserDO communityUser = new CommunityUserDO();
		communityUser.setCommunityName(communityName);
		if(withImage){
			image = new ImageDO();
			image.setData(testImageData);
			image.setMimeType("images/jpeg");
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
		CommunityUserDO createCommunityUser = userService.createCommunityUser(
				"test" + UUID.randomUUID().toString().substring(0, 10), communityUser);
		return createCommunityUser;
	}

	/**
	 * コミュニティユーザーをhBaseから取得します。
	 * @param communityUserId
	 * @return コミュニティユーザー
	 */
	protected CommunityUserDO getCommunityUserByHbase(String communityUserId){
		CommunityUserDO communityUser = hBaseOperations.load(
				CommunityUserDO.class, communityUserId, communityUserPath);
		return communityUser;
	}

	/**
	 * コミュニティユーザーをsolrから取得します。
	 * @param communityUserId
	 * @return コミュニティユーザー
	 */
	protected CommunityUserDO getCommunityUserBySolr(String communityUserId){
		CommunityUserDO communityUser = solrOperations.load(
				CommunityUserDO.class, communityUserId, communityUserPath);
		return communityUser;
	}

	/**
	 * 商品データを作成します。
	 */
	protected void createProduct() {
		product = productDao.loadProduct("100000001000624829");
		product2 = productDao.loadProduct("200000002000012355");
		product3 = productDao.loadProduct("100000009000738581");
		product4 = productDao.loadProduct("100000001001391026");
	}
	
	/**
	 * 購入情報を登録します。
	 * @param communityUser
	 * @param janCode
	 * @param receiptRegistType
	 * @param salesDate
	 */
	protected void createReceipt(CommunityUserDO communityUser, String janCode, Date salesDate) {
		createReceipt(communityUser, janCode, salesDate, true);
	}
	protected void createReceipt(CommunityUserDO communityUser, String janCode, Date salesDate, boolean isAggregateOrder) {
		int receiptNo = Math.abs(UUID.randomUUID().hashCode());
		ReceiptHeaderDO receiptHeader = new ReceiptHeaderDO();
		receiptHeader.setOuterCustomerId(communityUser.getCommunityId());
		receiptHeader.setReceiptNo(String.valueOf(receiptNo));
		receiptHeader.setReceiptRegistType(ReceiptRegistType.REGIST);
		receiptHeader.setSalesDate(salesDate);
		receiptHeader.setEffectiveSlipType(EffectiveSlipType.EFFECTIVE);
		receiptHeader.setModifyDateTime(new Date());

		hBaseOperations.save(receiptHeader);
		solrOperations.save(receiptHeader);

		ReceiptDetailDO receiptDetail = new ReceiptDetailDO();
		receiptDetail.setOuterCustomerId(communityUser.getCommunityId());
		receiptDetail.setReceiptNo(receiptHeader.getReceiptNo());
		receiptDetail.setReceiptDetailNo(1);
		receiptDetail.setReceiptType(ReceiptType.NORMAL);
		receiptDetail.setReceiptDetailType(ReceiptDetailType.PRODUCT);
		receiptDetail.setJanCode(janCode);
		receiptDetail.setNetNum(1);
		receiptDetail.setSalesRegistDetailType(SalesRegistDetailType.EFFECTIVE);
		receiptDetail.setModifyDateTime(new Date());

		hBaseOperations.save(receiptDetail);
		solrOperations.save(receiptDetail);
		
		if( isAggregateOrder)
			aggregateOrder(communityUser.getCommunityId());
	}
	
	protected void createSlip(
			CommunityUserDO communityUser, 
			String janCode, 
			Date salesDate, 
			Date billingDate){
		createSlip(communityUser, janCode, salesDate, billingDate, true);
	}
	protected void createSlip(
			CommunityUserDO communityUser, 
			String janCode, 
			Date salesDate, 
			Date billingDate,
			boolean isAggregateOrder){
		int slipNo = Math.abs(UUID.randomUUID().hashCode());
		SlipHeaderDO slipHeader = new SlipHeaderDO();
		slipHeader.setOuterCustomerId(communityUser.getCommunityId());
		slipHeader.setSlipNo(String.valueOf(slipNo));
		slipHeader.setOrderEntryType(OrderEntryType.EC);
		slipHeader.setDeliverType(DeliverType.DELIVER_CALL_IN);
		slipHeader.setOrderEntryDate(salesDate);
		slipHeader.setEffectiveSlipType(EffectiveSlipType.EFFECTIVE);
		slipHeader.setModifyDateTime(new Date());
		
		hBaseOperations.save(slipHeader);
		solrOperations.save(slipHeader);
		
		SlipDetailDO slipDetailDO = new SlipDetailDO();
		slipDetailDO.setOuterCustomerId(communityUser.getCommunityId());
		slipDetailDO.setSlipNo(slipHeader.getSlipNo());
		slipDetailDO.setSlipDetailNo(10);
		slipDetailDO.setSlipDetailCategory(SlipDetailCategory.NORMAL);
		slipDetailDO.setJanCode(janCode);
		slipDetailDO.setEffectiveNum(1);
		slipDetailDO.setReturnedNum(0);
		slipDetailDO.setOldestBillingDate(billingDate);
		SlipBillingDateDO slipBillingDateDO = new SlipBillingDateDO();
		slipBillingDateDO.setBillingDate(billingDate);
		slipBillingDateDO.setBillingNum(1L);
		slipDetailDO.setBillingDates(Lists.newArrayList(slipBillingDateDO));
		slipDetailDO.setSalesRegistDetailType(SalesRegistDetailType.EFFECTIVE);
		slipDetailDO.setHeader(slipHeader);
		slipDetailDO.setModifyDateTime(new Date());
		
		hBaseOperations.save(slipHeader);
		solrOperations.save(slipHeader);
		
		if( isAggregateOrder)
			aggregateOrder(communityUser.getCommunityId());
	}
	protected void aggregateOrder(CommunityUserDO communityUser){
		aggregateOrder(communityUser.getCommunityId());
	}
	
	/**
	 * 購入情報を登録します。
	 * @param communityUser
	 * @param janCode
	 * @param receiptRegistType
	 * @param salesDate
	 */
	protected void createReceiptNotAggregateOrder(CommunityUserDO communityUser, String janCode, ReceiptRegistType receiptRegistType, Date salesDate) {
		ReceiptHeaderDO receiptHeader = new ReceiptHeaderDO();
		receiptHeader.setEffectiveSlipType(EffectiveSlipType.EFFECTIVE);
		receiptHeader.setOuterCustomerId(communityUser.getCommunityId());
		receiptHeader.setReceiptNo(UUID.randomUUID().toString().substring(0, 10));
		receiptHeader.setReceiptRegistType(receiptRegistType);
		receiptHeader.setSalesDate(salesDate);

		hBaseOperations.save(receiptHeader);
		solrOperations.save(receiptHeader);

		ReceiptDetailDO receiptDetail = new ReceiptDetailDO();
		receiptDetail.setJanCode(janCode);
		receiptDetail.setNetNum(1);
		receiptDetail.setOuterCustomerId(communityUser.getCommunityId());
		receiptDetail.setReceiptDetailNo(1);
		receiptDetail.setReceiptDetailType(ReceiptDetailType.PRODUCT);
		receiptDetail.setReceiptNo(receiptHeader.getReceiptNo());
		receiptDetail.setReceiptType(ReceiptType.NORMAL);
		receiptDetail.setSalesRegistDetailType(SalesRegistDetailType.EFFECTIVE);

		hBaseOperations.save(receiptDetail);
		solrOperations.save(receiptDetail);
	}

	/**
	 * 購入商品をsolrから取得します。
	 * @param communityUser
	 * @return
	 */
	protected SearchResult<PurchaseProductDO> getSolrPurchaseProductsByCommunityUser(CommunityUserDO communityUser) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("communityUserId_s:");
		buffer.append(communityUser.getCommunityUserId());
		SolrQuery query = new SolrQuery(buffer.toString());
		SearchResult<PurchaseProductDO> purchaseProduct = new SearchResult<PurchaseProductDO>(
				solrOperations
						.findByQuery(query, PurchaseProductDO.class, purchaseProductPath));
		assertNotNull(purchaseProduct);
		return purchaseProduct;
	}

	/**
	 * 購入商品をHBaseから取得します。
	 * @param communityUser
	 * @return
	 */
	protected List<PurchaseProductDO> getHBasePurchaseProducts(CommunityUserDO communityUser) {
		List<PurchaseProductDO> purchaseProducts = hBaseOperations
				.scanWithIndex(PurchaseProductDO.class, "communityUserId",
						communityUser.getCommunityUserId());
		assertNotNull(purchaseProducts);
		return purchaseProducts;
	}

	/**
	 * 購入商品をsolrから取得します。
	 * @param communityUser
	 * @return
	 */
	protected SearchResult<PurchaseProductDO> getSolrPurchaseProductsByCommunityUserAndSku(CommunityUserDO communityUser, ProductDO product) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("communityUserId_s:");
		buffer.append(communityUser.getCommunityUserId());
		buffer.append(" AND productId_s:");
		buffer.append(product.getSku());
		SolrQuery query = new SolrQuery(buffer.toString());
		SearchResult<PurchaseProductDO> purchaseProducts = new SearchResult<PurchaseProductDO>(
				solrOperations
						.findByQuery(query, PurchaseProductDO.class, purchaseProductPath));
		return purchaseProducts;
	}

	/**
	 * お知らせをHBaseから取得します。
	 * @param review
	 * @param informationType
	 * @return
	 */
	protected List<InformationDO> getHBaseInformation(CommunityUserDO communityUser, InformationType informationType) {
		List<InformationDO> informations = hBaseOperations
				.scanWithIndex(
						InformationDO.class,
						"communityUserId",
						communityUser.getCommunityUserId(),
						hBaseOperations
								.createFilterBuilder(InformationDO.class)
								.appendSingleColumnValueFilter(
										"informationType", CompareOp.EQUAL,
										informationType).toFilter(), informationPath);
		return informations;
	}

	/**
	 * お知らせをSolrから取得します。
	 * @param review
	 * @param informationType
	 * @return
	 */
	protected SearchResult<InformationDO> getSolrInformation(CommunityUserDO communityUser, InformationType informationType) {
		StringBuilder informationBuffer = new StringBuilder();
		informationBuffer.append("communityUserId_s:");
		informationBuffer.append(communityUser.getCommunityUserId());
		informationBuffer.append(" AND informationType_s:");
		informationBuffer.append(informationType.getCode());
		SolrQuery informationQuery = new SolrQuery(informationBuffer.toString());
		SearchResult<InformationDO> informations = new SearchResult<InformationDO>(
				solrOperations.findByQuery(informationQuery, InformationDO.class, informationPath));
		return informations;
	}

	/**
	 * アクションヒストリーをHBaseから取得します。
	 */
	protected List<ActionHistoryDO> getHBaseActionHistorisByCommunityUserAndActionHistoryType(
			CommunityUserDO communityUser, ActionHistoryType actionHistoryType) {
		List<ActionHistoryDO> actionHistoris = hBaseOperations
				.scanWithIndex(
						ActionHistoryDO.class,
						"communityUserId",
						communityUser.getCommunityUserId(),
						hBaseOperations
								.createFilterBuilder(ActionHistoryDO.class)
								.appendSingleColumnValueFilter(
										"actionHistoryType", CompareOp.EQUAL,
										actionHistoryType).toFilter(), actionHistoryPath);
		return actionHistoris;
	}

	/**
	 * アクションヒストリーをsolrから取得します。
	 */
	protected SearchResult<ActionHistoryDO> getSolrActionHistorisByCommunityUserAndActionHistoryType(
			CommunityUserDO communityUser, ActionHistoryType actionHistoryType) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("communityUserId_s:");
		buffer.append(communityUser.getCommunityUserId());
		buffer.append(" AND actionHistoryType_s:");
		buffer.append(actionHistoryType.getCode());
		SolrQuery query = new SolrQuery(buffer.toString());
		SearchResult<ActionHistoryDO> actionHistoris = new SearchResult<ActionHistoryDO>(
				solrOperations.findByQuery(query, ActionHistoryDO.class, actionHistoryPath));
		return actionHistoris;
	}

	/**
	 * アクションヒストリーをHBaseから取得します。
	 */
	protected List<ActionHistoryDO> getHBaseActionHistorisByImageSetIdAndActionHistoryType(
			String imageSetId, ActionHistoryType actionHistoryType) {
		List<ActionHistoryDO> actionHistoris = hBaseOperations
				.scanWithIndex(
						ActionHistoryDO.class,
						"imageSetId",
						imageSetId,
						hBaseOperations
								.createFilterBuilder(ActionHistoryDO.class)
								.appendSingleColumnValueFilter(
										"actionHistoryType", CompareOp.EQUAL,
										actionHistoryType).toFilter(), actionHistoryPath);
		return actionHistoris;
	}

	/**
	 * アクションヒストリーをsolrから取得します。
	 */
	protected SearchResult<ActionHistoryDO> getSolrActionHistorisByImageSetIdAndActionHistoryType(
			String imageSetId, ActionHistoryType actionHistoryType) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("imageSetId_s:");
		buffer.append(imageSetId);
		buffer.append(" AND actionHistoryType_s:");
		buffer.append(actionHistoryType.getCode());
		SolrQuery query = new SolrQuery(buffer.toString());
		SearchResult<ActionHistoryDO> actionHistoris = new SearchResult<ActionHistoryDO>(
				solrOperations.findByQuery(query, ActionHistoryDO.class, actionHistoryPath));
		return actionHistoris;
	}

	/**
	 * アクションヒストリーをHBaseから取得します。
	 */
	protected ActionHistoryDO getHBaseActionHistorisByActionHistoryId(String actionHistoryId) {
		ActionHistoryDO actionHistory = hBaseOperations.load(
				ActionHistoryDO.class, actionHistoryId,	actionHistoryPath);
		return actionHistory;
	}

	/**
	 * アクションヒストリーをsolrから取得します。
	 */
	protected ActionHistoryDO getSolrActionHistorisByActionHistoryId(String actionHistoryId) {
		ActionHistoryDO actionHistory = solrOperations.load(
				ActionHistoryDO.class, actionHistoryId,	actionHistoryPath);
		return actionHistory;
	}

	/**
	 * 文字列を日付に変換します。
	 * @param dateText
	 * @return
	 */
	protected Date getDate(String dateText) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		formatter.setLenient(false);
		try {
			return formatter.parse(dateText);
		} catch (ParseException e) {
			throw new IllegalArgumentException(dateText + " is invalid. required format = 'yyyy/MM/dd'");
		}
	}

	protected void createImage(CommunityUserDO communityUser){
		image = new ImageDO();
		image.setCommunityUserId(communityUser.getCommunityUserId());
		image.setData(testImageData);
		image.setMimeType("images/jpeg");
		image = imageService.createTemporaryImage(image);
	}

	protected void createImageTwo(CommunityUserDO communityUser){
		image2 = new ImageDO();
		image2.setCommunityUserId(communityUser.getCommunityUserId());
		image2.setData(testImageData);
		image2.setMimeType("images/jpeg");
		image2 = imageService.createTemporaryImage(image2);
	}

	protected void aggregateOrder(String outerCustomerId) {
		List<String> outerCustomerIds = new ArrayList<String>();
		outerCustomerIds.add(outerCustomerId);
		aggregateOrderService.aggregateOrder(outerCustomerIds);
	}

	/**
	 * 削除時のアクションヒストリーを検証します。
	 * @param actionHistoryType
	 * @param actionCommunityUser
	 */
	protected void checkDeleteActionHistory(ActionHistoryType actionHistoryType, CommunityUserDO actionCommunityUser) {
		// アクションヒストリーをHBaseから取得します。
		List<ActionHistoryDO> actionHistorisByHBase =
				getHBaseActionHistorisByCommunityUserAndActionHistoryType(actionCommunityUser, actionHistoryType);
		assertNotNull(actionHistorisByHBase);
		for(ActionHistoryDO actionHistoryByHBase : actionHistorisByHBase) {
			// アクションヒストリーをSolrから取得します。
			ActionHistoryDO actionHistoryBySolr =
					getSolrActionHistorisByActionHistoryId(actionHistoryByHBase.getActionHistoryId());
			assertEquals(null, actionHistoryBySolr);
		}
	}

	/**
	 * 削除時のお知らせを検証します。
	 */
	protected void checkDeleteInformation(ReviewDO review, InformationType informationType) {
		// お知らせをSolrから取得します。
		SearchResult<InformationDO> informationsBySolr =
				getSolrInformation(review.getCommunityUser(), informationType);
		// お知らせをHBaseから取得します。
		List<InformationDO> informationsByHBase =
				getHBaseInformation(review.getCommunityUser(), informationType);
		// お知らせを検証します。
		assertNotNull(informationsByHBase);
		assertEquals(0, informationsBySolr.getDocuments().size());
	}

	/**
	 * レビューコメント投稿時のアクションヒストリーを検証します。
	 *
	 * @param communityUser
	 * @param review
	 * @param actionHistoryType
	 */
	protected void checkActionHistory(CommunityUserDO communityUser,
			CommentDO comment, ActionHistoryType actionHistoryType) {
		// アクションヒストリーをSolrから取得します。
		SearchResult<ActionHistoryDO> actionHistorisBySolr =
				getSolrActionHistorisByCommunityUserAndActionHistoryType(communityUser, actionHistoryType);
		// アクションヒストリーをHBaseから取得します。
		List<ActionHistoryDO> actionHistorisByHBase =
				getHBaseActionHistorisByCommunityUserAndActionHistoryType(communityUser, actionHistoryType);
		// アクションヒストリーを検証します。
		checkActionHistory(communityUser, comment, actionHistorisByHBase.get(0));
		checkActionHistory(communityUser, comment, actionHistorisBySolr.getDocuments().get(0));
	}

	/**
	 * コメントのアクションヒストリーを検証します。
	 *
	 * @param communityUser
	 * @param review
	 * @param actionHistory
	 */
	private void checkActionHistory(CommunityUserDO communityUser,
			CommentDO comment, ActionHistoryDO actionHistory) {
		assertEquals(communityUser.getCommunityUserId(),
				actionHistory.getCommunityUser().getCommunityUserId());
		assertNotNull(actionHistory.getActionTime());
		assertNotNull(actionHistory.getRegisterDateTime());
		assertNotNull(actionHistory.getModifyDateTime());
		if(ActionHistoryType.USER_REVIEW_COMMENT == actionHistory.getActionHistoryType()){
			assertEquals(comment.getReview().getReviewId(),
					actionHistory.getReview().getReviewId());
			assertEquals(comment.getReview().getProduct().isAdult(),
					actionHistory.isAdult());
		} else if(ActionHistoryType.USER_ANSWER_COMMENT == actionHistory.getActionHistoryType()){
			assertEquals(comment.getQuestionId(), actionHistory.getQuestion().getQuestionId());
			assertEquals(comment.getQuestionAnswer().getQuestionAnswerId(),
					actionHistory.getQuestionAnswer().getQuestionAnswerId());
		} else if(ActionHistoryType.USER_IMAGE_COMMENT == actionHistory.getActionHistoryType()){
			assertEquals(comment.getImageHeader().getImageId(), actionHistory.getImageHeader().getImageId());
		}
	}

	/**
	 * 画像を登録します。
	 */
	protected List<ImageHeaderDO> testUploadImageSet(CommunityUserDO communityUser, int loopCount, ProductDO product) {
		List<ImageHeaderDO> imageHeaders = new ArrayList<ImageHeaderDO>();

		for(int i=0; i<loopCount; i++) {
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
		List<ImageHeaderDO> uploadImage = imageService.saveImageSet(product.getSku(), imageHeaders, null);
		requestScopeDao.destroy();
		return uploadImage;
	}
}
