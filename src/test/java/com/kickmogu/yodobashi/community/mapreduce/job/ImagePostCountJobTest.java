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
 * 画像の投稿件数を商品マスターに記録するジョブのテストです。
 * @author kamiike
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/mr-context.xml")
public class ImagePostCountJobTest extends BaseJobTest {

	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() {
		initialize();
		createUser();
		createOrder();
		createImages();
	}

	@Test
	public void testExecute() throws Exception {
		assertEquals(0, ImagePostCountJob.execute(applicationContext));
		Integer version = productService.getNextProductMasterVersion(
				).getVersion();
		ProductMasterDO productMaster =
				hBaseOperations.load(ProductMasterDO.class,
						IdUtil.createIdByConcatIds(
				version.toString(),
				product.getSku(),
				communityUser.getCommunityUserId()));
		assertNotNull(productMaster);
		assertEquals(1, productMaster.getImagePostCount());
		ProductMasterDO productMaster2 =
			hBaseOperations.load(ProductMasterDO.class,
					IdUtil.createIdByConcatIds(
			version.toString(),
			product.getSku(),
			communityUser2.getCommunityUserId()));
		assertNotNull(productMaster2);
		assertEquals(1, productMaster2.getImagePostCount());
	}
}
