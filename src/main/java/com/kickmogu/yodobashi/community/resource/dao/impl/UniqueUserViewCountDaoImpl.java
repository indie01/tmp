/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.resource.dao.UniqueUserViewCountDao;
import com.kickmogu.yodobashi.community.resource.dao.util.SolrUtil;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.UniqueUserViewCountDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.UniqueUserViewCountType;

/**
 * ユニークユーザー閲覧数の DAO の実装です。
 * @author kamiike
 *
 */
@Service
public class UniqueUserViewCountDaoImpl implements UniqueUserViewCountDao {

	/**
	 * Solrアクセサです。
	 */
	@Autowired @Qualifier("default")
	private SolrOperations solrOperations;

	/**
	 * 指定したコンテンツのユニークユーザー閲覧数を返します。
	 * @param contentsId コンテンツID
	 * @param type コンテンツタイプ
	 * @param readLimit 一度に読み込む件数
	 * @return ユニークユーザー閲覧数
	 */
	@Override
	public long loadViewCountByContentsId(String contentsId,
			UniqueUserViewCountType type, int readLimit) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("type_s:");
		buffer.append(SolrUtil.escape(type.getCode()));
		buffer.append(" AND contentsId_s:");
		buffer.append(SolrUtil.escape(contentsId));
		;
		int offset = 0;
		long result = 0;
		while (true) {
			SearchResult<UniqueUserViewCountDO> searchResult
					= new SearchResult<UniqueUserViewCountDO>(solrOperations.findByQuery(
					new SolrQuery(buffer.toString()).setRows(readLimit
							).setStart(offset).setSortField("targetTime_dt",
									ORDER.asc), UniqueUserViewCountDO.class));
			for (UniqueUserViewCountDO viewCount : searchResult.getDocuments()) {
				result += viewCount.getViewCount();
				offset++;
			}
			if (searchResult.getDocuments().size(
					) < readLimit || searchResult.getNumFound() <= offset) {
				break;
			}
		}
		return result;
	}
}
