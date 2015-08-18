package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;

import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseIndex;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.yodobashi.community.resource.domain.constants.AnnounceType;


/**
 * アナウンス情報です。
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.SMALL)
public class AnnounceDO extends BaseWithTimestampDO{

	/**
	 *
	 */
	private static final long serialVersionUID = 7728640846761428554L;

	/**
	 * アナウンスIDです。
	 */
	@HBaseKey
	private String announceId;

	/**
	 * アナウンスタイプです。
	 */
	@HBaseColumn
	private AnnounceType type;

	/**
	 * コミュニティユーザーIDです。
	 */
	@HBaseColumn
	@HBaseIndex
	private String communityUserId;

	/**
	 * 削除フラグです。
	 */
	@HBaseColumn
	private boolean deleteFlag;

	/**
	 * 削除日時です。
	 */
	@HBaseColumn
	private Date deleteDate;

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
		return withdraw || deleteFlag;
	}

	/**
	 * @return announceId
	 */
	public String getAnnounceId() {
		return announceId;
	}

	/**
	 * @param announceId セットする announceId
	 */
	public void setAnnounceId(String announceId) {
		this.announceId = announceId;
	}

	/**
	 * @return type
	 */
	public AnnounceType getType() {
		return type;
	}

	/**
	 * @param type セットする type
	 */
	public void setType(AnnounceType type) {
		this.type = type;
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

	public String getWithdrawKey() {
		return withdrawKey;
	}

	public void setWithdrawKey(String withdrawKey) {
		this.withdrawKey = withdrawKey;
	}

}
