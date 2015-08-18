package com.kickmogu.yodobashi.community.resource.config;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.utils.AnnotationUtil;
import com.kickmogu.lib.core.utils.AopUtil;
import com.kickmogu.lib.core.utils.Asserts;
import com.kickmogu.yodobashi.community.resource.annotation.BackendWebServiceClient;
import com.kickmogu.yodobashi.community.resource.annotation.BackendWebServiceClientAware;

@Service
public class WebServiceClientInitializer implements InitializingBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(WebServiceClientInitializer.class);

	@Autowired
	private ApplicationContext context;

	@Autowired
	private WebServiceClientConfig config;
	
	@Autowired
	private WebServiceAccessFilterIntercepter webServiceAccessFilterIntercepter;
	
	private List<Class<?>> webServiceInterfaces = Lists.newArrayList();

	public List<Class<?>> getWebServiceInterfaces() {
		return webServiceInterfaces;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
		for (String beanName:context.getBeanDefinitionNames()) {

			if (!context.getType(beanName).getName().contains("$Proxy")) {
				if (context.getType(beanName).getAnnotation(BackendWebServiceClientAware.class) == null) continue;
			}

			Object bean = null;
			try {
				bean = AopUtil.getTargetObject(context.getBean(beanName));
			} catch (Throwable th) {
				continue;
			}
			if (bean == null) continue;
			if(bean.getClass().getAnnotation(BackendWebServiceClientAware.class) == null){
				continue;
			}


			for (Field field:AnnotationUtil.getAnnotatedFields(BackendWebServiceClient.class, bean.getClass())) {
				BackendWebServiceClient client = field.getAnnotation(BackendWebServiceClient.class);
				String endPointUrl = config.getMySitePropertyValue(client.endPointUrlPropertyKey());
				
				LOG.info(">>>>>>>>>>>>>>>>>> endPointUrl:" + endPointUrl);

				Asserts.isFalse(StringUtils.isEmpty(endPointUrl), client.endPointUrlPropertyKey() + " is not defined in configFile.");
				JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
				factory.getInInterceptors().add(new LoggingInInterceptor());
				if (config.enableWebServiceAccessFilter) {
					factory.getInInterceptors().add(webServiceAccessFilterIntercepter);					
				}
				factory.getOutInterceptors().add(new LoggingOutInterceptor());
				factory.setAddress(endPointUrl);
				factory.setServiceClass(field.getType());
				
				if (!webServiceInterfaces.contains(field.getType())) {
					webServiceInterfaces.add(field.getType());
				}
				
				if (!StringUtils.isEmpty(client.usernamePropertyKey())) {
					String username = config.getPropatyValue(client.usernamePropertyKey());
					if (!StringUtils.isEmpty(username)) {
						factory.setUsername(username);
					}
				}
				if (!StringUtils.isEmpty(client.passwordPropertyKey())) {
					String password = config.getPropatyValue(client.passwordPropertyKey());
					if (!StringUtils.isEmpty(password)) {
						factory.setPassword(password);
					}
				}
				Object clientService = factory.create();
				final Client cl = ClientProxy.getClient(clientService);
				HTTPConduit http = (HTTPConduit) cl.getConduit();
				final HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
			  
	            httpClientPolicy.setReceiveTimeout(client.receiveTimeout());
	            httpClientPolicy.setConnectionTimeout(client.connectionTimeout());
	 
	            http.setClient(httpClientPolicy);
				field.setAccessible(true);
				field.set(bean, clientService);

			}
		}

	}

}
