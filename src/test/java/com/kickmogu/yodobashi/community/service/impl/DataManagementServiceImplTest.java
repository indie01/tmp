package com.kickmogu.yodobashi.community.service.impl;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.lib.core.utils.DumpUtil;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.domain.DataManagementTableDO;
import com.kickmogu.yodobashi.community.service.DataManagementService;
@Ignore
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/serviceContext.xml")
public class DataManagementServiceImplTest {
	
	@Autowired
	private DataManagementService dataManagementService;
	
	@Test
	public void test() {
		assertTrue(dataManagementService.getDataManagement().getTableMap().size() > 0);
	}
	
	//2fCGMu_0000000001_1000000001
	@SuppressWarnings("rawtypes")
	@Test @Ignore
	public void test2() {
		DataManagementTableDO table = dataManagementService.getManagementTableByDoName("SlipHeaderDO");
		System.out.println(DumpUtil.dumpBean(dataManagementService.getByHBase(table, "2fCGMu_0000000001_1000000001")));
	}

}
