/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;

import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.SpoofingNameDO;
import com.kickmogu.yodobashi.community.resource.domain.StoppableContents;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;

/**
 * コミュニティユーザー DAO です。
 * @author kamiike
 *
 */
public interface CommunityUserDao {

	/**
	 * コミュニティユーザーIDを生成します。
	 * @return コミュニティユーザーID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="IDを払い出すだけなのでテスト対象外")
	public String issueCommunityUserId();

	/**
	 * 指定したコミュニティユーザーIDのコミュニティユーザー情報を取得します。
	 * @param hashCommunityId ハッシュ化されたコミュニティID
	 * @param statusSync ステータス情報を同期するかどうか
	 * @param withLock ロックを取得するかどうか
	 * @param path 取得情報
	 * @return コミュニティユーザー情報
	 */
	public CommunityUserDO loadByHashCommunityId(
			String hashCommunityId, Condition path, boolean withLock,
			boolean statusSync);

	/**
	 * 指定した標準化されたコミュニティ名のコミュニティユーザーIDを返します。
	 * @param normalizeCommunityName 標準化されたコミュニティ名
	 * @return コミュニティユーザーID
	 */
	public String loadCommunityUserIdByNormalizeCommunityName(String normalizeCommunityName);

	/**
	 * コミュニティユーザーを新規登録します。
	 * @param communityUser コミュニティユーザー
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="コミュニティユーザ作成は稀")
	public void createCommunityUser(CommunityUserDO communityUser, SpoofingNameDO spoofingName);

	/**
	 * 自分以外の登録者で、標準化されたニックネームが存在するかどうかチェックします。
	 * @param communityUserId コミュニティユーザー情報ID
	 * @param communityName ニックネーム
	 * @param normalizeCommunityName 標準化されたニックネーム
	 * @param withLock ロックを取得するかどうか
	 * @return ニックネームが存在する場合、true
	 */
	public boolean existsNormalizeCommunityName(
			String communityUserId,
			String communityName,
			String normalizeCommunityName,
			boolean withLock);

	/**
	 * 自分以外の登録者で、標準化されたニックネームが存在するかどうかチェックします。
	 * @param icOuterCustomerId IC外部顧客ID
	 * @param communityName ニックネーム
	 * @param normalizeCommunityName 標準化されたニックネーム
	 * @param withLock ロックを取得するかどうか
	 * @return ニックネームが存在する場合、true
	 */
	public boolean existsNormalizeCommunityNameForCreate(
			String icOuterCustomerId,
			String communityName,
			String normalizeCommunityName,
			boolean withLock);

	/**
	 * 指定したIDのコミュニティユーザー情報のインデックスを更新します。
	 * @param communityUserId コミュニティユーザー情報ID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="コミュニティユーザ登録、更新、削除は稀")
	public void updateCommunityUserInIndex(String communityUserId);

	/**
	 *
	 * @param communityUserIds
	 * @param path
	 * @return
	 */
	public List<CommunityUserDO> find(List<String> communityUserIds, Condition path);

	/**
	 * ニックネームを更新します。
	 * @param communityUser コミュニティユーザー
	 * @param communityNameMergeDone ニックネームマージ済み
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="コミュニティユーザ登録、更新は稀")
	public void updateCommunityName(
			CommunityUserDO communityUser,
			boolean communityNameMergeDone);

	/**
	 * ニックネームを更新します。
	 * @param communityUser コミュニティユーザー
	 * @param spoofingName なりすまし判定
	 * @param communityNameMergeDone ニックネームマージ済み
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="コミュニティユーザ登録、更新は稀")
	public void updateCommunityName(CommunityUserDO communityUser, SpoofingNameDO spoofingName, boolean communityNameMergeDone);

	/**
	 * プロフィール画像を更新します。
	 * @param communityUser コミュニティユーザー
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="コミュニティユーザ登録、更新は稀")
	public void updateProfileImage(
			CommunityUserDO communityUser);

	/**
	 * コミュニティユーザーのステータスを更新します。
	 * @param communityUserId コミュニティユーザー情報ID
	 * @param communityId コミュニティID
	 * @param status ステータス
	 * @param keepContents 任意退会の場合、コンテンツを保持するかの選択
	 * @return 退会した場合、退会キー
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="内部メソッドなのでテスト対象外")
	public String updateCommunityUserStatus(
			String communityUserId,
			String communityId,
			CommunityUserStatus status,
			boolean reviewDelete,
			boolean qaDelete,
			boolean imageDelete,
			boolean commentDelete);

	/**
	 * 設定情報を変更します。
	 * @param communityUser コミュニティユーザー
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="コミュニティユーザ更新は稀")
	public void updateSetting(
			CommunityUserDO communityUser);

	/**
	 * HTTP・HTTPSアクセス制御を変更します。
	 * @param communityUser コミュニティユーザー
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="コミュニティユーザ更新は稀")
	public void updateSecureAccess(
			CommunityUserDO communityUser);

	/**
	 * アダルト商品表示確認ステータスを変更します。
	 * @param communityUser コミュニティユーザー
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="コミュニティユーザ更新は稀")
	public void updateAdultVerification(
			CommunityUserDO communityUser);

	/**
	 * CERO商品表示確認ステータスを変更します。
	 * @param communityUser コミュニティユーザー
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="コミュニティユーザ更新は稀")
	public void updateCeroVerification(
			CommunityUserDO communityUser);

	/**
	 * コミュニティユーザー情報を取得します。
	 * @param communityUserId コミュニティユーザー情報ID
	 * @param path 取得情報
	 * @return コミュニティユーザー情報
	 */
	public CommunityUserDO load(String communityUserId, Condition path);

