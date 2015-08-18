package com.kickmogu.yodobashi.community.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.yodobashi.community.resource.dao.UniversalSessionManagerDao;
import com.kickmogu.yodobashi.community.service.annotation.SendMessage;
import com.kickmogu.yodobashi.community.service.annotation.SendMessage.Timing;

@Service
public class DummyTestServiceImpl implements DummyTestService{

	@Autowired @Qualifier("xi")
	private UniversalSessionManagerDao universalSessionManagerDao;

	@Override
	public void invokeXi() {
		System.out.println("DO DUMMYYYY");
		universalSessionManagerDao.deleteUniversalSession("hoge");
	}

	@Override @ArroundHBase @SendMessage(timing=Timing.SYNC_AFTER_PROCESS)
	public void invokeSendMessage(Invoker invoker) {
		System.out.println("enter");
		invoker.invoke();
		System.out.println("leave");
	}


}
