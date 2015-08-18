package com.kickmogu.yodobashi.community.performance;

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;

import com.kickmogu.lib.core.utils.Reflections;
import com.kickmogu.yodobashi.community.resource.dao.ActionHistoryDao;
import com.kickmogu.yodobashi.community.service.UserService;

public class PerformanceTestViewer {

	
	public static void main(String[] args) {
		for (Class<?> clazz:Reflections.getClassesByPackage(ActionHistoryDao.class.getPackage())) {
			if (!clazz.isInterface()) continue;
			for (Method method:Reflections.getAllDeclaredMethods(clazz)) {
				PerformanceTest performanceTest = method.getAnnotation(PerformanceTest.class);
				if (performanceTest == null) continue;
				System.out.println(StringUtils.rightPad(clazz.getSimpleName() + "." + method.getName(), 64)  + performanceTest.frequency() + ":" + performanceTest.frequencyComment());
			}
		}
		
		System.out.println("===================================================================");
		
		for (Class<?> clazz:Reflections.getClassesByPackage(UserService.class.getPackage())) {
			if (!clazz.isInterface()) continue;
			for (Method method:Reflections.getAllDeclaredMethods(clazz)) {
				PerformanceTest performanceTest = method.getAnnotation(PerformanceTest.class);
				if (performanceTest == null) continue;
				System.out.println(StringUtils.rightPad(clazz.getSimpleName() + "." + method.getName(), 64)  + performanceTest.frequency() + ":" + performanceTest.frequencyComment());
		//		System.out.println(StringUtils.rightPad("", 64)  + "(" + StringUtils.join(performanceTest.refClassNames(),",") + ")");

			}
		}
	}
}
