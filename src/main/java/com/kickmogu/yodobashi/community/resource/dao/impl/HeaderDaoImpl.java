/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kickmogu.yodobashi.community.common.exception.CatalogAccessException;
import com.kickmogu.yodobashi.community.resource.aop.AopMethodCacheHandlerImpl;
import com.kickmogu.yodobashi.community.resource.cache.CacheStrategyType;
import com.kickmogu.yodobashi.community.resource.cache.MethodCache;
import com.kickmogu.yodobashi.community.resource.cache.TargetSystemType;
import com.kickmogu.yodobashi.community.resource.config.AppConfigurationDao;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.dao.HeaderDao;
import com.kickmogu.yodobashi.community.resource.domain.UrlMakerDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.HeaderType;
import com.kickmogu.yodobashi.community.resource.domain.constants.MaintenanceStatus;


/**
 * HeaderDAO の実装です。
 * @author imaizumi
 *
 */
@Service
public class HeaderDaoImpl implements HeaderDao, InitializingBean {

	public static final String PARAM_IS_TYPE = "type";
	public static final String PARAM_IS_SSL = "isSsl";
	public static final String PARAM_IS_ADULT = "isAdult";
	public static final String PARAM_RETURN_URL = "returnUrl";
	public static final String PARAM_ITEM_SEARCH_URL = "hostName";
	public static final String PARAM_IS_SEARCHWINDOW = "isSearchWindowView";
	
	private static final String universalHeaderSearchWindowStatusKey = "universalheader.searchwindow.status";
	
	/**
	 * リソースコンフィグです。
	 */
	@Autowired
	private ResourceConfig resourceConfig;
	
	@Autowired
	private AopMethodCacheHandlerImpl methodCacheHandler;
	
	@Autowired
	AppConfigurationDao appConfigurationDao;
	
	/**
	 * パターン
	 */
	private static final Pattern innerBodyPattern = Pattern.compile("<body>(.*?)</body>", Pattern.DOTALL);

	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger log = LoggerFactory.getLogger(HeaderDaoImpl.class);

