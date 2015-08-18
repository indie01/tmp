package com.kickmogu.yodobashi.community.service.aop;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.stereotype.Service;
import org.springframework.util.StringValueResolver;

import com.google.common.collect.Sets;
import com.kickmogu.lib.core.utils.Reflections;
import com.kickmogu.yodobashi.community.service.config.ServiceConfig;

@Service @Aspect
public class OverrideServiceToUseDummyDao implements EmbeddedValueResolverAware {


	private static Logger log = org.slf4j.LoggerFactory.getLogger(OverrideServiceToUseDummyDao.class);

	@Autowired private ApplicationContext applicationContext;
	@Autowired private ServiceConfig config;

	protected StringValueResolver resolver;
	
	private Set<Object> dummyAutoWiredSet = Sets.newHashSet();

	@Override
	public void setEmbeddedValueResolver(StringValueResolver resolver) {
		this.resolver = resolver;
	}


	@Around("com.kickmogu.yodobashi.community.service.aop.ServicePointCuts.service()")
	public Object override(ProceedingJoinPoint jointPoint) throws Throwable {

		if (!config.enableDummyDao){
			return jointPoint.proceed();
		}


		final Object targetObject = jointPoint.getTarget();
		
		if (dummyAutoWiredSet.contains(targetObject)) {
			return jointPoint.proceed();
		}


		for (Field field:Reflections.getAllDeclaredFields(targetObject.getClass())) {
			if (field.getAnnotation(Autowired.class) == null) continue;
			for (Map.Entry<String, ?> entry : applicationContext.getBeansOfType(field.getType()).entrySet()) {
				if (!entry.getKey().startsWith("dummy")) continue;
				try {
					boolean useDummy = Boolean.valueOf(resolver.resolveStringValue("${use." + entry.getKey() + "}"));
					if (useDummy) {
						field.setAccessible(true);
						field.set(targetObject, entry.getValue());
						log.info("Service Overrided!!!! use " + entry.getKey());
					}
				} catch (IllegalArgumentException e) {}
			}
		}
		
		synchronized (dummyAutoWiredSet) {
			if (!dummyAutoWiredSet.contains(targetObject)) dummyAutoWiredSet.add(targetObject);
		}

		try {
			return jointPoint.proceed();
		} finally {
//			applicationContext.getAutowireCapableBeanFactory().autowireBeanProperties(targetObject, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
		}
	}


}
