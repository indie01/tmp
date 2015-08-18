package com.kickmogu.yodobashi.community.service;

import java.util.List;

import com.kickmogu.yodobashi.community.resource.domain.RecoverResultDO;

public interface RecoverService {

	/**
	 * レビュー情報にポイント付与情報をリカバリーする。
	 * ケース1：他社購入からヨドバシ購入になった場合、レビューにポイント付与情報をリカバリーする。
	 * @param reviewId リカバリーするレビューID
	 * @return リカバリー結果
	 */
	public RecoverResultDO recoverReviewPoint(String reviewId, boolean execMode);
	
	/**
	 * レビュー情報にポイント付与情報をリカバリーする。
	 * ケース1：他社購入からヨドバシ購入になった場合、レビューにポイント付与情報をリカバリーする。
	 * @param reviewIds リカバリーするレビューID一覧
	 * @return リカバリー結果一覧
	 */
	public List<RecoverResultDO> recoverReviewPoint(List<String> reviewIds, boolean execMode);
}
