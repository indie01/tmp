/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import java.util.List;

import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;

/**
 * ニュースフィードのビューオブジェクトです。
 * @author kamiike
 *
 */
public class NewsFeedVO extends ContentBaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = 5616519036974932778L;

	/**
	 * アクション履歴です。
	 */
	private ActionHistoryDO actionHistory;

	/**
	 * 購入商品情報です。
	 */
	private PurchaseProductDO purchaseProduct;
	
	/**
	 * ログインユーザーの購入商品かどうか
	 */
	private PurchaseProductDO loginUserPurchaseProduct;
	
	/**
	 * コメント数です。
	 */
	private long commentCount;
	
	/**
	 * レビューコメント表示数です。
	 */
	private long commentViewRemainingCount;

	/**
	 * いいね数です。
	 */
	private long likeCount;
	
    /**
     * いいね済みかどうかです。
     */
    private boolean likeFlg;

	/**
	 * 回答者数です。
	 */
	private long answerCount;
	
	/**
	 * 回答済みがどうかです。
	 */
	private boolean answerFlg;

	/**
	 * フォロー済みかどうかです。
	 */
	private boolean followingFlg;

	
	/**
	 * 画像一覧です。
	 */
	private List<ImageHeaderDO> images;

	/**
	 * サブコンテンツ（回答に対する質問など）の画像一覧です。
	 */
	private List<ImageHeaderDO> subContentImages;

	/**
	 * 画像ヘッダーリストです。
	 */
	private List<ImageHeaderDO> imageHeaders;
	
	/**
	 * 参考になった「はい」の数です。
	 */
	private long votingCountYes;
	/**
	 * 参考になった「いいえ」の数です。
	 */
	private long votingCountNo;
	
	/**
	 * 同じ商品の他のレビュー数
	 */
	private long otherReviewCount;
	
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
	 * 自身のアクションかどうか？
	 */
	private boolean myActionFlg;
	
	/**
	 * @return actionHistory
	 */
	public ActionHistoryDO getActionHistory() {
		return actionHistory;
	}

	/**
	 * @param actionHistory セットする actionHistory
	 */
	public void setActionHistory(ActionHistoryDO actionHistory) {
		this.actionHistory = actionHistory;
	}

	public PurchaseProductDO getPurchaseProduct() {
		return purchaseProduct;
	}

	public void setPurchaseProduct(PurchaseProductDO purchaseProduct) {
		this.purchaseProduct = purchaseProduct;
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
	 * @return actionHistoryType
	 */
	public ActionHistoryType getActionHistoryType(){
		return actionHistory.getActionHistoryType();
	}

	/**
	 * @return actionHistoryType
	 */
	public String getNewsFeedId(){
		return String.valueOf(actionHistory.getActionTime().getTime());
	}

	/**
	 * @return topImage
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

	public long getCommentViewRemainingCount() {
		return commentViewRemainingCount;
	}

	public void setCommentViewRemainingCount(long commentViewRemainingCount) {
		this.commentViewRemainingCount = commentViewRemainingCount;
	}

	public long getOtherReviewCount() {
		return otherReviewCount;
	}

	public void setOtherReviewCount(long otherReviewCount) {
		this.otherReviewCount = otherReviewCount;
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

	public boolean isAnswerFlg() {
		return answerFlg;
	}

	public void setAnswerFlg(boolean answerFlg) {
		this.answerFlg = answerFlg;
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

	public boolean isMyActionFlg() {
		return myActionFlg;
	}

	public void setMyActionFlg(boolean myActionFlg) {
		this.myActionFlg = myActionFlg;
	}
	
	@Override
	public boolean isPostImmediatelyAfter() {
		if( actionHistory == null || ( actionHistory.getReview() == null && actionHistory.getQuestion() == null ))
			return false;
		actionHistory.getActionTime();
		return checkPostImmediatelyAfter(null);
	}

	public List<ImageHeaderDO> getSubContentImages() {
		return subContentImages;
	}

	public void setSubContentImages(List<ImageHeaderDO> subContentImages) {
		this.subContentImages = subContentImages;
	}
}
