package com.kickmogu.yodobashi.community.service.vo;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.fest.reflect.core.Reflection;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.constants.LabeledEnum;
import com.kickmogu.lib.core.resource.annotation.Label;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseIndexMeta;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseTableMeta;
import com.kickmogu.lib.solr.meta.SolrFieldMeta;
import com.kickmogu.lib.solr.meta.SolrSchemaMeta;

public class SearchManagementDataForm {

	private HBaseSearchParam hbaseSearchParam;
	
	private SolrSearchParam solrSearchParam;
	
	private Integer rows = 25;
	
	private Integer start = 1;

	public HBaseSearchParam getHbaseSearchParam() {
		return hbaseSearchParam;
	}

	public void setHbaseSearchParam(HBaseSearchParam hbaseSearchParam) {
		this.hbaseSearchParam = hbaseSearchParam;
	}

	
	public SolrSearchParam getSolrSearchParam() {
		return solrSearchParam;
	}

	public void setSolrSearchParam(SolrSearchParam solrSearchParam) {
		this.solrSearchParam = solrSearchParam;
	}

	public Integer getRows() {
		return rows;
	}

	public void setRows(Integer rows) {
		this.rows = rows;
	}

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}


	public static class SolrSearchParam {
		
		private SearchManagementDataForm form;
		
		@SuppressWarnings("rawtypes")
		private SolrSchemaMeta solrSchemaMeta;
		
		private String query;
		
		private String uniqueKey;
		
		private String orderBy;
		
		private List<SolrFieldSearchParam> fieldParams = Lists.newArrayList();

		public SolrSearchParam(SearchManagementDataForm form) {
			this.form = form;
		}
		
		public String getUniqueKeyName() {
			return solrSchemaMeta.getUniqKeyFieldMeta().getName();
		}

		@SuppressWarnings("rawtypes")
		public SolrSchemaMeta getSolrSchemaMeta() {
			return solrSchemaMeta;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void setSolrSchemaMeta(SolrSchemaMeta solrSchemaMeta) {
			List<SolrFieldMeta> fieldMetas = solrSchemaMeta.getFieldMetas();
			for (SolrFieldMeta fieldMeta:fieldMetas) {
				if (solrSchemaMeta.getUniqKeyFieldMeta().equals(fieldMeta)) continue;
				if (!fieldMeta.isIndexed()) continue;
				SolrFieldSearchParam fieldSearchParam = SolrFieldSearchParam.create(fieldMeta.getField().getType());
				fieldSearchParam.setFieldMeta(fieldMeta);
				this.fieldParams.add(fieldSearchParam);
			}
			this.solrSchemaMeta = solrSchemaMeta;
		}

		public String getQuery() {
			return query;
		}

		public void setQuery(String query) {
			this.query = query;
		}

		public String getUniqueKey() {
			return uniqueKey;
		}

		public void setUniqueKey(String uniqueKey) {
			this.uniqueKey = uniqueKey;
		}

		public List<SolrFieldSearchParam> getFieldParams() {
			return fieldParams;
		}

		public void setFieldParams(List<SolrFieldSearchParam> fieldParams) {
			this.fieldParams = fieldParams;
		}

		public String getOrderBy() {
			return orderBy;
		}

		public void setOrderBy(String orderBy) {
			this.orderBy = orderBy;
		}
		
		public SolrQuery createSolrQuery() {
			SolrQuery solrQuery = new SolrQuery();
			solrQuery.setStart(form.start-1);
			solrQuery.setRows(form.rows);
			resolveOrderBy(solrQuery);
			if (!StringUtils.isEmpty(query)) {
				solrQuery.setQuery(query);
				return solrQuery;
			}

			
			List<String> queryParts = Lists.newArrayList();
			if (!StringUtils.isEmpty(this.uniqueKey)) {
				queryParts.add(this.solrSchemaMeta.getUniqKeyFieldMeta().getName() + ":" + uniqueKey);
			}
			for (SearchManagementDataForm.SolrFieldSearchParam fieldParam:getFieldParams()) {
				String parts = fieldParam.buildQueryParts();
				if (!StringUtils.isEmpty(parts)) {
					queryParts.add(parts);
				}
			}
			
			if (queryParts.isEmpty()) {
				solrQuery.setQuery("*:*");
			} else {
				solrQuery.setQuery(StringUtils.join(queryParts, " AND "));
			}
			return solrQuery;
		}

		
		private void resolveOrderBy(SolrQuery solrQuery) {
			if (this.orderBy == null) return ;
			for (String parts:this.orderBy.split(",")) {
				String[] s = parts.split("\\ ",2);
				if (s[0].trim().equals("")) break;
				ORDER order = ORDER.asc;
				if (s.length == 2) {
					if (s[1].trim().equals("desc")) order = ORDER.desc;
					else if (s[1].trim().equals("asc")) order = ORDER.asc;
					else throw new IllegalArgumentException("ソート指定が不正です." + s[1]);
				}
				solrQuery.addSortField(s[0].trim(), order);
			}
		}

	}
	
	public abstract static class SolrFieldSearchParam {
		
		private SolrFieldMeta fieldMeta;
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		static SolrFieldSearchParam create(Class<?> type) {
			if (type.equals(String.class)) {
				return new SolrStringFieldSearchParam();
			} else if (type.equals(Date.class)) {
				return new SolrDateFieldSearchParam();
			} else if (type.equals(Integer.class) ||type.equals(int.class) ) {
				return new SolrIntegerFieldSearchParam(); 
			} else if (type.equals(Long.class) ||type.equals(long.class) ) {
				return new SolrLongFieldSearchParam(); 	
			} else if (type.equals(Double.class) ||type.equals(double.class) ) {
				return new SolrDoubleFieldSearchParam(); 	
			} else if (type.equals(Boolean.class) ||type.equals(boolean.class) ) {
				return new SolrBooleanFieldSearchParam();
			} else if (LabeledEnum.class.isAssignableFrom(type) ) {
				SolrLabeledEnumFieldSearchParam labeledEnumFieldSearchParam = new SolrLabeledEnumFieldSearchParam();
				labeledEnumFieldSearchParam.setType((Class<? extends LabeledEnum>)type);
				return labeledEnumFieldSearchParam;
			}
			
			throw new RuntimeException(type.getName());
		}
		
		public String getLabel() {
			Label labelAnnotation = fieldMeta.getField().getAnnotation(Label.class);
			if (labelAnnotation != null) {
				return labelAnnotation.value();
			} else {
				return "";
			}
		}
		
		public abstract String getTypeString();

		
		public SolrFieldMeta getFieldMeta() {
			return fieldMeta;
		}


		public void setFieldMeta(SolrFieldMeta fieldMeta) {
			this.fieldMeta = fieldMeta;
		}

		public String getFieldName() {
			return fieldMeta.getName();
		}

		public String getPropertyName() {
			return fieldMeta.getPropertyName();
		}
		public abstract void setValueFromString(String[] values);
		
		
		protected abstract String buildQueryParts();
		
	}
	
	public  static class SolrBooleanFieldSearchParam extends SolrFieldSearchParam {
		
		private Boolean value;


		public Boolean getValue() {
			return value;
		}

		public void setValue(Boolean value) {
			this.value = value;
		}

		@Override
		public String getTypeString() {
			return "Boolean";
		}

		public List<CheckingLabeledEnum> getCodeAndLabels() {
			List<CheckingLabeledEnum> list = Lists.newArrayList();
			list.add(new CheckingLabeledEnum(new BooleanLabeledEnum("true", "true"), value != null && value));
			list.add(new CheckingLabeledEnum(new BooleanLabeledEnum("false", "false"), value != null && !value));			
			return list;
		}		
		
		public static class BooleanLabeledEnum implements LabeledEnum<String, String> {
			
			private String code;
			private String label;
					
			private BooleanLabeledEnum(String code, String label) {
				this.code = code;
				this.label = label;
			}
			
			@Override
			public String getLabel() {
				return label;
			}

			@Override
			public String getCode() {
				return code;
			}

			@Override
			public String name() {
				return getLabel();
			}
			
		}

		@Override
		public void setValueFromString(String[] values) {
			if (ArrayUtils.isEmpty(values)) return;
			this.value = values[0].equals("true");
		}

		@Override
		protected String buildQueryParts() {
			if (value == null) return null;
			return this.getFieldName() + ":" + (this.value ? "true" : "false");
		}
	}	
	
	public abstract static class SolrBaseFieldSearchParam<T> extends SolrFieldSearchParam {
		
		protected T value;


		public T getValue() {
			return value;
		}

		public void setValue(T value) {
			
			this.value = value;
		}

	}

	public static class SolrStringFieldSearchParam extends SolrBaseFieldSearchParam<String> {

		@Override
		public String getTypeString() {
			return "String";
		}

		@Override
		public void setValueFromString(String[] values) {
			if (ArrayUtils.isEmpty(values)) return;
			this.value = values[0];
		}

		@Override
		protected String buildQueryParts() {
			if (StringUtils.isEmpty(this.value)) return null;
			return this.getFieldName() + ":" + this.value;
		}
	}

	public static class SolrLongFieldSearchParam extends SolrBaseFieldSearchParam<Long> {
		@Override
		public String getTypeString() {
			return "Long";
		}

		@Override
		public void setValueFromString(String[] values) {
			if (ArrayUtils.isEmpty(values)) return;
			if (StringUtils.isEmpty(values[0])) return;
			this.value = Long.valueOf(values[0]);
		}

		@Override
		protected String buildQueryParts() {
			if (value == null) return null;
			return this.getFieldName() + ":" + this.value;
		}
	}
	
	public static class SolrDoubleFieldSearchParam extends SolrBaseFieldSearchParam<Double> {
		@Override
		public String getTypeString() {
			return "Double";
		}

		@Override
		public void setValueFromString(String[] values) {
			if (ArrayUtils.isEmpty(values)) return;
			if (StringUtils.isEmpty(values[0])) return;
			this.value = Double.valueOf(values[0]);
		}

		@Override
		protected String buildQueryParts() {
			if (value == null) return null;
			return this.getFieldName() + ":" + this.value;
		}
	}	
	
	public static class SolrIntegerFieldSearchParam extends SolrBaseFieldSearchParam<Integer> {
		@Override
		public String getTypeString() {
			return "Integer";
		}

		@Override
		public void setValueFromString(String[] values) {
			if (ArrayUtils.isEmpty(values)) return;
			if (StringUtils.isEmpty(values[0])) return;
			this.value = Integer.valueOf(values[0]);
		}

		@Override
		protected String buildQueryParts() {
			if (value == null) return null;
			return this.getFieldName() + ":" + this.value;
		}
	}
	
	public interface HasRange {
		void setStartValue(String startValue);
		void setEndValue(String endValue);

	}
	
	public static class SolrDateFieldSearchParam extends SolrFieldSearchParam implements HasRange {
		
		private String value;
		
		private String startValue;

		private String endValue;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getStartValue() {
			return startValue;
		}

		public void setStartValue(String startValue) {
			this.startValue = startValue;
		}

		public String getEndValue() {
			return endValue;
		}

		public void setEndValue(String endValue) {
			this.endValue = endValue;
		}

		@Override
		public String getTypeString() {
			return "Date";
		}

		@Override
		public void setValueFromString(String[] values) {
			if (ArrayUtils.isEmpty(values)) return;
			if (StringUtils.isEmpty(values[0])) return;
			this.value = values[0];
		}

		@Override
		protected String buildQueryParts() {
			List<String> dateParts = Lists.newArrayList();

			if (!StringUtils.isEmpty(this.value)) {
				dateParts.add(this.getFieldName() + ":" + this.value);
			}
			if (!StringUtils.isEmpty(this.startValue) || !StringUtils.isEmpty(this.endValue)) {
				String myStartValue = StringUtils.isEmpty(this.startValue) ? "*" : this.startValue;
				String myEndValue = StringUtils.isEmpty(this.endValue) ? "*" : this.endValue;
				dateParts.add(this.getFieldName() + ":[" + myStartValue + " TO " + myEndValue + "]");
			}	
			if (dateParts.isEmpty()) return null;
			if (dateParts.size() == 1) {
				return dateParts.get(0);
			} else {
				return "( " + StringUtils.join(dateParts, " OR ") + " )";
			}
		}

	}
	
	@SuppressWarnings("rawtypes")
	public static class CheckingLabeledEnum implements LabeledEnum {
		
		LabeledEnum labeledEnum;
		boolean checked;
		
		CheckingLabeledEnum(LabeledEnum labeledEnum, boolean checked) {
			this.labeledEnum = labeledEnum;
			this.checked = checked;
		}

		@Override
		public String getLabel() {

			return labeledEnum.getLabel();
		}

		@Override
		public Object getCode() {
			return labeledEnum.getCode();
		}
		
		public boolean isChecked() {
			return checked;
		}

		@Override
		public String name() {
			return labeledEnum.name();
		}
		
	}
	
	
	public static class SolrLabeledEnumFieldSearchParam extends SolrFieldSearchParam {
		
		@SuppressWarnings("rawtypes")
		private Class<? extends LabeledEnum> type;
		
		@SuppressWarnings("rawtypes")
		private List<LabeledEnum> values = Lists.newArrayList();



		@Override
		public String getTypeString() {
			return "LabeledEnum";
		}

		@SuppressWarnings("rawtypes")
		public Class<? extends LabeledEnum> getType() {
			return type;
		}

		@SuppressWarnings("rawtypes")
		public void setType(Class<? extends LabeledEnum> type) {
			this.type = type;
		}
		
		@SuppressWarnings("rawtypes")
		public List<CheckingLabeledEnum> getCodeAndLabels() {
			List<CheckingLabeledEnum> result = Lists.newArrayList();
			Enum[] enums = (Enum[])Reflection.staticMethod("values").withReturnType(new LabeledEnum[]{}.getClass()).in(type).invoke();
			for (Enum e: enums) {
				boolean checked = this.values.contains(e);
				result.add(new CheckingLabeledEnum((LabeledEnum)e, checked));
			}
			return result;
		}


		@SuppressWarnings("rawtypes")
		@Override
		public void setValueFromString(String[] values) {
			if (ArrayUtils.isEmpty(values)) return;
			if (StringUtils.isEmpty(values[0])) return;
			Enum[] enums = (Enum[])Reflection.staticMethod("values").withReturnType(new LabeledEnum[]{}.getClass()).in(type).invoke();
			for (String value:values) {
				for (Enum e: enums) {
					if (((LabeledEnum)e).getCode().toString().equals(value)) {
						this.values.add((LabeledEnum)e);
					}
				}				
			}
		}

		@SuppressWarnings("rawtypes")
		@Override
		protected String buildQueryParts() {
			List<String> enumParts = Lists.newArrayList();
			for (LabeledEnum e:values) {
				enumParts.add(this.getFieldName() + ":" + e.getCode());
			}
			if (enumParts.isEmpty()) return null;
			if (enumParts.size() == 1) {
				return enumParts.get(0);
			} else {
				return "( " + StringUtils.join(enumParts, " OR ") + " )";
			}
		}
	}	

	public static class HBaseSearchParam {
		
		private String key;
		
		private String startKey;
		
		private String endKey;
		
		private HBaseTableMeta tableMeta;
		
		private List<HBaseIndexSearchParam> indexParams = Lists.newArrayList();
		
		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getPropertyName() {
			return tableMeta.getKeyMeta().getPropertyName();
		}

		public String getStartKey() {
			return startKey;
		}

		public void setStartKey(String startKey) {
			this.startKey = startKey;
		}

		public String getEndKey() {
			return endKey;
		}

		public void setEndKey(String endKey) {
			this.endKey = endKey;
		}

		public HBaseTableMeta getTableMeta() {
			return tableMeta;
		}

		public void setTableMeta(HBaseTableMeta tableMeta) {
			for (HBaseIndexMeta indexMeta:tableMeta.getIndexes()) {
				HBaseIndexSearchParam indexSearchParam = new HBaseIndexSearchParam();
				indexSearchParam.setIndexMeta(indexMeta);
				indexParams.add(indexSearchParam);
			}
			this.tableMeta = tableMeta;
		}

		public List<HBaseIndexSearchParam> getIndexParams() {
			return indexParams;
		}

		public void setIndexParams(List<HBaseIndexSearchParam> indexParams) {
			this.indexParams = indexParams;
		}
		
		

	}
	
	public static class HBaseIndexSearchParam {
		
		private HBaseIndexMeta indexMeta;
		
		private String value;
		
		private String startValue;

		private String endValue;

		public HBaseIndexMeta getIndexMeta() {
			return indexMeta;
		}

		public void setIndexMeta(HBaseIndexMeta indexMeta) {
			this.indexMeta = indexMeta;
		}

		public String getName() {
			return indexMeta.getName();
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getStartValue() {
			return startValue;
		}

		public void setStartValue(String startValue) {
			this.startValue = startValue;
		}

		public String getEndValue() {
			return endValue;
		}

		public void setEndValue(String endValue) {
			this.endValue = endValue;
		}
		
	}
	
}
