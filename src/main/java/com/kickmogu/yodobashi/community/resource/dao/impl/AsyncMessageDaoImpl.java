/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.yodobashi.community.resource.dao.AsyncMessageDao;
import com.kickmogu.yodobashi.community.resource.domain.AsyncMessageDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.AsyncMessageStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.AsyncMessageType;

/**
 * 非同期メッセージ DAO です。
 * @author kamiike
 *
 */
@Service
public class AsyncMessageDaoImpl implements AsyncMessageDao {

	/**
	 * HBaseアクセサです。
	 */
	@Autowired  @Qualifier("default")
	private HBaseOperations hBaseOperations;

	/**
	 * タイムスタンプホルダーです。
	 */
	@Autowired
	private TimestampHolder timestampHolder;

	/**
	 * 指定したメッセージIDの非同期メッセージを取得します。
	 * @param messageId メッセージID
	 * @return 非同期メッセージ
	 */
	@Override
	public AsyncMessageDO load(String messageId) {
		return hBaseOperations.load(AsyncMessageDO.class, messageId);
	}

	/**
	 * 非同期メッセージを登録します。
	 * @param message 非同期メッセージ
	 */
	@Override
	public void create(AsyncMessageDO message) {
		message.setRegisterDateTime(timestampHolder.getTimestamp());
		message.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(message);
	}

	/**
	 * 指定したメッセージのステータスを更新します。
	 * @param messageId メッセージID
	 * @param consumerServerId 非同期サーバID
	 * @param status ステータス
	 */
	@Override
	public void updateStatus(
			String messageId,
			String consumerServerId,
			AsyncMessageStatus status) {
		AsyncMessageDO message = new AsyncMessageDO();
		message.setMessageId(messageId);
		message.setStatus(status);
		message.setConsumerServerId(consumerServerId);
		message.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(message, Path.includeProp("status,consumerServerId,modifyDateTime"));
	}

	/**
	 * 指定したメッセージIDの非同期メッセージを削除します。
	 * @param messageId メッセージID
	 */
	@Override
	public void delete(String messageId) {
		hBaseOperations.deleteByKey(AsyncMessageDO.class, messageId);
	}

	/**
	 * 非同期メッセージを返します。
	 * @param type タイプ
	 * @param limit 最大取得件数
	 * @param errorOnly エラーのみ
	 * @return エラー非同期メッセージ
	 */
	@Override
	public List<AsyncMessageDO> findByMessages(
			AsyncMessageType type, int limit, boolean errorOnly) {
		List<AsyncMessageDO> searchResult = hBaseOperations.scanWithIndex(
				AsyncMessageDO.class, "status", AsyncMessageStatus.ERROR,
				hBaseOperations.createFilterBuilder(AsyncMessageDO.class
				).appendSingleColumnValueFilter("type", CompareOp.EQUAL, type).toFilter());
		if (!errorOnly) {
			searchResult.addAll(hBaseOperations.scanWithIndex(
					AsyncMessageDO.class, "status", AsyncMessageStatus.ENTRY,
					hBaseOperations.createFilterBuilder(AsyncMessageDO.class
					).appendSingleColumnValueFilter("type", CompareOp.EQUAL, type).toFilter()));
		}
		Collections.sort(searchResult, new Comparator<AsyncMessageDO>() {

			@Override
			public int compare(AsyncMessageDO o1, AsyncMessageDO o2) {
				long diff = o1.getRegisterDateTime().getTime() - o2.getRegisterDateTime().getTime();
				if (diff == 0) {
					return 0;
				} else if (diff < 0) {
					return -1;
				} else {
					return 1;
				}
			}

		});
		List<AsyncMessageDO> result = new ArrayList<AsyncMessageDO>();
		for (int i = 0; i < searchResult.size() && i < limit; i++) {
			result.add(searchResult.get(i));
		}
		return result;
	}

}
