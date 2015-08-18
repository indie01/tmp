package com.kickmogu.yodobashi.community.mapreduce.job;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.BeanTestHelper;
import com.kickmogu.lib.core.exception.CommonSystemException;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.utils.AopUtil;
import com.kickmogu.lib.hadoop.hbase.HBaseContainer;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.impl.ExternalEntityHBaseTemplate;
import com.kickmogu.lib.hadoop.hbase.impl.HBaseTemplate;
import com.kickmogu.lib.hadoop.hbase.impl.TimestampConfigurableProcessContextHolderImpl;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseTableMeta;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.mapreduce.job.CommunityHBaseCleanupJob;
import com.kickmogu.yodobashi.community.resource.domain.SlipHeaderDO;

@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/serviceContext.xml")
public class CommunityHBaseCleanupJobTest {
	
	@Autowired  @Qualifier("default")
	private HBaseOperations hbaseOperations;

	@Autowired @Qualifier("MySite")
	private HBaseContainer hbaseContainer;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	private TimestampConfigurableProcessContextHolderImpl processContextHolderImpl = new TimestampConfigurableProcessContextHolderImpl();
	
	@Before
	public void setup() {
		HBaseTemplate template = (HBaseTemplate)((ExternalEntityHBaseTemplate)AopUtil.getTargetObject(hbaseOperations, HBaseOperations.class)).getOrgOperations();
		template.setProcessContextHolder(processContextHolderImpl);
		hbaseContainer.getAdministrator().dropTable(SlipHeaderDO.class);
		hbaseContainer.getAdministrator().createTable(SlipHeaderDO.class);
	}
	
	@Test
	public void test2() throws Exception {
		long baseTimestamp = System.currentTimeMillis();
		processContextHolderImpl.setTimestamp(baseTimestamp-2);
		hbaseOperations.save(SlipHeaderDO.class, BeanTestHelper.createList(SlipHeaderDO.class,
			"id,outerCustomerId,slipNo,orderEntryType,deliverType,orderEntryDate,effectiveSlipType",
			"11,1000000001,1000000001,A,1,2011-01-01 00:00:01,1",
			"12,1000000002,1000000002,B,2,2011-01-01 00:00:02,1",
			"13,1000000003,1000000003,B,3,2011-01-01 00:00:03,1",
			"14,1000000004,1000000004,B,4,2011-01-01 00:00:04,1"
		));
		
		assertEquals(4, hbaseOperations.scanAll(SlipHeaderDO.class).size());
		
		processContextHolderImpl.setTimestamp(baseTimestamp-1);
		hbaseOperations.deleteByKeys(SlipHeaderDO.class, String.class, Lists.newArrayList("11"));
		
		processContextHolderImpl.setTimestamp(baseTimestamp);
		hbaseOperations.deleteByKeys(SlipHeaderDO.class, String.class, Lists.newArrayList("12"));
		
		processContextHolderImpl.setTimestamp(baseTimestamp+1);
		hbaseOperations.deleteByKeys(SlipHeaderDO.class, String.class, Lists.newArrayList("13"));
		
		assertEquals(1, hbaseOperations.scanAll(SlipHeaderDO.class).size());
		assertEquals("14", hbaseOperations.scanAll(SlipHeaderDO.class).get(0).getId());
		assertKeys("11","12","13", "14");
		CommunityHBaseCleanupJob.execute(applicationContext, new String[]{
			"-s", Long.valueOf(baseTimestamp).toString(),
			"-t", hbaseContainer.getMeta().getTableMeta(SlipHeaderDO.class).getTableName(),
			"-f", "cf",
			"-Dio.sort.mb=8"
		});

		
		assertEquals(1, hbaseOperations.scanAll(SlipHeaderDO.class).size());
		assertEquals("14", hbaseOperations.scanAll(SlipHeaderDO.class).get(0).getId());
		assertKeys("13", "14");

	}	
	
