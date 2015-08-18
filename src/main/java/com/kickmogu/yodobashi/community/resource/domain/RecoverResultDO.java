package com.kickmogu.yodobashi.community.resource.domain;

import com.kickmogu.lib.core.resource.domain.BaseDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.RecoverResultStatus;

public class RecoverResultDO extends BaseDO{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5575607408056802583L;

	private RecoverResultStatus resultStatus;
	
	private String resultMessage;
	
	private String reviewId;
	
	private Long reviewPoint;

	public RecoverResultDO(
			RecoverResultStatus resultStatus,
			String resultMessage, 
			String reviewId) {
		this(resultStatus, resultMessage, reviewId, 0L);
	}
	public RecoverResultDO(
			RecoverResultStatus resultStatus,
			String resultMessage, 
			String reviewId, 
			Long reviewPoint) {
		this.resultStatus = resultStatus;
		this.resultMessage = resultMessage;
		this.reviewId = reviewId;
		this.reviewPoint = reviewPoint;
	}

	public RecoverResultStatus getResultStatus() {
		return resultStatus;
	}

	public void setResultStatus(RecoverResultStatus resultStatus) {
		this.resultStatus = resultStatus;
	}

	public String getResultMessage() {
		return resultMessage;
	}

	public void setResultMessage(String resultMessage) {
		this.resultMessage = resultMessage;
	}

	public String getReviewId() {
		return reviewId;
	}

	public void setReviewId(String reviewId) {
		this.reviewId = reviewId;
	}

	public Long getReviewPoint() {
		return reviewPoint;
	}

	public void setReviewPoint(Long reviewPoint) {
		this.reviewPoint = reviewPoint;
	}
	
}
