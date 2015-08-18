/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.resource.dao.RemoveContentsDao;
import com.kickmogu.yodobashi.community.resource.domain.RemoveContentsDO;
import com.kickmogu.yodobashi.community.resource.domain.RemoveContentsDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityContentsType;


/**
 * アプリケーションロック DAO です。
 * @author kamiike
 *
 */
@Service
public class RemoveContentsDaoImpl implements RemoveContentsDao {

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
	protected TimestampHolder timestampHolder;

	@Override
	public void save(RemoveContentsDO removeContents) {
		if(removeContents.getRegisterDateTime() == null)
			removeContents.setRegisterDateTime(timestampHolder.getTimestamp());
		removeContents.setModifyDateTime(timestampHolder.getTimestamp());
		for(RemoveContentsDetailDO detail:removeContents.getRemoveContentsDetails()){
			if(detail.getRegisterDateTime() == null)
				detail.setRegisterDateTime(timestampHolder.getTimestamp());
			detail.setModifyDateTime(timestampHolder.getTimestamp());
		}
		hBaseOperations.save(removeContents);
		for(RemoveContentsDetailDO detail :removeContents.getRemoveContentsDetails()){
			hBaseOperations.save(detail);
		}
		
	}
	
	/**
	 * お知らせ情報のインデックスを更新します。
	 * @param informationId お知らせ情報ID
	 */
	@Override
	@ArroundHBase
	@ArroundSolr
	public void updateRemoveContentsInIndex(String contentsId) {
		// 有効なユーザー（有効・一時停止）のみお知らせを作成する。
		if(StringUtils.isEmpty(contentsId))
			return;
		RemoveContentsDO removeContents = hBaseOperations.load(RemoveContentsDO.class, contentsId, Path.includeProp("*").includeRelation(RemoveContentsDetailDO.class).depth(1));
		if (removeContents != null) {
			solrOperations.save(removeContents,Path.includeProp("*").includeRelation(RemoveContentsDetailDO.class).depth(1));
		} else {
			solrOperations.deleteByKey(RemoveContentsDO.class, contentsId ,Path.includeProp("*").includeRelation(RemoveContentsDetailDO.class).depth(1));
		}
	}

	@Override
	public RemoveContentsDO getRemoveContents(CommunityContentsType type, String contentsId) {
		return hBaseOperations.load(RemoveContentsDO.class, contentsId, Path.includeProp("*").includeRelation(RemoveContentsDetailDO.class).depth(1));
	}
}
