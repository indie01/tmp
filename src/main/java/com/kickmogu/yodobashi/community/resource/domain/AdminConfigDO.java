/**
 *
 */
package com.kickmogu.yodobashi.community.resource.domain;

import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.yodobashi.community.service.annotation.AdminConfigKey;

/**
 * 管理コンフィグです。
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.TINY)
public class AdminConfigDO extends BaseWithTimestampDO {

	@AdminConfigKey
	public static final String MAIL_SEND_LIMIT_UNIT_TIME = "mailSendLimit.unitTime";

	@AdminConfigKey
	public static final String MAIL_SEND_LIMIT_SEND_COUNT = "mailSendLimit.sendCount";

	@AdminConfigKey
	public static final String REVIEW_POINT_SUMMARY_LASTEXECUTE = "reviewPointSummary.lastExecute";
	
	/**
	 *
	 */
	private static final long serialVersionUID = -7443672327567631127L;

	/**
	 * キーです。
	 */
	@HBaseKey
	private String key;

	/**
	 * 値です。
	 */
	@HBaseColumn
	private String value;

	/**
	 * @return key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key セットする key
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value セットする value
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
