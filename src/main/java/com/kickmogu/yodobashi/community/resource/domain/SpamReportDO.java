/**
 *
 */
package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;

import com.kickmogu.lib.core.resource.annotation.BelongsTo;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseIndex;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.lib.solr.annotation.SolrSchema;
import com.kickmogu.lib.solr.annotation.SolrUniqKey;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportGroupType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportTargetType;

/**
 * 違反報告クラスです。
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.SMALL)
@SolrSchema
public class SpamReportDO extends BaseWithTimestampDO {

	/**
	 *
	 */
	private static final long serialVersionUID = -2756800975550641321L;

	/**
	 * 違反報告IDです。
	 */
	@HBaseKey(createTableSplitKeys={"1","5","9","D","H","L","P","T","X","b","f","j","n","r","v"})
	@SolrField @SolrUniqKey
	private String spamReportId;

	/**
	 * 本文です。
	 */
	@HBaseColumn
	@SolrField(indexed=false)
	private String spamReportBody;

	/**
	 * 対象タイプです。
	 */
	@HBaseColumn
	@SolrField
	private SpamReportTargetType targetType;

	/**
	 * グループタイプです。
	 */
	@HBaseColumn
	@SolrField
	private SpamReportGroupType groupType;

	/**
	 * 違反報告ステータスです。
	 */
	@HBaseColumn
	@SolrField
	private SpamReportStatus status;

	/**
	 * 報告者です。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo CommunityUserDO communityUser;

	/**
	 * レビューです。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo ReviewDO review;

	/**
	 * 質問です。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo QuestionDO question;

	/**
	 * 質問回答です。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo QuestionAnswerDO questionAnswer;

	/**
	 * 画像ヘッダーです。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo ImageHeaderDO imageHeader;

	/**
	 * コメントです。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo CommentDO comment;

	/**
	 * 関連レビューオーナーIDです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex(additionalColumns={"targetType", "groupType"})
	private String relationReviewOwnerId;

	/**
	 * 関連質問オーナーIDです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex(additionalColumns={"targetType", "groupType"})
	private String relationQuestionOwnerId;

	/**
	 * 関連質問回答オーナーIDです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex(additionalColumns={"targetType", "groupType"})
	private String relationQuestionAnswerOwnerId;

	/**
	 * 関連画像オーナーIDです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex(additionalColumns={"targetType", "groupType"})
	private String relationImageOwnerId;

	/**
	 * 関連コメントオーナーIDです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex(additionalColumns={"targetType", "groupType"})
	private String relationCommentOwnerId;

	/**
	 * 報告日時です。
	 */
	@HBaseColumn
	@SolrField
	private Date reportDate;

	/**
	 * 解決日時です。
	 */
	@HBaseColumn
	@SolrField
	private Date resolvedDate;

	/**
	 * 削除日です。
	 */
	@HBaseColumn
	@SolrField
	private Date deleteDate;

	/**
	 * 退会データかどうかです。
	 */
	@HBaseColumn
	@SolrField
	private boolean withdraw;

	/**
	 * 退会キーです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex
	private String withdrawKey;

	/**
	 * 旧レビューIDです。
	 */
	@HBaseColumn
	@SolrField
	private String oldReviewId;

	/**
	 * 確認起算日です。
	 */
	@HBaseColumn
	@SolrField
	private Date checkInitialDate;

	/**
	 * @return withdraw
	 */
	public boolean isWithdraw() {
		return withdraw;
	}

	/**
	 * @param withdraw セットする withdraw
	 */
	public void setWithdraw(boolean withdraw) {
		this.withdraw = withdraw;
	}

	/**
	 * 削除済かどうか返します。
	 * @return 削除済の場合、true
	 */
	public boolean isDeleted() {
		return withdraw;
	}

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
	 * @return targetType
	 */
	public SpamReportTargetType getTargetType() {
		return targetType;
	}

	/**
	 * @param targetType セットする targetType
	 */
	public void setTargetType(SpamReportTargetType targetType) {
		this.targetType = targetType;
	}

	/**
	 * @return communityUser
	 */
	public CommunityUserDO getCommunityUser() {
		return communityUser;
	}

	/**
	 * @param communityUser セットする communityUser
	 */
	public void setCommunityUser(CommunityUserDO communityUser) {
		this.communityUser = communityUser;
	}

	/**
	 * @return review
	 */
	public ReviewDO getReview() {
		return review;
	}

	/**
	 * @param review セットする review
	 */
	public void setReview(ReviewDO review) {
		this.review = review;
	}

	/**
	 * @return question
	 */
	public QuestionDO getQuestion() {
		return question;
	}

	/**
	 * @param question セットする question
	 */
	public void setQuestion(QuestionDO question) {
		this.question = question;
	}

	/**
	 * @return questionAnswer
	 */
	public QuestionAnswerDO getQuestionAnswer() {
		return questionAnswer;
	}

	/**
	 * @param questionAnswer セットする questionAnswer
	 */
	public void setQuestionAnswer(QuestionAnswerDO questionAnswer) {
		this.questionAnswer = questionAnswer;
	}

	/**
	 * @return comment
	 */
	public CommentDO getComment() {
		return comment;
	}

	/**
	 * @param comment セットする comment
	 */
	public void setComment(CommentDO comment) {
		this.comment = comment;
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
	 * @return deleteDate
	 */
	public Date getDeleteDate() {
		return deleteDate;
	}

	/**
	 * @param deleteDate セットする deleteDate
	 */
	public void setDeleteDate(Date deleteDate) {
		this.deleteDate = deleteDate;
	}

	public String getWithdrawKey() {
		return withdrawKey;
	}

	public void setWithdrawKey(String withdrawKey) {
		this.withdrawKey = withdrawKey;
	}

	/**
	 * @return groupType
	 */
	public SpamReportGroupType getGroupType() {
		return groupType;
	}

	/**
	 * @param groupType セットする groupType
	 */
	public void setGroupType(SpamReportGroupType groupType) {
		this.groupType = groupType;
	}

	/**
	 * @return imageHeader
	 */
	public ImageHeaderDO getImageHeader() {
		return imageHeader;
	}

	/**
	 * @param imageHeader セットする imageHeader
	 */
	public void setImageHeader(ImageHeaderDO imageHeader) {
		this.imageHeader = imageHeader;
	}

	/**
	 * @return relationReviewOwnerId
	 */
	public String getRelationReviewOwnerId() {
		return relationReviewOwnerId;
	}

	/**
	 * @param relationReviewOwnerId セットする relationReviewOwnerId
	 */
	public void setRelationReviewOwnerId(String relationReviewOwnerId) {
		this.relationReviewOwnerId = relationReviewOwnerId;
	}

	/**
	 * @return relationQuestionOwnerId
	 */
	public String getRelationQuestionOwnerId() {
		return relationQuestionOwnerId;
	}

	/**
	 * @param relationQuestionOwnerId セットする relationQuestionOwnerId
	 */
	public void setRelationQuestionOwnerId(String relationQuestionOwnerId) {
		this.relationQuestionOwnerId = relationQuestionOwnerId;
	}

	/**
	 * @return relationQuestionAnswerOwnerId
	 */
	public String getRelationQuestionAnswerOwnerId() {
		return relationQuestionAnswerOwnerId;
	}

	/**
	 * @param relationQuestionAnswerOwnerId セットする relationQuestionAnswerOwnerId
	 */
	public void setRelationQuestionAnswerOwnerId(
			String relationQuestionAnswerOwnerId) {
		this.relationQuestionAnswerOwnerId = relationQuestionAnswerOwnerId;
	}

	/**
	 * @return relationImageOwnerId
	 */
	public String getRelationImageOwnerId() {
		return relationImageOwnerId;
	}

	/**
	 * @param relationImageOwnerId セットする relationImageOwnerId
	 */
	public void setRelationImageOwnerId(String relationImageOwnerId) {
		this.relationImageOwnerId = relationImageOwnerId;
	}

	/**
	 * @return relationCommentOwnerId
	 */
	public String getRelationCommentOwnerId() {
		return relationCommentOwnerId;
	}

	/**
	 * @param relationCommentOwnerId セットする relationCommentOwnerId
	 */
	public void setRelationCommentOwnerId(String relationCommentOwnerId) {
		this.relationCommentOwnerId = relationCommentOwnerId;
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
	 * @return the checkInitialDate
	 */
	public Date getCheckInitialDate() {
		return checkInitialDate;
	}

	/**
	 * @param checkInitialDate the checkInitialDate to set
	 */
	public void setCheckInitialDate(Date checkInitialDate) {
		this.checkInitialDate = checkInitialDate;
	}


}
