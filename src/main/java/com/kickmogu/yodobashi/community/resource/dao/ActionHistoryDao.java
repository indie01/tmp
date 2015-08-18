/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import java.util.Date;
import java.util.List;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.VotingDO;

/**
 * アクション履歴 DAO です。
 * @author kamiike
 *
 */
public interface ActionHistoryDao {

	/**
	 * アクション履歴を登録します。
	 * @param actionHistory アクション履歴
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.HIGH, frequencyComment="アクション履歴登録の頻度は高い")
	public void create(ActionHistoryDO actionHistory);
	
	/**
	 * アクション履歴を登録します。
	 * @param actionHistories アクション履歴
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.HIGH, frequencyComment="アクション履歴登録の頻度は高い")
	public void create(List<ActionHistoryDO> actionHistories);

	/**
	 * アクション履歴のインデックスを更新します。
	 * @param actionHistoryId アクション履歴ID
	 * @return アクション履歴
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.HIGH, frequencyComment="アクション履歴登録の頻度は高い")
	public ActionHistoryDO updateActionHistoryInIndex(
			String actionHistoryId);

	/**
	 * アクション履歴のインデックスを更新します。
	 * @param actionHistory アクション履歴
	 * @return アクション履歴
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.HIGH, frequencyComment="アクション履歴登録の頻度は高い")
	public ActionHistoryDO updateActionHistoryInIndex(ActionHistoryDO actionHistory);

	/**
	 * マイルストーンごとのいいね記録を登録すべきか判定します。
	 * @param like いいね
	 * @param threshold 閾値
	 * @return 登録すべきである場合、true
	 */
	public boolean requiredSaveLikeMilstone(LikeDO like, int threshold);

	/**
	 * いいねアクション履歴IDを生成します。
	 * @param like いいね
	 * @param threshold 閾値
	 * @return いいねアクション履歴ID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="IDを払い出すだけなのでテスト対象外")
	public String createLikeActionHistoryId(LikeDO like, int threshold);
	
	/**
	 * マイルストーンごとの参考になった記録を登録すべきか判定します。
	 * @param voting 参考になった
	 * @param threshold 閾値
	 * @return 登録すべきである場合、true
	 */
	public boolean requiredSaveVotingMilstone(VotingDO voting, int threshold);

	/**
	 * 参考になったアクション履歴IDを生成します。
	 * @param voting 参考になった
	 * @param threshold 閾値
	 * @return 参考になったアクション履歴ID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="IDを払い出すだけなのでテスト対象外")
	public String createVotingActionHistoryId(VotingDO voting, int threshold);

	/**
	 * 指定した商品のニュースフィード用アクション履歴をアクション日時順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return ニュースフィード用アクション履歴リスト
	 */
	public SearchResult<ActionHistoryDO> findNewsFeedBySku(
			String sku, int limit, Date offsetTime, boolean previous);

	public SearchResult<ActionHistoryDO> findNewsFeedBySku(
			String sku, int limit, Date offsetTime, boolean previous, boolean excludeProduct);
	
	public SearchResult<ActionHistoryDO> findNewsFeedBySkus(
			List<String> sku, int limit, Date offsetTime, boolean previous);

	public SearchResult<ActionHistoryDO> findNewsFeedBySkus(
			List<String> sku, int limit, Date offsetTime, boolean previous, boolean excludeProduct);

	/**
	 * 指定したコミュニティユーザーのニュースフィード用アクション履歴をアクション日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return ニュースフィード用アクション履歴リスト
	 */
	public SearchResult<ActionHistoryDO> findNewsFeedByCommunityUserId(
			String communityUserId, int limit, Date offsetTime, boolean previous);

	/**
	 * 指定したコミュニティユーザーの投稿系のアクティビティを投稿日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return アクティビティ一覧
	 */
	public SearchResult<ActionHistoryDO> findPrimaryActivityByCommunityUserId(
			String communityUserId,
			int limit,
			Date offsetTime,
			boolean previous);

	/**
	 * 指定したコミュニティユーザーのその他のアクティビティを投稿日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return アクティビティ一覧
	 */
	public SearchResult<ActionHistoryDO> findSecondaryActivityByCommunityUserId(
			String communityUserId,
			int limit,
			Date offsetTime,
			boolean previous);
	
	/**
	 * 指定したコミュニティユーザーのすべてのアクティビティを投稿日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return アクティビティ一覧
	 */
	public SearchResult<ActionHistoryDO> findTimelineActivityByCommunityUserId(
			String communityUserId,
			int limit,
			Date offsetTime,
			boolean previous);
}
