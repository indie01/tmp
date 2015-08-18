/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.axis.utils.StringUtils;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.resource.dao.DecisivePurchaseDao;
import com.kickmogu.yodobashi.community.resource.dao.util.SolrUtil;
import com.kickmogu.yodobashi.community.resource.domain.DecisivePurchaseDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDecisivePurchaseDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;

/**
 * 非同期メッセージ DAO です。
 * @author kamiike
 *
 */
@Service
public class DecisivePurchaseDaoImpl implements DecisivePurchaseDao {

	/**
	 * HBaseアクセサです。
	 */
	@Autowired  @Qualifier("default")
	private HBaseOperations hBaseOperations;

	/**
	 * HBaseアクセサです。
	 */
	@Autowired  @Qualifier("default")
	private SolrOperations solrOperations;

	/**
	 * タイムスタンプホルダーです。
	 */
	@Autowired
	private TimestampHolder timestampHolder;

	@Override
	public DecisivePurchaseDO loadDecisivePurchase(String decisivePurchaseId) {
		return hBaseOperations.load(DecisivePurchaseDO.class, decisivePurchaseId);
	}

	@Override
	public boolean isExistDecisivePurchase(String sku, String decisivePurchaseName, String exclusiveId){
		if( null == sku || null == decisivePurchaseName ){
			throw new NullPointerException();
		}
		if( 18 != sku.length() ){
			throw new IllegalArgumentException();
		}
		List<DecisivePurchaseDO> searchResult = hBaseOperations.scan(
				DecisivePurchaseDO.class,
				sku,
				hBaseOperations.createFilterBuilder(DecisivePurchaseDO.class)
					.appendSingleColumnValueFilter("decisivePurchaseName", CompareOp.EQUAL, decisivePurchaseName)
					.toFilter()
		);
		// 更新対象を除外
		if( null != exclusiveId ){
			for( int i = 0; i < searchResult.size(); i++ ){
				if( exclusiveId.equals(searchResult.get(i).getDecisivePurchaseId()) ){
					searchResult.remove(i);
				}
			}
		}
		return 0 < searchResult.size();
	}
	
	@ArroundHBase
	@ArroundSolr
	@Override
	public void modifyDecisivePurchase(String decisivePurchaseId, String decisivePurchaseName){
		DecisivePurchaseDO decisivePurchase = loadDecisivePurchase(decisivePurchaseId);
		decisivePurchase.setDecisivePurchaseName(decisivePurchaseName);
		decisivePurchase.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(decisivePurchase);
		solrOperations.save(decisivePurchase);
	}

	@ArroundHBase
	@ArroundSolr
	@Override
	public void removeDecisivePurchase(String decisivePurchaseId) {
		DecisivePurchaseDO decisivePurchase = loadDecisivePurchase(decisivePurchaseId);
		decisivePurchase.setDeleteFlg(true);
		decisivePurchase.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(decisivePurchase);
		solrOperations.save(decisivePurchase);
		
		SolrQuery query = new SolrQuery("decisivePurchaseId_s:" + decisivePurchaseId);
		for(ReviewDecisivePurchaseDO reviewDecisivePurchase:solrOperations.findByQuery(query, ReviewDecisivePurchaseDO.class,Path.includeProp("*")).getDocuments()){
			reviewDecisivePurchase.setDeleteFlag(true);
			reviewDecisivePurchase.setModifyDateTime(timestampHolder.getTimestamp());
			hBaseOperations.save(reviewDecisivePurchase, Path.includeProp("deleteFlag,modifyDateTime"));
			solrOperations.deleteByKey(ReviewDecisivePurchaseDO.class, reviewDecisivePurchase.getReviewDecisivePurchaseId());
		}
	}


	@ArroundHBase
	@ArroundSolr
	@Override
	public void checkDecisivePurchase(String decisivePurchaseId, boolean check) {
		DecisivePurchaseDO decisivePurchase = loadDecisivePurchase(decisivePurchaseId);
		decisivePurchase.setCheckFlg(check);
		decisivePurchase.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(decisivePurchase);
		solrOperations.save(decisivePurchase);
	}


	@Override
	public SearchResult<DecisivePurchaseDO> findDecisivePurchase(
			String name,
			String sku,
			boolean delete,
			Boolean check,
			Boolean skuSort,
			Boolean modifyDateTimeSort,
			int limit,
			int offset)
	{


		StringBuilder buffer = new StringBuilder();
		if( !StringUtils.isEmpty(name) ){	
			buffer.append("decisivePurchaseName_s:");
			buffer.append("*").append(SolrUtil.escape(name)).append("*");
		}
		if( !StringUtils.isEmpty(sku) ){
			if( buffer.length() > 0 ){
				buffer.append(" AND ");
			}
			buffer.append("sku_s:").append(SolrUtil.escape(sku));
		}
		if( buffer.length() > 0 ){
			buffer.append(" AND ");
		}
		buffer.append("deleteFlg_b:");
		buffer.append(delete);
		if( null != check ){
			if( buffer.length() > 0 ){
				buffer.append(" AND ");
			}
			if(check){
				buffer.append("checkFlg_b:true");
			}else{
				buffer.append("-checkFlg_b:true");
			}
		}

		SolrQuery sq = new SolrQuery(buffer.toString()).setRows(limit).setStart(offset);
		if( null != skuSort ){
			sq.addSortField("sku_s", (skuSort)? ORDER.asc : ORDER.desc);
		}
		if( null != modifyDateTimeSort ){
			sq.addSortField("modifyDateTime_dt", (modifyDateTimeSort)? ORDER.asc : ORDER.desc);
		}
		sq.addSortField("decisivePurchaseId", ORDER.asc);
		
		SearchResult<DecisivePurchaseDO> results = new SearchResult<DecisivePurchaseDO>(
				solrOperations.findByQuery(sq, DecisivePurchaseDO.class, Path.includeProp("decisivePurchaseId")));
		List<DecisivePurchaseDO> decisivePurchases = new ArrayList<DecisivePurchaseDO>();
		for( DecisivePurchaseDO decisivePurchase : results.getDocuments() ){
			DecisivePurchaseDO hbaseDecisivePurchase = hBaseOperations.load(DecisivePurchaseDO.class, decisivePurchase.getDecisivePurchaseId(), Path.includeProp("*"));
			decisivePurchases.add( hbaseDecisivePurchase );
		}

		return new SearchResult<DecisivePurchaseDO>( results.getNumFound(), decisivePurchases );

	}

}
