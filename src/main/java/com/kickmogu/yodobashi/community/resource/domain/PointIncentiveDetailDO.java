package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;

import org.msgpack.annotation.Ignore;
import org.msgpack.annotation.Message;

import com.kickmogu.lib.core.id.annotation.IDParts;
import com.kickmogu.lib.core.resource.annotation.BelongsTo;
import com.kickmogu.lib.core.resource.annotation.Label;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.lib.solr.annotation.SolrSchema;

@HBaseTable(columnFamilies={
	@HBaseColumnFamily(name="cf")
}
,sizeGroup=SizeGroup.SMALL)
@SolrSchema
@Message
public class PointIncentiveDetailDO extends DatasyncBaseDO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8028092251094636277L;

	@HBaseColumn @IDParts(order=1)
	@SolrField
	@Label("インセンティブID")
	protected String incentiveId;
	
	@HBaseColumn
	@SolrField
	@Label("有効開始時刻")
	protected Date startTime;

	@HBaseColumn @IDParts(order=2,isHashParts=false)
	@SolrField
	@Label("有効終了時刻")
	protected Date endTime;
	
	@HBaseColumn @IDParts(order=3,isHashParts=false)
	@SolrField
	@Label("インセンティブ明細ID")
	protected String incentiveDetailId;
	
	@HBaseColumn
	@SolrField
	@Label("インセンティブ詳細テキスト")
	protected String text;
	
	@HBaseColumn
	@SolrField
	@Label("ポイントインセンティブ総額")
	protected long totalValue;
	
	@HBaseColumn
	@SolrField
	@Label("ポイントインセンティブ人数")
	protected int limitValue;

	
	@HBaseColumn @Ignore
	@SolrField
	protected @BelongsTo PointIncentiveDO pointIncentive;


	public String getIncentiveId() {
		return incentiveId;
	}

	public void setIncentiveId(String incentiveId) {
		this.incentiveId = incentiveId;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getIncentiveDetailId() {
		return incentiveDetailId;
	}

	public void setIncentiveDetailId(String incentiveDetailId) {
		this.incentiveDetailId = incentiveDetailId;
	}

	public long getTotalValue() {
		return totalValue;
	}

	public void setTotalValue(long totalValue) {
		this.totalValue = totalValue;
	}


	public int getLimitValue() {
		return limitValue;
	}

	public void setLimitValue(int limitValue) {
		this.limitValue = limitValue;
	}

	public PointIncentiveDO getPointIncentive() {
		return pointIncentive;
	}

	public void setPointIncentive(PointIncentiveDO pointIncentive) {
		this.pointIncentive = pointIncentive;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
