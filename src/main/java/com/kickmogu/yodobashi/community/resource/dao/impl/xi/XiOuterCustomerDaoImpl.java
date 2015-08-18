/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl.xi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.cxf.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.kickmogu.yodobashi.community.common.exception.DataNotFoundException;
import com.kickmogu.yodobashi.community.common.utils.XiUtil;
import com.kickmogu.yodobashi.community.resource.annotation.BackendWebServiceClient;
import com.kickmogu.yodobashi.community.resource.annotation.BackendWebServiceClientAware;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.dao.OuterCustomerDao;
import com.kickmogu.yodobashi.community.resource.domain.AccountSharingDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.AccountType;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;
import com.yodobashi.esa.customer.createoutcustomerid.CreateOutCustomerID;
import com.yodobashi.esa.customer.createoutcustomerid.CreateOutCustomerIDReq;
import com.yodobashi.esa.customer.createoutcustomerid.CreateOutCustomerIDResponse;
import com.yodobashi.esa.customer.getoutcustomerid.GetOutCustomerID;
import com.yodobashi.esa.customer.getoutcustomerid.GetOutCustomerIDReq;
import com.yodobashi.esa.customer.getoutcustomerid.GetOutCustomerIDResponse;
import com.yodobashi.esa.customer.getoutcustomeridshareinfo.GetOutCustomerIDShareInfo;
import com.yodobashi.esa.customer.getoutcustomeridshareinfo.GetOutCustomerIDShareInfoReq;
import com.yodobashi.esa.customer.getoutcustomeridshareinfo.GetOutCustomerIDShareInfoResponse;
import com.yodobashi.esa.customer.refoutcustomeridstatus.RefOutCustomerIDStatus;
import com.yodobashi.esa.customer.refoutcustomeridstatus.RefOutCustomerIDStatusReq;
import com.yodobashi.esa.customer.refoutcustomeridstatus.RefOutCustomerIDStatusResponse;
import com.yodobashi.esa.customer.structure.COMMONINPUT;
import com.yodobashi.esa.customer.updateoutcustomeridstatus.UpdateOutCustomerIDStatus;
import com.yodobashi.esa.customer.updateoutcustomeridstatus.UpdateOutCustomerIDStatusReq;
import com.yodobashi.esa.customer.updateoutcustomeridstatus.UpdateOutCustomerIDStatusResponse;


/**
 * 外部顧客情報 DAO です。
 * @author kamiike
 *
 */
@Service @Qualifier("xi") @BackendWebServiceClientAware
public class XiOuterCustomerDaoImpl implements OuterCustomerDao {

	/**
	 * 外部顧客ID登録です。
	 */
	@BackendWebServiceClient(
		endPointUrlPropertyKey="endpoint.createOutCustomerID",
		usernamePropertyKey="xi.auth.user",
		passwordPropertyKey="xi.auth.passwd"
	)
	private CreateOutCustomerID createOutCustomerIDClient;

	/**
	 * 外部顧客IDステータス照会です。
	 */
	@BackendWebServiceClient(
		endPointUrlPropertyKey="endpoint.refOutCustomerIDStatus",
		usernamePropertyKey="xi.auth.user",
		passwordPropertyKey="xi.auth.passwd"
	)
	private RefOutCustomerIDStatus refOutCustomerIDStatusClient;

	/**
	 * 外部顧客IDステータス更新です。
	 */
	@BackendWebServiceClient(
		endPointUrlPropertyKey="endpoint.updateOutCustomerIDStatus",
		usernamePropertyKey="xi.auth.user",
		passwordPropertyKey="xi.auth.passwd"
	)
	private UpdateOutCustomerIDStatus updateOutCustomerIDStatusClient;

	/**
	 * 外部顧客ID共有化情報取得です。
	 */
	@BackendWebServiceClient(
		endPointUrlPropertyKey="endpoint.getOutCustomerIDShareInfo",
		usernamePropertyKey="xi.auth.user",
		passwordPropertyKey="xi.auth.passwd"
	)
	private GetOutCustomerIDShareInfo getOutCustomerIDShareInfoClient;

	/**
	 * 外部顧客ID情報取得です。
	 */
	@BackendWebServiceClient(
		endPointUrlPropertyKey="endpoint.getOutCustomerID",
		usernamePropertyKey="xi.auth.user",
		passwordPropertyKey="xi.auth.passwd"
	)
	private GetOutCustomerID getOutCustomerIDClient;

