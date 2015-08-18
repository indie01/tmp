package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;

import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;


@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.MEDIUM)
public class LoginDO extends BaseWithTimestampDO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4636412040418051948L;

	/**
	 * いいねIDです。
	 */
	@HBaseKey(createTableSplitKeys={"1","5","9","D","H","L","P","T","X","b","f","j","n","r","v"})
	private String loginId;

	@HBaseColumn
	private String communityId;

	@HBaseColumn
	private String communityUserId;
	
	@HBaseColumn
	private Date lastAccessDate;

	/**
	 * @return the loginId
	 */
	public String getLoginId() {
		return loginId;
	}

	/**
	 * @param loginId the loginId to set
	 */
	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	/**
	 * @return the communityId
	 */
	public String getCommunityId() {
		return communityId;
	}

	/**
	 * @param communityId the communityId to set
	 */
	public void setCommunityId(String communityId) {
		this.communityId = communityId;
	}

	/**
	 * @return the communityUserId
	 */
	public String getCommunityUserId() {
		return communityUserId;
	}

	/**
	 * @param communityUserId the communityUserId to set
	 */
	public void setCommunityUserId(String communityUserId) {
		this.communityUserId = communityUserId;
	}

	/**
	 * @return the lastAccessDate
	 */
	public Date getLastAccessDate() {
		return lastAccessDate;
	}

	/**
	 * @param lastAccessDate the lastAccessDate to set
	 */
	public void setLastAccessDate(Date lastAccessDate) {
		this.lastAccessDate = lastAccessDate;
	}


}
