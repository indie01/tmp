package com.kickmogu.yodobashi.community.service.vo;

public class ImageFileVO extends BaseVO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8491865114077704866L;

	private String imageId;
	
	private String imageUrl;
	
	private String editTypeCode;
	
	private String errorMessage;

	public ImageFileVO() {
	}
	
	public ImageFileVO(
			String imageId, 
			String imageUrl, 
			String editTypeCode,
			String errorMessage) {
		super();
		this.imageId = imageId;
		this.imageUrl = imageUrl;
		this.editTypeCode = editTypeCode;
		this.errorMessage = errorMessage;
	}



	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getEditTypeCode() {
		return editTypeCode;
	}

	public void setEditTypeCode(String editTypeCode) {
		this.editTypeCode = editTypeCode;
	}
	
}
