/**
 *
 */
package com.kickmogu.yodobashi.community.mapreduce.job;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;

/**
 * 日次メール送信ジョブのテストです。
 * @author kamiike
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/mr-context.xml")
public class DailyMailJobTest extends BaseJobTest {

	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() {
		initialize();
		createUser();
		createOrder();
		createReview();
		createQuestion();
		createQuestionAnswer();
		createImages();
		createLike();
	}

	@Test
	public void testExecute() throws Exception {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		assertEquals(0, DailyMailJob.execute(applicationContext, formatter.format(new Date())));
	}
}
