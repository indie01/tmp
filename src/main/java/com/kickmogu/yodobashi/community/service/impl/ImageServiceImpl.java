package com.kickmogu.yodobashi.community.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.common.exception.DataNotFoundException;
import com.kickmogu.yodobashi.community.common.exception.UnActiveException;
import com.kickmogu.yodobashi.community.common.utils.DateUtil;
import com.kickmogu.yodobashi.community.common.utils.HTMLConverter;
import com.kickmogu.yodobashi.community.common.utils.HTMLConverter.ImageUrlConverter;
import com.kickmogu.yodobashi.community.common.utils.HTMLConverter.ImageUrlInfo;
import com.kickmogu.yodobashi.community.resource.cache.CacheStrategyType;
import com.kickmogu.yodobashi.community.resource.cache.MethodCache;
import com.kickmogu.yodobashi.community.resource.cache.TargetSystemType;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.dao.ActionHistoryDao;
import com.kickmogu.yodobashi.community.resource.dao.ApplicationLockDao;
import com.kickmogu.yodobashi.community.resource.dao.CommentDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.DailyScoreFactorDao;
import com.kickmogu.yodobashi.community.resource.dao.ImageDao;
import com.kickmogu.yodobashi.community.resource.dao.LikeDao;
import com.kickmogu.yodobashi.community.resource.dao.OrderDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductDao;
import com.kickmogu.yodobashi.community.resource.dao.RemoveContentsDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.util.ProductUtil;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.DailyScoreFactorDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageSetDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ScoreFactorDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.TextEditableContents;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.AsyncMessageType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.DailyScoreFactorType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.LikeMessageType;
import com.kickmogu.yodobashi.community.resource.domain.constants.LikePrefixType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PurchaseHistoryType;
import com.kickmogu.yodobashi.community.service.ImageService;
import com.kickmogu.yodobashi.community.service.IndexService;
import com.kickmogu.yodobashi.community.service.MailService;
import com.kickmogu.yodobashi.community.service.SocialMediaService;
import com.kickmogu.yodobashi.community.service.UserService;
import com.kickmogu.yodobashi.community.service.annotation.SendMessage;
import com.kickmogu.yodobashi.community.service.annotation.SendMessage.Timing;
import com.kickmogu.yodobashi.community.service.vo.CommentSetVO;
import com.kickmogu.yodobashi.community.service.vo.ImageDetailSetVO;
import com.kickmogu.yodobashi.community.service.vo.ImageSetVO;
import com.kickmogu.yodobashi.community.service.vo.ProductImageActivityVO;

/**
 * 画像サービスの実装です。
 * @author kamiike
 *
 */
@Service
public class ImageServiceImpl extends AbstractServiceImpl implements ImageService, InitializingBean {

	/**
	 * アクション履歴 DAO です。
	 */
	@Autowired
	private ActionHistoryDao actionHistoryDao;

	/**
	 * アプリケーションロック DAO です。
	 */
	@Autowired
	private ApplicationLockDao applicationLockDao;

	/**
	 * コミュニティユーザー DAO です。
	 */
	@Autowired
	private CommunityUserDao communityUserDao;

	/**
	 * 画像 DAO です。
	 */
	@Autowired
	private ImageDao imageDao;

	/**
	 * 画像 DAO です。
	 */
	@Autowired
	private LikeDao likeDao;

	/**
	 * 画像 DAO です。
	 */
	@Autowired
	private CommentDao commentDao;

	/**
	 * 注文 DAO です。
	 */
	@Autowired
	private OrderDao orderDao;

	/**
	 * 商品 DAO です。
	 */
	@Autowired @Qualifier("catalog")
	private ProductDao productDao;

	/**
	 * 日次スコア要因 DAO です。
	 */
	@Autowired
	private DailyScoreFactorDao dailyScoreFactorDao;

	/**
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	private RequestScopeDao requestScopeDao;

	/**
	 * インデックスサービスです。
	 */
	@Autowired
	private IndexService indexService;

	/**
	 * socialメディア連携サービスです。
	 */
	@Autowired
	private SocialMediaService socialMediaService;

	/**
	 * メールサービスです。
	 */
	@Autowired
	private MailService mailService;

	/**
	 * コミュニティユーザーサービスです。
	 */
	@Autowired
	private UserService userService;

	/**
	 * リソースコンフィグです。
	 */
	@Autowired
	private ResourceConfig resourceConfig;

	@Autowired
	private RemoveContentsDao removeContentsDao;

	/**
	 * 一時保存中の画像URLパターンです。
	 */
	private Pattern temporaryImageUrlPattern = null;

	/**
	 * アップロード済み画像URLのパターンです。
	 */
	private Pattern uploadedImageUrlPattern = null;


