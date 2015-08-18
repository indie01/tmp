package com.kickmogu.yodobashi.community.service;

import java.io.File;

/**
 * サーチエンジン提供用サイトマップ作成・サービス
 *
 * @author m.takahashi
 */
public interface SearchEngineSitemapService {

	/**
	 * CommunityUserDOから対象データを抽出し、その情報からsitemap.xmlを出力します。
	 * @param outputDir 出力先ディレクトリ
	 * @return 作成件数
	 */
	int generateSitemapXmlFromUser(File outputDir);

	/**
	 * 対象のSKUが格納されたファイルを読み、その情報からsitemap.xmlを出力します。
	 * @param outputDir 出力先ディレクトリ
	 * @param inputSku 入力ファイル（SKU)
	 * @return 作成件数
	 */
	int generateSitemapXmlFromSku(File outputDir, File inputSku);

	/**
	 * 引数で指定されたDomainObjectのSKUを抽出して、ファイルに
	 * 出力します。
	 * @param type DO(domain object)のClass
	 * @param outputSku 出力ファイル
	 * @return 作成件数
	 */
	int extractSku(Class<?> type, File outputSku);

}
