/**
 *
 */
package com.kickmogu.yodobashi.community.mapreduce.output;

import java.io.IOException;

import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.annotation.ArroundSolr;

/**
 * レコードライタープロキシの実装です。
 * @author kamiike
 *
 */
@Service
public class RecordWriterProxyImpl<K, V> implements RecordWriterProxy<K, V> {

	/**
	 * プールです。
	 */
	private ThreadLocal<RecordWriter<K, V>> pool = new ThreadLocal<RecordWriter<K, V>>();

	/**
	 * 委譲先を設定します。
	 * @param delegate 委譲先
	 */
	@Override
	public void setDelegate(RecordWriter<K, V> delegate) {
		pool.set(delegate);
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
	public void write(
			K key, V value) throws IOException,
			InterruptedException {
		pool.get().write(key, value);
	}

	/**
	 * クローズ処理を実行します。
	 * @param context コンテキスト
	 * @throws IOException 入出力例外が発生した場合
	 * @throws InterruptedException 中断例外が発生した場合
	 */
	@Override
	@ArroundSolr
	@ArroundHBase
	public void close(
			TaskAttemptContext context) throws IOException,
			InterruptedException {
		pool.get().close(context);
	}
}
