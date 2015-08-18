/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.id.IDGenerator;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.resource.Path.Condition;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.UpdateColumns;
import com.kickmogu.lib.solr.FacetResult;
import com.kickmogu.lib.solr.SolrConstants;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.resource.cache.CacheStrategyType;
import com.kickmogu.yodobashi.community.resource.cache.MethodCache;
import com.kickmogu.yodobashi.community.resource.cache.TargetSystemType;
import com.kickmogu.yodobashi.community.resource.config.DomainConfig;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.ImageCacheDao;
import com.kickmogu.yodobashi.community.resource.dao.NormalizeCharDao;
import com.kickmogu.yodobashi.community.resource.dao.OuterCustomerDao;
import com.kickmogu.yodobashi.community.resource.dao.RequestScopeDao;
import com.kickmogu.yodobashi.community.resource.dao.SimplePmsDao;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.dao.util.SolrUtil;
import com.kickmogu.yodobashi.community.resource.domain.AccountSharingDO;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.AnnounceDO;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityNameDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.HashCommunityIdDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.LikeDO;
import com.kickmogu.yodobashi.community.resource.domain.MailSettingDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductMasterDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseLostProductDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionFollowDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDecisivePurchaseDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.SocialMediaSettingDO;
import com.kickmogu.yodobashi.community.resource.domain.SpamReportDO;
import com.kickmogu.yodobashi.community.resource.domain.SpoofingNameDO;
import com.kickmogu.yodobashi.community.resource.domain.StoppableContents;
import com.kickmogu.yodobashi.community.resource.domain.UsedProductDO;
import com.kickmogu.yodobashi.community.resource.domain.VotingDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.CancelPointGrantType;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommentTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageDeleteResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageSyncStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;
import com.kickmogu.yodobashi.community.resource.domain.constants.LikeTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportGroupType;
import com.kickmogu.yodobashi.community.resource.domain.constants.SpamReportTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.VotingTargetType;

/**
 * コミュニティユーザーの DAO 実装です。
 * @author kamiike
 *
 */
@Service
public class CommunityUserDaoImpl implements CommunityUserDao {

	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger LOG = LoggerFactory.getLogger(CommunityUserDaoImpl.class);

	/**
	 * HBaseアクセサです。
	 */
	@Autowired @Qualifier("default")
	private HBaseOperations hBaseOperations;

	/**
	 * Solrアクセサです。
	 */
	@Autowired @Qualifier("default")
	private SolrOperations solrOperations;

	/**
	 * タイムスタンプホルダーです。
	 */
	@Autowired
	private TimestampHolder timestampHolder;

	/**
	 * 外部顧客情報 DAO です。
	 */
	@Autowired @Qualifier("xi")
	private OuterCustomerDao outerCustomerDao;

	/**
	 * ポイント管理システム DAO です。
	 */
	@Autowired @Qualifier("pms")
	private SimplePmsDao simplePmsDao;

	/**
	 * 画像キャッシュ DAO です。
	 */
	@Autowired
	private NormalizeCharDao normalizeCharDao;

	/**
	 * 画像キャッシュ DAO です。
	 */
	@Autowired
	private ImageCacheDao imageCacheDao;

	/**
	 * リクエストスコープ DAO です。
	 */
	@Autowired
	private RequestScopeDao requestScopeDao;

	/**
	 * リソースコンフィグです。
	 */
	@Autowired
	private ResourceConfig resourceConfig;

	/**
	 * ドメインコンフィグです。
	 */
	@Autowired
	private DomainConfig domainConfig;

	/**
	 * IDジェネレーターです。
	 */
	@Autowired @Qualifier("default")
	private IDGenerator<String> idGenerator;

	/**
	 * コミュニティユーザーIDを生成します。
	 * @return コミュニティユーザーID
	 */
	@Override
	public String issueCommunityUserId() {
		return idGenerator.generateId();
	}

	/**
	 * 指定したコミュニティユーザーIDのコミュニティユーザー情報を取得します。
	 * @param hashCommunityId ハッシュ化されたコミュニティID
	 * @param path 取得情報
	 * @param withLock ロックを取得するかどうか
	 * @param statusSync ステータス情報を同期するかどうか
	 * @return コミュニティユーザー情報
	 */
	@Override
	public CommunityUserDO loadByHashCommunityId(
			String hashCommunityId,
			Condition path,
			boolean withLock,
			boolean statusSync) {
		HashCommunityIdDO id = hBaseOperations.load(HashCommunityIdDO.class, hashCommunityId);
		CommunityUserDO communityUser = null;
		if (id != null) {
			if (withLock) {
				communityUser = hBaseOperations.loadWithLock(CommunityUserDO.class, id.getCommunityUserId(),
						path);
			} else {
				communityUser = hBaseOperations.load(CommunityUserDO.class, id.getCommunityUserId(),
						path);
			}
		}
		if (communityUser != null && statusSync) {
			communityUser.setStatus(outerCustomerDao.loadCommunityUserStatusByOuterCustomerId(
					communityUser.getCommunityId()));
		}
		return communityUser;
	}

