package com.kickmogu.yodobashi.community.service.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Service;

@Service @Aspect
public class ServicePointCuts {
	
    @Pointcut("execution(* com.kickmogu.yodobashi.community.service.impl.*Impl.*(..))")
    public void service() {}
    
}
