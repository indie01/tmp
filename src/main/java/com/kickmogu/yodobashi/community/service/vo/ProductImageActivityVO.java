/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import java.util.Date;
import java.util.List;

import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;

/**
 * 商品に対して投稿アクティビティのビューオブジェクトです。
 * @author kamiike
 */
public class ProductImageActivityVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = 7954962871876421635L;

	/**
	 * 商品情報です。
	 */
	private ProductDO product;

	/**
	 * 画像リストです。
	 */
	private List<ImageHeaderDO> imageHeaders;

	/**
	 * コメント数です。
	 */
	private long commentCount;

	/**
	 * いいね数です。
	 */
	private long likeCount;

	/**
	 * 投稿日時です。
	 */
	private Date postDate;

	/**
	 * @return product
	 */
	public ProductDO getProduct() {
		return product;
	}

	/**
	 * @param product セットする product
	 */
	public void setProduct(ProductDO product) {
		this.product = product;
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

}
