package com.kickmogu.yodobashi.community.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.ImageDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionDao;
import com.kickmogu.yodobashi.community.resource.dao.ReviewDao;
import com.kickmogu.yodobashi.community.resource.dao.stored.NotifyUpdateContorlStoredProcedure;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.service.ImageService;
import com.kickmogu.yodobashi.community.service.QuestionService;
import com.kickmogu.yodobashi.community.service.ReviewPointSummaryService;
import com.kickmogu.yodobashi.community.service.ReviewService;
import com.kickmogu.yodobashi.community.service.vo.ProductSatisfactionSummaryVO;
import com.kickmogu.yodobashi.community.service.vo.ReviewPointSummaryVO;

@Service @Lazy @Qualifier("default")
public class ReviewPointSummaryServiceImpl implements ReviewPointSummaryService{
	private static final Logger log = LoggerFactory.getLogger(ReviewPointSummaryServiceImpl.class);
	/**
	 * Solrアクセサです。
	 */
	@Autowired @Qualifier("default")
	protected SolrOperations solrOperations;
	
	/**
	 * レビューサービスです。
	 */
	@Autowired
	private ReviewService reviewService;
	
	/**
	 * 画像サービスです。
	 */
	@Autowired
	private ImageService imageService;
	
	/**
	 * 質問サービスです。
	 */
	@Autowired
	private QuestionService questionService;
	
	/**
	 * レビュー DAO です。
	 */
	@Autowired
	private ReviewDao reviewDao;
	
	/**
	 * 質問 DAO です。
	 */
	@Autowired
	private QuestionDao questionDao;
	
	/**
	 * 画像 DAO です。
	 */
	@Autowired
	private ImageDao imageDao;
	
	/**
	 * ユーザー DAO です。
	 */
	@Autowired
	private CommunityUserDao communityUserDao;
	/**
	 * 商品フォローDAOです。
	 */
	@Autowired
	private ProductFollowDao productFollowDao;
	
	@Autowired @Qualifier("reviewPointSummary")
	private JdbcTemplate jdbcTemplate;
	
	@Autowired @Qualifier("reviewPointSummary")
	private DataSourceTransactionManager transactionManager;
	
