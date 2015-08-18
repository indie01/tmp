/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl.dummy;

import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.stereotype.Service;
import org.springframework.util.StringValueResolver;

import com.kickmogu.lib.core.utils.AopUtil;
import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.SimplePmsDao;
import com.kickmogu.yodobashi.community.resource.domain.PointGrantEntryDO;
import com.kickmogu.yodobashi.community.resource.domain.PointGrantEntryExecuteStatusDO;
import com.kickmogu.yodobashi.community.resource.domain.PointGrantEntryExecuteStatusResponseDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewPointSpecialConditionValidateDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.CancelPointGrantType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointExchangeType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointGrantEntrySearchType;

/**
 * ポイント管理 DAO のダミーです。
 * @author kamiike
 *
 */
@Service @Qualifier("dummy")
public class DummySimplePmsDaoImpl implements SimplePmsDao, EmbeddedValueResolverAware, InitializingBean {

	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger LOG = LoggerFactory.getLogger(DummySimplePmsDaoImpl.class);

	/**
	 * ランダムインスタンスです。
	 */
	private static Random random;

	/**
	 * アプリケーションコンテキストです。
	 */
	@Autowired
	private ApplicationContext applicationContext;

	/**
	 * プロパティリゾルバです。
	 */
	protected StringValueResolver resolver;

	static {
		try {
			random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		} catch (Exception e) {
			random = new Random();
		}
	}

	@Override
	public void setEmbeddedValueResolver(StringValueResolver resolver) {
		this.resolver = resolver;
	}

	/**
	 * ポイント付与を申請します。
	 * @param externalCustomerId 外部顧客ID（コミュニティID）
	 * @param pointExchangeType ポイント交換種別（ポイント伝票タイプ）（01:レビュー投稿ポイント　02:ランキングポイント）
	 * @param pointGrantExecStartDate ポイント付与実行開始日（この日付以降でポイント付与実行を可能とする）
	 * @param pointValue ポイント数
	 * @param specialConditionCodes 特別ポイント条件コードリスト
	 * @return ポイント付与申請ID
	 */
	@Override
	public String entryPointGrant(
			String externalCustomerId,
			PointExchangeType pointExchangeType,
			Date pointGrantExecStartDate,
			Long pointValue,
			String[] specialConditionCodes) {
		StringBuilder id = new StringBuilder();
		for (int i = 0; i < 20; i++) {
			id.append(getRandomChar());
		}
		return id.toString();
	}

	/**
	 * 特別条件ポイントコードの付与を予約します。
	 * @param externalCustomerId 外部顧客ID（コミュニティID）
	 * @param specialConditionCode 特別ポイント条件コード
	 * @return 予約順、取れなかった場合、null
	 */
	@Override
	public Integer reserveSpecialArrivalPoint(
			String externalCustomerId,
			String specialConditionCode) {

		Integer reserveNo = 1;
		if("1000000026".equals(specialConditionCode)) reserveNo = null;
		if("1000000039".equals(specialConditionCode)) reserveNo = null;
		if("1000000040".equals(specialConditionCode)) reserveNo = null;
		if("1000000041".equals(specialConditionCode)) reserveNo = null;
		if("1000000027".equals(specialConditionCode)) reserveNo = 101;
		if("1000000028".equals(specialConditionCode)) reserveNo = 10000;
		if("1000000043".equals(specialConditionCode)) reserveNo = 9999;



		LOG.info("ReserveNo:" + reserveNo );
		return reserveNo;
	}

	
	/**
	 * 特別条件レビューポイントのチェックを行う。
	 * @param externalCustomerIdClass 外部顧客ID種別
	 * @param externalCustomerId 外部顧客ID
	 * @param specialConditionCodes 特別条件コード一覧
	 * @return 特別レビューポイントチェック一覧情報
	 */
	@Override
	public List<ReviewPointSpecialConditionValidateDO> confirmReviewPointSpecialCondition(
			String externalCustomerIdClass, String externalCustomerId,
			String[] specialConditionCodes) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * ポイント付与情報を移行します。
	 * @param externalCustomerId 外部顧客ID（コミュニティID）
	 * @param pointExchangeType ポイント交換種別（ポイント伝票タイプ）（01:レビュー投稿ポイント　02:ランキングポイント）
	 * @param pointGrantApprovalDate ポイント承認日時
	 * @param pointGrantDate ポイント付与日付
	 * @param pointValue ポイント数
	 * @return ポイント付与申請ID
	 */
	@Override
	public String migratePointGrant(
			String externalCustomerId,
			PointExchangeType pointExchangeType,
			Date pointGrantApprovalDate,
			Date pointGrantDate,
			Long pointValue) {
		StringBuilder id = new StringBuilder();
		for (int i = 0; i < 20; i++) {
			id.append(getRandomChar());
		}
		return id.toString();
	}

