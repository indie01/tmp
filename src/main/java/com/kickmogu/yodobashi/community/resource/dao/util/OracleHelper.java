/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.util;

/**
 * オラクル用のヘルパーです。
 * @author kamiike
 *
 */
public class OracleHelper {

	/**
	 * コンストラクタをカプセル化します。
	 */
	private OracleHelper() {
	}

	/**
	 * Oracle の Limit、Offset 対応SQLを返します。
	 * @param sql SQL
	 * @return 加工したSQL
	 */
	public static String getLimitString(String sql) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("select * from ( select row_.*, rownum rownum_ from (");
		buffer.append(sql);
		buffer.append(" ) row_ ) where rownum_ <= ? and rownum_ > ?");
		return buffer.toString();
	}

}
