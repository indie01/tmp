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
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;
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
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityOperationStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;
import com.kickmogu.yodobashi.community.service.ProductService;
import com.kickmogu.yodobashi.community.service.SystemMaintenanceService;

/**
 * 画像の投稿件数を商品マスターに記録するジョブです。
 * @author kamiike
 */
@Service @Lazy
public class ImagePostCountJob extends AbstractCommunityJob implements InitializingBean {

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
	public HBaseTableInputFormat imagePostCountInputFormat() {
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
	public SimpleOutputFormat<Text, ProductMasterCountData> imagePostCountOutputFormat() {
		return new SimpleOutputFormat<Text, ProductMasterCountData>(new ProductMasterCountRecordWriter(
				version, hBaseOperations, solrOperations, timestampHolder,
				"imagePostCount", adminConfigDao.loadScoreFactor()));
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
	public ImagePostCountReducer imagePostCountReducer() {
		return new ImagePostCountReducer();
	}

	/**
	 * ジョブインスタンスを返します。<br />
	 * Job名 の Bean 名で定義します。
	 * @return ジョブインスタンス
	 * @throws Exception 例外が発生した場合
	 */
	@Bean @Scope("prototype")
	public Job imagePostCount() throws Exception {
		JobFactoryBean factory = getJobFactoryBean();
		factory.setInputFormat(imagePostCountInputFormat());
		factory.setReducer(imagePostCountReducer());
		factory.setOutputFormat(imagePostCountOutputFormat());
		Job job = factory.getObject();

		job.setMapperClass(ImagePostCountMapper.class);
		job.setCombinerClass(LongSumReducer.class);
		job.setPartitionerClass(ImagePostCountPartitioner.class);

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
		return "imagePostCount";
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
				StringUtils.uncapitalize(config.hbaseTableNamePrefix + ImageHeaderDO.class.getSimpleName()));
		properties.setProperty(TableInputFormat.SCAN_COLUMNS,
				"cf:productId cf:ownerCommunityUserId cf:thumbnail cf:status cf:withdraw cf:postContentType " +
				"cf:imageSetId cf:postDate");
		properties.setProperty(HBaseTableInputFormat.DELETE_FLG_FAMILY, "cf");
	}

	/**
	 * 画像投稿処理を実行します。
	 * @param parentContext 親コンテキスト
	 * @return 実行結果
	 * @throws Exception 例外が発生した場合
	 */
	public static int execute(ApplicationContext parentContext) throws Exception {
		return execute(parentContext, null);
	}

	/**
	 * 画像投稿処理を実行します。
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
		
		return ToolRunner.run(new ImagePostCountJob(
				).setParentApplicationContext(parentContext),
				new String[]{getTargetDateString(targetDate)});
	}

	/**
	 * 画像の投稿件数集計において、マップ処理を行います。
	 * @author kamiike
	 *
	 */
	public static class ImagePostCountMapper extends Mapper<ImmutableBytesWritable, Result, Text, LongWritable> {

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
		 * @param value 画像情報
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
			String imageSetId = null;
			for (KeyValue keyValue : value.raw()) {
				String columnName = Bytes.toString(
						keyValue.getBuffer(),
						keyValue.getQualifierOffset(),
						keyValue.getQualifierLength());
				if (columnName.equals("productId")) {
					sku = getStringValue(keyValue);
				} else if (columnName.equals("ownerCommunityUserId")) {
					communityUserId = getStringValue(keyValue);
				} else if (columnName.equals("status")) {
					if (!ContentsStatus.codeOf(getStringValue(keyValue)
							).equals(ContentsStatus.SUBMITTED)) {
						return;
					}
				} else if (columnName.equals("thumbnail")) {
					if (getBooleanValue(keyValue)) {
						return;
					}
				} else if (columnName.equals("withdraw")) {
					if (getBooleanValue(keyValue)) {
						return;
					}
				} else if (columnName.equals("postContentType")) {
					if (!PostContentType.codeOf(getStringValue(keyValue)).equals(
							PostContentType.IMAGE_SET)) {
						return;
					}
				} else if (columnName.equals("imageSetId")) {
					imageSetId = getStringValue(keyValue);;
				} else if (columnName.equals("postDate")) {
					if (keyValue.getValueLength() == 0) {
						return;
					}
					if (targetDate.getTime() <= getLongValue(keyValue)) {
						return;
					}
				}
			}
			keyWrapper.set(sku + "\t" + communityUserId + "\t" + imageSetId);
			context.write(keyWrapper, new LongWritable(1));
		}
	}

	/**
	 * 画像の投稿件数集計において、リデュース処理を行います。<br />
	 * 「order inversion」パターンを用いて実装しています。
	 * @author kamiike
	 */
	public static class ImagePostCountReducer extends Reducer<Text, LongWritable, Text, ProductMasterCountData> {

		/**
		 * キー情報のラッパーです。
		 */
		private static Text keyWrapper = new Text();

		/**
		 * 前回処理したグループキーです。
		 */
		private String preGroupKey = null;

		/**
		 * カウントです。
		 */
		private long count;

		/**
		 * 初期化処理を行います。
		 * @param context コンテキスト
		 * @throws IOException 入出力例外が発生した場合
		 * @throws InterruptedException 中断例外が発生した場合
		 */
		protected void setup(Context context) throws IOException, InterruptedException {
			preGroupKey = null;
			count = 0;
		}

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
			String[] keyParts = key.toString().split("\t");
			String sku = keyParts[0];
			String communityUserId = keyParts[1];

			String groupKey = sku + "\t" + communityUserId;

			if (preGroupKey == null) {
				preGroupKey = groupKey;
				count++;
			} else {
				//グループキーが変更された場合、保持していたカウントを出力して、初期化します。
				if (!preGroupKey.equals(groupKey)) {
					keyWrapper.set(preGroupKey);
					ProductMasterCountData countData = new ProductMasterCountData();
					countData.setImagePostCount(count);
					context.write(keyWrapper, countData);
					count = 0;
				}
				preGroupKey = groupKey;
				count++;
			}
		}

		/**
		 * 最後のキーに対して処理を行います。
		 * @param context コンテキスト
		 * @throws IOException 入出力例外が発生した場合
		 * @throws InterruptedException 中断例外が発生した場合
		 */
		protected void cleanup(Context context) throws IOException, InterruptedException {
			if (preGroupKey != null) {
				keyWrapper.set(preGroupKey);
				ProductMasterCountData countData = new ProductMasterCountData();
				countData.setImagePostCount(count);
				context.write(keyWrapper, countData);
			}
			preGroupKey = null;
			count = 0;
		}
	}

	/**
	 * 画像の投稿件数集計処理において、パーティション処理を行います。<br />
	 * @author kamiike
	 *
	 */
	public static class ImagePostCountPartitioner extends HashPartitioner<Text, LongWritable> {

		/**
		 * キー情報のラッパーです。
		 */
		private static Text keyWrapper = new Text();

		/**
		 * パーティションを親グループキーで判別します。
		 * @param key キー
		 * @param values 値リスト
		 * @param numReduceTasks リデュースタスク数
		 */
		public int getPartition(
				Text key,
				LongWritable value,
                int numReduceTasks) {
			String[] keyParts = key.toString().split("\t");
			String sku = keyParts[0];
			String communityUserId = keyParts[1];
			keyWrapper.set(sku + "\t" + communityUserId);
			return super.getPartition(keyWrapper, value, numReduceTasks);
		}

	}
}
