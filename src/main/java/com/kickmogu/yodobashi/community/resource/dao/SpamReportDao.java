/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import java.util.Date;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.SpamReportDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportStatus;
/**
 * 違反報告 DAO です。
 * @author kamiike
 *
 */
public interface SpamReportDao {

	/**
	 * 違反報告を登録します。
	 * @param spamReport 違反報告
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="違反報告の登録は頻度は稀")
	public void create(SpamReportDO spamReport);

	/**
	 * 違反報告のインデックスを更新します。
	 * @param spamReportId アクション履歴ID
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.RARE, frequencyComment="違反報告の登録は頻度は稀")
	public void updateSpamReportInIndex(
			String spamReportId);
	
	public SearchResult<SpamReportDO> findSpamReports(Date fromSpamReportDate, Date toSpamReportDate, SpamReportStatus status, int limit, int offset);
	
	public SpamReportDO getSpamReportById(String spamReportId);

	public SpamReportDO updateCheckInitialDate(String spamReportId);
	
	public SpamReportDO loadSpamReport(String spamReportId);
	
	public void saveSpamReport(SpamReportDO spamReport);
	
}
