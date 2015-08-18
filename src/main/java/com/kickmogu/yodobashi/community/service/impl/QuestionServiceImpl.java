/**
 *
 */
package com.kickmogu.yodobashi.community.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.SolrConstants;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.lib.solr.annotation.SolrTiming;
import com.kickmogu.yodobashi.community.common.exception.DataNotFoundException;
import com.kickmogu.yodobashi.community.common.exception.InputException;
import com.kickmogu.yodobashi.community.common.exception.UnActiveException;
import com.kickmogu.yodobashi.community.common.utils.DateUtil;
import com.kickmogu.yodobashi.community.common.utils.StringUtil;
import com.kickmogu.yodobashi.community.resource.cache.CacheStrategyType;
import com.kickmogu.yodobashi.community.resource.cache.MethodCache;
import com.kickmogu.yodobashi.community.resource.cache.TargetSystemType;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.dao.ActionHistoryDao;
import com.kickmogu.yodobashi.community.resource.dao.ApplicationLockDao;
import com.kickmogu.yodobashi.community.resource.dao.CommentDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.DailyScoreFactorDao;
import com.kickmogu.yodobashi.community.resource.dao.ImageDao;
import com.kickmogu.yodobashi.community.resource.dao.InformationDao;
import com.kickmogu.yodobashi.community.resource.dao.LikeDao;
import com.kickmogu.yodobashi.community.resource.dao.OrderDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductMasterDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionAnswerDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.RemoveContentsDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.UniqueUserViewCountDao;
import com.kickmogu.yodobashi.community.resource.dao.VotingDao;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.ProductUtil;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.DailyScoreFactorDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.SaveImageDO;
import com.kickmogu.yodobashi.community.resource.domain.ScoreFactorDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.DailyScoreFactorType;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;
import com.kickmogu.yodobashi.community.resource.domain.constants.LikeMessageType;
import com.kickmogu.yodobashi.community.resource.domain.constants.LikePrefixType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointScoreTerm;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PurchaseHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.UniqueUserViewCountType;
import com.kickmogu.yodobashi.community.service.ImageService;
import com.kickmogu.yodobashi.community.service.IndexService;
import com.kickmogu.yodobashi.community.service.MailService;
import com.kickmogu.yodobashi.community.service.QuestionService;
import com.kickmogu.yodobashi.community.service.SocialMediaService;
import com.kickmogu.yodobashi.community.service.UserService;
import com.kickmogu.yodobashi.community.service.config.ServiceConfig;
import com.kickmogu.yodobashi.community.service.vo.CommentSetVO;
import com.kickmogu.yodobashi.community.service.vo.QuestionAnswerSetVO;
import com.kickmogu.yodobashi.community.service.vo.QuestionSetVO;
import com.kickmogu.yodobashi.community.service.vo.SimpleQuestionSetVO;

/**
 * 質問サービスです。
 * @author kamiike
 *
 */
@Service
public class QuestionServiceImpl extends AbstractServiceImpl implements QuestionService {

	/**
	 * アクション履歴 DAO です。
	 */
	@Autowired
	protected ActionHistoryDao actionHistoryDao;

	/**
	 * アプリケーションロック DAO です。
	 */
	@Autowired
	protected ApplicationLockDao applicationLockDao;

	/**
	 * コミュニティユーザー DAO です。
	 */
	@Autowired
	protected CommunityUserDao communityUserDao;

	/**
	 * コメント DAO です。
	 */
	@Autowired
	protected CommentDao commentDao;

	/**
	 * コミュニティユーザーフォロー DAO です。
	 */
	@Autowired
	protected CommunityUserFollowDao communityUserFollowDao;

	/**
	 * 画像 DAO です。
	 */
	@Autowired
	protected ImageDao imageDao;

	/**
	 * お知らせ情報 DAO です。
	 */
	@Autowired
	protected InformationDao informationDao;

	/**
	 * いいね DAO です。
	 */
	@Autowired
	protected LikeDao likeDao;
	/**
	 * 参考になった　DAOです。
	 */
	@Autowired
	protected VotingDao votingDao;

	/**
	 * 注文 DAO です。
	 */
	@Autowired
	protected OrderDao orderDao;

	/**
	 * 商品 DAO です。
	 */
	@Autowired @Qualifier("catalog")
	protected ProductDao productDao;

	/**
	 * 商品マスター DAO です。
	 */
	@Autowired
	protected ProductMasterDao productMasterDao;

	/**
	 * 質問 DAO です。
	 */
	@Autowired
	protected QuestionDao questionDao;

	/**
	 * 質問回答 DAO です。
	 */
	@Autowired
	protected QuestionAnswerDao questionAnswerDao;

	/**
	 * 質問フォロー DAO です。
	 */
	@Autowired
	protected QuestionFollowDao questionFollowDao;

	/**
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	protected RequestScopeDao requestScopeDao;

	/**
	 * ユニークユーザー閲覧数 DAO です。
	 */
	@Autowired
	protected UniqueUserViewCountDao uniqueUserViewCountDao;

	/**
	 * 日次スコア要因 DAO です。
	 */
	@Autowired
	protected DailyScoreFactorDao dailyScoreFactorDao;

	/**
	 * socialメディア連携サービスです。
	 */
	@Autowired
	protected SocialMediaService socialMediaService;

	/**
	 * 画像サービスです。
	 */
	@Autowired
	protected ImageService imageService;

	/**
	 * インデックスサービスです。
	 */
	@Autowired
	protected IndexService indexService;

	/**
	 * コミュニティユーザーサービスです。
	 */
	@Autowired
	protected UserService userService;

	/**
	 * メールサービスです。
	 */
	@Autowired
	protected MailService mailService;

	/**
	 * サービスコンフィグです。
	 */
	@Autowired
	protected ServiceConfig serviceConfig;
	
	@Autowired
	protected ResourceConfig resourceConfig;

	/**
	 * タイムスタンプホルダーです。
	 */
	@Autowired
	protected TimestampHolder timestampHolder;

	@Autowired
	protected RemoveContentsDao removeContentsDao;

	/**
	 * 指定した商品に対するQA情報件数を返します。
	 * @param sku SKU
	 * @return QA情報件数
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=10000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public long countQuestionBySku(String sku) {
		return questionDao.countQuestionBySku(sku);
	}
	
	/**
	 * 指定した商品に対するQA情報件数を返します。
	 * @param sku SKU
	 * @return QA情報件数
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=10000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public long countQuestionBySkus(List<String> skus) {
		return questionDao.countQuestionBySkus(skus);
	}

	/**
	 * 指定した商品に対するQA情報件数リストを返します。
	 * @param skus SKUリスト
	 * @return QA情報件数リスト
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=10000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public Map<String, Long> countQuestionBySkus(
			String[] skus) {
		return questionDao.countQuestionBySku(skus);
	}
	
	/**
	 * 指定した商品に対するQA情報を更新日時（質問・回答の最終更新日時）順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<QuestionSetVO> findUpdateQuestionBySku(
			String sku,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous) {
		SearchResult<QuestionDO> searchResult = questionDao.findUpdateQuestionBySku(
				sku, excludeQuestionId, limit, offsetTime, previous);
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<QuestionSetVO>(0, new ArrayList<QuestionSetVO>());
		} else {
			//return appendPurchaseProduct(createQuestionSets(searchResult));
			return createQuestionSets(searchResult);
		}
	}

	/**
	 * 指定した商品に対する回答無しのQA情報を質問投稿日時順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<QuestionSetVO> findNewQuestionBySku(
			String sku,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous) {
		return findNewQuestionBySku(
				sku, 
				null, 
				excludeQuestionId, 
				limit, 
				offsetTime, 
				previous);
	}
	
	/**
	 * 指定した商品に対する回答無しのQA情報を質問投稿日時順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<QuestionSetVO> findNewQuestionBySku(
			String sku,
			String excludeCommunityUserId,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous) {
		SearchResult<QuestionDO> searchResult = questionDao.findNewQuestionBySku(
				sku, excludeCommunityUserId, excludeQuestionId, limit, offsetTime, previous);
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<QuestionSetVO>(0, new ArrayList<QuestionSetVO>());
		} else {
			return createQuestionSets(searchResult);
		}
	}

	/**
	 * 指定した商品に対する回答付のQA情報を盛り上がり順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetQuestionScore 検索開始スコア
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<QuestionSetVO> findPopularQuestionBySku(
			String sku,
			String excludeQuestionId,
			int limit,
			Double offsetQuestionScore,
			Date offsetTime,
			boolean previous) {
		SearchResult<QuestionDO> searchResult = questionDao.findPopularQuestionBySku(
				sku, excludeQuestionId, limit, offsetQuestionScore, offsetTime, previous);
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<QuestionSetVO>(0, new ArrayList<QuestionSetVO>());
		} else {
			return createQuestionSets(searchResult);
		}
	}
	
	/**
	 * 指定した商品に対するQA情報を更新日時（質問・回答の最終更新日時）順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<QuestionSetVO> findUpdateQuestionBySkus(
			String sku,
			List<String> skus,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous) {
		List<String> mergeSkus = new ArrayList<String>();
		mergeSkus.add(sku);
		mergeSkus.addAll(skus);
		SearchResult<QuestionDO> searchResult = questionDao.findUpdateQuestionBySkus(
				mergeSkus, excludeQuestionId, limit, offsetTime, previous);
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<QuestionSetVO>(0, new ArrayList<QuestionSetVO>());
		} else {
			return createQuestionSets(searchResult);
		}
	}

	/**
	 * 指定した商品に対する回答無しのQA情報を質問投稿日時順（降順）に返します。
	 * @param sku SKU メイン商品SKU
	 * @param skus バリエーション商品一覧
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<QuestionSetVO> findNewQuestionBySkus(
			String sku,
			List<String> skus,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous) {
		List<String> mergeSkus = new ArrayList<String>();
		mergeSkus.add(sku);
		mergeSkus.addAll(skus);
		SearchResult<QuestionDO> searchResult = questionDao.findNewQuestionBySkus(
				mergeSkus, excludeQuestionId, limit, offsetTime, previous);
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<QuestionSetVO>(0, new ArrayList<QuestionSetVO>());
		} else {
			return createQuestionSets(searchResult);
		}
	}
	
	/**
	 * 指定した商品に対する回答無しのQA情報を質問投稿日時順（降順）に返します。
	 * @param skus SKU一覧
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<QuestionSetVO> findNewQuestionBySkus(
			List<String> skus,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous) {
		SearchResult<QuestionDO> searchResult = questionDao.findNewQuestionBySkus(
				skus, excludeQuestionId, limit, offsetTime, previous);
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<QuestionSetVO>(0, new ArrayList<QuestionSetVO>());
		} else {
			return createQuestionSets(searchResult);
		}
	}
	
	@Override
	public SearchResult<QuestionSetVO> findNewQuestionBySkusInPurchaseProduct(
			String communityUserId, 
			String excludeQuestonId, 
			int limit,
			Date offsetTime) {
		SearchResult<QuestionSetVO> searchResult = new SearchResult<QuestionSetVO>(0, new ArrayList<QuestionSetVO>());
		Date tmpOffsetTime = offsetTime;
		String tmpOffsetSku = null;
		
		tmpOffsetTime = findQuestionsBySkus(
				communityUserId,
				excludeQuestonId,
				limit,
				tmpOffsetTime,
				tmpOffsetSku,
				searchResult);
		if( tmpOffsetTime != null ){
			while(searchResult.getNumFound() <= limit ){
				tmpOffsetTime = findQuestionsBySkus(
						communityUserId,
						excludeQuestonId,
						limit,
						tmpOffsetTime,
						tmpOffsetSku,
						searchResult);
				
				if( tmpOffsetTime == null ){
					break;
				}
			}
		}
		
		if( searchResult.getNumFound() > 0 ){
			List<String> questionIds = Lists.newArrayList();
			List<QuestionDO> questions = Lists.newArrayList();
			for(QuestionSetVO questionSetVO : searchResult.getDocuments()){
				questions.add(questionSetVO.getQuestion());
				questionIds.add(questionSetVO.getQuestion().getQuestionId());
			}
			Map<String, Long> answerCountMap = questionAnswerDao.loadQuestionAnswerCountMapByQuestionId(questionIds);
			Map<String, List<ImageHeaderDO>> imageAllMap = imageDao.loadAllImageMapByContentsIds(PostContentType.QUESTION, questionIds);
			Map<String, Boolean> hasAnswerMap = new HashMap<String, Boolean>();
			Map<String, Boolean> questionFollowMap = new HashMap<String, Boolean>();
			//質問回答済みかどうか
			hasAnswerMap = hasQuestionAnswer(communityUserId, questionIds);
			// 質問フォロー済みかどうか
			questionFollowMap = questionFollowDao.loadQuestionFollowMap(communityUserId, questionIds);
			
			Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(questions);
			
			for(QuestionSetVO vo : searchResult.getDocuments()){
				QuestionDO question = vo.getQuestion();
				String questionId = question.getQuestionId();
				
				if (answerCountMap.containsKey(questionId)) {
					vo.setAnswerCount(answerCountMap.get(questionId));
				}
				if( hasAnswerMap.containsKey(questionId)){
					vo.setAnswerFlg(hasAnswerMap.get(questionId));
				}
				if (imageAllMap.containsKey(questionId)) {
					vo.setImages(imageAllMap.get(questionId));
				}
				if (questionFollowMap.containsKey(questionId)) {
					vo.setFollowingFlg(questionFollowMap.get(questionId));
				}
				if (question.getLastAnswerDate() != null) {
					QuestionAnswerDO questionAnswer = questionAnswerDao.loadHighScoreQuestionAnswerByQuestionId(questionId);
					SearchResult<QuestionAnswerDO> answerSets = new SearchResult<QuestionAnswerDO>(0, new ArrayList<QuestionAnswerDO>());
					if (questionAnswer != null) {
						answerSets.setNumFound(1);
						answerSets.getDocuments().add(questionAnswer);
					}
					vo.setAnswerSets(createQuestionAnswerSets(answerSets));
				} else {
					vo.setAnswerSets(new SearchResult<QuestionAnswerSetVO>(0, new ArrayList<QuestionAnswerSetVO>()));
				}
				searchResult.updateFirstAndLast(vo);
				if (question.isStop(communityUserId, stopCommunityUserIds)) {
					searchResult.countUpStopContents();
					continue;
				}
				// コンテンツのコミュニティユーザーの購入情報を取得する。
				PurchaseProductDO purchaseProductDO = orderDao.loadPurchaseProductBySku(
						question.getCommunityUser().getCommunityUserId(),
						question.getProduct().getSku(),
						Path.DEFAULT,
						false);
				if( purchaseProductDO != null && !purchaseProductDO.isDeleted() ){
					vo.setPurchaseProduct( purchaseProductDO );
				}
			}
		}
		return searchResult;
	}
	
	private Date findQuestionsBySkus(
			String communityUserId, 
			String excludeQuestonId, 
			Integer limit,
			Date offsetTime,
			String offsetSku,
			SearchResult<QuestionSetVO> result){
		
		SearchResult<PurchaseProductDO> searchResult = orderDao.findPurchaseProductByCommunityUserId(
				communityUserId, 
				false,
				SolrConstants.QUERY_ROW_LIMIT,
				new Date(),
				offsetSku,
				false);
		
		if (searchResult.getDocuments().size() > 0) {
			List<String> skus = new ArrayList<String>();
			for (PurchaseProductDO purchaseProduct : searchResult.getDocuments()) {
				skus.add(purchaseProduct.getProduct().getSku());
			}
			
			SearchResult<QuestionDO> searchResultQuestions = questionDao.findUpdateQuestionBySkus(
					skus,
					communityUserId,
					excludeQuestonId,
					limit,
					offsetTime,
					false);
			if (searchResultQuestions.getDocuments().size() > 0) {
				if( !searchResultQuestions.isHasAdult() ){
					searchResultQuestions.setHasAdult(searchResultQuestions.isHasAdult());
				}
				long numFound = result.getNumFound() + searchResultQuestions.getNumFound();
				if( limit < numFound){
					for(QuestionDO questionDO : searchResultQuestions.getDocuments()){
						QuestionSetVO vo = new QuestionSetVO();
						vo.setQuestion(questionDO);
						for (PurchaseProductDO purchaseProduct : searchResult.getDocuments()) {
							if( questionDO.getProduct().getSku().equals(purchaseProduct.getProduct().getSku())){
								vo.setLoginUserPurchaseProduct(purchaseProduct);
								break;
							}
						}
						result.getDocuments().add(vo);
						if( result.getDocuments().size() == limit )
							break;
					}
					result.setNumFound(numFound);
				}else{
					for(QuestionDO questionDO : searchResultQuestions.getDocuments()){
						QuestionSetVO vo = new QuestionSetVO();
						vo.setQuestion(questionDO);
						for (PurchaseProductDO purchaseProduct : searchResult.getDocuments()) {
							if( questionDO.getProduct().getSku().equals(purchaseProduct.getProduct().getSku())){
								vo.setLoginUserPurchaseProduct(purchaseProduct);
								break;
							}
						}
						result.getDocuments().add(vo);
					}
					result.setNumFound(numFound);
				}
			}
			if( searchResult.getNumFound() > searchResult.getDocuments().size()){
				PurchaseProductDO tmpPurchaseProductDO = searchResult.getDocuments().get(searchResult.getDocuments().size() - 1);
				offsetTime = tmpPurchaseProductDO.getPurchaseDate();
				offsetSku = tmpPurchaseProductDO.getProduct().getSku();
			}else{
				offsetTime = null;
				offsetSku = null;
			}
			return offsetTime;
		}
		
		offsetTime = null;
		offsetSku = null;
		return null;
	}
	/**
	 * 指定した商品に対する回答付のQA情報を盛り上がり順（降順）に返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offsetQuestionScore 検索開始スコア
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return QA情報一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<QuestionSetVO> findPopularQuestionBySkus(
			String sku,
			List<String> skus,
			String excludeQuestionId,
			int limit,
			Double offsetQuestionScore,
			Date offsetTime,
			boolean previous) {
		List<String> mergeSkus = new ArrayList<String>();
		mergeSkus.add(sku);
		mergeSkus.addAll(skus);
		
		SearchResult<QuestionDO> searchResult = questionDao.findPopularQuestionBySkus(
				mergeSkus, excludeQuestionId, limit, offsetQuestionScore, offsetTime, previous);
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<QuestionSetVO>(0, new ArrayList<QuestionSetVO>());
		} else {
			return createQuestionSets(searchResult);
		}
	}
	
	/**
	 * 指定した質問をインデックス情報から返します。<br />
	 * 回答情報は設定されません。
	 * @param questionId 質問ID
	 * @return 質問
	 */
	@Override
	@ArroundSolr
	public QuestionSetVO getQuestionFromIndex(
			String questionId, boolean includeDeleteContents) {
		QuestionDO question = questionDao.loadQuestionFromIndex(questionId, includeDeleteContents);
		if (question != null && !ProductUtil.invalid(question)) {
			SearchResult<QuestionDO> list = new SearchResult<QuestionDO>(
					1, new ArrayList<QuestionDO>());
			list.getDocuments().add(question);
			SearchResult<QuestionSetVO> questionSetVOs = createQuestionSets(list);
			if (questionSetVOs.getDocuments().isEmpty())
				return null;
			return questionSetVOs.getDocuments().get(0);
		} else {
			return null;
		}
	}

