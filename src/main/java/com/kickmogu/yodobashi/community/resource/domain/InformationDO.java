/**
 *
 */
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
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;
import com.kickmogu.yodobashi.community.resource.domain.util.CommunityUserUtil;


/**
 * お知らせです。
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.HUGE)
@SolrSchema
public class InformationDO extends BaseWithTimestampDO implements SolrVisible, StoppableContents {

	/**
	 *
	 */
	private static final long serialVersionUID = 6396072493983511087L;

	/**
	 * お知らせIDです。
	 */
	@HBaseKey(createTableSplitKeys={"#", "5", "A", "G", "M", "S", "Y", "e", "k", "q", "w"})
	@SolrField @SolrUniqKey
	private String informationId;

	/**
	 * お知らせタイプです。
	 */
	@HBaseColumn
	@SolrField
	private InformationType informationType;

	/**
	 * コミュニティユーザーです。
	 */
	@HBaseColumn(indexTableAdditionalColumns={"followerCommunityUserId", "informationType"})
	@SolrField
	private @BelongsTo("informations") CommunityUserDO communityUser;

	/**
	 * お知らせ日時です。
	 */
	@HBaseColumn
	@SolrField
	private Date informationTime;

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
	 * 参照日です。
	 */
	@HBaseColumn
	@SolrField
	private Date readDate;

	/**
	 * 既読フラグです。
	 */
	@HBaseColumn
	@SolrField
	private boolean readFlag;

	/**
	 * フォロワーです。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo("followerInformations") CommunityUserDO followerCommunityUser;

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
	 * いいねです。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo LikeDO like;
	
	/**
	 * 参考になったのリストです。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo VotingDO voting;

	/**
	 * コメントです。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo CommentDO comment;

	/**
	 * 付与済みポイントです。
	 */
	@HBaseColumn
	@SolrField
	private Long grantPoint;

	/**
	 * 画像ヘッダーです。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo ImageHeaderDO imageHeader;

	/**
	 * 関連質問オーナーIDです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex(additionalColumns="informationType")
	private String relationQuestionOwnerId;

	/**
	 * 関連質問回答オーナーIDです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex(additionalColumns="informationType")
	private String relationQuestionAnswerOwnerId;

	/**
	 * 関連コメントオーナーIDです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex(additionalColumns="informationType")
	private String relationCommentOwnerId;

	/**
	 * 関連いいねオーナーIDです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex(additionalColumns="informationType")
	private String relationLikeOwnerId;
	
	@HBaseColumn
	@SolrField
	@HBaseIndex(additionalColumns="informationType")
	private String relationVotingOwnerId;

	/**
	 * 関連コミュニティユーザーIDです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex
	private String relationCommunityUserId;

	/**
	 * 関連レビューオーナーIDです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex
	private String relationReviewOwnerId;

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
	 * @return informationId
	 */
	public String getInformationId() {
		return informationId;
	}

	/**
	 * @param informationId セットする informationId
	 */
	public void setInformationId(String informationId) {
		this.informationId = informationId;
	}

	/**
	 * @return informationType
	 */
	public InformationType getInformationType() {
		return informationType;
	}

	/**
	 * @param informationType セットする informationType
	 */
	public void setInformationType(InformationType informationType) {
		this.informationType = informationType;
	}

	/**
	 * @return informationTime
	 */
	public Date getInformationTime() {
		return informationTime;
	}

	/**
	 * @param informationTime セットする informationTime
	 */
	public void setInformationTime(Date informationTime) {
		this.informationTime = informationTime;
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
	 * @return followerCommunityUser
	 */
	public CommunityUserDO getFollowerCommunityUser() {
		return followerCommunityUser;
	}

	/**
	 * @param followerCommunityUser セットする followerCommunityUser
	 */
	public void setFollowerCommunityUser(CommunityUserDO followerCommunityUser) {
		this.followerCommunityUser = followerCommunityUser;
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
	 * @return like
	 */
	public LikeDO getLike() {
		return like;
	}

	/**
	 * @param like セットする like
	 */
	public void setLike(LikeDO like) {
		this.like = like;
	}

	public VotingDO getVoting() {
		return voting;
	}

	public void setVoting(VotingDO voting) {
		this.voting = voting;
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
	 * @return readDate
	 */
	public Date getReadDate() {
		return readDate;
	}

	/**
	 * @param readDate セットする readDate
	 */
	public void setReadDate(Date readDate) {
		this.readDate = readDate;
	}

	/**
	 * @return readFlag
	 */
	public boolean isReadFlag() {
		return readFlag;
	}

	/**
	 * @param readFlag セットする readFlag
	 */
	public void setReadFlag(boolean readFlag) {
		this.readFlag = readFlag;
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
		return new String[]{"withdraw", "deleteFlag"};
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
	 * @return relationLikeOwnerId
	 */
	public String getRelationLikeOwnerId() {
		return relationLikeOwnerId;
	}

	/**
	 * @param relationLikeOwnerId セットする relationLikeOwnerId
	 */
	public void setRelationLikeOwnerId(String relationLikeOwnerId) {
		this.relationLikeOwnerId = relationLikeOwnerId;
	}

	/**
	 * @return relationCommunityUserId
	 */
	public String getRelationCommunityUserId() {
		return relationCommunityUserId;
	}

	/**
	 * @param relationCommunityUserId セットする relationCommunityUserId
	 */
	public void setRelationCommunityUserId(String relationCommunityUserId) {
		this.relationCommunityUserId = relationCommunityUserId;
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
	 * @return grantPoint
	 */
	public Long getGrantPoint() {
		return grantPoint;
	}

	/**
	 * @param grantPoint セットする grantPoint
	 */
	public void setGrantPoint(Long grantPoint) {
		this.grantPoint = grantPoint;
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

	public String getRelationVotingOwnerId() {
		return relationVotingOwnerId;
	}

	public void setRelationVotingOwnerId(String relationVotingOwnerId) {
		this.relationVotingOwnerId = relationVotingOwnerId;
	}
	// TODO あとで共通化する
	public String getRelationContentId(){
		if( PostContentType.REVIEW.equals(imageHeader.getPostContentType()) ){
			if( imageHeader.getReview() != null ){
				return imageHeader.getReview().getReviewId();
			}
		}else if( PostContentType.QUESTION.equals(imageHeader.getPostContentType()) ||
				PostContentType.ANSWER.equals(imageHeader.getPostContentType())){
			if(imageHeader.getQuestion() != null ){
				return imageHeader.getQuestion().getQuestionId();
			}
		}else if( PostContentType.IMAGE_SET.equals(imageHeader.getPostContentType()) ){
			return imageHeader.getImageSetId();
		}
		
		return null;
	}
	
	public ImageTargetType getImageTargetType(){
		if( PostContentType.REVIEW.equals(imageHeader.getPostContentType()) ){
			return ImageTargetType.REVIEW;
		}else if( PostContentType.QUESTION.equals(imageHeader.getPostContentType()) ||
				PostContentType.ANSWER.equals(imageHeader.getPostContentType())){
			return ImageTargetType.QUESTION;
		}else if( PostContentType.IMAGE_SET.equals(imageHeader.getPostContentType()) ){
			return ImageTargetType.IMAGE;
		}
		
		return null;
	}
	

}
