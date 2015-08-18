package com.kickmogu.yodobashi.community.resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.annotation.BackendWebServiceClientAware;
import com.yodobashi.esa.customer.createoutcustomerid.CreateOutCustomerIDReq;
import com.yodobashi.esa.customer.refoutcustomeridstatus.RefOutCustomerIDStatusReq;
import com.yodobashi.esa.customer.updateoutcustomeridstatus.UpdateOutCustomerIDStatusReq;

@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration({"/testWebClientContext.xml"})
@BackendWebServiceClientAware
public class BackendWebServiceClientTest {

	@Autowired
	private WebServiceClients clients;
	
	@Test
	public void test() {
		clients.createOutCustomerID.createOutCustomerID(new CreateOutCustomerIDReq());
		clients.refOutCustomerIDStatus.refOutCustomerIDStatus(new RefOutCustomerIDStatusReq());
		clients.updateOutCustomerIDStatus.updateOutCustomerIDStatus(new UpdateOutCustomerIDStatusReq());
	}
	
}
