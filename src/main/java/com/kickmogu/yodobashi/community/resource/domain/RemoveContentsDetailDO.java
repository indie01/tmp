package com.kickmogu.yodobashi.community.resource.domain;

import com.kickmogu.lib.core.resource.annotation.BelongsTo;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.lib.solr.annotation.SolrSchema;
import com.kickmogu.lib.solr.annotation.SolrUniqKey;
import com.kickmogu.yodobashi.community.resource.domain.constants.RemoveContentsType;

@HBaseTable(columnFamilies={
	@HBaseColumnFamily(name="cf")
}
,sizeGroup=SizeGroup.SMALL)
@SolrSchema
public class RemoveContentsDetailDO extends BaseWithTimestampDO  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 711030886719686356L;

	/**
	 * 削除対象コンテンツIDです。
	 */
	@HBaseKey(createTableSplitKeys={"1","5","9","D","H","L","P","T","X","b","f","j","n","r","v"})
	@SolrField @SolrUniqKey
	private String removeContentsDetailId;

	@HBaseColumn
	@SolrField
	private RemoveContentsType removeContentsType;
	
	@HBaseColumn
	@SolrField
	private String contentsId;

	@HBaseColumn
	@SolrField
	private @BelongsTo RemoveContentsDO removeContents;
	
	/**
	 * @return the removeContentsDetailId
	 */
	public String getRemoveContentsDetailId() {
		return removeContentsDetailId;
	}

	/**
	 * @param removeContentsDetailId the removeContentsDetailId to set
	 */
	public void setRemoveContentsDetailId(String removeContentsDetailId) {
		this.removeContentsDetailId = removeContentsDetailId;
	}

	/**
	 * @return the removeContentsType
	 */
	public RemoveContentsType getRemoveContentsType() {
		return removeContentsType;
	}

	/**
	 * @param removeContentsType the removeContentsType to set
	 */
	public void setRemoveContentsType(RemoveContentsType removeContentsType) {
		this.removeContentsType = removeContentsType;
	}

	/**
	 * @return the contentsId
	 */
	public String getContentsId() {
		return contentsId;
	}

	/**
	 * @param contentsId the contentsId to set
	 */
	public void setContentsId(String contentsId) {
		this.contentsId = contentsId;
	}

	/**
	 * @return the removeContents
	 */
	public RemoveContentsDO getRemoveContents() {
		return removeContents;
	}

	/**
	 * @param removeContents the removeContents to set
	 */
	public void setRemoveContents(RemoveContentsDO removeContents) {
		this.removeContents = removeContents;
	}
	
	
	
}
