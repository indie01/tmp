package com.kickmogu.yodobashi.community.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.yodobashi.community.common.exception.UnActiveException;
import com.kickmogu.yodobashi.community.common.exception.UnMatchTargetException;
import com.kickmogu.yodobashi.community.resource.dao.ActionHistoryDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.ImageDao;
import com.kickmogu.yodobashi.community.resource.dao.InformationDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionAnswerDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.ReviewDao;
import com.kickmogu.yodobashi.community.resource.dao.VotingDao;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.VotingDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;
import com.kickmogu.yodobashi.community.resource.domain.constants.VotingTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.VotingType;
import com.kickmogu.yodobashi.community.service.IndexService;
import com.kickmogu.yodobashi.community.service.UserService;
import com.kickmogu.yodobashi.community.service.VotingService;
import com.kickmogu.yodobashi.community.service.vo.CommunityUserSetVO;

@Service
public class VotingSeriveImpl implements VotingService {

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
	 * お知らせ情報 DAO です。
	 */
	@Autowired
	private InformationDao informationDao;

	/**
	 * コミュニティユーザーフォロー DAO です。
	 */
	@Autowired
	private CommunityUserFollowDao communityUserFollowDao;

	/**
	 * いいね DAO です。
	 */
	@Autowired
	private VotingDao votingDao;

	/**
	 * イメージDAO です。
	 */
	@Autowired
	private ImageDao imageDao;

	/**
	 * イメージDAO です。
	 */
	@Autowired
	private QuestionAnswerDao questionAnswerDao;

	/**
	 * イメージDAO です。
	 */
	@Autowired
	private QuestionDao questionDao;

	/**
	 * イメージDAO です。
	 */
	@Autowired
	private ReviewDao reviewDao;
	
	/**
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	private RequestScopeDao requestScopeDao;

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
	
	
	
	@Override
	public Map<String, Long[]> loadReviewVotingCountMap(List<String> reviewIds) {
		return votingDao.loadReviewVotingCountMap(reviewIds);
	}
	
	@Override
	public Map<String, Long[]> loadContentsVotingCountMap(VotingTargetType targetType, List<String> contentIds) {
		return votingDao.loadContentsVotingCountMap(targetType, contentIds, null);
	}

	/**
	 * レビューに対しての参考になった情報を更新します。
	 * @param communityUserId コミュニティユーザーID
	 * @param reviewId レビューID
	 * @param type 参考になったかのフラグ
	 * @param release 削除するかどうか
	 * @return 成功可否
	 */
	@Override
	@ArroundHBase
	public boolean updateVotingReview(
			String communityUserId, 
			String reviewId,
			VotingType type) {
		return updateVoting(
				communityUserId,
				reviewId,
				type,
				VotingTargetType.REVIEW);
	}

	@Override
	@ArroundHBase
	public boolean updateVotingQuestionAnswer(
			String communityUserId,
			String questionAnswerId,
			VotingType type) {
		return updateVoting(
				communityUserId,
				questionAnswerId,
				type,
				VotingTargetType.QUESTION_ANSWER);
	}

	@Override
	@ArroundHBase
	public boolean updateVotingImage(
			String communityUserId,
			String imageId,
			VotingType type) {
		return updateVoting(
				communityUserId,
				imageId,
				type,
				VotingTargetType.IMAGE);
	}
	
	@Override
	public SearchResult<VotingDO> findVotingByImageId(
			String imageId,
			String excludeCommunityUserId,
			int limit) {
		return filterStopUsers(votingDao.findVotingByContentsId(
				VotingTargetType.IMAGE,
				excludeCommunityUserId,
				imageId,
				limit));
	}

	@Override
	public SearchResult<VotingDO> findVotingByReviewId(
			String reviewId,
			String excludeCommunityUserId,
			int limit) {
		return filterStopUsers(votingDao.findVotingByContentsId(
				VotingTargetType.REVIEW,
				excludeCommunityUserId,
				reviewId,
				limit));
	}

	@Override
	public SearchResult<VotingDO> findVotingByQuestionAnswerId(
			String questionAnswerId,
			String excludeCommunityUserId,
			int limit) {
		return filterStopUsers(votingDao.findVotingByContentsId(
				VotingTargetType.QUESTION_ANSWER,
				excludeCommunityUserId,
				questionAnswerId,
				limit));
	}

	@Override
	public SearchResult<CommunityUserSetVO> findVotingCommunityUserByImageId(
			String imageId,
			int limit) {
		return createCommunityUserSets(
				VotingTargetType.IMAGE,
				imageId,
				limit);
	}

	@Override
	public SearchResult<CommunityUserSetVO> findVotingCommunityUserByReviewId(
			String reviewId,
			int limit) {
		return createCommunityUserSets(
				VotingTargetType.REVIEW,
				reviewId,
				limit);
	}

	@Override
	public SearchResult<CommunityUserSetVO> findVotingCommunityUserByQuestionAnswerId(
			String questionAnswerId,
			int limit) {
		return createCommunityUserSets(
				VotingTargetType.QUESTION_ANSWER,
				questionAnswerId,
				limit);
	}
	
