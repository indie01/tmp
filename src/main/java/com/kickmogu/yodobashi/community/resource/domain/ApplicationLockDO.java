package com.kickmogu.yodobashi.community.resource.domain;

import com.kickmogu.lib.core.resource.domain.BaseDO;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;


@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.TINY)
public class ApplicationLockDO extends BaseDO {

	/**
	 *
	 */
	private static final long serialVersionUID = -3025781108615386531L;

	/**
	 * ロックIDです。
	 */
	@HBaseKey
	private String lockId;

	/**
	 * @return lockId
	 */
	public String getLockId() {
		return lockId;
	}

	/**
	 * @param lockId セットする lockId
	 */
	public void setLockId(String lockId) {
		this.lockId = lockId;
	}
}
