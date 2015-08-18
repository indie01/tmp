/**
 *
 */
package com.kickmogu.yodobashi.community.mapreduce.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;

import org.apache.hadoop.conf.Configuration;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.hadoop.context.DefaultContextLoader;
import org.springframework.hadoop.context.HadoopApplicationContextUtils;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.lib.core.domain.SearchResult;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;

/**
 * 商品マスターランキングを更新するジョブのテストです。
 * @author kamiike
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/mr-context.xml")
public class ProductMasterRankingJobTest extends BaseJobTest {

	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() throws Exception {
		initialize();
		createUser();
		createOrder();
		for (int i = 0; i < 10; i++) {
			createReview();
		}
		createQuestion();
		createQuestionAnswer();
		createImages();
		createLike();
		createComment();
		uploadViewLog();
	}

	@Test
	public void testExecute() throws Exception {
		assertEquals(0, ReviewPostCountJob.execute(applicationContext));

		Configuration conf = new Configuration();
		conf.set(DefaultContextLoader.SPRING_CONFIG_LOCATION, "classpath:/mr-context.xml");
		HadoopApplicationContextUtils.releaseContext(conf);

		assertEquals(0, QuestionAnswerPostCountJob.execute(applicationContext));

		HadoopApplicationContextUtils.releaseContext(conf);

		assertEquals(0, ImagePostCountJob.execute(applicationContext));

		HadoopApplicationContextUtils.releaseContext(conf);

		assertEquals(0, GetLikeCountJob.execute(applicationContext));

		HadoopApplicationContextUtils.releaseContext(conf);

		assertEquals(0, UniqueUserViewLogCountJob.execute(applicationContext));

		HadoopApplicationContextUtils.releaseContext(conf);

		assertEquals(0, UniqueUserViewCountJob.execute(applicationContext));

		HadoopApplicationContextUtils.releaseContext(conf);

		assertEquals(0, ProductMasterRankingJob.execute(applicationContext));

		Integer version = productService.getNextProductMasterVersion(
				).getVersion();
		ProductMasterDO productMaster =
				hBaseOperations.load(ProductMasterDO.class,
						IdUtil.createIdByConcatIds(
				version.toString(),
				product.getSku(),
				communityUser.getCommunityUserId()));
		assertNotNull(productMaster);
		assertEquals(1, productMaster.getRank().intValue());
		assertEquals(true, productMaster.isRequiredNotify());
		assertEquals(10, productMaster.getReviewPostCount());
		assertEquals(1, productMaster.getReviewLikeCount());
		assertEquals(2, productMaster.getReviewShowCount());
		assertEquals(1, productMaster.getAnswerPostCount());
		assertEquals(1, productMaster.getAnswerLikeCount());
		assertEquals(1, productMaster.getImagePostCount());
		assertEquals(1, productMaster.getImageLikeCount());

		BigDecimal expected = new BigDecimal(7).multiply(new BigDecimal("0.3")
				).add((new BigDecimal(2).multiply(new BigDecimal("0.1")
				).add(new BigDecimal(1).multiply(new BigDecimal("0.5")
				).add(new BigDecimal(1).multiply(new BigDecimal("0.3")
				).add(new BigDecimal(1).multiply(new BigDecimal("0.5")
				).add(new BigDecimal(1).multiply(new BigDecimal("0.2")
				).add(new BigDecimal(1).multiply(new BigDecimal("0.3")))))))));
		assertEquals(expected.toString(),
				String.valueOf(productMaster.getProductMasterScore()));

		productService.upgradeProductMasterVersion();

		assertEquals((version + 1), productService.getNextProductMasterVersion(
				).getVersion().intValue());

		assertEquals(0, ProductMasterRankingNotifyJob.execute(applicationContext));

		SearchResult<InformationDO> informations = solrOperations.findByQuery(new SolrQuery(
				"communityUserId_s:" + communityUser.getCommunityUserId() +
				" AND informationType_s:"
				+ InformationType.PRODUCT_MASTER_RANK_CHANGE.getCode()),
				InformationDO.class);
		assertEquals(1, informations.getNumFound().longValue());
		assertEquals(communityUser.getCommunityUserId(),
				informations.getDocuments().get(0).getCommunityUser().getCommunityUserId());
		assertEquals(product.getSku(),
				informations.getDocuments().get(0).getProduct().getSku());

		SearchResult<ActionHistoryDO> actionHistorys = solrOperations.findByQuery(new SolrQuery(
				"communityUserId_s:" + communityUser.getCommunityUserId() +
				" AND actionHistoryType_s:"
				+ ActionHistoryType.USER_PRODUCT_MASTER_RANK_CHANGE.getCode()),
				ActionHistoryDO.class);
		assertEquals(1, actionHistorys.getNumFound().longValue());
		assertEquals(communityUser.getCommunityUserId(),
				actionHistorys.getDocuments().get(0).getCommunityUser().getCommunityUserId());
		assertEquals(product.getSku(),
				actionHistorys.getDocuments().get(0).getProduct().getSku());
	}
}
