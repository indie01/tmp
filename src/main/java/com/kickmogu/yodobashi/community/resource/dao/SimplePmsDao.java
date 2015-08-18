/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.PointGrantEntryDO;
import com.kickmogu.yodobashi.community.resource.domain.PointGrantEntryExecuteStatusDO;
import com.kickmogu.yodobashi.community.resource.domain.PointGrantEntryExecuteStatusResponseDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewPointSpecialConditionValidateDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.CancelPointGrantType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointExchangeType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointGrantEntrySearchType;

/**
 * ポイント管理 DAO です。
 * @author kamiike
 *
 */
public interface SimplePmsDao {

	/**
	 * ポイント付与を申請します。
	 * @param externalCustomerId 外部顧客ID（コミュニティID）
	 * @param pointExchangeType ポイント交換種別（ポイント伝票タイプ）（01:レビュー投稿ポイント　02:ランキングポイント）
	 * @param pointGrantExecStartDate ポイント付与実行開始日（この日付以降でポイント付与実行を可能とする）
	 * @param pointValue ポイント数
	 * @param specialConditionCodes 特別ポイント条件コードリスト
	 * @return ポイント付与申請ID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="テスト対象外")
	public String entryPointGrant(
			String externalCustomerId,
			PointExchangeType pointExchangeType,
			Date pointGrantExecStartDate,
			Long pointValue,
			String[] specialConditionCodes);

	/**
	 * 特別条件ポイントコードの付与を予約します。
	 * @param externalCustomerId 外部顧客ID（コミュニティID）
	 * @param specialConditionCode 特別ポイント条件コード
	 * @return 予約順、取れなかった場合、null
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="テスト対象外")
	public Integer reserveSpecialArrivalPoint(
			String externalCustomerId,
			String specialConditionCode);

	/**
	 * ポイント付与情報を移行します。
	 * @param externalCustomerId 外部顧客ID（コミュニティID）
	 * @param pointExchangeType ポイント交換種別（ポイント伝票タイプ）（01:レビュー投稿ポイント　02:ランキングポイント）
	 * @param pointGrantApprovalDate ポイント承認日時
	 * @param pointGrantDate ポイント付与日付
	 * @param pointValue ポイント数
	 * @return ポイント付与申請ID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="テスト対象外")
	public String migratePointGrant(
			String externalCustomerId,
			PointExchangeType pointExchangeType,
			Date pointGrantApprovalDate,
			Date pointGrantDate,
			Long pointValue);

	/**
	 * ポイント付与申請を取り下げます。
	 * @param pointGrantRequestId ポイント付与申請ID
	 * @param cancelReasonType キャンセル理由タイプ
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="テスト対象外")
	public void cancelPointGrant(
			String pointGrantRequestId,
			CancelPointGrantType cancelReasonType);

	/**
	 * ポイント付与情報を移行をキャンセルします。
	 * @param pointGrantRequestId ポイント付与申請ID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="テスト対象外")
	public void cancelMigratePointGrant(
			String pointGrantRequestId);
	
	/**
	 * ポイント付与申請実行メインタイプが更新可能なポイント付与申請情報を取得します。
	 * @param searchTypes 検索タイプ一覧
	 * @param limit 取得数
	 * @param offset 取得開始位置
	 * @return ポイント付与申請情報一覧
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="テスト対象外")
	public SearchResult<PointGrantEntryDO> findMutablePointGrantEntry(String externalCustomerId, Set<String> pointGrantRequestIds, Set<PointGrantEntrySearchType> searchTypes, Long limit, Long offset);

	/**
	 * 指定のポイント付与申請ID一覧に該当するポイント付与申請情報一覧を取得します。
	 * @param pointGrantRequestIds ポイント付与申請ID一覧
	 * @return ポイント付与申請情報一覧
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="テスト対象外")
	public SearchResult<PointGrantEntryDO> findPointGrantEntry(Set<String> pointGrantRequestIds);
	/**
	 * ポイント付与申請実行メインステータス更新をします。
	 * @param pointGrantExecuteStatusList ステータス更新一覧
	 * @return ステータス更新結果一覧
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="テスト対象外")
	public List<PointGrantEntryExecuteStatusResponseDO> updatePointGrantEntryExecuteStatus(List<PointGrantEntryExecuteStatusDO> changeStatusPointGrantEntries);
	
	/**
	 * 特別条件レビューポイントのチェックを行う。
	 * @param externalCustomerIdClass 外部顧客ID種別
	 * @param externalCustomerId 外部顧客ID
	 * @param specialConditionCodes 特別条件コード一覧
	 * @return 特別レビューポイントチェック一覧情報
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="テスト対象外")
	public List<ReviewPointSpecialConditionValidateDO> confirmReviewPointSpecialCondition(String externalCustomerIdClass, String externalCustomerId, String[] specialConditionCodes);
	
	/**
	 * ポイント付与申請システムを閉塞を解除する
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="テスト対象外")
	public void openService();
	
	/**
	 * ポイント付与申請システムを閉塞する
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="テスト対象外")
	public void closeService();
	
	/**
	 * ポイント付与申請システムの閉塞の状態を取得する。（True：サービス中　False：閉塞中）
	 * @return
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="テスト対象外")
	public Boolean isService();
	
}
