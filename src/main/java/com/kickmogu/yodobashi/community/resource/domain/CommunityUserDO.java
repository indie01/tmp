package com.kickmogu.yodobashi.community.resource.domain;

import java.util.List;

import org.apache.commons.lang.StringUtils;

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
import com.kickmogu.yodobashi.community.resource.config.PathConfig;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.domain.constants.ASyncStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.Verification;

@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.SMALL)
@SolrSchema
public class CommunityUserDO extends BaseWithTimestampDO{
	/**
	 *
	 */
	private static final long serialVersionUID = 2989299123060111401L;

	/**
	 * コミュニティユーザーIDです。
	 */
	@HBaseKey(createTableSplitKeys={"1","5","9","D","H","L","P","T","X","b","f","j","n","r","v"})
	@SolrField @SolrUniqKey
	private String communityUserId;

	/** コミュニティコード*/
	@HBaseColumn
	@SolrField
	private String communityId;

	/** ハッシュ化コミュニティコード*/
	@HBaseColumn(uniqueCheckWith=HashCommunityIdDO.class)
	@SolrField
	@HBaseIndex
	private String hashCommunityId;

	/** ニックネーム*/
	@HBaseColumn
	@SolrField
	private String communityName;

	/** 標準化されたニックネームです。*/
	@HBaseColumn(uniqueCheckWith=CommunityNameDO.class)
	@SolrField
	private String normalizeCommunityName;

	/**
	 *  プロフィール画像です。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo("communityUser") ImageHeaderDO imageHeader;

	/**
	 *  サムネイル画像です。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo("thumbnailUser") ImageHeaderDO thumbnail;

	/**
	 *  プロフィール画像URLです。
	 */
	@HBaseColumn
	@SolrField
	private String profileImageUrl;

	/**
	 *  サムネイル画像URLです。
	 */
	@HBaseColumn
	@SolrField
	private String thumbnailImageUrl;

	/** HTTP・HTTPSアクセス制御 */
	@HBaseColumn
	@SolrField
	private boolean secureAccess;

	/**
	 * 退会キーです。
	 */
	@HBaseColumn
	@HBaseIndex
	private String withdrawKey;

	/**
	 * 退会処理中フラグです。
	 */
	@HBaseColumn
	private boolean withdrawLock;

//	/**
//	 * コンテンツを残すかどうかです。
//	 */
//	@HBaseColumn
//	@SolrField
//	private boolean keepContents;

	/**
	 * レビューコンテンツを残すかどうかです。
	 */
	@HBaseColumn
	@SolrField
	private boolean keepReviewContents=true;

	/**
	 * 質問及び質問回答コンテンツを残すかどうかです。
	 */
	@HBaseColumn
	@SolrField
	private boolean keepQuestionContents=true;

	/**
	 * 画像コンテンツを残すかどうかです。
	 */
	@HBaseColumn
	@SolrField
	private boolean keepImageContents=true;

	/**
	 * 画像コンテンツを残すかどうかです。
	 */
	@HBaseColumn
	@SolrField
	private boolean keepCommentContents=true;

	/**
	 * コミュニティ名マージが必要かどうかです。
	 */
	@HBaseColumn
	@SolrField
	private boolean communityNameMergeRequired;

	/** 共有化情報リストです。 */
	private List<AccountSharingDO> accountSharings = Lists.newArrayList();

	/**
	 * アカウントの共有化情報キャッシュです。
	 */
	@HBaseColumn
	private List<AccountSharingDO> accountSharingCaches = Lists.newArrayList();

	/**
	 * ステータスです。<br />
	 * 本システムで管理するステータスはあくまでキャッシュです。
	 */
	@HBaseColumn
	@SolrField
	private CommunityUserStatus status;

	/** CERO承認ステータスです */
	@HBaseColumn
	@SolrField
	private Verification ceroVerification = Verification.ATANYTIME;

	/** アダルト承認ステータスです */
	@HBaseColumn
	@SolrField
	private Verification adultVerification = Verification.ATANYTIME;
	
