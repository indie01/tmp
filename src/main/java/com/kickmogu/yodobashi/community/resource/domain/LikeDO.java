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
import com.kickmogu.yodobashi.community.resource.domain.constants.LikeTargetType;
import com.kickmogu.yodobashi.community.resource.domain.util.CommunityUserUtil;


@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.LARGE)
@SolrSchema
public class LikeDO extends BaseWithTimestampDO implements SolrVisible, StoppableContents {

	/**
	 *
	 */
	private static final long serialVersionUID = -1863024902117909843L;

	/**
	 * いいねIDです。
	 */
	@HBaseKey(createTableSplitKeys={"1","5","9","D","H","L","P","T","X","b","f","j","n","r","v"})
	@SolrField @SolrUniqKey
	private String likeId;

	/**
	 * 対象タイプです。
	 */
	@HBaseColumn
	@SolrField
	private LikeTargetType targetType;

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
	 * @return targetType
	 */
	public LikeTargetType getTargetType() {
		return targetType;
	}

	/**
	 * @param targetType セットする targetType
	 */
	public void setTargetType(LikeTargetType targetType) {
		this.targetType = targetType;
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
	 * @return likeId
	 */
	public String getLikeId() {
		return likeId;
	}

	/**
	 * @param likeId セットする likeId
	 */
	public void setLikeId(String likeId) {
		this.likeId = likeId;
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

	public String getWithdrawKey() {
		return withdrawKey;
	}

	public void setWithdrawKey(String withdrawKey) {
		this.withdrawKey = withdrawKey;
	}

	/**
	 * @return sku
	 */
	public String getSku() {
		return sku;
	}

	/**
	 * @param sku セットする sku
	 */
	public void setSku(String sku) {
		this.sku = sku;
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

	@Override
	public boolean visible() {
		return !isDeleted();
	}

	@Override
	public String[] getHintPropertyNames() {
		return new String[]{"withdraw"};
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

}
