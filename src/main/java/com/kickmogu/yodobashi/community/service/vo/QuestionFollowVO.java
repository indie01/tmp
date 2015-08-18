/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;

/**
 * 質問フォロー情報のビューオブジェクトです。
 * @author kamiike
 *
 */
public class QuestionFollowVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = -1086372708877215380L;

	/**
	 * 質問です。
	 */
	private QuestionDO question;

	/**
	 * 回答者数です。
	 */
	private long answerCount;

	/**
	 * フォロー人数です。
	 */
	private long followerCount;

	/**
	 * フォロー日時です。
	 */
	private Date followDate;

	/**
	 * 最新のフォロワーのリストです。
	 */
	private List<CommunityUserDO> latestFollowers = Lists.newArrayList();

	/**
	 * @return question
	 */
	public QuestionDO getQuestion() {
		return question;
	}

	/**
	 * @param question セットする question
	 */
	public void setQuestion(QuestionDO question) {
		this.question = question;
	}

	/**
	 * @return answerCount
	 */
	public long getAnswerCount() {
		return answerCount;
	}

	/**
	 * @param answerCount セットする answerCount
	 */
	public void setAnswerCount(long answerCount) {
		this.answerCount = answerCount;
	}

	/**
	 * @return followerCount
	 */
	public long getFollowerCount() {
		return followerCount;
	}

	/**
	 * @param followerCount セットするfollowerCount
	 */
	public void setFollowerCount(long followerCount) {
		this.followerCount = followerCount;
	}

	/**
	 * @return latestFollowers
	 */
	public List<CommunityUserDO> getLatestFollowers() {
		return latestFollowers;
	}

	/**
	 * @param latestFollowers セットするlatestFollowers
	 */
	public void setLatestFollowers(List<CommunityUserDO> latestFollowers) {
		this.latestFollowers = latestFollowers;
	}

	/**
	 * @return followDate
	 */
	public Date getFollowDate() {
		return followDate;
	}

	/**
	 * @param followDate セットする followDate
	 */
	public void setFollowDate(Date followDate) {
		this.followDate = followDate;
	}

}
