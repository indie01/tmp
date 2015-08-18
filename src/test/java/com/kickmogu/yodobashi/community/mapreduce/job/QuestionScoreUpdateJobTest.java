/**
 *
 */
package com.kickmogu.yodobashi.community.mapreduce.job;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.hadoop.context.DefaultContextLoader;
import org.springframework.hadoop.context.HadoopApplicationContextUtils;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.domain.DailyScoreFactorDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;

/**
 * 質問のスコアを更新するジョブのテストです。
 * @author kamiike
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/mr-context.xml")
public class QuestionScoreUpdateJobTest extends BaseJobTest {

	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() throws Exception {
		initialize();
		createUser();
		createOrder();
		createQuestion();
		createQuestionAnswer();
		createLike();
		createFollow();
		uploadViewLog();
	}

	@Test
	public void testExecute() throws Exception {

		assertEquals(0, UniqueUserViewLogCountJob.execute(applicationContext));

		Configuration conf = new Configuration();
		conf.set(DefaultContextLoader.SPRING_CONFIG_LOCATION, "classpath:/mr-context.xml");
		HadoopApplicationContextUtils.releaseContext(conf);

		assertEquals(0, QuestionScoreUpdateJob.execute(applicationContext));

		BigDecimal expected = new BigDecimal(1).multiply(new BigDecimal("0.5")
				).add((new BigDecimal(1).multiply(new BigDecimal("0.4")
				).add(new BigDecimal(1).multiply(new BigDecimal("0.5")
				).add(new BigDecimal(2).multiply(new BigDecimal("0.1"))))));
		QuestionDO afterQuestion = hBaseOperations.load(
				QuestionDO.class, question.getQuestionId());
		assertEquals(expected.toString(),
				String.valueOf(afterQuestion.getQuestionScore()));

		afterQuestion = solrOperations.load(
				QuestionDO.class, question.getQuestionId());
		assertEquals(expected.toString(),
				String.valueOf(afterQuestion.getQuestionScore()));

		List<DailyScoreFactorDO> scoreList
				= hBaseOperations.scanAll(DailyScoreFactorDO.class,
						hBaseOperations.createFilterBuilder(DailyScoreFactorDO.class
						).appendSingleColumnValueFilter("contentsId", CompareOp.EQUAL,
								afterQuestion.getQuestionId()).toFilter());
		assertEquals(1, scoreList.size());
		DailyScoreFactorDO factor = scoreList.get(0);
		assertEquals(1, factor.getAnswerCount().longValue());
		assertEquals(1, factor.getLikeCount().longValue());
		assertEquals(1, factor.getFollowerCount().longValue());
		assertEquals(2, factor.getViewCount().longValue());
		assertEquals(1, factor.getElapsedDays().intValue());
		assertEquals(afterQuestion.getProduct().getSku(), factor.getSku());
	}
}
