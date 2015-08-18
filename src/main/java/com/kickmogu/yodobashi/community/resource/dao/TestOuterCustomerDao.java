/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.CreateOrderProcCode;
import com.yodobashi.esa.customer.createordertool.CreateOrderToolResponse;
import com.yodobashi.esa.customer.createoutcustomerid.CreateOutCustomerIDResponse;
import com.yodobashi.esa.customer.getoutcustomeridshareinfo.GetOutCustomerIDShareInfoResponse;
import com.yodobashi.esa.customer.refoutcustomeridstatus.RefOutCustomerIDStatusResponse;
import com.yodobashi.esa.customer.updateoutcustomeridstatus.UpdateOutCustomerIDStatusResponse;


/**
 * 外部顧客情報 DAO です。
 * @author kamiike
 *
 */
public interface TestOuterCustomerDao {

	/**
	 * コミュニティID（外部顧客ID）を登録します。
	 * @param universalSessionID ユニバーサルセッション
	 * @return コミュニティID（外部顧客ID）
	 */
	public CreateOutCustomerIDResponse createCommunityId(String universalSessionID);

	/**
	 * 指定したコミュニティID（外部顧客ID）のステータスを返します。
	 * @param outerCustomerId 外部顧客ID
	 * @return ステータス
	 */
	public RefOutCustomerIDStatusResponse loadCommunityUserStatusByOuterCustomerId(String outerCustomerId);

	/**
	 * 指定したコミュニティID（外部顧客ID）のステータスを更新します。
	 * @param communityId コミュニティID（外部顧客ID）
	 * @param status ステータス
	 */
	public UpdateOutCustomerIDStatusResponse updateCustomerIdStatus(String communityId, CommunityUserStatus status);

	/**
	 * 指定した外部顧客IDの共有化情報を取得して返します。
	 * @param outerCustomerId 外部顧客ID
	 * @return 共有化情報リスト
	 */
	public GetOutCustomerIDShareInfoResponse findAccountSharingByOuterCustomerId(
			String outerCustomerId);

	/**
	 * 注文作成ツールを呼び出します。
	 * @param orderNo 注文番号
	 * @param procCode 処理区分
	 */
	public CreateOrderToolResponse createOrderTool(String orderNo, CreateOrderProcCode procCode);
}