	/**
	 * 指定した質問をインデックス情報から返します。退会削除質問は取得しない<br />
	 * 回答情報は設定されません。
	 * @param questionId 質問ID
	 * @return 質問
	 */
	@Override
	@ArroundSolr
	public QuestionSetVO getQuestionFromIndexExcludeWithdraw(
			String questionId, boolean includeDeleteContents) {
		
		QuestionDO question = questionDao.loadQuestionFromIndex(questionId, includeDeleteContents);
		if (question != null && !ProductUtil.invalid(question)) {
			if(question.isWithdraw()){
				return null;
			} else {
				SearchResult<QuestionDO> list = new SearchResult<QuestionDO>(
						1, new ArrayList<QuestionDO>());
				list.getDocuments().add(question);
				SearchResult<QuestionSetVO> questionSetVOs = createQuestionSets(list);
				if (questionSetVOs.getDocuments().isEmpty())
					return null;
				return questionSetVOs.getDocuments().get(0);
			}
		} else {
			return null;
		}
	}
	
	/**
	 * 指定した質問以外で、指定した商品の盛り上がっている質問を盛り上がり順
	 * （降順）に返します。
	 * @param sku SKU
	 * @param excudeQuestionId 除外する質問ID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 盛り上がっている質問一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<SimpleQuestionSetVO> findPopularQuestionExcudeQuestionId(
			String sku,
			String excudeQuestionId,
			int limit,
			int offset) {
		SearchResult<QuestionDO> searchResult = questionDao.findPopularQuestionExcudeQuestionId(
				sku, excudeQuestionId, limit, offset);
		SearchResult<SimpleQuestionSetVO> result
				= new SearchResult<SimpleQuestionSetVO>(0, new ArrayList<SimpleQuestionSetVO>());
		result.setHasAdult(searchResult.isHasAdult());
		result.setNumFound(searchResult.getNumFound());
		List<String> questionIds = new ArrayList<String>();
		for (QuestionDO question : searchResult.getDocuments()) {
			questionIds.add(question.getQuestionId());
		}
		//質問回答者数
		Map<String, Long> answerCountMap = questionAnswerDao.loadQuestionAnswerCountMapByQuestionId(
				questionIds);
		Map<String, Long> commentCountMap = commentDao.loadQuestionCommentCountMap(questionIds);
		Map<String, Long> likeCountMap = likeDao.loadQuestionLikeCountMap(questionIds);
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(searchResult.getDocuments());
		String communityUserId = requestScopeDao.loadCommunityUserId();
		for (QuestionDO question : searchResult.getDocuments()) {
			SimpleQuestionSetVO vo = new SimpleQuestionSetVO();
			String questionId = question.getQuestionId();
			vo.setQuestion(question);
			if (answerCountMap.containsKey(questionId)) {
				vo.setAnswerCount(answerCountMap.get(questionId));
			}
			if (commentCountMap.containsKey(questionId)) {
				vo.setAnswerCommentCount(commentCountMap.get(questionId));
			}
			if (likeCountMap.containsKey(questionId)) {
				vo.setAnswerLikeCount(likeCountMap.get(questionId));
			}
			result.updateFirstAndLast(vo);
			if (question.isStop(communityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				continue;
			}
			result.getDocuments().add(vo);
		}

		return result;
	}

	/**
	 * 指定した質問以外で、指定した商品の質問を質問投稿日時順（降順）に返します。
	 * @param sku SKU
	 * @param excudeQuestionId 除外する質問ID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 新着質問一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<QuestionDO> findNewQuestionExcudeQuestionId(
			String sku,
			String excudeQuestionId,
			int limit,
			int offset) {
		SearchResult<QuestionDO> searchResult
				= questionDao.findNewQuestionExcudeQuestionId(
				sku, excudeQuestionId, limit, offset);
		SearchResult<QuestionDO> result = new SearchResult<QuestionDO>();
		result.setHasAdult(searchResult.isHasAdult());
		result.setNumFound(searchResult.getNumFound());
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(searchResult.getDocuments());
		String communityUserId = requestScopeDao.loadCommunityUserId();
		for (QuestionDO question : searchResult.getDocuments()) {
			result.updateFirstAndLast(question);
			if (question.isStop(communityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				continue;
			}
			result.getDocuments().add(question);
		}

		return result;
	}

	/**
	 * 指定したコミュニティユーザーが投稿した質問を質問投稿日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 質問一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<QuestionSetVO> findQuestionByCommunityUserId(
			String communityUserId,
			int limit,
			Date offsetTime,
			boolean previous) {
		return createQuestionSets(questionDao.findQuestionByCommunityUserId(
				communityUserId,
				null,
				limit,
				offsetTime,
				previous,
				requestScopeDao.loadAdultVerification()));
	}

	/**
	 * 指定したコミュニティユーザーが投稿した一時保存質問を質問保存日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 一時保存質問一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<QuestionSetVO> findTemporaryQuestionByCommunityUserId(
			String communityUserId, String excludeQuestionId, int limit, Date offsetTime, boolean previous) {
		return createQuestionSets(questionDao.findTemporaryQuestionByCommunityUserId(
				communityUserId,
				excludeQuestionId,
				limit,
				offsetTime,
				previous,
				requestScopeDao.loadAdultVerification()));
	}

	/**
	 * 指定したコミュニティユーザーが投稿した質問回答を回答投稿日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 質問回答一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<QuestionAnswerSetVO> findQuestionAnswerByCommunityUserId(
			String communityUserId, int limit, Date offsetTime, boolean previous) {
		return appendPurchaseProductForAnswer(createQuestionAnswerSets(
				questionAnswerDao.findQuestionAnswerByCommunityUserId(
				communityUserId,
				null, 
				limit, 
				offsetTime, 
				previous)));
	}

	/**
	 * 指定したコミュニティユーザーが投稿した一時保存質問回答を
	 * 回答保存日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 一時保存質問回答一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<QuestionAnswerSetVO> findTemporaryQuestionAnswerByCommunityUserId(
			String communityUserId, String excludeQuestionId, int limit, Date offsetTime, boolean previous) {
		return appendPurchaseProductForAnswer(createQuestionAnswerSets(
				questionAnswerDao.findTemporaryQuestionAnswerByCommunityUserId(
				communityUserId,
				excludeQuestionId,
				limit,
				offsetTime,
				previous)));
	}

	/**
	 * 指定したユーザーの購入商品の新着Q&A情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 購入商品の新着Q&A情報
	 */
	@Override
	@ArroundSolr
	public SearchResult<QuestionSetVO> findNewQuestionByPurchaseProduct(
			String communityUserId, int limit, Date offsetTime, boolean previous) {

		return createQuestionSets(
			questionDao.findNewQuestionByPurchaseProduct(
					communityUserId, limit, offsetTime, previous));
	}

	/**
	 * 指定したコミュニティユーザーが、指定した商品に対して投稿した一時保存中の
	 * 質問を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku SKU
	 * @return 一時保存質問
	 */
	@Override
	@ArroundHBase
	public QuestionDO getTemporaryQuestion(String communityUserId, String sku) {
		for (QuestionDO question : questionDao.findQuestionByCommunityUserIdAndSKU(communityUserId, sku)) {
			if (question.getStatus().equals(ContentsStatus.SAVE) && !question.isDeleted()) {
				if( question.getSaveImages() == null || question.getSaveImages().isEmpty() ){
					List<SaveImageDO> saveImages = Lists.newArrayList();
					if( question.getUploadImageIds() != null && !question.getUploadImageIds().isEmpty()){
						for( String imageId : question.getUploadImageIds()){
							saveImages.add(new SaveImageDO(imageId, null));
						}
					}
					question.setSaveImages(saveImages);
				}
				return question;
			}
		}
		return null;
	}

	/**
	 * 指定した質問を返します。
	 * @param questionId 質問ID
	 * @return 質問
	 */
	@Override
	@ArroundHBase
	public QuestionSetVO getQuestion(String questionId) {
		QuestionSetVO result = null;
		QuestionDO question = questionDao.loadQuestion(questionId);
		if (question != null && !question.isDeleted() && !ProductUtil.invalid(question)) {
			SearchResult<QuestionDO> list = new SearchResult<QuestionDO>(1, new ArrayList<QuestionDO>());
			list.getDocuments().add(question);
			if( ContentsStatus.SUBMITTED.equals(question.getStatus())){
				SearchResult<QuestionSetVO> questionSetVOs = createQuestionSets(list);
				if (!questionSetVOs.getDocuments().isEmpty()){
					result = questionSetVOs.getDocuments().get(0);
				}
			}else{
				result = new QuestionSetVO();
				result.setQuestion(question);
			}
		}
		
		return result;
	}

