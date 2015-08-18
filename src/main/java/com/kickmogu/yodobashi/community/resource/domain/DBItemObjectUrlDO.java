package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;

import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.lib.solr.annotation.SolrSchema;
import com.kickmogu.lib.solr.annotation.SolrUniqKey;

/**
 * カタログ商品DB情報
 */
@SolrSchema
public class DBItemObjectUrlDO extends BaseWithTimestampDO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5297735718548162851L;

	@SolrField @SolrUniqKey
	private String itemObujectUrlId;
	
	/**
	 * SKU
	 */
	@SolrField
	private String sku;

	/**
	 * オブジェクトタイプコード
	 **/
	@SolrField
	private String objectTypedCode;

	/**
	 * タイトル
	 **/
	@SolrField
	private String title;

	/**
	 * 代替テキスト
	 **/
	@SolrField
	private String alt;

	/**
	 * 説明文
	 **/
	@SolrField
	private String note;

	/**
	 * 優先順位
	 **/
	@SolrField
	private int priorityLeve;
	
	/**
	 * 最終更新日時
	 **/
	@SolrField
	private Date lastUpdate;
	
	/**
	 * 差し替えカウンタ
	 **/
	@SolrField
	private String replaceCount;

	/**
	 * @return the itemObujectUrlId
	 */
	public String getItemObujectUrlId() {
		return itemObujectUrlId;
	}

	/**
	 * @param itemObujectUrlId the itemObujectUrlId to set
	 */
	public void setItemObujectUrlId(String itemObujectUrlId) {
		this.itemObujectUrlId = itemObujectUrlId;
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
	 * @return the objectTypedCode
	 */
	public String getObjectTypedCode() {
		return objectTypedCode;
	}

	/**
	 * @param objectTypedCode the objectTypedCode to set
	 */
	public void setObjectTypedCode(String objectTypedCode) {
		this.objectTypedCode = objectTypedCode;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the alt
	 */
	public String getAlt() {
		return alt;
	}

	/**
	 * @param alt the alt to set
	 */
	public void setAlt(String alt) {
		this.alt = alt;
	}

	/**
	 * @return the note
	 */
	public String getNote() {
		return note;
	}

	/**
	 * @param note the note to set
	 */
	public void setNote(String note) {
		this.note = note;
	}

	/**
	 * @return the priorityLeve
	 */
	public int getPriorityLeve() {
		return priorityLeve;
	}

	/**
	 * @param priorityLeve the priorityLeve to set
	 */
	public void setPriorityLeve(int priorityLeve) {
		this.priorityLeve = priorityLeve;
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
	 * @return the replaceCount
	 */
	public String getReplaceCount() {
		return replaceCount;
	}

	/**
	 * @param replaceCount the replaceCount to set
	 */
	public void setReplaceCount(String replaceCount) {
		this.replaceCount = replaceCount;
	}

	
}
