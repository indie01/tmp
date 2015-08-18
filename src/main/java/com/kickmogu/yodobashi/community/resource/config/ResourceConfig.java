package com.kickmogu.yodobashi.community.resource.config;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.kickmogu.lib.core.cofig.BaseConfig;
import com.kickmogu.lib.core.resource.Site;
import com.kickmogu.lib.core.resource.Zone;
import com.kickmogu.yodobashi.community.resource.domain.ReviewMetaDO;

@Configuration
public class ResourceConfig extends BaseConfig {

	public static ResourceConfig INSTANCE;
			
	@Value("${enable.hazelcast.method.cache}")
	public boolean enableHazelcastMethodCache;
	
	@Value("${hazelcast.method.cache.port}")
	public int hazelcastMethodCachePort;
	
	@Value("${hazelcast.method.cache.multicast.group}")
	public String hazelcastMethodCacheMulticastGroup;
	
	@Value("${hazelcast.method.cache.multicast.port}")
	public int hazelcastMethodCacheMulticastPort;
	
	@Value("${solr.enable.master.monitor}")
	public boolean solrEnableMasterMonitor;
	
	@Value("${solr.enable.webserver.monitor}")
	public boolean solrEnableWebServerMonitor;
	
	@Value("${hbase.monitor.enable}")
	public boolean hbaseMonitorEnable;
	
	@Value("${hbase.monitor.waitUntilAlive}")
	public boolean hbaseMonitorWaitUntilAlive;
	
	@Value("${hbase.monitor.waitTimeout}")
	public long hbaseMonitorWaitTimeout;
	
	@Value("${enable.method.cache}")
	public boolean enableMethodCache;

	@Value("${message.force.sync.now}")
	public boolean messageForceSyncNow;

	@Value("${timeAdjustable}")
	public boolean timeAdjustable;

	@Value("${myZone}")
	public Zone myZone;

	@Value("${hbase.mysite}")
	public Site hbaseMySite;

	@Value("${solr.mysite}")
	public Site solrMySite;

	@Value("${solr.mirroring}")
	public boolean solrMirroring;

	@Value("${solr.use.only.mySite}")
	public boolean solrUseOnlyMySite;

	@Value("${xi.timeout}")
	public Integer xiTimeout;

	@Value("${external.system.id}")
	public String externalSystemId;

	@Value("${community.outerCustomerType}")
	public String communityOuterCustomerType;

	@Value("${product.image.url}")
	public String productImageUrl;
	
	@Value("${catalog.url}")
	public String catalogUrl;
	
	
	@Value("${image.url}")
	public String imageUrl;

	@Value("${image.temporary.url}")
	public String temporaryImageUrl;

	@Value("${image.temporary.url.relative}")
	public String temporaryImageUrlRelative;
	
	//image.uploadPath
	@Value("${image.uploadPath}")
	public String imageUploadPath;

	@Value("${imageServer.mirroring}")
	public boolean imageServerMirroring;

	@Value("${imageServer.host.primary}")
	public String imageServerHostPrimary;

	@Value("${imageServer.user.primary}")
	public String imageServerUserPrimary;

	@Value("${imageServer.password.primary}")
	public String imageServerPasswordPrimary;

	@Value("${imageServer.saveDir.primary}")
	public String imageServerSaveDirPrimary;
	
	@Value("${imageServer.stop.primary}")
	public boolean imageServerStopPrimary;

	@Value("${imageServer.host.secondary}")
	public String imageServerHostSecondary;

	@Value("${imageServer.user.secondary}")
	public String imageServerUserSecondary;

	@Value("${imageServer.password.secondary}")
	public String imageServerPasswordSecondary;

	@Value("${imageServer.saveDir.secondary}")
	public String imageServerSaveDirSecondary;

	@Value("${imageServer.stop.secondary}")
	public boolean imageServerStopSecondary;
	
	@Value("${comment.init.read.limit}")
	public int commentInitReadLimit;
	
	@Value("${evaluationArea.like.read.limit}")
	public int evaluationAreaLikeReadLimit;
	

	/**
	 * ヘッダーAPIのURL(WS)
	 */
	public String headerApiUrl;
	
