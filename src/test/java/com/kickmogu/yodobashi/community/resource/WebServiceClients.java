package com.kickmogu.yodobashi.community.resource;

import com.kickmogu.yodobashi.community.resource.annotation.BackendWebServiceClient;
import com.kickmogu.yodobashi.community.resource.annotation.BackendWebServiceClientAware;
import com.yodobashi.esa.customer.createoutcustomerid.CreateOutCustomerID;
import com.yodobashi.esa.customer.refoutcustomeridstatus.RefOutCustomerIDStatus;
import com.yodobashi.esa.customer.updateoutcustomeridstatus.UpdateOutCustomerIDStatus;

@BackendWebServiceClientAware
public class WebServiceClients {


	@BackendWebServiceClient(endPointUrlPropertyKey="endpoint.createOutCustomerID")
	CreateOutCustomerID createOutCustomerID;
	
		
	@BackendWebServiceClient(endPointUrlPropertyKey="endpoint.refOutCustomerIDStatus")
	RefOutCustomerIDStatus refOutCustomerIDStatus;
	
	@BackendWebServiceClient(endPointUrlPropertyKey="endpoint.updateOutCustomerIDStatus")
	UpdateOutCustomerIDStatus updateOutCustomerIDStatus;
}
