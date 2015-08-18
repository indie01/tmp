package com.kickmogu.yodobashi.community.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;
import com.kickmogu.yodobashi.community.resource.domain.constants.LikeTargetType;
import com.kickmogu.yodobashi.community.service.config.ServiceConfig;
import com.kickmogu.yodobashi.community.service.vo.NewsFeedVO;

/**
 * 画像サービスのテストクラスです。
 *
 * @author hirabayashi
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/jcclientContext.xml")
public class LikeServiceTest extends DataSetTest {



	@Autowired
	protected ProductService productService;
	
	@Autowired
	protected ServiceConfig serviceConfig;

	/**
	 * データを初期化します。
	 */
	protected void initialize() {
		super.initialize();
	}

	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() {
		initialize();
	}

	/**
	 * 画像のいいねを検証します。
	 */
	@Test
	public void testUpdateLikeImage() {
		ProductDO useProduct = product3;
		List<ImageHeaderDO> imageHeaders = testUploadImageSet(communityUser, 1, useProduct);
		ImageHeaderDO image = imageHeaders.get(0);
		// 画像のいいねを登録・検証します。
		testUpdateLikeImage(image, false, true); // いいね
		testUpdateLikeImage(image, false, false); // いいね済=>いいね
		testUpdateLikeImage(image, true, true); // いいね済=>いいね解除
		testUpdateLikeImage(image, true, false); // いいね解除=>いいね解除
		// 画像のいいね50回
		createLike50(image.getImageId(), useProduct);
	}

	private void testUpdateLikeImage(ImageHeaderDO image, boolean release, boolean expectation) {
		// いいねを登録します。
		boolean successful  = likeService.updateLikeImage(likeUser.getCommunityUserId(), image.getImageId(), release) > 0;
		assertEquals(expectation, successful);
		// 登録したいいねを取得します。
		Condition path = Path.includeProp("*")
				.includePath("communityUser.communityUserId,imageHeader.imageId").depth(1);
		// いいねをHBaseから取得します。
		List<LikeDO> likesByHBase = hBaseOperations
				.scanWithIndex(LikeDO.class, "relationImageOwnerId", communityUser.getCommunityUserId(),
						hBaseOperations.createFilterBuilder(LikeDO.class)
								.appendSingleColumnValueFilter("communityUserId", CompareOp.EQUAL,
										likeUser.getCommunityUserId()).toFilter(), path);
		// いいねをSolrから取得します。
		StringBuilder buffer = new StringBuilder();
		buffer.append("imageHeaderId_s:");
		buffer.append(image.getImageId());
		buffer.append(" AND communityUserId_s:");
		buffer.append(likeUser.getCommunityUserId());
		SolrQuery query = new SolrQuery(buffer.toString());
		SearchResult<LikeDO> likesBySolr = new SearchResult<LikeDO>(
				solrOperations.findByQuery(query, LikeDO.class, path));
		if(!release) {
			cheackUpdateLikeImage(likesByHBase, image);
			cheackUpdateLikeImage(likesBySolr.getDocuments(), image);
		} else {
			assertEquals(0, likesByHBase.size());
			assertEquals(0, likesBySolr.getDocuments().size());
		}
	}

	private void cheackUpdateLikeImage(List<LikeDO> likes, ImageHeaderDO image) {
		for(LikeDO like : likes) {
			checkLike(like, image);
			// お知らせを取得します。
			List<InformationDO> informationByHBase = getHBaseInformation(communityUser, InformationType.IMAGE_LIKE_ADD);
			SearchResult<InformationDO> informationBySolr = getSolrInformation(communityUser, InformationType.IMAGE_LIKE_ADD);
			assertEquals(1, informationByHBase.size());
			assertEquals(1, informationBySolr.getDocuments().size());
			checkLikeInformation(like, informationByHBase);
			checkLikeInformation(like, informationBySolr.getDocuments());
		}
	}

	private void checkLikeInformation(LikeDO like, List<InformationDO> informations) {
		for(InformationDO information : informations) {
			assertEquals(like.getLikeId(), information.getLike().getLikeId());
			assertEquals(like.getCommunityUser().getCommunityUserId(), information.getRelationLikeOwnerId());
			assertEquals(like.isWithdraw(), information.isWithdraw());
			assertEquals(like.isAdult(), information.isAdult());
			assertTrue(!information.isReadFlag());
			assertNotNull(information.getRegisterDateTime());
		}
	}

	/**
	 * いいねの検証を行います。
	 * @param like
	 */
	private void checkLike(LikeDO like, ImageHeaderDO image) {
		assertEquals(likeUser.getCommunityUserId(), like.getCommunityUser().getCommunityUserId());
		assertEquals(image.getImageId(), like.getImageHeader().getImageId());
		assertEquals(image.getOwnerCommunityUserId(), like.getRelationImageOwnerId());
		assertEquals(image.getProduct().getSku(), like.getSku());
		assertEquals(like.getPostDate(), like.getModifyDateTime());
		assertEquals(LikeTargetType.IMAGE, like.getTargetType());
	}

	/**
	 * いいね50件以上を登録・検証します。
	 *
	 * @param review
	 */
	private void createLike50(String imageId, ProductDO product) {
		CommunityUserDO testLikeUser50 = new CommunityUserDO();
		for(int i=1; i<=serviceConfig.likeThresholdLimit; i++) {
			CommunityUserDO testLikeUser = createCommunityUser("testImageLikeUser" + i, false);
			assertTrue(likeService.updateLikeImage(testLikeUser.getCommunityUserId(), imageId, false) == 1);
			if(i==serviceConfig.likeThresholdLimit){
				testLikeUser50 = testLikeUser;
			}
		}
		// アクションヒストリーをSolrから取得します。
		StringBuilder buffer = new StringBuilder();
		buffer.append("imageHeaderId_s:");
		buffer.append(imageId);
		buffer.append(" AND actionHistoryType_s:");
		buffer.append(ActionHistoryType.LIKE_IMAGE_50.getCode());
		SolrQuery query = new SolrQuery(buffer.toString());
		Condition path = Path.includeProp("*").includePath(
						"communityUser.communityUserId,product.sku," +
						"questionAnswer.questionAnswerId,imageHeader.imageId").depth(2);
		SearchResult<ActionHistoryDO> actionHistorisBySolr = new SearchResult<ActionHistoryDO>(
				solrOperations.findByQuery(query, ActionHistoryDO.class, path));
		ActionHistoryDO actionHistoryBySolr = actionHistorisBySolr.getDocuments().get(0);
		// アクションヒストリーをHBaseから取得します。
		ActionHistoryDO actionHistoryByHBase =
				hBaseOperations.load(ActionHistoryDO.class, actionHistoryBySolr.getActionHistoryId(), path);
		checkLike50(actionHistoryByHBase, imageId, testLikeUser50, product);
		checkLike50(actionHistoryBySolr, imageId, testLikeUser50, product);
	}

	/**
	 * いいね50件以上を検証します。
	 * @param actionHistory
	 * @param review
	 */
	private void checkLike50(ActionHistoryDO actionHistory, String imageId, CommunityUserDO testLikeUser50, ProductDO product) {
		assertEquals(imageId, actionHistory.getImageHeader().getImageId());
		assertEquals(testLikeUser50.getCommunityUserId(), actionHistory.getCommunityUser().getCommunityUserId());
		assertEquals(communityUser.getCommunityUserId(), actionHistory.getRelationImageOwnerId());
		SearchResult<NewsFeedVO> productNewsFeeds = productService.findNewsFeedBySku(product.getSku(), 100, null, false);
		int newsFeedSize = 0;
		for(NewsFeedVO newsFeed : productNewsFeeds.getDocuments()) {
			if(ActionHistoryType.LIKE_IMAGE_50.equals(newsFeed.getActionHistoryType())) {
				newsFeedSize++;
			}
		}
		assertEquals(1, newsFeedSize);
	}

}
