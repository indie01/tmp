package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.resource.annotation.BelongsTo;
import com.kickmogu.lib.core.resource.annotation.HasMany;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseIndex;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.lib.hadoop.hbase.annotation.RelatedByHBase;
import com.kickmogu.lib.solr.annotation.RelatedBySolr;
import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.lib.solr.annotation.SolrSchema;
import com.kickmogu.lib.solr.annotation.SolrUniqKey;
import com.kickmogu.yodobashi.community.resource.dao.util.UserUtil;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.EditorVersions;
import com.kickmogu.yodobashi.community.resource.domain.util.CommunityUserUtil;


@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
},sizeGroup=SizeGroup.MEDIUM)
@SolrSchema
public class QuestionDO extends AbstractContentBaseDO implements TextEditableContents, StoppableContents {

	/**
	 *
	 */
	private static final long serialVersionUID = -4619848482511244229L;

	/**
	 * 質問IDです。
	 */
	@HBaseKey(createTableSplitKeys={"1","5","9","D","H","L","P","T","X","b","f","j","n","r","v"})
	@SolrField @SolrUniqKey
	private String questionId;

	/**
	 * 商品です。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo ProductDO product;

	/**
	 * コミュニティユーザです。
	 */
	@HBaseColumn(indexTableAdditionalColumns="productId")
	@SolrField
	private @BelongsTo CommunityUserDO communityUser;

	/**
	 * 質問本文です。
	 */
	@HBaseColumn
	@SolrField(indexed=false)
	private String questionBody;

	/**
	 * ステータスです。
	 */
	@HBaseColumn
	@SolrField
	private ContentsStatus status;

	/**
	 * スコアです。
	 */
	@SolrField
	@HBaseColumn
	private double questionScore;

	/**
	 * 閲覧数です。
	 */
	@HBaseColumn
	@SolrField
	private long viewCount;

	/**
	 * アダルト商品に対する質問かどうかです。
	 */
	@HBaseColumn
	@SolrField
	private boolean adult;

	/**
	 * 保存日時
	 */
	@HBaseColumn
	@SolrField
	private Date saveDate;

	/**
	 * 投稿日です。
	 */
	@HBaseColumn
	@SolrField
	private Date postDate;

	/**
	 * 削除日です。
	 */
	@HBaseColumn
	@SolrField
	private Date deleteDate;

	/**
	 * 最終回答日です。
	 */
	@HBaseColumn
	@SolrField
	private Date lastAnswerDate;

	/**
	 * 質問回答のリストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<QuestionAnswerDO> questionAnswers = Lists.newArrayList();

	/**
	 * お知らせのリストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<InformationDO> informations = Lists.newArrayList();

	/**
	 * 質問フォローのリストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<QuestionFollowDO> questionFollows = Lists.newArrayList();

	/**
	 * アクション履歴のリストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<ActionHistoryDO> actionHistorys = Lists.newArrayList();

	/**
	 * 画像のリストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<ImageHeaderDO> imageHeaders = Lists.newArrayList();

	/**
	 * 違反報告リストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<SpamReportDO> spamReports = Lists.newArrayList();

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
	 * 管理ツール操作フラグ
	 */
	@SolrField
	@HBaseColumn
	private boolean mngToolOperation=false;
	
	/** 画像ID一覧 */
	@Deprecated
	@HBaseColumn
	private List<String> uploadImageIds;
	
	/** 一時保存用画像一覧 */
	@HBaseColumn
	private List<SaveImageDO> saveImages;
	
