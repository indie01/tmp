/**
 *
 */
package com.kickmogu.yodobashi.community.mapreduce.output;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import com.google.common.collect.Lists;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.domain.UniqueUserViewCountDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.UniqueUserViewCountType;


/**
 * ユニークユーザー閲覧数に出力するためのフォーマットです。
 * @author kamiike
 *
 */
public class UniqueUserViewCountOutputFormat extends OutputFormat<Text, LongWritable> {

	/**
	 * 時間文字列を受け取るためのキー名です。
	 */
	public static final String KEY_TIME_STRING = UniqueUserViewCountOutputFormat.class.getName().toString() + ".time";

	/**
	 * HBaseアクセサです。
	 */
	private HBaseOperations hBaseOperations;

	/**
	 * Solrアクセサです。
	 */
	private SolrOperations solrOperations;

	/**
	 * 日時フォーマッターです。
	 */
	private String timeFormatter;

	/**
	 * コンストラクタです。
	 * @param hBaseOperations HBaseアクセサ
	 * @param solrOperations Solrアクセサ
	 * @param timeFormatter フォーマッター
	 */
	public UniqueUserViewCountOutputFormat(
			HBaseOperations hBaseOperations,
			SolrOperations solrOperations,
			String timeFormatter) {
		this.hBaseOperations = hBaseOperations;
		this.solrOperations = solrOperations;
		this.timeFormatter = timeFormatter;
	}

	/**
	 * 統計レコードライターを返します。
	 * @param context コンテキスト
	 * @return 統計レコードライター
	 * @throws IOException 入出力例外が発生した場合
	 * @throws InterruptedException 中断例外が発生した場合
	 */
	@Override
	public RecordWriter<Text, LongWritable> getRecordWriter(
			TaskAttemptContext context) throws IOException,
			InterruptedException {
		String timeString = context.getConfiguration().get(KEY_TIME_STRING);
		try {
			return RecordWriterProxyWrapper.getRecordWriter(
					context.getConfiguration(),
					new UniqueUserViewCountRecordWriter(
					hBaseOperations, solrOperations,
					new SimpleDateFormat(
							timeFormatter).parse(timeString), timeString));
		} catch (ParseException e) {
			throw new IllegalStateException(
					"TimeString is invalid. formatter = "
					+ timeFormatter + ", timeString = " + timeString);
		}
	}

	/**
	 * 出力に必要な情報をチェックします。
	 * @param context コンテキスト
	 * @throws IOException 入出力例外が発生した場合
	 * @throws InterruptedException 中断例外が発生した場合
	 */
	@Override
	public void checkOutputSpecs(JobContext context) throws IOException,
			InterruptedException {
		String timeString = context.getConfiguration().get(KEY_TIME_STRING);
		if (timeString == null) {
			throw new IllegalStateException(
					"TimeString is not found. key = " + KEY_TIME_STRING);
		}
		try {
			new SimpleDateFormat(timeFormatter).parse(timeString);
		} catch (ParseException e) {
			throw new IllegalStateException(
					"TimeString is invalid. formatter = "
					+ timeFormatter + ", timeString = " + timeString);
		}
	}

	/**
	 * 出力コミッターを返します。
	 * @param context コンテキスト
	 * @return 出力コミッター
	 * @throws IOException 入出力例外が発生した場合
	 * @throws InterruptedException 中断例外が発生した場合
	 */
	@Override
	public OutputCommitter getOutputCommitter(TaskAttemptContext context)
			throws IOException, InterruptedException {
	    return new OutputCommitter() {
	    	public void abortTask(TaskAttemptContext taskContext) { }
	    	public void cleanupJob(JobContext jobContext) { }
	        public void commitJob(JobContext jobContext) { }
	        public void commitTask(TaskAttemptContext taskContext) { }
	        public boolean needsTaskCommit(TaskAttemptContext taskContext) {
	        	return false;
	        }
	        public void setupJob(JobContext jobContext) { }
	        public void setupTask(TaskAttemptContext taskContext) { }
	    };
	}

	/**
	 * ユニークユーザー閲覧数レコードライターです。
	 * @author kamiike
	 *
	 */
	public class UniqueUserViewCountRecordWriter extends RecordWriter<Text, LongWritable> {

		/**
		 * HBaseアクセサです。
		 */
		private HBaseOperations hBaseOperations;

		/**
		 * Solrアクセサです。
		 */
		private SolrOperations solrOperations;

		/**
		 * 対象日時です。
		 */
		private Date targetTime;

		/**
		 * 対象日時文字列です。
		 */
		private String targetTimeString;
		
		/**
		 * 一括更新用バッファです。
		 */
		private List<UniqueUserViewCountDO> listUniqueUserViewCountDO;
		
		/**
		 * 一括更新サイズです。
		 */
		private static final int BULK_SIZE = 100;

		/**
		 * コンストラクタです。
		 * @param hBaseOperations HBaseアクセサ
		 * @param solrOperations Solrアクセサ
		 * @param targetTime 対象日時
		 * @param targetTimeString 対象日時文字列
		 */
		public UniqueUserViewCountRecordWriter(
				HBaseOperations hBaseOperations,
				SolrOperations solrOperations,
				Date targetTime,
				String targetTimeString) {
			this.hBaseOperations = hBaseOperations;
			this.solrOperations = solrOperations;
			this.targetTime = targetTime;
			this.targetTimeString = targetTimeString;
			this.listUniqueUserViewCountDO = Lists.newArrayList();
		}

		/**
		 * 結果を書き込みます。
		 * @param key キー
		 * @param value 値
		 * @throws IOException 入出力例外が発生した場合
		 * @throws InterruptedException 中断例外が発生した場合
		 */
		@Override
		@ArroundSolr
		@ArroundHBase
		public void write(Text key, LongWritable value) throws IOException,
				InterruptedException {
			String[] keyParts = key.toString().split("\t");
			String type = keyParts[0];
			String contentsId = keyParts[1];
			String sku = keyParts[2];
			String communityUserId = keyParts[3];
			UniqueUserViewCountDO uuViewCount = new UniqueUserViewCountDO();
			uuViewCount.setUniqueUserViewCountId(
					IdUtil.createIdByConcatIds(
							contentsId, type,
							 targetTimeString));
			uuViewCount.setType(UniqueUserViewCountType.codeOf(type));
			uuViewCount.setContentsId(contentsId);
			uuViewCount.setTargetTime(targetTime);
			uuViewCount.setSku(sku);
			uuViewCount.setCommunityUserId(communityUserId);
			uuViewCount.setViewCount(value.get());
			
			listUniqueUserViewCountDO.add(uuViewCount);
			if (listUniqueUserViewCountDO.size() > BULK_SIZE) {
				flush();
			}
		}
		
		/**
		 * DBを更新します。
		 */
		public void flush() {
			hBaseOperations.save(UniqueUserViewCountDO.class, listUniqueUserViewCountDO);
			solrOperations.save(UniqueUserViewCountDO.class, listUniqueUserViewCountDO);
			listUniqueUserViewCountDO.clear();
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
			if (listUniqueUserViewCountDO.size() > 0) {
				flush();
			}
		}

	}
}
