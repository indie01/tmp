/**
 *
 */
package com.kickmogu.yodobashi.community.resource.domain;

import java.util.List;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.resource.annotation.HasMany;
import com.kickmogu.lib.core.utils.StringUtil;
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



/**
 * 購入の決め手情報です。
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.MEDIUM)
@SolrSchema
public class DecisivePurchaseDO extends BaseWithTimestampDO {

	/**
	 *
	 */
	private static final long serialVersionUID = 5275753777856460317L;

	/**
	 * 購入の決め手IDです。
	 */
	
	//TODO 第１キーがSKU
	@HBaseKey
	@SolrField @SolrUniqKey
	private String decisivePurchaseId;

	/**
	 * SKU です。
	 */
	@HBaseColumn
	@SolrField
	private String sku;

	/**
	 * 購入の決め手名称です。
	 */
	@HBaseColumn
	@SolrField
	private String decisivePurchaseName;

	/**
	 * 評価数です。<br />
	 * 実行時に読み込みます。キャッシュ化です。
	 */
	private long ratings;

	@HBaseColumn
	@SolrField
	private boolean deleteFlg=false;

	@HBaseColumn
	@SolrField
	private boolean checkFlg=false;
	
	/**
	 * レビューの購入の決め手情報のリストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<ReviewDecisivePurchaseDO> reviewDecisivePurchases = Lists.newArrayList();

	/**
	 * @return decisivePurchaseId
	 */
	public String getDecisivePurchaseId() {
		return decisivePurchaseId;
	}

	/**
	 * @param decisivePurchaseId セットする decisivePurchaseId
	 */
	public void setDecisivePurchaseId(String decisivePurchaseId) {
		this.decisivePurchaseId = decisivePurchaseId;
	}

	/**
	 * @return decisivePurchaseName
	 */
	public String getDecisivePurchaseName() {
		return decisivePurchaseName;
	}

	/**
	 * @param decisivePurchaseName セットする decisivePurchaseName
	 */
	public void setDecisivePurchaseName(String decisivePurchaseName) {
		this.decisivePurchaseName = decisivePurchaseName;
	}

	/**
	 * @return ratings
	 */
	public long getRatings() {
		return ratings;
	}

	/**
	 * @param ratings セットする ratings
	 */
	public void setRatings(long ratings) {
		this.ratings = ratings;
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
	 * 購入の決め手名称を標準化します。
	 * 両端スペースは取り除き、カタカナは全角に、スペース英数字記号は半角に変換します。
	 */
	public void normalize() {
		if (decisivePurchaseName != null) {
			String target = decisivePurchaseName.replace("　", " ").trim();
			target = StringUtil.hankakuKatakanaToZenkakuKatakana(target);
			target = StringUtil.zenkakuAlphabetToHankakuAlphabet(target);
			target = StringUtil.zenkakuDigitToHankakuDigit(target);
			target = StringUtil.zenkakuSignToHankakuSign(target);
			decisivePurchaseName = target;
		}
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
	 * @return the deleteFlg
	 */
	public boolean isDeleteFlg() {
		return deleteFlg;
	}

	/**
	 * @param deleteFlg the deleteFlg to set
	 */
	public void setDeleteFlg(boolean deleteFlg) {
		this.deleteFlg = deleteFlg;
	}

	/**
	 * @return the checkFlg
	 */
	public boolean isCheckFlg() {
		return checkFlg;
	}

	/**
	 * @param checkFlg the checkFlg to set
	 */
	public void setCheckFlg(boolean checkFlg) {
		this.checkFlg = checkFlg;
	}
	
	
}
