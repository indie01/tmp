/**
 *
 */
package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.kickmogu.lib.core.resource.annotation.BelongsTo;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseIndex;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.lib.solr.annotation.SolrSchema;
import com.kickmogu.lib.solr.annotation.SolrUniqKey;
import com.kickmogu.yodobashi.community.resource.dao.util.UserUtil;
import com.kickmogu.yodobashi.community.resource.domain.util.CommunityUserUtil;


/**
 * レビューの購入の決め手情報です。
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
},sizeGroup=SizeGroup.MEDIUM)
@SolrSchema
public class ReviewDecisivePurchaseDO extends BaseWithTimestampDO implements StoppableContents {

	/**
	 *
	 */
	private static final long serialVersionUID = 5275753777856460317L;

	/**
	 * レビューの購入の決め手情報IDです。
	 */
	@HBaseKey(createTableSplitKeys={"1","5","9","D","H","L","P","T","X","b","f","j","n","r","v"})
	@SolrField @SolrUniqKey
	private String reviewDecisivePurchaseId;

	/**
	 * 一時的な購入の決め手名称です。
	 */
	@HBaseColumn
	@SolrField
	private String temporaryDecisivePurchaseName;

	/**
	 * 購入日時です。
	 */
	@HBaseColumn
	@SolrField
	private Date purchaseDate;

	/**
	 * レビュー情報です。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo ReviewDO review;

	/**
	 * レビュー履歴情報です。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo ReviewHistoryDO reviewHistory;

	/**
	 * 購入の決め手情報です。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo DecisivePurchaseDO decisivePurchase;

	/**
	 * コミュニティユーザーです。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo CommunityUserDO communityUser;

	/**
	 * SKU です。
	 */
	@HBaseColumn
	@SolrField
	private String sku;

	/**
	 * 一時保存データかどうかです。
	 */
	@HBaseColumn
	@SolrField
	private boolean temporary;

	/**
	 * 有効フラグです。
	 */
	@HBaseColumn
	@SolrField
	private boolean effective;

	/**
	 * 削除フラグです。
	 */
	@HBaseColumn
	@SolrField
	private boolean deleteFlag;

	/**
	 * 退会データかどうかです。
	 */
	@HBaseColumn
	@SolrField
	private boolean withdraw;

	/**
	 * 退会キーです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex
	private String withdrawKey;

	/**
	 * @return withdraw
	 */
	public boolean isWithdraw() {
		return withdraw;
	}

	/**
	 * @param withdraw セットする withdraw
	 */
	public void setWithdraw(boolean withdraw) {
		this.withdraw = withdraw;
	}

	/**
	 * 削除済かどうか返します。
	 * @return 削除済の場合、true
	 */
	public boolean isDeleted() {
		return withdraw;
	}

	public String getWithdrawKey() {
		return withdrawKey;
	}

	public void setWithdrawKey(String withdrawKey) {
		this.withdrawKey = withdrawKey;
	}

	/**
	 * @return review
	 */
	public ReviewDO getReview() {
		return review;
	}

	/**
	 * @param review セットする review
	 */
	public void setReview(ReviewDO review) {
		this.review = review;
	}

	/**
	 * @return decisivePurchase
	 */
	public DecisivePurchaseDO getDecisivePurchase() {
		return decisivePurchase;
	}

	/**
	 * @param decisivePurchase セットする decisivePurchase
	 */
	public void setDecisivePurchase(DecisivePurchaseDO decisivePurchase) {
		this.decisivePurchase = decisivePurchase;
	}

	/**
	 * @return communityUser
	 */
	public CommunityUserDO getCommunityUser() {
		return communityUser;
	}

	/**
	 * @param communityUser セットする communityUser
	 */
	public void setCommunityUser(CommunityUserDO communityUser) {
		this.communityUser = communityUser;
	}

	/**
	 * @return reviewDecisivePurchaseId
	 */
	public String getReviewDecisivePurchaseId() {
		return reviewDecisivePurchaseId;
	}

	/**
	 * @param reviewDecisivePurchaseId セットする reviewDecisivePurchaseId
	 */
	public void setReviewDecisivePurchaseId(String reviewDecisivePurchaseId) {
		this.reviewDecisivePurchaseId = reviewDecisivePurchaseId;
	}

	/**
	 * @return temporaryDecisivePurchaseName
	 */
	public String getTemporaryDecisivePurchaseName() {
		return temporaryDecisivePurchaseName;
	}

	/**
	 * @param temporaryDecisivePurchaseName セットする temporaryDecisivePurchaseName
	 */
	public void setTemporaryDecisivePurchaseName(
			String temporaryDecisivePurchaseName) {
		this.temporaryDecisivePurchaseName = temporaryDecisivePurchaseName;
	}

	/**
	 * @return sku
	 */
	public String getSku() {
		return sku;
	}

	/**
	 * @param sku セットする sku
	 */
	public void setSku(String sku) {
		this.sku = sku;
	}

	/**
	 * @return reviewHistory
	 */
	public ReviewHistoryDO getReviewHistory() {
		return reviewHistory;
	}

	/**
	 * @param reviewHistory セットする reviewHistory
	 */
	public void setReviewHistory(ReviewHistoryDO reviewHistory) {
		this.reviewHistory = reviewHistory;
	}

	/**
	 * @return temporary
	 */
	public boolean isTemporary() {
		return temporary;
	}

	/**
	 * @param temporary セットする temporary
	 */
	public void setTemporary(boolean temporary) {
		this.temporary = temporary;
	}

	/**
	 * @return effective
	 */
	public boolean isEffective() {
		return effective;
	}

	/**
	 * @param effective セットする effective
	 */
	public void setEffective(boolean effective) {
		this.effective = effective;
	}

	/**
	 * 一時停止フラグです。
	 */
	@HBaseColumn
	@SolrField
	private boolean stopFlg=false;

	/**
	 * 関連オーナーのリストを返します。
	 * @return 関連オーナーのリスト
	 */
	@Override
	public List<CommunityUserDO> getRelationOwners() {
		return CommunityUserUtil.getRelationOwners(this);
	}

	/**
	 * 関連オーナーIDのリストを返します。
	 * @return 関連オーナーIDのリスト
	 */
	@Override
	public List<String> getRelationOwnerIds() {
		return CommunityUserUtil.getRelationOwnerIds(this);
	}

	/**
	 * 一時停止中かどうかを返します。
	 * @param communityUserDao コミュニティユーザー
	 * @param stopCommunityUserIds 一時停止中のコミュニティユーザーIDのリスト
	 * @return 一時停止中の場合、true
	 */
	@Override
	public boolean isStop(String communityUserId,
			Set<String> stopCommunityUserIds) {
		return UserUtil.isStop(this, communityUserId, stopCommunityUserIds);
	}

	/**
	 * @return purchaseDate
	 */
	public Date getPurchaseDate() {
		return purchaseDate;
	}

	/**
	 * @param purchaseDate セットする purchaseDate
	 */
	public void setPurchaseDate(Date purchaseDate) {
		this.purchaseDate = purchaseDate;
	}

	/**
	 * @return deleteFlag
	 */
	public boolean isDeleteFlag() {
		return deleteFlag;
	}

	/**
	 * @param deleteFlag セットする deleteFlag
	 */
	public void setDeleteFlag(boolean deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	/**
	 * @return the stopFlg
	 */
	public boolean isStopFlg() {
		return stopFlg;
	}

	/**
	 * @param stopFlg the stopFlg to set
	 */
	public void setStopFlg(boolean stopFlg) {
		this.stopFlg = stopFlg;
	}
	
	
}
