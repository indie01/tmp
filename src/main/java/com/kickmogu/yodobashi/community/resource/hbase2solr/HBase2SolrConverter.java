package com.kickmogu.yodobashi.community.resource.hbase2solr;

import java.util.List;

public interface HBase2SolrConverter<T,K> {
	int getBulkSize();
	void setType(Class<T> type);
	Class<T> getType();
	String[] getLoadHBasePropertyNames();
	void convert(List<T> hbaseDataList, HBase2SolrConvertContext<T,K> convertContext);
	void convertDeleted(List<K> hbaseDeleteKeyList, HBase2SolrConvertContext<T,K> convertContext);
	void setBulkSize(int bulkSize);
}
