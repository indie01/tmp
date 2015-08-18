package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;
import java.util.List;

import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;

/**
 * 画像投稿用のクラス
 * domainクラスではないので注意
 * その他domainクラスと構造を合わせるために追加
 * @author sugimoto
 *
 */
public class ImageSetDO {
	
	private String imageSetId;
	
	private ProductDO product;
	
	private CommunityUserDO communityUser;
	
	private List<ImageHeaderDO> imageHeaders;
	
	private PurchaseProductDO purchaseProduct;
	
	private Date inputPurchaseProductDate;
	
	private ContentsStatus status;

	private Date registerDateTime;
	
	private Date modifyDateTime;

	public String getImageSetId() {
		return imageSetId;
	}

	public void setImageSetId(String imageSetId) {
		this.imageSetId = imageSetId;
	}

	public ProductDO getProduct() {
		return product;
	}

	public void setProduct(ProductDO product) {
		this.product = product;
	}

	public CommunityUserDO getCommunityUser() {
		return communityUser;
	}

	public void setCommunityUser(CommunityUserDO communityUser) {
		this.communityUser = communityUser;
	}

	public List<ImageHeaderDO> getImageHeaders() {
		return imageHeaders;
	}

	public void setImageHeaders(List<ImageHeaderDO> imageHeaders) {
		this.imageHeaders = imageHeaders;
	}

	public Date getInputPurchaseProductDate() {
		return inputPurchaseProductDate;
	}

	public void setInputPurchaseProductDate(Date inputPurchaseProductDate) {
		this.inputPurchaseProductDate = inputPurchaseProductDate;
	}

	public ContentsStatus getStatus() {
		return status;
	}

	public void setStatus(ContentsStatus status) {
		this.status = status;
	}

	public Date getPostDate() {
		if( imageHeaders != null && !imageHeaders.isEmpty()){
			return imageHeaders.get(0).getPostDate();
		}
		return null;
	}

	public PurchaseProductDO getPurchaseProduct() {
		return purchaseProduct;
	}

	public void setPurchaseProduct(PurchaseProductDO purchaseProduct) {
		this.purchaseProduct = purchaseProduct;
	}

	public Date getRegisterDateTime() {
		return registerDateTime;
	}

	public void setRegisterDateTime(Date registerDateTime) {
		this.registerDateTime = registerDateTime;
	}

	public Date getModifyDateTime() {
		return modifyDateTime;
	}

	public void setModifyDateTime(Date modifyDateTime) {
		this.modifyDateTime = modifyDateTime;
	}
	
	
}
