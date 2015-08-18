package com.kickmogu.yodobashi.community.service.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.ImageDao;
import com.kickmogu.yodobashi.community.resource.dao.InformationDao;
import com.kickmogu.yodobashi.community.resource.dao.OrderDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductMasterDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionAnswerDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.ReviewDao;
import com.kickmogu.yodobashi.community.resource.dao.SocialMediaSettingDao;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.SocialMediaSettingDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityOperationStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.SocialMediaType;
import com.kickmogu.yodobashi.community.service.IndexService;
import com.kickmogu.yodobashi.community.service.MyPageService;
import com.kickmogu.yodobashi.community.service.SystemMaintenanceService;
import com.kickmogu.yodobashi.community.service.vo.MyPageInfoAreaVO;

/**
 * マイページサービスの実装です。
 * @author kamiike
 */
@Service
public class MyPageServiceImpl extends UserPageCommonServiceImpl implements MyPageService {

	/**
	 * コミュニティユーザー DAO です。
	 */
	@Autowired
	private CommunityUserDao communityUserDao;

	/**
	 * お知らせ情報 DAO です。
	 */
	@Autowired
	private InformationDao informationDao;

	/**
	 * 画像 DAO です。
	 */
	@Autowired
	private ImageDao imageDao;

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
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	private RequestScopeDao requestScopeDao;

	/**
	 * レビュー DAO です。
	 */
	@Autowired
	private ReviewDao reviewDao;

	/**
	 * ソーシャルメディア連携設定 DAO です。
	 */
	@Autowired
	private SocialMediaSettingDao socialMediaSettingDao;

	/**
	 * インデックスサービスです。
	 */
	@Autowired
	private IndexService indexService;

	@Autowired
	private SystemMaintenanceService systemMaintenanceService;


	/**
	 * 指定したユーザーのマイページ向けメニュー情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param purchaseProductLimit 購入商品の最大取得件数
	 * @param productMasterLimit 商品マスターの最大取得件数
	 * @param informationNoReadLimit 未読お知らせの最大取得件数
	 * @param isAdmin 管理者ログインかどうか
	 * @return 共通情報エリア情報
	 */
	@Override
	public MyPageInfoAreaVO getMyPageInfoAreaByCommunityUserId(
			String communityUserId,
			int purchaseProductLimit,
			int productMasterLimit,
			int informationNoReadLimit,
			boolean isAdmin) {
		MyPageInfoAreaVO myPageInfoArea = new MyPageInfoAreaVO();

		for (SocialMediaSettingDO setting : socialMediaSettingDao.findBySocialMediaSettingByCommunityUserId(communityUserId)) {
			if (setting.getSocialMediaType().equals(SocialMediaType.TWITTER) && setting.isLinkFlag()) {
				myPageInfoArea.setLinkTwitter(true);
			} else if (setting.getSocialMediaType().equals(SocialMediaType.FACEBOOK) && setting.isLinkFlag()) {
				myPageInfoArea.setLinkFacebook(true);
			}
		}

		//お知らせの数です。
		myPageInfoArea.setInformationCount(informationDao.count(communityUserId));
		//投稿レビュー数です。
		myPageInfoArea.setPostReviewCount(reviewDao.countReviewByCommunityUserIdForMypage(communityUserId));
		//一時保存レビュー数です。
		myPageInfoArea.setTemporaryReviewCount(reviewDao.countReviewByCommunityUserId(communityUserId, ContentsStatus.SAVE));
		//投稿質問数です。
		myPageInfoArea.setPostQuestionCount(questionDao.countQuestionByCommunityUserIdForMypage(communityUserId));
		//一時保存質問数です。
		myPageInfoArea.setTemporaryQuestionCount(
				questionDao.countQuestionByCommunityUserId(
						communityUserId,
						ContentsStatus.SAVE));

		//投稿質問回答数です。
		myPageInfoArea.setPostQuestionAnswerCount(
				questionAnswerDao.countPostQuestionAnswerCountForMypage(
						communityUserId,
						requestScopeDao.loadAdultVerification()));


		//一時保存質問回答数です。
		myPageInfoArea.setTemporaryQuestionAnswerCount(
				questionAnswerDao.countPostQuestionAnswerCount(
						communityUserId, ContentsStatus.SAVE,
						requestScopeDao.loadAdultVerification()));

		//投稿画像数です。
		myPageInfoArea.setPostImageCount(imageDao.countImageSetByCommunityUserIdForMypage(communityUserId));
		
		myPageInfoArea.setPostAllCount(myPageInfoArea.getPostReviewCount() + myPageInfoArea.getPostQuestionCount() + myPageInfoArea.getPostQuestionAnswerCount() + myPageInfoArea.getPostImageCount());
		
		// 購入商品の取得
		SearchResult<PurchaseProductDO> purchaseProducts = 
				findPurchaseProductByCommunityUserId(communityUserId, false, purchaseProductLimit);

		//購入商品数です。
		myPageInfoArea.setPurchaseProductCount(purchaseProducts.getNumFound());
		//購入商品リストです。
		myPageInfoArea.setPurchaseProducts(purchaseProducts.getDocuments());
		if (purchaseProducts.isHasAdult()) {
			myPageInfoArea.setHasAdult(true);
		}

		SearchResult<ProductMasterDO> productMasters
				= productMasterDao.findRankProductMasterByCommunityUserId(
				communityUserId, productMasterLimit,
				null, null, false, true,
				requestScopeDao.loadAdultVerification());
		//商品マスター数です。
		myPageInfoArea.setProductMasterCount(productMasters.getNumFound());
		//商品マスターのリストです。
		myPageInfoArea.setProductMasters(productMasters.getDocuments());
		if (productMasters.isHasAdult()) {
			myPageInfoArea.setHasAdult(true);
		}
		
		// 未読お知らせ
		SearchResult<InformationDO> noReadInformationResult = findNoReadInformationByCommunityUserId(
				communityUserId,
				informationNoReadLimit,
				0);
		if (noReadInformationResult.getNumFound() > 0) {
			// 未読を既読にする
			if( !CommunityOperationStatus.READONLY_OPERATION.equals(systemMaintenanceService.getCommunityOperationStatusWithCache())){
				if( !isAdmin ){
					List<InformationDO> noReadInformations = Lists.newArrayList();
					for( InformationDO informationDO : noReadInformationResult.getDocuments()){
						if( !informationDO.isReadFlag()){
							noReadInformations.add(informationDO);
						}
					}
					if( !noReadInformations.isEmpty() ){
						updateAllRead(noReadInformations);
					}
				}
			}
		}
		//未読のお知らせカウントです。
		myPageInfoArea.setNoReadInformationCount(noReadInformationResult.getNumFound());
		// 未読のお知らせのリストです。
		myPageInfoArea.setNoReadInformations(noReadInformationResult.getDocuments());

		return myPageInfoArea;
	}

