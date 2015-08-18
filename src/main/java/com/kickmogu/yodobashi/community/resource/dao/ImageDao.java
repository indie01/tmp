/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;
import com.kickmogu.yodobashi.community.resource.domain.constants.Verification;

/**
 * 画像 DAO です。
 * @author kamiike
 *
 */
public interface ImageDao {

	/**
	 * 指定した画像IDの画像を取得します。
	 * @param imageId 画像ID
	 * @param condition 取得条件
	 * @return 画像
	 */
	public ImageDO loadImage(String imageId, Condition condition);
	/**
	 * 指定した画像ID一覧の画像を取得します。
	 * @param imageId 画像ID
	 * @param condition 取得条件
	 * @return 画像一覧
	 */
	public List<ImageDO> loadImages(List<String> imageIds, Condition condition);
	
	/**
	 * 指定した画像IDの画像ヘッダーを取得します。
	 * @param imageId 画像ID
	 * @return 画像ヘッダー
	 */
	public ImageHeaderDO loadImageHeader(String imageId);

	/**
	 * 指定した画像IDの画像ヘッダーを取得します。
	 * @param imageId 画像ID
	 * @return 画像ヘッダー
	 */
	public ImageHeaderDO loadImageHeaderFromIndex(String imageId);

	public ImageHeaderDO loadImageHeaderFromIndex(String imageId, Boolean includeDeleteContents);

	/**
	 * 画像を一時保存します。
	 * @param image 画像
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="画像の登録系処理は稀")
	public void createTemporaryImage(ImageDO image);

	/**
	 * 画像を更新します。
	 * @param image 画像
	 * @param condition 更新条件
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="画像の登録系処理は稀")
	public void updateImage(ImageDO image, Condition condition);

	/**
	 * 画像ヘッダーを更新します。
	 * @param imageHeader 画像ヘッダー
	 * @param condition 更新条件
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="画像の登録系処理は稀")
	public void updateImageHeader(ImageHeaderDO imageHeader, Condition condition);

	/**
	 * 指定した画像をアップロードします。
	 * @param imageId 画像ID
	 * @return アップロードした場合、true
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="画像の登録系処理は稀")
	public boolean uploadImageForSync(String imageId);

	/**
	 * 画像をアップロードします。
	 * @param imageHeader 画像ヘッダー
	 * @param createThumbnail サムネイルを作成するかどうか
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="uploadImage(ImageHeaderDO imageHeader, boolean createThumbnail)のメソッドとかぶるのでテスト対象外")
	public void uploadImage(ImageHeaderDO imageHeader, Boolean createThumbnail);

	/**
	 * アップロードした画像ヘッダー情報を保存します。
	 * @param imageHeader 画像ヘッダー
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="画像の登録系処理は稀")
	public void saveUploadImageHeader(
			ImageHeaderDO imageHeader);

	/**
	 * 画像を保存しつつ、アップロードします。
	 * @param imageHeader 画像ヘッダー
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="他のテストメソッドがあるのでテスト対象外")
	public void saveAndUploadImage(
			ImageHeaderDO imageHeader);

	/**
	 * 画像を保存しつつ、アップロードします。
	 * @param imageHeader 画像ヘッダー
	 * @param createThumbnail サムネイルを作成するかどうか
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="他のテストメソッドがあるのでテスト対象外")
	public void saveAndUploadImage(ImageHeaderDO imageHeader, Boolean createThumbnail);
	
	public void updateImageInIndex(String imageId, Boolean withThumbnail, Boolean mngToolOperation);

	/**
	 * 指定した画像セットのインデックスを削除します。
	 * @param imageSetId 画像セットID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="画像の登録系処理は稀")
	public void deleteImageSetIndex(String imageSetId, String thumbnailImageId, Boolean mngToolOperation);

	/**
	 * 指定した画像セットのアクション履歴を削除します。
	 * @param imageSetId 画像セットID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="画像の登録系処理は稀")
	public List<String> deleteImageSetActionHistory(String imageSetId);

	/**
	 * 指定した画像IDの画像を削除します。<br />
	 * アップロード済みの情報の場合、アップロードした画像も削除します。<br />
	 * オーナー情報（コンテンツタイプ、コンテンツID）が一致しない場合は、無視します。
	 * @param contentType コンテンツタイプ
	 * @param contentsId コンテンツID
	 * @param imageId 画像ID
	 * @param logical 論理削除かどうか
	 * @return 成功した場合、true。無視した場合、false
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="画像の登録系処理は稀")
	public boolean deleteBothImage(
			PostContentType contentType,
			String contentsId,
			String imageId,
			Boolean logical,
			Boolean mngToolOperation,
			ContentsStatus status);

	/**
	 * 物理画像を全て物理削除します。
	 * @param imageIds 画像IDのリスト
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="画像の登録系処理は稀")
	public void deleteImages(List<String> imageIds);

	/**
	 * 一覧表示フラグを更新します。
	 * @param imageId 画像ID
	 * @param listViewFlag 一覧表示フラグ
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="画像の登録系処理は稀")
	public void updateListViewFlag(String imageId, Boolean listViewFlag);

	/**
	 * 指定した質問に紐づく全ての画像情報を返します。
	 * @param questionId 質問ID
	 * @return 画像情報一覧
	 */
	public List<ImageHeaderDO> findImageHeaderAllByQuestionId(
			String questionId);

