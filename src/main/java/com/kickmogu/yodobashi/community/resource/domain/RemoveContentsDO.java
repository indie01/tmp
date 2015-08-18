package com.kickmogu.yodobashi.community.resource.domain;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.resource.annotation.HasMany;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumnFamily;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseKey;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseTable.SizeGroup;
import com.kickmogu.lib.hadoop.hbase.annotation.RelatedByHBase;
import com.kickmogu.lib.solr.annotation.RelatedBySolr;
import com.kickmogu.lib.solr.annotation.SolrField;
import com.kickmogu.lib.solr.annotation.SolrSchema;
import com.kickmogu.lib.solr.annotation.SolrUniqKey;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityContentsType;

@HBaseTable(columnFamilies={
	@HBaseColumnFamily(name="cf")
}
,sizeGroup=SizeGroup.SMALL)
@SolrSchema
public class RemoveContentsDO extends BaseWithTimestampDO {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3162770785022359844L;

	/**
	 * 削除対象コンテンツIDです。
	 */
	@HBaseKey(createTableSplitKeys={"1","5","9","D","H","L","P","T","X","b","f","j","n","r","v"})
	@SolrField @SolrUniqKey
	private String removeContentsId;

	@HBaseColumn
	@SolrField
	private CommunityContentsType removeContentsType;
	
	@HBaseColumn
	@SolrField
	private String parentContentsId;
	
	@RelatedByHBase
	@RelatedBySolr
	private @HasMany List<RemoveContentsDetailDO> removeContentsDetails = Lists.newArrayList();
	
	/**
	 * @return the removeContentsId
	 */
	public String getRemoveContentsId() {
		return removeContentsId;
	}

	/**
	 * @param removeContentsId the removeContentsId to set
	 */
	public void setRemoveContentsId(String removeContentsId) {
		this.removeContentsId = removeContentsId;
	}

	/**
	 * @return the removeContentsType
	 */
	public CommunityContentsType getRemoveContentsType() {
		return removeContentsType;
	}

	/**
	 * @param removeContentsType the removeContentsType to set
	 */
	public void setRemoveContentsType(CommunityContentsType removeContentsType) {
		this.removeContentsType = removeContentsType;
	}

	/**
	 * @return the parentContentsId
	 */
	public String getParentContentsId() {
		return parentContentsId;
	}

	/**
	 * @param parentContentsId the parentContentsId to set
	 */
	public void setParentContentsId(String parentContentsId) {
		this.parentContentsId = parentContentsId;
	}

	/**
	 * @return the removeContentsDetails
	 */
	public List<RemoveContentsDetailDO> getRemoveContentsDetails() {
		return removeContentsDetails;
	}

	/**
	 * @param removeContentsDetails the removeContentsDetails to set
	 */
	public void setRemoveContentsDetails(
			List<RemoveContentsDetailDO> removeContentsDetails) {
		this.removeContentsDetails = removeContentsDetails;
		if (removeContentsDetails == null) return;
		for (RemoveContentsDetailDO removeContentsDetail:removeContentsDetails) {
			removeContentsDetail.setRemoveContents(this);
		}
	}

	public void addRemoveContentsDetails(RemoveContentsDetailDO removeContentsDetail){
		removeContentsDetail.setRemoveContents(this);
		this.removeContentsDetails.add(removeContentsDetail);
	}
	
	public List<String> getRemoveContentsDetailKeys(){
		List<String> keys = null;
		if(this.removeContentsDetails != null && !this.removeContentsDetails.isEmpty()){
			keys = new ArrayList<String>();
			for(RemoveContentsDetailDO detail: this.removeContentsDetails){
				keys.add(detail.getContentsId());
			}
		}
		return keys;
	}
	
	
}
