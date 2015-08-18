package com.kickmogu.yodobashi.community.resource.domain;

import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseIndex;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.yodobashi.community.resource.domain.constants.EventHistoryType;


@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.LARGE)
public class EventHistoryDO extends BaseWithTimestampDO {

	/**
	 *
	 */
	private static final long serialVersionUID = -65649235752333419L;

	/**
	 * イベント履歴IDです。
	 */
	// TODO キー分割
	@HBaseKey
	private String eventHistoryId;

	/**
	 * イベント履歴タイプです。
	 */
	@HBaseColumn
	@HBaseIndex
	private EventHistoryType eventHistoryType;

	/**
	 * @return eventHistoryId
	 */
	public String getEventHistoryId() {
		return eventHistoryId;
	}

	/**
	 * @param eventHistoryId セットする eventHistoryId
	 */
	public void setEventHistoryId(String eventHistoryId) {
		this.eventHistoryId = eventHistoryId;
	}

	/**
	 * @return eventHistoryType
	 */
	public EventHistoryType getEventHistoryType() {
		return eventHistoryType;
	}

	/**
	 * @param eventHistoryType セットする eventHistoryType
	 */
	public void setEventHistoryType(EventHistoryType eventHistoryType) {
		this.eventHistoryType = eventHistoryType;
	}
}
