package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Collection;

import com.kickmogu.lib.core.resource.domain.BaseDO;

public class ReviewPointSpecialConditionValidateResponseDO extends BaseDO {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9184860597680639934L;

	private Collection<ReviewPointSpecialConditionValidateDO> specialConditionValidates;

	public ReviewPointSpecialConditionValidateResponseDO() {
	}

	public ReviewPointSpecialConditionValidateResponseDO(
			Collection<ReviewPointSpecialConditionValidateDO> specialConditionValidates) {
		this.specialConditionValidates = specialConditionValidates;
	}

	public Collection<ReviewPointSpecialConditionValidateDO> getSpecialConditionValidates() {
		return specialConditionValidates;
	}

	public void setSpecialConditionValidates(
			Collection<ReviewPointSpecialConditionValidateDO> specialConditionValidates) {
		this.specialConditionValidates = specialConditionValidates;
	}
	
}
