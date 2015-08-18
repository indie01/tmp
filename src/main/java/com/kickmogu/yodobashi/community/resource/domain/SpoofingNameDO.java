package com.kickmogu.yodobashi.community.resource.domain;

import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseIndex;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;

/**
 * なりすまし判定クラスです。
 * @author takahashi
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.SMALL)

public class SpoofingNameDO {

	@HBaseKey
	private String spoofingNameId;
	
	@HBaseColumn
	@HBaseIndex
	private String  spoofingPattern;
	
	@HBaseColumn
	private String  spoofingName;

	/**
	 * @return the spoofingNameId
	 */
	public String getSpoofingNameId() {
		return spoofingNameId;
	}

	/**
	 * @param spoofingNameId the spoofingNameId to set
	 */
	public void setSpoofingNameId(String spoofingNameId) {
		this.spoofingNameId = spoofingNameId;
	}

	/**
	 * @return the spoofingPattern
	 */
	public String getSpoofingPattern() {
		return spoofingPattern;
	}

	/**
	 * @param spoofingPattern the spoofingPattern to set
	 */
	public void setSpoofingPattern(String spoofingPattern) {
		this.spoofingPattern = spoofingPattern;
	}

	/**
	 * @return the spoofingName
	 */
	public String getSpoofingName() {
		return spoofingName;
	}

	/**
	 * @param spoofingName the spoofingName to set
	 */
	public void setSpoofingName(String spoofingName) {
		this.spoofingName = spoofingName;
	}
	
}
