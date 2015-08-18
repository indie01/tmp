package com.kickmogu.yodobashi.community.resource.domain;

import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;

@HBaseTable(columnFamilies={
	@HBaseColumnFamily(name="cf")
},writeJournal=false,sizeGroup=SizeGroup.TINY,excludeBackup=true)
public class ApplicationLabelDO {

	/**
	 * ロックIDです。
	 */
	@HBaseKey
	private String id;
	
	@HBaseColumn
	private int labelAsInt;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getLabelAsInt() {
		return labelAsInt;
	}

	public void setLabelAsInt(int labelAsInt) {
		this.labelAsInt = labelAsInt;
	}


}
