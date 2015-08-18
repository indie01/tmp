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
public class CommunityNameDO extends BaseDO {

	/**
	 *
	 */
	private static final long serialVersionUID = -5486530009108528921L;

	/**
	 * 標準化されたニックネームです。
	 */
	@HBaseKey
	private String normalizeCommunityName;

	/**
	 * コミュニティユーザーIDです。
	 */
	@HBaseColumn
	private String communityUserId;

	/**
	 * 外部顧客IDです。
	 */
	@HBaseColumn
	private String outerCustomerId;

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
	 * @return normalizeCommunityName
	 */
	public String getNormalizeCommunityName() {
		return normalizeCommunityName;
	}

	/**
	 * @param normalizeCommunityName セットする normalizeCommunityName
	 */
	public void setNormalizeCommunityName(String normalizeCommunityName) {
		this.normalizeCommunityName = normalizeCommunityName;
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
	 * @return outerCustomerId
	 */
	public String getOuterCustomerId() {
		return outerCustomerId;
	}

	/**
	 * @param outerCustomerId セットする outerCustomerId
	 */
	public void setOuterCustomerId(String outerCustomerId) {
		this.outerCustomerId = outerCustomerId;
	}
}
