package com.kickmogu.yodobashi.community.mapreduce.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.kickmogu.lib.core.resource.Site;
import com.kickmogu.lib.hadoop.hbase.backup.HBaseBackupConfig;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;

@Configuration
public class CommunityBackupConfig extends HBaseBackupConfig {
	
	@Value("${backup.baseDir}")
	public String backupBaseDir;

	@Value("${SITE1.backup.sshHost}")
	private String site1BackupSshHost;

	@Value("${SITE2.backup.sshHost}")
	private String site2BackupSshHost;

	@Value("${backup.sshUser}")
	public String backupSshUser;	

	@Value("${backup.sshPassword}")
	public String backupSshPassword;	
	
	@Value("${SITE1.backup.sshHost.solr}")
	public String site1BackupSshHostSolr;
	
	@Value("${SITE2.backup.sshHost.solr}")
	public String site2BackupSshHostSolr;

	@Value("${backup.sshUser.solr}")
	public String backupSshUserSolr;	

	@Value("${backup.sshPassword.solr}")
	public String backupSshPasswordSolr;	
	
	@Value("${hbase.backup.excludeTableTypes}")
	public String excludeTableTypes;
	
	@Value("${hbase.backup.withoutMapreduceTableTypes}")
	public String withoutMapreduceTableTypes;
	
	@Value("${solr.backup.baseDir}")
	public String solrBackupBaseDir;
	
	@Autowired
	private ResourceConfig resourceConfig;
	
	public String getBackupSshHost() {
		return Site.SITE1.equals(resourceConfig.hbaseMySite) ? site1BackupSshHost : site2BackupSshHost;
	}
	
	public String getBackupSshHostSolr() {
		return Site.SITE1.equals(resourceConfig.hbaseMySite) ? site1BackupSshHostSolr : site2BackupSshHostSolr;
	}
}
