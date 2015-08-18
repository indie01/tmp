package com.kickmogu.yodobashi.community.mapreduce.job;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.JobContext;
import org.springframework.beans.factory.annotation.Autowired;

import com.kickmogu.hadoop.mapreduce.job.AbstractMapReduceJob;

public abstract class AbstractCommunityJob extends AbstractMapReduceJob {

	/**
	 * 対象日キーです。
	 */
	public static final String TARGET_DATE = AbstractCommunityJob.class.getName() + ".targetDate";

	/**
	 * 日付フォーマットです。
	 */
	public static final String DATE_FORMATTER = "yyyy-MM-dd";

	protected String getJobTrackerAddress() {
		return CommunityMapReduceJobUtils.getMyJobTrackerAddress(this);
	}

	protected String getJobTrackerFileSystem() {
		return CommunityMapReduceJobUtils.getMyJobTrackerFileSystem(this);
	}

	@Autowired
	public void setCommunityMapReduceConfig(CommunityMapReduceConfig communityMapReduceConfig) {
		this.setMapReduceConfig(communityMapReduceConfig);
	}

	@Autowired
	public void setExtConfiguration(Configuration extConfiguration) {
		super.setExtConfiguration(extConfiguration);
	}

	public AbstractCommunityJob() {
		this.springConfigLocation = "classpath:/mr-context.xml";
		this.mapReduceConfigType = CommunityMapReduceConfig.class;
		this.mapReduceConfigName =  "mr-config";
	}


	@Override
	protected void initializeConfiguration(Properties properties, String[] args)
			throws Exception {
		CommunityMapReduceJobUtils.initializeConfiguration(this, properties, args);
	}

	protected static String getStringValue(KeyValue keyValue) {
		return Bytes.toString(
				keyValue.getBuffer(),
				keyValue.getValueOffset(),
				keyValue.getValueLength());
	}

	protected static boolean getBooleanValue(KeyValue keyValue) {
		return Bytes.toBoolean(new byte[]{keyValue.getBuffer()[keyValue.getValueOffset()]});
	}

	protected static int getIntegerValue(KeyValue keyValue) {
		return Bytes.toInt(keyValue.getBuffer(),
				keyValue.getValueOffset());
	}

	protected static long getLongValue(KeyValue keyValue) {
		return Bytes.toLong(
				keyValue.getBuffer(),
				keyValue.getValueOffset(),
				keyValue.getValueLength());
	}

	protected static float getFloatValue(KeyValue keyValue) {
		return Bytes.toFloat(keyValue.getBuffer(), keyValue.getValueOffset());
	}


	/**
	 * 対象日付文字列をチェックし、入力が無い場合は、当日日付の文字列を返します。
	 * @param targetDate 対象日付文字列
	 * @return 対象日付文字列
	 * @throws Exception
	 */
	protected static String getTargetDateString(String targetDate) throws Exception {
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMATTER);
		formatter.setLenient(false);
		if (targetDate == null) {
			targetDate = formatter.format(new Date());
		} else {
			if (!formatter.format(formatter.parse(targetDate)).equals(targetDate)) {
				throw new IllegalArgumentException("parameter is invalid. input = " + targetDate);
			}
		}
		return targetDate;
	}

	/**
	 * コンフィグ情報を上書きします。
	 * @param configuration コンフィグ情報
	 */
	@Override
	protected void overrideConfiguration(Configuration configuration) {
		configuration.set(JobContext.MAPREDUCE_TASK_CLASSPATH_PRECEDENCE, "true");
	}
}
