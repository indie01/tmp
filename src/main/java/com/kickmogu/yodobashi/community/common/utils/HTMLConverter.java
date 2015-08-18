package com.kickmogu.yodobashi.community.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * HTMLの内容を変換するための API を提供します。
 * @author kamiike
 */
public class HTMLConverter {

	/**
	 * デフォルトエンコーディングです。
	 */
	public static final String DEFAULT_ENCODING = "UTF-8";

	/**
	 * エスケープしない HTML タグのリストです。
	 */
	private String[] nonEscapeTagList = new String[] {
		"a",
//		"abbr", "acronym", "address", "area", "b", "basefont",
//		"bdo", "bgsound", "big", "blink", "blockquote", 
		"br",
//		"caption", "center", "cite", "code", "col", "colgroup",
//		"dd", "del", "dfn", "dir", 
//		"div",
//		"dl", "dt", 
		"em", "font",
//		"h1", "h2", "h3", "h4", "h5", "h6", "hr", "i", 
		"img", 
//		"ins",
//		"li", "map", "menu", "nobr", "ol",
		"p", 
//		"plaintext", "pre",
//		"q", "rb", "rp", "rt", "ruby", "s", "samp", "small", "spacer",
		"span",
		"strike", "strong",
//		"sub", "sup", "table",
//		"tbody", "td", "tfoot", "th", "thead", "tr", "tt",
		"u",
//		"ul", "var", "wbr", "xmp"//, "object", "param", "style"
	};

	/**
	 * object タグのクラスＩＤ値で許可するパターンです。
	 */
	private Pattern OBJECT_CLASSID_PATTERN
			= Pattern.compile("^clsid:\\w+");

	/**
	 * スタイル属性で設定を許可するパターンです。
	 */
	private Pattern stylePattern
			= Pattern.compile("^(" +
					"\\s*color:#[0-9a-zA-Z]{6};" +
//					"|background[^:;]*" +
//					"|display" +
//					"|position" +
//					"|top" +
//					"|left" +
//					"|bottom" +
//					"|right" +
//					"|float" +
//					"|clear" +
//					"|z-index" +
//					"|direction" +
//					"|width" +
//					"|height" +
//					"|quotes" +
//					"|volume" +
//					"|speak" +
//					"|speak[^:;]*" +
//					"|min[^:;]*" +
//					"|max[^:;]*" +
//					"|overflow[^:;]*" +
//					"|padding[^:;]*" +
//					"|outline[^:;]*" +
//					"|ruby[^:;]*" +
//					"|font[^:;]*" +
//					"|margin[^:;]*" +
//					"|border[^:;]*" +
//					"|layout[^:;]*" +
//					"|line[^:;]*" +
//					"|text[^:;]*" +
//					"|list[^:;]*" +
					")$");

	/**
	 * 文章内に存在する画像 URL 情報リストです。
	 */
	private List<ImageUrlInfo> existsImageUrlInfoList;

	/**
	 * アップロード済み画像 URL パターンです。
	 */
	private Pattern uploadedImageUrlPattern;

	/**
	 * 一時保存中の画像 URL パターンです。
	 */
	private Pattern temporaryImageUrlPattern;

	/**
	 * 画像URLのコンバーターです。
	 */
	private ImageUrlConverter converter = new ImageUrlConverter(null);

	/**
	 * スタイル属性で設定を許可するパターンを設定します。
	 * @param stylePattern スタイル属性で設定を許可するパターン
	 */
	public void setStylePattern(Pattern stylePattern) {
		this.stylePattern = stylePattern;
	}

	/**
	 * エスケープしない HTML タグのリストを設定します。
	 * @param nonEscapeTagList エスケープしない HTML タグのリスト
	 */
	public void setNonEscapeTagList(String[] nonEscapeTagList) {
		this.nonEscapeTagList = nonEscapeTagList;
	}

	/**
	 * アップロード済み画像 URL パターンを設定します。
	 * @param uploadedImageUrlPattern アップロード済み画像 URL パターン
	 */
	public void setUploadedImageUrlPattern(Pattern uploadedImageUrlPattern) {
		this.uploadedImageUrlPattern = uploadedImageUrlPattern;
	}

	/**
	 * 一時保存中の画像 URL パターンを設定します。
	 * @param temporaryImageUrlPattern 一時保存中の画像 URL パターン
	 */
	public void setTemporaryImageUrlPattern(Pattern temporaryImageUrlPattern) {
		this.temporaryImageUrlPattern = temporaryImageUrlPattern;
	}