	/**
	 * 参考になった情報登録・更新処理　
	 * @param communityUserId コミュニティユーザーID
	 * @param contentsId　コンテンツID
	 * @param type 参考になった はい、いいえ
	 * @param votingTargetType コンテンツタイプ
	 * @return 処理結果（true:正常、false:異常）
	 */
	@Override
	@ArroundHBase
	public boolean updateVoting(
			String communityUserId, 
			String contentsId,
			VotingType votingType,
			VotingTargetType votingTargetType) {
		String informationId = null;
		String actionHistoryId = null;
		
		if( StringUtils.isEmpty(communityUserId) || StringUtils.isEmpty(contentsId) || votingType == null || votingTargetType == null )
			return false;
		
		// コンテンツ投稿可能チェック
		if(!userService.validateUserStatusForPostContents(communityUserId))
			throw new UnActiveException("can not post contens because user status is failure " + " communityUserId:" + communityUserId);
		
		VotingDO votingDO = null;
		if( votingDao.existsVoting(communityUserId, contentsId, votingTargetType) ) {
			votingDO = votingDao.loadVoting(communityUserId, contentsId, votingTargetType);
			if( votingDO == null)
				return false;
			// 参考になったの評価が同じ場合は、なにもしない。
			if( votingDO.getVotingType() != null && votingDO.getVotingType().equals(votingType) )
				return true;
			
			votingDO.setVotingType(votingType);
			// 更新処理
			votingDao.updateVoting(votingDO);
		}else {
			// 登録処理
			votingDO = new VotingDO();
			votingDO.setTargetType(votingTargetType);
			votingDO.setVotingType(votingType);
			votingDO.setCommunityUser(new CommunityUserDO());
			votingDO.getCommunityUser().setCommunityUserId(communityUserId);
						
			if( VotingTargetType.REVIEW.equals(votingTargetType) ) {
				ReviewDO review = reviewDao.loadReview(contentsId);
				if (review.getCommunityUser() == null || 
						(review.getCommunityUser().getStatus() != CommunityUserStatus.ACTIVE  && 
						!review.getCommunityUser().isKeepReviewContents())) {
					throw new UnActiveException("can not Voting review because deleted " + votingTargetType.name() + " communityUser");
				}
				if( review.getCommunityUser().getCommunityUserId().equals(communityUserId))
					throw new IllegalArgumentException("VotingUser and ReviewUser are the same users.");
				
				votingDO.setReview(new ReviewDO());
				votingDO.getReview().setReviewId(contentsId);
			}else if( VotingTargetType.QUESTION_ANSWER.equals(votingTargetType) ) {
				QuestionAnswerDO questionAnswer = questionAnswerDao.loadQuestionAnswer(contentsId);
				if (questionAnswer.getCommunityUser() == null
						|| (questionAnswer.getCommunityUser().getStatus() != CommunityUserStatus.ACTIVE 
							&& !questionAnswer.getCommunityUser().isKeepQuestionContents())) {
					throw new UnActiveException("can not Voting questionAnswer because deleted communityUser questionAnswerId:" + contentsId);
				}
				if( questionAnswer.getCommunityUser().getCommunityUserId().equals(communityUserId))
					throw new IllegalArgumentException("VotingUser and QuestionUser are the same users.");
				QuestionDO question = questionDao.loadQuestion(questionAnswer.getQuestion().getQuestionId());
				if (question.getCommunityUser() == null
						|| (question.getCommunityUser().getStatus() != CommunityUserStatus.ACTIVE 
							&& !question.getCommunityUser().isKeepQuestionContents())) {
					throw new UnActiveException("can not Voting questionAnswer because deleted communityUser questionId:" + questionAnswer.getQuestion().getQuestionId());
				}
				
				
				
				
				votingDO.setQuestionAnswer(new QuestionAnswerDO());
				votingDO.getQuestionAnswer().setQuestionAnswerId(contentsId);
			}else if( VotingTargetType.IMAGE.equals(votingTargetType) ) {
				ImageHeaderDO imageHeader = imageDao.loadImageHeader(contentsId);
				if (imageHeader == null || (imageHeader.getOwnerCommunityUser().getStatus() != CommunityUserStatus.ACTIVE  &&
						!imageHeader.getOwnerCommunityUser().isKeepImageContents())) {
					throw new UnActiveException("can not Voting image because deleted communityUser imageId:" + contentsId);
				}
				if( imageHeader.getOwnerCommunityUser().getCommunityUserId().equals(communityUserId))
					throw new IllegalArgumentException("VotingUser and ImageUser are the same users.");
				
				votingDO.setImageHeader(new ImageHeaderDO());
				votingDO.getImageHeader().setImageId(contentsId);
			}else{
				throw new UnMatchTargetException("VotingTargetType un match targetType:" + votingTargetType.name());
			}
			
			// 参考になった情報登録処理
			votingDao.createVoting(votingDO);
			
			// お知らせ登録処理
			if( VotingType.YES.equals(votingType) ){
				InformationDO information = null;
				if( VotingTargetType.REVIEW.equals(votingTargetType) ) {
					CommunityUserDO cummunityUserDO = votingDO.getReview().getCommunityUser();
					if( cummunityUserDO != null && cummunityUserDO.getCommunityUserId() != null && !cummunityUserDO.getCommunityUserId().equals(communityUserId)){
						information = new InformationDO();
						information.setReview(votingDO.getReview());
						information.setAdult(information.getReview().isAdult());
						information.setInformationType(InformationType.REVIEW_VOTING_ADD);
						information.setCommunityUser(cummunityUserDO);
					}
				}else if( VotingTargetType.QUESTION_ANSWER.equals(votingTargetType) ) {
					CommunityUserDO cummunityUserDO = votingDO.getQuestionAnswer().getCommunityUser();
					if( cummunityUserDO != null && cummunityUserDO.getCommunityUserId() != null && !cummunityUserDO.getCommunityUserId().equals(communityUserId)){
						information = new InformationDO();
						information.setQuestionAnswer(votingDO.getQuestionAnswer());
						information.setAdult(information.getQuestionAnswer().isAdult());
						information.setInformationType(InformationType.QUESTION_ANSWER_VOTING_ADD);
						information.setCommunityUser(cummunityUserDO);
					}
				}else if( VotingTargetType.IMAGE.equals(votingTargetType) ) {
					CommunityUserDO cummunityUserDO = votingDO.getImageHeader().getCommunityUser();
					if( cummunityUserDO != null && cummunityUserDO.getCommunityUserId() != null && !cummunityUserDO.getCommunityUserId().equals(communityUserId)){
						information = new InformationDO();
						information.setImageHeader(votingDO.getImageHeader());
						information.setAdult(information.getImageHeader().isAdult());
						information.setInformationType(InformationType.IMAGE_VOTING_ADD);
						information.setCommunityUser(cummunityUserDO);
					}
				}
				// 参考になったユーザーと投稿ユーザーが同じ場合はお知らせ表示しない。
				// 参考になったが「はい」の場合はお知らせを表示する。
				if( information != null ){
					information.setVoting(votingDO);
					information.setRelationVotingOwnerId(communityUserId);
					information.setRelationCommunityUserId(communityUserId);
					informationDao.createInformation(information);
					informationId = information.getInformationId();
				}
			}
		}
		
		//　インデックス更新処理
		indexService.updateIndexForSaveVoting(
				votingDO.getVotingId(),
				actionHistoryId,
				informationId);
		
		return true;
		
	}
	
