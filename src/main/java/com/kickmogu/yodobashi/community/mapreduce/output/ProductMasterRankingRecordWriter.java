/**
 *
 */
package com.kickmogu.yodobashi.community.mapreduce.output;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.mapreduce.output.ProductMasterRankingRecordWriter.ProductMasterRankingData;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;

/**
 * 商品マスターのランキングレコードライターです。
 * @author kamiike
 *
 */
public class ProductMasterRankingRecordWriter extends RecordWriter<Text, ProductMasterRankingData> {

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
	public ProductMasterRankingRecordWriter() {
	}

	/**
	 * コンストラクタです。
	 * @param version バージョン
	 * @param hBaseOperations HBaseアクセサ
	 * @param solrOperations Solrアクセサ
	 * @param timestampHolder タイムスタンプホルダー
	 */
	public ProductMasterRankingRecordWriter(
			Integer version,
			HBaseOperations hBaseOperations,
			SolrOperations solrOperations,
			TimestampHolder timestampHolder) {
		this.version = version;
		this.hBaseOperations = hBaseOperations;
		this.solrOperations = solrOperations;
		this.timestampHolder = timestampHolder;
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
	public void write(Text key, ProductMasterRankingData value) throws IOException,
			InterruptedException {
		String[] keyParts = key.toString().split("\t");
		String sku = keyParts[0];
		String communityUserId = keyParts[1];
		String productMasterId = IdUtil.createIdByConcatIds(
				IdUtil.formatVersion(version), sku, communityUserId);
		if (value.isDeleteFlag()) {
			ProductMasterDO productMaster = new ProductMasterDO();
			productMaster.setProductMasterId(productMasterId);
			productMaster.setModifyDateTime(timestampHolder.getTimestamp());
			productMaster.setDeleteDate(timestampHolder.getTimestamp());
			productMaster.setDeleteFlag(true);
			hBaseOperations.save(productMaster, Path.includeProp("modifyDateTime,deleteDate,deleteFlag"));
			solrOperations.deleteByKey(ProductMasterDO.class, productMasterId);
			return;
		}
		ProductMasterDO productMaster = hBaseOperations.load(
				ProductMasterDO.class, productMasterId);
		productMaster.setRank(value.getRank());
		Integer rankInVersion = null;
		if (version > 1) {
			ProductMasterDO preInfo = hBaseOperations.load(
					ProductMasterDO.class, IdUtil.createIdByConcatIds(
							IdUtil.formatVersion(version - 1), sku, communityUserId));
			if (preInfo != null && !preInfo.isDeleted()) {
				rankInVersion = preInfo.getRankInVersion();
				if (preInfo.getRank() > productMaster.getRank()
						&& productMaster.getRank() <= ProductMasterDO.RANK_RANGE) {
					productMaster.setRequiredNotify(true);
				}
			} else {
				if (productMaster.getRank() <= ProductMasterDO.RANK_RANGE) {
					productMaster.setRequiredNotify(true);
				}
			}
		} else {
			if (productMaster.getRank() <= ProductMasterDO.RANK_RANGE) {
				productMaster.setRequiredNotify(true);
			}
		}
		if (rankInVersion == null && productMaster.getRank() <= ProductMasterDO.RANK_RANGE) {
			rankInVersion = version;
		} else if (productMaster.getRank() > ProductMasterDO.RANK_RANGE) {
			rankInVersion = null;
		}
		productMaster.setRankInVersion(rankInVersion);
		productMaster.setPurchaseDate(value.getPurchaseDate());
		productMaster.setAdult(value.isAdult());
		productMaster.setModifyDateTime(timestampHolder.getTimestamp());
		
		listProductMasterDO.add(productMaster);
		if (listProductMasterDO.size() > BULK_SIZE) {
			flush();
		}
	}
	
	/**
	 * DBを更新します。
	 */
	public void flush() {
		hBaseOperations.save(ProductMasterDO.class, listProductMasterDO);
		solrOperations.save(ProductMasterDO.class, listProductMasterDO);
		listProductMasterDO.clear();
		
		solrOperations.commit(ProductMasterDO.class);
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
		solrOperations.optimize(ProductMasterDO.class);
	}

	/**
	 * 商品マスターのランキング情報です。
	 * @author kamiike
	 *
	 */
	public static class ProductMasterRankingData implements Writable {

		/**
		 * ランクです。
		 */
		private int rank;

		/**
		 * 商品購入日です。
		 */
		private Date purchaseDate;

		/**
		 * アダルト商品かどうかです。
		 */
		private boolean adult;

		/**
		 * 削除されたかどうかです。
		 */
		private boolean deleteFlag;

		/**
		 * データを書き込みます。
		 * @param out データ
		 * @throws IOException 入出力例外が発生した場合
		 */
		@Override
		public void write(DataOutput out) throws IOException {
			out.writeInt(rank);
			out.writeLong(purchaseDate.getTime());
			out.writeBoolean(adult);
			out.writeBoolean(deleteFlag);
		}

		/**
		 * データを読み込みます。
		 * @param in データ
		 * @throws IOException 入出力例外が発生した場合
		 */
		@Override
		public void readFields(DataInput in) throws IOException {
			rank = in.readInt();
			purchaseDate = new Date(in.readLong());
			adult = in.readBoolean();
			deleteFlag = in.readBoolean();
		}

		/**
		 * @return rank
		 */
		public int getRank() {
			return rank;
		}

		/**
		 * @param rank セットする rank
		 */
		public void setRank(int rank) {
			this.rank = rank;
		}

		/**
		 * @return purchaseDate
		 */
		public Date getPurchaseDate() {
			return purchaseDate;
		}

		/**
		 * @param purchaseDate セットする purchaseDate
		 */
		public void setPurchaseDate(Date purchaseDate) {
			this.purchaseDate = purchaseDate;
		}

		/**
		 * @return adult
		 */
		public boolean isAdult() {
			return adult;
		}

		/**
		 * @param adult セットする adult
		 */
		public void setAdult(boolean adult) {
			this.adult = adult;
		}

		/**
		 * @return deleteFlag
		 */
		public boolean isDeleteFlag() {
			return deleteFlag;
		}

		/**
		 * @param deleteFlag セットする deleteFlag
		 */
		public void setDeleteFlag(boolean deleteFlag) {
			this.deleteFlag = deleteFlag;
		}
	}
}
