/**
 *
 */
package com.kickmogu.yodobashi.community.service.impl;

import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.apache.log4j.NDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.yodobashi.community.common.exception.YcComException;
import com.kickmogu.yodobashi.community.resource.dao.AsyncMessageDao;
import com.kickmogu.yodobashi.community.resource.domain.AsyncMessageDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.AsyncMessageStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.AsyncMessageType;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityOperationStatus;
import com.kickmogu.yodobashi.community.service.AsyncMessageService;
import com.kickmogu.yodobashi.community.service.SystemMaintenanceService;


/**
 * 非同期サービスの実装です。
 * @author kamiike
 *
 */
@Service
public class AsyncMessageServiceImpl implements AsyncMessageService {

	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AsyncMessageServiceImpl.class);

	/**
	 * アプリケーションコンテキストです。
	 */
	@Autowired
	private ApplicationContext applicationContext;

	/**
	 * 非同期メッセージ DAO です。
	 */
	@Autowired
	private AsyncMessageDao asyncMessageDao;
	
	/**
	 * システムメンテナンスサービスです。
	 */
	@Autowired
	private SystemMaintenanceService systemMaintenanceService;

	/**
	 * 非同期メッセージを保存します。
	 * @param appServerId アプリケーションサーバID
	 * @param message メッセージ
	 */
	@Override
	@ArroundHBase
	public void createMessage(String appServerId, AsyncMessageDO message) {
		AsyncMessageDO messageDO = new AsyncMessageDO();
		messageDO.setHandlerName(message.getHandlerName());
		messageDO.setData(message.getData());
		messageDO.setStatus(AsyncMessageStatus.ENTRY);
		messageDO.setType(message.getType());
		messageDO.setAppServerId(appServerId);
		messageDO.setTraceId(NDC.get());
		messageDO.setDelayTime(message.getDelayTime());
		asyncMessageDao.create(messageDO);
		message.setMessageId(messageDO.getMessageId());
	}

	/**
	 * 非同期メッセージ結果を登録します。
	 * @param consumerServerId 非同期サーバID
	 * @param message メッセージ
	 * @param success 成功した場合、true
	 */
	@Override
	@ArroundHBase
	public void saveMessageResult(
			String consumerServerId,
			AsyncMessageDO message,
			boolean success) {
		//成功した場合は、削除します。
		if (success) {
			asyncMessageDao.delete(message.getMessageId());
		//失敗した場合は、ステータスをエラーとして更新し、残します。
		} else {
			asyncMessageDao.updateStatus(
					message.getMessageId(),
					consumerServerId,
					AsyncMessageStatus.ERROR);
		}
	}

	/**
	 * メッセージを再実行します。
	 * @param type タイプ
	 * @param limit 最大実行数
	 * @param errorOnly エラーのみ
	 */
	@Override
	@ArroundHBase
	public void retryMessages(AsyncMessageType type,
			int limit, boolean errorOnly) {
		
		// ReadOnlyモードチェック
		if (systemMaintenanceService.getCommunityOperationStatus().equals(CommunityOperationStatus.READONLY_OPERATION)) {
			LOG.warn("Not entryMessages for " + CommunityOperationStatus.READONLY_OPERATION.getLabel());
			return;
		}
		
		entryMessages(type, asyncMessageDao.findByMessages(type, limit, errorOnly), errorOnly);
	}

	/**
	 * 指定したメッセージを再エントリーします。
	 * @param type タイプ
	 * @param asyncMessages メッセージリスト
	 * @param errorOnly エラーのみ
	 */
	private void entryMessages(
			AsyncMessageType type,
			List<AsyncMessageDO> asyncMessages,
			boolean errorOnly) {
		LOG.info("start reEntry. count = " + asyncMessages.size() + ", errorOnly=" + errorOnly + ", type = " + type.name());
		ConnectionFactory connectionFactory = applicationContext.getBean(ConnectionFactory.class);
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        try {
            try {
            	connection = connectionFactory.createConnection();
    	        session = connection.createSession(false, QueueSession.AUTO_ACKNOWLEDGE);
    	        Queue queue = session.createQueue(type.getQueueName());
    	        producer = session.createProducer(queue);

    	        for (AsyncMessageDO asyncMessage : asyncMessages) {
    	        	if (!asyncMessage.getType().equals(type)) {
    					LOG.warn("type not match. messageId = " + asyncMessage.getMessageId()
    							+ ", handlerName = " + asyncMessage.getHandlerName());
    					continue;
    	        	}
    		        Message message = session.createObjectMessage(asyncMessage);

    		        if (LOG.isInfoEnabled()) {
    					LOG.info("entry messageId = " + asyncMessage.getMessageId()
    							+ ", handlerName = " + asyncMessage.getHandlerName());
    		        }
    	        	producer.send(message);
    	        }
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
        } catch (Throwable t) {
        	throw new YcComException(t);
        }
		LOG.info("complete reEntry. count = " + asyncMessages.size() + ", errorOnly=" + errorOnly + ", type = " + type.name());
	}
}
