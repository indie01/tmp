/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import java.util.List;
import java.util.Map;

import com.kickmogu.yodobashi.community.resource.domain.AbstractReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.MigrationCommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.OldImageDO;
import com.kickmogu.yodobashi.community.resource.domain.OldReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.OldReviewHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.OldSpamReportDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SpamReportDO;
import com.kickmogu.yodobashi.community.resource.domain.SpoofingNameDO;

/**
 * 移行用 DAO です。
 * @author kamiike
 *
 */
public interface MigrationDao {

	/**
	 * 移行用コミュニティユーザーを作成します。
	 * @param migrationCommunityUser 移行用コミュニティユーザー
	 * @param spoofingName なりすまし判定クラス
	 */
	public void createMigrationCommunityUser(
			MigrationCommunityUserDO migrationCommunityUser,
			SpoofingNameDO spoofingName);

	/**
	 * 指定した外部顧客IDの移行用コミュニティユーザーを返します。
	 * @param outerCustomerId 外部顧客ID
	 * @return 移行用コミュニティユーザー
	 */
	public MigrationCommunityUserDO loadMigrationCommunityUserByOuterCustomerId(String outerCustomerId);

	/**
	 * 指定した外部顧客IDの移行用コミュニティユーザーを削除します。
	 * @param outerCustomerId 外部顧客ID
	 */
	public void deleteMigrationCommunityUser(String outerCustomerId);

	/**
	 * 旧レビュー情報を生成します。
	 * @param oldReview 旧レビュー
	 */
	public void createOldReview(OldReviewDO oldReview);

	/**
	 * 旧レビュー履歴情報を生成します。
	 * @param oldHistoryReview 旧履歴レビュー
	 */
	public void createOldReviewHistory(OldReviewHistoryDO oldHistoryReview);

	/**
	 * 旧画像情報を生成します。
	 * @param oldImage 旧画像
	 */
	public void createOldImage(OldImageDO oldImage);

	/**
	 * 旧違反報告情報を生成します。
	 * @param oldSpamReport 旧違反報告情報
	 */
	public void createOldSpamReport(OldSpamReportDO oldSpamReport);

	/**
	 * 指定した外部顧客IDに紐づく旧レビュー情報を返します。
	 * @param outerCustomerId 外部顧客ID
	 * @return 旧レビューリスト
	 */
	public List<OldReviewDO> findOldReviewByOuterCustomerId(String outerCustomerId);

	/**
	 * 指定した外部顧客IDに紐づく旧レビュー履歴情報を返します。
	 * @param outerCustomerId 外部顧客ID
	 * @return 旧レビュー履歴リスト
	 */
	public List<OldReviewHistoryDO> findOldReviewHistoryByOuterCustomerId(String outerCustomerId);

	/**
	 * 指定したキーの旧画像情報を返します。
	 * @param ids キーリスト
	 * @return 旧画像情報マップ
	 */
	public Map<String, OldImageDO> findOldImage(List<String> ids);

	/**
	 * 指定した外部顧客ID、もしくは旧レビューIDに紐づく旧違反報告情報を返します。
	 * @param outerCustomerId 外部顧客ID
	 * @param oldReviewIds 旧レビューIDリスト
	 * @return 旧違反報告情報リスト
	 */
	public List<OldSpamReportDO> findOldSpamReportByOuterCustomerIdOrOldReviewId(
			String outerCustomerId,
			List<String> oldReviewIds);

	/**
	 * 指定した旧違反報告を移行済みに更新します。
	 * @param oldSpamReportId 旧違反報告ID
	 */
	public void markOldSpamReport(String oldSpamReportId);

	/**
	 * 画像を一時保存します。
	 * @param image 画像
	 */
	public void createTemporaryImage(ImageDO image);

	/**
	 * 画像を保存しつつ、アップロードします。
	 * @param image 画像
	 * @param imageHeader 画像ヘッダー
	 * @param createThumbnail サムネイルを作成するかどうか
	 */
	public void saveAndUploadImage(
			ImageDO image,
			ImageHeaderDO imageHeader,
			boolean createThumbnail);

	/**
	 * 画像を保持するコンテンツを更新します。
	 * @param imageHeaders 更新する画像ヘッダーリスト
	 */
	public void updateImageHeaderRelationWithIndex(
			List<ImageHeaderDO> imageHeaders);

	/**
	 * 指定したレビューIDに紐づく画像情報を返します。
	 * @param reviewId レビューID
	 * @return 画像情報
	 */
	public List<ImageHeaderDO> findImageHeaderByReviewId(String reviewId);

	/**
	 * レビュー情報をインデックスと一緒に登録します。
	 * @param review レビュー
	 */
	public void createReviewWithIndex(AbstractReviewDO review);
	
	public void createReviewsWithIndex(List<? extends AbstractReviewDO> reviews);

	/**
	 * 指定したレビュー情報を返します。
	 * @param oldReviewId 旧レビューID
	 * @return レビュー情報
	 */
	public ReviewDO loadReviewByOldReviewId(String oldReviewId);

	/**
	 * 指定した旧レビュー情報を返します。
	 * @param oldReviewId 旧レビューID
	 * @return 旧レビュー情報
	 */
	public OldReviewDO loadOldReview(String oldReviewId);

	/**
	 * レビューの違反報告をインデックスと一緒に登録します。
	 * @param spamReport 違反報告
	 */
	public void createSpamReportWithIndex(SpamReportDO spamReport);

	/**
	 * 
	 * @param purchaseProductList
	 */
	void updatePurchaseDateFix(List<PurchaseProductDO> purchaseProductList);

	/**
	 * 移行レビューのレビュー投稿アクション履歴を生成する。
	 * @param review
	 */
	void createReviewActionHistory(ReviewDO review);
}