	/**
	 * 質問情報を登録します。
	 * @param question 質問情報
	 * @return 登録した質問情報
	 */
	@Override
	@ArroundHBase  @ArroundSolr(commit = SolrTiming.NONE)
	public QuestionDO addQuestion(QuestionDO question) {
		// コンテンツ投稿可能チェック
		if(!userService.validateUserStatusForPostContents(question.getCommunityUser().getCommunityUserId()))
			throw new UnActiveException("can not post contens because user status is failure " + " communityUserId:" + question.getCommunityUser().getCommunityUserId());
		
		if( !ContentsStatus.SUBMITTED.equals(question.getStatus()))
			throw new IllegalArgumentException("Question status not sasubmitted.");
		// コミュニティユーザー情報を取得して設定します。
		settingProductAndCommunityUser(question);
				
		if (StringUtils.isEmpty(question.getQuestionId())) {
			question.setQuestionId(null);
		}
		
		QuestionDO saveQuestion = null;

		if (StringUtils.isNotEmpty(question.getQuestionId())) {
			saveQuestion = questionDao.loadQuestion(
					question.getQuestionId(), 
					Path.includeProp("*").includePath(
							"product.sku,communityUser.communityUserId").depth(1), true);
			if (saveQuestion == null) {
				throw new DataNotFoundException("This question was deleted. questionId = " + question.getQuestionId());
			}
			// 投稿ユーザーチェック
			if (!question.getCommunityUser().getCommunityUserId().equals(saveQuestion.getCommunityUser().getCommunityUserId())) {
				throw new SecurityException(
						"This question is different owner. ownerId = " +
								saveQuestion.getCommunityUser().getCommunityUserId() +
						" input = " + question.getCommunityUser().getCommunityUserId());
			}
			// 投稿ユーザーチェック
			if (saveQuestion.isDeleted()) {
				throw new DataNotFoundException("This question was deleted. questionId = " + question.getQuestionId());
			}
			if (ContentsStatus.SUBMITTED.equals(saveQuestion.getStatus())) {
				throw new SecurityException("already submitted. questionId = " + question.getQuestionId());
			}
		}
		
		applicationLockDao.lockForSaveQuestion(
				question.getProduct().getSku(),
				question.getCommunityUser().getCommunityUserId());
		
		question.setPostDate(timestampHolder.getTimestamp());
		
		if( saveQuestion != null ){
			question.setRegisterDateTime(saveQuestion.getRegisterDateTime());
		}else{
			question.setRegisterDateTime(timestampHolder.getTimestamp());
		}
		
		Map<String, ImageHeaderDO> uploadImageMap = Maps.newHashMap();
		List<SaveImageDO> saveImages = question.getSaveImages();
		if (saveImages != null && !saveImages.isEmpty()){
			updateImageRelateContents(
					question.getCommunityUser().getCommunityUserId(),
					question.getProduct().getSku(),
					PostContentType.QUESTION,
					question.isAdult(),
					question,
					saveImages,
					uploadImageMap);
			// 保存から投稿した場合、下記項目は初期化する。
			question.setUploadImageIds(null);
			question.setSaveImages(null);
		}
		
		questionDao.saveQuestion(question);
		
		for (ImageHeaderDO imageHeader : uploadImageMap.values()) {
			imageHeader.setQuestion(question);
			if (imageHeader.getTempThumbnailImage() != null) {
				imageHeader.getTempThumbnailImage().setQuestion(question);
			}
			imageDao.saveUploadImageHeader(imageHeader);
		}
		
		List<ActionHistoryDO> actionHistories = new ArrayList<ActionHistoryDO>();
		//自身のフォローユーザーに向けて、アクション履歴を記録します。
		ActionHistoryDO userActionHistory = new ActionHistoryDO();
		userActionHistory.setActionHistoryType(ActionHistoryType.USER_QUESTION);
		userActionHistory.setCommunityUser(question.getCommunityUser());
		userActionHistory.setQuestion(question);
		userActionHistory.setProduct(question.getProduct());
		userActionHistory.setAdult(question.isAdult());
		actionHistories.add(userActionHistory);
		//商品に対してアクションを記録します。
		ActionHistoryDO productActionHistory = new ActionHistoryDO();
		productActionHistory.setActionHistoryType(ActionHistoryType.PRODUCT_QUESTION);
		productActionHistory.setCommunityUser(question.getCommunityUser());
		productActionHistory.setProduct(question.getProduct());
		productActionHistory.setQuestion(question);
		productActionHistory.setAdult(question.isAdult());
		actionHistories.add(productActionHistory);
		actionHistoryDao.create(actionHistories);
		String userActionHistoryId = userActionHistory.getActionHistoryId();
		String productActionHistoryId = productActionHistory.getActionHistoryId();
		// インデックス更新
		indexService.updateIndexForSaveQuestion(
				question.getQuestionId(),
				uploadImageMap.keySet().toArray(new String[uploadImageMap.size()]),
				userActionHistoryId,
				productActionHistoryId);
		
		mailService.sendNotifyMailForJustAfterQuestionSubmit(
				question.getQuestionId(), question.getProduct().getSku(),
				question.getCommunityUser().getCommunityUserId());
		socialMediaService.notifySocialMediaForQuestionSubmit(question.getQuestionId(),
				question.getCommunityUser().getCommunityUserId());
		
		return question;
	}
	
	/**
	 * 質問情報を更新します。
	 * @param question 質問情報
	 * @return 登録した質問情報
	 */
	@Override
	@ArroundHBase @ArroundSolr(commit = SolrTiming.NONE)
	public QuestionDO modifyQuestion(QuestionDO question) {
		if (StringUtils.isEmpty(question.getQuestionId())) {
			throw new InputException("QuestionId is none");
		}
		// コンテンツ投稿可能チェック
		if(!userService.validateUserStatusForPostContents(question.getCommunityUser().getCommunityUserId()))
			throw new UnActiveException("can not post contens because user status is failure " + " communityUserId:" + question.getCommunityUser().getCommunityUserId());
		// ステータスチェック
		if (!ContentsStatus.SUBMITTED.equals(question.getStatus())){
			throw new DataNotFoundException(
					"This question not submitted. questionId = " + question.getQuestionId());
		}
		// コミュニティユーザー情報を取得して設定します。
		settingProductAndCommunityUser(question);
		
		QuestionDO submitQuestion = questionDao.loadQuestion(
				question.getQuestionId(), Path.includeProp(
						"questionBody,status,communityUserId,withdraw,postDate,registerDateTime"), true);
		if (submitQuestion == null) {
			throw new DataNotFoundException("This question was deleted. questionId = " + question.getQuestionId());
		}
		if (!question.getCommunityUser().getCommunityUserId().equals(submitQuestion.getCommunityUser().getCommunityUserId())) {
			throw new SecurityException(
					"This question is different owner. ownerId = " +
							submitQuestion.getCommunityUser().getCommunityUserId() +
					" input = " + question.getCommunityUser().getCommunityUserId());
		}
		if (submitQuestion.isDeleted()) {
			throw new DataNotFoundException("This question was deleted. questionId = " + question.getQuestionId());
		}
		if (!ContentsStatus.SUBMITTED.equals(submitQuestion.getStatus())) {
			throw new SecurityException("not submitted. questionId = " + question.getQuestionId());
		}
		// 更新範囲内かどうかチェック
		if( !checkPostImmediatelyAfter(submitQuestion.getPostDate())){
			throw new UnActiveException("Past Modify period. questionId = " + question.getQuestionId());
		}
		
		applicationLockDao.lockForSaveQuestion(
				question.getProduct().getSku(),
				question.getCommunityUser().getCommunityUserId());
		// question.adult
		// question.product
		// question.communityUser
		// は、引数のquestionに設定されているので更新しない
		// 更新しない項目をコピーする。
		question.setStatus(submitQuestion.getStatus());
		question.setQuestionScore(submitQuestion.getQuestionScore());
		question.setViewCount(submitQuestion.getViewCount());
		question.setSaveDate(submitQuestion.getSaveDate());
		question.setPostDate(submitQuestion.getPostDate());
		question.setDeleteDate(submitQuestion.getDeleteDate());
		question.setLastAnswerDate(submitQuestion.getLastAnswerDate());
		question.setWithdraw(submitQuestion.isWithdraw());
		question.setWithdrawKey(submitQuestion.getWithdrawKey());
		question.setMngToolOperation(submitQuestion.isMngToolOperation());
		question.setRegisterDateTime(submitQuestion.getRegisterDateTime());
		
		// 画像の比較
		// ・既にアップロードしている画像はそのままにする。
		// ・削除されている画像は削除フラグを立てる。
		// ・新規に投稿された画像は新規に追加する。
		// ・質問情報の画像一覧を更新する。
		// ・画像のキャプションを更新する。
		Map<String, ImageHeaderDO> uploadImageMap = Maps.newHashMap();
		List<String> updateImageIds = Lists.newArrayList();
		//　画像更新処理
		befoureSaveContentModifyImages(
				question, 
				PostContentType.QUESTION, 
				ImageTargetType.QUESTION, 
				uploadImageMap, 
				updateImageIds);
		// 質問更新
		questionDao.saveQuestion(question);
		// 画像後処理（テンポラリーの画像の後処理）
		for (ImageHeaderDO imageHeader : uploadImageMap.values()) {
			imageHeader.setQuestion(question);
			if (imageHeader.getTempThumbnailImage() != null) {
				imageHeader.getTempThumbnailImage().setQuestion(question);
			}
			imageDao.saveUploadImageHeader(imageHeader);
		}
		// 質問、画像インデックスの更新
		indexService.updateIndexForSaveQuestion(
				question.getQuestionId(),
				updateImageIds.toArray(new String[updateImageIds.size()]),
				null,
				null);
		return question;
	}
	
	/**
	 * 質問情報を登録します。
	 * @param question 質問情報
	 * @return 登録した質問情報
	 */
	@Override
	@ArroundHBase  @ArroundSolr(commit = SolrTiming.NONE)
	public QuestionDO saveQuestion(QuestionDO question) {
		// コンテンツ投稿可能チェック
		if(!userService.validateUserStatusForPostContents(question.getCommunityUser().getCommunityUserId()))
			throw new UnActiveException("can not post contens because user status is failure " + " communityUserId:" + question.getCommunityUser().getCommunityUserId());
		
		if( !ContentsStatus.SAVE.equals(question.getStatus()))
			throw new IllegalArgumentException("Question status not save.");
		// 商品情報とコミュニティユーザー情報のチェック
		settingProductAndCommunityUser(question);
		
		if (StringUtils.isEmpty(question.getQuestionId())) {
			question.setQuestionId(null);
		}
		
		QuestionDO savedQuestion = null;
		// 既に保存されている場合のチェック
		if (StringUtils.isNotEmpty(question.getQuestionId())) {
			savedQuestion = questionDao.loadQuestion(
					question.getQuestionId(), Path.includeProp("*").includePath(
							"product.sku,communityUser.communityUserId").depth(1), true);
			if (savedQuestion == null) {
				throw new DataNotFoundException("This question was deleted. questionId = " + question.getQuestionId());
			}
			if (!question.getCommunityUser().getCommunityUserId().equals(savedQuestion.getCommunityUser().getCommunityUserId())) {
				throw new SecurityException(
						"This question is different owner. ownerId = " +
								savedQuestion.getCommunityUser().getCommunityUserId() +
						" input = " + question.getCommunityUser().getCommunityUserId());
			}
			if (savedQuestion.isDeleted()) {
				throw new DataNotFoundException("This question was deleted. questionId = " + question.getQuestionId());
			}
			
			if (ContentsStatus.SUBMITTED.equals(savedQuestion.getStatus())) {
				throw new SecurityException("already submitted. questionId = " + question.getQuestionId());
			}
		} else {
			// 保存質問重複チェック
			List<QuestionDO> findQuestions = questionDao.findQuestionByCommunityUserIdAndSKU(
					question.getCommunityUser().getCommunityUserId(),
					question.getProduct().getSku());
			
			for (QuestionDO findQuestion : findQuestions) {
				// 既に保存レビューが登録されている場合は、重複エラーとする。
				if( !ContentsStatus.SAVE.equals(findQuestion.getStatus()) )
					continue;
				
				throw new SecurityException("duplicate save question. exists questionId = " + findQuestion.getQuestionId());
			}
		}
		
		if( savedQuestion != null ){
			question.setPostDate(savedQuestion.getPostDate());
			question.setRegisterDateTime(savedQuestion.getRegisterDateTime());
		}else{
			question.setPostDate(timestampHolder.getTimestamp());
			question.setRegisterDateTime(timestampHolder.getTimestamp());
		}
		
		applicationLockDao.lockForSaveQuestion(
				question.getProduct().getSku(),
				question.getCommunityUser().getCommunityUserId());
		
		questionDao.saveQuestion(question);
		
		indexService.updateIndexForSaveQuestion(
				question.getQuestionId(),
				null,
				null,
				null);
		
		return question;
	}
	
	@Override
	@ArroundHBase
	public void deleteQuestion(String questionId) {
		deleteQuestion(questionId, false);
	}

