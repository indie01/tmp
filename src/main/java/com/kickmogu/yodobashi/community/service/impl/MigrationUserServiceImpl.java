/**
 *
 */
package com.kickmogu.yodobashi.community.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.common.exception.DataNotFoundException;
import com.kickmogu.yodobashi.community.common.utils.ProfileUtil;
import com.kickmogu.yodobashi.community.resource.dao.AnnounceDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.MigrationDao;
import com.kickmogu.yodobashi.community.resource.dao.NormalizeCharDao;
import com.kickmogu.yodobashi.community.resource.dao.OrderDao;
import com.kickmogu.yodobashi.community.resource.dao.OuterCustomerDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductDao;
import com.kickmogu.yodobashi.community.resource.dao.SimplePmsDao;
import com.kickmogu.yodobashi.community.resource.domain.AbstractReviewDO.PointGrantRequestDetail;
import com.kickmogu.yodobashi.community.resource.domain.AccountSharingDO;
import com.kickmogu.yodobashi.community.resource.domain.AnnounceDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.MigrationCommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.OldImageDO;
import com.kickmogu.yodobashi.community.resource.domain.OldReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.OldReviewHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.OldSpamReportDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.SpamReportDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.AlsoBuyProduct;
import com.kickmogu.yodobashi.community.resource.domain.constants.AnnounceType;
import com.kickmogu.yodobashi.community.resource.domain.constants.AsyncMessageType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointExchangeType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointGrantStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PurchaseHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportGroupType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportTargetType;
import com.kickmogu.yodobashi.community.service.AggregateOrderService;
import com.kickmogu.yodobashi.community.service.IndexService;
import com.kickmogu.yodobashi.community.service.MigrationUserService;
import com.kickmogu.yodobashi.community.service.annotation.SendMessage;
import com.kickmogu.yodobashi.community.service.annotation.SendMessage.Timing;

/**
 * 移行ユーザーサービスの実装です。
 * @author kamiike
 */
@Service
public class MigrationUserServiceImpl implements MigrationUserService {

	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger LOG = LoggerFactory.getLogger(MigrationUserServiceImpl.class);

	@Autowired
	private ApplicationContext applicationContext;
	
	/**
	 * 外部顧客情報 DAO です。
	 */
	@Autowired @Qualifier("xi")
	private OuterCustomerDao outerCustomerDao;

	/**
	 * ポイント管理システム DAO です。
	 */
	@Autowired @Qualifier("pms")
	private SimplePmsDao simplePmsDao;

	/**
	 * 標準化文字 DAO です。
	 */
	@Autowired
	private NormalizeCharDao normalizeCharDao;

	/**
	 * 移行用 DAO です。
	 */
	@Autowired
	private MigrationDao migrationDao;

	/**
	 * コミュニティユーザー DAO です。
	 */
	@Autowired
	private CommunityUserDao communityUserDao;

	/**
	 * 注文 DAO です。
	 */
	@Autowired
	private OrderDao orderDao;

	/**
	 * 商品 DAO です。
	 */
	@Autowired
	private ProductDao productDao;

	/**
	 * インデックスサービスです。
	 */
	@Autowired
	private IndexService indexService;

	@Autowired
	private TimestampHolder timestampHolder;
	
	@Autowired
	private AnnounceDao announceDao;
	
	
	
	/**
	 * 注文集約サービスです。
	 */
	@Autowired
	private AggregateOrderService aggregateOrderService;

	/**
	 * 指定したコミュニティIDに紐付く移行ユーザー情報を返します。
	 * @param communityId コミュニティID
	 * @return 移行ユーザー情報
	 */
	@Override
	@ArroundHBase
	public MigrationCommunityUserDO getMigrationCommunityUserByCommunityId(
			String communityId) {
		for (AccountSharingDO sharing : outerCustomerDao.findAccountSharingByOuterCustomerId(
				communityId)) {
			if (!sharing.isEc()) {
				MigrationCommunityUserDO migrationUser = migrationDao.loadMigrationCommunityUserByOuterCustomerId(sharing.getOuterCustomerId());
				if (migrationUser != null && !migrationUser.isDeleteFlag()) {
					return migrationUser;
				}
			}
		}
		return null;
	}

