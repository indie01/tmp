/**
 *
 */
package com.kickmogu.yodobashi.community.resource.domain.util;

import java.util.ArrayList;
import java.util.List;

import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseLostProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDecisivePurchaseDO;
import com.kickmogu.yodobashi.community.resource.domain.UsedProductDO;
import com.kickmogu.yodobashi.community.resource.domain.VotingDO;

/**
 * @author kamiike
 *
 */
public class CommunityUserUtil {

	/**
	 * アクション履歴から関連オーナーのリストを返します。
	 * @param actionHistory アクション履歴
	 * @return 関連オーナーのリスト
	 */
	public static List<CommunityUserDO> getRelationOwners(ActionHistoryDO actionHistory) {
		List<CommunityUserDO> list = new ArrayList<CommunityUserDO>();
		if (actionHistory == null) {
			return list;
		}
		addCommunityUser(list, actionHistory.getCommunityUser());
		addCommunityUser(list, actionHistory.getFollowCommunityUser());
		list.addAll(getRelationOwners(actionHistory.getReview()));
		list.addAll(getRelationOwners(actionHistory.getQuestion()));
		list.addAll(getRelationOwners(actionHistory.getQuestionAnswer()));
		list.addAll(getRelationOwners(actionHistory.getProductMaster()));
		list.addAll(getRelationOwners(actionHistory.getComment()));
		list.addAll(getRelationOwners(actionHistory.getImageHeader()));
		return list;
	}

	/**
	 * アクション履歴から関連オーナーIDのリストを返します。
	 * @param actionHistory アクション履歴
	 * @return 関連オーナーIDのリスト
	 */
	public static List<String> getRelationOwnerIds(ActionHistoryDO actionHistory) {
		List<String> list = new ArrayList<String>();
		if (actionHistory == null) {
			return list;
		}
		addCommunityUserId(list, actionHistory.getRelationImageOwnerId());
		addCommunityUserId(list, actionHistory.getRelationQuestionAnswerOwnerId());
		addCommunityUserId(list, actionHistory.getRelationQuestionOwnerId());
		addCommunityUserId(list, actionHistory.getRelationReviewOwnerId());
		return list;
	}

	/**
	 * コメントから関連オーナーのリストを返します。
	 * @param comment コメント
	 * @return 関連オーナーのリスト
	 */
	public static List<CommunityUserDO> getRelationOwners(CommentDO comment) {
		List<CommunityUserDO> list = new ArrayList<CommunityUserDO>();
		if (comment == null) {
			return list;
		}
		addCommunityUser(list, comment.getCommunityUser());
		list.addAll(getRelationOwners(comment.getImageHeader()));
		list.addAll(getRelationOwners(comment.getReview()));
		list.addAll(getRelationOwners(comment.getQuestionAnswer()));

		return list;
	}

	/**
	 * コメントから関連オーナーIDのリストを返します。
	 * @param comment コメント
	 * @return 関連オーナーIDのリスト
	 */
	public static List<String> getRelationOwnerIds(CommentDO comment) {
		List<String> list = new ArrayList<String>();
		if (comment == null) {
			return list;
		}
		addCommunityUserId(list, comment.getRelationImageOwnerId());
		addCommunityUserId(list, comment.getRelationQuestionAnswerOwnerId());
		addCommunityUserId(list, comment.getRelationQuestionOwnerId());
		addCommunityUserId(list, comment.getRelationReviewOwnerId());
		return list;
	}

	/**
	 * いいねから関連オーナーのリストを返します。
	 * @param like いいね
	 * @return 関連オーナーのリスト
	 */
	public static List<CommunityUserDO> getRelationOwners(LikeDO like) {
		List<CommunityUserDO> list = new ArrayList<CommunityUserDO>();
		if (like == null) {
			return list;
		}
		addCommunityUser(list, like.getCommunityUser());
		list.addAll(getRelationOwners(like.getImageHeader()));
		list.addAll(getRelationOwners(like.getReview()));
		list.addAll(getRelationOwners(like.getQuestionAnswer()));

		return list;
	}

	/**
	 * いいねから関連オーナーIDのリストを返します。
	 * @param like いいね
	 * @return 関連オーナーIDのリスト
	 */
	public static List<String> getRelationOwnerIds(LikeDO like) {
		List<String> list = new ArrayList<String>();
		if (like == null) {
			return list;
		}
		addCommunityUserId(list, like.getRelationImageOwnerId());
		addCommunityUserId(list, like.getRelationQuestionAnswerOwnerId());
		addCommunityUserId(list, like.getRelationQuestionOwnerId());
		addCommunityUserId(list, like.getRelationReviewOwnerId());
		return list;
	}
	
