package com.kickmogu.yodobashi.community.resource.domain.constants;

public enum HeaderType {
	UNIVERSAL("universal"),
	COMMON("common"),
	IMPORTURL("import"), // 現状使わない方向
	PATH("path");

	public String code;
	
	private HeaderType(String code){
		this.code = code;
	}
}
