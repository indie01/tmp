/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.cxf.common.util.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.id.IDGenerator;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.web.common.constants.ImageType;
import com.kickmogu.web.common.util.ImageUtils;
import com.kickmogu.yodobashi.community.common.exception.YcComException;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.dao.ImageCacheDao;
import com.kickmogu.yodobashi.community.resource.dao.MigrationDao;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.SolrUtil;
import com.kickmogu.yodobashi.community.resource.domain.AbstractReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityNameDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.MigrationCommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.OldImageDO;
import com.kickmogu.yodobashi.community.resource.domain.OldReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.OldReviewHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.OldSpamReportDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.SpamReportDO;
import com.kickmogu.yodobashi.community.resource.domain.SpoofingNameDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageSyncStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageUploadResult;

/**
 * 移行用 DAO の実装です。
 * @author kamiike
 *
 */
@Service
public class MigrationDaoImpl implements MigrationDao {

	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger LOG = LoggerFactory.getLogger(MigrationDaoImpl.class);

	/**
	 * HBaseアクセサです。
	 */
	@Autowired  @Qualifier("default")
	private HBaseOperations hBaseOperations;

	/**
	 * Solrアクセサです。
	 */
	@Autowired @Qualifier("default")
	private SolrOperations solrOperations;

	/**
	 * タイムスタンプホルダーです。
	 */
	@Autowired
	private TimestampHolder timestampHolder;

	@Autowired @Qualifier("default")
	private IDGenerator<String> idGenerator;

	/**
	 * リソースコンフィグです。
	 */
	@Autowired
	private ResourceConfig resourceConfig;

	/**
	 * 画像キャッシュ DAO です。
	 */
	@Autowired
	private ImageCacheDao imageCacheDao;

	/**
	 * ランダムインスタンスです。
	 */
	private static Random random;

	static {
		try {
			random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		} catch (Exception e) {
			random = new Random();
		}
	}

	/**
	 * 移行用コミュニティユーザーを作成します。
	 * @param migrationCommunityUser 移行用コミュニティユーザー
	 * @param spoofingName なりすまし判定クラス
	 */
	@Override
	public void createMigrationCommunityUser(
			MigrationCommunityUserDO migrationCommunityUser,
			SpoofingNameDO spoofingName) {
		CommunityNameDO communityName = new CommunityNameDO();
		communityName.setCommunityUserId("");
		communityName.setOuterCustomerId(migrationCommunityUser.getOuterCustomerId());
		communityName.setNormalizeCommunityName(
				migrationCommunityUser.getNormalizeCommunityName());
		hBaseOperations.save(communityName);
		hBaseOperations.save(migrationCommunityUser);

		// マルチスレッドでの、成りすましチェックは非対応
		if (org.apache.commons.lang.StringUtils.isNotEmpty(
				spoofingName.getSpoofingPattern())) {
			hBaseOperations.save(spoofingName);
		}
	}

	/**
	 * 指定した外部顧客IDの移行用コミュニティユーザーを返します。
	 * @param outerCustomerId 外部顧客ID
	 * @return 移行用コミュニティユーザー
	 */
	@Override
	public MigrationCommunityUserDO loadMigrationCommunityUserByOuterCustomerId(String outerCustomerId) {
		return hBaseOperations.load(MigrationCommunityUserDO.class, outerCustomerId);
	}

	/**
	 * 指定した外部顧客IDの移行用コミュニティユーザーを削除します。
	 * @param outerCustomerId 外部顧客ID
	 */
	@Override
	public void deleteMigrationCommunityUser(String outerCustomerId) {
		MigrationCommunityUserDO migrationCommunityUser = loadMigrationCommunityUserByOuterCustomerId(outerCustomerId);
		CommunityNameDO communityName = hBaseOperations.load(
				CommunityNameDO.class,
				migrationCommunityUser.getNormalizeCommunityName());
		
		if (StringUtils.isEmpty(communityName.getCommunityUserId())) {
			communityName.setDeleteFlag(true);
			communityName.setDeleteDate(timestampHolder.getTimestamp());
			hBaseOperations.save(communityName,
					Path.includeProp("deleteFlag,deleteDate"));
		}
		migrationCommunityUser.setOuterCustomerId(outerCustomerId);
		migrationCommunityUser.setDeleteFlag(true);
		migrationCommunityUser.setDeleteDate(timestampHolder.getTimestamp());
		hBaseOperations.save(migrationCommunityUser,
				Path.includeProp("deleteFlag,deleteDate"));
	}