	/**
	 * 参考になったからから関連オーナーのリストを返します。
	 * @param like いいね
	 * @return 関連オーナーのリスト
	 */
	public static List<CommunityUserDO> getRelationOwners(VotingDO voting) {
		List<CommunityUserDO> list = new ArrayList<CommunityUserDO>();
		if (voting == null) {
			return list;
		}
		addCommunityUser(list, voting.getCommunityUser());
		list.addAll(getRelationOwners(voting.getImageHeader()));
		list.addAll(getRelationOwners(voting.getReview()));
		list.addAll(getRelationOwners(voting.getQuestionAnswer()));

		return list;
	}
	
	/**
	 * 参考になったからから関連オーナーIDのリストを返します。
	 * @param like いいね
	 * @return 関連オーナーIDのリスト
	 */
	public static List<String> getRelationOwnerIds(VotingDO voting) {
		List<String> list = new ArrayList<String>();
		if (voting == null) {
			return list;
		}
		addCommunityUserId(list, voting.getRelationImageOwnerId());
		addCommunityUserId(list, voting.getRelationQuestionAnswerOwnerId());
		addCommunityUserId(list, voting.getRelationQuestionOwnerId());
		addCommunityUserId(list, voting.getRelationReviewOwnerId());
		return list;
	}

	/**
	 * 画像から関連オーナーのリストを返します。
	 * @param imageHeader 画像
	 * @return 関連オーナーのリスト
	 */
	public static List<CommunityUserDO> getRelationOwners(ImageHeaderDO imageHeader) {
		List<CommunityUserDO> list = new ArrayList<CommunityUserDO>();
		if (imageHeader == null) {
			return list;
		}
		addCommunityUser(list, imageHeader.getOwnerCommunityUser());
		list.addAll(getRelationOwners(imageHeader.getReview()));
		list.addAll(getRelationOwners(imageHeader.getQuestion()));
		list.addAll(getRelationOwners(imageHeader.getQuestionAnswer()));

		return list;
	}

	/**
	 * 画像から関連オーナーIDのリストを返します。
	 * @param imageHeader 画像
	 * @return 関連オーナーIDのリスト
	 */
	public static List<String> getRelationOwnerIds(ImageHeaderDO imageHeader) {
		List<String> list = new ArrayList<String>();
		if (imageHeader == null) {
			return list;
		}
		addCommunityUserId(list, imageHeader.getRelationQuestionOwnerId());
		return list;
	}

	/**
	 * お知らせから関連オーナーのリストを返します。
	 * @param information お知らせ
	 * @return 関連オーナーのリスト
	 */
	public static List<CommunityUserDO> getRelationOwners(InformationDO information) {
		List<CommunityUserDO> list = new ArrayList<CommunityUserDO>();
		if (information == null) {
			return list;
		}
		addCommunityUser(list, information.getCommunityUser());
		addCommunityUser(list, information.getFollowerCommunityUser());
		list.addAll(getRelationOwners(information.getReview()));
		list.addAll(getRelationOwners(information.getQuestion()));
		list.addAll(getRelationOwners(information.getQuestionAnswer()));
		list.addAll(getRelationOwners(information.getProductMaster()));
		list.addAll(getRelationOwners(information.getComment()));
		list.addAll(getRelationOwners(information.getImageHeader()));
		list.addAll(getRelationOwners(information.getComment()));
		list.addAll(getRelationOwners(information.getLike()));
		list.addAll(getRelationOwners(information.getVoting()));
		return list;
	}

	/**
	 * お知らせから関連オーナーIDのリストを返します。
	 * @param information お知らせ
	 * @return 関連オーナーIDのリスト
	 */
	public static List<String> getRelationOwnerIds(InformationDO information) {
		List<String> list = new ArrayList<String>();
		if (information == null) {
			return list;
		}
		addCommunityUserId(list, information.getRelationCommentOwnerId());
		addCommunityUserId(list, information.getRelationQuestionAnswerOwnerId());
		addCommunityUserId(list, information.getRelationQuestionOwnerId());
		addCommunityUserId(list, information.getRelationCommunityUserId());
		addCommunityUserId(list, information.getRelationLikeOwnerId());
		addCommunityUserId(list, information.getRelationVotingOwnerId());
		
		return list;
	}

	/**
	 * 商品マスターから関連オーナーのリストを返します。
	 * @param productMaster 商品マスター
	 * @return 関連オーナーのリスト
	 */
	public static List<CommunityUserDO> getRelationOwners(ProductMasterDO productMaster) {
		List<CommunityUserDO> list = new ArrayList<CommunityUserDO>();
		if (productMaster == null) {
			return list;
		}
		addCommunityUser(list, productMaster.getCommunityUser());
		return list;
	}

