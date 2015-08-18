package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.resource.annotation.BelongsTo;
import com.kickmogu.lib.core.resource.annotation.HasMany;
import com.kickmogu.lib.core.resource.annotation.HasOne;
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
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageDeleteResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageSyncStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageUploadResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PurchaseHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.util.CommunityUserUtil;

@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.MEDIUM)
@SolrSchema
public class ImageHeaderDO extends AbstractContentBaseDO implements StoppableContents {

	/**
	 *
	 */
	private static final long serialVersionUID = -5349390547109199922L;

	/**
	 *  画像ID
	 */
	@HBaseKey(createTableSplitKeys={"1","5","9","D","H","L","P","T","X","b","f","j","n","r","v"})
	@SolrField @SolrUniqKey
	private String imageId;

	/**
	 *  画像URL
	 */
	@HBaseColumn
	@SolrField
	private String imageUrl;

	/**
	 * サムネイル画像IDです。
	 */
	@HBaseColumn
	@SolrField
	private String thumbnailImageId;

	/**
	 * サムネイル画像URLです。
	 */
	@HBaseColumn
	@SolrField
	private String thumbnailImageUrl;

	/**
	 * 横幅です。
	 */
	@HBaseColumn
	@SolrField
	private int width;

	/**
	 * 縦幅です。
	 */
	@HBaseColumn
	@SolrField
	private int heigth;

	/**
	 * 画像アップロード結果です。
	 */
	@HBaseColumn
	@SolrField
	private ImageUploadResult imageUploadResult;

	/**
	 * 画像削除結果です。
	 */
	@HBaseColumn
	@SolrField
	private ImageDeleteResult imageDeleteResult;

	/**
	 * 同期ステータスです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex
	private ImageSyncStatus imageSyncStatus;

	/**
	 *  投稿コンテンツタイプ
	 */
	@HBaseColumn
	@SolrField
	private PostContentType postContentType;

	/**
	 * レビューです。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo ReviewDO review;

	/**
	 * 質問です。
	 */
	@HBaseColumn(indexTableAdditionalColumns="status")
	@SolrField
	private @BelongsTo QuestionDO question;

	/**
	 * 質問回答です。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo QuestionAnswerDO questionAnswer;

	/**
	 * プロフィール画像オーナーです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasOne CommunityUserDO communityUser;

	/**
	 * サムネイル画像オーナーです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasOne CommunityUserDO thumbnailUser;

	/**
	 * オーナーとなるコミュニティユーザーです。
	 */
	@HBaseColumn(indexTableAdditionalColumns={"status", "postContentType"})
	@SolrField
	private @BelongsTo CommunityUserDO ownerCommunityUser;

	/**
	 * 画像セットIDです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex
	private String imageSetId;

	/**
	 * 購入日時です。
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
	 * お知らせのリストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<InformationDO> informations = Lists.newArrayList();

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
	 * 画像セット内の表示順です。
	 */
	@HBaseColumn
	@SolrField
	private int imageSetIndex;

	/**
	 * アダルト商品に対する画像かどうかです。
	 */
	@HBaseColumn
	@SolrField
	private boolean adult;

	/**
	 * サムネイル画像かどうかです。
	 */
	@HBaseColumn
	@SolrField
	private boolean thumbnail;

	/**
	 *  状態
	 */
	@HBaseColumn
	@SolrField
	private ContentsStatus status;

	/**
	 * コメントです。
	 */
	@HBaseColumn
	@SolrField
	private String comment;

	/**
	 *  投稿日
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
	 * 一覧表示フラグです。
	 */
	@HBaseColumn
	@SolrField
	private boolean listViewFlag;

	/**
	 * 移行前の画像ファイル名です。
	 */
	@HBaseColumn
	private String oldFileName;

	/**
	 * 関連質問オーナーIDです。
	 */
	@HBaseColumn
	@SolrField
	@HBaseIndex(additionalColumns={"status", "postContentType"})
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
	 * コメントのリストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<CommentDO> comments = Lists.newArrayList();

	/**
	 * アクション履歴のリストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<ActionHistoryDO> actionHistorys = Lists.newArrayList();

	/**
	 * 商品です。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo ProductDO product;

	/**
	 * 一時保存用サムネイル画像です。
	 */
	private ImageHeaderDO tempThumbnailImage;

