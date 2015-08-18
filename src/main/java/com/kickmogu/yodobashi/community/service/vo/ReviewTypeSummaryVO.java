/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewType;


/**
 * レビュータイプサマリービューオブジェクトです。
 * @author yanai
 *
 */
public class ReviewTypeSummaryVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = 1312106242666851466L;

	/**
	 * レビュータイプです。
	 */
	private ReviewType reviewType;

	/**
	 * 区間別レビュー件数です。
	 */
	private Long reviewCount;

	/**
	 * @return reviewCount
	 */
	public Long getReviewCount() {
		return reviewCount;
	}

	/**
	 * @param reviewCount セットする reviewCount
	 */
	public void setReviewCount(Long reviewCount) {
		this.reviewCount = reviewCount;
	}

	/**
	 * @return reviewType
	 */
	public ReviewType getReviewType() {
		return reviewType;
	}

	/**
	 * @param reviewType セットする reviewType
	 */
	public void setReviewType(ReviewType reviewType) {
		this.reviewType = reviewType;
	}

}
