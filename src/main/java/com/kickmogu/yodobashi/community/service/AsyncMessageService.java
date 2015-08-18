/**
 *
 */
package com.kickmogu.yodobashi.community.service;

import com.kickmogu.yodobashi.community.resource.domain.AsyncMessageDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.AsyncMessageType;

/**
 * 非同期サービスです。
 * @author kamiike
 *
 */
public interface AsyncMessageService {

	/**
	 * 非同期メッセージを保存します。
	 * @param appServerId アプリケーションサーバID
	 * @param message メッセージ
	 */
	public void createMessage(String appServerId, AsyncMessageDO message);

	/**
	 * 非同期メッセージ結果を登録します。
	 * @param consumerServerId 非同期サーバID
	 * @param message メッセージ
	 * @param success 成功した場合、true
	 */
	public void saveMessageResult(
			String consumerServerId,
			AsyncMessageDO message,
			boolean success);

	/**
	 * メッセージを再実行します。
	 * @param type タイプ
	 * @param limit 最大実行数
	 * @param errorOnly エラーのみ
	 */
	public void retryMessages(AsyncMessageType type,
			int limit, boolean errorOnly);
}
