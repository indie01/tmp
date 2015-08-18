/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.common.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.solr.FacetResult;
import com.kickmogu.lib.solr.SolrConstants;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.SolrUtil;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;


/**
 * フォロー DAO です。
 * @author kamiike
 *
 */
@Service
public class CommunityUserFollowDaoImpl implements CommunityUserFollowDao {

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

	/**
	 * コミュニティユーザーIDからフォロワーのコミュニティユーザーを検索して返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return フォロワーのコミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findFollowerCommunityUserByCommunityUserId(
			String communityUserId, String excludeFollowCommunityUserId, int limit, int offset) {

		StringBuilder sb = new StringBuilder();

		sb.append("followCommunityUserId_s:");
		sb.append(SolrUtil.escape(communityUserId));

		if(StringUtils.isNotEmpty(excludeFollowCommunityUserId)){
			sb.append(" AND !communityUserId_s:");
			sb.append(SolrUtil.escape(excludeFollowCommunityUserId));
		}
		SolrQuery query = new SolrQuery(sb.toString());
		query.addSortField("followDate_dt", ORDER.asc);
		query.setRows(limit);
		query.setStart(offset);
		SearchResult<CommunityUserFollowDO> searchResult = new SearchResult<CommunityUserFollowDO>(
				solrOperations.findByQuery(
				query, CommunityUserFollowDO.class,
				Path.includeProp("*"
						).includePath("communityUser.communityUserId").depth(1)));

		List<CommunityUserDO> followerCommunityUsers = new ArrayList<CommunityUserDO>();
		for (CommunityUserFollowDO communityUserFollow : searchResult.getDocuments()) {
			followerCommunityUsers.add(
					communityUserFollow.getCommunityUser());
		}
		SearchResult<CommunityUserDO> result = new SearchResult<CommunityUserDO>(
				searchResult.getNumFound(), followerCommunityUsers);
		return result;
	}

	/**
	 * コミュニティユーザーIDからフォローコミュニティユーザーを検索して返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return フォローのコミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findFollowCommunityUserByCommunityUserId(
			String communityUserId, String excludeFollowCommunityUserId, int limit, int offset) {

		StringBuilder sb = new StringBuilder();

		sb.append("communityUserId_s:");
		sb.append(SolrUtil.escape(communityUserId));

		if(StringUtils.isNotEmpty(excludeFollowCommunityUserId)){
			sb.append(" AND !followCommunityUserId_s:");
			sb.append(SolrUtil.escape(excludeFollowCommunityUserId));
		}
		SolrQuery query = new SolrQuery(sb.toString());
		query.addSortField("followDate_dt", ORDER.asc);
		query.setRows(limit);
		query.setStart(offset);

		SearchResult<CommunityUserFollowDO> searchResult = new SearchResult<CommunityUserFollowDO>(
				solrOperations.findByQuery(
				query, CommunityUserFollowDO.class, Path.includeProp("*").includePath(
						"followCommunityUser.communityUserId").depth(1)));

		List<CommunityUserDO> followCommunityUsers = new ArrayList<CommunityUserDO>();
		for (CommunityUserFollowDO communityUserFollow : searchResult.getDocuments()) {
			followCommunityUsers.add(
					communityUserFollow.getFollowCommunityUser());
		}

		SearchResult<CommunityUserDO> result = new SearchResult<CommunityUserDO>(
				searchResult.getNumFound(), followCommunityUsers);
		return result;
	}

	/**
	 * コミュニティユーザーフォロー情報を削除します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followCommunityUserId フォローユーザーのコミュニティユーザーID
	 */
	@Override
	public void deleteFollowCommunityUser(
			String communityUserId,
			String followCommunityUserId) {
		String communityUserFollowId = createCommunityUserFollowId(
				communityUserId, followCommunityUserId);
		hBaseOperations.scanDeleteWithIndex(
				ActionHistoryDO.class, "communityUserId", communityUserId,
				hBaseOperations.createFilterBuilder(ActionHistoryDO.class
				).appendSingleColumnValueFilter(
						"followCommunityUserId", CompareOp.EQUAL,
						followCommunityUserId).appendSingleColumnValueFilter(
								"actionHistoryType", CompareOp.EQUAL,
								ActionHistoryType.USER_FOLLOW_USER).toFilter());
		hBaseOperations.scanDeleteWithIndex(
				InformationDO.class, "communityUserId", followCommunityUserId,
				hBaseOperations.createFilterBuilder(InformationDO.class
				).appendSingleColumnValueFilter(
						"followerCommunityUserId", CompareOp.EQUAL,
						communityUserId).appendSingleColumnValueFilter(
								"informationType", CompareOp.EQUAL,
								InformationType.FOLLOW).toFilter());
		hBaseOperations.deleteByKey(CommunityUserFollowDO.class,
				communityUserFollowId);
	}

