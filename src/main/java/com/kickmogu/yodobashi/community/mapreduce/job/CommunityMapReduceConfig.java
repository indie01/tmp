package com.kickmogu.yodobashi.community.mapreduce.job;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.kickmogu.hadoop.mapreduce.job.MapReduceConfig;
import com.kickmogu.lib.core.resource.Site;

@Configuration
public class CommunityMapReduceConfig extends MapReduceConfig {

	public static MapReduceConfig INSTANCE;

	@Value("${hbase.mysite}")
	public Site hbaseMySite;

	/**
	 * JobTrackerのあて先です。
	 */
	@Value("${job.tracker.address.site1}")
	public String jobTrackerAddressSite1;

	/**
	 * JobTrackerで使用するファイルシステムです。
	 */
	@Value("${job.tracker.fs.site1}")
	public String jobTrackerFileSystemSite1;

	/**
	 * JobTrackerのあて先です。
	 */
	@Value("${job.tracker.address.site2}")
	public String jobTrackerAddressSite2;

	/**
	 * JobTrackerで使用するファイルシステムです。
	 */
	@Value("${job.tracker.fs.site2}")
	public String jobTrackerFileSystemSite2;

	/**
	 * UU閲覧数ログファイルパスです。
	 */
	@Value("${uniqueUserViewLogCountJob.input}")
	public String uniqueUserViewLogCountJobInput;

	/**
	 * アクセスログの保存サーバのホスト名です。
	 */
	public String accessLogServerHost;

	/**
	 * アクセスログの保存サーバのユーザー名です。
	 */
	public String accessLogServerUser;

	/**
	 * アクセスログの保存サーバのパスワードです。
	 */
	public String accessLogServerPassword;

	/**
	 * アクセスログの保存サーバのログ保存ディレクトリです。
	 */
	public String accessLogServerSaveDir;

	public String getMySitePropertyValue(String key) {
		try  {
			return resolver.resolveStringValue("${" + hbaseMySite.name() + "." + key +"}");
		} catch (IllegalArgumentException e) {
		}

		return getPropatyValue(key);
	}

	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		accessLogServerHost = getMySitePropertyValue("server.accesslog.host");
		accessLogServerUser = getMySitePropertyValue("server.accesslog.user");
		accessLogServerPassword = getMySitePropertyValue("server.accesslog.password");
		accessLogServerSaveDir = getMySitePropertyValue("server.accesslog.saveDir");
	}

}
