package com.kickmogu.yodobashi.community.resource.hbase2solr.impl;

import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.hbase.client.Put;

import com.google.common.collect.Lists;
import com.kickmogu.lib.hadoop.hbase.HBaseContainer;
import com.kickmogu.lib.hadoop.hbase.journal.regionserver.JournalIO;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseKeyMeta;
import com.kickmogu.lib.hadoop.hbase.service.HBaseRecoveryServiceFilter;
import com.kickmogu.lib.solr.SolrContainer;
import com.kickmogu.lib.solr.meta.SolrSchemaMeta;
import com.kickmogu.yodobashi.community.resource.hbase2solr.HBase2SolrLoader;


public class SolrRecoveryServiceFilter implements HBaseRecoveryServiceFilter {
	
	private static final int BATCH_SIZE = 100;
	

	private HBase2SolrLoader hBase2SolrLoader;


	private HBaseContainer hBaseContainer;
	

	private SolrContainer solrContainer;

	
	public void sethBase2SolrLoader(HBase2SolrLoader hBase2SolrLoader) {
		this.hBase2SolrLoader = hBase2SolrLoader;
	}

	public void sethBaseContainer(HBaseContainer hBaseContainer) {
		this.hBaseContainer = hBaseContainer;
	}

	public void setSolrContainer(SolrContainer solrContainer) {
		this.solrContainer = solrContainer;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void afterHBaseRecovery(Class<?> tableType, JournalIO.Record... records) {
		HBaseKeyMeta keyMeta = hBaseContainer.getMeta().getTableMeta(tableType).getKeyMeta();
		SolrSchemaMeta schemaMeta = solrContainer.getMeta().getSchemaMeta(tableType);
		if (schemaMeta == null) return;
		List<Put> puts = Lists.newArrayList();
		for (JournalIO.Record record:records) {
			puts.add((Put)record.getRow());
		}
		Iterator<Put> iterator = puts.iterator();
		List<Put> putList = Lists.newArrayList();

		while (iterator.hasNext()) {
			putList.add(iterator.next());
			if (putList.size() >= BATCH_SIZE) {
				recoverySolr(keyMeta, tableType, putList);
				putList.clear();
			}
		}
		if (!putList.isEmpty()) {
			recoverySolr(keyMeta, tableType, putList);
		}	
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void recoverySolr(HBaseKeyMeta keyMeta, Class<?> tableType, List<Put> putList) {
		List keyList = Lists.newArrayList();
		for (Put put:putList) {
			keyList.add(keyMeta.fromBytes(put.getRow()));
		}
		hBase2SolrLoader.loadByKeyList(tableType, keyList);
	}

}
