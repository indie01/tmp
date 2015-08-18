package com.kickmogu.yodobashi.community.mapreduce.job;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.hadoop.JobTemplate;
import org.springframework.hadoop.configuration.JobFactoryBean;
import org.springframework.stereotype.Service;

import com.kickmogu.hadoop.mapreduce.parts.MultiTableInputFormat;
import com.kickmogu.hadoop.mapreduce.parts.MultiTableSplit;
import com.kickmogu.yodobashi.community.service.ReviewPointSummaryService;
import com.kickmogu.yodobashi.community.service.vo.ReviewPointSummaryVO;

/**
 * レビューのスコア更新を行うジョブです。
 * @author a.sakamoto
 */
@Service @Lazy @Scope("prototype")
public class ReviewPointSummaryLoaderJob extends AbstractCommunityJob {
	private static final PathFilter hiddenFileFilter = new PathFilter() {
		public boolean accept(Path p) {
			String name = p.getName();
			return !name.startsWith("_") && !name.startsWith(".");
		}
	};

	private static final String OUTPUT_TMP_DIR = "/tmp/ReviewPointSummaryLoaderJob/";
	public static final String TARGET_DATE_START_KEY = ReviewPointSummaryLoaderJob.class.getName() + ".targetDateStart";
	public static final String TARGET_DATE_END_KEY = ReviewPointSummaryLoaderJob.class.getName() + ".targetDateEnd";
	public static final String BULK_SIZE_KEY = ReviewPointSummaryLoaderJob.class.getName() + ".bulkSize";

//	public static final String REDUCE_NUM_KEY = ReviewPointSummaryLoaderJob.class.getName() + ".reduceNum";
//	@Autowired @Qualifier("productLoader")
//	private JdbcTemplate jdbcTemplate;

	@Autowired @Qualifier("default")
	private ReviewPointSummaryService summaryService;
	
	private byte[] family_bytes = Bytes.toBytes("cf");
	private byte[] df_bytes = Bytes.toBytes("DF");
	private byte[] modifyDateTime_bytes = Bytes.toBytes("modifyDateTime");
	private byte[] productId_bytes = Bytes.toBytes("productId");
	
