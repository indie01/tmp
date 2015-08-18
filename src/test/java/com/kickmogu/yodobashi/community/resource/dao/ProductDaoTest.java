/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;

/**
 * 商品 DAO のテストクラスです。
 * @author kamiike
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/resourceContext.xml")
public class ProductDaoTest {

	/**
	 * HBaseアクセサです。
	 */
	@Autowired  @Qualifier("default")
	protected HBaseOperations hBaseOperations;

	/*
	 * Solrアクセサです。
	 */
	@Autowired @Qualifier("default")
	protected SolrOperations solrOperations;

	/**
	 * 商品 DAO です。
	 */
	@Autowired
	private ProductDao productDao;

	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() {
	}

	/**
	 * 登録、インデックス更新処理を検証します。
	 */
	@Test
	public void test() {

		assertNotNull(productDao.loadProduct("100000001000624829"));
		assertNotNull(productDao.loadProduct("200000002000012355"));
		assertNotNull(productDao.loadProduct("100000009000738581"));
		assertNotNull(productDao.loadProduct("100000001001391026"));

	}
}
