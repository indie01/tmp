package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DataManagementDO {

	@SuppressWarnings("rawtypes")
	private Map<Class, DataManagementTableDO> tableMap = Maps.newHashMap();

	@SuppressWarnings("rawtypes")
	public Map<Class, DataManagementTableDO> getTableMap() {
		return tableMap;
	}

	@SuppressWarnings("rawtypes")
	public void setTableMap(Map<Class, DataManagementTableDO> tableMap) {
		this.tableMap = tableMap;
	}

	@SuppressWarnings("rawtypes")
	public Collection<DataManagementTableDO> getTables() {
		List<DataManagementTableDO> result = Lists.newArrayList(tableMap.values().iterator());
		Collections.sort(result, new Comparator<DataManagementTableDO>() {
			@Override
			public int compare(DataManagementTableDO o1, DataManagementTableDO o2) {
				return o1.getDoName().compareTo(o2.getDoName());
			}
		});
		return result;
	}

}
