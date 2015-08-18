package com.kickmogu.yodobashi.community.mapreduce.job;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.MultiTableOutputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.ToolRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.hadoop.configuration.JobFactoryBean;
import org.springframework.stereotype.Service;

import com.kickmogu.hadoop.mapreduce.job.AbstractMapReduceJob;
import com.kickmogu.hadoop.mapreduce.job.util.OptionUtils;
import com.kickmogu.hadoop.mapreduce.job.util.ThreadsafeOptionBuilder;
import com.kickmogu.hadoop.mapreduce.parts.MultiTableInputFormat;
import com.kickmogu.hadoop.mapreduce.parts.MultiTableSplit;
import com.kickmogu.lib.core.utils.DateUtil;
import com.kickmogu.lib.hadoop.hbase.HBaseContainer;
import com.kickmogu.lib.hadoop.hbase.impl.HBaseDirectTemplate;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseTableMeta;

@Service
@Lazy
@Scope("prototype")
public class CommunityHBaseReIndexJob extends AbstractMapReduceJob {
	
	protected static Log LOG = LogFactory.getLog(CommunityHBaseReIndexJob.class);
	
	@Autowired
	@Qualifier("MySite")
	protected HBaseContainer hbaseContainer;

	@Autowired 
	@Qualifier("MySite")
	protected HBaseDirectTemplate hbaseDirectTemplate;

	@Override
	public String getJobName() {
		return "hbaseReIndex";
	}
	
	@Bean
	@Scope("prototype")
	public MultiTableInputFormat hbaseReIndexInputFormat() throws IOException {
		MultiTableInputFormat inputFormat = new MultiTableInputFormat();
		inputFormat.setConf(extConfiguration);
		Scan scan = new Scan();
		scan.addFamily(Bytes.toBytes("cf"));
		scan.setTimeRange(0L, Long.parseLong(extConfiguration.get("timestamp")));
		scan.setMaxVersions(1);
		scan.setCaching(1000);
		for (String tableName:extConfiguration.get("inputTables").split(",")) {
			inputFormat.addTableInputInfo(tableName, scan);
		}
		return inputFormat;
	}
	
	@Bean
	@Scope("prototype")
	public MultiTableOutputFormat hbaseReIndexOutputFormat() {
		MultiTableOutputFormat outputFormat = new MultiTableOutputFormat();
		return outputFormat;
	}
	
	@Bean
	@Scope("prototype")
	public HBaseReIndexMapper hbaseReIndexMapper() {
		HBaseReIndexMapper mapper = new HBaseReIndexMapper();
		mapper.setHBaseTemplate(hbaseDirectTemplate);
		mapper.setHBaseContainer(hbaseContainer);
		return mapper;
	}
	
	@Bean
	@Scope("prototype")
	public Job hbaseReIndex() throws Exception {
		JobFactoryBean factory = getJobFactoryBean();
		factory.setInputFormat(hbaseReIndexInputFormat());
		factory.setOutputFormat(hbaseReIndexOutputFormat());
		factory.setMapper(hbaseReIndexMapper());

		Job job = factory.getObject();
		job.setMapOutputKeyClass(ImmutableBytesWritable.class);
		job.setMapOutputValueClass(Put.class);
		job.setNumReduceTasks(0);
		
		return job;
	}
	
	public static class HBaseReIndexMapper extends Mapper<ImmutableBytesWritable, Result, ImmutableBytesWritable, Put> {
		protected HBaseContainer container;
		protected HBaseDirectTemplate hbaseTemplate;
		private String tableName;
		
		public void setHBaseContainer(HBaseContainer hbaseContainer) {
			this.container = hbaseContainer;
		}
		public void setHBaseTemplate(HBaseDirectTemplate hbaseTemplate) {
			this.hbaseTemplate = hbaseTemplate;
		}

		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
			tableName = Bytes.toString(((MultiTableSplit)context.getInputSplit()).getTableName());
			LOG.info("setup:" + tableName);
		}

		@Override
		protected void map(ImmutableBytesWritable key, Result result, Context context) throws IOException, InterruptedException {
			HBaseTableMeta tableMeta = container.getMeta().getTableMetaBySimpleClassName(tableName);

			Map<String, Put> puts = hbaseTemplate.makeIndexRecord(tableMeta, result);
			for (Map.Entry<String, Put> entry:puts.entrySet()) {
				LOG.info("map:" + entry.getKey());
				ImmutableBytesWritable indexTableName = new ImmutableBytesWritable(Bytes.toBytes(entry.getKey()));
				context.write(indexTableName, entry.getValue());
			}
			
		}

		@Override
		protected void cleanup(Context context) throws IOException, InterruptedException {
			LOG.info("cleanup");
		}
	}

	public CommunityHBaseReIndexJob() {
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
		
		Options options = new Options();
		options.addOption(new ThreadsafeOptionBuilder()
			.withArgName("tableNames").hasArg(true).isRequired(true)
			.withDescription("tableNames").withLongOpt("tableNames").create("t"));
		options.addOption(new ThreadsafeOptionBuilder()
			.withArgName("timestamp").hasArg(true).isRequired(false)
			.withDescription("timestamp").withLongOpt("timestamp").create("s"));

		
		CommandLine cmd = OptionUtils.parseWithDefaultOption(this.getJobName(), options, args, properties);

		long timestamp = cmd.hasOption("s") ? DateUtil.parseDate(cmd.getOptionValue("s")).getTime() : Long.valueOf(System.currentTimeMillis());

		String tableNames = cmd.getOptionValue("t");
		
		properties.setProperty("inputTables", tableNames);
		properties.setProperty("timestamp", Long.valueOf(timestamp + 1L).toString());
		CommunityMapReduceJobUtils.initializeConfiguration(this, properties, args);
	}

	public static int execute(ApplicationContext parentApplicationContext, String[] args) throws Exception {
		return ToolRunner.run(new CommunityHBaseReIndexJob().setParentApplicationContext(parentApplicationContext), args);
	}

	public static void main(String[] args) throws Exception {
		int res = execute(null, args);
		System.out.println(res);
		System.exit(res);
	}
}
