/**
 *
 */
package com.kickmogu.yodobashi.community.mapreduce.job;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.domain.DailyScoreFactorDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;

/**
 * 質問回答のスコアを更新するジョブのテストです。
 * @author kamiike
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/mr-context.xml")
public class QuestionAnswerScoreUpdateJobTest extends BaseJobTest {

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
		createComment();
	}

	@Test
	public void testExecute() throws Exception {

		assertEquals(0, QuestionAnswerScoreUpdateJob.execute(applicationContext));

		BigDecimal expected = 
				new BigDecimal(1).multiply(new BigDecimal("0.5"))
				.add(new BigDecimal(1).multiply(new BigDecimal("0.2")))
				.add(new BigDecimal(5).multiply(new BigDecimal("0.2")))
				.add(new BigDecimal(20).multiply(new BigDecimal("0.5")));
		QuestionAnswerDO afterQuestionAnswer = hBaseOperations.load(
				QuestionAnswerDO.class, questionAnswer.getQuestionAnswerId());
		assertEquals(expected.toString(),
				String.valueOf(afterQuestionAnswer.getQuestionAnswerScore()));

		afterQuestionAnswer = solrOperations.load(
				QuestionAnswerDO.class, questionAnswer.getQuestionAnswerId());
		assertEquals(expected.toString(),
				String.valueOf(afterQuestionAnswer.getQuestionAnswerScore()));

		List<DailyScoreFactorDO> scoreList
				= hBaseOperations.scanAll(DailyScoreFactorDO.class,
						hBaseOperations.createFilterBuilder(DailyScoreFactorDO.class
						).appendSingleColumnValueFilter("contentsId", CompareOp.EQUAL,
								afterQuestionAnswer.getQuestionAnswerId()).toFilter());
		assertEquals(1, scoreList.size());
		DailyScoreFactorDO factor = scoreList.get(0);
		assertEquals(1, factor.getCommentCount().longValue());
		assertEquals(1, factor.getLikeCount().longValue());
		assertEquals(1, factor.getFollowerCount().longValue());
		assertEquals(1, factor.getElapsedDays().intValue());
		assertEquals(afterQuestionAnswer.getProduct().getSku(), factor.getSku());
	}
}
