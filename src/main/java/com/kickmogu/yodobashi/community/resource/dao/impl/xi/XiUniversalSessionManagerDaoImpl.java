package com.kickmogu.yodobashi.community.resource.dao.impl.xi;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.yodobashi.community.common.exception.XiAccessException;
import com.kickmogu.yodobashi.community.common.utils.XiUtil;
import com.kickmogu.yodobashi.community.resource.annotation.BackendWebServiceClient;
import com.kickmogu.yodobashi.community.resource.annotation.BackendWebServiceClientAware;
import com.kickmogu.yodobashi.community.resource.dao.UniversalSessionManagerDao;
import com.kickmogu.yodobashi.community.resource.domain.constants.UniversalSessionStatus;
import com.yodobashi.esa.common.COMMONINPUT;
import com.yodobashi.esa.usm.deleteuniversalsession.DeleteUniversalSession;
import com.yodobashi.esa.usm.deleteuniversalsession.DeleteUniversalSessionReq;
import com.yodobashi.esa.usm.deleteuniversalsession.DeleteUniversalSessionResponse;
import com.yodobashi.esa.usm.searchuniversalsession.SearchUniversalSession;
import com.yodobashi.esa.usm.searchuniversalsession.SearchUniversalSessionReq;
import com.yodobashi.esa.usm.searchuniversalsession.SearchUniversalSessionResponse;
import com.yodobashi.esa.usm.type.NvPair;

@Service @Qualifier("xi") @BackendWebServiceClientAware
public class XiUniversalSessionManagerDaoImpl  implements UniversalSessionManagerDao {

	/**
	 * ユニバーサルセッションのレルム名です。
	 */
	public static final String REALM = "communitySession";

	/**
	 * 外部顧客IDのパラメーター名です。
	 */
	public static final String OUTER_CUSTOMER_ID_NAME = "outerCustomerId";

	/**
	 * ユニバーサルセッション削除です。
	 */
	@BackendWebServiceClient(
		endPointUrlPropertyKey="endpoint.deleteUniversalSession",
		usernamePropertyKey="xi.auth.user",
		passwordPropertyKey="xi.auth.passwd"
	)
	private DeleteUniversalSession deleteUniversalSessionClient;

	/**
	 * ユニバーサルセッション削除です。
	 */
	@BackendWebServiceClient(
		endPointUrlPropertyKey="endpoint.searchUniversalSession",
		usernamePropertyKey="xi.auth.user",
		passwordPropertyKey="xi.auth.passwd"
	)
	private SearchUniversalSession searchUniversalSessionClient;

	/**
	 * ユニバーサルセッションIDを削除します。
	 * @param universalSessionID ユニバーサルセッションID
	 */
	@Override
	public void deleteUniversalSession(String universalSessionID) {
		DeleteUniversalSessionReq request = new DeleteUniversalSessionReq();
		request.setUniversalSessionID(universalSessionID);
		request.setCOMMONINPUT(new COMMONINPUT());
		XiUtil.fillCommonInput(DeleteUniversalSession.class,
				request.getCOMMONINPUT());
		DeleteUniversalSessionResponse response = deleteUniversalSessionClient.deleteUniversalSession(request);

		XiUtil.checkResponse(response.getCOMMONRETURN());
	}

	/**
	 * 指定したユニバーサルセッションに保存した外部顧客IDを取得します。
	 * @param universalSessionID ユニバーサルセッションID
	 * @return 外部顧客ID
	 */
	@Override
	public String loadOuterCustomerId(String universalSessionID) {
		SearchUniversalSessionReq request = new SearchUniversalSessionReq();
		request.setUniversalSessionID(universalSessionID);
		request.setCOMMONINPUT(new COMMONINPUT());
		XiUtil.fillCommonInput(SearchUniversalSession.class,
				request.getCOMMONINPUT());
		SearchUniversalSessionResponse response = searchUniversalSessionClient.searchUniversalSession(request);

		XiUtil.checkResponse(response.getCOMMONRETURN());

		if (!response.getUniversalSessionReturn().getRealm().equals(REALM)) {
			throw new XiAccessException(
					"Realm is not match. expected='" + REALM + "' input="
					+ response.getUniversalSessionReturn().getRealm());
		}

		if (UniversalSessionStatus.codeOf(
				response.getUniversalSessionReturn().getStatus()).expired()) {
			throw new XiAccessException(
					"UniversalSession is expired. universalSessionID=" + universalSessionID);
		}
		for (NvPair nv : response.getUniversalSessionReturn().getNvPair()) {
			if (nv.getName().equals(OUTER_CUSTOMER_ID_NAME)) {
				return nv.getValue();
			}
		}

		throw new XiAccessException(
				"UniversalSessionData is not found. universalSessionID=" + universalSessionID);
	}
}
