package com.kickmogu.yodobashi.community.resource.hbase2solr;

import java.util.List;

import com.google.common.collect.Lists;

public class HBase2SolrConvertContext<T,K> {
	
	protected List<T> hbaseDataList = Lists.newArrayList();

	protected List<K> hbaseDeleteKeyList = Lists.newArrayList();
	
	protected List<T> solrUpdateList = Lists.newArrayList();

	protected List<T> solrDeleteList = Lists.newArrayList();

	protected List<K> solrDeleteKeyList = Lists.newArrayList();

	public int totalHBaseDataSize() {
		int totalHBaseDataSize = 0;
		totalHBaseDataSize += hbaseDataList.size();
		totalHBaseDataSize += hbaseDeleteKeyList.size();
		return totalHBaseDataSize;
	}

	public int totalUpdateSize() {
		int totalUpdateSize = 0;
		totalUpdateSize += solrUpdateList.size();
		totalUpdateSize += solrDeleteList.size();
		totalUpdateSize += solrDeleteKeyList.size();
		return totalUpdateSize;
	}
	
	public List<K> getHbaseDeleteKeyList() {
		return hbaseDeleteKeyList;
	}

	public void setHbaseDeleteKeyList(List<K> hbaseDeleteKeyList) {
		this.hbaseDeleteKeyList = hbaseDeleteKeyList;
	}

	public List<T> getHbaseDataList() {
		return hbaseDataList;
	}

	public void setHbaseDataList(List<T> hbaseDataList) {
		this.hbaseDataList = hbaseDataList;
	}

	public List<T> getSolrUpdateList() {
		return solrUpdateList;
	}

	public void setSolrUpdateList(List<T> solrUpdateList) {
		this.solrUpdateList = solrUpdateList;
	}
	
	public void addSolrUpdateList(T object) {
		this.solrUpdateList.add(object);
	}

	public List<T> getSolrDeleteList() {
		return solrDeleteList;
	}

	public void setSolrDeleteList(List<T> solrDeleteList) {
		this.solrDeleteList = solrDeleteList;
	}
	
	public void addSolrDeleteList(T object) {
		this.solrDeleteList.add(object);
	}

	public List<K> getSolrDeleteKeyList() {
		return solrDeleteKeyList;
	}

	public void setSolrDeleteKeyList(List<K> solrDeleteKeyList) {
		this.solrDeleteKeyList = solrDeleteKeyList;
	}

	public void addSolrDeleteKeyList(K key) {
		this.solrDeleteKeyList.add(key);
	}
	
	public void clear() {
		hbaseDataList.clear();
		hbaseDeleteKeyList.clear();
		solrUpdateList.clear();
		solrDeleteList.clear();
		solrDeleteKeyList.clear();

	}
	
}
