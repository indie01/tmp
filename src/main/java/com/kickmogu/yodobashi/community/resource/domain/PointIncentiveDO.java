package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;
import java.util.List;

import org.msgpack.annotation.Ignore;
import org.msgpack.annotation.Message;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.id.annotation.IDParts;
import com.kickmogu.lib.core.resource.annotation.HasMany;
import com.kickmogu.lib.core.resource.annotation.Label;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.lib.hadoop.hbase.annotation.RelatedByHBase;
import com.kickmogu.lib.solr.annotation.RelatedBySolr;
import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.lib.solr.annotation.SolrSchema;

@HBaseTable(columnFamilies={
	@HBaseColumnFamily(name="cf")
}
,sizeGroup=SizeGroup.SMALL)
@SolrSchema
@Message
public class PointIncentiveDO extends DatasyncBaseDO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4947912177499134140L;

	
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
	
	@HBaseColumn
	@SolrField
	@Label("インセンティブテキスト")
	protected String text;
	
	@HBaseColumn
	@SolrField
	@Label("インセンティブ 集計開始時刻")
	protected Date aggregateStartTime;

	@HBaseColumn
	@SolrField
	@Label("インセンティブ 集計終了時刻")
	protected Date aggregateEndTime;
	
	@HBaseColumn
	@SolrField
	@Label("インセンティブ発表日")
	protected Date announcementDate;
	
	@HBaseColumn
	@SolrField
	@Label("一時無効フラグ")
	protected boolean invalidFlag;

	@Ignore
	@RelatedByHBase
	@RelatedBySolr
	protected @HasMany(sortBy="incentiveDetailId") List<PointIncentiveDetailDO> details = Lists.newArrayList();


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

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Date getAggregateStartTime() {
		return aggregateStartTime;
	}

	public void setAggregateStartTime(Date aggregateStartTime) {
		this.aggregateStartTime = aggregateStartTime;
	}

	public Date getAggregateEndTime() {
		return aggregateEndTime;
	}

	public void setAggregateEndTime(Date aggregateEndTime) {
		this.aggregateEndTime = aggregateEndTime;
	}

	public boolean isInvalidFlag() {
		return invalidFlag;
	}

	public void setInvalidFlag(boolean invalidFlag) {
		this.invalidFlag = invalidFlag;
	}

	public Date getAnnouncementDate() {
		return announcementDate;
	}

	public void setAnnouncementDate(Date announcementDate) {
		this.announcementDate = announcementDate;
	}

	public List<PointIncentiveDetailDO> getDetails() {
		return details;
	}

	public void setDetails(List<PointIncentiveDetailDO> details) {
		this.details = details;
	}

}
