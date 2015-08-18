/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl.xi;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.utils.DumpUtil;
import com.kickmogu.yodobashi.community.common.utils.XiUtil;
import com.kickmogu.yodobashi.community.resource.annotation.BackendWebServiceClient;
import com.kickmogu.yodobashi.community.resource.annotation.BackendWebServiceClientAware;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.dao.CustomerDao;
import com.yodobashi.esa.customer.getcustomerid.GetCustomerID;
import com.yodobashi.esa.customer.getcustomerid.GetCustomerIDReq;
import com.yodobashi.esa.customer.getcustomerid.GetCustomerIDResponse;
import com.yodobashi.esa.customer.structure.COMMONINPUT;


/**
 * 外部顧客変換情報 DAO です。
 * @author takahashi
 *
 */
@Service @Qualifier("xi") @BackendWebServiceClientAware
public class XiCustomerDaoImpl implements CustomerDao{

	/**
	 * 外部顧客IDステータス照会です。
	 */
	@BackendWebServiceClient(
		endPointUrlPropertyKey="endpoint.getCustomerID",
		usernamePropertyKey="xi.auth.user",
		passwordPropertyKey="xi.auth.passwd"
	)
	private GetCustomerID customerIDClient;


	/**
	 * リソースコンフィグです。
	 */
	@Autowired
	private ResourceConfig resourceConfig;

	@Override
	public GetCustomerIDResponse getCustomer(List<String> outerCustomerIds) {
		GetCustomerIDReq request = new GetCustomerIDReq(); 
		
		for(String outerCustomerId: outerCustomerIds){
			GetCustomerIDReq.CustomerList customerList = new GetCustomerIDReq.CustomerList();
			customerList.setOuterCustomerId(outerCustomerId);
			customerList.setOuterCustomerType(resourceConfig.communityOuterCustomerType);
			request.getCustomerList().add(customerList);	
		}
		request.setCOMMONINPUT(new COMMONINPUT());
		XiUtil.fillCommonInput(GetCustomerID.class, request.getCOMMONINPUT());
		
		System.out.println(">>>>>>" + DumpUtil.dumpBeanToMultiLine(request));
		GetCustomerIDResponse response = customerIDClient.getCustomerID(request);
		XiUtil.checkResponse(response.getCOMMONReturn());

		return response;
	}
}