	/**
	 * 旧レビュー情報を生成します。
	 * @param oldReview 旧レビュー
	 */
	@Override
	public void createOldReview(OldReviewDO oldReview) {
		hBaseOperations.save(oldReview);
	}

	/**
	 * 旧レビュー履歴情報を生成します。
	 * @param oldHistoryReview 旧履歴レビュー
	 */
	@Override
	public void createOldReviewHistory(OldReviewHistoryDO oldHistoryReview) {
		hBaseOperations.save(oldHistoryReview);
	}

	/**
	 * 旧画像情報を生成します。
	 * @param oldImage 旧画像
	 */
	@Override
	public void createOldImage(OldImageDO oldImage) {
		hBaseOperations.save(oldImage);
	}

	/**
	 * 旧違反報告情報を生成します。
	 * @param oldSpamReport 旧違反報告情報
	 */
	@Override
	public void createOldSpamReport(OldSpamReportDO oldSpamReport) {
		hBaseOperations.save(oldSpamReport);
	}

	/**
	 * 指定した外部顧客IDに紐づく旧レビュー情報を返します。
	 * @param outerCustomerId 外部顧客ID
	 * @return 旧レビューリスト
	 */
	@Override
	public List<OldReviewDO> findOldReviewByOuterCustomerId(String outerCustomerId) {
		return hBaseOperations.scanWithIndex(
				OldReviewDO.class, "outerCustomerId", outerCustomerId);
	}

	/**
	 * 指定した外部顧客IDに紐づく旧レビュー履歴情報を返します。
	 * @param outerCustomerId 外部顧客ID
	 * @return 旧レビュー履歴リスト
	 */
	@Override
	public List<OldReviewHistoryDO> findOldReviewHistoryByOuterCustomerId(String outerCustomerId) {
		return hBaseOperations.scanWithIndex(
				OldReviewHistoryDO.class, "outerCustomerId", outerCustomerId);
	}

	/**
	 * 指定したキーの旧画像情報を返します。
	 * @param ids キーリスト
	 * @return 旧画像情報マップ
	 */
	@Override
	public Map<String, OldImageDO> findOldImage(List<String> ids) {
		return hBaseOperations.find(OldImageDO.class, String.class, ids);
	}

	/**
	 * 指定した外部顧客ID、もしくは旧レビューIDに紐づく旧違反報告情報を返します。
	 * @param outerCustomerId 外部顧客ID
	 * @param oldReviewIds 旧レビューIDリスト
	 * @return 旧違反報告情報リスト
	 */
	@Override
	public List<OldSpamReportDO> findOldSpamReportByOuterCustomerIdOrOldReviewId(
			String outerCustomerId,
			List<String> oldReviewIds) {
		Map<String, OldSpamReportDO> result = new HashMap<String, OldSpamReportDO>();
		for (OldSpamReportDO report : hBaseOperations.scanWithIndex(
				OldSpamReportDO.class, "outerCustomerId", outerCustomerId)) {
			result.put(report.getSpamReportId(), report);
		}
		for (String oldReviewId : oldReviewIds) {
			for (OldSpamReportDO report : hBaseOperations.scanWithIndex(
					OldSpamReportDO.class, "oldReviewId", oldReviewId)) {
				result.put(report.getSpamReportId(), report);
			}
		}

		List<OldSpamReportDO> reports = new ArrayList<OldSpamReportDO>();
		for (OldSpamReportDO report : result.values()) {
			if (!report.isMoved()) {
				reports.add(report);
			}
		}
		return reports;
	}

	/**
	 * 指定した旧違反報告を移行済みに更新します。
	 * @param oldSpamReportId 旧違反報告ID
	 */
	@Override
	public void markOldSpamReport(String oldSpamReportId) {
		OldSpamReportDO report = new OldSpamReportDO();
		report.setSpamReportId(oldSpamReportId);
		report.setMoved(true);
		hBaseOperations.save(report, Path.includeProp("moved"));
	}