	/**
	 * ポイント付与申請を取り下げます。
	 * @param pointGrantRequestId ポイント付与申請ID
	 * @param cancelReasonType キャンセル理由タイプ
	 */
	@Override
	public void cancelPointGrant(
			String pointGrantRequestId,
			CancelPointGrantType cancelReasonType) {
	}

	/**
	 * ポイント付与情報を移行をキャンセルします。
	 * @param pointGrantRequestId ポイント付与申請ID
	 */
	@Override
	public void cancelMigratePointGrant(
			String pointGrantRequestId) {
	}

	@Override
	public SearchResult<PointGrantEntryDO> findMutablePointGrantEntry(
			String externalCustomerId,
			Set<String> pointGrantRequestIds,
			Set<PointGrantEntrySearchType> searchTypes, 
			Long limit, 
			Long offset) {
		SearchResult<PointGrantEntryDO> result = new SearchResult<PointGrantEntryDO>();
		List<PointGrantEntryDO> items = new ArrayList<PointGrantEntryDO>();
		PointGrantEntryDO item = null;
		if( searchTypes == null || searchTypes.isEmpty()){
			for(int i=0; i<100; i++){
				item = new PointGrantEntryDO();
				
				items.add(item);
			}
		}else{
			Iterator<PointGrantEntrySearchType> it = searchTypes.iterator();
			while( it.hasNext()){
				item = new PointGrantEntryDO();
				switch (it.next()) {
				case POINT_GRANT_TARGET:
					
					break;
				case POINT_GRANT_PERMIT:
					
					break;
				case POINT_GRANT_HOLD:
					
					break;
				case POINT_DEPRIVATION_TARGET:
					
					break;
				case POINT_DEPRIVATION_PERMIT:
					
					break;
				case POINT_DEPRIVATION_HOLD:
					
					break;
				default:
					continue;
				}
				
				items.add(item);
			}
		}
		
		result.setDocuments(items);
		result.setNumFound(1000);
		
		return result;
	}

	
	@Override
	@PerformanceTest(type = Type.UPDATE, frequency = Frequency.NONE, frequencyComment = "テスト対象外")
	public SearchResult<PointGrantEntryDO> findPointGrantEntry(
			Set<String> pointGrantRequestIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PointGrantEntryExecuteStatusResponseDO> updatePointGrantEntryExecuteStatus(
			List<PointGrantEntryExecuteStatusDO> pointGrantExecuteStatusList) {
		List<PointGrantEntryExecuteStatusResponseDO> items = new ArrayList<PointGrantEntryExecuteStatusResponseDO>();
		
		return items;
	}

	@Override
	public void openService() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeService() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Boolean isService() {
		// TODO Auto-generated method stub
		return Boolean.TRUE;
	}

	/**
	 * ランダムなアルファベットを返します。
	 * @return ランダムなアルファベット
	 */
	private char getRandomChar() {
		int i = random.nextInt(36);
		if (i < 10) {
			return (char) (i + 48);
		} else {
			return (char) (i + 55);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			CommunityUserDao communityUserDao = applicationContext.getBean(CommunityUserDao.class);
			CommunityUserDao target = AopUtil.getTargetObject(communityUserDao, CommunityUserDao.class);
			Field field = target.getClass().getDeclaredField("simplePmsDao");
			boolean useDummy = Boolean.valueOf(resolver.resolveStringValue("${use.dummySimplePmsDaoImpl}"));
			try  {
				useDummy = Boolean.valueOf(resolver.resolveStringValue("${"
						+ System.getProperty("user.name") + ".use.dummySimplePmsDaoImpl}"));
			} catch (IllegalArgumentException e) {
			}
			if (useDummy) {
				field.setAccessible(true);
				field.set(target, this);
				LOG.info("Service Overrided!!!! use " + this.getClass().getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
