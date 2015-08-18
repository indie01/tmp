package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.apache.axis.utils.StringUtils;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;
import com.kickmogu.lib.core.aop.MyselfAware;
import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.utils.StringUtil;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.resource.dao.CatalogProductDao;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.domain.DBItemObjectUrlDO;
import com.kickmogu.yodobashi.community.resource.domain.DBMakerMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.DBProductBrandDO;
import com.kickmogu.yodobashi.community.resource.domain.DBProductDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.DBReviewPointDO;
import com.kickmogu.yodobashi.community.resource.domain.DBReviewPointQuestDO;
import com.kickmogu.yodobashi.community.resource.domain.DBReviewPointQuestDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.DBVariationProductDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ProductType;

@Service @Lazy @Qualifier("default")
public class CatalogProductDaoImpl implements CatalogProductDao, MyselfAware<CatalogProductDao> {
	private static final Logger log = LoggerFactory.getLogger(CatalogProductDaoImpl.class);

	@Autowired @Qualifier("productLoader")
	private JdbcTemplate template;
	
	@Autowired @Qualifier("productLoader")
	private DataSourceTransactionManager transactionManager;
	
	@Autowired @Qualifier("default")
	private SolrOperations solrOperations;
	
	@Autowired
	private TimestampHolder timestampHolder;

	// 無効な日付
	private static final String INVALID_DATE_TEXT = "99991231";
	
	// ora_hash最小値
	private static final Integer ORAHASH_MIN = 0;
	// ora_hash最大値
	private static final Integer ORAHASH_MAX = 199;
	
	// チャネル種別 : PC
	private static final String CHANNEL_TYPE_PC = "0001";
	
	// 複合フラグ : 複合品ではない
	private static final Integer COMPLEX_FLAG_NONE = 0;
	// 複合フラグ : 複合商品親
	private static final Integer COMPLEX_FLAG_PARENT = 1;
	
	// バリエーションフラグ : 親子関係なし
	private static final Integer FAMILY_FLAG_NONE = 0;
	// バリエーションフラグ : 子供
	private static final Integer FAMILY_FLAG_CHILD = 2;
	
	// オブジェクトタイプコード : 商品メイン画像・リサイズ大
//	private static final String OBJECT_TYPED_CD_MAIN_RESIZE_BIG = "10002";
	private static final String OBJECT_TYPED_CD_MAIN_RESIZE_BIG = "10203";
	// オブジェクトタイプコード : 商品一覧画像（大） （一覧画像URL）
//	private static final String OBJECT_TYPED_CD_LIST_BIG = "10006";
	private static final String OBJECT_TYPED_CD_LIST_BIG = "10201";

	// レビュー設問コード : 特別条件コード
	private static final String RQCODE_SPECIAL = "I001";
	
	// 関連商品種別コード : 複合品
	private static final String RELATEDPCATEGORYCODE_COMPLEX = "0009";
	
	// solr一括保存最大件数
	private static final Integer SAVE_MAX = 100;
	// solr一括削除最大件数
	private static final Integer DELETE_MAX = 100;
	// select件数(更新通知)
	private static final Integer SELECT_MAX = 100;
	// update件数(更新通知)
	private static final Integer UPDATE_MAX = 100;

	// 更新通知タイププレフィックス
	private static final String UPDATE_TYPE_PREFIX = "Comm_";
	// 更新通知タイプ : makerMaster
	private static final String UPDATE_TYPE_MAKERMASTER = "Comm_makerMaster";
	// 更新通知タイプ : productBrand
	private static final String UPDATE_TYPE_PRODUCTBRAND = "Comm_brand";
	// 更新通知タイプ : itemObjectUrl
	private static final String UPDATE_TYPE_ITEMOBJECTURL = "Comm_itemObjectURL";
	// 更新通知タイプ : productDetail
	private static final String UPDATE_TYPE_PRODUCTDETAIL = "Comm_productDetail";
	// 更新通知タイプ : reviewPoint
	private static final String UPDATE_TYPE_REVIEWPOINT = "Comm_productReviewPoint";
	// 更新通知タイプ : variationProduct（pAttrRelatedItemのRelatedpCategoryCodeが0012のデータ）
	@SuppressWarnings("unused")
	private static final String UPDATE_TYPE_VARIATIONPRODUCT = "Comm_variationProduct";
	// コミュニティ用全更新通知タイプの配列
	private static final String[] UPDATE_TYPE_LIST = new String[] {
		//TODO UPDATE_TYPE_MAKERMASTER, UPDATE_TYPE_PRODUCTBRAND, UPDATE_TYPE_ITEMOBJECTURL, UPDATE_TYPE_PRODUCTDETAIL, UPDATE_TYPE_VARIATIONPRODUCT, UPDATE_TYPE_REVIEWPOINT,
		UPDATE_TYPE_MAKERMASTER, UPDATE_TYPE_PRODUCTBRAND, UPDATE_TYPE_ITEMOBJECTURL, UPDATE_TYPE_PRODUCTDETAIL, UPDATE_TYPE_REVIEWPOINT,
	};

	// 更新通知ステータス : 更新していない
	private static final Integer TOKYODBUPDATESTATUS_INIT = 0;
	private static final Integer OSAKADBUPDATESTATUS_INIT = 0;
	// 更新通知ステータス : 更新済み
	private static final Integer TOKYODBUPDATESTATUS_UPDATED = 1;
	private static final Integer OSAKADBUPDATESTATUS_UPDATED = 1;
	
	// pool size
	private static final int THREAD_POOL_SIZE = 1;
	
	private CatalogProductDao myself;
	
	@Override
	public void setMyself(CatalogProductDao myself) {
		this.myself = myself;
	}
	
	private ThreadPoolExecutor makerMasterThreadPoolExecutor;
	private ThreadPoolExecutor productBrandThreadPoolExecutor;
	private ThreadPoolExecutor itemObjectUrlThreadPoolExecutor;
	private ThreadPoolExecutor productDetailThreadPoolExecutor;
	private ThreadPoolExecutor reviewPointThreadPoolExecutor;
	private ThreadPoolExecutor reviewPointQuestThreadPoolExecutor;
	private ThreadPoolExecutor reviewPointQuestDetailThreadPoolExecutor;
	private ThreadPoolExecutor variationProductThreadPoolExecutor;
	private AtomicInteger makerMasterCount;
	private AtomicInteger productBrandCount;
	private AtomicInteger itemObjectUrlCount;
	private AtomicInteger productDetailCount;
	private AtomicInteger reviewPointCount;
	private AtomicInteger reviewPointQuestCount;
	private AtomicInteger reviewPointQuestDetailCount;
	private AtomicInteger variationProductCount;
	
	/**
	 * カタログＤＢから全商品情報をロードします
	 */
	@Override
	@ArroundSolr
	public void initialLoadProducts(Integer sleep, Integer groupStart, Integer groupEnd) {
		outputDataSourceInfo();
		outputArgs(sleep, groupStart, groupEnd);
		
		initializeThreads();
		
		log.info("start " + DBMakerMasterDO.class.getSimpleName());
		myself.loadMakerMaster();
		log.info("finish " + DBMakerMasterDO.class.getSimpleName());
		
		log.info("start " + DBProductBrandDO.class.getSimpleName());
		myself.loadProductBrand();
		log.info("finish " + DBProductBrandDO.class.getSimpleName());
		
		if (sleep != null && sleep == 0) sleep = 1;
		if (groupStart == null) groupStart = ORAHASH_MIN;
		if (groupEnd == null) groupEnd = ORAHASH_MAX;

		try {
			for (int i = groupStart; i <= groupEnd; i++) {
				log.info("start group: " + i);
				myself.loadProductsByGroup(i);
				log.info("finish group: " + i);
				
				if (sleep != null) {
					try {
						Thread.sleep(sleep);
					} catch (InterruptedException e) {
					}
				}
			}
			
		} finally {
			terminateThreads();
			
			log.info("finally commit start.");
			solrCommitAll();
			log.info("finally commit finished.");
		}
	}
	
	@Override
	@ArroundSolr
	public void initialLoadProductsBySku(String[] skus){
		outputDataSourceInfo();
		
		initializeThreads();
		
		log.info("start " + DBMakerMasterDO.class.getSimpleName());
		myself.loadMakerMaster();
		log.info("finish " + DBMakerMasterDO.class.getSimpleName());
		
		log.info("start " + DBProductBrandDO.class.getSimpleName());
		myself.loadProductBrand();
		log.info("finish " + DBProductBrandDO.class.getSimpleName());
		

		try {
			log.info("start : " + skus.toString());
			myself.loadProductsBySku(skus);
			log.info("finish: " + skus.toString());
		} finally {
			terminateThreads();
			
			log.info("finally commit start.");
			solrCommitAll();
			log.info("finally commit finished.");
		}
	}
	
	/**
	 * カタログＤＢから全商品情報をロードします
	 */
	@Override
	@ArroundSolr
	public void initialLoadVariationProducts(Integer sleep, Integer groupStart, Integer groupEnd) {
		outputDataSourceInfo();
		outputArgs(sleep, groupStart, groupEnd);
		
		initializeThreads();
		
		if (sleep != null && sleep == 0) sleep = 1;
		if (groupStart == null) groupStart = ORAHASH_MIN;
		if (groupEnd == null) groupEnd = ORAHASH_MAX;

		try {
			for (int i = groupStart; i <= groupEnd; i++) {
				log.info("start group: " + i);
				myself.loadVariationProductsByGroup(i);
				log.info("finish group: " + i);
				
				if (sleep != null) {
					try {
						Thread.sleep(sleep);
					} catch (InterruptedException e) {
					}
				}
			}
			
		} finally {
			terminateThreads();
			
			log.info("finally commit start.");
			solrCommitAll();
			log.info("finally commit finished.");
		}
	}
	
