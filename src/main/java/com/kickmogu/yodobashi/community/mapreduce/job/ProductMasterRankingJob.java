package com.kickmogu.yodobashi.community.mapreduce.job;

import java.io.IOException;
import java.util.Properties;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;
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
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.utils.ConfigUtil;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.io.HBaseTableInputFormat;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.mapreduce.output.ProductMasterRankingRecordWriter;
import com.kickmogu.yodobashi.community.mapreduce.output.ProductMasterRankingRecordWriter.ProductMasterRankingData;
import com.kickmogu.yodobashi.community.mapreduce.output.SimpleOutputFormat;
import com.kickmogu.yodobashi.community.resource.config.HBaseConfig;
import com.kickmogu.yodobashi.community.resource.dao.OrderDao;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityOperationStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;
import com.kickmogu.yodobashi.community.service.ProductService;
import com.kickmogu.yodobashi.community.service.SystemMaintenanceService;

/**
 * 商品マスターのランキングを行うジョブです。
 * @author kamiike
 */
@Service @Lazy @Scope("prototype")
public class ProductMasterRankingJob extends AbstractCommunityJob implements InitializingBean {

	/**
	 * 注文 DAO です。
	 */
	@Autowired
	private OrderDao orderDao;

	/**
	 * HBaseアクセサです。
	 */
	@Autowired @Qualifier("default")
	private HBaseOperations hBaseOperations;

	/**
	 * Solrアクセサです。
	 */
	@Autowired  @Qualifier("default")
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
	 * マップを返します。<br />
	 * Job名 + Mapper の Bean 名で定義します。
	 * @return マップ
	 */
	@Bean @Scope("prototype")
	public ProductMasterRankingMapper productMasterRankingMapper() {
		ProductMasterRankingMapper mapper = new ProductMasterRankingMapper();
		mapper.setVersion(version);
		return mapper;
	}

	/**
	 * リデューサーを返します。<br />
	 * Job名 + Reducer の Bean 名で定義します。
	 * @return リデューサー
	 */
	@Bean @Scope("prototype")
	public ProductMasterRankingReducer productMasterRankingReducer() {
		ProductMasterRankingReducer reducer = new ProductMasterRankingReducer();
		reducer.setOrderDao(orderDao);
		return reducer;
	}

