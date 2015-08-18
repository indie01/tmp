package com.kickmogu.yodobashi.community.resource.domain;

import com.kickmogu.lib.core.resource.domain.BaseDO;


public class ResponseDetail extends BaseDO {

	/**
	 * 
	 */
	private static final long serialVersionUID = -719536222434875864L;

    private ResponseComDetail[] returnComDetail;

	public ResponseComDetail[] getReturnComDetail() {
		return returnComDetail;
	}

	public void setReturnComDetail(ResponseComDetail[] returnComDetail) {
		this.returnComDetail = returnComDetail;
	}
}
