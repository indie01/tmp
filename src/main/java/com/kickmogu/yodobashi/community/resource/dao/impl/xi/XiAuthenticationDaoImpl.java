package com.kickmogu.yodobashi.community.resource.dao.impl.xi;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.yodobashi.community.common.utils.XiUtil;
import com.kickmogu.yodobashi.community.resource.annotation.BackendWebServiceClient;
import com.kickmogu.yodobashi.community.resource.annotation.BackendWebServiceClientAware;
import com.kickmogu.yodobashi.community.resource.dao.AuthenticationDao;
import com.kickmogu.yodobashi.community.resource.domain.ValidateAuthSessionDO;
import com.yodobashi.esa.auth.type.NvPair;
import com.yodobashi.esa.auth.validateauthsession.ValidateAuthSession;
import com.yodobashi.esa.auth.validateauthsession.ValidateAuthSessionReq;
import com.yodobashi.esa.auth.validateauthsession.ValidateAuthSessionResponse;
import com.yodobashi.esa.auth.validateauthsession.ValidateAuthSessionV2;
import com.yodobashi.esa.auth.validateauthsession.ValidateAuthSessionV2Req;
import com.yodobashi.esa.auth.validateauthsession.ValidateAuthSessionV2Response;

@Service @Qualifier("xi") @BackendWebServiceClientAware
public class XiAuthenticationDaoImpl  implements AuthenticationDao {

	@Override
	/**
	 * 認証セッションID の有効性確認します。
	 * @param　認証キー　オートログインID
	 * @return true:有効
	 */
	public boolean isValidateAuthSession(String authKey) {
		ValidateAuthSessionReq request = new ValidateAuthSessionReq();
		request.setAuthSessionID(authKey);
		request.setNewTimeout(new Integer(3*30*24*60*60));	//3ヶ月
		request.setCOMMONINPUT(new com.yodobashi.esa.common.COMMONINPUT());
		XiUtil.fillCommonInput(ValidateAuthSession.class,request.getCOMMONINPUT());
		ValidateAuthSessionResponse response = validateAuthSessionClient.validateAuthSession(request);
		if (response == null )
			return false;
		
		XiUtil.checkResponse(response.getCOMMONRETURN());
		
		if (response.getAuthReturn() == null
			|| StringUtils.isEmpty(response.getAuthReturn().getAuthSessionID())) {
			return false;
		}
		return true;
	}

	@Override
	public ValidateAuthSessionDO validateAuthSessionV2(String authKey, Map<String, String> params) {
		// リクエストパラメーターの設定
		ValidateAuthSessionV2Req request = new ValidateAuthSessionV2Req();
		request.setAuthSessionID(authKey);
		request.setNewTimeout(new Integer(3*30*24*60*60));//3ヶ月
		request.setCOMMONINPUT(new com.yodobashi.esa.common.COMMONINPUT());
		XiUtil.fillCommonInput(ValidateAuthSessionV2.class,request.getCOMMONINPUT());
		// NvPairの設定
		setNvPairs(request, params);
		
		// 認証実行
		ValidateAuthSessionV2Response response = validateAuthSessionV2Client.validateAuthSessionV2(request);
		
		// 認証結果の設定
		ValidateAuthSessionDO result = new ValidateAuthSessionDO();
		
		if (response == null ) {
			result.setAuth(false);
			result.setChangeSession(false);
			result.setCurrentAutoId(authKey);
			
			return result;
		}
		
		XiUtil.checkResponse(response.getCOMMONRETURN());
		
		if (response.getAuthReturn() == null
			|| StringUtils.isEmpty(response.getAuthReturn().getAuthSessionID())) {
			result.setAuth(false);
			result.setChangeSession(false);
			result.setCurrentAutoId(authKey);
			result.setNewAuthId(null);
			return result;
		}
		
		result.setAuth(true);
		result.setCurrentAutoId(authKey);
		result.setCustomerCode(response.getAuthReturn().getCustomerCode());
		
		if( response.getAuthReturn().isAuthSessionIDUpdated() ) {
			result.setChangeSession(true);
			result.setNewAuthId(response.getAuthReturn().getAuthSessionID());
		}
		
		return result;
	}

	private void setNvPairs(ValidateAuthSessionV2Req request, Map<String, String> params){
		if( params == null || params.isEmpty() )
			return;
		
		NvPair nvPair = null;
		for (Iterator<Entry<String, String>> it = params.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, String> entry = it.next();
			nvPair = new NvPair();
			nvPair.setName(entry.getKey());
			nvPair.setValue(entry.getValue());
			request.getNvPair().add(nvPair);
		}
	}


	/**
	 * 認証セッションID の有効性確認です。
	 */
	@BackendWebServiceClient(
		endPointUrlPropertyKey="endpoint.validateAuthSession"
//		,
//		usernamePropertyKey="xi.auth.user",
//		passwordPropertyKey="xi.auth.passwd"
	)
	private ValidateAuthSession validateAuthSessionClient;
	
	@BackendWebServiceClient(
			endPointUrlPropertyKey="endpoint.validateAuthSessionV2"
//			,
//			usernamePropertyKey="xi.auth.user",
//			passwordPropertyKey="xi.auth.passwd"
		)
	private ValidateAuthSessionV2 validateAuthSessionV2Client;
}
