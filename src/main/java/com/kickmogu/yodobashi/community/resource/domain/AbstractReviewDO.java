package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.msgpack.annotation.Message;

import com.kickmogu.lib.core.resource.domain.BaseDO;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseIndex;
import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.yodobashi.community.common.utils.DateUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.SolrUtil;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewPoint;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewPointDetail;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewPointSpecial;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewPointSpecialDetail;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewQuestPoint;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewQuestPoint.ReviewQuestPointSpecial;
import com.kickmogu.yodobashi.community.resource.domain.constants.AlsoBuyProduct;
import com.kickmogu.yodobashi.community.resource.domain.constants.CancelPointGrantType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointGrantStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointIncentiveType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointQuestType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ProductSatisfaction;
import com.kickmogu.yodobashi.community.resource.domain.constants.PurchaseHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewType;

public class AbstractReviewDO extends AbstractContentBaseDO {

	/**
	 *
	 */
	private static final long serialVersionUID = 5917795532935033903L;

	/**
	 * レビュータイプです。
	 */
	@HBaseColumn
	@SolrField
	private ReviewType reviewType;

	/**
	 * 購入日時です。
	 */
	@HBaseColumn
	@SolrField
	private Date purchaseDate;

	/**
	 * 購入日から投稿日までの経過日数です。
	 */
	@SolrField
	@HBaseColumn
	private Integer elapsedDays;

	/**
	 * 購入履歴タイプです。
	 */
	@HBaseColumn
	@SolrField
	private PurchaseHistoryType purchaseHistoryType;

	/**
	 * アダルト商品に対するレビューかどうかです。
	 */
	@HBaseColumn
	@SolrField
	private boolean adult;

	/**
	 * 迷った商品はないフラグです。
	 */
	@HBaseColumn
	@SolrField
	private boolean noLostProductFlag;

	/**
	 * 過去に使用した商品はないフラグです。
	 */
	@HBaseColumn
	@SolrField
	private boolean noUsedProductFlag;

	/**
	 * 満足度評価数です。
	 */
	@HBaseColumn
	@SolrField
	private ProductSatisfaction productSatisfaction;

	/**
	 * 次も購入しますか評価です。
	 */
	@HBaseColumn
	@SolrField
	private AlsoBuyProduct alsoBuyProduct;

	/**
	 * レビュー本文です。
	 */
	@HBaseColumn
	@SolrField(indexed=false)
	private String reviewBody;

	/**
	 * ステータス
	 */
	@HBaseColumn
	@SolrField
	private ContentsStatus status;

	/**
	 * 保存日時
	 */
	@HBaseColumn
	@SolrField
	private Date saveDate;

	/**
	 * 投稿日です。
	 */
	@HBaseColumn
	@SolrField
	private Date postDate;

	/**
	 * ポイント計算基準用の日付です。
	 */
	@HBaseColumn
	@SolrField
	private Date pointBaseDate;

	/**
	 * 削除日です。
	 */
	@HBaseColumn
	@SolrField
	private Date deleteDate;

	/**
	 * 有効フラグです。
	 */
	@HBaseColumn
	@SolrField
	private boolean effective;

	/**
	 * 旧レビューIDです。
	 * ※移行用です。
	 */
	@HBaseColumn
	@SolrField
	private String oldReviewId;

	/**
	 * メモです。
	 */
	@HBaseColumn
	@SolrField(indexed=false)
	private String memo;

	/**
	 * ポイント付与申請IDです。
	 */
	@HBaseColumn
	@SolrField
	private String pointGrantRequestId;

	/**
	 * ポイント付与申請リストです。
	 */
	@HBaseColumn
	private List<PointGrantRequestDetail> pointGrantRequestDetails;

	/**
	 * ポイント付与ステータスです。
	 */
	@HBaseColumn
	@SolrField
	private PointGrantStatus pointGrantStatus;

	/**
	 * ポイント付与キャンセル理由です。
	 */
	@HBaseColumn
	@SolrField
	private CancelPointGrantType cancelPointGrantType;

	/**
	 * 付与済みポイントです。
	 */
	@HBaseColumn
	@SolrField
	private Long grantPoint;

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
	 * 商品単位での最新レビューフラグ
	 */
	@SolrField
	@HBaseColumn
	private boolean latestReview=false;
	
