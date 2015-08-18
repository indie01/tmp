package com.kickmogu.yodobashi.community.tdc2;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Maps;
import com.kickmogu.lib.core.utils.Asserts;
import com.kickmogu.yodobashi.community.resource.config.DomainConfig;
import com.kickmogu.yodobashi.community.resource.dao.NormalizeCharDao;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityNameDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.DecisivePurchaseDO;
import com.kickmogu.yodobashi.community.resource.domain.HashCommunityIdDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseLostProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDecisivePurchaseDO;
import com.kickmogu.yodobashi.community.resource.domain.UsedProductDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.AlsoBuyProduct;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommentTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageUploadResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;
import com.kickmogu.yodobashi.community.resource.domain.constants.LikeTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ProductSatisfaction;
import com.kickmogu.yodobashi.community.resource.domain.constants.PurchaseHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewType;
import com.kickmogu.yodobashi.community.resource.domain.constants.Verification;

// communityUserDO
// communityNameDO
// hashCommunityIdDO
// informationDO
// actionHistoryDO
// reviewDO
// imageHeaderDO
// imageDO
// questionDO
// questionAnswerDO
// CommentDO
// PurchaseProductDO
// LikeDO
// CommunityUserFollowDO
// ProductFollowDO
// QuestionFollowDO
//
// slipHeaderDO, slipDetailDO ,receiptHeaderDO, receiptDetailDO
//
// PurchaseLostProductDO
// UsedProductDO
// DecisivePurchaseDO
// ReviewDecisivePurchaseDO

// communityUserDO,communityNameDO,hashCommunityIdDO,PurchaseProductDO
public class TestDataCreators2 {

	// １ユーザーあたり商品１００件購入しているデータ作成用
	
	@CreateCount(perCommunityUser=1)
	public static class CommunityUserCreator2 extends DataCreator2<CommunityUserDO, TestId.CommunityUserId> {
		
