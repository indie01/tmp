/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;

/**
 * 画像単体の表示オブジェクトです。
 * @author kamiike
 *
 */
public class ImageDetailSetVO extends BaseVO {

	/**
	 *
	 */
	private static final long serialVersionUID = 6582388304376848968L;

	/**
	 * 画像ヘッダーです。
	 */
	private ImageHeaderDO imageHeader;

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
	 * 回答いいね数です。
	 */
	private long likeCount;

	//ポップアップで左（＜）、右（＞）を押した際に切り替えるコメントのHTML文字列
	private String commentContent;
	
	//ポップアップで左（＜）、右（＞）を押した際に切り替えるコメントのHTML文字列
	private String likeContent;
	
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
	 * @return commentContent
	 */
	public String getCommentContent() {
		return commentContent;
	}

	/**
	 * @param commentContent セットする commentContent
	 */
	public void setCommentContent(String s) {
		this.commentContent = s;
	}

	/**
	 * @return likeContent
	 */
	public String getLikeContent() {
		return likeContent;
	}

	/**
	 * @param likeContent セットする likeContent
	 */
	public void setLikeContent(String likeContent) {
		this.likeContent = likeContent;
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
