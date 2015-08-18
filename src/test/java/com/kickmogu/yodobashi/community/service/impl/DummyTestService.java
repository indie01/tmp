package com.kickmogu.yodobashi.community.service.impl;


public interface DummyTestService {

	void invokeXi();
	void invokeSendMessage(Invoker invoker);

	public abstract class Invoker {
		
		public abstract void invoke();
		
	}

}