	private PurchaseProductDO purchaseProduct;

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
		return withdraw || ContentsStatus.DELETE.equals(status);
	}

	/**
	 * @return reviewType
	 */
	public ReviewType getReviewType() {
		return reviewType;
	}

	/**
	 * @param reviewType セットする reviewType
	 */
	public void setReviewType(ReviewType reviewType) {
		this.reviewType = reviewType;
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
	 * @return noLostProductFlag
	 */
	public boolean isNoLostProductFlag() {
		return noLostProductFlag;
	}

	/**
	 * @param noLostProductFlag セットする noLostProductFlag
	 */
	public void setNoLostProductFlag(boolean noLostProductFlag) {
		this.noLostProductFlag = noLostProductFlag;
	}

	/**
	 * @return noUsedProductFlag
	 */
	public boolean isNoUsedProductFlag() {
		return noUsedProductFlag;
	}

	/**
	 * @param noUsedProductFlag セットする noUsedProductFlag
	 */
	public void setNoUsedProductFlag(boolean noUsedProductFlag) {
		this.noUsedProductFlag = noUsedProductFlag;
	}

	/**
	 * @return productSatisfaction
	 */
	public ProductSatisfaction getProductSatisfaction() {
		return productSatisfaction;
	}

	/**
	 * @param productSatisfaction セットする productSatisfaction
	 */
	public void setProductSatisfaction(ProductSatisfaction productSatisfaction) {
		this.productSatisfaction = productSatisfaction;
	}

	/**
	 * @return alsoBuyProduct
	 */
	public AlsoBuyProduct getAlsoBuyProduct() {
		return alsoBuyProduct;
	}

	/**
	 * @param alsoBuyProduct セットする alsoBuyProduct
	 */
	public void setAlsoBuyProduct(AlsoBuyProduct alsoBuyProduct) {
		this.alsoBuyProduct = alsoBuyProduct;
	}

	/**
	 * @return reviewBody
	 */
	public String getReviewBody() {
		return reviewBody;
	}

	/**
	 * @param reviewBody セットする reviewBody
	 */
	public void setReviewBody(String reviewBody) {
		this.reviewBody = reviewBody;
	}

	/**
	 * @return status
	 */
	public ContentsStatus getStatus() {
		return status;
	}

	/**
	 * @param status セットする status
	 */
	public void setStatus(ContentsStatus status) {
		this.status = status;
	}

	/**
	 * @return saveDate
	 */
	public Date getSaveDate() {
		return saveDate;
	}

	/**
	 * @param saveDate セットする saveDate
	 */
	public void setSaveDate(Date saveDate) {
		this.saveDate = saveDate;
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
	 * @return effective
	 */
	public boolean isEffective() {
		return effective;
	}

	/**
	 * @param effective セットする effective
	 */
	public void setEffective(boolean effective) {
		this.effective = effective;
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
	 * @return oldReviewId
	 */
	public String getOldReviewId() {
		return oldReviewId;
	}

	/**
	 * @param oldReviewId セットする oldReviewId
	 */
	public void setOldReviewId(String oldReviewId) {
		this.oldReviewId = oldReviewId;
	}

	/**
	 * @return memo
	 */
	public String getMemo() {
		return memo;
	}

	/**
	 * @param memo セットする memo
	 */
	public void setMemo(String memo) {
		this.memo = memo;
	}

	/**
	 * @return pointBaseDate
	 */
	public Date getPointBaseDate() {
		return pointBaseDate;
	}

	/**
	 * @param pointBaseDate セットする pointBaseDate
	 */
	public void setPointBaseDate(Date pointBaseDate) {
		this.pointBaseDate = pointBaseDate;
	}

	/**
	 * @return pointGrantRequestId
	 */
	public String getPointGrantRequestId() {
		return pointGrantRequestId;
	}

	/**
	 * @param pointGrantRequestId セットする pointGrantRequestId
	 */
	public void setPointGrantRequestId(String pointGrantRequestId) {
		this.pointGrantRequestId = pointGrantRequestId;
	}

	/**
	 * ポイント付与申請詳細です。
	 * @author kamiike
	 *
	 */
	@Message
	public static class PointGrantRequestDetail extends BaseDO {

		/**
		 *
		 */
		private static final long serialVersionUID = 7618916798426843842L;

		/**
		 * ポイントタイプです。
		 */
		protected PointIncentiveType type;

		/**
		 * ポイントです。
		 */
		protected Long point;

		/**
		 * レビュー特別条件コードです。
		 */
		protected String specialCondCode;

		/**
		 * レビュー履歴番号です。
		 */
		protected Integer reviewHistoryNo;

		/**
		 * ポイント付与タイプです。
		 */
		protected Integer pointGrantType;

		public PointGrantRequestDetail() {
		}

		/**
		 *
		 * @param type
		 * @param point
		 */
		public PointGrantRequestDetail(PointIncentiveType type, long point) {
			this.type = type;
			this.point = point;
		}

		/**
		 * @return type
		 */
		public PointIncentiveType getType() {
			return type;
		}

		/**
		 * @param type セットする type
		 */
		public void setType(PointIncentiveType type) {
			this.type = type;
		}

		/**
		 * @return point
		 */
		public Long getPoint() {
			return point;
		}

		/**
		 * @param point セットする point
		 */
		public void setPoint(Long point) {
			this.point = point;
		}

		/**
		 * @return reviewHistoryNo
		 */
		public Integer getReviewHistoryNo() {
			return reviewHistoryNo;
		}

		/**
		 * @param reviewHistoryNo セットする reviewHistoryNo
		 */
		public void setReviewHistoryNo(Integer reviewHistoryNo) {
			this.reviewHistoryNo = reviewHistoryNo;
		}

		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

		/**
		 * @return specialCondCode
		 */
		public String getSpecialCondCode() {
			return specialCondCode;
		}

		/**
		 * @param specialCondCode セットする specialCondCode
		 */
		public void setSpecialCondCode(String specialCondCode) {
			this.specialCondCode = specialCondCode;
		}

		public boolean isPointReplace() {
			return pointGrantType.equals(ReviewPointSpecial.POINT_TYPE_REPLACE);
		}

		public boolean isPointAdd() {
			return pointGrantType.equals(ReviewPointSpecial.POINT_TYPE_ADD);
		}

		/**
		 * @return pointGrantType
		 */
		public Integer getPointGrantType() {
			return pointGrantType;
		}

		/**
		 * @param pointGrantType セットする pointGrantType
		 */
		public void setPointGrantType(Integer pointGrantType) {
			this.pointGrantType = pointGrantType;
		}
	}

	/**
	 * @return pointGrantRequestDetails
	 */
	public List<PointGrantRequestDetail> getPointGrantRequestDetails() {
		return pointGrantRequestDetails;
	}

	/**
	 * @param pointGrantRequestDetails セットする pointGrantRequestDetails
	 */
	public void setPointGrantRequestDetails(
			List<PointGrantRequestDetail> pointGrantRequestDetails) {
		this.pointGrantRequestDetails = pointGrantRequestDetails;
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
	 * @return elapsedDays
	 */
	public Integer getElapsedDays() {
		return elapsedDays;
	}

	/**
	 * @param elapsedDays セットする elapsedDays
	 */
	public void setElapsedDays(Integer elapsedDays) {
		this.elapsedDays = elapsedDays;
	}

	public void calcElapsedDays() {
		this.elapsedDays = DateUtil.getElapsedDays(getPurchaseDate(), getPointBaseDate());
	}

	/**
	 * レビュータイプクエリーマップを返します。
	 * @param product 商品
	 * @return レビュータイプごとのクエリーのmap
	 */
	public static Map<String, String> getReviewTypeQuery(ProductDO product) {
		Map<String, String> result = new HashMap<String, String>();
		if (!product.isCanReview()) {
			return result;
		}

		for (ReviewType reviewType : ReviewType.values()) {
			result.put(reviewType.getCode(), "reviewType_s:" + SolrUtil.escape(reviewType.getCode()));
		}
		
		return result;
	}
	
	/**
	 * レビュータイプクエリーマップを返します。
	 * @param product 商品
	 * @return レビュータイプごとのクエリーのmap
	 */
	public static Map<String, String> getReviewTypeQuery() {
		Map<String, String> result = new HashMap<String, String>();
		for (ReviewType reviewType : ReviewType.values()) {
			result.put(reviewType.getCode(), "reviewType_s:" + SolrUtil.escape(reviewType.getCode()));
		}
		
		return result;
	}
	
	/**
	 * レビュー区間クエリーマップを返します。
	 * @param product 商品
	 * @return 有効なレビューの評価期間、レビュー不可の場合、-1
	 */
	public static Map<Integer, String> getReviewTermQuery(ProductDO product) {
		Map<Integer, String> result = new HashMap<Integer, String>();
		if (!product.isCanReview()) {
			return result;
		}
		int effectiveReviewTerm = 0;
		if (product.getRvwCntnPostCnt() == 0
				|| product.getRvwCntnPostTerm() == 0) {
			if (product.getRvwInitPostTerm() == 0) {
				effectiveReviewTerm = -1;
			} else {
				effectiveReviewTerm = 0;
			}
		} else {
			effectiveReviewTerm = product.getRvwCntnPostCnt();
		}

		if (effectiveReviewTerm < 0) {
			return result;
		}
		int rvwInitPostTerm = product.getRvwInitPostTerm();
		if (rvwInitPostTerm > 0) {
			result.put(0, "elapsedDays_i:[1 TO " + rvwInitPostTerm + "]");
		}
		if (product.getRvwCntnPostCnt() != 0
				&& product.getRvwCntnPostTerm() != 0) {
			for (int i = 0; i < product.getRvwCntnPostCnt(); i++) {
				String start = String.valueOf(rvwInitPostTerm
						+ (i * product.getRvwCntnPostTerm()) + 1);
				String end = String.valueOf(rvwInitPostTerm
						+ ((i + 1) * product.getRvwCntnPostTerm()));
				if (product.getRvwCntnPostCnt() == (i + 1)) {
					end = "*";
				}
				result.put(i + 1, "elapsedDays_i:[" + start + " TO " + end + "]");
			}
		}

		return result;
	}

	/**
	 * 指定したタイプのポイント情報を返します。
	 * @param type タイプ
	 * @param product 商品
	 * @param purchaseProduct 購入商品
	 * @param pointBaseDate ポイント基本情報
	 * @return ポイント情報
	 */
	public static ReviewQuestPoint getReviewQstPoint(
			PointQuestType type,
			ProductDO product,
			PurchaseProductDO purchaseProduct,
			Date pointBaseDate) {
		if (!product.isCanReview()) {
			return null;
		}
		ReviewQuestPoint point = new ReviewQuestPoint();
		if (type.isProductScope()) {
			point.setReviewQuestPointSpecial(getReviewQuestPointSpecial(
					purchaseProduct, pointBaseDate, product.getRvwSps()));
			if (point.getReviewQuestPointSpecial() == null) {
				return null;
			}
		} else {
			ReviewPoint reviewPoint = product.getReviewPoint(type.getType());
			if (reviewPoint == null) {
				return null;
			}
			point.setRvwQstCd(reviewPoint.getRvwQstCd());
			ReviewPointDetail detail = reviewPoint.getReviewPointDetail(
					DateUtil.getElapsedDays(purchaseProduct.getPurchaseDate(), pointBaseDate));
			if (detail == null) {
				return null;
			}
			point.setRvwQstBasePoint(detail.getRvwBasePt());
			if (reviewPoint.getRvwSps() != null) {
				point.setReviewQuestPointSpecial(getReviewQuestPointSpecial(
						purchaseProduct, pointBaseDate, reviewPoint.getRvwSps()));
			}
		}
		return point;
	}

	/**
	 * 指定した特別条件からポイント付与情報を取得します。
	 * @param review レビュー
	 * @param orderDate 注文日付
	 * @param rvwSps 特別ポイント条件リスト
	 * @return ポイント付与情報
	 */
	private static ReviewQuestPointSpecial getReviewQuestPointSpecial(
			PurchaseProductDO purchaseProduct,
			Date pointBaseDate,
			ReviewPointSpecial[] rvwSps) {
		if (rvwSps == null) {
			return null;
		}
		for (ReviewPointSpecial reviewPointSpecial : rvwSps) {
			Date baseDate = null;
			if (reviewPointSpecial.isPurchaseJudgeType()) {
				baseDate = purchaseProduct.getPurchaseDate();
			} else if (reviewPointSpecial.isOrderJudgeType()) {
				baseDate = purchaseProduct.getOrderDate();
			} else if (reviewPointSpecial.isPostJudgeType()) {
				baseDate = pointBaseDate;
			} else {
				continue;
			}
			if (DateUtil.matchTerm(
					reviewPointSpecial.getRvwSpSttTm(),
					reviewPointSpecial.getRvwSpEdTm(),
					baseDate)) {
				ReviewQuestPointSpecial special = new ReviewQuestPointSpecial();
				special.setPtTyp(reviewPointSpecial.getPtTyp());
				special.setRvwSpTyp(reviewPointSpecial.getRvwSpTyp());
				if (reviewPointSpecial.isFirstCondType()) {
					if (reviewPointSpecial.getRvwSpDtls() == null
							|| reviewPointSpecial.getRvwSpDtls().length == 0) {
						continue;
					}
					ReviewPointSpecialDetail[] rvwSpDtls = reviewPointSpecial.getRvwSpDtls();
					for (ReviewPointSpecialDetail rvwSpDtl : rvwSpDtls) {
						special.setRvwSpPoint(rvwSpDtl.getRvwSpDtlPt());
						special.setRvwSpDtlEdThdNum(rvwSpDtl.getRvwSpDtlEdThdNum());
						break;
					}
					if (special.getRvwSpPoint() == null) {
						continue;
					}
					return special;
				} else if (reviewPointSpecial.isTermCondType()) {
					if (reviewPointSpecial.getRvwSpDtls() == null
							|| reviewPointSpecial.getRvwSpDtls().length == 0) {
						continue;
					}
					ReviewPointSpecialDetail[] rvwSpDtls = reviewPointSpecial.getRvwSpDtls();
					for (ReviewPointSpecialDetail rvwSpDtl : rvwSpDtls) {
						if (DateUtil.matchTerm(
								rvwSpDtl.getRvwSpDtlSttThdDt(),
								rvwSpDtl.getRvwSpDtlEdThdDt(),
								baseDate)) {
							special.setRvwSpDtlSttThdDt(rvwSpDtl.getRvwSpDtlSttThdDt());
							special.setRvwSpDtlEdThdDt(rvwSpDtl.getRvwSpDtlEdThdDt());
							special.setRvwSpPoint(rvwSpDtl.getRvwSpDtlPt());
							break;
						}
					}
					if (special.getRvwSpPoint() == null) {
						continue;
					}
					return special;
				} else {
					continue;
				}
			}
		}
		return null;
	}

	/**
	 * @return pointGrantStatus
	 */
	public PointGrantStatus getPointGrantStatus() {
		return pointGrantStatus;
	}

	/**
	 * @param pointGrantStatus セットする pointGrantStatus
	 */
	public void setPointGrantStatus(PointGrantStatus pointGrantStatus) {
		this.pointGrantStatus = pointGrantStatus;
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
	 * @return cancelPointGrantType
	 */
	public CancelPointGrantType getCancelPointGrantType() {
		return cancelPointGrantType;
	}

	/**
	 * @param cancelPointGrantType セットする cancelPointGrantType
	 */
	public void setCancelPointGrantType(CancelPointGrantType cancelPointGrantType) {
		this.cancelPointGrantType = cancelPointGrantType;
	}
	
	/**
	 * 商品単位での最新レビューフラグ取得
	 * @return the latestReview
	 */
	public boolean isLatestReview() {
		return latestReview;
	}
	/**
	 * 商品単位での最新レビューフラグ設定
	 * @param latestReview the LatestReview to set
	 */
	public void setLatestReview(boolean latestReview) {
		this.latestReview = latestReview;
	}
	/**
	 * 購入商品取得
	 * データベースからは設定されない。
	 * @return 購入商品情報
	 */
	public PurchaseProductDO getPurchaseProduct() {
		return purchaseProduct;
	}
	/**
	 * 購入商品設定
	 * データベースからは設定されない。
	 * @param purchaseProduct 購入商品情報
	 */
	public void setPurchaseProduct(PurchaseProductDO purchaseProduct) {
		this.purchaseProduct = purchaseProduct;
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
