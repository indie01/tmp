package com.kickmogu.yodobashi.community.resource.hbase2solr.impl;

import java.util.List;

import com.google.common.collect.Lists;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseColumnMetaBase;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseKeyMeta;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseMeta;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseTableMeta;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.lib.solr.meta.SolrMeta;
import com.kickmogu.lib.solr.meta.SolrPropertyMetaBase;
import com.kickmogu.lib.solr.meta.SolrSchemaMeta;
import com.kickmogu.yodobashi.community.resource.hbase2solr.HBase2SolrConvertContext;

public class HBase2SolrConverterUtil {

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> String[] getLoadHBasePropertyNames(Class<T> type, HBaseMeta hbaseMeta, SolrMeta solrMeta) {
		
		List<String> hBasePropertyNameList = Lists.newArrayList();
		
		HBaseTableMeta hBaseTableMeta = hbaseMeta.getTableMeta(type);
		SolrSchemaMeta<T> solrSchemaMeta = solrMeta.getSchemaMeta(type);
		hBasePropertyNameList.add(hBaseTableMeta.getKeyMeta().getPropertyName());
		for (HBaseColumnMetaBase columnMeta : hBaseTableMeta.getAllColumnMeta()) {
			for (SolrPropertyMetaBase fieldMeta : solrSchemaMeta.getAllFieldMetas()) {
				if (columnMeta.getPropertyName().equals(fieldMeta.getPropertyName())) {
					hBasePropertyNameList.add(columnMeta.getColumnName());
				}
			}
		}
		return hBasePropertyNameList.toArray(new String[hBasePropertyNameList.size()]);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> void loadSolr(HBaseTableMeta hbaseTableMeta, HBase2SolrConvertContext convertContext, SolrOperations solrOperations) {
		
		Class<?> type = hbaseTableMeta.getType();
		
		if (convertContext.getSolrUpdateList() != null && !convertContext.getSolrUpdateList().isEmpty()) {
			solrOperations.save(type, convertContext.getSolrUpdateList());
		}
		
		List keyList = Lists.newArrayList();
		if (convertContext.getSolrDeleteKeyList() != null) {
			keyList.addAll(convertContext.getSolrDeleteKeyList());
		}
		
		HBaseKeyMeta keyMeta =hbaseTableMeta.getKeyMeta();
		
		if (convertContext.getSolrDeleteList() != null) {
			
			for (Object deleteObject:convertContext.getSolrDeleteList()) {
				Object key = keyMeta.getValue(deleteObject);
				if (!keyList.contains(key)) {
					keyList.add(key);
				}
			}
		}
		if (!keyList.isEmpty()) {
			solrOperations.deleteByKeys(type, keyMeta.getType(), keyList);
		}
	}
}
