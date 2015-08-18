/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import com.kickmogu.yodobashi.community.resource.domain.PointGrantEntryDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointGrantResultStatus;

/**
 * コミュニティ管理のポイント付与申請ビューオブジェクトです。
 * @author kamiike
 *
 */
public class PointGrantEntrySetVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = 1706865868838945154L;
	
	/**
	 * ポイント付与申請ID（ユニークID（東阪で））
	 */
	private String pointGrantRequestId;	
	/**
	 * レビューID
	 */
	private String reviewId;
	
	private PointGrantEntryDO pointGrantEntry;
	
	private ReviewDO review;

	/** 外部顧客ID */
	private String outerCustomerId;

	/** 得意先コード */
	private String custNo;
	
	/**
	 * ポイント付与/剥奪実行結果
	 */
	private PointGrantResultStatus pointGrantRequestExecMainType;
	
	public String getPointGrantRequestId() {
		return pointGrantRequestId;
	}

	public void setPointGrantRequestId(String pointGrantRequestId) {
		this.pointGrantRequestId = pointGrantRequestId;
	}

	public String getReviewId() {
		return reviewId;
	}

	public void setReviewId(String reviewId) {
		this.reviewId = reviewId;
	}

	public PointGrantEntryDO getPointGrantEntry() {
		return pointGrantEntry;
	}

	public void setPointGrantEntry(PointGrantEntryDO pointGrantEntry) {
		this.pointGrantEntry = pointGrantEntry;
	}

	public ReviewDO getReview() {
		return review;
	}

	public void setReview(ReviewDO review) {
		this.review = review;
	}

	public PointGrantResultStatus getPointGrantRequestExecMainType() {
		return pointGrantRequestExecMainType;
	}

	public void setPointGrantRequestExecMainType(
			PointGrantResultStatus pointGrantRequestExecMainType) {
		this.pointGrantRequestExecMainType = pointGrantRequestExecMainType;
	}

	public String getOuterCustomerId() {
		return outerCustomerId;
	}

	public void setOuterCustomerId(String outerCustomerId) {
		this.outerCustomerId = outerCustomerId;
	}

	public String getCustNo() {
		return custNo;
	}

	public void setCustNo(String custNo) {
		this.custNo = custNo;
	}

}
