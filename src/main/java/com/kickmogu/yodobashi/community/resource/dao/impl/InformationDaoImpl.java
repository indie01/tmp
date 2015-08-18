/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.common.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.id.IDGenerator;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.UpdateColumns;
import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.InformationDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.util.AdultHelper;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.ProductUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.SolrUtil;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.VotingDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.AsyncMessageType;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;
import com.kickmogu.yodobashi.community.service.annotation.SendMessage;
import com.kickmogu.yodobashi.community.service.annotation.SendMessage.Timing;

/**
 * お知らせ DAO の実装クラスです。
 * @author kamiike
 *
 */
@Service
public class InformationDaoImpl implements InformationDao {

	/**
	 * HBaseアクセサです。
	 */
	@Autowired  @Qualifier("default")
	protected HBaseOperations hBaseOperations;

	/**
	 * Solrアクセサです。
	 */
	@Autowired @Qualifier("default")
	protected SolrOperations solrOperations;

	/**
	 * タイムスタンプホルダーです。
	 */
	@Autowired
	protected TimestampHolder timestampHolder;

	/**
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	private RequestScopeDao requestScopeDao;

	@Autowired @Qualifier("default")
	private IDGenerator<String> idGenerator;

	@Autowired private CommunityUserDao communityUserDao;


	/**
	 * お知らせ情報を新規に登録します。
	 * @param information お知らせ情報
	 */
	@Override
	@ArroundHBase
	public void createInformation(InformationDO information) {
		// 有効なユーザー（有効・一時停止）のみお知らせを作成する。
		CommunityUserDO communityUser = communityUserDao.load(information.getCommunityUser().getCommunityUserId(), Path.includeProp("*"));
		if(communityUser == null || communityUser.getStatus().equals(CommunityUserStatus.INVALID) || communityUser.getStatus().equals(CommunityUserStatus.FORCE_LEAVE))
			return;

		information.setInformationId(IdUtil.getInfomationId(information, idGenerator));
		
		// InformationTime,RegisterDateTime,ModifyDateTimeが設定されていない場合は、現在時刻で設定する。
		// メンテナンスでデータを登録する場合があるため
		if( information.getInformationTime() == null )
			information.setInformationTime(DateUtils.truncate(timestampHolder.getTimestamp(), Calendar.DATE));
		if( information.getRegisterDateTime() == null )
			information.setRegisterDateTime(timestampHolder.getTimestamp());
		if( information.getModifyDateTime() == null )
			information.setModifyDateTime(timestampHolder.getTimestamp());

		hBaseOperations.save(information);
	}