	/**
	 * ヘッダーAPIのURL(catalog)
	 */
	public String headerCatalogApiUrl;


	/**
	 * ヘッダーAPI用ソケットタイムアウト値です。
	 */
	public Integer headerApiSocketTimeout;

	/**
	 * ヘッダーAPI用コネクションタイムアウト値です。
	 */
	public Integer headerApiConnectionTimeout;
	
	/**
	 * アカマイユーザーです。
	 */
	@Value("${akamai.user}")
	public String akamaiUser;

	/**
	 * アカマイパスワードです。
	 */
	@Value("${akamai.password}")
	public String akamaiPassword;

	/**
	 * アカマイBaseURLです。
	 */
	@Value("${akamai.baseUrl}")
	public String akamaiBaseUrl;

	/**
	 * アカマイE-Mailです。
	 */
	@Value("${akamai.email}")
	public String akamaiEMail;

	/**
	 * カタログ商品情報取得インターフェースのエンドポイントです。
	 */
	public String catalogProductEndpoint;
	/**
	 * カタログの商品のショッピングカートボタン取得のインターフェースのエンドポイント
	 */
	public String catalogCartEndpoint;

	/**
	 * カタログサジェストインターフェースのエンドポイントです。
	 */
	public String catalogSuggestEndpoint;

	/**
	 * カタログ用ソケットタイムアウト値です。
	 */
	public Integer catalogWsSocketTimeout;

	/**
	 * カタログ用コネクションタイムアウト値です。
	 */
	public Integer catalogWsConnectionTimeout;
	
	/**
	 * カタログのAPIモードです。
	 */
	@Value("${site.deployment.type}")
	public String siteDeploymentType;

	/**
	 * カタログのオートログインです。
	 */
	@Value("${catalog.autoid}")
	public String catalogCookieAutoid;
	
	/**
	 * カタログのオートログインの有効期限です。
	 */
	@Value("${catalog.autoid.expire}")
	public int catalogCookieAutoidExpire;

	/**
	 * カタログのカートIDです。
	 */
	@Value("${catalog.cartId}")
	public String catalogCookieCartId;

	/**
	 * カタログの納期回答キーです。
	 */
	@Value("${catalog.yatpz}")
	public String catalogCookieYatpz;
	
	/**
	 * ValidateAuthSessionV2の通常モードアクセス期限用クッキー
	 */
	@Value("${catalog.validsession}")
	public String catalogValidSession;
	
	/**
	 * カタログのYIDです。
	 */
	@Value("${catalog.yid}")
	public String catalogCookieYid;

	/**
	 * ローカルIPアドレスのリストパターンです。
	 */
	public List<Pattern> localIpAddrPatterrns;

	/**
	 * イントラネットワークのドメイン名です。
	 */
	@Value("${intra.domain.name}")
	public String intraDomainName;
	
	@Value("${community.login.expire}")
	public long loginExpire;

	/**
	 * ポイント管理システムのリクエストのスキームです。
	 */
	public String pmsSchema;
	/**
	 * ポイント管理システムのホスト名です。
	 */
	public String pmsHost;
	
	/**
	 * ポイント管理システムのポート番号です。
	 */
	public Integer pmsPort;
	/**
	 * ポイント管理システムインターフェースのエンドポイント（登録）です。
	 */
	public String pmsFrontEntryEndpoint;

	/**
	 * ポイント管理システムインターフェースのエンドポイント（キャンセル）です。
	 */
	public String pmsFrontCancelEndpoint;

	/**
	 * ポイント管理システムインターフェースのエンドポイント（特別ポイント予約）です。
	 */
	public String pmsFrontReserveEndpoint;

	/**
	 * ポイント管理システムインターフェースのエンドポイント（移行）です。
	 */
	public String pmsFrontMigrateEndpoint;

	/**
	 * ポイント管理システムインターフェースのエンドポイント（移行キャンセル）です。
	 */
	public String pmsFrontCancelMigrateEndpoint;
	
	/**
	 * ポイント管理システムインターフェースのエンドポイント（特別条件レビューポイントチェック）です。
	 */
	public String pmsFrontConfirmReviewPointSpecialConditionEndpoint;
	
