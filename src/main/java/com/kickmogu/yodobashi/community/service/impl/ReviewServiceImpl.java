/**
 *
 */
package com.kickmogu.yodobashi.community.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.FacetResult;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.lib.solr.annotation.SolrTiming;
import com.kickmogu.yodobashi.community.common.exception.DataNotFoundException;
import com.kickmogu.yodobashi.community.common.exception.InputException;
import com.kickmogu.yodobashi.community.common.exception.UnActiveException;
import com.kickmogu.yodobashi.community.common.utils.DateUtil;
import com.kickmogu.yodobashi.community.common.utils.StringUtil;
import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.cache.CacheStrategyType;
import com.kickmogu.yodobashi.community.resource.cache.MethodCache;
import com.kickmogu.yodobashi.community.resource.cache.TargetSystemType;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.dao.ActionHistoryDao;
import com.kickmogu.yodobashi.community.resource.dao.ApplicationLockDao;
import com.kickmogu.yodobashi.community.resource.dao.CommentDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.DailyScoreFactorDao;
import com.kickmogu.yodobashi.community.resource.dao.ImageDao;
import com.kickmogu.yodobashi.community.resource.dao.InformationDao;
import com.kickmogu.yodobashi.community.resource.dao.LikeDao;
import com.kickmogu.yodobashi.community.resource.dao.OrderDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductMasterDao;
import com.kickmogu.yodobashi.community.resource.dao.RemoveContentsDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.ReviewDao;
import com.kickmogu.yodobashi.community.resource.dao.SimplePmsDao;
import com.kickmogu.yodobashi.community.resource.dao.UniqueUserViewCountDao;
import com.kickmogu.yodobashi.community.resource.dao.VotingDao;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.ProductUtil;
import com.kickmogu.yodobashi.community.resource.domain.AbstractReviewDO.PointGrantRequestDetail;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.DailyScoreFactorDO;
import com.kickmogu.yodobashi.community.resource.domain.DecisivePurchaseDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewPoint;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewPointDetail;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewPointSpecial;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewPointSpecialDetail;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseLostProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewPointSpecialConditionValidateDO;
import com.kickmogu.yodobashi.community.resource.domain.SaveImageDO;
import com.kickmogu.yodobashi.community.resource.domain.ScoreFactorDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.UsedProductDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.AlsoBuyProduct;
import com.kickmogu.yodobashi.community.resource.domain.constants.CancelPointGrantType;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.DailyScoreFactorType;
import com.kickmogu.yodobashi.community.resource.domain.constants.FeedbackPointGrantStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;
import com.kickmogu.yodobashi.community.resource.domain.constants.LikeMessageType;
import com.kickmogu.yodobashi.community.resource.domain.constants.LikePrefixType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointExchangeType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointGrantStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointIncentiveType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointScoreTerm;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ProductSatisfaction;
import com.kickmogu.yodobashi.community.resource.domain.constants.PurchaseHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewPointSpecialConditionValidateType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewSortType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewType;
import com.kickmogu.yodobashi.community.resource.domain.constants.UniqueUserViewCountType;
import com.kickmogu.yodobashi.community.resource.domain.constants.VotingType;
import com.kickmogu.yodobashi.community.service.ImageService;
import com.kickmogu.yodobashi.community.service.IndexService;
import com.kickmogu.yodobashi.community.service.MailService;
import com.kickmogu.yodobashi.community.service.ProductService;
import com.kickmogu.yodobashi.community.service.ReviewService;
import com.kickmogu.yodobashi.community.service.SocialMediaService;
import com.kickmogu.yodobashi.community.service.UserService;
import com.kickmogu.yodobashi.community.service.config.ServiceConfig;
import com.kickmogu.yodobashi.community.service.vo.AlsoBuyProductSummaryVO;
import com.kickmogu.yodobashi.community.service.vo.AlsoBuyProductVO;
import com.kickmogu.yodobashi.community.service.vo.CommentSetVO;
import com.kickmogu.yodobashi.community.service.vo.DecisivePurchaseSetVO;
import com.kickmogu.yodobashi.community.service.vo.DecisivePurchaseSummaryVO;
import com.kickmogu.yodobashi.community.service.vo.DecisivePurchaseVO;
import com.kickmogu.yodobashi.community.service.vo.ProductSatisfactionSetVO;
import com.kickmogu.yodobashi.community.service.vo.ProductSatisfactionSummaryVO;
import com.kickmogu.yodobashi.community.service.vo.ProductSatisfactionVO;
import com.kickmogu.yodobashi.community.service.vo.PurchaseLostProductSetVO;
import com.kickmogu.yodobashi.community.service.vo.ReviewSetVO;
import com.kickmogu.yodobashi.community.service.vo.ReviewStatisticsVO;
import com.kickmogu.yodobashi.community.service.vo.ReviewSummaryVO;
import com.kickmogu.yodobashi.community.service.vo.ReviewTypeSummaryVO;
import com.kickmogu.yodobashi.community.service.vo.UsedProductSetVO;

/**
 * レビューサービスの実装です。
 * @author kamiike
 *
 */
@Service
public class ReviewServiceImpl extends AbstractServiceImpl implements ReviewService {

	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

	/**
	 * アクション履歴 DAO です。
	 */
	@Autowired
	protected ActionHistoryDao actionHistoryDao;

	/**
	 * アプリケーションロック DAO です。
	 */
	@Autowired
	protected ApplicationLockDao applicationLockDao;

	/**
	 * コメント DAO です。
	 */
	@Autowired
	protected CommentDao commentDao;

	/**
	 * コミュニティユーザー DAO です。
	 */
	@Autowired
	protected CommunityUserDao communityUserDao;

	/**
	 * コミュニティユーザーフォロー DAO です。
	 */
	@Autowired
	protected CommunityUserFollowDao communityUserFollowDao;

	/**
	 * お知らせ情報 DAO です。
	 */
	@Autowired
	protected InformationDao informationDao;

	/**
	 * いいね DAO です。
	 */
	@Autowired
	protected LikeDao likeDao;
	
	/**
	 * 参考になった DAO です。
	 */
	@Autowired
	protected VotingDao votingDao;

	/**
	 * 注文 DAO です。
	 */
	@Autowired
	protected OrderDao orderDao;

	/**
	 * 商品 DAO です。
	 */
	@Autowired @Qualifier("catalog")
	protected ProductDao productDao;

	/**
	 * 画像 DAO です。
	 */
	@Autowired
	protected ImageDao imageDao;

	/**
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	protected RequestScopeDao requestScopeDao;

	/**
	 * レビュー DAO です。
	 */
	@Autowired
	protected ReviewDao reviewDao;

	/**
	 * 商品マスター DAO です。
	 */
	@Autowired
	protected ProductMasterDao productMasterDao;

	/**
	 * ユニークユーザー閲覧数 DAO です。
	 */
	@Autowired
	protected UniqueUserViewCountDao uniqueUserViewCountDao;

	/**
	 * 日次スコア要因 DAO です。
	 */
	@Autowired
	protected DailyScoreFactorDao dailyScoreFactorDao;

	/**
	 * ポイント管理 DAO です。
	 */
	@Autowired @Qualifier("pms")
	protected SimplePmsDao simplePmsDao;

	/**
	 * 画像サービスです。
	 */
	@Autowired
	protected ImageService imageService;

	/**
	 * コミュニティユーザーサービスです。
	 */
	@Autowired
	protected UserService userService;

	/**
	 * インデックスサービスです。
	 */
	@Autowired
	protected IndexService indexService;

	/**
	 * メールサービスです。
	 */
	@Autowired
	protected MailService mailService;
	
	/**
	 * socialメディア連携サービスです。
	 */
	@Autowired
	protected SocialMediaService socialMediaService;

	/**
	 * タイムスタンプホルダーです。
	 */
	@Autowired
	protected TimestampHolder timestampHolder;

	/**
	 * サービスコンフィグです。
	 */
	@Autowired
	protected ServiceConfig serviceConfig;

	@Autowired
	protected ProductService productService;

	@Autowired
	protected RemoveContentsDao removeContentsDao;

	@Autowired
	protected ResourceConfig resourceConfig;
	
	/**
	 * レビューサマリー情報（レビュータイプごとのレビュー件数）を返します。
	 * @param product 商品
	 * @return レビューサマリー
	 */
	@Override
	@ArroundSolr
	public ReviewSummaryVO getReviewSummaryByReviewType(List<String> skus) {
		Map<String, Long> reviewSummary = reviewDao.loadReviewSummaryByReviewType(skus);
		ReviewSummaryVO vo = new ReviewSummaryVO();
		vo.setReviewTotalCount(0L);
		vo.setReviewTypeSummaries(new ArrayList<ReviewTypeSummaryVO>());
		for (ReviewType reviewType : ReviewType.values()) {
			ReviewTypeSummaryVO rts = new ReviewTypeSummaryVO();
			rts.setReviewType(reviewType);
			if (reviewSummary.containsKey(reviewType.getCode())) {
				rts.setReviewCount(reviewSummary.get(reviewType.getCode()));
			} else {
				rts.setReviewCount(0L);
			}
			vo.getReviewTypeSummaries().add(rts);
			vo.setReviewTotalCount(vo.getReviewTotalCount() + rts.getReviewCount());
		}
		return vo;
	}
	
	/**
	 * レビュー統計情報（商品満足度・購入の決め手・次も買いますか）を返します。
	 * @param product 商品
	 * @return レビュー統計情報
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=1000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public ReviewStatisticsVO getReviewStatistics(String sku,
			boolean isReviewDecisivePurchase, 
			boolean isAlsoBuyProductSummary,
			boolean isSatisfactionAvarage) {

		ProductDO product = productService.getSimpleProductBySku(sku);
		
		ReviewStatisticsVO result = new ReviewStatisticsVO();
		if( isReviewDecisivePurchase ) {
			result.setDecisivePurchaseSummary(getReviewDecisivePurchaseBySku(
					product, null, 5, 0));
			result.getDecisivePurchaseSummary().setTotalDecisivePurchaseCount(
					reviewDao.countTotalDecisivePurchaseRatings(product.getSku()));
		}
		if (isAlsoBuyProductSummary) {
			result.setAlsoBuyProductSummary(getAlsoBuyProductBySku(product.getSku()));
		} else {
			result.setAlsoBuyProductSummary(new AlsoBuyProductSummaryVO());
		}
		if( isSatisfactionAvarage )
			result.setProductSatisfactionSummary(getProductSatisfactionSummary(sku));
		return result;
	}
	
	/**
	 * 商品満足度のオブジェクトを返す
	 */
	@Override
	@ArroundSolr
	public ProductSatisfactionSummaryVO getProductSatisfactionSummary(String sku) {

		Map<ProductSatisfaction, Long> summaryAllMap = reviewDao.loadProductSatisfactionSummaryMapWithAll(sku);
		
		ProductSatisfactionSummaryVO productSatisfactionSummaryVO = new ProductSatisfactionSummaryVO();
		productSatisfactionSummaryVO.setProductSatisfactions(new ArrayList<ProductSatisfactionVO>());
		
		long totalScore = 0;
		for (ProductSatisfaction productSatisfaction : ProductSatisfaction.values()) {
			ProductSatisfactionVO vo = new ProductSatisfactionVO();
			vo.setProductSatisfaction(productSatisfaction);
			if (summaryAllMap.containsKey(productSatisfaction)) {
				vo.setSatisfactionCount(summaryAllMap.get(productSatisfaction));
				productSatisfactionSummaryVO.setReviewTotalCount(productSatisfactionSummaryVO.getReviewTotalCount() + vo.getSatisfactionCount());
				if (!ProductSatisfaction.NONE.equals(productSatisfaction)) {
					long count = summaryAllMap.get(productSatisfaction);
					productSatisfactionSummaryVO.setAnswerCount(productSatisfactionSummaryVO.getAnswerCount() + count);
					totalScore += count * Long.parseLong(productSatisfaction.getCode());
				}
			}
			
			productSatisfactionSummaryVO.getProductSatisfactions().add(vo);
		}
		if (productSatisfactionSummaryVO.getAnswerCount() > 0) {
			productSatisfactionSummaryVO.setSatisfactionAvarage(
					new BigDecimal(totalScore).divide(
					new BigDecimal(productSatisfactionSummaryVO.getAnswerCount()), 2, BigDecimal.ROUND_HALF_UP).doubleValue());
		} else {
			productSatisfactionSummaryVO.setSatisfactionAvarage(0D);
		}
		BigDecimal bd = new BigDecimal(productSatisfactionSummaryVO.getSatisfactionAvarage());
		Double average = bd.setScale(1,BigDecimal.ROUND_DOWN).doubleValue();
		productSatisfactionSummaryVO.setSatisfactionAvarageCss(getAvarageCss(average));
		productSatisfactionSummaryVO.setSatisfactionAvaragePoint(average.toString());
		
		return productSatisfactionSummaryVO;
	}
	
	private String getAvarageCss(Double avarage) {
		String lowPoint = null;
		int intAvarage = avarage.intValue();
		double lowAvarage = avarage - (double)intAvarage;
		
		if		( lowAvarage == 0.00 )	{ lowPoint = "_0";}
		else if	( lowAvarage <= 0.25 )	{ lowPoint = "_25";}
		else if	( lowAvarage <= 0.50 )	{ lowPoint = "_5";}
		else if	( lowAvarage <= 0.75 )	{ lowPoint = "_75";}
		else							{ lowPoint = "_0"; intAvarage++; }
		
		return intAvarage + lowPoint;
	}

	@Override
	@ArroundSolr
	public ProductSatisfactionSummaryVO getProductSatisfactionSummary(String[] skus) {
		Map<String, Map<ProductSatisfaction, Long>> summaryAllMaps = reviewDao.loadProductSatisfactionSummaryMapsWithAll(skus);
		
		ProductSatisfactionSummaryVO productSatisfactionSummaryVO = new ProductSatisfactionSummaryVO();
		productSatisfactionSummaryVO.setProductSatisfactions(new ArrayList<ProductSatisfactionVO>());
		
		long totalScore = 0;
		for (ProductSatisfaction productSatisfaction : ProductSatisfaction.values()) {
			ProductSatisfactionVO vo = new ProductSatisfactionVO();
			vo.setProductSatisfaction(productSatisfaction);
			
			for(String sku:skus){
				Map<ProductSatisfaction, Long> summaryAllMap = summaryAllMaps.get(sku);
				
				if (summaryAllMap.containsKey(productSatisfaction)) {
					vo.setSatisfactionCount(vo.getSatisfactionCount() + summaryAllMap.get(productSatisfaction));
					if (!ProductSatisfaction.NONE.equals(productSatisfaction)) {
						long count = summaryAllMap.get(productSatisfaction);
						productSatisfactionSummaryVO.setAnswerCount(productSatisfactionSummaryVO.getAnswerCount() + count);
						totalScore += count * Long.parseLong(productSatisfaction.getCode());
					}
				}
			}
			
			productSatisfactionSummaryVO.setReviewTotalCount(productSatisfactionSummaryVO.getReviewTotalCount() + vo.getSatisfactionCount());
			productSatisfactionSummaryVO.getProductSatisfactions().add(vo);
		}
		
		if (productSatisfactionSummaryVO.getAnswerCount() > 0) {
			productSatisfactionSummaryVO.setSatisfactionAvarage(
					new BigDecimal(totalScore).divide(
					new BigDecimal(productSatisfactionSummaryVO.getAnswerCount()), 2, BigDecimal.ROUND_HALF_UP).doubleValue());
		} else {
			productSatisfactionSummaryVO.setSatisfactionAvarage(0D);
		}
		
		BigDecimal bd = new BigDecimal(productSatisfactionSummaryVO.getSatisfactionAvarage());
		Double average = bd.setScale(1,BigDecimal.ROUND_DOWN).doubleValue();
		productSatisfactionSummaryVO.setSatisfactionAvarageCss(getAvarageCss(average));
		productSatisfactionSummaryVO.setSatisfactionAvaragePoint(average.toString());

		
		return productSatisfactionSummaryVO;
	}
	
