package com.kickmogu.yodobashi.community.mapreduce.job;

import static com.kickmogu.lib.core.BeanTestHelper.*;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.lib.core.BeanTestHelper;
import com.kickmogu.lib.core.ssh.SshUtils;
import com.kickmogu.lib.core.utils.AopUtil;
import com.kickmogu.lib.hadoop.hbase.HBaseContainer;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.backup.HBaseBackupService;
import com.kickmogu.lib.hadoop.hbase.impl.ExternalEntityHBaseTemplate;
import com.kickmogu.lib.hadoop.hbase.impl.HBaseTemplate;
import com.kickmogu.lib.hadoop.hbase.impl.TimestampConfigurableProcessContextHolderImpl;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.mapreduce.job.CommunityHBaseRestoreJob;
import com.kickmogu.yodobashi.community.resource.domain.SlipHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.DeliverType;
import com.kickmogu.yodobashi.community.resource.domain.constants.EffectiveSlipType;
import com.kickmogu.yodobashi.community.resource.domain.constants.OrderEntryType;

@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/serviceContext.xml")
public class CommunityHBaseRestoreJobTest {
	
	private static final String SLIPHEADER_TABLE = "/home/comm/test/SlipHeaderDO";
	
	private String host = "kmcms04";

	private String ftpUser = "comm";
	private String ftpPassword = "comm00";

	@Autowired  @Qualifier("default")
	private HBaseOperations hbaseOperations;
	
	@Autowired
	private HBaseBackupService backupService;
	

	@Autowired @Qualifier("MySite")
	private HBaseContainer hbaseContainer;
	
	private TimestampConfigurableProcessContextHolderImpl processContextHolderImpl = new TimestampConfigurableProcessContextHolderImpl();
	
	@Before
	public void setup() {
		SshUtils.command(host, ftpUser, ftpPassword, "rm -fr /home/comm/backup/current");
		SshUtils.command(host, ftpUser, ftpPassword, "mkdir -p /home/comm/backup/current/hbase");
		new File(SLIPHEADER_TABLE).delete();
		new File(SLIPHEADER_TABLE).mkdirs();
		clear();
	}
	
	private void clear() {
		HBaseTemplate template = (HBaseTemplate)((ExternalEntityHBaseTemplate)AopUtil.getTargetObject(hbaseOperations, HBaseOperations.class)).getOrgOperations();
		template.setProcessContextHolder(processContextHolderImpl);
		hbaseContainer.getAdministrator().dropTable(SlipHeaderDO.class);
		hbaseContainer.getAdministrator().createTable(SlipHeaderDO.class);
	}
	
	@After
	public void teardown() {
		new File(SLIPHEADER_TABLE).delete();
	}

	
	@Test
	public void test() throws Exception {

		long backupTimestamp = System.currentTimeMillis();

		processContextHolderImpl.setTimestamp(backupTimestamp-1);

		hbaseOperations.save(SlipHeaderDO.class, BeanTestHelper.createList(SlipHeaderDO.class,
			"id,outerCustomerId,slipNo,orderEntryType,deliverType,orderEntryDate,effectiveSlipType",
			"11,1000000001,1000000001,A,1,2011-01-01 00:00:01,1",
			"12,1000000002,1000000002,B,2,2011-01-01 00:00:02,1"
		));
		
		processContextHolderImpl.setTimestamp(backupTimestamp);
		hbaseOperations.save(SlipHeaderDO.class, BeanTestHelper.createList(SlipHeaderDO.class,
			"id,outerCustomerId,slipNo,orderEntryType,deliverType,orderEntryDate,effectiveSlipType",
			"21,2000000001,2000000001,C,1,2011-01-02 00:00:01,1",
			"22,2000000002,2000000002,D,2,2011-01-02 00:00:02,1"
		));
		
		prepareRestore(backupTimestamp, "hoge.dmp");
		
		CommunityHBaseRestoreJob.execute(applicationContext, new String[]{
			"-t", hbaseContainer.getMeta().getTableMeta(SlipHeaderDO.class).getTableName(),
			"-d", "file:///home/comm/test/SlipHeaderDO"
		});
		
		assertListContents(hbaseOperations.scanAll(SlipHeaderDO.class), SlipHeaderDO.class,
			"id,outerCustomerId,slipNo,orderEntryType,deliverType,orderEntryDate,effectiveSlipType",
			"11","1000000001","1000000001",OrderEntryType.FIX,DeliverType.SHOP,ts("2011-01-01 00:00:01"),EffectiveSlipType.EFFECTIVE,
			"12","1000000002","1000000002",OrderEntryType.NOT_REGIST,DeliverType.DELIVER_CALL_IN,ts("2011-01-01 00:00:02"),EffectiveSlipType.EFFECTIVE,
			"21","2000000001","2000000001",OrderEntryType.SHOP,DeliverType.SHOP,ts("2011-01-02 00:00:01"),EffectiveSlipType.EFFECTIVE,
			"22","2000000002","2000000002",OrderEntryType.SHOP_PREPAYED,DeliverType.DELIVER_CALL_IN,ts("2011-01-02 00:00:02"),EffectiveSlipType.EFFECTIVE
		);
		
	}

