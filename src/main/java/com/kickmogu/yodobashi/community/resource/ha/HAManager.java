package com.kickmogu.yodobashi.community.resource.ha;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Abortable;
import org.apache.hadoop.hbase.Chore;
import org.apache.hadoop.hbase.Stoppable;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.util.Threads;
import org.apache.hadoop.hbase.zookeeper.ZKUtil;
import org.apache.hadoop.hbase.zookeeper.ZooKeeperWatcher;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import com.kickmogu.lib.core.exception.CommonSystemException;
import com.kickmogu.lib.core.resource.Site;
import com.kickmogu.lib.core.utils.Asserts;

public class HAManager implements Stoppable, DisposableBean {
	
	public static final String HA_ZNODE = "/commHA";
	public static final String WATCHER_NAME = "/commHAWatcher";
	
	private static final Logger LOG = LoggerFactory.getLogger(HAManager.class);
	
	private Site mySite;
	
	private SiteZooKeeper site1ZooKeeper;

	private SiteZooKeeper site2ZooKeeper;

	private HAInfo previousHAInfo;
	
	boolean stopped;
	
	public HAManager(Site mySite, Configuration site1Conf, Configuration site2Conf)  {
		this.mySite = mySite;
		try {
			this.site1ZooKeeper = new SiteZooKeeper(Site.SITE1, site1Conf);
			this.site2ZooKeeper = new SiteZooKeeper(Site.SITE2, site2Conf);

		} catch (Throwable th) {
			throw new CommonSystemException(th);
		}
		
		getHAInfo();
		Asserts.notNull(previousHAInfo);
	}
	

	@Override
	public void stop(String why) {
		stopped = true;
		LOG.info(why);
		site1ZooKeeper.stop(why);
		site1ZooKeeper.stop(why);
	}

	@Override
	public boolean isStopped() {
		return stopped;
	}
	
	public synchronized HAInfo getHAInfo() {
		try {
			HAInfo site1HAInfo = site1ZooKeeper.getHAInfo();
			HAInfo site2HAInfo = site2ZooKeeper.getHAInfo();
			boolean site1Alive = site1ZooKeeper.isAlive() && site1HAInfo != null; 
			boolean site2Alive = site2ZooKeeper.isAlive() && site2HAInfo != null; 
			
			if (site1Alive && site2Alive) {
				if (site1HAInfo.equals(site2HAInfo)) {
					previousHAInfo =  mySite.equals(Site.SITE1) ? site1HAInfo : site2HAInfo;
					return previousHAInfo;
				} else {
					return previousHAInfo;
				} 
			} else if (site1Alive) {
				previousHAInfo = site1ZooKeeper.getHAInfo();
				return previousHAInfo;
			} else if (site2Alive) {
				previousHAInfo = site2ZooKeeper.getHAInfo();
				return previousHAInfo;
			} else {
				return previousHAInfo;
			}			
		} catch (Throwable th) {
			LOG.error("fail to getHAInfo", th);
			return previousHAInfo;
		}
	}

	
	public static class SiteZooKeeper implements Abortable, Stoppable {
		
		private Site site;
		
		private Configuration conf;
		
		private ZooKeeperWatcher zooKeeper;
		
		private HAInfoTracker haInfoTracker;
		
		private ZooKeeperReconnectChore zooKeeperReconnectThread;
		
		private volatile boolean closed = true;
		
		private volatile boolean stopped;
		
