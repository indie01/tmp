package com.kickmogu.yodobashi.community.resource.domain;

import com.kickmogu.lib.core.resource.domain.BaseDO;

public class ReviewPointSpecialConditionValidateDO extends BaseDO {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5551096324931216075L;

	private String specialConditionCode;
	
	private String validateStatus;

	public ReviewPointSpecialConditionValidateDO() {
	}

	public ReviewPointSpecialConditionValidateDO(
			String specialConditionCode,
			String validateStatus) {
		this.specialConditionCode = specialConditionCode;
		this.validateStatus = validateStatus;
	}

	public String getSpecialConditionCode() {
		return specialConditionCode;
	}

	public void setSpecialConditionCode(String specialConditionCode) {
		this.specialConditionCode = specialConditionCode;
	}

	public String getValidateStatus() {
		return validateStatus;
	}

	public void setValidateStatus(String validateStatus) {
		this.validateStatus = validateStatus;
	}
	
}
