package com.kickmogu.yodobashi.community.service.vo;

import java.util.Calendar;
import java.util.Date;

public abstract class ContentBaseVO extends BaseVO {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9018303037448841861L;

	abstract public boolean isPostImmediatelyAfter();
	
	protected boolean checkPostImmediatelyAfter(Date postDate){
		long postDateTime = postDate.getTime();
		long nowDateTime = Calendar.getInstance().getTimeInMillis();
		// 経過（分）を算出
		long minitusDiff = (nowDateTime - postDateTime)/(1000 * 60);
		// 60分以内かどうか
		return (minitusDiff < 60);
	}
}