	@Test
	public void test2() throws Exception {

		long backupTimestamp = System.currentTimeMillis();

		processContextHolderImpl.setTimestamp(backupTimestamp-1);

		hbaseOperations.save(SlipHeaderDO.class, BeanTestHelper.createList(SlipHeaderDO.class,
			"id,outerCustomerId,slipNo,orderEntryType,deliverType,orderEntryDate,effectiveSlipType",
			"11,1000000001,1000000001,A,1,2011-01-01 00:00:01,1",
			"12,1000000002,1000000002,B,2,2011-01-01 00:00:02,1"
		));
		
		processContextHolderImpl.setTimestamp(backupTimestamp);
		hbaseOperations.save(SlipHeaderDO.class, BeanTestHelper.createList(SlipHeaderDO.class,
			"id,outerCustomerId,slipNo,orderEntryType,deliverType,orderEntryDate,effectiveSlipType",
			"21,2000000001,2000000001,C,1,2011-01-02 00:00:01,1",
			"22,2000000002,2000000002,D,2,2011-01-02 00:00:02,1"
		));

		prepareRestore(backupTimestamp, "hoge.dmp");

		hbaseOperations.save(SlipHeaderDO.class, BeanTestHelper.createList(SlipHeaderDO.class,
			"id,outerCustomerId,slipNo,orderEntryType,deliverType,orderEntryDate,effectiveSlipType",
			"31,3000000001,1000000001,A,1,2011-01-01 00:00:01,1",
			"32,3000000002,1000000002,B,2,2011-01-01 00:00:02,1"
		));
		
		processContextHolderImpl.setTimestamp(backupTimestamp);
		hbaseOperations.save(SlipHeaderDO.class, BeanTestHelper.createList(SlipHeaderDO.class,
			"id,outerCustomerId,slipNo,orderEntryType,deliverType,orderEntryDate,effectiveSlipType",
			"41,4000000001,2000000001,C,1,2011-01-02 00:00:01,1",
			"42,4000000002,2000000002,D,2,2011-01-02 00:00:02,1"
		));
		
		prepareRestore(backupTimestamp, "foo.dmp");
		
		CommunityHBaseRestoreJob.execute(applicationContext, new String[]{
			"-t", hbaseContainer.getMeta().getTableMeta(SlipHeaderDO.class).getTableName(),
			"-d", "file:///home/comm/test/SlipHeaderDO/*.dmp"
		});
		
		assertListContents(hbaseOperations.scanAll(SlipHeaderDO.class), SlipHeaderDO.class,
			"id,outerCustomerId,slipNo,orderEntryType,deliverType,orderEntryDate,effectiveSlipType",
			"11","1000000001","1000000001",OrderEntryType.FIX,DeliverType.SHOP,ts("2011-01-01 00:00:01"),EffectiveSlipType.EFFECTIVE,
			"12","1000000002","1000000002",OrderEntryType.NOT_REGIST,DeliverType.DELIVER_CALL_IN,ts("2011-01-01 00:00:02"),EffectiveSlipType.EFFECTIVE,
			"21","2000000001","2000000001",OrderEntryType.SHOP,DeliverType.SHOP,ts("2011-01-02 00:00:01"),EffectiveSlipType.EFFECTIVE,
			"22","2000000002","2000000002",OrderEntryType.SHOP_PREPAYED,DeliverType.DELIVER_CALL_IN,ts("2011-01-02 00:00:02"),EffectiveSlipType.EFFECTIVE,
			"31","3000000001","1000000001",OrderEntryType.FIX,DeliverType.SHOP,ts("2011-01-01 00:00:01"),EffectiveSlipType.EFFECTIVE,
			"32","3000000002","1000000002",OrderEntryType.NOT_REGIST,DeliverType.DELIVER_CALL_IN,ts("2011-01-01 00:00:02"),EffectiveSlipType.EFFECTIVE,
			"41","4000000001","2000000001",OrderEntryType.SHOP,DeliverType.SHOP,ts("2011-01-02 00:00:01"),EffectiveSlipType.EFFECTIVE,
			"42","4000000002","2000000002",OrderEntryType.SHOP_PREPAYED,DeliverType.DELIVER_CALL_IN,ts("2011-01-02 00:00:02"),EffectiveSlipType.EFFECTIVE
		);
		
	}
	
	
	private void prepareRestore(long backupTimestamp, String fileName) throws Exception {
		ByteArrayOutputStream bodyOutStrm = new ByteArrayOutputStream(1024);
		ByteArrayOutputStream headerOutStrm = new ByteArrayOutputStream(1024);
		backupService.backup(SlipHeaderDO.class, backupTimestamp, bodyOutStrm, headerOutStrm);

		SshUtils.command(host, ftpUser, ftpPassword, "mkdir -p /home/comm/test/SlipHeaderDO");
		SshUtils.scpUpload(host, ftpUser, ftpPassword,  bodyOutStrm.toByteArray(), "/home/comm/test/SlipHeaderDO", fileName);

		new File(SLIPHEADER_TABLE).mkdirs();
		FileUtils.writeByteArrayToFile(new File("/home/comm/test/SlipHeaderDO/" + fileName), bodyOutStrm.toByteArray());

		hbaseContainer.getAdministrator().dropTable(SlipHeaderDO.class);
		hbaseContainer.getAdministrator().createTable(SlipHeaderDO.class);		
	}

	
	public static void main(String[] args) throws Throwable {

		Path path = new Path("file:///tmp/aaa.txt");
		FileSystem fs = path.getFileSystem(new Configuration());
		System.out.println(fs.getFileStatus(path).getLen());
		
	}

}
