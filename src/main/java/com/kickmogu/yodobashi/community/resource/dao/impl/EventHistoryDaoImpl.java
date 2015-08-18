/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.yodobashi.community.resource.dao.EventHistoryDao;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.domain.EventHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.EventHistoryType;


/**
 * イベント履歴 DAO です。
 * @author kamiike
 *
 */
@Service
public class EventHistoryDaoImpl implements EventHistoryDao {

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
	 * イベント履歴を登録します。
	 * @param uniqueId イベントごとにユニークなID
	 * @param eventHistoryType イベント履歴タイプ
	 */
	@Override
	public void saveLog(
			String uniqueId, EventHistoryType eventHistoryType) {
		EventHistoryDO event = new EventHistoryDO();
		event.setEventHistoryId(
				IdUtil.createIdByConcatIds(uniqueId,
						eventHistoryType.getCode()));
		event.setEventHistoryType(eventHistoryType);
		event.setRegisterDateTime(timestampHolder.getTimestamp());
		event.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(event);
	}

	/**
	 * イベント履歴が登録済みか判定します。
	 * @param uniqueId イベントごとにユニークなID
	 * @param eventHistoryType イベント履歴タイプ
	 * @return 既に登録済みの場合、true
	 */
	@Override
	public boolean existsLog(
			String uniqueId, EventHistoryType eventHistoryType) {
		return hBaseOperations.load(EventHistoryDO.class,
				IdUtil.createIdByConcatIds(uniqueId,
						eventHistoryType.getCode()), Path.includeProp("eventHistoryId")) != null;
	}
}
