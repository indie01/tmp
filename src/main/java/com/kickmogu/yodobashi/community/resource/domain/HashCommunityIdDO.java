package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;

import com.kickmogu.lib.core.resource.domain.BaseDO;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;


@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.SMALL)
public class HashCommunityIdDO extends BaseDO {

	/**
	 *
	 */
	private static final long serialVersionUID = -5486530009108528921L;

	/**
	 * ハッシュ化コミュニティコードです。
	 */
	@HBaseKey(createTableSplitKeys={"1","4","7","a","d","g","j","m","p","s","v","y"})
	private String hashCommunityId;

	/**
	 * コミュニティユーザーIDです。
	 */
	@HBaseColumn
	private String communityUserId;

	/**
	 * 削除日です。
	 */
	@HBaseColumn
	private Date deleteDate;

	/**
	 * 削除フラグです。
	 */
	@HBaseColumn
	private boolean deleteFlag;

	/**
	 * @return communityUserId
	 */
	public String getCommunityUserId() {
		return communityUserId;
	}

	/**
	 * @param communityUserId セットする communityUserId
	 */
	public void setCommunityUserId(String communityUserId) {
		this.communityUserId = communityUserId;
	}

	/**
	 * @return deleteDate
	 */
	public Date getDeleteDate() {
		return deleteDate;
	}

	/**
	 * @param deleteDate セットする deleteDate
	 */
	public void setDeleteDate(Date deleteDate) {
		this.deleteDate = deleteDate;
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
	 * @return hashCommunityId
	 */
	public String getHashCommunityId() {
		return hashCommunityId;
	}

	/**
	 * @param hashCommunityId セットする hashCommunityId
	 */
	public void setHashCommunityId(String hashCommunityId) {
		this.hashCommunityId = hashCommunityId;
	}
}
