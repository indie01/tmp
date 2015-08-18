package com.kickmogu.yodobashi.community.service.impl;

import java.util.ArrayList;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import com.kickmogu.yodobashi.community.resource.domain.PurchaseProductDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.service.UserPageCommonService;

/**
 * マイページとユーザーページの共通サービスです
 * 
 * @author kiryu
 *
 */
public abstract class UserPageCommonServiceImpl extends AbstractServiceImpl implements UserPageCommonService {

	
	/**
	 * 指定したコミュニティユーザーの購入商品を返します。<br/>
	 * 検索結果から非掲載品は取り除き、limitの数に達するまで処理を繰り返します。
	 * 
	 * @param communityUserId コミュニティユーザーID
	 * @param publicOnly 公開設定のもののみ取得する場合、true
	 * @param limit 最大取得件数
	 * @return 購入商品情報のリスト
	 */
	@Override
	public SearchResult<PurchaseProductDO> findPurchaseProductByCommunityUserId(
			String communityUserId, boolean publicOnly, int limit) {

		int offset = 0;
		SearchResult<PurchaseProductDO> searchResult = new SearchResult<PurchaseProductDO>(0, new ArrayList<PurchaseProductDO>());
		
		while (searchResult.getDocuments().size() < limit) {
			// 非掲載品での掲載落ちを考慮し、limit*1.5で検索する
			SearchResult<PurchaseProductDO> tmpSearchResult = 
					orderDao.findPurchaseProductByCommunityUserId(communityUserId, publicOnly, (int)(limit*1.5), offset);
			
			offset += tmpSearchResult.getDocuments().size();

			// 掲載不可商品は取り除く
			CollectionUtils.filter(tmpSearchResult.getDocuments(), new Predicate() {
				@Override
				public boolean evaluate(Object object) {
					PurchaseProductDO purchaseProductDO = (PurchaseProductDO) object;
					return purchaseProductDO != null && purchaseProductDO.getProduct() != null && purchaseProductDO.getProduct().isPublFlg();
				}
			});
			
			searchResult.setNumFound(tmpSearchResult.getNumFound());
			searchResult.getDocuments().addAll(tmpSearchResult.getDocuments());
			if (offset >= searchResult.getNumFound()) {
				break;
			}
		}
		if (searchResult.getDocuments().size() > limit) {
			searchResult.setDocuments(searchResult.getDocuments().subList(0, limit));
		}
		
		return searchResult;
	}

}
