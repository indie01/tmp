/**
 *
 */
package com.kickmogu.yodobashi.community.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.common.utils.BackendStubUtils;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.ReceiptDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.ReceiptHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.SkuCodeNotFoundDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.DeliverType;
import com.kickmogu.yodobashi.community.resource.domain.constants.EffectiveSlipType;
import com.kickmogu.yodobashi.community.resource.domain.constants.OrderEntryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReceiptDetailType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReceiptRegistType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReceiptType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SalesRegistDetailType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SlipDetailCategory;
import com.kickmogu.yodobashi.community.resource.domain.constants.SlipType;
import com.yodobashi.esa.customer.getoutcustomeridshareinfo.GetOutCustomerIDShareInfoResponse;
import com.yodobashi.esa.customer.structure.COMMONRETURN;

/**
 * 注文情報の集約サービスのテストクラスです。
 * @author kamiike
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/jcclientContext.xml")
public class AggregateOrderServiceTest extends BaseTest {

	/**
	 * 質問サービスです。
	 */
	@Autowired
	private AggregateOrderService aggregateOrderService;

	/**
	 * コミュニティユーザー情報です。
	 */
	private CommunityUserDO communityUser;

	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() {
		initialize();
		BackendStubUtils.clean();
		communityUser = createCommunityUser("communityUser", false);
	}

	/**
	 * 注文情報の集約処理をテストします。
	 */
	// 要修正
	@Ignore
	@Test
	public void testSlip() {
		SlipHeaderDO slipHeader = new SlipHeaderDO();
		slipHeader.setDeliverType(DeliverType.SHOP);
		slipHeader.setEffectiveSlipType(EffectiveSlipType.EFFECTIVE);
		slipHeader.setOrderEntryDate(getDate("2011/10/01"));
		slipHeader.setOrderEntryType(OrderEntryType.EC);
		slipHeader.setOuterCustomerId(communityUser.getCommunityId());
		slipHeader.setSlipNo("1234567890");
		hBaseOperations.save(slipHeader);
		solrOperations.save(slipHeader);

		SlipDetailDO slipDetail = new SlipDetailDO();
		slipDetail.setOldestBillingDate(getDate("2011/10/02"));
		slipDetail.setEffectiveNum(0);
		slipDetail.setHeader(slipHeader);
		slipDetail.setJanCode("4905524312737");
		slipDetail.setOuterCustomerId(communityUser.getCommunityId());
		slipDetail.setSalesRegistDetailType(SalesRegistDetailType.INEFFECTIVE);
		slipDetail.setSlipDetailCategory(SlipDetailCategory.NORMAL);
		slipDetail.setSlipDetailNo(10);
		slipDetail.setSlipNo(slipHeader.getSlipNo());
		hBaseOperations.save(slipDetail);
		solrOperations.save(slipDetail);

		aggregateOrder(communityUser.getCommunityId());

		PurchaseProductDO purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"100000001000624829", Path.DEFAULT, false);
		assertTrue(purchaseProduct == null);

		//有効数量を 1 に変更。
		slipDetail.setEffectiveNum(1);
		hBaseOperations.save(slipDetail);
		solrOperations.save(slipDetail);

		aggregateOrder(communityUser.getCommunityId());

		purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"100000001000624829", Path.DEFAULT, false);

		assertTrue(purchaseProduct == null);

		//売上登録明細区分を対象に変更。
		slipDetail.setSalesRegistDetailType(SalesRegistDetailType.EFFECTIVE);
		hBaseOperations.save(slipDetail);
		solrOperations.save(slipDetail);

		aggregateOrder(communityUser.getCommunityId());

		purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"100000001000624829", Path.DEFAULT, false);

		assertTrue(purchaseProduct != null);
		assertEquals(slipHeader.getOrderEntryDate(), purchaseProduct.getOrderDate());
		assertEquals(slipDetail.getOldestBillingDate(), purchaseProduct.getPurchaseDate());
		assertEquals(slipDetail.getOldestBillingDate(), purchaseProduct.getBillingDate());

		//注文のキャンセルを想定して、有効数量を 0 に変更。
		slipDetail.setEffectiveNum(0);
		hBaseOperations.save(slipDetail);
		solrOperations.save(slipDetail);

		aggregateOrder(communityUser.getCommunityId());

		purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"100000001000624829", Path.DEFAULT, false);
		assertTrue(purchaseProduct != null);

		//有効数量を 1 に戻す。
		slipDetail.setEffectiveNum(1);
		hBaseOperations.save(slipDetail);
		solrOperations.save(slipDetail);

		aggregateOrder(communityUser.getCommunityId());

		purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"100000001000624829", Path.DEFAULT, false);

		assertTrue(purchaseProduct != null);
		assertEquals(slipHeader.getOrderEntryDate(), purchaseProduct.getOrderDate());
		assertEquals(slipDetail.getOldestBillingDate(), purchaseProduct.getPurchaseDate());
		assertEquals(slipDetail.getOldestBillingDate(), purchaseProduct.getBillingDate());

		//伝票を一つ退会ユーザーのものとして更新
		slipHeader.setEffectiveSlipType(EffectiveSlipType.INEFFECTIVE);
		hBaseOperations.save(slipHeader);
		solrOperations.save(slipHeader);

		aggregateOrder(communityUser.getCommunityId());

		purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"100000001000624829", Path.DEFAULT, false);

		assertTrue(purchaseProduct != null);
		assertEquals(slipHeader.getOrderEntryDate(), purchaseProduct.getOrderDate());
		assertEquals(slipDetail.getOldestBillingDate(), purchaseProduct.getPurchaseDate());
		assertEquals(slipDetail.getOldestBillingDate(), purchaseProduct.getBillingDate());
	}

	/**
	 * 注文情報の集約処理をテストします。
	 */
	@Test
	public void testSlipForError() {
		SlipHeaderDO slipHeader = new SlipHeaderDO();
		slipHeader.setDeliverType(DeliverType.SHOP);
		slipHeader.setEffectiveSlipType(EffectiveSlipType.EFFECTIVE);
		slipHeader.setOrderEntryDate(getDate("2011/10/01"));
		slipHeader.setOrderEntryType(OrderEntryType.EC);
		slipHeader.setOuterCustomerId(communityUser.getCommunityId());
		slipHeader.setSlipNo("1234567890");
		hBaseOperations.save(slipHeader);
		solrOperations.save(slipHeader);

		SlipDetailDO slipDetail = new SlipDetailDO();
		slipDetail.setOldestBillingDate(getDate("2011/10/02"));
		slipDetail.setEffectiveNum(1);
		slipDetail.setHeader(slipHeader);
		slipDetail.setJanCode("test");
		slipDetail.setOuterCustomerId(communityUser.getCommunityId());
		slipDetail.setSalesRegistDetailType(SalesRegistDetailType.EFFECTIVE);
		slipDetail.setSlipDetailCategory(SlipDetailCategory.NORMAL);
		slipDetail.setSlipDetailNo(10);
		slipDetail.setSlipNo(slipHeader.getSlipNo());
		hBaseOperations.save(slipDetail);
		solrOperations.save(slipDetail);

		aggregateOrder(communityUser.getCommunityId());

		List<SkuCodeNotFoundDO> errors = hBaseOperations.scanAll(SkuCodeNotFoundDO.class);
		assertEquals(1, errors.size());
		SkuCodeNotFoundDO error = errors.get(0);
		assertEquals("test", error.getJanCode());
		assertEquals(SlipType.SLIP, error.getType());
		assertEquals(communityUser.getCommunityId(), error.getOuterCustomerId());
		assertEquals(slipDetail.getSlipNo(), error.getDataId());
		assertEquals(slipDetail.getSlipDetailNo(), error.getDetailNo());
	}

	/**
	 * 注文情報の集約処理をテストします。
	 */
	@Test
	public void testSlipForAdult() {
		SlipHeaderDO slipHeader = new SlipHeaderDO();
		slipHeader.setDeliverType(DeliverType.SHOP);
		slipHeader.setEffectiveSlipType(EffectiveSlipType.EFFECTIVE);
		slipHeader.setOrderEntryDate(getDate("2011/10/01"));
		slipHeader.setOrderEntryType(OrderEntryType.EC);
		slipHeader.setOuterCustomerId(communityUser.getCommunityId());
		slipHeader.setSlipNo("1234567890");
		hBaseOperations.save(slipHeader);
		solrOperations.save(slipHeader);

		SlipDetailDO slipDetail = new SlipDetailDO();
		slipDetail.setOldestBillingDate(getDate("2011/10/02"));
		slipDetail.setEffectiveNum(1);
		slipDetail.setHeader(slipHeader);
		slipDetail.setJanCode("4562215332070");
		slipDetail.setOuterCustomerId(communityUser.getCommunityId());
		slipDetail.setSalesRegistDetailType(SalesRegistDetailType.EFFECTIVE);
		slipDetail.setSlipDetailCategory(SlipDetailCategory.NORMAL);
		slipDetail.setSlipDetailNo(10);
		slipDetail.setSlipNo(slipHeader.getSlipNo());
		hBaseOperations.save(slipDetail);
		solrOperations.save(slipDetail);

		aggregateOrder(communityUser.getCommunityId());

		PurchaseProductDO purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"100000009000738581", Path.DEFAULT, false);

		assertTrue(purchaseProduct != null);
		assertEquals(slipHeader.getOrderEntryDate(), purchaseProduct.getOrderDate());
		assertEquals(slipDetail.getOldestBillingDate(), purchaseProduct.getPurchaseDate());
		assertEquals(slipDetail.getOldestBillingDate(), purchaseProduct.getBillingDate());
		assertTrue(purchaseProduct.isAdult());
	}


	/**
	 * 注文情報の集約処理をテストします。
	 */
	@Test
	public void testSlipForOverride() {
		SlipHeaderDO slipHeader = new SlipHeaderDO();
		slipHeader.setDeliverType(DeliverType.SHOP);
		slipHeader.setEffectiveSlipType(EffectiveSlipType.EFFECTIVE);
		slipHeader.setOrderEntryDate(getDate("2011/10/01"));
		slipHeader.setOrderEntryType(OrderEntryType.EC);
		slipHeader.setOuterCustomerId(communityUser.getCommunityId());
		slipHeader.setSlipNo("1234567890");
		hBaseOperations.save(slipHeader);
		solrOperations.save(slipHeader);

		SlipDetailDO slipDetail = new SlipDetailDO();
		slipDetail.setOldestBillingDate(getDate("2011/10/02"));
		slipDetail.setEffectiveNum(1);
		slipDetail.setHeader(slipHeader);
		slipDetail.setJanCode("4905524312737");
		slipDetail.setOuterCustomerId(communityUser.getCommunityId());
		slipDetail.setSalesRegistDetailType(SalesRegistDetailType.EFFECTIVE);
		slipDetail.setSlipDetailCategory(SlipDetailCategory.NORMAL);
		slipDetail.setSlipDetailNo(10);
		slipDetail.setSlipNo(slipHeader.getSlipNo());
		hBaseOperations.save(slipDetail);
		solrOperations.save(slipDetail);

		aggregateOrder(communityUser.getCommunityId());

		PurchaseProductDO purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"100000001000624829", Path.DEFAULT, false);

		assertTrue(purchaseProduct != null);
		assertEquals(slipHeader.getOrderEntryDate(), purchaseProduct.getOrderDate());
		assertEquals(slipDetail.getOldestBillingDate(), purchaseProduct.getPurchaseDate());
		assertEquals(slipDetail.getOldestBillingDate(), purchaseProduct.getBillingDate());


		SlipHeaderDO slipHeader2 = new SlipHeaderDO();
		slipHeader2.setDeliverType(DeliverType.SHOP);
		slipHeader2.setEffectiveSlipType(EffectiveSlipType.EFFECTIVE);
		slipHeader2.setOrderEntryDate(getDate("2011/09/01"));
		slipHeader2.setOrderEntryType(OrderEntryType.EC);
		slipHeader2.setOuterCustomerId(communityUser.getCommunityId());
		slipHeader2.setSlipNo("1234567891");
		hBaseOperations.save(slipHeader2);
		solrOperations.save(slipHeader2);

		SlipDetailDO slipDetail2 = new SlipDetailDO();
		slipDetail2.setOldestBillingDate(getDate("2011/09/02"));
		slipDetail2.setEffectiveNum(1);
		slipDetail2.setHeader(slipHeader2);
		slipDetail2.setJanCode("4905524312737");
		slipDetail2.setOuterCustomerId(communityUser.getCommunityId());
		slipDetail2.setSalesRegistDetailType(SalesRegistDetailType.EFFECTIVE);
		slipDetail2.setSlipDetailCategory(SlipDetailCategory.NORMAL);
		slipDetail2.setSlipDetailNo(10);
		slipDetail2.setSlipNo(slipHeader2.getSlipNo());
		hBaseOperations.save(slipDetail2);
		solrOperations.save(slipDetail2);

		aggregateOrder(communityUser.getCommunityId());

		purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"100000001000624829", Path.DEFAULT, false);

		assertTrue(purchaseProduct != null);
		assertEquals(slipHeader2.getOrderEntryDate(), purchaseProduct.getOrderDate());
		assertEquals(slipDetail2.getOldestBillingDate(), purchaseProduct.getPurchaseDate());
		assertEquals(slipDetail2.getOldestBillingDate(), purchaseProduct.getBillingDate());
	}

	/**
	 * 注文情報の集約処理をテストします。
	 */
	@Test
	public void testSlipForFixed() {
		SlipHeaderDO slipHeader = new SlipHeaderDO();
		slipHeader.setDeliverType(DeliverType.SHOP);
		slipHeader.setEffectiveSlipType(EffectiveSlipType.EFFECTIVE);
		slipHeader.setOrderEntryDate(getDate("2011/10/01"));
		slipHeader.setOrderEntryType(OrderEntryType.EC);
		slipHeader.setOuterCustomerId(communityUser.getCommunityId());
		slipHeader.setSlipNo("1234567890");
		hBaseOperations.save(slipHeader);
		solrOperations.save(slipHeader);

		SlipDetailDO slipDetail = new SlipDetailDO();
		slipDetail.setOldestBillingDate(getDate("2011/10/02"));
		slipDetail.setEffectiveNum(1);
		slipDetail.setHeader(slipHeader);
		slipDetail.setJanCode("4905524312737");
		slipDetail.setOuterCustomerId(communityUser.getCommunityId());
		slipDetail.setSalesRegistDetailType(SalesRegistDetailType.EFFECTIVE);
		slipDetail.setSlipDetailCategory(SlipDetailCategory.NORMAL);
		slipDetail.setSlipDetailNo(10);
		slipDetail.setSlipNo(slipHeader.getSlipNo());
		hBaseOperations.save(slipDetail);
		solrOperations.save(slipDetail);

		aggregateOrder(communityUser.getCommunityId());

		PurchaseProductDO purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"100000001000624829", Path.DEFAULT, false);

		assertTrue(purchaseProduct != null);
		assertEquals(slipHeader.getOrderEntryDate(), purchaseProduct.getOrderDate());
		assertEquals(slipDetail.getOldestBillingDate(), purchaseProduct.getPurchaseDate());
		assertEquals(slipDetail.getOldestBillingDate(), purchaseProduct.getBillingDate());

		orderDao.fixPurchaseDate(purchaseProduct.getPurchaseProductId());

		SlipHeaderDO slipHeader2 = new SlipHeaderDO();
		slipHeader2.setDeliverType(DeliverType.SHOP);
		slipHeader2.setEffectiveSlipType(EffectiveSlipType.EFFECTIVE);
		slipHeader2.setOrderEntryDate(getDate("2011/09/01"));
		slipHeader2.setOrderEntryType(OrderEntryType.EC);
		slipHeader2.setOuterCustomerId(communityUser.getCommunityId());
		slipHeader2.setSlipNo("1234567891");
		hBaseOperations.save(slipHeader2);
		solrOperations.save(slipHeader2);

		SlipDetailDO slipDetail2 = new SlipDetailDO();
		slipDetail2.setOldestBillingDate(getDate("2011/09/02"));
		slipDetail2.setEffectiveNum(1);
		slipDetail2.setHeader(slipHeader2);
		slipDetail2.setJanCode("4905524312737");
		slipDetail2.setOuterCustomerId(communityUser.getCommunityId());
		slipDetail2.setSalesRegistDetailType(SalesRegistDetailType.EFFECTIVE);
		slipDetail2.setSlipDetailCategory(SlipDetailCategory.NORMAL);
		slipDetail2.setSlipDetailNo(10);
		slipDetail2.setSlipNo(slipHeader2.getSlipNo());
		hBaseOperations.save(slipDetail2);
		solrOperations.save(slipDetail2);

		aggregateOrder(communityUser.getCommunityId());

		purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"100000001000624829", Path.DEFAULT, false);

		assertTrue(purchaseProduct != null);
		assertEquals(slipHeader2.getOrderEntryDate(), purchaseProduct.getOrderDate());
		assertEquals(slipDetail.getOldestBillingDate(), purchaseProduct.getPurchaseDate());
		assertEquals(slipDetail.getOldestBillingDate(), purchaseProduct.getBillingDate());

		slipHeader.setEffectiveSlipType(EffectiveSlipType.INEFFECTIVE);
		hBaseOperations.save(slipHeader);
		solrOperations.save(slipHeader);
		slipHeader2.setEffectiveSlipType(EffectiveSlipType.INEFFECTIVE);
		hBaseOperations.save(slipHeader2);
		solrOperations.save(slipHeader2);

		aggregateOrder(communityUser.getCommunityId());

		purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"100000001000624829", Path.DEFAULT, false);

		System.out.println("!!!");
		System.out.println(ToStringBuilder.reflectionToString(purchaseProduct,ToStringStyle.SHORT_PREFIX_STYLE));
		
		assertTrue(purchaseProduct != null);
		assertEquals(slipHeader2.getOrderEntryDate(), purchaseProduct.getOrderDate());
		assertEquals(slipDetail.getOldestBillingDate(), purchaseProduct.getPurchaseDate());
		assertEquals(slipDetail.getOldestBillingDate(), purchaseProduct.getBillingDate());
	}

	/**
	 * 注文情報の集約処理をテストします。
	 */
	// 要修正
	@Ignore
	@Test
	public void testReceipt() {
		ReceiptHeaderDO receiptHeader = new ReceiptHeaderDO();
		receiptHeader.setEffectiveSlipType(EffectiveSlipType.EFFECTIVE);
		receiptHeader.setOuterCustomerId(communityUser.getCommunityId());
		receiptHeader.setReceiptNo("1234567890");
		receiptHeader.setReceiptRegistType(ReceiptRegistType.REGIST);
		receiptHeader.setSalesDate(getDate("2011/10/01"));

		hBaseOperations.save(receiptHeader);
		solrOperations.save(receiptHeader);

		ReceiptDetailDO receiptDetail = new ReceiptDetailDO();
		receiptDetail.setJanCode("4902530916232");
		receiptDetail.setNetNum(0);
		receiptDetail.setOuterCustomerId(communityUser.getCommunityId());
		receiptDetail.setReceiptDetailNo(1);
		receiptDetail.setReceiptDetailType(ReceiptDetailType.PRODUCT);
		receiptDetail.setReceiptNo(receiptHeader.getReceiptNo());
		receiptDetail.setReceiptType(ReceiptType.NORMAL);
		receiptDetail.setSalesRegistDetailType(SalesRegistDetailType.INEFFECTIVE);

		hBaseOperations.save(receiptDetail);
		solrOperations.save(receiptDetail);

		aggregateOrder(communityUser.getCommunityId());

		PurchaseProductDO purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"200000002000012355", Path.DEFAULT, false);
		assertTrue(purchaseProduct == null);

		//正味数量を 1 に変更。
		receiptDetail.setNetNum(1);
		hBaseOperations.save(receiptDetail);
		solrOperations.save(receiptDetail);

		aggregateOrder(communityUser.getCommunityId());

		purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"200000002000012355", Path.DEFAULT, false);

		assertTrue(purchaseProduct == null);

		//売上登録明細区分を対象に変更。
		receiptDetail.setSalesRegistDetailType(SalesRegistDetailType.EFFECTIVE);
		hBaseOperations.save(receiptDetail);
		solrOperations.save(receiptDetail);

		aggregateOrder(communityUser.getCommunityId());

		purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"200000002000012355", Path.DEFAULT, false);

		assertTrue(purchaseProduct != null);
		assertEquals(receiptHeader.getSalesDate(), purchaseProduct.getOrderDate());
		assertEquals(receiptHeader.getSalesDate(), purchaseProduct.getPurchaseDate());
		assertEquals(receiptHeader.getSalesDate(), purchaseProduct.getBillingDate());

		//注文のキャンセルを想定して、正味数量を 0 に変更。
		receiptDetail.setNetNum(0);
		hBaseOperations.save(receiptDetail);
		solrOperations.save(receiptDetail);

		aggregateOrder(communityUser.getCommunityId());

		purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"200000002000012355", Path.DEFAULT, false);
		assertTrue(purchaseProduct != null);

		//正味数量を 1 に戻す。
		receiptDetail.setNetNum(1);
		hBaseOperations.save(receiptDetail);
		solrOperations.save(receiptDetail);

		aggregateOrder(communityUser.getCommunityId());

		purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"200000002000012355", Path.DEFAULT, false);

		assertTrue(purchaseProduct != null);
		assertEquals(receiptHeader.getSalesDate(), purchaseProduct.getOrderDate());
		assertEquals(receiptHeader.getSalesDate(), purchaseProduct.getPurchaseDate());
		assertEquals(receiptHeader.getSalesDate(), purchaseProduct.getBillingDate());

		//伝票を一つ退会ユーザーのものとして更新
		receiptHeader.setEffectiveSlipType(EffectiveSlipType.INEFFECTIVE);
		hBaseOperations.save(receiptHeader);
		solrOperations.save(receiptHeader);

		aggregateOrder(communityUser.getCommunityId());

		purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"200000002000012355", Path.DEFAULT, false);

		assertTrue(purchaseProduct != null);
		assertEquals(receiptHeader.getSalesDate(), purchaseProduct.getOrderDate());
		assertEquals(receiptHeader.getSalesDate(), purchaseProduct.getPurchaseDate());
		assertEquals(receiptHeader.getSalesDate(), purchaseProduct.getBillingDate());
	}

	/**
	 * 注文情報の集約処理をテストします。
	 */
	@Test
	public void testReceiptForError() {
		ReceiptHeaderDO receiptHeader = new ReceiptHeaderDO();
		receiptHeader.setEffectiveSlipType(EffectiveSlipType.EFFECTIVE);
		receiptHeader.setOuterCustomerId(communityUser.getCommunityId());
		receiptHeader.setReceiptNo("1234567890");
		receiptHeader.setReceiptRegistType(ReceiptRegistType.REGIST);
		receiptHeader.setSalesDate(getDate("2011/10/01"));

		hBaseOperations.save(receiptHeader);
		solrOperations.save(receiptHeader);

		ReceiptDetailDO receiptDetail = new ReceiptDetailDO();
		receiptDetail.setJanCode("test");
		receiptDetail.setNetNum(1);
		receiptDetail.setOuterCustomerId(communityUser.getCommunityId());
		receiptDetail.setReceiptDetailNo(1);
		receiptDetail.setReceiptDetailType(ReceiptDetailType.PRODUCT);
		receiptDetail.setReceiptNo(receiptHeader.getReceiptNo());
		receiptDetail.setReceiptType(ReceiptType.NORMAL);
		receiptDetail.setSalesRegistDetailType(SalesRegistDetailType.EFFECTIVE);

		hBaseOperations.save(receiptDetail);
		solrOperations.save(receiptDetail);

		aggregateOrder(communityUser.getCommunityId());

		List<SkuCodeNotFoundDO> errors = hBaseOperations.scanAll(SkuCodeNotFoundDO.class);
		assertEquals(1, errors.size());
		SkuCodeNotFoundDO error = errors.get(0);
		assertEquals("test", error.getJanCode());
		assertEquals(SlipType.RECEIPT, error.getType());
		assertEquals(communityUser.getCommunityId(), error.getOuterCustomerId());
		assertEquals(receiptDetail.getReceiptNo(), error.getDataId());
		assertEquals(receiptDetail.getReceiptDetailNo(), error.getDetailNo());
	}

	/**
	 * 注文情報の集約処理をテストします。
	 */
	@Test
	public void testReceiptForAdult() {
		ReceiptHeaderDO receiptHeader = new ReceiptHeaderDO();
		receiptHeader.setEffectiveSlipType(EffectiveSlipType.EFFECTIVE);
		receiptHeader.setOuterCustomerId(communityUser.getCommunityId());
		receiptHeader.setReceiptNo("1234567890");
		receiptHeader.setReceiptRegistType(ReceiptRegistType.REGIST);
		receiptHeader.setSalesDate(getDate("2011/10/01"));

		hBaseOperations.save(receiptHeader);
		solrOperations.save(receiptHeader);

		ReceiptDetailDO receiptDetail = new ReceiptDetailDO();
		receiptDetail.setJanCode("4562215332070");
		receiptDetail.setNetNum(1);
		receiptDetail.setOuterCustomerId(communityUser.getCommunityId());
		receiptDetail.setReceiptDetailNo(1);
		receiptDetail.setReceiptDetailType(ReceiptDetailType.PRODUCT);
		receiptDetail.setReceiptNo(receiptHeader.getReceiptNo());
		receiptDetail.setReceiptType(ReceiptType.NORMAL);
		receiptDetail.setSalesRegistDetailType(SalesRegistDetailType.EFFECTIVE);

		hBaseOperations.save(receiptDetail);
		solrOperations.save(receiptDetail);

		aggregateOrder(communityUser.getCommunityId());

		PurchaseProductDO purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"100000009000738581", Path.DEFAULT, false);
		assertTrue(purchaseProduct != null);
		assertEquals(receiptHeader.getSalesDate(), purchaseProduct.getOrderDate());
		assertEquals(receiptHeader.getSalesDate(), purchaseProduct.getPurchaseDate());
		assertEquals(receiptHeader.getSalesDate(), purchaseProduct.getBillingDate());
		assertTrue(purchaseProduct.isAdult());
	}

	/**
	 * 注文情報の集約処理をテストします。
	 */
	// 要修正
	@Ignore
	@Test
	public void testReceiptForOverride() {
		ReceiptHeaderDO receiptHeader = new ReceiptHeaderDO();
		receiptHeader.setEffectiveSlipType(EffectiveSlipType.EFFECTIVE);
		receiptHeader.setOuterCustomerId(communityUser.getCommunityId());
		receiptHeader.setReceiptNo("1234567890");
		receiptHeader.setReceiptRegistType(ReceiptRegistType.REGIST);
		receiptHeader.setSalesDate(getDate("2011/10/01"));

		hBaseOperations.save(receiptHeader);
		solrOperations.save(receiptHeader);

		ReceiptDetailDO receiptDetail = new ReceiptDetailDO();
		receiptDetail.setJanCode("4902530916232");
		receiptDetail.setNetNum(1);
		receiptDetail.setOuterCustomerId(communityUser.getCommunityId());
		receiptDetail.setReceiptDetailNo(1);
		receiptDetail.setReceiptDetailType(ReceiptDetailType.PRODUCT);
		receiptDetail.setReceiptNo(receiptHeader.getReceiptNo());
		receiptDetail.setReceiptType(ReceiptType.NORMAL);
		receiptDetail.setSalesRegistDetailType(SalesRegistDetailType.EFFECTIVE);

		hBaseOperations.save(receiptDetail);
		solrOperations.save(receiptDetail);

		aggregateOrder(communityUser.getCommunityId());

		PurchaseProductDO purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"200000002000012355", Path.DEFAULT, false);
		assertTrue(purchaseProduct != null);
		assertEquals(receiptHeader.getSalesDate(), purchaseProduct.getPurchaseDate());
		assertEquals(receiptHeader.getSalesDate(), purchaseProduct.getBillingDate());

		ReceiptHeaderDO receiptHeader2 = new ReceiptHeaderDO();
		receiptHeader2.setEffectiveSlipType(EffectiveSlipType.EFFECTIVE);
		receiptHeader2.setOuterCustomerId(communityUser.getCommunityId());
		receiptHeader2.setReceiptNo("1234567891");
		receiptHeader2.setReceiptRegistType(ReceiptRegistType.REGIST);
		receiptHeader2.setSalesDate(getDate("2011/09/01"));

		hBaseOperations.save(receiptHeader2);
		solrOperations.save(receiptHeader2);

		ReceiptDetailDO receiptDetail2 = new ReceiptDetailDO();
		receiptDetail2.setJanCode("4902530916232");
		receiptDetail2.setNetNum(1);
		receiptDetail2.setOuterCustomerId(communityUser.getCommunityId());
		receiptDetail2.setReceiptDetailNo(1);
		receiptDetail2.setReceiptDetailType(ReceiptDetailType.PRODUCT);
		receiptDetail2.setReceiptNo(receiptHeader2.getReceiptNo());
		receiptDetail2.setReceiptType(ReceiptType.NORMAL);
		receiptDetail2.setSalesRegistDetailType(SalesRegistDetailType.EFFECTIVE);

		hBaseOperations.save(receiptDetail2);
		solrOperations.save(receiptDetail2);

		aggregateOrder(communityUser.getCommunityId());

		purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"200000002000012355", Path.DEFAULT, false);
		assertTrue(purchaseProduct != null);
		assertEquals(receiptHeader2.getSalesDate(), purchaseProduct.getOrderDate());
		assertEquals(receiptHeader2.getSalesDate(), purchaseProduct.getPurchaseDate());
		assertEquals(receiptHeader2.getSalesDate(), purchaseProduct.getBillingDate());

	}

	/**
	 * 注文情報の集約処理をテストします。
	 */
	// 要修正
	@Ignore
	@Test
	public void testReceiptForFixed() {
		ReceiptHeaderDO receiptHeader = new ReceiptHeaderDO();
		receiptHeader.setEffectiveSlipType(EffectiveSlipType.EFFECTIVE);
		receiptHeader.setOuterCustomerId(communityUser.getCommunityId());
		receiptHeader.setReceiptNo("1234567890");
		receiptHeader.setReceiptRegistType(ReceiptRegistType.REGIST);
		receiptHeader.setSalesDate(getDate("2011/10/01"));

		hBaseOperations.save(receiptHeader);
		solrOperations.save(receiptHeader);

		ReceiptDetailDO receiptDetail = new ReceiptDetailDO();
		receiptDetail.setJanCode("4902530916232");
		receiptDetail.setNetNum(1);
		receiptDetail.setOuterCustomerId(communityUser.getCommunityId());
		receiptDetail.setReceiptDetailNo(1);
		receiptDetail.setReceiptDetailType(ReceiptDetailType.PRODUCT);
		receiptDetail.setReceiptNo(receiptHeader.getReceiptNo());
		receiptDetail.setReceiptType(ReceiptType.NORMAL);
		receiptDetail.setSalesRegistDetailType(SalesRegistDetailType.EFFECTIVE);

		hBaseOperations.save(receiptDetail);
		solrOperations.save(receiptDetail);

		aggregateOrder(communityUser.getCommunityId());

		PurchaseProductDO purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"200000002000012355", Path.DEFAULT, false);
		assertTrue(purchaseProduct != null);
		assertEquals(receiptHeader.getSalesDate(), purchaseProduct.getPurchaseDate());
		assertEquals(receiptHeader.getSalesDate(), purchaseProduct.getBillingDate());

		orderDao.fixPurchaseDate(purchaseProduct.getPurchaseProductId());

		ReceiptHeaderDO receiptHeader2 = new ReceiptHeaderDO();
		receiptHeader2.setEffectiveSlipType(EffectiveSlipType.EFFECTIVE);
		receiptHeader2.setOuterCustomerId(communityUser.getCommunityId());
		receiptHeader2.setReceiptNo("1234567891");
		receiptHeader2.setReceiptRegistType(ReceiptRegistType.REGIST);
		receiptHeader2.setSalesDate(getDate("2011/09/01"));

		hBaseOperations.save(receiptHeader2);
		solrOperations.save(receiptHeader2);

		ReceiptDetailDO receiptDetail2 = new ReceiptDetailDO();
		receiptDetail2.setJanCode("4902530916232");
		receiptDetail2.setNetNum(1);
		receiptDetail2.setOuterCustomerId(communityUser.getCommunityId());
		receiptDetail2.setReceiptDetailNo(1);
		receiptDetail2.setReceiptDetailType(ReceiptDetailType.PRODUCT);
		receiptDetail2.setReceiptNo(receiptHeader2.getReceiptNo());
		receiptDetail2.setReceiptType(ReceiptType.NORMAL);
		receiptDetail2.setSalesRegistDetailType(SalesRegistDetailType.EFFECTIVE);

		hBaseOperations.save(receiptDetail2);
		solrOperations.save(receiptDetail2);

		aggregateOrder(communityUser.getCommunityId());

		purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"200000002000012355", Path.DEFAULT, false);
		assertTrue(purchaseProduct != null);
		assertEquals(receiptHeader2.getSalesDate(), purchaseProduct.getOrderDate());
		assertEquals(receiptHeader.getSalesDate(), purchaseProduct.getPurchaseDate());
		assertEquals(receiptHeader.getSalesDate(), purchaseProduct.getBillingDate());

		receiptHeader.setEffectiveSlipType(EffectiveSlipType.INEFFECTIVE);
		hBaseOperations.save(receiptHeader);
		solrOperations.save(receiptHeader);
		receiptHeader2.setEffectiveSlipType(EffectiveSlipType.INEFFECTIVE);
		hBaseOperations.save(receiptHeader2);
		solrOperations.save(receiptHeader2);

		aggregateOrder(communityUser.getCommunityId());

		purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"200000002000012355", Path.DEFAULT, false);

		assertTrue(purchaseProduct != null);
		assertEquals(receiptHeader2.getSalesDate(), purchaseProduct.getOrderDate());
		assertEquals(receiptHeader.getSalesDate(), purchaseProduct.getPurchaseDate());
		assertEquals(receiptHeader.getSalesDate(), purchaseProduct.getBillingDate());

	}

	/**
	 * 共有化情報にEC会員が2通りあるケースをテストします。
	 */
	@Test
	public void testMerge() {
		testSlipForFixed();

		CommunityUserDO shareUser = createCommunityUser("shareUser", false);

		SlipHeaderDO slipHeader = new SlipHeaderDO();
		slipHeader.setDeliverType(DeliverType.SHOP);
		slipHeader.setEffectiveSlipType(EffectiveSlipType.EFFECTIVE);
		slipHeader.setOrderEntryDate(getDate("2010/10/01"));
		slipHeader.setOrderEntryType(OrderEntryType.EC);
		slipHeader.setOuterCustomerId(shareUser.getCommunityId());
		slipHeader.setSlipNo("1234567890");
		hBaseOperations.save(slipHeader);
		solrOperations.save(slipHeader);

		SlipDetailDO slipDetail = new SlipDetailDO();
		slipDetail.setOldestBillingDate(getDate("2010/10/02"));
		slipDetail.setEffectiveNum(1);
		slipDetail.setHeader(slipHeader);
		slipDetail.setJanCode("4905524312737");
		slipDetail.setOuterCustomerId(shareUser.getCommunityId());
		slipDetail.setSalesRegistDetailType(SalesRegistDetailType.EFFECTIVE);
		slipDetail.setSlipDetailCategory(SlipDetailCategory.NORMAL);
		slipDetail.setSlipDetailNo(10);
		slipDetail.setSlipNo(slipHeader.getSlipNo());
		hBaseOperations.save(slipDetail);
		solrOperations.save(slipDetail);

		aggregateOrder(shareUser.getCommunityId());

		PurchaseProductDO purchaseProduct = orderDao.loadPurchaseProductBySku(
				shareUser.getCommunityUserId(),
				"100000001000624829", Path.DEFAULT, false);

		assertTrue(purchaseProduct != null);
		assertEquals(slipHeader.getOrderEntryDate(), purchaseProduct.getOrderDate());
		assertEquals(slipDetail.getOldestBillingDate(), purchaseProduct.getPurchaseDate());
		assertEquals(slipDetail.getOldestBillingDate(), purchaseProduct.getBillingDate());

		orderDao.fixPurchaseDate(purchaseProduct.getPurchaseProductId());

		SlipHeaderDO slipHeader2 = new SlipHeaderDO();
		slipHeader2.setDeliverType(DeliverType.SHOP);
		slipHeader2.setEffectiveSlipType(EffectiveSlipType.EFFECTIVE);
		slipHeader2.setOrderEntryDate(getDate("2010/09/01"));
		slipHeader2.setOrderEntryType(OrderEntryType.EC);
		slipHeader2.setOuterCustomerId(shareUser.getCommunityId());
		slipHeader2.setSlipNo("1234567891");
		hBaseOperations.save(slipHeader2);
		solrOperations.save(slipHeader2);

		SlipDetailDO slipDetail2 = new SlipDetailDO();
		slipDetail2.setOldestBillingDate(getDate("2010/09/02"));
		slipDetail2.setEffectiveNum(1);
		slipDetail2.setHeader(slipHeader2);
		slipDetail2.setJanCode("4905524312737");
		slipDetail2.setOuterCustomerId(shareUser.getCommunityId());
		slipDetail2.setSalesRegistDetailType(SalesRegistDetailType.EFFECTIVE);
		slipDetail2.setSlipDetailCategory(SlipDetailCategory.NORMAL);
		slipDetail2.setSlipDetailNo(10);
		slipDetail2.setSlipNo(slipHeader2.getSlipNo());
		hBaseOperations.save(slipDetail2);
		solrOperations.save(slipDetail2);

		aggregateOrder(shareUser.getCommunityId());

		purchaseProduct = orderDao.loadPurchaseProductBySku(
				shareUser.getCommunityUserId(),
				"100000001000624829", Path.DEFAULT, false);

		assertTrue(purchaseProduct != null);
		assertEquals(getDate("2010/09/01"), purchaseProduct.getOrderDate());
		assertEquals(getDate("2010/10/02"), purchaseProduct.getPurchaseDate());
		assertEquals(getDate("2010/10/02"), purchaseProduct.getBillingDate());
		assertTrue(!purchaseProduct.isShare());
		
		System.out.println("PurchaseProductDetailDO");
		for (PurchaseProductDetailDO ppd:hBaseOperations.scanAll(PurchaseProductDetailDO.class)) {
			System.out.println(ppd.getOuterCustomerId() + ":" + ppd.getSku() + ":" + ppd.getBillingDate());
		}
		System.out.println("PurchaseProductDO");
		for (PurchaseProductDO ppd:hBaseOperations.scanAll(PurchaseProductDO.class)) {
			System.out.println(ppd.getProduct().getSku() + ":" + ppd.getPurchaseDate());
		}

		xiWillReturn(communityUser.getCommunityId(), shareUser.getCommunityId());

		aggregateOrder(shareUser.getCommunityId());

		purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				"100000001000624829", Path.DEFAULT, false);


		// 退会しているので共有化マージされない
		assertTrue(purchaseProduct != null);
		assertEquals(getDate("2010/09/01"), purchaseProduct.getOrderDate());
		assertEquals(getDate("2011/10/02"), purchaseProduct.getPurchaseDate());
		assertEquals(getDate("2011/10/02"), purchaseProduct.getBillingDate());
		assertTrue(!purchaseProduct.isShare());

		purchaseProduct = orderDao.loadPurchaseProductBySku(
				shareUser.getCommunityUserId(),
				"100000001000624829", Path.DEFAULT, false);

		// 退会しているので共有化マージされない
		assertTrue(purchaseProduct != null);
		assertEquals(getDate("2010/09/01"), purchaseProduct.getOrderDate());
		assertEquals(getDate("2010/10/02"), purchaseProduct.getPurchaseDate());
		assertEquals(getDate("2010/10/02"), purchaseProduct.getBillingDate());
		assertTrue(!purchaseProduct.isShare());

	}

	public static void xiWillReturn(
			String ecOuterCustomerId1,
			String ecOuterCustomerId2) {
		GetOutCustomerIDShareInfoResponse response = new GetOutCustomerIDShareInfoResponse();
		response.setCOMMONRETURN(COMMONRETURN.SUCCESS);
		GetOutCustomerIDShareInfoResponse.ShareInfoList shareInfoWrapper
				= new GetOutCustomerIDShareInfoResponse.ShareInfoList();
		shareInfoWrapper.setOuterCustomerId(ecOuterCustomerId1);
		GetOutCustomerIDShareInfoResponse.ShareInfoList.OuterCustomerIdShareInfoList shareInfo
				= new GetOutCustomerIDShareInfoResponse.ShareInfoList.OuterCustomerIdShareInfoList();
		shareInfo.setOuterCustomerId(ecOuterCustomerId1);
		shareInfo.setOuterCustomerStatus("01");
		shareInfo.setCustomerType("0002");
		shareInfoWrapper.getOuterCustomerIdShareInfoList().add(shareInfo);
		GetOutCustomerIDShareInfoResponse.ShareInfoList.OuterCustomerIdShareInfoList shareInfo2
				= new GetOutCustomerIDShareInfoResponse.ShareInfoList.OuterCustomerIdShareInfoList();
		shareInfo2.setOuterCustomerId(ecOuterCustomerId2);
		shareInfo2.setOuterCustomerStatus("01");
		shareInfo2.setCustomerType("0002");
		shareInfoWrapper.getOuterCustomerIdShareInfoList().add(shareInfo2);
		response.getShareInfoList().add(shareInfoWrapper);
		BackendStubUtils.prepareResponse("outerCustomerId", ecOuterCustomerId1, response);
		BackendStubUtils.prepareResponse("outerCustomerId", ecOuterCustomerId2, response);
	}

//	private void aggregateOrder(String outerCustomerId) {
//		List<String> outerCustomerIds = new ArrayList<String>();
//		outerCustomerIds.add(outerCustomerId);
//		aggregateOrderService.aggregateOrder(outerCustomerIds);
//	}
}
