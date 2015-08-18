package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.kickmogu.yodobashi.community.common.exception.YcComException;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.dao.ReviewMetaDao;
import com.kickmogu.yodobashi.community.resource.domain.ReviewMetaDO;
/** 
 * レビューMeta情報を操作する実装
 * @author sugimoto
 *
 */
@Service
public class ReviewMetaDaoImpl implements ReviewMetaDao {
	
	@Autowired
	private ResourceConfig resourceConfig;
	
	/**
	 * 既定値のカテゴリコード
	 */
	private static final String DEFAULT_CATEGORYCODE = "000000000000000000";
	
	@Override
	public ReviewMetaDO getReviewMeta(List<String> categoryCodes) {
		ReviewMetaDO result = null;
		if( categoryCodes == null || categoryCodes.isEmpty()){
			result = resourceConfig.reviewMetas.get(DEFAULT_CATEGORYCODE);
			
			if( result == null )
				throw new YcComException("Get Review Meta Failed");
			
			return result;
		}
		
		for(String categoryCode : Lists.reverse(categoryCodes)){
			result = resourceConfig.reviewMetas.get(categoryCode);
			if( result != null)
				return result;
		}
		
		result = resourceConfig.reviewMetas.get(DEFAULT_CATEGORYCODE);
		
		if( result == null )
			throw new YcComException("Get Review Meta Failed");
		
		return result;
	}

}
