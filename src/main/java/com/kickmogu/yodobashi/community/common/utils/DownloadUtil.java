package com.kickmogu.yodobashi.community.common.utils;


import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * ダウンロード処理を支援する API です。
 * @author kamiike
 */
public class DownloadUtil {

	/**
	 * コンストラクタをカプセル化します。
	 */
	private DownloadUtil() {
	}

	/**
	 * ダウンロードファイル名をヘッダーに追加します。
	 * @param request リクエスト
	 * @param response レスポンス
	 * @param fileName ファイル名
	 */
	public static void setFileNameHeader(
			HttpServletRequest request,
			HttpServletResponse response,
			String fileName) {
		setFileNameHeader(request, response, fileName, null);
	}

	/**
	 * ダウンロードファイル名をヘッダーに追加します。
	 * @param request リクエスト
	 * @param response レスポンス
	 * @param fileName ファイル名
	 * @param timestampFormat タイムスタンプフォーマット
	 */
	public static void setFileNameHeader(
			HttpServletRequest request,
			HttpServletResponse response,
			String fileName,
			String timestampFormat) {
		String clientBrowser = request.getHeader("User-Agent");
		boolean isMSEncoding = false;
		if (clientBrowser != null && clientBrowser.indexOf("MSIE") != -1) {
			isMSEncoding = true;
		}

		if (timestampFormat != null) {
			fileName = convertFileNameWithTimestamp(fileName, timestampFormat);
		}

		StringBuilder header = new StringBuilder();
		header.append("attachment; filename=\"");
		try {
			if (isMSEncoding) {
				header.append(java.net.URLEncoder.encode(fileName, "UTF-8"));
			} else {
				header.append(MimeUtility.encodeWord(
						fileName, "ISO-2022-JP", "B"));
			}
		} catch (UnsupportedEncodingException e) {
		}
		header.append("\"");

		response.setHeader("Content-Disposition", header.toString());
	}

	/**
	 * ファイル名にタイムスタンプを付加します。
	 * @param fileName ファイル名
	 * @param timestampFormat タイムスタンプ
	 * @return タイムスタンプを付加したファイル名
	 */
	private static String convertFileNameWithTimestamp(
			String fileName,
			String timestampFormat) {
		int extIndex = fileName.lastIndexOf(".");
		if (extIndex != -1) {
			String nameBody = fileName.substring(0, extIndex);
			String extParts = fileName.substring(
					extIndex, fileName.length());
			SimpleDateFormat formatter
					= new SimpleDateFormat(timestampFormat);
			fileName = nameBody + formatter.format(new Date()) + extParts;
		}
		return fileName;
	}
}
