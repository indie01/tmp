package com.kickmogu.yodobashi.community.mapreduce.job;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.hadoop.JobTemplate;
import org.springframework.hadoop.configuration.JobFactoryBean;
import org.springframework.stereotype.Service;

import com.kickmogu.hadoop.mapreduce.job.util.ThreadsafeOptionBuilder;
import com.kickmogu.lib.core.utils.ConfigUtil;
import com.kickmogu.lib.hadoop.hbase.io.HBaseTableInputFormat;
import com.kickmogu.lib.solr.SolrConstants;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.resource.config.HBaseConfig;
import com.kickmogu.yodobashi.community.resource.dao.util.SolrUtil;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewType;

/**
 * レビューの表示順（ソートタイプ毎）を出力します。<br/>
 * （ページ上に表示されているレビューを対象とする）
 * ※SASによるデータ分析で使用
 *
 * @author m.takahashi
 *
 */
@Service @Lazy @Scope("prototype")
public class ReviewSortOrderJob extends AbstractCommunityJob {

	private static final Logger logger = LoggerFactory.getLogger(ReviewSortOrderJob.class);

	// JOB名
	private static final String JOB_NAME = "reviewSortOrder";

	private static final String SORT_TYPE = "sortType";
	/** 全て+適合順 */
	private static final String SORT_TYPE01 = SORT_TYPE + "01";
	/** 全て+最新順 */
	private static final String SORT_TYPE02 = SORT_TYPE + "02";
	/** 第一印象レビュー+適合順 */
	private static final String SORT_TYPE03 = SORT_TYPE + "03";
	/** 第一印象レビュー+最新順 */
	private static final String SORT_TYPE04 = SORT_TYPE + "04";
	/** 満足度レビュー+適合順 */
	private static final String SORT_TYPE05 = SORT_TYPE + "05";
	/** 満足度レビュー+最新順 */
	private static final String SORT_TYPE06 = SORT_TYPE + "06";

	// 出力一時ディレクトリ
	private static String tempDir = "";
	// Service Context
	private static final String SERVICE_CONTEXT = "serviceContext.xml";

	/**
	 * Solr操作
	 */
	@Autowired @Qualifier("default")
	private SolrOperations solrOperations;

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
		help.printHelp(ReviewSortOrderJob.class.getSimpleName(), options, true);
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
				.run(new ReviewSortOrderJob().setParentApplicationContext(parentContext),
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
				org.springframework.util.StringUtils
						.uncapitalize(config.hbaseTableNamePrefix
								+ ReviewDO.class.getSimpleName()));

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
	public Job reviewSortOrder() throws Exception {
		print("[reviewSortOrder]");

		JobFactoryBean factory = getJobFactoryBean();
		factory.setInputFormat(reviewSortOrderInputFormat());
		factory.setOutputFormat(reviewSortOrderOutputFormat());
		factory.setMapper(reviewSortOrderMapper());
		factory.setReducer(reviewSortOrderReducer());
		Job job = factory.getObject();
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		MultipleOutputs.addNamedOutput(job, SORT_TYPE01, TextOutputFormat.class, Text.class, Text.class);
		MultipleOutputs.addNamedOutput(job, SORT_TYPE02, TextOutputFormat.class, Text.class, Text.class);
		MultipleOutputs.addNamedOutput(job, SORT_TYPE03, TextOutputFormat.class, Text.class, Text.class);
		MultipleOutputs.addNamedOutput(job, SORT_TYPE04, TextOutputFormat.class, Text.class, Text.class);
		MultipleOutputs.addNamedOutput(job, SORT_TYPE05, TextOutputFormat.class, Text.class, Text.class);
		MultipleOutputs.addNamedOutput(job, SORT_TYPE06, TextOutputFormat.class, Text.class, Text.class);

		return job;
	}

