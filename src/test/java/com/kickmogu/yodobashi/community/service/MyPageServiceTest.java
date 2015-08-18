package com.kickmogu.yodobashi.community.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;

/**
 * ユーザーサービスのテストクラスです。
 *
 * @author hirabayashi
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/jcclientContext.xml")
public class MyPageServiceTest extends BaseTest {

	/**
	 * データを初期化します。
	 */
	protected void initialize() {
		super.initialize();
	}
	
	private CommunityUserDO communityUser;
	private CommunityUserDO actionUser;
	
	/**
	 * フォローサービスです。
	 */
	@Autowired
	protected FollowService followService;
	
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
		createCommunityUserSet();
	}
	
	/**
	 * テストに使用するコミュニティユーザーを初期生成します。
	 */
	private void createCommunityUserSet() {
		communityUser = createCommunityUser("communityUser", false);
		actionUser = createCommunityUser("actionUser", false);
		assertTrue(followService.followCommunityUser(
				actionUser.getCommunityUserId(),
				communityUser.getCommunityUserId(), false));
		// 商品を購入します。
		Date salesDate = new Date();
		createReceipt(communityUser, "4905524312737", salesDate);
		createReceipt(actionUser, "4905524312737", salesDate);
	}

	/**
	 * お知らせの既読フラグを検証します。
	 */
	@Test
	public void testUpdateInformation() {
		// コミュニティ登録時のお知らせを検証します。
		List<InformationDO> welcomInformations = getHBaseInformation(communityUser, InformationType.WELCOME);
		assertEquals(1, welcomInformations.size());
		checkInfomation(welcomInformations.get(0), false);
		// フォローされた際のお知らせを検証します。
		List<InformationDO> followInformations = getHBaseInformation(communityUser, InformationType.FOLLOW);
		assertEquals(1, followInformations.size());
		checkInfomation(welcomInformations.get(0), false);
		// お知らせ情報で未読のカウントを確認します。
		assertEquals(2, myPageService.countNoReadInformation(communityUser.getCommunityUserId()));
		// お知らせ情報の未読を既読へ変更します。
		//myPageService.updateAllRead(communityUser.getCommunityUserId());
		// お知らせ情報で未読のカウントを確認します。
		assertEquals(0, myPageService.countNoReadInformation(communityUser.getCommunityUserId()));
		// コミュニティ登録時のお知らせを検証します。
		welcomInformations = getHBaseInformation(communityUser, InformationType.WELCOME);
		assertEquals(1, welcomInformations.size());
		checkInfomation(welcomInformations.get(0), true);
		// フォローされた際のお知らせを検証します。
		followInformations = getHBaseInformation(communityUser, InformationType.FOLLOW);
		assertEquals(1, followInformations.size());
		checkInfomation(welcomInformations.get(0), true);
	}
	
	private void checkInfomation(InformationDO information, boolean readFlag) {
		assertNotNull(information);
		assertEquals(readFlag, information.isReadFlag());
	}

}
