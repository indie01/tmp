package com.kickmogu.yodobashi.community.service;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.dao.ProductMasterDao;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.VersionDO;
import com.kickmogu.yodobashi.community.service.vo.CommunityUserSetVO;
import com.kickmogu.yodobashi.community.service.vo.ProductFollowVO;

/**
 * ユーザーサービスのテストクラスです。
 *
 * @author hirabayashi
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/jcclientContext.xml")
public class FollowViewServiceTest extends DataSetTest {
	
	/**
	 * データを初期化します。
	 */
	protected void initialize() {
		super.initialize();
	}
	
	
	/**
	 * オーダーサービスです。
	 */
	@Autowired
	protected OrderService orderService;
	
	
	/**
	 * 商品サービスです。
	 */
	@Autowired
	protected ProductService productService;
	
	/**
	 * 商品マスター DAO です。
	 */
	@Autowired
	protected ProductMasterDao productMasterDao;
	
	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() {
		initialize();
	}
	
	@After
	public void teardown() {
	}
	
	/**
	 * 指定したユーザーのユーザーページ向け共通情報エリア情報を検証します。
	 */
	@Test
	public void testFindFollowProduct() {
		followService.followProduct(
				communityUser.getCommunityUserId(), "100000001000624829", false);
		followService.followProduct(
				communityUser.getCommunityUserId(), "200000002000012355", false);
		followService.followProduct(
				communityUser.getCommunityUserId(), "100000009000738581", false);
		followService.followProduct(
				communityUser.getCommunityUserId(), "100000001001391026", false);

		ProductDO product = productService.getProductBySku("100000001000624829").getProduct();
		CommunityUserSetVO communityUserSetVO = userService.getCommunityUserByCommunityUserId(communityUser.getCommunityUserId(), product.getSku());
		
		VersionDO version = productService.getNextProductMasterVersion();
		ProductMasterDO productMaster = new ProductMasterDO();
		productMaster.setVersion(version.getVersion());
		productMaster.setRankInVersion(version.getVersion());
		productMaster.setRank(1);
		productMaster.setRequiredNotify(true);
		productMaster.setCommunityUser(communityUserSetVO.getCommunityUser());
		productMaster.setProduct(product);
		List<ProductMasterDO> productMasters = new ArrayList<ProductMasterDO>();
		productMasters.add(productMaster);
		productMasterDao.createProductMastersWithIndex(productMasters);
		productService.upgradeProductMasterVersion();
		
		SearchResult<ProductFollowVO> productFollows = 
				followService.findFollowProduct(communityUser.getCommunityUserId(), 100, 100, null, false);

		for(ProductFollowVO productFollow : productFollows.getDocuments()) {
			if("100000001000624829".equals(productFollow.getProduct().getSku())) {
				assertTrue(productFollow.isHasProductMaster());
			} else {
				assertTrue(!productFollow.isHasProductMaster());
			}
		}
	}
	
	
}