	/**
	 * 指定した標準化されたコミュニティ名のコミュニティユーザーIDを返します。
	 * @param normalizeCommunityName 標準化されたコミュニティ名
	 * @return コミュニティユーザーID
	 */
	@Override
	public String loadCommunityUserIdByNormalizeCommunityName(String normalizeCommunityName) {
		CommunityNameDO name = hBaseOperations.load(CommunityNameDO.class, normalizeCommunityName);
		// TODO 一度利用されたニックネームで逆引きできるのは仕様として許容しているのか？
		if (name != null && StringUtils.isNotEmpty(name.getCommunityUserId()) && !name.isDeleteFlag()) {
			CommunityUserDO communityUser = hBaseOperations.load(CommunityUserDO.class,
					name.getCommunityUserId(), Path.includePath("status"));
			//退会しているかの判別は呼び出し元でするため、ここでは判定しません。
			if (communityUser != null) {
				return name.getCommunityUserId();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * コミュニティユーザーを新規登録します。
	 * @param communityUser コミュニティユーザー
	 */
	@Override
	public void createCommunityUser(CommunityUserDO communityUser, SpoofingNameDO spoofingName) {
		communityUser.setRegisterDateTime(timestampHolder.getTimestamp());
		communityUser.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(communityUser,
				Path.includeProp("*").excludeProp("normalizeCommunityName"));

		CommunityNameDO communityName = new CommunityNameDO();
		communityName.setNormalizeCommunityName(
				communityUser.getNormalizeCommunityName());
		communityName.setCommunityUserId(communityUser.getCommunityUserId());
		hBaseOperations.save(communityName,
				Path.includeProp("normalizeCommunityName,communityUserId"));

		hBaseOperations.save(communityUser,
				Path.includeProp("normalizeCommunityName"));
		if(StringUtils.isNotEmpty(spoofingName.getSpoofingPattern()))
			hBaseOperations.save(spoofingName);
	}

	/**
	 * 指定したIDのコミュニティユーザー情報のインデックスを更新します。
	 * @param communityUserId コミュニティユーザー情報ID
	 */
	@Override
	public void updateCommunityUserInIndex(String communityUserId) {
		CommunityUserDO communityUser = hBaseOperations.load(
				CommunityUserDO.class, communityUserId);
		if (communityUser != null) {
			solrOperations.save(communityUser);
		}
	}

	/**
	 * 自分以外の登録者で、標準化されたニックネームが存在するかどうかチェックします。
	 * @param communityUserId コミュニティユーザー情報ID
	 * @param communityName ニックネーム
	 * @param normalizeCommunityName 標準化されたニックネーム
	 * @param withLock ロックを取得するかどうか
	 * @return ニックネームが存在する場合、true
	 */
	@Override
	public boolean existsNormalizeCommunityName(
			String communityUserId,
			String communityName,
			String normalizeCommunityName,
			boolean withLock) {
		CommunityNameDO name = null;

		if(StringUtils.isEmpty(normalizeCommunityName)) return true;
		if (withLock) {
			name = hBaseOperations.loadWithLock(CommunityNameDO.class, normalizeCommunityName);
		} else {
			name = hBaseOperations.load(CommunityNameDO.class, normalizeCommunityName);
		}
		if (name == null){
			String spoofingPattern = normalizeCharDao.getSpoofingPattern(normalizeCommunityName);
			return !normalizeCharDao.validateSpoofingPattern(spoofingPattern, false);
		}else if (communityUserId != null && !name.isDeleteFlag(
				) && name.getCommunityUserId().equals(communityUserId)) {
			return false;
		} else {
			return true;
		}

	}

	/**
	 * 自分以外の登録者で、標準化されたニックネームが存在するかどうかチェックします。
	 * @param icOuterCustomerId IC外部顧客ID
	 * @param communityName ニックネーム
	 * @param normalizeCommunityName 標準化されたニックネーム
	 * @param withLock ロックを取得するかどうか
	 * @return ニックネームが存在する場合、true
	 */
	@Override
	public boolean existsNormalizeCommunityNameForCreate(
			String icOuterCustomerId,
			String communityName,
			String normalizeCommunityName,
			boolean withLock) {
		CommunityNameDO name = null;
		if(StringUtils.isEmpty(normalizeCommunityName)) return true;

		if (withLock) {
			name = hBaseOperations.loadWithLock(CommunityNameDO.class, normalizeCommunityName);
		} else {
			name = hBaseOperations.load(CommunityNameDO.class, normalizeCommunityName);
		}
		if (name == null){
			String spoofingPattern = normalizeCharDao.getSpoofingPattern(normalizeCommunityName);
			return !normalizeCharDao.validateSpoofingPattern(spoofingPattern, false);
		}else if ((icOuterCustomerId != null && !name.isDeleteFlag(
				) && name.getOuterCustomerId().equals(icOuterCustomerId))) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public List<CommunityUserDO> find(List<String> communityUserIds, Condition path){
		List<CommunityUserDO> result = new ArrayList<CommunityUserDO>();
		Map<String, CommunityUserDO> users = hBaseOperations.find(CommunityUserDO.class, String.class, communityUserIds, path);

		if( users.isEmpty() )
			return result;

		Set<Entry<String, CommunityUserDO>> entrySet = users.entrySet();
		Iterator<Entry<String, CommunityUserDO>> entryIterator = entrySet.iterator();

		while(entryIterator.hasNext()){
			Map.Entry<String, CommunityUserDO> entry = entryIterator.next();
			result.add(entry.getValue());
		}

		return result;
	}

	/**
	 * ニックネームを更新します。
	 * @param communityUser コミュニティユーザー
	 * @param communityNameMergeDone ニックネームマージ済み
	 */
	@Override
	public void updateCommunityName(CommunityUserDO communityUser, boolean communityNameMergeDone) {
		doUpdateCommunityName(communityUser, null, communityNameMergeDone);
	}

	/**
	 * ニックネームを更新します。
	 * @param communityUser コミュニティユーザー
	 * @param spoofingName なりすまし判定
	 * @param communityNameMergeDone ニックネームマージ済み
	 */
	@Override
	public void updateCommunityName(CommunityUserDO communityUser, SpoofingNameDO spoofingName, boolean communityNameMergeDone) {
		doUpdateCommunityName(communityUser, spoofingName, communityNameMergeDone);
	}

	/**
	 * ニックネームを更新します。
	 * @param communityUser コミュニティユーザー
	 * @param spoofingName なりすまし判定
	 * @param communityNameMergeDone ニックネームマージ済み
	 */
	private void doUpdateCommunityName(
			CommunityUserDO communityUser,
			SpoofingNameDO spoofingName,
			boolean communityNameMergeDone) {
		String oldNormalizeCommunityName = hBaseOperations.load(CommunityUserDO.class,
				communityUser.getCommunityUserId(),
				Path.includeProp("normalizeCommunityName")).getNormalizeCommunityName();
		communityUser.setModifyDateTime(timestampHolder.getTimestamp());
		if (oldNormalizeCommunityName.equals(communityUser.getNormalizeCommunityName())) {
			if (communityNameMergeDone) {
				hBaseOperations.save(communityUser,
						Path.includeProp("communityNameMergeRequired,modifyDateTime"));
				return;
			}
			throw new IllegalStateException();
		}
		if (communityNameMergeDone) {
			CommunityNameDO name = hBaseOperations.load(CommunityNameDO.class,
					communityUser.getNormalizeCommunityName());
			if (name != null
					&& StringUtils.isEmpty(name.getCommunityUserId())) {
				name.setCommunityUserId(communityUser.getCommunityUserId());
				hBaseOperations.save(name, Path.includeProp("communityUserId"));
			}
		}
		StringBuilder prop = new StringBuilder();
		prop.append("communityName,normalizeCommunityName,modifyDateTime");
		if (communityNameMergeDone) {
			prop.append(",communityNameMergeRequired");
		}
		hBaseOperations.save(communityUser,
				Path.includeProp(prop.toString()));

		//更新後、古いニックネームを削除済みに更新します。
		CommunityNameDO name = new CommunityNameDO();
		name.setNormalizeCommunityName(oldNormalizeCommunityName);
		name.setDeleteFlag(true);
		name.setDeleteDate(timestampHolder.getTimestamp());
		hBaseOperations.save(name, Path.includeProp("deleteFlag,deleteDate"));

		if(spoofingName != null && StringUtils.isNotEmpty(spoofingName.getSpoofingPattern()))
			hBaseOperations.save(spoofingName);
	}

	/**
	 * プロフィール画像を更新します。
	 * @param communityUser コミュニティユーザー
	 */
	@Override
	public void updateProfileImage(
			CommunityUserDO communityUser) {
		communityUser.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(communityUser, Path.includeProp(
				"imageHeaderId,thumbnailId,profileImageUrl," +
				"thumbnailImageUrl,modifyDateTime"));
	}

	/**
	 * コミュニティユーザーのステータスを更新します。
	 * @param communityUserId コミュニティユーザー情報ID
	 * @param communityId コミュニティID
	 * @param status ステータス
	 * @param keepContents 任意退会の場合、コンテンツを保持するかの選択
	 * @return 退会した場合、退会キー
	 */
	@Override
	public String updateCommunityUserStatus(
			String communityUserId,
			String communityId,
			CommunityUserStatus status,
			boolean reviewDelete,
			boolean qaDelete,
			boolean imageDelete,
			boolean commentDelete) {
		outerCustomerDao.updateCustomerIdStatus(communityId, status);
		CommunityUserDO communityUser = new CommunityUserDO();
		communityUser.setCommunityUserId(communityUserId);
		communityUser.setStatus(status);
		communityUser.setModifyDateTime(timestampHolder.getTimestamp());
		communityUser.setWithdrawLock(false);
		communityUser.setKeepReviewContents(!reviewDelete);
		communityUser.setKeepQuestionContents(!qaDelete);
		communityUser.setKeepImageContents(!imageDelete);
		communityUser.setKeepCommentContents(!commentDelete);

		String withdrawKey = null;
		if (status.equals(CommunityUserStatus.INVALID) ||
				status.equals(CommunityUserStatus.FORCE_LEAVE)) {
			withdrawKey = IdUtil.createIdByConcatIds(communityUserId,
					String.valueOf(timestampHolder.getTimestamp().getTime()));
			communityUser.setWithdrawLock(true);
		}
		communityUser.setWithdrawKey(withdrawKey);
		hBaseOperations.save(communityUser,
				Path.includeProp("status," +
						"modifyDateTime,withdrawKey,withdrawLock,keepReviewContents,keepQuestionContents,keepImageContents,keepCommentContents"));
		return withdrawKey;
	}

	/**
	 * 設定情報を変更します。
	 * @param communityUser コミュニティユーザー
	 */
	@Override
	public void updateSetting(
			CommunityUserDO communityUser) {
		communityUser.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(communityUser,
				Path.includeProp("secureAccess,adultVerification,ceroVerification,modifyDateTime"));
	}

	/**
	 * HTTP・HTTPSアクセス制御を変更します。
	 * @param communityUser コミュニティユーザー
	 */
	@Override
	public void updateSecureAccess(
			CommunityUserDO communityUser) {
		communityUser.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(communityUser,
				Path.includeProp("secureAccess,modifyDateTime"));
	}

	/**
	 * アダルト商品表示確認ステータスを変更します。
	 * @param communityUser コミュニティユーザー
	 */
	@Override
	public void updateAdultVerification(
			CommunityUserDO communityUser) {
		communityUser.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(communityUser,
				Path.includeProp("adultVerification,modifyDateTime"));
	}

	/**
	 * CERO商品表示確認ステータスを変更します。
	 * @param communityUser コミュニティユーザー
	 */
	@Override
	public void updateCeroVerification(
			CommunityUserDO communityUser) {
		communityUser.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(communityUser,
				Path.includeProp("ceroVerification,modifyDateTime"));
	}

	/**
	 * コミュニティユーザー情報を取得します。
	 * @param communityUserId コミュニティユーザー情報ID
	 * @param path 取得情報
	 * @return コミュニティユーザー情報
	 */
	@Override
	public CommunityUserDO load(String communityUserId, Condition path) {
		return hBaseOperations.load(CommunityUserDO.class, communityUserId, path);
	}

	/**
	 * コミュニティユーザー情報をインデックスから取得します。
	 * @param communityUserId コミュニティユーザー情報ID
	 * @param path 取得情報
	 * @return コミュニティユーザー情報
	 */
	@Override
	public CommunityUserDO loadFromIndex(String communityUserId, Condition path) {
		return solrOperations.load(CommunityUserDO.class, communityUserId, path);
	}

	/**
	 * コミュニティユーザー情報マップを返します。
	 * @param communityUserIds コミュニティユーザーIDリスト
	 * @return コミュニティユーザー情報マップ
	 */
	@Override
	public Map<String, CommunityUserDO> loadCommunityUserMap(List<String> communityUserIds) {
		if (communityUserIds != null && !communityUserIds.isEmpty()) {
			return solrOperations.find(CommunityUserDO.class, String.class, communityUserIds);
		} else {
			return new HashMap<String,CommunityUserDO>();
		}
		
	}

	/**
	 * コミュニティユーザー情報を取得し、ロックします。
	 * @param communityUserId コミュニティユーザー情報ID
	 * @param path 取得情報
	 * @param statusSync ステータス情報を同期するかどうか
	 * @return コミュニティユーザー情報
	 */
	@Override
	public CommunityUserDO loadWithLock(
			String communityUserId,
			Condition path,
			boolean statusSync) {
		CommunityUserDO communityUser = hBaseOperations.loadWithLock(CommunityUserDO.class, communityUserId, path);
		if (communityUser != null && statusSync) {
			communityUser.setStatus(outerCustomerDao.loadCommunityUserStatusByOuterCustomerId(
					communityUser.getCommunityId()));
		}
		return communityUser;
	}

	/**
	 * フォローユーザーを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @return フォローユーザー
	 */
	@Override
	public SearchResult<CommunityUserFollowDO> findFollowCommunityUserForSuggest(
			String communityUserId) {
		SolrQuery baseQuery = new SolrQuery("communityUserId_s:" + SolrUtil.escape(communityUserId)
				).setRows(
						SolrConstants.QUERY_ROW_LIMIT);
		SearchResult<CommunityUserFollowDO> followCommunityUsers
				= new SearchResult<CommunityUserFollowDO>(
						solrOperations.findByQuery(
		baseQuery, CommunityUserFollowDO.class,
		Path.includeProp("followCommunityUserId")));
		return followCommunityUsers;
	}


	/**
	 * 指定したコミュニティユーザーと共通のフォローユーザーを持った、非フォローユーザー
	 * マップを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param excludeQuery フォローユーザーの除外条件
	 * @param followCommunityUsers フォローユーザーリスト
	 * @return ユーザーマップ
	 */
	@Override
	public Map<String, Long> loadCommonFollowUserScoresForSuggest(
			String communityUserId,
			String excludeQuery,
			SearchResult<CommunityUserFollowDO> followCommunityUsers) {
		Map<String, Long> followUserScores = new HashMap<String, Long>();
		if (followCommunityUsers.getNumFound() > 0) {
			StringBuilder subQuery = new StringBuilder();
			subQuery.append(excludeQuery);
			subQuery.append(" AND (");
			for (int i = 0; i < followCommunityUsers.getDocuments().size(); i++) {
				if (i > 0) {
					subQuery.append(" OR ");
				}
				subQuery.append("followCommunityUserId_s:");
				subQuery.append(SolrUtil.escape(followCommunityUsers.getDocuments(
						).get(i).getFollowCommunityUser().getCommunityUserId()));
			}
			subQuery.append(")");
			for (FacetResult<String> facetResult : solrOperations.facet(
					CommunityUserFollowDO.class,
					String.class,
					new SolrQuery(subQuery.toString()
							).addFacetField("communityUserId_s"
									).setFacetMinCount(1).setFacetSort(
											FacetParams.FACET_SORT_COUNT))) {
				followUserScores.put(
						facetResult.getValue(), facetResult.getCount());
			}
		}
		return followUserScores;
	}


	/**
	 * 指定したコミュニティユーザーと共通のフォロー商品を持った、非フォローユーザー
	 * マップを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param excludeQuery フォローユーザーの除外条件
	 * @return ユーザーマップ
	 */
	@Override
	public Map<String, Long> loadCommonFollowProductScoresForSuggest(
			String communityUserId,
			String excludeQuery) {
		SearchResult<ProductFollowDO> followProducts
				= new SearchResult<ProductFollowDO>(
						solrOperations.findByQuery(
								new SolrQuery(
								"communityUserId_s:" + SolrUtil.escape(communityUserId)).setRows(
								SolrConstants.QUERY_ROW_LIMIT), ProductFollowDO.class,
				Path.includeProp("followProductId")));
		Map<String, Long> followProductScores = new HashMap<String, Long>();
		if (followProducts.getNumFound() > 0) {
			StringBuilder subQuery = new StringBuilder();
			subQuery.append(excludeQuery);
			subQuery.append(" AND (");
			for (int i = 0; i < followProducts.getDocuments().size(); i++) {
				if (i > 0) {
					subQuery.append(" OR ");
				}
				subQuery.append("followProductId_s:");
				subQuery.append(SolrUtil.escape(followProducts.getDocuments(
						).get(i).getFollowProduct().getSku()));
			}
			subQuery.append(")");
			for (FacetResult<String> facetResult : solrOperations.facet(
					ProductFollowDO.class,
					String.class,
					new SolrQuery(subQuery.toString()
							).addFacetField("communityUserId_s"
									).setFacetMinCount(1).setFacetSort(
											FacetParams.FACET_SORT_COUNT))) {
				followProductScores.put(
						facetResult.getValue(), facetResult.getCount());
			}
		}
		return followProductScores;
	}

	/**
	 * 指定したコミュニティユーザーと共通のフォロー質問を持った、非フォローユーザー
	 * マップを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param excludeQuery フォローユーザーの除外条件
	 * @return ユーザーマップ
	 */
	@Override
	public Map<String, Long> loadCommonFollowQuestionScoresForSuggest(
			String communityUserId,
			String excludeQuery) {
		SolrQuery baseQuery = new SolrQuery("communityUserId_s:" + SolrUtil.escape(communityUserId) + " AND deleteFlag_b:false"
				).setRows(
						SolrConstants.QUERY_ROW_LIMIT);
		SearchResult<QuestionFollowDO> followQuestions
				= new SearchResult<QuestionFollowDO>(
						solrOperations.findByQuery(
				baseQuery, QuestionFollowDO.class,
				Path.includeProp("followQuestionId")));

		Map<String, Long> followQuestionScores = new HashMap<String, Long>();
		if (followQuestions.getNumFound() > 0) {
			StringBuilder subQuery = new StringBuilder();
			subQuery.append(excludeQuery.toString());
			subQuery.append(" AND deleteFlag_b:false");
			subQuery.append(" AND (");
			for (int i = 0; i < followQuestions.getDocuments().size(); i++) {
				if (i > 0) {
					subQuery.append(" OR ");
				}
				subQuery.append("followQuestionId_s:");
				subQuery.append(SolrUtil.escape(followQuestions.getDocuments(
						).get(i).getFollowQuestion().getQuestionId()));
			}
			subQuery.append(")");
			for (FacetResult<String> facetResult : solrOperations.facet(
					QuestionFollowDO.class,
					String.class,
					new SolrQuery(subQuery.toString()
							).addFacetField("communityUserId_s"
									).setFacetMinCount(1).setFacetSort(
											FacetParams.FACET_SORT_COUNT))) {
				followQuestionScores.put(
						facetResult.getValue(), facetResult.getCount());
			}
		}
		return followQuestionScores;
	}

	/**
	 * 指定したコミュニティユーザーと共通の購入商品を持った、非フォローユーザー
	 * マップを返します。
	 * @param communityUserId コミュニティユーザーID
	 * @param excludeQuery フォローユーザーの除外条件
	 * @return ユーザーマップ
	 */
	@Override
	public Map<String, Long> loadCommonPurchaseProductScoresForSuggest(
			String communityUserId,
			String excludeQuery) {
		SearchResult<PurchaseProductDO> purchaseProducts
				= new SearchResult<PurchaseProductDO>(
						solrOperations.findByQuery(
				new SolrQuery("communityUserId_s:" + SolrUtil.escape(communityUserId)
						+ " AND purchaseDate_dt:" + SolrConstants.QUERY_DATE_TO_NOW).setRows(
								SolrConstants.QUERY_ROW_LIMIT), PurchaseProductDO.class,
				Path.includeProp("productId")));
		Map<String, Long> purchaseProductScores = new HashMap<String, Long>();
		if (purchaseProducts.getNumFound() > 0) {
			StringBuilder subQuery = new StringBuilder();
			subQuery.append(excludeQuery.toString());
			subQuery.append(" AND ");
			subQuery.append("purchaseDate_dt:" + SolrConstants.QUERY_DATE_TO_NOW +  " AND (");
			for (int i = 0; i < purchaseProducts.getDocuments().size(); i++) {
				if (i > 0) {
					subQuery.append(" OR ");
				}
				subQuery.append("productId_s:");
				subQuery.append(SolrUtil.escape(purchaseProducts.getDocuments(
						).get(i).getProduct().getSku()));
			}
			subQuery.append(")");
			subQuery.append(" AND publicSetting_b:").append(true);
			for (FacetResult<String> facetResult : solrOperations.facet(
					PurchaseProductDO.class,
					String.class,
					new SolrQuery(subQuery.toString()
							).addFacetField("communityUserId_s"
									).setFacetMinCount(1).setFacetSort(
											FacetParams.FACET_SORT_COUNT))) {
				purchaseProductScores.put(
						facetResult.getValue(), facetResult.getCount());
			}
		}
		return purchaseProductScores;
	}

	/**
	 * 検索条件に合致する全てのCommunityUserDOを取得します。
	 * @param solrQuery Solr検索条件
	 * @return CommunityUserDO
	 */
	@Override
	public SearchResult<CommunityUserDO> findCommunityUserByQuery(SolrQuery solrQuery) {
		SearchResult<CommunityUserDO> searchResult = new SearchResult<CommunityUserDO>(
				solrOperations.findByQuery(solrQuery, CommunityUserDO.class, Path.DEFAULT));
		return searchResult;
	}

	/**
	 * 指定したコミュニティユーザーの中で、
	 * ニックネームに前方一致するコミュニティユーザーを返します。
	 * @param communityUserId コミュニティユーザーIDリスト
	 * @param keyword キーワード
	 * @param limit 最大取得件数
	 * @return コミュニティユーザーのリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findCommunityUserByPartialMatch(
			String communityUserId,
			String keyword,
			String offsetUserName,
			int limit) {

		// この検索方法は、Solr内で全角スペースをスペースと判断できない為、全角スペースの含まれる検索時に検索値を返さないバグ対処の為
		// 全角スペースを半角化する
		String src = keyword;
		List<String> keywords = new ArrayList<String>();
		if(StringUtils.isNotEmpty(src)) {
			if(src.indexOf("　") >= 0) {
				src = src.replaceAll("　", " ");
			}
			if(src.indexOf(" ") >= 0) {
				for(String sptKeyword:src.split(" ")){
					if(StringUtils.isNotEmpty(sptKeyword))
						keywords.add(sptKeyword);
				}
			}else{
				keywords.add(src);
			}
		}


		StringBuilder query = new StringBuilder();
		query.append("status_s:");
		query.append(SolrUtil.escape(CommunityUserStatus.ACTIVE.getCode()));
		if(!keywords.isEmpty()){
			query.append(" AND (");
			boolean isFirst = true;
			for(String key:keywords) {
				if(! isFirst){
					query.append(" OR ");
				}
				query.append("communityName_s:");
				query.append("*" + SolrUtil.escape(key) + "*");
				isFirst = false;
			}
			query.append(" ) ");
		}
		if (communityUserId != null) {
			query.append(" AND ");
			query.append("!communityUserId:");
			query.append(SolrUtil.escape(communityUserId));
		}
		if (offsetUserName != null) {
			query.append(" AND ");
			query.append("communityName_s:{" + SolrUtil.escape(offsetUserName) + "TO *}");
		}

		return new SearchResult<CommunityUserDO>(
				solrOperations.findByQuery(new SolrQuery(
				query.toString()).setRows(
						limit).setSortField("modifyDateTime_dt", ORDER.desc).setSortField("communityName_s", ORDER.asc),
						CommunityUserDO.class, Path.DEFAULT));
	}

	/**
	 * 指定した外部顧客IDに紐づくコミュニティユーザー（外部顧客ID付き）を取得します。
	 * @param outerCustomerId 外部顧客ID
	 * @return コミュニティユーザー（外部顧客ID付き）リスト
	 */
	@Override
	public List<CommunityUserDO> findCommunityUserWithAccountSharingByOuterCustomerId(String outerCustomerId) {
		List<AccountSharingDO> list = outerCustomerDao.findAccountSharingByOuterCustomerId(outerCustomerId);
		List<String> communityIds = new ArrayList<String>();
		for (AccountSharingDO accountSharing : list) {
			if (accountSharing.isEc()) {
				communityIds.add(accountSharing.getOuterCustomerId());
			}
		}
		if (communityIds.size() == 0) {
			return null;
		}
		List<CommunityUserDO> result = new ArrayList<CommunityUserDO>();
		for (String communityId : communityIds) {
			CommunityUserDO communityUser = loadByHashCommunityId(
					domainConfig.createHashCommunityId(communityId),
					Path.DEFAULT, false, false);
			if (communityUser != null) {
				communityUser.setAccountSharings(list);
				result.add(communityUser);
			}
		}
		if (result.size() == 0) {
			return null;
		}
		
		return result;
	}
	/**
	 * 指定したコミュニティユーザーIDに紐づくコミュニティユーザーIDを取得します。
	 * @param communityUserId コミュニティユーザーID
	 * @return コミュニティユーザーIDリスト
	 */
	@Override
	public List<String> findCommunityUserIdWithAccountSharingByCommunityUserId(String communityUserId) {
		List<String> result = new ArrayList<String>();
		CommunityUserDO baseuser = solrOperations.load(
				CommunityUserDO.class, communityUserId,
				Path.includeProp("communityUserId,communityId"));
		if (baseuser == null) {
			return result;
		}
		result.add(communityUserId);
		List<AccountSharingDO> list = outerCustomerDao.findAccountSharingByOuterCustomerId(
				baseuser.getCommunityId());
		List<String> communityIds = new ArrayList<String>();
		for (AccountSharingDO accountSharing : list) {
			if (accountSharing.isEc() && !accountSharing.getOuterCustomerId().equals(baseuser.getCommunityId())) {
				communityIds.add(accountSharing.getOuterCustomerId());
			}
		}
		if (communityIds.size() == 0) {
			return result;
		}
		for (String communityId : communityIds) {
			CommunityUserDO communityUser = loadByHashCommunityId(
					domainConfig.createHashCommunityId(communityId),
					Path.includeProp("communityUserId"), false, false);
			if (communityUser != null) {
				result.add(communityUser.getCommunityUserId());
			}
		}
		if (result.size() == 0) {
			return result;
		}
		
		return result;
	}

	/**
	 * 指定した期間に更新のあったコミュニティユーザーIDを返します
	 * @param fromDate 検索開始時間
	 * @param toDate 検索終了時間
	 * @param limit 最大検索数
	 * @param offset 検索開始位置
	 * @return コミュニティユーザーIDリスト
	 */
	@Override
	public SearchResult<CommunityUserDO> findUpdatedCommunityUserByOffsetTime(
			Date fromDate, Date toDate, int limit, int offset){
		StringBuilder buffer = new StringBuilder();
		buffer.append("modifyDateTime_dt:{" +
		DateUtil.getThreadLocalDateFormat().format(fromDate) + " TO " + DateUtil.getThreadLocalDateFormat().format(toDate) + "}");
		SolrQuery query = new SolrQuery(buffer.toString());
		if (limit > 0)
			query.setRows(limit);
		query.setStart(offset);
		SearchResult<CommunityUserDO> searchResult = new SearchResult<CommunityUserDO>(
				solrOperations.findByQuery(query, CommunityUserDO.class));
		return searchResult;
	}

	/**
	 * 一時停止処理を行います。
	 * @param communityUserId コミュニティユーザーID
	 * @param communityId コミュニティID
	 * @param stop 停止フラグ
	 */
	@Override
	public void updateStop(
			String communityUserId,
			String communityId,
			boolean stop) {
		CommunityUserStatus updateStatus = CommunityUserStatus.ACTIVE;
		if (stop) {
			updateStatus = CommunityUserStatus.STOP;
		}
		updateCommunityUserStatus(
				communityUserId, communityId, updateStatus,
				false, false, false, false);
	}

	/**
	 * 退会キャンセル処理の第一段階を行います。
	 * @param communityUserId コミュニティユーザーID
	 * @param communityId コミュニティID
	 * @param withdrawKey 退会キー
	 * @return 更新対象キーマップ
	 */
	@Override
	public HashMap<Class<?>, List<String>> cancelWithdraw(
			String communityUserId,
			String communityId,
			String withdrawKey) {

		HashMap<Class<?>, List<String>> updateKeyMap = new HashMap<Class<?>, List<String>>();
		//    購入商品（PurchaseProductDO）
		updateKeyMap.put(PurchaseProductDO.class,
				cancelWithdraw(PurchaseProductDO.class, withdrawKey));
		//    画像ヘッダー（ImageHeaderDO）
		updateKeyMap.put(ImageHeaderDO.class,
				cancelWithdraw(ImageHeaderDO.class, withdrawKey));
		//    画像（ImageDO）
		cancelWithdraw(ImageDO.class, withdrawKey);
		//    レビュー（ReviewDO）
		List<String> reviewIds = cancelWithdraw(ReviewDO.class, withdrawKey);
		for (ReviewDO review : hBaseOperations.find(
				ReviewDO.class, String.class, reviewIds,
				Path.includeProp("cancelPointGrantType")).values()) {
			if (review.getCancelPointGrantType() != null
					&& (review.getCancelPointGrantType(
							).equals(CancelPointGrantType.COMMUNITY_WITHDRAWAL)
							|| review.getCancelPointGrantType(
							).equals(CancelPointGrantType.COMMUNITY_FORCED_WITHDRAWAL))) {
				review.setCancelPointGrantType(null);
				hBaseOperations.save(review, Path.includeProp("cancelPointGrantType"));
			}
		}
		updateKeyMap.put(ReviewDO.class, reviewIds);
		cancelWithdraw(ReviewDecisivePurchaseDO.class, withdrawKey);
		cancelWithdraw(PurchaseLostProductDO.class, withdrawKey);
		cancelWithdraw(UsedProductDO.class, withdrawKey);
		//    レビュー履歴（ReviewHistoryDO）
		updateKeyMap.put(ReviewHistoryDO.class,
				cancelWithdraw(ReviewHistoryDO.class, withdrawKey));
		//    質問（QuestionDO）
		updateKeyMap.put(QuestionDO.class,
				cancelWithdraw(QuestionDO.class, withdrawKey));
		//    質問回答（QuestionAnswerDO）
		updateKeyMap.put(QuestionAnswerDO.class,
				cancelWithdraw(QuestionAnswerDO.class, withdrawKey));
		//    コメント（CommentDO）
		updateKeyMap.put(CommentDO.class,
				cancelWithdraw(CommentDO.class, withdrawKey));
		//    いいね（LikeDO）
		updateKeyMap.put(LikeDO.class,
				cancelWithdraw(LikeDO.class, withdrawKey));
		//    参考になった（VotingDO)
		updateKeyMap.put(VotingDO.class,
				cancelWithdraw(VotingDO.class, withdrawKey));
		//    質問フォロー（QuestionFollowDO）
		updateKeyMap.put(QuestionFollowDO.class,
				cancelWithdraw(QuestionFollowDO.class, withdrawKey));
		//    コミュニティユーザーフォロー（CommunityUserFollowDO）
		updateKeyMap.put(CommunityUserFollowDO.class,
				cancelWithdraw(CommunityUserFollowDO.class, withdrawKey));
		//    商品フォロー（ProductFollowDO）
		updateKeyMap.put(ProductFollowDO.class,
				cancelWithdraw(ProductFollowDO.class, withdrawKey));
		//    商品マスター（ProductMasterDO）
		updateKeyMap.put(ProductMasterDO.class,
				cancelWithdraw(ProductMasterDO.class, withdrawKey));
		//    アクション履歴（ActionHistoryDO）
		updateKeyMap.put(ActionHistoryDO.class,
				cancelWithdraw(ActionHistoryDO.class, withdrawKey));
		//    お知らせ（InformationDO）
		updateKeyMap.put(InformationDO.class,
				cancelWithdraw(InformationDO.class, withdrawKey));

		// お知らせ（一時停止復旧しない）
		List<InformationDO> informations = hBaseOperations.findWithIndex(
				InformationDO.class,
				"communityUserId",
				hBaseOperations.createFilterBuilder(InformationDO.class)
						.appendSingleColumnValueFilter("informationType", CompareOp.EQUAL, InformationType.ACCOUNT_STOP)
						.toFilter(), communityUserId);

		if(informations != null && !informations.isEmpty()) {
			String informationId  =informations.get(0).getInformationId();
			hBaseOperations.deleteByKey(InformationDO.class, informationId);
		}

		//    スパム報告（SpamReportDO）
		updateKeyMap.put(SpamReportDO.class,
				cancelWithdraw(SpamReportDO.class, withdrawKey));
		//    アナウンス（AnnounceDO）
		cancelWithdraw(AnnounceDO.class, withdrawKey);
		//    メール設定（MailSettingDO）
		cancelWithdraw(MailSettingDO.class, withdrawKey);
		//    ソーシャル連携設定（SocialMediaSettingDO）
		cancelWithdraw(SocialMediaSettingDO.class, withdrawKey);

		updateCommunityUserStatus(
				communityUserId, communityId, CommunityUserStatus.ACTIVE, false, false, false, false);
		return updateKeyMap;
	}

	/**
	 * 退会のための更新、削除処理の第一段階を行います。
	 * @param communityUserId コミュニティユーザーID
	 * @param communityId コミュニティID
	 * @param force 強制退会フラグ
	 * @param keepContents 任意退会の場合、コンテンツを保持するかの選択
	 * @return 退会キー
	 */
	@Override
	public String withdraw(
			String communityUserId,
			String communityId,
			boolean force,
			boolean reviewDelete,
			boolean qaDelete,
			boolean imageDelete,
			boolean commentDelete) {
		CommunityUserStatus updateStatus = CommunityUserStatus.INVALID;
		if (force) {
			updateStatus = CommunityUserStatus.FORCE_LEAVE;
		}

		//１、会員ステータスを更新します。
		String withdrawKey = updateCommunityUserStatus(
				communityUserId, communityId, updateStatus,
				reviewDelete, qaDelete, imageDelete, commentDelete);
		return withdrawKey;
	}

	/**
	 * 退会に伴い、コミュニティユーザーのデータをインデックスと共に削除します。
	 * @param communityUserId コミュニティユーザーID
	 * @param withdrawKey 退会キー
	 * @param force 強制退会フラグ
	 * @param byEcWithdraw EC退会かどうか
	 * @param reviewDelete 自身のレビュー＋自身のレビューに対するコメントを削除する場合、true
	 * @param qaDelete 自身の質問＋自身の回答＋自身の回答に関わるコメントを削除する場合、true
	 * @param imageDelete 自身の投稿画像＋自身の投稿画像に関わるコメントを削除する場合、true
	 * @param commentDelete 自身が投稿した全てのコメントを削除する場合、true
	 */
	@Override
	public void deleteCommunityUserDataForWithdrawWithIndex(
			String communityUserId,
			String withdrawKey,
			boolean force,
			boolean byEcWithdraw,
			boolean reviewDelete,
			boolean qaDelete,
			boolean imageDelete,
			boolean commentDelete) {

		deleteCommunityUserDataForIndex(
				communityUserId,
				withdrawKey,
				force,
				reviewDelete,
				qaDelete,
				imageDelete,
				commentDelete);

		deleteCommunityUserData(
				communityUserId,
				withdrawKey,
				force,
				byEcWithdraw,
				reviewDelete,
				qaDelete,
				imageDelete,
				commentDelete);
	}

	/**
	 * 退会のためにインデックスを削除します。
	 * @param communityUserId コミュニティユーザーID
	 * @param withdrawKey 退会キー
	 * @param force 強制退会フラグ
	 * @param reviewDelete 自身のレビュー＋自身のレビューに対するコメントを削除する場合、true
	 * @param qaDelete 自身の質問＋自身の回答＋自身の回答に関わるコメントを削除する場合、true
	 * @param imageDelete 自身の投稿画像＋自身の投稿画像に関わるコメントを削除する場合、true
	 * @param commentDelete 自身が投稿した全てのコメントを削除する場合、true
	 * @return 退会キー
	 */
	private void deleteCommunityUserDataForIndex(
			String communityUserId,
			String withdrawKey,
			boolean force,
			boolean reviewDelete,
			boolean qaDelete,
			boolean imageDelete,
			boolean commentDelete) {
		updateCommunityUserInIndex(communityUserId);

		//３、検索インデックスから削除します。
		StringBuilder deleteQuery = null;

		//    アクション履歴（ActionHistoryDO）
		deleteQuery = new StringBuilder();
		//ユーザーフォロー、商品フォロー、質問フォロー、商品マスターランクイン
		//のアクション履歴を全て削除
		deleteQuery.append("(");
		deleteQuery.append("(actionHistoryType_s:");
		deleteQuery.append(SolrUtil.escape(ActionHistoryType.USER_FOLLOW_USER.getCode()));
		deleteQuery.append(" OR actionHistoryType_s:");
		deleteQuery.append(SolrUtil.escape(ActionHistoryType.USER_FOLLOW_PRODUCT.getCode()));
		deleteQuery.append(" OR actionHistoryType_s:");
		deleteQuery.append(SolrUtil.escape(ActionHistoryType.USER_FOLLOW_QUESTION.getCode()));
		deleteQuery.append(" OR actionHistoryType_s:");
		deleteQuery.append(SolrUtil.escape(ActionHistoryType.USER_PRODUCT_MASTER_RANK_CHANGE.getCode()));
		deleteQuery.append(") AND communityUserId_s:");
		deleteQuery.append(SolrUtil.escape(communityUserId));
		deleteQuery.append(")");
		//自身をフォローするアクション履歴を全て削除
		deleteQuery.append(" OR followCommunityUserId_s:");
		deleteQuery.append(SolrUtil.escape(communityUserId));
		if (force || reviewDelete) {
			//自身の投稿したレビュー、それに伴うフォロー商品新着レビューの
			//アクション履歴を全て削除
			deleteQuery.append(" OR (");
			deleteQuery.append("(actionHistoryType_s:");
			deleteQuery.append(SolrUtil.escape(ActionHistoryType.USER_REVIEW.getCode()));
			deleteQuery.append(" OR actionHistoryType_s:");
			deleteQuery.append(SolrUtil.escape(ActionHistoryType.PRODUCT_REVIEW.getCode()));
			deleteQuery.append(") AND communityUserId_s:");
			deleteQuery.append(SolrUtil.escape(communityUserId));
			deleteQuery.append(")");
			//自身の投稿したレビューに対するコメント投稿、いいねのアクション履歴を全て削除
			deleteQuery.append(" OR (");
			deleteQuery.append("(actionHistoryType_s:");
			deleteQuery.append(SolrUtil.escape(ActionHistoryType.USER_REVIEW_COMMENT.getCode()));
			deleteQuery.append(" OR actionHistoryType_s:");
			deleteQuery.append(SolrUtil.escape(ActionHistoryType.LIKE_REVIEW_50.getCode()));
			deleteQuery.append(") AND relationReviewOwnerId_s:");
			deleteQuery.append(SolrUtil.escape(communityUserId));
			deleteQuery.append(")");
		}
		if (force || qaDelete) {
			//自身の投稿した質問・質問回答、それに伴うフォロー商品新着質問・質問回答、
			//フォロー質問の新着回答のアクション履歴を全て削除
			//自身が投稿した質問に紐付く質問回答投稿、フォロー商品新着回答、
			//フォロー質問の新着回答、質問フォローのアクション履歴を全て削除
			deleteQuery.append(" OR (");
			deleteQuery.append("(actionHistoryType_s:");
			deleteQuery.append(SolrUtil.escape(ActionHistoryType.USER_ANSWER.getCode()));
			deleteQuery.append(" OR actionHistoryType_s:");
			deleteQuery.append(SolrUtil.escape(ActionHistoryType.USER_QUESTION.getCode()));
			deleteQuery.append(" OR actionHistoryType_s:");
			deleteQuery.append(SolrUtil.escape(ActionHistoryType.USER_FOLLOW_QUESTION.getCode()));
			deleteQuery.append(" OR actionHistoryType_s:");
			deleteQuery.append(SolrUtil.escape(ActionHistoryType.PRODUCT_ANSWER.getCode()));
			deleteQuery.append(" OR actionHistoryType_s:");
			deleteQuery.append(SolrUtil.escape(ActionHistoryType.PRODUCT_QUESTION.getCode()));
			deleteQuery.append(" OR actionHistoryType_s:");
			deleteQuery.append(SolrUtil.escape(ActionHistoryType.QUESTION_ANSWER.getCode()));
			deleteQuery.append(") AND (communityUserId_s:");
			deleteQuery.append(SolrUtil.escape(communityUserId));
			deleteQuery.append(" OR relationQuestionOwnerId_s:");
			deleteQuery.append(SolrUtil.escape(communityUserId));
			deleteQuery.append("))");
			//自身の投稿した質問・質問回答に対するコメント投稿、いいねのアクション履歴を全て削除
			deleteQuery.append(" OR (");
			deleteQuery.append("(actionHistoryType_s:");
			deleteQuery.append(SolrUtil.escape(ActionHistoryType.USER_ANSWER_COMMENT.getCode()));
			deleteQuery.append(" OR actionHistoryType_s:");
			deleteQuery.append(SolrUtil.escape(ActionHistoryType.LIKE_ANSWER_50.getCode()));
			deleteQuery.append(") AND (relationQuestionAnswerOwnerId_s:");
			deleteQuery.append(SolrUtil.escape(communityUserId));
			deleteQuery.append(" OR relationQuestionOwnerId_s:");
			deleteQuery.append(SolrUtil.escape(communityUserId));
			deleteQuery.append("))");
		}
		if (force || imageDelete) {
			//自身の投稿した画像、それに伴うフォロー商品新着画像のアクション履歴を全て削除
			deleteQuery.append(" OR (");
			deleteQuery.append("(actionHistoryType_s:");
			deleteQuery.append(SolrUtil.escape(ActionHistoryType.USER_IMAGE.getCode()));
			deleteQuery.append(" OR actionHistoryType_s:");
			deleteQuery.append(SolrUtil.escape(ActionHistoryType.PRODUCT_IMAGE.getCode()));
			deleteQuery.append(") AND communityUserId_s:");
			deleteQuery.append(SolrUtil.escape(communityUserId));
			deleteQuery.append(")");
			//自身の投稿した画像に対するコメント投稿のアクション履歴を全て削除
			deleteQuery.append(" OR (");
			deleteQuery.append("(actionHistoryType_s:");
			deleteQuery.append(SolrUtil.escape(ActionHistoryType.USER_IMAGE_COMMENT.getCode()));
			deleteQuery.append(" OR actionHistoryType_s:");
			deleteQuery.append(SolrUtil.escape(ActionHistoryType.LIKE_IMAGE_50.getCode()));
			deleteQuery.append(") AND relationImageOwnerId_s:");
			deleteQuery.append(SolrUtil.escape(communityUserId));
			deleteQuery.append(")");
		}
		if (force || commentDelete) {
			//自身の投稿したコメント投稿のアクション履歴を全て削除
			deleteQuery.append(" OR (");
			deleteQuery.append("(actionHistoryType_s:");
			deleteQuery.append(SolrUtil.escape(ActionHistoryType.USER_REVIEW_COMMENT.getCode()));
			deleteQuery.append(" OR actionHistoryType_s:");
			deleteQuery.append(SolrUtil.escape(ActionHistoryType.USER_ANSWER_COMMENT.getCode()));
			deleteQuery.append(" OR actionHistoryType_s:");
			deleteQuery.append(SolrUtil.escape(ActionHistoryType.USER_IMAGE_COMMENT.getCode()));
			deleteQuery.append(") AND communityUserId_s:");
			deleteQuery.append(SolrUtil.escape(communityUserId));
			deleteQuery.append(")");
		}

		solrOperations.deleteByQuery(
				new SolrQuery(deleteQuery.toString()),
						ActionHistoryDO.class);

		//    お知らせ（InformationDO）
		deleteQuery = new StringBuilder();
		deleteQuery.append("communityUserId_s:");
		deleteQuery.append(SolrUtil.escape(communityUserId));
		deleteQuery.append(" OR followerCommunityUserId_s:");
		deleteQuery.append(SolrUtil.escape(communityUserId));
		deleteQuery.append(" OR relationLikeOwnerId_s:");
		deleteQuery.append(SolrUtil.escape(communityUserId));
		if (force || qaDelete) {
			deleteQuery.append(" OR ((informationType_s:");
			deleteQuery.append(SolrUtil.escape(InformationType.QUESTION_ANSWER_COMMENT_ADD.getCode()));
			deleteQuery.append(" OR informationType_s:");
			deleteQuery.append(SolrUtil.escape(InformationType.QUESTION_ANSWER_ADD.getCode()));
			deleteQuery.append(" OR informationType_s:");
			deleteQuery.append(SolrUtil.escape(InformationType.QUESTION_ANSWER_LIKE_ADD.getCode()));
			deleteQuery.append(") AND (relationQuestionOwnerId_s:");
			deleteQuery.append(SolrUtil.escape(communityUserId));
			deleteQuery.append(" OR relationQuestionAnswerOwnerId_s:");
			deleteQuery.append(SolrUtil.escape(communityUserId));
			deleteQuery.append("))");
		}
		if (force || commentDelete) {
			deleteQuery.append(" OR ((informationType_s:");
			deleteQuery.append(SolrUtil.escape(InformationType.REVIEW_COMMENT_ADD.getCode()));
			deleteQuery.append(" OR informationType_s:");
			deleteQuery.append(SolrUtil.escape(InformationType.QUESTION_ANSWER_COMMENT_ADD.getCode()));
			deleteQuery.append(" OR informationType_s:");
			deleteQuery.append(SolrUtil.escape(InformationType.IMAGE_COMMENT_ADD.getCode()));
			deleteQuery.append(") AND relationCommentOwnerId_s:");
			deleteQuery.append(SolrUtil.escape(communityUserId));
			deleteQuery.append(")");
		}
		solrOperations.deleteByQuery(
				new SolrQuery(deleteQuery.toString()),
						InformationDO.class);

		List<CommentDO> comments = new ArrayList<CommentDO>();
		//    コメント（CommentDO）
		if (force || reviewDelete) {
			//CommentTargetType.REVIEW && relationReviewOwnerId
			comments.addAll(scanForWithdraw(
					CommentDO.class,
					"relationReviewOwnerId", communityUserId,
					"targetType", CommentTargetType.REVIEW));
		}
		if (force || qaDelete) {
			//CommentTargetType.QUESTION_ANSWER && relationQuestionAnswerOwnerId
			comments.addAll(scanForWithdraw(
					CommentDO.class,
					"relationQuestionAnswerOwnerId", communityUserId,
					"targetType", CommentTargetType.QUESTION_ANSWER));
			//CommentTargetType.QUESTION_ANSWER && relationQuestionOwnerId
			comments.addAll(scanForWithdraw(
					CommentDO.class,
					"relationQuestionOwnerId", communityUserId,
					"targetType", CommentTargetType.QUESTION_ANSWER));
		}
		if (force || imageDelete) {
			//CommentTargetType.IMAGE && relationImageOwnerId
			comments.addAll(scanForWithdraw(
					CommentDO.class,
					"relationImageOwnerId", communityUserId,
					"targetType", CommentTargetType.IMAGE));
		}
		if (force || commentDelete) {
			//communityUserId
			comments.addAll(scanForWithdraw(
					CommentDO.class,
					"communityUserId", communityUserId));
		}
		if (comments.size() > 0) {
			for (CommentDO comment : comments) {
				comment.setWithdraw(true);
				comment.setWithdrawKey(withdrawKey);
				comment.setModifyDateTime(timestampHolder.getTimestamp());
			}
			solrOperations.save(CommentDO.class, comments);
		}
		comments = null;

		//    コミュニティユーザーフォロー（CommunityUserFollowDO）
		solrOperations.deleteByQuery(
				new SolrQuery("communityUserId_s:" + SolrUtil.escape(communityUserId)
						+ " OR followCommunityUserId_s:" + SolrUtil.escape(communityUserId)),
						CommunityUserFollowDO.class);

		List<ImageHeaderDO> imageHeaders = new ArrayList<ImageHeaderDO>();
		//    画像ヘッダー（ImageHeaderDO）
		//PostContentType.PROFILE && ownerCommunityUserId
		imageHeaders.addAll(scanImageForWithdraw(
				"ownerCommunityUserId",
				communityUserId,
				PostContentType.PROFILE));
		//PostContentType.PROFILE_THUMBNAIL && ownerCommunityUserId
		imageHeaders.addAll(scanImageForWithdraw(
				"ownerCommunityUserId",
				communityUserId,
				PostContentType.PROFILE_THUMBNAIL));
		if (force || reviewDelete) {
			//PostContentType.REVIEW && ownerCommunityUserId
			imageHeaders.addAll(scanImageForWithdraw(
					"ownerCommunityUserId",
					communityUserId,
					PostContentType.REVIEW));
		}
		if (force || qaDelete) {
			//PostContentType.QUESTION && ownerCommunityUserId
			imageHeaders.addAll(scanImageForWithdraw(
					"ownerCommunityUserId",
					communityUserId,
					PostContentType.QUESTION));
			//PostContentType.ANSWER && ownerCommunityUserId
			imageHeaders.addAll(scanImageForWithdraw(
					"ownerCommunityUserId",
					communityUserId,
					PostContentType.ANSWER));
			//PostContentType.ANSWER && relationQuestionOwnerId
			imageHeaders.addAll(scanImageForWithdraw(
					"relationQuestionOwnerId",
					communityUserId,
					PostContentType.ANSWER));
		}
		if (force || imageDelete) {
			//PostContentType.IMAGE_SET && ownerCommunityUserId
			imageHeaders.addAll(scanImageForWithdraw(
					"ownerCommunityUserId",
					communityUserId,
					PostContentType.IMAGE_SET));
		}
		if (imageHeaders.size() > 0) {
			for (ImageHeaderDO imageHeader : imageHeaders) {
				imageHeader.setWithdraw(true);
				imageHeader.setWithdrawKey(withdrawKey);
				imageHeader.setModifyDateTime(timestampHolder.getTimestamp());
			}
			solrOperations.save(ImageHeaderDO.class, imageHeaders);
		}
		imageHeaders = null;

		//    いいね（LikeDO）
		deleteQuery = new StringBuilder();
		deleteQuery.append("communityUserId_s:");
		deleteQuery.append(SolrUtil.escape(communityUserId));
		if (force || reviewDelete) {
			deleteQuery.append(" OR (targetType_s:");
			deleteQuery.append(SolrUtil.escape(LikeTargetType.REVIEW.getCode()));
			deleteQuery.append(" AND relationReviewOwnerId_s:");
			deleteQuery.append(SolrUtil.escape(communityUserId));
			deleteQuery.append(")");
		}
		if (force || qaDelete) {
			deleteQuery.append(" OR (targetType_s:");
			deleteQuery.append(SolrUtil.escape(LikeTargetType.QUESTION_ANSWER.getCode()));
			deleteQuery.append(" AND (relationQuestionAnswerOwnerId_s:");
			deleteQuery.append(SolrUtil.escape(communityUserId));
			deleteQuery.append(" OR relationQuestionOwnerId_s:");
			deleteQuery.append(SolrUtil.escape(communityUserId));
			deleteQuery.append("))");
		}
		if (force || imageDelete) {
			deleteQuery.append(" OR (targetType_s:");
			deleteQuery.append(SolrUtil.escape(LikeTargetType.IMAGE.getCode()));
			deleteQuery.append(" AND relationImageOwnerId_s:");
			deleteQuery.append(SolrUtil.escape(communityUserId));
			deleteQuery.append(")");
		}
		solrOperations.deleteByQuery(
				new SolrQuery(deleteQuery.toString()),
				LikeDO.class);
		
		//    参考になった（VotingDO）
		deleteQuery = new StringBuilder();
		deleteQuery.append("communityUserId_s:");
		deleteQuery.append(SolrUtil.escape(communityUserId));
		if (force || reviewDelete) {
			deleteQuery.append(" OR (targetType_s:");
			deleteQuery.append(SolrUtil.escape(VotingTargetType.REVIEW.getCode()));
			deleteQuery.append(" AND relationReviewOwnerId_s:");
			deleteQuery.append(SolrUtil.escape(communityUserId));
			deleteQuery.append(")");
		}
		if (force || qaDelete) {
			deleteQuery.append(" OR (targetType_s:");
			deleteQuery.append(SolrUtil.escape(VotingTargetType.QUESTION_ANSWER.getCode()));
			deleteQuery.append(" AND (relationQuestionAnswerOwnerId_s:");
			deleteQuery.append(SolrUtil.escape(communityUserId));
			deleteQuery.append(" OR relationQuestionOwnerId_s:");
			deleteQuery.append(SolrUtil.escape(communityUserId));
			deleteQuery.append("))");
		}
		if (force || imageDelete) {
			deleteQuery.append(" OR (targetType_s:");
			deleteQuery.append(SolrUtil.escape(VotingTargetType.IMAGE.getCode()));
			deleteQuery.append(" AND relationImageOwnerId_s:");
			deleteQuery.append(SolrUtil.escape(communityUserId));
			deleteQuery.append(")");
		}
		solrOperations.deleteByQuery(
				new SolrQuery(deleteQuery.toString()),
				VotingDO.class);
		

		//    商品フォロー（ProductFollowDO）
		solrOperations.deleteByQuery(
				new SolrQuery("communityUserId_s:" + SolrUtil.escape(communityUserId)),
				ProductFollowDO.class);

		//    商品マスター（ProductMasterDO）
		solrOperations.deleteByQuery(
				new SolrQuery("communityUserId_s:" + SolrUtil.escape(communityUserId)),
				ProductMasterDO.class);

		if (force) {
			List<PurchaseProductDO> purchaseProducts = scanForWithdraw(
					PurchaseProductDO.class,
					"communityUserId", communityUserId);
			if (purchaseProducts.size() > 0) {
				for (PurchaseProductDO purchaseProduct : purchaseProducts) {
					purchaseProduct.setWithdraw(true);
					purchaseProduct.setWithdrawKey(withdrawKey);
					purchaseProduct.setModifyDateTime(timestampHolder.getTimestamp());
				}
				solrOperations.save(PurchaseProductDO.class, purchaseProducts);
			}
		}

		//    質問回答（QuestionAnswerDO）
		if (force || qaDelete) {
			List<QuestionAnswerDO> questionAnswers = new ArrayList<QuestionAnswerDO>();
			questionAnswers.addAll(scanForWithdraw(
					QuestionAnswerDO.class,
					"communityUserId", communityUserId));
			questionAnswers.addAll(scanForWithdraw(
					QuestionAnswerDO.class,
					"relationQuestionOwnerId", communityUserId));
			if (questionAnswers.size() > 0) {
				for (QuestionAnswerDO questionAnswer : questionAnswers) {
					questionAnswer.setWithdraw(true);
					questionAnswer.setWithdrawKey(withdrawKey);
					questionAnswer.setModifyDateTime(timestampHolder.getTimestamp());
				}
				solrOperations.save(QuestionAnswerDO.class, questionAnswers);
			}
		}

		//    質問フォロー（QuestionFollowDO）
		if (force || qaDelete) {
			solrOperations.deleteByQuery(
					new SolrQuery("communityUserId_s:"
							+ SolrUtil.escape(communityUserId)
							+ " OR relationQuestionOwnerId_s:"
							+ SolrUtil.escape(communityUserId)),
					QuestionFollowDO.class);
		} else {
			solrOperations.deleteByQuery(
					new SolrQuery("communityUserId_s:"
							+ SolrUtil.escape(communityUserId)),
					QuestionFollowDO.class);
		}

		//    質問（QuestionDO）
		if (force || qaDelete) {
			List<QuestionDO> questions = scanForWithdraw(
					QuestionDO.class,
					"communityUserId", communityUserId);
			if (questions.size() > 0) {
				for (QuestionDO question : questions) {
					question.setWithdraw(true);
					question.setWithdrawKey(withdrawKey);
					question.setModifyDateTime(timestampHolder.getTimestamp());
				}
				solrOperations.save(QuestionDO.class, questions);
			}
		}

		//    レビュー（ReviewDO）
		if (force || reviewDelete) {
			List<ReviewDO> reviews = scanForWithdraw(
					ReviewDO.class,
					"communityUserId", communityUserId);
			if (reviews.size() > 0) {
				for (ReviewDO review : reviews) {
					review.setWithdraw(true);
					review.setWithdrawKey(withdrawKey);
					review.setModifyDateTime(timestampHolder.getTimestamp());
				}
				solrOperations.save(ReviewDO.class, reviews);
			}
			List<ReviewDecisivePurchaseDO> reviewDecisivePurchases
					= scanForWithdraw(
					ReviewDecisivePurchaseDO.class,
					"communityUserId", communityUserId);
			if (reviewDecisivePurchases.size() > 0) {
				for (ReviewDecisivePurchaseDO reviewDecisivePurchase : reviewDecisivePurchases) {
					reviewDecisivePurchase.setWithdraw(true);
					reviewDecisivePurchase.setWithdrawKey(withdrawKey);
					reviewDecisivePurchase.setModifyDateTime(timestampHolder.getTimestamp());
				}
				solrOperations.save(ReviewDecisivePurchaseDO.class, reviewDecisivePurchases);
			}
			List<PurchaseLostProductDO> purchaseLostProducts = scanForWithdraw(
					PurchaseLostProductDO.class,
					"communityUserId", communityUserId);
			if (purchaseLostProducts.size() > 0) {
				for (PurchaseLostProductDO purchaseLostProduct : purchaseLostProducts) {
					purchaseLostProduct.setWithdraw(true);
					purchaseLostProduct.setWithdrawKey(withdrawKey);
					purchaseLostProduct.setModifyDateTime(timestampHolder.getTimestamp());
				}
				solrOperations.save(PurchaseLostProductDO.class, purchaseLostProducts);
			}
			List<UsedProductDO> usedProducts = scanForWithdraw(
					UsedProductDO.class,
					"communityUserId", communityUserId);
			if (usedProducts.size() > 0) {
				for (UsedProductDO usedProduct : usedProducts) {
					usedProduct.setWithdraw(true);
					usedProduct.setWithdrawKey(withdrawKey);
					usedProduct.setModifyDateTime(timestampHolder.getTimestamp());
				}
				solrOperations.save(UsedProductDO.class, usedProducts);
			}
		}

		//    レビュー履歴（ReviewHistoryDO）
		if (force || reviewDelete) {
			List<ReviewHistoryDO> reviewHistories = scanForWithdraw(
					ReviewHistoryDO.class,
					"communityUserId", communityUserId);
			if (reviewHistories.size() > 0) {
				for (ReviewHistoryDO reviewHistory : reviewHistories) {
					reviewHistory.setWithdraw(true);
					reviewHistory.setWithdrawKey(withdrawKey);
					reviewHistory.setModifyDateTime(timestampHolder.getTimestamp());
				}
				solrOperations.save(ReviewHistoryDO.class, reviewHistories);
			}
		}

		List<SpamReportDO> spamReports = new ArrayList<SpamReportDO>();
		//    スパム報告（SpamReportDO）
		if (force || reviewDelete || qaDelete || imageDelete || commentDelete) {
			spamReports.addAll(scanForWithdraw(
					SpamReportDO.class,
					"communityUserId", communityUserId));
			if (force || reviewDelete) {
				//SpamReportGroupType.REVIEW && relationReviewOwnerId
				spamReports.addAll(scanForWithdraw(
						SpamReportDO.class,
						"relationReviewOwnerId", communityUserId,
						"groupType", SpamReportGroupType.REVIEW));
			}
			if (force || qaDelete) {
				//SpamReportGroupType.QUESTION && relationReviewOwnerId
				spamReports.addAll(scanForWithdraw(
						SpamReportDO.class,
						"relationReviewOwnerId", communityUserId,
						"groupType", SpamReportGroupType.QUESTION));
				//SpamReportGroupType.QUESTION && relationReviewOwnerId
				spamReports.addAll(scanForWithdraw(
						SpamReportDO.class,
						"relationReviewOwnerId", communityUserId,
						"groupType", SpamReportGroupType.QUESTION));
			}
			if (force || imageDelete) {
				//SpamReportGroupType.IMAGE && relationImageOwnerId
				spamReports.addAll(scanForWithdraw(
						SpamReportDO.class,
						"relationImageOwnerId", communityUserId,
						"groupType", SpamReportGroupType.IMAGE));
			}
			if (force || commentDelete) {
				//SpamReportTargetType.COMMENT && relationCommentOwnerId
				spamReports.addAll(scanForWithdraw(
						SpamReportDO.class,
						"relationCommentOwnerId", communityUserId,
						"targetType", SpamReportTargetType.COMMENT));
			}
		}
		if (spamReports.size() > 0) {
			for (SpamReportDO spamReport : spamReports) {
				spamReport.setWithdraw(true);
				spamReport.setWithdrawKey(withdrawKey);
				spamReport.setModifyDateTime(timestampHolder.getTimestamp());
			}
			solrOperations.save(SpamReportDO.class, spamReports);
		}
	}

	/**
	 * 退会のためにデータを削除します。
	 * @param communityUserId コミュニティユーザーID
	 * @param withdrawKey 退会キー
	 * @param force 強制退会フラグ
	 * @param byEcWithdraw EC退会かどうか
	 * @param reviewDelete 自身のレビュー＋自身のレビューに対するコメントを削除する場合、true
	 * @param qaDelete 自身の質問＋自身の回答＋自身の回答に関わるコメントを削除する場合、true
	 * @param imageDelete 自身の投稿画像＋自身の投稿画像に関わるコメントを削除する場合、true
	 * @param commentDelete 自身が投稿した全てのコメントを削除する場合、true
	 * @return 退会キー
	 */
	private void deleteCommunityUserData(
			String communityUserId,
			String withdrawKey,
			boolean force,
			boolean byEcWithdraw,
			boolean reviewDelete,
			boolean qaDelete,
			boolean imageDelete,
			boolean commentDelete) {

		//    アクション履歴（ActionHistoryDO）
		//ActionHistoryType.USER_FOLLOW_USER && communityUserId
		updateDataForWithdraw(
				ActionHistoryDO.class, withdrawKey,
				"communityUserId", communityUserId,
				"actionHistoryType", ActionHistoryType.USER_FOLLOW_USER);
		//ActionHistoryType.USER_FOLLOW_PRODUCT && communityUserId
		updateDataForWithdraw(
				ActionHistoryDO.class, withdrawKey,
				"communityUserId", communityUserId,
				"actionHistoryType", ActionHistoryType.USER_FOLLOW_PRODUCT);
		//ActionHistoryType.USER_FOLLOW_QUESTION && communityUserId
		updateDataForWithdraw(
				ActionHistoryDO.class, withdrawKey,
				"communityUserId", communityUserId,
				"actionHistoryType", ActionHistoryType.USER_FOLLOW_QUESTION);
		//ActionHistoryType.USER_PRODUCT_MASTER_RANK_CHANGE && communityUserId
		updateDataForWithdraw(
				ActionHistoryDO.class, withdrawKey,
				"communityUserId", communityUserId,
				"actionHistoryType", ActionHistoryType.USER_PRODUCT_MASTER_RANK_CHANGE);
		//followCommunityUserId
		updateDataForWithdraw(
				ActionHistoryDO.class, withdrawKey,
				"followCommunityUserId", communityUserId);
		if (force || reviewDelete) {
			//ActionHistoryType.USER_REVIEW && communityUserId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"communityUserId", communityUserId,
					"actionHistoryType", ActionHistoryType.USER_REVIEW);
			//ActionHistoryType.PRODUCT_REVIEW && communityUserId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"communityUserId", communityUserId,
					"actionHistoryType", ActionHistoryType.PRODUCT_REVIEW);
			//ActionHistoryType.USER_REVIEW_COMMENT && relationReviewOwnerId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"relationReviewOwnerId", communityUserId,
					"actionHistoryType", ActionHistoryType.USER_REVIEW_COMMENT);
			//ActionHistoryType.LIKE_REVIEW_50 && relationReviewOwnerId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"relationReviewOwnerId", communityUserId,
					"actionHistoryType", ActionHistoryType.LIKE_REVIEW_50);
		}
		if (force || qaDelete) {
			//ActionHistoryType.USER_ANSWER && communityUserId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"communityUserId", communityUserId,
					"actionHistoryType", ActionHistoryType.USER_ANSWER);
			//ActionHistoryType.USER_ANSWER && relationQuestionOwnerId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"relationQuestionOwnerId", communityUserId,
					"actionHistoryType", ActionHistoryType.USER_ANSWER);

