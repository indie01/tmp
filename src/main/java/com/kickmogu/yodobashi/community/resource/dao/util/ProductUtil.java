/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.util;

import java.util.Iterator;
import java.util.List;

import com.kickmogu.lib.core.resource.domain.BaseDO;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
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
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.SpamReportDO;
import com.kickmogu.yodobashi.community.resource.domain.UsedProductDO;
import com.kickmogu.yodobashi.community.resource.domain.VotingDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;

/**
 * @author kamiike
 *
 */
public class ProductUtil {

	public static <E extends BaseDO> void filterInvalidProduct(SearchResult<E> searchResult) {
		List<E> list = searchResult.getDocuments();
		filterInvalidProduct(list);
	}

	public static <E extends BaseDO> void filterInvalidProduct(List<E> list) {
		for (Iterator<E> it = list.iterator(); it.hasNext(); ) {
			E o = it.next();
			if (o instanceof ActionHistoryDO && invalid((ActionHistoryDO) o)) {
				it.remove();
			} else if (o instanceof ReviewDO && invalid((ReviewDO) o)) {
				it.remove();
			} else if (o instanceof QuestionDO && invalid((QuestionDO) o)) {
				it.remove();
			} else if (o instanceof QuestionAnswerDO && invalid((QuestionAnswerDO) o)) {
				it.remove();
			} else if (o instanceof ImageHeaderDO && invalid((ImageHeaderDO) o)) {
				it.remove();
			} else if (o instanceof PurchaseProductDO && invalid((PurchaseProductDO) o)) {
				it.remove();
			} else if (o instanceof CommentDO && invalid((CommentDO) o)) {
				it.remove();
			} else if (o instanceof InformationDO && invalid((InformationDO) o)) {
				it.remove();
			} else if (o instanceof LikeDO && invalid((LikeDO) o)) {
				it.remove();
			} else if (o instanceof VotingDO && invalid((VotingDO) o)) {
				it.remove();
			} else if (o instanceof QuestionFollowDO && invalid((QuestionFollowDO) o)) {
				it.remove();
			} else if (o instanceof ProductFollowDO && invalid((ProductFollowDO) o)) {
				it.remove();
			} else if (o instanceof UsedProductDO && invalid((UsedProductDO) o)) {
				it.remove();
			} else if (o instanceof PurchaseLostProductDO && invalid((PurchaseLostProductDO) o)) {
				it.remove();
			} else if (o instanceof SpamReportDO && invalid((SpamReportDO) o)) {
				it.remove();
			} else if (o instanceof ProductMasterDO && invalid((ProductMasterDO) o)) {
				it.remove();
			}
		}
	}

	public static boolean invalid(ActionHistoryDO target) {
		if (target == null) {
			return false;
		}
		// TODO 参考になったを入れるかどうか検討
		if (target.getActionHistoryType().equals(ActionHistoryType.USER_REVIEW_COMMENT)
				|| target.getActionHistoryType().equals(ActionHistoryType.USER_IMAGE_COMMENT)
				|| target.getActionHistoryType().equals(ActionHistoryType.USER_FOLLOW_PRODUCT)
				|| target.getActionHistoryType().equals(ActionHistoryType.USER_FOLLOW_QUESTION)
				|| target.getActionHistoryType().equals(ActionHistoryType.USER_IMAGE)
				|| target.getActionHistoryType().equals(ActionHistoryType.PRODUCT_IMAGE)
				|| target.getActionHistoryType().equals(ActionHistoryType.LIKE_IMAGE_50)
				|| target.getActionHistoryType().equals(ActionHistoryType.LIKE_ANSWER_50)
				|| target.getActionHistoryType().equals(ActionHistoryType.LIKE_REVIEW_50)
				|| target.getActionHistoryType().equals(ActionHistoryType.USER_PRODUCT_MASTER_RANK_CHANGE)
				|| target.getActionHistoryType().equals(ActionHistoryType.USER_QUESTION)
				|| target.getActionHistoryType().equals(ActionHistoryType.PRODUCT_QUESTION)
				|| target.getActionHistoryType().equals(ActionHistoryType.USER_ANSWER)
				|| target.getActionHistoryType().equals(ActionHistoryType.PRODUCT_ANSWER)
				|| target.getActionHistoryType().equals(ActionHistoryType.QUESTION_ANSWER)
				|| target.getActionHistoryType().equals(ActionHistoryType.USER_REVIEW)
				|| target.getActionHistoryType().equals(ActionHistoryType.PRODUCT_REVIEW)
				) {
			if (target.getProduct() == null) {
				return true;
			}
		}
		if (target.getCommunityUser() == null
				|| target.getCommunityUser().getCommunityName() == null) {
			return true;
		}

		return false;
	}

