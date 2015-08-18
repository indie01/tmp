package com.kickmogu.yodobashi.community.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.ImageDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageSetDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ScoreFactorDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.TextEditableContents;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;
import com.kickmogu.yodobashi.community.service.vo.ImageDetailSetVO;
import com.kickmogu.yodobashi.community.service.vo.ProductImageActivityVO;
import com.kickmogu.yodobashi.community.service.vo.ImageSetVO;

/**
 * 画像サービスです。
 * @author kamiike
 *
 */
public interface ImageService {

	/**
	 * 指定した商品に対して投稿した画像件数を返します。
	 * @param sku SKU
	 * @return 画像件数
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品系の各画面で共通的に呼ばれるので頻度は高い",
		refClassNames={"AbstractProductBaseController"}
	)
	public long countImageBySku(String sku);
	
	/**
	 * 指定した商品に対して投稿した画像件数を返します。
	 * @param sku SKU
	 * @return 画像件数
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品系の各画面で共通的に呼ばれるので頻度は高い",
		refClassNames={"AbstractProductBaseController"}
	)
	public long countImageBySkus(List<String> skus);

	/**
	 * 指定した商品に対して投稿した画像件数を返します。
	 * @param skus SKUリスト
	 * @return 画像件数リスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品系の各画面で共通的に呼ばれるので頻度は高い",
		refClassNames={"AbstractProductBaseController"}
	)
	public Map<String, Long> countImageBySkus(String[] skus);
	
	/**
	 * 指定した商品に対して投稿した画像を投稿日時順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param offsetImageIndex 検索画像インデックス（基準が画像セットの場合は必須）
	 * @param previous より前を取得する場合、true
	 * @return 画像一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="商品画像一覧の初期表示、もっと見るAjaxで呼ばれるので頻度は中くらい",
		refClassNames={"AjaxJsonProductImageController","ProductImageListController"}
	)
	public SearchResult<ImageSetVO> findImagesBySku(
			String sku,
			int limit,
			int offset,
			boolean previous);
	
	@PerformanceTest(type=Type.SELECT,
			frequency=Frequency.MEDIUM,
			frequencyComment="ユーザー画像一覧の初期表示、もっと見るAjaxで呼ばれるので頻度は中くらい",
			refClassNames={"AjaxJsonUserImageController","UserImageListController"}
		)
	public SearchResult<ImageSetVO> findImagesByCommunityUserId(
			String communityUserId,
			int limit,
			int offset);
	
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
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="商品画像一覧の初期表示、もっと見るAjaxで呼ばれるので頻度は中くらい",
		refClassNames={"AjaxJsonProductImageController","ProductImageListController"}
	)
	public SearchResult<ImageSetVO> findImagesBySkus(
			String sku,
			List<String> skus, 
			int limit, 
			int offset,
			boolean previous);
	
	/**
	 * 指定の画像IDで画像情報を取得する。
	 * @param imageId 画像ID
	 * @return 画像情報
	 */
	@PerformanceTest(type=Type.SELECT,
			frequency=Frequency.MEDIUM,
			frequencyComment="商品画像ポップアップから呼び出される",
			refClassNames={"AjaxJsonImageController"}
		)
	public ImageSetVO getImageByImageId(String imageId);

	/**
	 * 指定した画像セットに紐付く画像を順番に返します。
	 * @param imageSetId 画像セットID
	 * @return 画像一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="商品画像詳細の初期表示で呼ばれるので頻度は中くらい",
		refClassNames={"ProductImageDetailController"}
	)
	public List<ImageHeaderDO> findImageByImageSetId(
			String imageSetId, String excludeImageId);

	/**
	 * 指定したcontentIdに紐付く画像を順番に返します。
	 * @param contentId imageTargetTypeに従ったIDを指定する。レビューID, 質問ID, 質問回答ID, 画像セットIDのいずれか
	 * @param imageTargetType  contentIdのタイプを指定する
	 * @param excludeImageId 除外する画像ID
	 * @return 画像一覧
	 */
	public SearchResult<ImageSetVO> findImageByContentId(
			String contentId,
			ImageTargetType imageTargetType,
			String excludeImageId);

	/**
	 * 指定したコミュニティユーザーが投稿した画像を投稿日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 画像一覧
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="マイページ、ユーザページの画像系アクティビティ一覧の初期表示ともっと見るクリック時のAjax処理で呼ばれるので頻度は中くらい",
		refClassNames={"AjaxJsonMypageActivityController","AjaxJsonUserActivityController","MypageActivityImageListController","UserActivityImageListController"}
	)
	public SearchResult<ProductImageActivityVO> findImageByCommunityUserId(
			String communityUserId, int limit, Date offsetTime, boolean previous);

	/**
	 * 指定した画像をインデックス情報から返します。
	 * @param imageId 画像ID
	 * @return 画像情報
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品系,ユーザ系,マイページ系すべての画像ポップアップの各Ajax処理で呼ばれるので頻度は高",
		refClassNames={"AjaxJsonProductImageDetailController","AjaxHtmlProductImageDetailController","SpamReportController","AgeVerifyIntercepter"}
	)
	public ImageDetailSetVO getImageHeaderFromIndex(
			String imageId, boolean includeDeleteContents);

	/**
	 * 画像を仮登録します。
	 * @param image 画像情報
	 * @return 仮登録した画像情報
	 */
	public ImageDO createTemporaryImage(ImageDO image);

