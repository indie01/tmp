/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.kickmogu.yodobashi.community.resource.dao.util.UserUtil;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewPoint;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewPointSpecial;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewQuestPoint;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO.ReviewQuestPoint.ReviewQuestPointSpecial;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.StoppableContents;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewType;

/**
 * 購入商品関連の表示情報を集めたビューオブジェクトです。
 * @author kamiike
 */
public class PurchaseProductSetVO extends BaseVO implements StoppableContents {
	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = 2309669372867328338L;

	/**
	 * 購入商品情報です。
	 */
	private PurchaseProductDO purchaseProduct;

	/**
	 * レビューポイント付与可能かどうかです。
	 */
	private boolean reviewPointActive;

	/**
	 * 投稿可能レビュータイプです。
	 */
	private ReviewType canPostReviewType;


	/**
	 * レビューが購入商品に対して存在するかどうかです。
	 */
	private boolean existsReview;

	/**
	 * 回答待ち質問が購入商品に対して存在するかどうかです。
	 */
	private boolean existsAnswerWaitingQuestion;

	/**
	 * 他に同じ商品を購入したコミュニティユーザーのリストです。
	 */
	private List<CommunityUserDO> otherPurchaseCommunityUsers;

	/**
	 * 購入商品に直接紐付く特別条件レビューポイントです。
	 */
	private ReviewQuestPoint productPoint;

	/**
	 * 購入商品の設問レビューポイントです。
	 */
	private List<ReviewQuestPoint> questionPoints = Lists.newArrayList();
	
	/**
	 * レビュー件数です。
	 */
	private long reviewCount;
	
	/**
	 * Q&A件数です。
	 */
	private long questionCount;
	
	/**
	 * 画像件数です。
	 */
	private long imageCount;

	
	
	public ReviewQuestPoint getProductPoint() {
		return productPoint;
	}

	public void setProductPoint(ReviewQuestPoint productPoint) {
		this.productPoint = productPoint;
	}

	public List<ReviewQuestPoint> getQuestionPoints() {
		return questionPoints;
	}

	public void setQuestionPoints(List<ReviewQuestPoint> questionPoints) {
		this.questionPoints = questionPoints;
	}

