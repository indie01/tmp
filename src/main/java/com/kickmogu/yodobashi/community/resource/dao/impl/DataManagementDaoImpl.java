package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.domain.SearchResult;
import com.kickmogu.lib.hadoop.hbase.HBaseContainer;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseTableMeta;
import com.kickmogu.lib.solr.SolrContainer;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.lib.solr.meta.SolrSchemaMeta;
import com.kickmogu.yodobashi.community.resource.dao.DataManagementDao;
import com.kickmogu.yodobashi.community.resource.domain.DataManagementDO;
import com.kickmogu.yodobashi.community.resource.domain.DataManagementDataDO;
import com.kickmogu.yodobashi.community.resource.domain.DataManagementSearchResult;
import com.kickmogu.yodobashi.community.resource.domain.DataManagementTableDO;

@Service
public class DataManagementDaoImpl implements DataManagementDao {

	@Autowired @Qualifier("MySite")
	private HBaseContainer hBaseContainer;
	
	@Autowired @Qualifier("MySite")
	private SolrContainer solrContainer;
	
	@Autowired  @Qualifier("default")
	private HBaseOperations hBaseOperations;
	
	@Autowired @Qualifier("default")
	private SolrOperations solrOperations;
	

	public void sethBaseContainer(HBaseContainer hBaseContainer) {
		this.hBaseContainer = hBaseContainer;
	}


	public void setSolrContainer(SolrContainer solrContainer) {
		this.solrContainer = solrContainer;
	}


	public void sethBaseOperations(HBaseOperations hBaseOperations) {
		this.hBaseOperations = hBaseOperations;
	}


