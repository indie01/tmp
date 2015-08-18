/**
 *
 */
package com.kickmogu.yodobashi.community.resource.domain;

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
 * 購入を迷った商品情報です。
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
},sizeGroup=SizeGroup.MEDIUM)
@SolrSchema
public class PurchaseLostProductDO extends BaseWithTimestampDO implements StoppableContents {

	/**
	 *
	 */
	private static final long serialVersionUID = 1220035225780522353L;

	/**
	 * 購入を迷った商品情報IDです。
	 */
	@HBaseKey(createTableSplitKeys={"1","5","9","D","H","L","P","T","X","b","f","j","n","r","v"})
	@SolrField @SolrUniqKey
	private String purchaseLostProductId;

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
	 * レビューの商品情報です。
	 */
	@HBaseColumn
	@SolrField
	private String reviewProductId;

	/**
	 * 商品情報です。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo ProductDO product;

	/**
	 * 商品名称です。
	 */
	@HBaseColumn
	@SolrField
	private String productName;

	/**
	 * コミュニティユーザーです。
	 */
	@SolrField
	@HBaseColumn
	private @BelongsTo CommunityUserDO communityUser;

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
	 * アダルト商品かどうかです。
	 */
	@HBaseColumn
	@SolrField
	private boolean adult;

	/**
	 * 一時停止フラグです。
	 */
	@HBaseColumn
	@SolrField
	private boolean stopFlg=false;
	
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
	 * @return product
	 */
	public ProductDO getProduct() {
		return product;
	}

	/**
	 * @param product セットする product
	 */
	public void setProduct(ProductDO product) {
		this.product = product;
	}

	/**
	 * @return productName
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * @param productName セットする productName
	 */
	public void setProductName(String productName) {
		this.productName = productName;
	}

	/**
	 * @return purchaseLostProductId
	 */
	public String getPurchaseLostProductId() {
		return purchaseLostProductId;
	}

	/**
	 * @param purchaseLostProductId セットする purchaseLostProductId
	 */
	public void setPurchaseLostProductId(String purchaseLostProductId) {
		this.purchaseLostProductId = purchaseLostProductId;
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
	 * @return reviewProductId
	 */
	public String getReviewProductId() {
		return reviewProductId;
	}

	/**
	 * @param reviewProductId セットする reviewProductId
	 */
	public void setReviewProductId(String reviewProductId) {
		this.reviewProductId = reviewProductId;
	}

	/**
	 * @return adult
	 */
	public boolean isAdult() {
		return adult;
	}

	/**
	 * @param adult セットする adult
	 */
	public void setAdult(boolean adult) {
		this.adult = adult;
	}

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