	/**
	 * コミュニティユーザー情報を取得します。WS専用
	 * @param communityId 外部ID
	 * @param path 取得情報
	 * @return コミュニティユーザー情報
	 */
	public CommunityUserDO loadByCommunityId(String communityId, Condition path);

	/**
	 * コミュニティユーザー情報をインデックスから取得します。
	 * @param communityUserId コミュニティユーザー情報ID
	 * @param path 取得情報
	 * @return コミュニティユーザー情報
	 */
	public CommunityUserDO loadFromIndex(String communityUserId, Condition path);

	/**
	 * コミュニティユーザー情報マップを返します。
	 * @param communityUserIds コミュニティユーザーIDリスト
	 * @return コミュニティユーザー情報マップ
	 */
	public Map<String, CommunityUserDO> loadCommunityUserMap(
			List<String> communityUserIds);

	/**
	 * コミュニティユーザー情報を取得し、ロックします。
	 * @param communityUserId コミュニティユーザー情報ID
	 * @param path 取得情報
	 * @param statusSync ステータス情報を同期するかどうか
	 * @return コミュニティユーザー情報
	 */
	public CommunityUserDO loadWithLock(
			String communityUserId, Condition path, boolean statusSync);

	/**
	 * フォローユーザーを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @return フォローユーザー
	 */
	public SearchResult<CommunityUserFollowDO> findFollowCommunityUserForSuggest(
			String communityUserId);

	/**
	 * 検索条件に合致する全てのCommunityUserDOを取得します。
	 * @param query Solr検索条件
	 * @return CommunityUserDO
	 */
	SearchResult<CommunityUserDO> findCommunityUserByQuery(SolrQuery solrQuery);

	/**
	 * 指定したコミュニティユーザーと共通のフォローユーザーを持った、非フォローユーザー
	 * マップを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param excludeQuery フォローユーザーの除外条件
	 * @param followCommunityUsers フォローユーザーリスト
	 * @return ユーザーマップ
	 */
	public Map<String, Long> loadCommonFollowUserScoresForSuggest(
			String communityUserId,
			String excludeQuery,
			SearchResult<CommunityUserFollowDO> followCommunityUsers);


	/**
	 * 指定したコミュニティユーザーと共通のフォロー商品を持った、非フォローユーザー
	 * マップを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param excludeQuery フォローユーザーの除外条件
	 * @return ユーザーマップ
	 */
	public Map<String, Long> loadCommonFollowProductScoresForSuggest(
			String communityUserId,
			String excludeQuery);

	/**
	 * 指定したコミュニティユーザーと共通のフォロー質問を持った、非フォローユーザー
	 * マップを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param excludeQuery フォローユーザーの除外条件
	 * @return ユーザーマップ
	 */
	public Map<String, Long> loadCommonFollowQuestionScoresForSuggest(
			String communityUserId,
			String excludeQuery);

	/**
	 * 指定したコミュニティユーザーと共通の購入商品を持った、非フォローユーザー
	 * マップを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param excludeQuery フォローユーザーの除外条件
	 * @return ユーザーマップ
	 */
	public Map<String, Long> loadCommonPurchaseProductScoresForSuggest(
			String communityUserId,
			String excludeQuery);