	@Override
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			limitTime=30,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWeb}
			)
	public String getUniversalHeader( boolean isSsl, boolean isAdult, String returnUrl, String itemSearchUrl){
		
		return getBody( new UrlMakerDO(HeaderType.UNIVERSAL, isSsl, isAdult, returnUrl, itemSearchUrl) );
	}
	
	@Override
	public void removeUniversalHeader( boolean isSsl, boolean isAdult, String returnUrl, String itemSearchUrl) throws SecurityException, NoSuchMethodException{
		Method method = HeaderDaoImpl.class.getMethod("getUniversalHeader", new Class<?>[]{boolean.class,boolean.class, String.class, String.class});
		methodCacheHandler.clearCache(method);
	}
	@Override
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			limitTime=30,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWeb}
			)
	public String getCommonHeader( boolean isSsl, boolean isAdult, String returnUrl, String itemSearchUrl){
		return getBody( new UrlMakerDO(HeaderType.COMMON, isSsl, isAdult, returnUrl, itemSearchUrl) );
	}
	
	@Override
	public void removeCommonHeader(boolean isSsl, boolean isAdult, String returnUrl, String itemSearchUrl) throws SecurityException, NoSuchMethodException{
		Method method = HeaderDaoImpl.class.getMethod("getCommonHeader", new Class<?>[]{boolean.class,boolean.class, String.class, String.class});
		methodCacheHandler.clearCache(method);
	}
	@Override
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			limitTime=30,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWeb}
			)
	public String getImportUrl(){
		return getBody( new UrlMakerDO(HeaderType.IMPORTURL, null, null, null, null) );
	}
	
	@Override
	public void removeImportUrl() throws SecurityException, NoSuchMethodException{
		Method method = HeaderDaoImpl.class.getMethod("getImportUrl", new Class<?>[]{});
		methodCacheHandler.clearCache(method);
	}
	@Override
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			limitTime=30,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWeb}
			)
	public String getPath( boolean isSsl ){
		return getBody( new UrlMakerDO(HeaderType.PATH, isSsl, null, null, null) );
	}
	
	@Override
	public void removePath( boolean isSsl ) throws SecurityException, NoSuchMethodException{
		Method method = HeaderDaoImpl.class.getMethod("getPath", new Class<?>[]{boolean.class});
		methodCacheHandler.clearCache(method);
	}

	private String getBody( UrlMakerDO urlMakerDo ){
		CloseableHttpClient client = HttpClients.createDefault();
		
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(resourceConfig.headerApiSocketTimeout)
				.setConnectTimeout(resourceConfig.headerApiConnectionTimeout)
				.build();

		String body = "";
		try{
			URI uri = makeUri(urlMakerDo);
			
			log.info("targetUrl:"+uri.toString());
			
			HttpGet httpGet = new HttpGet(uri);
			httpGet.setConfig(requestConfig);
			
			CloseableHttpResponse response = client.execute(httpGet);
			try{
				if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
					throw new CatalogAccessException("catalog header access error (code:" + response.getStatusLine().getStatusCode() + ") from " + uri.toString());
				}
				body = EntityUtils.toString(response.getEntity(), "UTF-8");
				if (HeaderType.UNIVERSAL.equals(urlMakerDo.getHeaderType()) || HeaderType.COMMON.equals(urlMakerDo.getHeaderType())) {
					Matcher matcher = innerBodyPattern.matcher(body);
					if (matcher.find()) {
						body = matcher.group(1);
					} else {
						throw new CatalogAccessException("catalog header access error (pattern match error) from " + uri.toString());
					}
				}
			}finally{
				response.close();
			}
		}catch(Exception e){
			log.info("HeaderDao.getBody", e);
		}finally{
			try {
				client.close();
			} catch (IOException e) {
				log.error("HeaderDao.getBody", e);
			}
		}
		
		return body;
	}
	
	private URI makeUri(UrlMakerDO urlMakerDo) throws URISyntaxException{
		URIBuilder uriBuilder = null;
		
		if (HeaderType.UNIVERSAL.equals(urlMakerDo.getHeaderType()) || HeaderType.COMMON.equals(urlMakerDo.getHeaderType())) {
			uriBuilder = new URIBuilder(resourceConfig.headerCatalogApiUrl);
		}else{
			uriBuilder = new URIBuilder(resourceConfig.headerApiUrl);
		}
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(PARAM_IS_TYPE, urlMakerDo.getHeaderType().code));
		if( null!=urlMakerDo.getSsl() ){
			params.add(new BasicNameValuePair(PARAM_IS_SSL, urlMakerDo.getSsl()?"true":"false"));
		}
		if( null!=urlMakerDo.getAdult() ) {
			params.add(new BasicNameValuePair(PARAM_IS_ADULT, urlMakerDo.getAdult()?"true":"false"));
		}
		if( !StringUtils.isEmpty(urlMakerDo.getReturnUrl()) ){
			params.add(new BasicNameValuePair(PARAM_RETURN_URL, urlMakerDo.getReturnUrl()));
		}
		if( !StringUtils.isEmpty(urlMakerDo.getItemSearchUrl()) ){
			params.add(new BasicNameValuePair(PARAM_ITEM_SEARCH_URL, urlMakerDo.getItemSearchUrl()));
		}
		
		MaintenanceStatus searchWindowStatus = MaintenanceStatus.codeOf(appConfigurationDao.getWithCache(universalHeaderSearchWindowStatusKey));
		
		if( searchWindowStatus != null && MaintenanceStatus.IN_OPERATION.equals(searchWindowStatus)){
			params.add(new BasicNameValuePair(PARAM_IS_SEARCHWINDOW, "true"));
		}
		
		uriBuilder.setParameters(params);
		
		return uriBuilder.build();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}
}
