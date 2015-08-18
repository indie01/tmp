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
 * カタログDB情報
 */
/**
 * @author takahashi
 *
 */
@SolrSchema
public class DBReviewPointDO extends BaseWithTimestampDO{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4756810020888139247L;

	@SolrField @SolrUniqKey
	private String reviewPointId;
	
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
	 * レビューポイント可否
	 */
	@SolrField
	private int reviewPointWhetherFlag;

	/**
	 * レビューコメント
	 */
	@SolrField
	private String reviewComment;

	/**
	 * ポイント算定タイプ
	 */
	@SolrField
	private int pointCalcType;

	/**
	 * 初期投稿期間
	 */
	@SolrField
	private Integer initialPostTerm;

	/**
	 * 継続投稿回数
	 */
	@SolrField
	private Integer continuationPostTimes;

	/**
	 * 継続投稿期間
	 */
	@SolrField
	private Integer continuationPostTerm;

	/**
	 * 最終更新日
	 */
	@SolrField
	private Date lastUpdate;

	@RelatedBySolr
	private @HasMany List<DBReviewPointQuestDO> reviewPointQuests = Lists.newArrayList();
	
	/**
	 * @return the reviewPointId
	 */
	public String getReviewPointId() {
		return reviewPointId;
	}

	/**
	 * @param reviewPointId the reviewPointId to set
	 */
	public void setReviewPointId(String reviewPointId) {
		this.reviewPointId = reviewPointId;
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
	 * @return the reviewPointWhetherFlag
	 */
	public int getReviewPointWhetherFlag() {
		return reviewPointWhetherFlag;
	}

	/**
	 * @param reviewPointWhetherFlag the reviewPointWhetherFlag to set
	 */
	public void setReviewPointWhetherFlag(int reviewPointWhetherFlag) {
		this.reviewPointWhetherFlag = reviewPointWhetherFlag;
	}

	/**
	 * @return the reviewComment
	 */
	public String getReviewComment() {
		return reviewComment;
	}

	/**
	 * @param reviewComment the reviewComment to set
	 */
	public void setReviewComment(String reviewComment) {
		this.reviewComment = reviewComment;
	}

	/**
	 * @return the pointCalcType
	 */
	public int getPointCalcType() {
		return pointCalcType;
	}

	/**
	 * @param pointCalcType the pointCalcType to set
	 */
	public void setPointCalcType(int pointCalcType) {
		this.pointCalcType = pointCalcType;
	}

	/**
	 * @return the initialPostTerm
	 */
	public Integer getInitialPostTerm() {
		return initialPostTerm;
	}

	/**
	 * @param initialPostTerm the initialPostTerm to set
	 */
	public void setInitialPostTerm(Integer initialPostTerm) {
		this.initialPostTerm = initialPostTerm;
	}

	/**
	 * @return the continuationPostTimes
	 */
	public Integer getContinuationPostTimes() {
		return continuationPostTimes;
	}

	/**
	 * @param continuationPostTimes the continuationPostTimes to set
	 */
	public void setContinuationPostTimes(Integer continuationPostTimes) {
		this.continuationPostTimes = continuationPostTimes;
	}

	/**
	 * @return the continuationPostTerm
	 */
	public Integer getContinuationPostTerm() {
		return continuationPostTerm;
	}

	/**
	 * @param continuationPostTerm the continuationPostTerm to set
	 */
	public void setContinuationPostTerm(Integer continuationPostTerm) {
		this.continuationPostTerm = continuationPostTerm;
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
	 * @return the reviewPointQuests
	 */
	public List<DBReviewPointQuestDO> getReviewPointQuests() {
		return reviewPointQuests;
	}

	/**
	 * @param reviewPointQuests the reviewPointQuests to set
	 */
	public void setReviewPointQuests(List<DBReviewPointQuestDO> reviewPointQuests) {
		this.reviewPointQuests = reviewPointQuests;
	}
}
