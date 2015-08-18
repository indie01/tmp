package com.kickmogu.yodobashi.community.mapreduce.job;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.lang.time.DateUtils;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.hadoop.configuration.JobFactoryBean;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.exception.CommonSystemException;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.mapreduce.input.SolrInputFormat;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityOperationStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;
import com.kickmogu.yodobashi.community.service.BatchMailService;
import com.kickmogu.yodobashi.community.service.SystemMaintenanceService;

/**
 * 日次メール送信を行うジョブです。
 * @author kamiike
 */
@Service @Lazy @Scope("prototype")
public class DailyMailJob extends AbstractCommunityJob {

	/**
	 * Solrアクセサです。
	 */
	@Autowired  @Qualifier("default")
	private SolrOperations solrOperations;

	/**
	 * バッチメールサービスです。
	 */
	@Autowired
	private BatchMailService batchMailService;

	/**
	 * マップを返します。<br />
	 * Job名 + Mapper の Bean 名で定義します。
	 * @return マップ
	 */
	@Bean @Scope("prototype")
	public DailyMailMapper dailyMailMapper() {
		DailyMailMapper mapper = new DailyMailMapper();
		mapper.setBatchMailService(batchMailService);
		return mapper;
	}

	/**
	 * 入力フォーマットインスタンスを返します。<br />
	 * Job名 + InputFormat の Bean 名で定義します。
	 * @return 入力フォーマットインスタンス
	 */
	@Bean @Scope("prototype")
	public SolrInputFormat<String, CommunityUserDO> dailyMailInputFormat() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("status_s:");
		buffer.append(CommunityUserStatus.ACTIVE.getCode());
		buffer.append(" OR status_s:");
		buffer.append(CommunityUserStatus.STOP.getCode());
		return new SolrInputFormat<String, CommunityUserDO>(
				solrOperations,
				buffer.toString(),
				Path.DEFAULT, CommunityUserDO.class, "registerDateTime_dt");
	}

	/**
	 * ジョブインスタンスを返します。<br />
	 * Job名 の Bean 名で定義します。
	 * @return ジョブインスタンス
	 * @throws Exception 例外が発生した場合
	 */
	@Bean @Scope("prototype")
	public Job dailyMail() throws Exception {
		JobFactoryBean factory = getJobFactoryBean();

		factory.setInputFormat(dailyMailInputFormat());
		factory.setMapper(dailyMailMapper());
		Job job = factory.getObject();

		job.setOutputFormatClass(NullOutputFormat.class);

		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(NullWritable.class);

		return job;
	}

	/**
	 * JOB名を返します。
	 * @return Job名です。
	 */
	@Override
	public String getJobName() {
		return "dailyMail";
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

		String targetDate = formatter.format(
				DateUtils.addDays(formatter.parse(args[0]), -1));
		properties.setProperty(TARGET_DATE, targetDate);
	}

	/**
	 * 日次メール送信処理を実行します。
	 * @param parentContext 親コンテキスト
	 * @return 実行結果
	 * @throws Exception 例外が発生した場合
	 */
	public static int execute(ApplicationContext parentContext) throws Exception {
		return execute(parentContext, null);
	}

	/**
	 * 日次メール送信処理を実行します。
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
		
		return ToolRunner.run(new DailyMailJob(
				).setParentApplicationContext(parentContext),
				new String[]{getTargetDateString(targetDate)});
	}

	/**
	 * 日次メール送信において、マップ処理を行います。
	 * @author kamiike
	 *
	 */
	public static class DailyMailMapper extends Mapper<String, CommunityUserDO, NullWritable, NullWritable> {

		/**
		 * バッチメールサービスです。
		 */
		private BatchMailService batchMailService;

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
				String key,
				CommunityUserDO value,
				Context context)
				throws IOException,
				InterruptedException {
			batchMailService.sendMail(value, targetDate);
		}

		/**
		 * バッチメールサービスを設定します。
		 * @param batchMailService バッチメールサービス
		 */
		public void setBatchMailService(BatchMailService batchMailService) {
			this.batchMailService = batchMailService;
		}
	}
}