	/**
	 * 画像IDのリストを検証します。
	 * @param imageIds 画像IDリスト
	 * @param communityUserId コミュニティユーザーID
	 * @return 検証された画像IDのリスト
	 */
	public Set<String> validateImageIds(Set<String> imageIds, String communityUserId);

	/**
	 * 指定したタイプのトップ画像マップを返します。
	 * @param postContentType タイプ
	 * @param contentsIds コンテンツIDのリスト
	 * @return トップ画像マップ
	 */
	public Map<String, ImageHeaderDO> loadTopImageMapByContentsIds(
			PostContentType postContentType,
			List<String> contentsIds);
	
	/**
	 * 指定したタイプのトップ画像マップを返します。
	 * @param postContentType タイプ
	 * @param contentsIds コンテンツIDのリスト
	 * @return トップ画像マップ
	 */
	public Map<String, List<ImageHeaderDO>> loadAllImageMapByContentsIds(
			PostContentType postContentType,
			List<String> contentsIds);

	/**
	 * 指定した商品に対して投稿した画像を投稿日時順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param offsetImageIndex 検索画像インデックス（基準が画像セットの場合は必須）
	 * @param all 全てを取得するかどうか
	 * @param previous より前を取得する場合、true
	 * @return 画像一覧
	 */
	public SearchResult<ImageHeaderDO> findImagesBySku(
			String sku,
			int limit,
			int offset,
			boolean previous);
	
	/**
	 * 指定した商品に対して投稿した画像を投稿日時順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param offsetImageIndex 検索画像インデックス（基準が画像セットの場合は必須）
	 * @param all 全てを取得するかどうか
	 * @param previous より前を取得する場合、true
	 * @return 画像一覧
	 */
	public SearchResult<ImageHeaderDO> findImagesBySkus(
			List<String> skus,
			int limit,
			int offset,
			boolean previous);
	
	/**
	 * 指定の画像IDから画像情報を取得する。
	 * @param imageId 画像ID
	 * @return 画像情報
	 */
	public ImageHeaderDO getImageByImageId(String imageId);
	
	/**
	 * 指定した期間に更新のあった画像を返します。
	 * @param fromDate 検索開始時間
	 * @param toDate 検索終了時間
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return
	 */
	public SearchResult<ImageHeaderDO> findUpdatedImageByOffsetTime(
			Date fromDate, Date toDate, int limit, int offset);
		
	/**
	 * 指定したユーザーの全ての有効、一時停止画像を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return
	 */
	public SearchResult<ImageHeaderDO> findImageByCommunityUserId(
			String communityUserId, int limit, int offset);
	
	/**
	 * 指定したユーザーの全ての有効、一時停止画像を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return
	 */
	public SearchResult<ImageHeaderDO> findImagesByCommunityUserId(
			String communityUserId, int limit, int offset);
	
	/**
	 * 指定した商品に対して投稿した画像件数を返します。
	 * @param skus SKU
	 * @return 画像件数
	 */
	public long countImageBySku(String sku);
	
	/**
	 * 指定した商品に対して投稿した画像件数を返します。
	 * @param skus SKU
	 * @return 画像件数
	 */
	public long countImageBySkus(List<String> skus);
	
	/**
	 * 指定した商品に対して投稿した画像件数を返します。
	 * @param skus SKUリスト
	 * @return 画像件数リスト
	 */
	public Map<String, Long> countImageBySku(
			String[] skus);

