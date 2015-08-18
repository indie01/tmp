package com.kickmogu.yodobashi.community.resource.ha;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.aop.NestedMethodAopHandler;
import com.kickmogu.lib.core.resource.Site;

public class HAMirrorTemplateUtil {
	public static Log log = LogFactory.getLog(HAMirrorTemplateUtil.class);

	private HAMirrorTemplateUtil(){}
	
	public static <T> T getReadOperations(Class<T> operationsType, HAManager haManager, Site mySyte, T site1Operations, T site2Operations) {
		HAInfo haInfo = getHAInfo(haManager);
		if (haInfo.isNormal()) return Site.SITE1.equals(mySyte) ? site1Operations : site2Operations;
		return haInfo.getOneLungSite().equals(Site.SITE1) ? site1Operations : site2Operations;
	}
	
	public static <T> T getLockOperations(Class<T> operationsType, HAManager haManager, T site1Operations, T site2Operations) {
		HAInfo haInfo = getHAInfo(haManager);
		if (haInfo.isNormal()) return site1Operations;
		return haInfo.getOneLungSite().equals(Site.SITE1) ? site1Operations : site2Operations;
	}

	public static <T> T getPrimaryUpdateOperations(Class<T> operationsType, HAManager haManager, T site1Operations, T site2Operations) {
		HAInfo haInfo = getHAInfo(haManager);
		if (haInfo.isNormal()) return site1Operations;
		return haInfo.getOneLungSite().equals(Site.SITE1) ? site1Operations : site2Operations;
	}
	
	public static <T> T getSecondaryUpdateOperations(Class<T> operationsType, HAManager haManager, T site1Operations, T site2Operations) {
		HAInfo haInfo = getHAInfo(haManager);
		if (haInfo.isNormal()) return site2Operations;
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> getUpdateOperations(Class<T> operationsType, HAManager haManager, List<T> operationList) {
		HAInfo haInfo = getHAInfo(haManager);
		if (haInfo.isNormal() || haInfo.isOneLungOnRef()) return operationList;
		return haInfo.getOneLungSite().equals(Site.SITE1) ? Lists.newArrayList(operationList.get(0)) : Lists.newArrayList(operationList.get(1));
	}
	
	private static HAInfo getHAInfo(HAManager haManager) {
		HAInfo haInfo = NestedMethodAopHandler.getContext().getAttribute(HAInfo.class, HAInfo.class.getSimpleName());
		if (haInfo == null) {
			haInfo = haManager.getHAInfo();
			NestedMethodAopHandler.getContext().setAttribute(HAInfo.class.getSimpleName(), haInfo);
		}
		//log.debug("getHAInfo:" + haInfo);
		return haInfo;
	}
}
