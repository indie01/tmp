package com.kickmogu.yodobashi.community.resource.domain;


import com.kickmogu.yodobashi.community.resource.domain.constants.HeaderType;

public class UrlMakerDO {
	
	private Boolean ssl;
	private Boolean adult;
	private String returnUrl;
	private HeaderType headerType;
	private String itemSearchUrl;
	
	public UrlMakerDO(
			HeaderType headerType,
			Boolean ssl, 
			Boolean adult, 
			String returnUrl,
			String itemSearchUrl) {
		this.ssl = ssl;
		this.adult = adult;
		this.returnUrl = returnUrl;
		this.headerType = headerType;
		this.itemSearchUrl = itemSearchUrl;
	}

	public Boolean getSsl() {
		return ssl;
	}

	public void setSsl(Boolean ssl) {
		this.ssl = ssl;
	}

	public Boolean getAdult() {
		return adult;
	}

	public void setAdult(Boolean adult) {
		this.adult = adult;
	}

	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}

	public HeaderType getHeaderType() {
		return headerType;
	}

	public void setHeaderType(HeaderType headerType) {
		this.headerType = headerType;
	}

	public String getItemSearchUrl() {
		return itemSearchUrl;
	}

	public void setItemSearchUrl(String itemSearchUrl) {
		this.itemSearchUrl = itemSearchUrl;
	}
	
}
