package com.kickmogu.yodobashi.community.service.impl;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.solr.SolrConstants;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.resource.dao.util.SolrUtil;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.VotingDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.VotingTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.VotingType;
import com.kickmogu.yodobashi.community.service.ReviewVotingCountService;

/**
 *  レビュー評価データカウント・サービスの実装クラス
 *
 * @author m.takahashi
 *
 */
@Service
public class ReviewVotingCountServiceImpl implements ReviewVotingCountService {

	private static final Logger logger = LoggerFactory.getLogger(ReviewVotingCountServiceImpl.class);

	// 検索制限数
	private static final int READ_LIMIT = SolrConstants.QUERY_ROW_LIMIT;

	/**
	 * ユーザー DAO です。
	 */
//	@Autowired
//	private CommunityUserDao communityUserDao;

	/**
	 * Solr操作
	 */
	@Autowired
	private SolrOperations solrOperations;

	/**
	 * レビューが参考になったか？「はい」「いいえ」をカウントします。
	 * @param targetDate 処理日付
	 * @return counterMap：レビューIDをkeyに参考になったレビューの「はい」、「いいえ」の件数を保持するMap
	 */
	@Override
	public Map<String, Map<String, Long>> countReviewVoting(Date targetDate) {
		// レビューIDをkeyに「はい」、「いいえ」の件数を保持するMap
		Map<String, Map<String, Long>> counterMap = new TreeMap<String, Map<String, Long>>();
		int cnt = process(targetDate, counterMap);
		logger.info("counter map:"+NumberFormat.getNumberInstance().format(cnt)+"件");
		return counterMap;
	}

	/**
	 * カウンタ保持用Map作成
	 * @param targetDate 処理日付
	 * @param counterMap レビューIDをkeyに「はい」、「いいえ」の件数を保持するMap
	 * @return Map件数
	 */
	private int process(Date targetDate, Map<String, Map<String, Long>> counterMap) {
		// 検索条件
		String query = new StringBuilder()
							.append(SolrUtil.getSolrDateRangeQuery("postDate_dt", targetDate))
							.append(" AND ")
							.append("targetType_s:")
							.append(SolrUtil.escape(VotingTargetType.REVIEW.getCode()))
							.toString();
		SolrQuery solrQuery = new SolrQuery(query);
		long total = solrOperations.count(solrQuery, VotingDO.class);
		logger.info("カウント件数(VotingDO):"+total+"件");
		print(query);

		int offset = 0;
		int cnt = 0;
		while (cnt < total) {
			// 「参考になった：はい、いいえ」をそれぞれカウントする
			int processed = count(counterMap, solrQuery, offset);
			cnt = cnt + processed;
			offset = cnt;
		}
		return counterMap.size();
	}

	/**
	 * @param counterMap
	 * @param solrQuery
	 * @param offset
	 * @return
	 */
	private int count(Map<String, Map<String, Long>> counterMap, SolrQuery solrQuery, int offset) {
		int cnt = 0;
		Map<String, Long> map = new HashMap<String, Long>();
		List<VotingDO> list = getVotingInfo(solrQuery, offset);
		print(list.size());
		for (VotingDO votingDO : list) {
			VotingType votingType = votingDO.getVotingType();
			String reviewId = votingDO.getReview() == null ? "" : votingDO.getReview().getReviewId();
//			String reviewId = votingDO.getReview().getReviewId();
			print(reviewId);
			if (!validVoting(votingDO)) {
				// はい、いいえ以外
				cnt++;
				continue;
			}
			if (!isSubmittedReview(reviewId)) {
				// 有効でないレビュー
				cnt++;
				continue;
			}
			if (counterMap.containsKey(reviewId)) {
				map = counterMap.get(reviewId);
			} else {
				map = new HashMap<String, Long>();
				map.put(REVIEW_VOTING_YES, new Long(0));
				map.put(REVIEW_VOTING_NO,  new Long(0));
			}
			countUp(votingType, map);
			counterMap.put(reviewId, map);
			cnt++;
			logger.info("["+votingDO.getVotingId()+"]");
		}
		return cnt;
	}

	/**
	 * VotingDO取得
	 * @param solrQuery
	 * @param offset 開始位置
	 * @return VotingDOのList
	 */
	private List<VotingDO> getVotingInfo(SolrQuery solrQuery, int offset) {
		solrQuery.setRows(READ_LIMIT).setStart(offset).setSortField("votingId", ORDER.asc);
		SearchResult<VotingDO> searchResult = new SearchResult<VotingDO>(
			solrOperations.findByQuery(solrQuery,
							VotingDO.class,
							Path.DEFAULT));
		return searchResult != null ? searchResult.getDocuments() : new ArrayList<VotingDO>();
	}

	/**
	 * 有効なレビューか判定する
	 * @param reviewId
	 * @return true：有効 false：無効
	 */
	private boolean isSubmittedReview(String reviewId) {
		if (StringUtils.isEmpty(reviewId)) {
			return false;
		}
		String query = new StringBuilder()
							.append("status_s:")
							.append(ContentsStatus.SUBMITTED.getCode())
							.append(" AND ")
							.append("reviewId:")
							.append(SolrUtil.escape(reviewId))
							.toString();
		long cnt = solrOperations.count(new SolrQuery(query), ReviewDO.class);
		if (0 < cnt) {
			return true;
		}
		return false;
	}

	/**
	 *
	 * @param votingDO
	 * @return
	 */
	private boolean validVoting(VotingDO votingDO) {
		if (VotingTargetType.REVIEW == votingDO.getTargetType()) {
			VotingType votingType = votingDO.getVotingType();
			if (VotingType.YES == votingType || VotingType.NO == votingType) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 「参考になった：はい、いいえ」をそれぞれカウントします
	 * @param votingType
	 */
	private void countUp(VotingType votingType, Map<String, Long> map) {
		long yesCount = map.get(REVIEW_VOTING_YES);
		long noCount = map.get(REVIEW_VOTING_NO);
		if (votingType == VotingType.YES) {
			yesCount++;
		} else if (votingType == VotingType.NO) {
			noCount++;
		}
		map.put(REVIEW_VOTING_YES, yesCount);
		map.put(REVIEW_VOTING_NO, noCount);
	}

	private void print(Object o) {
		System.out.println(o);
//		logger.info(String.valueOf(o));
	}
}
