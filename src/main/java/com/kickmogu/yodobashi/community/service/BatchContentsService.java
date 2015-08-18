/**
 *
 */
package com.kickmogu.yodobashi.community.service;


public interface BatchContentsService {

	public void removeTemporaryContens(int interval, boolean removeReview, boolean removeQuestion, boolean removeAnswer, boolean viewOnly);

}
