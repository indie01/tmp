/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import java.util.List;

import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;

/**
 * 質問回答関連の表示情報を集めたビューオブジェクトです。
 * @author kamiike
 */
public class QuestionAnswerSetVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = 2077988195608340707L;

	/**
	 * 質問回答です。
	 */
	private QuestionAnswerDO questionAnswer;
	
	/**
	 * 質問画像一覧です。
	 */
	private List<ImageHeaderDO> images;

    /**
     * 商品マスター情報です。
     */
	private ProductMasterDO productMaster;
	
	/**
	 * 回答者数です。
	 */
	private long answerCount;
	/**
	 * 回答済みがどうかです。
	 */
	private boolean answerFlg;

    /**
     * コメント済みかどうかです。
     */
    private boolean commentFlg;

	/**
	 * 質問回答にいいねをしているかどうかです。
	 */
	private boolean likeFlg;

	/**
	 * 回答コメント数です。
	 */
	private long commentCount;
	
	/**
	 * 回答のコメント表示数です。
	 */
	private long commentViewRemainingCount;

	/**
	 * 回答いいね数です。
	 */
	private long likeCount;
	
	/**
	 * 参考になった「はい」の数です。
	 */
	private long votingCountYes;
	
	/**
	 * 参考になった「いいえ」の数です。
	 */
	private long votingCountNo;

	/**
	 * フォロー済みかどうかです。
	 */
	private boolean followingFlg;

	/**
	 * 購入商品情報です。
	 */
	private PurchaseProductDO purchaseProduct;
	
	/**
	 * ログインユーザーの購入商品かどうか
	 */
	private PurchaseProductDO loginUserPurchaseProduct;
	
	/**
	 * いいねの接頭語表示タイプ（0：なし　1:いいねは自分だけ　2：他の人もいいねしているタイプ）
	 */
	private int likePrefixType;
	/**
	 * いいねのメッセージタイプ（0：なし　1：自分を含め3人がいいねしている 2:4人以上がいいねしている）
	 */
	private int likeMessageType;
	
	/**
	 * 2以上3人以下がいいねしている場合のユーザー名一覧
	 */
	private List<String> likeUserNames;
	
	
	/**
	 * コメント一覧
	 */
	private SearchResult<CommentSetVO> comments;
	
	/**
	 * 改竄チェック用
	 */
	private String ghbf;
	
	/**
	 * @return questionAnswer
	 */
	public QuestionAnswerDO getQuestionAnswer() {
		return questionAnswer;
	}

	/**
	 * @param questionAnswer セットする questionAnswer
	 */
	public void setQuestionAnswer(QuestionAnswerDO questionAnswer) {
		this.questionAnswer = questionAnswer;
	}

	/**
	 * @return commentFlg
	 */
	public boolean isCommentFlg(){
		return commentFlg;
	}

	/**
	 * @param commentFlg セットする commentFlg
	 */
	public void setCommentFlg(boolean commentFlg) {
		this.commentFlg = commentFlg;
	}

	/**
	 * @return likeFlg
	 */
	public boolean isLikeFlg() {
		return likeFlg;
	}

	/**
	 * @param likeFlg セットする likeFlg
	 */
	public void setLikeFlg(boolean likeFlg) {
		this.likeFlg = likeFlg;
	}

	/**
	 * @return commentCount
	 */
	public long getCommentCount() {
		return commentCount;
	}

	/**
	 * @param commentCount セットする commentCount
	 */
	public void setCommentCount(long commentCount) {
		this.commentCount = commentCount;
	}

	/**
	 * @return likeCount
	 */
	public long getLikeCount() {
		return likeCount;
	}

	/**
	 * @param likeCount セットする likeCount
	 */
	public void setLikeCount(long likeCount) {
		this.likeCount = likeCount;
	}

	/**
	 * @return the followingFlg
	 */
	public boolean isFollowingFlg() {
		return followingFlg;
	}

	/**
	 * @param followingFlg the followingFlg to set
	 */
	public void setFollowingFlg(boolean followingFlg) {
		this.followingFlg = followingFlg;
	}

	/**
	 * @return the topImage
	 */
	public ImageHeaderDO getTopImage() {
		if( images == null || images.isEmpty() )
			return null;
		return images.get(0);
	}

	/**
	 * @return productMaster
	 */
	public ProductMasterDO getProductMaster() {
		return productMaster;
	}

	/**
	 * @param productMaster セットする productMaster
	 */
	public void setProductMaster(ProductMasterDO productMaster) {
		this.productMaster = productMaster;
	}
	/**
	 * @return 回答日
	 */
	public String getPostDateTime(){
		return String.valueOf(questionAnswer.getPostDate().getTime());
	}

	public PurchaseProductDO getPurchaseProduct() {
		return purchaseProduct;
	}

	public void setPurchaseProduct(PurchaseProductDO purchaseProduct) {
		this.purchaseProduct = purchaseProduct;
	}

	public List<ImageHeaderDO> getImages() {
		return images;
	}

	public void setImages(List<ImageHeaderDO> images) {
		this.images = images;
	}

	public long getVotingCountYes() {
		return votingCountYes;
	}

	public void setVotingCountYes(long votingCountYes) {
		this.votingCountYes = votingCountYes;
	}

	public long getVotingCountNo() {
		return votingCountNo;
	}

	public void setVotingCountNo(long votingCountNo) {
		this.votingCountNo = votingCountNo;
	}
	
	public long getVotingCount() {
		return votingCountYes + votingCountNo;
	}

	public int getLikePrefixType() {
		return likePrefixType;
	}

	public void setLikePrefixType(int likePrefixType) {
		this.likePrefixType = likePrefixType;
	}

	public int getLikeMessageType() {
		return likeMessageType;
	}

	public void setLikeMessageType(int likeMessageType) {
		this.likeMessageType = likeMessageType;
	}

	public List<String> getLikeUserNames() {
		return likeUserNames;
	}

	public void setLikeUserNames(List<String> likeUserNames) {
		this.likeUserNames = likeUserNames;
	}

	public SearchResult<CommentSetVO> getComments() {
		return comments;
	}

	public void setComments(SearchResult<CommentSetVO> comments) {
		this.comments = comments;
	}

	public long getCommentViewRemainingCount() {
		return commentViewRemainingCount;
	}

	public void setCommentViewRemainingCount(long commentViewRemainingCount) {
		this.commentViewRemainingCount = commentViewRemainingCount;
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

	public boolean isAnswerFlg() {
		return answerFlg;
	}

	public void setAnswerFlg(boolean answerFlg) {
		this.answerFlg = answerFlg;
	}

	public long getAnswerCount() {
		return answerCount;
	}

	public void setAnswerCount(long answerCount) {
		this.answerCount = answerCount;
	}

	public String getGhbf() {
		return ghbf;
	}

	public void setGhbf(String ghbf) {
		this.ghbf = ghbf;
	}
}