	/**
	 * 入力フォーマットインスタンスを返します。<br />
	 * Job名 + InputFormat の Bean 名で定義します。
	 * @return 入力フォーマットインスタンス
	 * @throws ParseException 例外が発生した場合
	 */
	@Bean @Scope("prototype")
	public MultiTableInputFormat reviewPointSummaryLoaderInputFormat() throws ParseException {
		MultiTableInputFormat f = new MultiTableInputFormat();
		try {
			f.setConf(extConfiguration);
			
			FilterList filterList = new FilterList();
			
			// 論理削除を対象外とするFilter
			SingleColumnValueFilter ldelFlgFilter = new SingleColumnValueFilter(family_bytes, df_bytes, CompareFilter.CompareOp.EQUAL, Bytes.toBytes(false));
			ldelFlgFilter.setFilterIfMissing(false);
			
			// 対象範囲開始時刻≦更新時刻＜対象範囲終了時刻
			long targetDateStart = Long.parseLong(extConfiguration.get(TARGET_DATE_START_KEY));
			long targetDateEnd = Long.parseLong(extConfiguration.get(TARGET_DATE_END_KEY));
			SingleColumnValueFilter filterStartDateTime = new SingleColumnValueFilter(family_bytes, modifyDateTime_bytes, 
					CompareFilter.CompareOp.GREATER_OR_EQUAL, Bytes.toBytes(targetDateStart));
			SingleColumnValueFilter filterEndDateTime = new SingleColumnValueFilter(family_bytes, modifyDateTime_bytes, 
					CompareFilter.CompareOp.LESS, Bytes.toBytes(targetDateEnd));
			
			filterList.addFilter(ldelFlgFilter);
			filterList.addFilter(filterStartDateTime);
			filterList.addFilter(filterEndDateTime);
			
			Scan scan = new Scan();
			scan.addColumn(family_bytes, productId_bytes);
//			scan.addColumn(family_bytes, modifyDateTime_bytes);
			scan.setMaxVersions(1);
			scan.setCaching(1000);
			scan.setFilter(filterList);
			
			f.addTableInputInfo("reviewDO", scan);
			f.addTableInputInfo("questionDO", scan);
			f.addTableInputInfo("imageHeaderDO", scan);
			f.addTableInputInfo("productFollowDO", scan);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return f;
	}

	/**
	 * 出力フォーマットインスタンスを返します。<br />
	 * Job名 + OutputFormat の Bean 名で定義します。
	 * @return 入力フォーマットインスタンス
	 * @throws ParseException 例外が発生した場合
	 */
	@Bean @Scope("prototype")
	public TextOutputFormat<Text,NullWritable> reviewPointSummaryLoaderOutputFormat() throws ParseException {
		return new TextOutputFormat<Text,NullWritable>();
	}
	
	/**
	 * マップインスタンスを返します。<br />
	 * Job名 + Mapper の Bean 名で定義します。
	 * @return マップインスタンス
	 */
	@Bean @Scope("prototype")
	public ReviewPointSummaryLoaderMapper reviewPointSummaryLoaderMapper() {
		ReviewPointSummaryLoaderMapper mapper = new ReviewPointSummaryLoaderMapper();
		return mapper;
	}

	/**
	 * マップインスタンスを返します。<br />
	 * Job名 + Reducer の Bean 名で定義します。
	 * @return マップインスタンス
	 */
	@Bean @Scope("prototype")
	public ReviewPointSummaryLoaderReducer reviewPointSummaryLoaderReducer() {
		ReviewPointSummaryLoaderReducer reducer = new ReviewPointSummaryLoaderReducer();
		reducer.setReviewPointSummaryService(summaryService);
		return reducer;
	}
	
	/**
	 * ジョブインスタンスを返します。<br />
	 * Job名 の Bean 名で定義します。
	 * @return ジョブインスタンス
	 * @throws Exception 例外が発生した場合
	 */
	@Bean @Scope("prototype")
	public Job reviewPointSummaryLoader() throws Exception {
		JobFactoryBean factory = getJobFactoryBean();

		factory.setInputFormat(reviewPointSummaryLoaderInputFormat());
		factory.setOutputFormat(reviewPointSummaryLoaderOutputFormat());
		factory.setMapper(reviewPointSummaryLoaderMapper());
		factory.setReducer(reviewPointSummaryLoaderReducer());

		Job job = factory.getObject();
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(NullWritable.class);
		
		return job;
	}

	/**
	 * JOB名を返します。
	 * @return Job名です。
	 */
	@Override
	public String getJobName() {
		return "reviewPointSummaryLoader";
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
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddhhmmss");
		Date startDate;
		Date endDate;
		try {
			startDate = formatter.parse(args[0]);
			endDate = formatter.parse(args[1]);
		}
		catch (ParseException e) {
			System.err.println("format. yyyyMMddhhmmss");
			throw e;
		}

		System.out.println("target start datetime:" + formatter.format(startDate) + " (" + startDate.getTime() + 
				") target end   datetime:" + formatter.format(endDate) + " (" + endDate.getTime() + 
				") bulk size:" + args[2]);
		
		properties.setProperty(TARGET_DATE_START_KEY, Long.toString(startDate.getTime()));
		properties.setProperty(TARGET_DATE_END_KEY, Long.toString(startDate.getTime()));
		properties.setProperty(BULK_SIZE_KEY, args[2]);
		//properties.setProperty(REDUCE_NUM_KEY, args[3]);
		
		properties.setProperty(JobTemplate.SPRING_OUTPUT_PATH, OUTPUT_TMP_DIR);
		
		FileSystem fs = FileSystem.get(getConf());
		Path path = new Path(OUTPUT_TMP_DIR);
		if (fs.exists(path)) {
			fs.delete(path, true);
		}
	}
	
	public FileStatus[] listResultFileStatus(Configuration conf, String dir) throws IOException {
		FileSystem fs = FileSystem.get(conf);
		Path path = new Path(dir);
		FileStatus[] files = fs.listStatus(path, hiddenFileFilter);
		return files;
	}
	
	@Override
	protected void executeForAfter() throws Exception {
	}

	public static int execute(
			ApplicationContext parentContext,
			String startDateTime, String endDateTime, String bulkSize) throws Exception {
		return ToolRunner.run(new ReviewPointSummaryLoaderJob().setParentApplicationContext(parentContext),
				new String[]{startDateTime, endDateTime, bulkSize});
	}
	
	public static class ReviewPointSummaryLoaderMapper extends Mapper<ImmutableBytesWritable, Result, Text, NullWritable> {

		private byte[] family_bytes = Bytes.toBytes("cf");
		private byte[] productId_bytes = Bytes.toBytes("productId");
		
		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
			String tableName = Bytes.toString(((MultiTableSplit)context.getInputSplit()).getTableName());
			
			// TODO debug
			System.out.println(tableName);
		}
		
		@Override
		public void map(ImmutableBytesWritable nameAndRow, Result record, Context context) throws IOException, InterruptedException {
			byte[] sku_bytes = record.getValue(family_bytes, productId_bytes);
			if (sku_bytes == null) return;

			String sku = Bytes.toString(sku_bytes);
			if (sku.isEmpty()) return;
			
			System.out.println(sku);
			context.write(new Text(sku), NullWritable.get());
			
			// TODO debug
//			byte[] modifyDateTime_bytes = record.getValue(family_bytes, Bytes.toBytes("modifyDateTime"));
//			Date d = null;
//			if (modifyDateTime_bytes != null && modifyDateTime_bytes.length >= 8) {
//				d = new Date();
//				d.setTime(Bytes.toLong(modifyDateTime_bytes));
//			}
//			System.out.println(sku + ":" + d);
		}
	}
	
