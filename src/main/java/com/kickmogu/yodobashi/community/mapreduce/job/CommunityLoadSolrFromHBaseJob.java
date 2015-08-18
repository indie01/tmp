package com.kickmogu.yodobashi.community.mapreduce.job;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.hadoop.configuration.JobFactoryBean;
import org.springframework.stereotype.Service;

import com.kickmogu.hadoop.mapreduce.job.util.OptionUtils;
import com.kickmogu.hadoop.mapreduce.job.util.ThreadsafeOptionBuilder;
import com.kickmogu.lib.core.utils.Asserts;
import com.kickmogu.lib.core.utils.ConfigUtil;
import com.kickmogu.lib.hadoop.hbase.impl.HBaseOperationUtils;
import com.kickmogu.lib.hadoop.hbase.io.HBaseTableInputFormat;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseColumnFamilyMeta;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseColumnMetaBase;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseMeta;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseTableMeta;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.yodobashi.community.resource.config.HBaseConfig;
import com.kickmogu.yodobashi.community.resource.hbase2solr.HBase2SolrConvertContext;
import com.kickmogu.yodobashi.community.resource.hbase2solr.HBase2SolrConverter;
import com.kickmogu.yodobashi.community.resource.hbase2solr.HBase2SolrConverterFactory;
import com.kickmogu.yodobashi.community.resource.hbase2solr.impl.HBase2SolrConverterUtil;

@Service @Lazy @Scope("prototype")
public class CommunityLoadSolrFromHBaseJob extends AbstractCommunityJob implements InitializingBean {

	@Autowired
	private HBase2SolrConverterFactory converterFactory;

	
	@Autowired
	private HBaseMeta hbaseMeta;

	
	@Autowired @Qualifier("MySite")
	private SolrOperations solrOperations;

