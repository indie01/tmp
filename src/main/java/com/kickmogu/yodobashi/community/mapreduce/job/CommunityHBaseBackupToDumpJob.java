package com.kickmogu.yodobashi.community.mapreduce.job;

import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ToolRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.utils.ConfigUtil;
import com.kickmogu.lib.hadoop.hbase.backup.HBaseBackupToDumpJob;
import com.kickmogu.yodobashi.community.resource.config.HBaseConfig;

@Service @Lazy @Scope("prototype")
public class CommunityHBaseBackupToDumpJob extends HBaseBackupToDumpJob {
	
	public CommunityHBaseBackupToDumpJob()  {
		super();
		this.springConfigLocation = "classpath:/mr-context.xml";
		this.mapReduceConfigName =  "mr-config";
	}
	
	protected String getJobTrackerAddress() {
		return CommunityMapReduceJobUtils.getMyJobTrackerAddress(this);
	}
	
	protected String getJobTrackerFileSystem() {
		return CommunityMapReduceJobUtils.getMyJobTrackerFileSystem(this);
	}
	

	@Autowired
	public void setCommunityMapReduceConfig(CommunityMapReduceConfig communityMapReduceConfig) {
		this.setMapReduceConfig(communityMapReduceConfig);
	}

	@Autowired
	public void setExtConfiguration(Configuration extConfiguration) {
		super.setExtConfiguration(extConfiguration);
	}

	
	@Override
	protected void initializeConfiguration(
			Properties properties,
			String[] args) throws Exception {
		super.initializeConfiguration(properties, args);
		CommunityMapReduceJobUtils.initializeConfiguration(this, properties, args);
		
		HBaseConfig config = new HBaseConfig();
		ConfigUtil.loadProperties(getProfileName(), "resource-config", config);
		properties.setProperty(HBaseBackupToDumpJob.TABLE_NAME_PREFIX, config.hbaseTableNamePrefix);
	}

	public static int execute(ApplicationContext parentApplicationContext, String[] args) throws Exception {
		return ToolRunner.run(new CommunityHBaseBackupToDumpJob().setParentApplicationContext(parentApplicationContext),
				args);
	}

	public static void main(String[] args) throws Exception {
		int res = execute(null, args);
		System.out.println(res);
		System.exit(res);
	}

}