	public static class ReviewPointSummaryLoaderReducer extends Reducer<Text, NullWritable, Text, NullWritable> {
		
		ReviewPointSummaryService summarySerivce;
		
		public void setReviewPointSummaryService(ReviewPointSummaryService summaryService) {
			this.summarySerivce = summaryService;
		}
		
		private int skuFindMax = 1000;
		private List<String> skus = null;
				
		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
			skuFindMax = Integer.parseInt(context.getConfiguration().get(BULK_SIZE_KEY, "1000"));
			skus = new ArrayList<String>(skuFindMax);
		}
		
		/**
		 * 後処理を行います。
		 */
		@Override
		protected void cleanup(Context context) throws IOException, InterruptedException {
			if (skus.size() > 0) {
				writeProductInformation(context);
			}
		}
		
		void writeProductInformation(Context context) throws IOException, InterruptedException {
			String[] array = skus.toArray(new String[]{});
			
			// TODO debug
			for (String sku:array) {
				System.out.println(sku);
			}
			
			List<ReviewPointSummaryVO> list = summarySerivce.ｇetProductInfomation(array);
			for (ReviewPointSummaryVO vo:list) {
				/*
				SKU	商品コード
				AVERAGERATING	評価平均数
				REVIEWTOTALCOUNT	レビュー件数
				QATOTALCOUNT	Q&A件数
				POSTIMAGECOUNT	画像投稿数
				PRODUCTFOLLOWCOUNT	商品フォロー数
				*/
				StringBuffer val = new StringBuffer();
				val.append(vo.getSku());
				val.append("," + vo.getAverageRating());
				val.append("," + vo.getReviewTotalCount());
				val.append("," + vo.getQaTotalCount());
				val.append("," + vo.getPostImageCount());
				val.append("," + vo.getProductFollowerCount());
				context.write(new Text(val.toString()), NullWritable.get());
			}
			skus.clear();
		}

		@Override
		public void reduce(Text skuText, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
			String sku = skuText.toString();
			skus.add(sku);
			if (skus.size() > skuFindMax) {
				writeProductInformation(context);
			}
		}
	}
}
