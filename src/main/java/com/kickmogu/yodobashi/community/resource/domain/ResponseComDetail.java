package com.kickmogu.yodobashi.community.resource.domain;

import com.kickmogu.lib.core.resource.domain.BaseDO;


public class ResponseComDetail extends BaseDO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4394070494443435804L;
	
	private String detailStatus;
	private String messageText;
	private String param_1;
	private String param_2;
	private String param_3;
	private String param_4;
	private String param_5;
	
	public String getDetailStatus() {
		return detailStatus;
	}
	public void setDetailStatus(String detailStatus) {
		this.detailStatus = detailStatus;
	}
	public String getMessageText() {
		return messageText;
	}
	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}
	public String getParam_1() {
		return param_1;
	}
	public void setParam_1(String param_1) {
		this.param_1 = param_1;
	}
	public String getParam_2() {
		return param_2;
	}
	public void setParam_2(String param_2) {
		this.param_2 = param_2;
	}
	public String getParam_3() {
		return param_3;
	}
	public void setParam_3(String param_3) {
		this.param_3 = param_3;
	}
	public String getParam_4() {
		return param_4;
	}
	public void setParam_4(String param_4) {
		this.param_4 = param_4;
	}
	public String getParam_5() {
		return param_5;
	}
	public void setParam_5(String param_5) {
		this.param_5 = param_5;
	}
	
}
