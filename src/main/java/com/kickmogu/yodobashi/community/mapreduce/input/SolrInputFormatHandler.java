package com.kickmogu.yodobashi.community.mapreduce.input;

import com.kickmogu.lib.core.domain.SearchResult;

public interface SolrInputFormatHandler<V> {
	
	/**
	 * Solrの検索直後に呼ばれるハンドラです。
	 * @param searchResult 検索結果（一部）
	 */
	public void handlePostSolrQuery(SearchResult<V> searchResult);

}
