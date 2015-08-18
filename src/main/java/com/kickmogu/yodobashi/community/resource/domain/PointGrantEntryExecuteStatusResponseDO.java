package com.kickmogu.yodobashi.community.resource.domain;

import com.kickmogu.lib.core.resource.domain.BaseDO;

public class PointGrantEntryExecuteStatusResponseDO extends BaseDO{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7521982504673812827L;
	/**
	 * ポイント付与申請ID（ユニークID（東阪で））一覧
	 */
	private String pointGrantRequestId;
	
	/**
	 * ポイント付与申請実行ステータス 一覧
	 */
	private String pointGrantRequestExecMainType;
	
	/**
	 * 更新結果一覧
	 */
	private Integer modifyResult;

	public PointGrantEntryExecuteStatusResponseDO() {
	}

	public PointGrantEntryExecuteStatusResponseDO(
			String pointGrantRequestId,
			String pointGrantRequestExecMainType, 
			Integer modifyResult) {
		this.pointGrantRequestId = pointGrantRequestId;
		this.pointGrantRequestExecMainType = pointGrantRequestExecMainType;
		this.modifyResult = modifyResult;
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

	public void setPointGrantRequestExecMainTypes(
			String pointGrantRequestExecMainType) {
		this.pointGrantRequestExecMainType = pointGrantRequestExecMainType;
	}

	public Integer getModifyResult() {
		return modifyResult;
	}

	public void setModifyResult(Integer modifyResult) {
		this.modifyResult = modifyResult;
	}
	
}