	/**
	 * 指定したニックネームと部分一致するコミュニティユーザーを返します。
	 * @param communityUserId 除外するコミュニティユーザーID（検索ユーザーID）
	 * @param keyword キーワード
	 * @param offsetUserName 前回の検索結果最後の名前
	 * @param limit 最大取得件数
	 * @return コミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findCommunityUserByPartialMatch(
			String communityUserId,
			String keyword,
			String offsetUserName,
			int limit);

	/**
	 * 指定した外部顧客IDに紐づくコミュニティユーザー（外部顧客ID付き）を取得します。
	 * @param outerCustomerId 外部顧客ID
	 * @return コミュニティユーザー（外部顧客ID付き）リスト
	 */
	public List<CommunityUserDO> findCommunityUserWithAccountSharingByOuterCustomerId(
			String outerCustomerId);

	/**
	 * 指定したコミュニティユーザーIDに紐づくコミュニティユーザーIDを取得します。
	 * @param communityUserId コミュニティユーザーID
	 * @return コミュニティユーザーIDリスト
	 */
	public List<String> findCommunityUserIdWithAccountSharingByCommunityUserId(
			String communityUserId);


	/**
	 * 指定した期間に更新のあったコミュニティユーザーIDを返します
	 * @param fromDate 検索開始時間
	 * @param toDate 検索終了時間
	 * @param limit 最大検索数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーIDリスト
	 */
	public SearchResult<CommunityUserDO> findUpdatedCommunityUserByOffsetTime(
			Date fromDate, Date toDate, int limit, int offset);

	/**
	 * 一時停止処理を行います。
	 * @param communityUserId コミュニティユーザーID
	 * @param communityId コミュニティID
	 * @param stop 停止フラグ
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="コミュニティユーザ更新は稀")
	public void updateStop(
			String communityUserId,
			String communityId,
			boolean stop);

	/**
	 * 退会キャンセル処理を行います。
	 * @param communityUserId コミュニティユーザーID
	 * @param communityId コミュニティID
	 * @param withdrawKey 退会キー
	 * @return 更新対象キーマップ
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="コミュニティユーザ更新は稀")
	public HashMap<Class<?>, List<String>> cancelWithdraw(
			String communityUserId,
			String communityId,
			String withdrawKey);

	/**
	 * 退会のための更新、削除処理の第一段階を行います。
	 * @param communityUserId コミュニティユーザーID
	 * @param communityId コミュニティID
	 * @param force 強制退会フラグ
	 * @param keepContents 任意退会の場合、コンテンツを保持するかの選択
	 * @return 退会キー
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="コミュニティユーザ更新は稀")
	public String withdraw(
			String communityUserId,
			String communityId,
			boolean force,
			boolean reviewDelete,
			boolean qaDelete,
			boolean imageDelete,
			boolean commentDelete);

	/**
	 * 退会に伴い、コミュニティユーザーのデータをインデックスと共に削除します。
	 * @param communityUserId コミュニティユーザーID
	 * @param withdrawKey 退会キー
	 * @param force 強制退会フラグ
	 * @param byEcWithdraw EC退会かどうか
	 * @param reviewDelete 自身のレビュー＋自身のレビューに対するコメントを削除する場合、true
	 * @param qaDelete 自身の質問＋自身の回答＋自身の回答に関わるコメントを削除する場合、true
	 * @param imageDelete 自身の投稿画像＋自身の投稿画像に関わるコメントを削除する場合、true
	 * @param commentDelete 自身が投稿した全てのコメントを削除する場合、true
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="コミュニティユーザ更新は稀")
	public void deleteCommunityUserDataForWithdrawWithIndex(
			String communityUserId,
			String withdrawKey,
			boolean force,
			boolean byEcWithdraw,
			boolean reviewDelete,
			boolean qaDelete,
			boolean imageDelete,
			boolean commentDelete);


	public Set<String> getStopCommunityUserIds(List<? extends StoppableContents> contents);

	public Set<String> getStopCommunityUserIds(List<? extends StoppableContents> contents, Set<String> stopCommunityUserIds);

	public CommunityUserDO loadCommunityUserByNormalizeCommunityName(String normalizeCommunityName);

	public void saveCommunityUser(CommunityUserDO communityUser);
}
