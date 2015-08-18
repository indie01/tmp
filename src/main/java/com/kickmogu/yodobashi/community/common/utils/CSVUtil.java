package com.kickmogu.yodobashi.community.common.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV（Comma Separated Values）形式のファイルを解析する
 * ユーティリティクラスです。
 * @author kamiike
 */
public class CSVUtil {

	/**
	 * コンストラクタをカプセル化します。
	 */
	private CSVUtil() {
	}

	/**
	 * CSV形式の文字列の予約語をエスケープします。
	 * @param text 文字列
	 * @return CSV形式対応のセキュアな文字列
	 */
	public static String escape(String text) {
		if (text.indexOf(",") != -1 ||
			text.indexOf("\n") != -1 ||
			text.indexOf("\r") != -1 ||
			text.indexOf("\"") != -1) {
			StringBuilder buffer = new StringBuilder();
			buffer.append("\"");
			buffer.append(text.replace("\"", "\"\""));
			buffer.append("\"");
			return buffer.toString();
		} else {
			return text;
		}
	}

	/**
	 * CSVストリームを解析し、結果をStringの配列に格納して返します。<br>
	 * 値がないフィールド（,,）については、
	 * 空文字（長さゼロのStringオブジェクト）が格納されます。<br>
	 * 解析結果がない場合は、長さゼロの配列オブジェクトを返します。
	 * @param inputStream 入力CSVストリーム
	 * @return CSV解析結果
	 * @exception java.io.IOException 入出力に関するエラーが発生した場合
	 */
	public static List<List<String>> parse(InputStream inputStream) throws IOException {
		return parse(inputStream, null);
	}

	/**
	 * CSVストリームを解析し、結果をStringの配列に格納して返します。<br>
	 * 値がないフィールド（,,）については、
	 * 空文字（長さゼロのStringオブジェクト）が格納されます。<br>
	 * 解析結果がない場合は、長さゼロの配列オブジェクトを返します。
	 * @param inputStream 入力CSVストリーム
	 * @param encoding 解析文字コード
	 * @return CSV解析結果
	 * @exception java.io.IOException 入出力に関するエラーが発生した場合
	 */
	public static List<List<String>> parse(InputStream inputStream,
                                   String encoding) throws IOException {

		BufferedReader reader = null;
		if (encoding == null) {
			encoding = "Windows-31J";
		}

		try {
			reader = new BufferedReader(
					new InputStreamReader(inputStream, encoding));
			List<List<String>> rowList = new ArrayList<List<String>>();

			for (String lineString = reader.readLine();
				 lineString != null;
				 lineString = reader.readLine()) {

				List<String> list = new ArrayList<String>();
				while (true) {
					LineData lineData = parseLine(lineString);
					if (lineData != null) {
						for (String data :lineData.getParseData()) {
							list.add(data);
						}
						if (lineData.getRemainder().length() > 0) {
							lineString = reader.readLine();
							if (lineString == null) {
								break;
							} else {
								lineString = lineData.getRemainder() + "\n" + lineString;
							}
						} else {
							break;
						}
					} else {
						break;
					}
				}
				rowList.add(list);
			}
			if (rowList.size() == 0) {
				return new ArrayList<List<String>>();
			} else {
				return rowList;
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	/**
	 * 一行の CSV文字列を解析し、結果をStringの配列に格納して返します。<br>
	 * 値がないフィールド（,,）については、
	 * 空文字（長さゼロのStringオブジェクト）が格納されます。<br>
	 * 解析結果がない場合は、長さゼロの配列オブジェクトを返します。
	 * @param lineString 一行の CSV文字列
	 * @param commentRead コメント読み込みフラグ
	 * @return CSV解析結果
	 */
	public static LineData parseLine(String lineString) {
		ArrayList<String> columnList = new ArrayList<String>();
		boolean roopFlg = true;
		while (roopFlg) {
			boolean stringFlg = lineString.startsWith("\"");
			if (stringFlg) {
				int index = lineString.indexOf("\",", 1);
				if (index == -1) {
					if (lineString.endsWith("\"")) {
						columnList.add(killEscape(
								lineString.substring(1,
										lineString.length() - 1)));
						lineString = "";
					}
					roopFlg = false;
				} else {
					while (true) {
						String data
								 = lineString.substring(1, index);
						if (checkText(data)) {
							columnList.add(killEscape(data));
							lineString = lineString.substring(
											(index + 2),
											lineString.length());
							break;
						} else {
							index = lineString.indexOf(
									"\",", (index + 2));
							if (index == -1) {
								columnList.add(killEscape(
										lineString.substring(1,
										lineString.length() - 1)));
								roopFlg = false;
								lineString = "";
								break;
							}
						}
					}
				}
			} else {
				int index = lineString.indexOf(",");
				if (index == -1) {
					columnList.add(killEscape(lineString));
					lineString = "";
					roopFlg = false;
				} else {
					columnList.add(
						killEscape(lineString.substring(0, index)));
					lineString = lineString.substring(index + 1,
							lineString.length());
				}
			}
		}
		String[] returnValue = (String[])columnList.toArray(
				new String[columnList.size()]);

		LineData data = new LineData();
		data.setParseData(returnValue);
		data.setRemainder(lineString);
		return data;
	}

	/**
	 * 文字列が CSV 区分文字列を含んでいないかチェックします。
	 * @param text テキスト
	 * @return 値文字列なら true
	 */
	private static boolean checkText(String text) {
		int cnt = 0;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '"') {
				cnt++;
			}
		}
		return ((cnt % 2) == 0);
	}

	/**
	 * 文字列の CSV エスケープを取り除きます。<br>
	 * 具体的には、'""'を'"'に変換します。
	 * @param text テキスト
	 * @return 変換後の文字列
	 */
	private static String killEscape(String text) {
		return text.replace("\"\"", "\"");
	}

	private static class LineData {

		private String[] parseData;

		private String remainder;

		public String[] getParseData() {
			return parseData;
		}

		public void setParseData(String[] parseData) {
			this.parseData = parseData;
		}

		public String getRemainder() {
			return remainder;
		}

		public void setRemainder(String remainder) {
			this.remainder = remainder;
		}
	}

}
