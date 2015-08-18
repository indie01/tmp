package com.kickmogu.yodobashi.community.resource.dao;

import java.util.Collection;
import java.util.Map;

import com.kickmogu.yodobashi.community.resource.domain.ProductDO;

public interface CommunityLocalProductDao {

	/**
	 * 商品を返します。(Solr検索)
	 * 
	 * @param sku SKU
	 * @return 商品
	 */
	public abstract Map<String, ProductDO> findProductBySku(Collection<String> skus);

	/**
	 * 商品を返します。(Solr検索)
	 * 
	 * @param skus SKUリスト
	 * @param targetDateTime 対象日時(yyyy-MM-dd'T'hh:mm:dd.SSS'Z')
	 * @return 商品
	 */
	public abstract Map<String, ProductDO> findProductBySku(Collection<String> skus, String targetDateTime);

	/**
	 * 商品を返します。(Solr検索)
	 * 
	 * @param janCodes janCodeリスト
	 * @return 商品
	 */
	public abstract Map<String, ProductDO> findProductByJanCode(Collection<String> janCodes);

	/**
	 * 商品を返します。(Solr検索)
	 * 
	 * @param janCodes janCodeリスト
	 * @param targetDateTime 対象日時(yyyy-MM-dd'T'hh:mm:dd.SSS'Z')
	 * @return 商品
	 */
	public abstract Map<String, ProductDO> findProductByJanCode(Collection<String> janCodes, String targetDateTime);

}