	/**
	 * 指定した質問を削除します。
	 * @param questionId 質問ID
	 */
	@Override
	@ArroundHBase
	public void deleteQuestion(String questionId, boolean mngToolOperation){
		QuestionDO question = questionDao.loadQuestion(
				questionId, Path.includeProp("questionBody,communityUserId,status")
				.includePath("imageHeaders.imageId").depth(1), true);
		if (question == null) {
			return;
		}

		if(!mngToolOperation){
			String accessUserId = requestScopeDao.loadCommunityUserId();
			if (!question.getCommunityUser().getCommunityUserId().equals(accessUserId)) {
				throw new SecurityException(
						"This question is different owner. ownerId = " +
						question.getCommunityUser().getCommunityUserId() +
						" input = " + accessUserId);
			}
		}
		if (question.getStatus().equals(ContentsStatus.DELETE)) {
			return;
		}

		if(!mngToolOperation){
			// コンテンツ投稿可能チェック
			if(!userService.validateUserStatusForPostContents(question.getCommunityUser().getCommunityUserId()))
				throw new UnActiveException("can not post contens because user status is failure " + " communityUserId:" + question.getCommunityUser().getCommunityUserId());
		}

		Set<String> updateImageIds = new HashSet<String>();
		imageService.deleteImagesInContents(
				question.getCommunityUser().getCommunityUserId(),
				PostContentType.QUESTION, questionId, question, updateImageIds);
		boolean logical = (question.getStatus().equals(ContentsStatus.SUBMITTED) || question.getStatus().equals(ContentsStatus.CONTENTS_STOP));
		questionDao.deleteQuestion(questionId, logical, mngToolOperation);

		if (logical) {
			indexService.updateIndexForSaveQuestion(
					questionId,
					updateImageIds.toArray(new String[updateImageIds.size()]),
					null, null);
			imageService.deleteQuestionAnswerImageByQuestionId(questionId);
		} else {
			indexService.updateIndexForSaveQuestion(
					questionId,
					null,
					null, null);
		}
	}

	/**
	 * 指定した質問回答をインデックス情報から返します。
	 * @param questionAnswerId 質問回答ID
	 * @return 質問回答
	 */
	@Override
	@ArroundSolr
	public QuestionAnswerSetVO getQuestionAnswerFromIndex(String questionAnswerId, boolean includeDeleteContents) {
		QuestionAnswerDO questionAnswer = questionAnswerDao.loadQuestionAnswerFromIndex(questionAnswerId, includeDeleteContents);
		if (questionAnswer == null || ProductUtil.invalid(questionAnswer)) {
			return null;
		}
		SearchResult<QuestionAnswerDO> answers = new SearchResult<QuestionAnswerDO>(1, new ArrayList<QuestionAnswerDO>());
		answers.getDocuments().add(questionAnswer);

		SearchResult<QuestionAnswerSetVO> answerSets = createQuestionAnswerSets(answers);
		if (answerSets.getDocuments().isEmpty()) {
			return null;
		}
		return answerSets.getDocuments().get(0);
	}

	/**
	 * 指定した質問回答を返します。
	 * @param questionAnswerId 質問回答ID
	 * @return 質問回答
	 */
	@Override
	@ArroundHBase
	public QuestionAnswerSetVO getQuestionAnswer(String questionAnswerId) {
		QuestionAnswerSetVO result = null;
		QuestionAnswerDO questionAnswer = questionAnswerDao.loadQuestionAnswer(questionAnswerId);
		if (questionAnswer != null && !questionAnswer.isDeleted() && !ProductUtil.invalid(questionAnswer)) {
			SearchResult<QuestionAnswerDO> answers = new SearchResult<QuestionAnswerDO>(1, new ArrayList<QuestionAnswerDO>());
			answers.getDocuments().add(questionAnswer);
			SearchResult<QuestionAnswerSetVO> answerSets = createQuestionAnswerSets(answers);
			if (!answerSets.getDocuments().isEmpty()) {
				result = answerSets.getDocuments().get(0);
			}
		}
		
		return result;
	}

	/**
	 * 指定の質問に対して、指定したコミュニティユーザーが回答しているかどうか
	 * 返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param questionId 質問ID
	 * @return 回答している場合、true
	 */
	@Override
	@ArroundHBase
	public boolean hasQuestionAnswer(String communityUserId, String questionId) {
		for (QuestionAnswerDO questionAnswer : questionAnswerDao.findQuestionAnswerByCommunityUserIdAndQuestionId(
				communityUserId, questionId,
				Path.includeProp("communityUserId,questionId,status,withdraw"))) {
			if ((questionAnswer.getStatus().equals(ContentsStatus.SUBMITTED) || questionAnswer.getStatus().equals(ContentsStatus.CONTENTS_STOP)) && !questionAnswer.isDeleted()) {
				return true;
			}
		}
		return false;
	}
	

	/**
	 * 指定した質問に対する、指定したコミュニティユーザーが一時保存した回答情報
	 * を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param questionId 質問ID
	 * @return 一時保存された質問回答情報
	 */
	@Override
	@ArroundHBase
	public QuestionAnswerDO getTemporaryQuestionAnswer(
			String communityUserId, String questionId) {
		for (QuestionAnswerDO questionAnswer : questionAnswerDao.findQuestionAnswerByCommunityUserIdAndQuestionId(communityUserId, questionId)) {
			if (questionAnswer.getStatus().equals(ContentsStatus.SAVE) && !questionAnswer.isDeleted()) {
				return questionAnswer;
			}
		}
		return null;
	}
	
	/**
	 * 指定した商品、コミュニティユーザーに対する、一時保存した最新の回答情報を取得する。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku 商品SKU
	 * @return 最新の一時保存回答情報
	 */
	@Override
	@ArroundHBase
	public QuestionAnswerDO getNewTemporaryQuestionAnswerBySku(String communityUserId, String sku) {
		return questionAnswerDao.getNewSaveQuestionAnswerByCommunityUserId(communityUserId, sku);
	}
	
	// TODO 直近1時間以内の回答を取得する。
	
	// TODO 過去に投稿した一番最新の回答を取得する。
	
	/**
	 * 質問回答情報を登録/更新します。
	 * @param questionAnswer 質問回答情報
	 * @return 登録された質問回答情報
	 */
	@Override
	@ArroundSolr
	@ArroundHBase
	public QuestionAnswerDO addQuestionAnswer(QuestionAnswerDO questionAnswer) {
		// コンテンツ投稿可能チェック
		if(!userService.validateUserStatusForPostContents(questionAnswer.getCommunityUser().getCommunityUserId()))
			throw new UnActiveException("can not post contens because user status is failure " + " communityUserId:" + questionAnswer.getCommunityUser().getCommunityUserId());
		// ステータスチェック
		if( !ContentsStatus.SUBMITTED.equals(questionAnswer.getStatus()))
			throw new IllegalArgumentException("Review status not sasubmitted.");
		// 質問情報チェック
		if (questionAnswer.getQuestion() == null || questionAnswer.getQuestion().getQuestionId() == null) {
			throw new IllegalArgumentException("Question Id required.");
		}
		// 親コンテンツが削除済みまたは親コンテンツ持ち主が一時退会
		QuestionDO question = questionDao.loadQuestion(
				questionAnswer.getQuestion().getQuestionId(),
				Path.includeProp("*").includePath("product.sku,communityUser.communityUserId,communityUser.status,communityUser.keepQuestionContents").depth(1),
				false);
		if(question == null || (!CommunityUserStatus.ACTIVE.equals(question.getCommunityUser().getStatus()) && !question.getCommunityUser().isKeepQuestionContents())){
			throw new UnActiveException("can not post contens because user status is failure " + " questionId:" 
			+ (question != null ? question.getQuestionId() : "none") 
			+ " communityUserId:" + (question != null ? (question.getCommunityUser() != null ? question.getCommunityUser().getCommunityUserId() : "none") : "none"));
		}
		
		questionAnswer.setQuestion(question);
		
		if (questionAnswer.getQuestion().getCommunityUser().getCommunityUserId().equals((questionAnswer.getCommunityUser().getCommunityUserId()))) {
			throw new IllegalArgumentException("AnswerUser and QuestionUser are the same users.");
		}
		
		questionAnswer.setRelationQuestionOwnerId(questionAnswer.getQuestion().getCommunityUser().getCommunityUserId());
		
		// 商品情報とコミュニティユーザー情報のチェック
		settingProductAndCommunityUser(questionAnswer);
		
		if (StringUtils.isEmpty(questionAnswer.getQuestionAnswerId())) {
			questionAnswer.setQuestionAnswerId(null);
		}
		
		applicationLockDao.lockForSaveQuestionAnswer(
				questionAnswer.getQuestion().getQuestionId(),
				questionAnswer.getCommunityUser().getCommunityUserId());
		
		QuestionAnswerDO saveQuestionAnswer = null;
		
		if (StringUtils.isNotEmpty(questionAnswer.getQuestionAnswerId())) {
			saveQuestionAnswer = questionAnswerDao.loadQuestionAnswer(
					questionAnswer.getQuestionAnswerId(), Path.includeProp(
							"answerBody,status,purchaseDate,purchaseHistoryType" +
							",communityUserId,withdraw,postDate,registerDateTime"), true);
			if (saveQuestionAnswer == null) {
				throw new DataNotFoundException("This questionAnswer was deleted. questionAnswerId = " + questionAnswer.getQuestionAnswerId());
			}
			if (!questionAnswer.getCommunityUser().getCommunityUserId().equals(saveQuestionAnswer.getCommunityUser().getCommunityUserId())) {
				throw new SecurityException(
						"This questionAnswer is different owner. ownerId = " +
								saveQuestionAnswer.getCommunityUser().getCommunityUserId() +
						" input = " + questionAnswer.getCommunityUser().getCommunityUserId());
			}
			if (saveQuestionAnswer.isDeleted()) {
				throw new DataNotFoundException(
						"This questionAnswer was deleted. questionAnswerId = "
						+ questionAnswer.getQuestionAnswerId());
			}
			if (ContentsStatus.SUBMITTED.equals(saveQuestionAnswer.getStatus())) {
				throw new SecurityException("already submitted. answerId = " + questionAnswer.getQuestionAnswerId());
			}
		}
		
		//購入日時は購入履歴情報から取得して、セットします。
		String purchaseProductId = settingPurchaseProduct(questionAnswer);
		
		questionAnswer.setPostDate(timestampHolder.getTimestamp());
		
		if( saveQuestionAnswer != null){
			questionAnswer.setRegisterDateTime(saveQuestionAnswer.getRegisterDateTime());
		}else{
			questionAnswer.setRegisterDateTime(timestampHolder.getTimestamp());
		}
		
		// イメージの登録処理
		Map<String, ImageHeaderDO> uploadImageMap= new HashMap<String, ImageHeaderDO>();
		List<SaveImageDO> saveImages = questionAnswer.getSaveImages();
		if (saveImages != null && !saveImages.isEmpty()){
			updateImageRelateContents(
					questionAnswer.getCommunityUser().getCommunityUserId(),
					questionAnswer.getProduct().getSku(),
					PostContentType.ANSWER,
					questionAnswer.isAdult(),
					questionAnswer,
					saveImages,
					uploadImageMap);
			// 保存から投稿した場合、下記項目は初期化する。
			questionAnswer.setUploadImageIds(null);
			questionAnswer.setSaveImages(null);
		}
		// 回答情報登録処理
		questionAnswerDao.saveQuestionAnswer(questionAnswer);
		//　質問情報の更新
		questionDao.updateQuestionLastAnswerDate(
				questionAnswer.getQuestion().getQuestionId(),
				questionAnswer.getPostDate());
		// 画像データの後処理
		String questionCommunityUserId = questionAnswer.getQuestion().getCommunityUser().getCommunityUserId();
		for (ImageHeaderDO imageHeader : uploadImageMap.values()) {
			imageHeader.setQuestionAnswer(questionAnswer);
			imageHeader.setQuestion(questionAnswer.getQuestion());
			imageHeader.setRelationQuestionOwnerId(questionCommunityUserId);
			if (imageHeader.getTempThumbnailImage() != null) {
				imageHeader.getTempThumbnailImage().setQuestionAnswer(questionAnswer);
				imageHeader.getTempThumbnailImage().setQuestion(questionAnswer.getQuestion());
				imageHeader.getTempThumbnailImage().setRelationQuestionOwnerId(questionCommunityUserId);
			}
			imageDao.saveUploadImageHeader(imageHeader);
		}
		//自身のフォローユーザーに向けて、アクション履歴を記録します。
		List<ActionHistoryDO> actionHistories = Lists.newArrayList();
		ActionHistoryDO userActionHistory = new ActionHistoryDO();
		userActionHistory.setActionHistoryType(ActionHistoryType.USER_ANSWER);
		userActionHistory.setCommunityUser(questionAnswer.getCommunityUser());
		userActionHistory.setProduct(questionAnswer.getProduct());
		userActionHistory.setQuestion(questionAnswer.getQuestion());
		userActionHistory.setQuestionAnswer(questionAnswer);
		userActionHistory.setAdult(questionAnswer.isAdult());
		userActionHistory.setRelationQuestionOwnerId(questionCommunityUserId);
		actionHistories.add(userActionHistory);
		//商品に対してアクションを記録します。
		ActionHistoryDO productActionHistory = new ActionHistoryDO();
		productActionHistory.setActionHistoryType(ActionHistoryType.PRODUCT_ANSWER);
		productActionHistory.setCommunityUser(questionAnswer.getCommunityUser());
		productActionHistory.setProduct(questionAnswer.getProduct());
		productActionHistory.setQuestion(questionAnswer.getQuestion());
		productActionHistory.setQuestionAnswer(questionAnswer);
		productActionHistory.setAdult(questionAnswer.isAdult());
		productActionHistory.setRelationQuestionOwnerId(questionCommunityUserId);
		actionHistories.add(productActionHistory);
		//質問に対してアクションを記録します。
		ActionHistoryDO questionActionHistory = new ActionHistoryDO();
		questionActionHistory.setActionHistoryType(ActionHistoryType.QUESTION_ANSWER);
		questionActionHistory.setCommunityUser(questionAnswer.getCommunityUser());
		questionActionHistory.setProduct(questionAnswer.getProduct());
		questionActionHistory.setQuestion(questionAnswer.getQuestion());
		questionActionHistory.setQuestionAnswer(questionAnswer);
		questionActionHistory.setAdult(questionAnswer.isAdult());
		questionActionHistory.setRelationQuestionOwnerId(questionCommunityUserId);
		actionHistories.add(questionActionHistory);
		actionHistoryDao.create(actionHistories);
		String userActionHistoryId = userActionHistory.getActionHistoryId();
		String productActionHistoryId = productActionHistory.getActionHistoryId();
		String questionActionHistoryId = questionActionHistory.getActionHistoryId();
		
		String answerCommunityUserId = questionAnswer.getCommunityUser().getCommunityUserId();
		String informationId = null;
		if(!question.getCommunityUser().getCommunityUserId().equals(answerCommunityUserId)){
			InformationDO information = new InformationDO();
			information.setInformationType(InformationType.QUESTION_ANSWER_ADD);
			information.setCommunityUser(question.getCommunityUser());
			information.setQuestionAnswer(questionAnswer);
			information.setAdult(questionAnswer.isAdult());
			information.setQuestion(question);
			information.setRelationQuestionAnswerOwnerId(answerCommunityUserId);
			information.setRelationCommunityUserId(answerCommunityUserId);
			informationDao.createInformation(information);
			informationId = information.getInformationId();
		}
		// インデックス更新
		indexService.updateIndexForSaveQuestionAnswer(
				questionAnswer.getQuestionAnswerId(),
				purchaseProductId,
				uploadImageMap.keySet().toArray(new String[uploadImageMap.size()]),
				userActionHistoryId,
				productActionHistoryId,
				questionActionHistoryId,
				informationId);
		
		mailService.sendNotifyMailForJustAfterQuestionAnswerSubmit(questionAnswer.getQuestionAnswerId());
		socialMediaService.notifySocialMediaForQuestionAnswerSubmit(
				questionAnswer.getQuestionAnswerId(),
				questionAnswer.getCommunityUser().getCommunityUserId());
			
		return questionAnswer;
	}

