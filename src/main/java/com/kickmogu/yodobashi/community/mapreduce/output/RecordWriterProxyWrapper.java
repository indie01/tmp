/**
 *
 */
package com.kickmogu.yodobashi.community.mapreduce.output;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.springframework.hadoop.context.HadoopApplicationContextUtils;

/**
 * レコードライタープロキシのラッパークラスです。
 * @author kamiike
 *
 */
public class RecordWriterProxyWrapper<K, V> extends RecordWriter<K, V> {

	/**
	 * プロキシです。
	 */
	private RecordWriterProxy<K, V> proxy;

	/**
	 * プロキシを設定します。
	 * @param proxy プロキシ
	 */
	public RecordWriterProxyWrapper(RecordWriterProxy<K, V> proxy) {
		this.proxy = proxy;
	}

	/**
	 * レコードライターを返します。
	 * @param configuration コンフィグ
	 * @param delegate 委譲先
	 * @return レコードライター
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> RecordWriter<K, V> getRecordWriter(
			Configuration configuration,
			RecordWriter<K, V> delegate) {
		RecordWriterProxy<K, V> proxy = HadoopApplicationContextUtils.getBean(
				configuration, RecordWriterProxy.class);
		proxy.setDelegate(delegate);
		return new RecordWriterProxyWrapper<K, V>(proxy);
	}

	/**
	 * 結果を書き込みます。
	 * @param key キー
	 * @param value 値
	 * @throws IOException 入出力例外が発生した場合
	 * @throws InterruptedException 中断例外が発生した場合
	 */
	@Override
	public void write(
			K key, V value) throws IOException,
			InterruptedException {
		proxy.write(key, value);
	}

	/**
	 * クローズ処理を実行します。
	 * @param context コンテキスト
	 * @throws IOException 入出力例外が発生した場合
	 * @throws InterruptedException 中断例外が発生した場合
	 */
	@Override
	public void close(
			TaskAttemptContext context) throws IOException,
			InterruptedException {
		proxy.close(context);
	}

}