	/**
	 * 画像URLのコンバーターを設定します。
	 * @param converter 画像URLのコンバーター
	 */
	public void setConverter(ImageUrlConverter converter) {
		this.converter = converter;
	}

	/**
	 * 文章内に存在する画像 URL 情報リストを返します。
	 * @return 文章内に存在する画像 URL 情報リスト
	 */
	public List<ImageUrlInfo> getExistsImageUrlInfoList() {
		return existsImageUrlInfoList;
	}

	/**
	 * HTMLテキストを無害化します。
	 * @param htmlText HTMLテキスト
	 * @return 無害化されたHTMLテキスト
	 */
	public String sanitizeHtml(String htmlText) {
		existsImageUrlInfoList = new ArrayList<ImageUrlInfo>();
		if (htmlText == null) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		boolean startTag = false;
		boolean startQuote = false;
		boolean startSingleQuote = false;
		boolean startDoubleQuote = false;

		StringBuilder tagParts = null;
		for (int i = 0; i < htmlText.length(); i++) {
			char c = htmlText.charAt(i);
			if (startTag) {
				if (htmlText.charAt(i - 1) == '<') {
					if (isNotAlphabet(c) &&
								c != '/') {
						buffer.append("&lt;");
						tagParts = null;
						i--;
						startTag = false;
						continue;
					} else {
						tagParts.append(c);
					}
				} else if (htmlText.charAt(i - 1) == '/' &&
						htmlText.charAt(i - 2) == '<') {
					if (isNotAlphabet(c)) {
						buffer.append("&lt;/");
						tagParts = null;
						i--;
						startTag = false;
						continue;
					} else {
						tagParts.append(c);
					}
				} else {
					switch (c) {
						case '<':
							if (!startQuote) {
								buffer.append(
										escapeTagString(
												tagParts.toString()));
								tagParts = new StringBuilder();
							}
							tagParts.append(c);
							break;
						case '>':
							tagParts.append(c);
							if (!startQuote) {
								buffer.append(sanitizeTag(
										tagParts.toString()));
								tagParts = null;
								startTag = false;
							}
							break;
						case '\'':
							if (startSingleQuote) {
								startQuote = false;
								startSingleQuote = false;
							} else if (!startDoubleQuote) {
								startQuote = true;
								startSingleQuote = true;
							}
							tagParts.append(c);
							break;
						case '"':
							if (startDoubleQuote) {
								startQuote = false;
								startDoubleQuote = false;
							} else if (!startSingleQuote) {
								startQuote = true;
								startDoubleQuote = true;
							}
							tagParts.append(c);
							break;
						default:
							tagParts.append(c);
					}
				}
			} else {
				if (c == '<') {
					tagParts = new StringBuilder();
					tagParts.append(c);
					startTag = true;
				} else {
					buffer.append(escapeTagString(
							String.valueOf(c)));
				}
			}
		}
		if (startTag) {
			buffer.append(escapeTagString(
					tagParts.toString()));
		}
		return buffer.toString();
	}

