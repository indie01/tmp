package com.kickmogu.yodobashi.community.resource.config;

import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.Lists;
import com.kickmogu.lib.solr.SolrDynamicFieldDefinition;


public class SolrDynamicFieldDefinitions implements InitializingBean,ApplicationContextAware {

	public static final List<SolrDynamicFieldDefinition> LIST;
	
	static {
		LIST = Lists.newArrayList(
			new SolrDynamicFieldDefinition().setName("*_s").setFieldType("string").setIndexed(true).setStored(true),
			new SolrDynamicFieldDefinition().setName("*_b").setFieldType("boolean").setIndexed(true).setStored(true),
			new SolrDynamicFieldDefinition().setName("*_i").setFieldType("tint").setIndexed(true).setStored(true),
			new SolrDynamicFieldDefinition().setName("*_l").setFieldType("tlong").setIndexed(true).setStored(true),
			new SolrDynamicFieldDefinition().setName("*_f").setFieldType("tfloat").setIndexed(true).setStored(true),
			new SolrDynamicFieldDefinition().setName("*_d").setFieldType("tdouble").setIndexed(true).setStored(true),
			new SolrDynamicFieldDefinition().setName("*_dt").setFieldType("tdate").setIndexed(true).setStored(true),
			new SolrDynamicFieldDefinition().setName("*_bn").setFieldType("binary").setIndexed(false).setStored(true),
			
			new SolrDynamicFieldDefinition().setName("*_s_d").setFieldType("string").setIndexed(false).setStored(true),
			new SolrDynamicFieldDefinition().setName("*_b_d").setFieldType("boolean").setIndexed(false).setStored(true),
			new SolrDynamicFieldDefinition().setName("*_i_d").setFieldType("tint").setIndexed(false).setStored(true),
			new SolrDynamicFieldDefinition().setName("*_l_d").setFieldType("tlong").setIndexed(false).setStored(true),
			new SolrDynamicFieldDefinition().setName("*_f_d").setFieldType("tfloat").setIndexed(false).setStored(true),
			new SolrDynamicFieldDefinition().setName("*_d_d").setFieldType("tdouble").setIndexed(false).setStored(true),
			new SolrDynamicFieldDefinition().setName("*_dt_d").setFieldType("tdate").setIndexed(false).setStored(true),

			new SolrDynamicFieldDefinition().setName("*_ms").setFieldType("string").setIndexed(true).setStored(true).setMultiValued(true),
			new SolrDynamicFieldDefinition().setName("*_mb").setFieldType("boolean").setIndexed(true).setStored(true).setMultiValued(true),
			new SolrDynamicFieldDefinition().setName("*_mi").setFieldType("tint").setIndexed(true).setStored(true).setMultiValued(true),
			new SolrDynamicFieldDefinition().setName("*_ml").setFieldType("tlong").setIndexed(true).setStored(true).setMultiValued(true),
			new SolrDynamicFieldDefinition().setName("*_mf").setFieldType("tfloat").setIndexed(true).setStored(true).setMultiValued(true),
			new SolrDynamicFieldDefinition().setName("*_md").setFieldType("tdouble").setIndexed(true).setStored(true).setMultiValued(true),
			new SolrDynamicFieldDefinition().setName("*_mdt").setFieldType("tdate").setIndexed(true).setStored(true).setMultiValued(true),
			
			new SolrDynamicFieldDefinition().setName("*_ms_d").setFieldType("string").setIndexed(false).setStored(true).setMultiValued(true),
			new SolrDynamicFieldDefinition().setName("*_mb_d").setFieldType("boolean").setIndexed(false).setStored(true).setMultiValued(true),
			new SolrDynamicFieldDefinition().setName("*_mi_d").setFieldType("tint").setIndexed(false).setStored(true).setMultiValued(true),
			new SolrDynamicFieldDefinition().setName("*_ml_d").setFieldType("tlong").setIndexed(false).setStored(true).setMultiValued(true),
			new SolrDynamicFieldDefinition().setName("*_mf_d").setFieldType("tfloat").setIndexed(false).setStored(true).setMultiValued(true),
			new SolrDynamicFieldDefinition().setName("*_md_d").setFieldType("tdouble").setIndexed(false).setStored(true).setMultiValued(true),
			new SolrDynamicFieldDefinition().setName("*_mdt_d").setFieldType("tdate").setIndexed(false).setStored(true).setMultiValued(true)
		);
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		((DefaultListableBeanFactory)applicationContext.getAutowireCapableBeanFactory()).registerSingleton("defaultDynamicFieldDefinitions", LIST);
	}

	private ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
}
