/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.resource.dao.SkuCodeNotFoundDao;
import com.kickmogu.yodobashi.community.resource.domain.SkuCodeNotFoundDO;

/**
 * SKU 変換エラー DAO の実装です。
 * @author kamiike
 *
 */
@Service
public class SkuCodeNotFoundDaoImpl implements SkuCodeNotFoundDao {

	/**
	 * HBaseアクセサです。
	 */
	@Autowired  @Qualifier("default")
	private HBaseOperations hBaseOperations;

	/**
	 * Solrアクセサです。
	 */
	@Autowired @Qualifier("default")
	private SolrOperations solrOperations;

	/**
	 * タイムスタンプホルダーです。
	 */
	@Autowired
	private TimestampHolder timestampHolder;

	@Override
	public void createListWithIndex(List<SkuCodeNotFoundDO> skuCodeNotFounds) {
		for (SkuCodeNotFoundDO skuCodeNotFound:skuCodeNotFounds) {
			skuCodeNotFound.setRegisterDateTime(timestampHolder.getTimestamp());
			skuCodeNotFound.setModifyDateTime(timestampHolder.getTimestamp());
		}
		hBaseOperations.save(SkuCodeNotFoundDO.class, skuCodeNotFounds);
		solrOperations.save(SkuCodeNotFoundDO.class, skuCodeNotFounds);
	}
}
