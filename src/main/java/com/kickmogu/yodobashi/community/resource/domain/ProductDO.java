package com.kickmogu.yodobashi.community.resource.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.resource.annotation.ExternalEntity;
import com.kickmogu.lib.core.resource.annotation.HasMany;
import com.kickmogu.lib.core.resource.domain.BaseDO;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.lib.hadoop.hbase.annotation.RelatedByHBase;
import com.kickmogu.lib.solr.annotation.RelatedBySolr;
import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.lib.solr.annotation.SolrSchema;
import com.kickmogu.lib.solr.annotation.SolrUniqKey;
import com.kickmogu.yodobashi.community.common.utils.DateUtil;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointIncentiveType;

/**
 * 商品情報です。<br />
 * API v0.22 対応。
 * @author kamiike
 *
 */
@ExternalEntity(operationsBeanName="productDao")
@HBaseTable(columnFamilies={
		@HBaseColumnFamily(name="cf")
	},sizeGroup=SizeGroup.LARGE)
@SolrSchema
public class ProductDO extends BaseWithTimestampDO{

	/**
	 *
	 */
	private static final long serialVersionUID = -4971231752586440028L;

	/**
	 * 商品コード
	 */
	// TODO SKUのキー分割
	@HBaseKey
	@SolrField @SolrUniqKey
	private String sku;

	/**
	 * 商品名
	 */
	private String productName;

	/**
	 * 商品概要
	 */
	private String productDescription;

	/**
	 * アダルト商品かどうかです。
	 */
	private boolean adult;

	/**
	 * CERO対象商品かどうかです。
	 */
	private boolean cero;

	/**
	 * お知らせのリストです。
	 */
	@RelatedByHBase(withoutSecondalyIndex=true)
	@RelatedBySolr
	private @HasMany List<InformationDO> informations = Lists.newArrayList();

	/**
	 * 質問のリストです。
	 */
	@RelatedByHBase(withoutSecondalyIndex=true)
	@RelatedBySolr
	private @HasMany List<QuestionDO> questions = Lists.newArrayList();

	/**
	 * 画像のリストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<ImageHeaderDO> imageHeaders = Lists.newArrayList();

	/**
	 * レビューのリストです。
	 */
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<ReviewDO> reviews = Lists.newArrayList();

	/**
	 * レビュー履歴のリストです。
	 */
	@RelatedByHBase(withoutSecondalyIndex=true)
	@RelatedBySolr
	private @HasMany List<ReviewHistoryDO> reviewHistorys = Lists.newArrayList();

	/**
	 * アクション履歴のリストです。
	 */
	@RelatedByHBase(withoutSecondalyIndex=true)
	@RelatedBySolr
	private @HasMany List<ActionHistoryDO> actionHistorys = Lists.newArrayList();

	/**
	 * プロダクトフォローのリストです。
	 */
	@RelatedByHBase(withoutSecondalyIndex=true)
	@RelatedBySolr
	private @HasMany List<ProductFollowDO> productFollows = Lists.newArrayList();

	/**
	 * プロダクトマスターのリストです。
	 */
	@RelatedByHBase(withoutSecondalyIndex=true)
	@RelatedBySolr
	private @HasMany List<ProductMasterDO> productMasters = Lists.newArrayList();

	/**
	 * 購入に迷った商品のリストです。
	 */
	@RelatedByHBase(withoutSecondalyIndex=true)
	@RelatedBySolr
	private @HasMany List<PurchaseLostProductDO> purchaseLostProducts = Lists.newArrayList();

	/**
	 * 購入商品のリストです。
	 */
	@RelatedByHBase(withoutSecondalyIndex=true)
	@RelatedBySolr
	private @HasMany List<PurchaseProductDO> purchaseProducts = Lists.newArrayList();

	/**
	 * 質問回答のリストです。
	 */
	@RelatedByHBase(withoutSecondalyIndex=true)
	@RelatedBySolr
	private @HasMany List<QuestionAnswerDO> questionAnswers = Lists.newArrayList();

	/**
	 * 過去に使用した商品のリストです。
	 */
	@RelatedByHBase(withoutSecondalyIndex=true)
	@RelatedBySolr
	private @HasMany List<UsedProductDO> usedProducts = Lists.newArrayList();

	/**
	 * JANコードです。
	 */
	private String jan;

	/**
	 * 短縮商品名です。
	 */
	private String stPrdNm;

	/**
	 * ブランド名です。
	 */
	private String brndNm;

	/**
	 * メーカーコードです。
	 */
	private String mkrCd;

	/**
	 * メーカー名です。
	 */
	private String mkrNm;

	/**
	 * 掲載フラグ
	 */
	private boolean publFlg=false;
	
	
	/**
	 * 商品URLです。
	 */
	private String prdUrl;

	/**
	 * 商品画像(メイン)です。
	 */
	private ProductImage[] mainImgs;

	/**
	 * 商品画像(一覧)です。
	 */
	private ProductImage[] listImgs;

	/**
	 * 商品画像(一覧)です。
	 */
	private ProductImage[] middleImgs;
	
	/**
	 * カテゴリNoです。
	 */
	private String cn;

	/**
	 * 商品カテゴリリストです。
	 */
	private ProductCategory[] cts;

	/**
	 * レビュー可否フラグです。
	 */
	private Boolean rvwPtFlg;

	/**
	 * レビューコメントです。
	 */
	private String rvwCmnt;

	/**
	 * レビューポイント算定タイプです。
	 */
	private Integer rvwPtCalcTyp;

	/**
	 * 初期投稿期間です。
	 */
	private Integer rvwInitPostTerm;

	/**
	 * 継続投稿回数です。
	 */
	private Integer rvwCntnPostCnt;

	/**
	 * 継続投稿期間です。
	 */
	private Integer rvwCntnPostTerm;

	/**
	 * レビューポイント情報有効開始日時です。
	 */
	private Date rvwPtSttTm;

	/**
	 * レビューポイント情報有効終了日時です。
	 */
	private Date rvwPtEdTm;

	/**
	 * レビューポイント情報最終更新日時です。
	 */
	private Date rvwPtLstUpd;

	/**
	 * レビュー設問情報リストです。
	 */
	private ReviewPoint[] rvwQsts;

	/**
	 * レビュー特別条件情報リストです。
	 */
	private ReviewPointSpecial[] rvwSps;
	
	/**
	 * ショッピングカートタグ（全部）
	 */
	private String cartTag;
	
	/**
	 * のれん用在庫表記タグ
	 */
	private String norenStock;

	/**
	 * のれん用カートタグ（一部）
	 */
	private String norenCartTag;
	
	public void setCeroKind(String ceroKind) {
		if (StringUtils.isNotEmpty(ceroKind) && ceroKind.equals("01")) {
			cero = true;
		} else {
			cero = false;
		}
	}

	public void setAdultKind(String adultKind) {
		if (StringUtils.isNotEmpty(adultKind) && adultKind.equals("01")) {
			adult = true;
		} else {
			adult = false;
		}
	}

	/**
	 * レビュー可能かどうかです。
	 * @return レビュー可能な場合、true
	 */
	public boolean isCanReview() {
		// 商品の掲載判定
		if (!publFlg) {
			return false;
		}
		
		// レビュー可否フラグ判定
		if (rvwPtFlg == null) {
			return false;
		}
		// レビュー有効期間判定
		Date now = new Date();
		if (rvwPtSttTm.compareTo(now) > 0) {
			return false;
		}

		return rvwPtFlg;
	}

	/**
	 * 指定したタイプのポイント情報を返します。
	 * @param type タイプ
	 * @return ポイント情報
	 */
	public ReviewPoint getReviewPoint(
			PointIncentiveType type) {
		if (rvwQsts != null) {
			for (ReviewPoint point : rvwQsts) {
				if (type.getCode().equals(point.getRvwQstCd()) &&
						DateUtil.matchTerm(point.getRvwQstSttTm(),point.getRvwQstEdTm(), new Date())) {
					return point;
				}
			}
		}
		return null;
	}

	/**
	 * @return productImageUrl
	 */
	public String getProductImageUrl() {
		if (mainImgs != null && mainImgs.length > 0) {
			return mainImgs[0].getUrl();
		} else {
			return null;
		}
	}

