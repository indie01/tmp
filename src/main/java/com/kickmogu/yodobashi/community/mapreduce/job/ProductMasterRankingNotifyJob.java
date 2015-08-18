package com.kickmogu.yodobashi.community.mapreduce.job;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
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

import com.google.common.collect.Sets;
import com.kickmogu.lib.core.domain.SearchResult;
import com.kickmogu.lib.core.exception.CommonSystemException;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.mapreduce.input.SolrInputFormat;
import com.kickmogu.yodobashi.community.mapreduce.input.SolrInputFormatHandler;
import com.kickmogu.yodobashi.community.resource.dao.ProductDao;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityOperationStatus;
import com.kickmogu.yodobashi.community.service.ProductService;
import com.kickmogu.yodobashi.community.service.SystemMaintenanceService;
import com.kickmogu.yodobashi.community.service.aop.CommunitySendMessageHandler;

/**
 * 商品マスターのランキング更新の通知を行うジョブです。
 * @author kamiike
 */
@Service @Lazy @Scope("prototype")
public class ProductMasterRankingNotifyJob extends AbstractCommunityJob implements InitializingBean {

	/**
	 * Solrアクセサです。
	 */
	@Autowired @Qualifier("default-RefMaster")
	private SolrOperations solrOperations;

	/**
	 * 非同期ハンドラです。
	 */
	@Autowired
	private CommunitySendMessageHandler sendMessageHandler;

	/**
	 * 商品サービスです。
	 */
	@Autowired
	private ProductService productService;
	
	/**
	 * 商品 DAO です。
	 */
	@Autowired @Qualifier("catalog")
	private ProductDao productDao;

	/**
	 * 初期化の後処理をします。
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		sendMessageHandler.setForceSyncNow(true);
	}

	/**
	 * 入力フォーマットインスタンスを返します。<br />
	 * Job名 + InputFormat の Bean 名で定義します。
	 * @return 入力フォーマットインスタンス
	 */
	@Bean @Scope("prototype")
	public SolrInputFormat<String, ProductMasterDO> productMasterRankingNotifyInputFormat() {
		int version = productService.getNextProductMasterVersion().getVersion() - 1;
		StringBuilder buffer = new StringBuilder();
		buffer.append("version_i:");
		buffer.append(version);
		buffer.append(" AND requiredNotify_b:true");
		
		return new SolrInputFormat<String, ProductMasterDO>(
				solrOperations,
				buffer.toString(),
				Path.includeProp(
				"*").depth(0), ProductMasterDO.class, "registerDateTime_dt",
						new ProductMasterRankingNotifySolrInputFormatHandler(productDao));
	}

	/**
	 * マップインスタンスを返します。<br />
	 * Job名 + Mapper の Bean 名で定義します。
	 * @return マップインスタンス
	 */
	@Bean @Scope("prototype")
	public ProductMasterRankingNotifyMapper productMasterRankingNotifyMapper() {
		ProductMasterRankingNotifyMapper mapper = new ProductMasterRankingNotifyMapper();
		mapper.setProductService(productService);
		return mapper;
	}

	/**
	 * ジョブインスタンスを返します。<br />
	 * Job名 の Bean 名で定義します。
	 * @return ジョブインスタンス
	 * @throws Exception 例外が発生した場合
	 */
	@Bean @Scope("prototype")
	public Job productMasterRankingNotify() throws Exception {
		JobFactoryBean factory = getJobFactoryBean();

		factory.setInputFormat(productMasterRankingNotifyInputFormat());
		factory.setMapper(productMasterRankingNotifyMapper());
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
		return "productMasterRankingNotify";
	}
		
	/**
	 * 商品マスターのランキング更新の通知を実行します。
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
				new ProductMasterRankingNotifyJob(
						).setParentApplicationContext(parentContext), new String[0]);
	}
	
	/**
	 * 商品マスターのランキング更新の通知において、Solrからの入力処理を行います。
	 * <p>Solr検索時depth(0)で関連idのみ取得し、MapReduce専用のfindBySkuForMR()を利用しLocalSolrにある商品DO（DBProductDetailDO等）を参照する。</p>
	 * @author a.sakamoto
	 */
	public static class ProductMasterRankingNotifySolrInputFormatHandler implements SolrInputFormatHandler<ProductMasterDO> {
		/**
		 * 商品 DAO です。
		 */
		private ProductDao productDao;
		
		public ProductMasterRankingNotifySolrInputFormatHandler(ProductDao productDao) {
			this.productDao = productDao;
		}
		
		/**
		 * Solrから規定数(500)取得した時に呼ばれる
		 */
		@Override
		public void handlePostSolrQuery(SearchResult<ProductMasterDO> searchResult) {
			Set<String> skus = Sets.newHashSet();
			List<ProductMasterDO> list = searchResult.getDocuments();
			for (ProductMasterDO productMaster:list) {
				// なぜか本番データでproductがNullであるデータがみつかった
				if (productMaster.getProduct() != null) {
					skus.add(productMaster.getProduct().getSku());
				}
			}
			Map<String,ProductDO> products = productDao.findBySkuForMR(skus);
			for (ProductMasterDO productMaster:list) {
				if (productMaster.getProduct() != null) {
					ProductDO product = products.get(productMaster.getProduct().getSku());
					productMaster.setProduct(product);
				}
			}
		}
	}
	
	/**
	 * 商品マスターのランキング更新の通知において、マップ処理を行います。
	 * @author kamiike
	 *
	 */
	public static class ProductMasterRankingNotifyMapper extends Mapper<String, ProductMasterDO, NullWritable, NullWritable> {

		/**
		 * 商品サービスです。
		 */
		private ProductService productService;

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
				ProductMasterDO value,
				Context context)
				throws IOException,
				InterruptedException {
			
			if (value.getProduct() == null) {
				// なぜか本番データでproductがNullであるデータがみつかった
				System.err.println("ProductMasterDO.product is null. id:[" + value.getProductMasterId() + "]");
				context.setStatus("ProductMasterDO.product is null. id:[" + value.getProductMasterId() + "]");
			}
			else {
				productService.changeProductMasterRanking(value);
			}
		}

		/**
		 * 商品サービスを設定します。
		 * @param productService 商品サービス
		 */
		public void setProductService(ProductService productService) {
			this.productService = productService;
		}
	}
}
