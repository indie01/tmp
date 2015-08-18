/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl.dummy;

import java.lang.reflect.Field;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.stereotype.Service;
import org.springframework.util.StringValueResolver;

import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.core.utils.AopUtil;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.yodobashi.community.resource.annotation.BackendWebServiceClientAware;
import com.kickmogu.yodobashi.community.resource.config.DomainConfig;
import com.kickmogu.yodobashi.community.resource.dao.CommunityUserDao;
import com.kickmogu.yodobashi.community.resource.dao.OuterCustomerDao;
import com.kickmogu.yodobashi.community.resource.dao.impl.xi.XiOuterCustomerDaoImpl;
import com.kickmogu.yodobashi.community.resource.domain.AccountSharingDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.HashCommunityIdDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityUserStatus;


/**
 * 外部顧客情報 DAO ダミーです。
 * @author kamiike
 *
 */
@Service @Qualifier("dummy") @BackendWebServiceClientAware
public class DummyOuterCustomerDaoImpl extends XiOuterCustomerDaoImpl implements EmbeddedValueResolverAware, InitializingBean {

	/**
	 * ドメインコンフィグです。
	 */
	@Autowired
	private DomainConfig domainConfig;

	/**
	 * HBaseアクセサです。
	 */
	@Autowired  @Qualifier("default")
	private HBaseOperations hBaseOperations;

	/**
	 * アプリケーションコンテキストです。
	 */
	@Autowired
	private ApplicationContext applicationContext;

	/**
	 * 外部顧客情報 DAO です。
	 */
	@Autowired @Qualifier("xi")
	private OuterCustomerDao outerCustomerDao;

	/**
	 * プロパティリゾルバです。
	 */
	protected StringValueResolver resolver;

	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger LOG = LoggerFactory.getLogger(DummyOuterCustomerDaoImpl.class);

	@Override
	public void setEmbeddedValueResolver(StringValueResolver resolver) {
		this.resolver = resolver;
	}

	/**
	 * 指定したコミュニティID（外部顧客ID）のステータスを返します。
	 * @param outerCustomerId 外部顧客ID
	 * @return ステータス
	 */
	@Override
	public CommunityUserStatus loadCommunityUserStatusByOuterCustomerId(String outerCustomerId) {
		super.loadCommunityUserStatusByOuterCustomerId(outerCustomerId);
		HashCommunityIdDO id = hBaseOperations.load(HashCommunityIdDO.class,
				domainConfig.createHashCommunityId(outerCustomerId));
		CommunityUserDO communityUser = hBaseOperations.load(CommunityUserDO.class, id.getCommunityUserId(),
					Path.includePath(
							"status"));
		return communityUser.getStatus();
	}

	/**
	 * 指定したコミュニティID（外部顧客ID）のステータスを更新します。
	 * @param communityId コミュニティID（外部顧客ID）
	 * @param status ステータス
	 */
	@Override
	public void updateCustomerIdStatus(String communityId, CommunityUserStatus status) {
		outerCustomerDao.updateCustomerIdStatus(communityId, status);
	}

	/**
	 * 指定した外部顧客IDの共有化情報を取得して返します。
	 * @param outerCustomerId 外部顧客ID
	 * @return 共有化情報リスト
	 */
	@Override
	public List<AccountSharingDO> findAccountSharingByOuterCustomerId(
			String outerCustomerId) {
		return outerCustomerDao.findAccountSharingByOuterCustomerId(outerCustomerId);
	}

	@Override
	public String createCommunityId(String universalSessionID) {
		if (universalSessionID.startsWith("test")) {
			return universalSessionID.replaceAll("test", "");
		} else {
			return super.createCommunityId(universalSessionID);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			CommunityUserDao communityUserDao = applicationContext.getBean(CommunityUserDao.class);
			CommunityUserDao target = AopUtil.getTargetObject(communityUserDao, CommunityUserDao.class);
			Field field = target.getClass().getDeclaredField("outerCustomerDao");
			boolean useDummy = Boolean.valueOf(resolver.resolveStringValue("${use.dummyOuterCustomerDaoImpl}"));
			try  {
				useDummy = Boolean.valueOf(resolver.resolveStringValue("${"
						+ System.getProperty("user.name") + ".use.dummyOuterCustomerDaoImpl}"));
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
