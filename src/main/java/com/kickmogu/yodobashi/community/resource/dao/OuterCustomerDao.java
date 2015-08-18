/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import java.util.List;
import java.util.Map;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.AccountSharingDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;


/**
 * 外部顧客情報 DAO です。
 * @author kamiike
 *
 */
public interface OuterCustomerDao {

	/**
	 * コミュニティID（外部顧客ID）を登録します。
	 * @param universalSessionID ユニバーサルセッション
	 * @return コミュニティID（外部顧客ID）
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="XI通信なのでテスト対象外")
	public String createCommunityId(String universalSessionID);

	/**
	 * 指定したコミュニティID（外部顧客ID）のステータスを返します。
	 * @param outerCustomerId 外部顧客ID
	 * @return ステータス
	 */
	public CommunityUserStatus loadCommunityUserStatusByOuterCustomerId(String outerCustomerId);

	/**
	 * 指定したコミュニティID（外部顧客ID）のステータスを更新します。
	 * @param communityId コミュニティID（外部顧客ID）
	 * @param status ステータス
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="XI通信なのでテスト対象外")
	public void updateCustomerIdStatus(String communityId, CommunityUserStatus status);

	/**
	 * 指定した外部顧客IDの共有化情報を取得して返します。
	 * @param outerCustomerId 外部顧客ID
	 * @return 共有化情報リスト
	 */
	public List<AccountSharingDO> findAccountSharingByOuterCustomerId(
			String outerCustomerId);
	
	
	public Map<String,List<AccountSharingDO>> findAccountSharingByOuterCustomerIds(
			List<String> outerCustomerIds);
	
	public String getCommunityIdByCustoNo(String custNo);
	
	
}
