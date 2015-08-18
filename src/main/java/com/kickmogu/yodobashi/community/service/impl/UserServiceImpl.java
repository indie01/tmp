/**
 *
 */
package com.kickmogu.yodobashi.community.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.kickmogu.lib.core.aop.MyselfAware;
import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.exception.UniqueConstraintException;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.lib.core.utils.StringUtil;
import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.common.exception.DataNotFoundException;
import com.kickmogu.yodobashi.community.common.exception.SecurityException;
import com.kickmogu.yodobashi.community.common.exception.SpoofingNameException;
import com.kickmogu.yodobashi.community.common.exception.UnMatchStatusException;
import com.kickmogu.yodobashi.community.common.utils.ProfileUtil;
import com.kickmogu.yodobashi.community.resource.cache.CacheStrategyType;
import com.kickmogu.yodobashi.community.resource.cache.MethodCache;
import com.kickmogu.yodobashi.community.resource.cache.TargetSystemType;
import com.kickmogu.yodobashi.community.resource.config.DomainConfig;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.dao.ActionHistoryDao;
import com.kickmogu.yodobashi.community.resource.dao.AnnounceDao;
import com.kickmogu.yodobashi.community.resource.dao.AuthenticationDao;
import com.kickmogu.yodobashi.community.resource.dao.CommentDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.ImageDao;
import com.kickmogu.yodobashi.community.resource.dao.InformationDao;
import com.kickmogu.yodobashi.community.resource.dao.LikeDao;
import com.kickmogu.yodobashi.community.resource.dao.LoginDao;
import com.kickmogu.yodobashi.community.resource.dao.MailSettingDao;
import com.kickmogu.yodobashi.community.resource.dao.NormalizeCharDao;
import com.kickmogu.yodobashi.community.resource.dao.OrderDao;
import com.kickmogu.yodobashi.community.resource.dao.OuterCustomerDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductMasterDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionAnswerDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.ReviewDao;
import com.kickmogu.yodobashi.community.resource.dao.SocialMediaSettingDao;
import com.kickmogu.yodobashi.community.resource.dao.UniversalSessionManagerDao;
import com.kickmogu.yodobashi.community.resource.dao.VotingDao;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.domain.AccountSharingDO;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.AnnounceDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.LoginDO;
import com.kickmogu.yodobashi.community.resource.domain.MailSettingDO;
import com.kickmogu.yodobashi.community.resource.domain.MailSettingMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.SocialMediaSettingDO;
import com.kickmogu.yodobashi.community.resource.domain.SpoofingNameDO;
import com.kickmogu.yodobashi.community.resource.domain.ValidateAuthSessionDO;
import com.kickmogu.yodobashi.community.resource.domain.VersionDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.AnnounceType;
import com.kickmogu.yodobashi.community.resource.domain.constants.AsyncMessageType;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;
import com.kickmogu.yodobashi.community.resource.domain.constants.LikeMessageType;
import com.kickmogu.yodobashi.community.resource.domain.constants.LikePrefixType;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSettingType;
import com.kickmogu.yodobashi.community.resource.domain.constants.MailSettingType.MailSettingCategory;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SocialMediaType;
import com.kickmogu.yodobashi.community.resource.domain.constants.Verification;
import com.kickmogu.yodobashi.community.service.AggregateOrderService;
import com.kickmogu.yodobashi.community.service.IndexService;
import com.kickmogu.yodobashi.community.service.MailService;
import com.kickmogu.yodobashi.community.service.MigrationUserService;
import com.kickmogu.yodobashi.community.service.UserService;
import com.kickmogu.yodobashi.community.service.annotation.SendMessage;
import com.kickmogu.yodobashi.community.service.annotation.SendMessage.Timing;
import com.kickmogu.yodobashi.community.service.config.ServiceConfig;
import com.kickmogu.yodobashi.community.service.vo.CommentSetVO;
import com.kickmogu.yodobashi.community.service.vo.CommunityUserFollowVO;
import com.kickmogu.yodobashi.community.service.vo.CommunityUserSetVO;
import com.kickmogu.yodobashi.community.service.vo.MailSettingCategoryVO;
import com.kickmogu.yodobashi.community.service.vo.MailSettingVO;
import com.kickmogu.yodobashi.community.service.vo.NewsFeedVO;
import com.kickmogu.yodobashi.community.service.vo.UserPageInfoAreaVO;

/**
 * コミュニティユーザーサービスの実装です。
 * @author kamiike
 *
 */
@Service
public class UserServiceImpl extends UserPageCommonServiceImpl implements UserService, MyselfAware<UserService> {

	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

	@Autowired
	private ApplicationContext applicationContext;
	
	/**
	 * アクション履歴 DAO です。
	 */
	@Autowired
	private ActionHistoryDao actionHistoryDao;

	/**
	 * アナウンス DAO です。
	 */
	@Autowired
	private AnnounceDao announceDao;

	/**
	 * コメント DAO です。
	 */
	@Autowired
	private CommentDao commentDao;

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
	 * いいね DAO です。
	 */
	@Autowired
	private LikeDao likeDao;
	
	/**
	 * いいね DAO です。
	 */
	@Autowired
	private VotingDao votingDao;

	/**
	 * メール設定 DAO です。
	 */
	@Autowired
	private MailSettingDao mailSettingDao;

	/**
	 * 標準化文字 DAO です。
	 */
	@Autowired
	private NormalizeCharDao normalizeCharDao;

	/**
	 * 外部顧客情報 DAO です。
	 */
	@Autowired @Qualifier("xi")
	private OuterCustomerDao outerCustomerDao;

	/**
	 * 注文 DAO です。
	 */
	@Autowired
	private OrderDao orderDao;

	/**
	 * 商品マスター DAO です。
	 */
	@Autowired
	private ProductMasterDao productMasterDao;

	/**
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	private RequestScopeDao requestScopeDao;

	/**
	 * 質問 DAO です。
	 */
	@Autowired
	private QuestionDao questionDao;

	/**
	 * 質問フォロー DAO です。
	 */
	@Autowired
	private QuestionFollowDao questionFollowDao;

	/**
	 * 質問回答 DAO です。
	 */
	@Autowired
	private QuestionAnswerDao questionAnswerDao;

	/**
	 * ソーシャルメディア連携設定 DAO です。
	 */
	@Autowired
	private SocialMediaSettingDao socialMediaSettingDao;

	/**
	 * レビュー DAO です。
	 */
	@Autowired
	private ReviewDao reviewDao;

	/**
	 * Login
	 */
	@Autowired
	private LoginDao loginDao;
	
	/**
	 * ユニバーサルセッション管理Dao です。
	 *
	 */
	@Autowired @Qualifier("xi")
	private UniversalSessionManagerDao universalSessionManagerDao;

	/**
	 * メールサービスです。
	 */
	@Autowired
	private MailService mailService;

	/**
	 * インデックスサービスです。
	 */
	@Autowired
	private IndexService indexService;

	/**
	 * 注文集約サービスです。
	 */
	@Autowired
	private AggregateOrderService aggregateOrderService;

	@Autowired
	private MigrationUserService migrationUserService;

	/**
	 * ドメインコンフィグです。
	 */
	@Autowired
	private DomainConfig domainConfig;

	/**
	 * タイムスタンプホルダーです。
	 */
	@Autowired
	private TimestampHolder timestampHolder;

	@Autowired
	private ServiceConfig serviceConfig;

	@Autowired
	private ResourceConfig resourceConfig;

	/**
	 * 自身のインスタンスです。
	 */
	private UserService myself;

	@Autowired AuthenticationDao authenticationDao;

	/**
	 * 自身のインスタンスを設定します。
	 * @param myself 自身のインスタンス
	 */
	@Override
	public void setMyself(UserService myself) {
		this.myself = myself;
	}

	/**
	 * 指定したユニバーサルセッションIDからコミュニティIDを発行します。
	 * @param universalSessionId ユニバーサルセッションID
	 * @return コミュニティID
	 */
	@Override
	public String createCommunityIdByUniversalSessionId(String universalSessionId) {
		//コミュニティID取得（XI）
		String communityId = outerCustomerDao.createCommunityId(universalSessionId);

		try {
			universalSessionManagerDao.deleteUniversalSession(universalSessionId);
		} catch (Exception e) {
			LOG.warn("fail delete universal session. ignore exception. no problem.", e);
		}
		return communityId;
	}

	/**
	 * 指定したユニバーサルセッションIDで保存されたコミュニティIDを返します。
	 * @param universalSessionId ユニバーサルセッションID
	 * @return コミュニティID
	 */
	@Override
	public String getCommunityIdByUniversalSessionId(String universalSessionId) {
		return universalSessionManagerDao.loadOuterCustomerId(universalSessionId);
	}

	/**
	 * コミュニティIDに紐づくコミュニティユーザーを返します。
	 * @param communityId コミュニティID
	 * @param syncStatus ステータスを厳密に取得するかどうか
	 * @return コミュニティユーザー
	 */
	@Override
	@ArroundHBase
	public CommunityUserDO getCommunityUserByCommunityId(
			String communityId, boolean syncStatus) {
		return communityUserDao.loadByHashCommunityId(
				domainConfig.createHashCommunityId(communityId),
				Path.DEFAULT,
						false,
						syncStatus);
	}

	/**
	 * コミュニティユーザーを新規登録します。
	 * @param communityUser コミュニティユーザー
	 * @param icOuterCustomerId IC外部顧客ID
	 * @return 登録されたコミュニティユーザー
	 */
	@Override
	@ArroundSolr
	@ArroundHBase
	public CommunityUserDO createCommunityUser(
			CommunityUserDO communityUser,
			String icOuterCustomerId,
			String autoId,
			boolean agreement
			) {
		communityUser.setSecureAccess(ProfileUtil.isCommunityUserProfileSecureAccess(applicationContext));
		
		String normalizeCommunityName = normalizeCharDao.normalizeString(
				communityUser.getCommunityName());
		if (duplicateNormalizeCommunityNameForCreate(icOuterCustomerId,
				communityUser.getCommunityName(), normalizeCommunityName, true)) {
			throw new UniqueConstraintException();
		}
		
		/* 20120627 コミュニティニックネーム重複対応 */
		String spoofingNamePattern = normalizeCharDao.getSpoofingPattern(normalizeCommunityName);
		String spoofingNameId = StringUtil.toSHA256(spoofingNamePattern);

		
		if(!normalizeCharDao.validateSpoofingPattern(spoofingNamePattern, true)){
			throw new SpoofingNameException("community name is spoofing communityId:" + communityUser.getCommunityId() + " communityName:" + communityUser.getCommunityName() + " normalizeName:" + normalizeCommunityName);
		}
		SpoofingNameDO spoofingName = new SpoofingNameDO();
		spoofingName.setSpoofingNameId(spoofingNameId);
		spoofingName.setSpoofingPattern(spoofingNamePattern);
		spoofingName.setSpoofingName(normalizeCommunityName);
		
		communityUser.setNormalizeCommunityName(normalizeCommunityName);
		
		
		communityUser.setHashCommunityId(
				domainConfig.createHashCommunityId(communityUser.getCommunityId()));
		communityUser.setStatus(CommunityUserStatus.ACTIVE);
		communityUser.setCommunityUserId(
				communityUserDao.issueCommunityUserId());

		String profileImageId = null;
		String thumbnailImageId = null;

		//アップロードされた場合のみ、画像登録処理を行います。
		if (communityUser.getImageHeader() != null && StringUtils.isNotEmpty(
				communityUser.getImageHeader().getImageId())) {
			ImageHeaderDO imageHeader = communityUser.getImageHeader();
			imageHeader.setOwnerCommunityUser(communityUser);
			imageHeader.setCommunityUser(communityUser);
			imageHeader.setPostContentType(PostContentType.PROFILE);

			imageDao.saveAndUploadImage(imageHeader);
			profileImageId = imageHeader.getImageId();
			communityUser.setProfileImageUrl(
					communityUser.getImageHeader().getImageUrl());

			ImageHeaderDO thumbnail = communityUser.getThumbnail();
			thumbnail.setOwnerCommunityUser(communityUser);
			thumbnail.setThumbnailUser(communityUser);
			thumbnail.setPostContentType(PostContentType.PROFILE_THUMBNAIL);
			thumbnail.setThumbnail(true);

			imageDao.saveAndUploadImage(thumbnail);
			thumbnailImageId = thumbnail.getImageId();
			communityUser.setThumbnailImageUrl(
					communityUser.getThumbnail().getImageUrl());
		}

		//コミュニティユーザーを登録します。
		communityUser.setAccountSharingCaches(
				outerCustomerDao.findAccountSharingByOuterCustomerId(
						communityUser.getCommunityId()));
		communityUserDao.createCommunityUser(communityUser, spoofingName);

		if(StringUtils.isNotEmpty(autoId)){
			LoginDO login = new LoginDO();
			login.setLoginId(autoId);
			login.setCommunityId(communityUser.getCommunityId());
			login.setCommunityUserId(communityUser.getCommunityUserId());
			login.setLastAccessDate(timestampHolder.getTimestamp());
			login.setModifyDateTime(timestampHolder.getTimestamp());
			login.setRegisterDateTime(timestampHolder.getTimestamp());
			loginDao.save(login);
		}

		myself.callAggregateOrder(communityUser);


		if(agreement) {
			// 参加規約のアナウンス情報を登録します。
			AnnounceDO announce = new AnnounceDO();
			announce.setCommunityUserId(communityUser.getCommunityUserId());
			announce.setType(AnnounceType.PARTICIPATING_AGREEMENT);
			announce.setDeleteFlag(true);
			announce.setDeleteDate(timestampHolder.getTimestamp());
			announceDao.create(announce);
		}
		
		InformationDO information = new InformationDO();
		information.setInformationType(InformationType.WELCOME);
		information.setCommunityUser(communityUser);
		informationDao.createInformation(information);

		indexService.updateIndexForCreateCommunityUser(
				communityUser.getCommunityUserId(),
				profileImageId,
				thumbnailImageId,
				information.getInformationId());

		//友達招待のアナウンス情報を登録します。
		AnnounceDO announce = new AnnounceDO();
		announce.setCommunityUserId(communityUser.getCommunityUserId());
		announce.setType(AnnounceType.WELCOME_HINT);
		announceDao.create(announce);

		mailService.sendRegistrationCompleteMail(
				communityUser.getCommunityUserId());

		return communityUser;

	}

