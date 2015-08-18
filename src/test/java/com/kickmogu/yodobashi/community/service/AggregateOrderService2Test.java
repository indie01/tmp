package com.kickmogu.yodobashi.community.service;

import static com.kickmogu.lib.core.BeanTestHelper.*;
import static org.junit.Assert.*;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.BeanTestHelper;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.common.utils.BackendStubUtils;
import com.kickmogu.yodobashi.community.resource.dao.OrderDao;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.AccountType;
import com.kickmogu.yodobashi.community.resource.domain.constants.EffectiveSlipType;
import com.kickmogu.yodobashi.community.resource.domain.constants.OrderEntryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PurchaseHistoryType;
import com.yodobashi.esa.customer.getoutcustomeridshareinfo.GetOutCustomerIDShareInfoResponse;
import com.yodobashi.esa.customer.structure.COMMONRETURN;

@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/jcclientContext.xml")
public class AggregateOrderService2Test extends BaseTest {

	@Autowired
	private AggregateOrderService aggregateOrderService;
	
	@Autowired
	private OrderDao orderDao;

	@Before
	public void setup() {
		initialize();
		BackendStubUtils.clean();
	}

	@Test
	public void test01() {
		
		// コミュニティ会員A(EC会員A)が商品１(sku=100000001000624829)を購入
		CommunityUserDO communityUserA = createCommunityUser("コミュニティ会員A", false);
		purchaseProduct(communityUserA.getCommunityId(), "01","4905524312737","2010-01-01","2010-01-02");
		aggregateOrder(communityUserA.getCommunityId());
		assertPurchaseProductNum(communityUserA, 1);
		assertPurchaseProduct(communityUserA, "4905524312737", "100000001000624829",  "2010-01-01", "2010-01-02", "2010-01-02", PurchaseHistoryType.YODOBASHI);

		// コミュニティ会員B(EC会員B)が商品２(sku=100000009000738581)を購入
		CommunityUserDO communityUserB = createCommunityUser("コミュニティ会員B", false);
		purchaseProduct(communityUserB.getCommunityId(), "02","4562215332070","2010-02-01","2010-02-02");
		aggregateOrder(communityUserB.getCommunityId());
		assertPurchaseProductNum(communityUserB, 1);
		assertPurchaseProduct(communityUserB, "4562215332070", "100000009000738581",  "2010-02-01", "2010-02-02", "2010-02-02", PurchaseHistoryType.YODOBASHI);
				
		// GPC会員Cが商品３(sku=100000001001391026)を購入
		purchaseProduct("GPC000000C", "03", "4988601007122","2010-03-01","2010-03-02");
		// GPC会員Dが商品４(sku=100000001000827415)を購入
		purchaseProduct("GPC000000D", "04", "4905524372564","2010-04-01","2010-04-02");
		
		//　この段階で注文サマリーしても、共有化していないのでコミュニティ会員は自身の購入商品しか見えない
		aggregateOrder(communityUserA.getCommunityId());
		assertPurchaseProductNum(communityUserA, 1);
		assertPurchaseProduct(communityUserA, "4905524312737", "100000001000624829", "2010-01-01", "2010-01-02", "2010-01-02", PurchaseHistoryType.YODOBASHI);		
		aggregateOrder(communityUserB.getCommunityId());
		assertPurchaseProductNum(communityUserB, 1);
		assertPurchaseProduct(communityUserB, "4562215332070", "100000009000738581", "2010-02-01", "2010-02-02", "2010-02-02", PurchaseHistoryType.YODOBASHI);
		
		// EC会員AとGPC会員Cを共有化
		shareUser(
			s(communityUserA.getCommunityId(), AccountType.EC),
			s("GPC000000C", AccountType.GPC)
		);
		aggregateOrder("GPC000000C");
		
		// コミュニティ会員A(EC会員A)はGPC会員Cの購入情報も見れるようになる
		assertPurchaseProductNum(communityUserA, 2);
		assertPurchaseProduct(communityUserA, "4905524312737", "100000001000624829", "2010-01-01", "2010-01-02", "2010-01-02", PurchaseHistoryType.YODOBASHI);	
		assertPurchaseProduct(communityUserA, "4988601007122", "100000001001391026", "2010-03-01", "2010-03-02", "2010-03-02", PurchaseHistoryType.YODOBASHI);	

		// EC会員BとGPC会員Dを共有化
		shareUser(
			s(communityUserB.getCommunityId(), AccountType.EC),
			s("GPC000000D", AccountType.GPC)
		);
		aggregateOrder("GPC000000D");
		
		// コミュニティ会員B(EC会員B)はGPC会員Dの購入情報も見れるようになる
		assertPurchaseProductNum(communityUserB, 2);
		assertPurchaseProduct(communityUserB, "4562215332070", "100000009000738581", "2010-02-01", "2010-02-02", "2010-02-02", PurchaseHistoryType.YODOBASHI);
		assertPurchaseProduct(communityUserB, "4905524372564", "100000001000827415", "2010-04-01", "2010-04-02", "2010-04-02", PurchaseHistoryType.YODOBASHI);	

		// EC会員AとGPC会員Cを共有化
		shareUser(
			s(communityUserA.getCommunityId(), AccountType.EC),
			s("GPC000000C", AccountType.GPC),
			s(communityUserB.getCommunityId(), AccountType.EC),
			s("GPC000000D", AccountType.GPC)
		);
		aggregateOrder(communityUserA.getCommunityId());
		aggregateOrder("GPC000000C");
		aggregateOrder(communityUserB.getCommunityId());
		aggregateOrder("GPC000000D");
		
		// コミュニティ会員A(EC会員A)は全ての購入情報を見れるようになる
		assertPurchaseProductNum(communityUserA, 4);
		assertPurchaseProduct(communityUserA, "4905524312737", "100000001000624829", "2010-01-01", "2010-01-02", "2010-01-02", PurchaseHistoryType.YODOBASHI);	
		assertPurchaseProduct(communityUserA, "4562215332070", "100000009000738581", "2010-02-01", "2010-02-02", "2010-02-02", PurchaseHistoryType.YODOBASHI);
		assertPurchaseProduct(communityUserA, "4988601007122", "100000001001391026", "2010-03-01", "2010-03-02", "2010-03-02", PurchaseHistoryType.YODOBASHI);		
		assertPurchaseProduct(communityUserA, "4905524372564", "100000001000827415", "2010-04-01", "2010-04-02", "2010-04-02", PurchaseHistoryType.YODOBASHI);		
		
		// コミュニティ会員B(EC会員B)は全ての購入情報を見れるようになる
		assertPurchaseProductNum(communityUserB, 4);
		assertPurchaseProduct(communityUserB, "4905524312737", "100000001000624829", "2010-01-01", "2010-01-02", "2010-01-02", PurchaseHistoryType.YODOBASHI);	
		assertPurchaseProduct(communityUserB, "4562215332070", "100000009000738581", "2010-02-01", "2010-02-02", "2010-02-02", PurchaseHistoryType.YODOBASHI);
		assertPurchaseProduct(communityUserB, "4988601007122", "100000001001391026", "2010-03-01", "2010-03-02", "2010-03-02", PurchaseHistoryType.YODOBASHI);		
		assertPurchaseProduct(communityUserB, "4905524372564", "100000001000827415", "2010-04-01", "2010-04-02", "2010-04-02", PurchaseHistoryType.YODOBASHI);
		
		// EC会員Bを退会する
		changeEffective(communityUserB, EffectiveSlipType.INEFFECTIVE);
		aggregateOrder(communityUserA.getCommunityId());
		aggregateOrder(communityUserB.getCommunityId());
		aggregateOrder("GPC000000C");
		aggregateOrder("GPC000000D");
		
		// EC会員Aの購入一覧で、EC会員Bが購入した商品が削除されている
		assertPurchaseProductNum(communityUserA, 3);
		assertPurchaseProduct(communityUserA, "4905524312737", "100000001000624829", "2010-01-01", "2010-01-02", "2010-01-02", PurchaseHistoryType.YODOBASHI);		
		assertPurchaseProduct(communityUserA, "4988601007122", "100000001001391026", "2010-03-01", "2010-03-02", "2010-03-02", PurchaseHistoryType.YODOBASHI);		
		assertPurchaseProduct(communityUserA, "4905524372564", "100000001000827415", "2010-04-01", "2010-04-02", "2010-04-02", PurchaseHistoryType.YODOBASHI);		

		// コミュニティ会員B(EC会員B)は全ての購入情報が削除される
		assertPurchaseProductNum(communityUserB, 0);

		// EC会員Bの退会の解除(運用ではできないが、オリジナルデータ(受注、売り上げ)があるので復元できる)
		changeEffective(communityUserB, EffectiveSlipType.EFFECTIVE);
		aggregateOrder(communityUserA.getCommunityId());
		aggregateOrder("GPC000000C");
		aggregateOrder(communityUserB.getCommunityId());
		aggregateOrder("GPC000000D");
		
		assertPurchaseProductNum(communityUserA, 4);
		assertPurchaseProduct(communityUserA, "4905524312737", "100000001000624829", "2010-01-01", "2010-01-02", "2010-01-02", PurchaseHistoryType.YODOBASHI);	
		assertPurchaseProduct(communityUserA, "4562215332070", "100000009000738581", "2010-02-01", "2010-02-02", "2010-02-02", PurchaseHistoryType.YODOBASHI);
		assertPurchaseProduct(communityUserA, "4988601007122", "100000001001391026", "2010-03-01", "2010-03-02", "2010-03-02", PurchaseHistoryType.YODOBASHI);		
		assertPurchaseProduct(communityUserA, "4905524372564", "100000001000827415", "2010-04-01", "2010-04-02", "2010-04-02", PurchaseHistoryType.YODOBASHI);		
		
	}