			//ActionHistoryType.USER_QUESTION && communityUserId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"communityUserId", communityUserId,
					"actionHistoryType", ActionHistoryType.USER_QUESTION);
			//ActionHistoryType.USER_QUESTION && relationQuestionOwnerId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"relationQuestionOwnerId", communityUserId,
					"actionHistoryType", ActionHistoryType.USER_QUESTION);

			//ActionHistoryType.USER_FOLLOW_QUESTION && communityUserId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"communityUserId", communityUserId,
					"actionHistoryType", ActionHistoryType.USER_FOLLOW_QUESTION);
			//ActionHistoryType.USER_FOLLOW_QUESTION && relationQuestionOwnerId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"relationQuestionOwnerId", communityUserId,
					"actionHistoryType", ActionHistoryType.USER_FOLLOW_QUESTION);

			//ActionHistoryType.PRODUCT_ANSWER && communityUserId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"communityUserId", communityUserId,
					"actionHistoryType", ActionHistoryType.PRODUCT_ANSWER);
			//ActionHistoryType.PRODUCT_ANSWER && relationQuestionOwnerId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"relationQuestionOwnerId", communityUserId,
					"actionHistoryType", ActionHistoryType.PRODUCT_ANSWER);

			//ActionHistoryType.PRODUCT_QUESTION && communityUserId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"communityUserId", communityUserId,
					"actionHistoryType", ActionHistoryType.PRODUCT_QUESTION);
			//ActionHistoryType.PRODUCT_QUESTION && relationQuestionOwnerId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"relationQuestionOwnerId", communityUserId,
					"actionHistoryType", ActionHistoryType.PRODUCT_QUESTION);

			//ActionHistoryType.QUESTION_ANSWER && communityUserId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"communityUserId", communityUserId,
					"actionHistoryType", ActionHistoryType.QUESTION_ANSWER);
			//ActionHistoryType.QUESTION_ANSWER && relationQuestionOwnerId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"relationQuestionOwnerId", communityUserId,
					"actionHistoryType", ActionHistoryType.QUESTION_ANSWER);

			//ActionHistoryType.USER_ANSWER_COMMENT && relationQuestionAnswerOwnerId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"relationQuestionAnswerOwnerId", communityUserId,
					"actionHistoryType", ActionHistoryType.USER_ANSWER_COMMENT);

			//ActionHistoryType.USER_ANSWER_COMMENT && relationQuestionOwnerId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"relationQuestionOwnerId", communityUserId,
					"actionHistoryType", ActionHistoryType.USER_ANSWER_COMMENT);

			//ActionHistoryType.LIKE_ANSWER_50 && relationQuestionAnswerOwnerId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"relationQuestionAnswerOwnerId", communityUserId,
					"actionHistoryType", ActionHistoryType.LIKE_ANSWER_50);

			//ActionHistoryType.LIKE_ANSWER_50 && relationQuestionOwnerId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"relationQuestionOwnerId", communityUserId,
					"actionHistoryType", ActionHistoryType.LIKE_ANSWER_50);
		}
		if (force || imageDelete) {
			//ActionHistoryType.USER_IMAGE && communityUserId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"communityUserId", communityUserId,
					"actionHistoryType", ActionHistoryType.USER_IMAGE);
			//ActionHistoryType.PRODUCT_IMAGE && communityUserId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"communityUserId", communityUserId,
					"actionHistoryType", ActionHistoryType.PRODUCT_IMAGE);
			//ActionHistoryType.USER_IMAGE_COMMENT && relationImageOwnerId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"relationImageOwnerId", communityUserId,
					"actionHistoryType", ActionHistoryType.USER_IMAGE_COMMENT);
			//ActionHistoryType.LIKE_IMAGE_50 && relationImageOwnerId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"relationImageOwnerId", communityUserId,
					"actionHistoryType", ActionHistoryType.LIKE_IMAGE_50);
		}
		if (force || commentDelete) {
			//ActionHistoryType.USER_REVIEW_COMMENT && communityUserId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"communityUserId", communityUserId,
					"actionHistoryType", ActionHistoryType.USER_REVIEW_COMMENT);
			//ActionHistoryType.USER_ANSWER_COMMENT && communityUserId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"communityUserId", communityUserId,
					"actionHistoryType", ActionHistoryType.USER_ANSWER_COMMENT);
			//ActionHistoryType.USER_IMAGE_COMMENT && communityUserId
			updateDataForWithdraw(
					ActionHistoryDO.class, withdrawKey,
					"communityUserId", communityUserId,
					"actionHistoryType", ActionHistoryType.USER_IMAGE_COMMENT);
		}

		//    お知らせ（InformationDO）
		//communityUserId
		updateDataForWithdraw(
				InformationDO.class, withdrawKey,
				"communityUserId", communityUserId);
		//followerCommunityUserId
		updateDataForWithdraw(
				InformationDO.class, withdrawKey,
				"followerCommunityUserId", communityUserId);
		//relationLikeOwnerId
		updateDataForWithdraw(
				InformationDO.class, withdrawKey,
				"relationLikeOwnerId", communityUserId);
		if (force || qaDelete) {
			//InformationType.QUESTION_ANSWER_COMMENT_ADD && relationQuestionOwnerId
			updateDataForWithdraw(
					InformationDO.class, withdrawKey,
					"relationQuestionOwnerId", communityUserId,
					"informationType", InformationType.QUESTION_ANSWER_COMMENT_ADD);
			//InformationType.QUESTION_ANSWER_COMMENT_ADD && relationQuestionAnswerOwnerId
			updateDataForWithdraw(
					InformationDO.class, withdrawKey,
					"relationQuestionAnswerOwnerId", communityUserId,
					"informationType", InformationType.QUESTION_ANSWER_COMMENT_ADD);

			//InformationType.QUESTION_ANSWER_ADD && relationQuestionOwnerId
			updateDataForWithdraw(
					InformationDO.class, withdrawKey,
					"relationQuestionOwnerId", communityUserId,
					"informationType", InformationType.QUESTION_ANSWER_ADD);
			//InformationType.QUESTION_ANSWER_ADD && relationQuestionAnswerOwnerId
			updateDataForWithdraw(
					InformationDO.class, withdrawKey,
					"relationQuestionAnswerOwnerId", communityUserId,
					"informationType", InformationType.QUESTION_ANSWER_ADD);

			//InformationType.QUESTION_ANSWER_LIKE_ADD && relationQuestionOwnerId
			updateDataForWithdraw(
					InformationDO.class, withdrawKey,
					"relationQuestionOwnerId", communityUserId,
					"informationType", InformationType.QUESTION_ANSWER_LIKE_ADD);
			//InformationType.QUESTION_ANSWER_LIKE_ADD && relationQuestionAnswerOwnerId
			updateDataForWithdraw(
					InformationDO.class, withdrawKey,
					"relationQuestionAnswerOwnerId", communityUserId,
					"informationType", InformationType.QUESTION_ANSWER_LIKE_ADD);
		}
		if (force || commentDelete) {
			//InformationType.REVIEW_COMMENT_ADD && relationCommentOwnerId
			updateDataForWithdraw(
					InformationDO.class, withdrawKey,
					"relationCommentOwnerId", communityUserId,
					"informationType", InformationType.REVIEW_COMMENT_ADD);
			//InformationType.QUESTION_ANSWER_COMMENT_ADD && relationCommentOwnerId
			updateDataForWithdraw(
					InformationDO.class, withdrawKey,
					"relationCommentOwnerId", communityUserId,
					"informationType", InformationType.QUESTION_ANSWER_COMMENT_ADD);
			//InformationType.IMAGE_COMMENT_ADD && relationCommentOwnerId
			updateDataForWithdraw(
					InformationDO.class, withdrawKey,
					"relationCommentOwnerId", communityUserId,
					"informationType", InformationType.IMAGE_COMMENT_ADD);
		}

		//    アナウンス（AnnounceDO）
		updateDataForWithdraw(
				AnnounceDO.class, withdrawKey,
				"communityUserId", communityUserId);

		//    コメント（CommentDO）
		if (force || reviewDelete) {
			//CommentTargetType.REVIEW && relationReviewOwnerId
			updateDataForWithdraw(
					CommentDO.class, withdrawKey,
					"relationReviewOwnerId", communityUserId,
					"targetType", CommentTargetType.REVIEW);
		}
		if (force || qaDelete) {
			//CommentTargetType.QUESTION_ANSWER && relationQuestionAnswerOwnerId
			updateDataForWithdraw(
					CommentDO.class, withdrawKey,
					"relationQuestionAnswerOwnerId", communityUserId,
					"targetType", CommentTargetType.QUESTION_ANSWER);
			//CommentTargetType.QUESTION_ANSWER && relationQuestionOwnerId
			updateDataForWithdraw(
					CommentDO.class, withdrawKey,
					"relationQuestionOwnerId", communityUserId,
					"targetType", CommentTargetType.QUESTION_ANSWER);
		}
		if (force || imageDelete) {
			//CommentTargetType.IMAGE && relationImageOwnerId
			updateDataForWithdraw(
					CommentDO.class, withdrawKey,
					"relationImageOwnerId", communityUserId,
					"targetType", CommentTargetType.IMAGE);
		}
		if (force || commentDelete) {
			//communityUserId
			updateDataForWithdraw(
					CommentDO.class, withdrawKey,
					"communityUserId", communityUserId);
		}

		//    コミュニティユーザーフォロー（CommunityUserFollowDO）
		updateDataForWithdraw(
				CommunityUserFollowDO.class, withdrawKey,
				"communityUserId", communityUserId);
		updateDataForWithdraw(
				CommunityUserFollowDO.class, withdrawKey,
				"followCommunityUserId", communityUserId);

		//    画像ヘッダー（ImageHeaderDO）
		//PostContentType.PROFILE && ownerCommunityUserId
		updateImageForWithdraw(withdrawKey,
				"ownerCommunityUserId",
				communityUserId,
				PostContentType.PROFILE);
		//PostContentType.PROFILE_THUMBNAIL && ownerCommunityUserId
		updateImageForWithdraw(withdrawKey,
				"ownerCommunityUserId",
				communityUserId,
				PostContentType.PROFILE_THUMBNAIL);
		if (force || reviewDelete) {
			//PostContentType.REVIEW && ownerCommunityUserId
			updateImageForWithdraw(withdrawKey,
					"ownerCommunityUserId",
					communityUserId,
					PostContentType.REVIEW);
		}
		if (force || qaDelete) {
			//PostContentType.QUESTION && ownerCommunityUserId
			updateImageForWithdraw(withdrawKey,
					"ownerCommunityUserId",
					communityUserId,
					PostContentType.QUESTION);
			//PostContentType.ANSWER && ownerCommunityUserId
			updateImageForWithdraw(withdrawKey,
					"ownerCommunityUserId",
					communityUserId,
					PostContentType.ANSWER);
			//PostContentType.ANSWER && relationQuestionOwnerId
			updateImageForWithdraw(withdrawKey,
					"relationQuestionOwnerId",
					communityUserId,
					PostContentType.ANSWER);
		}
		if (force || imageDelete) {
			//PostContentType.IMAGE_SET && ownerCommunityUserId
			updateImageForWithdraw(withdrawKey,
					"ownerCommunityUserId",
					communityUserId,
					PostContentType.IMAGE_SET);
		}

		//    いいね（LikeDO）
		//communityUserId
		updateDataForWithdraw(
				LikeDO.class, withdrawKey,
				"communityUserId", communityUserId);
		if (force || reviewDelete) {
			//relationReviewOwnerId && LikeTargetType.REVIEW
			updateDataForWithdraw(
					LikeDO.class, withdrawKey,
					"relationReviewOwnerId", communityUserId,
					"targetType", LikeTargetType.REVIEW);
		}
		if (force || qaDelete) {
			//relationQuestionAnswerOwnerId && LikeTargetType.QUESTION_ANSWER
			updateDataForWithdraw(
					LikeDO.class, withdrawKey,
					"relationQuestionAnswerOwnerId", communityUserId,
					"targetType", LikeTargetType.QUESTION_ANSWER);
			//relationQuestionOwnerId && LikeTargetType.QUESTION_ANSWER
			updateDataForWithdraw(
					LikeDO.class, withdrawKey,
					"relationQuestionOwnerId", communityUserId,
					"targetType", LikeTargetType.QUESTION_ANSWER);
		}
		if (force || imageDelete) {
			//relationImageOwnerId && LikeTargetType.IMAGE
			updateDataForWithdraw(
					LikeDO.class, withdrawKey,
					"relationImageOwnerId", communityUserId,
					"targetType", LikeTargetType.IMAGE);
		}
		//    参考になった（VotingDO）
		updateDataForWithdraw(
				VotingDO.class, withdrawKey,
				"communityUserId", communityUserId);
		if (force || reviewDelete) {
			//relationReviewOwnerId && LikeTargetType.REVIEW
			updateDataForWithdraw(
					VotingDO.class, withdrawKey,
					"relationReviewOwnerId", communityUserId,
					"targetType", LikeTargetType.REVIEW);
		}
		if (force || qaDelete) {
			//relationQuestionAnswerOwnerId && LikeTargetType.QUESTION_ANSWER
			updateDataForWithdraw(
					VotingDO.class, withdrawKey,
					"relationQuestionAnswerOwnerId", communityUserId,
					"targetType", LikeTargetType.QUESTION_ANSWER);
			//relationQuestionOwnerId && LikeTargetType.QUESTION_ANSWER
			updateDataForWithdraw(
					VotingDO.class, withdrawKey,
					"relationQuestionOwnerId", communityUserId,
					"targetType", LikeTargetType.QUESTION_ANSWER);
		}
		if (force || imageDelete) {
			//relationImageOwnerId && LikeTargetType.IMAGE
			updateDataForWithdraw(
					VotingDO.class, withdrawKey,
					"relationImageOwnerId", communityUserId,
					"targetType", LikeTargetType.IMAGE);
		}
		
		//    メール設定（MailSettingDO）
		updateDataForWithdraw(
				MailSettingDO.class, withdrawKey,
				"communityUserId", communityUserId);

		//    商品フォロー（ProductFollowDO）
		updateDataForWithdraw(
				ProductFollowDO.class, withdrawKey,
				"communityUserId", communityUserId);

		//    商品マスター（ProductMasterDO）
		updateDataForWithdraw(
				ProductMasterDO.class, withdrawKey,
				"communityUserId", communityUserId);

		if (force) {
			//    購入商品（PurchaseProductDO）
			updateDataForWithdraw(
					PurchaseProductDO.class, withdrawKey,
					"communityUserId", communityUserId);
		}

		//    質問回答（QuestionAnswerDO）
		if (force || qaDelete) {
			updateDataForWithdraw(
					QuestionAnswerDO.class, withdrawKey,
					"communityUserId", communityUserId);
			updateDataForWithdraw(
					QuestionAnswerDO.class, withdrawKey,
					"relationQuestionOwnerId", communityUserId);
		}

		//    質問フォロー（QuestionFollowDO）
		updateDataForWithdraw(
				QuestionFollowDO.class, withdrawKey,
				"communityUserId", communityUserId);
		if (force || qaDelete) {
			updateDataForWithdraw(
					QuestionFollowDO.class, withdrawKey,
					"relationQuestionOwnerId", communityUserId);
		}

		//    質問（QuestionDO）
		if (force || qaDelete) {
			updateDataForWithdraw(
					QuestionDO.class, withdrawKey,
					"communityUserId", communityUserId);
		}

		//    レビュー（ReviewDO）
		if (force || reviewDelete) {
			for (ReviewDO review : hBaseOperations.scanWithIndex(
					ReviewDO.class, "communityUserId", communityUserId,
					hBaseOperations.createFilterBuilder(ReviewDO.class
					).appendSingleColumnValueFilter(
					"effective",
					CompareOp.EQUAL, true).toFilter())) {
				if (review.getStatus().equals(ContentsStatus.SUBMITTED)
						&& review.getPointGrantRequestId() != null) {
					CancelPointGrantType cancelReasonType = null;
					if (byEcWithdraw) {
						cancelReasonType = CancelPointGrantType.EC_WITHDRAWAL;
					} else if (force) {
						cancelReasonType = CancelPointGrantType.COMMUNITY_FORCED_WITHDRAWAL;
					} else {
						cancelReasonType = CancelPointGrantType.COMMUNITY_WITHDRAWAL;
					}
					simplePmsDao.cancelPointGrant(
							review.getPointGrantRequestId(),
							cancelReasonType);
					review.setCancelPointGrantType(cancelReasonType);
					hBaseOperations.save(review);
				}
			}
			updateDataForWithdraw(
					ReviewDO.class, withdrawKey,
					"communityUserId", communityUserId);
			updateDataForWithdraw(
					ReviewDecisivePurchaseDO.class, withdrawKey,
					"communityUserId", communityUserId);
			updateDataForWithdraw(
					PurchaseLostProductDO.class, withdrawKey,
					"communityUserId", communityUserId);
			updateDataForWithdraw(
					UsedProductDO.class, withdrawKey,
					"communityUserId", communityUserId);
		}

		//    レビュー履歴（ReviewHistoryDO）
		if (force || reviewDelete) {
			updateDataForWithdraw(
					ReviewHistoryDO.class, withdrawKey,
					"communityUserId", communityUserId);
		}

		//    ソーシャル連携設定（SocialMediaSettingDO）
		updateDataForWithdraw(
				SocialMediaSettingDO.class, withdrawKey,
				"communityUserId", communityUserId);

		//    スパム報告（SpamReportDO）
		if (force || reviewDelete || qaDelete || imageDelete || commentDelete) {
			updateDataForWithdraw(
					SpamReportDO.class, withdrawKey,
					"communityUserId", communityUserId);
			if (force || reviewDelete) {
				//SpamReportGroupType.REVIEW && relationReviewOwnerId
				updateDataForWithdraw(
						SpamReportDO.class, withdrawKey,
						"relationReviewOwnerId", communityUserId,
						"groupType", SpamReportGroupType.REVIEW);
			}
			if (force || qaDelete) {
				//SpamReportGroupType.QUESTION && relationReviewOwnerId
				updateDataForWithdraw(
						SpamReportDO.class, withdrawKey,
						"relationReviewOwnerId", communityUserId,
						"groupType", SpamReportGroupType.QUESTION);
				//SpamReportGroupType.QUESTION && relationReviewOwnerId
				updateDataForWithdraw(
						SpamReportDO.class, withdrawKey,
						"relationReviewOwnerId", communityUserId,
						"groupType", SpamReportGroupType.QUESTION);
			}
			if (force || imageDelete) {
				//SpamReportGroupType.IMAGE && relationImageOwnerId
				updateDataForWithdraw(
						SpamReportDO.class, withdrawKey,
						"relationImageOwnerId", communityUserId,
						"groupType", SpamReportGroupType.IMAGE);
			}
			if (force || commentDelete) {
				//SpamReportTargetType.COMMENT && relationCommentOwnerId
				updateDataForWithdraw(
						SpamReportDO.class, withdrawKey,
						"relationCommentOwnerId", communityUserId,
						"targetType", SpamReportTargetType.COMMENT);
			}
		}

		CommunityUserDO communityUser = new CommunityUserDO();
		communityUser.setCommunityUserId(communityUserId);
		communityUser.setWithdrawLock(false);
		communityUser.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(communityUser,
				Path.includeProp("modifyDateTime,withdrawLock"));
	}

	public Set<String> getStopCommunityUserIds(List<? extends StoppableContents> contents) {
		return getStopCommunityUserIds(contents, new HashSet<String>());
	}


	public Set<String> getStopCommunityUserIds(
			List<? extends StoppableContents> contents,
			Set<String> stopCommunityUserIds) {

		String loginCommunityUserId = requestScopeDao.loadCommunityUserId();
		if (contents == null) {
			return stopCommunityUserIds;
		}

		Set<String> checkedIds = new HashSet<String>();
		Set<String> checkIds = new HashSet<String>();

		for(StoppableContents content:contents){
			boolean stop = false;
			List<CommunityUserDO> relationOwners = content.getRelationOwners();
			if (relationOwners != null) {
				for (CommunityUserDO communityUser : relationOwners) {
					if (communityUser == null) {
						continue;
					}
					if (communityUser.getStatus() == null) {
						if (!checkedIds.contains(communityUser.getCommunityUserId())) {
							checkIds.add(communityUser.getCommunityUserId());
						}
					} else if (communityUser.isStop()) {
						stopCommunityUserIds.add(communityUser.getCommunityUserId());
						if (loginCommunityUserId == null ||
								!loginCommunityUserId.equals(communityUser.getCommunityUserId())) {
							stop = true;
						}
					} else {
						checkedIds.add(communityUser.getCommunityUserId());
					}
				}
			}
			if (stop) {
				continue;
			}
			List<String> relationOwnerIds = content.getRelationOwnerIds();
			if (relationOwnerIds != null) {
				for (String communityUserId : relationOwnerIds) {
					if (communityUserId == null) {
						continue;
					}
					if (!checkedIds.contains(communityUserId)) {
						checkIds.add(communityUserId);
					}
				}
			}
			for (String checkId : checkIds) {
				if (stopCommunityUserIds.contains(checkId)) {
					if (loginCommunityUserId == null
							|| !loginCommunityUserId.equals(checkId)) {
						continue;
					}
				}
			}
		}

		if (checkIds.size() == 0) {
			return stopCommunityUserIds;
		}

		StringBuilder buffer = new StringBuilder();
		buffer.append("status_s:");
		buffer.append(SolrUtil.escape(CommunityUserStatus.STOP.getCode()));
		buffer.append(" AND (");
		boolean first = true;
		for (String checkId : checkIds) {
			if (first) {
				first = false;
			} else {
				buffer.append(" OR ");
			}
			buffer.append("communityUserId:");
			buffer.append(SolrUtil.escape(checkId));
		}
		buffer.append(")");
		for (CommunityUserDO communityUser : solrOperations.findByQuery(new SolrQuery(
				buffer.toString()).setRows(checkIds.size()),
				CommunityUserDO.class,
				Path.includeProp("communityUserId")).getDocuments()) {
			stopCommunityUserIds.add(communityUser.getCommunityUserId());
		}
		return stopCommunityUserIds;
	}

	/**
	 * データを退会済みに更新します。
	 * @param type タイプ
	 * @param withdrawKey 退会キー
	 * @param columnName カラム名
	 * @param communityUserId コミュニティユーザーID
	 * @param secondColumnName ２次絞り込みカラム名
	 * @param secondValue ２次絞り込みカラム値
	 */
	private void updateDataForWithdraw(
			Class<?> type,
			String withdrawKey,
			String columnName,
			String communityUserId) {
		updateDataForWithdraw(type, withdrawKey,
				columnName, communityUserId, null, null);
	}

	/**
	 * データを退会済みに更新します。
	 * @param type タイプ
	 * @param withdrawKey 退会キー
	 * @param columnName カラム名
	 * @param communityUserId コミュニティユーザーID
	 * @param secondColumnName ２次絞り込みカラム名
	 * @param secondValue ２次絞り込みカラム値
	 */
	private void updateDataForWithdraw(
			Class<?> type,
			String withdrawKey,
			String columnName,
			String communityUserId,
			String secondColumnName,
			Object secondValue) {
		FilterList filterList = null;
		if (secondColumnName != null && secondValue != null) {
			filterList = hBaseOperations.createFilterBuilder(type
			).appendSingleColumnValueFilter(
					secondColumnName, CompareOp.EQUAL,
					secondValue).toFilter();
		}
		hBaseOperations.scanUpdateWithIndex(
				type, columnName, communityUserId,
				UpdateColumns.set("withdraw", true
						).andSet("withdrawKey", withdrawKey)
						.andSet("modifyDateTime", timestampHolder.getTimestamp()),
						filterList);

	}

	/**
	 * 退会予定データを検索します。
	 * @param type タイプ
	 * @param columnName カラム名
	 * @param communityUserId コミュニティユーザーID
	 * @param secondColumnName ２次絞り込みカラム名
	 * @param secondValue ２次絞り込みカラム値
	 */
	private <E> List<E> scanForWithdraw(
			Class<E> type,
			String columnName,
			String communityUserId) {
		return scanForWithdraw(type,
				columnName, communityUserId, null, null);
	}

	/**
	 * 退会予定データを検索します。
	 * @param type タイプ
	 * @param columnName カラム名
	 * @param communityUserId コミュニティユーザーID
	 * @param secondColumnName ２次絞り込みカラム名
	 * @param secondValue ２次絞り込みカラム値
	 */
	private <E> List<E> scanForWithdraw(
			Class<E> type,
			String columnName,
			String communityUserId,
			String secondColumnName,
			Object secondValue) {
		FilterList filterList = null;
		if (secondColumnName != null && secondValue != null) {
			filterList = hBaseOperations.createFilterBuilder(type
			).appendSingleColumnValueFilter(
					secondColumnName, CompareOp.EQUAL,
					secondValue).toFilter();
		}
		return hBaseOperations.scanWithIndex(
				type, columnName, communityUserId, filterList);

	}

	/**
	 * 退会状態のデータをキャンセルします。
	 * @param type タイプ
	 * @param withdrawKey 退会キー
	 * @return 更新対象キーリスト
	 */
	private List<String> cancelWithdraw(
			Class<?> type,
			String withdrawKey) {
		return hBaseOperations.scanUpdateWithIndexReturningKeys(
				type, "withdrawKey", withdrawKey,
				UpdateColumns.set("withdraw", false
						).andSet("withdrawKey", null)
						.andSet("modifyDateTime", timestampHolder.getTimestamp()),
						String.class);
	}

	/**
	 * 画像データを退会済みに更新します。
	 * @param withdrawKey 退会キー
	 * @param columnName カラム名
	 * @param communityUserId コミュニティユーザーID
	 * @param postContentType 投稿タイプ
	 */
	private void updateImageForWithdraw(
			String withdrawKey,
			String columnName,
			String communityUserId,
			PostContentType postContentType) {
		for (ImageHeaderDO imageHeader :
			hBaseOperations.scanWithIndex(
					ImageHeaderDO.class,
					columnName, communityUserId,
					hBaseOperations.createFilterBuilder(ImageHeaderDO.class
					).appendSingleColumnValueFilter(
							"postContentType", CompareOp.EQUAL,
							postContentType
							).appendSingleColumnValueFilter(
									"status", CompareOp.NOT_EQUAL,
									ContentsStatus.DELETE).toFilter(),
									Path.includeProp(
											"imageId,imageUrl,status,syncError,imageDeleteResult," +
											"postContentType," +
											"ownerCommunityUserId," +
											"relationOwnerId"))) {
			//画像キャッシュ削除
			ImageDeleteResult imageDeleteResult = null;
			if (imageHeader.getStatus().equals(ContentsStatus.SUBMITTED)) {
				String remoteFilePath = imageHeader.getImageUrl().substring(
						imageHeader.getImageUrl().lastIndexOf(
								resourceConfig.imageUploadPath));
				int index = remoteFilePath.lastIndexOf("/");
				String remoteDir = remoteFilePath.substring(0, index);
				String remoteFileName = remoteFilePath.substring(index + 1);
				imageDeleteResult = imageCacheDao.delete(remoteDir, remoteFileName);
				// TODO KDDI CDN 対応する
				imageCacheDao.clearCache(
						imageHeader.getImageUrl().substring(resourceConfig.imageUrl.length()));
				LOG.info("image delete success. communityUserId=" + imageHeader.getOwnerCommunityUserId()
						+ ", imageId=" + imageHeader.getImageId()
						+ ", imageUrl=" + imageHeader.getImageUrl());
			}

			imageHeader.setWithdraw(true);
			imageHeader.setWithdrawKey(withdrawKey);
			if (imageDeleteResult != null) {
				imageHeader.setImageDeleteResult(imageDeleteResult);
				if (imageDeleteResult.equals(ImageDeleteResult.SUCCESS)) {
					imageHeader.setImageSyncStatus(ImageSyncStatus.SYNC);
				} else {
					imageHeader.setImageSyncStatus(ImageSyncStatus.ERROR);
				}
			}

			hBaseOperations.save(imageHeader,
					Path.includeProp("withdraw,withdrawKey,imageSyncStatus,imageDeleteResult"));

			ImageDO image = new ImageDO();
			image.setImageId(imageHeader.getImageId());
			image.setWithdraw(true);
			image.setWithdrawKey(withdrawKey);
			image.setModifyDateTime(timestampHolder.getTimestamp());
			hBaseOperations.save(image, Path.includeProp(
					"withdraw,withdrawKey,modifyDateTime"));
		}
	}

	/**
	 * 退会予定画像データを返します。
	 * @param columnName カラム名
	 * @param communityUserId コミュニティユーザーID
	 * @param postContentType 投稿タイプ
	 */
	private List<ImageHeaderDO> scanImageForWithdraw(
			String columnName,
			String communityUserId,
			PostContentType postContentType) {
		return hBaseOperations.scanWithIndex(
					ImageHeaderDO.class,
					columnName, communityUserId,
					hBaseOperations.createFilterBuilder(ImageHeaderDO.class
					).appendSingleColumnValueFilter(
							"postContentType", CompareOp.EQUAL,
							postContentType
							).appendSingleColumnValueFilter(
									"status", CompareOp.NOT_EQUAL,
									ContentsStatus.DELETE).toFilter());

	}

	@Override
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			limitTime=30,limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public CommunityUserDO loadCommunityUserByNormalizeCommunityName(String normalizeCommunityName){
		CommunityNameDO name = hBaseOperations.load(CommunityNameDO.class, normalizeCommunityName);
		if (name != null && StringUtils.isNotEmpty(name.getCommunityUserId())) {
			CommunityUserDO communityUser = hBaseOperations.load(CommunityUserDO.class,
					name.getCommunityUserId(), Path.includePath("*"));
			//退会しているかの判別は呼び出し元でするため、ここでは判定しません。
			if (communityUser != null) {
				return communityUser;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public void saveCommunityUser(CommunityUserDO communityUser) {
		hBaseOperations.save(communityUser);
		solrOperations.save(communityUser);
	}

	@Override
	public CommunityUserDO loadByCommunityId(String communityId,
			Condition path) {
		CommunityUserDO community = null;
		SolrQuery query = new SolrQuery("communityId_s:" + communityId);
		com.kickmogu.lib.core.domain.SearchResult<CommunityUserDO> communityUsers = solrOperations.findByQuery(query, CommunityUserDO.class, Path.includeProp("communityUserId"));
		if(communityUsers == null || communityUsers.getDocuments().isEmpty()){
			List<CommunityUserDO> loadCommunityUsers = hBaseOperations.scanAll(CommunityUserDO.class,
					hBaseOperations.createFilterBuilder(CommunityUserDO.class).appendSingleColumnValueFilter("communityId", CompareOp.EQUAL, communityId).toFilter(), Path.includeProp("communityUserId"));
			if(loadCommunityUsers == null || loadCommunityUsers.isEmpty()) {
				return null;
			}else if (loadCommunityUsers.size() > 1) {
				return null;
			}
			community = loadCommunityUsers.get(0);
		}else  if(communityUsers.getDocuments().size() > 1) {
			return null;
		}else {
			community = communityUsers.getDocuments().get(0);
		}
		return community;
	}
}
