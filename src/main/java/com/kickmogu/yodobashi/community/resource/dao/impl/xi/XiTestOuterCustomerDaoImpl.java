/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl.xi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.utils.DumpUtil;
import com.kickmogu.yodobashi.community.common.utils.XiUtil;
import com.kickmogu.yodobashi.community.resource.annotation.BackendWebServiceClient;
import com.kickmogu.yodobashi.community.resource.annotation.BackendWebServiceClientAware;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.dao.TestOuterCustomerDao;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.CreateOrderProcCode;
import com.yodobashi.esa.customer.createordertool.CreateOrderTool;
import com.yodobashi.esa.customer.createordertool.CreateOrderToolReq;
import com.yodobashi.esa.customer.createordertool.CreateOrderToolResponse;
import com.yodobashi.esa.customer.createoutcustomerid.CreateOutCustomerID;
import com.yodobashi.esa.customer.createoutcustomerid.CreateOutCustomerIDReq;
import com.yodobashi.esa.customer.createoutcustomerid.CreateOutCustomerIDResponse;
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
public class XiTestOuterCustomerDaoImpl implements TestOuterCustomerDao {

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
	 * 注文データツールです。
	 */
	@BackendWebServiceClient(
		endPointUrlPropertyKey="endpoint.createOrderTool",
		usernamePropertyKey="xi.auth.user",
		passwordPropertyKey="xi.auth.passwd"
	)
	private CreateOrderTool createOrderToolClient;

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
	public CreateOutCustomerIDResponse createCommunityId(String universalSessionID) {
		CreateOutCustomerIDReq request = new CreateOutCustomerIDReq();
		request.setUniversalSessionID(universalSessionID);
		request.setOuterCustomerType(resourceConfig.communityOuterCustomerType);
		request.setCOMMONINPUT(new COMMONINPUT());

		XiUtil.fillCommonInput(CreateOutCustomerID.class,
				request.getCOMMONINPUT());

		System.out.println(DumpUtil.dumpBean(request));

		CreateOutCustomerIDResponse res =createOutCustomerIDClient.createOutCustomerID(request);

		System.out.println(DumpUtil.dumpBean(res));

		return res;
	}

	/**
	 * 指定したコミュニティID（外部顧客ID）のステータスを返します。
	 * @param outerCustomerId 外部顧客ID
	 * @return ステータス
	 */
	@Override
	public RefOutCustomerIDStatusResponse loadCommunityUserStatusByOuterCustomerId(String outerCustomerId) {
		RefOutCustomerIDStatusReq request = new RefOutCustomerIDStatusReq();
		request.setCOMMONINPUT(new COMMONINPUT());
		XiUtil.fillCommonInput(RefOutCustomerIDStatus.class,
				request.getCOMMONINPUT());
		RefOutCustomerIDStatusReq.OuterCustomerList customer
				= new RefOutCustomerIDStatusReq.OuterCustomerList();
		customer.setOuterCustomerId(outerCustomerId);
		customer.setOuterCustomerType(resourceConfig.communityOuterCustomerType);
		request.getOuterCustomerList().add(customer);


		System.out.println(DumpUtil.dumpBean(request));

		RefOutCustomerIDStatusResponse res =refOutCustomerIDStatusClient.refOutCustomerIDStatus(request);

		System.out.println(DumpUtil.dumpBean(res));


		return res;
	}

	/**
	 * 指定したコミュニティID（外部顧客ID）のステータスを更新します。
	 * @param communityId コミュニティID（外部顧客ID）
	 * @param status ステータス
	 */
	@Override
	public UpdateOutCustomerIDStatusResponse updateCustomerIdStatus(String communityId, CommunityUserStatus status) {
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


		System.out.println(DumpUtil.dumpBean(request));

		UpdateOutCustomerIDStatusResponse res =updateOutCustomerIDStatusClient.updateOutCustomerIDStatus(request);

		System.out.println(DumpUtil.dumpBean(res));


		return res;
	}

	/**
	 * 指定した外部顧客IDの共有化情報を取得して返します。
	 * @param outerCustomerId 外部顧客ID
	 * @return 共有化情報リスト
	 */
	@Override
	public GetOutCustomerIDShareInfoResponse findAccountSharingByOuterCustomerId(
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

		System.out.println(DumpUtil.dumpBean(request));
		GetOutCustomerIDShareInfoResponse res =getOutCustomerIDShareInfoClient.getOutCustomerIDShareInfo(request);
		System.out.println(DumpUtil.dumpBean(res));

		return res;
	}

	/**
	 * 注文作成ツールを呼び出します。
	 * @param orderNo 注文番号
	 * @param procCode 処理区分
	 */
	@Override
	public CreateOrderToolResponse createOrderTool(String orderNo, CreateOrderProcCode procCode) {
		CreateOrderToolReq request = new CreateOrderToolReq();
		request.setOrderno(orderNo);
		request.setPrccode(procCode.getCode());
		request.setCOMMONINPUT(new COMMONINPUT());
		XiUtil.fillCommonInput(CreateOrderTool.class,
				request.getCOMMONINPUT());

		CreateOrderToolResponse response = createOrderToolClient.createOrderTool(request);
		return response;
	}
}
