/**
 *
 */
package com.kickmogu.yodobashi.community.mapreduce.input;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.springframework.beans.BeanUtils;

import com.kickmogu.lib.core.domain.SearchResult;
import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.lib.core.utils.AnnotationUtil;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.lib.solr.annotation.SolrUniqKey;
import com.kickmogu.yodobashi.community.mapreduce.job.CommunityMapReduceConfig;

/**
 * Solrを検索して、データを読み込みます。
 * @author kamiike
 */
public class SolrInputFormat<K, V> extends InputFormat<K, V> {

	/**
	 * Solrアクセサです。
	 */
	private SolrOperations solrOperations;

	/**
	 * 検索クエリです。
	 */
	private String queryString;

	/**
	 * 取得条件です。
	 */
	private Condition condition;

	/**
	 * 検索するデータタイプです。
	 */
	private Class<V> type;

	/**
	 * ソートフィールドです。
	 */
	private String sortField;
	
	/**
	 * handlerです。
	 */
	private SolrInputFormatHandler<V> handler = null;

	/**
	 * コンストラクタです。
	 * @param solrOperations Solrアクセサ
	 * @param queryString 検索クエリ
	 * @param condition 取得条件
	 * @param type 検索するデータタイプ
	 * @param sortField ソートフィールド
	 */
	public SolrInputFormat(
			SolrOperations solrOperations,
			String queryString,
			Condition condition,
			Class<V> type,
			String sortField) {
		this(solrOperations, queryString, condition, type, sortField, null);
	}

	/**
	 * コンストラクタです。
	 * @param solrOperations Solrアクセサ
	 * @param queryString 検索クエリ
	 * @param condition 取得条件
	 * @param type 検索するデータタイプ
	 * @param sortField ソートフィールド
	 * @param handler ハンドラ
	 */
	public SolrInputFormat(
			SolrOperations solrOperations,
			String queryString,
			Condition condition,
			Class<V> type,
			String sortField,
			SolrInputFormatHandler<V> handler) {
		this.solrOperations = solrOperations;
		this.queryString = queryString;
		this.condition = condition;
		this.type = type;
		this.sortField = sortField;
		this.handler = handler;
	}
	
	/**
	 * レコードリーダーを生成して返します。
	 * @param split スプリット
	 * @param context コンテキスト
	 * @throws IOException 入出力例外が発生した場合
	 * @throws InterruptedException 中断例外が発生した場合
	 */
	public RecordReader<K, V> createRecordReader(
			InputSplit split,
			TaskAttemptContext context) throws IOException, InterruptedException {
		return new SolrRecordReader(queryString, condition, (SolrInputSplit) split, type, handler);
	}

	/**
	 * 必要なスプリットを返します。
	 * @param job ジョブコンテキスト
	 * @return スプリットリスト
	 * @throws IOException 入出力例外が発生した場合
	 */
	public List<InputSplit> getSplits(JobContext job) throws IOException {

		solrOperations.commit(type);

		SolrQuery solrQuery = new SolrQuery(queryString);
		solrQuery.setRows(0);

		SearchResult<V> searchResult = solrOperations.findByQuery(solrQuery, type);

		long count = searchResult.getNumFound();
		System.out.println("SolrInputFormat Total Job Count:" + count + " type:" + type.getName());
		int chunks = job.getConfiguration().getInt(CommunityMapReduceConfig.INSTANCE.mapTaskNumKey, 1);
		long chunkSize = (count / chunks);
		if (count < chunks) {
			chunkSize = 1;
		}

		List<InputSplit> splits = new ArrayList<InputSplit>();

		for (int i = 0; i < chunks; i++) {
			if (count < chunks && i >= count) {
				break;
			}

			SolrInputSplit split;

			if ((i + 1) == chunks) {
				split = new SolrInputSplit(i * chunkSize, count);
			} else {
				split = new SolrInputSplit(
						i * chunkSize, (i * chunkSize) + chunkSize);
			}

			splits.add(split);
		}

		return splits;
	}

	/**
	 * Solrのレコードリーダーです。
	 * @author kamiike
	 */
	public class SolrRecordReader extends RecordReader<K, V> {

		/**
		 * 一度の最大取得件数です。
		 */
		private int max = 500; // 本番環境向けパフォーマンス向上用

		/**
		 * 分割条件です。
		 */
		private SolrInputSplit split;

		/**
		 * 検索クエリです。
		 */
		private SolrQuery solrQuery;

		/**
		 * 取得条件です。
		 */
		private Condition condition;

		/**
		 * 検索するデータタイプです。
		 */
		private Class<V> type;

		/**
		 * 検索結果です。
		 */
		private SearchResult<V> searchResult;

		/**
		 * 現在位置です。
		 */
		private int pos = 0;

		/**
		 * 現在位置（都度検索分）です。
		 */
		private int minPos = 0;

		/**
		 * 読み込んだ現在行のデータのキーです。
		 */
		private K key;

		/**
		 * 読み込んだ現在行のデータです。
		 */
		private V value;

		/**
		 * ゲッターです。
		 */
		private Method getter;
		
		/**
		 * handlerです。
		 */
		private SolrInputFormatHandler<V> handler = null;

