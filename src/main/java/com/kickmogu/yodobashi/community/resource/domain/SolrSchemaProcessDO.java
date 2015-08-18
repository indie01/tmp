package com.kickmogu.yodobashi.community.resource.domain;

import com.kickmogu.lib.core.id.annotation.IDParts;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.yodobashi.community.resource.domain.constants.SolrProcessStatus;


@HBaseTable(columnFamilies={
	@HBaseColumnFamily(name="cf")
},sizeGroup=SizeGroup.TINY,excludeBackup=true)
public class SolrSchemaProcessDO {

	@HBaseKey(idGenerator="idPartsGenerator")
	private String id;
	
	
	@HBaseColumn @IDParts
	private Class<?> schemaType;
	
	
	@HBaseColumn @IDParts(isHashParts=false)
	private String nodeId;
	
	private SolrProcessStatus status;

	public SolrSchemaProcessDO(){}

	public SolrSchemaProcessDO(Class<?> schemaType, String nodeId){
		this.schemaType = schemaType;
		this.nodeId = nodeId;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Class<?> getSchemaType() {
		return schemaType;
	}

	public void setSchemaType(Class<?> schemaType) {
		this.schemaType = schemaType;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public SolrProcessStatus getStatus() {
		return status;
	}

	public void setStatus(SolrProcessStatus status) {
		this.status = status;
	}

	
}