	/**
	 * 管理ツール操作フラグ
	 */
	@SolrField
	@HBaseColumn
	private boolean mngToolOperation=false;

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
	 * @return the imageId
	 */
	public String getImageId() {
		return imageId;
	}
	/**
	 * @param imageId the imageId to set
	 */
	public void setImageId(String imageId) {
		this.imageId = imageId;
	}
	/**
	 * @return the imageUrl
	 */
	public String getImageUrl() {
		return imageUrl;
	}
	/**
	 * @param imageUrl the imageUrl to set
	 */
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	/**
	 * @return the postContentType
	 */
	public PostContentType getPostContentType() {
		return postContentType;
	}
	/**
	 * @param postContentType the postContentType to set
	 */
	public void setPostContentType(PostContentType postContentType) {
		this.postContentType = postContentType;
	}
	/**
	 * @return the postDate
	 */
	public Date getPostDate() {
		return postDate;
	}
	/**
	 * @param postDate the postDate to set
	 */
	public void setPostDate(Date postDate) {
		this.postDate = postDate;
	}
	/**
	 * @param status セットする status
	 */
	public void setStatus(ContentsStatus status) {
		this.status = status;
	}
	/**
	 * @return status
	 */
	public ContentsStatus getStatus() {
		return status;
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
	 * @return listViewFlag
	 */
	public boolean isListViewFlag() {
		return listViewFlag;
	}
	/**
	 * @param listViewFlag セットする listViewFlag
	 */
	public void setListViewFlag(boolean listViewFlag) {
		this.listViewFlag = listViewFlag;
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
	 * 対象とするコンテンツIDを返します。
	 * @return コンテンツID
	 */
	public String getContentsId() {
		if( PostContentType.REVIEW.equals(postContentType) ){
			return review.getReviewId();
		}else if( PostContentType.QUESTION.equals(postContentType)){
			return question.getQuestionId();
		}else if( PostContentType.ANSWER.equals(postContentType)){
			return questionAnswer.getQuestionAnswerId();
		}else if( PostContentType.IMAGE_SET.equals(postContentType) ){
			return imageSetId;
		}else if( PostContentType.PROFILE.equals(postContentType) ){
			return communityUser.getCommunityUserId();
		}else if( PostContentType.PROFILE_THUMBNAIL.equals(postContentType) ){
			return thumbnailUser.getCommunityUserId();
		} else {
			return null;
		}
	}
	
	public ImageTargetType getImageTargetType(){
		if( PostContentType.REVIEW.equals(postContentType) ){
			return ImageTargetType.REVIEW;
		}else if( PostContentType.QUESTION.equals(postContentType)){
			return ImageTargetType.QUESTION;
		}else if( PostContentType.ANSWER.equals(postContentType)){
			return ImageTargetType.QUESTION_ANSWER;
		}else if( PostContentType.IMAGE_SET.equals(postContentType) ){
			return ImageTargetType.IMAGE;
		}
		
		return null;
	}
	
	public String getWithdrawKey() {
		return withdrawKey;
	}

	public void setWithdrawKey(String withdrawKey) {
		this.withdrawKey = withdrawKey;
	}

	/**
	 * @return oldFileName
	 */
	public String getOldFileName() {
		return oldFileName;
	}

	/**
	 * @param oldFileName セットする oldFileName
	 */
	public void setOldFileName(String oldFileName) {
		this.oldFileName = oldFileName;
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
	 * @return comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment セットする comment
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return imageSetIndex
	 */
	public int getImageSetIndex() {
		return imageSetIndex;
	}

	/**
	 * @param imageSetIndex セットする imageSetIndex
	 */
	public void setImageSetIndex(int imageSetIndex) {
		this.imageSetIndex = imageSetIndex;
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
	 * @return thumbnailUser
	 */
	public CommunityUserDO getThumbnailUser() {
		return thumbnailUser;
	}

	/**
	 * @param thumbnailUser セットする thumbnailUser
	 */
	public void setThumbnailUser(CommunityUserDO thumbnailUser) {
		this.thumbnailUser = thumbnailUser;
	}

	/**
	 * @return width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width セットする width
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return heigth
	 */
	public int getHeigth() {
		return heigth;
	}

	/**
	 * @param heigth セットする heigth
	 */
	public void setHeigth(int heigth) {
		this.heigth = heigth;
	}

	/**
	 * @return ownerCommunityUser
	 */
	public String getOwnerCommunityUserId() {
		return ownerCommunityUser.getCommunityUserId();
	}

	/**
	 * @return ownerCommunityUser
	 */
	public CommunityUserDO getOwnerCommunityUser() {
		return ownerCommunityUser;
	}

	/**
	 * @param ownerCommunityUser セットする ownerCommunityUser
	 */
	public void setOwnerCommunityUser(CommunityUserDO ownerCommunityUser) {
		this.ownerCommunityUser = ownerCommunityUser;
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
	 * @return thumbnailImageId
	 */
	public String getThumbnailImageId() {
		return thumbnailImageId;
	}

	/**
	 * @param thumbnailImageId セットする thumbnailImageId
	 */
	public void setThumbnailImageId(String thumbnailImageId) {
		this.thumbnailImageId = thumbnailImageId;
	}

	/**
	 * @return thumbnailImageUrl
	 */
	public String getThumbnailImageUrl() {
		return thumbnailImageUrl;
	}

	/**
	 * @param thumbnailImageUrl セットする thumbnailImageUrl
	 */
	public void setThumbnailImageUrl(String thumbnailImageUrl) {
		this.thumbnailImageUrl = thumbnailImageUrl;
	}

	/**
	 * @return tempThumbnailImage
	 */
	public ImageHeaderDO getTempThumbnailImage() {
		return tempThumbnailImage;
	}

	/**
	 * @param tempThumbnailImage セットする tempThumbnailImage
	 */
	public void setTempThumbnailImage(ImageHeaderDO tempThumbnailImage) {
		this.tempThumbnailImage = tempThumbnailImage;
	}

	/**
	 * @return thumbnail
	 */
	public boolean isThumbnail() {
		return thumbnail;
	}

	/**
	 * @param thumbnail セットする thumbnail
	 */
	public void setThumbnail(boolean thumbnail) {
		this.thumbnail = thumbnail;
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
	 * @return the sku
	 */
	public String getSku() {
		if (product == null) {
			return null;
		}
		return product.getSku();
	}
	/**
	 * @param sku the sku to set
	 */
	public void setSku(String sku) {
		product = new ProductDO();
		product.setSku(sku);
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
		if(!this.ownerCommunityUser.getCommunityUserId().equals(communityUserId) && this.status.equals(ContentsStatus.CONTENTS_STOP)) {
			return true;
		}
		return UserUtil.isStop(this, communityUserId, stopCommunityUserIds);
	}

	/**
	 * @return imageUploadResult
	 */
	public ImageUploadResult getImageUploadResult() {
		return imageUploadResult;
	}

	/**
	 * @param imageUploadResult セットする imageUploadResult
	 */
	public void setImageUploadResult(ImageUploadResult imageUploadResult) {
		this.imageUploadResult = imageUploadResult;
	}

	/**
	 * @return imageDeleteResult
	 */
	public ImageDeleteResult getImageDeleteResult() {
		return imageDeleteResult;
	}

	/**
	 * @param imageDeleteResult セットする imageDeleteResult
	 */
	public void setImageDeleteResult(ImageDeleteResult imageDeleteResult) {
		this.imageDeleteResult = imageDeleteResult;
	}

	/**
	 * @return imageSyncStatus
	 */
	public ImageSyncStatus getImageSyncStatus() {
		return imageSyncStatus;
	}

	/**
	 * @param imageSyncStatus セットする imageSyncStatus
	 */
	public void setImageSyncStatus(ImageSyncStatus imageSyncStatus) {
		this.imageSyncStatus = imageSyncStatus;
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
	public boolean isPostImmediatelyAfter() {
		if( getPostDate() == null )
			return false;
		if( getStatus() == null || !ContentsStatus.SUBMITTED.equals(getStatus()))
			return false;
		return checkPostImmediatelyAfter(getPostDate());
	}
}
