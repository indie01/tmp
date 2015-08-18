package com.kickmogu.yodobashi.community.resource.aop;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.time.SystemTime;
import com.kickmogu.lib.core.utils.AopUtil;
import com.kickmogu.lib.hadoop.hbase.HBaseDirectOperations;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.aop.AopHBaseProcessContextHolderImpl;
import com.kickmogu.lib.hadoop.hbase.impl.AbstractHBaseTemplate;
import com.kickmogu.lib.hadoop.hbase.impl.HBaseDirectTemplate;
import com.kickmogu.lib.hadoop.hbase.impl.PooledHTableInterfaceHolderImpl;
import com.kickmogu.yodobashi.community.resource.config.HBaseContext;

@Service @Aspect
public class CommunityHBaseProcessContextHandler extends AopHBaseProcessContextHolderImpl<PooledHTableInterfaceHolderImpl> implements InitializingBean {

	/** SpringDI初期化エラー対策 */
	@Autowired
	private HBaseContext hBaseContext;

	@Autowired @Qualifier("default")
	private HBaseOperations template;
	
	@Autowired
	private HBaseDirectOperations hBaseDirectOperations;
	

	@Override @Autowired
	public void setSystemTime(SystemTime systemTime) {
		super.setSystemTime(systemTime);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setInterfaceHolderType(PooledHTableInterfaceHolderImpl.class);
		((AbstractHBaseTemplate)AopUtil.getTargetObject(template)).setProcessContextHolder(this);
		((HBaseDirectTemplate)AopUtil.getTargetObject(hBaseDirectOperations)).setProcessContextHolder(this);
	}


}