	/**
	 * ポイント管理システムのエンドポイント（ステータス更新可能なポイント付与申請情報取得）です。
	 */
	public String pmsAdminFindMutableEntryEndpoint;
	
	/**
	 * ポイント管理システムのエンドポイント（指定のポイント付与申請ID一覧からポイント付与申請情報一覧を取得）です。
	 */
	public String pmsAdminFindEntryEndpoint;
	
	/**
	 * ポイント管理システムのエンドポイント（ステータス更新）です。
	 */
	public String pmsAdminUpdateEntryExecuteStatusEndpoint;
	
	/**
	 * ポイント管理システムのエンドポイント（サービスの閉塞）です。
	 */
	public String pmsAdminCloseServiceEndpoint;
	
	/**
	 * ポイント管理システムのエンドポイント（サービスの開始）です。
	 */
	public String pmsAdminOpenServiceEndpoint;
	
	/**
	 * ポイント管理システムのエンドポイント（サービスの状態取得）です。
	 */
	public String pmsAdminIsServiceEndpoint;
	
	/**
	 * akamai 処理をスキップするかどうか
	 */
	@Value("${akamaiSkip}")
	public boolean akamaiSkip;

	/**
	 * 画像サムネイル作成時の横幅です。
	 */
	@Value("${thumbnail.width}")
	public Integer thumbnailWidth;

	/**
	 * 画像サムネイル作成時の縦幅です。
	 */
	@Value("${thumbnail.height}")
	public Integer thumbnailHeight;


	@Value("${pms.externalSystem}")
	public String pmsExternalSystem;

	@Value("${mypage.purchaseProduct.searchSolrLimit}")
	public int mypagePurchaseProductSearchSolrLimit;
	
	@Value("${reviewMeta.classPath}")
	public String reviewMetaClassPath;
	
	/**
	 * ボットエージェント名のリストです。
	 */
	public String[] botAgentNames;

	public String hostname;

	/**
	 * カタログ用ソケットタイムアウト値です。
	 */
	public Integer pmsSocketTimeout;

	/**
	 * カタログ用コネクションタイムアウト値です。
	 */
	public Integer pmsConnectionTimeout;
	
	/**
	 * レビューのMeta情報一覧
	 */
	public Map<String, ReviewMetaDO> reviewMetas;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		hostname = InetAddress.getLocalHost().getHostName();

