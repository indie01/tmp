package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;

import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.lib.solr.annotation.SolrField;

/**
 * 移行用コミュニティユーザーです。
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.TINY)
public class MigrationCommunityUserDO extends BaseWithTimestampDO {

	/**
	 *
	 */
	private static final long serialVersionUID = 138450992828428360L;

	/**
	 * 外部顧客IDです。
	 */
	@HBaseKey
	private String outerCustomerId;

	/**
	 * ニックネームです。
	 */
	@HBaseColumn
	@SolrField
	private String communityName;

	/**
	 * 標準化されたニックネームです。
	 */
	@HBaseColumn(uniqueCheckWith=CommunityNameDO.class)
	@SolrField
	private String normalizeCommunityName;

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

	/**
	 * @return communityName
	 */
	public String getCommunityName() {
		return communityName;
	}

	/**
	 * @param communityName セットする communityName
	 */
	public void setCommunityName(String communityName) {
		this.communityName = communityName;
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
}
