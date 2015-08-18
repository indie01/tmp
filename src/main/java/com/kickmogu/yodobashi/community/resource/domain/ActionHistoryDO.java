package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;
import java.util.List;
import java.util.Set;

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
import com.kickmogu.yodobashi.community.resource.dao.util.UserUtil;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.util.CommunityUserUtil;

/**
 * アクション履歴情報です。
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.HUGE)
@SolrSchema
public class ActionHistoryDO extends BaseWithTimestampDO implements SolrVisible, StoppableContents {
	/**
	 *
	 */
	private static final long serialVersionUID = 5336512191692581718L;

	/**
	 * アクション履歴情報IDです。
	 */
	@HBaseKey(createTableSplitKeys={"#", "5", "A", "G", "M", "S", "Y", "e", "k", "q", "w"})
	@SolrField @SolrUniqKey
	private String actionHistoryId;

	/**
	 * アクション履歴タイプです。
	 */
	@HBaseColumn
	@SolrField
	private ActionHistoryType actionHistoryType;

	/**
	 * コミュニティユーザーです。
	 */
	@HBaseColumn(indexTableAdditionalColumns={"actionHistoryType", "followCommunityUserId","productId", "questionId"})
	@SolrField
	private @BelongsTo("actionHistorys") CommunityUserDO communityUser;

	/**
	 * アクション日時です。
	 */
	@HBaseColumn
	@SolrField
	private Date actionTime;

	/**
	 * 商品マスターランクです。
	 */
	@HBaseColumn
	@SolrField
	private Integer productMasterRank;

	/**
	 * アダルト商品に対するものかどうかです。
	 */
	@HBaseColumn
	@SolrField
	private boolean adult;

	/**
	 * 削除日です。
	 */
	@HBaseColumn
	private Date deleteDate;

	/**
	 * 削除フラグです。
	 */
	@HBaseColumn
	private boolean deleteFlag;

	/**
	 * フォローしたコミュニティユーザーです。
	 */
	@HBaseColumn(indexTableAdditionalColumns="actionHistoryType")
	@SolrField
	private @BelongsTo("followedActionHistorys") CommunityUserDO followCommunityUser;

	/**
	 * 商品です。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo ProductDO product;

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
	 * 商品マスターです。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo ProductMasterDO productMaster;

	/**
	 * コメントです。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo CommentDO comment;

	/**
	 * 画像です。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo ImageHeaderDO imageHeader;

	/**
	 * 画像セットIDです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex
	private String imageSetId;

	/**
	 * 関連レビューオーナーIDです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex(additionalColumns="actionHistoryType")
	private String relationReviewOwnerId;

	/**
	 * 関連質問オーナーIDです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex(additionalColumns="actionHistoryType")
	private String relationQuestionOwnerId;

	/**
	 * 関連質問回答オーナーIDです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex(additionalColumns="actionHistoryType")
	private String relationQuestionAnswerOwnerId;

	/**
	 * 関連画像オーナーIDです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex(additionalColumns="actionHistoryType")
	private String relationImageOwnerId;

	/**
	 * 退会データかどうかです。
	 */
	@HBaseColumn
	private boolean withdraw;

	/**
	 * 退会キーです。
	 */
	@HBaseColumn
	@HBaseIndex
	private String withdrawKey;

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
		return withdraw || deleteFlag;
	}

	/**
	 * @return actionHistoryId
	 */
	public String getActionHistoryId() {
		return actionHistoryId;
	}

	/**
	 * @param actionHistoryId セットする actionHistoryId
	 */
	public void setActionHistoryId(String actionHistoryId) {
		this.actionHistoryId = actionHistoryId;
	}

	/**
	 * @return actionHistoryType
	 */
	public ActionHistoryType getActionHistoryType() {
		return actionHistoryType;
	}

	/**
	 * @param actionHistoryType セットする actionHistoryType
	 */
	public void setActionHistoryType(ActionHistoryType actionHistoryType) {
		this.actionHistoryType = actionHistoryType;
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
	 * @return actionTime
	 */
	public Date getActionTime() {
		return actionTime;
	}

	/**
	 * @param actionTime セットする actionTime
	 */
	public void setActionTime(Date actionTime) {
		this.actionTime = actionTime;
	}

	/**
	 * @return followCommunityUser
	 */
	public CommunityUserDO getFollowCommunityUser() {
		return followCommunityUser;
	}

	/**
	 * @param followCommunityUser セットする followCommunityUser
	 */
	public void setFollowCommunityUser(CommunityUserDO followCommunityUser) {
		this.followCommunityUser = followCommunityUser;
	}

	/**
	 * @return product
	 */
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
	 * @return productMaster
	 */
	public ProductMasterDO getProductMaster() {
		return productMaster;
	}

	/**
	 * @param productMaster セットする productMaster
	 */
	public void setProductMaster(ProductMasterDO productMaster) {
		this.productMaster = productMaster;
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
	 * @return deleteFlag
	 */
	public boolean isDeleteFlag() {
		return deleteFlag;
	}

	/**
	 * @param deleteFlag セットする deleteFlag
	 */
	public void setDeleteFlag(boolean deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public String getWithdrawKey() {
		return withdrawKey;
	}

	public void setWithdrawKey(String withdrawKey) {
		this.withdrawKey = withdrawKey;
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
	 * @return imageSetId
	 */
	public String getImageSetId() {
		return imageSetId;
	}

	/**
	 * @param imageSetId セットする imageSetId
	 */
	public void setImageSetId(String imageSetId) {
		this.imageSetId = imageSetId;
	}

	@Override
	public boolean visible() {
		return !isDeleted();
	}

	@Override
	public String[] getHintPropertyNames() {
		return new String[]{"withdraw", "deleteFlag"};
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
		return UserUtil.isStop(this, communityUserId, stopCommunityUserIds);
	}

	/**
	 * @return productMasterRank
	 */
	public Integer getProductMasterRank() {
		return productMasterRank;
	}

	/**
	 * @param productMasterRank セットする productMasterRank
	 */
	public void setProductMasterRank(Integer productMasterRank) {
		this.productMasterRank = productMasterRank;
	}
}
