package com.kickmogu.yodobashi.community.resource.config;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.security.AccessDeniedException;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WebServiceAccessFilterIntercepter extends AbstractPhaseInterceptor<Message> {
	
	private static final Logger LOG = LoggerFactory.getLogger(WebServiceClientInitializer.class);
	
	@Autowired
	private AppConfigurationDao appConfigurationDao;

	public WebServiceAccessFilterIntercepter() {
		super(Phase.PRE_INVOKE);
	}

	@Override
	public void handleMessage(Message message) throws Fault {

		if (message.getExchange().getEndpoint().getEndpointInfo().getAddress().contains("yctcod01")) {
			return;
		}
		
		String endPointName = message.getExchange().getEndpoint().getEndpointInfo().getInterface().getName().getLocalPart();
		
		Boolean allow = appConfigurationDao.getAsBooleanWithCache("webServiceAccessFilter." + endPointName + ".allow");
		if (allow == null || !allow) {
			throw new AccessDeniedException(endPointName + " IS DENY");			
		}
		LOG.info(endPointName + " IS ALLOW");
	}

}
