package com.kickmogu.yodobashi.community.resource.domain;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.resource.annotation.BelongsTo;
import com.kickmogu.lib.core.resource.annotation.HasMany;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
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
public class ReviewDO extends AbstractReviewDO implements TextEditableContents, StoppableContents {

	/**
	 *
	 */
	private static final long serialVersionUID = 1454287216739550200L;

	/**
	 * レビューIDです。
	 */
	@HBaseKey(createTableSplitKeys={"1","5","9","D","H","L","P","T","X","b","f","j","n","r","v"})
	@SolrField @SolrUniqKey
	private String reviewId;

	/**
	 * 閲覧数です。
	 */
	@HBaseColumn
	@SolrField
	private long viewCount;

	/**
	 * スコアです。
	 */
	@SolrField
	@HBaseColumn
	private double reviewScore;

	/**
	 * お知らせ情報リストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<InformationDO> informations = Lists.newArrayList();

	/**
	 * コミュニティユーザーです。
	 */
	@HBaseColumn(indexTableAdditionalColumns={"productId","effective"})
	@SolrField
	private @BelongsTo CommunityUserDO communityUser;

	/**
	 * レビュー対象となる商品です。
	 */
	@HBaseColumn(indexTableAdditionalColumns="status")
	@SolrField
	private @BelongsTo ProductDO product;

	/**
	 * 購入の決め手情報リストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<ReviewDecisivePurchaseDO> reviewDecisivePurchases = Lists.newArrayList();

	/**
	 * 購入を迷った商品情報リストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<PurchaseLostProductDO> purchaseLostProducts = Lists.newArrayList();

	/**
	 * 過去に使用した商品情報です。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<UsedProductDO> usedProducts = Lists.newArrayList();

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
	 * 管理ツール操作フラグ
	 */
	@SolrField
	@HBaseColumn
	private boolean mngToolOperation=false;
	
	/**
	 * 一緒に投稿する画像IDの一覧
	 */
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
	 * @return reviewId
	 */
	public String getReviewId() {
		return reviewId;
	}

	/**
	 * @param reviewId セットする reviewId
	 */
	public void setReviewId(String reviewId) {
		this.reviewId = reviewId;
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

	/**
	 * @return reviewScore
	 */
	public double getReviewScore() {
		return reviewScore;
	}

	/**
	 * @param reviewScore セットする reviewScore
	 */
	public void setReviewScore(double reviewScore) {
		this.reviewScore = reviewScore;
	}

	@Override
	public String getTextEditableText() {
		return getReviewBody();
	}

	@Override
	public void setTextEditableText(String text) {
		setReviewBody(text);
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
		if(!this.communityUser.getCommunityUserId().equals(communityUserId) && getStatus().equals(ContentsStatus.CONTENTS_STOP)) {
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
		// TODO Auto-generated method stub
		return saveImages;
	}

	@Override
	public void setSaveImages(List<SaveImageDO> saveImages) {
		this.saveImages = saveImages;
	}
	@Override
	public String getContentId() {
		return reviewId;
	}
}