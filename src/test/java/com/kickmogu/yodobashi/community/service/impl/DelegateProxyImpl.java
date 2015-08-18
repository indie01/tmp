/**
 *
 */
package com.kickmogu.yodobashi.community.service.impl;

import org.springframework.stereotype.Service;

import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.lib.solr.annotation.ArroundSolr;
import com.kickmogu.yodobashi.community.common.test.Delegate;
import com.kickmogu.yodobashi.community.common.test.DelegateProxy;

/**
 * @author kamiike
 *
 */
@Service
public class DelegateProxyImpl implements DelegateProxy {

	/* (非 Javadoc)
	 * @see com.kickmogu.yodobashi.community.common.test.DelegateProxy#execute(com.kickmogu.yodobashi.community.common.test.Delegate)
	 */
	@Override
	@ArroundSolr
	@ArroundHBase
	public void execute(Delegate delegate) {
		delegate.execute();
	}

}
