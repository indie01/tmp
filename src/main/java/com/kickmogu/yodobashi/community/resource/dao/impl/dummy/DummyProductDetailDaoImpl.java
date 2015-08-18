/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl.dummy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.yodobashi.community.resource.dao.ProductDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductDetailDao;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ProductType;

/**
 * ダミープロダクト詳細 DAO です。
 * @author kamiike
 */
@Service @Qualifier("dummy")
public class DummyProductDetailDaoImpl implements ProductDetailDao {

	/**
	 *
	 */
	@Autowired @Qualifier("dummy")
	private ProductDao productDao;

	/* (非 Javadoc)
	 * @see com.kickmogu.yodobashi.community.resource.dao.ProductDetailDao#loadSkuMap(java.util.List)
	 */
	@Override
	public Map<String, String[]> loadSkuMap(List<String> janCodes) {
		Map<String, String[]> map = new HashMap<String, String[]>();
		for (ProductDO product : productDao.findByJanCode(janCodes).values()) {
			map.put(product.getJan(), new String[]{product.getSku(),
				ProductType.NORMAL.getCode(), product.isAdult() ? "01" : null});
		}
		return map;
	}

}
