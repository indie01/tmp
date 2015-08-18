package com.kickmogu.yodobashi.community.resource.domain;

import com.kickmogu.lib.core.resource.domain.BaseDO;

public class ValidateAuthSessionDO extends BaseDO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 383196626133391195L;

	
	private boolean auth;
	
	private boolean changeSession;
	
	private String currentAutoId;
	
	private String newAuthId;
	
	private String customerCode;

	public boolean isAuth() {
		return auth;
	}

	public void setAuth(boolean auth) {
		this.auth = auth;
	}

	public boolean isChangeSession() {
		return changeSession;
	}

	public void setChangeSession(boolean changeSession) {
		this.changeSession = changeSession;
	}

	public String getCurrentAutoId() {
		return currentAutoId;
	}

	public void setCurrentAutoId(String oldAutoId) {
		this.currentAutoId = oldAutoId;
	}

	public String getNewAuthId() {
		return newAuthId;
	}

	public void setNewAuthId(String newAuthId) {
		this.newAuthId = newAuthId;
	}

	public String getCustomerCode() {
		return customerCode;
	}

	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}
	
	
}
