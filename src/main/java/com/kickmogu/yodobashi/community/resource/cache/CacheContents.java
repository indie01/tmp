package com.kickmogu.yodobashi.community.resource.cache;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class CacheContents implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4906156581003947651L;
	
	private AtomicInteger hitCount = new AtomicInteger(0);

	private Serializable key;
	private long startTime;
	private Serializable contents;
	private AtomicBoolean startReload = new AtomicBoolean(false);
	private AtomicLong startReloadingTime;
	
	public CacheContents(Serializable key, Serializable contents) {
		this.key = key;
		this.contents = contents;
		this.startTime = System.currentTimeMillis();
		this.startReloadingTime = new AtomicLong(System.currentTimeMillis());
	}
	
	public int getHitCount() {
		return hitCount.get();
	}

	public int incrementHitCount() {
		 return hitCount.incrementAndGet();
	}
	
	public long getStartTime() {
		return startTime;
	}
	public Object getContents() {
		return contents;
	}

	public boolean isStartReload() {
		return startReload.get();
	}

	public void startReload() {
		this.startReloadingTime = new AtomicLong(System.currentTimeMillis());
		this.startReload.set(true);
	}

	public long getStartReloadingTime() {
		return startReloadingTime.get();
	}


	public String toReportString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
			.append("startTime", new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(new Date(startTime)))
			.append("key", key)
			.append("hitCount", hitCount)
			.toString();
	}

}
