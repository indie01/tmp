package com.kickmogu.yodobashi.community.mapreduce.job;

import static com.kickmogu.lib.core.BeanTestHelper.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles;
import org.junit.After;
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
import com.kickmogu.lib.hadoop.hbase.HBaseProcessContextHolder;
import com.kickmogu.lib.hadoop.hbase.impl.ExternalEntityHBaseTemplate;
import com.kickmogu.lib.hadoop.hbase.impl.HBaseTemplate;
import com.kickmogu.lib.hadoop.hbase.impl.TimestampConfigurableProcessContextHolderImpl;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.mapreduce.job.CommunityHBaseBackupToHFileJob;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.EcCustomerStatusDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommentTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.DeliverType;
import com.kickmogu.yodobashi.community.resource.domain.constants.EcCustomerStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.EffectiveSlipType;
import com.kickmogu.yodobashi.community.resource.domain.constants.OrderEntryType;

@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/serviceContext.xml")
public class CommunityHBaseBackupToHFileJobTest {
	
	private static String HBASE_CURRENT_BACKUP_DIR = "/home/comm/backup/current/hbase/";
	private String host = "kmcms04";

	private String ftpUser = "comm";
	private String ftpPassword = "comm00";

	@Autowired  @Qualifier("default")
	private HBaseOperations hbaseOperations;


	@Autowired @Qualifier("MySite")
	private HBaseContainer hbaseContainer;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	private TimestampConfigurableProcessContextHolderImpl processContextHolderImpl = new TimestampConfigurableProcessContextHolderImpl();
	
	private HBaseProcessContextHolder originalProcessContextHolder;
	
	@Before
	public void setup() {

		SshUtils.command(host, ftpUser, ftpPassword, "rm -fr /home/comm/backup/current");
		SshUtils.command(host, ftpUser, ftpPassword, "mkdir -p /home/comm/backup/current/hbase");
		try {
			FileUtils.deleteDirectory(new File(HBASE_CURRENT_BACKUP_DIR));
		} catch (IOException e) {
		}
		HBaseTemplate template = (HBaseTemplate)((ExternalEntityHBaseTemplate)AopUtil.getTargetObject(hbaseOperations, HBaseOperations.class)).getOrgOperations();
		originalProcessContextHolder = template.getProcessContextHolder();
		template.setProcessContextHolder(processContextHolderImpl);
		
	}
	
	@After
	public void teardown() {
		HBaseTemplate template = (HBaseTemplate)((ExternalEntityHBaseTemplate)AopUtil.getTargetObject(hbaseOperations, HBaseOperations.class)).getOrgOperations();
		template.setProcessContextHolder(originalProcessContextHolder);
	}

	
	@Test
	public void test2() throws Exception {
		
		hbaseContainer.getAdministrator().dropTable(EcCustomerStatusDO.class);
		hbaseContainer.getAdministrator().createTable(EcCustomerStatusDO.class);
		
		long backupTimestamp = System.currentTimeMillis();
		processContextHolderImpl.setTimestamp(backupTimestamp);
		hbaseOperations.save(EcCustomerStatusDO.class, BeanTestHelper.createList(EcCustomerStatusDO.class,
			"id,outerCustomerId,status",
			"11,1000000001,1",
			"12,1000000002,9"
		));
		CommunityHBaseBackupToHFileJob.execute(applicationContext, new String[]{
			"-s", Long.valueOf(backupTimestamp).toString(),
			"-t", "nomuEcCustomerStatusDO",
			"-f", "cf",
			"-Dio.sort.mb=8"
		});

		restore(EcCustomerStatusDO.class);
		
		assertListContents(hbaseOperations.scanAll(EcCustomerStatusDO.class), EcCustomerStatusDO.class,
			"id,outerCustomerId,status",
			"11","1000000001",EcCustomerStatus.ENABLE,
			"12","1000000002",EcCustomerStatus.WITHDRAWAL
		);
		
	}

