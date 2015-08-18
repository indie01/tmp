package com.kickmogu.yodobashi.community.mapreduce.job;

import java.io.IOException;
import java.text.ParseException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.hadoop.JobTemplate;
import org.springframework.hadoop.configuration.JobFactoryBean;
import org.springframework.stereotype.Service;

import com.kickmogu.hadoop.mapreduce.job.util.ThreadsafeOptionBuilder;
import com.kickmogu.hadoop.mapreduce.parts.MultiTableInputFormat;
import com.kickmogu.hadoop.mapreduce.parts.MultiTableSplit;

/**
 * レビュー、質問、質問回答、画像の各データからSKUを抽出し、
 * SKUにページ(index.html等)を付加した情報をファイルに出力します。
 *
 * @author m.takahashi
 *
 */
@Service @Lazy @Scope("prototype")
public class SkuCollectorJob extends AbstractCommunityJob {

	private static final Logger logger = LoggerFactory.getLogger(SkuCollectorJob.class);

	// JOB名
	private static final String JOB_NAME = "skuCollector";

	// レビュー
	private static final String TABLE_REVIEW = "reviewDO";
	// 質問
	private static final String TABLE_QUESTION = "questionDO";
	// 質問回答
	private static final String TABLE_QUESTION_ANSWER = "questionAnswerDO";
	// 画像ヘッダ
	private static final String TABLE_IMAGE_HEADER = "imageHeaderDO";

	// Service Context
	private static final String SERVICE_CONTEXT = "serviceContext.xml";

	// 出力ディレクトリ
	private static String dataDir = "";

	private byte[] family_bytes = Bytes.toBytes("cf");
	private byte[] df_bytes = Bytes.toBytes("DF");
	private byte[] productId_bytes = Bytes.toBytes("productId");