	/**
	 * 指定したコミュニティユーザーが投稿した画像を投稿日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @param type 絞込みタイプ
	 * @param アダルト確認フラグ
	 * @return 画像一覧
	 */
	public SearchResult<ImageHeaderDO> findImageSetByCommunityUserId(
			String communityUserId,
			int limit,
			Date offsetTime, 
			boolean previous,
			Verification adultVerification);

	public long countImageSetByCommunityUserId(
			String communityUserId);

	public long countImageSetByCommunityUserIdForMypage(String communityUserId);
	
	public long countImageSetByCommunityUserId(
			String communityUserId,
			String excludeImageSetId,
			ContentsStatus[] statuses,
			Verification adultVerification);

	/**
	 * 指定した画像情報に紐付くコンテンツに紐付く画像数を返します。
	 * @param imageHeaders 画像ヘッダーのリスト
	 * @return 画像数マップ
	 */
	public Map<String, Long> loadContentsImageCountMap(
			List<ImageHeaderDO> imageHeaders);

	/**
	 * 指定した画像セットの中でトップ画像を返します。
	 * @param imageSetId 画像セットID
	 * @return 画像
	 */
	public ImageHeaderDO loadTopImage(
			String imageSetId);

	/**
	 * 指定した画像セットに紐付く画像を順番に返します。
	 * @param reviewId レビューID
	 * @return 画像一覧
	 */
	public List<ImageHeaderDO> findImageByReviewId(
			String reviewId, String excludeImageId,
			ContentsStatus[] statuses);
	/**
	 * 指定した画像セットに紐付く画像を順番に返します。
	 * @param questionId 質問ID
	 * @return 画像一覧
	 */
	public List<ImageHeaderDO> findImageByQuestionId(
			String questionId, String excludeImageId,
			ContentsStatus[] statuses);
	/**
	 * 指定した画像セットに紐付く画像を順番に返します。
	 * @param questionAnswerId 質問回答ID
	 * @return 画像一覧
	 */
	public List<ImageHeaderDO> findImageByQuestionAnswerId(
			String questionAnswerId, String excludeImageId,
			ContentsStatus[] statuses);
	/**
	 * 指定した画像セットに紐付く画像を順番に返します。
	 * @param imageSetId 画像セットID
	 * @return 画像一覧
	 */
	public List<ImageHeaderDO> findImageByImageSetId(
			String imageSetId, String excludeImageId,
			ContentsStatus[] statuses);
	/**
	 * 指定した画像セットに紐付く画像を順番に返します。
	 * @param contentId コンテンツID
	 * @param imageTargetType 画像投稿タイプ（１：レビュー、２：質問、３：質問回答、４：画像）
	 * @return 画像一覧
	 */
	public List<ImageHeaderDO> findImageByContentId(
			String contentId,
			ImageTargetType imageTargetType,
			String excludeImageId,
			ContentsStatus[] statuses);
	
	public List<ImageHeaderDO> findImageByContentIdWithCommunityUserId(
			String communityUserId,
			String contentId,
			ImageTargetType imageTargetType,
			String excludeImageId,
			ContentsStatus[] statuses);

	/**
	 * 指定した画像の画像セットIDに紐付く画像リストマップ返します。
	 * @param imageHeaders 画像ヘッダーのリスト
	 * @return 画像セットマップ
	 */
	public Map<String, List<ImageHeaderDO>> loadImageSetMapByImageSetIds(
			List<ImageHeaderDO> imageHeaders);

	/**
	 * 画像数情報を返します。
	 * @param skus SKUリスト
	 * @return 画像数情報
	 */
	public Map<String, Long> loadImageCountMapBySKU(List<String> skus);

	/**
	 * 画像数情報を返します。
	 * @param communityUserIds コミュニティユーザーIDのリスト
	 * @return 画像数情報
	 */
	public Map<String, Long> loadImageCountMapByCommunityUserId(
			List<String> communityUserIds);

	/**
	 * 投稿画像セット数を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @return 投稿画像セット数
	 */
	public long countPostImageSetCount(String communityUserId, String sku);

	/**
	 * 指定した商品に画像を投稿したユーザーを重複を除いて返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param all 全てを対象とする場合（レビュー、質問、回答本文内含む）
	 * @return コミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findDistinctImageUploaderBySKU(
			String sku, int limit, int offset, boolean all);

	/**
	 * 指定したコミュニティユーザーが指定した日付に投稿した画像セットを返します。
	 * @param communityUserIds コミュニティユーザーIDリスト
	 * @param publicDate 公開された日付
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return 画像リスト
	 */
	public SearchResult<ImageHeaderDO> findImageSetByCommunityUserIds(
			List<String> communityUserIds, Date publicDate, int limit, int offset);
	public SearchResult<ImageHeaderDO> findImageSetByCommunityUserIdsForMR(
			List<String> communityUserIds, Date publicDate, int limit, int offset);
	
	

