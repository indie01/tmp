/**
 *
 */
package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;

import com.kickmogu.lib.core.id.annotation.IDParts;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseIndex;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportStatus;

/**
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	}
,sizeGroup=SizeGroup.TINY,excludeBackup=true)
public class OldSpamReportDO extends BaseWithTimestampDO {

	/**
	 *
	 */
	private static final long serialVersionUID = -1718818529363388666L;

	/**
	 * 違反報告IDです。
	 */
	@HBaseKey (idGenerator="idPartsGenerator",createTableSplitKeys={"#", "5", "A", "G", "M", "S", "Y", "e", "k", "q", "w"})
	private String spamReportId;

	/**
	 * 旧レビューIDです。
	 */
	@HBaseColumn @IDParts(order=1)
	@HBaseIndex
	private String oldReviewId;

	/**
	 * 連番です。
	 */
	@HBaseColumn @IDParts(order=2)
	private int childNo;

	/**
	 * 本文です。
	 */
	@HBaseColumn
	private String spamReportBody;

	/**
	 * 報告者の外部顧客IDです。
	 */
	@HBaseColumn
	@HBaseIndex
	private String outerCustomerId;

	/**
	 * 違反報告ステータスです。
	 */
	@HBaseColumn
	private SpamReportStatus status;

	/**
	 * 報告日時です。
	 */
	@HBaseColumn
	private Date reportDate;

	/**
	 * 解決日時です。
	 */
	@HBaseColumn
	private Date resolvedDate;

	/**
	 * 移行済みかどうかです。
	 */
	@HBaseColumn
	private boolean moved;

	/**
	 * @return spamReportId
	 */
	public String getSpamReportId() {
		return spamReportId;
	}

	/**
	 * @param spamReportId セットする spamReportId
	 */
	public void setSpamReportId(String spamReportId) {
		this.spamReportId = spamReportId;
	}

	/**
	 * @return spamReportBody
	 */
	public String getSpamReportBody() {
		return spamReportBody;
	}

	/**
	 * @param spamReportBody セットする spamReportBody
	 */
	public void setSpamReportBody(String spamReportBody) {
		this.spamReportBody = spamReportBody;
	}

	/**
	 * @return oldReviewId
	 */
	public String getOldReviewId() {
		return oldReviewId;
	}

	/**
	 * @param oldReviewId セットする oldReviewId
	 */
	public void setOldReviewId(String oldReviewId) {
		this.oldReviewId = oldReviewId;
	}

	/**
	 * @return outerCustomerId
	 */
	public String getOuterCustomerId() {
		return outerCustomerId;
	}

	/**
	 * @param outerCustomerId セットする outerCustomerId
	 */
	public void setOuterCustomerId(String outerCustomerId) {
		this.outerCustomerId = outerCustomerId;
	}

	/**
	 * @return status
	 */
	public SpamReportStatus getStatus() {
		return status;
	}

	/**
	 * @param status セットする status
	 */
	public void setStatus(SpamReportStatus status) {
		this.status = status;
	}

	/**
	 * @return reportDate
	 */
	public Date getReportDate() {
		return reportDate;
	}

	/**
	 * @param reportDate セットする reportDate
	 */
	public void setReportDate(Date reportDate) {
		this.reportDate = reportDate;
	}

	/**
	 * @return resolvedDate
	 */
	public Date getResolvedDate() {
		return resolvedDate;
	}

	/**
	 * @param resolvedDate セットする resolvedDate
	 */
	public void setResolvedDate(Date resolvedDate) {
		this.resolvedDate = resolvedDate;
	}

	/**
	 * @return childNo
	 */
	public int getChildNo() {
		return childNo;
	}

	/**
	 * @param childNo セットする childNo
	 */
	public void setChildNo(int childNo) {
		this.childNo = childNo;
	}

	/**
	 * @return moved
	 */
	public boolean isMoved() {
		return moved;
	}

	/**
	 * @param moved セットする moved
	 */
	public void setMoved(boolean moved) {
		this.moved = moved;
	}
}
