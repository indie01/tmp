package com.kickmogu.yodobashi.community.resource.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import com.kickmogu.lib.core.cofig.BaseConfig;
import com.kickmogu.lib.core.resource.Site;

@Configuration
@ImportResource("classpath:/webservice-client-config.xml")
public class WebServiceClientConfig extends BaseConfig {

	public static WebServiceClientConfig INSTANCE;

	@Value("${hbase.mysite}")
	public Site hbaseMySite;
	
	@Value("${enable.webServiceAccessFilter}")
	public boolean enableWebServiceAccessFilter;

	public String getMySitePropertyValue(String key) {
		try  {
			return resolver.resolveStringValue("${" + hbaseMySite.name() + "." + key +"}");
		} catch (IllegalArgumentException e) {
		}

		return getPropatyValue(key);
	}
}
