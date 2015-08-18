/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.kickmogu.yodobashi.community.resource.config.SocialConfig;
import com.kickmogu.yodobashi.community.resource.dao.ShortUrlDao;

/**
 * ショートURLを管理するDaoです。
 * @author hirabayashi
 *
 */
@Service
public class ShortUrlDaoImpl implements ShortUrlDao {
	/**
	 * このクラスに関するログを出力するためのインスタンスです。
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ShortUrlDaoImpl.class);

	/**
	 * ショートURLに変換します。
	 * @param url ロングURL
	 * @return ショートURL
	 */
	@Override
	public String convertShortUrl(String url) {
		CloseableHttpClient client = HttpClients.createDefault();
		
		CloseableHttpResponse response = null;
		
		String shortUrl=null;
		
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("login", SocialConfig.INSTANCE.bitlyUserName));
			params.add(new BasicNameValuePair("apiKey", SocialConfig.INSTANCE.bitlyApiKey));
			params.add(new BasicNameValuePair("longUrl", url));
			params.add(new BasicNameValuePair("format", "json"));
			
			URIBuilder uriBuilder = new URIBuilder(SocialConfig.INSTANCE.bitlyUrl);
			// TODO 確認する
			uriBuilder.setParameters(params);
			URI uri = uriBuilder.build();
			
			HttpGet httpGet = new HttpGet(uri);
			response = client.execute(httpGet);
			try{
				if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
					String entity = EntityUtils.toString(response.getEntity());
					JSONObject jsonEntity = new JSONObject(entity);
					if (jsonEntity != null) {
						JSONObject jsonResults = jsonEntity.optJSONObject("data");
						if (jsonResults != null) {
							shortUrl = jsonResults.optString("url");
						}
					}
				}
			}finally{
				response.close();
			}
		} catch (IOException e) {
			LOG.error(e.toString());
			return url;
		} catch (JSONException e) {
			LOG.error(e.toString());
			return url;
		} catch (Exception e){
			LOG.error(e.toString());
			return url;
		}finally{
			try {
				client.close();
			} catch (IOException e) {
				LOG.error("bitlyUrl short url", e);
			}
		}
		
		if( shortUrl == null )
			return url;
		
		return shortUrl;
	}

}
