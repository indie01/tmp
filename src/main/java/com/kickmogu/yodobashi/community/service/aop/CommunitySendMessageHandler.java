package com.kickmogu.yodobashi.community.service.aop;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.apache.activemq.util.InetAddressUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.aop.NestedMethodAopHandler;
import com.kickmogu.lib.core.aop.NestedMethodEvent;
import com.kickmogu.lib.core.utils.Reflections;
import com.kickmogu.yodobashi.community.common.utils.AsyncUtil;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.domain.AsyncMessageDO;
import com.kickmogu.yodobashi.community.service.AsyncMessageService;
import com.kickmogu.yodobashi.community.service.annotation.SendMessage;

@Service @Aspect
public class CommunitySendMessageHandler implements InitializingBean {

	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger LOG = LoggerFactory.getLogger(CommunitySendMessageHandler.class);

	@Autowired
	public ResourceConfig resourceConfig;

	@Autowired
	private ApplicationContext applicationContext;

	/**
	 * 非同期メッセージサービスです。
	 */
	@Autowired
	private AsyncMessageService asyncMessageService;

	private boolean forceSyncNow;

	public void setForceSyncNow(boolean forceSyncNow) {
		this.forceSyncNow = forceSyncNow;
	}
	
	private ThreadLocal<Object> skipAsyncThreadLocal = new ThreadLocal<Object>();
	
	public void skipAsync() {
		skipAsyncThreadLocal.set(new Object());
	}

	@Around("@annotation(com.kickmogu.yodobashi.community.service.annotation.SendMessage)")
	public Object handle(final ProceedingJoinPoint jointPoint) throws Throwable {

		if (forceSyncNow) {
			return jointPoint.proceed();
		}
		
		if (skipAsyncThreadLocal.get() != null) {
			skipAsyncThreadLocal.remove();
			return jointPoint.proceed();
		}


		final Method method = Reflections.getMethodFromJointPoint(jointPoint);
		final SendMessage sendMessage = method.getAnnotation(SendMessage.class);

		LOG.debug("#######" + method.getName());
		LOG.debug("####### sendMessage" + ((null == sendMessage) ? "NULL":sendMessage.timing()));
		
		
		String tmpBeanName = null;
		for (Map.Entry<String, ?> entry:applicationContext.getBeansOfType(jointPoint.getThis().getClass()).entrySet()) {
			if (entry.getValue() == jointPoint.getThis()) {
				tmpBeanName = entry.getKey();
				break;
			}
		}
		
		if( tmpBeanName == null )
			return null;
		
		final String beanName = tmpBeanName;
		if (sendMessage.timing().isNow()) {
			return sendMessage(jointPoint, sendMessage, beanName, method, jointPoint.getArgs());
		} else {
			NestedMethodAopHandler.getContext().addEvent(new NestedMethodEvent() {
				@Override
				public void onAfterProcess() throws Throwable {
					sendMessage(jointPoint, sendMessage, beanName, method, jointPoint.getArgs());
				}
			});
			return null;
		}
	}


	private Object sendMessage(ProceedingJoinPoint jointPoint, SendMessage sendMessage, String beanName, Method method, Object[] args) throws Throwable {
		if (sendMessage.timing().isSync()) {
			return jointPoint.proceed();
		} else {
			ConnectionFactory connectionFactory = applicationContext.getBean(ConnectionFactory.class);
	        Connection connection = null;
	        Session session = null;
	        MessageProducer producer = null;

	        AsyncMessageDO asyncMessage = new AsyncMessageDO();
	        asyncMessage.setHandlerName(beanName + "." + method.getName());
	        asyncMessage.setData(AsyncUtil.serialize(args));
	        asyncMessage.setType(sendMessage.asyncMessageType());
	        asyncMessage.setDelayTime(sendMessage.delayTime());
	        asyncMessageService.createMessage(InetAddressUtil.getLocalHostName(),
	        		asyncMessage);
	        
	        try {
	        	connection = connectionFactory.createConnection();
		        session = connection.createSession(false, QueueSession.AUTO_ACKNOWLEDGE);
		        Queue queue = session.createQueue(sendMessage.asyncMessageType().getQueueName());
		        producer = session.createProducer(queue);


		        Message message = session.createObjectMessage(asyncMessage);

		        if (LOG.isDebugEnabled()) {
					LOG.debug("send messageId = " + asyncMessage.getMessageId()
							+ ", handlerName = " + asyncMessage.getHandlerName()
							+ ", params = " + Arrays.toString(args));
		        }
	        	producer.send(message);
	        } catch (Throwable t) {
		        if (LOG.isErrorEnabled()) {
					LOG.error("error messageId = " + asyncMessage.getMessageId()
							+ ", handlerName = " + asyncMessage.getHandlerName()
							+ ", params = " + Arrays.toString(args), t);
		        }
	        	throw t;
	        } finally {
	        	if (producer != null) {
	        		producer.close();
	        	}
	        	if (session != null) {
	        		session.close();
	        	}
	        	if (connection != null) {
	        		connection.close();
	        	}
	        }
	        return null;
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setForceSyncNow(resourceConfig.messageForceSyncNow);
	}

}