	/**
	 * 指定した商品に対して投稿した画像件数を返します。
	 * @param sku SKU
	 * @return 画像件数
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=10000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public long countImageBySku(String sku) {
		return imageDao.countImageBySku(sku);
	}
	
	/**
	 * 指定した商品に対して投稿した画像件数を返します。
	 * @param sku SKU
	 * @return 画像件数
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=10000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public long countImageBySkus(List<String> skus) {
		return imageDao.countImageBySkus(skus);
	}

	/**
	 * 指定した商品に対して投稿した画像件数を返します。
	 * @param skus SKUリスト
	 * @return 画像件数リスト
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=10000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public Map<String, Long> countImageBySkus(
			String[] skus) {
		return imageDao.countImageBySku(skus);
	}
	
	@Override
	@ArroundSolr
	public SearchResult<ImageSetVO> findImagesBySku(
			String sku,
			int limit,
			int offset, 
			boolean previous) {
		SearchResult<ImageHeaderDO> searchResult = imageDao.findImagesBySku(
				sku,
				limit,
				offset,
				previous);
		
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<ImageSetVO>(0, new ArrayList<ImageSetVO>());
		} else {
			return createImageHeaderSets(searchResult);
		}
	}
	
	@Override
	@ArroundSolr
	public SearchResult<ImageSetVO> findImagesBySkus(
			String sku,
			List<String> skus,
			int limit,
			int offset,
			boolean previous) {
		List<String> mergeSkus = new ArrayList<String>();
		mergeSkus.add(sku);
		mergeSkus.addAll(skus);
		SearchResult<ImageHeaderDO> searchResult = imageDao.findImagesBySkus(
				mergeSkus, limit, offset, previous);
		
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<ImageSetVO>(0, new ArrayList<ImageSetVO>());
		} else {
			return createImageHeaderSets(searchResult);
		}
	}
	
	@Override
	@ArroundSolr
	public SearchResult<ImageSetVO> findImagesByCommunityUserId(
			String communityUserId,
			int limit,
			int offset) {
		SearchResult<ImageHeaderDO> searchResult = imageDao.findImagesByCommunityUserId(communityUserId, limit, offset);
		
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<ImageSetVO>(0, new ArrayList<ImageSetVO>());
		} else {
			return createImageHeaderSets(searchResult);
		}
	}
	
	@Override
	@ArroundSolr
	public ImageSetVO getImageByImageId(String imageId){
		ImageHeaderDO imageHeaderDO = imageDao.getImageByImageId(imageId);
		if( imageHeaderDO == null )
			return null;
		
		SearchResult<ImageHeaderDO> searchResultImageHeaders = new SearchResult<ImageHeaderDO>(1, Lists.newArrayList(imageHeaderDO));
		SearchResult<ImageSetVO> searchResult = createImageHeaderSets(searchResultImageHeaders);
		if( searchResult.getDocuments().isEmpty())
			return null;
		return searchResult.getDocuments().get(0);
	}

	private SearchResult<ImageSetVO> createImageHeaderSets(SearchResult<ImageHeaderDO> searchResult){
		SearchResult<ImageSetVO> result = new SearchResult<ImageSetVO>(0, new ArrayList<ImageSetVO>());
		result.setHasAdult(searchResult.isHasAdult());
		
		long numfound = searchResult.getNumFound();
		
		List<String> imageIds = new ArrayList<String>();
		
		for (ImageHeaderDO imageHeader : searchResult.getDocuments()) {
			imageIds.add(imageHeader.getImageId());
		}
		
		Map<String, Long> otherCountMap = imageDao.loadContentsImageCountMap(searchResult.getDocuments());
		//レビューいいね数
		Map<String, Long> likeCountMap = likeDao.loadImageLikeCountMap(imageIds);
		//レビューコメント数
		Map<String, Long> commentCountMap = commentDao.loadImageCommentCountMap(imageIds);
		
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(searchResult.getDocuments());
		
		Map<String, Boolean> likeMap = new HashMap<String, Boolean>();
		Map<String, Boolean> commentMap = new HashMap<String, Boolean>();
		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();
		if( StringUtils.isNotEmpty(loginCommunityUserId)){
			// いいね済みかどうか
			likeMap = likeDao.loadImageLikeMap(loginCommunityUserId, imageIds);
			// コメント済みかどうか
			commentMap = commentDao.loadImageCommentMap(loginCommunityUserId, imageIds);
		}
		
		for (ImageHeaderDO imageHeader : searchResult.getDocuments()) {
			ImageSetVO vo = new ImageSetVO();
			vo.setImageHeader(imageHeader);
			String imageId = imageHeader.getImageId();
			
			if( otherCountMap.containsKey(imageId)){
				Long count = otherCountMap.get(imageId);
				if (count == null) {
					count = 0L;
				}
				vo.setHasOtherImages(count > 1);
			}
			if (likeMap.containsKey(imageId)) {
				vo.setLikeFlg(likeMap.get(imageId));
			}
			if (likeCountMap.containsKey(imageId)) {
				vo.setLikeCount(likeCountMap.get(imageId));
			}
			if (commentCountMap.containsKey(imageId)) {
				vo.setCommentCount(commentCountMap.get(imageId));
			}
			if (commentMap.containsKey(imageId)) {
				vo.setCommentFlg(commentMap.get(imageId));
			}

			result.updateFirstAndLast(vo);
			if (imageHeader.isStop(loginCommunityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				numfound--;
				continue;
			}
			
			SearchResult<LikeDO> likes = findLikeByImageId(
					imageId,
					loginCommunityUserId,
					resourceConfig.evaluationAreaLikeReadLimit);
			
			long like_count = vo.getLikeCount() - (vo.isLikeFlg()? 1 : 0);
			if( vo.isLikeFlg() ){
				// getLikeCount()は、自分を含んだ数字であることが前提。
				if( likes.getDocuments().isEmpty() ){
					vo.setLikePrefixType(LikePrefixType.ONLYONE.getCode());
				}else{
					vo.setLikePrefixType(LikePrefixType.MULTIPLE.getCode());
				}
			}else{
				vo.setLikePrefixType(LikePrefixType.NONE.getCode());
			}
			List<String> communityUserNames = new ArrayList<String>();
			if( like_count == 0 ){
				vo.setLikeMessageType(LikeMessageType.NONE.getCode());
			}else{
				if( like_count <= 3 ){
					vo.setLikeMessageType(LikeMessageType.UPTO3.getCode());
					for( LikeDO like : likes.getDocuments()){
						communityUserNames.add(like.getCommunityUser().getCommunityName());
					}
				}else{
					vo.setLikeMessageType(LikeMessageType.MULTIPLE.getCode());
				}
			}
			vo.setLikeUserNames(communityUserNames);
			// TODO あとで効率よく取れるように変更する。
			SearchResult<CommentSetVO> resultComment = findImageCommentByImageId(
					imageId,
					null,
					resourceConfig.commentInitReadLimit,
					null,
					false);
			if( !resultComment.getDocuments().isEmpty() )
				Collections.reverse(resultComment.getDocuments());
			vo.setCommentViewRemainingCount(vo.getCommentCount() - resultComment.getDocuments().size());
			vo.setComments(resultComment);
			
			PurchaseProductDO purchaseProductDO = orderDao.loadPurchaseProductBySku(
					imageHeader.getOwnerCommunityUser().getCommunityUserId(),
					imageHeader.getProduct().getSku(),
					Path.DEFAULT,
					false);
			if( purchaseProductDO != null && !purchaseProductDO.isDeleted() ){
				vo.setPurchaseProduct( purchaseProductDO );
			}
			// ログインユーザーの購入情報を取得する。
			if( StringUtils.isNotEmpty(loginCommunityUserId)){
				purchaseProductDO = orderDao.loadPurchaseProductBySku(
						loginCommunityUserId,
						imageHeader.getProduct().getSku(),
						Path.DEFAULT,
						false);
				if( purchaseProductDO != null && !purchaseProductDO.isDeleted() ){
					vo.setLoginUserPurchaseProduct(purchaseProductDO);
				}
			}
			result.getDocuments().add(vo);
		}
		
		if(numfound < 0 ) numfound = 0;
		result.setNumFound(numfound);
		return result;
	}

	/**
	 * 指定した画像セットに紐付く画像を順番に返します。
	 * @param imageSetId 画像セットID
	 * @return 画像一覧
	 */
	@Override
	@ArroundSolr
	public List<ImageHeaderDO> findImageByImageSetId(
			String imageSetId, String excludeImageId) {
		return imageDao.findImageByImageSetId(imageSetId, excludeImageId, new ContentsStatus[]{ContentsStatus.SUBMITTED, ContentsStatus.CONTENTS_STOP});
	}

