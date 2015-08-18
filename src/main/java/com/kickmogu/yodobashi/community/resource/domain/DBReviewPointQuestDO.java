package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.resource.annotation.BelongsTo;
import com.kickmogu.lib.core.resource.annotation.HasMany;
import com.kickmogu.lib.solr.annotation.RelatedBySolr;
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
public class DBReviewPointQuestDO extends BaseWithTimestampDO{


	/**
	 * 
	 */
	private static final long serialVersionUID = -8068817208194150924L;

	@SolrField @SolrUniqKey
	private String reviewPointQuestId;
	
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
	 * 優先順位
	 */
	@SolrField
	private int orderNo;

	/**
	 * 基本レビューポイント
	 */
	@RelatedBySolr
	private @HasMany List<DBReviewPointQuestDetailDO> reviewPointQuestDetails = Lists.newArrayList();

	/**
	 * 最終更新日時
	 */
	@SolrField
	private Date lastUpdate;

	/**
	 * 
	 */
	@SolrField
	private @BelongsTo DBReviewPointDO reviewPoint;
	
	/**
	 * @return the reviewPointQuestId
	 */
	public String getReviewPointQuestId() {
		return reviewPointQuestId;
	}

	/**
	 * @param reviewPointQuestId the reviewPointQuestId to set
	 */
	public void setReviewPointQuestId(String reviewPointQuestId) {
		this.reviewPointQuestId = reviewPointQuestId;
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
	 * @return the orderNo
	 */
	public int getOrderNo() {
		return orderNo;
	}

	/**
	 * @param orderNo the orderNo to set
	 */
	public void setOrderNo(int orderNo) {
		this.orderNo = orderNo;
	}

	/**
	 * @return the reviewPointQuestDetails
	 */
	public List<DBReviewPointQuestDetailDO> getReviewPointQuestDetails() {
		return reviewPointQuestDetails;
	}

	/**
	 * @param reviewPointQuestDetails the reviewPointQuestDetails to set
	 */
	public void setReviewPointQuestDetails(
			List<DBReviewPointQuestDetailDO> reviewPointQuestDetails) {
		this.reviewPointQuestDetails = reviewPointQuestDetails;
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
	 * @return the reviewPoint
	 */
	public DBReviewPointDO getReviewPoint() {
		return reviewPoint;
	}

	/**
	 * @param reviewPoint the reviewPoint to set
	 */
	public void setReviewPoint(DBReviewPointDO reviewPoint) {
		this.reviewPoint = reviewPoint;
	}

	
}
