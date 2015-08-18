package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.util.List;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.solr.SolrOperations;

public class BulkUpdate<T> {
	int bulkSize = 100;
	List<T> updateRecords = null;
	Condition path = null;
	HBaseOperations hBaseOperations = null;
	SolrOperations solrOperations = null;
	Class<T> type;

	public BulkUpdate(Class<T> type, HBaseOperations hbaseOperations, SolrOperations solrOperations, Condition path, int bulkSize) {
		this.bulkSize = bulkSize;
		this.path = path;
		this.type = type;
		this.hBaseOperations = hbaseOperations;
		this.solrOperations = solrOperations;
		begin();
	}

	private void flush() {
		if (hBaseOperations != null)
			hBaseOperations.save(type, updateRecords, path);
		if (solrOperations != null)
			solrOperations.save(type, updateRecords); // Solrは全項目更新となる仕様
	}

	public void begin() {
		updateRecords = Lists.newArrayList();
	}

	public void write(T record) {
		updateRecords.add(record);
		if (updateRecords.size() > bulkSize) {
			flush();
			updateRecords.clear();
		}
	}

	public void end() {
		if (updateRecords.size() > 0) {
			flush();
		}
	}
}
