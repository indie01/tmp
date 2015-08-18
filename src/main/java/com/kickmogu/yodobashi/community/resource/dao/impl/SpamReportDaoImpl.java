/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
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
import com.kickmogu.yodobashi.community.resource.dao.SpamReportDao;
import com.kickmogu.yodobashi.community.resource.dao.util.ProductUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.SolrUtil;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.SpamReportDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportGroupType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportTargetType;

/**
 * 違反報告 DAO です。
 * @author kamiike
 *
 */
@Service
public class SpamReportDaoImpl implements SpamReportDao {

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
	 * 違反報告を登録します。
	 * @param spamReport 違反報告
	 */
	@Override
	public void create(SpamReportDO spamReport) {
		if (spamReport.getTargetType().equals(SpamReportTargetType.QUESTION_ANSWER)) {
			spamReport.setQuestionAnswer(hBaseOperations.load(
					QuestionAnswerDO.class, spamReport.getQuestionAnswer(
					).getQuestionAnswerId(), Path.includePath("communityUserId,question.communityUserId").depth(1)));
			spamReport.setQuestion(spamReport.getQuestionAnswer().getQuestion());
			spamReport.setRelationQuestionAnswerOwnerId(
					spamReport.getQuestionAnswer().getCommunityUser().getCommunityUserId());
			spamReport.setRelationQuestionOwnerId(
					spamReport.getQuestion().getCommunityUser().getCommunityUserId());
			spamReport.setGroupType(SpamReportGroupType.QUESTION);
		} else if (spamReport.getTargetType().equals(SpamReportTargetType.COMMENT)) {
			CommentDO comment = hBaseOperations.load(
					CommentDO.class, spamReport.getComment().getCommentId());
			spamReport.setRelationCommentOwnerId(
					comment.getCommunityUser().getCommunityUserId());
			spamReport.setRelationReviewOwnerId(
					comment.getRelationReviewOwnerId());
			spamReport.setRelationQuestionOwnerId(
					comment.getRelationQuestionOwnerId());
			spamReport.setRelationQuestionAnswerOwnerId(
					comment.getRelationQuestionAnswerOwnerId());
			if (comment.getReview() != null && StringUtils.isNotEmpty(
					comment.getReview().getReviewId())) {
				spamReport.setGroupType(SpamReportGroupType.REVIEW);
			} else if (comment.getImageHeader() != null && StringUtils.isNotEmpty(
					comment.getImageHeader().getImageId())) {
				spamReport.setGroupType(SpamReportGroupType.IMAGE);
			} else {
				spamReport.setGroupType(SpamReportGroupType.QUESTION);
			}
		} else if (spamReport.getTargetType().equals(SpamReportTargetType.QUESTION)) {
			QuestionDO question = hBaseOperations.load(QuestionDO.class,
					spamReport.getQuestion().getQuestionId());
			spamReport.setRelationQuestionOwnerId(
					question.getCommunityUser().getCommunityUserId());
			spamReport.setGroupType(SpamReportGroupType.QUESTION);
		} else if (spamReport.getTargetType().equals(SpamReportTargetType.REVIEW)) {
			ReviewDO review = hBaseOperations.load(ReviewDO.class,
					spamReport.getReview().getReviewId());
			spamReport.setRelationReviewOwnerId(
					review.getCommunityUser().getCommunityUserId());
			spamReport.setGroupType(SpamReportGroupType.REVIEW);
		} else {
			ImageHeaderDO imageHeader = hBaseOperations.load(ImageHeaderDO.class,
					spamReport.getImageHeader().getImageId());
			spamReport.setRelationImageOwnerId(
					imageHeader.getOwnerCommunityUserId());
			spamReport.setGroupType(SpamReportGroupType.IMAGE);
		}
		spamReport.setReportDate(timestampHolder.getTimestamp());
		spamReport.setRegisterDateTime(timestampHolder.getTimestamp());
		spamReport.setModifyDateTime(timestampHolder.getTimestamp());
		spamReport.setCheckInitialDate(timestampHolder.getTimestamp());
		spamReport.setStatus(SpamReportStatus.NEW);
		hBaseOperations.save(spamReport);
	}

	/**
	 * 違反報告のインデックスを更新します。
	 * @param spamReportId アクション履歴ID
	 */
	@Override
	public void updateSpamReportInIndex(
			String spamReportId) {
		SpamReportDO spamReport = hBaseOperations.load(
				SpamReportDO.class, spamReportId);
		if (spamReport != null && !spamReport.isDeleted()) {
			solrOperations.save(spamReport);
		} else {
			solrOperations.deleteByKey(SpamReportDO.class, spamReportId);
		}
	}

