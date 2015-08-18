package com.kickmogu.yodobashi.community.resource.domain;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kickmogu.lib.core.constants.LabeledEnum;
import com.kickmogu.lib.core.exception.CommonSystemException;
import com.kickmogu.lib.core.resource.TableMeta;
import com.kickmogu.lib.core.utils.StringUtil;
import com.kickmogu.lib.core.utils.ThreadSafeDateFormat;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseTableMeta;
import com.kickmogu.lib.solr.meta.SolrSchemaMeta;
import com.kickmogu.yodobashi.community.resource.domain.DataManagementTableDO.DataManagementFieldDO;

public class DataManagementDataDO<T> {
	
	private Map<String, DataManagementSiteData<T>> siteDataMap = Maps.newLinkedHashMap();
	
	@SuppressWarnings("rawtypes")
	private DataManagementTableDO table;
	
	@SuppressWarnings("rawtypes")
	public DataManagementDataDO(DataManagementTableDO table) {
		this.table = table;
	}
	
	public Map<String, DataManagementSiteData<T>> getSiteDataMap() {
		return siteDataMap;
	}
	
	public Collection<DataManagementSiteData<T>> getSiteDatas() {
		return siteDataMap.values();
	}

	public void setSiteDataMap(Map<String, DataManagementSiteData<T>> siteDataMap) {
		this.siteDataMap = siteDataMap;
	}

	@SuppressWarnings("rawtypes")
	public DataManagementTableDO getTable() {
		return table;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void appendSiteData(String site, Object domainObject, TableMeta tableMeta) {
		siteDataMap.put(site, new DataManagementSiteData(site, domainObject, table, tableMeta));	
	}
	
	public static class DataManagementSiteData<T> {
		
		
		@SuppressWarnings({ "rawtypes", "unused" })
		private DataManagementTableDO table;
		
		@SuppressWarnings("unused")
		private TableMeta tableMeta;
		
		private String site;
		
		private T domainObject;
		
		private Map<Field, DataManagementFieldValueDO> fieldValueMap = Maps.newLinkedHashMap();
		
		public DataManagementSiteData(String site, T domainObject, DataManagementTableDO<T> table, TableMeta tableMeta) {
			this.site = site;
			this.domainObject = domainObject;
			this.tableMeta = tableMeta;
			for (DataManagementFieldDO fieldDO:table.getFields()) {
				fieldValueMap.put(fieldDO.getField(), new DataManagementFieldValueDO(fieldDO, domainObject, tableMeta));
			}
		}
		
		public String getSite() {
			return site;
		}

		public void setSite(String site) {
			this.site = site;
		}

		public T getDomainObject() {
			return domainObject;
		}

		public void setDomainObject(T domainObject) {
			this.domainObject = domainObject;
		}
		
		public Collection<DataManagementFieldValueDO> getFieldValues() {
			return fieldValueMap.values();
		}
		
		public static class DataManagementFieldValueDO {
			
			private DataManagementFieldDO dataManagementField;
			
			private Object domainObject;
			
			private TableMeta tableMeta;
			
			public DataManagementFieldValueDO(DataManagementFieldDO dataManagementField, Object domainObject, TableMeta tableMeta) {
				this.dataManagementField =dataManagementField;
				this.domainObject = domainObject;
				this.tableMeta = tableMeta;
			}

			public String getValue() {
				Object value = null;
				try {
					value =  dataManagementField.getField().get(domainObject);
				} catch (Throwable e) {
					//throw new CommonSystemException(e);
					return "Error!";
				}
				return value.toString();
			}

			@SuppressWarnings({ "rawtypes", "unchecked" })
			public String getShortValue() {
				
				if (!tableMeta.isPersistant(dataManagementField.getField())) {
					return "管理対象外";
				}

				if (domainObject == null) {
					return tableMeta.isKey(dataManagementField.getField()) ?  "データ無し" : "-";
				}
				
				Object value = null;
				try {
					value =  dataManagementField.getField().get(domainObject);
				} catch (Throwable e) {
					throw new CommonSystemException(e);
				}
				
				if (value == null) return "NULL";
				
				if (tableMeta.isForeignKey(dataManagementField.getField())) {
					if (tableMeta instanceof HBaseTableMeta) {
						HBaseTableMeta hBaseTableMeta = (HBaseTableMeta)tableMeta;
						return hBaseTableMeta.getHBaseMeta().getTableMeta(value.getClass()).getKey(value).toString();
					} else if (tableMeta instanceof SolrSchemaMeta) {
						SolrSchemaMeta solrSchemaMeta = (SolrSchemaMeta)tableMeta;
						return solrSchemaMeta.getMeta().getSchemaMeta(value.getClass()).getKey(value).toString();
					}
				}
				
				if (LabeledEnum.class.isAssignableFrom(value.getClass())) {
					LabeledEnum e = (LabeledEnum)value;
					return e.getLabel() + "(" + e.getCode() + ")";
				} else if (Date.class.isAssignableFrom(value.getClass())) {
					return ThreadSafeDateFormat.format("yyyy/MM/dd-HH:mm:ss", (Date)value);
				} else if (byte[].class.isAssignableFrom(value.getClass())) {
					return "byte配列長=" + ((byte[])value).length + " hashCode=" + new HashCodeBuilder().append((byte[])value).toHashCode();
				} else if (List.class.isAssignableFrom(value.getClass())) {
					List strList = Lists.newArrayList();
					for(Object o:((List)value)) {
						strList.add(ToStringBuilder.reflectionToString(o, ToStringStyle.SHORT_PREFIX_STYLE));
					}
					return StringUtils.join(strList,",");
				}
				
				return StringUtil.chop(value.toString(), 40);
			}
			
		}

	}

	
}
