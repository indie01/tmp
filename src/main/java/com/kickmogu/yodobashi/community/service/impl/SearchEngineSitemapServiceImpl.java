package com.kickmogu.yodobashi.community.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.FacetParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.solr.FacetResult;
import com.kickmogu.lib.solr.SolrConstants;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.common.exception.SearchEngineSitemapException;
import com.kickmogu.yodobashi.community.common.utils.SitemapgenUtil;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;
import com.kickmogu.yodobashi.community.service.SearchEngineSitemapService;
import com.redfin.sitemapgenerator.ChangeFreq;
import com.redfin.sitemapgenerator.WebSitemapGenerator;

/**
 *  サーチエンジン用サイトマップ作成・サービスの実装クラス
 * @author m.takahashi
 *
 */
@Service
public class SearchEngineSitemapServiceImpl implements SearchEngineSitemapService {

	private static final Logger logger = LoggerFactory.getLogger(SearchEngineSitemapServiceImpl.class);

	private static final String BASE_URL = "http://www.yodobashi.com/community";
	private static final String CATEGORY_PRODUCT = "product";
	private static final String CATEGORY_USER = "user";
	private static final String PRODUCT_URL = BASE_URL + "/" + CATEGORY_PRODUCT  + "/";
	private static final String USER_URL = BASE_URL + "/" +CATEGORY_USER + "/";

	private static final String SITEMAP_URL = "http://www.yodobashi.com/community/sitemap";

	/**
	 * www.yodobashi.com/community/user/コミュニティユーザーネーム/index.html
	 */
	private static final String USER_TOP_PAGE = "/index.html";

	// sitemap.xmlに設定するURLの上限
	private static final int MAX_PRODUCT_URL = 45000;
	private static final int MAX_USER_URL = 40000;
	// gzipにするかどうか
	private boolean COMPRESS_FLAG = true;

	// プライオリティ（：任意項目）は設定しない
//	private static final Double PRIORITY = new Double(1.0);

	// sitemapindexファイル名Convention
	private static final String SITEMAP_INDEX_NAME_PREFIX = "sitemapindex";
	// サイトマップインデックスファイル名(product url用) ※可変から固定に仕様変更
	private static final String SKU_INDEX_FILE_NAME = "/" + SITEMAP_INDEX_NAME_PREFIX + CATEGORY_PRODUCT + "bStSd9TYnJ.xml";
	// サイトマップインデックスファイル名(user url用) ※可変から固定に仕様変更
	private static final String USER_INDEX_FILE_NAME = "/" + SITEMAP_INDEX_NAME_PREFIX + CATEGORY_USER + "q5lUeSuaJD.xml";

	// sitemap.xmlファイル名Convention
	private static final String SITEMAP_XML_NAME_PREFIX = "sitemap";
	// sitemap.xmlファイル名(product url用) ※可変から固定に仕様変更：sitemapTZ2OELib0Dsku.xml.gz
	private static final String SKU_FILE_PREFIX = SITEMAP_XML_NAME_PREFIX + "TZ2OELib0Dsku";
	// sitemap.xml名(user url用) ※可変から固定に仕様変更：sitemapGHGXmyzentuser.xml.gz
	private static final String USER_FILE_PREFIX = SITEMAP_XML_NAME_PREFIX + "GHGXmyzentuser";

	// 検索制限数：CommunityUserDO
	private static final int USER_DO_READ_LIMIT = 1000;

	private static final int FACET_LIMIT = SolrConstants.QUERY_ROW_LIMIT;

	/**
	 * ユーザー DAO です。
	 */
	@Autowired
	private CommunityUserDao communityUserDao;

	/**
	 * Solr操作
	 */
	@Autowired
	private SolrOperations solrOperations;

