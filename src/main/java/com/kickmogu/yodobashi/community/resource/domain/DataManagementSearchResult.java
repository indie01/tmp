package com.kickmogu.yodobashi.community.resource.domain;

import java.util.List;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.domain.SearchResult;

@SuppressWarnings("serial")
public class DataManagementSearchResult<T> extends SearchResult<DataManagementDataDO<T>> {
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final DataManagementSearchResult EMPTY = new DataManagementSearchResult(0L, Lists.newArrayList(), null);
	
	
	@SuppressWarnings("rawtypes")
	private DataManagementTableDO table;

	@SuppressWarnings("rawtypes")
	public DataManagementSearchResult(Long numFound,
			List<DataManagementDataDO<T>> documents, DataManagementTableDO table) {
		super(numFound, documents);
		this.table = table;
	}
	
	public boolean isEmpty() {
		if (getNumFound() == null) return false;
		return getNumFound() == 0;
	}

	@SuppressWarnings("rawtypes")
	public DataManagementTableDO getTable() {
		return table;
	}

	@SuppressWarnings("rawtypes")
	public void setTable(DataManagementTableDO table) {
		this.table = table;
	}
	
	public int getDocumentSize() {
		return getDocuments().size();
	}

}