	/**
	 * コミュニティユーザーのニックネームを更新し、マージ済みとし、
	 * 指定したIC会員の移行ユーザー情報を移行済みに更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param communityName コミュニティ名
	 * @param icOuterCustomerId ICの外部顧客ID
	 */
	@Override
	@ArroundHBase
	public void mergeCommunityName(
			String communityUserId,
			String communityName,
			String icOuterCustomerId,
			boolean agreement) {
		CommunityUserDO dbInstance = communityUserDao.loadWithLock(
				communityUserId,
				Path.DEFAULT,
				true);
		if (dbInstance == null || !dbInstance.isActive()) {
			throw new DataNotFoundException(
					"CommunityUser is not found. communityUserId = "
					+ communityUserId);
		}

		String normalizeCommunityName = dbInstance.getNormalizeCommunityName();
		if (!dbInstance.getCommunityName().equals(communityName)) {
			normalizeCommunityName = normalizeCharDao.normalizeString(communityName);
		}

		dbInstance.setNormalizeCommunityName(normalizeCommunityName);
		dbInstance.setCommunityName(communityName);
		dbInstance.setCommunityNameMergeRequired(false);
		dbInstance.setSecureAccess(ProfileUtil.isCommunityUserProfileSecureAccess(applicationContext));
		communityUserDao.updateSecureAccess(dbInstance);
		communityUserDao.updateCommunityName(dbInstance, true);
		indexService.updateIndexForUpdateCommunityUser(communityUserId, new String[0]);
		migrationDao.deleteMigrationCommunityUser(icOuterCustomerId);
		
		
		if(agreement) {
			// 参加規約のアナウンス情報を登録します。
			AnnounceDO announce = new AnnounceDO();
			announce.setCommunityUserId(communityUserId);
			announce.setType(AnnounceType.PARTICIPATING_AGREEMENT);
			announce.setDeleteFlag(true);
			announce.setDeleteDate(timestampHolder.getTimestamp());
			announceDao.create(announce);
		}
	}

	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.DELAYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void callCheckAndMigrateReview(String communityUserId) {
		checkAndMigrateReview(communityUserId);
	}
	
	/**
	 * 共有化しているIC会員情報で移行待ちのままのレビュー情報が無いかチェックし、
	 * あれば移行します。<br />
	 * また共有化情報の通知処理が実装されるまで、暫定対応としてキャッシュ
	 * された共有化情報との差分を確認し、更新があった場合、注文情報を再度サマリします。
	 * @param communityUserId コミュニティユーザーID
	 */
	@Override
	@ArroundSolr
	@ArroundHBase
	public void checkAndMigrateReview(String communityUserId) {
		CommunityUserDO communityUser = communityUserDao.load(communityUserId, Path.DEFAULT);
		if (communityUser == null) {
			return;
		}
		List<AccountSharingDO> accountSharings = outerCustomerDao.findAccountSharingByOuterCustomerId(
				communityUser.getCommunityId());
		List<String> cacheOuterCustomerIds = new ArrayList<String>();
		List<String> outerCustomerIds = new ArrayList<String>();
		if (communityUser.getAccountSharingCaches() != null) {
			for (AccountSharingDO accountSharing : communityUser.getAccountSharingCaches()) {
				cacheOuterCustomerIds.add(accountSharing.getOuterCustomerId());
			}
		}
		if (accountSharings != null) {
			for (AccountSharingDO accountSharing : accountSharings) {
				outerCustomerIds.add(accountSharing.getOuterCustomerId());
			}
		}
		Collections.sort(cacheOuterCustomerIds);
		Collections.sort(outerCustomerIds);
		if (!ListUtils.isEqualList(cacheOuterCustomerIds, outerCustomerIds)) {
			communityUser.setAccountSharingCaches(accountSharings);
			communityUserDao.saveCommunityUser(communityUser);
			aggregateOrderService.aggregateOrder(outerCustomerIds);
		}
		//ニックネームマージ要求の移行ユーザーはレビューは移行済みのため、無視します。
		if (communityUser.isCommunityNameMergeRequired()) {
			return;
		}
		for (AccountSharingDO sharing : accountSharings) {
			if (!sharing.isEc()) {
				MigrationCommunityUserDO migrationUser = migrationDao.loadMigrationCommunityUserByOuterCustomerId(sharing.getOuterCustomerId());
				if (migrationUser != null && !migrationUser.isDeleteFlag()) {
					migrateReview(communityUser, migrationUser.getOuterCustomerId());
					migrationDao.deleteMigrationCommunityUser(migrationUser.getOuterCustomerId());
				}
			}
		}
	}

