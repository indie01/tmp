/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.resource.dao.QuestionFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.util.AdultHelper;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.ProductUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.SolrUtil;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;


/**
 * フォロー DAO です。
 * @author kamiike
 *
 */
@Service
public class QuestionFollowDaoImpl implements QuestionFollowDao {

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
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	private RequestScopeDao requestScopeDao;

	/**
	 * 指定したフォロワー、質問の質問フォロー情報
	 * が存在するか判定します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followQuestionId フォローする質問ID
	 * @return フォロー済みの場合、true
	 */
	@Override
	public boolean existsQuestionFollow(
			String communityUserId, String followQuestionId) {
		String questionFollowId = createQuestionFollowId(communityUserId, followQuestionId);
		QuestionFollowDO questionFollow = hBaseOperations.load(QuestionFollowDO.class,
				questionFollowId, Path.includeProp("questionFollowId,deleteFlag"));
		if(questionFollow == null || questionFollow.isDeleteFlag())
			return false;
		return true;
	}

	/**
	 * 質問フォロー情報を新規に作成します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followQuestionId フォローする質問ID
	 * @param followQuestionOwnerId フォローする質問オーナーID
	 * @param adult アダルト商品に対する質問かどうか
	 * @return 商品フォロー情報
	 */
	@Override
	public QuestionFollowDO createQuestionFollow(
			String communityUserId,
			String followQuestionId,
			String followQuestionOwnerId,
			boolean adult) {
		String questionFollowId = createQuestionFollowId(communityUserId, followQuestionId);
		QuestionFollowDO questionFollow = new QuestionFollowDO();
		questionFollow.setQuestionFollowId(questionFollowId);
		questionFollow.setCommunityUser(new CommunityUserDO());
		questionFollow.getCommunityUser().setCommunityUserId(communityUserId);
		questionFollow.setFollowQuestion(new QuestionDO());
		questionFollow.getFollowQuestion().setQuestionId(followQuestionId);
		questionFollow.setRelationQuestionOwnerId(followQuestionOwnerId);
		questionFollow.setAdult(adult);
		questionFollow.setFollowDate(timestampHolder.getTimestamp());
		questionFollow.setRegisterDateTime(timestampHolder.getTimestamp());
		questionFollow.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(questionFollow);
		return questionFollow;
	}

	/**
	 * 質問フォロー情報を削除します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followQuestionId フォローする質問ID
	 */
	@Override
	public void deleteFollowQuestion(
			String communityUserId,
			String followQuestionId) {
		String questionFollowId = createQuestionFollowId(communityUserId, followQuestionId);
		hBaseOperations.scanDeleteWithIndex(
				ActionHistoryDO.class, "communityUserId", communityUserId,
				hBaseOperations.createFilterBuilder(ActionHistoryDO.class
				).appendSingleColumnValueFilter(
						"questionId", CompareOp.EQUAL,
						followQuestionId).appendSingleColumnValueFilter(
								"actionHistoryType", CompareOp.EQUAL,
								ActionHistoryType.USER_FOLLOW_QUESTION).toFilter());
		hBaseOperations.deleteByKey(QuestionFollowDO.class, questionFollowId);
	}

