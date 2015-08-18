package com.kickmogu.yodobashi.community.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.resource.dao.AnnounceDao;
import com.kickmogu.yodobashi.community.resource.domain.AnnounceDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.AnnounceType;
import com.kickmogu.yodobashi.community.service.AnnounceService;

@Service
public class AnnounceServiceImpl implements AnnounceService {

	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(AnnounceServiceImpl.class);

	@Autowired
	private AnnounceDao announceDao;

	@Autowired
	private TimestampHolder timestampHolder;

	@Override
	@ArroundHBase
	@ArroundSolr
	public boolean isAgreement(String communityUserId) {
		AnnounceDO announce = announceDao.load(communityUserId, AnnounceType.PARTICIPATING_AGREEMENT);
		return (announce == null);
	}

	@Override
	@ArroundHBase
	@ArroundSolr
	public void registAgreement(String communityUserId) {
		AnnounceDO announce = new AnnounceDO();
		announce.setCommunityUserId(communityUserId);
		announce.setDeleteDate(timestampHolder.getTimestamp());
		announce.setDeleteFlag(true);
		announce.setType(AnnounceType.PARTICIPATING_AGREEMENT);
		announce.setModifyDateTime(timestampHolder.getTimestamp());
		announce.setRegisterDateTime(timestampHolder.getTimestamp());
		announceDao.create(announce);
	}
}
