/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import javax.validation.Valid;

import com.kickmogu.lib.core.validator.constraints.NotEmpty;
import com.kickmogu.yodobashi.community.resource.domain.MailSettingMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSendTiming;

/**
 * メール配信設定関連の表示情報を集めたビューオブジェクトです。
 * @author kamiike
 */
public class MailSettingVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = -5187553864470022601L;

	/**
	 * メールセッティングマスタ情報です。
	 */
	@Valid
	private MailSettingMasterDO mailSettingMaster;

	/**
	 * 選択値です。
	 */
	@NotEmpty
	private MailSendTiming selectedValue;

	/**
	 * @return mailSettingMaster
	 */
	public MailSettingMasterDO getMailSettingMaster() {
		return mailSettingMaster;
	}

	/**
	 * @param mailSettingMaster セットする mailSettingMaster
	 */
	public void setMailSettingMaster(MailSettingMasterDO mailSettingMaster) {
		this.mailSettingMaster = mailSettingMaster;
	}

	/**
	 * @return selectedValue
	 */
	public MailSendTiming getSelectedValue() {
		return selectedValue;
	}

	/**
	 * @param selectedValue セットする selectedValue
	 */
	public void setSelectedValue(MailSendTiming selectedValue) {
		this.selectedValue = selectedValue;
	}

}
