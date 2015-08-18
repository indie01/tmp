/**
 *
 */
package com.kickmogu.yodobashi.community.resource.domain;

import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseIndex;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSendTiming;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSettingType;



/**
 * メール配信設定情報です。
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.MEDIUM)
public class MailSettingDO extends BaseWithTimestampDO {

	/**
	 *
	 */
	private static final long serialVersionUID = -7169131137273681354L;

	/**
	 * メール配信設定IDです。
	 */
	@HBaseKey(createTableSplitKeys={"1","5","9","D","H","L","P","T","X","b","f","j","n","r","v"})
	private String mailSettingId;

	/**
	 * メール配信タイプです。
	 */
	@HBaseColumn
	private MailSettingType mailSettingType;

	/**
	 * 設定値です。
	 */
	@HBaseColumn
	private MailSendTiming mailSettingValue;

	/**
	 * コミュニティユーザーIDです。
	 */
	@HBaseColumn
	@HBaseIndex
	private String communityUserId;

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
	 * @return mailSettingId
	 */
	public String getMailSettingId() {
		return mailSettingId;
	}

	/**
	 * @param mailSettingId セットする mailSettingId
	 */
	public void setMailSettingId(String mailSettingId) {
		this.mailSettingId = mailSettingId;
	}

	/**
	 * @return mailSettingValue
	 */
	public MailSendTiming getMailSettingValue() {
		return mailSettingValue;
	}

	/**
	 * @param mailSettingValue セットする mailSettingValue
	 */
	public void setMailSettingValue(MailSendTiming mailSettingValue) {
		this.mailSettingValue = mailSettingValue;
	}

	/**
	 * @return mailSettingType
	 */
	public MailSettingType getMailSettingType() {
		return mailSettingType;
	}

	/**
	 * @param mailSettingType セットする mailSettingType
	 */
	public void setMailSettingType(MailSettingType mailSettingType) {
		this.mailSettingType = mailSettingType;
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