	@Override
	@ArroundSolr
	public void initialLoadVariationProductsBySku(String[] skus){
		outputDataSourceInfo();
		
		initializeThreads();
		
		try {
			log.info("start : " + skus.toString());
			myself.loadVariationProductsBySku(skus);
			log.info("finish: " + skus.toString());
		} finally {
			terminateThreads();
			
			log.info("finally commit start.");
			solrCommitAll();
			log.info("finally commit finished.");
		}
	}
	/**
	 * 差分を更新します。
	 */
	@Override
	@ArroundSolr
	public void updateProducts() throws Exception {
		outputDataSourceInfo();
		
		final Map<String, List<NotifyUpdateControl>> notifyUpdateControlMap = loadNotifyUpdateControl();

		try {
			initializeThreads();
			
			for (final String type : UPDATE_TYPE_LIST) {
				if (!notifyUpdateControlMap.containsKey(type)) {
					continue;
				}
				List<NotifyUpdateControl> list = notifyUpdateControlMap.get(type);
				updateInvoker(type, list);
			}
		} finally {
			log.info("terminate threads start.");
			terminateThreads();
			log.info("terminate threads finish.");
			
			log.info("solr optimize start.");
			solrCommitAll();
			log.info("solr optimize finish.");
		}
		
		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		txTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
		
		log.info("update slaveNotifyUpdateControl start.");
		txTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				try {
					for (final String type : UPDATE_TYPE_LIST) {
						if (!notifyUpdateControlMap.containsKey(type)) {
							continue;
						}
						List<NotifyUpdateControl> notifyUpdateControlList = notifyUpdateControlMap.get(type);
						List<String> transformList = Lists.transform(notifyUpdateControlList,
								new Function<NotifyUpdateControl, String>() {
									public String apply(NotifyUpdateControl nc) {
										return nc.getIdentifyNo();
									}
						});
						
						List<List<String>> partitionList = Lists.partition(transformList, UPDATE_MAX);
						for (List<String> list : partitionList) {
							log.info(type + " - before update count : " + list.size());
							updateNotifyUpdateControl(list);
							log.info(type + " - after update count : " + list.size());
						}
					}
				} catch (Exception e) {
					log.error("update error. rollback.", e);
					status.setRollbackOnly();
				}
			}
		});
		log.info("update slaveNotifyUpdateControl finish.");
	}

	private void initializeThreads() {
		makerMasterThreadPoolExecutor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		productBrandThreadPoolExecutor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		itemObjectUrlThreadPoolExecutor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		productDetailThreadPoolExecutor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		reviewPointThreadPoolExecutor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		reviewPointQuestThreadPoolExecutor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		reviewPointQuestDetailThreadPoolExecutor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		variationProductThreadPoolExecutor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		makerMasterCount = new AtomicInteger(0);
		productBrandCount = new AtomicInteger(0);
		itemObjectUrlCount = new AtomicInteger(0);
		productDetailCount = new AtomicInteger(0);
		reviewPointCount = new AtomicInteger(0);
		reviewPointQuestCount = new AtomicInteger(0);
		reviewPointQuestDetailCount = new AtomicInteger(0);
		variationProductCount = new AtomicInteger(0);
	}
	
	private void terminateThreads() {
		waitToCompleteThreadPoolExecutor("makerMasterThreadPoolExecutor", makerMasterThreadPoolExecutor);
		waitToCompleteThreadPoolExecutor("productBrandThreadPoolExecutor", productBrandThreadPoolExecutor);
		waitToCompleteThreadPoolExecutor("itemObjectUrlThreadPoolExecutor", itemObjectUrlThreadPoolExecutor);
		waitToCompleteThreadPoolExecutor("productDetailThreadPoolExecutor", productDetailThreadPoolExecutor);
		waitToCompleteThreadPoolExecutor("reviewPointThreadPoolExecutor", reviewPointThreadPoolExecutor);
		waitToCompleteThreadPoolExecutor("reviewPointQuestThreadPoolExecutor", reviewPointQuestThreadPoolExecutor);
		waitToCompleteThreadPoolExecutor("reviewPointQuestDetailThreadPoolExecutor", reviewPointQuestDetailThreadPoolExecutor);
		waitToCompleteThreadPoolExecutor("variationProductThreadPoolExecutor", variationProductThreadPoolExecutor);
		
		makerMasterThreadPoolExecutor = null;
		productBrandThreadPoolExecutor = null;
		itemObjectUrlThreadPoolExecutor = null;
		productDetailThreadPoolExecutor = null;
		reviewPointThreadPoolExecutor = null;
		reviewPointQuestThreadPoolExecutor = null;
		reviewPointQuestDetailThreadPoolExecutor = null;
		variationProductThreadPoolExecutor = null;
	}
	
	private void solrCommitAll() {
		solrOperations.optimize(DBMakerMasterDO.class);
		solrOperations.optimize(DBProductBrandDO.class);
		solrOperations.optimize(DBItemObjectUrlDO.class);
		solrOperations.optimize(DBMakerMasterDO.class);
		solrOperations.optimize(DBProductDetailDO.class);
		solrOperations.optimize(DBProductBrandDO.class);
		solrOperations.optimize(DBReviewPointQuestDO.class);
		solrOperations.optimize(DBReviewPointDO.class);
		solrOperations.optimize(DBReviewPointQuestDetailDO.class);
		solrOperations.optimize(DBVariationProductDO.class);
		
	}
	
	private void updateNotifyUpdateControl(List<String> identifyNoList) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE SLAVENOTIFYUPDATECONTROL");
		sql.append(" SET TOKYODBUPDATESTATUS = ?, OSAKADBUPDATESTATUS = ?, LASTUPDATE = SYSDATE");
		sql.append(" WHERE IDENTIFYNO IN (");
		sql.append(createInClause(identifyNoList.size()));
		sql.append(" )");
		Object[] args = ObjectArrays.concat(
				new Object[] { TOKYODBUPDATESTATUS_UPDATED, OSAKADBUPDATESTATUS_UPDATED, },
				identifyNoList.toArray(new String[identifyNoList.size()]),
				Object.class);
		template.update(sql.toString(), args);
	}
	
	@ArroundSolr
	public void loadMakerMaster() {
		saveMakerMaster();
	}
	
	@ArroundSolr
	public void loadProductBrand() {
		saveProductBrand();
	}
	@Override
	public void loadProductsByGroup(Integer orahash) {
		saveItemObjectUrl(orahash);
		saveProductDetail(orahash);
		// TODO saveVariationProduct(orahash);
		final Map<String, DBReviewPointDO> reviewPointMap = loadReviewPoint(orahash);
		saveReviewPoints(orahash, reviewPointMap);
	}
	@Override
	public void loadProductsBySku(String[] skus) {
		List<String> skuList = new ArrayList<String>();
		
		for(String sku : skus )
			skuList.add(sku);
		
		saveItemObjectUrl(skuList);
		saveProductDetail(skuList);
		// TODO saveVariationProduct(skuList);
		final Map<String, DBReviewPointDO> reviewPointMap = loadReviewPoint(skuList);
		saveReviewPoints(1, skuList, reviewPointMap);
	}
	@Override
	public void loadVariationProductsByGroup(Integer orahash) {
		saveVariationProduct(orahash);
	}
	@Override
	public void loadVariationProductsBySku(String[] skus) {
		List<String> skuList = new ArrayList<String>();
		
		for(String sku : skus ){
			skuList.add(sku);
			
			if( skuList.size() == 1000){
				log.info("Load 1000 sku");
				saveVariationProduct(skuList);
				skuList.clear();
			}
		}
		
		if( !skuList.isEmpty()){
			log.info("Load finish sku");
			saveVariationProduct(skuList);
		}
	}

	private static String createInClause(int size) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append("?");
		}
		return sb.toString();
	}
	/**
	 * makerMasterから全件取得し、保存します。
	 */
	private void saveMakerMaster() {
		saveMakerMaster(null);
	}
	private void saveMakerMaster(List<String> makerCdList) {
		final List<DBMakerMasterDO> makerMasterList = Lists.newArrayList();
		final Date date = timestampHolder.getTimestamp();
		Object[] args = null;
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT");
		sql.append(" MAKERCD, MAKERNAME, KANANAME, ALPHANAME, PROPERNAME, LASTUPDATE");
		sql.append(" FROM MAKERMASTER");
		if (makerCdList != null) {
			sql.append(" WHERE MAKERCD IN (");
			sql.append(createInClause(makerCdList.size()));
			sql.append(")");
			args = makerCdList.toArray(new String[makerCdList.size()]);
		}

		template.query(
				sql.toString(),
				args,
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						DBMakerMasterDO makerMasterDO = new DBMakerMasterDO();
						String makerCode = rs.getString("MAKERCD");
						makerMasterDO.setMakerMasterId(StringUtil.toSHA256(makerCode));
						makerMasterDO.setMakerCode(makerCode);
						makerMasterDO.setMakerName(rs.getString("MAKERNAME"));
						makerMasterDO.setKanaName(rs.getString("KANANAME"));
						makerMasterDO.setAlphaNema(rs.getString("ALPHANAME"));
						makerMasterDO.setProperName(rs.getString("PROPERNAME"));
						makerMasterDO.setLastUpdate(rs.getTimestamp("LASTUPDATE"));
						makerMasterDO.setModifyDateTime(date);
						makerMasterDO.setRegisterDateTime(date);

						makerMasterList.add(makerMasterDO);
					}
				});
		
		if (makerCdList == null) {
			doSaveAll(DBMakerMasterDO.class, makerMasterList, makerMasterThreadPoolExecutor);
		} else {
			doSaveWithThreadPoolExecutor(null, DBMakerMasterDO.class, makerMasterList, makerMasterThreadPoolExecutor, makerMasterCount);
		}
	}
	
	/**
	 * productBandから全件取得し、保存します。
	 */
	private void saveProductBrand() {
		saveProductBrand(null);
	}
	private void saveProductBrand(List<String> brandCodeList) {
		final List<DBProductBrandDO> productBrandList = Lists.newArrayList();
		final Date date = timestampHolder.getTimestamp();
		Object[] args = null;
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT");
		sql.append(" BRANDCODE, BRANDNAME, BRANDREADING, ORDERNO, LASTUPDATE");
		sql.append(" FROM PRODUCTBRAND");
		if (brandCodeList != null) {
			sql.append(" WHERE BRANDCODE IN (");
			sql.append(createInClause(brandCodeList.size()));
			sql.append(")");
			args = brandCodeList.toArray(new String[brandCodeList.size()]);
		}

		template.query(
				sql.toString(),
				args,
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						DBProductBrandDO productBrandDO = new DBProductBrandDO();
						String brandCode = rs.getString("BRANDCODE");
						productBrandDO.setProductBrandId(StringUtil.toSHA256(brandCode));
						productBrandDO.setBrandCode(brandCode);
						productBrandDO.setBrandName(rs.getString("BRANDNAME"));
						productBrandDO.setBrandReading(rs.getString("BRANDREADING"));
						productBrandDO.setOrderNo(rs.getLong("ORDERNO"));
						productBrandDO.setLastUpdate(rs.getTimestamp("LASTUPDATE"));
						productBrandDO.setModifyDateTime(date);
						productBrandDO.setRegisterDateTime(date);
						
						productBrandList.add(productBrandDO);
					}
				});
		
		if (brandCodeList == null) {
			doSaveAll(DBProductBrandDO.class, productBrandList, productBrandThreadPoolExecutor);
		} else {
			doSaveWithThreadPoolExecutor(null, DBProductBrandDO.class, productBrandList, productBrandThreadPoolExecutor, productBrandCount);
		}
	}
	
	/**
	 * ItemObjectUrlを全件取得し、保存します。
	 * @param orahash
	 */
	private void saveItemObjectUrl(Integer orahash) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT");
		sql.append(" SKU, OBJECTTYPEDCD, TITLE, ALT, NOTE, PRIORITYLEVE, LASTUPDATE, REPLACECOUNT");
		sql.append(" FROM ITEMOBJECTURL");
		sql.append(" WHERE ORA_HASH(SKU, ?) = ?");
		sql.append(" AND OBJECTTYPEDCD IN (?, ?)");
		
		Object[] args = new Object[] { ORAHASH_MAX, orahash, OBJECT_TYPED_CD_MAIN_RESIZE_BIG, OBJECT_TYPED_CD_LIST_BIG };
		
		saveItemObjectUrl(orahash, sql.toString(), args);
	}
	
	private void saveItemObjectUrl(List<String> skuList) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT");
		sql.append(" SKU, OBJECTTYPEDCD, TITLE, ALT, NOTE, PRIORITYLEVE, LASTUPDATE, REPLACECOUNT");
		sql.append(" FROM ITEMOBJECTURL");
		sql.append(" WHERE SKU IN (");
		sql.append(createInClause(skuList.size()));
		sql.append(")");
		sql.append(" AND OBJECTTYPEDCD IN (?, ?)");
		
		Object[] args = ObjectArrays.concat(skuList.toArray(new String[skuList.size()]),
				new Object[] { OBJECT_TYPED_CD_MAIN_RESIZE_BIG, OBJECT_TYPED_CD_LIST_BIG },
				Object.class);
		
		saveItemObjectUrl(null, sql.toString(), args);
	}
	
	private void saveItemObjectUrl(Integer orahash, String sql, Object[] args) {
		final List<DBItemObjectUrlDO> itemObjectUrlList = Lists.newArrayList();
		final Date date = timestampHolder.getTimestamp();
		
		template.query(sql, args,
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						DBItemObjectUrlDO itemObjectUrlDO = new DBItemObjectUrlDO();
						String sku = rs.getString("SKU");
						String objectTypedCd = rs.getString("OBJECTTYPEDCD");
						itemObjectUrlDO.setItemObujectUrlId(createItemObjectUrlId(sku, objectTypedCd));
						itemObjectUrlDO.setSku(sku);
						itemObjectUrlDO.setObjectTypedCode(objectTypedCd);
						itemObjectUrlDO.setTitle(rs.getString("TITLE"));
						itemObjectUrlDO.setAlt(rs.getString("ALT"));
						itemObjectUrlDO.setNote(rs.getString("NOTE"));
						itemObjectUrlDO.setPriorityLeve(rs.getInt("PRIORITYLEVE"));
						itemObjectUrlDO.setLastUpdate(rs.getTimestamp("LASTUPDATE"));
						itemObjectUrlDO.setReplaceCount(rs.getString("REPLACECOUNT"));
						itemObjectUrlDO.setReplaceCount(null);
						itemObjectUrlDO.setModifyDateTime(date);
						itemObjectUrlDO.setRegisterDateTime(date);
						
						itemObjectUrlList.add(itemObjectUrlDO);
					}
				});

		doSaveWithThreadPoolExecutor(orahash, DBItemObjectUrlDO.class, itemObjectUrlList, itemObjectUrlThreadPoolExecutor, itemObjectUrlCount);
	}
	
	/**
	 * ProductDetailを全件取得し、保存します。
	 * @param orahash
	 * @param makerMap
	 * @param brandMap
	 */
	private void saveProductDetail(Integer orahash) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT");
		sql.append(" P.SKU AS SKU, P.STARTTIME AS STARTTIME, P.ENDTIME AS ENDTIME, P.JANCODE AS JANCODE, P.PRODUCTNAME AS PRODUCTNAME,");
		sql.append(" P.PRODUCTTYPE AS PRODUCTTYPE, P.SETPRODUCTFLAG AS SETPRODUCTFLAG, P.COMPLEXFLAG AS COMPLEXFLAG,");
		sql.append(" P.FAMILYFLAG AS FAMILYFLAG, P.SERVICEFLAG AS SERVICEFLAG, P.MAKERCODE AS MAKERCODE, P.BRANDCODE AS BRANDCODE,");
		sql.append(" P.LISTSUMMARY AS LISTSUMMARY, P.ENABLEFLAG AS ENABLEFLAG, P.URGENTSTOPFLG AS URGENTSTOPFLG,");
		sql.append(" P.LASTUPDATE AS LASTUPDATE, P.CEROKIND AS CEROKIND, P.ADULTKIND AS ADULTKIND");
		sql.append(" FROM PRODUCTDETAIL P");
		sql.append(" LEFT OUTER JOIN ITEMINFO I ON (P.SKU = I.SKU)");
		sql.append(" WHERE ORA_HASH(P.SKU, ?) = ?");
		sql.append(" AND P.CHANNELTYPE = ?");
		sql.append(" AND P.FAMILYFLAG IN (?, ?)");
		sql.append(" AND TO_CHAR(P.STARTTIME, 'YYYYMMDD') != ?");
		sql.append(" AND (P.STARTTIME > SYSDATE OR (SYSDATE BETWEEN P.STARTTIME AND P.ENDTIME))");
		
		Object[] args = new Object[] { ORAHASH_MAX, orahash, CHANNEL_TYPE_PC, FAMILY_FLAG_NONE, FAMILY_FLAG_CHILD, INVALID_DATE_TEXT };
		
		saveProductDetail(orahash, sql.toString(), args);
	}
	/**
	 * skuが一致するProductDetailを取得し、保存します。
	 * @param skuList
	 */
	private void saveProductDetail(List<String> skuList) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT");
		sql.append(" P.SKU AS SKU, P.STARTTIME AS STARTTIME, P.ENDTIME AS ENDTIME, P.JANCODE AS JANCODE, P.PRODUCTNAME AS PRODUCTNAME,");
		sql.append(" P.PRODUCTTYPE AS PRODUCTTYPE, P.SETPRODUCTFLAG AS SETPRODUCTFLAG, P.COMPLEXFLAG AS COMPLEXFLAG,");
		sql.append(" P.FAMILYFLAG AS FAMILYFLAG, P.SERVICEFLAG AS SERVICEFLAG, P.MAKERCODE AS MAKERCODE, P.BRANDCODE AS BRANDCODE,");
		sql.append(" P.LISTSUMMARY AS LISTSUMMARY, P.ENABLEFLAG AS ENABLEFLAG, P.URGENTSTOPFLG AS URGENTSTOPFLG,");
		sql.append(" P.LASTUPDATE AS LASTUPDATE, P.CEROKIND AS CEROKIND, P.ADULTKIND AS ADULTKIND");
		sql.append(" FROM PRODUCTDETAIL P");
		sql.append(" LEFT OUTER JOIN ITEMINFO I ON (P.SKU = I.SKU)");
		sql.append(" WHERE P.SKU IN (");
		sql.append(createInClause(skuList.size()));
		sql.append(")");
		sql.append(" AND P.CHANNELTYPE = ?");
		sql.append(" AND P.FAMILYFLAG IN (?, ?)");
		sql.append(" AND TO_CHAR(P.STARTTIME, 'YYYYMMDD') != ?");
		sql.append(" AND (P.STARTTIME > SYSDATE OR (SYSDATE BETWEEN P.STARTTIME AND P.ENDTIME))");
		
		Object[] args = ObjectArrays.concat(skuList.toArray(new String[skuList.size()]),
				new Object[] { CHANNEL_TYPE_PC, FAMILY_FLAG_NONE, FAMILY_FLAG_CHILD, INVALID_DATE_TEXT },
				Object.class);
		
		saveProductDetail(null, sql.toString(), args);
	}
	
	private void saveProductDetail(Integer orahash, String sql, Object[] args) {
		final List<DBProductDetailDO> productDetailList = Lists.newArrayList();
		final Date date = timestampHolder.getTimestamp();
		
		template.query(sql, args,
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						DBProductDetailDO productDetailDO = new DBProductDetailDO();
						String sku = rs.getString("SKU");
						Date endTime = rs.getTimestamp("EndTime");
						productDetailDO.setProductDetailId(createProductDetailId(sku, endTime));
						productDetailDO.setSku(sku);
						productDetailDO.setStartTime(rs.getTimestamp("STARTTIME"));
						productDetailDO.setEndTime(endTime);
						productDetailDO.setJanCode(rs.getString("JANCODE"));
						productDetailDO.setProductName(rs.getString("PRODUCTNAME"));
						productDetailDO.setProductNameShort(null);
						productDetailDO.setProductType(ProductType.codeOf(rs.getString("PRODUCTTYPE")));
						productDetailDO.setSetProductFlag(rs.getInt("SETPRODUCTFLAG"));
						productDetailDO.setComplexFlag(rs.getInt("COMPLEXFLAG"));
						productDetailDO.setFamilyFlag(rs.getInt("FAMILYFLAG"));
						productDetailDO.setServiceFlag(rs.getInt("SERVICEFLAG"));
						String makerCode = rs.getString("MAKERCODE");
						if (!StringUtils.isEmpty(makerCode)) {
							DBMakerMasterDO maker = new DBMakerMasterDO();
							maker.setMakerMasterId(StringUtil.toSHA256(makerCode));
							productDetailDO.setMaker(maker);
						}
						String brandCode = rs.getString("BRANDCODE");
						if (!StringUtils.isEmpty(brandCode)) {
							DBProductBrandDO brand = new DBProductBrandDO();
							brand.setProductBrandId(StringUtil.toSHA256(brandCode));
							productDetailDO.setBrand(brand);
						}
						productDetailDO.setListSummary(rs.getString("LISTSUMMARY"));
						productDetailDO.setEnableFlag(rs.getInt("ENABLEFLAG"));
						productDetailDO.setUrgentStopFlg(rs.getString("URGENTSTOPFLG"));
						productDetailDO.setLastUpdate(rs.getTimestamp("LASTUPDATE"));
						productDetailDO.setCeroKind(rs.getString("CEROKIND"));
						productDetailDO.setAdultKind(rs.getString("ADULTKIND"));
						productDetailDO.setModifyDateTime(date);
						productDetailDO.setRegisterDateTime(date);

						productDetailList.add(productDetailDO);
					}
				});
		
		doSaveWithThreadPoolExecutor(orahash, DBProductDetailDO.class, productDetailList, productDetailThreadPoolExecutor, productDetailCount);
	}

	
	private void saveVariationProduct(Integer orahash){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT");
		sql.append(" R.SKU AS SKU, R.RELATEDSKU AS RELATEDSKU, R.STARTTIME AS STARTTIME, R.ENDTIME AS ENDTIME, R.LASTUPDATE AS LASTUPDATE");
		sql.append(" FROM PATTRRELATEDITEM R");
		sql.append(" WHERE ORA_HASH(R.SKU, ?) = ?");
		sql.append(" AND R.CHANNELTYPE = ?");
		sql.append(" AND R.RELATEDPCATEGORYCODE = '0012'");
		sql.append(" AND TO_CHAR(R.STARTTIME, 'YYYYMMDD') != ?");
		sql.append(" AND (R.STARTTIME > SYSDATE OR (SYSDATE BETWEEN R.STARTTIME AND R.ENDTIME))");
		sql.append(" ORDER BY R.SKU, R.RELATEDSKU");
		
		Object[] args = new Object[] { ORAHASH_MAX, orahash, CHANNEL_TYPE_PC, INVALID_DATE_TEXT };
		
		saveVariationProduct(orahash, sql.toString(), args);
	}
	
	private void saveVariationProduct(List<String> skuList) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT");
		sql.append(" R.SKU AS SKU, R.RELATEDSKU AS RELATEDSKU, R.STARTTIME AS STARTTIME, R.ENDTIME AS ENDTIME, R.LASTUPDATE AS LASTUPDATE");
		sql.append(" FROM PATTRRELATEDITEM R");
		sql.append(" WHERE R.SKU IN (");
		sql.append(createInClause(skuList.size()));
		sql.append(")");
		sql.append(" AND R.CHANNELTYPE = ?");
		sql.append(" AND R.RELATEDPCATEGORYCODE = '0012'");
		sql.append(" AND TO_CHAR(R.STARTTIME, 'YYYYMMDD') != ?");
		sql.append(" AND (R.STARTTIME > SYSDATE OR (SYSDATE BETWEEN R.STARTTIME AND R.ENDTIME))");
		sql.append(" ORDER BY R.SKU, R.RELATEDSKU");
		
		Object[] args = ObjectArrays.concat(skuList.toArray(new String[skuList.size()]),
				new Object[] { CHANNEL_TYPE_PC, INVALID_DATE_TEXT },
				Object.class);
		
		saveVariationProduct(null, sql.toString(), args);
	}
	
	private void saveVariationProduct(Integer orahash, String sql, Object[] args) {
		final List<DBVariationProductDO> variationProductList = Lists.newArrayList();
		final Date date = timestampHolder.getTimestamp();
		final Map<String, VariantProduct> variationProductMap = Maps.newHashMap();
		
		template.query(sql, args,
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						String sku = rs.getString("SKU");
						Date endTime = rs.getTimestamp("EndTime");
						String productDetailId = createProductDetailId(sku, endTime);
						
						VariantProduct variantProduct = null;
						
						if( variationProductMap.containsKey(productDetailId) ){
							variantProduct = variationProductMap.get(productDetailId);
						}else{
							variantProduct = new VariantProduct();
							variantProduct.setSku(sku);
							variantProduct.setStartTime(rs.getTimestamp("STARTTIME"));
							variantProduct.setEndTime(rs.getTimestamp("ENDTIME"));
							variantProduct.setLastUpdate(rs.getTimestamp("LASTUPDATE"));
							variationProductMap.put(productDetailId, variantProduct);
						}
						
						variantProduct.getRelatedSkuList().add(rs.getString("RELATEDSKU"));
					}
				});
		
		DBVariationProductDO variationProductDO = null;
		VariantProduct variantProduct = null;
		List<String> relatefdSkuList = null;
		log.info("Load Product Size:" + variationProductMap.size());
		for (Iterator<Map.Entry<String, VariantProduct>> it = variationProductMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, VariantProduct> entry = it.next();
			variantProduct = entry.getValue();
			relatefdSkuList = variantProduct.getRelatedSkuList();
			Collections.sort(relatefdSkuList);
			
			variationProductDO = new DBVariationProductDO();
			variationProductDO.setProductDetailId(entry.getKey());
			variationProductDO.setSku(variantProduct.getSku());
			variationProductDO.setVariationProducts(relatefdSkuList);
			variationProductDO.setStartTime(variantProduct.getStartTime());
			variationProductDO.setEndTime(variantProduct.getEndTime());
			variationProductDO.setLastUpdate(variantProduct.getLastUpdate());
			variationProductDO.setModifyDateTime(date);
			variationProductDO.setRegisterDateTime(date);

			variationProductList.add(variationProductDO);
		}
		
		doSaveWithThreadPoolExecutor(orahash, DBVariationProductDO.class, variationProductList, variationProductThreadPoolExecutor, variationProductCount);
	}
	/**
	 * ReviewPointを取得します。
	 * @param orahash
	 * @return
	 */
	private Map<String, DBReviewPointDO> loadReviewPoint(Integer orahash) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT");
		sql.append(" PD.SKU AS SKU,");
		sql.append(" R.STARTTIME AS STARTTIME, R.ENDTIME AS ENDTIME, R.REVIEWPOINTWHETHERFLAG AS REVIEWPOINTWHETHERFLAG,");
		sql.append(" R.REVIEWCOMMENT AS REVIEWCOMMENT, R.POINTCALCTYPE AS POINTCALCTYPE, R.INITIALPOSTTERM AS INITIALPOSTTERM,");
		sql.append(" R.CONTINUATIONPOSTTIMES AS CONTINUATIONPOSTTIMES, R.CONTINUATIONPOSTTERM AS CONTINUATIONPOSTTERM,");
		sql.append(" R.LASTUPDATE AS LASTUPDATE");
		sql.append(" FROM");
		sql.append(" (");
		sql.append(" SELECT P.SKU AS SKU, P.SKU AS RELATEDSKU FROM PRODUCTDETAIL P");
		sql.append(" WHERE ORA_HASH(P.SKU, ?) = ?");
		sql.append(" AND P.CHANNELTYPE = ?");
		sql.append(" AND P.COMPLEXFLAG = ?");
		sql.append(" AND P.FAMILYFLAG IN (?, ?)");
		sql.append(" AND TO_CHAR(P.STARTTIME, 'YYYYMMDD') != ?");
		sql.append(" AND (SYSDATE BETWEEN P.STARTTIME AND P.ENDTIME)");
		sql.append(" UNION ALL");
		sql.append(" SELECT P.SKU, I.RELATEDSKU FROM PRODUCTDETAIL P, PATTRRELATEDITEM I");
		sql.append(" WHERE ORA_HASH(P.SKU, ?) = ?");
		sql.append(" AND P.CHANNELTYPE = ?");
		sql.append(" AND P.COMPLEXFLAG = ?");
		sql.append(" AND P.FAMILYFLAG IN (?, ?)");
		sql.append(" AND TO_CHAR(P.STARTTIME, 'YYYYMMDD') != ?");
		sql.append(" AND (SYSDATE BETWEEN P.STARTTIME AND P.ENDTIME)");
		sql.append(" AND P.SKU = I.SKU");
		sql.append(" AND P.CHANNELTYPE = I.CHANNELTYPE");
		sql.append(" AND I.RELATEDPCATEGORYCODE = ?");
		sql.append(" ) PD");
		sql.append(" INNER JOIN REVIEWPOINT R ON (PD.RELATEDSKU = R.SKU)");
		sql.append(" WHERE TO_CHAR(R.STARTTIME, 'YYYYMMDD') != ?");
		sql.append(" AND (R.STARTTIME > SYSDATE OR (SYSDATE BETWEEN R.STARTTIME AND R.ENDTIME))");
		
		Object[] args = new Object[] {
				ORAHASH_MAX, orahash, CHANNEL_TYPE_PC, COMPLEX_FLAG_NONE, FAMILY_FLAG_NONE, FAMILY_FLAG_CHILD, INVALID_DATE_TEXT,
				ORAHASH_MAX, orahash, CHANNEL_TYPE_PC, COMPLEX_FLAG_PARENT, FAMILY_FLAG_NONE, FAMILY_FLAG_CHILD, INVALID_DATE_TEXT,
				RELATEDPCATEGORYCODE_COMPLEX, INVALID_DATE_TEXT
		};
		
		return loadReviewPoint(sql.toString(), args);
	}
	private Map<String, DBReviewPointDO> loadReviewPoint(List<String> skuList) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT");
		sql.append(" PD.SKU AS SKU,");
		sql.append(" R.STARTTIME AS STARTTIME, R.ENDTIME AS ENDTIME, R.REVIEWPOINTWHETHERFLAG AS REVIEWPOINTWHETHERFLAG,");
		sql.append(" R.REVIEWCOMMENT AS REVIEWCOMMENT, R.POINTCALCTYPE AS POINTCALCTYPE, R.INITIALPOSTTERM AS INITIALPOSTTERM,");
		sql.append(" R.CONTINUATIONPOSTTIMES AS CONTINUATIONPOSTTIMES, R.CONTINUATIONPOSTTERM AS CONTINUATIONPOSTTERM,");
		sql.append(" R.LASTUPDATE AS LASTUPDATE");
		sql.append(" FROM");
		sql.append(" (");
		sql.append(" SELECT P.SKU AS SKU, P.SKU AS RELATEDSKU FROM PRODUCTDETAIL P");
		sql.append(" WHERE P.CHANNELTYPE = ?");
		sql.append(" AND P.COMPLEXFLAG = ?");
		sql.append(" AND P.FAMILYFLAG IN (?, ?)");
		sql.append(" AND TO_CHAR(P.STARTTIME, 'YYYYMMDD') != ?");
		sql.append(" AND (SYSDATE BETWEEN P.STARTTIME AND P.ENDTIME)");
		sql.append(" UNION ALL");
		sql.append(" SELECT P.SKU, I.RELATEDSKU FROM PRODUCTDETAIL P, PATTRRELATEDITEM I");
		sql.append(" WHERE P.CHANNELTYPE = ?");
		sql.append(" AND P.COMPLEXFLAG = ?");
		sql.append(" AND P.FAMILYFLAG IN (?, ?)");
		sql.append(" AND TO_CHAR(P.STARTTIME, 'YYYYMMDD') != ?");
		sql.append(" AND (SYSDATE BETWEEN P.STARTTIME AND P.ENDTIME)");
		sql.append(" AND P.SKU = I.SKU");
		sql.append(" AND P.CHANNELTYPE = I.CHANNELTYPE");
		sql.append(" AND I.RELATEDPCATEGORYCODE = ?");
		sql.append(" ) PD");
		sql.append(" INNER JOIN REVIEWPOINT R ON (PD.RELATEDSKU = R.SKU)");
		sql.append(" WHERE R.SKU IN (");
		sql.append(createInClause(skuList.size()));
		sql.append(")");
		sql.append(" AND TO_CHAR(R.STARTTIME, 'YYYYMMDD') != ?");
		sql.append(" AND (R.STARTTIME > SYSDATE OR (SYSDATE BETWEEN R.STARTTIME AND R.ENDTIME))");
		
		
		Object[] first = new Object[] {
				CHANNEL_TYPE_PC, COMPLEX_FLAG_NONE, FAMILY_FLAG_NONE, FAMILY_FLAG_CHILD, INVALID_DATE_TEXT,
				CHANNEL_TYPE_PC, COMPLEX_FLAG_PARENT, FAMILY_FLAG_NONE, FAMILY_FLAG_CHILD, INVALID_DATE_TEXT,
				RELATEDPCATEGORYCODE_COMPLEX,
				};
		Object[] second = skuList.toArray(new String[skuList.size()]);
		Object[] last = new Object[] { INVALID_DATE_TEXT };
		
		Object[] args = ObjectArrays.concat(ObjectArrays.concat(first, second, Object.class), last, Object.class);
		
		return loadReviewPoint(sql.toString(), args);
	}
	private Map<String, DBReviewPointDO> loadReviewPoint(String sql, Object[] args) {
		final Map<String, DBReviewPointDO> reviewPointMap = Maps.newHashMap();
		final Date date = timestampHolder.getTimestamp();
		
		template.query(sql, args,
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						DBReviewPointDO reviewPointDO = new DBReviewPointDO();
						String sku = rs.getString("SKU");
						Date endTime = rs.getTimestamp("ENDTIME");
						reviewPointDO.setReviewPointId(createReviewPointId(sku, endTime));
						reviewPointDO.setSku(sku);
						reviewPointDO.setStartTime(rs.getTimestamp("STARTTIME"));
						reviewPointDO.setEndTime(endTime);
						reviewPointDO.setReviewPointWhetherFlag(rs.getInt("REVIEWPOINTWHETHERFLAG"));
						reviewPointDO.setReviewComment(rs.getString("REVIEWCOMMENT"));
						reviewPointDO.setPointCalcType(rs.getInt("POINTCALCTYPE"));
						reviewPointDO.setInitialPostTerm(rs.getInt("INITIALPOSTTERM"));
						reviewPointDO.setContinuationPostTimes(rs.getInt("CONTINUATIONPOSTTIMES"));
						reviewPointDO.setContinuationPostTerm(rs.getInt("CONTINUATIONPOSTTERM"));
						reviewPointDO.setLastUpdate(rs.getTimestamp("LASTUPDATE"));
						reviewPointDO.setModifyDateTime(date);
						reviewPointDO.setRegisterDateTime(date);
						
						reviewPointMap.put(reviewPointDO.getReviewPointId(), reviewPointDO);
					}
				});
		
		return reviewPointMap;
	}

	/**
	 * 商品レビューポイント情報を保存します（reviewPoint, reviewPointQuest, reviewPointQuestDetail）。
	 * @param orahash
	 * @param reviewPointList
	 */
	private void saveReviewPoints(Integer orahash, final Map<String, DBReviewPointDO> reviewPointMap) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT");
		sql.append(" PD.SKU AS SKU,");
		sql.append(" Q.STARTTIME AS STARTTIME, Q.ENDTIME AS ENDTIME, Q.RQCODE AS RQCODE, Q.RQSTARTTIME AS RQSTARTTIME,");
		sql.append(" Q.RQENDTIME AS RQENDTIME, Q.ORDERNO AS ORDERNO, Q.BASEREVIEWPOINTVALUES AS BASEREVIEWPOINTVALUES,");
		sql.append(" Q.LASTUPDATE AS LASTUPDATE");
		sql.append(" FROM");
		sql.append(" (");
		sql.append(" SELECT P.SKU AS SKU, P.SKU AS RELATEDSKU FROM PRODUCTDETAIL P");
		sql.append(" WHERE ORA_HASH(P.SKU, ?) = ?");
		sql.append(" AND P.CHANNELTYPE = ?");
		sql.append(" AND P.COMPLEXFLAG = ?");
		sql.append(" AND P.FAMILYFLAG IN (?, ?)");
		sql.append(" AND TO_CHAR(P.STARTTIME, 'YYYYMMDD') != ?");
		sql.append(" AND (SYSDATE BETWEEN P.STARTTIME AND P.ENDTIME)");
		sql.append(" UNION ALL");
		sql.append(" SELECT P.SKU, I.RELATEDSKU FROM PRODUCTDETAIL P, PATTRRELATEDITEM I");
		sql.append(" WHERE ORA_HASH(P.SKU, ?) = ?");
		sql.append(" AND P.CHANNELTYPE = ?");
		sql.append(" AND P.COMPLEXFLAG = ?");
		sql.append(" AND P.FAMILYFLAG IN (?, ?)");
		sql.append(" AND TO_CHAR(P.STARTTIME, 'YYYYMMDD') != ?");
		sql.append(" AND (SYSDATE BETWEEN P.STARTTIME AND P.ENDTIME)");
		sql.append(" AND P.SKU = I.SKU");
		sql.append(" AND P.CHANNELTYPE = I.CHANNELTYPE");
		sql.append(" AND I.RELATEDPCATEGORYCODE = ?");
		sql.append(" ) PD");
		sql.append(" INNER JOIN REVIEWPOINTQUEST Q ON (PD.RELATEDSKU = Q.SKU)");
		sql.append(" WHERE Q.RQCODE != ?");
		sql.append(" AND TO_CHAR(Q.STARTTIME, 'YYYYMMDD') != ?");
		sql.append(" AND (Q.STARTTIME > SYSDATE OR (SYSDATE BETWEEN Q.STARTTIME AND Q.ENDTIME))");
		
		Object[] args = new Object[] {
				ORAHASH_MAX, orahash, CHANNEL_TYPE_PC, COMPLEX_FLAG_NONE, FAMILY_FLAG_NONE, FAMILY_FLAG_CHILD, INVALID_DATE_TEXT,
				ORAHASH_MAX, orahash, CHANNEL_TYPE_PC, COMPLEX_FLAG_PARENT, FAMILY_FLAG_NONE, FAMILY_FLAG_CHILD, INVALID_DATE_TEXT,
				RQCODE_SPECIAL, RELATEDPCATEGORYCODE_COMPLEX, INVALID_DATE_TEXT
		};
		
		saveReviewPoints(orahash, sql.toString(), args, reviewPointMap);
	}
	private void saveReviewPoints(Integer orahash, List<String> skuList, final Map<String, DBReviewPointDO> reviewPointMap) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT");
		sql.append(" PD.SKU AS SKU,");
		sql.append(" Q.STARTTIME AS STARTTIME, Q.ENDTIME AS ENDTIME, Q.RQCODE AS RQCODE, Q.RQSTARTTIME AS RQSTARTTIME,");
		sql.append(" Q.RQENDTIME AS RQENDTIME, Q.ORDERNO AS ORDERNO, Q.BASEREVIEWPOINTVALUES AS BASEREVIEWPOINTVALUES,");
		sql.append(" Q.LASTUPDATE AS LASTUPDATE");
		sql.append(" FROM");
		sql.append(" (");
		sql.append(" SELECT P.SKU AS SKU, P.SKU AS RELATEDSKU FROM PRODUCTDETAIL P");
		sql.append(" WHERE P.CHANNELTYPE = ?");
		sql.append(" AND P.COMPLEXFLAG = ?");
		sql.append(" AND P.FAMILYFLAG IN (?, ?)");
		sql.append(" AND TO_CHAR(P.STARTTIME, 'YYYYMMDD') != ?");
		sql.append(" AND (SYSDATE BETWEEN P.STARTTIME AND P.ENDTIME)");
		sql.append(" UNION ALL");
		sql.append(" SELECT P.SKU, I.RELATEDSKU FROM PRODUCTDETAIL P, PATTRRELATEDITEM I");
		sql.append(" WHERE P.CHANNELTYPE = ?");
		sql.append(" AND P.COMPLEXFLAG = ?");
		sql.append(" AND P.FAMILYFLAG IN (?, ?)");
		sql.append(" AND TO_CHAR(P.STARTTIME, 'YYYYMMDD') != ?");
		sql.append(" AND (SYSDATE BETWEEN P.STARTTIME AND P.ENDTIME)");
		sql.append(" AND P.SKU = I.SKU");
		sql.append(" AND P.CHANNELTYPE = I.CHANNELTYPE");
		sql.append(" AND I.RELATEDPCATEGORYCODE = ?");
		sql.append(" ) PD");
		sql.append(" INNER JOIN REVIEWPOINTQUEST Q ON (PD.RELATEDSKU = Q.SKU)");
		sql.append(" WHERE Q.SKU IN (");
		sql.append(createInClause(skuList.size()));
		sql.append(")");
		sql.append(" AND Q.RQCODE != ?");
		sql.append(" AND TO_CHAR(Q.STARTTIME, 'YYYYMMDD') != ?");
		sql.append(" AND (Q.STARTTIME > SYSDATE OR (SYSDATE BETWEEN Q.STARTTIME AND Q.ENDTIME))");
		
		Object[] first = new Object[] {
				CHANNEL_TYPE_PC, COMPLEX_FLAG_NONE, FAMILY_FLAG_NONE, FAMILY_FLAG_CHILD, INVALID_DATE_TEXT,
				CHANNEL_TYPE_PC, COMPLEX_FLAG_PARENT, FAMILY_FLAG_NONE, FAMILY_FLAG_CHILD, INVALID_DATE_TEXT,
				RELATEDPCATEGORYCODE_COMPLEX,
				};
		Object[] second = skuList.toArray(new String[skuList.size()]);
		Object[] last = new Object[] { RQCODE_SPECIAL, INVALID_DATE_TEXT };
		
		Object[] args = ObjectArrays.concat(ObjectArrays.concat(first, second, Object.class), last, Object.class);
		
		saveReviewPoints(orahash, sql.toString(), args, reviewPointMap);
	}
	
	private void saveReviewPoints(Integer orahash, String sql, Object[] args, final Map<String, DBReviewPointDO> reviewPointMap) {
		final List<DBReviewPointQuestDO> reviewPointQuestList = Lists.newArrayList();
		final List<DBReviewPointQuestDetailDO> reviewPointQuestDetailList = Lists.newArrayList();
		final Date date = timestampHolder.getTimestamp();

		template.query(sql, args,
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						DBReviewPointQuestDO reviewPointQuestDO = new DBReviewPointQuestDO();
						String sku = rs.getString("SKU");
						Date endTime = rs.getTimestamp("ENDTIME");
						String reviewPointId = createReviewPointId(sku, endTime);
						if (reviewPointMap.containsKey(reviewPointId)) {
							
							String rqCode = rs.getString("RQCODE");
							Date rqEndTime = rs.getTimestamp("RQENDTIME");
							reviewPointQuestDO.setReviewPointQuestId(createReviewPointQuestId(sku, endTime, rqCode, rqEndTime));
							
							reviewPointQuestDO.setSku(sku);
							reviewPointQuestDO.setStartTime(rs.getTimestamp("STARTTIME"));
							reviewPointQuestDO.setEndTime(endTime);
							reviewPointQuestDO.setRqCode(rqCode);
							reviewPointQuestDO.setRqStartTime(rs.getTimestamp("RQSTARTTIME"));
							reviewPointQuestDO.setRqEndTime(rqEndTime);
							reviewPointQuestDO.setOrderNo(rs.getInt("ORDERNO"));
							reviewPointQuestDO.setLastUpdate(rs.getTimestamp("LASTUPDATE"));
							reviewPointQuestDO.setModifyDateTime(date);
							reviewPointQuestDO.setRegisterDateTime(date);
							reviewPointQuestList.add(reviewPointQuestDO);
							
							// 商品レビューポイント設問詳細情報を作成
							DBReviewPointDO reviewPointDO = reviewPointMap.get(reviewPointId);
							List<DBReviewPointQuestDetailDO> details = createReviewPointQuestDetails(
									reviewPointDO, reviewPointQuestDO, rs.getString("BASEREVIEWPOINTVALUES"));
							
							if (details != null) {
								reviewPointQuestDO.setReviewPointQuestDetails(details);
								reviewPointQuestDetailList.addAll(details);
							}
							reviewPointQuestDO.setReviewPoint(reviewPointDO);
							reviewPointDO.getReviewPointQuests().add(reviewPointQuestDO);
						}
					}
				});

		doSaveWithThreadPoolExecutor(orahash, DBReviewPointDO.class, Lists.newArrayList(reviewPointMap.values().iterator()), reviewPointThreadPoolExecutor, reviewPointCount);
		doSaveWithThreadPoolExecutor(orahash, DBReviewPointQuestDO.class, reviewPointQuestList, reviewPointQuestThreadPoolExecutor, reviewPointQuestCount);
		doSaveWithThreadPoolExecutor(orahash, DBReviewPointQuestDetailDO.class, reviewPointQuestDetailList, reviewPointQuestDetailThreadPoolExecutor, reviewPointQuestDetailCount);
	}
	
	/**
	 * solrにコミットします。
	 * @param type
	 * @param list
	 * @param threadPoolExecutor
	 */
	private <T> void doSaveAll(final Class<T> type, final List<T> list, ThreadPoolExecutor threadPoolExecutor) {
		if (list == null || list.isEmpty()) {
			log.info(type.getSimpleName() + " empty.");
			return;
		}
		if (threadPoolExecutor == null) {
			save(type, list);
			return;
		}
		
		threadPoolExecutor.execute(new Runnable(){
			@Override
			public void run() {
				save(type, list);
				solrOperations.commit(type);
			}
		});
	}
	
	/**
	 * 10万件単位でsolrにコミットします。
	 * @param orahash
	 * @param type
	 * @param list
	 * @param threadPoolExecutor
	 * @param counter
	 */
	private <T> void doSaveWithThreadPoolExecutor(final Integer orahash, final Class<T> type, final List<T> list, ThreadPoolExecutor threadPoolExecutor, final AtomicInteger counter) {
		if (list == null || list.isEmpty()) {
			log.info(type.getSimpleName() + " empty.");
			return;
		}
		if (threadPoolExecutor == null) {
			save(type, list);
			return;
		}
		
		threadPoolExecutor.execute(new Runnable(){
			@Override
			public void run() {
				save(type, list);
				int size = counter.addAndGet(list.size());
				int previous = size - list.size();
				log.info("YYY " + type.getSimpleName() + " size=" + size + " previous=" + previous + " orahash=" + orahash);
				if ((int)(size / 100000) > (int)(previous / 100000)) {
					long start = System.currentTimeMillis();
					log.info("commit start. " + type.getSimpleName() + " size=" + size + " orahash=" + orahash);
					solrOperations.commit(type);
					log.info("commit finish. " + type.getSimpleName() + " size=" + size + " elapsed=" + ((System.currentTimeMillis() - start) / 1000) + " orahash=" + orahash);
				}
			}
		});
	}
	
	/**
	 * 商品レビューポイント設問情報から、商品レビューポイント設問詳細情報のlistを作成します
	 * @return 商品レビューポイント設問詳細情報のlist
	 */
	private List<DBReviewPointQuestDetailDO> createReviewPointQuestDetails(
			DBReviewPointDO reviewPointDO, DBReviewPointQuestDO reviewPointQuestDO, String baseReviewPointValues) {
		// 以下のソースをみて実装しました。
		// 参考ソース:
		//   YC_CATALOG_CacheServer: com.kickmogu.yodobashi.catalog.coherence.misc.ProductReviewPointQuestionDetailUtils.java
		
		if (StringUtils.isEmpty(baseReviewPointValues) ||
				reviewPointDO.getInitialPostTerm() == null ||
				reviewPointDO.getContinuationPostTimes() == null ||
				reviewPointDO.getContinuationPostTerm() == null)
			return null;

		String[] reviewPointValues = baseReviewPointValues.split(",");
		
		List<DBReviewPointQuestDetailDO> list = Lists.newArrayList();
		if (reviewPointQuestDO.getRqCode().startsWith("A")) {
			int start = 1;
			if (reviewPointDO.getInitialPostTerm() > 0) {
				int end = reviewPointDO.getInitialPostTerm();
				long point = Long.valueOf(reviewPointValues[0]);
				list.add(createReviewPointQuestDetail(reviewPointQuestDO, start, end, point));
			}
		} else {
			for (int i = 0; i < reviewPointDO.getContinuationPostTimes(); i++) {
				int start = (i * reviewPointDO.getContinuationPostTerm()) + reviewPointDO.getInitialPostTerm() + 1;
				int end = start + reviewPointDO.getContinuationPostTerm() - 1;
				String point = "0";
				if (reviewPointValues.length > i) {
					point = reviewPointValues[i];
				}
				list.add(createReviewPointQuestDetail(reviewPointQuestDO, start, end, Long.valueOf(point)));
			}
		}
		
		return list;
	}

	/**
	 * 商品レビューポイント設問詳細情報を作成します
	 * @param reviewPointQuest
	 * @param start
	 * @param end
	 * @param point
	 * @return 商品レビューポイント設問詳細情報
	 */
	private DBReviewPointQuestDetailDO createReviewPointQuestDetail(DBReviewPointQuestDO reviewPointQuest, int start, int end, long point) {
		DBReviewPointQuestDetailDO reviewPointQuestDetailDO = new DBReviewPointQuestDetailDO();
		reviewPointQuestDetailDO.setReviewPointQuestDetailId(createReviewPointQuestDetailId(reviewPointQuest, end));
		reviewPointQuestDetailDO.setSku(reviewPointQuest.getSku());
		reviewPointQuestDetailDO.setStartTime(reviewPointQuest.getStartTime());
		reviewPointQuestDetailDO.setEndTime(reviewPointQuest.getEndTime());
		reviewPointQuestDetailDO.setRqCode(reviewPointQuest.getRqCode());
		reviewPointQuestDetailDO.setRqStartTime(reviewPointQuest.getRqStartTime());
		reviewPointQuestDetailDO.setRqEndTime(reviewPointQuest.getRqEndTime());
		reviewPointQuestDetailDO.setRqStartThreshold(start);
		reviewPointQuestDetailDO.setRqEndThreshold(end);
		reviewPointQuestDetailDO.setRqBaseReviewPointValue(point);
		reviewPointQuestDetailDO.setLastUpdate(reviewPointQuest.getLastUpdate());
		reviewPointQuestDetailDO.setModifyDateTime(reviewPointQuest.getModifyDateTime());
		reviewPointQuestDetailDO.setRegisterDateTime(reviewPointQuest.getRegisterDateTime());
		
		reviewPointQuestDetailDO.setReviewPointQuest(reviewPointQuest);
		
		return reviewPointQuestDetailDO;
	}
	
	@Override
	public void updateInvoker(String updateType, List<?> list) throws Exception {
		Method method = this.getClass().getDeclaredMethod("update" + updateType, List.class);
		method.setAccessible(true);
		method.invoke(this, list);
	}
	
	/**
	 * 更新情報を取得します
	 * @param cacheKey
	 * @return 更新情報のリスト
	 */
	private Map<String, List<NotifyUpdateControl>> loadNotifyUpdateControl() {
		final Map<String, List<NotifyUpdateControl>> notifyUpdateControlMap = Maps.newHashMap();
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT");
		sql.append(" IDENTIFYNO, COHERENCECACHE, COHERENCEKEY");
		sql.append(" FROM SLAVENOTIFYUPDATECONTROL");
		sql.append(" WHERE TOKYODBUPDATESTATUS = ?");
		sql.append(" AND OSAKADBUPDATESTATUS = ?");
		sql.append(" AND COHERENCECACHE LIKE ?");
		
		Object[] args = new Object[] { TOKYODBUPDATESTATUS_INIT, OSAKADBUPDATESTATUS_INIT, UPDATE_TYPE_PREFIX + "%" };
		
		template.query(
				sql.toString(),
				args,
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						String identifyNo = rs.getString("IDENTIFYNO");
						String coherenceCache = rs.getString("COHERENCECACHE");
						String coherenceKey = rs.getString("COHERENCEKEY");
						List<NotifyUpdateControl> list = null;
						if (notifyUpdateControlMap.containsKey(coherenceCache)) {
							list = notifyUpdateControlMap.get(coherenceCache);
						} else {
							list = Lists.newArrayList();
							notifyUpdateControlMap.put(coherenceCache, list);
						}
						list.add(new NotifyUpdateControl(identifyNo, coherenceCache, coherenceKey));
					}
				});
		
		return notifyUpdateControlMap;
	}
	
	private static List<List<String>> transformPartitionValueList(List<NotifyUpdateControl> notifyUpdateControlList) {
		List<String> transformList = Lists.transform(notifyUpdateControlList,
				new Function<NotifyUpdateControl, String>() {
					public String apply(NotifyUpdateControl nc) {
						return nc.getCoherenceKey();
					}
		});
		
		return Lists.partition(transformList, SELECT_MAX);
	}
	
	/**
	 * makerMasterを更新します。
	 * @param list
	 */
	public void updateComm_makerMaster(List<NotifyUpdateControl> notifyUpdateControlList) {
		List<List<String>> partitionList = transformPartitionValueList(notifyUpdateControlList);
		for (List<String> list : partitionList) {
			delete(DBMakerMasterDO.class, "makerCode_s", list);
			saveMakerMaster(list);
		}
	}
	
	/**
	 * productBrandを更新します。
	 * @param list
	 */
	public void updateComm_brand(List<NotifyUpdateControl> notifyUpdateControlList) {
		List<List<String>> partitionList = transformPartitionValueList(notifyUpdateControlList);
		for (List<String> list : partitionList) {
			delete(DBProductBrandDO.class, "brandCode_s", list);
			saveProductBrand(list);
		}
	}
	
	/**
	 * itemObjectUrlを更新します。
	 * @param list
	 */
	public void updateComm_itemObjectURL(List<NotifyUpdateControl> notifyUpdateControlList) {
		List<List<String>> partitionList = transformPartitionValueList(notifyUpdateControlList);
		for (List<String> list : partitionList) {
			delete(DBItemObjectUrlDO.class, "sku_s", list);
			saveItemObjectUrl(list);
		}
	}
	
	/**
	 * productDetailを更新します。
	 * @param list
	 */
	public void updateComm_productDetail(List<NotifyUpdateControl> notifyUpdateControlList) {
		List<List<String>> partitionList = transformPartitionValueList(notifyUpdateControlList);
		for (List<String> list : partitionList) {
			delete(DBProductDetailDO.class, "sku_s", list);
			saveProductDetail(list);
		}
	}
	
	public void updateComm_variationProduct(List<NotifyUpdateControl> notifyUpdateControlList) {
		// TODO
//		List<List<String>> partitionList = transformPartitionValueList(notifyUpdateControlList);
//		for (List<String> list : partitionList) {
//			delete(DBVariationProductDO.class, "sku_s", list);
//			saveVariationProduct(list);
//		}
		return;
	}
	
	/**
	 * reviewPointを更新します。
	 * @param list
	 */
	public void updateComm_productReviewPoint(List<NotifyUpdateControl> notifyUpdateControlList) {
		List<List<String>> partitionList = transformPartitionValueList(notifyUpdateControlList);
		for (List<String> list : partitionList) {
			delete(DBReviewPointDO.class, "sku_s", list);
			delete(DBReviewPointQuestDO.class, "sku_s", list);
			delete(DBReviewPointQuestDetailDO.class, "sku_s", list);
		
			Map<String, DBReviewPointDO> reviewPointMap = loadReviewPoint(list);
			saveReviewPoints(null, list, reviewPointMap);
		}
	}
	
	/**
	 * Solrに保存します。
	 * @param type
	 * @param objects
	 */
	private <T> void save(Class<T> type, final List<T> objects) {
		log.info(type.getSimpleName() + " - before save count : " + objects.size());
		List<List<T>> partitionList = Lists.partition(objects, SAVE_MAX);
		int cnt = 0;
		for (List<T> list : partitionList) {
			solrOperations.save(type, list);
			cnt += list.size();
		}
		log.info(type.getSimpleName() + " - after save count : " + cnt);
	}
	
	private void delete(Class<?> type, String column, List<String> values) {
		log.info(type.getSimpleName() + " - before delete count : " + values.size());
		
		List<List<String>> partitionList = Lists.partition(values, DELETE_MAX);
		int cnt = 0;
		for (List<String> list : partitionList) {
			StringBuilder sb = new StringBuilder();
			for (int i= 0; i < list.size(); i++) {
				if (i > 0) {
					sb.append(" OR ");
				}
				sb.append(column).append(":").append(list.get(i));
			}
			solrOperations.deleteByQuery(new SolrQuery(sb.toString()), type);
			cnt += list.size();
		}
		
		log.info(type.getSimpleName() + " - after delete count : " + cnt);
	}
	
	/**
	 * DBItemObjectUrlのIDを生成します
	 * @param sku
	 * @param objectTypedCd
	 * @return itemObjectUrlId
	 */
	private static String createItemObjectUrlId(String sku, String objectTypedCd) {
		return IdUtil.createIdByConcatIds(StringUtil.toSHA256(sku), objectTypedCd);
	}
	/**
	 * DBProductDetailのIDを生成します
	 * @param sku
	 * @param endTime
	 * @return productDetailId
	 */
	private static String createProductDetailId(String sku, Date endTime) {
		return IdUtil.createIdByConcatIds(StringUtil.toSHA256(sku), toYYYYMMDDHHMISS(endTime));
	}
	/**
	 * DBReviewPointのIDを生成します
	 * @param sku
	 * @param endTime
	 * @return reviewPointId
	 */
	private static String createReviewPointId(String sku, Date endTime) {
		return IdUtil.createIdByConcatIds(StringUtil.toSHA256(sku), toYYYYMMDDHHMISS(endTime));
	}
	/**
	 * DBReviewPointQuestのIDを生成します
	 * @param sku
	 * @param endTime
	 * @param rqCode
	 * @param rqEndTime
	 * @return reviewPointQuestId
	 */
	private static String createReviewPointQuestId(String sku, Date endTime, String rqCode, Date rqEndTime) {
		return IdUtil.createIdByConcatIds(StringUtil.toSHA256(sku), toYYYYMMDDHHMISS(endTime), rqCode, toYYYYMMDDHHMISS(rqEndTime));
	}
	/**
	 * DBReviewPointQuestDetailのIDを生成します
	 * @param reviewPointQuestDO
	 * @return reviewPointQuestDetailId
	 */
	private static String createReviewPointQuestDetailId(DBReviewPointQuestDO reviewPointQuestDO, int rqEndThreshold) {
		return IdUtil.createIdByConcatIds(StringUtil.toSHA256(reviewPointQuestDO.getSku()),
				toYYYYMMDDHHMISS(reviewPointQuestDO.getEndTime()),
				reviewPointQuestDO.getRqCode(),
				toYYYYMMDDHHMISS(reviewPointQuestDO.getRqEndTime()),
				String.valueOf(rqEndThreshold));
	}
	
	// for debug
	private void outputDataSourceInfo() {
		DataSource ds = template.getDataSource();
		BasicDataSource bds = (BasicDataSource) ds;
		
		log.info("***datasource params");
		log.info("driverClassName: " + bds.getDriverClassName());
		log.info("url: " + bds.getUrl());
		log.info("username: " + "******");
		log.info("password: " + "******");
		log.info("defaultAutoCommit: " + bds.getDefaultAutoCommit());
		log.info("maxActive: " + bds.getMaxActive());
		log.info("maxWait: " + bds.getMaxWait());
		log.info("validationQuery: " + bds.getValidationQuery());
		log.info("testWhileIdle: " + bds.getTestWhileIdle());
		log.info("timeBetweenEvictionRunsMillis: " + bds.getTimeBetweenEvictionRunsMillis());
		log.info("minEvictableIdleTimeMillis: " + bds.getMinEvictableIdleTimeMillis());
		log.info("minIdle: " + bds.getMinIdle());
	}
	private void outputArgs(Integer sleep, Integer groupStart, Integer groupEnd) {
		log.info("***commandline args");
		log.info("sleep: " + ((sleep == null || sleep == 0) ? "none" : sleep));
		log.info("groupStart: " + (groupStart == null ? "default" : groupStart));
		log.info("groupEnd: " + (groupEnd == null ? "default" : groupEnd));
	}

	// 更新通知情報格納クラス
	private static class NotifyUpdateControl {
		// primary key
		private String identifyNo;
		
		// 更新通知タイプ
		private String coherenceCache;
		
		// 更新対象となる情報のキー。
		// 更新通知タイプにより入る値が変わります。
		// makerMaster : makerCd
		// productBrand : brandCode
		// itemObjectUrl : sku
		// productDetail : sku
		// reviewPoint : sku
		private String coherenceKey;
		
		public NotifyUpdateControl(String identifyNo, String coherenceCache, String coherenceKey) {
			this.identifyNo = identifyNo;
			this.coherenceCache = coherenceCache;
			this.coherenceKey = coherenceKey;
		}
		
		public String getIdentifyNo() {
			return identifyNo;
		}
		
		@SuppressWarnings("unused")
		public String getCoherenceCache() {
			return coherenceCache;
		}
		
		@SuppressWarnings("unused")
		public void setCoherenceCache(String coherenceCache) {
			this.coherenceCache = coherenceCache;
		}
		
		@SuppressWarnings("unused")
		public void setIdentifyNo(String identifyNo) {
			this.identifyNo = identifyNo;
		}
		
		public String getCoherenceKey() {
			return coherenceKey;
		}
		
		@SuppressWarnings("unused")
		public void setCoherenceKey(String coherenceKey) {
			this.coherenceKey = coherenceKey;
		}
	}
	
	private void waitToCompleteThreadPoolExecutor(String name, ThreadPoolExecutor threadPoolExecutor) {
		if (threadPoolExecutor == null) return;
		
		while (true) {
			if (threadPoolExecutor.getActiveCount() == 0) break;
			log.info("XXX name="+ name +" activeCount=" + threadPoolExecutor.getActiveCount() + " completedTaskCount=" + threadPoolExecutor.getCompletedTaskCount() + " taskCount=" + threadPoolExecutor.getTaskCount());
			try {
				Thread.sleep(10000L);
			} catch (InterruptedException e) {
			}
		}
		threadPoolExecutor.shutdown();
		log.info("threadPoolExecutor shutdown name=" + name);
	}

	private static String toYYYYMMDDHHMISS(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		return sdf.format(date);
	}
	
	private static class VariantProduct{
		private String sku;
		
		private List<String> relatedSkuList = Lists.newArrayList();
		
		private Date startTime;
		
		private Date endTime;
		
		private Date lastUpdate;

		public String getSku() {
			return sku;
		}

		public void setSku(String sku) {
			this.sku = sku;
		}

		public List<String> getRelatedSkuList() {
			return relatedSkuList;
		}

		public Date getStartTime() {
			return startTime;
		}

		public void setStartTime(Date startTime) {
			this.startTime = startTime;
		}

		public Date getEndTime() {
			return endTime;
		}

		public void setEndTime(Date endTime) {
			this.endTime = endTime;
		}

		public Date getLastUpdate() {
			return lastUpdate;
		}

		public void setLastUpdate(Date lastUpdate) {
			this.lastUpdate = lastUpdate;
		}
		
	}
}
