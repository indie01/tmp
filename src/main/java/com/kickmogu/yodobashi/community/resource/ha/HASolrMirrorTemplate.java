package com.kickmogu.yodobashi.community.resource.ha;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.kickmogu.lib.core.resource.Site;
import com.kickmogu.lib.solr.SolrContainer;
import com.kickmogu.lib.solr.SolrOperations;
import com.kickmogu.lib.solr.impl.SolrMirrorTemplate;

public class HASolrMirrorTemplate extends SolrMirrorTemplate {

	private HAManager haManager;
	
	protected Map<SolrOperations,SolrContainer> containerMap = Maps.newHashMap();

	public HASolrMirrorTemplate(HAManager haManager, SolrOperations primaryOperations,
			SolrOperations secondaryOperations, SolrContainer site1Container, SolrContainer site2Container, Site mySite) {
		super(primaryOperations, secondaryOperations, mySite);
		containerMap.put(primaryOperations, site1Container);
		containerMap.put(secondaryOperations, site2Container);
		this.haManager = haManager;
	}
	
	@Override
	protected SolrOperations getReadOperations() {
		return HAMirrorTemplateUtil.getReadOperations(SolrOperations.class, haManager, mySite, primaryOperations, secondaryOperations);
	}

	@Override
	protected List<SolrOperations> getUpdateOperations(Class<?> type) {
		List<SolrOperations> result = HAMirrorTemplateUtil.getUpdateOperations(SolrOperations.class, haManager, allOperation);
		if (result.size() == 1) return result;
		
		// SITE1が登録成功して、SITE2が登録失敗することによる不整合を極力減らすため
		// SITE2のSolrMasterが全部死んでいたら、事前にResourceBusyExceptionを投げるようにした
		SolrContainer solrContainer = containerMap.get(result.get(1));
		solrContainer.getMeta().getSchemaMeta(type).getServerGroup().getUpdateSolrServer();

		return result;
	}

}