	/**
	 * リソースコンフィグです。
	 */
	@Autowired
	private ResourceConfig resourceConfig;

	/**
	 * コミュニティID（外部顧客ID）を登録します。
	 * @param universalSessionID ユニバーサルセッション
	 * @return コミュニティID（外部顧客ID）
	 */
	@Override
	public String createCommunityId(String universalSessionID) {
		CreateOutCustomerIDReq request = new CreateOutCustomerIDReq();
		request.setUniversalSessionID(universalSessionID);
		request.setOuterCustomerType(resourceConfig.communityOuterCustomerType);
		request.setCOMMONINPUT(new COMMONINPUT());
		XiUtil.fillCommonInput(CreateOutCustomerID.class,
				request.getCOMMONINPUT());
		CreateOutCustomerIDResponse response = createOutCustomerIDClient.createOutCustomerID(request);

		XiUtil.checkResponse(response.getCOMMONRETURN());
		return response.getOuterCustomerId();
	}

	/**
	 * 指定したコミュニティID（外部顧客ID）のステータスを返します。
	 * @param outerCustomerId 外部顧客ID
	 * @return ステータス
	 */
	@Override
	public CommunityUserStatus loadCommunityUserStatusByOuterCustomerId(String outerCustomerId) {
		RefOutCustomerIDStatusReq request = new RefOutCustomerIDStatusReq();
		request.setCOMMONINPUT(new COMMONINPUT());
		XiUtil.fillCommonInput(RefOutCustomerIDStatus.class,
				request.getCOMMONINPUT());
		RefOutCustomerIDStatusReq.OuterCustomerList customer
				= new RefOutCustomerIDStatusReq.OuterCustomerList();
		customer.setOuterCustomerId(outerCustomerId);
		customer.setOuterCustomerType(resourceConfig.communityOuterCustomerType);
		request.getOuterCustomerList().add(customer);

		RefOutCustomerIDStatusResponse response = refOutCustomerIDStatusClient.refOutCustomerIDStatus(request);

		XiUtil.checkResponse(response.getCOMMONRETURN());

		if (response.getOuterCustomerList().size() == 0) {
			throw new DataNotFoundException("data is null.");
		}
		return CommunityUserStatus.codeOf(
				response.getOuterCustomerList().get(0).getOuterCustomerStatus());
	}

	/**
	 * 指定したコミュニティID（外部顧客ID）のステータスを更新します。
	 * @param communityId コミュニティID（外部顧客ID）
	 * @param status ステータス
	 */
	@Override
	public void updateCustomerIdStatus(String communityId, CommunityUserStatus status) {
		UpdateOutCustomerIDStatusReq request = new UpdateOutCustomerIDStatusReq();
		request.setCOMMONINPUT(new COMMONINPUT());
		XiUtil.fillCommonInput(UpdateOutCustomerIDStatus.class,
				request.getCOMMONINPUT());
		UpdateOutCustomerIDStatusReq.OuterCustomerList customer
				= new UpdateOutCustomerIDStatusReq.OuterCustomerList();
		customer.setOuterCustomerId(communityId);
		customer.setOuterCustomerType(resourceConfig.communityOuterCustomerType);
		customer.setOuterCustomerStatus(status.getCode());
		request.getOuterCustomerList().add(customer);

		UpdateOutCustomerIDStatusResponse response = updateOutCustomerIDStatusClient.updateOutCustomerIDStatus(request);

		XiUtil.checkResponse(response.getCOMMONRETURN());
	}

