package com.kickmogu.yodobashi.community.resource.aop;

import org.springframework.beans.factory.annotation.Autowired;

import com.kickmogu.lib.core.time.SystemTime;
import com.kickmogu.lib.solr.aop.AopSolrProcessContextHolderImpl;

//@Service @Aspect
public class CommunitySolrProcessContextHandler extends AopSolrProcessContextHolderImpl {
	
	@Override @Autowired
	public void setSystemTime(SystemTime systemTime) {
		super.setSystemTime(systemTime);
	}
	

}
