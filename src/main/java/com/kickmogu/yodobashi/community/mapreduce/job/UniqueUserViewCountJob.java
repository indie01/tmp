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
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.UniqueUserViewCountDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityOperationStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.UniqueUserViewCountType;
import com.kickmogu.yodobashi.community.service.ProductService;
import com.kickmogu.yodobashi.community.service.SystemMaintenanceService;

/**
 * コンテンツの閲覧件数を商品マスターに記録するジョブです。
 * @author kamiike
 */
@Service @Lazy @Scope("prototype")
public class UniqueUserViewCountJob extends AbstractCommunityJob implements InitializingBean {

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
	public HBaseTableInputFormat uniqueUserViewCountInputFormat() {
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
	public SimpleOutputFormat<Text, ProductMasterCountData> uniqueUserViewCountOutputFormat() {
		return new SimpleOutputFormat<Text, ProductMasterCountData>(new ProductMasterCountRecordWriter(
				version, hBaseOperations, solrOperations, timestampHolder,
				"reviewShowCount", adminConfigDao.loadScoreFactor()));
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
	 * ジョブインスタンスを返します。<br />
	 * Job名 の Bean 名で定義します。
	 * @return ジョブインスタンス
	 * @throws Exception 例外が発生した場合
	 */
	@Bean @Scope("prototype")
	public Job uniqueUserViewCount() throws Exception {
		JobFactoryBean factory = getJobFactoryBean();
		factory.setInputFormat(uniqueUserViewCountInputFormat());
		factory.setMapper(uniqueUserViewCountMapper());
		factory.setReducer(uniqueUserViewCountReducer());
		factory.setOutputFormat(uniqueUserViewCountOutputFormat());
		Job job = factory.getObject();

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
		return "uniqueUserViewCount";
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
				StringUtils.uncapitalize(config.hbaseTableNamePrefix + UniqueUserViewCountDO.class.getSimpleName()));
		properties.setProperty(TableInputFormat.SCAN_COLUMNS,
				"cf:type cf:sku cf:communityUserId cf:viewCount cf:targetTime cf:contentsId");
		properties.setProperty(HBaseTableInputFormat.DELETE_FLG_FAMILY, "cf");
	}

	/**
	 * コンテンツの閲覧件数集計処理を実行します。
	 * @param parentContext 親コンテキスト
	 * @return 実行結果
	 * @throws Exception 例外が発生した場合
	 */
	public static int execute(ApplicationContext parentContext) throws Exception {
		return execute(parentContext, null);
	}

	/**
	 * コンテンツの閲覧件数集計処理を実行します。
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
		return ToolRunner.run(new UniqueUserViewCountJob(
				).setParentApplicationContext(parentContext),
				new String[]{getTargetDateString(targetDate)});
	}

	
	/**
	 * マップを返します。<br />
	 * Job名 + Mapper の Bean 名で定義します。
	 * @return マップ
	 */
	@Bean @Scope("prototype")
	public UniqueUserViewCountMapper uniqueUserViewCountMapper() {
		UniqueUserViewCountMapper mapper = new UniqueUserViewCountMapper();
		mapper.sethBaseOperations(hBaseOperations);
		return mapper;
	}
	
	/**
	 * リデューサーを返します。<br />
	 * Job名 + Reducer の Bean 名で定義します。
	 * @return リデューサー
	 */
	@Bean @Scope("prototype")
	public UniqueUserViewCountReducer uniqueUserViewCountReducer() {
		return new UniqueUserViewCountReducer();
	}

	/**
	 * コンテンツの閲覧件数集計において、マップ処理を行います。
	 * @author kamiike
	 *
	 */
	public static class UniqueUserViewCountMapper extends Mapper<ImmutableBytesWritable, Result, Text, LongWritable> {

		/**
		 * キー情報のラッパーです。
		 */
		private static Text keyWrapper = new Text();

		private HBaseOperations hBaseOperations;
		
		/**
		 * @param hBaseOperations the hBaseOperations to set
		 */
		public void sethBaseOperations(HBaseOperations hBaseOperations) {
			this.hBaseOperations = hBaseOperations;
		}

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
		 * @param value レビュー情報
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
			long viewCount = 0;
			
			System.out.println("Map Start");
			
			for (KeyValue keyValue : value.raw()) {
				System.out.println(keyValue);
				String columnName = Bytes.toString(
						keyValue.getBuffer(),
						keyValue.getQualifierOffset(),
						keyValue.getQualifierLength());
				if (columnName.equals("sku")) {
					sku = getStringValue(keyValue);
				} else if (columnName.equals("communityUserId")) {
					communityUserId = getStringValue(keyValue);
				} else if (columnName.equals("type")) {
					if (!UniqueUserViewCountType.codeOf(
							getStringValue(keyValue)).equals(UniqueUserViewCountType.REVIEW)) {
						return;
					}
				} else if (columnName.equals("viewCount")) {
					viewCount = getLongValue(keyValue);
				} else if (columnName.equals("targetTime")) {
					if (targetDate.getTime() <= getLongValue(keyValue)) {
						return;
					}
				} else if(columnName.equals("contentsId")) {
					ReviewDO review = hBaseOperations.load(ReviewDO.class, getStringValue(keyValue));
					if(review == null || !review.getStatus().equals(ContentsStatus.SUBMITTED)) {
						
						if(review == null){
							System.out.println("review is null:" +getStringValue(keyValue) );
						}else{
							System.out.println("review.getStatus() is :" + review.getStatus() + "   " +getStringValue(keyValue) );
						}
							
						
						return;
					}
						
				}
			}
			keyWrapper.set(sku + "\t" + communityUserId);
			context.write(keyWrapper, new LongWritable(viewCount));
		}
	}

	/**
	 * コンテンツの閲覧件数集計において、リデュース処理を行います。
	 * @author kamiike
	 */
	public static class UniqueUserViewCountReducer extends Reducer<Text, LongWritable, Text, ProductMasterCountData> {

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
			count.setReviewShowCount(sum);
			context.write(key, count);
		}
	}
}
