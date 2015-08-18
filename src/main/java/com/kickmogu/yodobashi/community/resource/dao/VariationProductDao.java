package com.kickmogu.yodobashi.community.resource.dao;

import java.util.Set;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;

public interface VariationProductDao {

	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="IDを払い出すだけなのでテスト対象外")
	public Set<String> findVariationProduct(String sku, String targetDateTime);
	
}
