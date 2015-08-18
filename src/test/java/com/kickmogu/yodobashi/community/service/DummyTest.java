package com.kickmogu.yodobashi.community.service;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.service.impl.DummyTestService;
import com.kickmogu.yodobashi.community.service.impl.DummyTestService.Invoker;
@Ignore
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/serviceContext.xml")
public class DummyTest {


	@Autowired
	private DummyTestService service;


	@Test @Ignore
	public void testXi() {
		System.out.println("a");
		service.invokeXi();
		System.out.println("b");
	}

	@Test @Ignore
	public void sendMessage() {

		service.invokeSendMessage(new Invoker(){

			@Override
			public void invoke() {
				System.out.println("hello!!");
			}

		});
	}
}
