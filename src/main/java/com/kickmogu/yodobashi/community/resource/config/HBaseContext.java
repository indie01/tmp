package com.kickmogu.yodobashi.community.resource.config;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.io.hfile.Compression;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kickmogu.lib.core.id.IDGenerator;
import com.kickmogu.lib.core.id.RandomUUIDGenerator;
import com.kickmogu.lib.core.id.SimpleIDGenerator;
import com.kickmogu.lib.core.resource.Site;
import com.kickmogu.lib.hadoop.hbase.HBaseContainer;
import com.kickmogu.lib.hadoop.hbase.HBaseDirectOperations;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.impl.AbstractHBaseTemplate;
import com.kickmogu.lib.hadoop.hbase.impl.ExternalEntityHBaseTemplate;
import com.kickmogu.lib.hadoop.hbase.impl.HBaseDirectTemplate;
import com.kickmogu.lib.hadoop.hbase.impl.HBaseTemplate;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseMeta;
import com.kickmogu.yodobashi.community.resource.dao.impl.ApplicationLabelDaoImpl;
import com.kickmogu.yodobashi.community.resource.domain.ApplicationLabelDO;
import com.kickmogu.yodobashi.community.resource.domain.BaseWithTimestampDO;
import com.kickmogu.yodobashi.community.resource.ha.HAHBaseMirrorTemplate;
import com.kickmogu.yodobashi.community.resource.ha.HAManager;

@Configuration
public class HBaseContext implements InitializingBean,DisposableBean {

	@Autowired
	private HBaseConfig hbaseConfig;

	@Autowired
	private ApplicationContext applicationContext;
	
	@Bean
	public HAManager haManager()  {
		if (!hbaseConfig.hbaseMirroring) return null;
		if (hbaseConfig.hbaseUseOnlyMySite) return null;
		
		org.apache.hadoop.conf.Configuration conf1 = new org.apache.hadoop.conf.Configuration();
		conf1.addResource(hbaseConfig.hbaseConfigFileMirror1);
		conf1 = HBaseConfiguration.create(conf1);
		org.apache.hadoop.conf.Configuration conf2 = new org.apache.hadoop.conf.Configuration();
		conf2.addResource(hbaseConfig.hbaseConfigFileMirror2);
		conf2 = HBaseConfiguration.create(conf2);

		
		HAManager haManager = new HAManager(hbaseConfig.hbaseMySite, conf1, conf2);
		return haManager;
	}

	@Bean @Qualifier("default")
	public IDGenerator<String> idGenerator() {

		HAManager haManager =  haManager();
		 
		HBaseContainer container1 = createContainerMirror1();
		if (container1 != null) {
			container1.setDefaultGenerator(String.class, new RandomUUIDGenerator<String>(String.class));
			container1.addScanClasses(ApplicationLabelDO.class);
			container1.initialize();
		}
		
		HBaseContainer container2 = createContainerMirror2();
		if (container2 != null) {
			container2.setDefaultGenerator(String.class, new RandomUUIDGenerator<String>(String.class));
			container2.addScanClasses(ApplicationLabelDO.class);
			container2.initialize();
		}

		
		HBaseOperations hbaseOperations = null;
		if (container1 != null && container2 != null) {
			hbaseOperations = new HAHBaseMirrorTemplate(
					haManager,
					createHBaseOperations(container1),
					createHBaseOperations(container2),
				hbaseConfig.hbaseMySite);
		} else if (container1 != null) hbaseOperations = createHBaseOperations(container1);
		else if  (container2 != null) hbaseOperations = createHBaseOperations(container2);
		else throw new IllegalArgumentException();
		
		ApplicationLabelDaoImpl applicationLabelDao = new ApplicationLabelDaoImpl();
		applicationLabelDao.sethBaseOperations(hbaseOperations);
		String applicationLabel = applicationLabelDao.getLabel("community");

		return new SimpleIDGenerator(applicationLabel);
		
	}
	
	@Bean @Qualifier("MySite")
	public HBaseContainer hbaseMyContainer() {
		 return hbaseConfig.hbaseMySite.equals(Site.SITE1) ?  hbaseContainer1() : hbaseContainer2();
	}
	
	@Bean
	public HBaseMeta hbaseMeta() {
		return hbaseMyContainer().getMeta();
	}

	@Bean @Qualifier("Site1")
	public HBaseContainer hbaseContainer1() {
		HBaseContainer container = createContainerMirror1();
		if (container == null) return null;
		container.setDefaultGenerator(String.class, idGenerator());
		container.addScanPackages(BaseWithTimestampDO.class.getPackage());
		return container;
	}	
	
