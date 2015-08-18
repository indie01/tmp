/**
 * 
 */
package com.kickmogu.yodobashi.community.resource.dao;

import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipDetailDO;
import com.kickmogu.yodobashi.community.resource.domain.SlipHeaderDO;


/**
 * ショートURLを管理するDaoです。
 * @author hirabayashi
 *
 */
public interface MaintenanceDao {
	public <T> T load(Class<T> clazz, String key);
	public void saveSlipHeader(SlipHeaderDO slipHeader);
	public void saveSlipDetail(SlipDetailDO slipDetail);
	public void savePurchaseProduct(PurchaseProductDO purchaseProduct);
	public void saveCommunityUser(CommunityUserDO communityUser);	
//	public void saveComment(CommentDO comment);	
}
