/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.resource.dao.MaintenanceDao;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipHeaderDO;

/**
 * SKU 変換エラー DAO の実装です。
 * @author kamiike
 *
 */
@Service
public class MaintenanceDaoImpl implements MaintenanceDao {

	/**
	 * HBaseアクセサです。
	 */
	@Autowired  @Qualifier("default")
	private HBaseOperations hBaseOperations;

	/**
	 * Solrアクセサです。
	 */
	@Autowired @Qualifier("default")
	private SolrOperations solrOperations;

	/**
	 * タイムスタンプホルダーです。
	 */
	@Autowired
	private TimestampHolder timestampHolder;

	@Override
	public void saveSlipHeader(SlipHeaderDO slipHeader) {
		
		SlipHeaderDO header = hBaseOperations.load(SlipHeaderDO.class, slipHeader.getId());
		
		header.setDeliverType(slipHeader.getDeliverType());
		header.setEffectiveSlipType(slipHeader.getEffectiveSlipType());
		
		//TODO
//		header.setOrderEntryDate(slipHeader.getOrderEntryDate());
		header.setOrderEntryType(slipHeader.getOrderEntryType());
		header.setModifyDateTime(timestampHolder.getTimestamp());
		header.setSlipNo(slipHeader.getSlipNo());
		hBaseOperations.save(header);
		solrOperations.save(header);
	}

	@Override
	public <T> T load(Class<T> clazz, String key) {
		return hBaseOperations.load(clazz, key);
	}

	@Override
	public void saveSlipDetail(SlipDetailDO slipDetail) {
		SlipDetailDO detail = hBaseOperations.load(SlipDetailDO.class, slipDetail.getId());
		detail.setSlipNo(slipDetail.getSlipNo());
		detail.setSlipDetailNo(slipDetail.getSlipDetailNo());
		detail.setSlipDetailCategory(slipDetail.getSlipDetailCategory());
		detail.setJanCode(slipDetail.getJanCode());
		detail.setEffectiveNum(slipDetail.getEffectiveNum());
		detail.setReturnedNum(slipDetail.getReturnedNum());
		detail.setSetCouponId(StringUtils.trimToNull(slipDetail.getSetCouponId()));
		detail.setSetParentDetailNo(slipDetail.getSetParentDetailNo());
		detail.setSalesRegistDetailType(slipDetail.getSalesRegistDetailType());
		// TODO
		//detail.setOldestBillingDate(oldestBillingDate);

		detail.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(detail);
		solrOperations.save(detail);
	}

	@Override
	public void savePurchaseProduct(PurchaseProductDO purchaseProduct) {
		
		PurchaseProductDO purchase = hBaseOperations.load(PurchaseProductDO.class, purchaseProduct.getPurchaseProductId());
		purchase.setPurchaseDateFix(purchaseProduct.isPurchaseDateFix());
		// TODO
//		purchase.setPurchaseDate(purchaseProduct.getPurchaseDate());
		purchase.setPurchaseDateRefDataType(purchaseProduct.getPurchaseDateRefDataType());
		purchase.setPurchaseDateRefId(purchaseProduct.getPurchaseDateRefId());
		// TODO
//		purchase.setBillingDate(purchaseProduct.getBillingDate());
		purchase.setBillingDateRefDataType(purchaseProduct.getBillingDateRefDataType());
		purchase.setBillingDateRefId(purchaseProduct.getBillingDateRefId());
		purchase.setOrderDate(purchaseProduct.getOrderDate());
		purchase.setOrderDateRefDataType(purchaseProduct.getOrderDateRefDataType());
		purchase.setOrderDateRefId(purchaseProduct.getOrderDateRefId());
		purchase.setJanCode(purchaseProduct.getJanCode());
//TODO 
//		purchase.setDeleteDate(purchaseProduct.getDeleteDate());
		//TODO
//		purchase.setUserInputPurchaseDate(purchaseProduct.getUserInputPurchaseDate());
		purchase.setPurchaseHistoryType(purchaseProduct.getPurchaseHistoryType());
		purchase.setPublicSetting(purchaseProduct.isPublicSetting());
		purchase.setAdult(purchaseProduct.isAdult());
		purchase.setWithdraw(purchaseProduct.isWithdraw());
		purchase.setWithdrawKey(StringUtils.trimToNull(purchaseProduct.getWithdrawKey()));
		purchase.setShare(purchaseProduct.isShare());
		purchase.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(purchase);
		solrOperations.save(purchase);
	}

	@Override
	public void saveCommunityUser(CommunityUserDO communityUser) {
		CommunityUserDO user = hBaseOperations.load(CommunityUserDO.class, communityUser.getCommunityUserId());
		user.setModifyDateTime(timestampHolder.getTimestamp());
		user.setSecureAccess(communityUser.isSecureAccess());
		user.setWithdrawKey(communityUser.getWithdrawKey());
		user.setWithdrawLock(communityUser.isWithdrawLock());
		user.setKeepReviewContents(communityUser.isKeepReviewContents());
		user.setKeepQuestionContents(communityUser.isKeepQuestionContents());
		user.setKeepImageContents(communityUser.isKeepImageContents());
		user.setKeepCommentContents(communityUser.isKeepCommentContents());
		user.setCommunityNameMergeRequired(communityUser.isCommunityNameMergeRequired());
		user.setStatus(communityUser.getStatus());
		user.setCeroVerification(communityUser.getCeroVerification());
		user.setAdultVerification(communityUser.getAdultVerification());
		
		hBaseOperations.save(user);
		solrOperations.save(user);

	}

//	@Override
//	public void saveComment(CommentDO commentDO) {
//		CommentDO comment = hBaseOperations.load(CommentDO.class, commentDO.getCommentId());
//		comment.setModifyDateTime(timestampHolder.getTimestamp());
//		
//		comment.setWithdraw(commentDO.isWithdraw());
//		comment.setCommentId(commentDO.getCommentId());
//		comment.setCommentBody(commentDO.getCommentBody());
//		comment.setTargetType(commentDO.getTargetType());
//		comment.setReview(commentDO.getReview());
//		comment.setQuestionAnswer(commentDO.getQuestionAnswer());
//		comment.setPostDate(commentDO.getPostDate());
//		comment.setDeleteDate(commentDO.getDeleteDate());
//		comment.setDeleteFlag(commentDO.isDeleteFlag());
//		comment.setCommunityUser(commentDO.getCommunityUser());
//		comment.setQuestionId(commentDO.getQuestionId());
//		comment.setWithdrawKey(commentDO.getWithdrawKey());
//		comment.setImageHeader(commentDO.getImageHeader());
//		comment.setRelationReviewOwnerId(commentDO.getRelationReviewOwnerId());
//		comment.setRelationQuestionOwnerId(commentDO.getRelationQuestionOwnerId());
//		comment.setRelationQuestionAnswerOwnerId(commentDO.getRelationQuestionOwnerId());
//		comment.setRelationImageOwnerId(commentDO.getRelationImageOwnerId());
//		comment.setImageSetId(commentDO.getImageSetId());
//		comment.setAdult(commentDO.isAdult());
//		comment.setSaveDate(commentDO.getSaveDate());
//		comment.setMngToolOperation(commentDO.isMngToolOperation());
//		comment.setStopFlg(commentDO.isStopFlg());
//
//		hBaseOperations.save(comment);
//		solrOperations.save(comment);
//
//	}
}
