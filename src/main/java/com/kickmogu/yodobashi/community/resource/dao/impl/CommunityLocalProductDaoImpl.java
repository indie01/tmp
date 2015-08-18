/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kickmogu.lib.core.domain.SearchResult;
import com.kickmogu.lib.core.exception.CommonSystemException;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.dao.CommunityLocalProductDao;
import com.kickmogu.yodobashi.community.resource.domain.DBItemObjectUrlDO;
import com.kickmogu.yodobashi.community.resource.domain.DBProductDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.DBReviewPointDO;
import com.kickmogu.yodobashi.community.resource.domain.DBReviewPointQuestDO;
import com.kickmogu.yodobashi.community.resource.domain.DBReviewPointQuestDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ProductImage;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewPoint;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewPointDetail;

/**
 * 商品DAO(コミュニティLocalDB) の実装です。
 */
@Service
public class CommunityLocalProductDaoImpl implements CommunityLocalProductDao {

	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger log = LoggerFactory.getLogger(CommunityLocalProductDaoImpl.class);

	/**
	 * レビュー投稿可能
	 */
	static final int reviewContributionPossible = 1;

	private final int SOLR_QUERY_ITEM_MAX = 1000;
	
	/**
	 * Solrアクセサです。
	 */
	@Autowired
	@Qualifier("default")
	private SolrOperations solrOperations;

