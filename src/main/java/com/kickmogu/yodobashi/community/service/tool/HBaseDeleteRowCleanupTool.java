package com.kickmogu.yodobashi.community.service.tool;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.cxf.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.collect.Lists;
import com.kickmogu.hadoop.mapreduce.job.util.ThreadsafeOptionBuilder;
import com.kickmogu.lib.core.exception.CommonSystemException;
import com.kickmogu.lib.core.utils.Asserts;
import com.kickmogu.lib.core.utils.ThreadSafeDateFormat;
import com.kickmogu.lib.hadoop.hbase.HBaseContainer;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseTableMeta;
import com.kickmogu.lib.hadoop.hbase.service.HBaseDeleteRowCleanupService;
import com.kickmogu.yodobashi.community.common.utils.CommonsCliUtils;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityOperationStatus;
import com.kickmogu.yodobashi.community.service.SystemMaintenanceService;

public class HBaseDeleteRowCleanupTool {
	private static final Logger LOG = LoggerFactory.getLogger(HBaseDeleteRowCleanupTool.class);
	
	@Autowired
	private HBaseDeleteRowCleanupService hBaseDeleteRowCleanupService;
	
	@Autowired @Qualifier("MySite")
	private HBaseContainer hBaseContainer;
	
	/**
	 * システムメンテナンスサービスです。
	 */
	@Autowired
	private SystemMaintenanceService systemMaintenanceService;
	
	public void execute(List<String>tableNames, long timestamp, boolean onlyView) {
		
		// ReadOnlyモードチェック
		if (systemMaintenanceService.getCommunityOperationStatus().equals(CommunityOperationStatus.READONLY_OPERATION)) {
			LOG.warn("Stopped for " + CommunityOperationStatus.READONLY_OPERATION.getLabel());
			return;
		}
		
		List<Class<?>> tableTypes = Lists.newArrayList();
		for (String tableName:tableNames) {
			for (String t:tableName.split(",")) {
				HBaseTableMeta tableMeta = hBaseContainer.getMeta().getTableMetaBySimpleClassName(t);
				Asserts.notNull(tableMeta, "tableName invalid." + t);
				Asserts.isFalse(tableMeta.isExternalEntity(), "externalEntity." + t);
				tableTypes.add(tableMeta.getType());				
			}
		}
		for (Class<?> tableType:tableTypes) {
			hBaseDeleteRowCleanupService.cleanup(tableType, timestamp, onlyView);			
		}
	}
	
	public static void main(String[] args) {
		int result = 0;
		try {
			mainBody(args);
		} catch (Throwable th) {
			result = 1;
			th.printStackTrace();
		} finally {
			System.exit(result);			
		}
	}
	
	private static void mainBody(String[] args) {

		Options options = new Options();
		options.addOption(
			new ThreadsafeOptionBuilder().withArgName("view")
		    .hasArg(false)
		    .isRequired(false)
		    .withDescription("viewMode")
		    .create("view")
	    );
		options.addOption(
			new ThreadsafeOptionBuilder().withArgName("timestamp")
		    .hasArg(true)
		    .isRequired(true)
		    .withDescription("timestamp[yyyymmdd-hhmmss or now]")
		    .create("timestamp")
	    );		
		options.addOption(
			new ThreadsafeOptionBuilder().withArgName("tables")
		    .hasArg(true)
		    .isRequired(true)
		    .withValueSeparator(',')
		    .withDescription("tables(DONames) for clearing delete row.")
		    .create("tables")
	    );
		options.addOption(
			new ThreadsafeOptionBuilder().withArgName("appContextClassPath")
		    .hasArg(true)
		    .isRequired(true)
		    .withDescription("appContextClassPath")
		    .create("appContextClassPath")
	    );
		
		CommandLineParser parser = new PosixParser();
		CommandLine commandLine = null;
		try {
			commandLine = parser.parse(options, args);
		} catch (ParseException e) {
			CommonsCliUtils.argError(HBaseDeleteRowCleanupTool.class.getSimpleName(), options);
		}
		
		long timestamp = getTimestampFromOption(commandLine.hasOption("timestamp") ? commandLine.getOptionValue("timestamp") : null);
		List<String> tableNames = Lists.newArrayList();
		for (String tableName:commandLine.getOptionValues("tables")) {
			tableNames.add(tableName);
		}
		
		boolean viewMode = commandLine.hasOption("view");
		
		ApplicationContext context = new ClassPathXmlApplicationContext(commandLine.getOptionValue("appContextClassPath"));
		HBaseDeleteRowCleanupTool tool = new HBaseDeleteRowCleanupTool();
		context.getAutowireCapableBeanFactory().autowireBeanProperties(tool,
				AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
		
		tool.execute(tableNames, timestamp, viewMode);
	}
	
	private static Long getTimestampFromOption(String s) {
		if (StringUtils.isEmpty(s)) return System.currentTimeMillis();
		if (s.equals("now")) {
			return System.currentTimeMillis();
		} else if (s.startsWith("-")) {
			return System.currentTimeMillis() - Long.valueOf(s.substring(1));
		} else  {
			try {
				return ThreadSafeDateFormat.parse("yyyyMMdd-HHmmss", s).getTime();
			} catch (CommonSystemException e) {
				if (!(e.getCause() instanceof ParseException)) {
					throw e;
				}
				return Long.valueOf(s);
			}
		}
	}
	

}
