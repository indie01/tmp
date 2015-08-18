package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.dao.VariationProductDao;
import com.kickmogu.yodobashi.community.resource.dao.util.SolrUtil;
import com.kickmogu.yodobashi.community.resource.domain.DBVariationProductDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;

@Service
public class VariationProductDaoImpl implements VariationProductDao {
	
	/**
	 * Solrアクセサです。
	 */
	@Autowired @Qualifier("default")
	protected SolrOperations solrOperations;

	@Override
	@PerformanceTest(type = Type.UPDATE, frequency = Frequency.NONE, frequencyComment = "IDを払い出すだけなのでテスト対象外")
	public Set<String> findVariationProduct(String sku, String targetDateTime) {
		Set<String> result = Sets.newHashSet();
		
		String now = (targetDateTime == null)? "NOW":targetDateTime;
		
		StringBuilder buffer = new StringBuilder();
		buffer.append("sku_s:" + SolrUtil.escape(sku));
		buffer.append(" AND startTime_dt:[* TO " + now + "] AND endTime_dt:[" + now + " TO *]");
		SolrQuery query = new SolrQuery(buffer.toString());

		SearchResult<DBVariationProductDO> searchResult = new SearchResult<DBVariationProductDO>(
				solrOperations.findByQuery(query, DBVariationProductDO.class,
						Path.includeProp("*")));
		
		if( searchResult.getDocuments().size() <= 0 )
			return result;
		
		Iterator<DBVariationProductDO> iterator = searchResult.getDocuments().iterator();
		DBVariationProductDO variationProductDO = null;
		List<String> relatedSkus = null;
		while (iterator.hasNext()) {
			variationProductDO = iterator.next();
			relatedSkus = variationProductDO.getVariationProducts() ;
			if( relatedSkus == null || relatedSkus.isEmpty() )
				continue;
			
			for(String relatedSku : relatedSkus ){
				result.add(relatedSku);
			}
		}
		
		return result;
	}

}
