/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.util;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.apache.solr.common.util.DateUtil;

/**
 * @author kamiike
 * 
 */
public class SolrUtil {

	/**
	 * 日付の範囲検索条件文字列を返します。
	 * 
	 * @param fieldName
	 *            フィールド名
	 * @param targetDate
	 *            対象日付
	 * @return 日付の範囲検索条件文字列
	 */
	public static String getSolrDateRangeQuery(String fieldName, Date targetDate) {
		Date startTime = DateUtils.truncate(targetDate, Calendar.DATE);
		Date endTime = DateUtils.addDays(startTime, 1);
		StringBuilder buffer = new StringBuilder();
		buffer.append(fieldName);
		buffer.append(":[");
		buffer.append(DateUtil.getThreadLocalDateFormat().format(startTime));
		buffer.append(" TO *] AND ");
		buffer.append(fieldName);
		buffer.append(":{* TO ");
		buffer.append(DateUtil.getThreadLocalDateFormat().format(endTime));
		buffer.append("}");
		return buffer.toString();
	}

	/**
	 * 予約語リストです。
	 */
	private static final String[] SOLR_RESERVES = new String[] { "+", "-",
			"&&", "||", "!", "(", ")", "{", "}", "[", "]", "^", "\"", "~", "*",
			"?", ":" };

	/**
	 * Solr の予約語をエスケープします。
	 * 
	 * @param value
	 *            値
	 * @return エスケープした文字列
	 */
	public static String escape(String value) {
		for (String reserve : SOLR_RESERVES) {
			value = value.replace(reserve, "\\" + reserve);
		}
		return value;
	}

}
