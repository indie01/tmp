/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

/**
 * 購入の決め手のビューオブジェクトです。
 * @author kamiike
 *
 */
public class DecisivePurchaseVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = -297253914840329073L;

	/**
	 * 購入の決め手IDです。
	 */
	private String decisivePurchaseId;

	/**
	 * 購入の決め手名称です。
	 */
	private String decisivePurchaseName;

	/**
	 * 評価数です。
	 */
	private long ratings;

	/**
	 * @return decisivePurchaseId
	 */
	public String getDecisivePurchaseId() {
		return decisivePurchaseId;
	}

	/**
	 * @param decisivePurchaseId セットする decisivePurchaseId
	 */
	public void setDecisivePurchaseId(String decisivePurchaseId) {
		this.decisivePurchaseId = decisivePurchaseId;
	}

	/**
	 * @return decisivePurchaseName
	 */
	public String getDecisivePurchaseName() {
		return decisivePurchaseName;
	}

	/**
	 * @param decisivePurchaseName セットする decisivePurchaseName
	 */
	public void setDecisivePurchaseName(String decisivePurchaseName) {
		this.decisivePurchaseName = decisivePurchaseName;
	}

	/**
	 * @return ratings
	 */
	public long getRatings() {
		return ratings;
	}

	/**
	 * @param ratings セットする ratings
	 */
	public void setRatings(long ratings) {
		this.ratings = ratings;
	}

}
