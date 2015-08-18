/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;


/**
 * フォロー DAO です。
 * @author kamiike
 *
 */
public interface CommunityUserFollowDao {

	/**
	 * コミュニティユーザーIDからフォロワーのコミュニティユーザーを検索して返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return フォロワーのコミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findFollowerCommunityUserByCommunityUserId(
			String communityUserId, String excludeFollowCommunityUserId, int limit, int offset);
	/**
	 * コミュニティユーザーIDからフォローコミュニティユーザーを検索して返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return フォローのコミュニティユーザーのリスト
	 */
	public SearchResult<CommunityUserDO> findFollowCommunityUserByCommunityUserId(
			String communityUserId, String excludeFollowCommunityUserId, int limit, int offset);

	/**
	 * コミュニティユーザーフォロー情報を削除します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followCommunityUserId フォローユーザーのコミュニティユーザーID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.MEDIUM, frequencyComment="フォローの削除の頻度はそんなに多くない")
	public void deleteFollowCommunityUser(
			String communityUserId,
			String followCommunityUserId);

	/**
	 * 指定したフォロワー、フォローユーザーのコミュニティユーザーフォロー情報
	 * が存在するか判定します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followCommunityUserId フォローユーザーのコミュニティユーザーID
	 * @return フォロー済みの場合、true
	 */
	public boolean existsCommunityUserFollow(
			String communityUserId, String followCommunityUserId);

	/**
	 * コミュニティユーザーフォロー情報を新規に作成します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followCommunityUserId フォローユーザーのコミュニティユーザーID
	 * @return コミュニティユーザーフォロー情報
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.HIGH, frequencyComment="ユーザフォローの頻度は多い")
	public CommunityUserFollowDO createCommunityUserFollow(
			String communityUserId,
			String followCommunityUserId);

	/**
	 * コミュニティユーザーのフォロー情報マップを返します。
	 * @param communityUserId 対象となるコミュニティユーザーID
	 * @param communityUserIds コミュニティユーザーIDリスト
	 * @return コミュニティユーザーのフォロー情報マップ
	 */
	public Map<String, Boolean> loadCommunityUserFollowMap(
			String communityUserId, List<String> communityUserIds);

	/**
	 * コミュニティユーザーフォロー情報のインデックスを作成します。
	 * @param communityUserFollowId コミュニティユーザーフォローID
	 * @return 作成した場合、true
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="updateCommunityUserFollowInIndexの内部メソッドなのでテスト対象外")
	public boolean createCommunityUserFollowInIndex(
			String communityUserFollowId);

	/**
	 * コミュニティユーザーフォロー情報のインデックスを更新します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followCommunityUserId フォローユーザーのコミュニティユーザーID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.HIGH, frequencyComment="ユーザフォローの頻度は多い。登録と削除の2パターン")
	public void updateCommunityUserFollowInIndex(
			String communityUserId, String followCommunityUserId);

	/**
	 * フォローしているコミュニティユーザーを
	 * フォロー日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 検索結果
	 */
	public SearchResult<CommunityUserFollowDO> findFollowCommunityUser(
			String communityUserId, int limit, Date offsetTime, boolean previous);

	/**
	 * フォロワーとなっているコミュニティユーザーと最近取ったアクションを
	 * をフォロー日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 検索結果
	 */
	public SearchResult<CommunityUserFollowDO> findFollowerCommunityUser(
			String communityUserId, int limit, Date offsetTime, boolean previous);

	/**
	 * 指定日に指定したコミュニティユーザーをフォローしたユーザーを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param followDate フォロー日付
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return フォローしたユーザーリスト
	 */
	public SearchResult<CommunityUserFollowDO> findNewUserFollow(
			String communityUserId, Date followDate, int limit, int offset);

	/**
	 * コミュニティユーザーのフォローカウントマップを返します。
	 * @param communityUserIds コミュニティユーザーIDリスト
	 * @return コミュニティユーザーのフォロワーカウントマップ
	 */
	public Map<String, Long> loadFollowCountMap(
			List<String> communityUserIds);

	/**
	 * コミュニティユーザーIDからフォロワーのコミュニティユーザーIDを検索して返します。<br />
	 * 一時停止フラグを無視し、フォロー日時の昇順で返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param asc 昇順ソート
	 * @return フォロワーのコミュニティユーザーIDのリスト
	 */
	public SearchResult<String> findFollowerCommunityUserIdForIndex(
			String communityUserId, int limit, int offset);

	/**
	 * 指定のコミュニティユーザーIDのフォローユーザー数を返します。
	 * @param communityUserId
	 * @return フォローユーザー数
	 */
	public long countFollowCommunityUser(String communityUserId);
	
	
	/**
	 * 指定のコミュニティユーザーIDのフォロワーユーザー数を返します。
	 * @param communityUserId
	 * @return フォロワーユーザー数
	 */
	public long countFollowerCommunityUser(String communityUserId);
	
	
}
