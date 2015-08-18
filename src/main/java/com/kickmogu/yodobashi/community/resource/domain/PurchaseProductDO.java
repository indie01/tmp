package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.time.DateUtils;

import com.kickmogu.lib.core.resource.annotation.BelongsTo;
import com.kickmogu.lib.core.resource.annotation.Label;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseIndex;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.lib.solr.annotation.SolrSchema;
import com.kickmogu.lib.solr.annotation.SolrUniqKey;
import com.kickmogu.yodobashi.community.common.utils.DateUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.UserUtil;
import com.kickmogu.yodobashi.community.resource.domain.constants.PurchaseHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SlipType;
import com.kickmogu.yodobashi.community.resource.domain.util.CommunityUserUtil;

/**
 * 購入商品情報です。
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},
	excludeBackup=true)
@SolrSchema
public class PurchaseProductDO extends BaseWithTimestampDO implements StoppableContents {

	/**
	 *
	 */
	private static final long serialVersionUID = 1454287216739550200L;

	/**
	 * 購入商品情報IDです。
	 */
	@HBaseKey(createTableSplitKeys={"1","5","9","D","H","L","P","T","X","b","f","j","n","r","v"})
	@SolrField @SolrUniqKey
	private String purchaseProductId;

	/**
	 * 購入日が固定化されているかどうかです。
	 */
	@HBaseColumn
	@Label("購入日が固定化フラグ")
	private boolean purchaseDateFix;

	/**
	 * 購入日です。<br />
	 * 数量が 1 以上かつ売上登録明細区分が「1：対象」のデータ（受注情報、売り上げログ）の中で、
	 * 最も古い購入日のデータ内の請求日が該当します。<br />
	 * ただしレビュー起算日として使用するため、固定化されると変更されません。
	 */
	@HBaseColumn
	@SolrField
	@Label("購入日")
	private Date purchaseDate;

	/**
	 * 購入日を決定したデータのタイプです。
	 */
	@HBaseColumn
	@SolrField
	@Label("購入日参照データタイプ")
	private SlipType purchaseDateRefDataType;

	/**
	 * 購入日を決定した受注情報、もしくは売り上げログを一意に識別するIDです。
	 */
	@HBaseColumn
	@SolrField
	@Label("購入日参照ID")
	private String purchaseDateRefId;

	/**
	 * 請求日です。<br />
	 * 数量が 1 以上かつ売上登録明細区分が「1：対象」のデータ（受注情報、売り上げログ）の中で、
	 * 最も古い購入日のデータ内の請求日が該当します。
	 */
	@HBaseColumn
	@SolrField
	@Label("請求日")
	private Date billingDate;

	/**
	 * 購入日を決定したデータのタイプです。
	 */
	@HBaseColumn
	@SolrField
	@Label("請求日参照データタイプ")
	private SlipType billingDateRefDataType;

	/**
	 * 請求日を決定した受注情報、もしくは売り上げログを一意に識別するIDです。
	 */
	@HBaseColumn
	@SolrField
	@Label("請求日参照ID")
	private String billingDateRefId;

	/**
	 * 注文日です。<br />
	 * 数量が 1 以上で、最も古い購入日のデータ（受注情報、売り上げログ）内の請求日が該当します。
	 */
	@HBaseColumn
	@SolrField
	@Label("注文日")
	private Date orderDate;

	/**
	 * 注文日を決定したデータのタイプです。
	 */
	@HBaseColumn
	@SolrField
	@Label("注文日参照データタイプ")
	private SlipType orderDateRefDataType;

	/**
	 * 注文日を決定した受注情報、もしくは売り上げログを一意に識別するIDです。
	 */
	@HBaseColumn
	@SolrField
	@Label("注文日参照ID")
	private String orderDateRefId;

	/**
	 * JANコード
	 */
	@HBaseColumn
	@SolrField
	@Label("JANコード")
	private String janCode;

	/**
	 * ユーザー申告による購入日です。
	 */
	@HBaseColumn
	@Label("ユーザー入力購入日")
	private Date userInputPurchaseDate;

	/**
	 * 購入履歴タイプです。
	 */
	@HBaseColumn
	@SolrField
	private PurchaseHistoryType purchaseHistoryType;

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
	 * 公開フラグです。
	 */
	@HBaseColumn
	@SolrField
	private boolean publicSetting;

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
	 * 緩い共有化済みかどうかです。
	 */
	@HBaseColumn
	@SolrField
	private boolean share;
	
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
	 * @return publicSetting
	 */
	public boolean isPublicSetting() {
		return publicSetting;
	}

	/**
	 * @param publicSetting セットする publicSetting
	 */
	public void setPublicSetting(boolean publicSetting) {
		this.publicSetting = publicSetting;
	}

	/**
	 * @return purchaseProductId
	 */
	public String getPurchaseProductId() {
		return purchaseProductId;
	}

	/**
	 * @param purchaseProductId セットする purchaseProductId
	 */
	public void setPurchaseProductId(String purchaseProductId) {
		this.purchaseProductId = purchaseProductId;
	}

	/**
	 * 経過日数を算出して返します。
	 * @return 経過日数
	 */
	public int getElapsedDays() {
		return DateUtil.getElapsedDays(purchaseDate);
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
	 * @return purchaseDateRefId
	 */
	public String getPurchaseDateRefId() {
		return purchaseDateRefId;
	}

	/**
	 * @param purchaseDateRefId セットする purchaseDateRefId
	 */
	public void setPurchaseDateRefId(String purchaseDateRefId) {
		this.purchaseDateRefId = purchaseDateRefId;
	}

	/**
	 * @return billingDate
	 */
	public Date getBillingDate() {
		return billingDate;
	}

	/**
	 * @param billingDate セットする billingDate
	 */
	public void setBillingDate(Date billingDate) {
		this.billingDate = billingDate;
	}

	/**
	 * @return billingDateRefId
	 */
	public String getBillingDateRefId() {
		return billingDateRefId;
	}

	/**
	 * @param billingDateRefId セットする billingDateRefId
	 */
	public void setBillingDateRefId(String billingDateRefId) {
		this.billingDateRefId = billingDateRefId;
	}

	/**
	 * @return janCode
	 */
	public String getJanCode() {
		return janCode;
	}

	/**
	 * @param janCode セットする janCode
	 */
	public void setJanCode(String janCode) {
		this.janCode = janCode;
	}

	/**
	 * @return purchaseDateRefDataType
	 */
	public SlipType getPurchaseDateRefDataType() {
		return purchaseDateRefDataType;
	}

	/**
	 * @param purchaseDateRefDataType セットする purchaseDateRefDataType
	 */
	public void setPurchaseDateRefDataType(SlipType purchaseDateRefDataType) {
		this.purchaseDateRefDataType = purchaseDateRefDataType;
	}

	/**
	 * @return billingDateRefDataType
	 */
	public SlipType getBillingDateRefDataType() {
		return billingDateRefDataType;
	}

	/**
	 * @param billingDateRefDataType セットする billingDateRefDataType
	 */
	public void setBillingDateRefDataType(SlipType billingDateRefDataType) {
		this.billingDateRefDataType = billingDateRefDataType;
	}

	/**
	 * @return purchaseDateFix
	 */
	public boolean isPurchaseDateFix() {
		return purchaseDateFix;
	}

	/**
	 * @param purchaseDateFix セットする purchaseDateFix
	 */
	public void setPurchaseDateFix(boolean purchaseDateFix) {
		this.purchaseDateFix = purchaseDateFix;
	}

	/**
	 * @return orderDate
	 */
	public Date getOrderDate() {
		return orderDate;
	}

	/**
	 * @param orderDate セットする orderDate
	 */
	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	/**
	 * @return orderDateRefDataType
	 */
	public SlipType getOrderDateRefDataType() {
		return orderDateRefDataType;
	}

	/**
	 * @param orderDateRefDataType セットする orderDateRefDataType
	 */
	public void setOrderDateRefDataType(SlipType orderDateRefDataType) {
		this.orderDateRefDataType = orderDateRefDataType;
	}

	/**
	 * @return orderDateRefId
	 */
	public String getOrderDateRefId() {
		return orderDateRefId;
	}

	/**
	 * @param orderDateRefId セットする orderDateRefId
	 */
	public void setOrderDateRefId(String orderDateRefId) {
		this.orderDateRefId = orderDateRefId;
	}

	/**
	 * @return userInputPurchaseDate
	 */
	public Date getUserInputPurchaseDate() {
		return userInputPurchaseDate;
	}

	/**
	 * @param userInputPurchaseDate セットする userInputPurchaseDate
	 */
	public void setUserInputPurchaseDate(Date userInputPurchaseDate) {
		this.userInputPurchaseDate = userInputPurchaseDate;
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
	 * 次のレビュー期限日を返します。
	 * @return レビュー期限日
	 */
	public Date getNextLimitDate() {
		int nextGrantPointReviewLimit = product.getNextGrantPointReviewLimit(purchaseDate);
		return DateUtils.addDays(new Date(), nextGrantPointReviewLimit);
	}
	
	/**
	 * @return share
	 */
	public boolean isShare() {
		return share;
	}

	/**
	 * @param share セットする share
	 */
	public void setShare(boolean share) {
		this.share = share;
	}

	
	public boolean isDeleted() {
		return withdraw;
	}
	
	public String toShortString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
		.append("purchaseProductId", purchaseProductId)
		.append("purchaseDateFix", purchaseDateFix)
		.append("purchaseDate", purchaseDate)
		.append("purchaseDateRefDataType", purchaseDateRefDataType)
		.append("purchaseDateRefId", purchaseDateRefId)
		.append("billingDate", billingDate)
		.append("billingDateRefDataType", billingDateRefDataType)
		.append("billingDateRefId", billingDateRefId)
		.append("orderDate", orderDate)
		.append("orderDateRefDataType", orderDateRefDataType)
		.append("orderDateRefId", orderDateRefId)
		.append("janCode", janCode)
		.append("userInputPurchaseDate", userInputPurchaseDate)
		.append("purchaseHistoryType", purchaseHistoryType)
		.append("product.sku", product != null ? product.getSku() : null)
		.append("communityUser.communityUserId", communityUser != null ? communityUser.getCommunityUserId() : null)
		.append("publicSetting", publicSetting)
		.append("adult", adult)
		.append("withdraw", withdraw)
		.append("withdrawKey", withdrawKey)
		.append("share", share)
		.toString();
	}
 }