	/**
	 * 注文情報のサマリ処理を非同期で実行します。
	 * @param communityId コミュニティID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.DELAYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void callAggregateOrder(CommunityUserDO communityUser) {
		List<String> outerCustomerIds = new ArrayList<String>();
		for (AccountSharingDO accountSharing : outerCustomerDao.findAccountSharingByOuterCustomerId(communityUser.getCommunityId())) {
			outerCustomerIds.add(accountSharing.getOuterCustomerId());
		}
		aggregateOrderService.aggregateOrder(outerCustomerIds);
		migrationUserService.checkAndMigrateReview(communityUser.getCommunityUserId());
	}

	/**
	 * 指定したユーザーのユーザーページ向け情報エリア情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param purchaseProductLimit 購入商品の最大取得件数
	 * @param productMasterLimit 商品マスターの最大取得件数
	 * @param imageLimit 画像の最大取得件数
	 * @return ユーザーページ向け共通情報エリア情報
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=1000,
			limitTime=5,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb, TargetSystemType.CommunityDataSyncEngine}
			)
	public UserPageInfoAreaVO getUserPageInfoAreaByCommunityUserId(
			String communityUserId,
			int purchaseProductLimit,
			int productMasterLimit,
			int imageLimit) {
		UserPageInfoAreaVO userPageInfoArea = new UserPageInfoAreaVO();
		CommunityUserDO communityUser = communityUserDao.load(
				communityUserId, Path.DEFAULT);
		//退会ユーザーの情報も返します。
		if (communityUser == null) {
			throw new DataNotFoundException(
					"CommunityUser is not found. communityUserId = "
					+ communityUserId);
		}
		userPageInfoArea.setProfileCommunityUser(communityUser);

		for (SocialMediaSettingDO setting : socialMediaSettingDao.findBySocialMediaSettingByCommunityUserId(
				communityUserId)) {
			if (setting.getSocialMediaType().equals(SocialMediaType.TWITTER) && setting.isLinkFlag()) {
				userPageInfoArea.setLinkTwitter(true);
			} else if (setting.getSocialMediaType().equals(SocialMediaType.FACEBOOK) && setting.isLinkFlag()) {
				userPageInfoArea.setLinkFacebook(true);
			}
		}

		//投稿レビュー数です。
		userPageInfoArea.setPostReviewCount(
				reviewDao.countReviewByCommunityUserId(
						communityUserId,
						ContentsStatus.SUBMITTED));

		//投稿質問数です。
		userPageInfoArea.setPostQuestionCount(
				questionDao.countQuestionByCommunityUserId(
						communityUserId,
						ContentsStatus.SUBMITTED));

		//投稿質問回答数です。
		userPageInfoArea.setPostQuestionAnswerCount(
				questionAnswerDao.countPostQuestionAnswerCount(
						communityUserId, ContentsStatus.SUBMITTED,
						requestScopeDao.loadAdultVerification()));

		//投稿画像数です。
		userPageInfoArea.setPostImageCount(imageDao.countImageSetByCommunityUserId(
				communityUserId));
		
		// 全投稿数です。
		userPageInfoArea.setPostAllCount(userPageInfoArea.getPostReviewCount() + userPageInfoArea.getPostQuestionCount() + userPageInfoArea.getPostQuestionAnswerCount() * userPageInfoArea.getPostImageCount());

		SearchResult<PurchaseProductDO> purchaseProducts = 
				findPurchaseProductByCommunityUserId(communityUserId, true, purchaseProductLimit);
		
		//購入商品数です。
		userPageInfoArea.setPurchaseProductCount(purchaseProducts.getNumFound());
		//購入商品リストです。
		userPageInfoArea.setPurchaseProducts(purchaseProducts.getDocuments());
		if (purchaseProducts.isHasAdult()) {
			userPageInfoArea.setHasAdult(true);
		}

		SearchResult<ProductMasterDO> productMasters
				= productMasterDao.findRankProductMasterByCommunityUserId(
				communityUserId, productMasterLimit,
				null, null, false, true,
				requestScopeDao.loadAdultVerification());
		//商品マスター数です。
		userPageInfoArea.setProductMasterCount(productMasters.getNumFound());
		//商品マスターのリストです。
		userPageInfoArea.setProductMasters(productMasters.getDocuments());
		if (productMasters.isHasAdult()) {
			userPageInfoArea.setHasAdult(true);
		}
		
		// 投稿画像です。
		SearchResult<ImageHeaderDO> userImageSearchResult = imageDao.loadThumbnailImagesByOwnerCommunityUserId(communityUserId, imageLimit);
		userPageInfoArea.setImages(userImageSearchResult.getDocuments());
		userPageInfoArea.setUserPostedImageCount(userImageSearchResult.getNumFound());

		return userPageInfoArea;
	}

	@Override
	public boolean existsCommunityUserFollow(String followCommunityUserId) {
		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();
		return communityUserFollowDao.existsCommunityUserFollow(
				loginCommunityUserId, followCommunityUserId);
	}

	

	
	
	/**
	 * 指定したコミュニティユーザーの投稿系のアクティビティを投稿日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return アクティビティ一覧
	 */
	@Override
	public SearchResult<NewsFeedVO> findTimelineActivityByCommunityUserId(
			String communityUserId,
			int limit,
			Date offsetTime,
			boolean previous){
		SearchResult<ActionHistoryDO> searchResult = actionHistoryDao.findTimelineActivityByCommunityUserId(
				communityUserId, limit, offsetTime, previous);
		
		SearchResult<NewsFeedVO> result = new SearchResult<NewsFeedVO>();
		result.setHasAdult(searchResult.isHasAdult());
		result.setNumFound(searchResult.getNumFound());
		List<String> reviewIds = new ArrayList<String>();
		List<String> questionIds = new ArrayList<String>();
		List<String> questionAnswerIds = new ArrayList<String>();
		List<String> imageSetIds = new ArrayList<String>();
		List<ReviewDO> reviewDOs = new ArrayList<ReviewDO>();
		for (ActionHistoryDO actionHistory : searchResult.getDocuments()) {
			if (actionHistory.getReview() != null) {
				reviewIds.add(actionHistory.getReview().getReviewId());
				reviewDOs.add(actionHistory.getReview());
			}
			if (actionHistory.getQuestion() != null) {
				questionIds.add(actionHistory.getQuestion().getQuestionId());
			}
			if (actionHistory.getQuestionAnswer() != null) {
				questionAnswerIds.add(actionHistory.getQuestionAnswer().getQuestionAnswerId());
			}
			if (actionHistory.getImageSetId() != null) {
				imageSetIds.add(actionHistory.getImageSetId());
			}
		}
		Map<String, Long> reviewCommentCountMap = new HashMap<String, Long>();
		Map<String, Long> questionCommentCountMap = new HashMap<String, Long>();
		Map<String, Long> imageSetCommentCountMap = new HashMap<String, Long>();
		Map<String, Long> questionAnswerCommentCountMap = new HashMap<String, Long>();
		
		Map<String, Long> reviewLikeCountMap = new HashMap<String, Long>();
		Map<String, Long> questionLikeCountMap = new HashMap<String, Long>();
		Map<String, Long> questionAnswerLikeCountMap = new HashMap<String, Long>();
		Map<String, Long> imageSetLikeCountMap = new HashMap<String, Long>();
		
		Map<String, Long> sameProductReviewCountMap = new HashMap<String, Long>();
		
		Map<String, List<ImageHeaderDO>> reviewAllImageMap = new HashMap<String, List<ImageHeaderDO>>();
		Map<String, List<ImageHeaderDO>> questionAllImageMap = new HashMap<String, List<ImageHeaderDO>>();
		Map<String, List<ImageHeaderDO>> questionAnswerAllImageMap = new HashMap<String, List<ImageHeaderDO>>();
		Map<String, List<ImageHeaderDO>> imageSetAllImageMap = new HashMap<String, List<ImageHeaderDO>>();
		
		Map<String, Long[]> reviewVotingCountMap = new HashMap<String, Long[]>();
		Map<String, Long[]> questionVotingCountMap = new HashMap<String, Long[]>();
		Map<String, Long[]> questionAnswerVotingCountMap = new HashMap<String, Long[]>();
		Map<String, Long[]> imageSetVotingCountMap = new HashMap<String, Long[]>();
		
		commentDao.loadContentsCommentCountMap(reviewIds, questionIds,questionAnswerIds, imageSetIds,
				reviewCommentCountMap,questionCommentCountMap, questionAnswerCommentCountMap,imageSetCommentCountMap);
		
		likeDao.loadContentsLikeCountMap(reviewIds, questionIds, questionAnswerIds, imageSetIds, null,
				reviewLikeCountMap, questionLikeCountMap, questionAnswerLikeCountMap, imageSetLikeCountMap, null);
		
		votingDao.loadContentsVotingCountMap(reviewIds, questionIds, questionAnswerIds, imageSetIds,null, 
				reviewVotingCountMap, questionVotingCountMap, questionAnswerVotingCountMap, imageSetVotingCountMap, null);
		
		imageDao.loadAllImageMapByContentsIds(
				reviewIds,
				questionIds,
				questionAnswerIds,
				imageSetIds,
				reviewAllImageMap,
				questionAllImageMap,
				questionAnswerAllImageMap,
				imageSetAllImageMap);
		
		// 同じ商品・ユーザーで、他のレビュー数
		sameProductReviewCountMap = reviewDao.loadSameProductReviewCountMap(reviewDOs);
		
		Map<String, Boolean> likeReviewMap = new HashMap<String, Boolean>();
		Map<String, Boolean> likeAnswerMap = new HashMap<String, Boolean>();
		Map<String, Boolean> hasAnswerMap = new HashMap<String, Boolean>();
		Map<String, Boolean> questionFollowMap = new HashMap<String, Boolean>();
		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();
		if( StringUtils.isNotEmpty(loginCommunityUserId)){
			//いいね済みかどうか
			likeReviewMap = likeDao.loadReviewLikeMap(loginCommunityUserId, reviewIds);
			//いいね済みかどうか
			likeAnswerMap = likeDao.loadQuestionAnswerLikeMap(loginCommunityUserId, questionAnswerIds);
			//質問回答済みかどうか
			hasAnswerMap = hasQuestionAnswer(loginCommunityUserId, questionIds);
			// 質問フォロー済みかどうか
			questionFollowMap = questionFollowDao.loadQuestionFollowMap(loginCommunityUserId, questionIds);
		}
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(searchResult.getDocuments());
		for (ActionHistoryDO actionHistory : searchResult.getDocuments()) {
			NewsFeedVO vo = new NewsFeedVO();
			vo.setActionHistory(actionHistory);
			vo.setMyActionFlg(actionHistory.getCommunityUser().getCommunityUserId().equals(loginCommunityUserId));
			if (ActionHistoryType.USER_REVIEW.equals(actionHistory.getActionHistoryType()) ||
					ActionHistoryType.USER_REVIEW_COMMENT.equals(actionHistory.getActionHistoryType())) {
				Long reviewCommentCount = reviewCommentCountMap.get(actionHistory.getReview().getReviewId());
				Long reviewLikeCount = reviewLikeCountMap.get(actionHistory.getReview().getReviewId());
				Long[] reviewVotingCount = reviewVotingCountMap.get(actionHistory.getReview().getReviewId());
				
				if (reviewCommentCount != null) {
					vo.setCommentCount(reviewCommentCount);
				}
				if (reviewLikeCount != null) {
					vo.setLikeCount(reviewLikeCount);
				}
				if (reviewVotingCount != null) {
					vo.setVotingCountYes(reviewVotingCount[0]);
					vo.setVotingCountNo(reviewVotingCount[1]);
				}
				if (reviewAllImageMap.containsKey(actionHistory.getReview().getReviewId()))
					vo.setImages(reviewAllImageMap.get(actionHistory.getReview().getReviewId()));
				if( likeReviewMap.containsKey(actionHistory.getReview().getReviewId())){
					vo.setLikeFlg(likeReviewMap.get(actionHistory.getReview().getReviewId()));
				}
				if (sameProductReviewCountMap.containsKey(actionHistory.getReview().getReviewId())) {
					vo.setOtherReviewCount(sameProductReviewCountMap.get(actionHistory.getReview().getReviewId()));
				}
				// TODO あとで効率よく取れるように変更する。
				SearchResult<LikeDO> likes = findLikeByReviewId(
						actionHistory.getReview().getReviewId(),
						loginCommunityUserId,
						resourceConfig.evaluationAreaLikeReadLimit);
				
				long like_count = vo.getLikeCount() - (vo.isLikeFlg()? 1 : 0);
				if( vo.isLikeFlg() ){
					// getLikeCount()は、自分を含んだ数字であることが前提。
					if( likes.getDocuments().isEmpty() ){
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
				SearchResult<CommentSetVO> resultComment = findReviewCommentByReviewId(
						actionHistory.getReview().getReviewId(),
						null,
						resourceConfig.commentInitReadLimit,
						null,
						false);
				if( !resultComment.getDocuments().isEmpty() )
					Collections.reverse(resultComment.getDocuments());
				vo.setCommentViewRemainingCount(vo.getCommentCount() - resultComment.getDocuments().size());
				vo.setComments(resultComment);
			} else if (ActionHistoryType.USER_QUESTION.equals(actionHistory.getActionHistoryType()) || 
					ActionHistoryType.USER_FOLLOW_QUESTION.equals(actionHistory.getActionHistoryType())) {
				Long questionCommentCount = questionCommentCountMap.get(actionHistory.getQuestion().getQuestionId());
				Long questionLikeCount = questionLikeCountMap.get(actionHistory.getQuestion().getQuestionId());
				Long[] questionVotingCount = questionVotingCountMap.get(actionHistory.getQuestion().getQuestionId());
				
				if (questionCommentCount != null) {
					vo.setCommentCount(questionCommentCount);
				}
				if (questionLikeCount != null) {
					vo.setLikeCount(questionLikeCount);
				}
				if (questionVotingCount != null) {
					vo.setVotingCountYes(questionVotingCount[0]);
					vo.setVotingCountNo(questionVotingCount[1]);
				}
				if (questionAllImageMap.containsKey(actionHistory.getQuestion().getQuestionId()))
					vo.setImages(questionAllImageMap.get(actionHistory.getQuestion().getQuestionId()));
				if( hasAnswerMap.containsKey(actionHistory.getQuestion().getQuestionId())){
					vo.setAnswerFlg(hasAnswerMap.get(actionHistory.getQuestion().getQuestionId()));
				}
			} else if (ActionHistoryType.USER_ANSWER.equals(actionHistory.getActionHistoryType()) ||
					ActionHistoryType.USER_ANSWER_COMMENT.equals(actionHistory.getActionHistoryType())) {
				Long questionAnswerCommentCount = questionAnswerCommentCountMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId());
				Long questionAnswerLikeCount = questionAnswerLikeCountMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId());
				Long[] questionAnswerVotingCount = questionAnswerVotingCountMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId());
				
				if (questionAnswerCommentCount != null) {
					vo.setCommentCount(questionAnswerCommentCount);
				}
				if (questionAnswerLikeCount != null) {
					vo.setLikeCount(questionAnswerLikeCount);
				}
				if (questionAnswerVotingCount != null) {
					vo.setVotingCountYes(questionAnswerVotingCount[0]);
					vo.setVotingCountNo(questionAnswerVotingCount[1]);
				}
				if (questionAnswerAllImageMap.containsKey(actionHistory.getQuestionAnswer().getQuestionAnswerId()))
					vo.setImages(questionAnswerAllImageMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId()));
				if( likeAnswerMap.containsKey(actionHistory.getQuestionAnswer().getQuestionAnswerId())){
					vo.setLikeFlg(likeAnswerMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId()));
				}
				String questionId = actionHistory.getQuestionAnswer().getQuestion().getQuestionId();
				if (questionAllImageMap.containsKey(questionId)) {
					List<ImageHeaderDO> imageHeaderDOList = questionAllImageMap.get(questionId); 
					vo.setSubContentImages(imageHeaderDOList);
					vo.getActionHistory().getQuestion().setImageHeaders(imageHeaderDOList);
				}
				// TODO あとで効率よく取れるように変更する。
				SearchResult<LikeDO> likes = findLikeByQuestionAnswerId(
						actionHistory.getQuestionAnswer().getQuestionAnswerId(),
						loginCommunityUserId,
						resourceConfig.evaluationAreaLikeReadLimit);
				