	/**
	 * 指定したフォロワー、フォローユーザーのコミュニティユーザーフォロー情報
	 * が存在するか判定します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followCommunityUserId フォローユーザーのコミュニティユーザーID
	 * @return フォロー済みの場合、true
	 */
	@Override
	public boolean existsCommunityUserFollow(
			String communityUserId, String followCommunityUserId) {
		String communityUserFollowId = createCommunityUserFollowId(communityUserId, followCommunityUserId);
		return hBaseOperations.load(CommunityUserFollowDO.class,
				communityUserFollowId, Path.includeProp("communityUserFollowId")) != null;
	}

	/**
	 * コミュニティユーザーフォロー情報を新規に作成します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followCommunityUserId フォローユーザーのコミュニティユーザーID
	 * @return コミュニティユーザーフォロー情報
	 */
	@Override
	public CommunityUserFollowDO createCommunityUserFollow(
			String communityUserId,
			String followCommunityUserId) {
		String communityUserFollowId = createCommunityUserFollowId(
				communityUserId, followCommunityUserId);
		CommunityUserFollowDO communityUserFollow = new CommunityUserFollowDO();
		communityUserFollow.setCommunityUserFollowId(communityUserFollowId);
		communityUserFollow.setCommunityUser(new CommunityUserDO());
		communityUserFollow.getCommunityUser().setCommunityUserId(communityUserId);
		communityUserFollow.setFollowCommunityUser(new CommunityUserDO());
		communityUserFollow.getFollowCommunityUser().setCommunityUserId(followCommunityUserId);
		communityUserFollow.setFollowDate(timestampHolder.getTimestamp());
		communityUserFollow.setRegisterDateTime(timestampHolder.getTimestamp());
		communityUserFollow.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(communityUserFollow);
		return communityUserFollow;
	}

	/**
	 * コミュニティユーザーのフォロー情報マップを返します。
	 * @param communityUserId 対象となるコミュニティユーザーID
	 * @param communityUserIds コミュニティユーザーIDリスト
	 * @return コミュニティユーザーのフォロー情報マップ
	 */
	@Override
	public Map<String, Boolean> loadCommunityUserFollowMap(
			String communityUserId, List<String> communityUserIds) {
		Map<String, Boolean> resultMap = new HashMap<String, Boolean>();
		if (StringUtils.isEmpty(communityUserId) || communityUserIds == null || communityUserIds.size() == 0) {
			return resultMap;
		}
		Set<String> userFolowIds = new HashSet<String>();
		for (String userId : communityUserIds) {
			userFolowIds.add(IdUtil.createIdByConcatIds(communityUserId, userId));
		}
		Map<String, CommunityUserFollowDO> map = hBaseOperations.find(
				CommunityUserFollowDO.class, String.class, userFolowIds,
				Path.includeProp("communityUserFollowId"));
		for (String userId : communityUserIds) {
			resultMap.put(userId, map.containsKey(
					IdUtil.createIdByConcatIds(communityUserId, userId)));
		}
		return resultMap;
	}