	public void setSolrOperations(SolrOperations solrOperations) {
		this.solrOperations = solrOperations;
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public DataManagementDO getDataManagement() {
		
		DataManagementDO dataManagementDO = new DataManagementDO();
		
		for (HBaseTableMeta tableMeta : hBaseContainer.getMeta().getAllTableMeta()) {
			DataManagementTableDO tableDO = new DataManagementTableDO();
			tableDO.setDomainObjectType(tableMeta.getType());
			tableDO.sethBaseTableMeta(tableMeta);
			dataManagementDO.getTableMap().put(tableMeta.getType(), tableDO);
		}
		
		for (Map.Entry<Class<?>, SolrSchemaMeta> entry:solrContainer.getMeta().getSchemaMetaMap().entrySet()) {
			Class<?> type = entry.getKey();
			if (!dataManagementDO.getTableMap().containsKey(type)) {
				DataManagementTableDO tableDO = new DataManagementTableDO();
				tableDO.setDomainObjectType(type);
				tableDO.setSolrSchemaMeta(entry.getValue());
				dataManagementDO.getTableMap().put(type, tableDO);
			} else {
				dataManagementDO.getTableMap().get(type).setSolrSchemaMeta(entry.getValue());
			}
		}
		
		return dataManagementDO;
	}
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public DataManagementDO getDataManagement(Class<?>... types) {
		
		DataManagementDO dataManagementDO = new DataManagementDO();
		
		for (HBaseTableMeta tableMeta : hBaseContainer.getMeta().getAllTableMeta()) {
			if (!ArrayUtils.contains(types, tableMeta.getType())) continue;
			DataManagementTableDO tableDO = new DataManagementTableDO();
			tableDO.setDomainObjectType(tableMeta.getType());
			tableDO.sethBaseTableMeta(tableMeta);
			dataManagementDO.getTableMap().put(tableMeta.getType(), tableDO);
		}
		
		for (Map.Entry<Class<?>, SolrSchemaMeta> entry:solrContainer.getMeta().getSchemaMetaMap().entrySet()) {
			Class<?> type = entry.getKey();
			if (!ArrayUtils.contains(types, type)) continue;
			if (!dataManagementDO.getTableMap().containsKey(type)) {
				DataManagementTableDO tableDO = new DataManagementTableDO();
				tableDO.setDomainObjectType(type);
				tableDO.setSolrSchemaMeta(entry.getValue());
				dataManagementDO.getTableMap().put(type, tableDO);
			} else {
				dataManagementDO.getTableMap().get(type).setSolrSchemaMeta(entry.getValue());
			}
		}
		
		return dataManagementDO;
	}



	@SuppressWarnings("rawtypes")
	@Override
	public DataManagementTableDO getManagementTableByDoName(String doName) {
		for(DataManagementTableDO tableDO: getDataManagement().getTables()) {
			if (tableDO.getDoName().equals(doName)) return tableDO;
		}
		return null;
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public DataManagementDataDO getByHBase(DataManagementTableDO table, String hbaseGetKey) {

		Object hbaseDomainObject = hBaseOperations.load(table.getDomainObjectType(), hbaseGetKey);

		Object solrDomainObject = null;
		if (table.hbaseKeyFieldEqualsToSolrUniqueField()) {
			solrDomainObject = solrOperations.load(table.getDomainObjectType(), hbaseGetKey);
		}
		if (hbaseDomainObject == null && solrDomainObject == null) {
			return null;
		}
		DataManagementDataDO dataManagementData = new DataManagementDataDO(table);
		dataManagementData.appendSiteData("HBase", hbaseDomainObject, dataManagementData.getTable().gethBaseTableMeta());
		if (table.getSolrSchemaMeta() != null) {
			dataManagementData.appendSiteData("Solr", solrDomainObject, dataManagementData.getTable().getSolrSchemaMeta());			
		}
		return dataManagementData;
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public DataManagementSearchResult scanByHBase(DataManagementTableDO table, String startKey,
			String endKey, Integer rows, Integer start) {
		int maxCount = rows*start ;
		Class type = table.getDomainObjectType();
		Filter filter = hBaseOperations.createFilterBuilder(type).appendPageFilter(maxCount+1).toFilter();
		List list = hBaseOperations.scan(type, StringUtils.isEmpty(startKey) ? null : startKey, StringUtils.isEmpty(endKey) ? null : endKey, filter);
		return hbaseResultToDataManagementSearchResult(table, list, maxCount, rows, start);
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public DataManagementSearchResult getIndexByHBase(
			DataManagementTableDO table, String indexName, String hbaseGetKey, Integer rows, Integer start) {

		int maxCount = rows*start ;
		Class type = table.getDomainObjectType();
		Filter filter = hBaseOperations.createFilterBuilder(type).appendPageFilter(maxCount+1).toFilter();
		List list = hBaseOperations.scanWithIndex(type, indexName, StringUtils.isEmpty(hbaseGetKey) ? null : hbaseGetKey,  filter);
		return hbaseResultToDataManagementSearchResult(table, list, maxCount, rows, start);
	}
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public DataManagementSearchResult scanIndexByHBase(
			DataManagementTableDO table, String indexName,
			String hbaseStartValue, String hbaseEndValue, Integer rows,
			Integer start) {
		int maxCount = rows*start ;
		Class type = table.getDomainObjectType();
		Filter filter = hBaseOperations.createFilterBuilder(type).appendPageFilter(maxCount+1).toFilter();
		List list = hBaseOperations.scanWithIndex(type, indexName, StringUtils.isEmpty(hbaseStartValue) ? null : hbaseStartValue,  StringUtils.isEmpty(hbaseEndValue) ? null : hbaseEndValue, filter);
		return hbaseResultToDataManagementSearchResult(table, list, maxCount, rows, start);
	}
	


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataManagementSearchResult selectBySolr(DataManagementTableDO table,
			SolrQuery query) {
		SearchResult solrResult = solrOperations.findByQuery(query, table.getDomainObjectType());
		List<DataManagementDataDO> resultList  = Lists.newArrayList();
		for (Object domainObject:solrResult.getDocuments()) {
			DataManagementDataDO dataManagementData = new DataManagementDataDO(table);
			Object key = table.gethBaseTableMeta().getKey(domainObject);
			dataManagementData.appendSiteData("HBase", hBaseOperations.load(domainObject.getClass(), key), dataManagementData.getTable().gethBaseTableMeta());
			dataManagementData.appendSiteData("Solr", domainObject, dataManagementData.getTable().getSolrSchemaMeta());
			resultList.add(dataManagementData);
		}
		return new DataManagementSearchResult(solrResult.getNumFound(), resultList, table);
	}



	@SuppressWarnings({ "rawtypes", "unchecked" })
	private DataManagementSearchResult hbaseResultToDataManagementSearchResult(DataManagementTableDO table, List list, int maxCount, Integer rows, Integer start) {
		
		Long numFound = list.size() == maxCount+1 ? null : Long.valueOf(list.size());
		
		List<DataManagementDataDO> resultList  = Lists.newArrayList();
		
		for (int i = rows*(start-1) ; i < rows*(start-1)+rows ; i++) {
			if (i >= list.size()) break;
			DataManagementDataDO dataManagementData = new DataManagementDataDO(table);
			Object domainObject = list.get(i);
			dataManagementData.appendSiteData("HBase", domainObject, dataManagementData.getTable().gethBaseTableMeta());
			if (table.getSolrSchemaMeta() != null) {
				Object key = table.gethBaseTableMeta().getKey(domainObject);
				dataManagementData.appendSiteData("Solr", solrOperations.load(table.getDomainObjectType(), key), dataManagementData.getTable().getSolrSchemaMeta());				
			}
			resultList.add(dataManagementData);
		}

		return new DataManagementSearchResult(numFound, resultList, table);
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void deleteByHBase(DataManagementTableDO table, String key) {
		Class type = table.getDomainObjectType();
		hBaseOperations.deleteByKey(type, key);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void deleteBySolr(DataManagementTableDO table, String key) {
		Class type = table.getDomainObjectType();
		solrOperations.deleteByKey(type, key);
	}
	
	

}
