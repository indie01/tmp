package com.kickmogu.yodobashi.community.resource.domain;

import com.kickmogu.lib.core.resource.domain.BaseDO;

public class ShoppingCartDO extends BaseDO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7158571817834385611L;
	
	private String sku;
	
	private String cartTag;

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getCartTag() {
		return cartTag;
	}

	public void setCartTag(String cartTag) {
		this.cartTag = cartTag;
	}
	
}
