package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;
import java.util.List;

import com.kickmogu.lib.core.resource.annotation.BelongsTo;
import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.lib.solr.annotation.SolrSchema;
import com.kickmogu.lib.solr.annotation.SolrUniqKey;
import com.kickmogu.yodobashi.community.resource.domain.constants.ProductType;

/**
 * カタログ商品DB情報
 */
@SolrSchema
public class DBProductDetailDO extends BaseWithTimestampDO{

	/**
	 * 
	 */
	private static final long serialVersionUID = -815632499489313185L;
	

	@SolrField @SolrUniqKey
	private String productDetailId;
	
	@SolrField
	private String sku;
	
	/**
	 * JANコードです。
	 */
	@SolrField
	private String janCode;
	
	/**
	 * 商品名
	 */
	@SolrField
	private String productName;

	/**
	 * 短縮商品名です。
	 */
	@SolrField
	private String productNameShort;
	
	/**
	 * ブランドです。
	 */
	@SolrField
	private @BelongsTo DBProductBrandDO brand;
	
	/**
	 * メーカーです。
	 */
	@SolrField
	private @BelongsTo DBMakerMasterDO maker;

	/**
	 * 商品概要
	 */
	@SolrField
	private String listSummary;

	/**
	 * CERO
	 */
	@SolrField
	private String ceroKind;

	/**
	 * アダルト
	 */
	@SolrField
	private String adultKind;

	/**
	 * メイン画像URL（orderの一番新しいやつ）
	 */
	private List<DBItemObjectUrlDO> mainImageUrls;

	/**
	 * list画像URL（orderの一番新しいやつ）
	 */
	private List<DBItemObjectUrlDO> listImageUrls;

	@SolrField
	private Date startTime;

	@SolrField
	private Date endTime;
	
	@SolrField
	private Date lastUpdate;

	/**
	 * 掲載可否フラグ
	 */
	@SolrField
	private int enableFlag;
	
	/**
	 * 緊急停止フラグ
	 */
	@SolrField
	private String urgentStopFlg;

	/**
	 * 商品種別
	 */
	@SolrField
	private ProductType	productType;

	/**
	 * セットフラグ
	 */
	@SolrField
	private int setProductFlag;

	/**
	 * 複合フラグ
	 */
	@SolrField
	private int complexFlag;

	/**
	 * バリエーションフラグ
	 */
	@SolrField
	private int familyFlag;
	
	/**
	 * サービス商品フラグ
	 */
	@SolrField
	private int serviceFlag;

	/**
	 * @return the productDetailId
	 */
	public String getProductDetailId() {
		return productDetailId;
	}

	/**
	 * @param productDetailId the productDetailId to set
	 */
	public void setProductDetailId(String productDetailId) {
		this.productDetailId = productDetailId;
	}

	/**
	 * @return the sku
	 */
	public String getSku() {
		return sku;
	}

	/**
	 * @param sku the sku to set
	 */
	public void setSku(String sku) {
		this.sku = sku;
	}

	/**
	 * @return the janCode
	 */
	public String getJanCode() {
		return janCode;
	}

	/**
	 * @param janCode the janCode to set
	 */
	public void setJanCode(String janCode) {
		this.janCode = janCode;
	}

	/**
	 * @return the productName
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * @param productName the productName to set
	 */
	public void setProductName(String productName) {
		this.productName = productName;
	}

	/**
	 * @return the productNameShort
	 */
	public String getProductNameShort() {
		return productNameShort;
	}

	/**
	 * @param productNameShort the productNameShort to set
	 */
	public void setProductNameShort(String productNameShort) {
		this.productNameShort = productNameShort;
	}

	/**
	 * @return the brand
	 */
	public DBProductBrandDO getBrand() {
		return brand;
	}

	/**
	 * @param brand the brand to set
	 */
	public void setBrand(DBProductBrandDO brand) {
		this.brand = brand;
	}

