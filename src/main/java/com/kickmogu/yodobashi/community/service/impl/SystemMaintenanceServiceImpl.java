package com.kickmogu.yodobashi.community.service.impl;


import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.hadoop.hbase.HBaseContainer;
import com.kickmogu.yodobashi.community.resource.config.AppConfigurationDao;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityOperationStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.MaintenanceStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.SimplePmsOperationStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ValidateAuthSessionV2UpdateSessionStatus;
import com.kickmogu.yodobashi.community.service.SystemMaintenanceService;

@Service
public class SystemMaintenanceServiceImpl implements SystemMaintenanceService {

	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger LOG = LoggerFactory.getLogger(SystemMaintenanceServiceImpl.class);

	@Autowired
	AppConfigurationDao appConfigurationDao;
	
	@Autowired @Qualifier("Site1")
	HBaseContainer container1;
	@Autowired @Qualifier("Site2")
	HBaseContainer container2;
	
	public static final String simplepmsOperationStatusKey = "simplepms.operation.status";
	public static final String webMaintenanceOperationStatusKey = "web.maintenance.operation.status";
	public static final String productMasterOperationStatusKey = "web.productmaster.operation.status";
	public static final String batchMailOperationStatusKey = "web.batchmail.operation.status";
	public static final String sendMailOperationStatusKey = "send.mail.mode";
	public static final String batchNotifyUpdateStatusKey = "batch.catalog.notify.update.status";
	public static final String iamgeSubmitStatusKey = "web.imagesubmit.operation.status";
	public static final String communityOperationStatusKey = "community.operation.mode";
	public static final String akamaiPasswordKey = "akamai.password";
	public static final String universalHeaderSearchWindowStatusKey = "universalheader.searchwindow.status";
	public static final String validateauthsessionv2Key = "validateauthsessionv2.mode";
	public static final String validateauthsessionv2NormalModeFlagExpireKey = "validateauthsessionv2.normalmode.flag.expire";
	public static final String validateauthsessionv2UpdateSession = "validateauthsessionv2.update.session";
	
	private static final Integer VALIDATEAUTHSESSIONNORMALMODEFLAGEXPIRE = 60 * 60;

	@Override
	public void changePmsStatus(SimplePmsOperationStatus status) {
		appConfigurationDao.set(simplepmsOperationStatusKey, SimplePmsOperationStatus.IN_OPERATION.getCode());
		
	}

	@Override
	public SimplePmsOperationStatus getPmsStatus() {
			String operationKey = appConfigurationDao.get(simplepmsOperationStatusKey);
			if(StringUtils.isEmpty(operationKey))
				operationKey = SimplePmsOperationStatus.IN_OPERATION.getCode();
		return SimplePmsOperationStatus.codeOf(operationKey);
	}

	@Override
	public SimplePmsOperationStatus getPmsStatusWithCache() {
		String operationKey = appConfigurationDao.getWithCache(simplepmsOperationStatusKey);
		if(StringUtils.isEmpty(operationKey))
			operationKey = SimplePmsOperationStatus.IN_OPERATION.getCode();
		return SimplePmsOperationStatus.codeOf(operationKey);
	}

	@Override
	public void changeWebMaintenance(MaintenanceStatus status) {
		appConfigurationDao.set(webMaintenanceOperationStatusKey, status.getCode());
	}

	@Override
	public MaintenanceStatus getWebMaintenanceStatus() {
		String operationKey = appConfigurationDao.get(webMaintenanceOperationStatusKey);
		if(StringUtils.isEmpty(operationKey))
			operationKey = MaintenanceStatus.IN_OPERATION.getCode();
	return MaintenanceStatus.codeOf(operationKey);
	}

	@Override
	public MaintenanceStatus getWebMaintenanceStatusWithCache() {
		String operationKey = appConfigurationDao.getWithCache(webMaintenanceOperationStatusKey);
		if(StringUtils.isEmpty(operationKey))
			operationKey = MaintenanceStatus.IN_OPERATION.getCode();
		return MaintenanceStatus.codeOf(operationKey);
	}

