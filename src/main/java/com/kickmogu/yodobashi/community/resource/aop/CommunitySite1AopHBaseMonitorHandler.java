package com.kickmogu.yodobashi.community.resource.aop;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.hadoop.hbase.HBaseContainer;
import com.kickmogu.lib.hadoop.hbase.aop.AopHBaseMonitorHandler;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;

@Service @Aspect
public class CommunitySite1AopHBaseMonitorHandler extends AopHBaseMonitorHandler {


	@Autowired
	private ResourceConfig resourceConfig;
	
	@Autowired @Qualifier("Site1")
	private HBaseContainer hBaseContainer;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (hBaseContainer == null) {
			this.setEnable(false);
			return;
		}
		this.sethBaseContainer(hBaseContainer);
		this.setEnable(resourceConfig.hbaseMonitorEnable);
		this.setWaitUntilAlive(resourceConfig.hbaseMonitorWaitUntilAlive);
		this.setWaitTimeout(resourceConfig.hbaseMonitorWaitTimeout);
		super.afterPropertiesSet();
	}

}