	/**
	 * コミュニティユーザーフォロー情報のインデックスを作成します。
	 * @param communityUserFollowId コミュニティユーザーフォローID
	 * @return 作成した場合、true
	 */
	@Override
	public boolean createCommunityUserFollowInIndex(
			String communityUserFollowId) {
		CommunityUserFollowDO communityUserFollow = hBaseOperations.load(
				CommunityUserFollowDO.class, communityUserFollowId);
		if (communityUserFollow != null) {
			solrOperations.save(communityUserFollow);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * コミュニティユーザーフォロー情報のインデックスを更新します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followCommunityUserId フォローユーザーのコミュニティユーザーID
	 */
	@Override
	public void updateCommunityUserFollowInIndex(
			String communityUserId, String followCommunityUserId) {
		String communityUserFollowId = createCommunityUserFollowId(
				communityUserId, followCommunityUserId);
		if (!createCommunityUserFollowInIndex(communityUserFollowId)) {
			solrOperations.deleteByQuery(new SolrQuery(
					"communityUserId_s:"
					+ SolrUtil.escape(communityUserId)
					+ " AND followCommunityUserId_s:"
					+ SolrUtil.escape(followCommunityUserId)
					+ " AND actionHistoryType_s:"
					+ SolrUtil.escape(ActionHistoryType.USER_FOLLOW_USER.getCode())),
					ActionHistoryDO.class);
			solrOperations.deleteByQuery(new SolrQuery(
					"communityUserId_s:"
					+ SolrUtil.escape(followCommunityUserId)
					+ " AND followerCommunityUserId_s:"
					+ SolrUtil.escape(communityUserId)
					+ " AND informationType_s:"
					+ SolrUtil.escape(InformationType.FOLLOW.getCode())),
					InformationDO.class);
			solrOperations.deleteByKey(
					CommunityUserFollowDO.class, communityUserFollowId);
		}
	}

	/**
	 * フォローしているコミュニティユーザーを
	 * フォロー日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 検索結果
	 */
	@Override
	public SearchResult<CommunityUserFollowDO> findFollowCommunityUser(
			String communityUserId, int limit, Date offsetTime, boolean previous) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("communityUserId_s:" + SolrUtil.escape(communityUserId));
		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND followDate_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND followDate_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}
		SolrQuery query = new SolrQuery(
				buffer.toString());
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("followDate_dt", ORDER.desc);
		} else {
			query.setSortField("followDate_dt", ORDER.asc);
		}
		SearchResult<CommunityUserFollowDO> searchResult = new SearchResult<CommunityUserFollowDO>(
				solrOperations.findByQuery(query, CommunityUserFollowDO.class,
				Path.includeProp("*").includePath(
						"followCommunityUser.communityUserId").depth(1)));

		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}

	/**
	 * フォロワーとなっているコミュニティユーザーと最近取ったアクションを
	 * をフォロー日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 検索結果
	 */
	@Override
	public SearchResult<CommunityUserFollowDO> findFollowerCommunityUser(
			String communityUserId, int limit, Date offsetTime, boolean previous) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("followCommunityUserId_s:" + SolrUtil.escape(communityUserId));
		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND followDate_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND followDate_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("followDate_dt", ORDER.desc);
		} else {
			query.setSortField("followDate_dt", ORDER.asc);
		}
		SearchResult<CommunityUserFollowDO> searchResult = new SearchResult<CommunityUserFollowDO>(
				solrOperations.findByQuery(query, CommunityUserFollowDO.class,
				Path.includeProp("*").includePath(
						"communityUser.communityUserId").depth(1)));

		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}

	/**
	 * 指定日に指定したコミュニティユーザーをフォローしたユーザーを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param followDate フォロー日付
	 * @param limit 最大取得数
	 * @param offset 検索開始位置
	 * @return フォローしたユーザーリスト
	 */
	@Override
	public SearchResult<CommunityUserFollowDO> findNewUserFollow(
			String communityUserId, Date followDate, int limit, int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("followCommunityUserId_s:" + SolrUtil.escape(communityUserId));
		buffer.append(" AND ");
		buffer.append(SolrUtil.getSolrDateRangeQuery("followDate_dt", followDate));
		SolrQuery query = new SolrQuery(
					buffer.toString());
		query.setRows(limit);
		query.setStart(offset);
		query.setSortField("followDate_dt", ORDER.asc);
		return new SearchResult<CommunityUserFollowDO>(
				solrOperations.findByQuery(query, CommunityUserFollowDO.class,
						Path.includeProp("*").includePath(
								"communityUser.communityUserId").depth(1)));
	}

	/**
	 * コミュニティユーザーのフォロワーカウントマップを返します。
	 * @param communityUserIds コミュニティユーザーIDリスト
	 * @return コミュニティユーザーのフォロワーカウントマップ
	 */
	@Override
	public Map<String, Long> loadFollowCountMap(
			List<String> communityUserIds) {
		StringBuilder buffer = new StringBuilder();

		for (int i = 0; i < communityUserIds.size(); i++) {
			if (i > 0) {
				buffer.append(" OR ");
			}
			buffer.append("communityUserId_s:");
			buffer.append(SolrUtil.escape(communityUserIds.get(i)));
		}
		SolrQuery query = new SolrQuery(
					buffer.toString());
		query.addFacetField("communityUserId_s");
		query.setFacetLimit(SolrConstants.QUERY_ROW_LIMIT);
		Map<String, Long> result = new HashMap<String, Long>();
		for (FacetResult<String> follow : solrOperations.facet(
				CommunityUserFollowDO.class, String.class, query)) {
			result.put(follow.getValue(), follow.getCount());
		}
		return result;
	}

	/**
	 * コミュニティユーザーIDからフォロワーのコミュニティユーザーIDを検索して返します。<br />
	 * 一時停止フラグを無視し、フォロー日時の昇順で返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param asc 昇順ソート
	 * @return フォロワーのコミュニティユーザーIDのリスト
	 */
	@Override
	public SearchResult<String> findFollowerCommunityUserIdForIndex(
			String communityUserId, int limit, int offset) {
		SolrQuery query = new SolrQuery("followCommunityUserId_s:" + SolrUtil.escape(communityUserId));
		query.addSortField("followDate_dt", ORDER.asc);
		query.setRows(limit);
		query.setStart(offset);

		SearchResult<CommunityUserFollowDO> searchResult = new SearchResult<CommunityUserFollowDO>(
				solrOperations.findByQuery(
				query, CommunityUserFollowDO.class));

		List<String> followerCommunityUserIds = new ArrayList<String>();
		for (CommunityUserFollowDO communityUserFollow : searchResult.getDocuments()) {
			followerCommunityUserIds.add(
					communityUserFollow.getCommunityUser().getCommunityUserId());
		}

		SearchResult<String> result = new SearchResult<String>(
				searchResult.getNumFound(), followerCommunityUserIds);
		return result;
	}

	/**
	 * コミュニティユーザーフォロー情報IDを生成して返します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followCommunityUserId フォローユーザーのコミュニティユーザーID
	 * @return コミュニティユーザーフォロー情報ID
	 */
	private String createCommunityUserFollowId(String communityUserId, String followCommunityUserId) {
		return IdUtil.createIdByConcatIds(communityUserId, followCommunityUserId);
	}

	@Override
	public long countFollowCommunityUser(String communityUserId) {
		StringBuilder sb = new StringBuilder();

		sb.append("communityUserId_s:");
		sb.append(SolrUtil.escape(communityUserId));

		SearchResult<CommunityUserFollowDO> searchResult = new SearchResult<CommunityUserFollowDO>(
				solrOperations.findByQuery(new SolrQuery(sb.toString()), CommunityUserFollowDO.class));

		return searchResult.getNumFound();
	}

	@Override
	public long countFollowerCommunityUser(String communityUserId) {
		StringBuilder sb = new StringBuilder();

		sb.append("followCommunityUserId_s:");
		sb.append(SolrUtil.escape(communityUserId));

		SearchResult<CommunityUserFollowDO> searchResult = new SearchResult<CommunityUserFollowDO>(
				solrOperations.findByQuery(new SolrQuery(sb.toString()), CommunityUserFollowDO.class));

		return searchResult.getNumFound();
	}
}
