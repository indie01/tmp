/**
 *
 */
package com.kickmogu.yodobashi.community.resource.domain;

import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseIndex;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.yodobashi.community.resource.domain.constants.AsyncMessageStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.AsyncMessageType;

/**
 * 非同期メッセージです。
 * @author kamiike
 *
 */
@HBaseTable(columnFamilies={
	@HBaseColumnFamily(name="cf")
},sizeGroup=SizeGroup.TINY,excludeBackup=true)
public class AsyncMessageDO extends BaseWithTimestampDO {

	/**
	 *
	 */
	private static final long serialVersionUID = -6865909909029002501L;

	/**
	 * メッセージIDです。
	 */
	@HBaseKey
	private String messageId;

	/**
	 * アプリケーションサーバIDです。
	 */
	@HBaseColumn
	private String appServerId;

	/**
	 * 非同期サーバIDです。
	 */
	@HBaseColumn
	private String consumerServerId;

	/**
	 * ハンドラ名です。
	 */
	@HBaseColumn
	private String handlerName;

	/**
	 * データです。
	 */
	@HBaseColumn
	private byte[] data;

	/**
	 * 非同期メッセージタイプです。
	 */
	@HBaseColumn
	private AsyncMessageType type;

	/**
	 * 非同期メッセージステータスです。
	 */
	@HBaseColumn
	@HBaseIndex(additionalColumns={"appServerId", "consumerServerId", "type"})
	private AsyncMessageStatus status;
	
	@HBaseColumn
	private String traceId;
	
	@HBaseColumn
	private long delayTime;

	/**
	 * データを返します。
	 * @return データ
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * データを設定します。
	 * @param data データ
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

	/**
	 * ハンドラ名を返します。
	 * @return ハンドラ名
	 */
	public String getHandlerName() {
		return handlerName;
	}

	/**
	 * ハンドラ名を設定します。
	 * @param handlerName ハンドラ名
	 */
	public void setHandlerName(String handlerName) {
		this.handlerName = handlerName;
	}

	/**
	 * メッセージIDを返します。
	 * @return メッセージID
	 */
	public String getMessageId() {
		return messageId;
	}

	/**
	 * メッセージIDを設定します。
	 * @param messageId メッセージID
	 */
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	/**
	 * @return status
	 */
	public AsyncMessageStatus getStatus() {
		return status;
	}

	/**
	 * @param status セットする status
	 */
	public void setStatus(AsyncMessageStatus status) {
		this.status = status;
	}

	/**
	 * @return appServerId
	 */
	public String getAppServerId() {
		return appServerId;
	}

	/**
	 * @param appServerId セットする appServerId
	 */
	public void setAppServerId(String appServerId) {
		this.appServerId = appServerId;
	}

	/**
	 * @return consumerServerId
	 */
	public String getConsumerServerId() {
		return consumerServerId;
	}

	/**
	 * @param consumerServerId セットする consumerServerId
	 */
	public void setConsumerServerId(String consumerServerId) {
		this.consumerServerId = consumerServerId;
	}

	/**
	 * @return type
	 */
	public AsyncMessageType getType() {
		return type;
	}

	/**
	 * @param type セットする type
	 */
	public void setType(AsyncMessageType type) {
		this.type = type;
	}

	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public long getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(long delayTime) {
		this.delayTime = delayTime;
	}

	
}
