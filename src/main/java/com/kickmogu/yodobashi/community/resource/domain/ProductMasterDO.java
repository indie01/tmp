package com.kickmogu.yodobashi.community.resource.domain;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.io.Writable;

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
import com.kickmogu.yodobashi.community.resource.domain.util.CommunityUserUtil;

/**
 * 商品マスターです。
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.HUGE)
@SolrSchema
public class ProductMasterDO extends BaseWithTimestampDO implements Writable, SolrVisible, StoppableContents {

	/**
	 * ランクの範囲です。
	 */
	public static final int RANK_RANGE = 50;

	/**
	 *
	 */
	private static final long serialVersionUID = 5238834805471145216L;

	/**
	 * 商品マスターIDです。
	 */
	@HBaseKey(createTableSplitKeys={"1","5","9","D","H","L","P","T","X","b","f","j","n","r","v"})
	@SolrField @SolrUniqKey
	private String productMasterId;

	/**
	 * バージョンです。
	 */
	@HBaseColumn
	@SolrField
	private Integer version;

	/**
	 * ランクインバージョンです。
	 */
	@HBaseColumn
	@SolrField
	private Integer rankInVersion;

	/**
	 * ランク変動通知必須フラグです。
	 */
	@HBaseColumn
	@SolrField
	private boolean requiredNotify;

	/**
	 * 順位です。
	 */
	@HBaseColumn
	@SolrField
	private Integer rank;

	/**
	 * スコアです。
	 */
	@HBaseColumn
	@SolrField
	private double productMasterScore;

	/**
	 * レビュー投稿件数です。
	 */
	@HBaseColumn
	@SolrField
	private long reviewPostCount;

	/**
	 * レビュー閲覧件数です。
	 */
	@HBaseColumn
	@SolrField
	private long reviewShowCount;

	/**
	 * レビューいいね獲得件数です。
	 */
	@HBaseColumn
	@SolrField
	private long reviewLikeCount;

	/**
	 * Q&A回答数です。
	 */
	@HBaseColumn
	@SolrField
	private long answerPostCount;

	/**
	 * Q%A回答いいね獲得数です。
	 */
	@HBaseColumn
	@SolrField
	private long answerLikeCount;

	/**
	 * 画像投稿数です。
	 */
	@HBaseColumn
	@SolrField
	private long imagePostCount;

	/**
	 * 画像いいね獲得数です。
	 */
	@HBaseColumn
	@SolrField
	private long imageLikeCount;

	/**
	 * 商品購入日です。
	 */
	@HBaseColumn
	@SolrField
	private Date purchaseDate;

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
	 * 商品です。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo ProductDO product;

	/**
	 * コミュニティユーザーです。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo CommunityUserDO communityUser;

	/**
	 * お知らせのリストです。
	 */
	@RelatedByHBase(withoutSecondalyIndex=true)
	@RelatedBySolr
	private @HasMany List<InformationDO> informations = Lists.newArrayList();

	/**
	 * アクション履歴のリストです。
	 */
	@RelatedByHBase(withoutSecondalyIndex=true)
	@RelatedBySolr
	private @HasMany List<ActionHistoryDO> actionHistorys = Lists.newArrayList();

	/**
	 * アダルト商品かどうかです。
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
	 * データを書き込みます。
	 * @param out データ
	 * @throws IOException 入出力例外が発生した場合
	 */
	@Override
	public void write(DataOutput out) throws IOException {
		out.writeUTF(productMasterId);
		out.writeDouble(productMasterScore);
		out.writeLong(reviewPostCount);
		out.writeLong(reviewShowCount);
		out.writeLong(reviewLikeCount);
		out.writeLong(answerPostCount);
		out.writeLong(answerLikeCount);
		out.writeLong(imagePostCount);
		out.writeLong(imageLikeCount);
		out.writeUTF(product.getSku());
		out.writeUTF(communityUser.getCommunityUserId());
	}

	/**
	 * データを読み込みます。
	 * @param in データ
	 * @throws IOException 入出力例外が発生した場合
	 */
	@Override
	public void readFields(DataInput in) throws IOException {
		productMasterId = in.readUTF();
		productMasterScore = in.readDouble();
		reviewPostCount = in.readLong();
		reviewShowCount = in.readLong();
		reviewLikeCount = in.readLong();
		answerPostCount = in.readLong();
		answerLikeCount = in.readLong();
		imagePostCount = in.readLong();
		imageLikeCount = in.readLong();
		String sku = in.readUTF();
		product = new ProductDO();
		product.setSku(sku);
		String communityUserId = in.readUTF();
		communityUser = new CommunityUserDO();
		communityUser.setCommunityUserId(communityUserId);
	}

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
	 * @return version
	 */
	public Integer getVersion() {
		return version;
	}


	/**
	 * @param version セットする version
	 */
	public void setVersion(Integer version) {
		this.version = version;
	}


	/**
	 * @return rank
	 */
	public Integer getRank() {
		return rank;
	}


	/**
	 * @param rank セットする rank
	 */
	public void setRank(Integer rank) {
		this.rank = rank;
	}



	/**
	 * @return reviewPostCount
	 */
	public long getReviewPostCount() {
		return reviewPostCount;
	}


	/**
	 * @param reviewPostCount セットする reviewPostCount
	 */
	public void setReviewPostCount(long reviewPostCount) {
		this.reviewPostCount = reviewPostCount;
	}


	/**
	 * @return reviewShowCount
	 */
	public long getReviewShowCount() {
		return reviewShowCount;
	}


	/**
	 * @param reviewShowCount セットする reviewShowCount
	 */
	public void setReviewShowCount(long reviewShowCount) {
		this.reviewShowCount = reviewShowCount;
	}


	/**
	 * @return reviewLikeCount
	 */
	public long getReviewLikeCount() {
		return reviewLikeCount;
	}


	/**
	 * @param reviewLikeCount セットする reviewLikeCount
	 */
	public void setReviewLikeCount(long reviewLikeCount) {
		this.reviewLikeCount = reviewLikeCount;
	}


	/**
	 * @return answerPostCount
	 */
	public long getAnswerPostCount() {
		return answerPostCount;
	}


	/**
	 * @param answerPostCount セットする answerPostCount
	 */
	public void setAnswerPostCount(long answerPostCount) {
		this.answerPostCount = answerPostCount;
	}


	/**
	 * @return answerLikeCount
	 */
	public long getAnswerLikeCount() {
		return answerLikeCount;
	}


	/**
	 * @param answerLikeCount セットする answerLikeCount
	 */
	public void setAnswerLikeCount(long answerLikeCount) {
		this.answerLikeCount = answerLikeCount;
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
	 * @return productMasterId
	 */
	public String getProductMasterId() {
		return productMasterId;
	}


	/**
	 * @param productMasterId セットする productMasterId
	 */
	public void setProductMasterId(String productMasterId) {
		this.productMasterId = productMasterId;
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
	 * @return productMasterScore
	 */
	public double getProductMasterScore() {
		return productMasterScore;
	}


	/**
	 * @param productMasterScore セットする productMasterScore
	 */
	public void setProductMasterScore(double productMasterScore) {
		this.productMasterScore = productMasterScore;
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
	 * スコアを計算します。
	 */
	public void calcScore(ScoreFactorDO factor) {
		long reviewPostCountLimit = reviewPostCount;
		if (reviewPostCountLimit > factor.getProductMasterReviewPostCountLimit()) {
			reviewPostCountLimit = factor.getProductMasterReviewPostCountLimit();
		}
		long imagePostCountLimit = imagePostCount;
		if (imagePostCountLimit > factor.getProductMasterImagePostCountLimit()) {
			imagePostCountLimit = factor.getProductMasterImagePostCountLimit();
		}
		
		BigDecimal score = new BigDecimal(String.valueOf(reviewPostCountLimit)).multiply(
				factor.getProductMasterReviewPostCount());
		score = score.add(new BigDecimal(String.valueOf(reviewShowCount)).multiply(
				factor.getProductMasterReviewShowCount()));
		score = score.add(new BigDecimal(String.valueOf(reviewLikeCount)).multiply(
				factor.getProductMasterReviewLikeCount()));
		score = score.add(new BigDecimal(String.valueOf(answerPostCount)).multiply(
				factor.getProductMasterAnswerPostCount()));
		score = score.add(new BigDecimal(String.valueOf(answerLikeCount)).multiply(
				factor.getProductMasterAnswerLikeCount()));
		score = score.add(new BigDecimal(String.valueOf(imagePostCountLimit)).multiply(
				factor.getProductMasterImagePostCount()));
		score = score.add(new BigDecimal(String.valueOf(imageLikeCount)).multiply(
				factor.getProductMasterImageLikeCount()));
		productMasterScore = score.doubleValue();
	}

	/**
	 * @return rankInVersion
	 */
	public Integer getRankInVersion() {
		return rankInVersion;
	}

	/**
	 * @param rankInVersion セットする rankInVersion
	 */
	public void setRankInVersion(Integer rankInVersion) {
		this.rankInVersion = rankInVersion;
	}

	/**
	 * @return requiredNotify
	 */
	public boolean isRequiredNotify() {
		return requiredNotify;
	}

	/**
	 * @param requiredNotify セットする requiredNotify
	 */
	public void setRequiredNotify(boolean requiredNotify) {
		this.requiredNotify = requiredNotify;
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
	 * @return imagePostCount
	 */
	public long getImagePostCount() {
		return imagePostCount;
	}

	/**
	 * @param imagePostCount セットする imagePostCount
	 */
	public void setImagePostCount(long imagePostCount) {
		this.imagePostCount = imagePostCount;
	}

	/**
	 * @return imageLikeCount
	 */
	public long getImageLikeCount() {
		return imageLikeCount;
	}

	/**
	 * @param imageLikeCount セットする imageLikeCount
	 */
	public void setImageLikeCount(long imageLikeCount) {
		this.imageLikeCount = imageLikeCount;
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
