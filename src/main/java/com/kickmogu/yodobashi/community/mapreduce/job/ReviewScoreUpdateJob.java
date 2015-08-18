package com.kickmogu.yodobashi.community.mapreduce.job;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

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
import com.kickmogu.yodobashi.community.resource.dao.AdminConfigDao;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.ScoreFactorDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityOperationStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.service.ReviewService;
import com.kickmogu.yodobashi.community.service.SystemMaintenanceService;

/**
 * レビューのスコア更新を行うジョブです。
 * @author kamiike
 */
@Service @Lazy @Scope("prototype")
public class ReviewScoreUpdateJob extends AbstractCommunityJob {

	/**
	 * 管理者コンフィグ DAO です。
	 */
	@Autowired
	private AdminConfigDao adminConfigDao;

	/**
	 * Solrアクセサです。
	 */
	@Autowired @Qualifier("default-RefMaster")
	private SolrOperations solrOperations;

	/**
	 * レビューサービスです。
	 */
	@Autowired
	private ReviewService reviewService;

	/**
	 * 入力フォーマットインスタンスを返します。<br />
	 * Job名 + InputFormat の Bean 名で定義します。
	 * @return 入力フォーマットインスタンス
	 * @throws ParseException 例外が発生した場合
	 */
	@Bean @Scope("prototype")
	public SolrInputFormat<String, ReviewDO> reviewScoreUpdateInputFormat() throws ParseException {
		StringBuilder buffer = new StringBuilder();
		buffer.append("status_s:");
		buffer.append(ContentsStatus.SUBMITTED.getCode());
		return new SolrInputFormat<String, ReviewDO>(
				solrOperations,
				buffer.toString(),
				Path.DEFAULT, ReviewDO.class, "registerDateTime_dt");
	}

	/**
	 * マップインスタンスを返します。<br />
	 * Job名 + Mapper の Bean 名で定義します。
	 * @return マップインスタンス
	 */
	@Bean @Scope("prototype")
	public ReviewScoreUpdateMapper reviewScoreUpdateMapper() {
		ReviewScoreUpdateMapper mapper = new ReviewScoreUpdateMapper();
		mapper.setReviewService(reviewService);
		mapper.setScoreFactor(adminConfigDao.loadScoreFactor());
		return mapper;
	}

	/**
	 * ジョブインスタンスを返します。<br />
	 * Job名 の Bean 名で定義します。
	 * @return ジョブインスタンス
	 * @throws Exception 例外が発生した場合
	 */
	@Bean @Scope("prototype")
	public Job reviewScoreUpdate() throws Exception {
		JobFactoryBean factory = getJobFactoryBean();

		factory.setInputFormat(reviewScoreUpdateInputFormat());
		factory.setMapper(reviewScoreUpdateMapper());
		Job job = factory.getObject();

		job.setOutputFormatClass(NullOutputFormat.class);

		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(NullWritable.class);

		job.setNumReduceTasks(0);

		return job;
	}

	/**
	 * JOB名を返します。
	 * @return Job名です。
	 */
	@Override
	public String getJobName() {
		return "reviewScoreUpdate";
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
	}

	/**
	 * レビューのスコア更新処理を実行します。
	 * @param parentContext 親コンテキスト
	 * @return 実行結果
	 * @throws Exception 例外が発生した場合
	 */
	public static int execute(ApplicationContext parentContext) throws Exception {
		return execute(parentContext, null);
	}

	/**
	 * レビューのスコア更新処理を実行します。
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
		
		return ToolRunner.run(new ReviewScoreUpdateJob(
				).setParentApplicationContext(parentContext),
				new String[]{getTargetDateString(targetDate)});
	}

	/**
	 * レビューのスコア更新において、マップ処理を行います。
	 * @author kamiike
	 *
	 */
	public static class ReviewScoreUpdateMapper extends Mapper<String, ReviewDO, NullWritable, NullWritable> {
		
		private static int BULK_SIZE = 100;

		/**
		 * レビューサービスです。
		 */
		private ReviewService reviewService;

		/**
		 * スコア係数です。
		 */
		private ScoreFactorDO scoreFactor;

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
		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
			SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMATTER);
			try {
				targetDate = formatter.parse(context.getConfiguration().get(TARGET_DATE));
				reviewService.updateReviewScoreAndViewCountForBatchBegin(BULK_SIZE);
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
				ReviewDO value,
				Context context)
				throws IOException,
				InterruptedException {
			reviewService.updateReviewScoreAndViewCountForBatch(targetDate, value, scoreFactor);
		}
		
		/**
		 * 後処理を行います。
		 */
		@Override
		protected void cleanup(Context context) throws IOException, InterruptedException {
			reviewService.updateReviewScoreAndViewCountForBatchEnd();
		}

		/**
		 * レビューサービスを設定します。
		 * @param reviewService レビューサービス
		 */
		public void setReviewService(ReviewService reviewService) {
			this.reviewService = reviewService;
		}

		/**
		 * スコア係数を設定します。
		 * @param scoreFactor スコア係数
		 */
		public void setScoreFactor(ScoreFactorDO scoreFactor) {
			this.scoreFactor = scoreFactor;
		}
	}
}