	@Test
	public void test02() {
		
		// コミュニティ会員A(EC会員A)が商品１(sku=100000001000624829)を購入
		CommunityUserDO communityUserA = createCommunityUser("コミュニティ会員A", false);
		purchaseProduct(communityUserA.getCommunityId(), "11", "4905524312737","2010-01-01","2010-01-02");

		// コミュニティ会員B(EC会員B)が商品２(sku=100000009000738581)を購入、商品３(sku=100000009000738581)を購入
		CommunityUserDO communityUserB = createCommunityUser("コミュニティ会員B", false);
		purchaseProduct(communityUserB.getCommunityId(), "12", "4562215332070","2010-02-01","2010-02-02");
		purchaseProduct(communityUserB.getCommunityId(), "13", "4988601007122","2010-03-01","2010-03-02");
		
		// GPC会員Cが商品４(sku=100000001000827415)を購入
		purchaseProduct("GPC000000C", "14", "4905524372564","2010-04-01","2010-04-02");
		// GPC会員Cが商品５(sku=100000001001220335)を購入
		purchaseProduct("GPC000000D", "15", "4960999665610","2010-05-01","2010-05-02");

		// 共有化と注文サマリー
		shareUser(
			s(communityUserA.getCommunityId(), AccountType.EC),
			s("GPC000000C", AccountType.GPC),
			s(communityUserB.getCommunityId(), AccountType.EC),
			s("GPC000000D", AccountType.GPC)
		);
		aggregateOrder(communityUserA.getCommunityId(), communityUserB.getCommunityId(), "GPC000000C", "GPC000000D");
		
		// コミュニティ会員A(EC会員A)は全ての購入情報を見れるようになる
		assertPurchaseProductNum(communityUserA, 5);
		assertPurchaseProduct(communityUserA, "4905524312737", "100000001000624829", "2010-01-01", "2010-01-02", "2010-01-02", PurchaseHistoryType.YODOBASHI);		
		assertPurchaseProduct(communityUserA, "4562215332070", "100000009000738581", "2010-02-01", "2010-02-02", "2010-02-02", PurchaseHistoryType.YODOBASHI);
		assertPurchaseProduct(communityUserA, "4988601007122", "100000001001391026", "2010-03-01", "2010-03-02", "2010-03-02", PurchaseHistoryType.YODOBASHI);		
		assertPurchaseProduct(communityUserA, "4905524372564", "100000001000827415", "2010-04-01", "2010-04-02", "2010-04-02", PurchaseHistoryType.YODOBASHI);	
		assertPurchaseProduct(communityUserA, "4960999665610", "100000001001220335", "2010-05-01", "2010-05-02", "2010-05-02", PurchaseHistoryType.YODOBASHI);		
		
		// コミュニティ会員B(EC会員B)は全ての購入情報を見れるようになる
		assertPurchaseProductNum(communityUserB, 5);
		assertPurchaseProduct(communityUserB, "4905524312737", "100000001000624829", "2010-01-01", "2010-01-02", "2010-01-02", PurchaseHistoryType.YODOBASHI);	
		assertPurchaseProduct(communityUserB, "4562215332070", "100000009000738581", "2010-02-01", "2010-02-02", "2010-02-02", PurchaseHistoryType.YODOBASHI);
		assertPurchaseProduct(communityUserB, "4988601007122", "100000001001391026", "2010-03-01", "2010-03-02", "2010-03-02", PurchaseHistoryType.YODOBASHI);	
		assertPurchaseProduct(communityUserB, "4905524372564", "100000001000827415", "2010-04-01", "2010-04-02", "2010-04-02", PurchaseHistoryType.YODOBASHI);	
		assertPurchaseProduct(communityUserB, "4960999665610", "100000001001220335", "2010-05-01", "2010-05-02", "2010-05-02", PurchaseHistoryType.YODOBASHI);		

		// コミュニティ会員A(EC会員A)が商品１をレビュー
		fixPurchaseDate(communityUserA, "100000001000624829", "2011-01-01");
		// コミュニティ会員B(EC会員B)が商品２をレビュー
		fixPurchaseDate(communityUserB, "100000009000738581", "2011-02-01");
		// コミュニティ会員A(EC会員A)が商品３をレビュー
		fixPurchaseDate(communityUserA, "100000001001391026", "2011-03-01");
		
		// EC会員Bを退会する
		changeEffective(communityUserB, EffectiveSlipType.INEFFECTIVE);
		
		// 注文サマリー
		aggregateOrder(communityUserA.getCommunityId(), communityUserB.getCommunityId(), "GPC000000C", "GPC000000D");

		// コミュニティ会員A(EC会員A)は商品１，３，４，５が残る。商品３は多店舗購入扱いになる
		assertPurchaseProductNum(communityUserA, 4);
		assertPurchaseProduct(communityUserA, "4905524312737", "100000001000624829", "2010-01-01", "2010-01-02", "2010-01-02", PurchaseHistoryType.YODOBASHI);		
		assertPurchaseProduct(communityUserA, "4988601007122", "100000001001391026", "2010-03-01", "2010-03-02", "2010-03-02", PurchaseHistoryType.OTHER);		
		assertPurchaseProduct(communityUserA, "4905524372564", "100000001000827415", "2010-04-01", "2010-04-02", "2010-04-02", PurchaseHistoryType.YODOBASHI);		
		assertPurchaseProduct(communityUserA, "4960999665610", "100000001001220335", "2010-05-01", "2010-05-02", "2010-05-02", PurchaseHistoryType.YODOBASHI);	

		// コミュニティ会員B(EC会員B)はレビューを書いた購入商品が残る
		assertPurchaseProductNum(communityUserB, 1);
		assertPurchaseProduct(communityUserB, "4562215332070", "100000009000738581", "2010-02-01", "2010-02-02", "2010-02-02", PurchaseHistoryType.OTHER);

		// EC会員Bを再入会する
		changeEffective(communityUserB, EffectiveSlipType.EFFECTIVE);
		// 注文サマリー
		aggregateOrder(communityUserA.getCommunityId(), communityUserB.getCommunityId(), "GPC000000C", "GPC000000D");
		
		// コミュニティ会員A(EC会員A)は全ての購入情報を見れるようになる
		assertPurchaseProductNum(communityUserA, 5);
		assertPurchaseProduct(communityUserA, "4905524312737", "100000001000624829", "2010-01-01", "2010-01-02", "2010-01-02", PurchaseHistoryType.YODOBASHI);		
		assertPurchaseProduct(communityUserA, "4562215332070", "100000009000738581", "2010-02-01", "2010-02-02", "2010-02-02", PurchaseHistoryType.YODOBASHI);
		assertPurchaseProduct(communityUserA, "4988601007122", "100000001001391026", "2010-03-01", "2010-03-02", "2010-03-02", PurchaseHistoryType.YODOBASHI);		
		assertPurchaseProduct(communityUserA, "4905524372564", "100000001000827415", "2010-04-01", "2010-04-02", "2010-04-02", PurchaseHistoryType.YODOBASHI);	
		assertPurchaseProduct(communityUserA, "4960999665610", "100000001001220335", "2010-05-01", "2010-05-02", "2010-05-02", PurchaseHistoryType.YODOBASHI);		
		
		// コミュニティ会員B(EC会員B)は全ての購入情報を見れるようになる
		assertPurchaseProductNum(communityUserB, 5);
		assertPurchaseProduct(communityUserB, "4905524312737", "100000001000624829", "2010-01-01", "2010-01-02", "2010-01-02", PurchaseHistoryType.YODOBASHI);	
		assertPurchaseProduct(communityUserB, "4562215332070", "100000009000738581", "2010-02-01", "2010-02-02", "2010-02-02", PurchaseHistoryType.YODOBASHI);
		assertPurchaseProduct(communityUserB, "4988601007122", "100000001001391026", "2010-03-01", "2010-03-02", "2010-03-02", PurchaseHistoryType.YODOBASHI);	
		assertPurchaseProduct(communityUserB, "4905524372564", "100000001000827415", "2010-04-01", "2010-04-02", "2010-04-02", PurchaseHistoryType.YODOBASHI);	
		assertPurchaseProduct(communityUserB, "4960999665610", "100000001001220335", "2010-05-01", "2010-05-02", "2010-05-02", PurchaseHistoryType.YODOBASHI);		
		
	}
	
	
	// #6660 の対応
	@Test
	public void redmine6660() {
		
		CommunityUserDO communityUserA = createCommunityUser("コミュニティ会員A", false);

		purchaseProduct(communityUserA.getCommunityId(), "11", "4905524312737","2010-01-01",null);
		aggregateOrder(communityUserA.getCommunityId());
		assertPurchaseProductNum(communityUserA, 0);
		
		purchaseProduct(communityUserA.getCommunityId(), "11","4905524312737","2010-01-01","2010-01-02");
		aggregateOrder(communityUserA.getCommunityId());
		assertPurchaseProductNum(communityUserA, 1);
		assertPurchaseProduct(communityUserA, "4905524312737", "100000001000624829",  "2010-01-01", "2010-01-02", "2010-01-02", PurchaseHistoryType.YODOBASHI);

		purchaseProduct(communityUserA.getCommunityId(), "11", "4905524312737","2010-01-01",null);
		aggregateOrder(communityUserA.getCommunityId());
		assertPurchaseProductNum(communityUserA, 0);
	}
	