		String[] localIpAddrs = getPropatyValue("local.ipaddress").split(",");
		localIpAddrPatterrns = new ArrayList<Pattern>();
		for (String ipaddress : localIpAddrs) {
			try {
				localIpAddrPatterrns.add(Pattern.compile(ipaddress));
			} catch (RuntimeException e) {
			}
		}
		if (!localIpAddrPatterrns.isEmpty()) {
			localIpAddrPatterrns = Collections.unmodifiableList(localIpAddrPatterrns);
		}
		botAgentNames = getPropatyValue("bot.useragent.names").split(",");
		catalogProductEndpoint = getMySiteAndZonePropertyValue("catalog.product.endpoint");
		catalogCartEndpoint = getMySiteAndZonePropertyValue("catalog.cart.endpoint");
		catalogSuggestEndpoint = getMySiteAndZonePropertyValue("catalog.suggest.endpoint");
		catalogWsSocketTimeout = Integer.valueOf(getMySiteAndZonePropertyValue("catalog.ws.socket.timeout"));
		catalogWsConnectionTimeout = Integer.valueOf(getMySiteAndZonePropertyValue("catalog.ws.connection.timeout"));
		pmsSchema = getMySitePropertyValue("pms.schema");
		pmsHost = getMySitePropertyValue("pms.host");
		pmsPort = Integer.valueOf(getMySitePropertyValue("pms.port"));
		pmsFrontEntryEndpoint = getMySitePropertyValue("pms.front.entry.endpoint");
		pmsFrontCancelEndpoint = getMySitePropertyValue("pms.front.cancel.endpoint");
		pmsFrontReserveEndpoint = getMySitePropertyValue("pms.front.reserve.endpoint");
		pmsFrontMigrateEndpoint = getMySitePropertyValue("pms.front.migrate.endpoint");
		pmsFrontCancelMigrateEndpoint = getMySitePropertyValue("pms.front.cancel.migrate.endpoint");
		pmsFrontConfirmReviewPointSpecialConditionEndpoint = getMySitePropertyValue("pms.front.confirm.specialcondition.endpoint");
		pmsAdminFindMutableEntryEndpoint = getMySitePropertyValue("pms.admin.find.mutable.entry.endpoint");
		pmsAdminFindEntryEndpoint = getMySitePropertyValue("pms.admin.find.entry.endpoint");
		pmsAdminUpdateEntryExecuteStatusEndpoint = getMySitePropertyValue("pms.admin.update.entry.execute.status.endpoint");
		pmsAdminCloseServiceEndpoint = getMySitePropertyValue("pms.admin.closeservice.endpoint");
		pmsAdminOpenServiceEndpoint = getMySitePropertyValue("pms.admin.openservice.endpoint");
		pmsAdminIsServiceEndpoint = getMySitePropertyValue("pms.admin.isservice.endpoint");
		pmsSocketTimeout = Integer.valueOf(getMySiteAndZonePropertyValue("pms.socket.timeout"));
		pmsConnectionTimeout = Integer.valueOf(getMySiteAndZonePropertyValue("pms.connection.timeout"));
		headerApiUrl = getMySitePropertyValue("header.api.url");
		headerCatalogApiUrl = getMySitePropertyValue("header.catalog.api.url");
		headerApiSocketTimeout = Integer.valueOf(getMySitePropertyValue("header.api.socket.timeout"));
		headerApiConnectionTimeout = Integer.valueOf(getMySitePropertyValue("header.api.connection.timeout"));
		// レビューMetaファイルを読み込む
		readReviewMeta();
	}

	public String getMySitePropertyValue(String key) {
		try  {
			return resolver.resolveStringValue("${" + hbaseMySite.name() + "." + key +"}");
		} catch (IllegalArgumentException e) {
		}

		return getPropatyValue(key);
	}

	public String getMySiteAndZonePropertyValue(String key) {
		try  {
			return resolver.resolveStringValue("${" + hbaseMySite.name() + "." + myZone.name() + "." + key +"}");
		} catch (IllegalArgumentException e) {
		}

		return getPropatyValue(key);
	}

	/**
	 * 指定された情報から、イントラサイトからのアクセスであるか否かを判定し、その結果を返します。
	 * @param serverName サーバー名
	 * @param remoteAddr リモートアドレス
	 */
	public static boolean isIntra(String serverName, String remoteAddr) {

		// ドメイン名のチェック
		boolean result = StringUtils.equals(serverName, INSTANCE.intraDomainName);
		if (!result) {
			return result;
		}

		if (remoteAddr != null) {
			for (Pattern pattern : INSTANCE.localIpAddrPatterrns) {
				if (pattern.matcher(remoteAddr).matches()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 指定されたリクエストのユーザエージェント情報を元に、
	 * 検索エンジンロボット(クローラ,ボット)による巡回アクセスかどうかを判定します。
	 * @param userAgent ユーザーエージェント情報
	 */
	public static boolean isBot(String userAgent) {

		if (userAgent != null) {
			// 検索エンジンロボットかどうかの判定
			for (String botAgentPattern : INSTANCE.botAgentNames) {
				if (userAgent.contains(botAgentPattern)) {
					return true;
				}
			}
		}

		return false;
	}

	public int getCommentInitReadLimit() {
		return commentInitReadLimit;
	}

	public int getEvaluationAreaLikeReadLimit() {
		return evaluationAreaLikeReadLimit;
	}
	
	private void readReviewMeta() throws JsonParseException, JsonMappingException, IOException{
		if( StringUtils.isBlank(reviewMetaClassPath))
			throw new RuntimeException("Review Meta File Classpath is blank");
		
		Resource res = new ClassPathResource(reviewMetaClassPath);
		
		if( res.getInputStream() == null )
			throw new RuntimeException("Review Meta File Read is null");
			
		ObjectMapper mapper = new ObjectMapper();
		
		TypeReference<Map<String, ReviewMetaDO>> typeRef = new TypeReference<Map<String, ReviewMetaDO>>(){};
		try{
			reviewMetas = mapper.readValue(res.getInputStream(),typeRef);
		}finally{
			res.getInputStream().close();
		}
	}
}