				long like_count = vo.getLikeCount() - (vo.isLikeFlg()? 1 : 0);
				if( vo.isLikeFlg() ){
					// getLikeCount()は、自分を含んだ数字であることが前提。
					if( likes.getDocuments().isEmpty() ){
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
						actionHistory.getQuestionAnswer().getQuestionAnswerId(),
						null,
						resourceConfig.commentInitReadLimit,
						null,
						false);
				if( !resultComment.getDocuments().isEmpty() )
					Collections.reverse(resultComment.getDocuments());
				vo.setCommentViewRemainingCount(vo.getCommentCount() - resultComment.getDocuments().size());
				vo.setComments(resultComment);
			} else if (ActionHistoryType.USER_IMAGE.equals(actionHistory.getActionHistoryType())) {
				Long imageSetCommentCount = imageSetCommentCountMap.get(actionHistory.getImageSetId());
				Long imageSetLikeCount = imageSetLikeCountMap.get(actionHistory.getImageSetId());
				Long[] imageSetVotingCount = imageSetVotingCountMap.get(actionHistory.getImageSetId());
				if (imageSetCommentCount != null) {
					vo.setCommentCount(imageSetCommentCount);
				}
				if (imageSetLikeCount != null) {
					vo.setLikeCount(imageSetLikeCount);
				}
				if (imageSetVotingCount != null) {
					vo.setVotingCountYes(imageSetVotingCount[0]);
					vo.setVotingCountNo(imageSetVotingCount[1]);
				}
				if (imageSetAllImageMap.containsKey(actionHistory.getImageSetId())) {
					vo.setImageHeaders(imageSetAllImageMap.get(actionHistory.getImageSetId()));
				} else {
					result.updateFirstAndLast(vo);
					continue;
				}
			} else if (ActionHistoryType.USER_IMAGE_COMMENT.equals(actionHistory.getActionHistoryType())) {
				// FIXME ここのサマリー情報をとって設定するべきだが、画像詳細ページ（画像1枚の）が実装されるまでは、画像のサマリー情報は必要なし
				String contentId = null;
				List<ImageHeaderDO> images = null;
				if( actionHistory.getImageSetId() != null ){
					contentId = actionHistory.getImageSetId();
					images = imageSetAllImageMap.get(contentId);
					if(images == null || images.isEmpty()) continue;
					vo.setImageHeaders(images);
				}else if(actionHistory.getReview() != null && actionHistory.getReview().getReviewId() != null){
					contentId = actionHistory.getReview().getReviewId();
					images = reviewAllImageMap.get(contentId);
					if(images == null || images.isEmpty()) continue;
					vo.setImageHeaders(images);
				}else if(actionHistory.getQuestionAnswer() != null && actionHistory.getQuestionAnswer().getQuestionAnswerId() != null){
					contentId = actionHistory.getQuestionAnswer().getQuestionAnswerId();
					images = questionAnswerAllImageMap.get(contentId);
					if(images == null || images.isEmpty()) continue;
					vo.setImageHeaders(images);
				}else if(actionHistory.getQuestion() != null && actionHistory.getQuestion().getQuestionId() != null){
					contentId = actionHistory.getQuestion().getQuestionId();
					images = questionAllImageMap.get(contentId);
					if(images == null || images.isEmpty()) continue;
					vo.setImageHeaders(images);
				}else{
					result.updateFirstAndLast(vo);
					continue;
				}
				
				Long imageSetCommentCount = imageSetCommentCountMap.get(contentId);
				Long imageSetLikeCount = imageSetLikeCountMap.get(contentId);
				Long[] imageSetVotingCount = imageSetVotingCountMap.get(contentId);
				if (imageSetCommentCount != null) {
					vo.setCommentCount(imageSetCommentCount);
				}
				if (imageSetLikeCount != null) {
					vo.setLikeCount(imageSetLikeCount);
				}
				if (imageSetVotingCount != null) {
					vo.setVotingCountYes(imageSetVotingCount[0]);
					vo.setVotingCountNo(imageSetVotingCount[1]);
				}
			}
			
			if ( ActionHistoryType.USER_QUESTION.equals(actionHistory.getActionHistoryType()) ||
					ActionHistoryType.USER_ANSWER.equals(actionHistory.getActionHistoryType()) ||
					ActionHistoryType.USER_FOLLOW_QUESTION.equals(actionHistory.getActionHistoryType())) {
				if (questionFollowMap.containsKey(actionHistory.getQuestion().getQuestionId())) {
					vo.setFollowingFlg(questionFollowMap.get(actionHistory.getQuestion().getQuestionId()));
				}
			}
			
			result.updateFirstAndLast(vo);
			
