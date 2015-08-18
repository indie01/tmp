package com.kickmogu.yodobashi.community.resource.config;

import org.hsqldb.lib.StringUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.kickmogu.lib.core.cofig.BaseConfig;

@Configuration
public class OracleConfig extends BaseConfig implements InitializingBean {

	/**
	 * リソースコンフィグです。
	 */
	@Autowired
	private ResourceConfig resourceConfig;

	public static OracleConfig INSTANCE;

	@Value("${jdbc.driverClassName}")
	public String jdbcDriverClassName;

	@Value("${jdbc.url}")
	public String jdbcUrl;

	public String jdbcProductLoaderUrl;

	public String jdbcReviewPointSummaryUrl;
	
	@Value("${jdbc.username}")
	public String jdbcUserName;

	@Value("${jdbc.password}")
	public String jdbcPassword;

	@Value("${jdbc.maxActive}")
	public Integer jdbcMaxActive;

	@Value("${jdbc.maxWait}")
	public Long jdbcMaxWait;
	
	/**
	 * 初期化します。
	 */
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		jdbcDriverClassName = resourceConfig.getMySitePropertyValue("jdbc.driverClassName");
		jdbcUrl = resourceConfig.getMySitePropertyValue("jdbc.url");
		jdbcProductLoaderUrl = resourceConfig.getMySitePropertyValue("jdbc.productLoader.url");
		if (StringUtil.isEmpty(jdbcProductLoaderUrl)) {
			jdbcProductLoaderUrl = jdbcUrl;
		}
		jdbcReviewPointSummaryUrl = resourceConfig.getMySitePropertyValue("jdbc.reviewPointSummary.url");
		if (StringUtil.isEmpty(jdbcReviewPointSummaryUrl)) {
			jdbcReviewPointSummaryUrl = jdbcUrl;
		}
		jdbcUserName = resourceConfig.getMySitePropertyValue("jdbc.username");
		jdbcPassword = resourceConfig.getMySitePropertyValue("jdbc.password");
		jdbcMaxActive = Integer.valueOf(resourceConfig.getMySitePropertyValue("jdbc.maxActive"));
		jdbcMaxWait = Long.valueOf(resourceConfig.getMySitePropertyValue("jdbc.maxWait"));
	}
}
