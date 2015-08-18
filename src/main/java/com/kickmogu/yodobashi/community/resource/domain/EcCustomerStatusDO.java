package com.kickmogu.yodobashi.community.resource.domain;

import org.msgpack.annotation.Message;

import com.kickmogu.lib.core.id.annotation.IDParts;
import com.kickmogu.lib.core.resource.annotation.Label;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.lib.solr.annotation.SolrSchema;
import com.kickmogu.yodobashi.community.resource.domain.constants.EcCustomerStatus;

@HBaseTable(columnFamilies={
	@HBaseColumnFamily(name="cf")
}
,sizeGroup=SizeGroup.SMALL)
@SolrSchema
@Message
public class EcCustomerStatusDO extends DatasyncBaseDO {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9067317948814064760L;

	
	@HBaseColumn @IDParts(order=1)
	@SolrField
	@Label("外部顧客ID")
	protected String outerCustomerId;

	@HBaseColumn
	@SolrField
	@Label("ステータス")
	protected EcCustomerStatus status;


	public String getOuterCustomerId() {
		return outerCustomerId;
	}

	public void setOuterCustomerId(String outerCustomerId) {
		this.outerCustomerId = outerCustomerId;
	}

	public EcCustomerStatus getStatus() {
		return status;
	}

	public void setStatus(EcCustomerStatus status) {
		this.status = status;
	}

	
}
