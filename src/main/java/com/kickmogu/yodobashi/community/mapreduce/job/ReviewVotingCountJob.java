package com.kickmogu.yodobashi.community.mapreduce.job;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.hadoop.JobTemplate;
import org.springframework.hadoop.configuration.JobFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.kickmogu.hadoop.mapreduce.job.util.ThreadsafeOptionBuilder;
import com.kickmogu.lib.core.utils.ConfigUtil;
import com.kickmogu.lib.hadoop.hbase.io.HBaseTableInputFormat;
import com.kickmogu.lib.solr.SolrConstants;
import com.kickmogu.yodobashi.community.resource.config.HBaseConfig;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.service.VotingService;

/**
 * レビューの評価データ（レビューが参考になったor参考にならなかった）を出力します。<br/>
 * （ページ上に表示されているレビューを対象とする）
 * ※SASによるデータ分析で使用
 *
 * @author m.takahashi
 *
 */
@Service @Lazy @Scope("prototype")
public class ReviewVotingCountJob extends AbstractCommunityJob {

	private static final Logger logger = LoggerFactory.getLogger(ReviewVotingCountJob.class);

	// JOB名
	private static final String JOB_NAME = "reviewVotingCount";
	// 出力一時ディレクトリ
	private static String tempDir = "";
	// Service Context
	private static final String SERVICE_CONTEXT = "serviceContext.xml";
	@Autowired
	private VotingService votingService;

	/**
	 * entry point
	 * @param args 出力ディレクトリ
	 */
	public static void main(String[] args) {
		logger.info("START ---------------------------------------------------");

		// 引数チェック
		CommandLine commandLine = null;
		try {
			commandLine = checkArgs(args, new PosixParser());
			// 出力ディレクトリ
			tempDir = commandLine.getOptionValue("d");
		} catch (RuntimeException e) {
			e.printStackTrace();
			// 異常終了
			logger.info("処理が異常終了しました。");
			System.exit(1);
		}

		ApplicationContext applicationContext = new ClassPathXmlApplicationContext(SERVICE_CONTEXT);
		int result = 0;
		try {
			result = execute(applicationContext, args);
			logger.info("result=" + "["+result+"]");
			if (result != 0) {
				logger.error("failed. ["+result+"]");
				System.exit(result);
			}
		} catch (Throwable th) {
			logger.error("failed.", th);
			th.printStackTrace();
			// 異常終了
			System.exit(1);
		}
		logger.info("END ----------------------------------------------------");
		// 正常終了
		System.exit(0);
	}

	/**
	 * @param args
	 * @param parser
	 * @return
	 */
	private static CommandLine checkArgs(String[] args, CommandLineParser parser) {
		Options options = new Options();
		// 出力ディレクトリ
		options.addOption(new ThreadsafeOptionBuilder()
								.withArgName("output directory")
								.hasArg(true)
								.isRequired(true)
								.withDescription("output directory")
								.withLongOpt("outputDir")
								.create("d"));

		CommandLine commandLine = null;
		try {
			commandLine =  parser.parse(options, args);
		} catch (org.apache.commons.cli.ParseException e) {
			argError(options);
			throw new RuntimeException("ParseException", e);
		}

		return commandLine;
	}
	/**
	 * @param options
	 */
	private static void argError(Options options) {
		HelpFormatter help = new HelpFormatter();
		help.setWidth(120);
		help.printHelp(ReviewVotingCountJob.class.getSimpleName(), options, true);
	}

	/**
	 *
	 * @param parentContext
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public static int execute(ApplicationContext parentContext, String[] args) throws Exception {
		logger.info("-------------------------------------------------");
		logger.info("- execute ");
		logger.info("-------------------------------------------------");
		return ToolRunner
				.run(new ReviewVotingCountJob().setParentApplicationContext(parentContext),
					args);
	}

	/**
	 * コンフィグの初期化を実行します。
	 * @param properties コンフィグに読み込まれるプロパティ
	 * @param args パラメーター
	 * @throws Exception 例外が発生した場合
	 */
	@Override
	protected void initializeConfiguration(
			Properties properties,
			String[] args) throws Exception {
		print("---initializeConfiguration");

		super.initializeConfiguration(properties, args);
		properties.setProperty(JobTemplate.SPRING_OUTPUT_PATH, tempDir);
		FileSystem fs = FileSystem.get(getConf());
		Path path = new Path(tempDir);
		if (fs.exists(path)) {
			fs.delete(path, true);
		}
		HBaseConfig config = new HBaseConfig();
		ConfigUtil.loadProperties(getProfileName(), "resource-config", config);
		properties.setProperty(TableInputFormat.INPUT_TABLE,
				StringUtils.uncapitalize(config.hbaseTableNamePrefix + ReviewDO.class.getSimpleName()));
		properties.setProperty(TableInputFormat.SCAN_COLUMNS, "cf:status");
		properties.setProperty(HBaseTableInputFormat.DELETE_FLG_FAMILY, "cf");
	}

	/**
	 * JOB名を返します。
	 * @return Job名です。
	 */
	@Override
	public String getJobName() {
		return JOB_NAME;
	}

