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
 * カタログメーカーDB情報
 */
@SolrSchema
public class DBMakerMasterDO extends BaseWithTimestampDO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4694043101972564558L;

	@SolrField @SolrUniqKey
	private String makerMasterId;
	
	@SolrField
	private String makerCode;

	@SolrField
	private String makerName;
	
	@SolrField
	private String kanaName;

	@SolrField
	private String alphaNema;

	@SolrField
	private String properName;

	@SolrField
	private Date lastUpdate;

	@RelatedBySolr
	private @HasMany List<DBProductDetailDO> productDetails = Lists.newArrayList();

	/**
	 * @return the makerMasterId
	 */
	public String getMakerMasterId() {
		return makerMasterId;
	}

	/**
	 * @param makerMasterId the makerMasterId to set
	 */
	public void setMakerMasterId(String makerMasterId) {
		this.makerMasterId = makerMasterId;
	}

	/**
	 * @return the makerCode
	 */
	public String getMakerCode() {
		return makerCode;
	}

	/**
	 * @param makerCode the makerCode to set
	 */
	public void setMakerCode(String makerCode) {
		this.makerCode = makerCode;
	}

	/**
	 * @return the makerName
	 */
	public String getMakerName() {
		return makerName;
	}

	/**
	 * @param makerName the makerName to set
	 */
	public void setMakerName(String makerName) {
		this.makerName = makerName;
	}

	/**
	 * @return the alphaNema
	 */
	public String getAlphaNema() {
		return alphaNema;
	}

	/**
	 * @param alphaNema the alphaNema to set
	 */
	public void setAlphaNema(String alphaNema) {
		this.alphaNema = alphaNema;
	}

	/**
	 * @return the properName
	 */
	public String getProperName() {
		return properName;
	}

	/**
	 * @param properName the properName to set
	 */
	public void setProperName(String properName) {
		this.properName = properName;
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
	 * @return the kanaName
	 */
	public String getKanaName() {
		return kanaName;
	}

	/**
	 * @param kanaName the kanaName to set
	 */
	public void setKanaName(String kanaName) {
		this.kanaName = kanaName;
	}
}
