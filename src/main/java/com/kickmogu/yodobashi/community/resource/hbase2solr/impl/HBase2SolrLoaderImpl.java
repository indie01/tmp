package com.kickmogu.yodobashi.community.resource.hbase2solr.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseMeta;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.lib.solr.annotation.SolrTiming;
import com.kickmogu.yodobashi.community.resource.hbase2solr.HBase2SolrConvertContext;
import com.kickmogu.yodobashi.community.resource.hbase2solr.HBase2SolrConverter;
import com.kickmogu.yodobashi.community.resource.hbase2solr.HBase2SolrConverterFactory;
import com.kickmogu.yodobashi.community.resource.hbase2solr.HBase2SolrLoader;


public class HBase2SolrLoaderImpl implements HBase2SolrLoader {


	private HBaseOperations hBaseOperations;
	

	private SolrOperations solrOperations;
	

	private HBaseMeta hBaseMeta;
	

	private HBase2SolrConverterFactory converterFactory;
	

	public void sethBaseOperations(HBaseOperations hBaseOperations) {
		this.hBaseOperations = hBaseOperations;
	}


	public void setSolrOperations(SolrOperations solrOperations) {
		this.solrOperations = solrOperations;
	}


	public void sethBaseMeta(HBaseMeta hBaseMeta) {
		this.hBaseMeta = hBaseMeta;
	}


	public void setConverterFactory(HBase2SolrConverterFactory converterFactory) {
		this.converterFactory = converterFactory;
	}


	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> void loadByKey(Class<T> type, Object key) {
		HBase2SolrConverter converter = converterFactory.createHBase2SolrConverter(type);
		HBase2SolrConvertContext convertContext = new HBase2SolrConvertContext();
		T hbaseObject = hBaseOperations.load(type, key, getPathCondition(converter));
		if (hbaseObject != null) {
			convertContext.getHbaseDataList().add(hbaseObject);
			converter.convert(convertContext.getHbaseDataList(), convertContext);
		} else {
			convertContext.getHbaseDeleteKeyList().add(key);
			converter.convertDeleted(convertContext.getHbaseDeleteKeyList(), convertContext);			
		}
		HBase2SolrConverterUtil.loadSolr(hBaseMeta.getTableMeta(type), convertContext, solrOperations);

	}
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <T> void loadByKeyList(Class<T> type, List keyList) {
		HBase2SolrConverter converter = converterFactory.createHBase2SolrConverter(type);
		HBase2SolrConvertContext convertContext = new HBase2SolrConvertContext();
		Class keyType = hBaseMeta.getTableMeta(type).getKeyMeta().getType();
		Map<Object,T> resultMap = hBaseOperations.find(type, keyType, keyList);
		for (Object key:keyList) {
			Object hbaseObject = resultMap.get(key);
			if (hbaseObject != null) {
				convertContext.getHbaseDataList().add(hbaseObject);
				converter.convert(convertContext.getHbaseDataList(), convertContext);
			} else {
				convertContext.getHbaseDeleteKeyList().add(key);
				converter.convertDeleted(convertContext.getHbaseDeleteKeyList(), convertContext);			
			}
		}
		HBase2SolrConverterUtil.loadSolr(hBaseMeta.getTableMeta(type), convertContext, solrOperations);
	}
	
	@SuppressWarnings("rawtypes")
	private Path.Condition getPathCondition(HBase2SolrConverter converter) {
		Path.Condition pathCondition = new Path.Condition();
		for (String propertyName:converter.getLoadHBasePropertyNames()) {
			pathCondition.includeProp(propertyName);
		}
		return pathCondition;
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> void loadByKeyRange(Class<T> type, Object startKey, Object endKey) {
		HBase2SolrConverter converter = converterFactory.createHBase2SolrConverter(type);
		System.out.println("converter:" + converter);
		HBase2SolrConvertContext convertContext = new HBase2SolrConvertContext();
		convertContext.setHbaseDataList(hBaseOperations.scan(type, startKey, endKey, getPathCondition(converter)));
		converter.convert(convertContext.getHbaseDataList(), convertContext);
		HBase2SolrConverterUtil.loadSolr(hBaseMeta.getTableMeta(type), convertContext, solrOperations);

	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ArroundSolr(commit=SolrTiming.NONE)
	public <T> void loadByHBaseObjects(Class<T> type, List<T> objects) {
		
		HBase2SolrConverter converter = converterFactory.createHBase2SolrConverter(type);
		HBase2SolrConvertContext convertContext = new HBase2SolrConvertContext();
		
		// Bulkサイズに分割して登録要求を発行
		int bulkSize = converter.getBulkSize();
		List<T> registerUnit = new ArrayList<T>();
		for (int i = 0; i < objects.size(); i++) {
			registerUnit.add(objects.get(i));
			if (bulkSize == 0 || ((i+1) == objects.size()) || (((i+1) % bulkSize) == 0)) {
				convertContext.setHbaseDataList(registerUnit);
				converter.convert(convertContext.getHbaseDataList(), convertContext);
				HBase2SolrConverterUtil.loadSolr(hBaseMeta.getTableMeta(type), convertContext, solrOperations);
				registerUnit.clear();
				convertContext.clear();
			}
		}
	}


}
