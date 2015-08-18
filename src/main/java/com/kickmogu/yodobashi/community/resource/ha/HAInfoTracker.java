package com.kickmogu.yodobashi.community.resource.ha;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.Abortable;
import org.apache.hadoop.hbase.zookeeper.ZooKeeperNodeTracker;
import org.apache.hadoop.hbase.zookeeper.ZooKeeperWatcher;

import com.kickmogu.hadoop.io.utils.WritableUtil;

public class HAInfoTracker extends ZooKeeperNodeTracker {
	public static Log log = LogFactory.getLog(HAInfoTracker.class);

	public HAInfoTracker(ZooKeeperWatcher watcher, String node,
			Abortable abortable) {
		super(watcher, node, abortable);
	}
	
	public HAInfo getHAInfo() {
		byte[] data = super.getData();
		if (data == null) return null;
		return WritableUtil.toObject(HAInfo.class, data);
	}

	@Override
	public synchronized void nodeDataChanged(String path) {
		super.nodeDataChanged(path);
		log.info("nodeDataChanged:" + getHAInfo());
	}
}
