/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import java.util.List;

import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.VotingType;

/**
 * レビュー関連の表示情報を集めたビューオブジェクトです。
 * @author kamiike
 */
public class ReviewSetVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = -8491029107447013094L;

	/**
	 * レビューです。
	 */
	private ReviewDO review;

	
	/**
	 * レビュー画像一覧です。
	 */
	private List<ImageHeaderDO> images;

    /**
     * 商品マスター情報です。
     */
    private ProductMasterDO productMaster;

    /**
     * コメント済みかどうかです。
     */
    private boolean commentFlg;

    /**
     * いいね済みかどうかです。
     */
    private boolean likeFlg;

	/**
	 * レビューコメント数です。
	 */
	private long commentCount;
	
	/**
	 * レビューコメント表示数です。
	 */
	private long commentViewRemainingCount;

	/**
	 * レビューいいね数です。
	 */
	private long likeCount;

	/**
	 * ユーザーをフォロー済みかどうかです。
	 */
	private boolean followingUser;

	/**
	 * 購入商品情報です。
	 */
	private PurchaseProductDO purchaseProduct;
	
	/**
	 * 参考になったかどうかです。
	 */
	private VotingType votingType;
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
	 * 改竄チェック用
	 */
	private String ghbf;
	
	/**
	 * @return review
	 */
	public ReviewDO getReview() {
		return review;
	}

	/**
	 * @param review セットする review
	 */
	public void setReview(ReviewDO review) {
		this.review = review;
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
	 * @return actionHistoryType
	 */
	public String getPostDateTime(){
		return String.valueOf(review.getPostDate().getTime());
	}

	/**
	 * @return the topImage
	 */
	public ImageHeaderDO getTopImage() {
		if( images == null || images.isEmpty())
			return null;
		return images.get(0);
	}

	/**
	 * @return the followingUser
	 */
	public boolean isFollowingUser() {
		return followingUser;
	}

	/**
	 * @param followingUser the followingUser to set
	 */
	public void setFollowingUser(boolean followingUser) {
		this.followingUser = followingUser;
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

	public VotingType getVotingType() {
		return votingType;
	}

	public void setVotingType(VotingType votingType) {
		this.votingType = votingType;
	}

	public long getVotingCountYes() {
		return votingCountYes;
	}

	public void setVotingCountYes(long vortingCountYes) {
		this.votingCountYes = vortingCountYes;
	}

	public long getVotingCountNo() {
		return votingCountNo;
	}

	public void setVotingCountNo(long vortingCountNo) {
		this.votingCountNo = vortingCountNo;
	}
	
	public long getVotingCount() {
		return votingCountYes + votingCountNo;
	}

	public long getOtherReviewCount() {
		return otherReviewCount;
	}

	public void setOtherReviewCount(long otherReviewCount) {
		this.otherReviewCount = otherReviewCount;
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

	public String getGhbf() {
		return ghbf;
	}

	public void setGhbf(String ghbf) {
		this.ghbf = ghbf;
	}
	
}
