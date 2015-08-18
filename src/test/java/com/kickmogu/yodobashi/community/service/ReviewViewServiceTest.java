package com.kickmogu.yodobashi.community.service;


import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.service.vo.ReviewStatisticsVO;

/**
 * ユーザーサービスのテストクラスです。
 *
 * @author hirabayashi
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/jcclientContext.xml")
public class ReviewViewServiceTest extends BaseViewTest {
	
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
	
	String communityUserId;
	
	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() {
		initialize();
		communityUserId = communityUser.getCommunityUserId();
		requestScopeDao.initialize(communityUser, null);
	}
	
	@After
	public void teardown() {
		requestScopeDao.destroy();
	}
	
	/**
	 * レビュー統計情報（商品満足度・購入の決め手・次も買いますか）を検証します。
	 */
	@Test
	public void testGetReviewStatistics() {
		ReviewStatisticsVO reviewStatisticsVO = reviewService.getReviewStatistics(product.getSku(), true, true, true );
		assertNotNull(reviewStatisticsVO.getDecisivePurchaseSummary().getCandidateCount());
		assertNotNull(reviewStatisticsVO.getDecisivePurchaseSummary().getTotalDecisivePurchaseCount());
		assertNotNull(reviewStatisticsVO.getProductSatisfactionSummary().getAnswerCount());
	}
	
	
	
}
