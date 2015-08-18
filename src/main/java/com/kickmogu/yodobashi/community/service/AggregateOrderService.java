package com.kickmogu.yodobashi.community.service;

import java.util.List;

public interface AggregateOrderService {

	/**
	 * 更新通知から呼び出される注文情報の集約処理です。
	 * @param outerCustomerIds 外部顧客IDのリスト
	 */
	public void aggregateOrder(List<String> outerCustomerIds);
	
	/**
	 * 購入商品一覧を再作成する処理です。
	 * @param outerCustomerId 外部顧客ID（メインとなるID）
	 */
	public void reCreateAggregateOrder(String outerCustomerId);

}
