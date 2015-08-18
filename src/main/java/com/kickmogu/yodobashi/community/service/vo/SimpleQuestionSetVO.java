/**
 *
 */
package com.kickmogu.yodobashi.community.service.vo;

import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;

/**
 * 質問関連情報を集めたシンプルなビューオブジェクトです。
 * @author kamiike
 *
 */
public class SimpleQuestionSetVO extends BaseVO {

	/**
	 * シリアライズに使用する UID です。
	 */
	private static final long serialVersionUID = -3410503448920115683L;

	/**
	 * 質問です。
	 */
	private QuestionDO question;

	/**
	 * 回答者数です。
	 */
	private long answerCount;

	/**
	 * 回答のコメント数です。
	 */
	private long answerCommentCount;

	/**
	 * 回答のいいね数です。
	 */
	private long answerLikeCount;

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
	 * @return answerCommentCount
	 */
	public long getAnswerCommentCount() {
		return answerCommentCount;
	}

	/**
	 * @param answerCommentCount セットする answerCommentCount
	 */
	public void setAnswerCommentCount(long answerCommentCount) {
		this.answerCommentCount = answerCommentCount;
	}

	/**
	 * @return answerLikeCount
	 */
	public long getAnswerLikeCount() {
		return answerLikeCount;
	}

	/**
	 * @param answerLikeCount セットする answerLikeCount
	 */
	public void setAnswerLikeCount(long answerLikeCount) {
		this.answerLikeCount = answerLikeCount;
	}

}