	/**
	 * 商品マスターから関連オーナーIDのリストを返します。
	 * @param productMaster 商品マスター
	 * @return 関連オーナーIDのリスト
	 */
	public static List<String> getRelationOwnerIds(ProductMasterDO productMaster) {
		return null;
	}

	/**
	 * 購入に迷った商品から関連オーナーのリストを返します。
	 * @param purchaseLostProduct 購入に迷った商品
	 * @return 関連オーナーのリスト
	 */
	public static List<CommunityUserDO> getRelationOwners(PurchaseLostProductDO purchaseLostProduct) {
		List<CommunityUserDO> list = new ArrayList<CommunityUserDO>();
		if (purchaseLostProduct == null) {
			return list;
		}
		addCommunityUser(list, purchaseLostProduct.getCommunityUser());
		list.addAll(getRelationOwners(purchaseLostProduct.getReview()));
		return list;
	}

	/**
	 * 購入に迷った商品から関連オーナーIDのリストを返します。
	 * @param purchaseLostProduct 購入に迷った商品
	 * @return 関連オーナーIDのリスト
	 */
	public static List<String> getRelationOwnerIds(PurchaseLostProductDO purchaseLostProduct) {
		return null;
	}

	/**
	 * 購入商品情報から関連オーナーのリストを返します。
	 * @param purchaseLostProduct 購入商品情報
	 * @return 関連オーナーのリスト
	 */
	public static List<CommunityUserDO> getRelationOwners(PurchaseProductDO purchaseProduct) {
		List<CommunityUserDO> list = new ArrayList<CommunityUserDO>();
		if (purchaseProduct == null) {
			return list;
		}
		addCommunityUser(list, purchaseProduct.getCommunityUser());
		return list;
	}

	/**
	 * 購入商品情報から関連オーナーIDのリストを返します。
	 * @param purchaseLostProduct 購入商品情報
	 * @return 関連オーナーIDのリスト
	 */
	public static List<String> getRelationOwnerIds(PurchaseProductDO purchaseProduct) {
		return null;
	}

	/**
	 * 質問回答から関連オーナーのリストを返します。
	 * @param questionAnswer 質問回答
	 * @return 関連オーナーのリスト
	 */
	public static List<CommunityUserDO> getRelationOwners(QuestionAnswerDO questionAnswer) {
		List<CommunityUserDO> list = new ArrayList<CommunityUserDO>();
		if (questionAnswer == null) {
			return list;
		}
		addCommunityUser(list, questionAnswer.getCommunityUser());
		list.addAll(getRelationOwners(questionAnswer.getQuestion()));
		return list;
	}

	/**
	 * 質問回答から関連オーナーIDのリストを返します。
	 * @param questionAnswer 質問回答
	 * @return 関連オーナーIDのリスト
	 */
	public static List<String> getRelationOwnerIds(QuestionAnswerDO questionAnswer) {
		List<String> list = new ArrayList<String>();
		if (questionAnswer == null) {
			return list;
		}
		addCommunityUserId(list, questionAnswer.getRelationQuestionOwnerId());
		return list;
	}

	/**
	 * 質問から関連オーナーのリストを返します。
	 * @param question 質問
	 * @return 関連オーナーのリスト
	 */
	public static List<CommunityUserDO> getRelationOwners(QuestionDO question) {
		List<CommunityUserDO> list = new ArrayList<CommunityUserDO>();
		if (question == null) {
			return list;
		}
		addCommunityUser(list, question.getCommunityUser());
		return list;
	}

	/**
	 * 質問から関連オーナーIDのリストを返します。
	 * @param question 質問
	 * @return 関連オーナーIDのリスト
	 */
	public static List<String> getRelationOwnerIds(QuestionDO question) {
		return null;
	}

	/**
	 * 質問フォローから関連オーナーのリストを返します。
	 * @param questionFollow 質問フォロー
	 * @return 関連オーナーのリスト
	 */
	public static List<CommunityUserDO> getRelationOwners(QuestionFollowDO questionFollow) {
		List<CommunityUserDO> list = new ArrayList<CommunityUserDO>();
		if (questionFollow == null) {
			return list;
		}
		addCommunityUser(list, questionFollow.getCommunityUser());
		list.addAll(getRelationOwners(questionFollow.getFollowQuestion()));
		return list;
	}

	/**
	 * 質問フォローから関連オーナーIDのリストを返します。
	 * @param questionFollow 質問フォロー
	 * @return 関連オーナーIDのリスト
	 */
	public static List<String> getRelationOwnerIds(QuestionFollowDO questionFollow) {
		List<String> list = new ArrayList<String>();
		if (questionFollow == null) {
			return list;
		}
		addCommunityUserId(list, questionFollow.getRelationQuestionOwnerId());
		return list;
	}

