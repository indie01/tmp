/**
 *
 */
package com.kickmogu.yodobashi.community.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.yodobashi.community.common.exception.FollowLimitException;
import com.kickmogu.yodobashi.community.common.exception.UnActiveException;
import com.kickmogu.yodobashi.community.resource.dao.ActionHistoryDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.ImageDao;
import com.kickmogu.yodobashi.community.resource.dao.InformationDao;
import com.kickmogu.yodobashi.community.resource.dao.MailSettingDao;
import com.kickmogu.yodobashi.community.resource.dao.NormalizeCharDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductMasterDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionAnswerDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.ReviewDao;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSendTiming;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSettingType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;
import com.kickmogu.yodobashi.community.service.FollowService;
import com.kickmogu.yodobashi.community.service.IndexService;
import com.kickmogu.yodobashi.community.service.MailService;
import com.kickmogu.yodobashi.community.service.ReviewService;
import com.kickmogu.yodobashi.community.service.UserService;
import com.kickmogu.yodobashi.community.service.config.ServiceConfig;
import com.kickmogu.yodobashi.community.service.vo.CommunityUserFollowVO;
import com.kickmogu.yodobashi.community.service.vo.ProductFollowVO;
import com.kickmogu.yodobashi.community.service.vo.QuestionFollowVO;

/**
 * フォローサービスの実装です。
 * @author kamiike
 *
 */
@Service
public class FollowServiceImpl implements FollowService {

	/**
	 * アクション履歴 DAO です。
	 */
	@Autowired
	private ActionHistoryDao actionHistoryDao;

	/**
	 * コミュニティユーザー DAO です。
	 */
	@Autowired
	private CommunityUserDao communityUserDao;

	/**
	 * コミュニティユーザーフォロー DAO です。
	 */
	@Autowired
	private CommunityUserFollowDao communityUserFollowDao;

	/**
	 * 画像 DAO です。
	 */
	@Autowired
	private ImageDao imageDao;

	/**
	 * お知らせ情報 DAO です。
	 */
	@Autowired
	private InformationDao informationDao;

	/**
	 * メール設定 DAO です。
	 */
	@Autowired
	private MailSettingDao mailSettingDao;


	/**
	 * 商品 DAO です。
	 */
	@Autowired @Qualifier("catalog")
	private ProductDao productDao;

	/**
	 * 商品フォロー DAO です。
	 */
	@Autowired
	private ProductFollowDao productFollowDao;

	/**
	 * 質問 DAO です。
	 */
	@Autowired
	private QuestionDao questionDao;

	/**
	 * 質問回答 DAO です。
	 */
	@Autowired
	private QuestionAnswerDao questionAnswerDao;

	/**
	 * 質問フォロー DAO です。
	 */
	@Autowired
	private QuestionFollowDao questionFollowDao;

	/**
	 * レビュー DAO です。
	 */
	@Autowired
	private ReviewDao reviewDao;

	/**
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	private RequestScopeDao requestScopeDao;

	/**
	 * 商品マスター DAO です。
	 */
	@Autowired
	private ProductMasterDao productMasterDao;
	
	/**
	 * 標準化文字 DAO です。
	 */
	@Autowired
	private NormalizeCharDao normalizeCharDao;

	/**
	 * インデックスサービスです。
	 */
	@Autowired
	private IndexService indexService;

	/**
	 * コミュニティユーザーサービスです。
	 */
	@Autowired
	private UserService userService;

	/**
	 * メールサービスです。
	 */
	@Autowired
	private MailService mailService;

	@Autowired
	private ServiceConfig serviceConfig;
	
	@Autowired
	private ReviewService reviewService;