	/**
	 * @return listImageUrl
	 */
	public String getListImageUrl() {
		if (listImgs != null && listImgs.length > 0) {
			return listImgs[0].getUrl();
		} else {
			return null;
		}
	}

	/**
	 * @return MiddleImageUrl
	 */
	public String getMiddleImageUrl() {
		if (middleImgs != null && middleImgs.length > 0) {
			return middleImgs[0].getUrl();
		} else {
			return null;
		}
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
	 * @return productName
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * @param productName セットする productName
	 */
	public void setProductName(String productName) {
		this.productName = productName;
	}

	/**
	 * @return productDescription
	 */
	public String getProductDescription() {
		return productDescription;
	}

	/**
	 * @param productDescription セットする productDescription
	 */
	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
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
	 * @return cero
	 */
	public boolean isCero() {
		return cero;
	}

	/**
	 * @param cero セットする cero
	 */
	public void setCero(boolean cero) {
		this.cero = cero;
	}

	public void setPrdNm(String prdNm) {
		this.productName = prdNm;
	}

	public void setPrdSmry(String prdSmry) {
		this.productDescription = prdSmry;
	}

	/**
	 * @return the sPrdNm
	 */
	public String getStPrdNm() {
		return stPrdNm;
	}

	/**
	 * @param sPrdNm the sPrdNm to set
	 */
	public void setStPrdNm(String stPrdNm) {
		this.stPrdNm = stPrdNm;
	}

	/**
	 * @return brndNm
	 */
	public String getBrndNm() {
		return brndNm;
	}

	/**
	 * @param brndNm セットする brndNm
	 */
	public void setBrndNm(String brndNm) {
		if( StringUtils.isEmpty(StringUtils.trim(brndNm)))
			this.brndNm = null;
		else
			this.brndNm = brndNm;
	}

	/**
	 * @return mkrCd
	 */
	public String getMkrCd() {
		return mkrCd;
	}

	/**
	 * @param mkrCd セットする mkrCd
	 */
	public void setMkrCd(String mkrCd) {
		this.mkrCd = mkrCd;
	}

	/**
	 * @return mkrNm
	 */
	public String getMkrNm() {
		return mkrNm;
	}

	/**
	 * @param mkrNm セットする mkrNm
	 */
	public void setMkrNm(String mkrNm) {
		this.mkrNm = mkrNm;
	}

//	/**
//	 * @return slsRlsDt
//	 */
//	public String getSlsRlsDt() {
//		return slsRlsDt;
//	}
//
//	/**
//	 * @param slsRlsDt セットする slsRlsDt
//	 */
//	public void setSlsRlsDt(String slsRlsDt) {
//		this.slsRlsDt = slsRlsDt;
//	}
//
//	/**
//	 * @return slsPriceNum
//	 */
//	public Long getSlsPriceNum() {
//		return slsPriceNum;
//	}
//
//	/**
//	 * @param slsPriceNum セットする slsPriceNum
//	 */
//	public void setSlsPriceNum(Long slsPriceNum) {
//		this.slsPriceNum = slsPriceNum;
//	}
//
//	/**
//	 * @return dscRate
//	 */
//	public Integer getDscRate() {
//		return dscRate;
//	}
//
//	/**
//	 * @param dscRate セットする dscRate
//	 */
//	public void setDscRate(Integer dscRate) {
//		this.dscRate = dscRate;
//	}
//
//	/**
//	 * @return pt
//	 */
//	public Integer getPt() {
//		return pt;
//	}
//
//	/**
//	 * @param pt セットする pt
//	 */
//	public void setPt(Integer pt) {
//		this.pt = pt;
//	}
//
//	/**
//	 * @return ptRate
//	 */
//	public Integer getPtRate() {
//		return ptRate;
//	}
//
//	/**
//	 * @param ptRate セットする ptRate
//	 */
//	public void setPtRate(Integer ptRate) {
//		this.ptRate = ptRate;
//	}
//
//	/**
//	 * @return the prdRetFlg
//	 */
//	public boolean isPrdRetFlg() {
//		return prdRetFlg;
//	}
//
//	/**
//	 * @param prdRetFlg the prdRetFlg to set
//	 */
//	public void setPrdRetFlg(boolean prdRetFlg) {
//		this.prdRetFlg = prdRetFlg;
//	}
//
//	/**
//	 * @return the pkgFlg
//	 */
//	public boolean isPkgFlg() {
//		return pkgFlg;
//	}
//
//	/**
//	 * @param pkgFlg the pkgFlg to set
//	 */
//	public void setPkgFlg(boolean pkgFlg) {
//		this.pkgFlg = pkgFlg;
//	}
//
//	/**
//	 * @return the stkDspFlg
//	 */
//	public boolean isStkDspFlg() {
//		return stkDspFlg;
//	}
//
//	/**
//	 * @param stkDspFlg the stkDspFlg to set
//	 */
//	public void setStkDspFlg(boolean stkDspFlg) {
//		this.stkDspFlg = stkDspFlg;
//	}
//
//	/**
//	 * @return the stkMsg
//	 */
//	public String getStkMsg() {
//		return stkMsg;
//	}
//
//	/**
//	 * @param stkMsg the stkMsg to set
//	 */
//	public void setStkMsg(String stkMsg) {
//		this.stkMsg = stkMsg;
//	}
//
//	/**
//	 * @return scdlMsg
//	 */
//	public String getScdlMsg() {
//		return scdlMsg;
//	}
//
//	/**
//	 * @param scdlMsg セットする scdlMsg
//	 */
//	public void setScdlMsg(String scdlMsg) {
//		this.scdlMsg = scdlMsg;
//	}
//
//	/**
//	 * @return the dlvryMsg
//	 */
//	public String getDlvryMsg() {
//		return dlvryMsg;
//	}
//
//	/**
//	 * @param dlvryMsg the dlvryMsg to set
//	 */
//	public void setDlvryMsg(String dlvryMsg) {
//		this.dlvryMsg = dlvryMsg;
//	}

	/**
	 * @return prdUrl
	 */
	public String getPrdUrl() {
		return prdUrl;
	}

	/**
	 * @param prdUrl セットする prdUrl
	 */
	public void setPrdUrl(String prdUrl) {
		this.prdUrl = prdUrl;
	}

//	/**
//	 * @return prdNote
//	 */
//	public String getPrdNote() {
//		return prdNote;
//	}
//
//	/**
//	 * @param prdNote セットする prdNote
//	 */
//	public void setPrdNote(String prdNote) {
//		this.prdNote = prdNote;
//	}
//
//	/**
//	 * @return cartImgTag
//	 */
//	public String getCartImgTag() {
//		return cartImgTag;
//	}
//
//	/**
//	 * @param cartImgTag セットする cartImgTag
//	 */
//	public void setCartImgTag(String cartImgTag) {
//		this.cartImgTag = cartImgTag;
//	}
//
//	/**
//	 * @return taxation
//	 */
//	public String getTaxation() {
//		return taxation;
//	}
//
//	/**
//	 * @param taxation セットする taxation
//	 */
//	public void setTaxation(String taxation) {
//		this.taxation = taxation;
//	}
//
//	/**
//	 * @return jmdInfo
//	 */
//	public JmdInfo[] getJmdInfos() {
//		return jmdInfos;
//	}
//
//	/**
//	 * @param jmdInfo セットする jmdInfo
//	 */
//	public void setJmdInfos(JmdInfo[] jmdInfos) {
//		this.jmdInfos = jmdInfos;
//	}
//
	/**
	 * @return mainImgs
	 */
	public ProductImage[] getMainImgs() {
		return mainImgs;
	}

	/**
	 * @param mainImgs セットする mainImgs
	 */
	public void setMainImgs(ProductImage[] mainImgs) {
		this.mainImgs = mainImgs;
	}

//	/**
//	 * @return largeImgs
//	 */
//	public ProductImage[] getLargeImgs() {
//		return largeImgs;
//	}
//
//	/**
//	 * @param largeImgs セットする largeImgs
//	 */
//	public void setLargeImgs(ProductImage[] largeImgs) {
//		this.largeImgs = largeImgs;
//	}

	/**
	 * @return listImgs
	 */
	public ProductImage[] getListImgs() {
		return listImgs;
	}

	/**
	 * @param listImgs セットする listImgs
	 */
	public void setListImgs(ProductImage[] listImgs) {
		this.listImgs = listImgs;
	}

	/**
	 * @return middleImgs
	 */
	public ProductImage[] getMiddleImgs() {
		return middleImgs;
	}

	/**
	 * @param middleImgs セットする middleImgs
	 */
	public void setMiddleImgs(ProductImage[] middleImgs) {
		this.middleImgs = middleImgs;
	}
	
	/**
	 * @return jan
	 */
	public String getJan() {
		return jan;
	}

	/**
	 * @param jan セットする jan
	 */
	public void setJan(String jan) {
		this.jan = jan;
	}

	/**
	 * @return cts
	 */
	public ProductCategory[] getCts() {
		return cts;
	}

	/**
	 * @param cts セットする cts
	 */
	public void setCts(ProductCategory[] cts) {
		this.cts = cts;
	}

	/**
	 * @return the cn
	 */
	public String getCn() {
		return cn;
	}

	/**
	 * @param cn the cn to set
	 */
	public void setCn(String cn) {
		this.cn = cn;
	}

//	/**
//	 * @return medias
//	 */
//	public MediaFormat[] getMedias() {
//		return medias;
//	}
//
//	/**
//	 * @param medias セットする medias
//	 */
//	public void setMedias(MediaFormat[] medias) {
//		this.medias = medias;
//	}
//
//	/**
//	 * @return the dlFlg
//	 */
//	public boolean isDlFlg() {
//		return dlFlg;
//	}
//
//	/**
//	 * @param dlFlg the dlFlg to set
//	 */
//	public void setDlFlg(boolean dlFlg) {
//		this.dlFlg = dlFlg;
//	}
//
//	/**
//	 * @return the setPrds
//	 */
//	public SetProduct[] getSetPrds() {
//		return setPrds;
//	}
//
//	/**
//	 * @param setPrds the setPrds to set
//	 */
//	public void setSetPrds(SetProduct[] setPrds) {
//		this.setPrds = setPrds;
//	}

//	/**
//	 * @return the ecByFlg
//	 */
//	public boolean isEcByFlg() {
//		return ecByFlg;
//	}
//
//	/**
//	 * @param ecByFlg the ecByFlg to set
//	 */
//	public void setEcByFlg(boolean ecByFlg) {
//		this.ecByFlg = ecByFlg;
//	}
//
//	/**
//	 * @return the prPtDspFlg
//	 */
//	public boolean isPrPtDspFlg() {
//		return prPtDspFlg;
//	}
//
//	/**
//	 * @param prPtDspFlg the prPtDspFlg to set
//	 */
//	public void setPrPtDspFlg(boolean prPtDspFlg) {
//		this.prPtDspFlg = prPtDspFlg;
//	}

//	/**
//	 * @return the fixPriceMsg
//	 */
//	public String getFixPriceMsg() {
//		return fixPriceMsg;
//	}
//
//	/**
//	 * @param fixPriceMsg the fixPriceMsg to set
//	 */
//	public void setFixPriceMsg(String fixPriceMsg) {
//		this.fixPriceMsg = fixPriceMsg;
//	}
//
//	/**
//	 * @return the byLmt
//	 */
//	public int getByLmt() {
//		return byLmt;
//	}
//
//	/**
//	 * @param byLmt the byLmt to set
//	 */
//	public void setByLmt(int byLmt) {
//		this.byLmt = byLmt;
//	}
//
//	/**
//	 * @return the byLmtUnt
//	 */
//	public String getByLmtUnt() {
//		return byLmtUnt;
//	}
//
//	/**
//	 * @param byLmtUnt the byLmtUnt to set
//	 */
//	public void setByLmtUnt(String byLmtUnt) {
//		this.byLmtUnt = byLmtUnt;
//	}
//
//	/**
//	 * @return the stkLmtImgs
//	 */
//	public SaleLimit[] getStkLmtImgs() {
//		return stkLmtImgs;
//	}
//
//	/**
//	 * @param stkLmtImgs the stkLmtImgs to set
//	 */
//	public void setStkLmtImgs(SaleLimit[] stkLmtImgs) {
//		this.stkLmtImgs = stkLmtImgs;
//	}
//
//	/**
//	 * @return the cartNote
//	 */
//	public String getCartNote() {
//		return cartNote;
//	}
//
//	/**
//	 * @param cartNote the cartNote to set
//	 */
//	public void setCartNote(String cartNote) {
//		this.cartNote = cartNote;
//	}
//
//	/**
//	 * @return the setDscPrice
//	 */
//	public String getSetDscPrice() {
//		return setDscPrice;
//	}
//
//	/**
//	 * @param setDscPrice the setDscPrice to set
//	 */
//	public void setSetDscPrice(String setDscPrice) {
//		this.setDscPrice = setDscPrice;
//	}
//
//	/**
//	 * @return the qtyInFlg
//	 */
//	public boolean isQtyInFlg() {
//		return qtyInFlg;
//	}
//
//	/**
//	 * @param qtyInFlg the qtyInFlg to set
//	 */
//	public void setQtyInFlg(boolean qtyInFlg) {
//		this.qtyInFlg = qtyInFlg;
//	}

//	/**
//	 * @return the slsRst
//	 */
//	public SalesResult[] getSlsRst() {
//		return slsRst;
//	}
//
//	/**
//	 * @param slsRst the slsRst to set
//	 */
//	public void setSlsRst(SalesResult[] slsRst) {
//		this.slsRst = slsRst;
//	}

	/**
	 * @return rvwPtFlg
	 */
	public Boolean getRvwPtFlg() {
		return rvwPtFlg;
	}

	/**
	 * @param rvwPtFlg セットする rvwPtFlg
	 */
	public void setRvwPtFlg(Boolean rvwPtFlg) {
		this.rvwPtFlg = rvwPtFlg;
	}

	/**
	 * @return rvwPtSttTm
	 */
	public Date getRvwPtSttTm() {
		return rvwPtSttTm;
	}

	/**
	 * @param rvwPtSttTm セットする rvwPtSttTm
	 */
	public void setRvwPtSttTm(String rvwPtSttTm) {
		this.rvwPtSttTm = toDate(rvwPtSttTm);
	}

	/**
	 * @return rvwPtEdTm
	 */
	public Date getRvwPtEdTm() {
		return rvwPtEdTm;
	}

	/**
	 * @param rvwPtEdTm セットする rvwPtEdTm
	 */
	public void setRvwPtEdTm(String rvwPtEdTm) {
		this.rvwPtEdTm = toAfterOneSecond(toDate(rvwPtEdTm));
	}

	/**
	 * @return rvwPtLstUpd
	 */
	public Date getRvwPtLstUpd() {
		return rvwPtLstUpd;
	}

	/**
	 * @param rvwPtLstUpd セットする rvwPtLstUpd
	 */
	public void setRvwPtLstUpd(String rvwPtLstUpd) {
		this.rvwPtLstUpd = toDate(rvwPtLstUpd);
	}

	/**
	 * @return rvwQsts
	 */
	public ReviewPoint[] getRvwQsts() {
		return rvwQsts;
	}

	/**
	 * @param rvwQsts セットする rvwQsts
	 */
	public void setRvwQsts(ReviewPoint[] rvwQsts) {
		this.rvwQsts = rvwQsts;
	}

	/**
	 * メディアフォーマット情報です。
	 * @author kamiike
	 */
	public static class MediaFormat extends BaseDO {

		/**
		 *
		 */
		private static final long serialVersionUID = -8957113123308118301L;

		/**
		 * 表示順です。
		 */
		private int order;

		/**
		 * 画像URLです。
		 */
		private String url;

		/**
		 * @return order
		 */
		public int getOrder() {
			return order;
		}

		/**
		 * @param order セットする order
		 */
		public void setOrder(int order) {
			this.order = order;
		}

		/**
		 * @return url
		 */
		public String getUrl() {
			return url;
		}

		/**
		 * @param url セットする url
		 */
		public void setUrl(String url) {
			this.url = url;
		}
	}

	/**
	 * JMDアーティスト情報です。
	 * @author kamiike
	 */
	public static class JmdInfo extends BaseDO {

		/**
		 *
		 */
		private static final long serialVersionUID = -9153608527962226070L;

		/**
		 * 代表アーティスト名です。
		 */
		private String artistJp;

		/**
		 * @return artistJp
		 */
		public String getArtistJp() {
			return artistJp;
		}

		/**
		 * @param artistJp セットする artistJp
		 */
		public void setArtistJp(String artistJp) {
			this.artistJp = artistJp;
		}
	}

	/**
	 * 商品画像です。
	 * @author kamiike
	 *
	 */
	public static class ProductImage extends BaseDO {

		/**
		 *
		 */
		private static final long serialVersionUID = 1136663077009680445L;

		/**
		 * 表示順です。
		 */
		private int order;

		/**
		 * 画像URLです。
		 */
		private String url;

		/**
		 * @return order
		 */
		public int getOrder() {
			return order;
		}

		/**
		 * @param order セットする order
		 */
		public void setOrder(int order) {
			this.order = order;
		}

		/**
		 * @return url
		 */
		public String getUrl() {
			return url;
		}

		/**
		 * @param url セットする url
		 */
		public void setUrl(String url) {
			this.url = url;
		}
	}

	/**
	 * 商品カテゴリです。
	 * @author kamiike
	 *
	 */
	public static class ProductCategory extends BaseDO {

		/**
		 *
		 */
		private static final long serialVersionUID = -6941668412762721269L;

		/**
		 * 表示順です。
		 */
		private int order;

		/**
		 * カテゴリ名です。
		 */
		private String nm;

		/**
		 * URLです。
		 */
		private String url;
		
		/**
		 * カテゴリツリーNo
		 */
		private String cn;
		/**
		 * カテゴリコード
		 */
		private String cc;
		/**
		 * @return order
		 */
		public int getOrder() {
			return order;
		}

		/**
		 * @param order セットする order
		 */
		public void setOrder(int order) {
			this.order = order;
		}

		/**
		 * @return nm
		 */
		public String getNm() {
			return nm;
		}

		/**
		 * @param nm セットする nm
		 */
		public void setNm(String nm) {
			this.nm = nm;
		}

		/**
		 * @return url
		 */
		public String getUrl() {
			return url;
		}

		/**
		 * @param url セットする url
		 */
		public void setUrl(String url) {
			this.url = url;
		}

		public String getCn() {
			return cn;
		}

		public void setCn(String cn) {
			this.cn = cn;
		}

		public String getCc() {
			return cc;
		}

		public void setCc(String cc) {
			this.cc = cc;
		}
		
	}

	/**
	 * セット商品情報です。
	 * @author kamiike
	 *
	 */
	public static class SetProduct extends BaseDO {
		/**
		 *
		 */
		private static final long serialVersionUID = 923108615925819202L;

		/**
		 * 表示順序です
		 */
		private int order;

		/**
		 * 商品名です
		 */
		private String nm;

		/**
		 * 商品URL
		 */
		private String url;

		/**
		 * 単品価格
		 */
		private String slsPrice;

		/**
		 * 商品点数
		 */
		private int cnt;

		/**
		 * @return the order
		 */
		public int getOrder() {
			return order;
		}

		/**
		 * @param order the order to set
		 */
		public void setOrder(int order) {
			this.order = order;
		}

		/**
		 * @return the nm
		 */
		public String getNm() {
			return nm;
		}

		/**
		 * @param nm the nm to set
		 */
		public void setNm(String nm) {
			this.nm = nm;
		}

		/**
		 * @return the url
		 */
		public String getUrl() {
			return url;
		}

		/**
		 * @param url the url to set
		 */
		public void setUrl(String url) {
			this.url = url;
		}

		/**
		 * @return the slsPrice
		 */
		public String getSlsPrice() {
			return slsPrice;
		}

		/**
		 * @param slsPrice the slsPrice to set
		 */
		public void setSlsPrice(String slsPrice) {
			this.slsPrice = slsPrice;
		}

		/**
		 * @return the cnt
		 */
		public int getCnt() {
			return cnt;
		}

		/**
		 * @param cnt the cnt to set
		 */
		public void setCnt(int cnt) {
			this.cnt = cnt;
		}
	}

	/**
	 * 販売数情報です。
	 * @author kamiike
	 *
	 */
	public static class SaleLimit extends BaseDO {
		/**
		 *
		 */
		private static final long serialVersionUID = -506103430285673725L;

		/**
		 * 表示順序
		 */
		private int order;

		/**
		 * 画像URL
		 */
		private String url;

		/**
		 * @return the order
		 */
		public int getOrder() {
			return order;
		}

		/**
		 * @param order the order to set
		 */
		public void setOrder(int order) {
			this.order = order;
		}

		/**
		 * @return the url
		 */
		public String getUrl() {
			return url;
		}

		/**
		 * @param url the url to set
		 */
		public void setUrl(String url) {
			this.url = url;
		}
	}

	/**
	 * 販売数情報です。
	 * @author kamiike
	 *
	 */
	public static class SalesResult extends BaseDO {
		/**
		 *
		 */
		private static final long serialVersionUID = -8037589545314377271L;
		/**
		 * 累計販売数です。
		 */
		private Integer slsSumCnt;
		/**
		 * 販売リピート率です。
		 */
		private Float slsRptRate;
		/**
		 * @return the slsSumCnt
		 */
		public Integer getSlsSumCnt() {
			return slsSumCnt;
		}
		/**
		 * @param slsSumCnt the slsSumCnt to set
		 */
		public void setSlsSumCnt(Integer slsSumCnt) {
			this.slsSumCnt = slsSumCnt;
		}
		/**
		 * @return the slsRptRate
		 */
		public Float getSlsRptRate() {
			return slsRptRate;
		}
		/**
		 * @param slsRptRate the slsRptRate to set
		 */
		public void setSlsRptRate(Float slsRptRate) {
			this.slsRptRate = slsRptRate;
		}
	}

	/**
	 * レビュー設問情報です。
	 * @author kamiike
	 *
	 */
	public static class ReviewQuestPoint extends BaseDO {

		/**
		 *
		 */
		private static final long serialVersionUID = -8701533160518685857L;

		/**
		 * レビュー設問コードです。
		 */
		private String rvwQstCd;

		/**
		 * 設問ベースポイント
		 */
		private Long rvwQstBasePoint;

		/**
		 * レビュー特別条件情報
		 */
		private ReviewQuestPointSpecial reviewQuestPointSpecial;

		/**
		 * @return the rvwQstCd
		 */
		public String getRvwQstCd() {
			return rvwQstCd;
		}

		/**
		 * @param rvwQstCd the rvwQstCd to set
		 */
		public void setRvwQstCd(String rvwQstCd) {
			this.rvwQstCd = rvwQstCd;
		}

		/**
		 * @return the rvwQstBasePoint
		 */
		public Long getRvwQstBasePoint() {
			return rvwQstBasePoint;
		}

		/**
		 * @param rvwQstBasePoint the rvwQstBasePoint to set
		 */
		public void setRvwQstBasePoint(Long rvwQstBasePoint) {
			this.rvwQstBasePoint = rvwQstBasePoint;
		}

		/**
		 * @return the reviewQuestPointSpecial
		 */
		public ReviewQuestPointSpecial getReviewQuestPointSpecial() {
			return reviewQuestPointSpecial;
		}

		/**
		 * @param reviewQuestPointSpecial the reviewQuestPointSpecial to set
		 */
		public void setReviewQuestPointSpecial(
				ReviewQuestPointSpecial reviewQuestPointSpecial) {
			this.reviewQuestPointSpecial = reviewQuestPointSpecial;
		}

		public static class ReviewQuestPointSpecial extends BaseDO {

			/**
			 *
			 */
			private static final long serialVersionUID = 2915392006118825045L;

			/**
			 * 条件タイプです。
			 * 1:先着型のレビュー
			 * 2:期間のレビュー
			 */
			private Integer rvwSpTyp;

			/**
			 * ポイント付与タイプです。
			 * 1:置き換えのポイント
			 * 2:上乗せ
			 */
			private Integer ptTyp;

			/**
			 * 特別ポイントです。
			 */
			private Long rvwSpPoint;

			/**
			 * レビュー特別条件閾値終了（数値）です。
			 */
			private Integer rvwSpDtlEdThdNum;

			/**
			 * レビュー特別条件閾値開始（日付）です。
			 */
			private Date rvwSpDtlSttThdDt;

			/**
			 * レビュー特別条件閾値終了（日付）です。
			 */
			private Date rvwSpDtlEdThdDt;

			/**
			 * @return the rvwSpTyp
			 */
			public Integer getRvwSpTyp() {
				return rvwSpTyp;
			}

			/**
			 * @param rvwSpTyp the rvwSpTyp to set
			 */
			public void setRvwSpTyp(Integer rvwSpTyp) {
				this.rvwSpTyp = rvwSpTyp;
			}

			/**
			 * @return the ptTyp
			 */
			public Integer getPtTyp() {
				return ptTyp;
			}

			/**
			 * @param ptTyp the ptTyp to set
			 */
			public void setPtTyp(Integer ptTyp) {
				this.ptTyp = ptTyp;
			}

			/**
			 * @return the rvwSpPoint
			 */
			public Long getRvwSpPoint() {
				return rvwSpPoint;
			}

			/**
			 * @param rvwSpPoint the rvwSpPoint to set
			 */
			public void setRvwSpPoint(Long rvwSpPoint) {
				this.rvwSpPoint = rvwSpPoint;
			}

			/**
			 * @return the rvwSpDtlEdThdNum
			 */
			public Integer getRvwSpDtlEdThdNum() {
				return rvwSpDtlEdThdNum;
			}

			/**
			 * @param rvwSpDtlEdThdNum the rvwSpDtlEdThdNum to set
			 */
			public void setRvwSpDtlEdThdNum(Integer rvwSpDtlEdThdNum) {
				this.rvwSpDtlEdThdNum = rvwSpDtlEdThdNum;
			}

			/**
			 * @return the rvwSpDtlSttThdDt
			 */
			public Date getRvwSpDtlSttThdDt() {
				return rvwSpDtlSttThdDt;
			}

			/**
			 * @param rvwSpDtlSttThdDt the rvwSpDtlSttThdDt to set
			 */
			public void setRvwSpDtlSttThdDt(Date rvwSpDtlSttThdDt) {
				this.rvwSpDtlSttThdDt = rvwSpDtlSttThdDt;
			}

			/**
			 * @return the rvwSpDtlEdThdDt
			 */
			public Date getRvwSpDtlEdThdDt() {
				return rvwSpDtlEdThdDt;
			}

			/**
			 * @param rvwSpDtlEdThdDt the rvwSpDtlEdThdDt to set
			 */
			public void setRvwSpDtlEdThdDt(Date rvwSpDtlEdThdDt) {
				this.rvwSpDtlEdThdDt = rvwSpDtlEdThdDt;
			}
			
		}
	}



	/**
	 * レビュー設問情報です。
	 * @author kamiike
	 *
	 */
	public static class ReviewPoint extends BaseDO {

		/**
		 *
		 */
		private static final long serialVersionUID = 4263483157825776520L;

		/**
		 * レビュー設問コードです。
		 */
		private String rvwQstCd;

		/**
		 * レビュー設問有効開始日時です。
		 */
		private Date rvwQstSttTm;

		/**
		 * レビュー設問有効終了日時です。
		 */
		private Date rvwQstEdTm;

		
		private Date startTime;
		
		private Date endTime;
		
		/**
		 * @return the startTime
		 */
		public Date getStartTime() {
			return startTime;
		}

		/**
		 * @param startTime the startTime to set
		 */
		public void setStartTime(Date startTime) {
			this.startTime = startTime;
		}

		/**
		 * @return the endTime
		 */
		public Date getEndTime() {
			return endTime;
		}

		/**
		 * @param endTime the endTime to set
		 */
		public void setEndTime(Date endTime) {
			this.endTime = endTime;
		}

		/**
		 * 表示順序です。
		 */
		private Integer order;

		/**
		 * レビュー設問情報詳細リストです。
		 */
		private ReviewPointDetail[] rvwQstDtls;

		/**
		 * レビュー特別条件情報リストです。
		 */
		private ReviewPointSpecial[] rvwSps;

		public ReviewPointDetail getReviewPointDetail(int elapsedDays) {
			if (rvwQstDtls == null || rvwQstDtls.length == 0) {
				return null;
			}
			for (ReviewPointDetail detail : rvwQstDtls) {
				if (DateUtil.matchTerm(detail.getRvwQstDtlSttTm(),
						detail.getRvwQstDtlEdTm(), new Date())
						&& detail.getRvwQstDtlSttThd() <= elapsedDays
						&& detail.getRvwQstDtlEdThd() >= elapsedDays) {
					return detail;
				}
			}
			return null;
		}

		/**
		 * @return rvwQstCd
		 */
		public String getRvwQstCd() {
			return rvwQstCd;
		}

		/**
		 * @param rvwQstCd セットする rvwQstCd
		 */
		public void setRvwQstCd(String rvwQstCd) {
			this.rvwQstCd = rvwQstCd;
		}

		/**
		 * @return rvwQstSttTm
		 */
		public Date getRvwQstSttTm() {
			return rvwQstSttTm;
		}

		/**
		 * @param rvwQstSttTm セットする rvwQstSttTm
		 */
		public void setRvwQstSttTm(String rvwQstSttTm) {
			this.rvwQstSttTm = toDate(rvwQstSttTm);
		}

		/**
		 * @return rvwQstEdTm
		 */
		public Date getRvwQstEdTm() {
			return rvwQstEdTm;
		}

		/**
		 * @param rvwQstEdTm セットする rvwQstEdTm
		 */
		public void setRvwQstEdTm(String rvwQstEdTm) {
			this.rvwQstEdTm = toAfterOneSecond(toDate(rvwQstEdTm));
		}

		/**
		 * @return order
		 */
		public Integer getOrder() {
			return order;
		}

		/**
		 * @param order セットする order
		 */
		public void setOrder(Integer order) {
			this.order = order;
		}

		/**
		 * @return rvwQstDtls
		 */
		public ReviewPointDetail[] getRvwQstDtls() {
			return rvwQstDtls;
		}

		/**
		 * @param rvwQstDtls セットする rvwQstDtls
		 */
		public void setRvwQstDtls(ReviewPointDetail[] rvwQstDtls) {
			this.rvwQstDtls = rvwQstDtls;
		}

		/**
		 * @return rvwSps
		 */
		public ReviewPointSpecial[] getRvwSps() {
			return rvwSps;
		}

		/**
		 * @param rvwSps セットする rvwSps
		 */
		public void setRvwSps(ReviewPointSpecial[] rvwSps) {
			this.rvwSps = rvwSps;
		}
	}

	/**
	 * レビュー設問情報詳細です。
	 * @author kamiike
	 *
	 */
	public static class ReviewPointDetail extends BaseDO {

		/**
		 *
		 */
		private static final long serialVersionUID = -8118776046444288856L;

		/**
		 * レビュー設問詳細コードです。
		 */
		private String rvwQstDtlCd;

		/**
		 * レビュー設問詳細条件開始日時です。
		 */
		private Date rvwQstDtlSttTm;

		/**
		 * レビュー設問詳細条件終了日時です。
		 */
		private Date rvwQstDtlEdTm;

		/**
		 * レビュー設問詳細閾値開始日数です。
		 */
		private Integer rvwQstDtlSttThd;

		/**
		 * レビュー設問詳細閾値終了日数です。
		 */
		private Integer rvwQstDtlEdThd;

		/**
		 * 基本レビューポイント額です。
		 */
		private Long rvwBasePt;

		/**
		 * 最終更新日時です。
		 */
		private Date lstUpd;

		/**
		 * @return rvwQstDtlCd
		 */
		public String getRvwQstDtlCd() {
			return rvwQstDtlCd;
		}

		/**
		 * @param rvwQstDtlCd セットする rvwQstDtlCd
		 */
		public void setRvwQstDtlCd(String rvwQstDtlCd) {
			this.rvwQstDtlCd = rvwQstDtlCd;
		}

		/**
		 * @return rvwQstDtlSttTm
		 */
		public Date getRvwQstDtlSttTm() {
			return rvwQstDtlSttTm;
		}

		/**
		 * @param rvwQstDtlSttTm セットする rvwQstDtlSttTm
		 */
		public void setRvwQstDtlSttTm(String rvwQstDtlSttTm) {
			this.rvwQstDtlSttTm = toDate(rvwQstDtlSttTm);
		}

		/**
		 * @return rvwQstDtlEdTm
		 */
		public Date getRvwQstDtlEdTm() {
			return rvwQstDtlEdTm;
		}

		/**
		 * @param rvwQstDtlEdTm セットする rvwQstDtlEdTm
		 */
		public void setRvwQstDtlEdTm(String rvwQstDtlEdTm) {
			this.rvwQstDtlEdTm = toAfterOneSecond(toDate(rvwQstDtlEdTm));
		}

		/**
		 * @return rvwQstDtlSttThd
		 */
		public Integer getRvwQstDtlSttThd() {
			return rvwQstDtlSttThd;
		}

		/**
		 * @param rvwQstDtlSttThd セットする rvwQstDtlSttThd
		 */
		public void setRvwQstDtlSttThd(Integer rvwQstDtlSttThd) {
			this.rvwQstDtlSttThd = rvwQstDtlSttThd;
		}

		/**
		 * @return rvwQstDtlEdThd
		 */
		public Integer getRvwQstDtlEdThd() {
			return rvwQstDtlEdThd;
		}

		/**
		 * @param rvwQstDtlEdThd セットする rvwQstDtlEdThd
		 */
		public void setRvwQstDtlEdThd(Integer rvwQstDtlEdThd) {
			this.rvwQstDtlEdThd = rvwQstDtlEdThd;
		}

		/**
		 * @return rvwBasePt
		 */
		public Long getRvwBasePt() {
			return rvwBasePt;
		}

		/**
		 * @param rvwBasePt セットする rvwBasePt
		 */
		public void setRvwBasePt(Long rvwBasePt) {
			this.rvwBasePt = rvwBasePt;
		}

		/**
		 * @return lstUpd
		 */
		public Date getLstUpd() {
			return lstUpd;
		}

		/**
		 * @param lstUpd セットする lstUpd
		 */
		public void setLstUpd(String lstUpd) {
			this.lstUpd = toDate(lstUpd);
		}
	}

	/**
	 * レビュー特別条件情報です。
	 * @author kamiike
	 *
	 */
	public static class ReviewPointSpecial extends BaseDO {

		/**
		 * 先着型のレビュー特別条件タイプです。
		 */
		public static final int SPECIAL_COND_TYPE_FIRST = 1;

		/**
		 * 期間のレビュー特別条件タイプです。
		 */
		public static final int SPECIAL_COND_TYPE_TERM = 2;

		/**
		 * 購入日（請求日）のレビュー特別条件期間判定タイプです。
		 */
		public static final int SPECIAL_COND_JUDGEMENT_TYPE_PURCHASE = 1;

		/**
		 * 受注日のレビュー特別条件期間判定タイプです。
		 */
		public static final int SPECIAL_COND_JUDGEMENT_TYPE_ORDER = 2;

		/**
		 * 投稿日のレビュー特別条件期間判定タイプです。
		 */
		public static final int SPECIAL_COND_JUDGEMENT_TYPE_POST = 3;

		/**
		 * 置き換えのポイント付与タイプです。
		 */
		public static final int POINT_TYPE_REPLACE = 1;

		/**
		 * 上乗せのポイント付与タイプです。
		 */
		public static final int POINT_TYPE_ADD = 2;

		/**
		 *
		 */
		private static final long serialVersionUID = 3320967183987879701L;

		/**
		 * レビュー特別条件コードです。
		 */
		private String rvwSpCd;

		/**
		 * レビュー特別条件タイプです。
		 */
		private Integer rvwSpTyp;

		/**
		 * レビュー特別条件タイトルです。
		 */
		private String rvwSpTitle;

		/**
		 * レビュー特別条件期間判定タイプです。
		 */
		private Integer rvwSpJdgTyp;

		/**
		 * レビュー特別条件有効開始日時です。
		 */
		private Date rvwSpSttTm;

		/**
		 * レビュー特別条件有効終了日時です。
		 */
		private Date rvwSpEdTm;

		/**
		 * ポイント付与タイプです。
		 */
		private Integer ptTyp;

		/**
		 * 優先順位です。
		 */
		private Integer pryNo;

		/**
		 * 最終更新日時です。
		 */
		private Date lstUpd;

		/**
		 * レビュー特別条件詳細情報リストです。
		 */
		private ReviewPointSpecialDetail[] rvwSpDtls;

		/**
		 * @return rvwSpCd
		 */
		public String getRvwSpCd() {
			return rvwSpCd;
		}

		/**
		 * @param rvwSpCd セットする rvwSpCd
		 */
		public void setRvwSpCd(String rvwSpCd) {
			this.rvwSpCd = rvwSpCd;
		}

		/**
		 * @return rvwSpTyp
		 */
		public Integer getRvwSpTyp() {
			return rvwSpTyp;
		}

		/**
		 * @param rvwSpTyp セットする rvwSpTyp
		 */
		public void setRvwSpTyp(Integer rvwSpTyp) {
			this.rvwSpTyp = rvwSpTyp;
		}

		/**
		 * @return rvwSpTitle
		 */
		public String getRvwSpTitle() {
			return rvwSpTitle;
		}

		/**
		 * @param rvwSpTitle セットする rvwSpTitle
		 */
		public void setRvwSpTitle(String rvwSpTitle) {
			this.rvwSpTitle = rvwSpTitle;
		}

		/**
		 * @return rvwSpJdgTyp
		 */
		public Integer getRvwSpJdgTyp() {
			return rvwSpJdgTyp;
		}

		/**
		 * @param rvwSpJdgTyp セットする rvwSpJdgTyp
		 */
		public void setRvwSpJdgTyp(Integer rvwSpJdgTyp) {
			this.rvwSpJdgTyp = rvwSpJdgTyp;
		}

		/**
		 * @return rvwSpSttTm
		 */
		public Date getRvwSpSttTm() {
			return rvwSpSttTm;
		}

		/**
		 * @param rvwSpSttTm セットする rvwSpSttTm
		 */
		public void setRvwSpSttTm(String rvwSpSttTm) {
			this.rvwSpSttTm = toDate(rvwSpSttTm);
		}

		/**
		 * @return rvwSpEdTm
		 */
		public Date getRvwSpEdTm() {
			return rvwSpEdTm;
		}

		/**
		 * @param rvwSpEdTm セットする rvwSpEdTm
		 */
		public void setRvwSpEdTm(String rvwSpEdTm) {
			this.rvwSpEdTm = toAfterOneSecond(toDate(rvwSpEdTm));
		}

		/**
		 * @return ptTyp
		 */
		public Integer getPtTyp() {
			return ptTyp;
		}

		/**
		 * @param ptTyp セットする ptTyp
		 */
		public void setPtTyp(Integer ptTyp) {
			this.ptTyp = ptTyp;
		}

		/**
		 * @return pryNo
		 */
		public Integer getPryNo() {
			return pryNo;
		}

		/**
		 * @param pryNo セットする pryNo
		 */
		public void setPryNo(Integer pryNo) {
			this.pryNo = pryNo;
		}

		/**
		 * @return lstUpd
		 */
		public Date getLstUpd() {
			return lstUpd;
		}

		/**
		 * @param lstUpd セットする lstUpd
		 */
		public void setLstUpd(String lstUpd) {
			this.lstUpd = toDate(lstUpd);
		}

		/**
		 * @return rvwSpDtls
		 */
		public ReviewPointSpecialDetail[] getRvwSpDtls() {
			return rvwSpDtls;
		}

		/**
		 * @param rvwSpDtls セットする rvwSpDtls
		 */
		public void setRvwSpDtls(ReviewPointSpecialDetail[] rvwSpDtls) {
			this.rvwSpDtls = rvwSpDtls;
		}

		public boolean isFirstCondType() {
			if( rvwSpTyp == null)
				return false;
			
			return rvwSpTyp.equals(SPECIAL_COND_TYPE_FIRST);
		}

		public boolean isTermCondType() {
			if( rvwSpTyp == null)
				return false;
			
			return rvwSpTyp.equals(SPECIAL_COND_TYPE_TERM);
		}

		public boolean isPurchaseJudgeType() {
			if( rvwSpJdgTyp == null)
				return false;
			
			return rvwSpJdgTyp.equals(SPECIAL_COND_JUDGEMENT_TYPE_PURCHASE);
		}

		public boolean isOrderJudgeType() {
			if( rvwSpJdgTyp == null)
				return false;
			
			return rvwSpJdgTyp.equals(SPECIAL_COND_JUDGEMENT_TYPE_ORDER);
		}

		public boolean isPostJudgeType() {
			if( rvwSpJdgTyp == null)
				return false;
			
			return rvwSpJdgTyp.equals(SPECIAL_COND_JUDGEMENT_TYPE_POST);
		}

		public boolean isPointReplace() {
			if( ptTyp == null)
				return false;
			
			return ptTyp.equals(POINT_TYPE_REPLACE);
		}

		public boolean isPointAdd() {
			if( ptTyp == null)
				return false;
			
			return ptTyp.equals(POINT_TYPE_ADD);
		}
	}

	/**
	 * レビュー特別条件詳細情報です。
	 * @author kamiike
	 *
	 */
	public static class ReviewPointSpecialDetail extends BaseDO {

		/**
		 *
		 */
		private static final long serialVersionUID = 5308940271804762117L;

		/**
		 * レビュー特別条件閾値タイプです。
		 */
		private String rvwSpDtlTyp;

		/**
		 * レビュー特別条件閾値開始（日付）です。
		 */
		private Date rvwSpDtlSttThdDt;

		/**
		 * レビュー特別条件閾値終了（日付）です。
		 */
		private Date rvwSpDtlEdThdDt;

		/**
		 * レビュー特別条件閾値開始（数値）です。
		 */
		private Integer rvwSpDtlSttThdNum;

		/**
		 * レビュー特別条件閾値終了（数値）です。
		 */
		private Integer rvwSpDtlEdThdNum;

		/**
		 * レビュー特別条件ポイント額です。
		 */
		private Long rvwSpDtlPt;

		/**
		 * 最終更新日時です。
		 */
		private Date lstUpd;

		/**
		 * @return rvwSpDtlTyp
		 */
		public String getRvwSpDtlTyp() {
			return rvwSpDtlTyp;
		}

		/**
		 * @param rvwSpDtlTyp セットする rvwSpDtlTyp
		 */
		public void setRvwSpDtlTyp(String rvwSpDtlTyp) {
			this.rvwSpDtlTyp = rvwSpDtlTyp;
		}

		/**
		 * @return rvwSpDtlSttThdDt
		 */
		public Date getRvwSpDtlSttThdDt() {
			return rvwSpDtlSttThdDt;
		}

		/**
		 * @param rvwSpDtlSttThdDt セットする rvwSpDtlSttThdDt
		 */
		public void setRvwSpDtlSttThdDt(String rvwSpDtlSttThdDt) {
			this.rvwSpDtlSttThdDt = toDate(rvwSpDtlSttThdDt);
		}

		/**
		 * @return rvwSpDtlEdThdDt
		 */
		public Date getRvwSpDtlEdThdDt() {
			return rvwSpDtlEdThdDt;
		}

		/**
		 * @param rvwSpDtlEdThdDt セットする rvwSpDtlEdThdDt
		 */
		public void setRvwSpDtlEdThdDt(String rvwSpDtlEdThdDt) {
			this.rvwSpDtlEdThdDt = toAfterOneSecond(toDate(rvwSpDtlEdThdDt));
		}

		/**
		 * @return rvwSpDtlSttThdNum
		 */
		public Integer getRvwSpDtlSttThdNum() {
			return rvwSpDtlSttThdNum;
		}

		/**
		 * @param rvwSpDtlSttThdNum セットする rvwSpDtlSttThdNum
		 */
		public void setRvwSpDtlSttThdNum(Integer rvwSpDtlSttThdNum) {
			this.rvwSpDtlSttThdNum = rvwSpDtlSttThdNum;
		}

		/**
		 * @return rvwSpDtlEdThdNum
		 */
		public Integer getRvwSpDtlEdThdNum() {
			return rvwSpDtlEdThdNum;
		}

		/**
		 * @param rvwSpDtlEdThdNum セットする rvwSpDtlEdThdNum
		 */
		public void setRvwSpDtlEdThdNum(Integer rvwSpDtlEdThdNum) {
			this.rvwSpDtlEdThdNum = rvwSpDtlEdThdNum;
		}

		/**
		 * @return rvwSpDtlPt
		 */
		public Long getRvwSpDtlPt() {
			return rvwSpDtlPt;
		}

		/**
		 * @param rvwSpDtlPt セットする rvwSpDtlPt
		 */
		public void setRvwSpDtlPt(Long rvwSpDtlPt) {
			this.rvwSpDtlPt = rvwSpDtlPt;
		}

		/**
		 * @return lstUpd
		 */
		public Date getLstUpd() {
			return lstUpd;
		}

		/**
		 * @param lstUpd セットする lstUpd
		 */
		public void setLstUpd(String lstUpd) {
			this.lstUpd = toDate(lstUpd);
		}
	}


	/**
	 * 指定した日時の一秒後を返します。
	 * @return 指定時間の1秒後
	 */
	private static Date toAfterOneSecond(Date src) {
		if (src == null) {
			return null;
		}
		return new Date(src.getTime() + 1000L);
	}

	/**
	 * 日付インスタンスに変換します。
	 * @param time 時間
	 * @return 日付インスタンス
	 */
	private static Date toDate(String time) {
		if (StringUtils.isEmpty(time)) {
			return null;
		}
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss.SSS");
		try {
			return formatter.parse(time);
		} catch (ParseException e) {
			throw new IllegalArgumentException(
					"Format is invalid. value = " + time, e);
		}
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
	 * @return rvwCmnt
	 */
	public String getRvwCmnt() {
		return rvwCmnt;
	}

	/**
	 * @param rvwCmnt セットする rvwCmnt
	 */
	public void setRvwCmnt(String rvwCmnt) {
		this.rvwCmnt = rvwCmnt;
	}

	/**
	 * @return rvwPtCalcTyp
	 */
	public Integer getRvwPtCalcTyp() {
		return rvwPtCalcTyp;
	}

	/**
	 * @param rvwPtCalcTyp セットする rvwPtCalcTyp
	 */
	public void setRvwPtCalcTyp(Integer rvwPtCalcTyp) {
		this.rvwPtCalcTyp = rvwPtCalcTyp;
	}

	/**
	 * @return rvwInitPostTerm
	 */
	public int getRvwInitPostTerm() {
		return rvwInitPostTerm == null ? 0 : rvwInitPostTerm;
	}

	/**
	 * @param rvwInitPostTerm セットする rvwInitPostTerm
	 */
	public void setRvwInitPostTerm(Integer rvwInitPostTerm) {
		this.rvwInitPostTerm = rvwInitPostTerm;
	}

	/**
	 * @return rvwCntnPostCnt
	 */
	public int getRvwCntnPostCnt() {
		return rvwCntnPostCnt == null ? 0 : rvwCntnPostCnt;
	}

	/**
	 * @param rvwCntnPostCnt セットする rvwCntnPostCnt
	 */
	public void setRvwCntnPostCnt(Integer rvwCntnPostCnt) {
		this.rvwCntnPostCnt = rvwCntnPostCnt;
	}

	/**
	 * @return rvwCntnPostTerm
	 */
	public int getRvwCntnPostTerm() {
		return rvwCntnPostTerm == null ? 0 : rvwCntnPostTerm;
	}

	/**
	 * @param rvwCntnPostTerm セットする rvwCntnPostTerm
	 */
	public void setRvwCntnPostTerm(Integer rvwCntnPostTerm) {
		this.rvwCntnPostTerm = rvwCntnPostTerm;
	}

	/**
	 * @return rvwSps
	 */
	public ReviewPointSpecial[] getRvwSps() {
		return rvwSps;
	}

	/**
	 * @param rvwSps セットする rvwSps
	 */
	public void setRvwSps(ReviewPointSpecial[] rvwSps) {
		this.rvwSps = rvwSps;
	}
	
	
	/**
	 * @return rvwSps
	 */
	public String[] getRvwSpCodes() {
		List<String> spCodes = new ArrayList<String>();
		// 品目特別条件の取得
		if(rvwSps != null && rvwSps.length>0) {
			for(ReviewPointSpecial special:rvwSps){
				spCodes.add(special.getRvwSpCd());
			}
		}
		// 設問特別条件の取得
		if(rvwQsts != null && rvwQsts.length > 0) {
			for(ReviewPoint quest:rvwQsts){
				if(quest.getRvwSps() != null && quest.getRvwSps().length>0) {
					for(ReviewPointSpecial special:quest.getRvwSps()){
						spCodes.add(special.getRvwSpCd());
					}
				}
			}
		}
		return spCodes.toArray(new String[]{});
	}

	/**
	 * @return the publFlg
	 */
	public boolean isPublFlg() {
		return publFlg;
	}

	/**
	 * @param publFlg the publFlg to set
	 */
	public void setPublFlg(boolean publFlg) {
		this.publFlg = publFlg;
	}

	public String getCartTag() {
		return cartTag;
	}

	public void setCartTag(String cartTag) {
		this.cartTag = cartTag;
	}

	public String getNorenStock() {
		return norenStock;
	}

	public void setNorenStock(String norenStock) {
		this.norenStock = norenStock;
	}

	public String getNorenCartTag() {
		return norenCartTag;
	}

	public void setNorenCartTag(String norenCartTag) {
		this.norenCartTag = norenCartTag;
	}


	/**
	 * ポイント付与のレビューの評価期間を返します。
	 * @param purchaseDate 購入日付
	 * @param pointBaseTime レビューポイント基準日
	 * @return レビュー期間  初回投稿期間は0、以下1,2,,,, レビュー不可の場合、-1
	 */
	public int getGrantPointReviewTerm(Date purchaseDate) {
		return getGrantPointReviewTerm(purchaseDate, new Date());
	}
	public int getGrantPointReviewTerm(Date purchaseDate, Date pointBaseTime) {
		if (!this.isCanReview()) {
			return -1;
		}
		if (!isGrantPointWithinTerm(purchaseDate, pointBaseTime)) {
			return -1;
		}
		// 購入日からの経過日数
		int elapsedDays = DateUtil.getElapsedDays(purchaseDate, pointBaseTime);
		
		int reviewInitPostTerm = this.getRvwInitPostTerm();		// 初期投稿期間
		int reviewContinuePostTerm = this.getRvwCntnPostTerm();	// 継続投稿期間
		
		if (elapsedDays <= reviewInitPostTerm) {
			return 0;
		} else {
			int term = (elapsedDays - reviewInitPostTerm) / reviewContinuePostTerm;
			if ((elapsedDays - reviewInitPostTerm) % reviewContinuePostTerm > 0) {
				return term + 1;
			} else {
				return term;
			}
		}
	}
	
	/**
	 * 次のポイント付与レビューの期限までの日数を返します。
	 * @param purchaseDate 購入日付
	 * @param pointBaseTime レビューポイント基準日
	 * @return 次のレビューの期限までの日数、ポイントが付かない場合、マイナス値
	 */
	public int getNextGrantPointReviewLimit(Date purchaseDate) {
		return getNextGrantPointReviewLimit(purchaseDate, new Date());
	}
	public int getNextGrantPointReviewLimit(Date purchaseDate, Date pointBaseTime) {
		if (!this.isCanReview()) {
			return -1;
		}
		// 購入日からの経過日数
		int elapsedDays = DateUtil.getElapsedDays(purchaseDate, pointBaseTime);
		
		int reviewInitPostTerm = this.getRvwInitPostTerm();		// 初期投稿期間
		int reviewContinuePostCount = this.getRvwCntnPostCnt();	// 継続投稿回数
		int reviewContinuePostTerm = this.getRvwCntnPostTerm();	// 継続投稿期間
		
		// ポイント付与期間日数
		int reviewEffectiveDays = reviewInitPostTerm + reviewContinuePostCount * reviewContinuePostTerm;
		
		if (elapsedDays >= reviewEffectiveDays - reviewContinuePostTerm) {
			return -1;	// 次のレビューではもうポイント付与期間を超えている
		}
		
		if (elapsedDays <= reviewInitPostTerm) {
			return reviewInitPostTerm - elapsedDays;
		} else {
			return reviewContinuePostTerm - ((elapsedDays - reviewInitPostTerm) % reviewContinuePostTerm) + 1; 
		}
	}

	/**
	 * ポイント付与有効期限内か
	 * @param purchaseDate
	 * @return
	 */
	public boolean isGrantPointWithinTerm(Date purchaseDate) {
		return isGrantPointWithinTerm(purchaseDate, new Date());
	}
	public boolean isGrantPointWithinTerm(Date purchaseDate, Date pointBaseTime) {
		// 購入日からの経過日数
		int elapsedDays = DateUtil.getElapsedDays(purchaseDate, pointBaseTime);
		
		int reviewInitPostTerm = this.getRvwInitPostTerm();		// 初期投稿期間
		int reviewContinuePostCount = this.getRvwCntnPostCnt();	// 継続投稿回数
		int reviewContinuePostTerm = this.getRvwCntnPostTerm();	// 継続投稿期間
		
		// ポイント付与期間日数
		int reviewEffectiveDays = reviewInitPostTerm + reviewContinuePostCount * reviewContinuePostTerm;
		
		return elapsedDays <= reviewEffectiveDays;
	}

	/**
	 * 現期間のレビューポイント取得終了日までの日にちを返す
	 * @param purchaseDate
	 * @param pointBaseTime
	 * @return
	 */
	public int getNowGrantPointReviewLimit(Date purchaseDate) {
		return getNowGrantPointReviewLimit(purchaseDate, new Date());
	}
	public int getNowGrantPointReviewLimit(Date purchaseDate, Date pointBaseTime) {
		if (!this.isCanReview()) {
			return -1;
		}
		// 購入日からの経過日数
		int elapsedDays = DateUtil.getElapsedDays(purchaseDate, pointBaseTime);
		
		int reviewInitPostTerm = this.getRvwInitPostTerm();		// 初期投稿期間
		int reviewContinuePostCount = this.getRvwCntnPostCnt();	// 継続投稿回数
		int reviewContinuePostTerm = this.getRvwCntnPostTerm();	// 継続投稿期間
		
		// ポイント付与期間日数
		int reviewEffectiveDays = reviewInitPostTerm + reviewContinuePostCount * reviewContinuePostTerm;
		
		if (elapsedDays >= reviewEffectiveDays) {
			return -1;	// もうポイント付与期間を超えている
		}
		
		if (elapsedDays <= reviewInitPostTerm) {
			return reviewInitPostTerm - elapsedDays;
		} else {
			return reviewContinuePostTerm - ((elapsedDays - reviewInitPostTerm) % reviewContinuePostTerm); 
		}
	}
	
	// Test Code
//	public static void main(String[] args) throws Throwable {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
//		ProductDO productDO = new ProductDO();
//		productDO.setRvwInitPostTerm(10);
//		productDO.setRvwCntnPostCnt(3);
//		productDO.setRvwCntnPostTerm(5);
//		productDO.setRvwPtFlg(true);
//		productDO.setRvwPtSttTm("2014-01-01 00:00:00.000");
//		
//		Date purchaseDate = sdf.parse("2014/01/21");
//		
//		System.out.println(productDO.getReviewTerm(sdf.parse("2014/01/21")));
//		System.out.println(productDO.getReviewTerm(sdf.parse("2014/01/20")));
//		System.out.println(productDO.getReviewTerm(sdf.parse("2014/01/10")));
//		System.out.println(productDO.getReviewTerm(sdf.parse("2014/01/06")));
//		
//		System.out.println(sdf.format(productDO.getNextGrantPointLimitDate(sdf.parse("2014/01/01"), sdf.parse("2014/01/21"))));
//	}



}