	/**
	 * entry point
	 * @param args 出力ディレクトリ
	 */
	public static void main(String[] args) {
		logger.info("START");

		// 引数チェック
		CommandLine commandLine = null;
		try {
			commandLine = checkArgs(args, new PosixParser());
			// 出力ディレクトリ
			dataDir = commandLine.getOptionValue("d");
		} catch (RuntimeException e) {
			e.printStackTrace();
			// 異常終了
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
		logger.info("END");
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
		help.printHelp(SkuCollectorJob.class.getSimpleName(), options, true);
	}

	/**
	 *
	 * @param parentContext
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public static int execute(ApplicationContext parentContext, String[] args) throws Exception {
		return ToolRunner
				.run(new SkuCollectorJob().setParentApplicationContext(parentContext),
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

		super.initializeConfiguration(properties, args);

		properties.setProperty(JobTemplate.SPRING_OUTPUT_PATH, dataDir);

		FileSystem fs = FileSystem.get(getConf());
		Path path = new Path(dataDir);
		if (fs.exists(path)) {
			fs.delete(path, true);
		}
	}

	/**
	 * JOB名を返します。
	 * @return Job名です。
	 */
	@Override
	public String getJobName() {
		return JOB_NAME;
	}

//	@Override
//	protected void executeForAfter() throws Exception {
//		logger.info("[executeForAfter]");
//	}

	/**
	 * ジョブインスタンスを返します。<br />
	 * Job名 の Bean 名で定義します。
	 * @return ジョブインスタンス
	 * @throws Exception 例外が発生した場合
	 */
	@Bean @Scope("prototype")
	public Job skuCollector() throws Exception {
		System.out.println("[skuCollector]");

		JobFactoryBean factory = getJobFactoryBean();
		factory.setInputFormat(skuCollectorInputFormat());
		factory.setOutputFormat(skuCollectorOutputFormat());
		factory.setMapper(skuCollectorMapper());
		factory.setReducer(skuCollectorReducer());

		Job job = factory.getObject();
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(NullWritable.class);
		return job;
	}

	/**
	 * 入力フォーマットインスタンスを返します。<br />
	 * Job名 + InputFormat の Bean 名で定義します。
	 * @return 入力フォーマットインスタンス
	 * @throws ParseException 例外が発生した場合
	 */
	@Bean @Scope("prototype")
	public MultiTableInputFormat skuCollectorInputFormat() throws ParseException {

		MultiTableInputFormat inputFormat = new MultiTableInputFormat();
		try {
			inputFormat.setConf(extConfiguration);

			FilterList filterList = new FilterList();

			// 論理削除を対象外とするFilter
			SingleColumnValueFilter ldelFlgFilter =
					new SingleColumnValueFilter(family_bytes, df_bytes, CompareFilter.CompareOp.EQUAL, Bytes.toBytes(false));
			ldelFlgFilter.setFilterIfMissing(false);
			filterList.addFilter(ldelFlgFilter);

			Scan scan = new Scan();
			scan.addColumn(family_bytes, productId_bytes);
			scan.setMaxVersions(1);
			scan.setCaching(1000);
			scan.setFilter(filterList);

			// レビュー
			inputFormat.addTableInputInfo(TABLE_REVIEW, scan);
			// 質問
			inputFormat.addTableInputInfo(TABLE_QUESTION, scan);
			// 質問回答
			inputFormat.addTableInputInfo(TABLE_QUESTION_ANSWER, scan);
			// 画像ヘッダ
			inputFormat.addTableInputInfo(TABLE_IMAGE_HEADER, scan);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return inputFormat;
	}

	/**
	 * 出力フォーマットインスタンスを返します。<br />
	 * Job名 + OutputFormat の Bean 名で定義します。
	 * @return 入力フォーマットインスタンス
	 * @throws ParseException 例外が発生した場合
	 */
	@Bean @Scope("prototype")
	public TextOutputFormat<Text,NullWritable> skuCollectorOutputFormat() throws ParseException {
		return new TextOutputFormat<Text,NullWritable>();
	}

	/**
	 * マップインスタンスを返します。<br />
	 * Job名 + Mapper の Bean 名で定義します。
	 * @return マップインスタンス
	 */
	@Bean @Scope("prototype")
	public SkuCollectorMapper skuCollectorMapper() {
		return new SkuCollectorMapper();
	}

	/**
	 * Reduerインスタンスを返します。<br />
	 * Job名 + Reducer の Bean 名で定義します。
	 * @return Reduerインスタンス
	 */
	@Bean @Scope("prototype")
	public SkuCollectorReducer skuCollectorReducer() {
		SkuCollectorReducer reducer = new SkuCollectorReducer();
		return reducer;
	}


	/**
	 * Mapper：対象データからSKUを取り出します。
	 *
	 */
	public static class SkuCollectorMapper extends Mapper<ImmutableBytesWritable, Result, Text, NullWritable> {

		private byte[] family_bytes = Bytes.toBytes("cf");
		private byte[] productId_bytes = Bytes.toBytes("productId");

		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
			String tableName = Bytes.toString(((MultiTableSplit)context.getInputSplit()).getTableName());

			logger.info(tableName);
		}

		@Override
		public void map(ImmutableBytesWritable nameAndRow, Result record, Context context) throws IOException, InterruptedException {
			byte[] sku_bytes = record.getValue(family_bytes, productId_bytes);
			if (sku_bytes == null) {
				return;
			}
			String sku = Bytes.toString(sku_bytes);
			if (sku.isEmpty()) {
				return;
			}
			logger.info("map sku:"+sku);

			context.write(new Text(sku), NullWritable.get());
		}
	}

	/**
	 * Reducer：SKUからサイトマップURLを作成します。
	 *
	 */
	public static class SkuCollectorReducer extends Reducer<Text, NullWritable, Text, NullWritable> {

		private StringBuilder urlStrings = new StringBuilder();

		/**
		 * www.yodobashi.com/community/product/SKU/xxx.htmlの
		 * xxx.htmlを設定します。
		 * 設定した分のサイトマップURLを作成します。
		 */
		private String[] urlSuffixes = new String[] {
//			"/review.html",
			"/index.html"
		};

//		@Override
//		protected void setup(Context context) throws IOException, InterruptedException {
//		}

		/*
		 *
		 * @see org.apache.hadoop.mapreduce.Reducer#reduce(KEYIN, java.lang.Iterable<VALUEIN>, org.apache.hadoop.mapreduce.Reducer<KEYIN,VALUEIN,KEYOUT,VALUEOUT>.Context)
		 */
		@Override
		public void reduce(Text skuText, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {

			String sku = skuText.toString();

			for (String suffix : urlSuffixes) {
				String url = urlStrings
						.append(sku)
						.append(suffix)
						.toString();
				logger.info(url);
				urlStrings.delete(0, urlStrings.length());
				context.write(new Text(url), NullWritable.get());
			}
		}

		/**
		 * 後処理を行います。
		 */
//		@Override
//		protected void cleanup(Context context) throws IOException, InterruptedException {
//		}

	}

}
