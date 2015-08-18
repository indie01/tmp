package com.kickmogu.yodobashi.community.resource.dao;

import com.kickmogu.yodobashi.community.resource.domain.DecisivePurchaseDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;

public interface DecisivePurchaseDao {
	public DecisivePurchaseDO loadDecisivePurchase(String decisivePurchaseId);
	public boolean isExistDecisivePurchase(String sku, String decisivePurchaseName, String exclusiveId);
	public void modifyDecisivePurchase(String decisivePurchaseId, String decisivePurchaseName);
	public void removeDecisivePurchase(String decisivePurchaseId);
	public void checkDecisivePurchase(String decisivePurchaseId, boolean check);
	public SearchResult<DecisivePurchaseDO> findDecisivePurchase(
			String name,
			String sku,
			boolean delete,
			Boolean check,
			Boolean skuSort,
			Boolean modifyDateTimeSort,
			int limit,
			int offset
	);
}
