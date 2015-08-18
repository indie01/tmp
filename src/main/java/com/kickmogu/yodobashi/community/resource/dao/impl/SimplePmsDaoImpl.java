/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.http.Consts;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.resource.domain.BaseDO;
import com.kickmogu.yodobashi.community.common.exception.PmsAccessException;
import com.kickmogu.yodobashi.community.common.exception.PmsInputException;
import com.kickmogu.yodobashi.community.resource.config.ResourceConfig;
import com.kickmogu.yodobashi.community.resource.dao.SimplePmsDao;
import com.kickmogu.yodobashi.community.resource.domain.FindMutablePointGrantEntryResponseDO;
import com.kickmogu.yodobashi.community.resource.domain.FindPointGrantEntryResponseDO;
import com.kickmogu.yodobashi.community.resource.domain.PointGrantEntryDO;
import com.kickmogu.yodobashi.community.resource.domain.PointGrantEntryExecuteStatusDO;
import com.kickmogu.yodobashi.community.resource.domain.PointGrantEntryExecuteStatusResponseDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewPointSpecialConditionValidateDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewPointSpecialConditionValidateResponseDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.CancelPointGrantType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointExchangeType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PointGrantEntrySearchType;

/**
 * ポイント管理 DAO の実装です。
 * @author kamiike
 *
 */
@Service @Qualifier("pms")
public class SimplePmsDaoImpl implements SimplePmsDao {

	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger LOG = LoggerFactory.getLogger(SimplePmsDaoImpl.class);
	
	private static final String dateFormatPattern = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final DateFormat dateFormat = new SimpleDateFormat(dateFormatPattern);

	/**
	 * リソースコンフィグです。
	 */
	@Autowired
	private ResourceConfig resourceConfig;

	/**
	 * ポイント付与を申請します。
	 * @param externalCustomerId 外部顧客ID（コミュニティID）
	 * @param pointExchangeType ポイント交換種別（ポイント伝票タイプ）（01:レビュー投稿ポイント　02:ランキングポイント）
	 * @param pointGrantExecStartDate ポイント付与実行開始日（この日付以降でポイント付与実行を可能とする）
	 * @param pointValue ポイント数
	 * @param specialConditionCodes 特別ポイント条件コードリスト
	 * @return ポイント付与申請ID
	 */
	@Override
	public String entryPointGrant(
			String externalCustomerId,
			PointExchangeType pointExchangeType,
			Date pointGrantExecStartDate,
			Long pointValue,
			String[] specialConditionCodes) {
		if( externalCustomerId == null || pointExchangeType == null || pointGrantExecStartDate == null || pointValue == null )
			throw new PmsInputException("EntryPointGrant Input Value is None." + 
						" externalCustomerId = " + externalCustomerId +
						", pointExchangeType = " + pointExchangeType + 
						", pointGrantExecStartDate = " + pointGrantExecStartDate + 
						", pointValue = " + pointValue + 
						", specialConditionCodes = " + (specialConditionCodes!=null ? Arrays.asList(specialConditionCodes).toString() : "none" ));
		
		
		
		CloseableHttpClient client = HttpClients.createDefault();
		
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(resourceConfig.pmsSocketTimeout)
				.setConnectTimeout(resourceConfig.pmsConnectionTimeout)
				.build();
		
		CloseableHttpResponse response = null;
		
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("externalCustomerId", externalCustomerId));
			params.add(new BasicNameValuePair(
					"externalCustomerIdClass", resourceConfig.communityOuterCustomerType));
			params.add(new BasicNameValuePair("pointExchangeType", pointExchangeType.getCode()));
			params.add(new BasicNameValuePair("pointGrantExecStartDate",
					formatter.format(pointGrantExecStartDate)));
			params.add(new BasicNameValuePair("pointValue", pointValue.toString()));
			if (specialConditionCodes != null) {
				for (String specialConditionCode : specialConditionCodes) {
					params.add(new BasicNameValuePair("specialConditionCodes", specialConditionCode));
				}
			}
			
			URIBuilder uriBuilder = new URIBuilder();
			uriBuilder.setScheme(resourceConfig.pmsSchema);
			uriBuilder.setHost(resourceConfig.pmsHost);
			uriBuilder.setPort(resourceConfig.pmsPort);
			uriBuilder.setPath(resourceConfig.pmsFrontEntryEndpoint);
			URI uri = uriBuilder.build();
			
			HttpPost httpPost = new HttpPost(uri);
			httpPost.setConfig(requestConfig);
			httpPost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
			
