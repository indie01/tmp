/**
 *
 */
package com.kickmogu.yodobashi.community.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 非同期ユーティリティです。
 * @author kamiike
 *
 */
public class AsyncUtil {

	/**
	 * コンストラクタです。
	 */
	private AsyncUtil() {
	}

	/**
	 * シリアライズします。
	 * @param params パラメーターリスト
	 * @return データ
	 * @throws Exception
	 */
	public static byte[] serialize(Object[] params) throws IOException {
		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);
		objectOutputStream.writeObject(params);

		return byteOutputStream.toByteArray();
	}

	/**
	 * デシリアライズします。
	 * @param data データ
	 * @return パラメーターリスト
	 * @throws IOException 入出力で例外が発生した場合
	 * @throws ClassNotFoundException クラスが見つからない場合
	 */
	public static Object[] deserialize(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream byteInputStream = new ByteArrayInputStream(data);
		ObjectInputStream objectInputStream = new ObjectInputStream(byteInputStream);
		return  ( Object[]) objectInputStream.readObject();
	}

}
