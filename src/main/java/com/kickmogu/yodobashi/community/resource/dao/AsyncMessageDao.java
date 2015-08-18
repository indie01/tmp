/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import java.util.List;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.AsyncMessageDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.AsyncMessageStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.AsyncMessageType;

/**
 * 非同期メッセージ DAO です。
 * @author kamiike
 *
 */
public interface AsyncMessageDao {

	/**
	 * 指定したメッセージIDの非同期メッセージを取得します。
	 * @param messageId メッセージID
	 * @return 非同期メッセージ
	 */
	public AsyncMessageDO load(String messageId);

	/**
	 * 非同期メッセージを登録します。
	 * @param message 非同期メッセージ
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="テスト対象外")
	public void create(AsyncMessageDO message);

	/**
	 * 指定したメッセージIDの非同期メッセージを削除します。
	 * @param messageId メッセージID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="テスト対象外")
	public void delete(String messageId);

	/**
	 * 指定したメッセージのステータスを更新します。
	 * @param messageId メッセージID
	 * @param consumerServerId 非同期サーバID
	 * @param status ステータス
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="テスト対象外")
	public void updateStatus(
			String messageId,
			String consumerServerId,
			AsyncMessageStatus status);

	/**
	 * 非同期メッセージを返します。
	 * @param type タイプ
	 * @param limit 最大取得件数
	 * @param errorOnly エラーのみ
	 * @return エラー非同期メッセージ
	 */
	public List<AsyncMessageDO> findByMessages(
			AsyncMessageType type, int limit, boolean errorOnly);
}