	@Test
	public void test() throws Exception {
		
		hbaseContainer.getAdministrator().dropTable(SlipHeaderDO.class);
		hbaseContainer.getAdministrator().createTable(SlipHeaderDO.class);

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

		CommunityHBaseBackupToHFileJob.execute(applicationContext, new String[]{
			"-s", Long.valueOf(backupTimestamp).toString(),
			"-t", "nomuSlipHeaderDO",
			"-f", "cf",
			"-Dio.sort.mb=8"
		});
		
		restore(SlipHeaderDO.class);

		assertListContents(hbaseOperations.scanAll(SlipHeaderDO.class), SlipHeaderDO.class,
			"id,outerCustomerId,slipNo,orderEntryType,deliverType,orderEntryDate,effectiveSlipType",
			"11","1000000001","1000000001",OrderEntryType.FIX,DeliverType.SHOP,ts("2011-01-01 00:00:01"),EffectiveSlipType.EFFECTIVE,
			"12","1000000002","1000000002",OrderEntryType.NOT_REGIST,DeliverType.DELIVER_CALL_IN,ts("2011-01-01 00:00:02"),EffectiveSlipType.EFFECTIVE,
			"21","2000000001","2000000001",OrderEntryType.SHOP,DeliverType.SHOP,ts("2011-01-02 00:00:01"),EffectiveSlipType.EFFECTIVE,
			"22","2000000002","2000000002",OrderEntryType.SHOP_PREPAYED,DeliverType.DELIVER_CALL_IN,ts("2011-01-02 00:00:02"),EffectiveSlipType.EFFECTIVE
		);		
	}
	

	// TODO 現バージョンのHBaseのHFileOutputFormatは複数ColumnFamilyには対応していない
	// セカンダリインデックスは通常のcfと__INDEX__の2つのColumnFamilyがある
	// のでHFile方式のバックアップはできない
	// 
	//@Test
	public void testSecondaryIndex() throws Exception {
		
		hbaseContainer.getAdministrator().dropTable(CommentDO.class);
		hbaseContainer.getAdministrator().createTable(CommentDO.class);

		long backupTimestamp = System.currentTimeMillis();
		processContextHolderImpl.setTimestamp(backupTimestamp);

		List<CommentDO> comments = BeanTestHelper.createList(CommentDO.class,
			"commentId,targetType,questionId",
			"c009,1,q009"
		);
		appendCommunityUser(comments, "cu009");
		hbaseOperations.save(CommentDO.class, comments);
		hbaseOperations.deleteByKeys(CommentDO.class, String.class, Lists.newArrayList("c009"));
		
		comments = BeanTestHelper.createList(CommentDO.class,
			"commentId,targetType,questionId",
			"c001,1,q001"
		);
		appendCommunityUser(comments, "cu001");
		hbaseOperations.save(CommentDO.class, comments);
		
		processContextHolderImpl.setTimestamp(backupTimestamp+1);
		comments = BeanTestHelper.createList(CommentDO.class,
			"commentId,targetType,questionId",
			"c002,1,q002"
		);
		appendCommunityUser(comments, "cu002");
		hbaseOperations.save(CommentDO.class, comments);
		
		
		assertListContents(hbaseOperations.scanAll(CommentDO.class), CommentDO.class,
			"commentId,targetType,questionId",
			"c001",CommentTargetType.REVIEW,"q001",
			"c002",CommentTargetType.REVIEW,"q002"
		);
		
		assertEquals(0, hbaseOperations.scanWithIndex(CommentDO.class, "questionId", "q009").size());
		assertListContents(hbaseOperations.scanWithIndex(CommentDO.class, "questionId", "q001"), CommentDO.class,
			"commentId,targetType,questionId",
			"c001",CommentTargetType.REVIEW,"q001"
		);
		assertListContents(hbaseOperations.scanWithIndex(CommentDO.class, "questionId", "q002"), CommentDO.class,
			"commentId,targetType,questionId",
			"c002",CommentTargetType.REVIEW,"q002"
		);
		
		
		CommunityHBaseBackupToHFileJob.execute(applicationContext, new String[]{
			"-s", Long.valueOf(backupTimestamp).toString(),
			"-t", "nomuCommentDO",
			"-f", "cf",
			"-Dio.sort.mb=8"
		});
		
		CommunityHBaseBackupToHFileJob.execute(applicationContext, new String[]{
			"-s", Long.valueOf(backupTimestamp).toString(),
			"-t", "nomuCommentDO-questionId",
			"-f", "cf",
			"-Dio.sort.mb=8"
		});
		
		CommunityHBaseBackupToHFileJob.execute(applicationContext, new String[]{
			"-s", Long.valueOf(backupTimestamp).toString(),
			"-t", "nomuCommentDO-communityUserId",
			"-f", "cf",
			"-Dio.sort.mb=8"
		});
		
		restore(CommentDO.class);
		restoreSecondaryIndex("nomuCommentDO-questionId");
		restoreSecondaryIndex("nomuCommentDO-communityUserId");
		
		assertListContents(hbaseOperations.scanAll(CommentDO.class), CommentDO.class,
			"commentId,targetType,questionId",
			"c001",CommentTargetType.REVIEW,"q001"
		);
		
		assertEquals(0, hbaseOperations.scanWithIndex(CommentDO.class, "questionId", "q009").size());
		assertListContents(hbaseOperations.scanWithIndex(CommentDO.class, "questionId", "q001"), CommentDO.class,
			"commentId,targetType,questionId",
			"c001",CommentTargetType.REVIEW,"q001"
		);
		assertEquals(0, hbaseOperations.scanWithIndex(CommentDO.class, "questionId", "q002").size());
	}
	
