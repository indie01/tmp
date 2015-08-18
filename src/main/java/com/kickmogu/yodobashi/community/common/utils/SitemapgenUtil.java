package com.kickmogu.yodobashi.community.common.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.RandomStringUtils;

import com.redfin.sitemapgenerator.ChangeFreq;
import com.redfin.sitemapgenerator.SitemapIndexGenerator;
import com.redfin.sitemapgenerator.SitemapIndexUrl;
import com.redfin.sitemapgenerator.WebSitemapGenerator;
import com.redfin.sitemapgenerator.WebSitemapUrl;

/**
 * sitemapgen4jを使用するためのUtilityクラスです。
 *
 * @author m.takahashi
 *
 */
public class SitemapgenUtil {

	/**
	 * charset For URL Encoding
	 */
	public static final String CHARSET_NAME = CharEncoding.UTF_8;

	/**
	 * サイトマップXML、サイトマップインデックスXMLのファイル名を生成する時の文字数です。
	 */
	public static final int COUNT_RANDMAIZE = 10;


	/**
	 * sitemapgen4jのWebSitemapGeneratorを生成します。
	 * @param baseUrl
	 * @param baseDir
	 * @param fileNamePrefix
	 * @param maxUrls
	 * @param compressFlag
	 * @return WebSitemapGenerator
	 * @throws MalformedURLException
	 */
	public static WebSitemapGenerator getInstanceWebSitemapGenerator(String baseUrl, File baseDir,
			String fileNamePrefix, Integer maxUrls, Boolean compressFlag) throws MalformedURLException {

		WebSitemapGenerator generator = WebSitemapGenerator
				.builder(baseUrl, baseDir)
				.fileNamePrefix(fileNamePrefix)
				.gzip(compressFlag)
				.maxUrls(maxUrls)
				.build();
		return generator;
	}

	/**
	 * サイトマップurlを設定します。
	 * @param generator
	 * @param url
	 * @param priority
	 * @param changeFreq
	 * @throws MalformedURLException
	 */
	public static void buildSitemapUrl(WebSitemapGenerator generator, String url, Double priority, ChangeFreq changeFreq) throws MalformedURLException {
		if (null == priority) {
			buildSitemapUrl(generator, url, changeFreq);
			return;
		}

		WebSitemapUrl sitemapUrl = new WebSitemapUrl.Options(url)
				.lastMod(new Date())
				.priority(priority)
				.changeFreq(changeFreq)
				.build();
		generator.addUrl(sitemapUrl);
	}
	/**
	 * プライオリティ（設定任意項目）を設定せずに、サイトマップurlを設定。
	 * @param generator
	 * @param url
	 * @param changeFreq
	 * @throws MalformedURLException
	 */
	private static void buildSitemapUrl(WebSitemapGenerator generator, String url, ChangeFreq changeFreq) throws MalformedURLException {
		WebSitemapUrl sitemapUrl = new WebSitemapUrl.Options(url)
													.lastMod(new Date())
													.changeFreq(changeFreq)
													.build();
		generator.addUrl(sitemapUrl);
	}
// -----------------------------------------------------------------------------
//	WebSitemapUrl sitemapUrl = new WebSitemapUrl.Options(url)
//	.lastMod(new Date())
//	.priority(1.0)
//	.changeFreq(ChangeFreq.ALWAYS)
//	.build();
// -----------------------------------------------------------------------------



	/**
	 * sitemap.xmlを出力し、sitemap indexを生成します。
	 * @param generator WebSitemapGenerator
	 * @param baseUrl
	 * @param indexFileName サイトマップindexファイル名
	 * @throws MalformedURLException
	 */
	public static void generateSitemap(WebSitemapGenerator generator, String baseUrl, String indexFileName) throws MalformedURLException {

		// 出力ディレクトリ + ネーミング（例："sitemapindex"）+ Random文字列 + 拡張子
//		String fileName = getIndexFileName(new StringBuilder(indexFilePrefix));

		File outFile = new File(indexFileName);
		SitemapIndexGenerator indexGenerator = getInstanceSitemapIndexGenerator(baseUrl, outFile);
		StringBuilder sb = new StringBuilder();
		String base = baseUrl + "/";
		// sitemap.xml出力
		List<File> files = generator.write();
		for (File file : files) {
			String url = sb.append(base).append(file.getName()).toString();
			SitemapIndexUrl indexUrl =
					new SitemapIndexUrl(url, new Date());
			indexGenerator.addUrl(indexUrl);
			sb.delete(0, sb.length());
		}
		// sitemap index 出力
		indexGenerator.write();
	}
// -----------------------------------------------------------------------------
// 参考：以下の実装も可能
//		public static void 参考実装(WebSitemapGenerator generator) {
//			// sitemap.xml出力
//			generator.write();
//			// sitemap_index.xml出力
//			generator.writeSitemapsWithIndex();
//		}
// -----------------------------------------------------------------------------


	/**
	 * sitemap.xmlを出力し、sitemap indexも作成します。
	 * @param generator WebSitemapGenerator
	 * @param baseUrl
	 * @param indexFilePrefix サイトマップindexファイルのprefix：
	 * @throws MalformedURLException
	 */
//	public static void generateSitemap(WebSitemapGenerator generator, String baseUrl, String indexFilePrefix) throws MalformedURLException {
//		// 出力ディレクトリ + ネーミング（例："sitemapindex"）+ Random文字列 + 拡張子
//		String fileName = getIndexFileName(new StringBuilder(indexFilePrefix));
//		File outFile = new File(fileName);
//		SitemapIndexGenerator indexGenerator = getInstanceSitemapIndexGenerator(baseUrl, outFile);
//		StringBuilder sb = new StringBuilder();
//		String base = baseUrl + "/";
//		// sitemap.xml出力
//		List<File> files = generator.write();
//		for (File file : files) {
//			String url = sb.append(base).append(file.getName()).toString();
//			SitemapIndexUrl indexUrl =
//					new SitemapIndexUrl(url, new Date());
//			indexGenerator.addUrl(indexUrl);
//			sb.delete(0, sb.length());
//		}
//		// sitemap index 出力
//		indexGenerator.write();
//	}

	/**
	 * サイトマップindexファイルのprefixにRandomな文字列を付加してファイル名を生成する。
	 * ※未使用
	 * @param sb
	 * @return
	 */
	public static String getIndexFileName(StringBuilder sb) {
		return sb.append(RandomStringUtils.randomAlphanumeric(COUNT_RANDMAIZE)).append(".xml").toString();
	}


	/**
	 * @param baseUrl
	 * @param outFile
	 * @return
	 * @throws MalformedURLException
	 */
	private static SitemapIndexGenerator getInstanceSitemapIndexGenerator(String baseUrl, File outFile) throws MalformedURLException {
		return  new SitemapIndexGenerator
				.Options(baseUrl, outFile)
				.build();
	}


}
