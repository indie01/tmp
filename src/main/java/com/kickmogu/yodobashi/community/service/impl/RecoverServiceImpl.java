package com.kickmogu.yodobashi.community.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.common.utils.DateUtil;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.dao.ApplicationLockDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.OrderDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductDao;
import com.kickmogu.yodobashi.community.resource.dao.ReviewDao;
import com.kickmogu.yodobashi.community.resource.dao.SimplePmsDao;
import com.kickmogu.yodobashi.community.resource.dao.util.ProductUtil;
import com.kickmogu.yodobashi.community.resource.domain.AbstractReviewDO.PointGrantRequestDetail;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewPoint;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewPointDetail;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewPointSpecial;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewPointSpecialDetail;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.RecoverResultDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewPointSpecialConditionValidateDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.AlsoBuyProduct;
import com.kickmogu.yodobashi.community.resource.domain.constants.FillType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointExchangeType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointGrantStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointIncentiveType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ProductSatisfaction;
import com.kickmogu.yodobashi.community.resource.domain.constants.PurchaseHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.RecoverResultStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewPointSpecialConditionValidateType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewType;
import com.kickmogu.yodobashi.community.service.RecoverService;
import com.kickmogu.yodobashi.community.service.config.ServiceConfig;

@Service
public class RecoverServiceImpl implements RecoverService {
	
	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger LOG = LoggerFactory.getLogger(RecoverServiceImpl.class);
	
	/**
	 * HBaseアクセサです。
	 */
	@Autowired  @Qualifier("default")
	protected HBaseOperations hBaseOperations;
	/**
	 * Solrアクセサです。
	 */
	@Autowired @Qualifier("default")
	protected SolrOperations solrOperations;
	@Autowired
	private CommunityUserDao communityUserDao;
	@Autowired
	private OrderDao orderDao;
	@Autowired
	private ReviewDao reviewDao;
	/**
	 * 商品 DAO です。
	 */
	@Autowired @Qualifier("catalog")
	private ProductDao productDao;
	@Autowired
	private ApplicationLockDao applicationLockDao;
	/**
	 * ポイント管理 DAO です。
	 */
	@Autowired @Qualifier("pms")
	private SimplePmsDao simplePmsDao;
	/**
	 * タイムスタンプホルダーです。
	 */
	@Autowired
	private TimestampHolder timestampHolder;
	/**
	 * サービスコンフィグです。
	 */
	@Autowired
	protected ServiceConfig serviceConfig;
	@Autowired
	protected ResourceConfig resourceConfig;
	
	@Override
	@ArroundHBase
	@ArroundSolr
	public RecoverResultDO recoverReviewPoint(String reviewId, boolean execMode) {
		if( StringUtils.isBlank(reviewId))
			return new RecoverResultDO(RecoverResultStatus.FAILURE, "reviewId is none", null);
		
		LOG.info("start recover revireId=" + reviewId);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		try{
			// レビューIDからレビュー情報を取得する。
			ReviewDO reviewDO = reviewDao.loadReview(reviewId);
			if( reviewDO == null || ProductUtil.invalid(reviewDO)){
				stopWatch.stop();
				LOG.info("end  recover suspend revireId=" + reviewId + " (" + stopWatch.getTime() + "ms)");
				return new RecoverResultDO(RecoverResultStatus.SUSPEND, "review is NotFound", reviewId);
			}
			// レビューポイントが付いている場合は、処理しない
			if(reviewDO.isEffective()){
				stopWatch.stop();
				LOG.info("end  recover suspend revireId=" + reviewId + " (" + stopWatch.getTime() + "ms)");
				return new RecoverResultDO(RecoverResultStatus.SUSPEND, "review is already effective", reviewId);
			}
			// レビューのユーザー情報確認
			if( reviewDO.getCommunityUser() == null || reviewDO.getCommunityUser().getCommunityUserId() == null){
				stopWatch.stop();
				LOG.info("end  recover suspend revireId=" + reviewId + " (" + stopWatch.getTime() + "ms)");
				return new RecoverResultDO(RecoverResultStatus.SUSPEND, "communityUser is none", reviewId);
			}
			
			// ユーザー情報を取得する。
			CommunityUserDO communityUserDO = communityUserDao.load(
					reviewDO.getCommunityUser().getCommunityUserId(), 
					Path.includeProp("*"));
			if( communityUserDO == null || !communityUserDO.isActive()){
				stopWatch.stop();
				LOG.info("end  recover suspend revireId=" + reviewId + "communityUserId=" + reviewDO.getCommunityUser().getCommunityUserId() + " (" + stopWatch.getTime() + "ms)");
				return new RecoverResultDO(RecoverResultStatus.SUSPEND, "communityUser is not active", reviewId);
			}
			// ユーザーデータを設定する。
			reviewDO.setCommunityUser(communityUserDO);
			// レビューの商品情報を取得する。
			ProductDO productDO = productDao.loadProduct(
					reviewDO.getProduct().getSku(),
					FillType.SMALL, 
					null,
					null,
					null,
					false,
					null);
			if( productDO == null ){
				stopWatch.stop();
				LOG.info("end  recover suspend revireId=" + reviewId + "sku=" + reviewDO.getProduct().getSku() + " (" + stopWatch.getTime() + "ms)");
				return new RecoverResultDO(RecoverResultStatus.SUSPEND, "Product is NotFound", reviewId);
			}
			// 商品データを設定する。
			reviewDO.setProduct(productDO);
			reviewDO.setAdult(reviewDO.getProduct().isAdult());
			//商品情報をロックします。
			applicationLockDao.lockForSaveReview(
					reviewDO.getProduct().getSku(),
					reviewDO.getCommunityUser().getCommunityUserId());
			// 購入商品情報を取得する。
			PurchaseProductDO purchaseProduct = orderDao.loadPurchaseProductBySku(
					reviewDO.getCommunityUser().getCommunityUserId(),
					reviewDO.getProduct().getSku(),
					Path.includeProp("*").includePath(
							"purchaseProductId,purchaseDate,share," +
							"purchaseHistoryType,purchaseDateFix," +
							"product.sku").depth(1), true);
			if (purchaseProduct == null || purchaseProduct.getPurchaseDate() == null) {
				stopWatch.stop();
				LOG.info("end  recover suspend revireId=" + reviewId + 
						"sku=" + reviewDO.getProduct().getSku() + 
						"communityUserId=" + reviewDO.getCommunityUser().getCommunityUserId() +
						" (" + stopWatch.getTime() + "ms)");
				return new RecoverResultDO(RecoverResultStatus.SUSPEND, "purchaseProduct is NotFound", reviewId);
			}
			reviewDO.setPurchaseProduct(purchaseProduct);
			reviewDO.setPurchaseDate(purchaseProduct.getPurchaseDate());
			reviewDO.setPurchaseHistoryType(purchaseProduct.getPurchaseHistoryType());
			if (!purchaseProduct.isPurchaseDateFix()) {
				orderDao.fixPurchaseDate(purchaseProduct.getPurchaseProductId());
			}
			
			// ポイント情報登録処理
			if( !settingPointGrantByRecover(reviewDO,execMode)){
				stopWatch.stop();
				LOG.info("end  recover stop  revireId=" + reviewId + "(" + stopWatch.getTime() + "ms)");
				return new RecoverResultDO(RecoverResultStatus.SUSPEND, "review is not point target", reviewId);
			}
			// データ更新処理
			if( execMode ){
				reviewDO.setModifyDateTime(timestampHolder.getTimestamp());
				Condition updateCondition = Path.includeProp("purchaseHistoryType,effective,pointGrantRequestId,pointGrantRequestDetails,pointGrantStatus,modifyDateTime");
				// HBaseに差分を更新する。
				hBaseOperations.save(reviewDO, updateCondition);
				// Solrのインでクスを更新する。
				// レビューデータを再取得する。
				reviewDO = reviewDao.loadReview(reviewId);
				solrOperations.save(reviewDO, Path.includeProp("*"));
			}
			long totalPoint = 0;
			for (PointGrantRequestDetail detail : reviewDO.getPointGrantRequestDetails()) {
				totalPoint += detail.getPoint();
			}
			
			stopWatch.stop();
			LOG.info("end  recover success revireId=" + reviewId + "(" + stopWatch.getTime() + "ms)");
			return new RecoverResultDO(RecoverResultStatus.SUCCESS, "success", reviewId, totalPoint);
		}catch(Exception e){
			stopWatch.stop();
			LOG.error("end  recover failure revireId=" + reviewId + "(" + stopWatch.getTime() + "ms)", e);
			return new RecoverResultDO(RecoverResultStatus.FAILURE, "failure", reviewId);
		}
	}