	/**
	 * 質問回答情報を登録/更新します。
	 * @param questionAnswer 質問回答情報
	 * @return 登録された質問回答情報
	 */
	@Override
	@ArroundSolr
	@ArroundHBase
	public QuestionAnswerDO modifyQuestionAnswer(QuestionAnswerDO questionAnswer) {
		if (StringUtils.isEmpty(questionAnswer.getQuestionAnswerId())) {
			throw new InputException("QuestionId is none");
		}
		// コンテンツ投稿可能チェック
		if(!userService.validateUserStatusForPostContents(questionAnswer.getCommunityUser().getCommunityUserId()))
			throw new UnActiveException("can not post contens because user status is failure " + " communityUserId:" + questionAnswer.getCommunityUser().getCommunityUserId());
		// ステータスチェック
		if (!ContentsStatus.SUBMITTED.equals(questionAnswer.getStatus())){
			throw new DataNotFoundException(
					"This question not submitted. questionId = " + questionAnswer.getQuestionAnswerId());
		}
		// 質問情報のチェック
		if (questionAnswer.getQuestion() == null || questionAnswer.getQuestion().getQuestionId() == null) {
			throw new IllegalArgumentException("Question Id required.");
		}
		// 親コンテンツが削除済みまたは親コンテンツ持ち主が一時退会
		QuestionDO question = questionDao.loadQuestion(
				questionAnswer.getQuestion().getQuestionId(),
				Path.includeProp("*").includePath("product.sku,communityUser.communityUserId,communityUser.status,communityUser.keepQuestionContents").depth(1),
				false);
		
		if(question == null || (!CommunityUserStatus.ACTIVE.equals(question.getCommunityUser().getStatus()) && !question.getCommunityUser().isKeepQuestionContents())){
			throw new UnActiveException("can not post contens because user status is failure " + " questionId:" 
			+ (question != null ? question.getQuestionId() : "none") 
			+ " communityUserId:" + (question != null ? (question.getCommunityUser() != null ? question.getCommunityUser().getCommunityUserId() : "none") : "none"));
		}
		
		questionAnswer.setQuestion(question);
		
		if (questionAnswer.getQuestion().getCommunityUser().getCommunityUserId().equals((questionAnswer.getCommunityUser().getCommunityUserId()))) {
			throw new IllegalArgumentException("AnswerUser and QuestionUser are the same users.");
		}
		
		// 商品情報とコミュニティユーザー情報のチェック
		settingProductAndCommunityUser(questionAnswer);
		
		QuestionAnswerDO submitQuestionAnswer = questionAnswerDao.loadQuestionAnswer(
				questionAnswer.getQuestionAnswerId(), Path.includeProp(
						"answerBody,status,purchaseDate,purchaseHistoryType" +
						",communityUserId,withdraw,postDate,registerDateTime"), true);
		
		if (submitQuestionAnswer == null) {
			throw new DataNotFoundException("This questionAnswer was deleted. questionAnswerId = " + questionAnswer.getQuestionAnswerId());
		}
		if (!questionAnswer.getCommunityUser().getCommunityUserId().equals(submitQuestionAnswer.getCommunityUser().getCommunityUserId())) {
			throw new SecurityException(
					"This questionAnswer is different owner. ownerId = " +
							submitQuestionAnswer.getCommunityUser().getCommunityUserId() +
					" input = " + questionAnswer.getCommunityUser().getCommunityUserId());
		}
		if (submitQuestionAnswer.isDeleted()) {
			throw new DataNotFoundException("This questionAnswer was deleted. questionAnswerId = " + questionAnswer.getQuestionAnswerId());
		}
		
		if (!ContentsStatus.SUBMITTED.equals(submitQuestionAnswer.getStatus())) {
			throw new SecurityException("not submitted. questionAnswerd = " + questionAnswer.getQuestionAnswerId());
		}
		// 編集可能チェック(投稿時間から1時間以上立っている場合は、更新させない。
		if( !checkPostImmediatelyAfter(submitQuestionAnswer.getPostDate())){
			throw new UnActiveException("Past Modify period. questionAnswerd = " + questionAnswer.getQuestionAnswerId());
		}
		
		applicationLockDao.lockForSaveQuestionAnswer(
				questionAnswer.getQuestion().getQuestionId(),
				questionAnswer.getCommunityUser().getCommunityUserId());
		
		// 更新しない項目をコピーする。
		questionAnswer.setPurchaseDate(submitQuestionAnswer.getPurchaseDate());
		questionAnswer.setPurchaseHistoryType(submitQuestionAnswer.getPurchaseHistoryType());
		questionAnswer.setQuestionAnswerScore(submitQuestionAnswer.getQuestionAnswerScore());
		questionAnswer.setStatus(submitQuestionAnswer.getStatus());
		questionAnswer.setSaveDate(submitQuestionAnswer.getSaveDate());
		questionAnswer.setPostDate(submitQuestionAnswer.getPostDate());
		questionAnswer.setDeleteDate(submitQuestionAnswer.getDeleteDate());
		questionAnswer.setRelationQuestionOwnerId(questionAnswer.getQuestion().getCommunityUser().getCommunityUserId());
		questionAnswer.setWithdraw(submitQuestionAnswer.isWithdraw());
		questionAnswer.setWithdrawKey(submitQuestionAnswer.getWithdrawKey());
		questionAnswer.setMngToolOperation(submitQuestionAnswer.isMngToolOperation());
		questionAnswer.setRegisterDateTime(submitQuestionAnswer.getRegisterDateTime());
		//購入日時は購入履歴情報から取得して、セットします。
		settingPurchaseProduct(questionAnswer);
		// 画像の比較
		// ・既にアップロードしている画像はそのままにする。
		// ・削除されている画像は削除フラグを立てる。
		// ・新規に投稿された画像は新規に追加する。
		// ・質問情報の画像一覧を更新する。
		// ・画像のキャプションを更新する。
		Map<String, ImageHeaderDO> uploadImageMap = Maps.newHashMap();
		List<String> updateImageIds = Lists.newArrayList();
		//　画像更新処理
		befoureSaveContentModifyImages(
				questionAnswer, 
				PostContentType.ANSWER, 
				ImageTargetType.QUESTION_ANSWER, 
				uploadImageMap, 
				updateImageIds);
		// 回答情報を更新する。
		questionAnswerDao.saveQuestionAnswer(questionAnswer);
		// 画像後処理（テンポラリーの画像の後処理）
		String questionCommunityUserId = questionAnswer.getQuestion().getCommunityUser().getCommunityUserId();
		for (ImageHeaderDO imageHeader : uploadImageMap.values()) {
			imageHeader.setQuestionAnswer(questionAnswer);
			imageHeader.setQuestion(questionAnswer.getQuestion());
			imageHeader.setRelationQuestionOwnerId(questionCommunityUserId);
			if (imageHeader.getTempThumbnailImage() != null) {
				imageHeader.getTempThumbnailImage().setQuestionAnswer(questionAnswer);
				imageHeader.getTempThumbnailImage().setQuestion(questionAnswer.getQuestion());
				imageHeader.getTempThumbnailImage().setRelationQuestionOwnerId(questionCommunityUserId);
			}
			imageDao.saveUploadImageHeader(imageHeader);
		}
		// インデックス更新処理
		indexService.updateIndexForSaveQuestionAnswer(
				questionAnswer.getQuestionAnswerId(),
				null,
				updateImageIds.toArray(new String[updateImageIds.size()]),
				null,
				null,
				null,
				null);
		
		return questionAnswer;
	}
	/**
	 * 質問回答情報を登録/更新します。
	 * @param questionAnswer 質問回答情報
	 * @return 登録された質問回答情報
	 */
	@Override
	@ArroundSolr
	@ArroundHBase
	public QuestionAnswerDO saveQuestionAnswer(QuestionAnswerDO questionAnswer) {
		// コンテンツ投稿可能チェック
		if(!userService.validateUserStatusForPostContents(questionAnswer.getCommunityUser().getCommunityUserId()))
			throw new UnActiveException("can not post contens because user status is failure " + " communityUserId:" + questionAnswer.getCommunityUser().getCommunityUserId());

		if (questionAnswer.getQuestion() == null || questionAnswer.getQuestion().getQuestionId() == null) {
			throw new IllegalArgumentException("Question Id required.");
		}
		// 親コンテンツが削除済みまたは親コンテンツ持ち主が一時退会
		QuestionDO question = questionDao.loadQuestion(
				questionAnswer.getQuestion().getQuestionId(),
				Path.includeProp("*").includePath("product.sku,communityUser.communityUserId,communityUser.status,communityUser.keepQuestionContents").depth(1),
				false);
		if(question == null || (!CommunityUserStatus.ACTIVE.equals(question.getCommunityUser().getStatus()) && !question.getCommunityUser().isKeepQuestionContents())){
			throw new UnActiveException("can not post contens because user status is failure " + " questionId:" 
			+ (question != null ? question.getQuestionId() : "none") 
			+ " communityUserId:" + (question != null ? (question.getCommunityUser() != null ? question.getCommunityUser().getCommunityUserId() : "none") : "none"));
		}
		
		questionAnswer.setQuestion(question);
		
		if (questionAnswer.getQuestion().getCommunityUser().getCommunityUserId().equals((questionAnswer.getCommunityUser().getCommunityUserId()))) {
			throw new IllegalArgumentException("AnswerUser and QuestionUser are the same users.");
		}
		
		questionAnswer.setRelationQuestionOwnerId(questionAnswer.getQuestion().getCommunityUser().getCommunityUserId());
		
		// 商品情報とコミュニティユーザー情報のチェック
		settingProductAndCommunityUser(questionAnswer);
		
		if (StringUtils.isEmpty(questionAnswer.getQuestionAnswerId())) {
			questionAnswer.setQuestionAnswerId(null);
		}
		
		questionAnswer.setQuestion(question);
		
		applicationLockDao.lockForSaveQuestionAnswer(
				questionAnswer.getQuestion().getQuestionId(),
				questionAnswer.getCommunityUser().getCommunityUserId());
		
		questionAnswer.setQuestion(question);
		questionAnswer.setProduct(question.getProduct());
		questionAnswer.setAdult(questionAnswer.getProduct().isAdult());
		questionAnswer.setRelationQuestionOwnerId(questionAnswer.getQuestion().getCommunityUser().getCommunityUserId());

		QuestionAnswerDO oldQuestionAnswer = null;

		if (StringUtils.isNotEmpty(questionAnswer.getQuestionAnswerId())) {
			oldQuestionAnswer = questionAnswerDao.loadQuestionAnswer(
					questionAnswer.getQuestionAnswerId(), Path.includeProp(
							"answerBody,status,purchaseDate,purchaseHistoryType" +
							",communityUserId,withdraw,postDate,registerDateTime"), true);
			if (oldQuestionAnswer == null) {
				throw new DataNotFoundException(
						"This questionAnswer was deleted. questionAnswerId = "
						+ questionAnswer.getQuestionAnswerId());
			}
			if (!questionAnswer.getCommunityUser().getCommunityUserId().equals(oldQuestionAnswer.getCommunityUser().getCommunityUserId())) {
				throw new SecurityException(
						"This questionAnswer is different owner. ownerId = " +
						oldQuestionAnswer.getCommunityUser().getCommunityUserId() +
						" input = " + questionAnswer.getCommunityUser().getCommunityUserId());
			}
			if (oldQuestionAnswer.isDeleted()) {
				throw new DataNotFoundException("This questionAnswer was deleted. questionAnswerId = "+ questionAnswer.getQuestionAnswerId());
			}
			if (oldQuestionAnswer.getStatus().equals(ContentsStatus.SUBMITTED)) {
				throw new SecurityException("already submitted. answerId = " + questionAnswer.getQuestionAnswerId());
			}
			
		}
		
		//購入日時は購入履歴情報から取得して、セットします。
		String purchaseProductId = settingPurchaseProduct(questionAnswer);
		
		if( oldQuestionAnswer != null ){
			questionAnswer.setPostDate(oldQuestionAnswer.getPostDate());
			questionAnswer.setRegisterDateTime(oldQuestionAnswer.getRegisterDateTime());
		}else{
			questionAnswer.setRegisterDateTime(timestampHolder.getTimestamp());
			questionAnswer.setPostDate(timestampHolder.getTimestamp());
		}
		
		Map<String, ImageHeaderDO> uploadImageMap= new HashMap<String, ImageHeaderDO>();
		List<SaveImageDO> saveImages = questionAnswer.getSaveImages();
		if (saveImages != null && !saveImages.isEmpty()){
			updateImageRelateContents(
					questionAnswer.getCommunityUser().getCommunityUserId(),
					questionAnswer.getProduct().getSku(),
					PostContentType.ANSWER,
					questionAnswer.isAdult(),
					questionAnswer,
					saveImages,
					uploadImageMap);
		}
		
		questionAnswerDao.saveQuestionAnswer(questionAnswer);
		
		String questionCommunityUserId = questionAnswer.getQuestion().getCommunityUser().getCommunityUserId();
		for (ImageHeaderDO imageHeader : uploadImageMap.values()) {
			imageHeader.setQuestionAnswer(questionAnswer);
			imageHeader.setQuestion(questionAnswer.getQuestion());
			imageHeader.setRelationQuestionOwnerId(questionCommunityUserId);
			if (imageHeader.getTempThumbnailImage() != null) {
				imageHeader.getTempThumbnailImage().setQuestionAnswer(questionAnswer);
				imageHeader.getTempThumbnailImage().setQuestion(questionAnswer.getQuestion());
				imageHeader.getTempThumbnailImage().setRelationQuestionOwnerId(questionCommunityUserId);
			}
			imageDao.saveUploadImageHeader(imageHeader);
		}
		
		indexService.updateIndexForSaveQuestionAnswer(
				questionAnswer.getQuestionAnswerId(),
				purchaseProductId,
				null,
				null,
				null,
				null,
				null);
		
		return questionAnswer;
	}

