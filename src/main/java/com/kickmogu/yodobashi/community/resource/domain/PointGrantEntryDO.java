package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;

public class PointGrantEntryDO extends BaseWithTimestampDO {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7880204000009534116L;
	
	/**
	 * ポイント付与申請ID（ユニークID（東阪で））
	 */
	private String pointGrantRequestId;
	/**
	 * 外部顧客ID（コミュニティID）
	 */
	private String externalCustomerId;
	/**
	 * 外部顧客ID種別（0001：コミュニティ）
	 */
	private String externalCustomerIdClass;
	/**
	 * 新旧フラグ(0:旧システム　1:新システム)
	 */
	private Integer newOrPreFlag;
	/**
	 * ポイント数
	 */
	private Long pointValue;
	/**
	 * ポイント交換種別（ポイント伝票タイプ）（01:レビュー投稿ポイント　02:ランキングポイント）
	 */
	private String pointExchangeType;
	/**
	 * ポイント付与実行タイプ（0:未設定（既定値） 1:許可する 2:許可しない 3:保留 9:無効）
	 */
	private String pointGrantRequestExecMainType;
	/**
	 * ポイント付与実行サブタイプ（0:未設定（既定値） 1:許可する 2:許可しない 3:保留 9:無効）
	 */
	private String pointGrantRequestExecSubType;	
	/**
	 * ポイント付与実行開始日（この日付以降でポイント付与実行を可能とする）
	 */
	private Date pointGrantExecStartDate;
	/**
	 * ポイント付与承認日
	 */
	private Date pointGrantApprovalDate;
	/**
	 * ポイント付与日
	 */
	private Date pointGrantDate;
	/**
	 * ポイント剥奪終了日(本日付以内で剥奪可能とする。)
	 * ポイント付与日から30日経過した日を設定する。
	 */
	private Date pointDeprivationFinishDate;
	/**
	 * ポイント剥奪終了日(本日付以内で剥奪可能とする。)
	 * ポイント付与日から30日経過した日を設定する。
	 */
	private Date pointDeprivationApprovalDate;
	/**
	 * ポイント剥奪日
	 */
	private Date pointDeprivationDate;
	/**
	 * ポイント付与実行結果
	 */
	private String pointGrantResultStatus;
	
	/**
	 * ポイント剥奪実行結果
	 */
	private String pointDeprivationResultStatus;
	/**
	 * 運営者
	 */
	private String managerComment;
	
	public PointGrantEntryDO() {}
	
	public PointGrantEntryDO(
			String pointGrantRequestId,
			String externalCustomerId, 
			String externalCustomerIdClass,
			Integer newOrPreFlag,
			Long pointValue, 
			String pointExchangeType,
			String pointGrantRequestExecMainType,
			String pointGrantRequestExecSubType,
			Date pointGrantExecStartDate,
			Date pointGrantApprovalDate,
			Date pointGrantDate,
			Date pointDeprivationFinishDate,
			Date pointDeprivationApprovalDate,
			Date pointDeprivationDate,
			String pointGrantExecResultStatus,
			String pointDeprivationResultStatus,
			String managerComment) {
		this.pointGrantRequestId = pointGrantRequestId;
		this.externalCustomerId = externalCustomerId;
		this.externalCustomerIdClass = externalCustomerIdClass;
		this.newOrPreFlag = newOrPreFlag;
		this.pointValue = pointValue;
		this.pointExchangeType = pointExchangeType;
		this.pointGrantRequestExecMainType = pointGrantRequestExecMainType;
		this.pointGrantRequestExecSubType = pointGrantRequestExecSubType;
		this.pointGrantExecStartDate = pointGrantExecStartDate;
		this.pointGrantApprovalDate = pointGrantApprovalDate;
		this.pointGrantDate = pointGrantDate;
		this.pointDeprivationFinishDate = pointDeprivationFinishDate;
		this.pointDeprivationApprovalDate = pointDeprivationApprovalDate;
		this.pointDeprivationDate = pointDeprivationDate;
		this.pointGrantResultStatus = pointGrantExecResultStatus;
		this.pointDeprivationResultStatus = pointDeprivationResultStatus;
		this.managerComment = managerComment;
	}

	public String getPointGrantRequestId() {
		return pointGrantRequestId;
	}

	public void setPointGrantRequestId(String pointGrantRequestId) {
		this.pointGrantRequestId = pointGrantRequestId;
	}

	public String getExternalCustomerId() {
		return externalCustomerId;
	}

	public void setExternalCustomerId(String externalCustomerId) {
		this.externalCustomerId = externalCustomerId;
	}

	public String getExternalCustomerIdClass() {
		return externalCustomerIdClass;
	}

	public void setExternalCustomerIdClass(String externalCustomerIdClass) {
		this.externalCustomerIdClass = externalCustomerIdClass;
	}