	@Override
	public SearchResult<SpamReportDO> findSpamReports(Date fromSpamReportDate,
			Date toSpamReportDate, SpamReportStatus status, int limit,
			int offset) {

		StringBuilder buffer = new StringBuilder();
		if(fromSpamReportDate != null && toSpamReportDate != null){
			toSpamReportDate = DateUtils.addDays(toSpamReportDate, 1);
			buffer.append(" reportDate_dt:{" +
					DateUtil.getThreadLocalDateFormat().format(fromSpamReportDate) + " TO " + DateUtil.getThreadLocalDateFormat().format(toSpamReportDate) + "}");
		}else if(fromSpamReportDate != null){
			buffer.append(" reportDate_dt:{" +
					DateUtil.getThreadLocalDateFormat().format(fromSpamReportDate) + " TO *}");
		}else if(toSpamReportDate != null){
			toSpamReportDate = DateUtils.addDays(toSpamReportDate, 1);
			buffer.append(" reportDate_dt:{" +
					"* TO "+ DateUtil.getThreadLocalDateFormat().format(toSpamReportDate) + "}");
		}

		if(status != null){
			if(!StringUtils.isEmpty(buffer.toString()))
				buffer.append(" AND ");
			buffer.append(" status_s:");
			buffer.append(SolrUtil.escape(status.getCode()));
		}
		if(StringUtils.isEmpty(buffer.toString()))
			buffer.append(" *:* ");
		SolrQuery query = new SolrQuery(buffer.toString());
		query.setSortField("reportDate_dt", ORDER.desc);
		if (limit > 0)
			query.setRows(limit);
		query.setStart(offset);

		SearchResult<SpamReportDO> searchResult = new SearchResult<SpamReportDO>(
				solrOperations.findByQuery(
								query,
								SpamReportDO.class,
								Path.includeProp("*").includePath(
									"question.questionId," +
									"review.reviewId," +
									"questionAnswer.question.questionId," +
									"imageHeader.product.sku," +
									"comment.question.questionId," +
									"comment.review.reviewId," +
									"comment.questionAnswer.question.questionId," +
									"comment.imageHeader.product.sku," +
									"communityUser.communityUserId," +
									"comment.communityUser.communityUserId," +
									"imageHeader.ownerCommunityUser.communityUserId," +
									"comment.communityUser.communityUserId," +
									"questionAnswer.communityUser.communityUserId," +
									"question.communityUser.communityUserId," +
									"review.communityUser.communityUserId,").depth(3)));
		ProductUtil.filterInvalidProduct(searchResult);
		return searchResult;
	}

	@Override
	public SpamReportDO updateCheckInitialDate(String spamReportId) {
		SpamReportDO spamReport = hBaseOperations.load(SpamReportDO.class, spamReportId);
		spamReport.setCheckInitialDate(timestampHolder.getTimestamp());
		hBaseOperations.save(spamReport);
		solrOperations.save(spamReport);
		return spamReport;
	}

	@Override
	public SpamReportDO loadSpamReport(String spamReportId) {
		return hBaseOperations.load(SpamReportDO.class, spamReportId);
	}

	@Override
	public void saveSpamReport(SpamReportDO spamReport) {
		spamReport.setCheckInitialDate(timestampHolder.getTimestamp());
		hBaseOperations.save(spamReport);
		solrOperations.save(spamReport);
	}

	@Override
	public SpamReportDO getSpamReportById(String spamReportId) {
		SpamReportDO result = null;
		StringBuilder buffer = new StringBuilder();
		buffer.append(" spamReportId:");
		buffer.append(SolrUtil.escape(spamReportId));
		SolrQuery query = new SolrQuery(buffer.toString());
		SearchResult<SpamReportDO> searchResult = new SearchResult<SpamReportDO>(
				solrOperations.findByQuery(
								query,
								SpamReportDO.class,
								Path.includeProp("*").includePath(
										"question.questionId," +
										"review.reviewId," +
										"questionAnswer.question.questionId," +
										"imageHeader.product.sku," +
										"comment.question.questionId," +
										"comment.review.reviewId," +
										"comment.questionAnswer.question.questionId," +
										"comment.imageHeader.product.sku," +
										"communityUser.communityUserId," +
										"comment.communityUser.communityUserId," +
										"imageHeader.ownerCommunityUser.communityUserId," +
										"comment.communityUser.communityUserId," +
										"questionAnswer.communityUser.communityUserId," +
										"question.communityUser.communityUserId," +
										"review.communityUser.communityUserId,").depth(3)));

		ProductUtil.filterInvalidProduct(searchResult);
		if(searchResult != null && searchResult.getDocuments().size() > 0)
			result =  searchResult.getDocuments().get(0);
		return result;
	}
}
