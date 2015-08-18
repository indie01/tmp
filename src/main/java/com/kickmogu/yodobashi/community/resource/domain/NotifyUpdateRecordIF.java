package com.kickmogu.yodobashi.community.resource.domain;

import java.util.List;

public interface NotifyUpdateRecordIF {
	String getTableName();

	public List<? extends NotifyUpdateColumnIF> getNotifyUpdateColumnList();
}