	/**
	 * 指定した外部顧客IDの共有化情報を取得して返します。
	 * @param outerCustomerId 外部顧客ID
	 * @return 共有化情報リスト
	 */
	@Override
	public List<AccountSharingDO> findAccountSharingByOuterCustomerId(
			String outerCustomerId) {
		GetOutCustomerIDShareInfoReq request = new GetOutCustomerIDShareInfoReq();
		request.setOuterCustomerType(resourceConfig.communityOuterCustomerType);
		request.setCOMMONINPUT(new COMMONINPUT());
		XiUtil.fillCommonInput(GetOutCustomerIDShareInfo.class,
				request.getCOMMONINPUT());
		GetOutCustomerIDShareInfoReq.ShareInfoList shareInfo
				= new GetOutCustomerIDShareInfoReq.ShareInfoList();
		shareInfo.setOuterCustomerId(outerCustomerId);
		request.getShareInfoList().add(shareInfo);
		GetOutCustomerIDShareInfoResponse response = getOutCustomerIDShareInfoClient.getOutCustomerIDShareInfo(request);
		XiUtil.checkResponse(response.getCOMMONRETURN());
		List<AccountSharingDO> result = new ArrayList<AccountSharingDO>();
		for (GetOutCustomerIDShareInfoResponse.ShareInfoList.OuterCustomerIdShareInfoList info : response.getShareInfoList().get(0).getOuterCustomerIdShareInfoList()) {
			if (StringUtils.isEmpty(info.getOuterCustomerStatus().trim())) {
				continue;
			}
			AccountSharingDO accountSharing = new AccountSharingDO();
			accountSharing.setOuterCustomerId(info.getOuterCustomerId());
			if (info.getCustomerType() != null && AccountType.codeOf(
					info.getCustomerType()).equals(AccountType.EC)) {
				accountSharing.setEc(true);
			}
			CommunityUserStatus status = CommunityUserStatus.codeOf(info.getOuterCustomerStatus().trim());
			if (status.equals(CommunityUserStatus.ACTIVE)
					|| status.equals(CommunityUserStatus.STOP)) {
				accountSharing.setActive(true);
			}
			result.add(accountSharing);
		}
		return result;
	}
	
	/**
	 * コミュニティID（外部顧客ID）を登録します。
	 * @param custoNo 得意先コード
	 * @return コミュニティID（外部顧客ID）
	 */
	@Override
	public String getCommunityIdByCustoNo(String custNo) {
		GetOutCustomerIDReq request = new GetOutCustomerIDReq();
		request.setCustNo(custNo);
		request.setOuterCustomerType(resourceConfig.communityOuterCustomerType);
		request.setCOMMONINPUT(new COMMONINPUT());
		XiUtil.fillCommonInput(GetOutCustomerID.class,
				request.getCOMMONINPUT());
		GetOutCustomerIDResponse response = getOutCustomerIDClient.getOutCustomerID(request);
		if( null == response ){
			return null;
		}

		XiUtil.checkResponse(response.getCOMMONRETURN());
		return response.getOuterCustomerList().get(0).getOuterCustomerId();
	}

	@Override
	public Map<String, List<AccountSharingDO>> findAccountSharingByOuterCustomerIds(
			List<String> outerCustomerIds) {

		GetOutCustomerIDShareInfoReq request = new GetOutCustomerIDShareInfoReq();
		request.setOuterCustomerType(resourceConfig.communityOuterCustomerType);
		request.setCOMMONINPUT(new COMMONINPUT());
		XiUtil.fillCommonInput(GetOutCustomerIDShareInfo.class,
				request.getCOMMONINPUT());
		
		for (String outerCustomerId:outerCustomerIds) {
			GetOutCustomerIDShareInfoReq.ShareInfoList shareInfo
			= new GetOutCustomerIDShareInfoReq.ShareInfoList();
			shareInfo.setOuterCustomerId(outerCustomerId);
			request.getShareInfoList().add(shareInfo);
		}
		
		GetOutCustomerIDShareInfoResponse response = getOutCustomerIDShareInfoClient.getOutCustomerIDShareInfo(request);
		XiUtil.checkResponse(response.getCOMMONRETURN());
		
		Map<String, List<AccountSharingDO>> result = Maps.newHashMap();
		
		for (GetOutCustomerIDShareInfoResponse.ShareInfoList shareInfoList: response.getShareInfoList()) {
			List<AccountSharingDO> accountSharings = new ArrayList<AccountSharingDO>();
			for (GetOutCustomerIDShareInfoResponse.ShareInfoList.OuterCustomerIdShareInfoList info : shareInfoList.getOuterCustomerIdShareInfoList()) {
				if (StringUtils.isEmpty(info.getOuterCustomerStatus().trim())) {
					continue;
				}
				AccountSharingDO accountSharing = new AccountSharingDO();
				accountSharing.setOuterCustomerId(info.getOuterCustomerId());
				if (info.getCustomerType() != null && AccountType.codeOf(
						info.getCustomerType()).equals(AccountType.EC)) {
					accountSharing.setEc(true);
				}
				CommunityUserStatus status = CommunityUserStatus.codeOf(info.getOuterCustomerStatus().trim());
				if (status.equals(CommunityUserStatus.ACTIVE)
						|| status.equals(CommunityUserStatus.STOP)) {
					accountSharing.setActive(true);
				}
				accountSharings.add(accountSharing);
			}
			result.put(shareInfoList.getOuterCustomerId(), accountSharings);
		}
		return result;
	}
}