	/** アダルト承認ステータスが「随時」の場合に一時的に「アダルト商品を表示する」が選択されたか */
	private boolean isTemporaryAuthorized;

	/**
	 * フォローユーザー情報リストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<CommunityUserFollowDO> followCommunityUsers = Lists.newArrayList();

	/**
	 * フォロワーユーザー情報リストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<CommunityUserFollowDO> followerCommunityUsers = Lists.newArrayList();

	/**
	 * お知らせ情報です。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<InformationDO> informations = Lists.newArrayList();

	/**
	 * フォロワーのお知らせ情報です。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<InformationDO> followerInformations = Lists.newArrayList();

	/**
	 * アクション履歴情報リストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<ActionHistoryDO> actionHistorys = Lists.newArrayList();

	/**
	 * フォロワーにフォローされたアクション履歴情報リストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<ActionHistoryDO> followedActionHistorys = Lists.newArrayList();

	/**
	 * コメントリストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<CommentDO> comments = Lists.newArrayList();

	/**
	 * レビューリストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<ReviewDO> reviews = Lists.newArrayList();

	/**
	 * レビュー履歴リストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<ReviewHistoryDO> reviewHistorys = Lists.newArrayList();

	/**
	 * 購入の決め手リストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<ReviewDecisivePurchaseDO> reviewDecisivePurchases = Lists.newArrayList();

	/**
	 * 購入に迷った商品リストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<PurchaseLostProductDO> purchaseLostProducts = Lists.newArrayList();

	/**
	 * 過去に使用した商品リストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<UsedProductDO> usedProducts = Lists.newArrayList();

	/**
	 * 質問のリストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<QuestionDO> questions = Lists.newArrayList();

	/**
	 * 質問回答のリストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<QuestionAnswerDO> questionAnswers = Lists.newArrayList();

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
	 * プロダクトフォローのリストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<ProductFollowDO> productFollows = Lists.newArrayList();

	/**
	 * プロダクトマスターのリストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<ProductMasterDO> productMasters = Lists.newArrayList();

	/**
	 * 購入商品のリストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<PurchaseProductDO> purchaseProducts = Lists.newArrayList();

	/**
	 * 質問フォローのリストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<QuestionFollowDO> questionFollows = Lists.newArrayList();

	/**
	 * 違反報告リストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<SpamReportDO> spamReports = Lists.newArrayList();

	/**
	 * 画像リストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<ImageHeaderDO> imageHeaders = Lists.newArrayList();

	/**
	 * 管理ツールステータス残すかどうかです。
	 */
	@HBaseColumn
	@SolrField
	private ASyncStatus aSyncStatus = ASyncStatus.FIN;

	public boolean isActive() {
		return CommunityUserStatus.ACTIVE.equals(status) || CommunityUserStatus.STOP.equals(status);
	}

	public boolean isStop() {
		return CommunityUserStatus.STOP.equals(status);
	}

	public boolean isStop(RequestScopeDao requestScopeDao) {
		if (!isStop()) {
			return false;
		}
		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();
		if (loginCommunityUserId == null) {
			return true;
		} else {
			return !communityUserId.equals(loginCommunityUserId);
		}
	}

	/**
	 * @return the communityId
	 */
	public String getCommunityId() {
		return communityId;
	}

	/**
	 * @param communityId the communityId to set
	 */
	public void setCommunityId(String communityId) {
		this.communityId = communityId;
	}

	/**
	 * @return the hashCommunityId
	 */
	public String getHashCommunityId() {
		return hashCommunityId;
	}

	/**
	 * @param hashCommunityId the hashCommunityId to set
	 */
	public void setHashCommunityId(String hashCommunityId) {
		this.hashCommunityId = hashCommunityId;
	}

	/**
	 * @return the communityName
	 */
	public String getCommunityName() {
		return communityName;
	}

	/**
	 * @param communityName the communityName to set
	 */
	public void setCommunityName(String communityName) {
		this.communityName = communityName;
	}