			long startTime = System.currentTimeMillis();
			response = client.execute(httpPost);

			String body = EntityUtils.toString(response.getEntity(), "UTF-8");
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				ObjectMapper objectMapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, Visibility.ANY);
				objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				PointGrantEntryResponseDO result = objectMapper.readValue(body, PointGrantEntryResponseDO.class);
				if (LOG.isDebugEnabled()) {
					LOG.debug("pms entry access. time=" + (System.currentTimeMillis() - startTime)
							+ ", pointGrantRequestId=" + result.getPointGrantRequestId());
				}
				return result.getPointGrantRequestId();
			} else {
				throw new HttpException("status = " + response.getStatusLine().getStatusCode()
						+ ", time=" + (System.currentTimeMillis() - startTime)
						+ ", url=" + resourceConfig.pmsFrontEntryEndpoint
						+ ", body=" + body);
			}
		} catch (PmsAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new PmsAccessException(e);
		}finally{
			if( response != null ){
				try{
					response.close();
				}catch (Exception e) {
				}
			}
		}
	}

	/**
	 * 特別条件ポイントコードの付与を予約します。
	 * @param externalCustomerId 外部顧客ID（コミュニティID）
	 * @param specialConditionCode 特別ポイント条件コード
	 * @return 予約順、取れなかった場合、null
	 */
	@Override
	public Integer reserveSpecialArrivalPoint(
			String externalCustomerId,
			String specialConditionCode) {
		if( externalCustomerId == null || specialConditionCode == null )
			throw new PmsInputException("ReserveSpecialArrivalPoint Input Value is None." + 
						" externalCustomerId = " + externalCustomerId +
						", pointExchangeType = " + specialConditionCode);
		
		CloseableHttpClient client = HttpClients.createDefault();
		
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(resourceConfig.pmsSocketTimeout)
				.setConnectTimeout(resourceConfig.pmsConnectionTimeout)
				.build();
		
		CloseableHttpResponse response = null;
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("externalCustomerId", externalCustomerId));
			params.add(new BasicNameValuePair("externalCustomerIdClass", resourceConfig.communityOuterCustomerType));
			params.add(new BasicNameValuePair("specialConditionCode", specialConditionCode));
			
			URIBuilder uriBuilder = new URIBuilder();
			uriBuilder.setScheme(resourceConfig.pmsSchema);
			uriBuilder.setHost(resourceConfig.pmsHost);
			uriBuilder.setPort(resourceConfig.pmsPort);
			uriBuilder.setPath(resourceConfig.pmsFrontReserveEndpoint);
			URI uri = uriBuilder.build();
			
			HttpPost httpPost = new HttpPost(uri);
			httpPost.setConfig(requestConfig);
			httpPost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));

			long startTime = System.currentTimeMillis();
			response = client.execute(httpPost);
			
			String body = EntityUtils.toString(response.getEntity(), "UTF-8");
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				ObjectMapper objectMapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, Visibility.ANY);
				objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				ReserveSpecialArrivalPointEntryResponseDO result = objectMapper.readValue(body, ReserveSpecialArrivalPointEntryResponseDO.class);
				if (LOG.isDebugEnabled()) {
					LOG.debug("pms reserve access. time=" + (System.currentTimeMillis() - startTime)
							+ ", externalCustomerId=" + externalCustomerId
							+ ", arrivalPointRanking=" + result.getArrivalPointRanking());
					
				}
				return result.getArrivalPointRanking();
			} else {
				throw new HttpException("status = " + response.getStatusLine().getStatusCode()
						+ ", time=" + (System.currentTimeMillis() - startTime)
						+ ", url=" + resourceConfig.pmsFrontReserveEndpoint
						+ ", body=" + body);
			}
		} catch (PmsAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new PmsAccessException(e);
		}finally{
			if( response != null ){
				try{
					response.close();
				}catch (Exception e) {
				}
			}
		}
	}

	/**
	 * ポイント付与情報を移行します。
	 * @param externalCustomerId 外部顧客ID（コミュニティID）
	 * @param pointExchangeType ポイント交換種別（ポイント伝票タイプ）（01:レビュー投稿ポイント　02:ランキングポイント）
	 * @param pointGrantApprovalDate ポイント承認日時
	 * @param pointGrantDate ポイント付与日付
	 * @param pointValue ポイント数
	 * @return ポイント付与申請ID
	 */
	@Override
	public String migratePointGrant(
			String externalCustomerId,
			PointExchangeType pointExchangeType,
			Date pointGrantApprovalDate,
			Date pointGrantDate,
			Long pointValue) {
		if( externalCustomerId == null || pointExchangeType == null || pointGrantApprovalDate == null || pointGrantDate == null || pointValue == null)
			throw new PmsInputException("MigratePointGrant Input Value is None." + 
						" externalCustomerId = " + externalCustomerId +
						", pointExchangeType = " + pointExchangeType + 
						", pointGrantApprovalDate = " + pointGrantApprovalDate + 
						", pointGrantDate = " + pointGrantDate + 
						", pointValue = " + pointValue);
		
		CloseableHttpClient client = HttpClients.createDefault();
		
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(resourceConfig.pmsSocketTimeout)
				.setConnectTimeout(resourceConfig.pmsConnectionTimeout)
				.build();
		
		CloseableHttpResponse response = null;
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("externalCustomerId", externalCustomerId));
			params.add(new BasicNameValuePair(
					"externalCustomerIdClass", resourceConfig.communityOuterCustomerType));
			params.add(new BasicNameValuePair("pointExchangeType", pointExchangeType.getCode()));
			params.add(new BasicNameValuePair("pointGrantApprovalDate",
					formatter.format(pointGrantApprovalDate)));
			params.add(new BasicNameValuePair("pointGrantDate",
					formatter.format(pointGrantDate)));
			params.add(new BasicNameValuePair("pointValue", pointValue.toString()));
			
			URIBuilder uriBuilder = new URIBuilder();
			uriBuilder.setScheme(resourceConfig.pmsSchema);
			uriBuilder.setHost(resourceConfig.pmsHost);
			uriBuilder.setPort(resourceConfig.pmsPort);
			uriBuilder.setPath(resourceConfig.pmsFrontMigrateEndpoint);
			URI uri = uriBuilder.build();
			
			HttpPost httpPost = new HttpPost(uri);
			httpPost.setConfig(requestConfig);
			httpPost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));

			long startTime = System.currentTimeMillis();
			response = client.execute(httpPost);
			
			String body = EntityUtils.toString(response.getEntity(), "UTF-8");
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				
				ObjectMapper objectMapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, Visibility.ANY);
				objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				PointGrantEntryResponseDO result = objectMapper.readValue(body, PointGrantEntryResponseDO.class);
				if (LOG.isDebugEnabled()) {
					LOG.debug("pms migrate access. time=" + (System.currentTimeMillis() - startTime)
							+ ", pointGrantRequestId=" + result.getPointGrantRequestId());
				}
				return result.getPointGrantRequestId();
			} else {
				throw new HttpException("status = " + response.getStatusLine().getStatusCode()
						+ ", time=" + (System.currentTimeMillis() - startTime)
						+ ", url=" + resourceConfig.pmsFrontMigrateEndpoint
						+ ", body=" + body);
			}
		} catch (PmsAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new PmsAccessException(e);
		} finally {
			if( response != null ){
				try{
					response.close();
				}catch (Exception e) {
				}
			}
		}
	}

	/**
	 * ポイント付与申請を取り下げます。
	 * @param pointGrantRequestId ポイント付与申請ID
	 * @param cancelReasonType キャンセル理由タイプ
	 */
	@Override
	public void cancelPointGrant(
			String pointGrantRequestId,
			CancelPointGrantType cancelReasonType) {
		if( pointGrantRequestId == null || cancelReasonType == null)
			throw new PmsInputException("CancelPointGrant Input Value is None." + 
						" pointGrantRequestId = " + pointGrantRequestId +
						", cancelReasonType = " + cancelReasonType);
		
		CloseableHttpClient client = HttpClients.createDefault();
		
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(resourceConfig.pmsSocketTimeout)
				.setConnectTimeout(resourceConfig.pmsConnectionTimeout)
				.build();
		
		CloseableHttpResponse response = null;
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("pointGrantRequestId", pointGrantRequestId));
			params.add(new BasicNameValuePair("cancelReasonType", cancelReasonType.getCode()));
			
			URIBuilder uriBuilder = new URIBuilder();
			uriBuilder.setScheme(resourceConfig.pmsSchema);
			uriBuilder.setHost(resourceConfig.pmsHost);
			uriBuilder.setPort(resourceConfig.pmsPort);
			uriBuilder.setPath(resourceConfig.pmsFrontCancelEndpoint);
			// TODO 確認する
			//uriBuilder.setCustomQuery(URLEncodedUtils.format(params, "UTF-8"));
			uriBuilder.setParameters(params);
			URI uri = uriBuilder.build();

			HttpGet getMethod = new HttpGet(uri);
			getMethod.setConfig(requestConfig);
			
			long startTime = System.currentTimeMillis();
			response = client.execute(getMethod);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("pms cancel access. time=" + (System.currentTimeMillis() - startTime));
				}
			}else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
				if (LOG.isWarnEnabled()) {
					LOG.warn("status = " + response.getStatusLine().getStatusCode()
							+ ", time=" + (System.currentTimeMillis() - startTime)
							+ ", url=" + resourceConfig.pmsFrontCancelEndpoint
							+ ", params=" + URLEncodedUtils.format(params, "UTF-8"));
				}
			} else {
				String body = EntityUtils.toString(response.getEntity(), "UTF-8");
				throw new HttpException("status = " + response.getStatusLine().getStatusCode()
						+ ", time=" + (System.currentTimeMillis() - startTime)
						+ ", url=" + resourceConfig.pmsFrontCancelEndpoint
						+ ", body=" + body);
			}
		} catch (PmsAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new PmsAccessException(e);
		} finally {
			if( response != null ){
				try{
					response.close();
				}catch (Exception e) {
				}
			}
		}
	}

	/**
	 * ポイント付与情報を移行をキャンセルします。
	 * @param pointGrantRequestId ポイント付与申請ID
	 */
	@Override
	public void cancelMigratePointGrant(
			String pointGrantRequestId) {
		if( pointGrantRequestId == null )
			throw new PmsInputException("CancelMigratePointGrant Input Value is None. " +
							"pointGrantRequestId = " + pointGrantRequestId);
		
		CloseableHttpClient client = HttpClients.createDefault();
		
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(resourceConfig.pmsSocketTimeout)
				.setConnectTimeout(resourceConfig.pmsConnectionTimeout)
				.build();
		
		CloseableHttpResponse response = null;
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("pointGrantRequestId", pointGrantRequestId));

			URIBuilder uriBuilder = new URIBuilder();
			uriBuilder.setScheme(resourceConfig.pmsSchema);
			uriBuilder.setHost(resourceConfig.pmsHost);
			uriBuilder.setPort(resourceConfig.pmsPort);
			uriBuilder.setPath(resourceConfig.pmsFrontCancelMigrateEndpoint);
			// TODO 確認する
			//uriBuilder.setCustomQuery(URLEncodedUtils.format(params, "UTF-8"));
			uriBuilder.setParameters(params);
			URI uri = uriBuilder.build();
			
			HttpGet getMethod = new HttpGet(uri);
			getMethod.setConfig(requestConfig);
			
			long startTime = System.currentTimeMillis();
			response = client.execute(getMethod);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("pms cancel migration access. time=" + (System.currentTimeMillis() - startTime));
				}
			} else {
				String body = EntityUtils.toString(response.getEntity(), "UTF-8");
				throw new HttpException("status = " + response.getStatusLine().getStatusCode()
						+ ", time=" + (System.currentTimeMillis() - startTime)
						+ ", url=" + resourceConfig.pmsFrontCancelMigrateEndpoint
						+ ", body=" + body);
			}
		} catch (PmsAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new PmsAccessException(e);
		} finally {
			if( response != null ){
				try{
					response.close();
				}catch (Exception e) {
				}
			}
		}
	}

	@Override
	public SearchResult<PointGrantEntryDO> findMutablePointGrantEntry(
			String externalCustomerId,
			Set<String> pointGrantRequestIds,
			Set<PointGrantEntrySearchType> searchTypes, 
			Long limit, 
			Long offset) {
		CloseableHttpClient client = HttpClients.createDefault();
		
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(resourceConfig.pmsSocketTimeout)
				.setConnectTimeout(resourceConfig.pmsConnectionTimeout)
				.build();
		
		CloseableHttpResponse response = null;
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			
			if( externalCustomerId != null )
				params.add(new BasicNameValuePair("externalCustomerId", externalCustomerId));
			
			if( pointGrantRequestIds != null && !pointGrantRequestIds.isEmpty() ){
				for(String pointGrantRequestId : pointGrantRequestIds){
					params.add(new BasicNameValuePair("pointGrantRequestIds", pointGrantRequestId));
				}
			}
			if( searchTypes != null && !searchTypes.isEmpty()){
				Iterator<PointGrantEntrySearchType> it = searchTypes.iterator();
				while( it.hasNext())
					params.add(new BasicNameValuePair("searchTypes.searchType", it.next().getCode()));
			}
			if( limit != null )
				params.add(new BasicNameValuePair("limit", limit.toString()));
			if( offset != null)
				params.add(new BasicNameValuePair("offset", offset.toString()));
			
			URIBuilder uriBuilder = new URIBuilder();
			uriBuilder.setScheme(resourceConfig.pmsSchema);
			uriBuilder.setHost(resourceConfig.pmsHost);
			uriBuilder.setPort(resourceConfig.pmsPort);
			uriBuilder.setPath(resourceConfig.pmsAdminFindMutableEntryEndpoint);
			// TODO 確認する
			if( !params.isEmpty() ){
				//uriBuilder.setCustomQuery(URLEncodedUtils.format(params, "UTF-8"));
				uriBuilder.setParameters(params);
			}
			URI uri = uriBuilder.build();
			
			HttpGet httpGet = new HttpGet(uri);
			httpGet.setConfig(requestConfig);
			
			long startTime = System.currentTimeMillis();
			response = client.execute(httpGet);
			
			String body = EntityUtils.toString(response.getEntity(), "UTF-8");
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				ObjectMapper objectMapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, Visibility.ANY);
				objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				objectMapper.configure(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
				objectMapper.getDeserializationConfig().withDateFormat(dateFormat);
				FindMutablePointGrantEntryResponseDO  findMutablePointGrantEntryResponseDO = objectMapper.readValue(body, FindMutablePointGrantEntryResponseDO.class);
				
				List<PointGrantEntryDO> items = new ArrayList<PointGrantEntryDO>();
				if( null != findMutablePointGrantEntryResponseDO.getPointGrantEntries() ){
					items.addAll(findMutablePointGrantEntryResponseDO.getPointGrantEntries());
				}

				SearchResult<PointGrantEntryDO> result = new SearchResult<PointGrantEntryDO>();
				result.setDocuments(items);
				result.setNumFound(findMutablePointGrantEntryResponseDO.getNumFound());
				
				if (LOG.isDebugEnabled())
					LOG.debug("pms find mutable pointgarntentry access. time=" + (System.currentTimeMillis() - startTime));
				
				return result;
			} else {
				throw new HttpException("status = " + response.getStatusLine().getStatusCode()
						+ ", time=" + (System.currentTimeMillis() - startTime)
						+ ", url=" + resourceConfig.pmsAdminFindMutableEntryEndpoint
						+ ", body=" + body);
			}
		} catch (PmsAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new PmsAccessException(e);
		} finally {
			if( response != null ){
				try{
					response.close();
				}catch (Exception e) {
				}
			}
		}
	}
	
	@Override
	public SearchResult<PointGrantEntryDO> findPointGrantEntry(Set<String> pointGrantRequestIds) {
		if( pointGrantRequestIds == null || pointGrantRequestIds.isEmpty())
			throw new PmsInputException("FindPointGrantEntry Input Value is None.");
		
		CloseableHttpClient client = HttpClients.createDefault();
		
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(resourceConfig.pmsSocketTimeout)
				.setConnectTimeout(resourceConfig.pmsConnectionTimeout)
				.build();
		
		CloseableHttpResponse response = null;
		
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			
			for(String pointGrantRequestId : pointGrantRequestIds){
				params.add(new BasicNameValuePair("pointGrantRequestIds", pointGrantRequestId));
			}
			
			URIBuilder uriBuilder = new URIBuilder();
			uriBuilder.setScheme(resourceConfig.pmsSchema);
			uriBuilder.setHost(resourceConfig.pmsHost);
			uriBuilder.setPort(resourceConfig.pmsPort);
			uriBuilder.setPath(resourceConfig.pmsAdminFindEntryEndpoint);
			// TODO 確認する
			if( !params.isEmpty() ){
				uriBuilder.setParameters(params);
			}
			URI uri = uriBuilder.build();
			
			HttpGet httpGet = new HttpGet(uri);
			httpGet.setConfig(requestConfig);
			
			long startTime = System.currentTimeMillis();
			response = client.execute(httpGet);
			
			String body = EntityUtils.toString(response.getEntity(), "UTF-8");
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				ObjectMapper objectMapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, Visibility.ANY);
				objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				objectMapper.configure(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
				objectMapper.getDeserializationConfig().withDateFormat(dateFormat);
				FindPointGrantEntryResponseDO  findPointGrantEntryResponseDO = objectMapper.readValue(body, FindPointGrantEntryResponseDO.class);
				
				List<PointGrantEntryDO> items = new ArrayList<PointGrantEntryDO>();
				if( findPointGrantEntryResponseDO.getPointGrantEntries() != null){
					items.addAll(findPointGrantEntryResponseDO.getPointGrantEntries());
				}
				
				SearchResult<PointGrantEntryDO> result = new SearchResult<PointGrantEntryDO>();
				result.setDocuments(items);
				result.setNumFound(findPointGrantEntryResponseDO.getNumFound());
				
				if (LOG.isDebugEnabled())
					LOG.debug("pms find pointgarntentry access. time=" + (System.currentTimeMillis() - startTime));
				
				return result;
			}else if( response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND ){
				return new SearchResult<PointGrantEntryDO>();
			} else {
				throw new HttpException("status = " + response.getStatusLine().getStatusCode()
						+ ", time=" + (System.currentTimeMillis() - startTime)
						+ ", url=" + resourceConfig.pmsAdminFindEntryEndpoint
						+ ", body=" + body);
			}
		} catch (PmsAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new PmsAccessException(e);
		} finally {
			if( response != null ){
				try{
					response.close();
				}catch (Exception e) {
				}
			}
		}
	}

	@Override
	public List<PointGrantEntryExecuteStatusResponseDO> updatePointGrantEntryExecuteStatus(
			List<PointGrantEntryExecuteStatusDO> changeStatusPointGrantEntries) {
		
		if( changeStatusPointGrantEntries == null || changeStatusPointGrantEntries.isEmpty())
			throw new PmsInputException("UpdatePointGrantEntryExecuteStatus Input Value is None.");
		
		CloseableHttpClient client = HttpClients.createDefault();
		
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(resourceConfig.pmsSocketTimeout)
				.setConnectTimeout(resourceConfig.pmsConnectionTimeout)
				.build();
		
		CloseableHttpResponse response = null;
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			for( PointGrantEntryExecuteStatusDO input : changeStatusPointGrantEntries ){
				params.add(new BasicNameValuePair(
						"pointGrantEntryExecuteStatusDetails.pointGrantRequestId", 
						input.getPointGrantRequestId()));
				params.add(new BasicNameValuePair(
						"pointGrantEntryExecuteStatusDetails.pointGrantRequestExecMainType", 
						input.getPointGrantRequestExecMainType()));
			}
			
			URIBuilder uriBuilder = new URIBuilder();
			uriBuilder.setScheme(resourceConfig.pmsSchema);
			uriBuilder.setHost(resourceConfig.pmsHost);
			uriBuilder.setPort(resourceConfig.pmsPort);
			uriBuilder.setPath(resourceConfig.pmsAdminUpdateEntryExecuteStatusEndpoint);
			URI uri = uriBuilder.build();
			
			HttpPut httpPut = new HttpPut(uri);
			httpPut.setConfig(requestConfig);
			
			httpPut.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
			
			long startTime = System.currentTimeMillis();
			response = client.execute(httpPut);
			
			String body = EntityUtils.toString(response.getEntity(), "UTF-8");
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				ObjectMapper objectMapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, Visibility.ANY);
				objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				
				List<PointGrantEntryExecuteStatusResponseDO> result = objectMapper.readValue(
										body, 
										new TypeReference<ArrayList<PointGrantEntryExecuteStatusResponseDO>>() {});
				
				if (LOG.isDebugEnabled())
					LOG.debug("pms update pointgarntentry status access. time=" + (System.currentTimeMillis() - startTime));
				
				return result;
			} else {
				throw new HttpException("status = " + response.getStatusLine().getStatusCode()
						+ ", time=" + (System.currentTimeMillis() - startTime)
						+ ", url=" + resourceConfig.pmsAdminUpdateEntryExecuteStatusEndpoint
						+ ", body=" + body);
			}
		} catch (PmsAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new PmsAccessException(e);
		} finally {
			if( response != null ){
				try{
					response.close();
				}catch (Exception e) {
				}
			}
		}
	}
	
	@Override
	public List<ReviewPointSpecialConditionValidateDO> confirmReviewPointSpecialCondition(
			String externalCustomerIdClass, 
			String externalCustomerId,
			String[] specialConditionCodes) {
		if( externalCustomerIdClass == null || externalCustomerId == null || specialConditionCodes == null || specialConditionCodes.length == 0)
			throw new PmsInputException("ConfirmReviewPointSpecialCondition Input Value is None.");
		CloseableHttpClient client = HttpClients.createDefault();
		
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(resourceConfig.pmsSocketTimeout)
				.setConnectTimeout(resourceConfig.pmsConnectionTimeout)
				.build();
		
		CloseableHttpResponse response = null;
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("externalCustomerIdClass", externalCustomerIdClass));
			params.add(new BasicNameValuePair("externalCustomerId",externalCustomerId));
			for( String specialConditionCode : specialConditionCodes )
				params.add(new BasicNameValuePair("specialConditionCodes",specialConditionCode));
			
			URIBuilder uriBuilder = new URIBuilder();
			uriBuilder.setScheme(resourceConfig.pmsSchema);
			uriBuilder.setHost(resourceConfig.pmsHost);
			uriBuilder.setPort(resourceConfig.pmsPort);
			uriBuilder.setPath(resourceConfig.pmsFrontConfirmReviewPointSpecialConditionEndpoint);
			if( !params.isEmpty() ){
				//uriBuilder.setCustomQuery(URLEncodedUtils.format(params, "UTF-8"));
				uriBuilder.setParameters(params);
			}
			URI uri = uriBuilder.build();
			
			HttpGet httpGet = new HttpGet(uri);
			httpGet.setConfig(requestConfig);
			
			long startTime = System.currentTimeMillis();
			response = client.execute(httpGet);
			
			String body = EntityUtils.toString(response.getEntity(), "UTF-8");
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				ObjectMapper objectMapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, Visibility.ANY);
				objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				objectMapper.configure(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
				objectMapper.getDeserializationConfig().withDateFormat(dateFormat);
				ReviewPointSpecialConditionValidateResponseDO  reviewPointSpecialConditionValidateResponseDO = objectMapper.readValue(body, ReviewPointSpecialConditionValidateResponseDO.class);
				
				List<ReviewPointSpecialConditionValidateDO> result = new ArrayList<ReviewPointSpecialConditionValidateDO>();
				if( null != reviewPointSpecialConditionValidateResponseDO.getSpecialConditionValidates() ){
					result.addAll(reviewPointSpecialConditionValidateResponseDO.getSpecialConditionValidates());
				}
				
				if (LOG.isDebugEnabled())
					LOG.debug("pms confirm special condition access. time=" + (System.currentTimeMillis() - startTime));
				
				return result;
			} else {
				throw new HttpException("status = " + response.getStatusLine().getStatusCode()
						+ ", time=" + (System.currentTimeMillis() - startTime)
						+ ", url=" + resourceConfig.pmsFrontConfirmReviewPointSpecialConditionEndpoint
						+ ", body=" + body);
			}
		} catch (PmsAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new PmsAccessException(e);
		} finally {
			if( response != null ){
				try{
					response.close();
				}catch (Exception e) {
				}
			}
		}
	}

	@Override
	public void openService() {
		CloseableHttpClient client = HttpClients.createDefault();
		
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(resourceConfig.pmsSocketTimeout)
				.setConnectTimeout(resourceConfig.pmsConnectionTimeout)
				.build();
		
		CloseableHttpResponse response = null;
			
		try {
			URIBuilder uriBuilder = new URIBuilder();
			uriBuilder.setScheme(resourceConfig.pmsSchema);
			uriBuilder.setHost(resourceConfig.pmsHost);
			uriBuilder.setPort(resourceConfig.pmsPort);
			uriBuilder.setPath(resourceConfig.pmsAdminOpenServiceEndpoint);
			URI uri = uriBuilder.build();
			
			HttpGet httpGet = new HttpGet(uri);
			httpGet.setConfig(requestConfig);
			
			long startTime = System.currentTimeMillis();
			response = client.execute(httpGet);
			
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				if (LOG.isDebugEnabled())
					LOG.debug("pms open service access. time=" + (System.currentTimeMillis() - startTime));
				
				return;
			} else {
				throw new HttpException("status = " + response.getStatusLine().getStatusCode()
						+ ", time=" + (System.currentTimeMillis() - startTime)
						+ ", url=" + resourceConfig.pmsAdminOpenServiceEndpoint);
			}
		} catch (PmsAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new PmsAccessException(e);
		} finally {
			if( response != null ){
				try{
					response.close();
				}catch (Exception e) {
				}
			}
		}
	}

	@Override
	public void closeService() {
		CloseableHttpClient client = HttpClients.createDefault();
		
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(resourceConfig.pmsSocketTimeout)
				.setConnectTimeout(resourceConfig.pmsConnectionTimeout)
				.build();
		
		CloseableHttpResponse response = null;
		try {
			URIBuilder uriBuilder = new URIBuilder();
			uriBuilder.setScheme(resourceConfig.pmsSchema);
			uriBuilder.setHost(resourceConfig.pmsHost);
			uriBuilder.setPort(resourceConfig.pmsPort);
			uriBuilder.setPath(resourceConfig.pmsAdminCloseServiceEndpoint);
			URI uri = uriBuilder.build();
			
			HttpGet httpGet = new HttpGet(uri);
			httpGet.setConfig(requestConfig);
			
			long startTime = System.currentTimeMillis();
			response = client.execute(httpGet);
			
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				if (LOG.isDebugEnabled())
					LOG.debug("pms close service access. time=" + (System.currentTimeMillis() - startTime));
				
				return;
			} else {
				throw new HttpException("status = " + response.getStatusLine().getStatusCode()
						+ ", time=" + (System.currentTimeMillis() - startTime)
						+ ", url=" + resourceConfig.pmsAdminCloseServiceEndpoint);
			}
		} catch (PmsAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new PmsAccessException(e);
		} finally {
			if( response != null ){
				try{
					response.close();
				}catch (Exception e) {
				}
			}
		}
	}

	@Override
	public Boolean isService() {
		CloseableHttpClient client = HttpClients.createDefault();
		
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(resourceConfig.pmsSocketTimeout)
				.setConnectTimeout(resourceConfig.pmsConnectionTimeout)
				.build();
		
		CloseableHttpResponse response = null;
		try {
			URIBuilder uriBuilder = new URIBuilder();
			uriBuilder.setScheme(resourceConfig.pmsSchema);
			uriBuilder.setHost(resourceConfig.pmsHost);
			uriBuilder.setPort(resourceConfig.pmsPort);
			uriBuilder.setPath(resourceConfig.pmsAdminIsServiceEndpoint);
			URI uri = uriBuilder.build();
			
			HttpGet httpGet = new HttpGet(uri);
			httpGet.setConfig(requestConfig);
			
			long startTime = System.currentTimeMillis();
			response = client.execute(httpGet);
			
			String body = EntityUtils.toString(response.getEntity(), "UTF-8");
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				if (LOG.isDebugEnabled())
					LOG.debug("pms is service access. time=" + (System.currentTimeMillis() - startTime));
				
				ObjectMapper objectMapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, Visibility.ANY);
				objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				IsServiceResponseDO result = objectMapper.readValue(body, IsServiceResponseDO.class);
				
				return result.getService();
			} else {
				throw new HttpException("status = " + response.getStatusLine().getStatusCode()
						+ ", time=" + (System.currentTimeMillis() - startTime)
						+ ", url=" + resourceConfig.pmsAdminIsServiceEndpoint
						+ ", body=" + body);
			}
		} catch (PmsAccessException e) {
			throw e;
		} catch (Exception e) {
			throw new PmsAccessException(e);
		} finally {
			if( response != null ){
				try{
					response.close();
				}catch (Exception e) {
				}
			}
		}
	}

	/**
	 * ポイント付与権利追加インターフェースのレスポンスです。
	 * @author kamiike
	 *
	 */
	public static class PointGrantEntryResponseDO extends BaseDO {

		/**
		 *
		 */
		private static final long serialVersionUID = 3645985785427757087L;

		/**
		 * ポイント付与申請ID
		 */
		private String pointGrantRequestId;

		/**
		 * @return pointGrantRequestId
		 */
		public String getPointGrantRequestId() {
			return pointGrantRequestId;
		}

		/**
		 * @param pointGrantRequestId セットする pointGrantRequestId
		 */
		public void setPointGrantRequestId(String pointGrantRequestId) {
			this.pointGrantRequestId = pointGrantRequestId;
		}

	}

	/**
	 * 特別ポイント付与予約のレスポンスです。
	 * @author kamiike
	 *
	 */
	public static class ReserveSpecialArrivalPointEntryResponseDO extends BaseDO {

		/**
		 *
		 */
		private static final long serialVersionUID = 4741539420440468659L;

		private Integer arrivalPointRanking;


		public ReserveSpecialArrivalPointEntryResponseDO() {
		}

		public ReserveSpecialArrivalPointEntryResponseDO(
				Integer arrivalPointRanking) {
			this.arrivalPointRanking = arrivalPointRanking;
		}

		public Integer getArrivalPointRanking() {
			return arrivalPointRanking;
		}

		public void setArrivalPointRanking(Integer arrivalPointRanking) {
			this.arrivalPointRanking = arrivalPointRanking;
		}


	}
	
	public static class IsServiceResponseDO extends BaseDO{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -423879464862544141L;

		private Boolean service;

		public IsServiceResponseDO() {
		}

		public IsServiceResponseDO(Boolean service) {
			this.service = service;
		}

		public Boolean getService() {
			return service;
		}

		public void setService(Boolean service) {
			this.service = service;
		}

	}

}
