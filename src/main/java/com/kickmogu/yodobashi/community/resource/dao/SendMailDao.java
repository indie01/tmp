/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.MailInfoDO;


/**
 * メール送信用 DAO です。
 * @author kamiike
 *
 */
public interface SendMailDao {

	/**
	 * 送信メールを登録します。
	 * @param mailInfo メール情報
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="テスト対象外")
	public void sendMail(MailInfoDO mailInfo);
}