	/**
	 * CommunityUserDOから対象データ（アクティブなユーザー）を抽出し、その情報からsitemap.xmlを出力します。
	 * @param outputDir 出力先ディレクトリ
	 * @return 作成件数
	 */
	@Override
	public int generateSitemapXmlFromUser(File outputDir) {
		int cnt = 0;
		String indexFileName = outputDir.getAbsolutePath() + USER_INDEX_FILE_NAME;
		try {
			WebSitemapGenerator generator = SitemapgenUtil
				.getInstanceWebSitemapGenerator(BASE_URL, outputDir, USER_FILE_PREFIX, MAX_USER_URL, COMPRESS_FLAG);
			// ニックネームからsitemap urlを設定
			cnt = processUserSitemap(generator, USER_TOP_PAGE);
			logger.info("作成件数:"+NumberFormat.getNumberInstance().format(cnt)+"件");
			SitemapgenUtil.generateSitemap(generator, SITEMAP_URL, indexFileName);
		} catch (MalformedURLException e) {
			logger.info("不正なURL");
			throw new SearchEngineSitemapException("不正なURL", e);
		} catch (UnsupportedEncodingException e) {
			throw new SearchEngineSitemapException("不正なエンコーディング", e);
		}
		return cnt;
	}
	/**
	 * サイトマップXML作成処理：ニックネームからsitemap urlを設定する
	 * @param generator sitemapgen4jのWebSitemapGenerator
	 * @param urlSuffix
	 * @return 作成件数
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 */
	private int processUserSitemap(WebSitemapGenerator generator, String urlSuffix) throws UnsupportedEncodingException, MalformedURLException {
		// 検索条件
		String query = new StringBuilder()
				.append("status_s:")
				.append(CommunityUserStatus.ACTIVE.getCode())
				.toString();
		SolrQuery solrQuery = new SolrQuery(query);
		long total = solrOperations.count(solrQuery, CommunityUserDO.class);
		logger.info("対象データ件数:"+total+"件");

		int offset = 0;
		int cnt = 0;
		while (cnt < total) {
			int processed = buildUserSitemapUrl(generator, urlSuffix, solrQuery, offset);
			cnt = cnt + processed;
			offset = cnt;
		}
		return cnt;
	}
	/**
	 * サイトマップXML構築
	 * @param generator sitemapgen4jのWebSitemapGenerator
	 * @param urlSuffix
	 * @param solrQuery
	 * @param offset
	 * @return 処理件数
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 */
	private int buildUserSitemapUrl(WebSitemapGenerator generator, String urlSuffix, SolrQuery solrQuery, int offset) throws UnsupportedEncodingException, MalformedURLException {
		int cnt = 0;
		StringBuilder sb = new StringBuilder();
		// 対象CommunityUser情報取得
		List<CommunityUserDO> users = getCommunityUsers(solrQuery, offset);
		for (CommunityUserDO user : users) {
			// URLエンコード
			String nicname = URLEncoder.encode(user.getCommunityName(), SitemapgenUtil.CHARSET_NAME);
			// 例 http://www.yodobashi.com/community/user/家電くん/index.html
			sb.append(USER_URL).append(nicname).append(urlSuffix);
			toSitemapUrl(generator, sb.toString());
			// StringBuilderは再生成しないで使い回す
			sb.delete(0, sb.length());
			cnt++;
		}
		return cnt;
	}
	/**
	 * ACTIVE（有効）なCommunityUserDO取得
	 * @param solrQuery
	 * @param offset
	 * @return CommunityUserDOのList
	 */
	private List<CommunityUserDO> getCommunityUsers(SolrQuery solrQuery, int offset) {
		solrQuery.setRows(USER_DO_READ_LIMIT).setStart(offset);
		SearchResult<CommunityUserDO> searchResult = communityUserDao
				.findCommunityUserByQuery(solrQuery);
		return searchResult != null ? searchResult.getDocuments() : new ArrayList<CommunityUserDO>();
	}

