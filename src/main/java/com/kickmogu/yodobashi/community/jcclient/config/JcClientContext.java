package com.kickmogu.yodobashi.community.jcclient.config;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kickmogu.lib.core.resource.Site;
import com.kickmogu.yodobashi.community.resource.config.HBaseConfig;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;

@Configuration
public class JcClientContext implements DisposableBean {

	/**
	 * HBaseコンフィグです。
	 */
	@Autowired
	private HBaseConfig hbaseConfig;

	/**
	 * ジョブクライアントコンフィグです。
	 */
	@Autowired
	private JcClientConfig jcClientConfig;

	/**
	 * リソースコンフィグです。
	 */
	@Autowired
	private ResourceConfig resourceConfig;
	
	private PooledConnectionFactory pooledFactory = null;

	/**
	 * アクティブMQのコンポーネントです。
	 * @return アクティブMQのコンポーネント
	 */
	@Bean
	public ConnectionFactory activeMQConnectionFactory() {
		if (jcClientConfig.mqConnect && !resourceConfig.messageForceSyncNow) {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
			if (hbaseConfig.hbaseMySite.equals(Site.SITE1)) {
				connectionFactory.setBrokerURL(jcClientConfig.site1ProducerBrokerUrl);
			} else {
				connectionFactory.setBrokerURL(jcClientConfig.site2ProducerBrokerUrl);
			}

			pooledFactory = new PooledConnectionFactory();
			pooledFactory.setMaxConnections(jcClientConfig.maxConnections);
			pooledFactory.setMaximumActive(jcClientConfig.maximumActive);
			pooledFactory.setConnectionFactory(connectionFactory);

			return pooledFactory;
		} else {
			return null;
		}
	}

	@Override
	public void destroy() throws Exception {
		if( pooledFactory != null)
			pooledFactory.stop();
	}
}
