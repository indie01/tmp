package com.kickmogu.yodobashi.community.resource.domain;

import java.util.Calendar;
import java.util.Date;

public abstract class AbstractContentBaseDO extends BaseWithTimestampDO {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3841944387319069443L;
	
	abstract public boolean isPostImmediatelyAfter();
	
	protected boolean checkPostImmediatelyAfter(Date postDate){
		if( postDate == null )
			return false;
		
		long postDateTime = postDate.getTime();
		long nowDateTime = Calendar.getInstance().getTimeInMillis();
		// 経過（分）を算出
		long minitusDiff = (nowDateTime - postDateTime)/(1000 * 60);
		// 60分以内かどうか
		return (minitusDiff < 60);
	}
}
