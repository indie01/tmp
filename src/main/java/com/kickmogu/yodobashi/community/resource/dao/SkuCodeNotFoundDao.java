/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import java.util.List;

import com.kickmogu.yodobashi.community.resource.domain.SkuCodeNotFoundDO;

/**
 * SKU 変換エラー DAO です。
 * @author kamiike
 *
 */
public interface SkuCodeNotFoundDao {

	/**
	 * SKU 変換エラーをインデックスと一緒に生成します。
	 * @param skuCodeNotFound SKU 変換エラー
	 */

	
	public void createListWithIndex(List<SkuCodeNotFoundDO> skuCodeNotFounds);
}