	@Bean @Qualifier("Site2")
	public HBaseContainer hbaseContainer2() {
		HBaseContainer container = createContainerMirror2();
		
		if (container == null) return null;
		container.setDefaultGenerator(String.class, idGenerator());
		container.addScanPackages(BaseWithTimestampDO.class.getPackage());
		return container;
	}
	
	@Bean @Qualifier("MySite")
	public HBaseDirectOperations hBaseDirectOperations() {
		HBaseDirectTemplate hBaseDirectTemplate = new HBaseDirectTemplate();
		hBaseDirectTemplate.setContainer(hbaseMyContainer());
		return hBaseDirectTemplate;
	}

	@Bean @Qualifier("default")
	public HBaseOperations hbaseOperations() {
		HAManager haManager =  haManager();
		HBaseOperations operations1 = hBaseOperations1();
		HBaseOperations operations2 = hBaseOperations2();
		if (operations1 != null && operations2 != null) {
			return new HAHBaseMirrorTemplate(
					haManager,
					hBaseOperations1(),
					hBaseOperations2(),
				hbaseConfig.hbaseMySite);
		} else if (operations1 != null) {
			return operations1;
		} else if (operations2 != null) {
			return operations2;
		} else throw new IllegalArgumentException();
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (hbaseConfig.initializeData) {
			applicationContext.getBean(DataInitializer.class);
		}
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			@Override
			public void run() {
				HConnectionManager.deleteAllConnections(true);
			}
		}));
	}
	
	@Bean @Qualifier("Site1")
	public HBaseOperations hBaseOperations1() {
		return createHBaseOperations(hbaseContainer1());
	}

	@Bean @Qualifier("Site2")
	public HBaseOperations hBaseOperations2() {
		return createHBaseOperations(hbaseContainer2());
	}
	
	@Bean @Qualifier("MySite")
	public HBaseOperations hBaseOperationsMySite() {
		return hbaseConfig.hbaseMySite.equals(Site.SITE1) ? hBaseOperations1() : hBaseOperations2();
	}
	
	protected AbstractHBaseTemplate createHBaseOperations(HBaseContainer container) {
		if (container == null) return null;
		HBaseTemplate template = new HBaseTemplate();
		template.setAutoFlush(hbaseConfig.hbaseAutoFlushDefault);
		template.setContainer(container);
		if (System.getProperty("hbase.skip.locking") != null) {
			template.setSkipLocking(Boolean.valueOf(System.getProperty("hbase.skip.locking")));
		}
		return new ExternalEntityHBaseTemplate(container, template);
	}


	protected HBaseContainer createContainerMirror1() {
		if (hbaseConfig.hbaseMySite.equals(Site.SITE2) && hbaseConfig.hbaseUseOnlyMySite) return null;
		HBaseContainer container = createBaseContainer();
		container.addConfigFile(hbaseConfig.hbaseConfigFileMirror1);
		return container;
	}
	
	protected HBaseContainer createContainerMirror2() {
		if (!hbaseConfig.hbaseMirroring) return null;
		if (hbaseConfig.hbaseMySite.equals(Site.SITE1) && hbaseConfig.hbaseUseOnlyMySite) return null;
		HBaseContainer container = createBaseContainer();
		container.addConfigFile(hbaseConfig.hbaseConfigFileMirror2);
		return container;
	}
	
	protected HBaseContainer createBaseContainer() {
		HBaseContainer container = new HBaseContainer();
		if (!hbaseConfig.hbaseTableDefaultCompressionAlgorithm.equals(Compression.Algorithm.NONE)) {
			container.setDefaultCompressionAlgorithm(hbaseConfig.hbaseTableDefaultCompressionAlgorithm);			
		}
		container.setIndexIsEnabled(hbaseConfig.hbaseSecondaryIndexEnabled);
		container.setCreateTables(hbaseConfig.hbaseCreateTables);
		container.setDropTablesBeforeCreate(hbaseConfig.hbaseDropTablesBeforeCreate);
		container.setSkipCreatingTableIfExist(hbaseConfig.hbaseSkipCreatingTableIfExist);
		container.setTableNamePrefix(hbaseConfig.hbaseTableNamePrefix);
		container.setApplicationContext(applicationContext);
		return container;
	}

	@Override
	public void destroy() throws Exception {
		HConnectionManager.deleteAllConnections(true);
	}
 	
}