	@Override
	public String getJobName() {
		return "communityLoadSolrFromHBase";
	}

	
	@SuppressWarnings("rawtypes")
	@Bean @Scope("prototype")
	public HBaseTableInputFormat communityLoadSolrFromHBaseInputFormat() {
		HBaseTableInputFormat inputFormat = new HBaseTableInputFormat();
		
		String tableName = extConfiguration.get(TableInputFormat.INPUT_TABLE);
		HBaseTableMeta tableMeta = hbaseMeta.getTableMetaByTableName(tableName);
		HBase2SolrConverter converter = converterFactory.createHBase2SolrConverter(tableMeta.getType());
		extConfiguration.set(HBaseTableInputFormat.DELETE_FLG_FAMILY, tableMeta.getDeleteFlgColumnFamilyMeta().getColumnFamilyName());
		
		inputFormat.setConf(extConfiguration);

		for (String propertyName:converter.getLoadHBasePropertyNames()) {
			if (tableMeta.getKeyMeta().getPropertyName().equals(propertyName)) continue;
			boolean hit = false;
			for (HBaseColumnMetaBase columnMeta : tableMeta.getAllColumnMeta()) {
				if (columnMeta.getColumnName().equals(propertyName)) {
					inputFormat.getScan().addColumn(columnMeta.getColumnFamilyNameAsBytes(), columnMeta.getColumnNameAsBytes());
					hit  = true;
					break;
				}
			}
			Asserts.isTrue(hit, propertyName);
		}
		return inputFormat;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean @Scope("prototype")
	public CommunityLoadSolrFromHBaseMapper communityLoadSolrFromHBaseMapper() {
		CommunityLoadSolrFromHBaseMapper mapper = new CommunityLoadSolrFromHBaseMapper();
		mapper.solrOperations = solrOperations;
		String tableName = extConfiguration.get(TableInputFormat.INPUT_TABLE);
		mapper.tableMeta = hbaseMeta.getTableMetaByTableName(tableName);
		mapper.type = hbaseMeta.getTableMetaByTableName(tableName).getType();
		mapper.converter =  converterFactory.createHBase2SolrConverter(mapper.type);
		return mapper;
	}
	
	@Bean @Scope("prototype")
	public Job communityLoadSolrFromHBase() throws Exception {
		JobFactoryBean factory = getJobFactoryBean();

		factory.setInputFormat(communityLoadSolrFromHBaseInputFormat());
		factory.setMapper(communityLoadSolrFromHBaseMapper());
		Job job = factory.getObject();

		job.setOutputFormatClass(NullOutputFormat.class);
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(NullWritable.class);
		job.setNumReduceTasks(0);
		return job;
	}
	
	@Override
	protected void initializeConfiguration(
			Properties properties,
			String[] args) throws Exception {
		super.initializeConfiguration(properties, args);

		Options options = new Options();
		options.addOption(
			new ThreadsafeOptionBuilder().withArgName("tableName")
		    .hasArg(true)
		    .isRequired(false)
		    .withDescription("tableName")
		    .withLongOpt("tableName")
		    .create("t")
	    );
		options.addOption(
			new ThreadsafeOptionBuilder().withArgName("timestamp")
		    .hasArg(true)
		    .isRequired(false)
		    .withDescription("timestamp")
		    .withLongOpt("timestamp")
		    .create("s")
	    );
		CommandLine cmd = OptionUtils.parseWithDefaultOption(this.getJobName(), options, args, properties);

		
        String tableName = cmd.getOptionValue("t");
		properties.setProperty(TableInputFormat.INPUT_TABLE, tableName);

		if (cmd.hasOption("s")) {
			properties.setProperty(TableInputFormat.SCAN_TIMERANGE_START, "0");
			properties.setProperty(TableInputFormat.SCAN_TIMERANGE_END, Long.valueOf(Long.valueOf(cmd.getOptionValue("s"))+1L).toString());			
		}
		
		properties.setProperty(TableInputFormat.SCAN_CACHEDROWS, "1000");
		
		HBaseConfig config = new HBaseConfig();
		ConfigUtil.loadProperties(getProfileName(), "resource-config", config);
		getConf().addResource(config.getHBaseMyConfigFile());


	}


	public static int execute(ApplicationContext parentApplicationContext, String[] args) throws Exception {
		return ToolRunner.run(new CommunityLoadSolrFromHBaseJob().setParentApplicationContext(parentApplicationContext), args);
	}


	public static void main(String[] args) throws Exception {
		int res = execute(null, args);
		System.out.println(res);
		System.exit(res);
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
	}

	public static class CommunityLoadSolrFromHBaseMapper<T> extends Mapper<ImmutableBytesWritable, Result, NullWritable, NullWritable> {

		private HBaseTableMeta tableMeta;
		
		private SolrOperations solrOperations;
		
		private Class<T> type;
		
		@SuppressWarnings("rawtypes")
		private HBase2SolrConverter converter;
		
		private HBaseColumnFamilyMeta columnFamilyMeta;
		
		@SuppressWarnings("rawtypes")
		private HBase2SolrConvertContext convertContext = new HBase2SolrConvertContext();
		
		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			columnFamilyMeta = tableMeta.getOnlyOneColumnFamilyMeta();
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void map(ImmutableBytesWritable key, Result result,
				Context context) throws IOException, InterruptedException {

			KeyValue[] kvs = result.raw();
			if (kvs.length == 0) return;

			T object = (T)tableMeta.createObject();
			tableMeta.getKeyMeta().setValue(object, result.getRow());
			boolean alive = HBaseOperationUtils.fillColumnValues(kvs, object, tableMeta, columnFamilyMeta);
			if (!alive) return;
			
			convertContext.getHbaseDataList().add(object);
			converter.convert(convertContext.getHbaseDataList(), convertContext);
			if (convertContext.totalHBaseDataSize() >= converter.getBulkSize()) {
				HBase2SolrConverterUtil.loadSolr(tableMeta, convertContext, solrOperations);
				convertContext.clear();
			}
		}

		@Override
		protected void cleanup(Context context) throws IOException,
				InterruptedException {
			if (convertContext.totalHBaseDataSize() > 0) {
				HBase2SolrConverterUtil.loadSolr(tableMeta, convertContext, solrOperations);
			}
			solrOperations.commit(type);
		}
		
	}

}
