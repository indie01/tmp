package com.kickmogu.yodobashi.community.mapreduce.job;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.reduce.LongSumReducer;
import org.apache.hadoop.util.ToolRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.hadoop.JobTemplate;
import org.springframework.hadoop.configuration.JobFactoryBean;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.kickmogu.hadoop.mapreduce.job.AbstractMapReduceJob;
import com.kickmogu.hadoop.mapreduce.job.util.OptionUtils;
import com.kickmogu.hadoop.mapreduce.job.util.ThreadsafeOptionBuilder;
import com.kickmogu.lib.core.exception.CommonSystemException;
import com.kickmogu.lib.core.utils.Asserts;
import com.kickmogu.lib.hadoop.hbase.HBaseContainer;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseColumnFamilyMeta;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseTableMeta;

@Service
@Lazy
@Scope("prototype")
public class CommunityHBaseCounterTablesJob extends AbstractMapReduceJob {
	public static class DfsWorkDirMng {
		private static final String WORK_OUTPUT_DIR = "output";
		private static final String WORK_INPUT_DIR = "input";
		private static final String MAPPER_FILE = "part-%05d";

		private static final PathFilter hiddenFileFilter = new PathFilter() {
			public boolean accept(Path p) {
				String name = p.getName();
				return !name.startsWith("_") && !name.startsWith(".");
			}
		};

		protected Configuration conf;
		protected Path workDir;

		public DfsWorkDirMng(Configuration conf, String workPath) {
			Asserts.notEmpty(workPath);
			Asserts.notEquals("/", workPath);
			
			this.conf = conf;
			workDir = new Path(workPath);
		}

		protected void initWorkDir() throws Exception {
			FileSystem fs = FileSystem.get(conf);
			if (fs.exists(workDir)) {
				fs.delete(workDir, true);
			}
		}

		public Path getInputPath() {
			return new Path(workDir, WORK_INPUT_DIR);
		}

		public Path getOutputPath() {
			return new Path(workDir, WORK_OUTPUT_DIR);
		}

		protected void createInputTextFilePart(int id, List<String> lines) throws IOException {
			FileSystem fs = FileSystem.get(conf);
			Path file = new Path(new Path(workDir, WORK_INPUT_DIR), String.format(MAPPER_FILE, id));
			SequenceFile.Writer writer = null;
			try {
				long n = 0;
				writer = SequenceFile.createWriter(fs, conf, file, LongWritable.class, Text.class);
				for (String line : lines) {
					writer.append(new LongWritable(n++), new Text(line));
				}
			} finally {
				if (writer != null)
					writer.close();
			}
		}

		public FileStatus[] listResultFileStatus() throws IOException {
			FileSystem fs = FileSystem.get(conf);
			Path path = new Path(workDir, WORK_OUTPUT_DIR);
			FileStatus[] files = fs.listStatus(path, hiddenFileFilter);
			return files;
		}
	}

	public static final String WORK_DIR_KEY = "hbaseCounterTables.workDir";
	public static final String MAX_TIMESTAMP_KEY = "hbaseCounterTables.maxTimestamp";
	private DfsWorkDirMng workDir;

	@Autowired
	@Qualifier("MySite")
	protected HBaseContainer hbaseContainer;

	@Autowired
	@Qualifier("default")
	HBaseOperations hbaseOperations;