	/**
	 * 指定したコミュニティユーザーに対する未読お知らせ情報を検索して、
	 * 登録日時順（降順）に返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 検索結果
	 */
	@Override
	@ArroundSolr
	public SearchResult<InformationDO> findNoReadInformationByCommunityUserId(
			String communityUserId,
			int limit,
			int offset) {

		//hasAdult対応対象です。

		return filterInformationByStopUsers(informationDao.findNoReadInformationByCommunityUserId(
				communityUserId, limit, offset));
	}

	/**
	 * 指定したコミュニティユーザーに対するお知らせ情報を検索して、
	 * 返します。
	 * @param communityUserId コミュニティユーザー
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始日時
	 * @param previous より前を取得する場合、true
	 * @return 検索結果
	 */
	@Override
	@ArroundSolr
	public SearchResult<InformationDO> findInformationByCommunityUserId(
			String communityUserId,
			int limit,
			Date offsetTime,
			boolean previous) {
		return findInformationByCommunityUserId(communityUserId, limit, offsetTime, previous, false);
	}

	@Override
	@ArroundSolr
	public SearchResult<InformationDO> findInformationByCommunityUserId(
			String communityUserId,
			int limit,
			Date offsetTime,
			boolean previous,
			boolean excludeProduct) {
		return filterInformationByStopUsers(informationDao.findByCommunityUserId(
				communityUserId, limit, offsetTime, previous, excludeProduct));
	}
	
	/**
	 * 指定したお知らせ情報を既読に更新します。
	 */
	@Override
	@ArroundHBase
	public void updateAllRead(List<InformationDO> informations) {
		informationDao.updateInformationForRead(informations);
		List<String> informationIds = Lists.newArrayList();
		for(InformationDO informationDO : informations){
			informationIds.add(informationDO.getInformationId());
		}
		if( !informationIds.isEmpty()) {
			indexService.updateIndexForUpdateInformation(informationIds);
		}
	}

	/**
	 * 指定したコミュニティユーザーに対するお知らせ情報で未読のカウントを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @return 未読カウント
	 */
	@Override
	@ArroundSolr
	public long countNoReadInformation(String communityUserId) {
		return informationDao.countNoRead(communityUserId);
	}

	/**
	 * 一時停止ユーザーをフィルタリングします。
	 * @param searchResult 検索結果
	 * @return フィルタリングした結果
	 */
	private SearchResult<InformationDO> filterInformationByStopUsers(
			SearchResult<InformationDO> searchResult) {
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(searchResult.getDocuments());
		String communityUserId = requestScopeDao.loadCommunityUserId();
		for (Iterator<InformationDO> it = searchResult.getDocuments().iterator(); it.hasNext(); ) {
			InformationDO information = it.next();
			searchResult.updateFirstAndLast(information);
			if (information.isStop(communityUserId, stopCommunityUserIds)) {
				it.remove();
				searchResult.countUpStopContents();
			}
		}
		return searchResult;
	}
}
