package com.kickmogu.yodobashi.community.service;


import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;

/**
 * サーチエンジン提供用サイトマップ作成・サービスのテストクラスです。
 *
 * @author m.takahashi
 *
 */
//@RunWith(YcComJUnit4ClassRunner.class)
//@ContextConfiguration("/jcclientContext.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/serviceContext.xml")
//@ContextConfiguration("/test-serviceContext.xml")
//public class CommunityUserNicnameCollectServiceTest extends DataSetTest {
public class SearchEngineSitemapServiceTest {

	@Autowired
	private SearchEngineSitemapService service;

	@Autowired @Qualifier("default")
	private SolrOperations solrOperations;
	@Autowired  @Qualifier("default")
	private HBaseOperations hBaseOperations;

	static final String SKU_URL_FILE = "c:/home/comm/sku_url.txt";
	File outputDir = new File("c:/home/tmp");

	/**
	 * SKU
	 */
//	@Test
	public void testGenerateSitemapXmlFromSku() {
		System.out.println("[generateSitemapXmlFromSku]");
		File skuFile = new File(SKU_URL_FILE);
		int actual = service.generateSitemapXmlFromSku(outputDir, skuFile);
		int expected = 21;
		assertEquals("処理件数", expected, actual);
	}

	/**
	 * User
	 * @throws Exception
	 */
//	@Test
	public void testGenerateSitemapXmlFromUser() throws Exception {
		System.out.println("[generateSitemapXmlFromUser]");
		int actual = service.generateSitemapXmlFromUser(outputDir);
		int expected = 4;
		assertEquals("処理件数", expected, actual);
	}

	@Test
	public void extractSkuレビュー() throws Exception {
		System.out.println("[レビュー]");
		Class<?> type = ReviewDO.class;
		File file = new File("c:/home/comm/review_sku.txt");
		int actual = service.extractSku(type, file);
		int expected = 4;
		assertEquals("処理件数", expected, actual);
	}
	@Test
	public void extractSku質問() throws Exception {
		System.out.println("[質問]");
		Class<?> type = QuestionDO.class;
		File file = new File("c:/home/comm/question_sku.txt");
		int actual = service.extractSku(type, file);
		int expected = 3;
		assertEquals("処理件数", expected, actual);
	}
	@Test
	public void extractSku回答() throws Exception {
		System.out.println("[回答]");
		Class<?> type = QuestionAnswerDO.class;
		File file = new File("c:/home/comm/answer_sku.txt");
		int actual = service.extractSku(type, file);
		int expected = 3;
		assertEquals("処理件数", expected, actual);
	}
	@Test
	public void extractSkuイメージ() throws Exception {
		System.out.println("[イメージ]");
		Class<?> type = ImageHeaderDO.class;
		File file = new File("c:/home/comm/image_sku.txt");
		int actual = service.extractSku(type, file);
		int expected = 4;
		assertEquals("処理件数", expected, actual);
	}

	@Before
	public void setup() {
	}

	@After
	public void teardown() {
	}

}
