/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import java.util.List;

import com.yodobashi.esa.customer.getcustomerid.GetCustomerIDResponse;


/**
 * 外部顧客変換情報 DAO です。
 * @author takahashi
 *
 */
public interface CustomerDao {

	/**
	 * 外部顧客IDを元に得意先コードを取得する。
	 * @param outerCustomerId 外部顧客ID
	 * @return 外部顧客情報リスト
	 */
	public GetCustomerIDResponse getCustomer(List<String> outerCustomerIds);

}
