package com.kickmogu.yodobashi.community.resource.test;

public interface TestService {
	void invoke(Invoker invoker);
	void invokeSolrCommitAfterProcess(Invoker invoker);
	void invokeSolrNoCommit(Invoker invoker);
	void invokeSolrRollback(Invoker invoker);
	public abstract class Invoker {
		
		public abstract void invoke();
		
	}
}
