package com.kickmogu.yodobashi.community.resource.domain;

import org.msgpack.annotation.Ignore;
import org.msgpack.annotation.Message;

import com.kickmogu.lib.core.id.annotation.IDParts;
import com.kickmogu.lib.core.resource.annotation.BelongsTo;
import com.kickmogu.lib.core.resource.annotation.Label;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.lib.solr.annotation.SolrSchema;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReceiptDetailType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReceiptType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SalesRegistDetailType;


/**
	<table id="tbl_receiptDetail" >
		<column id="OuterCustomerId"  label="外部顧客ID"                 pk="true"  checkNN="true" checkLen="10" javaType="java.lang.String"/>
		<column id="ReceiptNo" label="POSレシート番号"                   pk="true"  checkNN="true" checkLen="24" javaType="java.lang.String"/>
		<column id="ReceiptDetailNo"  label="明細番号"                   pk="true"  checkNN="true" checkLen="6" javaType="int"/>
		<column id="ReceiptType"  label="識別ID"                         pk="false"  checkNN="true" checkLen="1" javaType="java.lang.String"/>
		<column id="ReceiptDetailType"  label="明細区分"                 pk="false"  checkNN="true" checkLen="1" javaType="java.lang.String"/>
		<column id="SetReceiptDetailNo"  label="セット品明細番号"        pk="false"  checkNN="false" checkLen="2" javaType="java.lang.String"/>
		<column id="JanCode" label="JANコード"                           pk="false" checkNN="true" checkLen="13" javaType="java.lang.String"/>
		<column id="NetNum"  label="正味数量"                            pk="false"  checkNN="false" checkLen="15" javaType="int"/>
		<column id="RefSlipNo"  label="参照伝票番号"                     pk="false"  checkNN="false" checkLen="10" javaType="java.lang.String"/>
		<column id="RefSalesDetailNo"  label="売上伝票照会の元明細番号"  pk="false"  checkNN="false" checkLen="6" javaType="int"/>
		<column id="RefSlipDetailNo"  label="各種伝票照会の元明細番号"   pk="false"  checkNN="false" checkLen="6" javaType="int"/>
		<column id="OrderEntryDetailNo"  label="受注受付明細番号"        pk="false"  checkNN="false" checkLen="6" javaType="int"/>
		<column id="NotRegistDetailNo"  label="未登録伝票の明細番号"     pk="false"  checkNN="false" checkLen="6" javaType="int"/>
		<column id="SetCouponId"  label="セットクーポンID"               pk="false"  checkNN="false" checkLen="10" javaType="java.lang.String"/>
		<column id="SalesRegistDetailType"  label="売上登録明細区分"     pk="false"  checkNN="true" checkLen="1" javaType="java.lang.String"/>
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
public class ReceiptDetailDO extends DatasyncBaseDO {

	/**
	 *
	 */
	private static final long serialVersionUID = -1020408103864337969L;


	@HBaseColumn @IDParts(order=1)
	@SolrField
	@Label("外部顧客ID")
	protected String outerCustomerId;

	@HBaseColumn @IDParts(order=2)
	@SolrField
	@Label("POSレシート番号")
	protected String receiptNo;

	@HBaseColumn @IDParts(order=3, isHashParts=false)
	@SolrField
	@Label("明細番号")
	protected int receiptDetailNo;

	@HBaseColumn
	@SolrField
	@Label("識別ID")
	protected ReceiptType receiptType;

	@HBaseColumn
	@SolrField
	@Label("明細区分")
	protected ReceiptDetailType receiptDetailType;

	@HBaseColumn
	@SolrField
	@Label("セット品明細番号")
	protected String setReceiptDetailNo;

	@HBaseColumn
	@SolrField
	@Label("JANコード")
	protected String janCode;

	@HBaseColumn
	@SolrField
	@Label("正味数量")
	protected int netNum;

	@HBaseColumn
	@SolrField
	@Label("参照伝票番号")
	protected String refSlipNo;

	@HBaseColumn
	@SolrField
	@Label("売上伝票照会の元明細番号")
	protected int refSalesDetailNo;

	@HBaseColumn
	@SolrField
	@Label("各種伝票照会の元明細番号")
	protected int refSlipDetailNo;

	@HBaseColumn
	@SolrField
	@Label("受注受付明細番号")
	protected int orderEntryDetailNo;

	@HBaseColumn
	@SolrField
	@Label("未登録伝票の明細番号")
	protected int notRegistDetailNo;

	@HBaseColumn
	@SolrField
	@Label("セットクーポンID")
	protected String setCouponId;

	@HBaseColumn
	@SolrField
	@Label("売上登録明細区分")
	protected SalesRegistDetailType salesRegistDetailType;

	@HBaseColumn @Ignore
	@SolrField
	protected @BelongsTo ReceiptHeaderDO header;


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

	public int getReceiptDetailNo() {
		return receiptDetailNo;
	}

	public void setReceiptDetailNo(int receiptDetailNo) {
		this.receiptDetailNo = receiptDetailNo;
	}

	public ReceiptType getReceiptType() {
		return receiptType;
	}

	public void setReceiptType(ReceiptType receiptType) {
		this.receiptType = receiptType;
	}

	public ReceiptDetailType getReceiptDetailType() {
		return receiptDetailType;
	}

	public void setReceiptDetailType(ReceiptDetailType receiptDetailType) {
		this.receiptDetailType = receiptDetailType;
	}

	public String getSetReceiptDetailNo() {
		return setReceiptDetailNo;
	}

	public void setSetReceiptDetailNo(String setReceiptDetailNo) {
		this.setReceiptDetailNo = setReceiptDetailNo;
	}

	public String getJanCode() {
		return janCode;
	}

	public void setJanCode(String janCode) {
		this.janCode = janCode;
	}

	public int getNetNum() {
		return netNum;
	}

	public void setNetNum(int netNum) {
		this.netNum = netNum;
	}

	public String getRefSlipNo() {
		return refSlipNo;
	}

	public void setRefSlipNo(String refSlipNo) {
		this.refSlipNo = refSlipNo;
	}

	public int getRefSalesDetailNo() {
		return refSalesDetailNo;
	}

	public void setRefSalesDetailNo(int refSalesDetailNo) {
		this.refSalesDetailNo = refSalesDetailNo;
	}

	public int getRefSlipDetailNo() {
		return refSlipDetailNo;
	}

	public void setRefSlipDetailNo(int refSlipDetailNo) {
		this.refSlipDetailNo = refSlipDetailNo;
	}

	public int getOrderEntryDetailNo() {
		return orderEntryDetailNo;
	}

	public void setOrderEntryDetailNo(int orderEntryDetailNo) {
		this.orderEntryDetailNo = orderEntryDetailNo;
	}

	public int getNotRegistDetailNo() {
		return notRegistDetailNo;
	}

	public void setNotRegistDetailNo(int notRegistDetailNo) {
		this.notRegistDetailNo = notRegistDetailNo;
	}

	public String getSetCouponId() {
		return setCouponId;
	}

	public void setSetCouponId(String setCouponId) {
		this.setCouponId = setCouponId;
	}

	public SalesRegistDetailType getSalesRegistDetailType() {
		return salesRegistDetailType;
	}

	public void setSalesRegistDetailType(SalesRegistDetailType salesRegistDetailType) {
		this.salesRegistDetailType = salesRegistDetailType;
	}

	@Ignore
	public ReceiptHeaderDO getHeader() {
		return header;
	}

	@Ignore
	public void setHeader(ReceiptHeaderDO header) {
		this.header = header;
	}



}
