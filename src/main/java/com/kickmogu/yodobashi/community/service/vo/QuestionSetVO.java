/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import java.util.List;

import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;

/**
 * 質問関連の表示情報を集めたビューオブジェクトです。
 * @author kamiike
 *
 */
public class QuestionSetVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = 844802089780233825L;

	/**
	 * 質問です。
	 */
	private QuestionDO question;

	
	/**
	 * 質問画像一覧です。
	 */
	private List<ImageHeaderDO> images;
	
	/**
	 * 質問回答リストです。
	 */
	private SearchResult<QuestionAnswerSetVO> answerSets;

	/**
	 * 回答者数です。
	 */
	private long answerCount;

	/**
	 * フォロー済みかどうかです。
	 */
	private boolean followingFlg;
	
	/**
	 * 回答済みがどうかです。
	 */
	private boolean answerFlg;
	
	/**
	 * 購入商品情報です。
	 */
	private PurchaseProductDO purchaseProduct;
	
	/**
	 * ログインユーザーの購入商品かどうか
	 */
	private PurchaseProductDO loginUserPurchaseProduct;
	/**
	 * 改竄チェック用
	 */
	private String ghbf;
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
	 * @return answerSets
	 */
	public SearchResult<QuestionAnswerSetVO> getAnswerSets() {
		return answerSets;
	}

	/**
	 * @param answerSets セットする answerSets
	 */
	public void setAnswerSets(SearchResult<QuestionAnswerSetVO> answerSets) {
		this.answerSets = answerSets;
	}

	/**
	 * @return answerCount
	 */
	public long getAnswerCount() {
		return answerCount;
	}

	/**
	 * @param answerCount セットする answerCount
	 */
	public void setAnswerCount(long answerCount) {
		this.answerCount = answerCount;
	}

	/**
	 * @return followingFlg
	 */
	public boolean isFollowingFlg() {
		return followingFlg;
	}

	/**
	 * @param followingFlg セットする followingFlg
	 */
	public void setFollowingFlg(boolean followingFlg) {
		this.followingFlg = followingFlg;
	}
	
	/**
	 * @return actionHistoryType
	 */
	public String getPostDateTime(){
		return String.valueOf(question.getPostDate().getTime());
	}

	/**
	 * @return the topImage
	 */
	public ImageHeaderDO getTopImage() {
		if( images == null || images.isEmpty())
			return null;
		return images.get(0);
	}


	public List<ImageHeaderDO> getImages() {
		return images;
	}

	public void setImages(List<ImageHeaderDO> images) {
		this.images = images;
	}

	public boolean isAnswerFlg() {
		return answerFlg;
	}

	public void setAnswerFlg(boolean answerFlg) {
		this.answerFlg = answerFlg;
	}

	public PurchaseProductDO getPurchaseProduct() {
		return purchaseProduct;
	}

	public void setPurchaseProduct(PurchaseProductDO purchaseProduct) {
		this.purchaseProduct = purchaseProduct;
	}

	public PurchaseProductDO getLoginUserPurchaseProduct() {
		return loginUserPurchaseProduct;
	}

	public void setLoginUserPurchaseProduct(PurchaseProductDO loginUserPurchaseProduct) {
		this.loginUserPurchaseProduct = loginUserPurchaseProduct;
	}
	
	public boolean hasLoginUserPurchaseProduct() {
		return loginUserPurchaseProduct != null;
	}

	public String getGhbf() {
		return ghbf;
	}

	public void setGhbf(String ghbf) {
		this.ghbf = ghbf;
	}
}
