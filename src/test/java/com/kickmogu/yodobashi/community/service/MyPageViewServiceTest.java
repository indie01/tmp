package com.kickmogu.yodobashi.community.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.service.vo.MyPageInfoAreaVO;

/**
 * ユーザーサービスのテストクラスです。
 *
 * @author hirabayashi
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/jcclientContext.xml")
public class MyPageViewServiceTest extends BaseViewTest {

	/**
	 * データを初期化します。
	 */
	protected void initialize() {
		super.initialize();
	}

	/**
	 * マイページサービスです。
	 */
	@Autowired
	protected MyPageService myPageService;

	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() {
		initialize();
	}

	/**
	 * 指定したユーザーのマイページ向け共通情報エリア情報を検証します。
	 */
	@Test
	public void testMyPageInfoAreaByCommunityUserId() {
		MyPageInfoAreaVO myPageInfoArea =
				myPageService.getMyPageInfoAreaByCommunityUserId(communityUser.getCommunityUserId(), limit, limit, limit, false);
		assertEquals(false, myPageInfoArea.isLinkFacebook());
		assertEquals(false, myPageInfoArea.isLinkTwitter());
		assertTrue(0!=myPageInfoArea.getNoReadInformationCount());
		//myPageService.updateAllRead(communityUser.getCommunityUserId());
		myPageInfoArea =
				myPageService.getMyPageInfoAreaByCommunityUserId(communityUser.getCommunityUserId(), limit, limit, limit, false);
		assertTrue(0==myPageInfoArea.getNoReadInformationCount());
		assertEquals(1, myPageInfoArea.getPostReviewCount());
		assertEquals(0, myPageInfoArea.getTemporaryReviewCount());
		assertEquals(1, myPageInfoArea.getPostQuestionCount());
		assertEquals(0, myPageInfoArea.getTemporaryQuestionCount());
		assertEquals(0, myPageInfoArea.getPostQuestionAnswerCount());
		assertEquals(0, myPageInfoArea.getTemporaryQuestionAnswerCount());
		assertEquals(4, myPageInfoArea.getPurchaseProductCount());
		assertTrue(!myPageInfoArea.isHasAdult());
		createReviewSetByAdult(communityUser);
		myPageInfoArea =
				myPageService.getMyPageInfoAreaByCommunityUserId(communityUser.getCommunityUserId(), limit, limit, limit, false);
		assertTrue(myPageInfoArea.isHasAdult());
	}

	/**
	 * 指定したコミュニティユーザーに対するお知らせ情報を検索して、
	 * 登録日時順（降順）の戻り値を検証します。
	 */
	@Test
	public void testFindInformationByCommunityUserId() {
		String communityUserId = communityUser.getCommunityUserId();
		SearchResult<InformationDO> informations =
				myPageService.findNoReadInformationByCommunityUserId(communityUserId, limit, offset);
		assertTrue(0!=informations.getNumFound());
		for(InformationDO information : informations.getDocuments()) {
			checkInfomation(information, communityUserId);
		}
		//myPageService.updateAllRead(communityUserId);
		informations =
				myPageService.findNoReadInformationByCommunityUserId(communityUserId, limit, offset);
		for(InformationDO information : informations.getDocuments()) {
			assertNotNull(information.getReadDate());
			assertEquals(true, information.isReadFlag());
		}
	}

	/**
	 * 指定したコミュニティユーザーに対するお知らせ情報を検索して、戻り値を検証します。
	 */
	@Test
	public void testFindInformationSetByCommunityUserId() {
		String communityUserId = communityUser.getCommunityUserId();
		SearchResult<InformationDO> informations =
			myPageService.findInformationByCommunityUserId(communityUser.getCommunityUserId(), 5, offsetTime, false);
		assertTrue(0!=informations.getNumFound());
		for(InformationDO information : informations.getDocuments()) {
			checkInfomation(information, communityUserId);
		}
	}

	/**
	 * 指定したコミュニティユーザーに対するお知らせ情報で未読のカウントの戻り値を検証します。
	 */
	@Test
	public void testCountNoReadInformation() {
		String communityUserId = communityUser.getCommunityUserId();
		long count = myPageService.countNoReadInformation(communityUserId);
		assertTrue(0!=count);
		//myPageService.updateAllRead(communityUserId);
		count = myPageService.countNoReadInformation(communityUserId);
		assertTrue(0==count);
	}

}
