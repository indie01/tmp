package com.kickmogu.yodobashi.community.resource.cache;

import java.io.Serializable;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class JaxRsCacheContentsFilter implements CacheContentsFilter {

	@Override
	public Serializable toCacheContents(Object org) throws NoCacheException {
		Response response = (Response)org;
		if (response.getStatus() != Status.OK.getStatusCode()) throw new NoCacheException();
		return (Serializable)response.getEntity();
	}

	@Override
	public Object fromCacheContents(Serializable cacheContents) {
		return Response.ok(cacheContents).build();
	}

}