	/**
	 * 設問レビューポイントのベースポイント合計を返します。
	 * ※表記上の仕様の為、設問特別ポイントは基本ポイントと合算する。
	 * @return
	 */
	public long getTotalRvwQstBasePointOfQuests(){
		long total_quest_point = 0;
		if( null != questionPoints ){
			for( ReviewQuestPoint questionPoint : questionPoints ){
				if( null == questionPoint ){
					continue;
				}
				// 設問特別ポイントの置き換えの場合は、特別ポイントのみ加算
				ReviewQuestPointSpecial reviewQuestPointSpecial = questionPoint.getReviewQuestPointSpecial();
				if( null != reviewQuestPointSpecial ){
					Integer ptTyp = reviewQuestPointSpecial.getPtTyp();
					if( null != ptTyp && 1 == ptTyp ){
						// 置き換え
						Long val = reviewQuestPointSpecial.getRvwSpPoint();
						if( null != val ){
							total_quest_point += val;
							continue;
						}
					}
				}
				
				Long val = questionPoint.getRvwQstBasePoint();
				if( null != val ){
					total_quest_point += val;
				}
			}
		}
		return total_quest_point;
	}
	/**
	 * 現期間のレビューポイント取得終了日までの日にちを返す
	 * @return
	 */
	public String getRvwPointRemainDate(){
		Date purchaseDate = this.purchaseProduct.getPurchaseDate();
		Date nowDate = new Date();
		int limitDate = this.purchaseProduct.getProduct().getNowGrantPointReviewLimit(purchaseDate, nowDate);
		if( 0 == limitDate ){
			// 残り0日の場合は、時間単位まで丸める
			Calendar cal = Calendar.getInstance();
			cal.setTime(nowDate);
			cal.add(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			long limitTime = cal.getTime().getTime() - nowDate.getTime();
			long viewHour = limitTime/1000/60/60;
			if( viewHour == 0 ){
				return "1時間以内";
			}
			return viewHour + "時間";
		}
		return (-1 == limitDate)? "" : String.valueOf(limitDate) + "日";
	}
	
	/**
	 * 設問レビューに特別ポイント追加があるかどうかです。
	 * ※設問特別ポイントは基本ポイントと合算して表記する仕様の為、例外としてこのメソッドのチェックから除外する。
	 * @return
	 */
	public boolean isSepcialPoint(){
		boolean special_point = false;
		if( null != questionPoints ){
			for( ReviewQuestPoint questionPoint : questionPoints ){
				if( null == questionPoint ){
					continue;
				}
				// 付与済み特別コードを無視
				if( null!=ignoreRvwPtCdMap && ignoreRvwPtCdMap.containsKey(questionPoint.getRvwQstCd()) ){
					continue;
				}
				ReviewQuestPointSpecial rqps = questionPoint.getReviewQuestPointSpecial();
				if( null == rqps ){
					continue;
				}
				// 設問特別ポイントの置き換えの場合は、+α表記しないので無視
				Integer ptTyp = rqps.getPtTyp();
				if( null != ptTyp && 1 == ptTyp ){
					continue;
				}
				Long val = rqps.getRvwSpPoint();
				if( null != val && 0 < val ){
					special_point = true;
					break;
				}
			}
		}
		return special_point;
	}
	/**
	 * 品目レビューの特別ポイントが付加済みであるかどうかです。
	 * @return
	 */
	public boolean isAlreadyProductSepcialPoint(){
		if(null != ignoreRvwPtCdMap){
			return ignoreRvwPtCdMap.containsKey("product");
		}
		return false;
	}

	/**
	 * レビューポイント付与済みコードマップのセット
	 * @param sku
	 * @param communityUserId
	 * @param product
	 * @return
	 */
	public void setIgnoreRvwPtCdMap( Set<String> ignoreSpCodeMap ){
		HashMap<String,Boolean> ignoreRqMap = new HashMap<String,Boolean>();
		ProductDO product = purchaseProduct.getProduct();
		if( null == product ){
			return;
		}
		ReviewPoint rvwPts[] = product.getRvwQsts();
		if( null != rvwPts ){
			for( ReviewPoint pt : rvwPts ){
				ReviewPointSpecial sps[] = pt.getRvwSps();
				if( null == sps || 0 == sps.length ){
					continue;
				}
				for( ReviewPointSpecial sp : sps ){
					if( ignoreSpCodeMap.contains(sp.getRvwSpCd()) ){
						ignoreRqMap.put(pt.getRvwQstCd(), true);
					}
				}
			}
		}
		ReviewPointSpecial  productSps[] = product.getRvwSps();
		if( null != productSps ){
			for( ReviewPointSpecial sp : productSps ){
				if( ignoreSpCodeMap.contains(sp.getRvwSpCd()) ){
					ignoreRqMap.put("product", true);
				}
			}
		}
		ignoreRvwPtCdMap = ignoreRqMap;
	}
	
	/**
	 * @return purchaseProduct
	 */
	public PurchaseProductDO getPurchaseProduct() {
		return purchaseProduct;
	}

	/**
	 * @param purchaseProduct セットする purchaseProduct
	 */
	public void setPurchaseProduct(PurchaseProductDO purchaseProduct) {
		this.purchaseProduct = purchaseProduct;
	}

	/**
	 * @return existsReview
	 */
	public boolean isExistsReview() {
		return existsReview;
	}

	/**
	 * @param existsReview セットする existsReview
	 */
	public void setExistsReview(boolean existsReview) {
		this.existsReview = existsReview;
	}

	/**
	 * @return existsAnswerWaitingQuestion
	 */
	public boolean isExistsAnswerWaitingQuestion() {
		return existsAnswerWaitingQuestion;
	}

	/**
	 * @param existsAnswerWaitingQuestion セットする existsAnswerWaitingQuestion
	 */
	public void setExistsAnswerWaitingQuestion(boolean existsAnswerWaitingQuestion) {
		this.existsAnswerWaitingQuestion = existsAnswerWaitingQuestion;
	}

	/**
	 * @return otherPurchaseCommunityUsers
	 */
	public List<CommunityUserDO> getOtherPurchaseCommunityUsers() {
		return otherPurchaseCommunityUsers;
	}

	/**
	 * @param otherPurchaseCommunityUsers セットする otherPurchaseCommunityUsers
	 */
	public void setOtherPurchaseCommunityUsers(
			List<CommunityUserDO> otherPurchaseCommunityUsers) {
		this.otherPurchaseCommunityUsers = otherPurchaseCommunityUsers;
	}

	/**
	 * @return reviewPointActive
	 */
	public boolean isReviewPointActive() {
		return reviewPointActive;
	}

	/**
	 * @param reviewPointActive セットする reviewPointActive
	 */
	public void setReviewPointActive(boolean reviewPointActive) {
		this.reviewPointActive = reviewPointActive;
	}

	/**
	 * @return the canPostReviewType
	 */
	public ReviewType getCanPostReviewType() {
		return canPostReviewType;
	}

	/**
	 * @param canPostReviewType the canPostReviewType to set
	 */
	public void setCanPostReviewType(ReviewType canPostReviewType) {
		this.canPostReviewType = canPostReviewType;
	}

	
	
	@Override
	public List<CommunityUserDO> getRelationOwners() {
		return purchaseProduct.getRelationOwners();
	}

	@Override
	public List<String> getRelationOwnerIds() {
		return purchaseProduct.getRelationOwnerIds();
	}
	
	private HashMap<String,Boolean> ignoreRvwPtCdMap;
	
	public HashMap<String, Boolean> getIgnoreRvwPtCdMap() {
		return ignoreRvwPtCdMap;
	}

	public void setIgnoreRvwPtCdMap(HashMap<String, Boolean> ignoreRvwPtCdMap) {
		this.ignoreRvwPtCdMap = ignoreRvwPtCdMap;
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

	public long getReviewCount() {
		return reviewCount;
	}

	public void setReviewCount(long reviewCount) {
		this.reviewCount = reviewCount;
	}

	public long getQuestionCount() {
		return questionCount;
	}

	public void setQuestionCount(long questionCount) {
		this.questionCount = questionCount;
	}

	public long getImageCount() {
		return imageCount;
	}

	public void setImageCount(long imageCount) {
		this.imageCount = imageCount;
	}
	
}
