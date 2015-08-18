package com.kickmogu.yodobashi.community.mapreduce.job;

import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ToolRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.hadoop.hbase.backup.HBaseCounterJob;

@Service
@Lazy
@Scope("prototype")
public class CommunityHBaseCounterJob extends HBaseCounterJob {

	public CommunityHBaseCounterJob() {
		super();
		this.springConfigLocation = "classpath:/mr-context.xml";
		this.mapReduceConfigName = "mr-config";
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
	protected void initializeConfiguration(Properties properties, String[] args) throws Exception {
		super.initializeConfiguration(properties, args);
		CommunityMapReduceJobUtils.initializeConfiguration(this, properties, args);
	}

	public static int execute(ApplicationContext parentApplicationContext, String[] args) throws Exception {
		return ToolRunner.run(new CommunityHBaseCounterJob().setParentApplicationContext(parentApplicationContext), args);
	}

	public static void main(String[] args) throws Exception {
		int res = execute(null, args);
		System.out.println(res);
		System.exit(res);
	}

}
