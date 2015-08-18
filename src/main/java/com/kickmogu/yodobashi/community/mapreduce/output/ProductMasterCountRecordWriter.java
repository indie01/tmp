/**
 *
 */
package com.kickmogu.yodobashi.community.mapreduce.output;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.mapreduce.output.ProductMasterCountRecordWriter.ProductMasterCountData;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.ScoreFactorDO;

/**
 * 商品マスターのカウントレコードライターです。
 * @author kamiike
 *
 */
public class ProductMasterCountRecordWriter extends RecordWriter<Text, ProductMasterCountData> {

	/**
	 * バージョンです。
	 */
	private Integer version;

	/**
	 * HBaseアクセサです。
	 */
	private HBaseOperations hBaseOperations;

	/**
	 * Solrアクセサです。
	 */
	private SolrOperations solrOperations;

	/**
	 * タイムスタンプホルダーです。
	 */
	private TimestampHolder timestampHolder;

	/**
	 * 更新対象プロパティリストです。
	 */
	private String updateProperties;

	/**
	 * スコア係数です。
	 */
	private ScoreFactorDO scoreFactor;

	/**
	 * 一括更新用バッファです。
	 */
	private List<ProductMasterDO> listProductMasterDO;
	
	/**
	 * 一括更新サイズです。
	 */
	private static final int BULK_SIZE = 100;
	
	/**
	 * デフォルトコンストラクタです。
	 */
	public ProductMasterCountRecordWriter() {
	}

	/**
	 * コンストラクタです。
	 * @param version バージョン
	 * @param hBaseOperations HBaseアクセサ
	 * @param solrOperations Solrアクセサ
	 * @param timestampHolder タイムスタンプホルダー
	 * @param updateProperties 更新対象プロパティリスト
	 * @param scoreFactor スコア係数
	 */
	public ProductMasterCountRecordWriter(
			Integer version,
			HBaseOperations hBaseOperations,
			SolrOperations solrOperations,
			TimestampHolder timestampHolder,
			String updateProperties,
			ScoreFactorDO scoreFactor) {
		this.version = version;
		this.hBaseOperations = hBaseOperations;
		this.solrOperations = solrOperations;
		this.timestampHolder = timestampHolder;
		this.updateProperties = updateProperties;
		this.scoreFactor = scoreFactor;
		this.listProductMasterDO = Lists.newArrayList();
	}
	
