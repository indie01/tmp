package com.kickmogu.yodobashi.community.service.aop;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import com.kickmogu.core.utils.ThreadLocalUtils;

@Service @Aspect 
public class PerformanceLog implements InitializingBean {
	
	public static Log log = LogFactory.getLog("PerformanceLog");
	
//	private static ThreadLocal<Context> contextHolder = new ThreadLocal<Context>(){
//
//		@Override
//		protected Context initialValue() {
//			return new Context();
//		}
//		
//	};

    
	@Pointcut("execution(* com.kickmogu..*DaoImpl.*(..))")
    private void anyDaoImpl() {};

    
	@Pointcut("execution(* com.kickmogu..*ServiceImpl.*(..))")
    private void anyServiceImpl() {};
    
	@Pointcut("execution(* com.kickmogu..*HBaseTemplate.*(..))")
    private void hbaseTemplate() {};

	@Pointcut("execution(* com.kickmogu..*SolrTemplate.*(..))")
    private void solrTemplate() {};    
    
    @Pointcut("execution(* org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter.handle(..))")
    private void anyWebHandle() {};
    
    private long thresholdMillsec = 0L;
   
	@Around("anyDaoImpl() || anyServiceImpl() || anyWebHandle() || hbaseTemplate() || solrTemplate()")
	public Object performanceLog(ProceedingJoinPoint joinPoint) throws Throwable {
		
		if (!log.isInfoEnabled()) {
			return joinPoint.proceed();
		}
		
		//Context context = contextHolder.get();
		Context context = ThreadLocalUtils.getThreadVariable(
				Context.class.getCanonicalName(), 
				Context.class);
		
		context.depth++;
		if (context.depth == 2) context.over2Depth = true;
		
		String methodName = joinPoint.getTarget().getClass().getSimpleName() + "." + joinPoint.getSignature().getName();

		long startTime = System.currentTimeMillis();
		try {
			Object result = joinPoint.proceed();
			long time = System.currentTimeMillis()-startTime;
			if (time >= thresholdMillsec) {
				log.info("INF|" + StringUtils.repeat(" ", context.depth) + methodName +"(" + time + "ms)");				
			}
			return result;
		} catch (Throwable th) {
			log.info("ERR|" + StringUtils.repeat(" ", context.depth) + methodName +"(" + (System.currentTimeMillis()-startTime) + "ms)");
			throw th;
		} finally {
			context.depth--;
			if (context.depth == 0) {
				if (context.over2Depth) {
					log.info("========================================================================");
				}
				context.over2Depth = false;
			}
		}
		
	}    
    

	public static class Context {
		private int depth;
		private boolean over2Depth;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (System.getProperty("performancelog.threshold.millisec")!=null) {
			thresholdMillsec = Long.valueOf(System.getProperty("performancelog.threshold.millisec"));
		}
	}
	
}
