package com.kickmogu.yodobashi.community.mapreduce.job;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
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
import com.kickmogu.lib.core.utils.AopUtil;
import com.kickmogu.lib.core.utils.ThreadSafeDateFormat;
import com.kickmogu.lib.hadoop.hbase.HBaseContainer;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.impl.ExternalEntityHBaseTemplate;
import com.kickmogu.lib.hadoop.hbase.impl.HBaseTemplate;
import com.kickmogu.lib.hadoop.hbase.impl.TimestampConfigurableProcessContextHolderImpl;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseTableMeta;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.domain.SlipDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipHeaderDO;

@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/serviceContext.xml")
public class CommunityHBaseMultiCounterJobTest {

	@Autowired  @Qualifier("default")
	private HBaseOperations hbaseOperations;

	@Autowired @Qualifier("MySite")
	private HBaseContainer hbaseContainer;
	
	@Autowired
	private ApplicationContext applicationContext;
	
//	@Autowired @Qualifier("MySite")
//	private MapReduceConfig mapReduceConfig;
//	@Autowired
//	private CommunityMapReduceConfig mapReduceConfig;
	// "hdfs://kmcms04.kickmogu.com:54310"
	
	private TimestampConfigurableProcessContextHolderImpl processContextHolderImpl = new TimestampConfigurableProcessContextHolderImpl();
	
	private static final String DATE_FORMAT = "yyyyMMddHHmmss";

	private String tableNamePrefix;
	private String hdfsUrl = "hdfs://kmcms04.kickmogu.com:54310";

	@Before
	public void setup() {
		HBaseTemplate template = (HBaseTemplate)((ExternalEntityHBaseTemplate)AopUtil.getTargetObject(hbaseOperations, HBaseOperations.class)).getOrgOperations();
		template.setProcessContextHolder(processContextHolderImpl);
		hbaseContainer.getAdministrator().dropTable(SlipHeaderDO.class);
		hbaseContainer.getAdministrator().createTable(SlipHeaderDO.class);
		hbaseContainer.getAdministrator().dropTable(SlipDetailDO.class);
		hbaseContainer.getAdministrator().createTable(SlipDetailDO.class);
		tableNamePrefix = hbaseContainer.getMeta().getTableNamePrefix();
	}
	@Test
	public void testFilterDeleteAndTimestamp() throws Exception {
		long baseTimestamp = System.currentTimeMillis();
		processContextHolderImpl.setTimestamp(baseTimestamp-2);
		hbaseOperations.save(SlipHeaderDO.class, BeanTestHelper.createList(SlipHeaderDO.class,
			"id,outerCustomerId,slipNo,orderEntryType,deliverType,orderEntryDate,effectiveSlipType",
			"11,1000000001,1000000001,A,1,2011-01-01 00:00:01,1", // 論理削除
			"12,1000000002,1000000002,B,2,2011-01-01 00:00:02,1", // 論理削除
			"13,1000000003,1000000003,B,3,2011-01-01 00:00:03,1",
			"14,1000000004,1000000004,B,4,2011-01-01 00:00:04,1", // 論理削除(検索対象外)
			"15,1000000005,1000000005,B,5,2011-01-01 00:00:05,1"
		));
		processContextHolderImpl.setTimestamp(baseTimestamp+1);
		hbaseOperations.save(SlipHeaderDO.class, BeanTestHelper.createList(SlipHeaderDO.class,
			"id,outerCustomerId,slipNo,orderEntryType,deliverType,orderEntryDate,effectiveSlipType",
			"16,1000000006,1000000006,A,6,2011-01-01 00:00:06,1",  // 検索対象外のため検索されない
			"17,1000000007,1000000007,A,7,2011-01-01 00:00:07,1"  // 検索対象外のため検索されない
		));
		
		assertEquals(7, hbaseOperations.scanAll(SlipHeaderDO.class).size());
		
		processContextHolderImpl.setTimestamp(baseTimestamp-1);
		hbaseOperations.deleteByKeys(SlipHeaderDO.class, String.class, Lists.newArrayList("11"));
		
		processContextHolderImpl.setTimestamp(baseTimestamp);
		hbaseOperations.deleteByKeys(SlipHeaderDO.class, String.class, Lists.newArrayList("12"));
		
		// 論理削除されるがtimestamp指定で対象外の更新のため、カウントされる
		processContextHolderImpl.setTimestamp(baseTimestamp+1);
		hbaseOperations.deleteByKeys(SlipHeaderDO.class, String.class, Lists.newArrayList("14")); 

		assertEquals(4, hbaseOperations.scanAll(SlipHeaderDO.class).size());
		assertEquals("13", hbaseOperations.scanAll(SlipHeaderDO.class).get(0).getId());
		
		assertKeys("11","12","13", "14", "15", "16", "17");
		CommunityHBaseMultiCounterJob.execute(applicationContext, new String[]{
			"-s", Long.valueOf(baseTimestamp).toString(),
			"-t", hbaseContainer.getMeta().getTableMeta(SlipHeaderDO.class).getTableName(),
			"-Dio.sort.mb=8"
		});
		
		String filePath = "/tmp/hbaseMultiCounter/" + ThreadSafeDateFormat.format(DATE_FORMAT, new Date(baseTimestamp)) + "_count.txt/part-r-00000";
		List<String> list = readFile(filePath);
		assertEquals(tableNamePrefix + SlipHeaderDO.class.getSimpleName() + "\t3",list.get(0));
		assertEquals(tableNamePrefix + SlipHeaderDO.class.getSimpleName() + "_delete_row\t2",list.get(1));
	}	
	
