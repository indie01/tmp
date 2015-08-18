package com.kickmogu.yodobashi.community.service;

import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipHeaderDO;


public interface MngMaintenanceService {
	public <T> T load(Class<T> clazz, String key);
	public void saveSlipHeader(SlipHeaderDO slipHeader);
	public void saveSlipDetail(SlipDetailDO slipDetail);
	public void savePurchaseProduct(PurchaseProductDO purchaseProduct);
	public void saveCommunityUser(CommunityUserDO communityUser);
}
