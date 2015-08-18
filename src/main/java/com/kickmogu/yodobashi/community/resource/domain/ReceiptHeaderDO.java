package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;
import java.util.List;

import org.msgpack.annotation.Ignore;
import org.msgpack.annotation.Message;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.id.annotation.IDParts;
import com.kickmogu.lib.core.resource.annotation.HasMany;
import com.kickmogu.lib.core.resource.annotation.Label;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.RelatedByHBase;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.lib.solr.annotation.RelatedBySolr;
import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.lib.solr.annotation.SolrSchema;
import com.kickmogu.yodobashi.community.resource.domain.constants.EffectiveSlipType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReceiptRegistType;


/**
	<table id="tbl_receiptHeader" >
		<column id="OuterCustomerId"  label="外部顧客ID"     pk="true"  checkNN="true" checkLen="10" javaType="java.lang.String"/>
		<column id="ReceiptNo" label="POSレシート番号"       pk="true"  checkNN="true" checkLen="24" javaType="java.lang.String"/>
		<column id="ReceiptRegistType" label="登録区分"      pk="false"  checkNN="true" checkLen="1" javaType="java.lang.String"/>
		<column id="SaledDate" label="売上日付(営業日)"            pk="false"  checkNN="true" checkLen="" javaType="java.util.Date"/>
		<column id="EffectiveSlipType" label="有効伝票区分"  pk="false"  checkNN="true" checkLen="1" javaType="java.lang.String"/>
	</table>
 *
 */
@HBaseTable(columnFamilies={
	@HBaseColumnFamily(name="cf")
}
,sizeGroup=SizeGroup.LARGE,
excludeBackup=true)
@SolrSchema
@Message
public class ReceiptHeaderDO extends DatasyncBaseDO implements AggregateOrderHeader {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6035397554329728558L;

	
	@HBaseColumn @IDParts(order=1)
	@SolrField
	@Label("外部顧客ID")
	protected String outerCustomerId;

	@HBaseColumn @IDParts(order=2)
	@SolrField
	@Label("POSレシート番号")
	protected String receiptNo;
	
	@HBaseColumn
	@SolrField
	@Label("登録区分")
	protected ReceiptRegistType receiptRegistType;
	
	@HBaseColumn
	@SolrField
	@Label("売上日付(営業日)")
	protected Date salesDate;

	@HBaseColumn
	@SolrField
	@Label("有効伝票区分")
	protected EffectiveSlipType effectiveSlipType;
	
	@Ignore
	@RelatedByHBase(withoutSecondalyIndex=true)
	@RelatedBySolr
	protected @HasMany(sortBy="receiptDetailNo") List<ReceiptDetailDO> details = Lists.newArrayList();


	public String getOuterCustomerId() {
		return outerCustomerId;
	}

	public void setOuterCustomerId(String outerCustomerId) {
		this.outerCustomerId = outerCustomerId;
	}

	public String getReceiptNo() {
		return receiptNo;
	}

	public void setReceiptNo(String receiptNo) {
		this.receiptNo = receiptNo;
	}

	public ReceiptRegistType getReceiptRegistType() {
		return receiptRegistType;
	}

	public void setReceiptRegistType(ReceiptRegistType receiptRegistType) {
		this.receiptRegistType = receiptRegistType;
	}

	public Date getSalesDate() {
		return salesDate;
	}

	public void setSalesDate(Date salesDate) {
		this.salesDate = salesDate;
	}

	public EffectiveSlipType getEffectiveSlipType() {
		return effectiveSlipType;
	}

	public void setEffectiveSlipType(EffectiveSlipType effectiveSlipType) {
		this.effectiveSlipType = effectiveSlipType;
	}

	public List<ReceiptDetailDO> getDetails() {
		return details;
	}

	public void setDetails(List<ReceiptDetailDO> details) {
		this.details = details;
	}
	
}
