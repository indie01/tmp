/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import com.kickmogu.yodobashi.community.resource.domain.DailyScoreFactorDO;

/**
 * 日次スコア要因 DAO です。
 * @author kamiike
 *
 */
public interface DailyScoreFactorDao {

	/**
	 * 日次スコア要因を登録します。
	 * @param factor 日次スコア要因
	 */
	public void createDailyScoreFactor(DailyScoreFactorDO factor);
	
	public void createDailyScoreFactorForBatchBegin(int bulkSize);
	public void createDailyScoreFactorForBatch(DailyScoreFactorDO factor);
	public void createDailyScoreFactorForBatchEnd();

}