	/**
	 * 入力フォーマットインスタンスを返します。<br />
	 * Job名 + InputFormat の Bean 名で定義します。
	 * @return 入力フォーマットインスタンス
	 */
	@Bean @Scope("prototype")
	public HBaseTableInputFormat productMasterRankingInputFormat() {
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
	public SimpleOutputFormat<Text, ProductMasterRankingData> productMasterRankingOutputFormat() {
		return new SimpleOutputFormat<Text, ProductMasterRankingData>(
				new ProductMasterRankingRecordWriter(
				version, hBaseOperations, solrOperations, timestampHolder));
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
	@Bean
	public Job productMasterRanking() throws Exception {
		JobFactoryBean factory = getJobFactoryBean();

		factory.setInputFormat(productMasterRankingInputFormat());
		factory.setMapper(productMasterRankingMapper());
		factory.setReducer(productMasterRankingReducer());
		factory.setOutputFormat(productMasterRankingOutputFormat());
		Job job = factory.getObject();

		job.setPartitionerClass(ProductMasterRankingPartitioner.class);
		job.setSortComparatorClass(ProductMasterRankingSortComparator.class);
		job.setGroupingComparatorClass(Text.Comparator.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(NullWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(ProductMasterRankingData.class);
		
		return job;
	}

	/**
	 * JOB名を返します。
	 * @return Job名です。
	 */
	@Override
	public String getJobName() {
		return "productMasterRanking";
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
		
		HBaseConfig config = new HBaseConfig();
		ConfigUtil.loadProperties(getProfileName(), "resource-config", config);
		properties.setProperty(TableInputFormat.INPUT_TABLE,
				StringUtils.uncapitalize(config.hbaseTableNamePrefix + ProductMasterDO.class.getSimpleName()));
		properties.setProperty(TableInputFormat.SCAN_COLUMNS,
				"cf:version cf:productId cf:communityUserId cf:productMasterScore cf:withdraw cf:deleteFlag");
		properties.setProperty(HBaseTableInputFormat.DELETE_FLG_FAMILY, "cf");
	}

	/**
	 * 商品マスターのランキング処理を実行します。
	 * @param parentContext 親コンテキスト
	 * @return 実行結果
	 * @throws Exception 例外が発生した場合
	 */
	public static int execute(ApplicationContext parentContext) throws Exception {
		
		// ReadOnlyモードチェック
		SystemMaintenanceService systemMaintenanceService = parentContext.getBean(SystemMaintenanceService.class);
		if (systemMaintenanceService.getCommunityOperationStatus().equals(CommunityOperationStatus.READONLY_OPERATION)) {
			throw new CommonSystemException("Stopped for " + CommunityOperationStatus.READONLY_OPERATION.getLabel());
		}
		
		return ToolRunner.run(
				new ProductMasterRankingJob().setParentApplicationContext(parentContext),
				new String[0]);
	}

	/**
	 * 商品マスターのランキング処理において、マップ処理を行います。
	 * @author kamiike
	 *
	 */
	public static class ProductMasterRankingMapper extends Mapper<ImmutableBytesWritable, Result, Text, NullWritable> {

		/**
		 * キー情報のラッパーです。
		 */
		private static Text keyWrapper = new Text();

		/**
		 * 対象バージョンです。
		 */
		private int version;

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
			String productMasterScore = null;
			for (KeyValue keyValue : value.raw()) {
				String columnName = Bytes.toString(
						keyValue.getBuffer(),
						keyValue.getQualifierOffset(),
						keyValue.getQualifierLength());
				if (columnName.equals("productId")) {
					sku = getStringValue(keyValue);
				} else if (columnName.equals("version")) {
					int readVersion = getIntegerValue(keyValue);
					if (readVersion != version) {
						return;
					}
				} else if (columnName.equals("withdraw")) {
					if (getBooleanValue(keyValue)) {
						return;
					}
				} else if (columnName.equals("communityUserId")) {
					communityUserId = getStringValue(keyValue);
				} else if (columnName.equals("productMasterScore")) {
					productMasterScore = String.valueOf(
							getFloatValue(keyValue));
				} else if (columnName.equals("deleteFlag")) {
					if (getBooleanValue(keyValue)) {
						return;
					}
				}
			}
			keyWrapper.set(sku + "\t" + communityUserId + "\t" + productMasterScore);
			context.write(keyWrapper, NullWritable.get());
		}

		/**
		 * 対象バージョンを設定します。
		 * @param version 対象バージョン
		 */
		public void setVersion(int version) {
			this.version = version;
		}
	}

	/**
	 * 商品マスターのランキング処理において、リデュース処理を行います。<br />
	 * 「order inversion」パターンを用いて実装しています。
	 * @author kamiike
	 */
	public static class ProductMasterRankingReducer extends Reducer<Text, NullWritable, Text, ProductMasterRankingData> {

		/**
		 * キー情報のラッパーです。
		 */
		private static Text keyWrapper = new Text();

		/**
		 * 注文 DAO です。
		 */
		private OrderDao orderDao;

		/**
		 * 前回処理した SKU です。
		 */
		private String preSku = null;

		/**
		 * 前回処理したデータのスコアです。
		 */
		private String preScore = null;

		/**
		 * ランクです。
		 */
		private int rank = 0;

		/**
		 * インデックスです。
		 */
		private int index = 1;

		/**
		 * 注文 DAO を設定します。
		 * @param orderDao 注文 DAO
		 */
		public void setOrderDao(OrderDao orderDao) {
			this.orderDao = orderDao;
		}

		/**
		 * 初期化処理を行います。
		 * @param context コンテキスト
		 * @throws IOException 入出力例外が発生した場合
		 * @throws InterruptedException 中断例外が発生した場合
		 */
		protected void setup(Context context) throws IOException, InterruptedException {
			initialize(null);
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
				Iterable<NullWritable> values,
                Context context) throws IOException, InterruptedException {
			String[] keyParts = key.toString().split("\t");
			String sku = keyParts[0];
			String communityUserId = keyParts[1];
			String productMasterScore = keyParts[2];

			if (preSku == null || !preSku.equals(sku)) {
				initialize(sku);
			}

			keyWrapper.set(sku + "\t" + communityUserId);

			PurchaseProductDO purchaseProduct = orderDao.loadPurchaseProductBySku(
					communityUserId, sku, Path.includeProp("*").includePath(
									"purchaseDate,adult,deleteFlag,communityUser.status").depth(1), false);
			if (purchaseProduct == null ||
					purchaseProduct.isDeleted() || !purchaseProduct.getCommunityUser(
					).getStatus().equals(CommunityUserStatus.ACTIVE)) {
				if (purchaseProduct == null) {
					context.setStatus("Input is invalid. data = " + key.toString());
				} else if (purchaseProduct.isDeleted()) {
						context.setStatus("PurchaseProduct is nothing. May have already been deleted.");
				} else {
					context.setStatus("CommunityUserStatus is not active. status = "
							+ purchaseProduct.getCommunityUser().getStatus());
				}
				ProductMasterRankingData data = new ProductMasterRankingData();
				data.setDeleteFlag(true);
				context.write(keyWrapper, data);
				return;
			}

			if (preScore == null || !preScore.equals(productMasterScore)) {
				preScore = productMasterScore;
				rank = index;
			}

			ProductMasterRankingData data = new ProductMasterRankingData();
			data.setAdult(purchaseProduct.isAdult());
			data.setRank(rank);
			data.setPurchaseDate(purchaseProduct.getPurchaseDate());

			context.write(keyWrapper, data);

			index++;
		}

		/**
		 * 最後のキーに対して処理を行います。
		 * @param context コンテキスト
		 * @throws IOException 入出力例外が発生した場合
		 * @throws InterruptedException 中断例外が発生した場合
		 */
		protected void cleanup(Context context) throws IOException, InterruptedException {
			initialize(null);
		}

		private void initialize(String sku) {
			preSku = sku;
			preScore = null;
			rank = 0;
			index = 1;
		}
	}

	/**
	 * リデューサに渡す値をソートします。
	 * @author kamiike
	 *
	 */
	public static class ProductMasterRankingSortComparator implements RawComparator<Text> {

		/**
		 * テキスト型１です。
		 */
		private static Text first = new Text();

		/**
		 * テキスト型２です。
		 */
		private static Text second = new Text();

		/**
		 * 順番を比較します。
		 * @param o1 値1
		 * @param o2 値2
		 * @return 比較結果
		 */
		@Override
		public int compare(Text o1, Text o2) {
			String[] values1 = o1.toString().split("\t");
			String[] values2 = o2.toString().split("\t");

			if (!values1[0].equals(values2[0])) {
				return values1[0].compareTo(values2[0]);
			}

			float v1 = Float.valueOf(values1[2]);
			float v2 = Float.valueOf(values2[2]);
			if ((v2 - v1) == 0.0) {
				return 0;
			} else if ((v2 - v1) < 0.0) {
				return -1;
			} else {
				return 1;
			}
		}

		@Override
		public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
			int n1 = WritableUtils.decodeVIntSize(b1[s1]);
			int n2 = WritableUtils.decodeVIntSize(b2[s2]);
			first.set(b1, s1+n1, l1-n1);
			second.set(b2, s2+n2, l2-n2);
			return compare(first, second);
		}

	}

	/**
	 * 商品マスターのランキング処理において、パーティション処理を行います。<br />
	 * @author kamiike
	 *
	 */
	public static class ProductMasterRankingPartitioner extends HashPartitioner<Text, NullWritable> {

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
				NullWritable value,
                int numReduceTasks) {
			String[] keyParts = key.toString().split("\t");
			String sku = keyParts[0];
			keyWrapper.set(sku);
			return super.getPartition(keyWrapper, value, numReduceTasks);
		}

	}
}
