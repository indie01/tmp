package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;

import javax.xml.bind.annotation.XmlTransient;

import com.kickmogu.lib.core.resource.domain.BaseDO;
import com.kickmogu.lib.hadoop.hbase.annotation.HBaseColumn;
import com.kickmogu.lib.solr.annotation.SolrField;

@XmlTransient
public abstract class BaseWithTimestampDO extends BaseDO {

	/**
	 *
	 */
	private static final long serialVersionUID = -1465803562209761863L;

	@XmlTransient
	@HBaseColumn
	@SolrField
	private Date registerDateTime;

	@XmlTransient
	@HBaseColumn
	@SolrField
	private Date modifyDateTime;

	/**
	 * @return registerDateTime
	 */
	public Date getRegisterDateTime() {
		return registerDateTime;
	}

	/**
	 * @param registerDateTime セットする registerDateTime
	 */
	public void setRegisterDateTime(Date registerDateTime) {
		this.registerDateTime = registerDateTime;
	}

	/**
	 * @return modifyDateTime
	 */
	public Date getModifyDateTime() {
		return modifyDateTime;
	}

	/**
	 * @param modifyDateTime セットする modifyDateTime
	 */
	public void setModifyDateTime(Date modifyDateTime) {
		this.modifyDateTime = modifyDateTime;
	}
}
