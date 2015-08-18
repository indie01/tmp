package com.kickmogu.yodobashi.community.resource.domain;

import com.kickmogu.lib.core.resource.domain.BaseDO;

public class PointGrantEntryExecuteStatusDO extends BaseDO{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8044922675220138951L;
	
	/**
	 * ポイント付与申請ID（ユニークID（東阪で））
	 */
	private String pointGrantRequestId;
	
	/**
	 * ポイント付与申請実行ステータス 一覧
	 */
	private String pointGrantRequestExecMainType;

	public PointGrantEntryExecuteStatusDO() {
	}

	public PointGrantEntryExecuteStatusDO(
			String pointGrantRequestId,
			String pointGrantRequestExecMainType) {
		this.pointGrantRequestId = pointGrantRequestId;
		this.pointGrantRequestExecMainType = pointGrantRequestExecMainType;
	}

	public String getPointGrantRequestId() {
		return pointGrantRequestId;
	}

	public void setPointGrantRequestId(String pointGrantRequestId) {
		this.pointGrantRequestId = pointGrantRequestId;
	}

	public String getPointGrantRequestExecMainType() {
		return pointGrantRequestExecMainType;
	}

	public void setPointGrantRequestExecMainType(
			String pointGrantRequestExecMainType) {
		this.pointGrantRequestExecMainType = pointGrantRequestExecMainType;
	}

	
}