	/**
	 * 指定した質問回答を削除します。
	 * @param questionAnswerId 質問回答ID
	 */
	@Override
	@ArroundHBase
	public void deleteQuestionAnswer(String questionAnswerId) {
		deleteQuestionAnswer(questionAnswerId, false);
	}

	/**
	 * 指定した質問回答を削除します。
	 * @param questionAnswerId 質問回答ID
	 */
	@Override
	@ArroundHBase
	public void deleteQuestionAnswer(String questionAnswerId, boolean mngToolOperation) {
		QuestionAnswerDO questionAnswer = questionAnswerDao.loadQuestionAnswer(
				questionAnswerId,
				Path.includeProp("answerBody,communityUserId,status")
				.includePath("imageHeaders.imageId").depth(1), true);
		if (questionAnswer == null) {
			return;
		}
		if(!mngToolOperation){
			String accessUserId = requestScopeDao.loadCommunityUserId();
			if (!questionAnswer.getCommunityUser(
					).getCommunityUserId().equals(accessUserId)) {
				throw new SecurityException(
						"This questionAnswer is different owner. ownerId = " +
						questionAnswer.getCommunityUser().getCommunityUserId() +
						" input = " + accessUserId);
			}
		}
		if (questionAnswer.getStatus().equals(ContentsStatus.DELETE)) {
			return;
		}

		if(!mngToolOperation){
			// コンテンツ投稿可能チェック
			if(!userService.validateUserStatusForPostContents(questionAnswer.getCommunityUser().getCommunityUserId()))
				throw new UnActiveException("can not post contens because user status is failure " + " communityUserId:" + questionAnswer.getCommunityUser().getCommunityUserId());
		}

		Set<String> updateImageIds = new HashSet<String>();

		imageService.deleteImagesInContents(
				questionAnswer.getCommunityUser().getCommunityUserId(),
				PostContentType.ANSWER, questionAnswerId,
				questionAnswer, updateImageIds);
		boolean logical = (questionAnswer.getStatus().equals(ContentsStatus.SUBMITTED) || questionAnswer.getStatus().equals(ContentsStatus.CONTENTS_STOP));
		questionAnswerDao.deleteQuestionAnswer(
				questionAnswerId, logical, mngToolOperation);
		if (logical) {
			indexService.updateIndexForSaveQuestionAnswer(
					questionAnswer.getQuestionAnswerId(),
					null,
					updateImageIds.toArray(new String[updateImageIds.size()]),
					null,
					null,
					null,
					null);
		} else {
			indexService.updateIndexForSaveQuestionAnswer(
					questionAnswer.getQuestionAnswerId(),
					null,
					null,
					null,
					null,
					null,
					null);
		}
	}

	/**
	 * 質問情報のスコア情報と閲覧数を更新します。
	 * <p>呼び出し前後にupdateQuestionScoreAndViewCountForBatchStart(),updateQuestionScoreAndViewCountForBatchEnd()を実行しておくこと</p>
	 * @param targetDate 対象日付
	 * @param question 質問情報
	 * @param scoreFactor スコア係数
	 */
	@Override
	@ArroundSolr
	@ArroundHBase
	public void updateQuestionScoreAndViewCountForBatch(
			Date targetDate,
			QuestionDO question,
			ScoreFactorDO scoreFactor) {
		List<String> questionIds = new ArrayList<String>();
		long answerCount = 0;
		questionIds.add(question.getQuestionId());
		Map<String, Long> answerCountMap = questionAnswerDao.loadQuestionAnswerCountMapByQuestionId(questionIds);
		if (answerCountMap.containsKey(question.getQuestionId())) {
			answerCount = answerCountMap.get(question.getQuestionId());
		}
		long likeCount = likeDao.loadQuestionLikeCount(question.getQuestionId());
		long followerCount = questionFollowDao.findFollowerCommunityUserByQuestionId(
				question.getQuestionId(), 0, 0, true).getNumFound();
		long viewCount = uniqueUserViewCountDao.loadViewCountByContentsId(
				question.getQuestionId(),
				UniqueUserViewCountType.QUESTION,
				serviceConfig.readLimit);

		long contentBodyCount = 0;
		if(!StringUtils.isEmpty(question.getQuestionBody()))
			contentBodyCount = StringUtil.stripTags(question.getQuestionBody()).length();
		long contentImageCount = imageDao.countQuestionsImage(question.getQuestionId());

		BigDecimal score = null;
		Integer elapsedDays = null;
		if (question.getLastAnswerDate() != null) {
			elapsedDays = DateUtil.getElapsedDays(question.getLastAnswerDate());
			score = new BigDecimal((elapsedDays - 1) * -1).multiply(
					scoreFactor.getQuestionDay());
			score = score.add(new BigDecimal(answerCount).multiply(
					scoreFactor.getQuestionAnswerCount()));
			score = score.add(new BigDecimal(likeCount).multiply(
					scoreFactor.getQuestionLikeCount()));
			score = score.add(new BigDecimal(viewCount).multiply(
					scoreFactor.getQuestionViewCount()));
			score = score.add(new BigDecimal(followerCount).multiply(
					scoreFactor.getQuestionFollowerCount()));
		} else {
			score = new BigDecimal(0);
		}

		question.setQuestionScore(score.doubleValue());
		question.setViewCount(viewCount);
		questionDao.updateQuestionScoreAndViewCountWithIndexForBatch(question);

		DailyScoreFactorDO dailyScoreFactor = new DailyScoreFactorDO();
		dailyScoreFactor.setType(DailyScoreFactorType.QUESTION);
		dailyScoreFactor.setTargetDate(targetDate);
		dailyScoreFactor.setContentsId(question.getQuestionId());
		dailyScoreFactor.setSku(question.getProduct().getSku());
		dailyScoreFactor.setElapsedDays(elapsedDays);
		dailyScoreFactor.setAnswerCount(answerCount);
		dailyScoreFactor.setLikeCount(likeCount);
		dailyScoreFactor.setViewCount(viewCount);
		dailyScoreFactor.setFollowerCount(followerCount);

		dailyScoreFactor.setPostDate(question.getPostDate());
		dailyScoreFactor.setContentBodyCount(contentBodyCount);
		dailyScoreFactor.setContentImageCount(contentImageCount);

		dailyScoreFactorDao.createDailyScoreFactorForBatch(dailyScoreFactor);
	}
	@Override
	@ArroundSolr
	@ArroundHBase
	public void updateQuestionScoreAndViewCountForBatchBegin(int bulkSize) {
		questionDao.updateQuestionScoreAndViewCountWithIndexForBatchBegin(bulkSize);
		dailyScoreFactorDao.createDailyScoreFactorForBatchBegin(bulkSize);
	}
	@Override
	@ArroundSolr
	@ArroundHBase
	public void updateQuestionScoreAndViewCountForBatchEnd() {
		questionDao.updateQuestionScoreAndViewCountWithIndexForBatchEnd();
		dailyScoreFactorDao.createDailyScoreFactorForBatchEnd();
	}

	/**
	 * 質問回答情報のスコア情報を更新します。
	 * @param targetDate 対象日付
	 * @param questionAnswer 質問回答情報
	 * @param scoreFactor スコア係数
	 */
	@Override
	@ArroundSolr
	@ArroundHBase
	public void updateQuestionAnswerScoreForBatch(
			Date targetDate,
			QuestionAnswerDO questionAnswer,
			ScoreFactorDO scoreFactor) {
		List<String> questionAnswerIds = new ArrayList<String>();
		questionAnswerIds.add(questionAnswer.getQuestionAnswerId());
		long commentCount = 0;
		Map<String, Long> commentCountMap = commentDao.loadQuestionAnswerCommentCountMap(
				questionAnswerIds, questionAnswer.getCommunityUser().getCommunityUserId());
		if (commentCountMap.containsKey(questionAnswer.getQuestionAnswerId())) {
			commentCount = commentCountMap.get(questionAnswer.getQuestionAnswerId());
		}
		long likeCount = 0;
		Map<String, Long> likeCountMap = likeDao.loadQuestionAnswerLikeCountMap(questionAnswerIds);
		if (likeCountMap.containsKey(questionAnswer.getQuestionAnswerId())) {
			likeCount = likeCountMap.get(questionAnswer.getQuestionAnswerId());
		}
		long followerCount = communityUserFollowDao.findFollowerCommunityUserByCommunityUserId(
				questionAnswer.getCommunityUser().getCommunityUserId(), null, 0, 0).getNumFound();
		int elapsedDays = DateUtil.getElapsedDays(questionAnswer.getPostDate());

		long contentBodyCount = 0;
		if(!StringUtils.isEmpty(questionAnswer.getAnswerBody()))
			contentBodyCount = StringUtil.stripTags(questionAnswer.getAnswerBody()).length();
		long contentImageCount = imageDao.countQuestionAnswersImage(questionAnswer.getQuestionAnswerId());

		BigDecimal contentBodyScore = new BigDecimal("0");
		if(PointScoreTerm.SCORE_0_TO_99.getStartTerm() <= contentBodyCount
				&& contentBodyCount <= PointScoreTerm.SCORE_0_TO_99.getEndTerm()){
			contentBodyScore = scoreFactor.getQuestionAnswerContentsCountTerm0to99();
		}else if(PointScoreTerm.SCORE_100_TO_199.getStartTerm() <= contentBodyCount
			&& contentBodyCount <= PointScoreTerm.SCORE_100_TO_199.getEndTerm()){
			contentBodyScore = scoreFactor.getQuestionAnswerContentsCountTerm100to199();
		}else if(PointScoreTerm.SCORE_200_TO_299.getStartTerm() <= contentBodyCount
				&& contentBodyCount <= PointScoreTerm.SCORE_200_TO_299.getEndTerm()){
			contentBodyScore = scoreFactor.getQuestionAnswerContentsCountTerm200to299();
		}else if(PointScoreTerm.SCORE_300_TO_399.getStartTerm() <= contentBodyCount
				&& contentBodyCount <= PointScoreTerm.SCORE_300_TO_399.getEndTerm()){
			contentBodyScore = scoreFactor.getQuestionAnswerContentsCountTerm300to399();
		}else if(PointScoreTerm.SCORE_400_TO_449.getStartTerm() <= contentBodyCount
				&& contentBodyCount <= PointScoreTerm.SCORE_400_TO_449.getEndTerm()){
			contentBodyScore = scoreFactor.getQuestionAnswerContentsCountTerm400to449();
		}else if(PointScoreTerm.SCORE_450_TO_499.getStartTerm() <= contentBodyCount
				&& contentBodyCount <= PointScoreTerm.SCORE_450_TO_499.getEndTerm()){
			contentBodyScore = scoreFactor.getQuestionAnswerContentsCountTerm450to499();
		}else if(PointScoreTerm.SCORE_MORE_500.getStartTerm() <= contentBodyCount){
			contentBodyScore = scoreFactor.getQuestionAnswerContentsCountTermMore500();
		}
		BigDecimal contentImageScore = new BigDecimal("0");
		if(contentImageCount > 0){
			contentImageScore = scoreFactor.getQuestionAnswerHasImages();
		}

		BigDecimal score = new BigDecimal((elapsedDays - 1) * -1).multiply(
				scoreFactor.getQuestionAnswerDay());
		score = score.add(new BigDecimal(likeCount).multiply(
				scoreFactor.getQuestionAnswerLikeCount()));
		score = score.add(new BigDecimal(commentCount).multiply(
				scoreFactor.getQuestionAnswerCommentCount()));
		score = score.add(new BigDecimal(followerCount).multiply(
				scoreFactor.getQuestionAnswerFollowerCount()));
		score = score.add(contentBodyScore.multiply(
				scoreFactor.getQuestionAnswerContentsCountCoefficient()));
		score = score.add(contentImageScore.multiply(
				scoreFactor.getQuestionAnswerHasImagesCoefficient()));
		questionAnswer.setQuestionAnswerScore(score.doubleValue());
		questionAnswerDao.updateQuestionAnswerScoreWithIndexForBatch(questionAnswer);

		DailyScoreFactorDO dailyScoreFactor = new DailyScoreFactorDO();
		dailyScoreFactor.setType(DailyScoreFactorType.QUESTION_ANSWER);
		dailyScoreFactor.setTargetDate(targetDate);
		dailyScoreFactor.setContentsId(questionAnswer.getQuestionAnswerId());
		dailyScoreFactor.setSku(questionAnswer.getProduct().getSku());
		dailyScoreFactor.setElapsedDays(elapsedDays);
		dailyScoreFactor.setCommentCount(commentCount);
		dailyScoreFactor.setLikeCount(likeCount);
		dailyScoreFactor.setFollowerCount(followerCount);
		dailyScoreFactor.setPostDate(questionAnswer.getPostDate());
		dailyScoreFactor.setContentBodyCount(contentBodyCount);
		dailyScoreFactor.setContentImageCount(contentImageCount);

		dailyScoreFactorDao.createDailyScoreFactorForBatch(dailyScoreFactor);
	}
	@Override
	@ArroundSolr
	@ArroundHBase
	public void updateQuestionAnswerScoreForBatchBegin(int bulkSize){
		questionAnswerDao.updateQuestionAnswerScoreWithIndexForBatchBegin(bulkSize);
		dailyScoreFactorDao.createDailyScoreFactorForBatchBegin(bulkSize);
	}
	@Override
	@ArroundSolr
	@ArroundHBase
	public void updateQuestionAnswerScoreForBatchEnd(){
		questionAnswerDao.updateQuestionAnswerScoreWithIndexForBatchEnd();
		dailyScoreFactorDao.createDailyScoreFactorForBatchEnd();
	}

