package com.kickmogu.yodobashi.community.resource.hbase2solr.impl;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseMeta;
import com.kickmogu.lib.solr.meta.SolrMeta;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.SolrVisible;
import com.kickmogu.yodobashi.community.resource.domain.VotingDO;
import com.kickmogu.yodobashi.community.resource.hbase2solr.HBase2SolrConvertContext;
import com.kickmogu.yodobashi.community.resource.hbase2solr.HBase2SolrConverter;
import com.kickmogu.yodobashi.community.resource.hbase2solr.annotation.HBase2Solr;

@HBase2Solr.List({
	@HBase2Solr(value={
			InformationDO.class,
			QuestionFollowDO.class,
			ActionHistoryDO.class,
			CommunityUserFollowDO.class,
			LikeDO.class,
			VotingDO.class,
			ProductFollowDO.class,
			ProductMasterDO.class

	},bulkSize=1000)
})
public class DefaultVisibilityHBase2SolrConverterImpl<T extends SolrVisible, K>  implements HBase2SolrConverter<T,K>, InitializingBean {

	private Class<T> type;

	@Autowired
	private HBaseMeta hbaseMeta;

	@Autowired
	private SolrMeta solrMeta;

	private String[] loadHBasePropertyNames;

	private int bulkSize;

	@Override
	public void setType(Class<T> type) {
		this.type = type;
	}

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public String[] getLoadHBasePropertyNames() {
		return loadHBasePropertyNames;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void convert(List<T> hbaseDataList, HBase2SolrConvertContext<T, K> convertContext) {
		for (SolrVisible object:hbaseDataList) {
			if (object.visible()) {
				convertContext.addSolrUpdateList((T)object);
			} else {
				convertContext.addSolrDeleteList((T)object);
			}
		}
	}

	@Override
	public void convertDeleted(List<K> hbaseDeleteKeyList,
			HBase2SolrConvertContext<T, K> convertContext) {
		convertContext.getSolrDeleteKeyList().addAll(hbaseDeleteKeyList);
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		List<String> loadHBasePropertyNameList = Lists.newArrayList(Arrays.asList(HBase2SolrConverterUtil.getLoadHBasePropertyNames(type, hbaseMeta, solrMeta)).iterator());
		for (String propertyName:type.newInstance().getHintPropertyNames()) {
			loadHBasePropertyNameList.add(propertyName);
		}
		loadHBasePropertyNames = loadHBasePropertyNameList.toArray(new String[loadHBasePropertyNameList.size()]);
	}

	@Override
	public int getBulkSize() {
		return bulkSize;
	}

	@Override
	public void setBulkSize(int bulkSize) {
		this.bulkSize = bulkSize;
	}
}
