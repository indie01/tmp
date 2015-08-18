/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;


/**
 * アプリケーションロック DAO です。
 * @author kamiike
 *
 */
public interface ApplicationLockDao {

	/**
	 * 質問回答登録処理をロックします。
	 * @param questionId 質問ID
	 * @param communityUserId コミュニティユーザーID
	 */
	public void lockForSaveQuestionAnswer(
			String questionId, String communityUserId);

	/**
	 * 質問登録処理をロックします。
	 * @param sku 商品ID
	 * @param communityUserId コミュニティユーザーID
	 */
	public void lockForSaveQuestion(
			String sku, String communityUserId);

	/**
	 * レビュー登録処理をロックします。
	 * @param sku 商品ID
	 */
	public void lockForSaveReview(
			String sku, String communityUserId);

	/**
	 * 画像削除処理をロックします。
	 * @param imageSetId 画像セットID
	 */
	public void lockForDeleteImageInImageSet(
			String imageSetId, String communityUserId);

	/**
	 * Solr制御処理をロックします。
	 * @param sku 商品ID
	 */
	public void lockForSolrControl(
			Class<?> type);

	/**
	 * Solr制御処理をロックします。
	 * @param sku 商品ID
	 */
	public void unlockForSolrControl(
			Class<?> type);

	public void lockForDeleteImageInPostContentType(String contentId,
			PostContentType postContentType);
}