	/**
	 * お知らせ情報のインデックスを更新します。
	 * @param informationId お知らせ情報ID
	 */
	@Override
	@ArroundHBase
	@ArroundSolr
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS, asyncMessageType=AsyncMessageType.INFORMATION)
	public void updateInformationInIndex(String informationId) {
		// 有効なユーザー（有効・一時停止）のみお知らせを作成する。
		if(StringUtils.isEmpty(informationId))
			return;

		InformationDO information = hBaseOperations.load(
				InformationDO.class, informationId);
		if (information != null && !information.isDeleted()) {
			solrOperations.save(information);
		} else {
			solrOperations.deleteByKey(InformationDO.class, informationId);
		}
	}

	/**
	 * お知らせ情報のインデックスを更新します。
	 * @param informationId お知らせ情報ID
	 */
	@Override
	@ArroundHBase
	@ArroundSolr
	@SendMessage(timing=Timing.ASYNC_AFTER_PROCESS, asyncMessageType=AsyncMessageType.INFORMATION)
	public void updateInformationInIndex(InformationDO information) {
		if( information == null )
			throw new IllegalArgumentException("InformationDO argument is null");
		// 有効なユーザー（有効・一時停止）のみお知らせを作成する。
		if(StringUtils.isEmpty(information.getInformationId()))
			return;

		if (!information.isDeleted()) {
			solrOperations.save(information);
		} else {
			solrOperations.deleteByKey(InformationDO.class, information.getInformationId());
		}
	}
	
	/**
	 * 指定したコミュニティユーザーに対するお知らせ情報で未読のカウントを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @return 未読カウント
	 */
	@Override
	public long countNoRead(String communityUserId) {
		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());
		StringBuilder buffer = new StringBuilder();
		buffer.append("communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		buffer.append(" AND readFlag_b:false");

		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(buffer.toString()));
		query.setRows(0);

		return solrOperations.findByQuery(query,
				InformationDO.class, Path.includeProp("informationId")).getNumFound();
	}
	/**
	 * 指定したコミュニティユーザーに対するお知らせ情報のカウントを返します。ｓ
	 * @param communityUserId
	 * @return
	 */
	@Override
	public long count(String communityUserId) {
		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());
		StringBuilder buffer = new StringBuilder();
		buffer.append("communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));

		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(buffer.toString()));
		query.setRows(0);

		return solrOperations.findByQuery(query,
				InformationDO.class, Path.includeProp("informationId")).getNumFound();
	}

	/**
	 * 指定したコミュニティユーザーの全てのお知らせ情報を既読に更新します。
	 * @return informationIds お知らせ情報IDのリスト
	 */
	@Override
	@ArroundHBase
	public List<String> updateInformationForRead(String communityUserId) {
		return hBaseOperations.scanUpdateWithIndexReturningKeys(
				InformationDO.class,
				"communityUserId",
				communityUserId,
				UpdateColumns.set("readFlag", true).andSet("readDate", timestampHolder.getTimestamp()),
				String.class);
	}

	@Override
	@ArroundHBase
	public void updateInformationForRead(List<InformationDO> informations) {
		for( InformationDO information : informations){
			information.setReadDate(timestampHolder.getTimestamp());
			information.setReadFlag(true);
			hBaseOperations.save(information, Path.includePath("readDate,readFlag,modifyDateTime"));
		}
	}

	/**
	 * 指定したコミュニティユーザーに対する未読お知らせ情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offset 検索開始位置
	 * @return 検索結果
	 */
	@Override
	public SearchResult<InformationDO> findNoReadInformationByCommunityUserId(
			String communityUserId, int limit, int offset) {

		//hasAdult対応対象です。
		AdultHelper adultHelper = new AdultHelper(requestScopeDao.loadAdultVerification());
		StringBuilder buffer = new StringBuilder();
		buffer.append("communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		
		Date now = new Date();
		String startDateTime = DateUtil.getThreadLocalDateFormat().format(DateUtils.addDays(now, -1));
		String endDateTime = DateUtil.getThreadLocalDateFormat().format(now);
		
		buffer.append(" AND (readFlag_b:false OR (readFlag_b:true AND readDate_dt:[" + startDateTime + " TO " + endDateTime + "]))");
		
		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(buffer.toString()));
		query.addSortField("registerDateTime_dt", ORDER.desc);
		query.setRows(limit);
		query.setStart(offset);

		SearchResult<InformationDO> result = new SearchResult<InformationDO>(
				solrOperations.findByQuery(query,
				InformationDO.class, getDefaultLoadCondition()));
		loadHbaseContents(result);

		if (adultHelper.isRequireCheckAdult()) {
			result.setHasAdult(
					adultHelper.hasAdult(
							query.toString(), InformationDO.class, solrOperations));
		}
		ProductUtil.filterInvalidProduct(result);
		// 未読カウント
		if( result.getDocuments() != null && !result.getDocuments().isEmpty()){
			result.setNumFound(0);
			for( InformationDO information : result.getDocuments()){
				if( !information.isReadFlag() ){
					result.setNumFound(result.getNumFound()+ 1);
				}
			}
		}
		
		return result;
	}

	/**
	 * 指定したコミュニティユーザーに対するお知らせ情報を返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param limit 最大取得件数
	 * @param offsetTime 検索開始日時
	 * @param previous より前を取得する場合、true
	 * @return 検索結果
	 */
	@Override
	public SearchResult<InformationDO> findByCommunityUserId(
			String communityUserId, int limit, Date offsetTime, boolean previous, boolean excludeProduct) {

		//hasAdult対応対象です。
		AdultHelper adultHelper = new AdultHelper(
				requestScopeDao.loadAdultVerification());
		StringBuilder buffer = new StringBuilder();
		buffer.append("communityUserId_s:");
		buffer.append(SolrUtil.escape(communityUserId));
		if (offsetTime != null) {
			if (previous) {
				buffer.append(" AND registerDateTime_dt:{" +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + " TO *}");
			} else {
				buffer.append(" AND registerDateTime_dt:{* TO " +
						DateUtil.getThreadLocalDateFormat().format(offsetTime) + "}");
			}
		}

		SolrQuery query = new SolrQuery(adultHelper.toFilterQuery(
				buffer.toString()));
		if (offsetTime == null || !previous) {
			query.setSortField("registerDateTime_dt", ORDER.desc);
		} else {
			query.setSortField("registerDateTime_dt", ORDER.asc);
		}
		query.setRows(limit);
		
		Condition condition = getDefaultLoadCondition();
		if(excludeProduct){
			condition = getExcludeProductLoadCondition();
		}

		SearchResult<InformationDO> result = new SearchResult<InformationDO>(
				solrOperations.findByQuery(query,
				InformationDO.class, condition));
		loadHbaseContents(result);
		if (adultHelper.isRequireCheckAdult()) {
			result.setHasAdult(
					adultHelper.hasAdult(
							buffer.toString(), InformationDO.class, solrOperations));
		}
		
		if(!excludeProduct) {
			ProductUtil.filterInvalidProduct(result);
		}

		if (offsetTime == null || !previous) {
			return result;
		} else {
			Collections.reverse(result.getDocuments());
			return result;
		}
	}

	/**
	 * お知らせ情報を読み出すデフォルト条件を返します。
	 * @return お知らせ情報の読み出し条件
	 */
	private Condition getDefaultLoadCondition() {
		return Path.includeProp("*").includePath("product.sku," +
				"review.product.sku," +
				"question.product.sku," +
				"questionAnswer.question.product.sku," +
				"questionAnswer.communityUser.communityUserId," +
				"productMaster.product.sku," +
				"like.likeId," +
				"like.communityUser.communityUserId," +
				"voting.votingId," +
				"voting.communityUser.communityUserId," +
				"imageHeader.imageId," +
				"comment.commentId," +
				"comment.communityUser.communityUserId," +
				"followerCommunityUser.imageHeader.imageId").depth(4);
	}

	/**
	 * お知らせ情報を読み出すデフォルト条件を返します。
	 * @return お知らせ情報の読み出し条件
	 */
	private Condition getExcludeProductLoadCondition() {
		return Path.includeProp("*").includePath(
				"questionAnswer.communityUser.communityUserId," +
				"like.likeId," +
				"like.communityUser.communityUserId," +
				"voting.votingId," +
				"voting.communityUser.communityUserId," +
				"imageHeader.imageId," +
				"comment.commentId," +
				"comment.communityUser.communityUserId," +
				"followerCommunityUser.imageHeader.imageId").depth(4);
	}


	private void loadHbaseContents(SearchResult<InformationDO> searchResult){
		if(searchResult == null || searchResult.getDocuments() == null || searchResult.getDocuments().isEmpty()) return;
		Set<String> reviewIds = new HashSet<String>();
		Set<String> questionIds = new HashSet<String>();
		Set<String> questionAnswerIds = new HashSet<String>();
		Set<String> imageHeaderIds = new HashSet<String>();
		Set<String> productMasterIds = new HashSet<String>();
		Set<String> likesIds = new HashSet<String>();
		Set<String> votingsIds = new HashSet<String>();
		Set<String> commentsIds = new HashSet<String>();

		for(InformationDO information: searchResult.getDocuments()){
			if(information.getReview() != null && StringUtils.isNotEmpty(information.getReview().getReviewId()) && information.getReview().getRegisterDateTime() == null){
				reviewIds.add(information.getReview().getReviewId());
			}
			if(information.getQuestion() != null && StringUtils.isNotEmpty(information.getQuestion().getQuestionId()) && information.getQuestion().getRegisterDateTime() == null){
				questionIds.add(information.getQuestion().getQuestionId());
			}
			if(information.getQuestionAnswer() != null && StringUtils.isNotEmpty(information.getQuestionAnswer().getQuestionAnswerId()) && information.getQuestionAnswer().getRegisterDateTime() == null){
				questionAnswerIds.add(information.getQuestionAnswer().getQuestionAnswerId());
			}
			if(information.getImageHeader() != null && StringUtils.isNotEmpty(information.getImageHeader().getImageId()) && information.getImageHeader().getRegisterDateTime() == null){
				imageHeaderIds.add(information.getImageHeader().getImageId());
			}
			if(information.getProductMaster() != null && StringUtils.isNotEmpty(information.getProductMaster().getProductMasterId()) && information.getProductMaster().getRegisterDateTime() == null){
				productMasterIds.add(information.getProductMaster().getProductMasterId());
			}
			if(information.getLike() != null && StringUtils.isNotEmpty(information.getLike().getLikeId()) && information.getLike().getRegisterDateTime() == null){
				likesIds.add(information.getLike().getLikeId());
			}
			if(information.getVoting() != null && StringUtils.isNotEmpty(information.getVoting().getVotingId()) && information.getVoting().getRegisterDateTime() == null){
				votingsIds.add(information.getVoting().getVotingId());
			}
			if(information.getComment() != null && StringUtils.isNotEmpty(information.getComment().getCommentId()) && information.getComment().getRegisterDateTime() == null){
				commentsIds.add(information.getComment().getCommentId());
			}
		}

		Map<String, ReviewDO> reviewsMap = null;
		Map<String, QuestionDO> questionsMap = null;
		Map<String, QuestionAnswerDO> questionAnswersMap = null;
		Map<String, ImageHeaderDO> imageHeadersMap = null;
		Map<String, ProductMasterDO> productMatersMap = null;
		Map<String, LikeDO> likesMap  = null;
		Map<String, VotingDO> votingsMap = null;
		Map<String, CommentDO> commentsMap  = null;

		if(!reviewIds.isEmpty())
			reviewsMap = hBaseOperations.find(ReviewDO.class, String.class, reviewIds);
		if(!questionIds.isEmpty())
			questionsMap = hBaseOperations.find(QuestionDO.class, String.class, questionIds);
		if(!questionAnswerIds.isEmpty())
			questionAnswersMap = hBaseOperations.find(QuestionAnswerDO.class, String.class, questionAnswerIds);
		if(!imageHeaderIds.isEmpty())
			imageHeadersMap = hBaseOperations.find(ImageHeaderDO.class, String.class, imageHeaderIds);
		if(!productMasterIds.isEmpty())
			productMatersMap = hBaseOperations.find(ProductMasterDO.class, String.class, productMasterIds);
		if(!likesIds.isEmpty())
			likesMap = hBaseOperations.find(LikeDO.class, String.class, likesIds);
		if(!votingsIds.isEmpty())
			votingsMap = hBaseOperations.find(VotingDO.class, String.class, votingsIds);
		if(!commentsIds.isEmpty())
			commentsMap = hBaseOperations.find(CommentDO.class, String.class, commentsIds);

		for(InformationDO information: searchResult.getDocuments()){
			if(reviewsMap != null && information.getReview() != null && StringUtils.isNotEmpty(information.getReview().getReviewId()) && information.getReview().getRegisterDateTime() == null){
				information.setReview(reviewsMap.get(information.getReview().getReviewId()));
			}
			if(questionsMap != null && information.getQuestion() != null && StringUtils.isNotEmpty(information.getQuestion().getQuestionId()) && information.getQuestion().getRegisterDateTime() == null){
				information.setQuestion(questionsMap.get(information.getQuestion().getQuestionId()));
			}
			if(questionAnswersMap != null && information.getQuestionAnswer() != null && StringUtils.isNotEmpty(information.getQuestionAnswer().getQuestionAnswerId()) && information.getQuestionAnswer().getRegisterDateTime() == null){
				information.setQuestionAnswer(questionAnswersMap.get(information.getQuestionAnswer().getQuestionAnswerId()));
			}
			if(imageHeadersMap != null && information.getImageHeader() != null && StringUtils.isNotEmpty(information.getImageHeader().getImageId()) && information.getImageHeader().getRegisterDateTime() == null){
				information.setImageHeader(imageHeadersMap.get(information.getImageHeader().getImageId()));
			}
			if(productMatersMap != null && information.getProductMaster() != null && StringUtils.isNotEmpty(information.getProductMaster().getProductMasterId()) && information.getProductMaster().getRegisterDateTime() == null){
				information.setProductMaster(productMatersMap.get(information.getProductMaster().getProductMasterId()));
			}
			if(likesMap != null && information.getLike() != null && StringUtils.isNotEmpty(information.getLike().getLikeId()) && information.getLike().getRegisterDateTime() == null){
				information.setLike(likesMap.get(information.getLike().getLikeId()));
			}
			if(votingsMap != null && information.getVoting() != null && StringUtils.isNotEmpty(information.getVoting().getVotingId()) && information.getVoting().getRegisterDateTime() == null){
				information.setVoting(votingsMap.get(information.getVoting().getVotingId()));
			}
			if(commentsMap != null && information.getComment() != null && StringUtils.isNotEmpty(information.getComment().getCommentId()) && information.getComment().getRegisterDateTime() == null){
				information.setComment(commentsMap.get(information.getComment().getCommentId()));
			}
		}
	}

	@Override
	public List<InformationDO> findInformationByType(
			String communityUserId, InformationType type) {
		return hBaseOperations.findWithIndex(InformationDO.class, "communityUserId",
				hBaseOperations.createFilterBuilder(InformationDO.class
						).appendSingleColumnValueFilter("informationType", CompareOp.EQUAL, type).toFilter()
				, communityUserId);
	}

	@Override
	public void deleteInformation(String informationId) {
		hBaseOperations.deleteByKey(InformationDO.class, informationId);
	}
}
