/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.domain.PointGrantEntryDO;
import com.kickmogu.yodobashi.community.resource.domain.PointGrantEntryExecuteStatusDO;
import com.kickmogu.yodobashi.community.resource.domain.PointGrantEntryExecuteStatusResponseDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewPointSpecialConditionValidateDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.CancelPointGrantType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointExchangeType;

/**
 * ポイント管理システム DAO のテストクラスです。
 * @author kamiike
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/jcclientContext.xml")
public class SimplePmsDaoTest {

	/**
	 * ポイント管理 DAO です。
	 */
	@Autowired @Qualifier("pms")
	private SimplePmsDao simplePmsDao;

	@Autowired @Qualifier("xi")
	private UniversalSessionManagerDao universalSessionManagerDao;

	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() {
	}

	/**
	 * 登録とキャンセルをテストします。
	 */
	@Test
	public void test() {

		String pointGrantRequestId = simplePmsDao.entryPointGrant(
				"0123456789",
				PointExchangeType.REVIEW,
				new Date(),
				10L, null);
		assertNotNull(pointGrantRequestId);
		simplePmsDao.cancelPointGrant(pointGrantRequestId, CancelPointGrantType.COMMUNITY_WITHDRAWAL);
		simplePmsDao.migratePointGrant("0123456789", PointExchangeType.REVIEW, new Date(), new Date(), 10L);
	}

	@Test
	public void test2() {
		assertEquals("0123456789", universalSessionManagerDao.loadOuterCustomerId("0123456789"));
	}
	
	@Test
	public void testFindMutablePointGrantEntry(){
		SearchResult<PointGrantEntryDO> result = simplePmsDao.findMutablePointGrantEntry(null, null, null, null, null);
		
		assertNotNull(result.getDocuments());
	}
	
	@Test
	public void testConfirmReviewPointSpecialCondition(){
		String externalCustomerIdClass = "0001";
		String externalCustomerId = "9bwQTktqL0";
		String[] specialConditionCodes = {
				"9000000015",
				"9000000014",
				"9000000099",
				"9000000002",
				"9000000006",
				"9000000012",
				"9000000011",
				"9000000005",
				"1000000003",
				"1000000001"
		};
		
		List<ReviewPointSpecialConditionValidateDO> result = simplePmsDao.confirmReviewPointSpecialCondition(externalCustomerIdClass, externalCustomerId, specialConditionCodes);
		
		assertNotNull(result);
		assertEquals(specialConditionCodes.length, result.size());
	}
	
	@Test
	public void testUpdatePointGrantEntryExecuteStatus(){
		List<PointGrantEntryExecuteStatusDO> changeStatusPointGrantEntries = new ArrayList<PointGrantEntryExecuteStatusDO>();
		
		changeStatusPointGrantEntries.add(new PointGrantEntryExecuteStatusDO("K0000000000000002221","12"));
		changeStatusPointGrantEntries.add(new PointGrantEntryExecuteStatusDO("K0000000000000002222","13"));
		changeStatusPointGrantEntries.add(new PointGrantEntryExecuteStatusDO("K0000000000000002224","92"));
		changeStatusPointGrantEntries.add(new PointGrantEntryExecuteStatusDO("K0000000000000002223","91"));
		changeStatusPointGrantEntries.add(new PointGrantEntryExecuteStatusDO("K0000000000000002224","22"));
		
		List<PointGrantEntryExecuteStatusResponseDO> result = simplePmsDao.updatePointGrantEntryExecuteStatus(changeStatusPointGrantEntries);
		
		assertEquals(changeStatusPointGrantEntries.size(), result.size());
		
	}
	
	@Test
	public void testIsService(){
		assertEquals(Boolean.TRUE, simplePmsDao.isService());
	}
}
