package com.kickmogu.yodobashi.community.resource.domain;

import com.kickmogu.lib.core.resource.domain.BaseDO;


public class ResponseHeader extends BaseDO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4394070494443435804L;
	
	private Boolean statusFlg;
	private String statusCode;
	private String messageText;
	private String param_1;
	private String param_2;
	
	public Boolean getStatusFlg() {
		return statusFlg;
	}
	public void setStatusFlg(Boolean statusFlg) {
		this.statusFlg = statusFlg;
	}
	public String getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
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

}
