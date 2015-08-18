/**
 *
 */
package com.kickmogu.yodobashi.community.mapreduce.output;

import java.io.IOException;

import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 * レコードライタープロキシです。
 * @author kamiike
 */
public interface RecordWriterProxy<K, V> {

	/**
	 * 委譲先を設定します。
	 * @param delegate 委譲先
	 */
	public void setDelegate(RecordWriter<K, V> delegate);

	/**
	 * 結果を書き込みます。
	 * @param key キー
	 * @param value 値
	 * @throws IOException 入出力例外が発生した場合
	 * @throws InterruptedException 中断例外が発生した場合
	 */
	public void write(K key, V value) throws IOException,
			InterruptedException;

	/**
	 * クローズ処理を実行します。
	 * @param context コンテキスト
	 * @throws IOException 入出力例外が発生した場合
	 * @throws InterruptedException 中断例外が発生した場合
	 */
	public void close(TaskAttemptContext context) throws IOException,
			InterruptedException;

}
