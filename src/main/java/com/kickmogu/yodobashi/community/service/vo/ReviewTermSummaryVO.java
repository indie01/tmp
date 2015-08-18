/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;


/**
 * レビューの月別サマリービューオブジェクトです。
 * @author kamiike
 *
 */
public class ReviewTermSummaryVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = -4184716012564468406L;

	/**
	 * レビュー区間です。
	 */
	private Integer reviewTerm;

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
	 * @return reviewTerm
	 */
	public Integer getReviewTerm() {
		return reviewTerm;
	}

	/**
	 * @param reviewTerm セットする reviewTerm
	 */
	public void setReviewTerm(Integer reviewTerm) {
		this.reviewTerm = reviewTerm;
	}

}
