package com.kickmogu.yodobashi.community.resource.domain;

import java.util.List;

public interface NotifyUpdateDataIF {
	void setNotifyUpdateEntryId(String entryId);
	String getNotifyUpdateEntryId();
	List<? extends NotifyUpdateRecordIF> getNotifyUpdateRecordList();
}
