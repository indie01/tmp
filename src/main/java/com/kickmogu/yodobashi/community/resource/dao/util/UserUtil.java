/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.StoppableContents;

/**
 * ユーザユーティリティー
 * @author imaizumi
 *
 */
public class UserUtil {

	/**
	 * 一時停止中かどうかを返します。
	 * @param communityUserDao コミュニティユーザー
	 * @param stopCommunityUserIds 一時停止中のコミュニティユーザーIDのリスト
	 * @return 一時停止中の場合、true
	 */
	public static boolean isStop(
			StoppableContents contents,
			String loginCommunityUserId,
			Set<String> stopCommunityUserIds) {
		if (contents == null) {
			return false;
		}
		Set<String> checkedIds = new HashSet<String>();
		Set<String> checkIds = new HashSet<String>();
		boolean stop = false;
		boolean stopUserAccess = false;
		List<CommunityUserDO> relationOwners = contents.getRelationOwners();
		if (relationOwners != null) {
			for (CommunityUserDO communityUser : relationOwners) {
				if (communityUser == null) {
					continue;
				}
				if (communityUser.getStatus() == null) {
					if (!checkedIds.contains(communityUser.getCommunityUserId())) {
						checkIds.add(communityUser.getCommunityUserId());
					}
				} else if (communityUser.isStop()) {
					stopCommunityUserIds.add(communityUser.getCommunityUserId());
					if (loginCommunityUserId == null ||
							!loginCommunityUserId.equals(communityUser.getCommunityUserId())) {
						stop = true;
					} else if (loginCommunityUserId != null &&
							loginCommunityUserId.equals(communityUser.getCommunityUserId())) {
						stopUserAccess = true;
					}
				} else {
					checkedIds.add(communityUser.getCommunityUserId());
				}
			}
		}
		if (stop) {
			return true;
		}
		List<String> relationOwnerIds = contents.getRelationOwnerIds();
		if (relationOwnerIds != null) {
			for (String communityUserId : relationOwnerIds) {
				if (communityUserId == null) {
					continue;
				}
				if (!checkedIds.contains(communityUserId)) {
					checkIds.add(communityUserId);
				}
			}
		}
		for (String checkId : checkIds) {
			if (stopCommunityUserIds.contains(checkId)) {
				if (loginCommunityUserId == null
						|| !loginCommunityUserId.equals(checkId)) {
					return true;
				}
			}
		}
		if (checkIds.size() == 0) {
			if (checkedIds.size() == 0 && !stopUserAccess) {
				throw new IllegalArgumentException("CommunityUser is not found.");
			}
			return false;
		}
		return stop;
	}
	
}
