/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kickmogu.lib.core.domain.SearchResult;
import com.kickmogu.lib.core.resource.ExternalEntityOperations;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.lib.core.utils.ListUtil;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.common.exception.CatalogAccessException;
import com.kickmogu.yodobashi.community.common.utils.EnvUtil;
import com.kickmogu.yodobashi.community.resource.cache.CacheContents;
import com.kickmogu.yodobashi.community.resource.cache.MethodCacheFilter;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.dao.CommunityLocalProductDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.util.SolrUtil;
import com.kickmogu.yodobashi.community.resource.domain.DBVariationProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.FillType;


/**
 * 商品 DAO の実装です。
 * @author kamiike
 *
 */
@Service("productDao") @Qualifier("catalog")
public class ProductDaoImpl implements ProductDao, ExternalEntityOperations<ProductDO, String> {

	private static final int DEFAULT_CACHE_SIZE = 1000;
	private static final int DEFAULT_CACHE_PERIOD = 5*60*1000;
	private static final int MR_CACHE_SIZE = 50000;
	private static final int MR_CACHE_PERIOD = 24*60*60*1000;

	private Cache cache = new Cache(DEFAULT_CACHE_SIZE);
	private Cache mrCache = new Cache(MR_CACHE_SIZE);

	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ProductDaoImpl.class);

	/**
	 * リソースコンフィグです。
	 */
	@Autowired
	private ResourceConfig resourceConfig;

	/**
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	private RequestScopeDao requestScopeDao;

	/**
	 * ダミー実装の ProductDao です。
	 */
	@Autowired @Qualifier("dummy")
	private ProductDao dummyProductDao;

	/**
	 * ダミー実装を使用するかどうかです。
	 */
	@Value("${use.dummyProductDaoImpl}")
	private boolean useDummy;
	
	/**
	 * Solrアクセサです。
	 */
	@Autowired
	@Qualifier("default")
	private SolrOperations solrOperations;
	
	@Autowired
	private CommunityLocalProductDao communityLocalProductDao;

	private boolean disableAccessCatalog = false;

	@Override
	public void disableAccessCatalog() {
		disableAccessCatalog = true;
	}	
	/**
	 * 指定した商品の情報を返します。
	 * @param sku SKU
	 * @return 商品情報
	 */
	@Override
	public ProductDO loadProduct(String sku) {
		String[] catalogCookies = requestScopeDao.getCatalogCookies();
		return loadProduct(sku, FillType.SMALL,
				catalogCookies[0],
				catalogCookies[1],
				catalogCookies[2],
				false,
				null);
	}

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
	@Override
	public ProductDO loadProduct(
			String sku,
			FillType fillType,
			String autoId,
			String cartId,
			String yatpz,
			boolean isWithCart,
			Map<String,Object> params) {
		if (useDummy) {
			return dummyProductDao.loadProduct(sku, fillType, autoId, cartId, yatpz, isWithCart, params);
		}
		List<String> skus = new ArrayList<String>();

		boolean cacheable = StringUtils.isEmpty(autoId) && StringUtils.isEmpty(cartId) && StringUtils.isEmpty(yatpz) && !isWithCart && (params == null || params.isEmpty());
		if (cacheable) {
			CacheContents cacheContents = cache.get(fillType, CacheKeyType.SKU, sku);
			if (cacheContents == null) {
			} else if (System.currentTimeMillis() - cacheContents.getStartTime() < DEFAULT_CACHE_PERIOD) {
				cacheContents.incrementHitCount();
				return (ProductDO)cacheContents.getContents();
			} else if (cacheContents.isStartReload() && System.currentTimeMillis() - cacheContents.getStartReloadingTime() < 30*1000) {
				cacheContents.incrementHitCount();
				return (ProductDO)cacheContents.getContents();
			} else {
				cacheContents.startReload();
			}
		}

		skus.add(sku);
		List<ProductDO> products = findProduct(skus, null, fillType, autoId, cartId, yatpz, isWithCart, params);
		if (products != null && products.size() > 0) {
			if (cacheable) {
				cache.put(fillType, products.get(0));
			}
			return products.get(0);
		} else {
			return null;
		}
	}

	
	@Override
	public List<ProductDO> loadProducts(List<String> skus) {
		String[] catalogCookies = requestScopeDao.getCatalogCookies();
		return loadProducts(
				skus,
				FillType.SMALL,
				catalogCookies[0],
				catalogCookies[1],
				catalogCookies[2],
				false,
				null);
	}
	
	@Override
	public List<ProductDO> loadProducts(
			List<String> skus,
			FillType fillType,
			String autoId,
			String cartId,
			String yatpz,
			boolean isWithCart,
			Map<String, Object> params) {
		if (useDummy) {
			return dummyProductDao.loadProducts(skus, fillType, autoId, cartId, yatpz, isWithCart, params);
		}
		
		List<ProductDO> results = new ArrayList<ProductDO>();
		List<String> loadSkus = new ArrayList<String>();
		boolean cacheable = StringUtils.isEmpty(autoId) && StringUtils.isEmpty(cartId) && StringUtils.isEmpty(yatpz) && !isWithCart && (params == null || params.isEmpty());
		if (cacheable) {
			for( String sku : skus){
				CacheContents cacheContents = cache.get(fillType, CacheKeyType.SKU, sku);
				if (cacheContents == null) {
					loadSkus.add(sku);
				} else if (System.currentTimeMillis() - cacheContents.getStartTime() < DEFAULT_CACHE_PERIOD) {
					cacheContents.incrementHitCount();
					results.add((ProductDO)cacheContents.getContents());
				} else if (cacheContents.isStartReload() && System.currentTimeMillis() - cacheContents.getStartReloadingTime() < 30*1000) {
					cacheContents.incrementHitCount();
					results.add((ProductDO)cacheContents.getContents());
				} else {
					cacheContents.startReload();
					loadSkus.add(sku);
				}
			}
		}else{
			loadSkus.addAll(skus);
		}
		if( !loadSkus.isEmpty() ){
			List<ProductDO> products = findProduct(loadSkus, null, fillType, autoId, cartId, yatpz, isWithCart, params);
			if (products != null && products.size() > 0) {
				for(ProductDO product : products){
					if (cacheable) {
						cache.put(fillType, product);
					}
					results.add(product);
				}
			}
		}
		
		return results;
	}

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
	@Override
	public ProductDO loadProductForMR(String sku) {
		CacheContents cacheContents = mrCache.get(FillType.SMALL, CacheKeyType.SKU, sku);
		if (cacheContents == null) {
		} else if (System.currentTimeMillis() - cacheContents.getStartTime() < MR_CACHE_PERIOD) {
			cacheContents.incrementHitCount();
			return (ProductDO)cacheContents.getContents();
		} else if (cacheContents.isStartReload() && System.currentTimeMillis() - cacheContents.getStartReloadingTime() < 30*1000) {
			cacheContents.incrementHitCount();
			return (ProductDO)cacheContents.getContents();
		} else {
			cacheContents.startReload();
		}
		Map<String, ProductDO> productMap = communityLocalProductDao.findProductBySku(Lists.newArrayList(sku));
		if (productMap != null && productMap.size() > 0) {
			for(ProductDO product:productMap.values()){
				mrCache.put(FillType.SMALL, product);
				return product;
			}
		}
		return null;
	}
	
	

	/**
	 * 指定した商品の情報を返します。
	 * @param janCode JANコード
	 * @return 商品情報
	 */
	@Override
	public ProductDO loadProductByJanCode(String janCode) {
		if (useDummy) {
			return dummyProductDao.loadProductByJanCode(janCode);
		}
		List<String> janCodes = new ArrayList<String>();
		janCodes.add(janCode);
		return findByJanCode(janCodes).get(janCode);
	}

	/**
	 * 指定した商品情報リストを返します。
	 * @param janCodes JANコードリスト
	 * @return 商品情報リスト
	 */
	@Override
	public Map<String, ProductDO> findByJanCode(Collection<String> janCodes) {
		if (useDummy) {
			return dummyProductDao.findByJanCode(janCodes);
		}

		Map<String, ProductDO> result = new HashMap<String, ProductDO>();

		List<String> findJanCodes = Lists.newArrayList();
		for (String janCode:janCodes) {
			CacheContents cacheContents = cache.get(FillType.JANCODE, CacheKeyType.JAN_CODE, janCode);
			if (cacheContents == null) {
				findJanCodes.add(janCode);
				continue;
			}
			if (System.currentTimeMillis() - cacheContents.getStartTime() < DEFAULT_CACHE_PERIOD) {
				cacheContents.incrementHitCount();
				result.put(janCode, (ProductDO)cacheContents.getContents());
			} else if (cacheContents.isStartReload() && System.currentTimeMillis() - cacheContents.getStartReloadingTime() < 30*1000) {
				cacheContents.incrementHitCount();
				result.put(janCode, (ProductDO)cacheContents.getContents());
			} else {
				cacheContents.startReload();
				findJanCodes.add(janCode);
			}
		}

		if (findJanCodes.isEmpty()) return result;

		List<ProductDO> products = findProduct(null, findJanCodes,FillType.JANCODE, null, null, null, false, null);

		for (ProductDO product : products) {
			result.put(product.getJan(), product);
			cache.put(FillType.SMALL, product);
		}
		return result;
	}

	/**
	 * 指定した商品情報を返します。
	 * @param sku SKU
	 * @param path パス
	 * @return 商品情報
	 */
	@Override
	public ProductDO load(String sku, Condition path) {
		return loadProduct(sku);
	}

	/**
	 * 指定した商品情報リストを返します。
	 * @param skus SKUリスト
	 * @return 商品情報リスト
	 */
	@Override
	public Map<String, ProductDO> findBySku(Collection<String> skus) {
		return find(skus, null);
	}
	
	/**
	 * 指定した商品情報リストを返します。
	 * @param skus SKUリスト
	 * @return 商品情報リスト
	 */
	@Override
	public Map<String, ProductDO> findBySkuForMR(Collection<String> skus) {
		Map<String, ProductDO> result = new HashMap<String, ProductDO>();
		List<String> findSkus = Lists.newArrayList();
		for (String sku:skus) {
			CacheContents cacheContents = mrCache.get(FillType.SMALL, CacheKeyType.SKU, sku);
			if (cacheContents == null) {
				findSkus.add(sku);
				continue;
			}
			if (System.currentTimeMillis() - cacheContents.getStartTime() < MR_CACHE_PERIOD) {
				cacheContents.incrementHitCount();
				result.put(sku, (ProductDO)cacheContents.getContents());
			} else if (cacheContents.isStartReload() && System.currentTimeMillis() - cacheContents.getStartReloadingTime() < 30*1000) {
				cacheContents.incrementHitCount();
				result.put(sku, (ProductDO)cacheContents.getContents());
			} else {
				cacheContents.startReload();
				findSkus.add(sku);
			}
		}

		if (findSkus.isEmpty()) return result;
		Map<String, ProductDO> findResult = communityLocalProductDao.findProductBySku(findSkus);
		for (String findSku : findSkus) {
			ProductDO product = findResult.get(findSku);
			result.put(findSku, product);
			if (product != null) {
				mrCache.put(FillType.SMALL, product);
			}
		}
		return result;
	}

	/**
	 * 指定した商品情報リストを返します。
	 * @param skus SKUリスト
	 * @param path パス
	 * @return 商品情報リスト
	 */
	@Override
	public Map<String, ProductDO> find(Collection<String> skus, Condition path) {
		Map<String, ProductDO> result = new HashMap<String, ProductDO>();
		if (useDummy) {
			for (String sku : skus) {
				ProductDO product = dummyProductDao.loadProduct(sku);
				result.put(sku, product);
			}
			return result;
		}
		List<String> findSkus = Lists.newArrayList();
		for (String sku:skus) {
			CacheContents cacheContents = cache.get(FillType.SMALL, CacheKeyType.SKU, sku);
			if (cacheContents == null) {
				findSkus.add(sku);
				continue;
			}
			if (System.currentTimeMillis() - cacheContents.getStartTime() < DEFAULT_CACHE_PERIOD) {
				cacheContents.incrementHitCount();
				result.put(sku, (ProductDO)cacheContents.getContents());
			} else if (cacheContents.isStartReload() && System.currentTimeMillis() - cacheContents.getStartReloadingTime() < 30*1000) {
				cacheContents.incrementHitCount();
				result.put(sku, (ProductDO)cacheContents.getContents());
			} else {
				cacheContents.startReload();
				findSkus.add(sku);
			}
		}

		if (findSkus.isEmpty()) return result;

		List<ProductDO> products = findProduct(findSkus, null, FillType.SMALL, null, null, null, false, null);
		Map<String, ProductDO> findResult = new HashMap<String, ProductDO>();
		for (ProductDO product : products) {
			findResult.put(product.getSku(), product);
		}
		for (String findSku : findSkus) {
			ProductDO product = findResult.get(findSku);
			result.put(findSku, product);
			if (product != null) {
				cache.put(FillType.SMALL,product);
			}
		}
		return result;
	}
	
	@Override
	public List<ProductDO> findVariationProductBySku(String sku) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("sku_s:");
		buffer.append(SolrUtil.escape(sku));
		buffer.append(" AND startTime_dt:[* TO NOW] AND endTime_dt:[NOW TO *]");
		
		SearchResult<DBVariationProductDO> variationProducts = solrOperations.findByQuery(
				new SolrQuery(buffer.toString()),
				DBVariationProductDO.class,
				Path.DEFAULT);
		
		if( variationProducts.getDocuments() == null || variationProducts.getDocuments().isEmpty() || variationProducts.getDocuments().size() > 1)
			return null;
		
		DBVariationProductDO variationProduct = variationProducts.getDocuments().get(0);
		if( variationProduct.getVariationProducts() == null || variationProduct.getVariationProducts().isEmpty() )
			return null;
		List<String> skus = variationProduct.getVariationProducts();
		Collections.sort(skus, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		
		return loadProducts(skus);
	}
	
	public static class OnlyCookieCacheFilter implements MethodCacheFilter {

		@SuppressWarnings("unchecked")
		@Override
		public boolean filter(Object[] args) {
			String autoId = (String)args[2];
			String cartId = (String)args[3];
			String yatpz = (String)args[4];
			Map<String,Object> params = (Map<String,Object>) args[6];
			if (params != null && !params.isEmpty()) return false;
			return !StringUtils.isEmpty(autoId) || !StringUtils.isEmpty(cartId) || !StringUtils.isEmpty(yatpz);
		}
	}

	enum CacheKeyType {
		SKU,JAN_CODE
	}

	class Cache {

		Map<FillType, Map<CacheKeyType,Map<String,CacheContents>>> cache = Maps.newHashMap();
		private int CACHE_SIZE = 0;
		Cache(int cacheSize) {
			initialize(cacheSize);
		}

		@SuppressWarnings("unchecked")
		synchronized void initialize(int cacheSize) {
			this.CACHE_SIZE = cacheSize;
			for (FillType fillType:FillType.values()) {
				cache.put(fillType, new HashMap<ProductDaoImpl.CacheKeyType, Map<String,CacheContents>>());
				for (CacheKeyType cacheKeyType:CacheKeyType.values()) {
					cache.get(fillType).put(cacheKeyType, new LRUMap(this.CACHE_SIZE));
				}
			}
		}

		synchronized CacheContents get(FillType fillType, CacheKeyType cacheKeyType, String key) {
			CacheContents cacheContents = cache.get(fillType).get(cacheKeyType).get(key);
			if (cacheContents == null) return null;

			return cacheContents;
		}

		synchronized void put(FillType fillType, ProductDO productDO) {
			String sku = productDO.getSku();
			String janCode = productDO.getJan();
			
			CacheContents cacheContents = new CacheContents(sku, productDO);
			
			cache.get(fillType).get(CacheKeyType.SKU).put(sku, cacheContents);
			if (!StringUtils.isBlank(janCode)) {
				cache.get(fillType).get(CacheKeyType.JAN_CODE).put(janCode, new CacheContents(janCode, productDO));
			}
			if (FillType.LARGE.equals(fillType)) {
				cache.get(FillType.SMALL).get(CacheKeyType.SKU).put(sku, cacheContents);
				if (!StringUtils.isBlank(janCode)) {
					cache.get(FillType.SMALL).get(CacheKeyType.JAN_CODE).put(janCode, new CacheContents(janCode, productDO));
				}
			}
		}
	}



	/**
	 * 指定されたキーワードで商品を検索して返します。
	 * @param keyword キーワード
	 * @param excludeSkus 除外する商品リスト
	 * @param includeCero CERO商品を含める場合、true
	 * @param includeAdult アダルト商品を含める場合、true
	 * @return 商品リスト
	 */
	@Override
	public List<ProductDO> findByKeyword(
			String keyword,
			List<String> excludeSkus,
			boolean includeCero,
			boolean includeAdult) {
		return findProductByKeyword(keyword, excludeSkus,
				includeCero, includeAdult);
	}

	@Override
	public void save(Collection<ProductDO> objects, Condition path) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteByKeys(Collection<String> keys, Condition path) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ProductDO> findByFk(String fkName, Object fkValue) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteByFk(String fkName, Object fkValue) {
		throw new UnsupportedOperationException();
	}

	/**
	 * ランダムインスタンスです。
	 */
	@SuppressWarnings("unused")
	private static Random random;

	static {
		try {
			random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		} catch (Exception e) {
			random = new Random();
		}
	}

	/**
	 * 商品情報リストを取得します。
	 * @param skus SKUリスト
	 * @param janCodes JANコードリスト
	 * @param fillType フィルタイプ
	 * @param autoId autoId
	 * @param cartId cartId
	 * @param yatpz yatpz
	 * @param params 拡張パラメーター
	 * @return 商品情報リスト
	 */
	private List<ProductDO> findProduct(
			Collection<String> skus,
			Collection<String> janCodes,
			FillType fillType,
			String autoId,
			String cartId,
			String yatpz,
			boolean isWithCart,
			Map<String,Object> params) {
		
		if (disableAccessCatalog) {
			throw new RuntimeException();
		}
		if (skus != null && janCodes != null && !skus.isEmpty() && janCodes.isEmpty()) {
			throw new UnsupportedOperationException("param error!!!!");
		}
		
		List<String> uniqSkus = null;
		List<String> uniqJans = null;
		if(skus !=null && !skus.isEmpty()){
			uniqSkus = ListUtil.convertCollection2List(skus);
			ListUtil.sortUniq(uniqSkus);
			return findCatalogProductApiBySku(uniqSkus, fillType, autoId, cartId,yatpz, isWithCart, params);		
		}else if(janCodes !=null && !janCodes.isEmpty()){
			uniqJans = ListUtil.convertCollection2List(janCodes);
			ListUtil.sortUniq(uniqJans);
			return findCatalogProductApiByJancode(uniqJans, fillType, autoId, cartId,yatpz, isWithCart, params);		
		}else {
			throw new UnsupportedOperationException("param error!!!!");
		}
	}

	private List<ProductDO> findCatalogProductApiBySku(
			Collection<String> skus, 
			FillType fillType, 
			String autoId,
			String cartId, 
			String yatpz, 
			boolean isWithCart,
			Map<String, Object> params) {
		if(skus.size() > 100) {
			List<String> requestSkus = new ArrayList<String>();
			List<ProductDO> products = new ArrayList<ProductDO>();
			for(String sku :skus) {
				requestSkus.add(sku);
				if(requestSkus.size() >= 100) {
					products.addAll(findCatalogProductApi(requestSkus, null, fillType, autoId, cartId, yatpz, isWithCart, params)) ;
					requestSkus = new ArrayList<String>();
				}
			}
			if(requestSkus.size() > 0){
				products.addAll(findCatalogProductApi(requestSkus, null, fillType, autoId, cartId, yatpz, isWithCart, params)) ;
			}
			return products;
		}else{
			return findCatalogProductApi(skus, null, fillType, autoId, cartId, yatpz, isWithCart, params);		
		}
	}
	private List<ProductDO> findCatalogProductApiByJancode(
			Collection<String> janCodes,
			FillType fillType,
			String autoId,
			String cartId,
			String yatpz,
			boolean isWithCart,
			Map<String, Object> params) {
		if(janCodes.size() > 100) {
			List<String> requestJanCodes = new ArrayList<String>();
			List<ProductDO> products = new ArrayList<ProductDO>();
			for(String jancode :janCodes) {
				requestJanCodes.add(jancode);
				if(requestJanCodes.size() >= 100) {
					products.addAll(findCatalogProductApi(null , requestJanCodes, fillType, autoId, cartId, yatpz, isWithCart, params)) ;
					requestJanCodes = new ArrayList<String>();
				}
			}
			if(requestJanCodes.size() > 0){
				products.addAll(findCatalogProductApi(null, requestJanCodes, fillType, autoId, cartId, yatpz, isWithCart, params)) ;
			}
			return products;
		}else{
			return findCatalogProductApi(null, janCodes, fillType, autoId, cartId, yatpz, isWithCart, params);		
		}
	}

	@SuppressWarnings({ "unchecked" })
	private List<ProductDO> findCatalogProductApi(
			Collection<String> skus,
			Collection<String> janCodes, 
			FillType fillType,
			String autoId,
			String cartId,
			String yatpz,
			boolean isWithCart,
			Map<String, Object> queryParams) {
		try {
			CookieStore cookieStore = new BasicCookieStore();
			String cookieDomain = new URL(resourceConfig.catalogProductEndpoint).getHost();

			if (StringUtils.isNotEmpty(autoId)) {
				BasicClientCookie cookie = new BasicClientCookie(EnvUtil.getEnvName(resourceConfig.catalogCookieAutoid), autoId);
				cookie.setVersion(0);
				cookie.setDomain(cookieDomain);
				cookie.setPath("/");
				cookieStore.addCookie(cookie);
			}
			if (StringUtils.isNotEmpty(cartId)) {
				BasicClientCookie cookie = new BasicClientCookie(EnvUtil.getEnvName(resourceConfig.catalogCookieCartId), cartId);
				cookie.setVersion(0);
				cookie.setDomain(cookieDomain);
				cookie.setPath("/");
				cookieStore.addCookie(cookie);
			}
			if (StringUtils.isNotEmpty(yatpz)) {
				BasicClientCookie cookie = new BasicClientCookie(EnvUtil.getEnvName(resourceConfig.catalogCookieYatpz), yatpz);
				cookie.setVersion(0);
				cookie.setDomain(cookieDomain);
				cookie.setPath("/");
				cookieStore.addCookie(cookie);
			}
			
			LOG.info("--------------------- Cookie Values Start---------------------");
			LOG.info(EnvUtil.getEnvName(resourceConfig.catalogCookieAutoid) + " : " + autoId);
			LOG.info(EnvUtil.getEnvName(resourceConfig.catalogCookieCartId) + " : " + cartId);
			LOG.info(EnvUtil.getEnvName(resourceConfig.catalogCookieYatpz) + " : " + yatpz);
			LOG.info("--------------------- Cookie Values End---------------------");
			
			CloseableHttpClient client = HttpClients.custom()
					.setDefaultCookieStore(cookieStore)
					.build();
			
			RequestConfig requestConfig = RequestConfig.custom()
					.setSocketTimeout(resourceConfig.catalogWsSocketTimeout)
					.setConnectTimeout(resourceConfig.catalogWsConnectionTimeout)
					.setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY)
					.build();
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("ft", fillType.getCode()));
			if (skus != null && !skus.isEmpty()) {
				for (String sku : skus) {
					params.add(new BasicNameValuePair("sku", sku));
				}
			}
			if (janCodes != null && !janCodes.isEmpty()) {
				for (String jan : janCodes) {
					params.add(new BasicNameValuePair("jan", jan));
				}
			}
			if( isWithCart ){
				params.add(new BasicNameValuePair("isCartTag", "1"));
			}
			if (queryParams != null && !queryParams.isEmpty()) {
				for (Entry<String, Object> entry : queryParams.entrySet()) {
					if (entry.getValue() instanceof Collection) {
						for (String value : ((Collection<String>) entry.getValue())) {
							params.add(new BasicNameValuePair(entry.getKey(), value));
						}
					} else {
						params.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
					}
				}
			}
			
			URIBuilder uriBuilder = new URIBuilder(resourceConfig.catalogProductEndpoint);
			//uriBuilder.setCustomQuery(URLEncodedUtils.format(params, "UTF-8"));
			uriBuilder.setParameters(params);
			URI uri = uriBuilder.build();

			if (LOG.isInfoEnabled()) {
				LOG.info("catalog access." + uri.toString());
			}
			
			HttpGet getMethod = new HttpGet(uri);
			getMethod.setConfig(requestConfig);
			
			long startTime = System.currentTimeMillis();
			CloseableHttpResponse response = client.execute(getMethod);
			
			try{
				String body = EntityUtils.toString(response.getEntity(), "UTF-8");
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					ObjectMapper objectMapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, Visibility.ANY);
					objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
					ApiResult result = objectMapper.readValue(body, ApiResult.class);
					if ("Success".equals(result.getResult())) {
						if(LOG.isDebugEnabled()) {
							LOG.debug("catalog access. time=" + (System.currentTimeMillis() - startTime) + ", url=" + uri.toString() + ", data=" + body);
						} else if (LOG.isInfoEnabled()) {
							LOG.info("catalog access. time=" + (System.currentTimeMillis() - startTime) + ", url=" + uri.toString());
						}
						
						if (result.getData() == null) {
							return new ArrayList<ProductDO>();
						} else {
							List<ProductDO> products = result.getData().getProducts();
							if (products != null) {
								for (ProductDO product : products) {
									if (product.getRvwPtFlg() == null) {
										LOG.warn("PointInfo is null. sku = " + product.getSku());
									}
								}
							}
							return products;
						}
					} else {
						throw new CatalogAccessException("result=" + result.getResult()
								+ ", time=" + (System.currentTimeMillis() - startTime) + ", url=" + uri.toString());
					}
				} else {
					throw new HttpException("status = " + response.getStatusLine().getStatusCode()
							+ ", time=" + (System.currentTimeMillis() - startTime) + ", url=" + uri.toString());
				}
			}finally{
				response.close();
			}
		} catch (CatalogAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new CatalogAccessException(e);
		}
	}
	
	/**
	 * 商品情報リストを取得します。
	 * @param keyword キーワード
	 * @param excludeSkus 除外する商品リスト
	 * @param includeCero CERO商品を含める場合、true
	 * @param includeAdult アダルト商品を含める場合、true
	 * @return 商品情報リスト
	 */
	private List<ProductDO> findProductByKeyword(
			String keyword,
			List<String> excludeSkus,
			boolean includeCero,
			boolean includeAdult) {
		
		if (disableAccessCatalog) {
			throw new RuntimeException();
		}
		
		CloseableHttpClient client = HttpClients.createDefault();
		
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(resourceConfig.catalogWsSocketTimeout)
				.setConnectTimeout(resourceConfig.catalogWsConnectionTimeout)
				.build();
		
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("ky", keyword));
			if( excludeSkus != null && !excludeSkus.isEmpty()){
				for( String excludeSku : excludeSkus){
					params.add(new BasicNameValuePair("excsku", excludeSku));
				}
			}
			params.add(new BasicNameValuePair("cr", includeCero?"1":"0"));
			params.add(new BasicNameValuePair("ad", includeAdult?"1":"0"));
			
			URIBuilder uriBuilder = new URIBuilder(resourceConfig.catalogSuggestEndpoint);
			uriBuilder.setParameters(params);
			URI uri = uriBuilder.build();
			
			if (LOG.isDebugEnabled()) {
				LOG.debug("catalog access." + uri.toString());
			}

			HttpGet getMethod = new HttpGet(uri);
			getMethod.setConfig(requestConfig);
			
			long startTime = System.currentTimeMillis();
			CloseableHttpResponse response = client.execute(getMethod);
			try{
				String body = EntityUtils.toString(response.getEntity(), "UTF-8");
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					ObjectMapper objectMapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, Visibility.ANY);
					objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
					ApiResult result = objectMapper.readValue(body, ApiResult.class);
					if ("Success".equals(result.getResult())) {
						if (LOG.isInfoEnabled()) {
							LOG.info("catalog access. time=" + (System.currentTimeMillis() - startTime) + ", url=" + uri.toString() + ", data=" + body);
						}
						if (result.getData() == null) {
							return Lists.newArrayList();
						} else {
							return result.getData().getProducts();
						}
					} else {
						throw new CatalogAccessException("result=" + result.getResult()
								+ ", time=" + (System.currentTimeMillis() - startTime) + ", url=" + uri.toString());
					}
				} else {
					throw new HttpException("status = " + response.getStatusLine().getStatusCode()
							+ ", time=" + (System.currentTimeMillis() - startTime) + ", url=" + uri.toString());
				}
			}finally{
				response.close();
			}
		} catch (CatalogAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new CatalogAccessException(e);
		}finally{
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * APIの実行結果です。
	 * @author kamiike
	 *
	 */
	public static class ApiResult {

		/**
		 * 処理結果です。
		 */
		private String result;

		/**
		 * レスポンスデータです。
		 */
		private ApiResultData data;

		/**
		 * @return result
		 */
		public String getResult() {
			return result;
		}

		/**
		 * @param result セットする result
		 */
		public void setResult(String result) {
			this.result = result;
		}

		/**
		 * @return data
		 */
		public ApiResultData getData() {
			return data;
		}

		/**
		 * @param data セットする data
		 */
		public void setData(ApiResultData data) {
			this.data = data;
		}
	}

	/**
	 * レスポンスデータです。
	 * @author kamiike
	 *
	 */
	public static class ApiResultData {

		/**
		 * 商品データリストです。
		 */
		private List<ProductDO> products;

		/**
		 * @return products
		 */
		public List<ProductDO> getProducts() {
			return products;
		}

		/**
		 * @param products セットする products
		 */
		public void setProducts(List<ProductDO> products) {
			this.products = products;
		}

	}
	
	

}
