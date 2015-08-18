package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;

import com.kickmogu.lib.core.resource.annotation.BelongsTo;
import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.lib.solr.annotation.SolrSchema;
import com.kickmogu.lib.solr.annotation.SolrUniqKey;

/**
 * カタログDB情報
 */
/**
 * @author takahashi
 *
 */
@SolrSchema
public class DBReviewPointQuestDetailDO extends BaseWithTimestampDO{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8068817208194150924L;

	@SolrField @SolrUniqKey
	private String reviewPointQuestDetailId;
	
	/**
	 * 商品コード
	 */
	@SolrField
	private String sku;

	/**
	 * 有効開始日時
	 */
	@SolrField
	private Date startTime;

	/**
	 * 有効終了日時
	 */
	@SolrField
	private Date endTime;

	/**
	 * レビュー設問コード
	 */
	@SolrField
	private String rqCode;

	/**
	 * 設問有効開始日時
	 */
	@SolrField
	private Date rqStartTime;
	
	/**
	 * 設問有効終了日時
	 */
	@SolrField
	private Date rqEndTime;

	/**
	 * レビュー設問閾値開始日数
	 */
	@SolrField
	private int rqStartThreshold;

	/**
	 * レビュー設問閾値終了日数
	 */
	@SolrField
	private int rqEndThreshold;

	/**
	 * 基本レビューポイント額
	 */
	@SolrField
	private long rqBaseReviewPointValue;

	/**
	 * 最終更新日時
	 */
	@SolrField
	private Date lastUpdate;

	/**
	 * 
	 */
	@SolrField
	private @BelongsTo DBReviewPointQuestDO reviewPointQuest;

	/**
	 * @return the reviewPointQuestDetailId
	 */
	public String getReviewPointQuestDetailId() {
		return reviewPointQuestDetailId;
	}

	/**
	 * @param reviewPointQuestDetailId the reviewPointQuestDetailId to set
	 */
	public void setReviewPointQuestDetailId(String reviewPointQuestDetailId) {
		this.reviewPointQuestDetailId = reviewPointQuestDetailId;
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
	 * @return the rqCode
	 */
	public String getRqCode() {
		return rqCode;
	}

	/**
	 * @param rqCode the rqCode to set
	 */
	public void setRqCode(String rqCode) {
		this.rqCode = rqCode;
	}

	/**
	 * @return the rqStartTime
	 */
	public Date getRqStartTime() {
		return rqStartTime;
	}

	/**
	 * @param rqStartTime the rqStartTime to set
	 */
	public void setRqStartTime(Date rqStartTime) {
		this.rqStartTime = rqStartTime;
	}

	/**
	 * @return the rqEndTime
	 */
	public Date getRqEndTime() {
		return rqEndTime;
	}

	/**
	 * @param rqEndTime the rqEndTime to set
	 */
	public void setRqEndTime(Date rqEndTime) {
		this.rqEndTime = rqEndTime;
	}

	/**
	 * @return the rqStartThreshold
	 */
	public int getRqStartThreshold() {
		return rqStartThreshold;
	}

	/**
	 * @param rqStartThreshold the rqStartThreshold to set
	 */
	public void setRqStartThreshold(int rqStartThreshold) {
		this.rqStartThreshold = rqStartThreshold;
	}

	/**
	 * @return the rqEndThreshold
	 */
	public int getRqEndThreshold() {
		return rqEndThreshold;
	}

	/**
	 * @param rqEndThreshold the rqEndThreshold to set
	 */
	public void setRqEndThreshold(int rqEndThreshold) {
		this.rqEndThreshold = rqEndThreshold;
	}

	/**
	 * @return the rqBaseReviewPointValue
	 */
	public long getRqBaseReviewPointValue() {
		return rqBaseReviewPointValue;
	}

	/**
	 * @param rqBaseReviewPointValue the rqBaseReviewPointValue to set
	 */
	public void setRqBaseReviewPointValue(long rqBaseReviewPointValue) {
		this.rqBaseReviewPointValue = rqBaseReviewPointValue;
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
	 * @return the reviewPointQuest
	 */
	public DBReviewPointQuestDO getReviewPointQuest() {
		return reviewPointQuest;
	}

	/**
	 * @param reviewPointQuest the reviewPointQuest to set
	 */
	public void setReviewPointQuest(DBReviewPointQuestDO reviewPointQuest) {
		this.reviewPointQuest = reviewPointQuest;
	}

}
