package com.kickmogu.yodobashi.community.mapreduce.job;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.hadoop.configuration.JobFactoryBean;
import org.springframework.stereotype.Service;

import com.kickmogu.hadoop.mapreduce.job.util.OptionUtils;
import com.kickmogu.hadoop.mapreduce.job.util.ThreadsafeOptionBuilder;
import com.kickmogu.lib.core.utils.ConfigUtil;
import com.kickmogu.lib.hadoop.hbase.io.HBaseTableInputFormat;
import com.kickmogu.yodobashi.community.resource.config.HBaseConfig;

/**
 * cf:outerCustomerIdをカラムを持つテーブルに対して、cf:outerCustomerIdの重複なしの一覧テキストをmapreduceを使って出力する
 * クラスです。
 * 
 * @author yoshida
 */
@Service @Lazy @Scope("prototype")
public class GetOuterCustomerIdJob extends AbstractCommunityJob implements InitializingBean {

	private static String outputDirPath;
	// 
	@Bean @Scope("prototype")
	public HBaseTableInputFormat getOuterCustomerIdInputFormat() {
		HBaseTableInputFormat inputFormat = new HBaseTableInputFormat();
		inputFormat.setConf(extConfiguration);
		return inputFormat;
	}
	
	@Bean @Scope("prototype")
	public TextOutputFormat<Text, NullWritable> getOuterCustomerIdOutputFormat() {
		return new TextOutputFormat<Text, NullWritable>();
	}
	
	@Override
	public String getJobName() {
		return "getOuterCustomerId";
	}

	// entry point
	public static void main(String[] args) throws Exception {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("serviceContext.xml");
		int result = execute(applicationContext, args);
		if (result == 0) {
			System.out.println("successfull!! please copy output file in hdfs to local machine.");
			System.out.println("your output hdfs directory uri :" + outputDirPath);
			System.out.println("for example..");
			System.out.println("hadoop fs -copyToLocal /outerCustomerIDOutput_20120215031512/part-r-00000 ~/outerCustomerID.txt");
		}
		System.exit(result);
	}

	public static int execute(
			ApplicationContext parentContext,
			String[] args) throws Exception {
		return ToolRunner.run(new GetOuterCustomerIdJob(
				).setParentApplicationContext(parentContext),
				args);
	}

	// ここでscan
	@Bean @Scope("prototype")
	public Job getOuterCustomerId() throws Exception {
		JobFactoryBean factory = getJobFactoryBean();
		factory.setInputFormat(getOuterCustomerIdInputFormat());
		factory.setOutputFormat(getOuterCustomerIdOutputFormat());
		Job job = factory.getObject();
		
		TextOutputFormat.setOutputPath(job, new Path(outputDirPath));
		
		job.setMapperClass(GetOuterCustomerIdMapper.class);
		job.setReducerClass(GetOuterCustomerIdReducer.class);
		
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(NullWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(NullWritable.class);
		job.setNumReduceTasks(1);
		
		return job;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
	}
	
	@Override
	protected void initializeConfiguration(Properties properties, String[] args)
			throws Exception {
		
		super.initializeConfiguration(properties, args);
		
		Options options = new Options();
		options.addOption(
				new ThreadsafeOptionBuilder().withArgName("tableName")
			    .hasArg(true)
			    .isRequired(true)
			    .withDescription("テーブル名")
			    .withLongOpt("tableName")
			    .create("t")
		    );

		options.addOption(
				new ThreadsafeOptionBuilder().withArgName("directory")
			    .hasArg(true)
			    .isRequired(true)
			    .withDescription("HDFS出力先ディレクトリ(URI形式) hdfs://<hostName>:<portNo>/<output directory>")
			    .withLongOpt("directory")
			    .create("d")
		    );

		CommandLine cmd = OptionUtils.parseWithDefaultOption(this.getJobName(), options, args, properties);
		
		properties.setProperty(TableInputFormat.INPUT_TABLE, cmd.getOptionValue("t"));
		outputDirPath = cmd.getOptionValue("d");
				
		properties.setProperty(HBaseTableInputFormat.DELETE_FLG_FAMILY, "cf");

		HBaseConfig config = new HBaseConfig();
		ConfigUtil.loadProperties(getProfileName(), "resource-config", config);
		getConf().addResource(config.getHBaseMyConfigFile());
	}


	// inner class define	
	public static class GetOuterCustomerIdMapper extends Mapper<ImmutableBytesWritable, Result, Text, NullWritable> {

		@Override
		protected void map(ImmutableBytesWritable key, Result value, Context context)
				throws IOException, InterruptedException {
			
			String outerCustomerId = null;
			
			for (KeyValue keyValue : value.raw()) {
				
				String columnName = Bytes.toString(
						keyValue.getBuffer(),
						keyValue.getQualifierOffset(),
						keyValue.getQualifierLength());
				
				if (columnName.equals("outerCustomerId")) {
					
					outerCustomerId = Bytes.toString(
							keyValue.getBuffer(),
							keyValue.getValueOffset(),
							keyValue.getValueLength());

					break;
				}
			}
			context.write(new Text(outerCustomerId), NullWritable.get());
		}
	}
	
	public static class GetOuterCustomerIdReducer extends Reducer<Text, NullWritable, Text, NullWritable> {

		@Override
		protected void reduce(Text key, Iterable<NullWritable> val,
				Context context)
				throws IOException, InterruptedException {
			context.write(key, NullWritable.get());
		}

	}
}
