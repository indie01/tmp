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
import com.kickmogu.yodobashi.community.resource.domain.constants.DeliverType;
import com.kickmogu.yodobashi.community.resource.domain.constants.EffectiveSlipType;
import com.kickmogu.yodobashi.community.resource.domain.constants.OrderEntryType;


/**
<column id="OuterCustomerId"  label="外部顧客ID"     pk="true"  checkNN="true" checkLen="10" javaType="java.lang.String"/>
<column id="SlipNo" label="受注伝票番号"             pk="true" checkNN="true" checkLen="10" javaType="java.lang.String"/>
<column id="OrderEntryType" label="受注区分"         pk="false"  checkNN="true" checkLen="1" javaType="java.lang.String"/>
<column id="DeliverType" label="商品引渡方法"        pk="false" checkNN="false" checkLen="1" javaType="java.lang.String"/>
<column id="OrderEntryDate" label="受注日付"         pk="false"  checkNN="true" dateChkFormat="yyyy-MM-dd'T'HH:mm:ss'Z'" javaType="java.util.Date"/>
<column id="EffectiveSlipType" label="有効伝票区分"  pk="false"  checkNN="true" checkLen="1" javaType="java.lang.String"/>
*/
@HBaseTable(columnFamilies={
	@HBaseColumnFamily(name="cf")
}
,sizeGroup=SizeGroup.MEDIUM,
excludeBackup=true)
@SolrSchema
@Message
public class SlipHeaderDO extends DatasyncBaseDO implements AggregateOrderHeader{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7434380346646448057L;

	
	@HBaseColumn @IDParts(order=1)
	@SolrField
	@Label("外部顧客ID")
	protected String outerCustomerId;
	
	@HBaseColumn @IDParts(order=2)
	@SolrField
	@Label("受注伝票番号")
	protected String slipNo;
	
	@HBaseColumn
	@SolrField
	@Label("受注区分")
	protected OrderEntryType orderEntryType;
	
	@HBaseColumn
	@SolrField
	@Label("商品引渡方法")
	protected DeliverType deliverType;
	
	@HBaseColumn
	@SolrField
	@Label("受注日付")
	protected Date orderEntryDate;
	
	@HBaseColumn
	@SolrField
	@Label("有効伝票区分")
	protected EffectiveSlipType effectiveSlipType;
	

	@RelatedByHBase(withoutSecondalyIndex=true)
	@RelatedBySolr @Ignore
	protected @HasMany(sortBy="slipDetailNo") List<SlipDetailDO> details = Lists.newArrayList();
	
	
	public SlipHeaderDO(){}
	
	public SlipHeaderDO(String outerCustomerId, String slipNo) {
		this.outerCustomerId = outerCustomerId;
		this.slipNo = slipNo;
	}

	public String getOuterCustomerId() {
		return outerCustomerId;
	}

	public void setOuterCustomerId(String outerCustomerId) {
		this.outerCustomerId = outerCustomerId;
	}

	public String getSlipNo() {
		return slipNo;
	}

	public void setSlipNo(String slipNo) {
		this.slipNo = slipNo;
	}

	public OrderEntryType getOrderEntryType() {
		return orderEntryType;
	}

	public void setOrderEntryType(OrderEntryType orderEntryType) {
		this.orderEntryType = orderEntryType;
	}

	public DeliverType getDeliverType() {
		return deliverType;
	}

	public void setDeliverType(DeliverType deliverType) {
		this.deliverType = deliverType;
	}

	public Date getOrderEntryDate() {
		return orderEntryDate;
	}

	public void setOrderEntryDate(Date orderEntryDate) {
		this.orderEntryDate = orderEntryDate;
	}

	public EffectiveSlipType getEffectiveSlipType() {
		return effectiveSlipType;
	}

	public void setEffectiveSlipType(EffectiveSlipType effectiveSlipType) {
		this.effectiveSlipType = effectiveSlipType;
	}

	public List<SlipDetailDO> getDetails() {
		return details;
	}

	public void setDetails(List<SlipDetailDO> details) {
		this.details = details;
	}

}
