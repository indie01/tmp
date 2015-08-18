package com.kickmogu.yodobashi.community.resource.ha;

import java.util.List;

import com.kickmogu.lib.core.resource.Site;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.impl.HBaseMirrorTemplate;

public class HAHBaseMirrorTemplate extends HBaseMirrorTemplate {
	
	private HAManager haManager;

	public HAHBaseMirrorTemplate(HAManager haManager, HBaseOperations primaryOperations,
			HBaseOperations secondaryOperations, Site mySite) {
		super(primaryOperations, secondaryOperations, mySite);
		this.haManager = haManager;
	}

	@Override
	public HBaseOperations getReadOperations() {
		return HAMirrorTemplateUtil.getReadOperations(HBaseOperations.class, haManager, mySite, primaryOperations, secondaryOperations);
	}

	public HBaseOperations getLockOperations() {
		return HAMirrorTemplateUtil.getLockOperations(HBaseOperations.class, haManager, primaryOperations, secondaryOperations);
	}
	
	public HBaseOperations getPrimaryUpdateOperations() {
		return HAMirrorTemplateUtil.getPrimaryUpdateOperations(HBaseOperations.class, haManager, primaryOperations, secondaryOperations);
	}
	
	public HBaseOperations getSecondaryUpdateOperations() {
		return  HAMirrorTemplateUtil.getSecondaryUpdateOperations(HBaseOperations.class, haManager, primaryOperations, secondaryOperations);
	}

	@Override
	public List<HBaseOperations> getUpdateOperations() {
		return HAMirrorTemplateUtil.getUpdateOperations(HBaseOperations.class, haManager, allOperation);
	}

}