	@Override
	@ArroundSolr
	public Map<String, ProductSatisfactionSummaryVO> getSatisfactionAvarageMap(String[] skus) {
		Map<String, Map<ProductSatisfaction, Long>> summaryAllMaps = reviewDao.loadProductSatisfactionSummaryMapsWithAll(skus);
		
		Map<String, ProductSatisfactionSummaryVO> productSatisfactionSummaryVOMap = new HashMap<String, ProductSatisfactionSummaryVO>();
		for(String sku:skus){
			Map<ProductSatisfaction, Long> summaryAllMap = summaryAllMaps.get(sku);
			
			ProductSatisfactionSummaryVO reviewProductSatisfaction = new ProductSatisfactionSummaryVO();
			reviewProductSatisfaction.setProductSatisfactions(new ArrayList<ProductSatisfactionVO>());
			long totalScore = 0;
			for (ProductSatisfaction productSatisfaction : ProductSatisfaction.values()) {
				ProductSatisfactionVO vo = new ProductSatisfactionVO();
				vo.setProductSatisfaction(productSatisfaction);

				if (summaryAllMap.containsKey(productSatisfaction)) {
					vo.setSatisfactionCount(summaryAllMap.get(productSatisfaction));
					reviewProductSatisfaction.setReviewTotalCount(reviewProductSatisfaction.getReviewTotalCount() + vo.getSatisfactionCount());
					if (!ProductSatisfaction.NONE.equals(productSatisfaction)) {
						long count = summaryAllMap.get(productSatisfaction);
						reviewProductSatisfaction.setAnswerCount(reviewProductSatisfaction.getAnswerCount() + count);
						totalScore += count * Long.parseLong(productSatisfaction.getCode());
						
					}
				}
				
				reviewProductSatisfaction.getProductSatisfactions().add(vo);
			}
			if (reviewProductSatisfaction.getAnswerCount() > 0) {
				reviewProductSatisfaction.setSatisfactionAvarage(
						new BigDecimal(totalScore).divide(
						new BigDecimal(reviewProductSatisfaction.getAnswerCount()), 2,
						BigDecimal.ROUND_HALF_UP).doubleValue());
			} else {
				reviewProductSatisfaction.setSatisfactionAvarage(0D);
			}
			productSatisfactionSummaryVOMap.put(sku, reviewProductSatisfaction);
		}
		return productSatisfactionSummaryVOMap;
	}
	/**
	 * 次も買いますか評価情報を返します。
	 * @param sku　商品SKU
	 * @return 次も買いますか情報一覧サマリー
	 */
	@Override
	public AlsoBuyProductSummaryVO getAlsoBuyProductBySku(String sku) {
		return getAlsoBuyProductBySkus(Lists.newArrayList(sku));
	}

	/**
	 * 次も買いますか評価情報を返します。
	 * @param skus 商品SKU一覧
	 * @return 次も買いますか情報一覧サマリー
	 */
	@Override
	public AlsoBuyProductSummaryVO getAlsoBuyProductBySkus(List<String> skus) {
		AlsoBuyProductSummaryVO result = new AlsoBuyProductSummaryVO();
		result.setAlsoBuyProducts(new ArrayList<AlsoBuyProductVO>());
		Map<AlsoBuyProduct, Long> alsoBuyProductSummaryMap = reviewDao.loadAlsoBuyProductSummaryMap(skus);
		for (AlsoBuyProduct alsoBuyProduct : AlsoBuyProduct.values()) {
			AlsoBuyProductVO vo = new AlsoBuyProductVO();
			vo.setAlsoBuyProduct(alsoBuyProduct);
			vo.setRatings(alsoBuyProductSummaryMap.get(alsoBuyProduct));
			result.getAlsoBuyProducts().add(vo);
			result.setTotalCount(result.getTotalCount() + vo.getRatings());
		}
		if (result.getTotalCount() > 0) {
			// 各評価のパーセンテージを求める
			long percentageTotal = 0;
			for (AlsoBuyProductVO vo : result.getAlsoBuyProducts()) {
				vo.setPercentage(vo.getRatings() * 100 / result.getTotalCount());
				percentageTotal += vo.getPercentage();
			}
			if (percentageTotal < 100) {
				// 端数切り捨ての影響で合計が100に満たない場合、最大の値に端数を加算して合計100になるようにする
				AlsoBuyProductVO vo = Collections.max(result.getAlsoBuyProducts(), new Comparator<AlsoBuyProductVO>() {
					@Override
					public int compare(AlsoBuyProductVO o1, AlsoBuyProductVO o2) {
						return (int)(o1.getPercentage() - o2.getPercentage());
					}
				});
				vo.setPercentage(vo.getPercentage() + (100 - percentageTotal));
			}
		}
		return result;
	}

	/**
	 * 購入の決め手情報を返します。
	 * @param product 商品
	 * @param decisivePurchaseIds 必ず含める購入の決め手IDリスト
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 購入の決め手情報
	 */
	private DecisivePurchaseSummaryVO getReviewDecisivePurchaseBySku(
			ProductDO product,
			List<String> decisivePurchaseIds,
			int limit, int offset) {
		DecisivePurchaseSummaryVO reviewDecisivePurchase = new DecisivePurchaseSummaryVO();
		reviewDecisivePurchase.setDecisivePurchases(new ArrayList<DecisivePurchaseVO>());

		SearchResult<DecisivePurchaseDO> searchResult = reviewDao.findDecisivePurchaseFromIndexBySKU(
				product, decisivePurchaseIds, null, limit, offset);

		reviewDecisivePurchase.setCandidateCount(searchResult.getNumFound());

		reviewDecisivePurchase.setTotalDecisivePurchaseCount(
				reviewDao.countTotalDecisivePurchaseRatings(product.getSku()));

		for (DecisivePurchaseDO decisivePurchase : searchResult.getDocuments()) {
			DecisivePurchaseVO vo = new DecisivePurchaseVO();
			vo.setDecisivePurchaseId(decisivePurchase.getDecisivePurchaseId());
			vo.setDecisivePurchaseName(decisivePurchase.getDecisivePurchaseName());
			vo.setRatings(decisivePurchase.getRatings());
			reviewDecisivePurchase.getDecisivePurchases().add(vo);
		}

		return reviewDecisivePurchase;
	}

	/**
	 * 購入の決め手情報を返します。
	 * @param product 商品
	 * @param decisivePurchaseIds 必ず含める購入の決め手IDリスト
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 購入の決め手情報
	 */
	public DecisivePurchaseSummaryVO getReviewDecisivePurchaseByProduct(
			ProductDO product,
			List<String> decisivePurchaseIds,
			int limit, int offset) {
		DecisivePurchaseSummaryVO reviewDecisivePurchase = new DecisivePurchaseSummaryVO();
		reviewDecisivePurchase.setDecisivePurchases(new ArrayList<DecisivePurchaseVO>());

		SearchResult<DecisivePurchaseDO> searchResult = reviewDao.findDecisivePurchaseFromIndexBySKU(
				product, decisivePurchaseIds, null, limit, offset);

		reviewDecisivePurchase.setCandidateCount(searchResult.getNumFound());

		reviewDecisivePurchase.setTotalDecisivePurchaseCount(
				reviewDao.countTotalDecisivePurchaseRatings(product.getSku()));

		for (DecisivePurchaseDO decisivePurchase : searchResult.getDocuments()) {
			DecisivePurchaseVO vo = new DecisivePurchaseVO();
			vo.setDecisivePurchaseId(decisivePurchase.getDecisivePurchaseId());
			vo.setDecisivePurchaseName(decisivePurchase.getDecisivePurchaseName());
			vo.setRatings(decisivePurchase.getRatings());
			reviewDecisivePurchase.getDecisivePurchases().add(vo);
		}

		return reviewDecisivePurchase;
	}
	
	/**
	 * 指定の「購入の決め手」を取得します
	 * @param decisivePurchaseIds
	 * @return
	 */
	public SearchResult<DecisivePurchaseSetVO> findDecisivePurchaseFromIndexByIds(List<String> decisivePurchaseIds) {
		
		SearchResult<DecisivePurchaseDO> decisivePurchaseSearchList = reviewDao.findDecisivePurchaseFromIndexByIds(decisivePurchaseIds);
		Map<String, Long> decisivePurchaseCountMap = reviewDao.decisivePurchaseCountMap(decisivePurchaseIds, requestScopeDao.loadCommunityUserId());

		SearchResult<DecisivePurchaseSetVO> result = new SearchResult<DecisivePurchaseSetVO>(0, new ArrayList<DecisivePurchaseSetVO>());
		for (DecisivePurchaseDO decisivePurchaseDO : decisivePurchaseSearchList.getDocuments()) {
			DecisivePurchaseSetVO vo = new DecisivePurchaseSetVO();
			vo.setDecisivePurchase(decisivePurchaseDO);
			String decisivePurchaseId = decisivePurchaseDO.getDecisivePurchaseId();
			if (decisivePurchaseCountMap.containsKey(decisivePurchaseId)) {
				vo.setOtherSelectedUserCount(decisivePurchaseCountMap.get(decisivePurchaseId));
			} else {
				vo.setOtherSelectedUserCount(0);
			}
			result.getDocuments().add(vo);
		}
		return result;
	}
	
	/**
	 * 指定したレビュー情報において、購入に迷った商品に選ばれた商品を返します。
	 * @param reviewId レビューID
	 * @return 購入に迷った商品のリスト
	 */
	@Override
	@ArroundSolr
	public SearchResult<PurchaseLostProductDO> findPurchaseLostProductByReviewId(String reviewId) {
		return reviewDao.findPurchaseLostProductByReviewId(reviewId);
	}

	/**
	 * 指定したレビュー情報において、過去に使用した商品に選ばれた商品を返します。
	 * @param reviewId レビューID
	 * @return 過去に使用した商品のリスト
	 */
	@Override
	@ArroundSolr
	public SearchResult<UsedProductDO> findUsedProductByReviewId(String reviewId) {
		return reviewDao.findUsedProductByReviewId(reviewId);
	}

