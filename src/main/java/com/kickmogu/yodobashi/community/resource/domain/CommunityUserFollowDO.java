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
public class CommunityUserFollowDO extends BaseWithTimestampDO implements SolrVisible, StoppableContents {

	/**
	 *
	 */
	private static final long serialVersionUID = 850649824326492075L;

	/**
	 * フォローIDです。
	 */
	@HBaseKey(createTableSplitKeys={"1","5","9","D","H","L","P","T","X","b","f","j","n","r","v"})
	@SolrField @SolrUniqKey
	private String communityUserFollowId;

	/**
	 * コミュニティユーザです。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo("followCommunityUsers") CommunityUserDO communityUser;

	/**
	 * フォローするコミュニティユーザです。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo("followerCommunityUsers") CommunityUserDO followCommunityUser;

	/**
	 * フォローした日です。
	 */
	@HBaseColumn
	@SolrField
	private Date followDate;

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
	 * @return followCommunityUser
	 */
	public CommunityUserDO getFollowCommunityUser() {
		return followCommunityUser;
	}

	/**
	 * @param followCommunityUser セットする followCommunityUser
	 */
	public void setFollowCommunityUser(CommunityUserDO followCommunityUser) {
		this.followCommunityUser = followCommunityUser;
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
	 * @return communityUserFollowId
	 */
	public String getCommunityUserFollowId() {
		return communityUserFollowId;
	}

	/**
	 * @param communityUserFollowId セットする communityUserFollowId
	 */
	public void setCommunityUserFollowId(String communityUserFollowId) {
		this.communityUserFollowId = communityUserFollowId;
	}

	public String getWithdrawKey() {
		return withdrawKey;
	}

	public void setWithdrawKey(String withdrawKey) {
		this.withdrawKey = withdrawKey;
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
