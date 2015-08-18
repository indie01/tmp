/**
 *
 */
package com.kickmogu.yodobashi.community.service.impl;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.aop.MyselfAware;
import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.resource.dao.ActionHistoryDao;
import com.kickmogu.yodobashi.community.resource.dao.CommentDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.ImageDao;
import com.kickmogu.yodobashi.community.resource.dao.InformationDao;
import com.kickmogu.yodobashi.community.resource.dao.LikeDao;
import com.kickmogu.yodobashi.community.resource.dao.OrderDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.ProductMasterDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionAnswerDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionDao;
import com.kickmogu.yodobashi.community.resource.dao.QuestionFollowDao;
import com.kickmogu.yodobashi.community.resource.dao.RemoveContentsDao;
import com.kickmogu.yodobashi.community.resource.dao.ReviewDao;
import com.kickmogu.yodobashi.community.resource.dao.SpamReportDao;
import com.kickmogu.yodobashi.community.resource.dao.VotingDao;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.SpamReportDO;
import com.kickmogu.yodobashi.community.resource.domain.VotingDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.AsyncMessageType;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityContentsType;
import com.kickmogu.yodobashi.community.service.IndexService;
import com.kickmogu.yodobashi.community.service.annotation.SendMessage;
import com.kickmogu.yodobashi.community.service.annotation.SendMessage.Timing;

/**
 * インデックスサービスの実装です。
 * @author kamiike
 *
 */
@Service
public class IndexServiceImpl implements IndexService, MyselfAware<IndexService>  {
	
	private static final Logger LOG = LoggerFactory.getLogger(IndexServiceImpl.class);
	/**
	 * アクション履歴 DAO です。
	 */
	@Autowired
	private ActionHistoryDao actionHistoryDao;

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
	 * 参考になったDAO です。
	 */
	@Autowired
	private VotingDao votingDao;

	/**
	 * 注文 DAO です。
	 */
	@Autowired
	private OrderDao orderDao;

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
	 * 違反報告 DAO です。
	 */
	@Autowired
	private SpamReportDao spamReportDao;
	
	/**
	 * いいね DAO です。
	 */
	@Autowired
	private RemoveContentsDao removeContentsDao;

	/**
	 * 自身のインスタンスです。
	 */
	private IndexService myself;
	
