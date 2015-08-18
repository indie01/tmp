/**
 *
 */
package com.kickmogu.yodobashi.community.resource.domain;

import java.util.List;
import java.util.Set;

/**
 * 一時停止可能なコンテンツです。
 * @author kamiike
 *
 */
public interface StoppableContents {

	/**
	 * 関連オーナーのリストを返します。
	 * @return 関連オーナーのリスト
	 */
	public List<CommunityUserDO> getRelationOwners();

	/**
	 * 関連オーナーIDのリストを返します。
	 * @return 関連オーナーIDのリスト
	 */
	public List<String> getRelationOwnerIds();

	/**
	 * 一時停止中かどうかを返します。
	 * @param communityUserDao コミュニティユーザー
	 * @param stopCommunityUserIds 一時停止中のコミュニティユーザーIDのリスト
	 * @return 一時停止中の場合、true
	 */
	public boolean isStop(String communityUserId,
			Set<String> stopCommunityUserIds);

}