	/**
	 * 指定した商品のレビュー情報において、購入に迷った商品に選ばれた商品を返します。
	 * @param sku SKU SKU
	 * @param limit 最大取得件数
	 * @return 購入に迷った商品のリスト
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=1000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public SearchResult<PurchaseLostProductSetVO> findPurchaseLostProductBySku(
			String sku, int limit) {
		SearchResult<PurchaseLostProductSetVO> result
				= new SearchResult<PurchaseLostProductSetVO>();
		List<String> skus = new ArrayList<String>();
		SearchResult<FacetResult<String>> searchResult
				= reviewDao.findPurchaseLostProductCountBySku(sku, limit);
		result.setHasAdult(searchResult.isHasAdult());
		result.setNumFound(searchResult.getNumFound());
		if (searchResult.getDocuments().size() == 0) {
			return result;
		}
		for (FacetResult<String> facetResult : searchResult.getDocuments()) {
			skus.add(facetResult.getValue());
		}
		Map<String, ProductDO> productMap = productDao.findBySku(skus);
		for (FacetResult<String> facetResult : searchResult.getDocuments()) {
			PurchaseLostProductSetVO detail = new PurchaseLostProductSetVO();
			detail.setPurchaseLostProduct(productMap.get(facetResult.getValue()));
			detail.setCount(facetResult.getCount());
			result.getDocuments().add(detail);
		}
		return result;
	}

	/**
	 * 指定した商品のレビュー情報において、過去に使用した商品に選ばれた商品を返します。
	 * @param sku SKU SKU
	 * @param limit 最大取得件数
	 * @return 過去に使用した商品のリスト
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=1000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public SearchResult<UsedProductSetVO> findUsedProductBySku(String sku, int limit) {
		SearchResult<UsedProductSetVO> result
				= new SearchResult<UsedProductSetVO>();
		List<String> skus = new ArrayList<String>();
		SearchResult<FacetResult<String>> searchResult
				= reviewDao.findUsedProductCountBySku(sku, limit);
		result.setHasAdult(searchResult.isHasAdult());
		result.setNumFound(searchResult.getNumFound());
		if (searchResult.getDocuments().size() == 0) {
			return result;
		}
		for (FacetResult<String> facetResult : searchResult.getDocuments()) {
			skus.add(facetResult.getValue());
		}
		Map<String, ProductDO> productMap = productDao.findBySku(skus);
		for (FacetResult<String> facetResult : searchResult.getDocuments()) {
			UsedProductSetVO detail = new UsedProductSetVO();
			detail.setUsedProduct(productMap.get(facetResult.getValue()));
			detail.setCount(facetResult.getCount());
			result.getDocuments().add(detail);
		}
		return result;
	}

	/**
	 * 指定した商品に対するレビュー件数を返します。
	 * @param product 商品
	 * @return レビュー件数
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=10000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public long countReviewBySku(String sku) {
		return reviewDao.countReviewBySku(sku);
	}
	
	/**
	 * 指定した商品に対するレビュー件数を返します。
	 * @param product 商品
	 * @return レビュー件数
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=10000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public long countReviewBySkus(List<String> skus) {
		return reviewDao.countReviewBySkus(skus);
	}

	/**
	 * 指定した商品、経過月に対するレビューを投稿日時順（降順）に返します。
	 * @param product 商品
	 * @param reviewType レビュータイプ
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<ReviewSetVO> findNewReviewBySkuAndReviewType(
			ProductDO product,
			ReviewType reviewType,
			String excludeReviewId,
			int limit, Date offsetTime, boolean previous) {
		SearchResult<ReviewDO> searchResult = reviewDao.findNewReviewBySku(
				product, reviewType, null, excludeReviewId, limit, offsetTime, previous);
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<ReviewSetVO>(0, new ArrayList<ReviewSetVO>());
		} else {
			return appendPurchaseProduct(createReviewSets(searchResult), false);
		}
	}
	
	@Override
	public SearchResult<ReviewSetVO> findNewReviewBySkusAndReviewType(
			String sku,
			List<String> skus,
			ReviewType reviewType,
			String excludeReviewId,
			int limit, Date offsetTime, boolean previous){
		List<String> mergeSkus = new ArrayList<String>();
		mergeSkus.add(sku);
		mergeSkus.addAll(skus);
		SearchResult<ReviewDO> searchResult = reviewDao.findNewReviewBySkus(
				mergeSkus, reviewType, excludeReviewId, limit, offsetTime, previous);
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<ReviewSetVO>(0, new ArrayList<ReviewSetVO>());
		} else {
			return appendPurchaseProduct(createReviewSets(searchResult), false);
		}
	}
	
	/**
	 * 指定した商品、評価に対するレビューを投稿日時順（降順）に返します。
	 * @param sku 商品SKU
	 * @param skus バリエーション商品一覧 
	 * @param productSatisfaction 指定の評価
	 * @param excludeReviewId 除外するレビューID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@Override
	public SearchResult<ReviewSetVO> findReviewBySkusAndRatingStar(
			String sku,
			List<String> skus,
			String excludeReviewId,
			ProductSatisfaction productSatisfaction,
			ReviewSortType sortType,
			int limit,
			Date offsetTime,
			Double offsetScore,
			boolean previous) {
		List<String> mergeSkus = new ArrayList<String>();
		mergeSkus.add(sku);
		mergeSkus.addAll(skus);
		SearchResult<ReviewDO> searchResult = reviewDao.findNewReviewBySkusAndRatingStar(
				mergeSkus, productSatisfaction, sortType, excludeReviewId, limit, offsetTime, offsetScore, previous);
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<ReviewSetVO>(0, new ArrayList<ReviewSetVO>());
		} else {
			return appendPurchaseProduct(createReviewSets(searchResult), false);
		}
	}
	
	/**
	 * 指定した商品、評価に対するレビューを投稿日時順（降順）に返します。
	 * @param sku 商品SKU
	 * @param skus バリエーション商品一覧 
	 * @param productSatisfaction 指定の評価
	 * @param excludeReviewId 除外するレビューID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@Override
	public SearchResult<ReviewSetVO> findReviewBySkuAndRatingStar(
			ProductDO product, 
			String excludeReviewId,
			ProductSatisfaction productSatisfaction,
			ReviewSortType sortType,
			int limit,
			Date offsetTime, 
			Double offsetScore,
			boolean previous) {
		SearchResult<ReviewDO> searchResult = reviewDao.findReviewBySkuAndRatingStar(
				product, productSatisfaction, sortType, null, excludeReviewId, limit, offsetTime, offsetScore, previous);
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<ReviewSetVO>(0, new ArrayList<ReviewSetVO>());
		} else {
			return appendPurchaseProduct(createReviewSets(searchResult), false);
		}
	}

	/**
	 * 指定した商品とコミュニティーユーザーIDで、レビューを投稿日時順（降順）に返します。
	 * @param communityUserId コミュニティーユーザーID
	 * @param sku 商品SKU
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@PerformanceTest(type=Type.SELECT,
			frequency=Frequency.HIGH,
			frequencyComment="レビュー一覧の初期表示ともっと見るAjaxで呼ばれるので頻度は高",
			refClassNames={"AjaxJsonProductReviewController","ProductReviewListController"}
		)
	public SearchResult<ReviewSetVO> findNewReviewBySkuAndCommunityUserId(
			String communityUserId,
			String sku,
			int limit, 
			Date offsetTime, 
			boolean previous){
		SearchResult<ReviewDO> searchResult = reviewDao.findNewReviewBySkuAndCommunityUserId(
				communityUserId, sku, limit, offsetTime, previous);
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<ReviewSetVO>(0, new ArrayList<ReviewSetVO>());
		} else {
			return appendPurchaseProduct(createReviewSets(searchResult), false);
		}
	}
			
	/**
	 * 購入商品情報を付加します。
	 * @param vos
	 * @return vos
	 */
	private SearchResult<ReviewSetVO> appendPurchaseProduct( SearchResult<ReviewSetVO> vos, boolean fromSolr){

		if(fromSolr){
			List<Map<String, String>> reviews = new ArrayList<Map<String, String>>();
			for( ReviewSetVO vo : vos.getDocuments() ){
				ReviewDO review = vo.getReview();
				if( null == review.getProduct() || null == review.getProduct().getSku() ){
					continue;
				}
				Map<String, String> params = new HashMap<String, String>();
				params.put("communityUserId", review.getCommunityUser().getCommunityUserId());
				params.put("sku", review.getProduct().getSku());
				reviews.add(params);
			}
			if(!reviews.isEmpty()){
				Map<String, PurchaseProductDO> orderMaps = orderDao.findPurchaseProductBySku(reviews);
				for( ReviewSetVO vo : vos.getDocuments() ){
					ReviewDO review = vo.getReview();
					if( null == review.getProduct() || null == review.getProduct().getSku() ){
						continue;
					}
					PurchaseProductDO order = orderMaps.get(IdUtil.createIdByConcatIds(review.getCommunityUser().getCommunityUserId(), review.getProduct().getSku()));
					if( null != order && !order.isDeleted() ){
						vo.setPurchaseProduct( order );
					}
				}
			}
		} else {
			for( ReviewSetVO vo : vos.getDocuments() ){
				ReviewDO review = vo.getReview();
				if( null == review.getProduct() || null == review.getProduct().getSku() ){
					continue;
				}
				PurchaseProductDO order = orderDao.loadPurchaseProductBySku(review.getCommunityUser().getCommunityUserId(),
						review.getProduct().getSku(), Path.DEFAULT, false);
				if( null != order && !order.isDeleted() ){
					vo.setPurchaseProduct( order );
				}
			}
		}
		return vos;
	}
	
