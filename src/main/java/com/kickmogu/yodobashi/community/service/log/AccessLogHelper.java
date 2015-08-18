/**
 *
 */
package com.kickmogu.yodobashi.community.service.log;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.domain.constants.AccessUserType;
import com.kickmogu.yodobashi.community.resource.domain.constants.LogGroup;
import com.kickmogu.yodobashi.community.resource.domain.constants.LogType;
import com.kickmogu.yodobashi.community.resource.domain.constants.UniqueUserViewCountType;

/**
 * アクセスログヘルパーです。
 * @author kamiike
 *
 */
public class AccessLogHelper {

	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AccessLogHelper.class);

	/**
	 * ログ記録用のゲスト用ユーザーIDを生成して返します。
	 * @return ゲスト用ユーザーID
	 */
	public static String createGuestUserId() {
		return UUID.randomUUID().toString();
	}

	/**
	 * アクセスログを記録します。
	 * @param logType ログタイプ
	 * @param accessUserType アクセスしたユーザータイプ
	 * @param userAgent ユーザーエージェント
	 * @param userId ユーザーID
	 * @param accessPath アクセスパス
	 * @param referrer リファラ
	 * @param yid yid
	 * @param sortType 表示ソートタイプ
	 * @param displayOrder 表示順
	 */
	public static void saveAccessLog(
			LogType logType,
			AccessUserType accessUserType,
			String userAgent,
			String userId,
			String accessPath,
			String referrer,
			String yid,
			String sortType,
			String displayOrder
			) {
		LOG.info(AccessLogHelper.getAccessLog(logType,
				accessUserType, userAgent,
				userId, accessPath, referrer,
				yid, sortType, displayOrder));
	}

	/**
	 * アクセスログを作成します。
	 * @param logType ログタイプ
	 * @param accessUserType アクセスしたユーザータイプ
	 * @param userAgent ユーザーエージェント
	 * @param userId ユーザーID
	 * @param accessPath アクセスパス
	 * @param referrer リファラ
	 * @param yid yid
	 * @param sortType 表示ソートタイプ
	 * @param displayOrder 表示順
	 * @return ログ
	 */
	public static String getAccessLog(
			LogType logType,
			AccessUserType accessUserType,
			String userAgent,
			String userId,
			String accessPath,
			String referrer,
			String yid,
			String sortType,
			String displayOrder
			) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(LogGroup.ACCESS.getCode());
		buffer.append("\t");
		buffer.append(logType.getCode());
		buffer.append("\t");
		buffer.append(accessUserType.getCode());
		buffer.append("\t");
		buffer.append(userAgent);
		buffer.append("\t");
		buffer.append(userId);
		buffer.append("\t");
		buffer.append(accessPath);
		buffer.append("\t");
		buffer.append(referrer);
		buffer.append("\t");
		buffer.append(yid);
		buffer.append("\t");
		buffer.append(sortType);
		buffer.append("\t");
		buffer.append(displayOrder);
		return buffer.toString();
	}

	/**
	 * レビュー表示ログを記録します。
	 * @param accessUserType アクセスしたユーザータイプ
	 * @param userAgent ユーザーエージェント
	 * @param userId ユーザーID
	 * @param reviewId レビューID
	 * @param sku レビューのSKU
	 * @param reviewerCommunityUserId レビューを投稿したコミュニティユーザーID
	 */
	public static void saveReviewShowLog(
			AccessUserType accessUserType,
			String userAgent,
			String userId,
			String reviewId,
			String sku,
			String reviewerCommunityUserId) {
		if (ResourceConfig.isBot(userAgent)) {
			return;
		}
		LOG.info(AccessLogHelper.getReviewShowLog(
				accessUserType, userAgent, userId,
				reviewId, sku, reviewerCommunityUserId));
	}

	/**
	 * レビュー表示ログを作成します。
	 * @param accessUserType アクセスしたユーザータイプ
	 * @param userAgent ユーザーエージェント
	 * @param userId ユーザーID
	 * @param reviewId レビューID
	 * @param sku レビューのSKU
	 * @param reviewerCommunityUserId レビューを投稿したコミュニティユーザーID
	 * @return ログ
	 */
	public static String getReviewShowLog(
			AccessUserType accessUserType,
			String userAgent,
			String userId,
			String reviewId,
			String sku,
			String reviewerCommunityUserId) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(LogGroup.REVIEW.getCode());
		buffer.append("\t");
		buffer.append(UniqueUserViewCountType.REVIEW.getCode());
		buffer.append("\t");
		buffer.append(accessUserType.getCode());
		buffer.append("\t");
		buffer.append(userId);
		buffer.append("\t");
		buffer.append(reviewId);
		buffer.append("\t");
		buffer.append(sku);
		buffer.append("\t");
		buffer.append(reviewerCommunityUserId);
		return buffer.toString();
	}

	/**
	 * 質問表示ログを記録します。
	 * @param accessUserType アクセスしたユーザータイプ
	 * @param userAgent ユーザーエージェント
	 * @param userId ユーザーID
	 * @param questionId 質問ID
	 * @param sku レビューのSKU
	 * @param questionCommunityUserId 質問を投稿したコミュニティユーザーID
	 */
	public static void saveQuestionShowLog(
			AccessUserType accessUserType,
			String userAgent,
			String userId,
			String questionId,
			String sku,
			String questionCommunityUserId) {
		if (ResourceConfig.isBot(userAgent)) {
			return;
		}
		LOG.info(AccessLogHelper.getQuestionShowLog(
				accessUserType, userAgent, userId,
				questionId, sku, questionCommunityUserId));
	}

	/**
	 * 質問表示ログを作成します。
	 * @param accessUserType アクセスしたユーザータイプ
	 * @param userAgent ユーザーエージェント
	 * @param userId ユーザーID
	 * @param questionId 質問ID
	 * @param sku レビューのSKU
	 * @param questionCommunityUserId 質問を投稿したコミュニティユーザーID
	 * @return ログ
	 */
	public static String getQuestionShowLog(
			AccessUserType accessUserType,
			String userAgent,
			String userId,
			String questionId,
			String sku,
			String questionCommunityUserId) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(LogGroup.QUESTION.getCode());
		buffer.append("\t");
		buffer.append(UniqueUserViewCountType.QUESTION.getCode());
		buffer.append("\t");
		buffer.append(accessUserType.getCode());
		buffer.append("\t");
		buffer.append(userId);
		buffer.append("\t");
		buffer.append(questionId);
		buffer.append("\t");
		buffer.append(sku);
		buffer.append("\t");
		buffer.append(questionCommunityUserId);
		return buffer.toString();
	}

	/**
	 * 画像表示ログを記録します。
	 * @param accessUserType アクセスしたユーザータイプ
	 * @param userAgent ユーザーエージェント
	 * @param userId ユーザーID
	 * @param imageSetId 画像セットID
	 * @param sku レビューのSKU
	 * @param imageCommunityUserId 画像を投稿したコミュニティユーザーID
	 */
	public static void saveImageShowLog(
			AccessUserType accessUserType,
			String userAgent,
			String userId,
			String imageSetId,
			String sku,
			String imageCommunityUserId) {
		if (ResourceConfig.isBot(userAgent)) {
			return;
		}
		LOG.info(AccessLogHelper.getImageShowLog(
				accessUserType, userAgent, userId,
				imageSetId, sku, imageCommunityUserId));
	}

	/**
	 * 画像表示ログを作成します。
	 * @param accessUserType アクセスしたユーザータイプ
	 * @param userAgent ユーザーエージェント
	 * @param userId ユーザーID
	 * @param imageSetId 画像セットID
	 * @param sku レビューのSKU
	 * @param imageCommunityUserId 画像を投稿したコミュニティユーザーID
	 * @return ログ
	 */
	public static String getImageShowLog(
			AccessUserType accessUserType,
			String userAgent,
			String userId,
			String imageSetId,
			String sku,
			String imageCommunityUserId) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(LogGroup.IMAGE.getCode());
		buffer.append("\t");
		buffer.append(UniqueUserViewCountType.IMAGE.getCode());
		buffer.append("\t");
		buffer.append(accessUserType.getCode());
		buffer.append("\t");
		buffer.append(userId);
		buffer.append("\t");
		buffer.append(imageSetId);
		buffer.append("\t");
		buffer.append(sku);
		buffer.append("\t");
		buffer.append(imageCommunityUserId);
		return buffer.toString();
	}
}