	/**
	 * 質問から質問のフォロワーのコミュニティユーザーを検索して返します。<br />
	 * 一時停止フラグ、アダルトフラグを無視し、フォロー日時の昇順で返します。
	 * @param questionId 質問ID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return フォロワーのコミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findFollowerCommunityUserByQuestionIdForIndex(
			String questionId, int limit,
			int offset) {
		return findFollowerCommunityUserByQuestionId(questionId, limit, offset, true);
	}

	/**
	 * 質問から質問のフォロワーのコミュニティユーザーを検索して返します。
	 * @param questionId 質問ID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param asc 昇順ソート
	 * @return フォロワーのコミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findFollowerCommunityUserByQuestionId(
			String questionId, int limit,
			int offset, boolean asc) {
		SolrQuery query = new SolrQuery("followQuestionId_s:" + SolrUtil.escape(questionId) + " AND deleteFlag_b:false");
		ORDER order = null;
		if (asc) {
			order = ORDER.asc;
		} else {
			order = ORDER.desc;
		}
		query.addSortField("followDate_dt", order);
		query.setRows(limit);
		query.setStart(offset);

		SearchResult<QuestionFollowDO> searchResult = new SearchResult<QuestionFollowDO>(
				solrOperations.findByQuery(
				query, QuestionFollowDO.class,
				Path.includeProp("*").includePath(
				"communityUser.communityUserId").depth(1)));
		ProductUtil.filterInvalidProduct(searchResult);

		List<CommunityUserDO> followerCommunityUsers = new ArrayList<CommunityUserDO>();
		for (QuestionFollowDO questionFollow : searchResult.getDocuments()) {
			followerCommunityUsers.add(
					questionFollow.getCommunityUser());
		}

		SearchResult<CommunityUserDO> result = new SearchResult<CommunityUserDO>(
				searchResult.getNumFound(), followerCommunityUsers);
		return result;
	}

	/**
	 * 質問フォロー情報のインデックスを作成します。
	 * @param questionFollowId 質問フォローID
	 * @return 作成した場合、true
	 */
	@Override
	public boolean createQuestionFollowInIndex(
			String questionFollowId) {
		QuestionFollowDO questionFollow = hBaseOperations.load(
				QuestionFollowDO.class, questionFollowId);
		if (questionFollow != null && !questionFollow.isDeleteFlag()) {
			solrOperations.save(questionFollow);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 質問フォロー情報のインデックスを更新します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followQuestionId フォローする質問ID
	 */
	@Override
	public void updateQuestionFollowInIndex(
			String communityUserId, String followQuestionId) {
		String questionFollowId = createQuestionFollowId(communityUserId, followQuestionId);
		if (!createQuestionFollowInIndex(questionFollowId)) {
			solrOperations.deleteByQuery(new SolrQuery(
					"communityUserId_s:"
					+ SolrUtil.escape(communityUserId)
					+ " AND questionId_s:"
					+ SolrUtil.escape(followQuestionId)
					+ " AND actionHistoryType_s:"
					+ SolrUtil.escape(ActionHistoryType.USER_FOLLOW_QUESTION.getCode())),
					ActionHistoryDO.class);
			solrOperations.deleteByKey(QuestionFollowDO.class, questionFollowId);
		}
	}

	/**
	 * 質問のフォロー情報マップを返します。
	 * @param communityUserId 対象となるコミュニティユーザーID
	 * @param questionIds 質問IDリスト
	 * @return 質問のフォロー情報マップ
	 */
	@Override
	public Map<String, Boolean> loadQuestionFollowMap(
			String communityUserId, List<String> questionIds) {
		Map<String, Boolean> resultMap = new HashMap<String, Boolean>();
		if (StringUtils.isEmpty(communityUserId) || questionIds == null || questionIds.size() == 0) {
			return resultMap;
		}
		List<String> questionFolowIds = new ArrayList<String>();
		for (String questionId : questionIds) {
			questionFolowIds.add(IdUtil.createIdByConcatIds(communityUserId, questionId));
		}
		Map<String, QuestionFollowDO> map = hBaseOperations.find(
				QuestionFollowDO.class, String.class, questionFolowIds,
				Path.includeProp("questionFollowId,deleteFlag"));
		for (String questionId : questionIds) {
			if(map.get(IdUtil.createIdByConcatIds(communityUserId, questionId)) == null
					|| map.get(IdUtil.createIdByConcatIds(communityUserId, questionId)).isDeleteFlag()) {
				continue;
			}

			resultMap.put(questionId,
					map.containsKey(IdUtil.createIdByConcatIds(communityUserId, questionId)));
		}
		return resultMap;
	}

	/**
	 * フォローしている質問情報をフォロー日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 検索結果
	 */
	@Override
	public SearchResult<QuestionFollowDO> findFollowQuestion(
			String communityUserId, int limit, Date offsetTime, boolean previous) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("communityUserId_s:" + SolrUtil.escape(communityUserId));
		buffer.append(" AND deleteFlag_b:false");
		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND followDate_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND followDate_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}
		AdultHelper adultHelper = new AdultHelper(requestScopeDao.loadAdultVerification());
		SolrQuery query = new SolrQuery(
				adultHelper.toFilterQuery(buffer.toString()));
		query.setRows(limit);
		if (offsetTime == null || !previous) {
			query.setSortField("followDate_dt", ORDER.desc);
		} else {
			query.setSortField("followDate_dt", ORDER.asc);
		}
		SearchResult<QuestionFollowDO> searchResult = new SearchResult<QuestionFollowDO>(
				solrOperations.findByQuery(query, QuestionFollowDO.class,
				Path.includeProp("*").includePath(
						"followQuestion.product.sku").depth(3)));

		if (adultHelper.isRequireCheckAdult()) {
			searchResult.setHasAdult(
					adultHelper.hasAdult(
							buffer.toString(), QuestionFollowDO.class, solrOperations));
		}
		ProductUtil.filterInvalidProduct(searchResult);

		if (offsetTime == null || !previous) {
			return searchResult;
		} else {
			Collections.reverse(searchResult.getDocuments());
			return searchResult;
		}
	}

