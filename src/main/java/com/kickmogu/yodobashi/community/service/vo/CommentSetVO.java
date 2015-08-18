/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommentTargetType;

/**
 * コメント関連の表示情報を集めたビューオブジェクトです。
 * @author kamiike
 */
public class CommentSetVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = -2097631983873651925L;

	/**
	 * コメント
	 */
	private CommentDO comment;

	/**
	 * ログインユーザーのコメントかどうかです。
	 */
	private boolean commentFlg;
	
	/**
	 * 改竄チェック用
	 */
	private String ghbf;
	
	/**
	 * 商品です。
	 */
	private ProductDO product;

	/**
	 * レビューです。
	 */
	private ReviewDO review;

	/**
	 * 画像情報
	 */
	private ImageHeaderDO imageHeader;
	/**
	 * 質問回答です。
	 */
	private QuestionAnswerDO questionAnswer;

	/**
	 * @return comment
	 */
	public CommentDO getComment() {
		return comment;
	}

	/**
	 * @param comment セットする comment
	 */
	public void setComment(CommentDO comment) {
		this.comment = comment;
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
	 * @return 回答日
	 */
	public String getPostDateTime(){
		return String.valueOf(comment.getPostDate().getTime());
	}

	public String getGhbf() {
		return ghbf;
	}

	public void setGhbf(String ghbf) {
		this.ghbf = ghbf;
	}

	public ProductDO getProduct() {
		return product;
	}

	public void setProduct(ProductDO product) {
		this.product = product;
	}

	public ReviewDO getReview() {
		return review;
	}

	public void setReview(ReviewDO review) {
		this.review = review;
	}

	public ImageHeaderDO getImageHeader() {
		return imageHeader;
	}

	public void setImageHeader(ImageHeaderDO imageHeader) {
		this.imageHeader = imageHeader;
	}

	public QuestionAnswerDO getQuestionAnswer() {
		return questionAnswer;
	}

	public void setQuestionAnswer(QuestionAnswerDO questionAnswer) {
		this.questionAnswer = questionAnswer;
	}
	
	public CommentTargetType getCommentTargetType(){
		if(comment == null)
			return null;
		
		if( comment.getReview() != null && comment.getReview().getReviewId() != null ){
			return  CommentTargetType.REVIEW;
		}else if( comment.getQuestionAnswer() != null && comment.getQuestionAnswer().getQuestionAnswerId() != null ){
			return  CommentTargetType.QUESTION_ANSWER;
		}else if( comment.getImageHeader() != null && comment.getImageHeader().getImageId() != null ){
			return  CommentTargetType.IMAGE;
		}
		
		return null;
	}
	
	public String getRelationContentId(){
		if(comment == null)
			return null;
		
		if( comment.getReview() != null && comment.getReview().getReviewId() != null ){
			return  comment.getReview().getReviewId();
		}else if( comment.getQuestionAnswer() != null && comment.getQuestionAnswer().getQuestionAnswerId() != null ){
			return  comment.getQuestionAnswer().getQuestionAnswerId();
		}else if( comment.getImageHeader() != null && comment.getImageHeader().getImageId() != null ){
			return comment.getImageHeader().getImageId();
		}
		return null;
	}
}
