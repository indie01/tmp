/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.filter.FilterList.Operator;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.lib.core.utils.Asserts;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.UpdateColumns;
import com.kickmogu.lib.solr.FacetResult;
import com.kickmogu.lib.solr.SolrConstants;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.web.common.constants.ImageType;
import com.kickmogu.web.common.util.ImageUtils;
import com.kickmogu.yodobashi.community.common.exception.YcComException;
import com.kickmogu.yodobashi.community.resource.cache.CacheStrategyType;
import com.kickmogu.yodobashi.community.resource.cache.MethodCache;
import com.kickmogu.yodobashi.community.resource.cache.TargetSystemType;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.dao.ImageCacheDao;
import com.kickmogu.yodobashi.community.resource.dao.ImageDao;
import com.kickmogu.yodobashi.community.resource.dao.InformationDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.util.AdultHelper;
import com.kickmogu.yodobashi.community.resource.dao.util.ProductUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.SolrUtil;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.SpamReportDO;
import com.kickmogu.yodobashi.community.resource.domain.VotingDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageDeleteResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageSyncStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageUploadResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.Verification;
import com.kickmogu.yodobashi.community.service.config.ServiceConfig;

/**
 * 画像 DAO の実装です。
 * @author kamiike
 *
 */
@Service
public class ImageDaoImpl implements ImageDao {

	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ImageDaoImpl.class);

	/**
	 * HBaseアクセサです。
	 */
	@Autowired  @Qualifier("default")
	protected HBaseOperations hBaseOperations;

	/**
	 * Solrアクセサです。
	 */
	@Autowired @Qualifier("default")
	protected SolrOperations solrOperations;

	/**
	 * タイムスタンプホルダーです。
	 */
	@Autowired
	protected TimestampHolder timestampHolder;

	/**
	 * 画像キャッシュ DAO です。
	 */
	@Autowired
	protected ImageCacheDao imageCacheDao;

	/**
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	protected RequestScopeDao requestScopeDao;

	/**
	 * リソースコンフィグです。
	 */
	@Autowired
	protected ResourceConfig resourceConfig;

	@Autowired 
	protected InformationDao informationDao;
	
	/**
	 * 商品 DAO です。
	 */
	@Autowired @Qualifier("catalog")
	protected ProductDao productDao;

	
	/**
	 * ランダムインスタンスです。
	 */
	private static Random random;

	static {
		try {
			random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		} catch (Exception e) {
			random = new Random();
		}
	}

	/**
	 * 指定した画像IDの画像を取得します。
	 * @param imageId 画像ID
	 * @param condition 取得条件
	 * @return 画像
	 */
	@Override
	public ImageDO loadImage(String imageId, Condition condition) {
		return hBaseOperations.load(ImageDO.class, imageId, condition);
	}

	@Override
	public List<ImageDO> loadImages(List<String> imageIds, Condition condition) {
		Map<String, ImageDO> imageMap = hBaseOperations.find(ImageDO.class, String.class, imageIds, condition);
		List<ImageDO> result = Lists.newArrayList();
		if( !imageMap.isEmpty() ){
			Iterator<Entry<String, ImageDO>> imageInterator = imageMap.entrySet().iterator();
			Entry<String, ImageDO> entry = null;
			ImageDO image = null;
			while( imageInterator.hasNext() ){
				entry = imageInterator.next();
				image = entry.getValue();
				
				result.add(image);
			}
		}
		return result;
	}

	/**
	 * 指定した画像IDの画像ヘッダーを取得します。
	 * @param imageId 画像ID
	 * @return 画像ヘッダー
	 */
	@Override
	public ImageHeaderDO loadImageHeader(String imageId) {
		return hBaseOperations.load(ImageHeaderDO.class, imageId);
	}

	/**
	 * 指定した画像IDの画像ヘッダーを取得します。
	 * @param imageId 画像ID
	 * @return 画像ヘッダー
	 */
	@Override
	public ImageHeaderDO loadImageHeaderFromIndex(String imageId) {
		return loadImageHeaderFromIndex(imageId, true);
	}

	@Override
	public ImageHeaderDO loadImageHeaderFromIndex(String imageId,
			Boolean includeDeleteContents) {

		StringBuilder buffer = new StringBuilder();
		buffer.append("imageId:");
		buffer.append(imageId);
		if(!includeDeleteContents){
			buffer.append(" AND !status_s:");
			buffer.append(ContentsStatus.DELETE.getCode());
		}

		SearchResult<ImageHeaderDO> results = new SearchResult<ImageHeaderDO>(
				solrOperations.findByQuery(
						new SolrQuery(buffer.toString()),
						ImageHeaderDO.class,
						Path.includeProp("*")
								.includePath(
										"product.sku," + 
										"review.reviewId," +
										"question.questionId," +
										"questionAnswer.questionAnswerId," +
										"ownerCommunityUser.communityUserId").depth(1)));
		ProductUtil.filterInvalidProduct(results);
		if(results == null || results.getDocuments().isEmpty() || results.getDocuments().size() > 1)
			return null;
		return results.getDocuments().get(0);
	}


	/**
	 * 画像を更新します。
	 * @param image 画像
	 * @param condition 更新条件
	 */
	@Override
	public void updateImage(ImageDO image, Condition condition) {
		image.setModifyDateTime(timestampHolder.getTimestamp());

		hBaseOperations.save(image, condition);
	}

	/**
	 * 画像ヘッダーを更新します。
	 * @param imageHeader 画像ヘッダー
	 * @param condition 更新条件
	 */
	@Override
	public void updateImageHeader(
			ImageHeaderDO imageHeader,
			Condition condition) {
		imageHeader.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(imageHeader, condition);
	}

	/**
	 * 画像を一時保存します。
	 * @param image 画像
	 */
	@Override
	public void createTemporaryImage(ImageDO image) {
		image.setImageId(null);
		image.setImageUrl(null);
		image.setRegisterDateTime(timestampHolder.getTimestamp());
		image.setModifyDateTime(timestampHolder.getTimestamp());
		image.setTemporaryFlag(true);

		hBaseOperations.save(image);
	}

	/**
	 * 指定した画像をアップロードします。
	 * @param imageId 画像ID
	 * @return アップロードした場合、true
	 */
	@Override
	public boolean uploadImageForSync(String imageId) {
		ImageHeaderDO imageHeader = hBaseOperations.load(
				ImageHeaderDO.class, imageId);
		if (imageHeader == null || imageHeader.isDeleted()) {
			return false;
		}
		ImageSyncStatus preStatus = imageHeader.getImageSyncStatus();
		uploadImageForSync(imageHeader);
		if (!imageHeader.getImageSyncStatus().equals(preStatus)) {
			imageHeader.setModifyDateTime(timestampHolder.getTimestamp());
			hBaseOperations.save(imageHeader, Path.includeProp(
					"imageSyncStatus,imageUploadResult,imageDeleteResult"));
		}

		return true;
	}

	/**
	 * 指定した画像をパスを変えずにアップロードします。
	 * @param imageHeader 画像ヘッダー
	 */
	private void uploadImageForSync(ImageHeaderDO imageHeader) {
		ImageDO image = hBaseOperations.load(ImageDO.class,imageHeader.getImageId(), Path.includeProp("mimeType,data,width,heigth,communityUserId"));
		String remoteFilePath = imageHeader.getImageUrl().substring(imageHeader.getImageUrl().lastIndexOf(resourceConfig.imageUploadPath));
		int index = remoteFilePath.lastIndexOf("/");
		String remoteDir = remoteFilePath.substring(0, index);
		String remoteFileName = remoteFilePath.substring(index + 1);
		String imageUrl = resourceConfig.imageUrl + remoteDir + "/" + remoteFileName;

		imageHeader.setImageUploadResult(imageCacheDao.upload(image.getData(), remoteDir, remoteFileName));
		if (imageHeader.getImageUploadResult().equals(ImageUploadResult.SUCCESS)) {
			imageHeader.setImageSyncStatus(ImageSyncStatus.SYNC);
		} else {
			imageHeader.setImageSyncStatus(ImageSyncStatus.ERROR);
		}
		LOG.info("image sync upload success. communityUserId=" + imageHeader.getOwnerCommunityUserId()
				+ ", imageId=" + imageHeader.getImageId()
				+ ", imageUrl=" + imageUrl
				+ ", imageSyncStatus=" + imageHeader.getImageSyncStatus());
	}

	/**
	 * 画像をアップロードします。
	 * @param imageHeader 画像ヘッダー
	 * @param createThumbnail サムネイルを作成するかどうか
	 */
	@Override
	public void uploadImage(ImageHeaderDO imageHeader, Boolean createThumbnail) {
		ImageDO image = hBaseOperations.load(ImageDO.class, imageHeader.getImageId(), Path.includeProp("mimeType,data,width,heigth,communityUserId"));
		if (!(PostContentType.PROFILE.equals(imageHeader.getPostContentType()) || 
				PostContentType.PROFILE_THUMBNAIL.equals(imageHeader.getPostContentType())) &&
				!imageHeader.getOwnerCommunityUserId().equals(image.getCommunityUserId())) {
			throw new SecurityException(
					"Different owner. action owner = "
					+ imageHeader.getOwnerCommunityUserId()
					+ ", image owner = "
					+ image.getCommunityUserId());
		}
		String mimeType = image.getMimeType();
		String ext = mimeType.substring(mimeType.lastIndexOf("/") + 1);
		//画像キャッシュアップロード
		String remoteTargetDirectory = (resourceConfig.imageUploadPath
				+ "/" + imageHeader.getPostContentType().getCode() + randomPath()).replace("//", "/");
		String remoteFileName = imageHeader.getImageId() + "." + ext;
		String imageUrl = resourceConfig.imageUrl + remoteTargetDirectory + "/" + remoteFileName;
		imageHeader.setImageUrl(imageUrl);
		imageHeader.setWidth(image.getWidth());
		imageHeader.setHeigth(image.getHeigth());

		imageHeader.setImageUploadResult(imageCacheDao.upload(image.getData(), remoteTargetDirectory, remoteFileName));
		if (ImageUploadResult.SUCCESS.equals(imageHeader.getImageUploadResult())) {
			imageHeader.setImageSyncStatus(ImageSyncStatus.SYNC);
		} else {
			imageHeader.setImageSyncStatus(ImageSyncStatus.ERROR);
		}
		LOG.info("image upload success. communityUserId=" + imageHeader.getOwnerCommunityUserId()
				+ ", imageId=" + imageHeader.getImageId()
				+ ", imageUrl=" + imageUrl
				+ ", imageSyncStatus=" + imageHeader.getImageSyncStatus());
		if (createThumbnail) {
			try {
				ByteArrayOutputStream[] os = ImageUtils.resizeIfNomatchSize(
						ImageUtils.strip(new ByteArrayInputStream(image.getData())),
						ImageType.PNG,
						resourceConfig.thumbnailWidth,
						resourceConfig.thumbnailHeight,
						true);
				ImageDO thumbnailImage = new ImageDO();
				thumbnailImage.setCommunityUserId(image.getCommunityUserId());
				thumbnailImage.setData(os[0].toByteArray());
				thumbnailImage.setMimeType("image/" + ImageType.PNG.getCode());
				createTemporaryImage(thumbnailImage);
				ImageHeaderDO thumbnailImageHeader = new ImageHeaderDO();
				thumbnailImageHeader.setImageId(thumbnailImage.getImageId());
				thumbnailImageHeader.setAdult(imageHeader.isAdult());
				thumbnailImageHeader.setOwnerCommunityUser(imageHeader.getOwnerCommunityUser());
				thumbnailImageHeader.setPostContentType(imageHeader.getPostContentType());
				thumbnailImageHeader.setSku(imageHeader.getSku());
				thumbnailImageHeader.setThumbnail(true);
				uploadImage(thumbnailImageHeader, false);
				imageHeader.setTempThumbnailImage(thumbnailImageHeader);
				imageHeader.setThumbnailImageId(thumbnailImageHeader.getImageId());
				imageHeader.setThumbnailImageUrl(thumbnailImageHeader.getImageUrl());
			} catch (Throwable t) {
				throw new YcComException(t);
			}
		}
	}

	/**
	 * アップロードした画像ヘッダー情報を保存します。
	 * @param imageHeader 画像ヘッダー
	 */
	@Override
	public void saveUploadImageHeader(ImageHeaderDO imageHeader) {
		imageHeader.setPostDate(timestampHolder.getTimestamp());
		imageHeader.setRegisterDateTime(timestampHolder.getTimestamp());
		imageHeader.setModifyDateTime(timestampHolder.getTimestamp());
		imageHeader.setStatus(ContentsStatus.SUBMITTED);
		hBaseOperations.save(imageHeader);

		updateUploaded(imageHeader.getOwnerCommunityUserId(),
				imageHeader.getImageId(),
				imageHeader.getImageUrl());

		if (imageHeader.getTempThumbnailImage() != null) {
			imageHeader.getTempThumbnailImage().setPostDate(timestampHolder.getTimestamp());
			imageHeader.getTempThumbnailImage().setRegisterDateTime(timestampHolder.getTimestamp());
			imageHeader.getTempThumbnailImage().setModifyDateTime(timestampHolder.getTimestamp());
			imageHeader.getTempThumbnailImage().setStatus(ContentsStatus.SUBMITTED);
			hBaseOperations.save(imageHeader.getTempThumbnailImage());

			updateUploaded(imageHeader.getTempThumbnailImage().getOwnerCommunityUserId(),
					imageHeader.getTempThumbnailImage().getImageId(),
					imageHeader.getTempThumbnailImage().getImageUrl());
		}
	}

	/**
	 * 画像を保存しつつ、アップロードします。
	 * @param imageHeader 画像ヘッダー
	 */
	@Override
	public void saveAndUploadImage(
			ImageHeaderDO imageHeader) {
		saveAndUploadImage(imageHeader, false);
	}

	/**
	 * 画像を保存しつつ、アップロードします。
	 * @param imageHeader 画像ヘッダー
	 * @param createThumbnail サムネイルを作成するかどうか
	 */
	@Override
	public void saveAndUploadImage(
			ImageHeaderDO imageHeader, Boolean createThumbnail) {
		if (imageHeader.getImageId() == null) {
			throw new IllegalArgumentException("imageId is null.");
		}
		if (hBaseOperations.load(ImageHeaderDO.class, imageHeader.getImageId(), Path.includeProp("imageId")) != null) {
			throw new IllegalArgumentException("Already registerd. imageId = " + imageHeader.getImageId());
		}
		uploadImage(imageHeader, createThumbnail);
		saveUploadImageHeader(imageHeader);
	}

	@Override
	//@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.INSTANCYSERVICE)
	public void updateImageInIndex(String imageId, Boolean withThumbnail, Boolean mngToolOperation) {
		ImageHeaderDO imageHeader = hBaseOperations.load(ImageHeaderDO.class, imageId);
		if (imageHeader != null &&
				ContentsStatus.SUBMITTED.equals(imageHeader.getStatus()) &&
				!imageHeader.isDeleted()) {
			solrOperations.save(imageHeader);
		} else {
			solrOperations.deleteByQuery(
					new SolrQuery("imageSetId_s:" + SolrUtil.escape(imageId)),
					ActionHistoryDO.class);
			solrOperations.deleteByQuery(
					new SolrQuery("imageHeaderId_s:" + SolrUtil.escape(imageId)),
					InformationDO.class);

			if(!mngToolOperation) {
				solrOperations.deleteByQuery(
						new SolrQuery("imageHeaderId_s:" + SolrUtil.escape(imageId)),
						LikeDO.class);
				solrOperations.deleteByQuery(
						new SolrQuery("imageHeaderId_s:" + SolrUtil.escape(imageId)),
						VotingDO.class);
			}
			solrOperations.save(SpamReportDO.class,
					hBaseOperations.scanWithIndex(
							SpamReportDO.class, "imageHeaderId", imageId));

			if (imageHeader == null) {
				solrOperations.deleteByQuery(
						new SolrQuery("imageHeaderId_s:" + SolrUtil.escape(imageId)),
						CommentDO.class);

				solrOperations.deleteByKey(ImageHeaderDO.class, imageId);
			} else {
				solrOperations.save(CommentDO.class,
						hBaseOperations.scanWithIndex(
								CommentDO.class, "imageHeaderId", imageId));
				solrOperations.save(imageHeader);
			}
		}
		if (imageHeader != null && withThumbnail && StringUtils.isNotEmpty(imageHeader.getThumbnailImageId())) {
			ImageHeaderDO thumbnail = hBaseOperations.load(
					ImageHeaderDO.class, 
					imageHeader.getThumbnailImageId());
			solrOperations.save(thumbnail);
		}
	}

	/**
	 * 一覧表示フラグを更新します。
	 * @param imageId 画像ID
	 * @param listViewFlag 一覧表示フラグ
	 */
	@Override
	//@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.INSTANCYSERVICE)
	public void updateListViewFlag(String imageId, Boolean listViewFlag) {
		ImageHeaderDO imageHeader = hBaseOperations.load(
				ImageHeaderDO.class, imageId);
		imageHeader.setListViewFlag(listViewFlag);
		imageHeader.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(imageHeader,
				Path.includeProp("listViewFlag,modifyDateTime"));
	}
	
	@Override
	//@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.INSTANCYSERVICE)
	public void deleteImageSetIndex(String imageSetId, String thumbnailImageId, Boolean mngToolOperation) {
		solrOperations.deleteByQuery(
				new SolrQuery("imageSetId_s:" + SolrUtil.escape(imageSetId)),
				ActionHistoryDO.class);
		List<ImageHeaderDO> imageHeaders = hBaseOperations.scanWithIndex(
				ImageHeaderDO.class, "imageSetId", imageSetId, Path.includeProp("imageId"));

		if(! mngToolOperation){
			if (imageHeaders.size() > 0) {
				StringBuilder buffer = new StringBuilder();
				for (int i = 0; i < imageHeaders.size(); i++) {
					if (i > 0) {
						buffer.append(" OR ");
					}
					buffer.append("imageHeaderId_s:");
					buffer.append(SolrUtil.escape(imageHeaders.get(i).getImageId()));
				}
				solrOperations.deleteByQuery(new SolrQuery(buffer.toString()),
						InformationDO.class);
			}
			solrOperations.deleteByQuery(
					new SolrQuery("imageSetId_s:" + SolrUtil.escape(imageSetId)),
					LikeDO.class);
			solrOperations.deleteByQuery(
					new SolrQuery("imageSetId_s:" + SolrUtil.escape(imageSetId)),
					VotingDO.class);
			solrOperations.save(CommentDO.class,
					hBaseOperations.scanWithIndex(
							CommentDO.class, "imageSetId", imageSetId));
			solrOperations.save(ImageHeaderDO.class,
					hBaseOperations.scanWithIndex(
							ImageHeaderDO.class, "imageSetId", imageSetId));
			for (ImageHeaderDO imageHeader : imageHeaders) {
				solrOperations.save(SpamReportDO.class,
						hBaseOperations.scanWithIndex(
								SpamReportDO.class, "imageHeaderId", imageHeader.getImageId()));
			}
		}
		if(StringUtils.isNotEmpty(thumbnailImageId)){
			ImageHeaderDO thumbnail = hBaseOperations.load(
					ImageHeaderDO.class, thumbnailImageId);
			solrOperations.save(thumbnail);
		}
	}

	/**
	 * 指定した画像セットのアクション履歴を削除します。
	 * @param imageSetId 画像セットID
	 */
	@Override
	public List<String> deleteImageSetActionHistory(String imageSetId) {
		List<String> deleteActionHistories = new ArrayList<String>();
		// ActionHistory
		for(ActionHistoryDO actionHistory:hBaseOperations.scanWithIndex(ActionHistoryDO.class, "imageSetId", imageSetId)){
			if(actionHistory.isWithdraw() || actionHistory.getImageHeader() != null) continue;
			actionHistory.setDeleteDate(timestampHolder.getTimestamp());
			actionHistory.setDeleteFlag(true);
			actionHistory.setModifyDateTime(timestampHolder.getTimestamp());
			hBaseOperations.save(actionHistory);
			deleteActionHistories.add(actionHistory.getActionHistoryId());
		}
		return deleteActionHistories;
	}

	@Override
	public boolean deleteBothImage(
			PostContentType contentType,
			String contentsId,
			String imageId,
			Boolean logical,
			Boolean mngToolOperation,
			ContentsStatus status) {
		ImageHeaderDO dbImage = hBaseOperations.load(ImageHeaderDO.class, imageId);
		
		if( dbImage == null )
			return false;
		
		if (dbImage.getPostContentType() != contentType)
			return false;
		
		if (dbImage.getPostContentType().equals(PostContentType.REVIEW)) {
			if (!dbImage.getReview().getReviewId().equals(contentsId)) {
				return false;
			}
		} else if (dbImage.getPostContentType().equals(PostContentType.QUESTION)) {
			if (!dbImage.getQuestion().getQuestionId().equals(contentsId)) {
				return false;
			}
		} else if (dbImage.getPostContentType().equals(PostContentType.ANSWER)) {
			if (!dbImage.getQuestionAnswer().getQuestionAnswerId().equals(contentsId)) {
				return false;
			}
		} else if (dbImage.getPostContentType().equals(PostContentType.PROFILE)) {
			if (!dbImage.getOwnerCommunityUserId().equals(contentsId)) {
				return false;
			}
		} else if (dbImage.getPostContentType().equals(PostContentType.PROFILE_THUMBNAIL)) {
			if (!dbImage.getOwnerCommunityUserId().equals(contentsId)) {
				return false;
			}
		} else if (dbImage.getPostContentType().equals(PostContentType.IMAGE_SET)) {
			if (!dbImage.isThumbnail() && !dbImage.getImageSetId().equals(contentsId)) {
				return false;
			}
		}
		//画像キャッシュ削除
		ImageDeleteResult imageDeleteResult = null;
		if (dbImage.getStatus().equals(ContentsStatus.SUBMITTED)) {
			String remoteFilePath = dbImage.getImageUrl().substring(
					dbImage.getImageUrl().lastIndexOf(
							resourceConfig.imageUploadPath));
			int index = remoteFilePath.lastIndexOf("/");
			String remoteDir = remoteFilePath.substring(0, index);
			String remoteFileName = remoteFilePath.substring(index + 1);
			imageDeleteResult = imageCacheDao.delete(remoteDir, remoteFileName);
			
			imageCacheDao.clearCache(dbImage.getImageUrl().substring(resourceConfig.imageUrl.length()));
			LOG.info("image delete success. communityUserId=" + dbImage.getOwnerCommunityUserId()
					+ ", imageId=" + dbImage.getImageId()
					+ ", imageUrl=" + dbImage.getImageUrl());
		}

		if (logical) {
			String updatePath = "status,modifyDateTime,mngToolOperation,listViewFlag";
			
			ImageHeaderDO imageHeader = new ImageHeaderDO();
			imageHeader.setImageId(imageId);
			imageHeader.setListViewFlag(false);
			if(!status.equals(ContentsStatus.CONTENTS_STOP)){
				imageHeader.setDeleteDate(timestampHolder.getTimestamp());
				updatePath += ",deleteDate";
			}else{
				if(!dbImage.getPostContentType().equals(PostContentType.IMAGE_SET)){
					imageHeader.setListViewFlag(dbImage.isListViewFlag());
				}
			}
			imageHeader.setModifyDateTime(timestampHolder.getTimestamp());
			imageHeader.setMngToolOperation(mngToolOperation);
			imageHeader.setStatus(status);
			if (imageDeleteResult != null) {
				imageHeader.setImageDeleteResult(imageDeleteResult);
				if (imageDeleteResult.equals(ImageDeleteResult.SUCCESS)) {
					imageHeader.setImageSyncStatus(ImageSyncStatus.SYNC);
				} else {
					imageHeader.setImageSyncStatus(ImageSyncStatus.ERROR);
				}
				updatePath += ",imageDeleteResult,imageSyncStatus";
				hBaseOperations.save(imageHeader, Path.includeProp(updatePath));
			} else {
				hBaseOperations.save(imageHeader, Path.includeProp(updatePath));
			}

			ImageDO image = new ImageDO();
			image.setImageId(imageId);
			image.setDeleteFlag(true);
			image.setDeleteDate(timestampHolder.getTimestamp());
			image.setModifyDateTime(timestampHolder.getTimestamp());
			hBaseOperations.save(image, Path.includeProp("deleteFlag,deleteDate,modifyDateTime"));

			// コンテンツの一時停止の場合は別途処理するので対象外
			if(!status.equals(ContentsStatus.CONTENTS_STOP)){
				//ActionHistoryDO
				hBaseOperations.scanUpdateWithIndex(
						ActionHistoryDO.class, "imageHeaderId", imageId,
						UpdateColumns.set("deleteFlag", true
								).andSet("deleteDate", timestampHolder.getTimestamp())
								.andSet("modifyDateTime", timestampHolder.getTimestamp()));
	
				if(!mngToolOperation){
					//InformationDO
					hBaseOperations.scanUpdateWithIndex(
							InformationDO.class, "imageHeaderId", imageId,
							UpdateColumns.set("deleteFlag", true
									).andSet("deleteDate", timestampHolder.getTimestamp())
									.andSet("modifyDateTime", timestampHolder.getTimestamp()));
					//CommentDO
					hBaseOperations.scanUpdateWithIndex(
							CommentDO.class, "imageHeaderId", imageId,
							UpdateColumns.set("deleteFlag", true
									).andSet("deleteDate", timestampHolder.getTimestamp())
									.andSet("modifyDateTime", timestampHolder.getTimestamp()));
					//LikeDO
					hBaseOperations.scanDeleteWithIndex(
							LikeDO.class, "imageHeaderId", imageId);
					//VotingDO
					hBaseOperations.scanDeleteWithIndex(
							VotingDO.class, "imageHeaderId", imageId);
					//SpamReportDO
					hBaseOperations.scanUpdateWithIndex(
							SpamReportDO.class, "imageHeaderId", imageId,
							UpdateColumns.set("status", SpamReportStatus.DELETE
									).andSet("deleteDate", timestampHolder.getTimestamp())
									.andSet("modifyDateTime", timestampHolder.getTimestamp()));
				}
			}
		} else {
			hBaseOperations.deleteByKey(ImageHeaderDO.class, imageId);
			hBaseOperations.deleteByKey(ImageDO.class, imageId);
			solrOperations.deleteByKey(ImageHeaderDO.class, imageId);
		}

		return true;
	}

	/**
	 * 物理画像を全て物理削除します。
	 * @param imageIds 画像IDのリスト
	 */
	@Override
	public void deleteImages(List<String> imageIds) {
		hBaseOperations.deleteByKeys(ImageDO.class, String.class, imageIds);
	}

	/**
	 * 指定した質問に紐づく全ての画像情報を返します。
	 * @param questionId 質問ID
	 * @return 画像情報一覧
	 */
	@Override
	public List<ImageHeaderDO> findImageHeaderAllByQuestionId(
			String questionId) {
		List<ImageHeaderDO> imageHeaders = hBaseOperations.scanWithIndex(ImageHeaderDO.class, "questionId", questionId,
				hBaseOperations.createFilterBuilder(ImageHeaderDO.class, Operator.MUST_PASS_ONE
				).includeColumnValues("status", ContentsStatus.SUBMITTED, ContentsStatus.CONTENTS_STOP).toFilter());
		ProductUtil.filterInvalidProduct(imageHeaders);
		return imageHeaders;
	}

	/**
	 * 画像IDのリストを検証します。
	 * @param imageIds 画像IDリスト
	 * @param communityUserId コミュニティユーザーID
	 * @return 検証された画像IDのリスト
	 */
	@Override
	public Set<String> validateImageIds(Set<String> imageIds, String communityUserId) {
		Map<String, ImageDO> resultMap = hBaseOperations.find(
				ImageDO.class,
				String.class,
				imageIds,
				Path.includeProp("imageId,communityUserId"));
		Set<String> result = new HashSet<String>();
		for (ImageDO image : resultMap.values()) {
			if (image.getCommunityUserId().equals(communityUserId)) {
				result.add(image.getImageId());
			}
		}
		return result;
	}

	/**
	 * 指定したタイプのトップ画像マップを返します。
	 * @param postContentType タイプ
	 * @param contentsIds コンテンツIDのリスト
	 * @return トップ画像マップ
	 */
	@Override
	public void loadTopImageMapByContentsIds(List<String> reviewIds, List<String> questionIds, List<String> questionAnswerIds,
			Map<String, ImageHeaderDO> reviewImageMap, Map<String, ImageHeaderDO> questionImageMap, Map<String, ImageHeaderDO> questionAnswerImageMap) {

		if ((reviewIds == null || reviewIds.isEmpty())
			&& (questionIds == null || questionIds.isEmpty())
			&& (questionAnswerIds == null || questionAnswerIds.isEmpty())) {
			return;
		}

		int limitCount = 0;
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND listViewFlag_b:true");
		buffer.append(" AND thumbnail_b:false");
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");

		buffer.append(" AND (");

		boolean isOrQuery = false;

		if(reviewIds != null &&  !reviewIds.isEmpty()){
			buffer.append(" (");
			buffer.append(" (");
			buffer.append(" postContentType_s:");
			buffer.append(SolrUtil.escape(PostContentType.REVIEW.getCode()));
			buffer.append(" ) AND ");
			buffer.append(" (");
			for (int i = 0; i < reviewIds.size(); i++) {
				if (i > 0) {
					buffer.append(" OR ");
				}
				buffer.append(" reviewId_s:");
				buffer.append(SolrUtil.escape(reviewIds.get(i)));
				limitCount++;
			}
			buffer.append(" )");
			buffer.append(" )");
			isOrQuery = true;
		}

		if(questionIds != null &&  !questionIds.isEmpty()){
			if(isOrQuery) buffer.append(" OR");
			buffer.append(" (");
			buffer.append(" (");
			buffer.append(" postContentType_s:");
			buffer.append(SolrUtil.escape(PostContentType.QUESTION.getCode()));
			buffer.append(" ) AND ");
			buffer.append(" (");
			for (int i = 0; i < questionIds.size(); i++) {
				if (i > 0) {
					buffer.append(" OR ");
				}
				buffer.append(" questionId_s:");
				buffer.append(SolrUtil.escape(questionIds.get(i)));
				limitCount++;
			}
			buffer.append(" )");
			buffer.append(" )");
			isOrQuery = true;
		}

		if(questionAnswerIds != null &&  !questionAnswerIds.isEmpty()){
			if(isOrQuery) buffer.append(" OR");
			buffer.append(" (");
			buffer.append(" (");
			buffer.append(" postContentType_s:");
			buffer.append(SolrUtil.escape(PostContentType.ANSWER.getCode()));
			buffer.append(" ) AND ");
			buffer.append(" (");
			for (int i = 0; i < questionAnswerIds.size(); i++) {
				if (i > 0) {
					buffer.append(" OR ");
				}
				buffer.append(" questionAnswerId_s:");
				buffer.append(SolrUtil.escape(questionAnswerIds.get(i)));
				limitCount++;
			}
			buffer.append(" )");
			buffer.append(" )");
		}
		buffer.append(" )");

		for (ImageHeaderDO imageHeader : solrOperations.findByQuery(
				new SolrQuery(buffer.toString()).setRows(limitCount),ImageHeaderDO.class).getDocuments()) {
			if (imageHeader.getPostContentType().equals(PostContentType.REVIEW)) {
				reviewImageMap.put(imageHeader.getReview().getReviewId(), imageHeader);
			} else if (imageHeader.getPostContentType().equals(PostContentType.QUESTION)) {
				questionImageMap.put(imageHeader.getQuestion().getQuestionId(), imageHeader);
			} else if (imageHeader.getPostContentType().equals(PostContentType.ANSWER)) {
				questionAnswerImageMap.put(imageHeader.getQuestionAnswer().getQuestionAnswerId(), imageHeader);
			}
		}
	}
	
	/**
	 * 指定したタイプのすべての画像マップを返します。
	 * @param postContentType タイプ
	 * @param contentsIds コンテンツIDのリスト
	 * @return トップ画像マップ
	 */
	@Override
	public void loadAllImageMapByContentsIds(
			List<String> reviewIds, 
			List<String> questionIds, 
			List<String> questionAnswerIds,
			List<String> imageSetIds,
			Map<String, List<ImageHeaderDO>> reviewImageMap, 
			Map<String, List<ImageHeaderDO>> questionImageMap, 
			Map<String, List<ImageHeaderDO>> questionAnswerImageMap,
			Map<String, List<ImageHeaderDO>> imageSetImageMap) {

		if ((reviewIds == null || reviewIds.isEmpty()) &&
				(questionIds == null || questionIds.isEmpty()) &&
				(questionAnswerIds == null || questionAnswerIds.isEmpty()) &&
				(imageSetIds == null || imageSetIds.isEmpty())) {
			return;
		}
		
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false");
		buffer.append(" AND thumbnail_b:false");
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");

		buffer.append(" AND (");

		boolean isOrQuery = false;
		
		// レビューの画像
		if(reviewIds != null &&  !reviewIds.isEmpty()){
			buffer.append(" (postContentType_s:");
			buffer.append(SolrUtil.escape(PostContentType.REVIEW.getCode()));
			buffer.append(" AND (");
			for (int i = 0; i < reviewIds.size(); i++) {
				if (i > 0) {
					buffer.append(" OR ");
				}
				buffer.append(" reviewId_s:");
				buffer.append(SolrUtil.escape(reviewIds.get(i)));
			}
			buffer.append(" ))");
			isOrQuery = true;
		}
		// 質問の画像
		if(questionIds != null &&  !questionIds.isEmpty()){
			if(isOrQuery) buffer.append(" OR");
			buffer.append(" (postContentType_s:");
			buffer.append(SolrUtil.escape(PostContentType.QUESTION.getCode()));
			buffer.append(" AND (");
			for (int i = 0; i < questionIds.size(); i++) {
				if (i > 0) {
					buffer.append(" OR ");
				}
				buffer.append(" questionId_s:");
				buffer.append(SolrUtil.escape(questionIds.get(i)));
			}
			buffer.append(" ))");
			if( !isOrQuery )
				isOrQuery = true;
		}
		// 回答の画像
		if(questionAnswerIds != null &&  !questionAnswerIds.isEmpty()){
			if(isOrQuery) buffer.append(" OR");
			buffer.append(" (postContentType_s:");
			buffer.append(SolrUtil.escape(PostContentType.ANSWER.getCode()));
			buffer.append(" AND (");
			for (int i = 0; i < questionAnswerIds.size(); i++) {
				if (i > 0) {
					buffer.append(" OR ");
				}
				buffer.append(" questionAnswerId_s:");
				buffer.append(SolrUtil.escape(questionAnswerIds.get(i)));
			}
			buffer.append(" ))");
		}
		
		// 投稿画像の画像
		if(imageSetIds != null &&  !imageSetIds.isEmpty()){
			if(isOrQuery) buffer.append(" OR");
			buffer.append(" (postContentType_s:");
			buffer.append(SolrUtil.escape(PostContentType.IMAGE_SET.getCode()));
			buffer.append(" AND (");
			for (int i = 0; i < imageSetIds.size(); i++) {
				if (i > 0) {
					buffer.append(" OR ");
				}
				buffer.append(" imageSetId_s:");
				buffer.append(SolrUtil.escape(imageSetIds.get(i)));
			}
			buffer.append(" ))");
		}
		
		buffer.append(" )");
		
		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();
		
		for (ImageHeaderDO imageHeader : solrOperations.findByQuery(
				new SolrQuery(buffer.toString())
				.setRows(SolrConstants.QUERY_ROW_LIMIT)
				.addSortField("imageSetIndex_i", ORDER.asc),
				ImageHeaderDO.class).getDocuments()) {
			
			// コンテンツの一時停止対応
			if( ContentsStatus.CONTENTS_STOP.equals(imageHeader.getStatus()) &&
					!imageHeader.getOwnerCommunityUser().getCommunityUserId().equals(loginCommunityUserId)) {
				continue;
			}
			
			List<ImageHeaderDO> reviewImages = null;
			List<ImageHeaderDO> questionImages = null;
			List<ImageHeaderDO> questionAnswerImages = null;
			List<ImageHeaderDO> imageSetImages = null;
			
			if (PostContentType.REVIEW.equals(imageHeader.getPostContentType())) {
				reviewImages = reviewImageMap.get(imageHeader.getReview().getReviewId());
				if( reviewImages == null )
					reviewImages = Lists.newArrayList();
				reviewImages.add(imageHeader);
				reviewImageMap.put(imageHeader.getReview().getReviewId(), reviewImages);
			} else if (PostContentType.QUESTION.equals(imageHeader.getPostContentType())) {
				questionImages = questionImageMap.get(imageHeader.getQuestion().getQuestionId());
				if(questionImages == null)
					questionImages = Lists.newArrayList();
				questionImages.add(imageHeader);
				questionImageMap.put(imageHeader.getQuestion().getQuestionId(), questionImages);
			} else if (PostContentType.ANSWER.equals(imageHeader.getPostContentType())) {
				questionAnswerImages = questionAnswerImageMap.get(imageHeader.getQuestionAnswer().getQuestionAnswerId());
				if(questionAnswerImages == null)
					questionAnswerImages = Lists.newArrayList();
				questionAnswerImages.add(imageHeader);
				questionAnswerImageMap.put(imageHeader.getQuestionAnswer().getQuestionAnswerId(), questionAnswerImages);
			} else if (PostContentType.IMAGE_SET.equals(imageHeader.getPostContentType())) {
				imageSetImages = imageSetImageMap.get(imageHeader.getImageSetId());
				if(imageSetImages == null)
					imageSetImages = Lists.newArrayList();
				imageSetImages.add(imageHeader);
				imageSetImageMap.put(imageHeader.getImageSetId(), imageSetImages);
			}
		}
	}


	/**
	 * 指定したタイプのトップ画像マップを返します。
	 * @param postContentType タイプ
	 * @param contentsIds コンテンツIDのリスト
	 * @return トップ画像マップ
	 */
	@Override
	public Map<String, ImageHeaderDO> loadTopImageMapByContentsIds(
			PostContentType postContentType,
			List<String> contentsIds) {
		Map<String, ImageHeaderDO> resultMap
				= new HashMap<String, ImageHeaderDO>();
		if (contentsIds == null || contentsIds.size() == 0) {
			return resultMap;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND listViewFlag_b:true");
		buffer.append(" AND thumbnail_b:false");
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		buffer.append(" AND postContentType_s:");
		buffer.append(SolrUtil.escape(postContentType.getCode()));
		buffer.append(" AND (");
		for (int i = 0; i < contentsIds.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			if (postContentType.equals(PostContentType.REVIEW)) {
				buffer.append("reviewId_s:");
			} else if (postContentType.equals(PostContentType.QUESTION)) {
				buffer.append("questionId_s:");
			} else if (postContentType.equals(PostContentType.ANSWER)) {
				buffer.append("questionAnswerId_s:");
			} else {
				throw new IllegalArgumentException("This type is unsupported. type = " + postContentType);
			}
			buffer.append(SolrUtil.escape(contentsIds.get(i)));
		}
		buffer.append(")");
		
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(SolrConstants.QUERY_ROW_LIMIT);
		query.setSortField("imageSetIndex_i", ORDER.desc);
		
		for (ImageHeaderDO imageHeader : solrOperations.findByQuery(query, ImageHeaderDO.class).getDocuments()) {
			if (postContentType.equals(PostContentType.REVIEW)) {
				resultMap.put(imageHeader.getReview().getReviewId(), imageHeader);
			} else if (postContentType.equals(PostContentType.QUESTION)) {
				resultMap.put(imageHeader.getQuestion().getQuestionId(), imageHeader);
			} else if (postContentType.equals(PostContentType.ANSWER)) {
				resultMap.put(imageHeader.getQuestionAnswer().getQuestionAnswerId(), imageHeader);
			}
		}
		return resultMap;
	}
	
	/**
	 * 指定したタイプのトップ画像マップを返します。
	 * @param postContentType タイプ
	 * @param contentsIds コンテンツIDのリスト
	 * @return トップ画像マップ
	 */
	public Map<String, List<ImageHeaderDO>> loadAllImageMapByContentsIds(
			PostContentType postContentType,
			List<String> contentsIds){
		Map<String, List<ImageHeaderDO>> resultMap
		= new HashMap<String, List<ImageHeaderDO>>();
		if (contentsIds == null || contentsIds.size() == 0) {
			return resultMap;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false");
		buffer.append(" AND thumbnail_b:false");
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		buffer.append(" AND postContentType_s:");
		buffer.append(SolrUtil.escape(postContentType.getCode()));
		buffer.append(" AND (");
		for (int i = 0; i < contentsIds.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			if (postContentType.equals(PostContentType.REVIEW)) {
				buffer.append("reviewId_s:");
			} else if (postContentType.equals(PostContentType.QUESTION)) {
				buffer.append("questionId_s:");
			} else if (postContentType.equals(PostContentType.ANSWER)) {
				buffer.append("questionAnswerId_s:");
			} else {
				throw new IllegalArgumentException("This type is unsupported. type = "
						+ postContentType);
			}
			buffer.append(SolrUtil.escape(contentsIds.get(i)));
		}
		buffer.append(")");
		
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(SolrConstants.QUERY_ROW_LIMIT);
		query.setSortField("imageSetIndex_i", ORDER.asc);
		
		for (ImageHeaderDO imageHeader : solrOperations.findByQuery(query, ImageHeaderDO.class).getDocuments()) {
			List<ImageHeaderDO> images = null;
			
			if (PostContentType.REVIEW.equals(postContentType)) {
				images = resultMap.get(imageHeader.getReview().getReviewId());
				if( images == null ){
					images = new ArrayList<ImageHeaderDO>();
					resultMap.put(imageHeader.getReview().getReviewId(), images);
				}
			} else if (PostContentType.QUESTION.equals(postContentType)) {
				images = resultMap.get(imageHeader.getQuestion().getQuestionId());
				if( images == null ){
					images = new ArrayList<ImageHeaderDO>();
					resultMap.put(imageHeader.getQuestion().getQuestionId(), images);
				}
			} else if (PostContentType.ANSWER.equals(postContentType)) {
				images = resultMap.get(imageHeader.getQuestionAnswer().getQuestionAnswerId());
				if( images == null ){
					images = new ArrayList<ImageHeaderDO>();
					resultMap.put(imageHeader.getQuestionAnswer().getQuestionAnswerId(), images);
				}
			} else {
				continue;
			}
			
			if( images != null )
				images.add(imageHeader);
		}
		
		return resultMap;
	}
	
	public List<ImageHeaderDO> loadImagesFromIndex(PostContentType postContentType, String contentId) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false");
		buffer.append(" AND thumbnail_b:false");
		buffer.append(" AND ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" AND postContentType_s:");
		buffer.append(SolrUtil.escape(postContentType.getCode()));
		buffer.append(" AND ");
		
		if (postContentType.equals(PostContentType.REVIEW)) {
			buffer.append("reviewId_s:");
		} else if (postContentType.equals(PostContentType.QUESTION)) {
			buffer.append("questionId_s:");
		} else if (postContentType.equals(PostContentType.ANSWER)) {
			buffer.append("questionAnswerId_s:");
		} else if (postContentType.equals(PostContentType.IMAGE_SET)) {
			buffer.append("imageSetId_s:");
		} else {
			throw new IllegalArgumentException("This type is unsupported. type = " + postContentType);
		}
		
		buffer.append(SolrUtil.escape(contentId));
		
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(SolrConstants.QUERY_ROW_LIMIT);
		query.setSortField("imageSetIndex_i", ORDER.asc);
		
		return solrOperations.findByQuery(query, ImageHeaderDO.class).getDocuments();
		
	}
	
	@Override
	public long countImageBySku(String sku){
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND productId_s:" + SolrUtil.escape(sku));
		buffer.append(" AND thumbnail_b:false");
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(0);
		
		SearchResult<ImageHeaderDO> searchResult = new SearchResult<ImageHeaderDO>(
				solrOperations.findByQuery(query, ImageHeaderDO.class,
				Path.includeProp("*").includePath(
						"ownerCommunityUser.communityUserId," +
						"review.reviewId," +
						"question.questionId," +
						"questionAnswer.questionAnswerId"
						).depth(1)));
		
		ProductUtil.filterInvalidProduct(searchResult);

		return searchResult.getNumFound();
	}
	
	@Override
	public long countImageBySkus(List<String> skus){
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false");
		if( skus.size() == 1){
			buffer.append(" AND productId_s:");
			buffer.append(SolrUtil.escape(skus.get(0)));
		}else{
			buffer.append(" AND (");
			for(int i=0; i<skus.size(); i++){
				buffer.append("productId_s:");
				buffer.append(SolrUtil.escape(skus.get(i)));
				if( i != skus.size() - 1 )
					buffer.append(" OR ");
			}
			buffer.append(")"); 
		}
		buffer.append(" AND thumbnail_b:false");
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(0);
		
		SearchResult<ImageHeaderDO> searchResult = new SearchResult<ImageHeaderDO>(
				solrOperations.findByQuery(query, ImageHeaderDO.class,
				Path.includeProp("*").includePath(
						"ownerCommunityUser.communityUserId," +
						"review.reviewId," +
						"question.questionId," +
						"questionAnswer.questionAnswerId"
						).depth(1)));
		
		ProductUtil.filterInvalidProduct(searchResult);

		return searchResult.getNumFound();
	}
	/**
	 * 指定した商品に対して投稿した画像を投稿日時順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param offsetImageIndex 検索画像インデックス（基準が画像セットの場合は必須）
	 * @param all 全てを取得するかどうか
	 * @param previous より前を取得する場合、true
	 * @return 画像一覧
	 */
	@Override
	public SearchResult<ImageHeaderDO> findImagesBySku(
			String sku, 
			int limit, 
			int offset, 
			boolean previous) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("withdraw_b:false AND ");
		sb.append("productId_s:");
		sb.append(SolrUtil.escape(sku));
		sb.append(" AND ( ");
		sb.append("status_s:");
		sb.append(ContentsStatus.SUBMITTED.getCode());
		sb.append(" OR status_s:");
		sb.append(ContentsStatus.CONTENTS_STOP.getCode());
		sb.append(" )");
		sb.append(" AND ");
		sb.append("!postContentType_s:");
		sb.append(PostContentType.PROFILE.getCode());
		sb.append(" AND ");
		sb.append("!postContentType_s:");
		sb.append(PostContentType.PROFILE_THUMBNAIL.getCode());
		sb.append(" AND ");
		sb.append(" thumbnail_b:false");
		
		SolrQuery query = new SolrQuery(sb.toString());
		query.setRows(limit);
		query.setStart(offset);
		
		if (previous) {
			query.setSortField("postDate_dt", ORDER.asc);
			query.addSortField("imageSetIndex_i", ORDER.desc);
		} else {
			
			query.setSortField("postDate_dt", ORDER.desc);
			query.addSortField("imageSetIndex_i", ORDER.asc);
		}
		
		SearchResult<ImageHeaderDO> searchResult = new SearchResult<ImageHeaderDO>(
				solrOperations.findByQuery(
					new SolrQuery(sb.toString())
							.setRows(limit)
							.setStart(offset)
							.addSortField("registerDateTime_dt", ORDER.desc).addSortField("imageSetIndex_i", ORDER.asc),
							ImageHeaderDO.class,
							Path.includeProp("*").includePath(
									"product.sku," +
									"ownerCommunityUser.communityUserId," +
									"review.reviewId," +
									"question.questionId," +
									"questionAnswer.questionAnswerId"
									).depth(1)));
		// Offset分引く
		if( offset > 0)
			searchResult.setNumFound(searchResult.getNumFound() - offset);
		
		ProductUtil.filterInvalidProduct(searchResult);

		ProductUtil.filterInvalidProduct(searchResult);
		if( previous )
			Collections.reverse(searchResult.getDocuments());
		
		return searchResult;
	}
	
	/**
	 * 指定した商品に対して投稿した画像を投稿日時順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param offsetImageIndex 検索画像インデックス（基準が画像セットの場合は必須）
	 * @param all 全てを取得するかどうか
	 * @param previous より前を取得する場合、true
	 * @return 画像一覧
	 */
	@Override
	public SearchResult<ImageHeaderDO> findImagesBySkus(
			List<String> skus,
			int limit,
			int offset,
			boolean previous) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("withdraw_b:false");
		if( skus.size() == 1){
			sb.append(" AND productId_s:");
			sb.append(SolrUtil.escape(skus.get(0)));
		}else{
			sb.append(" AND (");
			for(int i=0; i<skus.size(); i++){
				sb.append("productId_s:");
				sb.append(SolrUtil.escape(skus.get(i)));
				if( i != skus.size() - 1 )
					sb.append(" OR ");
			}
			sb.append(")"); 
		}
		
		sb.append(" AND ( ");
		sb.append("status_s:");
		sb.append(ContentsStatus.SUBMITTED.getCode());
		sb.append(" OR status_s:");
		sb.append(ContentsStatus.CONTENTS_STOP.getCode());
		sb.append(" )");
		sb.append(" AND !postContentType_s:");
		sb.append(PostContentType.PROFILE.getCode());
		sb.append(" AND !postContentType_s:");
		sb.append(PostContentType.PROFILE_THUMBNAIL.getCode());
		sb.append(" AND thumbnail_b:false");
		
		SolrQuery query = new SolrQuery(sb.toString());
		query.setRows(limit);
		query.setStart(offset);
		
		if (previous) {
			query.setSortField("postDate_dt", ORDER.asc);
			query.addSortField("imageSetIndex_i", ORDER.desc);
		} else {
			
			query.setSortField("postDate_dt", ORDER.desc);
			query.addSortField("imageSetIndex_i", ORDER.asc);
		}
		
		SearchResult<ImageHeaderDO> searchResult = new SearchResult<ImageHeaderDO>(
				solrOperations.findByQuery(
					new SolrQuery(sb.toString())
							.setRows(limit)
							.setStart(offset)
							.addSortField("registerDateTime_dt", ORDER.desc).addSortField("imageSetIndex_i", ORDER.asc),
							ImageHeaderDO.class,
							Path.includeProp("*").includePath(
									"product.sku," +
									"ownerCommunityUser.communityUserId," +
									"review.reviewId," +
									"question.questionId," +
									"questionAnswer.questionAnswerId"
									).depth(1)));
		// Offset分引く
		if( offset > 0)
			searchResult.setNumFound(searchResult.getNumFound() - offset);
		
		ProductUtil.filterInvalidProduct(searchResult);

		ProductUtil.filterInvalidProduct(searchResult);
		if( previous )
			Collections.reverse(searchResult.getDocuments());
		
		return searchResult;
	}
	
	@Override
	public SearchResult<ImageHeaderDO> findImagesByCommunityUserId(
			String communityUserId,
			int limit,
			int offset) {
		StringBuilder sb = new StringBuilder();

		sb.append("ownerCommunityUserId_s:");
		sb.append(SolrUtil.escape(communityUserId));
		sb.append(" AND ");
		sb.append("status_s:");
		sb.append(ContentsStatus.SUBMITTED.getCode());
		sb.append(" AND ");
		sb.append("!postContentType_s:");
		sb.append(PostContentType.PROFILE.getCode());
		sb.append(" AND ");
		sb.append("!postContentType_s:");
		sb.append(PostContentType.PROFILE_THUMBNAIL.getCode());
		sb.append(" AND ");
		sb.append("!thumbnail_b:true");
		
		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());
		SolrQuery query = new SolrQuery(
				adultHelper.toFilterQuery(sb.toString()));
		
		SearchResult<ImageHeaderDO> searchResult = new SearchResult<ImageHeaderDO>(
				solrOperations.findByQuery(
						query
							.setRows(limit)
							.setStart(offset)
							.addSortField("registerDateTime_dt", ORDER.desc).addSortField("imageSetIndex_i", ORDER.asc),
							ImageHeaderDO.class,
							Path.includeProp("*").includePath(
									"product.sku," +
									"ownerCommunityUser.communityUserId," +
									"review.reviewId," +
									"question.questionId," +
									"questionAnswer.questionAnswerId"
									).depth(1)));
		// Offset分引く
		if( offset > 0) {
			searchResult.setNumFound(searchResult.getNumFound() - offset);
		}
		
		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(adultHelper.hasAdult(sb.toString(), ImageHeaderDO.class, solrOperations));
		}
		
		ProductUtil.filterInvalidProduct(searchResult);
		
		return searchResult;
	}

	/**
	 * 指定の画像IDから画像情報を取得する。
	 * @param imageId 画像ID
	 * @return 画像情報
	 */
	@Override
	public ImageHeaderDO getImageByImageId(String imageId) {
		StringBuilder sb = new StringBuilder();

		sb.append("imageId:");
		sb.append(SolrUtil.escape(imageId));
		sb.append(" AND ");
		sb.append("status_s:");
		sb.append(ContentsStatus.SUBMITTED.getCode());
		sb.append(" AND ");
		sb.append("!postContentType_s:");
		sb.append(PostContentType.PROFILE.getCode());
		sb.append(" AND ");
		sb.append("!postContentType_s:");
		sb.append(PostContentType.PROFILE_THUMBNAIL.getCode());
		sb.append(" AND ");
		sb.append("!thumbnail_b:true");
		
		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());
		SolrQuery query = new SolrQuery(
				adultHelper.toFilterQuery(sb.toString()));
		
		SearchResult<ImageHeaderDO> searchResult = new SearchResult<ImageHeaderDO>(
				solrOperations.findByQuery(
						
						query,
						ImageHeaderDO.class,
						Path.includeProp("*").includePath(
							"product.sku," +
							"ownerCommunityUser.communityUserId," +
							"review.reviewId," +
							"question.questionId," +
							"questionAnswer.questionAnswerId"
							).depth(1)));
		
		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(adultHelper.hasAdult(sb.toString(), ImageHeaderDO.class, solrOperations));
		}
		
		ProductUtil.filterInvalidProduct(searchResult);
		
		if( searchResult.getDocuments().isEmpty())
			return null;
		
		return searchResult.getDocuments().get(0);
	}
	/**
	 * 指定した期間に更新のあった画像を返します。
	 * @param fromDate 検索開始時間
	 * @param toDate 検索終了時間
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return
	 */
	@Override
	public SearchResult<ImageHeaderDO> findUpdatedImageByOffsetTime(
			Date fromDate, Date toDate, int limit, int offset){
		StringBuilder buffer = new StringBuilder();
		// 有効
		buffer.append(" ( ");
		buffer.append("postDate_dt:{" +
				DateUtil.getThreadLocalDateFormat().format(fromDate) + " TO " + DateUtil.getThreadLocalDateFormat().format(toDate) + "}");
		buffer.append(" AND status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" ) ");
		// 削除
		buffer.append(" OR ( ");
		buffer.append("deleteDate_dt:{" +
				DateUtil.getThreadLocalDateFormat().format(fromDate) + " TO " + DateUtil.getThreadLocalDateFormat().format(toDate) + "}");
		buffer.append(" AND status_s:");
		buffer.append(ContentsStatus.DELETE.getCode());
		buffer.append(" ) ");
		// 一時停止
		buffer.append(" OR ( ");
		buffer.append("modifyDateTime_dt:{" +
				DateUtil.getThreadLocalDateFormat().format(fromDate) + " TO " + DateUtil.getThreadLocalDateFormat().format(toDate) + "}");
		buffer.append(" AND status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		SolrQuery query = new SolrQuery(buffer.toString());
		if (limit > 0)
			query.setRows(limit);
		query.setStart(offset);
		SearchResult<ImageHeaderDO> searchResult = new SearchResult<ImageHeaderDO>(
				solrOperations.findByQuery(query, ImageHeaderDO.class));
		return searchResult;
	}
	
	/**
	 * 指定したユーザーの全ての有効、一時停止画像を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return
	 */
	public SearchResult<ImageHeaderDO> findImageByCommunityUserId(
			String communityUserId, int limit, int offset){
		StringBuilder buffer = new StringBuilder();
		buffer.append("ownerCommunityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		SolrQuery query = new SolrQuery(buffer.toString());
		if (limit > 0)
			query.setRows(limit);
		query.setStart(offset);
		SearchResult<ImageHeaderDO> searchResult = new SearchResult<ImageHeaderDO>(
				solrOperations.findByQuery(query, ImageHeaderDO.class));
		return searchResult;
	}
	
	/**
	 * 指定した商品に対して投稿した画像件数を返します。
	 * @param skus SKUリスト
	 * @return 画像件数リスト
	 */
	@Override
	public Map<String, Long> countImageBySku(
			String[] skus) {
		
		Asserts.isTrue(skus.length > 0);
		
		Map<String, String> imageQueryMap = new HashMap<String,String>();
		SolrQuery solrQuery = new SolrQuery("*:*");
		Map<String, Long> imageCountMap = new HashMap<String, Long>();
		
		for(String sku:skus){
			StringBuilder buffer = new StringBuilder();
			buffer.append("withdraw_b:false AND productId_s:" + SolrUtil.escape(sku));
			buffer.append(" AND thumbnail_b:false");
			buffer.append(" AND status_s:");
			buffer.append(SolrUtil.escape(ContentsStatus.SUBMITTED.getCode()));
			String query = buffer.toString();
			imageQueryMap.put(query, sku);
			solrQuery.addFacetQuery(query);
		}
		solrQuery.setFacetLimit(solrQuery.getFacetQuery().length);
		solrQuery.setFacetMinCount(0);
		
		for (FacetResult<String> facetResult : solrOperations.facet(ImageHeaderDO.class, String.class, solrQuery)) {
			if(imageQueryMap.containsKey(facetResult.getFacetQuery())){
				imageCountMap.put(imageQueryMap.get(facetResult.getFacetQuery()), facetResult.getCount());
			}
		}
		return imageCountMap;
	}
	
	/**
	 * 指定したコミュニティユーザーが投稿した画像を投稿日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @param type 絞込みタイプ
	 * @param アダルト確認フラグ
	 * @return 画像一覧
	 */
	@Override
	public SearchResult<ImageHeaderDO> findImageSetByCommunityUserId(
			String communityUserId, 
			int limit,
			Date offsetTime,
			boolean previous,
			Verification adultVerification) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND ownerCommunityUserId_s:" + SolrUtil.escape(communityUserId));
		buffer.append(" AND listViewFlag_b:true");
		buffer.append(" AND thumbnail_b:false");
		buffer.append(" AND postContentType_s:");
		buffer.append(SolrUtil.escape(PostContentType.IMAGE_SET.getCode()));
		
		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();
		// コンテンツの一時停止対応
		if(communityUserId.equals(loginCommunityUserId)) {
			buffer.append(" AND (");
			buffer.append(" status_s:");
			buffer.append(ContentsStatus.SUBMITTED.getCode());
			buffer.append(" OR ");
			buffer.append(" status_s:");
			buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
			buffer.append(" ) ");
		}else{
			buffer.append(" AND status_s:");
			buffer.append(ContentsStatus.SUBMITTED.getCode());
		}
		
		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND postDate_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND postDate_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}
		AdultHelper adultHelper = new AdultHelper(adultVerification);
		SolrQuery query = new SolrQuery(
				adultHelper.toFilterQuery(buffer.toString()));
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("postDate_dt", ORDER.desc);
		} else {
			query.setSortField("postDate_dt", ORDER.asc);
		}
		SearchResult<ImageHeaderDO> searchResult = new SearchResult<ImageHeaderDO>(
				solrOperations.findByQuery(query, ImageHeaderDO.class,
				Path.includeProp("*").includePath(
						"ownerCommunityUser.communityUserId"
						).depth(1)));
		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(
							buffer.toString(), ImageHeaderDO.class, solrOperations));
		}
		ProductUtil.filterInvalidProduct(searchResult);
		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}

	/**
	 * 指定した画像情報に紐付くコンテンツに紐付く画像数を返します。
	 * @param imageHeaders 画像ヘッダーのリスト
	 * @return 画像数マップ
	 */
	@Override
	public Map<String, Long> loadContentsImageCountMap(
			List<ImageHeaderDO> imageHeaders) {
		List<ImageHeaderDO> reviewImages = new ArrayList<ImageHeaderDO>();
		List<ImageHeaderDO> questionImages = new ArrayList<ImageHeaderDO>();
		List<ImageHeaderDO> questionAnswerImages = new ArrayList<ImageHeaderDO>();
		List<ImageHeaderDO> imageSetImages = new ArrayList<ImageHeaderDO>();
		for (ImageHeaderDO imageHeader : imageHeaders) {
			if (imageHeader.getPostContentType().equals(PostContentType.REVIEW)) {
				reviewImages.add(imageHeader);
			} else if (imageHeader.getPostContentType().equals(PostContentType.QUESTION)) {
				questionImages.add(imageHeader);
			} else if (imageHeader.getPostContentType().equals(PostContentType.ANSWER)) {
				questionAnswerImages.add(imageHeader);
			} else if (imageHeader.getPostContentType().equals(PostContentType.IMAGE_SET)) {
				imageSetImages.add(imageHeader);
			}
		}
		Map<String, Long> result = new HashMap<String, Long>();
		if (reviewImages.size() > 0) {
			result.putAll(loadContentsImageCountMap(reviewImages, PostContentType.REVIEW));
		}
		if (questionImages.size() > 0) {
			result.putAll(loadContentsImageCountMap(questionImages, PostContentType.QUESTION));
		}
		if (questionAnswerImages.size() > 0) {
			result.putAll(loadContentsImageCountMap(questionAnswerImages, PostContentType.ANSWER));
		}
		if (imageSetImages.size() > 0) {
			result.putAll(loadContentsImageCountMap(imageSetImages, PostContentType.IMAGE_SET));
		}
		return result;
	}

	/**
	 * 指定した画像情報に紐付くコンテンツに紐付く画像数を返します。
	 * @param imageHeaders 画像ヘッダーのリスト
	 * @param type タイプ
	 * @return 画像数マップ
	 */
	private Map<String, Long> loadContentsImageCountMap(
			List<ImageHeaderDO> imageHeaders, PostContentType type) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		
		buffer.append("AND postContentType_s:");
		buffer.append(type.getCode());
		
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		buffer.append(" AND thumbnail_b:false");
		buffer.append(" AND (");
		String facetField = null;

		if (type.equals(PostContentType.REVIEW)) {
			facetField = "reviewId_s";
		} else if (type.equals(PostContentType.QUESTION)) {
			facetField = "questionId_s";
		} else if (type.equals(PostContentType.ANSWER)) {
			facetField = "questionAnswerId_s";
		} else if (type.equals(PostContentType.IMAGE_SET)) {
			facetField = "imageSetId_s";
		}
		for (int i = 0; i < imageHeaders.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append(facetField);
			buffer.append(":");
			buffer.append(SolrUtil.escape(imageHeaders.get(i).getContentsId()));
		}
		buffer.append(")");
		Map<String, Long> contentsIdMap = new HashMap<String, Long>();
		for (FacetResult<String> facetResult : solrOperations.facet(ImageHeaderDO.class, String.class,
				new SolrQuery(buffer.toString()).addFacetField(
						facetField).setFacetLimit(SolrConstants.QUERY_ROW_LIMIT))) {
			contentsIdMap.put(facetResult.getValue(), facetResult.getCount());
		}
		Map<String, Long> result = new HashMap<String, Long>();
		for (ImageHeaderDO imageHeader : imageHeaders) {
			result.put(imageHeader.getImageId(),
					contentsIdMap.get(imageHeader.getContentsId()));
		}
		return result;
	}


	@Override
	@MethodCache(
			cacheStrategy=CacheStrategyType.HttpSession,
			limitTime=5, limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityDataSyncWeb}
			)
	public long countImageSetByCommunityUserId(String communityUserId) {
		return countImageSetByCommunityUserId(
				communityUserId,
				null,
				new ContentsStatus[]{ContentsStatus.SUBMITTED},
				requestScopeDao.loadAdultVerification());
	}

	@Override
	@MethodCache(
			cacheStrategy=CacheStrategyType.HttpSession, 
			limitTime=5, limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityDataSyncWeb}
			)
	public long countImageSetByCommunityUserIdForMypage(String communityUserId) {
		return countImageSetByCommunityUserId(
				communityUserId,
				null,
				new ContentsStatus[]{ContentsStatus.SUBMITTED, ContentsStatus.CONTENTS_STOP},
				requestScopeDao.loadAdultVerification());
	}
	
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.HttpSession,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityDataSyncWeb}
			)
	public long countImageSetByCommunityUserId(
			String communityUserId,
			String excludeImageSetId,
			ContentsStatus[] statuses,
			Verification adultVerification){
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false");
		buffer.append(" AND ownerCommunityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND listViewFlag_b:true");
		buffer.append(" AND thumbnail_b:false");
		buffer.append(" AND postContentType_s:");
		buffer.append(SolrUtil.escape(PostContentType.IMAGE_SET.getCode()));
		
		if( statuses != null && statuses.length >0 ){
			buffer.append(" AND (");
			for( int i=0; i<statuses.length; i++){
				if( i > 0)
					buffer.append(" OR ");
				buffer.append(" status_s:");
				buffer.append(statuses[i].getCode());
			}
			buffer.append(" ) ");
		}
		if(!StringUtils.isEmpty(excludeImageSetId))
			buffer.append(" AND !imageSetId:" + excludeImageSetId);
		
		SolrQuery solrQuery = new SolrQuery(new AdultHelper(adultVerification).toFilterQuery(buffer.toString()));
		return solrOperations.count(solrQuery, ImageHeaderDO.class);
	}
	

	/**
	 * 指定した画像セットの中でトップ画像を返します。
	 * @param imageSetId 画像セットID
	 * @return 画像
	 */
	@Override
	public ImageHeaderDO loadTopImage(
			String imageSetId) {
		ImageHeaderDO result = null;
		for (ImageHeaderDO target : hBaseOperations.findWithIndex(
				ImageHeaderDO.class, "imageSetId", imageSetId)) {
			if (!target.isDeleted() && !target.getStatus().equals(ContentsStatus.CONTENTS_STOP)) {
				if (target.isListViewFlag()) {
					return target;
				} else if (result == null) {
					result = target;
				} else if (result.getImageSetIndex() > target.getImageSetIndex()) {
					result = target;
				}
			}
		}
		return result;
	}
	
	/**
	 * 指定した画像セットに紐付く画像を順番に返します。
	 * @param reviewId レビューID
	 * @param excludeImageId 除外する画像ID
	 * @param statuses コンテンツ状態一覧
	 * @return 画像一覧
	 */
	@Override
	public List<ImageHeaderDO> findImageByReviewId(
			String reviewId,
			String excludeImageId,
			ContentsStatus[] statuses) {
		return findImageByContentId(reviewId, ImageTargetType.REVIEW, excludeImageId, statuses);
	}
	/**
	 * 指定した画像セットに紐付く画像を順番に返します。
	 * @param questionId 質問ID
	 * @param excludeImageId 除外する画像ID
	 * @param statuses コンテンツ状態一覧
	 * @return 画像一覧
	 */
	@Override
	public List<ImageHeaderDO> findImageByQuestionId(
			String questionId,
			String excludeImageId,
			ContentsStatus[] statuses) {
		return findImageByContentId(questionId, ImageTargetType.QUESTION, excludeImageId, statuses);
	}

	/**
	 * 指定した画像セットに紐付く画像を順番に返します。
	 * @param questionAnswerId 質問回答ID
	 * @param excludeImageId 除外する画像ID
	 * @param statuses コンテンツ状態一覧
	 * @return 画像一覧
	 */
	@Override
	public List<ImageHeaderDO> findImageByQuestionAnswerId(
			String questionAnswerId, String excludeImageId,
			ContentsStatus[] statuses) {
		return findImageByContentId(questionAnswerId, ImageTargetType.QUESTION_ANSWER, excludeImageId, statuses);
	}

	/**
	 * 指定した画像セットに紐付く画像を順番に返します。
	 * @param imageSetId 画像セットID
	 * @param excludeImageId 除外する画像ID
	 * @param statuses コンテンツ状態一覧
	 * @return 画像一覧
	 */
	@Override
	public List<ImageHeaderDO> findImageByImageSetId(
			String imageSetId,
			String excludeImageId,
			ContentsStatus[] statuses) {
		return findImageByContentId(imageSetId, ImageTargetType.IMAGE, excludeImageId, statuses);
	}
	
	@Override
	public List<ImageHeaderDO> findImageByContentId(
			String contentId,
			ImageTargetType imageTargetType,
			String excludeImageId,
			ContentsStatus[] statuses) {
		if (contentId == null || contentId.trim().length() == 0 || imageTargetType == null) 
			throw new IllegalArgumentException("contentId or imageTargetType is null.");
		StringBuilder sb = new StringBuilder();
		
		sb.append("withdraw_b:false ");
		sb.append(" AND (");
		boolean isFirst = true;
		for(ContentsStatus status:statuses){
			if(!isFirst) sb.append(" OR ");
			sb.append(" status_s:");
			sb.append(status.getCode());
			isFirst = false;
		}
		sb.append(" ) ");
		sb.append(" AND thumbnail_b:false");
		sb.append(" AND postContentType_s:");
		if( ImageTargetType.REVIEW.equals(imageTargetType)){
			sb.append(SolrUtil.escape(PostContentType.REVIEW.getCode()));
			sb.append(" AND reviewId_s:");
		}else if( ImageTargetType.QUESTION.equals(imageTargetType)){
			sb.append(SolrUtil.escape(PostContentType.QUESTION.getCode()));
			sb.append(" AND questionId_s:");
		}else if( ImageTargetType.QUESTION_ANSWER.equals(imageTargetType)){
			sb.append(SolrUtil.escape(PostContentType.ANSWER.getCode()));
			sb.append(" AND questionAnswerId_s:");
		}else if( ImageTargetType.IMAGE.equals(imageTargetType)){
			sb.append(SolrUtil.escape(PostContentType.IMAGE_SET.getCode()));
			sb.append(" AND imageSetId_s:");
		}else{
			throw new IllegalArgumentException("contentId or imageTargetType is null.");
		}
		
		sb.append(SolrUtil.escape(contentId));
		if(!StringUtils.isEmpty(excludeImageId))
			sb.append(" AND !imageId:" + SolrUtil.escape(excludeImageId));
		
		List<ImageHeaderDO> imageHeaders = Lists.newArrayList();
		String communityUserId = requestScopeDao.loadCommunityUserId();
		for(ImageHeaderDO imageHeader:solrOperations.findByQuery(
				new SolrQuery(sb.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT).setSortField("imageSetIndex_i", ORDER.asc),
						ImageHeaderDO.class,
						Path.includeProp("*").includePath(
								"ownerCommunityUser.communityUserId").depth(1)).getDocuments()){
			// コンテンツの一時停止対応
			if(!imageHeader.getOwnerCommunityUser().getCommunityUserId().equals(communityUserId) && 
					ContentsStatus.CONTENTS_STOP.equals(imageHeader.getStatus())) {
				continue;
			}
			imageHeaders.add(imageHeader);
		}
		
		ProductUtil.filterInvalidProduct(imageHeaders);
		
		return imageHeaders;
	}
	@Override
	public List<ImageHeaderDO> findImageByContentIdWithCommunityUserId(
			String communityUserId,
			String contentId,
			ImageTargetType imageTargetType,
			String excludeImageId,
			ContentsStatus[] statuses) {
		if (communityUserId == null || communityUserId.trim().length() == 0 || contentId == null || contentId.trim().length() == 0 || imageTargetType == null) 
			throw new IllegalArgumentException("contentId or imageTargetType is null.");
		StringBuilder sb = new StringBuilder();
		
		sb.append("withdraw_b:false");
		sb.append(" AND ownerCommunityUserId_s:");
		sb.append(SolrUtil.escape(communityUserId));
		sb.append(" AND (");
		boolean isFirst = true;
		for(ContentsStatus status:statuses){
			if(!isFirst) sb.append(" OR ");
			sb.append(" status_s:");
			sb.append(status.getCode());
			isFirst = false;
		}
		sb.append(" ) ");
		sb.append(" AND thumbnail_b:false");
		if( ImageTargetType.REVIEW.equals(imageTargetType)){
			sb.append(" AND reviewId_s:");
		}else if( ImageTargetType.QUESTION.equals(imageTargetType)){
			sb.append(" AND questionId_s:");
		}else if( ImageTargetType.QUESTION_ANSWER.equals(imageTargetType)){
			sb.append(" AND questionAnswerId_s:");
		}else if( ImageTargetType.IMAGE.equals(imageTargetType)){
			sb.append(" AND imageSetId_s:");
		}
		sb.append(SolrUtil.escape(contentId));
		if(!StringUtils.isEmpty(excludeImageId))
			sb.append(" AND !imageId:" + SolrUtil.escape(excludeImageId));
		
		List<ImageHeaderDO> imageHeaders = Lists.newArrayList();
		for(ImageHeaderDO imageHeader:solrOperations.findByQuery(
				new SolrQuery(sb.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT).setSortField("imageSetIndex_i", ORDER.asc),
						ImageHeaderDO.class,
						Path.includeProp("*").includePath(
								"ownerCommunityUser.communityUserId").depth(1)).getDocuments()){
			imageHeaders.add(imageHeader);
		}
		
		ProductUtil.filterInvalidProduct(imageHeaders);
		
		return imageHeaders;
	}
	
	
	/**
	 * 指定した画像の画像セットIDに紐付く画像リストマップ返します。
	 * @param imageHeaders 画像ヘッダーのリスト
	 * @return 画像セットマップ
	 */
	@Override
	public Map<String, List<ImageHeaderDO>> loadImageSetMapByImageSetIds(
			List<ImageHeaderDO> imageHeaders) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		buffer.append(" AND thumbnail_b:false");
		buffer.append(" AND (");
		for (int i = 0; i < imageHeaders.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("imageSetId_s:");
			buffer.append(SolrUtil.escape(imageHeaders.get(i).getImageSetId()));
		}
		buffer.append(")");

		Map<String, List<ImageHeaderDO>> result
				= new HashMap<String, List<ImageHeaderDO>>();
		String communityUserId = requestScopeDao.loadCommunityUserId();
		for (ImageHeaderDO imageHeader : solrOperations.findByQuery(
				new SolrQuery(buffer.toString()).setRows(
				SolrConstants.QUERY_ROW_LIMIT
				).setSortField("imageSetId_s", ORDER.asc
						).addSortField("imageSetIndex_i", ORDER.asc), ImageHeaderDO.class).getDocuments()) {
			List<ImageHeaderDO> imageSet = result.get(imageHeader.getImageSetId());
			// コンテンツの一時停止対応
			if(!imageHeader.getOwnerCommunityUser().getCommunityUserId().equals(communityUserId) && imageHeader.getStatus().equals(ContentsStatus.CONTENTS_STOP)) {
				continue;
			}
			if (imageSet == null) {
				imageSet = new ArrayList<ImageHeaderDO>();
				result.put(imageHeader.getImageSetId(), imageSet);
			}
			imageSet.add(imageHeader);
		}
		for (List<ImageHeaderDO> list : result.values()) {
			ProductUtil.filterInvalidProduct(list);
		}
		return result;
	}

	/**
	 * 画像数情報を返します。
	 * @param skus SKUリスト
	 * @return 画像数情報
	 */
	@Override
	public Map<String, Long> loadImageCountMapBySKU(List<String> skus) {
		Map<String, Long> resultMap = new HashMap<String, Long>();
		if (skus == null || skus.size() == 0) {
			return resultMap;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		buffer.append(" AND thumbnail_b:false");
		buffer.append(" AND (");
		for (int i = 0; i < skus.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("productId_s:");
			buffer.append(SolrUtil.escape(skus.get(i)));
		}
		buffer.append(")");
		SolrQuery query = new SolrQuery(buffer.toString());
		query.addFacetField("productId_s");
		query.setFacetSort(FacetParams.FACET_SORT_COUNT);
		query.setFacetLimit(SolrConstants.QUERY_ROW_LIMIT);
		List<FacetResult<String>> searchResult = solrOperations.facet(
				ImageHeaderDO.class, String.class, query);
		for (FacetResult<String> facetResult : searchResult) {
			resultMap.put(facetResult.getValue(), facetResult.getCount());
		}

		return resultMap;
	}

	/**
	 * 画像数情報を返します。
	 * @param communityUserIds コミュニティユーザーIDのリスト
	 * @return 画像数情報
	 */
	@Override
	public Map<String, Long> loadImageCountMapByCommunityUserId(
			List<String> communityUserIds) {
		Map<String, Long> resultMap = new HashMap<String, Long>();
		if (communityUserIds == null || communityUserIds.size() == 0) {
			return resultMap;
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" AND listViewFlag_b:true");
		buffer.append(" AND thumbnail_b:false");
		buffer.append(" AND postContentType_s:");
		buffer.append(SolrUtil.escape(PostContentType.IMAGE_SET.getCode()));
		buffer.append(" AND (");
		for (int i = 0; i < communityUserIds.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("ownerCommunityUserId_s:");
			buffer.append(SolrUtil.escape(communityUserIds.get(i)));
		}
		buffer.append(")");
		SolrQuery query = new SolrQuery(
				new AdultHelper(requestScopeDao.loadAdultVerification(
										)).toFilterQuery(buffer.toString()));
		query.addFacetField("ownerCommunityUserId_s");
		query.setFacetSort(FacetParams.FACET_SORT_COUNT);
		query.setFacetLimit(SolrConstants.QUERY_ROW_LIMIT);
		List<FacetResult<String>> searchResult = solrOperations.facet(
				ImageHeaderDO.class, String.class, query);
		for (FacetResult<String> facetResult : searchResult) {
			resultMap.put(facetResult.getValue(), facetResult.getCount());
		}

		return resultMap;
	}

	/**
	 * 投稿画像セット数を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @return 投稿画像セット数
	 */
	@Override
	public long countPostImageSetCount(String communityUserId, String sku) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" AND listViewFlag_b:true");
		buffer.append(" AND thumbnail_b:false");
		buffer.append(" AND postContentType_s:");
		buffer.append(SolrUtil.escape(PostContentType.IMAGE_SET.getCode()));
		buffer.append(" AND ownerCommunityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		if (!StringUtils.isEmpty(sku)) {
			buffer.append(" AND productId_s:");
			buffer.append(SolrUtil.escape(sku));
		}

		SolrQuery solrQuery = new SolrQuery(new AdultHelper(
				requestScopeDao.loadAdultVerification(
						)).toFilterQuery(buffer.toString()));
		return solrOperations.count(solrQuery, ImageHeaderDO.class);
	}

	/**
	 * 指定した商品に画像を投稿したユーザーを重複を除いて返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param all 全てを対象とする場合（レビュー、質問、回答本文内含む）
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findDistinctImageUploaderBySKU(
			String sku, int limit, int offset, boolean all) {
		//重複除去と投稿順の並び替えを同時に満たすため、solr ではサブクエリ
		//とdistinctをサポートしていないので、上限数を絞り、java 側で処理します。
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND productId_s:");
		buffer.append(SolrUtil.escape(sku));
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		buffer.append(" AND thumbnail_b:false");
		buffer.append(" AND (postContentType_s:");
		buffer.append(SolrUtil.escape(PostContentType.IMAGE_SET.getCode()));
		if (all) {
			buffer.append(" OR postContentType_s:");
			buffer.append(SolrUtil.escape(PostContentType.REVIEW.getCode()));
			buffer.append(" OR postContentType_s:");
			buffer.append(SolrUtil.escape(PostContentType.QUESTION.getCode()));
			buffer.append(" OR postContentType_s:");
			buffer.append(SolrUtil.escape(PostContentType.ANSWER.getCode()));
		}
		buffer.append(")");
		SearchResult<ImageHeaderDO> searchResult = new SearchResult<ImageHeaderDO>(
				solrOperations.findByQuery(
						new SolrQuery(buffer.toString()).setRows(
										SolrConstants.QUERY_ROW_LIMIT).setStart(0).addSortField("postDate_dt", ORDER.desc),
										ImageHeaderDO.class, Path.includeProp("ownerCommunityUserId,status")));
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<CommunityUserDO>();
		}
		List<String> communityUserIds = new ArrayList<String>();
		List<String> communityUserIdAll = new ArrayList<String>();
		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();
		for (ImageHeaderDO imageHeader : searchResult.getDocuments()) {

			// コンテンツの一時停止対応
			if(!imageHeader.getOwnerCommunityUser().getCommunityUserId().equals(loginCommunityUserId) && imageHeader.getStatus().equals(ContentsStatus.CONTENTS_STOP)) {
				continue;
			}
			
			if (!communityUserIdAll.contains(imageHeader.getOwnerCommunityUser().getCommunityUserId())) {
				communityUserIdAll.add(imageHeader.getOwnerCommunityUser().getCommunityUserId());
				if (communityUserIdAll.size() > offset
						&& communityUserIdAll.size() <= (offset + limit)) {
					communityUserIds.add(imageHeader.getOwnerCommunityUser().getCommunityUserId());
				}
			}
		}
		SearchResult<CommunityUserDO> result = new SearchResult<CommunityUserDO>(
				communityUserIdAll.size(), new ArrayList<CommunityUserDO>());
		if( !communityUserIds.isEmpty() ){
			Map<String, CommunityUserDO> resultMap = solrOperations.find(
					CommunityUserDO.class, String.class, communityUserIds);
			for (String communityUserId : communityUserIds) {
				result.getDocuments().add(resultMap.get(communityUserId));
			}
		}
		return result;
	}

	/**
	 * 指定したコミュニティユーザーが指定した日付に投稿した画像セットを返します。
	 * @param communityUserIds コミュニティユーザーIDリスト
	 * @param publicDate 公開された日付
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return 画像セットリスト
	 */
	@Override
	public SearchResult<ImageHeaderDO> findImageSetByCommunityUserIds(
			List<String> communityUserIds, Date publicDate, int limit, int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND listViewFlag_b:true");
		buffer.append(" AND thumbnail_b:false");
		buffer.append(" AND postContentType_s:");
		buffer.append(SolrUtil.escape(PostContentType.IMAGE_SET.getCode()));
		buffer.append(" AND ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" AND (");
		for (int i = 0; i < communityUserIds.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("ownerCommunityUserId_s:");
			buffer.append(SolrUtil.escape(communityUserIds.get(i)));
		}
		buffer.append(") AND ");
		buffer.append(SolrUtil.getSolrDateRangeQuery("postDate_dt", publicDate));
		SearchResult<ImageHeaderDO> searchResult
		= new SearchResult<ImageHeaderDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
						).toFilterQuery(buffer.toString())).setRows(limit).setStart(offset
								).setSortField("postDate_dt", ORDER.asc),
						ImageHeaderDO.class, Path.includeProp("*").includePath(
						"product.sku,ownerCommunityUser.communityUserId").depth(1)));
		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}

	@Override
	public SearchResult<ImageHeaderDO> findImageSetByCommunityUserIdsForMR(
			List<String> communityUserIds, Date publicDate, int limit, int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND listViewFlag_b:true");
		buffer.append(" AND thumbnail_b:false");
		buffer.append(" AND postContentType_s:");
		buffer.append(SolrUtil.escape(PostContentType.IMAGE_SET.getCode()));
		buffer.append(" AND ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" AND (");
		for (int i = 0; i < communityUserIds.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("ownerCommunityUserId_s:");
			buffer.append(SolrUtil.escape(communityUserIds.get(i)));
		}
		buffer.append(") AND ");
		buffer.append(SolrUtil.getSolrDateRangeQuery("postDate_dt", publicDate));
		SearchResult<ImageHeaderDO> searchResult
		= new SearchResult<ImageHeaderDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
						).toFilterQuery(buffer.toString())).setRows(limit).setStart(offset
								).setSortField("postDate_dt", ORDER.asc),
						ImageHeaderDO.class, Path.includeProp("*").includePath(
						"ownerCommunityUser.communityUserId").depth(1)));

		List<String> skus = new ArrayList<String>();
		for(ImageHeaderDO image:searchResult.getDocuments()) {
			skus.add(image.getProduct().getSku());
		}
		Map<String, ProductDO> productMap = productDao.findBySkuForMR(skus);
		for(ImageHeaderDO image:searchResult.getDocuments()) {
			image.setProduct(productMap.get(image.getProduct().getSku()));
		}		

		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}
	
	
	/**
	 * 指定した商品、日付に投稿した画像セットを返します。
	 * @param skus SKUリスト
	 * @param publicDate 公開された日付
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return 画像セットリスト
	 */
	@Override
	public SearchResult<ImageHeaderDO> findImageSetBySKUs(
			List<String> skus, Date publicDate, String excludeCommunityId, int limit, int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND listViewFlag_b:true");
		buffer.append(" AND thumbnail_b:false");
		buffer.append(" AND postContentType_s:");
		buffer.append(SolrUtil.escape(PostContentType.IMAGE_SET.getCode()));
		buffer.append(" AND ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		if(!StringUtils.isEmpty(excludeCommunityId))
			buffer.append(" AND !ownerCommunityUserId_s:" + SolrUtil.escape(excludeCommunityId));
		buffer.append(" AND (");
		for (int i = 0; i < skus.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("productId_s:");
			buffer.append(SolrUtil.escape(skus.get(i)));
		}
		buffer.append(") AND ");
		buffer.append(SolrUtil.getSolrDateRangeQuery("postDate_dt", publicDate));
		SearchResult<ImageHeaderDO> searchResult = new SearchResult<ImageHeaderDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
						).toFilterQuery(buffer.toString())).setRows(limit).setStart(offset
								).setSortField("postDate_dt", ORDER.asc),
						ImageHeaderDO.class, Path.includeProp("*").includePath(
						"product.sku,ownerCommunityUser.communityUserId").depth(1)));
		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}

	@Override
	public SearchResult<ImageHeaderDO> findImageSetBySKUsForMR(
			List<String> skus, Date publicDate, String excludeCommunityId, int limit, int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false AND listViewFlag_b:true");
		buffer.append(" AND thumbnail_b:false");
		buffer.append(" AND postContentType_s:");
		buffer.append(SolrUtil.escape(PostContentType.IMAGE_SET.getCode()));
		buffer.append(" AND ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		if(!StringUtils.isEmpty(excludeCommunityId))
			buffer.append(" AND !ownerCommunityUserId_s:" + SolrUtil.escape(excludeCommunityId));
		buffer.append(" AND (");
		for (int i = 0; i < skus.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("productId_s:");
			buffer.append(SolrUtil.escape(skus.get(i)));
		}
		buffer.append(") AND ");
		buffer.append(SolrUtil.getSolrDateRangeQuery("postDate_dt", publicDate));
		SearchResult<ImageHeaderDO> searchResult = new SearchResult<ImageHeaderDO>(solrOperations.findByQuery(
				new SolrQuery(new AdultHelper(requestScopeDao.loadAdultVerification()
						).toFilterQuery(buffer.toString())).setRows(limit).setStart(offset
								).setSortField("postDate_dt", ORDER.asc),
						ImageHeaderDO.class, Path.includeProp("*").includePath(
						"ownerCommunityUser.communityUserId").depth(1)));
		
		for(ImageHeaderDO image:searchResult.getDocuments()) {
			skus.add(image.getProduct().getSku());
		}
		Map<String, ProductDO> productMap = productDao.findBySkuForMR(skus);
		for(ImageHeaderDO image:searchResult.getDocuments()) {
			image.setProduct(productMap.get(image.getProduct().getSku()));
		}		
		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}

	/**
	 * 画像サーバとの同期エラーが出ているものを全て同期します。
	 */
	@Override
	public void recoverImageSyncError() {
		List<ImageHeaderDO> imageHeaders = hBaseOperations.scanWithIndex(
				ImageHeaderDO.class, "imageSyncStatus", ImageSyncStatus.ERROR);
		List<ImageHeaderDO> updateImageHeaders = new ArrayList<ImageHeaderDO>();
		for (ImageHeaderDO imageHeader : imageHeaders) {
			if (imageHeader.getStatus().equals(ContentsStatus.SUBMITTED)) {
				if (imageHeader.getImageUploadResult() != null
						&& !imageHeader.getImageUploadResult().equals(ImageUploadResult.SUCCESS)) {
					uploadImageForSync(imageHeader);
					imageHeader.setModifyDateTime(timestampHolder.getTimestamp());
					updateImageHeaders.add(imageHeader);
				}
			} else {
				if (imageHeader.getImageDeleteResult() != null
						&& !imageHeader.getImageDeleteResult(
								).equals(ImageDeleteResult.SUCCESS)) {
					String remoteFilePath = imageHeader.getImageUrl().substring(
							imageHeader.getImageUrl().lastIndexOf(
									resourceConfig.imageUploadPath));
					int index = remoteFilePath.lastIndexOf("/");
					String remoteDir = remoteFilePath.substring(0, index);
					String remoteFileName = remoteFilePath.substring(index + 1);
					ImageDeleteResult imageDeleteResult = imageCacheDao.delete(remoteDir, remoteFileName);
					imageCacheDao.clearCache(imageHeader.getImageUrl().substring(resourceConfig.imageUrl.length()));
					LOG.info("image delete success. communityUserId=" + imageHeader.getOwnerCommunityUserId()
							+ ", imageId=" + imageHeader.getImageId()
							+ ", imageUrl=" + imageHeader.getImageUrl());
					
					imageHeader.setImageDeleteResult(imageDeleteResult);
					if (imageDeleteResult.equals(ImageDeleteResult.SUCCESS)) {
						imageHeader.setImageSyncStatus(ImageSyncStatus.SYNC);
					} else {
						imageHeader.setImageSyncStatus(ImageSyncStatus.ERROR);
					}
					imageHeader.setModifyDateTime(timestampHolder.getTimestamp());
					updateImageHeaders.add(imageHeader);
				}
			}
		}
		hBaseOperations.save(ImageHeaderDO.class, updateImageHeaders,
				Path.includeProp("imageSyncStatus,imageUploadResult," +
						"imageDeleteResult,modifyDateTime"));
		solrOperations.save(ImageHeaderDO.class, updateImageHeaders,
				Path.includeProp("imageSyncStatus,imageUploadResult," +
						"imageDeleteResult,modifyDateTime"));
	}

	/**
	 * 指定した画像をアップロード済みとして更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param imageId 画像ID
	 * @param imageUrl 画像URL
	 */
	private void updateUploaded(String communityUserId, String imageId, String imageUrl) {
		ImageDO image = new ImageDO();
		image.setImageId(imageId);
		image.setCommunityUserId(communityUserId);
		image.setImageUrl(imageUrl);
		image.setTemporaryFlag(false);
		image.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(image, Path.includeProp("imageUrl,communityUserId,temporaryFlag,modifyDateTime"));
	}

	/**
	 * ランダムなパスを返します。
	 * @return パス
	 */
	protected String randomPath() {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < 4; i++) {
			buffer.append("/");
			buffer.append(getRandomChar());
			buffer.append(getRandomChar());
		}
		return buffer.toString();
	}

	/**
	 * ランダムなアルファベットを返します。
	 * @return ランダムなアルファベット
	 */
	private char getRandomChar() {
		int i = random.nextInt(36);
		if (i < 10) {
			return (char) (i + 48);
		} else {
			return (char) (i + 55);
		}
	}

	/**
	 * 指定した画像投稿タイプのcontentIdに紐づく画像一覧を取得します。
	 * @param contentId コンテンツID（imageTargetTypeに従ったIDを指定する）
	 * @param imageTargetType 画像投稿タイプ（１：レビュー、２：質問、３：質問回答、４：画像）
	 * @param path パス
	 * @return 画像一覧
	 */
	@Override
	public List<ImageHeaderDO> loadImages(ImageTargetType imageTargetType, String contentId) {
		return loadImages(imageTargetType, contentId, Path.includeProp("*"));
	}
	
	/**
	 * 指定した画像投稿タイプのcontentIdに紐づく画像一覧を取得します。
	 * @param contentId コンテンツID（imageTargetTypeに従ったIDを指定する）
	 * @param imageTargetType 画像投稿タイプ（１：レビュー、２：質問、３：質問回答、４：画像）
	 * @return 画像一覧
	 */
	@Override
	public List<ImageHeaderDO> loadImages(ImageTargetType imageTargetType, String contentId, Path.Condition path) {
		String indexName = null;
		if( ImageTargetType.REVIEW.equals(imageTargetType)){
			indexName = "reviewId";
		}else if( ImageTargetType.QUESTION.equals(imageTargetType)){
			indexName = "questionId";
		}else if( ImageTargetType.QUESTION_ANSWER.equals(imageTargetType)){
			indexName = "questionAnswerId";
		}else if( ImageTargetType.IMAGE.equals(imageTargetType)){
			indexName = "imageSetId";
		}
		return hBaseOperations.scanWithIndex(ImageHeaderDO.class, indexName, contentId, path);
	}
	
	@Override
	public List<ImageHeaderDO> loadImages(String imageSetId) {
		return loadImages(imageSetId, Path.includeProp("*"));
	}

	@Override
	public List<ImageHeaderDO> loadImages(String imageSetId, Path.Condition path) {
		return hBaseOperations.scanWithIndex(ImageHeaderDO.class, "imageSetId", imageSetId, path);
	}

	public long countReviewsImage(String reviewId){
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		buffer.append(" AND thumbnail_b:false");
		buffer.append(" AND postContentType_s:");
		buffer.append(SolrUtil.escape(PostContentType.REVIEW.getCode()));
		buffer.append(" AND reviewId_s:");
		buffer.append(SolrUtil.escape(reviewId));
		return solrOperations.count(new SolrQuery(buffer.toString()), ImageHeaderDO.class);
	}

	public long countQuestionsImage(String questionId){
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		buffer.append(" AND thumbnail_b:false");
		buffer.append(" AND postContentType_s:");
		buffer.append(SolrUtil.escape(PostContentType.QUESTION.getCode()));
		buffer.append(" AND questionId_s:");
		buffer.append(SolrUtil.escape(questionId));
		return solrOperations.count(new SolrQuery(buffer.toString()), ImageHeaderDO.class);
	}

	public long countQuestionAnswersImage(String questionAnswerId){
		StringBuilder buffer = new StringBuilder();
		buffer.append("withdraw_b:false ");
		buffer.append(" AND ( ");
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		buffer.append(" OR status_s:");
		buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
		buffer.append(" ) ");
		buffer.append(" AND thumbnail_b:false");
		buffer.append(" AND postContentType_s:");
		buffer.append(SolrUtil.escape(PostContentType.ANSWER.getCode()));
		buffer.append(" AND questionAnswerId_s:");
		buffer.append(SolrUtil.escape(questionAnswerId));
		return solrOperations.count(new SolrQuery(buffer.toString()), ImageHeaderDO.class);
	}

	public void loadImageMapByImageSetId(List<String> imageSetIds, Map<String, List<ImageHeaderDO>> imageMap){

		if(imageSetIds == null || imageSetIds.isEmpty()) return;

		StringBuilder sb = new StringBuilder();

		sb.append("withdraw_b:false ");
		sb.append(" AND ( ");
		sb.append("status_s:");
		sb.append(ContentsStatus.SUBMITTED.getCode());
		sb.append(" OR status_s:");
		sb.append(ContentsStatus.CONTENTS_STOP.getCode());
		sb.append(" ) ");
		sb.append(" AND thumbnail_b:false");
		sb.append(" AND ( ");
		for(int i=0;i<imageSetIds.size();i++){
			if(i > 0) sb.append(" OR");
			sb.append(" imageSetId_s:" + SolrUtil.escape(imageSetIds.get(i)));
		}
		sb.append(" ) ");
		String communityUserId = requestScopeDao.loadCommunityUserId();
		for(ImageHeaderDO image:solrOperations.findByQuery(
				new SolrQuery(sb.toString()).setRows(imageSetIds.size() * ServiceConfig.INSTANCE.productImageSubmitFileuploadMaxLength).addSortField("imageSetId_s", ORDER.asc).addSortField("imageSetIndex_i", ORDER.asc),
								ImageHeaderDO.class,
								Path.includeProp("*").includePath(
										"ownerCommunityUser.communityUserId").depth(1)).getDocuments()){
			
			if(image == null || StringUtils.isEmpty(image.getImageSetId())) continue;
			
			// コンテンツの一時停止対応
			if(!image.getOwnerCommunityUser().getCommunityUserId().equals(communityUserId) && image.getStatus().equals(ContentsStatus.CONTENTS_STOP)) {
				continue;
			}
			
			if(!imageMap.containsKey(image.getImageSetId())){
				imageMap.put(image.getImageSetId(), new ArrayList<ImageHeaderDO>());
			}
			if(StringUtils.isNotEmpty(image.getImageSetId())){
				imageMap.get(image.getImageSetId()).add(image);
			}
		}
	}

	@Override
	public String findProductSku(String imageId) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("imageId:");
		buffer.append(imageId);

		SearchResult<ImageHeaderDO> results = new SearchResult<ImageHeaderDO>(
				solrOperations.findByQuery(
						new SolrQuery(buffer.toString()),
						ImageHeaderDO.class,
						Path.includeProp("*")));
		ProductUtil.filterInvalidProduct(results);
		if(results == null || results.getDocuments().isEmpty() || results.getDocuments().size() > 1 || results.getDocuments().get(0).getProduct() == null)
			return null;
		return results.getDocuments().get(0).getProduct().getSku();
	}

	@Override
	public SearchResult<ImageHeaderDO> loadThumbnailImagesByOwnerCommunityUserId(String ownerCommunityUserId, int limit) {
		StringBuilder sb = new StringBuilder();

		sb.append("ownerCommunityUserId_s:");
		sb.append(SolrUtil.escape(ownerCommunityUserId));
		sb.append(" AND ");
		sb.append("status_s:");
		sb.append(ContentsStatus.SUBMITTED.getCode());
		sb.append(" AND ");
		sb.append("!postContentType_s:");
		sb.append(PostContentType.PROFILE.getCode());
		sb.append(" AND ");
		sb.append("!postContentType_s:");
		sb.append(PostContentType.PROFILE_THUMBNAIL.getCode());
		sb.append(" AND ");
		sb.append("!thumbnail_b:true");
		
		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());
		SolrQuery query = new SolrQuery(
				adultHelper.toFilterQuery(sb.toString()));
		
		SearchResult<ImageHeaderDO> results = new SearchResult<ImageHeaderDO>(
				solrOperations.findByQuery(
						query.setRows(limit).addSortField("registerDateTime_dt", ORDER.desc).addSortField("imageSetIndex_i", ORDER.asc),ImageHeaderDO.class)
					);
		
		return results;
	}
	
}