	@Override
	public void changeWebProductMasterStatus(MaintenanceStatus status) {
		appConfigurationDao.set(productMasterOperationStatusKey, status.getCode());
	}

	@Override
	public MaintenanceStatus getWebProductMasterStatus() {
		String operationKey = appConfigurationDao.get(productMasterOperationStatusKey);
		if(StringUtils.isEmpty(operationKey))
			operationKey = MaintenanceStatus.STOP_OPERATION.getCode();
		return MaintenanceStatus.codeOf(operationKey);
	}

	@Override
	public MaintenanceStatus getWebProductMasterStatusWithCache() {
		String operationKey = appConfigurationDao.getWithCache(productMasterOperationStatusKey);
		if(StringUtils.isEmpty(operationKey))
			operationKey = MaintenanceStatus.STOP_OPERATION.getCode();
		return MaintenanceStatus.codeOf(operationKey);
	}

	@Override
	public void changeWebBatchMailStatus(MaintenanceStatus status) {
		appConfigurationDao.set(batchMailOperationStatusKey, status.getCode());
	}

	@Override
	public MaintenanceStatus getWebBatchMailStatus() {
		String operationKey = appConfigurationDao.get(batchMailOperationStatusKey);
		if(StringUtils.isEmpty(operationKey))
			operationKey = MaintenanceStatus.STOP_OPERATION.getCode();
	return MaintenanceStatus.codeOf(operationKey);
	}

	@Override
	public MaintenanceStatus getWebBatchMailStatusWithCache() {
		String operationKey = appConfigurationDao.getWithCache(batchMailOperationStatusKey);
		if(StringUtils.isEmpty(operationKey))
			operationKey = MaintenanceStatus.STOP_OPERATION.getCode();
		return MaintenanceStatus.codeOf(operationKey);
	}
	
	
	@Override
	public void changeSendMailStatus(String status) {
		appConfigurationDao.set(sendMailOperationStatusKey, status);
	}

	@Override
	public String getSendMailStatus() {
		String operationKey = appConfigurationDao.get(sendMailOperationStatusKey);
		if(StringUtils.isEmpty(operationKey))
			operationKey = "dummy";
		return operationKey;
	}

	@Override
	public String getSendMailStatusWithCache() {
		String operationKey = appConfigurationDao.getWithCache(sendMailOperationStatusKey);
		if(StringUtils.isEmpty(operationKey))
			operationKey = "dummy";
		return operationKey;
	}
	
	@Override
	public void changeBatchNotifyUpdateStatus(MaintenanceStatus status) {
		appConfigurationDao.set(batchNotifyUpdateStatusKey, status.getCode());
	}
	
	@Override
	public MaintenanceStatus getBatchNotifyUpdateStatus() {
		String operationKey = appConfigurationDao.get(batchNotifyUpdateStatusKey);
		if (StringUtils.isEmpty(operationKey)) {
			operationKey = MaintenanceStatus.STOP_OPERATION.getCode();
		}
		return MaintenanceStatus.codeOf(operationKey);
	}
	
	@Override
	public MaintenanceStatus getBatchNotifyUpdateStatusWithCache() {
		String operationKey = appConfigurationDao.getWithCache(batchNotifyUpdateStatusKey);
		if (StringUtils.isEmpty(operationKey)) {
			operationKey = MaintenanceStatus.STOP_OPERATION.getCode();
		}
		return MaintenanceStatus.codeOf(operationKey);
	}

	@Override
	public void changeCommunityOperationStatus(CommunityOperationStatus status) {
		CommunityOperationStatus nowStatus = CommunityOperationStatus.codeOf(appConfigurationDao.get(communityOperationStatusKey));
		
		appConfigurationDao.set(communityOperationStatusKey, status.getCode());
		
		LOG.info("changeCommunityOperationStatus Now   :" + nowStatus);
		
		LOG.info("changeCommunityOperationStatus Change:" + status);
		
		if((CommunityOperationStatus.READONLY_OPERATION.equals(nowStatus) || CommunityOperationStatus.STOP_OPERATION.equals(nowStatus)) && CommunityOperationStatus.IN_OPERATION.equals(status)){
			if (container1 != null) container1.resetPool();
			LOG.info("HBase site1 reset pool");
			if (container2 != null) container2.resetPool();		
			LOG.info("HBase site2 reset pool");
		}
	}

