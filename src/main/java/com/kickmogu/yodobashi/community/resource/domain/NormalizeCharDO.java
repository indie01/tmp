package com.kickmogu.yodobashi.community.resource.domain;

import com.kickmogu.lib.core.resource.annotation.BelongsTo;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;



/**
 * 標準化文字です。
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.TINY)
public class NormalizeCharDO extends BaseWithTimestampDO{

	/**
	 *
	 */
	private static final long serialVersionUID = -7290163389627179465L;

	/**
	 * 文字です。
	 */
	@HBaseKey
	private String character;

	/**
	 * 連番です。
	 */
	@HBaseColumn
	private int orderNo;

	/**
	 * 標準化文字グループです。
	 */
	@HBaseColumn
	private @BelongsTo NormalizeCharGroupDO group;

	/**
	 * @return character
	 */
	public String getCharacter() {
		return character;
	}

	/**
	 * @param character セットする character
	 */
	public void setCharacter(String character) {
		this.character = character;
	}

	/**
	 * @return orderNo
	 */
	public int getOrderNo() {
		return orderNo;
	}

	/**
	 * @param orderNo セットする orderNo
	 */
	public void setOrderNo(int orderNo) {
		this.orderNo = orderNo;
	}

	/**
	 * @return group
	 */
	public NormalizeCharGroupDO getGroup() {
		return group;
	}

	/**
	 * @param group セットする group
	 */
	public void setGroup(NormalizeCharGroupDO group) {
		this.group = group;
	}
}
