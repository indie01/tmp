package com.kickmogu.yodobashi.community.mapreduce.job;

import static com.kickmogu.lib.core.utils.ThreadSafeDateFormat.parse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.DataInputStream;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
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
import com.kickmogu.lib.core.ssh.SshUtils;
import com.kickmogu.lib.core.utils.AopUtil;
import com.kickmogu.lib.hadoop.hbase.HBaseContainer;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.impl.ExternalEntityHBaseTemplate;
import com.kickmogu.lib.hadoop.hbase.impl.HBaseTemplate;
import com.kickmogu.lib.hadoop.hbase.impl.TimestampConfigurableProcessContextHolderImpl;
import com.kickmogu.lib.hadoop.hbase.io.HBaseDump;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.domain.EcCustomerStatusDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.SocialMediaSettingDO;

@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/serviceContext.xml")
public class CommunityHBaseBackupJobTest {
	
	private String host = "kmcms04";
	private String baseDir;
	private String ftpUser = "comm";
	private String ftpPassword = "comm00";

	@Autowired  @Qualifier("default")
	private HBaseOperations hbaseOperations;
	
	@Autowired
	private ResourceConfig mapReduceConfig;
	

	@Autowired @Qualifier("MySite")
	private HBaseContainer hbaseContainer;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	private TimestampConfigurableProcessContextHolderImpl processContextHolderImpl = new TimestampConfigurableProcessContextHolderImpl();
	
	@Before
	public void setup() {
		baseDir = mapReduceConfig.getPropatyValue("hbase.backup.baseDir");
		SshUtils.command(host, ftpUser, ftpPassword, "rm -fr /home/comm/backup/current");
		SshUtils.command(host, ftpUser, ftpPassword, "mkdir -p /home/comm/backup/current/hbase");
		clear();
	}
	
	private void clear() {
		HBaseTemplate template = (HBaseTemplate)((ExternalEntityHBaseTemplate)AopUtil.getTargetObject(hbaseOperations, HBaseOperations.class)).getOrgOperations();
		template.setProcessContextHolder(processContextHolderImpl);
		hbaseContainer.getAdministrator().dropTable(SlipHeaderDO.class);
		hbaseContainer.getAdministrator().createTable(SlipHeaderDO.class);
		hbaseContainer.getAdministrator().dropTable(EcCustomerStatusDO.class);
		hbaseContainer.getAdministrator().createTable(EcCustomerStatusDO.class);		
	}

