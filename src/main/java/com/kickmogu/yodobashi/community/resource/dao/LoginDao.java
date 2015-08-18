package com.kickmogu.yodobashi.community.resource.dao;

import com.kickmogu.yodobashi.community.resource.domain.LoginDO;

public interface LoginDao {

	void removeExpired();
	
	public LoginDO loadByAutoId(String autoId);
	
	public void save(LoginDO login);
	
	public void removeLogin(String autoId);

}
