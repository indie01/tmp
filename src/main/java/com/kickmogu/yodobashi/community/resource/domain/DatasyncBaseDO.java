package com.kickmogu.yodobashi.community.resource.domain;

import java.io.Serializable;
import java.util.Date;

import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.lib.solr.annotation.SolrUniqKey;

public abstract class DatasyncBaseDO implements Serializable, DatasyncDOIF {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1842678577718015261L;
	
	@HBaseKey(idGenerator="idPartsGenerator",createTableSplitKeys={"#", "5", "A", "G", "M", "S", "Y", "e", "k", "q", "w"})
	@SolrField @SolrUniqKey
	protected String id;
	
	
	@HBaseColumn
	@SolrField
	protected Date modifyDateTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getModifyDateTime() {
		return modifyDateTime;
	}

	public void setModifyDateTime(Date modifyDateTime) {
		this.modifyDateTime = modifyDateTime;
	}

}