	public Integer getNewOrPreFlag() {
		return newOrPreFlag;
	}

	public void setNewOrPreFlag(Integer newOrPreFlag) {
		this.newOrPreFlag = newOrPreFlag;
	}

	public Long getPointValue() {
		return pointValue;
	}

	public void setPointValue(Long pointValue) {
		this.pointValue = pointValue;
	}

	public String getPointExchangeType() {
		return pointExchangeType;
	}

	public void setPointExchangeType(String pointExchangeType) {
		this.pointExchangeType = pointExchangeType;
	}

	public String getPointGrantRequestExecMainType() {
		return pointGrantRequestExecMainType;
	}

	public void setPointGrantRequestExecMainType(String pointGrantExecType) {
		this.pointGrantRequestExecMainType = pointGrantExecType;
	}

	public String getPointGrantRequestExecSubType() {
		return pointGrantRequestExecSubType;
	}

	public void setPointGrantRequestExecSubType(String pointGrantExecSubType) {
		this.pointGrantRequestExecSubType = pointGrantExecSubType;
	}

	public Date getPointGrantExecStartDate() {
		return pointGrantExecStartDate;
	}

	public void setPointGrantExecStartDate(Date pointGrantExecStartDate) {
		this.pointGrantExecStartDate = pointGrantExecStartDate;
	}

	public Date getPointGrantApprovalDate() {
		return pointGrantApprovalDate;
	}

	public void setPointGrantApprovalDate(Date pointGrantApprovalDate) {
		this.pointGrantApprovalDate = pointGrantApprovalDate;
	}

	public Date getPointGrantDate() {
		return pointGrantDate;
	}

	public void setPointGrantDate(Date pointGrantDate) {
		this.pointGrantDate = pointGrantDate;
	}

	public Date getPointDeprivationFinishDate() {
		return pointDeprivationFinishDate;
	}

	public void setPointDeprivationFinishDate(Date pointDeprivationFinishDate) {
		this.pointDeprivationFinishDate = pointDeprivationFinishDate;
	}

	public Date getPointDeprivationApprovalDate() {
		return pointDeprivationApprovalDate;
	}

	public void setPointDeprivationApprovalDate(Date pointDeprivationApprovalDate) {
		this.pointDeprivationApprovalDate = pointDeprivationApprovalDate;
	}

	public Date getPointDeprivationDate() {
		return pointDeprivationDate;
	}

	public void setPointDeprivationDate(Date pointDeprivationDate) {
		this.pointDeprivationDate = pointDeprivationDate;
	}

	public String getPointGrantResultStatus() {
		return pointGrantResultStatus;
	}

	public void setPointGrantResultStatus(String pointExecResultStatus) {
		this.pointGrantResultStatus = pointExecResultStatus;
	}

	public String getPointDeprivationResultStatus() {
		return pointDeprivationResultStatus;
	}

	public void setPointDeprivationResultStatus(String pointDeprivationResultStatus) {
		this.pointDeprivationResultStatus = pointDeprivationResultStatus;
	}

	public String getManagerComment() {
		return managerComment;
	}

	public void setManagerComment(String managerComment) {
		this.managerComment = managerComment;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PointGrantEntryDO [pointGrantRequestId=");
		builder.append(pointGrantRequestId);
		builder.append(", externalCustomerId=");
		builder.append(externalCustomerId);
		builder.append(", externalCustomerIdClass=");
		builder.append(externalCustomerIdClass);
		builder.append(", newOrPreFlag=");
		builder.append(newOrPreFlag);
		builder.append(", pointValue=");
		builder.append(pointValue);
		builder.append(", pointExchangeType=");
		builder.append(pointExchangeType);
		builder.append(", pointGrantRequestExecMainType=");
		builder.append(pointGrantRequestExecMainType);
		builder.append(", pointGrantRequestExecSubType=");
		builder.append(pointGrantRequestExecSubType);
		builder.append(", pointGrantExecStartDate=");
		builder.append(pointGrantExecStartDate);
		builder.append(", pointGrantApprovalDate=");
		builder.append(pointGrantApprovalDate);
		builder.append(", pointGrantDate=");
		builder.append(pointGrantDate);
		builder.append(", pointDeprivationFinishDate=");
		builder.append(pointDeprivationFinishDate);
		builder.append(", pointDeprivationApprovalDate=");
		builder.append(pointDeprivationApprovalDate);
		builder.append(", pointDeprivationDate=");
		builder.append(pointDeprivationDate);
		builder.append(", pointGrantResultStatus=");
		builder.append(pointGrantResultStatus);
		builder.append(", pointDeprivationResultStatus=");
		builder.append(pointDeprivationResultStatus);
		builder.append(", managerComment=");
		builder.append(managerComment);
		builder.append("]");
		return builder.toString();
	}
	
}
