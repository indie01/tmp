/**
 *
 */
package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseIndex;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.yodobashi.community.resource.domain.AbstractReviewDO.PointGrantRequestDetail;
import com.kickmogu.yodobashi.community.resource.domain.constants.ProductSatisfaction;

/**
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.SMALL,excludeBackup=true)
public class OldReviewDO extends BaseWithTimestampDO {

	/**
	 *
	 */
	private static final long serialVersionUID = 8387455089007748661L;

	/**
	 * 旧レビューIDです。
	 */
	@HBaseKey
	private String oldReviewId;

	/**
	 * 外部顧客IDです。
	 */
	@HBaseColumn
	@HBaseIndex
	private String outerCustomerId;

	/**
	 * SKU です。
	 */
	@HBaseColumn
	private String sku;

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
	 * ポイント付与申請リストです。
	 */
	@HBaseColumn
	private List<PointGrantRequestDetail> pointGrantRequestDetails;

	/**
	 * ポイント承認日時です。
	 */
	@HBaseColumn
	private Date pointGrantApprovalDate;

	/**
	 * ポイント付与日です。
	 */
	@HBaseColumn
	private Date pointGrantDate;

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

	/**
	 * @return pointGrantRequestDetails
	 */
	public List<PointGrantRequestDetail> getPointGrantRequestDetails() {
		return pointGrantRequestDetails;
	}

	/**
	 * @param pointGrantRequestDetails セットする pointGrantRequestDetails
	 */
	public void setPointGrantRequestDetails(
			List<PointGrantRequestDetail> pointGrantRequestDetails) {
		this.pointGrantRequestDetails = pointGrantRequestDetails;
	}

	/**
	 * @return pointGrantApprovalDate
	 */
	public Date getPointGrantApprovalDate() {
		return pointGrantApprovalDate;
	}

	/**
	 * @param pointGrantApprovalDate セットする pointGrantApprovalDate
	 */
	public void setPointGrantApprovalDate(Date pointGrantApprovalDate) {
		this.pointGrantApprovalDate = pointGrantApprovalDate;
	}

	/**
	 * @return pointGrantDate
	 */
	public Date getPointGrantDate() {
		return pointGrantDate;
	}

	/**
	 * @param pointGrantDate セットする pointGrantDate
	 */
	public void setPointGrantDate(Date pointGrantDate) {
		this.pointGrantDate = pointGrantDate;
	}

}
