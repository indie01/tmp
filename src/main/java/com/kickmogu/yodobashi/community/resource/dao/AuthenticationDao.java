package com.kickmogu.yodobashi.community.resource.dao;

import java.util.Map;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.ValidateAuthSessionDO;


public interface AuthenticationDao {

	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="XI通信なのでテスト対象外")
	public boolean isValidateAuthSession(String authKey);
	
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="XI通信なのでテスト対象外")
	public ValidateAuthSessionDO validateAuthSessionV2(String authKey, Map<String, String> params);
}