	public CommunityHBaseCounterTablesJob() {
		super();
		this.springConfigLocation = "classpath:/mr-context.xml";
		this.mapReduceConfigType = CommunityMapReduceConfig.class;
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
	public String getJobName() {
		return "hbaseCounterTables";
	}
	
	@Bean
	@Scope("prototype")
	public HBaseCounterTablesMapper hbaseCounterTablesMapper() {
		HBaseCounterTablesMapper mapper = new HBaseCounterTablesMapper();
		mapper.setHBaseContainer(hbaseContainer);
		return mapper;
	}

	@Bean
	@Scope("prototype")
	public Job hbaseCounterTables() throws Exception {
		// JOBクラス生成
		JobFactoryBean factory = getJobFactoryBean();
		factory.setMapper(hbaseCounterTablesMapper());
		Job job = factory.getObject();

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(LongWritable.class);
		job.setInputFormatClass(SequenceFileInputFormat.class);

		job.setReducerClass(LongSumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(LongWritable.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		job.setNumReduceTasks(1);

		return job;
	}

	@Override
	protected void initializeConfiguration(Properties properties, String[] args) throws Exception {
		super.initializeConfiguration(properties, args);
		CommunityMapReduceJobUtils.initializeConfiguration(this, properties, args);

		Options options = new Options();
		options.addOption(new ThreadsafeOptionBuilder()
				.withArgName("tableNames").hasArg(true).isRequired(true)
				.withDescription("tableNames")
				.withLongOpt("tableNames")
				.create("t"));
		options.addOption(new ThreadsafeOptionBuilder()
				.withArgName("workDir").hasArg(true).isRequired(false)
				.withDescription("workDir")
				.withLongOpt("workDir")
				.create("w"));
		options.addOption(new ThreadsafeOptionBuilder()
				.withArgName("maxTimestamp").hasArg(true).isRequired(false)
				.withDescription("maxTimestamp(inclusive)")
				.withLongOpt("maxTimestamp")
				.create("s"));

		CommandLine cmd = OptionUtils.parseWithDefaultOption(this.getJobName(), options, args, properties);

		String workDirParam = cmd.getOptionValue("w", "/tmp");
		workDirParam = workDirParam + "/" + getClass().getSimpleName() + "/";
		properties.setProperty(WORK_DIR_KEY, workDirParam);

		String maxTimestampParam = cmd.getOptionValue("s", "-1");
		properties.setProperty(MAX_TIMESTAMP_KEY, maxTimestampParam);
		
		workDir = new DfsWorkDirMng(getConf(), workDirParam);
		workDir.initWorkDir();
		
		properties.setProperty(JobTemplate.SPRING_INPUT_PATHS, workDir.getInputPath().toString());
		properties.setProperty(JobTemplate.SPRING_OUTPUT_PATH, workDir.getOutputPath().toString());

		String tablesParam = cmd.getOptionValue("t");
		String[] tables = tablesParam.split(",");
		for (int i = 0; i < tables.length; i++) {
			List<String> lines = Lists.newArrayList();
			lines.add(tables[i]);
			workDir.createInputTextFilePart(i, lines);
		}
	}

	@Override
	protected void executeForAfter() throws Exception {
		Configuration conf = getConf();
		FileSystem fs = FileSystem.get(conf);
		FileStatus[] files = workDir.listResultFileStatus();
		for (FileStatus fileStatus : files) {
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, fileStatus.getPath(), conf);
			try {
				Text key = new Text();
				LongWritable value = new LongWritable();
				while (reader.next(key, value)) {
					System.out.println(key + "\t" + value);
				}
			} finally {
				reader.close();
			}
		}
	}

	public static int execute(ApplicationContext parentApplicationContext, String[] args) throws Exception {
		return ToolRunner.run(new CommunityHBaseCounterTablesJob().setParentApplicationContext(parentApplicationContext), args);
	}

	public static void main(String[] args) throws Exception {
		int res = execute(null, args);
		System.out.println(res);
		System.exit(res);
	}

	public static class HBaseCounterTablesMapper extends Mapper<LongWritable, Text, Text, LongWritable> {

		private static final byte[] BOOLEAN_TRUE_AS_BYTES = Bytes.toBytes(true);
		protected HBaseContainer container;
		protected long maxTimestamp;

		public void setHBaseContainer(HBaseContainer hbaseContainer) {
			this.container = hbaseContainer;
		}

		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
			super.setup(context);
			Configuration conf = context.getConfiguration();
			maxTimestamp = Long.parseLong(conf.get(MAX_TIMESTAMP_KEY));
		}

		@Override
		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String tableName = value.toString();
			HBaseTableMeta tableMeta = container.getMeta().getTableMetaBySimpleClassName(tableName);
			long[] count = doCount(tableMeta, maxTimestamp);
			context.write(value, new LongWritable(count[0]));
			context.write(new Text(value + "_delete_row"), new LongWritable(count[1]));
		}
		
		private <T> long[] doCount(HBaseTableMeta tableMeta,final long maxTimeStamp) throws IOException {
			FilterList filterList = new FilterList();
			SingleColumnValueFilter notDeleteFilter = new SingleColumnValueFilter(tableMeta.getDeleteFlgColumnFamilyMeta().getColumnFamilyNameAsBytes(), HBaseColumnFamilyMeta.DELETE_FLG_COLUMN_NAME_AS_BYTES, CompareOp.NOT_EQUAL, BOOLEAN_TRUE_AS_BYTES);
			filterList.addFilter(notDeleteFilter);
			
			Scan scan = new Scan();
			if (maxTimeStamp != -1) {
				try {
					scan.setTimeRange(0, maxTimeStamp+1); // inclusive
				}
				catch (IOException e) {
					// Will never happen
					throw new CommonSystemException(e);
				}
			}
			scan.setFilter(filterList);
			scan.addColumn(tableMeta.getDeleteFlgColumnFamilyMeta().getColumnFamilyNameAsBytes(), HBaseColumnFamilyMeta.DELETE_FLG_COLUMN_NAME_AS_BYTES);		
			
			long[] count = new long[]{0,0};
			HTablePool pool = container.getHTablePool();
			HTableInterface hTableInterface = pool.getTable(tableMeta.getTableNameAsBytes());
			ResultScanner scanner = null;
			byte[] deleteFlgColumnFamilyNamsAsBytes = tableMeta.getDeleteFlgColumnFamilyMeta().getColumnFamilyNameAsBytes();
			try {
				scanner = hTableInterface.getScanner(scan);
				for (Result result : scanner) {
					KeyValue deleteFlgValue = result.getColumnLatest(deleteFlgColumnFamilyNamsAsBytes, HBaseColumnFamilyMeta.DELETE_FLG_COLUMN_NAME_AS_BYTES);
					if (deleteFlgValue == null || deleteFlgValue.getBuffer()[deleteFlgValue.getValueOffset()] == 0) {
						count[0]++;
					}
					else {
						count[1]++;
					}
				}
			} finally {
				if (scanner != null) {
					scanner.close();
				}
				pool.putTable(hTableInterface);
			}

			return count;
		}
	}
}
