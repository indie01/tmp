package com.kickmogu.yodobashi.community.resource.hbase2solr.config;

import org.apache.commons.lang.WordUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kickmogu.lib.core.utils.Asserts;
import com.kickmogu.lib.core.utils.Reflections;
import com.kickmogu.lib.hadoop.hbase.HBaseContainer;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseMeta;
import com.kickmogu.lib.hadoop.hbase.service.HBaseRecoveryService;
import com.kickmogu.lib.hadoop.hbase.service.impl.HBaseRecoveryServiceImpl;
import com.kickmogu.lib.solr.SolrContainer;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.resource.hbase2solr.HBase2SolrConverter;
import com.kickmogu.yodobashi.community.resource.hbase2solr.HBase2SolrConverterFactory;
import com.kickmogu.yodobashi.community.resource.hbase2solr.HBase2SolrLoader;
import com.kickmogu.yodobashi.community.resource.hbase2solr.annotation.HBase2Solr;
import com.kickmogu.yodobashi.community.resource.hbase2solr.impl.DefaultHBase2SolrConverterImpl;
import com.kickmogu.yodobashi.community.resource.hbase2solr.impl.HBase2SolrLoaderImpl;
import com.kickmogu.yodobashi.community.resource.hbase2solr.impl.SolrRecoveryServiceFilter;

@Configuration
public class HBase2SolrContext  {
	
	@Autowired
	private ApplicationContext applicationContext;

	@Autowired @Qualifier("MySite")
	private HBaseContainer hBaseContainer;
	
	@Autowired @Qualifier("MySite")
	private SolrContainer solrContainer;
	
	@Autowired @Qualifier("MySite")
	private HBaseOperations hbaseOperations;
	
	@Autowired @Qualifier("MySite")
	private SolrOperations solrOperations;
	
	@Autowired
	private HBaseMeta hBaseMeta;
	
	@Bean @Qualifier("HBaseAndSolr")
	public HBaseRecoveryService hBaseSolrRecoveryService()  throws Exception {
		HBaseRecoveryServiceImpl hBaseRecoveryServiceImpl = new HBaseRecoveryServiceImpl();
		hBaseRecoveryServiceImpl.setContainer(hBaseContainer);
		hBaseRecoveryServiceImpl.addFilter(solrRecoveryServiceFilter());
		return hBaseRecoveryServiceImpl;
	}
	
	@Bean
	public HBase2SolrLoader hBase2SolrLoader() throws Exception {
		HBase2SolrLoaderImpl hBase2SolrLoaderImpl = new HBase2SolrLoaderImpl();
		hBase2SolrLoaderImpl.sethBaseMeta(hBaseMeta);
		hBase2SolrLoaderImpl.setConverterFactory(hBase2SolrConverterFactory());
		hBase2SolrLoaderImpl.sethBaseOperations(hbaseOperations);
		hBase2SolrLoaderImpl.setSolrOperations(solrOperations);
		return hBase2SolrLoaderImpl;
	}
	
	@Bean
	public SolrRecoveryServiceFilter solrRecoveryServiceFilter()  throws Exception {
		SolrRecoveryServiceFilter solrRecoveryServiceFilter = new SolrRecoveryServiceFilter();
		solrRecoveryServiceFilter.sethBaseContainer(hBaseContainer);
		solrRecoveryServiceFilter.setSolrContainer(solrContainer);
		solrRecoveryServiceFilter.sethBase2SolrLoader(hBase2SolrLoader());
		return solrRecoveryServiceFilter;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
	public HBase2SolrConverterFactory hBase2SolrConverterFactory() throws Exception {
		HBase2SolrConverterFactory factory = new HBase2SolrConverterFactory();
		for (Class<?> type:Reflections.getClassesByPackage(DefaultHBase2SolrConverterImpl.class.getPackage())) {
			HBase2Solr.List hbase2SolrList = type.getAnnotation(HBase2Solr.List.class);
			if (hbase2SolrList == null) continue;
			for (HBase2Solr hbase2Solr:hbase2SolrList.value()) {
				for (Class<?> convertType: hbase2Solr.value()) {
					HBase2SolrConverter converter = (HBase2SolrConverter)type.newInstance();
					converter.setType(convertType);
					applicationContext.getAutowireCapableBeanFactory().autowireBeanProperties(converter,
							AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
					((DefaultListableBeanFactory)applicationContext.getAutowireCapableBeanFactory()).registerSingleton(WordUtils.uncapitalize(convertType.getSimpleName()) + HBase2SolrConverter.class.getSimpleName(), converter);
					if (converter instanceof InitializingBean) {
						((InitializingBean)converter).afterPropertiesSet();
					}
					converter.setBulkSize(hbase2Solr.bulkSize());
					factory.registerHBase2SolrConverter(convertType, converter);
				}
			}
		}
		Asserts.isTrue(factory.size() > 0);
		return factory;
	}


}
