package com.kickmogu.yodobashi.community.resource.ha;

import java.util.Date;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Abortable;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.zookeeper.ZKUtil;
import org.apache.hadoop.hbase.zookeeper.ZooKeeperWatcher;

import com.google.common.collect.Lists;
import com.kickmogu.hadoop.mapreduce.job.util.ThreadsafeOptionBuilder;
import com.kickmogu.lib.core.exception.CommonSystemException;
import com.kickmogu.lib.core.resource.Site;
import com.kickmogu.lib.core.utils.Asserts;
import com.kickmogu.lib.core.utils.ConfigUtil;
import com.kickmogu.yodobashi.community.resource.config.HBaseConfig;

public class HATool {

	
	private List<SiteConf> siteConfs;

	
	public HATool(List<SiteConf> siteConfs) throws Exception {
		this.siteConfs = siteConfs;
	}

	private void list() throws Exception {
		for (SiteConf siteConf:siteConfs) {
			System.out.println(siteConf.site.name() + "==========================================");
			byte[] data = ZKUtil.getData(siteConf.getZooKeeper(), HAManager.HA_ZNODE);
			if (data == null) {
				System.out.println("HAInfo is empty");
				continue;
			}
			HAInfo haInfo = HAInfo.fromBytes(data);
			System.out.println(haInfo);
		}
	}

	private void update(HAInfo haInfo) throws Exception {
		for (SiteConf siteConf:siteConfs) {
			System.out.println(siteConf.site.name() + "==========================================");
			ZKUtil.createSetData(siteConf.getZooKeeper(), HAManager.HA_ZNODE, haInfo.toBytes());
		}
	}
	
	
	private void delete() throws Exception {
		for (SiteConf siteConf:siteConfs) {
			System.out.println(siteConf.site.name() + "==========================================");
			byte[] data = ZKUtil.getData(siteConf.getZooKeeper(), HAManager.HA_ZNODE);
			if (data != null) {
				ZKUtil.deleteNode(siteConf.getZooKeeper(), HAManager.HA_ZNODE);				
			}
		}
	}

	public static void main(String[] args) throws Exception  {
		
		Options options = new Options();
		options.addOption(
			new ThreadsafeOptionBuilder()
		    .hasArg(true).withArgName("SITE1|SITE2|BOTH")
		    .isRequired(true)
		    .withDescription("site")
		    .create("site")
	    );
		options.addOption(
			new ThreadsafeOptionBuilder().withArgName("list")
		    .hasArg(false)
		    .isRequired(false)
		    .withDescription("list")
		    .create("list")
	    );
		options.addOption(
			new ThreadsafeOptionBuilder().withArgName("delete")
		    .hasArg(false)
		    .isRequired(false)
		    .withDescription("delete")
		    .create("delete")
	    );
		options.addOption(
			new ThreadsafeOptionBuilder()
		    .hasArg(true).withArgName("NORMAL|ONE_LUNG:SITE[12]|ONE_LUNG_ON_REF:SITE[12]")
		    .isRequired(false)
		    .withDescription("update")
		    .create("update")
	    );
		
		CommandLineParser parser = new PosixParser();
		CommandLine commandLine = null;
        try {
        	commandLine = parser.parse(options, args);
        } catch (ParseException e) {
        	argError(options);
        }
        
        List<SiteConf> siteConfs = null;
        if ("BOTH".equals(commandLine.getOptionValue("site"))) {
        	siteConfs = Lists.newArrayList(createSiteConf(Site.SITE1), createSiteConf(Site.SITE2));
        } else {
        	Site site = Site.valueOf(commandLine.getOptionValue("site"));
        	if (site == null) argError(options);
        	siteConfs = Lists.newArrayList(createSiteConf(site));
        }
        
        HATool tool = new HATool(siteConfs);
        
        if (commandLine.hasOption("list")) {
        	tool.list();
        } else if (commandLine.hasOption("update")) {
        	String[] haInfoStrs = commandLine.getOptionValue("update").split(":");
        	HAStatus status = HAStatus.valueOf(haInfoStrs[0]);
        	Site oneLungSite = null;
        	if (haInfoStrs.length == 2) oneLungSite = Site.valueOf(haInfoStrs[1]);
        	HAInfo haInfo = new HAInfo(status, oneLungSite, new Date());
        	tool.update(haInfo);
        	tool.list();
        } else if (commandLine.hasOption("delete")) {
        	tool.delete();
        	tool.list();
        }
	}


	private static SiteConf createSiteConf(Site site) {
        String profileName = System.getProperty("spring.profiles.active");
        Asserts.notNull(profileName, "spring.profiles.active is null");
        HBaseConfig config = new HBaseConfig();
        ConfigUtil.loadProperties(profileName, "resource-config", config);
        String configFile = site.equals(Site.SITE1) ? config.hbaseConfigFileMirror1 : config.hbaseConfigFileMirror2;
        Configuration conf = new Configuration();
        conf.addResource(configFile);
        conf.set("hbase.zookeeper.property.clientPort", conf.get("hbase.zookeeper.property.clientPort", String.valueOf(HConstants.DEFAULT_ZOOKEPER_CLIENT_PORT)));
        return new SiteConf(site, conf);
	}


	private static void argError(Options options) {
        HelpFormatter help = new HelpFormatter();
        help.printHelp(HATool.class.getSimpleName(), options, true);
        System.exit(1);
	}

	
	public static class SiteConf implements Abortable {
		public Site site;
		public Configuration conf;
		public SiteConf(Site site, Configuration conf) {
			this.site = site;
			this.conf = conf;
		}
		public ZooKeeperWatcher getZooKeeper() {
			try {
				return new ZooKeeperWatcher(conf, HAManager.WATCHER_NAME, this);
			} catch (Throwable e) {
				throw new CommonSystemException(e);
			}
		}
		
		@Override
		public void abort(String why, Throwable e) {
		}	
	}
}
