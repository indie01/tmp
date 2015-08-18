package com.kickmogu.yodobashi.community.mapreduce.job;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;
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

import com.kickmogu.lib.core.exception.CommonSystemException;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.mapreduce.output.UniqueUserViewCountOutputFormat;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityOperationStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.LogGroup;
import com.kickmogu.yodobashi.community.service.SystemMaintenanceService;

/**
 * ユニークユーザー閲覧数を記録するジョブです。
 * @author kamiike
 */
@Service @Lazy @Scope("prototype")
public class UniqueUserViewLogCountJob extends AbstractCommunityJob {

	/**
	 * HBaseアクセサです。
	 */
	@Autowired @Qualifier("default")
	private HBaseOperations hBaseOperations;

	/**
	 * Solrアクセサです。
	 */
	@Autowired @Qualifier("default")
	private SolrOperations solrOperations;

	/**
	 * 出力フォーマットインスタンスを返します。<br />
	 * Job名 + OutputFormat の Bean 名で定義します。
	 * @return 出力フォーマットインスタンス
	 */
	@Bean @Scope("prototype")
	public UniqueUserViewCountOutputFormat uniqueUserViewLogCountOutputFormat() {
		return new UniqueUserViewCountOutputFormat(
				hBaseOperations, solrOperations, DATE_FORMATTER);
	}
	
	/**
	 * リデューサーを返します。<br />
	 * Job名 + Reducer の Bean 名で定義します。
	 * @return リデューサー
	 */
	@Bean @Scope("prototype")
	public UniqueUserViewLogCountReducer uniqueUserViewLogCountReducer() {
		return new UniqueUserViewLogCountReducer();
	}

	/**
	 * ジョブインスタンスを返します。<br />
	 * Job名 の Bean 名で定義します。
	 * @return ジョブインスタンス
	 * @throws Exception 例外が発生した場合
	 */
	@Bean @Scope("prototype")
	public Job uniqueUserViewLogCount() throws Exception {
		JobFactoryBean factory = getJobFactoryBean();
		factory.setReducer(uniqueUserViewLogCountReducer());
		factory.setOutputFormat(uniqueUserViewLogCountOutputFormat());

		Job job = factory.getObject();
		job.setMapperClass(UniqueUserViewLogCountMapper.class);
		job.setCombinerClass(LongSumReducer.class);
		job.setPartitionerClass(UniqueUserViewLogCountPartitioner.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(LongWritable.class);

		return job;
	}

	/**
	 * JOB名を返します。
	 * @return Job名です。
	 */
	@Override
	public String getJobName() {
		return "uniqueUserViewLogCount";
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
		
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMATTER);
		Calendar time = Calendar.getInstance();
		time.setTime(formatter.parse(args[0]));
		time.add(Calendar.DATE, -1);

		String targetDate = formatter.format(time.getTime());

		properties.setProperty(UniqueUserViewCountOutputFormat.KEY_TIME_STRING,
				targetDate);

		properties.setProperty(JobTemplate.SPRING_INPUT_PATHS,
				(getMapRreduceConfig(CommunityMapReduceConfig.class).uniqueUserViewLogCountJobInput + "/" + targetDate).replace("//", "/"));
	}

	/**
	 * ユニークユーザー閲覧数のログ集計処理を実行します。
	 * @param parentContext 親コンテキスト
	 * @return 実行結果
	 * @throws Exception 例外が発生した場合
	 */
	public static int execute(ApplicationContext parentContext) throws Exception {
		return execute(parentContext, null);
	}

	/**
	 * ユニークユーザー閲覧数のログ集計処理を実行します。
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
		
		return ToolRunner.run(new UniqueUserViewLogCountJob(
				).setParentApplicationContext(parentContext),
				new String[]{getTargetDateString(targetDate)});
	}

	/**
	 * ユニークユーザー閲覧数集計において、マップ処理を行います。
	 * @author kamiike
	 *
	 */
	public static class UniqueUserViewLogCountMapper extends Mapper<LongWritable, Text, Text, LongWritable> {

		/**
		 * キー情報のラッパーです。
		 */
		private static Text keyWrapper = new Text();

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
				LongWritable key,
				Text value,
				Context context)
				throws IOException,
				InterruptedException {
			String line = value.toString();
			if (line.length() == 0) {
				return;
			}
			String[] dataList = line.split("\t");
			if (dataList.length < 2) {
				throw new IllegalArgumentException("LogGroup is not found. line = " + line);
			}
			LogGroup logGroup = LogGroup.codeOf(dataList[1]);
			if (logGroup == null || (!LogGroup.REVIEW.equals(logGroup)
					&& !LogGroup.QUESTION.equals(logGroup)
					&& !LogGroup.IMAGE.equals(logGroup))) {
				return;
			}
			// アクセスログ項目数拡張対応
			if (dataList.length < 8) {
				throw new IllegalArgumentException("Data is invalid. line = " + line);
			}
			String type = dataList[2];
			String accessUserType = dataList[3];
			String userId = dataList[4];
			String contentsId = dataList[5];
			String sku = dataList[6];
			String contentsOwnerId = dataList[7];

			keyWrapper.set(type + "\t" + contentsId + "\t" + sku + "\t" + contentsOwnerId + "\t" + accessUserType + "\t" + userId);
			context.write(keyWrapper, new LongWritable(1));
		}
	}

	/**
	 * ユニークユーザー閲覧数集計において、リデュース処理を行います。<br />
	 * 「order inversion」パターンを用いて実装しています。
	 * @author kamiike
	 */
	public static class UniqueUserViewLogCountReducer extends Reducer<Text, LongWritable, Text, LongWritable> {

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
			String type = keyParts[0];
			String contentsId = keyParts[1];
			String sku = keyParts[2];
			String communityUserId = keyParts[3];
			String groupKey = type + "\t" + contentsId + "\t" + sku + "\t" + communityUserId;

			if (preGroupKey == null) {
				preGroupKey = groupKey;
				count++;
			} else {
				//グループキーが変更された場合、保持していたカウントを出力して、初期化します。
				if (!preGroupKey.equals(groupKey)) {
					keyWrapper.set(preGroupKey);
					context.write(keyWrapper, new LongWritable(count));
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
				context.write(keyWrapper, new LongWritable(count));
			}
			preGroupKey = null;
			count = 0;
		}
	}

	/**
	 * ユニークユーザー閲覧数集計において、パーティション処理を行います。<br />
	 * @author kamiike
	 *
	 */
	public static class UniqueUserViewLogCountPartitioner extends HashPartitioner<Text, LongWritable> {

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
			String type = keyParts[0];
			String contentsId = keyParts[1];
			String sku = keyParts[2];
			String communityUserId = keyParts[3];
			String groupKey = type + "\t" + contentsId + "\t" + sku + "\t" + communityUserId;
			keyWrapper.set(groupKey);
			return super.getPartition(keyWrapper, value, numReduceTasks);
		}

	}
}