	@Override
	public void insertReviewPointSummary(List<ReviewPointSummaryVO> list){
		
		final String sqlDelete = "delete from reviewPointSummary where SKU=?";
		final String sqlInsert = "insert into reviewPointSummary" +
				"(SKU,AVERAGERATING,REVIEWTOTALCOUNT,QATOTALCOUNT,POSTIMAGECOUNT,PRODUCTFOLLOWCOUNT) values " +
				"(?,?,?,?,?,?)";

		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		txTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
		final List<ReviewPointSummaryVO> skus = list;
		
		log.info("update insertReviewPointSummary start.");
		txTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				try {
					log.info("doInTransactionWithoutResult start.");
					
					List<Object[]> batchArgs = new ArrayList<Object[]>(skus.size());
					for (ReviewPointSummaryVO vo:skus) {
						batchArgs.add(new Object[]{vo.getSku()});
					}
					jdbcTemplate.batchUpdate(sqlDelete, batchArgs);
					batchArgs.clear();
					
					for (ReviewPointSummaryVO vo:skus) {
						batchArgs.add(new Object[]{
								vo.getSku(),
								vo.getAverageRating(),
								vo.getReviewTotalCount(),
								vo.getQaTotalCount(),
								vo.getPostImageCount(),
								vo.getProductFollowerCount()
								});
					}
					jdbcTemplate.batchUpdate(sqlInsert, batchArgs);
					
					NotifyUpdateContorlStoredProcedure notify = new NotifyUpdateContorlStoredProcedure(jdbcTemplate.getDataSource());
					for (ReviewPointSummaryVO vo:skus) {
						System.out.println("notify:" + vo.getSku());
						Map<String, Object> ret = notify.execute(vo.getSku());
						for (String entry:ret.keySet()) {
							System.out.println(entry + "/" + ret.get(entry));
						}
					}
					
				} catch (Exception e) {
					log.error("update error. rollback.", e);
					status.setRollbackOnly();
				}
			}
		});
		log.info("update insertReviewPointSummary finish.");
	}

	@Override
	public List<ReviewPointSummaryVO> ｇetProductInfomation(String[] skus) {
		
		List<ReviewPointSummaryVO> productSummaries = new ArrayList<ReviewPointSummaryVO>();
		
		Map<String, Long> postQuestionCounts  = null;
		Map<String, Long> postImageCounts     = null;
		Map<String, Long> productFollowCounts = null;
		
		postQuestionCounts  = questionService.countQuestionBySkus(skus);
		postImageCounts     = imageService.countImageBySkus(skus);
		productFollowCounts = productFollowDao.countFollowerCommunityUserBySku(skus);
		
		Map<String, Long> postReviewCounts = reviewService.countPostReviewBySku(skus);
		Map<String, ProductSatisfactionSummaryVO> productSatisfactionSummarys = reviewService.getSatisfactionAvarageMap(skus);
		
		for(String sku:skus){
			ReviewPointSummaryVO reviewPointSummary = new ReviewPointSummaryVO();
			reviewPointSummary.setSku(sku);
			
			if(postQuestionCounts.containsKey(sku)) {
				reviewPointSummary.setQaTotalCount(postQuestionCounts.get(sku));
			}
			if(postImageCounts.containsKey(sku)) {
				reviewPointSummary.setPostImageCount(postImageCounts.get(sku));
			}
			if(productFollowCounts.containsKey(sku)){
				reviewPointSummary.setProductFollowerCount(productFollowCounts.get(sku));
			}
			if(postReviewCounts.containsKey(sku)){
				reviewPointSummary.setReviewTotalCount(postReviewCounts.get(sku));
			}
			if(productSatisfactionSummarys.containsKey(sku)){
				reviewPointSummary.setAverageRating(productSatisfactionSummarys.get(sku).getSatisfactionAvarage());
			}
			productSummaries.add(reviewPointSummary);
		}
		
		return productSummaries;
	}

	@Override
	public List<String> findModifyProductInformation(Date fromDate, Date toDate) {
		
		if(fromDate == null || toDate == null) {
			throw new IllegalArgumentException("Input format error");
		}
		List<String> listSku = new ArrayList<String>();
		int limit = 1000;

		addSkuByUpdatedReview(listSku, fromDate, toDate, limit);
		addSkuByUpdatedQuestion(listSku, fromDate, toDate, limit);
		addSkuByUpdatedImage(listSku, fromDate, toDate, limit);
		addSkuByCommunityUserId(listSku, fromDate, toDate, limit);
		
		return listSku;
	}

	private void addSkuByUpdatedReview(List<String> listSku, Date fromDate, Date toDate, int limit) {
		// TODO Auto-generated method stub
		int num = 0;
		int offset = 0;
		while(true) {
			SearchResult<ReviewDO> reviews = reviewDao.findUpdatedReviewByOffsetTime(fromDate, toDate, limit, offset);
			log.info("ReviewDO: resultcount: " + reviews.getNumFound());
			for(ReviewDO review:reviews.getDocuments()) {
				num++;
				if(review.getProduct() != null){
					String sku = review.getProduct().getSku();
					if(!listSku.contains(sku)) {
						listSku.add(sku);
					}
				}
			}
			log.info("ReviewDO: resultcount: " + reviews.getNumFound());
			log.info("ReviewDO: offset: " + offset);
			log.info("ReviewDO: getDocuments: " + reviews.getDocuments().size());
			log.info("ReviewDO: finished: " + (reviews.getDocuments().size() + offset));
			if(offset + reviews.getDocuments().size() >= reviews.getNumFound()){
				break;
			}
			if(offset == 0) {
				log.warn("ReviewDO: query max row size over:" + reviews.getNumFound());
			}
			offset += limit;
			log.info("ReviewDO: continue");
		}
		log.info("ReviewDO: allfinished: count:" + num);
	}

	private void addSkuByUpdatedQuestion(List<String> listSku, Date fromDate, Date toDate, int limit) {
		// TODO Auto-generated method stub
		int num = 0;
		int offset = 0;
		while(true){
			SearchResult<QuestionDO> questions = questionDao.findUpdatedQuestionByOffsetTime(fromDate, toDate, limit, offset);
			log.info("QuestionDO: resultcount: " + questions.getNumFound());
			for(QuestionDO question:questions.getDocuments()) {
				num++;
				if(question.getProduct() != null) {
					String sku = question.getProduct().getSku();
					if(!listSku.contains(sku)) {
						listSku.add(sku);
					}
				}
			}
			log.info("QuestionDO: resultcount: " + questions.getNumFound());
			log.info("QuestionDO: offset: " + offset);
			log.info("QuestionDO: getDocuments: " + questions.getDocuments().size());
			log.info("QuestionDO: finished: " + (questions.getDocuments().size() + offset));
			if(offset + questions.getDocuments().size() >= questions.getNumFound()){
				break;
			}
			if(offset == 0) {
				log.warn("QuestionDO: query max row size over:" + questions.getNumFound());
			}
			offset += limit;
			log.info("QuestionDO: continue");
		}
		log.info("QuestionDO: allfinished: count:" + num);
	}

	private void addSkuByUpdatedImage(List<String> listSku, Date fromDate, Date toDate, int limit) {
		// TODO Auto-generated method stub
		int num = 0;
		int offset = 0;
		while(true) {
			SearchResult<ImageHeaderDO> images = imageDao.findUpdatedImageByOffsetTime(fromDate, toDate, limit, offset);
			log.info("ImageHeaderDO: resultcount: " + images.getNumFound());
			for(ImageHeaderDO image:images.getDocuments()){
				num++;
				if(image.getProduct() != null){
					String sku = image.getProduct().getSku();
					if(!listSku.contains(sku)) {
						listSku.add(sku);
					}
				}
			}
			log.info("ImageHeaderDO: resultcount: " + images.getNumFound());
			log.info("ImageHeaderDO: offset: " + offset);
			log.info("ImageHeaderDO: getDocuments: " + images.getDocuments().size());
			log.info("ImageHeaderDO: finished: " + (images.getDocuments().size() + offset));
			if(offset + images.getDocuments().size() >= images.getNumFound()){
				break;
			}
			if(offset == 0) {
				log.warn("ImageHeaderDO: query max row size over:" + images.getNumFound());
			}
			offset += limit;
			log.info("ImageHeaderDO: continue");
		}
		log.info("ImageHeaderDO: allfinished: count:" + num);
	}
	
	private void addSkuByCommunityUserId(List<String> listSku, Date fromDate, Date toDate, int limit) {
		int num = 0;
		int offset = 0;
		while(true) {
			SearchResult<CommunityUserDO> users = communityUserDao.findUpdatedCommunityUserByOffsetTime(fromDate, toDate, limit, offset);
			log.info("CommunityUserDO: resultcount: " + users.getNumFound());
			for(CommunityUserDO user:users.getDocuments()) {
				num++;
				String communityUserId = user.getCommunityUserId();
				addReviewSkuByCommunityUserId(listSku, communityUserId, limit);
				addQuestionSkuByCommunityUserId(listSku, communityUserId, limit);
				addImageSkuByCommunityUserId(listSku, communityUserId, limit);
			}
			log.info("CommunityUserDO: resultcount: " + users.getNumFound());
			log.info("CommunityUserDO: offset: " + offset);
			log.info("CommunityUserDO: getDocuments: " + users.getDocuments().size());
			log.info("CommunityUserDO: finished: " + (users.getDocuments().size() + offset));
			if(offset + users.getDocuments().size() >= users.getNumFound()){
				break;
			}
			if(offset == 0) {
				log.warn("CommunityUserDO: query max row size over:" + users.getNumFound());
			}
			offset += limit;
			log.info("CommunityUserDO: continue");
		}
		log.info("CommunityUserDO: allfinished: count:" + num);	
	}

	private void addReviewSkuByCommunityUserId(List<String> listSku, String communityUserId, int limit) {
		int num = 0;
		int offset = 0;
		while(true) {
			SearchResult<ReviewDO> reviews = reviewDao.findReviewByCommunityUserId(communityUserId, limit, offset);
			log.info("ReviewDO: resultcount: " + reviews.getNumFound());
			for(ReviewDO review:reviews.getDocuments()) {
				num++;
				if(review.getProduct() != null){
					String sku = review.getProduct().getSku();
					if(!listSku.contains(sku)) {
						listSku.add(sku);
					}
				}
			}
			log.info("ReviewDO: resultcount: " + reviews.getNumFound());
			log.info("ReviewDO: offset: " + offset);
			log.info("ReviewDO: getDocuments: " + reviews.getDocuments().size());
			log.info("ReviewDO: finished: " + (reviews.getDocuments().size() + offset));
			if(offset + reviews.getDocuments().size() >= reviews.getNumFound()){
				break;
			}
			if(offset == 0) {
				log.warn("ReviewDO: query max row size over:" + reviews.getNumFound());
			}
			offset += limit;
			log.info("ReviewDO: continue");
		}
		log.info("ReviewDO: allfinished: count:" + num);
	}
	
	private void addQuestionSkuByCommunityUserId(List<String> listSku, String communityUserId, int limit) {
		int num = 0;
		int offset = 0;
		while(true) {
			SearchResult<QuestionDO> questions = questionDao.findQuestionByCommunityUserId(communityUserId, limit, offset);
			log.info("QuestionDO: resultcount: " + questions.getNumFound());
			for(QuestionDO question:questions.getDocuments()) {
				num++;
				if(question.getProduct() != null){
					String sku = question.getProduct().getSku();
					if(!listSku.contains(sku)) {
						listSku.add(sku);
					}
				}
			}
			log.info("QuestionDO: resultcount: " + questions.getNumFound());
			log.info("QuestionDO: offset: " + offset);
			log.info("QuestionDO: getDocuments: " + questions.getDocuments().size());
			log.info("QuestionDO: finished: " + (questions.getDocuments().size() + offset));
			if(offset + questions.getDocuments().size() >= questions.getNumFound()){
				break;
			}
			if(offset == 0) {
				log.warn("QuestionDO: query max row size over:" + questions.getNumFound());
			}
			offset += limit;
			log.info("QuestionDO: continue");
		}
		log.info("QuestionDO: allfinished: count:" + num);
	}

	private void addImageSkuByCommunityUserId(List<String> listSku, String communityUserId, int limit) {
		int num = 0;
		int offset = 0;
		while(true) {
			SearchResult<ImageHeaderDO> images = imageDao.findImageByCommunityUserId(communityUserId, limit, offset);
			log.info("ImageHeaderDO: resultcount: " + images.getNumFound());
			for(ImageHeaderDO image:images.getDocuments()){
				num++;
				if(image.getProduct() != null){
					String sku = image.getProduct().getSku();
					if(!listSku.contains(sku)) {
						listSku.add(sku);
					}
				}
			}
			log.info("ImageHeaderDO: resultcount: " + images.getNumFound());
			log.info("ImageHeaderDO: offset: " + offset);
			log.info("ImageHeaderDO: getDocuments: " + images.getDocuments().size());
			log.info("ImageHeaderDO: finished: " + (images.getDocuments().size() + offset));
			if(offset + images.getDocuments().size() >= images.getNumFound()){
				break;
			}
			if(offset == 0) {
				log.warn("ImageHeaderDO: query max row size over:" + images.getNumFound());
			}
			offset += limit;
			log.info("ImageHeaderDO: continue");
		}
		log.info("ImageHeaderDO: allfinished: count:" + num);
	}

}
