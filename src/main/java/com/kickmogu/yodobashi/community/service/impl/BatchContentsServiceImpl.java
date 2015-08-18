/**
 *
 */
package com.kickmogu.yodobashi.community.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.resource.dao.QuestionAnswerDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionDao;
import com.kickmogu.yodobashi.community.resource.dao.ReviewDao;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityOperationStatus;
import com.kickmogu.yodobashi.community.service.BatchContentsService;
import com.kickmogu.yodobashi.community.service.SystemMaintenanceService;


@Service
public class BatchContentsServiceImpl implements BatchContentsService {
	private static final Logger LOG = LoggerFactory.getLogger(BatchContentsServiceImpl.class);

	@Autowired
	private ReviewDao reviewDao;

	@Autowired
	private QuestionDao questionDao;

	@Autowired
	private QuestionAnswerDao questionAnswerDao;
	
	/**
	 * システムメンテナンスサービスです。
	 */
	@Autowired
	private SystemMaintenanceService systemMaintenanceService;

	@Override
	@ArroundHBase
	@ArroundSolr
	public void removeTemporaryContens(int interval, boolean removeReview,
			boolean removeQuestion, boolean removeAnswer, boolean viewOnly) {
		
		// ReadOnlyモードチェック
		if (systemMaintenanceService.getCommunityOperationStatus().equals(CommunityOperationStatus.READONLY_OPERATION)) {
			LOG.warn("Not removeTemporaryContens for " + CommunityOperationStatus.READONLY_OPERATION.getLabel());
			return;
		}
		
		Date intervalDate = DateUtils.truncate(new Date(), Calendar.DATE);
		intervalDate = DateUtils.addDays(intervalDate, (-1 * (interval)));

		if(removeReview){
			SearchResult<ReviewDO> reviewResult = reviewDao.findTemporaryReviewByBeforeInterval(intervalDate);
			List<String> reviewIds = new ArrayList<String>();
			for(ReviewDO review: reviewResult.getDocuments()){
				reviewIds.add(review.getReviewId());
				if(viewOnly)
					System.out.println("reviewId:" + review.getReviewId());
			}
			if(!viewOnly && !reviewIds.isEmpty())
				reviewDao.removeReviews(reviewIds);
		}		
		
		if(removeQuestion){
			SearchResult<QuestionDO> questionResult = questionDao.findTemporaryQuestionByBeforeInterval(intervalDate);
			List<String> questionIds = new ArrayList<String>();
			for(QuestionDO question: questionResult.getDocuments()){
				questionIds.add(question.getQuestionId());
				if(viewOnly)
					System.out.println("questionId:" + question.getQuestionId());
			}
			if(!viewOnly && !questionIds.isEmpty())
				questionDao.removeQuestions(questionIds);
		}		

		if(removeAnswer){
			SearchResult<QuestionAnswerDO> reviewResult = questionAnswerDao.findTemporaryQuestionAnswerByBeforeInterval(intervalDate);
			List<String> questionAnswerIds = new ArrayList<String>();
			for(QuestionAnswerDO questionAnswer: reviewResult.getDocuments()){
				questionAnswerIds.add(questionAnswer.getQuestionAnswerId());
				if(viewOnly)
					System.out.println("questionAnswerId:" + questionAnswer.getQuestionAnswerId());
			}
			if(!viewOnly && !questionAnswerIds.isEmpty())
				questionAnswerDao.removeQuestionAnswers(questionAnswerIds);
		}		
	}
}