	@Test
	public void testNomal() throws Exception {
		long baseTimestamp = System.currentTimeMillis();
		processContextHolderImpl.setTimestamp(baseTimestamp-2);
		hbaseOperations.save(SlipHeaderDO.class, BeanTestHelper.createList(SlipHeaderDO.class,
			"id,outerCustomerId,slipNo,orderEntryType,deliverType,orderEntryDate,effectiveSlipType",
			"11,1000000001,1000000001,A,1,2011-01-01 00:00:01,1", // 論理削除
			"12,1000000002,1000000002,B,2,2011-01-01 00:00:02,1", // 論理削除
			"13,1000000003,1000000003,B,3,2011-01-01 00:00:03,1",
			"14,1000000004,1000000004,B,4,2011-01-01 00:00:04,1", 
			"15,1000000005,1000000005,B,5,2011-01-01 00:00:05,1"
		));
		
		processContextHolderImpl.setTimestamp(baseTimestamp-1);
		hbaseOperations.deleteByKeys(SlipHeaderDO.class, String.class, Lists.newArrayList("11"));
		
		processContextHolderImpl.setTimestamp(baseTimestamp-1);
		hbaseOperations.deleteByKeys(SlipHeaderDO.class, String.class, Lists.newArrayList("12"));
		
		hbaseOperations.save(SlipDetailDO.class, BeanTestHelper.createList(SlipDetailDO.class,
			"outerCustomerId,slipNo,slipDetailNo,slipDetailCategory,janCode,effectiveNum,returnedNum,setCouponId,setParentDetailNo,salesRegistDetailType",
			"0000000001,1000000001,1,01,100000000011,11,21,2000000011,41,1"
		));
		CommunityHBaseMultiCounterJob.execute(applicationContext, new String[]{
			"-s", Long.valueOf(baseTimestamp).toString(),
			"-t", hbaseContainer.getMeta().getTableMeta(SlipHeaderDO.class).getTableName() + "," + hbaseContainer.getMeta().getTableMeta(SlipDetailDO.class).getTableName(),
			"-Dio.sort.mb=8"
		});
		
		String filePath = "/tmp/hbaseMultiCounter/" + ThreadSafeDateFormat.format(DATE_FORMAT, new Date(baseTimestamp)) + "_count.txt/part-r-00000";
		List<String> list = readFile(filePath);
		assertEquals(tableNamePrefix + SlipDetailDO.class.getSimpleName() + "\t1",list.get(0));
		assertEquals(tableNamePrefix + SlipDetailDO.class.getSimpleName() + "_delete_row\t0",list.get(1));
		assertEquals(tableNamePrefix + SlipHeaderDO.class.getSimpleName() + "\t3",list.get(2));
		assertEquals(tableNamePrefix + SlipHeaderDO.class.getSimpleName() + "_delete_row\t2",list.get(3));

	}
	
	private List<String> readFile(String file) throws IOException {
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(URI.create(hdfsUrl), conf);
		
		List<String> list = Lists.newArrayList();
		Text tableName = new Text();
		LongWritable num = new LongWritable();
		
		SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(file), conf);
		try {
			while (reader.next(tableName, num)) {
				list.add(tableName + "\t" + num.get());
			}
		} finally {
			reader.close();
		}
		return list;
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