	@Override
	public void setMyself(IndexService myself) {
		this.myself = myself;
	}

	
	/**
	 * アクション履歴のインデックス更新とリンク生成を行います。
	 * @param actionHistoryIds アクション履歴IDリスト
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.ACTIONHISTORY)
	@ArroundSolr
	@ArroundHBase
	public void updateActionHistoryIndexWithCreateLink(
			String... actionHistoryIds) {
		if (actionHistoryIds == null) {
			return;
		}
		for (String actionHistoryId : actionHistoryIds) {
			actionHistoryDao.updateActionHistoryInIndex(
					actionHistoryId);
		}
	}

	/**
	 * アクション履歴のインデックス更新とリンク生成を行います。
	 * @param actionHistory アクション履歴
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.ACTIONHISTORY)
	@ArroundSolr
	@ArroundHBase
	public void updateActionHistoryIndexWithCreateLink(ActionHistoryDO actionHistory) {
		if (actionHistory == null) {
			return;
		}
		actionHistoryDao.updateActionHistoryInIndex(actionHistory);
	}
	
	/**
	 * コミュニティユーザー作成に伴うインデックス更新を行います。
	 * @param communityUserId コミュニティユーザーID
	 * @param profileImageId プロフィール画像ID
	 * @param thumbnailImageId サムネイル画像ID
	 * @param informationId お知らせID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.INSTANCYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void updateIndexForCreateCommunityUser(
			String communityUserId,
			String profileImageId,
			String thumbnailImageId,
			String informationId) {
		if (profileImageId != null) {
			imageDao.updateImageInIndex(profileImageId, false, false);
		}
		if (thumbnailImageId != null) {
			imageDao.updateImageInIndex(thumbnailImageId, false, false);
		}
		communityUserDao.updateCommunityUserInIndex(communityUserId);
		if (informationId != null) {
			informationDao.updateInformationInIndex(informationId);
		}
	}

	/**
	 * コミュニティユーザー更新に伴うインデックス更新を行います。
	 * @param communityUserId コミュニティユーザーID
	 * @param imageIds 画像IDリスト
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.INSTANCYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void updateIndexForUpdateCommunityUser(
			String communityUserId,
			String... imageIds) {
		if (imageIds != null && imageIds.length > 0) {
			for (String imageId : imageIds) {
				imageDao.updateImageInIndex(imageId, true, false);
			}
		}
		communityUserDao.updateCommunityUserInIndex(communityUserId);
	}

	/**
	 * レビュー投稿に伴うインデックス更新を行います。
	 * @param reviewId レビューID
	 * @param reviewHistoryId レビュー履歴ID
	 * @param purchaseProductId 購入商品情報ID
	 * @param imageIds 画像IDのリスト
	 * @param userActionHistoryId ユーザーアクション履歴ID
	 * @param productActionHistoryId 商品アクション履歴ID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.INSTANCYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void updateIndexForSaveReview(
			String reviewId,
			String reviewHistoryId,
			String[] previousReviewIds,
			String purchaseProductId,
			String[] imageIds,
			String userActionHistoryId,
			String productActionHistoryId) {
		try{
			if (purchaseProductId != null) {
				orderDao.updatePurchaseProductInIndex(purchaseProductId);
			}
			if (imageIds != null && imageIds.length > 0) {
				// サムネイルがあればそれも更新している
				for (String imageId : imageIds) {
					imageDao.updateImageInIndex(imageId, true, false);
				}
			}
			
			reviewDao.updateReviewInIndex(reviewId);
			
			if( previousReviewIds != null && previousReviewIds.length > 0){
				for( String previousReviewId : previousReviewIds)
					reviewDao.updateReviewInIndex(previousReviewId);
			}
			
			if (reviewHistoryId != null) {
				reviewDao.updateReviewHistoryInIndex(reviewHistoryId);
			}
			
			if (userActionHistoryId != null) {
				myself.updateActionHistoryIndexWithCreateLink(userActionHistoryId);
			}
			if (productActionHistoryId != null) {
				myself.updateActionHistoryIndexWithCreateLink(productActionHistoryId);
			}
		}catch(Exception e){
			LOG.debug("updateIndexForSaveReview", e);
		}
	}

	/**
	 * ポイント付与フィードバックに伴うインデックス更新を行います。
	 * @param reviewId レビューID
	 * @param informationId お知らせID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.DELAYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void updateIndexForPointGrantFeedback(
			String reviewId,
			String informationId) {
		reviewDao.updateReviewInIndexForMR(reviewId);
		if (informationId != null) {
			informationDao.updateInformationInIndex(informationId);
		}
	}

	/**
	 * 質問投稿に伴うインデックス更新を行います。
	 * @param questionId 質問ID
	 * @param imageIds 画像IDのリスト
	 * @param userActionHistoryId ユーザーアクション履歴ID
	 * @param productActionHistoryId 商品アクション履歴ID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.INSTANCYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void updateIndexForSaveQuestion(
			String questionId,
			String[] imageIds,
			String userActionHistoryId,
			String productActionHistoryId) {
		if (imageIds != null && imageIds.length > 0) {
			for (String imageId : imageIds) {
				imageDao.updateImageInIndex(imageId, true, false);
			}
		}
		questionDao.updateQuestionInIndex(questionId);
		if (userActionHistoryId != null) {
			myself.updateActionHistoryIndexWithCreateLink(userActionHistoryId);
		}
		if (productActionHistoryId != null) {
			myself.updateActionHistoryIndexWithCreateLink(productActionHistoryId);
		}
	}

	/**
	 * 質問回答投稿に伴うインデックス更新を行います。
	 * @param questionAnswerId 質問回答ID
	 * @param purchaseProductId 購入商品情報ID
	 * @param imageIds 画像IDのリスト
	 * @param userActionHistoryId ユーザーアクション履歴ID
	 * @param productActionHistoryId 商品アクション履歴ID
	 * @param questionActionHistoryId 質問アクション履歴ID
	 * @param informationId お知らせID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.INSTANCYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void updateIndexForSaveQuestionAnswer(
			String questionAnswerId,
			String purchaseProductId,
			String[] imageIds,
			String userActionHistoryId,
			String productActionHistoryId,
			String questionActionHistoryId,
			String informationId) {
		if (purchaseProductId != null) {
			orderDao.updatePurchaseProductInIndex(purchaseProductId);
		}
		if (imageIds != null && imageIds.length > 0) {
			for (String imageId : imageIds) {
				imageDao.updateImageInIndex(imageId, true, false);
			}
		}
		QuestionAnswerDO questionAnswer = questionAnswerDao.updateQuestionAnswerInIndex(questionAnswerId);
		if (questionAnswer != null && !questionAnswer.isDeleted()) {
			questionDao.updateQuestionInIndex(
					questionAnswer.getQuestion().getQuestionId());
		}
		if (userActionHistoryId != null) {
			myself.updateActionHistoryIndexWithCreateLink(userActionHistoryId);
		}
		if (productActionHistoryId != null) {
			myself.updateActionHistoryIndexWithCreateLink(productActionHistoryId);
		}
		if (questionActionHistoryId != null) {
			myself.updateActionHistoryIndexWithCreateLink(questionActionHistoryId);
		}
		if (informationId != null) {
			informationDao.updateInformationInIndex(informationId);
		}
	}

	/**
	 * コミュニティユーザーのフォロー関連情報のインデックスを更新します。
	 * @param communityUserId フォロワーとなるコミュニティユーザーID
	 * @param followCommunityUserId フォローするコミュニティユーザーID
	 * @param actionHistoryId アクション履歴ID
	 * @param informationId お知らせ情報ID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.INSTANCYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void updateIndexForCommunityUserFollow(
			String communityUserId,
			String followCommunityUserId,
			String actionHistoryId,
			String informationId) {
		communityUserFollowDao.updateCommunityUserFollowInIndex(
				communityUserId, followCommunityUserId);
		if (actionHistoryId != null) {
			myself.updateActionHistoryIndexWithCreateLink(actionHistoryId);
		}
		if (informationId != null) {
			informationDao.updateInformationInIndex(informationId);
		}
	}

	/**
	 * 商品のフォロー関連情報のインデックスを更新します。
	 * @param communityUserId フォロワーとなるコミュニティユーザーID
	 * @param followProductId フォローする商品ID
	 * @param actionHistoryId アクション履歴ID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.INSTANCYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void updateIndexForProductFollow(
			String communityUserId,
			String followProductId,
			String actionHistoryId) {
		productFollowDao.updateProductFollowInIndex(
				communityUserId, followProductId);
		if (actionHistoryId != null) {
			myself.updateActionHistoryIndexWithCreateLink(actionHistoryId);
		}
	}

	/**
	 * 質問のフォロー関連情報のインデックスを更新します。
	 * @param communityUserId フォロワーとなるコミュニティユーザーID
	 * @param followQuestionId フォローする質問ID
	 * @param actionHistoryId アクション履歴ID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.INSTANCYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void updateIndexForQuestionFollow(
			String communityUserId,
			String followQuestionId,
			String actionHistoryId) {
		questionFollowDao.updateQuestionFollowInIndex(
				communityUserId, followQuestionId);
		if (actionHistoryId != null) {
			myself.updateActionHistoryIndexWithCreateLink(actionHistoryId);
		}
	}

	/**
	 * コメント登録に伴うインデックス更新を行います。
	 * @param commentId コメントID
	 * @param actionHistoryId アクション履歴ID
	 * @param informationId お知らせ情報ID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.INSTANCYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void updateIndexForSaveComment(
			String commentId,
			String actionHistoryId,
			String informationId) {
		commentDao.updateCommentInIndex(commentId);
		if (actionHistoryId != null) {
			myself.updateActionHistoryIndexWithCreateLink(actionHistoryId);
		}
		if (informationId != null) {
			informationDao.updateInformationInIndex(informationId);
		}
		removeContentsDao.updateRemoveContentsInIndex(IdUtil.createIdByConcatIds(IdUtil.createIdByConcatIds(commentId, CommunityContentsType.COMMENT.getCode())));
	}

	/**
	 * いいね登録に伴うインデックス更新を行います。
	 * @param likeId いいねID
	 * @param actionHistoryId アクション履歴ID
	 * @param informationId お知らせ情報ID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.INSTANCYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void updateIndexForSaveLike(
			String likeId,
			String actionHistoryId,
			String informationId) {
		if( likeId != null)
			likeDao.updateLikeInIndex(likeId);
		
		if (actionHistoryId != null)
			myself.updateActionHistoryIndexWithCreateLink(actionHistoryId);
		
		if (informationId != null)
			informationDao.updateInformationInIndex(informationId);
	}
	
	
	@Override
	public void updateIndexForSaveVoting(
			String votingId,
			String actionHistoryId,
			String informationId) {
		if( votingId != null)
			votingDao.updateVotingInIndex(votingId);
		
		if (actionHistoryId != null)
			myself.updateActionHistoryIndexWithCreateLink(actionHistoryId);
		
		if (informationId != null)
			informationDao.updateInformationInIndex(informationId);
	}


	/**
	 * 画像セット登録に伴うインデックス更新を行います。
	 * @param imageIds 画像IDのリスト
	 * @param purchaseProductId 購入商品情報ID
	 * @param userActionHistoryId ユーザーアクション履歴ID
	 * @param productActionHistoryId 商品アクション履歴ID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.INSTANCYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void updateIndexForSaveImageSet(
			String[] imageIds,
			String purchaseProductId,
			String userActionHistoryId,
			String productActionHistoryId) {
		if (imageIds != null && imageIds.length > 0) {
			for (String imageId : imageIds) {
				imageDao.updateImageInIndex(imageId, true, false);
			}
		}
		if (purchaseProductId != null) {
			orderDao.updatePurchaseProductInIndex(purchaseProductId);
		}
		if (userActionHistoryId != null) {
			myself.updateActionHistoryIndexWithCreateLink(userActionHistoryId);
		}
		if (productActionHistoryId != null) {
			myself.updateActionHistoryIndexWithCreateLink(productActionHistoryId);
		}
	}

	/**
	 * 画像セット更新に伴うインデックス更新を行います。
	 * @param imageSetId 画像セットID
	 * @param imageId 画像ID
	 * @param nextListViewImageId 次に一覧表示される画像ID
	 * @param delete 完全に削除されたかどうか
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.INSTANCYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void updateIndexForUpdateImageSet(
			String imageSetId,
			String imageId,
			String thumbnailImageId,
			String nextListViewImageId,
			Boolean delete) {
		if (delete) {
			imageDao.deleteImageSetIndex(imageSetId, thumbnailImageId, false);
		} else {
			imageDao.updateImageInIndex(imageId, true, false);
			if (nextListViewImageId != null) {
				imageDao.updateImageInIndex(nextListViewImageId, true, false);
			}
		}
	}
	
	/**
	 * お知らせ更新に伴うインデックス更新を行います。
	 * @param informationIds お知らせIDリスト
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.INFORMATION)
	@ArroundSolr
	@ArroundHBase
	public void updateIndexForUpdateInformation(
			List<String> informationIds) {
		if (informationIds != null) {
			for (String informationId : informationIds) {
				informationDao.updateInformationInIndex(informationId);
			}
		}
	}

	/**
	 * 購入商品生成に伴うインデックス更新を行います。
	 * @param purchaseProductIds 購入商品IDリスト
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.INSTANCYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void updateIndexForCreatePurchaseProduct(
			String... purchaseProductIds) {
		for (String purchaseProductId : purchaseProductIds) {
			orderDao.updatePurchaseProductInIndex(purchaseProductId);
		}
	}

	/**
	 * 違反報告の更新に伴うインデックス更新を行います。
	 * @param spamReportId 違反報告ID
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.INSTANCYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void updateIndexForSaveSpamReport(
			String spamReportId) {
		spamReportDao.updateSpamReportInIndex(spamReportId);
	}

	/**
	 * 指定した更新キーマップに従ってインデックスの同期処理を行います。
	 * @param communityUserId コミュニティユーザーID
	 * @param updateKeyMap 更新対象キーマップ
	 * @param imageUpload 画像アップロード
	 */
	@Override
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS,asyncMessageType=AsyncMessageType.DELAYSERVICE)
	@ArroundSolr
	@ArroundHBase
	public void syncIndexForCommunityUser(
			String communityUserId,
			HashMap<Class<?>, List<String>> updateKeyMap,
			Boolean imageUpload) {
		communityUserDao.updateCommunityUserInIndex(communityUserId);

		//    購入商品（PurchaseProductDO）
		if (updateKeyMap.containsKey(PurchaseProductDO.class)) {
			for (String key : updateKeyMap.get(PurchaseProductDO.class)) {
				orderDao.updatePurchaseProductInIndex(key);
			}
		}
		//    画像ヘッダー（ImageHeaderDO）
		if (updateKeyMap.containsKey(ImageHeaderDO.class)) {
			for (String key : updateKeyMap.get(ImageHeaderDO.class)) {
				if (imageUpload) {
					imageDao.uploadImageForSync(key);
				}
				imageDao.updateImageInIndex(key, false, false);
			}
		}
		//    レビュー（ReviewDO）
		if (updateKeyMap.containsKey(ReviewDO.class)) {
			for (String key : updateKeyMap.get(ReviewDO.class)) {
				reviewDao.updateReviewInIndex(key);
			}
		}
		//    レビュー（ReviewHistoryDO）
		if (updateKeyMap.containsKey(ReviewHistoryDO.class)) {
			for (String key : updateKeyMap.get(ReviewHistoryDO.class)) {
				reviewDao.updateReviewHistoryInIndex(key);
			}
		}
		//    質問（QuestionDO）
		if (updateKeyMap.containsKey(QuestionDO.class)) {
			for (String key : updateKeyMap.get(QuestionDO.class)) {
				questionDao.updateQuestionInIndex(key);
			}
		}
		//    質問回答（QuestionAnswerDO）
		if (updateKeyMap.containsKey(QuestionAnswerDO.class)) {
			for (String key : updateKeyMap.get(QuestionAnswerDO.class)) {
				questionAnswerDao.updateQuestionAnswerInIndex(key);
			}
		}
		//    コメント（CommentDO）
		if (updateKeyMap.containsKey(CommentDO.class)) {
			for (String key : updateKeyMap.get(CommentDO.class)) {
				commentDao.updateCommentInIndex(key);
			}
		}
		//    いいね（LikeDO）
		if (updateKeyMap.containsKey(LikeDO.class)) {
			for (String key : updateKeyMap.get(LikeDO.class)) {
				likeDao.updateLikeInIndex(key);
			}
		}
		//    参考になった（VotingDO)
		if (updateKeyMap.containsKey(VotingDO.class)) {
			for (String key : updateKeyMap.get(VotingDO.class)) {
				votingDao.updateVotingInIndex(key);
			}
		}
		//    質問フォロー（QuestionFollowDO）
		if (updateKeyMap.containsKey(QuestionFollowDO.class)) {
			for (String key : updateKeyMap.get(QuestionFollowDO.class)) {
				questionFollowDao.createQuestionFollowInIndex(key);
			}
		}
		//    コミュニティユーザーフォロー（CommunityUserFollowDO）
		if (updateKeyMap.containsKey(CommunityUserFollowDO.class)) {
			for (String key : updateKeyMap.get(CommunityUserFollowDO.class)) {
				communityUserFollowDao.createCommunityUserFollowInIndex(key);
			}
		}
		//    商品フォロー（ProductFollowDO）
		if (updateKeyMap.containsKey(ProductFollowDO.class)) {
			for (String key : updateKeyMap.get(ProductFollowDO.class)) {
				productFollowDao.createProductFollowInIndex(key);
			}
		}
		//    商品マスター（ProductMasterDO）
		if (updateKeyMap.containsKey(ProductMasterDO.class)) {
			for (String key : updateKeyMap.get(ProductMasterDO.class)) {
				productMasterDao.updateProductMasterInIndex(key);
			}
		}
		//    アクション履歴（ActionHistoryDO）
		if (updateKeyMap.containsKey(ActionHistoryDO.class)) {
			for (String key : updateKeyMap.get(ActionHistoryDO.class)) {
				actionHistoryDao.updateActionHistoryInIndex(key);
			}
		}
		//    お知らせ（InformationDO）
		if (updateKeyMap.containsKey(InformationDO.class)) {
			for (String key : updateKeyMap.get(InformationDO.class)) {
				informationDao.updateInformationInIndex(key);
			}
		}
		//    スパム報告（SpamReportDO）
		if (updateKeyMap.containsKey(SpamReportDO.class)) {
			for (String key : updateKeyMap.get(SpamReportDO.class)) {
				spamReportDao.updateSpamReportInIndex(key);
			}
		}
	}

}
