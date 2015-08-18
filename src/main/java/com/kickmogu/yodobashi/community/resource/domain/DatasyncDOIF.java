package com.kickmogu.yodobashi.community.resource.domain;

import java.io.Serializable;
import java.util.Date;

public interface DatasyncDOIF extends Serializable {
	String getId();
	Date getModifyDateTime();
	void setModifyDateTime(Date modifyDateTime);
}