	@Test
	public void redmine6675() {
		// コミュニティ会員A(EC会員A)が商品１(sku=100000001000624829)を購入
		CommunityUserDO communityUserA = createCommunityUser("コミュニティ会員A", false);
		purchaseProduct(communityUserA.getCommunityId(), "01","4905524312737","2010-01-01","2010-01-02");
		aggregateOrder(communityUserA.getCommunityId());
		assertPurchaseProductNum(communityUserA, 1);
		assertPurchaseProduct(communityUserA, "4905524312737", "100000001000624829",  "2010-01-01", "2010-01-02", "2010-01-02", PurchaseHistoryType.YODOBASHI);

		fixPurchaseDate(communityUserA, "100000001000624829", "2011-01-01");
		
		// EC会員Aを退会する
		changeEffective(communityUserA, EffectiveSlipType.INEFFECTIVE);
		aggregateOrder(communityUserA.getCommunityId());
		
		// コミュニティ会員A(EC会員A)は他店購入扱いになる
		assertPurchaseProductNum(communityUserA, 1);
		assertPurchaseProduct(communityUserA, "4905524312737", "100000001000624829",  "2010-01-01", "2010-01-02", "2010-01-02", PurchaseHistoryType.OTHER);
		
		// EC会員Aを再入会する
		changeEffective(communityUserA, EffectiveSlipType.EFFECTIVE);
		aggregateOrder(communityUserA.getCommunityId());
		
		assertPurchaseProductNum(communityUserA, 1);
		assertPurchaseProduct(communityUserA, "4905524312737", "100000001000624829",  "2010-01-01", "2010-01-02", "2010-01-02", PurchaseHistoryType.YODOBASHI);

	}
	
