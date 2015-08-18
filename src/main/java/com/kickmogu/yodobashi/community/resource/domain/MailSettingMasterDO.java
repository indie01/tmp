/**
 *
 */
package com.kickmogu.yodobashi.community.resource.domain;

import java.util.ArrayList;
import java.util.List;

import com.kickmogu.lib.core.validator.constraints.NotEmpty;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.web.annotation.Hidden;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSendTiming;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSettingType;



/**
 * メール配信設定マスタ情報です。
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.TINY)
public class MailSettingMasterDO extends BaseWithTimestampDO {

	/**
	 *
	 */
	private static final long serialVersionUID = 8699481477152646355L;

	/**
	 * メール配信設定タイプです。
	 */
	@NotEmpty
	@HBaseKey
	@Hidden
	private MailSettingType mailSettingType;

	/**
	 * 表示名です。
	 */
	@HBaseColumn
	private String mailSettingMasterLabel;

	/**
	 * 説明です。
	 */
	@HBaseColumn
	private String mailSettingMasterDescription;

	/**
	 * 選択肢リストです。
	 */
	@HBaseColumn
	private List<MailSendTiming> choices;

	/**
	 * デフォルト値です。
	 */
	@HBaseColumn
	private MailSendTiming defaultValue;

	/**
	 * 表示順です。
	 */
	@HBaseColumn
	private int orderNo;

	/**
	 * @return choices
	 */
	public List<MailSendTiming> getChoices() {
		return choices;
	}

	/**
	 * @return choices
	 */
	public List<MailSendTiming> getChoicesExcludeBatch() {
		List<MailSendTiming> timings = new ArrayList<MailSendTiming>();
		for(MailSendTiming timing: choices) {
			if(!timing.equals(MailSendTiming.TEN_DAYS_AGO) &&
					!timing.equals(MailSendTiming.FIVE_DAYS_AGO) &&
					!timing.equals(MailSendTiming.DAILY_NOTIFY)) {
				timings.add(timing);
			}
		}
		return timings;
	}
	
	/**
	 * @param choices セットする choices
	 */
	public void setChoices(List<MailSendTiming> choices) {
		this.choices = choices;
	}


	/**
	 * @return orderNo
	 */
	public int getOrderNo() {
		return orderNo;
	}


	/**
	 * @param orderNo セットする orderNo
	 */
	public void setOrderNo(int orderNo) {
		this.orderNo = orderNo;
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
	 * @return mailSettingMasterLabel
	 */
	public String getMailSettingMasterLabel() {
		return mailSettingMasterLabel;
	}


	/**
	 * @param mailSettingMasterLabel セットする mailSettingMasterLabel
	 */
	public void setMailSettingMasterLabel(String mailSettingMasterLabel) {
		this.mailSettingMasterLabel = mailSettingMasterLabel;
	}


	/**
	 * @return mailSettingMasterDescription
	 */
	public String getMailSettingMasterDescription() {
		return mailSettingMasterDescription;
	}


	/**
	 * @param mailSettingMasterDescription セットする mailSettingMasterDescription
	 */
	public void setMailSettingMasterDescription(String mailSettingMasterDescription) {
		this.mailSettingMasterDescription = mailSettingMasterDescription;
	}


	/**
	 * @return defaultValue
	 */
	public MailSendTiming getDefaultValue() {
		return defaultValue;
	}


	/**
	 * @param defaultValue セットする defaultValue
	 */
	public void setDefaultValue(MailSendTiming defaultValue) {
		this.defaultValue = defaultValue;
	}


}
