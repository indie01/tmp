package com.kickmogu.yodobashi.community.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.resource.dao.CommentDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.ImageDao;
import com.kickmogu.yodobashi.community.resource.dao.LikeDao;
import com.kickmogu.yodobashi.community.resource.dao.OrderDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionAnswerDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.ReviewDao;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SaveImageDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.TextEditableContents;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommentTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.LikeTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointIncentiveType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointQuestType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PurchaseHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewType;
import com.kickmogu.yodobashi.community.service.CommonService;
import com.kickmogu.yodobashi.community.service.vo.CommentSetVO;
import com.kickmogu.yodobashi.community.service.vo.PurchaseProductSetVO;

public abstract class AbstractServiceImpl implements CommonService {
	
	@Autowired
	protected ImageDao imageDao;
	
	/**
	 * いいね DAO です。
	 */
	@Autowired
	protected LikeDao likeDao;
	
	@Autowired
	protected OrderDao orderDao;
	
	/**
	 * 商品 DAO です。
	 */
	@Autowired @Qualifier("catalog")
	protected ProductDao productDao;
	
	/**
	 * レビュー DAO です。
	 */
	@Autowired
	protected ReviewDao reviewDao;
	
	/**
	 * コメント DAO です。
	 */
	@Autowired
	protected CommentDao commentDao;
	
	@Autowired
	protected QuestionAnswerDao questionAnswerDao;
	
	/**
	 * コミュニティユーザー DAO です。
	 */
	@Autowired
	protected CommunityUserDao communityUserDao;
	
	/**
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	protected RequestScopeDao requestScopeDao;
	
	/**
	 * 指定したレビューに対するいいねを返します。
	 * @param reviewId レビューID
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param limit 最大取得件数
	 * @return いいねリスト
	 */
	@Override
	@ArroundSolr
	public SearchResult<LikeDO> findLikeByReviewId(
			String reviewId, String excludeCommunityUserId, int limit) {
		return filterStopUsers(likeDao.findLikeByContentsId(
				LikeTargetType.REVIEW, excludeCommunityUserId, reviewId, limit));
	}

	/**
	 * 指定した画像に対するいいねを返します。
	 * @param imageId 画像ID
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param limit 最大取得件数
	 * @return いいねリスト
	 */
	@Override
	@ArroundSolr
	public SearchResult<LikeDO> findLikeByImageId(
			String imageId, String excludeCommunityUserId, int limit) {
		return filterStopUsers(likeDao.findLikeByContentsId(
				LikeTargetType.IMAGE, excludeCommunityUserId, imageId, limit));
	}
	/**
	 * 指定した質問回答に対するいいねを返します。
	 * @param questionAnswerId 質問回答ID
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param limit 最大取得件数
	 * @return いいねリスト
	 */
	@Override
	@ArroundSolr
	public SearchResult<LikeDO> findLikeByQuestionAnswerId(
			String questionAnswerId, String excludeCommunityUserId, int limit) {
		return filterStopUsers(likeDao.findLikeByContentsId(LikeTargetType.QUESTION_ANSWER, excludeCommunityUserId, questionAnswerId, limit));
	}
	
	/**
	 * 指定したレビューに対するコメントを返します。
	 * @param reviewId レビューID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return コメントリスト
	 */
	@Override
	public SearchResult<CommentSetVO> findReviewCommentByReviewId(
			String reviewId, List<String> excludeCommentIds, int limit, Date offsetTime, boolean previous) {
		return findCommentByContentsId(
				CommentTargetType.REVIEW,
				reviewId,
				excludeCommentIds,
				limit,
				offsetTime,
				previous);
	}
	
	/**
	 * 指定した画像に対するコメントを返します。
	 * @param imageId 画像ID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return コメントリスト
	 */
	@Override
	public SearchResult<CommentSetVO> findImageCommentByImageId(
			String imageId, List<String> excludeCommentIds, int limit, Date offsetTime, boolean previous) {
		return findCommentByContentsId(
				CommentTargetType.IMAGE,
				imageId,
				excludeCommentIds,
				limit,
				offsetTime,
				previous);
	}

	

