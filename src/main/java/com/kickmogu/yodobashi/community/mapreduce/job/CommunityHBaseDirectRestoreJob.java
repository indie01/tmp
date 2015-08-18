package com.kickmogu.yodobashi.community.mapreduce.job;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.hadoop.configuration.JobFactoryBean;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.kickmogu.lib.hadoop.hbase.HBaseContainer;
import com.kickmogu.lib.hadoop.hbase.backup.HBaseDirectRestoreJob;
import com.kickmogu.lib.hadoop.hbase.backup.HBaseRestoreService;
import com.kickmogu.lib.hadoop.hbase.io.HBaseDump;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseTableMeta;

@Service
@Lazy
@Scope("prototype")
public class CommunityHBaseDirectRestoreJob extends HBaseDirectRestoreJob {

	private static final String WORK_OUTPUT_DIR = "output";

	private static final String WORK_INPUT_DIR = "input";

	// ※InputFormat.getSplit()のoverrideでMapReduceの読み時にファイル分割ができそう
	private static final String MAPPER_FILE = "part-%05d";

	@Autowired
	@Qualifier("MySite")
	protected HBaseContainer hbaseContainer;

	@Autowired  @Qualifier("direct")
	protected HBaseRestoreService service;

	public CommunityHBaseDirectRestoreJob() {
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

	private static final PathFilter hiddenFileFilter = new PathFilter() {
		public boolean accept(Path p) {
			String name = p.getName();
			return !name.startsWith("_") && !name.startsWith(".");
		}
	};

	private static final PathFilter hdrFileFilter = new PathFilter() {
		public boolean accept(Path p) {
			String name = p.getName();
			return !name.startsWith("_") && !name.startsWith(".") && name.toLowerCase().endsWith(".hdr");
		}
	};

	/**
	 * マップインスタンスを返します。<br />
	 * Job名 + Mapper の Bean 名で定義します。
	 * 
	 * @return マップインスタンス
	 */
	@Bean
	@Scope("prototype")
	public HBaseDirectRestoreMapper hbaseDirectRestoreMapper() {
		HBaseDirectRestoreMapper mapper = new HBaseDirectRestoreMapper();
		mapper.setHBaseRestoreService(service);
		mapper.setHBaseContainer(hbaseContainer);
		return mapper;
	}

	@Bean
	@Scope("prototype")
	public Job hbaseDirectRestore() throws Exception {

		Path workDir = new Path(extConfiguration.get(WORK_DIR_KEY));
		Path inputDir = new Path(extConfiguration.get(INPUT_DIR_KEY));
		long splitWaitSize = Long.parseLong(extConfiguration.get(SPLIT_WAIT_SIZE));
		Path tempInputPath = new Path(workDir, WORK_INPUT_DIR);
		Path outputPath = new Path(workDir, WORK_OUTPUT_DIR);

		// コマンドファイル作成
		makeCommandFiles(extConfiguration, inputDir, tempInputPath, splitWaitSize);

		// JOBクラス生成
		JobFactoryBean factory = getJobFactoryBean();
		factory.setMapper(hbaseDirectRestoreMapper());
		Job job = factory.getObject();

		FileInputFormat.addInputPath(job, tempInputPath);
		FileOutputFormat.setOutputPath(job, outputPath);

		job.setMapOutputKeyClass(LongWritable.class);
		job.setMapOutputValueClass(RestoreCommandWriterble.class);
		job.setInputFormatClass(SequenceFileInputFormat.class);

		job.setReducerClass(HBaseDirectRestoreReducer.class);
		job.setOutputKeyClass(LongWritable.class);
		job.setOutputValueClass(RestoreCommandWriterble.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		job.setNumReduceTasks(1);

		return job;
	}

	@Override
	protected void initializeConfiguration(Properties properties, String[] args) throws Exception {
		super.initializeConfiguration(properties, args);
		CommunityMapReduceJobUtils.initializeConfiguration(this, properties, args);

		String workDirString = properties.getProperty(WORK_DIR_KEY) + "/" + getClass().getSimpleName() + "/";
		properties.setProperty(WORK_DIR_KEY, workDirString);

		FileSystem fs = FileSystem.get(getConf());
		Path workDir = new Path(workDirString);
		if (fs.exists(workDir)) {
			fs.delete(workDir, true);
		}
	}

	protected void writeComamndFile(FileSystem fs, Configuration conf, Path outDir, int id, List<RestoreCommandWriterble> commands) throws IOException {
		Path file = new Path(outDir, String.format(MAPPER_FILE, id));
		SequenceFile.Writer writer = null;
		try {
			long n = 0;
			System.out.println("split commands file-id:" + id);
			writer = SequenceFile.createWriter(fs, conf, file, LongWritable.class, RestoreCommandWriterble.class);
			for (RestoreCommandWriterble cmd : commands) {
				System.out.println(cmd);
				writer.append(new LongWritable(n++), cmd);
			}
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	public void makeCommandFiles(Configuration conf, Path inDir, Path outDir, long splitWaitSize) throws IOException {
		FileSystem fs = FileSystem.get(conf);

		// 各Mapperへの入力ファイルを作成
		long sumWait = 0;
		int n = 0;
		List<RestoreCommandWriterble> commands = Lists.newArrayList();
		FileStatus[] files = fs.listStatus(inDir, hdrFileFilter);
		for (FileStatus file : files) {
			Path filePath = file.getPath();
			HBaseDump.Header header = readHeader(fs, filePath);
			String hdrFilePathString = filePath.getName();
			String tableName = header.getTableNameWithoutPrefix();

			HBaseTableMeta tableMeta = hbaseContainer.getMeta().getTableMetaBySimpleClassName(tableName);
			long wait = header.getRowSize() + tableMeta.getIndexes().size() * (header.getRowSize() / 2);
			if (wait == 0) {
				continue;
			}

			RestoreCommandWriterble cmd = new RestoreCommandWriterble(tableName, hdrFilePathString);
			cmd.setRowSize(header.getRowSize());

			if (wait > splitWaitSize) {
				// TODO １テーブルのデータが極端に大きい場合できれば分割して処理したい
				List<RestoreCommandWriterble> single = Lists.newArrayList();
				single.add(cmd);
				writeComamndFile(fs, conf, outDir, n++, single);
			} else {
				if ((sumWait + wait) > splitWaitSize) {
					writeComamndFile(fs, conf, outDir, n++, commands);
					sumWait = 0;
					commands.clear();
				}
				sumWait += wait;
				commands.add(cmd);
			}
		}
		if (commands.size() > 0) {
			writeComamndFile(fs, conf, outDir, n++, commands);
		}

		if (n == 0) {
			// MapReduceが起動するため、DIRがないとエラーがでるためその防止
			fs.mkdirs(outDir);
		}
	}

	public HBaseDump.Header readHeader(FileSystem fs, Path filePath) throws IOException {
		FSDataInputStream inHdr = null;
		try {
			HBaseDump.Header header = new HBaseDump.Header();
			inHdr = fs.open(filePath);
			header.readFields(inHdr);
			return header;
		} finally {
			IOUtils.closeStream(inHdr);
		}
	}

	protected void executeForAfter() throws Exception {
		Configuration conf = getConf();
		FileSystem fs = FileSystem.get(conf);
		Path path = new Path(conf.get(WORK_DIR_KEY), WORK_OUTPUT_DIR);
		FileStatus[] files = fs.listStatus(path, hiddenFileFilter);
		boolean success = true;
		for (FileStatus fileStatus : files) {
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, fileStatus.getPath(), conf);
			try {
				LongWritable key = new LongWritable();
				RestoreCommandWriterble value = new RestoreCommandWriterble();
				while (reader.next(key, value)) {
					if (value.getRetCode() != 0) {
						System.out.println("[error]" + value);
						success = false;
					}
				}
			} finally {
				reader.close();
			}
		}
		if (success) {
			System.out.println("success full.");
		}
	}

	public static int execute(ApplicationContext parentApplicationContext, String[] args) throws Exception {
		return ToolRunner.run(new CommunityHBaseDirectRestoreJob().setParentApplicationContext(parentApplicationContext), args);
	}

	public static void main(String[] args) throws Exception {
		int res = execute(null, args);
		System.out.println(res);
		System.exit(res);
	}

	public static class RestoreCommandWriterble implements Writable {
		protected int taskId;
		protected int retCode;
		protected String tableName;
		protected long rowSize;
		protected String hdrFileName;

		public RestoreCommandWriterble() {
		}

		public RestoreCommandWriterble(String tableName, String hdrFileName) {
			this.tableName = tableName;
			this.hdrFileName = hdrFileName;
		}

		@Override
		public String toString() {
			return "RestoreCommandWriterble [taskId=" + taskId + ", retCode=" + retCode + ", tableName=" + tableName + ", rowSize=" + rowSize
					+ ", hdrFilePath=" + hdrFileName + "]";
		}

		public int getTaskId() {
			return taskId;
		}

		public void setTaskId(int taskId) {
			this.taskId = taskId;
		}

		public int getRetCode() {
			return retCode;
		}

		public void setRetCode(int retCode) {
			this.retCode = retCode;
		}

		public String getTableName() {
			return tableName;
		}

		public void setTableName(String tableName) {
			this.tableName = tableName;
		}

		public long getRowSize() {
			return rowSize;
		}

		public void setRowSize(long rowSize) {
			this.rowSize = rowSize;
		}

		public String getHdrFileName() {
			return hdrFileName;
		}

		public String getDumpFileName() {
			return hdrFileName.replace("hdr", "dump").replace("HDR", "dump");
		}

		public void setHdrFilePath(String hdrFileName) {
			this.hdrFileName = hdrFileName;
		}

		@Override
		public void write(DataOutput out) throws IOException {
			out.writeInt(taskId);
			out.writeInt(retCode);
			out.writeUTF(tableName);
			out.writeLong(rowSize);
			out.writeUTF(hdrFileName);
		}

		@Override
		public void readFields(DataInput in) throws IOException {
			taskId = in.readInt();
			retCode = in.readInt();
			tableName = in.readUTF();
			rowSize = in.readLong();
			hdrFileName = in.readUTF();
		}
	}

	public static class HBaseDirectRestoreMapper extends Mapper<LongWritable, RestoreCommandWriterble, LongWritable, RestoreCommandWriterble> {

		protected HBaseContainer container;
		protected HBaseRestoreService service;
		protected String inputDirName;
		protected int bulkSize;

		public void setHBaseRestoreService(HBaseRestoreService hbaseRestoreService) {
			this.service = hbaseRestoreService;
		}

		public void setHBaseContainer(HBaseContainer hbaseContainer) {
			this.container = hbaseContainer;
		}

		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
			super.setup(context);
			Configuration conf = context.getConfiguration();
			inputDirName = conf.get(HBaseDirectRestoreJob.INPUT_DIR_KEY);
			bulkSize = Integer.parseInt(conf.get(HBaseDirectRestoreJob.BULK_OUTPUT));
		}

		@Override
		protected void map(LongWritable key, RestoreCommandWriterble value, Context context) throws IOException, InterruptedException {
			value.setTaskId(context.getTaskAttemptID().getTaskID().getId());
			FSDataInputStream inHdr = null;
			FSDataInputStream inDmp = null;
			try {
				String tableName = value.getTableName();
				Path hdr = new Path(inputDirName, value.getHdrFileName());
				Path dmp = new Path(inputDirName, value.getDumpFileName());

				Class<?> type = container.getMeta().getTableMetaBySimpleClassName(tableName).getType();
				FileSystem fs = FileSystem.get(context.getConfiguration());
				inHdr = fs.open(hdr);
				inDmp = fs.open(dmp);
				service.restore(type, inDmp, inHdr, bulkSize);

				value.setRetCode(0);
			} catch (Exception e) {
				e.printStackTrace();
				value.setRetCode(-1);
			} finally {
				IOUtils.closeStream(inHdr);
				IOUtils.closeStream(inDmp);
			}
			context.write(key, value);
		}
	}

	public static class HBaseDirectRestoreReducer extends Reducer<LongWritable, RestoreCommandWriterble, LongWritable, RestoreCommandWriterble> {
		public void reduce(LongWritable key, Iterable<RestoreCommandWriterble> values, Context context) throws IOException, InterruptedException {
			for (RestoreCommandWriterble value : values) {
				context.write(key, value);
			}
		}
	}
}
