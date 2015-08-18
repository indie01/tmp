/**
 *
 */
package com.kickmogu.yodobashi.community.mapreduce.job;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.domain.DailyScoreFactorDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;

/**
 * 画像のスコアを更新するジョブのテストです。
 * @author kamiike
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/mr-context.xml")
public class ImageScoreUpdateJobTest extends BaseJobTest {

	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() throws Exception {
		initialize();
		createUser();
		createOrder();
		createImages();
		createLike();
		createComment();
	}

	@Test
	public void testExecute() throws Exception {

		assertEquals(0, ImageScoreUpdateJob.execute(applicationContext));
		ImageHeaderDO imageHeader = imageHeaders.get(0);

		List<DailyScoreFactorDO> scoreList
				= hBaseOperations.scanAll(DailyScoreFactorDO.class,
						hBaseOperations.createFilterBuilder(DailyScoreFactorDO.class
						).appendSingleColumnValueFilter("contentsId", CompareOp.EQUAL,
								imageHeader.getImageId()).toFilter());
		assertEquals(1, scoreList.size());
		DailyScoreFactorDO factor = scoreList.get(0);
		assertEquals(1, factor.getCommentCount().longValue());
		assertEquals(1, factor.getLikeCount().longValue());
		assertEquals(1, factor.getElapsedDays().intValue());
		assertEquals(imageHeader.getProduct().getSku(), factor.getSku());
	}
}
