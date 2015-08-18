package com.kickmogu.yodobashi.community.resource.domain;

import org.msgpack.annotation.Message;

import com.kickmogu.lib.core.resource.domain.BaseDO;

@Message
public class SaveImageDO extends BaseDO {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1627178866236887770L;
	
	public String imageId;
	
	public String caption;
	
	public SaveImageDO() {
	}
	
	public SaveImageDO(String imageId, String caption) {
		this.imageId = imageId;
		this.caption = caption;
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}
	
	
}
