package com.kickmogu.yodobashi.community.service.impl;

import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.kickmogu.yodobashi.community.resource.dao.DataManagementDao;
import com.kickmogu.yodobashi.community.resource.domain.DataManagementDO;
import com.kickmogu.yodobashi.community.resource.domain.DataManagementDataDO;
import com.kickmogu.yodobashi.community.resource.domain.DataManagementSearchResult;
import com.kickmogu.yodobashi.community.resource.domain.DataManagementTableDO;
import com.kickmogu.yodobashi.community.service.DataManagementService;

@Service
public class DataManagementServiceImpl implements DataManagementService {
	
	@Autowired
	private DataManagementDao daoManagementDao;

	public void setDaoManagementDao(DataManagementDao daoManagementDao) {
		this.daoManagementDao = daoManagementDao;
	}

	@Override
	public DataManagementDO getDataManagement() {
		return daoManagementDao.getDataManagement();
	}
	
	@Override
	public DataManagementDO getDataManagement(Class<?>... types) {
		return daoManagementDao.getDataManagement(types);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public DataManagementTableDO getManagementTableByDoName(String doName) {
		return daoManagementDao.getManagementTableByDoName(doName);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public DataManagementSearchResult getByHBase(DataManagementTableDO table, String hbaseGetKey) {
		DataManagementDataDO dataManagementData = daoManagementDao.getByHBase(table, hbaseGetKey);
		if (dataManagementData == null) return DataManagementSearchResult.EMPTY;
		return new DataManagementSearchResult(1L, Lists.newArrayList(dataManagementData), table);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public DataManagementSearchResult scanByHBase(DataManagementTableDO table,
			String startKey, String endKey, Integer rows, Integer start) {
		return daoManagementDao.scanByHBase(table, startKey, endKey, rows, start);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public DataManagementSearchResult getIndexByHBase(
			DataManagementTableDO table, String indexName, String hbaseGetKey, Integer rows, Integer start) {

		return daoManagementDao.getIndexByHBase(table, indexName, hbaseGetKey, rows, start);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public DataManagementSearchResult scanIndexByHBase(
			DataManagementTableDO table, String indexName,
			String hbaseStartValue, String hbaseEndValue, Integer rows,
			Integer start) {
		return daoManagementDao.scanIndexByHBase(table, indexName, hbaseStartValue, hbaseEndValue, rows, start);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public DataManagementSearchResult selectBySolr(DataManagementTableDO table,SolrQuery query) {
		return daoManagementDao.selectBySolr(table, query);
	}
	@SuppressWarnings("rawtypes")
	@Override
	public void deleteByHBase(DataManagementTableDO table, String key) {
		daoManagementDao.deleteByHBase(table, key);
	}
	@SuppressWarnings("rawtypes")
	@Override
	public void deleteBySolr(DataManagementTableDO table, String key) {
		daoManagementDao.deleteBySolr(table, key);
	}


}