	/**
	 * ジョブインスタンスを返します。<br />
	 * Job名 の Bean 名で定義します。
	 * @return ジョブインスタンス
	 * @throws Exception 例外が発生した場合
	 */
	@Bean @Scope("prototype")
	public Job reviewVotingCount() throws Exception {
		print("[reviewVotingCount]");

		JobFactoryBean factory = getJobFactoryBean();
		factory.setInputFormat(reviewVotingCountInputFormat());
		factory.setOutputFormat(reviewVotingCountOutputFormat());
		factory.setMapper(reviewVotingCountMapper());
		factory.setReducer(reviewVotingCountReducer());
		Job job = factory.getObject();
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(NullWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		return job;
	}


	/**
	 * 入力フォーマットインスタンスを返します。<br />
	 * Job名 + InputFormat の Bean 名で定義します。
	 * @return 入力フォーマットインスタンス
	 */
	@Bean @Scope("prototype")
	public HBaseTableInputFormat reviewVotingCountInputFormat() {
		HBaseTableInputFormat inputFormat = new HBaseTableInputFormat();
		inputFormat.setConf(extConfiguration);
		return inputFormat;
	}

	/**
	 * 出力フォーマットインスタンスを返します。<br />
	 * Job名 + OutputFormat の Bean 名で定義します。
	 * @return 入力フォーマットインスタンス
	 * @throws ParseException 例外が発生した場合
	 */
	@Bean @Scope("prototype")
	public TextOutputFormat<Text,Text> reviewVotingCountOutputFormat() throws ParseException {
		return new TextOutputFormat<Text,Text>();
	}

	/**
	 * マップインスタンスを返します。<br />
	 * Job名 + Mapper の Bean 名で定義します。
	 * @return マップインスタンス
	 */
	@Bean @Scope("prototype")
	public ReviewVotingCountMapper reviewVotingCountMapper() {
		ReviewVotingCountMapper mapper = new ReviewVotingCountMapper();
		return mapper;
	}

	/**
	 * Reduerインスタンスを返します。<br />
	 * Job名 + Reducer の Bean 名で定義します。
	 * @return Reduerインスタンス
	 */
	@Bean @Scope("prototype")
	public ReviewVotingCountReducer reviewVotingCountReducer() {
		ReviewVotingCountReducer reducer = new ReviewVotingCountReducer();
		reducer.setVotingService(votingService);
		return reducer;
	}


	/**
	 * Mapper：対象データからSKUを取り出します。
	 *
	 */
	public static class ReviewVotingCountMapper extends Mapper<ImmutableBytesWritable, Result, Text, NullWritable> {

		private Text reviewId  = new Text();

		private long mapIn = 0;
		private long mapOut = 0;

		/**
		 *
		 */
		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
		}

		/**
		 *
		 */
		@Override
		public void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
			logger.info("map ----------------------------------");
			mapIn++;

			String reviewIdStr = Bytes.toString(key.get());
			reviewId.set(reviewIdStr);

			String status = "";
			for (KeyValue keyValue : value.raw()) {
				String columnName = Bytes.toString(
						keyValue.getBuffer(),
						keyValue.getQualifierOffset(),
						keyValue.getQualifierLength());
				if ("status".equals(columnName)) {
					status = Bytes.toString(
							keyValue.getBuffer(),
							keyValue.getValueOffset(),
							keyValue.getValueLength());
					break;
				}
			}
//			logger.info(reviewIdStr+":"+status);

			if (ContentsStatus.SUBMITTED.getCode().equals(status)) {
				context.write(reviewId, NullWritable.get());
				mapOut++;
//				logger.info(reviewIdStr+":"+status);
			}
		}

		@Override
		protected void cleanup(Context context)
				throws IOException, InterruptedException {
			super.cleanup(context);
			logger.info("mapIn:"+String.valueOf(mapIn));
			logger.info("mapOut:"+String.valueOf(mapOut));
		}


	}

	private void print(Object o) {
//		System.out.println(o);
		logger.info(String.valueOf(o));
	}
	/**
	 * Reducer：評価データを作成します。
	 *
	 */
	public static class ReviewVotingCountReducer extends Reducer<Text, NullWritable, Text, Text> {

		// 検索制限数
		private static final int READ_LIMIT = SolrConstants.QUERY_ROW_LIMIT;
		// レビューIDのリスト
		private List<String> reviewIds = null;

		private VotingService votingService;
		/**
		 * @param votingService
		 */
		public void setVotingService(VotingService votingService) {
			this.votingService = votingService;
		}

		private long redIn = 0;
		private long redOut = 0;

		/**
		 *
		 */
		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
			reviewIds = new ArrayList<String>();
		}

		/**
		 *
		 */
		@Override
		public void reduce(Text key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
			logger.info("reduce ----------------------------------");
			redIn++;

			String reviewId = key.toString();
			reviewIds.add(reviewId);

//			logger.info(reviewId);
//			logger.info(String.valueOf(reviewIds.size()));
			if (reviewIds.size() >= READ_LIMIT) {
				write(context);
			}
		}

		/**
		 * 後処理を行います。
		 */
		@Override
		protected void cleanup(Context context) throws IOException, InterruptedException {
			if (reviewIds.size() > 0) {
				write(context);
			}
			logger.info("redIn:"+String.valueOf(redIn));
			logger.info("redOut"+String.valueOf(redOut));
		}

		/**
		 * @param context
		 * @throws IOException
		 * @throws InterruptedException
		 */
		private void write(Context context) throws IOException, InterruptedException {
			Map<String, Long[]> map = votingService.loadReviewVotingCountMap(reviewIds);
			for (String reviewId : map.keySet()) {
				Long[] counts = map.get(reviewId);
				Long yes = counts[0];
				Long no = counts[1];
				yes = yes == null ? 0 : counts[0];
				no = no == null ? 0 : counts[1];
				if (0 == yes && 0 == no) {
					logger.info("yes=0 & no=0");
					continue;
				}
				redOut++;

				String yesCount = String.valueOf(yes);
				String noCount = String.valueOf(no);
				StringBuilder sb = new StringBuilder().append(yesCount).append("\t").append(noCount);
				Text counters = new Text(sb.toString());
				context.write(new Text(reviewId), counters);

//				logger.info(String.valueOf(redOut)+":"+reviewId+"("+yesCount+")("+noCount+")");
			}
			reviewIds.clear();
		}
	}

}
