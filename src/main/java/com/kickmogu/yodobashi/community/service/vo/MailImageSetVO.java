/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import java.util.List;

import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;

/**
 * 画像セットです。
 * @author kamiike
 *
 */
public class MailImageSetVO extends BaseVO {

	/**
	 *
	 */
	private static final long serialVersionUID = -5215162367251158159L;

	private ImageHeaderDO topImage;
	/**
	 * 画像ヘッダーリストです。
	 */
	private List<ImageHeaderDO> imageHeaders;

	public ImageHeaderDO getTopImage() {
		return topImage;
	}

	public void setTopImage(ImageHeaderDO topImage) {
		this.topImage = topImage;
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
}
