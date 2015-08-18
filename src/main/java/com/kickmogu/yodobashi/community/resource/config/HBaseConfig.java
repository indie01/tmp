package com.kickmogu.yodobashi.community.resource.config;

import java.net.InetAddress;

import org.apache.hadoop.hbase.io.hfile.Compression;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.kickmogu.lib.core.cofig.BaseConfig;
import com.kickmogu.lib.core.dynamicprop.DynamicObjectCreaters;
import com.kickmogu.lib.core.dynamicprop.DynamicPropertyAccessors;
import com.kickmogu.lib.core.resource.Site;
import com.kickmogu.lib.core.utils.Asserts;

@Configuration
public class HBaseConfig extends BaseConfig {

	public static HBaseConfig INSTANCE;
	

	@Value("${initialize.data}")
	public boolean initializeData;
	

	@Value("${dynamicAccessor.useReflection}")
	public boolean dynamicAccessorUseReflection;

	@Value("${dynamicAccessor.lazyCompile}")
	public boolean dynamicAccessorLazyCompile;	
	
	@Value("${hbase.mirroring}")
	public boolean hbaseMirroring;
	
	@Value("${hbase.use.only.mySite}")
	public boolean hbaseUseOnlyMySite;
	
	@Value("${hbase.mysite}")
	public Site hbaseMySite;
	
	@Value("${hbase.config.file.mirror1}")
	public String hbaseConfigFileMirror1;

	@Value("${hbase.config.file.mirror2}")
	public String hbaseConfigFileMirror2;	
	
	private String hbaseMyConfigFile;
	
	@Value("${hbase.create.tables}")
	public boolean hbaseCreateTables;
	
	@Value("${hbase.secondary.index.enabled}")
	public boolean hbaseSecondaryIndexEnabled;
	

	@Value("${hbase.drop.tables.before.create}")
	public boolean hbaseDropTablesBeforeCreate;

	@Value("${hbase.skip.creating.table.if.exist}")
	public boolean hbaseSkipCreatingTableIfExist;

	@Value("${hbase.auto.flush.default}")
	public boolean hbaseAutoFlushDefault;

	@Value("${hbase.table.name.prefix}")
	public String hbaseTableNamePrefix;

	@Value("${hbase.table.default.compression.algorithm}")
	public Compression.Algorithm hbaseTableDefaultCompressionAlgorithm;
	
	public String hostname;

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		hostname = InetAddress.getLocalHost().getHostName();
		DynamicPropertyAccessors.setUseReflection(dynamicAccessorUseReflection);
		DynamicPropertyAccessors.setLazyCompile(dynamicAccessorLazyCompile);
		DynamicObjectCreaters.setUseReflection(dynamicAccessorUseReflection);
		DynamicObjectCreaters.setLazyCompile(dynamicAccessorLazyCompile);
		initializeHBaseMyConfigFile();
	}
	
	private void initializeHBaseMyConfigFile() {
		Asserts.notNull(hbaseMySite);
		hbaseMyConfigFile = hbaseMySite.equals(Site.SITE1) ? hbaseConfigFileMirror1 : hbaseConfigFileMirror2;
	}
	
	public String getHBaseMyConfigFile() {
		if (hbaseMyConfigFile == null) {
			initializeHBaseMyConfigFile();
		}
		return hbaseMyConfigFile;
	}
}
