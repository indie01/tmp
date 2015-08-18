package com.kickmogu.yodobashi.community.service;

import java.util.Date;
import java.util.Map;

/**
 *  レビュー評価データカウント・サービス
 *
 * @author m.takahashi
 */
public interface ReviewVotingCountService {

	static final String REVIEW_VOTING_YES = "review_yes";
	static final String REVIEW_VOTING_NO = "review_no";

	/**
	 * レビューが参考になったか？「はい」「いいえ」をカウントします。
	 * @param targetDate 処理日付
	 * @return counterMap：レビューIDをkeyに参考になったレビューの「はい」、「いいえ」の件数を保持するMap
	 */
	public abstract Map<String, Map<String, Long>> countReviewVoting(Date targetDate);

}
