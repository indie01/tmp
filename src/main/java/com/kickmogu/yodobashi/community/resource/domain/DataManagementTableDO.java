package com.kickmogu.yodobashi.community.resource.domain;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kickmogu.lib.core.resource.annotation.Label;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseColumnMetaBase;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseKeyMeta;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseTableMeta;
import com.kickmogu.lib.solr.meta.SolrPropertyMetaBase;
import com.kickmogu.lib.solr.meta.SolrSchemaMeta;

public class DataManagementTableDO<T> {
	
	private HBaseTableMeta hBaseTableMeta;
	
	private SolrSchemaMeta<T> solrSchemaMeta;

	private Class<T> domainObjectType;

	public HBaseTableMeta gethBaseTableMeta() {
		return hBaseTableMeta;
	}
	
	public boolean hbaseKeyFieldEqualsToSolrUniqueField() {
		if (solrSchemaMeta == null) return false;
		return hBaseTableMeta.getKeyMeta().getField().equals(solrSchemaMeta.getUniqKeyFieldMeta().getField());
	}

	public void sethBaseTableMeta(HBaseTableMeta hBaseTableMeta) {
		this.hBaseTableMeta = hBaseTableMeta;
	}

	public SolrSchemaMeta<T> getSolrSchemaMeta() {
		return solrSchemaMeta;
	}

	public void setSolrSchemaMeta(SolrSchemaMeta<T> solrSchemaMeta) {
		this.solrSchemaMeta = solrSchemaMeta;
	}

	public Class<T> getDomainObjectType() {
		return domainObjectType;
	}

	public void setDomainObjectType(Class<T> domainObjectType) {
		this.domainObjectType = domainObjectType;
	}
	
	public String getDoName() {
		return domainObjectType.getSimpleName();
	}
	
	public boolean hasHBase() {
		return this.hBaseTableMeta != null;
	}

	public boolean hasSolr() {
		return this.solrSchemaMeta != null;
	}
	
	@SuppressWarnings("rawtypes")
	public List<DataManagementFieldDO> getFields() {
		List<DataManagementFieldDO> fields = Lists.newArrayList();
		Map<Field, DataManagementFieldDO> fieldMap = Maps.newHashMap();
		
		if (hBaseTableMeta != null) {
			DataManagementFieldDO dataManagementKeyField = new DataManagementFieldDO();
			dataManagementKeyField.hBaseKeyMeta = hBaseTableMeta.getKeyMeta();
			dataManagementKeyField.field = hBaseTableMeta.getKeyMeta().getField();
			fields.add(dataManagementKeyField);
			fieldMap.put(dataManagementKeyField.field, dataManagementKeyField);
			
			for (HBaseColumnMetaBase hbaseColumnMeta: hBaseTableMeta.getAllColumnMeta()) {
				DataManagementFieldDO dataManagementField = new DataManagementFieldDO();
				dataManagementField.field = hbaseColumnMeta.getField();
				dataManagementField.hBaseColumnMeta = hbaseColumnMeta;
				fieldMap.put(dataManagementField.field, dataManagementField);
				fields.add(dataManagementField);
			}
		}
		if (solrSchemaMeta != null) for (SolrPropertyMetaBase solrFieldMeta:solrSchemaMeta.getAllFieldMetas()) {
			Field field = solrFieldMeta.getField();
			if (fieldMap.containsKey(field)) {
				fieldMap.get(field).solrFieldMeta = solrFieldMeta;
			} else {
				DataManagementFieldDO dataManagementField = new DataManagementFieldDO();
				dataManagementField.field = field;
				dataManagementField.solrFieldMeta = solrFieldMeta;
				fields.add(dataManagementField);
			}
		}
		
		
		return fields;
	}
	
	public static class DataManagementFieldDO {
		
		private Field field;
		
		private HBaseColumnMetaBase hBaseColumnMeta;
		
		@SuppressWarnings("rawtypes")
		private SolrPropertyMetaBase solrFieldMeta;
		
		private HBaseKeyMeta hBaseKeyMeta;
		

		public Field getField() {
			return field;
		}

		public HBaseColumnMetaBase gethBaseColumnMeta() {
			return hBaseColumnMeta;
		}

		@SuppressWarnings("rawtypes")
		public SolrPropertyMetaBase getSolrFieldMeta() {
			return solrFieldMeta;
		}
		
		public String getFieldName() {
			return field.getName();
		}

		public HBaseKeyMeta gethBaseKeyMeta() {
			return hBaseKeyMeta;
		}
		
		public String getLabel() {
			Label labelAnnotation = field.getAnnotation(Label.class);
			if (labelAnnotation != null) {
				return labelAnnotation.value();
			} else {
				return "";
			}
		}

	}
	
}

