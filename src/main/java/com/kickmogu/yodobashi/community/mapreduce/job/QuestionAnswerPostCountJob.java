package com.kickmogu.yodobashi.community.mapreduce.job;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.reduce.LongSumReducer;
import org.apache.hadoop.util.ToolRunner;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.hadoop.configuration.JobFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.exception.CommonSystemException;
import com.kickmogu.lib.core.utils.ConfigUtil;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.io.HBaseTableInputFormat;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.mapreduce.output.ProductMasterCountRecordWriter;
import com.kickmogu.yodobashi.community.mapreduce.output.ProductMasterCountRecordWriter.ProductMasterCountData;
import com.kickmogu.yodobashi.community.mapreduce.output.SimpleOutputFormat;
import com.kickmogu.yodobashi.community.resource.config.HBaseConfig;
import com.kickmogu.yodobashi.community.resource.dao.AdminConfigDao;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityOperationStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.service.ProductService;
import com.kickmogu.yodobashi.community.service.SystemMaintenanceService;

/**
 * 質問回答の投稿件数を商品マスターに記録するジョブです。
 * @author kamiike
 */
@Service @Lazy @Scope("prototype")
public class QuestionAnswerPostCountJob extends AbstractCommunityJob implements InitializingBean {

	/**
	 * 管理者コンフィグ DAO です。
	 */
	@Autowired
	private AdminConfigDao adminConfigDao;

	/**
	 * HBaseアクセサです。
	 */
	@Autowired @Qualifier("default")
	private HBaseOperations hBaseOperations;

	/**
	 * Solrアクセサです。
	 */
//	@Autowired @Qualifier("default")
	private SolrOperations solrOperations;

	/**
	 * 商品サービスです。
	 */
	@Autowired
	private ProductService productService;

	/**
	 * タイムスタンプホルダーです。
	 */
	@Autowired
	private TimestampHolder timestampHolder;

	/**
	 * バージョン情報です。
	 */
	private Integer version;

	/**
	 * 入力フォーマットインスタンスを返します。<br />
	 * Job名 + InputFormat の Bean 名で定義します。
	 * @return 入力フォーマットインスタンス
	 */
	@Bean @Scope("prototype")
	public HBaseTableInputFormat questionAnswerPostCountInputFormat() {
		HBaseTableInputFormat inputFormat = new HBaseTableInputFormat();
		inputFormat.setConf(extConfiguration);
		return inputFormat;
	}

	/**
	 * 出力フォーマットインスタンスを返します。<br />
	 * Job名 + OutputFormat の Bean 名で定義します。
	 * @return 出力フォーマットインスタンス
	 */
	@Bean @Scope("prototype")
	public SimpleOutputFormat<Text, ProductMasterCountData> questionAnswerPostCountOutputFormat() {
		return new SimpleOutputFormat<Text, ProductMasterCountData>(new ProductMasterCountRecordWriter(
				version, hBaseOperations, solrOperations, timestampHolder,
				"answerPostCount", adminConfigDao.loadScoreFactor()));
	}

	/**
	 * 初期化処理を行います。
	 * @throws Exception 例外が発生した場合
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		this.version = productService.getNextProductMasterVersion().getVersion();
	}
	
	/**
	 * リデューサーを返します。<br />
	 * Job名 + Reducer の Bean 名で定義します。
	 * @return リデューサー
	 */
	@Bean @Scope("prototype")
	public QuestionAnswerPostCountReducer questionAnswerPostCountReducer() {
		return new QuestionAnswerPostCountReducer();
	}

