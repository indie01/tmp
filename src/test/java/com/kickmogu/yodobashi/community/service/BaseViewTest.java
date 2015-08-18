package com.kickmogu.yodobashi.community.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType.ActionHistoryGroup;

/**
 * 初期データを生成します。
 * @author hirabayashi
 *
 */
public class BaseViewTest extends DataSetTest {

	int limit = 10;

	int offset = 0;

	Date offsetTime = null;

	/**
	 * お知らせを検証します。
	 * @param information
	 */
	protected void checkInfomation(InformationDO information, String communityUserId) {
		assertNotNull(information.getInformationId());
		assertEquals(communityUserId, information.getCommunityUser().getCommunityUserId());
		assertNotNull(information.getInformationType());
		assertNotNull(information.getInformationTime());
		assertTrue(!information.isAdult());
		assertEquals(null, information.getDeleteDate());
		assertEquals(false, information.isDeleted());
		assertEquals(null, information.getReadDate());
		assertEquals(false, information.isReadFlag());
		if(InformationType.FOLLOW.equals(information.getInformationType())) {
			assertNotNull(information.getFollowerCommunityUser().getCommunityUserId());
			assertNotNull(information.getFollowerCommunityUser().getCommunityName());
		}
		if(!InformationType.WELCOME.equals(information.getInformationType())) {
			assertNotNull(information.getRelationCommunityUserId());
		}
		assertEquals(false, information.isWithdraw());
		assertEquals(null, information.getWithdrawKey());
	}

	/**
	 * アクションヒストリーを検証します。
	 */
	protected void checkActionHistory(ActionHistoryDO actionHistory, String communityUserId) {
		assertNotNull(actionHistory.getActionHistoryId());
		assertNotNull(actionHistory.getActionHistoryType());
		if(ActionHistoryType.USER_FOLLOW_USER.equals(actionHistory.getActionHistoryType())) {
			assertNotNull(actionHistory.getFollowCommunityUser().getCommunityUserId());
			assertNotNull(actionHistory.getFollowCommunityUser().getCommunityName());
		}
		if(ActionHistoryGroup.PRODUCT.equals(actionHistory.getActionHistoryType().getGroup())) {
			assertNotNull(actionHistory.getProduct().getSku());
			if(ActionHistoryType.PRODUCT_IMAGE.equals(actionHistory.getActionHistoryType())) {
				assertNotNull(actionHistory.getImageHeader().getImageId());
				assertNotNull(actionHistory.getImageHeader().getImageUrl());
			}
		}
	}
}
