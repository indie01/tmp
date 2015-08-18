package com.kickmogu.yodobashi.community.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.SaveImageDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.TextEditableContents;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewType;
import com.kickmogu.yodobashi.community.service.vo.CommentSetVO;

public interface CommonService {
	
	/**
	 * 指定したレビューに対するいいねを返します。
	 * @param reviewId レビューID
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param limit 最大取得件数
	 * @return いいねリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.LOW,
		frequencyComment="レビュー詳細画面で3人以上いいねがいたら呼ばれるので頻度は低",
		refClassNames={"AjaxJsonProductReviewDetailController"}
	)
	public SearchResult<LikeDO> findLikeByReviewId(
			String reviewId, String excludeCommunityUserId, int limit);
	
	/**
	 * 指定した画像に対するいいねを返します。
	 * @param imageId 画像ID
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param limit 最大取得件数
	 * @return いいねリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.LOW,
		frequencyComment="商品画像詳細画面で3人以上いいねがいたら呼ばれるので頻度は低",
		refClassNames={"AjaxJsonProductImageDetailController"}
	)
	public SearchResult<LikeDO> findLikeByImageId(
			String imageId, String excludeCommunityUserId, int limit);

	/**
	 * 指定した質問回答に対するいいねを返します。
	 * @param questionAnswerId 質問回答ID
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param limit 最大取得件数
	 * @return いいねリスト
	 */
	@PerformanceTest(type=Type.SELECT,
			frequency=Frequency.LOW,
			frequencyComment="質問詳細画面で回答に3人以上いいねがいたら呼ばれるので頻度は低",
			refClassNames={"AjaxJsonProductQuestionDetailController"}
		)	
	public SearchResult<LikeDO> findLikeByQuestionAnswerId(
			String questionAnswerId, String excludeCommunityUserId, int limit);
	
	/**
	 * 指定したレビューに対するコメントを返します。
	 * @param reviewId レビューID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return コメントリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品レビュー詳細画面の初期表示、Ajax処理で呼ばれるので頻度は高",
		refClassNames={"AjaxJsonProductReviewDetailController","ProductReviewDetailController"}
	)
	public SearchResult<CommentSetVO> findReviewCommentByReviewId(
			String reviewId, List<String> excludeCommentIds, int limit, Date offsetTime, boolean previous);
	
	/**
	 * 指定した画像に対するコメントを返します。
	 * @param imageId 画像ID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return コメントリスト
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.MEDIUM,
		frequencyComment="画像一覧の画像をクリックしたときのAjax処理でしか呼ばれないので頻度は中",
		refClassNames={"AjaxJsonProductImageDetailController"}
	)
	public SearchResult<CommentSetVO> findImageCommentByImageId(
			String imageId, List<String> excludeCommentIds, int limit, Date offsetTime, boolean previous);

	

	/**
	 * 指定した質問回答に対するコメントを返します。
	 * @param questionAnswerId 質問回答ID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return コメントリスト
	 */
	@PerformanceTest(type=Type.SELECT,
			frequency=Frequency.MEDIUM,
			frequencyComment="商品質問画面で回答のコメントをクリックしたときのAjax処理で呼ばれるので頻度は中",
			refClassNames={"AjaxJsonProductQuestionDetailController"}
		)
	public SearchResult<CommentSetVO> findQuestionAnswerCommentByQuestionAnswerId(
			String questionAnswerId, List<String> excludeCommentIds, int limit, Date offsetTime, boolean previous);

	
	/**
	 * 指定の質問に対して、指定したコミュニティユーザーが回答しているかどうか
	 * 返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param questionId 質問ID
	 * @return 回答している場合、true
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.HIGH,
		frequencyComment="商品質問詳細で初期表示時に呼ばれるので頻度は高",
		refClassNames={
			"ProductQuestionDetailController"
			}
	)
	public Map<String, Boolean> hasQuestionAnswer(String communityUserId, List<String> questionIds);
	
	/**
	 * 指定した形式のレビューを投稿可能かどうか判定します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @param reviewType レビュータイプ
	 * @return 投稿可能な場合、true
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.RARE,
		frequencyComment="レビュー投稿時に呼ばれるので頻度は稀",
		refClassNames={
			"AjaxJsonProductReviewSubmitController"
			}
	)
	public boolean canPostReview(
			String communityUserId, String sku, ReviewType reviewType);
	
	/**
	 * 指定した形式のレビューを投稿可能かどうか判定します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @param reviewType レビュータイプ
	 * @return 投稿可能な場合、true
	 */
	@PerformanceTest(type=Type.SELECT,
		frequency=Frequency.RARE,
		frequencyComment="レビュー投稿時に呼ばれるので頻度は稀",
		refClassNames={
			"AjaxJsonProductReviewSubmitController"
			}
	)
	public boolean canPostReview(
			String communityUserId, 
			ProductDO product,
			PurchaseProductDO purchaseProduct,
			ReviewType reviewType);
	
	/**
	 * コンテンツに関連数する画像情報を更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @param postContentType 投稿タイプ
	 * @param adult アダルト商品に紐付く画像かどうか
	 * @param contents コンテンツ
	 * @param updateImageIds 更新画像IDのリスト
	 * @param uploadImageMap アップロード対象となる画像マップ
	 */
	public void updateImageRelateContents(
			String communityUserId,
			String sku,
			PostContentType postContentType,
			boolean adult,
			TextEditableContents contents,
			List<SaveImageDO> saveImages,
			Map<String, ImageHeaderDO> uploadImageMap);
	
	/**
	 * コンテンツに関連数する画像情報を更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @param postContentType 投稿タイプ
	 * @param adult アダルト商品に紐付く画像かどうか
	 * @param contents コンテンツ
	 * @param updateImageIds 更新画像IDのリスト
	 * @param uploadImageMap アップロード対象となる画像マップ
	 */
	public void updateImageRelateContents(
			String communityUserId,
			String sku,
			PostContentType postContentType,
			boolean adult,
			TextEditableContents contents,
			List<SaveImageDO> saveImages,
			Map<String, ImageHeaderDO> uploadImageMap,
			int offsetIndex);

}
