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
import com.kickmogu.yodobashi.community.resource.domain.constants.PurchaseHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.util.CommunityUserUtil;


@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
},sizeGroup=SizeGroup.LARGE)
@SolrSchema
public class QuestionAnswerDO extends AbstractContentBaseDO implements TextEditableContents, StoppableContents {

	/**
	 *
	 */
	private static final long serialVersionUID = 3236707144802307088L;

	/**
	 * 回答IDです。
	 */
	@HBaseKey(createTableSplitKeys={"1","5","9","D","H","L","P","T","X","b","f","j","n","r","v"})
	@SolrField @SolrUniqKey
	private String questionAnswerId;

	/**
	 * 質問です。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo QuestionDO question;

	/**
	 * 商品情報です。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo ProductDO product;

	/**
	 * コミュニティユーザです。
	 */
	@HBaseColumn(indexTableAdditionalColumns="questionId")
	@SolrField
	private @BelongsTo CommunityUserDO communityUser;

	/**
	 * 購入日です。
	 */
	@HBaseColumn
	@SolrField
	private Date purchaseDate;

	/**
	 * 購入履歴タイプです。
	 */
	@HBaseColumn
	@SolrField
	private PurchaseHistoryType purchaseHistoryType;

	/**
	 * 回答本文です。
	 */
	@HBaseColumn
	@SolrField(indexed=false)
	private String answerBody;

	/**
	 * アダルト商品に対する質問回答かどうかです。
	 */
	@HBaseColumn
	@SolrField
	private boolean adult;

	/**
	 * スコアです。
	 */
	@SolrField
	@HBaseColumn
	private double questionAnswerScore;

	/**
	 * ステータス
	 */
	@HBaseColumn
	@SolrField
	private ContentsStatus status;

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
	 * お知らせのリストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<InformationDO> informations = Lists.newArrayList();

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
	 * コメントのリストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<CommentDO> comments = Lists.newArrayList();

	/**
	 * いいねのリストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<LikeDO> likes = Lists.newArrayList();
	
	/**
	 * 参考になったのリストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<VotingDO> votings = Lists.newArrayList();

	/**
	 * 違反報告リストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<SpamReportDO> spamReports = Lists.newArrayList();

	/**
	 * 関連質問オーナーIDです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex
	private String relationQuestionOwnerId;

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
	
	/**
	 * 画像ID一覧
	 */
	@Deprecated
	@HBaseColumn
	private List<String> uploadImageIds;
	
	/** 一時保存用画像一覧 */
	@HBaseColumn
	private List<SaveImageDO> saveImages  = Lists.newArrayList();
	
	/**
	 * エディターバージョン
	 */
	@SolrField
	@HBaseColumn
	private EditorVersions editorVersion = EditorVersions.WYSIWYG_EDITOR;
	
	private PurchaseProductDO purchaseProduct;
	
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
		return withdraw || status.equals(ContentsStatus.DELETE);
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
	 * @return purchaseDate
	 */
	public Date getPurchaseDate() {
		return purchaseDate;
	}

	/**
	 * @param purchaseDate セットする purchaseDate
	 */
	public void setPurchaseDate(Date purchaseDate) {
		this.purchaseDate = purchaseDate;
	}

	/**
	 * @return answerBody
	 */
	public String getAnswerBody() {
		return answerBody;
	}

	/**
	 * @param answerBody セットする answerBody
	 */
	public void setAnswerBody(String answerBody) {
		this.answerBody = answerBody;
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
	 * @return questionAnswerId
	 */
	public String getQuestionAnswerId() {
		return questionAnswerId;
	}

	/**
	 * @param questionAnswerId セットする questionAnswerId
	 */
	public void setQuestionAnswerId(String questionAnswerId) {
		this.questionAnswerId = questionAnswerId;
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

	/**
	 * @return comments
	 */
	public List<CommentDO> getComments() {
		return comments;
	}

	/**
	 * @param comments セットする comments
	 */
	public void setComments(List<CommentDO> comments) {
		this.comments = comments;
	}

	/**
	 * @return likes
	 */
	public List<LikeDO> getLikes() {
		return likes;
	}

	/**
	 * @param likes セットする likes
	 */
	public void setLikes(List<LikeDO> likes) {
		this.likes = likes;
	}

	public List<VotingDO> getVotings() {
		return votings;
	}

	public void setVotings(List<VotingDO> votings) {
		this.votings = votings;
	}

	@Override
	public String getTextEditableText() {
		return answerBody;
	}

	@Override
	public void setTextEditableText(String text) {
		this.answerBody = text;
	}

	/**
	 * @return questionAnswerScore
	 */
	public double getQuestionAnswerScore() {
		return questionAnswerScore;
	}

	/**
	 * @param questionAnswerScore セットする questionAnswerScore
	 */
	public void setQuestionAnswerScore(double questionAnswerScore) {
		this.questionAnswerScore = questionAnswerScore;
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

	public String getWithdrawKey() {
		return withdrawKey;
	}

	public void setWithdrawKey(String withdrawKey) {
		this.withdrawKey = withdrawKey;
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
	 * @return purchaseHistoryType
	 */
	public PurchaseHistoryType getPurchaseHistoryType() {
		return purchaseHistoryType;
	}

	/**
	 * @param purchaseHistoryType セットする purchaseHistoryType
	 */
	public void setPurchaseHistoryType(PurchaseHistoryType purchaseHistoryType) {
		this.purchaseHistoryType = purchaseHistoryType;
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
		return questionAnswerId;
	}

	public PurchaseProductDO getPurchaseProduct() {
		return purchaseProduct;
	}

	public void setPurchaseProduct(PurchaseProductDO purchaseProduct) {
		this.purchaseProduct = purchaseProduct;
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