	/**
	 * 指定した質問に対して投稿した質問回答を回答投稿日時順（降順）に返します。
	 * @param questionId 質問ID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 質問回答一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<QuestionAnswerSetVO> findNewQuestionAnswerByQuestionId(
			String questionId, String excludeAnswerId, int limit, Date offsetTime, boolean previous) {
		return appendPurchaseProductForAnswer(createQuestionAnswerSets(
				questionAnswerDao.findNewQuestionAnswerByQuestionId(
						questionId, excludeAnswerId, limit, offsetTime, previous)));
	}

	/**
	 * 指定した質問に対して投稿した質問回答を適合度順（降順）に返します。
	 * @param questionId 質問ID
	 * @param limit 最大取得件数
	 * @param offsetMatchScore 検索開始適合度
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 質問回答一覧
	 */
	@Override
	@ArroundSolr
	public SearchResult<QuestionAnswerSetVO> findMatchQuestionAnswerByQuestionId(
			String questionId, String excludeAnswerId, int limit, Double offsetMatchScore,
			Date offsetTime, boolean previous) {
		return appendPurchaseProductForAnswer(createQuestionAnswerSets(
				questionAnswerDao.findMatchQuestionAnswerByQuestionId(
						questionId, excludeAnswerId, limit, offsetMatchScore, offsetTime, previous)));
	}