	/**
	 * @return the maker
	 */
	public DBMakerMasterDO getMaker() {
		return maker;
	}

	/**
	 * @param maker the maker to set
	 */
	public void setMaker(DBMakerMasterDO maker) {
		this.maker = maker;
	}

	/**
	 * @return the listSummary
	 */
	public String getListSummary() {
		return listSummary;
	}

	/**
	 * @param listSummary the listSummary to set
	 */
	public void setListSummary(String listSummary) {
		this.listSummary = listSummary;
	}

	/**
	 * @return the ceroKind
	 */
	public String getCeroKind() {
		return ceroKind;
	}

	/**
	 * @param ceroKind the ceroKind to set
	 */
	public void setCeroKind(String ceroKind) {
		this.ceroKind = ceroKind;
	}

	/**
	 * @return the adultKind
	 */
	public String getAdultKind() {
		return adultKind;
	}

	/**
	 * @param adultKind the adultKind to set
	 */
	public void setAdultKind(String adultKind) {
		this.adultKind = adultKind;
	}

	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the endTime
	 */
	public Date getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
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
	 * @return the enableFlag
	 */
	public int getEnableFlag() {
		return enableFlag;
	}

	/**
	 * @param enableFlag the enableFlag to set
	 */
	public void setEnableFlag(int enableFlag) {
		this.enableFlag = enableFlag;
	}

	/**
	 * @return the urgentStopFlg
	 */
	public String getUrgentStopFlg() {
		return urgentStopFlg;
	}

	/**
	 * @param urgentStopFlg the urgentStopFlg to set
	 */
	public void setUrgentStopFlg(String urgentStopFlg) {
		this.urgentStopFlg = urgentStopFlg;
	}

	/**
	 * @return the productType
	 */
	public ProductType getProductType() {
		return productType;
	}

	/**
	 * @param productType the productType to set
	 */
	public void setProductType(ProductType productType) {
		this.productType = productType;
	}

	/**
	 * @return the setProductFlag
	 */
	public int getSetProductFlag() {
		return setProductFlag;
	}

	/**
	 * @param setProductFlag the setProductFlag to set
	 */
	public void setSetProductFlag(int setProductFlag) {
		this.setProductFlag = setProductFlag;
	}

	/**
	 * @return the complexFlag
	 */
	public int getComplexFlag() {
		return complexFlag;
	}

	/**
	 * @param complexFlag the complexFlag to set
	 */
	public void setComplexFlag(int complexFlag) {
		this.complexFlag = complexFlag;
	}

	/**
	 * @return the familyFlag
	 */
	public int getFamilyFlag() {
		return familyFlag;
	}

	/**
	 * @param familyFlag the familyFlag to set
	 */
	public void setFamilyFlag(int familyFlag) {
		this.familyFlag = familyFlag;
	}

	/**
	 * @return the serviceFlag
	 */
	public int getServiceFlag() {
		return serviceFlag;
	}

	/**
	 * @param serviceFlag the serviceFlag to set
	 */
	public void setServiceFlag(int serviceFlag) {
		this.serviceFlag = serviceFlag;
	}

	/**
	 * @return the mainImageUrls
	 */
	public List<DBItemObjectUrlDO> getMainImageUrls() {
		return mainImageUrls;
	}

	/**
	 * @param mainImageUrls the mainImageUrls to set
	 */
	public void setMainImageUrls(List<DBItemObjectUrlDO> mainImageUrls) {
		this.mainImageUrls = mainImageUrls;
	}

	/**
	 * @return the listImageUrls
	 */
	public List<DBItemObjectUrlDO> getListImageUrls() {
		return listImageUrls;
	}

	/**
	 * @param listImageUrls the listImageUrls to set
	 */
	public void setListImageUrls(List<DBItemObjectUrlDO> listImageUrls) {
		this.listImageUrls = listImageUrls;
	}	

	
}
