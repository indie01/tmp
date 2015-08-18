/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;



/**
 * 標準化文字 DAO です。
 * @author kamiike
 *
 */
public interface NormalizeCharDao {

	/**
	 * 指定したテキストを標準化します。
	 * @param text テキスト
	 * @return 標準化されたテキスト
	 */
	public String normalizeString(String text);

	public String getSpoofingPattern(String text);
	public boolean validateSpoofingPattern(String text, boolean withLock);
}
