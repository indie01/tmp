package com.kickmogu.yodobashi.community.resource.domain;

import java.io.Serializable;
import java.util.Date;

import org.msgpack.annotation.Message;

import com.kickmogu.lib.core.resource.annotation.Label;
import com.kickmogu.yodobashi.community.resource.domain.constants.SlipDetailCategory;

@Message
public class SlipBillingDateDO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3050990391065472689L;
	

	@Label("外部顧客ID")
	protected String outerCustomerId;

	@Label("受注伝票番号")
	protected String slipNo;

	@Label("明細番号")
	protected Integer slipDetailNo;

	@Label("受注明細カテゴリ")
	protected SlipDetailCategory slipDetailCategory;

	@Label("順序")
	protected Integer orderNo;
	
	@Label("請求日")
	protected Date billingDate;
	
	@Label("請求数量")
	protected long billingNum;

	public String getOuterCustomerId() {
		return outerCustomerId;
	}

	public void setOuterCustomerId(String outerCustomerId) {
		this.outerCustomerId = outerCustomerId;
	}

	public String getSlipNo() {
		return slipNo;
	}

	public void setSlipNo(String slipNo) {
		this.slipNo = slipNo;
	}

	public Integer getSlipDetailNo() {
		return slipDetailNo;
	}

	public void setSlipDetailNo(Integer slipDetailNo) {
		this.slipDetailNo = slipDetailNo;
	}

	public SlipDetailCategory getSlipDetailCategory() {
		return slipDetailCategory;
	}

	public void setSlipDetailCategory(SlipDetailCategory slipDetailCategory) {
		this.slipDetailCategory = slipDetailCategory;
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	public Date getBillingDate() {
		return billingDate;
	}

	public void setBillingDate(Date billingDate) {
		this.billingDate = billingDate;
	}

	public long getBillingNum() {
		return billingNum;
	}

	public void setBillingNum(long billingNum) {
		this.billingNum = billingNum;
	}


}