	@Test
	public void test() throws Exception {
		long baseTimestamp = System.currentTimeMillis();

		processContextHolderImpl.setTimestamp(baseTimestamp-2);
		hbaseOperations.save(SlipHeaderDO.class, BeanTestHelper.createList(SlipHeaderDO.class,
			"id,outerCustomerId,slipNo,orderEntryType,deliverType,orderEntryDate,effectiveSlipType",
			"11,1000000001,1000000001,A,1,2011-01-01 00:00:01,1"
		));
		
		processContextHolderImpl.setTimestamp(baseTimestamp-1);
		hbaseOperations.save(SlipHeaderDO.class, BeanTestHelper.createList(SlipHeaderDO.class,
			"id,outerCustomerId,slipNo,orderEntryType,deliverType,orderEntryDate,effectiveSlipType",
			"11,1000000011,2000000001,A,1,2011-01-01 00:00:01,1"
		), Path.includeProp("id,outerCustomerId,slipNo"));

		processContextHolderImpl.setTimestamp(baseTimestamp);
		hbaseOperations.save(SlipHeaderDO.class, BeanTestHelper.createList(SlipHeaderDO.class,
			"id,outerCustomerId,slipNo,orderEntryType,deliverType,orderEntryDate,effectiveSlipType",
			"11,1000000011,3000000001,A,1,2011-01-01 00:00:01,1"
		), Path.includeProp("id,slipNo"));		
		
		
		assertEquals("1000000011", hbaseOperations.load(SlipHeaderDO.class, "11").getOuterCustomerId());
		assertEquals("3000000001", hbaseOperations.load(SlipHeaderDO.class, "11").getSlipNo());
		assertVersionNum("11", "outerCustomerId", 2);
		assertVersionNum("11", "slipNo", 3);
		
		CommunityHBaseCleanupJob.execute(applicationContext, new String[]{
			"-s", Long.valueOf(baseTimestamp).toString(),
			"-t", hbaseContainer.getMeta().getTableMeta(SlipHeaderDO.class).getTableName(),
			"-f", "cf",
			"-Dio.sort.mb=8"
		});
		assertEquals("1000000011", hbaseOperations.load(SlipHeaderDO.class, "11").getOuterCustomerId());
		assertEquals("3000000001", hbaseOperations.load(SlipHeaderDO.class, "11").getSlipNo());
		assertVersionNum("11", "outerCustomerId", 1);
		assertVersionNum("11", "slipNo", 1);
	}
	
	private void assertVersionNum(String key, String columnName, int versionNum) {
		

		HBaseTableMeta tableMeta = hbaseContainer.getMeta().getTableMeta(SlipHeaderDO.class);
		HTableInterface table = hbaseContainer.getPlaneHTablePool().getTable(tableMeta.getTableNameAsBytes());
		Get get = new Get(Bytes.toBytes(key));
		try {
			get.setMaxVersions(64);
			Result result = table.get(get);
			assertEquals(versionNum, result.getColumn(tableMeta.getDeleteFlgColumnFamilyMeta().getColumnFamilyNameAsBytes(), Bytes.toBytes(columnName)).size());
		} catch (IOException e) {
			e.printStackTrace();
			throw new CommonSystemException(e);
		} finally {
			try {
				table.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new CommonSystemException(e);
			}
		}

	}

	private void assertKeys(String... ids) {
		

		HBaseTableMeta tableMeta = hbaseContainer.getMeta().getTableMeta(SlipHeaderDO.class);
		HTableInterface table = hbaseContainer.getPlaneHTablePool().getTable(tableMeta.getTableNameAsBytes());
		Scan scan = new Scan();
		ResultScanner scanner = null;
		int count = 0;
		try {
			scanner = table.getScanner(scan);
			for (Result result : scanner) {
				assertEquals(ids[count], Bytes.toString(result.getRow()));
				count++;
			}
			assertEquals(ids.length, count);
		} catch (IOException th) {
			th.printStackTrace();
			throw new CommonSystemException(th);
		} finally {
			if (scanner != null) scanner.close();
		}
	}

}
