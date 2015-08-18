/**
 * 
 */
package com.kickmogu.yodobashi.community.resource.dao;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;

/**
 * ショートURLを管理するDaoです。
 * @author hirabayashi
 *
 */
public interface ShortUrlDao {
	
	/**
	 * ショートURLに変換します。
	 * @param url ロングURL
	 * @return ショートURL
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="テスト対象外")
	public String convertShortUrl(String url);

}