	/**
	 * 指定した商品、日付に投稿した画像セットを返します。
	 * @param skus SKUリスト
	 * @param publicDate 公開された日付
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return 画像セットリスト
	 */
	public SearchResult<ImageHeaderDO> findImageSetBySKUs(
			List<String> skus, Date publicDate, String excludeCommunityId, int limit, int offset);
	public SearchResult<ImageHeaderDO> findImageSetBySKUsForMR(
			List<String> skus, Date publicDate, String excludeCommunityId, int limit, int offset);

	/**
	 * 画像サーバとの同期エラーが出ているものを全て同期します。
	 */
	public void recoverImageSyncError();

	/**
	 * 指定した画像セットの中で画像を返します。
	 * @param imageSetId 画像セットID
	 * @return 画像
	 */
	public List<ImageHeaderDO> loadImages(String imageSetId);

	public List<ImageHeaderDO> loadImages(String imageSetId, Path.Condition path);

	public long countQuestionsImage(String questionId);

	public long countReviewsImage(String reviewId);

	public long countQuestionAnswersImage(String questionAnswerId);

	public void loadTopImageMapByContentsIds(
			List<String> reviewIds, 
			List<String> questionIds, 
			List<String> questionAnswerIds,
			Map<String, ImageHeaderDO> reviewImageMap, 
			Map<String, ImageHeaderDO> questionImageMap, 
			Map<String, ImageHeaderDO> questionAnswerImageMap);
	
	public void loadAllImageMapByContentsIds(
			List<String> reviewIds, 
			List<String> questionIds, 
			List<String> questionAnswerIds,
			List<String> imageSetIds,
			Map<String, List<ImageHeaderDO>> reviewImageMap, 
			Map<String, List<ImageHeaderDO>> questionImageMap, 
			Map<String, List<ImageHeaderDO>> questionAnswerImageMap,
			Map<String, List<ImageHeaderDO>> imageSetImageMap);

	/**
	 * 指定した画像セットに紐付く画像を取得します。
	 * @param imageSetId 画像セットID
	 */
	public void loadImageMapByImageSetId(List<String> imageSetIds, Map<String, List<ImageHeaderDO>> imageMap);
	
	public String findProductSku(String imageId);
	
	/**
	 * 指定した画像投稿タイプのcontentIdに紐づく画像一覧を取得します。
	 * @param contentId コンテンツID（imageTargetTypeに従ったIDを指定する）
	 * @param imageTargetType 画像投稿タイプ（１：レビュー、２：質問、３：質問回答、４：画像）
	 * @param path パス
	 * @return 画像一覧
	 */
	public List<ImageHeaderDO> loadImages(ImageTargetType imageTargetType, String contentId, Condition path);

	/**
	 * 指定した画像投稿タイプのcontentIdに紐づく画像一覧を取得します。
	 * @param contentId コンテンツID(imageTargetTypeに従ったIDを指定する)
	 * @param imageTargetType 画像投稿タイプ（１：レビュー、２：質問、３：質問回答、４：画像）
	 * @param path パス
	 * @return 画像一覧
	 */
	public List<ImageHeaderDO> loadImages(ImageTargetType imageTargetType, String contentId);
	
	/**
	 * 指定した画像投稿タイプのcontentIdに紐づく画像一覧を取得します(Solrから)。
	 * @param contentId コンテンツID(imageTargetTypeに従ったIDを指定する)
	 * @param imageTargetType 画像投稿タイプ（１：レビュー、２：質問、３：質問回答、４：画像）
	 * @param path パス
	 * @return 画像一覧
	 */
	public List<ImageHeaderDO> loadImagesFromIndex(PostContentType postContentType, String contentId);

	/**
	 * 指定したコミュニティユーザーIDに紐づくサムネイル画像を一覧を取得します。
	 * @param ownerCommunityUserId
	 * @param imageLimit 取得上限数
	 * @return
	 */
	public SearchResult<ImageHeaderDO> loadThumbnailImagesByOwnerCommunityUserId(String ownerCommunityUserId, int imageLimit);
}