	/**
	 * 入力フォーマットインスタンスを返します。<br />
	 * Job名 + InputFormat の Bean 名で定義します。
	 * @return 入力フォーマットインスタンス
	 */
	@Bean @Scope("prototype")
	public HBaseTableInputFormat reviewSortOrderInputFormat() {
		HBaseTableInputFormat inputFormat = new HBaseTableInputFormat();
		inputFormat.setConf(extConfiguration);

		byte[] family = Bytes.toBytes("cf");
		byte[] qualifier_sku = Bytes.toBytes("productId");
		Scan scan = inputFormat.getScan();
		scan.addColumn(family, qualifier_sku);
		scan.setMaxVersions(1);
		scan.setCaching(1000);
		inputFormat.setScan(scan);
		return inputFormat;
	}

	/**
	 * 出力フォーマットインスタンスを返します。<br />
	 * Job名 + OutputFormat の Bean 名で定義します。
	 * @return 入力フォーマットインスタンス
	 * @throws ParseException 例外が発生した場合
	 */
	@Bean @Scope("prototype")
	public TextOutputFormat<Text,Text> reviewSortOrderOutputFormat() throws ParseException {
		return new TextOutputFormat<Text,Text>();
	}

	/**
	 * マップインスタンスを返します。<br />
	 * Job名 + Mapper の Bean 名で定義します。
	 * @return マップインスタンス
	 */
	@Bean @Scope("prototype")
	public ReviewSortOrderMapper reviewSortOrderMapper() {
		ReviewSortOrderMapper mapper = new ReviewSortOrderMapper();
		return mapper;
	}

	/**
	 * Reduerインスタンスを返します。<br />
	 * Job名 + Reducer の Bean 名で定義します。
	 * @return Reduerインスタンス
	 */
	@Bean @Scope("prototype")
	public ReviewSortOrderReducer reviewSortOrderReducer() {
		ReviewSortOrderReducer reducer = new ReviewSortOrderReducer();
		reducer.setSolrOperations(solrOperations);
		return reducer;
	}

	private void print(Object o) {
//		System.out.println(o);
		logger.info(String.valueOf(o));
	}

	/**
	 * Mapper：対象データからSKUを取り出します。
	 *
	 */
	public static class ReviewSortOrderMapper extends Mapper<ImmutableBytesWritable, Result, Text, Text> {

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

			// レビューID
			String reviewIdStr = Bytes.toString(key.get());
			// ステータス
			byte[] status = value.getValue(Bytes.toBytes("cf"), Bytes.toBytes("status"));
			String statusStr =  Bytes.toString(status);
			// SKU
			byte[] sku_bytes = value.getValue(Bytes.toBytes("cf"), Bytes.toBytes("productId"));
			String skuStr = Bytes.toString(sku_bytes);
			logger.info("["+reviewIdStr+"]["+statusStr+"]["+skuStr+"]");

			if (StringUtils.isEmpty(skuStr)) {
				return;
			}
			if (ContentsStatus.SUBMITTED.getCode().equals(statusStr)) {
				context.write(new Text(skuStr), new Text(reviewIdStr));
				mapOut++;
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

	/**
	 * Reducer：表示順データを作成します。
	 *
	 */
	public static class ReviewSortOrderReducer extends Reducer<Text, Text, Text, Text> {

		// 検索制限数
		private static final int READ_LIMIT = SolrConstants.QUERY_ROW_LIMIT;

		private SolrOperations solrOperations;
		public void setSolrOperations(SolrOperations solrOperations) {
			this.solrOperations = solrOperations;
		}

		private long redIn = 0;
		private long redOut = 0;

		/** 複数出力 */
		private MultipleOutputs<Text, Text> multipleOutputs;

		/**
		 *
		 */
		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
			multipleOutputs = new MultipleOutputs<Text, Text>(context);
		}

		/**
		 *
		 */
		@Override
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			logger.info("reduce ----------------------------------");
			redIn++;

			String sku = key.toString();
			logger.info(sku);

			ProductDO product = new ProductDO();
			product.setSku(sku);

			// 全て+適合順
			logger.info(SORT_TYPE01+"出力");
			outputSortType1(product);

			// 全て+最新順
			logger.info(SORT_TYPE02+"出力");
			outputSortType2(product);

			// 第一印象レビュー+適合順
			logger.info(SORT_TYPE03+"出力");
			outputSortType3(product);

			// 第一印象レビュー+最新順
			logger.info(SORT_TYPE04+"出力");
			outputSortType4(product);

			// 満足度レビュー+適合順
			logger.info(SORT_TYPE05+"出力");
			outputSortType5(product);

			// 満足度レビュー+最新順
			logger.info(SORT_TYPE06+"出力");
			outputSortType6(product);

			redOut++;
		}

