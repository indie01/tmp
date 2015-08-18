package com.yodobashi.esa.auth.validateauthsession;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was generated by Apache CXF 2.5.2
 * 2012-06-18T12:41:10.787+09:00
 * Generated source version: 2.5.2
 * 
 */
@WebService(targetNamespace = "http://esa.yodobashi.com/AUTH/ValidateAuthSession", name = "ValidateAuthSession")
@XmlSeeAlso({com.yodobashi.esa.common.ObjectFactory.class, com.yodobashi.esa.auth.type.ObjectFactory.class, ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface ValidateAuthSession {

    @WebResult(name = "ValidateAuthSession_Response", targetNamespace = "http://esa.yodobashi.com/AUTH/ValidateAuthSession", partName = "ValidateAuthSession_Response")
    @WebMethod(operationName = "ValidateAuthSession", action = "http://sap.com/xi/WebService/soap1.1")
    public ValidateAuthSessionResponse validateAuthSession(
        @WebParam(partName = "ValidateAuthSession_Req", name = "ValidateAuthSession_Req", targetNamespace = "http://esa.yodobashi.com/AUTH/ValidateAuthSession")
        ValidateAuthSessionReq validateAuthSessionReq
    );
}
