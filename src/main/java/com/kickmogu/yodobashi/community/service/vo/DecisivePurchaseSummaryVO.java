/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import java.util.List;

/**
 * 購入の決め手のサマリ情報です。
 * @author kamiike
 *
 */
public class DecisivePurchaseSummaryVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = 1750692578037445929L;

	/**
	 * 購入の決め手全候補数です。
	 */
	private long candidateCount;

	/**
	 * 購入の決め手全評価数です。
	 */
	private long totalDecisivePurchaseCount;

	/**
	 * 購入の決め手リストです。
	 */
	private List<DecisivePurchaseVO> decisivePurchases;

	/**
	 * @return candidateCount
	 */
	public long getCandidateCount() {
		return candidateCount;
	}

	/**
	 * @param candidateCount セットする candidateCount
	 */
	public void setCandidateCount(long candidateCount) {
		this.candidateCount = candidateCount;
	}

	/**
	 * @return totalDecisivePurchaseCount
	 */
	public long getTotalDecisivePurchaseCount() {
		return totalDecisivePurchaseCount;
	}

	/**
	 * @param totalDecisivePurchaseCount セットする totalDecisivePurchaseCount
	 */
	public void setTotalDecisivePurchaseCount(long totalDecisivePurchaseCount) {
		this.totalDecisivePurchaseCount = totalDecisivePurchaseCount;
	}

	/**
	 * @return decisivePurchases
	 */
	public List<DecisivePurchaseVO> getDecisivePurchases() {
		return decisivePurchases;
	}

	/**
	 * @param decisivePurchases セットする decisivePurchases
	 */
	public void setDecisivePurchases(List<DecisivePurchaseVO> decisivePurchases) {
		this.decisivePurchases = decisivePurchases;
	}

}
