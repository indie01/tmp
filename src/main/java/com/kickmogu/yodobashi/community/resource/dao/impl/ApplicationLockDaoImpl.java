/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.yodobashi.community.resource.dao.ApplicationLockDao;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.domain.ApplicationLockDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.LockType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;


/**
 * アプリケーションロック DAO です。
 * @author kamiike
 *
 */
@Service
public class ApplicationLockDaoImpl implements ApplicationLockDao {

	/**
	 * HBaseアクセサです。
	 */
	@Autowired  @Qualifier("default")
	private HBaseOperations hBaseOperations;


	public HBaseOperations gethBaseOperations() {
		return hBaseOperations;
	}

	public void sethBaseOperations(HBaseOperations hBaseOperations) {
		this.hBaseOperations = hBaseOperations;
	}

	/**
	 * 質問回答登録処理をロックします。
	 * @param questionId 質問ID
	 * @param communityUserId コミュニティユーザーID
	 */
	@Override
	public void lockForSaveQuestionAnswer(
			String questionId, String communityUserId) {
		hBaseOperations.lockRow(ApplicationLockDO.class,
				IdUtil.createIdByConcatIds(
						LockType.SAVE_QUESTION_ANSWER.getCode(),
						questionId,
						communityUserId));
	}

	/**
	 * 質問登録処理をロックします。
	 * @param sku 商品ID
	 * @param communityUserId コミュニティユーザーID
	 */
	@Override
	public void lockForSaveQuestion(
			String sku, String communityUserId) {
		hBaseOperations.lockRow(ApplicationLockDO.class,
				IdUtil.createIdByConcatIds(
						LockType.SAVE_QUESTION.getCode(),
						sku,
						communityUserId));
	}

	/**
	 * レビュー登録処理をロックします。
	 * @param sku 商品ID
	 */
	@Override
	public void lockForSaveReview(
			String sku, String communityUserId) {
		hBaseOperations.lockRow(ApplicationLockDO.class,
				IdUtil.createIdByConcatIds(
						LockType.SAVE_REVIEW.getCode(),
						sku,
						communityUserId));
	}

	/**
	 * 画像削除処理をロックします。
	 * @param imageSetId 画像セットID
	 */
	@Override
	public void lockForDeleteImageInImageSet(
			String imageSetId, String communityUserId) {
		hBaseOperations.lockRow(ApplicationLockDO.class,
				IdUtil.createIdByConcatIds(
						LockType.DELETE_IMAGE_IN_SET.getCode(),
						imageSetId,
						communityUserId));
	}

	/**
	 * 画像削除処理をロックします。
	 * @param imageSetId 画像セットID
	 */
	@Override
	public void lockForDeleteImageInPostContentType(
			String contentId, PostContentType postContentType) {
		//TODO LockTypeのDELETE系がImageSetしかないので要確認
		hBaseOperations.lockRow(ApplicationLockDO.class,
				IdUtil.createIdByConcatIds(
						LockType.DELETE_IMAGE_IN_SET.getCode(),
						contentId));
	}

	
	/**
	 * Solr制御処理をロックします。
	 * @param type Schemaの型です
	 */
	@Override
	public void lockForSolrControl(Class<?> type) {
		hBaseOperations.lockRow(ApplicationLockDO.class,
				IdUtil.createIdByConcatIds(
						LockType.SOLR_CONTROL.getCode(),
						type.getSimpleName()));
	}

	@Override
	public void unlockForSolrControl(Class<?> type) {
		hBaseOperations.unlockRow(ApplicationLockDO.class,
				IdUtil.createIdByConcatIds(
						LockType.SOLR_CONTROL.getCode(),
						type.getSimpleName()));
	}
}
