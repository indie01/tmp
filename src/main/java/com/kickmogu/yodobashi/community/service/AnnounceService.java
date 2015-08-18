package com.kickmogu.yodobashi.community.service;


public interface AnnounceService {

	public boolean isAgreement(String communityUserId);
	
	public void registAgreement(String communityUserId);
}
