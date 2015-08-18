package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Date;

public interface LogicalDeletable {
	void setDeleteDate(Date deleteDate);
	Date getDeleteDate();
	boolean isDeleteFlag();
	void setDeleteFlag(boolean deleteFlag); 
}