	/**
	 * 商品フォローから関連オーナーのリストを返します。
	 * @param productFollow 商品フォロー
	 * @return 関連オーナーのリスト
	 */
	public static List<CommunityUserDO> getRelationOwners(ProductFollowDO productFollow) {
		List<CommunityUserDO> list = new ArrayList<CommunityUserDO>();
		if (productFollow == null) {
			return list;
		}
		addCommunityUser(list, productFollow.getCommunityUser());
		return list;
	}

	/**
	 * 商品フォローから関連オーナーIDのリストを返します。
	 * @param productFollow 商品フォロー
	 * @return 関連オーナーIDのリスト
	 */
	public static List<String> getRelationOwnerIds(ProductFollowDO productFollow) {
		return null;
	}

	/**
	 * コミュニティユーザーフォローから関連オーナーのリストを返します。
	 * @param communityUserFollow コミュニティユーザーフォロー
	 * @return 関連オーナーのリスト
	 */
	public static List<CommunityUserDO> getRelationOwners(CommunityUserFollowDO communityUserFollow) {
		List<CommunityUserDO> list = new ArrayList<CommunityUserDO>();
		if (communityUserFollow == null) {
			return list;
		}
		addCommunityUser(list, communityUserFollow.getCommunityUser());
		addCommunityUser(list, communityUserFollow.getFollowCommunityUser());
		return list;
	}

	/**
	 * コミュニティユーザーフォローから関連オーナーIDのリストを返します。
	 * @param communityUserFollow コミュニティユーザーフォロー
	 * @return 関連オーナーIDのリスト
	 */
	public static List<String> getRelationOwnerIds(CommunityUserFollowDO communityUserFollow) {
		return null;
	}

	/**
	 * レビュー購入の決め手から関連オーナーのリストを返します。
	 * @param reviewDecisivePurchase レビュー購入の決め手
	 * @return 関連オーナーのリスト
	 */
	public static List<CommunityUserDO> getRelationOwners(ReviewDecisivePurchaseDO reviewDecisivePurchase) {
		List<CommunityUserDO> list = new ArrayList<CommunityUserDO>();
		if (reviewDecisivePurchase == null) {
			return list;
		}
		addCommunityUser(list, reviewDecisivePurchase.getCommunityUser());
		list.addAll(getRelationOwners(reviewDecisivePurchase.getReview()));
		return list;
	}

	/**
	 * レビュー購入の決め手から関連オーナーIDのリストを返します。
	 * @param reviewDecisivePurchase レビュー購入の決め手
	 * @return 関連オーナーIDのリスト
	 */
	public static List<String> getRelationOwnerIds(ReviewDecisivePurchaseDO reviewDecisivePurchase) {
		return null;
	}

	/**
	 * レビューから関連オーナーのリストを返します。
	 * @param review レビュー
	 * @return 関連オーナーのリスト
	 */
	public static List<CommunityUserDO> getRelationOwners(ReviewDO review) {
		List<CommunityUserDO> list = new ArrayList<CommunityUserDO>();
		if (review == null) {
			return list;
		}
		addCommunityUser(list, review.getCommunityUser());
		return list;
	}

	/**
	 * レビューから関連オーナーIDのリストを返します。
	 * @param review レビュー
	 * @return 関連オーナーIDのリスト
	 */
	public static List<String> getRelationOwnerIds(ReviewDO review) {
		return null;
	}

	/**
	 * 過去に使用した商品から関連オーナーのリストを返します。
	 * @param usedProduct 過去に使用した商品
	 * @return 関連オーナーのリスト
	 */
	public static List<CommunityUserDO> getRelationOwners(UsedProductDO usedProduct) {
		List<CommunityUserDO> list = new ArrayList<CommunityUserDO>();
		if (usedProduct == null) {
			return list;
		}
		addCommunityUser(list, usedProduct.getCommunityUser());
		list.addAll(getRelationOwners(usedProduct.getReview()));
		return list;
	}

	/**
	 * 過去に使用した商品から関連オーナーIDのリストを返します。
	 * @param usedProduct 過去に使用した商品
	 * @return 関連オーナーIDのリスト
	 */
	public static List<String> getRelationOwnerIds(UsedProductDO usedProduct) {
		return null;
	}

	/**
	 * コミュニティユーザーをリストに追加します。
	 * @param list リスト
	 * @param communityUser コミュニティユーザー
	 */
	private static void addCommunityUser(
			List<CommunityUserDO> list,
			CommunityUserDO communityUser) {
		if (communityUser != null) {
			list.add(communityUser);
		}
	}

	/**
	 * コミュニティユーザーIDをリストに追加します。
	 * @param list リスト
	 * @param communityUserId コミュニティユーザーID
	 */
	private static void addCommunityUserId(
			List<String> list,
			String communityUserId) {
		if (communityUserId != null) {
			list.add(communityUserId);
		}
	}
}
