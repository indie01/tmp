/**
 *
 */
package com.kickmogu.yodobashi.community.service;

import java.util.HashMap;
import java.util.List;

import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;





/**
 * インデックスサービスです。
 * @author kamiike
 *
 */
public interface IndexService {

	/**
	 * アクション履歴のインデックス更新とリンク生成を行います。
	 * @param actionHistoryIds アクション履歴IDリスト
	 */
	public void updateActionHistoryIndexWithCreateLink(
			String... actionHistoryIds);

	/**
	 * アクション履歴のインデックス更新とリンク生成を行います。
	 * @param actionHistory アクション履歴
	 */
	public void updateActionHistoryIndexWithCreateLink(ActionHistoryDO actinHistory);

	/**
	 * コミュニティユーザー作成に伴うインデックス更新を行います。
	 * @param communityUserId コミュニティユーザーID
	 * @param profileImageId プロフィール画像ID
	 * @param thumbnailImageId サムネイル画像ID
	 * @param informationId お知らせID
	 */
	public void updateIndexForCreateCommunityUser(
			String communityUserId,
			String profileImageId,
			String thumbnailImageId,
			String informationId);

	/**
	 * コミュニティユーザー更新に伴うインデックス更新を行います。
	 * @param communityUserId コミュニティユーザーID
	 * @param imageIds 画像IDリスト
	 */
	public void updateIndexForUpdateCommunityUser(
			String communityUserId,
			String... imageIds);

	/**
	 * レビュー投稿に伴うインデックス更新を行います。
	 * @param reviewId レビューID
	 * @param reviewHistoryId レビュー履歴ID
	 * @param previousReviewIds 前のレビューID一覧
	 * @param previousReviewHistoryIds 前のレビュー履歴ID
	 * @param purchaseProductId 購入商品情報ID
	 * @param imageIds 画像IDのリスト
	 * @param userActionHistoryId ユーザーアクション履歴ID
	 * @param productActionHistoryId 商品アクション履歴ID
	 */
	public void updateIndexForSaveReview(
			String reviewId,
			String reviewHistoryId,
			String[] previousReviewIds,
			String purchaseProductId,
			String[] imageIds,
			String userActionHistoryId,
			String productActionHistoryId);

	/**
	 * ポイント付与フィードバックに伴うインデックス更新を行います。
	 * @param reviewId レビューID
	 * @param informationId お知らせID
	 */
	public void updateIndexForPointGrantFeedback(
			String reviewId,
			String informationId);

	/**
	 * 質問投稿に伴うインデックス更新を行います。
	 * @param questionId 質問ID
	 * @param imageIds 画像IDのリスト
	 * @param userActionHistoryId ユーザーアクション履歴ID
	 * @param productActionHistoryId 商品アクション履歴ID
	 */
	public void updateIndexForSaveQuestion(
			String questionId,
			String[] imageIds,
			String userActionHistoryId,
			String productActionHistoryId);

	/**
	 * 質問回答投稿に伴うインデックス更新を行います。
	 * @param questionAnswerId 質問回答ID
	 * @param purchaseProductId 購入商品情報ID
	 * @param imageIds 画像IDのリスト
	 * @param userActionHistoryId ユーザーアクション履歴ID
	 * @param productActionHistoryId 商品アクション履歴ID
	 * @param questionActionHistoryId 質問アクション履歴ID
	 * @param informationId お知らせID
	 */
	public void updateIndexForSaveQuestionAnswer(
			String questionAnswerId,
			String purchaseProductId,
			String[] imageIds,
			String userActionHistoryId,
			String productActionHistoryId,
			String questionActionHistoryId,
			String informationId);

	/**
	 * コミュニティユーザーのフォロー関連情報のインデックスを更新します。
	 * @param communityUserId フォロワーとなるコミュニティユーザーID
	 * @param followCommunityUserId フォローするコミュニティユーザーID
	 * @param actionHistoryId アクション履歴ID
	 * @param informationId お知らせ情報ID
	 */
	public void updateIndexForCommunityUserFollow(
			String communityUserId,
			String followCommunityUserId,
			String actionHistoryId,
			String informationId);

