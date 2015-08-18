package com.kickmogu.yodobashi.community.resource.domain;

import java.util.List;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.resource.annotation.HasMany;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.RelatedByHBase;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;



/**
 * 標準化文字グループです。
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.TINY)
public class NormalizeCharGroupDO extends BaseWithTimestampDO{

	/**
	 *
	 */
	private static final long serialVersionUID = 1868528560848167593L;

	/**
	 * グループIDです。
	 */
	@HBaseKey
	private String groupId;

	/**
	 * 文字リストです。
	 */
	@RelatedByHBase(withoutSecondalyIndex=true)
	private @HasMany List<NormalizeCharDO> chars = Lists.newArrayList();

	/**
	 * @return groupId
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId セットする groupId
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * @return chars
	 */
	public List<NormalizeCharDO> getChars() {
		return chars;
	}

	/**
	 * @param chars セットする chars
	 */
	public void setChars(List<NormalizeCharDO> chars) {
		this.chars = chars;
	}
}
