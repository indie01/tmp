package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.dao.LoginDao;
import com.kickmogu.yodobashi.community.resource.domain.LoginDO;

@Service
public class LoginDaoImpl implements LoginDao {

	private static final Logger LOG = LoggerFactory.getLogger(LoginDaoImpl.class);
	
	@Autowired  @Qualifier("default")
	private HBaseOperations hBaseOperations;
	
	@Autowired
	private ResourceConfig resourceConfig;
	
	@Autowired
	private TimestampHolder timestampHolder;
	
	@Override
	public void removeExpired() {
		
		LOG.info("start removeExpired. ");
		List<LoginDO> list = hBaseOperations.scanAll(LoginDO.class);
		LOG.info("full Size=" + list.size());

		List<String> expiredList = Lists.newArrayList();
		long current = System.currentTimeMillis();
		for (LoginDO loginDO:list) {
			if (loginDO.getLastAccessDate() == null || (current - loginDO.getLastAccessDate().getTime()) > resourceConfig.loginExpire) {
				expiredList.add(loginDO.getLoginId());
				LOG.info("expireLoginRecord:" + ToStringBuilder.reflectionToString(loginDO, ToStringStyle.SHORT_PREFIX_STYLE));
			}
		}
		LOG.info("expire Size=" + expiredList.size());
		
		for (List<String> targets:Lists.partition(expiredList, 100)) {
			hBaseOperations.deleteByKeys(LoginDO.class, String.class, targets);
		}
		LOG.info("finished");
	}

	@Override
	public LoginDO loadByAutoId(String autoId) {
		return hBaseOperations.load(LoginDO.class, autoId);
	}

	@Override
	public void save(LoginDO login) {
		login.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(login);
	}

	@Override
	public void removeLogin(String autoId) {
		hBaseOperations.deleteByKey(LoginDO.class, autoId);
	}
}