	/**
	 * コミュニティユーザーIDからフォロー質問を検索して返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return フォロー質問のリスト
	 */
	@Override
	public SearchResult<QuestionDO> findFollowQuestionByCommunityUserId(
			String communityUserId, int limit, int offset) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("communityUserId_s:" + SolrUtil.escape(communityUserId));
		buffer.append(" AND deleteFlag_b:false");
		AdultHelper adultHelper = new AdultHelper(requestScopeDao.loadAdultVerification());
		SolrQuery query = new SolrQuery(
					adultHelper.toFilterQuery(buffer.toString()));
		query.setRows(limit);
		query.setStart(offset);
		query.setSortField("followDate_dt", ORDER.asc);
		SearchResult<QuestionFollowDO> searchResult = new SearchResult<QuestionFollowDO>(
				solrOperations.findByQuery(query, QuestionFollowDO.class,
						Path.includeProp("*").includePath(
								"followQuestion.questionId").depth(1)));
		ProductUtil.filterInvalidProduct(searchResult);

		SearchResult<QuestionDO> result = new SearchResult<QuestionDO>();
		result.setNumFound(searchResult.getNumFound());
		for (QuestionFollowDO follow : searchResult.getDocuments()) {
			result.getDocuments().add(follow.getFollowQuestion());
		}
		return result;
	}

	/**
	 * 質問フォロー情報IDを生成して返します。
	 * @param communityUserId フォロワーのコミュニティユーザーID
	 * @param followQuestionId フォローする質問ID
	 * @return 商品フォロー情報ID
	 */
	private String createQuestionFollowId(String communityUserId, String followQuestionId) {
		return IdUtil.createIdByConcatIds(communityUserId, followQuestionId);
	}

	@Override
	public long countFollowQuestion(String communityUserId) {
		StringBuilder sb = new StringBuilder();

		sb.append("communityUserId_s:");
		sb.append(SolrUtil.escape(communityUserId));
		sb.append(" AND deleteFlag_b:false");
		sb.append(" AND ownerStop_b:false");

		SearchResult<QuestionFollowDO> searchResult = new SearchResult<QuestionFollowDO>(
				solrOperations.findByQuery(new SolrQuery(sb.toString()), QuestionFollowDO.class));

		return searchResult.getNumFound();
	}
}