		@Autowired DomainConfig domainConfig;
		@Autowired NormalizeCharDao normalizeCharDao;

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.CommunityUserImageId.class, dataCreateContext.num/5);
			dataCreateContext.readIdList(TestId.CommunityUserThumbnailImageId.class, dataCreateContext.num/5);
		}
		
		@Override
		public void fillItems(CommunityUserDO object, DataCreateContext2 dataCreateContext) {
			object.setCommunityUserId(dataCreateContext.getCurrentId());
			object.setCommunityId(new DecimalFormat("0000000000").format(dataCreateContext.currentCount));
			object.setHashCommunityId(domainConfig.createHashCommunityId(object.getCommunityId()));
			object.setCommunityName("テストユーザ"+dataCreateContext.currentCountAsString);
			object.setNormalizeCommunityName(normalizeCharDao.normalizeString(object.getCommunityName()));
			//object.setNormalizeCommunityName(object.getCommunityName());
			
			if (dataCreateContext.currentCount <= dataCreateContext.num / 5 - 1) {
				object.setImageHeader(createObjectWithId(ImageHeaderDO.class, dataCreateContext.getCurrentId(TestId.CommunityUserImageId.class)));
				object.setThumbnail(createObjectWithId(ImageHeaderDO.class, dataCreateContext.getCurrentId(TestId.CommunityUserThumbnailImageId.class)));
				object.setProfileImageUrl(getImageUrl(object.getImageHeader().getImageId(), "images/png", PostContentType.PROFILE, false));
				object.setThumbnailImageUrl(getImageUrl(object.getThumbnail().getImageId(), "images/png", PostContentType.PROFILE_THUMBNAIL, true));
			}
			object.setStatus(CommunityUserStatus.ACTIVE);
			object.setCeroVerification(Verification.ATANYTIME);
			object.setAdultVerification(Verification.ATANYTIME);
		}
	}
	
	@CreateCount(perCommunityUser=1)
	public static class HashCommunityIdCreator2 extends DataCreator2<HashCommunityIdDO, TestId.CommunityUserId> {
		@Autowired DomainConfig domainConfig;
		
		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
		}
		
		@Override
		public void fillItems(HashCommunityIdDO object, DataCreateContext2 dataCreateContext) {
			String communityId = new DecimalFormat("0000000000").format(dataCreateContext.currentCount);
			object.setCommunityUserId(dataCreateContext.getCurrentId());
			object.setHashCommunityId(domainConfig.createHashCommunityId(communityId));
		}
	}
	
	
	@CreateCount(perCommunityUser=1)
	public static class CommunityNameCreator2 extends DataCreator2<CommunityNameDO, TestId.CommunityUserId> {
		
		@Autowired NormalizeCharDao normalizeCharDao;

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
		}
		
		@Override
		public void fillItems(CommunityNameDO object, DataCreateContext2 dataCreateContext) {
			object.setNormalizeCommunityName(normalizeCharDao.normalizeString("テストユーザ"+dataCreateContext.currentCountAsString));
			object.setCommunityUserId(dataCreateContext.getCurrentId());
			object.setOuterCustomerId(new DecimalFormat("0000000000").format(dataCreateContext.currentCount));
		}
	}
	
	@CreateCount(perCommunityUser=1)
	public static class WelcomeInformationCreator2 extends DataCreator2<InformationDO, TestId.WelcomeInformationId> {
		
		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.CommunityUserId.class, dataCreateContext.num);
		}
		
		@Override
		public void fillItems(InformationDO object, DataCreateContext2 dataCreateContext) {
			object.setInformationId(dataCreateContext.getCurrentId());
			object.setInformationType(InformationType.WELCOME);
			object.setInformationTime(dataCreateContext.currentTime);
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, dataCreateContext.getCurrentId(TestId.CommunityUserId.class)));
		}
	}
	
	@CreateCount(perCommunityUser=0.2)
	public static class CommunityUserImageHeaderCreator2 extends DataCreator2<ImageHeaderDO, TestId.CommunityUserImageId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.CommunityUserThumbnailImageId.class, dataCreateContext.num);
		}
		
		@Override
		public void fillItems(ImageHeaderDO object, DataCreateContext2 dataCreateContext) {
			object.setImageId(dataCreateContext.getCurrentId());
			object.setOwnerCommunityUser(createObjectWithId(CommunityUserDO.class, dataCreateContext.getCurrentId(TestId.CommunityUserId.class)));
			object.setImageUrl(getImageUrl(object.getImageId(), "images/png", PostContentType.PROFILE, false));
			object.setPostContentType(PostContentType.PROFILE);
			object.setThumbnailImageId(dataCreateContext.getCurrentId(TestId.CommunityUserThumbnailImageId.class));
			object.setThumbnailImageUrl(getImageUrl(object.getThumbnailImageId(), "images/png", PostContentType.PROFILE_THUMBNAIL, true));
			fillImageHeaderCommonItems(object, dataCreateContext);
			object.setImageUploadResult(ImageUploadResult.SUCCESS);
		}
	}
	
	@CreateCount(perCommunityUser=0.2)
	public static class CommunityUserThumbnailImageHeaderCreator2 extends DataCreator2<ImageHeaderDO, TestId.CommunityUserThumbnailImageId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
		}
		
		@Override
		public void fillItems(ImageHeaderDO object, DataCreateContext2 dataCreateContext) {
			object.setImageId(dataCreateContext.getCurrentId());
			object.setOwnerCommunityUser(createObjectWithId(CommunityUserDO.class, dataCreateContext.getCurrentId(TestId.CommunityUserId.class)));
			object.setImageUrl(getImageUrl(object.getImageId(), "images/png", PostContentType.PROFILE_THUMBNAIL, true));
			object.setPostContentType(PostContentType.PROFILE_THUMBNAIL);
			fillThumbnailImageHeaderCommonItems(object, dataCreateContext);
			object.setImageUploadResult(ImageUploadResult.SUCCESS);
		}
	}	
	
	@CreateCount(perCommunityUser=0.2)
	public static class CommunityUserImageCreator2 extends DataCreator2<ImageDO, TestId.CommunityUserImageId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
		}

		@Override
		public void fillItems(ImageDO object, DataCreateContext2 dataCreateContext) {
			object.setImageId(dataCreateContext.getCurrentId());
			object.setMimeType("images/png");
			object.setCommunityUserId(dataCreateContext.getCurrentId(TestId.CommunityUserId.class));
			object.setImageUrl(getImageUrl(object.getImageId(), object.getMimeType(), PostContentType.PROFILE, false));
			object.setData(getCommunityUserProfileImage());
			object.setWidth(210);
			object.setHeigth(210);
		}
	}
	
	@CreateCount(perCommunityUser=0.2)
	public static class CommunityUserThumbnailImageCreator2 extends DataCreator2<ImageDO, TestId.CommunityUserThumbnailImageId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
		}

		@Override
		public void fillItems(ImageDO object, DataCreateContext2 dataCreateContext) {
			object.setImageId(dataCreateContext.getCurrentId());
			object.setMimeType("images/png");			
			object.setCommunityUserId(dataCreateContext.getCurrentId(TestId.CommunityUserId.class));
			object.setImageUrl(getImageUrl(object.getImageId(), object.getMimeType(), PostContentType.PROFILE_THUMBNAIL, true));
			object.setData(getCommunityUserProfileThumbnailImage());
			object.setWidth(50);
			object.setHeigth(50); 
		}
	}
	
	@CreateCount(perCommunityUser=2)
	public static class ReviewImageHeaderCreator2 extends DataCreator2<ImageHeaderDO, TestId.ReviewImageId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.ReviewThumbnailImageId.class, dataCreateContext.num);
			dataCreateContext.readIdList(TestId.ReviewId.class, dataCreateContext.num*5);
		}
		
		@Override
		public void fillItems(ImageHeaderDO object, DataCreateContext2 dataCreateContext) {
			object.setImageId(dataCreateContext.getCurrentId());
			
			object.setOwnerCommunityUser(createObjectWithId(CommunityUserDO.class, dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/2)));
			object.setImageUrl(getImageUrl(object.getImageId(), "images/png", PostContentType.REVIEW, false));
			object.setPostContentType(PostContentType.REVIEW);
			object.setThumbnailImageId(dataCreateContext.getCurrentId(TestId.ReviewThumbnailImageId.class));
			object.setThumbnailImageUrl(getImageUrl(object.getThumbnailImageId(), "images/png", PostContentType.REVIEW, true));
			fillImageHeaderCommonItems(object, dataCreateContext);
			
			int reviewCount = dataCreateContext.currentCount*5;
			object.setReview(createObjectWithId(ReviewDO.class, dataCreateContext.getId(TestId.ReviewId.class, reviewCount)));
			object.setProduct(createObjectWithId(ProductDO.class, getSku(reviewCount)));
			object.setPurchaseDate(dataCreateContext.currentTime);
			object.setPurchaseHistoryType(PurchaseHistoryType.YODOBASHI);
			
		}

	}
	
	@CreateCount(perCommunityUser=2)
	public static class ReviewThumbnailImageHeaderCreator2 extends DataCreator2<ImageHeaderDO, TestId.ReviewThumbnailImageId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.ReviewId.class, dataCreateContext.num*5);
		}
		
		@Override
		public void fillItems(ImageHeaderDO object, DataCreateContext2 dataCreateContext) {
			object.setImageId(dataCreateContext.getCurrentId());
			object.setOwnerCommunityUser(createObjectWithId(CommunityUserDO.class, dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/2)));
			object.setImageUrl(getImageUrl(object.getImageId(), "images/png", PostContentType.REVIEW, true));
			object.setPostContentType(PostContentType.REVIEW);
			fillThumbnailImageHeaderCommonItems(object, dataCreateContext);
			
			int reviewCount = dataCreateContext.currentCount*5;
			object.setReview(createObjectWithId(ReviewDO.class, dataCreateContext.getId(TestId.ReviewId.class, reviewCount)));
			object.setProduct(createObjectWithId(ProductDO.class, getSku(reviewCount)));
		}
	}
	
	@CreateCount(perCommunityUser=2)
	public static class ReviewImageCreator2 extends DataCreator2<ImageDO, TestId.ReviewImageId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
		}

		@Override
		public void fillItems(ImageDO object, DataCreateContext2 dataCreateContext) {
			object.setImageId(dataCreateContext.getCurrentId());
			object.setMimeType("images/png");
			object.setCommunityUserId(dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/2));
			object.setImageUrl(getImageUrl(object.getImageId(), object.getMimeType(), PostContentType.REVIEW, false));
			object.setData(getProductImage());
			object.setWidth(210);
			object.setHeigth(210);
		}
	}
	
	@CreateCount(perCommunityUser=2)
	public static class ReviewThumbnailImageCreator2 extends DataCreator2<ImageDO, TestId.ReviewThumbnailImageId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
		}

		@Override
		public void fillItems(ImageDO object, DataCreateContext2 dataCreateContext) {
			object.setImageId(dataCreateContext.getCurrentId());
			object.setMimeType("images/png");			
			object.setCommunityUserId(dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/2));
			object.setImageUrl(getImageUrl(object.getImageId(), object.getMimeType(), PostContentType.REVIEW, true));
			object.setData(getProductThumbnailImage());
			object.setWidth(50);
			object.setHeigth(50); 
		}
	}
	
	@CreateCount(perCommunityUser=10)
	public static class ReviewCreator2 extends DataCreator2<ReviewDO, TestId.ReviewId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
		}

		@Override
		public void fillItems(ReviewDO object, DataCreateContext2 dataCreateContext) {
			object.setReviewId(dataCreateContext.getCurrentId());
			object.setReviewType(ReviewType.REVIEW_AFTER_FEW_DAYS);
			object.setPurchaseDate(dataCreateContext.currentTime);
			object.setElapsedDays(1);
			object.setPurchaseHistoryType(PurchaseHistoryType.YODOBASHI);
			object.setProductSatisfaction(ProductSatisfaction.FOUR);
			object.setAlsoBuyProduct(AlsoBuyProduct.WANTOBUY);
			object.setReviewBody(StringUtils.repeat("レビュー本文"+dataCreateContext.currentCountAsString, 10));
			object.setStatus(ContentsStatus.SUBMITTED);
			object.setSaveDate(dataCreateContext.currentTime);
			object.setPostDate(dataCreateContext.currentTime);
			object.setPointBaseDate(dataCreateContext.currentTime);
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/10)));
			object.setProduct(createObjectWithId(ProductDO.class, getSku(dataCreateContext.currentCount)));
		}
	}
	
	@CreateCount(perCommunityUser=10)
	public static class UserReviewActionHistoryCreator2 extends DataCreator2<ActionHistoryDO, TestId.NullId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.ReviewId.class, dataCreateContext.num);
		}

		@Override
		public void fillItems(ActionHistoryDO object, DataCreateContext2 dataCreateContext) {
			
			object.setActionHistoryType(ActionHistoryType.USER_REVIEW);
			object.setActionTime(dataCreateContext.currentTime);
			
			String reviewId = dataCreateContext.getId(TestId.ReviewId.class, dataCreateContext.currentCount);
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/10);
			String sku = getSku(dataCreateContext.currentCount);
			
			object.setReview(createObjectWithId(ReviewDO.class, reviewId));
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
			object.setProduct(createObjectWithId(ProductDO.class, sku));
			object.setActionHistoryId(IdUtil.generateActionHistoryId(object, idGenerator));
		}
	}
	
	@CreateCount(perCommunityUser=10)
	public static class ProductReviewActionHistoryCreator2 extends DataCreator2<ActionHistoryDO, TestId.NullId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.ReviewId.class, dataCreateContext.num);
		}

		@Override
		public void fillItems(ActionHistoryDO object, DataCreateContext2 dataCreateContext) {
			object.setActionHistoryType(ActionHistoryType.PRODUCT_REVIEW);
			object.setActionTime(dataCreateContext.currentTime);
			
			String reviewId = dataCreateContext.getId(TestId.ReviewId.class, dataCreateContext.currentCount);
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/10);
			String sku = getSku(dataCreateContext.currentCount);
			
			object.setReview(createObjectWithId(ReviewDO.class, reviewId));
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
			object.setProduct(createObjectWithId(ProductDO.class, sku));
			object.setActionHistoryId(IdUtil.generateActionHistoryId(object, idGenerator));
		}
	}
	
	@CreateCount(perCommunityUser=10)
	public static class PurchaseLostProductCreator2 extends DataCreator2<PurchaseLostProductDO, TestId.NullId> {
		
		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.ReviewId.class, dataCreateContext.num);
		}
		
		@Override
		public void fillItems(PurchaseLostProductDO object, DataCreateContext2 dataCreateContext) {
		
			String reviewId = dataCreateContext.getId(TestId.ReviewId.class, dataCreateContext.currentCount);
			String purchaseLostProductId = IdUtil.createIdByBranchNo(reviewId, 0);
			String communityUserId = dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/10);
			
			object.setPurchaseLostProductId(purchaseLostProductId);
			object.setReviewProductId(getSku(dataCreateContext.currentCount));
			object.setReview(createObjectWithId(ReviewDO.class, reviewId));
			object.setProduct(createObjectWithId(ProductDO.class, getOtherSku(dataCreateContext.currentCount)));
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
		}
	}
	
	@CreateCount(perCommunityUser=10)
	public static class UsedProductCreator2 extends DataCreator2<UsedProductDO, TestId.NullId> {
		
		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.ReviewId.class, dataCreateContext.num);
		}
		
		@Override
		public void fillItems(UsedProductDO object, DataCreateContext2 dataCreateContext) {
		
			String reviewId = dataCreateContext.getId(TestId.ReviewId.class, dataCreateContext.currentCount);
			String purchaseLostProductId = IdUtil.createIdByBranchNo(reviewId, 0);
			String communityUserId = dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/10);
			
			object.setUsedProductId(purchaseLostProductId);
			object.setReviewProductId(getSku(dataCreateContext.currentCount));
			object.setReview(createObjectWithId(ReviewDO.class, reviewId));
			object.setProduct(createObjectWithId(ProductDO.class, getOtherSku(dataCreateContext.currentCount)));
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
		}
	}
	
	@CreateCount(perCommunityUser=20)
	public static class ReviewCommentCreator2 extends DataCreator2<CommentDO, TestId.ReviewCommentId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.ReviewId.class, dataCreateContext.num/2);
		}

		@Override
		public void fillItems(CommentDO object, DataCreateContext2 dataCreateContext) {
			object.setCommentId(dataCreateContext.getCurrentId());
			object.setCommentBody(StringUtils.repeat("レビューコメント"+dataCreateContext.currentCountAsString, 2));
			object.setTargetType(CommentTargetType.REVIEW);
			object.setPostDate(dataCreateContext.currentTime);
			
			String reviewId = dataCreateContext.getId(TestId.ReviewId.class, dataCreateContext.currentCount/2);
			String relationReviewOwnerId = dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/20);
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/20);
			
			object.setRelationReviewOwnerId(relationReviewOwnerId);
			object.setReview(createObjectWithId(ReviewDO.class, reviewId));
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
		}
	}

	@CreateCount(perCommunityUser=20)
	public static class UserReviewCommentActionHistoryCreator2 extends DataCreator2<ActionHistoryDO, TestId.NullId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.ReviewId.class, dataCreateContext.num/2);
			dataCreateContext.readIdList(TestId.ReviewCommentId.class, dataCreateContext.num);
		}

		@Override
		public void fillItems(ActionHistoryDO object, DataCreateContext2 dataCreateContext) {

			object.setActionHistoryType(ActionHistoryType.USER_REVIEW_COMMENT);
			object.setActionTime(dataCreateContext.currentTime);

			String commentId = dataCreateContext.getCurrentId(TestId.ReviewCommentId.class);
			String reviewId = dataCreateContext.getId(TestId.ReviewId.class, dataCreateContext.currentCount/2);
			String relationReviewOwnerId = dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/20);
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/20);
			String sku = getSku(dataCreateContext.currentCount);
			
			object.setComment(createObjectWithId(CommentDO.class, commentId));
			object.setRelationReviewOwnerId(relationReviewOwnerId);
			object.setReview(createObjectWithId(ReviewDO.class, reviewId));
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
			object.setProduct(createObjectWithId(ProductDO.class, sku));
			object.setActionHistoryId(IdUtil.generateActionHistoryId(object, idGenerator));
		}
	}
	
	@CreateCount(perCommunityUser=20)
	public static class ReviewCommentAddInformationCreator2 extends DataCreator2<InformationDO, TestId.NullId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.ReviewId.class, dataCreateContext.num/2);
			dataCreateContext.readIdList(TestId.ReviewCommentId.class, dataCreateContext.num);
		}

		@Override
		public void fillItems(InformationDO object, DataCreateContext2 dataCreateContext) {
			
			object.setInformationType(InformationType.REVIEW_COMMENT_ADD);
			object.setInformationTime(dataCreateContext.currentTime);

			String commentId = dataCreateContext.getCurrentId(TestId.ReviewCommentId.class);
			String reviewId = dataCreateContext.getId(TestId.ReviewId.class, dataCreateContext.currentCount/2);
			String relationCommentOwnerId =dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/20);
			String relationReviewOwnerId = dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/20);
			
			object.setComment(createObjectWithId(CommentDO.class, commentId));
			object.setRelationCommentOwnerId(relationCommentOwnerId);
			object.setRelationCommunityUserId(relationCommentOwnerId);
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, relationReviewOwnerId));
			object.setReview(createObjectWithId(ReviewDO.class, reviewId));
			object.setInformationId(IdUtil.getInfomationId(object, idGenerator));
		}
	}
	
	@CreateCount(perCommunityUser=20)
	public static class ReviewLikeCreator2 extends DataCreator2<LikeDO, TestId.NullId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.ReviewId.class, dataCreateContext.num/2);
		}

		@Override
		public void fillItems(LikeDO object, DataCreateContext2 dataCreateContext) {

			int diff = dataCreateContext.currentCount % 2 + 1;
			
			String reviewId = dataCreateContext.getId(TestId.ReviewId.class, dataCreateContext.currentCount/2);
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/20,diff);
			String likeId = IdUtil.createIdByConcatIds(communityUserId, reviewId, LikeTargetType.REVIEW.getCode());

			object.setLikeId(likeId);
			object.setTargetType(LikeTargetType.REVIEW);
			object.setSku(getSku(dataCreateContext.currentCount/2));
			object.setPostDate(dataCreateContext.currentTime);
			
			String relationReviewOwnerId = dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/20);

			object.setRelationReviewOwnerId(relationReviewOwnerId);
			object.setReview(createObjectWithId(ReviewDO.class, reviewId));
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
		}
	}
	
	@CreateCount(perCommunityUser=20)
	public static class ReviewLikeAddInformationCreator2 extends DataCreator2<InformationDO, TestId.NullId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.ReviewId.class, dataCreateContext.num/2);
		}

		@Override
		public void fillItems(InformationDO object, DataCreateContext2 dataCreateContext) {
			object.setInformationType(InformationType.REVIEW_LIKE_ADD);
			object.setInformationTime(dataCreateContext.currentTime);

			int diff = dataCreateContext.currentCount % 2 + 1;
			
			String reviewId = dataCreateContext.getId(TestId.ReviewId.class, dataCreateContext.currentCount/2);
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/20,diff);
			String likeId = IdUtil.createIdByConcatIds(communityUserId, reviewId, LikeTargetType.REVIEW.getCode());
						
			String relationReviewOwnerId = dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/20);
			
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class,relationReviewOwnerId));
			object.setReview(createObjectWithId(ReviewDO.class, reviewId));
			object.setLike(createObjectWithId(LikeDO.class, likeId));
			object.setRelationLikeOwnerId(communityUserId);
			object.setRelationCommunityUserId(communityUserId);
			object.setInformationId(IdUtil.getInfomationId(object, idGenerator));
		}
	}	
	
	@CreateCount(perCommunityUser=2)
	public static class QuestionImageHeaderCreator2 extends DataCreator2<ImageHeaderDO, TestId.QuestionImageId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.QuestionThumbnailImageId.class, dataCreateContext.num);
			dataCreateContext.readIdList(TestId.QuestionId.class, dataCreateContext.num*5);
		}
		
		@Override
		public void fillItems(ImageHeaderDO object, DataCreateContext2 dataCreateContext) {
			object.setImageId(dataCreateContext.getCurrentId());
			
			object.setOwnerCommunityUser(createObjectWithId(CommunityUserDO.class, dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/2)));
			object.setImageUrl(getImageUrl(object.getImageId(), "images/png", PostContentType.QUESTION, false));
			object.setPostContentType(PostContentType.QUESTION);
			object.setThumbnailImageId(dataCreateContext.getCurrentId(TestId.QuestionThumbnailImageId.class));
			object.setThumbnailImageUrl(getImageUrl(object.getThumbnailImageId(), "images/png", PostContentType.QUESTION, true));
			fillImageHeaderCommonItems(object, dataCreateContext);
			
			int questionCount = dataCreateContext.currentCount*5;
			object.setQuestion(createObjectWithId(QuestionDO.class, dataCreateContext.getId(TestId.QuestionId.class, questionCount)));
			object.setProduct(createObjectWithId(ProductDO.class, getSku(questionCount)));
			object.setPurchaseDate(dataCreateContext.currentTime);
			object.setPurchaseHistoryType(PurchaseHistoryType.YODOBASHI);
			
		}
	}
	
	@CreateCount(perCommunityUser=2)
	public static class QuestionThumbnailImageHeaderCreator2 extends DataCreator2<ImageHeaderDO, TestId.QuestionThumbnailImageId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.QuestionId.class, dataCreateContext.num*5);
		}
		
		@Override
		public void fillItems(ImageHeaderDO object, DataCreateContext2 dataCreateContext) {
			object.setImageId(dataCreateContext.getCurrentId());
			object.setOwnerCommunityUser(createObjectWithId(CommunityUserDO.class, dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/2)));
			object.setImageUrl(getImageUrl(object.getImageId(), "images/png", PostContentType.QUESTION, true));
			object.setPostContentType(PostContentType.QUESTION);
			fillThumbnailImageHeaderCommonItems(object, dataCreateContext);
			
			int questionCount = dataCreateContext.currentCount*5;
			object.setQuestion(createObjectWithId(QuestionDO.class, dataCreateContext.getId(TestId.QuestionId.class, questionCount)));
			object.setProduct(createObjectWithId(ProductDO.class, getSku(questionCount)));
		}
	}
	
	@CreateCount(perCommunityUser=2)
	public static class QuestionImageCreator2 extends DataCreator2<ImageDO, TestId.QuestionImageId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
		}

		@Override
		public void fillItems(ImageDO object, DataCreateContext2 dataCreateContext) {
			object.setImageId(dataCreateContext.getCurrentId());
			object.setMimeType("images/png");
			object.setCommunityUserId(dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/2));
			object.setImageUrl(getImageUrl(object.getImageId(), object.getMimeType(), PostContentType.QUESTION, false));
			object.setData(getProductImage());
			object.setWidth(210);
			object.setHeigth(210);
		}
	}
	
	@CreateCount(perCommunityUser=2)
	public static class QuestionThumbnailImageCreator2 extends DataCreator2<ImageDO, TestId.QuestionThumbnailImageId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
		}

		@Override
		public void fillItems(ImageDO object, DataCreateContext2 dataCreateContext) {
			object.setImageId(dataCreateContext.getCurrentId());
			object.setMimeType("images/png");			
			object.setCommunityUserId(dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/2));
			object.setImageUrl(getImageUrl(object.getImageId(), object.getMimeType(), PostContentType.QUESTION, true));
			object.setData(getProductThumbnailImage());
			object.setWidth(50);
			object.setHeigth(50); 
		}
	}
	
	@CreateCount(perCommunityUser=10)
	public static class QuestionCreator2 extends DataCreator2<QuestionDO, TestId.QuestionId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
		}

		@Override
		public void fillItems(QuestionDO object, DataCreateContext2 dataCreateContext) {
			object.setQuestionId(dataCreateContext.getCurrentId());
			object.setQuestionBody(StringUtils.repeat("質問本文"+dataCreateContext.currentCountAsString, 10));
			object.setStatus(ContentsStatus.SUBMITTED);
			object.setSaveDate(dataCreateContext.currentTime);
			object.setPostDate(dataCreateContext.currentTime);
			object.setLastAnswerDate(dataCreateContext.currentTime);
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/10)));
			object.setProduct(createObjectWithId(ProductDO.class, getSku(dataCreateContext.currentCount)));	
		}
	}

	@CreateCount(perCommunityUser=10)
	public static class UserQuestionActionHistoryCreator2 extends DataCreator2<ActionHistoryDO, TestId.NullId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.QuestionId.class, dataCreateContext.num);
		}

		@Override
		public void fillItems(ActionHistoryDO object, DataCreateContext2 dataCreateContext) {

			object.setActionHistoryType(ActionHistoryType.USER_QUESTION);
			object.setActionTime(dataCreateContext.currentTime);
			
			String quetionId = dataCreateContext.getId(TestId.QuestionId.class, dataCreateContext.currentCount);
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/10);
			String sku = getSku(dataCreateContext.currentCount);
			
			object.setQuestion(createObjectWithId(QuestionDO.class, quetionId));
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
			object.setProduct(createObjectWithId(ProductDO.class, sku));
			object.setActionHistoryId(IdUtil.generateActionHistoryId(object, idGenerator));
		}
	}
	
	@CreateCount(perCommunityUser=10)
	public static class ProductQuestionActionHistoryCreator2 extends DataCreator2<ActionHistoryDO, TestId.NullId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.QuestionId.class, dataCreateContext.num);
		}

		@Override
		public void fillItems(ActionHistoryDO object, DataCreateContext2 dataCreateContext) {

			object.setActionHistoryType(ActionHistoryType.PRODUCT_QUESTION);
			object.setActionTime(dataCreateContext.currentTime);
			
			String quetionId = dataCreateContext.getId(TestId.QuestionId.class, dataCreateContext.currentCount);
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/10);
			String sku = getSku(dataCreateContext.currentCount);
			
			object.setQuestion(createObjectWithId(QuestionDO.class, quetionId));
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
			object.setProduct(createObjectWithId(ProductDO.class, sku));
			object.setActionHistoryId(IdUtil.generateActionHistoryId(object, idGenerator));
		}
	}		
	
	@CreateCount(perCommunityUser=4)
	public static class QuestionAnswerImageHeaderCreator2 extends DataCreator2<ImageHeaderDO, TestId.QuestionAnswerImageId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.QuestionAnswerThumbnailImageId.class, dataCreateContext.num);
			dataCreateContext.readIdList(TestId.QuestionAnswerId.class, dataCreateContext.num*5);
			dataCreateContext.readIdList(TestId.QuestionId.class, dataCreateContext.num*5/2);
		}
		
		@Override
		public void fillItems(ImageHeaderDO object, DataCreateContext2 dataCreateContext) {
			object.setImageId(dataCreateContext.getCurrentId());
			
			object.setOwnerCommunityUser(createObjectWithId(CommunityUserDO.class, dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/4)));
			object.setImageUrl(getImageUrl(object.getImageId(), "images/png", PostContentType.ANSWER, false));
			object.setPostContentType(PostContentType.ANSWER);
			object.setThumbnailImageId(dataCreateContext.getCurrentId(TestId.QuestionAnswerThumbnailImageId.class));
			object.setThumbnailImageUrl(getImageUrl(object.getThumbnailImageId(), "images/png", PostContentType.ANSWER, true));
			fillImageHeaderCommonItems(object, dataCreateContext);
			
			int questionAnswerCount = dataCreateContext.currentCount*5;
			object.setQuestionAnswer(createObjectWithId(QuestionAnswerDO.class, dataCreateContext.getId(TestId.QuestionAnswerId.class, questionAnswerCount)));
			object.setProduct(createObjectWithId(ProductDO.class, getSku(questionAnswerCount/2)));
			object.setQuestion(createObjectWithId(QuestionDO.class,  dataCreateContext.getId(TestId.QuestionId.class, questionAnswerCount/2)));
			object.setPurchaseDate(dataCreateContext.currentTime);
			object.setPurchaseHistoryType(PurchaseHistoryType.YODOBASHI);
		}
	}
	
	@CreateCount(perCommunityUser=4)
	public static class QuestionAnswerThumbnailImageHeaderCreator2 extends DataCreator2<ImageHeaderDO, TestId.QuestionAnswerThumbnailImageId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.QuestionAnswerId.class, dataCreateContext.num*5);
			dataCreateContext.readIdList(TestId.QuestionId.class, dataCreateContext.num*5/2);
		}
		
		@Override
		public void fillItems(ImageHeaderDO object, DataCreateContext2 dataCreateContext) {
			object.setImageId(dataCreateContext.getCurrentId());
			object.setOwnerCommunityUser(createObjectWithId(CommunityUserDO.class, dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/4)));
			object.setImageUrl(getImageUrl(object.getImageId(), "images/png", PostContentType.ANSWER, true));
			object.setPostContentType(PostContentType.ANSWER);
			fillThumbnailImageHeaderCommonItems(object, dataCreateContext);
			
			int questionAnswerCount = dataCreateContext.currentCount*5;
			object.setQuestionAnswer(createObjectWithId(QuestionAnswerDO.class, dataCreateContext.getId(TestId.QuestionAnswerId.class, questionAnswerCount)));
			object.setQuestion(createObjectWithId(QuestionDO.class,  dataCreateContext.getId(TestId.QuestionId.class, questionAnswerCount/2)));
			object.setProduct(createObjectWithId(ProductDO.class, getSku(questionAnswerCount)));
		}
	}
	
	@CreateCount(perCommunityUser=4)
	public static class QuestionAnswerImageCreator2 extends DataCreator2<ImageDO, TestId.QuestionAnswerImageId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
		}

		@Override
		public void fillItems(ImageDO object, DataCreateContext2 dataCreateContext) {
			object.setImageId(dataCreateContext.getCurrentId());
			object.setMimeType("images/png");
			object.setCommunityUserId(dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/4));
			object.setImageUrl(getImageUrl(object.getImageId(), object.getMimeType(), PostContentType.ANSWER, false));
			object.setData(getProductImage());
			object.setWidth(210);
			object.setHeigth(210);
		}
	}
	
	@CreateCount(perCommunityUser=4)
	public static class QuestionAnswerThumbnailImageCreator2 extends DataCreator2<ImageDO, TestId.QuestionAnswerThumbnailImageId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
		}

		@Override
		public void fillItems(ImageDO object, DataCreateContext2 dataCreateContext) {
			object.setImageId(dataCreateContext.getCurrentId());
			object.setMimeType("images/png");			
			object.setCommunityUserId(dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/4));
			object.setImageUrl(getImageUrl(object.getImageId(), object.getMimeType(), PostContentType.ANSWER, true));
			object.setData(getProductThumbnailImage());
			object.setWidth(50);
			object.setHeigth(50); 
		}
	}
	
	@CreateCount(perCommunityUser=20)
	public static class QuestionAnswerCreator2 extends DataCreator2<QuestionAnswerDO, TestId.QuestionAnswerId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.QuestionId.class, dataCreateContext.num/2);
		}

		@Override
		public void fillItems(QuestionAnswerDO object, DataCreateContext2 dataCreateContext) {
			object.setQuestionAnswerId(dataCreateContext.getCurrentId());
			object.setPurchaseDate(dataCreateContext.currentTime);
			object.setPurchaseHistoryType(PurchaseHistoryType.YODOBASHI);
			object.setAnswerBody(StringUtils.repeat("質問回答"+dataCreateContext.currentCountAsString, 10));
			object.setStatus(ContentsStatus.SUBMITTED);
			object.setSaveDate(dataCreateContext.currentTime);
			object.setPostDate(dataCreateContext.currentTime);
			
			String relationQuestionOwnerId = dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/20);
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/20);
			Asserts.notEquals(relationQuestionOwnerId, communityUserId);
			
			object.setRelationQuestionOwnerId(relationQuestionOwnerId);
			object.setQuestion(createObjectWithId(QuestionDO.class, dataCreateContext.getId(TestId.QuestionId.class, dataCreateContext.currentCount/2)));
			object.setProduct(createObjectWithId(ProductDO.class, getSku(dataCreateContext.currentCount/2)));	
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
		}
	}
	
	@CreateCount(perCommunityUser=20)
	public static class UserQuestionAnswerActionHistoryCreator2 extends DataCreator2<ActionHistoryDO, TestId.NullId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.QuestionId.class, dataCreateContext.num/4);
			dataCreateContext.readIdList(TestId.QuestionAnswerId.class, dataCreateContext.num);
		}

		@Override
		public void fillItems(ActionHistoryDO object, DataCreateContext2 dataCreateContext) {

			object.setActionHistoryType(ActionHistoryType.USER_ANSWER);
			object.setActionTime(dataCreateContext.currentTime);
			
			String relationQuestionOwnerId =  dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/20);
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/20);
			String sku = getSku(dataCreateContext.currentCount/2);
			String quetionId = dataCreateContext.getId(TestId.QuestionId.class, dataCreateContext.currentCount/4);
			String quetionAnswerId = dataCreateContext.getId(TestId.QuestionAnswerId.class, dataCreateContext.currentCount);
			
			object.setRelationQuestionOwnerId(relationQuestionOwnerId);
			object.setQuestion(createObjectWithId(QuestionDO.class, quetionId));
			object.setQuestionAnswer(createObjectWithId(QuestionAnswerDO.class, quetionAnswerId));
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
			object.setProduct(createObjectWithId(ProductDO.class, sku));
			object.setActionHistoryId(IdUtil.generateActionHistoryId(object, idGenerator));
		}
	}
	
	@CreateCount(perCommunityUser=20)
	public static class QuestionAnswerAddInformationCreator2 extends DataCreator2<InformationDO, TestId.NullId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.QuestionId.class, dataCreateContext.num/4);
			dataCreateContext.readIdList(TestId.QuestionAnswerId.class, dataCreateContext.num);
		}

		@Override
		public void fillItems(InformationDO object, DataCreateContext2 dataCreateContext) {
			object.setInformationType(InformationType.QUESTION_ANSWER_ADD);
			object.setInformationTime(dataCreateContext.currentTime);
			
			String relationQuestionOwnerId =  dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/20);
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/20);
			String sku = getSku(dataCreateContext.currentCount/2);
			String quetionId = dataCreateContext.getId(TestId.QuestionId.class, dataCreateContext.currentCount/4);
			String quetionAnswerId = dataCreateContext.getId(TestId.QuestionAnswerId.class, dataCreateContext.currentCount);
			
			object.setRelationQuestionOwnerId(relationQuestionOwnerId);
			object.setQuestion(createObjectWithId(QuestionDO.class, quetionId));
			object.setQuestionAnswer(createObjectWithId(QuestionAnswerDO.class, quetionAnswerId));
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
			object.setProduct(createObjectWithId(ProductDO.class, sku));
			
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, relationQuestionOwnerId));
			object.setQuestionAnswer(createObjectWithId(QuestionAnswerDO.class, quetionAnswerId));
			object.setQuestion(createObjectWithId(QuestionDO.class, quetionId));
			object.setRelationQuestionAnswerOwnerId(communityUserId);
			object.setRelationCommunityUserId(communityUserId);
			object.setInformationId(IdUtil.getInfomationId(object, idGenerator));
		}
	}	
	
	@CreateCount(perCommunityUser=20)
	public static class ProductQuestionAnswerActionHistoryCreator2 extends DataCreator2<ActionHistoryDO, TestId.NullId>{
		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.QuestionId.class, dataCreateContext.num/4);
			dataCreateContext.readIdList(TestId.QuestionAnswerId.class, dataCreateContext.num);
		}

		@Override
		public void fillItems(ActionHistoryDO object, DataCreateContext2 dataCreateContext) {

			object.setActionHistoryType(ActionHistoryType.PRODUCT_ANSWER);
			object.setActionTime(dataCreateContext.currentTime);
			
			String relationQuestionOwnerId =  dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/20);
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/20);
			String sku = getSku(dataCreateContext.currentCount/2);
			String quetionId = dataCreateContext.getId(TestId.QuestionId.class, dataCreateContext.currentCount/4);
			String quetionAnswerId = dataCreateContext.getId(TestId.QuestionAnswerId.class, dataCreateContext.currentCount);
			
			object.setRelationQuestionOwnerId(relationQuestionOwnerId);
			object.setQuestion(createObjectWithId(QuestionDO.class, quetionId));
			object.setQuestionAnswer(createObjectWithId(QuestionAnswerDO.class, quetionAnswerId));
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
			object.setProduct(createObjectWithId(ProductDO.class, sku));
			object.setActionHistoryId(IdUtil.generateActionHistoryId(object, idGenerator));
		}
	}
	
	@CreateCount(perCommunityUser=20)
	public static class QuestionAnswerActionHistoryCreator2 extends DataCreator2<ActionHistoryDO, TestId.NullId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.QuestionId.class, dataCreateContext.num/4);
			dataCreateContext.readIdList(TestId.QuestionAnswerId.class, dataCreateContext.num);
		}

		@Override
		public void fillItems(ActionHistoryDO object, DataCreateContext2 dataCreateContext) {

			object.setActionHistoryType(ActionHistoryType.QUESTION_ANSWER);
			object.setActionTime(dataCreateContext.currentTime);
			
			String relationQuestionOwnerId =  dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/20);
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/20);
			String sku = getSku(dataCreateContext.currentCount/2);
			String quetionId = dataCreateContext.getId(TestId.QuestionId.class, dataCreateContext.currentCount/4);
			String quetionAnswerId = dataCreateContext.getId(TestId.QuestionAnswerId.class, dataCreateContext.currentCount);
			
			object.setRelationQuestionOwnerId(relationQuestionOwnerId);
			object.setQuestion(createObjectWithId(QuestionDO.class, quetionId));
			object.setQuestionAnswer(createObjectWithId(QuestionAnswerDO.class, quetionAnswerId));
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
			object.setProduct(createObjectWithId(ProductDO.class, sku));
			object.setActionHistoryId(IdUtil.generateActionHistoryId(object, idGenerator));
		}
	}
	
	@CreateCount(perCommunityUser=40)
	public static class QuestionAnswerCommentCreator2 extends DataCreator2<CommentDO, TestId.QuestionAnswerCommentId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.QuestionId.class, dataCreateContext.num/4);
			dataCreateContext.readIdList(TestId.QuestionAnswerId.class, dataCreateContext.num/2);
		}

		@Override
		public void fillItems(CommentDO object, DataCreateContext2 dataCreateContext) {
			object.setCommentId(dataCreateContext.getCurrentId());
			object.setCommentBody(StringUtils.repeat("質問回答コメント"+dataCreateContext.currentCountAsString, 2));
			object.setTargetType(CommentTargetType.QUESTION_ANSWER);
			object.setPostDate(dataCreateContext.currentTime);
			
			String relationQuestionOwnerId =  dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/40);
			String relationQuestionAnswerOwnerId =  dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/40);
			String questionAnswerId = dataCreateContext.getId(TestId.QuestionAnswerId.class, dataCreateContext.currentCount/2);
			String communityUserId =  dataCreateContext.getOtherCommunityUserId((dataCreateContext.currentCount/40+1)%(dataCreateContext.num/40));
			
			object.setRelationQuestionOwnerId(relationQuestionOwnerId);
			object.setRelationQuestionAnswerOwnerId(relationQuestionAnswerOwnerId);
			object.setQuestionAnswer(createObjectWithId(QuestionAnswerDO.class, questionAnswerId));
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
		}
	}

	@CreateCount(perCommunityUser=40)
	public static class UserAnswerCommentActionHistoryCreator2 extends DataCreator2<ActionHistoryDO, TestId.NullId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.QuestionId.class, dataCreateContext.num/4);
			dataCreateContext.readIdList(TestId.QuestionAnswerId.class, dataCreateContext.num/2);
			dataCreateContext.readIdList(TestId.QuestionAnswerCommentId.class, dataCreateContext.num);
		}

		@Override
		public void fillItems(ActionHistoryDO object, DataCreateContext2 dataCreateContext) {

			object.setActionHistoryType(ActionHistoryType.USER_ANSWER_COMMENT);
			object.setActionTime(dataCreateContext.currentTime);
			
			String commentId = dataCreateContext.getCurrentId(TestId.QuestionAnswerCommentId.class);
			String relationQuestionOwnerId =  dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/40);
			String relationQuestionAnswerOwnerId =  dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/40);
			String questionId = dataCreateContext.getId(TestId.QuestionId.class, dataCreateContext.currentCount/4);
			String questionAnswerId = dataCreateContext.getId(TestId.QuestionAnswerId.class, dataCreateContext.currentCount/2);
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/40);
			String sku = getSku(dataCreateContext.currentCount);
			
			object.setComment(createObjectWithId(CommentDO.class, commentId));
			object.setQuestion(createObjectWithId(QuestionDO.class, questionId));
			object.setRelationQuestionOwnerId(relationQuestionOwnerId);
			object.setRelationQuestionAnswerOwnerId(relationQuestionAnswerOwnerId);
			object.setQuestionAnswer(createObjectWithId(QuestionAnswerDO.class, questionAnswerId));
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));	
			object.setProduct(createObjectWithId(ProductDO.class, sku));
			object.setActionHistoryId(IdUtil.generateActionHistoryId(object, idGenerator));
		}
	}
	
	@CreateCount(perCommunityUser=40)
	public static class QuestionAnswerCommentAddInformationCreator2 extends DataCreator2<InformationDO, TestId.NullId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.QuestionId.class, dataCreateContext.num/4);
			dataCreateContext.readIdList(TestId.QuestionAnswerId.class, dataCreateContext.num/2);
			dataCreateContext.readIdList(TestId.QuestionAnswerCommentId.class, dataCreateContext.num);
		}

		@Override
		public void fillItems(InformationDO object, DataCreateContext2 dataCreateContext) {
			object.setInformationType(InformationType.QUESTION_ANSWER_COMMENT_ADD);
			object.setInformationTime(dataCreateContext.currentTime);
			
			String commentId = dataCreateContext.getCurrentId(TestId.QuestionAnswerCommentId.class);
			String relationQuestionOwnerId =  dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/40);
			String relationQuestionAnswerOwnerId =  dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/40);
			String questionId = dataCreateContext.getId(TestId.QuestionId.class, dataCreateContext.currentCount/4);
			String questionAnswerId = dataCreateContext.getId(TestId.QuestionAnswerId.class, dataCreateContext.currentCount/2);
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/20);
			
			object.setComment(createObjectWithId(CommentDO.class, commentId));
			object.setRelationCommentOwnerId(communityUserId);
			object.setRelationCommunityUserId(communityUserId);
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, relationQuestionAnswerOwnerId));	
			object.setQuestion(createObjectWithId(QuestionDO.class, questionId));
			object.setQuestionAnswer(createObjectWithId(QuestionAnswerDO.class, questionAnswerId));
			object.setRelationQuestionOwnerId(relationQuestionOwnerId);
			object.setInformationId(IdUtil.getInfomationId(object, idGenerator));
		}
	}	
	
	
	@CreateCount(perCommunityUser=40)
	public static class QuestionAnswerLikeCreator2 extends DataCreator2<LikeDO, TestId.NullId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.QuestionId.class, dataCreateContext.num/4);
			dataCreateContext.readIdList(TestId.QuestionAnswerId.class, dataCreateContext.num/2);
		}

		@Override
		public void fillItems(LikeDO object, DataCreateContext2 dataCreateContext) {

			int diff = dataCreateContext.currentCount % 2 + 1;
			
			String questionAnswerId = dataCreateContext.getId(TestId.QuestionAnswerId.class, dataCreateContext.currentCount/2);
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/20,diff);
			String likeId = IdUtil.createIdByConcatIds(communityUserId, questionAnswerId, LikeTargetType.QUESTION_ANSWER.getCode());
			
			object.setLikeId(likeId);
			object.setTargetType(LikeTargetType.QUESTION_ANSWER);
			object.setSku(getSku(dataCreateContext.currentCount/2));
			object.setPostDate(dataCreateContext.currentTime);
			
			String relationQuestionOwnerId =  dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/40);
			String relationQuestionAnswerOwnerId =  dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/40);
			
			object.setRelationQuestionOwnerId(relationQuestionOwnerId);
			object.setRelationQuestionAnswerOwnerId(relationQuestionAnswerOwnerId);
			object.setQuestionAnswer(createObjectWithId(QuestionAnswerDO.class, questionAnswerId));
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
		}
	}
	
	@CreateCount(perCommunityUser=40)
	public static class QuestionAnswerLikeAddInformationCreator2 extends DataCreator2<InformationDO, TestId.NullId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.QuestionId.class, dataCreateContext.num/4);
			dataCreateContext.readIdList(TestId.QuestionAnswerId.class, dataCreateContext.num/2);
		}

		@Override
		public void fillItems(InformationDO object, DataCreateContext2 dataCreateContext) {
			object.setInformationType(InformationType.QUESTION_ANSWER_LIKE_ADD);
			object.setInformationTime(dataCreateContext.currentTime);

			int diff = dataCreateContext.currentCount % 2 + 1;
			
			String questionAnswerId = dataCreateContext.getId(TestId.QuestionAnswerId.class, dataCreateContext.currentCount/2);
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/20,diff);
			String likeId = IdUtil.createIdByConcatIds(communityUserId, questionAnswerId, LikeTargetType.QUESTION_ANSWER.getCode());
			
			String relationQuestionOwnerId =  dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/40);
			String relationQuestionAnswerOwnerId =  dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/40);
			String questionId = dataCreateContext.getId(TestId.QuestionId.class, dataCreateContext.currentCount/4);
			String sku = getSku(dataCreateContext.currentCount);
			
			object.setQuestion(createObjectWithId(QuestionDO.class, questionId));
			object.setRelationQuestionOwnerId(relationQuestionOwnerId);
			object.setRelationQuestionAnswerOwnerId(relationQuestionAnswerOwnerId);
			object.setQuestionAnswer(createObjectWithId(QuestionAnswerDO.class, questionAnswerId));
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));	
			object.setProduct(createObjectWithId(ProductDO.class, sku));

			
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, relationQuestionAnswerOwnerId));	
			object.setQuestionAnswer(createObjectWithId(QuestionAnswerDO.class, questionAnswerId));
			object.setQuestion(createObjectWithId(QuestionDO.class, questionId));
			object.setLike(createObjectWithId(LikeDO.class, likeId));

			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, relationQuestionAnswerOwnerId));	
			object.setQuestionAnswer(createObjectWithId(QuestionAnswerDO.class, questionAnswerId));
			object.setQuestion(createObjectWithId(QuestionDO.class, questionId));
			object.setLike(createObjectWithId(LikeDO.class, likeId));
			object.setRelationLikeOwnerId(communityUserId);
			object.setRelationQuestionOwnerId(relationQuestionOwnerId);
			object.setRelationCommunityUserId(communityUserId);
			object.setInformationId(IdUtil.getInfomationId(object, idGenerator));
		}
	}
	
	@CreateCount(perCommunityUser=4)
	public static class ProductSetImageHeaderCreator2 extends DataCreator2<ImageHeaderDO, TestId.ProductSetImageId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.ProductSetThumbnailImageId.class, dataCreateContext.num);
			dataCreateContext.readIdList(TestId.ImageSetId.class, dataCreateContext.num/4);
		}
		
		@Override
		public void fillItems(ImageHeaderDO object, DataCreateContext2 dataCreateContext) {
			object.setImageId(dataCreateContext.getCurrentId());
			String communityUserId = dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/4);
			
			object.setOwnerCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
			object.setImageUrl(getImageUrl(object.getImageId(), "images/png", PostContentType.IMAGE_SET, false));
			object.setPostContentType(PostContentType.IMAGE_SET);
			object.setThumbnailImageId(dataCreateContext.getCurrentId(TestId.ProductSetThumbnailImageId.class));
			object.setThumbnailImageUrl(getImageUrl(object.getThumbnailImageId(), "images/png", PostContentType.IMAGE_SET, true));
			fillImageHeaderCommonItems(object, dataCreateContext);
			
			String imageSetId = dataCreateContext.getId(TestId.ImageSetId.class, dataCreateContext.currentCount/4);
			object.setImageSetId(imageSetId);
			object.setImageSetIndex(dataCreateContext.currentCount%4);
			
			object.setProduct(createObjectWithId(ProductDO.class, getSku(dataCreateContext.currentCount/4)));
			object.setPurchaseDate(dataCreateContext.currentTime);
			object.setPurchaseHistoryType(PurchaseHistoryType.YODOBASHI);
		}
	}
	
	@CreateCount(perCommunityUser=1)
	public static class UserImageActionHistoryCreator2 extends DataCreator2<ActionHistoryDO, TestId.NullId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.ImageSetId.class, dataCreateContext.num);
		}

		@Override
		public void fillItems(ActionHistoryDO object, DataCreateContext2 dataCreateContext) {

			object.setActionHistoryType(ActionHistoryType.USER_IMAGE);
			object.setActionTime(dataCreateContext.currentTime);
			
			String imageSetId = dataCreateContext.getId(TestId.ImageSetId.class, dataCreateContext.currentCount);
			String sku = getSku(dataCreateContext.currentCount);
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/20);

			object.setImageSetId(imageSetId);
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
			object.setProduct(createObjectWithId(ProductDO.class, sku));	
			object.setActionHistoryId(IdUtil.generateActionHistoryId(object, idGenerator));
		}
	}	
	
	@CreateCount(perCommunityUser=8)
	public static class ProductImageCommentCreator2 extends DataCreator2<CommentDO, TestId.ProductSetImageCommentId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.ImageSetId.class, dataCreateContext.num/8);
			dataCreateContext.readIdList(TestId.ProductSetImageId.class, dataCreateContext.num/2);
		}

		@Override
		public void fillItems(CommentDO object, DataCreateContext2 dataCreateContext) {
			object.setCommentId(dataCreateContext.getCurrentId());
			object.setCommentBody(StringUtils.repeat("画像コメント"+dataCreateContext.currentCountAsString, 2));
			object.setTargetType(CommentTargetType.IMAGE);
			object.setPostDate(dataCreateContext.currentTime);
			
			String imageSetId = dataCreateContext.getId(TestId.ImageSetId.class, dataCreateContext.currentCount/8);
			String relationImageOwnerId = dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/8);
			String imageHeaderId = dataCreateContext.getId(TestId.ProductSetImageId.class, dataCreateContext.currentCount/2);
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/8);
			
			object.setImageSetId(imageSetId);
			object.setRelationImageOwnerId(relationImageOwnerId);
			object.setImageHeader(createObjectWithId(ImageHeaderDO.class, imageHeaderId));
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
		}
	}	
	
	@CreateCount(perCommunityUser=8)
	public static class UserImageCommentActionHistoryCreator2 extends DataCreator2<ActionHistoryDO, TestId.NullId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.ProductSetImageCommentId.class, dataCreateContext.num);
			dataCreateContext.readIdList(TestId.ImageSetId.class, dataCreateContext.num/8);
			dataCreateContext.readIdList(TestId.ProductSetImageId.class, dataCreateContext.num/2);
		}

		@SuppressWarnings("unused")
		@Override
		public void fillItems(ActionHistoryDO object, DataCreateContext2 dataCreateContext) {

			object.setActionHistoryType(ActionHistoryType.USER_IMAGE_COMMENT);
			object.setActionTime(dataCreateContext.currentTime);

			String commentId = dataCreateContext.getCurrentId(TestId.ProductSetImageCommentId.class);
			String relationCommentOwnerId = dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/8);
			String imageSetId = dataCreateContext.getId(TestId.ImageSetId.class, dataCreateContext.currentCount/8);
			String relationImageOwnerId = dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/8);
			String imageHeaderId = dataCreateContext.getId(TestId.ProductSetImageId.class, dataCreateContext.currentCount/2);
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/8);
			String sku = getSku(dataCreateContext.currentCount/2);
			
			object.setComment(createObjectWithId(CommentDO.class, commentId));
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
			object.setImageHeader(createObjectWithId(ImageHeaderDO.class, imageHeaderId));
			object.setImageSetId(imageSetId);
			object.setRelationImageOwnerId(relationImageOwnerId);
			object.setProduct(createObjectWithId(ProductDO.class, sku));
			object.setActionHistoryId(IdUtil.generateActionHistoryId(object, idGenerator));
		}
	}
	
	@CreateCount(perCommunityUser=8)
	public static class UserImageCommentInformationCreator2 extends DataCreator2<InformationDO, TestId.NullId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.ProductSetImageCommentId.class, dataCreateContext.num);
			dataCreateContext.readIdList(TestId.ImageSetId.class, dataCreateContext.num/8);
			dataCreateContext.readIdList(TestId.ProductSetImageId.class, dataCreateContext.num/2);
		}

		@Override
		public void fillItems(InformationDO object, DataCreateContext2 dataCreateContext) {
			object.setInformationType(InformationType.IMAGE_COMMENT_ADD);
			object.setInformationTime(dataCreateContext.currentTime);
			
			String commentId = dataCreateContext.getCurrentId(TestId.ProductSetImageCommentId.class);
			String relationImageOwnerId = dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/8);
			String imageHeaderId = dataCreateContext.getId(TestId.ProductSetImageId.class, dataCreateContext.currentCount/2);
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/8);
			
			object.setComment(createObjectWithId(CommentDO.class, commentId));
			object.setRelationCommentOwnerId(communityUserId);
			object.setRelationCommunityUserId(communityUserId);
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, relationImageOwnerId));
			object.setImageHeader(createObjectWithId(ImageHeaderDO.class, imageHeaderId));
			object.setInformationId(IdUtil.getInfomationId(object, idGenerator));

		}
	}
	
	@CreateCount(perCommunityUser=8)
	public static class ProductImageLikeCreator2 extends DataCreator2<LikeDO, TestId.NullId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.ImageSetId.class, dataCreateContext.num/8);
			dataCreateContext.readIdList(TestId.ProductSetImageId.class, dataCreateContext.num/2);
		}

		@Override
		public void fillItems(LikeDO object, DataCreateContext2 dataCreateContext) {

			int diff = dataCreateContext.currentCount % 2 + 1;
			
			String imageHeaderId = dataCreateContext.getId(TestId.ProductSetImageId.class, dataCreateContext.currentCount/2);
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/8,diff);
			String likeId = IdUtil.createIdByConcatIds(communityUserId, imageHeaderId, LikeTargetType.IMAGE.getCode());
			
			object.setLikeId(likeId);
			object.setTargetType(LikeTargetType.IMAGE);
			object.setSku(getSku(dataCreateContext.currentCount/2));
			object.setPostDate(dataCreateContext.currentTime);
			
			String imageSetId = dataCreateContext.getId(TestId.ImageSetId.class, dataCreateContext.currentCount/8);
			String relationImageOwnerId = dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/40);
			
			object.setImageSetId(imageSetId);
			object.setRelationImageOwnerId(relationImageOwnerId);
			object.setImageHeader(createObjectWithId(ImageHeaderDO.class, imageHeaderId));
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
		}
	}
	
	@CreateCount(perCommunityUser=8)
	public static class ImageLikeAddInformationCreator2 extends DataCreator2<InformationDO, TestId.NullId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.ImageSetId.class, dataCreateContext.num/8);
			dataCreateContext.readIdList(TestId.ProductSetImageId.class, dataCreateContext.num/2);
		}

		@Override
		public void fillItems(InformationDO object, DataCreateContext2 dataCreateContext) {
			object.setInformationType(InformationType.IMAGE_LIKE_ADD);
			object.setInformationTime(dataCreateContext.currentTime);
			
			int diff = dataCreateContext.currentCount % 2 + 1;
			
			String imageHeaderId = dataCreateContext.getId(TestId.ProductSetImageId.class, dataCreateContext.currentCount/2);
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/20,diff);
			String likeId = IdUtil.createIdByConcatIds(communityUserId, imageHeaderId, LikeTargetType.IMAGE.getCode());			
			
			String relationImageOwnerId = dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/8);
			
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, relationImageOwnerId));
			object.setImageHeader(createObjectWithId(ImageHeaderDO.class, imageHeaderId));
			object.setLike(createObjectWithId(LikeDO.class, likeId));
			object.setRelationLikeOwnerId(communityUserId);
			object.setRelationCommunityUserId(communityUserId);
			object.setInformationId(IdUtil.getInfomationId(object, idGenerator));
		}
	}	
		
	
	@CreateCount(perCommunityUser=4)
	public static class ProductSetThumbnailImageHeaderCreator2 extends DataCreator2<ImageHeaderDO, TestId.ProductSetThumbnailImageId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.ImageSetId.class, dataCreateContext.num/4);
		}
		
		@Override
		public void fillItems(ImageHeaderDO object, DataCreateContext2 dataCreateContext) {
			object.setImageId(dataCreateContext.getCurrentId());
			String communityUserId = dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/4);
			
			
			object.setOwnerCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
			object.setImageUrl(getImageUrl(object.getImageId(), "images/png", PostContentType.IMAGE_SET, true));
			object.setPostContentType(PostContentType.IMAGE_SET);
			fillThumbnailImageHeaderCommonItems(object, dataCreateContext);
			
			String imageSetId = dataCreateContext.getId(TestId.ImageSetId.class, dataCreateContext.currentCount/4);
			object.setImageSetId(imageSetId);
			object.setImageSetIndex(dataCreateContext.currentCount%4);
			
			object.setProduct(createObjectWithId(ProductDO.class, getSku(dataCreateContext.currentCount/4)));
			object.setPurchaseDate(dataCreateContext.currentTime);
			object.setPurchaseHistoryType(PurchaseHistoryType.YODOBASHI);
		}
	}

	@CreateCount(perCommunityUser=4)
	public static class ProductSetImageCreator2 extends DataCreator2<ImageDO, TestId.ProductSetImageId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
		}

		@Override
		public void fillItems(ImageDO object, DataCreateContext2 dataCreateContext) {
			object.setImageId(dataCreateContext.getCurrentId());
			object.setMimeType("images/png");
			object.setCommunityUserId(dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/4));
			object.setImageUrl(getImageUrl(object.getImageId(), object.getMimeType(), PostContentType.IMAGE_SET, false));
			object.setData(getProductImage());
			object.setWidth(210);
			object.setHeigth(210);
		}
	}
	
	@CreateCount(perCommunityUser=4)
	public static class ProductSetThumbnailImageCreator2 extends DataCreator2<ImageDO, TestId.ProductSetThumbnailImageId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
		}

		@Override
		public void fillItems(ImageDO object, DataCreateContext2 dataCreateContext) {
			object.setImageId(dataCreateContext.getCurrentId());
			object.setMimeType("images/png");			
			object.setCommunityUserId(dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/4));
			object.setImageUrl(getImageUrl(object.getImageId(), object.getMimeType(), PostContentType.IMAGE_SET, true));
			object.setData(getProductThumbnailImage());
			object.setWidth(50);
			object.setHeigth(50); 
		}
	}
	

	@CreateCount(perCommunityUser=2)
	public static class CommunityUserFollowCreator2 extends
	DataCreator2<CommunityUserFollowDO, TestId.NullId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
		}
		
		@Override
		public void fillItems(CommunityUserFollowDO object, DataCreateContext2 dataCreateContext) {
		
			String communityUserId = dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/2);
			String followCommunityUserId =  dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/2);
			String communityUserFollowId =  IdUtil.createIdByConcatIds(communityUserId, followCommunityUserId);
		
			object.setCommunityUserFollowId(communityUserFollowId);
			object.setFollowDate(dataCreateContext.currentTime);
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
			object.setFollowCommunityUser(createObjectWithId(CommunityUserDO.class, followCommunityUserId));
			
		}
	}
	
	@CreateCount(perCommunityUser=2)
	public static class UserFollowUserActionHistoryCreator2 extends DataCreator2<ActionHistoryDO, TestId.UserFollowUserActionHistoryId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
		}

		@Override
		public void fillItems(ActionHistoryDO object, DataCreateContext2 dataCreateContext) {
			object.setActionHistoryId(dataCreateContext.getCurrentId());
			object.setActionHistoryType(ActionHistoryType.USER_FOLLOW_USER);
			object.setActionTime(dataCreateContext.currentTime);
			
			String communityUserId = dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/2);
			String followCommunityUserId =  dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/2);

			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
			object.setFollowCommunityUser(createObjectWithId(CommunityUserDO.class, followCommunityUserId));
		}
	}
	
	@CreateCount(perCommunityUser=2)
	public static class FollowInformationCreator2 extends DataCreator2<InformationDO, TestId.FollowInformationId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
		}

		@Override
		public void fillItems(InformationDO object, DataCreateContext2 dataCreateContext) {
			object.setInformationId(dataCreateContext.getCurrentId());
			object.setInformationType(InformationType.FOLLOW);
			object.setInformationTime(dataCreateContext.currentTime);
			
			String communityUserId = dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/2);
			String followCommunityUserId =  dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/2);
			
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, followCommunityUserId));
			object.setFollowerCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
			object.setRelationCommunityUserId(communityUserId);
		}
	}	
	

	@CreateCount(perCommunityUser=2)
	public static class ProductFollowCreator2 extends DataCreator2<ProductFollowDO, TestId.NullId> {
	
		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
		}
		
		@Override
		public void fillItems(ProductFollowDO object, DataCreateContext2 dataCreateContext) {
		
			String communityUserId = dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/2);
			String sku =  getSku(dataCreateContext.currentCount);
			String productFollowId =  IdUtil.createIdByConcatIds(communityUserId, sku);
		
			object.setProductFollowId(productFollowId);
			object.setFollowDate(dataCreateContext.currentTime);
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
			object.setFollowProduct(createObjectWithId(ProductDO.class, sku));
			
		}
	}
	
	@CreateCount(perCommunityUser=2)
	public static class UserFollowProductActionHistoryCreator2 extends DataCreator2<ActionHistoryDO, TestId.UserFollowProductActionHistoryId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
		}

		@Override
		public void fillItems(ActionHistoryDO object, DataCreateContext2 dataCreateContext) {
			object.setActionHistoryId(dataCreateContext.getCurrentId());
			object.setActionHistoryType(ActionHistoryType.USER_FOLLOW_PRODUCT);
			object.setActionTime(dataCreateContext.currentTime);
			
			String communityUserId = dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/2);
			String sku =  getSku(dataCreateContext.currentCount);

			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
			object.setProduct(createObjectWithId(ProductDO.class, sku));
		}
	}	

	@CreateCount(perCommunityUser=2)
	public static class QuestionFollowCreator2 extends DataCreator2<QuestionFollowDO, TestId.NullId> {
		
		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.QuestionId.class, dataCreateContext.num*5);
		}
		
		@Override
		public void fillItems(QuestionFollowDO object, DataCreateContext2 dataCreateContext) {
		
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/2);
			String questionId =  dataCreateContext.getId(TestId.QuestionId.class, dataCreateContext.currentCount*5);
			String questionFollowId =  IdUtil.createIdByConcatIds(communityUserId, questionId);
		
			object.setQuestionFollowId(questionFollowId);
			object.setFollowDate(dataCreateContext.currentTime);
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
			object.setFollowQuestion(createObjectWithId(QuestionDO.class, questionId));
		}
	}
	
	@CreateCount(perCommunityUser=2)
	public static class UserFollowQuestionActionHistoryCreator2 extends DataCreator2<ActionHistoryDO, TestId.UserFollowQuestionActionHistoryId> {

		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.QuestionId.class, dataCreateContext.num*5);
		}

		@Override
		public void fillItems(ActionHistoryDO object, DataCreateContext2 dataCreateContext) {
			object.setActionHistoryId(dataCreateContext.getCurrentId());
			object.setActionHistoryType(ActionHistoryType.USER_FOLLOW_QUESTION);
			object.setActionTime(dataCreateContext.currentTime);
			
			String relationQuestionOwnerId = dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/2);
			String sku = getSku(dataCreateContext.currentCount*5);
			String communityUserId = dataCreateContext.getOtherCommunityUserId(dataCreateContext.currentCount/2);
			String questionId =  dataCreateContext.getId(TestId.QuestionId.class, dataCreateContext.currentCount*5);

			object.setRelationQuestionOwnerId(relationQuestionOwnerId);
			object.setProduct(createObjectWithId(ProductDO.class, sku));
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
			object.setQuestion(createObjectWithId(QuestionDO.class, questionId));
		}
	}

	// SKUの数
	@CreateCount(perCommunityUser=100)
	public static class PurchaseProductCreator2 extends DataCreator2<PurchaseProductDO, TestId.NullId> {
		
		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
		}
		
		@Override
		public void fillItems(PurchaseProductDO object, DataCreateContext2 dataCreateContext) {
		
			String communityUserId = dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/100);
			String sku =  getSku(dataCreateContext.currentCount);
			String purchaseProductId =  IdUtil.createIdByConcatIds(communityUserId, sku);
			
			object.setPurchaseProductId(purchaseProductId);
			object.setPurchaseDateFix(true);
			object.setPurchaseDate(dataCreateContext.currentTime);
			object.setUserInputPurchaseDate(dataCreateContext.currentTime);
			object.setPurchaseHistoryType(PurchaseHistoryType.YODOBASHI);
		
			object.setProduct(createObjectWithId(ProductDO.class, sku));
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));

		}
	}

	//SKU * 5
	@CreateCount(fixed=50)
	public static class DecisivePurchaseCreator2 extends DataCreator2<DecisivePurchaseDO, TestId.NullId> {
		
		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			Asserts.equals(SKU_LIST.size()*5, dataCreateContext.num);
		}
		
		@Override
		public void fillItems(DecisivePurchaseDO object, DataCreateContext2 dataCreateContext) {
			String sku = getSku(dataCreateContext.currentCount * 5);
			String decisivePurchaseId = IdUtil.createIdByBranchNo(sku, dataCreateContext.currentCount % 5);
			
			object.setDecisivePurchaseId(decisivePurchaseId);
			object.setSku(sku);
			object.setDecisivePurchaseName("購入の決め手"+dataCreateContext.currentCountAsString);

		}
	}

	
	@CreateCount(perCommunityUser=10)
	public static class ReviewDecisivePurchaseCreator2 extends DataCreator2<ReviewDecisivePurchaseDO, TestId.NullId> {
		
		@Override
		protected void beforeCreate(DataCreateContext2 dataCreateContext) {
			dataCreateContext.readIdList(TestId.ReviewId.class, dataCreateContext.num);
		}
		
		@Override
		public void fillItems(ReviewDecisivePurchaseDO object, DataCreateContext2 dataCreateContext) {
			String reviewId = dataCreateContext.getId(TestId.ReviewId.class, dataCreateContext.currentCount);
			String sku = getSku(dataCreateContext.currentCount);
			String communityUserId = dataCreateContext.getId(TestId.CommunityUserId.class, dataCreateContext.currentCount/10);
			String decisivePurchaseId = IdUtil.createIdByBranchNo(sku, 0);
			String reviewDecisivePurchaseId = IdUtil.createIdByBranchNo(reviewId, 0);
			
			object.setReviewDecisivePurchaseId(reviewDecisivePurchaseId);
			object.setPurchaseDate(dataCreateContext.currentTime);
			object.setSku(sku);
			object.setDecisivePurchase(createObjectWithId(DecisivePurchaseDO.class, decisivePurchaseId));
			object.setCommunityUser(createObjectWithId(CommunityUserDO.class, communityUserId));
		}
	}
	