	public static boolean invalid(ReviewDO target) {
		if (target == null) {
			return false;
		}
		if (target.getStatus() != null
				&& target.getProduct() == null) {
			return true;
		}
		if (target.getPurchaseLostProducts() != null) {
			for (PurchaseLostProductDO child : target.getPurchaseLostProducts()) {
				if (invalid(child)) {
					return true;
				}
			}
		}
		if (target.getUsedProducts() != null) {
			for (UsedProductDO child : target.getUsedProducts()) {
				if (invalid(child)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean invalid(QuestionDO target) {
		if (target == null) {
			return false;
		}
		if (target.getStatus() != null
				&& target.getProduct() == null) {
			return true;
		}
		return false;
	}

	public static boolean invalid(QuestionAnswerDO target) {
		if (target == null) {
			return false;
		}
		if (target.getStatus() != null
				&& target.getProduct() == null) {
			return true;
		}
		if (target.getQuestion() != null) {
			if (invalid(target.getQuestion())) {
				return true;
			}
		}
		return false;
	}

	public static boolean invalid(ImageHeaderDO target) {
		if (target == null) {
			return false;
		}
		if (target.getStatus() != null
				&& target.getProduct() == null) {
			return true;
		}
		return false;
	}

	public static boolean invalid(PurchaseProductDO target) {
		if (target == null) {
			return false;
		}
		if (target.getProduct() == null) {
			return true;
		}
		return false;
	}

	public static boolean invalid(CommentDO target) {
		if (target == null) {
			return false;
		}
		if (invalid(target.getReview())) {
			return true;
		}
		if (invalid(target.getQuestionAnswer())) {
			return true;
		}
		if (invalid(target.getImageHeader())) {
			return true;
		}
		return false;
	}

	public static boolean invalid(InformationDO target) {
		if (target == null) {
			return false;
		}
		if (invalid(target.getReview())) {
			return true;
		}
		if (invalid(target.getQuestionAnswer())) {
			return true;
		}
		if (invalid(target.getImageHeader())) {
			return true;
		}
		if (invalid(target.getProductMaster())) {
			return true;
		}
		return false;
	}

	public static boolean invalid(LikeDO target) {
		if (target == null) {
			return false;
		}
		if (invalid(target.getReview())) {
			return true;
		}
		if (invalid(target.getQuestionAnswer())) {
			return true;
		}
		if (invalid(target.getImageHeader())) {
			return true;
		}
		return false;
	}
	
	public static boolean invalid(VotingDO target) {
		if (target == null) {
			return false;
		}
		if (invalid(target.getReview())) {
			return true;
		}
		if (invalid(target.getQuestionAnswer())) {
			return true;
		}
		if (invalid(target.getImageHeader())) {
			return true;
		}
		return false;
	}

	public static boolean invalid(QuestionFollowDO target) {
		if (target == null) {
			return false;
		}
		if (invalid(target.getFollowQuestion())) {
			return true;
		}
		return false;
	}

	public static boolean invalid(ProductFollowDO target) {
		if (target == null) {
			return false;
		}
		if (target.getFollowProduct() == null) {
			return true;
		}
		return false;
	}

	public static boolean invalid(UsedProductDO target) {
		if (target == null) {
			return false;
		}
		if (target.getProductName() == null && target.getProduct() == null) {
			return true;
		}
		return false;
	}

	public static boolean invalid(PurchaseLostProductDO target) {
		if (target == null) {
			return false;
		}
		if (target.getProductName() == null && target.getProduct() == null) {
			return true;
		}
		return false;
	}

	public static boolean invalid(ProductMasterDO target) {
		if (target == null) {
			return false;
		}
		if (target.getVersion() != null && target.getProduct() == null) {
			return true;
		}
		return false;
	}

	public static boolean invalid(SpamReportDO target) {
		if (target == null) {
			return false;
		}
		if (target.getQuestion() != null) {
			if (invalid(target.getQuestion())) {
				return true;
			}
		}
		if (target.getQuestionAnswer() != null) {
			if (invalid(target.getQuestionAnswer())) {
				return true;
			}
		}
		if (target.getReview() != null) {
			if (invalid(target.getReview())) {
				return true;
			}
		}
		if (target.getImageHeader() != null) {
			if (invalid(target.getImageHeader())) {
				return true;
			}
		}
		if (target.getComment() != null) {
			if (invalid(target.getComment())) {
				return true;
			}
		}
		return false;
	}

}
