package com.kickmogu.yodobashi.community.service;

import org.apache.solr.client.solrj.SolrQuery;

import com.kickmogu.yodobashi.community.resource.domain.DataManagementDO;
import com.kickmogu.yodobashi.community.resource.domain.DataManagementSearchResult;
import com.kickmogu.yodobashi.community.resource.domain.DataManagementTableDO;

public interface DataManagementService {
	DataManagementDO getDataManagement();
	DataManagementDO getDataManagement(Class<?>... types);
	@SuppressWarnings("rawtypes")
	DataManagementTableDO getManagementTableByDoName(String doName);
	@SuppressWarnings("rawtypes")
	DataManagementSearchResult getByHBase(DataManagementTableDO table, String hbaseGetKey);
	@SuppressWarnings("rawtypes")
	DataManagementSearchResult scanByHBase(DataManagementTableDO table,
			String startKey, String endKey, Integer rows, Integer start);
	@SuppressWarnings("rawtypes")
	DataManagementSearchResult getIndexByHBase(DataManagementTableDO table,
			String indexName, String hbaseGetKey, Integer rows, Integer start);
	@SuppressWarnings("rawtypes")
	DataManagementSearchResult scanIndexByHBase(DataManagementTableDO table,
			String indexName, String hbaseStartValue, String hbaseEndValue,
			Integer rows, Integer start);
	@SuppressWarnings("rawtypes")
	DataManagementSearchResult selectBySolr(DataManagementTableDO table,
			SolrQuery query);
	@SuppressWarnings("rawtypes")
	void deleteByHBase(DataManagementTableDO table, String key);
	@SuppressWarnings("rawtypes")
	void deleteBySolr(DataManagementTableDO table, String key);
	
}
