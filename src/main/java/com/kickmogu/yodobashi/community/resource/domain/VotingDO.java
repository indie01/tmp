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
import com.kickmogu.lib.hadoop.hbase.annotation.RelatedByHBase;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.lib.solr.annotation.RelatedBySolr;
import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.lib.solr.annotation.SolrSchema;
import com.kickmogu.lib.solr.annotation.SolrUniqKey;
import com.kickmogu.yodobashi.community.resource.dao.util.UserUtil;
import com.kickmogu.yodobashi.community.resource.domain.constants.VotingTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.VotingType;
import com.kickmogu.yodobashi.community.resource.domain.util.CommunityUserUtil;

@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.LARGE)
@SolrSchema
public class VotingDO extends BaseWithTimestampDO implements SolrVisible,StoppableContents {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3706272171939371206L;
	
	/**
	 * 参考になったIDです。
	 */
	@HBaseKey(createTableSplitKeys={"1","5","9","D","H","L","P","T","X","b","f","j","n","r","v"})
	@SolrField @SolrUniqKey
	private String votingId;
	
	/**
	 * 対象タイプです。
	 */
	@HBaseColumn
	@SolrField
	private VotingTargetType targetType;
	
	/**
	 * 参考になった「１：はい、２：いいえ）
	 */
	@HBaseColumn
	@SolrField
	private VotingType votingType;
	
	
	/**
	 * レビューです。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo ReviewDO review;

	/**
	 * SKUです。
	 */
	@HBaseColumn
	@SolrField
	private String sku;

	/**
	 * 質問IDです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex
	private String questionId;

	/**
	 * 質問回答です。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo QuestionAnswerDO questionAnswer;

	/**
	 * 画像セットIDです。
	 */
	@HBaseColumn
	@SolrField
	private String imageSetId;

	/**
	 * 画像ヘッダーです。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo ImageHeaderDO imageHeader;

	/**
	 * 投稿日です。
	 */
	@HBaseColumn
	@SolrField
	private Date postDate;

	/**
	 * コミュニティユーザです。
	 */
	@HBaseColumn(indexTableAdditionalColumns="targetType")
	@SolrField
	private @BelongsTo CommunityUserDO communityUser;

	/**
	 * お知らせのリストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<InformationDO> informations = Lists.newArrayList();

	/**
	 * 関連レビューオーナーIDです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex(additionalColumns="targetType")
	private String relationReviewOwnerId;

	/**
	 * 関連質問オーナーIDです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex(additionalColumns="targetType")
	private String relationQuestionOwnerId;

	/**
	 * 関連質問回答オーナーIDです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex(additionalColumns="targetType")
	private String relationQuestionAnswerOwnerId;

	/**
	 * 関連画像オーナーIDです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex(additionalColumns="targetType")
	private String relationImageOwnerId;

	/**
	 * アダルト商品に関するコンテンツのいいねかどうかです。
	 */
	@HBaseColumn
	@SolrField
	private boolean adult;

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
	
	public String getVotingId() {
		return votingId;
	}

	public void setVotingId(String votingId) {
		this.votingId = votingId;
	}

	public VotingTargetType getTargetType() {
		return targetType;
	}

	public void setTargetType(VotingTargetType targetType) {
		this.targetType = targetType;
	}

	public VotingType getVotingType() {
		return votingType;
	}

	public void setVotingType(VotingType votingType) {
		this.votingType = votingType;
	}

	public ReviewDO getReview() {
		return review;
	}

	public void setReview(ReviewDO review) {
		this.review = review;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getQuestionId() {
		return questionId;
	}

	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}

	public QuestionAnswerDO getQuestionAnswer() {
		return questionAnswer;
	}

	public void setQuestionAnswer(QuestionAnswerDO questionAnswer) {
		this.questionAnswer = questionAnswer;
	}

	public String getImageSetId() {
		return imageSetId;
	}

	public void setImageSetId(String imageSetId) {
		this.imageSetId = imageSetId;
	}

	public ImageHeaderDO getImageHeader() {
		return imageHeader;
	}

	public void setImageHeader(ImageHeaderDO imageHeader) {
		this.imageHeader = imageHeader;
	}

	public Date getPostDate() {
		return postDate;
	}

	public void setPostDate(Date postDate) {
		this.postDate = postDate;
	}

	public CommunityUserDO getCommunityUser() {
		return communityUser;
	}

	public void setCommunityUser(CommunityUserDO communityUser) {
		this.communityUser = communityUser;
	}

	public List<InformationDO> getInformations() {
		return informations;
	}

	public void setInformations(List<InformationDO> informations) {
		this.informations = informations;
	}

	public String getRelationReviewOwnerId() {
		return relationReviewOwnerId;
	}

	public void setRelationReviewOwnerId(String relationReviewOwnerId) {
		this.relationReviewOwnerId = relationReviewOwnerId;
	}

	public String getRelationQuestionOwnerId() {
		return relationQuestionOwnerId;
	}

	public void setRelationQuestionOwnerId(String relationQuestionOwnerId) {
		this.relationQuestionOwnerId = relationQuestionOwnerId;
	}

	public String getRelationQuestionAnswerOwnerId() {
		return relationQuestionAnswerOwnerId;
	}

	public void setRelationQuestionAnswerOwnerId(
			String relationQuestionAnswerOwnerId) {
		this.relationQuestionAnswerOwnerId = relationQuestionAnswerOwnerId;
	}

	public String getRelationImageOwnerId() {
		return relationImageOwnerId;
	}

	public void setRelationImageOwnerId(String relationImageOwnerId) {
		this.relationImageOwnerId = relationImageOwnerId;
	}

	public boolean isAdult() {
		return adult;
	}

	public void setAdult(boolean adult) {
		this.adult = adult;
	}

	public boolean isWithdraw() {
		return withdraw;
	}

	public void setWithdraw(boolean withdraw) {
		this.withdraw = withdraw;
	}

	public String getWithdrawKey() {
		return withdrawKey;
	}

	public void setWithdrawKey(String withdrawKey) {
		this.withdrawKey = withdrawKey;
	}
	
	/**
	 * 削除済かどうか返します。
	 * @return 削除済の場合、true
	 */
	public boolean isDeleted() {
		return withdraw;
	}

	@Override
	public List<CommunityUserDO> getRelationOwners() {
		return CommunityUserUtil.getRelationOwners(this);
	}

	@Override
	public List<String> getRelationOwnerIds() {
		return CommunityUserUtil.getRelationOwnerIds(this);
	}

	@Override
	public boolean isStop(String communityUserId, Set<String> stopCommunityUserIds) {
		return UserUtil.isStop(this, communityUserId, stopCommunityUserIds);
	}

	@Override
	public boolean visible() {
		return !isDeleted();
	}

	@Override
	public String[] getHintPropertyNames() {
		return new String[]{"withdraw"};
	}

}
