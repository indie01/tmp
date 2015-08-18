package com.kickmogu.yodobashi.community.resource.domain;

import java.io.Serializable;

import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;

@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.TINY)
public class AppConfigurationDO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2801556258343670298L;

	@HBaseKey
	private String key;
	
	@HBaseColumn
	private String value;
	
	public AppConfigurationDO(){}
	
	public AppConfigurationDO(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
