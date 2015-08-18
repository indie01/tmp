package com.kickmogu.yodobashi.community.resource.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.BeanTestHelper;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.domain.ReceiptHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipHeaderDO;

@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/resourceContext.xml")
public class OrderDaoTest {
	
	@Autowired private OrderDao orderDao;

	@Autowired  @Qualifier("default")
	private SolrOperations solrOperations;

	@Before
	public void setup() {
		solrOperations.deleteAll(SlipHeaderDO.class);
		solrOperations.deleteAll(ReceiptHeaderDO.class);
	}
	
	@Test
	public void test01() {
		
		solrOperations.save(SlipHeaderDO.class, BeanTestHelper.createList(SlipHeaderDO.class,
			"outerCustomerId,slipNo,modifyDateTime,effectiveSlipType",
			//　1件のみ(有効)
			"1000000001,01,2011-01-01 00:00:01,1",
			//　1件のみ(無効)
			"1000000002,01,2011-01-01 00:00:01,2",
			// 有効無効混在(有効)
			"1000000003,01,2011-01-01 00:00:01,2",
			"1000000003,02,2011-01-01 00:00:02,2",
			"1000000003,03,2011-01-01 00:00:03,1",
			// 有効無効混在(無効)
			"1000000004,01,2011-01-01 00:00:01,1",
			"1000000004,02,2011-01-01 00:00:02,1",
			"1000000004,03,2011-01-01 00:00:03,2"
		));
		Map<String, Boolean> isEnableMap = orderDao.checkCommunityUserIsEnableFromSlipAndReceipt((List<String>)Lists.newArrayList(
			"1000000001","1000000002","1000000003","1000000004","9999999999"
		));
		
		assertEquals(4, isEnableMap.size());
		assertTrue(isEnableMap.get("1000000001"));
		assertFalse(isEnableMap.get("1000000002"));
		assertTrue(isEnableMap.get("1000000003"));
		assertFalse(isEnableMap.get("1000000004"));
	}

	@Test
	public void test02() {
		
		solrOperations.save(ReceiptHeaderDO.class, BeanTestHelper.createList(ReceiptHeaderDO.class,
			"outerCustomerId,receiptNo,modifyDateTime,effectiveSlipType",
			//　1件のみ(有効)
			"1000000001,01,2011-01-01 00:00:01,1",
			//　1件のみ(無効)
			"1000000002,01,2011-01-01 00:00:01,2",
			// 有効無効混在(有効)
			"1000000003,01,2011-01-01 00:00:01,2",
			"1000000003,02,2011-01-01 00:00:02,2",
			"1000000003,03,2011-01-01 00:00:03,1",
			// 有効無効混在(無効)
			"1000000004,01,2011-01-01 00:00:01,1",
			"1000000004,02,2011-01-01 00:00:02,1",
			"1000000004,03,2011-01-01 00:00:03,2"
		));
		Map<String, Boolean> isEnableMap = orderDao.checkCommunityUserIsEnableFromSlipAndReceipt((List<String>)Lists.newArrayList(
			"1000000001","1000000002","1000000003","1000000004","9999999999"
		));
		
		assertEquals(4, isEnableMap.size());
		assertTrue(isEnableMap.get("1000000001"));
		assertFalse(isEnableMap.get("1000000002"));
		assertTrue(isEnableMap.get("1000000003"));
		assertFalse(isEnableMap.get("1000000004"));
	}
	
	@Test
	public void test03() {
		
		solrOperations.save(ReceiptHeaderDO.class, BeanTestHelper.createList(ReceiptHeaderDO.class,
			"outerCustomerId,receiptNo,modifyDateTime,effectiveSlipType",
			"1000000001,01,2011-01-01 00:00:01,2",
			"1000000002,01,2011-01-01 00:00:02,2",
			"1000000003,01,2011-01-01 00:00:01,1",
			"1000000004,01,2011-01-01 00:00:02,1"
		));
		solrOperations.save(ReceiptHeaderDO.class, BeanTestHelper.createList(ReceiptHeaderDO.class,
			"outerCustomerId,receiptNo,modifyDateTime,effectiveSlipType",
			"1000000001,01,2011-01-01 00:00:02,1",
			"1000000002,01,2011-01-01 00:00:01,1",
			"1000000003,01,2011-01-01 00:00:02,2",
			"1000000004,01,2011-01-01 00:00:01,2"
		));
		Map<String, Boolean> isEnableMap = orderDao.checkCommunityUserIsEnableFromSlipAndReceipt((List<String>)Lists.newArrayList(
			"1000000001","1000000002","1000000003","1000000004","9999999999"
		));
		
		assertEquals(4, isEnableMap.size());
		assertTrue(isEnableMap.get("1000000001"));
		assertFalse(isEnableMap.get("1000000002"));
		assertFalse(isEnableMap.get("1000000003"));
		assertTrue(isEnableMap.get("1000000004"));
	}
	
}
