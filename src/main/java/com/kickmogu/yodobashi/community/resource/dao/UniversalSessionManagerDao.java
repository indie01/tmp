package com.kickmogu.yodobashi.community.resource.dao;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;


public interface UniversalSessionManagerDao {

	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="XI通信なのでテスト対象外")
	void deleteUniversalSession(String universalSessionID);

	/**
	 * 指定したユニバーサルセッションに保存した外部顧客IDを取得します。
	 * @param universalSessionID ユニバーサルセッションID
	 * @return 外部顧客ID
	 */
	String loadOuterCustomerId(String universalSessionID);

}