	/**
	 * @return the imageHeader
	 */
	public ImageHeaderDO getImageHeader() {
		return imageHeader;
	}

	/**
	 * @param imageHeader the imageHeader to set
	 */
	public void setImageHeader(ImageHeaderDO imageHeader) {
		this.imageHeader = imageHeader;
	}

	/**
	 * @return the secureAccess
	 */
	public boolean isSecureAccess() {
		return secureAccess;
	}

	/**
	 * @param secureAccess the secureAccess to set
	 */
	public void setSecureAccess(boolean secureAccess) {
		this.secureAccess = secureAccess;
	}

	/**
	 * @return the status
	 */
	public CommunityUserStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(CommunityUserStatus status) {
		this.status = status;
	}

	/**
	 * @return accountSharings
	 */
	public List<AccountSharingDO> getAccountSharings() {
		return accountSharings;
	}

	/**
	 * @param accountSharings セットする accountSharings
	 */
	public void setAccountSharings(List<AccountSharingDO> accountSharings) {
		this.accountSharings = accountSharings;
	}

	/**
	 * @return communityUserId
	 */
	public String getCommunityUserId() {
		return communityUserId;
	}

	/**
	 * @param communityUserId セットする communityUserId
	 */
	public void setCommunityUserId(String communityUserId) {
		this.communityUserId = communityUserId;
	}

	/**
	 * @return normalizeCommunityName
	 */
	public String getNormalizeCommunityName() {
		return normalizeCommunityName;
	}

	/**
	 * @param normalizeCommunityName セットする normalizeCommunityName
	 */
	public void setNormalizeCommunityName(String normalizeCommunityName) {
		this.normalizeCommunityName = normalizeCommunityName;
	}

	/**
	 * @return followCommunityUsers
	 */
	public List<CommunityUserFollowDO> getFollowCommunityUsers() {
		return followCommunityUsers;
	}

	/**
	 * @param followCommunityUsers セットする followCommunityUsers
	 */
	public void setFollowCommunityUsers(
			List<CommunityUserFollowDO> followCommunityUsers) {
		this.followCommunityUsers = followCommunityUsers;
	}

	/**
	 * @return followerCommunityUsers
	 */
	public List<CommunityUserFollowDO> getFollowerCommunityUsers() {
		return followerCommunityUsers;
	}