//	@CreateCount(fixed=400000)
//	public static class SlipHeaderCreator2 extends DataCreator2<SlipHeaderDO, TestId.NullId> {
//
//		@Override
//		public void fillItems(SlipHeaderDO object, DataCreateContext2 dataCreateContext) {
//			String outerCustomerId = new DecimalFormat("0000000000").format(dataCreateContext.currentCount%this.communityUserNum);
//			String slipNo = new DecimalFormat("0000000000").format(dataCreateContext.currentCount);
//			object.setOuterCustomerId(outerCustomerId);
//			object.setSlipNo(slipNo);
//			object.setOrderEntryDate(dataCreateContext.currentTime);
//			object.setDeliverType(DeliverType.CENTER);
//			object.setEffectiveSlipType(EffectiveSlipType.EFFECTIVE);
//			object.setOrderEntryType(OrderEntryType.EC);
//		}
//		
//	}
//	
//	@CreateCount(fixed=1600000)
//	public static class SlipDetailCreator2 extends DataCreator2<SlipDetailDO, TestId.NullId> {
//
//		@Override
//		public void fillItems(SlipDetailDO object, DataCreateContext2 dataCreateContext) {
//			String outerCustomerId = new DecimalFormat("0000000000").format(dataCreateContext.currentCount%this.communityUserNum);
//			String slipNo = new DecimalFormat("0000000000").format(dataCreateContext.currentCount);
//			int slipDetailNo = dataCreateContext.currentCount % 4 + 1;
//			object.setOuterCustomerId(outerCustomerId);
//			object.setSlipNo(slipNo);
//			object.setSlipDetailNo(slipDetailNo);
//			object.setEffectiveNum(9);
//			object.setJanCode("9999999999");
//			object.setOldestBillingDate(dataCreateContext.currentTime);
//			object.setReturnedNum(9);
//			object.setSalesRegistDetailType(SalesRegistDetailType.EFFECTIVE);
//			object.setSetCouponId("9999999999");
//			object.setSlipDetailCategory(SlipDetailCategory.SERVICE);
//		}
//		
//	}
//
//	@CreateCount(fixed=1500000)
//	public static class ReceiptHeaderCreator2 extends DataCreator2<ReceiptHeaderDO, TestId.NullId> {
//
//		@Override
//		public void fillItems(ReceiptHeaderDO object, DataCreateContext2 dataCreateContext) {
//			String outerCustomerId = new DecimalFormat("0000000000").format(dataCreateContext.currentCount%this.communityUserNum);
//			String receiptNo = new DecimalFormat("0000000000").format(dataCreateContext.currentCount);
//			object.setOuterCustomerId(outerCustomerId);
//			object.setReceiptNo(receiptNo);
//			object.setEffectiveSlipType(EffectiveSlipType.EFFECTIVE);
//			object.setReceiptRegistType(ReceiptRegistType.ACCOUNTING);
//			object.setSalesDate(dataCreateContext.currentTime);
//		}
//		
//	}
//	
//	@CreateCount(fixed=6000000)
//	public static class ReceiptDetailCreator2 extends DataCreator2<ReceiptDetailDO, TestId.NullId> {
//
//		@Override
//		public void fillItems(ReceiptDetailDO object, DataCreateContext2 dataCreateContext) {
//			String outerCustomerId = new DecimalFormat("0000000000").format(dataCreateContext.currentCount%this.communityUserNum);
//			String receiptNo = new DecimalFormat("0000000000").format(dataCreateContext.currentCount);
//			int receiptDetailNo = dataCreateContext.currentCount % 4 + 1;
//			object.setOuterCustomerId(outerCustomerId);
//			object.setReceiptNo(receiptNo);
//			object.setReceiptDetailNo(receiptDetailNo);
//			object.setJanCode("9999999999");
//			object.setNetNum(9);
//			object.setNotRegistDetailNo(9);
//			object.setOrderEntryDetailNo(9);
//			object.setReceiptDetailType(ReceiptDetailType.PRODUCT);
//			object.setReceiptType(ReceiptType.NORMAL);
//			object.setRefSalesDetailNo(9);
//			object.setRefSlipDetailNo(1);
//			object.setRefSlipNo("0000000001");
//			object.setSalesRegistDetailType(SalesRegistDetailType.EFFECTIVE);
//			object.setSetCouponId("9999999999");
//			object.setSetReceiptDetailNo("9999999999");
//		}
//		
//	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////

	static class DataCreateContext2 {
		
		int currentCount;
		@SuppressWarnings("rawtypes")
		Map<Class<? extends TestId.Target>, List<String>> idListMap = Maps.newHashMap();
		@SuppressWarnings("rawtypes")
		Class<? extends TestId.Target> idType;
		Date currentTime;
		int num;
		int communityUserNum;
		String currentCountAsString;
		
		@SuppressWarnings("rawtypes")
		<T extends TestId.Target> void readIdList(Class<T> idType, int num) {
			if (!idListMap.containsKey(idType) || idListMap.get(idType).size() < num) {
				idListMap.put(idType, TestId.readIdList(idType, num));				
			}
		}
		
		@SuppressWarnings("rawtypes")
		String getId(Class<? extends TestId.Target> idType, int num) {
			return idListMap.get(idType).get(num);
		}
		
		@SuppressWarnings("rawtypes")
		String getCurrentId(Class<? extends TestId.Target> idType) {
			return idListMap.get(idType).get(currentCount);
		}
		
		String getCurrentId() {
			return idListMap.get(idType).get(currentCount);
		}
		
		String getOtherCommunityUserId(int myUserCounter) {
			return getOtherCommunityUserId(myUserCounter, 1);
		}
		
		String getOtherCommunityUserId(int myUserCounter, int diff) {
			Asserts.isFalse(diff == 0);
//			System.err.println("HOGE--->"+myUserCounter+":"+communityUserNum);
//			System.err.println(myUserCounter);
//			System.err.println(communityUserNum);
//			System.err.println((myUserCounter == this.communityUserNum-1) ? 0 : myUserCounter+1);

			return getId(TestId.CommunityUserId.class, ((myUserCounter+diff) % this.communityUserNum));
		}
	}


}
