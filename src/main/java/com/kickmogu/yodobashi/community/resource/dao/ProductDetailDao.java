/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import java.util.List;
import java.util.Map;

/**
 * 商品詳細情報を直接読み出すための DAO です。
 * @author kamiike
 *
 */
public interface ProductDetailDao {

	/**
	 * SKUマップを返します。
	 * @param janCodes JANコードリスト
	 * @return SKUマップ（key=janCode、value={sku, productType, adultKind}）
	 */
	public Map<String, String[]> loadSkuMap(List<String> janCodes);
}
