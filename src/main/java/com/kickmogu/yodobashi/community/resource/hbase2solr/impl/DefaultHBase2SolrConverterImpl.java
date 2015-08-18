package com.kickmogu.yodobashi.community.resource.hbase2solr.impl;

import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.kickmogu.lib.hadoop.hbase.meta.HBaseMeta;
import com.kickmogu.lib.solr.meta.SolrMeta;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.DecisivePurchaseDO;
import com.kickmogu.yodobashi.community.resource.domain.EcCustomerStatusDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.PointIncentiveDO;
import com.kickmogu.yodobashi.community.resource.domain.PointIncentiveDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseLostProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.ReceiptDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.ReceiptHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.RemoveContentsDO;
import com.kickmogu.yodobashi.community.resource.domain.RemoveContentsDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDecisivePurchaseDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.SkuCodeNotFoundDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.SpamReportDO;
import com.kickmogu.yodobashi.community.resource.domain.UniqueUserViewCountDO;
import com.kickmogu.yodobashi.community.resource.domain.UsedProductDO;
import com.kickmogu.yodobashi.community.resource.hbase2solr.HBase2SolrConvertContext;
import com.kickmogu.yodobashi.community.resource.hbase2solr.HBase2SolrConverter;
import com.kickmogu.yodobashi.community.resource.hbase2solr.annotation.HBase2Solr;

@HBase2Solr.List({
	@HBase2Solr(value={
		ImageHeaderDO.class,
		PurchaseProductDO.class,
		QuestionDO.class,
		QuestionAnswerDO.class,
		SpamReportDO.class,
		CommentDO.class,
		ReviewDO.class,
		ReviewHistoryDO.class,
		PurchaseLostProductDO.class,
		ReviewDecisivePurchaseDO.class,
		UsedProductDO.class,
		SlipHeaderDO.class,
		SlipDetailDO.class,
		EcCustomerStatusDO.class,
		ReceiptHeaderDO.class,
		ReceiptDetailDO.class,
		CommunityUserDO.class,
		PointIncentiveDO.class,
		PointIncentiveDetailDO.class,
		SkuCodeNotFoundDO.class,
		UniqueUserViewCountDO.class,
//		PurchaseProductDetailDO.class,
		DecisivePurchaseDO.class,
		RemoveContentsDO.class,
		RemoveContentsDetailDO.class

	},bulkSize=1000)
})
public class DefaultHBase2SolrConverterImpl<T,K>  implements HBase2SolrConverter<T,K>, InitializingBean {

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


	@Override
	public void convert(List<T> hbaseDataList, HBase2SolrConvertContext<T, K> convertContext) {
		convertContext.getSolrUpdateList().addAll(hbaseDataList);
	}


	@Override
	public void convertDeleted(List<K> hbaseDeleteKeyList,
			HBase2SolrConvertContext<T, K> convertContext) {
		convertContext.getSolrDeleteKeyList().addAll(hbaseDeleteKeyList);
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		loadHBasePropertyNames = HBase2SolrConverterUtil.getLoadHBasePropertyNames(type, hbaseMeta, solrMeta);
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