	/**
	 * エディターバージョン
	 */
	@SolrField
	@HBaseColumn
	private EditorVersions editorVersion = EditorVersions.WYSIWYG_EDITOR;
	
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
		return withdraw || ContentsStatus.DELETE.equals(status);
	}

	/**
	 * @return questionId
	 */
	public String getQuestionId() {
		return questionId;
	}

	/**
	 * @param questionId セットする questionId
	 */
	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}

	/**
	 * @return product
	 */
	@Override
	public ProductDO getProduct() {
		return product;
	}

	/**
	 * @param product セットする product
	 */
	public void setProduct(ProductDO product) {
		this.product = product;
	}

	/**
	 * @return communityUser
	 */
	@Override
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
	 * @return questionBody
	 */
	public String getQuestionBody() {
		return questionBody;
	}

	/**
	 * @param questionBody セットする questionBody
	 */
	public void setQuestionBody(String questionBody) {
		this.questionBody = questionBody;
	}

	/**
	 * @return status
	 */
	public ContentsStatus getStatus() {
		return status;
	}

	/**
	 * @param status セットする status
	 */
	public void setStatus(ContentsStatus status) {
		this.status = status;
	}

	/**
	 * @return saveDate
	 */
	public Date getSaveDate() {
		return saveDate;
	}

	/**
	 * @param saveDate セットする saveDate
	 */
	public void setSaveDate(Date saveDate) {
		this.saveDate = saveDate;
	}

	/**
	 * @return postDate
	 */
	public Date getPostDate() {
		return postDate;
	}

	/**
	 * @param postDate セットする postDate
	 */
	public void setPostDate(Date postDate) {
		this.postDate = postDate;
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

	/**
	 * @return questionAnswers
	 */
	public List<QuestionAnswerDO> getQuestionAnswers() {
		return questionAnswers;
	}

	/**
	 * @param questionAnswers セットする questionAnswers
	 */
	public void setQuestionAnswers(List<QuestionAnswerDO> questionAnswers) {
		this.questionAnswers = questionAnswers;
	}

	/**
	 * @return informations
	 */
	public List<InformationDO> getInformations() {
		return informations;
	}

	/**
	 * @param informations セットする informations
	 */
	public void setInformations(List<InformationDO> informations) {
		this.informations = informations;
	}

	/**
	 * @return questionFollows
	 */
	public List<QuestionFollowDO> getQuestionFollows() {
		return questionFollows;
	}

	/**
	 * @param questionFollows セットする questionFollows
	 */
	public void setQuestionFollows(List<QuestionFollowDO> questionFollows) {
		this.questionFollows = questionFollows;
	}

	/**
	 * @return actionHistorys
	 */
	public List<ActionHistoryDO> getActionHistorys() {
		return actionHistorys;
	}

	/**
	 * @param actionHistorys セットする actionHistorys
	 */
	public void setActionHistorys(List<ActionHistoryDO> actionHistorys) {
		this.actionHistorys = actionHistorys;
	}

	/**
	 * @return imageHeaders
	 */
	public List<ImageHeaderDO> getImageHeaders() {
		return imageHeaders;
	}

	/**
	 * @param imageHeaders セットする imageHeaders
	 */
	public void setImageHeaders(List<ImageHeaderDO> imageHeaders) {
		this.imageHeaders = imageHeaders;
	}

	@Override
	public String getTextEditableText() {
		return getQuestionBody();
	}

	@Override
	public void setTextEditableText(String text) {
		setQuestionBody(text);
	}

	/**
	 * @return questionScore
	 */
	public double getQuestionScore() {
		return questionScore;
	}

	/**
	 * @param questionScore セットする questionScore
	 */
	public void setQuestionScore(double questionScore) {
		this.questionScore = questionScore;
	}

	/**
	 * @return lastAnswerDate
	 */
	public Date getLastAnswerDate() {
		return lastAnswerDate;
	}

	/**
	 * @param lastAnswerDate セットする lastAnswerDate
	 */
	public void setLastAnswerDate(Date lastAnswerDate) {
		this.lastAnswerDate = lastAnswerDate;
	}

	/**
	 * @return adult
	 */
	public boolean isAdult() {
		return adult;
	}

	/**
	 * @param adult セットする adult
	 */
	public void setAdult(boolean adult) {
		this.adult = adult;
	}

	/**
	 * @return spamReports
	 */
	public List<SpamReportDO> getSpamReports() {
		return spamReports;
	}

	/**
	 * @param spamReports セットする spamReports
	 */
	public void setSpamReports(List<SpamReportDO> spamReports) {
		this.spamReports = spamReports;
	}

	/**
	 * @return viewCount
	 */
	public long getViewCount() {
		return viewCount;
	}

	/**
	 * @param viewCount セットする viewCount
	 */
	public void setViewCount(long viewCount) {
		this.viewCount = viewCount;
	}

	public String getWithdrawKey() {
		return withdrawKey;
	}

	public void setWithdrawKey(String withdrawKey) {
		this.withdrawKey = withdrawKey;
	}

	/**
	 * 関連オーナーのリストを返します。
	 * @return 関連オーナーのリスト
	 */
	@Override
	public List<CommunityUserDO> getRelationOwners() {
		return CommunityUserUtil.getRelationOwners(this);
	}

	/**
	 * 関連オーナーIDのリストを返します。
	 * @return 関連オーナーIDのリスト
	 */
	@Override
	public List<String> getRelationOwnerIds() {
		return CommunityUserUtil.getRelationOwnerIds(this);
	}

	/**
	 * 一時停止中かどうかを返します。
	 * @param communityUserDao コミュニティユーザー
	 * @param stopCommunityUserIds 一時停止中のコミュニティユーザーIDのリスト
	 * @return 一時停止中の場合、true
	 */
	@Override
	public boolean isStop(String communityUserId,
			Set<String> stopCommunityUserIds) {
		
		// コンテンツの一時停止対応
		if(!this.communityUser.getCommunityUserId().equals(communityUserId) && this.status.equals(ContentsStatus.CONTENTS_STOP)) {
			return true;
		}
		return UserUtil.isStop(this, communityUserId, stopCommunityUserIds);
	}

	/**
	 * @return the mngToolOperation
	 */
	public boolean isMngToolOperation() {
		return mngToolOperation;
	}

	/**
	 * @param mngToolOperation the mngToolOperation to set
	 */
	public void setMngToolOperation(boolean mngToolOperation) {
		this.mngToolOperation = mngToolOperation;
	}
	@Override
	public List<String> getUploadImageIds() {
		return uploadImageIds;
	}
	@Override
	public void setUploadImageIds(List<String> uploadImageIds) {
		this.uploadImageIds = uploadImageIds;
	}
	@Override
	public EditorVersions getEditorVersion() {
		return editorVersion;
	}
	@Override
	public void setEditorVersion(EditorVersions editorVersion) {
		this.editorVersion = editorVersion;
	}
	
	@Override
	public List<SaveImageDO> getSaveImages() {
		return saveImages;
	}
	@Override
	public void setSaveImages(List<SaveImageDO> saveImages) {
		this.saveImages = saveImages;
	}
	@Override
	public String getContentId() {
		return questionId;
	}
	
	@Override
	public boolean isPostImmediatelyAfter() {
		if( getPostDate() == null )
			return false;
		if( getStatus() == null || !ContentsStatus.SUBMITTED.equals(getStatus()))
			return false;
		return checkPostImmediatelyAfter(getPostDate());
	}
}
