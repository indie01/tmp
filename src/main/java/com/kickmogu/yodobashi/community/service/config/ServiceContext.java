package com.kickmogu.yodobashi.community.service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kickmogu.lib.hadoop.hbase.HBaseContainer;
import com.kickmogu.lib.hadoop.hbase.HBaseDirectOperations;
import com.kickmogu.lib.hadoop.hbase.backup.HBaseBackupService;
import com.kickmogu.lib.hadoop.hbase.backup.HBaseBackupServiceImpl;
import com.kickmogu.lib.hadoop.hbase.backup.HBaseDirectRestoreServiceImpl;
import com.kickmogu.lib.hadoop.hbase.backup.HBaseRestoreService;
import com.kickmogu.lib.hadoop.hbase.backup.HBaseRestoreServiceImpl;
import com.kickmogu.lib.hadoop.hbase.service.HBaseCounterService;
import com.kickmogu.lib.hadoop.hbase.service.HBaseDeleteRowCleanupService;
import com.kickmogu.lib.hadoop.hbase.service.HBaseOlderVersionCleanupService;
import com.kickmogu.lib.hadoop.hbase.service.HBaseRecoveryService;
import com.kickmogu.lib.hadoop.hbase.service.impl.HBaseCounterServiceImpl;
import com.kickmogu.lib.hadoop.hbase.service.impl.HBaseDeleteRowCleanupServiceImpl;
import com.kickmogu.lib.hadoop.hbase.service.impl.HBaseOlderVersionCleanupServiceImpl;
import com.kickmogu.lib.hadoop.hbase.service.impl.HBaseRecoveryServiceImpl;

@Configuration
public class ServiceContext {

	@Autowired @Qualifier("MySite")
	private HBaseContainer container;

	@Autowired @Qualifier("MySite")
	private HBaseDirectOperations hbaseDirectOperations;

	@Bean(name="hBaseBackupService")
	public HBaseBackupService hBaseBackupService() {
		HBaseBackupServiceImpl backupService = new HBaseBackupServiceImpl();
		backupService.setContainer(container);
		return backupService;
	}

	@Bean(name="hBaseRestoreService") @Qualifier("simple")
	public HBaseRestoreService hBaseRestoreService() {
		HBaseRestoreServiceImpl restoreService = new HBaseRestoreServiceImpl();
		restoreService.setContainer(container);
		return restoreService;
	}
	
	@Bean(name="hBaseDirectRestoreService") @Qualifier("direct")
	public HBaseRestoreService hBaseDirectRestoreService() {
		HBaseDirectRestoreServiceImpl restoreService = new HBaseDirectRestoreServiceImpl();
		restoreService.setHBaseDirectOperations(hbaseDirectOperations);
		restoreService.setContainer(container);
		return restoreService;
	}

	@Bean(name="hBaseDeleteRowCleanupService")
	public HBaseDeleteRowCleanupService hBaseDeleteRowCleanupService() {
		HBaseDeleteRowCleanupServiceImpl cleanupService = new HBaseDeleteRowCleanupServiceImpl();
		cleanupService.setContainer(container);
		return cleanupService;
	}

	@Bean(name="hBaseOlderVersionCleanupService")
	public HBaseOlderVersionCleanupService hBaseOlderVersionCleanupService() {
		HBaseOlderVersionCleanupServiceImpl cleanupService = new HBaseOlderVersionCleanupServiceImpl();
		cleanupService.setContainer(container);
		return cleanupService;
	}

	@Bean @Qualifier("OnlyHBase")
	public HBaseRecoveryService hBaseSolrRecoveryService() {
		HBaseRecoveryServiceImpl hBaseRecoveryServiceImpl = new HBaseRecoveryServiceImpl();
		hBaseRecoveryServiceImpl.setContainer(container);
		return hBaseRecoveryServiceImpl;
	}

	@Bean(name="hBaseCounterService")
	public HBaseCounterService hBaseCounterCleanupService() {
		HBaseCounterServiceImpl counterService = new HBaseCounterServiceImpl();
		counterService.setContainer(container);
		return counterService;
	}
}
