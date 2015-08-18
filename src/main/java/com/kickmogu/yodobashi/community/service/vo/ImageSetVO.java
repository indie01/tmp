/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import java.util.List;

import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;

/**
 * 商品に対する投稿画像のサマリビューオブジェクトです。
 * @author kamiike
 */
public class ImageSetVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = -5215162367251158159L;

	/**
	 * 画像です。
	 */
	private ImageHeaderDO imageHeader;
	
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
	 * 複数の関連画像が存在するかどうかです。
	 */
	private boolean hasOtherImages;
	
	/**
	 * 改竄チェック用
	 */
	private String ghbf;
	
	/**
	 * 購入商品情報
	 */
	private PurchaseProductDO purchaseProduct;
	
	/**
	 * ログインユーザーの購入商品かどうか
	 */
	private PurchaseProductDO loginUserPurchaseProduct;

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

	public CommunityUserDO getCommunityUser() {
		return imageHeader.getOwnerCommunityUser();
	}

	/**
	 * @return contentId
	 */
	public String getContentId() {
		return imageHeader.getContentsId();
	}

	/**
	 * @return contentType
	 */
	public PostContentType getContentType() {
		return imageHeader.getPostContentType();
	}

	/**
	 * @return imageHeader
	 */
	public ImageHeaderDO getImageHeader() {
		return imageHeader;
	}

	/**
	 * @param imageHeader セットする imageHeader
	 */
	public void setImageHeader(ImageHeaderDO imageHeader) {
		this.imageHeader = imageHeader;
	}
	
	/**
	 * @return commentFlg
	 */
	public boolean isCommentFlg() {
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
	 * @return commentViewRemainingCount
	 */
	public long getCommentViewRemainingCount() {
		return commentViewRemainingCount;
	}

	/**
	 * @param commentViewRemainingCount セットする commentViewRemainingCount
	 */
	public void setCommentViewRemainingCount(long commentViewRemainingCount) {
		this.commentViewRemainingCount = commentViewRemainingCount;
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
	 * @return hasOtherImages
	 */
	public boolean isHasOtherImages() {
		return hasOtherImages;
	}

	/**
	 * @param hasOtherImages セットする hasOtherImages
	 */
	public void setHasOtherImages(boolean hasOtherImages) {
		this.hasOtherImages = hasOtherImages;
	}

	/**
	 * @return actionHistoryType
	 */
	public String getPostDateTime(){
		return String.valueOf(imageHeader.getPostDate().getTime());
	}

	/**
	 * @return likePrefixType
	 */
	public int getLikePrefixType() {
		return likePrefixType;
	}

	/**
	 * @param likePrefixType セットする likePrefixType
	 */
	public void setLikePrefixType(int likePrefixType) {
		this.likePrefixType = likePrefixType;
	}

	/**
	 * @return likeMessageType
	 */
	public int getLikeMessageType() {
		return likeMessageType;
	}

	/**
	 * @param likeMessageType セットする likeMessageType
	 */
	public void setLikeMessageType(int likeMessageType) {
		this.likeMessageType = likeMessageType;
	}

	/**
	 * @return likeUserNames
	 */
	public List<String> getLikeUserNames() {
		return likeUserNames;
	}

	/**
	 * @param likeUserNames セットする likeUserNames
	 */
	public void setLikeUserNames(List<String> likeUserNames) {
		this.likeUserNames = likeUserNames;
	}

	/**
	 * @return comments
	 */
	public SearchResult<CommentSetVO> getComments() {
		return comments;
	}

	/**
	 * @param comments セットする comments
	 */
	public void setComments(SearchResult<CommentSetVO> comments) {
		this.comments = comments;
	}

	
	/**
	 * @return ghbf
	 */
	public String getGhbf() {
		return ghbf;
	}

	/**
	 * @param comments セットする ghbf
	 */
	public void setGhbf(String ghbf) {
		this.ghbf = ghbf;
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
	
	public String getRelationContentId(){
		if( PostContentType.REVIEW.equals(imageHeader.getPostContentType()) ){
			if( imageHeader.getReview() != null ){
				return imageHeader.getReview().getReviewId();
			}
		}else if( PostContentType.QUESTION.equals(imageHeader.getPostContentType())){
			if(imageHeader.getQuestion() != null ){
				return imageHeader.getQuestion().getQuestionId();
			}
		}else if(PostContentType.ANSWER.equals(imageHeader.getPostContentType())){
			if(imageHeader.getQuestionAnswer() != null ){
				return imageHeader.getQuestionAnswer().getQuestionAnswerId();
			}
		}else if( PostContentType.IMAGE_SET.equals(imageHeader.getPostContentType()) ){
			return imageHeader.getImageSetId();
		}
		
		return null;
	}
	
	public ImageTargetType getImageTargetType(){
		if( PostContentType.REVIEW.equals(imageHeader.getPostContentType()) ){
			return ImageTargetType.REVIEW;
		}else if( PostContentType.QUESTION.equals(imageHeader.getPostContentType())){
			return ImageTargetType.QUESTION;
		}else if( PostContentType.ANSWER.equals(imageHeader.getPostContentType())){
			return ImageTargetType.QUESTION_ANSWER;
		}else if( PostContentType.IMAGE_SET.equals(imageHeader.getPostContentType()) ){
			return ImageTargetType.IMAGE;
		}
		
		return null;
	}
}
