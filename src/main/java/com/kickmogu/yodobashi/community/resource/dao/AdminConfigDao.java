/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.AdminConfigDO;
import com.kickmogu.yodobashi.community.resource.domain.ScoreFactorDO;

/**
 * 管理者コンフィグ DAO です。
 * @author kamiike
 *
 */
public interface AdminConfigDao {

	/**
	 * コンフィグを返します。
	 * @param key キー
	 * @return コンフィグ
	 */
	public AdminConfigDO loadAdminConfig(String key);
	
	
	public AdminConfigDO loadAdminConfigWithCache(String key);

	/**
	 * コンフィグを保存します。
	 * @param adminConfig コンフィグ
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="テスト対象外")
	public void saveAdminConfig(AdminConfigDO adminConfig);

	/**
	 * スコア係数を返します。
	 * @return スコア係数
	 */
	public ScoreFactorDO loadScoreFactor();

	/**
	 * スコア係数を保存します。
	 * @param scoreFactor スコア係数
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="テスト対象外")
	public void saveScoreFactor(ScoreFactorDO scoreFactor);

}
