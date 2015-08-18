package com.kickmogu.yodobashi.community.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;

/**
 * ユーザーサービスのテストクラスです。
 *
 * @author hirabayashi
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/jcclientContext.xml")
public class OrderServiceTest extends BaseTest {

	/**
	 * オーダーサービスです。
	 */
	@Autowired
	protected OrderService orderService;


	private CommunityUserDO communityUser;

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

	/**
	 * テストに使用するコミュニティユーザーを初期生成します。
	 */
	private void createCommunityUserSet() {
		communityUser = createCommunityUser("communityUser", false);
	}

	/**
	 * 購入商品情報の公開設定を更新・検証します。
	 */
	@Test
	public void testUpdatePublicSettingForPurchaseProduct() {
		// 購入商品情報を登録します。
		Date salesDate = new Date();
		createReceipt(communityUser, "4905524312737", salesDate);
		createReceipt(communityUser, "4902530916232", salesDate);
		createReceipt(communityUser, "4562215332070", salesDate);
		createReceipt(communityUser, "4988601007122", salesDate);
		// 公開・非公開を選択します。(purchaseProduct3のみ非公開にする)
		List<String> publicSkus = new ArrayList<String>();
		List<String> privateSkus = new ArrayList<String>();
		publicSkus.add(product.getSku());
		publicSkus.add(product2.getSku());
		publicSkus.add(product4.getSku());
		privateSkus.add(product3.getSku());
		// 購入商品情報の公開設定を更新・検証します。
		testUpdatePublicSettingForPurchaseProduct(publicSkus, privateSkus);
	}

	/**
	 * 購入商品情報の公開設定を更新・検証します。
	 * @param purchaseProduct
	 */
	private void testUpdatePublicSettingForPurchaseProduct(List<String> publicSkus, List<String> privateSkus) {
		for(String publicSku : publicSkus) {
			orderService.updatePublicSettingForPurchaseProduct(communityUser.getCommunityUserId(), publicSku, true);
		}
		for(String privateSku : privateSkus) {
			orderService.updatePublicSettingForPurchaseProduct(communityUser.getCommunityUserId(), privateSku, false);
		}
		// 保存されている一覧をsolrから取得
		SearchResult<PurchaseProductDO> purchaseProductsBySolr = orderDao.findPurchaseProductByCommunityUserId(
				communityUser.getCommunityUserId(), true, 10, 0);
		// 保存されている一覧をHBaseから取得
		Condition path = Path.includeProp("*")
				.includePath("communityUser.communityUserId,review.reviewId").depth(1);
		List<PurchaseProductDO> purchaseProductsByHBase = hBaseOperations
				.scanWithIndex(PurchaseProductDO.class, "communityUserId", communityUser.getCommunityUserId(), path);
		assertNotNull(purchaseProductsByHBase);
		assertNotNull(purchaseProductsBySolr.getDocuments());
		checkUpdatePublicSettingForPurchaseProduct(purchaseProductsByHBase, publicSkus, privateSkus);
		checkUpdatePublicSettingForPurchaseProduct(purchaseProductsBySolr.getDocuments(), publicSkus, privateSkus);
	}

	/**
	 * 購入商品情報の公開設定を検証します。
	 * @param purchaseProducts
	 * @param publicSkus
	 * @param privateSkus
	 */
	private void checkUpdatePublicSettingForPurchaseProduct(List<PurchaseProductDO> purchaseProducts, List<String> publicSkus, List<String> privateSkus) {
		for(PurchaseProductDO purchaseProduct : purchaseProducts) {
			if(1 == Collections.frequency(publicSkus, purchaseProduct.getProduct().getSku())) {
				assertTrue(purchaseProduct.isPublicSetting());
			} else {
				assertEquals(false, purchaseProduct.isPublicSetting());
			}
			assertNotNull(purchaseProduct.getModifyDateTime());
		}
	}

}