	/**
	 * 結果を書き込みます。
	 * @param key キー
	 * @param value 値
	 * @throws IOException 入出力例外が発生した場合
	 * @throws InterruptedException 中断例外が発生した場合
	 */
	@Override
	public void write(Text key, ProductMasterCountData value) throws IOException,
			InterruptedException {
		String[] keyParts = key.toString().split("\t");
		String sku = keyParts[0];
		String communityUserId = keyParts[1];
		String productMasterId = IdUtil.createIdByConcatIds(
				IdUtil.formatVersion(version), sku, communityUserId);
		ProductMasterDO productMaster = hBaseOperations.load(
				ProductMasterDO.class, productMasterId);
		if (productMaster == null) {
			productMaster = new ProductMasterDO();
			productMaster.setProductMasterId(productMasterId);
			productMaster.setVersion(version);
			productMaster.setProduct(new ProductDO());
			productMaster.getProduct().setSku(sku);
			productMaster.setCommunityUser(new CommunityUserDO());
			productMaster.getCommunityUser().setCommunityUserId(communityUserId);
			productMaster.setRegisterDateTime(timestampHolder.getTimestamp());
		}
		productMaster.setModifyDateTime(timestampHolder.getTimestamp());
		for (String updateProperty : updateProperties.split(",")) {
			try {
				BeanUtils.setProperty(productMaster, updateProperty.trim(),
						BeanUtils.getProperty(value, updateProperty.trim()));
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
		productMaster.calcScore(scoreFactor);
		
		listProductMasterDO.add(productMaster);
		if (listProductMasterDO.size() > BULK_SIZE) {
			flush();
		}		
	}
	
	/**
	 * DBを更新します。
	 */
	public void flush() {
		hBaseOperations.save(ProductMasterDO.class, listProductMasterDO,
				Path.includeProp("productMasterId,version,productMasterScore," +
						"registerDateTime,modifyDateTime,productId,communityUserId," +
						updateProperties));
		if (solrOperations != null) {
			solrOperations.save(ProductMasterDO.class, listProductMasterDO);
			solrOperations.commit(ProductMasterDO.class);
		}
		listProductMasterDO.clear();
	}

	/**
	 * クローズ処理を実行します。
	 * @param context コンテキスト
	 * @throws IOException 入出力例外が発生した場合
	 * @throws InterruptedException 中断例外が発生した場合
	 */
	@Override
	public void close(TaskAttemptContext context) throws IOException,
			InterruptedException {
		if (listProductMasterDO.size() > 0) {
			flush();
		}		
		if (solrOperations != null) {
			solrOperations.optimize(ProductMasterDO.class);
		}
	}

	/**
	 * 商品マスターのカウント情報です。
	 * @author kamiike
	 *
	 */
	public static class ProductMasterCountData implements Writable {

		/**
		 * レビュー投稿件数です。
		 */
		private long reviewPostCount;

		/**
		 * レビュー閲覧件数です。
		 */
		private long reviewShowCount;

		/**
		 * レビューいいね獲得件数です。
		 */
		private long reviewLikeCount;

		/**
		 * Q&A回答数です。
		 */
		private long answerPostCount;

		/**
		 * Q%A回答いいね獲得数です。
		 */
		private long answerLikeCount;

		/**
		 * 画像投稿数です。
		 */
		private long imagePostCount;

		/**
		 * 画像いいね獲得数です。
		 */
		private long imageLikeCount;

		/**
		 * データを書き込みます。
		 * @param out データ
		 * @throws IOException 入出力例外が発生した場合
		 */
		@Override
		public void write(DataOutput out) throws IOException {
			out.writeLong(reviewPostCount);
			out.writeLong(reviewShowCount);
			out.writeLong(reviewLikeCount);
			out.writeLong(answerPostCount);
			out.writeLong(answerLikeCount);
			out.writeLong(imagePostCount);
			out.writeLong(imageLikeCount);
		}

		/**
		 * データを読み込みます。
		 * @param in データ
		 * @throws IOException 入出力例外が発生した場合
		 */
		@Override
		public void readFields(DataInput in) throws IOException {
			reviewPostCount = in.readLong();
			reviewShowCount = in.readLong();
			reviewLikeCount = in.readLong();
			answerPostCount = in.readLong();
			answerLikeCount = in.readLong();
			imagePostCount = in.readLong();
			imageLikeCount = in.readLong();
		}

		/**
		 * @return reviewPostCount
		 */
		public long getReviewPostCount() {
			return reviewPostCount;
		}

		/**
		 * @param reviewPostCount セットする reviewPostCount
		 */
		public void setReviewPostCount(long reviewPostCount) {
			this.reviewPostCount = reviewPostCount;
		}

		/**
		 * @return reviewShowCount
		 */
		public long getReviewShowCount() {
			return reviewShowCount;
		}

		/**
		 * @param reviewShowCount セットする reviewShowCount
		 */
		public void setReviewShowCount(long reviewShowCount) {
			this.reviewShowCount = reviewShowCount;
		}

		/**
		 * @return reviewLikeCount
		 */
		public long getReviewLikeCount() {
			return reviewLikeCount;
		}

		/**
		 * @param reviewLikeCount セットする reviewLikeCount
		 */
		public void setReviewLikeCount(long reviewLikeCount) {
			this.reviewLikeCount = reviewLikeCount;
		}

		/**
		 * @return answerPostCount
		 */
		public long getAnswerPostCount() {
			return answerPostCount;
		}

		/**
		 * @param answerPostCount セットする answerPostCount
		 */
		public void setAnswerPostCount(long answerPostCount) {
			this.answerPostCount = answerPostCount;
		}

		/**
		 * @return answerLikeCount
		 */
		public long getAnswerLikeCount() {
			return answerLikeCount;
		}

		/**
		 * @param answerLikeCount セットする answerLikeCount
		 */
		public void setAnswerLikeCount(long answerLikeCount) {
			this.answerLikeCount = answerLikeCount;
		}

		/**
		 * @return imagePostCount
		 */
		public long getImagePostCount() {
			return imagePostCount;
		}

		/**
		 * @param imagePostCount セットする imagePostCount
		 */
		public void setImagePostCount(long imagePostCount) {
			this.imagePostCount = imagePostCount;
		}

		/**
		 * @return imageLikeCount
		 */
		public long getImageLikeCount() {
			return imageLikeCount;
		}

		/**
		 * @param imageLikeCount セットする imageLikeCount
		 */
		public void setImageLikeCount(long imageLikeCount) {
			this.imageLikeCount = imageLikeCount;
		}
	}

}