	/**
	 * 仮登録した画像を更新します。
	 * @param image 画像情報
	 * @return 更新した仮登録画像情報
	 */
	public ImageDO updateTemporaryImage(ImageDO image);

	/**
	 * 指定した仮登録されている画像情報を返します。
	 * @param imageId 画像ID
	 * @return 仮登録した画像情報
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.LOW,
		frequencyComment="プロフィール編集で画像投稿時、質問、レビュー、回答での画像投稿時に呼ばれるので頻度は稀",
		refClassNames={"ImageController"}
	)
	public ImageDO getTemporaryImage(String imageId);
	/**
	 * 指定した仮登録されている画像情報を返します。
	 * @param imageId 画像ID
	 * @return 仮登録した画像情報
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.LOW,
		frequencyComment="プロフィール編集で画像投稿時、質問、レビュー、回答での画像投稿時に呼ばれるので頻度は稀",
		refClassNames={"ImageController"}
	)
	public List<ImageDO> getTemporaryImages(List<String> imageIds);
	
	/**
	 * 指定した本登録されている画像情報を返します。
	 * @param imageId 画像ID
	 * @return 本登録した画像情報
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.LOW,
		frequencyComment="プロフィール編集で画像投稿時、質問、レビュー、回答での画像投稿時に呼ばれるので頻度は稀",
		refClassNames={"ImageController"}
	)
	public ImageDO getPermanentImage(String imageId);



	/**
	 * 画像セットをアップロードします。
	 * @param sku 商品
	 * @param imageHeaders 画像ヘッダーリスト
	 * @return 画像ヘッダーリスト
	 */
	public List<ImageHeaderDO> saveImageSet(
			String sku,
			List<ImageHeaderDO> imageHeaders,
			Date inputPurchaseProductDate);
	
	/**
	 * 画像セットをアップロードします。
	 * @param sku 商品
	 * @param imageHeaders 画像ヘッダーリスト
	 * @return 画像ヘッダーリスト
	 */
	public List<ImageHeaderDO> saveImageSet(
			ProductDO product,
			List<ImageHeaderDO> imageHeaders,
			Date inputPurchaseProductDate);
	
	public ImageSetDO saveImageSet(ImageSetDO imageSet);
	
	public ImageSetDO modifyImageSet(ImageSetDO imageSet);
	
	public void deleteImageSet(
			String communityUserId,
			ImageTargetType imageTargetType,
			String contentsId, 
			boolean mngToolOperation);
	
	
	/**
	 * 画像セットの中から指定した画像を削除します。
	 * @param imageId 画像ID
	 */
	public void deleteImageInImageSet(String imageId);

	public void deleteImageInImageSet(String imageId, boolean mngToolOperation);

	
	/**
	 * 指定した画像のコメントを編集します。
	 * @param imageId 画像ID
	 * @param comment コメント
	 * @return 画像ヘッダー
	 */
	public ImageHeaderDO updateImageComment(
			String imageId,
			String comment);
	
	

	/**
	 * 指定したコンテンツに紐づく画像を削除します。
	 * @param communityUserId コミュニティユーザーID
	 * @param contentType コンテンツタイプ
	 * @param contentsId コンテンツID
	 * @param contents コンテンツ
	 * @param updateImageIds 更新画像IDのリスト
	 */
	public void deleteImagesInContents(
			String communityUserId,
			PostContentType contentType,
			String contentsId,
			TextEditableContents contents,
			Set<String> updateImageIds);

	/**
	 * 指定した質問に紐付く質問回答の画像を削除します。
	 * @param questionId 質問ID
	 */
	public void deleteQuestionAnswerImageByQuestionId(String questionId);

	/**
	 * 画像サーバとの同期エラーが出ているものを全て同期します。
	 */
	public void recoverImageSyncError();

	/**
	 * 画像情報のスコア情報を更新します。
	 * @param targetDate 対象日付
	 * @param imageHeader 画像ヘッダー
	 * @param scoreFactor スコア係数
	 */
	public void updateImageScoreForBatch(
			Date targetDate,
			ImageHeaderDO image,
			ScoreFactorDO scoreFactor);
	public void updateImageScoreForBatchBegin(int bulkSize);
	public void updateImageScoreForBatchEnd();
	
	
	/**
	 * 画像情報をHbaseから取得し、周辺情報を付与して返します。
	 * SearchResult形式ですが、1件のみ返します
	 * hasAdultはfalse
	 * @param reviewId
	 * @return
	 */
	public SearchResult<ImageSetVO> loadProductImageSummary(String imageSetId);
	
	/**
	 * 有効な画像が存在するか
	 * @param imageSetId
	 * @return
	 */
	public boolean existsEffectiveImage(String imageSetId);
	
	public ImageHeaderDO loadImageHeader(String imageId);

	public ImageHeaderDO loadImageHeaderFromIndex(String imageId,boolean includeDeleteContents);
	
	public ImageDO loadImage(String imageId);
	
	public String findProductSku(String imageId);

	boolean existsEffectiveImage(ImageTargetType imageTargetType, String contentId);

	public List<ImageHeaderDO> loadImagesByContentId(PostContentType postContentType, String contentId);
}
