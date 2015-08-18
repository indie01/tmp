package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;

import com.kickmogu.lib.core.resource.annotation.Label;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.lib.solr.annotation.SolrSchema;
import com.kickmogu.lib.solr.annotation.SolrUniqKey;
import com.kickmogu.yodobashi.community.resource.domain.constants.SlipType;

/**
 * 購入商品詳細情報です。
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
},sizeGroup=SizeGroup.LARGE,
excludeBackup=true)
@SolrSchema
public class PurchaseProductDetailDO extends BaseWithTimestampDO {


	/**
	 *
	 */
	private static final long serialVersionUID = 8944146099501165270L;

	/**
	 * 購入商品詳細ID です。
	 */
	// TODO 外部顧客IDのキー分割
	@HBaseKey
	@SolrField @SolrUniqKey
	private String purchaseProductDetailId;

	/**
	 * 外部顧客IDです。
	 */
	@HBaseColumn
	@SolrField
	@Label("外部顧客ID")
	private String outerCustomerId;

	/**
	 * SKUです。
	 */
	@HBaseColumn
	@SolrField
	@Label("SKU")
	private String sku;

	/**
	 * JANコード
	 */
	@HBaseColumn
	@SolrField
	@Label("JANコード")
	private String janCode;

	/**
	 * 注文日です。<br />
	 * 数量が 1 以上で、最も古い購入日のデータ（受注情報、売り上げログ）内の請求日が該当します。
	 */
	@HBaseColumn
	@SolrField
	@Label("注文日")
	private Date orderDate;

	/**
	 * 注文日を決定したデータのタイプです。
	 */
	@HBaseColumn
	@SolrField
	@Label("注文日参照データタイプ")
	private SlipType orderDateRefDataType;

	/**
	 * 注文日を決定した受注情報、もしくは売り上げログを一意に識別するIDです。
	 */
	@HBaseColumn
	@SolrField
	@Label("注文日参照ID")
	private String orderDateRefId;

	/**
	 * 請求日です。<br />
	 * 数量が 1 以上かつ売上登録明細区分が「1：対象」のデータ（受注情報、売り上げログ）の中で、
	 * 最も古い購入日のデータ内の請求日が該当します。
	 */
	@HBaseColumn
	@SolrField
	@Label("請求日")
	private Date billingDate;

	/**
	 * 購入日を決定したデータのタイプです。
	 */
	@HBaseColumn
	@SolrField
	@Label("請求日参照データタイプ")
	private SlipType billingDateRefDataType;

	/**
	 * 請求日を決定した受注情報、もしくは売り上げログを一意に識別するIDです。
	 */
	@HBaseColumn
	@SolrField
	@Label("請求日参照ID")
	private String billingDateRefId;

	/**
	 * @return purchaseProductDetailId
	 */
	public String getPurchaseProductDetailId() {
		return purchaseProductDetailId;
	}

	/**
	 * @param purchaseProductDetailId セットする purchaseProductDetailId
	 */
	public void setPurchaseProductDetailId(String purchaseProductDetailId) {
		this.purchaseProductDetailId = purchaseProductDetailId;
	}

	/**
	 * @return outerCustomerId
	 */
	public String getOuterCustomerId() {
		return outerCustomerId;
	}

	/**
	 * @param outerCustomerId セットする outerCustomerId
	 */
	public void setOuterCustomerId(String outerCustomerId) {
		this.outerCustomerId = outerCustomerId;
	}

	/**
	 * @return sku
	 */
	public String getSku() {
		return sku;
	}

	/**
	 * @param sku セットする sku
	 */
	public void setSku(String sku) {
		this.sku = sku;
	}

	/**
	 * @return billingDate
	 */
	public Date getBillingDate() {
		return billingDate;
	}

	/**
	 * @param billingDate セットする billingDate
	 */
	public void setBillingDate(Date billingDate) {
		this.billingDate = billingDate;
	}

	/**
	 * @return billingDateRefId
	 */
	public String getBillingDateRefId() {
		return billingDateRefId;
	}

	/**
	 * @param billingDateRefId セットする billingDateRefId
	 */
	public void setBillingDateRefId(String billingDateRefId) {
		this.billingDateRefId = billingDateRefId;
	}

	/**
	 * @return janCode
	 */
	public String getJanCode() {
		return janCode;
	}

	/**
	 * @param janCode セットする janCode
	 */
	public void setJanCode(String janCode) {
		this.janCode = janCode;
	}

	/**
	 * @return billingDateRefDataType
	 */
	public SlipType getBillingDateRefDataType() {
		return billingDateRefDataType;
	}

	/**
	 * @param billingDateRefDataType セットする billingDateRefDataType
	 */
	public void setBillingDateRefDataType(SlipType billingDateRefDataType) {
		this.billingDateRefDataType = billingDateRefDataType;
	}

	/**
	 * @return orderDate
	 */
	public Date getOrderDate() {
		return orderDate;
	}

	/**
	 * @param orderDate セットする orderDate
	 */
	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	/**
	 * @return orderDateRefDataType
	 */
	public SlipType getOrderDateRefDataType() {
		return orderDateRefDataType;
	}

	/**
	 * @param orderDateRefDataType セットする orderDateRefDataType
	 */
	public void setOrderDateRefDataType(SlipType orderDateRefDataType) {
		this.orderDateRefDataType = orderDateRefDataType;
	}

	/**
	 * @return orderDateRefId
	 */
	public String getOrderDateRefId() {
		return orderDateRefId;
	}

	/**
	 * @param orderDateRefId セットする orderDateRefId
	 */
	public void setOrderDateRefId(String orderDateRefId) {
		this.orderDateRefId = orderDateRefId;
	}
}
