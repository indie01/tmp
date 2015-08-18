/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;

/**
 * 商品詳細 DAO のテストクラスです。
 * @author kamiike
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/resourceContext.xml")
public class ProductDetailDaoTest {

	/**
	 * 商品詳細 DAOです。
	 */
	@Autowired @Qualifier("default")
	protected ProductDetailDao productDetailDao;

	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() {
	}

	/**
	 * 動作を検証します。
	 */
	@Test
	public void test() {
		List<String> janCodes = new ArrayList<String>();
		janCodes.add("4905524312737");
		janCodes.add("4902530916232");
		janCodes.add("4562215332070");
		janCodes.add("4988601007122");
		Map<String, String[]> map = productDetailDao.loadSkuMap(janCodes);
		assertEquals("100000001000624829", map.get("4905524312737")[0]);
		assertEquals("200000002000012355", map.get("4902530916232")[0]);
		assertEquals("100000009000738581", map.get("4562215332070")[0]);
		assertEquals("100000001001391026", map.get("4988601007122")[0]);
	}
}
