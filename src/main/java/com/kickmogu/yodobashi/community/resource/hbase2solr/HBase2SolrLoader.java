package com.kickmogu.yodobashi.community.resource.hbase2solr;

import java.util.List;

public interface HBase2SolrLoader {
	@SuppressWarnings("rawtypes")
	<T> void loadByKeyList(Class<T> type, List keyList);
	<T> void loadByKey(Class<T> type, Object key);
	<T> void loadByKeyRange(Class<T> type, Object startKey, Object endKey);
	<T> void loadByHBaseObjects(Class<T> type, List<T> objects);
}