	/**
	 * 画像を一時保存します。
	 * @param image 画像
	 */
	@Override
	public void createTemporaryImage(ImageDO image) {
		image.setImageId(null);
		image.setImageUrl(null);
		image.setTemporaryFlag(true);

		hBaseOperations.save(image);
	}

	/**
	 * 画像を保存しつつ、アップロードします。
	 * @param image 画像
	 * @param imageHeader 画像ヘッダー
	 * @param createThumbnail サムネイルを作成するかどうか
	 */
	public void saveAndUploadImage(
			ImageDO image,
			ImageHeaderDO imageHeader,
			boolean createThumbnail) {
		String mimeType = image.getMimeType();
		String ext = mimeType.substring(mimeType.lastIndexOf("/") + 1);
		//画像キャッシュアップロード
		String remoteTargetDirectory = (resourceConfig.imageUploadPath
				+ "/" + imageHeader.getPostContentType().getCode() + randomPath()).replace("//", "/");
		String remoteFileName = imageHeader.getImageId() + "." + ext;
		String imageUrl = resourceConfig.imageUrl + remoteTargetDirectory + "/" + remoteFileName;
		imageHeader.setImageUploadResult(
				imageCacheDao.upload(image.getData(), remoteTargetDirectory, remoteFileName));
		if (imageHeader.getImageUploadResult().equals(ImageUploadResult.SUCCESS)) {
			imageHeader.setImageSyncStatus(ImageSyncStatus.SYNC);
		} else {
			imageHeader.setImageSyncStatus(ImageSyncStatus.ERROR);
		}
		LOG.info("image upload success. communityUserId=" + imageHeader.getOwnerCommunityUserId()
				+ ", imageId=" + imageHeader.getImageId()
				+ ", imageUrl=" + imageUrl
				+ ", imageSyncStatus=" + imageHeader.getImageSyncStatus());

		imageHeader.setImageUrl(imageUrl);
		imageHeader.setStatus(ContentsStatus.SUBMITTED);

		if (createThumbnail) {
			try {
				ByteArrayOutputStream[] os = ImageUtils.resizeIfNomatchSize(
						ImageUtils.strip(new ByteArrayInputStream(image.getData())),
						ImageType.PNG,
						resourceConfig.thumbnailWidth,
						resourceConfig.thumbnailHeight,
						true);
				ImageDO thumbnailImage = new ImageDO();
				thumbnailImage.setCommunityUserId(image.getCommunityUserId());
				thumbnailImage.setData(os[0].toByteArray());
				thumbnailImage.setMimeType("image/" + ImageType.PNG.getCode());
				createTemporaryImage(thumbnailImage);
				ImageHeaderDO thumbnailImageHeader = new ImageHeaderDO();
				thumbnailImageHeader.setImageId(thumbnailImage.getImageId());
				thumbnailImageHeader.setAdult(imageHeader.isAdult());
				thumbnailImageHeader.setOwnerCommunityUser(
						imageHeader.getOwnerCommunityUser());
				thumbnailImageHeader.setPostContentType(imageHeader.getPostContentType());
				thumbnailImageHeader.setSku(imageHeader.getSku());
				thumbnailImageHeader.setThumbnail(true);
				if (imageHeader.getReview() != null) {
					thumbnailImageHeader.setReview(imageHeader.getReview());
				}
				saveAndUploadImage(thumbnailImage, thumbnailImageHeader, false);
				imageHeader.setTempThumbnailImage(thumbnailImageHeader);
				imageHeader.setThumbnailImageId(
						thumbnailImageHeader.getImageId());
				imageHeader.setThumbnailImageUrl(
						thumbnailImageHeader.getImageUrl());
			} catch (Throwable t) {
				throw new YcComException(t);
			}
		}

		hBaseOperations.save(imageHeader);

		checkUploaded(imageHeader.getOwnerCommunityUserId(),
				imageHeader.getImageId(),
				imageHeader.getImageUrl());
		solrOperations.save(imageHeader);
	}

