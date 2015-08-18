/**
 *
 */
package com.kickmogu.yodobashi.community.mapreduce.output;

import java.io.IOException;

import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;


/**
 * シンプルな出力フォーマットです。
 * @author kamiike
 *
 */
public class SimpleOutputFormat<K, V> extends OutputFormat<K, V> {

	/**
	 * レコードライターです。
	 */
	private RecordWriter<K, V> writer;

	/**
	 * コンストラクタです。
	 * @param writer レコードライター
	 */
	public SimpleOutputFormat(
			RecordWriter<K, V> writer) {
		this.writer = writer;
	}

	/**
	 * レコードライターを返します。
	 * @param context コンテキスト
	 * @return レコードライター
	 * @throws IOException 入出力例外が発生した場合
	 * @throws InterruptedException 中断例外が発生した場合
	 */
	@Override
	public RecordWriter<K, V> getRecordWriter(
			TaskAttemptContext context) throws IOException,
			InterruptedException {
		return RecordWriterProxyWrapper.getRecordWriter(
				context.getConfiguration(), writer);
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
}
