/**
 *
 */
package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.id.annotation.IDParts;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseIndex;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.yodobashi.community.resource.domain.constants.ProductSatisfaction;

/**
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	}
,sizeGroup=SizeGroup.SMALL,excludeBackup=true)
public class OldReviewHistoryDO extends BaseWithTimestampDO {

	/**
	 *
	 */
	private static final long serialVersionUID = -4469626882846809084L;

	/**
	 *レビュー履歴ID（旧レビューID + 連番）です。
	 */
	@HBaseKey (idGenerator="idPartsGenerator",createTableSplitKeys={"#", "5", "A", "G", "M", "S", "Y", "e", "k", "q", "w"})
	private String oldReviewHistoryId;

	/**
	 * 旧レビューIDです。
	 */
	@HBaseColumn @IDParts(order=1)
	private String oldReviewId;

	/**
	 * 連番です。
	 */
	@HBaseColumn @IDParts(order=2)
	private int childNo;

	/**
	 * 外部顧客IDです。
	 */
	@HBaseColumn
	@HBaseIndex
	private String outerCustomerId;

	/**
	 * 満足度評価数です。
	 */
	@HBaseColumn
	private ProductSatisfaction productSatisfaction;

	/**
	 * レビュー本文です。
	 */
	@HBaseColumn
	private String reviewBodyParts;

	/**
	 * 保存日時
	 */
	@HBaseColumn
	private Date saveDate;

	/**
	 * 投稿日です。
	 */
	@HBaseColumn
	private Date postDate;

	/**
	 * メモです。
	 */
	@HBaseColumn
	private String memo;

	/**
	 * 画像パスのリストです。
	 */
	@HBaseColumn
	private List<String> imagePaths = Lists.newArrayList();

	/**
	 * @return oldReviewHistoryId
	 */
	public String getOldReviewHistoryId() {
		return oldReviewHistoryId;
	}

	/**
	 * @param oldReviewHistoryId セットする oldReviewHistoryId
	 */
	public void setOldReviewHistoryId(String oldReviewHistoryId) {
		this.oldReviewHistoryId = oldReviewHistoryId;
	}

	/**
	 * @return oldReviewId
	 */
	public String getOldReviewId() {
		return oldReviewId;
	}

	/**
	 * @param oldReviewId セットする oldReviewId
	 */
	public void setOldReviewId(String oldReviewId) {
		this.oldReviewId = oldReviewId;
	}

	/**
	 * @return childNo
	 */
	public int getChildNo() {
		return childNo;
	}

	/**
	 * @param childNo セットする childNo
	 */
	public void setChildNo(int childNo) {
		this.childNo = childNo;
	}

	/**
	 * @return productSatisfaction
	 */
	public ProductSatisfaction getProductSatisfaction() {
		return productSatisfaction;
	}

	/**
	 * @param productSatisfaction セットする productSatisfaction
	 */
	public void setProductSatisfaction(ProductSatisfaction productSatisfaction) {
		this.productSatisfaction = productSatisfaction;
	}

	/**
	 * @return saveDate
	 */
	public Date getSaveDate() {
		return saveDate;
	}

	/**
	 * @param saveDate セットする saveDate
	 */
	public void setSaveDate(Date saveDate) {
		this.saveDate = saveDate;
	}

	/**
	 * @return postDate
	 */
	public Date getPostDate() {
		return postDate;
	}

	/**
	 * @param postDate セットする postDate
	 */
	public void setPostDate(Date postDate) {
		this.postDate = postDate;
	}

	/**
	 * @return memo
	 */
	public String getMemo() {
		return memo;
	}

	/**
	 * @param memo セットする memo
	 */
	public void setMemo(String memo) {
		this.memo = memo;
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
	 * @return reviewBodyParts
	 */
	public String getReviewBodyParts() {
		return reviewBodyParts;
	}

	/**
	 * @param reviewBodyParts セットする reviewBodyParts
	 */
	public void setReviewBodyParts(String reviewBodyParts) {
		this.reviewBodyParts = reviewBodyParts;
	}

	/**
	 * @return imagePaths
	 */
	public List<String> getImagePaths() {
		return imagePaths;
	}

	/**
	 * @param imagePaths セットする imagePaths
	 */
	public void setImagePaths(List<String> imagePaths) {
		this.imagePaths = imagePaths;
	}

}