	/**
	 * レビュー情報を移行します。
	 * @param communityUser コミュニティユーザー
	 * @param outerCustomerId 外部顧客ID
	 */
	private void migrateReview(CommunityUserDO communityUser, String outerCustomerId) {
		if (LOG.isInfoEnabled()) {
			LOG.info("start migrateReview. communityId=" + communityUser.getCommunityId()
					+ ", communityUserId=" + communityUser.getCommunityUserId()
					+ ", outerCustomerId=" + outerCustomerId);
		}
		List<OldReviewDO> oldReviews = migrationDao.findOldReviewByOuterCustomerId(outerCustomerId);
		List<String> reviewIds = new ArrayList<String>();
		Map<String, ReviewDO> oldReviewMap = new HashMap<String, ReviewDO>();
		Map<String, String> productNameCache = new HashMap<String, String>();
		Map<String, ImageHeaderDO> imageHeaderMap = new HashMap<String, ImageHeaderDO>();
		int migrateReviewCount = 0;
		for (OldReviewDO oldReview : oldReviews) {
			ReviewDO review = new ReviewDO();
			review.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
			review.setCommunityUser(communityUser);
			review.setProduct(new ProductDO());
			review.getProduct().setSku(oldReview.getSku());
			review.setOldReviewId(oldReview.getOldReviewId());
			PurchaseProductDO purchaseProduct = orderDao.loadPurchaseProductBySku(
					review.getCommunityUser().getCommunityUserId(),
					oldReview.getSku(), Path.DEFAULT, false);
			if (purchaseProduct == null || purchaseProduct.isDeleted()) {
				LOG.warn("migrateReview: PurchaseProduct is not found. outerCustomerId="
						+ outerCustomerId
						+ ", oldReviewId=" + oldReview.getOldReviewId()
						+ ", sku=" + oldReview.getSku());
				purchaseProduct = new PurchaseProductDO();
				// 移行レビューに関する購入商品は「購入日が固定化フラグ=true」で設定
				purchaseProduct.setPurchaseDateFix(true);
				purchaseProduct.setCommunityUser(communityUser);
				purchaseProduct.setPurchaseHistoryType(PurchaseHistoryType.OTHER);
				purchaseProduct.setProduct(productDao.loadProduct(oldReview.getSku()));
				purchaseProduct.setAdult(purchaseProduct.getProduct().isAdult());
				purchaseProduct.setPublicSetting(true);
				purchaseProduct.setPurchaseDate(oldReview.getPostDate());
				orderDao.createPurchaseProduct(purchaseProduct, true);
			} else if (!purchaseProduct.isPurchaseDateFix()) {
				// 移行レビューに関する購入商品は「購入日が固定化フラグ=true」で設定
//				List<PurchaseProductDO> purchaseProductList = new ArrayList<PurchaseProductDO>();
//				purchaseProduct.setPurchaseDateFix(true);
//				purchaseProductList.add(purchaseProduct);
//				migrationDao.updatePurchaseDateFix(purchaseProductList);
				// 移行時ではないので、modifyDateTimeも更新する
				orderDao.fixPurchaseDate(purchaseProduct.getPurchaseProductId());
			}

			review.setPurchaseDate(purchaseProduct.getPurchaseDate());
			review.setPurchaseHistoryType(PurchaseHistoryType.YODOBASHI);
			review.setAdult(purchaseProduct.isAdult());
			review.setAlsoBuyProduct(AlsoBuyProduct.NONE);
			review.setNoLostProductFlag(true);
			review.setNoUsedProductFlag(true);
			review.setProductSatisfaction(oldReview.getProductSatisfaction());
			review.setPostDate(oldReview.getPostDate());
			review.setPointBaseDate(oldReview.getPostDate());
			review.calcElapsedDays();
			review.setStatus(ContentsStatus.SUBMITTED);
			review.setMemo(oldReview.getMemo());
			review.setSaveDate(oldReview.getSaveDate());
			review.setRegisterDateTime(oldReview.getRegisterDateTime());
			review.setModifyDateTime(oldReview.getModifyDateTime());

			if (oldReview.getPointGrantRequestDetails() != null) {
				long totalPoint = 0;
				for (PointGrantRequestDetail detail : oldReview.getPointGrantRequestDetails()) {
					totalPoint += detail.getPoint();
				}
				if (oldReview.getPointGrantRequestDetails().size() > 0) {
					review.setPointGrantRequestDetails(oldReview.getPointGrantRequestDetails());
					review.setPointGrantStatus(PointGrantStatus.ADD);
					String pointGrantRequestId = simplePmsDao.migratePointGrant(
							communityUser.getCommunityId(),
							PointExchangeType.REVIEW,
							oldReview.getPointGrantApprovalDate(),
							oldReview.getPointGrantDate(),
							totalPoint);
					review.setPointGrantRequestId(pointGrantRequestId);
					review.setGrantPoint(totalPoint);
				}
			}
			// ポイント付与対象でなくても、移行レビューは全て有効フラグをtrueでセットする
			review.setEffective(true);

			Map<String, OldImageDO> imageMap = migrationDao.findOldImage(oldReview.getImagePaths());

			StringBuilder buffer = new StringBuilder();
			List<ImageHeaderDO> imageHeaders = new ArrayList<ImageHeaderDO>();
			if (oldReview.getReviewBodyParts() != null) {
				buffer.append(oldReview.getReviewBodyParts().replace("\n", "<br />"));
			}
			if (oldReview.getImagePaths() != null && oldReview.getImagePaths().size() > 0) {
				for (int i = 0; i < oldReview.getImagePaths().size(); i++) {
					OldImageDO oldImage = imageMap.get(oldReview.getImagePaths().get(i));

					ImageDO imageDO = new ImageDO();
					imageDO.setCommunityUserId(review.getCommunityUser().getCommunityUserId());
					imageDO.setData(oldImage.getData());
					imageDO.setMimeType(oldImage.getMimeType());
					imageDO.setRegisterDateTime(oldImage.getRegisterDateTime());
					imageDO.setModifyDateTime(oldImage.getModifyDateTime());
					migrationDao.createTemporaryImage(imageDO);
					ImageHeaderDO imageHeader = new ImageHeaderDO();
					imageHeader.setImageId(imageDO.getImageId());
					if (i == 0) {
						imageHeader.setListViewFlag(true);
					}
					imageHeader.setOwnerCommunityUser(
							review.getCommunityUser());
					imageHeader.setPostContentType(PostContentType.REVIEW);
					imageHeader.setPostDate(oldImage.getRegisterDateTime());
					imageHeader.setSku(review.getProduct().getSku());
					imageHeader.setStatus(ContentsStatus.SUBMITTED);
					imageHeader.setOldFileName(oldImage.getOldFileName());
					migrationDao.saveAndUploadImage(imageDO, imageHeader, true);
					imageHeaders.add(imageHeader);
					imageHeaderMap.put(oldImage.getOldFileName(), imageHeader);
					buffer.append("<p><img src=\"");
					buffer.append(imageHeader.getImageUrl());
					buffer.append("\"/></p>");
				}
			}
			review.setReviewBody(buffer.toString());
			if (StringUtils.isEmpty(review.getReviewBody())) {
				String productName = null;
				if (productNameCache.containsKey(review.getProduct().getSku())) {
					productName = productNameCache.get(review.getProduct().getSku());
				} else {
					ProductDO product = productDao.loadProduct(review.getProduct().getSku());
					productName = product.getProductName();
					productNameCache.put(review.getProduct().getSku(), productName);
				}
				review.setReviewBody(review.getCommunityUser(
						).getCommunityName() + "の" + productName + "のレビュー");
			}

			migrationDao.createReviewWithIndex(review);
			migrateReviewCount++;
			oldReviewMap.put(review.getOldReviewId(), review);
			reviewIds.add(review.getOldReviewId());
			for (ImageHeaderDO imageHeader : imageHeaders) {
				imageHeader.setReview(review);
				if (imageHeader.getTempThumbnailImage() != null) {
					imageHeader.getTempThumbnailImage().setReview(review);
				}
			}
			migrationDao.updateImageHeaderRelationWithIndex(imageHeaders);
			// 移行レビューにもアクション履歴を生成する
			migrationDao.createReviewActionHistory(review);
		}

		int migrateReviewHistoryCount = 0;
		List<OldReviewHistoryDO> oldHistorys = migrationDao.findOldReviewHistoryByOuterCustomerId(outerCustomerId);
		for (OldReviewHistoryDO oldHistory : oldHistorys) {
			ReviewDO review = oldReviewMap.get(
					oldHistory.getOldReviewId());

			ReviewHistoryDO history = new ReviewHistoryDO();
			history.setReviewId(review.getReviewId());
			history.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
			history.setCommunityUser(communityUser);

			history.setOldReviewId(review.getOldReviewId());
			history.setPurchaseDate(review.getPurchaseDate());
			history.setElapsedDays(review.getElapsedDays());
			history.setAdult(review.isAdult());
			history.setAlsoBuyProduct(review.getAlsoBuyProduct());
			history.setNoLostProductFlag(review.isNoLostProductFlag());
			history.setNoUsedProductFlag(review.isNoUsedProductFlag());

			history.setProductSatisfaction(oldHistory.getProductSatisfaction());
			history.setPostDate(oldHistory.getPostDate());
			history.setPointBaseDate(oldHistory.getPostDate());

			history.setStatus(ContentsStatus.SUBMITTED);
			history.setMemo(oldHistory.getMemo());
			history.setSaveDate(oldHistory.getPostDate());
			history.setRegisterDateTime(oldHistory.getRegisterDateTime());
			history.setModifyDateTime(oldHistory.getModifyDateTime());
			history.setProduct(review.getProduct());

			Map<String, OldImageDO> imageMap = migrationDao.findOldImage(oldHistory.getImagePaths());

			StringBuilder buffer = new StringBuilder();
			List<ImageHeaderDO> imageHeaders = new ArrayList<ImageHeaderDO>();
			if (oldHistory.getReviewBodyParts() != null) {
				buffer.append(oldHistory.getReviewBodyParts().replace("\n", "<br />"));
			}
			if (oldHistory.getImagePaths() != null && oldHistory.getImagePaths().size() > 0) {
				for (int i = 0; i < oldHistory.getImagePaths().size(); i++) {
					OldImageDO oldImage = imageMap.get(oldHistory.getImagePaths().get(i));
					if (imageHeaderMap.containsKey(oldImage.getOldFileName())) {
						imageHeaders.add(imageHeaderMap.get(oldImage.getOldFileName()));
						continue;
					}
					byte[] data = oldImage.getData();
					ImageDO imageDO = new ImageDO();
					imageDO.setCommunityUserId(history.getCommunityUser().getCommunityUserId());
					imageDO.setData(data);
					imageDO.setMimeType(oldImage.getMimeType());
					imageDO.setRegisterDateTime(oldImage.getRegisterDateTime());
					imageDO.setModifyDateTime(oldImage.getModifyDateTime());
					migrationDao.createTemporaryImage(imageDO);

					ImageHeaderDO imageHeader = new ImageHeaderDO();
					imageHeader.setImageId(imageDO.getImageId());
					imageHeader.setOwnerCommunityUser(
							history.getCommunityUser());
					imageHeader.setPostContentType(PostContentType.REVIEW);
					imageHeader.setPostDate(oldImage.getRegisterDateTime());
					imageHeader.setSku(history.getProduct().getSku());
					imageHeader.setStatus(ContentsStatus.SUBMITTED);
					imageHeader.setReview(review);
					imageHeader.setOldFileName(oldImage.getOldFileName());
					migrationDao.saveAndUploadImage(imageDO, imageHeader, true);
					imageHeaders.add(imageHeader);
					buffer.append("<p><img src=\"");
					buffer.append(imageHeader.getImageUrl());
					buffer.append("\"/></p>");
				}
			}
			history.setReviewBody(buffer.toString());
			if (StringUtils.isEmpty(history.getReviewBody())) {
				String productName = null;
				if (productNameCache.containsKey(review.getProduct().getSku())) {
					productName = productNameCache.get(review.getProduct().getSku());
				} else {
					ProductDO product = productDao.loadProduct(review.getProduct().getSku());
					productName = product.getProductName();
					productNameCache.put(review.getProduct().getSku(), productName);
				}

				history.setReviewBody(history.getCommunityUser(
						).getCommunityName() + "の" + productName + "のレビュー");
			}

			migrationDao.createReviewWithIndex(history);
			migrateReviewHistoryCount++;
		}

		int migrateSpamRportCount = 0;
		List<OldSpamReportDO> oldSpamReports = migrationDao.findOldSpamReportByOuterCustomerIdOrOldReviewId(
				outerCustomerId, reviewIds);
		for (OldSpamReportDO oldSpamReport : oldSpamReports) {
			ReviewDO review = oldReviewMap.get(oldSpamReport.getOldReviewId());
			if (review == null) {
				review = migrationDao.loadReviewByOldReviewId(oldSpamReport.getOldReviewId());
				if (review == null) {
					continue;
				}
			}
			SpamReportDO spamReport = new SpamReportDO();
			spamReport.setOldReviewId(spamReport.getOldReviewId());
			spamReport.setCommunityUser(communityUser);
			spamReport.setReview(review);
			spamReport.setReportDate(oldSpamReport.getReportDate());
			spamReport.setRelationReviewOwnerId(
					review.getCommunityUser().getCommunityUserId());
			spamReport.setStatus(oldSpamReport.getStatus());
			spamReport.setResolvedDate(oldSpamReport.getResolvedDate());
			spamReport.setGroupType(SpamReportGroupType.REVIEW);
			spamReport.setTargetType(SpamReportTargetType.REVIEW);
			spamReport.setRegisterDateTime(oldSpamReport.getRegisterDateTime());
			spamReport.setModifyDateTime(oldSpamReport.getModifyDateTime());
			spamReport.setSpamReportBody(oldSpamReport.getSpamReportBody());
			migrationDao.createSpamReportWithIndex(spamReport);
			migrationDao.markOldSpamReport(oldSpamReport.getSpamReportId());
			migrateSpamRportCount++;
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("end migrateReview. communityId=" + communityUser.getCommunityId()
					+ ", communityUserId=" + communityUser.getCommunityUserId()
					+ ", outerCustomerId=" + outerCustomerId
					+ ", migrateReviewCount=" + migrateReviewCount
					+ ", migrateReviewHistoryCount=" + migrateReviewHistoryCount
					+ ", migrateSpamRportCount=" + migrateSpamRportCount);
		}
	}
}
