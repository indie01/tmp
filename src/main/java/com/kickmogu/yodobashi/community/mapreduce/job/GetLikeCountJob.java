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
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
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
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityOperationStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.LikeTargetType;
import com.kickmogu.yodobashi.community.service.ProductService;
import com.kickmogu.yodobashi.community.service.SystemMaintenanceService;

/**
 * いいね獲得件数を商品マスターに記録するジョブです。
 * @author kamiike
 */
@Service @Lazy @Scope("prototype")
public class GetLikeCountJob extends AbstractCommunityJob implements InitializingBean {

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
	public HBaseTableInputFormat getLikeCountInputFormat() {
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
	public SimpleOutputFormat<Text, ProductMasterCountData> getLikeCountOutputFormat() {
		return new SimpleOutputFormat<Text, ProductMasterCountData>(new ProductMasterCountRecordWriter(
				version, hBaseOperations, solrOperations, timestampHolder,
				"reviewLikeCount,answerLikeCount,imageLikeCount", adminConfigDao.loadScoreFactor()));
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
	public Job getLikeCount() throws Exception {
		JobFactoryBean factory = getJobFactoryBean();
		factory.setInputFormat(getLikeCountInputFormat());
		factory.setMapper(getLikeCountMapper());
		factory.setReducer(getLikeCountReducer());
		factory.setOutputFormat(getLikeCountOutputFormat());
		Job job = factory.getObject();
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
		return "getLikeCount";
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
				StringUtils.uncapitalize(config.hbaseTableNamePrefix + LikeDO.class.getSimpleName()));
		properties.setProperty(TableInputFormat.SCAN_COLUMNS,
				"cf:sku cf:relationReviewOwnerId cf:relationQuestionAnswerOwnerId " +
				"cf:relationImageOwnerId cf:targetType cf:withdraw cf:postDate");
		properties.setProperty(HBaseTableInputFormat.DELETE_FLG_FAMILY, "cf");
	}

	/**
	 * いいね獲得件数登録処理を実行します。
	 * @param parentContext 親コンテキスト
	 * @return 実行結果
	 * @throws Exception 例外が発生した場合
	 */
	public static int execute(ApplicationContext parentContext) throws Exception {
		return execute(parentContext, null);
	}

	/**
	 * いいね獲得件数登録処理を実行します。
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
		
		return ToolRunner.run(new GetLikeCountJob(
				).setParentApplicationContext(parentContext),
				new String[]{getTargetDateString(targetDate)});
	}

	
	/**
	 * マップを返します。<br />
	 * Job名 + Mapper の Bean 名で定義します。
	 * @return マップ
	 */
	@Bean @Scope("prototype")
	public GetLikeCountMapper getLikeCountMapper() {
		GetLikeCountMapper mapper = new GetLikeCountMapper();
		mapper.sethBaseOperations(hBaseOperations);
		return mapper;
	}

	/**
	 * リデューサーを返します。<br />
	 * Job名 + Reducer の Bean 名で定義します。
	 * @return リデューサー
	 */
	@Bean @Scope("prototype")
	public GetLikeCountReducer getLikeCountReducer() {
		return new GetLikeCountReducer();
	}
	
	/**
	 * いいね獲得件数の集計において、マップ処理を行います。
	 * @author kamiike
	 *
	 */
	public static class GetLikeCountMapper extends Mapper<ImmutableBytesWritable, Result, Text, ProductMasterCountData> {

		/**
		 * HBaseアクセサです。
		 */
		private HBaseOperations hBaseOperations;
		
		/**
		 * @param hBaseOperations the hBaseOperations to set
		 */
		public void sethBaseOperations(HBaseOperations hBaseOperations) {
			this.hBaseOperations = hBaseOperations;
		}

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
			String relationReviewOwnerId = null;
			String relationQuestionAnswerOwnerId = null;
			String relationImageOwnerId = null;
			String likeId = Bytes.toString(value.getRow());
			LikeTargetType targetType = null;
			for (KeyValue keyValue : value.raw()) {
				String columnName = Bytes.toString(
						keyValue.getBuffer(),
						keyValue.getQualifierOffset(),
						keyValue.getQualifierLength());
				if (columnName.equals("sku")) {
					sku = getStringValue(keyValue);
				} else if (columnName.equals("relationReviewOwnerId")) {
					relationReviewOwnerId = getStringValue(keyValue);
				} else if (columnName.equals("relationQuestionAnswerOwnerId")) {
					relationQuestionAnswerOwnerId = getStringValue(keyValue);
				} else if (columnName.equals("relationImageOwnerId")) {
					relationImageOwnerId = getStringValue(keyValue);
				} else if (columnName.equals("targetType")) {
					targetType = LikeTargetType.codeOf(getStringValue(keyValue));
				} else if (columnName.equals("withdraw")) {
					if (getBooleanValue(keyValue)) {
						return;
					}
				} else if (columnName.equals("postDate")) {
					if (targetDate.getTime() <= getLongValue(keyValue)) {
						return;
					}
				}
			}
			
			if(org.apache.commons.lang.StringUtils.isEmpty(likeId)) {
				System.out.println("LikeId is Notfound");
				return;
			}
			
			System.out.println("LikeId is found");
			
			String[] sptLikeId = likeId.split(IdUtil.ID_SEPARATOR);
			
			if(sptLikeId == null || sptLikeId.length != 3) {
				
				System.out.println("LikeId length is not 3");
				return;
			}
			if(!org.apache.commons.lang.StringUtils.isEmpty(relationReviewOwnerId)) {
				ReviewDO review =  hBaseOperations.load(ReviewDO.class, sptLikeId[1]);
				if(review == null || !review.getStatus().equals(ContentsStatus.SUBMITTED)) {
					if(review == null){
						System.out.println("Review is null:" + sptLikeId[1]);
					}else {
						System.out.println("Review Status:" + review.getStatus() + "  "  + sptLikeId[1]);
					}
					
					return;
				}
			} else if(!org.apache.commons.lang.StringUtils.isEmpty(relationQuestionAnswerOwnerId)) {
				QuestionAnswerDO answer =  hBaseOperations.load(QuestionAnswerDO.class, sptLikeId[1]);
				if(answer == null || !answer.getStatus().equals(ContentsStatus.SUBMITTED)) {
					if(answer == null){
						System.out.println("answer is null:" + sptLikeId[1]);
					}else {
						System.out.println("answer Status:" + answer.getStatus() + "  "  + sptLikeId[1]);
					}
					return;
				}
			} else if(!org.apache.commons.lang.StringUtils.isEmpty(relationImageOwnerId)) {
				ImageHeaderDO image =  hBaseOperations.load(ImageHeaderDO.class, sptLikeId[1]);
				if(image == null || !image.getStatus().equals(ContentsStatus.SUBMITTED)) {
					if(image == null){
						System.out.println("image is null:" + sptLikeId[1]);
					}else {
						System.out.println("image Status:" + image.getStatus() + "  "  + sptLikeId[1]);
					}
					return;
				}
			} else {
				return;
			}
			
			ProductMasterCountData count = new ProductMasterCountData();
			if (targetType.equals(LikeTargetType.REVIEW)) {
				count.setReviewLikeCount(1);
				keyWrapper.set(sku + "\t" + relationReviewOwnerId);
			} else if (targetType.equals(LikeTargetType.QUESTION_ANSWER)) {
				count.setAnswerLikeCount(1);
				keyWrapper.set(sku + "\t" + relationQuestionAnswerOwnerId);
			} else if (targetType.equals(LikeTargetType.IMAGE)) {
				count.setImageLikeCount(1);
				keyWrapper.set(sku + "\t" + relationImageOwnerId);
			} else {
				String communityUserId = null;
				if (relationReviewOwnerId != null) {
					communityUserId = relationReviewOwnerId;
				}
				if (relationQuestionAnswerOwnerId != null) {
					communityUserId = relationQuestionAnswerOwnerId;
				}
				if (relationImageOwnerId != null) {
					communityUserId = relationImageOwnerId;
				}
				throw new IllegalArgumentException(
						"targetType is invalid. sku = "
						+ sku + ", communityUserId = " + communityUserId);
			}
			context.write(keyWrapper, count);
		}
	}

	/**
	 * いいね獲得件数の集計において、リデュース処理を行います。
	 * @author kamiike
	 */
	public static class GetLikeCountReducer extends Reducer<Text, ProductMasterCountData, Text, ProductMasterCountData> {

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
				Iterable<ProductMasterCountData> values,
                Context context) throws IOException, InterruptedException {
			ProductMasterCountData sum = new ProductMasterCountData();
			for (ProductMasterCountData val : values) {
				sum.setReviewLikeCount(sum.getReviewLikeCount() + val.getReviewLikeCount());
				sum.setAnswerLikeCount(sum.getAnswerLikeCount() + val.getAnswerLikeCount());
				sum.setImageLikeCount(sum.getImageLikeCount() + val.getImageLikeCount());
			}
			context.write(key, sum);
		}
	}
}