		/**
		 * コンストラクタです。
		 * @param queryString 検索クエリ
		 * @param condition 取得条件
		 * @param split 分割条件
		 * @param type 検索するデータタイプ
		 * @param sortField ソートフィールド
		 */
		public SolrRecordReader(String queryString,
				Condition condition,
				SolrInputSplit split,
				Class<V> type) {
			this(queryString, condition, split, type, null);
		}
		/**
		 * コンストラクタです。
		 * @param queryString 検索クエリ
		 * @param condition 取得条件
		 * @param split 分割条件
		 * @param type 検索するデータタイプ
		 * @param sortField ソートフィールド
		 * @param handler ハンドラ
		 */
		public SolrRecordReader(String queryString,
				Condition condition,
				SolrInputSplit split,
				Class<V> type,
				SolrInputFormatHandler<V> handler) {
			this.solrQuery = new SolrQuery(queryString);
			this.condition = condition;
			this.split = split;
			this.type = type;
			this.solrQuery.addSortField(sortField, ORDER.asc);
			this.handler = handler;
			for (Field field:AnnotationUtil.getAnnotatedFields(SolrUniqKey.class, type)) {
				getter = BeanUtils.getPropertyDescriptor(type, field.getName()).getReadMethod();
			}
			if (getter == null) {
				throw new IllegalArgumentException();
			}
			System.out.println("SolrRecordReader start:" + split.getStart() + " end:" + split.getEnd() + " type:" + type.getName());
		}

		/**
		 * 初期化処理を行います。
		 * @param split 分割条件
		 * @param context コンテキスト
		 * @throws IOException 入出力例外が発生した場合
		 * @throws InterruptedException 中断例外が発生した場合
		 */
		public void initialize(
				InputSplit split,
				TaskAttemptContext context
				) throws IOException, InterruptedException {
		}

		/**
		 * 次のキー情報を読み込みます。
		 * @return 次の行を読み込めた場合、true
		 * @throws IOException 入出力例外が発生した場合
		 * @throws InterruptedException 中断例外が発生した場合
		 */
		@SuppressWarnings("unchecked")
		public boolean nextKeyValue() throws IOException, InterruptedException {
			if (pos >= split.getLength()) {
				return false;
			}
			if (searchResult == null || minPos >= searchResult.getDocuments().size()) {
				int limit = (int) (split.getLength() - pos);
				if (limit > max) {
					limit = max;
				}
				solrQuery.setRows(limit);
				solrQuery.setStart((int) (split.getStart() + pos));
				searchResult = solrOperations.findByQuery(solrQuery, type, condition);
				System.out.println("SolrRecordReader search limit:" + limit + " offset:" + (split.getStart() + pos) + " found:" + searchResult.getDocuments().size() + " type:" + type.getName());
				minPos = 0;
				
				if (handler != null)
					handler.handlePostSolrQuery(searchResult);
			}
			if (searchResult.getDocuments().size() == 0) {
				return false;
			}
			value = searchResult.getDocuments().get(minPos);
			try {
				key = (K) getter.invoke(value);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			pos++;
			minPos++;
			return true;
		}

		/**
		 * 現在読み込んでいる行のキーを返します。
		 * @return 現在読み込んでいる行のキー
		 * @throws IOException 入出力例外が発生した場合
		 * @throws InterruptedException 中断例外が発生した場合
		 */
		public K getCurrentKey() throws IOException, InterruptedException {
			return key;
		}

		/**
		 * 現在読み込んでいる行のデータを返します。
		 * @return 現在読み込んでいる行のデータ
		 * @throws IOException 入出力例外が発生した場合
		 * @throws InterruptedException 中断例外が発生した場合
		 */
		public V getCurrentValue() throws IOException, InterruptedException {
			return value;
		}

		/**
		 * 進捗状況を返します。
		 * @return 進捗状況
		 * @throws IOException 入出力例外が発生した場合
		 * @throws InterruptedException 中断例外が発生した場合
		 */
		public float getProgress() throws IOException, InterruptedException {
			return pos / (float)split.getLength();
		}

		/**
		 * クロースします。
		 * @throws IOException 入出力例外が発生した場合
		 */
		public void close() throws IOException {
			searchResult = null;
		}
	}

	/**
	 * 入力データの分割クラスです。
	 */
	public static class SolrInputSplit extends InputSplit implements Writable {

		/**
		 * 検索開始位置です。
		 */
	    private long start = 0;

	    /**
	     * 検索終了位置です。
	     */
		private long end = 0;

		/**
	     * コンストラクタです。
	     */
		public SolrInputSplit() {
		}

		/**
	     * コンストラクタです。
	     * @param start 検索開始位置
	     * @param end 検索終了位置
	     */
		public SolrInputSplit(long start, long end) {
			this.start = start;
			this.end = end;
		}

	    /**
	     * 場所情報に対応した分割対応情報を返します。
	     * @return 現在は何も返しません。
	     */
	    public String[] getLocations() throws IOException {
	    	return new String[] {};
	    }

	    /**
	     * 検索開始位置を返します。
	     * @return 検索開始位置
	     */
	    public long getStart() {
	    	return start;
	    }

	    /**
	     * 検索終了位置を返します。
	     * @return 検索終了位置
	     */
	    public long getEnd() {
	    	return end;
	    }

	    /**
	     * 担当件数を返します。
	     * @return 担当件数
	     */
	    public long getLength() throws IOException {
	    	return end - start;
	    }

		/**
		 * データを書き込みます。
		 * @param out データ
		 * @throws IOException 入出力例外が発生した場合
		 */
		@Override
		public void write(DataOutput out) throws IOException {
			out.writeLong(start);
			out.writeLong(end);
		}

		/**
		 * データを読み込みます。
		 * @param in データ
		 * @throws IOException 入出力例外が発生した場合
		 */
		@Override
		public void readFields(DataInput in) throws IOException {
	    	start = in.readLong();
	    	end = in.readLong();
		}
	}
}