	/**
	 * @param followerCommunityUsers セットする followerCommunityUsers
	 */
	public void setFollowerCommunityUsers(
			List<CommunityUserFollowDO> followerCommunityUsers) {
		this.followerCommunityUsers = followerCommunityUsers;
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
	 * @return followerInformations
	 */
	public List<InformationDO> getFollowerInformations() {
		return followerInformations;
	}

	/**
	 * @param followerInformations セットする followerInformations
	 */
	public void setFollowerInformations(List<InformationDO> followerInformations) {
		this.followerInformations = followerInformations;
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
	 * @return reviewDecisivePurchases
	 */
	public List<ReviewDecisivePurchaseDO> getReviewDecisivePurchases() {
		return reviewDecisivePurchases;
	}

	/**
	 * @param reviewDecisivePurchases セットする reviewDecisivePurchases
	 */
	public void setReviewDecisivePurchases(
			List<ReviewDecisivePurchaseDO> reviewDecisivePurchases) {
		this.reviewDecisivePurchases = reviewDecisivePurchases;
	}

	/**
	 * @return purchaseLostProducts
	 */
	public List<PurchaseLostProductDO> getPurchaseLostProducts() {
		return purchaseLostProducts;
	}

	/**
	 * @param purchaseLostProducts セットする purchaseLostProducts
	 */
	public void setPurchaseLostProducts(
			List<PurchaseLostProductDO> purchaseLostProducts) {
		this.purchaseLostProducts = purchaseLostProducts;
	}

	/**
	 * @return usedProducts
	 */
	public List<UsedProductDO> getUsedProducts() {
		return usedProducts;
	}

	/**
	 * @param usedProducts セットする usedProducts
	 */
	public void setUsedProducts(List<UsedProductDO> usedProducts) {
		this.usedProducts = usedProducts;
	}

	/**
	 * @return questions
	 */
	public List<QuestionDO> getQuestions() {
		return questions;
	}

	/**
	 * @param questions セットする questions
	 */
	public void setQuestions(List<QuestionDO> questions) {
		this.questions = questions;
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
	 * @return productFollows
	 */
	public List<ProductFollowDO> getProductFollows() {
		return productFollows;
	}

	/**
	 * @param productFollows セットする productFollows
	 */
	public void setProductFollows(List<ProductFollowDO> productFollows) {
		this.productFollows = productFollows;
	}

	/**
	 * @return productMasters
	 */
	public List<ProductMasterDO> getProductMasters() {
		return productMasters;
	}

	/**
	 * @param productMasters セットする productMasters
	 */
	public void setProductMasters(List<ProductMasterDO> productMasters) {
		this.productMasters = productMasters;
	}

	/**
	 * @return purchaseProducts
	 */
	public List<PurchaseProductDO> getPurchaseProducts() {
		return purchaseProducts;
	}

	/**
	 * @param purchaseProducts セットする purchaseProducts
	 */
	public void setPurchaseProducts(List<PurchaseProductDO> purchaseProducts) {
		this.purchaseProducts = purchaseProducts;
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
	 * @return reviews
	 */
	public List<ReviewDO> getReviews() {
		return reviews;
	}

	/**
	 * @param reviews セットする reviews
	 */
	public void setReviews(List<ReviewDO> reviews) {
		this.reviews = reviews;
	}

	/**
	 * @return reviewHistorys
	 */
	public List<ReviewHistoryDO> getReviewHistorys() {
		return reviewHistorys;
	}

	/**
	 * @param reviewHistorys セットする reviewHistorys
	 */
	public void setReviewHistorys(List<ReviewHistoryDO> reviewHistorys) {
		this.reviewHistorys = reviewHistorys;
	}

	/**
	 * サムネイル画像を返します。
	 * @return サムネイル画像
	 */
	public ImageHeaderDO getProfileThumbnailImage() {
		if (thumbnail == null || StringUtils.isEmpty(
				thumbnail.getImageId()) || !isActive()) {
			ImageHeaderDO thumbnailImage = new ImageHeaderDO();
			thumbnailImage.setImageUrl(PathConfig.INSTANCE.defaultThumbnailImageUrl);
			return thumbnailImage;
		} else {
			ImageHeaderDO thumbnailImage = new ImageHeaderDO();
			thumbnailImage.setImageUrl(thumbnailImageUrl);
			return thumbnailImage;
		}
	}

	/**
	 * プロフィール画像を返します。
	 * @return プロフィール画像
	 */
	public ImageHeaderDO getProfileImage() {
		if (imageHeader == null || StringUtils.isEmpty(
				imageHeader.getImageId()) || !isActive()) {
			ImageHeaderDO profileImage = new ImageHeaderDO();
			profileImage.setImageUrl(PathConfig.INSTANCE.defaultProfileImageUrl);
			return profileImage;
		} else {
			ImageHeaderDO profileImage = new ImageHeaderDO();
			profileImage.setImageUrl(profileImageUrl);
			return profileImage;
		}
	}

	/**
	 * @return followedActionHistorys
	 */
	public List<ActionHistoryDO> getFollowedActionHistorys() {
		return followedActionHistorys;
	}

	/**
	 * @param followedActionHistorys セットする followedActionHistorys
	 */
	public void setFollowedActionHistorys(
			List<ActionHistoryDO> followedActionHistorys) {
		this.followedActionHistorys = followedActionHistorys;
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

	public boolean isWithdrawLock() {
		return withdrawLock;
	}

	public void setWithdrawLock(boolean withdrawLock) {
		this.withdrawLock = withdrawLock;
	}

	/**
	 * @return the ceroVerification
	 */
	public Verification getCeroVerification() {
		return ceroVerification;
	}

	/**
	 * アダルトの状態を兼ねたcero認証状態を返す。
	 * @return ceroVerification
	 */
	public Verification getCeroVerificationWithAdultState() {
		// アダルト認証が終了している場合は、アダルトを優先する
		if(Verification.AUTHORIZED.equals(adultVerification))
			return Verification.AUTHORIZED;
		return ceroVerification;
	}

	/**
	 * @param ceroVerification the ceroVerification to set
	 */
	public void setCeroVerification(Verification ceroVerification) {
		this.ceroVerification = ceroVerification;
	}

	/**
	 * @return the adultVerification
	 */
	public Verification getAdultVerification() {
		return adultVerification;
	}

	/**
	 * @param adultVerification the adultVerification to set
	 */
	public void setAdultVerification(Verification adultVerification) {
		this.adultVerification = adultVerification;
	}

	/**
	 * @return thumbnail
	 */
	public ImageHeaderDO getThumbnail() {
		return thumbnail;
	}

	/**
	 * @param thumbnail セットする thumbnail
	 */
	public void setThumbnail(ImageHeaderDO thumbnail) {
		this.thumbnail = thumbnail;
	}

	/**
	 * @return profileImageUrl
	 */
	public String getProfileImageUrl() {
		return profileImageUrl;
	}

	/**
	 * @param profileImageUrl セットする profileImageUrl
	 */
	public void setProfileImageUrl(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
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
	 * @return communityNameMergeRequired
	 */
	public boolean isCommunityNameMergeRequired() {
		return communityNameMergeRequired;
	}

	/**
	 * @param communityNameMergeRequired セットする communityNameMergeRequired
	 */
	public void setCommunityNameMergeRequired(boolean communityNameMergeRequired) {
		this.communityNameMergeRequired = communityNameMergeRequired;
	}

	/**
	 * @return the keepReviewContents
	 */
	public boolean isKeepReviewContents() {
		return keepReviewContents;
	}

	/**
	 * @param keepReviewContents the keepReviewContents to set
	 */
	public void setKeepReviewContents(boolean keepReviewContents) {
		this.keepReviewContents = keepReviewContents;
	}

	/**
	 * @return the keepQuestionContents
	 */
	public boolean isKeepQuestionContents() {
		return keepQuestionContents;
	}

	/**
	 * @param keepQuestionContents the keepQuestionContents to set
	 */
	public void setKeepQuestionContents(boolean keepQuestionContents) {
		this.keepQuestionContents = keepQuestionContents;
	}

	/**
	 * @return the keepImageContents
	 */
	public boolean isKeepImageContents() {
		return keepImageContents;
	}

	/**
	 * @param keepImageContents the keepImageContents to set
	 */
	public void setKeepImageContents(boolean keepImageContents) {
		this.keepImageContents = keepImageContents;
	}

	/**
	 * @return the keepCommentContents
	 */
	public boolean isKeepCommentContents() {
		return keepCommentContents;
	}

	/**
	 * @param keepCommentContents the keepCommentContents to set
	 */
	public void setKeepCommentContents(boolean keepCommentContents) {
		this.keepCommentContents = keepCommentContents;
	}

	/**
	 * @return the aSyncStatus
	 */
	public ASyncStatus getaSyncStatus() {
		return aSyncStatus;
	}

	/**
	 * @param aSyncStatus the aSyncStatus to set
	 */
	public void setaSyncStatus(ASyncStatus aSyncStatus) {
		this.aSyncStatus = aSyncStatus;
	}

	/**
	 * @return accountSharingCaches
	 */
	public List<AccountSharingDO> getAccountSharingCaches() {
		return accountSharingCaches;
	}

	/**
	 * @param accountSharingCaches セットする accountSharingCaches
	 */
	public void setAccountSharingCaches(List<AccountSharingDO> accountSharingCaches) {
		this.accountSharingCaches = accountSharingCaches;
	}

	public boolean isTemporaryAuthorized() {
		return isTemporaryAuthorized;
	}

	public void setTemporaryAuthorized(boolean isTemporaryAuthorized) {
		this.isTemporaryAuthorized = isTemporaryAuthorized;
	}

}