	/**
	 * 画像を保持するコンテンツを更新します。
	 * @param imageHeaders 更新する画像ヘッダーリスト
	 */
	@Override
	public void updateImageHeaderRelationWithIndex(
			List<ImageHeaderDO> imageHeaders) {
		List<ImageHeaderDO> list = new ArrayList<ImageHeaderDO>();
		for (ImageHeaderDO imageHeader : imageHeaders) {
			list.add(imageHeader);
			if (imageHeader.getTempThumbnailImage() != null) {
				list.add(imageHeader.getTempThumbnailImage());
			}
		}
		hBaseOperations.save(ImageHeaderDO.class, list,
				Path.includeProp("reviewId"));
		solrOperations.save(ImageHeaderDO.class, list);
	}

	/**
	 * 指定したレビューIDに紐づく画像情報を返します。
	 * @param reviewId レビューID
	 * @return 画像情報
	 */
	@Override
	public List<ImageHeaderDO> findImageHeaderByReviewId(String reviewId) {
		return hBaseOperations.findWithIndex(ImageHeaderDO.class, "reviewId", reviewId);
	}

	/**
	 * レビュー情報をインデックスと一緒に登録します。
	 * @param review レビュー
	 */
	@Override
	public void createReviewWithIndex(AbstractReviewDO review) {
		hBaseOperations.save(review);
		solrOperations.save(review);
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public void createReviewsWithIndex(List<? extends AbstractReviewDO> reviews) {
		if (reviews.get(0) instanceof ReviewDO) {
			hBaseOperations.save(ReviewDO.class, (List<ReviewDO>)reviews);
			solrOperations.save(ReviewDO.class, (List<ReviewDO>)reviews);
		} else if (reviews.get(0) instanceof ReviewHistoryDO) {
			hBaseOperations.save(ReviewHistoryDO.class, (List<ReviewHistoryDO>)reviews);
			solrOperations.save(ReviewHistoryDO.class, (List<ReviewHistoryDO>)reviews);
		} else throw new RuntimeException();
	}

	/**
	 * 指定したレビュー情報を返します。
	 * @param oldReviewId 旧レビューID
	 * @return レビュー情報
	 */
	@Override
	public ReviewDO loadReviewByOldReviewId(String oldReviewId) {
		SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(
				solrOperations.findByQuery(
				new SolrQuery("oldReviewId_s:" + SolrUtil.escape(oldReviewId)), ReviewDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId").depth(1)));
		if (searchResult.getNumFound() > 0) {
			return searchResult.getDocuments().get(0);
		} else {
			return null;
		}
	}

	/**
	 * 指定した旧レビュー情報を返します。
	 * @param oldReviewId 旧レビューID
	 * @return 旧レビュー情報
	 */
	@Override
	public OldReviewDO loadOldReview(String oldReviewId) {
		return hBaseOperations.load(OldReviewDO.class, oldReviewId);
	}

	/**
	 * レビューの違反報告をインデックスと一緒に登録します。
	 * @param spamReport 違反報告
	 */
	@Override
	public void createSpamReportWithIndex(SpamReportDO spamReport) {
		if (StringUtils.isEmpty(spamReport.getSpamReportBody())) {
			spamReport.setSpamReportBody(null);
		}
		hBaseOperations.save(spamReport);
		solrOperations.save(spamReport);
	}

	/**
	 * 購入商品の「購入日固定化フラグ」をtrueに更新する。
	 */
	@Override
	public void updatePurchaseDateFix(List<PurchaseProductDO> purchaseProductList) {
//		PurchaseProductDO purchaseProduct = new PurchaseProductDO();
//		purchaseProduct.setPurchaseProductId(purchaseProductId);
//		purchaseProduct.setPurchaseDateFix(true);
//		purchaseProduct.setModifyDateTime(timestampHolder.getTimestamp());
//		hBaseOperations.save(purchaseProduct, Path.includeProp("purchaseDateFix"));
		hBaseOperations.save(PurchaseProductDO.class,
				purchaseProductList, Path.includeProp("purchaseDateFix"));
	}

	/**
	 * 移行レビューのレビュー投稿アクション履歴を生成する。
	 * @param review
	 */
	@Override
	public void createReviewActionHistory(ReviewDO review) {

		List<ActionHistoryDO> actionHistories = Lists.newArrayList();

		try {
			if (review == null) {
				LOG.info("-- skip review is null.");
				return;
			}
			if (review.getPostDate() == null) {
				LOG.info("-- skip postDate is null." 
						+ ", reviewId=" + review.getReviewId());
				return;
			}
			if (review.getOldReviewId() == null || review.getOldReviewId().length() == 0) {
				// 移行レビューでなければskip
				LOG.info("-- skip oldReviewId is empty. oldReviewId=" + review.getOldReviewId()
									+ ", reviewId=" + review.getReviewId());
				return;
			}
			
			// 自身のフォローユーザーに向けて、アクション履歴を記録します。
			ActionHistoryDO userActionHistory = new ActionHistoryDO();
			userActionHistory.setActionHistoryType(ActionHistoryType.USER_REVIEW);
			userActionHistory.setCommunityUser(review.getCommunityUser());
			userActionHistory.setReview(review);
			userActionHistory.setProduct(review.getProduct());
			userActionHistory.setAdult(review.isAdult());
			userActionHistory.setActionTime(review.getPostDate());
			actionHistories.add(userActionHistory);

			// 商品に対してアクションを記録します。
			ActionHistoryDO productActionHistory = new ActionHistoryDO();
			productActionHistory.setActionHistoryType(ActionHistoryType.PRODUCT_REVIEW);
			productActionHistory.setCommunityUser(review.getCommunityUser());
			productActionHistory.setProduct(review.getProduct());
			productActionHistory.setReview(review);
			productActionHistory.setAdult(review.isAdult());
			productActionHistory.setActionTime(review.getPostDate());
			actionHistories.add(productActionHistory);

			saveActions(actionHistories);
		} catch (Throwable th) {
			String msg = null;
			if (review == null) {
				msg = "-- review is null. : " + th.getMessage();
			} else {
				msg = "-- reviewId=" + review.getReviewId() + " : " + th.getMessage();
			}
			LOG.error(msg, th);
		}

		solrOperations.optimize(ActionHistoryDO.class);
	}

	private void saveActions(List<ActionHistoryDO> actionHistories) {
		Date timestamp = new Date();
		List<ActionHistoryDO> saveActionHistories = new ArrayList<ActionHistoryDO>();
		for(ActionHistoryDO actionHistory : actionHistories) {
			actionHistory.setActionHistoryId(IdUtil.generateActionHistoryId(actionHistory, idGenerator));
			actionHistory.setRegisterDateTime(timestamp);
			actionHistory.setModifyDateTime(timestamp);
			saveActionHistories.add(actionHistory);
			ReviewDO review = actionHistory.getReview();
			String reviewId = "default";
			String oldReviewId = "default_old";
			if (review != null) {
				reviewId = review.getReviewId();
				oldReviewId = review.getOldReviewId();
			}
			LOG.info("--- saved reviewId=" + reviewId
							+ ", oldReviewId=" + oldReviewId
							+ ", actionHistoryId=" + actionHistory.getActionHistoryId());
		}
		hBaseOperations.save(ActionHistoryDO.class, saveActionHistories);
		solrOperations.save(ActionHistoryDO.class, saveActionHistories);
	}

	/**
	 * 指定した画像をアップロード済みとして更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param imageId 画像ID
	 * @param imageUrl 画像URL
	 */
	private void checkUploaded(String communityUserId, String imageId, String imageUrl) {
		ImageDO image = new ImageDO();
		image.setImageId(imageId);
		image.setCommunityUserId(communityUserId);
		image.setImageUrl(imageUrl);
		image.setTemporaryFlag(false);
		hBaseOperations.save(image, Path.includeProp("imageUrl,communityUserId,temporaryFlag,modifyDateTime"));
	}

	/**
	 * ランダムなパスを返します。
	 * @return パス
	 */
	private String randomPath() {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < 4; i++) {
			buffer.append("/");
			buffer.append(getRandomChar());
			buffer.append(getRandomChar());
		}
		return buffer.toString();
	}

	/**
	 * ランダムなアルファベットを返します。
	 * @return ランダムなアルファベット
	 */
	private char getRandomChar() {
		int i = random.nextInt(36);
		if (i < 10) {
			return (char) (i + 48);
		} else {
			return (char) (i + 55);
		}
	}

}
