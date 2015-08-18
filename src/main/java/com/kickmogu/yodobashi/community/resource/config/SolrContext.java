package com.kickmogu.yodobashi.community.resource.config;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.id.IDGenerator;
import com.kickmogu.lib.core.resource.Site;
import com.kickmogu.lib.core.shard.ShardingPartitioner;
import com.kickmogu.lib.core.shard.SimpleShardingPartitioner;
import com.kickmogu.lib.core.time.SystemTime;
import com.kickmogu.lib.core.utils.Asserts;
import com.kickmogu.lib.hadoop.hbase.HBaseContainer;
import com.kickmogu.lib.solr.SolrContainer;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.lib.solr.SolrRole;
import com.kickmogu.lib.solr.aop.AopSolrProcessContextHolderImpl;
import com.kickmogu.lib.solr.impl.AbstractSolrTemplate;
import com.kickmogu.lib.solr.impl.ExternalEntitySolrTemplate;
import com.kickmogu.lib.solr.impl.SolrTemplate;
import com.kickmogu.lib.solr.meta.SolrMeta;
import com.kickmogu.yodobashi.community.resource.domain.BaseWithTimestampDO;
import com.kickmogu.yodobashi.community.resource.ha.HAManager;
import com.kickmogu.yodobashi.community.resource.ha.HASolrMirrorTemplate;

@Configuration
public class SolrContext {

	@Autowired
	private ResourceConfig resourceConfig;

	@Autowired
	private CommunitySolrProcessContextHandler solrProcessContextHandler;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired @Qualifier("default")
	private IDGenerator<String> idGenerator;
	
	@Autowired
	private HAManager haManager;
	
	@Autowired @Qualifier("Site1")
	private HBaseContainer hBaseContainer1;

	@Autowired @Qualifier("Site2")
	private HBaseContainer hBaseContainer2;

	@Bean @Qualifier("MySite")
	public SolrContainer solrMyContainer() {
		 if ( resourceConfig.getPropatyValue("solr.mysite").equals("SITE1")) {
			 return solrContainer1();
		 } else if ( resourceConfig.getPropatyValue("solr.mysite").equals("SITE2")) {
			 return solrContainer2();
		 }
		 throw new IllegalArgumentException(resourceConfig.getPropatyValue("solr.mysite"));
	}

	@Bean
	public SolrMeta solrMeta() {
		return solrMyContainer().getMeta();
	}

	@Bean @Qualifier("Site1")
	public SolrContainer solrContainer1() {
		if (resourceConfig.solrMySite.equals(Site.SITE2) && resourceConfig.solrUseOnlyMySite) return null;
		return createBaseContainer("site1");
	}

	@Bean @Qualifier("Site2")
	public SolrContainer solrContainer2() {
		if (!resourceConfig.solrMirroring) return null;
		if (resourceConfig.solrMySite.equals(Site.SITE1) && resourceConfig.solrUseOnlyMySite) return null;
		return createBaseContainer("site2");
	}

	protected SolrContainer createBaseContainer(String siteName) {
		SolrContainer container = new SolrContainer();
		container.setDefautDynamicFieldDefinitions(SolrDynamicFieldDefinitions.LIST);
		container.addScanPackages(BaseWithTimestampDO.class.getPackage());
		container.setConfig(resourceConfig);
		container.setApplicationContext(applicationContext);
		container.setDefaultGenerator(String.class, idGenerator);
		container.setSiteName(siteName);
		container.setEnableMasterMonitor(resourceConfig.solrEnableMasterMonitor);
		
		if (resourceConfig.solrEnableMasterMonitor) {
			Site site = Site.valueOf(siteName.toUpperCase());
			Asserts.notNull(site);
			HBaseContainer hBaseContainer = site.equals(Site.SITE1) ? hBaseContainer1 : hBaseContainer2;
			container.setMasterMonitorZooKeeperConfiguration(hBaseContainer.getConfiguration());
			
		}
		container.setEnableWebServerMonitor(resourceConfig.solrEnableWebServerMonitor);		
		return container;
	}

