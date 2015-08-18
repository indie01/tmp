package com.kickmogu.yodobashi.community.resource.dao.stored;

import java.net.InetAddress;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;

public class NotifyUpdateContorlStoredProcedure extends StoredProcedure {
	
	private static final String SPROC_NAME = "CMS_NOTIFYUPDATE";
	
	private static final String IN_DATA_TYPE = "dataType";
	private static final String IN_DATA_ID = "dataID";
	private static final String IN_UPDATE_TYPE = "updateType";
	private static final String IN_ECTERNAL_SYSTEM = "externalSystem";
	private static final String IN_OBJECT_KEY = "objectKey";
	private static final String IN_TERMINAL_ID = "terminalID";
	private static final String IN_TRACE_ID = "traceID";
	private static final String IN_PRIORITY_FLAG = "priorityFlag";
	private static final String IN_STATUS = "status";
	private static final String OUT_ERROR_NO = "errno";
	
	private static final String NOTIFY_UPDATE_CONTROL_DATA_TYPE = "communityReviewPointSummary"; // 商品レビューポイントサマリーのデータタイプ
	private static final String NOTIFY_UPDATE_CONTROL_UPDATE_TYPE = "2"; // 1:新規　2:更新　3:削除
	private static final String NOTIFY_UPDATE_CONTROL_EXTERNAL_SYSTEM = "CMSWS";
	private static final Integer NOTIFY_UPDATE_CONTROL_PRIORITY_FLAG = Integer.valueOf(1); // 0:通常　1:優先
	private static final Integer NOTIFY_UPDATE_CONTROL_STATUS = Integer.valueOf(2); // 2:データベース登録済み
	
	private static final String SYSTEM_PREFIX = "COMMUNITY";
	
	public NotifyUpdateContorlStoredProcedure(DataSource ds) {
		super(ds, SPROC_NAME);
		declareParameter(new SqlParameter(IN_DATA_TYPE, Types.VARCHAR));
		declareParameter(new SqlParameter(IN_DATA_ID, Types.VARCHAR));
		declareParameter(new SqlParameter(IN_UPDATE_TYPE, Types.VARCHAR));
		declareParameter(new SqlParameter(IN_ECTERNAL_SYSTEM, Types.VARCHAR));
		declareParameter(new SqlParameter(IN_OBJECT_KEY, Types.VARCHAR));
		declareParameter(new SqlParameter(IN_TERMINAL_ID, Types.VARCHAR));
		declareParameter(new SqlParameter(IN_TRACE_ID, Types.VARCHAR));
		declareParameter(new SqlParameter(IN_PRIORITY_FLAG, Types.NUMERIC));
		declareParameter(new SqlParameter(IN_STATUS, Types.NUMERIC));
		declareParameter(new SqlOutParameter(OUT_ERROR_NO, Types.NUMERIC));
		compile();
	}

	public Map<String, Object> execute(String sku)throws DataAccessException {
		Map<String, Object> inParams = new HashMap<String, Object>();
		inParams.put(IN_DATA_TYPE, NOTIFY_UPDATE_CONTROL_DATA_TYPE);
		inParams.put(IN_DATA_ID, sku);
		inParams.put(IN_UPDATE_TYPE, NOTIFY_UPDATE_CONTROL_UPDATE_TYPE);
		inParams.put(IN_ECTERNAL_SYSTEM, NOTIFY_UPDATE_CONTROL_EXTERNAL_SYSTEM);
		inParams.put(IN_OBJECT_KEY, null);
		inParams.put(IN_TERMINAL_ID, null);
		inParams.put(IN_TRACE_ID, createTraceID(sku));
		inParams.put(IN_PRIORITY_FLAG, NOTIFY_UPDATE_CONTROL_PRIORITY_FLAG);
		inParams.put(IN_STATUS, NOTIFY_UPDATE_CONTROL_STATUS);
		return super.execute(inParams);
	}
	
	private String createTraceID(String id) {
		String targetTimeString = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		StringBuilder buffer = new StringBuilder();
		buffer.append(getHostName());
		buffer.append("@");
		buffer.append(SYSTEM_PREFIX);
		buffer.append("-");
		buffer.append(NOTIFY_UPDATE_CONTROL_DATA_TYPE);
		buffer.append("-");
		buffer.append(id);
		buffer.append("-");
		buffer.append(targetTimeString);
		return buffer.toString();
	}
	
	private String getHostName() {
	    try {
	        return InetAddress.getLocalHost().getHostName();
	    }catch (Exception e) {
	        e.printStackTrace();
	    }
	    return "UnknownHost";
	}
}