	/**
	 * 指定したcontentIdに紐付く画像を順番に返します。
	 * @param contentId imageTargetTypeに従ったIDを指定する。レビューID, 質問ID, 質問回答ID, 画像セットIDのいずれか
	 * @param imageTargetType  contentIdのタイプを指定する
	 * @param excludeImageId 除外する画像ID
	 * @return 画像一覧
	 */
	@Override
	public SearchResult<ImageSetVO> findImageByContentId(
			String contentId,
			ImageTargetType imageTargetType,
			String excludeImageId){
		
		List<ImageHeaderDO> result = imageDao.findImageByContentId(
				contentId,
				imageTargetType,
				excludeImageId,
				new ContentsStatus[] { ContentsStatus.SUBMITTED,ContentsStatus.CONTENTS_STOP });

		SearchResult<ImageHeaderDO> searchResult = new SearchResult<ImageHeaderDO>(
				result.size(), new ArrayList<ImageHeaderDO>());
		searchResult.setDocuments(result);
	
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<ImageSetVO>(0, new ArrayList<ImageSetVO>());
		} else {
			return createImageHeaderSets(searchResult);
		}
	}

	/**
	 * 指定したコミュニティユーザーが投稿した画像を投稿日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 画像一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<ProductImageActivityVO> findImageByCommunityUserId(
			String communityUserId,
			int limit,
			Date offsetTime,
			boolean previous) {
		SearchResult<ImageHeaderDO> imageHeaders = imageDao.findImageSetByCommunityUserId(
				communityUserId,
				limit,
				offsetTime,
				previous,
				requestScopeDao.loadAdultVerification());
		SearchResult<ProductImageActivityVO> result
				= new SearchResult<ProductImageActivityVO>(
						imageHeaders.getNumFound(),
						new ArrayList<ProductImageActivityVO>());
		result.setHasAdult(imageHeaders.isHasAdult());
		if (imageHeaders.getDocuments().size() > 0) {
			List<String> skus = new ArrayList<String>();
			for (ImageHeaderDO imageHeader : imageHeaders.getDocuments()) {
				skus.add(imageHeader.getSku());
			}
			Map<String, List<ImageHeaderDO>> imageSetMap = imageDao.loadImageSetMapByImageSetIds(
					imageHeaders.getDocuments());
			List<String> imageIds = new ArrayList<String>();
			for (List<ImageHeaderDO> list : imageSetMap.values()) {
				for (ImageHeaderDO imageHeader : list) {
					imageIds.add(imageHeader.getImageId());
				}
			}
			//画像いいね数
			Map<String, Long> likeCountMap = likeDao.loadImageLikeCountMap(
					imageIds);
			//画像コメント数
			Map<String, Long> commentCountMap = commentDao.loadImageCommentCountMap(
					imageIds);
			Map<String, ProductDO> productMap = productDao.findBySku(skus);
			Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(imageHeaders.getDocuments());
			for (ImageHeaderDO imageHeader : imageHeaders.getDocuments()) {
				ProductImageActivityVO vo = new ProductImageActivityVO();
				int likeCount = 0;
				int commentCount = 0;
				List<ImageHeaderDO> images = imageSetMap.get(imageHeader.getImageSetId());
				if(images == null || images.isEmpty()) continue;
				for (ImageHeaderDO detail : images) {
					if (likeCountMap.containsKey(detail.getImageId())) {
						likeCount += likeCountMap.get(detail.getImageId());
					}
					if (commentCountMap.containsKey(detail.getImageId())) {
						commentCount += commentCountMap.get(detail.getImageId());
					}
				}

				vo.setCommentCount(commentCount);
				vo.setImageHeaders(imageSetMap.get(imageHeader.getImageSetId()));
				vo.setLikeCount(likeCount);
				vo.setPostDate(imageHeader.getPostDate());
				vo.setProduct(productMap.get(imageHeader.getSku()));

				result.updateFirstAndLast(vo);
				if (imageHeader.isStop(communityUserId, stopCommunityUserIds)) {
					result.countUpStopContents();
					continue;
				}
				result.getDocuments().add(vo);
			}

		}
		return result;
	}

	/**
	 * 指定した画像をインデックス情報から返します。
	 * @param imageId 画像ID
	 * @return 画像情報
	 */
	@Override
	@ArroundSolr
	public ImageDetailSetVO getImageHeaderFromIndex(
			String imageId,
			boolean includeDeleteContents) {
		ImageHeaderDO imageHeader = imageDao.loadImageHeaderFromIndex(imageId, includeDeleteContents);
		if (imageHeader == null) {
			return null;
		}
		ImageDetailSetVO vo = new ImageDetailSetVO();
		vo.setImageHeader(imageHeader);
		return vo;
	}

	/**
	 * 画像を仮登録します。
	 * @param image 画像情報
	 * @return 仮登録した画像情報
	 */
	@Override
	@ArroundHBase
	public ImageDO createTemporaryImage(ImageDO image) {
		imageDao.createTemporaryImage(image);
		return image;
	}

	/**
	 * 仮登録した画像を更新します。
	 * @param image 画像情報
	 * @return 更新した仮登録画像情報
	 */
	@Override
	@ArroundHBase
	public ImageDO updateTemporaryImage(ImageDO image) {
		ImageDO dbImage = imageDao.loadImage(image.getImageId(),
				Path.includeProp("imageId,temporaryFlag"));
		if (!dbImage.isTemporaryFlag()) {
			throw new IllegalArgumentException(
					"This image is not temporary. imageId = " + image.getImageId());
		}
		imageDao.updateImage(image, Path.includeProp(
				"data,width,heigth,mimeType,modifyDateTime"));
		return image;
	}

	/**
	 * 指定した仮登録されている画像情報を返します。
	 * @param imageId 画像ID
	 * @return 仮登録した画像情報
	 */
	@Override
	@ArroundHBase
	public ImageDO getTemporaryImage(String imageId) {
		ImageDO image = imageDao.loadImage(imageId, Path.DEFAULT);
		if (image.isTemporaryFlag() && !image.isDeleted()) {
			return image;
		}
		return null;
	}
	
	@Override
	public List<ImageDO> getTemporaryImages(List<String> imageIds) {
		List<ImageDO> images = imageDao.loadImages(imageIds, Path.DEFAULT);
		List<ImageDO> result = Lists.newArrayList();
		for( ImageDO image : images){
			if (image.isTemporaryFlag() && !image.isDeleted()) {
				result.add(image);
			}
		}
		return result;
	}
	
	@Override
	public ImageDO getPermanentImage(String imageId) {
		return imageDao.loadImage(imageId, Path.DEFAULT);
	}
	
	

	/**
	 * 指定した仮登録されている画像情報を返します。
	 * @param imageId 画像ID
	 * @return 仮登録した画像情報
	 */
	@Override
	@ArroundHBase
	public ImageDO loadImage(String imageId) {
		return imageDao.loadImage(imageId, Path.DEFAULT);
	}

	/**
	 * 画像セットをアップロードします。
	 * @param sku 商品SKU
	 * @param imageHeaders 画像ヘッダーリスト
	 */
	@Override
	@ArroundHBase
	public List<ImageHeaderDO> saveImageSet(
			String sku,
			List<ImageHeaderDO> imageHeaders,
			Date inputPurchaseProductDate) {
		ProductDO product = productDao.loadProduct(sku);
		if (product == null) {
			throw new IllegalArgumentException("Product is null. sku = " + sku);
		}
		return saveImageSet(product, imageHeaders, inputPurchaseProductDate);
	}
	
	/**
	 * 画像セットをアップロードします。
	 * @param product 商品情報
	 * @param imageHeaders 画像ヘッダーリスト
	 */
	@Override
	@ArroundHBase
	@ArroundSolr
	public List<ImageHeaderDO> saveImageSet(
			ProductDO product,
			List<ImageHeaderDO> imageHeaders,
			Date inputPurchaseProductDate) {
		if (product == null) {
			throw new IllegalArgumentException("Product is null.");
		}
		String imageSetId = imageHeaders.get(0).getImageId();
		List<String> imageIds = new ArrayList<String>();

		CommunityUserDO communityUser = requestScopeDao.loadCommunityUser();

		// コンテンツ投稿可能チェック
		if(!userService.validateUserStatusForPostContents(communityUser.getCommunityUserId()))
			throw new UnActiveException("can not post contens because user status is failure " + " communityUserId:" + communityUser.getCommunityUserId());

		PurchaseProductDO purchaseProduct = orderDao.loadPurchaseProductBySku(
				communityUser.getCommunityUserId(),
				product.getSku(),
				Path.DEFAULT, true);
		
		String purchaseProductId = null;
		if (purchaseProduct == null || purchaseProduct.isDeleted()) {
			if (inputPurchaseProductDate == null) {
				throw new IllegalArgumentException(
						"PurchaseDate in imageHeader required.");
			}
			purchaseProduct = new PurchaseProductDO();
			purchaseProduct.setCommunityUser(communityUser);
			purchaseProduct.setPurchaseDate(inputPurchaseProductDate);
			purchaseProduct.setUserInputPurchaseDate(purchaseProduct.getPurchaseDate());
			purchaseProduct.setProduct(product);
			purchaseProduct.setPurchaseHistoryType(PurchaseHistoryType.OTHER);
			purchaseProduct.setAdult(product.isAdult());
			purchaseProduct.setPublicSetting(true);
			orderDao.createPurchaseProduct(purchaseProduct, false);
			purchaseProductId = purchaseProduct.getPurchaseProductId();
		}
		
		if (purchaseProduct.getPurchaseDate() == null) {
			throw new IllegalStateException(
					"PurchaseDate is null. communityUserId = "
					+ communityUser.getCommunityUserId()
					+ " sku = " + product.getSku());
		}

		for (int i = 0; i < imageHeaders.size(); i++) {
			ImageHeaderDO imageHeader = imageHeaders.get(i);

			imageHeader.setPurchaseDate(purchaseProduct.getPurchaseDate());
			imageHeader.setPurchaseHistoryType(purchaseProduct.getPurchaseHistoryType());
			imageHeader.setOwnerCommunityUser(communityUser);
			imageHeader.setPostContentType(PostContentType.IMAGE_SET);
			imageHeader.setSku(product.getSku());
			imageHeader.setAdult(product.isAdult());
			imageHeader.setImageSetIndex(i);
			imageHeader.setImageSetId(imageSetId);
			if (i == 0) {
				imageHeader.setListViewFlag(true);
			}
			imageDao.saveAndUploadImage(imageHeader, true);
			imageIds.add(imageHeader.getImageId());
		}

		List<ActionHistoryDO> actionHistories = new ArrayList<ActionHistoryDO>();

		ActionHistoryDO userActionHistory = new ActionHistoryDO();
		userActionHistory.setActionHistoryType(ActionHistoryType.USER_IMAGE);
		userActionHistory.setCommunityUser(requestScopeDao.loadCommunityUser());
		userActionHistory.setImageSetId(imageSetId);
		userActionHistory.setAdult(product.isAdult());
		userActionHistory.setProduct(product);
		actionHistories.add(userActionHistory);

		//商品に対してアクションを記録します。
		ActionHistoryDO productActionHistory = new ActionHistoryDO();
		productActionHistory.setActionHistoryType(ActionHistoryType.PRODUCT_IMAGE);
		productActionHistory.setCommunityUser(requestScopeDao.loadCommunityUser());
		productActionHistory.setImageSetId(imageSetId);
		productActionHistory.setAdult(product.isAdult());
		productActionHistory.setProduct(product);
		actionHistories.add(productActionHistory);

		actionHistoryDao.create(actionHistories);

		indexService.updateIndexForSaveImageSet(
				imageIds.toArray(new String[imageIds.size()]),
				purchaseProductId,
				userActionHistory.getActionHistoryId(),
				productActionHistory.getActionHistoryId());

		mailService.sendNotifyMailForJustAfterImageSubmit(
				imageSetId, product.getSku(), communityUser.getCommunityUserId());

		socialMediaService.notifySocialMediaForImageSubmit(
				imageSetId, product.getSku(), communityUser.getCommunityUserId());

		return imageHeaders;
	}

	@Override
	@ArroundHBase
	@ArroundSolr
	public ImageSetDO saveImageSet(ImageSetDO imageSet) {
		if (imageSet.getProduct() == null) {
			throw new IllegalArgumentException("Product is null.");
		}
		
		String communityUserId = imageSet.getCommunityUser().getCommunityUserId();
		// コンテンツ投稿可能チェック
		if(!userService.validateUserStatusForPostContents(communityUserId))
			throw new UnActiveException("can not post contens because user status is failure " + " communityUserId:" + imageSet.getCommunityUser().getCommunityUserId());
		
		// 購入商品チェック
		PurchaseProductDO purchaseProduct = orderDao.loadPurchaseProductBySku(
				imageSet.getCommunityUser().getCommunityUserId(),
				imageSet.getProduct().getSku(),
				Path.DEFAULT, true);
		
		String purchaseProductId = null;
		if (purchaseProduct == null || purchaseProduct.isDeleted()) {
			if (imageSet.getInputPurchaseProductDate() == null) {
				throw new IllegalArgumentException(
						"PurchaseDate in imageHeader required.");
			}
			purchaseProduct = new PurchaseProductDO();
			purchaseProduct.setCommunityUser(imageSet.getCommunityUser());
			purchaseProduct.setPurchaseDate(imageSet.getInputPurchaseProductDate());
			purchaseProduct.setUserInputPurchaseDate(imageSet.getInputPurchaseProductDate());
			purchaseProduct.setProduct(imageSet.getProduct());
			purchaseProduct.setPurchaseHistoryType(PurchaseHistoryType.OTHER);
			purchaseProduct.setAdult(imageSet.getProduct().isAdult());
			purchaseProduct.setPublicSetting(true);
			orderDao.createPurchaseProduct(purchaseProduct, false);
			purchaseProductId = purchaseProduct.getPurchaseProductId();
		}
		
		if (purchaseProduct.getPurchaseDate() == null) {
			throw new IllegalStateException(
					"PurchaseDate is null. communityUserId = "
					+ imageSet.getCommunityUser().getCommunityUserId()
					+ " sku = " + imageSet.getProduct().getSku());
		}
		
		imageSet.setPurchaseProduct(purchaseProduct);
		
		// 更新時の画像データ削除処理
		List<ImageHeaderDO> oldImageHeaders = imageDao.findImageByContentId(
				imageSet.getImageSetId(),
				ImageTargetType.IMAGE, null,
				new ContentsStatus[] { ContentsStatus.SUBMITTED, ContentsStatus.CONTENTS_STOP });
		
		// 削除処理
		List<String> exixtImageIds = Lists.newArrayList();
		List<String> deleteImageIds = Lists.newArrayList();
		for( ImageHeaderDO oldImageHeader : oldImageHeaders){
			if( existImageIdWithImageHeader(imageSet.getImageHeaders(), oldImageHeader.getImageId()) ){
				exixtImageIds.add(oldImageHeader.getImageId());
			}else{
				// 削除処理
				deleteImageIds.add(oldImageHeader.getImageId());
				imageDao.deleteBothImage(PostContentType.IMAGE_SET, oldImageHeader.getImageSetId(), oldImageHeader.getImageId(), true, false, ContentsStatus.DELETE);
			}
		}
		
		List<String> imageIds = new ArrayList<String>();
		for (int i = 0; i < imageSet.getImageHeaders().size(); i++) {
			ImageHeaderDO imageHeader = imageSet.getImageHeaders().get(i);
			imageHeader.setPurchaseDate(purchaseProduct.getPurchaseDate());
			imageHeader.setPurchaseHistoryType(purchaseProduct.getPurchaseHistoryType());
			imageHeader.setOwnerCommunityUser(imageSet.getCommunityUser());			
			imageHeader.setPostContentType(PostContentType.IMAGE_SET);
			imageHeader.setSku(imageSet.getProduct().getSku());
			imageHeader.setAdult(imageSet.getProduct().isAdult());
			imageHeader.setImageSetIndex(i);
			imageHeader.setImageSetId(imageSet.getImageSetId());
			imageHeader.setListViewFlag(i == 0);
			
			if( existImageIdWithImageId(exixtImageIds, imageHeader.getImageId()) ){
				// 更新処理（データ）
				imageDao.updateImageHeader(imageHeader, Path.includeProp("listViewFlag, imageSetIndex, comment, modifyDateTime"));
			}else{
				// 新規追加
				imageDao.saveAndUploadImage(imageHeader, true);
			}
			
			imageIds.add(imageHeader.getImageId());
		}
		
		// 新規追加のときのみ実行する。
		String userActionHistoryId = null;
		String productActionHistoryId = null;
		if( oldImageHeaders == null || oldImageHeaders.isEmpty() ){
			List<ActionHistoryDO> actionHistories = Lists.newArrayList();
			ActionHistoryDO userActionHistory = new ActionHistoryDO();
			userActionHistory.setActionHistoryType(ActionHistoryType.USER_IMAGE);
			userActionHistory.setCommunityUser(imageSet.getCommunityUser());
			userActionHistory.setImageSetId(imageSet.getImageSetId());
			userActionHistory.setAdult(imageSet.getProduct().isAdult());
			userActionHistory.setProduct(imageSet.getProduct());
			actionHistories.add(userActionHistory);
			
			//商品に対してアクションを記録します。
			ActionHistoryDO productActionHistory = new ActionHistoryDO();
			productActionHistory.setActionHistoryType(ActionHistoryType.PRODUCT_IMAGE);
			productActionHistory.setCommunityUser(imageSet.getCommunityUser());
			productActionHistory.setImageSetId(imageSet.getImageSetId());
			productActionHistory.setAdult(imageSet.getProduct().isAdult());
			productActionHistory.setProduct(imageSet.getProduct());
			actionHistories.add(productActionHistory);
			actionHistoryDao.create(actionHistories);
			
			userActionHistoryId = userActionHistory.getActionHistoryId();
			productActionHistoryId = productActionHistory.getActionHistoryId();
			
			mailService.sendNotifyMailForJustAfterImageSubmit(
					imageSet.getImageSetId(), imageSet.getProduct().getSku(), imageSet.getCommunityUser().getCommunityUserId());

			socialMediaService.notifySocialMediaForImageSubmit(
					imageSet.getImageSetId(), imageSet.getProduct().getSku(), imageSet.getCommunityUser().getCommunityUserId());
		}
		
		List<String> updateImageIds = Lists.newArrayList(imageIds);
		updateImageIds.addAll(deleteImageIds);
		
		indexService.updateIndexForSaveImageSet(
				updateImageIds.toArray(new String[updateImageIds.size()]),
				purchaseProductId,
				userActionHistoryId,
				productActionHistoryId);
		
		return imageSet;
	}
	
	
	
	@Override
	@ArroundHBase
	@ArroundSolr
	public void deleteImageSet(
			String communityUserId,
			ImageTargetType imageTargetType, 
			String contentsId,
			boolean mngToolOperation) {
		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();
		if( loginCommunityUserId == null || !loginCommunityUserId.equals(communityUserId)){
			throw new SecurityException(
					"This review is different owner. ownerId = " +
							communityUserId +
					" input = " + loginCommunityUserId);
		}
		
		PostContentType postContentType = null;
		
		if( ImageTargetType.REVIEW.equals(imageTargetType)){
			postContentType = PostContentType.REVIEW;
		}else if( ImageTargetType.QUESTION.equals(imageTargetType)){
			postContentType = PostContentType.QUESTION;
		}else if( ImageTargetType.QUESTION_ANSWER.equals(imageTargetType)){
			postContentType = PostContentType.ANSWER;
		}else if( ImageTargetType.IMAGE.equals(imageTargetType)){
			postContentType = PostContentType.IMAGE_SET;
		}else{
			throw new IllegalArgumentException("ImageTargetType is Different.");
		}
		
		for (ImageHeaderDO imageHeader : imageDao.findImageByContentIdWithCommunityUserId(
				communityUserId,
				contentsId, 
				imageTargetType,
				null,
				new ContentsStatus[]{ContentsStatus.SUBMITTED, ContentsStatus.CONTENTS_STOP})) {
			imageDao.deleteBothImage(
					postContentType, 
					imageHeader.getContentsId(),
					imageHeader.getImageId(),
					true,
					mngToolOperation,
					ContentsStatus.DELETE);
			imageDao.updateImageInIndex(imageHeader.getImageId(), false, mngToolOperation);
		}
	}

	private boolean existImageIdWithImageId(List<String> existImageIds, String imageId){
		for( String exixtImageId : existImageIds){
			if( exixtImageId.equals(imageId)){
				return true;
			}
		}
		return false;
	}
	
	private boolean existImageIdWithImageHeader(List<ImageHeaderDO> imageHeaders, String imageId){
		for( ImageHeaderDO imageHeader : imageHeaders){
			if( imageHeader.getImageId().equals(imageId)){
				return true;
			}
		}
		return false;
	}

	@Override
	@ArroundHBase
	public ImageSetDO modifyImageSet(ImageSetDO imageSet) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 画像セットの中から指定した画像を削除します。
	 * @param imageId 画像ID
	 */
	@Override
	@ArroundHBase
	public void deleteImageInImageSet(String imageId) {
		deleteImageInImageSet(imageId, false);
	}
	
	/**
	 * 画像セットの中から指定した画像を削除します。
	 * @param imageId 画像ID
	 */
	@Override
	@ArroundHBase
	@ArroundSolr
	public void deleteImageInImageSet(String imageId, boolean mngToolOperation) {

		ImageHeaderDO imageHeader = imageDao.loadImageHeader(imageId);

		try{
			checkUpdateImage(imageHeader, mngToolOperation);
		}catch(IllegalArgumentException e){
			throw new IllegalArgumentException(e.getMessage() + " imageId = " + imageId);
		}catch(SecurityException e){
			throw new SecurityException(e.getMessage() + " imageId = " + imageId);
		}catch(UnActiveException e){
			throw new UnActiveException(e.getMessage() + " imageId = " + imageId);
		}

		applicationLockDao.lockForDeleteImageInImageSet(
				imageHeader.getImageSetId(),
				imageHeader.getCommunityUser().getCommunityUserId());
		
		imageDao.deleteBothImage(
				PostContentType.IMAGE_SET,
				imageHeader.getImageSetId(),
				imageId,
				true,
				mngToolOperation,
				ContentsStatus.DELETE);

		String tumbnailImageId = null;
		if(!StringUtils.isEmpty(imageHeader.getThumbnailImageId())){
			imageDao.deleteBothImage(
					PostContentType.IMAGE_SET,
					imageHeader.getImageSetId(),
					imageHeader.getThumbnailImageId(),
					true,
					mngToolOperation,
					ContentsStatus.DELETE);
			tumbnailImageId = imageHeader.getThumbnailImageId();
		}

		if (imageHeader.isListViewFlag()) {
			imageDao.updateListViewFlag(imageId, false);
		}

		ImageHeaderDO next = imageDao.loadTopImage(imageHeader.getImageSetId());

		String nextListViewImageId = null;
		
		if (next == null) {
			imageDao.deleteImageSetActionHistory(imageHeader.getImageSetId());
		} else if(!next.isListViewFlag()) {
			imageDao.updateListViewFlag(next.getImageId(), true);
			nextListViewImageId = next.getImageId();
		}

		indexService.updateIndexForUpdateImageSet(
				imageHeader.getImageSetId(),
				imageId,
				tumbnailImageId,
				nextListViewImageId,
				(next == null));
	}
	
	/**
	 * 指定した画像のコメントを編集します。
	 * @param imageId 画像ID
	 * @param comment コメント
	 * @return 画像ヘッダー
	 */
	@Override
	@ArroundHBase
	@ArroundSolr
	public ImageHeaderDO updateImageComment(
			String imageId,
			String comment) {
		ImageHeaderDO imageHeader = imageDao.loadImageHeader(imageId);

		try{
			checkUpdateImage(imageHeader, false);
		}catch(IllegalArgumentException e){
			throw new IllegalArgumentException(e.getMessage() + " imageId = " + imageId);
		}catch(SecurityException e){
			throw new SecurityException(e.getMessage() + " imageId = " + imageId);
		}catch(UnActiveException e){
			throw new UnActiveException(e.getMessage() + " imageId = " + imageId);
		}
		
		imageHeader.setComment(comment);
		
		imageDao.updateImageHeader(imageHeader, Path.includeProp("comment,modifyDateTime"));
		
		indexService.updateIndexForUpdateImageSet(
				imageHeader.getImageSetId(),
				imageId,
				null,
				null,
				false);
		return imageHeader;
	}
	
	/**
	 * 指定したコンテンツに紐づく画像を削除します。
	 * @param communityUserId コミュニティユーザーID
	 * @param contentType コンテンツタイプ
	 * @param contentsId コンテンツID
	 * @param contents コンテンツ
	 * @param updateImageIds 更新画像IDのリスト
	 */
	@Override
	@ArroundHBase
	public void deleteImagesInContents(
			String communityUserId,
			PostContentType contentType,
			String contentsId,
			TextEditableContents contents,
			Set<String> updateImageIds) {
		HTMLConverter imageHolder = createHTMLConverter(null);
		imageHolder.sanitizeHtml(contents.getTextEditableText());
		//削除対象となる画像IDのリストです。
		List<String> deleteImageIds = new ArrayList<String>();
		for( ImageHeaderDO imageHeader : contents.getImageHeaders()){
			deleteImageIds.add(imageHeader.getImageId());
		}
		//削除対象となる画像IDのリストです。
		for (ImageUrlInfo imageData : imageHolder.getExistsImageUrlInfoList()) {
			if (imageData.isTemporary()) {
				deleteImageIds.add(imageData.getImageId());
			}
		}

		Set<String> validImageIds = new HashSet<String>();
		validImageIds.addAll(deleteImageIds);

		validImageIds = imageDao.validateImageIds(validImageIds,communityUserId);
		dropInvalidIds(validImageIds, deleteImageIds);
		
		if (contents.getStatus().equals(ContentsStatus.SAVE)) {
			//除去された画像を物理削除します。
			if (deleteImageIds.size() > 0) {
				imageDao.deleteImages(deleteImageIds);
			}
		} else {
			//除去された画像を物理削除します。
			if (deleteImageIds.size() > 0) {
				for (String deleteImageId : deleteImageIds) {
					imageDao.deleteBothImage(contentType, contentsId, deleteImageId, true, false, ContentsStatus.DELETE);
				}
				updateImageIds.addAll(deleteImageIds);
			}
		}
	}

	/**
	 * 指定した質問に紐付く質問回答の画像を削除します。
	 * @param questionId 質問ID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS, asyncMessageType=AsyncMessageType.INSTANCYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void deleteQuestionAnswerImageByQuestionId(String questionId) {
		for (ImageHeaderDO imageHeader : imageDao.findImageHeaderAllByQuestionId(questionId)) {
			if (imageHeader.getPostContentType() != PostContentType.ANSWER) {
				continue;
			}
			imageDao.deleteBothImage(PostContentType.ANSWER, imageHeader.getContentsId(),
					imageHeader.getImageId(), true, false, ContentsStatus.DELETE);
			imageDao.updateImageInIndex(imageHeader.getImageId(), true, false);
		}
	}

	/**
	 * 初期化します。
	 * @throws Exception 例外が発生した場合
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		temporaryImageUrlPattern = Pattern.compile(
				"^" + resourceConfig.temporaryImageUrl + "[\\da-zA-Z\\-\\.]+$");
		uploadedImageUrlPattern = Pattern.compile(
				"^" + resourceConfig.imageUrl + resourceConfig.imageUploadPath + "[\\da-zA-Z\\-\\/\\.]+$");
	}

	/**
	 * 画像サーバとの同期エラーが出ているものを全て同期します。
	 */
	@Override
	@ArroundSolr
	@ArroundHBase
	public void recoverImageSyncError() {
		imageDao.recoverImageSyncError();
	}

	/**
	 * 画像情報のスコア情報を更新します。
	 * @param targetDate 対象日付
	 * @param imageHeader 画像ヘッダー
	 * @param scoreFactor スコア係数
	 */
	@Override
	@ArroundSolr
	@ArroundHBase
	public void updateImageScoreForBatch(
			Date targetDate,
			ImageHeaderDO image,
			ScoreFactorDO scoreFactor) {
		int elapsedDays = DateUtil.getElapsedDays(image.getPostDate());
		long commentCount = 0;
		List<String> imageIds = new ArrayList<String>();
		imageIds.add(image.getImageId());
		Map<String, Long> commentCountMap = commentDao.loadImageCommentCountMap(imageIds, image.getOwnerCommunityUserId());
		if (commentCountMap.containsKey(image.getImageId())) {
			commentCount = commentCountMap.get(image.getImageId());
		}
		long likeCount = 0;
		Map<String, Long> likeCountMap = likeDao.loadImageLikeCountMap(imageIds);
		if (likeCountMap.containsKey(image.getImageId())) {
			likeCount = likeCountMap.get(image.getImageId());
		}

		DailyScoreFactorDO dailyScoreFactor = new DailyScoreFactorDO();
		dailyScoreFactor.setType(DailyScoreFactorType.IMAGE);
		dailyScoreFactor.setTargetDate(targetDate);
		dailyScoreFactor.setContentsId(image.getImageId());
		dailyScoreFactor.setSku(image.getProduct().getSku());
		dailyScoreFactor.setElapsedDays(elapsedDays);
		dailyScoreFactor.setCommentCount(commentCount);
		dailyScoreFactor.setLikeCount(likeCount);
		dailyScoreFactor.setPostDate(image.getPostDate());
		dailyScoreFactorDao.createDailyScoreFactorForBatch(dailyScoreFactor);
	}
	@Override
	@ArroundSolr
	@ArroundHBase
	public void updateImageScoreForBatchBegin(int bulkSize) {
		dailyScoreFactorDao.createDailyScoreFactorForBatchBegin(bulkSize);
	}
	@Override
	@ArroundSolr
	@ArroundHBase
	public void updateImageScoreForBatchEnd() {
		dailyScoreFactorDao.createDailyScoreFactorForBatchEnd();
	}

	/**
	 * HTMLコンバーターを生成して返します。
	 * @param imageConverterMap 画像変換マップ（画像ID、画像URL）
	 * @return HTMLコンバーター
	 */
	private HTMLConverter createHTMLConverter(Map<String, String> imageConverterMap) {
		HTMLConverter converter = new HTMLConverter();
		converter.setTemporaryImageUrlPattern(temporaryImageUrlPattern);
		converter.setUploadedImageUrlPattern(uploadedImageUrlPattern);
		if (imageConverterMap != null) {
			converter.setConverter(new ImageUrlConverter(imageConverterMap));
		}
		return converter;
	}

	/**
	 * 不要なIDを削除します。
	 * @param validIds 正しいIDリスト
	 * @param ids チェックするIDリスト
	 */
	private void dropInvalidIds(Set<String> validIds, List<String> ids) {
		for (Iterator<String> it = ids.iterator(); it.hasNext();) {
			String id = it.next();
			if (!validIds.contains(id)) {
				it.remove();
			}
		}
	}

	/**
	 * 指定した画像を編集可能かチェックします。
	 * @param imageId 画像ID
	 * @param imageHeader 画像ヘッダー
	 */
	private void checkUpdateImage(ImageHeaderDO imageHeader, boolean mngToolOperation) {
		if (imageHeader == null || imageHeader.getImageId() == null|| imageHeader.getPostContentType() == null || imageHeader.isDeleted()) {
			throw new DataNotFoundException("Image is not found.");
		}
		if (!PostContentType.IMAGE_SET.equals(imageHeader.getPostContentType()) && 
				!PostContentType.REVIEW.equals(imageHeader.getPostContentType()) &&
				!PostContentType.QUESTION.equals(imageHeader.getPostContentType()) &&
				!PostContentType.ANSWER.equals(imageHeader.getPostContentType())) {
			throw new IllegalArgumentException(
					"This image type is invalid. imageId = " + imageHeader.getImageId()
					+ ", findType = " + imageHeader.getPostContentType().name());
		}

		if(!mngToolOperation){
			if (!imageHeader.getOwnerCommunityUserId().equals(requestScopeDao.loadCommunityUserId())) {
				throw new SecurityException("No authorized. imageId = " + imageHeader.getImageId()
						+ ", accessUserId = " + requestScopeDao.loadCommunityUserId());
			}
			// コンテンツ投稿可能チェック
			if(!userService.validateUserStatusForPostContents(requestScopeDao.loadCommunityUserId()))
				throw new UnActiveException("can not post contens because user status is failure " + " communityUserId:" + requestScopeDao.loadCommunityUserId());
		}
	}

	@Override
	public SearchResult<ImageSetVO> loadProductImageSummary(String imageSetId) {
		List<ImageHeaderDO> imageHeaders = imageDao.loadImages( imageSetId );
		ProductUtil.filterInvalidProduct(imageHeaders);
		if (imageHeaders.size() == 0) {
			return new SearchResult<ImageSetVO>();
		}

		Map<String, Long> countMap = imageDao.loadContentsImageCountMap(imageHeaders);
		SearchResult<ImageSetVO> result = new SearchResult<ImageSetVO>(
				imageHeaders.size(), new ArrayList<ImageSetVO>());
		result.setHasAdult(false);
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(imageHeaders);
		String communityUserId = requestScopeDao.loadCommunityUserId();
		for (ImageHeaderDO imageHeader : imageHeaders) {
			ImageSetVO vo = new ImageSetVO();
			vo.setImageHeader(imageHeader);
			imageHeader.setOwnerCommunityUser( communityUserDao.loadFromIndex( imageHeader.getOwnerCommunityUserId(), Path.DEFAULT ) );
			Long count = countMap.get(imageHeader.getImageId());
			if (count != null && count > 1)
				vo.setHasOtherImages(true);
			result.updateFirstAndLast(vo);
			if (imageHeader.isStop(communityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				continue;
			}
			result.getDocuments().add(vo);
		}
		return result;
	}

	@Override
	public boolean existsEffectiveImage(String imageSetId) {
		List<ImageHeaderDO> imageHeaders = imageDao.loadImages(imageSetId);
		if(imageHeaders == null || imageHeaders.isEmpty()) return false;

		for(ImageHeaderDO imageHeader:imageHeaders){
			if(imageHeader.getStatus().equals(ContentsStatus.SUBMITTED) && !imageHeader.isWithdraw()) return true;
		}
		return false;
	}

	@Override
	public boolean existsEffectiveImage(ImageTargetType imageTargetType, String contentId) {
		
		List<ImageHeaderDO> imageHeaders = imageDao.loadImages(imageTargetType, contentId);
		if(imageHeaders == null || imageHeaders.isEmpty()) return false;

		for(ImageHeaderDO imageHeader:imageHeaders){
			if(imageHeader.getStatus().equals(ContentsStatus.SUBMITTED) && !imageHeader.isWithdraw()) return true;
		}
		return false;
	}

	@Override
	public ImageHeaderDO loadImageHeader(String imageId) {
		return imageDao.loadImageHeader(imageId);
	}

	@Override
	public ImageHeaderDO loadImageHeaderFromIndex(String imageId, boolean includeDeleteContents) {
		ImageHeaderDO imageHeader =  imageDao.loadImageHeaderFromIndex(imageId, includeDeleteContents);
		if (ProductUtil.invalid(imageHeader)) {
			return null;
		} else {
			return imageHeader;
		}
	}

	@Override
	public String findProductSku(String imageId) {
		return imageDao.findProductSku(imageId);
	}
	
	@Override
	public List<ImageHeaderDO> loadImagesByContentId(PostContentType postContentType, String contentId){
		return imageDao.loadImagesFromIndex(postContentType, contentId);
	}
	
}
