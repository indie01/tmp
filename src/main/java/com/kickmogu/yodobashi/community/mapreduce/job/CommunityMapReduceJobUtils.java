package com.kickmogu.yodobashi.community.mapreduce.job;

import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;

import com.kickmogu.hadoop.mapreduce.job.AbstractMapReduceJob;
import com.kickmogu.lib.core.resource.Site;
import com.kickmogu.lib.core.utils.Asserts;
import com.kickmogu.lib.core.utils.ConfigUtil;
import com.kickmogu.yodobashi.community.resource.config.HBaseConfig;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;

public class CommunityMapReduceJobUtils {
	

	protected static final String COMMON_ENV_PREFIX = "commonJob.";

	private CommunityMapReduceJobUtils(){}
	
	public static String getMyJobTrackerAddress(AbstractMapReduceJob job) {
		CommunityMapReduceConfig config = new CommunityMapReduceConfig();
		ConfigUtil.loadProperties(job.getProfileName(), "mr-config", config);
		return getMySite(job).equals(Site.SITE1) ? config.jobTrackerAddressSite1 : config.jobTrackerAddressSite2;
	}

	public static String getMyJobTrackerFileSystem(AbstractMapReduceJob job) {
		CommunityMapReduceConfig config = new CommunityMapReduceConfig();
		ConfigUtil.loadProperties(job.getProfileName(), "mr-config", config);
		return getMySite(job).equals(Site.SITE1) ? config.jobTrackerFileSystemSite1 : config.jobTrackerFileSystemSite2;
	}
	
	private static Site getMySite(AbstractMapReduceJob job) {
		HBaseConfig hbaseConfig = new HBaseConfig();
		ConfigUtil.loadProperties(job.getProfileName(), "resource-config", hbaseConfig);
		job.getConf().addResource(hbaseConfig.getHBaseMyConfigFile());
		Asserts.notNull(hbaseConfig.hbaseMySite);
		return hbaseConfig.hbaseMySite;
	}
	
	public static void initializeConfiguration(AbstractMapReduceJob job, Properties properties, String[] args) throws Exception {
		HBaseConfig hbaseConfig = new HBaseConfig();
		ConfigUtil.loadProperties(job.getProfileName(), "resource-config", hbaseConfig);
		job.getConf().addResource(hbaseConfig.getHBaseMyConfigFile());

		ResourceBundle env = ResourceBundle.getBundle(ConfigUtil.getPropertiesPath(job.getProfileName(), "mr-env-config"));
		for (String key : env.keySet()) {
			if (key.startsWith(COMMON_ENV_PREFIX)) {
				properties.put(key.substring(COMMON_ENV_PREFIX.length()), env.getString(key));
			}
		}
		for (String key : env.keySet()) {
			if (key.startsWith(job.getJobName() + ".")) {
				properties.put(key.substring(job.getJobName().length() + 1), env.getString(key));
			}
		}

		if (job.getProfileName() != null) {
			
			ResourceConfig resourceConfig = new ResourceConfig();
			ConfigUtil.loadProperties(job.getProfileName(), "resource-config", resourceConfig);

			StringBuilder sb = new StringBuilder();
			sb.append("-Dspring.profiles.active=" + job.getProfileName() + " ");
			sb.append("-Dhbase.mysite=" + hbaseConfig.hbaseMySite + " ");
			sb.append("-Dsolr.mysite=" + resourceConfig.solrMySite + " ");
			sb.append("-Dhbase.mirroring=" + hbaseConfig.hbaseMirroring + " ");
			sb.append("-Dhbase.monitor.enable=" + resourceConfig.hbaseMonitorEnable + " ");
			sb.append("-Dsolr.enable.master.monitor=" +  resourceConfig.solrEnableMasterMonitor + " ");
			sb.append("-Dsolr.enable.webserver.monitor=" +  resourceConfig.solrEnableWebServerMonitor + " ");
			sb.append("-Dsolr.mirroring=" + resourceConfig.solrMirroring + " ");
			sb.append("-Dhbase.use.only.mySite=" + hbaseConfig.hbaseUseOnlyMySite + " ");
			sb.append("-Dsolr.use.only.mySite=" + resourceConfig.solrUseOnlyMySite + " ");
			sb.append("-DmyZone=" + resourceConfig.myZone.name() + " ");
			sb.append("-Dhbase.secondary.index.enabled=" + hbaseConfig.hbaseSecondaryIndexEnabled + " ");
			
			if (System.getProperty("disable.arroundSolr") != null) {
				sb.append("-Ddisable.arroundSolr=" + System.getProperty("disable.arroundSolr") + " ");
			}
			
			sb.append(StringUtils.defaultString(properties.getProperty("mapred.child.java.opts")));
			
			properties.setProperty("mapred.child.java.opts", sb.toString().trim());

		}
		
		
	}


}
