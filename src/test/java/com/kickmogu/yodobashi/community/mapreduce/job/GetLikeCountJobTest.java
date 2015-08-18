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
 * いいね件数を商品マスターに記録するジョブのテストです。
 * @author kamiike
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/mr-context.xml")
public class GetLikeCountJobTest extends BaseJobTest {

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
		assertEquals(0, GetLikeCountJob.execute(applicationContext));
		Integer version = productService.getNextProductMasterVersion(
				).getVersion();
		ProductMasterDO productMaster =
				hBaseOperations.load(ProductMasterDO.class,
						IdUtil.createIdByConcatIds(
				version.toString(),
				product.getSku(),
				communityUser.getCommunityUserId()));
		assertNotNull(productMaster);
		assertEquals(1, productMaster.getReviewLikeCount());
		assertEquals(1, productMaster.getAnswerLikeCount());
		assertEquals(1, productMaster.getImageLikeCount());
	}
}