	@Test
	public void test4899() {
		long backupTimestamp = System.currentTimeMillis();
		processContextHolderImpl.setTimestamp(backupTimestamp);
		try {
			CommunityHBaseBackupToDumpJob.execute(applicationContext, new String[]{
					"-b", "20120508",
					"-s", Long.valueOf(backupTimestamp).toString(),
					"-t", hbaseContainer.getMeta().getTableMeta(SocialMediaSettingDO.class).getTableName(),
					"-f", "cf",
					"-Dio.sort.mb=8"
				});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void test2() throws Exception {
		long backupTimestamp = System.currentTimeMillis();
		processContextHolderImpl.setTimestamp(backupTimestamp);
		hbaseOperations.save(EcCustomerStatusDO.class, BeanTestHelper.createList(EcCustomerStatusDO.class,
			"id,outerCustomerId,status",
			"11,1000000001,1",
			"12,1000000002,9"
		));
		CommunityHBaseBackupToDumpJob.execute(applicationContext, new String[]{
			"-s", Long.valueOf(backupTimestamp).toString(),
			"-t", hbaseContainer.getMeta().getTableMeta(EcCustomerStatusDO.class).getTableName(),
			"-f", "cf",
			"-Dio.sort.mb=8"
		});
		HBaseDump.UnPacker unpacker = getUnPacker(EcCustomerStatusDO.class);
		HBaseDump.Header header = unpacker.getHeader();
		assertEquals("nomuEcCustomerStatusDO", header.getTableName());
		assertEquals("nomu", header.getTableNamePrefix());
		
		HBaseDump.UnpackedRow unpackedRow = unpacker.readNextRow();
		assertEquals("11", unpackedRow.getKeyAsString());
		assertEquals(4, unpackedRow.getColumnSize());
		assertEquals("1000000001", unpackedRow.getColumnByName("cf:outerCustomerId").getValueAsString());
		assertEquals("1", unpackedRow.getColumnByName("cf:status").getValueAsString());
		assertEquals(false, unpackedRow.getColumnByName("cf:DF").getValueAsBoolean());
		assertEquals(null, unpackedRow.getColumnByName("cf:modifyDateTime").getValueAsDate());
		
		unpackedRow = unpacker.readNextRow();
		assertEquals("12", unpackedRow.getKeyAsString());
		assertEquals(4, unpackedRow.getColumnSize());
		assertEquals("1000000002", unpackedRow.getColumnByName("cf:outerCustomerId").getValueAsString());
		assertEquals("9", unpackedRow.getColumnByName("cf:status").getValueAsString());
		assertEquals(false, unpackedRow.getColumnByName("cf:DF").getValueAsBoolean());
		assertEquals(null, unpackedRow.getColumnByName("cf:modifyDateTime").getValueAsDate());
	}	
	
	@Test
	public void test() throws Exception {
		long backupTimestamp = System.currentTimeMillis();

		processContextHolderImpl.setTimestamp(backupTimestamp-1);

		hbaseOperations.save(SlipHeaderDO.class, BeanTestHelper.createList(SlipHeaderDO.class,
			"id,outerCustomerId,slipNo,orderEntryType,deliverType,orderEntryDate,effectiveSlipType",
			"91,9000000001,9000000001,A,1,2011-01-01 00:00:01,1",
			"92,9000000002,9000000002,B,2,2011-01-01 00:00:02,1"
		));
		hbaseOperations.deleteByKeys(SlipHeaderDO.class, String.class, Lists.newArrayList("91","92"));
		
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
		
		processContextHolderImpl.setTimestamp(backupTimestamp+1);
		hbaseOperations.save(SlipHeaderDO.class, BeanTestHelper.createList(SlipHeaderDO.class,
			"id,outerCustomerId,slipNo,orderEntryType,deliverType,orderEntryDate,effectiveSlipType",
			"31,3000000001,3000000001,A,1,2011-01-03 00:00:01,1",
			"32,3000000002,3000000002,B,2,2011-01-03 00:00:02,1"
		));
		
		
		CommunityHBaseBackupToDumpJob.execute(applicationContext, new String[]{
			"-s", Long.valueOf(backupTimestamp).toString(),
			"-t", hbaseContainer.getMeta().getTableMeta(SlipHeaderDO.class).getTableName(),
			"-f", "cf",
			"-Dio.sort.mb=8"
		});
		HBaseDump.UnPacker unpacker = getUnPacker(SlipHeaderDO.class);
		HBaseDump.Header header = unpacker.getHeader();
		assertEquals("nomuSlipHeaderDO", header.getTableName());
		assertEquals("nomu", header.getTableNamePrefix());
		
		HBaseDump.UnpackedRow unpackedRow = unpacker.readNextRow();
		assertEquals("11", unpackedRow.getKeyAsString());
		assertEquals(8, unpackedRow.getColumnSize());
		assertEquals("1000000001", unpackedRow.getColumnByName("cf:outerCustomerId").getValueAsString());
		assertEquals("1000000001", unpackedRow.getColumnByName("cf:slipNo").getValueAsString());
		assertEquals("A", unpackedRow.getColumnByName("cf:orderEntryType").getValueAsString());
		assertEquals("1", unpackedRow.getColumnByName("cf:deliverType").getValueAsString());
		assertEquals(parse("yyyy-MM-dd HH:mm:ss", "2011-01-01 00:00:01"), unpackedRow.getColumnByName("cf:orderEntryDate").getValueAsDate());
		assertEquals("1", unpackedRow.getColumnByName("cf:effectiveSlipType").getValueAsString());
		assertEquals(false, unpackedRow.getColumnByName("cf:DF").getValueAsBoolean());
		assertEquals(null, unpackedRow.getColumnByName("cf:modifyDateTime").getValueAsDate());
		
		unpackedRow = unpacker.readNextRow();
		assertEquals("12", unpackedRow.getKeyAsString());
		assertEquals(8, unpackedRow.getColumnSize());
		assertEquals("1000000002", unpackedRow.getColumnByName("cf:outerCustomerId").getValueAsString());
		assertEquals("1000000002", unpackedRow.getColumnByName("cf:slipNo").getValueAsString());
		assertEquals("B", unpackedRow.getColumnByName("cf:orderEntryType").getValueAsString());
		assertEquals("2", unpackedRow.getColumnByName("cf:deliverType").getValueAsString());
		assertEquals(parse("yyyy-MM-dd HH:mm:ss", "2011-01-01 00:00:02"), unpackedRow.getColumnByName("cf:orderEntryDate").getValueAsDate());
		assertEquals("1", unpackedRow.getColumnByName("cf:effectiveSlipType").getValueAsString());
		assertEquals(false, unpackedRow.getColumnByName("cf:DF").getValueAsBoolean());
		assertEquals(null, unpackedRow.getColumnByName("cf:modifyDateTime").getValueAsDate());
		
		unpackedRow = unpacker.readNextRow();
		assertEquals(true, unpacker.hasNextRow());
		
		unpackedRow = unpacker.readNextRow();
		assertEquals("22", unpackedRow.getKeyAsString());
		assertEquals("2000000002", unpackedRow.getColumnByName("cf:outerCustomerId").getValueAsString());
		assertEquals(parse("yyyy-MM-dd HH:mm:ss", "2011-01-02 00:00:02"), unpackedRow.getColumnByName("cf:orderEntryDate").getValueAsDate());
		assertEquals(false, unpacker.hasNextRow());
		
		clear();

	}
	
	private HBaseDump.UnPacker getUnPacker(Class<?> type) {
		try {
			return new HBaseDump.UnPacker(
					getBackupDataInputStream(type, 0), getBackupDataInputStream(type, 1));
		} catch (Throwable th) {
			throw new CommonSystemException(th);
		}
	}
	
	private DataInputStream getBackupDataInputStream(Class<?> type, int dumpTypeIndex) {
		try {
			FTPClient ftpClient = readyFtp(type);
			InputStream inputStream = ftpClient.retrieveFileStream(ftpClient.listFiles()[dumpTypeIndex].getName());

			assertNotNull(inputStream);
			return new DataInputStream(inputStream);

		} catch (Throwable th) {
			th.printStackTrace();
			throw new CommonSystemException(th);
		}
	}
	
	private FTPClient readyFtp(Class<?> type) {
		try {
			String workDir = baseDir + "/"  + hbaseContainer.getMeta().getTableMeta(type).getTableName();
			
			FTPClient ftpClient = new FTPClient();
			ftpClient.connect(host);
			int reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) throw new CommonSystemException("ftp connect failed. host=" + host + ". reply=" + reply);
			if (!ftpClient.login(ftpUser, ftpPassword)) throw new CommonSystemException("ftp login failed. host=" + host + ". user=" + ftpUser);
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			System.out.println(workDir);
			if (!ftpClient.changeWorkingDirectory(workDir)) {
				throw new RuntimeException(workDir);
			}
			return ftpClient;
		} catch (Throwable th) {
			throw new CommonSystemException(th);
		}
	}

}
