/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.kickmogu.lib.core.id.IDGenerator;
import com.kickmogu.lib.core.id.IDPartsDomainObjectIDGenerator;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;

/**
 * アプリケーション固有の ID 生成ロジックを提供します。
 * @author kamiike
 *
 */
public class IdUtil {

	/**
	 * ID のセパレーターです。
	 */
	public static final String ID_SEPARATOR = "-";

	/**
	 * ID の枝番のフォーマットです。
	 */
	private static final String ID_BRANCH_FORMAT = "000000000";

	/**
	 * ID の枝番の最小値です。
	 */
	private static final String ID_BRANCH_MIN = "000000000";

	/**
	 * ID の枝番の最大値です。
	 */
	private static final String ID_BRANCH_MAX = "999999999";

	/**
	 * コンストラクタをカプセル化します。
	 */
	private IdUtil() {
	}

	/**
	 * ID 連結により新しいIDを生成します。
	 * @param ids 連結するID
	 * @return 新しいID
	 */
	public static String createIdByConcatIds(String... ids) {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < ids.length; i++) {
			if (i > 0) {
				buffer.append(ID_SEPARATOR);
			}
			buffer.append(ids[i]);
		}
		return buffer.toString();
	}

	/**
	 * 指定した基本となるIDの最小枝番IDを返します。
	 * @param baseId 基本となるID
	 * @return 最小枝番ID
	 */
	public static String getMinBranchId(String baseId) {
		return createIdByConcatIds(baseId, ID_BRANCH_MIN);
	}

	/**
	 * 指定した基本となるIDの最大枝番IDを返します。
	 * @param baseId 基本となるID
	 * @return 最大枝番ID
	 */
	public static String getMaxBranchId(String baseId) {
		return createIdByConcatIds(baseId, ID_BRANCH_MAX);
	}

	/**
	 * 基本となるIDと枝番からIDを生成します。
	 * @param baseId 基本となるID
	 * @param branchNo 枝番
	 * @return 新しいID
	 */
	public static String createIdByBranchNo(String baseId, int branchNo) {
		return createIdByConcatIds(baseId,
				String.valueOf(new DecimalFormat(ID_BRANCH_FORMAT).format(branchNo)));
	}

	/**
	 * IDリストの中から一番大きい枝番を返します。
	 * @param baseId 基本となるID
	 * @param ids IDリスト
	 * @return 一番大きい枝番
	 */
	public static int getMaxBranchNo(String baseId, List<String> ids) {
		int maxBranchNo = -1;
		for (String id : ids) {
			int branchNo = Integer.parseInt(id.substring(
					baseId.length() + ID_SEPARATOR.length()));
			if (maxBranchNo < branchNo) {
				maxBranchNo = branchNo;
			}
		}
		return maxBranchNo;
	}

	/**
	 * IDから一部分を取り除いて抽出します。
	 * @param id オリジナルID
	 * @param cutTail 後ろをカットするかどうか
	 * @param idParts 不要なIDパーツ
	 * @return 切り出したID
	 */
	public static String stripId(String id, boolean cutTail, String... idParts) {
		String cutParts = createIdByConcatIds(idParts);
		if (cutTail) {
			int index = id.lastIndexOf(ID_SEPARATOR + cutParts);
			if (index < 0) {
				throw new IllegalArgumentException();
			}
			return id.substring(0, index);
		} else {
			int index = id.indexOf(cutParts + ID_SEPARATOR);
			if (index < 0) {
				throw new IllegalArgumentException();
			}
			return id.substring(index + cutParts.length() + 1);
		}
	}

	/**
	 * 日付を逆さの文字列形式に変換します。
	 * @param version バージョン
	 * @return 逆さの文字列形式
	 */
	public static String formatDate(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String src = formatter.format(date);
		StringBuilder dest = new StringBuilder();
		for (int i = src.length() - 1; i >= 0; i--) {
			dest.append(src.charAt(i));
		}
		return dest.toString();
	}

	/**
	 * バージョンを固定長の文字列形式に変換します。
	 * @param version バージョン
	 * @return バージョン文字列
	 */
	public static String formatVersion(Integer version) {
		String src = new DecimalFormat(ID_BRANCH_FORMAT).format(version);
		StringBuilder dest = new StringBuilder();
		for (int i = src.length() - 1; i >= 0; i--) {
			dest.append(src.charAt(i));
		}
		return dest.toString();
	}

	public static String withHashPrefix(String org) {
		return IDPartsDomainObjectIDGenerator.hashString(org);
	}
	
	public static String generateActionHistoryId(ActionHistoryDO actionHistory, IDGenerator<String> idGenerator){
		if(StringUtils.isNotEmpty(actionHistory.getActionHistoryId())) return actionHistory.getActionHistoryId(); 
		String actionHistoryId = null;
		
		if(actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_REVIEW)){
			actionHistoryId = IdUtil.createIdByConcatIds(actionHistory.getReview().getReviewId(), ActionHistoryType.USER_REVIEW.getCode());
		}else if(actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_QUESTION)){
			actionHistoryId = IdUtil.createIdByConcatIds(actionHistory.getQuestion().getQuestionId(), ActionHistoryType.USER_QUESTION.getCode());
		}else if(actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_ANSWER)){
			actionHistoryId = IdUtil.createIdByConcatIds(actionHistory.getQuestionAnswer().getQuestionAnswerId(), ActionHistoryType.USER_ANSWER.getCode());
		}else if(actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_IMAGE)){
			actionHistoryId = IdUtil.createIdByConcatIds(actionHistory.getImageSetId(), ActionHistoryType.USER_IMAGE.getCode());
		}else if(actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_REVIEW_COMMENT)){
			actionHistoryId = IdUtil.createIdByConcatIds(actionHistory.getComment().getCommentId(), ActionHistoryType.USER_REVIEW_COMMENT.getCode());
		}else if(actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_ANSWER_COMMENT)){
			actionHistoryId = IdUtil.createIdByConcatIds(actionHistory.getComment().getCommentId(), ActionHistoryType.USER_ANSWER_COMMENT.getCode());
		}else if(actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_IMAGE_COMMENT)){
			actionHistoryId = IdUtil.createIdByConcatIds(actionHistory.getComment().getCommentId(), ActionHistoryType.USER_IMAGE_COMMENT.getCode());
		}else if(actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_FOLLOW_USER)){
			actionHistoryId = IdUtil.createIdByConcatIds(idGenerator.generateId(),  ActionHistoryType.USER_FOLLOW_USER.getCode());
		}else if(actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_FOLLOW_PRODUCT)){
			actionHistoryId = IdUtil.createIdByConcatIds(idGenerator.generateId(), ActionHistoryType.USER_FOLLOW_PRODUCT.getCode());
		}else if(actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_FOLLOW_QUESTION)){
			actionHistoryId = IdUtil.createIdByConcatIds(idGenerator.generateId(), ActionHistoryType.USER_FOLLOW_QUESTION.getCode());
		}else if(actionHistory.getActionHistoryType().equals(ActionHistoryType.USER_PRODUCT_MASTER_RANK_CHANGE)){
			actionHistoryId = IdUtil.createIdByConcatIds(actionHistory.getProductMaster().getProductMasterId(), ActionHistoryType.USER_PRODUCT_MASTER_RANK_CHANGE.getCode());
		}else if(actionHistory.getActionHistoryType().equals(ActionHistoryType.PRODUCT_REVIEW)){
			actionHistoryId = IdUtil.createIdByConcatIds(actionHistory.getReview().getReviewId(), ActionHistoryType.PRODUCT_REVIEW.getCode());
		}else if(actionHistory.getActionHistoryType().equals(ActionHistoryType.PRODUCT_QUESTION)){
			actionHistoryId = IdUtil.createIdByConcatIds(actionHistory.getQuestion().getQuestionId(), ActionHistoryType.PRODUCT_QUESTION.getCode());
		}else if(actionHistory.getActionHistoryType().equals(ActionHistoryType.PRODUCT_ANSWER)){
			actionHistoryId = IdUtil.createIdByConcatIds(actionHistory.getQuestionAnswer().getQuestionAnswerId(), ActionHistoryType.PRODUCT_ANSWER.getCode());
		}else if(actionHistory.getActionHistoryType().equals(ActionHistoryType.PRODUCT_IMAGE)){
			actionHistoryId = IdUtil.createIdByConcatIds(actionHistory.getImageSetId(), ActionHistoryType.PRODUCT_IMAGE.getCode());
		}else if(actionHistory.getActionHistoryType().equals(ActionHistoryType.QUESTION_ANSWER)){
			actionHistoryId = IdUtil.createIdByConcatIds(actionHistory.getQuestionAnswer().getQuestionAnswerId(), ActionHistoryType.QUESTION_ANSWER.getCode());
		}
		
		if(StringUtils.isNotEmpty(actionHistoryId))
			actionHistoryId = IdUtil.withHashPrefix(actionHistoryId);
		
		return actionHistoryId;
	}
	
	public static String getInfomationId(InformationDO information, IDGenerator<String> idGenerator){
		
		if(StringUtils.isNotEmpty(information.getInformationId())) return information.getInformationId(); 

		String informationId = null;
		if(information.getInformationType().equals(InformationType.PRODUCT_MASTER_RANK_CHANGE)){
			informationId = IdUtil.createIdByConcatIds(
					information.getProductMaster().getProductMasterId()
					,StringUtils.leftPad(InformationType.PRODUCT_MASTER_RANK_CHANGE.getCode(), 2, "0"));
		}else if(information.getInformationType().equals(InformationType.REVIEW_COMMENT_ADD)){
			informationId = IdUtil.createIdByConcatIds(
					information.getComment().getCommentId()
					,StringUtils.leftPad(InformationType.REVIEW_COMMENT_ADD.getCode(), 2, "0"));
		}else if(information.getInformationType().equals(InformationType.REVIEW_LIKE_ADD)){
			informationId = IdUtil.createIdByConcatIds(
					information.getLike().getLikeId()
					,StringUtils.leftPad(InformationType.REVIEW_LIKE_ADD.getCode(), 2, "0"));
		}else if(information.getInformationType().equals(InformationType.QUESTION_ANSWER_COMMENT_ADD)){
			informationId = IdUtil.createIdByConcatIds(
					information.getComment().getCommentId()
					,StringUtils.leftPad(InformationType.QUESTION_ANSWER_COMMENT_ADD.getCode(), 2, "0"));
		}else if(information.getInformationType().equals(InformationType.QUESTION_ANSWER_LIKE_ADD)){
			informationId = IdUtil.createIdByConcatIds(
					information.getLike().getLikeId()
					,StringUtils.leftPad(InformationType.QUESTION_ANSWER_LIKE_ADD.getCode(), 2, "0"));
		}else if(information.getInformationType().equals(InformationType.IMAGE_COMMENT_ADD)){
			informationId = IdUtil.createIdByConcatIds(
					information.getComment().getCommentId()
					,StringUtils.leftPad(InformationType.IMAGE_COMMENT_ADD.getCode(), 2, "0"));
		}else if(information.getInformationType().equals(InformationType.IMAGE_LIKE_ADD)){
			informationId = IdUtil.createIdByConcatIds(
					information.getLike().getLikeId()
					,StringUtils.leftPad(InformationType.IMAGE_LIKE_ADD.getCode(), 2, "0"));
		}else if(information.getInformationType().equals(InformationType.QUESTION_ANSWER_ADD)){
			informationId = IdUtil.createIdByConcatIds(
					information.getQuestionAnswer().getQuestionAnswerId()
					,StringUtils.leftPad(InformationType.QUESTION_ANSWER_ADD.getCode(), 2, "0"));
		}else if(information.getInformationType().equals(InformationType.FOLLOW)){
			informationId = IdUtil.createIdByConcatIds(
					idGenerator.generateId()
					,StringUtils.leftPad(InformationType.FOLLOW.getCode(), 2, "0"));
		}else if(information.getInformationType().equals(InformationType.POINT_REVIEW)){
			informationId = IdUtil.createIdByConcatIds(
					information.getReview().getReviewId()
					,StringUtils.leftPad(InformationType.POINT_REVIEW.getCode(), 2, "0"));
		}else if(information.getInformationType().equals(InformationType.POINT_COMMUNITY)){
			informationId = IdUtil.createIdByConcatIds(
					idGenerator.generateId()
					,StringUtils.leftPad(InformationType.POINT_COMMUNITY.getCode(), 2, "0"));
		}else if(information.getInformationType().equals(InformationType.ACCOUNT_STOP)){
			informationId = IdUtil.createIdByConcatIds(
					idGenerator.generateId()
					,StringUtils.leftPad(InformationType.ACCOUNT_STOP.getCode(), 2, "0"));
		}else if(information.getInformationType().equals(InformationType.WELCOME)){
			informationId = IdUtil.createIdByConcatIds(
					idGenerator.generateId()
					,StringUtils.leftPad(InformationType.WELCOME.getCode(), 2, "0"));
		}else if(InformationType.REVIEW_VOTING_ADD.equals(information.getInformationType())){
			informationId = IdUtil.createIdByConcatIds(
					information.getVoting().getVotingId(),
					StringUtils.leftPad(InformationType.REVIEW_VOTING_ADD.getCode(), 2, "0"));
		}else if(InformationType.QUESTION_ANSWER_VOTING_ADD.equals(information.getInformationType())){
			informationId = IdUtil.createIdByConcatIds(
					information.getVoting().getVotingId(),
					StringUtils.leftPad(InformationType.QUESTION_ANSWER_VOTING_ADD.getCode(), 2, "0"));
		}else if(InformationType.IMAGE_VOTING_ADD.equals(information.getInformationType())){
			informationId = IdUtil.createIdByConcatIds(
					information.getVoting().getVotingId(),
					StringUtils.leftPad(InformationType.IMAGE_VOTING_ADD.getCode(), 2, "0"));
		}
		if(StringUtils.isNotEmpty(informationId))
			informationId = IdUtil.withHashPrefix(informationId);

		return informationId;
	}	
}
