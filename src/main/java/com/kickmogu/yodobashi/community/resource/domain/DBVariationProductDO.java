package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;
import java.util.List;

import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.lib.solr.annotation.SolrSchema;
import com.kickmogu.lib.solr.annotation.SolrUniqKey;

/**
 * バリエーション商品
 * @author sugimoto
 *
 */
@SolrSchema
public class DBVariationProductDO extends BaseWithTimestampDO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8394976375010478141L;

	@SolrField @SolrUniqKey
	private String productDetailId;
	
	/**
	 * 商品SKU
	 */
	@SolrField
	private String sku;
	
	/**
	 * バリエーション商品一覧
	 */
	@SolrField
	private List<String> variationProducts;
	
	@SolrField
	private Date startTime;

	@SolrField
	private Date endTime;
	
	@SolrField
	private Date lastUpdate;

	public String getProductDetailId() {
		return productDetailId;
	}

	public void setProductDetailId(String productDetailId) {
		this.productDetailId = productDetailId;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public List<String> getVariationProducts() {
		return variationProducts;
	}

	public void setVariationProducts(List<String> variationProducts) {
		this.variationProducts = variationProducts;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
}
