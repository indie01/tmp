package com.kickmogu.yodobashi.community.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.SocialMediaSettingDO;
import com.kickmogu.yodobashi.community.service.vo.CommunityUserSetVO;
import com.kickmogu.yodobashi.community.service.vo.MailSettingCategoryVO;
import com.kickmogu.yodobashi.community.service.vo.MailSettingVO;
import com.kickmogu.yodobashi.community.service.vo.NewsFeedVO;
import com.kickmogu.yodobashi.community.service.vo.UserPageInfoAreaVO;

/**
 * ユーザーサービスのテストクラスです。
 *
 * @author hirabayashi
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/jcclientContext.xml")
public class UserViewServiceTest extends BaseViewTest {
	
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
	
	CommunityUserDO viewUser;
	
	String communityUserId;
	
	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() {
		initialize();
		viewUser = createCommunityUser("viewUser", false);
		communityUserId = communityUser.getCommunityUserId();
		requestScopeDao.initialize(viewUser, null);
	}
	
	@After
	public void teardown() {
		requestScopeDao.destroy();
	}
	
	/**
	 * 指定したユーザーのユーザーページ向け共通情報エリア情報を検証します。
	 */
	@Test
	public void testGetUserPageInfoAreaByCommunityUserId() {
		UserPageInfoAreaVO userPageInfoArea = 
				userService.getUserPageInfoAreaByCommunityUserId(communityUserId, limit, limit, limit);
		assertEquals(communityUserId, userPageInfoArea.getProfileCommunityUser().getCommunityUserId());
		assertTrue(!userPageInfoArea.isLinkFacebook());
		assertTrue(!userPageInfoArea.isLinkMixi());
		assertTrue(!userPageInfoArea.isLinkTwitter());
		assertEquals(1, userPageInfoArea.getPostReviewCount());
		assertEquals(1, userPageInfoArea.getPostQuestionCount());
		assertEquals(1, userPageInfoArea.getPostImageCount());
		assertEquals(0, userPageInfoArea.getPurchaseProductCount());
		orderService.updatePublicSettingForPurchaseProduct(communityUserId, product.getSku(), true);
		userPageInfoArea = 
				userService.getUserPageInfoAreaByCommunityUserId(communityUserId, limit, limit, limit);
		assertEquals(1, userPageInfoArea.getPurchaseProductCount());
		assertEquals(0, userPageInfoArea.getProductMasterCount());
		assertTrue(!userPageInfoArea.isHasAdult());
		assertTrue(!userService.existsCommunityUserFollow(communityUserId));
		followService.followCommunityUser(viewUser.getCommunityUserId(), communityUserId, false);
		userPageInfoArea = 
				userService.getUserPageInfoAreaByCommunityUserId(communityUserId, limit, limit, limit);
		assertTrue(userService.existsCommunityUserFollow(communityUserId));
	}
	
	/**
	 * 指定したコミュニティユーザーのニュースフィードを検証します。
	 */
	@Test
	public void testFindNewsFeedByCommunityUserId() {
		SearchResult<NewsFeedVO> newsFeeds = 
				userService.findNewsFeedByCommunityUserId(communityUserId, limit, offsetTime, false);
		for(NewsFeedVO newsFeed : newsFeeds.getDocuments()) {
			checkActionHistory(newsFeed.getActionHistory(), communityUserId);
			assertNotNull(newsFeed.getCommentCount());
			assertNotNull(newsFeed.getLikeCount());
			assertNotNull(newsFeed.getAnswerCount());
			assertNotNull(newsFeed.isFollowingFlg());
		}
	}
	
	/**
	 * 指定したニックネームのコミュニティユーザーIDを検証します。
	 */
	@Test
	public void testGetCommunityUserIdByCommunityName() {
		String getCommunityUserId = 
				userService.getCommunityUserIdByCommunityName(communityUser.getCommunityName());
		assertNotNull(getCommunityUserId);
		assertEquals(communityUserId, getCommunityUserId);
	}
	
	/**
	 * ハッシュ化されたコミュニティIDに紐づくコミュニティユーザーを検証します。
	 */
	@Test
	public void testGetCommunityUserByHashCommunityId() {
		CommunityUserDO getCommunityUser = 
				userService.getCommunityUserByHashCommunityId(communityUser.getHashCommunityId());
		checkCommunityUser(communityUser, getCommunityUser);
	}
	
	/**
	 * 指定したニックネームのコミュニティユーザーを検証します。
	 */
	@Test
	public void testGetCommunityUserByCommunityName() {
		CommunityUserSetVO communityUserSet = 
				userService.getCommunityUserSetByCommunityName(communityUser.getCommunityName());
		checkCommunityUser(communityUser, communityUserSet.getCommunityUser());
	}
	
	/**
	 * 指定したニックネームのコミュニティユーザーを検証します。
	 */
	@Test
	public void testGetCommunityUserByCommunityNameAndSku() {
		ProductDO useProduct = product;
		CommunityUserSetVO communityUserSet = 
				userService.getCommunityUserByCommunityName(communityUser.getCommunityName(), useProduct.getSku());
		checkCommunityUser(communityUser, communityUserSet.getCommunityUser());
		assertEquals(0, communityUserSet.getProductMasterCount());
		assertEquals(useProduct.getSku(), communityUserSet.getPurchaseProduct().getProduct().getSku());
		assertNotNull(communityUserSet.getPostReviewCount());
		assertNotNull(communityUserSet.getPostQuestionCount());
		assertNotNull(communityUserSet.getPostQuestionAnswerCount());
		assertNotNull(communityUserSet.getPostImageCount());
		assertNotNull(communityUserSet.getProductMasterCount());
		assertEquals(false, communityUserSet.isFollowingUser());
		assertNotNull(communityUserSet.getMatchScore());
		assertNotNull(communityUserSet.getCommonInterestCount());
		assertNotNull(communityUserSet.getCommonPurchaseProductCount());
	}
	
	/**
	 * ソーシャルネットワークのユーザーIDから該当するコミュニティユーザーを検証します。
	 */
	@Test
	public void testFindCommunityUserBySocialProviderUserIds() {
		Set<String> providerUserIds = new HashSet<String>();
		providerUserIds.add("123");
		Map<String, CommunityUserDO> communityUserMap = 
				userService.findCommunityUserBySocialProviderUserIds("twitter", providerUserIds);
		assertEquals(0, communityUserMap.size());
	}
	
	/**
	 * コミュニティ名が重複しているか検証します。
	 */
	@Test
	public void testDuplicateCommunityName() {
		assertTrue(!userService.duplicateCommunityName(communityUserId, communityUser.getCommunityName()));
		assertTrue(!userService.duplicateCommunityName(communityUserId, "aaaaaaaaaaaaaaaaaaaaaaaa"));
		assertTrue(userService.duplicateCommunityName(communityUserId, viewUser.getCommunityName()));
	}
	
	/**
	 * 指定したコミュニティユーザーのメール配信設定を検証します。
	 */
	@Test
	public void testFindMailSettingList() {
		List<MailSettingCategoryVO> mailSettingCategoryVOs = userService.findMailSettingList(communityUserId);
		for(MailSettingCategoryVO mailSettingCategoryVO : mailSettingCategoryVOs) {
			List<MailSettingVO> mailSettingVOs = mailSettingCategoryVO.getMailSettings();
			assertNotNull(mailSettingCategoryVO.getCategory());
			for(MailSettingVO mailSettingVO : mailSettingVOs) {
				assertNotNull(mailSettingVO.getMailSettingMaster().getMailSettingType());
				assertNotNull(mailSettingVO.getMailSettingMaster().getDefaultValue());
			}
		}
	}
	
	/**
	 * 指定したコミュニティユーザーにはじめてのヒント画面を表示するかを検証します。
	 */
	@Test
	public void testIsShowWelcomeHint() {
		assertTrue(userService.isShowWelcomeHint(communityUserId));
		userService.hideWelcomeHint(communityUserId);
		assertTrue(!userService.isShowWelcomeHint(communityUserId));
	}
	
	/**
	 * 指定したコミュニティユーザーのSNS連携情報を検証します。
	 */
	@Test
	public void testFindSocialMediaSettingList() {
		List<SocialMediaSettingDO> socialMediaSettings = userService.findSocialMediaSettingList(communityUserId);
		assertEquals(2, socialMediaSettings.size());
	}
	
	/**
	 * コミュニティユーザーを検証します。
	 * @param communityUser
	 * @param getCommunityUser
	 */
	private void checkCommunityUser(CommunityUserDO communityUser, CommunityUserDO getCommunityUser) {
		assertNotNull(getCommunityUser.getCommunityUserId());
		assertNotNull(getCommunityUser.getCommunityName());
		assertEquals(communityUser.getCommunityUserId(), getCommunityUser.getCommunityUserId());
		assertEquals(communityUserId, getCommunityUser.getCommunityUserId());
	}
	
}
