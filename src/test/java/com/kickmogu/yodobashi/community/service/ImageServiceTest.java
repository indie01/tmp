package com.kickmogu.yodobashi.community.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.lib.solr.SolrConstants;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageDeleteResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageSyncStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageUploadResult;

/**
 * 画像サービスのテストクラスです。
 *
 * @author hirabayashi
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/jcclientContext.xml")
public class ImageServiceTest extends DataSetTest {

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
	 * 画像同期エラーの復旧処理を検証します。
	 */
	@Test @Ignore
	public void testRecoverImageSyncError() {
		List<ImageHeaderDO> imageHeaders = testUploadImageSet(
				communityUser, 10, product);
		for (int i = 0; i < imageHeaders.size(); i++) {
			ImageHeaderDO imageHeader = imageHeaders.get(i);
			imageHeader.setImageSyncStatus(ImageSyncStatus.ERROR);
			if (i % 2 == 0) {
				imageHeader.setImageUploadResult(ImageUploadResult.PRIMARY_ONLY);
			} else {
				requestScopeDao.initialize(imageHeader.getOwnerCommunityUser(), null);
				imageService.deleteImageInImageSet(imageHeader.getImageId());
				requestScopeDao.destroy();
				imageHeader.setImageDeleteResult(ImageDeleteResult.PRIMARY_ONLY);
				imageHeader.setStatus(ContentsStatus.DELETE);
			}
		}
		hBaseOperations.save(ImageHeaderDO.class, imageHeaders);
		solrOperations.save(ImageHeaderDO.class, imageHeaders);
		assertEquals(imageHeaders.size(),
				solrOperations.count(new SolrQuery(
						"imageSyncStatus_s:" + ImageSyncStatus.ERROR.getCode()),
						ImageHeaderDO.class));
		imageService.recoverImageSyncError();
		assertEquals(0,
				solrOperations.count(new SolrQuery(
						"imageSyncStatus_s:" + ImageSyncStatus.ERROR.getCode()),
						ImageHeaderDO.class));
	}

	/**
	 * 画像の一括投稿を登録・検証します。
	 */
	@Test
	public void testUploadImageSet() {
		ProductDO setProduct = product;
		int imageCount = 10;
		// 画像の登録をします。
		List<ImageHeaderDO> imageHeaders = testUploadImageSet(communityUser, imageCount, setProduct);
		// 画像の検証をします。
		StringBuilder buffer = new StringBuilder();
		buffer.append("imageSetId_s:");
		buffer.append(imageHeaders.get(0).getImageSetId());
		SolrQuery query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT).
				setSortField("imageSetIndex_i", ORDER.asc);
		SearchResult<ImageHeaderDO> imageHeadersBySolr = new SearchResult<ImageHeaderDO>(
				solrOperations.findByQuery(query, ImageHeaderDO.class, imageHeaderPath));
		List<ImageHeaderDO> imageHeadersByHBase = new ArrayList<ImageHeaderDO>();
		for(ImageHeaderDO imageHeader : imageHeaders) {
			ImageHeaderDO imageHeaderByHBase = hBaseOperations.load(ImageHeaderDO.class, imageHeader.getImageId());
			assertEquals(imageHeader.getImageSetIndex(), imageHeaderByHBase.getImageSetIndex());
			imageHeadersByHBase.add(imageHeaderByHBase);
		}
		assertEquals(imageCount, imageHeaders.size());
		assertEquals(imageCount, imageHeadersByHBase.size());
		assertEquals(imageCount, imageHeadersBySolr.getDocuments().size());
		checkTestUploadImageSet(imageHeaders, imageCount, setProduct);
		checkTestUploadImageSet(imageHeadersByHBase, imageCount, setProduct);
		checkTestUploadImageSet(imageHeadersBySolr.getDocuments(), imageCount, setProduct);
	}

	/**
	 * 画像セットの中から指定した画像の削除を検証します。
	 * 先頭の画像を削除
	 */
	@Test
	public void testDeleteImageInImageSet() {
		int imageCount = 10;
		int deleteImageNumber = 0;
		testDeleteImageInImageSet(imageCount, deleteImageNumber);
	}

	/**
	 * 画像セットの中から指定した画像の削除を検証します。
	 * 途中の画像を削除
	 */
	@Test
	public void testDeleteImageInImageSetPatternTwo() {
		int imageCount = 50;
		int deleteImageNumber = 5;
		testDeleteImageInImageSet(imageCount, deleteImageNumber);
	}

	/**
	 * 画像コメントを投稿・検証します。
	 */
	@Test
	public void testUpdateImageComment() {
		String comment = "画像コメントテスト";
		ImageHeaderDO updateImage = createImage();
		testUpdateImageComment(updateImage, comment, 1);
	}

	/**
	 * 画像コメントを投稿・検証します。
	 */
	@Test
	public void testUpdateImageCommentPatternTwo() {
		String comment = "画像コメントテスト";
		ProductDO setProduct = product;
		int imageCount = 10;
		// 画像の登録をします。
		List<ImageHeaderDO> imageHeaders = testUploadImageSet(communityUser, imageCount, setProduct);
		ImageHeaderDO updateImage = imageHeaders.get(2);
		testUpdateImageComment(updateImage, comment, imageCount);
	}

	/**
	 * 画像の仮登録を実行検証します。
	 */
	@Test
	public void testCreateTemporaryImage() {
		ImageDO image = testCreateTemporaryImage(communityUser);
		ImageHeaderDO imageHeaderByHBase = hBaseOperations.load(ImageHeaderDO.class, image.getImageId());
		ImageHeaderDO actionHistoryBySolr = solrOperations.load(
				ImageHeaderDO.class, image.getImageId(), imageHeaderPath);
		assertEquals(null, imageHeaderByHBase);
		assertEquals(null, actionHistoryBySolr);
		ImageDO imageByHBase = hBaseOperations.load(ImageDO.class, image.getImageId());
		assertNotNull(imageByHBase);
		assertNotNull(imageByHBase.getModifyDateTime());
		assertNotNull(imageByHBase.getRegisterDateTime());
		assertNotNull(imageByHBase.getData());
		assertEquals(null, imageByHBase.getImageUrl());
		assertEquals(image.getImageId(), imageByHBase.getImageId());
		assertEquals(image.getCommunityUserId(), imageByHBase.getCommunityUserId());
		assertEquals(image.getImageUrl(), imageByHBase.getImageUrl());
		assertEquals(image.getModifyDateTime(), imageByHBase.getModifyDateTime());
		assertEquals(image.getRegisterDateTime(), imageByHBase.getRegisterDateTime());
		assertEquals(image.getTemporaryKey(), imageByHBase.getTemporaryKey());
		assertEquals(400, imageByHBase.getWidth());
		assertEquals(500, imageByHBase.getHeigth());

	}

	/**
	 * 画像の仮登録をします。
	 * @param communityUser
	 * @return
	 */
	private ImageDO testCreateTemporaryImage(CommunityUserDO communityUser) {
		ImageDO image = new ImageDO();
		image.setData(testImageData);
		image.setMimeType("images/jpeg");
		image.setTemporaryKey("test");
		image.setWidth(400);
		image.setHeigth(500);
		image.setCommunityUserId(communityUser.getCommunityUserId());
		image = imageService.createTemporaryImage(image);
		return image;
	}

	private void testUpdateImageComment(ImageHeaderDO updateImage, String comment, int imageCount) {
		requestScopeDao.initialize(communityUser, null);
		imageService.updateImageComment(
				updateImage.getImageId(), comment);
		requestScopeDao.destroy();

		// 画像セットの確認をします。
		StringBuilder buffer = new StringBuilder();
		buffer.append("imageSetId_s:");
		buffer.append(updateImage.getImageSetId());
		SolrQuery query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT).
				setSortField("imageSetIndex_i", ORDER.asc);
		SearchResult<ImageHeaderDO> imageHeadersBySolr = new SearchResult<ImageHeaderDO>(
				solrOperations.findByQuery(query, ImageHeaderDO.class, imageHeaderPath));
		List<ImageHeaderDO> imageHeadersByHBase = new ArrayList<ImageHeaderDO>();
		for(ImageHeaderDO image : imageHeadersBySolr.getDocuments()) {
			ImageHeaderDO imageHeaderDO = hBaseOperations.load(ImageHeaderDO.class, image.getImageId());
			assertEquals(image.getImageSetIndex(), imageHeaderDO.getImageSetIndex());
			imageHeadersByHBase.add(imageHeaderDO);
		}
		assertEquals(imageCount, imageHeadersByHBase.size());
		assertEquals(imageCount, imageHeadersBySolr.getDocuments().size());
		testUpdateImageComment(imageHeadersByHBase, updateImage, comment);
		testUpdateImageComment(imageHeadersBySolr.getDocuments(), updateImage, comment);
	}

	/**
	 * 画像セットの確認をします。
	 * @param imageHeader
	 * @param updateImage
	 */
	private void testUpdateImageComment(List<ImageHeaderDO> imageHeaders, ImageHeaderDO updateImage, String comment) {
		for(ImageHeaderDO imageHeader : imageHeaders) {
			if(updateImage.getImageId().equals(imageHeader.getImageId())) {
				assertNotNull(imageHeader.getComments());
				assertEquals(updateImage.getPostDate(), imageHeader.getPostDate());
				assertTrue(!imageHeader.getPostDate().equals(imageHeader.getModifyDateTime()));
				assertEquals(comment, imageHeader.getComment());
			} else {
				assertNotNull(imageHeader.getPostDate());
				assertTrue(updateImage.getModifyDateTime().equals(imageHeader.getModifyDateTime()));
			}
		}
	}

	/**
	 * 画像の一括投稿を検証します。
	 * @param imageHeaders
	 * @param imageCount
	 */
	private void checkTestUploadImageSet(List<ImageHeaderDO> imageHeaders, int imageCount, ProductDO product) {
		assertEquals(imageCount, imageHeaders.size());
		String imageSetId = imageHeaders.get(0).getImageSetId();
		int i = 0;
		for(ImageHeaderDO imageHeader : imageHeaders) {
			assertTrue(!imageHeader.isThumbnail());
			assertNotNull(imageHeader.getImageSetId());
			assertEquals(imageHeader.getImageSetId(), imageSetId);
			// 商品
			assertNotNull(imageHeader.getProduct().getSku());
			assertEquals(product.getSku(), imageHeader.getProduct().getSku());
			assertEquals(product.isAdult(), imageHeader.isAdult());
			// 日付
			SearchResult<PurchaseProductDO> purchaseProducts =
					getSolrPurchaseProductsByCommunityUserAndSku(communityUser, product);
			assertTrue(0 != purchaseProducts.getDocuments().size());
			PurchaseProductDO purchaseProduct = purchaseProducts.getDocuments().get(0);
			assertNotNull(imageHeader.getPurchaseDate());
			assertNotNull(purchaseProduct.getPurchaseDate());

			assertEquals(i, imageHeader.getImageSetIndex());
			if(0==imageHeader.getImageSetIndex()) {
				assertTrue(imageHeader.isListViewFlag());
			}
			// サムネイルを取得・検証します。
			ImageHeaderDO actionHistoryByHBase = hBaseOperations.load(
					ImageHeaderDO.class, imageHeader.getThumbnailImageId(), imageHeaderPath);
			ImageHeaderDO actionHistoryBySolr = solrOperations.load(
					ImageHeaderDO.class, imageHeader.getThumbnailImageId(), imageHeaderPath);
			assertTrue(actionHistoryByHBase.isThumbnail());
			assertTrue(actionHistoryBySolr.isThumbnail());
			i++;
		}
		List<ActionHistoryDO> userActionHistoriesByHBase =
				getHBaseActionHistorisByImageSetIdAndActionHistoryType(imageSetId, ActionHistoryType.USER_IMAGE);
		List<ActionHistoryDO> productActionHistoriesByHBase =
				getHBaseActionHistorisByImageSetIdAndActionHistoryType(imageSetId, ActionHistoryType.PRODUCT_IMAGE);
		SearchResult<ActionHistoryDO> userActionHistoriesBySolr =
				getSolrActionHistorisByImageSetIdAndActionHistoryType(imageSetId, ActionHistoryType.USER_IMAGE);
		SearchResult<ActionHistoryDO> productActionHistoriesBySolr =
				getSolrActionHistorisByImageSetIdAndActionHistoryType(imageSetId, ActionHistoryType.PRODUCT_IMAGE);
		checkActionHistorisUploadImageSet(userActionHistoriesByHBase, imageSetId);
		checkActionHistorisUploadImageSet(userActionHistoriesBySolr.getDocuments(), imageSetId);
		checkActionHistorisUploadImageSet(productActionHistoriesByHBase, imageSetId);
		checkActionHistorisUploadImageSet(productActionHistoriesBySolr.getDocuments(), imageSetId);
	}

	/**
	 * 画像セット登録時のアクションヒストリーを検証します。
	 */
	private void checkActionHistorisUploadImageSet(List<ActionHistoryDO> actionHistories, String imageSetId) {
		assertNotNull(actionHistories);
		for(ActionHistoryDO actionHistory : actionHistories) {
			assertNotNull(actionHistory.getCommunityUser().getCommunityUserId());
			assertNotNull(actionHistory.getActionTime());
			assertNotNull(actionHistory.getModifyDateTime());
			assertNotNull(actionHistory.getRegisterDateTime());
			assertNotNull(actionHistory.getImageSetId());
			assertEquals(imageSetId, actionHistory.getImageSetId());
		}
	}

	private void testDeleteImageInImageSet(int imageCount, int deleteImageNumber) {
		List<ImageHeaderDO> imageHeaders = testUploadImageSet(communityUser, imageCount, product);
		ImageHeaderDO deleteImage = imageHeaders.get(deleteImageNumber);
		requestScopeDao.initialize(communityUser, null);
		imageService.deleteImageInImageSet(deleteImage.getImageId());
		requestScopeDao.destroy();

		// イメージの削除の検証
		ImageHeaderDO imageHeaderByHBase = hBaseOperations.load(ImageHeaderDO.class,
				deleteImage.getImageId(), imageHeaderPath);
		ImageHeaderDO imageHeaderBySolr = solrOperations.load(ImageHeaderDO.class,
				deleteImage.getImageId(), imageHeaderPath);
		assertNotNull(imageHeaderByHBase);
		assertNotNull(imageHeaderByHBase.getDeleteDate());
		assertEquals(true, imageHeaderByHBase.isDeleted());
		assertEquals(true, imageHeaderBySolr.isDeleted());

		// サムネイルの削除の検証
		ImageHeaderDO imageHeaderThumbnailByHBase = hBaseOperations.load(ImageHeaderDO.class,
				deleteImage.getImageId(), imageHeaderPath);
		ImageHeaderDO imageHeaderThumbnailBySolr = solrOperations.load(ImageHeaderDO.class,
				deleteImage.getImageId(), imageHeaderPath);
		assertNotNull(imageHeaderThumbnailByHBase);
		assertNotNull(imageHeaderThumbnailByHBase.getDeleteDate());
		assertEquals(true, imageHeaderThumbnailByHBase.isDeleted());
		assertEquals(true, imageHeaderThumbnailBySolr.isDeleted());

		// 残りの画像が削除されていないことを確認します。
		// 画像の検証をします。
		StringBuilder buffer = new StringBuilder();
		buffer.append("imageSetId_s:");
		buffer.append(imageHeaders.get(0).getImageSetId());
		SolrQuery query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT).
				setSortField("imageSetIndex_i", ORDER.asc);
		SearchResult<ImageHeaderDO> imageHeadersBySolr = new SearchResult<ImageHeaderDO>(
				solrOperations.findByQuery(query, ImageHeaderDO.class, imageHeaderPath));
		List<ImageHeaderDO> imageHeadersByHBase = new ArrayList<ImageHeaderDO>();
		for(ImageHeaderDO imageHeader : imageHeadersBySolr.getDocuments()) {
			ImageHeaderDO imageHeaderDO = hBaseOperations.load(ImageHeaderDO.class, imageHeader.getImageId());
			assertEquals(imageHeader.getImageSetIndex(), imageHeaderDO.getImageSetIndex());
			imageHeadersByHBase.add(imageHeaderDO);
		}
		checkRemainImageHeader(imageHeadersByHBase, imageCount, deleteImageNumber);
		checkRemainImageHeader(imageHeadersBySolr.getDocuments(), imageCount, deleteImageNumber);
	}

	/**
	 * 残りの画像が削除されていないことを確認します。
	 * @param imageHeaders
	 * @param imageCount
	 */
	private void checkRemainImageHeader(List<ImageHeaderDO> imageHeaders, int imageCount, int deleteImageNumber) {
		assertEquals(imageCount, imageHeaders.size());
		int loopCount = 0;
		int listViewFlagCount = 0;
		for(ImageHeaderDO imageHeader : imageHeaders) {
			assertTrue(!imageHeader.isWithdraw());
			if(!imageHeader.isDeleted()) {
				assertEquals(null, imageHeader.getDeleteDate());
				if(loopCount==deleteImageNumber) {
					loopCount++;
				}
				assertEquals(loopCount, imageHeader.getImageSetIndex());
				if(imageHeader.isListViewFlag()) {
					if(0==deleteImageNumber) {
						assertEquals(1, imageHeader.getImageSetIndex());
					} else {
						assertEquals(0, imageHeader.getImageSetIndex());
					}
					listViewFlagCount++;
				}
				loopCount++;
			}
		}
		assertEquals(1, listViewFlagCount);
	}

}
