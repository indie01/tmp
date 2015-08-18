/**
 *
 */
package com.kickmogu.yodobashi.community.service.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.resource.cache.CacheStrategyType;
import com.kickmogu.yodobashi.community.resource.cache.MethodCache;
import com.kickmogu.yodobashi.community.resource.cache.TargetSystemType;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.ImageDao;
import com.kickmogu.yodobashi.community.resource.dao.LikeDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductMasterDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionAnswerDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.ReviewDao;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.service.SocialUserFindService;
import com.kickmogu.yodobashi.community.service.vo.CommunityUserSetVO;

/**
 * 関係するユーザーを検索するサービスの実装です。
 * @author kamiike
 *
 */
@Service
public class SocialUserFindServiceImpl implements SocialUserFindService {

	/**
	 * コミュニティユーザー DAO です。
	 */
	@Autowired
	private CommunityUserDao communityUserDao;
	
	@Autowired
	private CommunityUserFollowDao communityUserFollowDao;

	/**
	 * 画像 DAO です。
	 */
	@Autowired
	private ImageDao imageDao;

	/**
	 * いいね DAO です。
	 */
	@Autowired
	private LikeDao likeDao;

	/**
	 * 商品フォロー DAO です。
	 */
	@Autowired
	private ProductFollowDao productFollowDao;

	/**
	 * 商品マスター DAO です。
	 */
	@Autowired
	private ProductMasterDao productMasterDao;

	/**
	 * 質問フォロー DAO です。
	 */
	@Autowired
	private QuestionFollowDao questionFollowDao;
	
	@Autowired
	private QuestionDao questionDao;
	
	/**
	 * 質問回答 DAO です。
	 */
	@Autowired
	private QuestionAnswerDao questionAnswerDao;

	/**
	 * レビューDAO です。
	 */
	@Autowired
	private ReviewDao reviewDao;

