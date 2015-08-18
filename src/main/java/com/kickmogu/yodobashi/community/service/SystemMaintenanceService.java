package com.kickmogu.yodobashi.community.service;

import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityOperationStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.MaintenanceStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.SimplePmsOperationStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.ValidateAuthSessionV2UpdateSessionStatus;

public interface SystemMaintenanceService {

	public void changePmsStatus(SimplePmsOperationStatus status);
	
	public SimplePmsOperationStatus getPmsStatus();
	
	public SimplePmsOperationStatus getPmsStatusWithCache();

	public void changeWebMaintenance(MaintenanceStatus status);
	
	public MaintenanceStatus getWebMaintenanceStatus();
	
	public MaintenanceStatus getWebMaintenanceStatusWithCache();
	
	public void changeWebProductMasterStatus(MaintenanceStatus status);
	public MaintenanceStatus getWebProductMasterStatus();
	public MaintenanceStatus getWebProductMasterStatusWithCache();

	public void changeWebBatchMailStatus(MaintenanceStatus status);
	public MaintenanceStatus getWebBatchMailStatus();
	public MaintenanceStatus getWebBatchMailStatusWithCache();

	public void changeSendMailStatus(String status);
	public String getSendMailStatus();
	public String getSendMailStatusWithCache();

	public void changeBatchNotifyUpdateStatus(MaintenanceStatus status);
	public MaintenanceStatus getBatchNotifyUpdateStatus();
	public MaintenanceStatus getBatchNotifyUpdateStatusWithCache();
	
	public void changeCommunityOperationStatus(CommunityOperationStatus status);
	public CommunityOperationStatus getCommunityOperationStatus();
	public CommunityOperationStatus getCommunityOperationStatusWithCache();
	
	public void changeAkamaiPassword(String password);
	public String getAkamaiPassword();
	
	public void changeUniversalHeaderSearchWindowStatus(MaintenanceStatus status);
	public MaintenanceStatus getUniversalHeaderSearchWindowStatus();
	
	public void changeValidateAuthSessionV2Mode(MaintenanceStatus status);
	public MaintenanceStatus getValidateAuthSessionV2Mode();
	
	public void changeValidateAuthSessionNormalModeFlagExpire(String expire);
	public Integer getValidateAuthSessionNormalModeFlagExpire();
	public Integer getValidateAuthSessionNormalModeFlagExpireWithCache();
	
	public void changeValidateAuthSessionUpdateSession(boolean updateSession);
	public ValidateAuthSessionV2UpdateSessionStatus getValidateAuthSessionUpdateSession();
}
