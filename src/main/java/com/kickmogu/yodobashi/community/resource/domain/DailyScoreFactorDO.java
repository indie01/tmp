/**
 *
 */
package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;

import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.yodobashi.community.resource.domain.constants.DailyScoreFactorType;

/**
 * 日次のスコア要因情報です。
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.HUGE,
	excludeBackup=true)
public class DailyScoreFactorDO extends BaseWithTimestampDO {

	/**
	 *
	 */
	private static final long serialVersionUID = 2716416301113128098L;

	/**
	 * 日次のスコア要因IDです。
	 */
	@HBaseKey(createTableSplitKeys={"1","5","9","D","H","L","P","T","X","b","f","j","n","r","v"})
	private String dailyScoreFactorId;

	/**
	 * コンテンツタイプです。
	 */
	@HBaseColumn
	private DailyScoreFactorType type;

	/**
	 * 対象日付です。
	 */
	@HBaseColumn
	private Date targetDate;

	/**
	 * コンテンツIDです。
	 */
	@HBaseColumn
	private String contentsId;

	/**
	 * SKUです。
	 */
	@HBaseColumn
	private String sku;

	/**
	 * 経過日数です。
	 */
	@HBaseColumn
	private Integer elapsedDays;

	/**
	 * コメント数です。
	 */
	@HBaseColumn
	private Long commentCount;

	/**
	 * いいね数です。
	 */
	@HBaseColumn
	private Long likeCount;

	/**
	 * 閲覧数です。
	 */
	@HBaseColumn
	private Long viewCount;

	/**
	 * フォロワー数です。
	 */
	@HBaseColumn
	private Long followerCount;

	/**
	 * 回答数です。
	 */
	@HBaseColumn
	private Long answerCount;
	
	/**
	 * 投稿日です。
	 */
	@HBaseColumn
	private Date postDate;

	
	/**
	 * レビュー文字数です。
	 */
	@HBaseColumn
	private Long contentBodyCount;

	/**
	 * 画像数です。
	 */
	@HBaseColumn
	private Long contentImageCount;
	
	/**
	 * @return type
	 */
	public DailyScoreFactorType getType() {
		return type;
	}

	/**
	 * @param type セットする type
	 */
	public void setType(DailyScoreFactorType type) {
		this.type = type;
	}

	/**
	 * @return targetDate
	 */
	public Date getTargetDate() {
		return targetDate;
	}

	/**
	 * @param targetDate セットする targetDate
	 */
	public void setTargetDate(Date targetDate) {
		this.targetDate = targetDate;
	}

	/**
	 * @return contentsId
	 */
	public String getContentsId() {
		return contentsId;
	}

	/**
	 * @param contentsId セットする contentsId
	 */
	public void setContentsId(String contentsId) {
		this.contentsId = contentsId;
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
	 * @return elapsedDays
	 */
	public Integer getElapsedDays() {
		return elapsedDays;
	}

	/**
	 * @param elapsedDays セットする elapsedDays
	 */
	public void setElapsedDays(Integer elapsedDays) {
		this.elapsedDays = elapsedDays;
	}

	/**
	 * @return commentCount
	 */
	public Long getCommentCount() {
		return commentCount;
	}

	/**
	 * @param commentCount セットする commentCount
	 */
	public void setCommentCount(Long commentCount) {
		this.commentCount = commentCount;
	}

	/**
	 * @return likeCount
	 */
	public Long getLikeCount() {
		return likeCount;
	}

	/**
	 * @param likeCount セットする likeCount
	 */
	public void setLikeCount(Long likeCount) {
		this.likeCount = likeCount;
	}

	/**
	 * @return viewCount
	 */
	public Long getViewCount() {
		return viewCount;
	}

	/**
	 * @param viewCount セットする viewCount
	 */
	public void setViewCount(Long viewCount) {
		this.viewCount = viewCount;
	}

	/**
	 * @return followerCount
	 */
	public Long getFollowerCount() {
		return followerCount;
	}

	/**
	 * @param followerCount セットする followerCount
	 */
	public void setFollowerCount(Long followerCount) {
		this.followerCount = followerCount;
	}

	/**
	 * @return answerCount
	 */
	public Long getAnswerCount() {
		return answerCount;
	}

	/**
	 * @param answerCount セットする answerCount
	 */
	public void setAnswerCount(Long answerCount) {
		this.answerCount = answerCount;
	}

	/**
	 * @return dailyScoreFactorId
	 */
	public String getDailyScoreFactorId() {
		return dailyScoreFactorId;
	}

	/**
	 * @param dailyScoreFactorId セットする dailyScoreFactorId
	 */
	public void setDailyScoreFactorId(String dailyScoreFactorId) {
		this.dailyScoreFactorId = dailyScoreFactorId;
	}

	/**
	 * @return the postDate
	 */
	public Date getPostDate() {
		return postDate;
	}

	/**
	 * @param postDate the postDate to set
	 */
	public void setPostDate(Date postDate) {
		this.postDate = postDate;
	}

	/**
	 * @return the contentBodyCount
	 */
	public Long getContentBodyCount() {
		return contentBodyCount;
	}

	/**
	 * @param contentBodyCount the contentBodyCount to set
	 */
	public void setContentBodyCount(Long contentBodyCount) {
		this.contentBodyCount = contentBodyCount;
	}

	/**
	 * @return the contentImageCount
	 */
	public Long getContentImageCount() {
		return contentImageCount;
	}

	/**
	 * @param contentImageCount the contentImageCount to set
	 */
	public void setContentImageCount(Long contentImageCount) {
		this.contentImageCount = contentImageCount;
	}


}