	/**
	 * タグ文字列を解析して、無害化します。
	 * @param tagText タグ文字列
	 * @return 無害化されたタグ文字列
	 */
	private String sanitizeTag(String tagText) {
		boolean startTag = false;
		boolean closed = false;
		for (int i = tagText.length() - 2; i >= 0; i--) {
			char c = tagText.charAt(i);
			if (c == '/') {
				closed = true;
				break;
			} else if (!Character.isWhitespace(c)) {
				break;
			}
		}
		String tagName = null;
		int startIndex = 2;
		if (!tagText.startsWith("</")) {
			startTag = true;
			startIndex = 1;
		}

		StringBuilder result = new StringBuilder();
		result.append("<");
		if (!startTag) {
			result.append("/");
		}

		boolean tagNameSearch = true;
		boolean startQuote = false;
		boolean startSingleQuote = false;
		boolean startDoubleQuote = false;

		StringBuilder attributeName = null;
		StringBuilder attributeValue = null;

		//0=属性名取得前、1=属性名取得中、2=「=」の前、3=「=」の後、4=属性値取得中
		int status = 0;

		StringBuilder tagNameBuffer = new StringBuilder();
		for (int i = startIndex; i < tagText.length() - 1; i++) {
			char c = tagText.charAt(i);
			if (tagNameSearch) {
				if (isNotAlphabet(c)) {
					tagNameSearch = false;
					tagName = tagNameBuffer.toString();
					result.append(tagName);
					boolean requiredEscape = true;
					for (String nonEscapeTag : nonEscapeTagList) {
						if (nonEscapeTag.equalsIgnoreCase(tagName)) {
							requiredEscape = false;
							break;
						}
					}
					if (requiredEscape) {
						return escapeTagString(tagText);
					}
					i--;
					continue;
				} else {
					tagNameBuffer.append(c);
				}
			} else if (Character.isWhitespace(c) && (status == 0 ||
					status == 2 || status == 3)) {
				continue;
			} else {
				boolean storeAttribute = false;
				switch (status) {
					case 0 :
						if (isNotAlphabet(c)) {
							if (closed) {
								result.append("/");
							}
							result.append(">");
							return result.toString();
						}
						if (attributeName == null) {
							attributeName = new StringBuilder();
						}
						attributeName.append(c);
						status++;
						break;
					case 1 :
						if (Character.isWhitespace(c)) {
							status++;
						} else if (c == '=') {
							attributeValue = new StringBuilder();
							status += 2;
						} else if (isNotAlphabet(c)) {
							if (closed) {
								result.append("/");
							}
							result.append(">");
							return result.toString();
						} else {
							attributeName.append(c);
						}
						break;
					case 2 :
						if (c == '=') {
							attributeValue = new StringBuilder();
							status++;
						} else if (!Character.isWhitespace(c)) {
							if (closed) {
								result.append("/");
							}
							result.append(">");
							return result.toString();
						}
						break;
					case 3 :
						if (c == '\'') {
							startQuote = true;
							startSingleQuote = true;
						} else if (c == '"') {
							startQuote = true;
							startDoubleQuote = true;
						} else {
							attributeValue.append(c);
						}
						status++;
						break;
					case 4 :
						if (startQuote) {
							if ((c == '\'' && startSingleQuote) ||
									(c == '"' && startDoubleQuote)) {
								storeAttribute = true;
							} else {
								attributeValue.append(c);
							}
						} else if (Character.isWhitespace(c)) {
							storeAttribute = true;
						} else {
							attributeValue.append(c);
						}
				}
				if (storeAttribute) {
					boolean doWrite = false;
					String name = attributeName.toString().toLowerCase();
					String value = attributeValue.toString();
					if (name.equals("href") ||
							name.equals("src")) {
						if (value.startsWith("//") ||
								value.startsWith("http://") ||
								value.startsWith("https://") ||
								value.startsWith("/")) {
							doWrite = true;
						}
					} else if (name.equalsIgnoreCase("style")) {
						if (stylePattern.matcher(value).find()) {
							doWrite = true;
						}
					} else if (tagName.equals("object") &&
							name.startsWith("classid")) {
						if (OBJECT_CLASSID_PATTERN.matcher(
								value).find()) {
							doWrite = true;
						}
					} else if (!name.startsWith("on")) {
						doWrite = true;
					}

					if (doWrite) {
						result.append(" ");
						result.append(attributeName);
						result.append("=");
						if (startSingleQuote) {
							result.append("'");
						} else {
							result.append("\"");
						}
						if (tagName.equals("img") && attributeName.toString().equals("src")) {
							if (uploadedImageUrlPattern != null &&
									uploadedImageUrlPattern.matcher(value).find()) {
								ImageUrlInfo info = new ImageUrlInfo();
								info.setImageId(converter.getImageId(value));
								info.setImageUrl(value);
								info.setIndex(existsImageUrlInfoList.size());
								addImageUrlInfo(info);
							} else if (temporaryImageUrlPattern != null &&
									temporaryImageUrlPattern.matcher(value).find()) {
								String imageId = converter.getImageId(value);
								String imageUrl = converter.toUploadedUrl(imageId);
								ImageUrlInfo info = new ImageUrlInfo();
								info.setImageId(imageId);
								info.setImageUrl(imageUrl);
								info.setTemporary(true);
								info.setIndex(existsImageUrlInfoList.size());
								addImageUrlInfo(info);
								if (imageUrl != null) {
									value = imageUrl;
								}
							}
						}
						result.append(escapeTagString(value));
						if (startSingleQuote) {
							result.append("'");
						} else {
							result.append("\"");
						}
					}

					status = 0;
					storeAttribute = false;
					attributeName = null;
					attributeValue = null;
					startQuote = false;
					startSingleQuote = false;
					startDoubleQuote = false;
				}
			}
		}

		if (tagName == null) {
			tagName = tagNameBuffer.toString();
			boolean requiredEscape = true;
			for (String nonEscapeTag : nonEscapeTagList) {
				if (nonEscapeTag.equalsIgnoreCase(tagName)) {
					requiredEscape = false;
					break;
				}
			}
			if (requiredEscape) {
				return escapeTagString(tagText);
			}
			result.append(tagName);
		}
		if (closed) {
			result.append("/");
		}
		result.append(">");
		return result.toString();
	}

