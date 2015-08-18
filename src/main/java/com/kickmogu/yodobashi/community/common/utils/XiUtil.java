/**
 *
 */
package com.kickmogu.yodobashi.community.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.kickmogu.lib.core.domain.CommonInputIF;
import com.kickmogu.lib.core.domain.CommonReturnIF;
import com.kickmogu.yodobashi.community.common.exception.XiAccessException;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;

/**
 * XI通信処理のヘルパーです。
 * @author kamiike
 *
 */
public class XiUtil {

	/**
	 * トレースIDの最大長です。
	 */
	public static final int MAX_TRACEID_LENGTH = 128;

	/**
	 * コンストラクタです。
	 */
	private XiUtil() {
	}

	/**
	 * 共通入力を埋めます。
	 * @param interfaceClass インターフェースクラス
	 * @param commonInput 共通入力情報
	 */
	public static void fillCommonInput(Class<?> interfaceClass, CommonInputIF commonInput) {
		commonInput.setExternalSystem(ResourceConfig.INSTANCE.externalSystemId);
		commonInput.setObjectKey("");
		commonInput.setTerminalID(ResourceConfig.INSTANCE.hostname.split("\\.")[0]);
		commonInput.setTraceID(createTraceId(interfaceClass.getSimpleName()));
	}

	/**
	 * レスポンス結果をチェックし、エラーの場合、例外をスローします。
	 * @param commonReturn 共通出力情報
	 */
	public static void checkResponse(CommonReturnIF commonReturn) {
		if (!commonReturn.getReturnComHeader().isStatusFlg()) {
			throw new XiAccessException(
					commonReturn.getReturnComHeader(),
					commonReturn.getReturnComDetail());
		}
	}

	/**
	 * トレースIDを生成して返します。
	 * @param methodName サービス名
	 * @return
	 */
	private static String createTraceId(String methodName) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.JAPAN);
		String id = formatter.format(new Date()).replaceFirst(" ", "T");

		String hostname = ResourceConfig.INSTANCE.hostname.split("\\.")[0];

		// 長いときはホスト名を短くする
		int clen = id.length() + methodName.length() + "@#".length();
		if (clen > MAX_TRACEID_LENGTH) {
			int len = clen - MAX_TRACEID_LENGTH;
			if (len > hostname.length()) hostname = "";
			else hostname = hostname.substring(0, len);
		}
		String traceId = id + "@" + hostname + "#" + methodName;

		// URL等が長すぎると超えるので、さらに制限する。
		clen = traceId.length();
		if (clen > MAX_TRACEID_LENGTH) traceId = traceId.substring(0, MAX_TRACEID_LENGTH);
		return traceId;
	}
}
