/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.AnnounceDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.AnnounceType;

/**
 * アナウンス DAO です。
 * @author kamiike
 *
 */
public interface AnnounceDao {

	/**
	 * 指定したコミュニティユーザーID、タイプのアナウンスを取得します。
	 * @param communityUserId コミュニティユーザーID
	 * @param type タイプ
	 * @return アナウンス
	 */
	public AnnounceDO load(String communityUserId, AnnounceType type);

	/**
	 * アナウンス情報を新規に登録します。
	 * @param announce アナウンス
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="コミュニティユーザ作成のタイミングで呼ばれるので頻度は稀")
	public void create(AnnounceDO announce);

	/**
	 * アナウンス情報を削除します。
	 * @param announceId アナウンスID
	 * @param type タイプ
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="はじめてのヒントを表示させないように設定を更新するタイミングで呼ばれるので頻度は稀")
	public void delete(String communityUserId, AnnounceType type);
	
}