	/**
	 * 質問セットの検索結果を作成します。
	 * @param searchResult 質問リスト
	 * @return 質問セットの検索結果
	 */
	private SearchResult<QuestionSetVO> createQuestionSets(SearchResult<QuestionDO> searchResult) {
		SearchResult<QuestionSetVO> result
				= new SearchResult<QuestionSetVO>(0, new ArrayList<QuestionSetVO>());
		result.setHasAdult(searchResult.isHasAdult());
		result.setNumFound(searchResult.getNumFound());
		if (searchResult.getDocuments().size() == 0) {
			return result;
		}
		List<String> questionIds = new ArrayList<String>();
		List<String> skus = new ArrayList<String>();
		for (QuestionDO question : searchResult.getDocuments()) {
			questionIds.add(question.getQuestionId());
			if( question.getProduct() == null || question.getProduct().getSku() == null)
				continue;
			skus.add(question.getProduct().getSku());
		}
		//質問回答者数
		Map<String, Long> answerCountMap = questionAnswerDao.loadQuestionAnswerCountMapByQuestionId(questionIds);
		Map<String, List<ImageHeaderDO>> imageAllMap = imageDao.loadAllImageMapByContentsIds(PostContentType.QUESTION, questionIds);
		Map<String, Boolean> hasAnswerMap = new HashMap<String, Boolean>();
		Map<String, Boolean> questionFollowMap = new HashMap<String, Boolean>();
		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();
		if( StringUtils.isNotEmpty(loginCommunityUserId)){
			//質問回答済みかどうか
			hasAnswerMap = hasQuestionAnswer(loginCommunityUserId, questionIds);
			// 質問フォロー済みかどうか
			questionFollowMap = questionFollowDao.loadQuestionFollowMap(loginCommunityUserId, questionIds);
		}
		
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(searchResult.getDocuments());
		
		for (QuestionDO question : searchResult.getDocuments()) {
			QuestionSetVO vo = new QuestionSetVO();
			String questionId = question.getQuestionId();
			vo.setQuestion(question);
			if (answerCountMap.containsKey(questionId)) {
				vo.setAnswerCount(answerCountMap.get(questionId));
			}
			if( hasAnswerMap.containsKey(questionId)){
				vo.setAnswerFlg(hasAnswerMap.get(questionId));
			}
			if (imageAllMap.containsKey(questionId)) {
				vo.setImages(imageAllMap.get(questionId));
			}
			if (questionFollowMap.containsKey(questionId)) {
				vo.setFollowingFlg(questionFollowMap.get(questionId));
			}
			if (question.getLastAnswerDate() != null) {
				vo.setAnswerSets(createQuestionAnswerSets(questionAnswerDao.findNewQuestionAnswerByQuestionId(questionId, null, 1, new Date(), false)));
			} else {
				vo.setAnswerSets(new SearchResult<QuestionAnswerSetVO>(0, new ArrayList<QuestionAnswerSetVO>()));
			}
			result.updateFirstAndLast(vo);
			if (question.isStop(loginCommunityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				continue;
			}
			// コンテンツのコミュニティユーザーの購入情報を取得する。
			PurchaseProductDO purchaseProductDO = orderDao.loadPurchaseProductBySku(
					question.getCommunityUser().getCommunityUserId(),
					question.getProduct().getSku(),
					Path.DEFAULT,
					false);
			if( purchaseProductDO != null && !purchaseProductDO.isDeleted() ){
				vo.setPurchaseProduct( purchaseProductDO );
			}
			// ログインユーザーの購入情報を取得する。
			if( StringUtils.isNotEmpty(loginCommunityUserId)){
				purchaseProductDO = orderDao.loadPurchaseProductBySku(
						loginCommunityUserId,
						question.getProduct().getSku(),
						Path.DEFAULT,
						false);
				if( purchaseProductDO != null && !purchaseProductDO.isDeleted() ){
					vo.setLoginUserPurchaseProduct(purchaseProductDO);
				}
			}
			
			result.getDocuments().add(vo);
		}
		
		return result;
	}
	

	/**
	 * 質問回答セットの検索結果を作成します。
	 * @param searchResult 質問回答リスト
	 * @return 質問回答セットの検索結果
	 */
	//TODO
	private SearchResult<QuestionAnswerSetVO> createQuestionAnswerSets(SearchResult<QuestionAnswerDO> searchResult) {
		SearchResult<QuestionAnswerSetVO> result
				= new SearchResult<QuestionAnswerSetVO>(0, new ArrayList<QuestionAnswerSetVO>());
		result.setHasAdult(searchResult.isHasAdult());
		result.setNumFound(searchResult.getNumFound());
		if (searchResult.getDocuments().size() == 0) {
			return result;
		}
		List<String> questionAnswerIds = new ArrayList<String>();
		List<String> communityUserIds = new ArrayList<String>();
		List<ProductMasterDO> productMasters = new ArrayList<ProductMasterDO>();
		List<String> questionIds = new ArrayList<String>();
		for (QuestionAnswerDO questionAnswer : searchResult.getDocuments()) {
			questionAnswerIds.add(questionAnswer.getQuestionAnswerId());
			communityUserIds.add(questionAnswer.getCommunityUser().getCommunityUserId());
			ProductMasterDO productMaster = new ProductMasterDO();
			productMaster.setProduct(questionAnswer.getProduct());
			productMaster.setCommunityUser(questionAnswer.getCommunityUser());
			productMasters.add(productMaster);
			questionIds.add(questionAnswer.getQuestion().getQuestionId());
		}
		//質問回答いいね数
		Map<String, Long> likeCountMap = likeDao.loadQuestionAnswerLikeCountMap(questionAnswerIds);
		// 参考になったかどうか
		Map<String, Long[]> votingMap = votingDao.loadQuestionAnswerVotingCountMap(questionAnswerIds);
		//質問回答コメント数
		Map<String, Long> commentCountMap = commentDao.loadQuestionAnswerCommentCountMap(questionAnswerIds);
		Map<String, Long> answerCountMap = questionAnswerDao.loadQuestionAnswerCountMapByQuestionId(questionIds);
		Map<String, List<ImageHeaderDO>> imageAllMap = imageDao.loadAllImageMapByContentsIds(PostContentType.ANSWER, questionAnswerIds);
		Map<String, List<ImageHeaderDO>> questionImageMap = imageDao.loadAllImageMapByContentsIds(PostContentType.QUESTION, questionIds);
		
		Map<String, Boolean> commentMap = new HashMap<String, Boolean>();
		Map<String, Boolean> likeMap = new HashMap<String, Boolean>();
		Map<String, Boolean> questionFollowMap = new HashMap<String, Boolean>();
		Map<String, Boolean> hasAnswerMap = new HashMap<String, Boolean>();
		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();
		if( StringUtils.isNotEmpty(loginCommunityUserId)){
			//コメント済みかどうか
			commentMap = commentDao.loadQuestionAnswerCommentMap(loginCommunityUserId, questionAnswerIds);
			//いいね済みかどうか
			likeMap = likeDao.loadQuestionAnswerLikeMap(loginCommunityUserId, questionAnswerIds);
			//質問をフォロー済みかどうか
			questionFollowMap = questionFollowDao.loadQuestionFollowMap(loginCommunityUserId, questionIds);
			//質問回答済みかどうか
			hasAnswerMap = hasQuestionAnswer(loginCommunityUserId, questionIds);
		}
		
		Map<String, ProductMasterDO> productMasterMap = new HashMap<String, ProductMasterDO>();
		//商品マスター情報
		for (ProductMasterDO productMaster : productMasterDao.findProductMasterInRank(productMasters)) {
			productMasterMap.put(IdUtil.createIdByConcatIds(
					productMaster.getProduct().getSku(),
					productMaster.getCommunityUser().getCommunityUserId()),
					productMaster);
		}
		
		
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(searchResult.getDocuments());
		
		for (QuestionAnswerDO questionAnswer : searchResult.getDocuments()) {
			QuestionAnswerSetVO vo = new QuestionAnswerSetVO();
			String questionAnswerId = questionAnswer.getQuestionAnswerId();
			if (likeCountMap.containsKey(questionAnswerId)) {
				vo.setLikeCount(likeCountMap.get(questionAnswerId));
			}
			if (commentMap.containsKey(questionAnswerId)) {
				vo.setCommentFlg(commentMap.get(questionAnswerId));
			}
			if (likeMap.containsKey(questionAnswerId)) {
				vo.setLikeFlg(likeMap.get(questionAnswerId));
			}
			if (votingMap.containsKey(questionAnswerId)) {
				Long[] votingCount = votingMap.get(questionAnswerId);
				if( votingCount != null ) {
					vo.setVotingCountYes(votingCount[0]);
					vo.setVotingCountNo(votingCount[1]);
				}
			}
			if (commentCountMap.containsKey(questionAnswerId)) {
				vo.setCommentCount(commentCountMap.get(questionAnswerId));
			}
			if (questionFollowMap.containsKey(questionAnswer.getQuestion().getQuestionId())) {
				vo.setFollowingFlg(questionFollowMap.get(questionAnswer.getQuestion().getQuestionId()));
			}
			if (answerCountMap.containsKey(questionAnswer.getQuestion().getQuestionId())) {
				vo.setAnswerCount(answerCountMap.get(questionAnswer.getQuestion().getQuestionId()));
			}
			if( hasAnswerMap.containsKey(questionAnswer.getQuestion().getQuestionId())){
				vo.setAnswerFlg(hasAnswerMap.get(questionAnswer.getQuestion().getQuestionId()));
			}
			if (questionImageMap.containsKey(questionAnswer.getQuestion().getQuestionId())) {
				questionAnswer.getQuestion().setImageHeaders(questionImageMap.get(questionAnswer.getQuestion().getQuestionId()));
			}
			
			vo.setQuestionAnswer(questionAnswer);

			String key = IdUtil.createIdByConcatIds(
					questionAnswer.getProduct().getSku(),
					questionAnswer.getCommunityUser().getCommunityUserId());
			
			if (productMasterMap.containsKey(key)) {
				vo.setProductMaster(productMasterMap.get(key));
			}
			if (imageAllMap.containsKey(questionAnswerId)) {
				vo.setImages(imageAllMap.get(questionAnswerId));
			}

			result.updateFirstAndLast(vo);
			
			if (questionAnswer.isStop(loginCommunityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				continue;
			}
			
			SearchResult<LikeDO> likes = findLikeByQuestionAnswerId(
					questionAnswerId,
					loginCommunityUserId,
					resourceConfig.commentInitReadLimit);
			
			long like_count = vo.getLikeCount() - (vo.isLikeFlg()? 1 : 0);
			if( vo.isLikeFlg() ){
				// getLikeCount()は、自分を含んだ数字であることが前提。
				if( 0 == likes.getDocuments().size() ){
					vo.setLikePrefixType(LikePrefixType.ONLYONE.getCode());
				}else{
					vo.setLikePrefixType(LikePrefixType.MULTIPLE.getCode());
				}
			}else{
				vo.setLikePrefixType(LikePrefixType.NONE.getCode());
			}
			
			List<String> communityUserNames = new ArrayList<String>();
			if( like_count == 0 ){
				vo.setLikeMessageType(LikeMessageType.NONE.getCode());
			}else{
				if( like_count <= 3 ){
					vo.setLikeMessageType(LikeMessageType.UPTO3.getCode());
					for( LikeDO like : likes.getDocuments()){
						communityUserNames.add(like.getCommunityUser().getCommunityName());
					}
				}else{
					vo.setLikeMessageType(LikeMessageType.MULTIPLE.getCode());
				}
			}
			vo.setLikeUserNames(communityUserNames);
			
			// TODO あとで効率よく取れるように変更する。
			SearchResult<CommentSetVO> resultComment = findQuestionAnswerCommentByQuestionAnswerId(
					questionAnswerId,
					null,
					resourceConfig.commentInitReadLimit,
					null,
					false);
			if( !resultComment.getDocuments().isEmpty() )
				Collections.reverse(resultComment.getDocuments());
			vo.setCommentViewRemainingCount(vo.getCommentCount() - resultComment.getDocuments().size());
			vo.setComments(resultComment);
			
			PurchaseProductDO purchaseProductDO = orderDao.loadPurchaseProductBySku(
					questionAnswer.getCommunityUser().getCommunityUserId(),
					questionAnswer.getProduct().getSku(),
					Path.DEFAULT,
					false);
			if( purchaseProductDO != null && !purchaseProductDO.isDeleted() ){
				vo.setPurchaseProduct( purchaseProductDO );
			}
			// ログインユーザーの購入情報を取得する。
			if( StringUtils.isNotEmpty(loginCommunityUserId)){
				purchaseProductDO = orderDao.loadPurchaseProductBySku(
						loginCommunityUserId,
						questionAnswer.getProduct().getSku(),
						Path.DEFAULT,
						false);
				if( purchaseProductDO != null && !purchaseProductDO.isDeleted() ){
					vo.setLoginUserPurchaseProduct(purchaseProductDO);
				}
			}
			
			result.getDocuments().add(vo);
		}

		return result;
	}

	/**
	 * 購入商品情報を付加します。
	 * solrから検索して付加します。無ければhbaseから取得して付加します。
	 * @param qvos
	 * @return qvos
	 */
//	private SearchResult<QuestionSetVO> appendPurchaseProduct( SearchResult<QuestionSetVO> qvos){
//		List<Map<String, String>> questions = new ArrayList<Map<String, String>>();
//		for( QuestionSetVO qvo : qvos.getDocuments() ){
//			if( null == qvo.getAnswerSets() ){
//				continue;
//			}
//			for( QuestionAnswerSetVO vo : qvo.getAnswerSets().getDocuments() ){
//				QuestionAnswerDO answer = vo.getQuestionAnswer();
//				if( null == answer.getProduct() || null == answer.getProduct().getSku() ){
//					continue;
//				}
//				Map<String, String> params = new HashMap<String, String>();
//				params.put("communityUserId", answer.getCommunityUser().getCommunityUserId());
//				params.put("sku", answer.getProduct().getSku());
//				questions.add(params);
//			}
//		}
//		if(!questions.isEmpty()){
//			Map<String, PurchaseProductDO> orderMaps = orderDao.findPurchaseProductBySku(questions);
//			
//			for( QuestionSetVO qvo : qvos.getDocuments() ){
//				if( null == qvo.getAnswerSets() ){
//					continue;
//				}
//				for( QuestionAnswerSetVO vo : qvo.getAnswerSets().getDocuments() ){
//					QuestionAnswerDO answer = vo.getQuestionAnswer();
//					if( null == answer.getProduct() || null == answer.getProduct().getSku() ){
//						continue;
//					}
//					String key = IdUtil.createIdByConcatIds(answer.getCommunityUser().getCommunityUserId(), answer.getProduct().getSku());
//					if (orderMaps.containsKey(key)) {
//						PurchaseProductDO order = orderMaps.get(key);
//						if( null != order && !order.isDeleted() ){
//							vo.setPurchaseProduct( order );
//						}
//					} else {
//						PurchaseProductDO order = orderDao.loadPurchaseProductBySku(answer.getCommunityUser().getCommunityUserId(),
//								answer.getProduct().getSku(), Path.DEFAULT, false);
//						if( null != order && !order.isDeleted() ){
//							vo.setPurchaseProduct( order );
//						}
//					}
//				}
//			}
//		}
//		return qvos;
//	}

	/**
	 * 購入商品情報を付加します。
	 * hbaseから取得します。
	 * @param qvos
	 * @return qvos
	 */
//	private SearchResult<QuestionSetVO> appendPurchaseProductFromHBase(SearchResult<QuestionSetVO> qvos) {
//		for( QuestionSetVO qvo : qvos.getDocuments() ){
//			if( null == qvo.getAnswerSets() ){
//				continue;
//			}
//			for( QuestionAnswerSetVO vo : qvo.getAnswerSets().getDocuments() ){
//				QuestionAnswerDO answer = vo.getQuestionAnswer();
//				if( null == answer.getProduct() || null == answer.getProduct().getSku() ){
//					continue;
//				}
//				PurchaseProductDO order = orderDao.loadPurchaseProductBySku(answer.getCommunityUser().getCommunityUserId(),
//						answer.getProduct().getSku(), Path.DEFAULT, false);
//				if( null != order && !order.isDeleted() ){
//					vo.setPurchaseProduct( order );
//				}
//			}
//		}
//		return qvos;
//	}
	
	/**
	 * 購入商品情報を付加します。
	 * @param qvos
	 * @return qvos
	 */
	private SearchResult<QuestionAnswerSetVO> appendPurchaseProductForAnswer( SearchResult<QuestionAnswerSetVO> qvos ){
		List<Map<String, String>> questions = new ArrayList<Map<String, String>>();
		for( QuestionAnswerSetVO vo : qvos.getDocuments() ){
			QuestionAnswerDO answer = vo.getQuestionAnswer();
			if( null == answer.getProduct() || null == answer.getProduct().getSku() ){
				continue;
			}
			Map<String, String> params = new HashMap<String, String>();
			params.put("communityUserId", answer.getCommunityUser().getCommunityUserId());
			params.put("sku", answer.getProduct().getSku());
			questions.add(params);
		}
		if (!questions.isEmpty()) {
			Map<String, PurchaseProductDO> orderMaps = orderDao.findPurchaseProductBySku(questions);
			
			for( QuestionAnswerSetVO vo : qvos.getDocuments() ){
				QuestionAnswerDO answer = vo.getQuestionAnswer();
				if( null == answer.getProduct() || null == answer.getProduct().getSku() ){
					continue;
				}
				String key = IdUtil.createIdByConcatIds(answer.getCommunityUser().getCommunityUserId(), answer.getProduct().getSku());
				if (orderMaps.containsKey(key)) {
					PurchaseProductDO order = orderMaps.get(key);
					if( null != order && !order.isDeleted() ){
						vo.setPurchaseProduct( order );
					}
				} else {
					PurchaseProductDO order = orderDao.loadPurchaseProductBySku(answer.getCommunityUser().getCommunityUserId(),
							answer.getProduct().getSku(), Path.DEFAULT, false);
					if( null != order && !order.isDeleted() ){
						vo.setPurchaseProduct( order );
					}
				}
			}
		}
		return qvos;
	}
	
	@Override
	public SearchResult<QuestionSetVO> loadQuestionSet(String questionId) {
		QuestionDO question = questionDao.loadQuestion(questionId);
		if (ProductUtil.invalid(question)) {
			return new SearchResult<QuestionSetVO>();
		}
		List<QuestionDO> documents = new ArrayList<QuestionDO>();
		documents.add(question);
		SearchResult<QuestionDO> searchResult = new SearchResult<QuestionDO>(documents.size(), documents);
		//return appendPurchaseProduct(createQuestionSets(searchResult));
		return createQuestionSets(searchResult);
	}

	@Override
	public SearchResult<QuestionAnswerSetVO> loadQuestionAnswerSet(
			String questionAnswerId) {
		QuestionAnswerDO questionAnswer = questionAnswerDao.loadQuestionAnswer(questionAnswerId);
		if (ProductUtil.invalid(questionAnswer)) {
			return new SearchResult<QuestionAnswerSetVO>();
		}
		List<QuestionAnswerDO> documents = new ArrayList<QuestionAnswerDO>();
		documents.add(questionAnswer);
		SearchResult<QuestionAnswerDO> searchResult = new SearchResult<QuestionAnswerDO>(documents.size(), documents);
		return appendPurchaseProductForAnswer(createQuestionAnswerSets(searchResult));
	}

	@Override
	public boolean isShowQuestion(String questionId) {
		QuestionDO question = questionDao.loadQuestionFromIndex(questionId);
		String communityUserId = requestScopeDao.loadCommunityUserId();
		 if(question != null && question.getCommunityUser().getStatus().equals(CommunityUserStatus.STOP)
				&& !question.getCommunityUser().getCommunityUserId().equals(communityUserId)){
			 return false;
		 }
		return true;
	}

	@Override
	public QuestionDO loadQuestion(String questionId) {
		QuestionDO question = questionDao.loadQuestion(questionId);
		if (ProductUtil.invalid(question)) {
			return null;
		} else {
			return question;
		}
	}

	@Override
	public QuestionAnswerDO loadQuestionAnswer(String questionAnswerId) {
		QuestionAnswerDO questionAnswer =  questionAnswerDao.loadQuestionAnswer(questionAnswerId);
		if (ProductUtil.invalid(questionAnswer)) {
			return null;
		} else {
			return questionAnswer;
		}
	}

	@Override
	public String findProductSku(String questionId) {
		return questionDao.findProductSku(questionId);
	}
	
	@Override
	public String findProductSkuByAnswer(String questionAnswerId) {
		return questionAnswerDao.findProductSku(questionAnswerId);
	}

	private void settingProductAndCommunityUser(QuestionDO question){
		// 商品とポイント情報を取得して設定します。
		if( question.getProduct() == null || question.getProduct().getSku() == null){
			ProductDO product = productDao.loadProduct(question.getProduct().getSku());
			if( product == null ){
				throw new IllegalArgumentException("Product is null. sku = " + question.getProduct().getSku());
			}
			question.setProduct(product);
			question.setAdult(question.getProduct().isAdult());
		}
		
		// コミュニティユーザー情報を取得して設定します。
		if( question.getCommunityUser() == null || question.getCommunityUser().getCommunityUserId() == null ){
			CommunityUserDO communityUser = communityUserDao.load(
					question.getCommunityUser().getCommunityUserId(), 
					Path.includeProp("*"));
			if( communityUser == null ){
				throw new IllegalArgumentException("can not post contents. because userdata can not load. communityUserId:" + question.getCommunityUser().getCommunityUserId());
			}
			question.setCommunityUser(communityUser);
		}
	}
	
	private void settingProductAndCommunityUser(QuestionAnswerDO answer){
		// 商品とポイント情報を取得して設定します。
		if( answer.getProduct() == null || answer.getProduct().getSku() == null){
			ProductDO product = productDao.loadProduct(answer.getProduct().getSku());
			if( product == null ){
				throw new IllegalArgumentException("Product is null. sku = " + answer.getProduct().getSku());
			}
			answer.setProduct(product);
			answer.setAdult(answer.getProduct().isAdult());
		}
		
		// コミュニティユーザー情報を取得して設定します。
		if( answer.getCommunityUser() == null || answer.getCommunityUser().getCommunityUserId() == null ){
			CommunityUserDO communityUser = communityUserDao.load(
					answer.getCommunityUser().getCommunityUserId(), 
					Path.includeProp("*"));
			if( communityUser == null ){
				throw new IllegalArgumentException("can not post contents. because userdata can not load. communityUserId:" + answer.getCommunityUser().getCommunityUserId());
			}
			answer.setCommunityUser(communityUser);
			
		}
	}
	private String settingPurchaseProduct(QuestionAnswerDO answer){
		PurchaseProductDO purchaseProduct = orderDao.loadPurchaseProductBySku(
				answer.getCommunityUser().getCommunityUserId(),
				answer.getProduct().getSku(),
				Path.includeProp("*").includePath("purchaseProductId,purchaseDate,share," +
						"purchaseHistoryType,purchaseDateFix," +
						"product.sku").depth(1), true);
		String purchaseProductId = null;
		if (purchaseProduct == null || purchaseProduct.isDeleted()) {
			if (answer.getPurchaseDate() == null) {
				throw new IllegalArgumentException("PurchaseDate in review required.");
			}
			purchaseProduct = new PurchaseProductDO();
			purchaseProduct.setCommunityUser(answer.getCommunityUser());
			purchaseProduct.setPurchaseDate(answer.getPurchaseDate());
			purchaseProduct.setUserInputPurchaseDate(purchaseProduct.getPurchaseDate());
			purchaseProduct.setProduct(answer.getProduct());
			purchaseProduct.setPurchaseHistoryType(PurchaseHistoryType.OTHER);
			purchaseProduct.setAdult(answer.getProduct().isAdult());
			purchaseProduct.setPublicSetting(true);
			orderDao.createPurchaseProduct(purchaseProduct, false);
			purchaseProductId = purchaseProduct.getPurchaseProductId();
		}
		if (purchaseProduct.getPurchaseDate() == null) {
			throw new IllegalStateException(
					"PurchaseDate is null. communityUserId = "
					+ answer.getCommunityUser().getCommunityUserId()
					+ " sku = " + answer.getProduct().getSku());
		}
		answer.setPurchaseProduct(purchaseProduct);
		answer.setPurchaseDate(purchaseProduct.getPurchaseDate());
		answer.setPurchaseHistoryType(purchaseProduct.getPurchaseHistoryType());
		if (!purchaseProduct.isPurchaseDateFix()) {
			orderDao.fixPurchaseDate(purchaseProduct.getPurchaseProductId());
		}
		
		return purchaseProductId;
	}

	/**
	 * 指定した商品に対するQA情報を回答なし・質問投稿日時順（降順）に返します。
	 * @param sku
	 * @param excludeQuestionId
	 * @param limit
	 * @param offsetTime
	 * @param previous
	 * @return
	 */
	@Override
	@ArroundSolr
	public SearchResult<QuestionSetVO> findNewQuestionWithNotAnswerPriorityBySku(
			String sku,
			String excludeCommunityUserId,
			String excludeQuestionId,
			int limit,
			Date offsetTime,
			boolean previous) {
		SearchResult<QuestionDO> searchResult = questionDao.findNewQuestionWithNotAnswerPriorityBySku(
				sku, excludeCommunityUserId, excludeQuestionId, limit, offsetTime, previous);
		if (searchResult.getNumFound() == 0) {
			return new SearchResult<QuestionSetVO>(0, new ArrayList<QuestionSetVO>());
		} else {
			return createQuestionSets(searchResult);
		}
	}

}
