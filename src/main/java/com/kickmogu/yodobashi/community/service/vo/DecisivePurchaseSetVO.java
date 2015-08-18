/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import com.kickmogu.yodobashi.community.resource.domain.DecisivePurchaseDO;

/**
 * 購入の決め手関連の表示情報を集めたビューオブジェクトです。
 * @author kamiike
 *
 */
public class DecisivePurchaseSetVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = -297253914840329073L;

	/**
	 * 購入の決め手です。
	 */
	private DecisivePurchaseDO decisivePurchase;
	/**
	 * 他に選択しているコミュニティユーザーの数です。
	 */
	private long otherSelectedUserCount;

	/**
	 * @return decisivePurchase
	 */
	public DecisivePurchaseDO getDecisivePurchase() {
		return decisivePurchase;
	}

	/**
	 * @param decisivePurchase セットする decisivePurchase
	 */
	public void setDecisivePurchase(DecisivePurchaseDO decisivePurchase) {
		this.decisivePurchase = decisivePurchase;
	}

	public long getOtherSelectedUserCount() {
		return otherSelectedUserCount;
	}

	public void setOtherSelectedUserCount(long otherSelectedUserCount) {
		this.otherSelectedUserCount = otherSelectedUserCount;
	}

	/**
	 * @return ratings
	 */
	public long getRatings() {
		return decisivePurchase.getRatings();
	}

}
