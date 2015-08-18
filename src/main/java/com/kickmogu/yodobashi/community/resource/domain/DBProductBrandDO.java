package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.resource.annotation.HasMany;
import com.kickmogu.lib.solr.annotation.RelatedBySolr;
import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.lib.solr.annotation.SolrSchema;
import com.kickmogu.lib.solr.annotation.SolrUniqKey;

/**
 * カタログブランドDB情報
 */
@SolrSchema
public class DBProductBrandDO extends BaseWithTimestampDO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4694043101972564558L;

	@SolrField @SolrUniqKey
	private String productBrandId;

	@SolrField
	private String brandCode;

	@SolrField
	private String brandName;
	
	@SolrField
	private String brandReading;

	@SolrField
	private long orderNo;


	@SolrField
	private Date lastUpdate;

	@RelatedBySolr
	private @HasMany List<DBProductDetailDO> productDetails = Lists.newArrayList();

	/**
	 * @return the productBrandId
	 */
	public String getProductBrandId() {
		return productBrandId;
	}

	/**
	 * @param productBrandId the productBrandId to set
	 */
	public void setProductBrandId(String productBrandId) {
		this.productBrandId = productBrandId;
	}

	/**
	 * @return the brandCode
	 */
	public String getBrandCode() {
		return brandCode;
	}

	/**
	 * @param brandCode the brandCode to set
	 */
	public void setBrandCode(String brandCode) {
		this.brandCode = brandCode;
	}

	/**
	 * @return the brandName
	 */
	public String getBrandName() {
		return brandName;
	}

	/**
	 * @param brandName the brandName to set
	 */
	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	/**
	 * @return the brandReading
	 */
	public String getBrandReading() {
		return brandReading;
	}

	/**
	 * @param brandReading the brandReading to set
	 */
	public void setBrandReading(String brandReading) {
		this.brandReading = brandReading;
	}

	/**
	 * @return the lastUpdate
	 */
	public Date getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * @param lastUpdate the lastUpdate to set
	 */
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	/**
	 * @return the productDetails
	 */
	public List<DBProductDetailDO> getProductDetails() {
		return productDetails;
	}

	/**
	 * @param productDetails the productDetails to set
	 */
	public void setProductDetails(List<DBProductDetailDO> productDetails) {
		this.productDetails = productDetails;
	}

	/**
	 * @return the orderNo
	 */
	public long getOrderNo() {
		return orderNo;
	}

	/**
	 * @param orderNo the orderNo to set
	 */
	public void setOrderNo(long orderNo) {
		this.orderNo = orderNo;
	}

	
}
