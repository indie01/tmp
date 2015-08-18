package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Collection;

import com.kickmogu.lib.core.resource.domain.BaseDO;

public class FindPointGrantEntryResponseDO extends BaseDO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4244187788171887588L;
	
	private Collection<PointGrantEntryDO> pointGrantEntries;
	
	private Long numFound;

	public FindPointGrantEntryResponseDO() {
	}

	public FindPointGrantEntryResponseDO(
			Collection<PointGrantEntryDO> pointGrantEntries, 
			Long numFound) {
		this.pointGrantEntries = pointGrantEntries;
		this.numFound = numFound;
	}

	public Collection<PointGrantEntryDO> getPointGrantEntries() {
		return pointGrantEntries;
	}

	public void setPointGrantEntries(Collection<PointGrantEntryDO> pointGrantEntries) {
		this.pointGrantEntries = pointGrantEntries;
	}

	public Long getNumFound() {
		return numFound;
	}

	public void setNumFound(Long numFound) {
		this.numFound = numFound;
	}

}