	@Override
	public CommunityOperationStatus getCommunityOperationStatus() {
		String operationKey = appConfigurationDao.get(communityOperationStatusKey);
		if(StringUtils.isEmpty(operationKey))
			operationKey = CommunityOperationStatus.IN_OPERATION.getCode();
		return CommunityOperationStatus.codeOf(operationKey);
	}

	@Override
	public CommunityOperationStatus getCommunityOperationStatusWithCache() {
		String operationKey = appConfigurationDao.getWithCache(communityOperationStatusKey);
		if(StringUtils.isEmpty(operationKey))
			operationKey = CommunityOperationStatus.IN_OPERATION.getCode();
		return CommunityOperationStatus.codeOf(operationKey);
	}

	@Override
	public void changeAkamaiPassword(String password) {
		appConfigurationDao.set(akamaiPasswordKey, password);
	}

	@Override
	public String getAkamaiPassword() {
		return appConfigurationDao.get(akamaiPasswordKey);
	}

	@Override
	public void changeUniversalHeaderSearchWindowStatus(MaintenanceStatus status) {
		appConfigurationDao.set(universalHeaderSearchWindowStatusKey, status.getCode());
	}

	@Override
	public MaintenanceStatus getUniversalHeaderSearchWindowStatus() {
		String operationKey = appConfigurationDao.get(universalHeaderSearchWindowStatusKey);
		if(StringUtils.isEmpty(operationKey))
			return MaintenanceStatus.STOP_OPERATION;
		return MaintenanceStatus.codeOf(operationKey);
	}

	@Override
	public void changeValidateAuthSessionV2Mode(MaintenanceStatus status) {
		appConfigurationDao.set(validateauthsessionv2Key, status.getCode());
		
	}

	@Override
	public MaintenanceStatus getValidateAuthSessionV2Mode() {
		String operationKey = appConfigurationDao.get(validateauthsessionv2Key);
		if(StringUtils.isEmpty(operationKey))
			return MaintenanceStatus.STOP_OPERATION;
		return MaintenanceStatus.codeOf(operationKey);
	}

	@Override
	public void changeValidateAuthSessionNormalModeFlagExpire(String expire) {
		appConfigurationDao.set(validateauthsessionv2NormalModeFlagExpireKey, expire);
		
	}

	@Override
	public Integer getValidateAuthSessionNormalModeFlagExpire() {
		Integer result = appConfigurationDao.getAsInt(validateauthsessionv2NormalModeFlagExpireKey);
		if( result == null )
			return VALIDATEAUTHSESSIONNORMALMODEFLAGEXPIRE;
		
		return result;
	}

	@Override
	public Integer getValidateAuthSessionNormalModeFlagExpireWithCache() {
		Integer result = appConfigurationDao.getAsIntWithCache(validateauthsessionv2NormalModeFlagExpireKey);
		if( result == null )
			return VALIDATEAUTHSESSIONNORMALMODEFLAGEXPIRE;
		
		return result;
	}
	
	@Override
	public void changeValidateAuthSessionUpdateSession(boolean updateSession){
		appConfigurationDao.set(validateauthsessionv2UpdateSession, updateSession);
	}
	
	@Override
	public ValidateAuthSessionV2UpdateSessionStatus getValidateAuthSessionUpdateSession() {
		Boolean result = appConfigurationDao.getAsBoolean(validateauthsessionv2UpdateSession);
		if( result == null )
			return ValidateAuthSessionV2UpdateSessionStatus.DO_NOT_UPDATE;
		
		return result?ValidateAuthSessionV2UpdateSessionStatus.FULLTIME_UPDATE:ValidateAuthSessionV2UpdateSessionStatus.DO_NOT_UPDATE;
	}
}
