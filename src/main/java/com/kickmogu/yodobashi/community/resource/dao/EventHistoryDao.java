/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.constants.EventHistoryType;


/**
 * イベント履歴 DAO です。
 * @author kamiike
 *
 */
public interface EventHistoryDao {

	/**
	 * イベント履歴を登録します。
	 * @param uniqueId イベントごとにユニークなID
	 * @param eventHistoryType イベント履歴タイプ
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="テスト対象外")
	public void saveLog(
			String uniqueId, EventHistoryType eventHistoryType);

	/**
	 * イベント履歴が登録済みか判定します。
	 * @param uniqueId イベントごとにユニークなID
	 * @param eventHistoryType イベント履歴タイプ
	 * @return 既に登録済みの場合、true
	 */
	public boolean existsLog(
			String uniqueId, EventHistoryType eventHistoryType);
}