		/**
		 * 全て+適合順
		 * @param product
		 * @throws IOException
		 * @throws InterruptedException
		 */
		private void outputSortType1(ProductDO product) throws IOException, InterruptedException {
			String sku = product.getSku();
			long total = countReviewByCondition(sku, null);
			SolrQuery solrQuery = new SolrQuery(buildQuery(sku, null));
			solrQuery.setSortField("reviewScore_d", ORDER.desc);
			solrQuery.addSortField("postDate_dt", ORDER.desc);
			writeData(total, solrQuery, SORT_TYPE01, sku);
		}

		/**
		 * 全て+最新順
		 * @param product
		 * @throws IOException
		 * @throws InterruptedException
		 */
		private void outputSortType2(ProductDO product) throws IOException, InterruptedException {
			String sku = product.getSku();
			long total = countReviewByCondition(sku, null);
			SolrQuery solrQuery = new SolrQuery(buildQuery(sku, null));
			solrQuery.setSortField("postDate_dt", ORDER.desc);
			writeData(total, solrQuery, SORT_TYPE02, sku);
		}

		/**
		 * 第一印象レビュー+適合順
		 * @param product
		 * @throws IOException
		 * @throws InterruptedException
		 */
		private void outputSortType3(ProductDO product) throws IOException, InterruptedException {
			String sku = product.getSku();
			// レビュータイプ
			String query = " AND reviewType_s:" + SolrUtil.escape(ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE.getCode());
			long total = countReviewByCondition(sku, query);
			SolrQuery solrQuery = new SolrQuery(buildQuery(sku, query));
			solrQuery.setSortField("reviewScore_d", ORDER.desc);
			solrQuery.addSortField("postDate_dt", ORDER.desc);
			writeData(total, solrQuery, SORT_TYPE03, sku);
		}

		/**
		 * 第一印象レビュー+最新順
		 * @param product
		 * @throws IOException
		 * @throws InterruptedException
		 */
		private void outputSortType4(ProductDO product) throws IOException, InterruptedException {
			String sku = product.getSku();
			// レビュータイプ
			String query = " AND reviewType_s:" + SolrUtil.escape(ReviewType.REVIEW_IMMEDIATELY_AFTER_PURCHASE.getCode());
			long total = countReviewByCondition(sku, query);
			SolrQuery solrQuery = new SolrQuery(buildQuery(sku, query));
			solrQuery.setSortField("postDate_dt", ORDER.desc);
			writeData(total, solrQuery, SORT_TYPE04, sku);
		}

		/**
		 * 満足度レビュー+適合順
		 * @param product
		 * @throws IOException
		 * @throws InterruptedException
		 */
		private void outputSortType5(ProductDO product) throws IOException, InterruptedException {
			String sku = product.getSku();
			// レビュータイプ
			String query = " AND reviewType_s:" + SolrUtil.escape(ReviewType.REVIEW_AFTER_FEW_DAYS.getCode());
			long total = countReviewByCondition(sku, query);
			SolrQuery solrQuery = new SolrQuery(buildQuery(sku, query));
			solrQuery.setSortField("reviewScore_d", ORDER.desc);
			solrQuery.addSortField("postDate_dt", ORDER.desc);
			writeData(total, solrQuery, SORT_TYPE05, sku);
		}