	/**
	 * フォローしているコミュニティユーザーを
	 * フォロー日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param followUserLimit フォローユーザーの最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 検索結果
	 */
	@Override
	public SearchResult<CommunityUserFollowVO> findFollowCommunityUser(
			String communityUserId, int limit,
			int followUserLimit, Date offsetTime, boolean previous) {
		SearchResult<CommunityUserFollowDO> searchResult
				= communityUserFollowDao.findFollowCommunityUser(
						communityUserId, limit, offsetTime, previous);
		SearchResult<CommunityUserFollowVO> result
				= new SearchResult<CommunityUserFollowVO>();
		result.setHasAdult(searchResult.isHasAdult());
		result.setNumFound(searchResult.getNumFound());
		List<String> communityUserIds = new ArrayList<String>();
		for (CommunityUserFollowDO follow : searchResult.getDocuments()) {
			communityUserIds.add(
					follow.getFollowCommunityUser().getCommunityUserId());
		}

		Map<String, Long> reviewCountMap
				= reviewDao.loadReviewCountMapByCommunityUserId(
						communityUserIds);
		Map<String, Long> questionCountMap
				= questionDao.loadQuestionCountMapByCommunityUserId(
						communityUserIds);
		Map<String, Long> questionAnswerCountMap
				= questionAnswerDao.loadQuestionAnswerCountMapByCommunityUserId(
				communityUserIds);
		Map<String, Long> imageCountMap
				= imageDao.loadImageCountMapByCommunityUserId(communityUserIds);

		Map<String, Long> productMasterCountMap
				= productMasterDao.loadProductMasterCountMapByCommunityUserId(
						communityUserIds);
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(searchResult.getDocuments());
		for (CommunityUserFollowDO follow : searchResult.getDocuments()) {
			CommunityUserFollowVO vo = new CommunityUserFollowVO();
			vo.setCommunityUser(follow.getFollowCommunityUser());
			vo.setFollowDate(follow.getFollowDate());
			if (reviewCountMap.containsKey(
					follow.getFollowCommunityUser().getCommunityUserId())) {
				vo.setPostReviewCount(
						reviewCountMap.get(
						follow.getFollowCommunityUser().getCommunityUserId()));
			}
			if (questionCountMap.containsKey(
					follow.getFollowCommunityUser().getCommunityUserId())) {
				vo.setPostQuestionCount(
						questionCountMap.get(
						follow.getFollowCommunityUser().getCommunityUserId()));
			}
			if (questionAnswerCountMap.containsKey(
					follow.getFollowCommunityUser().getCommunityUserId())) {
				vo.setPostAnswerCount(
						questionAnswerCountMap.get(
						follow.getFollowCommunityUser().getCommunityUserId()));
			}
			if (imageCountMap.containsKey(
					follow.getFollowCommunityUser().getCommunityUserId())) {
				vo.setPostImageCount(
						imageCountMap.get(
						follow.getFollowCommunityUser().getCommunityUserId()));
			}
			if (productMasterCountMap.containsKey(
					follow.getFollowCommunityUser().getCommunityUserId())) {
				vo.setProductMasterCount(
						productMasterCountMap.get(
						follow.getFollowCommunityUser().getCommunityUserId()));
			}
			SearchResult<CommunityUserFollowDO> followUsers
					= communityUserFollowDao.findFollowCommunityUser(
							follow.getFollowCommunityUser().getCommunityUserId(),
							followUserLimit, null, false);
			vo.setFollowUserCount(followUsers.getNumFound());
			for (CommunityUserFollowDO info : followUsers.getDocuments()) {
				if (info.getFollowCommunityUser().isStop(requestScopeDao)) {
					continue;
				}
				vo.getLatestFollowUsers().add(info.getFollowCommunityUser());
			}
			SearchResult<CommunityUserFollowDO> followerUsers
					= communityUserFollowDao.findFollowerCommunityUser(
							follow.getFollowCommunityUser().getCommunityUserId(),
							followUserLimit, null, false);
			vo.setFollowerUserCount(followerUsers.getNumFound());
			for (CommunityUserFollowDO info : followerUsers.getDocuments()) {
				if (info.getCommunityUser().isStop(requestScopeDao)) {
					continue;
				}
				vo.getLatestFollowerUsers().add(info.getCommunityUser());
			}

			vo.setFollowingFlg(true);

			result.updateFirstAndLast(vo);
			if (follow.isStop(communityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				continue;
			}
			result.getDocuments().add(vo);
		}
		return result;
	}

	/**
	 * フォロワーとなっているコミュニティユーザーを
	 * をフォロー日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param followUserLimit フォローユーザーの最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 検索結果
	 */
	@Override
	public SearchResult<CommunityUserFollowVO> findFollowerCommunityUser(
			String communityUserId, int limit,
			int followUserLimit, Date offsetTime, boolean previous) {
		SearchResult<CommunityUserFollowDO> searchResult
				= communityUserFollowDao.findFollowerCommunityUser(
						communityUserId, limit, offsetTime, previous);
		SearchResult<CommunityUserFollowVO> result
				= new SearchResult<CommunityUserFollowVO>();
		result.setHasAdult(searchResult.isHasAdult());
		result.setNumFound(searchResult.getNumFound());
		List<String> communityUserIds = new ArrayList<String>();
		for (CommunityUserFollowDO follow : searchResult.getDocuments()) {
			communityUserIds.add(
					follow.getCommunityUser().getCommunityUserId());
		}

		Map<String, Long> reviewCountMap
				= reviewDao.loadReviewCountMapByCommunityUserId(
						communityUserIds);
		Map<String, Long> questionCountMap
				= questionDao.loadQuestionCountMapByCommunityUserId(
						communityUserIds);
		Map<String, Long> questionAnswerCountMap
				= questionAnswerDao.loadQuestionAnswerCountMapByCommunityUserId(
				communityUserIds);
		Map<String, Long> imageCountMap
				= imageDao.loadImageCountMapByCommunityUserId(communityUserIds);

		Map<String, Long> productMasterCountMap
				= productMasterDao.loadProductMasterCountMapByCommunityUserId(
						communityUserIds);
		Map<String, Boolean> followMap
				= communityUserFollowDao.loadCommunityUserFollowMap(
						communityUserId, communityUserIds);

		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(searchResult.getDocuments());
		for (CommunityUserFollowDO follow : searchResult.getDocuments()) {
			CommunityUserFollowVO vo = new CommunityUserFollowVO();
			vo.setCommunityUser(follow.getCommunityUser());
			vo.setFollowDate(follow.getFollowDate());
			if (reviewCountMap.containsKey(
					follow.getCommunityUser().getCommunityUserId())) {
				vo.setPostReviewCount(
						reviewCountMap.get(
						follow.getCommunityUser().getCommunityUserId()));
			}
			if (questionCountMap.containsKey(
					follow.getCommunityUser().getCommunityUserId())) {
				vo.setPostQuestionCount(
						questionCountMap.get(
						follow.getCommunityUser().getCommunityUserId()));
			}
			if (questionAnswerCountMap.containsKey(
					follow.getCommunityUser().getCommunityUserId())) {
				vo.setPostAnswerCount(
						questionAnswerCountMap.get(
						follow.getCommunityUser().getCommunityUserId()));
			}
			if (imageCountMap.containsKey(
					follow.getCommunityUser().getCommunityUserId())) {
				vo.setPostImageCount(
						imageCountMap.get(
						follow.getCommunityUser().getCommunityUserId()));
			}
			if (productMasterCountMap.containsKey(
					follow.getCommunityUser().getCommunityUserId())) {
				vo.setProductMasterCount(
						productMasterCountMap.get(
						follow.getCommunityUser().getCommunityUserId()));
			}
			SearchResult<CommunityUserFollowDO> followUsers
					= communityUserFollowDao.findFollowCommunityUser(
							follow.getCommunityUser().getCommunityUserId(),
							followUserLimit, null, false);
			vo.setFollowUserCount(followUsers.getNumFound());
			for (CommunityUserFollowDO info : followUsers.getDocuments()) {
				if (info.getFollowCommunityUser().isStop(requestScopeDao)) {
					continue;
				}
				vo.getLatestFollowUsers().add(info.getFollowCommunityUser());
			}
			SearchResult<CommunityUserFollowDO> followerUsers
					= communityUserFollowDao.findFollowerCommunityUser(
							follow.getCommunityUser().getCommunityUserId(),
							followUserLimit, null, false);
			vo.setFollowerUserCount(followerUsers.getNumFound());
			for (CommunityUserFollowDO info : followerUsers.getDocuments()) {
				if (info.getCommunityUser().isStop(requestScopeDao)) {
					continue;
				}
				vo.getLatestFollowerUsers().add(info.getCommunityUser());
			}

			if (followMap.containsKey(
					follow.getCommunityUser().getCommunityUserId())) {
				vo.setFollowingFlg(
						followMap.get(
						follow.getCommunityUser().getCommunityUserId()));
			}

			result.updateFirstAndLast(vo);
			if (follow.isStop(communityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				continue;
			}
			result.getDocuments().add(vo);
		}
		return result;
	}

	/**
	 * フォローしている商品情報をフォロー日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param followUserLimit フォローユーザーの最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 検索結果
	 */
	@Override
	public SearchResult<ProductFollowVO> findFollowProduct(
			String communityUserId, int limit,
			int followUserLimit, Date offsetTime, boolean previous) {
		SearchResult<ProductFollowDO> searchResult
				= productFollowDao.findFollowProduct(
				communityUserId, limit, offsetTime, previous);
		SearchResult<ProductFollowVO> result
				= new SearchResult<ProductFollowVO>();
		result.setHasAdult(searchResult.isHasAdult());
		result.setNumFound(searchResult.getNumFound());
		List<String> skus = new ArrayList<String>();
		for (ProductFollowDO follow : searchResult.getDocuments()) {
			skus.add(
					follow.getFollowProduct().getSku());
		}

		Map<String, Long> reviewCountMap
				= reviewDao.loadReviewCountMapBySKU(
						skus);
		Map<String, Long> questionCountMap
				= questionDao.loadQuestionCountMapBySKU(
						skus, false, null);
		Map<String, Long> imageCountMap
				= imageDao.loadImageCountMapBySKU(skus);
		Map<String, Long> productMasterMap
				= productMasterDao.loadProductMasterCountMapBySKU(skus);

		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(searchResult.getDocuments());
		for (ProductFollowDO follow : searchResult.getDocuments()) {
			String sku = follow.getFollowProduct().getSku();
			
			ProductFollowVO vo = new ProductFollowVO();
			vo.setProduct(follow.getFollowProduct());
			vo.setFollowDate(follow.getFollowDate());

			if (reviewCountMap.containsKey(sku)) {
				vo.setPostReviewCount(reviewCountMap.get(sku));
			}
			if (questionCountMap.containsKey(sku)) {
				vo.setPostQuestionCount(questionCountMap.get(sku));
			}
			if (imageCountMap.containsKey(sku)) {
				vo.setPostImageCount(imageCountMap.get(sku));
			}
			if (productMasterMap.containsKey(sku)) {
				vo.setHasProductMaster(productMasterMap.get(sku) > 0);
			}
			
			// 商品満足度情報の取得
			List<ProductDO> variationProductList = productDao.findVariationProductBySku(sku);
			if (variationProductList == null || variationProductList.isEmpty()) {
				vo.setProductSatisfactionSummary(reviewService.getProductSatisfactionSummary(sku));
			} else {
				List<String> variationSkus = new ArrayList<String>();
				for (ProductDO variationProduct : variationProductList) {
					variationSkus.add(variationProduct.getSku());
				}
				variationSkus.add(sku);
				vo.setProductSatisfactionSummary(reviewService.getProductSatisfactionSummary(variationSkus.toArray(new String[0])));
			}

			SearchResult<CommunityUserDO> followers
					= productFollowDao.findFollowerCommunityUserBySKU(sku, followUserLimit, 0, false);
			for (Iterator<CommunityUserDO> it = followers.getDocuments().iterator(); it.hasNext();) {
				CommunityUserDO target = it.next();
				if (target.isStop(requestScopeDao)) {
					it.remove();
					followers.countUpStopContents();
				}
			}
			vo.setFollowerCount(followers.getNumFound());
			vo.setLatestFollowers(followers.getDocuments());

			result.updateFirstAndLast(vo);
			if (follow.isStop(communityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				continue;
			}
			result.getDocuments().add(vo);
		}
		return result;
	}

	/**
	 * フォローしている質問情報をフォロー日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param followUserLimit フォローユーザーの最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return 検索結果
	 */
	@Override
	public SearchResult<QuestionFollowVO> findFollowQuestion(
			String communityUserId, int limit,
			int followUserLimit, Date offsetTime, boolean previous) {
		
		SearchResult<QuestionFollowDO> searchResult
				= questionFollowDao.findFollowQuestion(communityUserId, limit, offsetTime, previous);
		SearchResult<QuestionFollowVO> result = new SearchResult<QuestionFollowVO>();
		result.setHasAdult(searchResult.isHasAdult());
		result.setNumFound(searchResult.getNumFound());
		List<String> questionIds = new ArrayList<String>();
		for (QuestionFollowDO follow : searchResult.getDocuments()) {
			questionIds.add(follow.getFollowQuestion().getQuestionId());
		}
		Map<String, Long> questionAnswerCountMap = questionAnswerDao.loadQuestionAnswerCountMapByQuestionId(questionIds);
		Map<String, List<ImageHeaderDO>> questionImageMap = imageDao.loadAllImageMapByContentsIds(PostContentType.QUESTION, questionIds);
		
		Set<String> stopCommunityUserIds = new HashSet<String>();
		for (QuestionFollowDO follow : searchResult.getDocuments()) {
			QuestionFollowVO vo = new QuestionFollowVO();
			QuestionDO questionDO = follow.getFollowQuestion();
			if (questionImageMap.containsKey(questionDO.getQuestionId())) {
				questionDO.setImageHeaders(questionImageMap.get(questionDO.getQuestionId()));
			}
			vo.setQuestion(questionDO);
			vo.setFollowDate(follow.getFollowDate());
			if (questionAnswerCountMap.containsKey(follow.getFollowQuestion().getQuestionId())) {
				vo.setAnswerCount(questionAnswerCountMap.get(follow.getFollowQuestion().getQuestionId()));
			}
			SearchResult<CommunityUserDO> followers
					= questionFollowDao.findFollowerCommunityUserByQuestionId(follow.getFollowQuestion().getQuestionId(), followUserLimit, 0, false);
			for (Iterator<CommunityUserDO> it = followers.getDocuments().iterator(); it.hasNext();) {
				CommunityUserDO target = it.next();
				if (target.isStop(requestScopeDao)) {
					it.remove();
					followers.countUpStopContents();
				}
			}
			vo.setFollowerCount(followers.getNumFound());
			vo.setLatestFollowers(followers.getDocuments());

			result.updateFirstAndLast(vo);
			if (follow.isStop(communityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				continue;
			}
			result.getDocuments().add(vo);
		}
		return result;
	}
	
	
	@Override
	@ArroundHBase
	public boolean followCommunityUserByUserName(
			String communityUserId,
			String followCommunityUserName, 
			boolean release) {
		String normalizeCommunityName = normalizeCharDao.normalizeString(followCommunityUserName);
		String followCommunityUserId = communityUserDao.loadCommunityUserIdByNormalizeCommunityName(normalizeCommunityName);
		return followCommunityUser(communityUserId, followCommunityUserId, release);
	}

	/**
	 * コミュニティユーザーをフォローします。
	 * @param communityUserId フォロワーとなるコミュニティユーザーID
	 * @param followCommunityUserId フォローするコミュニティユーザーID
	 * @param release フォローを解除する場合、true
	 * @return 成功した場合、true
	 */
	@Override
	@ArroundHBase
	public boolean followCommunityUser(
			String communityUserId,
			String followCommunityUserId,
			boolean release) {

		// 退会ユーザーの操作の場合無効
		if(!userService.validateUserStatusForPostContents(communityUserId))
			throw new UnActiveException("can not post contens because user status is failure " + " communityUserId:" + communityUserId);
		// 同一ユーザーの場合は、無効
		if (communityUserId.equals(followCommunityUserId)) {
			return true;
		}
		// フォロー対象が有効でない場合無効
		CommunityUserDO followCommunityUser = communityUserDao.load(followCommunityUserId, Path.includeProp("status"));
		if (followCommunityUser == null || !followCommunityUser.isActive()) {
			String communityUserStatus = (followCommunityUser != null ? (followCommunityUser.getStatus() != null ? followCommunityUser.getStatus().getCode() : "none") : "none");
			
			throw new UnActiveException("communityUser status is" + communityUserStatus + " communityUserId:" + followCommunityUserId);
		}
		if (release) {
			if (!communityUserFollowDao.existsCommunityUserFollow(communityUserId, followCommunityUserId)) {
				return false;
			}
			communityUserFollowDao.deleteFollowCommunityUser(communityUserId, followCommunityUserId);

			indexService.updateIndexForCommunityUserFollow(
					communityUserId, 
					followCommunityUserId,
					null,
					null);
		} else {
			// check Limit
			if(!canFollowCommunityUser(communityUserId))
				throw new FollowLimitException("follow communityUser limit is over. communityUserId=" + communityUserId);

			if (communityUserFollowDao.existsCommunityUserFollow(communityUserId, followCommunityUserId)) {
				return false;
			} else {
				communityUserFollowDao.createCommunityUserFollow(communityUserId, followCommunityUserId);
				ActionHistoryDO actionHistory = new ActionHistoryDO();
				actionHistory.setActionHistoryType(ActionHistoryType.USER_FOLLOW_USER);
				actionHistory.setCommunityUser(new CommunityUserDO());
				actionHistory.getCommunityUser().setCommunityUserId(communityUserId);
				actionHistory.setFollowCommunityUser(new CommunityUserDO());
				actionHistory.getFollowCommunityUser().setCommunityUserId(followCommunityUserId);
				actionHistoryDao.create(actionHistory);

				InformationDO information = new InformationDO();
				information.setInformationType(InformationType.FOLLOW);
				information.setCommunityUser(new CommunityUserDO());
				information.getCommunityUser().setCommunityUserId(followCommunityUserId);
				information.setFollowerCommunityUser(new CommunityUserDO());
				information.getFollowerCommunityUser().setCommunityUserId(communityUserId);
				information.setRelationCommunityUserId(communityUserId);

				informationDao.createInformation(information);

				indexService.updateIndexForCommunityUserFollow(
						communityUserId, followCommunityUserId,
						actionHistory.getActionHistoryId(),
						information.getInformationId());

				if (mailSettingDao.loadMailSettingValueWithDefault(
						followCommunityUserId, MailSettingType.USER_FOLLOW
						).equals(MailSendTiming.EVERYTIME_NOTIFY)) {
					mailService.sendFollowCommunityUserNotifyMail(
							communityUserId, followCommunityUserId);
				}
			}
		}
		return true;
	}

	/**
	 * 商品をフォローします。
	 * @param communityUserId フォロワーとなるコミュニティユーザーID
	 * @param followProductId フォローする商品ID
	 * @param release フォローを解除する場合、true
	 * @return 成功した場合、true
	 */
	@Override
	@ArroundHBase
	public boolean followProduct(
			String communityUserId,
			String followProductId,
			boolean release) {

		// コンテンツ投稿可能チェック
		if(!userService.validateUserStatusForPostContents(communityUserId))
			throw new UnActiveException("can not post contens because user status is failure " + " communityUserId:" + communityUserId);

		if (release) {
			if (!productFollowDao.existsProductFollow(communityUserId, followProductId)) {
				return false;
			}
			productFollowDao.deleteFollowProduct(
					communityUserId, followProductId);

			indexService.updateIndexForProductFollow(
					communityUserId, followProductId,
					null);
		} else {
			// check Limit
			if(!canFollowProduct(communityUserId))
				throw new FollowLimitException("follow product limit is over. communityUserId=" + communityUserId);

			if (productFollowDao.existsProductFollow(communityUserId, followProductId)) {
				return false;
			} else {
				ProductDO followProduct = productDao.loadProduct(followProductId);
				if (followProduct == null) {
					return false;
				}
				productFollowDao.createProductFollow(
						communityUserId, followProductId,
						followProduct.isAdult());
				ActionHistoryDO actionHistory = new ActionHistoryDO();
				actionHistory.setActionHistoryType(ActionHistoryType.USER_FOLLOW_PRODUCT);
				actionHistory.setCommunityUser(new CommunityUserDO());
				actionHistory.getCommunityUser().setCommunityUserId(communityUserId);
				actionHistory.setProduct(followProduct);
				actionHistory.setAdult(followProduct.isAdult());
				actionHistoryDao.create(actionHistory);

				indexService.updateIndexForProductFollow(
						communityUserId, followProductId,
						actionHistory.getActionHistoryId());
			}
		}
		return true;
	}

	/**
	 * 質問をフォローします。
	 * @param communityUserId フォロワーとなるコミュニティユーザーID
	 * @param followQuestionId フォローする質問ID
	 * @param release フォローを解除する場合、true
	 * @return 成功した場合、true
	 */
	@Override
	@ArroundHBase
	public boolean followQuestion(
			String communityUserId,
			String followQuestionId,
			boolean release) {
		// コンテンツ投稿可能チェック
		if(!userService.validateUserStatusForPostContents(communityUserId))
			throw new UnActiveException("can not post contens because user status is failure " + " communityUserId:" + communityUserId);

		QuestionDO followQuestion = questionDao.loadQuestion(
				followQuestionId, Path.includeProp("status,communityUserId,productId,adult")
				.includePath("communityUser.status,communityUser.keepQuestionContents").depth(1), false);
		
		if(followQuestion != null && 
				followQuestion.getCommunityUser().getStatus().equals(CommunityUserStatus.STOP) &&
				!followQuestion.getCommunityUser().getCommunityUserId().equals(communityUserId)){
			throw new IllegalArgumentException("FollowUser and QuestionUser are the same users.");
		}
		
		if (release) {
			if (!questionFollowDao.existsQuestionFollow(communityUserId, followQuestionId)) {
				return false;
			}
			questionFollowDao.deleteFollowQuestion(
					communityUserId, followQuestionId);

			indexService.updateIndexForQuestionFollow(
					communityUserId, followQuestionId,
					null);
		} else {
			// check Limit
			if(!canFollowQuestion(communityUserId))
				throw new FollowLimitException("follow question limit is over. communityUserId=" + communityUserId);

			if (questionFollowDao.existsQuestionFollow(communityUserId, followQuestionId)) {
				return false;
			} else {
				if (followQuestion == null || followQuestion.isDeleted() ||
						followQuestion.getStatus() != ContentsStatus.SUBMITTED) {
					return false;
				}
				if (followQuestion.getCommunityUser() == null
						|| (followQuestion.getCommunityUser().getStatus() != CommunityUserStatus.ACTIVE
							&& !followQuestion.getCommunityUser().isKeepQuestionContents())) {
					throw new UnActiveException("can not follow question because deleted communityUser questionId:" + followQuestionId);
				}

				questionFollowDao.createQuestionFollow(
						communityUserId, followQuestionId,
						followQuestion.getCommunityUser().getCommunityUserId(),
						followQuestion.isAdult());
				ActionHistoryDO actionHistory = new ActionHistoryDO();
				actionHistory.setActionHistoryType(ActionHistoryType.USER_FOLLOW_QUESTION);
				actionHistory.setCommunityUser(new CommunityUserDO());
				actionHistory.getCommunityUser().setCommunityUserId(communityUserId);
				actionHistory.setAdult(followQuestion.isAdult());
				actionHistory.setProduct(followQuestion.getProduct());
				actionHistory.setQuestion(new QuestionDO());
				actionHistory.getQuestion().setQuestionId(followQuestionId);
				actionHistory.setRelationQuestionOwnerId(
						followQuestion.getCommunityUser().getCommunityUserId());
				actionHistoryDao.create(actionHistory);

				indexService.updateIndexForQuestionFollow(
						communityUserId, followQuestionId,
						actionHistory.getActionHistoryId());
			}
		}
		return true;
	}

	@Override
	public boolean canFollowCommunityUser(String communityUserId) {
		return serviceConfig.followCommunityUserLimit > communityUserFollowDao.countFollowCommunityUser(communityUserId);
	}

	@Override
	public boolean canFollowProduct(String communityUserId) {
		return serviceConfig.followProductLimit > productFollowDao.countFollowProduct(communityUserId);
	}

	@Override
	public boolean canFollowQuestion(String communityUserId) {
		return serviceConfig.followQuestionLimit > questionFollowDao.countFollowQuestion(communityUserId);
	}

	@Override
	public long countFollowProduct(String communityUserId) {
		return productFollowDao.countFollowProduct(communityUserId);
	}

	@Override
	public long countFollowQuestion(String communityUserId) {
		return questionFollowDao.countFollowQuestion(communityUserId);
	}

	@Override
	public long countFollowUser(String communityUserId) {
		return communityUserFollowDao.countFollowCommunityUser(communityUserId);
	}

	@Override
	public long countFollowerUser(String communityUserId) {
		return communityUserFollowDao.countFollowerCommunityUser(communityUserId);
	}

}