	/**
	 * 商品のフォロー関連情報のインデックスを更新します。
	 * @param communityUserId フォロワーとなるコミュニティユーザーID
	 * @param followProductId フォローする商品ID
	 * @param actionHistoryId アクション履歴ID
	 */
	public void updateIndexForProductFollow(
			String communityUserId,
			String followProductId,
			String actionHistoryId);

	/**
	 * 質問のフォロー関連情報のインデックスを更新します。
	 * @param communityUserId フォロワーとなるコミュニティユーザーID
	 * @param followQuestionId フォローする質問ID
	 * @param actionHistoryId アクション履歴ID
	 */
	public void updateIndexForQuestionFollow(
			String communityUserId,
			String followQuestionId,
			String actionHistoryId);

	/**
	 * コメント登録に伴うインデックス更新を行います。
	 * @param commentId コメントID
	 * @param actionHistoryId アクション履歴ID
	 * @param informationId お知らせ情報ID
	 */
	public void updateIndexForSaveComment(
			String commentId,
			String actionHistoryId,
			String informationId);

	/**
	 * いいね登録に伴うインデックス更新を行います。
	 * @param likeId いいねID
	 * @param actionHistoryId アクション履歴ID
	 * @param informationId お知らせ情報ID
	 */
	public void updateIndexForSaveLike(
			String likeId,
			String actionHistoryId,
			String informationId);
	
	/**
	 * 参考になった登録に伴うインデックス更新を行います。
	 * @param votingId 参考になったID
	 * @param actionHistoryId アクション履歴ID
	 * @param informationId お知らせ情報ID
	 */
	public void updateIndexForSaveVoting(
			String votingId,
			String actionHistoryId,
			String informationId);

	/**
	 * 画像セット登録に伴うインデックス更新を行います。
	 * @param imageIds 画像IDのリスト
	 * @param purchaseProductId 購入商品情報ID
	 * @param userActionHistoryId ユーザーアクション履歴ID
	 * @param productActionHistoryId 商品アクション履歴ID
	 */
	public void updateIndexForSaveImageSet(
			String[] imageIds,
			String purchaseProductId,
			String userActionHistoryId,
			String productActionHistoryId);

	/**
	 * 画像セット更新に伴うインデックス更新を行います。
	 * @param imageSetId 画像セットID
	 * @param imageId 画像ID
	 * @param nextListViewImageId 次に一覧表示される画像ID
	 * @param delete 完全に削除されたかどうか
	 */
	public void updateIndexForUpdateImageSet(
			String imageSetId,
			String imageId,
			String thumbnailImageId,
			String nextListViewImageId,
			Boolean delete);

	/**
	 * お知らせ更新に伴うインデックス更新を行います。
	 * @param informationIds お知らせIDリスト
	 */
	public void updateIndexForUpdateInformation(
			List<String> informationIds);

	/**
	 * 購入商品生成に伴うインデックス更新を行います。
	 * @param purchaseProductIds 購入商品IDリスト
	 */
	public void updateIndexForCreatePurchaseProduct(
			String... purchaseProductIds);

	/**
	 * 違反報告の更新に伴うインデックス更新を行います。
	 * @param spamReportId 違反報告ID
	 */
	public void updateIndexForSaveSpamReport(
			String spamReportId);

	/**
	 * 指定した更新キーマップに従ってインデックスの同期処理を行います。
	 * @param communityUserId コミュニティユーザーID
	 * @param updateKeyMap 更新対象キーマップ
	 * @param imageUpload 画像アップロード
	 */
	public void syncIndexForCommunityUser(
			String communityUserId,
			HashMap<Class<?>, List<String>> updateKeyMap,
			Boolean imageUpload);
}
