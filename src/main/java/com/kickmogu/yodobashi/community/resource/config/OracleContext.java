package com.kickmogu.yodobashi.community.resource.config;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;



@Configuration @Lazy
public class OracleContext {

	/**
	 * オラクルのコンフィグです。
	 */
	@Autowired
	private OracleConfig oracleConfig;

	/**
	 * DBに接続するテンプレートを返します。
	 * @return DBに接続するテンプレート
	 */
	@Bean @Qualifier("default")
	public JdbcTemplate jdbcTemplate() {
		return new JdbcTemplate(dataSource());
	}

	@Bean @Qualifier("productLoader")
	public JdbcTemplate jdbcTemplateForProductDetailLoader() {
		return new JdbcTemplate(dataSourceForProductDetailLoader());
	}
	
	@Bean @Qualifier("reviewPointSummary")
	public JdbcTemplate jdbcTemplateForReviewPointSummary() {
		return new JdbcTemplate(dataSourceForReviewPointSummary());
	}

	/**
	 * DBに接続するデータソースを返します。
	 * @return DBに接続するデータソース
	 */
	@Bean @Qualifier("default")
	public DataSource dataSource() {
		return defaultDataSource();
	}

	@Bean @Qualifier("productLoader")
	public DataSourceTransactionManager dataSourceTransactionManagerForProductDetailLoader() {
		DataSourceTransactionManager dtm = new DataSourceTransactionManager();
		dtm.setDataSource(dataSourceForProductDetailLoader());
		return dtm;
	}
	
	@Bean @Qualifier("reviewPointSummary")
	public DataSourceTransactionManager dataSourceTransactionManagerForReviewPointSummary() {
		DataSourceTransactionManager dtm = new DataSourceTransactionManager();
		dtm.setDataSource(dataSourceForReviewPointSummary());
		return dtm;
	}
	
	@Bean @Qualifier("reviewPointSummary")
	public DataSource dataSourceForReviewPointSummary() {
		BasicDataSource dataSource = (BasicDataSource) defaultDataSource();
		dataSource.setUrl(oracleConfig.jdbcReviewPointSummaryUrl);
		dataSource.setMaxActive(1);
		dataSource.setMaxWait(-1);
		return dataSource;
	}

	/**
	 * ProductDetailLoader用設定
	 * @return
	 */
	@Bean @Qualifier("productLoader")
	public DataSource dataSourceForProductDetailLoader() {
		BasicDataSource dataSource = (BasicDataSource) defaultDataSource();
		dataSource.setUrl(oracleConfig.jdbcProductLoaderUrl);
		dataSource.setMaxActive(1);
		dataSource.setMaxWait(-1);
		
		return dataSource;
	}

	private DataSource defaultDataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(oracleConfig.jdbcDriverClassName);
		dataSource.setUrl(oracleConfig.jdbcUrl);
		dataSource.setUsername(oracleConfig.jdbcUserName);
		dataSource.setPassword(oracleConfig.jdbcPassword);
		dataSource.setDefaultAutoCommit(false);
		dataSource.setMaxActive(oracleConfig.jdbcMaxActive);
		dataSource.setMaxWait(oracleConfig.jdbcMaxWait);
		dataSource.setValidationQuery("select 1 from dual");
		dataSource.setTestWhileIdle(true);
		dataSource.setTimeBetweenEvictionRunsMillis(10*1000);
		dataSource.setMinEvictableIdleTimeMillis(10*1000);
		dataSource.setMinIdle(1);
		return dataSource;
	}
}