	/**
	 * 画像URL情報を追加します。一度追加済みの場合は、追加しません。
	 * @param info 画像情報
	 */
	private void addImageUrlInfo(ImageUrlInfo info) {
		for (ImageUrlInfo target : existsImageUrlInfoList) {
			if (target.getImageId().equals(info.getImageId())) {
				return;
			}
		}
		existsImageUrlInfoList.add(info);
	}

	/**
	 * 指定した文字がアルファベットで無いかどうか返します。
	 * @param c 文字
	 * @return アルファベットで無い場合、true
	 */
	private boolean isNotAlphabet(char c) {
		if ((c >= 0x0041 && c <= 0x005a) ||
				(c >= 0x0061 && c <= 0x007a)) {
			return false;
		} else {
			return true;
		}
	}

    /**
     * 「<」、「>」をエスケープします。
     * @param src 文字ソース
     * @return 変換後の文字列
     */
	private String escapeTagString(String src) {
		String dest = src;
		dest = dest.replaceAll("<", "&lt;");
		dest = dest.replaceAll(">", "&gt;");
		return dest;
	}

	/**
	 * 画像URL情報です。
	 * @author kamiike
	 */
	public class ImageUrlInfo {

		/**
		 * 画像IDです。
		 */
		private String imageId;

		/**
		 * アップロード済み画像URLです。
		 */
		private String imageUrl;

		/**
		 * 一時保存中の画像化どうかです。
		 */
		private boolean temporary;

		/**
		 * 出現順序です。
		 */
		private int index;

		/**
		 * 画像IDを返します。
		 * @return 画像ID
		 */
		public String getImageId() {
			return imageId;
		}

		/**
		 * 画像IDを設定します。
		 * @param imageId 画像ID
		 */
		public void setImageId(String imageId) {
			this.imageId = imageId;
		}

		/**
		 * アップロード済み画像URLを返します。
		 * @return アップロード済み画像URL
		 */
		public String getImageUrl() {
			return imageUrl;
		}

		/**
		 * アップロード済み画像URLを設定します。
		 * @param imageUrl アップロード済み画像URL
		 */
		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
		}

		/**
		 * 一時保存中の画像化どうかを返します。
		 * @return 一時保存中の画像化どうか
		 */
		public boolean isTemporary() {
			return temporary;
		}

		/**
		 * 一時保存中の画像化どうかを設定します。
		 * @param temporary 一時保存中の画像化どうか
		 */
		public void setTemporary(boolean temporary) {
			this.temporary = temporary;
		}

		/**
		 * 出現順序を返します。
		 * @return 出現順序
		 */
		public int getIndex() {
			return index;
		}

		/**
		 * 出現順序を設定します。
		 * @param index 出現順序
		 */
		public void setIndex(int index) {
			this.index = index;
		}
	}

	/**
	 * 画像URLのコンバーターです。
	 * @author kamiike
	 */
	public static class ImageUrlConverter {

		/**
		 * 変換用マップです。
		 */
		private Map<String, String> converterMap;

		/**
		 * コンストラクタです。
		 * @param converterMap 変換マップ
		 */
		public ImageUrlConverter(Map<String, String> converterMap) {
			this.converterMap = converterMap;
		}

		/**
		 * 画像IDからアップロード済み画像URLに変換します。
		 * @param imageId 画像ID
		 * @return アップロード済み画像URL
		 */
		public String toUploadedUrl(String imageId) {
			if (imageId == null || converterMap == null ||
					converterMap.size() == 0) {
				return null;
			}

			return converterMap.get(imageId);
		}

		/**
		 * 画像URLから画像IDを抽出して返します。
		 * @param imageUrl 画像URL
		 * @return 画像ID
		 */
		public String getImageId(String imageUrl) {
			if (imageUrl == null) {
				return null;
			}
			int findSeparator = imageUrl.lastIndexOf("/");
			if (findSeparator == -1) {
				return null;
			}
			int lastLength = imageUrl.length();
			int extIndex = imageUrl.lastIndexOf(".");
			if (extIndex != -1) {
				lastLength = extIndex;
			}
			return imageUrl.substring(findSeparator + 1, lastLength);
		}
	}
}
