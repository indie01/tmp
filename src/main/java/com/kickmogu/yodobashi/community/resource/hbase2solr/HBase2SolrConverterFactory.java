package com.kickmogu.yodobashi.community.resource.hbase2solr;

import java.util.Map;

import com.google.common.collect.Maps;


public class HBase2SolrConverterFactory {

	@SuppressWarnings("rawtypes")
	private Map<Class<?>, HBase2SolrConverter> converterMap = Maps.newHashMap();
	
	@SuppressWarnings("rawtypes")
	public  HBase2SolrConverter createHBase2SolrConverter(Class type) {
		return converterMap.get(type);
	}
	
	@SuppressWarnings("rawtypes")
	public void registerHBase2SolrConverter(Class type, HBase2SolrConverter hBase2SolrConverter) {
		converterMap.put(type, hBase2SolrConverter);
	}
	
	public int size() {
		return converterMap.size();
	}


}