	private void fixPurchaseDate(CommunityUserDO communityUser, String sku, String phurchaseDate) {
		orderDao.fixPurchaseDate(orderDao.createPurchaseProductId(communityUser.getCommunityUserId(),sku));
	}

	
	private static ShareUser s(String outerCustomerId, AccountType accountType) {
		return new ShareUser(outerCustomerId, accountType);
	}
	
	private static class ShareUser {
		AccountType accountType;
		String outerCustomerId;
		ShareUser(String outerCustomerId, AccountType accountType) {
			this.outerCustomerId = outerCustomerId;
			this.accountType = accountType;
		}
	}
	
	private void changeEffective(CommunityUserDO communityUser, EffectiveSlipType effectiveSlipType) {
		List<SlipHeaderDO> slipHeaders = hBaseOperations.scanAll(SlipHeaderDO.class);
		for (SlipHeaderDO slipHeader:slipHeaders) {
			if (slipHeader.getOuterCustomerId().equals(communityUser.getCommunityId())) {
				slipHeader.setEffectiveSlipType(effectiveSlipType);
				hBaseOperations.save(slipHeader);
				solrOperations.save(slipHeader);
			}
		}
	}
	
	private void shareUser(ShareUser... shareUsers) {
		for (ShareUser shareUser:shareUsers) {
			GetOutCustomerIDShareInfoResponse response = new GetOutCustomerIDShareInfoResponse();
			response.setCOMMONRETURN(COMMONRETURN.SUCCESS);
			GetOutCustomerIDShareInfoResponse.ShareInfoList shareInfoWrapper= new GetOutCustomerIDShareInfoResponse.ShareInfoList();
			shareInfoWrapper.setOuterCustomerId(shareUser.outerCustomerId);
			for (ShareUser shareUser2:shareUsers) {
				GetOutCustomerIDShareInfoResponse.ShareInfoList.OuterCustomerIdShareInfoList shareInfo
				= new GetOutCustomerIDShareInfoResponse.ShareInfoList.OuterCustomerIdShareInfoList();
				shareInfo.setOuterCustomerId(shareUser2.outerCustomerId);
				shareInfo.setOuterCustomerStatus("01");
				shareInfo.setCustomerType(shareUser2.accountType.getCode());
				shareInfoWrapper.getOuterCustomerIdShareInfoList().add(shareInfo);
			}
			response.getShareInfoList().add(shareInfoWrapper);
			BackendStubUtils.prepareResponse("outerCustomerId", shareUser.outerCustomerId, response);
		}

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
	
	@SuppressWarnings("unused")
	private void assertPurchaseProductExist(CommunityUserDO communityUser, String sku) {
		PurchaseProductDO purchaseProductDO = hBaseOperations.load(PurchaseProductDO.class, communityUser.getCommunityUserId()+"-"+sku);
		assertTrue(purchaseProductDO != null);
	}
	
	@SuppressWarnings("unused")
	private void assertPurchaseProductNotExist(CommunityUserDO communityUser, String sku) {
		PurchaseProductDO purchaseProductDO = hBaseOperations.load(PurchaseProductDO.class, communityUser.getCommunityUserId()+"-"+sku);
		assertTrue(purchaseProductDO == null);
	}
	
	private void assertPurchaseProductNum(CommunityUserDO communityUser, int size) {
		List<PurchaseProductDO> list = hBaseOperations.scan(PurchaseProductDO.class, communityUser.getCommunityUserId());
		assertEquals(size, list.size());
	}

	private void assertPurchaseProduct(CommunityUserDO communityUser, String janCode, String sku, String orderDate, String purchaseDate, String billingDate, PurchaseHistoryType purchaseHistoryType) {
		PurchaseProductDO purchaseProductDO = hBaseOperations.load(PurchaseProductDO.class, communityUser.getCommunityUserId()+"-"+sku);
		System.out.println(ToStringBuilder.reflectionToString(purchaseProductDO));
		assertBean(purchaseProductDO, PurchaseProductDO.class,
			"communityUser.communityUserId,product.sku,janCode,orderDate,purchaseDate,billingDate,purchaseHistoryType",
			communityUser.getCommunityUserId(),sku,janCode,ts(orderDate),ts(purchaseDate),ts(billingDate),purchaseHistoryType
		);
	}
	
	private void purchaseProduct(String outerCustomerId, String slipNo, String janCode, String orderEntryDate, String oldestBillingDate) {
		saveHBaseAndSolr(SlipHeaderDO.class, BeanTestHelper.createList(SlipHeaderDO.class,
			"outerCustomerId,slipNo,orderEntryType,orderEntryDate,effectiveSlipType,modifyDateTime",
			outerCustomerId+","+slipNo+","+OrderEntryType.EC.getCode()+","+orderEntryDate+",1,2011-01-01"
		));
		saveHBaseAndSolr(SlipDetailDO.class, BeanTestHelper.createList(SlipDetailDO.class,
			"outerCustomerId,slipNo,slipDetailNo,slipDetailCategory,janCode,effectiveNum,salesRegistDetailType,oldestBillingDate",
			outerCustomerId+","+slipNo+",1,01,"+janCode+",1,1," +((oldestBillingDate!=null)?oldestBillingDate:"NULL")
		));
		solrOperations.optimize(SlipHeaderDO.class);
		solrOperations.optimize(SlipDetailDO.class);

	}

	private <T> void saveHBaseAndSolr(Class<T> type, List<T> list) {
		hBaseOperations.save(type, list);
		solrOperations.save(type, list);
	}
	
	private void aggregateOrder(String... outerCustomerIds) {
		aggregateOrderService.aggregateOrder(Lists.newArrayList(outerCustomerIds));
	}
}
