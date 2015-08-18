/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;

/**
 * ショートURLを管理する DAO のテストクラスです。
 * @author hirabayashi
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/resourceContext.xml")
public class ShortUrlDaoTest {

	/**
	 * ショートURLを管理するDaoです。
	 */
	@Autowired
	private ShortUrlDao shortUrlDao;

	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() {
	}

	/**
	 * 生成したショートURLを検証します。
	 */
	@Test
	public void test() {
		String url = "http://dev.km-comsite.com:8080/community/mypage/index.html";
		String shortUrl = shortUrlDao.convertShortUrl(url);
		assertEquals(shortUrl, "http://j.mp/u1veKg");
	}

}