	protected AbstractSolrTemplate createSolrOperations(SolrContainer container, boolean refMaster) {
		if (container == null) return null;
		SolrTemplate template = new SolrTemplate();
		template.setOptimizeIntervalOnCommit(resourceConfig.getPropatyValueAsInt("solr.optimizeInterval.on.commit"));
		template.setMeta(container.getMeta());
		template.setAutoCommit(Boolean.valueOf(System.getProperty("solr.auto.commit", "true")));
		template.setProcessContextHolder(solrProcessContextHandler);
		if (refMaster) template.setQueryRole(SolrRole.MASTER);
		return new ExternalEntitySolrTemplate(container, template);
	}

	@Bean @Qualifier("Site1")
	public SolrOperations solrOperations1() {
		return createSolrOperations(solrContainer1(), false);
	}
	
	@Bean @Qualifier("Site1-RefMaster")
	public SolrOperations solrOperationsRefMaster1() {
		return createSolrOperations(solrContainer1(), true);
	}

	@Bean @Qualifier("Site2")
	public SolrOperations solrOperations2() {
		return createSolrOperations(solrContainer2(), false);
	}
	
	@Bean @Qualifier("Site2-RefMaster")
	public SolrOperations solrOperationsRefMaster2() {
		return createSolrOperations(solrContainer2(), true);
	}

	@Bean @Qualifier("MySite")
	public SolrOperations solrOperationsMySite() {
		return resourceConfig.getPropatyValue("solr.mysite").equals("SITE1") ? solrOperations1() : solrOperations2();
	}
	
	@Bean @Qualifier("MySite-RefMaster")
	public SolrOperations solrOperationsMySiteRefMaster() {
		return resourceConfig.getPropatyValue("solr.mysite").equals("SITE1") ? solrOperationsRefMaster1() : solrOperationsRefMaster2();
	}

	@Bean @Qualifier("default")
	public SolrOperations solrOperations() {
		
		SolrOperations solrOperations1 = solrOperations1();
		SolrOperations solrOperations2 = solrOperations2();
		Site site = null;
		 if ( resourceConfig.getPropatyValue("solr.mysite").equals("SITE1")) {
			 site = Site.SITE1;
		 } else if ( resourceConfig.getPropatyValue("solr.mysite").equals("SITE2")) {
			 site = Site.SITE2;
		 } else {
			 throw new IllegalArgumentException(resourceConfig.getPropatyValue("solr.mysite"));
		 }
		
		if (solrOperations1 != null && solrOperations2 != null) {
			HASolrMirrorTemplate mirrorTemplate = new HASolrMirrorTemplate(
					haManager,
					solrOperations1(),
					solrOperations2(),
					solrContainer1(),
					solrContainer2(),
					site);
				return mirrorTemplate;
		} else if (solrOperations1 != null) {
			return solrOperations1;
		} else if (solrOperations2 != null) {
			return solrOperations2;
		} else throw new IllegalArgumentException();

	}
	
	@Bean @Qualifier("default-RefMaster")
	public SolrOperations solrOperationsRefMaster() {
		
		SolrOperations solrOperations1 = solrOperationsRefMaster1();
		SolrOperations solrOperations2 = solrOperationsRefMaster2();
		Site site = null;
		 if ( resourceConfig.getPropatyValue("solr.mysite").equals("SITE1")) {
			 site = Site.SITE1;
		 } else if ( resourceConfig.getPropatyValue("solr.mysite").equals("SITE2")) {
			 site = Site.SITE2;
		 } else {
			 throw new IllegalArgumentException(resourceConfig.getPropatyValue("solr.mysite"));
		 }
		
		if (solrOperations1 != null && solrOperations2 != null) {
			HASolrMirrorTemplate mirrorTemplate = new HASolrMirrorTemplate(
					haManager,
					solrOperationsRefMaster1(),
					solrOperationsRefMaster2(),
					solrContainer1(),
					solrContainer2(),
					site);
				return mirrorTemplate;
		} else if (solrOperations1 != null) {
			return solrOperations1;
		} else if (solrOperations2 != null) {
			return solrOperations2;
		} else throw new IllegalArgumentException();

	}

	@Bean(name="solrShardingPartitioner")
	public ShardingPartitioner solrShardingPartitioner() {
		return new SimpleShardingPartitioner();
	}
	
	@Service @Aspect
	public static class CommunitySolrProcessContextHandler extends AopSolrProcessContextHolderImpl {
		
		
		@Override @Autowired
		public void setSystemTime(SystemTime systemTime) {
			super.setSystemTime(systemTime);
		}
	}

}
