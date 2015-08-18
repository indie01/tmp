/**
 *
 */
package com.kickmogu.yodobashi.community.service;

import java.util.Date;

import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.SpamReportDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportStatus;

/**
 * 違反報告サービスです。
 * @author kamiike
 *
 */
public interface SpamReportService {

	/**
	 * 違反報告を登録します。
	 * @param spamReport 違反報告
	 * @return 違反報告
	 */
	public SpamReportDO createSpamReport(SpamReportDO spamReport);

	/**
	 * 違反報告を登録します。
	 * @param spamReport 違反報告
	 * @return 違反報告
	 */
	public SearchResult<SpamReportDO> findSpamReports(Date fromSpamReportDate, Date toSpamReportDate, SpamReportStatus status, int limit, int offset);

	public SpamReportDO updateCheckInitialDate(String spamReportId);

	public SpamReportDO getSpamReportById(String spamReportId);
}