		/**
		 * 満足度レビュー+最新順
		 * @param product
		 * @throws IOException
		 * @throws InterruptedException
		 */
		private void outputSortType6(ProductDO product) throws IOException, InterruptedException {
			String sku = product.getSku();
			// レビュータイプ
			String query = " AND reviewType_s:" + SolrUtil.escape(ReviewType.REVIEW_AFTER_FEW_DAYS.getCode());
			long total = countReviewByCondition(sku, query);
			SolrQuery solrQuery = new SolrQuery(buildQuery(sku, query));
			solrQuery.setSortField("postDate_dt", ORDER.desc);
			writeData(total, solrQuery, SORT_TYPE06, sku);
		}

		/**
		 * @param sku
		 * @param query
		 * @return
		 */
		private long countReviewByCondition(String sku, String query) {
			SolrQuery solrQuery = new SolrQuery(buildQuery(sku, query));
			long total = solrOperations.count(solrQuery, ReviewDO.class);
			logger.info("対象データ件数:"+total+"件");
			return total;
		}

		/**
		 * @param sku
		 * @param query
		 * @return
		 */
		private String buildQuery(String sku, String query) {
			StringBuilder buffer = new StringBuilder();
			buffer.append("withdraw_b:false AND productId_s:" + SolrUtil.escape(sku));
			buffer.append(" AND ( ");
			buffer.append("status_s:");
			buffer.append(ContentsStatus.SUBMITTED.getCode());
			buffer.append(" OR status_s:");
			buffer.append(ContentsStatus.CONTENTS_STOP.getCode());
			buffer.append(" ) ");
			if (StringUtils.isNotEmpty(query)) {
				buffer.append(query);
			}
//			logger.info(buffer.toString());
			return buffer.toString();
		}

		/**
		 * 表示順データ出力
		 * @param total
		 * @param solrQuery
		 * @param sortType
		 * @param sku
		 * @throws IOException
		 * @throws InterruptedException
		 */
		private void writeData(long total, SolrQuery solrQuery, String sortType, String sku) throws IOException, InterruptedException {
			int sortOrder = 0;
			int offset = 0;
			int cnt = 0;
			String reviewType = "";
			while (cnt < total) {
				int processed = 0;
				solrQuery.setRows(READ_LIMIT).setStart(offset);
				List<ReviewDO> list = findReviewList(solrQuery);
				for (ReviewDO reviewDO : list) {
					processed++;
					sortOrder++;
					write(sortType, reviewDO.getReviewId(), sortOrder);
					if (reviewDO.getReviewType() != null) {
						reviewType = reviewDO.getReviewType().getCode();
					} else {
						reviewType = "";
					}
					logger.info(reviewDO.getReviewId()+"\t"+String.valueOf(sortOrder)+"\t"+sku+"\t["+reviewType+"]");
				}
				cnt = cnt + processed;
				offset = cnt;
			}
		}

		/**
		 * ReviewDOのリストを取得
		 * @param solrQuery
		 * @return
		 */
		private List<ReviewDO> findReviewList(SolrQuery solrQuery) {
			SearchResult<ReviewDO> searchResult = new SearchResult<ReviewDO>(
			solrOperations.findByQuery(solrQuery, ReviewDO.class, com.kickmogu.lib.core.resource.Path.DEFAULT));
			return searchResult != null ? searchResult.getDocuments() : new ArrayList<ReviewDO>();
		}

		/**
		 * 表示順データ書き込み
		 * @param sortType
		 * @param reviewId
		 * @param sortOrder
		 * @throws IOException
		 * @throws InterruptedException
		 */
		private void write(String sortType, String reviewId, int sortOrder) throws IOException, InterruptedException {
			Text key = new Text(reviewId);
			Text value = new Text(String.valueOf(sortOrder));
			try {
				multipleOutputs.write(sortType, key, value);
			} catch (IOException e) {
				logger.info(e.getMessage());
				e.printStackTrace();
				throw e;
			}
		}

		/**
		 * 後処理を行います。
		 */
		@Override
		protected void cleanup(Context context) throws IOException, InterruptedException {
			multipleOutputs.close();
			logger.info("redIn:"+String.valueOf(redIn));
			logger.info("redOut:"+String.valueOf(redOut));
		}

	}

}