package com.yodobashi.esa.usm.deleteuniversalsession;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was generated by Apache CXF 2.4.2
 * 2011-11-16T18:55:56.226+09:00
 * Generated source version: 2.4.2
 * 
 */
@WebService(targetNamespace = "http://esa.yodobashi.com/USM/DeleteUniversalSession", name = "DeleteUniversalSession")
@XmlSeeAlso({com.yodobashi.esa.common.ObjectFactory.class, ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface DeleteUniversalSession {

    @WebResult(name = "DeleteUniversalSession_Response", targetNamespace = "http://esa.yodobashi.com/USM/DeleteUniversalSession", partName = "DeleteUniversalSession_Response")
    @WebMethod(operationName = "DeleteUniversalSession", action = "http://sap.com/xi/WebService/soap1.1")
    public DeleteUniversalSessionResponse deleteUniversalSession(
        @WebParam(partName = "DeleteUniversalSession_Req", name = "DeleteUniversalSession_Req", targetNamespace = "http://esa.yodobashi.com/USM/DeleteUniversalSession")
        DeleteUniversalSessionReq deleteUniversalSessionReq
    );
}