	@Override
	@ArroundHBase
	@ArroundSolr
	public List<RecoverResultDO> recoverReviewPoint(List<String> reviewIds, boolean execMode) {
		List<RecoverResultDO> results = Lists.newArrayList();
		if( reviewIds == null || reviewIds.isEmpty() )
			return results;
		
		RecoverResultDO result = null;
		for( String reviewId : reviewIds){
			result = recoverReviewPoint(reviewId,execMode);
			results.add(result);
		}
		return results;
	}
	
	private boolean settingPointGrantByRecover(ReviewDO review, boolean execMode){
		if (PurchaseHistoryType.OTHER.equals(review.getPurchaseHistoryType()) 
				|| !review.getProduct().isGrantPointWithinTerm(review.getPurchaseDate())) {
			review.setEffective(false);
		} else {
			review.setEffective(canGrantPointReview(
					review.getCommunityUser().getCommunityUserId(),
					review.getProduct(),
					review.getPurchaseProduct(),
					review.getPointBaseDate()));
		}
		
		return settingPointGrant(review,execMode);
	}
	
	private boolean canGrantPointReview(
			String communityUserId,
			ProductDO product,
			PurchaseProductDO purchaseProduct,
			Date pointBaseDate) {
		
		if (!product.isCanReview()) {
			return false;
		}
		if (PurchaseHistoryType.OTHER.equals(purchaseProduct.getPurchaseHistoryType())) {
			return false;
		}
		// レビュー投稿時の日時で判断する。
		if (!product.isGrantPointWithinTerm(purchaseProduct.getPurchaseDate(), pointBaseDate)) {
			return false;
		}
		// レビュー投稿時の日時で判断する。
		int reviewTerm = product.getGrantPointReviewTerm(purchaseProduct.getPurchaseDate(),pointBaseDate);
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
	
	private boolean settingPointGrant(
			ReviewDO review,
			boolean execMode){
		if(!review.isEffective()){
			return false;
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
			
			if( execMode ){
				String pointGrantRequestId = simplePmsDao.entryPointGrant(
						review.getCommunityUser().getCommunityId(),
						PointExchangeType.REVIEW,
						//31日目からポイント付与可となります。
						DateUtils.addDays(DateUtils.truncate(review.getPurchaseDate(), Calendar.DATE),serviceConfig.pointGrantExecStartInterval),
						totalPoint,
						specialConditionCodes.toArray(new String[specialConditionCodes.size()]));
				review.setPointGrantRequestId(pointGrantRequestId);
			}
			return true;
		} else {
			review.setEffective(false);
			LOG.info("ReviewPoint is zero. change effective off. reviewId=" + review.getReviewId());
			return false;
		}
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
}