	/**
	 * 指定したコンテンツに対する参考になったユーザーを返します。
	 * @param type タイプ
	 * @param contentsId コンテンツID
	 * @param limit 最大取得件数
	 * @return いいねユーザーリスト
	 */
	private SearchResult<CommunityUserSetVO> createCommunityUserSets(
			VotingTargetType type,
			String contentsId,
			int limit) {
		String communityUserId = requestScopeDao.loadCommunityUserId();
		SearchResult<VotingDO> searchResult = votingDao.findVotingByContentsId(
				type, communityUserId, contentsId, limit);
		SearchResult<CommunityUserSetVO> result = new SearchResult<CommunityUserSetVO>();
		result.setHasAdult(searchResult.isHasAdult());
		result.setNumFound(searchResult.getNumFound());
		List<String> communityUserIds = new ArrayList<String>();
		for (VotingDO voting : searchResult.getDocuments()) {
			communityUserIds.add(voting.getCommunityUser().getCommunityUserId());
		}
		Map<String, Boolean> userFollowMap = null;
		if (communityUserId != null) {
			userFollowMap = communityUserFollowDao.loadCommunityUserFollowMap(
					communityUserId, communityUserIds);
		}
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(searchResult.getDocuments());
		for (VotingDO voting : searchResult.getDocuments()) {
			CommunityUserSetVO vo = new CommunityUserSetVO();
			vo.setCommunityUser(voting.getCommunityUser());
			if (userFollowMap != null
					&& userFollowMap.size() > 0
					&& userFollowMap.containsKey(
							voting.getCommunityUser().getCommunityUserId())) {
				vo.setFollowingUser(userFollowMap.get(
						voting.getCommunityUser().getCommunityUserId()));
			}

			result.updateFirstAndLast(vo);
			if (voting.isStop(communityUserId, stopCommunityUserIds)) {
				result.countUpStopContents();
				continue;
			}
			result.getDocuments().add(vo);
		}
		return result;
	}
	/**
	 * 一時停止ユーザーをフィルタリングします。
	 * @param searchResult 検索結果
	 * @return フィルタリングした結果
	 */
	private SearchResult<VotingDO> filterStopUsers(SearchResult<VotingDO> searchResult) {
		Set<String> stopCommunityUserIds = communityUserDao.getStopCommunityUserIds(searchResult.getDocuments());
		String communityUserId = requestScopeDao.loadCommunityUserId();
		for (Iterator<VotingDO> it = searchResult.getDocuments().iterator(); it.hasNext(); ) {
			VotingDO voting = it.next();
			searchResult.updateFirstAndLast(voting);
			if (voting.isStop(communityUserId, stopCommunityUserIds)) {
				it.remove();
				searchResult.countUpStopContents();
			}
		}
		return searchResult;
	}
}