	/**
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	private RequestScopeDao requestScopeDao;

	/**
	 * 指定した商品にレビューを書いたユーザーを返します。
	 * @param sku SKU
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=1000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public SearchResult<CommunityUserDO> findReviewerBySKU(
			String sku, String excludeCommunityUserId, int limit, int offset) {
		return findReviewerBySKU(sku, excludeCommunityUserId, limit, offset, false);
	}

	
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=1000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public SearchResult<CommunityUserDO> findReviewerBySKU(
			String sku, String excludeCommunityUserId, int limit, int offset, boolean excludeProduct) {
		return filterStopUsers(reviewDao.findDistinctReviwerExcludeCommunityUserIdBySKU(
				sku, excludeCommunityUserId, limit, offset, excludeProduct));
	}

	/**
	 * 指定した商品の質問に回答を書いたユーザーを返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=1000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public SearchResult<CommunityUserDO> findAnswererBySKU(
			String sku, int limit, int offset) {
		return findAnswererBySKU(sku, limit, offset, false);
	}
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=1000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public SearchResult<CommunityUserDO> findAnswererBySKU(
			String sku, int limit, int offset, boolean excludeProduct) {
		return filterStopUsers(
				questionAnswerDao.findDistinctAnswererBySKU(sku, limit, offset, excludeProduct));
	}

	/**
	 * SKUから商品のフォロワーのコミュニティユーザーを検索して返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param asc 昇順ソート
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=1000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public SearchResult<CommunityUserDO> findProductFollowerBySKU(
			String sku, int limit, int offset, boolean asc) {
		return filterStopUsers(productFollowDao.findFollowerCommunityUserBySKU(
				sku, limit, offset, asc));
	}

	/**
	 * 指定した商品の画像（レビュー、質問、回答本文内含む）を投稿したユーザーを返します。
	 * @param sku SKU
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=1000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public SearchResult<CommunityUserDO> findAllTypeImagePostCommunityUserBySKU(
			String sku, int limit, int offset) {
		return filterStopUsers(
				imageDao.findDistinctImageUploaderBySKU(sku, limit, offset, true));
	}

	/**
	 * 指定した商品の画像を投稿したユーザーを返します。
	 * @param sku SKU
	 * @param excludeCommunityUserId 除外するコミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	@ArroundSolr
	public SearchResult<CommunityUserDO> findImagePostCommunityUserBySKU(
			String sku, String excludeCommunityUserId, int limit, int offset) {
		return filterStopUsers(
				imageDao.findDistinctImageUploaderBySKU(sku, limit, offset, false));
	}

	/**
	 * 指定したコミュニティユーザーがフォローした商品に対してレビューを書いている
	 * ユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=1000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public SearchResult<CommunityUserDO> findReviewerByFollowProduct(
			String communityUserId, int limit, int offset) {
		return filterStopUsers(
				reviewDao.findDistinctReviwerByFollowProduct(
				communityUserId, limit, offset));
	}

	/**
	 * 指定したコミュニティユーザーがフォローした商品の質問に対して回答を書いている
	 * ユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=1000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public SearchResult<CommunityUserDO> findAnswererByFollowProduct(
			String communityUserId, int limit, int offset) {
		return filterStopUsers(
				questionAnswerDao.findDistinctAnswererByFollowProduct(
				communityUserId, limit, offset));
	}

	/**
	 * 指定したコミュニティユーザーが投稿した質問に対して回答を書いている
	 * ユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=1000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public SearchResult<CommunityUserDO> findAnswererByPostQuestion(
			String communityUserId, int limit, int offset) {
		return filterStopUsers(
				questionAnswerDao.findDistinctAnswererByPostQuestion(
				communityUserId, limit, offset));
	}

	/**
	 * 指定したコミュニティユーザーがフォローした質問に対して回答を書いている
	 * ユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=1000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public SearchResult<CommunityUserDO> findAnswererByFollowQuestion(
			String communityUserId, int limit, int offset) {
		return filterStopUsers(
				questionAnswerDao.findDistinctAnswererByFollowQuestion(
				communityUserId, limit, offset));
	}


	/**
	 * 指定したコミュニティユーザーがフォローした商品の商品マスターである
	 * コミュニティユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=1000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public SearchResult<CommunityUserDO> findProductMasterByFollowProduct(
			String communityUserId, int limit, int offset) {
		return filterStopUsers(
				productMasterDao.findDistinctProductMasterByFollowProduct(
				communityUserId, limit, offset));
	}

	/**
	 * 指定したコミュニティユーザーが購入した商品に対してレビューを書いている
	 * ユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param publicSetting 非公開情報を表示する場合はtrue
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=1000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public SearchResult<CommunityUserDO> findReviewerByPurchaseProduct(
			String communityUserId, int limit, int offset, boolean publicSetting) {
		return filterStopUsers(
				reviewDao.findDistinctReviwerByPurchaseProduct(
				communityUserId, limit, offset, publicSetting));
	}

	/**
	 * 指定したコミュニティユーザーが購入した商品の質問に対して回答を書いている
	 * ユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param publicSetting 非公開情報を表示する場合はtrue
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=1000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public SearchResult<CommunityUserDO> findAnswererByQuestionForPurchaseProduct(
			String communityUserId, int limit, int offset, boolean publicSetting) {
		return filterStopUsers(
				questionAnswerDao.findDistinctAnswererByQuestionForPurchaseProduct(
				communityUserId, limit, offset, publicSetting));
	}

	/**
	 * 指定したコミュニティユーザーが投稿したレビューに対していいねをしている
	 * ユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=1000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public SearchResult<CommunityUserDO> findLikeCommunityUserByReview(
			String communityUserId, int limit, int offset) {
		return filterStopUsers(
				likeDao.findDistinctLikeUserByReview(
				communityUserId, limit, offset));
	}

	/**
	 * 指定したコミュニティユーザーが投稿した回答に対していいねをしている
	 * ユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=1000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public SearchResult<CommunityUserDO> findLikeCommunityUserByQuestionAnswer(
			String communityUserId, int limit, int offset) {
		return filterStopUsers(
				likeDao.findDistinctLikeUserByQuestionAnswer(
				communityUserId, limit, offset));
	}

	/**
	 * 指定したコミュニティユーザーが投稿した画像に対していいねをしている
	 * ユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=1000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public SearchResult<CommunityUserDO> findLikeCommunityUserByImage(
			String communityUserId, int limit, int offset) {
		return filterStopUsers(
				likeDao.findDistinctLikeUserByImage(
				communityUserId, limit, offset));
	}

	/**
	 * 指定したコミュニティユーザーが購入した商品をフォローしている
	 * ユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @param publicSetting 非公開情報を表示する場合はtrue
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=1000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public SearchResult<CommunityUserDO> findFollowerByPurchaseProduct(
			String communityUserId, int limit, int offset, boolean publicSetting) {
		return filterStopUsers(
				productFollowDao.findFollowerByPurchaseProduct(
				communityUserId, limit, offset, publicSetting));
	}

	/**
	 * 指定した質問をフォローしているユーザーを返します。
	 * @param questionId 質問ID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=1000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public SearchResult<CommunityUserDO> findFollowerByQuestion(
			String questionId, int limit, int offset) {
		return filterStopUsers(
				questionFollowDao.findFollowerCommunityUserByQuestionId(
				questionId, limit, offset, false));
	}

	/**
	 * 指定した（最近閲覧した）商品のレビューを書いているユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	@ArroundSolr
	public SearchResult<CommunityUserDO> findReviewerByViewProducts(
			List<String> skus, int limit, int offset) {
		return filterStopUsers(
				reviewDao.findDistinctReviwerBySKU(skus, limit, offset));
	}

	/**
	 * 指定した（最近閲覧した）商品の質問に回答を書いている自分以外のユーザーを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	@ArroundSolr
	public SearchResult<CommunityUserDO> findAnswererByViewProducts(
			List<String> skus, int limit, int offset) {
		return filterStopUsers(
				questionAnswerDao.findDistinctAnswererBySKU(skus, limit, offset));
	}


	/**
	 * 指定したニックネームに部分一致するコミュニティユーザーを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param keyword キーワード
	 * @param offsetUserName 検索済みユーザー名
	 * @param limit 最大取得件数
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	@ArroundSolr
	public SearchResult<CommunityUserSetVO> findCommunityUserByPartialMatch(
			String communityUserId,
			String keyword,
			String offsetUserName,
			int limit) {
		

		SearchResult<CommunityUserDO> findUsers
				= communityUserDao.findCommunityUserByPartialMatch(
						communityUserId, keyword, offsetUserName, limit);

		SearchResult<CommunityUserSetVO> result = new SearchResult<CommunityUserSetVO>();
		for (CommunityUserDO communityUser : findUsers.getDocuments()) {
			CommunityUserSetVO vo = new CommunityUserSetVO();
			vo.setCommunityUser(communityUser);
			result.getDocuments().add(vo);
		}
		result.setNumFound(findUsers.getNumFound());
		List<String> communityUserIds = Lists.newArrayList();
		for (CommunityUserSetVO vo : result.getDocuments()) {
			communityUserIds.add(vo.getCommunityUser().getCommunityUserId());
		}
		
		// レビュー投稿数、質問数、回答数、投稿画像数を取得し各ユーザーのVOにセットする
		if( !communityUserIds.isEmpty() ){
			Map<String, Long> reviewCountMap = reviewDao.loadReviewCountMapByCommunityUserId(communityUserIds);
			Map<String, Long> questionCountMap = questionDao.loadQuestionCountMapByCommunityUserId(communityUserIds);
			Map<String, Long> answerCountMap = questionAnswerDao.loadQuestionAnswerCountMapByCommunityUserId(communityUserIds);
			Map<String, Long> imageCountMap = imageDao.loadImageCountMapByCommunityUserId(communityUserIds);
			Map<String, Boolean> followUserMap = communityUserFollowDao.loadCommunityUserFollowMap(communityUserId, communityUserIds);
			
			for(CommunityUserSetVO vo : result.getDocuments()){
				String confirmCommunityUserId = vo.getCommunityUser().getCommunityUserId();
				
				if( reviewCountMap.containsKey(confirmCommunityUserId)){
					vo.setPostReviewCount(reviewCountMap.get(confirmCommunityUserId));
				}
				if( questionCountMap.containsKey(confirmCommunityUserId)){
					vo.setPostQuestionCount(questionCountMap.get(confirmCommunityUserId));
				}
				if( answerCountMap.containsKey(confirmCommunityUserId)){
					vo.setPostQuestionAnswerCount(answerCountMap.get(confirmCommunityUserId));
				}
				if( imageCountMap.containsKey(confirmCommunityUserId)){
					vo.setPostImageCount(imageCountMap.get(confirmCommunityUserId));
				}
				if( followUserMap.containsKey(confirmCommunityUserId)){
					vo.setFollowingUser(followUserMap.get(confirmCommunityUserId));
				}
			}
		}
		
		
		return result;
	}

	/**
	 * 一時停止ユーザーをフィルタリングします。
	 * @param searchResult 検索結果
	 * @return フィルタリングした結果
	 */
	private SearchResult<CommunityUserDO> filterStopUsers(
			SearchResult<CommunityUserDO> searchResult) {
		for (Iterator<CommunityUserDO> it = searchResult.getDocuments(
				).iterator(); it.hasNext(); ) {
			CommunityUserDO communityUser = it.next();
			searchResult.updateFirstAndLast(communityUser);
			if (communityUser.isStop(requestScopeDao)) {
				it.remove();
				searchResult.countUpStopContents();
			}
		}
		return searchResult;
	}
}