	/**
	 * ジョブインスタンスを返します。<br />
	 * Job名 の Bean 名で定義します。
	 * @return ジョブインスタンス
	 * @throws Exception 例外が発生した場合
	 */
	@Bean @Scope("prototype")
	public Job questionAnswerPostCount() throws Exception {
		JobFactoryBean factory = getJobFactoryBean();
		factory.setInputFormat(questionAnswerPostCountInputFormat());
		factory.setReducer(questionAnswerPostCountReducer());
		factory.setOutputFormat(questionAnswerPostCountOutputFormat());
		Job job = factory.getObject();

		job.setMapperClass(QuestionAnswerPostCountMapper.class);
		job.setCombinerClass(LongSumReducer.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(LongWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(ProductMasterCountData.class);
		return job;
	}

	/**
	 * JOB名を返します。
	 * @return Job名です。
	 */
	@Override
	public String getJobName() {
		return "questionAnswerPostCount";
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
		
		properties.setProperty(TARGET_DATE, args[0]);
		HBaseConfig config = new HBaseConfig();
		ConfigUtil.loadProperties(getProfileName(), "resource-config", config);
		properties.setProperty(TableInputFormat.INPUT_TABLE,
				StringUtils.uncapitalize(config.hbaseTableNamePrefix + QuestionAnswerDO.class.getSimpleName()));
		properties.setProperty(TableInputFormat.SCAN_COLUMNS,
				"cf:productId cf:communityUserId cf:status cf:withdraw cf:postDate");
		properties.setProperty(HBaseTableInputFormat.DELETE_FLG_FAMILY, "cf");
	}

	/**
	 * 質問回答投稿処理を実行します。
	 * @param parentContext 親コンテキスト
	 * @return 実行結果
	 * @throws Exception 例外が発生した場合
	 */
	public static int execute(ApplicationContext parentContext) throws Exception {
		return execute(parentContext, null);
	}

	/**
	 * 質問回答投稿処理を実行します。
	 * @param parentContext 親コンテキスト
	 * @param targetDate 対象日
	 * @return 実行結果
	 * @throws Exception 例外が発生した場合
	 */
	public static int execute(
			ApplicationContext parentContext,
			String targetDate) throws Exception {
		
		// ReadOnlyモードチェック
		SystemMaintenanceService systemMaintenanceService = parentContext.getBean(SystemMaintenanceService.class);
		if (systemMaintenanceService.getCommunityOperationStatus().equals(CommunityOperationStatus.READONLY_OPERATION)) {
			throw new CommonSystemException("Stopped for " + CommunityOperationStatus.READONLY_OPERATION.getLabel());
		}
		
		return ToolRunner.run(new QuestionAnswerPostCountJob(
				).setParentApplicationContext(parentContext),
				new String[]{getTargetDateString(targetDate)});
	}

	/**
	 * 質問回答の投稿件数集計において、マップ処理を行います。
	 * @author kamiike
	 *
	 */
	public static class QuestionAnswerPostCountMapper extends Mapper<ImmutableBytesWritable, Result, Text, LongWritable> {

		/**
		 * キー情報のラッパーです。
		 */
		private static Text keyWrapper = new Text();

		/**
		 * 対象日付です。
		 */
		private Date targetDate = null;

		/**
		 * 初期化処理を行います。
		 * @param context コンテキスト
		 * @throws IOException 入出力例外が発生した場合
		 * @throws InterruptedException 中断例外が発生した場合
		 */
		protected void setup(Context context) throws IOException, InterruptedException {
			SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMATTER);
			try {
				targetDate = formatter.parse(context.getConfiguration().get(TARGET_DATE));
			} catch (ParseException e) {
				throw new IllegalStateException(e);
			}
		}

		/**
		 * マップ処理を行います。
		 * @param key キー
		 * @param value 質問回答情報
		 * @param context コンテキスト
		 * @throws IOException 入出力例外が発生した場合
		 * @throws InterruptedException 中断例外が発生した場合
		 */
		@Override
		public void map(
				ImmutableBytesWritable key,
				Result value,
				Context context)
				throws IOException,
				InterruptedException {
			String sku = null;
			String communityUserId = null;
			for (KeyValue keyValue : value.raw()) {
				String columnName = Bytes.toString(
						keyValue.getBuffer(),
						keyValue.getQualifierOffset(),
						keyValue.getQualifierLength());
				if (columnName.equals("productId")) {
					sku = getStringValue(keyValue);
				} else if (columnName.equals("communityUserId")) {
					communityUserId = getStringValue(keyValue);
				} else if (columnName.equals("status")) {
					if (!ContentsStatus.codeOf(getStringValue(keyValue)
							).equals(ContentsStatus.SUBMITTED)) {
						return;
					}
				} else if (columnName.equals("withdraw")) {
					if (getBooleanValue(keyValue)) {
						return;
					}
				} else if (columnName.equals("postDate")) {
					if (keyValue.getValueLength() == 0) {
						return;
					}
					if (targetDate.getTime() <= getLongValue(keyValue)) {
						return;
					}
				}
			}
			keyWrapper.set(sku + "\t" + communityUserId);
			context.write(keyWrapper, new LongWritable(1));
		}
	}

	/**
	 * 質問回答の投稿件数集計において、リデュース処理を行います。
	 * @author kamiike
	 */
	public static class QuestionAnswerPostCountReducer extends Reducer<Text, LongWritable, Text, ProductMasterCountData> {

		/**
		 * リデュース処理を行います。
		 * @param key キー
		 * @param values 値リスト
		 * @param context コンテキスト
		 * @throws IOException 入出力例外が発生した場合
		 * @throws InterruptedException 中断例外が発生した場合
		 */
		@Override
		public void reduce(
				Text key,
				Iterable<LongWritable> values,
                Context context) throws IOException, InterruptedException {
			long sum = 0;
			for (LongWritable val : values) {
				sum += val.get();
			}
			ProductMasterCountData count = new ProductMasterCountData();
			count.setAnswerPostCount(sum);
			context.write(key, count);
		}
	}
}
