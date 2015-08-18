package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;
import java.util.List;

import org.msgpack.annotation.Ignore;
import org.msgpack.annotation.Message;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.id.annotation.IDParts;
import com.kickmogu.lib.core.resource.annotation.BelongsTo;
import com.kickmogu.lib.core.resource.annotation.Label;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.lib.solr.annotation.SolrSchema;
import com.kickmogu.lib.solr.io.MessagePackSolrFieldConverter;
import com.kickmogu.yodobashi.community.resource.domain.constants.SlipDetailCategory;
import com.kickmogu.yodobashi.community.resource.domain.constants.SalesRegistDetailType;



/**
	<table id="tbl_slipDetail" >
		<column id="OuterCustomerId"  label="外部顧客ID"             pk="true" checkNN="true" checkLen="10" javaType="java.lang.String"/>
		<column id="SlipNo" label="受注伝票番号"                     pk="true" checkNN="true" checkLen="10" javaType="java.lang.String"/>
		<column id="SlipDetailNo" label="明細番号"                   pk="true" checkNN="true" checkLen="6" javaType="int"/>
		<column id="SlipDetailCategory" label="受注明細カテゴリ"     pk="false" checkNN="false" checkLen="2" javaType="java.lang.String"/>
		<column id="JanCode" label="JANコード"                       pk="false" checkNN="true" checkLen="13" javaType="java.lang.String"/>
		<column id="EffectiveNum" label="有効数量"                   pk="false" checkNN="false" checkLen="15" javaType="int"/>
		<column id="SaledNum" label="販売済み数量"                   pk="false" checkNN="false" checkLen="15" javaType="int"/>
		<column id="SetCouponId" label="セットクーポンID"            pk="false" checkNN="false" checkLen="10" javaType="java.lang.String"/>
		<column id="SetParentDetailNo" label="セット親明細番号"      pk="false" checkNN="false" checkLen="6" javaType="int"/>
		<column id="SalesRegistDetailType" label="売上登録明細区分"  pk="false" checkNN="true" checkLen="1" javaType="java.lang.String"/>
	</table>
 */
@HBaseTable(columnFamilies={
	@HBaseColumnFamily(name="cf")
}
,sizeGroup=SizeGroup.LARGE,
excludeBackup=true)
@SolrSchema
@Message
public class SlipDetailDO extends DatasyncBaseDO {

	/**
	 *
	 */
	private static final long serialVersionUID = -8131442541869028090L;


	@HBaseColumn @IDParts(order=1)
	@SolrField
	@Label("外部顧客ID")
	protected String outerCustomerId;

	@HBaseColumn @IDParts(order=2)
	@SolrField
	@Label("受注伝票番号")
	protected String slipNo;

	@HBaseColumn @IDParts(order=3, isHashParts=false)
	@SolrField
	@Label("明細番号")
	protected int slipDetailNo;

	@HBaseColumn
	@SolrField
	@Label("受注明細カテゴリ")
	protected SlipDetailCategory slipDetailCategory;

	@HBaseColumn
	@SolrField
	@Label("JANコード")
	protected String janCode;

	@HBaseColumn
	@SolrField
	@Label("有効数量")
	protected int effectiveNum;

	@HBaseColumn
	@SolrField
	@Label("返品数量")
	protected int returnedNum;

	@HBaseColumn
	@SolrField
	@Label("セットクーポンID")
	protected String setCouponId;

	@HBaseColumn
	@SolrField
	@Label("セット親明細番号")
	protected int setParentDetailNo;

	@HBaseColumn
	@SolrField
	@Label("売上登録明細区分")
	protected SalesRegistDetailType salesRegistDetailType;

	@HBaseColumn
	@SolrField
	@Label("最古請求日")
	protected Date oldestBillingDate;
	
	@Ignore
	@HBaseColumn
	@SolrField(type="binary",fieldConverterClass=MessagePackSolrFieldConverter.class,indexed=false)
	protected List<SlipBillingDateDO> billingDates = Lists.newArrayList();

	@HBaseColumn @Ignore
	@SolrField
	protected @BelongsTo SlipHeaderDO header;

	public SlipDetailDO() {}

	public SlipDetailDO(String outerCustomerId, String slipNo, int slipDetailNo) {
		this.outerCustomerId = outerCustomerId;
		this.slipNo = slipNo;
		this.slipDetailNo = slipDetailNo;
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

	public int getSlipDetailNo() {
		return slipDetailNo;
	}

	public void setSlipDetailNo(int slipDetailNo) {
		this.slipDetailNo = slipDetailNo;
	}

	public SlipDetailCategory getSlipDetailCategory() {
		return slipDetailCategory;
	}

	public void setSlipDetailCategory(SlipDetailCategory slipDetailCategory) {
		this.slipDetailCategory = slipDetailCategory;
	}

	public String getJanCode() {
		return janCode;
	}

	public void setJanCode(String janCode) {
		this.janCode = janCode;
	}

	public int getEffectiveNum() {
		return effectiveNum;
	}

	public void setEffectiveNum(int effectiveNum) {
		this.effectiveNum = effectiveNum;
	}


	public int getReturnedNum() {
		return returnedNum;
	}

	public void setReturnedNum(int returnedNum) {
		this.returnedNum = returnedNum;
	}

	public String getSetCouponId() {
		return setCouponId;
	}

	public void setSetCouponId(String setCouponId) {
		this.setCouponId = setCouponId;
	}

	public int getSetParentDetailNo() {
		return setParentDetailNo;
	}

	public void setSetParentDetailNo(int setParentDetailNo) {
		this.setParentDetailNo = setParentDetailNo;
	}

	@Ignore
	public SlipHeaderDO getHeader() {
		return header;
	}

	@Ignore
	public void setHeader(SlipHeaderDO header) {
		this.header = header;
	}

	public SalesRegistDetailType getSalesRegistDetailType() {
		return salesRegistDetailType;
	}

	public void setSalesRegistDetailType(SalesRegistDetailType salesRegistDetailType) {
		this.salesRegistDetailType = salesRegistDetailType;
	}


	public Date getOldestBillingDate() {
		return oldestBillingDate;
	}

	public void setOldestBillingDate(Date oldestBillingDate) {
		this.oldestBillingDate = oldestBillingDate;
	}

	@Ignore
	public List<SlipBillingDateDO> getBillingDates() {
		return billingDates;
	}

	@Ignore
	public void setBillingDates(List<SlipBillingDateDO> billingDates) {
		this.billingDates = billingDates;
	}
	
	


}