			if (actionHistory.isStop(loginCommunityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				continue;
			}
			if (ActionHistoryType.USER_REVIEW.equals(actionHistory.getActionHistoryType()) ||
					ActionHistoryType.USER_ANSWER.equals(actionHistory.getActionHistoryType()) ||
					ActionHistoryType.USER_IMAGE.equals(actionHistory.getActionHistoryType()) ||
					ActionHistoryType.USER_REVIEW_COMMENT.equals(actionHistory.getActionHistoryType()) ||
					ActionHistoryType.USER_ANSWER_COMMENT.equals(actionHistory.getActionHistoryType())) {
				// コンテンツの購入情報を取得する。
				PurchaseProductDO purchaseProductDO = orderDao.loadPurchaseProductBySku(
						actionHistory.getCommunityUser().getCommunityUserId(),
						actionHistory.getProduct().getSku(),
						Path.DEFAULT,
						false);
				if( purchaseProductDO != null && !purchaseProductDO.isDeleted() ){
					vo.setPurchaseProduct( purchaseProductDO );
				}
				// ログインユーザーの購入情報を取得する。
				if( StringUtils.isNotEmpty(loginCommunityUserId)){
					purchaseProductDO = orderDao.loadPurchaseProductBySku(
							loginCommunityUserId,
							actionHistory.getProduct().getSku(), Path.DEFAULT, false);
					if( purchaseProductDO != null && !purchaseProductDO.isDeleted() ){
						vo.setLoginUserPurchaseProduct(purchaseProductDO);
					}
				}
			}
			
			//呼び出し側で一時停止対応をするため、処理しない
			result.getDocuments().add(vo);
		}
		return result;
	}
	
	/**
	 * 指定したアクション履歴が一時停止中かどうかを返します。
	 * @param actionHistory アクション履歴
	 * @param stopCommunityUserIds 一時停止中のコミュニティユーザーIDのリスト
	 * @return 一時停止中の場合、true
	 */
	@Override
	@ArroundSolr
	public boolean isStop(ActionHistoryDO actionHistory,
			Set<String> stopCommunityUserIds) {
		return actionHistory.isStop(requestScopeDao.loadCommunityUserId(), stopCommunityUserIds);
	}

	/**
	 * 指定したコミュニティユーザーのニュースフィードを検索開始時間より前、
	 * もしくはより後から取得して返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始時間
	 * @param previous より前を取得する場合、true
	 * @return ニュースフィード
	 */
	@Override
	@ArroundSolr
	public SearchResult<NewsFeedVO> findNewsFeedByCommunityUserId(
			String communityUserId, int limit, Date offsetTime, boolean previous) {
		SearchResult<ActionHistoryDO> searchResult
				= actionHistoryDao.findNewsFeedByCommunityUserId(
						communityUserId, limit, offsetTime, previous);
		SearchResult<NewsFeedVO> result = new SearchResult<NewsFeedVO>();
		result.setHasAdult(searchResult.isHasAdult());
		result.setNumFound(searchResult.getNumFound());
		
		List<String> reviewIds = new ArrayList<String>();
		List<String> questionIds = new ArrayList<String>();
		List<String> skus = new ArrayList<String>();
		List<String> questionAnswerIds = new ArrayList<String>();
		List<String> imageSetIds = new ArrayList<String>();
		List<ReviewDO> reviewDOs = new ArrayList<ReviewDO>();
		
		for (ActionHistoryDO actionHistory : searchResult.getDocuments()) {
			if (actionHistory.getReview() != null) {
				reviewIds.add(actionHistory.getReview().getReviewId());
				reviewDOs.add(actionHistory.getReview());
			}
			if (actionHistory.getQuestion() != null) {
				questionIds.add(actionHistory.getQuestion().getQuestionId());
				if( actionHistory.getQuestion().getProduct() != null ){
					skus.add(actionHistory.getQuestion().getProduct().getSku());
				}
			}
			if (actionHistory.getQuestionAnswer() != null) {
				questionAnswerIds.add(actionHistory.getQuestionAnswer().getQuestionAnswerId());
				if(actionHistory.getQuestionAnswer().getQuestion() != null ){
					questionIds.add(actionHistory.getQuestionAnswer().getQuestion().getQuestionId());
				}
			}
			if (actionHistory.getImageSetId() != null) {
				imageSetIds.add(actionHistory.getImageSetId());
			}
		}
		
		Map<String, Long> reviewCommentCountMap = new HashMap<String, Long>();
		Map<String, Long> questionCommentCountMap = new HashMap<String, Long>();
		Map<String, Long> questionAnswerCommentCountMap = new HashMap<String, Long>();
		Map<String, Long> imageSetCommentCountMap = new HashMap<String, Long>();

		Map<String, Long> reviewLikeCountMap =  new HashMap<String, Long>();
		Map<String, Long> questionLikeCountMap =  new HashMap<String, Long>();
		Map<String, Long> questionAnswerLikeCountMap =  new HashMap<String, Long>();
		Map<String, Long> imageSetLikeCountMap =  new HashMap<String, Long>();
		
		Map<String, Long[]> reviewVotingCountMap =  new HashMap<String, Long[]>();
		Map<String, Long[]> questionVotingCountMap =  new HashMap<String, Long[]>();
		Map<String, Long[]> questionAnswerVotingCountMap =  new HashMap<String, Long[]>();
		Map<String, Long[]> imageSetVotingCountMap =  new HashMap<String, Long[]>();
		
		Map<String, List<ImageHeaderDO>> reviewAllImageMap = new HashMap<String, List<ImageHeaderDO>>();
		Map<String, List<ImageHeaderDO>> questionAllImageMap = new HashMap<String, List<ImageHeaderDO>>();
		Map<String, List<ImageHeaderDO>> questionAnswerAllImageMap = new HashMap<String, List<ImageHeaderDO>>();
		Map<String, List<ImageHeaderDO>> imageSetAllImageMap = new HashMap<String, List<ImageHeaderDO>>();
		
		// コメント件数
		commentDao.loadContentsCommentCountMap(
				reviewIds, 
				questionIds,
				questionAnswerIds, 
				imageSetIds, 
				reviewCommentCountMap,
				questionCommentCountMap, 
				questionAnswerCommentCountMap,
				imageSetCommentCountMap);
		
		Map<String, Boolean> likeReviewMap = new HashMap<String, Boolean>();
		Map<String, Boolean> likeAnswerMap = new HashMap<String, Boolean>();
		Map<String, Boolean> hasAnswerMap = new HashMap<String, Boolean>();
		Map<String, Boolean> questionFollowMap = new HashMap<String, Boolean>();
		
		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();
		
		if( StringUtils.isNotEmpty(loginCommunityUserId)){
			//いいね済みかどうか
			likeReviewMap = likeDao.loadReviewLikeMap(loginCommunityUserId, reviewIds);
			//いいね済みかどうか
			likeAnswerMap = likeDao.loadQuestionAnswerLikeMap(loginCommunityUserId, questionAnswerIds);
			//質問回答済みかどうか
			hasAnswerMap = hasQuestionAnswer(loginCommunityUserId, questionIds);
			// 質問フォロー済みかどうか
			questionFollowMap = questionFollowDao.loadQuestionFollowMap(loginCommunityUserId, questionIds);
		}
		// 同じ商品・ユーザーで、他のレビュー数
		Map<String, Long> sameProductReviewCountMap = reviewDao.loadSameProductReviewCountMap(reviewDOs);
		// いいね件数
		likeDao.loadContentsLikeCountMap(reviewIds, questionIds, questionAnswerIds, imageSetIds, null, 
				reviewLikeCountMap, questionLikeCountMap, questionAnswerLikeCountMap, imageSetLikeCountMap, null);
		
		// 参考になった数
		votingDao.loadContentsVotingCountMap(reviewIds, questionIds, questionAnswerIds, imageSetIds,null, 
				reviewVotingCountMap, questionVotingCountMap, questionAnswerVotingCountMap, imageSetVotingCountMap, null);

		imageDao.loadAllImageMapByContentsIds(
				reviewIds, 
				questionIds, 
				questionAnswerIds, 
				imageSetIds,
				reviewAllImageMap, 
				questionAllImageMap, 
				questionAnswerAllImageMap, 
				imageSetAllImageMap);
		
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(searchResult.getDocuments());
		for (ActionHistoryDO actionHistory : searchResult.getDocuments()) {
			NewsFeedVO vo = new NewsFeedVO();
			vo.setActionHistory(actionHistory);
			vo.setMyActionFlg(actionHistory.getCommunityUser().getCommunityUserId().equals(communityUserId));
			if (actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_REVIEW)
					|| actionHistory.getActionHistoryType().equals(ActionHistoryType.PRODUCT_REVIEW)) {
				Long reviewCommentCount = reviewCommentCountMap.get(actionHistory.getReview().getReviewId());
				Long reviewLikeCount = reviewLikeCountMap.get(actionHistory.getReview().getReviewId());
				Long[] reviewVotingCount = reviewVotingCountMap.get(actionHistory.getReview().getReviewId());
				
				if (reviewCommentCount != null)
					vo.setCommentCount(reviewCommentCount);
				if (reviewLikeCount != null)
					vo.setLikeCount(reviewLikeCount);
				if( reviewVotingCount != null ) {
					vo.setVotingCountYes(reviewVotingCount[0]);
					vo.setVotingCountNo(reviewVotingCount[1]);
				}
				
				if (reviewAllImageMap.containsKey(actionHistory.getReview().getReviewId()))
					vo.setImages(reviewAllImageMap.get(actionHistory.getReview().getReviewId()));
				if (likeReviewMap.containsKey(actionHistory.getReview().getReviewId()))
					vo.setLikeFlg(likeReviewMap.get(actionHistory.getReview().getReviewId()));
				if( sameProductReviewCountMap.containsKey(actionHistory.getReview().getReviewId()))
					vo.setOtherReviewCount(sameProductReviewCountMap.get(actionHistory.getReview().getReviewId()));
				
				// TODO あとで効率よく取れるように変更する。
				SearchResult<LikeDO> likes = findLikeByReviewId(
						actionHistory.getReview().getReviewId(),
						loginCommunityUserId,
						resourceConfig.evaluationAreaLikeReadLimit);
				
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
				SearchResult<CommentSetVO> resultComment = findReviewCommentByReviewId(
						actionHistory.getReview().getReviewId(),
						null,
						resourceConfig.commentInitReadLimit,
						null,
						false);
				if( !resultComment.getDocuments().isEmpty() )
					Collections.reverse(resultComment.getDocuments());
				vo.setCommentViewRemainingCount(vo.getCommentCount() - resultComment.getDocuments().size());
				vo.setComments(resultComment);
			} else if (actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_QUESTION)
					|| actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_FOLLOW_QUESTION)
					|| actionHistory.getActionHistoryType().equals(ActionHistoryType.PRODUCT_QUESTION)) {
				Long questionCommentCount = questionCommentCountMap.get(actionHistory.getQuestion().getQuestionId());
				Long questionLikeCount = questionLikeCountMap.get(actionHistory.getQuestion().getQuestionId());
				Long[] questionVotingCount = questionVotingCountMap.get(actionHistory.getQuestion().getQuestionId());
				
				if (questionCommentCount != null)
					vo.setCommentCount(questionCommentCount);
				if (questionLikeCount != null)
					vo.setLikeCount(questionLikeCount);
				if( questionVotingCount != null ) {
					vo.setVotingCountYes(questionVotingCount[0]);
					vo.setVotingCountNo(questionVotingCount[1]);
				}
				if (questionAllImageMap.containsKey(actionHistory.getQuestion().getQuestionId()))
					vo.setImages(questionAllImageMap.get(actionHistory.getQuestion().getQuestionId()));
				if( hasAnswerMap.containsKey(actionHistory.getQuestion().getQuestionId())){
					vo.setAnswerFlg(hasAnswerMap.get(actionHistory.getQuestion().getQuestionId()));
				}
			} else if (actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_ANSWER)
					|| actionHistory.getActionHistoryType().equals(ActionHistoryType.PRODUCT_ANSWER)
					|| actionHistory.getActionHistoryType().equals(ActionHistoryType.QUESTION_ANSWER)) {
				Long questionAnswerCommentCount = questionAnswerCommentCountMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId());
				Long questionAnswerLikeCount = questionAnswerLikeCountMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId());
				Long[] questionAnswerVotingCount = questionAnswerVotingCountMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId());
				
				if (questionAnswerCommentCount != null)
					vo.setCommentCount(questionAnswerCommentCount);
				if (questionAnswerLikeCount != null)
					vo.setLikeCount(questionAnswerLikeCount);
				if( questionAnswerVotingCount != null ) {
					vo.setVotingCountYes(questionAnswerVotingCount[0]);
					vo.setVotingCountNo(questionAnswerVotingCount[1]);
				}
				if( hasAnswerMap.containsKey(actionHistory.getQuestionAnswer().getQuestion().getQuestionId())){
					vo.setAnswerFlg(hasAnswerMap.get(actionHistory.getQuestionAnswer().getQuestion().getQuestionId()));
				}
				if (questionAnswerAllImageMap.containsKey(actionHistory.getQuestionAnswer().getQuestionAnswerId()))
					vo.setImages(questionAnswerAllImageMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId()));
				if (likeAnswerMap.containsKey(actionHistory.getQuestionAnswer().getQuestionAnswerId()))
					vo.setLikeFlg(likeAnswerMap.get(actionHistory.getQuestionAnswer().getQuestionAnswerId()));
				
				if (questionAllImageMap.containsKey(actionHistory.getQuestionAnswer().getQuestion().getQuestionId())) {
					vo.setSubContentImages(questionAllImageMap.get(actionHistory.getQuestionAnswer().getQuestion().getQuestionId()));
				}				
				// TODO あとで効率よく取れるように変更する。
				SearchResult<LikeDO> likes = findLikeByQuestionAnswerId(
						actionHistory.getQuestionAnswer().getQuestionAnswerId(),
						loginCommunityUserId,
						resourceConfig.commentInitReadLimit);
				long like_count = vo.getLikeCount() - (vo.isLikeFlg()? 1 : 0);
				if( vo.isLikeFlg() ){
					// getLikeCount()は、自分を含んだ数字であることが前提。
					if( likes.getDocuments().isEmpty() ){
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
						actionHistory.getQuestionAnswer().getQuestionAnswerId(),
						null,
						resourceConfig.commentInitReadLimit,
						null,
						false);
				if( !resultComment.getDocuments().isEmpty() )
					Collections.reverse(resultComment.getDocuments());
				vo.setCommentViewRemainingCount(vo.getCommentCount() - resultComment.getDocuments().size());
				vo.setComments(resultComment);
			} else if (actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_IMAGE)
					|| actionHistory.getActionHistoryType().equals(ActionHistoryType.PRODUCT_IMAGE)) {
				Long imageSetCommentCount = imageSetCommentCountMap.get(actionHistory.getImageSetId());
				Long imageSetLikeCount = imageSetLikeCountMap.get(actionHistory.getImageSetId());
				Long[] imageSetVotingCount = imageSetVotingCountMap.get(actionHistory.getImageSetId());
				
				if (imageSetCommentCount != null)
					vo.setCommentCount(imageSetCommentCount);
				if (imageSetLikeCount != null)
					vo.setLikeCount(imageSetLikeCount);
				if( imageSetVotingCount != null ) {
					vo.setVotingCountYes(imageSetVotingCount[0]);
					vo.setVotingCountNo(imageSetVotingCount[1]);
				}
				
				if (imageSetAllImageMap.containsKey(actionHistory.getImageSetId()))
					vo.setImageHeaders(imageSetAllImageMap.get(actionHistory.getImageSetId()));
					//vo.setImages(imageSetAllImageMap.get(actionHistory.getImageSetId()));
			}
			if ( actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_QUESTION)
					|| actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_FOLLOW_QUESTION)
					|| actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_ANSWER)
					|| actionHistory.getActionHistoryType().equals(ActionHistoryType.PRODUCT_ANSWER)
					|| actionHistory.getActionHistoryType().equals(ActionHistoryType.QUESTION_ANSWER)
					|| actionHistory.getActionHistoryType().equals(ActionHistoryType.PRODUCT_QUESTION)) {
				if ( questionFollowMap.containsKey(actionHistory.getQuestion().getQuestionId()))
					vo.setFollowingFlg(questionFollowMap.get(actionHistory.getQuestion().getQuestionId()));
			}
			result.updateFirstAndLast(vo);
			
			if (actionHistory.isStop(loginCommunityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				continue;
			}
			
			if( actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_REVIEW) ||
					actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_IMAGE) ||
					actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_ANSWER) ||
					actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_QUESTION) ||
					actionHistory.getActionHistoryType().equals(ActionHistoryType.PRODUCT_REVIEW) ||
					actionHistory.getActionHistoryType().equals(ActionHistoryType.PRODUCT_IMAGE) ||
					actionHistory.getActionHistoryType().equals(ActionHistoryType.PRODUCT_ANSWER) ||
					actionHistory.getActionHistoryType().equals(ActionHistoryType.PRODUCT_QUESTION) ||
					actionHistory.getActionHistoryType().equals(ActionHistoryType.QUESTION_ANSWER) ||
					actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_FOLLOW_QUESTION)){
				if( actionHistory.getProduct() != null && actionHistory.getProduct().getSku() != null ){
					// コンテンツの購入情報を取得する。
					PurchaseProductDO purchaseProductDO = orderDao.loadPurchaseProductBySku(
							actionHistory.getCommunityUser().getCommunityUserId(),
							actionHistory.getProduct().getSku(), Path.DEFAULT, false);
					if( purchaseProductDO != null && !purchaseProductDO.isDeleted() ){
						vo.setPurchaseProduct( purchaseProductDO );
					}
					// ログインユーザーの購入情報を取得する。
					if( StringUtils.isNotEmpty(loginCommunityUserId)){
						purchaseProductDO = orderDao.loadPurchaseProductBySku(
								loginCommunityUserId,
								actionHistory.getProduct().getSku(), Path.DEFAULT, false);
						if( purchaseProductDO != null && !purchaseProductDO.isDeleted() ){
							vo.setLoginUserPurchaseProduct(purchaseProductDO);
						}
					}
				}
			}
			
			result.getDocuments().add(vo);
		}
		return result;
	}

	/**
	 * 指定したニックネームのコミュニティユーザーIDを返します。
	 * @param communityName ニックネーム
	 * @return コミュニティユーザーID
	 */
	@Override
	@ArroundSolr
	@MethodCache(
			cacheStrategy=CacheStrategyType.Ajax,
			targetSystems={TargetSystemType.CommunityWeb, TargetSystemType.CommunityWs, TargetSystemType.CommunityDataSyncWeb}
			)
	public String getCommunityUserIdByCommunityName(String communityName) {
		String normalizeCommunityName = normalizeCharDao.normalizeString(communityName);
		return communityUserDao.loadCommunityUserIdByNormalizeCommunityName(normalizeCommunityName);
	}
	
	/**
	 * 指定したニックネームのコミュニティユーザーを返します。
	 * @param communityName ニックネーム
	 * @return コミュニティユーザー
	 */
	@Override
	@ArroundSolr
	public CommunityUserDO getCommunityUserByCommunityName(String communityName) {
		String communityUserId = getCommunityUserIdByCommunityName(communityName);
		if (communityUserId == null) {
			return null;
		}
		return communityUserDao.loadFromIndex(communityUserId, Path.DEFAULT);
	}

	/**
	 * 指定したニックネームのコミュニティユーザーを返します。
	 * @param communityName ニックネーム
	 * @return コミュニティユーザー
	 */
	@Override
	@ArroundSolr
	public CommunityUserSetVO getCommunityUserSetByCommunityName(String communityName) {
		String communityUserId = getCommunityUserIdByCommunityName(communityName);
		if (communityUserId == null) {
			return null;
		}
		CommunityUserDO communityUser = communityUserDao.loadFromIndex(communityUserId, Path.DEFAULT);
		CommunityUserSetVO vo = new CommunityUserSetVO();
		vo.setCommunityUser(communityUser);

		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();
		if (loginCommunityUserId != null) {
			vo.setFollowingUser(communityUserFollowDao.existsCommunityUserFollow(loginCommunityUserId, communityUserId));
		}
		vo.setPostImageCount(imageDao.countPostImageSetCount(communityUserId, null));
		vo.setPostQuestionAnswerCount(questionAnswerDao.countPostQuestionAnswerCount(communityUserId, null));
		vo.setPostQuestionCount(questionDao.countPostQuestionCount(communityUserId, null));
		vo.setPostReviewCount(reviewDao.countPostReviewCount(communityUserId, null));
		vo.setProductMasterCount(productMasterDao.countRankProductMasterByCommunityUserId(communityUserId));
		return vo;
	}

	/**
	 * 指定したニックネームのコミュニティユーザーのステータスを返します。
	 * @param communityName ニックネーム
	 * @return ステータス
	 */
	@Override
	@ArroundSolr
	public CommunityUserStatus getCommunityUserStatusByCommunityName(String communityName) {
		String communityUserId = getCommunityUserIdByCommunityName(
				communityName);
		if (communityUserId == null) {
			return null;
		}
		CommunityUserDO communityUser = communityUserDao.loadFromIndex(
				communityUserId, Path.includeProp("status"));
		if (communityUser != null) {
			return communityUser.getStatus();
		} else {
			return null;
		}
	}

	/**
	 * 指定したニックネームのコミュニティユーザーを返します。
	 * @param communityName ニックネーム
	 * @param sku sku
	 * @return コミュニティユーザー
	 */
	@Override
	@ArroundSolr
	public CommunityUserSetVO getCommunityUserByCommunityName(String communityName, String sku){
		String communityUserId = getCommunityUserIdByCommunityName(
				communityName);
		if (communityUserId == null) {
			return null;
		}
		CommunityUserDO communityUser = communityUserDao.loadFromIndex(
				communityUserId, Path.DEFAULT);
		CommunityUserSetVO vo = new CommunityUserSetVO();
		vo.setCommunityUser(communityUser);

		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();
		if (loginCommunityUserId != null) {
			vo.setFollowingUser(
					communityUserFollowDao.existsCommunityUserFollow(
							loginCommunityUserId, communityUserId));
		}
		if (!StringUtils.isEmpty(sku)) {
			PurchaseProductDO purchaseProduct = orderDao.loadPurchaseProductBySku(
					communityUserId, sku, Path.DEFAULT, false);
			// 購入履歴が存在しない場合、非公開の場合は未設定
			if (null!=purchaseProduct && purchaseProduct.isPublicSetting()) {
				vo.setPurchaseProduct(purchaseProduct);
			}
			VersionDO version = productMasterDao.loadProductMasterVersion(false);
			if (version != null) {
				vo.setProductMaster(
						productMasterDao.loadProductMaster(IdUtil.createIdByConcatIds(
								IdUtil.formatVersion(version.getVersion()),
								sku,
								communityUserId), Path.DEFAULT));
			}
			vo.setPostImageCount(
					imageDao.countPostImageSetCount(communityUserId, sku));
			vo.setPostQuestionAnswerCount(
					questionAnswerDao.countPostQuestionAnswerCount(
							communityUserId, sku));
			vo.setPostQuestionCount(
					questionDao.countPostQuestionCount(
							communityUserId, sku));
			vo.setPostReviewCount(
					reviewDao.countPostReviewCount(communityUserId, sku));
		}
		return vo;
	}

	/**
	 * 指定したIDのコミュニティユーザーを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param sku sku
	 * @return コミュニティユーザー
	 */
	@Override
	@ArroundSolr
	public CommunityUserSetVO getCommunityUserByCommunityUserId(String communityUserId, String sku){
		CommunityUserDO communityUser = communityUserDao.loadFromIndex(
				communityUserId, Path.DEFAULT);
		CommunityUserSetVO vo = new CommunityUserSetVO();
		vo.setCommunityUser(communityUser);

		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();
		if (loginCommunityUserId != null) {
			vo.setFollowingUser(
					communityUserFollowDao.existsCommunityUserFollow(
							loginCommunityUserId, communityUserId));
		}
		
		vo.setPurchaseProduct(null);
		PurchaseProductDO purchaseProduct = orderDao.loadPurchaseProductBySku(communityUserId, sku, Path.DEFAULT, false);
		if(purchaseProduct.isPublicSetting()) {
			vo.setPurchaseProduct(purchaseProduct);
		}

		VersionDO version = productMasterDao.loadProductMasterVersion(false);
		if (version != null) {
			vo.setProductMaster(
					productMasterDao.loadProductMaster(IdUtil.createIdByConcatIds(
							IdUtil.formatVersion(version.getVersion()),
							sku,
							communityUserId), Path.DEFAULT));
		}
		return vo;
	}

	/**
	 * コミュニティーユーザIDに紐づくコミュニティユーザーを返します。
	 * @param communityUserId
	 * @return
	 */
	@Override
	@ArroundSolr
	public CommunityUserDO getCommunityUser(String communityUserId){
		CommunityUserDO communityUser = communityUserDao.loadFromIndex(
				communityUserId, Path.DEFAULT);
		return communityUser;
	}

	/**
	 * ハッシュ化されたコミュニティIDに紐づくコミュニティユーザーを返します。
	 * @param hashCommunityId ハッシュ化されたコミュニティID
	 * @return コミュニティユーザー
	 */
	@Override
	@ArroundHBase
	public CommunityUserDO getCommunityUserByHashCommunityId(String hashCommunityId) {
		return getCommunityUserByHashCommunityId(hashCommunityId, Path.DEFAULT);
	}

	public CommunityUserDO getCommunityUserByHashCommunityId(String hashCommunityId, Condition condition) {
		return communityUserDao.loadByHashCommunityId(hashCommunityId,
				condition,
						false,
						serviceConfig.syncCommunityUserToBackend);
	}

	/**
	 * コミュニティユーザーを新規登録します。
	 * @param universalSessionId ユニバーサルセッションID
	 * @param communityUser コミュニティユーザー
	 * @return 登録されたコミュニティユーザー
	 */
	@Deprecated
	@Override
	@ArroundHBase
	public CommunityUserDO createCommunityUser(
			String universalSessionId,
			CommunityUserDO communityUser) {
		String normalizeCommunityName = normalizeCharDao.normalizeString(
				communityUser.getCommunityName());
		if (duplicateNormalizeCommunityName(null,
				communityUser.getCommunityName(), normalizeCommunityName, true)) {
			throw new UniqueConstraintException();
		}
		/* 20120627 コミュニティニックネーム重複対応 */
		String spoofingNamePattern = normalizeCharDao.getSpoofingPattern(normalizeCommunityName);
		if(!normalizeCharDao.validateSpoofingPattern(spoofingNamePattern, true)){
			throw new SpoofingNameException("community name is spoofing communityName:" + communityUser.getCommunityName() + " normalizeName:" + normalizeCommunityName);
		}
		SpoofingNameDO spoofingName = new SpoofingNameDO();
		spoofingName.setSpoofingNameId(StringUtil.toSHA256(spoofingNamePattern));
		spoofingName.setSpoofingPattern(spoofingNamePattern);
		spoofingName.setSpoofingName(normalizeCommunityName);

		communityUser.setNormalizeCommunityName(normalizeCommunityName);

		//コミュニティID取得（XI）
		String communityId = outerCustomerDao.createCommunityId(universalSessionId);
		communityUser.setCommunityId(communityId);

		communityUser.setHashCommunityId(
				domainConfig.createHashCommunityId(communityUser.getCommunityId()));
		communityUser.setStatus(CommunityUserStatus.ACTIVE);
		communityUser.setCommunityUserId(
				communityUserDao.issueCommunityUserId());

		String profileImageId = null;
		String thumbnailImageId = null;

		//アップロードされた場合のみ、画像登録処理を行います。
		if (communityUser.getImageHeader() != null && StringUtils.isNotEmpty(
				communityUser.getImageHeader().getImageId())) {
			ImageHeaderDO imageHeader = communityUser.getImageHeader();
			imageHeader.setOwnerCommunityUser(communityUser);
			imageHeader.setCommunityUser(communityUser);
			imageHeader.setPostContentType(PostContentType.PROFILE);

			imageDao.saveAndUploadImage(imageHeader);
			profileImageId = imageHeader.getImageId();
			communityUser.setProfileImageUrl(
					communityUser.getImageHeader().getImageUrl());

			ImageHeaderDO thumbnail = communityUser.getThumbnail();
			thumbnail.setOwnerCommunityUser(communityUser);
			thumbnail.setThumbnailUser(communityUser);
			thumbnail.setPostContentType(PostContentType.PROFILE_THUMBNAIL);
			thumbnail.setThumbnail(true);

			imageDao.saveAndUploadImage(thumbnail);
			thumbnailImageId = thumbnail.getImageId();
			communityUser.setThumbnailImageUrl(
					communityUser.getThumbnail().getImageUrl());
		}

		//コミュニティユーザーを登録します。
		communityUserDao.createCommunityUser(communityUser, spoofingName);

		try {
			universalSessionManagerDao.deleteUniversalSession(universalSessionId);
		} catch (Exception e) {
			LOG.warn("fail delete universal session. ignore exception. no problem.", e);
		}

		InformationDO information = new InformationDO();
		information.setInformationType(InformationType.WELCOME);
		information.setCommunityUser(communityUser);

		informationDao.createInformation(information);

		indexService.updateIndexForCreateCommunityUser(
				communityUser.getCommunityUserId(),
				profileImageId,
				thumbnailImageId,
				information.getInformationId());

		//友達招待のアナウンス情報を登録します。
		AnnounceDO announce = new AnnounceDO();
		announce.setCommunityUserId(communityUser.getCommunityUserId());
		announce.setType(AnnounceType.WELCOME_HINT);
		announceDao.create(announce);

		mailService.sendRegistrationCompleteMail(
				communityUser.getCommunityUserId());

		return communityUser;
	}

	/**
	 * ソーシャルネットワークのユーザーIDから該当するコミュニティユーザーを返します。
	 * @param providerId プロバイダーID
	 * @param providerUserIds ソーシャルネットワークのユーザーIDのリスト
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	@ArroundSolr
	@ArroundHBase
	public Map<String, CommunityUserDO> findCommunityUserBySocialProviderUserIds(
			String providerId, Set<String> providerUserIds) {
		Map<String, CommunityUserDO> results
				= new HashMap<String, CommunityUserDO>();

		List<SocialMediaSettingDO> settings = socialMediaSettingDao.findBySocialSettingsByProviderIdAndProviderUserIds(
				providerId, providerUserIds);

		if (settings.isEmpty()) {
			return results;
		}

		List<String> communityUserIds = new ArrayList<String>();

		for (SocialMediaSettingDO setting : settings) {
			communityUserIds.add(setting.getCommunityUserId());
		}

		List<CommunityUserDO> communityUsers = communityUserDao.find(
				communityUserIds,
				Path.includeProp("*").includePath("imageHeader.imageId").depth(1)
				);

		if (communityUsers.isEmpty()) {
			return results;
		}

		for (CommunityUserDO communityUser : communityUsers) {
			for (SocialMediaSettingDO setting : settings) {
				if (setting.getCommunityUserId().equals(communityUser.getCommunityUserId())) {
					results.put(setting.getSocialMediaAccountCode(), communityUser);
					break;
				}
			}
		}

		return results;
	}

	/**
	 * 指定したコミュニティユーザーマップを返します。
	 * @param communityUserId コミュニティユーザー
	 * @param communityUserIds コミュニティユーザーリスト
	 * @param followUserLimit フォローユーザーの最大取得件数
	 * @return コミュニティユーザーマップ
	 */
	@Override
	@ArroundSolr
	public Map<String, CommunityUserFollowVO> loadCommunityUserMap(
			String communityUserId, List<String> communityUserIds,
			int followUserLimit) {
		Map<String, CommunityUserDO> userMap
				= communityUserDao.loadCommunityUserMap(communityUserIds);
		Map<String, CommunityUserFollowVO> result
				= new HashMap<String, CommunityUserFollowVO>();

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
		Map<String, Boolean> userFollowMap
				= communityUserFollowDao.loadCommunityUserFollowMap(
						communityUserId, communityUserIds);
		for (String target : communityUserIds) {
			CommunityUserFollowVO vo = new CommunityUserFollowVO();
			vo.setCommunityUser(userMap.get(target));
			if (reviewCountMap.containsKey(
					target)) {
				vo.setPostReviewCount(
						reviewCountMap.get(target));
			}
			if (questionCountMap.containsKey(target)) {
				vo.setPostQuestionCount(
						questionCountMap.get(target));
			}
			if (questionAnswerCountMap.containsKey(target)) {
				vo.setPostAnswerCount(
						questionAnswerCountMap.get(target));
			}
			if (imageCountMap.containsKey(target)) {
				vo.setPostImageCount(
						imageCountMap.get(target));
			}
			if (productMasterCountMap.containsKey(target)) {
				vo.setProductMasterCount(
						productMasterCountMap.get(target));
			}
			SearchResult<CommunityUserFollowDO> followUsers
					= communityUserFollowDao.findFollowCommunityUser(
							target,
							followUserLimit, null, false);
			vo.setFollowUserCount(followUsers.getNumFound());
			for (CommunityUserFollowDO info : followUsers.getDocuments()) {
				vo.getLatestFollowUsers().add(info.getFollowCommunityUser());
			}
			SearchResult<CommunityUserFollowDO> followerUsers
					= communityUserFollowDao.findFollowerCommunityUser(
							target,
							followUserLimit, null, false);
			vo.setFollowerUserCount(followerUsers.getNumFound());
			for (CommunityUserFollowDO info : followerUsers.getDocuments()) {
				vo.getLatestFollowerUsers().add(info.getCommunityUser());
			}

			if (userFollowMap != null
					&& userFollowMap.size() > 0
					&& userFollowMap.containsKey(
					target)) {
				vo.setFollowingFlg(userFollowMap.get(
						target));
			}

			result.put(target, vo);
		}
		return result;
	}

	/**
	 * コミュニティユーザーを再登録します。
	 * @param communityUser コミュニティユーザー
	 * @return コミュニティユーザー
	 */
	@Override
	@ArroundHBase
	@ArroundSolr
	public CommunityUserDO reCreateCommunityUser(CommunityUserDO communityUser, String catalogAutoId, boolean agreement) {
		CommunityUserDO dbInstance = communityUserDao.loadWithLock(
				communityUser.getCommunityUserId(),
				Path.includeProp("*").includePath("*").depth(1),
				true);
		if (dbInstance == null) {
			throw new DataNotFoundException(
					"CommunityUser is not found. communityUserId = "
					+ communityUser.getCommunityUserId());
		} else if (dbInstance.getStatus() != CommunityUserStatus.INVALID) {
			throw new SecurityException(
					"CommunityUser can not recreate. communityUserId = "
					+ communityUser.getCommunityUserId() + " status = " + dbInstance.getStatus());
		}
		String normalizeCommunityName = normalizeCharDao.normalizeString(
				communityUser.getCommunityName());
		if (duplicateNormalizeCommunityName(
				null,
				communityUser.getCommunityName(), normalizeCommunityName, true)) {
			throw new UniqueConstraintException();
		} else {
			String spoofingNamePattern = normalizeCharDao.getSpoofingPattern(normalizeCommunityName);
			String spoofingNameId = StringUtil.toSHA256(spoofingNamePattern);
			
			SpoofingNameDO spoofingName = new SpoofingNameDO();
			spoofingName.setSpoofingNameId(spoofingNameId);
			spoofingName.setSpoofingPattern(spoofingNamePattern);
			spoofingName.setSpoofingName(normalizeCommunityName);
			
			dbInstance.setNormalizeCommunityName(normalizeCommunityName);
			dbInstance.setCommunityName(communityUser.getCommunityName());
			communityUserDao.updateCommunityName(dbInstance, spoofingName, false);
		}
		boolean newImageUpload = communityUser.getImageHeader() != null &&
				StringUtils.isNotEmpty(communityUser.getImageHeader().getImageId());
		boolean alreadyImageUpload = dbInstance.getImageHeader() != null &&
				StringUtils.isNotEmpty(dbInstance.getImageHeader().getImageId());

		//論理削除されたプロフィール画像が紐づいている場合、物理削除します。
		if (alreadyImageUpload) {
			imageDao.deleteBothImage(PostContentType.PROFILE,
					dbInstance.getCommunityUserId(),
					dbInstance.getImageHeader().getImageId(), false, false,ContentsStatus.DELETE);
			dbInstance.setImageHeader(null);
			dbInstance.setProfileImageUrl(null);
			imageDao.deleteBothImage(PostContentType.PROFILE_THUMBNAIL,
					dbInstance.getCommunityUserId(),
					dbInstance.getThumbnail().getImageId(), false, false,ContentsStatus.DELETE);
			dbInstance.setThumbnail(null);
			dbInstance.setThumbnailImageUrl(null);
		}

		String profileImageId = null;
		String thumbnailImageId = null;

		//新しい画像がアップロードされた場合、登録します。
		if (newImageUpload) {
			ImageHeaderDO imageHeader = communityUser.getImageHeader();
			imageHeader.setOwnerCommunityUser(communityUser);
			imageHeader.setCommunityUser(communityUser);
			imageHeader.setPostContentType(PostContentType.PROFILE);

			imageDao.saveAndUploadImage(imageHeader);
			profileImageId = imageHeader.getImageId();
			dbInstance.setImageHeader(imageHeader);
			dbInstance.setProfileImageUrl(
					dbInstance.getImageHeader().getImageUrl());

			ImageHeaderDO thumbnail = communityUser.getThumbnail();
			thumbnail.setOwnerCommunityUser(communityUser);
			thumbnail.setThumbnailUser(communityUser);
			thumbnail.setPostContentType(PostContentType.PROFILE_THUMBNAIL);
			thumbnail.setThumbnail(true);

			imageDao.saveAndUploadImage(thumbnail);
			thumbnailImageId = thumbnail.getImageId();
			dbInstance.setThumbnail(thumbnail);
			dbInstance.setThumbnailImageUrl(
					dbInstance.getThumbnail().getImageUrl());
		}

		// 保存レビュー質問回答が存在する場合、物理削除する
		reviewDao.removeTemporaryReview(communityUser.getCommunityUserId());
		questionDao.removeTemporaryQuestion(communityUser.getCommunityUserId());
		questionAnswerDao.removeTemporaryQuestionAnswer(communityUser.getCommunityUserId());
		communityUserDao.updateProfileImage(dbInstance);
		communityUserDao.updateCommunityUserStatus(
				dbInstance.getCommunityUserId(),
				dbInstance.getCommunityId(),
				CommunityUserStatus.ACTIVE,
				false, false, false,false);

		if(agreement) {
			// 参加規約のアナウンス情報を登録します。
			AnnounceDO announce = new AnnounceDO();
			announce.setCommunityUserId(dbInstance.getCommunityUserId());
			announce.setType(AnnounceType.PARTICIPATING_AGREEMENT);
			announce.setDeleteFlag(true);
			announce.setDeleteDate(timestampHolder.getTimestamp());
			announceDao.create(announce);
		}

		AnnounceDO announce = new AnnounceDO();
		announce.setCommunityUserId(communityUser.getCommunityUserId());
		announce.setType(AnnounceType.WELCOME_HINT);
		announceDao.create(announce);
		
		if(StringUtils.isNotEmpty(catalogAutoId)){
			LoginDO login = new LoginDO();
			login.setLoginId(catalogAutoId);
			login.setCommunityId(dbInstance.getCommunityId());
			login.setCommunityUserId(dbInstance.getCommunityUserId());
			login.setLastAccessDate(timestampHolder.getTimestamp());
			login.setModifyDateTime(timestampHolder.getTimestamp());
			login.setRegisterDateTime(timestampHolder.getTimestamp());
			loginDao.save(login);
		}
		
		mailSettingDao.destroyOldSettings(dbInstance.getCommunityUserId());

		List<SocialMediaSettingDO> socialMediaSettings = new ArrayList<SocialMediaSettingDO>();
		for(SocialMediaType socialMediaType : SocialMediaType.values()){
			socialMediaSettings.add(new SocialMediaSettingDO(dbInstance.getCommunityId(), socialMediaType));
		}
		socialMediaSettingDao.saveSocialMediaSettings(socialMediaSettings);

		dbInstance.setStatus(CommunityUserStatus.ACTIVE);

		dbInstance.setKeepReviewContents(true);
		dbInstance.setKeepQuestionContents(true);
		dbInstance.setKeepImageContents(true);
		dbInstance.setKeepCommentContents(true);
		dbInstance.setModifyDateTime(timestampHolder.getTimestamp());

		InformationDO information = new InformationDO();
		information.setInformationType(InformationType.WELCOME);
		information.setCommunityUser(dbInstance);

		informationDao.createInformation(information);

		indexService.updateIndexForCreateCommunityUser(
				communityUser.getCommunityUserId(),
				profileImageId,
				thumbnailImageId,
				information.getInformationId());

		mailService.sendRegistrationCompleteMail(
				dbInstance.getCommunityUserId());

		return dbInstance;
	}

	/**
	 * コミュニティユーザーの情報を更新します。
	 * @param communityUser コミュニティユーザー
	 * @return コミュニティユーザー
	 */
	@Override
	@ArroundHBase
	public CommunityUserDO updateCommunityUser(CommunityUserDO communityUser) {
		CommunityUserDO dbInstance = communityUserDao.loadWithLock(
				communityUser.getCommunityUserId(),
				Path.DEFAULT,
				true);
		if (dbInstance == null || !dbInstance.isActive()) {
			throw new DataNotFoundException(
					"CommunityUser is not found. communityUserId = "
					+ communityUser.getCommunityUserId());
		}

		dbInstance.setSecureAccess(communityUser.isSecureAccess());
		dbInstance.setAdultVerification(communityUser.getAdultVerification());
		dbInstance.setCeroVerification(communityUser.getCeroVerification());
		communityUserDao.updateSetting(dbInstance);

		String normalizeCommunityName = null;
		if (!dbInstance.getCommunityName(
				).equals(communityUser.getCommunityName())) {
			normalizeCommunityName = normalizeCharDao.normalizeString(
					communityUser.getCommunityName());
		}

		//ニックネームを更新した場合のみ、更新します。
		if (!dbInstance.getCommunityName(
				).equals(communityUser.getCommunityName())
				&& !dbInstance.getNormalizeCommunityName().equals(normalizeCommunityName)) {

			if (duplicateNormalizeCommunityName(
					communityUser.getCommunityUserId(),
					communityUser.getCommunityName(), normalizeCommunityName, true)) {
				throw new UniqueConstraintException();
			} else {
				String spoofingNamePattern = normalizeCharDao.getSpoofingPattern(normalizeCommunityName);
				String spoofingNameId = StringUtil.toSHA256(spoofingNamePattern);
				
				SpoofingNameDO spoofingName = new SpoofingNameDO();
				spoofingName.setSpoofingNameId(spoofingNameId);
				spoofingName.setSpoofingPattern(spoofingNamePattern);
				spoofingName.setSpoofingName(normalizeCommunityName);
				
				dbInstance.setNormalizeCommunityName(normalizeCommunityName);
				dbInstance.setCommunityName(communityUser.getCommunityName());
				communityUserDao.updateCommunityName(dbInstance, spoofingName, false);
			}
		}

		boolean newImageUpload = communityUser.getImageHeader() != null &&
				StringUtils.isNotEmpty(communityUser.getImageHeader().getImageId());
		boolean alreadyImageUpload = dbInstance.getImageHeader() != null &&
				StringUtils.isNotEmpty(dbInstance.getImageHeader().getImageId());

		List<String> imageIds = new ArrayList<String>();

		//プロフィール画像を更新した場合のみ、更新します。
		if (newImageUpload != alreadyImageUpload ||
				(newImageUpload &&
				!communityUser.getImageHeader().getImageId().equals(
				dbInstance.getImageHeader().getImageId()))) {
			if (alreadyImageUpload) {
				//アップロード済みの画像がある場合、削除します。
				imageDao.deleteBothImage(
						PostContentType.PROFILE,
						communityUser.getCommunityUserId(),
						dbInstance.getImageHeader().getImageId(),
						false, false,ContentsStatus.DELETE);
				imageDao.deleteBothImage(
						PostContentType.PROFILE_THUMBNAIL,
						communityUser.getCommunityUserId(),
						dbInstance.getThumbnail().getImageId(),
						false, false,ContentsStatus.DELETE);
				imageIds.add(dbInstance.getImageHeader().getImageId());
				imageIds.add(dbInstance.getThumbnail().getImageId());
			}

			//新しい画像がアップロードされた場合、登録します。
			if (newImageUpload) {
				ImageHeaderDO imageHeader = communityUser.getImageHeader();
				imageHeader.setOwnerCommunityUser(communityUser);
				imageHeader.setCommunityUser(communityUser);
				imageHeader.setPostContentType(PostContentType.PROFILE);

				imageDao.saveAndUploadImage(imageHeader);
				imageIds.add(imageHeader.getImageId());
				communityUser.setProfileImageUrl(
						communityUser.getImageHeader().getImageUrl());

				ImageHeaderDO thumbnail = communityUser.getThumbnail();
				thumbnail.setOwnerCommunityUser(communityUser);
				thumbnail.setThumbnailUser(communityUser);
				thumbnail.setPostContentType(PostContentType.PROFILE_THUMBNAIL);
				thumbnail.setThumbnail(true);

				imageDao.saveAndUploadImage(thumbnail);
				imageIds.add(thumbnail.getImageId());
				communityUser.setThumbnailImageUrl(
						communityUser.getThumbnail().getImageUrl());
			} else {
				communityUser.setImageHeader(null);
				communityUser.setProfileImageUrl(null);
				communityUser.setThumbnail(null);
				communityUser.setThumbnailImageUrl(null);
			}
			communityUserDao.updateProfileImage(communityUser);
		}

		indexService.updateIndexForUpdateCommunityUser(
				communityUser.getCommunityUserId(),
				imageIds.toArray(new String[imageIds.size()]));

		dbInstance.setImageHeader(communityUser.getImageHeader());
		dbInstance.setProfileImageUrl(communityUser.getProfileImageUrl());
		dbInstance.setThumbnail(communityUser.getThumbnail());
		dbInstance.setThumbnailImageUrl(communityUser.getThumbnailImageUrl());

		return dbInstance;
	}

	/**
	 * コミュニティ名が重複しているかチェックします。
	 * @param communityUserId コミュニティユーザーID
	 * @param commuityName コミュニティ名
	 * @return 重複する場合、true。ただし自身のニックネームの場合、false
	 */
	@Override
	@ArroundHBase
	public boolean duplicateCommunityName(
			String communityUserId, String communityName) {
		String normalizeCommunityName = normalizeCharDao.normalizeString(communityName);
		return duplicateNormalizeCommunityName(
				communityUserId, communityName, normalizeCommunityName, false);
	}

	/**
	 * コミュニティ名が重複しているかチェックします。
	 * @param icOuterCustomerId IC外部顧客ID
	 * @param commuityName コミュニティ名
	 * @return 重複する場合、true。ただし自身のニックネームの場合、false
	 */
	@Override
	@ArroundHBase
	public boolean duplicateCommunityNameForCreate(
			String icOuterCustomerId, String communityName) {
		String normalizeCommunityName = normalizeCharDao.normalizeString(communityName);
		return duplicateNormalizeCommunityNameForCreate(
				icOuterCustomerId, communityName, normalizeCommunityName, false);
	}

	/**
	 * 指定したコミュニティユーザーのメール配信設定を返します。
	 * @param communityUserId コミュニティユーザー
	 * @return メール配信設定情報
	 */
	@Override
	@ArroundHBase
	public List<MailSettingCategoryVO> findMailSettingList(String communityUserId) {
		Map<MailSettingCategory, MailSettingCategoryVO> categoryMap
				= new HashMap<MailSettingCategory, MailSettingCategoryVO>();
		List<MailSettingCategoryVO> categoryList = new ArrayList<MailSettingCategoryVO>();
		for (MailSettingCategory category : MailSettingCategory.values()) {
			MailSettingCategoryVO categoryVO = new MailSettingCategoryVO();
			categoryVO.setCategory(category);
			categoryMap.put(category, categoryVO);
			categoryList.add(categoryVO);
		}


		List<MailSettingMasterDO> masters = mailSettingDao.findMailSettingMaster();
		List<String> mailSettingIds = new ArrayList<String>();

		for (MailSettingMasterDO master : masters) {
			mailSettingIds.add(mailSettingDao.createMailSettingId(
					communityUserId, master.getMailSettingType()));
		}

		Map<MailSettingType, MailSettingDO> settingMap
				= new HashMap<MailSettingType, MailSettingDO>();

		for (MailSettingDO mailSetting : mailSettingDao.findMailSettingByIds(
				mailSettingIds)) {
			settingMap.put(mailSetting.getMailSettingType(), mailSetting);
		}

		for (MailSettingMasterDO master : masters) {
			MailSettingVO vo = new MailSettingVO();
			vo.setMailSettingMaster(master);
			MailSettingDO mailSetting = settingMap.get(master.getMailSettingType());
			if (mailSetting != null) {
				vo.setSelectedValue(mailSetting.getMailSettingValue());
			} else {
				vo.setSelectedValue(master.getDefaultValue());
			}
			MailSettingCategoryVO categoryVO = categoryMap.get(
					master.getMailSettingType().getCategory());
			categoryVO.getMailSettings().add(vo);
		}

		return categoryList;
	}

	/**
	 * メール設定を保存します。
	 * @param mailSettings メール設定
	 * @return 保存したメール設定
	 */
	@Override
	@ArroundHBase
	public List<MailSettingDO> saveMailSettings(List<MailSettingDO> mailSettings) {
		for (MailSettingDO mailSetting : mailSettings) {
			mailSettingDao.saveMailSetting(mailSetting);
		}
		return mailSettings;
	}

	/**
	 * 指定したコミュニティユーザーにはじめてのヒント画面を表示するかを返します。
	 * @param communityUserId コミュニティユーザー
	 * @return 表示する場合、true
	 */
	@Override
	@ArroundHBase
	public boolean isShowWelcomeHint(String communityUserId) {
		AnnounceDO announce = announceDao.load(communityUserId,
				AnnounceType.WELCOME_HINT);
		return announce != null;
	}

	/**
	 * 指定したコミュニティユーザーにはじめてのヒント画面を表示させないように設定します。
	 * @param communityUserId コミュニティユーザー
	 */
	@Override
	@ArroundHBase
	public void hideWelcomeHint(String communityUserId) {
		announceDao.delete(communityUserId, AnnounceType.WELCOME_HINT);
	}

	/**
	 * 指定したコミュニティユーザーのSNS連携情報を返します。
	 * @param コミュニティID
	 * @return SNS連携情報
	 */
	@Override
	@ArroundHBase
	public List<SocialMediaSettingDO> findSocialMediaSettingList(String communityUserId) {
		return socialMediaSettingDao.findBySocialMediaSettingByCommunityUserId(communityUserId);
	}

	/**
	 * 指定したコミュニティユーザーのSNS連携情報を保存します。
	 * @param socialMediaSettings メール設定
	 * @return 保存したメール設定
	 */
	@Override
	@ArroundHBase
	public List<SocialMediaSettingDO> saveSocialMediaSetting(List<SocialMediaSettingDO> socialMediaSettings) {
		socialMediaSettingDao.saveSocialMediaSettings(socialMediaSettings);
		return socialMediaSettings;
	}

	/**
	 * 指定したコミュニティID（外部顧客ID）のユーザーのステータスを退会状態に
	 * 同期し、関連データも退会状態に更新します。
	 *
	 * @param communityId コミュニティID（外部顧客ID）
	 */
	@Override
	@ArroundSolr
	@ArroundHBase
	public void syncCommunityUserStatusForWithdraw(String communityId) {
		String hashCommunityId = domainConfig.createHashCommunityId(communityId);
		CommunityUserDO dbInstance = communityUserDao.loadByHashCommunityId(
				hashCommunityId,
				Path.includeProp(
						"communityUserId,communityId,status"),
						true,
						false);
		if (dbInstance == null) {
			throw new DataNotFoundException(
					"CommunityUser is not found. communityId = "
					+ communityId);
		}
		if (!dbInstance.isActive()) {
			LOG.warn("CommunityUserStatus is invalid. communityId = "
					+ communityId + ", status = " + dbInstance.getStatus());
			return;
		}

		// 一時退会の場合、コンテンツは削除する
		boolean delete = false;
		if(dbInstance.getStatus().equals(CommunityUserStatus.STOP)){
			delete = true;
		}

		String withdrawKey = communityUserDao.withdraw(
				dbInstance.getCommunityUserId(),
				dbInstance.getCommunityId(),
				false, delete, delete, delete, delete);
		communityUserDao.deleteCommunityUserDataForWithdrawWithIndex(
				dbInstance.getCommunityUserId(),
				withdrawKey,
				false,
				true,
				delete,
				delete,
				delete,
				delete);
		if (LOG.isInfoEnabled()) {
			LOG.info("end syncCommunityUserStatusForWithdraw. communityId=" + communityId);
		}
	}

	/**
	 * 停止状態を更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param stop 停止する場合、true
	 */
	@Override
	@ArroundHBase
	public void updateStop(
			String communityUserId,
			boolean stop) {
		CommunityUserDO dbInstance = communityUserDao.loadWithLock(
				communityUserId,
				Path.includePath(
				"communityUserId,communityId,status"),
				true);
		if (dbInstance == null) {
			throw new DataNotFoundException(
					"CommunityUser is not found. communityUserId = "
					+ communityUserId);
		}
		if (stop && !dbInstance.getStatus().equals(CommunityUserStatus.ACTIVE)) {
			throw new IllegalStateException(
					"CommunityUser is not active. requestStop = true, status = "
					+ dbInstance.getStatus()
					+ ", communityUserId = "
					+ communityUserId);
		} else if (!stop && !dbInstance.getStatus().equals(CommunityUserStatus.STOP)) {
			throw new IllegalStateException(
					"CommunityUser is not stop. requestStop = false, status = "
					+ dbInstance.getStatus()
					+ ", communityUserId = "
					+ communityUserId);
		}
		communityUserDao.updateStop(
				communityUserId,
				dbInstance.getCommunityId(),
				stop);
		String informationId = null;
		if (stop) {
			InformationDO information = new InformationDO();
			information.setInformationType(InformationType.ACCOUNT_STOP);
			information.setCommunityUser(dbInstance);
			informationDao.createInformation(information);
			informationId = information.getInformationId();
		}else{
			List<InformationDO> informations = informationDao.findInformationByType(communityUserId, InformationType.ACCOUNT_STOP);
			if(informations != null && !informations.isEmpty())
				informationId  =informations.get(0).getInformationId();
			informationDao.deleteInformation(informationId);
		}
		indexService.updateIndexForCreateCommunityUser(
				communityUserId, null, null, informationId);
		if (stop) {
			mailService.sendStopNotifyMail(communityUserId);
		}
	}

	/**
	 * 退会処理を行います。
	 * @param communityUserId コミュニティユーザーID
	 * @param force 強制退会フラグ
	 * @param reviewDelete 自身のレビュー＋自身のレビューに対するコメントを削除する場合、true
	 * @param qaDelete 自身の質問＋自身の回答＋自身の回答に関わるコメントを削除する場合、true
	 * @param imageDelete 自身の投稿画像＋自身の投稿画像に関わるコメントを削除する場合、true
	 * @param commentDelete 自身が投稿した全てのコメントを削除する場合、true
	 */
	@Override
	@ArroundHBase
	public void withdraw(
			String communityUserId,
			boolean force,
			boolean reviewDelete,
			boolean qaDelete,
			boolean imageDelete,
			boolean commentDelete,
			boolean mngToolOperation) {

		CommunityUserDO requestCommunityUser = requestScopeDao.loadCommunityUser();

		CommunityUserDO dbInstance = communityUserDao.loadWithLock(
				communityUserId,
				Path.includeProp(
				"communityUserId,communityId,status"), true);
		if (dbInstance == null) {
			throw new DataNotFoundException(
					"CommunityUser is not found. communityUserId = "
					+ communityUserId);
		}
		if (!dbInstance.isActive()) {
			throw new DataNotFoundException(
					"CommunityUserStatus is invalid. communityUserId = "
					+ communityUserId + ", status = " + dbInstance.getStatus());
		}
		if(!mngToolOperation) {
			if(requestCommunityUser != null &&
					!requestCommunityUser.getStatus().equals(dbInstance.getStatus())){
				throw new UnMatchStatusException(
						"withdraw user status is unmatch "
								+"communityUserId:"+ communityUserId
								+" sessionStatus:"+ requestCommunityUser.getStatus().getCode()
								+ " hbaseStatus:" + dbInstance.getStatus().getCode());
			}
		}

		// 一時退会の場合、コンテンツは全て削除
		if (force || dbInstance.getStatus().equals(CommunityUserStatus.STOP)) {
			reviewDelete = true;
			qaDelete = true;
			imageDelete = true;
			commentDelete = true;
		}

		String withdrawKey = communityUserDao.withdraw(
				communityUserId, dbInstance.getCommunityId(),
				force, reviewDelete, qaDelete, imageDelete, commentDelete );
//		(!force && (!reviewDelete || !qaDelete || !imageDelete || !commentDelete)));

		myself.deleteCommunityUserDataForWithdraw(
				communityUserId,
				withdrawKey,
				force,
				reviewDelete,
				qaDelete,
				imageDelete,
				commentDelete);
	}

	/**
	 * 退会に伴い、コミュニティユーザーのデータを削除します。
	 * @param communityUserId コミュニティユーザーID
	 * @param withdrawKey 退会キー
	 * @param force 強制退会フラグ
	 * @param reviewDelete 自身のレビュー＋自身のレビューに対するコメントを削除する場合、true
	 * @param qaDelete 自身の質問＋自身の回答＋自身の回答に関わるコメントを削除する場合、true
	 * @param imageDelete 自身の投稿画像＋自身の投稿画像に関わるコメントを削除する場合、true
	 * @param commentDelete 自身が投稿した全てのコメントを削除する場合、true
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.DELAYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void deleteCommunityUserDataForWithdraw(
			String communityUserId,
			String withdrawKey,
			Boolean force,
			Boolean reviewDelete,
			Boolean qaDelete,
			Boolean imageDelete,
			Boolean commentDelete) {
		CommunityUserDO dbInstance = communityUserDao.loadWithLock(
				communityUserId,
				Path.includePath(
				"communityUserId,communityId,status,withdrawKey,withdrawLock"),
				true);
		if (dbInstance == null) {
			throw new DataNotFoundException(
					"CommunityUser is not found. communityUserId = "
					+ communityUserId);
		}
		if (dbInstance.isActive()) {
			throw new IllegalStateException(
					"CommunityUser is not withdraw. status = "
					+ dbInstance.getStatus()
					+ ", communityUserId = "
					+ communityUserId);
		}
		if (!dbInstance.getWithdrawKey().equals(withdrawKey)) {
			throw new IllegalStateException(
					"WithdrawKey not match. input = "
					+ withdrawKey
					+ ", valid = "
					+ dbInstance.getWithdrawKey()
					+ ", communityUserId = "
					+ communityUserId);
		}
		if (!dbInstance.isWithdrawLock()) {
			throw new IllegalStateException(
					"No withdrawLock. communityUserId = "
					+ communityUserId);
		}

		// 一時退会の場合、コンテンツは全て削除
		if (dbInstance.getStatus().equals(CommunityUserStatus.STOP)) {
			reviewDelete = true;
			qaDelete = true;
			imageDelete = true;
			commentDelete = true;
		}

		communityUserDao.deleteCommunityUserDataForWithdrawWithIndex(
				communityUserId,
				withdrawKey,
				force,
				false,
				reviewDelete,
				qaDelete,
				imageDelete,
				commentDelete);
	}

	/**
	 * アダルト表示確認ステータスを更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param verification アダルト表示確認ステータス
	 */
	@Override
	@ArroundHBase
	public void updateAdultVerification(String communityUserId, Verification verification) {
		CommunityUserDO communityUser = communityUserDao.loadWithLock(
				communityUserId,
				Path.DEFAULT,
				false);
		communityUser.setAdultVerification(verification);
		communityUserDao.updateAdultVerification(communityUser);
		indexService.updateIndexForUpdateCommunityUser(communityUserId);
	}

	/**
	 * CERO商品表示確認ステータスを更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param verification CERO商品表示確認ステータス
	 */
	@Override
	@ArroundHBase
	public void updateCeroVerification(String communityUserId, Verification verification) {
		CommunityUserDO communityUser = communityUserDao.loadWithLock(
				communityUserId,
				Path.DEFAULT,
				false);
		communityUser.setCeroVerification(verification);
		communityUserDao.updateCeroVerification(communityUser);
		indexService.updateIndexForUpdateCommunityUser(communityUserId);
	}

	/**
	 * 退会キャンセル処理を行います。
	 * @param communityUserId コミュニティユーザーID
	 * @return 正常に終了した場合、true
	 */
	@Override
	@ArroundHBase
	public boolean cancelWithdraw(
			String communityUserId) {
		CommunityUserDO dbInstance = communityUserDao.loadWithLock(
				communityUserId,
				Path.includePath(
				"communityUserId,communityId,status,withdrawKey,withdrawLock"),
				true);
		if (dbInstance == null) {
			throw new DataNotFoundException(
					"CommunityUser is not found. communityUserId = "
					+ communityUserId);
		}
		if (dbInstance.getStatus().equals(CommunityUserStatus.INVALID)) {
			throw new IllegalStateException(
					"CommunityUser is not force withdraw. status = "
					+ dbInstance.getStatus()
					+ ", communityUserId = "
					+ communityUserId);
		}
		if (dbInstance.isActive()) {
			return false;
		}
		if (dbInstance.isWithdrawLock()) {
			return false;
		}

		HashMap<Class<?>, List<String>> updateKeyMap =
				communityUserDao.cancelWithdraw(
				communityUserId,
				dbInstance.getCommunityId(),
				dbInstance.getWithdrawKey());

		indexService.syncIndexForCommunityUser(communityUserId, updateKeyMap, true);
		return true;
	}

	/**
	 * 標準化されたコミュニティ名が重複しているかチェックします。
	 * @param communityUserId コミュニティユーザーID
	 * @param commuityName コミュニティ名
	 * @param normalizeCommunityName 標準化されたコミュニティ名
	 * @param withLock ロックを取得するかどうか
	 * @return 重複する場合、true。ただし自身のニックネームの場合、false
	 */
	private boolean duplicateNormalizeCommunityName(
			String communityUserId,
			String communityName,
			String normalizeCommunityName,
			boolean withLock) {
		return communityUserDao.existsNormalizeCommunityName(
				communityUserId, communityName, normalizeCommunityName, withLock);
	}

	/**
	 * 標準化されたコミュニティ名が重複しているかチェックします。
	 * @param icOuterCustomerId IC外部顧客ID
	 * @param commuityName コミュニティ名
	 * @param normalizeCommunityName 標準化されたコミュニティ名
	 * @param withLock ロックを取得するかどうか
	 * @return 重複する場合、true。ただし自身のニックネームの場合、false
	 */
	private boolean duplicateNormalizeCommunityNameForCreate(
			String icOuterCustomerId,
			String communityName,
			String normalizeCommunityName,
			boolean withLock) {
		return communityUserDao.existsNormalizeCommunityNameForCreate(
				icOuterCustomerId, communityName, normalizeCommunityName, withLock);
	}

	@Override
	public CommunityUserStatus loadCommunityUserStatusByCommunityUserId(
			String communityUserId) {
		CommunityUserDO communityUser = communityUserDao.load(
				communityUserId, Path.includeProp("status"));
		if (communityUser != null) {
			return communityUser.getStatus();
		} else {
			return null;
		}
	}

	@Override
	public boolean validateUserStatusForPostContents(String communityUserId) {
		// 投稿前にユーザーのステータスをチェック
		CommunityUserDO communityUserStatus = communityUserDao.load(communityUserId, Path.includeProp("status"));
		if (communityUserStatus != null
			&& (communityUserStatus.getStatus().equals(CommunityUserStatus.ACTIVE)
				|| communityUserStatus.getStatus().equals(CommunityUserStatus.STOP))) {
			return true;
		}
		return false;
	}

	/**
	 * 指定したニックネームのコミュニティユーザーIDを返します。
	 * @param communityName ニックネーム
	 * @return コミュニティユーザーID
	 */
	@Override
	@ArroundSolr
	public CommunityUserDO getCommunityUserByCommunityNameforMail(String communityName) {
		String normalizeCommunityName = normalizeCharDao.normalizeString(communityName);
		return communityUserDao.loadCommunityUserByNormalizeCommunityName(normalizeCommunityName);
	}

	@Override
	@MethodCache(
			cacheStrategy=CacheStrategyType.Ajax,
			targetSystems={TargetSystemType.CommunityWeb, TargetSystemType.CommunityWs, TargetSystemType.CommunityDataSyncWeb}
			)
	public boolean catalogValidateAuthSession(String catalogAutoId) {
		return authenticationDao.isValidateAuthSession(catalogAutoId);
	}
	
	
	@Override
	@MethodCache(
			cacheStrategy=CacheStrategyType.Ajax,
			targetSystems={TargetSystemType.CommunityWeb, TargetSystemType.CommunityWs, TargetSystemType.CommunityDataSyncWeb}
			)
	public ValidateAuthSessionDO catalogValidateAuthSession(String catalogAutoId, Map<String, String> params){
		if( params == null )
			params = Maps.newHashMap();
		return authenticationDao.validateAuthSessionV2(catalogAutoId, params);
	}

	/**
	 * ECから呼ばれるコミュニティ認証情報の登録
	 */
	@Override
	@ArroundHBase
	public void authenticate(String catalogAutoId, String communityId) {
		// AutoIDの存在チェック
		LoginDO login = loginDao.loadByAutoId(catalogAutoId);
		if(login != null && StringUtils.isNotEmpty(login.getCommunityId())){
			login.setLastAccessDate(timestampHolder.getTimestamp());
			loginDao.save(login);
		}else {
			LoginDO saveLogin = new LoginDO();
			saveLogin.setLoginId(catalogAutoId);
			saveLogin.setCommunityId(communityId);
			//ユーザーの取得処理
			CommunityUserDO community = communityUserDao.loadByCommunityId(communityId, Path.includeProp("communityUserId"));
			if(community != null) {
				saveLogin.setCommunityUserId(community.getCommunityUserId());
			}
			saveLogin.setLastAccessDate(timestampHolder.getTimestamp());
			saveLogin.setRegisterDateTime(timestampHolder.getTimestamp());
			saveLogin.setModifyDateTime(timestampHolder.getTimestamp());
			loginDao.save(saveLogin);
		}
	}
	
	/**
	 * 外部システムから呼ばれるコミュニティ認証情報更新
	 */
	@Override
	@ArroundHBase
	public void modifyAuthenticate(String oldAutoId, String newAutoId) {
		LoginDO loginDo = loginDao.loadByAutoId(oldAutoId);
		
		if( loginDo == null || loginDo.getCommunityId() == null ) {
			LOG.warn("Get LoginDO is Failure. oldAutoId:" + oldAutoId);
			return;
		}
		
		LoginDO saveLogin = new LoginDO();
		saveLogin.setLoginId(newAutoId);
		saveLogin.setCommunityId(loginDo.getCommunityId());
		saveLogin.setCommunityUserId(loginDo.getCommunityUserId());
		saveLogin.setLastAccessDate(timestampHolder.getTimestamp());
		saveLogin.setRegisterDateTime(timestampHolder.getTimestamp());
		saveLogin.setModifyDateTime(timestampHolder.getTimestamp());
		// 新しい認証セッションIDで更新する。
		loginDao.save(saveLogin);
	}

	@Override
	@MethodCache(
			cacheStrategy=CacheStrategyType.Ajax,
			targetSystems={TargetSystemType.CommunityWeb, TargetSystemType.CommunityWs, TargetSystemType.CommunityDataSyncWeb}
			)
	public String loadCommunityUserIdByAutoId(String catalogAutoId) {
		LoginDO login = loginDao.loadByAutoId(catalogAutoId);
		
		if( login == null )
			return null;
		
		if( System.currentTimeMillis() < (login.getLastAccessDate().getTime() + resourceConfig.loginExpire) )
			return login.getCommunityId();
		
		return null;
	}

	@Override
	@ArroundHBase
	public void updateLastAccessTime(String autoId) {
		LoginDO login = loginDao.loadByAutoId(autoId);
		if(login == null) {
			throw new SecurityException("Login data is null AutoId:" + autoId);
		}
		login.setLastAccessDate(timestampHolder.getTimestamp());
		loginDao.save(login);
	}
	
	@Override
	@ArroundHBase
	public void removeLogin(String autoId) {
		loginDao.removeLogin(autoId);
	}
}
