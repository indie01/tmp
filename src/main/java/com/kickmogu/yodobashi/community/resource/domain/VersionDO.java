package com.kickmogu.yodobashi.community.resource.domain;

import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.yodobashi.community.resource.domain.constants.VersionType;


/**
 * バージョン管理情報
 * @author kamiike
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.SMALL)
public class VersionDO extends BaseWithTimestampDO {

	/**
	 *
	 */
	private static final long serialVersionUID = 5561455082948908832L;

	/**
	 * バージョンタイプです。
	 */
	@HBaseKey
	private VersionType versionType;

	/**
	 * バージョンです。
	 */
	@HBaseColumn
	private Integer version;

	/**
	 * @return versionType
	 */
	public VersionType getVersionType() {
		return versionType;
	}

	/**
	 * @param versionType セットする versionType
	 */
	public void setVersionType(VersionType versionType) {
		this.versionType = versionType;
	}

	/**
	 * @return version
	 */
	public Integer getVersion() {
		return version;
	}

	/**
	 * @param version セットする version
	 */
	public void setVersion(Integer version) {
		this.version = version;
	}
}
