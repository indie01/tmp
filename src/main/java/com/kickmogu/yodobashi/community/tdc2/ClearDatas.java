package com.kickmogu.yodobashi.community.tdc2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import com.kickmogu.lib.hadoop.hbase.HBaseContainer;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseTableMeta;
import com.kickmogu.lib.solr.SolrContainer;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.lib.solr.meta.SolrSchemaMeta;

public class ClearDatas {
	
	@Autowired @Qualifier("MySite")
	SolrContainer solrContainer;
	
	@Autowired @Qualifier("default")
	SolrOperations solrOperations;

	@Autowired @Qualifier("MySite")
	HBaseContainer hbaseContainer;
	
	@Autowired @Qualifier("default")
	HBaseOperations hbaseOperations;

	void clear() {
		for (HBaseTableMeta hbaseTableMeta:hbaseContainer.getMeta().getAllTableMeta()) {
			if (hbaseTableMeta.isExternalEntity()) continue;
			hbaseOperations.physicalDeleteAll(hbaseTableMeta.getType());
		}
		
		for (@SuppressWarnings("rawtypes") SolrSchemaMeta solrSchemaMeta:solrContainer.getMeta().getAllSchemaMeta()) {
			if (solrSchemaMeta.isExternalEntity()) continue;
			solrOperations.deleteAll(solrSchemaMeta.getType());
			solrOperations.optimize(solrSchemaMeta.getType());
		}
	}
	
	public static void main(String[] args) {
		ClearDatas clearDatas = new ClearDatas();
		DataCreator.getApplicationContext().getAutowireCapableBeanFactory().autowireBeanProperties(clearDatas,
				AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
		clearDatas.clear();
		System.exit(0);
	}

}
