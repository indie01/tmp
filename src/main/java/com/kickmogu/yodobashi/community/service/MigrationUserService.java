/**
 *
 */
package com.kickmogu.yodobashi.community.service;

import com.kickmogu.yodobashi.community.resource.domain.MigrationCommunityUserDO;

/**
 * 移行ユーザーサービスです。
 * @author kamiike
 */
public interface MigrationUserService {

	/**
	 * 指定したコミュニティIDに紐付く移行ユーザー情報を返します。
	 * @param communityId コミュニティID
	 * @return 移行ユーザー情報
	 */
	public MigrationCommunityUserDO getMigrationCommunityUserByCommunityId(String communityId);

	/**
	 * コミュニティユーザーのニックネームを更新し、マージ済みとし、
	 * 指定したIC会員の移行ユーザー情報を移行済みに更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param communityName コミュニティ名
	 * @param icOuterCustomerId ICの外部顧客ID
	 */
	public void mergeCommunityName(
			String communityUserId,
			String communityName,
			String icOuterCustomerId,
			boolean agreement);

	/**
	 * 共有化しているIC会員情報で移行待ちのままのレビュー情報が無いかチェックし、
	 * あれば移行します。<br />
	 * また共有化情報の通知処理が実装されるまで、暫定対応としてキャッシュ
	 * された共有化情報との差分を確認し、更新があった場合、注文情報を再度サマリします。
	 * @param communityUserId コミュニティユーザーID
	 */
	public void callCheckAndMigrateReview(String communityUserId);
	public void checkAndMigrateReview(String communityUserId);
}