	/**
	 * 引数で指定されたDomainObjectのSKUを抽出して、ファイルに
	 * 出力します。
	 * @param type DO(domain object)のClass
	 * @param outputSku 出力ファイル
	 * @return 作成件数
	 */
	@Override
	public int extractSku(Class<?> type, File outputSku) {
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setQuery("*:*")
				.setFacet(true)
				.setFacetLimit(FACET_LIMIT)
				.addFacetField("productId_s");
		solrQuery.set(FacetParams.FACET_OFFSET, 0);

		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		int offset = 0;
		int cnt = 0;
		try {
			fileWriter = new FileWriter(outputSku);
			bufferedWriter = new BufferedWriter(fileWriter);
			while (true) {
				List<FacetResult<String>> list = solrOperations.facet(type, String.class, solrQuery);
				if (CollectionUtils.isEmpty(list)) {
					break;
				}
				// ファイル出力
				int processed = writeSku(list, bufferedWriter);
				cnt = cnt + processed;
				offset = cnt;
				solrQuery.set(FacetParams.FACET_OFFSET, offset);

//				System.out.println();
//				logger.info("processed[{}]、offset[{}]", processed, offset);
			}
		} catch (IOException e) {
			logger.info("ファイルエラー[IOException]");
			e.printStackTrace();
			throw new SearchEngineSitemapException("[IOException]", e);
		} finally {
			try {
				if (fileWriter != null) {
					fileWriter.flush();
				}
				if (bufferedWriter != null) {
					bufferedWriter.flush();
				}
				IOUtils.closeQuietly(fileWriter);
				IOUtils.closeQuietly(bufferedWriter);
			} catch (IOException e) {
				logger.info("ファイルエラー：flush or close");
				throw new SearchEngineSitemapException("[IOException]", e);
			}
		}
		logger.info("作成件数:{}件", NumberFormat.getNumberInstance().format(cnt));
		return cnt;
	}
	/**
	 * ファイル出力
	 * @param list
	 * @param outputSku
	 * @return
	 * @throws IOException
	 */
	private int writeSku(List<FacetResult<String>> list, BufferedWriter bufferedWriter) throws IOException {
		for (FacetResult<String> facetResult : list) {
			String sku = facetResult.getValue();
//			long count = facetResult.getCount();
//			logger.info("sku={}、count={}", sku, count);
			bufferedWriter.write(sku);
			bufferedWriter.newLine();
		}
		return list.size();
	}

	/**
	 * 対象のSKUが格納されたファイルを読み、その情報からsitemap.xmlを出力します。
	 * @param outputDir 出力先ディレクトリ
	 * @param inputSku 入力ファイル：対象skuから作成したURLを格納したファイル
	 * @return 作成件数
	 */
	@Override
	public int generateSitemapXmlFromSku(File outputDir, File inputSku) {
		// 固定ファイル名に変更
		String indexFileName = outputDir.getAbsolutePath() + SKU_INDEX_FILE_NAME;

		int cnt = 0;
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		String line = null;
		StringBuilder sb = new StringBuilder();
		try {
			WebSitemapGenerator generator = SitemapgenUtil
				.getInstanceWebSitemapGenerator(BASE_URL, outputDir, SKU_FILE_PREFIX, MAX_PRODUCT_URL, COMPRESS_FLAG);
			// SKU入力
			fileReader = new FileReader(inputSku);
			bufferedReader = new BufferedReader(fileReader);
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(PRODUCT_URL).append(line);
				// サイトマップURLを追加
				toSitemapUrl(generator, sb.toString());
				sb.delete(0, sb.length());
				cnt++;
			}
			logger.info("作成件数:"+NumberFormat.getNumberInstance().format(cnt)+"件");
			// sitemap.xml出力 sitemap index 作成
			SitemapgenUtil.generateSitemap(generator, SITEMAP_URL, indexFileName);
		} catch (MalformedURLException e) {
			logger.info("不正なURL");
			throw new SearchEngineSitemapException("不正なURL", e);
		} catch (FileNotFoundException e) {
			throw new SearchEngineSitemapException("ファイルが存在しない", e);
		} catch (IOException e) {
			throw new SearchEngineSitemapException("BufferedReaderエラー", e);
		} finally {
			IOUtils.closeQuietly(fileReader);
			IOUtils.closeQuietly(bufferedReader);
		}
		return cnt;
	}

	/**
	 * SitemapgenUtil sitemapURL生成呼び出し
	 * @param generator sitemapgen4jのWebSitemapGenerator
	 * @param url
	 * @throws MalformedURLException
	 */
	private void toSitemapUrl(WebSitemapGenerator generator, String url) throws MalformedURLException {
		SitemapgenUtil.buildSitemapUrl(generator, url, null, ChangeFreq.ALWAYS);
	}

}
