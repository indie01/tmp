/**
 *
 */
package com.kickmogu.yodobashi.community.service.impl.mng;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.resource.dao.MaintenanceDao;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipHeaderDO;
import com.kickmogu.yodobashi.community.service.MngMaintenanceService;

@Service
public class MngMaintenanceServiceImpl implements MngMaintenanceService{

	@Autowired private MaintenanceDao maintenanceDao;
	
	@Override
	public <T> T load(Class<T> clazz, String key) {
		return maintenanceDao.load(clazz, key);
	}

	@Override
	@ArroundHBase
	@ArroundSolr
	public void saveSlipHeader(SlipHeaderDO slipHeader){
		maintenanceDao.saveSlipHeader(slipHeader);
	}
	
	@Override
	@ArroundHBase
	@ArroundSolr
	public void saveSlipDetail(SlipDetailDO slipDetail){
		maintenanceDao.saveSlipDetail(slipDetail);
	}

	@Override
	@ArroundHBase
	@ArroundSolr
	public void savePurchaseProduct(PurchaseProductDO purchaseProduct) {
		maintenanceDao.savePurchaseProduct(purchaseProduct);
		
	}

	@Override
	@ArroundHBase
	@ArroundSolr
	public void saveCommunityUser(CommunityUserDO communityUser) {
		maintenanceDao.saveCommunityUser(communityUser);
	}
}