		public SiteZooKeeper(Site site, Configuration conf) throws Exception {
			this.site = site;
			this.conf = conf;
			try {
				setupZookeeperTrackers();				
			} catch (ZooKeeperConnectionException e) {
				LOG.warn("failt Connect ZooKeeper " + site);
			}
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				@Override
				public void run() {
					stop("shutdown");
				}
			}));
			if (haInfoTracker != null && haInfoTracker.getHAInfo() == null) {
				ZKUtil.createSetData(zooKeeper, HA_ZNODE, HAInfo.createNormalHAInfo().toBytes());
			}
			this.zooKeeperReconnectThread = (ZooKeeperReconnectChore)Threads.setDaemonThreadRunning(new ZooKeeperReconnectChore(this));
		}
		
	    public synchronized ZooKeeperWatcher getZooKeeperWatcher()
	            throws ZooKeeperConnectionException {
	      if(zooKeeper == null) {
	        try {
	          this.zooKeeper = new ZooKeeperWatcher(conf, WATCHER_NAME, this);
	        } catch (IOException e) {
	          throw new ZooKeeperConnectionException("",e);
	        }
	      }
	      this.closed = false;
	      LOG.info("Start ZooKeeperWatcher");
	      return zooKeeper;
	    }
		
		public synchronized void stop(String why) {
			if (zooKeeperReconnectThread != null) zooKeeperReconnectThread.interrupt();
			if (haInfoTracker != null) haInfoTracker.stop();
			if (haInfoTracker != null) zooKeeper.close();
			this.closed = true;
			this.stopped = true;
			LOG.info("stopped why=" + why);
		}
		
	    private synchronized void setupZookeeperTrackers() throws ZooKeeperConnectionException{
			this.zooKeeper = getZooKeeperWatcher();
			this.haInfoTracker = new HAInfoTracker(this.zooKeeper, HA_ZNODE, this);
			this.zooKeeper.registerListener(this.haInfoTracker);
			this.haInfoTracker.start();
	    }

	    private synchronized void resetZooKeeperTrackers()
	            throws ZooKeeperConnectionException {
	      if (haInfoTracker != null) haInfoTracker.stop();
	      setupZookeeperTrackers();
	    }
	    
		public HAInfo getHAInfo() {
			return haInfoTracker.getHAInfo();
		}

	    @Override
	    public void abort(final String msg, Throwable t) {
	      if (t instanceof KeeperException.SessionExpiredException) {
	        try {
	          LOG.info("This client just lost it's session with ZooKeeper, trying to reconnect.");
	          resetZooKeeperTrackers();
	          LOG.info("Reconnected successfully. This disconnect could have been" +
	              " caused by a network partition or a long-running GC pause," +
	              " either way it's recommended that you verify your environment.");
	          return;
	        } catch (ZooKeeperConnectionException e) {
	          LOG.error("Could not reconnect to ZooKeeper after session expiration, aborting");
	          t = e;
	        }
	      }
	      if (t != null) LOG.error(msg, t);
	      else LOG.error(msg);
	      this.closed = true;
	    }

	    public boolean isClosed() {
	    	return this.closed;
	    }
	    
	    public boolean isAlive() {
	    	return !isClosed();
	    }

		@Override
		public boolean isStopped() {
			return stopped;
		}
	}
	
	private static class ZooKeeperReconnectChore extends Chore  {

		private static final int DEFAULT_SLEEP = 10 * 1000;
		
		private SiteZooKeeper siteZooKeeper;

		ZooKeeperReconnectChore(SiteZooKeeper siteZooKeeper) {
		    super("ZooKeeperReconnect"+siteZooKeeper.site, DEFAULT_SLEEP, siteZooKeeper);
		    this.siteZooKeeper = siteZooKeeper;
			LOG.info("Runs every " + DEFAULT_SLEEP + "ms");
		}

		@Override
		protected void chore() {
			// LOG.info("checking.. " + siteZooKeeper.site);
			if (siteZooKeeper.closed) {
				LOG.info("Trying to reconnect to zookeeper " + siteZooKeeper.site);
				try {
					siteZooKeeper.resetZooKeeperTrackers();
					LOG.info("Succeed to reconnect to zookeeper");
				} catch (Throwable th) {
					LOG.info("Fail to reconnect to zookeeper." + th.getMessage());
				}
			}
		}
	}

	@Override
	public void destroy() throws Exception {
		this.stop("shutdown");
	}

}

