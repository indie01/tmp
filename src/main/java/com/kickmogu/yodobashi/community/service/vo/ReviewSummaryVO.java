/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import java.util.List;

/**
 * レビューサマリーのビューオブジェクトです。
 * @author kamiike
 *
 */
public class ReviewSummaryVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = 6539500250692525496L;

	/**
	 * レビュー総数
	 */
	private Long reviewTotalCount;

	/**
	 * 区間サマリー情報
	 */
	private List<ReviewTermSummaryVO> reviewTermSummaries;
	
	/**
	 * レビュータイプサマリー情報
	 */
	private List<ReviewTypeSummaryVO> reviewTypeSummaries;

	/**
	 * @return reviewTotalCount
	 */
	public Long getReviewTotalCount() {
		return reviewTotalCount;
	}

	/**
	 * @param reviewTotalCount セットする reviewTotalCount
	 */
	public void setReviewTotalCount(Long reviewTotalCount) {
		this.reviewTotalCount = reviewTotalCount;
	}

	/**
	 * @return reviewTermSummaries
	 */
	public List<ReviewTermSummaryVO> getReviewTermSummaries() {
		return reviewTermSummaries;
	}

	/**
	 * @param reviewTermSummaries セットする reviewTermSummaries
	 */
	public void setReviewTermSummaries(List<ReviewTermSummaryVO> reviewTermSummaries) {
		this.reviewTermSummaries = reviewTermSummaries;
	}

	/**
	 * @return reviewTypeSummaries
	 */
	public List<ReviewTypeSummaryVO> getReviewTypeSummaries() {
		return reviewTypeSummaries;
	}

	/**
	 * @param reviewTypeSummaries セットする reviewTypeSummaries
	 */
	public void setReviewTypeSummaries(List<ReviewTypeSummaryVO> reviewTypeSummaries) {
		this.reviewTypeSummaries = reviewTypeSummaries;
	}

}