	/**
	 * 指定した質問回答に対するコメントを返します。
	 * @param questionAnswerId 質問回答ID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return コメントリスト
	 */
	@Override
	public SearchResult<CommentSetVO> findQuestionAnswerCommentByQuestionAnswerId(
			String questionAnswerId, List<String> excludeCommentIds, int limit, Date offsetTime, boolean previous) {
		return findCommentByContentsId(
				CommentTargetType.QUESTION_ANSWER,
				questionAnswerId,
				excludeCommentIds,
				limit,
				offsetTime,
				previous);
	}
	
	/**
	 * 指定の質問に対して、指定したコミュニティユーザーが回答しているかどうか
	 * 返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param questionId 質問ID
	 * @return 回答している場合、true
	 */
	@Override
	public Map<String, Boolean> hasQuestionAnswer(String communityUserId, List<String> questionIds) {
		Map<String, Boolean> resultMap = new HashMap<String, Boolean>();
		
		if( StringUtils.isEmpty(communityUserId) || questionIds == null || questionIds.isEmpty() )
			return resultMap;
		
		List<QuestionAnswerDO> questionAnswers = questionAnswerDao.findQuestionAnswerByCommunityUserIdAndQuestionIds(
				communityUserId, questionIds,
				Path.includeProp("communityUserId,questionId,status,withdraw"));
		
		if( questionAnswers == null || questionAnswers.isEmpty())
			return resultMap;
		
		for(String questionId : questionIds){
			for(QuestionAnswerDO questionAnswer : questionAnswers){
				if( !questionId.equals(questionAnswer.getQuestion().getQuestionId()))
					continue;
				
				if( questionAnswer.isDeleted() )
					break;
				
				if (ContentsStatus.SUBMITTED.equals(questionAnswer.getStatus()) || ContentsStatus.CONTENTS_STOP.equals(questionAnswer.getStatus())) {
					resultMap.put(questionId, Boolean.TRUE);
					break;
				}
			}
			
			if( !resultMap.containsKey(questionId))
				resultMap.put(questionId, Boolean.FALSE);
		}
		return resultMap;
	}
	
	/**
	 * 一時停止ユーザーをフィルタリングします。
	 * @param searchResult 検索結果
	 * @return フィルタリングした結果
	 */
	protected SearchResult<LikeDO> filterStopUsers(
			SearchResult<LikeDO> searchResult) {
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(searchResult.getDocuments());
		String communityUserId = requestScopeDao.loadCommunityUserId();
		for (Iterator<LikeDO> it = searchResult.getDocuments().iterator(); it.hasNext(); ) {
			LikeDO like = it.next();
			searchResult.updateFirstAndLast(like);
			if (like.isStop(communityUserId, stopCommunityUserIds)) {
				it.remove();
				searchResult.countUpStopContents();
			}
		}
		return searchResult;
	}
	
