/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.FillType;


/**
 * 商品 DAO です。
 * @author kamiike
 *
 */
public interface ProductDao {
	
	public void disableAccessCatalog();

	/**
	 * 指定した商品の情報を返します。
	 * @param sku SKU
	 * @return 商品情報
	 */
	public ProductDO loadProduct(String sku);
	
	public ProductDO loadProductForMR(String sku);
	
	public List<ProductDO> loadProducts(List<String> skus);
	/**
	 * 指定した条件で商品情報を返します。
	 * @param sku SKU
	 * @param fillType フィルタイプ
	 * @param autoId autoId
	 * @param cartId cartId
	 * @param yatpz yatpz
	 * @param params 拡張パラメーター
	 * @return 商品情報
	 */
	public ProductDO loadProduct(
			String sku,
			FillType fillType,
			String autoId,
			String cartId,
			String yatpz,
			boolean isWithCart,
			Map<String,Object> params);
	
	public List<ProductDO> loadProducts(
			List<String> skus,
			FillType fillType,
			String autoId,
			String cartId,
			String yatpz,
			boolean isWithCart,
			Map<String,Object> params);

	/**
	 * 指定した商品の情報を返します。
	 * @param janCode JANコード
	 * @return 商品情報
	 */
	public ProductDO loadProductByJanCode(String janCode);

	/**
	 * 指定した商品情報リストを返します。
	 * @param janCodes JANコードリスト
	 * @return 商品情報リスト
	 */
	public Map<String, ProductDO> findByJanCode(Collection<String> janCodes);

	/**
	 * 指定した商品情報リストを返します。
	 * @param skus SKUリスト
	 * @return 商品情報リスト
	 */
	public Map<String, ProductDO> findBySku(Collection<String> skus);
	
	public Map<String, ProductDO> findBySkuForMR(Collection<String> skus);

	/**
	 * 指定されたキーワードで商品を検索して返します。
	 * @param keyword キーワード
	 * @param excludeSkus 除外する商品リスト
	 * @param includeCero CERO商品を含める場合、true
	 * @param includeAdult アダルト商品を含める場合、true
	 * @return 商品リスト
	 */
	public List<ProductDO> findByKeyword(
			String keyword,
			List<String> excludeSkus,
			boolean includeCero,
			boolean includeAdult);
	
	/**
	 * 指定したSKUのバリエーション商品一覧を返します。
	 * @param sku 指定のSKU
	 * @return 商品リスト
	 */
	public List<ProductDO> findVariationProductBySku(String sku);
}
