package com.kickmogu.yodobashi.community.resource.test;

import org.springframework.stereotype.Service;

import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.lib.solr.annotation.SolrTiming;

@Service
public class TestServiceImpl implements TestService {
	
	@ArroundHBase
	public void invoke(Invoker invoker) {
		invoker.invoke();
	}

	@ArroundSolr @ArroundHBase
	public void invokeSolrCommitAfterProcess(Invoker invoker) {
		invoker.invoke();
	}

	@ArroundSolr(commit=SolrTiming.NONE,autoCommit=false) @ArroundHBase
	public void invokeSolrNoCommit(Invoker invoker) {
		invoker.invoke();
	}
	
	@ArroundSolr(rollbackOnException=true) @ArroundHBase
	public void invokeSolrRollback(Invoker invoker) {
		invoker.invoke();
	}

}