	/**
	 * 指定したコンテンツに対するコメントを返します。
	 * @param type タイプ
	 * @param contentsId コンテンツID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return コメントリスト
	 */
	private SearchResult<CommentSetVO> findCommentByContentsId(
			CommentTargetType type,
			String contentsId,
			List<String> excludeCommentIds,
			int limit,
			Date offsetTime,
			boolean previous) {
		SearchResult<CommentDO> searchResult = commentDao.findCommentByContentsId(
				type,
				contentsId,
				excludeCommentIds,
				limit,
				offsetTime,
				previous);
		
		SearchResult<CommentSetVO> result = new SearchResult<CommentSetVO>();
		result.setHasAdult(searchResult.isHasAdult());
		result.setNumFound(searchResult.getNumFound());
		
		String communityUserId = requestScopeDao.loadCommunityUserId();
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(searchResult.getDocuments());
		for (CommentDO comment : searchResult.getDocuments()) {
			CommentSetVO vo = new CommentSetVO();
			vo.setComment(comment);
			if (communityUserId != null) {
				vo.setCommentFlg(communityUserId.equals(comment.getCommunityUser().getCommunityUserId()));
			}
			result.updateFirstAndLast(vo);
			if (comment.isStop(communityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				continue;
			}
			result.getDocuments().add(vo);
		}
		return result;
	}
	
	/**
	 * 指定した形式のレビューを投稿可能かどうか判定します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @param reviewType レビュータイプ
	 * @return 投稿可能な場合、true
	 */
	@Override
	@ArroundHBase
	public boolean canPostReview(
			String communityUserId, String sku, ReviewType reviewType) {
		ProductDO product = productDao.loadProduct(sku);
		if (product == null || !product.isCanReview()) {
			return false;
		}
		Date now = new Date();
		if (reviewType.equals(ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE)) {
			ReviewDO review = null;
			for (ReviewDO target : reviewDao.findReviewByCommunityUserIdAndSKU(communityUserId, sku, Path.includeProp("reviewType,status,effective"))) {
				if (target.getReviewType().equals(ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE) && !target.isDeleted()) {
					review = target;
				} else if (target.getReviewType().equals(ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE) && target.isEffective()) {
					return false;
				} else if (target.getReviewType().equals(ReviewType.REVIEW_AFTER_FEW_DAYS)) {
					return false;
				}
			}
			if (review != null && review.getStatus().equals(ContentsStatus.SAVE)) {
				return true;
			} else if (review != null) {
				return false;
			} else {
				PurchaseProductDO purchaseProduct = orderDao.loadPurchaseProductBySku(
						communityUserId, sku,
						Path.DEFAULT, false);
				if (purchaseProduct == null
						|| purchaseProduct.isDeleted()
						|| purchaseProduct.getPurchaseDate() == null
						|| purchaseProduct.getProduct() == null
						|| product.getGrantPointReviewTerm(purchaseProduct.getPurchaseDate(), now) != 0) {
					return false;
				} else {
					return true;
				}
			}

		} else {
			PurchaseProductDO purchaseProduct = orderDao.loadPurchaseProductBySku(
					communityUserId, sku,
					Path.DEFAULT, false);
			if (purchaseProduct == null
					|| purchaseProduct.isDeleted()
					|| purchaseProduct.getPurchaseDate() == null
					|| purchaseProduct.getProduct() == null
					|| purchaseProduct.getProduct().getGrantPointReviewTerm(purchaseProduct.getPurchaseDate(), now ) < 0) {
				return false;
			} else if (purchaseProduct.getProduct().getGrantPointReviewTerm(purchaseProduct.getPurchaseDate(), now ) == 0) {
				ReviewDO review = null;
				for (ReviewDO target : reviewDao.findReviewByCommunityUserIdAndSKU(
						communityUserId, sku, Path.includeProp("reviewType,status"))) {
					if (target.getReviewType().equals(ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE)) {
						review = target;
						break;
					}
				}
				//購入直後でなくても、購入直後レビューが投稿済みの場合、経過レビューが投稿可能となります。
				//投稿ステータスか削除ステータスの場合、投稿済みと判定。
				//※一時保存を削除した場合は物理削除となるため、削除ステータスのものは一度投稿したものとみなせるため
				return review != null && !review.getStatus().equals(ContentsStatus.SAVE);
			} else {
				return true;
			}
		}
	}

	@Override
	@ArroundHBase
	public boolean canPostReview(
			String communityUserId,
			ProductDO product,
			PurchaseProductDO purchaseProduct,
			ReviewType reviewType) {
		if (product == null || !product.isCanReview()) {
			return false;
		}
		Date now = new Date();
		if (ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE.equals(reviewType)) {
			List<ReviewDO> reviews = reviewDao.findReviewByCommunityUserIdAndSKU(
					communityUserId,
					product.getSku(),
					Path.includeProp("reviewType,status,effective,purchaseHistoryType").includePath("product.sku").depth(1));
			for (ReviewDO target : reviews) {
				// YC購入のレビューなら、ここで比較する。他社購入なら再度第一印象レビューを行えるようにする。
				if( PurchaseHistoryType.YODOBASHI.equals(purchaseProduct.getPurchaseHistoryType())){
					if( PurchaseHistoryType.OTHER.equals(target.getPurchaseHistoryType()) )
						continue;
				}
				if (ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE.equals(target.getReviewType())){
					if( !target.isDeleted() ){
						return ContentsStatus.SAVE.equals(target.getStatus());
					}
					if( target.isEffective() ){
						return false;
					}
				} else if (ReviewType.REVIEW_AFTER_FEW_DAYS.equals(target.getReviewType())) {
					return false;
				}
			}
			
			if (purchaseProduct == null
					|| purchaseProduct.isDeleted()
					|| purchaseProduct.getPurchaseDate() == null
					|| purchaseProduct.getProduct() == null
					|| product.getGrantPointReviewTerm(purchaseProduct.getPurchaseDate(), now) != 0) {
				return false;
			} else {
				return true;
			}
		} else {
			if (purchaseProduct == null
					|| purchaseProduct.isDeleted()
					|| purchaseProduct.getPurchaseDate() == null
					|| purchaseProduct.getProduct() == null
					|| purchaseProduct.getProduct().getGrantPointReviewTerm(purchaseProduct.getPurchaseDate(), now ) < 0) {
				return false;
			} else if (purchaseProduct.getProduct().getGrantPointReviewTerm(purchaseProduct.getPurchaseDate(), now ) == 0) {
				for (ReviewDO target : reviewDao.findReviewByCommunityUserIdAndSKU(
						communityUserId,
						product.getSku(),
						Path.includeProp("reviewType,status").includePath("product.sku").depth(1))) {
					if (ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE.equals(target.getReviewType())) {
						return !ContentsStatus.SAVE.equals(target.getStatus());
					}
				}
				//購入直後でなくても、購入直後レビューが投稿済みの場合、経過レビューが投稿可能となります。
				//投稿ステータスか削除ステータスの場合、投稿済みと判定。
				//※一時保存を削除した場合は物理削除となるため、削除ステータスのものは一度投稿したものとみなせるため
				return false;
			} else {
				return true;
			}
		}
	}
	
	protected void settingPointInformation(
			String communityUserId, 
			Map<String, Long> myReviewCountMap,
			Map<String, Long> reviewCountMap,
			Map<String, Long> questionCountMap,
			List<PurchaseProductSetVO> purchaseProductSets){
		
		// ポイント付与判断およびポイント情報
		List<String> accountSharingCommunityUserIds = communityUserDao.findCommunityUserIdWithAccountSharingByCommunityUserId(communityUserId);
		List<Map<String, Object>> leniencePointGrantReviewInputList = Lists.newArrayList();
		
		for (PurchaseProductSetVO vo : purchaseProductSets) {
			if (!PurchaseHistoryType.YODOBASHI.equals(vo.getPurchaseProduct().getPurchaseHistoryType()))
				continue;
			int reviewTerm = vo.getPurchaseProduct().getProduct().getGrantPointReviewTerm(
					vo.getPurchaseProduct().getPurchaseDate(),
					new Date());
			if(reviewTerm == 0) {
				if(!canPostReview(communityUserId, vo.getPurchaseProduct().getProduct().getSku(), ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE)) {
					reviewTerm = -1;
				}
			}
//			int effectiveReviewTerm = ReviewDO.getEffectiveReviewTerm(vo.getPurchaseProduct().getProduct());
			if (!vo.getPurchaseProduct().getProduct().isGrantPointWithinTerm(vo.getPurchaseProduct().getPurchaseDate())) {
				vo.setReviewPointActive(false);
			} else {
				if (vo.getPurchaseProduct().isShare()) {
					for (String userId : accountSharingCommunityUserIds) {
						Map<String, Object> leniencePointGrantReviewParams = new HashMap<String, Object>();
						leniencePointGrantReviewParams.put("communityUserId", userId);
						leniencePointGrantReviewParams.put("product", vo.getPurchaseProduct().getProduct());
						leniencePointGrantReviewParams.put("reviewTerm", reviewTerm);
						leniencePointGrantReviewInputList.add(leniencePointGrantReviewParams);
					}
					vo.setReviewPointActive(true);
				} else {
					Map<String, Object> leniencePointGrantReviewParams = new HashMap<String, Object>();
					leniencePointGrantReviewParams.put("communityUserId", communityUserId);
					leniencePointGrantReviewParams.put("product", vo.getPurchaseProduct().getProduct());
					leniencePointGrantReviewParams.put("reviewTerm", reviewTerm);
					leniencePointGrantReviewInputList.add(leniencePointGrantReviewParams);
				}
			}
		}
		
		Map<String, Boolean> leniencePointGrantReviewsMap = reviewDao.isLeniencePointGrantReviews(leniencePointGrantReviewInputList);
		
		for (PurchaseProductSetVO vo : purchaseProductSets) {
			String sku = vo.getPurchaseProduct().getProduct().getSku();
			Long myReviewCountCount = null;
			Long reviewCount = null;
			Long questionCount = null;
			
			int reviewTerm = vo.getPurchaseProduct().getProduct().getGrantPointReviewTerm(
					vo.getPurchaseProduct().getPurchaseDate(),
					new Date());
			if(reviewTerm == 0) {
				if(!canPostReview(communityUserId, vo.getPurchaseProduct().getProduct().getSku(), ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE)) {
					reviewTerm = -1;
				}
			}
			if (PurchaseHistoryType.YODOBASHI.equals(vo.getPurchaseProduct().getPurchaseHistoryType())) {
//				int effectiveReviewTerm = ReviewDO.getEffectiveReviewTerm(vo.getPurchaseProduct().getProduct());
//				if (reviewTerm > effectiveReviewTerm || effectiveReviewTerm == -1|| reviewTerm == -1) {
				if (!vo.getPurchaseProduct().getProduct().isGrantPointWithinTerm(vo.getPurchaseProduct().getPurchaseDate())) {
					vo.setReviewPointActive(false);
				} else {
					if (vo.getPurchaseProduct().isShare()) {
						boolean reviewPointActive = true;
						for (String userId : accountSharingCommunityUserIds) {
							if (!leniencePointGrantReviewsMap.get(IdUtil.createIdByConcatIds(userId, sku, String.valueOf(reviewTerm)))) {
								reviewPointActive = false;
								break;
							}
						}
						vo.setReviewPointActive(reviewPointActive);
					} else {
						vo.setReviewPointActive(leniencePointGrantReviewsMap.get(IdUtil.createIdByConcatIds(communityUserId, sku, String.valueOf(reviewTerm))));
					}
				}
			}
			
			if( myReviewCountMap != null ){
				myReviewCountCount = myReviewCountMap.get(sku);
			}
			if( reviewCountMap != null ){
				reviewCount = reviewCountMap.get(sku);
			}
			if( questionCountMap != null ){
				questionCount = questionCountMap.get(sku);
			}
			
			if ((myReviewCountCount == null || myReviewCountCount == 0) && reviewTerm == 0) {
				vo.setCanPostReviewType(ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE);
			} else {
				vo.setCanPostReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
			}
			if (reviewCount != null && reviewCount > 0) {
				vo.setExistsReview(true);
			}
			if (questionCount != null && questionCount > 0) {
				vo.setExistsAnswerWaitingQuestion(true);
			}
			
			if (!PurchaseHistoryType.YODOBASHI.equals(vo.getPurchaseProduct().getPurchaseHistoryType()))
				continue;
			
			for (PointIncentiveType type : PointIncentiveType.values()) {
				if (type.isOld()) {
					continue;
				}
				if (type.getReviewType() == null) {
					vo.setProductPoint(
							ReviewDO.getReviewQstPoint(
									PointQuestType.PRODUCT_POINT,
									vo.getPurchaseProduct().getProduct(),
									vo.getPurchaseProduct(),
									new Date()));
				} else if (type.getReviewType().equals(vo.getCanPostReviewType())) {
					vo.getQuestionPoints().add(
							ReviewDO.getReviewQstPoint(
									PointQuestType.valueOf(type),
									vo.getPurchaseProduct().getProduct(),
									vo.getPurchaseProduct(),
									new Date()));
				}
			}
		}
	}
	
	protected boolean checkPostImmediatelyAfter(Date postDate){
		long postDateTime = postDate.getTime();
		long nowDateTime = Calendar.getInstance().getTimeInMillis();
		// 経過（分）を算出
		long minitusDiff = (nowDateTime - postDateTime)/(1000 * 60);
		// 90分以内かどうか（外部プロパティ化するかどうか検討する）
		return (minitusDiff < 90);
	}
	
	protected void befoureSaveContentModifyImages(
			TextEditableContents contents, 
			PostContentType postContentType, 
			ImageTargetType imageTargetType,
			Map<String, ImageHeaderDO> uploadImageMap,
			List<String> updateImageIds){
		// 画像の比較
		// ・既にアップロードしている画像はそのままにする。
		// ・削除されている画像は削除フラグを立てる。
		// ・新規に投稿された画像は新規に追加する。
		// ・質問情報の画像一覧を更新する。
		// ・画像のキャプションを更新する。
		List<ImageHeaderDO> submittedImages = imageDao.findImageByContentId(
				contents.getContentId(), 
				imageTargetType, 
				null, 
				new ContentsStatus[]{ContentsStatus.SUBMITTED, ContentsStatus.CONTENTS_STOP});
		
		List<SaveImageDO> saveImages = contents.getSaveImages();
		List<ImageHeaderDO> modifyImageHeaders = Lists.newArrayList();
		List<ImageHeaderDO> deleteImageHeaders = Lists.newArrayList();
		List<String> deleteImageIds = Lists.newArrayList();
		List<String> modifyImageIds = Lists.newArrayList();
		List<String> newImageIds = Lists.newArrayList();
		
		int imageSetIndex = 0;
		
		if( saveImages != null && !saveImages.isEmpty()){
			if( submittedImages != null && !submittedImages.isEmpty() ){
				// 削除する登録済み画像一覧および存在する画像一覧を取得する。
				for( ImageHeaderDO submittedImage : submittedImages){
					boolean existImage = false;
					// 新規投稿画像とチェックしなければ削除、あれば更新
					for( SaveImageDO saveImageDO : saveImages ){
						if( submittedImage.getImageId().equals(saveImageDO.getImageId())){
							existImage = true;
							break;
						}
					}
					if( existImage ){
						modifyImageHeaders.add(submittedImage);
					}else{
						deleteImageHeaders.add(submittedImage);
					}
				}
			}
			// 新規の投稿画像一覧を取得する。
			for( SaveImageDO saveImageDO : saveImages ){
				boolean existImage = false;
				for( ImageHeaderDO modifyImageHeader : modifyImageHeaders){
					if( saveImageDO.getImageId().equals(modifyImageHeader.getImageId())){
						existImage = true;
						break;
					}
				}
				if( !existImage ){
					newImageIds.add(saveImageDO.getImageId());
				}
			}
		}else{
			// すべて削除する。
			if( submittedImages != null && !submittedImages.isEmpty() ){
				deleteImageHeaders.addAll(submittedImages);
			}
		}
		
		// 削除画像一覧の処理
		if( !deleteImageHeaders.isEmpty() ){
			Set<String> validImageIds = new HashSet<String>();
			for( ImageHeaderDO deleteImageHeader : deleteImageHeaders){
				validImageIds.add(deleteImageHeader.getImageId());
				if( !StringUtils.isBlank(deleteImageHeader.getThumbnailImageId())){
					validImageIds.add(deleteImageHeader.getThumbnailImageId());
				}
			}
			deleteImageIds.addAll(validImageIds);
			// 画像の有効性チェック
			validImageIds = imageDao.validateImageIds(validImageIds, contents.getCommunityUser().getCommunityUserId());
			dropInvalidIds(validImageIds, deleteImageIds);
			//除去された画像を物理削除します。
			if (!deleteImageIds.isEmpty()) {
				for (String deleteImageId : deleteImageIds) {
					imageDao.deleteBothImage(postContentType, contents.getContentId(), deleteImageId, true, false, ContentsStatus.DELETE);
				}
			}
		}
		// 更新画像の処理（ImageSetIndexの更新)
		if( !modifyImageHeaders.isEmpty() ){
			for( ImageHeaderDO imageHeader : modifyImageHeaders){
				// インデックスの設定
				imageHeader.setImageSetIndex(imageSetIndex);
				imageHeader.setComment(null);
				imageHeader.setListViewFlag(imageSetIndex == 0);
				// コメント設定
				if( saveImages != null && !saveImages.isEmpty() ){
					for(SaveImageDO saveImage : saveImages){
						if( saveImage.getImageId().equals(imageHeader.getImageId())){
							imageHeader.setComment(saveImage.getCaption());
							break;
						}
					}
				}
				// 更新処理（データ）
				imageDao.updateImageHeader(imageHeader, Path.includeProp("listViewFlag, imageSetIndex, comment, modifyDateTime"));
				// 更新処理（インデックス）
				modifyImageIds.add(imageHeader.getImageId());
				imageSetIndex++;
			}
		}
		// 新規画像登録処理
		if( !newImageIds.isEmpty() ){
			List<SaveImageDO> newImages = Lists.newArrayList();
			for(String newImageId : newImageIds){
				for( SaveImageDO saveImage : saveImages ){
					if( newImageId.equals(saveImage.getImageId())){
						newImages.add(saveImage);
						break;
					}
				}
			}
			updateImageRelateContents(
					contents.getCommunityUser().getCommunityUserId(),
					contents.getProduct().getSku(),
					postContentType,
					contents.getProduct().isAdult(),
					contents,
					newImages,
					uploadImageMap,
					imageSetIndex);
		}
		// 画像ID一覧は、保存用のためNULLで初期化する。
		contents.setUploadImageIds(null);
		contents.setSaveImages(null);
		
		updateImageIds.addAll(deleteImageIds);
		updateImageIds.addAll(modifyImageIds);
		updateImageIds.addAll(newImageIds);
	}
	
	/**
	 * コンテンツ内の画像情報を更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @param postContentType 投稿タイプ
	 * @param contentsId コンテンツID
	 * @param adult アダルト商品に紐付く画像かどうか
	 * @param contents コンテンツ
	 * @param updateImageIds 更新画像IDのリスト
	 * @param uploadImageMap アップロード対象となる画像マップ
	 */
	@Override
	@ArroundHBase
	public void updateImageRelateContents(
			String communityUserId,
			String sku,
			PostContentType postContentType,
			boolean adult,
			TextEditableContents contents,
			List<SaveImageDO> saveImages,
			Map<String, ImageHeaderDO> uploadImageMap){
		updateImageRelateContents(
				communityUserId,
				sku, 
				postContentType,
				adult,
				contents,
				saveImages,
				uploadImageMap,
				0);
	}
	/**
	 * コンテンツ内の画像情報を更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @param postContentType 投稿タイプ
	 * @param contentsId コンテンツID
	 * @param adult アダルト商品に紐付く画像かどうか
	 * @param contents コンテンツ
	 * @param updateImageIds 更新画像IDのリスト
	 * @param uploadImageMap アップロード対象となる画像マップ
	 */
	@Override
	@ArroundHBase
	public void updateImageRelateContents(
			String communityUserId,
			String sku,
			PostContentType postContentType,
			boolean adult,
			TextEditableContents contents,
			List<SaveImageDO> saveImages,
			Map<String, ImageHeaderDO> uploadImageMap,
			int offsetIndex){
		
		Map<String, String> imageMap = new HashMap<String, String>();
		if (saveImages != null && !saveImages.isEmpty() && ContentsStatus.SUBMITTED.equals(contents.getStatus())) {
			//テンポラリ画像を全てアップロードします。
			for (int i=0; i<saveImages.size(); i++) {
				SaveImageDO image = saveImages.get(i);
				ImageHeaderDO imageHeader = new ImageHeaderDO();
				imageHeader.setImageId(image.getImageId());
				// コメント設定
				imageHeader.setComment(image.getCaption());
				imageHeader.setPostContentType(postContentType);
				// 最初の画像に一覧表示で表示フラグを付ける
				imageHeader.setListViewFlag((i == 0));
				CommunityUserDO communityUser = new CommunityUserDO();
				communityUser.setCommunityUserId(communityUserId);
				imageHeader.setOwnerCommunityUser(communityUser);
				imageHeader.setSku(sku);
				imageHeader.setAdult(adult);
				//同一タイミングでアップロードした画像のインデックスは同じにならない。
				imageHeader.setImageSetIndex(offsetIndex + i);
				imageDao.uploadImage(imageHeader, true);
				imageMap.put(imageHeader.getImageId(), imageHeader.getImageUrl());
				uploadImageMap.put(imageHeader.getImageId(), imageHeader);
			}
		}
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
}
