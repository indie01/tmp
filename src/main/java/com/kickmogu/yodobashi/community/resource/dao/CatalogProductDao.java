package com.kickmogu.yodobashi.community.resource.dao;

import java.util.List;

public interface CatalogProductDao {

	/**
	 * 全商品の情報を取得し保存します。
	 */
	public void initialLoadProducts(Integer sleep, Integer groupStart, Integer groupEnd);
	
	/**
	 * 指定のSKUで商品情報を取得し保存します。
	 * @param sku
	 */
	public void initialLoadProductsBySku(String[] sku);
	
	/**
	 * 全バリエーション商品の情報を取得し保存します。
	 */
	public void initialLoadVariationProducts(Integer sleep, Integer groupStart, Integer groupEnd);
	
	/**
	 * 指定のSKUでバリエーション商品情報を取得し保存します。
	 * @param sku
	 */
	public void initialLoadVariationProductsBySku(String[] sku);
	
	/**
	 * 差分更新します。
	 */
	public void updateProducts() throws Exception;
	
	public void loadMakerMaster();
	public void loadProductBrand();
	public void loadProductsByGroup(Integer orahash);
	public void loadProductsBySku(String[] sku);
	
	public void loadVariationProductsByGroup(Integer orahash);
	public void loadVariationProductsBySku(String[] sku);
	
	public void updateInvoker(String updateType, List<?> obj) throws Exception;
	
	
}