	private List<CommentDO> appendCommunityUser(List<CommentDO> comments, String communityUserId) {
		CommunityUserDO communityUser = new CommunityUserDO();
		communityUser.setCommunityUserId(communityUserId);
		for (CommentDO comment:comments) {
			comment.setCommunityUser(communityUser);
		}
		return comments;
	}

	private void restoreSecondaryIndex(String tableName) {

		try {
			FileUtils.deleteDirectory(new File(HBASE_CURRENT_BACKUP_DIR + tableName));
		} catch (IOException e1) {
		}
		new File(HBASE_CURRENT_BACKUP_DIR).mkdirs();
		
		SshUtils.scpDownload( "kmcms04", "comm", "comm00"," /home/comm/backup/current/hbase/" + tableName, "/home/comm/backup/current/hbase/" + tableName);
		try {
			LoadIncrementalHFiles loadIncrementalHFiles = new LoadIncrementalHFiles(hbaseContainer.getConfiguration());
			loadIncrementalHFiles.run(new String[]{"file:///home/comm/backup/current/hbase/" + tableName, tableName});
		} catch (Exception e) {
			throw new CommonSystemException(e);
		}
	}

	private void restore(Class<?> type) {
		
		hbaseContainer.getAdministrator().dropTable(type);
		hbaseContainer.getAdministrator().createTable(type);
		
		String tableName = hbaseContainer.getMeta().getTableMetaMap().get(type).getTableName();

		try {
			FileUtils.deleteDirectory(new File("/home/comm/backup/current/hbase/" + tableName));
		} catch (IOException e1) {
		}
		new File(HBASE_CURRENT_BACKUP_DIR).mkdirs();
		
		SshUtils.scpDownload( "kmcms04", "comm", "comm00"," /home/comm/backup/current/hbase/" + tableName, "/home/comm/backup/current/hbase/" + tableName);
		try {
			LoadIncrementalHFiles loadIncrementalHFiles = new LoadIncrementalHFiles(hbaseContainer.getConfiguration());
			loadIncrementalHFiles.run(new String[]{"file:///home/comm/backup/current/hbase/" + tableName, tableName});
		} catch (Exception e) {
			throw new CommonSystemException(e);
		}
	}
}