	private <T> void resultToMapBySku(SearchResult<T> result, Map<String, SearchResult<T>> map, Method skuGetter) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		for (T record : result.getDocuments()) {
			String sku = (String) skuGetter.invoke(record);
			SearchResult<T> resultSkuGroup = map.get(sku);
			if (resultSkuGroup == null) {
				List<T> list = Lists.newArrayList();
				resultSkuGroup = new SearchResult<T>((long)0, list);
				map.put(sku, resultSkuGroup);
			}
			resultSkuGroup.getDocuments().add(record);
			resultSkuGroup.setNumFound((long) resultSkuGroup.getDocuments().size());
		}
	}

	private Map<String, ProductDO> catalogProductToCommunityProduct(String query, String targetDateTime) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String now = (targetDateTime == null)? "NOW":targetDateTime;
		
		// 未来日情報もあるので、日付を指定して検索すること
		// mainImageUrls,listImageUrlsはリンクサイズが大きいので直接objectTypedCode_s条件を指定して検索すること
		
		// DBProductDetailDO
		SearchResult<DBProductDetailDO> resultProductDetail = solrOperations.findByQuery(
				new SolrQuery(query + " AND enableFlag_i:1 AND urgentStopFlg_s:0 AND startTime_dt:[* TO " + now + "] AND endTime_dt:[" + now + " TO *]").setRows(SOLR_QUERY_ITEM_MAX),
				DBProductDetailDO.class,
				Path.includeProp("*").includePath("brand.productBrandId,maker.makerMasterId").depth(1));

		if (resultProductDetail.getDocuments().size() <= 0) {
			return new HashMap<String, ProductDO>();
		}
		
		StringBuffer skusQuery = new StringBuffer("(");
		Iterator<DBProductDetailDO> iter = resultProductDetail.getDocuments().iterator();
		while (iter.hasNext()) {
			String sku = iter.next().getSku();
			skusQuery.append("sku_s:" + sku);
			if (iter.hasNext()) {
				skusQuery.append(" OR ");
			}
		}
		skusQuery.append(")");

		// DBItemObjectUrlDO MainImageUrls
		Map<String, SearchResult<DBItemObjectUrlDO>> mapMainImageUrls = Maps.newHashMap();
		SearchResult<DBItemObjectUrlDO> resultsMainImageUrls = findAll(
				new SolrQuery(skusQuery.toString() + " AND objectTypedCode_s:10203"),
				DBItemObjectUrlDO.class);
		resultToMapBySku(resultsMainImageUrls, mapMainImageUrls, DBItemObjectUrlDO.class.getMethod("getSku"));
		
		// DBItemObjectUrlDO ListImageUrls
		Map<String, SearchResult<DBItemObjectUrlDO>> mapListImageUrls = Maps.newHashMap();
		SearchResult<DBItemObjectUrlDO> resultsListImageUrls = findAll(
				new SolrQuery(skusQuery.toString() + " AND objectTypedCode_s:10201").setRows(SOLR_QUERY_ITEM_MAX),
				DBItemObjectUrlDO.class);
		resultToMapBySku(resultsListImageUrls, mapListImageUrls, DBItemObjectUrlDO.class.getMethod("getSku"));
		
		// DBReviewPointDO
		Map<String, SearchResult<DBReviewPointDO>> mapReviewPoint = Maps.newHashMap();
		SearchResult<DBReviewPointDO> resltReviewPoint = findAll(
				new SolrQuery(skusQuery.toString() + " AND startTime_dt:[* TO " + now + "] AND endTime_dt:[" + now + " TO *]").setRows(SOLR_QUERY_ITEM_MAX),
				DBReviewPointDO.class);
		resultToMapBySku(resltReviewPoint, mapReviewPoint, DBReviewPointDO.class.getMethod("getSku"));
		
		// DBReviewPointQuestDO
		Map<String, SearchResult<DBReviewPointQuestDO>> mapReviewPointQuest = Maps.newHashMap();
		SearchResult<DBReviewPointQuestDO> resltReviewPointQuest = findAll(
				new SolrQuery(skusQuery.toString() + " AND startTime_dt:[* TO " + now + "] AND endTime_dt:[" + now + " TO *] AND rqStartTime_dt:[* TO " + now + "] AND rqEndTime_dt:[" + now + " TO *]").setRows(999).addSortField("orderNo_i", ORDER.asc),
				DBReviewPointQuestDO.class);
		resultToMapBySku(resltReviewPointQuest, mapReviewPointQuest, DBReviewPointQuestDO.class.getMethod("getSku"));
		
		// DBReviewPointQuestDetailDO
		Map<String, SearchResult<DBReviewPointQuestDetailDO>> mapReviewPointQuestDetail = Maps.newHashMap();
		SearchResult<DBReviewPointQuestDetailDO> resltReviewPointQuestDetail = findAll(
				new SolrQuery(skusQuery.toString() + " AND startTime_dt:[* TO " + now + "] AND endTime_dt:[" + now + " TO *] AND rqStartTime_dt:[* TO " + now + "] AND rqEndTime_dt:[" + now + " TO *]").setRows(999),
				DBReviewPointQuestDetailDO.class);
		resultToMapBySku(resltReviewPointQuestDetail, mapReviewPointQuestDetail, DBReviewPointQuestDetailDO.class.getMethod("getSku"));
		// データの詰め替え
		return convertCatalogProductToCommunityProduct(
				resultProductDetail, 
				mapMainImageUrls,
				mapListImageUrls, 
 				mapReviewPoint, 
				mapReviewPointQuest, 
				mapReviewPointQuestDetail
				);
	}

	
	private <T> SearchResult<T> find(SolrQuery query, Class<T> clazz, int start) {
		query.setRows(SOLR_QUERY_ITEM_MAX);
		query.setStart(start);
		return solrOperations.findByQuery(query, clazz);
	}
	
	private <T> SearchResult<T> findAll(SolrQuery query, Class<T> clazz) {
		SearchResult<T> result = find(query, clazz, 0);
		long numfound = result.getNumFound();
		if(numfound < SOLR_QUERY_ITEM_MAX) return result;
		long endPos = numfound / SOLR_QUERY_ITEM_MAX;
		endPos +=   (numfound % SOLR_QUERY_ITEM_MAX > 0)?1:0;

		for(int i=1;i<endPos;i++) {
			int offsetStart = i * SOLR_QUERY_ITEM_MAX;
			SearchResult<T> appendResult = find(query, clazz, offsetStart);
			result.getDocuments().addAll(appendResult.getDocuments());
		}
		return result;
	}
	
	protected Map<String, ProductDO> findProduct(String queryField, Collection<String> items, String targetDateTime) {
		Map<String, ProductDO> result = Maps.newHashMap();
		try {
			Iterator<String> iter = items.iterator();
			while (iter.hasNext()) {
				StringBuffer sb = new StringBuffer("(");
				for (int i = 0; i < SOLR_QUERY_ITEM_MAX; i++) {
					sb.append(queryField + ":" + iter.next());
					if (iter.hasNext() && i < (SOLR_QUERY_ITEM_MAX - 1)) {
						sb.append(" OR ");
					} else {
						break;
					}
				}
				sb.append(")");
				result.putAll(catalogProductToCommunityProduct(sb.toString(), targetDateTime));
			}
		}
		catch (Exception e) {
			log.error("", e);
			throw new CommonSystemException(e);
		}
		return result;
	}

	@Override
	public Map<String, ProductDO> findProductBySku(Collection<String> skus) {
		return findProduct("sku_s", skus, null);
	}

	@Override
	public Map<String, ProductDO> findProductBySku(Collection<String> skus, String targetDateTime) {
		return findProduct("sku_s", skus, targetDateTime);
	}
	
	@Override
	public Map<String, ProductDO> findProductByJanCode(Collection<String> janCodes) {
		return findProduct("janCode_s", janCodes, null);
	}
	
	@Override
	public Map<String, ProductDO> findProductByJanCode(Collection<String> janCodes, String targetDateTime) {
		return findProduct("janCode_s", janCodes, targetDateTime);
	}
	
	static String createProductImageUrl(DBItemObjectUrlDO itemObjectUrl) {
		String sku = itemObjectUrl.getSku();
		if (StringUtils.isEmpty(sku) || sku.length() != 18)
			return null;
		StringBuilder sb = new StringBuilder();
		sb.append(ResourceConfig.INSTANCE.productImageUrl);
		sb.append("product/");
		sb.append(sku.substring(0, 3));
		sb.append("/");
		sb.append(sku.substring(3, 6));
		sb.append("/");
		sb.append(sku.substring(6, 9));
		sb.append("/");
		sb.append(sku.substring(9, 12));
		sb.append("/");
		sb.append(sku.substring(12, 15));
		sb.append("/");
		sb.append(sku.substring(15, 18));
		sb.append("/");
		sb.append(sku);
		sb.append("_");
		sb.append(itemObjectUrl.getObjectTypedCode());
		if (StringUtils.isNotEmpty(itemObjectUrl.getReplaceCount())) {
			sb.append("_");
			sb.append(itemObjectUrl.getReplaceCount());
		}
		sb.append(".jpg");
		return sb.toString();
	}

	static String createProductUrl(String sku) {
		return ResourceConfig.INSTANCE.catalogUrl + "ec/product/" + sku
				+ "/index.html";
	}

	static ProductImage[] convertCatalogImageToCommunityImage(
			SearchResult<DBItemObjectUrlDO> imageUrls, ImageType imageType) {
		List<ProductImage> productImages = new ArrayList<ProductImage>();
		if(imageUrls != null && !imageUrls.getDocuments().isEmpty()) {
			for (DBItemObjectUrlDO imteObjectUrl : imageUrls.getDocuments()) {
				ProductImage productImage = new ProductImage();
				productImage.setOrder(imteObjectUrl.getPriorityLeve());
				productImage.setUrl(createProductImageUrl(imteObjectUrl));
				productImages.add(productImage);
			}
		}
		if(productImages.isEmpty()) {
			ProductImage productImage = new ProductImage();
			productImage.setOrder(0);
			if(imageType.equals(ImageType.MainImage)) {
				productImage.setUrl(ResourceConfig.INSTANCE.productImageUrl + "product/NoImage_250x250.jpg");
			} else {
				productImage.setUrl(ResourceConfig.INSTANCE.productImageUrl + "product/NoImage_80x80.jpg");
			}
			productImages.add(productImage);
		}
		return productImages.toArray(new ProductImage[] {});
	}

	static String formatDate(SimpleDateFormat sdf, Date date) {
		try {
			return sdf.format(date);
		} catch (Exception e) {
		}
		return null;
	}

	static void convertCatalogPointToCommunityPoint(ProductDO product,
			DBReviewPointDO reviewPoint,
			List<DBReviewPointQuestDO> reviewPointQuests,
			List<DBReviewPointQuestDetailDO> reviewPointQuestDetails) {

		if (reviewPoint == null)
			return;

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		product.setRvwPtFlg(reviewPoint.getReviewPointWhetherFlag() == reviewContributionPossible);
		product.setRvwCmnt(reviewPoint.getReviewComment());
		product.setRvwPtCalcTyp(reviewPoint.getPointCalcType());
		product.setRvwInitPostTerm(reviewPoint.getInitialPostTerm());
		product.setRvwCntnPostCnt(reviewPoint.getContinuationPostTimes());
		product.setRvwCntnPostTerm(reviewPoint.getContinuationPostTerm());
		product.setRvwPtSttTm(formatDate(sdf, reviewPoint.getStartTime()));
		product.setRvwPtEdTm(formatDate(sdf, reviewPoint.getEndTime()));
		product.setRvwPtLstUpd(formatDate(sdf, reviewPoint.getLastUpdate()));

		if(reviewPointQuests == null || reviewPointQuests.isEmpty()) return;
		List<ReviewPoint> questReviewPoints = Lists.newArrayList();
		for(DBReviewPointQuestDO reviewPointQuest: reviewPointQuests) {
			ReviewPoint questReviewPoint = new ReviewPoint();
			questReviewPoint.setOrder(reviewPointQuest.getOrderNo());
			questReviewPoint.setRvwQstCd(reviewPointQuest.getRqCode());
			questReviewPoint.setStartTime(reviewPointQuest.getStartTime());
			questReviewPoint.setEndTime(reviewPointQuest.getEndTime());
			questReviewPoint.setRvwQstSttTm(formatDate(sdf, reviewPointQuest.getRqStartTime()));
			questReviewPoint.setRvwQstEdTm(formatDate(sdf, reviewPointQuest.getRqEndTime()));
			questReviewPoint.setRvwQstDtls(null);
			questReviewPoints.add(questReviewPoint);
		}
		product.setRvwQsts(questReviewPoints.toArray(new ReviewPoint[]{}));

		if(reviewPointQuestDetails == null || reviewPointQuestDetails.isEmpty()) return;
		
		Map<String, List<ReviewPointDetail>> questReviewPointDetailMap = new HashMap<String, List<ReviewPointDetail>>();
		for(DBReviewPointQuestDetailDO reviewPointQuestDetail: reviewPointQuestDetails) {
			ReviewPointDetail questReviewPointDetail = new ReviewPointDetail();
			questReviewPointDetail.setLstUpd(formatDate(sdf, reviewPointQuestDetail.getLastUpdate()));
			questReviewPointDetail.setRvwBasePt(reviewPointQuestDetail.getRqBaseReviewPointValue());
			questReviewPointDetail.setRvwQstDtlCd(reviewPointQuestDetail.getRqCode());
			questReviewPointDetail.setRvwQstDtlSttTm(formatDate(sdf, reviewPointQuestDetail.getRqStartTime()));
			questReviewPointDetail.setRvwQstDtlEdTm(formatDate(sdf, reviewPointQuestDetail.getRqEndTime()));
			questReviewPointDetail.setRvwQstDtlSttThd(reviewPointQuestDetail.getRqStartThreshold());
			questReviewPointDetail.setRvwQstDtlEdThd(reviewPointQuestDetail.getRqEndThreshold());
			String mapKey = questReviewPointDetail.getRvwQstDtlCd() + "_" + String.valueOf(reviewPointQuestDetail.getEndTime().getTime()) + "_" + String.valueOf(questReviewPointDetail.getRvwQstDtlEdTm().getTime());
			if(!questReviewPointDetailMap.containsKey(mapKey)) {
				questReviewPointDetailMap.put(mapKey, new ArrayList<ReviewPointDetail>());
			}
			questReviewPointDetailMap.get(mapKey).add(questReviewPointDetail); 
		}
		
		for(ReviewPoint reviewPointQuest: product.getRvwQsts()) {
			String mapKey = reviewPointQuest.getRvwQstCd() + "_" + String.valueOf(reviewPointQuest.getEndTime().getTime()) + "_" + String.valueOf(reviewPointQuest.getRvwQstEdTm().getTime());
			if(questReviewPointDetailMap.containsKey(mapKey)) {
				reviewPointQuest.setRvwQstDtls(questReviewPointDetailMap.get(mapKey).toArray(new ReviewPointDetail[]{}));
			}
		}
	}

	static Map<String, ProductDO> convertCatalogProductToCommunityProduct(
			SearchResult<DBProductDetailDO> resultProductDetail,
			Map<String, SearchResult<DBItemObjectUrlDO>> resultMainImageUrls,
			Map<String, SearchResult<DBItemObjectUrlDO>> resltListImageUrls,
			Map<String, SearchResult<DBReviewPointDO>> resltReviewPoint,
			Map<String, SearchResult<DBReviewPointQuestDO>> resltReviewPointQuest,
			Map<String, SearchResult<DBReviewPointQuestDetailDO>> resltReviewPointQuestDetail) {
		
		Map<String, ProductDO> productMap = new HashMap<String, ProductDO>();
		for (DBProductDetailDO productDetail : resultProductDetail.getDocuments()) {
			ProductDO product = new ProductDO();

			product.setSku(productDetail.getSku());
			product.setJan(productDetail.getJanCode());
			product.setProductName(productDetail.getProductName());
			product.setStPrdNm(productDetail.getProductNameShort());
			product.setProductDescription(productDetail.getListSummary());
			product.setAdultKind(productDetail.getAdultKind());
			product.setCeroKind(productDetail.getCeroKind());
			if(productDetail.getBrand() != null)
				product.setBrndNm(productDetail.getBrand().getBrandName());
			if(productDetail.getMaker() != null) {
				product.setMkrCd(productDetail.getMaker().getMakerCode());
				product.setMkrNm(productDetail.getMaker().getMakerName());
			}
			
			product.setPrdUrl(createProductUrl(productDetail.getSku()));

			product.setMainImgs(convertCatalogImageToCommunityImage(resultMainImageUrls.get(productDetail.getSku()), ImageType.MainImage));
			product.setListImgs(convertCatalogImageToCommunityImage(resltListImageUrls.get(productDetail.getSku()), ImageType.ListImage));
			product.setPublFlg(productDetail.getEnableFlag() != 0);
			product.setRegisterDateTime(productDetail.getRegisterDateTime());
			product.setModifyDateTime(productDetail.getModifyDateTime());
			
			DBReviewPointDO reviewPoint = null;
			List<DBReviewPointQuestDO> reviewPointQuests = null;
			List<DBReviewPointQuestDetailDO> reviewPointQuestDetails = null;
			if(resltReviewPoint.containsKey(productDetail.getSku())) {
				reviewPoint = resltReviewPoint.get(productDetail.getSku()).getDocuments().get(0);
			}
			if(resltReviewPointQuest.containsKey(productDetail.getSku())) {
				reviewPointQuests = resltReviewPointQuest.get(productDetail.getSku()).getDocuments();
			}
			if(resltReviewPointQuestDetail.containsKey(productDetail.getSku())) {
				reviewPointQuestDetails = resltReviewPointQuestDetail.get(productDetail.getSku()).getDocuments();
			}
			convertCatalogPointToCommunityPoint(product, reviewPoint,
					reviewPointQuests, reviewPointQuestDetails);
			productMap.put(product.getSku(), product);
		}
		return productMap;
	}
	
	enum ImageType{
		MainImage(0),
		ListImage(1);
		
		private int code;

		ImageType(int code) {
			this.code = code;
		}
		
		int getCode() {
			return code;
		}
	}
}