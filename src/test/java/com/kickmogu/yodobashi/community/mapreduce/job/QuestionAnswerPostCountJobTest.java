/**
 *
 */
package com.kickmogu.yodobashi.community.mapreduce.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;

/**
 * 質問回答の投稿件数を商品マスターに記録するジョブのテストです。
 * @author kamiike
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/mr-context.xml")
public class QuestionAnswerPostCountJobTest extends BaseJobTest {

	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() {
		initialize();
		createUser();
		createOrder();
		createQuestion();
		createQuestionAnswer();
	}

	@Test
	public void testExecute() throws Exception {
		assertEquals(0, QuestionAnswerPostCountJob.execute(applicationContext));
		Integer version = productService.getNextProductMasterVersion(
				).getVersion();
		ProductMasterDO productMaster =
				hBaseOperations.load(ProductMasterDO.class,
						IdUtil.createIdByConcatIds(
				version.toString(),
				product.getSku(),
				communityUser.getCommunityUserId()));
		assertNotNull(productMaster);
		assertEquals(1, productMaster.getAnswerPostCount());
	}
}