	/**
	 * 指定した商品、経過月に対するレビューを投稿日時順（降順）に返します。
	 * @param product 商品
	 * @param reviewTerm レビュー区間
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<ReviewSetVO> findNewReviewBySku(
			ProductDO product,
			Integer reviewTerm,
			String excludeReviewId,
			int limit, Date offsetTime, boolean previous) {
		SearchResult<ReviewDO> searchResult = reviewDao.findNewReviewBySku(
				product, null, reviewTerm, excludeReviewId, limit, offsetTime, previous);
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<ReviewSetVO>(0, new ArrayList<ReviewSetVO>());
		} else {
			return createReviewSets(searchResult);
		}
	}
	
	
	public SearchResult<ReviewSetVO> findNewReviewBySku(String sku, int limit) {
		SearchResult<ReviewDO> searchResult = reviewDao.findNewReviewBySku(sku, limit);
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<ReviewSetVO>(0, new ArrayList<ReviewSetVO>());
		} else {
			return appendPurchaseProduct(createReviewSets(searchResult), true);
		}
	}
	
	/**
	 * 指定のカテゴリコードの最新のレビューを取得する（カテゴリ指定なしは、すべての商品が対象）
	 * @param categoryCode カテゴリコード
	 * @param offsetTime 検索キー（対象は、投稿日時）
	 * @param limit 取得上限
	 * @param previous (true：古い順,false:新しい順）
	 * @return レビュー一覧を取得
	 */
	@Override
	public SearchResult<ReviewSetVO> findReviewByCategoryCode(
			String categoryCode,
			Date offsetTime,
			int limit,
			boolean previous) {
		SearchResult<ReviewDO> searchResult = reviewDao.findReviewByCategoryCode(
				categoryCode,
				offsetTime,
				limit,
				previous);
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<ReviewSetVO>(0, new ArrayList<ReviewSetVO>());
		} else {
			return appendPurchaseProduct(createReviewSets(searchResult), true);
		}
	}
	/**
	 * 指定した商品、経過月に対するレビューを適合順（降順）に返します。
	 * @param product 商品情報
	 * @param reviewType レビュータイプ
	 * @param excludeReviewId 対象外のレビューID
	 * @param limit 最大取得件数
	 * @param offsetMatchScore 検索開始適合度
	 * @param offsetTime 検索開始時間（２次ソート条件）
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<ReviewSetVO> findMatchReviewBySkuAndReviewType(
			ProductDO product,
			ReviewType reviewType,
			String excludeReviewId,
			int limit,
			Double offsetMatchScore,
			Date offsetTime,
			boolean previous) {
		SearchResult<ReviewDO> searchResult = reviewDao.findMatchReviewBySku(
				product, reviewType, null, excludeReviewId, limit, offsetMatchScore, offsetTime, previous);
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<ReviewSetVO>(0, new ArrayList<ReviewSetVO>());
		} else {
			return appendPurchaseProduct(createReviewSets(searchResult), false);
		}
	}
	
	

	public SearchResult<ReviewSetVO> findMatchReviewBySkusAndReviewType(
			String sku,
			List<String> skus,
			ReviewType reviewType,
			String excludeReviewId,
			int limit,
			Double offsetMatchScore,
			Date offsetTime, boolean previous){
		List<String> mergeSkus = new ArrayList<String>();
		mergeSkus.add(sku);
		mergeSkus.addAll(skus);
		SearchResult<ReviewDO> searchResult = reviewDao.findMatchReviewBySkus(
				mergeSkus, reviewType, excludeReviewId, limit, offsetMatchScore, offsetTime, previous);
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<ReviewSetVO>(0, new ArrayList<ReviewSetVO>());
		} else {
			searchResult.setSku(sku);
			return appendPurchaseProduct(createReviewSets(searchResult), false);
		}
	}

	/**
	 * 指定した商品、経過月に対するレビューを適合順（降順）に返します。
	 * @param sku SKU
	 * @param reviewTerm レビュー区間
	 * @param limit 最大取得件数
	 * @param offsetMatchScore 検索開始適合度
	 * @param offsetTime 検索開始時間（２次ソート条件）
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<ReviewSetVO> findMatchReviewBySku(
			ProductDO product,
			Integer reviewTerm,
			String excludeReviewId,
			int limit,
			Double offsetMatchScore,
			Date offsetTime,
			boolean previous) {
		SearchResult<ReviewDO> searchResult = reviewDao.findMatchReviewBySku(
				product, null, reviewTerm, excludeReviewId, limit, offsetMatchScore, offsetTime, previous);
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<ReviewSetVO>(0, new ArrayList<ReviewSetVO>());
		} else {
			return createReviewSets(searchResult);
		}
	}

	/**
	 * 指定した商品、経過月に対するレビューを適合順（降順）に返します。
	 * @param sku SKU
	 * @param reviewTerm レビュー区間
	 * @param limit 最大取得件数
	 * @param offsetMatchScore 検索開始適合度
	 * @param offsetTime 検索開始時間（２次ソート条件）
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<ReviewSetVO> findMatchReviewBySku(String sku, int limit) {
		SearchResult<ReviewDO> searchResult = reviewDao.findMatchReviewBySku(sku, limit);
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<ReviewSetVO>(0, new ArrayList<ReviewSetVO>());
		} else {
			return appendPurchaseProduct(createReviewSets(searchResult), true);
		}
	}

	
	/**
	 * レビューセットの検索結果を作成します。
	 * @param searchResult レビューリスト
	 * @return レビューセットの検索結果
	 */
	private SearchResult<ReviewSetVO> createReviewSets(SearchResult<ReviewDO> searchResult) {
		SearchResult<ReviewSetVO> result = new SearchResult<ReviewSetVO>(0, new ArrayList<ReviewSetVO>());
		result.setHasAdult(searchResult.isHasAdult());

		long numfound = searchResult.getNumFound();
		
		List<String> reviewIds = new ArrayList<String>();
		List<String> communityUserIds = new ArrayList<String>();
		List<ProductMasterDO> productMasters = new ArrayList<ProductMasterDO>();
		for (ReviewDO review : searchResult.getDocuments()) {
			reviewIds.add(review.getReviewId());
			communityUserIds.add(review.getCommunityUser().getCommunityUserId());
			ProductMasterDO productMaster = new ProductMasterDO();
			productMaster.setProduct(review.getProduct());
			productMaster.setCommunityUser(review.getCommunityUser());
			productMasters.add(productMaster);
		}
		//レビューいいね数
		Map<String, Long> likeCountMap = likeDao.loadReviewLikeCountMap(reviewIds);
		//レビュー参考になった数
		Map<String, Long[]> votingCountMap = votingDao.loadReviewVotingCountMap(reviewIds);
		//レビューコメント数
		Map<String, Long> commentCountMap = commentDao.loadReviewCommentCountMap(reviewIds);
		
		Map<String, Boolean> likeMap = new HashMap<String, Boolean>();
		Map<String, Boolean> userFollowMap = new HashMap<String, Boolean>();
		Map<String, Boolean> commentMap = new HashMap<String, Boolean>();
		Map<String, VotingType> votingMap = new HashMap<String, VotingType>();
		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();
		if( StringUtils.isNotEmpty(loginCommunityUserId)){
			// いいね済みかどうか
			likeMap = likeDao.loadReviewLikeMap(loginCommunityUserId, reviewIds);
			// レビュー参考になった済みかどうか
			votingMap = votingDao.loadReviewVotingMap(loginCommunityUserId, reviewIds);
			// コメント済みかどうか
			commentMap = commentDao.loadReviewCommentMap(loginCommunityUserId, reviewIds);
			// ユーザーフォロー済みかどうか
			userFollowMap = communityUserFollowDao.loadCommunityUserFollowMap(loginCommunityUserId, communityUserIds);
		}
		
		Map<String, ProductMasterDO> productMasterMap = new HashMap<String, ProductMasterDO>();
		//商品マスター情報
		for (ProductMasterDO productMaster : productMasterDao.findProductMasterInRank(
				productMasters)) {
			productMasterMap.put(IdUtil.createIdByConcatIds(
					productMaster.getProduct().getSku(),
					productMaster.getCommunityUser().getCommunityUserId()),
					productMaster);
		}
		//レビュー画像
		Map<String, List<ImageHeaderDO>> imageAllMap = imageDao.loadAllImageMapByContentsIds(PostContentType.REVIEW, reviewIds);
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(searchResult.getDocuments());
		// 同じ商品・ユーザーで、他のレビュー数
		Map<String, Long> sameProductReviewCountMap = reviewDao.loadSameProductReviewCountMap(searchResult.getDocuments());
		
		// レビューに投稿されているコメントを取得する。
		for (ReviewDO review : searchResult.getDocuments()) {
			ReviewSetVO vo = new ReviewSetVO();
			String reviewId = review.getReviewId();
			vo.setReview(review);
			if (likeMap.containsKey(reviewId)) {
				vo.setLikeFlg(likeMap.get(reviewId));
			}
			if (likeCountMap.containsKey(reviewId)) {
				vo.setLikeCount(likeCountMap.get(reviewId));
			}
			if (votingCountMap.containsKey(reviewId)) {
				Long[] votingCount = votingCountMap.get(reviewId);
				vo.setVotingCountYes(votingCount[0]);
				vo.setVotingCountNo(votingCount[1]);
			}
			if (votingMap.containsKey(reviewId)) {
				vo.setVotingType(votingMap.get(reviewId));
			}
			if (commentMap.containsKey(reviewId)) {
				vo.setCommentFlg(commentMap.get(reviewId));
			}
			if (commentCountMap.containsKey(reviewId)) {
				vo.setCommentCount(commentCountMap.get(reviewId));
			}
			if (userFollowMap.containsKey(review.getCommunityUser().getCommunityUserId())) {
				vo.setFollowingUser(userFollowMap.get(review.getCommunityUser().getCommunityUserId()));
			}
			String key = IdUtil.createIdByConcatIds(
					review.getProduct().getSku(),
					review.getCommunityUser().getCommunityUserId());
			if (productMasterMap.containsKey(key)) {
				vo.setProductMaster(productMasterMap.get(key));
			}
			if (imageAllMap.containsKey(reviewId)) {
				List<ImageHeaderDO> imageHeaders = imageAllMap.get(reviewId);
				vo.setImages(imageHeaders);
				vo.getReview().setImageHeaders(imageHeaders);
			}
			
			if( sameProductReviewCountMap.containsKey(reviewId)){
				vo.setOtherReviewCount(sameProductReviewCountMap.get(reviewId));
			}
			
			result.updateFirstAndLast(vo);
			if (review.isStop(loginCommunityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				numfound--;
				continue;
			}
			
			SearchResult<LikeDO> likes = findLikeByReviewId(
					reviewId,
					loginCommunityUserId,
					resourceConfig.evaluationAreaLikeReadLimit);
			
			long like_count = vo.getLikeCount() - (vo.isLikeFlg()? 1 : 0);
			if( vo.isLikeFlg() ){
				// getLikeCount()は、自分を含んだ数字であることが前提。
				if( likes.getDocuments().isEmpty() ){
					vo.setLikePrefixType(LikePrefixType.ONLYONE.getCode());
				}else{
					vo.setLikePrefixType(LikePrefixType.MULTIPLE.getCode());
				}
			}else{
				vo.setLikePrefixType(LikePrefixType.NONE.getCode());
			}
			List<String> communityUserNames = new ArrayList<String>();
			if( like_count == 0 ){
				vo.setLikeMessageType(LikeMessageType.NONE.getCode());
			}else{
				if( like_count <= 3 ){
					vo.setLikeMessageType(LikeMessageType.UPTO3.getCode());
					for( LikeDO like : likes.getDocuments()){
						communityUserNames.add(like.getCommunityUser().getCommunityName());
					}
				}else{
					vo.setLikeMessageType(LikeMessageType.MULTIPLE.getCode());
				}
			}
			vo.setLikeUserNames(communityUserNames);
			// TODO あとで効率よく取れるように変更する。
			SearchResult<CommentSetVO> resultComment = findReviewCommentByReviewId(
					reviewId,
					null,
					resourceConfig.commentInitReadLimit,
					null,
					false);
			if( !resultComment.getDocuments().isEmpty() )
				Collections.reverse(resultComment.getDocuments());
			vo.setCommentViewRemainingCount(vo.getCommentCount() - resultComment.getDocuments().size());
			vo.setComments(resultComment);
			
			result.getDocuments().add(vo);
		}
		if(numfound < 0 ) numfound = 0;
		result.setNumFound(numfound);
		return result;
	}

	/**
	 * 指定した商品を除いた、指定したレビュアーによるレビューを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param excludeSKU 除くSKU
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return レビュー一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<ReviewDO> findReviewExcludeSKUByCommunityUserId(
			String communityUserId, String excludeSKU, int limit, int offset) {
		
		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();
		
		SearchResult<ReviewDO> searchResult
				= reviewDao.findReviewExcludeSkuByCommunityUserId(
				communityUserId, excludeSKU, limit, offset);
		SearchResult<ReviewDO> result = new SearchResult<ReviewDO>();
		result.setHasAdult(searchResult.isHasAdult());
		result.setNumFound(searchResult.getNumFound());
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(searchResult.getDocuments());
		for (ReviewDO review : searchResult.getDocuments()) {
			result.updateFirstAndLast(review);
			if (review.isStop(loginCommunityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				continue;
			}
			result.getDocuments().add(review);
		}
		return result;
	}

	/**
	 * 指定したレビューを除いた、指定したレビュアーによるレビューを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @param excludeReviewId 除くレビューID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return レビュー一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<ReviewDO> findReviewExcludeReviewIdByCommunityUserId(
			String communityUserId, String sku, String excludeReviewId,
			int limit, int offset) {
		
		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();

		SearchResult<ReviewDO> searchResult
				= reviewDao.findReviewExcludeReviewIdByCommuntyUserIdAndSKU(
				sku, communityUserId, excludeReviewId, limit, offset);
		SearchResult<ReviewDO> result = new SearchResult<ReviewDO>();
		result.setHasAdult(searchResult.isHasAdult());
		result.setNumFound(searchResult.getNumFound());
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(searchResult.getDocuments());
		for (ReviewDO review : searchResult.getDocuments()) {
			result.updateFirstAndLast(review);
			if (review.isStop(loginCommunityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				continue;
			}
			result.getDocuments().add(review);
		}
		return result;
	}

	/**
	 * 指定した商品に対する購入の決め手を返します。
	 * @param product 商品
	 * @param decisivePurchaseIds 先頭に含める購入の決め手IDリスト
	 * @param withSelectedUsers 評価したユーザー情報を含めるかどうか
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param selectedUserLimit 選択済みユーザーの最大取得件数
	 * @return 購入の決め手一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<DecisivePurchaseSetVO> findDecisivePurchaseBySKU(
			ProductDO product,
			List<String> decisivePurchaseIds,
			boolean withSelectedUsers,
			int limit,
			int offset,
			int selectedUserLimit) {
		SearchResult<DecisivePurchaseSetVO> result
				= new SearchResult<DecisivePurchaseSetVO>(0, new ArrayList<DecisivePurchaseSetVO>());
		SearchResult<DecisivePurchaseDO> searchResult = 
				reviewDao.findDecisivePurchaseFromIndexBySKU(product, decisivePurchaseIds, null, limit, offset);
		result.setHasAdult(searchResult.isHasAdult());
		result.setNumFound(searchResult.getNumFound());
		result.setHasAdult(searchResult.isHasAdult());
		List<String> decisivePurchaseIdList = new ArrayList<String>();
		
		for(DecisivePurchaseDO decisivePurchase : searchResult.getDocuments() ){
			decisivePurchaseIdList.add(decisivePurchase.getDecisivePurchaseId());
		}
		Map<String, Long> decisivePurchaseCountMap = new HashMap<String, Long>();
		
		decisivePurchaseCountMap = reviewDao.decisivePurchaseCountMap(decisivePurchaseIdList, requestScopeDao.loadCommunityUserId());
		
		for (DecisivePurchaseDO decisivePurchase : searchResult.getDocuments()) {
			DecisivePurchaseSetVO vo = new DecisivePurchaseSetVO();
			vo.setDecisivePurchase(decisivePurchase);
			
			if( decisivePurchaseCountMap.containsKey(decisivePurchase.getDecisivePurchaseId())){
				vo.setOtherSelectedUserCount(decisivePurchaseCountMap.get(decisivePurchase.getDecisivePurchaseId()));
			}
			
			result.getDocuments().add(vo);
		}
		// 先頭に含める購入の決め手IDリストが指定されていた場合、その範囲内で件数の多い順に並べなおす
		if (decisivePurchaseIds != null && !decisivePurchaseIds.isEmpty()) {
			if (result.getDocuments().size() > decisivePurchaseIds.size()) {
				Collections.sort(result.getDocuments().subList(0, decisivePurchaseIds.size()), new Comparator<DecisivePurchaseSetVO>() {
					@Override
					public int compare(DecisivePurchaseSetVO o1, DecisivePurchaseSetVO o2) {
						return (int)(o2.getOtherSelectedUserCount() - o1.getOtherSelectedUserCount());
					}
				});
			}
		}
		return result;
	}

	/**
	 * 指定した商品に対する購入の決め手を返します。
	 * @param product 商品
	 * @param excludeDecisivePurchaseIds 除外する「購入の決め手」のIDリスト
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return
	 */
	@Override
	@ArroundSolr
	public SearchResult<DecisivePurchaseSetVO> findDecisivePurchaseBySKU(
			ProductDO product,
			List<String> excludeDecisivePurchaseIds,
			int limit,
			int offset
			) {
		SearchResult<DecisivePurchaseSetVO> result = new SearchResult<DecisivePurchaseSetVO>(0, new ArrayList<DecisivePurchaseSetVO>());
		SearchResult<DecisivePurchaseDO> searchResult
				= reviewDao.findDecisivePurchaseFromIndexBySKU(product, excludeDecisivePurchaseIds, limit, offset);
		result.setHasAdult(searchResult.isHasAdult());
		result.setNumFound(searchResult.getNumFound());
		result.setHasAdult(searchResult.isHasAdult());
		List<String> decisivePurchaseIdList = new ArrayList<String>();
		
		for(DecisivePurchaseDO decisivePurchase :  searchResult.getDocuments() ){
			decisivePurchaseIdList.add(decisivePurchase.getDecisivePurchaseId());
		}
		Map<String, Long> decisivePurchaseCountMap = new HashMap<String, Long>();
		
		decisivePurchaseCountMap = reviewDao.decisivePurchaseCountMap(decisivePurchaseIdList, requestScopeDao.loadCommunityUserId());
		
		for (DecisivePurchaseDO decisivePurchase : searchResult.getDocuments()) {
			DecisivePurchaseSetVO vo = new DecisivePurchaseSetVO();
			vo.setDecisivePurchase(decisivePurchase);
			
			if( decisivePurchaseCountMap.containsKey(decisivePurchase.getDecisivePurchaseId())){
				vo.setOtherSelectedUserCount(decisivePurchaseCountMap.get(decisivePurchase.getDecisivePurchaseId()));
			}
			
			result.getDocuments().add(vo);
		}
		return result;
	}
	/**
	 * 指定した商品に対する商品満足度セットを返します。
	 * @param sku SKU
	 * @param selectedUserLimit 選択済みユーザーの最大取得件数
	 * @return 商品満足度セットリスト
	 */
	@Override
	@ArroundSolr
	public List<ProductSatisfactionSetVO> findProductSatisfactionBySKU(
			String sku, int selectedUserLimit) {
		Map<ProductSatisfaction, SearchResult<CommunityUserDO>> userMap
				= reviewDao.loadProductSatisfactionUserMap(sku, selectedUserLimit);
		List<ProductSatisfactionSetVO> result = new ArrayList<ProductSatisfactionSetVO>();
		for (ProductSatisfaction satisfaction : ProductSatisfaction.values()) {
			ProductSatisfactionSetVO vo = new ProductSatisfactionSetVO();
			vo.setProductSatisfaction(satisfaction);
			vo.setOtherSelectedCommunityUsers(userMap.get(satisfaction));
			if (vo.getOtherSelectedCommunityUsers() == null) {
				vo.setOtherSelectedCommunityUsers(new SearchResult<CommunityUserDO>());
			}
			for (Iterator<CommunityUserDO> it = vo.getOtherSelectedCommunityUsers().getDocuments().iterator(); it.hasNext(); ) {
				CommunityUserDO communityUser = it.next();
				vo.getOtherSelectedCommunityUsers().updateFirstAndLast(communityUser);
				if (communityUser.isStop(requestScopeDao)) {
					vo.getOtherSelectedCommunityUsers().countUpStopContents();
					it.remove();
				}
			}
			result.add(vo);
		}
		return result;
	}

	/**
	 * 指定したコミュニティユーザーの投稿したレビューを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return レビュー一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<ReviewSetVO> findReviewByCommunityUserId(
			String communityUserId, int limit,
			Date offsetTime, boolean previous) {
		
		return appendPurchaseProduct(createReviewSets(reviewDao.findReviewByCommunityUserId(
				communityUserId,
				null,
				limit,
				offsetTime,
				previous,
				requestScopeDao.loadAdultVerification())),
				false);
	}

	/**
	 * 指定したコミュニティユーザーの投稿した一時保存レビューを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 一時保存レビュー一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<ReviewSetVO> findTemporaryReviewByCommunityUserId(
			String communityUserId, String excludeReviewId,
			int limit,
			Date offsetTime, boolean previous) {
		return appendPurchaseProduct(createReviewSets(reviewDao.findTemporaryReviewByCommunityUserId(
				communityUserId,
				excludeReviewId,
				limit,
				offsetTime,
				previous,
				requestScopeDao.loadAdultVerification())),
				false);
	}

	

	/**
	 * 指定した条件でポイント付与可能かどうか返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param product 商品
	 * @param purchaseProduct 購入商品
	 * @param pointBaseDate ポイント基本情報
	 * @return ポイント付与できる場合、true
	 */
	@Override
	@ArroundHBase
	public boolean canGrantPointReview(
			String communityUserId,
			ProductDO product,
			PurchaseProductDO purchaseProduct) {
		
		if (!product.isCanReview()) {
			return false;
		}
		if (PurchaseHistoryType.OTHER.equals(purchaseProduct.getPurchaseHistoryType())) {
			return false;
		}
		
		if (!product.isGrantPointWithinTerm(purchaseProduct.getPurchaseDate())) {
			return false;
		}
		
		int reviewTerm = product.getGrantPointReviewTerm(purchaseProduct.getPurchaseDate());
		if (purchaseProduct.isShare()) {
			boolean reviewPointActive = true;
			for (String userId : communityUserDao.findCommunityUserIdWithAccountSharingByCommunityUserId(communityUserId)) {
				if (!reviewDao.isStrictPointGrantReview(
						userId,
						product,
						reviewTerm)) {
					reviewPointActive = false;
					break;
				}
			}
			return reviewPointActive;
		} else {
			return reviewDao.isStrictPointGrantReview(communityUserId, product, reviewTerm);
		}
	}

	/**
	 * 指定した形式の一時保存レビューを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @param reviewType レビュータイプ
	 * @return 一時保存レビュー
	 */
	@Override
	@ArroundHBase
	public ReviewDO getTemporaryReview(
			String communityUserId, String sku, ReviewType reviewType) {
		for (ReviewDO review : reviewDao.findReviewByCommunityUserIdAndSKU(communityUserId, sku, reviewType, ContentsStatus.SAVE)) {
			if (!review.isWithdraw()) {
				return review;
			}
		}
		return null;
	}


	/**
	 * 指定したレビューを返します。
	 * @param reviewId レビューID
	 * @return レビュー
	 */
	@Override
	@ArroundHBase
	public ReviewDO getReview(String reviewId) {
		ReviewDO review = reviewDao.loadReview(reviewId);
		
		if( review == null || review.isDeleted() || ProductUtil.invalid(review))
			return null;
		
		return review;
	}

	/**
	 * 指定したレビューをインデックス情報から返します。
	 * @param reviewId レビューID
	 * @return レビュー情報
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@ArroundSolr
	public ReviewSetVO getReviewFromIndex(
			String reviewId, boolean includeDeleteContents) {

		ReviewDO review = reviewDao.loadReviewFromIndex(reviewId, false, includeDeleteContents);
		if (review == null || ProductUtil.invalid(review)) {
			return null;
		}

		SearchResult<ReviewDO> reviews = new SearchResult(1, new ArrayList<ReviewDO>());
		reviews.getDocuments().add(review);
		SearchResult<ReviewSetVO> reviewSets = createReviewSets(reviews);
		if (reviewSets.getDocuments().isEmpty()) {
			return null;
		}
		return reviewSets.getDocuments().get(0);
	}

	/**
	 * 指定したレビューをインデックス情報から返します。退会削除レビューは取得しない。
	 * @param reviewId レビューID
	 * @return レビュー情報
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@ArroundSolr
	public ReviewSetVO getReviewFromIndexExcludeWithdraw(
			String reviewId, boolean includeDeleteContents) {

		ReviewDO review = reviewDao.loadReviewFromIndex(reviewId, false, includeDeleteContents);
		if (review == null || ProductUtil.invalid(review)) {
			return null;
		}
		if (review.isWithdraw()){
			return null;
		}
		SearchResult<ReviewDO> reviews = new SearchResult(
				1, new ArrayList<ReviewDO>());
		reviews.getDocuments().add(review);
		SearchResult<ReviewSetVO> reviewSets = appendPurchaseProduct(createReviewSets(reviews), false);

		if (reviewSets.getDocuments().isEmpty()) {
			return null;
		}
		return reviewSets.getDocuments().get(0);
	}
	
	/**
	 * レビュー情報を登録/更新します。
	 * @param review レビュー情報
	 * @return レビュー情報
	 */
	@Override
	@ArroundHBase @ArroundSolr(commit = SolrTiming.NONE)
	public ReviewDO addReview(ReviewDO review) {
		// コンテンツ投稿可能チェック
		if(!userService.validateUserStatusForPostContents(review.getCommunityUser().getCommunityUserId()))
			throw new UnActiveException("can not post contens because user status is failure " + " communityUserId:" + review.getCommunityUser().getCommunityUserId());
		
		if( !ContentsStatus.SUBMITTED.equals(review.getStatus()))
			throw new IllegalArgumentException("Review status not sasubmitted.");
		// 商品情報とコミュニティユーザー情報のチェック
		settingProductAndCommunityUser(review);
		
		if (StringUtils.isBlank(review.getReviewId())) {
			review.setReviewId(null);
		}
		
		ReviewDO saveReview = null;
		List<ReviewDO> findReviews = null;
		// 既に保存されている場合のチェック
		if (StringUtils.isNotEmpty(review.getReviewId())) {
			saveReview = reviewDao.loadReview(
					review.getReviewId(), Path.includeProp("*").includePath(
							"communityUser.communityUserId,product.sku," +
							"reviewDecisivePurchases.decisivePurchase.decisivePurchaseId," +
							"purchaseLostProducts.product.sku," +
							"usedProducts.product.sku,mngToolOperation").depth(2), true);
			if (saveReview == null) {
				throw new DataNotFoundException("This review was deleted. reviewId = " + review.getReviewId());
			}
			// 投稿ユーザーチェック
			if (!review.getCommunityUser().getCommunityUserId().equals(saveReview.getCommunityUser().getCommunityUserId())) {
				throw new SecurityException(
						"This review is different owner. ownerId = " +saveReview.getCommunityUser().getCommunityUserId() +
						" input = " + saveReview.getCommunityUser().getCommunityUserId());
			}
			if (saveReview.isDeleted()) {
				throw new DataNotFoundException("This review was deleted. reviewId = " + review.getReviewId());
			}
			if (ContentsStatus.SUBMITTED.equals(saveReview.getStatus())) {
				throw new SecurityException("already submitted. reviewId = " + review.getReviewId());
			}
			// レビュー可否チェック
			if (!review.getProduct().isCanReview() && !ContentsStatus.SUBMITTED.equals(saveReview.getStatus())) {
				throw new IllegalArgumentException("This product can not review. sku=" + review.getProduct().getSku());
			}
		}
		
		//商品情報をロックします。
		applicationLockDao.lockForSaveReview(
				review.getProduct().getSku(),
				review.getCommunityUser().getCommunityUserId());
		
		//購入日時は購入履歴情報から取得して、セットします。
		String purchaseProductId = settingPurchaseProduct(review);
		
		if (StringUtils.isEmpty(review.getReviewId())) {
			findReviews = reviewDao.findReviewByCommunityUserIdAndSKU(
					review.getCommunityUser().getCommunityUserId(),
					review.getProduct().getSku(),
					Path.includeProp("*").includePath("product.sku").depth(1));
			
			// 購入直後レビュー重複ェック
			if(ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE.equals(review.getReviewType())){
				for (ReviewDO findReview : findReviews) {
					// 後でヨドバシ購入商品になり、すでに他社購入のレビューがある場合は、スキップする。
					if( PurchaseHistoryType.YODOBASHI.equals( review.getPurchaseProduct().getPurchaseHistoryType()) &&
							PurchaseHistoryType.OTHER.equals(findReview.getPurchaseHistoryType()))
						continue;
					// レビュータイプが違うものはスキップする。
					if( !findReview.getReviewType().equals(review.getReviewType()) )
						continue;
					// 既に購入直後レビューが登録されている場合は、重複としエラーとする
					if( !ContentsStatus.SUBMITTED.equals(findReview.getStatus()) )
						continue;
					throw new SecurityException("duplicate review type = REVIEW_IMMEDIATELY_AFTER_PURCHASE. exists reviewId = " + findReview.getReviewId());
				}
			}
		}
		
		if( saveReview != null ){
			review.setPointBaseDate(saveReview.getPointBaseDate());
			review.setPostDate(saveReview.getPostDate());
			review.setRegisterDateTime(saveReview.getRegisterDateTime());
			review.setElapsedDays(saveReview.getElapsedDays());
			review.setEffective(saveReview.isEffective());
			// FIXME 移行前に設定されている可能性があるから残す（リリース後、1ヶ月位してから消す
			review.setPointGrantRequestId(saveReview.getPointGrantRequestId());
			review.setPointGrantRequestDetails(saveReview.getPointGrantRequestDetails());
			review.setPointGrantStatus(saveReview.getPointGrantStatus());
			cancelPointGrant(review);
			// FIXME 移行前に設定されている可能性があるから残す（リリース後、1ヶ月位してから消す
		}else{
			review.setRegisterDateTime(timestampHolder.getTimestamp());
			review.setPostDate(timestampHolder.getTimestamp());
			review.setPointBaseDate(timestampHolder.getTimestamp());
			review.calcElapsedDays();
		}
		// ポイント情報を登録する。
		settingPointGrantByAddReview(review);
		
		List<String> previousReviewIdList = Lists.newArrayList();
		review.setLatestReview(true);
		if( findReviews != null ){
			for( ReviewDO findReview : findReviews ){
				if( !ContentsStatus.SUBMITTED.equals(findReview.getStatus()) )
					continue;
				
				if( !findReview.isLatestReview() )
					continue;
				
				if( findReview.getPostDate().getTime() < review.getPostDate().getTime() ){
					findReview.setLatestReview(false);
					previousReviewIdList.add(findReview.getReviewId());
					reviewDao.updateReviewForLatestReview(findReview);
				}else{
					review.setLatestReview(false);
				}
			}
		}
		// 画像投稿処理
		Map<String, ImageHeaderDO> uploadImageMap = Maps.newHashMap();
		List<SaveImageDO> saveImages = review.getSaveImages();
		if (saveImages != null && !saveImages.isEmpty()) {
			updateImageRelateContents(
					review.getCommunityUser().getCommunityUserId(),
					review.getProduct().getSku(),
					PostContentType.REVIEW,
					review.isAdult(),
					review,
					saveImages,
					uploadImageMap);
			// 保存から投稿した場合、下記項目は初期化する。
			review.setUploadImageIds(null);
			review.setSaveImages(null);
		}
		// レビュー登録・保存処理（HBase）
		String reviewHistoryId = reviewDao.saveReview(review);
		// 画像データにレビューを緋も付ける
		for (ImageHeaderDO imageHeader : uploadImageMap.values()) {
			imageHeader.setReview(review);
			if (imageHeader.getTempThumbnailImage() != null) {
				imageHeader.getTempThumbnailImage().setReview(review);
			}
			imageDao.saveUploadImageHeader(imageHeader);
		}
		
		List<ActionHistoryDO> actionHistories = Lists.newArrayList();
		//自身のフォローユーザーに向けて、アクション履歴を記録します。
		ActionHistoryDO userActionHistory = new ActionHistoryDO();
		userActionHistory.setActionHistoryType(ActionHistoryType.USER_REVIEW);
		userActionHistory.setCommunityUser(review.getCommunityUser());
		userActionHistory.setReview(review);
		userActionHistory.setProduct(review.getProduct());
		userActionHistory.setAdult(review.isAdult());
		actionHistories.add(userActionHistory);
		//商品に対してアクションを記録します。
		ActionHistoryDO productActionHistory = new ActionHistoryDO();
		productActionHistory.setActionHistoryType(ActionHistoryType.PRODUCT_REVIEW);
		productActionHistory.setCommunityUser(review.getCommunityUser());
		productActionHistory.setProduct(review.getProduct());
		productActionHistory.setReview(review);
		productActionHistory.setAdult(review.isAdult());
		actionHistories.add(productActionHistory);

		actionHistoryDao.create(actionHistories);
		String userActionHistoryId = userActionHistory.getActionHistoryId();
		String productActionHistoryId = productActionHistory.getActionHistoryId();
		
		List<String> saveImageIds = Lists.newArrayList();
		if( saveImages != null && !saveImages.isEmpty()){
			for( SaveImageDO image : saveImages)
				saveImageIds.add(image.getImageId());
		}
		
		// インデックスを更新する。（Solr）
		indexService.updateIndexForSaveReview(
				review.getReviewId(),
				reviewHistoryId,
				previousReviewIdList.toArray(new String[previousReviewIdList.size()]),
				purchaseProductId,
				uploadImageMap.keySet().toArray(new String[uploadImageMap.size()]),
				userActionHistoryId,
				productActionHistoryId);
		// メール通知処理
		mailService.sendNotifyMailForJustAfterReviewSubmit(
				review.getReviewId(), review.getProduct().getSku(),
				review.getCommunityUser().getCommunityUserId());
		// ソーシャルメディア通知処理
		socialMediaService.notifySocialMediaForReviewSubmit(review.getReviewId(),
				review.getCommunityUser().getCommunityUserId());
		
		return review;
	}
	
	/**
	 * レビュー情報を更新します。
	 * @param review レビュー情報
	 * @param uploadImageIds 画像一覧
	 * @return レビュー情報
	 */
	@Override
	@ArroundHBase @ArroundSolr(commit = SolrTiming.NONE)
	public ReviewDO modifyReview(ReviewDO review){
		if (StringUtils.isEmpty(review.getReviewId())) {
			throw new InputException("ReviewId is none");
		}
		// レビューのステータスチェック
		if (!ContentsStatus.SUBMITTED.equals(review.getStatus())){
			throw new DataNotFoundException("This question not submitted. questionId = " + review.getReviewId());
		}
		// コンテンツ投稿可能チェック
		if(!userService.validateUserStatusForPostContents(review.getCommunityUser().getCommunityUserId()))
			throw new UnActiveException("can not post contens because user status is failure " + " communityUserId:" + review.getCommunityUser().getCommunityUserId());
		// 既に登録してあるレビューチェック
		ReviewDO submitReview = reviewDao.loadReview(
				review.getReviewId(), Path.includeProp("*").includePath(
						"communityUser.communityUserId,product.sku," +
						"reviewDecisivePurchases.decisivePurchase.decisivePurchaseId," +
						"purchaseLostProducts.product.sku," +
						"usedProducts.product.sku,mngToolOperation").depth(2), true);
		// 存在チェック
		if (submitReview == null) {
			throw new DataNotFoundException("This review was deleted. reviewId = " + review.getReviewId());
		}
		if (!review.getCommunityUser().getCommunityUserId().equals(submitReview.getCommunityUser().getCommunityUserId())) {
			throw new SecurityException(
					"This question is different owner. ownerId = " +
							submitReview.getCommunityUser().getCommunityUserId() +
					" input = " + review.getCommunityUser().getCommunityUserId());
		}
		if (submitReview.isDeleted()) {
			throw new DataNotFoundException("This review was deleted. reviewId = " + review.getReviewId());
		}
		if (!ContentsStatus.SUBMITTED.equals(submitReview.getStatus())) {
			throw new SecurityException("not submitted. reviewId = " + review.getReviewId());
		}
		// 編集可能チェック(投稿時間から1時間以上立っている場合は、更新させない。
		if( !checkPostImmediatelyAfter(submitReview.getPostDate())){
			throw new UnActiveException("Past Modify period. reviewId = " + review.getReviewId());
		}
		//商品情報をロックします。
		applicationLockDao.lockForSaveReview(
				review.getProduct().getSku(),
				review.getCommunityUser().getCommunityUserId());
		// 更新しない項目をコピーする。
		review.setViewCount(submitReview.getViewCount());
		review.setReviewScore(submitReview.getReviewScore());
		review.setMngToolOperation(submitReview.isMngToolOperation());
		review.setReviewType(submitReview.getReviewType());
		review.setStatus(submitReview.getStatus());
		review.setSaveDate(submitReview.getSaveDate());
		review.setPostDate(submitReview.getPostDate());
		review.setPointBaseDate(submitReview.getPointBaseDate());
		review.setElapsedDays(submitReview.getElapsedDays());
		review.setEffective(submitReview.isEffective());
		review.setDeleteDate(submitReview.getDeleteDate());
		review.setMemo(submitReview.getMemo());
		review.setWithdraw(submitReview.isWithdraw());
		review.setWithdrawKey(submitReview.getWithdrawKey());
		review.setLatestReview(submitReview.isLatestReview());
		review.setRegisterDateTime(submitReview.getRegisterDateTime());
		//購入日時は購入履歴情報から取得して、セットします。
		settingPurchaseProduct(review);
		// 登録済みポイント付与情報の削除処理
		cancelPointGrant(review);
		// 再度ポイント付与を設定する。
		settingPointGrantByModifyReview(review);
		// 画像の比較
		// ・既にアップロードしている画像はそのままにする。
		// ・削除されている画像は削除フラグを立てる。
		// ・新規に投稿された画像は新規に追加する。
		// ・質問情報の画像一覧を更新する。
		// ・画像のキャプションを更新する。
		Map<String, ImageHeaderDO> uploadImageMap = Maps.newHashMap();
		List<String> updateImageIds = Lists.newArrayList();
		//　画像更新処理
		befoureSaveContentModifyImages(
				review, 
				PostContentType.REVIEW, 
				ImageTargetType.REVIEW, 
				uploadImageMap, 
				updateImageIds);
		// レビュー登録・保存処理（HBase）
		String reviewHistoryId = reviewDao.saveReview(review);
		// 画像後処理（テンポラリーの画像の後処理）
		for (ImageHeaderDO imageHeader : uploadImageMap.values()) {
			imageHeader.setReview(review);
			if (imageHeader.getTempThumbnailImage() != null) {
				imageHeader.getTempThumbnailImage().setReview(review);
			}
			imageDao.saveUploadImageHeader(imageHeader);
		}
		// インデックスの更新
		indexService.updateIndexForSaveReview(
				review.getReviewId(),
				reviewHistoryId,
				null,
				null,
				updateImageIds.toArray(new String[updateImageIds.size()]),
				null,
				null);
		
		return review;
	}
	
	/**
	 * レビュー情報を一時保存します。
	 * @param review レビュー情報
	 * @return レビュー情報
	 */
	@Override
	@ArroundHBase @ArroundSolr(commit = SolrTiming.NONE)
	public ReviewDO saveReview(ReviewDO review) {
		// コンテンツ投稿可能チェック
		if(!userService.validateUserStatusForPostContents(review.getCommunityUser().getCommunityUserId()))
			throw new UnActiveException("can not post contens because user status is failure " + " communityUserId:" + review.getCommunityUser().getCommunityUserId());
		
		if( !ContentsStatus.SAVE.equals(review.getStatus()))
			throw new IllegalArgumentException("Review status not save.");
		// 商品情報とコミュニティユーザー情報のチェック
		settingProductAndCommunityUser(review);
		
		if (StringUtils.isBlank(review.getReviewId())) {
			review.setReviewId(null);
		}
		
		ReviewDO oldReview = null;
		// 既に保存されている場合のチェック
		if (StringUtils.isNotEmpty(review.getReviewId())) {
			oldReview = reviewDao.loadReview(
					review.getReviewId(), Path.includeProp("*").includePath(
							"communityUser.communityUserId,product.sku," +
							"reviewDecisivePurchases.decisivePurchase.decisivePurchaseId," +
							"purchaseLostProducts.product.sku," +
							"usedProducts.product.sku,mngToolOperation").depth(2), true);
			if (oldReview == null) {
				throw new DataNotFoundException("This review was deleted. reviewId = " + review.getReviewId());
			}
			// 投稿ユーザーチェック
			if (!review.getCommunityUser().getCommunityUserId().equals(oldReview.getCommunityUser().getCommunityUserId())) {
				throw new SecurityException(
						"This review is different owner. ownerId = " +
						oldReview.getCommunityUser().getCommunityUserId() +
						" input = " + oldReview.getCommunityUser().getCommunityUserId());
			}
			if (ContentsStatus.DELETE.equals(oldReview.getStatus())) {
				throw new DataNotFoundException("This review was deleted. reviewId = " + review.getReviewId());
			}
			if (ContentsStatus.SUBMITTED.equals(oldReview.getStatus())) {
				throw new SecurityException("already submitted. reviewId = " + review.getReviewId());
			}
			// レビュー可否チェック
			if (!review.getProduct().isCanReview() && !ContentsStatus.SUBMITTED.equals(oldReview.getStatus())) {
				throw new IllegalArgumentException("This product can not review. sku=" + review.getProduct().getSku());
			}
		}else{
			// ユーザーの指定商品のレビュー一覧情報を取得する。
			List<ReviewDO> findReviews = reviewDao.findReviewByCommunityUserIdAndSKU(
					review.getCommunityUser().getCommunityUserId(),
					review.getProduct().getSku(),
					Path.includeProp("*").includePath("product.sku").depth(1));
			// 購入直後レビュー重複ェック
			if(ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE.equals(review.getReviewType())){
				for (ReviewDO findReview : findReviews) {
					// レビュータイプが違うものはスキップする。
					if( !findReview.getReviewType().equals(review.getReviewType()) )
						continue;
					// 既に購入直後レビューが登録されている場合は、重複としエラーとする
					if( !ContentsStatus.SUBMITTED.equals(findReview.getStatus()) )
						continue;
					throw new SecurityException("duplicate review type = REVIEW_IMMEDIATELY_AFTER_PURCHASE. exists reviewId = " + findReview.getReviewId());
				}
			}
			// 保存レビュー重複チェック
			for (ReviewDO findReview : findReviews) {
				if( !findReview.getReviewType().equals(review.getReviewType()) )
					continue;
				// 既に保存レビューが登録されている場合は、重複エラーとする。
				if( !ContentsStatus.SAVE.equals(findReview.getStatus()) )
					continue;
				throw new SecurityException("duplicate save review. exists reviewId = " + findReview.getReviewId());
			}
			
			// レビュー可否チェック
			if (!review.getProduct().isCanReview()) {
				throw new IllegalArgumentException("This product can not review. sku=" + review.getProduct().getSku());
			}
		}
		//商品情報をロックします。
		applicationLockDao.lockForSaveReview(
				review.getProduct().getSku(),
				review.getCommunityUser().getCommunityUserId());
		
		// 現行、購入日は変更できない仕様となっている。
		// 他社購入の場合は、変更できてもいいのではないか？
		String purchaseProductId = settingPurchaseProduct(review);
		
		if (oldReview != null) {
			review.setPointBaseDate(oldReview.getPointBaseDate());
			review.setPostDate(oldReview.getPostDate());
			review.setRegisterDateTime(oldReview.getRegisterDateTime());
			review.setElapsedDays(oldReview.getElapsedDays());
			review.setEffective(oldReview.isEffective());
			// FIXME 移行前に設定されている可能性があるから残す（リリース後、1ヶ月位してから消す
			review.setPointGrantRequestId(oldReview.getPointGrantRequestId());
			review.setPointGrantRequestDetails(oldReview.getPointGrantRequestDetails());
			review.setPointGrantStatus(oldReview.getPointGrantStatus());
			cancelPointGrant(review);
			// FIXME 移行前に設定されている可能性があるから残す（リリース後、1ヶ月位してから消す
		} else {
			review.setPostDate(timestampHolder.getTimestamp());
			review.setRegisterDateTime(timestampHolder.getTimestamp());
			review.setPointBaseDate(timestampHolder.getTimestamp());
			review.calcElapsedDays();
		}
		// レビュー登録・保存処理（HBase）
		reviewDao.saveReview(review);
		// インデックスを更新する。（Solr）
		indexService.updateIndexForSaveReview(
				review.getReviewId(),
				null,
				null,
				purchaseProductId,
				null,
				null,
				null);
		
		return review;
	}
	/**
	 * 指定した特別条件からポイント付与情報を取得します。
	 * @param review レビュー
	 * @param orderDate 注文日付
	 * @param incentiveType インセンティブタイプ
	 * @param rvwSps 特別ポイント条件リスト
	 * @return ポイント付与情報
	 */
	private PointGrantRequestDetail getPointGrantRequestDetail(
			ReviewDO review,
			PointIncentiveType incentiveType,
			ReviewPointSpecial[] rvwSps,
			Set<String> ignoreSpCodes) {
		ProductDO product = review.getProduct();
		for (ReviewPointSpecial reviewPointSpecial : rvwSps) {
			if(ignoreSpCodes.contains(reviewPointSpecial.getRvwSpCd())) {
				continue;
			}
			Date baseDate = null;
			if (reviewPointSpecial.isPurchaseJudgeType()) {
				baseDate = review.getPurchaseDate();
			} else if (reviewPointSpecial.isOrderJudgeType()) {
				baseDate = review.getPointBaseDate();
			} else if (reviewPointSpecial.isPostJudgeType()) {
				baseDate = review.getPointBaseDate();
			} else {
				LOG.error("This rvwSpJdgTyp is invalid. sku="
						+ product.getSku() + ", rvwSpJdgTyp="
						+ reviewPointSpecial.getRvwSpJdgTyp());
				continue;
			}
			if (DateUtil.matchTerm(
					reviewPointSpecial.getRvwSpSttTm(),
					reviewPointSpecial.getRvwSpEdTm(),
					baseDate)) {
				PointGrantRequestDetail detail = new PointGrantRequestDetail();
				detail.setType(incentiveType);
				detail.setPointGrantType(reviewPointSpecial.getPtTyp());
				boolean rvwSpDtlInvalid = false;
				if (reviewPointSpecial.getRvwSpDtls() == null
						|| reviewPointSpecial.getRvwSpDtls().length == 0) {
					LOG.error("RvwSpDtls is not found. "
							+ "sku=" + product.getSku()
							+ ", specialCondCode=" + reviewPointSpecial.getRvwSpCd()
							+ ", communityUserId=" + review.getCommunityUser().getCommunityUserId()
							+ ", communityId=" + review.getCommunityUser().getCommunityId());
					continue;
				}
				ReviewPointSpecialDetail[] rvwSpDtls = reviewPointSpecial.getRvwSpDtls();
				for (ReviewPointSpecialDetail rvwSpDtl : rvwSpDtls) {
					if (rvwSpDtl.getRvwSpDtlPt() == null
							|| rvwSpDtl.getRvwSpDtlPt() == 0) {
						rvwSpDtlInvalid = true;
						break;
					}
				}
				if (rvwSpDtlInvalid) {
					LOG.error("ReviewPointSpecialDetail is invalid. because point is zero."
							+ "sku=" + product.getSku()
							+ ", specialCondCode=" + reviewPointSpecial.getRvwSpCd()
							+ ", communityUserId=" + review.getCommunityUser().getCommunityUserId()
							+ ", communityId=" + review.getCommunityUser().getCommunityId());
					continue;
				}
				if (reviewPointSpecial.isFirstCondType()) {
					Integer arrivalPointRanking = simplePmsDao.reserveSpecialArrivalPoint(
							review.getCommunityUser().getCommunityId(),
							reviewPointSpecial.getRvwSpCd());
					if (arrivalPointRanking == null) {
						continue;
					}
					detail.setSpecialCondCode(reviewPointSpecial.getRvwSpCd());
					for (ReviewPointSpecialDetail rvwSpDtl : rvwSpDtls) {
						if (rvwSpDtl.getRvwSpDtlSttThdNum() <= arrivalPointRanking
								&& rvwSpDtl.getRvwSpDtlEdThdNum() >= arrivalPointRanking) {
							detail.setPoint(rvwSpDtl.getRvwSpDtlPt());
							break;
						}
					}
					if (detail.getPoint() == null) {
						LOG.error("Reserve Condition is not found. "
								+ "sku=" + product.getSku()
								+ ", specialCondCode=" + reviewPointSpecial.getRvwSpCd()
								+ ", communityUserId=" + review.getCommunityUser().getCommunityUserId()
								+ ", communityId=" + review.getCommunityUser().getCommunityId()
								+ ", arrivalPointRanking=" + arrivalPointRanking
								+ ", rvwSpDtls=" + Arrays.toString(rvwSpDtls));
						continue;
					}
					return detail;
				} else if (reviewPointSpecial.isTermCondType()) {
					for (ReviewPointSpecialDetail rvwSpDtl : rvwSpDtls) {
						if (DateUtil.matchTerm(
								rvwSpDtl.getRvwSpDtlSttThdDt(),
								rvwSpDtl.getRvwSpDtlEdThdDt(),
								baseDate)) {
							detail.setPoint(rvwSpDtl.getRvwSpDtlPt());
							break;
						}
					}
					detail.setSpecialCondCode(reviewPointSpecial.getRvwSpCd());
					if (detail.getPoint() == null) {
						LOG.error("Reserve Condition is not found. "
								+ "sku=" + product.getSku()
								+ ", specialCondCode=" + reviewPointSpecial.getRvwSpCd()
								+ ", communityUserId=" + review.getCommunityUser().getCommunityUserId()
								+ ", communityId=" + review.getCommunityUser().getCommunityId()
								+ ", userDate=" + baseDate
								+ ", rvwSpDtls=" + Arrays.toString(rvwSpDtls));
						continue;
					}
					return detail;
				} else {
					LOG.error("This rvwSpTyp is invalid. sku="
							+ product.getSku() + ", rvwSpTyp="
							+ reviewPointSpecial.getRvwSpTyp());
					continue;
				}
			}
		}
		return null;
	}

	/**
	 * レビュー投稿に際して付与されるポイントを計算します。
	 * @param review レビュー
	 * @return 付与されるポイント詳細リスト
	 */
	private List<PointGrantRequestDetail> getPointGrantRequestDetails(ReviewDO review) {
		List<PointGrantRequestDetail> details = Lists.newArrayList();
		ProductDO product = review.getProduct();
		Set<String> ignoreSpCodes = getIgnoreSpCode(review.getCommunityUser().getCommunityId() ,product.getRvwSpCodes());
		ReviewPointSpecial[] rvwSps = product.getRvwSps();
		if (rvwSps != null) {
			PointGrantRequestDetail detail = getPointGrantRequestDetail(
							review,
							PointIncentiveType.SPECIAL_COND,
							rvwSps,
							ignoreSpCodes);
			if (detail != null) {
				details.add(detail);
				if (detail.isPointReplace()) {
					return details;
				}
			}
		}
		
		if (ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE.equals(review.getReviewType())) {
			ReviewPoint reviewPoint = product.getReviewPoint(PointIncentiveType.IMMEDIATELY_AFTER_DECISIVE_PURCHASE);
			if (reviewPoint != null && review.getReviewDecisivePurchases().size() > 0) {
				PointGrantRequestDetail detail = getPoint(
						review, 
						PointIncentiveType.IMMEDIATELY_AFTER_DECISIVE_PURCHASE,
						reviewPoint,
						ignoreSpCodes);
				if (detail != null) {
					details.add(detail);
				}
			}
			reviewPoint = product.getReviewPoint(PointIncentiveType.IMMEDIATELY_AFTER_PURCHASE_LOST_PRODUCT);
			if (reviewPoint != null
					&& review.getPurchaseLostProducts().size() > 0) {
				PointGrantRequestDetail detail = getPoint(
						review,
						PointIncentiveType.IMMEDIATELY_AFTER_PURCHASE_LOST_PRODUCT,
						reviewPoint,
						ignoreSpCodes);
				if (detail != null) {
					details.add(detail);
				}
			}
			reviewPoint = product.getReviewPoint(PointIncentiveType.IMMEDIATELY_AFTER_USED_PRODUCT);
			if (reviewPoint != null&& review.getUsedProducts().size() > 0) {
				PointGrantRequestDetail detail = getPoint(
						review,
						PointIncentiveType.IMMEDIATELY_AFTER_USED_PRODUCT,
						reviewPoint,
						ignoreSpCodes);
				if (detail != null) {
					details.add(detail);
				}
			}
			reviewPoint = product.getReviewPoint(PointIncentiveType.IMMEDIATELY_AFTER_REVIEW);
			if (reviewPoint != null) {
				PointGrantRequestDetail detail = getPoint(
						review,
						PointIncentiveType.IMMEDIATELY_AFTER_REVIEW,
						reviewPoint,
						ignoreSpCodes);
				if (detail != null) {
					details.add(detail);
				}
			}
			reviewPoint = product.getReviewPoint(PointIncentiveType.IMMEDIATELY_SATISFACTION);
			if (reviewPoint != null && review.getReviewDecisivePurchases().size() > 0) {
				PointGrantRequestDetail detail = getPoint(
						review,
						PointIncentiveType.IMMEDIATELY_SATISFACTION,
						reviewPoint,
						ignoreSpCodes);
				if (detail != null) {
					details.add(detail);
				}
			}
		} else if (ReviewType.REVIEW_AFTER_FEW_DAYS.equals(review.getReviewType())) {
			ReviewPoint reviewPoint = product.getReviewPoint(
					PointIncentiveType.AFTER_FEW_DAYS_SATISFACTION);
			if (reviewPoint != null &&
					review.getProductSatisfaction() != null &&
					!review.getProductSatisfaction().equals(ProductSatisfaction.NONE)) {
				PointGrantRequestDetail detail = getPoint(
						review,
						PointIncentiveType.AFTER_FEW_DAYS_SATISFACTION,
						reviewPoint,
						ignoreSpCodes);
				if (detail != null) {
					details.add(detail);
				}
			}
			reviewPoint = product.getReviewPoint(PointIncentiveType.AFTER_FEW_DAYS_ALSO_BUY);
			if (reviewPoint != null &&
					review.getAlsoBuyProduct() != null &&
					!AlsoBuyProduct.NONE.equals(review.getAlsoBuyProduct())) {
				PointGrantRequestDetail detail = getPoint(
						review,
						PointIncentiveType.AFTER_FEW_DAYS_ALSO_BUY,
						reviewPoint,
						ignoreSpCodes);
				if (detail != null) {
					details.add(detail);
				}
			}
			reviewPoint = product.getReviewPoint(PointIncentiveType.AFTER_FEW_DAYS_REVIEW);
			if (reviewPoint != null) {
				PointGrantRequestDetail detail = getPoint(
						review,
						PointIncentiveType.AFTER_FEW_DAYS_REVIEW,
						reviewPoint,
						ignoreSpCodes);
				if (detail != null) {
					details.add(detail);
				}
			}
		} else {
			throw new IllegalArgumentException("ReviewType is invalid.");
		}

		return details;
	}

	/**
	 * レビュー投稿に際して付与されるポイントを計算します。(SP除外)
	 * @param reviewType レビュータイプ
	 * @param product 商品情報
	 * @param elapsedDays 購入日から投稿日までの経過日数
	 * @return 付与されるポイント詳細リスト
	 */
	public List<PointGrantRequestDetail> getPointGrantRequestDetailsWithoutSp(ReviewType reviewType, ProductDO product, Integer elapsedDays) {
		List<PointGrantRequestDetail> details = Lists.newArrayList();

		if (ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE.equals(reviewType)) {
			ReviewPoint reviewPoint = product.getReviewPoint(PointIncentiveType.IMMEDIATELY_AFTER_DECISIVE_PURCHASE);
			if (reviewPoint != null) {
				PointGrantRequestDetail detail = getPointWithoutSP(
						PointIncentiveType.IMMEDIATELY_AFTER_DECISIVE_PURCHASE,
						reviewPoint,
						elapsedDays);
				if (detail != null) {
					details.add(detail);
				}
			}
			reviewPoint = product.getReviewPoint(PointIncentiveType.IMMEDIATELY_AFTER_PURCHASE_LOST_PRODUCT);
			if (reviewPoint != null) {
				PointGrantRequestDetail detail = getPointWithoutSP(
						PointIncentiveType.IMMEDIATELY_AFTER_PURCHASE_LOST_PRODUCT,
						reviewPoint,
						elapsedDays);
				if (detail != null) {
					details.add(detail);
				}
			}
			reviewPoint = product.getReviewPoint(PointIncentiveType.IMMEDIATELY_AFTER_USED_PRODUCT);
			if (reviewPoint != null) {
				PointGrantRequestDetail detail = getPointWithoutSP(
						PointIncentiveType.IMMEDIATELY_AFTER_USED_PRODUCT,
						reviewPoint,
						elapsedDays);
				if (detail != null) {
					details.add(detail);
				}
			}
			reviewPoint = product.getReviewPoint(PointIncentiveType.IMMEDIATELY_AFTER_REVIEW);
			if (reviewPoint != null) {
				PointGrantRequestDetail detail = getPointWithoutSP(
						PointIncentiveType.IMMEDIATELY_AFTER_REVIEW,
						reviewPoint,
						elapsedDays);
				if (detail != null) {
					details.add(detail);
				}
			}
		} else if (ReviewType.REVIEW_AFTER_FEW_DAYS.equals(reviewType)) {
			ReviewPoint reviewPoint = product.getReviewPoint(PointIncentiveType.AFTER_FEW_DAYS_SATISFACTION);
			if (reviewPoint != null) {
				PointGrantRequestDetail detail = getPointWithoutSP(
						PointIncentiveType.AFTER_FEW_DAYS_SATISFACTION,
						reviewPoint,
						elapsedDays);
				if (detail != null) {
					details.add(detail);
				}
			}
			reviewPoint = product.getReviewPoint(PointIncentiveType.AFTER_FEW_DAYS_ALSO_BUY);
			if (reviewPoint != null) {
				PointGrantRequestDetail detail = getPointWithoutSP(
						PointIncentiveType.AFTER_FEW_DAYS_ALSO_BUY,
						reviewPoint,
						elapsedDays);
				if (detail != null) {
					details.add(detail);
				}
			}
			reviewPoint = product.getReviewPoint(PointIncentiveType.AFTER_FEW_DAYS_REVIEW);
			if (reviewPoint != null) {
				PointGrantRequestDetail detail = getPointWithoutSP(
						PointIncentiveType.AFTER_FEW_DAYS_REVIEW,
						reviewPoint,
						elapsedDays);
				if (detail != null) {
					details.add(detail);
				}
			}
		} else {
			throw new IllegalArgumentException("ReviewType is invalid.");
		}

		return details;
	}
	
	/**
	 * ポイントを返します。
	 * @param review レビュー
	 * @param product 注文日付
	 * @param incentiveType インセンティブタイプ
	 * @param reviewPoint レビューポイント情報
	 * @return ポイント
	 */
	private PointGrantRequestDetail getPoint(
			ReviewDO review,
			PointIncentiveType incentiveType,
			ReviewPoint reviewPoint,
			Set<String> ignoreSpCodes) {
		PointGrantRequestDetail detail = new PointGrantRequestDetail();
		detail.setType(incentiveType);

		ReviewPointDetail baseDetail = reviewPoint.getReviewPointDetail(review.getElapsedDays());
		if (baseDetail == null) {
			return null;
		}
		long point = baseDetail.getRvwBasePt();
		if (reviewPoint.getRvwSps() != null && reviewPoint.getRvwSps().length > 0) {
			PointGrantRequestDetail special = getPointGrantRequestDetail(
					review,
					incentiveType,
					reviewPoint.getRvwSps(),
					ignoreSpCodes);
			if (special != null) {
				detail.setSpecialCondCode(special.getSpecialCondCode());
				if (special.isPointReplace()) {
					point = special.getPoint();
				} else {
					point += special.getPoint();
				}
			}
		}
		detail.setPoint(point);
		return detail;
	}

	/**
	 * ポイントを返します。(SP除外)
	 * @param incentiveType インセンティブタイプ
	 * @param reviewPoint レビューポイント情報
	 * @param elapsedDays 購入日から投稿日までの経過日数
	 * @return ポイント
	 */
	private PointGrantRequestDetail getPointWithoutSP(
			PointIncentiveType incentiveType,
			ReviewPoint reviewPoint,
			Integer elapsedDays) {
		PointGrantRequestDetail detail = new PointGrantRequestDetail();
		detail.setType(incentiveType);

		ReviewPointDetail baseDetail = reviewPoint.getReviewPointDetail(elapsedDays);
		if (baseDetail == null) {
			return null;
		}
		detail.setPoint(baseDetail.getRvwBasePt());
		return detail;
	}

	@Override
	@ArroundHBase
	public void deleteReview(String reviewId) {
		deleteReview(reviewId, false);
	}

	/**
	 * 指定したレビュー情報を削除します。
	 * @param reviewId レビューID
	 */
	@Override
	@ArroundHBase
	public void deleteReview(String reviewId, boolean mngToolOperation) {
		ReviewDO review = reviewDao.loadReview(
				reviewId, Path.includeProp("reviewBody,status,pointGrantStatus,pointGrantRequestId,cancelPointGrantType,latestReview")
							.includePath("communityUser.communityUserId,product.sku,imageHeaders.imageId").depth(1), true);
		
		if (review == null || ContentsStatus.DELETE.equals(review.getStatus())) {
			return;
		}

		// 管理ツールから来た場合は非チェック
		if(!mngToolOperation){
			// レビュー保持ユーザーとアクセスユーザーのチェック
			String accessUserId = requestScopeDao.loadCommunityUserId();
			if (!review.getCommunityUser().getCommunityUserId().equals(accessUserId)) {
				throw new SecurityException(
						"This review is different owner. ownerId = " +
						review.getCommunityUser().getCommunityUserId() +
						" input = " + accessUserId);
			}
			// コンテンツ投稿可能チェック
			if(!userService.validateUserStatusForPostContents(review.getCommunityUser().getCommunityUserId()))
				throw new UnActiveException("can not post contens because user status is failure " + " communityUserId:" + review.getCommunityUser().getCommunityUserId());
		}

		//テンポラリの場合は、画像を抽出して削除。関連情報も削除。
		Set<String> updateImageIds = Sets.newHashSet();
		imageService.deleteImagesInContents(
				review.getCommunityUser().getCommunityUserId(),
				PostContentType.REVIEW,
				reviewId,
				review,
				updateImageIds);
		
		boolean logical = (ContentsStatus.SUBMITTED.equals(review.getStatus()) || ContentsStatus.CONTENTS_STOP.equals(review.getStatus()));
		boolean effective = review.isEffective();
		
		if(CancelPointGrantType.CONTENTS_STOP.equals(review.getCancelPointGrantType())){
			effective = false;
		}
		
		String updateLatestReviewId = null;
		
		if( review.isLatestReview() ){
			List<ReviewDO> findReviews = reviewDao.findReviewByCommunityUserIdAndSKU(
					review.getCommunityUser().getCommunityUserId(),
					review.getProduct().getSku(),
					Path.includeProp("*").includePath("product.sku").depth(1));
			
			if( findReviews != null ){
				ReviewDO updateLatestReview = null;
				for( ReviewDO findReview : findReviews ){
					if( reviewId.equals(findReview.getReviewId()) 
							|| !ContentsStatus.SUBMITTED.equals(findReview.getStatus()))
						continue;
					
					if( updateLatestReview == null 
							|| updateLatestReview.getPostDate().getTime() < findReview.getPostDate().getTime() )
						updateLatestReview = findReview;
				}
				
				if( updateLatestReview != null ){
					updateLatestReview.setLatestReview(true);
					updateLatestReviewId = updateLatestReview.getReviewId();
					reviewDao.updateReviewForLatestReview(updateLatestReview);
				}
			}
		}
		
		if (review.getPointGrantRequestId() != null
				&& review.getCancelPointGrantType() == null) {
			// レビュー削除処理
			reviewDao.deleteReview(
					reviewId, 
					effective, 
					logical, 
					CancelPointGrantType.DELETE_REVIEW, 
					mngToolOperation);
			// ポイント付与申請キャンセル処理
			simplePmsDao.cancelPointGrant(
					review.getPointGrantRequestId(),
					CancelPointGrantType.DELETE_REVIEW);
		}else{
			// レビュー削除処理
			reviewDao.deleteReview(
					reviewId, 
					effective, 
					logical, 
					null, 
					mngToolOperation);
		}
		
		indexService.updateIndexForSaveReview(
				review.getReviewId(),
				null,
				(updateLatestReviewId == null ? null : new String[]{updateLatestReviewId}),
				null,
				(logical?updateImageIds.toArray(new String[updateImageIds.size()]):null),
				null,
				null);
	}
	
	/**
	 * レビューのスコア情報と閲覧数を更新します。
	 * <p>Localの商品テーブル(DBProductDetail等）を参照するため、夜間バッチ(MapReduce)専用のメソッドです。</p>
	 * @param targetDate 対象日付
	 * @param review レビュー情報
	 * @param scoreFactor スコア係数
	 */
	@Override
	@ArroundSolr
	@ArroundHBase
	public void updateReviewScoreAndViewCountForBatch(
			Date targetDate,
			ReviewDO review,
			ScoreFactorDO scoreFactor) {

		List<String> reviewIds = new ArrayList<String>();
		long commentCount = 0;
		reviewIds.add(review.getReviewId());
		Map<String, Long> commentCountMap = commentDao.loadReviewCommentCountMap(reviewIds,
				review.getCommunityUser().getCommunityUserId());
		if (commentCountMap.containsKey(review.getReviewId())) {
			commentCount = commentCountMap.get(review.getReviewId());
		}
		long likeCount = 0;
		Map<String, Long> likeCountMap = likeDao.loadReviewLikeCountMap(reviewIds);
		if (likeCountMap.containsKey(review.getReviewId())) {
			likeCount = likeCountMap.get(review.getReviewId());
		}
		long followerCount = communityUserFollowDao.findFollowerCommunityUserByCommunityUserId(
				review.getCommunityUser().getCommunityUserId(), null, 0, 0).getNumFound();
		long viewCount = uniqueUserViewCountDao.loadViewCountByContentsId(
				review.getReviewId(),
				UniqueUserViewCountType.REVIEW,
				serviceConfig.readLimit);
		int elapsedDays = DateUtil.getElapsedDays(review.getPostDate());

		long contentBodyCount = 0;

		if(!StringUtils.isEmpty(review.getReviewBody())){
			contentBodyCount = StringUtil.stripTags(review.getReviewBody()).length();
		}

		long contentImageCount = imageDao.countReviewsImage(review.getReviewId());
		BigDecimal contentBodyScore = new BigDecimal("0");
		if(PointScoreTerm.SCORE_0_TO_99.getStartTerm() <= contentBodyCount
				&& contentBodyCount <= PointScoreTerm.SCORE_0_TO_99.getEndTerm()){
			contentBodyScore = scoreFactor.getReviewContentsCountTerm0to99();
		}else if(PointScoreTerm.SCORE_100_TO_199.getStartTerm() <= contentBodyCount
			&& contentBodyCount <= PointScoreTerm.SCORE_100_TO_199.getEndTerm()){
			contentBodyScore = scoreFactor.getReviewContentsCountTerm100to199();
		}else if(PointScoreTerm.SCORE_200_TO_299.getStartTerm() <= contentBodyCount
				&& contentBodyCount <= PointScoreTerm.SCORE_200_TO_299.getEndTerm()){
			contentBodyScore = scoreFactor.getReviewContentsCountTerm200to299();
		}else if(PointScoreTerm.SCORE_300_TO_399.getStartTerm() <= contentBodyCount
				&& contentBodyCount <= PointScoreTerm.SCORE_300_TO_399.getEndTerm()){
			contentBodyScore = scoreFactor.getReviewContentsCountTerm300to399();
		}else if(PointScoreTerm.SCORE_400_TO_449.getStartTerm() <= contentBodyCount
				&& contentBodyCount <= PointScoreTerm.SCORE_400_TO_449.getEndTerm()){
			contentBodyScore = scoreFactor.getReviewContentsCountTerm400to449();
		}else if(PointScoreTerm.SCORE_450_TO_499.getStartTerm() <= contentBodyCount
				&& contentBodyCount <= PointScoreTerm.SCORE_450_TO_499.getEndTerm()){
			contentBodyScore = scoreFactor.getReviewContentsCountTerm450to499();
		}else if(PointScoreTerm.SCORE_MORE_500.getStartTerm() <= contentBodyCount){
			contentBodyScore = scoreFactor.getReviewContentsCountTermMore500();
		}
		BigDecimal contentImageScore = new BigDecimal("0");
		if(contentImageCount > 0){
			contentImageScore = scoreFactor.getReviewHasImages();
		}

		BigDecimal score = new BigDecimal((elapsedDays - 1) * -1).multiply(
				scoreFactor.getReviewDay());
		score = score.add(new BigDecimal(commentCount).multiply(
				scoreFactor.getReviewCommentCount()));
		score = score.add(new BigDecimal(likeCount).multiply(
				scoreFactor.getReviewLikeCount()));
		score = score.add(new BigDecimal(viewCount).multiply(
				scoreFactor.getReviewViewCount()));
		score = score.add(new BigDecimal(followerCount).multiply(
				scoreFactor.getReviewFollowerCount()));
		score = score.add(contentBodyScore.multiply(
				scoreFactor.getReviewContentsCountCoefficient()));
		score = score.add(contentImageScore.multiply(
				scoreFactor.getReviewHasImagesCoefficient()));

		// updateReviewScoreAndViewCountWithIndex内でReviewDOのLoadをしており、depth指定でProductDO(Catalog)との通信が発生している
		// updateReviewScoreAndViewCountWithIndex()内を修正し、ProductDOをLocalのSolr（DBProductDetail)から取得するよう修正
		review.setReviewScore(score.doubleValue());
		review.setViewCount(viewCount);
		review.setModifyDateTime(timestampHolder.getTimestamp());
		reviewDao.updateReviewScoreAndViewCountWithIndexForBatch(review);
		
		DailyScoreFactorDO dailyScoreFactor = new DailyScoreFactorDO();
		dailyScoreFactor.setType(DailyScoreFactorType.REVIEW);
		dailyScoreFactor.setTargetDate(targetDate);
		dailyScoreFactor.setContentsId(review.getReviewId());
		dailyScoreFactor.setSku(review.getProduct().getSku());
		dailyScoreFactor.setElapsedDays(elapsedDays);
		dailyScoreFactor.setCommentCount(commentCount);
		dailyScoreFactor.setLikeCount(likeCount);
		dailyScoreFactor.setViewCount(viewCount);
		dailyScoreFactor.setFollowerCount(followerCount);
		dailyScoreFactor.setPostDate(review.getPostDate());
		dailyScoreFactor.setContentBodyCount(contentBodyCount);
		dailyScoreFactor.setContentImageCount(contentImageCount);

		dailyScoreFactorDao.createDailyScoreFactorForBatch(dailyScoreFactor);
	}

	@Override
	@ArroundSolr
	@ArroundHBase
	public void updateReviewScoreAndViewCountForBatchBegin(int bulkSize) {
		reviewDao.updateReviewScoreAndViewCountWithIndexForBatchBegin(bulkSize);
		dailyScoreFactorDao.createDailyScoreFactorForBatchBegin(bulkSize);
	}
	
	@Override
	@ArroundSolr
	@ArroundHBase
	public void updateReviewScoreAndViewCountForBatchEnd() {
		reviewDao.updateReviewScoreAndViewCountWithIndexForBatchEnd();
		dailyScoreFactorDao.createDailyScoreFactorForBatchEnd();
	}

	/**
	 * ポイント付与結果をフィードバックします。
	 * @param pointGrantRequestId ポイント付与ID
	 * @param point ポイント
	 * @param status 結果ステータス
	 */
	@Override
	@ArroundSolr
	@ArroundHBase
	public void feedbackPointGrant(
			String pointGrantRequestId,
			Long point,
			String status,
			Date feedbackDate) {
		ReviewDO review = reviewDao.loadReviewByPointGrantRequestId(pointGrantRequestId);
		if (review == null) {
			throw new IllegalStateException(
					"Review is not found. pointGrantRequestId=" + pointGrantRequestId);
		}
		if (review.getPointGrantStatus() != null &&
				review.getPointGrantStatus().getCode().equals(status)) {
			return;
		}
		if (status.equals(FeedbackPointGrantStatus.ADD.getCode())){
			// 付与の場合
			review.setPointGrantStatus(PointGrantStatus.ADD);
			review.setGrantPoint(point);
		} else if (status.equals(FeedbackPointGrantStatus.REJECT.getCode())) {
			// 却下の場合
			review.setPointGrantStatus(PointGrantStatus.REJECT);
		} else if (status.equals(FeedbackPointGrantStatus.DEPRIVE.getCode())) {
			// 剥奪の場合
			review.setPointGrantStatus(PointGrantStatus.DEPRIVE);
		} else {
			throw new IllegalStateException(
					"illegal status. status=" + status);
		}
		reviewDao.updateReviewForPointGrantFeedback(review);

		String informationId = null;
		if (!review.getPointGrantStatus().equals(PointGrantStatus.REJECT)) {
			InformationDO information = new InformationDO();
			if (review.getPointGrantStatus().equals(PointGrantStatus.ADD)) {
				information.setInformationType(InformationType.POINT_REVIEW);
			} else {
				information.setInformationType(InformationType.DEPRIVE_POINT);
			}
			information.setCommunityUser(review.getCommunityUser());
			information.setReview(review);
			information.setAdult(information.getReview().isAdult());
			information.setRelationCommunityUserId(
					review.getCommunityUser().getCommunityUserId());
			information.setGrantPoint(point);
			// メンテナンスのためFeedbackDateがある場合は、InformationTimeに設定する。
			if( feedbackDate != null){
				information.setInformationTime(DateUtils.truncate(feedbackDate, Calendar.DATE));
				information.setRegisterDateTime(feedbackDate);
				information.setModifyDateTime(feedbackDate);
			}
			informationDao.createInformation(information);
			informationId = information.getInformationId();
		}
		indexService.updateIndexForPointGrantFeedback(
				review.getReviewId(), informationId);

	}

	@Override
	public SearchResult<ReviewSetVO> loadReviewSet(String reviewId) {
		ReviewDO review = reviewDao.loadReview(reviewId);
		if (ProductUtil.invalid(review)) {
			return new SearchResult<ReviewSetVO>();
		}
		List<ReviewDO> documents = new ArrayList<ReviewDO>();
		documents.add(review);
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(documents.size(), documents);
		return appendPurchaseProduct(createReviewSets(searchResult), false);
	}

	@Override
	public boolean isShowReview(String reviewId) {
		ReviewDO review = reviewDao.loadReviewFromIndex(reviewId, false);
		String communityUserId = requestScopeDao.loadCommunityUserId();
		 if(review != null && review.getCommunityUser().getStatus().equals(CommunityUserStatus.STOP)
				&& !review.getCommunityUser().getCommunityUserId().equals(communityUserId)){
			 return false;
		 }
		return true;
	}

	@Override
	public ReviewDO loadReview(String reviewId) {
		ReviewDO review = reviewDao.loadReview(reviewId);
		if (ProductUtil.invalid(review)) {
			return null;
		} else {
			return review;
		}
	}

	@Override
	public long countPostReviewCount(String communityUserId, String sku) {
		return reviewDao.countPostReviewCount(communityUserId, sku);
	}

	@Override
	public Map<String, Long> countPostReviewBySku(String[] skus) {
		return reviewDao.countPostReviewBySku(skus);
	}

	@Override
	public Set<String> getAlreadyGrantSpCode(String sku, String communityUserId) {
		ProductDO product = productDao.loadProduct(sku);
		return getAlreadyGrantSpCode(product, communityUserId);
	}

	@Override
	public Set<String> getAlreadyGrantSpCode(ProductDO product, String communityUserId) {
		CommunityUserDO communityUser = communityUserDao.load(communityUserId, Path.includeProp("*"));
		Set<String> result = getIgnoreSpCode(communityUser.getCommunityId() ,product.getRvwSpCodes());
		if(result == null)
			result = new HashSet<String>();
		return result; 
	}

	@Override
	public String findProductSku(String reviewId) {
		return reviewDao.findProductSku(reviewId);
	}
	
	private Set<String> getIgnoreSpCode(String communityId ,String[] specialConditionCodes){
		List<ReviewPointSpecialConditionValidateDO> reviewPointSpecialConditionValidates = null;
		if(specialConditionCodes != null && specialConditionCodes.length > 0){
			reviewPointSpecialConditionValidates = simplePmsDao
					.confirmReviewPointSpecialCondition(
							resourceConfig.pmsExternalSystem, communityId,
							specialConditionCodes);
		}
		Set<String> ignoreSpCodes = new HashSet<String>();
		if(reviewPointSpecialConditionValidates !=null && !reviewPointSpecialConditionValidates.isEmpty()){
			for(ReviewPointSpecialConditionValidateDO reviewPointSpecialCondition:reviewPointSpecialConditionValidates){
				if(!ReviewPointSpecialConditionValidateType.codeOf(reviewPointSpecialCondition.getValidateStatus()).equals(ReviewPointSpecialConditionValidateType.YET_REGISTRED)){
					ignoreSpCodes.add(reviewPointSpecialCondition.getSpecialConditionCode());
				}
			}
		}
		return ignoreSpCodes;
	}
	private void settingProductAndCommunityUser(ReviewDO review){
		// 商品とポイント情報を取得して設定します。
		if( review.getProduct() == null || review.getProduct().getSku() == null){
			ProductDO product = productDao.loadProduct(review.getProduct().getSku());
			if( product == null ){
				throw new IllegalArgumentException("Product is null. sku = " + review.getProduct().getSku());
			}
			review.setProduct(product);
			review.setAdult(review.getProduct().isAdult());
		}
		
		if (!review.getProduct().isCanReview()) {
			throw new IllegalArgumentException("This product can not review. sku=" + review.getProduct().getSku());
		}
		// コミュニティユーザー情報を取得して設定します。
		if( review.getCommunityUser() == null || review.getCommunityUser().getCommunityUserId() == null ){
			CommunityUserDO communityUser = communityUserDao.load(
					review.getCommunityUser().getCommunityUserId(), 
					Path.includeProp("*"));
			if( communityUser == null ){
				throw new IllegalArgumentException("can not post contents. because userdata can not load. communityUserId:" + review.getCommunityUser().getCommunityUserId());
			}
			review.setCommunityUser(communityUser);
		}
	}
	private String settingPurchaseProduct(ReviewDO review){
		PurchaseProductDO purchaseProduct = orderDao.loadPurchaseProductBySku(
				review.getCommunityUser().getCommunityUserId(),
				review.getProduct().getSku(),
				Path.includeProp("*").includePath("purchaseProductId,purchaseDate,share," +
						"purchaseHistoryType,purchaseDateFix," +
						"product.sku").depth(1), true);
		String purchaseProductId = null;
		if (purchaseProduct == null || purchaseProduct.isDeleted()) {
			if (review.getPurchaseDate() == null) {
				throw new IllegalArgumentException("PurchaseDate in review required.");
			}
			purchaseProduct = new PurchaseProductDO();
			purchaseProduct.setCommunityUser(review.getCommunityUser());
			purchaseProduct.setPurchaseDate(review.getPurchaseDate());
			purchaseProduct.setUserInputPurchaseDate(purchaseProduct.getPurchaseDate());
			purchaseProduct.setProduct(review.getProduct());
			purchaseProduct.setPurchaseHistoryType(PurchaseHistoryType.OTHER);
			purchaseProduct.setAdult(review.getProduct().isAdult());
			purchaseProduct.setPublicSetting(true);
			orderDao.createPurchaseProduct(purchaseProduct, false);
			purchaseProductId = purchaseProduct.getPurchaseProductId();
		}
		if (purchaseProduct.getPurchaseDate() == null) {
			throw new IllegalStateException(
					"PurchaseDate is null. communityUserId = "
					+ review.getCommunityUser().getCommunityUserId()
					+ " sku = " + review.getProduct().getSku());
		}
		review.setPurchaseProduct(purchaseProduct);
		review.setPurchaseDate(purchaseProduct.getPurchaseDate());
		review.setPurchaseHistoryType(purchaseProduct.getPurchaseHistoryType());
		if (!purchaseProduct.isPurchaseDateFix()) {
			orderDao.fixPurchaseDate(purchaseProduct.getPurchaseProductId());
		}
		
		return purchaseProductId;
	}
	
	private void settingPointGrantByAddReview(ReviewDO review){
		if (PurchaseHistoryType.OTHER.equals(review.getPurchaseHistoryType()) 
				|| !review.getProduct().isGrantPointWithinTerm(review.getPurchaseDate())) {
			review.setEffective(false);
		} else {
			review.setEffective(canGrantPointReview(
					review.getCommunityUser().getCommunityUserId(),
					review.getProduct(),
					review.getPurchaseProduct()));
		}
		
		settingPointGrant(review);
	}
	
	private void settingPointGrantByModifyReview(ReviewDO review){
		settingPointGrant(review);
	}
	
	private void settingPointGrant(ReviewDO review){
		if(!review.isEffective()){
			return;
		}
		
		List<PointGrantRequestDetail> details = getPointGrantRequestDetails(review);
		long totalPoint = 0;
		List<String> specialConditionCodes = new ArrayList<String>();
		for (PointGrantRequestDetail detail : details) {
			totalPoint += detail.getPoint();
			if (detail.getSpecialCondCode() != null) {
				specialConditionCodes.add(detail.getSpecialCondCode());
			}
		}
		if (totalPoint > 0) {
			review.setPointGrantRequestDetails(details);
			review.setPointGrantStatus(PointGrantStatus.WAIT);
			String pointGrantRequestId = simplePmsDao.entryPointGrant(
					review.getCommunityUser().getCommunityId(),
					PointExchangeType.REVIEW,
					//31日目からポイント付与可となります。
					DateUtils.addDays(DateUtils.truncate(review.getPurchaseDate(), Calendar.DATE),
							serviceConfig.pointGrantExecStartInterval),
					totalPoint,
					specialConditionCodes.toArray(new String[specialConditionCodes.size()]));
			review.setPointGrantRequestId(pointGrantRequestId);
		} else {
			review.setEffective(false);
			LOG.info("ReviewPoint is zero. change effective off. reviewId=" + review.getReviewId());
		}
	}
	private void cancelPointGrant(ReviewDO review){
		if( !review.isEffective() ||
				review.getPointGrantRequestId() == null ||
				CancelPointGrantType.CONTENTS_STOP.equals(review.getCancelPointGrantType())){
			return;
		}
		// ポイント削除処理
		simplePmsDao.cancelPointGrant(review.getPointGrantRequestId(), CancelPointGrantType.MODIFY_REVIEW);
		// 初期化
		review.setPointGrantRequestId(null);
		review.setPointGrantRequestDetails(null);
		review.setPointGrantStatus(null);
	}
}
