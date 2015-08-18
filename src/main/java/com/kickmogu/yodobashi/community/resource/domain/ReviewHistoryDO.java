package com.kickmogu.yodobashi.community.resource.domain;

import java.util.List;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.resource.annotation.BelongsTo;
import com.kickmogu.lib.core.resource.annotation.HasMany;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.RelatedByHBase;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.lib.solr.annotation.RelatedBySolr;
import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.lib.solr.annotation.SolrSchema;
import com.kickmogu.lib.solr.annotation.SolrUniqKey;


/**
 * レビュー履歴です。<br />
 * 現状インデックス更新を実装していません。
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.LARGE)
@SolrSchema
public class ReviewHistoryDO extends AbstractReviewDO {

	/**
	 *
	 */
	private static final long serialVersionUID = 1454287216739550200L;

	/**
	 * レビュー履歴IDです。
	 */
	@HBaseKey(createTableSplitKeys={"1","5","9","D","H","L","P","T","X","b","f","j","n","r","v"})
	@SolrField @SolrUniqKey
	private String reviewHistoryId;

	/**
	 * レビューIDです。
	 */
	@HBaseColumn
	@SolrField
	private String reviewId;

	/**
	 * コミュニティユーザーです。
	 */
	@HBaseColumn
	@SolrField
	private @BelongsTo CommunityUserDO communityUser;

	/**
	 * レビュー対象となる商品です。
	 */
	@HBaseColumn
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
	 * @return reviewHistoryId
	 */
	public String getReviewHistoryId() {
		return reviewHistoryId;
	}

	/**
	 * @param reviewHistoryId セットする reviewHistoryId
	 */
	public void setReviewHistoryId(String reviewHistoryId) {
		this.reviewHistoryId = reviewHistoryId;
	}

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
}
