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

@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.MEDIUM)
@SolrSchema
public class ProductFollowDO extends BaseWithTimestampDO implements SolrVisible, StoppableContents {

	/**
	 *
	 */
	private static final long serialVersionUID = -525969347557111549L;

	/**
	 * フォローIDです。
	 */
	@HBaseKey
	@SolrField @SolrUniqKey
	private String productFollowId;

	/**
	 * コミュニティユーザです。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo CommunityUserDO communityUser;

	/**
	 * フォローする商品です。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo ProductDO followProduct;

	/**
	 * フォローした日です。
	 */
	@HBaseColumn
	@SolrField
	private Date followDate;

	/**
	 * アダルト商品に対するものかどうかです。
	 */
	@HBaseColumn
	@SolrField
	private boolean adult;

	/**
	 * オーナー停止フラグです。
	 */
	@SolrField
	@HBaseColumn
	private boolean ownerStop;

	/**
	 * 退会データかどうかです。
	 */
	@HBaseColumn
	private boolean withdraw;

	/**
	 * 退会キーです。
	 */
	@HBaseColumn
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
	 * @return followProduct
	 */
	public ProductDO getFollowProduct() {
		return followProduct;
	}

	/**
	 * @param followProduct セットする followProduct
	 */
	public void setFollowProduct(ProductDO followProduct) {
		this.followProduct = followProduct;
	}

	/**
	 * @return followDate
	 */
	public Date getFollowDate() {
		return followDate;
	}

	/**
	 * @param followDate セットする followDate
	 */
	public void setFollowDate(Date followDate) {
		this.followDate = followDate;
	}

	/**
	 * @return productFollowId
	 */
	public String getProductFollowId() {
		return productFollowId;
	}

	/**
	 * @param productFollowId セットする productFollowId
	 */
	public void setProductFollowId(String productFollowId) {
		this.productFollowId = productFollowId;
	}

	public String getWithdrawKey() {
		return withdrawKey;
	}

	public void setWithdrawKey(String withdrawKey) {
		this.withdrawKey = withdrawKey;
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

	@Override
	public boolean visible() {
		return !isDeleted();
	}

	@Override
	public String[] getHintPropertyNames() {
		return new String[]{"withdraw"};
	}

	/**
	 * @return ownerStop
	 */
	public boolean isOwnerStop() {
		return ownerStop;
	}

	/**
	 * @param ownerStop セットする ownerStop
	 */
	public void setOwnerStop(boolean ownerStop) {
		this.ownerStop = ownerStop;
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

}
