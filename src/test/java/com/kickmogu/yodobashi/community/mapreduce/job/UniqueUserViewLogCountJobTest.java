/**
 *
 */
package com.kickmogu.yodobashi.community.mapreduce.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.hadoop.conf.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.hadoop.context.DefaultContextLoader;
import org.springframework.hadoop.context.HadoopApplicationContextUtils;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.UniqueUserViewCountDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.UniqueUserViewCountType;

/**
 * ユニークユーザー閲覧数を記録するジョブのテストです。
 * @author kamiike
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/mr-context.xml")
public class UniqueUserViewLogCountJobTest extends BaseJobTest {

	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() throws Exception {
		initialize();
		createUser();
		createOrder();
		createReview();
		createQuestion();
		createQuestionAnswer();
		createImages();
		uploadViewLog();
	}

	@Test
	public void testExecute() throws Exception {

		assertEquals(0, UniqueUserViewLogCountJob.execute(applicationContext));

		UniqueUserViewCountDO reviewCount =
			hBaseOperations.load(UniqueUserViewCountDO.class,
				IdUtil.createIdByConcatIds(
						UniqueUserViewCountType.REVIEW.getCode(),
						review.getReviewId(), targetTimeString));

		assertNotNull(reviewCount);
		assertEquals(2, reviewCount.getViewCount());
		assertEquals(review.getCommunityUser().getCommunityUserId(),
				reviewCount.getCommunityUserId());
		assertEquals(review.getReviewId(), reviewCount.getContentsId());

		reviewCount =
			solrOperations.load(UniqueUserViewCountDO.class,
				IdUtil.createIdByConcatIds(
						UniqueUserViewCountType.REVIEW.getCode(),
						review.getReviewId(), targetTimeString));

		assertNotNull(reviewCount);
		assertEquals(2, reviewCount.getViewCount());
		assertEquals(review.getCommunityUser().getCommunityUserId(),
				reviewCount.getCommunityUserId());
		assertEquals(review.getReviewId(), reviewCount.getContentsId());

		UniqueUserViewCountDO questionCount =
			hBaseOperations.load(UniqueUserViewCountDO.class,
				IdUtil.createIdByConcatIds(
						UniqueUserViewCountType.QUESTION.getCode(),
						question.getQuestionId(), targetTimeString));

		assertNotNull(questionCount);
		assertEquals(2, questionCount.getViewCount());
		assertEquals(question.getCommunityUser().getCommunityUserId(),
				questionCount.getCommunityUserId());
		assertEquals(question.getQuestionId(), questionCount.getContentsId());

		questionCount =
			solrOperations.load(UniqueUserViewCountDO.class,
				IdUtil.createIdByConcatIds(
						UniqueUserViewCountType.QUESTION.getCode(),
						question.getQuestionId(), targetTimeString));

		assertNotNull(questionCount);
		assertEquals(2, questionCount.getViewCount());
		assertEquals(question.getCommunityUser().getCommunityUserId(),
				questionCount.getCommunityUserId());
		assertEquals(question.getQuestionId(), questionCount.getContentsId());

		UniqueUserViewCountDO imageCount =
			hBaseOperations.load(UniqueUserViewCountDO.class,
				IdUtil.createIdByConcatIds(
						UniqueUserViewCountType.IMAGE.getCode(),
						imageHeaders.get(0).getImageSetId(), targetTimeString));

		assertNotNull(imageCount);
		assertEquals(2, imageCount.getViewCount());
		assertEquals(imageHeaders.get(0).getOwnerCommunityUserId(),
				imageCount.getCommunityUserId());
		assertEquals(imageHeaders.get(0).getImageSetId(), imageCount.getContentsId());

		imageCount =
			solrOperations.load(UniqueUserViewCountDO.class,
				IdUtil.createIdByConcatIds(
						UniqueUserViewCountType.IMAGE.getCode(),
						imageHeaders.get(0).getImageSetId(), targetTimeString));

		assertNotNull(imageCount);
		assertEquals(2, imageCount.getViewCount());
		assertEquals(imageHeaders.get(0).getOwnerCommunityUserId(),
				imageCount.getCommunityUserId());
		assertEquals(imageHeaders.get(0).getImageSetId(), imageCount.getContentsId());

		Configuration conf = new Configuration();
		conf.set(DefaultContextLoader.SPRING_CONFIG_LOCATION, "classpath:/mr-context.xml");
		HadoopApplicationContextUtils.releaseContext(conf);

		assertEquals(0, UniqueUserViewCountJob.execute(applicationContext));
		Integer version = productService.getNextProductMasterVersion(
				).getVersion();
		ProductMasterDO productMaster =
				hBaseOperations.load(ProductMasterDO.class,
						IdUtil.createIdByConcatIds(
				version.toString(),
				product.getSku(),
				communityUser.getCommunityUserId()));
		assertNotNull(productMaster);
		assertEquals(2, productMaster.getReviewShowCount());
	}
}
